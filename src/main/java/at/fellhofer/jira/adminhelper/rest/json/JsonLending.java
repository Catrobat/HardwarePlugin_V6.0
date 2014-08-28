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

import at.fellhofer.jira.adminhelper.activeobject.Lending;
import com.atlassian.jira.user.util.UserManager;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@XmlRootElement
public class JsonLending {

    @XmlElement
    private int ID;

    @XmlElement
    private JsonDevice device;

    @XmlElement
    private String lentOutFrom;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date begin;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date end;

    @XmlElement
    private String purpose;

    @XmlElement
    private String comment;

    public JsonLending() {

    }

    public JsonLending(Lending toCopy, UserManager userManager) {
        ID = toCopy.getID();
        device = new JsonDevice(toCopy.getDevice());
        lentOutFrom = userManager.getUserByKey(toCopy.getLendingUserKey()).getDisplayName();
        begin = toCopy.getBegin();
        end = toCopy.getEnd();
        purpose = toCopy.getPurpose();
        comment = toCopy.getComment();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public JsonDevice getDevice() {
        return device;
    }

    public void setDevice(JsonDevice device) {
        this.device = device;
    }

    public String getLentOutFrom() {
        return lentOutFrom;
    }

    public void setLentOutFrom(String lentOutFrom) {
        this.lentOutFrom = lentOutFrom;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
