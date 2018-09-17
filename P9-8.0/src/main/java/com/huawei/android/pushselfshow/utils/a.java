package com.huawei.android.pushselfshow.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.TextView;
import com.huawei.android.pushagent.PushReceiver.KEY_TYPE;
import com.huawei.android.pushagent.a.a.a.d;
import com.huawei.android.pushagent.a.a.a.e;
import com.huawei.android.pushagent.a.a.c;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class a {
    private static final char[] a = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static Typeface b = null;

    public static int a(int i, int i2) {
        c.a("PushSelfShowLog", "enter ctrlSockets(cmd:" + i + " param:" + i2 + ")");
        int i3 = -2;
        try {
            return ((Integer) Class.forName("dalvik.system.Zygote").getMethod("ctrlSockets", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)})).intValue();
        } catch (NoSuchMethodException e) {
            c.d("PushSelfShowLog", "NoSuchMethodException:" + e);
            return i3;
        } catch (ClassNotFoundException e2) {
            c.d("PushSelfShowLog", "ClassNotFoundException:" + e2);
            return i3;
        } catch (IllegalAccessException e3) {
            c.d("PushSelfShowLog", "IllegalAccessException:" + e3);
            return i3;
        } catch (InvocationTargetException e4) {
            c.d("PushSelfShowLog", "InvocationTargetException:" + e4);
            return i3;
        } catch (RuntimeException e5) {
            c.d("PushSelfShowLog", "RuntimeException:" + e5);
            return i3;
        } catch (Exception e6) {
            c.d("PushSelfShowLog", "Exception:" + e6);
            return i3;
        }
    }

    public static int a(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static long a() {
        return System.currentTimeMillis();
    }

    public static long a(Context context) {
        c.a("PushSelfShowLog", "enter getVersion()");
        long j = -1000;
        try {
            List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(new Intent("com.huawei.android.push.intent.REGISTER").setPackage(context.getPackageName()), 640);
            if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() == 0) {
                return -1000;
            }
            j = a((ResolveInfo) queryBroadcastReceivers.get(0), "CS_cloud_version");
            c.a("PushSelfShowLog", "get the version is :" + j);
            return j;
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    public static long a(ResolveInfo resolveInfo, String str) {
        long j = -1;
        if (resolveInfo == null) {
            return -1;
        }
        try {
            String b = b(resolveInfo, str);
            if (b == null || b.length() == 0) {
                return -1;
            }
            j = Long.parseLong(b);
            return j;
        } catch (NumberFormatException e) {
            c.b("PushSelfShowLog", str + " is not set in " + a(resolveInfo));
        }
    }

    public static Boolean a(Context context, String str, Intent intent) {
        try {
            List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
            if (queryIntentActivities != null) {
                if (queryIntentActivities.size() > 0) {
                    int size = queryIntentActivities.size();
                    int i = 0;
                    while (i < size) {
                        if (((ResolveInfo) queryIntentActivities.get(i)).activityInfo != null && str.equals(((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.packageName)) {
                            return Boolean.valueOf(true);
                        }
                        i++;
                    }
                }
            }
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
        return Boolean.valueOf(false);
    }

    public static String a(Context context, String str) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 128)).toString();
        } catch (NameNotFoundException e) {
            c.b("PushSelfShowLog", "get the app name of package:" + str + " failed.");
            return null;
        }
    }

    public static String a(ResolveInfo resolveInfo) {
        return resolveInfo.serviceInfo == null ? resolveInfo.activityInfo.packageName : resolveInfo.serviceInfo.packageName;
    }

    public static String a(String str) {
        String str2 = "";
        String str3 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDf5raDExuuXbsVNCWl48yuB89W\rfNOuuhPuS2Mptii/0UorpzypBkNTTGt11E7aorCc1lFwlB+4KDMIpFyQsdChSk+A\rt9UfhFKa95uiDpMe5rMfU+DAhoXGER6WQ2qGtrHmBWVv33i3lc76u9IgEfYuLwC6\r1mhQDHzAKPiViY6oeQIDAQAB\r";
        try {
            return a(e.a(str.getBytes("UTF-8"), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDf5raDExuuXbsVNCWl48yuB89W\rfNOuuhPuS2Mptii/0UorpzypBkNTTGt11E7aorCc1lFwlB+4KDMIpFyQsdChSk+A\rt9UfhFKa95uiDpMe5rMfU+DAhoXGER6WQ2qGtrHmBWVv33i3lc76u9IgEfYuLwC6\r1mhQDHzAKPiViY6oeQIDAQAB\r"));
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "encrypter error ", e);
            return str2;
        }
    }

    public static String a(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder(bArr.length);
        for (int i = 0; i < bArr.length; i++) {
            stringBuilder.append(a[(bArr[i] >>> 4) & 15]);
            stringBuilder.append(a[bArr[i] & 15]);
        }
        return stringBuilder.toString();
    }

    public static void a(Context context, int i) {
        if (context != null) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                if (notificationManager != null) {
                    notificationManager.cancel(i);
                }
            } catch (Exception e) {
                c.d("PushSelfShowLog", "removeNotifiCationById err:" + e.toString());
            }
            return;
        }
        c.d("PushSelfShowLog", "context is null");
    }

    public static void a(Context context, Intent intent, long j) {
        try {
            c.a("PushSelfShowLog", "enter setAPDelayAlarm(intent:" + intent.toURI() + " interval:" + j + "ms, context:" + context);
            ((AlarmManager) context.getSystemService("alarm")).set(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(context, new SecureRandom().nextInt(), intent, 0));
        } catch (Throwable e) {
            c.a("PushSelfShowLog", "set DelayAlarm error", e);
        }
    }

    /* JADX WARNING: Missing block: B:13:0x001b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(Context context, TextView textView) {
        synchronized (a.class) {
            if (context == null || textView == null) {
                c.b("PushSelfShowLog", "context is null or textView is null");
                return;
            } else if (com.huawei.android.pushagent.a.a.a.a() >= 10) {
                if (g()) {
                    String str = "chnfzxh";
                    if (com.huawei.android.pushagent.a.a.a.a() >= 11) {
                        str = "HwChinese-medium";
                    }
                    if (b == null) {
                        try {
                            b = Typeface.create(str, 0);
                        } catch (Exception e) {
                            c.d("PushSelfShowLog", e.toString());
                        }
                    }
                    if (b != null) {
                        c.a("PushSelfShowLog", "setTypeFaceEx success");
                        textView.setTypeface(b);
                    }
                }
            }
        }
    }

    public static void a(Context context, String str, com.huawei.android.pushselfshow.c.a aVar, int i) {
        if (context == null || aVar == null) {
            c.b("PushSelfShowLog", "context or msg is null");
            return;
        }
        if ("com.huawei.android.pushagent".equals(context.getPackageName())) {
            b(context, str, aVar.a(), aVar.m(), aVar.k(), i);
        } else {
            a(context, str, aVar.a(), aVar.m(), aVar.k(), i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0082 A:{SYNTHETIC, Splitter: B:41:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0091 A:{SYNTHETIC, Splitter: B:45:0x0091} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x006d A:{SYNTHETIC, Splitter: B:33:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:54:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x00a3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0040 A:{SYNTHETIC, Splitter: B:21:0x0040} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(Context context, String str, String str2) {
        Throwable e;
        Throwable th;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (!new File(str2).exists()) {
                inputStream = context.getAssets().open(str);
                FileOutputStream fileOutputStream2 = new FileOutputStream(str2);
                try {
                    byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read <= 0) {
                            break;
                        }
                        fileOutputStream2.write(bArr, 0, read);
                    }
                    fileOutputStream = fileOutputStream2;
                } catch (IOException e2) {
                    e = e2;
                    fileOutputStream = fileOutputStream2;
                    try {
                        c.e("PushSelfShowLog", "copyAsset ", e);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e3) {
                                c.e("PushSelfShowLog", "fos.close() ", e3);
                            }
                        }
                        if (inputStream == null) {
                            try {
                                inputStream.close();
                                return;
                            } catch (Throwable e32) {
                                c.e("PushSelfShowLog", "is.close() ", e32);
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutputStream2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e4) {
                            c.e("PushSelfShowLog", "fos.close() ", e4);
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e42) {
                            c.e("PushSelfShowLog", "is.close() ", e42);
                        }
                    }
                    throw th;
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Throwable e322) {
                    c.e("PushSelfShowLog", "fos.close() ", e322);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable e3222) {
                    c.e("PushSelfShowLog", "is.close() ", e3222);
                }
            }
        } catch (IOException e5) {
            e3222 = e5;
            c.e("PushSelfShowLog", "copyAsset ", e3222);
            if (fileOutputStream != null) {
            }
            if (inputStream == null) {
            }
        }
    }

    public static void a(Context context, String str, String str2, String str3, String str4, int i) {
        if (com.huawei.android.pushagent.a.a.a.c() && com.huawei.android.pushagent.a.a.a.d()) {
            c.b("PushSelfShowLog", "enter sendHiAnalytics, eventId is " + str + ",msgid is " + str2 + ",cmd is " + str3);
            new Thread(new b(context, str2, str, str3, str4, i)).start();
            return;
        }
        c.a("PushSelfShowLog", "not EMUI system or not in China, no need report analytics.");
    }

    public static void a(File file) {
        if (file != null) {
            c.a("PushSelfShowLog", "delete file " + file.getAbsolutePath());
            File file2 = new File(file.getAbsolutePath() + System.currentTimeMillis());
            if (file.renameTo(file2)) {
                if (!(file2.isFile() && file2.delete()) && file2.isDirectory()) {
                    File[] listFiles = file2.listFiles();
                    if (listFiles == null || listFiles.length == 0) {
                        if (!file2.delete()) {
                            c.a("PushSelfShowLog", "delete file failed");
                        }
                        return;
                    }
                    for (File a : listFiles) {
                        a(a);
                    }
                    if (!file2.delete()) {
                        c.a("PushSelfShowLog", "delete file unsuccess");
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a6 A:{SYNTHETIC, Splitter: B:57:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00b5 A:{SYNTHETIC, Splitter: B:61:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d4 A:{SYNTHETIC, Splitter: B:69:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004b A:{SYNTHETIC, Splitter: B:33:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:124:? A:{SYNTHETIC, RETURN, Catch:{ IOException -> 0x0035, all -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00ef A:{SYNTHETIC, Splitter: B:79:0x00ef} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fe A:{SYNTHETIC, Splitter: B:83:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x011d A:{SYNTHETIC, Splitter: B:91:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x012c A:{SYNTHETIC, Splitter: B:95:0x012c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(File file, File file2) {
        Throwable e;
        Throwable th;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            InputStream inputStream;
            try {
                try {
                    BufferedInputStream bufferedInputStream2 = new BufferedInputStream(fileInputStream2);
                    try {
                        OutputStream fileOutputStream2 = new FileOutputStream(file2);
                        OutputStream outputStream;
                        try {
                            try {
                                BufferedOutputStream bufferedOutputStream2 = new BufferedOutputStream(fileOutputStream2);
                                try {
                                    byte[] bArr = new byte[5120];
                                    while (true) {
                                        int read = bufferedInputStream2.read(bArr);
                                        if (read == -1) {
                                            break;
                                        }
                                        bufferedOutputStream2.write(bArr, 0, read);
                                    }
                                    if (bufferedInputStream2 != null) {
                                        try {
                                            bufferedInputStream2.close();
                                        } catch (Throwable e2) {
                                            c.e("PushSelfShowLog", "inBuff.close() ", e2);
                                        }
                                    }
                                    if (bufferedOutputStream2 != null) {
                                        try {
                                            bufferedOutputStream2.flush();
                                        } catch (Throwable e22) {
                                            c.d("PushSelfShowLog", e22.toString(), e22);
                                        }
                                        try {
                                            bufferedOutputStream2.close();
                                        } catch (Throwable e222) {
                                            c.d("PushSelfShowLog", e222.toString(), e222);
                                        }
                                    }
                                    if (fileOutputStream2 != null) {
                                        try {
                                            fileOutputStream2.close();
                                        } catch (Throwable e2222) {
                                            c.e("PushSelfShowLog", "output.close() ", e2222);
                                        }
                                    }
                                    if (fileInputStream2 != null) {
                                        try {
                                            fileInputStream2.close();
                                        } catch (Throwable e22222) {
                                            c.e("PushSelfShowLog", "input.close() ", e22222);
                                        }
                                    }
                                    bufferedOutputStream = bufferedOutputStream2;
                                    outputStream = fileOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    inputStream = fileInputStream2;
                                } catch (IOException e3) {
                                    e22222 = e3;
                                    bufferedOutputStream = bufferedOutputStream2;
                                    fileOutputStream = fileOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    fileInputStream = fileInputStream2;
                                    try {
                                        c.e("PushSelfShowLog", "copyFile ", e22222);
                                        if (bufferedInputStream != null) {
                                        }
                                        if (bufferedOutputStream != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        if (fileInputStream != null) {
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (bufferedInputStream != null) {
                                            try {
                                                bufferedInputStream.close();
                                            } catch (Throwable e4) {
                                                c.e("PushSelfShowLog", "inBuff.close() ", e4);
                                            }
                                        }
                                        if (bufferedOutputStream != null) {
                                            try {
                                                bufferedOutputStream.flush();
                                            } catch (Throwable e42) {
                                                c.d("PushSelfShowLog", e42.toString(), e42);
                                            }
                                            try {
                                                bufferedOutputStream.close();
                                            } catch (Throwable e422) {
                                                c.d("PushSelfShowLog", e422.toString(), e422);
                                            }
                                        }
                                        if (fileOutputStream != null) {
                                            try {
                                                fileOutputStream.close();
                                            } catch (Throwable e4222) {
                                                c.e("PushSelfShowLog", "output.close() ", e4222);
                                            }
                                        }
                                        if (fileInputStream != null) {
                                            try {
                                                fileInputStream.close();
                                            } catch (Throwable e42222) {
                                                c.e("PushSelfShowLog", "input.close() ", e42222);
                                            }
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    bufferedOutputStream = bufferedOutputStream2;
                                    outputStream = fileOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    inputStream = fileInputStream2;
                                    if (bufferedInputStream != null) {
                                    }
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (fileOutputStream != null) {
                                    }
                                    if (fileInputStream != null) {
                                    }
                                    throw th;
                                }
                            } catch (IOException e5) {
                                e22222 = e5;
                                outputStream = fileOutputStream2;
                                bufferedInputStream = bufferedInputStream2;
                                inputStream = fileInputStream2;
                                c.e("PushSelfShowLog", "copyFile ", e22222);
                                if (bufferedInputStream != null) {
                                }
                                if (bufferedOutputStream != null) {
                                }
                                if (fileOutputStream != null) {
                                }
                                if (fileInputStream != null) {
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                outputStream = fileOutputStream2;
                                bufferedInputStream = bufferedInputStream2;
                                inputStream = fileInputStream2;
                                if (bufferedInputStream != null) {
                                }
                                if (bufferedOutputStream != null) {
                                }
                                if (fileOutputStream != null) {
                                }
                                if (fileInputStream != null) {
                                }
                                throw th;
                            }
                        } catch (IOException e6) {
                            e22222 = e6;
                            outputStream = fileOutputStream2;
                            bufferedInputStream = bufferedInputStream2;
                            inputStream = fileInputStream2;
                            c.e("PushSelfShowLog", "copyFile ", e22222);
                            if (bufferedInputStream != null) {
                            }
                            if (bufferedOutputStream != null) {
                            }
                            if (fileOutputStream != null) {
                            }
                            if (fileInputStream != null) {
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            outputStream = fileOutputStream2;
                            bufferedInputStream = bufferedInputStream2;
                            inputStream = fileInputStream2;
                            if (bufferedInputStream != null) {
                            }
                            if (bufferedOutputStream != null) {
                            }
                            if (fileOutputStream != null) {
                            }
                            if (fileInputStream != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e7) {
                        e22222 = e7;
                        bufferedInputStream = bufferedInputStream2;
                        inputStream = fileInputStream2;
                        c.e("PushSelfShowLog", "copyFile ", e22222);
                        if (bufferedInputStream != null) {
                        }
                        if (bufferedOutputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        if (fileInputStream != null) {
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        bufferedInputStream = bufferedInputStream2;
                        inputStream = fileInputStream2;
                        if (bufferedInputStream != null) {
                        }
                        if (bufferedOutputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    e22222 = e8;
                    inputStream = fileInputStream2;
                    c.e("PushSelfShowLog", "copyFile ", e22222);
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    if (fileInputStream != null) {
                    }
                } catch (Throwable th7) {
                    th = th7;
                    inputStream = fileInputStream2;
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e9) {
                e22222 = e9;
                inputStream = fileInputStream2;
                c.e("PushSelfShowLog", "copyFile ", e22222);
                if (bufferedInputStream != null) {
                }
                if (bufferedOutputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                if (fileInputStream != null) {
                }
            } catch (Throwable th8) {
                th = th8;
                inputStream = fileInputStream2;
                if (bufferedInputStream != null) {
                }
                if (bufferedOutputStream != null) {
                }
                if (fileOutputStream != null) {
                }
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (IOException e10) {
            e22222 = e10;
            c.e("PushSelfShowLog", "copyFile ", e22222);
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (Throwable e222222) {
                    c.e("PushSelfShowLog", "inBuff.close() ", e222222);
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.flush();
                } catch (Throwable e2222222) {
                    c.d("PushSelfShowLog", e2222222.toString(), e2222222);
                }
                try {
                    bufferedOutputStream.close();
                } catch (Throwable e22222222) {
                    c.d("PushSelfShowLog", e22222222.toString(), e22222222);
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Throwable e222222222) {
                    c.e("PushSelfShowLog", "output.close() ", e222222222);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable e2222222222) {
                    c.e("PushSelfShowLog", "input.close() ", e2222222222);
                }
            }
        }
    }

    public static boolean a(Context context, Intent intent) {
        if (context == null) {
            c.b("PushSelfShowLog", "context is null");
            return false;
        } else if (intent != null) {
            List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 640);
            if (queryIntentActivities == null || queryIntentActivities.size() == 0) {
                c.d("PushSelfShowLog", "no activity exist, may be system Err!! pkgName:");
                return false;
            }
            boolean z = ((ResolveInfo) queryIntentActivities.get(0)).activityInfo.exported;
            c.b("PushSelfShowLog", "exportedFlag:" + z);
            CharSequence charSequence = ((ResolveInfo) queryIntentActivities.get(0)).activityInfo.permission;
            c.b("PushSelfShowLog", "need permission:" + charSequence);
            if (z) {
                return TextUtils.isEmpty(charSequence) || "com.huawei.pushagent.permission.LAUNCH_ACTIVITY".equals(charSequence);
            } else {
                return false;
            }
        } else {
            c.b("PushSelfShowLog", "intent is null");
            return false;
        }
    }

    public static boolean a(String str, String str2) {
        try {
            boolean mkdirs = new File(str2).mkdirs();
            c.a("PushSelfShowLog", "urlSrc is %s ,urlDest is %s,urlDest is already exist?%s ", str, str2, Boolean.valueOf(mkdirs));
            File[] listFiles = new File(str).listFiles();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i].isFile()) {
                        a(listFiles[i], new File(str2 + File.separator + listFiles[i].getName()));
                    }
                    if (listFiles[i].isDirectory()) {
                        b(str + File.separator + listFiles[i].getName(), str2 + File.separator + listFiles[i].getName());
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "fileCopy error ", e);
            return false;
        }
    }

    public static long b(String str) {
        if (str == null) {
            str = "";
        }
        try {
            Date date = new Date();
            int hours = (date.getHours() * 2) + (date.getMinutes() / 30);
            String concat = str.concat(str);
            c.a("PushSelfShowLog", "startIndex is %s ,and ap is %s ,length is %s", Integer.valueOf(hours), concat, Integer.valueOf(concat.length()));
            int i = hours;
            while (i < concat.length()) {
                if (concat.charAt(i) == '0') {
                    i++;
                } else {
                    long minutes = ((long) (((i - hours) * 30) - (date.getMinutes() % 30))) * 60000;
                    c.a("PushSelfShowLog", "startIndex is %s i is %s delay %s", Integer.valueOf(hours), Integer.valueOf(i), Long.valueOf(minutes));
                    if ((minutes < 0 ? 1 : null) != null) {
                        minutes = 0;
                    }
                    return minutes;
                }
            }
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "error ", e);
        }
        return 0;
    }

    public static Intent b(Context context, String str) {
        Intent intent = null;
        try {
            return context.getPackageManager().getLaunchIntentForPackage(str);
        } catch (Throwable e) {
            c.b("PushSelfShowLog", e.toString(), e);
            return intent;
        }
    }

    public static String b(Context context) {
        String str = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            return telephonyManager == null ? str : telephonyManager.getDeviceId();
        } catch (Exception e) {
            c.d("PushSelfShowLog", e.toString());
            return str;
        }
    }

    private static String b(ResolveInfo resolveInfo, String str) {
        Bundle bundle = resolveInfo.serviceInfo == null ? resolveInfo.activityInfo.metaData : resolveInfo.serviceInfo.metaData;
        return bundle != null ? bundle.getString(str) : null;
    }

    public static void b(Context context, Intent intent) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            int i = 0;
            if (intent.hasExtra("selfshow_notify_id")) {
                i = intent.getIntExtra("selfshow_notify_id", 0) + 3;
            }
            c.a("PushSelfShowLog", "setDelayAlarm(cancel) alarmNotityId " + i + " and intent is " + intent.toURI());
            Intent intent2 = new Intent("com.huawei.intent.action.PUSH");
            intent2.setPackage(context.getPackageName()).setFlags(32);
            PendingIntent broadcast = PendingIntent.getBroadcast(context, i, intent2, 536870912);
            if (broadcast == null) {
                c.a("PushSelfShowLog", "alarm not exist");
                return;
            }
            c.a("PushSelfShowLog", "  alarm cancel");
            alarmManager.cancel(broadcast);
        } catch (Exception e) {
            c.d("PushSelfShowLog", "cancelAlarm err:" + e.toString());
        }
    }

    public static void b(Context context, String str, String str2, String str3, String str4, int i) {
        c.b("PushSelfShowLog", "enter bdReport, cmd =" + str3 + ", msgid = " + str2 + ", eventId = " + str + ",notifyId= " + i);
        if (context == null) {
            c.d("PushSelfShowLog", "context is null");
        } else if (c(str)) {
            try {
                if ("-1".equals(str)) {
                    str = "101";
                } else if ("0".equals(str)) {
                    str = "100";
                }
                int parseInt = Integer.parseInt(str);
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("msgId", str2);
                jSONObject.put("version", "2907");
                jSONObject.put("cmd", str3);
                jSONObject.put("pkg", str4);
                jSONObject.put(KEY_TYPE.PUSH_KEY_NOTIFY_ID, i);
                Class cls = Class.forName("com.huawei.bd.Reporter");
                cls.getMethod("j", new Class[]{Context.class, Integer.TYPE, JSONObject.class}).invoke(cls, new Object[]{context, Integer.valueOf(parseInt), jSONObject});
                c.b("PushSelfShowLog", "bd success");
            } catch (Throwable e) {
                c.d("PushSelfShowLog", e.toString(), e);
            } catch (ClassNotFoundException e2) {
                c.d("PushSelfShowLog", e2.toString());
            } catch (Throwable e3) {
                c.d("PushSelfShowLog", e3.toString(), e3);
            } catch (Throwable e32) {
                c.d("PushSelfShowLog", e32.toString(), e32);
            } catch (Throwable e322) {
                c.d("PushSelfShowLog", e322.toString(), e322);
            } catch (Throwable e3222) {
                c.d("PushSelfShowLog", e3222.toString(), e3222);
            } catch (Throwable e32222) {
                c.d("PushSelfShowLog", e32222.toString(), e32222);
            } catch (Throwable e322222) {
                c.d("PushSelfShowLog", e322222.toString(), e322222);
            }
        } else {
            c.b("PushSelfShowLog", str + " need not bdreport");
        }
    }

    public static void b(File file) {
        c.a("PushSelfShowLog", "delete file before ");
        if (file != null && file.exists()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length != 0) {
                long currentTimeMillis = System.currentTimeMillis();
                for (File file2 : listFiles) {
                    try {
                        if ((currentTimeMillis - file2.lastModified() <= 86400000 ? 1 : null) == null) {
                            c.e("PushSelfShowLog", "delete file before " + file2.getAbsolutePath());
                            a(file2);
                        }
                    } catch (Exception e) {
                        c.e("PushSelfShowLog", e.toString());
                    }
                }
            }
        }
    }

    private static void b(String str, String str2) throws IOException {
        if (new File(str2).mkdirs()) {
            c.e("PushSelfShowLog", "mkdir");
        }
        File[] listFiles = new File(str).listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile()) {
                    a(listFiles[i], new File(new File(str2).getAbsolutePath() + File.separator + listFiles[i].getName()));
                }
                if (listFiles[i].isDirectory()) {
                    b(str + "/" + listFiles[i].getName(), str2 + "/" + listFiles[i].getName());
                }
            }
        }
    }

    public static boolean b() {
        return VERSION.SDK_INT >= 11;
    }

    public static ArrayList c(Context context) {
        ArrayList arrayList = new ArrayList();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse("market://details?id="));
        List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!(queryIntentActivities == null || queryIntentActivities.size() == 0)) {
            int size = queryIntentActivities.size();
            for (int i = 0; i < size; i++) {
                if (((ResolveInfo) queryIntentActivities.get(i)).activityInfo != null) {
                    arrayList.add(((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.packageName);
                }
            }
        }
        return arrayList;
    }

    public static boolean c() {
        return VERSION.SDK_INT >= 16;
    }

    public static boolean c(Context context, String str) {
        if (context == null || str == null || "".equals(str)) {
            return false;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(str, 8192) == null) {
                return false;
            }
            c.a("PushSelfShowLog", str + " is installed");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean c(String str) {
        if (TextUtils.isEmpty(str)) {
            c.b("PushSelfShowLog", "eventId is empty");
            return false;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-1");
        arrayList.add("0");
        arrayList.add("1");
        arrayList.add("2");
        arrayList.add("14");
        arrayList.add("17");
        return arrayList.contains(str);
    }

    public static String d(Context context, String str) {
        String str2 = "";
        try {
            String str3 = "";
            str2 = ((Environment.getExternalStorageState().equals("mounted") ? Environment.getExternalStorageDirectory().getPath() : context.getFilesDir().getPath()) + File.separator + "PushService") + File.separator + str;
            c.a("PushSelfShowLog", "dbPath is " + str2);
            return str2;
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "getDbPath error", e);
            return str2;
        }
    }

    public static boolean d() {
        return com.huawei.android.pushagent.a.a.a.a() >= 9;
    }

    public static boolean d(Context context) {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setPackage("com.android.email");
        intent.setData(Uri.fromParts("mailto", "xxxx@xxxx.com", null));
        List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        return (queryIntentActivities == null || queryIntentActivities.size() == 0) ? false : true;
    }

    public static final String e() {
        return com.huawei.android.pushagent.a.a.a.c.a(String.valueOf("com.huawei.android.pushagent".hashCode()));
    }

    public static void e(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            c.b("PushSelfShowLog", "url is null.");
            return;
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(str));
            intent.setFlags(402653184);
            List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
            String str2 = null;
            for (ResolveInfo resolveInfo : queryIntentActivities) {
                String str3 = resolveInfo.activityInfo.packageName;
                if (f(context, str3)) {
                    str2 = str3;
                    break;
                }
            }
            if (str2 == null) {
                String str4 = "com.android.browser";
                for (ResolveInfo resolveInfo2 : queryIntentActivities) {
                    String str5 = resolveInfo2.activityInfo.packageName;
                    if (str4.equalsIgnoreCase(str5)) {
                        str2 = str5;
                        break;
                    }
                }
            }
            if (str2 != null) {
                intent.setPackage(str2);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            c.d("PushSelfShowLog", "start browser activity failed, exception:" + e.getMessage());
        }
    }

    public static boolean e(Context context) {
        return "com.huawei.android.pushagent".equals(context.getPackageName());
    }

    public static final String f() {
        return com.huawei.android.pushagent.a.a.a.c.a(String.valueOf("com.huawei.hwid".hashCode()));
    }

    public static boolean f(Context context) {
        return "com.huawei.hwid".equals(context.getPackageName());
    }

    public static boolean f(Context context, String str) {
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        context.getPackageManager().getPreferredActivities(arrayList, arrayList2, str);
        return arrayList2 != null && arrayList2.size() > 0;
    }

    private static boolean g() {
        return "zh".equals(Locale.getDefault().getLanguage());
    }

    public static boolean g(Context context) {
        boolean z = false;
        try {
            if (context.getPackageManager().getApplicationInfo("com.huawei.android.pushagent", 128) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean h(Context context) {
        boolean z = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider.a.a, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int i = cursor.getInt(cursor.getColumnIndex("isSupport"));
                c.a("PushSelfShowLog", "isExistProvider:" + i);
                if (1 == i) {
                    z = true;
                }
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            }
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } catch (Throwable e) {
            c.a("PushSelfShowLog", e.toString(), e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int i(Context context) {
        if (context == null) {
            return 3;
        }
        return (VERSION.SDK_INT >= 16 && context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null) != 0) ? 0 : 3;
    }

    public static int j(Context context) {
        try {
            Class cls = Class.forName("com.huawei.android.immersion.ImmersionStyle");
            int intValue = ((Integer) cls.getDeclaredMethod("getPrimaryColor", new Class[]{Context.class}).invoke(cls, new Object[]{context})).intValue();
            c.b("PushSelfShowLog", "colorPrimary:" + intValue);
            return intValue;
        } catch (ClassNotFoundException e) {
            c.d("PushSelfShowLog", "ImmersionStyle ClassNotFoundException");
            return 0;
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", e2.toString(), e2);
            return 0;
        } catch (Throwable e22) {
            c.d("PushSelfShowLog", e22.toString(), e22);
            return 0;
        } catch (Throwable e222) {
            c.d("PushSelfShowLog", e222.toString(), e222);
            return 0;
        } catch (Throwable e2222) {
            c.d("PushSelfShowLog", e2222.toString(), e2222);
            return 0;
        } catch (Throwable e22222) {
            c.d("PushSelfShowLog", e22222.toString(), e22222);
            return 0;
        }
    }

    public static int k(Context context) {
        int i = -1;
        try {
            Class cls = Class.forName("com.huawei.android.immersion.ImmersionStyle");
            int intValue = ((Integer) cls.getDeclaredMethod("getPrimaryColor", new Class[]{Context.class}).invoke(cls, new Object[]{context})).intValue();
            i = ((Integer) cls.getDeclaredMethod("getSuggestionForgroundColorStyle", new Class[]{Integer.TYPE}).invoke(cls, new Object[]{Integer.valueOf(intValue)})).intValue();
            c.b("PushSelfShowLog", "getSuggestionForgroundColorStyle:" + i);
            return i;
        } catch (ClassNotFoundException e) {
            c.d("PushSelfShowLog", "ImmersionStyle ClassNotFoundException");
            return i;
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", e2.toString(), e2);
            return i;
        } catch (Throwable e22) {
            c.d("PushSelfShowLog", e22.toString(), e22);
            return i;
        } catch (Throwable e222) {
            c.d("PushSelfShowLog", e222.toString(), e222);
            return i;
        } catch (Throwable e2222) {
            c.d("PushSelfShowLog", e2222.toString(), e2222);
            return i;
        } catch (Throwable e22222) {
            c.d("PushSelfShowLog", e22222.toString(), e22222);
            return i;
        }
    }

    public static String l(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            return externalCacheDir.getPath();
        }
        return Environment.getExternalStorageDirectory().getPath() + ("/Android/data/" + context.getPackageName() + "/cache");
    }

    public static String m(Context context) {
        String str = "";
        String str2 = "";
        String packageName = context.getPackageName();
        str2 = !"com.huawei.android.pushagent".equals(packageName) ? !"com.huawei.hwid".equals(packageName) ? d.b(context, new com.huawei.android.pushagent.a.a.e(context, "push_client_self_info").b("push_notify_key")) : f() : e();
        try {
            return d.a(context, packageName, str2.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            c.d("PushSelfShowLog", e.toString());
            return str;
        }
    }

    private static boolean o(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        int i = -1;
        try {
            i = Secure.getInt(context.getContentResolver(), "user_experience_involved", -1);
            c.a("PushSelfShowLog", "settingMainSwitch:" + i);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
        if (i == 1) {
            z = true;
        }
        return z;
    }
}
