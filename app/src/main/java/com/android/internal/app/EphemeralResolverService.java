package com.android.internal.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.EphemeralResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.app.IEphemeralResolver.Stub;
import java.util.List;

public abstract class EphemeralResolverService extends Service {
    public static final String EXTRA_RESOLVE_INFO = "com.android.internal.app.RESOLVE_INFO";
    public static final String EXTRA_SEQUENCE = "com.android.internal.app.SEQUENCE";
    private Handler mHandler;

    private final class ServiceHandler extends Handler {
        public static final int MSG_GET_EPHEMERAL_RESOLVE_INFO = 1;

        public ServiceHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            int action = message.what;
            switch (action) {
                case MSG_GET_EPHEMERAL_RESOLVE_INFO /*1*/:
                    IRemoteCallback callback = message.obj;
                    List<EphemeralResolveInfo> resolveInfo = EphemeralResolverService.this.getEphemeralResolveInfoList(message.arg1);
                    Bundle data = new Bundle();
                    data.putInt(EphemeralResolverService.EXTRA_SEQUENCE, message.arg2);
                    data.putParcelableList(EphemeralResolverService.EXTRA_RESOLVE_INFO, resolveInfo);
                    try {
                        callback.sendResult(data);
                    } catch (RemoteException e) {
                    }
                default:
                    throw new IllegalArgumentException("Unknown message: " + action);
            }
        }
    }

    protected abstract List<EphemeralResolveInfo> getEphemeralResolveInfoList(int i);

    protected final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new ServiceHandler(base.getMainLooper());
    }

    public final IBinder onBind(Intent intent) {
        return new Stub() {
            public void getEphemeralResolveInfoList(IRemoteCallback callback, int digestPrefix, int sequence) {
                EphemeralResolverService.this.mHandler.obtainMessage(1, digestPrefix, sequence, callback).sendToTarget();
            }
        };
    }
}
