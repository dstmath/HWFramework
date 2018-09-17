package com.huawei.android.pushselfshow.richpush.html;

import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.huawei.android.pushagent.a.a.c;

class d extends WebChromeClient {
    final /* synthetic */ HtmlViewer a;

    d(HtmlViewer htmlViewer) {
        this.a = htmlViewer;
    }

    public void onProgressChanged(WebView webView, int i) {
        if (this.a.e != null) {
            if (this.a.e.canGoBack()) {
                if (this.a.j != null) {
                    this.a.j.setEnabled(true);
                }
            } else if (this.a.j != null) {
                this.a.j.setEnabled(false);
            }
            if (this.a.e.canGoForward()) {
                if (this.a.k != null) {
                    this.a.k.setEnabled(true);
                }
            } else if (this.a.k != null) {
                this.a.k.setEnabled(false);
            }
        }
        if (i < 5) {
            i = 5;
        }
        this.a.setProgress(i);
        super.onProgressChanged(webView, i);
    }

    public void onReceivedTitle(WebView webView, String str) {
        super.onReceivedTitle(webView, str);
        c.a("PushSelfShowLog", "onReceivedTitle:" + str);
        if (!TextUtils.isEmpty(str)) {
            this.a.a(str);
        }
    }
}
