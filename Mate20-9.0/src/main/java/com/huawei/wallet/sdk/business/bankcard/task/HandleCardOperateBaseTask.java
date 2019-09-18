package com.huawei.wallet.sdk.business.bankcard.task;

import android.os.Handler;
import com.huawei.wallet.sdk.business.bankcard.api.CUPService;
import com.huawei.wallet.sdk.business.bankcard.api.CardOperateListener;
import com.huawei.wallet.sdk.business.bankcard.constant.BankcardConstant;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.List;

public abstract class HandleCardOperateBaseTask extends WalletProcessTrace implements Runnable {
    private static final String TAG = "HandleCardOperateBaseTa|";
    private final CardOperateListener mListener;
    private List<String> mRefIds;
    private final HandleOperationResultTask mResultTask;
    private final CUPService mServiceApi;
    protected String mSign;
    protected String mSsid;

    /* access modifiers changed from: protected */
    public abstract String getOperateEventTag();

    /* access modifiers changed from: protected */
    public abstract void handleFailResult(List<String> list, int i);

    /* access modifiers changed from: protected */
    public abstract boolean handleSuccessResult(List<String> list);

    /* access modifiers changed from: protected */
    public abstract boolean isOperationSatisfied(List<String> list);

    /* access modifiers changed from: protected */
    public abstract boolean prepareLocalInfo(List<String> list);

    public HandleCardOperateBaseTask(CUPService service, HandleOperationResultTask resulthandleTask, CardOperateListener listener) {
        this.mServiceApi = service;
        this.mResultTask = resulthandleTask;
        this.mListener = listener;
    }

    public void doTask(String ssid, String sign, List<String> refIds, Handler excuteHandler) {
        this.mSsid = ssid;
        this.mSign = sign;
        this.mRefIds = refIds;
        excuteHandler.post(this);
    }

    public void run() {
        boolean isSatisfied = isOperationSatisfied(this.mRefIds);
        LogC.d(getSubProcessPrefix() + "the operation task isSatisfied: " + isSatisfied, false);
        if (!isSatisfied) {
            notifyListenerResult(-99);
            this.mResultTask.notifyOperateResult(-99);
            return;
        }
        boolean isPrepareSuccess = prepareLocalInfo(this.mRefIds);
        LogC.d(getSubProcessPrefix() + "the operation task isPrepareSuccess: " + isPrepareSuccess, false);
        if (!isPrepareSuccess) {
            notifyListenerResult(-99);
            this.mResultTask.notifyOperateResult(-99);
            return;
        }
        notifyListenerStart();
        LogC.i(getSubProcessPrefix() + "excute cup cmd now.", false);
        this.mServiceApi.setProcessPrefix(getProcessPrefix(), null);
        int excuteResult = this.mServiceApi.excuteCMD(this.mSsid, this.mSign);
        this.mServiceApi.resetProcessPrefix();
        LogC.i(getSubProcessPrefix() + "excute cup cmd result: " + excuteResult, false);
        if (excuteResult == 0) {
            boolean isUpdateSuccess = handleSuccessResult(this.mRefIds);
            LogC.d(getSubProcessPrefix() + "the operation task isUpdateSuccess: " + isUpdateSuccess, false);
            notifyListenerResult(0);
            this.mResultTask.notifyOperateResult(0);
        } else {
            int failResutl = -99;
            if (-7 == excuteResult) {
                failResutl = -1;
            }
            handleFailResult(this.mRefIds, excuteResult);
            notifyListenerResult(failResutl);
            this.mResultTask.notifyOperateResult(-99);
        }
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }

    private void notifyListenerResult(int resultCode) {
        if (!(this.mListener == null || this.mRefIds == null)) {
            String mEvent = getOperateEventTag();
            if (BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(mEvent)) {
                this.mListener.operateFinished(mEvent, this.mRefIds.get(0), resultCode);
                return;
            }
            for (String refId : this.mRefIds) {
                this.mListener.operateFinished(mEvent, refId, resultCode);
            }
        }
    }

    private void notifyListenerStart() {
        if (!(this.mListener == null || this.mRefIds == null)) {
            String mEvent = getOperateEventTag();
            if (BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(mEvent)) {
                this.mListener.operateStart(mEvent, this.mRefIds.get(0));
                return;
            }
            for (String refId : this.mRefIds) {
                this.mListener.operateStart(mEvent, refId);
            }
        }
    }
}
