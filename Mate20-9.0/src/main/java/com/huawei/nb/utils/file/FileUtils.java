package com.huawei.nb.utils.file;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import com.huawei.nb.utils.logger.DSLog;
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
import java.nio.file.OpenOption;
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
    private static final Pattern pattern = Pattern.compile("(.*([/\\\\]{1}[\\.\\.]{1,2}|[\\.\\.]{1,2}[/\\\\]{1}|\\.\\.).*|\\.)");

    public static File getFile(String filePath) throws IOException {
        return getFile(filePath, null);
    }

    public static File getFile(String filePath, String fileName) throws IOException {
        String canonicalizePath = canonicalize(filePath, fileName);
        if (canonicalizePath == null) {
            throw new IOException("get file failed while canonicalizing!");
        } else if (isSafePath(canonicalizePath)) {
            return new File(checkFile(canonicalizePath));
        } else {
            throw new IOException("Invalid file filePath, not safe!");
        }
    }

    private static boolean isSafePath(String filePath) {
        boolean isNotSafe = pattern.matcher(filePath).matches();
        if (isNotSafe) {
            DSLog.e("Invalid file path : " + filePath, new Object[0]);
        }
        if (!isNotSafe) {
            return true;
        }
        return false;
    }

    private static String checkFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        StringBuffer tmpStrBuf = new StringBuffer();
        int filePathLength = filePath.length();
        int whiteFilePathLength = PATH_WHITE_LIST.length();
        for (int i = 0; i < filePathLength; i++) {
            int j = 0;
            while (true) {
                if (j >= whiteFilePathLength) {
                    break;
                } else if (filePath.charAt(i) == PATH_WHITE_LIST.charAt(j)) {
                    tmpStrBuf.append(PATH_WHITE_LIST.charAt(j));
                    break;
                } else {
                    j++;
                }
            }
        }
        return tmpStrBuf.toString();
    }

    public static FileAttribute<Set<PosixFilePermission>> getDefaultFileAttribute(File file, boolean isReadShare) {
        return getDefaultFileAttribute(file, isReadShare, false);
    }

    public static FileAttribute<Set<PosixFilePermission>> getDefaultFileAttribute(File file, boolean isReadShare, boolean isWriteShare) {
        Path path = file.toPath();
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            return PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString((isReadShare ? "rw-r" : "rw--") + (isWriteShare ? "w----" : "-----")));
        }
        UserPrincipal user = null;
        try {
            user = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name"));
        } catch (IOException e) {
            DSLog.e("FileUtils getDefaultFileAttribute IOException.", new Object[0]);
        }
        AclEntryPermission[] permList = {AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.READ_NAMED_ATTRS, AclEntryPermission.READ_ACL, AclEntryPermission.WRITE_DATA, AclEntryPermission.DELETE, AclEntryPermission.APPEND_DATA, AclEntryPermission.WRITE_ATTRIBUTES, AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.WRITE_ACL, AclEntryPermission.SYNCHRONIZE};
        Set<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        for (AclEntryPermission perm : permList) {
            perms.add(perm);
        }
        if (user == null) {
            return null;
        }
        final AclEntry entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(user).setPermissions(perms).setFlags(new AclEntryFlag[]{AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT}).build();
        return new FileAttribute<List<AclEntry>>() {
            public String name() {
                return "acl:acl";
            }

            public List<AclEntry> value() {
                ArrayList<AclEntry> l = new ArrayList<>();
                l.add(entry);
                return l;
            }
        };
    }

    private static int getDirMode(boolean isGroupReadShare, boolean isGroupWriteShare) {
        int mode = OsConstants.S_IRUSR | OsConstants.S_IWUSR | OsConstants.S_IXUSR;
        if (isGroupReadShare) {
            mode |= OsConstants.S_IRGRP;
        }
        if (isGroupWriteShare) {
            mode |= OsConstants.S_IWGRP;
        }
        if (!isGroupReadShare || !isGroupWriteShare) {
            return mode;
        }
        return mode | OsConstants.S_IXGRP;
    }

    private static int getFileMode(boolean isGroupReadShare, boolean isGroupWriteShare) {
        int mode = OsConstants.S_IRUSR | OsConstants.S_IWUSR;
        if (isGroupReadShare) {
            mode |= OsConstants.S_IRGRP;
        }
        if (isGroupWriteShare) {
            return mode | OsConstants.S_IWGRP;
        }
        return mode;
    }

    public static boolean mkdir(File file, int uid, int gid, boolean isReadShare, boolean isWriteShare) {
        if (!file.mkdir()) {
            return false;
        }
        if (uid < 0 && gid < 0) {
            return false;
        }
        try {
            String path = file.getCanonicalPath();
            Os.chown(path, uid, gid);
            Os.chmod(path, getDirMode(isReadShare, isWriteShare));
            return true;
        } catch (ErrnoException | IOException e) {
            if (file.delete()) {
                return false;
            }
            DSLog.w("mkdir a dir with wrong uid/gid remains.", new Object[0]);
            return false;
        }
    }

    public static boolean mkdirs(File file, int uid, int gid, boolean isReadShare, boolean isWriteShare) {
        boolean z = true;
        if (file.exists()) {
            return false;
        }
        if (mkdir(file, uid, gid, isReadShare, isWriteShare)) {
            return true;
        }
        try {
            File canonFile = file.getCanonicalFile();
            File parent = canonFile.getParentFile();
            if (parent == null || ((!parent.exists() && !mkdirs(parent, uid, gid, isReadShare, isWriteShare)) || !mkdir(canonFile, uid, gid, isReadShare, isWriteShare))) {
                z = false;
            }
            return z;
        } catch (IOException e) {
            return false;
        }
    }

    public static OutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, -1, -1, false, false);
    }

    public static OutputStream openOutputStream(File file, int uid, int gid, boolean isReadShare, boolean isWriteShare) throws IOException {
        return openOutputStream(file, uid, gid, isReadShare, isWriteShare, StandardOpenOption.WRITE);
    }

    public static OutputStream openOutputStream(File file, int uid, int gid, boolean isReadShare, boolean isWriteShare, StandardOpenOption option) throws IOException {
        boolean alreadyExists = file.exists();
        try {
            String path = file.getCanonicalPath();
            File safeFile = getFile(path);
            Path safePath = safeFile.toPath();
            FileAttribute<Set<PosixFilePermission>> attribute = getDefaultFileAttribute(safeFile, isReadShare, isWriteShare);
            Files.newByteChannel(safePath, EnumSet.of(StandardOpenOption.CREATE, option), new FileAttribute[]{attribute}).close();
            OutputStream outputStream = Files.newOutputStream(safePath, new OpenOption[]{option});
            if (!alreadyExists) {
                setFileAccessPermission(path, uid, gid, isReadShare, isWriteShare);
            }
            return outputStream;
        } catch (ErrnoException | IOException e) {
            closeCloseable(null);
            throw new IOException(e.getMessage());
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (!file.exists()) {
            forceMkdirParent(file);
        } else if (file.isDirectory() || !file.canWrite()) {
            throw new IOException("File is a directory or un-writable");
        }
        return new FileOutputStream(file, append);
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        return new FileInputStream(getFile(file.getCanonicalPath()));
    }

    public static void setFileAccessPermission(String path, int uid, int gid, boolean isReadShare, boolean isWriteShare) throws ErrnoException {
        if (uid >= 0 || gid >= 0) {
            Os.chown(path, uid, gid);
            Os.chmod(path, getFileMode(isReadShare, isWriteShare));
        }
    }

    public static File getOutputFile(String path, int uid, int gid, boolean isReadShare, boolean isWriteShare) throws IOException {
        File file = getFile(path);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !mkdirs(parent, uid, gid, isReadShare, isWriteShare) && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        } else if (file.isDirectory()) {
            throw new IOException("File '" + file + "' exists but is a directory");
        } else if (!file.canWrite()) {
            throw new IOException("File '" + file + "' cannot be written to");
        }
        return file;
    }

    public static String canonicalize(String filePath, String fileName) {
        File tmpFile;
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        boolean onlyUseBasePath = TextUtils.isEmpty(fileName);
        String nomalFilePath = Normalizer.normalize(filePath, Normalizer.Form.NFKC);
        String nomalFileName = onlyUseBasePath ? "" : Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        if (onlyUseBasePath) {
            try {
                tmpFile = new File(nomalFilePath);
            } catch (IOException e) {
                return null;
            }
        } else {
            tmpFile = new File(nomalFilePath, nomalFileName);
        }
        return tmpFile.getCanonicalPath();
    }

    public static void closeCloseable(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                DSLog.e("closeClosable failed: " + e.getMessage(), new Object[0]);
            }
        }
    }

    public static List<File> getAllFileInDir(String pathName) {
        List<File> array = new ArrayList<>();
        try {
            getAllFile(pathName, array);
        } catch (IOException e) {
            DSLog.w(" IOException happened while checkLogDataFiles.err:" + e.getMessage(), new Object[0]);
        }
        return array;
    }

    private static void getAllFile(String pathName, List<File> files) throws IOException {
        File dirFile = new File(pathName);
        if (files == null || !dirFile.exists()) {
            DSLog.e(" pathName is not exists or List null.", new Object[0]);
        } else if (!dirFile.isDirectory()) {
            if (dirFile.isFile()) {
                files.add(dirFile);
            }
            DSLog.e(" pathName is not a dir nor file.", new Object[0]);
        } else {
            File[] fileList = dirFile.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    files.add(file);
                    if (file.isDirectory()) {
                        getAllFile(file.getCanonicalPath(), files);
                    }
                }
            }
        }
    }

    public static boolean deleteDir(String dir) {
        try {
            forceDelete(getFile(dir));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteDir(File dataFileDir) {
        try {
            forceDelete(dataFileDir);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void forceDelete(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is null to delete");
        } else if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean fileExists = file.exists();
            if (file.delete()) {
                return;
            }
            if (!fileExists) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            throw new IOException("Unable to delete file: " + file);
        }
    }

    private static void deleteDirectory(File dir) throws IOException {
        if (dir.exists()) {
            cleanDirectory(dir);
            if (!dir.delete()) {
                throw new IOException("Unable to delete directory " + dir);
            }
        }
    }

    private static void cleanDirectory(File directory) throws IOException {
        IOException exception = null;
        for (File file : listFiles(directory)) {
            try {
                forceDelete(file);
            } catch (IOException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    private static File[] listFiles(File dir) {
        File[] files = null;
        if (dir.exists() && dir.isDirectory()) {
            files = dir.listFiles();
        }
        return files == null ? new File[0] : files;
    }

    public static void copyInputStreamToFile(InputStream inputStream, File outputFile) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = openOutputStream(outputFile, false);
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int n = inputStream.read(buffer);
                if (n != -1) {
                    outputStream.write(buffer, 0, n);
                } else {
                    return;
                }
            }
        } finally {
            closeCloseable(outputStream);
            closeCloseable(inputStream);
        }
    }

    public static String readFileToString(File file, String charset, long maxFileLength) throws IOException {
        if (file == null || TextUtils.isEmpty(charset)) {
            throw new IOException(" input params contain null!");
        }
        InputStream inputStream = null;
        try {
            inputStream = openInputStream(file);
            return readFileToString(inputStream, charset, maxFileLength);
        } finally {
            closeCloseable(inputStream);
        }
    }

    public static String readFileToString(InputStream inputStream, String charset, long maxFileLength) throws IOException {
        if (inputStream == null || TextUtils.isEmpty(charset)) {
            throw new IOException(" input params contain null!");
        }
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(inputStream);
            try {
                byte[] bb = new byte[BUFFER_SIZE];
                long totalLength = 0;
                while (true) {
                    int len = bufferedInputStream2.read(bb);
                    if (len != -1) {
                        totalLength += (long) len;
                        if (totalLength > maxFileLength) {
                            throw new IOException(" file is too large!");
                        }
                        byteArrayOutputStream.write(bb, 0, len);
                    } else {
                        closeCloseable(bufferedInputStream2);
                        closeCloseable(byteArrayOutputStream);
                        return byteArrayOutputStream.toString(charset);
                    }
                }
            } catch (Throwable th) {
                th = th;
                bufferedInputStream = bufferedInputStream2;
            }
        } catch (Throwable th2) {
            th = th2;
            closeCloseable(bufferedInputStream);
            closeCloseable(byteArrayOutputStream);
            throw th;
        }
    }

    public static void writeStringToFile(File file, String str, boolean append, String charset) throws IOException {
        if (file == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(charset)) {
            throw new IOException(" input params contain null!");
        }
        OutputStream os = null;
        BufferedOutputStream bos = null;
        try {
            os = openOutputStream(file, append);
            BufferedOutputStream bos2 = new BufferedOutputStream(os);
            try {
                bos2.write(str.getBytes(charset));
                bos2.flush();
                closeCloseable(os);
                closeCloseable(bos2);
            } catch (Throwable th) {
                th = th;
                bos = bos2;
                closeCloseable(os);
                closeCloseable(bos);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            closeCloseable(os);
            closeCloseable(bos);
            throw th;
        }
    }

    public static void writeLineToFile(File file, String line, String charset) throws IOException {
        if (line != null) {
            writeStringToFile(file, line + LINE_SEPARATOR, true, charset);
        }
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (srcFile == null || destFile == null) {
            throw new IllegalArgumentException("Source file or destination file is null");
        } else if (!srcFile.exists() || srcFile.isDirectory()) {
            throw new IOException("Source file does not exists or is a directory");
        } else if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source and destination files are the same");
        } else {
            File destParentFile = destFile.getParentFile();
            if (destParentFile != null && !destParentFile.mkdirs() && !destParentFile.isDirectory()) {
                throw new IOException("Destination parent directory cannot be created");
            } else if (destFile.exists() && !destFile.canWrite()) {
                throw new IOException("Destination file exists but is read-only");
            } else if (!destFile.exists() || !destFile.isDirectory()) {
                doCopyFile(srcFile, destFile);
            } else {
                throw new IOException("Destination file exists but is a directory");
            }
        }
    }

    private static void doCopyFile(File srcFile, File destFile) throws IOException {
        FileOutputStream outputStream;
        long count;
        FileInputStream inputStream = null;
        FileOutputStream outputStream2 = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(srcFile);
            try {
                outputStream = new FileOutputStream(destFile);
            } catch (Throwable th) {
                th = th;
                inputStream = inputStream2;
                closeCloseable(null);
                closeCloseable(outputStream2);
                closeCloseable(null);
                closeCloseable(inputStream);
                throw th;
            }
            try {
                FileChannel inputChannel = inputStream2.getChannel();
                FileChannel outputChannel = outputStream.getChannel();
                long totalSize = inputChannel.size();
                long pos = 0;
                while (pos < totalSize) {
                    long remainSize = totalSize - pos;
                    if (remainSize > FILE_COPY_BUFFER_SIZE) {
                        count = FILE_COPY_BUFFER_SIZE;
                    } else {
                        count = remainSize;
                    }
                    long copiedCount = outputChannel.transferFrom(inputChannel, pos, count);
                    if (copiedCount == 0) {
                        break;
                    }
                    pos += copiedCount;
                }
                closeCloseable(outputChannel);
                closeCloseable(outputStream);
                closeCloseable(inputChannel);
                closeCloseable(inputStream2);
                if (srcFile.length() != destFile.length()) {
                    throw new IOException("Failed to copy full contents from source to destination");
                }
                destFile.setLastModified(srcFile.lastModified());
            } catch (Throwable th2) {
                th = th2;
                outputStream2 = outputStream;
                inputStream = inputStream2;
                closeCloseable(null);
                closeCloseable(outputStream2);
                closeCloseable(null);
                closeCloseable(inputStream);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            closeCloseable(null);
            closeCloseable(outputStream2);
            closeCloseable(null);
            closeCloseable(inputStream);
            throw th;
        }
    }

    public static void moveFile(File srcFile, File destFile) throws IOException {
        if (srcFile == null || destFile == null) {
            throw new IllegalArgumentException("Source file or destination file is null");
        } else if (!srcFile.exists() || srcFile.isDirectory()) {
            throw new IOException("Source file does not exists or is a directory");
        } else if (destFile.exists() || destFile.isDirectory()) {
            throw new IOException("Destination file exists or is a directory");
        } else if (!srcFile.renameTo(destFile)) {
            copyFile(srcFile, destFile);
            if (!srcFile.delete()) {
                deleteDir(destFile);
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

    public static void forceMkdir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory, as the file exists and is not a directory");
            }
        } else if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Unable to create directory");
        }
    }

    public static boolean fileExists(String path) {
        try {
            return getFile(path).exists();
        } catch (IOException e) {
            return false;
        }
    }
}
