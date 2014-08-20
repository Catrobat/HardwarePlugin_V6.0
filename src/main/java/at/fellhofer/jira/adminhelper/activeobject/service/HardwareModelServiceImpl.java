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

package at.fellhofer.jira.adminhelper.activeobject.service;

import at.fellhofer.jira.adminhelper.activeobject.entity.HardwareModel;
import com.atlassian.activeobjects.external.ActiveObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class HardwareModelServiceImpl implements HardwareModelService {

    private final ActiveObjects ao;

    public HardwareModelServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public HardwareModel add(String name, String typeOfDevice, String version, String price, String producer, String operationSystem, String articleNumber) {
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
