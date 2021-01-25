package com.huawei.android.bastet;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.bastet.IBastetListener;
import com.huawei.android.bastet.IBastetManager;

public abstract class BastetManager {
    private static final String BASTET_SERVICE = "BastetService";
    private static final String TAG = "BastetManager";
    private static IBinder sBastetService;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.huawei.android.bastet.BastetManager.AnonymousClass2 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.e(BastetManager.TAG, "Bastet service has died");
            if (BastetManager.sBastetService != null) {
                BastetManager.sBastetService.unlinkToDeath(this, 0);
                IBinder unused = BastetManager.sBastetService = null;
                BastetManager bastetManager = BastetManager.this;
                bastetManager.mIBastetManager = null;
                bastetManager.onBastetDied();
            }
        }
    };
    protected Handler mHandler;
    protected IBastetListener mIBastetListener = new IBastetListener.Stub() {
        /* class com.huawei.android.bastet.BastetManager.AnonymousClass1 */

        @Override // com.huawei.android.bastet.IBastetListener
        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
            BastetManager.this.handleProxyMessage(proxyId, err, ext);
        }
    };
    protected IBastetManager mIBastetManager;

    /* access modifiers changed from: protected */
    public abstract void handleProxyMessage(int i, int i2, int i3);

    /* access modifiers changed from: protected */
    public abstract void onBastetDied();

    protected BastetManager() {
        sBastetService = ServiceManager.getService(BASTET_SERVICE);
        IBinder iBinder = sBastetService;
        if (iBinder == null) {
            Log.e(TAG, "Failed to get bastet service");
            return;
        }
        try {
            iBinder.linkToDeath(this.mDeathRecipient, 0);
            this.mIBastetManager = IBastetManager.Stub.asInterface(sBastetService);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException");
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

    public boolean isBastetSupportIpv6() {
        try {
            if (this.mIBastetManager == null) {
                return false;
            }
            boolean isSupportIpv6 = this.mIBastetManager.isBastetSupportIpv6();
            Log.e(TAG, "isBastetSupportIpv6, isSupportIpv6=" + isSupportIpv6);
            return isSupportIpv6;
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
            Log.e(TAG, "RemoteException");
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void sendMessage(int msgId) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(msgId);
        }
    }

    /* access modifiers changed from: protected */
    public void sendMessage(int msgId, int val) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = msgId;
            msg.arg1 = val;
            this.mHandler.sendMessage(msg);
        }
    }
}
