package com.huawei.android.bastet;

import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.bastet.IBastetListener.Stub;

public abstract class BastetManager {
    private static final String BASTET_SERVICE = "BastetService";
    private static final String TAG = "BastetManager";
    private static IBinder mBastetService;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.e(BastetManager.TAG, "Bastet service has died");
            if (BastetManager.mBastetService != null) {
                BastetManager.mBastetService.unlinkToDeath(this, 0);
                BastetManager.mBastetService = null;
                BastetManager.this.mIBastetManager = null;
                BastetManager.this.onBastetDied();
            }
        }
    };
    protected Handler mHandler;
    protected IBastetListener mIBastetListener = new Stub() {
        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
            BastetManager.this.handleProxyMessage(proxyId, err, ext);
        }
    };
    protected IBastetManager mIBastetManager;

    protected abstract void handleProxyMessage(int i, int i2, int i3);

    protected abstract void onBastetDied();

    protected BastetManager() {
        mBastetService = ServiceManager.getService(BASTET_SERVICE);
        if (mBastetService == null) {
            Log.e(TAG, "Failed to get bastet service");
            return;
        }
        try {
            mBastetService.linkToDeath(this.mDeathRecipient, 0);
            this.mIBastetManager = IBastetManager.Stub.asInterface(mBastetService);
        } catch (RemoteException e) {
        }
    }

    public boolean isBastetAvailable() {
        try {
            if (this.mIBastetManager != null) {
                return this.mIBastetManager.isProxyServiceAvailable();
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public int inquireNetworkQuality() {
        if (!isBastetAvailable()) {
            return 0;
        }
        try {
            if (this.mIBastetManager != null) {
                return this.mIBastetManager.inquireNetworkQuality();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    protected void sendMessage(int msgId) {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(msgId);
        }
    }

    protected void sendMessage(int msgId, int val) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = msgId;
            msg.arg1 = val;
            this.mHandler.sendMessage(msg);
        }
    }
}
