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
