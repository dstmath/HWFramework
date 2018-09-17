package tmsdk.common.utils;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import tmsdkobf.lu;

public final class l {
    private static String LI = "tms_";
    private static String LJ = "[com.android.internal.telephony.ITelephonyRegistry]";
    private static Boolean LK = null;
    private static long LL = -1;
    private static String TELEPHONY_SERVICE = "[com.android.internal.telephony.ITelephony]";

    public static class a {
        public long LM;
        public long LN;
    }

    public static String L(Context context) {
        String str = null;
        try {
            str = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return str != null ? str : "00000000000000";
    }

    public static String M(Context context) {
        String str = null;
        try {
            str = ((TelephonyManager) context.getSystemService("phone")).getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str != null ? str : "000000000000000";
    }

    public static String N(Context context) {
        return h.G(context);
    }

    public static String O(Context context) {
        String str = null;
        try {
            return ((TelephonyManager) context.getSystemService("phone")).getSimSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String P(Context context) {
        try {
            return Secure.getString(context.getContentResolver(), "android_id");
        } catch (Throwable th) {
            return "";
        }
    }

    public static int Q(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int R(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String R(boolean z) {
        String str = "";
        try {
            String readLine;
            InputStream fileInputStream = new FileInputStream("/proc/version");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream), 8192);
            String str2 = "";
            StringBuilder stringBuilder = new StringBuilder("");
            while (true) {
                try {
                    readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Throwable th) {
                                f.e("PhoneInfoUtil", th);
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th2) {
                                f.e("PhoneInfoUtil", th2);
                            }
                        }
                        str2 = stringBuilder.toString();
                    } else {
                        stringBuilder.append(readLine);
                    }
                } catch (Throwable th22) {
                    f.e("PhoneInfoUtil", th22);
                }
            }
            if (!z) {
                str = str2;
            } else if (!(str2 == null || str2.equals(""))) {
                try {
                    readLine = str2.substring("version ".length() + str2.indexOf("version "));
                    str = readLine.substring(0, readLine.indexOf(" "));
                } catch (Throwable th222) {
                    f.e("PhoneInfoUtil", th222);
                }
            }
            return str;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            str2 = stringBuilder.toString();
            if (!z) {
            }
            return str;
            str2 = stringBuilder.toString();
            if (!z) {
            }
            return str;
        } catch (Throwable th3) {
            f.e("PhoneInfoUtil", th3);
            return str;
        }
    }

    public static int S(Context context) {
        return p.cH(M(context));
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00a6 A:{SYNTHETIC, Splitter: B:51:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ac A:{SYNTHETIC, Splitter: B:54:0x00ac} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009a A:{SYNTHETIC, Splitter: B:44:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0094 A:{SYNTHETIC, Splitter: B:41:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00a6 A:{SYNTHETIC, Splitter: B:51:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ac A:{SYNTHETIC, Splitter: B:54:0x00ac} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String S(boolean z) {
        String str;
        Object obj;
        Throwable th;
        if (z) {
            str = "/sys/block/mmcblk0/device/";
            obj = "MMC";
        } else {
            str = "/sys/block/mmcblk1/device/";
            obj = "SD";
        }
        BufferedReader bufferedReader = null;
        BufferedReader bufferedReader2 = null;
        try {
            BufferedReader bufferedReader3 = new BufferedReader(new FileReader(str + "type"));
            try {
                String readLine = bufferedReader3.readLine();
                if (readLine != null) {
                    if (readLine.toUpperCase().equals(obj)) {
                        BufferedReader bufferedReader4 = new BufferedReader(new FileReader(str + "cid"));
                        try {
                            String readLine2 = bufferedReader4.readLine();
                            if (readLine2 == null) {
                                bufferedReader2 = bufferedReader4;
                            } else {
                                String trim = readLine2.trim();
                                if (bufferedReader3 != null) {
                                    try {
                                        bufferedReader3.close();
                                    } catch (IOException e) {
                                    }
                                }
                                if (bufferedReader4 != null) {
                                    try {
                                        bufferedReader4.close();
                                    } catch (IOException e2) {
                                    }
                                }
                                return trim;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader2 = bufferedReader4;
                            bufferedReader = bufferedReader3;
                            if (bufferedReader != null) {
                            }
                            if (bufferedReader2 != null) {
                            }
                            throw th;
                        }
                    }
                }
                if (bufferedReader3 != null) {
                    try {
                        bufferedReader3.close();
                    } catch (IOException e3) {
                    }
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e4) {
                    }
                }
                bufferedReader = bufferedReader3;
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = bufferedReader3;
                if (bufferedReader != null) {
                }
                if (bufferedReader2 != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                }
            }
            if (bufferedReader2 != null) {
                try {
                    bufferedReader2.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
        return null;
    }

    public static void a(File file, a aVar) {
        try {
            StatFs statFs = new StatFs(file.getPath());
            long blockSize = (long) statFs.getBlockSize();
            aVar.LM = ((long) statFs.getAvailableBlocks()) * blockSize;
            aVar.LN = ((long) statFs.getBlockCount()) * blockSize;
        } catch (Throwable e) {
            f.b("PhoneInfoUtil", "getSizeInfo err:" + e.getMessage(), e);
        }
    }

    public static void a(a aVar) {
        if (lu.eF()) {
            a(Environment.getExternalStorageDirectory(), aVar);
            return;
        }
        aVar.LM = 0;
        aVar.LN = 0;
    }

    public static void b(a aVar) {
        a(Environment.getDataDirectory(), aVar);
    }

    public static String cE(String str) {
        String str2 = SystemProperties.get(str);
        return str2 != null ? str2 : "";
    }

    public static String getProductName() {
        return Build.PRODUCT;
    }

    public static String getRadioVersion() {
        String str = "";
        try {
            return (String) Class.forName("android.os.Build").getMethod("getRadioVersion", new Class[0]).invoke(null, new Object[0]);
        } catch (Throwable th) {
            f.g("PhoneInfoUtil", th);
            return str;
        }
    }

    public static String iL() {
        return Build.MODEL;
    }

    public static boolean iM() {
        if (LK == null) {
            try {
                String[] exec = ScriptHelper.exec("service", "list");
                int i = 0;
                int i2 = 0;
                if (exec != null) {
                    if (exec.length > 0) {
                        String[] strArr = exec;
                        for (String str : exec) {
                            if (!str.contains(LI)) {
                                if (str.contains(TELEPHONY_SERVICE)) {
                                    i++;
                                } else if (str.contains(LJ)) {
                                    i2++;
                                }
                            }
                        }
                        if (i <= 1 && i2 <= 1) {
                            LK = Boolean.valueOf(false);
                        } else {
                            LK = Boolean.valueOf(true);
                        }
                    }
                }
                LK = Boolean.valueOf(false);
            } catch (Exception e) {
                LK = Boolean.valueOf(false);
            }
        }
        if (LK == null) {
            LK = Boolean.valueOf(false);
        }
        return LK.booleanValue();
    }

    public static String iN() {
        return VERSION.INCREMENTAL;
    }

    public static String iO() {
        return VERSION.RELEASE;
    }

    public static String iP() {
        return Build.BRAND;
    }

    public static String iQ() {
        return Build.DEVICE;
    }

    public static String iR() {
        return Build.BOARD;
    }

    public static String iS() {
        return R(true);
    }

    public static String iT() {
        String str = Build.MANUFACTURER;
        return str != null ? str : "UNKNOWN";
    }

    public static String iU() {
        try {
            Object obj = Build.MANUFACTURER;
            if (TextUtils.isEmpty(obj)) {
                return null;
            }
            String toLowerCase = obj.toLowerCase(Locale.ENGLISH);
            if (toLowerCase.contains("huawei")) {
                return cE("ro.build.version.emui");
            }
            if (toLowerCase.contains("xiaomi")) {
                return cE("ro.miui.ui.version.name");
            }
            String cE;
            Object cE2;
            if (toLowerCase.contains("gionee")) {
                cE = cE("ro.gn.extvernumber");
                if (TextUtils.isEmpty(cE)) {
                    cE = cE("ro.build.display.id");
                }
                return cE;
            } else if (toLowerCase.contains("vivo")) {
                cE2 = cE("ro.vivo.os.name");
                Object cE3 = cE("ro.vivo.os.version");
                cE = (TextUtils.isEmpty(cE2) || TextUtils.isEmpty(cE3)) ? cE("ro.vivo.os.build.display.id") : cE2 + "_" + cE3;
                return cE;
            } else if (toLowerCase.contains("meizu")) {
                return cE("ro.build.display.id");
            } else {
                if (toLowerCase.contains("lenovo")) {
                    cE = null;
                    cE2 = cE("ro.lenovo.lvp.version");
                    if (!TextUtils.isEmpty(cE2)) {
                        String[] split = cE2.split("_");
                        if (split != null && split.length > 0) {
                            cE = split[0];
                        }
                    }
                    if (TextUtils.isEmpty(cE)) {
                        cE = cE("ro.build.version.incremental");
                    }
                    return cE;
                }
                if (toLowerCase.contains("letv")) {
                    return cE("ro.letv.eui");
                }
                return null;
            }
        } catch (Exception e) {
            f.g("PhoneInfoUtil", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0023  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005e A:{SYNTHETIC, Splitter: B:24:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0023  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0070 A:{SYNTHETIC, Splitter: B:33:0x0070} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long iV() {
        Throwable th;
        Object obj = 1;
        if (LL == -1) {
            File file = new File("/proc/meminfo");
            DataInputStream dataInputStream = null;
            if (file.exists()) {
                try {
                    DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file));
                    try {
                        String readLine = dataInputStream2.readLine();
                        if (readLine != null) {
                            LL = Long.parseLong(readLine.trim().split("[\\s]+")[1]);
                            if (dataInputStream2 != null) {
                                try {
                                    dataInputStream2.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            throw new IOException("/proc/meminfo is empty!");
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        dataInputStream = dataInputStream2;
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (dataInputStream != null) {
                    }
                    throw th;
                }
            }
        }
        if (LL > 0) {
            obj = null;
        }
        return obj == null ? LL : 1;
    }
}
