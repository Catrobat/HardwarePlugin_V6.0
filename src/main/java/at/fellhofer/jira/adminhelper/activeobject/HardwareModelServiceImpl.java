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
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class HardwareModelServiceImpl implements HardwareModelService {

    private final ActiveObjects ao;
    private final TypeOfDeviceService typeOfDeviceService;
    private final ProducerService producerService;
    private final OperatingSystemService operatingSystemService;

    public HardwareModelServiceImpl(ActiveObjects ao, TypeOfDeviceService typeOfDeviceService, ProducerService producerService, OperatingSystemService operatingSystemService) {
        this.ao = checkNotNull(ao);
        this.typeOfDeviceService = checkNotNull(typeOfDeviceService);
        this.producerService = checkNotNull(producerService);
        this.operatingSystemService = checkNotNull(operatingSystemService);
    }

    @Override
    public HardwareModel add(String name, String typeOfDeviceName, String version, String price, String producerName, String operationSystemName, String articleNumber) {
        name = escapeHtml4(name);
        typeOfDeviceName = escapeHtml4(typeOfDeviceName);
        version = escapeHtml4(version);
        price = escapeHtml4(price);
        producerName = escapeHtml4(producerName);
        operationSystemName = escapeHtml4(operationSystemName);
        articleNumber = escapeHtml4(articleNumber);

        TypeOfDevice typeOfDevice = typeOfDeviceService.getOrCreateTypeOfDevice(typeOfDeviceName);
        Producer producer = producerService.getOrCreateProducer(producerName);
        OperatingSystem operatingSystem = operatingSystemService.getOrCreateOperatingSystem(operationSystemName);

        final HardwareModel hardwareModel = ao.create(HardwareModel.class);
        hardwareModel.setName(name);
        hardwareModel.setTypeOfDevice(typeOfDevice);
        hardwareModel.setVersion(version);
        hardwareModel.setPrice(price);
        hardwareModel.setProducer(producer);
        hardwareModel.setOperatingSystem(operatingSystem);
        hardwareModel.setArticleNumber(articleNumber);
        hardwareModel.save();
        return hardwareModel;
    }

    @Override
    public HardwareModel add(String name, TypeOfDevice typeOfDevice, String version, String price, Producer producer, OperatingSystem operationSystem, String articleNumber) {
        name = escapeHtml4(name);
        version = escapeHtml4(version);
        price = escapeHtml4(price);
        articleNumber = escapeHtml4(articleNumber);

        final HardwareModel hardwareModel = ao.create(HardwareModel.class);
        hardwareModel.setName(name);
        hardwareModel.setTypeOfDevice(typeOfDevice);
        hardwareModel.setVersion(version);
        hardwareModel.setPrice(price);
        hardwareModel.setProducer(producer);
        hardwareModel.setOperatingSystem(operationSystem);
        hardwareModel.setArticleNumber(articleNumber);
        hardwareModel.save();
        return hardwareModel;
    }

    @Override
    public List<HardwareModel> all() {
        return Arrays.asList(ao.find(HardwareModel.class));
    }
}
