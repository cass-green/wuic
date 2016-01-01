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
 * @version 1.4
 * @since 0.1.0
 */
public class WuicTag extends TagSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4305181623848741300L;

    /**
     * The page name.
     */
    private String workflowId;

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
            } else {
                out.print("<wuic:html-import workflowId='");
                out.print(workflowId);
                out.println("'/>");
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
}
