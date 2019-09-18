package com.huawei.wallet.sdk;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.api.HandleDeleteLocalCardsCallback;
import com.huawei.wallet.sdk.business.bankcard.task.CleanAllLocalBankCardsTask;
import com.huawei.wallet.sdk.business.buscard.BuscardCloudTransferHelper;
import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;
import com.huawei.wallet.sdk.business.clearssd.util.ClearSSDInterface;
import com.huawei.wallet.sdk.business.clearssd.util.OperateUtil;
import com.huawei.wallet.sdk.business.diploma.util.CertUtil;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.business.diploma.util.UploadCertInterface;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.ResetAccessAndIdCardsTask;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.TSMOperateCallback;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.BaseCommonContext;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.DicItem;
import com.huawei.wallet.sdk.common.utils.DicsQueryTask;
import com.huawei.wallet.sdk.common.utils.NfcUtil;
import com.huawei.wallet.sdk.common.utils.PropertyUtils;
import com.huawei.wallet.sdk.common.utils.QueryDicsRequset;
import com.huawei.wallet.sdk.common.utils.QueryDicsResponse;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public class WalletFactory {
    private static final int CHECKINTERVALE = 100;
    private static final int CHECKTIME = 20;
    private static final int CLEAR_BANKCARD_FAILED = 32;
    private static final int CLEAR_BUSCARD_FAILED = 64;
    private static final int CLEAR_IDCARD_FAILED = 128;
    private static final int CLEAR_SSD_FAILED = 16;
    private static final int DIPLOMA_UPLOAD_FAILED = 4;
    private static final int MSG_CHECK_RESET_PROGRESS = 1001;
    private static final int NO_ROUTER_INFO = 1;
    private static final int OPEN_NFC_END = 1;
    private static final int RESET_FLAG_REMOVE_FAILED = 8;
    private static final int RESET_FLAG_SET_FAILED = 2;
    private static final String TAG = "WalletFactoryReset";
    private static final int WALLET_BUSCARD_OP_SUCCESS = 0;
    private static final int WALLET_FACTORY_GET_ROUTER_FAIL = 1;
    private static final String WALLET_FACTORY_RESET_FIFTH = "5";
    private static final String WALLET_FACTORY_RESET_FIRST = "1";
    private static final String WALLET_FACTORY_RESET_FORTH = "4";
    private static final String WALLET_FACTORY_RESET_SECOND = "2";
    private static final String WALLET_FACTORY_RESET_THIRD = "3";
    private static final String WALLET_SDK_FLAG = "hw.wallet.reset_flag";
    private static final String WALLET_SDK_RESET_FAIL = "F";
    private static final String WALLET_SDK_RESET_INTERRUPT = "I";
    private static final String WALLET_SDK_RESET_SUCCESS = "S";
    private static final int WALLET_SSD_OP_SUCCESS = 0;
    private static volatile WalletFactory mInstance = null;
    /* access modifiers changed from: private */
    public String businessSwitch = "1000000000";
    private boolean isOpenNFCReadly = false;
    private WalletFactoryCallBack mCallback;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mStatus = 0;

    private WalletFactory(Context context, WalletFactoryCallBack callBack) {
        setFlagToProp("1");
        this.mContext = context;
        this.mCallback = callBack;
        BaseCommonContext.getInstance().initContext(context);
    }

    public static WalletFactory getInstance(Context context, WalletFactoryCallBack listener) {
        if (mInstance == null) {
            synchronized (WalletFactory.class) {
                if (mInstance == null) {
                    mInstance = new WalletFactory(context, listener);
                }
            }
        }
        return mInstance;
    }

    private void enableNFC() {
        if (!NfcUtil.isEnabledNFC(this.mContext)) {
            this.isOpenNFCReadly = false;
            boolean enableNFC = NfcUtil.enableNFC(this.mContext);
            LogC.i("WalletFactory|enableNFC: " + enableNFC, false);
        } else {
            this.isOpenNFCReadly = true;
        }
        LogC.i("WalletFactory|enableNFC", false);
    }

    /* access modifiers changed from: private */
    public void disableNFC() {
        if (NfcUtil.isEnabledNFC(this.mContext) && !this.isOpenNFCReadly) {
            boolean disableNFC = NfcUtil.disableNFC(this.mContext);
            LogC.i("WalletFactory|walletFactoryFailed|disableNFC: " + disableNFC, false);
        }
    }

    public void walletFactoryStart() {
        try {
            LogC.i("WalletFactory killBackgroundProcesses", false);
            ((ActivityManager) this.mContext.getSystemService("activity")).killBackgroundProcesses("com.huawei.wallet");
            LogC.i("WalletFactory killBackgroundProcesses end", false);
        } catch (SecurityException e) {
            LogC.i("WalletFactory killBackgroundProcesses SecurityException", false);
        }
        LogC.i("WalletFactory|walletFactoryFailed|mark to be try " + getResetFlag() + " time.", false);
        walletFactoryStartBusiness();
    }

    private boolean setFlagToProp(String value) {
        boolean result = PropertyUtils.setProperty(WALLET_SDK_FLAG, value);
        LogC.i("WalletFactory|setFlagToProp|set prop to be: " + value + ", result: " + result, false);
        return result;
    }

    private String getResetFlag() {
        String result = PropertyUtils.getProperty(WALLET_SDK_FLAG, "");
        LogC.i("WalletFactory|getResetFlag|result: " + result, false);
        return result;
    }

    public void walletFactoryStartBusiness() {
        ResetRoundOffThread.init();
        LogC.i("WalletFactory|walletFactoryStartBusiness|Send delay message to check result after 9 minutes 30 seconds.", false);
        ResetRoundOffThread.postDelayed(new Runnable() {
            public void run() {
                LogC.i("WalletFactory|walletFactoryStartBusiness|call checkResetProgress for round off.", false);
                WalletFactory.this.checkResetProgress();
            }
        }, 570000);
        String resetFlag = getResetFlag();
        if ("1".equals(resetFlag) || "2".equals(resetFlag) || "3".equals(resetFlag) || "4".equals(resetFlag) || "5".equals(resetFlag)) {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                public void run() {
                    CertUtil.uploadCert(WalletFactory.this.mContext, new UploadCertInterface() {
                        public void onEventBack(int code, String msg) {
                            switch (code) {
                                case 0:
                                    WalletFactory.this.clearSSD();
                                    break;
                                case 1:
                                    int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus | 4;
                                    WalletFactory.this.walletFactoryFailed(WalletFactory.this.mStatus);
                                    return;
                            }
                        }
                    });
                }
            });
            return;
        }
        LogC.i("WalletFactory|walletFactoryStartBusiness|no need to reset after check reset flag with result:" + resetFlag, false);
        walletFactorySuccess();
    }

    /* access modifiers changed from: private */
    public void startWalletBuiz() {
        if (isBankCardBusinessAvailable(this.businessSwitch)) {
            deleteBankCards();
        } else if (isPublicBusinessAvailable(this.businessSwitch)) {
            clearAccessAndIdCards();
        } else if (isBusCardBusinessAvailable(this.businessSwitch)) {
            clearBusCards();
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldStartWalletBuiz() {
        return isBankCardBusinessAvailable(this.businessSwitch) || isPublicBusinessAvailable(this.businessSwitch) || isBusCardBusinessAvailable(this.businessSwitch);
    }

    /* access modifiers changed from: private */
    public void clearSSD() {
        OperateUtil.delSSD(this.mContext, new ClearSSDInterface() {
            public void onEventBack(int code, String msg) {
                if (code == 0) {
                    LogC.i("WalletFactoryReset|clearSSD|WALLET_SSD_OP_SUCCESS", false);
                    int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus & -17;
                } else {
                    LogC.i("WalletFactoryReset|clearSSD|WALLET_SSD_OP_FAIL", false);
                    int unused2 = WalletFactory.this.mStatus = WalletFactory.this.mStatus | 16;
                }
                if (DiplomaUtil.getRouterInfo(WalletFactory.this.mContext) == 1) {
                    int unused3 = WalletFactory.this.mStatus = 1 | WalletFactory.this.mStatus;
                    WalletFactory.this.walletFactoryFailed(WalletFactory.this.mStatus);
                    return;
                }
                WalletFactory.this.getDicsFromServer();
                if (WalletFactory.this.shouldStartWalletBuiz()) {
                    WalletFactory.this.startWalletBuiz();
                } else {
                    WalletFactory.this.walletFactoryFinish();
                }
            }
        });
    }

    private void deleteBankCards() {
        enableNFC();
        Executors.newSingleThreadExecutor().submit(new CleanAllLocalBankCardsTask(this.mContext, new HandleDeleteLocalCardsCallback() {
            public void handleDeletelocalcardCallback(boolean isSuccess) {
                if (isSuccess) {
                    int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus & -33;
                } else {
                    int unused2 = WalletFactory.this.mStatus = WalletFactory.this.mStatus | 32;
                }
                WalletFactory.this.disableNFC();
                if (WalletFactory.this.isPublicBusinessAvailable(WalletFactory.this.businessSwitch)) {
                    WalletFactory.this.clearAccessAndIdCards();
                } else if (WalletFactory.this.isBusCardBusinessAvailable(WalletFactory.this.businessSwitch)) {
                    WalletFactory.this.clearBusCards();
                } else {
                    WalletFactory.this.walletFactoryFinish();
                }
            }
        }));
    }

    /* access modifiers changed from: private */
    public void clearAccessAndIdCards() {
        enableNFC();
        Executors.newSingleThreadExecutor().submit(new ResetAccessAndIdCardsTask(this.mContext, new TSMOperateCallback() {
            public void onSuccess(long elapse) {
                int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus & -129;
                WalletFactory.this.disableNFC();
                if (WalletFactory.this.isBusCardBusinessAvailable(WalletFactory.this.businessSwitch)) {
                    WalletFactory.this.clearBusCards();
                } else {
                    WalletFactory.this.walletFactoryFinish();
                }
            }

            public void onFail(int errorCode, String errorMsg, long elapse) {
                LogC.e("WalletFactoryResetreset idCard and accessCard failed, elapse=" + errorMsg, false);
                int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus | 128;
                WalletFactory.this.disableNFC();
                if (WalletFactory.this.isBusCardBusinessAvailable(WalletFactory.this.businessSwitch)) {
                    WalletFactory.this.clearBusCards();
                } else {
                    WalletFactory.this.walletFactoryFinish();
                }
            }
        }));
    }

    /* access modifiers changed from: private */
    public void clearBusCards() {
        BuscardCloudTransferHelper.transferBuscardToCloud(this.mContext, new TransferOutTrafficCardCallback() {
            public void transferOutCallback(int resultCode) {
                if (resultCode != 0) {
                    int unused = WalletFactory.this.mStatus = WalletFactory.this.mStatus | 64;
                } else {
                    int unused2 = WalletFactory.this.mStatus = WalletFactory.this.mStatus & -65;
                }
                WalletFactory.this.walletFactoryFinish();
            }
        });
    }

    /* access modifiers changed from: private */
    public void walletFactoryFinish() {
        if (this.mStatus == 0) {
            walletFactorySuccess();
        } else {
            walletFactoryFailed(this.mStatus);
        }
    }

    /* access modifiers changed from: private */
    public void walletFactoryFailed(int errorCode) {
        LogC.i("WalletFactory|walletFactoryFailed|wallet factory reset failed: " + errorCode, false);
        if ("5".equals(getResetFlag())) {
            setFlagToProp(WALLET_SDK_RESET_FAIL);
            LogC.i("WalletFactory|walletFactoryFailed|mark prop to be reset failed ", false);
            DiplomaUtil.unInitTA(this.mContext);
            LogC.i("WalletFactory|walletFactoryFailed|unInitTA success ", false);
            ResetRoundOffThread.destory();
            LogC.i("WalletFactory|walletFactoryFailed|clear delay message after failed.", false);
            LogC.i("WalletFactory|walletFactoryFailed|call onSuccess to finish.", false);
            this.mCallback.onSuccess();
            return;
        }
        updateRetryTimes();
        ResetRoundOffThread.destory();
        LogC.i("WalletFactory|walletFactoryFailed|clear delay message after failed.", false);
        LogC.i("WalletFactory|walletFactoryFailed|call onError to retry.", false);
        this.mCallback.onError(errorCode);
    }

    private void walletFactorySuccess() {
        LogC.i("WalletFactory|walletFactorySuccess|wallet factory reset success ", false);
        setFlagToProp(WALLET_SDK_RESET_SUCCESS);
        LogC.i("WalletFactory|walletFactorySuccess|mark prop to be reset success ", false);
        DiplomaUtil.unInitTA(this.mContext);
        LogC.i("WalletFactory|walletFactorySuccess|unInitTA success ", false);
        ResetRoundOffThread.destory();
        LogC.i("WalletFactory|walletFactorySuccess|clear delay message after success.", false);
        LogC.i("WalletFactory|walletFactorySuccess|call onSuccess to finish.", false);
        this.mCallback.onSuccess();
    }

    /* access modifiers changed from: private */
    public void getDicsFromServer() {
        DicsQueryTask dicsQueryTask = new DicsQueryTask(this.mContext, AddressNameMgr.getInstance().getAddress(AddressNameMgr.MODULE_NAME_WALLET, this.mContext));
        QueryDicsRequset request = new QueryDicsRequset();
        request.setDicName("RecoverSDKCfg");
        request.setItemName("RecoverSwitch");
        request.setIsNeedServiceTokenAuth(false);
        QueryDicsResponse response = (QueryDicsResponse) dicsQueryTask.processTask(request);
        String result = null;
        if (response == null || response.returnCode != 0) {
            LogC.w("WalletFactory|getDicsFromServer|response is null", false);
        } else if (response.dicItems == null || response.dicItems.size() < 1) {
            LogC.w("WalletFactory|getDicsFromServer|dicItems is null", false);
        } else {
            DicItem item = response.dicItems.get(0);
            String itemName = item.getParent();
            String dName = item.getName();
            String value = item.getValue();
            if (request.getDicName().equals(dName) && request.getItemName().equals(itemName) && !StringUtil.isEmpty(value, true)) {
                LogC.i("WalletFactoryReset queryLimitInfoFromServer end", false);
                try {
                    JSONObject jsonObj = new JSONObject(value);
                    if (jsonObj.has("business_switch")) {
                        result = jsonObj.getString("business_switch");
                    }
                    if (jsonObj.has("wallet_version")) {
                        jsonObj.getString("wallet_version");
                    }
                } catch (JSONException e) {
                    LogX.e("WalletFactory|getDicsFromServer|JSONException", false);
                }
            }
            if (!TextUtils.isEmpty(result)) {
                LogC.i("WalletFactory|getDicsFromServer|result: " + result, false);
                this.businessSwitch = result;
            }
        }
    }

    private boolean isSSDBusinessAvailable(String businessSwitch2) {
        LogC.i("WalletFactory|isSSDBusinessAvailable|businessSwitch: " + businessSwitch2, false);
        if (TextUtils.isEmpty(businessSwitch2) || businessSwitch2.length() < 1) {
            return true;
        }
        return TextUtils.equals(String.valueOf(businessSwitch2.charAt(0)), "1");
    }

    private boolean isBankCardBusinessAvailable(String businessSwitch2) {
        LogC.i("WalletFactory|isBankCardBusinessAvailable|businessSwitch: " + businessSwitch2, false);
        if (TextUtils.isEmpty(businessSwitch2) || businessSwitch2.length() < 2) {
            return false;
        }
        return TextUtils.equals(String.valueOf(businessSwitch2.charAt(1)), "1");
    }

    public boolean isBusCardBusinessAvailable(String businessSwitch2) {
        LogC.i("WalletFactory|isBusCardBusinessAvailable|businessSwitch: " + businessSwitch2, false);
        boolean available = false;
        if (TextUtils.isEmpty(businessSwitch2) || businessSwitch2.length() < 7) {
            return false;
        }
        if (!TextUtils.equals(String.valueOf(businessSwitch2.charAt(6)), "0")) {
            available = true;
        }
        return available;
    }

    public boolean isNotSupportTranfserBusCardDelete() {
        LogC.i("WalletFactory|isNotSupportTranfserBusCardDelete|businessSwitch: " + this.businessSwitch, false);
        boolean available = false;
        if (TextUtils.isEmpty(this.businessSwitch) || this.businessSwitch.length() < 7) {
            return false;
        }
        if (TextUtils.equals(String.valueOf(this.businessSwitch.charAt(6)), "2")) {
            available = true;
        }
        return available;
    }

    /* access modifiers changed from: private */
    public boolean isPublicBusinessAvailable(String businessSwitch2) {
        LogC.i("WalletFactory|isPublicBusinessAvailable|businessSwitch: " + businessSwitch2, false);
        boolean available = false;
        if (TextUtils.isEmpty(businessSwitch2) || businessSwitch2.length() < 3) {
            return false;
        }
        if (isEIDNeedClean() || isCTIDNeedClean() || isAccessCardNeedClean() || isBlankCardNeedClean()) {
            available = true;
        }
        return available;
    }

    public boolean isEIDNeedClean() {
        LogC.i("WalletFactory|isEIDNeedClean|businessSwitch: " + this.businessSwitch, false);
        boolean isEIDNeedClean = false;
        if (TextUtils.isEmpty(this.businessSwitch) || this.businessSwitch.length() < 3) {
            LogC.i("No configuration or length less than 3:" + false, false);
            return false;
        }
        if (TextUtils.equals(String.valueOf(this.businessSwitch.charAt(2)), "1")) {
            isEIDNeedClean = true;
        }
        LogC.i("isEIDNeedClean:" + isEIDNeedClean, false);
        return isEIDNeedClean;
    }

    public boolean isCTIDNeedClean() {
        LogC.i("WalletFactory|isCTIDNeedClean|businessSwitch: " + this.businessSwitch, false);
        boolean isCTIDNeedClean = false;
        if (TextUtils.isEmpty(this.businessSwitch) || this.businessSwitch.length() < 4) {
            LogC.i("No configuration or length less than 4:" + false, false);
            return false;
        }
        if (TextUtils.equals(String.valueOf(this.businessSwitch.charAt(3)), "1")) {
            isCTIDNeedClean = true;
        }
        LogC.i("isCTIDNeedClean:" + isCTIDNeedClean, false);
        return isCTIDNeedClean;
    }

    public boolean isAccessCardNeedClean() {
        LogC.i("WalletFactory|isAccessCardNeedClean|businessSwitch: " + this.businessSwitch, false);
        boolean isAccessCardNeedClean = false;
        if (TextUtils.isEmpty(this.businessSwitch) || this.businessSwitch.length() < 5) {
            LogC.i("No configuration or length less than 5:" + false, false);
            return false;
        }
        if (TextUtils.equals(String.valueOf(this.businessSwitch.charAt(4)), "1")) {
            isAccessCardNeedClean = true;
        }
        LogC.i("isAccessCardNeedClean:" + isAccessCardNeedClean, false);
        return isAccessCardNeedClean;
    }

    public boolean isBlankCardNeedClean() {
        LogC.i("WalletFactory|isBlankCardNeedClean|businessSwitch: " + this.businessSwitch, false);
        boolean isBlankCardNeedClean = false;
        if (TextUtils.isEmpty(this.businessSwitch) || this.businessSwitch.length() < 6) {
            LogC.i("No configuration or length less than 6:" + false, false);
            return false;
        }
        if (TextUtils.equals(String.valueOf(this.businessSwitch.charAt(5)), "1")) {
            isBlankCardNeedClean = true;
        }
        LogC.i("isBlankCardNeedClean:" + isBlankCardNeedClean, false);
        return isBlankCardNeedClean;
    }

    /* access modifiers changed from: private */
    public void checkResetProgress() {
        String resetResult = getResetFlag();
        if (WALLET_SDK_RESET_SUCCESS.equals(resetResult) || WALLET_SDK_RESET_FAIL.equals(resetResult)) {
            LogC.i(TAG, "WalletFactory|checkResetProgress|Clean card finished with result:" + resetResult, false);
            DiplomaUtil.unInitTA(this.mContext);
            LogC.i("WalletFactory|checkResetProgress|unInitTA success ", false);
            if (this.mCallback != null) {
                LogC.i(TAG, "WalletFactory|checkResetProgress|call onSuccess function to finish reset in checkResetProgress.", false);
                this.mCallback.onSuccess();
            }
        } else if ("5".equals(resetResult)) {
            setFlagToProp(WALLET_SDK_RESET_INTERRUPT);
            LogC.i("WalletFactory|checkResetProgress|mark prop to be reset interrupt.", false);
            DiplomaUtil.unInitTA(this.mContext);
            LogC.i("WalletFactory|checkResetProgress|unInitTA success ", false);
            if (this.mCallback != null) {
                LogC.i(TAG, "WalletFactory|checkResetProgress|call onSuccess function to finish reset in checkResetProgress.", false);
                this.mCallback.onSuccess();
            }
        } else {
            updateRetryTimes();
            LogC.i("WalletFactory|checkResetProgress|call onError to retry.", false);
            this.mCallback.onError(-1);
        }
        ResetRoundOffThread.destory();
        LogC.i("WalletFactory|checkResetProgress|clear delay message after failed.", false);
    }

    private void updateRetryTimes() {
        String resetFlag = getResetFlag();
        if ("1".equals(resetFlag)) {
            setFlagToProp("2");
        } else if ("2".equals(resetFlag)) {
            setFlagToProp("3");
        } else if ("3".equals(resetFlag)) {
            setFlagToProp("4");
        } else if ("4".equals(resetFlag)) {
            setFlagToProp("5");
        }
    }
}
