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

import at.fellhofer.jira.adminhelper.activeobject.HardwareModel;
import at.fellhofer.jira.adminhelper.activeobject.LendingService;
import at.fellhofer.jira.adminhelper.activeobject.LendingServiceImpl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@XmlRootElement
public class JsonHardwareModel {
    @XmlElement
    private int ID;
    @XmlElement
    private String name;
    @XmlElement
    private String typeOfDevice;
    @XmlElement
    private String version;
    @XmlElement
    private String price;
    @XmlElement
    private String producer;
    @XmlElement
    private String operatingSystem;
    @XmlElement
    private String articleNumber;
    @XmlElement
    private int available;
    @XmlElement
    private int sumOfDevices;

    public JsonHardwareModel() {
    }

    public JsonHardwareModel(HardwareModel toCopy, LendingService lendingService) throws InvocationTargetException, IllegalAccessException {
        copyProperties(this, toCopy);
        setProducer(toCopy.getProducer().getProducerName());
        setOperatingSystem(toCopy.getOperatingSystem().getOperatingSystemName());
        setTypeOfDevice(toCopy.getTypeOfDevice().getTypeOfDeviceName());

        available = toCopy.getDevices().length - lendingService.currentlyLentOutDevices(toCopy).size();
        sumOfDevices = toCopy.getDevices().length;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeOfDevice() {
        return typeOfDevice;
    }

    public void setTypeOfDevice(String typeOfDevice) {
        this.typeOfDevice = typeOfDevice;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getSumOfDevices() {
        return sumOfDevices;
    }

    public void setSumOfDevices(int sumOfDevices) {
        this.sumOfDevices = sumOfDevices;
    }
}