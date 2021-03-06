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


package com.github.wuic.tag;

import com.github.wuic.WuicFacade;
import com.github.wuic.context.ContextBuilderConfigurator;
import com.github.wuic.exception.WuicException;
import com.github.wuic.servlet.HttpUtil;
import com.github.wuic.servlet.ServletProcessContext;
import com.github.wuic.servlet.WuicServletContextListener;
import com.github.wuic.config.bean.xml.ReaderXmlContextBuilderConfigurator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.bind.JAXBException;

/**
 * <p>
 * This tag evaluates the XML configuration described in the body-content and injects it to the global configuration.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.2
 */
public class WuicXmlConfigurationTag extends BodyTagSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4305181623848741300L;

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
     * <p>
     * Can creates heaps on the fly based on given body content.
     * </p>
     *
     * @throws JspException if an I/O error occurs
     */
    @Override
    public int doAfterBody() throws JspException {
        try {
            // Let's load the wuic.xml file and configure the builder with it
            final BodyContent content = getBodyContent();
            final ContextBuilderConfigurator c = new ReaderXmlContextBuilderConfigurator.Simple(
                    content.getReader(),
                    HttpUtil.INSTANCE.computeUniqueTag(HttpServletRequest.class.cast(pageContext.getRequest())),
                    wuicFacade.allowsMultipleConfigInTagSupport(),
                    new ServletProcessContext(HttpServletRequest.class.cast(pageContext.getRequest())));

            wuicFacade.configure(c);
            content.clearBody();
        } catch (WuicException we) {
            throw new JspException(we);
        } catch (JAXBException se) {
            throw new JspException("Body content is not a valid XML to describe WUIC configuration", se);
        }

        return SKIP_BODY;
    }
}
