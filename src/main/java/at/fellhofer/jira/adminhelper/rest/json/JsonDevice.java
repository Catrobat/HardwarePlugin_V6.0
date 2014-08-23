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
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@XmlRootElement
public class JsonDevice {
    @XmlElement
    int ID;
    @XmlElement
    String hardwareModelName;
    @XmlElement
    String serialNumber;
    @XmlElement
    String imei;
    @XmlElement
    String inventoryNumber;
    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    Date receivedDate;
    @XmlElement
    String receivedBy;
    @XmlElement
    String usefulLiveOfAsset;
    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    Date sortedOutDate;
    @XmlElement
    String sortedOutComment;
    @XmlElement
    String currentlyLentOutFrom;
    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    Date currentlyLentOutSince;

    public JsonDevice() {

    }

    public JsonDevice(Device toCopy) throws InvocationTargetException, IllegalAccessException {
        ConvertUtils.register(new DateConverter(null), Date.class);
        copyProperties(this, toCopy);
        hardwareModelName = toCopy.getHardwareModel().getName();
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

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
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
