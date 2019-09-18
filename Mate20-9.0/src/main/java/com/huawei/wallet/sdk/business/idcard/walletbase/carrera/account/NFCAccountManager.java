package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account;

import android.app.Activity;
import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.account.AccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.model.account.AccountInfo;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;

public final class NFCAccountManager extends AccountManager {
    private static volatile NFCAccountManager mInstance;
    private Context mContext;

    public static NFCAccountManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NFCAccountManager(context);
        }
        return mInstance;
    }

    public static AccountManager getInstance() {
        return AccountManager.getInstance();
    }

    private NFCAccountManager(Context context) {
        if (context instanceof Activity) {
            this.mContext = context.getApplicationContext();
        } else {
            this.mContext = context;
        }
    }

    public static String getAccountUserId() {
        AccountInfo accountInfo = getInstance().getAccountInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("getAccountUserId, accountInfo is null: ");
        sb.append(accountInfo == null);
        LogX.d(sb.toString());
        if (accountInfo != null) {
            return accountInfo.getUserId();
        }
        return null;
    }
}
