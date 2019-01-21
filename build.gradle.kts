import com.google.protobuf.gradle.*
import groovy.lang.Closure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.TestLibrary
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
    idea
    kotlin("jvm") version Version.KOTLIN
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.google.protobuf") version "0.8.8"
    id("org.unbroken-dome.test-sets") version "2.1.1"
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

@Suppress("UNUSED_VARIABLE")
testSets {
    val testUtil by creating(TestLibrary::class)
    val integrationTestUtil by creating(TestLibrary::class)

    val unitTest by getting { imports(testUtil) }
    val integrationTest by creating { imports(testUtil, integrationTestUtil) }
    val acceptanceTest by creating { imports(testUtil, integrationTestUtil) }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.KOTLIN}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Version.KOTLIN}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")

    implementation("io.ktor:ktor-server-netty:${Version.KTOR}")
    implementation("io.ktor:ktor-client-core:${Version.KTOR}")
    implementation("io.ktor:ktor-client-gson:${Version.KTOR}")
    implementation("io.ktor:ktor-client-apache:${Version.KTOR}")

    implementation("org.koin:koin-ktor:${Version.KOIN}")
    implementation("org.koin:koin-logger-slf4j:${Version.KOIN}")

    implementation("com.google.protobuf:protobuf-java:${Version.PROTOBUF}")
    implementation("org.neo4j.driver:neo4j-java-driver:${Version.NEO4J_DRIVER}")
    implementation("org.slf4j:slf4j-simple:${Version.SLF4J_SIMPLE}")

    add("testUtilApi", sourceSets["main"].compileClasspath)
    add("testUtilApi", "org.jetbrains.kotlin:kotlin-test:${Version.KOTLIN}")
    add("testUtilApi", "org.spekframework.spek2:spek-dsl-jvm:${Version.SPEK}")
    add("testUtilApi", "org.amshove.kluent:kluent:${Version.KLUENT}")
    add("testUtilApi", "io.mockk:mockk:${Version.MOCKK}")

    testApi("io.ktor:ktor-client-mock-jvm:${Version.KTOR}")
    testApi("com.github.jcornaz.miop:miop-jvm:${Version.MIOP}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Version.SPEK}")

    add("integrationTestUtilApi", sourceSets["main"].compileClasspath)
    add("integrationTestUtilApi", "org.neo4j:neo4j:${Version.NEO4J}")
    add("integrationTestUtilApi", "org.neo4j.test:neo4j-harness:${Version.NEO4J}")

    add("acceptanceTestImplementation", "io.ktor:ktor-server-test-host:${Version.KTOR}")
    add("acceptanceTestImplementation", "org.koin:koin-test:${Version.KOIN}")
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
