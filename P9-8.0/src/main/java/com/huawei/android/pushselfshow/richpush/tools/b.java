package com.huawei.android.pushselfshow.richpush.tools;

public class b {
    public static String a(String str) {
        return ("application/zip".equals(str) || "application/zip_local".equals(str)) ? ".zip" : !"text/html".equals(str) ? !"image/jpeg".equals(str) ? ".unknow" : ".jpg" : ".html";
    }
}
