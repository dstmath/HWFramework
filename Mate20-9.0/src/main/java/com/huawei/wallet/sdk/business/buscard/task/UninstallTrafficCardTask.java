package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.buscard.model.UninstallTrafficCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;

public class UninstallTrafficCardTask extends TrafficCardBaseTask {
    private String mAccount;
    private String mAccountType;
    private String mReason;
    private String mReasonCode;
    private UninstallTrafficCardResultHandler mResultHandler;
    private String mSource;
    private boolean mUpdateTA;

    public UninstallTrafficCardTask(Context mContext, SPIOperatorManager operatorManager, String mIssuerId, UninstallTrafficCardResultHandler resultHandler, boolean updateTA, String source, String reason, String reasonCode, String accountType, String account) {
        super(mContext, operatorManager, mIssuerId);
        this.mResultHandler = resultHandler;
        this.mUpdateTA = updateTA;
        this.mSource = source;
        this.mReason = reason;
        this.mReasonCode = reasonCode;
        this.mAccountType = accountType;
        this.mAccount = account;
    }

    /* access modifiers changed from: protected */
    public void excuteAction(TrafficCardOperator operator, IssuerInfoItem item) {
        TACardInfo taCardInfo;
        TrafficCardOperateException e;
        if (item == null || operator == null) {
            this.mResultHandler.handleResult(10);
            return;
        }
        acquireTrafficCardTaskWakelock();
        if (item.getMode() == 14 && !item.getIssuerId().equals("t_sh_01")) {
            operator = this.operatorManager.getTrafficCardOpertor(20);
        }
        String aid = item.getAid();
        try {
            taCardInfo = WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid);
            try {
                WalletTaManager.getInstance(this.mContext).updateCardStatusByAid(aid, 21);
                if (operator.uninstallTrafficCard(item, this.mUpdateTA, this.mSource, this.mReason, this.mReasonCode, this.mAccountType, this.mAccount)) {
                    boolean z = this.mUpdateTA;
                }
                this.mResultHandler.handleResult(0);
            } catch (WalletTaException.WalletTaCardNotExistException e2) {
                handleResultError(item, 2002, null);
                LogX.e("UninstallTrafficCardTask", "delete card failed,Wallet TaCard Not Exist");
                releaseTrafficCardTaskWakelock();
            } catch (WalletTaException.WalletTaSystemErrorException e3) {
                handleResultError(item, 2002, null);
                LogX.e("UninstallTrafficCardTask", "delete card failed,Wallet Ta System Error");
                releaseTrafficCardTaskWakelock();
            } catch (TrafficCardOperateException e4) {
                e = e4;
                int errorCode = e.getErrorCode();
                resumeTaCardStatus(taCardInfo, aid, errorCode);
                handleResultError(item, errorCode, e.getErrorInfo());
                LogX.e("UninstallTrafficCardTask excuteAction", "TrafficCardOperateException");
                releaseTrafficCardTaskWakelock();
            }
        } catch (WalletTaException.WalletTaCardNotExistException e5) {
            WalletTaException.WalletTaCardNotExistException walletTaCardNotExistException = e5;
            handleResultError(item, 2002, null);
            LogX.e("UninstallTrafficCardTask", "delete card failed,Wallet TaCard Not Exist");
            releaseTrafficCardTaskWakelock();
        } catch (WalletTaException.WalletTaSystemErrorException e6) {
            WalletTaException.WalletTaSystemErrorException walletTaSystemErrorException = e6;
            handleResultError(item, 2002, null);
            LogX.e("UninstallTrafficCardTask", "delete card failed,Wallet Ta System Error");
            releaseTrafficCardTaskWakelock();
        } catch (TrafficCardOperateException e7) {
            taCardInfo = null;
            e = e7;
            int errorCode2 = e.getErrorCode();
            resumeTaCardStatus(taCardInfo, aid, errorCode2);
            handleResultError(item, errorCode2, e.getErrorInfo());
            LogX.e("UninstallTrafficCardTask excuteAction", "TrafficCardOperateException");
            releaseTrafficCardTaskWakelock();
        } catch (Throwable th) {
            th = th;
            releaseTrafficCardTaskWakelock();
            throw th;
        }
        releaseTrafficCardTaskWakelock();
    }

    private void resumeTaCardStatus(TACardInfo taCardInfo, String aid, int errorCode) {
        if (errorCode == 2003 && taCardInfo != null) {
            try {
                WalletTaManager.getInstance(this.mContext).updateCardStatusByAid(aid, taCardInfo.getCardStatus());
            } catch (WalletTaException.WalletTaCardNotExistException e) {
                LogX.e("UninstallTrafficCardTask", "resume card status failed,Wallet TaCard Not Exist");
            } catch (WalletTaException.WalletTaSystemErrorException e2) {
                LogX.e("UninstallTrafficCardTask", "resume card status failed,Wallet Ta System Error");
            }
        }
    }

    private void handleResultError(IssuerInfoItem item, int errorCode, ErrorInfo info) {
        this.mResultHandler.handleResult(errorCode);
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "UninstallTrafficCardTask";
    }
}
