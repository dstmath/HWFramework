package com.android.internal.util;

import android.os.FileUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    /* renamed from: com.android.internal.util.FileRotator.1 */
    class AnonymousClass1 implements Rewriter {
        final /* synthetic */ Reader val$reader;
        final /* synthetic */ Writer val$writer;

        AnonymousClass1(Reader val$reader, Writer val$writer) {
            this.val$reader = val$reader;
            this.val$writer = val$writer;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            this.val$reader.read(in);
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            this.val$writer.write(out);
        }
    }

    private static class FileInfo {
        public long endMillis;
        public final String prefix;
        public long startMillis;

        public FileInfo(String prefix) {
            this.prefix = (String) Preconditions.checkNotNull(prefix);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean parse(String name) {
            this.endMillis = -1;
            this.startMillis = -1;
            int dotIndex = name.lastIndexOf(46);
            int dashIndex = name.lastIndexOf(45);
            if (dotIndex == -1 || dashIndex == -1 || !this.prefix.equals(name.substring(0, dotIndex))) {
                return FileRotator.LOGD;
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
                return FileRotator.LOGD;
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
            return this.endMillis == Long.MAX_VALUE ? true : FileRotator.LOGD;
        }
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
        for (String name : this.mBasePath.list()) {
            if (info.parse(name)) {
                new File(this.mBasePath, name).delete();
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
        r5 = new java.util.zip.ZipOutputStream;
        r5.<init>(r11);
        r2 = new com.android.internal.util.FileRotator$FileInfo;	 Catch:{ all -> 0x0043 }
        r6 = r10.mPrefix;	 Catch:{ all -> 0x0043 }
        r2.<init>(r6);	 Catch:{ all -> 0x0043 }
        r6 = r10.mBasePath;	 Catch:{ all -> 0x0043 }
        r7 = r6.list();	 Catch:{ all -> 0x0043 }
        r6 = 0;	 Catch:{ all -> 0x0043 }
        r8 = r7.length;	 Catch:{ all -> 0x0043 }
    L_0x0014:
        if (r6 >= r8) goto L_0x0048;	 Catch:{ all -> 0x0043 }
    L_0x0016:
        r4 = r7[r6];	 Catch:{ all -> 0x0043 }
        r9 = r2.parse(r4);	 Catch:{ all -> 0x0043 }
        if (r9 == 0) goto L_0x003b;	 Catch:{ all -> 0x0043 }
    L_0x001e:
        r0 = new java.util.zip.ZipEntry;	 Catch:{ all -> 0x0043 }
        r0.<init>(r4);	 Catch:{ all -> 0x0043 }
        r5.putNextEntry(r0);	 Catch:{ all -> 0x0043 }
        r1 = new java.io.File;	 Catch:{ all -> 0x0043 }
        r9 = r10.mBasePath;	 Catch:{ all -> 0x0043 }
        r1.<init>(r9, r4);	 Catch:{ all -> 0x0043 }
        r3 = new java.io.FileInputStream;	 Catch:{ all -> 0x0043 }
        r3.<init>(r1);	 Catch:{ all -> 0x0043 }
        libcore.io.Streams.copy(r3, r5);	 Catch:{ all -> 0x003e }
        libcore.io.IoUtils.closeQuietly(r3);	 Catch:{ all -> 0x0043 }
        r5.closeEntry();	 Catch:{ all -> 0x0043 }
    L_0x003b:
        r6 = r6 + 1;	 Catch:{ all -> 0x0043 }
        goto L_0x0014;	 Catch:{ all -> 0x0043 }
    L_0x003e:
        r6 = move-exception;	 Catch:{ all -> 0x0043 }
        libcore.io.IoUtils.closeQuietly(r3);	 Catch:{ all -> 0x0043 }
        throw r6;	 Catch:{ all -> 0x0043 }
    L_0x0043:
        r6 = move-exception;
        libcore.io.IoUtils.closeQuietly(r5);
        throw r6;
    L_0x0048:
        libcore.io.IoUtils.closeQuietly(r5);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.FileRotator.dumpAll(java.io.OutputStream):void");
    }

    public void rewriteActive(Rewriter rewriter, long currentTimeMillis) throws IOException {
        rewriteSingle(rewriter, getActiveName(currentTimeMillis));
    }

    @Deprecated
    public void combineActive(Reader reader, Writer writer, long currentTimeMillis) throws IOException {
        rewriteActive(new AnonymousClass1(reader, writer), currentTimeMillis);
    }

    public void rewriteAll(Rewriter rewriter) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : this.mBasePath.list()) {
            if (info.parse(name)) {
                rewriteSingle(rewriter, name);
            }
        }
    }

    private void rewriteSingle(Rewriter rewriter, String name) throws IOException {
        File file = new File(this.mBasePath, name);
        rewriter.reset();
        File backupFile;
        if (file.exists()) {
            readFile(file, rewriter);
            if (rewriter.shouldWrite()) {
                backupFile = new File(this.mBasePath, name + SUFFIX_BACKUP);
                file.renameTo(backupFile);
                try {
                    writeFile(file, rewriter);
                    backupFile.delete();
                } catch (Throwable t) {
                    file.delete();
                    backupFile.renameTo(file);
                    IOException rethrowAsIoException = rethrowAsIoException(t);
                }
            } else {
                return;
            }
        }
        backupFile = new File(this.mBasePath, name + SUFFIX_NO_BACKUP);
        backupFile.createNewFile();
        try {
            writeFile(file, rewriter);
            backupFile.delete();
        } catch (Throwable t2) {
            file.delete();
            backupFile.delete();
            rethrowAsIoException = rethrowAsIoException(t2);
        }
    }

    public void readMatching(Reader reader, long matchStartMillis, long matchEndMillis) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
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
        if (baseFiles != null) {
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
