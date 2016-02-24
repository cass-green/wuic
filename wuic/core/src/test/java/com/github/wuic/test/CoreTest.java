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


package com.github.wuic.test;

import com.github.wuic.ProcessContext;
import com.github.wuic.WuicTask;
import com.github.wuic.context.Context;
import com.github.wuic.context.ContextBuilder;
import com.github.wuic.WuicFacade;
import com.github.wuic.WuicFacadeBuilder;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.nut.Nut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.wuic.util.IOUtils;
import com.github.wuic.util.UrlUtils;
import com.github.wuic.xml.FileXmlContextBuilderConfigurator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Core tests.
 * </p>
 * 
 * @author Guillaume DROUET
 * @since 0.1.0
 */
@RunWith(JUnit4.class)
public class CoreTest extends WuicTest {

    /**
     * Temporary.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Test javascript compression.
     *
     * @throws Exception if test fails
     */
    @Test(timeout = 60000)
    public void javascriptTest() throws Exception {
        Long startTime = System.currentTimeMillis();
        final WuicFacade facade = new WuicFacadeBuilder().noDefaultContextBuilderConfigurator().build();
        Long loadTime = System.currentTimeMillis() - startTime;
        log.info(String.valueOf(((float) loadTime / 1000)));

        startTime = System.currentTimeMillis();
        List<ConvertibleNut> group = facade.runWorkflow("util-js", ProcessContext.DEFAULT);
        loadTime = System.currentTimeMillis() - startTime;
        log.info(String.valueOf(((float) loadTime / 1000)));

        Assert.assertFalse(group.isEmpty());
        InputStream is;

        for (Nut res : group) {
            is = res.openStream();
            Assert.assertTrue(IOUtils.readString(new InputStreamReader(is)).length() > 0);
            is.close();
        }

        startTime = System.currentTimeMillis();
        group = facade.runWorkflow("util-js", ProcessContext.DEFAULT);
        loadTime = System.currentTimeMillis() - startTime;
        log.info(String.valueOf(((float) loadTime / 1000)));

        Assert.assertFalse(group.isEmpty());
        int i = 0;

        for (Nut res : group) {
            is = res.openStream();
            Assert.assertTrue(IOUtils.readString(new InputStreamReader(is)).length() > 0);
            is.close();
            writeToDisk(res, i++ + "test.js");
        }
    }

    /**
     * CSS compression test.
     * 
     * @throws Exception in I/O error case
     */
    @Test(timeout = 60000)
    public void cssTest() throws Exception {
        // TODO : WUIC currently supports only one configuration per NutType. To be fixed in the future !
        Long startTime = System.currentTimeMillis();
        final WuicFacade facade = new WuicFacadeBuilder().build();
        Long loadTime = System.currentTimeMillis() - startTime;
        log.info(String.valueOf(((float) loadTime / 1000)));
        InputStream is;
        final Nut nut = facade.runWorkflow("css-image", "aggregate.css", ProcessContext.DEFAULT);
        is = nut.openStream();
        Assert.assertTrue(IOUtils.readString(new InputStreamReader(is)).length() > 0);
        is.close();
        writeToDisk(nut, "sprite.css");

        List<ConvertibleNut> group = facade.runWorkflow("css-scripts", ProcessContext.DEFAULT);
        int i = 0;

        for (Nut res : group) {
            is = res.openStream();
            Assert.assertTrue(IOUtils.readString(new InputStreamReader(is)).length() > 0);
            is.close();
            writeToDisk(res, i++ + "css-script.css");
        }
    }

    /**
     * Javascript sprite test.
     *
     * @throws Exception if test fails
     */
    @Test(timeout = 60000)
    public void jsSpriteTest() throws Exception {
        Long startTime = System.currentTimeMillis();
        final ContextBuilder builder = new ContextBuilder().configureDefault();
        new FileXmlContextBuilderConfigurator(getClass().getResource("/wuic.xml")).configure(builder);
        final Context facade = builder.build();
        Long loadTime = System.currentTimeMillis() - startTime;
        log.info(String.valueOf(((float) loadTime / 1000)));
        List<ConvertibleNut> group = facade.process("", "js-image", UrlUtils.urlProviderFactory(), ProcessContext.DEFAULT);

        Assert.assertEquals(1, group.size());
        Assert.assertEquals(1, group.get(0).getReferencedNuts().size());

        final Iterator<ConvertibleNut> it = Arrays.asList(group.get(0)).iterator();
        int i = 0;

        while (it.hasNext()) {
            InputStream fis = null;

            try {
                final String name = i++ + "sprite";
                Nut next = it.next();
                writeToDisk(next, name + ".js");

                fis = next.openStream();
                final File file = temporaryFolder.newFile(name + ".js");
                IOUtils.copyStream(fis, new FileOutputStream(file));
                final String content = IOUtils.readString(new InputStreamReader(new FileInputStream(file)));
                log.info(content);
                final int start = content.indexOf("url : \"") + 8;
                final int end = content.indexOf("/aggregate.png");
                String imageGroup = content.substring(start, end);
                imageGroup = imageGroup.substring(0, imageGroup.lastIndexOf('/'));
                group = facade.process("", imageGroup, UrlUtils.urlProviderFactory(), null);

                writeToDisk(group.get(0).getReferencedNuts().get(0), "aggregate.png");
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }

    /**
     * <p>
     * Task test.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test(timeout = 60000)
    public void taskTest() throws Exception {
        final String wuicXml = IOUtils.normalizePathSeparator(getClass().getResource("/wuic.xml").toString());
        final String currentDir = IOUtils.normalizePathSeparator(new File(".").toURI().toURL().toString());
        final String relative =  wuicXml.substring(currentDir.length() - 2);
        final File out = new File(System.getProperty("java.io.tmpdir"), "wuic-static-test");

        // Create MOJO
        final WuicTask task = new WuicTask();
        task.setXml(relative);
        task.setOutput(new File(out, "generated").toString());
        task.setRelocateTransformedXmlTo(out.toString());

        // Invoke
        task.execute();

        // Verify
        final File parent = new File(System.getProperty("java.io.tmpdir"), "wuic-static-test/generated/");
        Assert.assertTrue(new File(parent, "util-js").listFiles()[0].list()[0].equals("aggregate.js"));

        Boolean found = Boolean.FALSE;
        File[] files = new File(parent, "css-scripts").listFiles();

        for (int i = 0; i < files.length && !found; found = files[i++].list()[0].equals("aggregate.css"));
        Assert.assertTrue(found);

        // Assert resources are closed
        TestHelper.delete(out);
    }
}
