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

package mobi.hsz.idea.gitignore.lang.kind;

import jakarta.annotation.Nonnull;
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType;
import mobi.hsz.idea.gitignore.file.type.kind.SwaggerCodegenFileType;
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage;
import mobi.hsz.idea.gitignore.util.Icons;

/**
 * Swagger Codegen {@link SwaggerCodegenLanguage} definition
 *
 * @author Jim Schubert <james.schubert@gmail.com>
 * @since 2.0.0
 */
public class SwaggerCodegenLanguage extends IgnoreLanguage {
    /** The {@link SwaggerCodegenLanguage} instance */
    public static final SwaggerCodegenLanguage INSTANCE = new SwaggerCodegenLanguage();

    /** {@link IgnoreLanguage} is a non-instantiable static class. */
    private SwaggerCodegenLanguage() {
        super("Swagger Codegen", "swagger-codegen-ignore", null, Icons.SWAGGER_CODEGEN);
    }

    /**
     * Language file type.
     *
     * @return {@link SwaggerCodegenFileType} instance
     */
    @Nonnull
    @Override
    public IgnoreFileType getFileType() {
        return SwaggerCodegenFileType.INSTANCE;
    }

    /**
     * Language is related to the VCS.
     *
     * @return is VCS
     */
    @Override
    public boolean isVCS() {
        return false;
    }
}
