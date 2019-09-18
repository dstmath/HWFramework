package com.huawei.wallet.sdk.common.apdu.tsm;

import android.content.Context;
import com.huawei.wallet.sdk.common.apdu.tsm.bean.CommonRequestParams;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.TSMOperateParamRequester;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.response.TSMOperateParam;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.response.TSMParamRequestTaskResult;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class TSMOperatorImpl extends WalletProcessTrace {
    private static final String TAG = "TSMOperatorImpl|";
    private ApduRequestAndExcuter apduRequestAndExcuter;

    public TSMOperatorImpl(Context context) {
        this.apduRequestAndExcuter = new ApduRequestAndExcuter(context);
    }

    public TSMOperateResponse requestExcuteTsmOperationWithLogicChannel(TSMOperateParamRequester operateParamRequester, int mediaType) {
        return requestTsmOperate(0, operateParamRequester, mediaType);
    }

    public TSMOperateResponse requestExcuteTsmOperationwithBasicChannel(TSMOperateParamRequester operateParamRequester, int mediaType) {
        return requestTsmOperate(1, operateParamRequester, mediaType);
    }

    private TSMOperateResponse requestTsmOperate(int channelType, TSMOperateParamRequester operateParamRequester, int mediaType) {
        if (operateParamRequester == null) {
            TSMOperateResponse resp = new TSMOperateResponse(TSMOperateResponse.RETURN_UNKNOW_ERROR, "TSMOperatorImpl requestTsmOperate failed. operateParamRequester is null, channelType: " + channelType + ", mediaType: " + mediaType);
            resp.setOriResultCode(TSMOperateResponse.RETURN_UNKNOW_ERROR);
            return resp;
        }
        operateParamRequester.setProcessPrefix(getProcessPrefix(), null);
        TSMParamRequestTaskResult<TSMOperateParam> param = operateParamRequester.requestOperateParams();
        operateParamRequester.resetProcessPrefix();
        if (param.getResultCode() == 0) {
            return excuteRequest(param.getData(), channelType, mediaType);
        }
        int code = changeErrorCodeForParamRequest(param.getResultCode());
        TSMOperateResponse resp2 = new TSMOperateResponse(code, "TSMOperatorImpl requestTsmOperate failed. request param error code : " + param.getResultCode() + ", channelType: " + channelType + ", mediaType: " + mediaType);
        resp2.setOriResultCode(param.getOriResultCode());
        resp2.setApplyApdu(true);
        return resp2;
    }

    private TSMOperateResponse excuteRequest(TSMOperateParam request, int channelType, int mediaType) {
        int checkResult = checkCommonRequestParams(request);
        if (checkResult != 100000) {
            TSMOperateResponse resp = new TSMOperateResponse(checkResult, "excuteTsmCommand, params illegal.");
            resp.setOriResultCode(checkResult);
            resp.setApplyApdu(true);
            return resp;
        }
        LogC.d(getSubProcessPrefix() + "excuteTsmCommand, serviceId: " + request.getServerId() + ",functionId: " + request.getFunCallId(), false);
        CommonRequestParams tsmRequest = new CommonRequestParams(request.getServerId(), request.getFunCallId(), request.getCplc());
        LogC.i(getSubProcessPrefix() + "Start to query apdu from TSM and execute it, channelType " + channelType + ", mediaType " + mediaType, false);
        this.apduRequestAndExcuter.setProcessPrefix(getProcessPrefix(), null);
        TSMOperateResponse response = this.apduRequestAndExcuter.requestCommonMethod(tsmRequest, channelType, mediaType);
        this.apduRequestAndExcuter.resetProcessPrefix();
        return response;
    }

    private int checkCommonRequestParams(TSMOperateParam request) {
        if (request == null) {
            return TSMOperateResponse.RETURN_REQUEST_PARAMS_IS_NULL;
        }
        if (StringUtil.isEmpty(request.getCplc(), true)) {
            return TSMOperateResponse.RETURN_REQUESTPARAM_CPLC_IS_NULL;
        }
        if (StringUtil.isEmpty(request.getServerId(), true)) {
            return TSMOperateResponse.RETURN_REQUESTPARAM_SERVICEID_IS_NULL;
        }
        if (StringUtil.isEmpty(request.getFunCallId(), true)) {
            return TSMOperateResponse.RETURN_REQUESTPARAM_FUNCALLID_IS_NULL;
        }
        return TSMOperateResponse.TSM_OPERATE_RESULT_SUCCESS;
    }

    private int changeErrorCodeForParamRequest(int paramRequestErrorCd) {
        if (paramRequestErrorCd == -99) {
            return TSMOperateResponse.RETURN_UNKNOW_ERROR;
        }
        if (paramRequestErrorCd == 10099) {
            return 10099;
        }
        switch (paramRequestErrorCd) {
            case -4:
                return TSMOperateResponse.RETURN_REQUESTPARAM_ST_INVALID;
            case -3:
                return TSMOperateResponse.RETURN_REQUESTPARAM_CPLC_IS_NULL;
            case -2:
                return TSMOperateResponse.RETURN_REQUESTPARAM_CONN_UNAVAILABLE;
            case -1:
                return TSMOperateResponse.RETURN_REQUESTPARAM_NO_NETWORK;
            default:
                return TSMOperateResponse.RETURN_UNKNOW_ERROR;
        }
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
