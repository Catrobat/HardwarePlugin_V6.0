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

package at.fellhofer.jira.adminhelper.rest.json;

import at.fellhofer.jira.adminhelper.activeobject.Device;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement
public class JsonDevice {

    @XmlElement
    private int ID;

    @XmlElement
    private String hardwareModelName;

    @XmlElement
    private String serialNumber;

    @XmlElement
    private String imei;

    @XmlElement
    private String inventoryNumber;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date receivedDate;

    @XmlElement
    private String receivedFrom;

    @XmlElement
    private String usefulLiveOfAsset;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date sortedOutDate;

    @XmlElement
    private String sortedOutComment;

    @XmlElement
    private String currentlyLentOutFrom;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date currentlyLentOutSince;

    public JsonDevice() {

    }

    public JsonDevice(Device toCopy) {
        ID = toCopy.getID();
        hardwareModelName = toCopy.getHardwareModel().getName();
        serialNumber = toCopy.getSerialNumber();
        imei = toCopy.getImei();
        inventoryNumber = toCopy.getInventoryNumber();
        receivedDate = toCopy.getReceivedDate();
        receivedFrom = toCopy.getReceivedFrom();
        usefulLiveOfAsset = toCopy.getUsefulLifeOfAsset();
        sortedOutDate = toCopy.getSortedOutDate();
        sortedOutComment = toCopy.getSortedOutComment();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getHardwareModelName() {
        return hardwareModelName;
    }

    public void setHardwareModelName(String hardwareModelName) {
        this.hardwareModelName = hardwareModelName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getReceivedFrom() {
        return receivedFrom;
    }

    public void setReceivedFrom(String receivedFrom) {
        this.receivedFrom = receivedFrom;
    }

    public String getUsefulLiveOfAsset() {
        return usefulLiveOfAsset;
    }

    public void setUsefulLiveOfAsset(String usefulLiveOfAsset) {
        this.usefulLiveOfAsset = usefulLiveOfAsset;
    }

    public Date getSortedOutDate() {
        return sortedOutDate;
    }

    public void setSortedOutDate(Date sortedOutDate) {
        this.sortedOutDate = sortedOutDate;
    }

    public String getSortedOutComment() {
        return sortedOutComment;
    }

    public void setSortedOutComment(String sortedOutComment) {
        this.sortedOutComment = sortedOutComment;
    }

    public String getCurrentlyLentOutFrom() {
        return currentlyLentOutFrom;
    }

    public void setCurrentlyLentOutFrom(String currentlyLentOutFrom) {
        this.currentlyLentOutFrom = currentlyLentOutFrom;
    }

    public Date getCurrentlyLentOutSince() {
        return currentlyLentOutSince;
    }

    public void setCurrentlyLentOutSince(Date currentlyLentOutSince) {
        this.currentlyLentOutSince = currentlyLentOutSince;
    }
}
