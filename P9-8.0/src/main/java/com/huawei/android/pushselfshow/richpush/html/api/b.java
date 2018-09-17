package com.huawei.android.pushselfshow.richpush.html.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.huawei.android.pushagent.a.a.c;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class b {
    public static String a(String str, String str2) {
        if (a(str2)) {
            return str2;
        }
        if (str != null) {
            String str3 = str + File.separator + str2;
            c.e("PushSelfShowLog", "the audio path is " + str3);
            if (str3.startsWith("file://")) {
                str3 = str3.substring(7);
            }
            if (new File(str3).exists()) {
                return str3;
            }
        }
        return null;
    }

    public static ArrayList a(Context context, Intent intent) {
        ArrayList arrayList = new ArrayList();
        List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!(queryIntentActivities == null || queryIntentActivities.size() == 0)) {
            int size = queryIntentActivities.size();
            for (int i = 0; i < size; i++) {
                if (((ResolveInfo) queryIntentActivities.get(i)).activityInfo != null) {
                    c.a("PushSelfShowLog", "getSupportPackage:" + ((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.packageName);
                    arrayList.add(((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.packageName);
                }
            }
        }
        return arrayList;
    }

    public static boolean a(String str) {
        return str.contains("http://") || str.contains("https://");
    }
}
