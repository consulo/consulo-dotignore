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

package consulo.dotignore.lang;

import consulo.language.ignore.IgnoreLanguage;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import mobi.hsz.idea.gitignore.outer.OuterFileFetcher;
import org.jspecify.annotations.Nullable;

/**
 * Base of the ignore kinds registered by the .ignore plugin itself. Adds the plugin-only properties
 * (VCS directory, outer files) on top of the platform {@link IgnoreLanguage}.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 */
public abstract class DotIgnoreLanguage extends IgnoreLanguage {
    /**
     * The Ignore VCS directory name.
     */
    private final @Nullable String myVcsDirectory;

    /**
     * The language icon.
     */
    private final Image myIcon;

    /**
     * Outer files fetchers.
     */
    private final OuterFileFetcher[] myFetchers;

    protected DotIgnoreLanguage(String name, String extension, @Nullable String vcsDirectory, Image icon) {
        this(name, extension, vcsDirectory, icon, new OuterFileFetcher[0]);
    }

    protected DotIgnoreLanguage(
        String name,
        String extension,
        @Nullable String vcsDirectory,
        Image icon,
        OuterFileFetcher[] fetchers
    ) {
        super(name, extension);
        myVcsDirectory = vcsDirectory;
        myIcon = icon;
        myFetchers = fetchers;
    }

    /**
     * Returns Ignore VCS directory name.
     *
     * @return VCS directory name
     */
    public @Nullable String getVcsDirectory() {
        return myVcsDirectory;
    }

    @Override
    public Image getIcon() {
        return myIcon;
    }

    /**
     * Returns outer files fetchers.
     *
     * @return outer files fetchers
     */
    public final OuterFileFetcher[] getOuterFileFetchers() {
        return myFetchers;
    }

    /**
     * Language is related to the VCS.
     *
     * @return is VCS
     */
    public boolean isVCS() {
        return true;
    }

    /**
     * Returns fixed directory for the given {@link IgnoreLanguage}.
     *
     * @param project current project
     * @return fixed directory
     */
    public @Nullable VirtualFile getFixedDirectory(Project project) {
        return null;
    }
}
