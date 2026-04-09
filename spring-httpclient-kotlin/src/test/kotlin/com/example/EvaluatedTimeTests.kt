package com.example

import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo

open class EvaluatedTimeTests {
    companion object {
        private var stime: Long = 0
        private var etime: Long = 0
        private var totalStime: Long = 0
        private var totalEtime: Long = 0

        @JvmStatic
        @BeforeAll
        fun beforeAllTest(testInfo: TestInfo) {
            totalStime = System.nanoTime()
            println("\n### start tests: ${testInfo.displayName}")
        }

        @JvmStatic
        @AfterAll
        fun afterAllTest(testInfo: TestInfo) {
            println("\n### end tests: ${testInfo.displayName}")
            totalEtime = System.nanoTime()
            println(
                "\n### estimated time(msec): " +
                    TimeUnit.MILLISECONDS.convert(totalEtime - totalStime, TimeUnit.NANOSECONDS)
            )
        }
    }

    @BeforeEach
    fun beforeEachTest(testInfo: TestInfo) {
        println("\n*** start test: ${testInfo.displayName}")
        stime = System.nanoTime()
    }

    @AfterEach
    fun afterEachTest(testInfo: TestInfo) {
        etime = System.nanoTime()
        println("\n*** end test: ${testInfo.displayName}")
        println("\n*** estimated time(nano): ${etime - stime}")
    }
}
