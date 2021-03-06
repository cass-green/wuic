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


package com.github.wuic.util;

import com.github.wuic.ClassPathResourceResolver;
import com.github.wuic.nut.ConvertibleNut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>
 * Utility class for URLs management around {@link ConvertibleNut nuts}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.0
 */
public final class UrlUtils {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

    /**
     * <p>
     * This factory builds default {@link UrlProvider}.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.5.0
     */
    public static final class DefaultUrlProviderFactory implements UrlProviderFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public UrlProvider create(final String workflowId) {
            return new UrlUtils.DefaultUrlProvider(workflowId);
        }
    }

    /**
     * <p>
     * Provides the default mechanism to build URL.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.5.0
     */
    private static final class DefaultUrlProvider implements UrlProvider {

        /**
         * The workflow ID and base path.
         */
        private String workflowContextPath;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param wcp the workflow ID and base path
         */
        private DefaultUrlProvider(final String wcp) {
            workflowContextPath = wcp;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getUrl(final ConvertibleNut nut) {
            if (nut.getProxyUri() != null) {
                return nut.getProxyUri();
            } else if (nut.getName().startsWith("http://")) {
                return nut.getName();
            } else {
                return IOUtils.mergePath(workflowContextPath, String.valueOf(NutUtils.getVersionNumber(nut)), nut.getName());
            }
        }
    }

    /**
     * <p>
     * Creates a default {@link UrlProviderFactory}.
     * </p>
     *
     * @return the {@link com.github.wuic.util.UrlUtils.DefaultUrlProviderFactory}
     */
    public static UrlProviderFactory urlProviderFactory() {
        return new DefaultUrlProviderFactory();
    }

    /**
     * <p>
     * Builds a new {@link UrlMatcher}.
     * </p>
     *
     * @param requestUrl the URL to match
     * @return the matcher
     */
    public static UrlMatcher urlMatcher(final String requestUrl) {
        return new UrlMatcher(requestUrl);
    }

    /**
     * <p>
     * Detects the given files in the {@link com.github.wuic.ClassPathResourceResolver} specified in parameter and
     * install them thanks to the given callback.
     * </p>
     *
     * @param classpathResourceResolver the resolver
     * @param files file to test
     * @param callback the callback that install the file
     */
    public static void detectInClassesLocation(final ClassPathResourceResolver classpathResourceResolver,
                                               final Consumer<URL> callback,
                                               final String ... files) {
        for (final String file : files) {
            try {
                final URL classesPath = classpathResourceResolver.getResource(file);

                if (classesPath != null) {
                    LOG.info("Installing '{}' located in {}", file, classesPath.toString());
                    callback.apply(classesPath);
                }
            } catch (MalformedURLException mue) {
                LOG.error("Unexpected exception", mue);
            }
        }
    }

    /**
     * <p>
     * Private constructor for utility class.
     * </p>
     */
    private UrlUtils() {

    }
}
