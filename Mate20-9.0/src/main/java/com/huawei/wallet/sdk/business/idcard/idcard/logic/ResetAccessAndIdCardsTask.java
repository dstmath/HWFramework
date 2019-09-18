package com.huawei.wallet.sdk.business.idcard.idcard.logic;

import android.content.Context;
import com.huawei.wallet.sdk.WalletFactory;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.AccessCardOperateLogic;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.InitAccessCardOperatorCallback;
import com.huawei.wallet.sdk.business.idcard.idcard.api.CtidApi;
import com.huawei.wallet.sdk.business.idcard.idcard.api.EidApi;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.WhiteCardOperateLogic;
import com.huawei.wallet.sdk.common.apdu.base.BaseCallback;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResetAccessAndIdCardsTask implements Runnable {
    private static final String RESET_FAIL = "fail";
    private static final String RESET_FIRST_FAIL = "firstFail";
    private static final String RESET_INIT = "";
    private static final String RESET_SUCCESS = "success";
    private static final String TAG = "IDCard:ResetAccessAndIdCardsTask";
    private TSMOperateCallback mCallback;
    private final Context mContext;
    private long mElapse;
    /* access modifiers changed from: private */
    public String resetAccessCardResult = "";
    private String resetBlankCardResult = "";
    private String resetIdCardResult = "";

    public ResetAccessAndIdCardsTask(Context context, TSMOperateCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    private void calElapse(long startTime) {
        this.mElapse = (System.currentTimeMillis() - startTime) / 1000;
    }

    public void run() {
        LogC.i(TAG, "Enter ResetAccessAndIdCardsTask", false);
        resetIdCard(System.currentTimeMillis());
        LogC.i(TAG, "End to resetIdCard", false);
        resetAccessCard();
    }

    private void resetIdCard(long startTime) {
        LogC.i(TAG, "Begin to resetIdCard", false);
        int i = 1;
        while (i <= 2) {
            boolean isCtidCardClean = true;
            boolean isEidCardClean = true;
            List<TACardInfo> taCardInfoList = WalletTaManager.getInstance(this.mContext).getCardList();
            if (taCardInfoList != null && taCardInfoList.size() > 0) {
                for (TACardInfo cardinfo : taCardInfoList) {
                    int groupType = cardinfo.getCardGroupType();
                    String issuerId = cardinfo.getIssuerId();
                    if (CtidApi.isCtid(groupType, issuerId, this.mContext) && WalletFactory.getInstance(this.mContext, null).isCTIDNeedClean()) {
                        LogC.i(TAG, "try to deleteCtid", false);
                        isCtidCardClean = CtidApi.handlerDeviceReset(this.mContext);
                    } else if (EidApi.isEid(groupType, issuerId, this.mContext) && WalletFactory.getInstance(this.mContext, null).isEIDNeedClean()) {
                        LogC.i(TAG, "try to deleteEid", false);
                        isEidCardClean = EidApi.handlerDeviceReset(this.mContext);
                    }
                }
            }
            if (!isCtidCardClean || !isEidCardClean) {
                if (i < 2) {
                    this.resetIdCardResult = RESET_FIRST_FAIL;
                    LogC.e(TAG, "deleteCtid and deleteEid fail, tryCount = " + i, false);
                    calElapse(startTime);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        LogC.e(TAG, "sleep exception", true);
                    }
                }
                i++;
            } else {
                this.resetIdCardResult = "success";
                LogC.i(TAG, "delete IdCard success, tryCount = " + i, false);
                calElapse(startTime);
                return;
            }
        }
        this.resetIdCardResult = "fail";
        LogC.e(TAG, "deleteCtid and deleteEid fail, tryCount = 2", false);
        calElapse(startTime);
    }

    private void resetAccessCard() {
        LogC.i(TAG, "Begin to resetAccessCard", false);
        if (WalletFactory.getInstance(this.mContext, null).isAccessCardNeedClean()) {
            List<TACardInfo> accessList = WalletTaManager.getInstance(this.mContext).getResetAccessCardList();
            if (accessList == null || accessList.size() <= 0) {
                LogC.i(TAG, "no access card found", false);
                this.resetAccessCardResult = "success";
                resetBlankCard();
                return;
            }
            AccessCardOperateLogic.getInstance(this.mContext).initAccessCard(accessList, new InitAccessCardOperatorCallback() {
                public void initCUPCardOperatorResult(int resultCode) {
                    if (resultCode == 0) {
                        LogC.i(ResetAccessAndIdCardsTask.TAG, "delete access card success", false);
                        String unused = ResetAccessAndIdCardsTask.this.resetAccessCardResult = "success";
                        ResetAccessAndIdCardsTask.this.resetBlankCard();
                        return;
                    }
                    LogC.e(ResetAccessAndIdCardsTask.TAG, "delete access card failed", false);
                    ResetAccessAndIdCardsTask.this.handleResetAccessFail();
                }
            });
            return;
        }
        LogC.i(TAG, "No need to resetAccessCard after check dictionary", false);
        this.resetAccessCardResult = "success";
        resetBlankCard();
    }

    /* access modifiers changed from: private */
    public void resetBlankCard() {
        LogC.i(TAG, "Begin to resetBlankCard", false);
        if (WalletFactory.getInstance(this.mContext, null).isBlankCardNeedClean()) {
            List<TACardInfo> blankCardList = WalletTaManager.getInstance(this.mContext).getBlankCardList();
            if (blankCardList == null || blankCardList.size() <= 0) {
                LogC.i(TAG, "no blank card found", false);
                this.resetBlankCardResult = "success";
                callBack();
                return;
            }
            TACardInfo taCardInfo = blankCardList.get(0);
            final String passTypeId = taCardInfo.getPassTypeId();
            final String passId = taCardInfo.getProductId();
            final String whiteCardAid = taCardInfo.getAid();
            WhiteCardOperateLogic.getInstance(this.mContext).deleteWhiteCard(passTypeId, passId, whiteCardAid, new BaseCallback() {
                public void onSuccess(int i) {
                    LogC.i("delete whiteCard success", false);
                    LogC.d("delete whiteCard success, passtypeid=" + passTypeId + ", passid=" + passId + ", aid=" + whiteCardAid, false);
                    ResetAccessAndIdCardsTask.this.resetBlankCard();
                }

                public void onFail(int i, ErrorInfo errorInfo) {
                    LogC.i("delete whiteCard failed", false);
                    LogC.d(ResetAccessAndIdCardsTask.TAG, "delete whiteCard failed, passtypeid=" + passTypeId + ", passid=" + passId + ", aid=" + whiteCardAid, false);
                    ResetAccessAndIdCardsTask.this.handleResetBlankFail();
                }
            });
            return;
        }
        LogC.i(TAG, "No need to resetBlankCard after check dictionary", false);
        this.resetBlankCardResult = "success";
        callBack();
    }

    /* access modifiers changed from: private */
    public void handleResetAccessFail() {
        if ("".equals(this.resetAccessCardResult)) {
            this.resetAccessCardResult = RESET_FIRST_FAIL;
            resetAccessCard();
            return;
        }
        this.resetAccessCardResult = "fail";
        resetBlankCard();
    }

    /* access modifiers changed from: private */
    public void handleResetBlankFail() {
        if ("".equals(this.resetBlankCardResult)) {
            this.resetBlankCardResult = RESET_FIRST_FAIL;
            resetBlankCard();
            return;
        }
        this.resetBlankCardResult = "fail";
        callBack();
    }

    private void callBack() {
        LogC.i(TAG, "End to resetBlankCard", false);
        if ("success".equals(this.resetIdCardResult) && "success".equals(this.resetAccessCardResult) && "success".equals(this.resetBlankCardResult)) {
            LogC.i(TAG, "reset IDCard and AccessCard success", false);
            this.mCallback.onSuccess(this.mElapse);
        } else if ("fail".equals(this.resetIdCardResult)) {
            LogC.e(TAG, "reset IDCard failed", false);
            this.mCallback.onFail(-1, "Reset IDCard failed", this.mElapse);
        } else if ("fail".equals(this.resetAccessCardResult)) {
            LogC.e(TAG, "reset AccessCard failed", false);
            this.mCallback.onFail(-2, "Reset AccessCard failed", this.mElapse);
        } else {
            LogC.e(TAG, "reset BlankCard failed", false);
            this.mCallback.onFail(-2, "Reset BlankCard failed", this.mElapse);
        }
    }
}
