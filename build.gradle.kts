import com.google.protobuf.gradle.*
import groovy.lang.Closure

plugins {
    idea
    kotlin("jvm") version Version.KOTLIN
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.google.protobuf") version "0.8.7"
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

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"

            freeCompilerArgs += listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi"
            )
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"

            freeCompilerArgs += listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi"
            )
        }
    }

    test {

        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    clean {
        delete("tmp")
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${Version.PROTOBUF}"
        }
    }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.KOTLIN}")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:${Version.KOTLIN}")

    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")

    compile("io.ktor:ktor-server-netty:${Version.KTOR}")
    compile("io.ktor:ktor-gson:${Version.KTOR}")
    compile("io.ktor:ktor-client-core:${Version.KTOR}")
    compile("io.ktor:ktor-client-gson:${Version.KTOR}")
    compile("io.ktor:ktor-client-apache:${Version.KTOR}")
    testCompile("io.ktor:ktor-server-test-host:${Version.KTOR}")
    testCompile("io.ktor:ktor-client-mock-jvm:${Version.KTOR}")

    compile("com.google.protobuf:protobuf-java:${Version.PROTOBUF}")

    compile("org.neo4j.driver:neo4j-java-driver:${Version.NEO4J_DRIVER}")

    testCompile("org.neo4j:neo4j:${Version.NEO4J}")
    testCompile("org.neo4j:neo4j-kernel:${Version.NEO4J}:tests")
    testCompile("org.neo4j:neo4j-io:${Version.NEO4J}:tests")
    testCompile("org.neo4j.test:neo4j-harness:${Version.NEO4J}")

    compile("org.slf4j:slf4j-simple:${Version.SLF4J_SIMPLE}")

    testCompile("org.spekframework.spek2:spek-dsl-jvm:${Version.SPEK}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Version.SPEK}")

    testCompile("org.amshove.kluent:kluent:${Version.KLUENT}")
}
