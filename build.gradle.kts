plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "codes.shiftmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.xenondevs.xyz/releases") {
        name = "xenondevs"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.vertx:vertx-sql-client:4.5.11")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
    compileOnly("io.projectreactor:reactor-core:3.7.2")
    compileOnly("io.vertx:vertx-mysql-client:4.5.12")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("xyz.xenondevs.invui:invui:2.0.0-alpha.7")
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
