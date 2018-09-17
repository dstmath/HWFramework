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
    protected static Handler mHandler;
    protected static IBastetManager mIBastetManager;
    private DeathRecipient mDeathRecipient;
    protected IBastetListener mIBastetListener;

    protected abstract void handleProxyMessage(int i, int i2, int i3);

    protected abstract void onBastetDied();

    protected BastetManager() {
        this.mIBastetListener = new Stub() {
            public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
                BastetManager.this.handleProxyMessage(proxyId, err, ext);
            }
        };
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                Log.e(BastetManager.TAG, "Bastet service has died");
                if (BastetManager.mBastetService != null) {
                    BastetManager.mBastetService.unlinkToDeath(this, 0);
                    BastetManager.mBastetService = null;
                    BastetManager.mIBastetManager = null;
                    BastetManager.this.onBastetDied();
                }
            }
        };
        mBastetService = ServiceManager.getService(BASTET_SERVICE);
        if (mBastetService == null) {
            Log.e(TAG, "Failed to get bastet service");
            return;
        }
        try {
            mBastetService.linkToDeath(this.mDeathRecipient, 0);
            mIBastetManager = IBastetManager.Stub.asInterface(mBastetService);
        } catch (RemoteException e) {
        }
    }

    public boolean isBastetAvailable() {
        try {
            if (mIBastetManager != null) {
                return mIBastetManager.isProxyServiceAvailable();
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public int inquireNetworkQuality() {
        int level = 0;
        if (isBastetAvailable()) {
            try {
                if (mIBastetManager != null) {
                    level = mIBastetManager.inquireNetworkQuality();
                }
            } catch (RemoteException e) {
            }
        }
        return level;
    }

    protected void sendMessage(int msgId) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(msgId);
        }
    }

    protected void sendMessage(int msgId, int val) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage();
            msg.what = msgId;
            msg.arg1 = val;
            mHandler.sendMessage(msg);
        }
    }
}
