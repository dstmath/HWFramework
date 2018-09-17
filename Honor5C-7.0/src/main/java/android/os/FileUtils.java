package android.os;

import android.bluetooth.BluetoothAssignedNumbers;
import android.media.ToneGenerator;
import android.net.NetworkAgent;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult.InformationElement;
import android.provider.DocumentsContract.Document;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant.Database;
import android.speech.tts.TextToSpeech;
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
import java.io.FileWriter;
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
    private static final File[] EMPTY = null;
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
        public static final Pattern SAFE_FILENAME_PATTERN = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.FileUtils.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.FileUtils.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.FileUtils.NoImagePreloadHolder.<clinit>():void");
        }

        private NoImagePreloadHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.FileUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.FileUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.FileUtils.<clinit>():void");
    }

    public FileUtils() {
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

    public static void copyFileOrThrow(File srcFile, File destFile) throws IOException {
        Throwable th;
        Throwable th2 = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                copyToFileOrThrow(in, destFile);
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                inputStream = in;
                if (inputStream != null) {
                    try {
                        inputStream.close();
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
            if (inputStream != null) {
                inputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
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
            byte[] buffer = new byte[StrictMode.DETECT_VM_REGISTRATION_LEAKS];
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
        long size = file.length();
        byte[] data;
        String str;
        if (max > 0 || (size > 0 && max == 0)) {
            if (size > 0 && (max == 0 || size < ((long) max))) {
                max = (int) size;
            }
            data = new byte[(max + S_IXOTH)];
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
                    try {
                        data = new byte[(-max)];
                    } finally {
                        bis.close();
                        input.close();
                    }
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
                if (ellipsis == null || !rolled) {
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
            data = new byte[Document.FLAG_SUPPORTS_REMOVE];
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
    }

    public static void stringToFile(File file, String string) throws IOException {
        stringToFile(file.getAbsolutePath(), string);
    }

    public static void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }

    public static long checksumCrc32(File file) throws FileNotFoundException, IOException {
        Throwable th;
        CRC32 checkSummer = new CRC32();
        CheckedInputStream checkedInputStream = null;
        try {
            CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), checkSummer);
            try {
                do {
                } while (cis.read(new byte[S_IWUSR]) >= 0);
                long value = checkSummer.getValue();
                if (cis != null) {
                    try {
                        cis.close();
                    } catch (IOException e) {
                    }
                }
                return value;
            } catch (Throwable th2) {
                th = th2;
                checkedInputStream = cis;
                if (checkedInputStream != null) {
                    try {
                        checkedInputStream.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (checkedInputStream != null) {
                checkedInputStream.close();
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
                return (int) (rhs.lastModified() - lhs.lastModified());
            }
        });
        boolean deleted = false;
        for (int i = minCount; i < files.length; i += S_IXOTH) {
            File file = files[i];
            if (System.currentTimeMillis() - file.lastModified() > minAge && file.delete()) {
                Log.d(TAG, "Deleted old file " + file);
                deleted = true;
            }
        }
        return deleted;
    }

    public static boolean contains(File[] dirs, File file) {
        int length = dirs.length;
        for (int i = 0; i < length; i += S_IXOTH) {
            if (contains(dirs[i], file)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(File dir, File file) {
        if (dir == null || file == null) {
            return false;
        }
        String dirPath = dir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
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
            int length = files.length;
            for (int i = 0; i < length; i += S_IXOTH) {
                File file = files[i];
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
            case TextToSpeech.SUCCESS /*0*/:
            case ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_SP_PRI /*47*/:
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
        for (int i = 0; i < name.length(); i += S_IXOTH) {
            char c = name.charAt(i);
            if (isValidExtFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, (int) Process.PROC_TERM_MASK);
        return res.toString();
    }

    private static boolean isValidFatFilenameChar(char c) {
        if (c >= '\u0000' && c <= '\u001f') {
            return false;
        }
        switch (c) {
            case HwSysResource.APPMNGWHITELIST /*34*/:
            case InformationElement.EID_ERP /*42*/:
            case ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_SP_PRI /*47*/:
            case ToneGenerator.TONE_CDMA_LOW_SS /*58*/:
            case NetworkAgent.WIFI_BASE_SCORE /*60*/:
            case ToneGenerator.TONE_CDMA_HIGH_SS_2 /*62*/:
            case ToneGenerator.TONE_CDMA_MED_SS_2 /*63*/:
            case ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK /*92*/:
            case BluetoothAssignedNumbers.A_AND_R_CAMBRIDGE /*124*/:
            case InformationElement.EID_EXTENDED_CAPS /*127*/:
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
        for (int i = 0; i < name.length(); i += S_IXOTH) {
            char c = name.charAt(i);
            if (isValidFatFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, (int) Process.PROC_TERM_MASK);
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
                res.deleteCharAt(res.length() / S_IWOTH);
                raw = res.toString().getBytes(StandardCharsets.UTF_8);
            }
            res.insert(res.length() / S_IWOTH, "...");
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
        for (int i = 0; i < paths.length; i += S_IXOTH) {
            result[i] = rewriteAfterRename(beforeDir, afterDir, paths[i]);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static File rewriteAfterRename(File beforeDir, File afterDir, File file) {
        if (file == null || beforeDir == null || afterDir == null || !contains(beforeDir, file)) {
            return null;
        }
        return new File(afterDir, file.getAbsolutePath().substring(beforeDir.getAbsolutePath().length()));
    }

    public static File buildUniqueFile(File parent, String mimeType, String displayName) throws FileNotFoundException {
        String[] parts = splitFileName(mimeType, displayName);
        String name = parts[0];
        String ext = parts[S_IXOTH];
        File file = buildFile(parent, name, ext);
        int n = 0;
        while (file.exists()) {
            int n2 = n + S_IXOTH;
            if (n >= S_IRGRP) {
                throw new FileNotFoundException("Failed to create unique file");
            }
            file = buildFile(parent, name + " (" + n2 + ")", ext);
            n = n2;
        }
        return file;
    }

    public static String[] splitFileName(String mimeType, String displayName) {
        String name;
        Object obj;
        if (Document.MIME_TYPE_DIR.equals(mimeType)) {
            name = displayName;
            obj = null;
        } else {
            Object mimeTypeFromExtension;
            int lastDot = displayName.lastIndexOf(46);
            if (lastDot >= 0) {
                name = displayName.substring(0, lastDot);
                obj = displayName.substring(lastDot + S_IXOTH);
                mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(obj.toLowerCase());
            } else {
                name = displayName;
                obj = null;
                mimeTypeFromExtension = null;
            }
            if (mimeTypeFromExtension == null) {
                mimeTypeFromExtension = Database.UNKNOWN_MIME_TYPE;
            }
            String extFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!(Objects.equals(mimeType, mimeTypeFromExtension) || Objects.equals(r0, extFromMimeType))) {
                name = displayName;
                String str = extFromMimeType;
            }
        }
        if (obj == null) {
            obj = ProxyInfo.LOCAL_EXCL_LIST;
        }
        String[] strArr = new String[S_IWOTH];
        strArr[0] = name;
        strArr[S_IXOTH] = obj;
        return strArr;
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
}
