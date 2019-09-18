package com.huawei.nb.utils.file;

import com.huawei.nb.utils.logger.DSLog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final int BUFFER_SIZE = 32768;
    private static final int DEFAULT_MAX_SIZE = 1073741824;

    public static String sanitizeFileName(String fileName, String intendedDir) throws IOException {
        String fileCanonicalPath = FileUtils.getFile(intendedDir, fileName).getCanonicalPath();
        if (fileCanonicalPath.startsWith(FileUtils.getFile(intendedDir).getCanonicalPath())) {
            return fileCanonicalPath;
        }
        throw new IOException("sanitizeFileName fileName is outside intendedDir");
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, String directoryPath, int uid, int gid, boolean isReadShare, boolean isWriteShare) {
        return unzip(zipFile, zipEntry, directoryPath, uid, gid, isReadShare, isWriteShare, (int) DEFAULT_MAX_SIZE);
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, String directoryPath, int uid, int gid, boolean isReadShare, boolean isWriteShare, int maxSize) {
        if (zipFile == null || zipEntry == null || directoryPath == null) {
            DSLog.e("unzip input parameters are null.", new Object[0]);
            return false;
        }
        try {
            return unzip(zipFile, zipEntry, FileUtils.getOutputFile(sanitizeFileName(zipEntry.getName(), directoryPath), uid, gid, isReadShare, isWriteShare), uid, gid, isReadShare, isWriteShare, maxSize);
        } catch (IOException e) {
            DSLog.e("unzip exception." + e.getMessage(), new Object[0]);
            return false;
        }
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, File outputFile, int uid, int gid, boolean isReadShare, boolean isWriteShare) {
        return unzip(zipFile, zipEntry, outputFile, uid, gid, isReadShare, isWriteShare, (int) DEFAULT_MAX_SIZE);
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, File outputFile, int uid, int gid, boolean isReadShare, boolean isWriteShare, int maxSize) {
        if (zipFile == null || zipEntry == null || outputFile == null) {
            DSLog.e("unzip input parameters are null.", new Object[0]);
            return false;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream inputStream = null;
        OutputStream outputStream = null;
        int total = 0;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            outputStream = FileUtils.openOutputStream(outputFile, uid, gid, isReadShare, isWriteShare);
            while (total <= maxSize) {
                int length = inputStream.read(buffer);
                if (length == -1) {
                    break;
                }
                outputStream.write(buffer, 0, length);
                total += length;
            }
            if (total > maxSize) {
                throw new IOException("outputFile is too large, maxSize: " + maxSize);
            }
            return true;
        } catch (IOException e) {
            DSLog.e("unzip failed: " + e.getMessage(), new Object[0]);
            return false;
        } finally {
            FileUtils.closeCloseable(outputStream);
            FileUtils.closeCloseable(inputStream);
        }
    }

    public static boolean zip(File srcFile, File dstFile) {
        if (srcFile == null || !srcFile.exists()) {
            DSLog.e("Failed to zip file, error: invalid src file.", new Object[0]);
            return false;
        } else if (dstFile == null || dstFile.exists()) {
            DSLog.e("Failed to zip file, error: invalid dst file.", new Object[0]);
            return false;
        } else {
            OutputStream outputStream = null;
            ZipOutputStream zipOut = null;
            try {
                outputStream = FileUtils.openOutputStream(dstFile);
                ZipOutputStream zipOut2 = new ZipOutputStream(outputStream);
                try {
                    compress(srcFile, zipOut2, "");
                    FileUtils.closeCloseable(zipOut2);
                    FileUtils.closeCloseable(outputStream);
                    return true;
                } catch (SecurityException e) {
                    e = e;
                    zipOut = zipOut2;
                    SecurityException securityException = e;
                    try {
                        DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                        FileUtils.closeCloseable(zipOut);
                        FileUtils.closeCloseable(outputStream);
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        FileUtils.closeCloseable(zipOut);
                        FileUtils.closeCloseable(outputStream);
                        throw th;
                    }
                } catch (IOException e2) {
                    e = e2;
                    zipOut = zipOut2;
                    IOException iOException = e;
                    DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                    FileUtils.closeCloseable(zipOut);
                    FileUtils.closeCloseable(outputStream);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    zipOut = zipOut2;
                    FileUtils.closeCloseable(zipOut);
                    FileUtils.closeCloseable(outputStream);
                    throw th;
                }
            } catch (SecurityException e3) {
                e = e3;
                SecurityException securityException2 = e;
                DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                FileUtils.closeCloseable(zipOut);
                FileUtils.closeCloseable(outputStream);
                return false;
            } catch (IOException e4) {
                e = e4;
                IOException iOException2 = e;
                DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                FileUtils.closeCloseable(zipOut);
                FileUtils.closeCloseable(outputStream);
                return false;
            }
        }
    }

    private static void compress(File srcFile, ZipOutputStream outputStream, String baseDir) throws IOException {
        if (srcFile.isDirectory()) {
            compressDirectory(srcFile, outputStream, baseDir);
        } else {
            compressFile(srcFile, outputStream, baseDir);
        }
    }

    private static void compressDirectory(File srcDir, ZipOutputStream outputStream, String parentDir) throws IOException {
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File compress : files) {
                compress(compress, outputStream, parentDir + srcDir.getName() + "/");
            }
        }
    }

    private static void compressFile(File srcFile, ZipOutputStream outputStream, String baseDir) throws IOException {
        if (srcFile.exists()) {
            BufferedInputStream inputStream = null;
            try {
                BufferedInputStream inputStream2 = new BufferedInputStream(new FileInputStream(srcFile));
                try {
                    outputStream.putNextEntry(new ZipEntry(baseDir + srcFile.getName()));
                    byte[] data = new byte[BUFFER_SIZE];
                    while (true) {
                        int count = inputStream2.read(data, 0, BUFFER_SIZE);
                        if (count != -1) {
                            outputStream.write(data, 0, count);
                        } else {
                            FileUtils.closeCloseable(inputStream2);
                            return;
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    inputStream = inputStream2;
                    FileUtils.closeCloseable(inputStream);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                FileUtils.closeCloseable(inputStream);
                throw th;
            }
        }
    }
}
