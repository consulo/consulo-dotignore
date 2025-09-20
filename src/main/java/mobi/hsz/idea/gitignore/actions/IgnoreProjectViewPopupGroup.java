package mobi.hsz.idea.gitignore.actions;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.application.dumb.DumbAware;
import consulo.localize.LocalizeValue;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.IdeActions;

/**
 * @author UNV
 * @since 2025-09-20
 */
@ActionImpl(
    id = "Ignore.ProjectViewPopupGroup",
    children = {
        @ActionRef(type = HideIgnoredFilesAction.class),
        @ActionRef(type = HandleTrackedIgnoredFilesAction.class)
    },
    parents = @ActionParentRef(@ActionRef(id = IdeActions.GROUP_PROJECT_VIEW_POPUP))
)
public class IgnoreProjectViewPopupGroup extends DefaultActionGroup implements DumbAware {
    public IgnoreProjectViewPopupGroup() {
        super(LocalizeValue.empty(), false);
    }
}
