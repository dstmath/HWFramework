package com.huawei.android.pushselfshow.richpush.html;

import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;
import com.huawei.android.pushagent.a.a.c;

class a implements DownloadListener {
    final /* synthetic */ HtmlViewer a;

    a(HtmlViewer htmlViewer) {
        this.a = htmlViewer;
    }

    public void onDownloadStart(String str, String str2, String str3, String str4, long j) {
        try {
            c.a("PushSelfShowLog", "url=" + str);
            c.a("PushSelfShowLog", "userAgent=" + str2);
            c.a("PushSelfShowLog", "contentDisposition=" + str3);
            c.a("PushSelfShowLog", "mimetype=" + str4);
            c.a("PushSelfShowLog", "contentLength=" + j);
            this.a.d.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
        } catch (Throwable e) {
            c.a("PushSelfShowLog", "onDownloadStart err", e);
        }
    }
}
