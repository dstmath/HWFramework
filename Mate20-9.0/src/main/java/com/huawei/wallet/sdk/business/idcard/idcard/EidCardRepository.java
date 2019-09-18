package com.huawei.wallet.sdk.business.idcard.idcard;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.QueryCardStatusCallback;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.QueryCardStatusRunnable;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.IdCardStatusItem;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.CtidTaManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.model.WalletCardBaseInfo;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import java.util.Iterator;
import java.util.List;

public class EidCardRepository extends CardRepository {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:EidCardRepository";
    private static volatile EidCardRepository mInstance;
    private Context mContext;

    private EidCardRepository(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    /* access modifiers changed from: protected */
    public void queryRemoteCardInfo() {
    }

    public void refreshCardInfo() {
        refreshCardInfo(true);
    }

    /* access modifiers changed from: private */
    public void add() {
        synchronized (SYNC_LOCK) {
            this.mCacheCardInfo.clear();
            this.mCacheCardInfo.add(cardInfo());
            notifyListeners();
        }
    }

    /* access modifiers changed from: private */
    public void clear() {
        synchronized (SYNC_LOCK) {
            this.mCacheCardInfo.clear();
            notifyListeners();
        }
    }

    public void refreshCardInfo(boolean needQueryRemote) {
        refreshCardInfo(needQueryRemote, null);
    }

    private void refreshCardInfo(boolean needQueryRemote, String accountId) {
        if (isEidExist(accountId)) {
            LogC.i(TAG, "eid exist in ta", false);
            add();
        } else if (!needQueryRemote) {
            clear();
        } else {
            ThreadPoolManager.getInstance().execute(new QueryCardStatusRunnable(this.mContext, new QueryCardStatusCallback() {
                public void onSuccess(List<IdCardStatusItem> items) {
                    if (items != null) {
                        Iterator<IdCardStatusItem> it = items.iterator();
                        if (it.hasNext()) {
                            IdCardStatusItem item = it.next();
                            if (item.getEidCode() == null) {
                                LogC.i(EidCardRepository.TAG, "EidCode is null", false);
                                EidCardRepository.this.clear();
                            } else if (item.getStatus().equals("0")) {
                                LogC.i(EidCardRepository.TAG, "eid exist in server", false);
                                EidCardRepository.this.add();
                            } else {
                                LogC.i(EidCardRepository.TAG, "eid exist in server, but status is not normal. status = " + item.getStatus(), false);
                                EidCardRepository.this.clear();
                            }
                        }
                    } else {
                        LogC.i(EidCardRepository.TAG, "no eid in server", false);
                        EidCardRepository.this.clear();
                    }
                }

                public void onFail(int errorCode, String msg) {
                    LogC.e(EidCardRepository.TAG, "refreshCardInfo QueryCardStatus fail", false);
                    EidCardRepository.this.clear();
                }
            }));
        }
    }

    public static EidCardRepository getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SYNC_LOCK) {
                if (mInstance == null) {
                    mInstance = new EidCardRepository(context);
                }
            }
        }
        return mInstance;
    }

    private boolean isEidExist(String accountId) {
        return CtidTaManager.getInstance(this.mContext).isEidCardInfoExist(accountId);
    }

    private WalletCardBaseInfo cardInfo() {
        TACardInfo taCardInfo = CtidTaManager.getInstance(this.mContext).getEidCardInfoFromTA();
        WalletCardBaseInfo cardInfo = new WalletCardBaseInfo();
        cardInfo.setName("EID, make life easier");
        if (taCardInfo != null) {
            cardInfo.setDefault(taCardInfo.isDefaultCard());
        }
        return cardInfo;
    }
}
