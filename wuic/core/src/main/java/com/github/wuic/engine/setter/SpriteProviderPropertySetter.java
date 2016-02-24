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


package com.github.wuic.engine.setter;

import com.github.wuic.ApplicationConfig;
import com.github.wuic.engine.SpriteProvider;
import com.github.wuic.engine.core.CssSpriteProvider;
import com.github.wuic.engine.core.JavascriptSpriteProvider;
import com.github.wuic.config.PropertySetter;
import com.github.wuic.exception.WuicException;

/**
 * <p>
 * Setter for the {@link com.github.wuic.ApplicationConfig#SPRITE_PROVIDER_CLASS_NAME} property.
 * </p>
 *
 * <p>
 * Uris array is specified as a {@code String} containing each value separated by a '|' character.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.0
 */
public class SpriteProviderPropertySetter extends PropertySetter<SpriteProvider[]> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyKey() {
        return ApplicationConfig.SPRITE_PROVIDER_CLASS_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void set(final Object value) {
        if (value == null) {
            put(getPropertyKey(), value);
        } else if (value instanceof String) {
            final String[] strArray = value.toString().split("\\|");
            final SpriteProvider[] sp = new SpriteProvider[strArray.length];
            int cpt = 0;

            for (final String o : strArray) {
                if (o.equals("css")) {
                    sp[cpt++] = new CssSpriteProvider();
                } else if (o.equals("javascript")) {
                    sp[cpt++] = new JavascriptSpriteProvider();
                } else {
                    WuicException.throwBadArgumentException(new IllegalArgumentException(
                             String.format(
                                     "Key '%s' is associated to a the value '%s' which is not a String which equals to css or javascript",
                                     getPropertyKey(),
                                     value)));
                }
            }

            put(getPropertyKey(), sp);
        } else {
            WuicException.throwBadArgumentException(new IllegalArgumentException(
                    String.format("Value '%s' associated to key %s must be an String", value, getPropertyKey())));
        }
    }
}
