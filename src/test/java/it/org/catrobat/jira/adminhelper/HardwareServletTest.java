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

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class HardwareServletTest extends TestBase {

//    @Test
//    public void testOverviewTab() {
//        HardwareServletPage hardwareServletPage = jira().gotoLoginPage().loginAsSysAdmin(HardwareServletPage.class);
//        Poller.waitUntilTrue(hardwareServletPage.linkVisible());
//        Poller.waitUntil(hardwareServletPage.getAvailables(),
//                IsIterableWithSize.<PageElement>iterableWithSize(2));
//        assertThat(PageElements.asText(hardwareServletPage.getAvailables().now()),
//                IsCollectionContaining.<String>hasItems(
//                        containsString("2/3"),
//                        containsString("1/2")));
//    }

//    @Test
//    public void testLendOutDialog() {
//        HardwareServletPage hardwareServletPage = jira().gotoLoginPage().loginAsSysAdmin(HardwareServletPage.class);
//        Poller.waitUntilTrue(hardwareServletPage.linkVisible());
//        LendingOutDialogPage lendingOutDialog = hardwareServletPage.lendOutDevice(0);
//        Poller.waitUntilTrue(lendingOutDialog.isAt());
//        Poller.waitUntil(lendingOutDialog.getSerial1(), (Matcher<String>) equalTo("111"));
//        Poller.waitUntil(lendingOutDialog.getImei1(), (Matcher<String>) equalTo("111"));
//        Poller.waitUntil(lendingOutDialog.getInventory1(), (Matcher<String>) equalTo("111"));
//        Poller.waitUntil(lendingOutDialog.getSerial2(), (Matcher<String>) equalTo("789"));
//        Poller.waitUntil(lendingOutDialog.getImei2(), (Matcher<String>) equalTo("890"));
//        Poller.waitUntil(lendingOutDialog.getInventory2(), (Matcher<String>) equalTo("901"));
//
//        lendingOutDialog.clickChoose();
//
//        Poller.waitUntil(lendingOutDialog.getChosenSerial(), (Matcher<String>) equalTo("789"));
//        Poller.waitUntil(lendingOutDialog.getChosenImei(), (Matcher<String>) equalTo("890"));
//        Poller.waitUntil(lendingOutDialog.getChosenInventory(), (Matcher<String>) equalTo("901"));

//        HardwareServletPage hardwareServletPage = jira().gotoLoginPage().loginAsSysAdmin(HardwareServletPage.class);
//        Poller.waitUntilTrue(hardwareServletPage.linkVisible());
//        LendingOutDialogPage lendingOutDialog = hardwareServletPage.lendOutDevice(0);
//        Poller.waitUntilTrue(lendingOutDialog.isAt());
//        System.out.println(PageElements.asText(lendingOutDialog.getSerialNumbers().now()));
//        assertThat(PageElements.asText(lendingOutDialog.getSerialNumbers().now()),
//                IsCollectionContaining.<String>hasItems(
//                        containsString("111"),
//                        containsString("789")));
//        assertThat(PageElements.asText(lendingOutDialog.getImeis()),
//                IsCollectionContaining.<String>hasItems(
//                        containsString("111"),
//                        containsString("890")));
//        assertThat(PageElements.asText(lendingOutDialog.getInventoryNumbers()),
//                IsCollectionContaining.<String>hasItems(
//                        containsString("111"),
//                        containsString("901")));
//
//        Poller.waitUntil(lendingOutDialog.getItemButtons(),
//                IsIterableWithSize.<PageElement>iterableWithSize(2));
//        assertNotNull(lendingOutDialog.choose(1));
//
////        Poller.waitUntil(lendingOutDialog.getChosenSerial(), (Matcher<String>) equalTo("789"));
////        Poller.waitUntil(lendingOutDialog.getChosenImei(), (Matcher<String>) equalTo("890"));
////        Poller.waitUntil(lendingOutDialog.getChosenInventory(), (Matcher<String>) equalTo("901"));
//
//        lendingOutDialog.setUser("Blah Blub")
//                .setDeviceComment("PageObject Device Comment")
//                .setLendingComment("PageObject Lending Comment")
//                .setPurpose("PageObject Purpose")
//                .setBegin("2014-12-03")
//                .clickOnSave();
//
//        Poller.waitUntilFalse(lendingOutDialog.isAt());
//
//        Poller.waitUntil(hardwareServletPage.getAvailables(),
//                IsIterableWithSize.<PageElement>iterableWithSize(2));
//        assertThat(PageElements.asText(hardwareServletPage.getAvailables().now()),
//                IsCollectionContaining.<String>hasItems(
//                        containsString("2/3"),
//                        containsString("1/2")));
//    }
}
