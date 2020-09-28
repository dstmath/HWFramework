package com.android.internal.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.StrictMode;
import com.android.internal.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
public final class ProcStatsUtil {
    private static final boolean DEBUG = false;
    private static final int READ_SIZE = 1024;
    private static final String TAG = "ProcStatsUtil";

    private ProcStatsUtil() {
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public static String readNullSeparatedFile(String path) {
        String contents = readSingleLineProcFile(path);
        if (contents == null) {
            return null;
        }
        int endIndex = contents.indexOf("\u0000\u0000");
        if (endIndex != -1) {
            contents = contents.substring(0, endIndex);
        }
        return contents.replace("\u0000", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public static String readSingleLineProcFile(String path) {
        return readTerminatedProcFile(path, (byte) 10);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0065, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x006a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x006b, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x006e, code lost:
        throw r3;
     */
    public static String readTerminatedProcFile(String path, byte terminator) {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            FileInputStream is = new FileInputStream(path);
            ByteArrayOutputStream byteStream = null;
            byte[] buffer = new byte[1024];
            while (true) {
                int len = is.read(buffer);
                if (len <= 0) {
                    break;
                }
                int terminatingIndex = -1;
                int i = 0;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (buffer[i] == terminator) {
                        terminatingIndex = i;
                        break;
                    } else {
                        i++;
                    }
                }
                boolean foundTerminator = terminatingIndex != -1;
                if (!foundTerminator || byteStream != null) {
                    if (byteStream == null) {
                        byteStream = new ByteArrayOutputStream(1024);
                    }
                    byteStream.write(buffer, 0, foundTerminator ? terminatingIndex : len);
                    if (foundTerminator) {
                        break;
                    }
                } else {
                    String str = new String(buffer, 0, terminatingIndex);
                    is.close();
                    StrictMode.setThreadPolicy(savedPolicy);
                    return str;
                }
            }
            if (byteStream == null) {
                is.close();
                return "";
            }
            String byteArrayOutputStream = byteStream.toString();
            is.close();
            StrictMode.setThreadPolicy(savedPolicy);
            return byteArrayOutputStream;
        } catch (IOException e) {
            return null;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }
}
