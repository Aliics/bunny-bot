plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

repositories {
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("org.slf4j:slf4j-simple:1.7.28")
    implementation("com.discord4j:discord4j-core:3.0.10")
    implementation("com.google.inject:guice:4.2.2")
    implementation("org.postgresql:postgresql:42.2.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.h2database:h2:1.4.200")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

val shadowJar = tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fish.eyebrow.bunnybot.App"
    }
}

tasks.build {
    dependsOn(shadowJar)
}
