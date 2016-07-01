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


package com.github.wuic.nut;

import com.github.wuic.NutType;
import com.github.wuic.exception.WuicException;
import com.github.wuic.util.NutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * <p>
 * Base implementation of the {@link Nut} interface. A Nut is often represented by a name and a
 * {@link com.github.wuic.NutType}. This class already manages it.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.3.0
 */
public abstract class AbstractNut implements Nut, Serializable {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The path type.
     */
    private NutType nutType;

    /**
     * The original name.
     */
    private String originalName;

    /**
     * The proxy URI.
     */
    private String proxyUri;

    /**
     * The nut's version number.
     */
    private Future<Long> versionNumber;

    /**
     * <p>
     * Creates a new instance by copying the given {@link Nut}.
     * </p>
     *
     * @param o the nut to copy
     */
    protected AbstractNut(final Nut o) {
        this(o.getInitialName(), o.getInitialNutType(),  o.getVersionNumber());
    }

    /**
     * <p>
     * Creates a new instance. The nut is actually an original nut because constructor provides a version number of this
     * nut without any original nuts.
     * </p>
     *
     * @param name the nut's name
     * @param ft the nut's type
     * @param v version number
     */
    protected AbstractNut(final String name,
                          final NutType ft,
                          final Future<Long> v) {
        if (ft == null) {
            WuicException.throwBadArgumentException(new IllegalArgumentException("You can't create a nut with a null NutType"));
        } else if (name == null) {
            WuicException.throwBadArgumentException(new IllegalArgumentException("You can't create a nut with a null name"));
        } else {
            nutType = ft;
            originalName = name;
            versionNumber = v;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitialName() {
        return originalName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NutType getInitialNutType() {
        return nutType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProxyUri() {
        return proxyUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProxyUri(final String uri) {
        this.proxyUri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Long> getVersionNumber() {
        return versionNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentFile() {
        // Should be overridden by subclass if they could
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic() {
        // Should be overridden by subclass if content is not dynamic
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String retval = getClass().getSimpleName() + "[" + getInitialName() + "]";
        return logger.isInfoEnabled() ? (retval + " - v" + NutUtils.getVersionNumber(this)) : retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof AbstractNut) {
            return ((AbstractNut) other).getInitialName().equals(getInitialName());
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getInitialName().hashCode();
    }

    /**
     * <p>
     * Sets internal version number.
     * </p>
     *
     * @param vn the new version
     */
    protected final void setVersionNumber(final Future<Long> vn) {
        versionNumber = vn;
    }
}
