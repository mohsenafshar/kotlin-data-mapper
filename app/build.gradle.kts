import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val isCI = providers.systemProperty("isCI")

plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        if (isCI.isPresent) {
            intellijIdeaCommunity("2024.2.4")
        } else {
            local("H:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.2.4")
        }

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name

    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    signing {
        certificateChainFile = file("chain.crt")
        privateKeyFile = file("private.pem")
        password = "admin123"
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }

    if (isCI.isPresent) {
        pluginVerification {
            ides {
                recommended()
                select {
                    types = listOf(
                        IntelliJPlatformType.IntellijIdeaCommunity,
                        IntelliJPlatformType.IntellijIdeaUltimate,
                        IntelliJPlatformType.AndroidStudio
                    )
//                    sinceBuild = "233"
//                untilBuild = "243.*"
                }
            }
        }
    }
}

//changelog {
//    version.set(project.version.toString())
//    path.set(file("${project.projectDir}/CHANGELOG.md").canonicalPath)
//    header.set(provider { "[${version.get()}] - ${date()}" })
//    headerParserRegex.set("""(\d+\.\d+\.\d+(-[a-zA-Z0-9]+)?)""".toRegex())
//    itemPrefix.set("-")
//    keepUnreleasedSection.set(true)
//    unreleasedTerm.set("[Unreleased]")
//    groups.set(listOf("Feature", "Bug Fix", "Improvement", "Removal"))
//    combinePreReleases.set(true)
//}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks {
    patchPluginXml {
        sinceBuild = providers.gradleProperty("pluginSinceBuild")
        untilBuild = provider { null }

        changeNotes.set(provider {
            val version = project.version.toString() // Get the version from the project
            val changelogItem = changelog.get(version) // Get the changelog item for that version
            val rendered = changelog.renderItem(
                changelogItem,
                Changelog.OutputType.HTML
            )
            println("Rendered Change Notes: $rendered")
            rendered
        })
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    test {
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }
    }
}
