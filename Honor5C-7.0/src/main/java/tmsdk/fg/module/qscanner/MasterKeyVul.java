package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class MasterKeyVul {
    public static final String TAG = "MasterKeyVul";

    private static void a(File file, byte[] bArr) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(bArr);
        } finally {
            fileOutputStream.close();
        }
    }

    private static byte[] a(InputStream inputStream, int i) throws IOException {
        byte[] bArr = new byte[i];
        int i2 = 0;
        while (true) {
            int read = inputStream.read(bArr, i2, i - i2);
            if (read != -1) {
                i2 += read;
                if (i2 >= i) {
                    break;
                }
            } else {
                break;
            }
        }
        return bArr;
    }

    private static void c(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        try {
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ((ZipEntry) entries.nextElement()).getName();
            }
        } finally {
            zipFile.close();
        }
    }

    private static boolean d(File file) {
        try {
            c(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean e(File file) throws IOException {
        Exception e;
        Throwable th;
        ZipFile zipFile = new ZipFile(file);
        Enumeration entries = zipFile.entries();
        boolean z = false;
        while (entries.hasMoreElements()) {
            InputStream inputStream;
            try {
                inputStream = zipFile.getInputStream((ZipEntry) entries.nextElement());
                try {
                    byte[] bArr = new byte[10];
                    String str = new String(bArr, 0, inputStream.read(bArr));
                    z = !"J".equals(str) ? !"C".equals(str) ? false : false : true;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e2) {
                    e = e2;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        z = z;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
            } catch (Exception e3) {
                e = e3;
                inputStream = null;
                e.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                z = z;
            } catch (Throwable th3) {
                th = th3;
                inputStream = null;
            }
            z = z;
        }
        zipFile.close();
        return z;
    }

    public static String file2Base64String(File file) {
        InputStream fileInputStream;
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        String str = null;
        try {
            fileInputStream = new FileInputStream(file);
            try {
                str = b.encodeToString(a(fileInputStream, (int) file.length()), 0);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e4) {
                e2 = e4;
                try {
                    e2.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e32 = e6;
                e32.printStackTrace();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                return str;
            }
        } catch (FileNotFoundException e7) {
            e2 = e7;
            fileInputStream = str;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return str;
        } catch (IOException e8) {
            e322 = e8;
            fileInputStream = str;
            e322.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return str;
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = str;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        return str;
    }

    public static boolean scan(Context context) {
        boolean d;
        boolean z = false;
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, "8219321.zip");
        try {
            a(file, b.decode("UEsDBAoAAAgIAHqYJUOLntnTAwAAAAEAAAABABQAYQEAEAABAAAAAAAAAAMAAAAAAAAAcwQAUEsDBAoAAAgIAHqYJUMxz9BKAwAAAAEAAAABABQAYQEAEAABAAAAAAAAAAMAAAAAAAAAcwIAUEsBAhQACgAACAgAepglQ4ue2dMDAAAAAQAAAAEAAAAAAAAAAAAAAAAAAAAAAGFQSwECFAAKAAAICAB6mCVDMc/QSgMAAAABAAAAAQAAAAAAAAAAAAAAAAA2AAAAYVBLBQYAAAAAAgACAF4AAABsAAAAAAA=", 0));
            d = d(file);
        } catch (Throwable e) {
            d.a(TAG, "fail to write zip file for test", e);
            d = z;
        }
        file.delete();
        if (!d) {
            file = new File(cacheDir, "9695860.zip");
            try {
                a(file, b.decode("UEsDBAoAAAgIAImbJUOLntnTBgAAAAEAAAABAAAAYQEBAP7/QVBLAQIUAAoAAAgIAImbJUOLntnTBgAAAAEAAAABAAAA//8AAAAAAAAAAAAAAABhUEsFBgAAAAABAAEALwAAACUAAAAAAA==", 0));
                d = d(file);
            } catch (Throwable e2) {
                d.a(TAG, "fail to write zip file for test", e2);
                d = z;
            }
            file.delete();
        }
        if (d) {
            return d;
        }
        file = new File(cacheDir, "9950697.zip");
        try {
            a(file, b.decode("UEsDBAoAAAgAAFGQZUMDRwtEAQAAAAEAAAACABQAYQEAEAABAAAAAAAAAAEAAAAAAAAASkNQSwECFAAKAAAIAABRkGVDA0cLRAEAAAABAAAAAQAAAAAAAAAAAAAAAAAAAAAAYVBLBQYAAAAAAQABAC8AAAA1AAAAAAA=", 0));
            z = e(file);
        } catch (Throwable e22) {
            d.a(TAG, "fail to write zip file for test", e22);
        }
        file.delete();
        return z;
    }
}
