package com.huawei.wallet.sdk.business.buscard.api;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.bankcard.util.EseTsmInitLoader;
import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.base.result.UninstallTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.base.util.Constant;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.TransferOutTrafficCardResultHandler;
import com.huawei.wallet.sdk.business.buscard.model.UninstallTrafficCardResultHandler;
import com.huawei.wallet.sdk.business.buscard.task.ApplyPayOrderTask;
import com.huawei.wallet.sdk.business.buscard.task.QueryOrdersTask;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardBaseTask;
import com.huawei.wallet.sdk.business.buscard.task.TransferOutTrafficCardTask;
import com.huawei.wallet.sdk.business.buscard.task.UninstallTrafficCardTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CardOperateLogic implements CardOperateLogicApi {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile CardOperateLogic instance;
    private Handler auxOperateHandler;
    private HandlerThread auxOperateHandlerThread;
    private final Context mContext;
    private final Handler operateHandler;
    private final Handler operateResultHandler = new Handler(this.mContext.getMainLooper());
    private final SPIOperatorManager operatorManager = new SPIOperatorManager(this.mContext, this.operateHandler);
    private final Handler queryTransferableHandler;
    private ConcurrentMap<String, TrafficCardBaseTask> runTasks;

    private CardOperateLogic(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread operateThread = new HandlerThread("card_operate_thread");
        operateThread.start();
        this.operateHandler = new Handler(operateThread.getLooper());
        HandlerThread queryTransferableThread = new HandlerThread("card_query_thread");
        queryTransferableThread.start();
        this.queryTransferableHandler = new Handler(queryTransferableThread.getLooper());
        this.runTasks = new ConcurrentHashMap(1);
    }

    private void startAuxOperateHandlerThread() {
        if (this.auxOperateHandlerThread == null) {
            this.auxOperateHandlerThread = new HandlerThread("aux_card_operate_thread");
            this.auxOperateHandlerThread.start();
            this.auxOperateHandler = new Handler(this.auxOperateHandlerThread.getLooper());
        } else if (!this.auxOperateHandlerThread.isAlive()) {
            this.auxOperateHandlerThread.start();
            this.auxOperateHandler = new Handler(this.auxOperateHandlerThread.getLooper());
        }
    }

    public static CardOperateLogic getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new CardOperateLogic(context);
                }
            }
        }
        return instance;
    }

    public void initEseInfo() {
        LogX.i("initEseInfo.");
        this.operateHandler.post(new EseTsmInitLoader(this.mContext));
    }

    public void applyPayOrder(String issuerId, ApplyOrderInfo applyOrderInfo) {
        Handler operateHandler2;
        if (applyOrderInfo.getOrderType() == 1 || applyOrderInfo.getOrderType() == 3) {
            startAuxOperateHandlerThread();
            operateHandler2 = this.auxOperateHandler;
        } else {
            operateHandler2 = this.operateHandler;
        }
        operateHandler2.post(new ApplyPayOrderTask(this.mContext, this.operatorManager, issuerId, applyOrderInfo));
    }

    public void uninstallTrafficCard(String issuerId, UninstallTrafficCardCallback callback, boolean updateTA, String source, String reason, String reasonCode, String accountType, String account) {
        StringBuilder sb = new StringBuilder();
        sb.append("CardOperateLogic uninstallTrafficCard, begin to uninstall traffic card, issuerId=");
        String str = issuerId;
        sb.append(str);
        sb.append(", source=");
        String str2 = source;
        sb.append(str2);
        LogX.w(sb.toString());
        UninstallTrafficCardTask uninstallTrafficCardTask = new UninstallTrafficCardTask(this.mContext, this.operatorManager, str, new UninstallTrafficCardResultHandler(this.operateResultHandler, callback), updateTA, str2, reason, reasonCode, accountType, account);
        this.operateHandler.post(uninstallTrafficCardTask);
    }

    public void queryOrder(String issuerId, int orderType) {
        this.operateHandler.post(new QueryOrdersTask(this.mContext, this.operatorManager, issuerId, orderType));
    }

    public void cloudTransferOut(String eventId, String mIssuerId, TransferOutTrafficCardCallback callback) {
        if (callback == null) {
            LogX.e("CardOperateLogic transferTrafficCard, null == callback");
            return;
        }
        String str = eventId;
        TransferOutTrafficCardTask transferOutTrafficCardTask = new TransferOutTrafficCardTask(this.mContext, str, this.operatorManager, mIssuerId, new TransferOutTrafficCardResultHandler(this.operateResultHandler, callback));
        transferOutTrafficCardTask.setIsFromCloudTransfer(true);
        this.operateHandler.post(transferOutTrafficCardTask);
    }

    public void removeTask() {
        this.runTasks.remove(Constant.RUN_TASK_KEY);
    }

    public void addTask(TrafficCardBaseTask task) {
        this.runTasks.put(Constant.RUN_TASK_KEY, task);
    }
}
