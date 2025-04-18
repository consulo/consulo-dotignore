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

package mobi.hsz.idea.gitignore.lang;

import consulo.language.Commenter;
import consulo.language.Language;
import jakarta.annotation.Nullable;
import mobi.hsz.idea.gitignore.util.Constants;

import jakarta.annotation.Nonnull;

/**
 * Defines the support for "Comment with Line Comment" and "Comment with Block Comment"
 * actions in a custom language.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.4
 */
public class IgnoreCommenter implements Commenter {
    private final Language ignoreLanguage;

    public IgnoreCommenter(Language ignoreLanguage) {
        this.ignoreLanguage = ignoreLanguage;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return ignoreLanguage;
    }

    /**
     * Returns the string which prefixes a line comment in the language, or null if the language
     * does not support line comments.
     *
     * @return the line comment text, or null.
     */
    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return Constants.HASH;
    }

    /**
     * Returns the string which marks the beginning of a block comment in the language,
     * or null if the language does not support block comments.
     *
     * @return the block comment start text, or null.
     */
    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return null;
    }

    /**
     * Returns the string which marks the end of a block comment in the language,
     * or null if the language does not support block comments.
     *
     * @return the block comment end text, or null.
     */
    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return null;
    }

    /**
     * Returns the string which marks the commented beginning of a block comment in the language,
     * or null if the language does not support block comments.
     *
     * @return the commented block comment start text, or null.
     */
    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    /**
     * Returns the string which marks the commented end of a block comment in the language,
     * or null if the language does not support block comments.
     *
     * @return the commented block comment end text, or null.
     */
    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
