package defpackage;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.INotificationManager.Stub;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.bd.Reporter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/* renamed from: au */
public class au {
    private static final char[] bK = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: au.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: au.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: au.<clinit>():void");
    }

    public static String E(Context context) {
        Object V = au.V(context);
        if (!TextUtils.isEmpty(V)) {
            return V;
        }
        String T = au.T(context);
        az.a(context, "device_info", "deviceId", T);
        return T;
    }

    public static String F(Context context) {
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

    public static int G(Context context) {
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

    public static String H(Context context) {
        return au.E(context) + "0000000000000000";
    }

    public static String I(Context context) {
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
                aw.d("PushLog2828", "push apn is " + extraInfo);
                return extraInfo;
            }
        }
        extraInfo = str;
        if (extraInfo == null) {
            extraInfo = "";
        }
        aw.d("PushLog2828", "push apn is " + extraInfo);
        return extraInfo;
    }

    private static void J(Context context) {
        try {
            ae.l(context).t.clear();
            ao.z(context).bv.clear();
            ai.q(context).bA();
        } catch (Exception e) {
            aw.e("PushLog2828", "clearMemory failed!");
        }
    }

    private static void K(Context context) {
        try {
            au.n(context, "PushConnectControl");
            au.n(context, "PushRouteInfo");
            au.n(context, "RouteInfo");
            au.n(context, "HeartBeatCfg");
            for (String n : ChannelMgr.g(context).aS()) {
                au.n(context, n);
            }
            au.n(context, "socket_info");
            au.n(context, "update_remind");
        } catch (Exception e) {
            aw.d("PushLog2828", "deleteSameFiles failed!");
        }
    }

    private static void L(Context context) {
        au.n(context, "PushAppNotifiCfg");
        au.n(context, "pclient_request_info");
        au.n(context, "pclient_unRegist_info_v2");
        au.n(context, "pclient_info_v2");
        au.n(context, "pclient_info");
        au.n(context, "pclient_info_encrypt");
    }

    private static void M(Context context) {
        try {
            bt btVar = new bt(context, "pclient_info_v2");
            for (String str : btVar.getAll().keySet()) {
                if (!au.p(context, au.o(str))) {
                    btVar.z(str);
                }
            }
        } catch (Exception e) {
            aw.d("PushLog2828", "removeNotExistPackage failed!");
        }
    }

    private static boolean N(Context context) {
        CharSequence V = au.V(context);
        aw.d("PushLog2828", "imei from localfile is " + bi.w(V));
        CharSequence U = au.U(context);
        aw.d("PushLog2828", "deviceId from device is " + bi.w(U));
        CharSequence R = au.R(context);
        Object S = au.S(context);
        aw.d("PushLog2828", "mac from localfile is " + bi.w(R));
        aw.d("PushLog2828", "mac from device is " + bi.w(S));
        if (TextUtils.isEmpty(V) || TextUtils.isEmpty(U)) {
            if (TextUtils.isEmpty(V)) {
                az.a(context, "device_info", "deviceId", au.T(context));
            }
            if (TextUtils.isEmpty(R) || TextUtils.isEmpty(S)) {
                if (!TextUtils.isEmpty(R) || TextUtils.isEmpty(S)) {
                    return false;
                }
                az.a(context, "device_info", "macAddress", S);
                return false;
            } else if (R.equals(S)) {
                return false;
            } else {
                aw.w("PushLog2828", "After check mac, it is cloned, need reset files");
                return true;
            }
        } else if (V.equals(U)) {
            return false;
        } else {
            aw.w("PushLog2828", "After check imei, it is cloned, need reset files");
            return true;
        }
    }

    public static void O(Context context) {
        if (au.N(context)) {
            new Thread(new av(context)).start();
        }
    }

    private static void P(Context context) {
        bt btVar = new bt(context, "pclient_request_info");
        bt btVar2 = new bt(context, "pclient_info_v2");
        for (String str : btVar2.getAll().keySet()) {
            if (!TextUtils.isEmpty(str)) {
                btVar.f(str, "true");
                aw.d("PushLog2828", "pkg : " + str + " need register again");
            }
        }
        btVar2.clear();
    }

    private static void Q(Context context) {
        aw.d("PushLog2828", "update deviceInfo File");
        new bt(context, "device_info").clear();
        String T = au.T(context);
        String S = au.S(context);
        az.a(context, "device_info", "deviceId", T);
        az.a(context, "device_info", "macAddress", S);
    }

    private static String R(Context context) {
        aw.d("PushLog2828", "enter getMacFromLocalFile()");
        Object k = az.k(context, "device_info", "macAddress");
        if (TextUtils.isEmpty(k)) {
            aw.d("PushLog2828", "no macAddress in device_info");
        } else {
            aw.d("PushLog2828", "get macAddress from LocalFile success");
        }
        return k;
    }

    private static String S(Context context) {
        Throwable e;
        aw.d("PushLog2828", "enter getMacAddress()");
        String str = "";
        String macAddress;
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            WifiInfo connectionInfo = wifiManager == null ? null : wifiManager.getConnectionInfo();
            if (connectionInfo == null) {
                aw.w("PushLog2828", "info = null");
                return str;
            }
            macAddress = connectionInfo.getMacAddress();
            try {
                if (TextUtils.isEmpty(macAddress)) {
                    aw.d("PushLog2828", "Mac is empty");
                    return macAddress;
                }
                aw.d("PushLog2828", "get Mac from device success");
                return macAddress;
            } catch (Exception e2) {
                e = e2;
                aw.d("PushLog2828", "getLocalMacAddress() exception, e=" + e.toString(), e);
                return macAddress;
            }
        } catch (Throwable e3) {
            Throwable th = e3;
            macAddress = str;
            e = th;
            aw.d("PushLog2828", "getLocalMacAddress() exception, e=" + e.toString(), e);
            return macAddress;
        }
    }

    private static String T(Context context) {
        Object U = au.U(context);
        return TextUtils.isEmpty(U) ? au.getUUID() : U;
    }

    public static String U(Context context) {
        String deviceId;
        String str = "";
        boolean isMultiSimEnabled = ae.l(context).isMultiSimEnabled();
        aw.d("PushLog2828", "isMultiSimEnabledFromServer:" + isMultiSimEnabled);
        if (bn.isMultiSimEnabled() && isMultiSimEnabled) {
            aw.d("PushLog2828", "multicard device");
            deviceId = bn.cf().getDeviceId(0);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            deviceId = telephonyManager != null ? telephonyManager.getDeviceId() : str;
        }
        if (TextUtils.isEmpty(deviceId) || deviceId.matches("[0]+")) {
            aw.i("PushLog2828", "get uniqueId from device is empty or all 0");
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        if (deviceId.length() >= 16) {
            return deviceId.substring(deviceId.length() - 16);
        }
        stringBuffer.append("0").append(deviceId);
        if (stringBuffer.length() < 16) {
            StringBuffer stringBuffer2 = new StringBuffer();
            for (int i = 0; i < 16 - stringBuffer.length(); i++) {
                stringBuffer2.append("0");
            }
            stringBuffer.append(stringBuffer2);
        }
        return stringBuffer.toString();
    }

    private static String V(Context context) {
        aw.d("PushLog2828", "enter getImeiFromLocalFile()");
        String k = az.k(context, "device_info", "deviceId");
        if (TextUtils.isEmpty(k) || 16 != k.length()) {
            aw.d("PushLog2828", "no deviceId in device_info");
        } else {
            aw.d("PushLog2828", "get imei from localFile success");
        }
        return k;
    }

    public static String a(long j, String str) {
        String str2 = "";
        try {
            str2 = new SimpleDateFormat(str).format(new Date(j));
        } catch (Exception e) {
            aw.e("PushLog2828", "getTimeString,milliseconds:" + j + " e:" + e.toString());
        }
        return str2;
    }

    public static String a(ResolveInfo resolveInfo, String str) {
        Bundle bundle = resolveInfo.serviceInfo != null ? resolveInfo.serviceInfo.metaData : resolveInfo.activityInfo.metaData;
        return bundle == null ? null : bundle.getString(str);
    }

    public static void a(Context context, int i, String str) {
        aw.i("PushLog2828", "enter bdReport, eventID is " + i + ",content is " + str);
        if (context != null && !TextUtils.isEmpty(str)) {
            Reporter.e(context, i, str);
        }
    }

    public static void a(Context context, long j) {
        aw.d("PushLog2828", "enter wakeSystem");
        ((PowerManager) context.getSystemService("power")).newWakeLock(1, "dispatcher").acquire(j);
    }

    private static ResolveInfo b(Context context, Intent intent, int i) {
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(intent, 640, i);
        return (queryBroadcastReceivers == null || queryBroadcastReceivers.size() <= 0) ? null : (ResolveInfo) queryBroadcastReceivers.get(0);
    }

    public static String b(String str, String str2) {
        return TextUtils.isEmpty(str) ? "" : (TextUtils.isEmpty(str2) || str2.matches("[0]+")) ? str : str + "/" + str2;
    }

    public static void b(Context context, int i) {
        switch (i) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                au.K(context);
                au.M(context);
                ag.n(context).init();
                ag.n(context).bv();
                ae.m(context);
            case Reporter.ACTIVITY_RESUME /*2*/:
                au.J(context);
                au.K(context);
                au.L(context);
                ag.n(context).init();
                ag.n(context).bv();
                ae.m(context);
            case Reporter.ACTIVITY_PAUSE /*3*/:
                au.K(context);
                au.L(context);
                ag.n(context).init();
                ag.n(context).bv();
            default:
        }
    }

    public static boolean b(Context context, String str, int i) {
        boolean z = false;
        if (!TextUtils.isEmpty(str)) {
            if (au.b(context, new Intent("com.huawei.android.push.intent.REGISTRATION").setPackage(str), i) != null) {
                z = true;
            }
            aw.d("PushLog2828", "isPushClient:" + z);
        }
        return z;
    }

    public static void bI() {
        aw.d("PushLog2828", "enter powerLow");
        try {
            Class.forName("com.huawei.pgmng.log.LogPower").getMethod("push", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(119)});
        } catch (ClassNotFoundException e) {
            aw.d("PushLog2828", "ClassNotFoundException, not support LogPower");
        } catch (NoSuchMethodException e2) {
            aw.d("PushLog2828", "NoSuchMethodException, not support LogPower");
        } catch (IllegalArgumentException e3) {
            aw.d("PushLog2828", "IllegalArgumentException, not support LogPower");
        } catch (IllegalAccessException e4) {
            aw.d("PushLog2828", "IllegalAccessException, not support LogPower");
        } catch (InvocationTargetException e5) {
            aw.d("PushLog2828", "InvocationTargetException, not support LogPower");
        }
    }

    public static String bJ() {
        String str = "";
        Class[] clsArr = new Class[]{String.class};
        Object[] objArr = new Object[]{"ro.build.version.emui"};
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            String str2 = (String) cls.getDeclaredMethod("get", clsArr).invoke(cls, objArr);
            aw.d("PushLog2828", "get EMUI version is:" + str2);
            if (!TextUtils.isEmpty(str2)) {
                return str2;
            }
        } catch (ClassNotFoundException e) {
            aw.e("PushLog2828", " getEmuiVersion wrong, ClassNotFoundException");
        } catch (LinkageError e2) {
            aw.e("PushLog2828", " getEmuiVersion wrong, LinkageError");
        } catch (NoSuchMethodException e3) {
            aw.e("PushLog2828", " getEmuiVersion wrong, NoSuchMethodException");
        } catch (NullPointerException e4) {
            aw.e("PushLog2828", " getEmuiVersion wrong, NullPointerException");
        } catch (Exception e5) {
            aw.e("PushLog2828", " getEmuiVersion wrong");
        }
        return str;
    }

    public static final String bK() {
        return bh.getMD5str(String.valueOf("com.huawei.android.pushagent".hashCode()));
    }

    public static int byteArrayToInt(byte[] bArr) {
        return ((((bArr[0] << 24) & 255) | ((bArr[1] << 16) & 255)) | ((bArr[2] << 8) & 255)) | (bArr[3] & 255);
    }

    public static int c(Socket socket) {
        int intValue;
        try {
            intValue = ((Integer) FileDescriptor.class.getMethod("getInt$", new Class[0]).invoke((FileDescriptor) Socket.class.getMethod("getFileDescriptor$", new Class[0]).invoke(socket, new Object[0]), new Object[0])).intValue();
            try {
                aw.d("PushLog2828", "socket fd is " + intValue);
            } catch (NoSuchMethodException e) {
                aw.d("PushLog2828", "There is no method of ctrlSockets.");
                return intValue;
            } catch (IllegalArgumentException e2) {
                aw.d("PushLog2828", "There is no method of ctrlSockets.");
                return intValue;
            } catch (IllegalAccessException e3) {
                aw.d("PushLog2828", "There is no method of ctrlSockets.");
                return intValue;
            } catch (InvocationTargetException e4) {
                aw.d("PushLog2828", "There is no method of ctrlSockets.");
                return intValue;
            }
        } catch (NoSuchMethodException e5) {
            intValue = 0;
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return intValue;
        } catch (IllegalArgumentException e6) {
            intValue = 0;
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return intValue;
        } catch (IllegalAccessException e7) {
            intValue = 0;
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return intValue;
        } catch (InvocationTargetException e8) {
            intValue = 0;
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return intValue;
        }
        return intValue;
    }

    public static boolean c(Context context, String str, int i) {
        if (context == null || str == null || "".equals(str)) {
            return false;
        }
        try {
            PackageInfo packageInfoAsUser = context.getPackageManager().getPackageInfoAsUser(str, 0, i);
            if (packageInfoAsUser == null) {
                aw.e("PushLog2828", "query package isInstalled failed! packageInfo is null");
                return false;
            } else if (!str.equals(packageInfoAsUser.packageName)) {
                return false;
            } else {
                aw.d("PushLog2828", str + " is installed in user " + i);
                return true;
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", "query package isInstalled failed!", e);
            return false;
        } catch (Throwable e2) {
            aw.d("PushLog2828", "query package isInstalled failed!", e2);
            return false;
        }
    }

    public static byte[] c(int i) {
        return new byte[]{(byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static int ctrlSockets(int i, int i2) {
        aw.i("PushLog2828", "enter ctrlSockets(cmd:" + i + " param:" + i2 + ")");
        try {
            return ((Integer) Class.forName("dalvik.system.Zygote").getMethod("ctrlSockets", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)})).intValue();
        } catch (ClassNotFoundException e) {
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return -2;
        } catch (NoSuchMethodException e2) {
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return -2;
        } catch (IllegalArgumentException e3) {
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return -2;
        } catch (IllegalAccessException e4) {
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return -2;
        } catch (InvocationTargetException e5) {
            aw.d("PushLog2828", "There is no method of ctrlSockets.");
            return -2;
        }
    }

    public static String e(byte b) {
        return new String(new char[]{bK[(b & 240) >> 4], bK[b & 15]});
    }

    public static String f(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        if (bArr.length == 0) {
            return "";
        }
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            cArr[i * 2] = bK[(b & 240) >> 4];
            cArr[(i * 2) + 1] = bK[b & 15];
        }
        return new String(cArr);
    }

    public static int g(byte[] bArr) {
        return ((bArr[0] & 255) << 8) | (bArr[1] & 255);
    }

    public static String getUUID() {
        String replace = UUID.randomUUID().toString().replace("-", "");
        return replace.length() > 15 ? "_" + replace.substring(0, 15) : "_" + "000000000000000".substring(15 - replace.length()) + replace;
    }

    public static String getVersion(Context context) {
        return String.valueOf(2828);
    }

    public static long h(byte[] bArr) {
        return (((((((0 | ((((long) bArr[0]) & 255) << 56)) | ((((long) bArr[1]) & 255) << 48)) | ((((long) bArr[2]) & 255) << 40)) | ((((long) bArr[3]) & 255) << 32)) | ((((long) bArr[4]) & 255) << 24)) | ((((long) bArr[5]) & 255) << 16)) | ((((long) bArr[6]) & 255) << 8)) | (((long) bArr[7]) & 255);
    }

    private static String i(Context context, String str, String str2) {
        String str3 = null;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 640);
            if (applicationInfo == null || applicationInfo.metaData == null) {
                aw.d("PushLog2828", "could not read Applicationinfo from AndroidManifest.xml.");
                return str3;
            }
            Object obj = applicationInfo.metaData.get(str2);
            if (obj == null) {
                aw.i("PushLog2828", "could not read meta-data from AndroidManifest.xml.");
            } else {
                str3 = String.valueOf(obj);
                aw.d("PushLog2828", "read meta-data from AndroidManifest.xml,name is:" + str2 + ",value is:" + str3);
            }
            return str3;
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        }
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static String j(Context context, String str, String str2) {
        if (context == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return "";
        }
        String b = au.b(str, au.q(str2));
        Object decrypter = bj.decrypter(new bt(context, "push_notify_key").getString(b));
        if (!TextUtils.isEmpty(decrypter)) {
            return decrypter;
        }
        String mD5str = bh.getMD5str(String.valueOf((str2 + System.currentTimeMillis()).hashCode()));
        new bt(context, "push_notify_key").f(b, bj.encrypter(mD5str));
        return mD5str;
    }

    public static boolean l(Context context, String str) {
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(new Intent("com.huawei.android.push.intent.RECEIVE").setPackage(str), 640, ActivityManager.getCurrentUser());
        if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() == 0) {
            aw.d("PushLog2828", "isClientSupportMsgResponse: false");
            return false;
        }
        String str2 = ((ResolveInfo) queryBroadcastReceivers.get(0)).serviceInfo != null ? ((ResolveInfo) queryBroadcastReceivers.get(0)).serviceInfo.packageName : ((ResolveInfo) queryBroadcastReceivers.get(0)).activityInfo.packageName;
        if (str2 != null && str2.equals(str)) {
            Object a = au.a((ResolveInfo) queryBroadcastReceivers.get(0), "CS_cloud_ablitity");
            if (TextUtils.isEmpty(a)) {
                return false;
            }
            try {
                boolean contains = Arrays.asList(a.split("\\|")).contains("successRateAnalytics");
                aw.d("PushLog2828", "isClientSupportMsgResponse:" + contains);
                return contains;
            } catch (Exception e) {
                aw.e("PushLog2828", e.toString());
            }
        }
        return false;
    }

    public static List m(Context context, String str) {
        return context.getPackageManager().queryBroadcastReceivers(new Intent(str), 640);
    }

    public static byte[] m(long j) {
        byte[] bArr = new byte[8];
        for (int length = bArr.length - 1; length >= 0; length--) {
            bArr[length] = (byte) ((int) j);
            j >>= 8;
        }
        return bArr;
    }

    public static byte[] m(String str) {
        byte[] bArr = new byte[(str.length() / 2)];
        try {
            byte[] bytes = str.getBytes("UTF-8");
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = (byte) (((byte) (Byte.decode("0x" + new String(new byte[]{bytes[i * 2]}, "UTF-8")).byteValue() << 4)) ^ Byte.decode("0x" + new String(new byte[]{bytes[(i * 2) + 1]}, "UTF-8")).byteValue());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bArr;
    }

    public static long n(String str) {
        long j = -1;
        try {
            Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").parse(str);
            if (parse != null) {
                j = parse.getTime();
            }
        } catch (ParseException e) {
            aw.e("PushLog2828", "ParseException,timeStr:" + str + " e:" + e.toString());
        }
        return j;
    }

    public static boolean n(Context context, String str) {
        String str2 = "/data/misc/hwpush" + File.separator + str + ".xml";
        File file = new File(str2);
        if (file.exists() && file.isFile() && file.canWrite()) {
            aw.d("PushLog2828", "enter deletePrefrence(fileName:" + str + ".xml)");
            return file.delete();
        }
        if (file.exists()) {
            aw.e("PushLog2828", "delete File:" + str2 + " failed!!");
        }
        return false;
    }

    public static String o(Context context, String str) {
        String i = au.i(context, str, "CHANNEL");
        if (i == null) {
            return str;
        }
        str = str + "#" + i;
        aw.d("PushLog2828", "after add downloadChannel, the new packageName is:" + str);
        return str;
    }

    public static String o(String str) {
        return TextUtils.isEmpty(str) ? "" : str.split("/")[0];
    }

    public static int p(String str) {
        int i = 0;
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("/");
            if (split.length > 1) {
                try {
                    i = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    aw.e("PushLog2828", "parse int error:" + split[1]);
                }
            }
        }
        return i;
    }

    public static boolean p(Context context, String str) {
        return au.c(context, str, ActivityManager.getCurrentUser());
    }

    public static String q(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.length() >= 32) {
            return str.substring(26, 28);
        }
        aw.e("PushLog2828", "token length is less than 32");
        return "";
    }

    public static boolean q(Context context, String str) {
        if (context == null) {
            aw.i("PushLog2828", "enter getNotificationsBanned, context is null");
            return false;
        } else if (TextUtils.isEmpty(str)) {
            aw.i("PushLog2828", "enter getNotificationsBanned, pkg is empty");
            return false;
        } else {
            try {
                context.getPackageManager();
                int currentUser = ActivityManager.getCurrentUser();
                ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(str, 0, currentUser);
                if (applicationInfo == null) {
                    aw.i("PushLog2828", "enter getNotificationsBanned, ApplicationInfo is empty");
                    return false;
                }
                aw.i("PushLog2828", str + "  in user " + currentUser + " ,uid is " + applicationInfo.uid);
                boolean areNotificationsEnabledForPackage = Stub.asInterface(ServiceManager.getService("notification")).areNotificationsEnabledForPackage(str, applicationInfo.uid);
                aw.i("PushLog2828", "areNotificationsEnabledForPackage:" + areNotificationsEnabledForPackage);
                return areNotificationsEnabledForPackage;
            } catch (Throwable e) {
                aw.c("PushLog2828", "Error calling NoMan", e);
                return false;
            }
        }
    }

    public static boolean r(Context context, String str) {
        boolean z = false;
        if (context != null) {
            Object i = au.i(context, str, "NOTIFICATION_CENTER");
            if (!TextUtils.isEmpty(i)) {
                z = Boolean.parseBoolean(i);
            }
            aw.i("PushLog2828", "isSupportNotificationCenter:" + z);
        }
        return z;
    }
}
