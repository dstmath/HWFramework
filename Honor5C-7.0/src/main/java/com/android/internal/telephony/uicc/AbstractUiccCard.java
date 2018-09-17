package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.view.View.OnClickListener;
import com.android.internal.telephony.HwTelephonyFactory;

public class AbstractUiccCard {
    protected static final int EVENT_CARD_UIM_LOCK = 30;
    private static final String LOG_TAG = "AbstractUiccCard";
    protected boolean bCardUimLocked;
    private UiccCardReference mReference;

    public interface UiccCardReference {
        void closeLogicalChannel(int i, Message message);

        void displayRestartDialog(Context context);

        void displayUimTipDialog(Context context, int i);

        void exchangeAPDU(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

        void exchangeSimIO(int i, int i2, int i3, int i4, int i5, String str, Message message);

        int getNumApplications();

        AlertDialog getSimAddDialog(Context context, String str, String str2, String str3, OnClickListener onClickListener);

        boolean hasAppActived();

        void iccGetATR(Message message);

        boolean isAllAndCardRemoved(boolean z);

        void openLogicalChannel(String str, Message message);
    }

    public AbstractUiccCard() {
        this.bCardUimLocked = false;
        this.mReference = HwTelephonyFactory.getHwUiccManager().createHwUiccCardReference(this);
    }

    public boolean hasAppActived() {
        return this.mReference.hasAppActived();
    }

    public int getNumApplications() {
        return this.mReference.getNumApplications();
    }

    public boolean isCardUimLocked() {
        return this.bCardUimLocked;
    }

    public void exchangeAPDU(int cla, int command, int channel, int p1, int p2, int p3, String data, Message onComplete) {
        this.mReference.exchangeAPDU(cla, command, channel, p1, p2, p3, data, onComplete);
    }

    public void openLogicalChannel(String AID, Message onComplete) {
        this.mReference.openLogicalChannel(AID, onComplete);
    }

    public void closeLogicalChannel(int channel, Message onComplete) {
        this.mReference.closeLogicalChannel(channel, onComplete);
    }

    public void exchangeSimIO(int fileID, int command, int p1, int p2, int p3, String pathID, Message onComplete) {
        this.mReference.exchangeSimIO(fileID, command, p1, p2, p3, pathID, onComplete);
    }

    public void iccGetATR(Message onComplete) {
        this.mReference.iccGetATR(onComplete);
    }

    public AlertDialog getSimAddDialog(Context mContext, String title, String message, String buttonTxt, OnClickListener listener) {
        return this.mReference.getSimAddDialog(mContext, title, message, buttonTxt, listener);
    }

    public void displayUimTipDialog(Context context, int resId) {
        this.mReference.displayUimTipDialog(context, resId);
    }

    public void displayRestartDialog(Context context) {
        this.mReference.displayRestartDialog(context);
    }

    public boolean isAllAndCardRemoved(boolean isAdded) {
        return this.mReference.isAllAndCardRemoved(isAdded);
    }
}
