package com.huawei.wallet.sdk.business.bankcard.server;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.api.CUPService;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.EMUIBuildUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.unionpay.tsmservice.ITsmCallback;
import com.unionpay.tsmservice.UPTsmAddon;
import com.unionpay.tsmservice.data.Constant;
import com.unionpay.tsmservice.request.ExecuteCmdRequestParams;
import com.unionpay.tsmservice.request.GetSeIdRequestParams;
import com.unionpay.tsmservice.result.GetSeIdResult;
import java.util.List;

public class CUPServiceImp implements CUPService, UPTsmAddon.UPTsmConnectionListener {
    private static final int BINDING_TIME_OUT = 2000;
    private static final String CUP_TSM_APP_PACKAGE_NAME = "com.unionpay.tsmservice";
    public static final String CUP_TSM_LAST_PUSH_TIME = "CUPTransactionLastPushTime";
    public static final String CUP_TSM_LAST_UPDATE_TAG = "CUPTransactionLastUpdateTag";
    public static final String RECEIVE_PUSH_TOKENTYPE_INFO = "03";
    public static final int SEID_ERROR = 1004300004;
    private static final String TAG = "CUPServiceImp|";
    private final TsmCallback initResult;
    private final CUPResponseCodeInterpreter interpreter;
    /* access modifiers changed from: private */
    public final Object lock = new Object();
    private final Context mContext;
    private String mLastUpdateTag = Constant.EMPTY_TAG;
    private final UPTsmAddon mUPTsmAddon;
    /* access modifiers changed from: private */
    public String processPrefix = "";

    class TsmCallback extends ITsmCallback.Stub {
        private ClassLoader mClassLoader;
        String mErrorMsg;
        int resultCode;
        Object resultObject;

        TsmCallback() {
        }

        TsmCallback(ClassLoader classLoader) {
            this.mClassLoader = classLoader;
        }

        public void onError(String errorCode, String errorMsg) throws RemoteException {
            LogC.i(CUPServiceImp.this.processPrefix + "UPTsmAddon onError, repairCode: " + errorCode + " ,errorMsg: " + errorMsg + " ,threadId: " + Thread.currentThread().getId(), false);
            synchronized (CUPServiceImp.this.lock) {
                this.resultCode = Integer.parseInt(errorCode);
                this.mErrorMsg = errorMsg;
                CUPServiceImp.this.lock.notifyAll();
            }
        }

        public void onResult(Bundle result) throws RemoteException {
            LogC.i(CUPServiceImp.this.processPrefix + "UPTsmAddon onResult, threadId: " + Thread.currentThread().getId(), false);
            synchronized (CUPServiceImp.this.lock) {
                this.resultCode = EMUIBuildUtil.VERSION_CODES.CUR_DEVELOPMENT;
                if (this.mClassLoader != null) {
                    result.setClassLoader(this.mClassLoader);
                    this.resultObject = result.get("result");
                }
                CUPServiceImp.this.lock.notifyAll();
            }
        }
    }

    public CUPServiceImp(Context context) {
        this.mContext = context;
        this.interpreter = new CUPResponseCodeInterpreter(this.mContext);
        this.initResult = new TsmCallback();
        this.mUPTsmAddon = UPTsmAddon.getInstance(this.mContext);
        this.mUPTsmAddon.addConnectionListener(this);
    }

    public int init() {
        int responseCode;
        synchronized (this.lock) {
            LogC.i(this.processPrefix + "initService now.", false);
            int isInitResult = initUPTsmAddon();
            if (isInitResult != 0) {
                LogC.d(this.processPrefix + "initService, init up tsm addon failed, init result " + isInitResult, false);
                return isInitResult;
            }
            responseCode = -99;
            try {
                if (this.initResult.resultCode != 10000) {
                    this.initResult.resultCode = 0;
                    LogC.i(this.processPrefix + "Start to init UPTsmAddon.", false);
                    this.mUPTsmAddon.init(null, this.initResult);
                    while (this.initResult.resultCode == 0 && this.mUPTsmAddon.isConnected()) {
                        LogC.d(this.processPrefix + "Wait for init UPTsmAddon,  threadId " + Thread.currentThread().getId() + " ,wait " + 2000 + "ms.", false);
                        this.lock.wait(2000);
                    }
                }
                if (this.mUPTsmAddon.isConnected()) {
                    responseCode = this.interpreter.translateReponseCode(this.initResult.resultCode, "initService", this.initResult.mErrorMsg);
                } else {
                    LogC.e(this.processPrefix + "Init UPTsmAddon failed, UPTsmAddon is not connected.", false);
                    responseCode = this.interpreter.translateReponseCode(99999, "initService", null);
                }
                LogC.i(this.processPrefix + "Init UPTsmAddon end, responseCode " + responseCode, false);
            } catch (RemoteException e) {
                this.processPrefix + "Init UPTsmAddon failed " + e.getMessage();
            } catch (InterruptedException e2) {
                this.processPrefix + "Init UPTsmAddon failed " + e2.getMessage();
            }
        }
        return responseCode;
    }

    public int excuteCMD(String ssid, String sign) {
        synchronized (this.lock) {
            LogC.i(this.processPrefix + "excuteCMD", false);
            if (!StringUtil.isEmpty(ssid, true)) {
                if (!StringUtil.isEmpty(sign, true)) {
                    int initResponseCode = init();
                    if (initResponseCode != 0) {
                        LogC.d(this.processPrefix + "excuteCMD, init response error code: " + initResponseCode, false);
                        return initResponseCode;
                    }
                    int responseCode = -99;
                    try {
                        ExecuteCmdRequestParams params = new ExecuteCmdRequestParams();
                        params.setSign(sign);
                        params.setSsid(ssid);
                        TsmCallback excuteCmdCallback = new TsmCallback();
                        LogC.i(this.processPrefix + "excuteCMD,before OMA_ACCESS_SYNC_LOCK", false);
                        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                            this.mUPTsmAddon.executeCmd(params, excuteCmdCallback, null);
                            LogC.i(this.processPrefix + "excuteCMD,in OMA_ACCESS_SYNC_LOCK", false);
                            while (this.mUPTsmAddon.isConnected() && excuteCmdCallback.resultCode == 0) {
                                LogC.d(this.processPrefix + "excuteCMD threadId: " + Thread.currentThread().getId(), false);
                                this.lock.wait();
                            }
                        }
                        LogC.i(this.processPrefix + "excuteCMD,after OMA_ACCESS_SYNC_LOCK", false);
                        if (this.mUPTsmAddon.isConnected()) {
                            responseCode = this.interpreter.translateReponseCode(excuteCmdCallback.resultCode, "excuteCMD", excuteCmdCallback.mErrorMsg);
                        } else {
                            LogC.e(this.processPrefix + "cup tsm service killed, when excuteCMDing.", false);
                            responseCode = this.interpreter.translateReponseCode(99999, "excuteCMD", null);
                        }
                    } catch (RemoteException e) {
                        this.processPrefix + "excuteCMD, remote exception: " + e.getMessage();
                    } catch (InterruptedException e2) {
                        this.processPrefix + "excuteCMD, interrupted exception: " + e2.getMessage();
                    }
                    LogC.i(this.processPrefix + "excuteCMD responseCode: " + responseCode, false);
                    return responseCode;
                }
            }
            LogC.d(this.processPrefix + "excuteCMD, params illegal.", false);
            return -2;
        }
    }

    private int initUPTsmAddon() {
        synchronized (this.lock) {
            if (!PackageUtil.isAppInstalled(this.mContext, "com.unionpay.tsmservice")) {
                LogC.e(this.processPrefix + "initUPTsmAddon, but package is not installed.", false);
                return -1;
            }
            while (!this.mUPTsmAddon.isConnected()) {
                LogC.i(this.processPrefix + "Start to bind UPTsmAddon.", false);
                if (!this.mUPTsmAddon.bind()) {
                    LogC.d(this.processPrefix + "Bind UPTsmAddon failed.", false);
                    return -5;
                }
                try {
                    LogC.d(this.processPrefix + "Init UPTsmAddon, threadId " + Thread.currentThread().getId() + ", wait " + 2000 + "ms", false);
                    this.lock.wait(2000);
                    if (!this.mUPTsmAddon.isConnected()) {
                        return -5;
                    }
                } catch (InterruptedException e) {
                    LogC.e(this.processPrefix + "Init UPTsmAddon end with interruptedException " + e.getMessage(), false);
                }
            }
            return 0;
        }
    }

    public void onTsmConnected() {
        LogC.i(this.processPrefix + "UPTsmAddon connected, threadId: " + Thread.currentThread().getId(), false);
        synchronized (this.lock) {
            this.lock.notifyAll();
        }
    }

    public void onTsmDisconnected() {
        LogC.i(this.processPrefix + "UPTsmAddon disconnected, threadId: " + Thread.currentThread().getId(), false);
        synchronized (this.lock) {
            this.initResult.resultCode = 99999;
            this.lock.notifyAll();
        }
    }

    public void notifyCardState() {
        synchronized (this.lock) {
            LogC.i(this.processPrefix + "notifyCardState", false);
            int initResponseCode = init();
            if (initResponseCode != 0) {
                LogC.i(this.processPrefix + "notifyCardState  init cup servers erro  and code is :" + initResponseCode, false);
                return;
            }
            try {
                this.mUPTsmAddon.cardListStatusChanged(null, new TsmCallback());
            } catch (RemoteException e) {
                LogC.e(this.processPrefix + "notifyCardState, RemoteException, " + e.getMessage(), false);
            }
        }
    }

    private boolean isPaymentCodeTopStack() {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
            for (ActivityManager.RunningTaskInfo info : runningTaskInfos) {
                if (TextUtils.equals("com.huawei.wallet.nfc.bankcard.paymentcode.ui.PaymentCodeActivity", info.topActivity.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSeidForCMB() {
        synchronized (this.lock) {
            LogC.i("getSeidForCMB", false);
            int initResponseCode = init();
            if (initResponseCode != 0) {
                LogC.i("notifyCardState  init cup servers erro  and code is :" + initResponseCode, false);
                return null;
            }
            try {
                TsmCallback callback = new TsmCallback(GetSeIdResult.class.getClassLoader());
                this.mUPTsmAddon.getSeId(new GetSeIdRequestParams(), callback);
                while (this.mUPTsmAddon.isConnected() && callback.resultCode == 0) {
                    LogC.d("excuteCMD threadId: " + Thread.currentThread().getId(), false);
                    this.lock.wait(2000);
                }
                if (this.mUPTsmAddon.isConnected()) {
                    LogC.d("getSeidForCMB isConnected: true ", false);
                    if (callback.resultCode == 10000 && callback.resultObject != null) {
                        String id = ((GetSeIdResult) callback.resultObject).getSeId();
                        LogC.i("getSeidForCMB getSeId success", false);
                        return id;
                    }
                } else {
                    LogC.d("getSeidForCMB isConnected: false ", false);
                    return null;
                }
            } catch (RemoteException e) {
                LogC.e("getSeidForCMB, RemoteException.", false);
            } catch (Exception e2) {
                LogC.e("getSeidForCMB, Exception.", false);
            }
        }
        return null;
    }

    public void setProcessPrefix(String processPrefix2, String tag) {
        this.processPrefix = processPrefix2 + TAG;
    }

    public void resetProcessPrefix() {
        this.processPrefix = "";
    }
}
