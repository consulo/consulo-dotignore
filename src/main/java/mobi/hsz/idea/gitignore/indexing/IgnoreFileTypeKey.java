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

package mobi.hsz.idea.gitignore.indexing;

import consulo.language.ignore.IgnoreFileType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Decorator for {@link IgnoreFileType} to provide less unique hashcode when used with {@link IgnoreFilesIndex}.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 2.0
 */
public class IgnoreFileTypeKey {
    /** Current file type. */
    @Nonnull
    private final IgnoreFileType type;

    /**
     * Constructor.
     *
     * @param type current file type
     */
    public IgnoreFileTypeKey(@Nonnull IgnoreFileType type) {
        this.type = type;
    }

    /**
     * Returns current file type.
     *
     * @return file type
     */
    @Nonnull
    public IgnoreFileType getType() {
        return type;
    }

    /**
     * Checks if file types are equal - if language names matches.
     *
     * @param o object to check
     * @return file types are equal
     */
    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof IgnoreFileTypeKey ignoreFileTypeKey 
            && ignoreFileTypeKey.getType().getLanguage().getID().equals(this.type.getLanguage().getID());
    }

    /**
     * Returns hashcode using hashcode of the language's name.
     *
     * @return hashcode of language's name
     */
    @Override
    public int hashCode() {
        return type.getLanguage().getID().hashCode();
    }
}
