package com.huawei.wallet.sdk.business.idcard.idcard;

import android.app.Activity;
import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.walletbase.model.IWalletCardBaseInfo;
import com.huawei.wallet.sdk.common.log.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class CardRepository {
    private static final Logger LogX = Logger.tag("WalletBase:CardRepository").build();
    protected List<IWalletCardBaseInfo> mCacheCardInfo = new ArrayList();
    private final Context mContext;
    private final CopyOnWriteArraySet<OnDataReadyListener> mFirstListeners = new CopyOnWriteArraySet<>();
    protected CopyOnWriteArrayList<OnDataReadyListener> mListeners = new CopyOnWriteArrayList<>();

    /* access modifiers changed from: protected */
    public abstract void queryRemoteCardInfo();

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    protected CardRepository(Context context) {
        if (context instanceof Activity) {
            this.mContext = context.getApplicationContext();
        } else {
            this.mContext = context;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyListeners() {
        Logger logger = LogX;
        logger.d("notifyListeners: " + this.mListeners.size(), false);
        List<IWalletCardBaseInfo> tmpCardInfo = getCardInfo();
        Iterator<OnDataReadyListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().refreshData(tmpCardInfo);
        }
    }

    private List<IWalletCardBaseInfo> getCardInfo() {
        List<IWalletCardBaseInfo> tmpCardInfo;
        synchronized (this) {
            tmpCardInfo = new ArrayList<>(this.mCacheCardInfo.size());
            tmpCardInfo.addAll(this.mCacheCardInfo);
        }
        return tmpCardInfo;
    }

    /* access modifiers changed from: protected */
    public boolean existCache() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setCached() {
    }
}
