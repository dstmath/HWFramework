package defpackage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import java.util.List;

/* renamed from: bu */
public class bu {
    private static ResolveInfo a(List list, String str) {
        ResolveInfo resolveInfo = null;
        if (!(list == null || list.size() == 0)) {
            for (ResolveInfo resolveInfo2 : list) {
                ResolveInfo resolveInfo22;
                if (!bu.a(resolveInfo22, resolveInfo, str)) {
                    resolveInfo22 = resolveInfo;
                }
                resolveInfo = resolveInfo22;
            }
            if (resolveInfo != null) {
                aw.d("PushLog2828", "after getHighVersion pushService pkgName=" + (resolveInfo.serviceInfo != null ? resolveInfo.serviceInfo.packageName : resolveInfo.activityInfo.packageName));
            }
        }
        return resolveInfo;
    }

    private static String a(ResolveInfo resolveInfo) {
        if (resolveInfo != null) {
            return resolveInfo.serviceInfo != null ? resolveInfo.serviceInfo.packageName : resolveInfo.activityInfo.packageName;
        } else {
            aw.i("PushLog2828", "ResolveInfo is null , cannot get packageName");
            return null;
        }
    }

    private static boolean a(ResolveInfo resolveInfo, ResolveInfo resolveInfo2, String str) {
        if (resolveInfo2 == null) {
            return true;
        }
        if (resolveInfo == null) {
            return false;
        }
        String a = bu.a(resolveInfo);
        long b = bu.b(resolveInfo, str);
        long b2 = bu.b(resolveInfo2, str);
        String str2 = resolveInfo2.serviceInfo != null ? resolveInfo2.serviceInfo.packageName : resolveInfo2.activityInfo.packageName;
        aw.d("PushLog2828", "the curPkgName(" + a + ")version is:" + b + "the oldPkgName (" + str2 + ")version is:" + b2);
        if (b > b2) {
            return true;
        }
        if (b != b2) {
            return false;
        }
        boolean z = a != null && a.compareTo(str2) > 0;
        return z;
    }

    public static String ae(Context context) {
        aw.d("PushLog2828", " choose the high version for push service");
        long x = bu.x(context, "com.huawei.android.pushagent");
        if (228 > x || !bu.co()) {
            ResolveInfo n = bu.n(context, "com.huawei.android.push.intent.REGISTER", "CS_cloud_version");
            long b = bu.b(n, "CS_cloud_version");
            aw.d("PushLog2828", "the getHightMetaPackageName return version is :" + b + " curApkVersion:" + x);
            String str;
            if (x >= b) {
                str = "com.huawei.android.pushagent";
                aw.d("PushLog2828", "the push APK version (" + x + ")is hight,use APK for push service");
                return str;
            }
            str = bu.a(n);
            aw.d("PushLog2828", "use the hight version(" + b + " )for push service, highPkgName is :" + str);
            return str;
        }
        aw.i("PushLog2828", "support ctrlsocket.");
        return "com.huawei.android.pushagent";
    }

    private static long b(ResolveInfo resolveInfo, String str) {
        long j = -1;
        if (resolveInfo != null) {
            try {
                j = Long.parseLong(au.a(resolveInfo, str));
            } catch (NumberFormatException e) {
                aw.d("PushLog2828", str + " is not set in " + bu.a(resolveInfo));
            }
        }
        return j;
    }

    private static boolean co() {
        if (-2 != au.ctrlSockets(1, 3) && !bv.cs()) {
            return true;
        }
        aw.d("PushLog2828", "not support ctrlsocket.");
        return false;
    }

    private static ResolveInfo n(Context context, String str, String str2) {
        List m = au.m(context, str);
        if (m == null || m.size() == 0) {
            aw.e("PushLog2828", "no push service install, may be system Err!! pkgName:" + context.getPackageName());
            return null;
        }
        aw.d("PushLog2828", "begin to get the hight Version package, have action:" + str);
        ResolveInfo a = bu.a(m, str2);
        if (a != null) {
            return a;
        }
        aw.e("PushLog2828", "there is no hightVersion PushService, maybe system Err!! pkgName:" + context.getPackageName());
        return null;
    }

    public static long x(Context context, String str) {
        Exception exception;
        long j = -1000;
        try {
            List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(new Intent("com.huawei.android.push.intent.REGISTER").setPackage(str), 640, ActivityManager.getCurrentUser());
            if (!(queryBroadcastReceivers == null || queryBroadcastReceivers.size() == 0)) {
                try {
                    String str2 = ((ResolveInfo) queryBroadcastReceivers.get(0)).serviceInfo != null ? ((ResolveInfo) queryBroadcastReceivers.get(0)).serviceInfo.packageName : ((ResolveInfo) queryBroadcastReceivers.get(0)).activityInfo.packageName;
                    if (str2 == null || !str2.equals(str)) {
                        j = 228;
                        aw.d("PushLog2828", str + " version is :" + j);
                    } else {
                        j = bu.b((ResolveInfo) queryBroadcastReceivers.get(0), "CS_cloud_version");
                        aw.d("PushLog2828", str + " version is :" + j);
                    }
                } catch (Exception e) {
                    Exception exception2 = e;
                    j = 228;
                    exception = exception2;
                    aw.e("PushLog2828", "get Apk version faild ,Exception e= " + exception.toString());
                    aw.d("PushLog2828", str + " version is :" + j);
                    return j;
                }
            }
        } catch (Exception e2) {
            exception = e2;
            aw.e("PushLog2828", "get Apk version faild ,Exception e= " + exception.toString());
            aw.d("PushLog2828", str + " version is :" + j);
            return j;
        }
        return j;
    }
}
