package com.huawei.android.pushselfshow.richpush.tools;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.b.a;
import com.huawei.android.pushselfshow.utils.b.b;
import java.io.File;

public class d {
    public static String a(Context context, String str) {
        c.a("PushSelfShowLog", "download richpush file successed ,try to unzip file,file path is " + str);
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.startsWith(b.b(context))) {
            String str2 = "";
            try {
                str2 = str.substring(0, str.lastIndexOf(File.separator));
                new a(str, str2 + File.separator).a();
                File file = new File(str2 + "/" + "index.html");
                if (file.exists()) {
                    c.a("PushSelfShowLog", "unzip success ,so delete src zip file");
                    File file2 = new File(str);
                    if (file2.exists()) {
                        com.huawei.android.pushselfshow.utils.a.a(file2);
                    }
                    return file.getAbsolutePath();
                }
                c.a("PushSelfShowLog", "unzip fail ,don't exist index.html");
                com.huawei.android.pushselfshow.utils.a.a(new File(str2));
                return null;
            } catch (IndexOutOfBoundsException e) {
                c.d("PushSelfShowLog", e.toString());
                return "";
            }
        }
        c.a("PushSelfShowLog", "localfile dose not startsWith PushService directory");
        return "";
    }

    public String a(Context context, String str, int i, String str2) {
        String str3 = null;
        try {
            str3 = new b().a(context, str, str2);
            if (str3 != null && str3.length() > 0) {
                return str3;
            }
            c.a("PushSelfShowLog", "download failed");
            if (i <= 0) {
                i = 1;
            }
            int i2 = i - 1;
            return (i2 > 0 && a(context, str, i2, str2) != null) ? str3 : null;
        } catch (Exception e) {
            c.a("PushSelfShowLog", "download err" + e.toString());
        }
    }
}
