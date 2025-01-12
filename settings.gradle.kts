//pluginManagement {
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
//
//rootProject.name = "DataMapper"
//
//
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.1.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()

        maven { url = uri("https://cache-redirector.jetbrains.com/maven-central") }
        maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
        maven { url = uri("https://cache-redirector.jetbrains.com/download.jetbrains.com/teamcity-repository") }
        maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
        maven { url = uri("https://www.jetbrains.com/intellij-repository/snapshots") }
        maven { url = uri("https://download.jetbrains.com/teamcity-repository") }
        maven { url = uri("https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/grazi/grazie-platform-public") }

        intellijPlatform {
            defaultRepositories()
            jetbrainsRuntime()
            intellijDependencies()
        }
    }
}

rootProject.name = "KotlinDataMapper"
include("uiTest")
