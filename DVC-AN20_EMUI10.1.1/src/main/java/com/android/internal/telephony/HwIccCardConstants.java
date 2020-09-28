package com.android.internal.telephony;

import android.telephony.HwVSimManager;

public class HwIccCardConstants {
    public static final String INTENT_VALUE_ICC_DEACTIVED = "DEACTIVED";
    public static final String INTENT_VALUE_ICC_SIM_REFRESH = "SIM_REFRESH";
    public static final String INTENT_VALUE_LOCKED_CORPORATE = "SIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_CORPORATE_PUK = "SIM LOCK CORPORATE BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_PUK = "SIM LOCK BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET = "SIM NETWORK SUBSET";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK = "SIM LOCK NETWORK SUBSET BLOCK";
    public static final String INTENT_VALUE_LOCKED_RUIM_CORPORATE = "RUIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_RUIM_HRPD = "RUIM HRPD";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK1 = "RUIM NETWORK1";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK2 = "RUIM NETWORK2";
    public static final String INTENT_VALUE_LOCKED_RUIM_RUIM = "RUIM RUIM";
    public static final String INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER = "RUIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER = "SIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK = "SIM LOCK SERVICE PROVIDERBLOCK";
    public static final String INTENT_VALUE_LOCKED_SIM = "SIM SIM";

    /* renamed from: com.android.internal.telephony.HwIccCardConstants$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState = new int[HwState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.PIN_REQUIRED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.PUK_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.NETWORK_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_NETWORK_SUBSET_LOCKED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_CORPORATE_LOCKED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_SIM_LOCKED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_NETWORK1_LOCKED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_NETWORK2_LOCKED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_HRPD_LOCKED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_CORPORATE_LOCKED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.RUIM_RUIM_LOCKED.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_NETWORK_LOCKED_PUK.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_NETWORK_SUBSET_LOCKED_PUK.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_CORPORATE_LOCKED_PUK.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.SIM_SERVICE_PROVIDER_LOCKED_PUK.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.READY.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.NOT_READY.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.PERM_DISABLED.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.CARD_IO_ERROR.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwState.DEACTIVED.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
        }
    }

    public enum HwState {
        UNKNOWN,
        ABSENT,
        PIN_REQUIRED,
        PUK_REQUIRED,
        NETWORK_LOCKED,
        SIM_NETWORK_SUBSET_LOCKED,
        SIM_CORPORATE_LOCKED,
        SIM_SERVICE_PROVIDER_LOCKED,
        SIM_SIM_LOCKED,
        RUIM_NETWORK1_LOCKED,
        RUIM_NETWORK2_LOCKED,
        RUIM_HRPD_LOCKED,
        RUIM_CORPORATE_LOCKED,
        RUIM_SERVICE_PROVIDER_LOCKED,
        RUIM_RUIM_LOCKED,
        SIM_NETWORK_LOCKED_PUK,
        SIM_NETWORK_SUBSET_LOCKED_PUK,
        SIM_CORPORATE_LOCKED_PUK,
        SIM_SERVICE_PROVIDER_LOCKED_PUK,
        READY,
        NOT_READY,
        CARD_IO_ERROR,
        DEACTIVED,
        PERM_DISABLED;

        public String getIntentString() {
            switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[ordinal()]) {
                case 1:
                    return "ABSENT";
                case 2:
                    return "LOCKED";
                case 3:
                    return "LOCKED";
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case HwVSimManager.NETWORK_TYPE_HSUPA:
                case 10:
                case HwVSimManager.NETWORK_TYPE_IDEN:
                case 12:
                case HwVSimManager.NETWORK_TYPE_LTE:
                case HwVSimManager.NETWORK_TYPE_EHRPD:
                case HwVSimManager.NETWORK_TYPE_HSPAP:
                case 16:
                case HwVSimManager.NETWORK_TYPE_TDS:
                case HwVSimManager.NETWORK_TYPE_TDS_HSDPA:
                    return "LOCKED";
                case HwVSimManager.NETWORK_TYPE_TDS_HSUPA:
                    return "READY";
                case 20:
                    return "NOT_READY";
                case 21:
                    return "LOCKED";
                case 22:
                    return "CARD_IO_ERROR";
                case 23:
                    return HwIccCardConstants.INTENT_VALUE_ICC_DEACTIVED;
                default:
                    return "UNKNOWN";
            }
        }

        public String getReason() {
            switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[ordinal()]) {
                case 2:
                    return "PIN";
                case 3:
                    return "PUK";
                case 4:
                    return "NETWORK";
                case 5:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                case 6:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                case 7:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                case 8:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SIM;
                case HwVSimManager.NETWORK_TYPE_HSUPA:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK1;
                case 10:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK2;
                case HwVSimManager.NETWORK_TYPE_IDEN:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_HRPD;
                case 12:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_CORPORATE;
                case HwVSimManager.NETWORK_TYPE_LTE:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER;
                case HwVSimManager.NETWORK_TYPE_EHRPD:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_RUIM;
                case HwVSimManager.NETWORK_TYPE_HSPAP:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_PUK;
                case 16:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK;
                case HwVSimManager.NETWORK_TYPE_TDS:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE_PUK;
                case HwVSimManager.NETWORK_TYPE_TDS_HSDPA:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK;
                case HwVSimManager.NETWORK_TYPE_TDS_HSUPA:
                case 20:
                default:
                    return null;
                case 21:
                    return "PERM_DISABLED";
                case 22:
                    return "CARD_IO_ERROR";
            }
        }
    }
}
