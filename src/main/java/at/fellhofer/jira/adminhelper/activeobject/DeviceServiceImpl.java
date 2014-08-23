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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeviceServiceImpl implements DeviceService {

    private final ActiveObjects ao;

    public DeviceServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Device add(HardwareModel hardwareModel, String imei, String serialNumber, String inventoryNumber, String receivedBy, Date receivedDate) {
        Device device = ao.create(Device.class);
        device.setHardwareModel(hardwareModel);
        device.setImei(imei);
        device.setSerialNumber(serialNumber);
        device.setInventoryNumber(inventoryNumber);
        device.setReceivedBy(receivedBy);
        device.setReceivedDate(receivedDate);
        device.save();

        return device;
    }

    @Override
    public List<Device> all() {
        return Arrays.asList(ao.find(Device.class));
    }
}
