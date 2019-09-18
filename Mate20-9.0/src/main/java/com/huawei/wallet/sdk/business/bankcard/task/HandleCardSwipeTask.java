package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.CUPService;
import com.huawei.wallet.sdk.business.bankcard.api.CardOperateListener;
import com.huawei.wallet.sdk.business.bankcard.constant.BankcardConstant;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.List;

public class HandleCardSwipeTask extends HandleCardOperateBaseTask {
    private final Context mContext;

    public HandleCardSwipeTask(Context context, CUPService service, HandleOperationResultTask resulthandleTask, CardOperateListener listener) {
        super(service, resulthandleTask, listener);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public String getOperateEventTag() {
        return BankcardConstant.OPERATE_EVENT_WIPEOUT;
    }

    /* access modifiers changed from: protected */
    public boolean isOperationSatisfied(List<String> list) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean prepareLocalInfo(List<String> refIds) {
        LogC.i("swipe, prepareLocalInfo", false);
        for (String refId : refIds) {
            TACardInfo taCardInfo = WalletTaManager.getInstance(this.mContext).getCard(refId);
            if (taCardInfo == null) {
                LogC.d("swipe prepare, refId not exsited in ta.", false);
            } else {
                LogC.d("delete prepare now, refId: " + refId + ",existed status: " + taCardInfo.getCardStatus(), false);
                if (3 == taCardInfo.getCardStatus()) {
                    LogC.d("swipe prepare, card nullified in ta, refId: " + refId, false);
                } else {
                    try {
                        WalletTaManager.getInstance(this.mContext).updateCardStatus(refId, 3);
                    } catch (WalletTaException.WalletTaCardNotExistException e) {
                        LogC.e("ta card not exist exception .", false);
                        return false;
                    } catch (WalletTaException.WalletTaSystemErrorException e2) {
                        LogC.e("wallet ta system error exception .", false);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleSuccessResult(List<String> refIds) {
        boolean isUpdateSuccess = true;
        for (String refId : refIds) {
            if (WalletTaManager.getInstance(this.mContext).getCard(refId) == null) {
                LogC.d("update after delete, refId not exsited in ta.", false);
            } else {
                LogC.d("delete after swipe, ta info now, refId: " + refId, false);
                try {
                    WalletTaManager.getInstance(this.mContext).removeCard(refId);
                } catch (WalletTaException.WalletTaCardNotExistException e) {
                    LogC.e("ta card not exist exception.", false);
                } catch (WalletTaException.WalletTaSystemErrorException e2) {
                    LogC.e("wallet ta system error exception.", false);
                    isUpdateSuccess = false;
                }
            }
        }
        return isUpdateSuccess;
    }

    /* access modifiers changed from: protected */
    public void handleFailResult(List<String> list, int excuteErrCode) {
    }
}
