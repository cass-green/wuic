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


package com.github.wuic.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * <p>
 * This class can wrap a {@link ServletOutputStream} and GZIP the stream on the fly.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.2
 */
public class GzipHttpServletResponseWrapper extends OkHttpServletResponseWrapper {

    /**
     * <p>
     * Extends {@link ServletOutputStream} and stores the stream in a wrapped byte array.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.5.2
     */
    private final class GzipServletStream extends ServletOutputStream {

        /**
         * The servlet output stream.
         */
        private final ServletOutputStream sos;

        /**
         * The GZIP output stream
         */
        private final GZIPOutputStream gos;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param sos the wrapper stream
         * @throws IOException if an I/O error occurs
         */
        private GzipServletStream(final ServletOutputStream sos) throws IOException{
            this.sos = sos;
            this.gos = new GZIPOutputStream(sos);
        }

        /**
         * Makes sures that all gzipped bytes are actually committed to the response.
         */
        public void close() throws IOException {
            gos.close();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final int param) throws IOException {
            // This is where we GZIP what is written
            gos.write(param);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isReady() {
            // delegate to wrapper output stream
            return sos.isReady();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setWriteListener(final WriteListener writeListener) {
            // delegate to wrapper output stream
            sos.setWriteListener(writeListener);
        }
    }

    /**
     * Wrapped stream.
     */
    private GzipServletStream gss;

    /**
     * Print writer built on top of char array.
     */
    private PrintWriter pw;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param httpServletResponse the response to wrap
     */
    public GzipHttpServletResponseWrapper(final HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        HttpUtil.INSTANCE.setGzipHeader(httpServletResponse);
    }

    /**
     * <p>
     * Closes the response and flush the stream.
     * </p>
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        // Delegate call
        if (gss != null) {
            gss.close();
        } else if (pw != null) {
            pw.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // Makes sure both methods are not called since this is not allowed
        if (pw != null) {
            throw new IllegalStateException("getWriter() already called!");
        }

        if (gss == null) {
            gss = new GzipServletStream(super.getOutputStream());
        }

        return gss;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        // Makes sure both methods are not called since this is not allowed
        if (gss != null) {
            throw new IllegalStateException("getOutputStream() already called!");
        }

        if (pw == null) {
            // Force UTF-8 encoding
            pw = new PrintWriter(new OutputStreamWriter(new GzipServletStream(super.getOutputStream()), "UTF-8"));
            super.setCharacterEncoding("UTF-8");
        }

        return pw;
    }
}
