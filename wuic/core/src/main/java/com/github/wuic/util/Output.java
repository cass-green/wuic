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

import java.io.Closeable;
import java.io.OutputStream;
import java.io.Writer;

/**
 * <p>
 * A data output that can be written in bytes or characters. An instance is supposed to be written only once in
 * order to have a consistent output stream.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.3
 */
public interface Output extends Closeable {

    /**
     * <p>
     * Opens the output stream for bytes.
     * </p>
     *
     * @return the output stream
     */
    OutputStream outputStream();

    /**
     * <p>
     * Opens the output stream for chars.
     * </p>
     *
     * @return the output stream
     */
    Writer writer();

    /**
     * <p>
     * Builds an execution from the data written to the wrapped writer or output stream.
     * </p>
     *
     * @return the execution based on written content
     */
    Pipe.Execution execution();

    /**
     * <p>
     * Creates an input from the written content.
     * </p>
     *
     * @param charset the charset to use
     * @return the input
     */
    Input input(String charset);

    /**
     * <p>
     * Gets the charset used for encoding/decoding charset.
     * </p>
     *
     * @return the charset
     */
    String getCharset();
}