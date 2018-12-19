plugins {
    kotlin("jvm") version "1.3.11"
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
        useJUnitPlatform()
    }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")

    compile("io.ktor:ktor-server-netty:${Version.KTOR}")
    compile("io.ktor:ktor-gson:${Version.KTOR}")
    compile("io.ktor:ktor-client-core:${Version.KTOR}")
    compile("io.ktor:ktor-client-gson:${Version.KTOR}")
    compile("io.ktor:ktor-client-apache:${Version.KTOR}")

    compile("org.koin:koin-ktor:${Version.KOIN}")
    compile("org.koin:koin-logger-slf4j:${Version.KOIN}")

    compile("org.slf4j:slf4j-simple:${Version.SLF4J_SIMPLE}")

    testCompile("org.junit.jupiter:junit-jupiter-api:${Version.JUNIT}")
    testCompile("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")
    testCompile("org.amshove.kluent:kluent:${Version.KLUENT}")

    testCompile("io.ktor:ktor-server-test-host:${Version.KTOR}")
    testCompile("io.ktor:ktor-client-mock:${Version.KTOR}")

    testCompile("org.koin:koin-test:${Version.KOIN}")
}
