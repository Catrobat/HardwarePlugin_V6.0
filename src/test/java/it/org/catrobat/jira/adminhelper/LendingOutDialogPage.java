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

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;

import javax.inject.Inject;
import java.sql.Time;

public class LendingOutDialogPage extends AbstractJiraPage {

    @Inject
    private Timeouts timeouts;

    @Inject
    private ExtendedElementFinder extendedElementFinder;

    @ElementBy(id = "lend-out-dialog")
    private PageElement lendingOutDialog;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr/td)[17]")
    private PageElement serial1;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr/td[2])[3]")
    private PageElement imei1;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr/td[3])[3]")
    private PageElement inventory1;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr[2]/td)[9]")
    private PageElement serial2;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr[2]/td[2])[2]")
    private PageElement imei2;

    @ElementBy(xpath = "(//tbody[@id='table-lent-out']/tr[2]/td[3])[2]")
    private PageElement inventory2;

    @ElementBy(xpath = "(//a[contains(text(),'Choose...')])[2]")
    private PageElement chooseStage1;

    @ElementBy(cssSelector = "css=li.page-menu-item.selected > button.item-button")
    private PageElement chooseStage2;

    @ElementBy(cssSelector = "input#serial")
    private PageElement chosenSerial;

    @ElementBy(cssSelector = "input#imei")
    private PageElement chosenImei;

    @ElementBy(cssSelector = "input#inventory")
    private PageElement chosenInventory;

    @Override
    public TimedCondition isAt() {
        return lendingOutDialog.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/admin_helper/hardware";
    }

    public TimedQuery<String> getSerial1() {
        return serial1.timed().getText();
    }

    public TimedQuery<String> getSerial2() {
        return serial2.timed().getText();
    }

    public TimedQuery<String> getImei1() {
        return imei1.timed().getText();
    }

    public TimedQuery<String> getImei2() {
        return imei2.timed().getText();
    }

    public TimedQuery<String> getInventory1() {
        return inventory1.timed().getText();
    }

    public TimedQuery<String> getInventory2() {
        return inventory2.timed().getText();
    }

    public void clickChoose() {
        Poller.waitUntilTrue(chooseStage1.timed().isEnabled());
        Poller.waitUntilTrue(chooseStage2.timed().isEnabled());
        chooseStage1.click();
        chooseStage2.click();
    }

    public TimedQuery<String> getChosenSerial() {
        return chosenSerial.timed().getValue();
    }

    public TimedQuery<String> getChosenImei() {
        return chosenImei.timed().getValue();
    }

    public TimedQuery<String> getChosenInventory() {
        return chosenInventory.timed().getValue();
    }
}
