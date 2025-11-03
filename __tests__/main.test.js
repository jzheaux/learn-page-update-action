import { describe, test, expect } from '@jest/globals'
import { readVersionFromGradleProperties, calculateVersions } from '../src/main.js'
import * as fs from 'fs'
import * as os from 'os'
import * as path from 'path'

describe('readVersionFromGradleProperties', () => {
  test('reads version from valid gradle.properties', () => {
    const testFile = path.join(os.tmpdir(), 'test-gradle.properties')
    fs.writeFileSync(testFile, 'version=6.5.4\n')

    const version = readVersionFromGradleProperties(testFile)
    expect(version).toBe('6.5.4')

    fs.unlinkSync(testFile)
  })

  test('reads version with spaces', () => {
    const testFile = path.join(os.tmpdir(), 'test-gradle2.properties')
    fs.writeFileSync(testFile, 'version = 6.5.4 \n')

    const version = readVersionFromGradleProperties(testFile)
    expect(version).toBe('6.5.4')

    fs.unlinkSync(testFile)
  })

  test('throws error if file not found', () => {
    const testFile = path.join(os.tmpdir(), 'nonexistent.properties')
    expect(() => {
      readVersionFromGradleProperties(testFile)
    }).toThrow('gradle.properties file not found')
  })

  test('throws error if version not found', () => {
    const testFile = path.join(os.tmpdir(), 'test-gradle3.properties')
    fs.writeFileSync(testFile, 'someOtherProperty=value\n')

    expect(() => {
      readVersionFromGradleProperties(testFile)
    }).toThrow('Version not found in gradle.properties')

    fs.unlinkSync(testFile)
  })
})

describe('calculateVersions', () => {
  test('calculates versions for standard version', () => {
    const versions = calculateVersions('6.5.4')

    expect(versions.previous).toBe('6.5.3')
    expect(versions.currentSnapshot).toBe('6.5.4-SNAPSHOT')
    expect(versions.current).toBe('6.5.4')
    expect(versions.nextSnapshot).toBe('6.5.5-SNAPSHOT')
    expect(versions.next).toBe('6.5.5')
  })

  test('calculates versions for snapshot version', () => {
    const versions = calculateVersions('6.5.4-SNAPSHOT')

    expect(versions.previous).toBe('6.5.3')
    expect(versions.currentSnapshot).toBe('6.5.4-SNAPSHOT')
    expect(versions.current).toBe('6.5.4')
    expect(versions.nextSnapshot).toBe('6.5.5-SNAPSHOT')
    expect(versions.next).toBe('6.5.5')
  })

  test('calculates versions for patch 0', () => {
    const versions = calculateVersions('6.5.0')

    expect(versions.previous).toBe('6.5.0')
    expect(versions.currentSnapshot).toBe('6.5.0-SNAPSHOT')
    expect(versions.current).toBe('6.5.0')
    expect(versions.nextSnapshot).toBe('6.5.1-SNAPSHOT')
    expect(versions.next).toBe('6.5.1')
  })

  test('calculates versions for major version', () => {
    const versions = calculateVersions('7.0.0')

    expect(versions.previous).toBe('7.0.0')
    expect(versions.currentSnapshot).toBe('7.0.0-SNAPSHOT')
    expect(versions.current).toBe('7.0.0')
    expect(versions.nextSnapshot).toBe('7.0.1-SNAPSHOT')
    expect(versions.next).toBe('7.0.1')
  })

  test('throws error for invalid version format', () => {
    expect(() => {
      calculateVersions('invalid')
    }).toThrow('Invalid version format')
  })

  test('throws error for version with non-numeric parts', () => {
    expect(() => {
      calculateVersions('6.5.x')
    }).toThrow('Invalid version format')
  })

  test('throws error for version with too few parts', () => {
    expect(() => {
      calculateVersions('6.5')
    }).toThrow('Invalid version format')
  })
})
