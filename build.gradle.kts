plugins {
//    id("application")
    application
    kotlin("jvm") version "1.9.22"
}

application {
    mainClass="com.fermion.android.cli.MainKt"
}


group = "com.fermion.android.cli"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("com.github.ajalt.mordant:mordant:2.2.0")
    implementation("com.jakewharton.picnic:picnic:0.5.0")
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:0.1.0")
    implementation("com.sealwu:kscript-tools:1.0.2")
    implementation("com.squareup:kotlinpoet:1.16.0")

    implementation("junit:junit:4.13.2")


}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}