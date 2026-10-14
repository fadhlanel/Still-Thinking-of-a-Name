plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}

group = "com.stillthinkingofaname"
version = "1.1.0"
description = "MasihMikirNamaPlugin"
java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    processResources {
        val pluginVersion = version
        filesMatching("plugin.yml") {
            expand("pluginVersion" to pluginVersion)
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveFileName.set("MasihMikirNamaPlugin.jar")
}
