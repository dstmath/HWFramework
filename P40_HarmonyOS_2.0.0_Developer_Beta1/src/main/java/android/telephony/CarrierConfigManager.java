package android.telephony;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.job.JobInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.android.internal.telephony.AbstractPhoneConstants;
import com.android.internal.telephony.ICarrierConfigLoader;
import com.android.internal.telephony.PhoneConstants;

public class CarrierConfigManager {
    public static final String ACTION_CARRIER_CONFIG_CHANGED = "android.telephony.action.CARRIER_CONFIG_CHANGED";
    public static final int DATA_CYCLE_THRESHOLD_DISABLED = -2;
    @Deprecated
    public static final int DATA_CYCLE_USE_PLATFORM_DEFAULT = -1;
    public static final String EXTRA_SLOT_INDEX = "android.telephony.extra.SLOT_INDEX";
    public static final String EXTRA_SUBSCRIPTION_INDEX = "android.telephony.extra.SUBSCRIPTION_INDEX";
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    public static final String IMSI_KEY_AVAILABILITY_INT = "imsi_key_availability_int";
    public static final String IMSI_KEY_DOWNLOAD_URL_STRING = "imsi_key_download_url_string";
    private static final boolean IS_CSP_ENABLE = SystemProperties.getBoolean("ro.config.csp_enable", false);
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", WifiEnterpriseConfig.EMPTY_VALUE).contains("docomo");
    public static final String KEY_5G_ICON_CONFIGURATION_STRING = "5g_icon_configuration_string";
    public static final String KEY_ADDITIONAL_CALL_SETTING_BOOL = "additional_call_setting_bool";
    public static final String KEY_ADDITIONAL_SETTINGS_CALLER_ID_VISIBILITY_BOOL = "additional_settings_caller_id_visibility_bool";
    public static final String KEY_ADDITIONAL_SETTINGS_CALL_WAITING_VISIBILITY_BOOL = "additional_settings_call_waiting_visibility_bool";
    public static final String KEY_ALLOW_ADDING_APNS_BOOL = "allow_adding_apns_bool";
    public static final String KEY_ALLOW_ADD_CALL_DURING_VIDEO_CALL_BOOL = "allow_add_call_during_video_call";
    public static final String KEY_ALLOW_EMERGENCY_NUMBERS_IN_CALL_LOG_BOOL = "allow_emergency_numbers_in_call_log_bool";
    public static final String KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL = "allow_emergency_video_calls_bool";
    public static final String KEY_ALLOW_ENABLING_WAP_PUSH_SI_BOOL = "allowEnablingWapPushSI";
    public static final String KEY_ALLOW_ERI_BOOL = "allow_cdma_eri_bool";
    public static final String KEY_ALLOW_HOLD_IN_IMS_CALL_BOOL = "allow_hold_in_ims_call";
    public static final String KEY_ALLOW_LOCAL_DTMF_TONES_BOOL = "allow_local_dtmf_tones_bool";
    public static final String KEY_ALLOW_MERGE_WIFI_CALLS_WHEN_VOWIFI_OFF_BOOL = "allow_merge_wifi_calls_when_vowifi_off_bool";
    public static final String KEY_ALLOW_METERED_NETWORK_FOR_CERT_DOWNLOAD_BOOL = "allow_metered_network_for_cert_download_bool";
    public static final String KEY_ALLOW_NON_EMERGENCY_CALLS_IN_ECM_BOOL = "allow_non_emergency_calls_in_ecm_bool";
    public static final String KEY_ALLOW_USSD_REQUESTS_VIA_TELEPHONY_MANAGER_BOOL = "allow_ussd_requests_via_telephony_manager_bool";
    public static final String KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL = "allow_video_calling_fallback_bool";
    public static final String KEY_ALWAYS_PLAY_REMOTE_HOLD_TONE_BOOL = "always_play_remote_hold_tone_bool";
    public static final String KEY_ALWAYS_SHOW_DATA_RAT_ICON_BOOL = "always_show_data_rat_icon_bool";
    public static final String KEY_ALWAYS_SHOW_EMERGENCY_ALERT_ONOFF_BOOL = "always_show_emergency_alert_onoff_bool";
    public static final String KEY_ALWAYS_SHOW_PRIMARY_SIGNAL_BAR_IN_OPPORTUNISTIC_NETWORK_BOOLEAN = "always_show_primary_signal_bar_in_opportunistic_network_boolean";
    public static final String KEY_APN_EXPAND_BOOL = "apn_expand_bool";
    public static final String KEY_ASCII_7_BIT_SUPPORT_FOR_LONG_MESSAGE_BOOL = "ascii_7_bit_support_for_long_message_bool";
    public static final String KEY_AUTO_CANCEL_CS_REJECT_NOTIFICATION = "carrier_auto_cancel_cs_notification";
    public static final String KEY_AUTO_RETRY_ENABLED_BOOL = "auto_retry_enabled_bool";
    public static final String KEY_AUTO_RETRY_FAILED_WIFI_EMERGENCY_CALL = "auto_retry_failed_wifi_emergency_call";
    public static final String KEY_BOOSTED_LTE_EARFCNS_STRING_ARRAY = "boosted_lte_earfcns_string_array";
    public static final String KEY_BROADCAST_EMERGENCY_CALL_STATE_CHANGES_BOOL = "broadcast_emergency_call_state_changes_bool";
    public static final String KEY_CALLER_ID_OVER_UT_WARNING_BOOL = "caller_id_over_ut_warning_bool";
    public static final String KEY_CALL_BARRING_OVER_UT_WARNING_BOOL = "call_barring_over_ut_warning_bool";
    public static final String KEY_CALL_BARRING_SUPPORTS_DEACTIVATE_ALL_BOOL = "call_barring_supports_deactivate_all_bool";
    public static final String KEY_CALL_BARRING_SUPPORTS_PASSWORD_CHANGE_BOOL = "call_barring_supports_password_change_bool";
    public static final String KEY_CALL_BARRING_VISIBILITY_BOOL = "call_barring_visibility_bool";
    public static final String KEY_CALL_FORWARDING_BLOCKS_WHILE_ROAMING_STRING_ARRAY = "call_forwarding_blocks_while_roaming_string_array";
    public static final String KEY_CALL_FORWARDING_MAP_NON_NUMBER_TO_VOICEMAIL_BOOL = "call_forwarding_map_non_number_to_voicemail_bool";
    public static final String KEY_CALL_FORWARDING_OVER_UT_WARNING_BOOL = "call_forwarding_over_ut_warning_bool";
    public static final String KEY_CALL_FORWARDING_VISIBILITY_BOOL = "call_forwarding_visibility_bool";
    public static final String KEY_CALL_REDIRECTION_SERVICE_COMPONENT_NAME_STRING = "call_redirection_service_component_name_string";
    public static final String KEY_CALL_WAITING_OVER_UT_WARNING_BOOL = "call_waiting_over_ut_warning_bool";
    public static final String KEY_CALL_WAITING_SERVICE_CLASS_INT = "call_waiting_service_class_int";
    public static final String KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL = "carrier_allow_deflect_ims_call_bool";
    public static final String KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL = "carrier_allow_turnoff_ims_bool";
    public static final String KEY_CARRIER_APP_NO_WAKE_SIGNAL_CONFIG_STRING_ARRAY = "carrier_app_no_wake_signal_config";
    public static final String KEY_CARRIER_APP_REQUIRED_DURING_SIM_SETUP_BOOL = "carrier_app_required_during_setup_bool";
    public static final String KEY_CARRIER_APP_WAKE_SIGNAL_CONFIG_STRING_ARRAY = "carrier_app_wake_signal_config";
    public static final String KEY_CARRIER_CALL_SCREENING_APP_STRING = "call_screening_app";
    public static final String KEY_CARRIER_CERTIFICATE_STRING_ARRAY = "carrier_certificate_string_array";
    public static final String KEY_CARRIER_CONFIG_APPLIED_BOOL = "carrier_config_applied_bool";
    public static final String KEY_CARRIER_CONFIG_VERSION_STRING = "carrier_config_version_string";
    public static final String KEY_CARRIER_DATA_CALL_APN_DELAY_DEFAULT_LONG = "carrier_data_call_apn_delay_default_long";
    public static final String KEY_CARRIER_DATA_CALL_APN_DELAY_FASTER_LONG = "carrier_data_call_apn_delay_faster_long";
    public static final String KEY_CARRIER_DATA_CALL_APN_RETRY_AFTER_DISCONNECT_LONG = "carrier_data_call_apn_retry_after_disconnect_long";
    public static final String KEY_CARRIER_DATA_CALL_PERMANENT_FAILURE_STRINGS = "carrier_data_call_permanent_failure_strings";
    public static final String KEY_CARRIER_DATA_CALL_RETRY_CONFIG_STRINGS = "carrier_data_call_retry_config_strings";
    public static final String KEY_CARRIER_DATA_SERVICE_WLAN_PACKAGE_OVERRIDE_STRING = "carrier_data_service_wlan_package_override_string";
    public static final String KEY_CARRIER_DATA_SERVICE_WWAN_PACKAGE_OVERRIDE_STRING = "carrier_data_service_wwan_package_override_string";
    public static final String KEY_CARRIER_DEFAULT_ACTIONS_ON_DCFAILURE_STRING_ARRAY = "carrier_default_actions_on_dcfailure_string_array";
    public static final String KEY_CARRIER_DEFAULT_ACTIONS_ON_DEFAULT_NETWORK_AVAILABLE = "carrier_default_actions_on_default_network_available_string_array";
    @UnsupportedAppUsage
    public static final String KEY_CARRIER_DEFAULT_ACTIONS_ON_REDIRECTION_STRING_ARRAY = "carrier_default_actions_on_redirection_string_array";
    public static final String KEY_CARRIER_DEFAULT_ACTIONS_ON_RESET = "carrier_default_actions_on_reset_string_array";
    public static final String KEY_CARRIER_DEFAULT_DATA_ROAMING_ENABLED_BOOL = "carrier_default_data_roaming_enabled_bool";
    public static final String KEY_CARRIER_DEFAULT_REDIRECTION_URL_STRING_ARRAY = "carrier_default_redirection_url_string_array";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL = "carrier_default_wfc_ims_enabled_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT = "carrier_default_wfc_ims_mode_int";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL = "carrier_default_wfc_ims_roaming_enabled_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT = "carrier_default_wfc_ims_roaming_mode_int";
    public static final String KEY_CARRIER_ERI_FILE_NAME_STRING = "carrier_eri_file_name_string";
    public static final String KEY_CARRIER_FORBID_USSD_WHEN_IMS_CALLING = "carrier_forbid_ussd_when_ims_calling_bool";
    public static final String KEY_CARRIER_FORCE_DISABLE_ETWS_CMAS_TEST_BOOL = "carrier_force_disable_etws_cmas_test_bool";
    public static final String KEY_CARRIER_IMS_GBA_REQUIRED_BOOL = "carrier_ims_gba_required_bool";
    public static final String KEY_CARRIER_INSTANT_LETTERING_AVAILABLE_BOOL = "carrier_instant_lettering_available_bool";
    public static final String KEY_CARRIER_INSTANT_LETTERING_ENCODING_STRING = "carrier_instant_lettering_encoding_string";
    public static final String KEY_CARRIER_INSTANT_LETTERING_ESCAPED_CHARS_STRING = "carrier_instant_lettering_escaped_chars_string";
    public static final String KEY_CARRIER_INSTANT_LETTERING_INVALID_CHARS_STRING = "carrier_instant_lettering_invalid_chars_string";
    public static final String KEY_CARRIER_INSTANT_LETTERING_LENGTH_LIMIT_INT = "carrier_instant_lettering_length_limit_int";
    public static final String KEY_CARRIER_METERED_APN_TYPES_STRINGS = "carrier_metered_apn_types_strings";
    public static final String KEY_CARRIER_METERED_ROAMING_APN_TYPES_STRINGS = "carrier_metered_roaming_apn_types_strings";
    public static final String KEY_CARRIER_NAME_OVERRIDE_BOOL = "carrier_name_override_bool";
    public static final String KEY_CARRIER_NAME_STRING = "carrier_name_string";
    public static final String KEY_CARRIER_NETWORK_SERVICE_WLAN_PACKAGE_OVERRIDE_STRING = "carrier_network_service_wlan_package_override_string";
    public static final String KEY_CARRIER_NETWORK_SERVICE_WWAN_PACKAGE_OVERRIDE_STRING = "carrier_network_service_wwan_package_override_string";
    public static final String KEY_CARRIER_PROMOTE_WFC_ON_CALL_FAIL_BOOL = "carrier_promote_wfc_on_call_fail_bool";
    public static final String KEY_CARRIER_QUALIFIED_NETWORKS_SERVICE_PACKAGE_OVERRIDE_STRING = "carrier_qualified_networks_service_package_override_string";
    public static final String KEY_CARRIER_SETTINGS_ACTIVITY_COMPONENT_NAME_STRING = "carrier_settings_activity_component_name_string";
    public static final String KEY_CARRIER_SETTINGS_ENABLE_BOOL = "carrier_settings_enable_bool";
    @SystemApi
    public static final String KEY_CARRIER_SETUP_APP_STRING = "carrier_setup_app_string";
    public static final String KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL = "carrier_supports_ss_over_ut_bool";
    public static final String KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL = "carrier_use_ims_first_for_emergency_bool";
    public static final String KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL = "carrier_ut_provisioning_required_bool";
    public static final String KEY_CARRIER_VOLTE_AVAILABLE_BOOL = "carrier_volte_available_bool";
    public static final String KEY_CARRIER_VOLTE_OVERRIDE_WFC_PROVISIONING_BOOL = "carrier_volte_override_wfc_provisioning_bool";
    public static final String KEY_CARRIER_VOLTE_PROVISIONED_BOOL = "carrier_volte_provisioned_bool";
    public static final String KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL = "carrier_volte_provisioning_required_bool";
    public static final String KEY_CARRIER_VOLTE_SHOW_SWITCH_BOOL = "carrier_volte_show_switch_bool";
    public static final String KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL = "carrier_volte_tty_supported_bool";
    public static final String KEY_CARRIER_VT_AVAILABLE_BOOL = "carrier_vt_available_bool";
    @Deprecated
    public static final String KEY_CARRIER_VVM_PACKAGE_NAME_STRING = "carrier_vvm_package_name_string";
    public static final String KEY_CARRIER_VVM_PACKAGE_NAME_STRING_ARRAY = "carrier_vvm_package_name_string_array";
    public static final String KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL = "carrier_wfc_ims_available_bool";
    public static final String KEY_CARRIER_WFC_SUPPORTS_CELLULAR_PREFERRED_BOOL = "carrier_wfc_supports_cellular_preferred_bool";
    public static final String KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL = "carrier_wfc_supports_wifi_only_bool";
    public static final String KEY_CARRIER_WIFI_STRING_ARRAY = "carrier_wifi_string_array";
    public static final String KEY_CDMA_3WAYCALL_FLASH_DELAY_INT = "cdma_3waycall_flash_delay_int";
    public static final String KEY_CDMA_DTMF_TONE_DELAY_INT = "cdma_dtmf_tone_delay_int";
    public static final String KEY_CDMA_ENHANCED_ROAMING_INDICATOR_FOR_HOME_NETWORK_INT_ARRAY = "cdma_enhanced_roaming_indicator_for_home_network_int_array";
    public static final String KEY_CDMA_HOME_REGISTERED_PLMN_NAME_OVERRIDE_BOOL = "cdma_home_registered_plmn_name_override_bool";
    public static final String KEY_CDMA_HOME_REGISTERED_PLMN_NAME_STRING = "cdma_home_registered_plmn_name_string";
    public static final String KEY_CDMA_NONROAMING_NETWORKS_STRING_ARRAY = "cdma_nonroaming_networks_string_array";
    public static final String KEY_CDMA_ROAMING_MODE_INT = "cdma_roaming_mode_int";
    public static final String KEY_CDMA_ROAMING_NETWORKS_STRING_ARRAY = "cdma_roaming_networks_string_array";
    public static final String KEY_CHECK_PRICING_WITH_CARRIER_FOR_DATA_ROAMING_BOOL = "check_pricing_with_carrier_data_roaming_bool";
    public static final String KEY_CI_ACTION_ON_SYS_UPDATE_BOOL = "ci_action_on_sys_update_bool";
    public static final String KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_STRING = "ci_action_on_sys_update_extra_string";
    public static final String KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_VAL_STRING = "ci_action_on_sys_update_extra_val_string";
    public static final String KEY_CI_ACTION_ON_SYS_UPDATE_INTENT_STRING = "ci_action_on_sys_update_intent_string";
    public static final String KEY_CONFIG_IMS_PACKAGE_OVERRIDE_STRING = "config_ims_package_override_string";
    public static final String KEY_CONFIG_PLANS_PACKAGE_OVERRIDE_STRING = "config_plans_package_override_string";
    public static final String KEY_CONFIG_SHOW_ORIG_DIAL_STRING_FOR_CDMA_BOOL = "config_show_orig_dial_string_for_cdma";
    public static final String KEY_CONFIG_TELEPHONY_USE_OWN_NUMBER_FOR_VOICEMAIL_BOOL = "config_telephony_use_own_number_for_voicemail_bool";
    public static final String KEY_CONFIG_WIFI_DISABLE_IN_ECBM = "config_wifi_disable_in_ecbm";
    public static final String KEY_CONVERT_CDMA_CALLER_ID_MMI_CODES_WHILE_ROAMING_ON_3GPP_BOOL = "convert_cdma_caller_id_mmi_codes_while_roaming_on_3gpp_bool";
    public static final String KEY_CSP_ENABLED_BOOL = "csp_enabled_bool";
    public static final String KEY_DATA_LIMIT_NOTIFICATION_BOOL = "data_limit_notification_bool";
    public static final String KEY_DATA_LIMIT_THRESHOLD_BYTES_LONG = "data_limit_threshold_bytes_long";
    public static final String KEY_DATA_RAPID_NOTIFICATION_BOOL = "data_rapid_notification_bool";
    public static final String KEY_DATA_WARNING_NOTIFICATION_BOOL = "data_warning_notification_bool";
    public static final String KEY_DATA_WARNING_THRESHOLD_BYTES_LONG = "data_warning_threshold_bytes_long";
    public static final String KEY_DEFAULT_SIM_CALL_MANAGER_STRING = "default_sim_call_manager_string";
    public static final String KEY_DEFAULT_VM_NUMBER_ROAMING_STRING = "default_vm_number_roaming_string";
    public static final String KEY_DEFAULT_VM_NUMBER_STRING = "default_vm_number_string";
    public static final String KEY_DIAL_STRING_REPLACE_STRING_ARRAY = "dial_string_replace_string_array";
    public static final String KEY_DISABLE_CDMA_ACTIVATION_CODE_BOOL = "disable_cdma_activation_code_bool";
    public static final String KEY_DISABLE_CHARGE_INDICATION_BOOL = "disable_charge_indication_bool";
    public static final String KEY_DISABLE_SEVERE_WHEN_EXTREME_DISABLED_BOOL = "disable_severe_when_extreme_disabled_bool";
    @UnsupportedAppUsage
    public static final String KEY_DISABLE_VOICE_BARRING_NOTIFICATION_BOOL = "disable_voice_barring_notification_bool";
    public static final String KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL = "display_hd_audio_property_bool";
    public static final String KEY_DISPLAY_VOICEMAIL_NUMBER_AS_DEFAULT_CALL_FORWARDING_NUMBER_BOOL = "display_voicemail_number_as_default_call_forwarding_number";
    public static final String KEY_DROP_VIDEO_CALL_WHEN_ANSWERING_AUDIO_CALL_BOOL = "drop_video_call_when_answering_audio_call_bool";
    public static final String KEY_DTMF_TYPE_ENABLED_BOOL = "dtmf_type_enabled_bool";
    public static final String KEY_DURATION_BLOCKING_DISABLED_AFTER_EMERGENCY_INT = "duration_blocking_disabled_after_emergency_int";
    public static final String KEY_EDITABLE_ENHANCED_4G_LTE_BOOL = "editable_enhanced_4g_lte_bool";
    public static final String KEY_EDITABLE_VOICEMAIL_NUMBER_BOOL = "editable_voicemail_number_bool";
    public static final String KEY_EDITABLE_VOICEMAIL_NUMBER_SETTING_BOOL = "editable_voicemail_number_setting_bool";
    public static final String KEY_EDITABLE_WFC_MODE_BOOL = "editable_wfc_mode_bool";
    public static final String KEY_EDITABLE_WFC_ROAMING_MODE_BOOL = "editable_wfc_roaming_mode_bool";
    public static final String KEY_EHPLMN_OVERRIDE_STRING_ARRAY = "ehplmn_override_string_array";
    public static final String KEY_EMERGENCY_NOTIFICATION_DELAY_INT = "emergency_notification_delay_int";
    public static final String KEY_EMERGENCY_NUMBER_PREFIX_STRING_ARRAY = "emergency_number_prefix_string_array";
    public static final String KEY_EMERGENCY_SMS_MODE_TIMER_MS_INT = "emergency_sms_mode_timer_ms_int";
    public static final String KEY_ENABLE_APPS_STRING_ARRAY = "enable_apps_string_array";
    public static final String KEY_ENABLE_CARRIER_DISPLAY_NAME_RESOLVER_BOOL = "enable_carrier_display_name_resolver_bool";
    public static final String KEY_ENABLE_DIALER_KEY_VIBRATION_BOOL = "enable_dialer_key_vibration_bool";
    public static final String KEY_ENABLE_WAP_PUSH_SI_BOOL = "enableWapPushSI";
    public static final String KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL = "enhanced_4g_lte_on_by_default_bool";
    @Deprecated
    public static final String KEY_ENHANCED_4G_LTE_TITLE_VARIANT_BOOL = "enhanced_4g_lte_title_variant_bool";
    public static final String KEY_ENHANCED_4G_LTE_TITLE_VARIANT_INT = "enhanced_4g_lte_title_variant_int";
    public static final String KEY_FEATURE_ACCESS_CODES_STRING_ARRAY = "feature_access_codes_string_array";
    public static final String KEY_FILTERED_CNAP_NAMES_STRING_ARRAY = "filtered_cnap_names_string_array";
    public static final String KEY_FORCE_HOME_NETWORK_BOOL = "force_home_network_bool";
    public static final String KEY_FORCE_IMEI_BOOL = "force_imei_bool";
    public static final String KEY_GROUP_CHAT_DEFAULT_TO_MMS_BOOL = "groupChatDefaultsToMMS";
    public static final String KEY_GSM_CDMA_CALLS_CAN_BE_HD_AUDIO = "gsm_cdma_calls_can_be_hd_audio";
    public static final String KEY_GSM_DTMF_TONE_DELAY_INT = "gsm_dtmf_tone_delay_int";
    public static final String KEY_GSM_NONROAMING_NETWORKS_STRING_ARRAY = "gsm_nonroaming_networks_string_array";
    public static final String KEY_GSM_ROAMING_NETWORKS_STRING_ARRAY = "gsm_roaming_networks_string_array";
    public static final String KEY_GSM_RSSI_THRESHOLDS_INT_ARRAY = "gsm_rssi_thresholds_int_array";
    public static final String KEY_HAS_IN_CALL_NOISE_SUPPRESSION_BOOL = "has_in_call_noise_suppression_bool";
    public static final String KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL = "hide_carrier_network_settings_bool";
    public static final String KEY_HIDE_ENHANCED_4G_LTE_BOOL = "hide_enhanced_4g_lte_bool";
    public static final String KEY_HIDE_IMS_APN_BOOL = "hide_ims_apn_bool";
    public static final String KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL = "hide_lte_plus_data_icon_bool";
    public static final String KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL = "hide_preferred_network_type_bool";
    public static final String KEY_HIDE_PRESET_APN_DETAILS_BOOL = "hide_preset_apn_details_bool";
    public static final String KEY_HIDE_SIM_LOCK_SETTINGS_BOOL = "hide_sim_lock_settings_bool";
    public static final String KEY_IDENTIFY_HIGH_DEFINITION_CALLS_IN_CALL_LOG_BOOL = "identify_high_definition_calls_in_call_log_bool";
    public static final String KEY_IGNORE_DATA_ENABLED_CHANGED_FOR_VIDEO_CALLS = "ignore_data_enabled_changed_for_video_calls";
    public static final String KEY_IGNORE_SIM_NETWORK_LOCKED_EVENTS_BOOL = "ignore_sim_network_locked_events_bool";
    public static final String KEY_IMS_CONFERENCE_SIZE_LIMIT_INT = "ims_conference_size_limit_int";
    public static final String KEY_IMS_DTMF_TONE_DELAY_INT = "ims_dtmf_tone_delay_int";
    public static final String KEY_IMS_REASONINFO_MAPPING_STRING_ARRAY = "ims_reasoninfo_mapping_string_array";
    public static final String KEY_IS_IMS_CONFERENCE_SIZE_ENFORCED_BOOL = "is_ims_conference_size_enforced_bool";
    public static final String KEY_IS_OPPORTUNISTIC_SUBSCRIPTION_BOOL = "is_opportunistic_subscription_bool";
    public static final String KEY_LTE_EARFCNS_RSRP_BOOST_INT = "lte_earfcns_rsrp_boost_int";
    public static final String KEY_LTE_ENABLED_BOOL = "lte_enabled_bool";
    public static final String KEY_LTE_RSRP_THRESHOLDS_INT_ARRAY = "lte_rsrp_thresholds_int_array";
    public static final String KEY_MDN_IS_ADDITIONAL_VOICEMAIL_NUMBER_BOOL = "mdn_is_additional_voicemail_number_bool";
    public static final String KEY_MESSAGE_EXPIRATION_TIME_LONG = "message_expiration_time_long";
    public static final String KEY_MMS_ALIAS_ENABLED_BOOL = "aliasEnabled";
    public static final String KEY_MMS_ALIAS_MAX_CHARS_INT = "aliasMaxChars";
    public static final String KEY_MMS_ALIAS_MIN_CHARS_INT = "aliasMinChars";
    public static final String KEY_MMS_ALLOW_ATTACH_AUDIO_BOOL = "allowAttachAudio";
    public static final String KEY_MMS_APPEND_TRANSACTION_ID_BOOL = "enabledTransID";
    public static final String KEY_MMS_CLOSE_CONNECTION_BOOL = "mmsCloseConnection";
    public static final String KEY_MMS_EMAIL_GATEWAY_NUMBER_STRING = "emailGatewayNumber";
    public static final String KEY_MMS_GROUP_MMS_ENABLED_BOOL = "enableGroupMms";
    public static final String KEY_MMS_HTTP_PARAMS_STRING = "httpParams";
    public static final String KEY_MMS_HTTP_SOCKET_TIMEOUT_INT = "httpSocketTimeout";
    public static final String KEY_MMS_MAX_IMAGE_HEIGHT_INT = "maxImageHeight";
    public static final String KEY_MMS_MAX_IMAGE_WIDTH_INT = "maxImageWidth";
    public static final String KEY_MMS_MAX_MESSAGE_SIZE_INT = "maxMessageSize";
    public static final String KEY_MMS_MESSAGE_TEXT_MAX_SIZE_INT = "maxMessageTextSize";
    public static final String KEY_MMS_MMS_DELIVERY_REPORT_ENABLED_BOOL = "enableMMSDeliveryReports";
    public static final String KEY_MMS_MMS_ENABLED_BOOL = "enabledMMS";
    public static final String KEY_MMS_MMS_READ_REPORT_ENABLED_BOOL = "enableMMSReadReports";
    public static final String KEY_MMS_MULTIPART_SMS_ENABLED_BOOL = "enableMultipartSMS";
    public static final String KEY_MMS_NAI_SUFFIX_STRING = "naiSuffix";
    public static final String KEY_MMS_NOTIFY_WAP_MMSC_ENABLED_BOOL = "enabledNotifyWapMMSC";
    public static final String KEY_MMS_RECIPIENT_LIMIT_INT = "recipientLimit";
    public static final String KEY_MMS_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES_BOOL = "sendMultipartSmsAsSeparateMessages";
    public static final String KEY_MMS_SHOW_CELL_BROADCAST_APP_LINKS_BOOL = "config_cellBroadcastAppLinks";
    public static final String KEY_MMS_SMS_DELIVERY_REPORT_ENABLED_BOOL = "enableSMSDeliveryReports";
    public static final String KEY_MMS_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD_INT = "smsToMmsTextLengthThreshold";
    public static final String KEY_MMS_SMS_TO_MMS_TEXT_THRESHOLD_INT = "smsToMmsTextThreshold";
    public static final String KEY_MMS_SUBJECT_MAX_LENGTH_INT = "maxSubjectLength";
    public static final String KEY_MMS_SUPPORT_HTTP_CHARSET_HEADER_BOOL = "supportHttpCharsetHeader";
    public static final String KEY_MMS_SUPPORT_MMS_CONTENT_DISPOSITION_BOOL = "supportMmsContentDisposition";
    public static final String KEY_MMS_UA_PROF_TAG_NAME_STRING = "uaProfTagName";
    public static final String KEY_MMS_UA_PROF_URL_STRING = "uaProfUrl";
    public static final String KEY_MMS_USER_AGENT_STRING = "userAgent";
    public static final String KEY_MONTHLY_DATA_CYCLE_DAY_INT = "monthly_data_cycle_day_int";
    public static final String KEY_NON_ROAMING_OPERATOR_STRING_ARRAY = "non_roaming_operator_string_array";
    public static final String KEY_NOTIFY_HANDOVER_VIDEO_FROM_LTE_TO_WIFI_BOOL = "notify_handover_video_from_lte_to_wifi_bool";
    public static final String KEY_NOTIFY_HANDOVER_VIDEO_FROM_WIFI_TO_LTE_BOOL = "notify_handover_video_from_wifi_to_lte_bool";
    public static final String KEY_NOTIFY_INTERNATIONAL_CALL_ON_WFC_BOOL = "notify_international_call_on_wfc_bool";
    public static final String KEY_NOTIFY_VT_HANDOVER_TO_WIFI_FAILURE_BOOL = "notify_vt_handover_to_wifi_failure_bool";
    public static final String KEY_ONLY_AUTO_SELECT_IN_HOME_NETWORK_BOOL = "only_auto_select_in_home_network";
    public static final String KEY_ONLY_SINGLE_DC_ALLOWED_INT_ARRAY = "only_single_dc_allowed_int_array";
    public static final String KEY_OPERATOR_NAME_FILTER_PATTERN_STRING = "operator_name_filter_pattern_string";
    public static final String KEY_OPERATOR_SELECTION_EXPAND_BOOL = "operator_selection_expand_bool";
    public static final String KEY_OPL_OVERRIDE_STRING_ARRAY = "opl_override_opl_string_array";
    public static final String KEY_OPPORTUNISTIC_NETWORK_DATA_SWITCH_HYSTERESIS_TIME_LONG = "opportunistic_network_data_switch_hysteresis_time_long";
    public static final String KEY_OPPORTUNISTIC_NETWORK_ENTRY_OR_EXIT_HYSTERESIS_TIME_LONG = "opportunistic_network_entry_or_exit_hysteresis_time_long";
    public static final String KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_BANDWIDTH_INT = "opportunistic_network_entry_threshold_bandwidth_int";
    public static final String KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_RSRP_INT = "opportunistic_network_entry_threshold_rsrp_int";
    public static final String KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_RSSNR_INT = "opportunistic_network_entry_threshold_rssnr_int";
    public static final String KEY_OPPORTUNISTIC_NETWORK_EXIT_THRESHOLD_RSRP_INT = "opportunistic_network_exit_threshold_rsrp_int";
    public static final String KEY_OPPORTUNISTIC_NETWORK_EXIT_THRESHOLD_RSSNR_INT = "opportunistic_network_exit_threshold_rssnr_int";
    public static final String KEY_PLAY_CALL_RECORDING_TONE_BOOL = "play_call_recording_tone_bool";
    public static final String KEY_PNN_OVERRIDE_STRING_ARRAY = "pnn_override_string_array";
    public static final String KEY_PREFER_2G_BOOL = "prefer_2g_bool";
    public static final String KEY_PREF_NETWORK_NOTIFICATION_DELAY_INT = "network_notification_delay_int";
    public static final String KEY_PROTOCOL_ERRORS_PERM_FAILURE = "protocol_errors_perm_failure";
    public static final String KEY_RADIO_RESTART_FAILURE_CAUSES_INT_ARRAY = "radio_restart_failure_causes_int_array";
    public static final String KEY_RATCHET_RAT_FAMILIES = "ratchet_rat_families";
    public static final String KEY_RCS_CONFIG_SERVER_URL_STRING = "rcs_config_server_url_string";
    public static final String KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY = "read_only_apn_fields_string_array";
    public static final String KEY_READ_ONLY_APN_TYPES_STRING_ARRAY = "read_only_apn_types_string_array";
    public static final String KEY_REJECT_GGSN_PERM_FAILURE = "reject_ggsn_perm_failure";
    public static final String KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL = "require_entitlement_checks_bool";
    @Deprecated
    public static final String KEY_RESTART_RADIO_ON_PDP_FAIL_REGULAR_DEACTIVATION_BOOL = "restart_radio_on_pdp_fail_regular_deactivation_bool";
    public static final String KEY_ROAMING_OPERATOR_STRING_ARRAY = "roaming_operator_string_array";
    public static final String KEY_RTT_AUTO_UPGRADE_BOOL = "rtt_auto_upgrade_bool";
    public static final String KEY_RTT_DOWNGRADE_SUPPORTED_BOOL = "rtt_downgrade_supported_bool";
    public static final String KEY_RTT_SUPPORTED_BOOL = "rtt_supported_bool";
    public static final String KEY_RTT_SUPPORTED_FOR_VT_BOOL = "rtt_supported_for_vt_bool";
    public static final String KEY_RTT_UPGRADE_SUPPORTED_BOOL = "rtt_upgrade_supported_bool";
    public static final String KEY_SHOW_4G_FOR_LTE_DATA_ICON_BOOL = "show_4g_for_lte_data_icon_bool";
    public static final String KEY_SHOW_APN_SETTING_CDMA_BOOL = "show_apn_setting_cdma_bool";
    public static final String KEY_SHOW_BLOCKING_PAY_PHONE_OPTION_BOOL = "show_blocking_pay_phone_option_bool";
    public static final String KEY_SHOW_CALL_BLOCKING_DISABLED_NOTIFICATION_ALWAYS_BOOL = "show_call_blocking_disabled_notification_always_bool";
    public static final String KEY_SHOW_CARRIER_DATA_ICON_PATTERN_STRING = "show_carrier_data_icon_pattern_string";
    public static final String KEY_SHOW_CDMA_CHOICES_BOOL = "show_cdma_choices_bool";
    public static final String KEY_SHOW_ICCID_IN_SIM_STATUS_BOOL = "show_iccid_in_sim_status_bool";
    public static final String KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL = "show_ims_registration_status_bool";
    public static final String KEY_SHOW_ONSCREEN_DIAL_BUTTON_BOOL = "show_onscreen_dial_button_bool";
    public static final String KEY_SHOW_PRECISE_FAILED_CAUSE_BOOL = "show_precise_failed_cause_bool";
    public static final String KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL = "show_signal_strength_in_sim_status_bool";
    public static final String KEY_SHOW_VIDEO_CALL_CHARGES_ALERT_DIALOG_BOOL = "show_video_call_charges_alert_dialog_bool";
    public static final String KEY_SHOW_WFC_LOCATION_PRIVACY_POLICY_BOOL = "show_wfc_location_privacy_policy_bool";
    public static final String KEY_SIMPLIFIED_NETWORK_SETTINGS_BOOL = "simplified_network_settings_bool";
    public static final String KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING = "sim_country_iso_override_string";
    public static final String KEY_SIM_NETWORK_UNLOCK_ALLOW_DISMISS_BOOL = "sim_network_unlock_allow_dismiss_bool";
    public static final String KEY_SKIP_CF_FAIL_TO_DISABLE_DIALOG_BOOL = "skip_cf_fail_to_disable_dialog_bool";
    public static final String KEY_SMART_FORWARDING_CONFIG_COMPONENT_NAME_STRING = "smart_forwarding_config_component_name_string";
    public static final String KEY_SMS_DELIVERY_REPORT_SETTING_BOOL = "smsDeliveryReportSettingOnByDefault";
    public static final String KEY_SMS_REQUIRES_DESTINATION_NUMBER_CONVERSION_BOOL = "sms_requires_destination_number_conversion_bool";
    public static final String KEY_SMS_USES_SIMPLE_CHARACTERS_ONLY_BOOL = "smsUsesSimpleCharactersOnly";
    public static final String KEY_SPDI_OVERRIDE_STRING_ARRAY = "spdi_override_string_array";
    public static final String KEY_SPN_DISPLAY_CONDITION_OVERRIDE_INT = "spn_display_condition_override_int";
    public static final String KEY_SPN_DISPLAY_RULE_USE_ROAMING_FROM_SERVICE_STATE_BOOL = "spn_display_rule_use_roaming_from_service_state_bool";
    public static final String KEY_STK_DISABLE_LAUNCH_BROWSER_BOOL = "stk_disable_launch_browser_bool";
    public static final String KEY_SUBSCRIPTION_GROUP_UUID_STRING = "subscription_group_uuid_string";
    public static final String KEY_SUPPORT_3GPP_CALL_FORWARDING_WHILE_ROAMING_BOOL = "support_3gpp_call_forwarding_while_roaming_bool";
    public static final String KEY_SUPPORT_CLIR_NETWORK_DEFAULT_BOOL = "support_clir_network_default_bool";
    public static final String KEY_SUPPORT_CNAP_BOOL = "suppport_cnap_bool";
    public static final String KEY_SUPPORT_CONFERENCE_CALL_BOOL = "support_conference_call_bool";
    public static final String KEY_SUPPORT_DIRECT_FDN_DIALING_BOOL = "support_direct_fdn_dialing_bool";
    public static final String KEY_SUPPORT_DOWNGRADE_VT_TO_AUDIO_BOOL = "support_downgrade_vt_to_audio_bool";
    public static final String KEY_SUPPORT_EMERGENCY_CALL_AND_HANGUP_OTHER_CALL = "suppport_emergency_call_and_hangup_other_call_bool";
    public static final String KEY_SUPPORT_EMERGENCY_DIALER_SHORTCUT_BOOL = "support_emergency_dialer_shortcut_bool";
    public static final String KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL = "support_emergency_sms_over_ims_bool";
    public static final String KEY_SUPPORT_ENHANCED_CALL_BLOCKING_BOOL = "support_enhanced_call_blocking_bool";
    public static final String KEY_SUPPORT_IMS_CONFERENCE_CALL_BOOL = "support_ims_conference_call_bool";
    public static final String KEY_SUPPORT_IMS_CONFERENCE_EVENT_PACKAGE_BOOL = "support_ims_conference_event_package_bool";
    public static final String KEY_SUPPORT_MANAGE_IMS_CONFERENCE_CALL_BOOL = "support_manage_ims_conference_call_bool";
    public static final String KEY_SUPPORT_NO_REPLY_TIMER_FOR_CFNRY_BOOL = "support_no_reply_timer_for_cfnry_bool";
    public static final String KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL = "support_pause_ims_video_calls_bool";
    public static final String KEY_SUPPORT_SWAP_AFTER_MERGE_BOOL = "support_swap_after_merge_bool";
    public static final String KEY_SUPPORT_TDSCDMA_BOOL = "support_tdscdma_bool";
    public static final String KEY_SUPPORT_TDSCDMA_ROAMING_NETWORKS_STRING_ARRAY = "support_tdscdma_roaming_networks_string_array";
    public static final String KEY_SUPPORT_USER_CHANGE_VIDEO_CAP_BOOL = "support_user_change_video_capability_bool";
    public static final String KEY_SUPPORT_VIDEO_CONFERENCE_CALL_BOOL = "support_video_conference_call_bool";
    public static final String KEY_SUPPORT_WPS_OVER_IMS_BOOL = "support_wps_over_ims_bool";
    public static final String KEY_TREAT_DOWNGRADED_VIDEO_CALLS_AS_VIDEO_CALLS_BOOL = "treat_downgraded_video_calls_as_video_calls_bool";
    public static final String KEY_TTY_SUPPORTED_BOOL = "tty_supported_bool";
    public static final String KEY_UNDELIVERED_SMS_MESSAGE_EXPIRATION_TIME = "undelivered_sms_message_expiration_time";
    public static final String KEY_UNLOGGABLE_NUMBERS_STRING_ARRAY = "unloggable_numbers_string_array";
    public static final String KEY_USE_CALLER_ID_USSD_BOOL = "use_caller_id_ussd_bool";
    public static final String KEY_USE_CALL_FORWARDING_USSD_BOOL = "use_call_forwarding_ussd_bool";
    public static final String KEY_USE_CUSTOM_USER_AGENT_BOOL = "useCustomUserAgent";
    public static final String KEY_USE_HFA_FOR_PROVISIONING_BOOL = "use_hfa_for_provisioning_bool";
    public static final String KEY_USE_ONLY_RSRP_FOR_LTE_SIGNAL_BAR_BOOL = "use_only_rsrp_for_lte_signal_bar_bool";
    public static final String KEY_USE_OTASP_FOR_PROVISIONING_BOOL = "use_otasp_for_provisioning_bool";
    public static final String KEY_USE_RCS_PRESENCE_BOOL = "use_rcs_presence_bool";
    public static final String KEY_USE_USIM_BOOL = "use_usim_bool";
    public static final String KEY_USE_WFC_HOME_NETWORK_MODE_IN_ROAMING_NETWORK_BOOL = "use_wfc_home_network_mode_in_roaming_network_bool";
    public static final String KEY_VIDEO_CALLS_CAN_BE_HD_AUDIO = "video_calls_can_be_hd_audio";
    public static final String KEY_VILTE_DATA_IS_METERED_BOOL = "vilte_data_is_metered_bool";
    public static final String KEY_VOICEMAIL_NOTIFICATION_PERSISTENT_BOOL = "voicemail_notification_persistent_bool";
    public static final String KEY_VOICE_PRIVACY_DISABLE_UI_BOOL = "voice_privacy_disable_ui_bool";
    public static final String KEY_VOLTE_REPLACEMENT_RAT_INT = "volte_replacement_rat_int";
    public static final String KEY_VVM_CELLULAR_DATA_REQUIRED_BOOL = "vvm_cellular_data_required_bool";
    public static final String KEY_VVM_CLIENT_PREFIX_STRING = "vvm_client_prefix_string";
    public static final String KEY_VVM_DESTINATION_NUMBER_STRING = "vvm_destination_number_string";
    public static final String KEY_VVM_DISABLED_CAPABILITIES_STRING_ARRAY = "vvm_disabled_capabilities_string_array";
    public static final String KEY_VVM_LEGACY_MODE_ENABLED_BOOL = "vvm_legacy_mode_enabled_bool";
    public static final String KEY_VVM_PORT_NUMBER_INT = "vvm_port_number_int";
    public static final String KEY_VVM_PREFETCH_BOOL = "vvm_prefetch_bool";
    public static final String KEY_VVM_SSL_ENABLED_BOOL = "vvm_ssl_enabled_bool";
    public static final String KEY_VVM_TYPE_STRING = "vvm_type_string";
    public static final String KEY_WCDMA_DEFAULT_SIGNAL_STRENGTH_MEASUREMENT_STRING = "wcdma_default_signal_strength_measurement_string";
    public static final String KEY_WCDMA_RSCP_THRESHOLDS_INT_ARRAY = "wcdma_rscp_thresholds_int_array";
    public static final String KEY_WFC_DATA_SPN_FORMAT_IDX_INT = "wfc_data_spn_format_idx_int";
    public static final String KEY_WFC_EMERGENCY_ADDRESS_CARRIER_APP_STRING = "wfc_emergency_address_carrier_app_string";
    public static final String KEY_WFC_FLIGHT_MODE_SPN_FORMAT_IDX_INT = "wfc_flight_mode_spn_format_idx_int";
    public static final String KEY_WFC_OPERATOR_ERROR_CODES_STRING_ARRAY = "wfc_operator_error_codes_string_array";
    public static final String KEY_WFC_SPN_FORMAT_IDX_INT = "wfc_spn_format_idx_int";
    public static final String KEY_WFC_SPN_USE_ROOT_LOCALE = "wfc_spn_use_root_locale";
    public static final String KEY_WIFI_CALLS_CAN_BE_HD_AUDIO = "wifi_calls_can_be_hd_audio";
    public static final String KEY_WORLD_MODE_ENABLED_BOOL = "world_mode_enabled_bool";
    public static final String KEY_WORLD_PHONE_BOOL = "world_phone_bool";
    public static final String REMOVE_GROUP_UUID_STRING = "00000000-0000-0000-0000-000000000000";
    private static final String TAG = "CarrierConfigManager";
    private static final PersistableBundle sDefaults = new PersistableBundle();
    private final Context mContext;

    public CarrierConfigManager(Context context) {
        this.mContext = context;
    }

    static {
        sDefaults.putString(KEY_CARRIER_CONFIG_VERSION_STRING, "");
        sDefaults.putBoolean(KEY_ALLOW_HOLD_IN_IMS_CALL_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL, false);
        sDefaults.putBoolean(KEY_ALWAYS_PLAY_REMOTE_HOLD_TONE_BOOL, false);
        sDefaults.putBoolean(KEY_AUTO_RETRY_FAILED_WIFI_EMERGENCY_CALL, false);
        sDefaults.putBoolean(KEY_ADDITIONAL_CALL_SETTING_BOOL, true);
        sDefaults.putBoolean(KEY_ALLOW_EMERGENCY_NUMBERS_IN_CALL_LOG_BOOL, false);
        sDefaults.putStringArray(KEY_UNLOGGABLE_NUMBERS_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_ALLOW_LOCAL_DTMF_TONES_BOOL, true);
        sDefaults.putBoolean(KEY_PLAY_CALL_RECORDING_TONE_BOOL, false);
        sDefaults.putBoolean(KEY_APN_EXPAND_BOOL, true);
        sDefaults.putBoolean(KEY_AUTO_RETRY_ENABLED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_SETTINGS_ENABLE_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_USER_CHANGE_VIDEO_CAP_BOOL, false);
        sDefaults.putBoolean(KEY_NOTIFY_HANDOVER_VIDEO_FROM_WIFI_TO_LTE_BOOL, false);
        sDefaults.putBoolean(KEY_NOTIFY_HANDOVER_VIDEO_FROM_LTE_TO_WIFI_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_DOWNGRADE_VT_TO_AUDIO_BOOL, true);
        sDefaults.putString(KEY_DEFAULT_VM_NUMBER_STRING, "");
        sDefaults.putString(KEY_DEFAULT_VM_NUMBER_ROAMING_STRING, "");
        sDefaults.putBoolean(KEY_CONFIG_TELEPHONY_USE_OWN_NUMBER_FOR_VOICEMAIL_BOOL, false);
        sDefaults.putBoolean(KEY_IGNORE_DATA_ENABLED_CHANGED_FOR_VIDEO_CALLS, true);
        sDefaults.putBoolean(KEY_VILTE_DATA_IS_METERED_BOOL, true);
        if (FEATURE_VOLTE_DYN) {
            sDefaults.putBoolean(KEY_CARRIER_VOLTE_AVAILABLE_BOOL, false);
            sDefaults.putBoolean(KEY_CARRIER_VT_AVAILABLE_BOOL, false);
        } else {
            sDefaults.putBoolean(KEY_CARRIER_VOLTE_AVAILABLE_BOOL, true);
            sDefaults.putBoolean(KEY_CARRIER_VT_AVAILABLE_BOOL, true);
        }
        sDefaults.putBoolean(KEY_CARRIER_VOLTE_SHOW_SWITCH_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_WFC_SUPPORTS_CELLULAR_PREFERRED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_PROMOTE_WFC_ON_CALL_FAIL_BOOL, false);
        sDefaults.putInt(KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT, 2);
        sDefaults.putBoolean(KEY_CARRIER_FORCE_DISABLE_ETWS_CMAS_TEST_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_VOLTE_OVERRIDE_WFC_PROVISIONING_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_IMS_GBA_REQUIRED_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_INSTANT_LETTERING_AVAILABLE_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL, true);
        sDefaults.putString(KEY_CARRIER_NETWORK_SERVICE_WWAN_PACKAGE_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_NETWORK_SERVICE_WLAN_PACKAGE_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_QUALIFIED_NETWORKS_SERVICE_PACKAGE_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_DATA_SERVICE_WWAN_PACKAGE_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_DATA_SERVICE_WLAN_PACKAGE_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_INSTANT_LETTERING_INVALID_CHARS_STRING, "");
        sDefaults.putString(KEY_CARRIER_INSTANT_LETTERING_ESCAPED_CHARS_STRING, "");
        sDefaults.putString(KEY_CARRIER_INSTANT_LETTERING_ENCODING_STRING, "");
        sDefaults.putInt(KEY_CARRIER_INSTANT_LETTERING_LENGTH_LIMIT_INT, 64);
        sDefaults.putBoolean(KEY_DISABLE_CDMA_ACTIVATION_CODE_BOOL, false);
        sDefaults.putBoolean(KEY_DTMF_TYPE_ENABLED_BOOL, false);
        sDefaults.putBoolean(KEY_ENABLE_DIALER_KEY_VIBRATION_BOOL, true);
        sDefaults.putBoolean(KEY_HAS_IN_CALL_NOISE_SUPPRESSION_BOOL, false);
        sDefaults.putBoolean(KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL, false);
        sDefaults.putBoolean(KEY_ONLY_AUTO_SELECT_IN_HOME_NETWORK_BOOL, false);
        sDefaults.putBoolean(KEY_SIMPLIFIED_NETWORK_SETTINGS_BOOL, false);
        sDefaults.putBoolean(KEY_HIDE_SIM_LOCK_SETTINGS_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_VOLTE_PROVISIONED_BOOL, false);
        sDefaults.putBoolean(KEY_CALL_BARRING_VISIBILITY_BOOL, false);
        sDefaults.putBoolean(KEY_CALL_BARRING_SUPPORTS_PASSWORD_CHANGE_BOOL, true);
        sDefaults.putBoolean(KEY_CALL_BARRING_SUPPORTS_DEACTIVATE_ALL_BOOL, true);
        sDefaults.putBoolean(KEY_CALL_FORWARDING_VISIBILITY_BOOL, true);
        sDefaults.putBoolean(KEY_ADDITIONAL_SETTINGS_CALLER_ID_VISIBILITY_BOOL, true);
        sDefaults.putBoolean(KEY_ADDITIONAL_SETTINGS_CALL_WAITING_VISIBILITY_BOOL, true);
        sDefaults.putBoolean(KEY_IGNORE_SIM_NETWORK_LOCKED_EVENTS_BOOL, false);
        sDefaults.putBoolean(KEY_MDN_IS_ADDITIONAL_VOICEMAIL_NUMBER_BOOL, false);
        sDefaults.putBoolean(KEY_OPERATOR_SELECTION_EXPAND_BOOL, true);
        sDefaults.putBoolean(KEY_PREFER_2G_BOOL, true);
        sDefaults.putBoolean(KEY_SHOW_APN_SETTING_CDMA_BOOL, true);
        sDefaults.putBoolean(KEY_SHOW_CDMA_CHOICES_BOOL, false);
        sDefaults.putBoolean(KEY_SMS_REQUIRES_DESTINATION_NUMBER_CONVERSION_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_ONSCREEN_DIAL_BUTTON_BOOL, true);
        sDefaults.putBoolean(KEY_SIM_NETWORK_UNLOCK_ALLOW_DISMISS_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_SWAP_AFTER_MERGE_BOOL, true);
        sDefaults.putBoolean(KEY_USE_HFA_FOR_PROVISIONING_BOOL, false);
        sDefaults.putBoolean(KEY_EDITABLE_VOICEMAIL_NUMBER_SETTING_BOOL, true);
        sDefaults.putBoolean(KEY_EDITABLE_VOICEMAIL_NUMBER_BOOL, false);
        sDefaults.putBoolean(KEY_USE_OTASP_FOR_PROVISIONING_BOOL, false);
        sDefaults.putBoolean(KEY_VOICEMAIL_NOTIFICATION_PERSISTENT_BOOL, false);
        sDefaults.putBoolean(KEY_VOICE_PRIVACY_DISABLE_UI_BOOL, false);
        sDefaults.putBoolean(KEY_WORLD_PHONE_BOOL, false);
        sDefaults.putBoolean(KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL, true);
        sDefaults.putBoolean(KEY_RESTART_RADIO_ON_PDP_FAIL_REGULAR_DEACTIVATION_BOOL, false);
        sDefaults.putIntArray(KEY_RADIO_RESTART_FAILURE_CAUSES_INT_ARRAY, new int[0]);
        sDefaults.putInt(KEY_VOLTE_REPLACEMENT_RAT_INT, 0);
        sDefaults.putString(KEY_DEFAULT_SIM_CALL_MANAGER_STRING, "");
        sDefaults.putString(KEY_VVM_DESTINATION_NUMBER_STRING, "");
        sDefaults.putInt(KEY_VVM_PORT_NUMBER_INT, 0);
        sDefaults.putString(KEY_VVM_TYPE_STRING, "");
        sDefaults.putBoolean(KEY_VVM_CELLULAR_DATA_REQUIRED_BOOL, false);
        sDefaults.putString(KEY_VVM_CLIENT_PREFIX_STRING, VisualVoicemailSmsFilterSettings.DEFAULT_CLIENT_PREFIX);
        sDefaults.putBoolean(KEY_VVM_SSL_ENABLED_BOOL, false);
        sDefaults.putStringArray(KEY_VVM_DISABLED_CAPABILITIES_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_VVM_LEGACY_MODE_ENABLED_BOOL, false);
        sDefaults.putBoolean(KEY_VVM_PREFETCH_BOOL, true);
        sDefaults.putString(KEY_CARRIER_VVM_PACKAGE_NAME_STRING, "");
        sDefaults.putStringArray(KEY_CARRIER_VVM_PACKAGE_NAME_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_SHOW_ICCID_IN_SIM_STATUS_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_SIGNAL_STRENGTH_IN_SIM_STATUS_BOOL, true);
        sDefaults.putBoolean(KEY_CI_ACTION_ON_SYS_UPDATE_BOOL, false);
        sDefaults.putString(KEY_CI_ACTION_ON_SYS_UPDATE_INTENT_STRING, "");
        sDefaults.putString(KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_STRING, "");
        sDefaults.putString(KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_VAL_STRING, "");
        if (IS_CSP_ENABLE) {
            sDefaults.putBoolean(KEY_CSP_ENABLED_BOOL, true);
        } else {
            sDefaults.putBoolean(KEY_CSP_ENABLED_BOOL, false);
        }
        sDefaults.putBoolean(KEY_ALLOW_ADDING_APNS_BOOL, true);
        sDefaults.putStringArray(KEY_READ_ONLY_APN_TYPES_STRING_ARRAY, new String[]{PhoneConstants.APN_TYPE_DUN});
        sDefaults.putStringArray(KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_BROADCAST_EMERGENCY_CALL_STATE_CHANGES_BOOL, false);
        sDefaults.putBoolean(KEY_ALWAYS_SHOW_EMERGENCY_ALERT_ONOFF_BOOL, false);
        sDefaults.putBoolean(KEY_DISABLE_SEVERE_WHEN_EXTREME_DISABLED_BOOL, true);
        sDefaults.putLong(KEY_MESSAGE_EXPIRATION_TIME_LONG, 86400000);
        sDefaults.putStringArray(KEY_CARRIER_DATA_CALL_RETRY_CONFIG_STRINGS, new String[]{"default:default_randomization=2000,5000,10000,20000,40000,80000:5000,160000:5000,320000:5000,640000:5000,1280000:5000,1800000:5000", "mms:default_randomization=2000,5000,10000,20000,40000,80000:5000,160000:5000,320000:5000,640000:5000,1280000:5000,1800000:5000", "others:max_retries=3, 5000, 5000, 5000"});
        sDefaults.putLong(KEY_CARRIER_DATA_CALL_APN_DELAY_DEFAULT_LONG, 5000);
        sDefaults.putLong(KEY_CARRIER_DATA_CALL_APN_DELAY_FASTER_LONG, 3000);
        sDefaults.putLong(KEY_CARRIER_DATA_CALL_APN_RETRY_AFTER_DISCONNECT_LONG, 5000);
        sDefaults.putString(KEY_CARRIER_ERI_FILE_NAME_STRING, "eri.xml");
        sDefaults.putInt(KEY_DURATION_BLOCKING_DISABLED_AFTER_EMERGENCY_INT, 7200);
        sDefaults.putStringArray(KEY_CARRIER_METERED_APN_TYPES_STRINGS, new String[]{PhoneConstants.APN_TYPE_DEFAULT, PhoneConstants.APN_TYPE_MMS, PhoneConstants.APN_TYPE_DUN, PhoneConstants.APN_TYPE_SUPL, AbstractPhoneConstants.APN_TYPE_INTERNALDEFAULT, AbstractPhoneConstants.APN_TYPE_SNSSAI});
        sDefaults.putStringArray(KEY_CARRIER_METERED_ROAMING_APN_TYPES_STRINGS, new String[]{PhoneConstants.APN_TYPE_DEFAULT, PhoneConstants.APN_TYPE_MMS, PhoneConstants.APN_TYPE_DUN, PhoneConstants.APN_TYPE_SUPL, AbstractPhoneConstants.APN_TYPE_INTERNALDEFAULT, AbstractPhoneConstants.APN_TYPE_SNSSAI});
        sDefaults.putIntArray(KEY_ONLY_SINGLE_DC_ALLOWED_INT_ARRAY, new int[]{4, 5, 6, 7, 8, 12});
        sDefaults.putStringArray(KEY_GSM_ROAMING_NETWORKS_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_GSM_NONROAMING_NETWORKS_STRING_ARRAY, null);
        sDefaults.putString(KEY_CONFIG_IMS_PACKAGE_OVERRIDE_STRING, null);
        sDefaults.putStringArray(KEY_CDMA_ROAMING_NETWORKS_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_CDMA_NONROAMING_NETWORKS_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_DIAL_STRING_REPLACE_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_FORCE_HOME_NETWORK_BOOL, false);
        sDefaults.putInt(KEY_GSM_DTMF_TONE_DELAY_INT, 0);
        sDefaults.putInt(KEY_IMS_DTMF_TONE_DELAY_INT, 0);
        sDefaults.putInt(KEY_CDMA_DTMF_TONE_DELAY_INT, 100);
        sDefaults.putBoolean(KEY_CALL_FORWARDING_MAP_NON_NUMBER_TO_VOICEMAIL_BOOL, false);
        sDefaults.putInt(KEY_CDMA_3WAYCALL_FLASH_DELAY_INT, 0);
        sDefaults.putBoolean(KEY_SUPPORT_CONFERENCE_CALL_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_IMS_CONFERENCE_CALL_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_MANAGE_IMS_CONFERENCE_CALL_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_IMS_CONFERENCE_EVENT_PACKAGE_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_VIDEO_CONFERENCE_CALL_BOOL, false);
        sDefaults.putBoolean(KEY_IS_IMS_CONFERENCE_SIZE_ENFORCED_BOOL, false);
        sDefaults.putInt(KEY_IMS_CONFERENCE_SIZE_LIMIT_INT, 5);
        sDefaults.putBoolean(KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL, true);
        sDefaults.putBoolean(KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, true);
        sDefaults.putBoolean(KEY_HIDE_ENHANCED_4G_LTE_BOOL, true);
        sDefaults.putBoolean(KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL, true);
        sDefaults.putBoolean(KEY_HIDE_IMS_APN_BOOL, false);
        sDefaults.putBoolean(KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL, false);
        sDefaults.putBoolean(KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL, false);
        sDefaults.putStringArray(KEY_ENABLE_APPS_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_EDITABLE_WFC_MODE_BOOL, true);
        sDefaults.putStringArray(KEY_WFC_OPERATOR_ERROR_CODES_STRING_ARRAY, null);
        sDefaults.putInt(KEY_WFC_SPN_FORMAT_IDX_INT, 0);
        sDefaults.putInt(KEY_WFC_DATA_SPN_FORMAT_IDX_INT, 0);
        sDefaults.putInt(KEY_WFC_FLIGHT_MODE_SPN_FORMAT_IDX_INT, -1);
        sDefaults.putBoolean(KEY_WFC_SPN_USE_ROOT_LOCALE, false);
        sDefaults.putString(KEY_WFC_EMERGENCY_ADDRESS_CARRIER_APP_STRING, "");
        sDefaults.putBoolean(KEY_CONFIG_WIFI_DISABLE_IN_ECBM, false);
        sDefaults.putBoolean(KEY_CARRIER_NAME_OVERRIDE_BOOL, false);
        sDefaults.putString(KEY_CARRIER_NAME_STRING, "");
        sDefaults.putInt(KEY_SPN_DISPLAY_CONDITION_OVERRIDE_INT, -1);
        sDefaults.putStringArray(KEY_SPDI_OVERRIDE_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_PNN_OVERRIDE_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_OPL_OVERRIDE_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_EHPLMN_OVERRIDE_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_ALLOW_ERI_BOOL, false);
        sDefaults.putBoolean(KEY_ENABLE_CARRIER_DISPLAY_NAME_RESOLVER_BOOL, false);
        sDefaults.putString(KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING, "");
        sDefaults.putString(KEY_CARRIER_CALL_SCREENING_APP_STRING, "");
        sDefaults.putString(KEY_CALL_REDIRECTION_SERVICE_COMPONENT_NAME_STRING, null);
        sDefaults.putBoolean(KEY_CDMA_HOME_REGISTERED_PLMN_NAME_OVERRIDE_BOOL, false);
        sDefaults.putString(KEY_CDMA_HOME_REGISTERED_PLMN_NAME_STRING, "");
        sDefaults.putBoolean(KEY_SUPPORT_DIRECT_FDN_DIALING_BOOL, false);
        sDefaults.putBoolean(KEY_CARRIER_DEFAULT_DATA_ROAMING_ENABLED_BOOL, false);
        sDefaults.putBoolean(KEY_SKIP_CF_FAIL_TO_DISABLE_DIALOG_BOOL, false);
        sDefaults.putBoolean(KEY_REJECT_GGSN_PERM_FAILURE, true);
        sDefaults.putBoolean(KEY_PROTOCOL_ERRORS_PERM_FAILURE, true);
        sDefaults.putBoolean(KEY_SUPPORT_EMERGENCY_CALL_AND_HANGUP_OTHER_CALL, false);
        sDefaults.putBoolean(KEY_SUPPORT_CNAP_BOOL, false);
        if (IS_DOCOMO) {
            sDefaults.putBoolean(KEY_SUPPORT_ENHANCED_CALL_BLOCKING_BOOL, true);
        } else {
            sDefaults.putBoolean(KEY_SUPPORT_ENHANCED_CALL_BLOCKING_BOOL, false);
        }
        sDefaults.putBoolean("aliasEnabled", false);
        sDefaults.putBoolean("allowAttachAudio", true);
        sDefaults.putBoolean("enabledTransID", false);
        sDefaults.putBoolean("enableGroupMms", true);
        sDefaults.putBoolean("enableMMSDeliveryReports", false);
        sDefaults.putBoolean("enabledMMS", true);
        sDefaults.putBoolean("enableMMSReadReports", false);
        sDefaults.putBoolean("enableMultipartSMS", true);
        sDefaults.putBoolean("enabledNotifyWapMMSC", false);
        sDefaults.putBoolean("sendMultipartSmsAsSeparateMessages", false);
        sDefaults.putBoolean("config_cellBroadcastAppLinks", true);
        sDefaults.putBoolean("enableSMSDeliveryReports", true);
        sDefaults.putBoolean("supportHttpCharsetHeader", false);
        sDefaults.putBoolean("supportMmsContentDisposition", true);
        sDefaults.putBoolean("mmsCloseConnection", false);
        sDefaults.putInt("aliasMaxChars", 48);
        sDefaults.putInt("aliasMinChars", 2);
        sDefaults.putInt("httpSocketTimeout", MediaPlayer.ProvisioningThread.TIMEOUT_MS);
        sDefaults.putInt("maxImageHeight", 480);
        sDefaults.putInt("maxImageWidth", 640);
        sDefaults.putInt("maxMessageSize", 307200);
        sDefaults.putInt("maxMessageTextSize", -1);
        sDefaults.putInt("recipientLimit", Integer.MAX_VALUE);
        sDefaults.putInt("smsToMmsTextLengthThreshold", -1);
        sDefaults.putInt("smsToMmsTextThreshold", -1);
        sDefaults.putInt("maxSubjectLength", 40);
        sDefaults.putString("emailGatewayNumber", "");
        sDefaults.putString("httpParams", "");
        sDefaults.putString("naiSuffix", "");
        sDefaults.putString("uaProfTagName", "x-wap-profile");
        sDefaults.putString("uaProfUrl", "");
        sDefaults.putString("userAgent", "");
        sDefaults.putBoolean(KEY_SMS_USES_SIMPLE_CHARACTERS_ONLY_BOOL, false);
        sDefaults.putBoolean(KEY_GROUP_CHAT_DEFAULT_TO_MMS_BOOL, true);
        sDefaults.putBoolean(KEY_USE_CUSTOM_USER_AGENT_BOOL, false);
        sDefaults.putBoolean(KEY_SMS_DELIVERY_REPORT_SETTING_BOOL, false);
        sDefaults.putBoolean(KEY_ENABLE_WAP_PUSH_SI_BOOL, true);
        sDefaults.putBoolean(KEY_ALLOW_ENABLING_WAP_PUSH_SI_BOOL, true);
        sDefaults.putBoolean(KEY_ALLOW_NON_EMERGENCY_CALLS_IN_ECM_BOOL, true);
        sDefaults.putInt(KEY_EMERGENCY_SMS_MODE_TIMER_MS_INT, 0);
        sDefaults.putBoolean(KEY_USE_RCS_PRESENCE_BOOL, false);
        sDefaults.putBoolean(KEY_FORCE_IMEI_BOOL, false);
        sDefaults.putInt(KEY_CDMA_ROAMING_MODE_INT, -1);
        sDefaults.putString(KEY_RCS_CONFIG_SERVER_URL_STRING, "");
        sDefaults.putString(KEY_CARRIER_SETUP_APP_STRING, "");
        sDefaults.putStringArray(KEY_CARRIER_APP_WAKE_SIGNAL_CONFIG_STRING_ARRAY, new String[]{"com.android.carrierdefaultapp/.CarrierDefaultBroadcastReceiver:com.android.internal.telephony.CARRIER_SIGNAL_RESET"});
        sDefaults.putStringArray(KEY_CARRIER_APP_NO_WAKE_SIGNAL_CONFIG_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_CARRIER_APP_REQUIRED_DURING_SIM_SETUP_BOOL, false);
        sDefaults.putStringArray(KEY_CARRIER_DEFAULT_ACTIONS_ON_REDIRECTION_STRING_ARRAY, new String[]{"9, 4, 1"});
        sDefaults.putStringArray(KEY_CARRIER_DEFAULT_ACTIONS_ON_RESET, new String[]{"6, 8"});
        PersistableBundle persistableBundle = sDefaults;
        persistableBundle.putStringArray(KEY_CARRIER_DEFAULT_ACTIONS_ON_DEFAULT_NETWORK_AVAILABLE, new String[]{String.valueOf(false) + ": 7", String.valueOf(true) + ": 8"});
        sDefaults.putStringArray(KEY_CARRIER_DEFAULT_REDIRECTION_URL_STRING_ARRAY, null);
        sDefaults.putInt(KEY_MONTHLY_DATA_CYCLE_DAY_INT, -1);
        sDefaults.putLong(KEY_DATA_WARNING_THRESHOLD_BYTES_LONG, -1);
        sDefaults.putBoolean(KEY_DATA_WARNING_NOTIFICATION_BOOL, true);
        sDefaults.putLong(KEY_DATA_LIMIT_THRESHOLD_BYTES_LONG, -1);
        sDefaults.putBoolean(KEY_DATA_LIMIT_NOTIFICATION_BOOL, true);
        sDefaults.putBoolean(KEY_DATA_RAPID_NOTIFICATION_BOOL, true);
        sDefaults.putStringArray(KEY_RATCHET_RAT_FAMILIES, new String[]{"1,2", "7,8,12", "3,11,9,10,15", "14,19"});
        sDefaults.putBoolean(KEY_TREAT_DOWNGRADED_VIDEO_CALLS_AS_VIDEO_CALLS_BOOL, false);
        sDefaults.putBoolean(KEY_DROP_VIDEO_CALL_WHEN_ANSWERING_AUDIO_CALL_BOOL, false);
        sDefaults.putBoolean(KEY_ALLOW_MERGE_WIFI_CALLS_WHEN_VOWIFI_OFF_BOOL, true);
        sDefaults.putBoolean(KEY_ALLOW_ADD_CALL_DURING_VIDEO_CALL_BOOL, true);
        sDefaults.putBoolean(KEY_WIFI_CALLS_CAN_BE_HD_AUDIO, true);
        sDefaults.putBoolean(KEY_VIDEO_CALLS_CAN_BE_HD_AUDIO, true);
        sDefaults.putBoolean(KEY_GSM_CDMA_CALLS_CAN_BE_HD_AUDIO, false);
        sDefaults.putBoolean(KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL, true);
        sDefaults.putStringArray(KEY_IMS_REASONINFO_MAPPING_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_ENHANCED_4G_LTE_TITLE_VARIANT_BOOL, false);
        sDefaults.putInt(KEY_ENHANCED_4G_LTE_TITLE_VARIANT_INT, 0);
        sDefaults.putBoolean(KEY_NOTIFY_VT_HANDOVER_TO_WIFI_FAILURE_BOOL, false);
        sDefaults.putStringArray(KEY_FILTERED_CNAP_NAMES_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_EDITABLE_WFC_ROAMING_MODE_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_BLOCKING_PAY_PHONE_OPTION_BOOL, false);
        sDefaults.putBoolean(KEY_USE_WFC_HOME_NETWORK_MODE_IN_ROAMING_NETWORK_BOOL, false);
        sDefaults.putBoolean(KEY_STK_DISABLE_LAUNCH_BROWSER_BOOL, false);
        sDefaults.putBoolean(KEY_ALLOW_METERED_NETWORK_FOR_CERT_DOWNLOAD_BOOL, false);
        sDefaults.putStringArray(KEY_CARRIER_WIFI_STRING_ARRAY, null);
        sDefaults.putInt(KEY_PREF_NETWORK_NOTIFICATION_DELAY_INT, -1);
        sDefaults.putInt(KEY_EMERGENCY_NOTIFICATION_DELAY_INT, -1);
        sDefaults.putBoolean(KEY_ALLOW_USSD_REQUESTS_VIA_TELEPHONY_MANAGER_BOOL, true);
        sDefaults.putBoolean(KEY_CARRIER_FORBID_USSD_WHEN_IMS_CALLING, false);
        sDefaults.putBoolean(KEY_SUPPORT_3GPP_CALL_FORWARDING_WHILE_ROAMING_BOOL, true);
        sDefaults.putBoolean(KEY_DISPLAY_VOICEMAIL_NUMBER_AS_DEFAULT_CALL_FORWARDING_NUMBER_BOOL, false);
        sDefaults.putBoolean(KEY_NOTIFY_INTERNATIONAL_CALL_ON_WFC_BOOL, false);
        sDefaults.putBoolean(KEY_HIDE_PRESET_APN_DETAILS_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_VIDEO_CALL_CHARGES_ALERT_DIALOG_BOOL, false);
        sDefaults.putStringArray(KEY_CALL_FORWARDING_BLOCKS_WHILE_ROAMING_STRING_ARRAY, null);
        sDefaults.putInt(KEY_LTE_EARFCNS_RSRP_BOOST_INT, 0);
        sDefaults.putStringArray(KEY_BOOSTED_LTE_EARFCNS_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_USE_ONLY_RSRP_FOR_LTE_SIGNAL_BAR_BOOL, false);
        sDefaults.putBoolean(KEY_DISABLE_VOICE_BARRING_NOTIFICATION_BOOL, false);
        sDefaults.putInt(IMSI_KEY_AVAILABILITY_INT, 0);
        sDefaults.putString(IMSI_KEY_DOWNLOAD_URL_STRING, null);
        sDefaults.putBoolean(KEY_CONVERT_CDMA_CALLER_ID_MMI_CODES_WHILE_ROAMING_ON_3GPP_BOOL, false);
        sDefaults.putStringArray(KEY_NON_ROAMING_OPERATOR_STRING_ARRAY, null);
        sDefaults.putStringArray(KEY_ROAMING_OPERATOR_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL, false);
        sDefaults.putBoolean(KEY_RTT_SUPPORTED_BOOL, false);
        sDefaults.putBoolean(KEY_TTY_SUPPORTED_BOOL, true);
        sDefaults.putBoolean(KEY_DISABLE_CHARGE_INDICATION_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_NO_REPLY_TIMER_FOR_CFNRY_BOOL, true);
        sDefaults.putStringArray(KEY_FEATURE_ACCESS_CODES_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_IDENTIFY_HIGH_DEFINITION_CALLS_IN_CALL_LOG_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_PRECISE_FAILED_CAUSE_BOOL, false);
        sDefaults.putBoolean(KEY_SPN_DISPLAY_RULE_USE_ROAMING_FROM_SERVICE_STATE_BOOL, false);
        sDefaults.putBoolean(KEY_ALWAYS_SHOW_DATA_RAT_ICON_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_4G_FOR_LTE_DATA_ICON_BOOL, false);
        sDefaults.putString(KEY_OPERATOR_NAME_FILTER_PATTERN_STRING, "");
        sDefaults.putString(KEY_SHOW_CARRIER_DATA_ICON_PATTERN_STRING, "");
        sDefaults.putBoolean(KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL, true);
        sDefaults.putBoolean(KEY_LTE_ENABLED_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_TDSCDMA_BOOL, false);
        sDefaults.putStringArray(KEY_SUPPORT_TDSCDMA_ROAMING_NETWORKS_STRING_ARRAY, null);
        sDefaults.putBoolean(KEY_WORLD_MODE_ENABLED_BOOL, false);
        sDefaults.putString(KEY_CARRIER_SETTINGS_ACTIVITY_COMPONENT_NAME_STRING, "");
        sDefaults.putBoolean(KEY_CARRIER_CONFIG_APPLIED_BOOL, false);
        sDefaults.putBoolean(KEY_CHECK_PRICING_WITH_CARRIER_FOR_DATA_ROAMING_BOOL, false);
        sDefaults.putIntArray(KEY_LTE_RSRP_THRESHOLDS_INT_ARRAY, new int[]{-128, PackageManager.INSTALL_FAILED_BAD_SIGNATURE, PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, -98});
        sDefaults.putIntArray(KEY_WCDMA_RSCP_THRESHOLDS_INT_ARRAY, new int[]{PackageManager.INSTALL_FAILED_ABORTED, PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, -95, -85});
        sDefaults.putString(KEY_WCDMA_DEFAULT_SIGNAL_STRENGTH_MEASUREMENT_STRING, CellSignalStrengthWcdma.LEVEL_CALCULATION_METHOD_RSSI);
        sDefaults.putBoolean(KEY_CONFIG_SHOW_ORIG_DIAL_STRING_FOR_CDMA_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_CALL_BLOCKING_DISABLED_NOTIFICATION_ALWAYS_BOOL, false);
        sDefaults.putBoolean(KEY_CALL_FORWARDING_OVER_UT_WARNING_BOOL, false);
        sDefaults.putBoolean(KEY_CALL_BARRING_OVER_UT_WARNING_BOOL, false);
        sDefaults.putBoolean(KEY_CALLER_ID_OVER_UT_WARNING_BOOL, false);
        sDefaults.putBoolean(KEY_CALL_WAITING_OVER_UT_WARNING_BOOL, false);
        sDefaults.putBoolean(KEY_SUPPORT_CLIR_NETWORK_DEFAULT_BOOL, true);
        sDefaults.putBoolean(KEY_SUPPORT_EMERGENCY_DIALER_SHORTCUT_BOOL, true);
        sDefaults.putBoolean(KEY_USE_CALL_FORWARDING_USSD_BOOL, false);
        sDefaults.putBoolean(KEY_USE_CALLER_ID_USSD_BOOL, false);
        sDefaults.putInt(KEY_CALL_WAITING_SERVICE_CLASS_INT, 1);
        sDefaults.putString(KEY_5G_ICON_CONFIGURATION_STRING, "connected_mmwave:None,connected:5G,not_restricted:None,restricted:None");
        sDefaults.putBoolean(KEY_ASCII_7_BIT_SUPPORT_FOR_LONG_MESSAGE_BOOL, false);
        sDefaults.putInt(KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_RSRP_INT, PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED);
        sDefaults.putInt(KEY_OPPORTUNISTIC_NETWORK_EXIT_THRESHOLD_RSRP_INT, PackageManager.INSTALL_FAILED_BAD_SIGNATURE);
        sDefaults.putInt(KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_RSSNR_INT, 45);
        sDefaults.putInt(KEY_OPPORTUNISTIC_NETWORK_EXIT_THRESHOLD_RSSNR_INT, 10);
        sDefaults.putInt(KEY_OPPORTUNISTIC_NETWORK_ENTRY_THRESHOLD_BANDWIDTH_INT, 1024);
        sDefaults.putLong(KEY_OPPORTUNISTIC_NETWORK_ENTRY_OR_EXIT_HYSTERESIS_TIME_LONG, JobInfo.MIN_BACKOFF_MILLIS);
        sDefaults.putLong(KEY_OPPORTUNISTIC_NETWORK_DATA_SWITCH_HYSTERESIS_TIME_LONG, JobInfo.MIN_BACKOFF_MILLIS);
        sDefaults.putAll(Gps.getDefaults());
        sDefaults.putAll(Wifi.getDefaults());
        sDefaults.putIntArray(KEY_CDMA_ENHANCED_ROAMING_INDICATOR_FOR_HOME_NETWORK_INT_ARRAY, new int[]{1});
        sDefaults.putStringArray(KEY_EMERGENCY_NUMBER_PREFIX_STRING_ARRAY, new String[0]);
        sDefaults.putBoolean(KEY_USE_USIM_BOOL, false);
        sDefaults.putBoolean(KEY_SHOW_WFC_LOCATION_PRIVACY_POLICY_BOOL, false);
        sDefaults.putBoolean(KEY_AUTO_CANCEL_CS_REJECT_NOTIFICATION, false);
        sDefaults.putString(KEY_SMART_FORWARDING_CONFIG_COMPONENT_NAME_STRING, "");
        sDefaults.putBoolean(KEY_ALWAYS_SHOW_PRIMARY_SIGNAL_BAR_IN_OPPORTUNISTIC_NETWORK_BOOLEAN, false);
        sDefaults.putString(KEY_SUBSCRIPTION_GROUP_UUID_STRING, "");
        sDefaults.putBoolean(KEY_IS_OPPORTUNISTIC_SUBSCRIPTION_BOOL, false);
        sDefaults.putIntArray(KEY_GSM_RSSI_THRESHOLDS_INT_ARRAY, new int[]{PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID, PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, -97, -89});
        sDefaults.putBoolean(KEY_SUPPORT_WPS_OVER_IMS_BOOL, true);
        sDefaults.putStringArray(KEY_CARRIER_CERTIFICATE_STRING_ARRAY, null);
    }

    public static final class Gps {
        public static final String KEY_A_GLONASS_POS_PROTOCOL_SELECT_STRING = "gps.a_glonass_pos_protocol_select";
        public static final String KEY_ES_EXTENSION_SEC_STRING = "gps.es_extension_sec";
        public static final String KEY_ES_SUPL_CONTROL_PLANE_SUPPORT_INT = "gps.es_supl_control_plane_support_int";
        public static final String KEY_GPS_LOCK_STRING = "gps.gps_lock";
        public static final String KEY_LPP_PROFILE_STRING = "gps.lpp_profile";
        public static final String KEY_NFW_PROXY_APPS_STRING = "gps.nfw_proxy_apps";
        public static final String KEY_PERSIST_LPP_MODE_BOOL = "gps.persist_lpp_mode_bool";
        public static final String KEY_PREFIX = "gps.";
        public static final String KEY_SUPL_ES_STRING = "gps.supl_es";
        public static final String KEY_SUPL_HOST_STRING = "gps.supl_host";
        public static final String KEY_SUPL_MODE_STRING = "gps.supl_mode";
        public static final String KEY_SUPL_PORT_STRING = "gps.supl_port";
        public static final String KEY_SUPL_VER_STRING = "gps.supl_ver";
        public static final String KEY_USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL_STRING = "gps.use_emergency_pdn_for_emergency_supl";
        public static final int SUPL_EMERGENCY_MODE_TYPE_CP_FALLBACK = 1;
        public static final int SUPL_EMERGENCY_MODE_TYPE_CP_ONLY = 0;
        public static final int SUPL_EMERGENCY_MODE_TYPE_DP_ONLY = 2;

        /* access modifiers changed from: private */
        public static PersistableBundle getDefaults() {
            PersistableBundle defaults = new PersistableBundle();
            defaults.putBoolean(KEY_PERSIST_LPP_MODE_BOOL, true);
            defaults.putString(KEY_SUPL_HOST_STRING, "supl.google.com");
            defaults.putString(KEY_SUPL_PORT_STRING, "7275");
            defaults.putString(KEY_SUPL_VER_STRING, "0x20000");
            defaults.putString(KEY_SUPL_MODE_STRING, "1");
            defaults.putString(KEY_SUPL_ES_STRING, "1");
            defaults.putString(KEY_LPP_PROFILE_STRING, WifiEnterpriseConfig.ENGINE_DISABLE);
            defaults.putString(KEY_USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL_STRING, "1");
            defaults.putString(KEY_A_GLONASS_POS_PROTOCOL_SELECT_STRING, WifiEnterpriseConfig.ENGINE_DISABLE);
            defaults.putString(KEY_GPS_LOCK_STRING, WifiScanLog.EVENT_KEY3);
            defaults.putString(KEY_ES_EXTENSION_SEC_STRING, WifiEnterpriseConfig.ENGINE_DISABLE);
            defaults.putString(KEY_NFW_PROXY_APPS_STRING, "");
            defaults.putInt(KEY_ES_SUPL_CONTROL_PLANE_SUPPORT_INT, 0);
            return defaults;
        }
    }

    public static final class Wifi {
        public static final String KEY_CARRIER_CONNECTION_MANAGER_PACKAGE_STRING = "wifi.carrier_connection_manager_package_string";
        public static final String KEY_CARRIER_PROFILES_VERSION_INT = "wifi.carrier_profiles_version_int";
        public static final String KEY_NETWORK_PROFILES_STRING_ARRAY = "wifi.network_profiles_string_array";
        public static final String KEY_PASSPOINT_PROFILES_STRING_ARRAY = "wifi.passpoint_profiles_string_array";
        public static final String KEY_PREFIX = "wifi.";

        /* access modifiers changed from: private */
        public static PersistableBundle getDefaults() {
            PersistableBundle defaults = new PersistableBundle();
            defaults.putInt(KEY_CARRIER_PROFILES_VERSION_INT, -1);
            defaults.putString(KEY_CARRIER_CONNECTION_MANAGER_PACKAGE_STRING, null);
            defaults.putStringArray(KEY_NETWORK_PROFILES_STRING_ARRAY, null);
            defaults.putStringArray(KEY_PASSPOINT_PROFILES_STRING_ARRAY, null);
            return defaults;
        }

        private Wifi() {
        }
    }

    public PersistableBundle getConfigForSubId(int subId) {
        try {
            ICarrierConfigLoader loader = getICarrierConfigLoader();
            if (loader != null) {
                return loader.getConfigForSubId(subId, this.mContext.getOpPackageName());
            }
            Rlog.w(TAG, "Error getting config for subId " + subId + " ICarrierConfigLoader is null");
            return null;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error getting config for subId " + subId + ": " + ex.toString());
            return null;
        }
    }

    @SystemApi
    public void overrideConfig(int subscriptionId, PersistableBundle overrideValues) {
        try {
            ICarrierConfigLoader loader = getICarrierConfigLoader();
            if (loader == null) {
                Rlog.w(TAG, "Error setting config for subId " + subscriptionId + " ICarrierConfigLoader is null");
                return;
            }
            loader.overrideConfig(subscriptionId, overrideValues);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error setting config for subId " + subscriptionId + ": " + ex.toString());
        }
    }

    public PersistableBundle getConfig() {
        return getConfigForSubId(SubscriptionManager.getDefaultSubscriptionId());
    }

    public static boolean isConfigForIdentifiedCarrier(PersistableBundle bundle) {
        return bundle != null && bundle.getBoolean(KEY_CARRIER_CONFIG_APPLIED_BOOL);
    }

    public void notifyConfigChangedForSubId(int subId) {
        try {
            ICarrierConfigLoader loader = getICarrierConfigLoader();
            if (loader == null) {
                Rlog.w(TAG, "Error reloading config for subId=" + subId + " ICarrierConfigLoader is null");
                return;
            }
            loader.notifyConfigChangedForSubId(subId);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error reloading config for subId=" + subId + ": " + ex.toString());
        }
    }

    @SystemApi
    public void updateConfigForPhoneId(int phoneId, String simState) {
        try {
            ICarrierConfigLoader loader = getICarrierConfigLoader();
            if (loader == null) {
                Rlog.w(TAG, "Error updating config for phoneId=" + phoneId + " ICarrierConfigLoader is null");
                return;
            }
            loader.updateConfigForPhoneId(phoneId, simState);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error updating config for phoneId=" + phoneId + ": " + ex.toString());
        }
    }

    public String getDefaultCarrierServicePackageName() {
        try {
            return getICarrierConfigLoader().getDefaultCarrierServicePackageName();
        } catch (Throwable th) {
            return null;
        }
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    public static PersistableBundle getDefaultConfig() {
        return new PersistableBundle(sDefaults);
    }

    private ICarrierConfigLoader getICarrierConfigLoader() {
        return ICarrierConfigLoader.Stub.asInterface(ServiceManager.getService(Context.CARRIER_CONFIG_SERVICE));
    }
}
