package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.constant.ServerAddressConstant;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.task.CardStatusQueryTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.task.ServerAccessApplyAPDUTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.task.ServerAccessDeleteAppletTask;
import com.huawei.wallet.sdk.business.idcard.idcard.util.ServerAddressUtil;
import com.huawei.wallet.sdk.common.log.LogC;

public class IdCardServer {
    private static final String TAG = "IDCard:IdCardServer";
    protected Context mContext;
    protected String modelName = ServerAddressConstant.EID_CARD_MODULE_NAME;

    public IdCardServer(Context context) {
        this.mContext = context;
    }

    public CardStatusQueryResponse queryCardStatus(CardStatusQueryRequest request) {
        LogC.i("queryCardStatus begin.", false);
        CardStatusQueryResponse response = (CardStatusQueryResponse) new CardStatusQueryTask(this.mContext, ServerAddressUtil.getInstance().getAddress(ServerAddressConstant.IDCARD_CMD_READCARDLIST, this.modelName, null, this.mContext)).processTask(request);
        LogC.i(TAG, "queryCardStatus end.", false);
        return response;
    }

    public ServerAccessDeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest request) {
        LogC.i(TAG, "deleteAppletApplet begin.", false);
        ServerAccessDeleteAppletResponse response = (ServerAccessDeleteAppletResponse) new ServerAccessDeleteAppletTask(this.mContext, ServerAddressUtil.getInstance().getAddress(ServerAddressConstant.IDCARD_CMD_DELAPP, this.modelName, null, this.mContext)).processTask(request);
        LogC.i(TAG, "deleteAppletApplet end.", false);
        return response;
    }

    public ServerAccessApplyAPDUResponse applyAPDU(ServerAccessApplyAPDURequest request) {
        LogC.i(TAG, "applyAPDU begin.", false);
        ServerAccessApplyAPDUResponse response = (ServerAccessApplyAPDUResponse) new ServerAccessApplyAPDUTask(this.mContext, ServerAddressUtil.getInstance().getAddress("apdu", this.modelName, null, this.mContext)).processTask(request);
        LogC.i(TAG, "applyAPDU end.", false);
        return response;
    }
}
