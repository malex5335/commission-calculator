plugins {
    kotlin("jvm") version "1.8.20"
}

group = "de.riagade"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}