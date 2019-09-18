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
        this((InputStream) new ByteArrayInputStream(bytes));
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0023, code lost:
        r4 = new byte[8192];
        r5 = new java.io.ByteArrayOutputStream();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r6 = r0.read(r4);
        r7 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        if (r6 == -1) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        r5.write(r4, 0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0039, code lost:
        r6 = r5.toByteArray();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        $closeResource(null, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
        $closeResource(null, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0044, code lost:
        r6 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        r7 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0049, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        r9 = r7;
        r7 = r6;
        r6 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0073, code lost:
        throw new java.io.IOException("Entry " + r11 + " too large: " + r3.getSize());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0079, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0080, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0021, code lost:
        if (r3.getSize() > MAX_GET_ENTRY_CONTENTS_SIZE) goto L_0x0051;
     */
    private static byte[] getEntryContents(InputStream is, String entryName) throws IOException {
        ByteArrayOutputStream baos;
        Throwable th;
        Throwable th2;
        ZipInputStream zipInputStream = new ZipInputStream(is);
        while (true) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            ZipEntry entry = nextEntry;
            if (nextEntry != null) {
                if (entryName.equals(entry.getName())) {
                    break;
                }
            } else {
                $closeResource(null, zipInputStream);
                return null;
            }
        }
        $closeResource(th, baos);
        throw th2;
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004a, code lost:
        r6.getFD().sync();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        $closeResource(null, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0054, code lost:
        if (r13 == false) goto L_0x000d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        com.android.timezone.distro.FileUtils.makeWorldReadable(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005a, code lost:
        r7 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005f, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        r10 = r8;
        r8 = r7;
        r7 = r10;
     */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable) throws IOException {
        Throwable th;
        FileOutputStream fos;
        Throwable th2;
        Throwable th3;
        FileUtils.ensureDirectoriesExist(targetDir, makeWorldReadable);
        ZipInputStream zipInputStream = new ZipInputStream(is);
        try {
            byte[] buffer = new byte[8192];
            loop0:
            while (true) {
                ZipEntry nextEntry = zipInputStream.getNextEntry();
                ZipEntry entry = nextEntry;
                if (nextEntry != null) {
                    File entryFile = FileUtils.createSubFile(targetDir, entry.getName());
                    if (!entry.isDirectory()) {
                        if (!entryFile.getParentFile().exists()) {
                            FileUtils.ensureDirectoriesExist(entryFile.getParentFile(), makeWorldReadable);
                        }
                        fos = new FileOutputStream(entryFile);
                        while (true) {
                            int read = zipInputStream.read(buffer);
                            int count = read;
                            if (read == -1) {
                                break;
                            }
                            fos.write(buffer, 0, count);
                        }
                    } else {
                        FileUtils.ensureDirectoriesExist(entryFile, makeWorldReadable);
                    }
                } else {
                    $closeResource(null, zipInputStream);
                    return;
                }
            }
            $closeResource(th2, fos);
            throw th3;
        } catch (Throwable th4) {
            $closeResource(th, zipInputStream);
            throw th4;
        }
    }
}
