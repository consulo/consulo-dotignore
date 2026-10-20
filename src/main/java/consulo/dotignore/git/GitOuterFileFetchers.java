/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.dotignore.git;

import consulo.language.ignore.IgnoreFileType;
import consulo.language.ignore.IgnoreLanguage;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryFiles;
import mobi.hsz.idea.gitignore.outer.OuterFileFetcher;
import mobi.hsz.idea.gitignore.util.exec.ExternalExec;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Outer files of the Git ignore kinds: the global {@code core.excludesfile} and the {@code .git/info/exclude} files.
 * The kinds are found by the file names they are registered for, so no Git kind class is referenced.
 * Must be touched only when the Git plugin is enabled.
 *
 * @author VISTALL
 * @since 2026-10-20
 */
public final class GitOuterFileFetchers {
    private static final String EXCLUDE = "exclude";

    private static final String INFO = "info";

    private GitOuterFileFetchers() {
    }

    public static List<OuterFileFetcher> forLanguage(IgnoreLanguage language) {
        if (isKindOf(language, GitRepositoryFiles.GITIGNORE)) {
            return List.of(
                project -> ContainerUtil.createMaybeSingletonList(ExternalExec.getGitExcludesFile()),
                GitOuterFileFetchers::fetchExcludeFiles
            );
        }
        if (isKindOf(language, EXCLUDE)) {
            return List.of(GitOuterFileFetchers::fetchExcludeFiles);
        }
        return List.of();
    }

    public static @Nullable VirtualFile getFixedDirectory(Project project, IgnoreLanguage language) {
        if (!isKindOf(language, EXCLUDE)) {
            return null;
        }
        VirtualFile baseDir = project.getBaseDir();
        return baseDir != null ? baseDir.findFileByRelativePath(GitUtil.DOT_GIT + "/" + INFO) : null;
    }

    private static Collection<VirtualFile> fetchExcludeFiles(Project project) {
        List<VirtualFile> files = new ArrayList<>();
        for (GitRepository repository : GitUtil.getRepositories(project)) {
            ContainerUtil.addIfNotNull(files, LocalFileSystem.getInstance().findFileByIoFile(repository.getRepositoryFiles().getExcludeFile()));
        }
        return files;
    }

    private static boolean isKindOf(IgnoreLanguage language, String fileName) {
        FileType fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(fileName);
        return fileType instanceof IgnoreFileType ignoreFileType && ignoreFileType.getIgnoreLanguage() == language;
    }
}
