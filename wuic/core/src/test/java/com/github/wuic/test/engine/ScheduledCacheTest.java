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


package com.github.wuic.test.engine;

import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.core.ScheduledCacheEngine;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * {@link ScheduledCacheEngine} tests.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.0
 */
@RunWith(JUnit4.class)
public class ScheduledCacheTest {

    /**
     * Timeout.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(60);

    /**
     * Checks that scheduled clear works.
     *
     * @throws Exception if test fails
     */
    @Test
    public void scheduledClearCacheTest() throws Exception {
        final CountDownLatch count = new CountDownLatch(2);
        final ScheduledCacheEngine cache = new ScheduledCacheEngine() {

            /**
             * {@inheritDoc}
             */
            @Override protected void clearCache() {
                count.countDown();
            }

            /**
             * {@inheritDoc}
             */
            @Override public void putToCache(EngineRequest.Key request, CacheResult nuts) { }

            /**
             * {@inheritDoc}
             */
            @Override public void removeFromCache(EngineRequest.Key request) { }

            /**
             * {@inheritDoc}
             */
            @Override public CacheResult getFromCache(EngineRequest.Key request) { return null; }
        };
        cache.init(1, true, false);

        count.await(1500, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, count.getCount());
        cache.setTimeToLive(-1);
        count.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(1, count.getCount());
    }
}
