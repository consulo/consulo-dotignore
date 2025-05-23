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

package mobi.hsz.idea.gitignore.file;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import mobi.hsz.idea.gitignore.IgnoreBundle;
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType;
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage;

/**
 * Class that assigns file types with languages.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 0.1
 */
@ExtensionImpl
public class IgnoreFileTypeFactory extends FileTypeFactory {
    private final FileNameMatcherFactory fileNameMatcherFactory;

    @Inject
    public IgnoreFileTypeFactory(FileNameMatcherFactory fileNameMatcherFactory) {
        this.fileNameMatcherFactory = fileNameMatcherFactory;
    }

    /**
     * Assigns file types with languages.
     *
     * @param consumer file types consumer
     */
    @Override
    public void createFileTypes(@Nonnull FileTypeConsumer consumer) {
        consume(consumer, IgnoreFileType.INSTANCE);
        for (IgnoreLanguage language : IgnoreBundle.LANGUAGES) {
            consume(consumer, language.getFileType());
        }
    }

    /**
     * Shorthand for consuming ignore file types.
     *
     * @param consumer file types consumer
     * @param fileType file type to consume
     */
    private void consume(@Nonnull FileTypeConsumer consumer, @Nonnull IgnoreFileType fileType) {
        consumer.consume(fileType, fileNameMatcherFactory.createExactFileNameMatcher(fileType.getIgnoreLanguage().getFilename()));
        consumer.consume(fileType, fileType.getIgnoreLanguage().getExtension());
    }
}
