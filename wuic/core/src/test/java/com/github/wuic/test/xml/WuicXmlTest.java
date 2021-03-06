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


package com.github.wuic.test.xml;

import com.github.wuic.context.Context;
import com.github.wuic.context.ContextBuilder;
import com.github.wuic.context.ContextBuilderConfigurator;
import com.github.wuic.config.ObjectBuilderFactory;
import com.github.wuic.engine.EngineService;
import com.github.wuic.engine.Engine;
import com.github.wuic.nut.dao.NutDao;
import com.github.wuic.nut.dao.NutDaoService;
import com.github.wuic.nut.filter.NutFilter;
import com.github.wuic.nut.filter.NutFilterService;
import com.github.wuic.test.ProcessContextRule;
import com.github.wuic.util.IOUtils;
import com.github.wuic.util.UrlUtils;
import com.github.wuic.config.bean.xml.FileXmlContextBuilderConfigurator;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Test the wuic.xml support.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.0
 */
@RunWith(JUnit4.class)
public class WuicXmlTest {

    /**
     * Process context.
     */
    @ClassRule
    public static ProcessContextRule processContext = new ProcessContextRule();

    /**
     * Temporary.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Timeout.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(60);

    /**
     * Tests a wuic.xml file referencing default DAO.
     *
     * @throws Exception if test fails
     */
    @Test
    public void withPollingTest() throws Exception {
        // Add custom DAO and engine required
        final ObjectBuilderFactory<Engine> ebf = new ObjectBuilderFactory<Engine>(EngineService.class, MockEngine.class);
        final ObjectBuilderFactory<NutDao> nbf = new ObjectBuilderFactory<NutDao>(NutDaoService.class, MockDao.class);
        final ObjectBuilderFactory<NutFilter> fbf = new ObjectBuilderFactory<NutFilter>(NutFilterService.class);
        final ContextBuilder builder = new ContextBuilder(ebf, nbf, fbf);

        // By default we use this file
        final URL full = getClass().getResource("/wuic-polling.xml");
        final File tmp = temporaryFolder.newFile("wuic.xml");
        IOUtils.copyStream(full.openStream(), new FileOutputStream(tmp));

        // Load configuration
        final ContextBuilderConfigurator cfg = new FileXmlContextBuilderConfigurator(tmp.toURI().toURL());
        cfg.configure(builder);
        Context ctx = builder.build();
        Assert.assertTrue(ctx.isUpToDate());

        // We change the content before polling
        final URL wwdb = getClass().getResource("/wuic-simple.xml");
        IOUtils.copyStream(wwdb.openStream(), new FileOutputStream(tmp));

        final CountDownLatch latch = new CountDownLatch(1);

        builder.addExpirationListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                latch.countDown();
            }
        });

        // Polling is done every seconds
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertFalse(ctx.isUpToDate());

        // Check new context which contains new workflow
        ctx = builder.build();
        Assert.assertTrue(ctx.isUpToDate());
        ctx.process("", "simpleWorkflowsimpleHeap", UrlUtils.urlProviderFactory(), processContext.getProcessContext());

        // Remove test file
        tmp.delete();
    }
}
