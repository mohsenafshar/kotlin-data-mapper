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

        intellijPlatform {
            defaultRepositories()
            jetbrainsRuntime()
            intellijDependencies()
        }
    }
}

rootProject.name = "KotlinDataMapper"
include("app")
include("uiTest")
