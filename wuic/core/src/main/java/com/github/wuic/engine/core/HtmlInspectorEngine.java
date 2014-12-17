/*
 * "Copyright (c) 2014   Capgemini Technology Services (hereinafter "Capgemini")
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


package com.github.wuic.engine.core;

import com.github.wuic.ApplicationConfig;
import com.github.wuic.NutType;
import com.github.wuic.config.BooleanConfigParam;
import com.github.wuic.config.ConfigConstructor;
import com.github.wuic.config.StringConfigParam;
import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.EngineService;
import com.github.wuic.engine.EngineType;
import com.github.wuic.engine.HeadEngine;
import com.github.wuic.engine.NodeEngine;
import com.github.wuic.exception.WuicException;
import com.github.wuic.exception.wrapper.BadArgumentException;
import com.github.wuic.exception.wrapper.StreamException;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.nut.dao.NutDao;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.nut.ByteArrayNut;
import com.github.wuic.nut.dao.core.ProxyNutDao;
import com.github.wuic.nut.filter.NutFilter;
import com.github.wuic.nut.filter.NutFilterHolder;
import com.github.wuic.util.Pipe;
import com.github.wuic.util.TerFunction;
import com.github.wuic.util.CollectionUtils;
import com.github.wuic.util.HtmlUtil;
import com.github.wuic.util.IOUtils;
import com.github.wuic.util.NumberUtils;
import com.github.wuic.util.StringUtils;

import com.github.wuic.util.UrlProvider;
import com.github.wuic.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This engine is able to parse an HTML to collect all CSS and JS scripts, refer them through a heap and set
 * reference to associated workflow URL in DOM.
 * </p>
 *
 * <p>
 * Some points have to be notified:
 * </p>
 *
 * <ul>
 *     <li>Observing collected nuts is not possible here so polling won't be perform to invalidate any cache.</li>
 *     <li>Version number is based on content hash because this is the unique strategy that applies to inline scripts</li>
 * </ul>
 *
 * @author Guillaume DROUET
 * @version 1.1
 * @since 0.4.4
 */
@EngineService(injectDefaultToWorkflow = true, isCoreEngine = true)
public class HtmlInspectorEngine extends NodeEngine implements NutFilterHolder {

    /**
     * We use a specific parser for each matched group. Store them in a TreeMap to ensure that lower groups will be tested before higher.
     */
    private static final Map<Integer, TerFunction<String, ProxyNutDao, String, String>> PARSERS =
            new TreeMap<Integer, TerFunction<String, ProxyNutDao, String, String>>() {
        {
            put(NumberUtils.SIX, new HrefParser());
        }
        {
            put(NumberUtils.FOURTEEN, new DefaultParser());
        }
        {
            put(1, new JsParser());
        }
        {
            put(NumberUtils.THIRTEEN, new CssParser());
        }
        {
            put(NumberUtils.FIFTEEN, new ImgParser());
        }
    };

    /**
     * Engines types that will be skipped when processing referenced nuts.
     */
    private static final EngineType[] SKIPPED_ENGINE = new EngineType[] { EngineType.CACHE, };

    /**
     * Regex that matches JS script import or JS declaration.
     */
    private static final String JS_SCRIPT_PATTERN = String.format("(<%1$s.*?(%2$s=)?(([^>]*>[^<]*</%1$s>)|([^/]*/>)))", "script", "src");

    /**
     * Regex that matches CSS script import.
     */
    private static final String HREF_SCRIPT_PATTERN = String.format("(<%1$s.*?(%2$s=)?(([^>]*>)(([^<]*</%1$s>)|([^/]*/>))?))", "link", "href");

    /**
     * Regex that matches images import.
     */
    private static final String IMG_PATTERN = String.format("(<%1$s.*?(%2$s=)?(([^>]*>[^<]*</%1$s>)|([^/]*/>)))", "img", "src");

    /**
     * Regex that matches CSS declaration.
     */
    private static final String CSS_SCRIPT_PATTERN = "(<style>.*?</style>)";

    /**
     * Regex that matches HTML comment.
     */
    private static final String HTML_COMMENT_PATTERN = "(<!--.*?-->)";

    /**
     * The entire regex that collects desired data.
     */
    private static final String REGEX =
            String.format("%s|%s|%s|%s|%s", JS_SCRIPT_PATTERN, HREF_SCRIPT_PATTERN, CSS_SCRIPT_PATTERN, HTML_COMMENT_PATTERN, IMG_PATTERN);

    /**
     * The pattern that collects desired data
     */
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The applied filters
     */
    private List<NutFilter> nutFilters;

    /**
     * Inspects or not.
     */
    private Boolean doInspection;

    /**
     * The charset of inspected file.
     */
    private String charset;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param inspect activate inspection or not
     * @param cs files charset
     */
    @ConfigConstructor
    public HtmlInspectorEngine(
            @BooleanConfigParam(defaultValue = true, propertyKey = ApplicationConfig.INSPECT) final Boolean inspect,
            @StringConfigParam(defaultValue = "UTF-8", propertyKey = ApplicationConfig.CHARSET) final String cs) {
        doInspection = inspect;
        charset = cs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConvertibleNut> internalParse(final EngineRequest request) throws WuicException {
        // Will contains both heap's nuts eventually modified or extracted nuts.
        final List<ConvertibleNut> retval = new ArrayList<ConvertibleNut>();

        if (works()) {
            for (final ConvertibleNut nut : request.getNuts()) {

                try {
                    retval.add(transformHtml(nut, request.getContextPath(), request));
                } catch (IOException ioe) {
                   throw new StreamException(ioe);
                }
            }
        }

        if (getNext() != null) {
            return getNext().parse(new EngineRequest(retval, request));
        } else {
            return retval;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNutFilter(final List<NutFilter> nutFilters) {
        this.nutFilters = nutFilters;
    }

    /**
     * <p>
     * This class is a transformer that appends to a particular nut an extracted resource added as a referenced nut.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.5.0
     */
    private final class HtmlTransformer extends Pipe.DefaultTransformer<ConvertibleNut> {

        /**
         * The request.
         */
        private EngineRequest request;

        /**
         * The context path.
         */
        private String contextPath;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param r the request
         * @param cp the context path
         */
        private HtmlTransformer(final EngineRequest r, final String cp) {
            this.request = r;
            this.contextPath = cp;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void transform(final InputStream is, final OutputStream os, final ConvertibleNut convertible)
                throws IOException {
            final long now = System.currentTimeMillis();
            final String content = IOUtils.readString(new InputStreamReader(is, charset));
            final int endParent = convertible.getName().lastIndexOf('/');
            final String rootPath = endParent == -1 ? "" : convertible.getName().substring(0, endParent);
            final List<ParseInfo> parseInfoList;

            try {
                parseInfoList = parse(convertible.getName(), content, request.getHeap().findDaoFor(convertible), rootPath);
            } catch (WuicException we) {
                throw new IOException(we);
            }

            final StringBuilder transform = new StringBuilder(content);
            int end = 0;
            final List<ConvertibleNut> referenced = new ArrayList<ConvertibleNut>();

            final UrlProvider urlProvider = UrlUtils.urlProviderFactory().create(IOUtils.mergePath(contextPath, request.getWorkflowId()));

            // A workflow have been created for each heap
            for (final ParseInfo parseInfo : parseInfoList) {
                final EngineType[] skip;

                // Do not generate sprite, just compress "img"
                if (NutType.PNG.equals(parseInfo.getHeap().getNuts().get(0).getNutType())) {
                    skip = new EngineType[SKIPPED_ENGINE.length + 1];
                    System.arraycopy(SKIPPED_ENGINE, 0, skip, 0, SKIPPED_ENGINE.length);
                    skip[skip.length -1] = EngineType.INSPECTOR;
                } else {
                    skip = SKIPPED_ENGINE;
                }

                // Render HTML for workflow result
                final StringBuilder html = new StringBuilder();
                final EngineRequest parseRequest = new EngineRequest(parseInfo.getHeap().getNuts(), parseInfo.getHeap(), request, skip);
                final List<ConvertibleNut> merged;

                try {
                    merged = HeadEngine.runChains(parseRequest, Boolean.FALSE);
                } catch (WuicException we) {
                    throw new IOException(we);
                }

                for (final ConvertibleNut n : merged) {
                    // Just add the heap ID as prefix to refer many nuts with same name but from different heaps
                    n.setNutName(parseInfo.getHeap().getId() + n.getName());
                    referenced.add(n);
                    html.append(HtmlUtil.writeScriptImport(n, urlProvider)).append("\r\n");
                }

                // Replace all captured statements with HTML generated from WUIC process
                for (int i = 0; i < parseInfo.getCapturedStatements().size(); i++) {
                    final String toReplace = parseInfo.getCapturedStatements().get(i);
                    int start = transform.indexOf(toReplace, end);
                    end = start + toReplace.length();

                    // Add the WUIC result in place of the first statement
                    if (i == 0) {
                        final String replacement = html.toString();
                        transform.replace(start, end, replacement);
                        end = start + replacement.length();
                    } else {
                        transform.replace(start, end, "");
                        end = start;
                    }
                }
            }

            IOUtils.copyStreamIoe(new ByteArrayInputStream(transform.toString().getBytes()), os);

            for (final ConvertibleNut ref : referenced) {
                convertible.addReferencedNut(ref);
            }

            logger.info("HTML transformation in {}ms", System.currentTimeMillis() - now);
        }
    }

    /**
     * <p>
     * This abstract class is a base for script references.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.4
     */
    private abstract static class ResourceParser implements TerFunction<String, ProxyNutDao, String, String> {

        /**
         * <p>
         * Returns the token that refers the URL.
         * </p>
         *
         * @return the token
         */
        protected abstract String urlToken();

        /**
         * <p>
         * Returns the {@link NutType} of created nut.
         * </p>
         *
         * @return the nut type
         */
        protected abstract NutType getNutType();

        /**
         * <p>
         * Indicates if the parser should tries to read ant inline content.
         * </p>

         * @return {@code true} if inline content needs to be read, {@code false} otherwise
         */
        protected abstract Boolean readInlineIfTokenNotFound();

        /**
         * {@inheritDoc}
         */
        @Override
        public String apply(final String s, final ProxyNutDao proxy, final String nutName) {
            if (s.contains("data-wuic-skip")) {
                return null;
            }

            final String token = urlToken();
            int index = token.isEmpty() ? -1 : s.indexOf(token) + token.length();

            if (index != token.length() - 1) {
                char c = s.charAt(index);
                final String retval;

                if (c == '\'' || c == '"') {
                    retval = s.substring(index + 1, s.indexOf(c, index + 1));
                } else {
                    int end = s.indexOf(' ', index);

                    // No space, search />
                    if (end == -1) {
                        end = s.indexOf('>', index);

                        if (s.charAt(end - 1) == '/') {
                            end--;
                        }
                    }
                    retval = s.substring(index, end);
                }

                if (retval.startsWith("http://") || retval.startsWith("https://")) {
                    return null;
                } else {
                    // Looking for a delimiter adding extra characters after the extracted name
                    int delimiter = retval.indexOf('#');

                    if (delimiter == -1) {
                        delimiter = retval.indexOf('?');
                    }

                    return delimiter == -1 ? retval : retval.substring(0, delimiter);
                }
            } else if (readInlineIfTokenNotFound()) {
                // Looking for content
                final int start = s.indexOf('>') + 1;
                final int end = s.indexOf('<', start - 1);
                final byte[] content = s.substring(start, end).getBytes();

                // Create nut
                final NutType nt = getNutType();
                final String retval = String.format("%s%s", nutName, nt.getExtensions()[0]);

                // Sign content
                final MessageDigest md = IOUtils.newMessageDigest();
                md.update(content);
                proxy.addRule(retval, new ByteArrayNut(content, retval, nt, ByteBuffer.wrap(md.digest()).getLong()));

                return retval;
            } else {
                return null;
            }
        }
    }

    /**
     * <p>
     * This class parses links to JS scripts and inline JS scripts.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.4
     */
    private static class JsParser extends ResourceParser {

        /**
         * {@inheritDoc}
         */
        @Override
        public String urlToken() {
            return "src=";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean readInlineIfTokenNotFound() {
            return Boolean.TRUE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NutType getNutType() {
            return NutType.JAVASCRIPT;
        }
    }

    /**
     * <p>
     * This class parses links to CSS.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.4
     */
    private static class HrefParser extends ResourceParser {

        /**
         * {@inheritDoc}
         */
        @Override
        public String urlToken() {
            return "href=";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean readInlineIfTokenNotFound() {
            return Boolean.FALSE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NutType getNutType() {
            return NutType.CSS;
        }
    }

    /**
     * <p>
     * This class parses 'img' tags.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.5.0
     */
    private static class ImgParser extends ResourceParser {

        /**
         * {@inheritDoc}
         */
        @Override
        public String urlToken() {
            return "src=";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean readInlineIfTokenNotFound() {
            return Boolean.FALSE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NutType getNutType() {
            return NutType.PNG;
        }
    }

    /**
     * <p>
     * This class parses inline CSS.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.1
     * @since 0.4.4
     */
    private static class CssParser extends ResourceParser {

        /**
         * {@inheritDoc}
         */
        @Override
        public String urlToken() {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean readInlineIfTokenNotFound() {
            return Boolean.TRUE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NutType getNutType() {
            return NutType.CSS;
        }
    }

    /**
     * <p>
     * This class returns always {@code null}, telling the caller to not replace the parsed {@code String}.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.4.4
     */
    private static class DefaultParser implements TerFunction<String, ProxyNutDao, String, String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String apply(final String s, final ProxyNutDao proxyNutDao, final String nutName) {
            return null;
        }
    }

    /**
     * <p>
     * Provides information from collected data in a HTML content.
     * </p>
     *
     * @author Guillaume DROUET
     * @version 1.0
     * @since 0.4.4
     */
    public final class ParseInfo {

        /**
         * The heap generated during collect.
         */
        private NutsHeap heap;

        /**
         * Collected statements.
         */
        private List<String> capturedStatements;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param groupName the nut name
         * @param groups captured statements
         * @param proxyNutDao the DAO to use when computing path from collected data
         * @param rootPath the root path of content
         * @throws StreamException if any I/O error occurs
         */
        private ParseInfo(final String groupName,
                          final Map<String, Integer> groups,
                          final ProxyNutDao proxyNutDao,
                          final String rootPath)
                throws StreamException {
            final String[] groupPaths = new String[groups.size()];
            this.capturedStatements = new ArrayList<String>(groups.keySet());

            int cpt = 0;

            // Gets the appropriate parser for each captured group according to their position and compute path
            for (final Map.Entry<String, Integer> entry : groups.entrySet()) {
                final TerFunction<String, ProxyNutDao, String, String> function = PARSERS.get(entry.getValue());
                final String path = function.apply(entry.getKey(), proxyNutDao, groupName + cpt);

                // Path is null, do not replace anything
                if (path != null) {
                    Boolean supportedType = Boolean.FALSE;

                    // Maybe the extension is not supported, evict the exception by not replacing the path
                    if (ResourceParser.class.isAssignableFrom(function.getClass())) {
                        final String[] ext = ResourceParser.class.cast(function).getNutType().getExtensions();

                        for (final String e : ext) {
                            if (path.endsWith(e)) {
                                supportedType = Boolean.TRUE;
                                break;
                            }
                        }

                        if (!supportedType) {
                            logger.warn("{} does not ends with an extension supported by WUIC, skipping...", path);
                            this.capturedStatements.remove(entry.getKey());
                        }
                    } else {
                        supportedType = Boolean.TRUE;
                    }

                    if (supportedType) {
                        final String simplify = rootPath.isEmpty() ? path : IOUtils.mergePath(rootPath, path);
                        final String simplified = StringUtils.simplifyPathWithDoubleDot(simplify);

                        if (simplified == null) {
                            throw new BadArgumentException(new IllegalArgumentException(String.format("%s does not represents a reachable path", simplify)));
                        }

                        groupPaths[cpt++] = simplified;
                    }
                } else {
                    this.capturedStatements.remove(entry.getKey());
                }
            }

            // No info have been collected
            if (cpt == 0) {
                return;
            }

            // All paths computed from captured statements.
            final String[] paths = new String[cpt];
            System.arraycopy(groupPaths, 0, paths, 0, cpt);

            List<String> filteredPath = CollectionUtils.newList(paths);

            if (nutFilters != null) {
                for (final NutFilter filter : nutFilters) {
                    filteredPath = filter.filterPaths(filteredPath);
                }
            }

            final byte[] hash = IOUtils.digest(filteredPath.toArray(new String[filteredPath.size()]));
            final String heapId = StringUtils.toHexString(hash);
            heap = new NutsHeap(filteredPath, proxyNutDao, heapId);
        }

        /**
         * <p>
         * Gets the captured statement by parsing.
         * </p>
         *
         * @return the statements
         */
        public List<String> getCapturedStatements() {
            return capturedStatements;
        }

        /**
         * <p>
         * Returns the computed heap.
         * </p>
         *
         * @return the heap
         */
        public NutsHeap getHeap() {
            return heap;
        }

        /**
         * <p>
         * Add this info to the given list if and only if some statements have been captured
         * </p>
         *
         * @param list the list
         */
        public void addTo(final List<ParseInfo> list) {
            if (!capturedStatements.isEmpty()) {
                list.add(this);
            }
        }
    }

    /**
     * <p>
     * Parses the given HTML content and returns all the information collected during the operation.
     * </p>
     *
     * <p>
     * When a script is referenced, the given {@link NutDao} will be used to retrieve the corresponding
     * {@link ConvertibleNut}.
     * </p>
     *
     * @param content the HTML content to parse
     * @param dao the DAO
     * @param nutName the nut name
     * @return the parsed HTML
     * @throws WuicException if WUIC fails to configure context or process created workflow
     */
    private List<ParseInfo> parse(final String nutName, final String content, final NutDao dao, final String rootPath) throws WuicException {
        // Create a proxy that maps inline scripts
        final ProxyNutDao proxy = new ProxyNutDao(rootPath, dao);

        // Create the matcher from the given content, we will keep in an integer the end position of group that  previously matched
        final Matcher matcher = PATTERN.matcher(content);
        int previousGroupEnd = -1;

        // All the paths we have currently collected
        final Map<String, Integer> paths = new LinkedHashMap<String, Integer>();
        final List<ParseInfo> retval = new ArrayList<ParseInfo>();
        int no = 0;

        // Finds desired groups
        while (matcher.find()) {

            // There is something to parse
            if (!matcher.group().trim().isEmpty()) {

                /*
                 * We've already matched some scripts and there is something (excluding comment and whitespace) between
                 * the previous script and the script currently matched that implies that they should not be imported
                 * together. Consequently, we create here a separate heap.
                 */
                if (previousGroupEnd != -1 && !content.substring(previousGroupEnd + 1, matcher.start()).trim().isEmpty()) {
                    new ParseInfo(nutName + (no++), paths, proxy, rootPath).addTo(retval);
                    paths.clear();
                }

                // Now find the appropriate parser
                for (final Integer groupPosition : PARSERS.keySet()) {

                    // Test value at the associated group position to find the appropriate parser
                    if (matcher.group(groupPosition) != null) {
                        paths.put(matcher.group(), groupPosition);
                        break;
                    }
                }

                previousGroupEnd = matcher.end();
            }
        }

        // Create a heap for remaining paths
        if (!paths.isEmpty()) {
            new ParseInfo(nutName + (no), paths, proxy, rootPath).addTo(retval);
        }

        return retval;
    }

    /**
     * <p>
     * Transforms the given HTML content and returns the replacements done with the collected parse information.
     * </p>
     *
     * @param nut the HTML content to parse
     * @param request the request
     * @return the nut wrapping parsed HTML
     * @throws WuicException if WUIC fails to configure context or process created workflow
     */
    public ConvertibleNut transformHtml(final ConvertibleNut nut,
                                        final String contextPath,
                                        final EngineRequest request)
            throws WuicException, IOException {

        nut.addTransformer(new HtmlTransformer(request, contextPath));

        return nut;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NutType> getNutTypes() {
        return Arrays.asList(NutType.HTML);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EngineType getEngineType() {
        return EngineType.INSPECTOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean works() {
        return doInspection;
    }
}