package com.android.internal.orange;

import android.view.ViewGroup;

public interface IDgilForLockscreen {

    public interface ICallback {
        void showLockscreenTips(String str);

        void unlock();
    }

    ViewGroup getDgilLayout();

    String getLockscreenStaticMessage();

    void notifyUnlockedScreen();
}
