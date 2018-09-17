package com.huawei.android.pushagent.utils.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.f;
import java.util.List;

public class b {
    public static long ql(Context context, String str) {
        long j = -1000;
        long qm;
        try {
            List ts = com.huawei.android.pushagent.utils.b.ts(context.getPackageManager(), new Intent("com.huawei.android.push.intent.REGISTER").setPackage(str), 787072, f.vb());
            if (ts == null || ts.size() == 0) {
                return -1000;
            }
            j = 228;
            String str2 = ((ResolveInfo) ts.get(0)).serviceInfo != null ? ((ResolveInfo) ts.get(0)).serviceInfo.packageName : ((ResolveInfo) ts.get(0)).activityInfo.packageName;
            qm = str2 != null ? str2.equals(str) ? qm((ResolveInfo) ts.get(0), "CS_cloud_version") : 228 : 228;
            c.sg("PushLog2951", str + " version is :" + qm);
            return qm;
        } catch (Exception e) {
            c.sf("PushLog2951", "get Apk version faild ,Exception e= " + e.toString());
            qm = j;
        }
    }

    private static long qm(ResolveInfo resolveInfo, String str) {
        long j = -1;
        if (resolveInfo == null) {
            return j;
        }
        try {
            j = Long.parseLong(com.huawei.android.pushagent.utils.b.tt(resolveInfo, str));
        } catch (NumberFormatException e) {
            c.sg("PushLog2951", str + " is not set in " + qn(resolveInfo));
        }
        return j;
    }

    private static String qn(ResolveInfo resolveInfo) {
        if (resolveInfo == null) {
            c.sh("PushLog2951", "ResolveInfo is null , cannot get packageName");
            return null;
        }
        return resolveInfo.serviceInfo != null ? resolveInfo.serviceInfo.packageName : resolveInfo.activityInfo.packageName;
    }
}
