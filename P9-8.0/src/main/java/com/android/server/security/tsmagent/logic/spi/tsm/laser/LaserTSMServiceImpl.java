package com.android.server.security.tsmagent.logic.spi.tsm.laser;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.logic.spi.tsm.request.CommandRequest;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PackageUtil;
import com.android.server.security.tsmagent.utils.StringUtil;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.tsm.TSMOperator;

public final class LaserTSMServiceImpl {
    public static final int EXCUTE_OTA_RESULT_DEFAULT_ERROR = -100099;
    public static final int EXCUTE_OTA_RESULT_SUCCESS = 100000;
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile LaserTSMServiceImpl instance;
    private final Context mContext;
    private final TSMOperator tsmOperator = TSMOperator.getInstance(this.mContext, getTsmRemoteUrl());

    private LaserTSMServiceImpl(Context context) {
        this.mContext = context;
    }

    private String getTsmRemoteUrl() {
        return ServiceConfig.HUAWEI_TSM_REMOTE_URL + "?version=" + PackageUtil.getVersionCode(this.mContext);
    }

    public static LaserTSMServiceImpl getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new LaserTSMServiceImpl(context);
                }
            }
        }
        return instance;
    }

    public int excuteTsmCommand(CommandRequest request) {
        HwLog.i("excuteTsmCommand now");
        if (request == null || StringUtil.isTrimedEmpty(request.getServerID()) || StringUtil.isTrimedEmpty(request.getFuncCall()) || StringUtil.isTrimedEmpty(request.getCplc())) {
            HwLog.e("excuteTsmCommand, params illegal.");
            return EXCUTE_OTA_RESULT_DEFAULT_ERROR;
        }
        HwLog.d("excuteTsmCommand, serviceId: " + request.getServerID() + ",functionId: " + request.getFuncCall());
        int excuteResult = this.tsmOperator.commonExecute(new CommonRequestParams(request.getServerID(), request.getFuncCall(), request.getCplc()));
        HwLog.i("excuteTsmCommand, result: " + excuteResult);
        return excuteResult;
    }
}
