package com.android.internal.telephony.uicc;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccSlotStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
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
    private boolean mIsRemovable;
    private int mLastRadioState = 2;
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

    public void update(CommandsInterface ci, IccCardStatus ics, int phoneId, int slotIndex) {
        log("cardStatus update: " + ics.toString());
        synchronized (this.mLock) {
            try {
                IccCardStatus.CardState oldState = this.mCardState;
                this.mCardState = ics.mCardState;
                this.mIccId = ics.iccid;
                this.mPhoneId = phoneId;
                parseAtr(ics.atr);
                if (TextUtils.isEmpty(ics.eid)) {
                    this.mIsEuicc = false;
                }
                try {
                    this.mCi = ci;
                    this.mIsRemovable = isSlotRemovable(slotIndex);
                    int radioState = HwTelephonyFactory.getHwUiccManager().powerUpRadioIfhasCard(this.mContext, this.mPhoneId, this.mCi.getRadioState(), this.mLastRadioState, IccCardStatusExt.CardStateEx.getCardStateExByCardState(this.mCardState));
                    log("update: radioState=" + radioState + " mLastRadioState=" + this.mLastRadioState + ", phoneId = " + phoneId);
                    HwTelephonyFactory.getHwUiccManager().isGoingToshowCountDownTimerDialog(radioState, this.mLastRadioState, IccCardStatusExt.CardStateEx.getCardStateExByCardState(oldState), IccCardStatusExt.CardStateEx.getCardStateExByCardState(this.mCardState), this, this.mPhoneId);
                    if (absentStateUpdateNeeded(oldState)) {
                        updateCardStateAbsent(oldState);
                    } else if ((oldState == null || oldState == IccCardStatus.CardState.CARDSTATE_ABSENT || this.mUiccCard == null) && this.mCardState != IccCardStatus.CardState.CARDSTATE_ABSENT && radioState == 1 && this.mLastRadioState == 1) {
                        log("update: notify card added");
                        sendMessage(obtainMessage(14, null));
                    }
                    if (this.mUiccCard != null) {
                        if (this.mIsEuicc != (this.mUiccCard instanceof EuiccCard)) {
                            loge("switch to esim/sim, we nend to dispose uiccard first.");
                            this.mUiccCard.dispose();
                            if (!this.mIsEuicc) {
                                this.mUiccCard = new UiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                            } else {
                                this.mUiccCard = new EuiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                            }
                        } else {
                            this.mUiccCard.update(this.mContext, this.mCi, ics);
                        }
                    } else if (!this.mIsEuicc) {
                        this.mUiccCard = new UiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                    } else {
                        this.mUiccCard = new EuiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                    }
                    this.mLastRadioState = radioState;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public void update(CommandsInterface ci, IccSlotStatus iss, int slotIndex) {
        log("slotStatus update: " + iss.toString());
        synchronized (this.mLock) {
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCi = ci;
            parseAtr(iss.atr);
            this.mCardState = iss.cardState;
            this.mIccId = iss.iccid;
            this.mIsRemovable = isSlotRemovable(slotIndex);
            if (iss.slotState != IccSlotStatus.SlotState.SLOTSTATE_INACTIVE) {
                this.mActive = true;
                this.mPhoneId = iss.logicalSlotIndex;
                if (absentStateUpdateNeeded(oldState)) {
                    updateCardStateAbsent(oldState);
                }
            } else if (this.mActive) {
                this.mActive = false;
                this.mLastRadioState = 2;
                UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.ABSENT, null, this.mPhoneId, true);
                this.mPhoneId = -1;
                nullifyUiccCard(true);
            }
        }
    }

    private boolean absentStateUpdateNeeded(IccCardStatus.CardState oldState) {
        return !(oldState == IccCardStatus.CardState.CARDSTATE_ABSENT && this.mUiccCard == null) && this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT;
    }

    private void updateCardStateAbsent(IccCardStatus.CardState oldState) {
        CommandsInterface commandsInterface = this.mCi;
        int radioState = commandsInterface == null ? 2 : commandsInterface.getRadioState();
        if (radioState == 1 && this.mLastRadioState == 1) {
            log("update: notify card removed");
            if (!this.mUiccCard.isCardUimLocked() && oldState != IccCardStatus.CardState.CARDSTATE_ABSENT) {
                sendMessage(obtainMessage(13, null));
            }
        }
        UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.ABSENT, null, this.mPhoneId);
        nullifyUiccCard(false);
        this.mLastRadioState = radioState;
    }

    private void nullifyUiccCard(boolean stateUnknown) {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            uiccCard.dispose();
        }
        this.mStateIsUnknown = stateUnknown;
        if (stateUnknown) {
            this.mUiccCard = null;
        }
    }

    public boolean isStateUnknown() {
        IccCardStatus.CardState cardState = this.mCardState;
        if (cardState == null || cardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
            return this.mStateIsUnknown;
        }
        return this.mUiccCard == null;
    }

    private boolean isSlotRemovable(int slotIndex) {
        int[] euiccSlots = this.mContext.getResources().getIntArray(17236095);
        if (euiccSlots == null) {
            return true;
        }
        for (int euiccSlot : euiccSlots) {
            if (euiccSlot == slotIndex) {
                return false;
            }
        }
        return true;
    }

    private void checkIsEuiccSupported() {
        AnswerToReset answerToReset = this.mAtr;
        if (answerToReset == null || !answerToReset.isEuiccSupported()) {
            this.mIsEuicc = false;
        } else {
            this.mIsEuicc = true;
        }
    }

    private void parseAtr(String atr) {
        this.mAtr = AnswerToReset.parseAtr(atr);
        checkIsEuiccSupported();
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

    public boolean isRemovable() {
        return this.mIsRemovable;
    }

    public String getIccId() {
        String str = this.mIccId;
        if (str != null) {
            return str;
        }
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            return uiccCard.getIccId();
        }
        return null;
    }

    public boolean isExtendedApduSupported() {
        AnswerToReset answerToReset = this.mAtr;
        return answerToReset != null && answerToReset.isExtendedApduSupported();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        log("UiccSlot finalized");
    }

    private void onIccSwap(boolean isAdded) {
        if (this.mContext.getResources().getBoolean(17891468)) {
            log("onIccSwap: isHotSwapSupported is true, don't prompt for rebooting");
            return;
        }
        log("onIccSwap: isHotSwapSupported is false, prompt for rebooting");
        promptForRestart(isAdded);
    }

    private void promptForRestart(boolean isAdded) {
        if (!VSimUtilsInner.isPlatformTwoModems() || (!VSimUtilsInner.isVSimSub(this.mPhoneId) && !VSimUtilsInner.isVSimInProcess() && !VSimUtilsInner.isVSimOn() && !VSimUtilsInner.isVSimCauseCardReload())) {
            synchronized (this.mLock) {
                String dialogComponent = this.mContext.getResources().getString(17039859);
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
        int i = msg.what;
        if (i == 13) {
            onIccSwap(false);
        } else if (i != 14) {
            loge("Unknown Event " + msg.what);
        } else {
            onIccSwap(true);
        }
    }

    public IccCardStatus.CardState getCardState() {
        synchronized (this.mLock) {
            if (this.mCardState == null) {
                return IccCardStatus.CardState.CARDSTATE_ABSENT;
            }
            return this.mCardState;
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
        nullifyUiccCard(true);
        if (this.mPhoneId != -1) {
            UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.UNKNOWN, null, this.mPhoneId);
            UiccProfile.broadcastIccStateChangedIntent(IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN, null, this.mPhoneId);
        }
        this.mCardState = null;
        this.mLastRadioState = 2;
    }

    private void log(String msg) {
        Rlog.i(TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccSlot:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mActive=" + this.mActive);
        pw.println(" mIsEuicc=" + this.mIsEuicc);
        pw.println(" mLastRadioState=" + this.mLastRadioState);
        pw.println(" mIccId=" + SubscriptionInfo.givePrintableIccid(this.mIccId));
        pw.println(" mCardState=" + this.mCardState);
        if (this.mUiccCard != null) {
            pw.println(" mUiccCard=" + this.mUiccCard);
            this.mUiccCard.dump(fd, pw, args);
        } else {
            pw.println(" mUiccCard=null");
        }
        pw.println();
        pw.flush();
    }
}
