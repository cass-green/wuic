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


package com.github.wuic.engine;

import com.github.wuic.exception.WuicException;
import com.github.wuic.nut.ConvertibleNut;

import java.util.List;

/**
 * <p>
 * An engine is in charge to parse a set of nuts. They are generally able to
 * parse an unique kind of {@link com.github.wuic.NutType type} but the subclass could
 * supports several types.
 * </p>
 *
 * <p>
 * Engines comparisons is based on their {@link EngineType}.
 * </p>
 * 
 * @author Guillaume DROUET
 * @since 0.1.0
 */
public abstract class Engine implements Comparable<Engine> {

    /**
     * <p>
     * Gets the type of engine.
     * </p>
     *
     * @return the type of process done by with engine
     */
    public abstract EngineType getEngineType();

    /**
     * <p>
     * Parses the given nuts and returns the result of this operation.
     * </p>
     *
     * <p>
     * Should throw an {@link IllegalArgumentException} the files type is not supported by this {@link Engine}.
     * </p>
     *
     * @param request the request with nuts to parse
     * @return the parsed nuts
     * @throws WuicException if any kind of error occurs
     */
    public abstract List<ConvertibleNut> parse(EngineRequest request) throws WuicException;

    /**
     * <p>
     * Internal method that parses eventually called by {@link NodeEngine#parse(EngineRequest)} method during its invocation.
     * </p>
     *
     * @param request the request with files to parse
     * @return the parsed files
     * @throws com.github.wuic.exception.WuicException if any kind of error occurs
     */
    protected abstract List<ConvertibleNut> internalParse(EngineRequest request) throws WuicException;

    /**
     * <p>
     * Returns a flag indicating if the engine is configured to do something or not.
     * </p>
     * 
     * @return {@code true} is something will be done, {@code false} otherwise
     */
    public abstract Boolean works();

    /**
     * {@inheritDoc}
     */
    @Override
    public final int compareTo(final Engine other) {
        return getEngineType().compareTo(other.getEngineType());
    }
}
