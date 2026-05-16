package com.portfolio.fintech.automation;

import org.junit.jupiter.api.Test;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunBackendCucumberIT {

    @Test
    void runBackendCucumberThroughTestNg() {
        TestListenerAdapter listener = new TestListenerAdapter();
        TestNG testNg = new TestNG();
        testNg.setTestClasses(new Class[]{BackendCucumberTestNgTest.class});
        testNg.setDefaultSuiteName("Backend Cucumber Automation");
        testNg.setDefaultTestName("Backend API scenarios");
        testNg.addListener(listener);
        testNg.run();

        assertThat(listener.getFailedTests()).isEmpty();
        assertThat(listener.getSkippedTests()).isEmpty();
        assertThat(listener.getPassedTests()).as("TestNG should execute Cucumber scenarios").isNotEmpty();
    }
}
