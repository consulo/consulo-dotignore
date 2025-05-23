/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 hsz Jakub Chrzanowski <jakub@hsz.mobi>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package mobi.hsz.idea.gitignore.vcs;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.document.Document;
import consulo.dotignore.localize.IgnoreLocalize;
import consulo.project.Project;
import consulo.ui.style.StandardColors;
import consulo.util.lang.ThreeState;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.status.FileStatus;
import consulo.virtualFileSystem.status.FileStatusFactory;
import consulo.virtualFileSystem.status.FileStatusProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import mobi.hsz.idea.gitignore.IgnoreManager;

/**
 * Ignore instance of {@link FileStatusProvider} that provides {@link #IGNORED} status
 * for the files matched with ignore rules.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.0
 */
@ExtensionImpl
public class IgnoreFileStatusProvider implements FileStatusProvider, DumbAware {
    /** Ignored status. */
    public static final FileStatus IGNORED = FileStatusFactory.getInstance().createFileStatus(
        "IGNORE.PROJECT_VIEW.IGNORED",
        IgnoreLocalize.projectviewIgnored(),
        StandardColors.GRAY
    );

    /** Instance of {@link IgnoreManager}. */
    private final IgnoreManager ignoreManager;

    @Inject
    public IgnoreFileStatusProvider(@Nonnull Project project) {
        this.ignoreManager = IgnoreManager.getInstance(project);
    }

    /**
     * Returns the {@link #IGNORED} status if file is ignored or <code>null</code>.
     *
     * @param virtualFile file to check
     * @return {@link #IGNORED} status or <code>null</code>
     */
    @Nullable
    @Override
    public FileStatus getFileStatus(@Nonnull VirtualFile virtualFile) {
        return ignoreManager.isFileIgnored(virtualFile) && !ignoreManager.isFileTracked(virtualFile) ? IGNORED : null;
    }
}
