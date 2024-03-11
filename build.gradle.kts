plugins {
//    id("application")
    application
    kotlin("jvm") version "1.9.22"
}

application {
    mainClass = "com.fermion.android.cli.MainKt"
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

tasks.named("installDist") {
    // Rename the generated script
    doLast {
        val scriptFileName = "template-cli" // Change to your desired script name
        val scriptFile = project.file("build/install/${project.name}/bin/${project.name}")
        val scriptFileBat = project.file("build/install/${project.name}/bin/${project.name}.bat")
        if (scriptFile.exists()) {
            val newScriptFile = scriptFile.parentFile.resolve("$scriptFileName${scriptFile.extension}")
            scriptFile.renameTo(newScriptFile)
        }
        if (scriptFileBat.exists()) {
            val newScriptFileBat = scriptFileBat.parentFile.resolve("$scriptFileName.${scriptFileBat.extension}")
            scriptFileBat.renameTo(newScriptFileBat)
        }

    }
}

task("createZip", Zip::class) {
//    include("*/*") //to include contents of a folder present inside Reports directory
    archiveFileName.set("${project.name}.zip")
    destinationDirectory.set(File("${layout.projectDirectory.asFile.path}/template-cli/tools/"))
    from("${layout.buildDirectory.asFile.get().path}/install/${project.name}/")
    finalizedBy("shellExec")
}.mustRunAfter("installDist")


tasks.register<Exec>("shellExec") {
    workingDir = file("${layout.projectDirectory.asFile.path}/template-cli/tools/")
    commandLine(
        "powershell",
        "Get-FileHash -Path ${layout.projectDirectory.asFile.path}\\template-cli\\tools\\ProjectTemplateCLI.zip"
    )
    doLast {
        println("Executed!")
    }
}