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

package at.fellhofer.jira.adminhelper.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class DeviceServiceImpl implements DeviceService {

    private final ActiveObjects ao;

    public DeviceServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Device add(HardwareModel hardwareModel, String imei, String serialNumber, String inventoryNumber, String receivedFrom, Date receivedDate, String usefulLifeOfAsset) {
        if (!(isImeiUnique(imei) && isSerialNumberUnique(serialNumber) && isInventoryNumberUnique(inventoryNumber)) || hardwareModel == null) {
            return null;
        }

        Device device = ao.create(Device.class);
        device.setHardwareModel(hardwareModel);
        device.setImei(imei);
        device.setSerialNumber(serialNumber);
        device.setInventoryNumber(inventoryNumber);
        device.setReceivedFrom(receivedFrom);
        device.setReceivedDate(receivedDate);
        device.setUsefulLifeOfAsset(usefulLifeOfAsset);
        device.save();

        return device;
    }

    @Override
    public List<Device> all() {
        return Arrays.asList(ao.find(Device.class));
    }

    @Override
    public Device sortOutDevice(int id, Date sortedOutDate, String sortedOutComment) {
        sortedOutComment = escapeHtml4(sortedOutComment);
        Device[] toSortOut = ao.find(Device.class, Query.select().where("ID = ?", id));
        if (toSortOut.length != 1)
            return null;

        toSortOut[0].setSortedOutDate(sortedOutDate);
        toSortOut[0].setSortedOutComment(sortedOutComment);
        toSortOut[0].save();

        return toSortOut[0];
    }

    @Override
    public List<Device> getCurrentlyAvailableDevices(int hardwareId) {
        List<Device> allDevices = Arrays.asList(ao.find(Device.class, Query.select()
                .where("HARDWARE_MODEL_ID = ?", hardwareId)));

        // Inner Join won't work on new hardware (no lending at the beginning)
        List<Device> wantedDevices = new ArrayList<Device>();
        for (Device device : allDevices) {
            if (device.getLendings().length == 0) {
                wantedDevices.add(device);
            }
        }

        wantedDevices.addAll(Arrays.asList(ao.find(Device.class, Query.select()
                .alias(Lending.class, "lending")
                .alias(Device.class, "device")
                .join(Lending.class, "lending.DEVICE_ID = device.ID")
                .where("lending.END IS NULL AND device.SORTED_OUT_DATE IS NULL AND device.HARDWARE_MODEL_ID = ?", hardwareId))));

        return allDevices;
    }

    @Override
    public List<Device> getSortedOutDevices() {
        return Arrays.asList(ao.find(Device.class, Query.select().where("SORTED_OUT_DATE IS NOT NULL")));
    }

    @Override
    public List<Device> getSortedOutDevicesOfHardware(int hardwareId) {
        return Arrays.asList(ao.find(Device.class, Query.select().where("SORTED_OUT_DATE IS NOT NULL AND HARDWARE_MODEL_ID = ?", hardwareId)));
    }

    private boolean isSerialNumberUnique(String serialNumber) {
        if (serialNumber == null || serialNumber.equals(""))
            return true;

        return ao.find(Device.class, Query.select().where("SERIAL_NUMBER = ?", serialNumber)).length == 0;
    }

    private boolean isImeiUnique(String imei) {
        if (imei == null || imei.equals(""))
            return true;

        return ao.find(Device.class, Query.select().where("IMEI = ?", imei)).length == 0;
    }

    private boolean isInventoryNumberUnique(String inventoryNumber) {
        if (inventoryNumber == null || inventoryNumber.equals(""))
            return true;

        return ao.find(Device.class, Query.select().where("INVENTORY_NUMBER = ?", inventoryNumber)).length == 0;
    }
}
