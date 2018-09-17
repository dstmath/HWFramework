package com.android.internal.telephony.uicc;

public final class UsimServiceTable extends IccServiceTable {

    public enum UsimService {
        PHONEBOOK,
        FDN,
        FDN_EXTENSION,
        SDN,
        SDN_EXTENSION,
        BDN,
        BDN_EXTENSION,
        OUTGOING_CALL_INFO,
        INCOMING_CALL_INFO,
        SM_STORAGE,
        SM_STATUS_REPORTS,
        SM_SERVICE_PARAMS,
        ADVICE_OF_CHARGE,
        CAP_CONFIG_PARAMS_2,
        CB_MESSAGE_ID,
        CB_MESSAGE_ID_RANGES,
        GROUP_ID_LEVEL_1,
        GROUP_ID_LEVEL_2,
        SPN,
        USER_PLMN_SELECT,
        MSISDN,
        IMAGE,
        LOCALISED_SERVICE_AREAS,
        EMLPP,
        EMLPP_AUTO_ANSWER,
        RFU,
        GSM_ACCESS,
        DATA_DL_VIA_SMS_PP,
        DATA_DL_VIA_SMS_CB,
        CALL_CONTROL_BY_USIM,
        MO_SMS_CONTROL_BY_USIM,
        RUN_AT_COMMAND,
        IGNORED_1,
        ENABLED_SERVICES_TABLE,
        APN_CONTROL_LIST,
        DEPERSONALISATION_CONTROL_KEYS,
        COOPERATIVE_NETWORK_LIST,
        GSM_SECURITY_CONTEXT,
        CPBCCH_INFO,
        INVESTIGATION_SCAN,
        MEXE,
        OPERATOR_PLMN_SELECT,
        HPLMN_SELECT,
        EXTENSION_5,
        PLMN_NETWORK_NAME,
        OPERATOR_PLMN_LIST,
        MBDN,
        MWI_STATUS,
        CFI_STATUS,
        IGNORED_2,
        SERVICE_PROVIDER_DISPLAY_INFO,
        MMS_NOTIFICATION,
        MMS_NOTIFICATION_EXTENSION,
        GPRS_CALL_CONTROL_BY_USIM,
        MMS_CONNECTIVITY_PARAMS,
        NETWORK_INDICATION_OF_ALERTING,
        VGCS_GROUP_ID_LIST,
        VBS_GROUP_ID_LIST,
        PSEUDONYM,
        IWLAN_USER_PLMN_SELECT,
        IWLAN_OPERATOR_PLMN_SELECT,
        USER_WSID_LIST,
        OPERATOR_WSID_LIST,
        VGCS_SECURITY,
        VBS_SECURITY,
        WLAN_REAUTH_IDENTITY,
        MM_STORAGE,
        GBA,
        MBMS_SECURITY,
        DATA_DL_VIA_USSD,
        EQUIVALENT_HPLMN,
        TERMINAL_PROFILE_AFTER_UICC_ACTIVATION,
        EQUIVALENT_HPLMN_PRESENTATION,
        LAST_RPLMN_SELECTION_INDICATION,
        OMA_BCAST_PROFILE,
        GBA_LOCAL_KEY_ESTABLISHMENT,
        TERMINAL_APPLICATIONS,
        SPN_ICON,
        PLMN_NETWORK_NAME_ICON,
        USIM_IP_CONNECTION_PARAMS,
        IWLAN_HOME_ID_LIST,
        IWLAN_EQUIVALENT_HPLMN_PRESENTATION,
        IWLAN_HPLMN_PRIORITY_INDICATION,
        IWLAN_LAST_REGISTERED_PLMN,
        EPS_MOBILITY_MANAGEMENT_INFO,
        ALLOWED_CSG_LISTS_AND_INDICATIONS,
        CALL_CONTROL_ON_EPS_PDN_CONNECTION_BY_USIM,
        HPLMN_DIRECT_ACCESS,
        ECALL_DATA,
        OPERATOR_CSG_LISTS_AND_INDICATIONS,
        SM_OVER_IP,
        CSG_DISPLAY_CONTROL,
        IMS_COMMUNICATION_CONTROL_BY_USIM,
        EXTENDED_TERMINAL_APPLICATIONS,
        UICC_ACCESS_TO_IMS,
        NAS_CONFIG_BY_USIM
    }

    public UsimServiceTable(byte[] table) {
        super(table);
    }

    public boolean isAvailable(UsimService service) {
        return super.isAvailable(service.ordinal());
    }

    protected String getTag() {
        return "UsimServiceTable";
    }

    protected Object[] getValues() {
        return UsimService.values();
    }
}
