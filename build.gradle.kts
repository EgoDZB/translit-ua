plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.12.0"
}

group = "dev.idzb"
version = "1.0.4"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(kotlin("test"))

    intellijPlatform {
        intellijIdea("2026.1")
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "231"
            untilBuild = provider { null }
        }

        changeNotes = """
            Added support for the new version of IDE
        """.trimIndent()
    }

    pluginVerification {
        ides {
            create("IC", "2023.1")
            create("IC", "2024.1")
            create("IC", "2025.1")
            create("IU", "2026.1")
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
