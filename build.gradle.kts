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

configurations {
    implementation {
        exclude(group = "io.ktor")
        exclude(group = "com.jetbrains.infra")
        exclude(group = "com.jetbrains.intellij.remoteDev")
    }
}

sourceSets {

//    named("test") {
//        kotlin.srcDirs("src/uiTest/kotlin")
//        resources {
//            srcDirs("src/uiTest/resources")
//            include("**/*.*")
//        }
//    }

//    create("uiTest") {
//        kotlin.srcDir("src/uiTest/kotlin")
//        resources.srcDir("src/uiTest/resources")
//        compileClasspath += sourceSets["main"].output + configurations["uiTestCompileClasspath"]
//        runtimeClasspath += output + configurations["uiTestRuntimeClasspath"]
//    }
}

//    named("test") {
//        kotlin.srcDirs("src/uiTest/kotlin")
//        resources {
//            srcDirs("src/uiTest/resources")
//            include("**/*.*")
//        }
//    }

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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2") // For JUnit 4 compatibility
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")

    // Utilities
    testImplementation("commons-io:commons-io:2.15.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    // Analytics
    testImplementation("com.jetbrains.fus.reporting:ap-validation:76")
    testImplementation("com.jetbrains.fus.reporting:model:76")

//    testImplementation("com.intellij.remoterobot:remote-robot:0.11.23")
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

        exclude("ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uitest")
    }

//    register<Test>("uiTest") {
//        useJUnitPlatform()
//
//        testLogging {
//            events("passed", "skipped", "failed", "standardOut", "standardError")
//        }
//
//        // Include only the uiTest package
//        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")
//
//        // Set classpath for uiTest source set
//        testClassesDirs = sourceSets["test"].output.classesDirs
//        classpath = sourceSets["test"].runtimeClasspath
//    }

//    register<Test>("uiTest") {
//        useJUnitPlatform()
//        testLogging {
//            events("passed", "skipped", "failed", "standardOut", "standardError")
//        }
//
//        testClassesDirs = sourceSets["uiTest"].output.classesDirs
//        classpath = sourceSets["uiTest"].runtimeClasspath
//    }

    register<Test>("unitTest") {
        description = "Runs Unit tests"
        group = "verification"

        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/unittest/**")
        exclude("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")

//        testClassesDirs = sourceSets["uiTest"].output.classesDirs
//        classpath = sourceSets["uiTest"].runtimeClasspath

        useJUnitPlatform()
    }

    register<Test>("uiTest") {
        description = "Runs UI tests"
        group = "verification"

        include("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/uitest/**")
        exclude("ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/unittest/**")

//        testClassesDirs = sourceSets["uiTest"].output.classesDirs
//        classpath = sourceSets["uiTest"].runtimeClasspath
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}
