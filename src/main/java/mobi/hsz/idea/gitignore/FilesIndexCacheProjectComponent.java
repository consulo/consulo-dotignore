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

package mobi.hsz.idea.gitignore;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.content.FileIndex;
import consulo.disposer.Disposable;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import mobi.hsz.idea.gitignore.util.Constants;
import mobi.hsz.idea.gitignore.util.MatcherUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Cache that retrieves matching files using given {@link Pattern}.
 * It uses {@link VirtualFileListener} to handle changes in the files tree and clear cached entries
 * for the specific pattern parts.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.3.1
 */
@ServiceAPI(value = ComponentScope.PROJECT, lazy = false)
@ServiceImpl
@Singleton
public class FilesIndexCacheProjectComponent implements Disposable {
    /** Concurrent cache map. */
    @NotNull
    private final ConcurrentMap<String, Collection<VirtualFile>> cacheMap;

    /** {@link VirtualFileManager} instance. */
    @NotNull
    private final VirtualFileManager virtualFileManager;

    /** {@link FileIndex} instance. */
    @NotNull
    private final FileIndex projectFileIndex;

    /** {@link VirtualFileListener} instance to watch for operations on the filesystem. */
    @NotNull
    private final VirtualFileListener virtualFileListener = new VirtualFileListener() {
        @Override
        public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
            if (event.getPropertyName().equals("name")) {
                removeAffectedCaches(event);
            }
        }

        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
            removeAffectedCaches(event);
        }

        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            removeAffectedCaches(event);
        }

        @Override
        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            removeAffectedCaches(event);
        }

        @Override
        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            removeAffectedCaches(event);
        }

        @Override
        public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
            removeAffectedCaches(event);
        }

        private void removeAffectedCaches(@NotNull VirtualFileEvent event) {
            for (String key : cacheMap.keySet()) {
                List<String> parts = StringUtil.split(key, Constants.DOLLAR);
                if (MatcherUtil.matchAnyPart(parts.toArray(new String[0]), event.getFile().getPath())) {
                    cacheMap.remove(key);
                }
            }
        }
    };

    /**
     * Returns {@link FilesIndexCacheProjectComponent} service instance.
     *
     * @param project current project
     * @return {@link FilesIndexCacheProjectComponent instance}
     */
    public static FilesIndexCacheProjectComponent getInstance(@NotNull final Project project) {
        return project.getComponent(FilesIndexCacheProjectComponent.class);
    }

    /**
     * Initializes {@link #cacheMap} and {@link VirtualFileManager}.
     *
     * @param project current project
     */
    @Inject
    public FilesIndexCacheProjectComponent(@NotNull final Project project) {
        cacheMap = ContainerUtil.newConcurrentMap();
        virtualFileManager = VirtualFileManager.getInstance();
        projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        virtualFileManager.addVirtualFileListener(virtualFileListener);
    }

    /**
     * Finds {@link VirtualFile} instances for the specific {@link Pattern} and caches them.
     *
     * @param project current project
     * @param pattern to handle
     * @return matched files list
     */
    @NotNull
    public Collection<VirtualFile> getFilesForPattern(@NotNull final Project project, @NotNull Pattern pattern) {
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final String[] parts = MatcherUtil.getParts(pattern);

        if (parts.length > 0) {
            final String key = StringUtil.join(parts, Constants.DOLLAR);
            if (cacheMap.get(key) == null) {
                final Set<VirtualFile> files = new HashSet<>(1000);

                projectFileIndex.iterateContent(fileOrDir -> {
                    final String name = fileOrDir.getName();
                    if (MatcherUtil.matchAnyPart(parts, name)) {
                        for (VirtualFile file : FilenameIndex.getVirtualFilesByName(project, name, scope)) {
                            if (file.isValid() && MatcherUtil.matchAllParts(parts, file.getPath())) {
                                files.add(file);
                            }
                        }
                    }
                    return true;
                });

                cacheMap.put(key, files);
            }

            return cacheMap.get(key);
        }

        return Set.of();
    }

    @Override
    public void dispose() {
        virtualFileManager.removeVirtualFileListener(virtualFileListener);
        cacheMap.clear();
    }
}
