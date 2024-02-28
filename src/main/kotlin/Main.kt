package com.fermion.android.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.ListViewOptions
import com.github.kinquirer.components.promptConfirm
import com.github.kinquirer.components.promptList
import java.io.File
import java.nio.file.Paths

val terminal = Terminal()

fun main(args: Array<String>) {
    PromptTemplateCLIOption().main(args)

    App(args).main(args)

}

class PromptTemplateCLIOption : NoOpCliktCommand() {
    override fun run() {
        super.run()
        echo(Res.View.morseOptionsTable)
    }
}

private const val repeatTimer = 2

class App(private val args: Array<String>) : CliktCommand() {

    override fun run() {

        val a = terminal.textAnimation<Int>() { frame ->
            (1..frame).joinToString("") {
                val hue = (frame + it) * 3 % 360
                TextColors.hsv(hue, 1, 1)(".")
            }

        }


        var isSystemRequirementMet = true

        terminal.println("Checking System Requirement".showInfoCliMessage())

        try {
            "git --version ".runCommand(File(Paths.get("").toAbsolutePath().toString()), onFailure = {
                isSystemRequirementMet = false
                echo("Git might not exists on your system or check if path is added to environment".showErrorCliMessage())
            }, onSuccess = {
                var i = 1
                repeat(repeatTimer) {
                    a.update(i)
                    i += 1
                    if (i == repeatTimer) {
                        i = 1
                    }
                    Thread.sleep(100)
                }
                a.clear()
                terminal.println("Git found in your system ".showSuccessCliMessage())
            })

            "java --version ".runCommand(File(Paths.get("").toAbsolutePath().toString()), onFailure = {
                isSystemRequirementMet = false
                echo("Java might not exists on your system or check if path is added to environment".showErrorCliMessage())
            }, onSuccess = {
                var i = 1
                repeat(repeatTimer) {
                    a.update(i)
                    i += 1
                    if (i == repeatTimer) {
                        i = 1
                    }
                    Thread.sleep(100)
                }
                a.clear()
                terminal.println("Java found in your system ".showSuccessCliMessage())
            })

            "adb --version ".runCommand(File(Paths.get("").toAbsolutePath().toString()), onFailure = {
                isSystemRequirementMet = false
                echo("ADB might not exists on your system or check if path is added to environment".showErrorCliMessage())
            }, onSuccess = {
                var i = 1
                repeat(repeatTimer) {
                    a.update(i)
                    i += 1
                    if (i == repeatTimer) {
                        i = 1
                    }
                    Thread.sleep(100)
                }
                a.clear()
                terminal.println("ADB found in your system ".showSuccessCliMessage())
            })


        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isSystemRequirementMet) {
            showProjectTemplateOptions()
        } else {
            terminal.println("Some system requirements didn't met.".showErrorCliMessage())
        }

    }

    private fun showProjectTemplateOptions() {
        val choice: Options = KInquirer.promptList(
            Res.StyledRes.selectTitle, Res.StyledRes.selectOption, viewOptions = ListViewOptions()
        ).toOption()

        when (choice) {
            Options.ERROR -> {
                echo("Invalid option")
                val retry: Boolean = KInquirer.promptConfirm(Res.StyledRes.tryAgainTitle, default = false)
                if (retry) {
                    run()
                }
            }

            Options.TemplateWithDagger -> TemplateWithDaggerCLI().main(args)
            Options.TemplateWithHilt -> TemplateWithHiltCLI().main(args)
            Options.Exit -> {
            }
        }
    }

}


