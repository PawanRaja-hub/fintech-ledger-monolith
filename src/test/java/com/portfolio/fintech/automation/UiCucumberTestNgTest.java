package com.portfolio.fintech.automation;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features/ui",
        glue = "com.portfolio.fintech.automation.steps",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/ui.html",
                "json:target/cucumber-reports/ui.json"
        }
)
public class UiCucumberTestNgTest extends AbstractTestNGCucumberTests {
}
