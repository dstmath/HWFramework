package com.android.timezone.distro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class TimeZoneDistro {
    private static final int BUFFER_SIZE = 8192;
    public static final String DISTRO_VERSION_FILE_NAME = "distro_version";
    public static final String FILE_NAME = "distro.zip";
    public static final String ICU_DATA_FILE_NAME = "icu/icu_tzdata.dat";
    private static final long MAX_GET_ENTRY_CONTENTS_SIZE = 131072;
    public static final String TZDATA_FILE_NAME = "tzdata";
    public static final String TZLOOKUP_FILE_NAME = "tzlookup.xml";
    private final InputStream inputStream;

    public TimeZoneDistro(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    public TimeZoneDistro(InputStream inputStream2) {
        this.inputStream = inputStream2;
    }

    public DistroVersion getDistroVersion() throws DistroException, IOException {
        byte[] contents = getEntryContents(this.inputStream, DISTRO_VERSION_FILE_NAME);
        if (contents != null) {
            return DistroVersion.fromBytes(contents);
        }
        throw new DistroException("Distro version file entry not found");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
        $closeResource(r3, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004a, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0075, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0076, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0079, code lost:
        throw r2;
     */
    private static byte[] getEntryContents(InputStream is, String entryName) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(is);
        while (true) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                $closeResource(null, zipInputStream);
                return null;
            } else if (entryName.equals(entry.getName())) {
                if (entry.getSize() <= MAX_GET_ENTRY_CONTENTS_SIZE) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (true) {
                        int count = zipInputStream.read(buffer);
                        if (count != -1) {
                            baos.write(buffer, 0, count);
                        } else {
                            byte[] byteArray = baos.toByteArray();
                            $closeResource(null, baos);
                            $closeResource(null, zipInputStream);
                            return byteArray;
                        }
                    }
                } else {
                    throw new IOException("Entry " + entryName + " too large: " + entry.getSize());
                }
            }
        }
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

    public void extractTo(File targetDir) throws IOException {
        extractZipSafely(this.inputStream, targetDir, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005c, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005d, code lost:
        $closeResource(r4, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0067, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0068, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006b, code lost:
        throw r2;
     */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable) throws IOException {
        FileUtils.ensureDirectoriesExist(targetDir, makeWorldReadable);
        ZipInputStream zipInputStream = new ZipInputStream(is);
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry != null) {
                File entryFile = FileUtils.createSubFile(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    FileUtils.ensureDirectoriesExist(entryFile, makeWorldReadable);
                } else {
                    if (!entryFile.getParentFile().exists()) {
                        FileUtils.ensureDirectoriesExist(entryFile.getParentFile(), makeWorldReadable);
                    }
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    while (true) {
                        int count = zipInputStream.read(buffer);
                        if (count != -1) {
                            fos.write(buffer, 0, count);
                        } else {
                            fos.getFD().sync();
                            $closeResource(null, fos);
                            if (makeWorldReadable) {
                                FileUtils.makeWorldReadable(entryFile);
                            }
                        }
                    }
                }
            } else {
                $closeResource(null, zipInputStream);
                return;
            }
        }
    }
}
