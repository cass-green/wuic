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


package com.github.wuic.engine.core;

import com.github.wuic.util.StringUtils;

/**
 * <p>
 * This class represents a statement delimited by two positions (line/column) in a particular matrix of characters.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.3
 */
public class Statement {

    /**
     * The starting line.
     */
    private final int startLine;

    /**
     * The starting column.
     */
    private final int startColumn;

    /**
     * The ending line.
     */
    private final int endLine;

    /**
     * The ending column.
     */
    private final int endColumn;

    /**
     * The matrix.
     */
    private final String[] matrix;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param startLine the starting line
     * @param startColumn the starting column
     * @param endLine the ending line
     * @param endColumn the ending column
     * @param matrix the characters matrix
     */
    Statement(final int startLine, final int startColumn, final int endLine, final int endColumn, final String[] matrix) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;

        this.matrix = new String[matrix.length];
        System.arraycopy(matrix, 0, this.matrix, 0, this.matrix.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtils.substringMatrix(matrix, startLine, startColumn, endLine, endColumn);
    }

    /**
     * <p>
     * Gets the end column.
     * </p>
     *
     * @return the end column
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * <p>
     * Gets the end line.
     * </p>
     *
     * @return the end line
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * <p>
     * Gets the start column.
     * </p>
     *
     * @return the start column
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * <p>
     * Gets the start line.
     * </p>
     *
     * @return the start line
     */
    public int getStartLine() {
        return startLine;
    }
}

