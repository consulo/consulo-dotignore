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

package mobi.hsz.idea.gitignore.util;

import consulo.language.ignore.cache.PatternCache;
import consulo.language.ignore.psi.IgnoreEntry;
import consulo.language.ignore.util.RegexUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Glob util class that prepares glob statements or searches for content using glob rules.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.5
 */
public class Glob {
    /**
     * Private constructor to prevent creating {@link Glob} instance.
     */
    private Glob() {
    }

    /**
     * Finds for {@link VirtualFile} list using glob rule in given root directory.
     *
     * @param root  root directory
     * @param entry ignore entry
     * @return search result
     */
    @Nullable
    public static VirtualFile findOne(
        @Nonnull VirtualFile root,
        @Nonnull IgnoreEntry entry,
        @Nonnull PatternCache patternCache
    ) {
        List<VirtualFile> files = find(root, ContainerUtil.newArrayList(entry), patternCache, false).get(entry);
        return ContainerUtil.getFirstItem(files);
    }

    /**
     * Finds for {@link VirtualFile} list using glob rule in given root directory.
     *
     * @param root          root directory
     * @param entries       ignore entries
     * @param includeNested attach children to the search result
     * @return search result
     */
    @Nonnull
    public static Map<IgnoreEntry, List<VirtualFile>> find(
        @Nonnull VirtualFile root,
        @Nonnull List<IgnoreEntry> entries,
        @Nonnull PatternCache patternCache,
        boolean includeNested
    ) {
        ConcurrentMap<IgnoreEntry, List<VirtualFile>> result = ContainerUtil.newConcurrentMap();
        HashMap<IgnoreEntry, Pattern> map = new HashMap<>();

        for (IgnoreEntry entry : entries) {
            result.put(entry, new ArrayList<>());

            Pattern pattern = patternCache.createPattern(entry);
            if (pattern != null) {
                map.put(entry, pattern);
            }
        }

        VirtualFileVisitor<HashMap<IgnoreEntry, Pattern>> visitor = new VirtualFileVisitor<>(VirtualFileVisitor.NO_FOLLOW_SYMLINKS) {
            @Override
            public boolean visitFile(@Nonnull VirtualFile file) {
                HashMap<IgnoreEntry, Pattern> current = new HashMap<>(getCurrentValue());
                if (current.isEmpty()) {
                    return false;
                }

                String path = Utils.getRelativePath(root, file);
                if (path == null || Utils.isVcsDirectory(file)) {
                    return false;
                }

                for (Map.Entry<IgnoreEntry, Pattern> item : current.entrySet()) {
                    Pattern value = item.getValue();
                    boolean matches = false;
                    if (value == null || RegexUtil.match(value, path)) {
                        matches = true;
                        result.get(item.getKey()).add(file);
                    }
                    if (includeNested && matches) {
                        current.put(item.getKey(), null);
                    }
                }

                setValueForChildren(current);
                return true;
            }
        };
        visitor.setValueForChildren(map);
        VirtualFileUtil.visitChildrenRecursively(root, visitor);

        return result;
    }

    /**
     * Finds for {@link VirtualFile} paths list using glob rule in given root directory.
     *
     * @param root          root directory
     * @param entries       ignore entry
     * @param includeNested attach children to the search result
     * @return search result
     */
    @Nonnull
    public static Map<IgnoreEntry, Set<String>> findAsPaths(
        @Nonnull VirtualFile root,
        @Nonnull List<IgnoreEntry> entries,
        @Nonnull PatternCache patternCache,
        boolean includeNested
    ) {
        Map<IgnoreEntry, Set<String>> result = new HashMap<>();

        Map<IgnoreEntry, List<VirtualFile>> files = find(root, entries, patternCache, includeNested);
        for (Map.Entry<IgnoreEntry, List<VirtualFile>> item : files.entrySet()) {
            Set<String> set = new HashSet<>();
            for (VirtualFile file : item.getValue()) {
                set.add(Utils.getRelativePath(root, file));
            }
            result.put(item.getKey(), set);
        }

        return result;
    }
}
