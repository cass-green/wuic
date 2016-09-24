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


package com.github.wuic.context;

import com.github.wuic.ProcessContext;
import com.github.wuic.Profile;
import com.github.wuic.util.PollingScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A configurator configures {@link ContextBuilder}. It's is called by a WUIC bootstrap in charge of {@link Context}
 * creation using a {@link ContextBuilder}.
 * </p>
 *
 * <p>
 * This class is abstract and should be extended to configure in a specific way the {@link ContextBuilder}.
 * </p>
 *
 * <p>
 * If the subclass is annotated with {@link com.github.wuic.Profile}, the tags are automatically applied with the declared
 * values.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.0
 */
public abstract class ContextBuilderConfigurator extends PollingScheduler<ContextBuilderConfigurator> {

    /**
     * Sets of tags representing the configurator to ignore when {@link ContextBuilderConfigurator#configure(ContextBuilder)}
     * is invoked.
     */
    private static final Set<String> IGNORE_TAGS = new HashSet<String>();

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Tracks {@link ContextBuilder} to update when polling.
     */
    private ContextBuilder pollingContextBuilder;

    /**
     * Last update timestamp.
     */
    private Long timestamp;

    /**
     * Only one configuration with the configurator tag could occurs if this flag is {@code true}.
     */
    private Boolean multipleConfigurations;

    /**
     * The profiles declared in {@link com.github.wuic.Profile} annotation.
     */
    private String[] profiles;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     */
    protected ContextBuilderConfigurator() {
        this(Boolean.TRUE);
    }

    /**
     * <p>
     * Builds a configurator with a flag indicating to configure or not anything if a configuration with its tag has
     * been done before.
     * </p>
     *
     * @param multiple {@code true} if multiple configurations with the same tag could be executed, {@code false} otherwise
     */
    protected ContextBuilderConfigurator(final Boolean multiple) {
        multipleConfigurations = multiple;

        if (getClass().isAnnotationPresent(Profile.class)) {
            profiles = getClass().getAnnotation(Profile.class).value();
        }
    }

    /**
     * <p>
     * Sets the flag allowing or not multiple configurations.
     * </p>
     *
     * @param flag the new value
     */
    public void setMultipleConfigurations(final Boolean flag) {
        multipleConfigurations = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        if (pollingContextBuilder != null) {
            try {
                final Long ts = getLastUpdateTimestampFor(getClass().getName());

                // Configuration has been updated so we reset current settings and refresh it
                if (!ts.equals(timestamp)) {
                    timestamp = ts;
                    log.info("Updating configuration for {}", getClass().getName());
                    pollingContextBuilder.clearTag(getTag());
                    configure(pollingContextBuilder);
                }
            } catch (IOException se) {
                log.info("Unable to poll configuration", se);
            }
        } else {
            log.warn("Polling interval is set to {} seconds but no context builder is polled", getPollingInterval());
        }
    }

    /**
     * <p>
     * Tags the given {@link ContextBuilder} with the value returned by {@link #getTag()} and any profiles declared
     * in the {@link Profile} annotation of this class.
     * </p>
     *
     * @param ctxBuilder the builder
     */
    public final void tag(final ContextBuilder ctxBuilder) {
        if (profiles == null) {
            ctxBuilder.tag(getTag());
        } else {
            // profiles restriction
            ctxBuilder.tag(getTag(), profiles);
        }
    }

    /**
     * <p>
     * Configures the given context. It tags all the settings with the value returned by
     * the {@link ContextBuilderConfigurator#getTag()} method.
     * </p>
     *
     * <p>
     * All settings associated to each configurator tag are cleared before (re)configuration.
     * </p>
     *
     * @param ctxBuilder the builder
     * @throws IOException if I/O error occurs when start polling
     */
    public void configure(final ContextBuilder ctxBuilder) throws IOException {
        // Do not run multiple times configurators with the same tags
        if (!multipleConfigurations && !IGNORE_TAGS.add(getTag())) {
            log.info("Configuration with tag {} has been already performed, ignoring", getTag());
            return;
        }

        try {
            ctxBuilder.clearTag(getTag());
            tag(ctxBuilder);

            ctxBuilder.processContext(getProcessContext());

            // Update polling
            final int polling = internalConfigure(ctxBuilder);
            pollingContextBuilder = polling > 0 ? ctxBuilder : null;
            setPollingInterval(polling);

            if (polling != -1) {
                timestamp = getLastUpdateTimestampFor(getClass().getName());
            }

            // Add this instance as an observer to be notified when polling
            observe(getClass().getName(), this);
        } finally {
            ctxBuilder.releaseTag();
        }
    }

    /**
     * <p>
     * Configures the given context internally. This method is called just after the {@link ContextBuilder} has been
     * tagged with the value returned by {@link ContextBuilderConfigurator#getTag()} method. Once the
     * execution of this method is terminated, the tag is released.
     * </p>
     *
     * <p>
     * To activate polling on this configurator, this method should returns a positive integer representing the polling
     * interval in seconds.
     * </p>
     *
     * @param ctxBuilder the builder
     */
    public abstract int internalConfigure(ContextBuilder ctxBuilder);

    /**
     * <p>
     * Gets the tag to use to identify the set of settings defined when {@link ContextBuilderConfigurator#configure(ContextBuilder)}
     * is called.
     * </p>
     *
     * @return the tag
     */
    public abstract String getTag();

    /**
     * <p>
     * Gets the process context associated to the tag while configuring.
     * </p>
     *
     * @return the process context
     */
    public abstract ProcessContext getProcessContext();
}
