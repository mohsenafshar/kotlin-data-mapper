import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform")
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

dependencies {
    testImplementation(kotlin("test"))

    intellijPlatform {
        local("H:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.2.4")
        instrumentationTools()
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

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }

//    systemProperty("path.to.build.plugin", tasks.buildPlugin.get().archiveFile.get().asFile.absolutePath)
    systemProperty("path.to.build.plugin", "H:\\Project\\java\\data-mapper\\DataMapper\\app\\build\\distributions\\app-0.3.0.zip")
}

kotlin {
    jvmToolchain(17)
}