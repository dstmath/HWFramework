package com.hianalytics.android.a.a;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;

public final class a {
    static final char[] a = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static boolean b = true;
    private static Long c = Long.valueOf(30);
    private static Long d = Long.valueOf(86400);
    private static Long e = Long.valueOf(1000);
    private static Long f = Long.valueOf(1800);
    private static int g = Integer.MAX_VALUE;
    private static HandlerThread h;
    private static HandlerThread i;
    private static Handler j;
    private static Handler k;

    static {
        HandlerThread handlerThread = new HandlerThread("HiAnalytics_messageThread");
        h = handlerThread;
        handlerThread.start();
        handlerThread = new HandlerThread("HiAnalytics_sessionThread");
        i = handlerThread;
        handlerThread.start();
    }

    public static long a(String str) {
        long j = 0;
        try {
            Date parse = new SimpleDateFormat("yyyyMMddHHmmss").parse(str);
            if (parse != null) {
                j = parse.getTime();
            }
        } catch (ParseException e) {
            e.toString();
        }
        return j / 1000;
    }

    public static Long a() {
        return c;
    }

    public static String a(Context context) {
        String str = "";
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo != null) {
                Object obj = applicationInfo.metaData.get("APPKEY");
                if (obj != null) {
                    str = obj.toString();
                }
            }
        } catch (Exception e) {
        }
        return (str == null || str.trim().length() == 0) ? context.getPackageName() : str;
    }

    public static void a(int i) {
        g = i;
    }

    public static void a(Long l) {
        c = l;
    }

    public static void a(boolean z) {
        b = z;
    }

    public static boolean a(Context context, String str) {
        return context.getPackageManager().checkPermission(str, context.getPackageName()) == 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x002a A:{SYNTHETIC, Splitter: B:19:0x002a} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002a A:{SYNTHETIC, Splitter: B:19:0x002a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x003a A:{SYNTHETIC, Splitter: B:26:0x003a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x003a A:{SYNTHETIC, Splitter: B:26:0x003a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x003a A:{SYNTHETIC, Splitter: B:26:0x003a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] a(byte[] bArr) {
        Exception e;
        OutputStream outputStream;
        Throwable th;
        byte[] bArr2 = null;
        ByteArrayOutputStream outputStream2 = null;
        DeflaterOutputStream deflaterOutputStream = null;
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                try {
                    DeflaterOutputStream deflaterOutputStream2 = new DeflaterOutputStream(byteArrayOutputStream);
                    try {
                        deflaterOutputStream2.write(bArr);
                        deflaterOutputStream2.close();
                        bArr2 = byteArrayOutputStream.toByteArray();
                        try {
                            deflaterOutputStream2.close();
                            byteArrayOutputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        return bArr2;
                    } catch (Exception e3) {
                        e = e3;
                        deflaterOutputStream = deflaterOutputStream2;
                        outputStream2 = byteArrayOutputStream;
                    } catch (Throwable th2) {
                        th = th2;
                        deflaterOutputStream = deflaterOutputStream2;
                        outputStream2 = byteArrayOutputStream;
                        if (deflaterOutputStream != null) {
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    outputStream2 = byteArrayOutputStream;
                    try {
                        e.printStackTrace();
                        if (deflaterOutputStream != null) {
                        }
                        return bArr2;
                    } catch (Throwable th3) {
                        th = th3;
                        if (deflaterOutputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    outputStream2 = byteArrayOutputStream;
                    if (deflaterOutputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                outputStream2 = byteArrayOutputStream;
                e.printStackTrace();
                if (deflaterOutputStream != null) {
                }
                return bArr2;
            } catch (Throwable th5) {
                th = th5;
                outputStream2 = byteArrayOutputStream;
                if (deflaterOutputStream != null) {
                    try {
                        deflaterOutputStream.close();
                        outputStream2.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e7) {
            e = e7;
            e.printStackTrace();
            if (deflaterOutputStream != null) {
                try {
                    deflaterOutputStream.close();
                    outputStream2.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            return bArr2;
        }
    }

    public static Long b() {
        return d;
    }

    public static String b(Context context) {
        String str = "Unknown";
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo == null || applicationInfo.metaData == null) {
                return str;
            }
            Object obj = applicationInfo.metaData.get("CHANNEL");
            return obj != null ? obj.toString() : str;
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String b(String str) {
        return (str == null || str.equals("")) ? "000000000000000" : str;
    }

    public static String b(byte[] -l_4_R) {
        if (-l_4_R == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(-l_4_R.length * 2);
        for (byte b : -l_4_R) {
            stringBuilder.append(a[(b & 240) >> 4]).append(a[b & 15]);
        }
        return stringBuilder.toString();
    }

    public static void b(Long l) {
        d = l;
    }

    public static Long c() {
        return f;
    }

    public static void c(Long l) {
        e = l;
    }

    public static String[] c(Context context) {
        String[] strArr = new String[]{"Unknown", "Unknown"};
        if (context.getPackageManager().checkPermission("android.permission.ACCESS_NETWORK_STATE", context.getPackageName()) == 0) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager == null) {
                strArr[0] = "Unknown";
                return strArr;
            } else if (connectivityManager.getNetworkInfo(1).getState() != State.CONNECTED) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(0);
                if (networkInfo.getState() != State.CONNECTED) {
                    return strArr;
                }
                strArr[0] = "2G/3G";
                strArr[1] = networkInfo.getSubtypeName();
                return strArr;
            } else {
                strArr[0] = "Wi-Fi";
                return strArr;
            }
        }
        strArr[0] = "Unknown";
        return strArr;
    }

    public static int d() {
        return g;
    }

    public static void d(Long l) {
        f = l;
    }

    public static boolean d(Context context) {
        if (!(e.longValue() >= 0)) {
            return false;
        }
        String packageName = context.getPackageName();
        return !((new File(new StringBuilder("/data/data/").append(packageName).append("/shared_prefs/").append(new StringBuilder("hianalytics_state_").append(packageName).append(".xml").toString()).toString()).length() > e.longValue() ? 1 : (new File(new StringBuilder("/data/data/").append(packageName).append("/shared_prefs/").append(new StringBuilder("hianalytics_state_").append(packageName).append(".xml").toString()).toString()).length() == e.longValue() ? 0 : -1)) <= 0);
    }

    public static String e(Context context) {
        try {
            return String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            return "unknown";
        }
    }

    public static boolean e() {
        return b;
    }

    public static Handler f() {
        if (j == null) {
            Looper looper = h.getLooper();
            if (looper == null) {
                return null;
            }
            j = new Handler(looper);
        }
        return j;
    }

    public static boolean f(Context context) {
        SharedPreferences a = c.a(context, "flag");
        String str = Build.DISPLAY;
        String string = a.getString("rom_version", "");
        "currentRom=" + str + ",lastRom=" + string;
        return "".equals(string) || !string.equals(str);
    }

    public static Handler g() {
        if (k == null) {
            Looper looper = i.getLooper();
            if (looper == null) {
                return null;
            }
            k = new Handler(looper);
        }
        return k;
    }

    public static void h() {
    }

    public static String i() {
        String str = "http://data.hicloud.com:8089/sdkv1";
        "URL = " + str;
        return str;
    }
}
