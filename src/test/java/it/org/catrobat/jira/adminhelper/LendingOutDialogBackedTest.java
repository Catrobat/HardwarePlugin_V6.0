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

import com.atlassian.jira.tests.TestBase;
import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.Selenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;

import java.awt.event.KeyEvent;
import java.io.File;

import static com.thoughtworks.selenium.SeleneseTestBase.fail;

public class LendingOutDialogBackedTest extends TestBase {
    private Selenium selenium;

    @Before
    public void setUp() throws Exception {
//        new DataRestorer(jira()).doRestore();
        jira().gotoLoginPage().loginAsSysAdmin(HardwareServletPage.class);
        WebDriver driver = jira().getTester().getDriver();
        String baseUrl = "http://localhost:2990/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
    }

    @After
    public void tearDown() throws Exception {
        new DataRestorer(jira()).doRestore();
    }

    @Test
    public void testAAA() {

    }

    @Test
    public void testLendingOutDialog() throws Exception {
//        selenium.open("/jira/secure/Dashboard.jspa");
//        selenium.click("id=hardware_management_link");
        selenium.waitForPageToLoad("30000");
        verifyEquals("2/3", selenium.getText("css=td.available"));
        selenium.click("id=1");
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (selenium.isElementPresent("css=td.serial")) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        verifyEquals("111", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr/td)[17]"));
        verifyEquals("111", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr/td[2])[3]"));
        verifyEquals("111", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr/td[3])[3]"));
        verifyEquals("789", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr[2]/td)[9]"));
        verifyEquals("890", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr[2]/td[2])[2]"));
        verifyEquals("901", selenium.getText("xpath=(//tbody[@id='table-lent-out']/tr[2]/td[3])[2]"));
        selenium.click("xpath=(//a[contains(text(),'Choose...')])[2]");
        selenium.click("css=li.page-menu-item.selected > button.item-button");
        verifyEquals("789", selenium.getValue("css=input#serial"));
        verifyEquals("890", selenium.getValue("css=input#imei"));
        verifyEquals("901", selenium.getValue("css=input#inventory"));
        selenium.click("css=div#s2id_user .select2-choice");
        selenium.typeKeys("css=#select2-drop input.select2-input", "Blah");
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if ("Blah".equals(selenium.getText("css=li.select2-highlighted"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        verifyEquals("Blah", selenium.getText("css=li.select2-highlighted"));
        verifyEquals("Blah Blub", selenium.getText("css=li.select2-result:nth-child(2)"));
        selenium.keyPress("css=#select2-drop input.select2-input", "\\13");
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if ("Blah".equals(selenium.getText("css=#s2id_user > a.select2-choice > span.select2-chosen"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        verifyEquals("Blah", selenium.getText("css=#s2id_user > a.select2-choice > span.select2-chosen"));
        selenium.type("css=#device-comment", "Selenium Device Comment");
        selenium.type("css=#begin_date", "2014-12-01");
        selenium.type("css=#lending_purpose", "Selenium Lending Purpose");
        selenium.type("css=#lending-comment", "Selenium Lending Comment");
        selenium.click("css=button.button-panel-button.lend-out-save");
        verifyEquals("1/3", selenium.getText("css=td.available"));
    }

    private void verifyEquals(String expected, String actual) {
        SeleneseTestBase.assertEquals(expected, actual);
    }
}