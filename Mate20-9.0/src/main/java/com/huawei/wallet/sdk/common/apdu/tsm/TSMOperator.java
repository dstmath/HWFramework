package com.huawei.wallet.sdk.common.apdu.tsm;

import android.content.Context;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.CreateOrDeleteOpenSSDParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.DeleteAppletParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.DeleteSSDParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.InitEseParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.ResetParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.SynESEParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.TSMOperateParamRequester;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;

public final class TSMOperator extends WalletProcessTrace implements ITSMOperator {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "TSMOperator|";
    private static volatile TSMOperator instance = null;
    private static final Object sLock = new Object();
    private Context mContext;
    private TSMOperatorImpl tsmOperator;

    private TSMOperator(Context context) {
        this.mContext = context;
        this.tsmOperator = new TSMOperatorImpl(context);
    }

    public static TSMOperator getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new TSMOperator(context);
                }
            }
        }
        return instance;
    }

    public TSMOperateResponse initEse() {
        return initEse(0);
    }

    public TSMOperateResponse initEse(int mediaType) {
        TSMOperateParamRequester requester = new InitEseParamRequester(this.mContext, mediaType);
        this.tsmOperator.setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(requester, mediaType);
        this.tsmOperator.resetProcessPrefix();
        return response;
    }

    public TSMOperateResponse initEseByBasicChannel() {
        return initEseByBasicChannel(0);
    }

    public TSMOperateResponse initEseByBasicChannel(int mediaType) {
        return this.tsmOperator.requestExcuteTsmOperationwithBasicChannel(new InitEseParamRequester(this.mContext, mediaType), mediaType);
    }

    public TSMOperateResponse syncEseInfo(String aid) {
        return syncEseInfo(aid, 0);
    }

    public TSMOperateResponse syncEseInfo(String aid, int mediaType) {
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(new SynESEParamRequester(this.mContext, aid, mediaType), mediaType);
    }

    public TSMOperateResponse syncEseInfo(String spid, String sign, String timeStamp, boolean isNeedServiceTokenAuth) {
        return syncEseInfo(spid, sign, timeStamp, isNeedServiceTokenAuth, 0);
    }

    public TSMOperateResponse syncEseInfo(String spid, String sign, String timeStamp, boolean isNeedServiceTokenAuth, int mediaType) {
        SynESEParamRequester synESEParamRequester = new SynESEParamRequester(this.mContext, spid, sign, timeStamp, mediaType);
        synESEParamRequester.setIsNeedServiceTokenAuth(isNeedServiceTokenAuth);
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(synESEParamRequester, mediaType);
    }

    public TSMOperateResponse deleteSSD(String aid, boolean deleteRelatedObjects) {
        return deleteSSD(aid, deleteRelatedObjects, 0);
    }

    public TSMOperateResponse deleteSSD(String aid, boolean deleteRelatedObjects, int mediaType) {
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(new DeleteSSDParamRequester(this.mContext, aid, deleteRelatedObjects, mediaType), mediaType);
    }

    public TSMOperateResponse deleteSSD(String aid, String issueId, boolean deleteRelatedObjects) {
        return deleteSSD(aid, issueId, deleteRelatedObjects, 0);
    }

    public TSMOperateResponse deleteSSD(String aid, String issueId, boolean deleteRelatedObjects, int mediaType) {
        DeleteSSDParamRequester deleteSSDParamRequester = new DeleteSSDParamRequester(this.mContext, aid, issueId, deleteRelatedObjects, mediaType);
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(deleteSSDParamRequester, mediaType);
    }

    public TSMOperateResponse deleteSSD(String aid, String spId, String sign, String timeStamp, boolean isNeedServiceTokenAuth) {
        return deleteSSD(aid, spId, sign, timeStamp, isNeedServiceTokenAuth, 0);
    }

    public TSMOperateResponse deleteSSD(String aid, String spId, String sign, String timeStamp, boolean isNeedServiceTokenAuth, int mediaType) {
        CreateOrDeleteOpenSSDParamRequester createOrDeleteOpenSSDParamRequester = new CreateOrDeleteOpenSSDParamRequester(this.mContext, aid, spId, sign, timeStamp, CreateOrDeleteOpenSSDParamRequester.OPERATOR_TYPE_DELETE_SSD, mediaType);
        createOrDeleteOpenSSDParamRequester.setIsNeedServiceTokenAuth(isNeedServiceTokenAuth);
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(createOrDeleteOpenSSDParamRequester, mediaType);
    }

    public TSMOperateResponse deleteApplet(String aid) {
        return deleteApplet(aid, 0);
    }

    public TSMOperateResponse deleteApplet(String aid, int mediaType) {
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(new DeleteAppletParamRequester(this.mContext, aid, mediaType), mediaType);
    }

    public TSMOperateResponse deleteApplet(String aid, String sign, String timeStamp, String rsaIndex) {
        return deleteApplet(aid, sign, timeStamp, rsaIndex, 0);
    }

    public TSMOperateResponse deleteApplet(String aid, String sign, String timeStamp, String rsaIndex, int mediaType) {
        DeleteAppletParamRequester deleteAppletParamRequester = new DeleteAppletParamRequester(this.mContext, aid, sign, timeStamp, rsaIndex, mediaType);
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(deleteAppletParamRequester, mediaType);
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }

    public TSMOperateResponse resetOpt(String rand, int mediaType) {
        return this.tsmOperator.requestExcuteTsmOperationWithLogicChannel(new ResetParamRequester(this.mContext, rand, mediaType), mediaType);
    }
}
