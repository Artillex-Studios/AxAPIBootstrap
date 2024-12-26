import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version ("8.3.5")
}

group = "com.artillexstudios.axapibootstrapper"
version = "1.0.5"

repositories {
    mavenCentral()

    maven("https://repo.artillex-studios.com/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("io.github.revxrsal:zapper.api:1.0.3")
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

publishing {
    repositories {
        maven {
            name = "Artillex-Studios"
            url = uri("https://repo.artillex-studios.com/releases/")
            credentials(PasswordCredentials::class) {
                username = project.properties["maven_username"].toString()
                password = project.properties["maven_password"].toString()
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "axapi-bootstrapper"

            from(components["shadow"])
        }
    }
}

tasks.withType<ShadowJar> {
    relocate("revxrsal.zapper", "com.artillexstudios.axapibootstrapper.libraries.zapper")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}