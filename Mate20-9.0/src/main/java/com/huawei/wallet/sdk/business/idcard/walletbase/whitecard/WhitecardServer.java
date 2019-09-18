package com.huawei.wallet.sdk.business.idcard.walletbase.whitecard;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;

public class WhitecardServer {
    private static final String TAG = "WhitecardServer";
    private final Context mContext;
    private final String serverTotalUrl;

    public WhitecardServer(Context context) {
        this.mContext = context;
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        this.serverTotalUrl = ServiceConfig.getAccessCardManageServerUrl() + "?clientVersion=" + versionCode;
    }
}
