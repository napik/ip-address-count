val kotestVersion = "5.9.1"

plugins {
    kotlin("jvm") version "2.0.0"
    application
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    minHeapSize = "512m"
    maxHeapSize = "1024m"
}

application {
    applicationDefaultJvmArgs = listOf("-XX:+AlwaysPreTouch", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC")
}

kotlin {
    jvmToolchain(17)
}
