package mobi.hsz.idea.gitignore.outer;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicImpl;
import consulo.disposer.Disposer;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.TextEditor;
import consulo.fileEditor.event.FileEditorManagerListener;
import consulo.language.ignore.IgnoreFileType;
import consulo.language.ignore.IgnoreLanguage;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import mobi.hsz.idea.gitignore.IgnoreBundle;
import mobi.hsz.idea.gitignore.settings.IgnoreSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Listener for ignore editor manager.
 */
@TopicImpl(ComponentScope.PROJECT)
public class IgnoreEditorManagerListener implements FileEditorManagerListener {
    private final Project project;

    @Inject
    public IgnoreEditorManagerListener(@Nonnull Project project) {
        this.project = project;
    }

    /**
     * Handles file opening event and attaches outer ignore component.
     *
     * @param source editor manager
     * @param file   current file
     */
    @Override
    public void fileOpened(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
        FileType fileType = file.getFileType();
        if (!(fileType instanceof IgnoreFileType) || !IgnoreSettings.getInstance().isOuterIgnoreRules()) {
            return;
        }

        IgnoreLanguage language = ((IgnoreFileType)fileType).getIgnoreLanguage();
        if (!IgnoreBundle.isEnabled(language)) {
            return;
        }

        // outer files may need a git call and their documents are loaded from disk - never on the UI thread
        DumbService.getInstance(project).runWhenSmart(() -> project.getApplication().executeOnPooledThread(() -> {
            List<VirtualFile> outerFiles = new ArrayList<>(IgnoreBundle.getOuterFiles(project, language));
            if (outerFiles.isEmpty() || outerFiles.contains(file)) {
                return;
            }

            Map<VirtualFile, Document> documents = new HashMap<>();
            project.getApplication().runReadAction(() -> {
                for (VirtualFile outerFile : outerFiles) {
                    Document document = FileDocumentManager.getInstance().getDocument(outerFile);
                    if (document != null) {
                        documents.put(outerFile, document);
                    }
                }
            });
            VirtualFile userHomeDir = VirtualFileUtil.getUserHomeDir();
            String userHomePath = userHomeDir != null ? userHomeDir.getPath() : null;

            project.getUIAccess().give(() -> {
                if (project.isDisposed()) {
                    return;
                }
                addOuterIgnoreWrappers(source, file, language, outerFiles, documents, userHomePath);
            });
        }));
    }

    @RequiredUIAccess
    private void addOuterIgnoreWrappers(
        FileEditorManager source,
        VirtualFile file,
        IgnoreLanguage language,
        List<VirtualFile> outerFiles,
        Map<VirtualFile, Document> documents,
        @Nullable String userHomePath
    ) {
        for (FileEditor fileEditor : source.getEditors(file)) {
            if (!(fileEditor instanceof TextEditor)) {
                return;
            }
            OuterIgnoreWrapper wrapper = new OuterIgnoreWrapper(project, language, outerFiles, documents, userHomePath);
            JComponent component = wrapper.getComponent();
            IgnoreSettings.Listener settingsListener = (key, value) -> {
                if (IgnoreSettings.KEY.OUTER_IGNORE_RULES.equals(key)) {
                    component.setVisible((Boolean)value);
                }
            };

            IgnoreSettings.getInstance().addListener(settingsListener);
            source.addBottomComponent(fileEditor, component);

            Disposer.register(fileEditor, wrapper);
            Disposer.register(
                fileEditor,
                () -> {
                    IgnoreSettings.getInstance().removeListener(settingsListener);
                    source.removeBottomComponent(fileEditor, component);
                }
            );
        }
    }
}
