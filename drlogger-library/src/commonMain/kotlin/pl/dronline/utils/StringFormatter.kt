/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils

import kotlin.math.pow


/**
 * Multiplatform String formatting utility
 * Supports basic format specifiers similar to Java's String.format
 */
object StringFormatter {

    /**
     * Format string with provided arguments
     * Supports: %s (string), %d (integer), %f (float), %b (boolean), %% (literal %)
     * Also supports positional arguments: %1$s, %2$d, etc.
     */
    fun format(format: String, vararg args: Any?): String {
        val result = StringBuilder()
        var i = 0

        while (i < format.length) {
            if (format[i] == '%' && i + 1 < format.length) {
                when {
                    format[i + 1] == '%' -> {
                        result.append('%')
                        i += 2
                    }

                    else -> {
                        val spec = parseFormatSpecifier(format, i)
                        if (spec != null) {
                            val argIndex = spec.position ?: getNextArgIndex(format, i)
                            if (argIndex < args.size) {
                                result.append(formatValue(args[argIndex], spec))
                            } else {
                                throw IllegalArgumentException(
                                    "Missing argument for format specifier at position $i"
                                )
                            }
                            i += spec.length
                        } else {
                            result.append(format[i])
                            i++
                        }
                    }
                }
            } else {
                result.append(format[i])
                i++
            }
        }

        return result.toString()
    }

    private data class FormatSpecifier(
        val type: Char,
        val width: Int? = null,
        val precision: Int? = null,
        val position: Int? = null,
        val leftAlign: Boolean = false,
        val showSign: Boolean = false,
        val padWithZero: Boolean = false,
        val length: Int
    )

    private fun parseFormatSpecifier(format: String, start: Int): FormatSpecifier? {
        if (start >= format.length - 1 || format[start] != '%') return null

        var i = start + 1
        var position: Int? = null
        var width: Int? = null
        var precision: Int? = null
        var leftAlign = false
        var showSign = false
        var padWithZero = false

        // Check for positional argument %n$
        val positionMatch = Regex("^(\\d+)\\$").find(format.substring(i))
        if (positionMatch != null) {
            position = positionMatch.groupValues[1].toInt() - 1 // Convert to 0-based
            i += positionMatch.value.length
        }

        // Parse flags
        while (i < format.length) {
            when (format[i]) {
                '-' -> {
                    leftAlign = true
                    i++
                }

                '+' -> {
                    showSign = true
                    i++
                }

                '0' -> {
                    padWithZero = true
                    i++
                }

                else -> break
            }
        }

        // Parse width
        val widthMatch = Regex("^(\\d+)").find(format.substring(i))
        if (widthMatch != null) {
            width = widthMatch.groupValues[1].toInt()
            i += widthMatch.value.length
        }

        // Parse precision
        if (i < format.length && format[i] == '.') {
            i++
            val precisionMatch = Regex("^(\\d+)").find(format.substring(i))
            if (precisionMatch != null) {
                precision = precisionMatch.groupValues[1].toInt()
                i += precisionMatch.value.length
            }
        }

        // Parse type
        if (i < format.length) {
            val type = format[i]
            if (type in "sdifbxXoeEgG") {
                return FormatSpecifier(
                    type = type,
                    width = width,
                    precision = precision,
                    position = position,
                    leftAlign = leftAlign,
                    showSign = showSign,
                    padWithZero = padWithZero,
                    length = i - start + 1
                )
            }
        }

        return null
    }

    private fun getNextArgIndex(format: String, currentPos: Int): Int {
        var argIndex = 0
        var i = 0

        while (i < currentPos && i < format.length) {
            if (format[i] == '%' && i + 1 < format.length) {
                when {
                    format[i + 1] == '%' -> i += 2
                    else -> {
                        val spec = parseFormatSpecifier(format, i)
                        if (spec != null) {
                            if (spec.position == null) {
                                argIndex++
                            }
                            i += spec.length
                        } else {
                            i++
                        }
                    }
                }
            } else {
                i++
            }
        }

        return argIndex
    }

    private fun formatValue(value: Any?, spec: FormatSpecifier): String {
        val formatted = when (spec.type) {
            's' -> value?.toString() ?: "null"
            'd', 'i' -> formatInteger(value)
            'f' -> formatFloat(value, spec.precision ?: 6)
            'e', 'E' -> formatScientific(value, spec.precision ?: 6, spec.type == 'E')
            'g', 'G' -> formatGeneral(value, spec.precision ?: 6, spec.type == 'G')
            'b' -> value?.toString()?.toBoolean()?.toString() ?: "false"
            'x' -> formatHex(value, false)
            'X' -> formatHex(value, true)
            'o' -> formatOctal(value)
            else -> value?.toString() ?: ""
        }

        return applyWidthAndAlignment(formatted, spec)
    }

    private fun formatInteger(value: Any?): String {
        return when (value) {
            is Number -> value.toLong().toString()
            is Char -> value.code.toString()
            else -> value?.toString() ?: "0"
        }
    }

    private fun formatFloat(value: Any?, precision: Int): String {
        val number = when (value) {
            is Number -> value.toDouble()
            else -> value?.toString()?.toDoubleOrNull() ?: 0.0
        }
        return formatDouble(number, precision)
    }

    private fun formatDouble(value: Double, precision: Int): String {
        val multiplier = 10.0.pow(precision.toDouble())
        val rounded = kotlin.math.round(value * multiplier) / multiplier

        val intPart = rounded.toLong()
        val fracPart = kotlin.math.abs(rounded - intPart)

        return if (precision == 0) {
            intPart.toString()
        } else {
            // Use round() to avoid floating point precision issues
            val fracStr = kotlin.math.round(fracPart * multiplier).toLong().toString().padStart(precision, '0')
            "$intPart.$fracStr"
        }
    }

    private fun formatScientific(value: Any?, precision: Int, uppercase: Boolean): String {
        val number = when (value) {
            is Number -> value.toDouble()
            else -> value?.toString()?.toDoubleOrNull() ?: 0.0
        }

        if (number == 0.0) {
            val zeros = "0" + if (precision > 0) "." + "0".repeat(precision) else ""
            val e = if (uppercase) "E+00" else "e+00"
            return zeros + e
        }

        val sign = if (number < 0) "-" else ""
        val absNumber = kotlin.math.abs(number)
        val exponent = kotlin.math.floor(kotlin.math.log10(absNumber)).toInt()
        val mantissa = absNumber / 10.0.pow(exponent.toDouble())

        val formattedMantissa = formatDouble(mantissa, precision)
        val e = if (uppercase) "E" else "e"
        val expSign = if (exponent >= 0) "+" else "-"
        val expStr = kotlin.math.abs(exponent).toString().padStart(2, '0')

        return "$sign$formattedMantissa$e$expSign$expStr"
    }

    private fun formatGeneral(value: Any?, precision: Int, uppercase: Boolean): String {
        val number = when (value) {
            is Number -> value.toDouble()
            else -> value?.toString()?.toDoubleOrNull() ?: 0.0
        }

        val absNumber = kotlin.math.abs(number)
        val exponent = if (absNumber == 0.0) 0 else kotlin.math.floor(kotlin.math.log10(absNumber)).toInt()

        val formatted = if (exponent >= -4 && exponent < precision) {
            formatDouble(number, precision - 1 - exponent)
        } else {
            formatScientific(number, precision - 1, uppercase)
        }

        // Remove trailing zeros and unnecessary decimal point for %g format
        return removeTrailingZeros(formatted)
    }

    private fun removeTrailingZeros(value: String): String {
        if (!value.contains('.')) {
            return value
        }

        // Handle scientific notation separately
        val eIndex = value.indexOfFirst { it == 'e' || it == 'E' }
        if (eIndex >= 0) {
            val mantissa = value.substring(0, eIndex)
            val exponent = value.substring(eIndex)
            val trimmedMantissa = mantissa.trimEnd('0').let {
                if (it.endsWith('.')) it.dropLast(1) else it
            }
            return trimmedMantissa + exponent
        }

        // Remove trailing zeros from fixed-point notation
        var result = value.trimEnd('0')
        // Remove decimal point if no fractional part remains
        if (result.endsWith('.')) {
            result = result.dropLast(1)
        }
        return result
    }

    private fun formatHex(value: Any?, uppercase: Boolean): String {
        val number = when (value) {
            is Byte -> value.toUByte().toLong()  // Convert signed byte to unsigned
            is Short -> value.toUShort().toLong()  // Convert signed short to unsigned
            is Int -> value.toLong() and 0xFFFFFFFFL  // Mask to unsigned 32-bit
            is Number -> value.toLong()
            is Char -> value.code.toLong()
            else -> value?.toString()?.toLongOrNull() ?: 0L
        }

        val hex = number.toString(16)
        return if (uppercase) hex.uppercase() else hex
    }

    private fun formatOctal(value: Any?): String {
        val number = when (value) {
            is Byte -> value.toUByte().toLong()  // Convert signed byte to unsigned
            is Short -> value.toUShort().toLong()  // Convert signed short to unsigned
            is Int -> value.toLong() and 0xFFFFFFFFL  // Mask to unsigned 32-bit
            is Number -> value.toLong()
            is Char -> value.code.toLong()
            else -> value?.toString()?.toLongOrNull() ?: 0L
        }

        return number.toString(8)
    }

    private fun applyWidthAndAlignment(value: String, spec: FormatSpecifier): String {
        val width = spec.width ?: return value

        if (value.length >= width) return value

        val padding = width - value.length
        val padChar = if (spec.padWithZero && !spec.leftAlign) '0' else ' '
        val padString = padChar.toString().repeat(padding)

        return if (spec.leftAlign) {
            value + padString
        } else {
            // Handle sign for zero-padding
            if (spec.padWithZero && (value.startsWith('-') || value.startsWith('+'))) {
                value[0] + padString + value.substring(1)
            } else {
                padString + value
            }
        }
    }
}

fun String.format(vararg args: Any?): String = StringFormatter.format(this, *args)