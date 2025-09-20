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
    id = "Ignore.PopupGroup",
    children = {
        @ActionRef(type = IgnoreFileGroupAction.class),
        @ActionRef(type = UnignoreFileGroupAction.class),
        @ActionRef(type = IgnoreTemplateGroup.class)
    },
    parents = {
        @ActionParentRef(@ActionRef(id = IdeActions.GROUP_EDITOR_POPUP)),
        @ActionParentRef(@ActionRef(id = IdeActions.GROUP_PROJECT_VIEW_POPUP)),
        @ActionParentRef(@ActionRef(id = IdeActions.GROUP_EDITOR_TAB_POPUP)),
        @ActionParentRef(@ActionRef(id = IdeActions.GROUP_CONSOLE_EDITOR_POPUP))
    }
)
public class IgnorePopupGroup extends DefaultActionGroup implements DumbAware {
    public IgnorePopupGroup() {
        super(LocalizeValue.empty(), false);
    }
}
