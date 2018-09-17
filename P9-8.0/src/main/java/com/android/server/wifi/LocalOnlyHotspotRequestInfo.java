package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.android.internal.util.Preconditions;

public class LocalOnlyHotspotRequestInfo implements DeathRecipient {
    static final int HOTSPOT_NO_ERROR = -1;
    private final IBinder mBinder;
    private final RequestingApplicationDeathCallback mCallback;
    private final Messenger mMessenger;
    private final int mPid = Binder.getCallingPid();

    public interface RequestingApplicationDeathCallback {
        void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo localOnlyHotspotRequestInfo);
    }

    LocalOnlyHotspotRequestInfo(IBinder binder, Messenger messenger, RequestingApplicationDeathCallback callback) {
        this.mBinder = (IBinder) Preconditions.checkNotNull(binder);
        this.mMessenger = (Messenger) Preconditions.checkNotNull(messenger);
        this.mCallback = (RequestingApplicationDeathCallback) Preconditions.checkNotNull(callback);
        try {
            this.mBinder.linkToDeath(this, 0);
        } catch (RemoteException e) {
            binderDied();
        }
    }

    public void unlinkDeathRecipient() {
        this.mBinder.unlinkToDeath(this, 0);
    }

    public void binderDied() {
        this.mCallback.onLocalOnlyHotspotRequestorDeath(this);
    }

    public void sendHotspotFailedMessage(int reasonCode) throws RemoteException {
        Message message = Message.obtain();
        message.what = 2;
        message.arg1 = reasonCode;
        this.mMessenger.send(message);
    }

    public void sendHotspotStartedMessage(WifiConfiguration config) throws RemoteException {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = config;
        this.mMessenger.send(message);
    }

    public void sendHotspotStoppedMessage() throws RemoteException {
        Message message = Message.obtain();
        message.what = 1;
        this.mMessenger.send(message);
    }

    public int getPid() {
        return this.mPid;
    }
}
