package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.nano.TelephonyProto;
import com.google.android.mms.pdu.PduHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.IntPredicate;

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
    COMPANION_IFACE_IN_USE(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_COMPANION_IFACE_IN_USE),
    IP_ADDRESS_MISMATCH(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH),
    IFACE_AND_POL_FAMILY_MISMATCH(120),
    EMM_ACCESS_BARRED_INFINITE_RETRY(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY),
    AUTH_FAILURE_ON_EMERGENCY_CALL(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL),
    NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_DNS_ADDR),
    NETWORK_RECONFIGURE(PduHeaders.TOTALS),
    OEM_DCFAILCAUSE_1(4097),
    OEM_DCFAILCAUSE_2(4098),
    OEM_DCFAILCAUSE_3(4099),
    OEM_DCFAILCAUSE_4(4100),
    OEM_DCFAILCAUSE_5(4101),
    OEM_DCFAILCAUSE_6(4102),
    OEM_DCFAILCAUSE_7(4103),
    OEM_DCFAILCAUSE_8(4104),
    OEM_DCFAILCAUSE_9(4105),
    OEM_DCFAILCAUSE_10(4106),
    OEM_DCFAILCAUSE_11(4107),
    OEM_DCFAILCAUSE_12(4108),
    OEM_DCFAILCAUSE_13(4109),
    OEM_DCFAILCAUSE_14(4110),
    OEM_DCFAILCAUSE_15(4111),
    REGISTRATION_FAIL(-1),
    GPRS_REGISTRATION_FAIL(-2),
    SIGNAL_LOST(-3),
    PREF_RADIO_TECH_CHANGED(-4),
    RADIO_POWER_OFF(-5),
    TETHERED_CALL_ACTIVE(-6),
    ERROR_UNSPECIFIED(65535),
    PDP_ACTIVE_LIMIT(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_PDN_INACTIVITY_TIMER_EXPIRED),
    SM_MAX_TIME_OUT(130),
    EHSM_PPP_ERROR_CONN_EXISTS_FOR_THIS_APN(1821),
    DSM_UNKNOWN(3199),
    UNKNOWN(InboundSmsTracker.DEST_PORT_FLAG_NO_PORT),
    RADIO_NOT_AVAILABLE(65537),
    UNACCEPTABLE_NETWORK_PARAMETER(65538),
    CONNECTION_TO_DATACONNECTIONAC_BROKEN(65539),
    LOST_CONNECTION(65540),
    RESET_BY_FRAMEWORK(65541);
    
    private static final String mCustPsPermanentFailure = SystemProperties.get("ro.hwpp_ds_permanent_fail", PhoneConfigurationManager.SSSS);
    private static final HashMap<Integer, DcFailCause> sErrorCodeToFailCauseMap = new HashMap<>();
    private static final HashMap<Integer, HashSet<DcFailCause>> sPermanentFailureCache = new HashMap<>();
    private final int mErrorCode;

    static {
        DcFailCause[] values = values();
        for (DcFailCause fc : values) {
            sErrorCodeToFailCauseMap.put(Integer.valueOf(fc.getErrorCode()), fc);
        }
    }

    private DcFailCause(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public boolean isRadioRestartFailure(Context context, int subId) {
        PersistableBundle b;
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configManager == null || (b = configManager.getConfigForSubId(subId)) == null) {
            return false;
        }
        if (this == REGULAR_DEACTIVATION && b.getBoolean("restart_radio_on_pdp_fail_regular_deactivation_bool")) {
            return true;
        }
        int[] causeCodes = b.getIntArray("radio_restart_failure_causes_int_array");
        if (causeCodes != null) {
            return Arrays.stream(causeCodes).anyMatch(new IntPredicate() {
                /* class com.android.internal.telephony.dataconnection.$$Lambda$DcFailCause$8iNev91wyTaCZ2sT8xfno9dw64 */

                public final boolean test(int i) {
                    return DcFailCause.this.lambda$isRadioRestartFailure$0$DcFailCause(i);
                }
            });
        }
        return false;
    }

    public /* synthetic */ boolean lambda$isRadioRestartFailure$0$DcFailCause(int i) {
        return i == getErrorCode();
    }

    public boolean isPermanentFailure(Context context, int subId) {
        boolean contains;
        PersistableBundle b;
        String[] permanentFailureStrings;
        if (isMatchedDsFail()) {
            return false;
        }
        if (isMatchedDsPermanentFail()) {
            return true;
        }
        synchronized (sPermanentFailureCache) {
            HashSet<DcFailCause> permanentFailureSet = sPermanentFailureCache.get(Integer.valueOf(subId));
            if (permanentFailureSet == null) {
                CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
                if (!(configManager == null || (b = configManager.getConfigForSubId(subId)) == null || (permanentFailureStrings = b.getStringArray("carrier_data_call_permanent_failure_strings")) == null)) {
                    permanentFailureSet = new HashSet<>();
                    for (String failure : permanentFailureStrings) {
                        permanentFailureSet.add(valueOf(failure));
                    }
                }
                if (permanentFailureSet == null) {
                    permanentFailureSet = new HashSet<DcFailCause>() {
                        /* class com.android.internal.telephony.dataconnection.DcFailCause.AnonymousClass1 */

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
        return this == OPERATOR_BARRED || this == INSUFFICIENT_RESOURCES || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == ACTIVATION_REJECT_UNSPECIFIED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_OUT_OF_ORDER || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == SIGNAL_LOST || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == UNACCEPTABLE_NETWORK_PARAMETER;
    }

    public static DcFailCause fromInt(int errorCode) {
        DcFailCause fc = sErrorCodeToFailCauseMap.get(Integer.valueOf(errorCode));
        if (fc == null) {
            return UNKNOWN;
        }
        return fc;
    }

    private boolean isMatchedDsFail() {
        boolean isMatched = false;
        try {
            String cntelfailcau = SystemProperties.get("ro.hwpp_ds_fail", PhoneConfigurationManager.SSSS);
            Rlog.i("DcFailCause", "isMatchedDsFail cntelfailcau: " + cntelfailcau);
            for (String fcau : cntelfailcau.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.i("DcFailCause", "ErrorCode has been matched: " + this.mErrorCode);
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
            Rlog.i("DcFailCause", "isMatchedDsPermanentFail mCustPsPermanentFailure: " + mCustPsPermanentFailure);
            for (String fcau : mCustPsPermanentFailure.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.i("DcFailCause", "isMatchedDsPermanentFail, ErrorCode has been matched: " + this.mErrorCode);
                    isMatched = true;
                }
            }
        } catch (Exception e) {
            Rlog.e("DcFailCause", "Exception get ds Permanent fail cause: ", e);
        }
        return isMatched;
    }
}
