package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.common.AppConfig;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.response.TSMOperateParam;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.response.TSMParamRequestTaskResult;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.NfcUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public abstract class TSMOperateParamRequester extends WalletProcessTrace {
    public static final String SIGN_TYPE_SHA256 = "RSA256";
    private static final String TAG = "TSMOperateParamRequeste|";
    private final String mAction;
    protected Context mContext;
    private boolean mIsNeedServiceTokenAuth = false;
    private int mMediaType = 0;

    /* access modifiers changed from: protected */
    public abstract TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest tsmParamQueryRequest);

    public boolean getIsNeedServiceTokenAuth() {
        return this.mIsNeedServiceTokenAuth;
    }

    public void setIsNeedServiceTokenAuth(boolean isNeedServiceTokenAuth) {
        this.mIsNeedServiceTokenAuth = isNeedServiceTokenAuth;
    }

    protected TSMOperateParamRequester(Context context, String action, int mediaType) {
        this.mContext = context;
        this.mAction = action;
        this.mMediaType = mediaType;
    }

    public TSMParamRequestTaskResult<TSMOperateParam> requestOperateParams() {
        LogC.i(getSubProcessPrefix() + "Start to query tsm operate params for " + this.mAction, false);
        TSMParamRequestTaskResult<TSMOperateParam> result = new TSMParamRequestTaskResult<>();
        String cplc = getCplc();
        if (StringUtil.isEmpty(cplc, true)) {
            result.setResultCode(-3);
            result.setMsg(this.mAction + " requestOperateParams failed. cplc is null");
            result.setOriResultCode(TSMParamRequestTaskResult.TSM_OPERATE_RESULT_SA_CPLC_ERROR);
            return result;
        }
        TsmParamQueryResponse paramQueryResponse = queryOperateParams(createTsmParamQueryRequest(cplc));
        if (paramQueryResponse == null) {
            String msg = this.mAction + " queryOperateParams from wallet server failed. response is null";
            LogC.e(msg, false);
            result.setResultCode(-99);
            result.setMsg(msg);
            result.setOriResultCode(-99);
            return result;
        }
        LogC.i(getSubProcessPrefix() + "Query tsm operate params for " + this.mAction + " end, returnCode " + paramQueryResponse.returnCode, false);
        if (paramQueryResponse.returnCode != 0) {
            result.setResultCode(translateReturnCode(paramQueryResponse.returnCode));
            result.setMsg(this.mAction + " queryOperateParams from wallet server failed. result is wrong");
            result.setOriResultCode(paramQueryResponse.returnCode);
            return result;
        }
        result.setData(createCommandRequest(cplc, paramQueryResponse));
        result.setResultCode(0);
        result.setOriResultCode(0);
        return result;
    }

    /* access modifiers changed from: protected */
    public String getCplc() {
        if (this.mMediaType == 3) {
            LogC.i("TSMOperateParamRequester|queryinSECplc|mMediaType=" + this.mMediaType, false);
            ESEInfoManager.getInstance(this.mContext).setProcessPrefix(getProcessPrefix(), null);
            String cplc = ESEInfoManager.getInstance(this.mContext).queryinSECplc();
            ESEInfoManager.getInstance(this.mContext).resetProcessPrefix();
            return cplc;
        }
        LogC.i("TSMOperateParamRequester|getCplc|mMediaType=" + this.mMediaType, false);
        ESEInfoManager.getInstance(this.mContext).setProcessPrefix(getProcessPrefix(), null);
        String cplc2 = ESEInfoManager.getInstance(this.mContext).queryCplc();
        ESEInfoManager.getInstance(this.mContext).resetProcessPrefix();
        return cplc2;
    }

    private TsmParamQueryRequest createTsmParamQueryRequest(String cplc) {
        TsmParamQueryRequest tsmParamQueryRequest = new TsmParamQueryRequest(cplc, AppConfig.MERCHANT_ID, -1, NfcUtil.generateSrcId(), Build.MODEL);
        return tsmParamQueryRequest;
    }

    private TSMOperateParam createCommandRequest(String cplc, TsmParamQueryResponse paramQueryResponse) {
        return TSMOperateParam.build(cplc, paramQueryResponse.funcID, paramQueryResponse.servicID);
    }

    private int translateReturnCode(int returnCode) {
        int operateResult = -99;
        if (-1 == returnCode) {
            operateResult = -1;
        } else if (-2 == returnCode) {
            operateResult = -2;
        } else if (2 == returnCode) {
            operateResult = -4;
        } else if (99 == returnCode) {
            operateResult = 10099;
        }
        if (!(operateResult == -1 || operateResult == -2)) {
            LogC.e("Tsm quire param err!", false);
        }
        return operateResult;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
