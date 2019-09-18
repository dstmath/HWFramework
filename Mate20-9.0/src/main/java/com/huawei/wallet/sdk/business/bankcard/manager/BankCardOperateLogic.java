package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.bankcard.api.BankCardOperateLogicApi;
import com.huawei.wallet.sdk.business.bankcard.api.CUPOperationListener;
import com.huawei.wallet.sdk.business.bankcard.api.HandleCardOperateResultCallback;
import com.huawei.wallet.sdk.business.bankcard.task.HandleOperationResultTask;
import com.huawei.wallet.sdk.business.bankcard.util.EseTsmInitLoader;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;

public final class BankCardOperateLogic implements BankCardOperateLogicApi {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "BankCardOperateLogic|";
    private static volatile BankCardOperateLogic instance;
    private final Context mContext;
    private final Handler operateHandler;
    private final Handler operateResultHandler;
    private final BankSPIOperatorManager operatorManager;
    private String processPrefix = "";
    private String subProcessPrefix = "";

    private BankCardOperateLogic(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread operateThread = new HandlerThread("bank_card_operate_thread");
        operateThread.start();
        this.operateHandler = new Handler(operateThread.getLooper());
        this.operateResultHandler = new Handler(this.mContext.getMainLooper());
        this.operatorManager = new BankSPIOperatorManager(this.mContext, this.operateHandler);
    }

    public void initEseInfo() {
        LogC.i(this.subProcessPrefix + "initEseInfo. Post InitEse task to operateHandler.", false);
        EseTsmInitLoader eseInitLoader = new EseTsmInitLoader(this.mContext);
        eseInitLoader.setProcessPrefix(this.processPrefix, null);
        this.operateHandler.post(eseInitLoader);
    }

    public static BankCardOperateLogic getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new BankCardOperateLogic(context);
                }
            }
        }
        return instance;
    }

    public void registerCUPOperationListener(String event, String refId, CUPOperationListener listner) {
        LogC.i(this.subProcessPrefix + "registerCUPOperationListener, event: " + event + ",refId: " + refId, false);
        this.operatorManager.getCUPOperator().registerOperationListener(event, refId, listner);
    }

    public void unregisterCUPOperationListener(String event, String refId, CUPOperationListener listner) {
        LogC.i(this.subProcessPrefix + "unregisterCUPOperationListener, event: " + event + ",refId: " + refId, false);
        this.operatorManager.getCUPOperator().unregisterOperationListener(event, refId, listner);
    }

    public void notifyCUPCardOperation(String event, String ssid, String sign, List<String> refIds, HandleCardOperateResultCallback callback, String source) {
        LogC.i(this.subProcessPrefix + "notifyCUPCardOperation, event: " + event, false);
        if (StringUtil.isEmpty(event, true) || StringUtil.isEmpty(ssid, true) || StringUtil.isEmpty(sign, true)) {
            LogC.e(this.subProcessPrefix + "notifyCUPCardOperation, params is illegal.", false);
            callback.operateResultCallback(-99);
            return;
        }
        HandleOperationResultTask resultTask = new HandleOperationResultTask(this.operateResultHandler, callback);
        this.operatorManager.getCUPOperator().setProcessPrefix(this.processPrefix, null);
        this.operatorManager.getCUPOperator().notifyCUPCardOperation(event, ssid, sign, refIds, resultTask, source);
        this.operatorManager.getCUPOperator().resetProcessPrefix();
        notifyCardStateChange();
    }

    public void notifyCardStateChange() {
        this.operatorManager.getCUPOperator().setProcessPrefix(this.processPrefix, null);
        this.operatorManager.getCUPOperator().notifyCardState();
        this.operatorManager.getCUPOperator().resetProcessPrefix();
    }

    public void setProcessPrefix(String processPrefix2, String tag) {
        this.processPrefix = processPrefix2;
        this.subProcessPrefix = this.processPrefix + TAG;
    }

    public void resetProcessPrefix() {
        this.processPrefix = "";
        this.subProcessPrefix = "";
    }
}
