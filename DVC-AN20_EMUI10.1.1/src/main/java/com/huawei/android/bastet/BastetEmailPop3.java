package com.huawei.android.bastet;

import android.os.Handler;

public class BastetEmailPop3 extends BastetEmail {
    public BastetEmailPop3(EmailLoginInfo login, EmailSyncConfig config, Handler handler) {
        super(login, config, 1, handler);
    }
}
