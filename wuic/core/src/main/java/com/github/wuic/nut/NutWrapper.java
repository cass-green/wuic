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

import com.github.wuic.NutType;
import com.github.wuic.mbean.TransformationStat;
import com.github.wuic.util.BiFunction;
import com.github.wuic.util.Input;
import com.github.wuic.util.Pipe;
import com.github.wuic.util.TimerTreeFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class wraps a {@link com.github.wuic.nut.Nut} and implements {@link Nut} interface by delegating method calls.
 * Useful when you need to change only some method behavior.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.4
 */
public class NutWrapper extends AbstractNut implements ConvertibleNut {

    /**
     * The wrapped nut.
     */
    private ConvertibleNut wrapped;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param w the wrapped nut
     */
    public NutWrapper(final ConvertibleNut w) {
        super(w);
        wrapped = w;
    }

    /**
     * <p>
     * Gets the wrapped nut.
     * </p>
     *
     * @return the wrapped nut
     */
    protected final ConvertibleNut getWrapped() {
        return wrapped;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public boolean isTransformed() {
        return wrapped.isTransformed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Input openStream() throws IOException {
        return wrapped.openStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getSource() {
        return wrapped.getSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSource(final Source source) {
        wrapped.setSource(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return wrapped.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNutName(final String nutName) {
        wrapped.setNutName(nutName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic() {
        return wrapped.isDynamic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NutType getNutType() {
        return wrapped.getNutType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNutType(final NutType nutType) {
        wrapped.setNutType(nutType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<TransformationStat>> transform(final Pipe.OnReady... onReady) throws IOException {
        return wrapped.transform(onReady);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<TransformationStat>> transform(final TimerTreeFactory timerTreeFactory, final Pipe.OnReady... onReady)
            throws IOException {
        return wrapped.transform(timerTreeFactory, onReady);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReady(final Pipe.OnReady onReady, final  boolean removeOnInvocation) {
        wrapped.onReady(onReady, removeOnInvocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Pipe.OnReady> getReadyCallbacks() {
        return wrapped.getReadyCallbacks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTransformer(final Pipe.Transformer<ConvertibleNut> transformer) {
        wrapped.addTransformer(transformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Pipe.Transformer<ConvertibleNut>> getTransformers() {
        return wrapped.getTransformers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addReferencedNut(final ConvertibleNut referenced) {
        wrapped.addReferencedNut(referenced);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConvertibleNut> getReferencedNuts() {
        return wrapped.getReferencedNuts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsCompressed(final Boolean c) {
        wrapped.setIsCompressed(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isCompressed() {
        return wrapped.isCompressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubResource() {
        return wrapped.isSubResource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsSubResource(final boolean subResource) {
        wrapped.setIsSubResource(subResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ignoreCompositeStreamOnTransformation() {
        return wrapped.ignoreCompositeStreamOnTransformation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addVersionNumberCallback(final BiFunction<ConvertibleNut, Long, Long> callback) {
        wrapped.addVersionNumberCallback(callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BiFunction<ConvertibleNut, Long, Long>> getVersionNumberCallbacks() {
        return wrapped.getVersionNumberCallbacks();
    }
}
