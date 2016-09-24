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


package com.github.wuic.path.core;

import com.github.wuic.path.DirectoryPath;
import com.github.wuic.util.TemporaryFileManager;

import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * <p>
 * This {@link com.github.wuic.path.DirectoryPath} represents an entry inside a ZIP archive identified as a directory.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.3.4
 */
public class ZipEntryDirectoryPath extends ZipDirectoryPath implements DirectoryPath {

    /**
     * Entry path.
     */
    private ZipEntryPath entryPath;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param manager the temporary file manager
     * @param charset the charset
     * @param entry the ZIP entry name
     * @param parent the parent
     */
    public ZipEntryDirectoryPath(final TemporaryFileManager manager, final String entry, final DirectoryPath parent, final String charset) {
        super(entry, parent, charset, manager);
        entryPath = new ZipEntryPath(entry, parent, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] list() throws IOException {
        final ZipEntryPath.ArchiveWithParentEntry zip = entryPath.findZipArchive("");
        final ZipFilePath archive = zip.getArchive();
        return archive.list(zip.getEntryPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ZipFile getZipFile() throws IOException {
        final ZipEntryPath.ArchiveWithParentEntry zip = entryPath.findZipArchive("");
        return new ZipFile(zip.getArchive().getRawFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String absoluteEntryOf(final String child) throws IOException {
        return entryPath.findZipArchive(child).getEntryPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastUpdate() throws IOException {
        return entryPath.getLastUpdate();
    }
}
