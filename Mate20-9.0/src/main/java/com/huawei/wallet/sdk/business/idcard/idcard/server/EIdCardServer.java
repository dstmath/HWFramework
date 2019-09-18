package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.constant.ServerAddressConstant;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessCancelEidRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessCancelEidResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.task.ServerAccessCancelEidTask;
import com.huawei.wallet.sdk.business.idcard.idcard.util.ServerAddressUtil;
import com.huawei.wallet.sdk.common.log.LogC;

public class EIdCardServer extends IdCardServer {
    private static final String TAG = "IDCard:EIdCardServer";

    public EIdCardServer(Context context) {
        super(context);
        this.modelName = ServerAddressConstant.EID_CARD_MODULE_NAME;
    }

    public ServerAccessCancelEidResponse cancelEid(ServerAccessCancelEidRequest request) {
        LogC.i(TAG, "cancelEid begin.", false);
        ServerAccessCancelEidResponse response = (ServerAccessCancelEidResponse) new ServerAccessCancelEidTask(this.mContext, ServerAddressUtil.getInstance().getAddress(ServerAddressConstant.IDCARD_CMD_CANCELEID, this.modelName, null, this.mContext)).processTask(request);
        LogC.i(TAG, "cancelEid end.", false);
        return response;
    }
}
