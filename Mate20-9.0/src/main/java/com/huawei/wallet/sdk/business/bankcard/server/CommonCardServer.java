package com.huawei.wallet.sdk.business.bankcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;

public class CommonCardServer {
    protected final Context mContext;
    protected final String serverTotalUrl = (ServiceConfig.getCardInfoManageServerUrl() + "?clientVersion=" + 8);

    public CommonCardServer(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public String getServerAddress(String commander, String module) {
        if (module == null || module.isEmpty()) {
            return this.serverTotalUrl;
        }
        return AddressNameMgr.getInstance().getAddress(commander, module, null, this.mContext);
    }
}
