package tmsdk.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* compiled from: Unknown */
public final class q {
    private byte[] Lr;

    public q() {
        this.Lr = new byte[16384];
    }

    private void a(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            do {
            } while (-1 != inputStream.read(this.Lr));
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void dr(String str) throws IOException {
        Throwable th;
        ZipFile zipFile;
        try {
            long length = new File(str).length();
            zipFile = new ZipFile(str);
            try {
                Enumeration entries = zipFile.entries();
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
                                            a(zipFile, zipEntry);
                                            if (zipFile != null) {
                                                zipFile.close();
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        d.c("ZipChecker", " fileName :: " + str);
                        throw new RuntimeException("Invalid entry size!");
                    }
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Throwable th3) {
            th = th3;
            zipFile = null;
            if (zipFile != null) {
                zipFile.close();
            }
            throw th;
        }
    }

    public synchronized boolean dq(String str) {
        try {
            dr(str);
        } catch (Throwable e) {
            d.a("ZipChecker", "check", e);
            return false;
        }
        return true;
    }
}
