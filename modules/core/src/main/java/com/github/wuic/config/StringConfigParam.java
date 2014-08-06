/*
 * "Copyright (c) 2014   Capgemini Technology Services (hereinafter "Capgemini")
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation marks a parameter in a constructor annotated with {@link ConfigConstructor}
 * that sets a string value.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.5
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StringConfigParam {

    /**
     * <p>
     * A {@link ConfigParam} that handles {@link StringConfigParam}.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.5
     */
    public class ConfigParamImpl implements ConfigParam {

        /**
         * The configuration parameter.
         */
        private StringConfigParam configParam;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param configParam the annotation
         */
        public ConfigParamImpl(StringConfigParam configParam) {
            this.configParam = configParam;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends PropertySetter> setter() {
            return configParam.setter();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object defaultValue() {
            return configParam.defaultValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String propertyKey() {
            return configParam.propertyKey();
        }
    }

    /**
     * <p>
     * Gets the property key for this parameter.
     * </p>
     *
     * @return the property key.
     */
    String propertyKey();

    /**
     * <p>
     * Gets the {@link PropertySetter}.
     * </p>
     *
     * @return the setter
     */
    Class<? extends PropertySetter> setter() default PropertySetter.PropertySetterOfString.class;

    /**
     * <p>
     * Gets the default value.
     * </p>
     *
     * @return the value
     */
    String defaultValue();
}
