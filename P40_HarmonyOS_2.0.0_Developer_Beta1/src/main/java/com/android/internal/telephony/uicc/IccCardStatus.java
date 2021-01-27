package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.telephony.SubscriptionInfo;

public class IccCardStatus {
    public static final int CARD_MAX_APPS = 8;
    public String atr;
    public String eid;
    public String iccid;
    @UnsupportedAppUsage
    public IccCardApplicationStatus[] mApplications;
    @UnsupportedAppUsage
    public CardState mCardState;
    @UnsupportedAppUsage
    public int mCdmaSubscriptionAppIndex;
    @UnsupportedAppUsage
    public int mGsmUmtsSubscriptionAppIndex;
    @UnsupportedAppUsage
    public int mImsSubscriptionAppIndex;
    @UnsupportedAppUsage
    public PinState mUniversalPinState;
    public int physicalSlotIndex = -1;

    public enum CardState {
        CARDSTATE_ABSENT,
        CARDSTATE_PRESENT,
        CARDSTATE_ERROR,
        CARDSTATE_RESTRICTED;

        @UnsupportedAppUsage
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
        if (state == 0) {
            this.mCardState = CardState.CARDSTATE_ABSENT;
        } else if (state == 1) {
            this.mCardState = CardState.CARDSTATE_PRESENT;
        } else if (state == 2) {
            this.mCardState = CardState.CARDSTATE_ERROR;
        } else if (state == 3) {
            this.mCardState = CardState.CARDSTATE_RESTRICTED;
        } else {
            throw new RuntimeException("Unrecognized RIL_CardState: " + state);
        }
    }

    public void setUniversalPinState(int state) {
        if (state == 0) {
            this.mUniversalPinState = PinState.PINSTATE_UNKNOWN;
        } else if (state == 1) {
            this.mUniversalPinState = PinState.PINSTATE_ENABLED_NOT_VERIFIED;
        } else if (state == 2) {
            this.mUniversalPinState = PinState.PINSTATE_ENABLED_VERIFIED;
        } else if (state == 3) {
            this.mUniversalPinState = PinState.PINSTATE_DISABLED;
        } else if (state == 4) {
            this.mUniversalPinState = PinState.PINSTATE_ENABLED_BLOCKED;
        } else if (state == 5) {
            this.mUniversalPinState = PinState.PINSTATE_ENABLED_PERM_BLOCKED;
        } else {
            throw new RuntimeException("Unrecognized RIL_PinState: " + state);
        }
    }

    public String toString() {
        int i;
        int i2;
        int i3;
        IccCardApplicationStatus[] iccCardApplicationStatusArr = this.mApplications;
        int appSize = iccCardApplicationStatusArr != null ? iccCardApplicationStatusArr.length : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("IccCardState {");
        sb.append(this.mCardState);
        sb.append(",");
        sb.append(this.mUniversalPinState);
        if (this.mApplications != null) {
            sb.append(",num_apps=");
            sb.append(appSize);
        } else {
            sb.append(",mApplications=null");
        }
        sb.append(",gsm_id=");
        sb.append(this.mGsmUmtsSubscriptionAppIndex);
        IccCardApplicationStatus[] iccCardApplicationStatusArr2 = this.mApplications;
        Object obj = "null";
        if (iccCardApplicationStatusArr2 != null && (i3 = this.mGsmUmtsSubscriptionAppIndex) >= 0 && i3 < appSize) {
            IccCardApplicationStatus app = iccCardApplicationStatusArr2[i3];
            sb.append(app == null ? obj : app);
        }
        sb.append(",cdma_id=");
        sb.append(this.mCdmaSubscriptionAppIndex);
        IccCardApplicationStatus[] iccCardApplicationStatusArr3 = this.mApplications;
        if (iccCardApplicationStatusArr3 != null && (i2 = this.mCdmaSubscriptionAppIndex) >= 0 && i2 < appSize) {
            IccCardApplicationStatus app2 = iccCardApplicationStatusArr3[i2];
            sb.append(app2 == null ? obj : app2);
        }
        sb.append(",ims_id=");
        sb.append(this.mImsSubscriptionAppIndex);
        IccCardApplicationStatus[] iccCardApplicationStatusArr4 = this.mApplications;
        if (iccCardApplicationStatusArr4 != null && (i = this.mImsSubscriptionAppIndex) >= 0 && i < appSize) {
            IccCardApplicationStatus app3 = iccCardApplicationStatusArr4[i];
            if (app3 != null) {
                obj = app3;
            }
            sb.append(obj);
        }
        sb.append(",physical_slot_id=");
        sb.append(this.physicalSlotIndex);
        sb.append(",atr=");
        sb.append(SubscriptionInfo.givePrintableIccid(this.atr));
        sb.append(",iccid=");
        sb.append(SubscriptionInfo.givePrintableIccid(this.iccid));
        sb.append(",eid=");
        sb.append(SubscriptionInfo.givePrintableIccid(this.eid));
        sb.append("}");
        return sb.toString();
    }
}
