import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation("com.mewna:catnip:3.3.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}