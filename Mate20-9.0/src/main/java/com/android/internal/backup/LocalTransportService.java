package com.android.internal.backup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocalTransportService extends Service {
    private static LocalTransport sTransport = null;

    public void onCreate() {
        if (sTransport == null) {
            sTransport = new LocalTransport(this, new LocalTransportParameters(getMainThreadHandler(), getContentResolver()));
        }
        sTransport.getParameters().start();
    }

    public void onDestroy() {
        sTransport.getParameters().stop();
    }

    public IBinder onBind(Intent intent) {
        return sTransport.getBinder();
    }
}
