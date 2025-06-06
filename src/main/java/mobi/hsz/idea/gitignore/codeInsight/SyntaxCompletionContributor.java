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

package mobi.hsz.idea.gitignore.codeInsight;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import mobi.hsz.idea.gitignore.IgnoreBundle;
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage;
import mobi.hsz.idea.gitignore.psi.IgnoreSyntax;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides completion feature for {@link mobi.hsz.idea.gitignore.psi.IgnoreTypes#SYNTAX} element.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.0
 */
@ExtensionImpl
public class SyntaxCompletionContributor extends CompletionContributor {
    /** Allowed values for the completion. */
    @Nonnull
    private static final List<LookupElementBuilder> SYNTAX_ELEMENTS = new ArrayList<>();

    static {
        for (IgnoreBundle.Syntax syntax : IgnoreBundle.Syntax.values()) {
            SYNTAX_ELEMENTS.add(LookupElementBuilder.create(syntax.toString().toLowerCase()));
        }
    }

    /** Constructor. */
    public SyntaxCompletionContributor() {
        extend(
            CompletionType.BASIC,
            StandardPatterns.instanceOf(PsiElement.class),
            (parameters, context, result) -> {
                PsiElement current = parameters.getPosition();
                if (current.getParent() instanceof IgnoreSyntax && current.getPrevSibling() != null) {
                    result.addAllElements(SYNTAX_ELEMENTS);
                }
            }
        );
    }

    /** Allow autoPopup to appear after custom symbol. */
    @Override
    public boolean invokeAutoPopup(@Nonnull PsiElement position, char typeChar) {
        return true;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return IgnoreLanguage.INSTANCE;
    }
}
