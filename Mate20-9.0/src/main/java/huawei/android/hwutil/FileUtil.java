package huawei.android.hwutil;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
    static final String TAG = "FileUtil";

    public static boolean isFileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0038, code lost:
        android.util.Log.w(TAG, "isFileContentAllZero IOException ");
     */
    public static boolean isFileContentAllZero(String fileName) {
        if (fileName != null) {
            boolean isNotValid = false;
            FileInputStream fis = null;
            try {
                FileInputStream fis2 = new FileInputStream(fileName);
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = fis2.read(buffer);
                    int realLength = read;
                    if (read == -1) {
                        break;
                    }
                    int j = 0;
                    while (true) {
                        if (j < realLength) {
                            if (buffer[j] != 0) {
                                try {
                                    fis2.close();
                                } catch (IOException e) {
                                    Log.w(TAG, "isFileContentAllZero IOException ");
                                }
                                return false;
                            }
                            j++;
                        }
                    }
                }
            } catch (RuntimeException e2) {
                Log.w(TAG, "isFileContentAllZero RuntimeException ");
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e3) {
                Log.w(TAG, "isFileContentAllZero Exception ");
                if (fis != null) {
                    fis.close();
                }
            } catch (Throwable th) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e4) {
                        Log.w(TAG, "isFileContentAllZero IOException ");
                    }
                }
                throw th;
            }
            if (isNotValid) {
                Log.w(TAG, "file content is all zero");
                return true;
            }
        }
        return false;
    }
}
