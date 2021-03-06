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


package com.github.wuic.nut;

import java.io.IOException;

/**
 * <p>
 * This interface relies on a sourcemap protocol to exposes more information about original nuts.
 * The sources are {@link ConvertibleNut} instead of {@link Nut} to allow any transformer to fully generate any source
 * (sprites are a good example).
 * </p>
 *
 * <p>
 * According to transformation (inspection that detect existing sourcemap, aggregation, etc), the mapping should be updated.
 * </p>
 *
 * <p>
 * The {@link ConvertibleNut} is implemented for convenient usage by different APIs, but the contract that must be really
 * be implemented is defined by {@link Nut}. Methods inherited from {@link ConvertibleNut} can do nothing, as a source map
 * is not supposed to be transformed.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.3
 */
public interface SourceMapNut extends Source, ConvertibleNut {

    /**
     * <p>
     * Adds the given {@link ConvertibleNut} to this source mapping.
     * The source mapping of the nut specified in parameter is inserted in this source map.
     * The given positions indicate the position of the specified nut in the owner's content.
     * </p>
     *
     * @param nut the source
     * @param startColumn the column of starting position
     * @param startLine the line of starting position
     * @param endColumn the column of ending position
     * @param endLine the line of ending position
     */
    void addSource(int startLine, int startColumn, int endLine, int endColumn, ConvertibleNut nut);

    /**
     * <p>
     * Gets the source at the given line and column in the owner's content.
     * </p>
     *
     * @param line the line
     * @param column the column
     * @return the source
     * @throws IOException if any I/O error occurs
     */
    ConvertibleNut getNutAt(int line, int column) throws IOException;
}
