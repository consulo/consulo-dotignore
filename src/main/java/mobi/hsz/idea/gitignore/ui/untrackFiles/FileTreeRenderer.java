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

package mobi.hsz.idea.gitignore.ui.untrackFiles;

import consulo.component.util.Iconable;
import consulo.project.Project;
import consulo.ui.ex.awt.tree.CheckboxTree;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;

import javax.swing.*;

/**
 * {@link FileTreeRenderer} implementation of checkbox renderer.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.7
 */
public class FileTreeRenderer extends CheckboxTree.CheckboxTreeCellRenderer {
    /**
     * Renders checkbox tree cell filled with @{link {@link FileTreeNode} data.
     *
     * @param tree     current working tree
     * @param value    template data
     * @param selected node is selected
     * @param expanded node is expanded
     * @param leaf     node is a leaf
     * @param row      node is a row
     * @param hasFocus node has focus
     */
    @Override
    public void customizeRenderer(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus
    ) {
        if (!(value instanceof FileTreeNode)) {
            return;
        }

        FileTreeNode node = (FileTreeNode)value;
        VirtualFile file = node.getFile();
        Project project = node.getProject();

        getTextRenderer().append(file.getName());
        getTextRenderer().setIcon(VirtualFileManager.getInstance().getFileIcon(file, project, Iconable.ICON_FLAG_READ_STATUS));
    }
}
