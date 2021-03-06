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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Represents a property to be set in a {@link com.github.wuic.config.ObjectBuilder}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.0
 */
public class PropertyBean {

    /**
     * The key.
     */
    @XmlAttribute(name = "key")
    private String key;

    /**
     * The value
     */
    @XmlValue
    private String value;

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public PropertyBean() {

    }


    /**
     * <p>
     * Constructor based on a given key and a give, associated value.
     * </p>
     *
     * @param key the key
     * @param value the value
     */
    public PropertyBean(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * <p>
     *  Gets the key.
     * </p>
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * <p>
     * Gets the value.
     * </p>
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
