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

package mobi.hsz.idea.gitignore.daemon;

import consulo.application.AllIcons;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.language.Language;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import mobi.hsz.idea.gitignore.IgnoreManager;
import mobi.hsz.idea.gitignore.psi.IgnoreEntryDirectory;
import mobi.hsz.idea.gitignore.psi.IgnoreEntryFile;
import mobi.hsz.idea.gitignore.util.Glob;
import mobi.hsz.idea.gitignore.util.MatcherUtil;
import mobi.hsz.idea.gitignore.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.annotation.Nonnull;
import java.util.HashMap;

/**
 * {@link LineMarkerProvider} that marks entry lines with directory icon if they point to the directory in virtual
 * system.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.5
 */
public class IgnoreDirectoryMarkerProvider implements LineMarkerProvider {
    /** Cache map. */
    private final HashMap<String, Boolean> cache = new HashMap<>();

    private final Language language;

    public IgnoreDirectoryMarkerProvider(Language language) {
        this.language = language;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns {@link LineMarkerInfo} with set {@link PlatformIcons#FOLDER_ICON} if entry points to the directory.
     *
     * @param element current element
     * @return <code>null</code> if entry is not a directory
     */
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof IgnoreEntryFile)) {
            return null;
        }

        boolean isDirectory = element instanceof IgnoreEntryDirectory;

        if (!isDirectory) {
            final String key = element.getText();
            if (cache.containsKey(key)) {
                isDirectory = cache.get(key);
            } else {
                final IgnoreEntryFile entry = (IgnoreEntryFile) element;
                final VirtualFile parent = element.getContainingFile().getVirtualFile().getParent();
                final Project project = element.getProject();
                final VirtualFile projectDir = project.getBaseDir();
                if (parent == null || projectDir == null || !Utils.isUnder(parent, projectDir)) {
                    return null;
                }
                final MatcherUtil matcher = IgnoreManager.getInstance(project).getMatcher();
                final VirtualFile file = Glob.findOne(parent, entry, matcher);
                cache.put(key, isDirectory = file != null && file.isDirectory());
            }
        }

        if (isDirectory) {
            return new LineMarkerInfo<>(element.getFirstChild(), element.getTextRange(), AllIcons.Nodes.TreeOpen, Pass.UPDATE_ALL, null, null,
                    GutterIconRenderer.Alignment.CENTER);
        }
        return null;
    }
}
