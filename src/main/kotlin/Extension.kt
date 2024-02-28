package com.fermion.android.cli

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal
import onFailure
import onSuccess
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


/***
 * Convert String input from user to Options that can be
 * performed in script.
 */
fun String.toOption(): Options {
    return when {
        Options.TemplateWithDagger.label in this -> Options.TemplateWithDagger
        Options.TemplateWithHilt.label in this -> Options.TemplateWithHilt
        Options.Exit.label in this -> Options.Exit
        else -> Options.ERROR
    }
}


/***
 * Option menu actions
 */
enum class Options(val label: String, val value: Int) {
    ERROR(label = "Error", value = 0), TemplateWithDagger(
        label = "Create Android Native Template With Dagger", value = 1
    ),
    TemplateWithHilt(label = "Create Android Native Template With Hilt", value = 2), Exit(label = "Exit", value = 3)
}


fun String.runCommand(workingDir: File, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}): String? {
    try {
        val terminal = Terminal()
        val parts = this.split("\\s".toRegex())
        val proc =
            ProcessBuilder(*parts.toTypedArray()).directory(workingDir).redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE).redirectErrorStream(true).start()
        proc.waitFor(60, TimeUnit.MINUTES)
        proc.onFailure {
            val response = proc.errorStream.bufferedReader().readText()
            terminal.println(response)
            onFailure.invoke()
        }

        proc.onSuccess {
            onSuccess.invoke()
        }

        val response = proc.inputStream.bufferedReader().readText()
        terminal.println(response)

        return response
    } catch (e: IOException) {
        e.printStackTrace()
        onFailure.invoke()
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        onFailure.invoke()
        return null
    }
}

fun String.showErrorCliMessage(): String {
    return brightRed(this)

}

fun String.showSuccessCliMessage(): String {

    return brightGreen(this)

}

fun String.showInfoCliMessage(): String {

    return brightBlue(this)

}