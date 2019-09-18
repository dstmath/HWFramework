package com.huawei.android.telephony;

import android.telephony.HwTelephonyManagerInner;
import com.huawei.internal.telephony.NetworkInfoWithActCustEx;

public class TelephonyManagerCustEx {

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

    public enum DataSettingModeTypeEx {
        MODE_LTE_OFF_EX,
        MODE_LTETDD_ONLY_EX,
        MODE_LTE_AND_AUTO_EX,
        MODE_ERROR_EX
    }

    public static void setDataSettingMode(DataSettingModeTypeEx dataMode) {
        HwTelephonyManagerInner.DataSettingModeType tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
        switch (AnonymousClass1.$SwitchMap$com$huawei$android$telephony$TelephonyManagerCustEx$DataSettingModeTypeEx[dataMode.ordinal()]) {
            case 1:
                tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
                break;
            case 2:
                tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTETDD_ONLY;
                break;
            case 3:
                tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_AND_AUTO;
                break;
            case NetworkInfoWithActCustEx.ACT_UTRAN:
                tempMode = HwTelephonyManagerInner.DataSettingModeType.MODE_ERROR;
                break;
        }
        HwTelephonyManagerInner.getDefault().setDataSettingMode(tempMode);
    }

    public static DataSettingModeTypeEx getDataSettingMode() {
        DataSettingModeTypeEx tempModeEx = DataSettingModeTypeEx.MODE_LTE_OFF_EX;
        HwTelephonyManagerInner.DataSettingModeType dataSettingModeType = HwTelephonyManagerInner.DataSettingModeType.MODE_LTE_OFF;
        switch (AnonymousClass1.$SwitchMap$android$telephony$HwTelephonyManagerInner$DataSettingModeType[HwTelephonyManagerInner.getDefault().getDataSettingMode().ordinal()]) {
            case 1:
                return DataSettingModeTypeEx.MODE_LTE_OFF_EX;
            case 2:
                return DataSettingModeTypeEx.MODE_LTETDD_ONLY_EX;
            case 3:
                return DataSettingModeTypeEx.MODE_LTE_AND_AUTO_EX;
            case NetworkInfoWithActCustEx.ACT_UTRAN:
                return DataSettingModeTypeEx.MODE_ERROR_EX;
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
