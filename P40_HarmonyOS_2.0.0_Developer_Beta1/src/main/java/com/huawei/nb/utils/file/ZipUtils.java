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

    public static String sanitizeFileName(String str, String str2) throws IOException {
        String canonicalPath = FileUtils.getFile(str2, str).getCanonicalPath();
        if (canonicalPath.startsWith(FileUtils.getFile(str2).getCanonicalPath())) {
            return canonicalPath;
        }
        throw new IOException("sanitizeFileName fileName is outside intendedDir");
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, String str, int i, int i2, boolean z, boolean z2) {
        return unzip(zipFile, zipEntry, str, i, i2, z, z2, (int) DEFAULT_MAX_SIZE);
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, String str, int i, int i2, boolean z, boolean z2, int i3) {
        if (zipFile == null || zipEntry == null || str == null) {
            DSLog.e("unzip input parameters are null.", new Object[0]);
            return false;
        }
        try {
            return unzip(zipFile, zipEntry, FileUtils.getOutputFile(sanitizeFileName(zipEntry.getName(), str), i, i2, z, z2), i, i2, z, z2, i3);
        } catch (IOException e) {
            DSLog.e("unzip exception." + e.getMessage(), new Object[0]);
            return false;
        }
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, File file, int i, int i2, boolean z, boolean z2) {
        return unzip(zipFile, zipEntry, file, i, i2, z, z2, (int) DEFAULT_MAX_SIZE);
    }

    public static boolean unzip(ZipFile zipFile, ZipEntry zipEntry, File file, int i, int i2, boolean z, boolean z2, int i3) {
        Throwable th;
        InputStream inputStream;
        IOException e;
        if (zipFile == null || zipEntry == null || file == null) {
            DSLog.e("unzip input parameters are null.", new Object[0]);
            return false;
        }
        byte[] bArr = new byte[BUFFER_SIZE];
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            try {
                OutputStream openOutputStream = FileUtils.openOutputStream(file, i, i2, z, z2);
                int i4 = 0;
                while (i4 <= i3) {
                    int read = inputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    openOutputStream.write(bArr, 0, read);
                    i4 += read;
                }
                if (i4 <= i3) {
                    FileUtils.closeCloseable(openOutputStream);
                    FileUtils.closeCloseable(inputStream);
                    return true;
                }
                throw new IOException("outputFile is too large, maxSize: " + i3);
            } catch (IOException e2) {
                e = e2;
                try {
                    DSLog.e("unzip failed: " + e.getMessage(), new Object[0]);
                    FileUtils.closeCloseable(null);
                    FileUtils.closeCloseable(inputStream);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtils.closeCloseable(null);
                    FileUtils.closeCloseable(inputStream);
                    throw th;
                }
            }
        } catch (IOException e3) {
            e = e3;
            inputStream = null;
            DSLog.e("unzip failed: " + e.getMessage(), new Object[0]);
            FileUtils.closeCloseable(null);
            FileUtils.closeCloseable(inputStream);
            return false;
        } catch (Throwable th3) {
            th = th3;
            inputStream = null;
            FileUtils.closeCloseable(null);
            FileUtils.closeCloseable(inputStream);
            throw th;
        }
    }

    public static boolean zip(File file, File file2) {
        OutputStream outputStream;
        Throwable th;
        if (file == null || !file.exists()) {
            DSLog.e("Failed to zip file, error: invalid src file.", new Object[0]);
            return false;
        } else if (file2 == null || file2.exists()) {
            DSLog.e("Failed to zip file, error: invalid dst file.", new Object[0]);
            return false;
        } else {
            ZipOutputStream zipOutputStream = null;
            try {
                outputStream = FileUtils.openOutputStream(file2);
                try {
                    ZipOutputStream zipOutputStream2 = new ZipOutputStream(outputStream);
                    try {
                        compress(file, zipOutputStream2, "");
                        FileUtils.closeCloseable(zipOutputStream2);
                        FileUtils.closeCloseable(outputStream);
                        return true;
                    } catch (IOException | SecurityException unused) {
                        zipOutputStream = zipOutputStream2;
                        try {
                            DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                            FileUtils.closeCloseable(zipOutputStream);
                            FileUtils.closeCloseable(outputStream);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            FileUtils.closeCloseable(zipOutputStream);
                            FileUtils.closeCloseable(outputStream);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        zipOutputStream = zipOutputStream2;
                        FileUtils.closeCloseable(zipOutputStream);
                        FileUtils.closeCloseable(outputStream);
                        throw th;
                    }
                } catch (IOException | SecurityException unused2) {
                    DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                    FileUtils.closeCloseable(zipOutputStream);
                    FileUtils.closeCloseable(outputStream);
                    return false;
                }
            } catch (IOException | SecurityException unused3) {
                outputStream = null;
                DSLog.e("Failed to zip file, error: input file is invalid.", new Object[0]);
                FileUtils.closeCloseable(zipOutputStream);
                FileUtils.closeCloseable(outputStream);
                return false;
            } catch (Throwable th4) {
                th = th4;
                outputStream = null;
                FileUtils.closeCloseable(zipOutputStream);
                FileUtils.closeCloseable(outputStream);
                throw th;
            }
        }
    }

    private static void compress(File file, ZipOutputStream zipOutputStream, String str) throws IOException {
        if (file.isDirectory()) {
            compressDirectory(file, zipOutputStream, str);
        } else {
            compressFile(file, zipOutputStream, str);
        }
    }

    private static void compressDirectory(File file, ZipOutputStream zipOutputStream, String str) throws IOException {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                compress(file2, zipOutputStream, str + file.getName() + "/");
            }
        }
    }

    private static void compressFile(File file, ZipOutputStream zipOutputStream, String str) throws IOException {
        Throwable th;
        BufferedInputStream bufferedInputStream;
        if (file.exists()) {
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(str + file.getName()));
                    byte[] bArr = new byte[BUFFER_SIZE];
                    while (true) {
                        int read = bufferedInputStream.read(bArr, 0, BUFFER_SIZE);
                        if (read != -1) {
                            zipOutputStream.write(bArr, 0, read);
                        } else {
                            FileUtils.closeCloseable(bufferedInputStream);
                            return;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    FileUtils.closeCloseable(bufferedInputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = null;
                FileUtils.closeCloseable(bufferedInputStream);
                throw th;
            }
        }
    }
}
