import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform")
}

group = "ir.mohsenafshar.toolkits.jetbrains"
version = "0.2.0-beta2"


dependencies {
    intellijPlatform {
//        intellijIdeaCommunity("2024.2.4")
        local("H:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.2.4")

        jetbrainsRuntime()
        instrumentationTools()

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        pluginVerifier()
        zipSigner()
    }
}


tasks {
    patchPluginXml {
        sinceBuild = "233"
        untilBuild = provider { null }
    }
}

intellijPlatform {
    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "233"
        }
    }

    signing {
////        certificateChainFile.set(file(System.getenv("CERTIFICATE_CHAIN")))

//        certificateChainFile = file(EnvironmentUtil.getValue("CERTIFICATE_CHAIN")!!)
//        privateKeyFile = file(System.getenv("PRIVATE_KEY"))
//        password = System.getenv("PRIVATE_KEY_PASSWORD")

        certificateChainFile = file("chain.crt")
        privateKeyFile = file("private.pem")
        password = "admin123"
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            recommended()
            select {
                types = listOf(
                    IntelliJPlatformType.IntellijIdeaCommunity,
                    IntelliJPlatformType.IntellijIdeaUltimate,
                    IntelliJPlatformType.AndroidStudio
                )
                sinceBuild = "233"
//                untilBuild = "243.*"
            }
        }
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
//intellij {
//    version.set("2023.2.6")
//    type.set("IC") // Target IDE Platform
//
//    plugins.set(listOf("java", "org.jetbrains.kotlin"))
//}

/*intellijPlatform {
    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name

    pluginConfiguration {
        id = "my-plugin-id"
        name = "My Awesome Plugin"
        version = "1.0.0"
        description = "It's an awesome plugin!"
        changeNotes =
            """
      A descriptive release note...
      """.trimIndent()

        productDescriptor {
            code = "MY_CODE"
            releaseDate = "20240217"
            releaseVersion = "20241"
            optional = false
            eap = false
        }
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "242.*"
        }
        vendor {
            name = "JetBrains"
            email = "hello@jetbrains.com"
            url = "https://www.jetbrains.com"
        }
    }

    signing {
        cliPath = file("/path/to/marketplace-zip-signer-cli.jar")
        keyStore = file("/path/to/keyStore.ks")
        keyStorePassword = "..."
        keyStoreKeyAlias = "..."
        keyStoreType = "..."
        keyStoreProviderName = "..."
        privateKey = "..."
        privateKeyFile = file("/path/to/private.pem")
        password = "..."
        certificateChain = "..."
        certificateChainFile = file("/path/to/chain.crt")
    }

    signing {
        certificateChainFile.set(file(System.getenv("CERTIFICATE_CHAIN")))
        privateKeyFile.set(file(System.getenv("PRIVATE_KEY")))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        host = ""
        token = System.getenv("PUBLISH_TOKEN")
        channels = listOf("default")
        ideServices = false
        hidden = false
    }

    pluginVerification {
        cliPath = file("/path/to/plugin-verifier-cli.jar")
        freeArgs = listOf("foo", "bar")
//        homeDirectory = file("/path/to/pluginVerifierHomeDirectory/")
//        downloadDirectory = file("/path/to/pluginVerifierHomeDirectory/ides/")
        failureLevel = VerifyPluginTask.FailureLevel.ALL
//        verificationReportsDirectory = "build/reports/pluginVerifier"
        verificationReportsFormats = VerifyPluginTask.VerificationReportsFormats.ALL
//        externalPrefixes = "com.example"
        teamCityOutputFormat = false
        subsystemsToCheck = VerifyPluginTask.Subsystems.ALL
        ignoredProblemsFile = file("/path/to/ignoredProblems.txt")

        ides {
            ide(IntelliJPlatformType.RustRover, "2023.3")
            local(file("/path/to/ide/"))
            recommended()
            select {
                types = listOf(IntelliJPlatformType.PhpStorm)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "232"
                untilBuild = "241.*"
            }
        }
    }

}*/

