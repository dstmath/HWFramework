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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;

/* compiled from: Unknown */
public final class a {
    static final char[] a = null;
    private static boolean b;
    private static Long c;
    private static Long d;
    private static Long e;
    private static Long f;
    private static int g;
    private static HandlerThread h;
    private static HandlerThread i;
    private static Handler j;
    private static Handler k;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.hianalytics.android.a.a.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.hianalytics.android.a.a.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.hianalytics.android.a.a.a.<clinit>():void");
    }

    public static long a(String str) {
        long time;
        try {
            Date parse = new SimpleDateFormat("yyyyMMddHHmmss").parse(str);
            if (parse != null) {
                time = parse.getTime();
                return time / 1000;
            }
        } catch (ParseException e) {
            e.toString();
        }
        time = 0;
        return time / 1000;
    }

    public static Long a() {
        return c;
    }

    public static String a(Context context) {
        String str = "";
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), SystemCallLogFilterConsts.NOTIFY_SHORT_CALL);
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

    public static byte[] a(byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream;
        DeflaterOutputStream deflaterOutputStream;
        Exception e;
        Throwable th;
        byte[] bArr2 = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
                try {
                    deflaterOutputStream.write(bArr);
                    deflaterOutputStream.close();
                    bArr2 = byteArrayOutputStream.toByteArray();
                    try {
                        deflaterOutputStream.close();
                        byteArrayOutputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return bArr2;
                } catch (Exception e3) {
                    e = e3;
                    try {
                        e.printStackTrace();
                        if (deflaterOutputStream != null) {
                            try {
                                deflaterOutputStream.close();
                                byteArrayOutputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        return bArr2;
                    } catch (Throwable th2) {
                        th = th2;
                        if (deflaterOutputStream != null) {
                            try {
                                deflaterOutputStream.close();
                                byteArrayOutputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e5) {
                e = e5;
                deflaterOutputStream = bArr2;
                e.printStackTrace();
                if (deflaterOutputStream != null) {
                    deflaterOutputStream.close();
                    byteArrayOutputStream.close();
                }
                return bArr2;
            } catch (Throwable th3) {
                th = th3;
                deflaterOutputStream = bArr2;
                if (deflaterOutputStream != null) {
                    deflaterOutputStream.close();
                    byteArrayOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            deflaterOutputStream = bArr2;
            byteArrayOutputStream = bArr2;
            e.printStackTrace();
            if (deflaterOutputStream != null) {
                deflaterOutputStream.close();
                byteArrayOutputStream.close();
            }
            return bArr2;
        } catch (Throwable th4) {
            th = th4;
            deflaterOutputStream = bArr2;
            byteArrayOutputStream = bArr2;
            if (deflaterOutputStream != null) {
                deflaterOutputStream.close();
                byteArrayOutputStream.close();
            }
            throw th;
        }
    }

    public static Long b() {
        return d;
    }

    public static String b(Context context) {
        String str = "Unknown";
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), SystemCallLogFilterConsts.NOTIFY_SHORT_CALL);
            if (!(applicationInfo == null || applicationInfo.metaData == null)) {
                Object obj = applicationInfo.metaData.get("CHANNEL");
                if (obj != null) {
                    return obj.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String b(String str) {
        return (str == null || str.equals("")) ? "000000000000000" : str;
    }

    public static String b(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
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
