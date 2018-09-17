package com.android.server.rms.io;

import android.os.FileUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.rms.utils.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import libcore.io.IoUtils;

public class IOFileRotator {
    private static final int MAX_FILE_SIZE = 262144;
    private static long MAX_SIZE_BASE_PATH = 0;
    private static final String SUFFIX_BACKUP = ".backup";
    private static final String SUFFIX_NO_BACKUP = ".no_backup";
    private static final String TAG = "IO.FileRotator";
    private static long mMaxSizeForBasePath;
    private final File mBasePath;
    private final long mDeleteAgeMillis;
    private long mMaxFileSize;
    private final String mPrefix;
    private final long mRotateAgeMillis;

    private static class FileInfo {
        public long endMillis;
        public final String prefix;
        public long startMillis;

        public FileInfo(String prefix) {
            this.prefix = (String) Preconditions.checkNotNull(prefix);
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
            } else if (this.prefix.equals(name.substring(0, dotIndex))) {
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
            } else {
                if (Utils.DEBUG) {
                    Log.d(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " prefix doesn't match");
                }
                return false;
            }
        }

        public String build() {
            StringBuilder name = new StringBuilder();
            name.append(this.prefix).append('.').append(this.startMillis).append('-');
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

    public interface Writer {
        void write(OutputStream outputStream) throws IOException;
    }

    public interface Rewriter extends Reader, Writer {
        void reset();

        boolean shouldWrite();
    }

    private static class RewriterDef implements Rewriter {
        private Reader mReader;
        private Writer mWriter;

        public RewriterDef(Reader reader, Writer writer) {
            this.mReader = null;
            this.mWriter = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.io.IOFileRotator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.io.IOFileRotator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.io.IOFileRotator.<clinit>():void");
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
        if (!(this.mBasePath.exists() || this.mBasePath.mkdirs())) {
            Log.e(TAG, "IOFileRotator,fail to create the directory:" + this.mBasePath);
        }
        for (String name : getBasePathFileList()) {
            if (name.startsWith(this.mPrefix)) {
                if (name.endsWith(SUFFIX_BACKUP)) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "recovering " + name);
                    }
                    File backupFile = new File(this.mBasePath, name);
                    if (!backupFile.renameTo(new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_BACKUP.length())))) {
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

    public void dumpAll(java.io.OutputStream r11) throws java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r6 = new java.util.zip.ZipOutputStream;
        r6.<init>(r11);
        r3 = new com.android.server.rms.io.IOFileRotator$FileInfo;	 Catch:{ all -> 0x0041 }
        r7 = r10.mPrefix;	 Catch:{ all -> 0x0041 }
        r3.<init>(r7);	 Catch:{ all -> 0x0041 }
        r0 = r10.getBasePathFileList();	 Catch:{ all -> 0x0041 }
        r7 = 0;	 Catch:{ all -> 0x0041 }
        r8 = r0.length;	 Catch:{ all -> 0x0041 }
    L_0x0012:
        if (r7 >= r8) goto L_0x0046;	 Catch:{ all -> 0x0041 }
    L_0x0014:
        r5 = r0[r7];	 Catch:{ all -> 0x0041 }
        r9 = r3.parse(r5);	 Catch:{ all -> 0x0041 }
        if (r9 == 0) goto L_0x0039;	 Catch:{ all -> 0x0041 }
    L_0x001c:
        r1 = new java.util.zip.ZipEntry;	 Catch:{ all -> 0x0041 }
        r1.<init>(r5);	 Catch:{ all -> 0x0041 }
        r6.putNextEntry(r1);	 Catch:{ all -> 0x0041 }
        r2 = new java.io.File;	 Catch:{ all -> 0x0041 }
        r9 = r10.mBasePath;	 Catch:{ all -> 0x0041 }
        r2.<init>(r9, r5);	 Catch:{ all -> 0x0041 }
        r4 = new java.io.FileInputStream;	 Catch:{ all -> 0x0041 }
        r4.<init>(r2);	 Catch:{ all -> 0x0041 }
        libcore.io.Streams.copy(r4, r6);	 Catch:{ all -> 0x003c }
        libcore.io.IoUtils.closeQuietly(r4);	 Catch:{ all -> 0x0041 }
        r6.closeEntry();	 Catch:{ all -> 0x0041 }
    L_0x0039:
        r7 = r7 + 1;	 Catch:{ all -> 0x0041 }
        goto L_0x0012;	 Catch:{ all -> 0x0041 }
    L_0x003c:
        r7 = move-exception;	 Catch:{ all -> 0x0041 }
        libcore.io.IoUtils.closeQuietly(r4);	 Catch:{ all -> 0x0041 }
        throw r7;	 Catch:{ all -> 0x0041 }
    L_0x0041:
        r7 = move-exception;
        libcore.io.IoUtils.closeQuietly(r6);
        throw r7;
    L_0x0046:
        libcore.io.IoUtils.closeQuietly(r6);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.io.IOFileRotator.dumpAll(java.io.OutputStream):void");
    }

    public long getAvailableBytesInActiveFile(long currentTimeMillis) {
        RuntimeException e;
        File file;
        Throwable th;
        long availableBytes = 0;
        try {
            File activeFile = new File(this.mBasePath, getActiveName(currentTimeMillis));
            try {
                availableBytes = this.mMaxFileSize - activeFile.length();
                if (availableBytes <= 0) {
                    availableBytes = 0;
                }
            } catch (RuntimeException e2) {
                e = e2;
                file = activeFile;
                Log.e(TAG, "checkIfActiveFileFull,RuntimeException:" + e.getMessage());
                return availableBytes;
            } catch (Exception e3) {
                file = activeFile;
                try {
                    Log.e(TAG, "checkIfActiveFileFull,fail to read the file's size");
                    return availableBytes;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        } catch (RuntimeException e4) {
            e = e4;
            Log.e(TAG, "checkIfActiveFileFull,RuntimeException:" + e.getMessage());
            return availableBytes;
        } catch (Exception e5) {
            Log.e(TAG, "checkIfActiveFileFull,fail to read the file's size");
            return availableBytes;
        }
        return availableBytes;
    }

    public boolean removeFilesWhenOverFlow() {
        boolean isHandle = false;
        try {
            long directorySize = Utils.getSizeOfDirectory(this.mBasePath);
            if (directorySize < mMaxSizeForBasePath) {
                Log.i(TAG, "removeFilesWhenOverFlow,the total size is ok,current size:" + directorySize);
                return false;
            }
            int index;
            String[] baseFiles = getBasePathFileList();
            int totalSize = 0;
            int deleteEndIndex = 0;
            for (index = baseFiles.length - 1; index >= 0; index--) {
                totalSize = (int) (((long) totalSize) + new File(this.mBasePath, baseFiles[index]).length());
                if (((long) totalSize) >= mMaxSizeForBasePath) {
                    deleteEndIndex = index;
                    break;
                }
            }
            for (index = 0; index <= deleteEndIndex; index++) {
                if (!new File(this.mBasePath, baseFiles[index]).delete()) {
                    Log.e(TAG, "removeFilesWhenOverFlow,fail to delete the " + baseFiles[index]);
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
        File backupFile;
        if (file.exists()) {
            readFile(file, rewriter);
            if (rewriter.shouldWrite()) {
                backupFile = new File(this.mBasePath, name + SUFFIX_BACKUP);
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
                    IOException rethrowAsIoException = rethrowAsIoException(t);
                }
            } else {
                return;
            }
        }
        backupFile = new File(this.mBasePath, name + SUFFIX_NO_BACKUP);
        if (!backupFile.createNewFile()) {
            Log.e(TAG, "rewriteSingle,fail to createNewFile," + backupFile.getName());
        }
        try {
            writeFile(file, rewriter);
            if (!backupFile.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
            }
        } catch (Throwable t2) {
            if (!file.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + file.getName());
            }
            if (!backupFile.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
            }
            rethrowAsIoException = rethrowAsIoException(t2);
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
        long rotateBefore = currentTimeMillis - this.mRotateAgeMillis;
        long deleteBefore = currentTimeMillis - this.mDeleteAgeMillis;
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name)) {
                File file;
                if (info.isActive()) {
                    if (info.startMillis <= rotateBefore) {
                        if (Utils.DEBUG) {
                            Log.d(TAG, "rotating " + name);
                        }
                        info.endMillis = currentTimeMillis;
                        file = new File(this.mBasePath, name);
                        File destFile = new File(this.mBasePath, info.build());
                        if (!file.renameTo(destFile)) {
                            Log.e(TAG, "maybeRotate,fail to renameTo:" + destFile.getName());
                        }
                    }
                } else if (info.endMillis <= deleteBefore) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "deleting " + name);
                    }
                    file = new File(this.mBasePath, name);
                    if (!file.delete()) {
                        Log.e(TAG, "maybeRotate,fail to delete the file:" + file.getName());
                    }
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
