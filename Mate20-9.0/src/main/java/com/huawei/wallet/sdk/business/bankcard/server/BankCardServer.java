package com.huawei.wallet.sdk.business.bankcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.BankCardServerApi;
import com.huawei.wallet.sdk.business.bankcard.constant.BankCardServerCmdConstant;
import com.huawei.wallet.sdk.business.bankcard.request.DeleteOverSeaCardRequest;
import com.huawei.wallet.sdk.business.bankcard.request.QueryAidRequest;
import com.huawei.wallet.sdk.business.bankcard.request.QueryUnionPayPushRequest;
import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.CardSwipeResponse;
import com.huawei.wallet.sdk.business.bankcard.response.NullifyCardResponse;
import com.huawei.wallet.sdk.business.bankcard.response.QueryAidResponse;
import com.huawei.wallet.sdk.business.bankcard.response.QueryUnionPayPushResponse;
import com.huawei.wallet.sdk.business.bankcard.task.OverSeaDeleteCardTask;
import com.huawei.wallet.sdk.business.bankcard.task.QueryAidOnCUPCardTask;
import com.huawei.wallet.sdk.business.bankcard.task.QueryUnionPayPushTask;
import com.huawei.wallet.sdk.business.bankcard.task.WipeAllBankCardTask;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.task.ServerAccessApplyAPDUTask;
import com.huawei.wallet.sdk.common.log.LogC;

public class BankCardServer extends CommonCardServer implements BankCardServerApi {
    private static final String TAG = "BankCardServer";

    public BankCardServer(Context context) {
        super(context);
    }

    public CardSwipeResponse wipeAllBankCard(WipeAllBankCardRequest request) {
        LogC.i(TAG, " Wipe All Bank Card request begin.", false);
        Object response = new WipeAllBankCardTask(this.mContext, getServerAddress("wipe.device", AddressNameMgr.MODULE_NAME_BANKCARD), request).processTask(request);
        if (response instanceof CardSwipeResponse) {
            LogC.i(TAG, "Wipe All Bank Card request end.", false);
            return (CardSwipeResponse) response;
        }
        LogC.i(TAG, " Wipe All Bank Card request end.", false);
        return null;
    }

    public NullifyCardResponse deleteOverSeaCard(DeleteOverSeaCardRequest request) {
        LogC.i(TAG, " deleteOverSeaCard,start request service to delete card", false);
        NullifyCardResponse response = (NullifyCardResponse) new OverSeaDeleteCardTask(this.mContext, getServerAddress("delete.app", AddressNameMgr.MODULE_NAME_BANKCARD)).processTask(request);
        LogC.i(TAG, " deleteOverSeaCard request end.", false);
        return response;
    }

    public ServerAccessApplyAPDUResponse applyApdu(ServerAccessApplyAPDURequest request) {
        LogC.i(TAG, " applyApdu,start request service to delete card", false);
        ServerAccessApplyAPDUResponse response = (ServerAccessApplyAPDUResponse) new ServerAccessApplyAPDUTask(this.mContext, getServerAddress("get.apdu", AddressNameMgr.MODULE_NAME_BANKCARD)).processTask(request);
        LogC.i(TAG, " applyApdu request end.", false);
        return response;
    }

    public QueryAidResponse queryAidOnCUP(QueryAidRequest request) {
        LogC.i(TAG, "Query Aid On CUP request begin.", false);
        QueryAidResponse response = (QueryAidResponse) new QueryAidOnCUPCardTask(this.mContext, getServerAddress(BankCardServerCmdConstant.QUERY_AID_CMD, AddressNameMgr.MODULE_NAME_BANKCARD)).processTask(request);
        LogC.i(TAG, "Query Aid On CUP request end.", false);
        return response;
    }

    public QueryUnionPayPushResponse queryUnionPayPush(QueryUnionPayPushRequest request) {
        return (QueryUnionPayPushResponse) new QueryUnionPayPushTask(this.mContext, getServerAddress(BankCardServerCmdConstant.QUERY_UNION_PAY_PUSH_CMD, AddressNameMgr.MODULE_NAME_BANKCARD)).processTask(request);
    }
}
