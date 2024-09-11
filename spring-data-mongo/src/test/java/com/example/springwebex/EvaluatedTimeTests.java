package com.example.springwebex;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.util.StopWatch;

public class EvaluatedTimeTests {
    private static StopWatch stopWatch = new StopWatch();

    @BeforeAll
    static public void beforeAllTest(TestInfo testInfo) {
        stopWatch.setKeepTaskList(true);
        System.out.println("\n*** start tests: " + testInfo.getDisplayName());
    }

    @AfterAll
    static public void afterAllTest(TestInfo testInfo) {
        System.out.println("\n*** end tests: " + testInfo.getDisplayName());
        System.out.println(stopWatch.prettyPrint());
        stopWatch.setKeepTaskList(false);
    }

    @BeforeEach
    public void beforeEachTest(TestInfo testInfo) {
        System.out.println("\n*** start test: " + testInfo.getDisplayName());
        stopWatch.start(testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEachTest(TestInfo testInfo) {
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        System.out.println("\n*** end test: " + testInfo.getDisplayName());
    }
}
