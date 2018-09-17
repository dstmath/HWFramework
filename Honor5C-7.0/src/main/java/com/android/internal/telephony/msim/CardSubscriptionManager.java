package com.android.internal.telephony.msim;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.huawei.android.util.NoExtAPIException;

public class CardSubscriptionManager extends Handler {

    class CardInfo {
        public CardInfo(UiccCard uiccCard) {
            throw new NoExtAPIException("method not supported.");
        }

        public UiccCard getUiccCard() {
            throw new NoExtAPIException("method not supported.");
        }

        public void setUiccCard(UiccCard uiccCard) {
            throw new NoExtAPIException("method not supported.");
        }

        public boolean isReadIccIdInProgress() {
            throw new NoExtAPIException("method not supported.");
        }

        public void setReadIccIdInProgress(boolean read) {
            throw new NoExtAPIException("method not supported.");
        }

        public String getIccId() {
            throw new NoExtAPIException("method not supported.");
        }

        public void setIccId(String iccId) {
            throw new NoExtAPIException("method not supported.");
        }

        public String toString() {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public static CardSubscriptionManager getInstance(Context context, UiccController uiccMgr, CommandsInterface[] ci) {
        throw new NoExtAPIException("method not supported.");
    }

    public static CardSubscriptionManager getInstance() {
        throw new NoExtAPIException("method not supported.");
    }

    public void handleMessage(Message msg) {
        throw new NoExtAPIException("method not supported.");
    }

    public void registerForAllCardsInfoAvailable(Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public void registerForCardInfoUnavailable(int cardIndex, Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public void registerForCardInfoAvailable(int cardIndex, Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public SubscriptionData getCardSubscriptions(int cardIndex) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isValidCards() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isCardAbsentOrError(int cardIndex) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isAllCardsUpdated() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isCardInfoAvailable(int cardIndex) {
        throw new NoExtAPIException("method not supported.");
    }
}
