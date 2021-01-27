package com.huawei.nb.utils.file;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.nb.utils.logger.DbLogUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FileUtils {
    private static final int BUFFER_SIZE = 4096;
    private static final long FILE_COPY_BUFFER_SIZE = 31457280;
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String PATH_WHITE_LIST = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=[];\\',./ ~!@#$%^&*()_+\"{}|:<>?";
    private static final Pattern PATTERN = Pattern.compile("(.*([/\\\\]{1}[\\.\\.]{1,2}|[\\.\\.]{1,2}[/\\\\]{1}|\\.\\.).*|\\.)");

    public static File getFile(String str) throws IOException {
        return getFile(str, null);
    }

    public static File getFile(String str, String str2) throws IOException {
        String canonicalize = canonicalize(str, str2);
        if (canonicalize == null) {
            throw new IOException("get file failed while canonicalizing!");
        } else if (isSafePath(canonicalize)) {
            return new File(checkFile(canonicalize));
        } else {
            throw new IOException("Invalid file filePath, not safe!");
        }
    }

    private static boolean isSafePath(String str) {
        boolean matches = PATTERN.matcher(str).matches();
        if (matches) {
            DSLog.e("Invalid file path : " + DbLogUtil.getSafeNameForLog(str), new Object[0]);
        }
        return !matches;
    }

    private static String checkFile(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            int i2 = 0;
            while (true) {
                if (i2 >= 94) {
                    break;
                } else if (str.charAt(i) == PATH_WHITE_LIST.charAt(i2)) {
                    stringBuffer.append(PATH_WHITE_LIST.charAt(i2));
                    break;
                } else {
                    i2++;
                }
            }
        }
        return stringBuffer.toString();
    }

    public static FileAttribute<Set<PosixFilePermission>> getDefaultFileAttribute(File file, boolean z) {
        return getDefaultFileAttribute(file, z, false);
    }

    public static FileAttribute<Set<PosixFilePermission>> getDefaultFileAttribute(File file, boolean z, boolean z2) {
        UserPrincipal userPrincipal;
        Path path = file.toPath();
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            StringBuilder sb = new StringBuilder();
            sb.append(z ? "rw-r" : "rw--");
            sb.append(z2 ? "w----" : "-----");
            return PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(sb.toString()));
        }
        try {
            userPrincipal = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name"));
        } catch (IOException unused) {
            DSLog.e("FileUtils getDefaultFileAttribute IOException.", new Object[0]);
            userPrincipal = null;
        }
        AclEntryPermission[] aclEntryPermissionArr = {AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.READ_NAMED_ATTRS, AclEntryPermission.READ_ACL, AclEntryPermission.WRITE_DATA, AclEntryPermission.DELETE, AclEntryPermission.APPEND_DATA, AclEntryPermission.WRITE_ATTRIBUTES, AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.WRITE_ACL, AclEntryPermission.SYNCHRONIZE};
        EnumSet noneOf = EnumSet.noneOf(AclEntryPermission.class);
        for (AclEntryPermission aclEntryPermission : aclEntryPermissionArr) {
            noneOf.add(aclEntryPermission);
        }
        if (userPrincipal == null) {
            return null;
        }
        final AclEntry build = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(userPrincipal).setPermissions(noneOf).setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT).build();
        return new FileAttribute<List<AclEntry>>() {
            /* class com.huawei.nb.utils.file.FileUtils.AnonymousClass1 */

            @Override // java.nio.file.attribute.FileAttribute
            public String name() {
                return "acl:acl";
            }

            @Override // java.nio.file.attribute.FileAttribute
            public List<AclEntry> value() {
                ArrayList arrayList = new ArrayList();
                arrayList.add(build);
                return arrayList;
            }
        };
    }

    private static int getDirMode(boolean z, boolean z2) {
        int i = OsConstants.S_IRUSR | OsConstants.S_IWUSR | OsConstants.S_IXUSR;
        if (z) {
            i |= OsConstants.S_IRGRP;
        }
        if (z2) {
            i |= OsConstants.S_IWGRP;
        }
        return (!z || !z2) ? i : i | OsConstants.S_IXGRP;
    }

    private static int getFileMode(boolean z, boolean z2) {
        int i = OsConstants.S_IRUSR | OsConstants.S_IWUSR;
        if (z) {
            i |= OsConstants.S_IRGRP;
        }
        return z2 ? i | OsConstants.S_IWGRP : i;
    }

    public static boolean mkdir(File file, int i, int i2, boolean z, boolean z2) {
        if (file.mkdir() && (i >= 0 || i2 >= 0)) {
            try {
                String canonicalPath = file.getCanonicalPath();
                Os.chown(canonicalPath, i, i2);
                Os.chmod(canonicalPath, getDirMode(z, z2));
                return true;
            } catch (ErrnoException | IOException unused) {
                if (!file.delete()) {
                    DSLog.w("mkdir a dir with wrong uid/gid remains.", new Object[0]);
                }
            }
        }
        return false;
    }

    public static boolean mkdirs(File file, int i, int i2, boolean z, boolean z2) {
        if (file.exists()) {
            return false;
        }
        if (mkdir(file, i, i2, z, z2)) {
            return true;
        }
        try {
            File canonicalFile = file.getCanonicalFile();
            File parentFile = canonicalFile.getParentFile();
            if (parentFile == null) {
                return false;
            }
            if ((parentFile.exists() || mkdirs(parentFile, i, i2, z, z2)) && mkdir(canonicalFile, i, i2, z, z2)) {
                return true;
            }
            return false;
        } catch (IOException unused) {
            return false;
        }
    }

    public static OutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, -1, -1, false, false);
    }

    public static OutputStream openOutputStream(File file, int i, int i2, boolean z, boolean z2) throws IOException {
        return openOutputStream(file, i, i2, z, z2, StandardOpenOption.WRITE);
    }

    public static OutputStream openOutputStream(File file, int i, int i2, boolean z, boolean z2, StandardOpenOption standardOpenOption) throws IOException {
        boolean exists = file.exists();
        try {
            String canonicalPath = file.getCanonicalPath();
            File file2 = getFile(canonicalPath);
            Path path = file2.toPath();
            Files.newByteChannel(path, EnumSet.of(StandardOpenOption.CREATE, standardOpenOption), getDefaultFileAttribute(file2, z, z2)).close();
            OutputStream newOutputStream = Files.newOutputStream(path, standardOpenOption);
            if (!exists) {
                setFileAccessPermission(canonicalPath, i, i2, z, z2);
            }
            return newOutputStream;
        } catch (ErrnoException | IOException e) {
            closeCloseable(null);
            throw new IOException(e.getMessage());
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean z) throws IOException {
        if (!file.exists()) {
            forceMkdirParent(file);
        } else if (file.isDirectory() || !file.canWrite()) {
            throw new IOException("File is a directory or un-writable");
        }
        return new FileOutputStream(file, z);
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        return new FileInputStream(getFile(file.getCanonicalPath()));
    }

    public static void setFileAccessPermission(String str, int i, int i2, boolean z, boolean z2) throws ErrnoException {
        if (i >= 0 || i2 >= 0) {
            Os.chown(str, i, i2);
            Os.chmod(str, getFileMode(z, z2));
        }
    }

    public static File getOutputFile(String str, int i, int i2, boolean z, boolean z2) throws IOException {
        File file = getFile(str);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !mkdirs(parentFile, i, i2, z, z2) && !parentFile.isDirectory()) {
                throw new IOException("Directory '" + DbLogUtil.getSafeNameForLog(parentFile.toString()) + "' could not be created");
            }
        } else if (file.isDirectory()) {
            throw new IOException("File '" + DbLogUtil.getSafeNameForLog(file.toString()) + "' exists but is a directory");
        } else if (!file.canWrite()) {
            throw new IOException("File '" + DbLogUtil.getSafeNameForLog(file.toString()) + "' cannot be written to");
        }
        return file;
    }

    public static String canonicalize(String str, String str2) {
        String str3;
        File file;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        boolean isEmpty = TextUtils.isEmpty(str2);
        String normalize = Normalizer.normalize(str, Normalizer.Form.NFKC);
        if (isEmpty) {
            str3 = "";
        } else {
            str3 = Normalizer.normalize(str2, Normalizer.Form.NFKC);
        }
        if (isEmpty) {
            try {
                file = new File(normalize);
            } catch (IOException unused) {
                return null;
            }
        } else {
            file = new File(normalize, str3);
        }
        return file.getCanonicalPath();
    }

    public static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                DSLog.e("closeClosable failed.", new Object[0]);
            }
        }
    }

    public static List<File> getAllFileInDir(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            getAllFile(str, arrayList);
        } catch (IOException e) {
            DSLog.w(" IOException happened while checkLogDataFiles.err:" + e.getMessage(), new Object[0]);
        }
        return arrayList;
    }

    private static void getAllFile(String str, List<File> list) throws IOException {
        File file = new File(str);
        if (list == null || !file.exists()) {
            DSLog.e(" pathName is not exists or List null.", new Object[0]);
        } else if (!file.isDirectory()) {
            if (file.isFile()) {
                list.add(file);
            }
            DSLog.e(" pathName is not a dir nor file.", new Object[0]);
        } else {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File file2 : listFiles) {
                    list.add(file2);
                    if (file2.isDirectory()) {
                        getAllFile(file2.getCanonicalPath(), list);
                    }
                }
            }
        }
    }

    public static boolean deleteDir(String str) {
        try {
            forceDelete(getFile(str));
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public static boolean deleteDir(File file) {
        try {
            forceDelete(file);
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public static void forceDelete(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is null to delete");
        } else if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean exists = file.exists();
            if (file.delete()) {
                return;
            }
            if (!exists) {
                throw new FileNotFoundException("File does not exist: " + DbLogUtil.getSafeNameForLog(file.toString()));
            }
            throw new IOException("Unable to delete file: " + DbLogUtil.getSafeNameForLog(file.toString()));
        }
    }

    private static void deleteDirectory(File file) throws IOException {
        if (file.exists()) {
            cleanDirectory(file);
            if (!file.delete()) {
                throw new IOException("Unable to delete directory " + DbLogUtil.getSafeNameForLog(file.toString()));
            }
        }
    }

    private static void cleanDirectory(File file) throws IOException {
        IOException e = null;
        for (File file2 : listFiles(file)) {
            try {
                forceDelete(file2);
            } catch (IOException e2) {
                e = e2;
            }
        }
        if (e != null) {
            throw e;
        }
    }

    private static File[] listFiles(File file) {
        File[] listFiles = (!file.exists() || !file.isDirectory()) ? null : file.listFiles();
        return listFiles == null ? new File[0] : listFiles;
    }

    public static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream fileOutputStream;
        Throwable th;
        try {
            fileOutputStream = openOutputStream(file, false);
            try {
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        closeCloseable(fileOutputStream);
                        closeCloseable(inputStream);
                        return;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                closeCloseable(fileOutputStream);
                closeCloseable(inputStream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            closeCloseable(fileOutputStream);
            closeCloseable(inputStream);
            throw th;
        }
    }

    public static String readFileToString(File file, String str, long j) throws IOException {
        if (file == null || TextUtils.isEmpty(str)) {
            throw new IOException(" input params contain null!");
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openInputStream(file);
            return readFileToString(fileInputStream, str, j);
        } finally {
            closeCloseable(fileInputStream);
        }
    }

    public static String readFileToString(InputStream inputStream, String str, long j) throws IOException {
        Throwable th;
        BufferedInputStream bufferedInputStream;
        if (inputStream == null || TextUtils.isEmpty(str)) {
            throw new IOException(" input params contain null!");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            try {
                byte[] bArr = new byte[4096];
                long j2 = 0;
                while (true) {
                    int read = bufferedInputStream.read(bArr);
                    if (read != -1) {
                        j2 += (long) read;
                        if (j2 <= j) {
                            byteArrayOutputStream.write(bArr, 0, read);
                        } else {
                            throw new IOException(" file is too large!");
                        }
                    } else {
                        closeCloseable(bufferedInputStream);
                        closeCloseable(byteArrayOutputStream);
                        return byteArrayOutputStream.toString(str);
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                closeCloseable(bufferedInputStream);
                closeCloseable(byteArrayOutputStream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedInputStream = null;
            closeCloseable(bufferedInputStream);
            closeCloseable(byteArrayOutputStream);
            throw th;
        }
    }

    public static void writeStringToFile(File file, String str, boolean z, String str2) throws IOException {
        BufferedOutputStream bufferedOutputStream;
        Throwable th;
        FileOutputStream fileOutputStream;
        if (file == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            throw new IOException(" input params contain null!");
        }
        try {
            fileOutputStream = openOutputStream(file, z);
            try {
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            } catch (Throwable th2) {
                th = th2;
                bufferedOutputStream = null;
                closeCloseable(fileOutputStream);
                closeCloseable(bufferedOutputStream);
                throw th;
            }
            try {
                bufferedOutputStream.write(str.getBytes(str2));
                bufferedOutputStream.flush();
                closeCloseable(fileOutputStream);
                closeCloseable(bufferedOutputStream);
            } catch (Throwable th3) {
                th = th3;
                closeCloseable(fileOutputStream);
                closeCloseable(bufferedOutputStream);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            fileOutputStream = null;
            bufferedOutputStream = null;
            closeCloseable(fileOutputStream);
            closeCloseable(bufferedOutputStream);
            throw th;
        }
    }

    public static void writeLineToFile(File file, String str, String str2) throws IOException {
        if (str != null) {
            writeStringToFile(file, str + LINE_SEPARATOR, true, str2);
        }
    }

    public static void copyFile(File file, File file2) throws IOException {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Source file or destination file is null");
        } else if (!file.exists() || file.isDirectory()) {
            throw new IOException("Source file does not exists or is a directory");
        } else if (!file.getCanonicalPath().equals(file2.getCanonicalPath())) {
            File parentFile = file2.getParentFile();
            if (parentFile != null && !parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination parent directory cannot be created");
            } else if (file2.exists() && !file2.canWrite()) {
                throw new IOException("Destination file exists but is read-only");
            } else if (!file2.exists() || !file2.isDirectory()) {
                doCopyFile(file, file2);
            } else {
                throw new IOException("Destination file exists but is a directory");
            }
        } else {
            throw new IOException("Source and destination files are the same");
        }
    }

    private static void doCopyFile(File file, File file2) throws IOException {
        FileChannel fileChannel;
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream;
        Throwable th;
        FileChannel channel;
        try {
            fileInputStream = new FileInputStream(file);
            try {
                fileOutputStream = new FileOutputStream(file2);
                try {
                    channel = fileInputStream.getChannel();
                } catch (Throwable th2) {
                    th = th2;
                    fileChannel = null;
                    closeCloseable(null);
                    closeCloseable(fileOutputStream);
                    closeCloseable(fileChannel);
                    closeCloseable(fileInputStream);
                    throw th;
                }
                try {
                    FileChannel channel2 = fileOutputStream.getChannel();
                    long size = channel.size();
                    long j = 0;
                    while (j < size) {
                        long j2 = size - j;
                        long transferFrom = channel2.transferFrom(channel, j, j2 > FILE_COPY_BUFFER_SIZE ? 31457280 : j2);
                        if (transferFrom == 0) {
                            break;
                        }
                        j += transferFrom;
                    }
                    closeCloseable(channel2);
                    closeCloseable(fileOutputStream);
                    closeCloseable(channel);
                    closeCloseable(fileInputStream);
                    if (file.length() != file2.length()) {
                        throw new IOException("Failed to copy full contents from source to destination");
                    } else if (!file2.setLastModified(file.lastModified())) {
                        DSLog.e(" Set last modified failed while do copy file.", new Object[0]);
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileChannel = channel;
                    closeCloseable(null);
                    closeCloseable(fileOutputStream);
                    closeCloseable(fileChannel);
                    closeCloseable(fileInputStream);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = null;
                fileChannel = fileOutputStream;
                closeCloseable(null);
                closeCloseable(fileOutputStream);
                closeCloseable(fileChannel);
                closeCloseable(fileInputStream);
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            fileInputStream = null;
            fileOutputStream = null;
            fileChannel = fileOutputStream;
            closeCloseable(null);
            closeCloseable(fileOutputStream);
            closeCloseable(fileChannel);
            closeCloseable(fileInputStream);
            throw th;
        }
    }

    public static void moveFile(File file, File file2) throws IOException {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Source file or destination file is null");
        } else if (!file.exists() || file.isDirectory()) {
            throw new IOException("Source file does not exists or is a directory");
        } else if (file2.exists() || file2.isDirectory()) {
            throw new IOException("Destination file exists or is a directory");
        } else if (!file.renameTo(file2)) {
            copyFile(file, file2);
            if (!file.delete()) {
                deleteDir(file2);
                throw new IOException("Failed to delete original file after copy");
            }
        }
    }

    public static void forceMkdirParent(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            forceMkdir(parentFile);
        }
    }

    public static void forceMkdir(File file) throws IOException {
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IOException("Unable to create directory, as the file exists and is not a directory");
            }
        } else if (!file.mkdirs() && !file.isDirectory()) {
            throw new IOException("Unable to create directory");
        }
    }

    public static boolean fileExists(String str) {
        try {
            return getFile(str).exists();
        } catch (IOException unused) {
            return false;
        }
    }
}
