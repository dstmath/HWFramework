package com.huawei.wallet.sdk.business.idcard.accesscard.logic;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperateLogicApi;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.impl.CommonAccessCardOperatorImpl;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.InitAccessCardOperatorCallback;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.NullifyCardResultCallback;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.HandleNullifyResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.InitAccessCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.task.DeleteAccessCardResultTask;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.task.InitAccessCardTask;
import com.huawei.wallet.sdk.business.idcard.accesscard.util.Constants;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;

public final class AccessCardOperateLogic implements AccessCardOperateLogicApi {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "AccessCardOperateLogic";
    private static volatile AccessCardOperateLogic instance;
    private final Context mContext;
    private final Handler operateHandler;
    private final Handler operateResultHandler = new Handler(this.mContext.getMainLooper());
    private AccessCardOperator operatorManager = new CommonAccessCardOperatorImpl(this.mContext);

    private AccessCardOperateLogic(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread operateThread = new HandlerThread("access_card_operate_thread");
        operateThread.start();
        this.operateHandler = new Handler(operateThread.getLooper());
    }

    public static AccessCardOperateLogic getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new AccessCardOperateLogic(context);
                }
            }
        }
        return instance;
    }

    public void initAccessCard(List<TACardInfo> list, InitAccessCardOperatorCallback callback) {
        LogX.i("initAccessCardOperator");
        List<TACardInfo> list2 = list;
        InitAccessCardTask initAccessCardTask = new InitAccessCardTask(this.mContext, list2, Constants.ACCESSCARD_ISSUREID, this.operatorManager, new InitAccessCardResultHandler(this.operateResultHandler, callback));
        this.operateHandler.post(initAccessCardTask);
    }

    public void uninstallAccessCard(String issuerId, boolean updateTA, String aid, NullifyCardResultCallback callback) {
        LogX.w("CardOperateLogic uninstallAccessCard, begin to uninstall access card, issuerId=" + issuerId + ", aid=" + aid);
        StringBuilder sb = new StringBuilder();
        sb.append("nullifyCard now,issuerId: ");
        sb.append(issuerId);
        LogX.i(sb.toString());
        if (callback == null) {
            LogX.e("nullifyCard, callback is illegal.");
        } else if (StringUtil.isEmpty(issuerId, true) || StringUtil.isEmpty(aid, true)) {
            LogX.e("nullifyCard, params is illegal.");
            callback.nullifyResultCallback(-1);
        } else {
            DeleteAccessCardResultTask deleteAccessCardResultTask = new DeleteAccessCardResultTask(this.mContext, this.operatorManager, issuerId, aid, new HandleNullifyResultHandler(this.operateResultHandler, callback), callback);
            this.operateHandler.post(deleteAccessCardResultTask);
        }
    }
}
