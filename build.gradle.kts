import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClass = "no.nav.k9brukerdialogapi.AppKt"
val dusseldorfKtorVersion = "4.2.1"
val ktorVersion = "2.3.9"
val kafkaTestcontainerVersion = "1.19.7"
val kafkaVersion = "3.7.0"
val k9FormatVersion = "9.2.14"
val fuelVersion = "2.3.1"
val tokenSupportVersion = "4.1.4"
val mockOauth2ServerVersion = "2.1.3"
val junitVersion = "5.10.2"
val jakartaElVersion = "4.0.2"

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.sonarqube") version "5.0.0.4638"
    id("jacoco")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Server
    implementation("no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-common:$dusseldorfKtorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }

    // NAV
    implementation("no.nav.security:token-validation-ktor-v2:$tokenSupportVersion"){
        exclude("jakarta.validation", "jakarta.validation-api")
    }
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")

    // Client
    implementation("no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")

    // K9-format
    implementation ("no.nav.k9:k9-format:$k9FormatVersion")
    implementation ("no.nav.k9:soknad:$k9FormatVersion")
    implementation ("no.nav.k9:ettersendelse:$k9FormatVersion")

    // Må være inkludert for å kunne validere søknad.
    implementation ("org.glassfish:jakarta.el:$jakartaElVersion")

    // kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    // Test
    testImplementation("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testImplementation("org.testcontainers:kafka:$kafkaTestcontainerVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
}

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: "k9-brukerdialog-api"
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    mavenCentral()

    maven("https://jitpack.io")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to mainClass
            )
        )
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.5"
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "navikt_k9-brukerdialog-api")
        property("sonar.organization", "navikt")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
        property("sonar.sourceEncoding", "UTF-8")
    }
}
