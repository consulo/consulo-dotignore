package mobi.hsz.idea.gitignore.actions;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.dumb.DumbAware;
import consulo.localize.LocalizeValue;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.IdeActions;

/**
 * @author UNV
 * @since 2025-09-20
 */
@ActionImpl(
    id = "Ignore.TemplateGroup",
    children = {
        @ActionRef(type = AddTemplateAction.class),
        @ActionRef(type = CreateUserTemplateAction.class)
    },
    parents = @ActionParentRef(value = @ActionRef(id = IdeActions.GROUP_GENERATE), anchor = ActionRefAnchor.FIRST)
)
public class IgnoreTemplateGroup extends DefaultActionGroup implements DumbAware {
    public IgnoreTemplateGroup() {
        super(LocalizeValue.empty(), false);
    }
}
