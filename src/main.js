import * as core from '@actions/core'
import * as github from '@actions/github'
import * as fs from 'fs'

/**
 * Reads the version from gradle.properties file
 * @param {string} filePath - Path to gradle.properties
 * @returns {string} The version string
 */
export function readVersionFromGradleProperties(filePath) {
  core.info(`Reading version from ${filePath}`)

  if (!fs.existsSync(filePath)) {
    throw new Error(`gradle.properties file not found at ${filePath}`)
  }

  const content = fs.readFileSync(filePath, 'utf-8')
  const versionMatch = content.match(/^version\s*=\s*(.+)$/m)

  if (!versionMatch) {
    throw new Error('Version not found in gradle.properties')
  }

  return versionMatch[1].trim()
}

/**
 * Calculates version information based on the current version
 * @param {string} version - Current version (e.g., "6.5.4")
 * @returns {Object} Version information
 */
export function calculateVersions(version) {
  // Remove -SNAPSHOT suffix if present
  const cleanVersion = version.replace(/-SNAPSHOT$/, '')

  // Parse version components
  const versionParts = cleanVersion.split('.')
  if (versionParts.length !== 3) {
    throw new Error(`Invalid version format: ${version}. Expected format: X.Y.Z`)
  }

  const [major, minor, patch] = versionParts.map(v => parseInt(v, 10))

  if (isNaN(major) || isNaN(minor) || isNaN(patch)) {
    throw new Error(`Invalid version format: ${version}. All parts must be numbers`)
  }

  // Calculate versions
  const currentVersion = `${major}.${minor}.${patch}`
  const previousPatch = patch > 0 ? patch - 1 : 0
  const previousVersion = `${major}.${minor}.${previousPatch}`
  const nextPatch = patch + 1
  const nextVersion = `${major}.${minor}.${nextPatch}`

  return {
    previous: previousVersion,
    currentSnapshot: `${currentVersion}-SNAPSHOT`,
    current: currentVersion,
    nextSnapshot: `${nextVersion}-SNAPSHOT`,
    next: nextVersion
  }
}

/**
 * Syncs a release with the Spring API
 * @param {string} apiUrl - Base API URL
 * @param {string} projectId - Project ID
 * @param {string} version - Version to sync
 * @param {string} status - Release status (SNAPSHOT, GENERAL_AVAILABILITY, etc.)
 * @param {boolean} current - Whether this is the current version
 */
export async function syncReleaseWithApi(apiUrl, projectId, version, status, current = false) {
  core.info(`Syncing release: ${version} with status ${status}`)

  const releaseData = {
    version: version,
    status: status,
    current: current,
    referenceDocUrl: `https://docs.spring.io/${projectId}/docs/${version}/reference/html5/`,
    apiDocUrl: `https://docs.spring.io/${projectId}/docs/${version}/api/`
  }

  try {
    const response = await fetch(`${apiUrl}/projects/${projectId}/releases`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(releaseData)
    })

    if (!response.ok && response.status !== 409) {
      // 409 = already exists
      const errorText = await response.text()
      core.warning(`Failed to sync ${version}: ${response.status} - ${errorText}`)
    } else {
      core.info(`Successfully synced ${version}`)
    }
  } catch (error) {
    core.warning(`Error syncing ${version}: ${error.message}`)
  }
}

/**
 * Creates or updates a branch in the GitHub repository
 * @param {string} token - GitHub token
 * @param {string} owner - Repository owner
 * @param {string} repo - Repository name
 * @param {string} branchName - Branch name to create/update
 * @param {string} baseBranch - Base branch to create from (default: main)
 */
export async function syncBranch(token, owner, repo, branchName, baseBranch = 'main') {
  core.info(`Syncing branch: ${branchName}`)

  const octokit = github.getOctokit(token)

  try {
    // Check if branch exists
    try {
      await octokit.rest.repos.getBranch({
        owner,
        repo,
        branch: branchName
      })
      core.info(`Branch ${branchName} already exists`)
      return
    } catch (error) {
      if (error.status !== 404) {
        throw error
      }
      // Branch doesn't exist, create it
    }

    // Get the SHA of the base branch
    const { data: baseRef } = await octokit.rest.repos.getBranch({
      owner,
      repo,
      branch: baseBranch
    })

    // Create the new branch
    await octokit.rest.git.createRef({
      owner,
      repo,
      ref: `refs/heads/${branchName}`,
      sha: baseRef.commit.sha
    })

    core.info(`Successfully created branch ${branchName}`)
  } catch (error) {
    core.warning(`Error syncing branch ${branchName}: ${error.message}`)
  }
}

/**
 * Main function that orchestrates the release sync
 */
export async function run() {
  try {
    // Get inputs
    const token = core.getInput('token', { required: true })
    const apiUrl = core.getInput('api-url', { required: false }) || 'https://api.spring.io'
    const projectId = core.getInput('project-id', { required: true })
    const gradlePropertiesPath =
      core.getInput('gradle-properties-path', { required: false }) || 'gradle.properties'

    core.info(`Starting release sync for project: ${projectId}`)

    // Read version from gradle.properties
    const version = readVersionFromGradleProperties(gradlePropertiesPath)
    core.info(`Current version from gradle.properties: ${version}`)

    // Calculate version information
    const versions = calculateVersions(version)
    core.info(`Calculated versions:`)
    core.info(`  Previous: ${versions.previous}`)
    core.info(`  Current Snapshot: ${versions.currentSnapshot}`)
    core.info(`  Current: ${versions.current}`)
    core.info(`  Next Snapshot: ${versions.nextSnapshot}`)

    // Sync releases with API
    await syncReleaseWithApi(apiUrl, projectId, versions.previous, 'GENERAL_AVAILABILITY', false)
    await syncReleaseWithApi(apiUrl, projectId, versions.currentSnapshot, 'SNAPSHOT', false)
    await syncReleaseWithApi(apiUrl, projectId, versions.current, 'GENERAL_AVAILABILITY', true)
    await syncReleaseWithApi(apiUrl, projectId, versions.nextSnapshot, 'SNAPSHOT', false)

    // Sync branches
    const { owner, repo } = github.context.repo
    await syncBranch(token, owner, repo, versions.current)
    await syncBranch(token, owner, repo, versions.nextSnapshot)

    // Set output
    const syncedVersions = [
      versions.previous,
      versions.currentSnapshot,
      versions.current,
      versions.nextSnapshot
    ]
    core.setOutput('synced-versions', syncedVersions.join(', '))

    core.info('Release sync completed successfully')
  } catch (error) {
    core.setFailed(error.message)
    throw error
  }
}
