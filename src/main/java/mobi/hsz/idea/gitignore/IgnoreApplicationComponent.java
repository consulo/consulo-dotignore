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

package mobi.hsz.idea.gitignore;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import mobi.hsz.idea.gitignore.settings.IgnoreSettings;
import mobi.hsz.idea.gitignore.util.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.3
 */
public class IgnoreApplicationComponent implements ApplicationComponent {
    /** Plugin has been updated with the current run. */
    private boolean updated;

    /** Plugin is a release candidate. */
    private boolean rc;

    /** Plugin update notification has been shown. */
    private boolean updateNotificationShown;

    /**
     * Get Ignore Application Component
     *
     * @return Ignore Application Component
     */
    @NotNull
    public static IgnoreApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(IgnoreApplicationComponent.class);
    }

    /** Component initialization method. */
    @Override
    public void initComponent() {
        /* The settings storage object. */
        final IgnoreSettings settings = IgnoreSettings.getInstance();
        updated = !Utils.getVersion().equals(settings.getVersion());
        rc = Utils.getVersion().toUpperCase().contains("RC");
        if (updated) {
            settings.setVersion(Utils.getVersion());
        }
    }

    /** Component dispose method. */
    @Override
    public void disposeComponent() {
    }

    /**
     * Returns component's name.
     *
     * @return component's name
     */
    @NotNull
    @Override
    public String getComponentName() {
        return "IgnoreApplicationComponent";
    }

    /**
     * Checks if plugin is a release candidate version.
     *
     * @return plugin is RC
     */
    public boolean isRC() {
        return rc;
    }

    /**
     * Checks if plugin was updated in the current run.
     *
     * @return plugin was updated
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * Checks if update notification was shown.
     *
     * @return notification shown
     */
    public boolean isUpdateNotificationShown() {
        return updateNotificationShown;
    }

    /**
     * Sets update notification shown status.
     *
     * @param shown notification
     */
    public void setUpdateNotificationShown(boolean shown) {
        this.updateNotificationShown = shown;
    }
}
