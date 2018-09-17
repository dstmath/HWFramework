package tmsdk.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class v {
    private byte[] Mh = new byte[16384];

    private void a(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            do {
            } while (-1 != inputStream.read(this.Mh));
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ba  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void cP(String str) throws IOException {
        Throwable th;
        ZipFile zipFile = null;
        try {
            long length = new File(str).length();
            ZipFile zipFile2 = new ZipFile(str);
            try {
                Enumeration entries = zipFile2.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (!zipEntry.isDirectory()) {
                        long compressedSize = zipEntry.getCompressedSize();
                        long size = zipEntry.getSize();
                        if ((compressedSize < -1 ? 1 : null) == null) {
                            if ((compressedSize > length ? 1 : null) == null) {
                                if ((size < -1 ? 1 : null) == null) {
                                    if ((size <= 2500 * compressedSize ? 1 : null) != null) {
                                        if (zipEntry.getName() != null && zipEntry.getName().contains("AndroidManifest.xml")) {
                                            if (compressedSize == 0 || size == 0) {
                                                throw new RuntimeException("Invalid AndroidManifest!");
                                            }
                                            try {
                                                a(zipFile2, zipEntry);
                                                if (zipFile2 != null) {
                                                    zipFile2.close();
                                                }
                                                return;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                zipFile = zipFile2;
                                                if (zipFile != null) {
                                                }
                                                throw th;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        f.e("ZipChecker", " fileName :: " + str);
                        throw new RuntimeException("Invalid entry size!");
                    }
                }
                if (zipFile2 != null) {
                    zipFile2.close();
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (zipFile != null) {
            }
            throw th;
        }
    }

    public synchronized boolean cO(String str) {
        try {
            cP(str);
        } catch (Throwable e) {
            f.b("ZipChecker", "check", e);
            return false;
        }
        return true;
    }
}
