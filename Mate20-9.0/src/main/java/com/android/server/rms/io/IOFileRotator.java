package com.android.server.rms.io;

import android.os.FileUtils;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
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
import libcore.io.Streams;

public class IOFileRotator {
    private static final int MAX_FILE_SIZE = 262144;
    private static long MAX_SIZE_BASE_PATH = 2097152;
    private static final String SUFFIX_BACKUP = ".backup";
    private static final String SUFFIX_NO_BACKUP = ".no_backup";
    private static final String TAG = "RMS.IO.FileRotator";
    private static long mMaxSizeForBasePath = MAX_SIZE_BASE_PATH;
    private final File mBasePath;
    private final long mDeleteAgeMillis;
    private long mMaxFileSize;
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
            if (dotIndex == -1 || dashIndex == -1) {
                if (Utils.DEBUG) {
                    Log.d(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " missing time section");
                }
                return false;
            } else if (!this.prefix.equals(name.substring(0, dotIndex))) {
                if (Utils.DEBUG) {
                    Log.d(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " prefix doesn't match");
                }
                return false;
            } else {
                try {
                    this.startMillis = Long.parseLong(name.substring(dotIndex + 1, dashIndex));
                    if (name.length() - dashIndex == 1) {
                        this.endMillis = Long.MAX_VALUE;
                    } else {
                        this.endMillis = Long.parseLong(name.substring(dashIndex + 1));
                    }
                    return true;
                } catch (NumberFormatException e) {
                    Slog.e(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " NumberFormatException");
                    return false;
                }
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

    private static class RewriterDef implements Rewriter {
        private Reader mReader = null;
        private Writer mWriter = null;

        public RewriterDef(Reader reader, Writer writer) {
            this.mReader = reader;
            this.mWriter = writer;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            if (this.mReader == null) {
                Log.e(IOFileRotator.TAG, "RewriterDef,the reader is null");
            } else {
                this.mReader.read(in);
            }
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            if (this.mWriter == null) {
                Log.e(IOFileRotator.TAG, "RewriterDef,the Writer is null");
            } else {
                this.mWriter.write(out);
            }
        }
    }

    public interface Writer {
        void write(OutputStream outputStream) throws IOException;
    }

    public IOFileRotator(File basePath, String prefix, long rotateAgeMillis, long deleteAgeMillis, long maxFileSize) {
        this(basePath, prefix, rotateAgeMillis, deleteAgeMillis);
        this.mMaxFileSize = maxFileSize;
    }

    public IOFileRotator(File basePath, String prefix, long rotateAgeMillis, long deleteAgeMillis) {
        this.mMaxFileSize = 262144;
        this.mBasePath = (File) Preconditions.checkNotNull(basePath);
        this.mPrefix = (String) Preconditions.checkNotNull(prefix);
        this.mRotateAgeMillis = rotateAgeMillis;
        this.mDeleteAgeMillis = deleteAgeMillis;
        if (!this.mBasePath.exists() && !this.mBasePath.mkdirs()) {
            Log.e(TAG, "IOFileRotator,fail to create the directory:" + this.mBasePath);
        }
        for (String name : getBasePathFileList()) {
            if (name.startsWith(this.mPrefix)) {
                if (name.endsWith(SUFFIX_BACKUP)) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "recovering " + name);
                    }
                    if (!new File(this.mBasePath, name).renameTo(new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_BACKUP.length())))) {
                        Log.e(TAG, "IOFileRotator,fail to renameTo,file:" + backupFile.getName());
                    }
                } else if (name.endsWith(SUFFIX_NO_BACKUP)) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "recovering " + name);
                    }
                    File noBackupFile = new File(this.mBasePath, name);
                    File file = new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_NO_BACKUP.length()));
                    if (!noBackupFile.delete()) {
                        Log.e(TAG, "IOFileRotator,fail to delete,file:" + noBackupFile.getName());
                    }
                    if (!file.delete()) {
                        Log.e(TAG, "IOFileRotator,fail to delete,file:" + file.getName());
                    }
                }
            }
        }
    }

    public void deleteAll() {
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name) && !new File(this.mBasePath, name).delete()) {
                Log.e(TAG, "deleteAll,fail to delete the file:" + name);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        throw r3;
     */
    public void dumpAll(OutputStream os) throws IOException {
        FileInputStream is = new ZipOutputStream(os);
        try {
            FileInfo info = new FileInfo(this.mPrefix);
            for (String name : getBasePathFileList()) {
                if (info.parse(name)) {
                    is.putNextEntry(new ZipEntry(name));
                    is = new FileInputStream(new File(this.mBasePath, name));
                    Streams.copy(is, is);
                    IoUtils.closeQuietly(is);
                    is.closeEntry();
                }
            }
        } finally {
        }
    }

    public long getAvailableBytesInActiveFile(long currentTimeMillis) {
        long j = 0;
        long availableBytes = 0;
        try {
            long availableBytes2 = this.mMaxFileSize - new File(this.mBasePath, getActiveName(currentTimeMillis)).length();
            if (availableBytes2 > 0) {
                j = availableBytes2;
            }
            availableBytes = j;
        } catch (RuntimeException e) {
            Log.e(TAG, "checkIfActiveFileFull,RuntimeException:" + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "checkIfActiveFileFull,fail to read the file's size");
        } catch (Throwable th) {
            throw th;
        }
        return availableBytes;
    }

    public boolean removeFilesWhenOverFlow() {
        boolean isHandle = false;
        try {
            if (Utils.getSizeOfDirectory(this.mBasePath) < mMaxSizeForBasePath) {
                Log.i(TAG, "removeFilesWhenOverFlow,the total size is ok,current size:" + directorySize);
                return false;
            }
            String[] baseFiles = getBasePathFileList();
            int totalSize = 0;
            int deleteEndIndex = 0;
            int index = baseFiles.length - 1;
            while (true) {
                if (index < 0) {
                    break;
                }
                totalSize = (int) (((long) totalSize) + new File(this.mBasePath, baseFiles[index]).length());
                if (((long) totalSize) >= mMaxSizeForBasePath) {
                    deleteEndIndex = index;
                    break;
                }
                index--;
            }
            for (int index2 = 0; index2 <= deleteEndIndex; index2++) {
                if (!new File(this.mBasePath, baseFiles[index2]).delete()) {
                    Log.e(TAG, "removeFilesWhenOverFlow,fail to delete the " + baseFiles[index2]);
                }
            }
            isHandle = true;
            return isHandle;
        } catch (RuntimeException e) {
            Log.e(TAG, "removeFilesWhenOverFlow,RuntimeException:" + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "removeFilesWhenOverFlow,fail to read the file's size");
        }
    }

    public void forceFile(long currentTimeMillis, long endTimeMills) {
        String activeFileName = getActiveName(currentTimeMillis);
        File currentFile = new File(this.mBasePath, activeFileName);
        if (currentFile.exists()) {
            FileInfo info = new FileInfo(this.mPrefix);
            if (info.parse(activeFileName) && info.isActive()) {
                info.endMillis = endTimeMills;
                File destFile = new File(this.mBasePath, info.build());
                if (!currentFile.renameTo(destFile)) {
                    Log.e(TAG, "forceFile,fail to renameTo:destFile" + destFile.getName());
                }
            }
        }
    }

    public void rewriteActive(Rewriter rewriter, long currentTimeMillis) throws IOException {
        if (rewriter == null) {
            Log.e(TAG, "rewriteActive,the rewriter is null");
        } else {
            rewriteSingle(rewriter, getActiveName(currentTimeMillis));
        }
    }

    @Deprecated
    public void combineActive(Reader reader, Writer writer, long currentTimeMillis) throws IOException {
        rewriteActive(new RewriterDef(reader, writer), currentTimeMillis);
    }

    private void rewriteSingle(Rewriter rewriter, String name) throws IOException {
        if (Utils.DEBUG) {
            Log.d(TAG, "rewriting " + name);
        }
        File file = new File(this.mBasePath, name);
        rewriter.reset();
        if (file.exists()) {
            readFile(file, rewriter);
            if (rewriter.shouldWrite()) {
                File file2 = this.mBasePath;
                File backupFile = new File(file2, name + SUFFIX_BACKUP);
                if (!file.renameTo(backupFile)) {
                    Log.e(TAG, "rewriteSingle,fail to renameTo:" + backupFile.getName());
                }
                try {
                    writeFile(file, rewriter);
                    if (!backupFile.delete()) {
                        Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
                    }
                } catch (Throwable t) {
                    if (!file.delete()) {
                        Log.e(TAG, "rewriteSingle,fail to delete the file:" + file.getName());
                    }
                    if (!backupFile.renameTo(file)) {
                        Log.e(TAG, "rewriteSingle,fail to renameTo:" + backupFile.getName());
                    }
                    throw rethrowAsIoException(t);
                }
            }
        } else {
            File file3 = this.mBasePath;
            File backupFile2 = new File(file3, name + SUFFIX_NO_BACKUP);
            if (!backupFile2.createNewFile()) {
                Log.e(TAG, "rewriteSingle,fail to createNewFile," + backupFile2.getName());
            }
            try {
                writeFile(file, rewriter);
                if (!backupFile2.delete()) {
                    Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile2.getName());
                }
            } catch (Throwable t2) {
                if (!file.delete()) {
                    Log.e(TAG, "rewriteSingle,fail to delete the file:" + file.getName());
                }
                if (!backupFile2.delete()) {
                    Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile2.getName());
                }
                throw rethrowAsIoException(t2);
            }
        }
    }

    public void readMatching(Reader reader, long matchStartMillis, long matchEndMillis) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name) && info.startMillis <= matchEndMillis && matchStartMillis <= info.endMillis) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "reading matching " + name);
                }
                readFile(new File(this.mBasePath, name), reader);
            }
        }
    }

    private String getActiveName(long currentTimeMillis) {
        String oldestActiveName = null;
        long oldestActiveStart = Long.MAX_VALUE;
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
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

    private String[] getBasePathFileList() {
        String[] baseFiles = this.mBasePath.list();
        if (baseFiles != null && baseFiles.length != 0) {
            return baseFiles;
        }
        Log.e(TAG, "getBasePathFileList,the baseFiles is empty");
        return new String[0];
    }

    public void maybeRotate(long currentTimeMillis) {
        long j = currentTimeMillis;
        long rotateBefore = j - this.mRotateAgeMillis;
        long deleteBefore = j - this.mDeleteAgeMillis;
        FileInfo info = new FileInfo(this.mPrefix);
        String[] baseFileList = getBasePathFileList();
        int length = baseFileList.length;
        int i = 0;
        while (i < length) {
            String name = baseFileList[i];
            if (info.parse(name)) {
                if (info.isActive()) {
                    if (info.startMillis <= rotateBefore) {
                        if (Utils.DEBUG) {
                            Log.d(TAG, "rotating " + name);
                        }
                        info.endMillis = j;
                        File file = new File(this.mBasePath, name);
                        File destFile = new File(this.mBasePath, info.build());
                        if (!file.renameTo(destFile)) {
                            Log.e(TAG, "maybeRotate,fail to renameTo:" + destFile.getName());
                        }
                    }
                } else if (info.endMillis <= deleteBefore) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "deleting " + name);
                    }
                    File file2 = new File(this.mBasePath, name);
                    if (!file2.delete()) {
                        Log.e(TAG, "maybeRotate,fail to delete the file:" + file2.getName());
                    }
                }
            }
            i++;
            j = currentTimeMillis;
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
