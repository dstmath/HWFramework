package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.util.Router;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperateResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;

public class BankCardNullifiedModifier extends CardStatusBaseModifier {
    public BankCardNullifiedModifier(String aid, Context context) {
        super(aid, context);
    }

    /* access modifiers changed from: protected */
    public boolean modifyCardStatusInESE(String mAid, Context mContext, String source) {
        TACardInfo cardInfo = WalletTaManager.getInstance(mContext).getCardInfoByAid(mAid);
        if (cardInfo == null) {
            return true;
        }
        IssuerInfoItem issuerInfoItem = Router.getCardAndIssuerInfoCacheApi(mContext).cacheIssuerInfoItem(cardInfo.getIssuerId());
        if (issuerInfoItem == null || issuerInfoItem.getMode() == 0) {
            return false;
        }
        TSMOperateResponse resp = BankCardTsmOperator.getInstance(mContext).deleteApplet(mAid);
        int excuteResultCode = resp.getResultCode();
        LogC.d("modifyCardStatusInESE, excute tsm delete, result: " + excuteResultCode, false);
        if (100000 == excuteResultCode) {
            return true;
        }
        LogC.i("modifyCardStatusInESE delete applet failed. " + resp.getPrintMsg(), false);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean modifyLocalCardInfo(String refId, Context context, int oldStatus) {
        try {
            WalletTaManager.getInstance(context).removeCard(refId);
            return true;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogC.d("modifyLocalCardInfo, remove card in ta, WalletTaCardNotExistException", false);
            return true;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogC.d("modifyLocalCardInfo, remove card in ta, WalletTaSystemErrorException", false);
            return false;
        }
    }
}
