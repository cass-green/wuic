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


package com.github.wuic.tag;

import com.github.wuic.WuicFacade;
import com.github.wuic.exception.WuicException;
import com.github.wuic.servlet.ServletProcessContext;
import com.github.wuic.servlet.WuicServletContextListener;
import com.github.wuic.nut.ConvertibleNut;
import com.github.wuic.servlet.HtmlParserFilter;
import com.github.wuic.util.HtmlUtil;
import com.github.wuic.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>
 * This tag writes the scripts of the page specified to it attributes.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1.0
 */
public class WuicTag extends TagSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4305181623848741300L;

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The page name.
     */
    private String workflowId;

    /**
     * Breaks the aggregation.
     */
    private String breakAggregation;

    /**
     * The WUIC facade.
     */
    private WuicFacade wuicFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageContext(final PageContext pageContext) {
        super.setPageContext(pageContext);
        wuicFacade = WuicServletContextListener.getWuicFacade(pageContext.getServletContext());
    }

    /**
     * <p>
     * Includes according to the page name.
     * </p>
     * 
     * <p>
     * Only CSS and Javascript files could be imported.
     * </p>
     * 
     * @throws JspException if an I/O error occurs
     * @return the {@code SKIP_BODY} code
     */
    @Override
    public int doStartTag() throws JspException {
        try {
            log.debug("Process JSP tag for workflow {}.", workflowId);
            pageContext.getRequest().setAttribute(HtmlParserFilter.FORCE_DYNAMIC_CONTENT, "");

            if (wuicFacade.allowsMultipleConfigInTagSupport()) {
                wuicFacade.clearTag(workflowId);
            }

            final JspWriter out = pageContext.getOut();

            if (pageContext.getRequest().getAttribute(HtmlParserFilter.class.getName()) == null) {
                final List<ConvertibleNut> nuts = wuicFacade.runWorkflow(workflowId, new ServletProcessContext(HttpServletRequest.class.cast(pageContext.getRequest())));

                for (final ConvertibleNut nut : nuts) {
                    out.println(HtmlUtil.writeScriptImport(nut, IOUtils.mergePath(wuicFacade.getContextPath(), workflowId)));
                }

                if (breakAggregation != null) {
                    log.warn("breakAggregation attribute has bean specified for the import of workflow {} but will be ignored because the page is not filtered by",
                            workflowId, HtmlParserFilter.class.getName());
                }
            } else {
                out.print("<wuic:html-import workflowId='");
                out.print(workflowId);
                out.print("'");

                if (breakAggregation != null) {
                    out.print(" data-wuic-break");
                }

                out.println("/>");
            }

        } catch (IOException ioe) {
            throw new JspException("Can't write import statements into JSP output stream", ioe);
        } catch (WuicException we) {
            throw new JspException(we);
        }
        
        return SKIP_BODY;
    }

    /**
     * <p>
     * Returns the workflow IDs.
     * </p>
     * 
     * @return the page name
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     * <p>
     * Sets the workflow IDs.
     * </p>
     * 
     * @param page the workflow IDs
     */
    public void setWorkflowId(final String page) {
        this.workflowId = page;
    }

    /**
     * <p>
     * Gets the break flag.
     * </p>
     *
     * @return the break flag
     */
    public String getBreakAggregation() {
        return breakAggregation;
    }

    /**
     * <p>
     * Sets the break flag.
     * </p>
     *
     * @param breakAggregation the break flag
     */
    public void setBreakAggregation(final String breakAggregation) {
        this.breakAggregation = breakAggregation;
    }
}
