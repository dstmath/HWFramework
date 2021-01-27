package com.huawei.android.bastet;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public class BastetEmail extends BastetManager {
    private static final String TAG = "BastetEmail";
    private EmailLoginInfo mLoginInfo;
    private int mProtocol;
    protected int mProxyId;
    private EmailSyncConfig mSyncConfig;

    protected BastetEmail(EmailLoginInfo login, EmailSyncConfig config, int protocol, Handler handler) {
        this.mLoginInfo = login;
        this.mSyncConfig = config;
        this.mProtocol = protocol;
        try {
            this.mProxyId = this.mIBastetManager.initEmailProxy(this.mProtocol, this.mLoginInfo.getHost(), this.mLoginInfo.getPort(), this.mLoginInfo.getSecurity(), this.mLoginInfo.getInterval(), this.mLoginInfo.getAccount(), this.mLoginInfo.getPassword(), this.mIBastetListener);
        } catch (RemoteException e) {
            this.mProxyId = -1;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.bastet.BastetManager
    public void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "handleProxyMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
        if (proxyId != this.mProxyId) {
            Log.e(TAG, "proxyId is not match");
        } else if (err == -11) {
            sendMessage(6);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.bastet.BastetManager
    public void onBastetDied() {
        Log.d(TAG, "bastetd died");
    }

    public void startProxy() throws RemoteException {
        if (this.mProxyId > 0) {
            this.mIBastetManager.updateEmailBoxInfo(this.mProxyId, this.mSyncConfig.getFolderName(), this.mSyncConfig.getLatestUid());
            this.mIBastetManager.startBastetProxy(this.mProxyId);
            return;
        }
        throw new RemoteException();
    }

    public void stopProxy() throws BastetException, RemoteException {
        if (this.mProxyId > 0) {
            this.mIBastetManager.stopBastetProxy(this.mProxyId);
            return;
        }
        throw new BastetException();
    }

    public void clearProxy() throws RemoteException {
        if (this.mProxyId > 0) {
            this.mIBastetManager.clearProxyById(this.mProxyId);
            return;
        }
        throw new RemoteException();
    }
}
