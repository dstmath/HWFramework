package com.huawei.wallet.sdk.common.apdu.tsm;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.tsm.bean.CommonRequestParams;
import com.huawei.wallet.sdk.common.apdu.tsm.business.ApduBean;
import com.huawei.wallet.sdk.common.apdu.tsm.business.ApduResBean;
import com.huawei.wallet.sdk.common.apdu.tsm.business.BaseBusinessForResp;
import com.huawei.wallet.sdk.common.apdu.tsm.business.BaseResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.http.AsyncHttpClient;
import com.huawei.wallet.sdk.common.apdu.tsm.http.ResponseHandlerInterface;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JsonUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public class ApduRequestAndExcuter extends WalletProcessTrace implements ResponseHandlerInterface {
    private static final String BOUNDARY = " ==> ";
    private static final int BUSINESS_TYPE_COMMON_METHOD = 40;
    private static final int RESULT_SEND_TO_SERVER_FAILED = 1;
    private static final int RESULT_SEND_TO_SERVER_SUCCESS = 0;
    private static final String TAG = "ApduRequestAndExcuter";
    private static TypeToken TYPE_TOKEN = new TypeToken<BaseResponse<BaseBusinessForResp>>() {
    };
    private String apduError = "";
    private AsyncHttpClient mAsyncHttpClient;
    private int mBusinessType = -1;
    private ChannelID mChannelID;
    private CommonRequestParams mCommonRequestParams;
    private Context mContext;
    private int mCurrentTaskIndex = 1;
    private String mErrorMessage;
    private TSMOperateResponse mOperatorResult = new TSMOperateResponse();
    private IAPDUService omaService;

    ApduRequestAndExcuter(Context context) {
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mAsyncHttpClient = new AsyncHttpClient();
        this.omaService = OmaApduManager.getInstance(context);
    }

    /* access modifiers changed from: package-private */
    public TSMOperateResponse requestCommonMethod(CommonRequestParams params, int channelType, int eseType) {
        this.mBusinessType = 40;
        this.mChannelID = new ChannelID();
        this.mChannelID.setChannelType(channelType);
        this.mChannelID.setMediaType(eseType);
        this.mCommonRequestParams = params;
        String request = JsonUtil.getBaseReqJsonResult(this.mContext, params, this.mBusinessType, this.mCurrentTaskIndex);
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            this.apduError = "";
            sendFirstApdu(request);
        }
        return this.mOperatorResult;
    }

    private void sendFirstApdu(String request) {
        sendApduToServer(request);
    }

    private void sendNextApdu(int result, int index, String rapdu, String sw) {
        ApduResBean rapduList = new ApduResBean();
        rapduList.setIndex(index);
        rapduList.setApdu(rapdu);
        rapduList.setSw(sw);
        sendApduToServer(JsonUtil.getReqNextJsonResult(this.mContext, this.mCommonRequestParams, this.mBusinessType, rapduList, result, this.mCurrentTaskIndex));
    }

    private void sendApduToServer(String request) {
        LogC.d(getSubProcessPrefix() + TAG + BOUNDARY + "request:" + request, true);
        if (request != null) {
            try {
                this.mAsyncHttpClient.setProcessPrefix(getProcessPrefix(), null);
                this.mAsyncHttpClient.post(this.mContext, request, "text/json", this);
            } catch (Exception e) {
                setFailureResult(TSMOperateResponse.RETURN_UNKNOW_ERROR, e.getMessage());
            }
            this.mAsyncHttpClient.resetProcessPrefix();
        }
    }

    private void sendRequestToSmartCard(List<ApduCommand> commandList) {
        this.omaService.setProcessPrefix(getProcessPrefix(), null);
        TaskResult<ChannelID> result = this.omaService.excuteApduList(commandList, this.mChannelID);
        this.omaService.resetProcessPrefix();
        this.mChannelID = result.getData();
        this.mCurrentTaskIndex++;
        ApduCommand command = result.getLastExcutedCommand();
        int idx = command.getIndex();
        String rapdu = command.getRapdu();
        String sw = command.getSw();
        int resultCd = 0;
        if (result.getResultCode() != 0) {
            resultCd = 1;
            this.mErrorMessage = result.getMsg();
            LogC.i(getSubProcessPrefix() + TAG + " commands excuted failed. error code : " + result.getResultCode() + " msg : " + this.mErrorMessage, false);
            this.apduError = "resultCode_" + result.getResultCode() + "_idx_" + idx + "_rapdu_" + rapdu + "_sw_" + sw;
        }
        sendNextApdu(resultCd, idx, rapdu, sw);
    }

    private void clearData() {
        this.mCurrentTaskIndex = 1;
        this.mBusinessType = -1;
        this.mCommonRequestParams = null;
        this.mChannelID = null;
    }

    public void sendSuccessMessage(int statuCode, String responseString) {
        LogC.d(getSubProcessPrefix() + TAG + BOUNDARY + "response:" + responseString, true);
        try {
            BaseResponse<BaseBusinessForResp> response = (BaseResponse) new Gson().fromJson(responseString, TYPE_TOKEN.getType());
            if (response == null) {
                setFailureResult(TSMOperateResponse.RETURN_RESPONSE_PARSE_ERROR, "response data is empty");
                return;
            }
            BaseBusinessForResp business = response.getBusiness();
            if (!(business instanceof BaseBusinessForResp)) {
                setFailureResult(TSMOperateResponse.RETURN_RESPONSE_PARSE_ERROR, "Business is not BaseBusinessForResp");
                return;
            }
            BaseBusinessForResp resp = business;
            if (resp.getOperationResult() != 100000) {
                String operationDes = "tsm return code : " + result + " msg : " + resp.getOperationDes();
                if (!StringUtil.isEmpty(this.mErrorMessage, true)) {
                    operationDes = operationDes + ":" + this.mErrorMessage;
                    this.mErrorMessage = null;
                }
                setFailureResult(TSMOperateResponse.RETURN_SERVER_ERROR, operationDes);
                return;
            }
            if (resp.getFinishFlag() == 0) {
                setSuccessResult();
            } else {
                sendRequestToSmartCard(parseBean(resp.getCapduList()));
            }
        } catch (JsonSyntaxException e) {
            setFailureResult(TSMOperateResponse.RETURN_RESPONSE_PARSE_ERROR, "response data parse failure");
        }
    }

    public void sendFailureMessage(int statusCode, String httpResponse) {
        LogC.e(TAG, getSubProcessPrefix() + BOUNDARY + "onFailure response:", false);
        setFailureResult(TSMOperateResponse.RETURN_NETWORK_ERROR, httpResponse);
    }

    private List<ApduCommand> parseBean(List<ApduBean> beans) {
        List<ApduCommand> commands = new ArrayList<>();
        for (ApduBean bean : beans) {
            commands.add(parseBean(bean));
        }
        return commands;
    }

    private ApduCommand parseBean(ApduBean bean) {
        ApduCommand apdu = new ApduCommand();
        apdu.setIndex(bean.getIndex());
        apdu.setApdu(bean.getApdu());
        apdu.setChecker(bean.getSw());
        return apdu;
    }

    private void setSuccessResult() {
        this.omaService.setProcessPrefix(getProcessPrefix(), null);
        this.omaService.closeChannel(this.mChannelID);
        this.omaService.resetProcessPrefix();
        this.mOperatorResult = new TSMOperateResponse(TSMOperateResponse.TSM_OPERATE_RESULT_SUCCESS, "Success");
        clearData();
    }

    private void setFailureResult(int result, String msg) {
        this.omaService.setProcessPrefix(getProcessPrefix(), null);
        this.omaService.closeChannel(this.mChannelID);
        this.omaService.resetProcessPrefix();
        this.mOperatorResult = new TSMOperateResponse(result, msg, this.apduError);
        this.mOperatorResult.setOriResultCode(result);
        clearData();
    }
}
