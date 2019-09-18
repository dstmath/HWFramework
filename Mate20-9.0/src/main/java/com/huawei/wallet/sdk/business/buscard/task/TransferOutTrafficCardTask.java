package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.util.EseTsmInitLoader;
import com.huawei.wallet.sdk.business.buscard.api.CardOperateLogic;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.buscard.model.TransferEvent;
import com.huawei.wallet.sdk.business.buscard.model.TransferOutTrafficCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class TransferOutTrafficCardTask extends TrafficCardBaseTask {
    private String mEventId;
    private boolean mIsFromCloudTransfer = false;
    private TransferOutTrafficCardResultHandler mResultHandler;

    public void setIsFromCloudTransfer(boolean isFromCloud) {
        this.mIsFromCloudTransfer = isFromCloud;
    }

    public TransferOutTrafficCardTask(Context mContext, String eventId, SPIOperatorManager operatorManager, String mIssuerId, TransferOutTrafficCardResultHandler resultHandler) {
        super(mContext, operatorManager, mIssuerId);
        this.mResultHandler = resultHandler;
        this.mEventId = eventId;
    }

    private TransferEvent excutePreTask(TrafficCardOperator operator, IssuerInfoItem item) throws TrafficCardOperateException {
        TransferEvent transferEvent = new TransferEvent();
        if (this.mIsFromCloudTransfer) {
            transferEvent = new TransferEvent();
            transferEvent.setEventId(String.valueOf(10));
        }
        new EseTsmInitLoader(this.mContext).excuteEseInit();
        return transferEvent;
    }

    /* access modifiers changed from: protected */
    public void excuteAction(TrafficCardOperator operator, IssuerInfoItem item) {
        CardOperateLogic.getInstance(this.mContext).addTask(this);
        if (item == null || operator == null) {
            this.mResultHandler.handleResult(10);
            LogC.i("TransferOutTrafficCardTask excuteAction item or operator is null", false);
            return;
        }
        acquireTrafficCardTaskWakelock();
        try {
            TransferEvent transferEvent = excutePreTask(operator, item);
            if (transferEvent != null && this.mIsFromCloudTransfer) {
                operator.cloudTransferOutTrafficCard(transferEvent, item);
            }
            this.mResultHandler.handleResult(0);
        } catch (TrafficCardOperateException e) {
            if (StringUtil.isEmpty(this.mEventId, true)) {
                TACardInfo taCardInfo = WalletTaManager.getInstance(this.mContext).getCard(item.getAid());
                if (taCardInfo != null) {
                    String eventId = taCardInfo.getFpanDigest();
                }
            }
            this.mResultHandler.handleResult(e.getErrorCode());
            LogC.e(e.getMessage(), false);
        } catch (Throwable th) {
            releaseTrafficCardTaskWakelock();
            throw th;
        }
        releaseTrafficCardTaskWakelock();
    }

    private boolean updateTaCardInfo(TACardInfo info) {
        try {
            WalletTaManager.getInstance(this.mContext).removeCardByAid(info.getAid());
            WalletTaManager.getInstance(this.mContext).addCard(info);
            return true;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogX.e("TransferOutTrafficCardTask updateTaCardInfo failed WalletTaCardNotExistException. ", e.getMessage());
            return false;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogX.e("TransferOutTrafficCardTask updateTaCardInfo failed WalletTaSystemErrorException. ", e2.getMessage());
            return false;
        } catch (WalletTaException.WalletTaCardNumReachMaxException e3) {
            LogX.e("TransferOutTrafficCardTask updateTaCardInfo failed WalletTaCardNumReachMaxException. ", e3.getMessage());
            return false;
        } catch (WalletTaException.WalletTaBadParammeterException e4) {
            LogX.e("TransferOutTrafficCardTask updateTaCardInfo failed WalletTaBadParammeterException. ", e4.getMessage());
            return false;
        } catch (WalletTaException.WalletTaCardAlreadyExistException e5) {
            LogX.e("TransferOutTrafficCardTask updateTaCardInfo failed WalletTaCardAlreadyExistException. ", e5.getMessage());
            return false;
        }
    }

    private TACardInfo updateTaCardStatus2Deleting(String aid) {
        TACardInfo cardInfo = null;
        try {
            cardInfo = WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid);
            if (cardInfo == null) {
                return null;
            }
            int status = cardInfo.getCardStatus();
            boolean hasNoEventId = StringUtil.isEmpty(cardInfo.getFpanDigest(), true);
            if (!(status == 15 || status == 16)) {
                if (!this.mIsFromCloudTransfer) {
                    status = 15;
                }
                if (hasNoEventId) {
                    cardInfo.setFpanDigest(this.mEventId);
                    cardInfo.setCardStatus(status);
                    updateTaCardInfo(cardInfo);
                } else {
                    WalletTaManager.getInstance(this.mContext).updateCardStatus(aid, status);
                }
            }
            return cardInfo;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogX.e("TransferOutTrafficCardTask updateTaCardStatus failed WalletTaCardNotExistException. ", e.getMessage());
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogX.e("TransferOutTrafficCardTask updateTaCardStatus failed WalletTaSystemErrorException. ", e2.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "TransferOutTrafficCardTask";
    }
}
