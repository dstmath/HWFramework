package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.util.EseTsmInitLoader;

public class InitEseTask implements Runnable {
    private final Context mContext;

    public InitEseTask(Context context) {
        this.mContext = context;
    }

    public void run() {
        int checkEseInitStatus = checkEseInitStatus();
    }

    private int checkEseInitStatus() {
        return new EseTsmInitLoader(this.mContext).excuteEseInit();
    }
}
