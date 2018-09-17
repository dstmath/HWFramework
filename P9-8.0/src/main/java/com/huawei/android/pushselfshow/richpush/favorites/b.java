package com.huawei.android.pushselfshow.richpush.favorites;

import com.huawei.android.pushagent.a.a.c;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

class b implements Runnable {
    final /* synthetic */ FavoritesActivity a;

    b(FavoritesActivity favoritesActivity) {
        this.a = favoritesActivity;
    }

    public void run() {
        c.a("PushSelfShowLog", "onCreate mThread run");
        this.a.h.b();
        this.a.a.sendEmptyMessage(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
    }
}
