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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class LendingServiceImpl implements LendingService {

    private final ActiveObjects ao;

    public LendingServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Lending add(Device device, String lendingUserKey, String purpose, String comment, Date begin) {
        Lending lending = ao.create(Lending.class);
        lending.setDevice(device);
        lending.setLendingUserKey(lendingUserKey);
        lending.setPurpose(purpose);
        lending.setComment(comment);
        lending.setBegin(begin);
        lending.save();

        return lending;
    }

    @Override
    public void bringBack(Lending lending, String purpose, String comment, Date end) {
        lending.setPurpose(purpose);
        lending.setComment(comment);
        lending.setEnd(end);
        lending.save();
    }

    @Override
    public List<Lending> currentlyLentOut() {
        return Arrays.asList(ao.find(Lending.class, Query.select().where("END IS NULL")));
    }

    @Override
    public List<Lending> currentlyLentOutDevices(HardwareModel hardwareModel) {
        return Arrays.asList(ao.find(Lending.class, Query.select()
                .alias(Lending.class, "lending")
                .alias(Device.class, "device")
                .join(Device.class, "lending.DEVICE_ID = device.ID")
                .where("lending.END IS NULL AND device.HARDWARE_MODEL_ID = ?", hardwareModel.getID())));
    }

    @Override
    public List<Lending> all() {
        return Arrays.asList(ao.find(Lending.class));
    }
}
