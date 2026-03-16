plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.github.rigarenu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Source: https://mvnrepository.com/artifact/net.java.dev.jna/jna
    implementation("net.java.dev.jna:jna:5.18.1")

    // Source: https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform
    implementation("net.java.dev.jna:jna-platform:5.18.1")

    // Source: https://mvnrepository.com/artifact/net.sourceforge.tess4j/tess4j
    implementation("net.sourceforge.tess4j:tess4j:5.18.0")

    // Source: https://mvnrepository.com/artifact/org.openpnp/opencv
    implementation("org.openpnp:opencv:4.9.0-0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}