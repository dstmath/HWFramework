package com.android.location.provider;

import android.os.IBinder;

@Deprecated
public abstract class FusedProvider {
    public IBinder getBinder() {
        return null;
    }
}
