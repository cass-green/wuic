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

package com.github.wuic.nut;

import com.github.wuic.NutType;
import com.github.wuic.path.FilePath;
import com.github.wuic.path.FsItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * <p>
 * Represents a nut on the path system provided or to be managed by the WUIC framework. Thanks to
 * {@link FilePath}, the nut could also be a ZIP entry.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.6
 * @since 0.1.1
 */
public class FilePathNut extends AbstractNut {

    /**
     * The root directory that contains the files of a same group.
     */
    private FilePath path;

    /**
     * <p>
     * Builds a new {@code Nut} based on a given path.
     * </p>
     *
     * @param p the path
     * @param name the nut name
     * @param ft the path type
     * @param versionNumber the nut's version number
     */
    public FilePathNut(final FilePath p, final String name, final NutType ft, final Future<Long> versionNumber) {
        super(name, ft, versionNumber);
        path = p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openStream() throws IOException {
        return path.openStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentFile() {
        if (path instanceof FsItem) {
            final File f = FsItem.class.cast(path).getFile();
            final String absPath = f.getAbsolutePath();

            if (absPath.length() > getInitialName().length()) {
                return absPath.substring(0, absPath.length() - getInitialName().length());
            }
        }

        return null;
    }
}
