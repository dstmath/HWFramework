package libcore.tzdata.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ConfigBundle {
    private static final int BUFFER_SIZE = 8192;
    public static final String CHECKSUMS_FILE_NAME = "checksums";
    public static final String ICU_DATA_FILE_NAME = "icu/icu_tzdata.dat";
    public static final String TZ_DATA_VERSION_FILE_NAME = "tzdata_version";
    public static final String ZONEINFO_FILE_NAME = "tzdata";
    private final byte[] bytes;

    public ConfigBundle(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBundleBytes() {
        return this.bytes;
    }

    public void extractTo(File targetDir) throws IOException {
        extractZipSafely(new ByteArrayInputStream(this.bytes), targetDir, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable) throws IOException {
        Throwable th;
        Throwable th2;
        FileUtils.ensureDirectoriesExist(targetDir, makeWorldReadable);
        Throwable th3 = null;
        ZipInputStream zipInputStream = null;
        try {
            ZipInputStream zipInputStream2 = new ZipInputStream(is);
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (true) {
                    ZipEntry entry = zipInputStream2.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    File entryFile = FileUtils.createSubFile(targetDir, entry.getName());
                    if (entry.isDirectory()) {
                        FileUtils.ensureDirectoriesExist(entryFile, makeWorldReadable);
                    } else {
                        if (!entryFile.getParentFile().exists()) {
                            FileUtils.ensureDirectoriesExist(entryFile.getParentFile(), makeWorldReadable);
                        }
                        th = null;
                        FileOutputStream fileOutputStream = null;
                        try {
                            FileOutputStream fos = new FileOutputStream(entryFile);
                            while (true) {
                                try {
                                    int count = zipInputStream2.read(buffer);
                                    if (count == -1) {
                                        break;
                                    }
                                    fos.write(buffer, 0, count);
                                } catch (Throwable th4) {
                                    th2 = th4;
                                    fileOutputStream = fos;
                                }
                            }
                            fos.getFD().sync();
                            if (fos != null) {
                                fos.close();
                            }
                            if (th != null) {
                                break;
                            } else if (makeWorldReadable) {
                                FileUtils.makeWorldReadable(entryFile);
                            }
                        } catch (Throwable th5) {
                            th2 = th5;
                        }
                    }
                }
                throw th;
            } catch (Throwable th6) {
                th2 = th6;
                th = null;
                zipInputStream = zipInputStream2;
            }
        } catch (Throwable th7) {
            th2 = th7;
            th = null;
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Throwable th32) {
                    if (th == null) {
                        th = th32;
                    } else if (th != th32) {
                        th.addSuppressed(th32);
                    }
                }
            }
            if (th != null) {
                throw th;
            }
            throw th2;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(this.bytes, ((ConfigBundle) o).bytes);
    }
}
