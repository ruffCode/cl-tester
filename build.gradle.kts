import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.21"
    kotlin("kapt") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("org.jlleitschuh.gradle.ktlint-idea").version("10.1.0")
    id("org.jetbrains.dokka") version "1.4.30"
    id("com.diffplug.spotless") version "5.14.2"
    id("io.gitlab.arturbosch.detekt") version "1.18.0-RC3"
}

java.sourceCompatibility = JavaVersion.VERSION_16

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}
val kotestVersion = "4.6.1"
extra["testcontainersVersion"] = "1.16.0"
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.21"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.1"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("io.github.microutils:kotlin-logging:2.0.10")
    implementation("io.r2dbc:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.0.0")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.0.0")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("app.cash.turbine:turbine:0.6.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.0-RC3")
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = "16"
    }
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xuse-experimental=kotlin.Experimental",
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.time.ExperimentalTime"
            )
            jvmTarget = "16"
        }
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
            )
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

ktlint {
    debug.set(true)
    version.set("0.42.1")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(true)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
    outputToConsole.set(true)
    outputColorName.set("BLUE")
    ignoreFailures.set(true)

    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    additionalEditorconfigFile.set(file("${project.projectDir}/.editorConfig"))
}

detekt {
    config = rootProject.files("config/detekt/detekt.yml")
    reports {
        html {
            enabled = true
            destination = file("build/reports/detekt.html")
        }
        autoCorrect = true
        parallel = true
    }
}
spotless {
    isEnforceCheck = false
    kotlin {
        target("src/**/*.kt")
        targetExclude("$buildDir/**/*.kt")
        targetExclude("**/generated/**")
        targetExclude("spotless/copyright.kt")
        licenseHeaderFile {
            rootProject.file("spotless/copyright.kt")
        }
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.42.1")
    }
}
