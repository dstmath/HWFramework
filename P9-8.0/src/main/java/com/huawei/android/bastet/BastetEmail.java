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

    protected void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "handleProxyMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
        if (proxyId != this.mProxyId) {
            Log.e(TAG, "proxyId is not match");
            return;
        }
        switch (err) {
            case BastetParameters.PROXY_NEW_MESSAGE /*-11*/:
                sendMessage(6);
                break;
        }
    }

    protected void onBastetDied() {
        Log.d(TAG, "bastetd died");
    }

    public void startProxy() throws Exception {
        if (this.mProxyId <= 0) {
            throw new Exception();
        }
        this.mIBastetManager.updateEmailBoxInfo(this.mProxyId, this.mSyncConfig.getFolderName(), this.mSyncConfig.getLatestUid());
        this.mIBastetManager.startBastetProxy(this.mProxyId);
    }

    public void stopProxy() throws Exception {
        if (this.mProxyId <= 0) {
            throw new Exception();
        }
        this.mIBastetManager.stopBastetProxy(this.mProxyId);
    }

    public void clearProxy() throws Exception {
        if (this.mProxyId <= 0) {
            throw new Exception();
        }
        this.mIBastetManager.clearProxyById(this.mProxyId);
    }
}
