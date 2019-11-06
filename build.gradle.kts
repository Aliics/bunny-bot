plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
    application
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
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.9.3")
}

application {
    mainClassName = "fish.eyebrow.bunnybot.AppKt"
}
