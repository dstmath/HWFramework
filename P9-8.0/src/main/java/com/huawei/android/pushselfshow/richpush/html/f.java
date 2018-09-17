package com.huawei.android.pushselfshow.richpush.html;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

class f implements Runnable {
    final /* synthetic */ HtmlViewer a;

    f(HtmlViewer htmlViewer) {
        this.a = htmlViewer;
    }

    public void run() {
        if (this.a.b(this.a.d) >= CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
            this.a.b.sendEmptyMessage(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
        } else {
            this.a.f.b();
        }
    }
}
