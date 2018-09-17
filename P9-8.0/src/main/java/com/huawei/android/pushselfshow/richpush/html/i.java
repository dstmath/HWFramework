package com.huawei.android.pushselfshow.richpush.html;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import com.huawei.android.pushagent.a.a.c;

class i implements OnDismissListener {
    final /* synthetic */ Activity a;
    final /* synthetic */ HtmlViewer b;

    i(HtmlViewer htmlViewer, Activity activity) {
        this.b = htmlViewer;
        this.a = activity;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        c.a("PushSelfShowLog", "DialogInterface onDismiss,mClickDialogOkBtn:" + this.b.o);
        if (this.b.o) {
            this.b.d(this.a);
            return;
        }
        this.b.l.setEnabled(true);
        this.b.r = false;
    }
}
