package com.huawei.wallet.sdk.business.idcard.idcard.storage;

import android.content.Context;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;

public class CtidTaManager {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:CtidTaManager";
    private static volatile CtidTaManager mInstance;
    private Context mContext;
    private TACardInfo mCtidCardInfo;
    private TACardInfo mEidCardInfo;

    private CtidTaManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mCtidCardInfo = getCardInfoFromTA(EidCache.getInstance(context).getCtidIssuerId());
        this.mEidCardInfo = getCardInfoFromTA(EidCache.getInstance(context).getEidIssuerId());
    }

    public static CtidTaManager getInstance(Context context) {
        LogC.i(TAG, "CtidTaManager.getInstance executed", false);
        if (mInstance == null) {
            synchronized (SYNC_LOCK) {
                if (mInstance == null) {
                    mInstance = new CtidTaManager(context);
                }
            }
        }
        return mInstance;
    }

    public synchronized TACardInfo getCardInfoFromTA(String issuerId) {
        List<TACardInfo> cardInfoList = WalletTaManager.getInstance(this.mContext).getCardList();
        if (cardInfoList != null) {
            for (TACardInfo cardInfo : cardInfoList) {
                if (3 == cardInfo.getCardGroupType() && issuerId.equals(cardInfo.getIssuerId())) {
                    return cardInfo;
                }
            }
        }
        return null;
    }

    public synchronized TACardInfo getCtidCardInfo() {
        return this.mCtidCardInfo;
    }

    public synchronized TACardInfo getEidCardInfo() {
        return this.mEidCardInfo;
    }

    public synchronized TACardInfo getEidCardInfoFromTA() {
        this.mEidCardInfo = getCardInfoFromTA(EidCache.getInstance(this.mContext).getEidIssuerId());
        return this.mEidCardInfo;
    }

    public synchronized boolean isEidCardInfoExist() {
        if (this.mEidCardInfo == null) {
            return false;
        }
        if (!StringUtil.isEmpty(this.mEidCardInfo.getAid(), true)) {
            return true;
        }
        LogC.e(TAG, "isEidCardInfoExist fail, aid is empty.", false);
        return false;
    }

    public synchronized boolean isEidCardInfoExist(String accountId) {
        if (!isEidCardInfoExist()) {
            return false;
        }
        if (accountId == null) {
            LogC.e(TAG, "isEidCardInfoExist fail, accountId is null", false);
            return false;
        }
        return accountId.equals(this.mEidCardInfo.getUserid());
    }

    public synchronized boolean deleteCtidCardInfo() {
        if (deleteCardInfo(this.mCtidCardInfo)) {
            LogC.i(TAG, "deleteCtidCardInfo success", false);
            this.mCtidCardInfo = null;
            return true;
        }
        LogC.e(TAG, "deleteCtidCardInfo fail", false);
        return false;
    }

    public synchronized boolean deleteEidCardInfo() {
        if (deleteCardInfo(this.mEidCardInfo)) {
            LogC.i(TAG, "deleteEidCardInfo success", false);
            this.mEidCardInfo = null;
            return true;
        }
        LogC.e(TAG, "deleteEidCardInfo fail", false);
        return false;
    }

    public boolean deleteCardInfo(TACardInfo cardInfo) {
        if (cardInfo == null) {
            LogC.i(TAG, "deleteCardInfo success, cardInfo is null.", false);
            return true;
        } else if (StringUtil.isEmpty(cardInfo.getAid(), true)) {
            LogC.e(TAG, "deleteCardInfo fail, aid is null", false);
            return false;
        } else {
            try {
                WalletTaManager.getInstance(this.mContext).removeCardByAid(cardInfo.getAid());
                return true;
            } catch (WalletTaException.WalletTaCardNotExistException e) {
                LogC.i(TAG, "TA have been deleted", false);
                return true;
            } catch (WalletTaException.WalletTaSystemErrorException e2) {
                LogC.e(TAG, "Delete TA fail, WalletTaSystemErrorException errorCode = " + e2.getCode(), false);
                return false;
            }
        }
    }
}
