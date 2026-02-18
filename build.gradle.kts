import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    kotlin("jvm") version "2.0.20-Beta1"
    kotlin("kapt") version "2.0.20-Beta1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("xyz.jpenilla.run-velocity") version "2.3.1"
}

group = "org.pixiemays"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation(kotlin("stdlib"))
}

tasks {
  runVelocity {
    // Configure the Velocity version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    velocityVersion("3.4.0-SNAPSHOT")
  }
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

project.idea.project.settings.taskTriggers.afterSync(generateTemplates)
project.eclipse.synchronizationTasks(generateTemplates)

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("TelegramWhitelist-${project.version}.jar")
        relocate("org.telegram",         "dev.swlplugin.libs.telegram")
        relocate("org.yaml.snakeyaml",   "dev.swlplugin.libs.snakeyaml")
        relocate("com.fasterxml",        "dev.swlplugin.libs.jackson")
        relocate("okhttp3",              "dev.swlplugin.libs.okhttp3")
        relocate("okio",                 "dev.swlplugin.libs.okio")
    }
    build { dependsOn(shadowJar) }
}