val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String = "1.0.0-beta-4" // Update to the latest version as needed
plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.2.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.jetbrains.kotlin.kapt") version "2.1.10"
}

group = "com.aatech"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-auth:3.2.2")
    implementation("io.ktor:ktor-server-auth-jwt:3.2.2")
    implementation("io.ktor:ktor-server-status-pages:3.2.2")
    implementation("io.insert-koin:koin-ktor:3.5.6")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.6")
    implementation("io.ktor:ktor-client-logging:3.2.2")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    implementation("io.ktor:ktor-client-core:3.2.2")
    implementation("io.ktor:ktor-client-cio:3.2.2")
    implementation("io.ktor:ktor-client-auth:3.2.2")
    implementation("io.ktor:ktor-client-content-negotiation")




    implementation( "com.google.dagger:dagger:2.57")
    kapt("com.google.dagger:dagger-compiler:2.57")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.0.1") // Connection pooling
    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-migration:${exposedVersion}")

    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.0.0")


    implementation("org.bouncycastle:bcprov-jdk18on:1.81")

    implementation("org.mindrot:jbcrypt:0.4")


}
