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
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class HardwareServletPage extends AbstractJiraPage {

    @Inject
    private Timeouts timeouts;

    @Inject
    private ExtendedElementFinder extendedElementFinder;
    @ElementBy(id = "hardware_management_link")
    private PageElement hardwareManagementLink;
    @ElementBy(id = "tabs-overview")
    private PageElement tabOverview;
    @ElementBy(id = "table-overview")
    private PageElement tableOverview;
    @ElementBy(id = "tabs-lent-out")
    private PageElement tabLentOut;
    @ElementBy(id = "search-filter-overview")
    private PageElement searchField;
    @ElementBy(className = "lending_out")
    private Iterable<PageElement> lendOutButtons;
    @ElementBy(className = "serial")
    private PageElement serial;

    @Override
    public TimedCondition isAt() {
        return tabOverview.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/admin_helper/hardware";
    }

    public TimedCondition linkVisible() {
        return hardwareManagementLink.timed().isVisible();
    }

    public LendingOutDialogPage lendOutDevice(int index) {
        int i = 0;
        PageElement button = null;
        for (PageElement temp : lendOutButtons) {
            if (i == index) {
                button = temp;
                break;
            }
            i++;
        }

        if (button == null) {
            return null;
        }

        Poller.waitUntilTrue(button.timed().isEnabled());
        button.click();
        return pageBinder.bind(LendingOutDialogPage.class);
    }

    public TimedQuery<Iterable<PageElement>> getAvailables() {
        return Queries.forSupplier(timeouts,
                extendedElementFinder.within(tableOverview).
                        newQuery(By.className("available")).
                        supplier());
    }

    public TimedQuery<String> getSerial() {
        return serial.timed().getValue();
    }

    public PageElement getTableOverview() {
        return tableOverview;
    }
}
