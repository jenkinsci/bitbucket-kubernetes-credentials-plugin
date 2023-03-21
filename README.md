# bitbucket-kubernetes-credentials-plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/bitbucket-kubernetes-credentials-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/bitbucket-kubernetes-credentials-plugin/job/main/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/bitbucket-kubernetes-credentials.svg)](https://plugins.jenkins.io/bitbucket-kubernetes-credentials)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/bitbucket-kubernetes-credentials-plugin.svg?label=changelog)](https://github.com/jenkinsci/bitbucket-kubernetes-credentials-plugin/releases/latest)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/bitbucket-kubernetes-credentials-plugin)](https://github.com/jenkinsci/bitbucket-kubernetes-credentials-plugin/blob/main/LICENSE.md)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/bitbucket-kubernetes-credentials.svg?color=blue)](https://plugins.jenkins.io/bitbucket-kubernetes-credentials)


# Introduction

This plugin provides an extension for the [kubernetes-credentials-provider-plugin](https://github.com/jenkinsci/kubernetes-credentials-provider-plugin)
plugin, and the [atlassian-bitbucket-server-integration-plugin](https://github.com/jenkinsci/atlassian-bitbucket-server-integration-plugin) that extend the kubernetes credentials provider to create the special credential type required by the atlassian-bitbucket-server-integration when interacting with a bitbucket server instance.

## Usage

This plugin consumes extends the kubernetes-credentials-provider-plugin to consume kubernetes secrets with a `"jenkins.io/credentials-type"` of `"bitbucketToken"`. These secrets need to have a data property `"text"` that contains a base64 encoded `bearer token` for bitbucket server.

### Example

```
apiVersion: v1
data:
  text: c3VwZXJkdXBlcnNlY3JldA==
kind: Secret
metadata:
  annotations:
    jenkins.io/credentials-description: The Bitbucket token for creating web hooks
  labels:
    jenkins.io/credentials-type: bitbucketToken
  name: bitbucket-hook-token
  namespace: jenkins-demo
type: Opaque
```
## LICENSE

Licensed under MIT, see [LICENSE](LICENSE)
