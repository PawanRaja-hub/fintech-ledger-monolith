package com.portfolio.fintech.automation;

import org.junit.jupiter.api.Test;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import static org.assertj.core.api.Assertions.assertThat;

class RunUiCucumberIT {

    @Test
    void runUiCucumberThroughTestNg() {
        TestListenerAdapter listener = new TestListenerAdapter();
        TestNG testNg = new TestNG();
        testNg.setTestClasses(new Class[]{UiCucumberTestNgTest.class});
        testNg.setDefaultSuiteName("UI Cucumber Automation");
        testNg.setDefaultTestName("Selenium UI scenarios");
        testNg.addListener(listener);
        testNg.run();

        assertThat(listener.getFailedTests()).isEmpty();
        assertThat(listener.getSkippedTests()).isEmpty();
        assertThat(listener.getPassedTests()).as("TestNG should execute Cucumber scenarios").isNotEmpty();
    }
}
