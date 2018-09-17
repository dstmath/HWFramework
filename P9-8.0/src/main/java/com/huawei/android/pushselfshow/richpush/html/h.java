package com.huawei.android.pushselfshow.richpush.html;

import android.app.Activity;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

class h implements Runnable {
    final /* synthetic */ Activity a;
    final /* synthetic */ HtmlViewer b;

    h(HtmlViewer htmlViewer, Activity activity) {
        this.b = htmlViewer;
        this.a = activity;
    }

    public void run() {
        if (this.b.b(this.a) >= CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
            this.b.b.sendEmptyMessage(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
        } else {
            this.b.f.b();
        }
    }
}
