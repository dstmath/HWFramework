package com.android.internal.telephony;

import android.content.ContentValues;
import com.huawei.android.telephony.RlogEx;

public class HwUiccPhoneBookController implements IHwUiccPhoneBookControllerEx {
    private static final String LOG_TAG = "HwUiccPhoneBookController";
    private IUiccPhoneBookControllerInner mUiccPhoneBookController;

    public HwUiccPhoneBookController(IUiccPhoneBookControllerInner iUiccPhoneBookControllerInner) {
        this.mUiccPhoneBookController = iUiccPhoneBookControllerInner;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsWithContentValuesInEfBySearchHW(efid, values, pin2);
        }
        loge("updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimAdnRecordsInEfByIndexHW(efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
        }
        loge("updateUsimAdnRecordsInEfByIndexUsingSubIdHW iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int getAdnCountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnCountHw();
        }
        loge("getAdnCountHw iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getAnrCountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAnrCountHw();
        }
        loge("getAnrCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getEmailCountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getEmailCountHw();
        }
        loge("getEmailCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getSpareAnrCountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareAnrCountHw();
        }
        loge("getSpareAnrCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getSpareEmailCountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareEmailCountHw();
        }
        loge("getSpareEmailCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int[] getRecordsSizeUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getRecordsSizeHw();
        }
        loge("getRecordsSizeHW iccPbkIntMgr isnull for Subscription:" + subId);
        return new int[9];
    }

    public int getSpareExt1CountUsingSubIdHW(int subId) {
        IIccPhoneBookInterfaceManagerInner iccPbkIntMgr = this.mUiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareExt1CountHw();
        }
        loge("getRecordsSizeHW iccPbkIntMgr isnull for Subscription:" + subId);
        return -1;
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
