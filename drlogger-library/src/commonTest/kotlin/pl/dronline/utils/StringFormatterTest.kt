/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringFormatterTest {

    // Basic String Formatting Tests
    @Test
    fun testBasicStringFormat() {
        assertEquals("Hello World", StringFormatter.format("Hello %s", "World"))
        assertEquals("Hello World!", "Hello %s!".format("World"))
    }

    @Test
    fun testMultipleStrings() {
        assertEquals("Hello World from Kotlin", StringFormatter.format("Hello %s from %s", "World", "Kotlin"))
    }

    @Test
    fun testNullString() {
        assertEquals("Hello null", StringFormatter.format("Hello %s", null))
    }

    // Integer Formatting Tests
    @Test
    fun testBasicIntegerFormat() {
        assertEquals("Number: 42", StringFormatter.format("Number: %d", 42))
        assertEquals("Number: -42", StringFormatter.format("Number: %d", -42))
    }

    @Test
    fun testIntegerFromDouble() {
        assertEquals("Number: 42", StringFormatter.format("Number: %d", 42.7))
        assertEquals("Number: -42", StringFormatter.format("Number: %d", -42.3))
    }

    @Test
    fun testIntegerFromChar() {
        assertEquals("Number: 65", StringFormatter.format("Number: %d", 'A'))
    }

    @Test
    fun testIntegerFormatI() {
        assertEquals("Number: 42", StringFormatter.format("Number: %i", 42))
    }

    @Test
    fun testNullInteger() {
        assertEquals("Number: 0", StringFormatter.format("Number: %d", null))
    }

    // Float Formatting Tests
    @Test
    fun testBasicFloatFormat() {
        // Note: Default precision for %f is 6 decimal places
        assertEquals("Pi: 3.141593", StringFormatter.format("Pi: %f", 3.14159265))
    }

    @Test
    fun testFloatWithPrecision() {
        assertEquals("Value: 3.14", StringFormatter.format("Value: %.2f", 3.14159))
        assertEquals("Value: 3.1", StringFormatter.format("Value: %.1f", 3.14159))
        assertEquals("Value: 3", StringFormatter.format("Value: %.0f", 3.14159))
    }

    @Test
    fun testFloatWithHighPrecision() {
        // High precision formatting
        assertEquals("Value: 3.141590000", StringFormatter.format("Value: %.9f", 3.14159))
    }

    @Test
    fun testNegativeFloat() {
        assertEquals("Value: -3.14", StringFormatter.format("Value: %.2f", -3.14159))
    }

    @Test
    fun testFloatFromString() {
        assertEquals("Value: 3.140000", StringFormatter.format("Value: %f", "3.14"))
    }

    @Test
    fun testNullFloat() {
        assertEquals("Value: 0.000000", StringFormatter.format("Value: %f", null))
    }

    // Boolean Formatting Tests
    @Test
    fun testBooleanFormat() {
        assertEquals("Result: true", StringFormatter.format("Result: %b", true))
        assertEquals("Result: false", StringFormatter.format("Result: %b", false))
    }

    @Test
    fun testBooleanFromString() {
        assertEquals("Result: true", StringFormatter.format("Result: %b", "true"))
        assertEquals("Result: false", StringFormatter.format("Result: %b", "false"))
        assertEquals("Result: false", StringFormatter.format("Result: %b", "something"))
    }

    @Test
    fun testNullBoolean() {
        assertEquals("Result: false", StringFormatter.format("Result: %b", null))
    }

    // Hexadecimal Formatting Tests
    @Test
    fun testHexFormatLowercase() {
        assertEquals("Hex: ff", StringFormatter.format("Hex: %x", 255))
        assertEquals("Hex: 10", StringFormatter.format("Hex: %x", 16))
        assertEquals("Hex: 0", StringFormatter.format("Hex: %x", 0))
    }

    @Test
    fun testHexFormatUppercase() {
        assertEquals("Hex: FF", StringFormatter.format("Hex: %X", 255))
        assertEquals("Hex: 10", StringFormatter.format("Hex: %X", 16))
        assertEquals("Hex: DEADBEEF", StringFormatter.format("Hex: %X", 0xDEADBEEF))
    }

    @Test
    fun testHexFromChar() {
        assertEquals("Hex: 41", StringFormatter.format("Hex: %x", 'A'))
    }

    @Test
    fun testNullHex() {
        assertEquals("Hex: 0", StringFormatter.format("Hex: %x", null))
    }

    // Octal Formatting Tests
    @Test
    fun testOctalFormat() {
        assertEquals("Octal: 377", StringFormatter.format("Octal: %o", 255))
        assertEquals("Octal: 20", StringFormatter.format("Octal: %o", 16))
        assertEquals("Octal: 0", StringFormatter.format("Octal: %o", 0))
    }

    @Test
    fun testOctalFromChar() {
        assertEquals("Octal: 101", StringFormatter.format("Octal: %o", 'A'))
    }

    // Scientific Notation Tests
    @Test
    fun testScientificNotationLowercase() {
        assertEquals("Value: 1.234568e+02", StringFormatter.format("Value: %e", 123.4567890))
        assertEquals("Value: 1.234568e-02", StringFormatter.format("Value: %e", 0.01234567890))
    }

    @Test
    fun testScientificNotationUppercase() {
        assertEquals("Value: 1.234568E+02", StringFormatter.format("Value: %E", 123.4567890))
        assertEquals("Value: 1.234568E-02", StringFormatter.format("Value: %E", 0.01234567890))
    }

    @Test
    fun testScientificNotationWithPrecision() {
        assertEquals("Value: 1.23e+02", StringFormatter.format("Value: %.2e", 123.4567890))
        assertEquals("Value: 1e+02", StringFormatter.format("Value: %.0e", 123.4567890))
    }

    @Test
    fun testScientificNotationZero() {
        assertEquals("Value: 0.000000e+00", StringFormatter.format("Value: %e", 0.0))
        assertEquals("Value: 0E+00", StringFormatter.format("Value: %.0E", 0.0))
    }

    @Test
    fun testScientificNotationNegative() {
        assertEquals("Value: -1.234568e+02", StringFormatter.format("Value: %e", -123.4567890))
    }

    // General Format Tests
    @Test
    fun testGeneralFormatLowercase() {
        // General format chooses between fixed and scientific notation
        // %g with default precision=6 shows 6 significant digits
        // Trailing zeros are removed
        // Uses scientific notation when exponent < -4 or >= precision
        assertEquals("Value: 123.457", StringFormatter.format("Value: %g", 123.456789))
        assertEquals("Value: 1.23457e+09", StringFormatter.format("Value: %g", 1234567890.0))
        assertEquals("Value: 1e-05", StringFormatter.format("Value: %g", 0.00001))
        // 0.0001 (exponent=-4) should use fixed notation
        assertEquals("Value: 0.0001", StringFormatter.format("Value: %g", 0.0001))
    }

    @Test
    fun testGeneralFormatUppercase() {
        // %G with default precision=6 shows 6 significant digits
        // Trailing zeros are removed
        assertEquals("Value: 123.457", StringFormatter.format("Value: %G", 123.456789))
        assertEquals("Value: 1.23457E+09", StringFormatter.format("Value: %G", 1234567890.0))
    }

    // Width and Alignment Tests
    @Test
    fun testWidthRightAlign() {
        // Width of 5 for number 42 (2 digits) = 3 spaces padding
        assertEquals("Number:    42", StringFormatter.format("Number: %5d", 42))
        // Width of 6 for "hello" (5 chars) = 1 space padding
        assertEquals("Text:  hello", StringFormatter.format("Text: %6s", "hello"))
    }

    @Test
    fun testWidthLeftAlign() {
        assertEquals("Number: 42   ", StringFormatter.format("Number: %-5d", 42))
        assertEquals("Text: hello ", StringFormatter.format("Text: %-6s", "hello"))
    }

    @Test
    fun testWidthZeroPadding() {
        assertEquals("Number: 00042", StringFormatter.format("Number: %05d", 42))
        assertEquals("Number: -0042", StringFormatter.format("Number: %05d", -42))
    }

    @Test
    fun testWidthWithFloat() {
        assertEquals("Value:    3.14", StringFormatter.format("Value: %7.2f", 3.14))
    }

    @Test
    fun testWidthNoEffect() {
        assertEquals("Number: 123456", StringFormatter.format("Number: %3d", 123456))
    }

    // Positional Arguments Tests
    @Test
    fun testPositionalArguments() {
        assertEquals("World Hello", StringFormatter.format("%2\$s %1\$s", "Hello", "World"))
    }

    @Test
    fun testPositionalArgumentsRepeated() {
        assertEquals("Hello World Hello", StringFormatter.format("%1\$s %2\$s %1\$s", "Hello", "World"))
    }

    @Test
    fun testPositionalArgumentsMixed() {
        assertEquals("42 World 42", StringFormatter.format("%1\$d %2\$s %1\$d", 42, "World"))
    }

    @Test
    fun testPositionalWithFormatting() {
        assertEquals("  3.14 Hello", StringFormatter.format("%2\$6.2f %1\$s", "Hello", 3.14159))
    }

    // Special Cases Tests
    @Test
    fun testPercentEscape() {
        assertEquals("100% done", StringFormatter.format("100%% done"))
        assertEquals("50% of 100%", StringFormatter.format("50%% of 100%%"))
    }

    @Test
    fun testMixedFormats() {
        assertEquals(
            "Name: Alice, Age: 30, Height: 1.75m, Active: true",
            StringFormatter.format("Name: %s, Age: %d, Height: %.2fm, Active: %b", "Alice", 30, 1.75, true)
        )
    }

    @Test
    fun testEmptyString() {
        assertEquals("", StringFormatter.format(""))
    }

    @Test
    fun testNoFormatSpecifiers() {
        assertEquals("Just plain text", StringFormatter.format("Just plain text"))
    }

    @Test
    fun testComplexFormatting() {
        val result = StringFormatter.format(
            "User: %s, ID: %05d, Balance: $%.2f, Status: %b, Hex ID: 0x%X",
            "John Doe",
            42,
            1234.5678,
            true,
            255
        )
        assertEquals("User: John Doe, ID: 00042, Balance: $1234.57, Status: true, Hex ID: 0xFF", result)
    }

    // Extension Function Tests
    @Test
    fun testExtensionFunction() {
        assertEquals("Hello World", "Hello %s".format("World"))
        assertEquals("Number: 42", "Number: %d".format(42))
    }

    // Error Cases Tests
    @Test
    fun testMissingArgument() {
        assertFailsWith<IllegalArgumentException> {
            StringFormatter.format("Hello %s %s", "World")
        }
    }

    @Test
    fun testPositionalArgumentOutOfBounds() {
        assertFailsWith<IllegalArgumentException> {
            StringFormatter.format("%5\$s", "Hello", "World")
        }
    }

    // Edge Cases Tests
    @Test
    fun testVeryLargeNumber() {
        assertEquals("Number: 9223372036854775807", StringFormatter.format("Number: %d", Long.MAX_VALUE))
    }

    @Test
    fun testVerySmallNumber() {
        assertEquals("Number: -9223372036854775808", StringFormatter.format("Number: %d", Long.MIN_VALUE))
    }

    @Test
    fun testVeryLargeFloat() {
        val result = StringFormatter.format("Value: %.2f", Double.MAX_VALUE)
        // Just check it doesn't crash, exact formatting of MAX_VALUE can be platform-dependent
        kotlin.test.assertTrue(result.startsWith("Value:"))
    }

    @Test
    fun testZeroValues() {
        assertEquals(
            "Int: 0, Float: 0.00, Hex: 0, Oct: 0",
            StringFormatter.format("Int: %d, Float: %.2f, Hex: %x, Oct: %o", 0, 0.0, 0, 0)
        )
    }

    @Test
    fun testMultipleFormatsInSequence() {
        assertEquals(
            "s:test d:1 f:2.50 b:true x:ff o:10",
            StringFormatter.format("s:%s d:%d f:%.2f b:%b x:%x o:%o", "test", 1, 2.5, true, 255, 8)
        )
    }

    @Test
    fun testWidthWithAllTypes() {
        assertEquals(
            "s:  test d:   42 f: 3.14",
            StringFormatter.format("s:%6s d:%5d f:%5.2f", "test", 42, 3.14)
        )
    }

    @Test
    fun testComplexPositionalWithWidth() {
        assertEquals(
            "  42 World   42",
            StringFormatter.format("%1\$4d %2\$s %1\$4d", 42, "World")
        )
    }

    // ByteArray Formatting Tests
    @Test
    fun testByteArrayFormattingLowercase() {
        val bytes = byteArrayOf(0x00, 0x01, 0x0F, 0x10, 0x7F)
        val result = bytes.joinToString(":") { "%02x".format(it) }
        assertEquals("00:01:0f:10:7f", result)
    }

    @Test
    fun testByteArrayFormattingUppercase() {
        val bytes = byteArrayOf(0x00, 0x01, 0x0F, 0x10, 0x7F)
        val result = bytes.joinToString(":") { "%02X".format(it) }
        assertEquals("00:01:0F:10:7F", result)
    }

    @Test
    fun testByteArrayWithNegativeValues() {
        // Bytes > 0x7F are negative in Kotlin (signed byte)
        val bytes = byteArrayOf(0x80.toByte(), 0xAA.toByte(), 0xFF.toByte())
        val result = bytes.joinToString(":") { "%02x".format(it) }
        assertEquals("80:aa:ff", result)
    }

    @Test
    fun testMacAddressFormatting() {
        // Real-world MAC address formatting
        val sourceMac =
            byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
        val result = sourceMac.joinToString(":") { "%02x".format(it) }
        assertEquals("aa:bb:cc:dd:ee:ff", result)
    }

    @Test
    fun testMacAddressFormattingUppercase() {
        val sourceMac =
            byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
        val result = sourceMac.joinToString(":") { "%02X".format(it) }
        assertEquals("AA:BB:CC:DD:EE:FF", result)
    }

    @Test
    fun testByteArrayMixedValues() {
        // Mix of small, medium, and large byte values
        val bytes = byteArrayOf(0x00, 0x12, 0x7F, 0x80.toByte(), 0xAB.toByte(), 0xFF.toByte())
        val result = bytes.joinToString(" ") { "%02x".format(it) }
        assertEquals("00 12 7f 80 ab ff", result)
    }

    @Test
    fun testByteFormattingWithWidth() {
        // Ensure padding works correctly
        assertEquals("00", StringFormatter.format("%02x", 0.toByte()))
        assertEquals("01", StringFormatter.format("%02x", 1.toByte()))
        assertEquals("0f", StringFormatter.format("%02x", 0x0F.toByte()))
        assertEquals("ff", StringFormatter.format("%02x", 0xFF.toByte()))
    }

    @Test
    fun testByteFormattingWithoutWidth() {
        // Without width specifier
        assertEquals("0", StringFormatter.format("%x", 0.toByte()))
        assertEquals("1", StringFormatter.format("%x", 1.toByte()))
        assertEquals("f", StringFormatter.format("%x", 0x0F.toByte()))
        assertEquals("ff", StringFormatter.format("%x", 0xFF.toByte()))
    }

    @Test
    fun testByteArrayDifferentSeparators() {
        val bytes = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

        val colonResult = bytes.joinToString(":") { "%02x".format(it) }
        assertEquals("de:ad:be:ef", colonResult)

        val dashResult = bytes.joinToString("-") { "%02X".format(it) }
        assertEquals("DE-AD-BE-EF", dashResult)

        val spaceResult = bytes.joinToString(" ") { "%02X".format(it) }
        assertEquals("DE AD BE EF", spaceResult)

        val noSepResult = bytes.joinToString("") { "%02x".format(it) }
        assertEquals("deadbeef", noSepResult)
    }

    @Test
    fun testByteToUnsignedConversion() {
        // Test that negative byte values format correctly as unsigned hex
        val negativeByte: Byte = -1  // 0xFF
        assertEquals("ff", StringFormatter.format("%02x", negativeByte))

        val byte128: Byte = -128  // 0x80
        assertEquals("80", StringFormatter.format("%02x", byte128))

        val byte127: Byte = 127  // 0x7F
        assertEquals("7f", StringFormatter.format("%02x", byte127))
    }

    @Test
    fun testByteOctalFormatting() {
        // Test that bytes format correctly as octal
        assertEquals("0", StringFormatter.format("%o", 0.toByte()))
        assertEquals("77", StringFormatter.format("%o", 0x3F.toByte()))
        assertEquals("177", StringFormatter.format("%o", 0x7F.toByte()))
        // Negative bytes should be treated as unsigned
        assertEquals("200", StringFormatter.format("%o", 0x80.toByte()))  // -128 as unsigned = 128
        assertEquals("377", StringFormatter.format("%o", 0xFF.toByte()))  // -1 as unsigned = 255
    }

    @Test
    fun testByteWidthPadding() {
        // Test width padding with bytes
        assertEquals("  ff", StringFormatter.format("%4x", 0xFF.toByte()))
        assertEquals("00ff", StringFormatter.format("%04x", 0xFF.toByte()))
        assertEquals("ff  ", StringFormatter.format("%-4x", 0xFF.toByte()))
    }

    @Test
    fun testRealWorldByteArrays() {
        // Test realistic byte array scenarios

        // IPv6 address segment formatting
        val ipv6Segment = byteArrayOf(0x20.toByte(), 0x01.toByte())
        assertEquals("2001", ipv6Segment.joinToString("") { "%02x".format(it) })

        // Binary data dump (Java class file magic number)
        val binaryData = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
        assertEquals("CAFEBABE", binaryData.joinToString("") { "%02X".format(it) })

        // Checksums
        val checksum: Byte = 0x5A.toByte()
        assertEquals("5a", StringFormatter.format("%02x", checksum))

        // Full MAC address (your production case)
        val sourceMac =
            byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
        val macStr = sourceMac.joinToString(":") { "%02x".format(it) }
        assertEquals("aa:bb:cc:dd:ee:ff", macStr)
    }
}