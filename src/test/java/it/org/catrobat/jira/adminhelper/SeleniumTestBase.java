/*
 * Copyright 2014 Stephan Fellhofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.org.catrobat.jira.adminhelper;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.jira.tests.TestBase;
import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.Selenium;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;

import java.util.Calendar;

public abstract class SeleniumTestBase extends TestBase {
    protected Selenium selenium;
    protected String today;

    @Before
    public void setUp() {
        Backdoor testKit = jira().backdoor();
        testKit.restoreDataFromResource("selenium.zip", TimeBombLicence.LICENCE_FOR_TESTING);
        testKit.websudo().disable();
        jira().gotoLoginPage().loginAsSysAdmin(HardwareServletPage.class);
        WebDriver driver = jira().getTester().getDriver();
        String baseUrl = "http://localhost:2990/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);

        Calendar calendar = Calendar.getInstance();
        String month = "" + (calendar.get(Calendar.MONTH) + 1);
        month = month.length() == 1 ? "0" + month : month;
        today = calendar.get(Calendar.YEAR) + "-" +
                month + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }

    protected void verifyEquals(String expected, String actual) {
        SeleneseTestBase.assertEquals(expected, actual);
    }

    protected void verifyNotEquals(String expected, String actual) {
        SeleneseTestBase.assertNotEquals(expected, actual);
    }
}
