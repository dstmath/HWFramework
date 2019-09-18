package com.huawei.wallet.sdk.business.bankcard.util;

import android.content.Context;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperateResponse;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;

public class EseTsmInitLoader extends WalletProcessTrace implements Runnable {
    private static final long RECHECK_CAP_UPGRADE_DURATION = 86400000;
    private static final String SHAREPREFRENCE_KEY_LAST_CHECK_CAP_TIME = "last_check_cap_time";
    private static final String SHAREPREFRENCE_KEY_LAST_CHECK_CAP_TIME_FOR_INSE = "last_check_cap_time_for_inse";
    private static final String TAG = "EseTsmInitLoader|";
    private boolean isPushInit = false;
    private final Context mContext;
    private int mediaType = 0;

    public EseTsmInitLoader(Context context) {
        this.mContext = context;
    }

    public EseTsmInitLoader(Context context, int mediaType2) {
        this.mContext = context;
        this.mediaType = mediaType2;
    }

    public void run() {
        LogC.i(getSubProcessPrefix() + "Start to run EseTsmInitLoader.", false);
        excuteEseInit();
    }

    public int excuteEseInit() {
        if (!isUnLockEseSuccess()) {
            return TSMOperateResponse.RETURN_REQUESTPARAM_CPLC_IS_NULL;
        }
        if (this.mediaType == 0) {
        }
        return notifyInfoInit();
    }

    private int unLockEse() {
        return 0;
    }

    private int notifyInfoInit() {
        return 0;
    }

    private boolean isUnLockEseSuccess() {
        LogC.i(getSubProcessPrefix() + "Unlock eSE end, isUnLockEseSuccess " + true, false);
        return true;
    }

    private boolean isCanUnLockDevice() {
        if (this.mediaType == 3) {
            return false;
        }
        return true;
    }

    public void setPushInit(boolean pushInit) {
        this.isPushInit = pushInit;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
