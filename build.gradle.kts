import groovy.lang.Closure

plugins {
    kotlin("jvm") version Version.KOTLIN
    id("com.github.ben-manes.versions") version "0.20.0"
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

    compile("org.koin:koin-ktor:${Version.KOIN}")
    compile("org.koin:koin-logger-slf4j:${Version.KOIN}")
    testCompile("org.koin:koin-test:${Version.KOIN}")

    compile("org.slf4j:slf4j-simple:${Version.SLF4J_SIMPLE}")

    testCompile("org.spekframework.spek2:spek-dsl-jvm:${Version.SPEK}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Version.SPEK}")

    testCompile("org.amshove.kluent:kluent:${Version.KLUENT}")
}
