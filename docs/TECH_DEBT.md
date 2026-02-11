# Technical Debt

This document tracks known technical debt items in the project.

## Status Legend

- 🔴 **Critical**: Security issue or blocks production usage
- 🟠 **High**: Significant impact on maintainability or reliability
- 🟡 **Medium**: Quality improvement or technical cleanup
- 🟢 **Low**: Nice-to-have improvement

---

## Open Items

### [TD-001] 🔴 Use Environment Variables for Artifactory Credentials

**Status:** Open  
**Priority:** Critical  
**Component:** CLI integration (JFrog CLI service)

#### Problem
Currently, Artifactory credentials (access tokens and passwords) are passed as CLI arguments to the JFrog CLI. This is a **security vulnerability** because:
- CLI arguments show up in `ps` output
- Arguments are visible in `/proc/<pid>/cmdline`
- CI/CD "debug" tooling logs often capture command-line arguments
- Crash dumps may contain command-line data
- Anyone with access to the build agent can read `--access-token` and `--password`

#### Impact
- **Security Risk**: Credentials exposure in logs and process listings
- **Compliance**: May violate security policies for credential handling

#### Suggested Approach
1. Use environment variables for sensitive data:
   - `JFROG_ACCESS_TOKEN` or `ARTIFACTORY_ACCESS_TOKEN`
   - `JFROG_URL` or `ARTIFACTORY_URL`
   - `JFROG_USER` (if needed)
2. Update `JFrogNpmCliServiceImpl` to set environment variables before executing CLI commands
3. Remove credential parameters from CLI command strings
4. Update documentation to reflect the change
5. Consider using JFrog CLI config files (`jfrog config add`) as an alternative

---

### [TD-002] 🟠 Implement Proper Xray Scan Status Check

**Status:** Open  
**Priority:** High  
**Component:** Build Info Integration Service 

#### Problem
Currently, the system uses a hardcoded 1-minute sleep (`Thread.sleep()`) before uploading merged build info to avoid race conditions with Xray indexing. This is a **workaround**, not a proper solution.

**Current code location:**  
`NpmBuildInfoIntegrationServiceImpl.integrateNpmBuildInfo()` - line ~45

#### Impact
- **Reliability**: Race conditions may still occur if Xray takes longer than 1 minute
- **Performance**: Unnecessary delays when Xray indexing completes faster
- **Maintainability**: Magic number timeout is not configurable or adaptive

#### Suggested Approach
1. Use Artifactory REST API to check Xray scan status:
   - Endpoint: `GET /api/xray/scanStatus/build/{buildName}/{buildNumber}`
   - Poll with exponential backoff until scan is complete or timeout
2. Implement retry logic with proper error handling
3. Add logging to indicate polling progress
