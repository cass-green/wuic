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

import com.github.wuic.NutType;
import com.github.wuic.NutTypeFactory;
import com.github.wuic.ProcessContext;
import com.github.wuic.context.Context;
import com.github.wuic.mbean.HeapResolution;
import com.github.wuic.mbean.TransformationStat;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.nut.PipedConvertibleNut;
import com.github.wuic.util.CollectionUtils;
import com.github.wuic.util.TimerTreeFactory;
import com.github.wuic.util.UrlProviderFactory;
import com.github.wuic.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>
 * This class builds {@link EngineRequest} objects.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.5.0
 */
public final class EngineRequestBuilder {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The nuts.
     */
    private List<ConvertibleNut> nuts;

    /**
     * The context path.
     */
    private String contextPath;

    /**
     * The heap.
     */
    private NutsHeap heap;

    /**
     * The workflow ID.
     */
    private String workflowId;

    /**
     * The engine chains for each type.
     */
    private Map<NutType, NodeEngine> chains;

    /**
     * {@link EngineType} that should be skipped during workflow execution.
     */
    private EngineType[] skip;

    /**
     * The prefix path of created nuts.
     */
    private String prefixCreatedNut;

    /**
     * The URL provider.
     */
    private UrlProviderFactory urlProviderFactory;

    /**
     * Indicates if this request requires a best effort or not.
     */
    private boolean bestEffort;

    /**
     * Process context.
     */
    private ProcessContext processContext;

    /**
     * All image nut names that should not be included in any sprite.
     */
    private Set<String> excludeFromSprite;

    /**
     * Indicates if WUIC will serve the result or not.
     */
    private boolean staticsServedByWuicServlet;

    /**
     * The nut that has created this builder.
     */
    private ConvertibleNut origin;

    /**
     * Nut type factory.
     */
    private NutTypeFactory nutTypeFactory;

    /**
     * All reported transformation statistics.
     */
    private Map<String, List<TransformationStat>> transformationStats;

    /**
     * All reported heap resolution statistics.
     */
    private Map<String, List<HeapResolution>> heapResolutionStats;

    /**
     * All reported parse operation duration.
     */
    private long parseEngines;

    /**
     * The timer tree factory.
     */
    private TimerTreeFactory timerTreeFactory;

    /**
     * The context that created this builder.
     */
    private final Context context;

    /**
     * <p>
     * Builds a new instance with mandatory workflow ID and {@link NutsHeap}.
     * </p>
     *
     * @param wId the workflow id
     * @param h the heap
     * @param ctx the context
     * @param ntf the nut type factory
     */
    public EngineRequestBuilder(final String wId, final NutsHeap h, final Context ctx, final NutTypeFactory ntf) {
        workflowId(wId);
        heap(h);
        prefixCreatedNut("");
        contextPath("");
        bestEffort = false;
        context = ctx;
        staticsServedByWuicServlet = false;
        nutTypeFactory = ntf;
        transformationStats = new TreeMap<String, List<TransformationStat>>();
        heapResolutionStats = new TreeMap<String, List<HeapResolution>>();
        parseEngines = 0;
        timerTreeFactory = new TimerTreeFactory();
    }

    /**
     * <p>
     * Builds a new instance with the {@link EngineRequestBuilder} wrapped inside the given {@link EngineRequest request}.
     * </p>
     *
     * @param request the request
     */
    public EngineRequestBuilder(final EngineRequest request) {
        final EngineRequestBuilder other = request.getBuilder();
        nuts = other.nuts;
        contextPath = other.contextPath;
        heap = other.heap;
        workflowId = other.workflowId;
        chains = other.chains;
        skip = other.skip;
        prefixCreatedNut = other.prefixCreatedNut;
        urlProviderFactory = other.urlProviderFactory;
        bestEffort = other.bestEffort;
        processContext = other.processContext;
        context = other.context;
        excludeFromSprite = other.excludeFromSprite;
        staticsServedByWuicServlet = other.staticsServedByWuicServlet;
        origin = other.origin;
        nutTypeFactory = other.nutTypeFactory;
        transformationStats = other.transformationStats;
        heapResolutionStats = other.heapResolutionStats;
        parseEngines = other.parseEngines;
        timerTreeFactory = other.timerTreeFactory;
    }

    /**
     * <p>
     * Disables best effort for this request.
     * </p>
     *
     * @return this
     */
    public EngineRequestBuilder disableBestEffort() {
        bestEffort = false;
        return this;
    }

    /**
     * <p>
     * Enables best effort for this request.
     * </p>
     *
     * @return this
     */
    public EngineRequestBuilder bestEffort() {
        bestEffort = true;
        return this;
    }

    /**
     * <p>
     * Sets the nuts. Each nut will be wrapped in a {@link PipedConvertibleNut} to be exposed as {@link ConvertibleNut}.
     * </p>
     *
     * @param n the nuts
     * @return this
     */
    public EngineRequestBuilder nuts(final List<? extends Nut> n) {
        nuts = new ArrayList<ConvertibleNut>(n.size());

        for (final Nut nut : n) {
            nuts.add(new PipedConvertibleNut(nut));
        }

        return this;
    }

    /**
     * <p>
     * Sets the workflow ID.
     * </p>
     *
     * @param wId the workflow ID
     * @return this
     */
    public EngineRequestBuilder workflowId(final String wId) {
        workflowId = wId;
        return this;
    }

    /**
     * <p>
     * Sets the prefix of created nut.
     * </p>
     *
     * @param pcn the prefix
     * @return this
     */
    public EngineRequestBuilder prefixCreatedNut(final String pcn) {
        prefixCreatedNut = pcn;
        return this;
    }

    /**
     * <p>
     * Sets the {@link UrlProviderFactory}.
     * </p>
     *
     * @param upf the factory
     * @return this
     */
    public EngineRequestBuilder urlProviderFactory(final UrlProviderFactory upf) {
        urlProviderFactory = upf;
        return this;
    }

    /**
     * <p>
     * Sets the skipped {@link EngineType}.
     * </p>
     *
     * @param toSkip the skipped types
     * @return this
     */
    public EngineRequestBuilder skip(final EngineType... toSkip) {
        skip = new EngineType[toSkip.length];
        System.arraycopy(toSkip, 0, skip, 0, toSkip.length);
        return this;
    }

    /**
     * <p>
     * Sets the context path.
     * </p>
     *
     * @param cp the context path
     * @return this
     */
    public EngineRequestBuilder contextPath(final String cp) {
        contextPath = cp;
        return this;
    }

    /**
     * <p>
     * Sets the {@link NutsHeap}.
     * </p>
     *
     * @param h the heap
     * @return this
     */
    public EngineRequestBuilder heap(final NutsHeap h) {
        heap = h;
        return this;
    }

    /**
     * <p>
     * Excludes any nut with a name in the given set from sprite computation.
     * </p>
     *
     * @param efs the set
     * @return this
     */
    public EngineRequestBuilder excludeFromSprite(final Set<String> efs) {
        excludeFromSprite = efs;
        return this;
    }

    /**
     * <p>
     * Sets the chains of {@link NodeEngine engines} for each {@link NutType type}.
     * </p>
     *
     * @param c the chains
     * @return this
     */
    public EngineRequestBuilder chains(final Map<NutType, ? extends NodeEngine> c) {
        for (final Map.Entry<NutType, ? extends NodeEngine> chain : c.entrySet()) {
            chain(chain.getKey(), chain.getValue());
        }

        return this;
    }

    /**
     * <p>
     * Sets the chains of {@link NodeEngine engines} for each {@link NutType type}.
     * </p>
     *
     * @param nutType the type
     * @param nodeEngine the chain's root
     * @return this
     */
    public EngineRequestBuilder chain(final NutType nutType, final NodeEngine nodeEngine) {
        if (chains == null) {
            chains = new HashMap<NutType, NodeEngine>();
        }

        chains.put(nutType, nodeEngine);

        return this;
    }

    /**
     * <p>
     * Sets the process context.
     * </p>
     *
     * @param pc the process context
     * @return this
     */
    public EngineRequestBuilder processContext(final ProcessContext pc) {
        processContext = pc;
        return this;
    }

    /**
     * <p>
     * Indicates that result will be served by WUIC servlet.
     * </p>
     *
     * @return this
     */
    public EngineRequestBuilder staticsServedByWuicServlet() {
        staticsServedByWuicServlet = true;
        return this;
    }

    /**
     * <p>
     * Sets the nut which created this builder.
     * </p>
     *
     * @param origin the origin
     * @return this
     */
    public EngineRequestBuilder origin(final ConvertibleNut origin) {
        this.origin = origin;
        return this;
    }

    /**
     * <p>
     * Builds a bew instance. Default state is applied here if some attributes have not been initialized.
     * </p>
     *
     * @return the built {@link EngineRequest}
     */
    public EngineRequest build() {
        if (nuts == null) {
            nuts(heap.getNuts());
        }

        if (urlProviderFactory == null) {
            urlProviderFactory = UrlUtils.urlProviderFactory();
        }

        if (skip == null) {
            skip();
        }

        if (chains == null) {
            chains = new HashMap<NutType, NodeEngine>();
        }

        return new EngineRequest(this);
    }

    /**
     * <p>
     * Returns the prefix created nut
     * </p>
     *
     * @return the created nut
     */
    public String getPrefixCreatedNut() {
        return prefixCreatedNut;
    }

    /**
     * <p>
     * Gets the nuts.
     * </p>
     *
     * @return the nuts
     */
    public List<ConvertibleNut> getNuts() {
        return nuts;
    }

    /**
     * <p>
     * Gets the context path.
     * </p>
     *
     * @return the context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * <p>
     * Returns the heap.
     * </p>
     *
     * @return the heap
     */
    public NutsHeap getHeap() {
        return heap;
    }

    /**
     * <p>
     * Gets the workflow ID.
     * </p>
     *
     * @return the workflow ID.
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     * <p>
     * Gets the chains which can treat nuts of the given {@link NutType}.
     * </p>
     *
     * @param nutType the nut type
     * @return the chains that can treat this nut type
     */
    public NodeEngine getChainFor(final NutType nutType) {
        final NodeEngine retval = chains.get(nutType);

        if (retval == null) {
            log.warn("No chain exists for the heap '{}' and the nut type {}.", heap.getId(), nutType.name());
        }

        return retval;
    }

    /**
     * <p>
     * Indicates if an engine of the given type should skip its treatment when this request is submitted.
     * </p>
     *
     * @param engineType the {@link EngineType}
     * @return {@code true} if treatment should be skipped, {@code false} otherwise.
     */
    public boolean shouldSkip(final EngineType engineType) {
        return CollectionUtils.indexOf(engineType, skip) != -1;
    }

    /**
     * <p>
     * Gets the {@link UrlProviderFactory}.
     * </p>
     *
     * @return the factory
     */
    public UrlProviderFactory getUrlProviderFactory() {
        return urlProviderFactory;
    }

    /**
     * <p>
     * Indicates if this request requires best effort.
     * </p>
     *
     * @return {@code true} in case of best effort, {@code false} otherwise
     */
    public boolean isBestEffort() {
        return bestEffort;
    }

    /**
     * <p>
     * Gets the process context.
     * </p>
     *
     * @return the process context
     */
    public ProcessContext getProcessContext() {
        return processContext;
    }

    /**
     * <p>
     * Gets the nut which created this builder.
     * </p>
     *
     * @return the origin, {@code null} if the builder has not been created by a nut
     */
    public ConvertibleNut getOrigin() {
        return origin;
    }

    /**
     * <p>
     * Gets the skipped {@link EngineType types}. Package access for {@link EngineRequest} only.
     * </p>
     *
     * @return the skipped engine types
     */
    EngineType[] getSkip() {
        return skip;
    }

    /**
     * <p>
     * Gets the context.
     * </p>
     *
     * @return the context
     */
    Context getContext() {
        return context;
    }

    /**
     * <p>
     * Get the set containing excluded nuts from sprite computation.
     * </p>
     *
     * @return the excluded names
     */
    Set<String> getExcludeFromSprite() {
        return excludeFromSprite;
    }

    /**
     * <p>
     * Indicates if result is served by WUIC servlet.
     * </p>
     *
     * @return {@code true} if statics are served by WUIC servlet, {@code false} otherwise
     */
    boolean isStaticsServedByWuicServlet() {
        return staticsServedByWuicServlet;
    }

    /**
     * <p>
     * Gets the nut type factory.
     * </p>
     *
     * @return the nut type factory
     */
    NutTypeFactory getNutTypeFactory() {
        return nutTypeFactory;
    }

    /**
     * <p>
     * Gets the transformation statistics.
     * </p>
     *
     * @return the statistics
     */
    Map<String, List<TransformationStat>> getTransformationStats() {
        return transformationStats;
    }

    /**
     * <p>
     * Gets the heap resolution statistics.
     * </p>
     *
     * @return the statistics
     */
    Map<String, List<HeapResolution>> getHeapResolutionStats() {
        return heapResolutionStats;
    }

    /**
     * <p>
     * Gets the time elapsed during request parsing.
     * </p>
     *
     * @return the parse engines
     */
    long getParseEngines() {
        return parseEngines;
    }

    /**
     * <p>
     * Increment the time spent during engine parsing.
     * </p>
     *
     * @param elapse the elapsed time
     */
    void incrementParseEngines(final long elapse) {
        parseEngines += elapse;
    }

    /**
     * <p>
     * Gets the timer tree factory.
     * </p>
     *
     * @return the timer tree factory
     */
    TimerTreeFactory getTimerTreeFactory() {
        return timerTreeFactory;
    }
}
