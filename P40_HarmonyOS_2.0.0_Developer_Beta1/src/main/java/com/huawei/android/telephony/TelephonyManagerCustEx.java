package com.huawei.android.telephony;

import android.telephony.HwTelephonyManagerInner;

public class TelephonyManagerCustEx {

    public enum DataSettingModeTypeEx {
        MODE_LTE_OFF_EX,
        MODE_LTETDD_ONLY_EX,
        MODE_LTE_AND_AUTO_EX,
        MODE_ERROR_EX
    }

    public static void setDataSettingMode(DataSettingModeTypeEx dataMode) {
        HwTelephonyManagerInner.DataSettingModeType tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
        int i = AnonymousClass1.$SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[dataMode.ordinal()];
        if (i == 1) {
            tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
        } else if (i == 2) {
            tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTETDD_ONLY;
        } else if (i == 3) {
            tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_AND_AUTO;
        } else if (i == 4) {
            tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_ERROR;
        }
        HwTelephonyManagerInner.getDefault().setDataSettingMode(tempMode);
    }

    public static DataSettingModeTypeEx getDataSettingMode() {
        DataSettingModeTypeEx tempModeEx = DataSettingModeTypeEx.MODE_LTE_OFF_EX;
        HwTelephonyManagerInner.DataSettingModeType dataSettingModeType = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
        int i = AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.getDefault().getDataSettingMode().ordinal()];
        if (i == 1) {
            return DataSettingModeTypeEx.MODE_LTE_OFF_EX;
        }
        if (i == 2) {
            return DataSettingModeTypeEx.MODE_LTETDD_ONLY_EX;
        }
        if (i == 3) {
            return DataSettingModeTypeEx.MODE_LTE_AND_AUTO_EX;
        }
        if (i != 4) {
            return tempModeEx;
        }
        return DataSettingModeTypeEx.MODE_ERROR_EX;
    }

    /* renamed from: com.huawei.android.telephony.TelephonyManagerCustEx$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType = new int[HwTelephonyManagerInner.DataSettingModeType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx = new int[DataSettingModeTypeEx.values().length];

        static {
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.DataSettingModeType.MODE_LTETDD_ONLY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_AND_AUTO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.DataSettingModeType.MODE_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[DataSettingModeTypeEx.MODE_LTE_OFF_EX.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[DataSettingModeTypeEx.MODE_LTETDD_ONLY_EX.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[DataSettingModeTypeEx.MODE_LTE_AND_AUTO_EX.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[DataSettingModeTypeEx.MODE_ERROR_EX.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
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
