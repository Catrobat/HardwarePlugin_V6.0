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

package at.fellhofer.jira.adminhelper.activeobject.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface HardwareModel extends Entity {

    public String getName();

    public void setName(String name);

    public String getTypeOfDevice();

    public void setTypeOfDevice(String typeOfDevice);

    public String getVersion();

    public void setVersion(String version);

    public String getPrice();

    public void setPrice(String price);

    public String getProducer();

    public void setProducer(String producer);

    public String getOperatingSystem();

    public void setOperatingSystem(String operatingSystem);

    public String getArticleNumber();

    public void setArticleNumber(String articleNumber);
}
