package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.api.WhiteCardOperateLogicApi;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.common.WhiteCardOperatorApi;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.common.WhiteCardOperatorImpl;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.task.DeleteWhiteCardTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.BaseResultHandler;
import com.huawei.wallet.sdk.common.apdu.base.BaseCallback;
import com.huawei.wallet.sdk.common.log.LogC;

public final class WhiteCardOperateLogic implements WhiteCardOperateLogicApi {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "WhiteCardOperateLogic";
    private static volatile WhiteCardOperateLogic instance;
    private final Context mContext;
    private final Handler operateHandler;
    private final Handler operateResultHandler = new Handler(this.mContext.getMainLooper());
    private WhiteCardOperatorApi operatorManager = new WhiteCardOperatorImpl(this.mContext);

    private WhiteCardOperateLogic(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread operateThread = new HandlerThread("white_card_operate_thread");
        operateThread.start();
        this.operateHandler = new Handler(operateThread.getLooper());
    }

    public static WhiteCardOperateLogic getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new WhiteCardOperateLogic(context);
                }
            }
        }
        return instance;
    }

    public void deleteWhiteCard(String passTypeId, String passId, String aid, BaseCallback callback) {
        if (callback == null) {
            LogX.w("WhiteCardOperateLogic deleteWhiteCard, null == callback");
            return;
        }
        LogC.i(TAG, "deleteWhiteCard: start-----", false);
        DeleteWhiteCardTask deleteWhiteCardTask = new DeleteWhiteCardTask(this.mContext, this.operatorManager, passTypeId, passId, aid, new BaseResultHandler(this.operateResultHandler, callback));
        this.operateHandler.post(deleteWhiteCardTask);
        LogC.i(TAG, "deleteWhiteCard: end-----", false);
    }
}
