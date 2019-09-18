package com.huawei.wallet.sdk.business.idcard.accesscard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.request.OpenAccessCardRequest;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.response.OpenAccessCardResponse;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.task.OpenAccessCardTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;
import com.huawei.wallet.sdk.common.log.LogC;

public class AccesscardServer {
    private static final String TAG = "AccesscardServer";
    private final Context mContext;
    private final String serverTotalUrl;

    public AccesscardServer(Context context) {
        this.mContext = context;
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        this.serverTotalUrl = ServiceConfig.getAccessCardManageServerUrl() + "?clientVersion=" + versionCode;
    }

    public OpenAccessCardResponse openAccessCard(OpenAccessCardRequest request) {
        LogC.i("openAccessCard begin.", false);
        OpenAccessCardResponse response = (OpenAccessCardResponse) new OpenAccessCardTask(this.mContext, this.serverTotalUrl).processTask(request);
        LogC.i(TAG, "openAccessCard end.", false);
        return response;
    }
}
