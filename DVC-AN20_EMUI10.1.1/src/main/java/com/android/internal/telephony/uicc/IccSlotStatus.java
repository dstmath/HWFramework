package com.android.internal.telephony.uicc;

import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardStatus;

public class IccSlotStatus {
    public String atr;
    public IccCardStatus.CardState cardState;
    public String eid;
    public String iccid;
    public int logicalSlotIndex;
    public SlotState slotState;

    public enum SlotState {
        SLOTSTATE_INACTIVE,
        SLOTSTATE_ACTIVE
    }

    public void setCardState(int state) {
        if (state == 0) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        } else if (state == 1) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_PRESENT;
        } else if (state == 2) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_ERROR;
        } else if (state == 3) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_RESTRICTED;
        } else {
            throw new RuntimeException("Unrecognized RIL_CardState: " + state);
        }
    }

    public void setSlotState(int state) {
        if (state == 0) {
            this.slotState = SlotState.SLOTSTATE_INACTIVE;
        } else if (state == 1) {
            this.slotState = SlotState.SLOTSTATE_ACTIVE;
        } else {
            throw new RuntimeException("Unrecognized RIL_SlotState: " + state);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IccSlotStatus {");
        sb.append(this.cardState);
        sb.append(",");
        sb.append(this.slotState);
        sb.append(",");
        sb.append("logicalSlotIndex=");
        sb.append(this.logicalSlotIndex);
        sb.append(",");
        sb.append("atr=");
        sb.append(this.atr);
        sb.append(",iccid=");
        sb.append(SubscriptionInfo.givePrintableIccid(this.iccid));
        sb.append(",");
        sb.append("eid=");
        sb.append(Log.HWINFO ? this.eid : "***");
        sb.append("}");
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IccSlotStatus that = (IccSlotStatus) obj;
        if (this.cardState != that.cardState || this.slotState != that.slotState || this.logicalSlotIndex != that.logicalSlotIndex || !TextUtils.equals(this.atr, that.atr) || !TextUtils.equals(this.iccid, that.iccid) || !TextUtils.equals(this.eid, that.eid)) {
            return false;
        }
        return true;
    }
}
