package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.server.card;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;

public class CommonCardServer implements CommonCardServerApi {
    protected final Context mContext;
    private String mModule;
    protected final String serverTotalUrl;

    public CommonCardServer(Context context) {
        this.mContext = context;
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        this.serverTotalUrl = ServiceConfig.getCardInfoManageServerUrl() + "?clientVersion=" + versionCode;
    }

    public CommonCardServer(Context context, String module) {
        this.mContext = context;
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        this.serverTotalUrl = ServiceConfig.getCardInfoManageServerUrl() + "?clientVersion=" + versionCode;
        if (module != null && !module.isEmpty()) {
            setModule(module);
        }
    }

    /* access modifiers changed from: protected */
    public void setModule(String module) {
        this.mModule = module;
    }

    /* access modifiers changed from: protected */
    public String getModule() {
        return this.mModule;
    }

    /* access modifiers changed from: protected */
    public String getServerAddress(String commander, String module) {
        if (module == null || module.isEmpty()) {
            return this.serverTotalUrl;
        }
        return AddressNameMgr.getInstance().getAddress(commander, module, null, this.mContext);
    }

    public ServerAccessDeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest request) {
        LogX.i("CardServer deleteApplet begin");
        return (ServerAccessDeleteAppletResponse) new ServerAccessDeleteAppletTask(this.mContext, getServerAddress("delete.app", AddressNameMgr.MODULE_NAME_WALLETPASS)).processTask(request);
    }

    public ServerAccessApplyAPDUResponse applyAPDU(ServerAccessApplyAPDURequest request) {
        LogX.i("CardServer applyAPDU begin");
        return (ServerAccessApplyAPDUResponse) new ServerAccessApplyAPDUTask(this.mContext, getServerAddress("get.apdu", AddressNameMgr.MODULE_NAME_WALLETPASS)).processTask(request);
    }
}
