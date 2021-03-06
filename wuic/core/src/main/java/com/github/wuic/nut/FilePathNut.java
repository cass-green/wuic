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
import com.github.wuic.path.FilePath;
import com.github.wuic.path.FsItem;
import com.github.wuic.util.Input;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * <p>
 * Represents a nut on the path system provided or to be managed by the WUIC framework. Thanks to
 * {@link FilePath}, the nut could also be a ZIP entry.
 * </p>
 *
 * @author Guillaume DROUET
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
    public Input openStream() throws IOException {
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
