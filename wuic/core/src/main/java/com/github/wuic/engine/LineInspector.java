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


package com.github.wuic.engine;

import com.github.wuic.exception.WuicException;
import com.github.wuic.nut.CompositeNut;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.util.IOUtils;
import com.github.wuic.util.NutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * Represents an object providing replacement functionality inside a line for a group of matched characters.
 * A matching operation can be applied line by line but sometimes the result can be affected by what has been discovered
 * on the previous line matching. This is why each file inspection should start after calling {@link #newInspection()}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.3
 * @since 0.3.3
 */
public abstract class LineInspector {

    /**
     * Possible value for path name.
     */
    protected static final String STRING_LITERAL_REGEX = "(\"(?:\\.|[^\\\"])*\"|'(?:\\.|[^\\'])*')";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LineInspector.class);

    /**
     * <p>
     * When the nut resolution fails (no nut found), the path is keep in the source file. However, this method allows
     * to append this path to a {@link StringBuilder} with a version number retrieved from the referencer in order to
     * give a chance to evict client cache.
     * </p>
     *
     * @param sb the string builder to append
     * @param referencedPath the unresolved path
     * @param originalNut the referencer with the version number
     * @throws WuicException if version number resolution fails
     */
    public static void fallbackToVersionNumberInQueryString(final StringBuilder sb,
                                                            final String referencedPath,
                                                            final Nut originalNut) throws WuicException {

        LOGGER.warn("{} is referenced as a relative file but it was not found with in the DAO. "
                + "Keeping same value with the referencer version number in a query string to evict client cache.",
                referencedPath);
        try {
            sb.append(referencedPath)
                    .append(referencedPath.indexOf('?') == -1 ? '?' : '&')
                    .append("versionNumber=")
                    .append(originalNut.getVersionNumber().get());
        } catch (ExecutionException ee) {
            WuicException.throwWuicException(ee);
        } catch (InterruptedException ie) {
            WuicException.throwWuicException(ie);
        }
    }

    /**
     * <p>
     * Manages the given nut that corresponds to the specified referenced path and append the transformation with proper
     * path.
     * </p>
     *
     * @param nut the nut retrieved with path
     * @param replacement the string builder to append
     * @param request the engine request
     * @param heap the heap that contains the original nut
     * @param skippedEngine the engine to skip when processing resulting nuts
     * @return the processed nuts specified in parameter
     * @throws WuicException if processing fails
     */
    public static List<? extends ConvertibleNut> manageAppend(final ConvertibleNut nut,
                                                              final StringBuilder replacement,
                                                              final EngineRequest request,
                                                              final NutsHeap heap,
                                                              final EngineType ... skippedEngine) throws WuicException {
        List<? extends ConvertibleNut> res;

        // If nut name is null, it means that nothing has been changed by the inspector
        res = Arrays.asList(nut);

        // Process nut
        final NodeEngine engine = request.getChainFor(nut.getInitialNutType());
        if (engine != null) {
            res = engine.parse(new EngineRequestBuilder(request).nuts(res).heap(heap).skip(skippedEngine).build());
        }

        // Use proxy URI if DAO provide it
        final String proxy = nut.getProxyUri();

        final ConvertibleNut resNut = res.isEmpty() ? null: res.get(0);
        final ConvertibleNut renamed;

        if (resNut != null) {
            renamed = resNut;
            resNut.setNutName(resNut.getName().replace("../", "a/../"));
        } else {
            renamed = nut;
        }

        if (proxy == null) {
            replacement.append(IOUtils.mergePath(
                    "/",
                    request.getContextPath(),
                    request.getWorkflowId(),
                    String.valueOf(NutUtils.getVersionNumber(nut)),
                    renamed.getName()));
        } else {
            replacement.append(proxy);
        }

        return res;
    }

    /**
     * <p>
     * Gets the {@link NutsHeap} pointing to the right {@link com.github.wuic.nut.dao.NutDao} according to the given
     * original nut.
     * </p>
     *
     * @param request the request
     * @param originalNut the original nut
     * @param cis the composite stream if any
     * @param position the position of the matched data in the inspected stream
     * @return the heap
     */
    public static NutsHeap getHeap(final EngineRequest request,
                                   final ConvertibleNut originalNut,
                                   final CompositeNut.CompositeInputStream cis,
                                   final int position) {
        final NutsHeap heap = new NutsHeap(request.getHeap());
        final String name;

        // Extracts the location where nut is listed in order to compute the location of the extracted imported nuts
        if (cis == null) {
            name = originalNut.getName();
        } else {
            name = cis.nutAt(position).getName();
        }

        final int lastIndexOfSlash = name.lastIndexOf('/') + 1;
        final String nutLocation = lastIndexOfSlash == 0 ? "" : name.substring(0, lastIndexOfSlash);
        heap.setNutDao(request.getHeap().withRootPath(nutLocation, originalNut), originalNut);
        return heap;
    }

    /**
     * <p>
     * Builds a new {@link ReplacementInfo}.
     * </p>
     *
     * @param startIndex the start index
     * @param endIndex the end index
     * @param referencer the referencer
     * @param convertibleNuts the nuts
     * @param string the {@code String} to use as replacement
     * @return the new instance
     */
    public ReplacementInfo replacementInfo(final int startIndex,
                                           final int endIndex,
                                           final Nut referencer,
                                           final List<? extends ConvertibleNut> convertibleNuts,
                                           final String string) {
        return new ReplacementInfo(startIndex, endIndex, referencer, convertibleNuts, string);
    }

    /**
     * <p>
     * Inspects the given char array and notifies the given {@link LineInspectorListener listener} when some data are matched.
     * </p>
     *
     * @param listener the listener the notify
     * @param data the char array to inspect
     * @param request the request that orders this transformation
     * @param cis a composite stream which indicates what nut owns the transformed text, {@code null} if the nut is not a composition
     * @param originalNut the original nut
     * @throws WuicException if inspection fails
     */
    public void inspect(final LineInspectorListener listener,
                        final char[] data,
                        final EngineRequest request,
                        final CompositeNut.CompositeInputStream cis,
                        final ConvertibleNut originalNut) throws WuicException {
        inspect(listener, data, 0, data.length, request, cis, originalNut);
    }

    /**
     * <p>
     * Tells this inspector a new inspection of a set of lines is going to start.
     * This allows this object to clean any retained information regarding lines previously inspected and that could be
     * taken into consideration in the matching operation that are going to be processed.
     * This method should be called each time a file content is going to be inspected line by line.
     * </p>
     */
    public abstract void newInspection();

    /**
     * <p>
     * Inspects a portion of the given char array and notifies the given {@link LineInspectorListener listener} when
     * some data are matched.
     * </p>
     *
     * @param listener the listener the notify
     * @param data the char array to inspect
     * @param offset the start position of content to inspect in the array
     * @param length the length of content to inspect
     * @param request the request that orders this transformation
     * @param cis a composite stream which indicates what nut owns the transformed text, {@code null} if the nut is not a composition
     * @param originalNut the original nut
     * @throws WuicException if inspection fails
     */
    public abstract void inspect(LineInspectorListener listener,
                                 char[] data,
                                 int offset,
                                 int length,
                                 EngineRequest request,
                                 CompositeNut.CompositeInputStream cis,
                                 ConvertibleNut originalNut) throws WuicException;

    /**
     * <p>
     * Converts the given {@link ConvertibleNut} to a {@code String} representation that is possible to append directly
     * to the referencer. This could be used as an alternative to URL statements.
     * </p>
     *
     * @param convertibleNut the convertible nut to convert
     * @return the {@code String} representation, {@code null} if no inclusion is possible
     * @throws IOException if the transformation fails
     */
    protected abstract String toString(ConvertibleNut convertibleNut) throws IOException;

    /**
     * <p>
     * This object indicates the index of the rewritten statement and the associated list of nuts.
     * </p>
     *
     * <p>
     * The {@link Comparable} is implemented to order a set of {@link ReplacementInfo} from the greatest {@link #startIndex}
     * to the smallest.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.5.1
     */
    public final class ReplacementInfo implements Comparable<ReplacementInfo> {

        /**
         * Start index.
         */
        private final int startIndex;

        /**
         * End index.
         */
        private final int endIndex;

        /**
         * The nut referencing the convertibles nuts.
         */
        private final Nut referencer;

        /**
         * The convertible nut.
         */
        private final List<? extends ConvertibleNut> convertibleNuts;

        /**
         * The {@code String} to use for replacement.
         */
        private final String string;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param startIndex the start index
         * @param endIndex the end index
         * @param referencer the referencer
         * @param convertibleNuts the nuts
         * @param string the {@code String} to use for replacement.
         */
        private ReplacementInfo(final int startIndex,
                                final int endIndex,
                                final Nut referencer,
                                final List<? extends ConvertibleNut> convertibleNuts,
                                final String string) {
            this.startIndex = startIndex;
            this.convertibleNuts = convertibleNuts;
            this.referencer = referencer;
            this.endIndex = endIndex;
            this.string = string;
            LOGGER.debug("Statement between position {} and {} is referenced for replacement in referencer '{}'",
                    startIndex, endIndex, referencer.getInitialName());
        }

        /**
         * <p>
         * Performs a replacement inside the given {@code StringBuilder} by replacing the portion delimited by the
         * {@link #startIndex} and {@link #endIndex} positions with the {@link #string} value.
         * </p>
         *
         * @param stringBuilder the builder
         */
        public void replace(final StringBuilder stringBuilder) {
            replace(stringBuilder, string);
        }

        /**
         * <p>
         * Performs a replacement inside the given {@code StringBuilder} by replacing the portion delimited by the
         * {@link #startIndex} and {@link #endIndex} positions with the given value.
         * </p>
         *
         * @param stringBuilder the builder
         * @param str the replacement {@code String}
         */
        public void replace(final StringBuilder stringBuilder, final String str) {
            stringBuilder.replace(startIndex, endIndex, str);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final ReplacementInfo o) {
            return o.startIndex - startIndex;
        }

        /**
         * <p>
         * Gets the referencer.
         * </p>
         *
         * @return the referencer
         */
        public Nut getReferencer() {
            return referencer;
        }

        /**
         * <p>
         * Gets the nuts.
         * </p>
         *
         * @return the list
         */
        public List<? extends ConvertibleNut> getConvertibleNuts() {
            return convertibleNuts;
        }

        /**
         * <p>
         * Returns a concatenated {@code String} representation of each  nut returned {@link #getConvertibleNuts()}.
         * </p>
         *
         * @return the string, {@code null} if no {@code String} representation could be provided
         */
        public String asString() throws IOException {
            final StringBuilder stringBuilder = new StringBuilder();

            for (final ConvertibleNut convertibleNut : getConvertibleNuts()) {
                final String str = LineInspector.this.toString(convertibleNut);

                if (str == null) {
                    return str;
                }

                stringBuilder.append(str);
            }

            return stringBuilder.toString();
        }
    }

    /**
     * <p>
     * This class represents the metadata describing an appended transformation.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.5.3
     */
    public static class AppendedTransformation {

        /**
         * Start index of appended transformation.
         */
        private final int start;

        /**
         * End index of appended transformation.
         */
        private final int end;

        /**
         * the nut that was referenced in the matching text, {@code null} if the inspector did not perform any change.
         */
        private final List<? extends ConvertibleNut> result;

        /**
         * The replacement for the given range of characters.
         */
        private final String replacement;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param start start index
         * @param end end index
         * @param result extracted nuts
         * @param replacement the replacement for the given range of characters.
         */
        public AppendedTransformation(final int start, final int end, final List<? extends ConvertibleNut> result, final String replacement) {
            this.start = start;
            this.end = end;
            this.result = result;
            this.replacement = replacement;
        }

        /**
         * <p>
         * Gets the start index.
         * </p>
         *
         * @return the start index.
         */
        public int getStart() {
            return start;
        }

        /**
         * <p>
         * Gets the end index.
         * </p>
         *
         * @return the end index
         */
        public int getEnd() {
            return end;
        }

        /**
         * <p>
         * Gets the extracted nuts.
         * </p>
         *
         * @return the nuts
         */
        public List<? extends ConvertibleNut> getResult() {
            return result;
        }

        /**
         * <p>
         * Gets the string replacement.
         * </p>
         *
         * @return the replacement
         */
        public String getReplacement() {
            return replacement;
        }
    }
}
