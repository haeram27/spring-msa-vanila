package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Month;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ParameterizedTests {

    /**
     * https://www.baeldung.com/parameterized-tests-junit-5
     * @ValueSource
     * @NullSource
     * @EmptySource
     * @NullAndEmptySource
     * @EnumSource
     * @CsvSource
     * @CsvFileSource
     */

    /*
    * @ValueSource : single argument, list
    */
    // @ValueSource is only support these types:

    // short (with the shorts attribute)
    // byte (bytes attribute)
    // int (ints attribute)
    // long (longs attribute)
    // float (floats attribute)
    // double (doubles attribute)
    // char (chars attribute)
    // java.lang.String (strings attribute)
    // java.lang.Class (classes attribute)

    boolean isOdd(int n) {
        if (n == 0)
            return true;
        return (n % 2 == 1) ? true : false;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, -3, 15, Integer.MAX_VALUE}) // six numbers
    void valueSourceIntTest(int number) {
        assertTrue(isOdd(number));
    }

    public class Strings {
        public static boolean isBlank(String input) {
            return input == null || input.trim().isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void isBlank_ShouldReturnTrueForNullOrBlankStrings(String input) {
        assertTrue(Strings.isBlank(input));
    }

    @ParameterizedTest
    @NullSource
    void isBlank_ShouldReturnTrueForNullInputs(String input) {
        assertTrue(Strings.isBlank(input));
    }

    @ParameterizedTest
    @EmptySource
    void isBlank_ShouldReturnTrueForEmptyStrings(String input) {
        assertTrue(Strings.isBlank(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isBlank_ShouldReturnTrueForNullAndEmptyStrings(String input) {
        assertTrue(Strings.isBlank(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings(String input) {
        assertTrue(Strings.isBlank(input));
    }

    /*
     * @EnumSource
     */
    @ParameterizedTest
    @EnumSource(Month.class) // passing all 12 months
    void getValueForAMonth_IsAlwaysBetweenOneAndTwelve(Month month) {
        int monthNumber = month.getValue();
        assertTrue(monthNumber >= 1 && monthNumber <= 12);
    }

    @ParameterizedTest
    @EnumSource(value = Month.class, names = {"APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"})
    void someMonths_Are30DaysLong(Month month) {
        final boolean isALeapYear = false;
        assertEquals(30, month.length(isALeapYear));
    }

    @ParameterizedTest
    @EnumSource(value = Month.class, names = {"APRIL", "JUNE", "SEPTEMBER", "NOVEMBER", "FEBRUARY"},
            mode = EnumSource.Mode.EXCLUDE)
    void exceptFourMonths_OthersAre31DaysLong(Month month) {
        final boolean isALeapYear = false;
        assertEquals(31, month.length(isALeapYear));
    }

    @ParameterizedTest
    @EnumSource(value = Month.class, names = ".+BER", mode = EnumSource.Mode.MATCH_ANY)
    void fourMonths_AreEndingWithBer(Month month) {
        EnumSet<Month> months = EnumSet.of(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER);
        assertTrue(months.contains(month));
    }

    /*
     * @CsvSource : multiple arguments, list
     */
    // default delimeter: comma
    @ParameterizedTest
    @CsvSource({"test,TEST", "tEst,TEST", "Java,JAVA"})
    void toUpperCase_ShouldGenerateTheExpectedUppercaseValue(String input, String expected) {
        String actualValue = input.toUpperCase();
        assertEquals(expected, actualValue);
    }

    // delimeter: colon
    @ParameterizedTest
    @CsvSource(value = {"test:test", "tEst:test", "Java:java"}, delimiter = ':')
    void toLowerCase_ShouldGenerateTheExpectedLowercaseValue(String input, String expected) {
        String actualValue = input.toLowerCase();
        assertEquals(expected, actualValue);
    }

    /*
    * @CsvFileSource : multiple arguments, list
    */
    // === data.csv
    // input,expected
    // test,TEST
    // tEst,TEST
    // Java,JAVA
    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void toUpperCase_ShouldGenerateTheExpectedUppercaseValueCSVFile(String input, String expected) {
        String actualValue = input.toUpperCase();
        assertEquals(expected, actualValue);
    }

    /*
    * @MethodSource : multiple arguments, list
    */
    private static Stream<Arguments> provideStringsForIsBlank() {
        return Stream.of(
        // @formatter:off
            Arguments.of(null, true),
            Arguments.of("", true),
            Arguments.of("  ", true),
            Arguments.of("not blank", false)
        // @formatter:on
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    void isBlank_ShouldReturnTrueForNullOrBlankStrings(String input, boolean expected) {
        assertEquals(expected, Strings.isBlank(input));
    }

    // no param method name if there is same name of param method
    private static Stream<String> isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgument() {
        return Stream.of(null, "", "  ");
    }

    @ParameterizedTest
    @MethodSource // hmm, no method name ...
    void isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgument(String input) {
        assertTrue(Strings.isBlank(input));
    }

    /* use parameter method in other package */
    // class StringParams {
    //     static Stream<String> blankStrings() {
    //         return Stream.of(null, "", "  ");
    //     }
    // }

    // class StringsUnitTest {

    //     @ParameterizedTest
    //     @MethodSource("com.example.test.StringParams#blankStrings")
    //     void isBlank_ShouldReturnTrueForNullOrBlankStringsExternalSource(String input) {
    //         assertTrue(Strings.isBlank(input));
    //     }
    // }

    /*
    * @ArgumentsSource :  Custom ArgumentsProvider interface
    */
    class BlankStringsArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

            return Stream.of(
            // @formatter:off
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   ")
            // @formatter:on
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringsArgumentsProvider.class)
    void isBlank_ShouldReturnTrueForNullOrBlankStringsArgProvider(String input) {
        assertTrue(Strings.isBlank(input));
    }

    /*
     * Timeout Annotation
     */
    @Test
    @Timeout(value = 3, threadMode = Timeout.ThreadMode.SEPARATE_THREAD) // Deafault TimeUnit.SECONDS
    //@Timeout(value = 3, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void timeOutTest() {
        try {
            Thread.sleep(6000);
        } catch (Exception e) {

        }
    }
}
