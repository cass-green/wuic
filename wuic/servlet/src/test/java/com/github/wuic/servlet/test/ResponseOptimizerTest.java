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


package com.github.wuic.servlet.test;

import com.github.wuic.servlet.ResponseOptimizerFilter;
import com.github.wuic.test.Server;
import com.github.wuic.test.WuicRunnerConfiguration;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

/**
 * <p>
 * Tests the {@link ResponseOptimizerFilter}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.2
 */
@RunWith(JUnit4.class)
@WuicRunnerConfiguration(installFilter = ResponseOptimizerFilter.class, webApplicationPath = "/servletTest")
public class ResponseOptimizerTest {

    /**
     * The server running during tests.
     */
    @ClassRule
    public static com.github.wuic.test.Server server = new Server();

    /**
     * Timeout.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(60);

    /**
     * <p>
     * Executes a basic HTTP request and checks the header.
     * </p>
     *
     * @throws java.io.IOException if any I/O error occurs
     */
    @Test
    public void expiresTest() throws IOException {
        Assert.assertNotNull(server.get("/index.html").getFirstHeader("Expires"));
    }
}
