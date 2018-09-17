package tmsdkobf;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.f;
import tmsdk.common.utils.l;
import tmsdk.common.utils.l.a;

public final class lu {
    private static final String[][] yV;

    static {
        String[][] strArr = new String[69][];
        strArr[0] = new String[]{"3gp", "video/3gpp"};
        strArr[1] = new String[]{"apk", "application/vnd.android.package-archive"};
        strArr[2] = new String[]{"asf", "video/x-ms-asf"};
        strArr[3] = new String[]{"avi", "video/x-msvideo"};
        strArr[4] = new String[]{"bin", "application/octet-stream"};
        strArr[5] = new String[]{"bmp", "image/bmp"};
        strArr[6] = new String[]{"c", "text/plain"};
        strArr[7] = new String[]{"class", "application/octet-stream"};
        strArr[8] = new String[]{"conf", "text/plain"};
        strArr[9] = new String[]{"cpp", "text/plain"};
        strArr[10] = new String[]{"doc", "application/msword"};
        strArr[11] = new String[]{"docx", "application/msword"};
        strArr[12] = new String[]{"exe", "application/octet-stream"};
        strArr[13] = new String[]{"gif", "image/gif"};
        strArr[14] = new String[]{"gtar", "application/x-gtar"};
        strArr[15] = new String[]{"gz", "application/x-gzip"};
        strArr[16] = new String[]{"h", "text/plain"};
        strArr[17] = new String[]{"htm", "text/html"};
        strArr[18] = new String[]{"html", "text/html"};
        strArr[19] = new String[]{"jar", "application/java-archive"};
        strArr[20] = new String[]{"java", "text/plain"};
        strArr[21] = new String[]{"jpeg", "image/jpeg"};
        strArr[22] = new String[]{"jpg", "image/jpeg"};
        strArr[23] = new String[]{"js", "application/x-javascript"};
        strArr[24] = new String[]{"log", "text/plain"};
        strArr[25] = new String[]{"m3u", "audio/x-mpegurl"};
        strArr[26] = new String[]{"m4a", "audio/mp4a-latm"};
        strArr[27] = new String[]{"m4b", "audio/mp4a-latm"};
        strArr[28] = new String[]{"m4p", "audio/mp4a-latm"};
        strArr[29] = new String[]{"m4u", "video/vnd.mpegurl"};
        strArr[30] = new String[]{"m4v", "video/x-m4v"};
        strArr[31] = new String[]{"mov", "video/quicktime"};
        strArr[32] = new String[]{"mp2", "audio/x-mpeg"};
        strArr[33] = new String[]{"mp3", "audio/x-mpeg"};
        strArr[34] = new String[]{"mp4", "video/mp4"};
        strArr[35] = new String[]{"mpc", "application/vnd.mpohn.certificate"};
        strArr[36] = new String[]{"mpe", "video/mpeg"};
        strArr[37] = new String[]{"mpeg", "video/mpeg"};
        strArr[38] = new String[]{"mpg", "video/mpeg"};
        strArr[39] = new String[]{"mpg4", "video/mp4"};
        strArr[40] = new String[]{"mpga", "audio/mpeg"};
        strArr[41] = new String[]{"msg", "application/vnd.ms-outlook"};
        strArr[42] = new String[]{"ogg", "audio/ogg"};
        strArr[43] = new String[]{"pdf", "application/pdf"};
        strArr[44] = new String[]{"png", "image/png"};
        strArr[45] = new String[]{"pps", "application/vnd.ms-powerpoint"};
        strArr[46] = new String[]{"ppsx", "application/vnd.ms-powerpoint"};
        strArr[47] = new String[]{"ppt", "application/vnd.ms-powerpoint"};
        strArr[48] = new String[]{"pptx", "application/vnd.ms-powerpoint"};
        strArr[49] = new String[]{"xls", "application/vnd.ms-excel"};
        strArr[50] = new String[]{"xlsx", "application/vnd.ms-excel"};
        strArr[51] = new String[]{"prop", "text/plain"};
        strArr[52] = new String[]{"rar", "application/x-rar-compressed"};
        strArr[53] = new String[]{"rc", "text/plain"};
        strArr[54] = new String[]{"rmvb", "audio/x-pn-realaudio"};
        strArr[55] = new String[]{"rtf", "application/rtf"};
        strArr[56] = new String[]{"sh", "text/plain"};
        strArr[57] = new String[]{"tar", "application/x-tar"};
        strArr[58] = new String[]{"tgz", "application/x-compressed"};
        strArr[59] = new String[]{"txt", "text/plain"};
        strArr[60] = new String[]{"wav", "audio/x-wav"};
        strArr[61] = new String[]{"wma", "audio/x-ms-wma"};
        strArr[62] = new String[]{"wmv", "audio/x-ms-wmv"};
        strArr[63] = new String[]{"wps", "application/vnd.ms-works"};
        strArr[64] = new String[]{"xml", "text/plain"};
        strArr[65] = new String[]{"z", "application/x-compress"};
        strArr[66] = new String[]{"zip", "application/zip"};
        strArr[67] = new String[]{"epub", "application/epub+zip"};
        strArr[68] = new String[]{"", "*/*"};
        yV = strArr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x0108 A:{SYNTHETIC, Splitter: B:54:0x0108} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00fe A:{SYNTHETIC, Splitter: B:48:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010e A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0108 A:{SYNTHETIC, Splitter: B:54:0x0108} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00fe A:{SYNTHETIC, Splitter: B:48:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010e A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean a(Context -l_3_R, String str, boolean z) {
        Exception e;
        boolean z2;
        Throwable th;
        if (z) {
            return true;
        }
        Context currentContext = TMSDKContext.getCurrentContext();
        if (currentContext != null) {
            -l_3_R = currentContext;
        }
        InputStream inputStream = null;
        int i = 0;
        int i2 = 0;
        try {
            inputStream = -l_3_R.getAssets().open(str);
            byte[] bArr = new byte[28];
            inputStream.read(bArr);
            i = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            i2 = (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16)) | ((bArr[27] & 255) << 24);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        }
        int i3 = 0;
        int i4 = 0;
        try {
            InputStream fileInputStream = new FileInputStream(-l_3_R.getFilesDir().toString() + File.separator + str);
            try {
                byte[] bArr2 = new byte[28];
                try {
                    fileInputStream.read(bArr2);
                    i3 = (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16)) | ((bArr2[7] & 255) << 24);
                    i4 = (((bArr2[24] & 255) | ((bArr2[25] & 255) << 8)) | ((bArr2[26] & 255) << 16)) | ((bArr2[27] & 255) << 24);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    inputStream = fileInputStream;
                } catch (Exception e7) {
                    e = e7;
                    inputStream = fileInputStream;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                        }
                        if (i != i3) {
                        }
                        return z2;
                    } catch (Throwable th3) {
                        th = th3;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e8) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    inputStream = fileInputStream;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e9) {
                e = e9;
                inputStream = fileInputStream;
                e.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                    }
                }
                if (i != i3) {
                    return z2;
                }
                return z2;
            } catch (Throwable th5) {
                th = th5;
                inputStream = fileInputStream;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (Exception e11) {
            e = e11;
            e.printStackTrace();
            if (inputStream != null) {
            }
            if (i != i3) {
            }
            return z2;
        }
        z2 = i != i3 || i2 > i4;
        return z2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x0216 A:{SYNTHETIC, Splitter: B:111:0x0216} */
    /* JADX WARNING: Missing block: B:44:0x00d1, code:
            if (r26.equals("") == false) goto L_0x0017;
     */
    /* JADX WARNING: Missing block: B:49:0x00e7, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.VIRUS_BASE_EN_NAME) == false) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:79:0x019c, code:
            if (a(r24, r25, r3) == false) goto L_0x019e;
     */
    /* JADX WARNING: Missing block: B:81:0x01a8, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.LOCATION_NAME) != false) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:82:0x01aa, code:
            if (r18 != false) goto L_0x01c5;
     */
    /* JADX WARNING: Missing block: B:83:0x01ac, code:
            if (r18 == false) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:85:0x01ba, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.VIRUS_BASE_EN_NAME) == false) goto L_0x019e;
     */
    /* JADX WARNING: Missing block: B:87:0x01c1, code:
            if (r(r24) != false) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:89:0x01cf, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.VIRUS_BASE_NAME) == false) goto L_0x01d1;
     */
    /* JADX WARNING: Missing block: B:91:0x01db, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.VIRUS_BASE_EN_NAME) == false) goto L_0x01dd;
     */
    /* JADX WARNING: Missing block: B:93:0x01e7, code:
            if (r25.equals(tmsdk.common.module.update.UpdateConfig.LOCATION_NAME) == false) goto L_0x01e9;
     */
    /* JADX WARNING: Missing block: B:95:0x01ed, code:
            if (e(r24, r25) != false) goto L_0x0072;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized String b(Context -l_3_R, String str, String str2) {
        String str3;
        InputStream inputStream;
        FileOutputStream fileOutputStream;
        Throwable th;
        synchronized (lu.class) {
            Context currentContext = TMSDKContext.getCurrentContext();
            if (currentContext != null) {
                -l_3_R = currentContext;
            }
            if (-l_3_R == null) {
                try {
                    throw new Exception("TMSDKContext is null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (str2 != null) {
            }
            str2 = -l_3_R.getFilesDir().toString();
            File file = new File(str2);
            if (!(file.exists() && file.isDirectory())) {
                file.mkdirs();
            }
            str3 = str2 + File.separator + str;
            inputStream = null;
            fileOutputStream = null;
            try {
                File file2 = new File(str3);
                boolean exists = file2.exists();
                boolean isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                boolean z = false;
                if (!str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                }
                if (eE()) {
                    z = true;
                }
                if (!exists && isUpdatableAssetFile) {
                    String str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                    if (str4 != null) {
                        String str5 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                        File file3 = new File(str5);
                        File file4 = new File(str5 + UpdateConfig.PATCH_SUFIX);
                        if (file3.exists()) {
                            file3.delete();
                        }
                        if (file4.exists()) {
                            file4.delete();
                        }
                    }
                }
                if (exists) {
                    if (!str.equals("MToken.zip")) {
                        if (!str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                        }
                    }
                }
                d(file2);
                inputStream = -l_3_R.getResources().getAssets().open(str, 1);
                FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                try {
                    byte[] bArr = new byte[8192];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read <= 0) {
                            fileOutputStream2.getChannel().force(true);
                            fileOutputStream2.flush();
                            fileOutputStream = fileOutputStream2;
                            break;
                        }
                        fileOutputStream2.write(bArr, 0, read);
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (IOException e4) {
                    fileOutputStream = fileOutputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    fileOutputStream = fileOutputStream2;
                }
            } catch (IOException e5) {
            }
        }
        String str6;
        try {
            f.e("getCommonFilePath", "getCommonFilePath error");
            str6 = "";
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e7) {
                }
            }
            return str6;
        } catch (Throwable th3) {
            th = th3;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e9) {
                }
            }
            throw th;
        }
        return str3;
        if (fileOutputStream != null) {
        }
        return str6;
        return str6;
    }

    public static boolean bK(String str) {
        File file = new File(str);
        if (file.exists()) {
            return !file.isFile() ? bL(str) : deleteFile(str);
        } else {
            return false;
        }
    }

    public static boolean bL(String str) {
        if (!str.endsWith(File.separator)) {
            str = str + File.separator;
        }
        File file = new File(str);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        boolean z = true;
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            if (!listFiles[i].isFile()) {
                z = bL(listFiles[i].getAbsolutePath());
                if (!z) {
                    break;
                }
            } else {
                z = deleteFile(listFiles[i].getAbsolutePath());
                if (!z) {
                    break;
                }
            }
        }
        return z && file.delete();
    }

    public static boolean bM(String str) {
        return new File(str).exists();
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0055 A:{SYNTHETIC, Splitter: B:36:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005f A:{SYNTHETIC, Splitter: B:41:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x007a A:{SYNTHETIC, Splitter: B:55:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0071 A:{SYNTHETIC, Splitter: B:51:0x0071} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x008a A:{SYNTHETIC, Splitter: B:64:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0094 A:{SYNTHETIC, Splitter: B:69:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0055 A:{SYNTHETIC, Splitter: B:36:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005f A:{SYNTHETIC, Splitter: B:41:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x007a A:{SYNTHETIC, Splitter: B:55:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0071 A:{SYNTHETIC, Splitter: B:51:0x0071} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x008a A:{SYNTHETIC, Splitter: B:64:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0094 A:{SYNTHETIC, Splitter: B:69:0x0094} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String bN(String str) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream2;
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(str));
            try {
                byteArrayOutputStream2 = new ByteArrayOutputStream();
            } catch (FileNotFoundException e3) {
                e = e3;
                bufferedInputStream = bufferedInputStream2;
                try {
                    e.printStackTrace();
                    if (byteArrayOutputStream != null) {
                    }
                    if (bufferedInputStream != null) {
                    }
                    return "";
                } catch (Throwable th2) {
                    th = th2;
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e2 = e5;
                bufferedInputStream = bufferedInputStream2;
                e2.printStackTrace();
                if (byteArrayOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                return "";
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = bufferedInputStream2;
                if (byteArrayOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                while (true) {
                    int read = bufferedInputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream2.write(bArr, 0, read);
                }
                String str2 = new String(byteArrayOutputStream2.toByteArray());
                if (byteArrayOutputStream2 == null) {
                    byteArrayOutputStream = byteArrayOutputStream2;
                } else {
                    try {
                        byteArrayOutputStream2.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (bufferedInputStream2 == null) {
                    bufferedInputStream = bufferedInputStream2;
                } else {
                    try {
                        bufferedInputStream2.close();
                    } catch (IOException e62) {
                        e62.printStackTrace();
                    }
                }
                return str2;
            } catch (FileNotFoundException e7) {
                e = e7;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                e.printStackTrace();
                if (byteArrayOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                return "";
            } catch (IOException e8) {
                e2 = e8;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                e2.printStackTrace();
                if (byteArrayOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                return "";
            } catch (Throwable th4) {
                th = th4;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                if (byteArrayOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return "";
        } catch (IOException e10) {
            e222 = e10;
            e222.printStackTrace();
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
            return "";
        }
    }

    public static boolean cL() {
        String str = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File file = new File(str);
        if (!file.isDirectory() && !file.mkdirs()) {
            return false;
        }
        File file2 = new File(str, ".probe");
        try {
            if (file2.exists()) {
                file2.delete();
            }
            if (!file2.createNewFile()) {
                return false;
            }
            file2.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void d(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean deleteFile(String str) {
        try {
            File file = new File(str);
            return file.isFile() ? file.delete() : false;
        } catch (Exception e) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0097 A:{SYNTHETIC, Splitter: B:54:0x0097} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00a4 A:{SYNTHETIC, Splitter: B:61:0x00a4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean e(Context -l_7_R, String str) {
        Exception e;
        Throwable th;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + str);
        if (!file.exists()) {
            return true;
        }
        InputStream inputStream = null;
        int i = 0;
        try {
            Context currentContext = TMSDKContext.getCurrentContext();
            if (currentContext != null) {
                -l_7_R = currentContext;
            }
            inputStream = -l_7_R.getAssets().open(str, 1);
            i = lt.c(inputStream).yT;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        } catch (Throwable th2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
        if (i == 0) {
            return false;
        }
        FileInputStream fileInputStream = null;
        int i2 = 0;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            InputStream inputStream2;
            try {
                i2 = lt.c(fileInputStream2).yT;
                if (fileInputStream2 == null) {
                    inputStream2 = fileInputStream2;
                    return i <= i2;
                }
                try {
                    fileInputStream2.close();
                } catch (IOException e42) {
                    e42.printStackTrace();
                }
                if (i <= i2) {
                }
                return i <= i2;
            } catch (Exception e5) {
                e = e5;
                inputStream2 = fileInputStream2;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                    }
                    if (i <= i2) {
                    }
                    return i <= i2;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                inputStream2 = fileInputStream2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e7) {
            e = e7;
            e.printStackTrace();
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e422) {
                    e422.printStackTrace();
                }
            }
            if (i <= i2) {
            }
            return i <= i2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0059 A:{SYNTHETIC, Splitter: B:35:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0063 A:{SYNTHETIC, Splitter: B:40:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007e A:{SYNTHETIC, Splitter: B:54:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0075 A:{SYNTHETIC, Splitter: B:50:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x008e A:{SYNTHETIC, Splitter: B:63:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0098 A:{SYNTHETIC, Splitter: B:68:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0059 A:{SYNTHETIC, Splitter: B:35:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0063 A:{SYNTHETIC, Splitter: B:40:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007e A:{SYNTHETIC, Splitter: B:54:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0075 A:{SYNTHETIC, Splitter: B:50:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x008e A:{SYNTHETIC, Splitter: B:63:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0098 A:{SYNTHETIC, Splitter: B:68:0x0098} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String[] e(File file) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream2;
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file));
            try {
                byteArrayOutputStream2 = new ByteArrayOutputStream();
            } catch (FileNotFoundException e3) {
                e = e3;
                bufferedInputStream = bufferedInputStream2;
                try {
                    e.printStackTrace();
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedInputStream != null) {
                    }
                    if (byteArrayOutputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222 = e4;
                bufferedInputStream = bufferedInputStream2;
                e222.printStackTrace();
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = bufferedInputStream2;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e52) {
                        e52.printStackTrace();
                    }
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                while (true) {
                    int read = bufferedInputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream2.write(bArr, 0, read);
                }
                String[] split = new String(byteArrayOutputStream2.toByteArray()).split("\\n");
                if (bufferedInputStream2 == null) {
                    bufferedInputStream = bufferedInputStream2;
                } else {
                    try {
                        bufferedInputStream2.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (byteArrayOutputStream2 == null) {
                    byteArrayOutputStream = byteArrayOutputStream2;
                } else {
                    try {
                        byteArrayOutputStream2.close();
                    } catch (IOException e62) {
                        e62.printStackTrace();
                    }
                }
                return split;
            } catch (FileNotFoundException e7) {
                e = e7;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                e.printStackTrace();
                if (bufferedInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                return null;
            } catch (IOException e8) {
                e22222 = e8;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                e22222.printStackTrace();
                if (bufferedInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                return null;
            } catch (Throwable th4) {
                th = th4;
                byteArrayOutputStream = byteArrayOutputStream2;
                bufferedInputStream = bufferedInputStream2;
                if (bufferedInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (bufferedInputStream != null) {
            }
            if (byteArrayOutputStream != null) {
            }
            return null;
        } catch (IOException e10) {
            e22222 = e10;
            e22222.printStackTrace();
            if (bufferedInputStream != null) {
            }
            if (byteArrayOutputStream != null) {
            }
            return null;
        }
    }

    private static boolean eE() {
        md mdVar = new md("tms");
        String str = "6.1.0";
        if (mdVar.getString("soft_version", "").equals(str)) {
            return false;
        }
        mdVar.a("soft_version", str, true);
        return true;
    }

    public static boolean eF() {
        try {
            String externalStorageState = Environment.getExternalStorageState();
            return externalStorageState != null ? externalStorageState.equals("mounted") : false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String eG() {
        return !Environment.getExternalStorageState().equals("mounted") ? "/sdcard" : Environment.getExternalStorageDirectory().getPath();
    }

    public static final String p(String str, String -l_2_R) {
        String str2 = null;
        if (null == null) {
            String decode = Uri.decode(str);
            if (decode != null) {
                int indexOf = decode.indexOf(63);
                if (indexOf > 0) {
                    decode = decode.substring(0, indexOf);
                }
                if (!decode.endsWith("/")) {
                    int lastIndexOf = decode.lastIndexOf(47) + 1;
                    if (lastIndexOf > 0) {
                        str2 = decode.substring(lastIndexOf);
                    }
                }
            }
        }
        if (str2 != null) {
            -l_2_R = str2;
        }
        return -l_2_R != null ? -l_2_R : "downloadfile";
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0061 A:{SYNTHETIC, Splitter: B:43:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0067 A:{SYNTHETIC, Splitter: B:46:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0061 A:{SYNTHETIC, Splitter: B:43:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0067 A:{SYNTHETIC, Splitter: B:46:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0073 A:{SYNTHETIC, Splitter: B:53:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0079 A:{SYNTHETIC, Splitter: B:56:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0073 A:{SYNTHETIC, Splitter: B:53:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0079 A:{SYNTHETIC, Splitter: B:56:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0061 A:{SYNTHETIC, Splitter: B:43:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0067 A:{SYNTHETIC, Splitter: B:46:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0073 A:{SYNTHETIC, Splitter: B:53:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0079 A:{SYNTHETIC, Splitter: B:56:0x0079} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean q(String str, String str2) {
        IOException e;
        Throwable th;
        if (str == null || str.length() == 0) {
            return false;
        }
        File file = new File(str);
        if (!file.exists() || !file.canRead()) {
            return false;
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2;
            File file2 = new File(str2);
            d(file2);
            InputStream fileInputStream = new FileInputStream(file);
            try {
                fileOutputStream2 = new FileOutputStream(file2);
            } catch (IOException e2) {
                e = e2;
                inputStream = fileInputStream;
                try {
                    e.printStackTrace();
                    if (inputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = fileInputStream;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    try {
                        int read = fileInputStream.read(bArr);
                        if (read <= 0) {
                            break;
                        }
                        fileOutputStream2.write(bArr, 0, read);
                    } catch (IOException e5) {
                        e = e5;
                        fileOutputStream = fileOutputStream2;
                        inputStream = fileInputStream;
                        e.printStackTrace();
                        if (inputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        fileOutputStream = fileOutputStream2;
                        inputStream = fileInputStream;
                        if (inputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        throw th;
                    }
                }
                fileOutputStream2.flush();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e6) {
                    }
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e7) {
                    }
                }
                return true;
            } catch (IOException e8) {
                e = e8;
                fileOutputStream = fileOutputStream2;
                inputStream = fileInputStream;
                e.printStackTrace();
                if (inputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                return false;
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = fileOutputStream2;
                inputStream = fileInputStream;
                if (inputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                throw th;
            }
        } catch (IOException e9) {
            e = e9;
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e10) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e11) {
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b9 A:{SYNTHETIC, Splitter: B:44:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b9 A:{SYNTHETIC, Splitter: B:44:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c3 A:{SYNTHETIC, Splitter: B:50:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c3 A:{SYNTHETIC, Splitter: B:50:0x00c3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean r(Context -l_1_R) {
        Exception e;
        Throwable th;
        Context currentContext = TMSDKContext.getCurrentContext();
        if (currentContext != null) {
            -l_1_R = currentContext;
        }
        InputStream inputStream = null;
        int i = 0;
        try {
            inputStream = -l_1_R.getAssets().open(UpdateConfig.LOCATION_NAME, 1);
            byte[] bArr = new byte[8];
            inputStream.read(bArr);
            i = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        }
        int i2 = 0;
        try {
            InputStream fileInputStream = new FileInputStream(-l_1_R.getFilesDir().toString() + File.separator + UpdateConfig.LOCATION_NAME);
            try {
                byte[] bArr2 = new byte[8];
                try {
                    fileInputStream.read(bArr2);
                    i2 = (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16)) | ((bArr2[7] & 255) << 24);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    inputStream = fileInputStream;
                } catch (Exception e7) {
                    e = e7;
                    inputStream = fileInputStream;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                        }
                        if (i <= i2) {
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    inputStream = fileInputStream;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e8) {
                e = e8;
                inputStream = fileInputStream;
                e.printStackTrace();
                if (inputStream != null) {
                }
                if (i <= i2) {
                }
            } catch (Throwable th5) {
                th = th5;
                inputStream = fileInputStream;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        } catch (Exception e10) {
            e = e10;
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e11) {
                }
            }
            if (i <= i2) {
            }
        }
        return i <= i2;
    }

    public static List<String> s(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        List<String> arrayList = new ArrayList();
        try {
            Object[] objArr = (Object[]) storageManager.getClass().getMethod("getVolumeList", new Class[0]).invoke(storageManager, new Object[0]);
            if (objArr != null && objArr.length > 0) {
                Method declaredMethod = objArr[0].getClass().getDeclaredMethod("getPath", new Class[0]);
                Method method = storageManager.getClass().getMethod("getVolumeState", new Class[]{String.class});
                Object[] objArr2 = objArr;
                for (Object invoke : objArr) {
                    String str = (String) declaredMethod.invoke(invoke, new Object[0]);
                    if (str != null) {
                        if ("mounted".equals(method.invoke(storageManager, new Object[]{str}))) {
                            arrayList.add(str);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static int t(long j) {
        int i = 1;
        if (!eF()) {
            return 1;
        }
        if (!cL()) {
            return 2;
        }
        a aVar = new a();
        l.a(aVar);
        if (aVar.LM < j) {
            i = 0;
        }
        return i == 0 ? 3 : 0;
    }
}
