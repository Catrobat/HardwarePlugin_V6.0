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

package ut.at.fellhofer.jira.adminhelper.activeobject;

import at.fellhofer.jira.adminhelper.activeobject.*;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.junit.Assert.*;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(AdminHelperDatabaseUpdater.class)
public class HardwareModelServiceImplTest {

    private EntityManager entityManager;
    private ActiveObjects ao;
    private HardwareModelServiceImpl hardwareModelService;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        hardwareModelService = new HardwareModelServiceImpl(ao, new TypeOfDeviceServiceImpl(ao), new ProducerServiceImpl(ao), new OperatingSystemServiceImpl(ao));
    }

    @Test
    public void testAdd() throws Exception {
        final String name = AdminHelperDatabaseUpdater.HARDWARE_NAME_1 + "#1";
        final String type = AdminHelperDatabaseUpdater.TYPE_OF_DEVICE_1 + "#1";
        final String model = AdminHelperDatabaseUpdater.HARDWARE_VERSION_1 + "#1";
        final String price = AdminHelperDatabaseUpdater.HARDWARE_PRICE_1 + "#1";
        final String producer = AdminHelperDatabaseUpdater.PRODUCER_1 + "#1";
        final String operatingSystem = AdminHelperDatabaseUpdater.OPERATING_SYSTEM_1 + "#1";
        final String articleNumber = AdminHelperDatabaseUpdater.HARDWARE_ARTICLE_NUMBER_1 + "#1";
        assertEquals(4, ao.find(HardwareModel.class).length);

        final HardwareModel add = hardwareModelService.add(name, type, model, price, producer, operatingSystem, articleNumber);
        assertFalse(add.getID() == 0);

        ao.flushAll();

        final HardwareModel[] hardwareModels = ao.find(HardwareModel.class);
        assertEquals(5, hardwareModels.length);
        assertEquals(AdminHelperDatabaseUpdater.HARDWARE_NAME_1, hardwareModels[0].getName());
        assertEquals(AdminHelperDatabaseUpdater.TYPE_OF_DEVICE_1, hardwareModels[0].getTypeOfDevice().getTypeOfDeviceName());
        assertEquals(AdminHelperDatabaseUpdater.HARDWARE_VERSION_1, hardwareModels[0].getVersion());
        assertEquals(AdminHelperDatabaseUpdater.HARDWARE_PRICE_1, hardwareModels[0].getPrice());
        assertEquals(AdminHelperDatabaseUpdater.PRODUCER_1, hardwareModels[0].getProducer().getProducerName());
        assertEquals(AdminHelperDatabaseUpdater.OPERATING_SYSTEM_1, hardwareModels[0].getOperatingSystem().getOperatingSystemName());
        assertEquals(AdminHelperDatabaseUpdater.HARDWARE_ARTICLE_NUMBER_1, hardwareModels[0].getArticleNumber());

        assertEquals(name, hardwareModels[4].getName());
        assertEquals(type, hardwareModels[4].getTypeOfDevice().getTypeOfDeviceName());
        assertEquals(model, hardwareModels[4].getVersion());
        assertEquals(escapeHtml4(price), hardwareModels[4].getPrice());
        assertEquals(producer, hardwareModels[4].getProducer().getProducerName());
        assertEquals(operatingSystem, hardwareModels[4].getOperatingSystem().getOperatingSystemName());
        assertEquals(articleNumber, hardwareModels[4].getArticleNumber());
    }

    @Test
    public void testAll() throws Exception {
        assertEquals(4, hardwareModelService.all().size());

        final HardwareModel hardwareModel = ao.create(HardwareModel.class);
        hardwareModel.setName("Some hardware model");
        hardwareModel.save();

        ao.flushAll();

        final List<HardwareModel> all = hardwareModelService.all();
        assertEquals(5, all.size());
        assertEquals(hardwareModel.getID(), all.get(4).getID());
    }
}