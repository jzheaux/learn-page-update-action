# releases-api-sync-action

A GitHub action for syncing a project's releases with those listed on api.spring.io

## Overview

This GitHub Action automatically synchronizes release information between your Spring project repository and the Spring.io releases API. Given a version in `gradle.properties`, it will:

1. Calculate and sync multiple release versions:
   - Previous release (e.g., 6.5.3)
   - Current snapshot (e.g., 6.5.4-SNAPSHOT)
   - Current release (e.g., 6.5.4)
   - Next snapshot (e.g., 6.5.5-SNAPSHOT)

2. Create/update GitHub branches for:
   - Current version branch
   - Next snapshot branch

## Usage

```yaml
name: Sync Releases
on:
  push:
    branches:
      - main

jobs:
  sync-releases:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Sync Releases with API
        uses: jzheaux/releases-api-sync-action@v1
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
          project-id: 'spring-security'
          api-url: 'https://api.spring.io'
          gradle-properties-path: 'gradle.properties'
```

## Inputs

### `token` (required)

GitHub token for authentication. Use `${{ secrets.GITHUB_TOKEN }}` or a personal access token with appropriate permissions.

### `project-id` (required)

The Spring project ID (e.g., `spring-security`, `spring-boot`).

### `api-url` (optional)

The Spring API base URL. Default: `https://api.spring.io`

### `gradle-properties-path` (optional)

Path to the gradle.properties file containing the version. Default: `gradle.properties`

## Outputs

### `synced-versions`

A comma-separated list of versions that were synced with the API.

## Example gradle.properties

```properties
version=6.5.4
```

Based on this version, the action will sync:

- 6.5.3 (GENERAL_AVAILABILITY)
- 6.5.4-SNAPSHOT (SNAPSHOT)
- 6.5.4 (GENERAL_AVAILABILITY, marked as current)
- 6.5.5-SNAPSHOT (SNAPSHOT)

And create/update branches:

- 6.5.4
- 6.5.5-SNAPSHOT

**Note:** For versions with patch 0 (e.g., 6.5.0), the previous version will be the same (6.5.0) as there is no prior patch release in that minor version series.

### `api-token` (optional)

Optional API token for authenticating with the Spring API. If provided, it will be sent as a Bearer token in the Authorization header.

## Development

### Prerequisites

- Node.js 20 or higher
- npm

### Setup

```bash
npm install
```

### Build

```bash
npm run package
```

### Lint

```bash
npm run lint
```

### Format

```bash
npm run format:write
```

## License

Apache-2.0
