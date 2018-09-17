package com.huawei.android.pushselfshow.utils.b;

class c implements Runnable {
    final /* synthetic */ b a;

    c(b bVar) {
        this.a = bVar;
    }

    public void run() {
        try {
            if (this.a.b != null) {
                if (this.a.c != null) {
                    String a = this.a.a(this.a.b, this.a.c, this.a.d);
                    com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "getDownloadFileWithHandler success, and localfile =  " + a);
                    if (a != null) {
                        this.a.a(a);
                        return;
                    }
                }
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushSelfShowLog", "getDownloadFileWithHandler failed ", e);
        }
        this.a.c();
    }
}
