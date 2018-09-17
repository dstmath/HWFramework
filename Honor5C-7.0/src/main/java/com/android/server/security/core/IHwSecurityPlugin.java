package com.android.server.security.core;

import android.content.Context;
import android.os.IBinder;

public interface IHwSecurityPlugin {

    public interface Creator {
        IHwSecurityPlugin createPlugin(Context context);

        String getPluginPermission();
    }

    IBinder asBinder();

    void onStart();

    void onStop();
}
