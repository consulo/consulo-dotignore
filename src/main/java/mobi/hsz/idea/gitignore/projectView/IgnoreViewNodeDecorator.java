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

package mobi.hsz.idea.gitignore.projectView;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotignore.localize.IgnoreLocalize;
import consulo.project.Project;
import consulo.project.ui.view.tree.ProjectViewNode;
import consulo.project.ui.view.tree.ProjectViewNodeDecorator;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.tree.PresentationData;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import mobi.hsz.idea.gitignore.IgnoreManager;
import mobi.hsz.idea.gitignore.settings.IgnoreSettings;
import mobi.hsz.idea.gitignore.util.Utils;

import static consulo.ui.ex.SimpleTextAttributes.STYLE_SMALLER;

/**
 * {@link ProjectViewNodeDecorator} implementation to show on the Project Tree if ignored file is
 * still tracked with Git.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.7
 */
@ExtensionImpl
public class IgnoreViewNodeDecorator implements ProjectViewNodeDecorator {
    /** Label text attribute for ignored content. */
    private static final SimpleTextAttributes GRAYED_SMALL_ATTRIBUTES =
        new SimpleTextAttributes(STYLE_SMALLER, UIUtil.getInactiveTextColor());

    /** {@link IgnoreManager} instance. */
    private final IgnoreManager manager;

    /** {@link IgnoreSettings} instance. */
    @Nonnull
    private final IgnoreSettings ignoreSettings;

    /**
     * Constructor.
     *
     * @param project current project
     */
    @Inject
    public IgnoreViewNodeDecorator(@Nonnull Project project) {
        this.manager = IgnoreManager.getInstance(project);
        this.ignoreSettings = IgnoreSettings.getInstance();
    }

    /**
     * Modifies the presentation of a project view node.
     *
     * @param node the node to modify (use {@link ProjectViewNode#getValue()} to get the object represented by the
     *             node).
     * @param data the current presentation of the node, which you can modify as necessary.
     */
    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        VirtualFile file = node.getVirtualFile();
        if (file == null) {
            return;
        }

        if (ignoreSettings.isInformTrackedIgnored() && manager.isFileTracked(file) && manager.isFileIgnored(file)) {
            Utils.addColoredText(
                data,
                IgnoreLocalize.projectviewTracked().get(),
                GRAYED_SMALL_ATTRIBUTES
            );
        }
        else if (ignoreSettings.isHideIgnoredFiles() && file.isDirectory()) {
            int count = ContainerUtil.filter(
                file.getChildren(),
                child -> manager.isFileIgnored(child) && !manager.isFileTracked(child)
            ).size();

            if (count > 0) {
                Utils.addColoredText(
                    data,
                    IgnoreLocalize.projectviewContainshidden(count).get(),
                    GRAYED_SMALL_ATTRIBUTES
                );
            }
        }
    }
}
