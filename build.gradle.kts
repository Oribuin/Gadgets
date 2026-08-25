import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
}

group = "dev.oribuin"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    disableAutoTargetJvm()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://nexus.neetgames.com/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://repo.nexomc.com/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

dependencies {
    // Commands, Configs & Database
    api("org.incendo:cloud-core:2.0.0")
    api("org.incendo:cloud-annotations:2.0.0")
    api("org.incendo:cloud-paper:2.0.0")
    api("org.spongepowered:configurate-yaml:4.2.0")
    api("com.jeff-media:custom-block-data:2.2.5")
    api("com.jeff-media:MorePersistentDataTypes:2.4.0")
    api("com.zaxxer:HikariCP:4.0.3")
    api("dev.triumphteam:triumph-gui:3.1.13") {  // https://triumphteam.dev/docs/triumph-gui/
        exclude(group = "net.kyori", module = "*") // Remove kyori
    }

    // Plugin Dependencies
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.18")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.3.001-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.palmergames.bukkit.towny:towny:0.99.5.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.coreprotect:coreprotect:21.3")
    compileOnly("com.nexomc:nexo:1.8.0")
}

tasks {
    val commitHash = let {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
        val output = ByteArrayOutputStream()
        process.inputStream.copyTo(output)

        output.toString().trim()
    }
    
    if (commitHash.isNotEmpty()) project.version = commitHash
    
    compileJava {
        this.options.compilerArgs.add("-parameters")
        this.options.isFork = true
        this.options.encoding = "UTF-8"
    }

    shadowJar {
        // add commit hash to the jar name
        this.archiveClassifier.set("")
//        this.archiveVersion.set("$version-[$commitHash]")

        this.relocate("com.jeff_media.morepersistentdatatypes", "${project.group}.fishing.libs.pdt")
        this.relocate("net.objecthunter.exp4j", "${project.group}.fishing.libs.exp4j")
        this.relocate("dev.triumphteam.gui", "${project.group}.fishing.libs.triumphgui")
        this.relocate("io.leangen.geantyref", "${project.group}.fishing.libs.geantyref")
        this.relocate("org.incendo", "${project.group}.fishing.libs.incendo")
        this.relocate("org.spongepowered", "${project.group}.fishing.libs.spongepowered")
        this.relocate("com.zaxxer", "${project.group}.fishing.libs.hikari")
        this.relocate("org.slf4j", "${project.group}.fishing.libs.slf4j")
        this.minimize()
    }

    bukkit {
        this.main = "dev.oribuin.gadgets.GadgetsPlugin"
        this.name = "Gadgets"
        this.version = "${project.version}"
        this.author = "Oribuin"
        this.description = "hello"
        this.apiVersion = "1.21"
        this.foliaSupported = false
        this.softDepend = listOf("Vault", "HeadDatabase", "PlaceholderAPI", "PlayerPoints")
    }
    build {
//        this.dependsOn(javadoc)
        this.dependsOn(shadowJar)
    }
}