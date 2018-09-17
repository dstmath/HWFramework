package com.huawei.android.pushselfshow.richpush.html;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.android.pushagent.a.a.e;

class b implements OnClickListener {
    final /* synthetic */ e a;
    final /* synthetic */ HtmlViewer b;

    b(HtmlViewer htmlViewer, e eVar) {
        this.b = htmlViewer;
        this.a = eVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.a("isFirstCollect", true);
        this.b.a(this.b.d);
    }
}
