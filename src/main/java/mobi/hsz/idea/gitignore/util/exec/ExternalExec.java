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

package mobi.hsz.idea.gitignore.util.exec;

import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.Lists;
import consulo.util.dataholder.Key;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.versionControlSystem.root.VcsRoot;
import consulo.virtualFileSystem.VirtualFile;
import git4idea.config.GitExecutableManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import mobi.hsz.idea.gitignore.util.Utils;
import mobi.hsz.idea.gitignore.util.exec.parser.ExecutionOutputParser;
import mobi.hsz.idea.gitignore.util.exec.parser.GitExcludesOutputParser;
import mobi.hsz.idea.gitignore.util.exec.parser.GitUnignoredFilesOutputParser;
import mobi.hsz.idea.gitignore.util.exec.parser.SimpleOutputParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds util methods for calling external executables (i.e. git/hg)
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.4
 */
public class ExternalExec {
    private static final Logger LOG = Logger.getInstance(ExternalExec.class);
    /** Default external exec timeout. */
    private static final int DEFAULT_TIMEOUT = 5000;

    /** Private constructor to prevent creating Icons instance. */
    private ExternalExec() {
    }

    /** Checks if Git plugin is enabled. */
    private static final boolean GIT_ENABLED = Utils.isGitPluginEnabled();

    /** Git command to get user's excludesfile path. */
    private static final List<String> GIT_CONFIG_EXCLUDES_FILE = List.of("config", "--global", "core.excludesfile");

    /** Git command to list unversioned files. */
    private static final List<String> GIT_UNIGNORED_FILES = List.of("clean", "-dn");

    /** Git command to list ignored but tracked files. */
    private static final List<String> GIT_IGNORED_FILES = List.of("ls-files", "-i", "--exclude-standard");

    /** Git command to remove file from tracking. */
    private static final List<String> GIT_REMOVE_FILE_FROM_TRACKING = List.of("rm", "--cached", "--force");

    /**
     * Returns {@link VirtualFile} instance of the Git excludes file if available.
     *
     * @return Git excludes file
     */
    @Nullable
    public static VirtualFile getGitExcludesFile() {
        return runForSingle(GIT_CONFIG_EXCLUDES_FILE, null, new GitExcludesOutputParser());
    }

    /**
     * Returns list of unignored files for the given directory.
     *
     * @param project  current project
     * @param file     current file
     * @return unignored files list
     */
    @Nonnull
    public static List<String> getUnignoredFiles(@Nonnull Project project, @Nonnull VirtualFile file) {
        if (!Utils.isInProject(file, project)) {
            return new ArrayList<>();
        }

        ArrayList<String> result = run(
            GIT_UNIGNORED_FILES,
            file.getParent(),
            new GitUnignoredFilesOutputParser()
        );
        return Lists.notNullize(result);
    }

    /**
     * Returns list of ignored files for the given repository.
     *
     * @param vcsRoot repository to check
     * @return unignored files list
     */
    @Nonnull
    public static List<String> getIgnoredFiles(@Nonnull VcsRoot vcsRoot) {
        ArrayList<String> result = run(
            GIT_IGNORED_FILES,
            vcsRoot.getPath(),
            new SimpleOutputParser()
        );
        return Lists.notNullize(result);
    }

    /**
     * Removes given files from the git tracking.
     *
     * @param file    to untrack
     * @param vcsRoot file's repository
     */
    public static void removeFileFromTracking(@Nonnull VirtualFile file, @Nonnull VcsRoot vcsRoot) {
        VirtualFile root = vcsRoot.getPath();
        if (root != null) {
            List<String> command = new ArrayList<>(GIT_REMOVE_FILE_FROM_TRACKING);
            command.add(Utils.getRelativePath(root, file));
            run(command, root);
        }
    }

    /**
     * Returns path to the git binary or null if not available.
     *
     * @return path to binary
     */
    @Nullable
    private static String bin() {
        if (GIT_ENABLED) {
            String bin = GitExecutableManager.getInstance().getPathToGit();
            return StringUtil.nullize(bin);
        }
        return null;
    }

    /**
     * Runs git executable with the given command and current working directory.
     *
     * @param command   to call
     * @param directory current working directory
     * @param parser    {@link ExecutionOutputParser} implementation
     * @param <T>       return type
     * @return result of the call
     */
    @Nullable
    private static <T> T runForSingle(
        @Nonnull List<String> command,
        @Nullable VirtualFile directory,
        @Nonnull ExecutionOutputParser<T> parser
    ) {
        return ContainerUtil.getFirstItem(run(command, directory, parser));
    }

    /**
     * Runs git executable with the given command and current working directory.
     *
     * @param command   to call
     * @param directory current working directory
     */
    private static void run(@Nonnull List<String> command, @Nullable VirtualFile directory) {
        run(command, directory, null);
    }

    /**
     * Runs git executable with the given command and current working directory.
     *
     * @param command   to call
     * @param directory current working directory
     * @param parser    {@link ExecutionOutputParser} implementation
     * @param <T>       return type
     * @return result of the call
     */
    @Nullable
    private static <T> ArrayList<T> run(
        @Nonnull List<String> command,
        @Nullable VirtualFile directory,
        @Nullable ExecutionOutputParser<T> parser
    ) {
        String bin = bin();
        if (bin == null) {
            return null;
        }

        try {
            File workingDirectory = directory != null ? new File(directory.getPath()) : null;

            GeneralCommandLine commandLine = new GeneralCommandLine(bin);
            commandLine.addParameters(command);
            if (workingDirectory != null) {
                commandLine.withWorkDirectory(workingDirectory);
            }

            ProcessHandler handler = ProcessHandlerBuilder.create(commandLine).build();
            handler.addProcessListener(new ProcessListener() {
                @Override
                public void onTextAvailable(ProcessEvent event, Key outputType) {
                    if (parser != null) {
                        parser.onTextAvailable(event.getText(), outputType);
                    }
                }
            });
            handler.startNotify();
            if (!handler.waitFor(DEFAULT_TIMEOUT)) {
                return null;
            }
            if (parser != null) {
                parser.notifyFinished(ObjectUtil.notNull(handler.getExitCode(), 0));
                if (parser.isErrorsReported()) {
                    return null;
                }
                return parser.getOutput();
            }
        }
        catch (ExecutionException e) {
            LOG.warn(e);
        }

        return null;
    }
}
