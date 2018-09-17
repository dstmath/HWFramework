package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.hardware.radio.V1_0.DataCallFailCause;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import java.util.HashMap;
import java.util.HashSet;

public enum DcFailCause {
    NONE(0),
    OPERATOR_BARRED(8),
    NAS_SIGNALLING(14),
    LLC_SNDCP(25),
    INSUFFICIENT_RESOURCES(26),
    MISSING_UNKNOWN_APN(27),
    UNKNOWN_PDP_ADDRESS_TYPE(28),
    USER_AUTHENTICATION(29),
    ACTIVATION_REJECT_GGSN(30),
    ACTIVATION_REJECT_UNSPECIFIED(31),
    SERVICE_OPTION_NOT_SUPPORTED(32),
    SERVICE_OPTION_NOT_SUBSCRIBED(33),
    SERVICE_OPTION_OUT_OF_ORDER(34),
    NSAPI_IN_USE(35),
    REGULAR_DEACTIVATION(36),
    QOS_NOT_ACCEPTED(37),
    NETWORK_FAILURE(38),
    UMTS_REACTIVATION_REQ(39),
    FEATURE_NOT_SUPP(40),
    TFT_SEMANTIC_ERROR(41),
    TFT_SYTAX_ERROR(42),
    UNKNOWN_PDP_CONTEXT(43),
    FILTER_SEMANTIC_ERROR(44),
    FILTER_SYTAX_ERROR(45),
    PDP_WITHOUT_ACTIVE_TFT(46),
    ONLY_IPV4_ALLOWED(50),
    ONLY_IPV6_ALLOWED(51),
    ONLY_SINGLE_BEARER_ALLOWED(52),
    ESM_INFO_NOT_RECEIVED(53),
    PDN_CONN_DOES_NOT_EXIST(54),
    MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED(55),
    MAX_ACTIVE_PDP_CONTEXT_REACHED(65),
    UNSUPPORTED_APN_IN_CURRENT_PLMN(66),
    INVALID_TRANSACTION_ID(81),
    MESSAGE_INCORRECT_SEMANTIC(95),
    INVALID_MANDATORY_INFO(96),
    MESSAGE_TYPE_UNSUPPORTED(97),
    MSG_TYPE_NONCOMPATIBLE_STATE(98),
    UNKNOWN_INFO_ELEMENT(99),
    CONDITIONAL_IE_ERROR(100),
    MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE(101),
    PROTOCOL_ERRORS(111),
    APN_TYPE_CONFLICT(112),
    INVALID_PCSCF_ADDR(113),
    INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN(114),
    EMM_ACCESS_BARRED(115),
    EMERGENCY_IFACE_ONLY(116),
    IFACE_MISMATCH(117),
    COMPANION_IFACE_IN_USE(118),
    IP_ADDRESS_MISMATCH(119),
    IFACE_AND_POL_FAMILY_MISMATCH(120),
    EMM_ACCESS_BARRED_INFINITE_RETRY(121),
    AUTH_FAILURE_ON_EMERGENCY_CALL(122),
    NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN(123),
    OEM_DCFAILCAUSE_1(DataCallFailCause.OEM_DCFAILCAUSE_1),
    OEM_DCFAILCAUSE_2(DataCallFailCause.OEM_DCFAILCAUSE_2),
    OEM_DCFAILCAUSE_3(DataCallFailCause.OEM_DCFAILCAUSE_3),
    OEM_DCFAILCAUSE_4(DataCallFailCause.OEM_DCFAILCAUSE_4),
    OEM_DCFAILCAUSE_5(DataCallFailCause.OEM_DCFAILCAUSE_5),
    OEM_DCFAILCAUSE_6(DataCallFailCause.OEM_DCFAILCAUSE_6),
    OEM_DCFAILCAUSE_7(DataCallFailCause.OEM_DCFAILCAUSE_7),
    OEM_DCFAILCAUSE_8(DataCallFailCause.OEM_DCFAILCAUSE_8),
    OEM_DCFAILCAUSE_9(DataCallFailCause.OEM_DCFAILCAUSE_9),
    OEM_DCFAILCAUSE_10(DataCallFailCause.OEM_DCFAILCAUSE_10),
    OEM_DCFAILCAUSE_11(DataCallFailCause.OEM_DCFAILCAUSE_11),
    OEM_DCFAILCAUSE_12(DataCallFailCause.OEM_DCFAILCAUSE_12),
    OEM_DCFAILCAUSE_13(DataCallFailCause.OEM_DCFAILCAUSE_13),
    OEM_DCFAILCAUSE_14(DataCallFailCause.OEM_DCFAILCAUSE_14),
    OEM_DCFAILCAUSE_15(DataCallFailCause.OEM_DCFAILCAUSE_15),
    REGISTRATION_FAIL(-1),
    GPRS_REGISTRATION_FAIL(-2),
    SIGNAL_LOST(-3),
    PREF_RADIO_TECH_CHANGED(-4),
    RADIO_POWER_OFF(-5),
    TETHERED_CALL_ACTIVE(-6),
    ERROR_UNSPECIFIED(65535),
    PDP_ACTIVE_LIMIT(2051),
    UNKNOWN(65536),
    RADIO_NOT_AVAILABLE(65537),
    UNACCEPTABLE_NETWORK_PARAMETER(65538),
    CONNECTION_TO_DATACONNECTIONAC_BROKEN(65539),
    LOST_CONNECTION(65540),
    RESET_BY_FRAMEWORK(65541);
    
    private static final String mCustPsPermanentFailure = null;
    private static final HashMap<Integer, DcFailCause> sErrorCodeToFailCauseMap = null;
    private static final HashMap<Integer, HashSet<DcFailCause>> sPermanentFailureCache = null;
    private final int mErrorCode;

    static {
        sErrorCodeToFailCauseMap = new HashMap();
        DcFailCause[] values = values();
        int length = values.length;
        int i;
        while (i < length) {
            DcFailCause fc = values[i];
            sErrorCodeToFailCauseMap.put(Integer.valueOf(fc.getErrorCode()), fc);
            i++;
        }
        sPermanentFailureCache = new HashMap();
        mCustPsPermanentFailure = SystemProperties.get("ro.hwpp_ds_permanent_fail", "");
    }

    private DcFailCause(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public boolean isRestartRadioFail(Context context, int subId) {
        if (this == REGULAR_DEACTIVATION) {
            CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(subId);
                if (b != null) {
                    return b.getBoolean("restart_radio_on_pdp_fail_regular_deactivation_bool");
                }
            }
        }
        return false;
    }

    public boolean isPermanentFailure(Context context, int subId) {
        int i = 0;
        if (isMatchedDsFail()) {
            return false;
        }
        if (isMatchedDsPermanentFail()) {
            return true;
        }
        boolean contains;
        synchronized (sPermanentFailureCache) {
            HashSet<DcFailCause> permanentFailureSet = (HashSet) sPermanentFailureCache.get(Integer.valueOf(subId));
            if (permanentFailureSet == null) {
                CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
                if (configManager != null) {
                    PersistableBundle b = configManager.getConfigForSubId(subId);
                    if (b != null) {
                        String[] permanentFailureStrings = b.getStringArray("carrier_data_call_permanent_failure_strings");
                        if (permanentFailureStrings != null) {
                            permanentFailureSet = new HashSet();
                            int length = permanentFailureStrings.length;
                            while (i < length) {
                                permanentFailureSet.add(valueOf(permanentFailureStrings[i]));
                                i++;
                            }
                        }
                    }
                }
                if (permanentFailureSet == null) {
                    permanentFailureSet = new HashSet<DcFailCause>() {
                        {
                            add(DcFailCause.OPERATOR_BARRED);
                            add(DcFailCause.MISSING_UNKNOWN_APN);
                            add(DcFailCause.UNKNOWN_PDP_ADDRESS_TYPE);
                            add(DcFailCause.USER_AUTHENTICATION);
                            add(DcFailCause.ACTIVATION_REJECT_GGSN);
                            add(DcFailCause.SERVICE_OPTION_NOT_SUPPORTED);
                            add(DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED);
                            add(DcFailCause.NSAPI_IN_USE);
                            add(DcFailCause.ONLY_IPV4_ALLOWED);
                            add(DcFailCause.ONLY_IPV6_ALLOWED);
                            add(DcFailCause.PROTOCOL_ERRORS);
                            add(DcFailCause.RADIO_POWER_OFF);
                            add(DcFailCause.TETHERED_CALL_ACTIVE);
                            add(DcFailCause.RADIO_NOT_AVAILABLE);
                            add(DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER);
                            add(DcFailCause.SIGNAL_LOST);
                        }
                    };
                }
                sPermanentFailureCache.put(Integer.valueOf(subId), permanentFailureSet);
            }
            contains = permanentFailureSet.contains(this);
        }
        return contains;
    }

    public boolean isEventLoggable() {
        if (this == OPERATOR_BARRED || this == INSUFFICIENT_RESOURCES || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == ACTIVATION_REJECT_UNSPECIFIED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_OUT_OF_ORDER || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == SIGNAL_LOST || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == UNACCEPTABLE_NETWORK_PARAMETER) {
            return true;
        }
        return false;
    }

    public static DcFailCause fromInt(int errorCode) {
        DcFailCause fc = (DcFailCause) sErrorCodeToFailCauseMap.get(Integer.valueOf(errorCode));
        if (fc == null) {
            return UNKNOWN;
        }
        return fc;
    }

    private boolean isMatchedDsFail() {
        boolean isMatched = false;
        try {
            String cntelfailcau = SystemProperties.get("ro.hwpp_ds_fail", "");
            Rlog.d("DcFailCause", "isMatchedDsFail cntelfailcau: " + cntelfailcau);
            for (String fcau : cntelfailcau.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.d("DcFailCause", "ErrorCode has been matched: " + this.mErrorCode);
                    isMatched = true;
                }
            }
        } catch (Exception ex) {
            Rlog.e("DcFailCause", "Exception isMatchedDsFail get ds fail cause, ", ex);
        }
        return isMatched;
    }

    private boolean isMatchedDsPermanentFail() {
        boolean isMatched = false;
        try {
            Rlog.d("DcFailCause", "isMatchedDsPermanentFail mCustPsPermanentFailure: " + mCustPsPermanentFailure);
            for (String fcau : mCustPsPermanentFailure.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.d("DcFailCause", "isMatchedDsPermanentFail, ErrorCode has been matched: " + this.mErrorCode);
                    isMatched = true;
                }
            }
        } catch (Exception e) {
            Rlog.e("DcFailCause", "Exception get ds Permanent fail cause: ", e);
        }
        return isMatched;
    }
}
