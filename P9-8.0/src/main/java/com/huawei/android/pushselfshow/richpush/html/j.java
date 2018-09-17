package com.huawei.android.pushselfshow.richpush.html;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

class j implements OnClickListener {
    final /* synthetic */ HtmlViewer a;

    j(HtmlViewer htmlViewer) {
        this.a = htmlViewer;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.o = true;
    }
}
