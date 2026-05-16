package com.portfolio.fintech.automation;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features/backend",
        glue = "com.portfolio.fintech.automation.steps",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/backend.html",
                "json:target/cucumber-reports/backend.json"
        }
)
public class BackendCucumberTestNgTest extends AbstractTestNGCucumberTests {
}
