package com.android.server.security.tsmagent.logic.card.tsm;

import android.content.Context;
import android.os.Build;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.logic.ese.ESEInfoManager;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;
import com.android.server.security.tsmagent.logic.spi.tsm.request.CommandRequest;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.card.response.TsmParamQueryResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PhoneDeviceUtil;
import com.android.server.security.tsmagent.utils.StringUtil;

public abstract class TsmBaseOperator {
    public static final String SIGN_TYPE_SHA256 = "RSA256";
    public static final int TSM_OPERATE_RESULT_FAILED_CONN_UNAVAILABLE = -2;
    public static final int TSM_OPERATE_RESULT_FAILED_CPLC_ERRO = -3;
    public static final int TSM_OPERATE_RESULT_FAILED_NO_NETWORK = -1;
    public static final int TSM_OPERATE_RESULT_FAILED_UNKNOWN_ERROR = -99;
    public static final int TSM_OPERATE_RESULT_SUCCESS = 0;
    /* access modifiers changed from: private */
    public String errInfo;
    private final String mAction;
    protected Context mContext;

    /* access modifiers changed from: package-private */
    public abstract int excute(int i);

    /* access modifiers changed from: package-private */
    public abstract TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest tsmParamQueryRequest);

    TsmBaseOperator(Context context, String action) {
        this.mContext = context;
        this.mAction = action;
    }

    public String getErrInfo() {
        return this.errInfo;
    }

    public int excute_withAction(int reader, LaserTSMService execute) {
        HwLog.i("tsm operate.");
        int operateResult = -99;
        String cplc = ESEInfoManager.getInstance(this.mContext).queryCplc(reader);
        if (StringUtil.isTrimedEmpty(cplc)) {
            return -3;
        }
        TsmParamQueryResponse paramQueryResponse = queryOperateParams(createTsmParamQueryRequest(cplc, PhoneDeviceUtil.getSerialNumber(this.mContext)));
        if (paramQueryResponse == null) {
            HwLog.e("excute, query params failed.");
            return -99;
        }
        HwLog.i("excute tsm operate, query params result code:" + paramQueryResponse.returnCode);
        if (paramQueryResponse.returnCode != 0) {
            return translateReturnCode(paramQueryResponse.returnCode);
        }
        if (StringUtil.isTrimedEmpty(paramQueryResponse.funcID) || StringUtil.isTrimedEmpty(paramQueryResponse.servicID)) {
            HwLog.e("TsmBaseOperator", "funcID or serviceID illegal.");
            return -99;
        }
        CommandRequest commandRequest = createCommandRequest(cplc, paramQueryResponse);
        StringBuilder sb = new StringBuilder(128);
        sb.append("excute, funcallID: ");
        sb.append(commandRequest.getFuncCall());
        sb.append(",serverID: ");
        sb.append(commandRequest.getServerID());
        HwLog.d(sb.toString());
        int excuteResult = execute.excuteTsmCommand(commandRequest, new LaserTSMService.TsmOperCallback() {
            public void onOperSuccess(String response) {
            }

            public void onOperFailure(int result, Error e) {
                String unused = TsmBaseOperator.this.errInfo = e.getMessage();
            }
        });
        HwLog.i("excute action result: " + String.valueOf(excuteResult));
        if (100000 == excuteResult) {
            operateResult = 0;
        } else {
            HwLog.e("Tsm excute err: 907125806, fail_action: " + this.mAction + ", fail_code: " + String.valueOf(excuteResult));
        }
        return operateResult;
    }

    private TsmParamQueryRequest createTsmParamQueryRequest(String cplc, String imei) {
        if (imei != null && imei.length() > 16) {
            HwLog.d("operation code : " + imei.length());
            imei = imei.substring(0, 16);
        }
        TsmParamQueryRequest tsmParamQueryRequest = new TsmParamQueryRequest(cplc, ServiceConfig.getWalletId(), -1, ServiceConfig.getWalletId(), Build.MODEL, imei);
        return tsmParamQueryRequest;
    }

    private CommandRequest createCommandRequest(String cplc, TsmParamQueryResponse paramQueryResponse) {
        return CommandRequest.build(cplc, paramQueryResponse.funcID, paramQueryResponse.servicID);
    }

    private int translateReturnCode(int returnCode) {
        if (-1 == returnCode) {
            return -1;
        }
        if (-2 == returnCode) {
            return -2;
        }
        HwLog.e("Tsm quire param err: 907125805, fail_action: " + this.mAction + ", fail_code: " + String.valueOf(returnCode));
        return -99;
    }
}
