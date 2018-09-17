package com.huawei.android.pushselfshow.richpush.html;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class c implements OnTouchListener {
    final /* synthetic */ HtmlViewer a;

    c(HtmlViewer htmlViewer) {
        this.a = htmlViewer;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this.a.e != null && this.a.e.requestFocus()) {
            com.huawei.android.pushagent.a.a.c.e("PushSelfShowLog", "webView.requestFocus");
        }
        return false;
    }
}
