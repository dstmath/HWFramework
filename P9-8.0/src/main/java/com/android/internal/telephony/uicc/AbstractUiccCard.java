package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.view.View.OnClickListener;
import com.android.internal.telephony.HwTelephonyFactory;

public class AbstractUiccCard {
    protected static final int EVENT_CARD_UIM_LOCK = 30;
    private static final String LOG_TAG = "AbstractUiccCard";
    protected boolean bCardUimLocked = false;
    private UiccCardReference mReference = HwTelephonyFactory.getHwUiccManager().createHwUiccCardReference(this);

    public interface UiccCardReference {
        void displayRestartDialog(Context context);

        void displayUimTipDialog(Context context, int i);

        int getNumApplications();

        AlertDialog getSimAddDialog(Context context, String str, String str2, String str3, OnClickListener onClickListener);

        boolean hasAppActived();

        void iccGetATR(Message message);

        boolean isAllAndCardRemoved(boolean z);
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
