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

public class OperatingSystemServiceImpl implements OperatingSystemService {

    private final ActiveObjects ao;

    public OperatingSystemServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public OperatingSystem getOperatingSystem(String operatingSystemName) {
        operatingSystemName = escapeHtml4(operatingSystemName);

        OperatingSystem[] operatingSystems = ao.find(OperatingSystem.class, Query.select().where("OPERATING_SYSTEM_NAME = ?", operatingSystemName));
        if (operatingSystems.length == 0) {
            return null;
        } else if (operatingSystems.length == 1) {
            return operatingSystems[0];
        }

        throw new RuntimeException("Should be unique - this should never happen!");
    }

    @Override
    public OperatingSystem getOrCreateOperatingSystem(String operatingSystemName) {
        operatingSystemName = escapeHtml4(operatingSystemName);

        OperatingSystem operatingSystem = getOperatingSystem(operatingSystemName);
        if (operatingSystem != null) {
            return operatingSystem;
        }

        final OperatingSystem createdOperatingSystem = ao.create(OperatingSystem.class);
        createdOperatingSystem.setOperatingSystemName(operatingSystemName);
        createdOperatingSystem.save();
        return createdOperatingSystem;
    }

}
