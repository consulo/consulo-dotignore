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

package mobi.hsz.idea.gitignore.codeInspection;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.dotignore.codeInspection.IgnoreInspection;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReferenceOwner;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import mobi.hsz.idea.gitignore.FilesIndexCacheProjectComponent;
import mobi.hsz.idea.gitignore.IgnoreBundle;
import mobi.hsz.idea.gitignore.IgnoreManager;
import mobi.hsz.idea.gitignore.psi.IgnoreEntry;
import mobi.hsz.idea.gitignore.psi.IgnoreVisitor;
import mobi.hsz.idea.gitignore.util.Glob;
import mobi.hsz.idea.gitignore.util.Utils;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Inspection tool that checks if entries are unused - does not cover any file or directory.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.5
 */
@ExtensionImpl
public class IgnoreUnusedEntryInspection extends IgnoreInspection {
    @Nonnull
    @Override
    public String getDisplayName() {
        return IgnoreBundle.message("codeInspection.unusedEntry");
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    /**
     * Checks if entries are related to any file.
     *
     * @param holder     where visitor will register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return not-null visitor for this inspection
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        final Project project = holder.getProject();
        final FilesIndexCacheProjectComponent cache = FilesIndexCacheProjectComponent.getInstance(project);
        final IgnoreManager manager = IgnoreManager.getInstance(project);

        return new IgnoreVisitor() {
            @Override
            public void visitEntry(@NotNull IgnoreEntry entry) {
                PsiReference[] references = entry.getReferences();
                boolean resolved = true;
                int previous = Integer.MAX_VALUE;
                for (PsiReference reference : references) {
                    ProgressManager.checkCanceled();
                    if (reference instanceof FileReferenceOwner) {
                        PsiPolyVariantReference fileReference = (PsiPolyVariantReference) reference;
                        ResolveResult[] result = fileReference.multiResolve(false);
                        resolved = result.length > 0 || (previous > 0 && reference.getCanonicalText().endsWith("/*"));
                        previous = result.length;
                    }
                    if (!resolved) {
                        break;
                    }
                }

                if (!resolved) {
                    if (!isEntryExcluded(entry, holder.getProject())) {
                        holder.registerProblem(entry, IgnoreBundle.message("codeInspection.unusedEntry.message"),
                                new IgnoreRemoveEntryFix(entry));
                    }
                }

                super.visitEntry(entry);
            }

            /**
             * Checks if given {@link IgnoreEntry} is excluded in the current {@link Project}.
             *
             * @param entry   Gitignore entry
             * @param project current project
             * @return entry is excluded in current project
             */
            private boolean isEntryExcluded(@NotNull IgnoreEntry entry, @NotNull Project project) {
                final Pattern pattern = Glob.createPattern(entry);
                if (pattern == null) {
                    return false;
                }

                final VirtualFile projectRoot = project.getBaseDir();
                final List<VirtualFile> matched = ContainerUtil.newArrayList();
                final Collection<VirtualFile> files = cache.getFilesForPattern(project, pattern);

                if (projectRoot == null) {
                    return false;
                }

                for (final VirtualFile root : Utils.getExcludedRoots(project)) {
                    for (VirtualFile file : files) {
                        ProgressManager.checkCanceled();
                        if (!Utils.isUnder(file, root)) {
                            continue;
                        }
                        String path = Utils.getRelativePath(projectRoot, root);
                        if (manager.getMatcher().match(pattern, path)) {
                            matched.add(file);
                            return false;
                        }
                    }

                    if (matched.size() > 0) {
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
