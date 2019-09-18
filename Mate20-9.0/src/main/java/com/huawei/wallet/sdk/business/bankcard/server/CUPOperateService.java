package com.huawei.wallet.sdk.business.bankcard.server;

import android.content.Context;
import android.content.Intent;
import com.huawei.wallet.sdk.business.bankcard.api.HandleCardOperateResultCallback;
import com.huawei.wallet.sdk.business.bankcard.manager.BankCardOperateLogic;
import com.huawei.wallet.sdk.business.bankcard.manager.BankCardOperateServiceManager;
import com.huawei.wallet.sdk.business.bankcard.util.SecureCommonUtil;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.NfcUtil;
import java.util.ArrayList;

public class CUPOperateService implements HandleCardOperateResultCallback {
    public static final String SERVICE_ACTION_CUP_OPERATE = "com.huawei.wallet.nfc.CUP_CARD_OPERATE_SERVICE";
    public static final String SERVICE_ACTION_GET_TRANSACTION_DETAIL_RESULT = "com.huawei.wallet.nfc.GET_TRANSACTION_DETAIL_RESULT_HANDLE_SERVICE";
    public static final String SERVICE_ACTION_PERSONALIZED_RESULT = "com.huawei.wallet.nfc.PERSONAL_RESULT_HANDLE_SERVICE";
    public static final String SERVICE_INTENT_KEY_AID = "aid";
    public static final String SERVICE_INTENT_KEY_CPLC = "cplc";
    public static final String SERVICE_INTENT_KEY_EVENT = "event";
    public static final String SERVICE_INTENT_KEY_PUSHTIME = "pushTime";
    public static final String SERVICE_INTENT_KEY_REFID = "ref_id";
    public static final String SERVICE_INTENT_KEY_REFIDS = "ref_ids";
    public static final String SERVICE_INTENT_KEY_SIGN = "sign";
    private static final String SERVICE_INTENT_KEY_SOURCE = "source";
    public static final String SERVICE_INTENT_KEY_SSID = "ssid";
    public static final String SERVICE_INTENT_KEY_TOKENID = "tokenId";
    public static final String SERVICE_INTENT_KEY_TOKENTYPE = "tokenType";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile CUPOperateService instance;
    private Context mContext = null;
    private String procesPrefix = "";
    private int serviceStartedTimes = 0;
    private String walletProcessStartTimestamp = "";

    public CUPOperateService(Context context) {
        this.mContext = context;
    }

    public static CUPOperateService getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new CUPOperateService(context);
                }
            }
        }
        return instance;
    }

    public void startCommand(Intent intent) {
        LogC.d("CUPOperateService onStartCommand", false);
        addOneServiceTask();
        if (!NfcUtil.isEnabledNFC(this.mContext)) {
            LogC.i("isEnabledNFC: false", false);
            finishOneServiceTask();
            return;
        }
        String action = intent.getAction();
        LogC.i(this.procesPrefix + "Start CUPOperateService, action " + action, false);
        if (SERVICE_ACTION_CUP_OPERATE.equals(action)) {
            String event = SecureCommonUtil.getStringExtra(intent, "event");
            String ssid = SecureCommonUtil.getStringExtra(intent, "ssid");
            String sign = SecureCommonUtil.getStringExtra(intent, "sign");
            String source = SecureCommonUtil.getStringExtra(intent, SERVICE_INTENT_KEY_SOURCE);
            ArrayList<String> stringArrayListExtra = SecureCommonUtil.getStringArrayListExtra(intent, SERVICE_INTENT_KEY_REFIDS);
            BankCardOperateLogic.getInstance(this.mContext).setProcessPrefix(this.procesPrefix, null);
            BankCardOperateLogic.getInstance(this.mContext).notifyCUPCardOperation(event, ssid, sign, stringArrayListExtra, this, source);
            BankCardOperateLogic.getInstance(this.mContext).resetProcessPrefix();
        } else {
            LogC.e(this.procesPrefix + "action error.", false);
            finishOneServiceTask();
        }
    }

    private void addOneServiceTask() {
        this.serviceStartedTimes++;
    }

    private void finishOneServiceTask() {
        this.serviceStartedTimes--;
        if (this.serviceStartedTimes == 0) {
            BankCardOperateServiceManager.releaseOperateWakeLock();
        }
    }

    public void operateResultCallback(int resultCode) {
        finishOneServiceTask();
    }
}
