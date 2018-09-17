package com.android.internal.backup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocalTransportService extends Service {
    private static LocalTransport sTransport = null;

    public void onCreate() {
        if (sTransport == null) {
            sTransport = new LocalTransport(this);
        }
    }

    public IBinder onBind(Intent intent) {
        return sTransport.getBinder();
    }
}
