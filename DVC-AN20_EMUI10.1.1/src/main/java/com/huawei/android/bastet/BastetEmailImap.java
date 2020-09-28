package com.huawei.android.bastet;

import android.os.Handler;
import android.os.RemoteException;

public class BastetEmailImap extends BastetEmail {
    private String mIdCmd;

    public BastetEmailImap(EmailLoginInfo login, EmailSyncConfig config, String idCmd, Handler handler) {
        super(login, config, 2, handler);
        this.mIdCmd = idCmd;
    }

    @Override // com.huawei.android.bastet.BastetEmail
    public void startProxy() throws RemoteException {
        if (this.mProxyId > 0) {
            this.mIBastetManager.setImapIdCmd(this.mProxyId, this.mIdCmd);
            super.startProxy();
            return;
        }
        throw new RemoteException();
    }
}
