package com.android.server.security.tsmagent.logic.spi.tsm.laser;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.logic.spi.tsm.request.CommandRequest;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PackageUtil;
import com.android.server.security.tsmagent.utils.StringUtil;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.tsm.TSMOperator;
import com.leisen.wallet.sdk.tsm.TSMOperatorResponse;

public abstract class LaserTSMService {
    public static final int EXCUTE_OTA_RESULT_DEFAULT_ERROR = -100099;
    public static final int EXCUTE_OTA_RESULT_SUCCESS = 100000;
    private Context mContext;
    private final TSMOperator tsmOperator;

    public interface TsmOperCallback {
        void onOperFailure(int i, Error error);

        void onOperSuccess(String str);
    }

    /* access modifiers changed from: package-private */
    public abstract int excuteTSMcommand(TSMOperator tSMOperator, CommonRequestParams commonRequestParams);

    /* access modifiers changed from: package-private */
    public abstract String getRemoteUrl();

    /* access modifiers changed from: package-private */
    public String getTsmUrl() {
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        return ServiceConfig.getTsmUrl() + "?version=" + versionCode;
    }

    public LaserTSMService(Context context, int reader) {
        this.mContext = context;
        this.tsmOperator = TSMOperator.getInstance(context, getRemoteUrl(), reader);
    }

    public int excuteTsmCommand(final CommandRequest request, final TsmOperCallback tsmOperatorResponse) {
        HwLog.i("excuteTsmCommand now");
        if (request == null || StringUtil.isTrimedEmpty(request.getServerID()) || StringUtil.isTrimedEmpty(request.getFuncCall()) || StringUtil.isTrimedEmpty(request.getCplc())) {
            HwLog.e("excuteTsmCommand, params illegal.");
            return EXCUTE_OTA_RESULT_DEFAULT_ERROR;
        }
        HwLog.d("excuteTsmCommand, serviceId: " + request.getServerID() + ",functionId: " + request.getFuncCall());
        CommonRequestParams leisenRequest = new CommonRequestParams(request.getServerID(), request.getFuncCall(), request.getCplc());
        if (tsmOperatorResponse != null) {
            this.tsmOperator.setTsmOperatorResponse(new TSMOperatorResponse() {
                public void onOperSuccess(String response) {
                    tsmOperatorResponse.onOperSuccess(response);
                }

                public void onOperFailure(int result, Error e) {
                    HwLog.e("excuteTsmCommand, failed reason.=" + request + " " + e.getMessage());
                    tsmOperatorResponse.onOperFailure(result, e);
                }
            });
        }
        int excuteResult = excuteTSMcommand(this.tsmOperator, leisenRequest);
        this.tsmOperator.setTsmOperatorResponse(null);
        HwLog.i("excuteTsmCommand, result: " + excuteResult);
        return excuteResult;
    }
}
