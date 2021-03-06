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


package com.github.wuic.test.engine;

import com.github.wuic.ApplicationConfig;
import com.github.wuic.EnumNutType;
import com.github.wuic.NutTypeFactory;
import com.github.wuic.NutTypeFactoryHolder;
import com.github.wuic.config.ObjectBuilder;
import com.github.wuic.config.ObjectBuilderFactory;
import com.github.wuic.engine.Engine;
import com.github.wuic.engine.EngineRequestBuilder;
import com.github.wuic.engine.EngineService;
import com.github.wuic.engine.NodeEngine;
import com.github.wuic.engine.core.CommandLineConverterEngine;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.test.ProcessContextRule;
import com.github.wuic.test.WuicTest;
import com.github.wuic.util.FutureLong;
import com.github.wuic.util.IOUtils;
import com.github.wuic.util.InMemoryOutput;
import com.github.wuic.util.Output;
import com.github.wuic.util.Pipe;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CommandLineConverterEngine} tests.
 */
@RunWith(JUnit4.class)
public class CommandLineConverterEngineTest {

    /**
     * Process context.
     */
    @ClassRule
    public static ProcessContextRule processContext = new ProcessContextRule();

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Source map sample.
     */
    public static final String SOURCEMAP = "{"
            + "  \"version\": 3,"
            + "  \"file\": \"testcode.js\","
            + "  \"sections\": ["
            + "    {"
            + "      \"map\": {"
            + "         \"version\": 3,"
            + "         \"mappings\": \"AAAAA,QAASA,UAAS,EAAG;\","
            + "         \"sources\": [\"testcode.js\"],"
            + "         \"names\": [\"foo\"]"
            + "      },"
            + "      \"offset\": {"
            + "        \"line\": 1,"
            + "        \"column\": 1"
            + "      }"
            + "    }"
            + "  ]"
            + "}";

    /**
     * Timeout.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(60);

    /**
     * Typescript compilation test.
     *
     * @throws Exception if test fails
     */
    @Test
    public void compileTest() throws Exception {
        final String command = String.format("echo %s > %s | echo %s > %s",
                CommandLineConverterEngine.PATH_TOKEN,
                CommandLineConverterEngine.OUT_PATH_TOKEN,
                SOURCEMAP,
                CommandLineConverterEngine.SOURCE_MAP_TOKEN);
        final File parent = temporaryFolder.newFolder("parent");
        NutsHeap heap = mockHeap(parent);
        final ObjectBuilderFactory<Engine> factory = new ObjectBuilderFactory<Engine>(EngineService.class, CommandLineConverterEngine.class);
        final ObjectBuilder<Engine> builder = factory.create("CommandLineConverterEngineBuilder");
        final Engine engine = builder.property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, command)
                .build();

        NutTypeFactoryHolder.class.cast(engine).setNutTypeFactory(new NutTypeFactory(Charset.defaultCharset().displayName()));
        List<ConvertibleNut> res = engine.parse(new EngineRequestBuilder("wid", heap, null, new NutTypeFactory(Charset.defaultCharset().displayName()))
                .processContext(processContext.getProcessContext()).contextPath("cp").build());
        InMemoryOutput bos = new InMemoryOutput(Charset.defaultCharset().displayName());
        res.get(0).transform(new Pipe.DefaultOnReady(bos));
        final String content = bos.execution().toString();
        Assert.assertTrue(content, content.contains("foo.less"));
        Assert.assertTrue(content, content.contains("bar.less"));
    }

    /**
     * Test libraries detection.
     *
     * @throws Exception if test fails
     */
    @Test
    public void libTest() throws Exception {
        final File parent = temporaryFolder.newFolder("parent");
        NutsHeap heap = mockHeap(parent);
        final ObjectBuilderFactory<Engine> factory = new ObjectBuilderFactory<Engine>(EngineService.class, CommandLineConverterEngine.class);
        final ObjectBuilder<Engine> builder = factory.create("CommandLineConverterEngineBuilder");
        final Engine engine = builder.property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, String.format("dir . > %s & echo %s > %s", CommandLineConverterEngine.OUT_PATH_TOKEN, SOURCEMAP, CommandLineConverterEngine.SOURCE_MAP_TOKEN))
                .property(ApplicationConfig.LIBRARIES, "/paths;/outPath;/sourceMap;/foo")
                .build();
        NutTypeFactoryHolder.class.cast(engine).setNutTypeFactory(new NutTypeFactory(Charset.defaultCharset().displayName()));

        List<ConvertibleNut> res = engine.parse(new EngineRequestBuilder("wid", heap, null, new NutTypeFactory(Charset.defaultCharset().displayName())).processContext(processContext.getProcessContext()).contextPath("cp").build());
        res.get(0).transform(new Pipe.DefaultOnReady(new InMemoryOutput(Charset.defaultCharset().displayName())));
    }

    /**
     * Bad command execution test.
     *
     * @throws java.io.IOException if test succeed
     * @throws com.github.wuic.exception.WuicException if test fails
     */
    @Test(expected = IOException.class)
    public void convertErrorTest() throws Exception {
        final String command = String.format("unknown %s %s %s ",
                CommandLineConverterEngine.PATH_TOKEN,
                CommandLineConverterEngine.OUT_PATH_TOKEN,
                CommandLineConverterEngine.SOURCE_MAP_TOKEN);
        final File parent = temporaryFolder.newFolder("parent");
        NutsHeap heap = mockHeap(parent);
        final ObjectBuilderFactory<Engine> factory = new ObjectBuilderFactory<Engine>(EngineService.class, CommandLineConverterEngine.class);
        final ObjectBuilder<Engine> builder = factory.create("CommandLineConverterEngineBuilder");
        final Engine engine = builder.property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, command)
                .build();
        NutTypeFactoryHolder.class.cast(engine).setNutTypeFactory(new NutTypeFactory(Charset.defaultCharset().displayName()));

        List<ConvertibleNut> res = engine.parse(new EngineRequestBuilder("wid", heap, null, new NutTypeFactory(Charset.defaultCharset().displayName()))
                .processContext(processContext.getProcessContext()).contextPath("cp").build());
        Output bos = new InMemoryOutput(Charset.defaultCharset().displayName());
        res.get(0).transform(new Pipe.DefaultOnReady(bos));
    }

    /**
     * Bad configuration test.
     */
    @Test
    public void badConfTokenTest() {
        final ObjectBuilderFactory<Engine> factory = new ObjectBuilderFactory<Engine>(EngineService.class, CommandLineConverterEngine.class);

        // No token
        Assert.assertNull(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, "unknown")
                .build());

        // Only path token
        Assert.assertNull(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, String.format("unknown %s", CommandLineConverterEngine.PATH_TOKEN))
                .build());

        // Only path and out path token
        Assert.assertNull(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .property(ApplicationConfig.COMMAND, String.format("unknown %s %s", CommandLineConverterEngine.PATH_TOKEN, CommandLineConverterEngine.OUT_PATH_TOKEN))
                .build());

        // Missing input type
        Assert.assertTrue(NodeEngine.class.cast(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, EnumNutType.LESS.name())
                .build()).getNutTypes().isEmpty());

        // Missing output type
        Assert.assertTrue(NodeEngine.class.cast(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .build()).getNutTypes().isEmpty());

        // Bad nut type
        Assert.assertNull(NodeEngine.class.cast(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, EnumNutType.LESS.name())
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, "bar")
                .build()));

        Assert.assertNull(NodeEngine.class.cast(factory.create("CommandLineConverterEngineBuilder")
                .property(ApplicationConfig.INPUT_NUT_TYPE, "foo")
                .property(ApplicationConfig.OUTPUT_NUT_TYPE, new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.CSS).name())
                .build()));
    }

    /**
     * <p>
     * Mocks a new heap.
     * </p>
     *
     * @param parent where nut could be stored
     * @throws Exception if mock fails
     * @return the mock
     */
    private NutsHeap mockHeap(final File parent) throws Exception {
        final Nut nut1 = mock(Nut.class);
        when(nut1.getInitialName()).thenReturn("foo.less");
        when(nut1.openStream()).thenAnswer(WuicTest.openStreamAnswer("foo"));

        // Value must be different for each test
        when(nut1.getVersionNumber()).thenReturn(new FutureLong(0L));
        when(nut1.getInitialNutType()).thenReturn(new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.LESS));

        IOUtils.copyStream(new ByteArrayInputStream("bar".getBytes()), new FileOutputStream(new File(parent, "bar.less")));
        final Nut nut2 = mock(Nut.class);
        when(nut2.getParentFile()).thenReturn(parent.getAbsolutePath());
        when(nut2.getInitialName()).thenReturn("bar.less");
        when(nut2.openStream()).thenAnswer(WuicTest.openStreamAnswer("bar"));
        when(nut2.getVersionNumber()).thenReturn(new FutureLong(0L));
        when(nut2.getInitialNutType()).thenReturn(new NutTypeFactory(Charset.defaultCharset().displayName()).getNutType(EnumNutType.LESS));

        final NutsHeap heap = mock(NutsHeap.class);
        when(heap.getNuts()).thenReturn(Arrays.asList(nut1, nut2));

        return heap;
    }
}
