import com.google.protobuf.gradle.*
import groovy.lang.Closure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.TestLibrary
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
    idea
    kotlin("jvm") version Version.KOTLIN
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.google.protobuf") version "0.8.7"
    id("org.unbroken-dome.test-sets") version "2.0.3"
}

group = "com.github.jcornaz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://jitpack.io") }
}

testSets {
    val integration = create("integrationTest")
    create("acceptanceTest") { extendsFrom(integration) }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.KOTLIN}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Version.KOTLIN}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")

    implementation("io.ktor:ktor-server-netty:${Version.KTOR}")
    implementation("io.ktor:ktor-client-core:${Version.KTOR}")
    implementation("io.ktor:ktor-client-gson:${Version.KTOR}")
    implementation("io.ktor:ktor-client-apache:${Version.KTOR}")

    implementation("com.google.protobuf:protobuf-java:${Version.PROTOBUF}")

    implementation("org.neo4j.driver:neo4j-java-driver:${Version.NEO4J_DRIVER}")

    implementation("org.slf4j:slf4j-simple:${Version.SLF4J_SIMPLE}")

    testApi("org.jetbrains.kotlin:kotlin-test:${Version.KOTLIN}")
    testApi("org.spekframework.spek2:spek-dsl-jvm:${Version.SPEK}")
    testApi("org.amshove.kluent:kluent:${Version.KLUENT}")
    testApi("io.mockk:mockk:${Version.MOCKK}")
    testApi("com.github.jcornaz.miop:miop-jvm:${Version.MIOP}")
    testApi("io.ktor:ktor-client-mock-jvm:${Version.KTOR}")

    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Version.SPEK}")

    add("integrationTestImplementation", sourceSets["test"].output)
    add("integrationTestImplementation", "org.neo4j:neo4j:${Version.NEO4J}")
    add("integrationTestImplementation", "org.neo4j.test:neo4j-harness:${Version.NEO4J}")

    add("acceptanceTestImplementation", sourceSets["test"].output)
    add("acceptanceTestImplementation", sourceSets["integrationTest"].output)
    add("acceptanceTestImplementation", "io.ktor:ktor-server-test-host:${Version.KTOR}")
}

tasks {
    clean {
        delete("tmp")
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${Version.PROTOBUF}"
        }
    }

    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"

            freeCompilerArgs += listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi"
            )
        }
    }

    withType(Test::class).all {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    "integrationTest" {
        mustRunAfter("test")
    }

    "acceptanceTest" {
        mustRunAfter("integrationTest")
    }

    check {
        dependsOn += get("integrationTest")
        dependsOn += get("acceptanceTest")
    }
}
