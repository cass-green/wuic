/*
 * "Copyright (c) 2015   Capgemini Technology Services (hereinafter "Capgemini")
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


package com.github.wuic.nut;

import com.github.wuic.NutType;
import com.github.wuic.util.CollectionUtils;
import com.github.wuic.util.IOUtils;
import com.github.wuic.util.Pipe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * <p>
 * This {@link ConvertibleNut} pipes transformers with the {@link Pipe} class.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.5.0
 */
public class PipedConvertibleNut extends AbstractConvertibleNut {

    /**
     * If transformation already occurred.
     */
    private boolean transformed;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param o the transformed nut
     */
    public PipedConvertibleNut(final Nut o) {
        super(o);
        transformed = false;
    }

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param name the name
     * @param ft the nut type
     * @param v the version
     * @param comp if compressible or not
     */
    protected PipedConvertibleNut(final String name,
                                  final NutType ft,
                                  final Future<Long> v,
                                  final Boolean comp) {
        super(name, ft, v, comp);
        transformed = false;
    }

    /**
     * <p>
     * Apply transformers by chaining each transformer specified in the given list.
     * </p>
     *
     * @param convertibleNut nut to transform
     * @param transformers the transformers chain
     * @param callbacks callbacks to call
     * @throws IOException if transformation fails
     */
    public static void transform(final ConvertibleNut convertibleNut,
                                 final Set<Pipe.Transformer<ConvertibleNut>> transformers,
                                 final List<Pipe.OnReady> callbacks)
            throws IOException {

        InputStream is = null;

        try {
            if (transformers != null) {
                final Pipe<ConvertibleNut> pipe = new Pipe<ConvertibleNut>(convertibleNut, convertibleNut.openStream());

                for (final Pipe.Transformer<ConvertibleNut> transformer : transformers) {
                    pipe.register(transformer);
                }

                pipe.execute(convertibleNut.ignoreCompositeStreamOnTransformation(), callbacks.toArray(new Pipe.OnReady[callbacks.size()]));
            } else {
                is = convertibleNut.openStream();
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStream(is, bos);
                final Pipe.Execution execution = new Pipe.Execution(bos.toByteArray());

                for (final Pipe.OnReady cb : callbacks) {
                    cb.ready(execution);
                }
            }
        } finally {
            IOUtils.close(is);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransformed() {
        return transformed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transform(final Pipe.OnReady ... onReady) throws IOException {
        if (isTransformed()) {
            throw new IllegalStateException("Could not call transform(Pipe.OnReady...) method twice.");
        }

        try {
            final List<Pipe.OnReady> merge = CollectionUtils.newList(onReady);

            if (getReadyCallbacks() != null) {
                merge.addAll(getReadyCallbacks());
            }

            transform(this, getTransformers(), merge);
        } finally {
            setTransformed(true);
        }
    }

    /**
     * <p>
     * Change the state of this nut by indicating if its transformed or not.
     * </p>
     *
     * @param transformed {@code true} if transformed, {@code false} otherwise
     */
    protected void setTransformed(final boolean transformed) {
        this.transformed = transformed;
    }
}
