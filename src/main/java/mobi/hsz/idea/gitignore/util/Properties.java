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

import consulo.component.PropertiesComponent;
import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Properties} util class that holds project specified settings using {@link PropertiesComponent}.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.3.3
 */
public class Properties {
    /** Ignore missing gitignore property key. */
    private static final String IGNORE_MISSING_GITIGNORE = "ignore_missing_gitignore";

    /** Add unversioned files property key. */
    private static final String ADD_UNVERSIONED_FILES = "add_unversioned_files";

    /** Dismissed ignored editing notification key. */
    private static final String DISMISSED_IGNORED_EDITING_NOTIFICATION = "add_unversioned_files";

    /** Private constructor to prevent creating {@link Properties} instance. */
    private Properties() {
    }

    /**
     * Checks value of {@link #IGNORE_MISSING_GITIGNORE} key in {@link PropertiesComponent}.
     *
     * @param project current project
     * @return {@link #IGNORE_MISSING_GITIGNORE} value
     */
    public static boolean isIgnoreMissingGitignore(@Nonnull Project project) {
        return properties(project).getBoolean(IGNORE_MISSING_GITIGNORE, false);
    }

    /**
     * Sets value of {@link #IGNORE_MISSING_GITIGNORE} key in {@link PropertiesComponent} to <code>true</code>.
     *
     * @param project current project
     */
    public static void setIgnoreMissingGitignore(@Nonnull Project project) {
        properties(project).setValue(IGNORE_MISSING_GITIGNORE, Boolean.TRUE.toString());
    }

    /**
     * Checks value of {@link #ADD_UNVERSIONED_FILES} key in {@link PropertiesComponent}.
     *
     * @param project current project
     * @return {@link #ADD_UNVERSIONED_FILES} value
     */
    public static boolean isAddUnversionedFiles(@Nonnull Project project) {
        return properties(project).getBoolean(ADD_UNVERSIONED_FILES, false);
    }

    /**
     * Sets value of {@link #ADD_UNVERSIONED_FILES} key in {@link PropertiesComponent} to <code>true</code>.
     *
     * @param project current project
     */
    public static void setAddUnversionedFiles(@Nonnull Project project) {
        properties(project).setValue(ADD_UNVERSIONED_FILES, Boolean.TRUE.toString());
    }

    /**
     * Checks if user already dismissed notification about editing ignored file.
     *
     * @param project current project
     * @param file    current file
     * @return notification was dismissed
     */
    public static boolean isDismissedIgnoredEditingNotification(@Nonnull Project project, @Nonnull VirtualFile file) {
        PropertiesComponent props = properties(project);
        String[] values = props.getValues(DISMISSED_IGNORED_EDITING_NOTIFICATION);

        return Set.of(values != null ? values : new String[0]).contains(file.getCanonicalPath());
    }

    /**
     * Stores information about dismissed notification about editing ignored file.
     *
     * @param project current project
     * @param file    current file
     */
    public static void setDismissedIgnoredEditingNotification(@Nonnull Project project, @Nonnull VirtualFile file) {
        PropertiesComponent props = properties(project);
        String[] values = props.getValues(DISMISSED_IGNORED_EDITING_NOTIFICATION);

        Set<String> set = new HashSet<>(Set.of(values != null ? values : new String[0]));
        set.add(file.getCanonicalPath());

        props.setValues(DISMISSED_IGNORED_EDITING_NOTIFICATION, set.toArray(new String[0]));
    }

    /**
     * Shorthand for {@link ProjectPropertiesComponent#getInstance} method.
     *
     * @param project current project
     * @return component instance
     */
    @Nonnull
    private static PropertiesComponent properties(@Nonnull Project project) {
        return ProjectPropertiesComponent.getInstance(project);
    }
}
