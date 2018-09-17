package com.huawei.android.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwTelephonyManagerInner.DataSettingModeType;
import com.huawei.internal.telephony.NetworkInfoWithActCustEx;

public class TelephonyManagerCustEx {
    private static final /* synthetic */ int[] -android-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -com-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues = null;

    public enum DataSettingModeTypeEx {
        MODE_LTE_OFF_EX,
        MODE_LTETDD_ONLY_EX,
        MODE_LTE_AND_AUTO_EX,
        MODE_ERROR_EX
    }

    private static /* synthetic */ int[] -getandroid-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues() {
        if (-android-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues != null) {
            return -android-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues;
        }
        int[] iArr = new int[DataSettingModeType.values().length];
        try {
            iArr[DataSettingModeType.MODE_ERROR.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataSettingModeType.MODE_LTETDD_ONLY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataSettingModeType.MODE_LTE_AND_AUTO.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataSettingModeType.MODE_LTE_OFF.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues() {
        if (-com-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues != null) {
            return -com-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues;
        }
        int[] iArr = new int[DataSettingModeTypeEx.values().length];
        try {
            iArr[DataSettingModeTypeEx.MODE_ERROR_EX.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataSettingModeTypeEx.MODE_LTETDD_ONLY_EX.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataSettingModeTypeEx.MODE_LTE_AND_AUTO_EX.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataSettingModeTypeEx.MODE_LTE_OFF_EX.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues = iArr;
        return iArr;
    }

    public static void setDataSettingMode(DataSettingModeTypeEx dataMode) {
        DataSettingModeType tempMode = DataSettingModeType.MODE_LTE_OFF;
        switch (-getcom-huawei-android-telephony-TelephonyManagerCustEx$DataSettingModeTypeExSwitchesValues()[dataMode.ordinal()]) {
            case 1:
                tempMode = DataSettingModeType.MODE_ERROR;
                break;
            case 2:
                tempMode = DataSettingModeType.MODE_LTETDD_ONLY;
                break;
            case 3:
                tempMode = DataSettingModeType.MODE_LTE_AND_AUTO;
                break;
            case NetworkInfoWithActCustEx.ACT_UTRAN /*4*/:
                tempMode = DataSettingModeType.MODE_LTE_OFF;
                break;
        }
        HwTelephonyManagerInner.getDefault().setDataSettingMode(tempMode);
    }

    public static DataSettingModeTypeEx getDataSettingMode() {
        DataSettingModeTypeEx tempModeEx = DataSettingModeTypeEx.MODE_LTE_OFF_EX;
        DataSettingModeType tempMode = DataSettingModeType.MODE_LTE_OFF;
        switch (-getandroid-telephony-HwTelephonyManagerInner$DataSettingModeTypeSwitchesValues()[HwTelephonyManagerInner.getDefault().getDataSettingMode().ordinal()]) {
            case 1:
                return DataSettingModeTypeEx.MODE_ERROR_EX;
            case 2:
                return DataSettingModeTypeEx.MODE_LTETDD_ONLY_EX;
            case 3:
                return DataSettingModeTypeEx.MODE_LTE_AND_AUTO_EX;
            case NetworkInfoWithActCustEx.ACT_UTRAN /*4*/:
                return DataSettingModeTypeEx.MODE_LTE_OFF_EX;
            default:
                return tempModeEx;
        }
    }

    public static void setLteServiceAbility(int sub, int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(ability);
    }

    public static int checkLteServiceAbiltiy(int sub, int nwMode) {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility();
    }

    public static int getDataState(long subId) {
        return HwTelephonyManagerInner.getDefault().getDataState(subId);
    }
}
