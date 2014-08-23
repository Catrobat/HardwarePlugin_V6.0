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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class TypeOfDeviceServiceImpl implements TypeOfDeviceService {

    private final ActiveObjects ao;

    public TypeOfDeviceServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public TypeOfDevice getTypeOfDevice(String name) {
        name = escapeHtml4(name);

        TypeOfDevice[] typeOfDevices = ao.find(TypeOfDevice.class, Query.select().where("TYPE_OF_DEVICE_NAME = ?", name));
        if (typeOfDevices.length == 0) {
            return null;
        } else if (typeOfDevices.length == 1) {
            return typeOfDevices[0];
        }

        throw new RuntimeException("Should be unique - this should never happen!");
    }

    @Override
    public TypeOfDevice getOrCreateTypeOfDevice(String name) {
        name = escapeHtml4(name);

        TypeOfDevice typeOfDevice = getTypeOfDevice(name);
        if(typeOfDevice != null) {
            return typeOfDevice;
        }

        final TypeOfDevice createdTypeOfDevice = ao.create(TypeOfDevice.class);
        createdTypeOfDevice.setTypeOfDeviceName(name);
        createdTypeOfDevice.save();
        return createdTypeOfDevice;
    }
}
