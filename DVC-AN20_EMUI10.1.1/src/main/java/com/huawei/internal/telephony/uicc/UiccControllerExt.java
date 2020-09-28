package com.huawei.internal.telephony.uicc;

import android.os.Handler;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;

public class UiccControllerExt {
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    private UiccController mUiccController;

    public void setUiccController(UiccController uiccController) {
        this.mUiccController = uiccController;
    }

    public static UiccControllerExt getInstance() {
        UiccController instance = UiccController.getInstance();
        if (instance == null) {
            return null;
        }
        UiccControllerExt uiccControllerExt = new UiccControllerExt();
        uiccControllerExt.setUiccController(instance);
        return uiccControllerExt;
    }

    public UiccCardExt getUiccCard(int slotId) {
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        if (uiccCard == null) {
            return null;
        }
        UiccCardExt uiccCardExt = new UiccCardExt();
        uiccCardExt.setUiccCard(uiccCard);
        return uiccCardExt;
    }

    public void disposeCard(int index) {
        this.mUiccController.disposeCard(index);
    }

    public void notifyFdnStatusChange() {
        this.mUiccController.notifyFdnStatusChange();
    }

    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        this.mUiccController.registerForFdnStatusChange(h, what, obj);
    }

    public void unregisterForFdnStatusChange(Handler h) {
        this.mUiccController.unregisterForFdnStatusChange(h);
    }

    public IccRecordsEx getIccRecords(int phoneId, int family) {
        IccRecordsEx iccRecordsEx = new IccRecordsEx();
        iccRecordsEx.setIccRecords(this.mUiccController.getIccRecords(phoneId, family));
        return iccRecordsEx;
    }

    public int getSlotIdFromPhoneId(int phoneId) {
        return this.mUiccController.getSlotIdFromPhoneId(phoneId);
    }

    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        return this.mUiccController.getUiccCardApplication(phoneId, family);
    }

    public UiccCardApplicationEx getUiccCardApplicationEx(int phoneId, int family) {
        UiccCardApplication uiccCardApplication = this.mUiccController.getUiccCardApplication(phoneId, family);
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(uiccCardApplication);
        return uiccCardApplicationEx;
    }

    public UiccCardExt[] getUiccCards() {
        return this.mUiccController.getUiccCards();
    }

    public void registerForIccChanged(Handler handler, int what, Object obj) {
        this.mUiccController.registerForIccChanged(handler, what, obj);
    }

    public int convertToPublicCardId(String cardString) {
        return this.mUiccController.convertToPublicCardId(cardString);
    }
}
