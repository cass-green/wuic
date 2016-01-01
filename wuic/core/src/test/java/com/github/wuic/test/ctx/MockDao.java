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


package com.github.wuic.test.ctx;

import com.github.wuic.NutType;
import com.github.wuic.ProcessContext;
import com.github.wuic.config.ConfigConstructor;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.dao.NutDao;
import com.github.wuic.nut.dao.NutDaoListener;
import com.github.wuic.nut.dao.NutDaoService;
import com.github.wuic.util.FutureLong;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Mocked DAO builder.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.1
 * @since 0.4.4
 */
@NutDaoService
public class MockDao implements NutDao {

    /**
     * Mocked nut.
     */
    private final Nut mockNutOne = mock(Nut.class);

    /**
     * Another mocked nut.
     */
    private final Nut mockNutTwo = mock(Nut.class);

    /**
     * The last process context used during creation.
     */
    private ProcessContext lastProcessContext;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @throws java.io.IOException if mock fails
     */
    @ConfigConstructor
    public MockDao() throws IOException {
        // Prepare Nut mock
        when(mockNutOne.getInitialName()).thenReturn("foo.js");
        when(mockNutOne.getInitialNutType()).thenReturn(NutType.JAVASCRIPT);
        when(mockNutOne.openStream()).thenReturn(new ByteArrayInputStream("var foo;".getBytes()));
        when(mockNutOne.getVersionNumber()).thenReturn(new FutureLong(1L));

        when(mockNutTwo.getInitialName()).thenReturn("test.js");
        when(mockNutTwo.getInitialNutType()).thenReturn(NutType.JAVASCRIPT);
        when(mockNutTwo.openStream()).thenReturn(new ByteArrayInputStream("var test;".getBytes()));
        when(mockNutTwo.getVersionNumber()).thenReturn(new FutureLong(1L));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void observe(final String realPath, final NutDaoListener... listeners) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Nut> create(final String path, final ProcessContext processContext) throws IOException {
        return create(path, null, processContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Nut> create(final String path, final PathFormat format, final ProcessContext processContext) throws IOException {
        lastProcessContext = processContext;

        if (ContextBuilderTest.NUT_NAME_ONE.equals(path)) {
            return Arrays.asList(mockNutOne);
        } else if (ContextBuilderTest.NUT_NAME_TWO.equals(path)) {
            return Arrays.asList(mockNutTwo);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String proxyUriFor(final Nut nut) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Nut nut) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean saveSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NutDao withRootPath(final String rootPath) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream newInputStream(final String path, final ProcessContext processContext) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean exists(final String path, final ProcessContext processContext) throws IOException {
        return null;
    }

    /**
     * <p>
     * Gets the process context
     * </p>
     *
     * @return last captured instance
     */
    public ProcessContext getLastProcessContext() {
        return lastProcessContext;
    }
}
