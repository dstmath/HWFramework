package com.huawei.android.pushselfshow.richpush.favorites;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

class c implements OnClickListener {
    final /* synthetic */ FavoritesActivity a;

    c(FavoritesActivity favoritesActivity) {
        this.a = favoritesActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.i.setEnabled(false);
        new Thread(new d(this)).start();
    }
}
