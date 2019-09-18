package com.huawei.android.bastet;

import android.os.Handler;

public class BastetEmailImap extends BastetEmail {
    private String mIdCmd;

    public BastetEmailImap(EmailLoginInfo login, EmailSyncConfig config, String idCmd, Handler handler) {
        super(login, config, 2, handler);
        this.mIdCmd = idCmd;
    }

    public void startProxy() throws Exception {
        if (this.mProxyId > 0) {
            this.mIBastetManager.setImapIdCmd(this.mProxyId, this.mIdCmd);
            super.startProxy();
            return;
        }
        throw new Exception();
    }
}
