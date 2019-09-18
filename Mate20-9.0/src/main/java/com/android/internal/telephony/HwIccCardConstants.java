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
            switch (this) {
                case ABSENT:
                    return "ABSENT";
                case PIN_REQUIRED:
                    return "LOCKED";
                case PUK_REQUIRED:
                    return "LOCKED";
                case NETWORK_LOCKED:
                case SIM_NETWORK_SUBSET_LOCKED:
                case SIM_CORPORATE_LOCKED:
                case SIM_SERVICE_PROVIDER_LOCKED:
                case SIM_SIM_LOCKED:
                case RUIM_NETWORK1_LOCKED:
                case RUIM_NETWORK2_LOCKED:
                case RUIM_HRPD_LOCKED:
                case RUIM_CORPORATE_LOCKED:
                case RUIM_SERVICE_PROVIDER_LOCKED:
                case RUIM_RUIM_LOCKED:
                case SIM_NETWORK_LOCKED_PUK:
                case SIM_NETWORK_SUBSET_LOCKED_PUK:
                case SIM_CORPORATE_LOCKED_PUK:
                case SIM_SERVICE_PROVIDER_LOCKED_PUK:
                    return "LOCKED";
                case READY:
                    return "READY";
                case NOT_READY:
                    return "NOT_READY";
                case PERM_DISABLED:
                    return "LOCKED";
                case CARD_IO_ERROR:
                    return "CARD_IO_ERROR";
                case DEACTIVED:
                    return HwIccCardConstants.INTENT_VALUE_ICC_DEACTIVED;
                default:
                    return "UNKNOWN";
            }
        }

        public String getReason() {
            switch (this) {
                case PIN_REQUIRED:
                    return "PIN";
                case PUK_REQUIRED:
                    return "PUK";
                case NETWORK_LOCKED:
                    return "NETWORK";
                case SIM_NETWORK_SUBSET_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                case SIM_CORPORATE_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                case SIM_SERVICE_PROVIDER_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                case SIM_SIM_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SIM;
                case RUIM_NETWORK1_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK1;
                case RUIM_NETWORK2_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK2;
                case RUIM_HRPD_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_HRPD;
                case RUIM_CORPORATE_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_CORPORATE;
                case RUIM_SERVICE_PROVIDER_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER;
                case RUIM_RUIM_LOCKED:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_RUIM;
                case SIM_NETWORK_LOCKED_PUK:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_PUK;
                case SIM_NETWORK_SUBSET_LOCKED_PUK:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK;
                case SIM_CORPORATE_LOCKED_PUK:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE_PUK;
                case SIM_SERVICE_PROVIDER_LOCKED_PUK:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK;
                case PERM_DISABLED:
                    return "PERM_DISABLED";
                case CARD_IO_ERROR:
                    return "CARD_IO_ERROR";
                default:
                    return null;
            }
        }
    }
}
