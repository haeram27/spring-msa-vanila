package com.example;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
// import org.springframework.util.StopWatch;

// public class EvaluatedTimeStopWatchTests {
// private static StopWatch stopWatch = new StopWatch();

// @BeforeAll
// static public void beforeAllTest(TestInfo testInfo) {
// stopWatch.setKeepTaskList(true);
// System.out.println("\n*** start tests: " + testInfo.getDisplayName());
// }

// @AfterAll
// static public void afterAllTest(TestInfo testInfo) {
// System.out.println("\n*** end tests: " + testInfo.getDisplayName());
// System.out.println(stopWatch.prettyPrint());
// stopWatch.setKeepTaskList(false);
// }

// @BeforeEach
// public void beforeEachTest(TestInfo testInfo) {
// System.out.println("\n*** start test: " + testInfo.getDisplayName());
// stopWatch.start(testInfo.getDisplayName());
// }

// @AfterEach
// public void afterEachTest(TestInfo testInfo) {
// if (stopWatch.isRunning()) {
// stopWatch.stop();
// }
// System.out.println("\n*** end test: " + testInfo.getDisplayName());
// }
// }

public class EvaluatedTimeTests {
    private static long stime, etime, total_stime, total_etime;

    @BeforeAll
    static public void beforeAllTest(TestInfo testInfo) {
        total_stime = System.nanoTime();
        System.out.println("\n### start tests: " + testInfo.getDisplayName());
    }

    @AfterAll
    static public void afterAllTest(TestInfo testInfo) {
        System.out.println("\n### end tests: " + testInfo.getDisplayName());
        total_etime = System.nanoTime();
        System.out.println("\n### estimated time(msec): "
                + TimeUnit.MILLISECONDS.convert((total_etime - total_stime), TimeUnit.NANOSECONDS));
    }

    @BeforeEach
    public void beforeEachTest(TestInfo testInfo) {
        System.out.println("\n*** start test: " + testInfo.getDisplayName());
        stime = System.nanoTime();
    }

    @AfterEach
    public void afterEachTest(TestInfo testInfo) {
        etime = System.nanoTime();
        System.out.println("\n*** end test: " + testInfo.getDisplayName());
        System.out.println("\n*** estimated time(nano): " + (etime - stime));
    }
}
