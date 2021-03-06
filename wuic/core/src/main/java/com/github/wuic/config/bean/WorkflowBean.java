/*
 * Copyright (c) 2016   The authors of WUIC
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.github.wuic.config.bean;

import com.github.wuic.config.bean.json.StringArrayAdapterFactory;
import com.google.gson.annotations.JsonAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * <p>
 * This class corresponds to the bean representation of a {@link com.github.wuic.Workflow}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.3
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowBean {

    /**
     * The workflow ID.
     */
    @XmlAttribute(name = "id")
    private String id;

    /**
     * Comma-separated list of profiles required to apply this bean.
     */
    @XmlAttribute(name = "profiles")
    @JsonAdapter(StringArrayAdapterFactory.class)
    private String profiles;

    /**
     * The workflow prefix for ID.
     */
    @XmlAttribute(name = "id-prefix")
    private String idPrefix;

    /**
     * The heap ID pattern.
     */
    @XmlAttribute(name = "heap-id-pattern")
    private String heapIdPattern;

    /**
     * The workflow ID to copy.
     */
    @XmlAttribute(name = "workflow-template-id")
    private String workflowTemplateId;

    /**
     * <p>
     * Gets the profiles.
     * </p>
     *
     * @return the profiles
     */
    public String[] getProfiles() {
        return profiles != null ? profiles.split(",") : null;
    }

    /**
     * <p>
     * Gets the ID prefix (id attribute should be {@code null}).
     * </p>
     *
     * @return the string prefixing the workflow ID
     */
    public String getIdPrefix() {
        return idPrefix;
    }

    /**
     * <p>
     * Sets the prefix ID.
     * </p>
     *
     * @param prefix the prefix ID
     */
    public void setIdPrefix(final String prefix) {
        idPrefix = prefix;
    }

    /**
     * <p>
     * Gets the ID (idPrefix attribute should be {@code null}).
     * </p>
     *
     * @return the ID identifying the workflow
     */
    public String getId() {
        return id;
    }

    /**
     * <p>
     * Gets the heap ID.
     * </p>
     *
     * @return the heap ID
     */
    public String getHeapIdPattern() {
        return heapIdPattern;
    }

    /**
     * <p>
     * Sets the heap ID.
     * </p>
     *
     * @param pattern the heap ID pattern
     */
    public void setHeapIdPattern(final String pattern) {
        heapIdPattern = pattern;
    }

    /**
     * <p>
     * Sets the workflow template ID.
     * </p>
     *
     * @param tplId the workflow template ID
     */
    public void setWorkflowTemplateId(final String tplId) {
        workflowTemplateId = tplId;
    }

    /**
     * <p>
     * Gets the workflow ID.
     * </p>
     *
     * @return the workflow ID
     */
    public String getWorkflowTemplateId() {
        return workflowTemplateId;
    }
}
