# Learn Page Update Action

This GitHub action updates the `#learn` page for a specified Spring project.

It can be used in a workflow like so:

```yaml
name: Update Learn Page
uses: jzheaux/learn-page-update-action@v1
```

Such a declaration will do the following:

1. Look up your current project version
2. Delete any release versions in your project's generation
3. Post a release version for the current version and the next snapshot

[NOTE]
If your project is releasing a SNAPSHOT version, it will only delete and update the SNAPSHOT release entries

For a `7.0.1` release, a project's learn page likely has release entries for `7.0.0` and `7.0.1-SNAPSHOT`.
Running in the above will result in new entries, `7.0.1` and `7.0.2-SNAPSHOT`, replacing the old entries `7.0.0` and `7.0.1-SNAPSHOT` respectively.

## Arguments

This action supports the following arguments:

| Argument           | Description                                                                                        | Default Value                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| project-slug       | your project's website slug                                                                        | the name of the repository, less any -commercial suffix                          
| version            | the version of the project being built                                                             | the value found in your `gradle.properties` or `pom.xml` file                    |
| website-repository | the website repository, like spring-io/spring-website-content, to pull and push changes from       | spring-website-content for OSS, spring-website-commercial-cotnent for Commercial |
| github-token       | the GitHub token for the website repository                                                        | https://api.spring.io                                                            |
| api-doc-url        | the link to your project's JavaDoc; supports `{name}` and `{version}` placeholders                 | https://docs.spring.io/{project}/site/docs/{version}/api/                        |
| ref-doc-url        | the link to your project's reference documentation; supports `{name}` and `{version}` placeholders | https://docs.spring.io/{project}/reference/{version}/index.html                  |
| is-antora          | whether your reference documentation is Antora-based                                               | true                                                                             |

For example, if your links are not Antora-based, you can do something like the following:

```yaml
name: Update Learn Page
uses: jzheaux/learn-page-update-action@v1
inputs:
  api-doc-url: https://docs.spring.io/{name}/{version}/api/java/index.html
  ref-doc-url: https://docs.spring.io/{name}/docs/{version}/reference/html/
  is-antora: false
```

