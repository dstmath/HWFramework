package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.util.Router;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;

public abstract class CardStatusBaseModifier {
    private final String mAid;
    private final Context mContext;

    /* access modifiers changed from: protected */
    public abstract boolean modifyCardStatusInESE(String str, Context context, String str2);

    /* access modifiers changed from: protected */
    public abstract boolean modifyLocalCardInfo(String str, Context context, int i);

    public CardStatusBaseModifier(String aid, Context context) {
        this.mContext = context;
        this.mAid = aid;
    }

    public boolean modifyLocalCardStatus(boolean ifNeedNotify, String source) {
        TACardInfo taCardInfo = WalletTaManager.getInstance(this.mContext).getCardInfoByAid(this.mAid);
        if (taCardInfo == null) {
            LogC.e("modifyLocalCardStatus, no ta info.", false);
            return false;
        } else if (!modifyCardStatusInESE(this.mAid, this.mContext, source)) {
            LogC.d("modifyCardStatusInESE, modify card status in ese failed.", false);
            return false;
        } else if (!modifyLocalCardInfo(taCardInfo.getDpanDigest(), this.mContext, taCardInfo.getCardStatus())) {
            LogC.d("modifyLocalCardStatus, update local card info failed.", false);
            return false;
        } else {
            IssuerInfoItem itemInfo = Router.getCardAndIssuerInfoCacheApi(this.mContext).cacheIssuerInfoItem(taCardInfo.getIssuerId());
            if ((itemInfo == null ? -1 : itemInfo.getMode()) != -1) {
                return true;
            }
            LogC.e("reportCardStatus, mode unsupported.", false);
            return false;
        }
    }
}
