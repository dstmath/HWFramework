package android.os;

import android.net.ProxyInfo;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.webkit.MimeTypeMap;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import libcore.util.EmptyArray;

public class FileUtils {
    private static final File[] EMPTY = new File[0];
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

    private static class NoImagePreloadHolder {
        public static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[\\w%+,./=_-]+");

        private NoImagePreloadHolder() {
        }
    }

    public static int setPermissions(File path, int mode, int uid, int gid) {
        return setPermissions(path.getAbsolutePath(), mode, uid, gid);
    }

    public static int setPermissions(String path, int mode, int uid, int gid) {
        try {
            Os.chmod(path, mode);
            if (uid >= 0 || gid >= 0) {
                try {
                    Os.chown(path, uid, gid);
                } catch (ErrnoException e) {
                    Slog.w(TAG, "Failed to chown(" + path + "): " + e);
                    return e.errno;
                }
            }
            return 0;
        } catch (ErrnoException e2) {
            if (uid >= 0 || gid >= 0) {
                Slog.w(TAG, "Failed to chmod(" + path + "): " + e2);
            }
            return e2.errno;
        }
    }

    public static int setPermissions(FileDescriptor fd, int mode, int uid, int gid) {
        try {
            Os.fchmod(fd, mode);
            if (uid >= 0 || gid >= 0) {
                try {
                    Os.fchown(fd, uid, gid);
                } catch (ErrnoException e) {
                    Slog.w(TAG, "Failed to fchown(): " + e);
                    return e.errno;
                }
            }
            return 0;
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

    public static int getUid(String path) {
        try {
            return Os.stat(path).st_uid;
        } catch (ErrnoException e) {
            return -1;
        }
    }

    public static boolean sync(FileOutputStream stream) {
        if (stream != null) {
            try {
                stream.getFD().sync();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static boolean copyFile(File srcFile, File destFile) {
        try {
            copyFileOrThrow(srcFile, destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x001c A:{SYNTHETIC, Splitter: B:17:0x001c} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void copyFileOrThrow(File srcFile, File destFile) throws IOException {
        Throwable th;
        Throwable th2 = null;
        InputStream in = null;
        try {
            InputStream in2 = new FileInputStream(srcFile);
            try {
                copyToFileOrThrow(in2, destFile);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (in != null) {
            }
            if (th2 == null) {
            }
        }
    }

    @Deprecated
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            copyToFileOrThrow(inputStream, destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void copyToFileOrThrow(InputStream inputStream, File destFile) throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        FileOutputStream out = new FileOutputStream(destFile);
        try {
            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead < 0) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
            }
            try {
                out.getFD().sync();
            } catch (IOException e) {
            }
            out.close();
        } finally {
            out.flush();
            try {
                out.getFD().sync();
            } catch (IOException e2) {
            }
            out.close();
        }
    }

    public static boolean isFilenameSafe(File file) {
        return NoImagePreloadHolder.SAFE_FILENAME_PATTERN.matcher(file.getPath()).matches();
    }

    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        InputStream input = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(input);
        try {
            long size = file.length();
            byte[] data;
            String str;
            int len;
            if (max > 0 || (size > 0 && max == 0)) {
                if (size > 0 && (max == 0 || size < ((long) max))) {
                    max = (int) size;
                }
                data = new byte[(max + 1)];
                int length = bis.read(data);
                if (length <= 0) {
                    str = ProxyInfo.LOCAL_EXCL_LIST;
                    return str;
                } else if (length <= max) {
                    str = new String(data, 0, length);
                    bis.close();
                    input.close();
                    return str;
                } else if (ellipsis == null) {
                    str = new String(data, 0, max);
                    bis.close();
                    input.close();
                    return str;
                } else {
                    str = new String(data, 0, max) + ellipsis;
                    bis.close();
                    input.close();
                    return str;
                }
            } else if (max < 0) {
                boolean rolled = false;
                byte[] last = null;
                data = null;
                while (true) {
                    if (last != null) {
                        rolled = true;
                    }
                    byte[] tmp = last;
                    last = data;
                    data = tmp;
                    if (tmp == null) {
                        data = new byte[(-max)];
                    }
                    len = bis.read(data);
                    if (len != data.length) {
                        break;
                    }
                }
                if (last == null && len <= 0) {
                    str = ProxyInfo.LOCAL_EXCL_LIST;
                    bis.close();
                    input.close();
                    return str;
                } else if (last == null) {
                    str = new String(data, 0, len);
                    bis.close();
                    input.close();
                    return str;
                } else {
                    if (len > 0) {
                        rolled = true;
                        System.arraycopy(last, len, last, 0, last.length - len);
                        System.arraycopy(data, 0, last, last.length - len, len);
                    }
                    if (ellipsis == null || (rolled ^ 1) != 0) {
                        str = new String(last);
                        bis.close();
                        input.close();
                        return str;
                    }
                    str = ellipsis + new String(last);
                    bis.close();
                    input.close();
                    return str;
                }
            } else {
                ByteArrayOutputStream contents = new ByteArrayOutputStream();
                data = new byte[1024];
                while (true) {
                    len = bis.read(data);
                    if (len > 0) {
                        contents.write(data, 0, len);
                    }
                    if (len != data.length) {
                        str = contents.toString();
                        bis.close();
                        input.close();
                        return str;
                    }
                }
            }
        } finally {
            bis.close();
            input.close();
        }
    }

    public static void stringToFile(File file, String string) throws IOException {
        stringToFile(file.getAbsolutePath(), string);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x001c A:{SYNTHETIC, Splitter: B:17:0x001c} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void bytesToFile(String filename, byte[] content) throws IOException {
        Throwable th;
        Throwable th2 = null;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(filename);
            try {
                fos2.write(content);
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                fos = fos2;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (fos != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public static void stringToFile(String filename, String string) throws IOException {
        bytesToFile(filename, string.getBytes(StandardCharsets.UTF_8));
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0029 A:{SYNTHETIC, Splitter: B:16:0x0029} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long checksumCrc32(File file) throws FileNotFoundException, IOException {
        Throwable th;
        CRC32 checkSummer = new CRC32();
        CheckedInputStream cis = null;
        try {
            CheckedInputStream cis2 = new CheckedInputStream(new FileInputStream(file), checkSummer);
            try {
                do {
                } while (cis2.read(new byte[128]) >= 0);
                long value = checkSummer.getValue();
                if (cis2 != null) {
                    try {
                        cis2.close();
                    } catch (IOException e) {
                    }
                }
                return value;
            } catch (Throwable th2) {
                th = th2;
                cis = cis2;
                if (cis != null) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    public static boolean deleteOlderFiles(File dir, int minCount, long minAge) {
        if (minCount < 0 || minAge < 0) {
            throw new IllegalArgumentException("Constraints must be positive or 0");
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File lhs, File rhs) {
                return Long.compare(rhs.lastModified(), lhs.lastModified());
            }
        });
        boolean deleted = false;
        for (int i = minCount; i < files.length; i++) {
            File file = files[i];
            if (System.currentTimeMillis() - file.lastModified() > minAge && file.delete()) {
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

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean z = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    z &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    z = false;
                }
            }
        }
        return z;
    }

    private static boolean isValidExtFilenameChar(char c) {
        switch (c) {
            case 0:
            case '/':
                return false;
            default:
                return true;
        }
    }

    public static boolean isValidExtFilename(String name) {
        return name != null ? name.equals(buildValidExtFilename(name)) : false;
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
        if (c >= 0 && c <= 31) {
            return false;
        }
        switch (c) {
            case '\"':
            case '*':
            case '/':
            case ':':
            case '<':
            case '>':
            case '?':
            case '\\':
            case '|':
            case 127:
                return false;
            default:
                return true;
        }
    }

    public static boolean isValidFatFilename(String name) {
        return name != null ? name.equals(buildValidFatFilename(name)) : false;
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

    public static String trimFilename(String str, int maxBytes) {
        StringBuilder res = new StringBuilder(str);
        trimFilename(res, maxBytes);
        return res.toString();
    }

    private static void trimFilename(StringBuilder res, int maxBytes) {
        byte[] raw = res.toString().getBytes(StandardCharsets.UTF_8);
        if (raw.length > maxBytes) {
            maxBytes -= 3;
            while (raw.length > maxBytes) {
                res.deleteCharAt(res.length() / 2);
                raw = res.toString().getBytes(StandardCharsets.UTF_8);
            }
            res.insert(res.length() / 2, "...");
        }
    }

    public static String rewriteAfterRename(File beforeDir, File afterDir, String path) {
        String str = null;
        if (path == null) {
            return null;
        }
        File result = rewriteAfterRename(beforeDir, afterDir, new File(path));
        if (result != null) {
            str = result.getAbsolutePath();
        }
        return str;
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

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            if (n >= 32) {
                throw new FileNotFoundException("Failed to create unique file");
            }
            file = buildFile(parent, name + " (" + n2 + ")", ext);
            n = n2;
        }
        return file;
    }

    public static File buildUniqueFile(File parent, String mimeType, String displayName) throws FileNotFoundException {
        String[] parts = splitFileName(mimeType, displayName);
        return buildUniqueFileWithExtension(parent, parts[0], parts[1]);
    }

    public static File buildUniqueFile(File parent, String displayName) throws FileNotFoundException {
        String name;
        String ext;
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
        Object ext;
        if ("vnd.android.document/directory".equals(mimeType)) {
            name = displayName;
            ext = null;
        } else {
            Object mimeTypeFromExt;
            int lastDot = displayName.lastIndexOf(46);
            if (lastDot >= 0) {
                name = displayName.substring(0, lastDot);
                ext = displayName.substring(lastDot + 1);
                mimeTypeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
            } else {
                name = displayName;
                ext = null;
                mimeTypeFromExt = null;
            }
            if (mimeTypeFromExt == null) {
                mimeTypeFromExt = "application/octet-stream";
            }
            String extFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!(Objects.equals(mimeType, mimeTypeFromExt) || Objects.equals(ext, extFromMimeType))) {
                name = displayName;
                String ext2 = extFromMimeType;
            }
        }
        if (ext2 == null) {
            ext2 = ProxyInfo.LOCAL_EXCL_LIST;
        }
        return new String[]{name, ext2};
    }

    private static File buildFile(File parent, String name, String ext) {
        if (TextUtils.isEmpty(ext)) {
            return new File(parent, name);
        }
        return new File(parent, name + "." + ext);
    }

    public static String[] listOrEmpty(File dir) {
        if (dir == null) {
            return EmptyArray.STRING;
        }
        String[] res = dir.list();
        if (res != null) {
            return res;
        }
        return EmptyArray.STRING;
    }

    public static File[] listFilesOrEmpty(File dir) {
        if (dir == null) {
            return EMPTY;
        }
        File[] res = dir.listFiles();
        if (res != null) {
            return res;
        }
        return EMPTY;
    }

    public static File[] listFilesOrEmpty(File dir, FilenameFilter filter) {
        if (dir == null) {
            return EMPTY;
        }
        File[] res = dir.listFiles(filter);
        if (res != null) {
            return res;
        }
        return EMPTY;
    }

    public static File newFileOrNull(String path) {
        return path != null ? new File(path) : null;
    }

    public static File createDir(File baseDir, String name) {
        File dir = new File(baseDir, name);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                dir = null;
            }
            return dir;
        }
        if (!dir.mkdir()) {
            dir = null;
        }
        return dir;
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
}
