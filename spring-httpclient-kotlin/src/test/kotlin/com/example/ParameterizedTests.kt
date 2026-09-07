package com.example

import java.time.Month
import java.util.EnumSet
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class ParameterizedTests {

    private fun isOdd(n: Int): Boolean {
        if (n == 0) return true
        return n % 2 == 1
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3, 5, -3, 15, Int.MAX_VALUE])
    fun valueSourceIntTest(number: Int) {
        assertTrue(isOdd(number))
    }

    object Strings {
        fun isBlank(input: String?): Boolean = input == null || input.trim().isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun isBlank_ShouldReturnTrueForNullOrBlankStrings(input: String) {
        assertTrue(Strings.isBlank(input))
    }

    @ParameterizedTest
    @NullSource
    fun isBlank_ShouldReturnTrueForNullInputs(input: String?) {
        assertTrue(Strings.isBlank(input))
    }

    @ParameterizedTest
    @EmptySource
    fun isBlank_ShouldReturnTrueForEmptyStrings(input: String) {
        assertTrue(Strings.isBlank(input))
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun isBlank_ShouldReturnTrueForNullAndEmptyStrings(input: String?) {
        assertTrue(Strings.isBlank(input))
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["  ", "\t", "\n"])
    fun isBlank_ShouldReturnTrueForAllTypesOfBlankStrings(input: String?) {
        assertTrue(Strings.isBlank(input))
    }

    @ParameterizedTest
    @EnumSource(Month::class)
    fun getValueForAMonth_IsAlwaysBetweenOneAndTwelve(month: Month) {
        val monthNumber = month.value
        assertTrue(monthNumber in 1..12)
    }

    @ParameterizedTest
    @EnumSource(value = Month::class, names = ["APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"])
    fun someMonths_Are30DaysLong(month: Month) {
        assertEquals(30, month.length(false))
    }

    @ParameterizedTest
    @EnumSource(
        value = Month::class,
        names = ["APRIL", "JUNE", "SEPTEMBER", "NOVEMBER", "FEBRUARY"],
        mode = EnumSource.Mode.EXCLUDE,
    )
    fun exceptFourMonths_OthersAre31DaysLong(month: Month) {
        assertEquals(31, month.length(false))
    }

    @ParameterizedTest
    @EnumSource(value = Month::class, names = [".+BER"], mode = EnumSource.Mode.MATCH_ANY)
    fun fourMonths_AreEndingWithBer(month: Month) {
        val months = EnumSet.of(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER)
        assertTrue(months.contains(month))
    }

    @ParameterizedTest
    @CsvSource("test,TEST", "tEst,TEST", "Java,JAVA")
    fun toUpperCase_ShouldGenerateTheExpectedUppercaseValue(input: String, expected: String) {
        assertEquals(expected, input.uppercase())
    }

    @ParameterizedTest
    @CsvSource(value = ["test:test", "tEst:test", "Java:java"], delimiter = ':')
    fun toLowerCase_ShouldGenerateTheExpectedLowercaseValue(input: String, expected: String) {
        assertEquals(expected, input.lowercase())
    }

    @ParameterizedTest
    @CsvFileSource(resources = ["/data.csv"], numLinesToSkip = 1)
    fun toUpperCase_ShouldGenerateTheExpectedUppercaseValueCSVFile(input: String, expected: String) {
        assertEquals(expected, input.uppercase())
    }

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    fun isBlank_ShouldReturnTrueForNullOrBlankStrings(input: String?, expected: Boolean) {
        assertEquals(expected, Strings.isBlank(input))
    }

    @ParameterizedTest
    @MethodSource("isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgumentSource")
    fun isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgument(input: String?) {
        assertTrue(Strings.isBlank(input))
    }

    class BlankStringsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(null),
                Arguments.of(""),
                Arguments.of("   "),
            )
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringsArgumentsProvider::class)
    fun isBlank_ShouldReturnTrueForNullOrBlankStringsArgProvider(input: String?) {
        assertTrue(Strings.isBlank(input))
    }

    @Test
    @Timeout(value = 3, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    fun timeOutTest() {
        try {
            Thread.sleep(6000)
        } catch (_: Exception) {
        }
    }

    companion object {
        @JvmStatic
        private fun provideStringsForIsBlank(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null, true),
                Arguments.of("", true),
                Arguments.of("  ", true),
                Arguments.of("not blank", false),
            )
        }

        @JvmStatic
        private fun isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgumentSource(): Stream<String?> {
            return Stream.of(null, "", "  ")
        }
    }
}
