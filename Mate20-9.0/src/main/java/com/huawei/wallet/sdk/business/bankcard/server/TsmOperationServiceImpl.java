package com.huawei.wallet.sdk.business.bankcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.TsmOperationService;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;

public class TsmOperationServiceImpl extends WalletProcessTrace implements TsmOperationService {
    private static final String TAG = "TsmOperationService";
    private Context mContext;

    public TsmOperationServiceImpl(Context mContext2) {
        this.mContext = mContext2;
    }

    public TsmParamQueryResponse queryCreateSSDTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryCreateSSDTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_CREATE_SSD, TsmOperationConstant.TASK_COMMANDER_CREATE_SSD).processTask(request);
        LogC.i(TAG, "queryCreateSSDTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryInstallTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryInstallTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_INSTALL_APP, TsmOperationConstant.TASK_COMMANDER_INSTALL_APP).processTask(request);
        LogC.i(TAG, "queryInstallTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryDeleteAppletTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryDeleteAppletTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_DEL_APP, TsmOperationConstant.TASK_COMMANDER_DEL_APP).processTask(request);
        LogC.i(TAG, "queryDeleteAppletTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryUnLockEseTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryUnLockEseTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_UNLOCK_ESE, TsmOperationConstant.TASK_COMMANDER_UNLOCK_ESE).processTask(request);
        LogC.i(TAG, "queryUnLockEseTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryLockAppletTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryLockAppletTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_LOCK_APP, TsmOperationConstant.TASK_COMMANDER_LOCK_APP).processTask(request);
        LogC.i(TAG, "queryLockAppletTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryUnockAppletTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryUnockAppletTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_UNLOCK_APP, TsmOperationConstant.TASK_COMMANDER_UNLOCK_APP).processTask(request);
        LogC.i(TAG, "queryUnockAppletTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryDeleteSSDTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryDeleteSSDTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_DEL_SSD, TsmOperationConstant.TASK_COMMANDER_DEL_SSD).processTask(request);
        LogC.i(TAG, "queryDeleteSSDTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryInfoInitTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_INFO_INIT, TsmOperationConstant.TASK_COMMANDER_INFO_INIT).processTask(request);
        LogC.i(TAG, "queryInfoInitTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryUpdateTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryUpdateTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_UPDATE_APP, TsmOperationConstant.TASK_COMMANDER_UPDATE_APP).processTask(request);
        LogC.i(TAG, "queryUpdateTsmParam end.", false);
        return response;
    }

    public TsmParamQueryResponse queryInfoSynTsmParam(TsmParamQueryRequest request) {
        LogC.i(TAG, "queryInfoSynTsmParam begin.", false);
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, TsmOperationConstant.TASK_COMMANDER_SYNC_INFO, TsmOperationConstant.TASK_COMMANDER_SYNC_INFO).processTask(request);
        LogC.i(TAG, "queryInfoSynTsmParam end.", false);
        return response;
    }
}
