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

package mobi.hsz.idea.gitignore.actions;

import consulo.dotignore.localize.IgnoreLocalize;
import consulo.ui.ex.action.Presentation;
import mobi.hsz.idea.gitignore.Common;
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType;
import org.junit.Assert;

/**
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.5
 */
public class AddTemplateActionTest extends Common<AddTemplateAction> {
    public void testAddTemplateActionInvocation() {
        AddTemplateAction action = new AddTemplateAction();
        Presentation presentation;

        presentation = myFixture.testAction(action);
        Assert.assertEquals(IgnoreLocalize.actionAddtemplate().get(), presentation.getText());
        Assert.assertEquals(IgnoreLocalize.actionAddtemplateDescription().get(), presentation.getDescription());
        Assert.assertFalse("Action is not visible if there is no Ignore file context", presentation.isEnabledAndVisible());

        myFixture.configureByText(IgnoreFileType.INSTANCE, "foo");
        presentation = myFixture.testAction(action);
        Assert.assertTrue("Action is visible if there is Ignore file context", presentation.isEnabledAndVisible());
    }
}
