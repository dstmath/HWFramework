package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.request.DeleteAppletRequest;
import com.huawei.wallet.sdk.business.bankcard.request.DeleteSSDRequest;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperateResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperator;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperatorImpl;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.TSMOperateParamRequester;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;

public final class BankCardTsmOperator extends WalletProcessTrace {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "BankCardTsmOperator";
    private static volatile BankCardTsmOperator instance = null;
    private Context mContext;
    private TSMOperatorImpl tsmOperator;

    private BankCardTsmOperator(Context context) {
        this.mContext = context;
        this.tsmOperator = new TSMOperatorImpl(context);
    }

    public static BankCardTsmOperator getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new BankCardTsmOperator(context);
                }
            }
        }
        return instance;
    }

    public TSMOperateResponse initEse(int mediaType) {
        TSMOperator.getInstance(this.mContext).setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = TSMOperator.getInstance(this.mContext).initEse(mediaType);
        TSMOperator.getInstance(this.mContext).resetProcessPrefix();
        return response;
    }

    public TSMOperateResponse syncEseInfo(String aid) {
        return syncEseInfo(aid, 0);
    }

    public TSMOperateResponse syncEseInfo(String aid, int mediaType) {
        return TSMOperator.getInstance(this.mContext).syncEseInfo(aid, mediaType);
    }

    public TSMOperateResponse deleteSSD(String aid, boolean deleteRelatedObjects) {
        return deleteSSD(aid, deleteRelatedObjects, 0);
    }

    public TSMOperateResponse deleteSSD(String aid, boolean deleteRelatedObjects, int mediaType) {
        TSMOperateParamRequester requester = new DeleteSSDRequest(this.mContext, aid, deleteRelatedObjects, mediaType);
        this.tsmOperator.setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(requester, mediaType);
        this.tsmOperator.resetProcessPrefix();
        return response;
    }

    public TSMOperateResponse deleteApplet(String aid) {
        return deleteApplet(aid, 0);
    }

    public TSMOperateResponse deleteApplet(String aid, int mediaType) {
        TSMOperateParamRequester requester = new DeleteAppletRequest(this.mContext, aid, mediaType);
        this.tsmOperator.setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(requester, mediaType);
        this.tsmOperator.resetProcessPrefix();
        return response;
    }

    public TSMOperateResponse deleteApplet(String aid, String sign, String timeStamp, String rsaIndex) {
        return deleteApplet(aid, sign, timeStamp, rsaIndex, 0);
    }

    public TSMOperateResponse deleteApplet(String aid, String sign, String timeStamp, String rsaIndex, int mediaType) {
        DeleteAppletRequest requester = new DeleteAppletRequest(this.mContext, aid, mediaType);
        requester.setVerifySign(sign);
        requester.setVerifyTime(timeStamp);
        requester.setBankRsaIndex(rsaIndex);
        this.tsmOperator.setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(requester, mediaType);
        this.tsmOperator.resetProcessPrefix();
        return response;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, "BankCardTsmOperator|");
    }
}
