package com.android.internal.telephony.uicc;

import android.telephony.SubscriptionInfo;

public class IccCardStatus {
    public static final int CARD_MAX_APPS = 8;
    public String atr;
    public String iccid;
    public IccCardApplicationStatus[] mApplications;
    public CardState mCardState;
    public int mCdmaSubscriptionAppIndex;
    public int mGsmUmtsSubscriptionAppIndex;
    public int mImsSubscriptionAppIndex;
    public PinState mUniversalPinState;
    public int physicalSlotIndex = -1;

    public enum CardState {
        CARDSTATE_ABSENT,
        CARDSTATE_PRESENT,
        CARDSTATE_ERROR,
        CARDSTATE_RESTRICTED;

        /* access modifiers changed from: package-private */
        public boolean isCardPresent() {
            return this == CARDSTATE_PRESENT || this == CARDSTATE_RESTRICTED;
        }
    }

    public enum PinState {
        PINSTATE_UNKNOWN,
        PINSTATE_ENABLED_NOT_VERIFIED,
        PINSTATE_ENABLED_VERIFIED,
        PINSTATE_DISABLED,
        PINSTATE_ENABLED_BLOCKED,
        PINSTATE_ENABLED_PERM_BLOCKED;

        /* access modifiers changed from: package-private */
        public boolean isPermBlocked() {
            return this == PINSTATE_ENABLED_PERM_BLOCKED;
        }

        /* access modifiers changed from: package-private */
        public boolean isPinRequired() {
            return this == PINSTATE_ENABLED_NOT_VERIFIED;
        }

        /* access modifiers changed from: package-private */
        public boolean isPukRequired() {
            return this == PINSTATE_ENABLED_BLOCKED;
        }
    }

    public void setCardState(int state) {
        switch (state) {
            case 0:
                this.mCardState = CardState.CARDSTATE_ABSENT;
                return;
            case 1:
                this.mCardState = CardState.CARDSTATE_PRESENT;
                return;
            case 2:
                this.mCardState = CardState.CARDSTATE_ERROR;
                return;
            case 3:
                this.mCardState = CardState.CARDSTATE_RESTRICTED;
                return;
            default:
                throw new RuntimeException("Unrecognized RIL_CardState: " + state);
        }
    }

    public void setUniversalPinState(int state) {
        switch (state) {
            case 0:
                this.mUniversalPinState = PinState.PINSTATE_UNKNOWN;
                return;
            case 1:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_NOT_VERIFIED;
                return;
            case 2:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_VERIFIED;
                return;
            case 3:
                this.mUniversalPinState = PinState.PINSTATE_DISABLED;
                return;
            case 4:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_BLOCKED;
                return;
            case 5:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_PERM_BLOCKED;
                return;
            default:
                throw new RuntimeException("Unrecognized RIL_PinState: " + state);
        }
    }

    public String toString() {
        int appSize = this.mApplications != null ? this.mApplications.length : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("IccCardState {");
        sb.append(this.mCardState);
        sb.append(",");
        sb.append(this.mUniversalPinState);
        sb.append(",num_apps=");
        sb.append(appSize);
        sb.append(",gsm_id=");
        sb.append(this.mGsmUmtsSubscriptionAppIndex);
        if (this.mApplications != null && this.mGsmUmtsSubscriptionAppIndex >= 0 && this.mGsmUmtsSubscriptionAppIndex < appSize) {
            IccCardApplicationStatus app = this.mApplications[this.mGsmUmtsSubscriptionAppIndex];
            sb.append(app == null ? "null" : app);
        }
        sb.append(",cdma_id=");
        sb.append(this.mCdmaSubscriptionAppIndex);
        if (this.mApplications != null && this.mCdmaSubscriptionAppIndex >= 0 && this.mCdmaSubscriptionAppIndex < appSize) {
            IccCardApplicationStatus app2 = this.mApplications[this.mCdmaSubscriptionAppIndex];
            sb.append(app2 == null ? "null" : app2);
        }
        sb.append(",ims_id=");
        sb.append(this.mImsSubscriptionAppIndex);
        if (this.mApplications != null && this.mImsSubscriptionAppIndex >= 0 && this.mImsSubscriptionAppIndex < appSize) {
            IccCardApplicationStatus app3 = this.mApplications[this.mImsSubscriptionAppIndex];
            sb.append(app3 == null ? "null" : app3);
        }
        sb.append(",physical_slot_id=");
        sb.append(this.physicalSlotIndex);
        sb.append(",atr=");
        sb.append(this.atr);
        sb.append(",iccid=");
        sb.append(SubscriptionInfo.givePrintableIccid(this.iccid));
        sb.append("}");
        return sb.toString();
    }
}
