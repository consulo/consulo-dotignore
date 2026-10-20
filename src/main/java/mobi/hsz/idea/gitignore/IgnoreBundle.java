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

import consulo.dotignore.git.GitOuterFileFetchers;
import consulo.dotignore.lang.DotIgnoreLanguage;
import consulo.language.ignore.IgnoreFileType;
import consulo.language.ignore.IgnoreLanguage;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.util.collection.ContainerUtil;
import consulo.versionControlSystem.change.IgnoredFileContentProvider;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import mobi.hsz.idea.gitignore.outer.OuterFileFetcher;
import mobi.hsz.idea.gitignore.settings.IgnoreSettings;
import mobi.hsz.idea.gitignore.util.CachedConcurrentMap;
import mobi.hsz.idea.gitignore.util.ExpiringMap;
import mobi.hsz.idea.gitignore.util.Utils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Discovery of the ignore kinds: every {@link IgnoreFileType} registered by any plugin (platform, VCS plugins,
 * the .ignore plugin itself). The .ignore plugin features work over these kinds and never reference
 * a kind class it does not own.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.4
 */
public class IgnoreBundle {
    private IgnoreBundle() {
    }

    /**
     * Git ignore file name.
     */
    public static final String GITIGNORE = ".gitignore";

    /**
     * Git directory name.
     */
    public static final String DOT_GIT = ".git";

    /**
     * Enabled languages cache.
     */
    public static final CachedConcurrentMap<IgnoreFileType, Boolean> ENABLED_LANGUAGES =
        CachedConcurrentMap.create(key -> isEnabled(key.getIgnoreLanguage()));

    /**
     * Outer files cache.
     */
    private static final ExpiringMap<Integer, Set<VirtualFile>> OUTER_FILES = new ExpiringMap<>(5000);

    /**
     * Registered ignore kinds, file types are registered once on startup.
     */
    private static volatile @Nullable List<IgnoreLanguage> ourLanguages;

    /**
     * Ignore kinds owned by a VCS, see {@link #findContentProvider(IgnoreLanguage)}.
     */
    private static final Map<IgnoreLanguage, Boolean> OUR_VCS_OWNED = new ConcurrentHashMap<>();

    /**
     * Returns all registered ignore kinds.
     *
     * @return ignore languages
     */
    public static List<IgnoreLanguage> getLanguages() {
        List<IgnoreLanguage> languages = ourLanguages;
        if (languages == null) {
            languages = collectLanguages();
            ourLanguages = languages;
        }
        return languages;
    }

    private static List<IgnoreLanguage> collectLanguages() {
        Set<IgnoreLanguage> languages = new LinkedHashSet<>();
        for (FileType fileType : FileTypeRegistry.getInstance().getRegisteredFileTypes()) {
            if (fileType instanceof IgnoreFileType ignoreFileType && ignoreFileType.getIgnoreLanguage() != IgnoreLanguage.INSTANCE) {
                languages.add(ignoreFileType.getIgnoreLanguage());
            }
        }
        List<IgnoreLanguage> result = new ArrayList<>(languages);
        result.sort(Comparator.comparing(IgnoreLanguage::getID, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(result);
    }

    /**
     * Returns all registered ignore kinds related to the VCS.
     *
     * @return VCS ignore languages
     */
    public static List<IgnoreLanguage> getVcsLanguages() {
        return ContainerUtil.filter(getLanguages(), IgnoreBundle::isVCS);
    }

    /**
     * Finds the ignore kind by its ID.
     *
     * @param id language ID
     * @return ignore language or <code>null</code>
     */
    public static @Nullable IgnoreLanguage findLanguage(String id) {
        for (IgnoreLanguage language : getLanguages()) {
            if (id.equals(language.getID())) {
                return language;
            }
        }
        return null;
    }

    /**
     * Returns the ignore kind of the given file.
     *
     * @param file file to check
     * @return ignore language or <code>null</code>
     */
    public static @Nullable IgnoreLanguage obtainLanguage(VirtualFile file) {
        if (file.getFileType() instanceof IgnoreFileType ignoreFileType) {
            return ignoreFileType.getIgnoreLanguage();
        }
        String filename = file.getName();
        for (IgnoreLanguage language : getLanguages()) {
            if (language.getFilename().equals(filename)) {
                return language;
            }
        }
        return null;
    }

    /**
     * Returns the ignore kind registered for {@code .gitignore} files, if any.
     *
     * @return gitignore file type
     */
    public static @Nullable IgnoreFileType findGitIgnoreFileType() {
        return FileTypeRegistry.getInstance().getFileTypeByFileName(GITIGNORE) instanceof IgnoreFileType fileType ? fileType : null;
    }

    /**
     * Ignore kinds of the .ignore plugin say themselves whether they belong to a VCS, other kinds belong to a VCS
     * when a VCS plugin provides the content of that ignore file.
     *
     * @param language ignore language
     * @return language is related to the VCS
     */
    public static boolean isVCS(IgnoreLanguage language) {
        if (language instanceof DotIgnoreLanguage dotIgnoreLanguage) {
            return dotIgnoreLanguage.isVCS();
        }
        return isOwnedByVcs(language);
    }

    /**
     * Ignore kinds owned by a VCS plugin get their "Add to ignore file" actions and ignored status from the VCS.
     *
     * @param language ignore language
     * @return a VCS plugin provides the content of that ignore file
     */
    public static boolean isOwnedByVcs(IgnoreLanguage language) {
        if (language instanceof DotIgnoreLanguage) {
            return false;
        }
        return OUR_VCS_OWNED.computeIfAbsent(language, it -> findContentProvider(it) != null);
    }

    /**
     * Returns the {@link IgnoredFileContentProvider} that owns the given ignore kind.
     *
     * @param language ignore language
     * @return provider or <code>null</code> if the kind is not owned by a VCS
     */
    public static @Nullable IgnoredFileContentProvider findContentProvider(IgnoreLanguage language) {
        Project project = ProjectManager.getInstance().getDefaultProject();
        String filename = language.getFilename();
        return project.getExtensionPoint(IgnoredFileContentProvider.class).findFirstSafe(it -> filename.equals(it.getFileName()));
    }

    /**
     * Returns Ignore VCS directory name.
     *
     * @param language ignore language
     * @return VCS directory name
     */
    public static @Nullable String getVcsDirectory(IgnoreLanguage language) {
        return language instanceof DotIgnoreLanguage dotIgnoreLanguage ? dotIgnoreLanguage.getVcsDirectory() : null;
    }

    /**
     * Returns fixed directory for the given {@link IgnoreLanguage}.
     *
     * @param project  current project
     * @param language ignore language
     * @return fixed directory
     */
    public static @Nullable VirtualFile getFixedDirectory(Project project, IgnoreLanguage language) {
        if (language instanceof DotIgnoreLanguage dotIgnoreLanguage) {
            return dotIgnoreLanguage.getFixedDirectory(project);
        }
        if (Utils.isGitPluginEnabled()) {
            return GitOuterFileFetchers.getFixedDirectory(project, language);
        }
        return null;
    }

    /**
     * Returns outer files fetchers of the given ignore kind.
     *
     * @param language ignore language
     * @return fetchers
     */
    public static List<OuterFileFetcher> getOuterFileFetchers(IgnoreLanguage language) {
        List<OuterFileFetcher> fetchers = new ArrayList<>();
        if (language instanceof DotIgnoreLanguage dotIgnoreLanguage) {
            ContainerUtil.addAll(fetchers, dotIgnoreLanguage.getOuterFileFetchers());
        }
        if (Utils.isGitPluginEnabled()) {
            fetchers.addAll(GitOuterFileFetchers.forLanguage(language));
        }
        return fetchers;
    }

    /**
     * Returns <code>true</code> if outer files are supported.
     *
     * @param language ignore language
     * @return supports outer files
     */
    public static boolean isOuterFileSupported(IgnoreLanguage language) {
        return !getOuterFileFetchers(language).isEmpty();
    }

    /**
     * Returns outer files for the current language.
     *
     * @param project  current project
     * @param language ignore language
     * @return outer files
     */
    public static Set<VirtualFile> getOuterFiles(Project project, IgnoreLanguage language) {
        int key = Objects.hash(project, language.getFileType());
        Set<VirtualFile> files = OUTER_FILES.get(key);
        if (files == null) {
            files = new HashSet<>();
            for (OuterFileFetcher fetcher : getOuterFileFetchers(language)) {
                ContainerUtil.addAllNotNull(files, fetcher.fetch(project));
            }
            OUTER_FILES.set(key, files);
        }
        return OUTER_FILES.getOrElse(key, new HashSet<>());
    }

    /**
     * Checks is language is enabled or with {@link IgnoreSettings}.
     *
     * @param language ignore language
     * @return language is enabled
     */
    public static boolean isEnabled(IgnoreLanguage language) {
        Map<IgnoreSettings.IgnoreLanguagesSettings.KEY, Object> data = IgnoreSettings.getInstance().getLanguagesSettings().get(language);
        return data != null && Boolean.parseBoolean(String.valueOf(data.get(IgnoreSettings.IgnoreLanguagesSettings.KEY.ENABLE)));
    }

    /**
     * Checks if creating new file for given language is allowed with the settings.
     *
     * @param language ignore language
     * @return new file is allowed
     */
    public static boolean isNewAllowed(IgnoreLanguage language) {
        Map<IgnoreSettings.IgnoreLanguagesSettings.KEY, Object> data = IgnoreSettings.getInstance().getLanguagesSettings().get(language);
        return data != null && Boolean.parseBoolean(String.valueOf(data.get(IgnoreSettings.IgnoreLanguagesSettings.KEY.NEW_FILE)));
    }

    /**
     * Returns outer files of all registered kinds.
     *
     * @param project current project
     * @return outer files
     */
    public static Collection<VirtualFile> getAllOuterFiles(Project project) {
        Set<VirtualFile> result = new HashSet<>();
        for (IgnoreLanguage language : getLanguages()) {
            result.addAll(getOuterFiles(project, language));
        }
        return result;
    }
}
