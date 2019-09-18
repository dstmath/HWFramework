package com.android.internal.util;

import android.os.FileUtils;
import android.util.Slog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import libcore.io.IoUtils;

public class FileRotator {
    private static final boolean LOGD = false;
    private static final String SUFFIX_BACKUP = ".backup";
    private static final String SUFFIX_NO_BACKUP = ".no_backup";
    private static final String TAG = "FileRotator";
    private final File mBasePath;
    private final long mDeleteAgeMillis;
    private final String mPrefix;
    private final long mRotateAgeMillis;

    private static class FileInfo {
        public long endMillis;
        public final String prefix;
        public long startMillis;

        public FileInfo(String prefix2) {
            this.prefix = (String) Preconditions.checkNotNull(prefix2);
        }

        public boolean parse(String name) {
            this.endMillis = -1;
            this.startMillis = -1;
            int dotIndex = name.lastIndexOf(46);
            int dashIndex = name.lastIndexOf(45);
            if (dotIndex == -1 || dashIndex == -1 || !this.prefix.equals(name.substring(0, dotIndex))) {
                return false;
            }
            try {
                this.startMillis = Long.parseLong(name.substring(dotIndex + 1, dashIndex));
                if (name.length() - dashIndex == 1) {
                    this.endMillis = Long.MAX_VALUE;
                } else {
                    this.endMillis = Long.parseLong(name.substring(dashIndex + 1));
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public String build() {
            StringBuilder name = new StringBuilder();
            name.append(this.prefix);
            name.append('.');
            name.append(this.startMillis);
            name.append('-');
            if (this.endMillis != Long.MAX_VALUE) {
                name.append(this.endMillis);
            }
            return name.toString();
        }

        public boolean isActive() {
            return this.endMillis == Long.MAX_VALUE;
        }
    }

    public interface Reader {
        void read(InputStream inputStream) throws IOException;
    }

    public interface Rewriter extends Reader, Writer {
        void reset();

        boolean shouldWrite();
    }

    public interface Writer {
        void write(OutputStream outputStream) throws IOException;
    }

    public FileRotator(File basePath, String prefix, long rotateAgeMillis, long deleteAgeMillis) {
        this.mBasePath = (File) Preconditions.checkNotNull(basePath);
        this.mPrefix = (String) Preconditions.checkNotNull(prefix);
        this.mRotateAgeMillis = rotateAgeMillis;
        this.mDeleteAgeMillis = deleteAgeMillis;
        this.mBasePath.mkdirs();
        for (String name : this.mBasePath.list()) {
            if (name.startsWith(this.mPrefix)) {
                if (name.endsWith(SUFFIX_BACKUP)) {
                    new File(this.mBasePath, name).renameTo(new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_BACKUP.length())));
                } else if (name.endsWith(SUFFIX_NO_BACKUP)) {
                    File noBackupFile = new File(this.mBasePath, name);
                    File file = new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_NO_BACKUP.length()));
                    noBackupFile.delete();
                    file.delete();
                }
            }
        }
    }

    public void deleteAll() {
        FileInfo info = new FileInfo(this.mPrefix);
        if (this.mBasePath == null || this.mBasePath.list() == null) {
            Slog.i(TAG, "deleteAll filed is null");
            return;
        }
        for (String name : this.mBasePath.list()) {
            if (info.parse(name)) {
                new File(this.mBasePath, name).delete();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        throw r2;
     */
    public void dumpAll(OutputStream os) throws IOException {
        FileInputStream is = new ZipOutputStream(os);
        try {
            FileInfo info = new FileInfo(this.mPrefix);
            if (this.mBasePath != null) {
                if (this.mBasePath.list() != null) {
                    for (String name : this.mBasePath.list()) {
                        if (info.parse(name)) {
                            is.putNextEntry(new ZipEntry(name));
                            is = new FileInputStream(new File(this.mBasePath, name));
                            FileUtils.copy(is, is);
                            IoUtils.closeQuietly(is);
                            is.closeEntry();
                        }
                    }
                    IoUtils.closeQuietly(is);
                    return;
                }
            }
            Slog.i(TAG, "dumpAll filed is null");
        } finally {
        }
    }

    public void rewriteActive(Rewriter rewriter, long currentTimeMillis) throws IOException {
        rewriteSingle(rewriter, getActiveName(currentTimeMillis));
    }

    @Deprecated
    public void combineActive(final Reader reader, final Writer writer, long currentTimeMillis) throws IOException {
        rewriteActive(new Rewriter() {
            public void reset() {
            }

            public void read(InputStream in) throws IOException {
                reader.read(in);
            }

            public boolean shouldWrite() {
                return true;
            }

            public void write(OutputStream out) throws IOException {
                writer.write(out);
            }
        }, currentTimeMillis);
    }

    public void rewriteAll(Rewriter rewriter) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
        if (this.mBasePath == null || this.mBasePath.list() == null) {
            Slog.i(TAG, "rewriteAll filed is null");
            return;
        }
        for (String name : this.mBasePath.list()) {
            if (info.parse(name)) {
                rewriteSingle(rewriter, name);
            }
        }
    }

    private void rewriteSingle(Rewriter rewriter, String name) throws IOException {
        File file = new File(this.mBasePath, name);
        rewriter.reset();
        if (file.exists()) {
            readFile(file, rewriter);
            if (rewriter.shouldWrite()) {
                File file2 = this.mBasePath;
                File backupFile = new File(file2, name + SUFFIX_BACKUP);
                file.renameTo(backupFile);
                try {
                    writeFile(file, rewriter);
                    backupFile.delete();
                } catch (Throwable t) {
                    file.delete();
                    backupFile.renameTo(file);
                    throw rethrowAsIoException(t);
                }
            }
        } else {
            File file3 = this.mBasePath;
            File backupFile2 = new File(file3, name + SUFFIX_NO_BACKUP);
            backupFile2.createNewFile();
            try {
                writeFile(file, rewriter);
                backupFile2.delete();
            } catch (Throwable t2) {
                file.delete();
                backupFile2.delete();
                throw rethrowAsIoException(t2);
            }
        }
    }

    public void readMatching(Reader reader, long matchStartMillis, long matchEndMillis) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
        if (this.mBasePath == null || this.mBasePath.list() == null) {
            Slog.i(TAG, "readMatching filed is null");
            return;
        }
        for (String name : this.mBasePath.list()) {
            if (info.parse(name) && info.startMillis <= matchEndMillis && matchStartMillis <= info.endMillis) {
                readFile(new File(this.mBasePath, name), reader);
            }
        }
    }

    private String getActiveName(long currentTimeMillis) {
        String oldestActiveName = null;
        long oldestActiveStart = Long.MAX_VALUE;
        FileInfo info = new FileInfo(this.mPrefix);
        if (this.mBasePath == null || this.mBasePath.list() == null) {
            Slog.i(TAG, "getActiveName filed is null");
            return "";
        }
        for (String name : this.mBasePath.list()) {
            if (info.parse(name) && info.isActive() && info.startMillis < currentTimeMillis && info.startMillis < oldestActiveStart) {
                oldestActiveName = name;
                oldestActiveStart = info.startMillis;
            }
        }
        if (oldestActiveName != null) {
            return oldestActiveName;
        }
        info.startMillis = currentTimeMillis;
        info.endMillis = Long.MAX_VALUE;
        return info.build();
    }

    public void maybeRotate(long currentTimeMillis) {
        long rotateBefore = currentTimeMillis - this.mRotateAgeMillis;
        long deleteBefore = currentTimeMillis - this.mDeleteAgeMillis;
        FileInfo info = new FileInfo(this.mPrefix);
        String[] baseFiles = this.mBasePath.list();
        if (baseFiles == null) {
            Slog.i(TAG, "maybeRotate filed is null");
            return;
        }
        for (String name : baseFiles) {
            if (info.parse(name)) {
                if (info.isActive()) {
                    if (info.startMillis <= rotateBefore) {
                        info.endMillis = currentTimeMillis;
                        new File(this.mBasePath, name).renameTo(new File(this.mBasePath, info.build()));
                    }
                } else if (info.endMillis <= deleteBefore) {
                    new File(this.mBasePath, name).delete();
                }
            }
        }
    }

    private static void readFile(File file, Reader reader) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        try {
            reader.read(bis);
        } finally {
            IoUtils.closeQuietly(bis);
        }
    }

    private static void writeFile(File file, Writer writer) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            writer.write(bos);
            bos.flush();
        } finally {
            FileUtils.sync(fos);
            IoUtils.closeQuietly(bos);
        }
    }

    private static IOException rethrowAsIoException(Throwable t) throws IOException {
        if (t instanceof IOException) {
            throw ((IOException) t);
        }
        throw new IOException(t.getMessage(), t);
    }
}
