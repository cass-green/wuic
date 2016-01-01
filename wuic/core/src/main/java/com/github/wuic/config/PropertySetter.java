/*
 * "Copyright (c) 2016   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * -   The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Any failure to comply with the above shall automatically terminate the license
 * and be construed as a breach of these Terms of Use causing significant harm to
 * Capgemini.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Capgemini shall not be used in
 * advertising or otherwise to promote the use or other dealings in this Software
 * without prior written authorization from Capgemini.
 *
 * These Terms of Use are subject to French law.
 *
 * IMPORTANT NOTICE: The WUIC software implements software components governed by
 * open source software licenses (BSD and Apache) of which CAPGEMINI is not the
 * author or the editor. The rights granted on the said software components are
 * governed by the specific terms and conditions specified by Apache 2.0 and BSD
 * licenses."
 */


package com.github.wuic.config;

import com.github.wuic.exception.WuicException;
import com.github.wuic.util.NumberUtils;

/**
 * <p>
 * A property setter is an object which is able to set to a particular {@link com.github.wuic.nut.dao.NutDao} builder a property.
 * This property is associated to a key specifically supported by the type of setter.
 * </p>
 *
 * <p>
 * The property setter is generic to allow subclasses to define the real type of value associated to its supported property
 * key.
 * </p>
 *
 * <p>
 * Fields can be initialized with {@link #init(ObjectBuilder, String, Object)}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.1
 * @since 0.4.0
 * @param <T> the type of property value
 */
public abstract class PropertySetter<T> {

    /**
     * The builder.
     */
    private ObjectBuilder<?> builder;

    /**
     * The property key.
     */
    private String propertyKey;

    /**
     * <p>
     * Initializes this instance.
     * </p>
     *
     * @param b the {@link com.github.wuic.config.AbstractObjectBuilder} which needs to be configured
     * @param defaultValue the default value
     */
    public void init(final ObjectBuilder<?> b, final String propertyKey, final Object defaultValue) {
        builder = b;
        this.propertyKey = propertyKey;
        set(defaultValue);
    }

    /**
     * <p>
     * Puts internally the given value into the builder's properties.
     * </p>
     *
     * @param key the property key
     * @param value the property value
     */
    protected final void put(final String key, final Object value) {
        builder.getProperties().put(key, value);
    }

    /**
     * <p>
     * Sets the given property value only if its key is supported by this setter.
     * </p>
     *
     * @param key the key
     * @param value the property value
     * @return {@code true} if the setter supports the key and has put the value, {@code false} otherwise
     */
    public Boolean setProperty(final String key, final Object value) {
        if (getPropertyKey().equals(key)) {
            set(value);
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * <p>
     * Gets the value from the builder associated to the supported key.
     * </p>
     *
     * @return the property value, {@code null} if the property value has not been set to the builder
     */
    @SuppressWarnings("unchecked")
    public T get() {
        final Object retval = builder.getProperties().get(getPropertyKey());
        return retval == null ? null : (T) retval;
    }

    /**
     * <p>
     * Sets the given value by associating it to the supported key and by trying to convert it into the 'T' type.
     * </p>
     *
     * @param value the value
     */
    protected abstract void set(Object value);

    /**
     * <p>
     * Gets the property key supported by this setter.
     * </p>
     *
     * @return the property key
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * <p>
     * Abstract builder for {@code String} values.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.0
     */
    public static class PropertySetterOfString extends PropertySetter<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void set(final Object value) {
            put(getPropertyKey(), value == null ? null : String.valueOf(value));
        }

        /**
         * <p>
         * Builds a new instance. Should be initialized with {@link #init(ObjectBuilder, String, Object)}.
         * </p>
         */
        public PropertySetterOfString() {
        }
    }

    /**
     * <p>
     * Abstract builder for {@code Integer} values.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.0
     */
    public static class PropertySetterOfInteger extends PropertySetter<Integer> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void set(final Object value) {

            if (value == null) {
                put(getPropertyKey(), null);
            } else {
                final String str = String.valueOf(value);

                if (NumberUtils.isNumber(str)) {
                    put(getPropertyKey(), Integer.parseInt(str));
                } else {
                    WuicException.throwBadArgumentException(
                            new NumberFormatException(String.format("Key '%s' must be an Integer", getPropertyKey())));
                }
            }
        }
    }

    /**
     * <p>
     * Abstract builder for {@code Boolean} values.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.0
     */
    public static class PropertySetterOfBoolean extends PropertySetter<Boolean> {

        /**
         * <p>
         * Builds a new instance. Should be initialized with {@link #init(ObjectBuilder, String, Object)}.
         * </p>
         */
        public PropertySetterOfBoolean() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void set(final Object value) {
            put(getPropertyKey(), value == null ? null : Boolean.valueOf(value.toString()));
        }
    }

    /**
     * <p>
     * Abstract builder for {@code Object} values instantiated with the default constructor of a class loaded with its
     * name.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.0
     */
    public static class PropertySetterOfObject extends PropertySetter<Object> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void set(final Object value) {
            try {
                put(getPropertyKey(), value == null ? null : Class.forName(value.toString()).newInstance());
            } catch (ClassNotFoundException cnfe) {
                WuicException.throwBadStateException(cnfe);
            } catch (IllegalAccessException iae) {
                WuicException.throwBadStateException(iae);
            } catch (InstantiationException ie) {
                WuicException.throwBadStateException(ie);
            }
        }
    }
}
