package com.huawei.android.pushagent.utils;

import android.app.AppGlobals;
import android.app.INotificationManager.Stub;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.utils.a.g;
import com.huawei.android.pushagent.utils.c.c;
import com.huawei.android.pushagent.utils.c.e;
import com.huawei.android.pushagent.utils.d.a;
import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class b {
    public static byte[] uj(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static int tw(byte[] bArr) {
        return ((bArr[0] & 255) << 8) | (bArr[1] & 255);
    }

    public static int ur(byte b) {
        return b & 255;
    }

    public static byte[] uq(int i) {
        return new byte[]{(byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static String ua(Context context) {
        String tz = tz(context);
        if (!TextUtils.isEmpty(tz)) {
            return tz;
        }
        c qx = e.qx(context, h.dp(context).dq());
        tz = qx.qv();
        int deviceIdType = qx.getDeviceIdType();
        h.dp(context).dr(tz);
        h.dp(context).setDeviceIdType(deviceIdType);
        return tz;
    }

    public static int ub(Context context) {
        if (-1 == h.dp(context).getDeviceIdType()) {
            ua(context);
        }
        return h.dp(context).getDeviceIdType();
    }

    public static String ui(Context context) {
        return String.valueOf(2951);
    }

    public static boolean uk(Context context, String str) {
        List ts = ts(context.getPackageManager(), new Intent("com.huawei.android.push.intent.RECEIVE").setPackage(str), 787072, f.vb());
        if (ts == null || ts.size() == 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "isClientSupportMsgResponse: false");
            return false;
        }
        String str2 = ((ResolveInfo) ts.get(0)).serviceInfo != null ? ((ResolveInfo) ts.get(0)).serviceInfo.packageName : ((ResolveInfo) ts.get(0)).activityInfo.packageName;
        if (str2 != null && str2.equals(str)) {
            Object tt = tt((ResolveInfo) ts.get(0), "CS_cloud_ablitity");
            if (TextUtils.isEmpty(tt)) {
                return false;
            }
            try {
                boolean contains = Arrays.asList(tt.split("\\|")).contains("successRateAnalytics");
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "isClientSupportMsgResponse:" + contains);
                return contains;
            } catch (Exception e) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e.toString());
            }
        }
        return false;
    }

    public static String tt(ResolveInfo resolveInfo, String str) {
        Bundle bundle = resolveInfo.serviceInfo != null ? resolveInfo.serviceInfo.metaData : resolveInfo.activityInfo.metaData;
        if (bundle == null) {
            return null;
        }
        return bundle.getString(str);
    }

    public static String tn(Context context) {
        String simOperator = ((TelephonyManager) context.getSystemService("phone")).getSimOperator();
        if (simOperator == null) {
            return "";
        }
        char[] toCharArray = simOperator.toCharArray();
        int i = 0;
        while (i < toCharArray.length) {
            if (toCharArray[i] < '0' || toCharArray[i] > '9') {
                return simOperator.substring(0, i);
            }
            i++;
        }
        return simOperator;
    }

    public static String tq(long j, String str) {
        String str2 = "";
        try {
            return new SimpleDateFormat(str).format(new Date(j));
        } catch (Exception e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "getTimeString,milliseconds:" + j + " e:" + e.toString());
            return str2;
        }
    }

    public static long tp(String str) {
        try {
            Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").parse(str);
            if (parse != null) {
                return parse.getTime();
            }
            return -1;
        } catch (ParseException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "ParseException,timeStr:" + str + " e:" + e.toString());
            return -1;
        }
    }

    public static int tm(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return -1;
        }
        NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
        if (allNetworkInfo == null) {
            return -1;
        }
        for (int i = 0; i < allNetworkInfo.length; i++) {
            if (allNetworkInfo[i].getState() == State.CONNECTED) {
                return allNetworkInfo[i].getType();
            }
        }
        return -1;
    }

    public static boolean ul(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        if (activeNetworkInfo.getState() == State.CONNECTED || activeNetworkInfo.getState() == State.CONNECTING) {
            return true;
        }
        return false;
    }

    public static boolean tx(Context context, String str) {
        String str2 = "/data/misc/hwpush" + File.separator + str + ".xml";
        File file = new File(str2);
        if (file.exists() && file.isFile() && file.canWrite()) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter deletePrefrence(fileName:" + str + ".xml)");
            return file.delete();
        }
        if (file.exists()) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "delete File:" + str2 + " failed!!");
        }
        return false;
    }

    public static void uo() {
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter powerLow");
        try {
            Class.forName("com.huawei.pgmng.log.LogPower").getMethod("push", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(119)});
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "ClassNotFoundException, not support LogPower");
        } catch (NoSuchMethodException e2) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "NoSuchMethodException, not support LogPower");
        } catch (IllegalArgumentException e3) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "IllegalArgumentException, not support LogPower");
        } catch (IllegalAccessException e4) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "IllegalAccessException, not support LogPower");
        } catch (InvocationTargetException e5) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "InvocationTargetException, not support LogPower");
        }
    }

    public static int tl(int i, int i2) {
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter ctrlSockets(cmd:" + i + " param:" + i2 + ")");
        try {
            return ((Integer) Class.forName("dalvik.system.Zygote").getMethod("ctrlSockets", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)})).intValue();
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return -2;
        } catch (NoSuchMethodException e2) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return -2;
        } catch (IllegalArgumentException e3) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return -2;
        } catch (IllegalAccessException e4) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return -2;
        } catch (InvocationTargetException e5) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return -2;
        }
    }

    public static int tk(Socket socket) {
        int i = 0;
        int intValue;
        try {
            intValue = ((Integer) FileDescriptor.class.getMethod("getInt$", new Class[0]).invoke((FileDescriptor) Socket.class.getMethod("getFileDescriptor$", new Class[0]).invoke(socket, new Object[0]), new Object[0])).intValue();
            try {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "socket fd is " + intValue);
                return intValue;
            } catch (NoSuchMethodException e) {
                i = intValue;
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
                return i;
            } catch (IllegalArgumentException e2) {
                i = intValue;
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
                return i;
            } catch (IllegalAccessException e3) {
                i = intValue;
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
                return i;
            } catch (InvocationTargetException e4) {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
                return intValue;
            }
        } catch (NoSuchMethodException e5) {
        } catch (IllegalArgumentException e6) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return i;
        } catch (IllegalAccessException e7) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return i;
        } catch (InvocationTargetException e8) {
            intValue = 0;
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "There is no method of ctrlSockets.");
            return intValue;
        }
    }

    public static void us(Context context, long j) {
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter wakeSystem");
        ((PowerManager) context.getSystemService("power")).newWakeLock(1, "dispatcher").acquire(j);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0020  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String to(Context context) {
        if (context == null) {
            return "";
        }
        String extraInfo;
        String str = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                extraInfo = activeNetworkInfo.getExtraInfo();
                if (extraInfo == null) {
                    extraInfo = "";
                }
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "push apn is " + extraInfo);
                return extraInfo;
            }
        }
        extraInfo = str;
        if (extraInfo == null) {
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "push apn is " + extraInfo);
        return extraInfo;
    }

    public static String uc() {
        String str = "";
        Class[] clsArr = new Class[]{String.class};
        Object[] objArr = new Object[]{"ro.build.version.emui"};
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            String str2 = (String) cls.getDeclaredMethod("get", clsArr).invoke(cls, objArr);
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "get EMUI version is:" + str2);
            if (TextUtils.isEmpty(str2)) {
                return str;
            }
            return str2;
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " getEmuiVersion wrong, ClassNotFoundException");
        } catch (LinkageError e2) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " getEmuiVersion wrong, LinkageError");
        } catch (NoSuchMethodException e3) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " getEmuiVersion wrong, NoSuchMethodException");
        } catch (Exception e4) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " getEmuiVersion wrong");
        }
    }

    public static boolean un() {
        Class[] clsArr = new Class[]{String.class, Boolean.TYPE};
        Object[] objArr = new Object[]{"ro.config.bg_data_switch", Boolean.valueOf(false)};
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            boolean booleanValue = ((Boolean) cls.getDeclaredMethod("getBoolean", clsArr).invoke(cls, objArr)).booleanValue();
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "is support HW network policy:" + booleanValue);
            return booleanValue;
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " get SupportHwNetworkPolicy wrong, ClassNotFoundException");
            return false;
        } catch (LinkageError e2) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " get SupportHwNetworkPolicy wrong, LinkageError");
            return false;
        } catch (NoSuchMethodException e3) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " get SupportHwNetworkPolicy wrong, NoSuchMethodException");
            return false;
        } catch (Exception e4) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", " get SupportHwNetworkPolicy wrong");
            return false;
        }
    }

    public static boolean um(Context context, String str, int i) {
        if (context == null || str == null || "".equals(str)) {
            return false;
        }
        try {
            PackageInfo packageInfoAsUser = context.getPackageManager().getPackageInfoAsUser(str, 0, i);
            if (packageInfoAsUser == null) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "query package isInstalled failed! packageInfo is null");
                return false;
            }
            if (str.equals(packageInfoAsUser.packageName)) {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", str + " is installed in user " + i);
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "query package isInstalled failed, NameNotFoundException!");
        } catch (Exception e2) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "query package isInstalled failed!");
        }
    }

    private static String tz(Context context) {
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter getCachedDeviceId()");
        return h.dp(context).getDeviceId();
    }

    public static String ue(String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str + "/" + f.vc(str2);
    }

    public static String uf(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str.split("/")[0];
    }

    public static int uh(String str) {
        int i = 0;
        if (TextUtils.isEmpty(str)) {
            return i;
        }
        String[] split = str.split("/");
        if (split.length > 1) {
            try {
                return Integer.parseInt(split[1]);
            } catch (Exception e) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "parse int error:" + split[1]);
            }
        }
        return i;
    }

    public static String ty(String str) {
        if (str.indexOf("/") <= 0) {
            return str + "/" + "00";
        }
        return str;
    }

    public static boolean tv(Context context, String str, int i) {
        if (context == null) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter getNotificationsBanned, context is null");
            return false;
        } else if (TextUtils.isEmpty(str)) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter getNotificationsBanned, pkg is empty");
            return false;
        } else {
            try {
                ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(str, 0, i);
                if (applicationInfo == null) {
                    com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "enter getNotificationsBanned, ApplicationInfo is empty");
                    return false;
                }
                boolean areNotificationsEnabledForPackage = Stub.asInterface(ServiceManager.getService("notification")).areNotificationsEnabledForPackage(str, applicationInfo.uid);
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "areNotificationsEnabledForPackage:" + areNotificationsEnabledForPackage);
                return areNotificationsEnabledForPackage;
            } catch (Throwable e) {
                com.huawei.android.pushagent.utils.d.c.sk("PushLog2951", "Error calling NoMan", e);
                return false;
            }
        }
    }

    public static boolean tu(Context context, String str, int i) {
        if (context == null || str == null) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "areNotificationEnableForChannel context or pkg is null");
            return true;
        }
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            int intValue = ((Integer) notificationManager.getClass().getDeclaredField("IMPORTANCE_NONE").get(notificationManager)).intValue();
            ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(str, 0, i);
            if (applicationInfo == null) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "ApplicationInfo is empty, maybe app is not installed ");
                return true;
            }
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", str + "  in user " + i + " ,uid is " + applicationInfo.uid);
            Class cls = Class.forName("android.app.NotificationManager");
            Object invoke = cls.getDeclaredMethod("getService", new Class[0]).invoke(cls, new Object[0]);
            Object invoke2 = Class.forName("android.app.INotificationManager").getDeclaredMethod("getNotificationChannelForPackage", new Class[]{String.class, Integer.TYPE, String.class, Boolean.TYPE}).invoke(invoke, new Object[]{str, Integer.valueOf(applicationInfo.uid), "com.huawei.android.pushagent", Boolean.valueOf(false)});
            if (invoke2 == null) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "notificationChannel is null, maybe not set");
                return true;
            }
            int intValue2 = ((Integer) Class.forName("android.app.NotificationChannel").getDeclaredMethod("getImportance", new Class[0]).invoke(invoke2, new Object[0])).intValue();
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "importance:" + intValue2);
            if (intValue == intValue2) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "areNotificationEnableForChannel:false");
                return false;
            }
            return true;
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e.toString());
        } catch (NoSuchMethodException e2) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e2.toString());
        } catch (IllegalAccessException e3) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e3.toString());
        } catch (IllegalArgumentException e4) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e4.toString());
        } catch (InvocationTargetException e5) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e5.toString());
        } catch (NoSuchFieldException e6) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e6.toString());
        } catch (RemoteException e7) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e7.toString());
        } catch (Exception e8) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", e8.toString());
        }
    }

    public static final String ug() {
        return g.ol(String.valueOf("com.huawei.android.pushagent".hashCode()));
    }

    public static String ud(Context context, String str, String str2, String str3) {
        if (context == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str3)) {
            return "";
        }
        String ue = ue(str, str2);
        Object nu = com.huawei.android.pushagent.utils.a.e.nu(new a(context, "push_notify_key").rt(ue));
        if (!TextUtils.isEmpty(nu)) {
            return nu;
        }
        String ol = g.ol(String.valueOf((str3 + System.currentTimeMillis()).hashCode()));
        new a(context, "push_notify_key").rv(ue, com.huawei.android.pushagent.utils.a.e.nv(ol));
        return ol;
    }

    public static void up(Context context, String str, String str2, String str3) {
        if (str != null && str3 != null) {
            try {
                Intent flags = new Intent("com.huawei.android.push.intent.REGISTRATION").setPackage(str).putExtra("device_token", str3.getBytes("UTF-8")).putExtra("belongId", com.huawei.android.pushagent.model.a.g.aq(context).getBelongId()).setFlags(32);
                Object ud = ud(context, str, str2, str3);
                if (!TextUtils.isEmpty(ud)) {
                    flags.putExtra("extra_encrypt_key", ud);
                }
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "send registerToken to:" + str);
                tr(context, flags, Integer.parseInt(str2));
            } catch (Throwable e) {
                com.huawei.android.pushagent.utils.d.c.se("PushLog2951", e.toString(), e);
            }
        }
    }

    public static void tr(Context context, Intent intent, int i) {
        boolean supportsMultipleUsers = UserManager.supportsMultipleUsers();
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "isSupportsMultiUsers: " + supportsMultipleUsers);
        if (supportsMultipleUsers) {
            context.sendBroadcastAsUser(intent, new UserHandle(i));
        } else {
            context.sendBroadcast(intent);
        }
    }

    public static List<ResolveInfo> ts(PackageManager packageManager, Intent intent, int i, int i2) {
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter queryBroadcastReceiversAsUser");
        if (packageManager == null || intent == null) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "packageManager is null");
            return null;
        }
        try {
            return (List) Class.forName("android.content.pm.PackageManager").getMethod("queryBroadcastReceiversAsUser", new Class[]{Intent.class, Integer.TYPE, Integer.TYPE}).invoke(packageManager, new Object[]{intent, Integer.valueOf(i), Integer.valueOf(i2)});
        } catch (ClassNotFoundException e) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "queryBroadcastReceiversAsUser ClassNotFoundException");
            return null;
        } catch (NoSuchMethodException e2) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "queryBroadcastReceiversAsUser NoSuchMethodException");
            return null;
        } catch (IllegalArgumentException e3) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "queryBroadcastReceiversAsUser IllegalArgumentException");
            return null;
        } catch (IllegalAccessException e4) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "queryBroadcastReceiversAsUser IllegalAccessException");
            return null;
        } catch (InvocationTargetException e5) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "queryBroadcastReceiversAsUser InvocationTargetException");
            return null;
        }
    }

    public static String tj(Throwable th) {
        if (th == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Exception: ").append(th.getClass().getName()).append(10);
        StackTraceElement[] stackTrace = th.getStackTrace();
        if (stackTrace == null) {
            return "";
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuilder.append(stackTraceElement.toString()).append(10);
        }
        return stringBuilder.toString();
    }
}
