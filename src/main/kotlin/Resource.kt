package com.fermion.android.cli

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.table


import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*

object Res {

    // Provide coloring and text options

    // inline styling of string

    // Dimension values
    object DimenRes {
        const val pad_2 = 2
    }


    // String values
    object StringRes {
        const val app_name = "Fermion Project Template: CLI"
        const val options_title = "Options:"
        const val select_title = "Select:"
        const val try_again_title = "Again?"

        object Spanners {
            val options = listOf(Options.TemplateWithDagger, Options.TemplateWithHilt, Options.Exit)
        }
    }


    // styled Strings values
    object StyledRes {
        val optionTitle by lazy { bold(white(StringRes.options_title)) }

        val options by lazy {
            StringRes.Spanners.options.mapIndexed { index, option ->
                gray(" ${index + 1}) ${option.label}")
            }
        }

        val selectTitle by lazy { bold(white(StringRes.select_title)) }

        val selectOption by lazy {
            StringRes.Spanners.options.mapIndexed { index, option ->
                brightBlue(" ${index + 1}) ${option.label}")
            }
        }

        val enterAppName by lazy { "Enter your ${brightGreen("app name")}:" }
        val enterAppClassName by lazy { "Enter your ${brightGreen("module name")}:" }
        val enterPackageName by lazy { "Enter your ${brightGreen("package name")}:" }

        val enterUserMorseTitle by lazy { "Enter your ${brightGreen("Morse Code")}:" }

        val tryAgainTitle by lazy { bold(white(StringRes.try_again_title)) }

    }

    // Merged Styles components
    object View {
        val morseOptionsTable by lazy {
            table {
                cellStyle {
                    border = true
                    paddingLeft = DimenRes.pad_2
                    paddingRight = DimenRes.pad_2
                }
                row(bold(yellow(StringRes.app_name)))
                row(
                    """
${StyledRes.optionTitle}
${StyledRes.options.joinToString(separator = "\n")}
                    """.trimIndent()
                )
            }
        }

        fun displayResultTable(result: String): Table {
            return table {
                cellStyle {
                    border = true
                    paddingLeft = DimenRes.pad_2
                    paddingRight = DimenRes.pad_2
                }
                row(result)
            }
        }
    }

}

