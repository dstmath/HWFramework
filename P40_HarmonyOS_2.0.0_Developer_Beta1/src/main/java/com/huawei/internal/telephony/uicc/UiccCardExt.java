package com.huawei.internal.telephony.uicc;

import android.content.pm.PackageManager;
import android.os.Handler;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;

public class UiccCardExt {
    private static final int APP_NUMBER_DEFAULT = 0;
    private static final int INVALID_APPLICATION_INDEX = -1;
    private UiccCard mUiccCard;

    public static UiccCardExt getUiccCardFromUiccSlot(UiccSlotEx uiccSlotEx) {
        if (uiccSlotEx == null || uiccSlotEx.getUiccSlot() == null) {
            return null;
        }
        UiccCardExt uiccCardExt = new UiccCardExt();
        uiccCardExt.setUiccCard(uiccSlotEx.getUiccSlot().getUiccCard());
        return uiccCardExt;
    }

    public UiccCard getUiccCard() {
        return this.mUiccCard;
    }

    public void setUiccCard(UiccCard uiccCard) {
        this.mUiccCard = uiccCard;
    }

    public UiccCardApplicationEx getApplication(int family) {
        UiccProfile uiccProfile;
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null || (uiccProfile = uiccCard.getUiccProfile()) == null) {
            return null;
        }
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(uiccProfile.getApplication(family));
        return uiccCardApplicationEx;
    }

    public UiccCardApplicationEx getApplicationByType(int type) {
        UiccCardApplication uiccCardApplication;
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null || (uiccCardApplication = uiccCard.getApplicationByType(type)) == null) {
            return null;
        }
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(uiccCardApplication);
        return uiccCardApplicationEx;
    }

    public IccCardStatusExt.CardStateEx getCardState() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            return IccCardStatusExt.CardStateEx.getCardStateExByCardState(uiccCard.getCardState());
        }
        return IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
    }

    public IccRecordsEx getIccRecords() {
        UiccProfile uiccProfile;
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null || (uiccProfile = uiccCard.getUiccProfile()) == null) {
            return null;
        }
        return uiccProfile.getIccRecordsHw();
    }

    public String getIccId() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            return uiccCard.getIccId();
        }
        return null;
    }

    public UiccProfileEx getUiccProfile() {
        if (this.mUiccCard == null) {
            return null;
        }
        UiccProfileEx uiccProfileEx = new UiccProfileEx();
        uiccProfileEx.setUiccProfile(this.mUiccCard.getUiccProfile());
        return uiccProfileEx;
    }

    public int getCdmaSubscriptionAppIndex() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return -1;
        }
        return uiccCard.getCdmaSubscriptionAppIndex();
    }

    public int getGsmUmtsSubscriptionAppIndex() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return -1;
        }
        return uiccCard.getGsmUmtsSubscriptionAppIndex();
    }

    public boolean isEuiccCard() {
        return this.mUiccCard instanceof EuiccCard;
    }

    public int getNumApplications() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return 0;
        }
        return uiccCard.getNumApplications();
    }

    public int getPhoneId() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return 0;
        }
        return uiccCard.getPhoneId();
    }

    public boolean isCardUimLocked() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return false;
        }
        return uiccCard.isCardUimLocked();
    }

    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard == null) {
            return -1;
        }
        return uiccCard.getCarrierPrivilegeStatusForCurrentTransaction(packageManager);
    }

    public boolean isSameUiccCard(UiccCardExt uiccCardExt) {
        if (uiccCardExt == null || this.mUiccCard != uiccCardExt.getUiccCard()) {
            return false;
        }
        return true;
    }

    public String getEid() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard instanceof EuiccCard) {
            return ((EuiccCard) uiccCard).getEid();
        }
        return null;
    }

    public void registerForEidReady(Handler h, int what, Object obj) {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard instanceof EuiccCard) {
            ((EuiccCard) uiccCard).registerForEidReady(h, what, obj);
        }
    }

    public void unregisterForEidReady(Handler h) {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard instanceof EuiccCard) {
            ((EuiccCard) uiccCard).unregisterForEidReady(h);
        }
    }

    public String getCardId() {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            return uiccCard.getCardId();
        }
        return null;
    }
}
