package android.os;

import android.annotation.UnsupportedAppUsage;
import android.app.backup.FullBackup;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.webkit.MimeTypeMap;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.SizedInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;

public final class FileUtils {
    private static final long COPY_CHECKPOINT_BYTES = 524288;
    public static final int S_IRGRP = 32;
    public static final int S_IROTH = 4;
    public static final int S_IRUSR = 256;
    public static final int S_IRWXG = 56;
    public static final int S_IRWXO = 7;
    public static final int S_IRWXU = 448;
    public static final int S_IWGRP = 16;
    public static final int S_IWOTH = 2;
    public static final int S_IWUSR = 128;
    public static final int S_IXGRP = 8;
    public static final int S_IXOTH = 1;
    public static final int S_IXUSR = 64;
    private static final String TAG = "FileUtils";
    private static boolean sEnableCopyOptimizations = true;

    public interface ProgressListener {
        void onProgress(long j);
    }

    @UnsupportedAppUsage
    private FileUtils() {
    }

    private static class NoImagePreloadHolder {
        public static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[\\w%+,./=_-]+");

        private NoImagePreloadHolder() {
        }
    }

    @UnsupportedAppUsage
    public static int setPermissions(File path, int mode, int uid, int gid) {
        return setPermissions(path.getAbsolutePath(), mode, uid, gid);
    }

    @UnsupportedAppUsage
    public static int setPermissions(String path, int mode, int uid, int gid) {
        try {
            Os.chmod(path, mode);
            if (uid < 0 && gid < 0) {
                return 0;
            }
            try {
                Os.chown(path, uid, gid);
                return 0;
            } catch (ErrnoException e) {
                Slog.w(TAG, "Failed to chown(" + path + "): " + e);
                return e.errno;
            }
        } catch (ErrnoException e2) {
            Slog.w(TAG, "Failed to chmod(" + path + "): " + e2);
            return e2.errno;
        }
    }

    @UnsupportedAppUsage
    public static int setPermissions(FileDescriptor fd, int mode, int uid, int gid) {
        try {
            Os.fchmod(fd, mode);
            if (uid < 0 && gid < 0) {
                return 0;
            }
            try {
                Os.fchown(fd, uid, gid);
                return 0;
            } catch (ErrnoException e) {
                Slog.w(TAG, "Failed to fchown(): " + e);
                return e.errno;
            }
        } catch (ErrnoException e2) {
            Slog.w(TAG, "Failed to fchmod(): " + e2);
            return e2.errno;
        }
    }

    public static void copyPermissions(File from, File to) throws IOException {
        try {
            StructStat stat = Os.stat(from.getAbsolutePath());
            Os.chmod(to.getAbsolutePath(), stat.st_mode);
            Os.chown(to.getAbsolutePath(), stat.st_uid, stat.st_gid);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @Deprecated
    public static int getUid(String path) {
        try {
            return Os.stat(path).st_uid;
        } catch (ErrnoException e) {
            return -1;
        }
    }

    @UnsupportedAppUsage
    public static boolean sync(FileOutputStream stream) {
        if (stream == null) {
            return true;
        }
        try {
            stream.getFD().sync();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean copyFile(File srcFile, File destFile) {
        try {
            copyFileOrThrow(srcFile, destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        $closeResource(r1, r0);
     */
    @Deprecated
    public static void copyFileOrThrow(File srcFile, File destFile) throws IOException {
        InputStream in = new FileInputStream(srcFile);
        copyToFileOrThrow(in, destFile);
        $closeResource(null, in);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            copyToFileOrThrow(inputStream, destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0027, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        throw r2;
     */
    @Deprecated
    public static void copyToFileOrThrow(InputStream in, File destFile) throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        FileOutputStream out = new FileOutputStream(destFile);
        copy(in, out);
        try {
            Os.fsync(out.getFD());
            $closeResource(null, out);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static long copy(File from, File to) throws IOException {
        return copy(from, to, (CancellationSignal) null, (Executor) null, (ProgressListener) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0024, code lost:
        throw r2;
     */
    public static long copy(File from, File to, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);
        long copy = copy(in, out, signal, executor, listener);
        $closeResource(null, out);
        $closeResource(null, in);
        return copy;
    }

    public static long copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, (CancellationSignal) null, (Executor) null, (ProgressListener) null);
    }

    public static long copy(InputStream in, OutputStream out, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        if (!sEnableCopyOptimizations || !(in instanceof FileInputStream) || !(out instanceof FileOutputStream)) {
            return copyInternalUserspace(in, out, signal, executor, listener);
        }
        return copy(((FileInputStream) in).getFD(), ((FileOutputStream) out).getFD(), signal, executor, listener);
    }

    public static long copy(FileDescriptor in, FileDescriptor out) throws IOException {
        return copy(in, out, (CancellationSignal) null, (Executor) null, (ProgressListener) null);
    }

    public static long copy(FileDescriptor in, FileDescriptor out, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        return copy(in, out, Long.MAX_VALUE, signal, executor, listener);
    }

    public static long copy(FileDescriptor in, FileDescriptor out, long count, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        if (sEnableCopyOptimizations) {
            try {
                StructStat st_in = Os.fstat(in);
                StructStat st_out = Os.fstat(out);
                if (OsConstants.S_ISREG(st_in.st_mode) && OsConstants.S_ISREG(st_out.st_mode)) {
                    return copyInternalSendfile(in, out, count, signal, executor, listener);
                }
                if (!OsConstants.S_ISFIFO(st_in.st_mode)) {
                    if (OsConstants.S_ISFIFO(st_out.st_mode)) {
                    }
                }
                return copyInternalSplice(in, out, count, signal, executor, listener);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }
        return copyInternalUserspace(in, out, count, signal, executor, listener);
    }

    @VisibleForTesting
    public static long copyInternalSplice(FileDescriptor in, FileDescriptor out, long count, CancellationSignal signal, Executor executor, ProgressListener listener) throws ErrnoException {
        long checkpoint = 0;
        long progress = 0;
        long count2 = count;
        while (true) {
            long t = Os.splice(in, null, out, null, Math.min(count2, 524288L), OsConstants.SPLICE_F_MOVE | OsConstants.SPLICE_F_MORE);
            if (t == 0) {
                break;
            }
            progress += t;
            checkpoint += t;
            count2 -= t;
            if (checkpoint >= 524288) {
                if (signal != null) {
                    signal.throwIfCanceled();
                }
                if (!(executor == null || listener == null)) {
                    executor.execute(new Runnable(progress) {
                        /* class android.os.$$Lambda$FileUtils$RlOy_0MlKMWkkCC1mk_jzWcLTKs */
                        private final /* synthetic */ long f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            FileUtils.ProgressListener.this.onProgress(this.f$1);
                        }
                    });
                }
                checkpoint = 0;
            }
        }
        if (!(executor == null || listener == null)) {
            executor.execute(new Runnable(progress) {
                /* class android.os.$$Lambda$FileUtils$e0JoEHjVf9vMX679eNxZixyUZ0 */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FileUtils.ProgressListener.this.onProgress(this.f$1);
                }
            });
        }
        return progress;
    }

    @VisibleForTesting
    public static long copyInternalSendfile(FileDescriptor in, FileDescriptor out, long count, CancellationSignal signal, Executor executor, ProgressListener listener) throws ErrnoException {
        long checkpoint = 0;
        long progress = 0;
        long count2 = count;
        while (true) {
            long t = Os.sendfile(out, in, null, Math.min(count2, 524288L));
            if (t == 0) {
                break;
            }
            progress += t;
            checkpoint += t;
            count2 -= t;
            if (checkpoint >= 524288) {
                if (signal != null) {
                    signal.throwIfCanceled();
                }
                if (!(executor == null || listener == null)) {
                    executor.execute(new Runnable(progress) {
                        /* class android.os.$$Lambda$FileUtils$QtbHtI8Y1rifwydngi6coGK5l2A */
                        private final /* synthetic */ long f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            FileUtils.ProgressListener.this.onProgress(this.f$1);
                        }
                    });
                }
                checkpoint = 0;
            }
        }
        if (!(executor == null || listener == null)) {
            executor.execute(new Runnable(progress) {
                /* class android.os.$$Lambda$FileUtils$XQaJiyjsC2_MFNDbZFQcIhqPnNA */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FileUtils.ProgressListener.this.onProgress(this.f$1);
                }
            });
        }
        return progress;
    }

    @VisibleForTesting
    @Deprecated
    public static long copyInternalUserspace(FileDescriptor in, FileDescriptor out, ProgressListener listener, CancellationSignal signal, long count) throws IOException {
        return copyInternalUserspace(in, out, count, signal, $$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.INSTANCE, listener);
    }

    @VisibleForTesting
    public static long copyInternalUserspace(FileDescriptor in, FileDescriptor out, long count, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        if (count != Long.MAX_VALUE) {
            return copyInternalUserspace(new SizedInputStream(new FileInputStream(in), count), new FileOutputStream(out), signal, executor, listener);
        }
        return copyInternalUserspace(new FileInputStream(in), new FileOutputStream(out), signal, executor, listener);
    }

    @VisibleForTesting
    public static long copyInternalUserspace(InputStream in, OutputStream out, CancellationSignal signal, Executor executor, ProgressListener listener) throws IOException {
        long progress = 0;
        long checkpoint = 0;
        byte[] buffer = new byte[8192];
        while (true) {
            int t = in.read(buffer);
            if (t == -1) {
                break;
            }
            out.write(buffer, 0, t);
            progress += (long) t;
            checkpoint += (long) t;
            if (checkpoint >= 524288) {
                if (signal != null) {
                    signal.throwIfCanceled();
                }
                if (!(executor == null || listener == null)) {
                    executor.execute(new Runnable(progress) {
                        /* class android.os.$$Lambda$FileUtils$TJeD9NeX5giO5vlBrurGIg4IY */
                        private final /* synthetic */ long f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            FileUtils.ProgressListener.this.onProgress(this.f$1);
                        }
                    });
                }
                checkpoint = 0;
            }
        }
        if (!(executor == null || listener == null)) {
            executor.execute(new Runnable(progress) {
                /* class android.os.$$Lambda$FileUtils$0SBPRWOXcbR9EMG_p55sUuxJ_0 */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FileUtils.ProgressListener.this.onProgress(this.f$1);
                }
            });
        }
        return progress;
    }

    @UnsupportedAppUsage
    public static boolean isFilenameSafe(File file) {
        return NoImagePreloadHolder.SAFE_FILENAME_PATTERN.matcher(file.getPath()).matches();
    }

    @UnsupportedAppUsage
    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        int len;
        int len2;
        InputStream input = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(input);
        try {
            long size = file.length();
            if (max > 0 || (size > 0 && max == 0)) {
                if (size > 0 && (max == 0 || size < ((long) max))) {
                    max = (int) size;
                }
                byte[] data = new byte[(max + 1)];
                int length = bis.read(data);
                if (length <= 0) {
                    bis.close();
                    input.close();
                    return "";
                } else if (length <= max) {
                    String str = new String(data, 0, length);
                    bis.close();
                    input.close();
                    return str;
                } else if (ellipsis == null) {
                    String str2 = new String(data, 0, max);
                    bis.close();
                    input.close();
                    return str2;
                } else {
                    String str3 = new String(data, 0, max) + ellipsis;
                    bis.close();
                    input.close();
                    return str3;
                }
            } else if (max < 0) {
                boolean rolled = false;
                byte[] last = null;
                byte[] data2 = null;
                do {
                    if (last != null) {
                        rolled = true;
                    }
                    last = data2;
                    data2 = last;
                    if (data2 == null) {
                        data2 = new byte[(-max)];
                    }
                    len2 = bis.read(data2);
                } while (len2 == data2.length);
                if (last == null && len2 <= 0) {
                    return "";
                }
                if (last == null) {
                    String str4 = new String(data2, 0, len2);
                    bis.close();
                    input.close();
                    return str4;
                }
                if (len2 > 0) {
                    rolled = true;
                    System.arraycopy(last, len2, last, 0, last.length - len2);
                    System.arraycopy(data2, 0, last, last.length - len2, len2);
                }
                if (ellipsis == null || !rolled) {
                    String str5 = new String(last);
                    bis.close();
                    input.close();
                    return str5;
                }
                String str6 = ellipsis + new String(last);
                bis.close();
                input.close();
                return str6;
            } else {
                ByteArrayOutputStream contents = new ByteArrayOutputStream();
                byte[] data3 = new byte[1024];
                do {
                    len = bis.read(data3);
                    if (len > 0) {
                        contents.write(data3, 0, len);
                    }
                } while (len == data3.length);
                String byteArrayOutputStream = contents.toString();
                bis.close();
                input.close();
                return byteArrayOutputStream;
            }
        } finally {
            bis.close();
            input.close();
        }
    }

    @UnsupportedAppUsage
    public static void stringToFile(File file, String string) throws IOException {
        stringToFile(file.getAbsolutePath(), string);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        $closeResource(r1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0037, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0038, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003b, code lost:
        throw r2;
     */
    public static void bytesToFile(String filename, byte[] content) throws IOException {
        if (filename.startsWith("/proc/")) {
            int oldMask = StrictMode.allowThreadDiskWritesMask();
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(content);
                $closeResource(null, fos);
            } finally {
                StrictMode.setThreadPolicyMask(oldMask);
            }
        } else {
            FileOutputStream fos2 = new FileOutputStream(filename);
            fos2.write(content);
            $closeResource(null, fos2);
        }
    }

    @UnsupportedAppUsage
    public static void stringToFile(String filename, String string) throws IOException {
        bytesToFile(filename, string.getBytes(StandardCharsets.UTF_8));
    }

    @UnsupportedAppUsage
    @Deprecated
    public static long checksumCrc32(File file) throws FileNotFoundException, IOException {
        CRC32 checkSummer = new CRC32();
        CheckedInputStream cis = null;
        try {
            CheckedInputStream cis2 = new CheckedInputStream(new FileInputStream(file), checkSummer);
            while (cis2.read(new byte[128]) >= 0) {
            }
            long value = checkSummer.getValue();
            try {
                cis2.close();
            } catch (IOException e) {
            }
            return value;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cis.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        $closeResource(r1, r0);
     */
    public static byte[] digest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        FileInputStream in = new FileInputStream(file);
        byte[] digest = digest(in, algorithm);
        $closeResource(null, in);
        return digest;
    }

    public static byte[] digest(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        return digestInternalUserspace(in, algorithm);
    }

    public static byte[] digest(FileDescriptor fd, String algorithm) throws IOException, NoSuchAlgorithmException {
        return digestInternalUserspace(new FileInputStream(fd), algorithm);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0020, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0021, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        throw r3;
     */
    private static byte[] digestInternalUserspace(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        DigestInputStream digestStream = new DigestInputStream(in, digest);
        do {
        } while (digestStream.read(new byte[8192]) != -1);
        $closeResource(null, digestStream);
        return digest.digest();
    }

    @UnsupportedAppUsage
    public static boolean deleteOlderFiles(File dir, int minCount, long minAgeMs) {
        if (minCount < 0 || minAgeMs < 0) {
            throw new IllegalArgumentException("Constraints must be positive or 0");
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        Arrays.sort(files, new Comparator<File>() {
            /* class android.os.FileUtils.AnonymousClass1 */

            public int compare(File lhs, File rhs) {
                return Long.compare(rhs.lastModified(), lhs.lastModified());
            }
        });
        boolean deleted = false;
        for (int i = minCount; i < files.length; i++) {
            File file = files[i];
            if (System.currentTimeMillis() - file.lastModified() > minAgeMs && file.delete()) {
                Log.d(TAG, "Deleted old file " + file);
                deleted = true;
            }
        }
        return deleted;
    }

    public static boolean contains(File[] dirs, File file) {
        for (File dir : dirs) {
            if (contains(dir, file)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(Collection<File> dirs, File file) {
        for (File dir : dirs) {
            if (contains(dir, file)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(File dir, File file) {
        if (dir == null || file == null) {
            return false;
        }
        return contains(dir.getAbsolutePath(), file.getAbsolutePath());
    }

    public static boolean contains(String dirPath, String filePath) {
        if (dirPath.equals(filePath)) {
            return true;
        }
        if (!dirPath.endsWith("/")) {
            dirPath = dirPath + "/";
        }
        return filePath.startsWith(dirPath);
    }

    public static boolean deleteContentsAndDir(File dir) {
        if (deleteContents(dir)) {
            return dir.delete();
        }
        return false;
    }

    @UnsupportedAppUsage
    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    private static boolean isValidExtFilenameChar(char c) {
        if (c == 0 || c == '/') {
            return false;
        }
        return true;
    }

    public static boolean isValidExtFilename(String name) {
        return name != null && name.equals(buildValidExtFilename(name));
    }

    public static String buildValidExtFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isValidExtFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, 255);
        return res.toString();
    }

    private static boolean isValidFatFilenameChar(char c) {
        if ((c >= 0 && c <= 31) || c == '\"' || c == '*' || c == '/' || c == ':' || c == '<' || c == '\\' || c == '|' || c == 127 || c == '>' || c == '?') {
            return false;
        }
        return true;
    }

    public static boolean isValidFatFilename(String name) {
        return name != null && name.equals(buildValidFatFilename(name));
    }

    public static String buildValidFatFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isValidFatFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, 255);
        return res.toString();
    }

    @VisibleForTesting
    public static String trimFilename(String str, int maxBytes) {
        StringBuilder res = new StringBuilder(str);
        trimFilename(res, maxBytes);
        return res.toString();
    }

    private static void trimFilename(StringBuilder res, int maxBytes) {
        byte[] raw = res.toString().getBytes(StandardCharsets.UTF_8);
        if (raw.length > maxBytes) {
            int maxBytes2 = maxBytes - 3;
            while (raw.length > maxBytes2) {
                res.deleteCharAt(res.length() / 2);
                raw = res.toString().getBytes(StandardCharsets.UTF_8);
            }
            res.insert(res.length() / 2, Session.TRUNCATE_STRING);
        }
    }

    public static String rewriteAfterRename(File beforeDir, File afterDir, String path) {
        File result;
        if (path == null || (result = rewriteAfterRename(beforeDir, afterDir, new File(path))) == null) {
            return null;
        }
        return result.getAbsolutePath();
    }

    public static String[] rewriteAfterRename(File beforeDir, File afterDir, String[] paths) {
        if (paths == null) {
            return null;
        }
        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            result[i] = rewriteAfterRename(beforeDir, afterDir, paths[i]);
        }
        return result;
    }

    public static File rewriteAfterRename(File beforeDir, File afterDir, File file) {
        if (file == null || beforeDir == null || afterDir == null || !contains(beforeDir, file)) {
            return null;
        }
        return new File(afterDir, file.getAbsolutePath().substring(beforeDir.getAbsolutePath().length()));
    }

    private static File buildUniqueFileWithExtension(File parent, String name, String ext) throws FileNotFoundException {
        File file = buildFile(parent, name, ext);
        int n = 0;
        while (file.exists()) {
            int n2 = n + 1;
            if (n < 32) {
                file = buildFile(parent, name + " (" + n2 + ")", ext);
                n = n2;
            } else {
                throw new FileNotFoundException("Failed to create unique file");
            }
        }
        return file;
    }

    public static File buildUniqueFile(File parent, String mimeType, String displayName) throws FileNotFoundException {
        String[] parts = splitFileName(mimeType, displayName);
        return buildUniqueFileWithExtension(parent, parts[0], parts[1]);
    }

    public static File buildNonUniqueFile(File parent, String mimeType, String displayName) {
        String[] parts = splitFileName(mimeType, displayName);
        return buildFile(parent, parts[0], parts[1]);
    }

    public static File buildUniqueFile(File parent, String displayName) throws FileNotFoundException {
        String ext;
        String name;
        int lastDot = displayName.lastIndexOf(46);
        if (lastDot >= 0) {
            name = displayName.substring(0, lastDot);
            ext = displayName.substring(lastDot + 1);
        } else {
            name = displayName;
            ext = null;
        }
        return buildUniqueFileWithExtension(parent, name, ext);
    }

    public static String[] splitFileName(String mimeType, String displayName) {
        String name;
        String ext;
        String mimeTypeFromExt;
        String extFromMimeType;
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
            ext = null;
            name = displayName;
        } else {
            int lastDot = displayName.lastIndexOf(46);
            if (lastDot >= 0) {
                String name2 = displayName.substring(0, lastDot);
                String ext2 = displayName.substring(lastDot + 1);
                mimeTypeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext2.toLowerCase());
                name = name2;
                ext = ext2;
            } else {
                mimeTypeFromExt = null;
                name = displayName;
                ext = null;
            }
            if (mimeTypeFromExt == null) {
                mimeTypeFromExt = "application/octet-stream";
            }
            if ("application/octet-stream".equals(mimeType)) {
                extFromMimeType = null;
            } else {
                extFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }
            if (!Objects.equals(mimeType, mimeTypeFromExt) && !Objects.equals(ext, extFromMimeType)) {
                name = displayName;
                ext = extFromMimeType;
            }
        }
        if (ext == null) {
            ext = "";
        }
        return new String[]{name, ext};
    }

    private static File buildFile(File parent, String name, String ext) {
        if (TextUtils.isEmpty(ext)) {
            return new File(parent, name);
        }
        return new File(parent, name + "." + ext);
    }

    public static String[] listOrEmpty(File dir) {
        if (dir != null) {
            return ArrayUtils.defeatNullable(dir.list());
        }
        return EmptyArray.STRING;
    }

    public static File[] listFilesOrEmpty(File dir) {
        if (dir != null) {
            return ArrayUtils.defeatNullable(dir.listFiles());
        }
        return ArrayUtils.EMPTY_FILE;
    }

    public static File[] listFilesOrEmpty(File dir, FilenameFilter filter) {
        if (dir != null) {
            return ArrayUtils.defeatNullable(dir.listFiles(filter));
        }
        return ArrayUtils.EMPTY_FILE;
    }

    public static File newFileOrNull(String path) {
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    public static File createDir(File baseDir, String name) {
        File dir = new File(baseDir, name);
        if (createDir(dir)) {
            return dir;
        }
        return null;
    }

    public static boolean createDir(File dir) {
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdir();
    }

    public static long roundStorageSize(long size) {
        long val = 1;
        long pow = 1;
        while (val * pow < size) {
            val <<= 1;
            if (val > 512) {
                val = 1;
                pow *= 1000;
            }
        }
        return val * pow;
    }

    public static void closeQuietly(AutoCloseable closeable) {
        IoUtils.closeQuietly(closeable);
    }

    public static void closeQuietly(FileDescriptor fd) {
        IoUtils.closeQuietly(fd);
    }

    public static int translateModeStringToPosix(String mode) {
        int res;
        for (int i = 0; i < mode.length(); i++) {
            char charAt = mode.charAt(i);
            if (charAt != 'a' && charAt != 'r' && charAt != 't' && charAt != 'w') {
                throw new IllegalArgumentException("Bad mode: " + mode);
            }
        }
        if (mode.startsWith("rw")) {
            res = OsConstants.O_RDWR | OsConstants.O_CREAT;
        } else if (mode.startsWith("w")) {
            res = OsConstants.O_WRONLY | OsConstants.O_CREAT;
        } else if (mode.startsWith("r")) {
            res = OsConstants.O_RDONLY;
        } else {
            throw new IllegalArgumentException("Bad mode: " + mode);
        }
        if (mode.indexOf(116) != -1) {
            res |= OsConstants.O_TRUNC;
        }
        if (mode.indexOf(97) != -1) {
            return res | OsConstants.O_APPEND;
        }
        return res;
    }

    public static String translateModePosixToString(int mode) {
        String res;
        if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_RDWR) {
            res = "rw";
        } else if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_WRONLY) {
            res = "w";
        } else if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_RDONLY) {
            res = "r";
        } else {
            throw new IllegalArgumentException("Bad mode: " + mode);
        }
        if ((OsConstants.O_TRUNC & mode) == OsConstants.O_TRUNC) {
            res = res + IncidentManager.URI_PARAM_TIMESTAMP;
        }
        if ((OsConstants.O_APPEND & mode) != OsConstants.O_APPEND) {
            return res;
        }
        return res + FullBackup.APK_TREE_TOKEN;
    }

    public static int translateModePosixToPfd(int mode) {
        int res;
        if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_RDWR) {
            res = 805306368;
        } else if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_WRONLY) {
            res = 536870912;
        } else if ((OsConstants.O_ACCMODE & mode) == OsConstants.O_RDONLY) {
            res = 268435456;
        } else {
            throw new IllegalArgumentException("Bad mode: " + mode);
        }
        if ((OsConstants.O_CREAT & mode) == OsConstants.O_CREAT) {
            res |= 134217728;
        }
        if ((OsConstants.O_TRUNC & mode) == OsConstants.O_TRUNC) {
            res |= 67108864;
        }
        if ((OsConstants.O_APPEND & mode) == OsConstants.O_APPEND) {
            return res | 33554432;
        }
        return res;
    }

    public static int translateModePfdToPosix(int mode) {
        int res;
        if ((mode & 805306368) == 805306368) {
            res = OsConstants.O_RDWR;
        } else if ((mode & 536870912) == 536870912) {
            res = OsConstants.O_WRONLY;
        } else if ((mode & 268435456) == 268435456) {
            res = OsConstants.O_RDONLY;
        } else {
            throw new IllegalArgumentException("Bad mode: " + mode);
        }
        if ((mode & 134217728) == 134217728) {
            res |= OsConstants.O_CREAT;
        }
        if ((mode & 67108864) == 67108864) {
            res |= OsConstants.O_TRUNC;
        }
        if ((mode & 33554432) == 33554432) {
            return res | OsConstants.O_APPEND;
        }
        return res;
    }

    public static int translateModeAccessToPosix(int mode) {
        if (mode == OsConstants.F_OK) {
            return OsConstants.O_RDONLY;
        }
        if (((OsConstants.R_OK | OsConstants.W_OK) & mode) == (OsConstants.R_OK | OsConstants.W_OK)) {
            return OsConstants.O_RDWR;
        }
        if ((OsConstants.R_OK & mode) == OsConstants.R_OK) {
            return OsConstants.O_RDONLY;
        }
        if ((OsConstants.W_OK & mode) == OsConstants.W_OK) {
            return OsConstants.O_WRONLY;
        }
        throw new IllegalArgumentException("Bad mode: " + mode);
    }

    @VisibleForTesting
    public static class MemoryPipe extends Thread implements AutoCloseable {
        private final byte[] data;
        private final FileDescriptor[] pipe;
        private final boolean sink;

        private MemoryPipe(byte[] data2, boolean sink2) throws IOException {
            try {
                this.pipe = Os.pipe();
                this.data = data2;
                this.sink = sink2;
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }

        private MemoryPipe startInternal() {
            super.start();
            return this;
        }

        public static MemoryPipe createSource(byte[] data2) throws IOException {
            return new MemoryPipe(data2, false).startInternal();
        }

        public static MemoryPipe createSink(byte[] data2) throws IOException {
            return new MemoryPipe(data2, true).startInternal();
        }

        public FileDescriptor getFD() {
            return this.sink ? this.pipe[1] : this.pipe[0];
        }

        public FileDescriptor getInternalFD() {
            return this.sink ? this.pipe[0] : this.pipe[1];
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002a, code lost:
            if (r6.sink != false) goto L_0x0044;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0042, code lost:
            if (r6.sink == false) goto L_0x004d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
            android.os.SystemClock.sleep(java.util.concurrent.TimeUnit.SECONDS.toMillis(1));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
            libcore.io.IoUtils.closeQuietly(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0051, code lost:
            return;
         */
        public void run() {
            FileDescriptor fd = getInternalFD();
            int i = 0;
            while (i < this.data.length) {
                try {
                    if (this.sink) {
                        i += Os.read(fd, this.data, i, this.data.length - i);
                    } else {
                        i += Os.write(fd, this.data, i, this.data.length - i);
                    }
                } catch (ErrnoException | IOException e) {
                } catch (Throwable th) {
                    if (this.sink) {
                        SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
                    }
                    IoUtils.closeQuietly(fd);
                    throw th;
                }
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() throws Exception {
            IoUtils.closeQuietly(getFD());
        }
    }
}
