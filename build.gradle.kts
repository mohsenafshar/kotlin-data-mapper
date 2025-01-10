import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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


configurations {
    implementation {
        exclude(group = "io.ktor")
        exclude(group = "com.jetbrains.infra")
        exclude(group = "com.jetbrains.intellij.remoteDev")
    }
}


dependencies {
    intellijPlatform {
//        intellijIdeaCommunity("2024.2.4")
        local("H:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.2.4")

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        jetbrainsRuntime()
        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }

    // IntelliJ IDEA testing tools
    testImplementation("com.jetbrains.intellij.tools:ide-starter-squashed:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.tools:ide-starter-junit5:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.tools:ide-metrics-collector:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.tools:ide-metrics-collector-starter:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.tools:ide-performance-testing-commands:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.tools:ide-starter-driver:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.driver:driver-client:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.driver:driver-sdk:LATEST-EAP-SNAPSHOT")
    testImplementation("com.jetbrains.intellij.driver:driver-model:LATEST-EAP-SNAPSHOT")

    // Dependency Injection
    testImplementation("org.kodein.di:kodein-di-jvm:7.20.2")

    // Testing frameworks
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.4") // For JUnit 4 compatibility
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")

    // Utilities
    testImplementation("commons-io:commons-io:2.15.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    // Analytics
    testImplementation("com.jetbrains.fus.reporting:ap-validation:76")
    testImplementation("com.jetbrains.fus.reporting:model:76")

    // to fix: Provider com.intellij.tests.JUnit5TestSessionListener could not be instantiated - for uiTest
    testImplementation("com.jetbrains.intellij.junit:junit-rt:243.22562.220")
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

//    pluginVerification {
//        ides {
//            recommended()
//            select {
//                types = listOf(
//                    IntelliJPlatformType.IntellijIdeaCommunity,
//                    IntelliJPlatformType.IntellijIdeaUltimate,
//                    IntelliJPlatformType.AndroidStudio
//                )
//                sinceBuild = "233"
////                untilBuild = "243.*"
//            }
//        }
//    }
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
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

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
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }

        systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)

        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/unittest/**")
        exclude("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")
    }

    register<Test>("unitTest") {
        description = "Runs Unit tests"
        group = "verification"

        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }

        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/unittest/**")
        exclude("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")

        systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)
    }

    register<Test>("uiTest") {
        description = "Runs UI tests"
        group = "verification"

        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }

        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")
        exclude("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/unittest/**")

        systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)
    }
}