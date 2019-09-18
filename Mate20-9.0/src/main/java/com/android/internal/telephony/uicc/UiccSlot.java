package com.android.internal.telephony.uicc;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccSlotStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UiccSlot extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_CARD_ADDED = 14;
    private static final int EVENT_CARD_REMOVED = 13;
    public static final String EXTRA_ICC_CARD_ADDED = "com.android.internal.telephony.uicc.ICC_CARD_ADDED";
    public static final int INVALID_PHONE_ID = -1;
    private static final String TAG = "UiccSlot";
    private boolean mActive;
    private AnswerToReset mAtr;
    private IccCardStatus.CardState mCardState;
    private CommandsInterface mCi;
    private Context mContext;
    private String mIccId;
    private boolean mIsEuicc;
    private CommandsInterface.RadioState mLastRadioState = CommandsInterface.RadioState.RADIO_UNAVAILABLE;
    private final Object mLock = new Object();
    private int mPhoneId = -1;
    private boolean mStateIsUnknown = true;
    private UiccCard mUiccCard;

    public UiccSlot(Context c, boolean isActive) {
        log("Creating");
        this.mContext = c;
        this.mActive = isActive;
        this.mCardState = null;
    }

    public void update(CommandsInterface ci, IccCardStatus ics, int phoneId) {
        log("cardStatus update: " + ics.toString());
        synchronized (this.mLock) {
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCardState = ics.mCardState;
            this.mIccId = ics.iccid;
            this.mPhoneId = phoneId;
            parseAtr(ics.atr);
            this.mCi = ci;
            CommandsInterface.RadioState radioState = HwTelephonyFactory.getHwUiccManager().powerUpRadioIfhasCard(this.mContext, this.mPhoneId, this.mCi.getRadioState(), this.mLastRadioState, this.mCardState);
            log("update: radioState=" + radioState + " mLastRadioState=" + this.mLastRadioState + ", phoneId = " + phoneId);
            HwTelephonyFactory.getHwUiccManager().isGoingToshowCountDownTimerDialog(radioState, this.mLastRadioState, oldState, this.mCardState, this, this.mPhoneId);
            if (absentStateUpdateNeeded(oldState)) {
                updateCardStateAbsent();
            } else if ((oldState != null && oldState != IccCardStatus.CardState.CARDSTATE_ABSENT && this.mUiccCard != null) || this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                UiccCard uiccCard = this.mUiccCard;
            } else if (radioState == CommandsInterface.RadioState.RADIO_ON && this.mLastRadioState == CommandsInterface.RadioState.RADIO_ON) {
                log("update: notify card added");
                sendMessage(obtainMessage(14, null));
            }
            if (this.mUiccCard != null) {
                this.mUiccCard.update(this.mContext, this.mCi, ics);
            } else if (!this.mIsEuicc) {
                UiccCard uiccCard2 = new UiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                this.mUiccCard = uiccCard2;
            } else {
                EuiccCard euiccCard = new EuiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                this.mUiccCard = euiccCard;
            }
            this.mLastRadioState = radioState;
        }
    }

    public void update(CommandsInterface ci, IccSlotStatus iss) {
        log("slotStatus update: " + iss.toString());
        synchronized (this.mLock) {
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCi = ci;
            parseAtr(iss.atr);
            this.mCardState = iss.cardState;
            this.mIccId = iss.iccid;
            if (iss.slotState != IccSlotStatus.SlotState.SLOTSTATE_INACTIVE) {
                this.mActive = true;
                this.mPhoneId = iss.logicalSlotIndex;
                if (absentStateUpdateNeeded(oldState)) {
                    updateCardStateAbsent();
                }
            } else if (this.mActive) {
                this.mActive = false;
                this.mLastRadioState = CommandsInterface.RadioState.RADIO_UNAVAILABLE;
                this.mPhoneId = -1;
                if (this.mUiccCard != null) {
                    this.mUiccCard.dispose();
                }
                nullifyUiccCard(true);
            }
        }
    }

    private boolean absentStateUpdateNeeded(IccCardStatus.CardState oldState) {
        return !(oldState == IccCardStatus.CardState.CARDSTATE_ABSENT && this.mUiccCard == null) && this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT;
    }

    private void updateCardStateAbsent() {
        CommandsInterface.RadioState radioState = this.mCi == null ? CommandsInterface.RadioState.RADIO_UNAVAILABLE : this.mCi.getRadioState();
        if (radioState == CommandsInterface.RadioState.RADIO_ON && this.mLastRadioState == CommandsInterface.RadioState.RADIO_ON) {
            log("update: notify card removed");
            if (!this.mUiccCard.getCardUimLocked()) {
                sendMessage(obtainMessage(13, null));
            }
        }
        UiccController.updateInternalIccState("ABSENT", null, this.mPhoneId);
        nullifyUiccCard(false);
        this.mLastRadioState = radioState;
    }

    private void nullifyUiccCard(boolean stateUnknown) {
        this.mStateIsUnknown = stateUnknown;
        if (stateUnknown) {
            this.mUiccCard = null;
        }
    }

    public boolean isStateUnknown() {
        return (this.mCardState == null || this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT) && this.mStateIsUnknown;
    }

    private void checkIsEuiccSupported() {
        if (this.mAtr == null || !this.mAtr.isEuiccSupported()) {
            this.mIsEuicc = false;
        } else {
            this.mIsEuicc = true;
        }
    }

    private void parseAtr(String atr) {
        this.mAtr = AnswerToReset.parseAtr(atr);
        if (this.mAtr != null) {
            checkIsEuiccSupported();
        }
    }

    public boolean isEuicc() {
        return this.mIsEuicc;
    }

    public boolean isActive() {
        return this.mActive;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getIccId() {
        if (this.mIccId != null) {
            return this.mIccId;
        }
        if (this.mUiccCard != null) {
            return this.mUiccCard.getIccId();
        }
        return null;
    }

    public boolean isExtendedApduSupported() {
        return this.mAtr != null && this.mAtr.isExtendedApduSupported();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("UiccSlot finalized");
    }

    private void onIccSwap(boolean isAdded) {
        if (this.mContext.getResources().getBoolean(17956983)) {
            log("onIccSwap: isHotSwapSupported is true, don't prompt for rebooting");
            return;
        }
        log("onIccSwap: isHotSwapSupported is false, prompt for rebooting");
        promptForRestart(isAdded);
    }

    private void promptForRestart(boolean isAdded) {
        if (!VSimUtilsInner.isPlatformTwoModems() || (!VSimUtilsInner.isVSimSub(this.mPhoneId) && !VSimUtilsInner.isVSimInProcess() && !VSimUtilsInner.isVSimOn() && !VSimUtilsInner.isVSimCauseCardReload())) {
            synchronized (this.mLock) {
                String dialogComponent = this.mContext.getResources().getString(17039820);
                if (dialogComponent != null) {
                    try {
                        this.mContext.startActivity(new Intent().setComponent(ComponentName.unflattenFromString(dialogComponent)).addFlags(268435456).putExtra("com.android.internal.telephony.uicc.ICC_CARD_ADDED", isAdded));
                        return;
                    } catch (ActivityNotFoundException e) {
                        loge("Unable to find ICC hotswap prompt for restart activity: " + e);
                    }
                }
                if (!HwTelephonyFactory.getHwUiccManager().isHotswapSupported()) {
                    HwTelephonyFactory.getHwUiccManager().createSimAddDialog(this.mContext, isAdded, this.mPhoneId).show();
                    return;
                }
                return;
            }
        }
        log("[2Cards] mPhoneId=" + this.mPhoneId + " is VSIM sub or VSIM is on(" + VSimUtilsInner.isVSimOn() + ")!");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 13:
                onIccSwap(false);
                return;
            case 14:
                onIccSwap(true);
                return;
            default:
                loge("Unknown Event " + msg.what);
                return;
        }
    }

    public IccCardStatus.CardState getCardState() {
        synchronized (this.mLock) {
            if (this.mCardState == null) {
                IccCardStatus.CardState cardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
                return cardState;
            }
            IccCardStatus.CardState cardState2 = this.mCardState;
            return cardState2;
        }
    }

    public UiccCard getUiccCard() {
        UiccCard uiccCard;
        synchronized (this.mLock) {
            uiccCard = this.mUiccCard;
        }
        return uiccCard;
    }

    public void onRadioStateUnavailable() {
        if (this.mUiccCard != null) {
            this.mUiccCard.dispose();
        }
        nullifyUiccCard(true);
        if (this.mPhoneId != -1) {
            UiccController.updateInternalIccState("UNKNOWN", null, this.mPhoneId);
            UiccProfile.broadcastIccStateChangedIntent("UNKNOWN", null, this.mPhoneId);
        }
        this.mCardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        this.mLastRadioState = CommandsInterface.RadioState.RADIO_UNAVAILABLE;
    }

    private void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccSlot:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mActive=" + this.mActive);
        pw.println(" mLastRadioState=" + this.mLastRadioState);
        pw.println(" mCardState=" + this.mCardState);
        if (this.mUiccCard != null) {
            pw.println(" mUiccCard=" + this.mUiccCard);
            this.mUiccCard.dump(fd, pw, args);
        } else {
            pw.println(" mUiccCard=null");
        }
        pw.println();
        pw.flush();
        pw.flush();
    }
}
