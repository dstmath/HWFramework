package com.android.internal.telephony;

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
            switch (-getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues()[ordinal()]) {
                case 1:
                    return "ABSENT";
                case 2:
                    return "CARD_IO_ERROR";
                case 3:
                    return HwIccCardConstants.INTENT_VALUE_ICC_DEACTIVED;
                case 4:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                    return "LOCKED";
                case 5:
                    return "NOT_READY";
                case 6:
                    return "LOCKED";
                case 7:
                    return "LOCKED";
                case 8:
                    return "LOCKED";
                case 9:
                    return "READY";
                default:
                    return "UNKNOWN";
            }
        }

        public String getReason() {
            switch (-getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues()[ordinal()]) {
                case 2:
                    return "CARD_IO_ERROR";
                case 4:
                    return "NETWORK";
                case 6:
                    return "PERM_DISABLED";
                case 7:
                    return "PIN";
                case 8:
                    return "PUK";
                case 10:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_CORPORATE;
                case 11:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_HRPD;
                case 12:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK1;
                case 13:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK2;
                case 14:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_RUIM;
                case 15:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER;
                case 16:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                case 17:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE_PUK;
                case 18:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_PUK;
                case 19:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                case 20:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK;
                case 21:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                case 22:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK;
                case 23:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SIM;
                default:
                    return null;
            }
        }
    }
}
