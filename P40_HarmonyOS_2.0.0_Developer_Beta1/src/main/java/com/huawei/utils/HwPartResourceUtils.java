package com.huawei.utils;

import com.huawei.internal.widget.ConstantValues;
import java.util.HashMap;
import java.util.Map;

public class HwPartResourceUtils {
    public static final int INVALID_ID = -1;
    private static Map<String, Integer> basicStringPlatformResourceIdMap = new HashMap();
    private static Map<String, Integer> mmiCodeStringResourceIdMap = new HashMap();
    private static Map<String, Integer> powerStringOfficeResourceIdMap = new HashMap();
    private static Map<String, Integer> telephonyStringResourceIdMap = new HashMap();

    static {
        telephonyStringResourceIdMap.put("network_rejinfo_notification_action", 33686086);
        telephonyStringResourceIdMap.put("ic_phone_fail_statusbar", 33752271);
        telephonyStringResourceIdMap.put("network_rejinfo_notification_app", 33686087);
        telephonyStringResourceIdMap.put("network_rejinfo_notify_title", 33686089);
        telephonyStringResourceIdMap.put("network_rejinfo_notify_content", 33686088);
        telephonyStringResourceIdMap.put("global_actions_toggle_airplane_mode", 17040222);
        telephonyStringResourceIdMap.put("lockscreen_carrier_default", 17040422);
        telephonyStringResourceIdMap.put("emergency_calls_only", 17040034);
        telephonyStringResourceIdMap.put("switch_notify_toast", 33686270);
        telephonyStringResourceIdMap.put("smart_card_4G_switch_toast", 33686252);
        telephonyStringResourceIdMap.put("smart_card_4G_5G_switch_back_toast", 33686251);
        telephonyStringResourceIdMap.put("smart_card_5G_bind_toast", 33686253);
        telephonyStringResourceIdMap.put("data_near_limit", 33685616);
        telephonyStringResourceIdMap.put("data_near_explain", 33685615);
        telephonyStringResourceIdMap.put("sim_card_manager", 33686240);
        telephonyStringResourceIdMap.put("data_remind", 33685617);
        telephonyStringResourceIdMap.put("data_remind_summary", 33685619);
        telephonyStringResourceIdMap.put("notification_typeC_nonotify", 33685985);
        telephonyStringResourceIdMap.put("magngesture_dialog_ignore", 33685756);
        telephonyStringResourceIdMap.put("back_to_main", 33685532);
        telephonyStringResourceIdMap.put("alert_dialog_ok", 33685725);
        telephonyStringResourceIdMap.put("network_limit_prompt", 33686083);
        telephonyStringResourceIdMap.put("network_limit_prompt_summary", 33686085);
        telephonyStringResourceIdMap.put("network_limit_prompt_content", 33686084);
        telephonyStringResourceIdMap.put("Theme_Emui_Dialog_Alert", 33947691);
        telephonyStringResourceIdMap.put("data_enable_confirm_title", 33685614);
        telephonyStringResourceIdMap.put("data_enable_confirm_msg", 33685612);
        telephonyStringResourceIdMap.put("data_enable_confirm_msg_vowifi", 33685613);
        telephonyStringResourceIdMap.put("cancel", 17039360);
        telephonyStringResourceIdMap.put("gsm_alphabet_default_charset", 17040240);
        telephonyStringResourceIdMap.put("uim_tip_ok", 17039370);
        telephonyStringResourceIdMap.put("sim_added_title", 17041237);
        telephonyStringResourceIdMap.put("sim_removed_title", 17041240);
        telephonyStringResourceIdMap.put("defaultVoiceMailAlphaTag", 17039364);
        telephonyStringResourceIdMap.put("data_saver_enable_button", 17039912);
        telephonyStringResourceIdMap.put("popupwindow_incall_networking_prompt_text1", 33686206);
        basicStringPlatformResourceIdMap.put("toast_gesture_retry", 33686279);
        basicStringPlatformResourceIdMap.put("toast_hiVision_not_available", 33686280);
        basicStringPlatformResourceIdMap.put("wireless_tx_error_full_device", 33686327);
        basicStringPlatformResourceIdMap.put("wireless_tx_error_no_device", 33686329);
        basicStringPlatformResourceIdMap.put("wireless_tx_error_temperature_low", 33686331);
        basicStringPlatformResourceIdMap.put("wireless_tx_error_temperature_high", 33686330);
        basicStringPlatformResourceIdMap.put("wireless_tx_error_low_battery", 33686328);
        basicStringPlatformResourceIdMap.put("battery_iscd_error_notification_title", 33685541);
        basicStringPlatformResourceIdMap.put("battery_iscd_error_notification_msg", 33685540);
        basicStringPlatformResourceIdMap.put("non_standard_charge_line_title", 33686094);
        basicStringPlatformResourceIdMap.put("non_standard_charge_line_message", 33686093);
        basicStringPlatformResourceIdMap.put("non_standard_charge_line_button_text", 33686092);
        basicStringPlatformResourceIdMap.put("non_standard_charging_line_profile_url", 33686095);
        basicStringPlatformResourceIdMap.put("battery_charge_notification_channel_name", 33685539);
        basicStringPlatformResourceIdMap.put("mistouch_prevention_quitnote2", 33686050);
        basicStringPlatformResourceIdMap.put("mistouch_prevention_quitnote", 33686044);
        basicStringPlatformResourceIdMap.put("split_app_three_finger_slide_message", 33686258);
        basicStringPlatformResourceIdMap.put("notify_content", 33686108);
        basicStringPlatformResourceIdMap.put("notify_title", 33686110);
        basicStringPlatformResourceIdMap.put("notify_know", 33686109);
        basicStringPlatformResourceIdMap.put("package_incompatible", 33685898);
        basicStringPlatformResourceIdMap.put("package_uninstall", 33685899);
        basicStringPlatformResourceIdMap.put("mdm_toast_prohibit_install_app", 33686038);
        basicStringPlatformResourceIdMap.put("toast_auto_keyboard_layout_default", 17041387);
        basicStringPlatformResourceIdMap.put("toast_auto_keyboard_layout_prefix", 17041388);
        powerStringOfficeResourceIdMap.put("notification_pc_desktop_mode", 33686103);
        powerStringOfficeResourceIdMap.put("notification_pc_phone_mode", 33686104);
        powerStringOfficeResourceIdMap.put("notification_pc_connected", 33686102);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_exclusive_keyboard2", 33685626);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_content", 33685624);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_settings", 33685628);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_title", 33685634);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_start", 33685633);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_cancel", 33685623);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_toast", 33685636);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_toast", 33685650);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_exclusive_keyboard", 33685625);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_exclusive_keyboard", 33685642);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_exclusive_keyboard2", 33685643);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_content", 33685641);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_title", 33685649);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_start", 33685645);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_cancel", 33685640);
        powerStringOfficeResourceIdMap.put("proj_mode_notification_label", 33685941);
        powerStringOfficeResourceIdMap.put("welink_device_name", 33686307);
        powerStringOfficeResourceIdMap.put("notification_btn_touchpad", 33686099);
        powerStringOfficeResourceIdMap.put("notification_btn_disconnect", 33686097);
        powerStringOfficeResourceIdMap.put("notification_btn_phone_mode", 33686098);
        powerStringOfficeResourceIdMap.put("notification_btn_desktop_mode", 33686096);
        powerStringOfficeResourceIdMap.put("desktop_mode_exit_incall", 33685644);
        powerStringOfficeResourceIdMap.put("desktop_mode_enter_incall", 33685627);
        powerStringOfficeResourceIdMap.put("pc_dp_link_error1", 33686114);
        powerStringOfficeResourceIdMap.put("pc_dp_link_error2", 33686115);
        powerStringOfficeResourceIdMap.put("restore_current_window", 33686222);
        powerStringOfficeResourceIdMap.put("window_cast_mode_reminder_secure_string", 33686326);
        powerStringOfficeResourceIdMap.put("window_cast_mode_reminder_locked", 33686325);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_disable_bluetooth_tip", 33685558);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_dialog_close_title", 33685554);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_enable_bluetooth_tip", 33685560);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_dialog_open_title", 33685555);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_disable", 33685556);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_cancel", 33685552);
        powerStringOfficeResourceIdMap.put("bluetooth_notify_enable", 33685559);
        powerStringOfficeResourceIdMap.put("pc_vassist_start_in_phone", 33686148);
        powerStringOfficeResourceIdMap.put("pc_vassist_start_in_screen", 33686149);
        powerStringOfficeResourceIdMap.put("pc_vassist_inline_search_market", 33686134);
        powerStringOfficeResourceIdMap.put("pc_vassist_cannot_do_it", 33686124);
        powerStringOfficeResourceIdMap.put("pc_vassist_close_phone_app_for_start", 33686129);
        powerStringOfficeResourceIdMap.put("pc_vassist_close_external_app_for_start", 33686127);
        powerStringOfficeResourceIdMap.put("pc_vassist_start_succ", 33686151);
        powerStringOfficeResourceIdMap.put("pc_vassist_start_ok", 33686150);
        powerStringOfficeResourceIdMap.put("pc_vassist_close_succ", 33686130);
        powerStringOfficeResourceIdMap.put("pc_vassist_close_ok", 33686128);
        powerStringOfficeResourceIdMap.put("pc_vassist_ok", 33686140);
        powerStringOfficeResourceIdMap.put("pc_vassist_no_problem", 33686139);
        powerStringOfficeResourceIdMap.put("pc_vassist_conn_external_display_tip", 33686131);
        powerStringOfficeResourceIdMap.put("pc_vassist_disconn_external_display_tip", 33686132);
        powerStringOfficeResourceIdMap.put("pc_vassist_prev_page", 33686144);
        powerStringOfficeResourceIdMap.put("pc_vassist_prev_page1", 33686145);
        powerStringOfficeResourceIdMap.put("pc_vassist_prev_page2", 33686146);
        powerStringOfficeResourceIdMap.put("pc_vassist_prev_page3", 33686147);
        powerStringOfficeResourceIdMap.put("pc_vassist_next_page", 33686135);
        powerStringOfficeResourceIdMap.put("pc_vassist_next_page1", 33686136);
        powerStringOfficeResourceIdMap.put("pc_vassist_next_page2", 33686137);
        powerStringOfficeResourceIdMap.put("pc_vassist_next_page3", 33686138);
        powerStringOfficeResourceIdMap.put("pc_vassist_play_ppt", 33686142);
        powerStringOfficeResourceIdMap.put("pc_vassist_play_ppt1", 33686143);
        powerStringOfficeResourceIdMap.put("pc_vassist_app_not_found", 33686122);
        powerStringOfficeResourceIdMap.put("pc_vassist_app_not_operation", 33686123);
        powerStringOfficeResourceIdMap.put("pc_vassist_doc_not_found", 33686133);
        powerStringOfficeResourceIdMap.put("pc_vassist_cannot_larger", 33686126);
        powerStringOfficeResourceIdMap.put("pc_vassist_ok_done", 33686141);
        powerStringOfficeResourceIdMap.put("pc_recommend_dialog_open", 33686120);
        powerStringOfficeResourceIdMap.put("pc_recommend_dialog_ignore", 33686118);
        powerStringOfficeResourceIdMap.put("pc_recommend_dialog_learn", 33686119);
        powerStringOfficeResourceIdMap.put("pc_recommend_dialog_desc_new", 33686117);
        powerStringOfficeResourceIdMap.put("popupwindow_networking_prompt_001", 33686207);
        powerStringOfficeResourceIdMap.put("pc_notification_click_more_action", 33686116);
        powerStringOfficeResourceIdMap.put("notification_wireless_projection", 33686107);
        mmiCodeStringResourceIdMap.put("PinMmi", 17039467);
        mmiCodeStringResourceIdMap.put("badPin", 17039700);
        mmiCodeStringResourceIdMap.put("mismatchPin", 17040610);
        mmiCodeStringResourceIdMap.put("mmiError", 17040620);
        mmiCodeStringResourceIdMap.put("BaMmi", 17039397);
        mmiCodeStringResourceIdMap.put("ClipMmi", 17039408);
        mmiCodeStringResourceIdMap.put("ClirMmi", 17039409);
        mmiCodeStringResourceIdMap.put("PwdMmi", 17039468);
        mmiCodeStringResourceIdMap.put("ColpMmi", 17039413);
        mmiCodeStringResourceIdMap.put("CwMmi", 17039415);
        mmiCodeStringResourceIdMap.put("ColrMmi", 17039414);
        mmiCodeStringResourceIdMap.put("serviceRegistered", 17041207);
        mmiCodeStringResourceIdMap.put("serviceErased", 17041205);
        mmiCodeStringResourceIdMap.put("passwordIncorrect", 17040720);
        mmiCodeStringResourceIdMap.put("serviceNotProvisioned", 17041206);
        mmiCodeStringResourceIdMap.put("CLIRPermanent", 17039402);
        mmiCodeStringResourceIdMap.put("CLIRDefaultOnNextCallOn", 17039401);
        mmiCodeStringResourceIdMap.put("CLIRDefaultOnNextCallOff", 17039400);
        mmiCodeStringResourceIdMap.put("CLIRDefaultOffNextCallOff", 17039398);
        mmiCodeStringResourceIdMap.put("CLIRDefaultOffNextCallOn", 17039399);
        mmiCodeStringResourceIdMap.put("CfMmi", 17039403);
        mmiCodeStringResourceIdMap.put("CfuMmi", 17039407);
        mmiCodeStringResourceIdMap.put("CfbMmi", 17039406);
        mmiCodeStringResourceIdMap.put("CfNryMmi", 17039405);
        mmiCodeStringResourceIdMap.put("CfNrcMmi", 17039404);
        mmiCodeStringResourceIdMap.put("serviceEnabled", 17041203);
        mmiCodeStringResourceIdMap.put("serviceDisabled", 17041202);
    }

    private HwPartResourceUtils() {
    }

    public static int getResourceId(String key) {
        if (key == null) {
            return -1;
        }
        int value = getResourceIdForBasicPlatform(key);
        if (value != -1) {
            return value;
        }
        int value2 = getResourceIdForFingerprint(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getResourceIdForTelephony(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getResourceIdForSecurity(key);
        if (value4 != -1) {
            return value4;
        }
        int value5 = getResourceIdForPowerOffice(key);
        if (value5 != -1) {
            return value5;
        }
        int value6 = getResourceIdForMmiCode(key);
        if (value6 != -1) {
            return value6;
        }
        int value7 = getResourceIdForAutoCamera(key);
        if (value7 != -1) {
            return value7;
        }
        int value8 = getResourceIdForSingleHand(key);
        if (value8 != -1) {
            return value8;
        }
        int value9 = getResourceIdForMedia(key);
        if (value9 != -1) {
            return value9;
        }
        int value10 = getResourceIdForPadEdu(key);
        if (value10 != -1) {
            return value10;
        }
        return value10;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForSingleHand(String key) {
        char c;
        switch (key.hashCode()) {
            case -1966463794:
                if (key.equals("freshman_guide_info_gesture")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1750940022:
                if (key.equals("freshman_guide_title_new_info")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1675550909:
                if (key.equals("hint_section_top")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1596518051:
                if (key.equals("freshman_layout")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1585659707:
                if (key.equals("freshman_guide_button_info")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1228513791:
                if (key.equals("freshman_guide_title_gesture_info")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1195307691:
                if (key.equals("tips_info")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1145813236:
                if (key.equals("freshman_guide_title_gesture")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1012135550:
                if (key.equals("singlehand_click_settings")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -675057099:
                if (key.equals("overlay_display_window_title")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -451150424:
                if (key.equals("ic_settings")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case -402799488:
                if (key.equals("hint_section_info")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -402479873:
                if (key.equals("hint_section_text")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -200327473:
                if (key.equals("freshman_layout_text")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -124040299:
                if (key.equals("freshman_guide_image_id")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 11141532:
                if (key.equals("tips_title_info")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 258162772:
                if (key.equals("freshman_layout_button")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 431125036:
                if (key.equals("freshman_guide_info_gesture_id")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 540156038:
                if (key.equals("gesture_guide")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 831810324:
                if (key.equals("navigator_guide")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 913832869:
                if (key.equals("freshman_guide_info_new")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1168010290:
                if (key.equals("freshman_guide_title_triple_info")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1527008891:
                if (key.equals("tips_info_detail")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1603372442:
                if (key.equals("hint_setting_container")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 2096983757:
                if (key.equals("confirm_info")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 33685755;
            case 1:
                return 33685752;
            case 2:
                return 33685747;
            case 3:
                return 33685748;
            case 4:
                return 33685746;
            case 5:
                return 33686277;
            case 6:
                return 33686276;
            case 7:
                return 33685753;
            case '\b':
                return 34603263;
            case '\t':
                return 34603261;
            case '\n':
                return 34603264;
            case 11:
                return 34603262;
            case '\f':
                return 34603164;
            case '\r':
                return 34603541;
            case 14:
                return 34603246;
            case 15:
                return 34603248;
            case 16:
                return 34603245;
            case 17:
                return 34603244;
            case 18:
                return 34603243;
            case 19:
                return 34603247;
            case 20:
                return 34603049;
            case 21:
                return 33752011;
            case 22:
                return 33752300;
            case 23:
                return 33752426;
            case 24:
                return 33752328;
            default:
                return -1;
        }
    }

    private static int getStringResourceIdForTelephony(String key) {
        return telephonyStringResourceIdMap.getOrDefault(key, -1).intValue();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getBooleanResourceIdForTelephony(String key) {
        boolean z;
        switch (key.hashCode()) {
            case -1994759852:
                if (key.equals("config_device_vt_available")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1642130809:
                if (key.equals("skip_restoring_network_selection")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -952305534:
                if (key.equals("config_device_volte_available")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1484367234:
                if (key.equals("config_device_wfc_ims_available")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 17891406;
        }
        if (z) {
            return 17891405;
        }
        if (z) {
            return 17891404;
        }
        if (!z) {
            return -1;
        }
        return 17891621;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getArrayResourceIdForTelephony(String key) {
        boolean z;
        switch (key.hashCode()) {
            case -770819464:
                if (key.equals("config_sms_enabled_single_shift_tables")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 1747727825:
                if (key.equals("systemui_vowifi_operator_name")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1937387263:
                if (key.equals("wfcSpnFormats")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 2099983851:
                if (key.equals("config_sms_enabled_locking_shift_tables")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 17236062;
        }
        if (z) {
            return 17236061;
        }
        if (z) {
            return 17236117;
        }
        if (!z) {
            return -1;
        }
        return 17041358;
    }

    private static int getDrawableResourceIdForTelephony(String key) {
        if ((key.hashCode() == -202305772 && key.equals("ic_sim_statusbar")) ? false : true) {
            return -1;
        }
        return 33752301;
    }

    private static int getResourceIdForTelephony(String key) {
        int value = getStringResourceIdForTelephony(key);
        if (value != -1) {
            return value;
        }
        int value2 = getBooleanResourceIdForTelephony(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getArrayResourceIdForTelephony(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getDrawableResourceIdForTelephony(key);
        if (value4 != -1) {
            return value4;
        }
        return -1;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getDimenResourceIdForBasicPlatform(String key) {
        char c;
        switch (key.hashCode()) {
            case -1971601134:
                if (key.equals("slide_out_circle_logo_side_margin")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1743417713:
                if (key.equals("slide_out_max_distance")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1631871138:
                if (key.equals("slide_out_circle_diameter_max_size")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1540336536:
                if (key.equals("gesture_nav_back_max_distance_1")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1540336535:
                if (key.equals("gesture_nav_back_max_distance_2")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1364921414:
                if (key.equals("status_bar_height_portrait")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -841625987:
                if (key.equals("gesture_nav_back_window_width")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -611229376:
                if (key.equals("status_bar_height")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -208721198:
                if (key.equals("fold_disable_touch_padding_top")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 278039962:
                if (key.equals("navigation_bar_height_landscape")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 310368618:
                if (key.equals("slide_out_circle_travel_distance")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 630374991:
                if (key.equals("slide_out_start_threshold")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 630789324:
                if (key.equals("slide_out_circle_base_margin")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 694738398:
                if (key.equals("navigation_bar_height")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 886899504:
                if (key.equals("slide_out_circle_diameter_min_size")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 919226644:
                if (key.equals("gesture_nav_bottom_window_height")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 928728477:
                if (key.equals("slide_out_circle_center_start_pos")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 935313020:
                if (key.equals("status_bar_height_landscape")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1372049228:
                if (key.equals("gesture_nav_bottom_quick_out_height")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1382503648:
                if (key.equals("gesture_nav_bottom_side_width")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1629467435:
                if (key.equals("gesture_nav_curved_offset")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1837493807:
                if (key.equals("navigation_bar_width")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 17105445;
            case 1:
                return 17105447;
            case 2:
                return 17105446;
            case 3:
                return 17105314;
            case 4:
                return 17105309;
            case 5:
                return 17105311;
            case 6:
                return 17105190;
            case 7:
                return 17105189;
            case '\b':
                return 34472895;
            case '\t':
                return 34472894;
            case '\n':
                return 34472600;
            case 11:
                return 34472601;
            case '\f':
                return 34472602;
            case '\r':
                return 34472598;
            case 14:
                return 34472599;
            case 15:
                return 34472891;
            case 16:
                return 34472890;
            case 17:
                return 34472889;
            case 18:
                return 34472888;
            case 19:
                return 34472887;
            case 20:
                return 34472893;
            case 21:
                return 34472590;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getDrawableResourceIdForBasicPlatform(String key) {
        char c;
        switch (key.hashCode()) {
            case -1694973564:
                if (key.equals("default_wallpaper")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1480134508:
                if (key.equals("battery_iscd_error_notification_icon")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1298360886:
                if (key.equals("ic_hivoice_oversea_app")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1033716838:
                if (key.equals("ic_dock_app")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -250338241:
                if (key.equals("mask_circle_oversea")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 532094270:
                if (key.equals("image_laptop")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 572382990:
                if (key.equals("mask_circle1")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 572382991:
                if (key.equals("mask_circle2")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 955038389:
                if (key.equals("wireless_tx_status_error")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1189503080:
                if (key.equals("ic_hivoice_oversea")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1284359852:
                if (key.equals("ic_hivoice")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1393393343:
                if (key.equals("stat_sys_warning")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1661231814:
                if (key.equals("ic_battery_non_standard_charge_line")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2119368151:
                if (key.equals("gesture_nav_back_anim")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 17302140;
            case 1:
                return 33752548;
            case 2:
                return 33751351;
            case 3:
                return 33752177;
            case 4:
                return 33752306;
            case 5:
                return 33752012;
            case 6:
                return 33752211;
            case 7:
                return 33752325;
            case '\b':
                return 33752326;
            case '\t':
                return 33752327;
            case '\n':
                return 33752242;
            case 11:
                return 33752243;
            case '\f':
                return 33752244;
            case '\r':
                return 17301642;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getIntgerResourceIdForBasicPlatform(String key) {
        boolean z;
        switch (key.hashCode()) {
            case -1780162851:
                if (key.equals("config_notificationsBatteryFullARGB")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1772244925:
                if (key.equals("config_notificationsBatteryMediumARGB")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -974638712:
                if (key.equals("config_notificationsBatteryLedOn")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -149029146:
                if (key.equals("config_notificationsBatteryLedOff")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -21535010:
                if (key.equals("config_notificationsBatteryLowARGB")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 17694866;
        }
        if (z) {
            return 17694867;
        }
        if (z) {
            return 17694863;
        }
        if (z) {
            return 17694865;
        }
        if (!z) {
            return -1;
        }
        return 17694864;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002f A[RETURN] */
    private static int getStyleResourceIdForBasicPlatform(String key) {
        boolean z;
        int hashCode = key.hashCode();
        if (hashCode != -1200735292) {
            if (hashCode == 1135162361 && key.equals("Animation_RecentApplications")) {
                z = false;
                if (!z) {
                    return 16974601;
                }
                if (!z) {
                    return -1;
                }
                return 16974858;
            }
        } else if (key.equals("Theme_Holo_Dialog_Alert")) {
            z = true;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    private static int getColorResourceIdForBasicPlatform(String key) {
        if ((key.hashCode() == 1731386482 && key.equals("system_notification_accent_color")) ? false : true) {
            return -1;
        }
        return 17170460;
    }

    private static int getStringResourceIdForBasicPlatform(String key) {
        return basicStringPlatformResourceIdMap.getOrDefault(key, -1).intValue();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045 A[RETURN] */
    private static int getLayoutResourceIdForBasicPlatform(String key) {
        boolean z;
        int hashCode = key.hashCode();
        if (hashCode != -717454760) {
            if (hashCode != -311912715) {
                if (hashCode == 1091708282 && key.equals("gesture_slide_out_view")) {
                    z = false;
                    if (z) {
                        return 34013351;
                    }
                    if (z) {
                        return 34013427;
                    }
                    if (!z) {
                        return -1;
                    }
                    return 34013422;
                }
            } else if (key.equals("notify_dismiss_softinput")) {
                z = true;
                if (z) {
                }
            }
        } else if (key.equals("screen_on_proximity_view_land")) {
            z = true;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getIdResourceIdForBasicPlatform(String key) {
        char c;
        switch (key.hashCode()) {
            case -2050330750:
                if (key.equals("left_circle_logo")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1410993327:
                if (key.equals("close_layout")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1344547834:
                if (key.equals("notify_description")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -205566778:
                if (key.equals("disable_touch_notchview")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 257729274:
                if (key.equals("mis_touch_hint_layout")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1077819710:
                if (key.equals("disable_touch_hint")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1116659355:
                if (key.equals("anim_text")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1143740951:
                if (key.equals("right_circle_logo")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1179105159:
                if (key.equals("slide_out_scrim")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1739990575:
                if (key.equals("slide_out_circle")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1770187795:
                if (key.equals("mis_touch_bottom")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1803045093:
                if (key.equals("notify_image")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1856982751:
                if (key.equals("screen_on_proximity_view_for_tp")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 2114275289:
                if (key.equals("slide_out_circle_legacy")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34603414;
            case 1:
                return 34603482;
            case 2:
                return 34603507;
            case 3:
                return 34603504;
            case 4:
                return 34603505;
            case 5:
                return 34603062;
            case 6:
                return 34603061;
            case 7:
                return 34603433;
            case '\b':
                return 34603432;
            case '\t':
                return 34603045;
            case '\n':
                return 34603022;
            case 11:
                return 34603444;
            case '\f':
                return 34603442;
            case '\r':
                return 34013452;
            default:
                return -1;
        }
    }

    private static int getInterpolatorResourceIdForBasicPlatform(String key) {
        if ((key.hashCode() == -1647969273 && key.equals("cubic_bezier_interpolator_type_33_33")) ? false : true) {
            return -1;
        }
        return 34078724;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002f A[RETURN] */
    private static int getBooleanResourceIdForBasicPlatform(String key) {
        boolean z;
        int hashCode = key.hashCode();
        if (hashCode != -1995417619) {
            if (hashCode == -1180046299 && key.equals("config_handleVolumeKeysInWindowManager")) {
                z = true;
                if (!z) {
                    return 17891481;
                }
                if (!z) {
                    return -1;
                }
                return 17891465;
            }
        } else if (key.equals("config_maskMainBuiltInDisplayCutout")) {
            z = false;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    private static int getResourceIdForBasicPlatform(String key) {
        int value = getDimenResourceIdForBasicPlatform(key);
        if (value != -1) {
            return value;
        }
        int value2 = getDrawableResourceIdForBasicPlatform(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getIntgerResourceIdForBasicPlatform(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getStyleResourceIdForBasicPlatform(key);
        if (value4 != -1) {
            return value4;
        }
        int value5 = getColorResourceIdForBasicPlatform(key);
        if (value5 != -1) {
            return value5;
        }
        int value6 = getStringResourceIdForBasicPlatform(key);
        if (value6 != -1) {
            return value6;
        }
        int value7 = getLayoutResourceIdForBasicPlatform(key);
        if (value7 != -1) {
            return value7;
        }
        int value8 = getIdResourceIdForBasicPlatform(key);
        if (value8 != -1) {
            return value8;
        }
        int value9 = getInterpolatorResourceIdForBasicPlatform(key);
        if (value9 != -1) {
            return value9;
        }
        int value10 = getBooleanResourceIdForBasicPlatform(key);
        if (value10 != -1) {
            return value10;
        }
        return -1;
    }

    private static int getResourceIdForFingerprint(String key) {
        int value = getResourceIdForFingerprintCheck(key);
        if (value != -1) {
            return value;
        }
        int value2 = getResourceIdForFingerdialogView(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getResourceIdForFingerView(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getResourceIdForFingerAppView(key);
        if (value4 != -1) {
            return value4;
        }
        int value5 = getResourceIdForFingerHint(key);
        if (value5 != -1) {
            return value5;
        }
        int value6 = getResourceIdForFaceHint(key);
        if (value6 != -1) {
            return value6;
        }
        int value7 = getResourceIdForFaceView(key);
        if (value7 != -1) {
            return value7;
        }
        return value7;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFingerprintCheck(String key) {
        char c;
        switch (key.hashCode()) {
            case -2068498690:
                if (key.equals("remote_view")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1654885476:
                if (key.equals("l2_back_fingerprint_usepassword_text_size")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1635244675:
                if (key.equals("l2_back_fingerprint_button_layout")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1426830592:
                if (key.equals("l2_back_fingerprint_button_layout_bottom_margin")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1068256066:
                if (key.equals("l2_cancel_fingerprint_button_top_margin")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -959854575:
                if (key.equals("level2_fingerprint_view")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -693408952:
                if (key.equals("l2_back_fingerprint_usepassword")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -452112213:
                if (key.equals("l2_back_fingerprint_button_top_margin")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -401704507:
                if (key.equals("fingerprint_ud_mask_hint")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 361738793:
                if (key.equals("l2_back_fingerprint_button_layout_height")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 451741418:
                if (key.equals("voice_fingerprint_checking_area")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 509016352:
                if (key.equals("fingerprint_view")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 570328489:
                if (key.equals("fingerprintView")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 634761108:
                if (key.equals("fingerprint_dual_fp_hint")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1124371604:
                if (key.equals("l2_back_fingerprint_cancel")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1133147237:
                if (key.equals("voice_fingerprint_mask_expand")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1167941046:
                if (key.equals("backfingerprintView_button_width")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1413439287:
                if (key.equals("backfingerprintView_button_height")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1461934602:
                if (key.equals("cancel_hotspot")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1549657220:
                if (key.equals("l2_back_fingerprint_button_layout_width")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1696916173:
                if (key.equals("voice_fingerprint_mask_close")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 33686299;
            case 1:
                return 33685721;
            case 2:
                return 33685697;
            case 3:
                return 33686300;
            case 4:
                return 33686301;
            case 5:
                return 34013416;
            case 6:
                return 34013348;
            case 7:
                return 34603236;
            case '\b':
                return 34603480;
            case '\t':
                return 34603409;
            case '\n':
                return 34603408;
            case 11:
                return 34603407;
            case '\f':
                return 34603040;
            case '\r':
                return 34472789;
            case 14:
                return 34472788;
            case 15:
                return 34472787;
            case 16:
                return 34472792;
            case 17:
                return 34472119;
            case 18:
                return 34472112;
            case 19:
                return 34472790;
            case 20:
                return 34472794;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFingerdialogView(String key) {
        char c;
        switch (key.hashCode()) {
            case -2082025524:
                if (key.equals("backfingerprintView_button_layout_height")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1798955258:
                if (key.equals("reenroll_fingerprint_message")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1489492506:
                if (key.equals("z_back_fingerprint_subtitle")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1460984888:
                if (key.equals("z_back_fingerprint_cancel")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1367724422:
                if (key.equals("cancel")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1345542670:
                if (key.equals("suspension_button_height")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -719926327:
                if (key.equals("z_back_fingerprint_button_layout")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -535631431:
                if (key.equals("finger_print_view_for_alipay_width")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -523179886:
                if (key.equals("Theme_Emui_Dialog_Alert")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -366862479:
                if (key.equals("fingerprint_anim_view_themes")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -293792940:
                if (key.equals("iv_fp_anim")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 114843:
                if (key.equals("tip")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 245910346:
                if (key.equals("z_back_fingerprint_title")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 617365503:
                if (key.equals("reenroll_fingerprint_positive_button_message")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 643304140:
                if (key.equals("fingerprint_recognized_fail")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 729194132:
                if (key.equals("finger_print_view_width")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 889664660:
                if (key.equals("z_back_fingerprint_usepassword")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1026535278:
                if (key.equals("z_back_fingerprint_description")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1470826113:
                if (key.equals("backfingerprintView_button_layout_width")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1475760155:
                if (key.equals("reenroll_fingerprint_button_message")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 2102771394:
                if (key.equals("reenroll_fingerprint_message_oversea")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34472118;
            case 1:
                return 34472117;
            case 2:
                return 34603575;
            case 3:
                return 34603572;
            case 4:
                return 34472589;
            case 5:
                return 34472904;
            case 6:
                return 34603405;
            case 7:
                return 34013345;
            case '\b':
                return 33685715;
            case '\t':
                return 33947691;
            case '\n':
                return 33686218;
            case 11:
                return 33686215;
            case '\f':
                return 33685797;
            case '\r':
                return 33686217;
            case 14:
                return 33686216;
            case 15:
                return 17039360;
            case 16:
                return 34472587;
            case 17:
                return 34603574;
            case 18:
                return 34603576;
            case 19:
                return 34603571;
            case 20:
                return 34603570;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFingerView(String key) {
        char c;
        switch (key.hashCode()) {
            case -1541361524:
                if (key.equals("bottom_margin_of_cancel_button")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1194217245:
                if (key.equals("use_password_hotspot")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1118214169:
                if (key.equals("navigationbar_height")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -963073787:
                if (key.equals("remoteview_right_margin")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -456371233:
                if (key.equals("app_lock_finger")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -194084212:
                if (key.equals("l2_remote_view_width")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 97769583:
                if (key.equals("app_back_fingerprint_cancel")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 165764844:
                if (key.equals("finger_print_button_height")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 586853548:
                if (key.equals("remoteview_top_margin")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 697186841:
                if (key.equals("finger_print_view_height")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 728724863:
                if (key.equals("fingerbuttomview")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 807899746:
                if (key.equals("suspension_button_hotspot_height")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 958270515:
                if (key.equals("bottom_margin_of_cancel_button_elle")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1191606318:
                if (key.equals("remoteview_land_margin")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1299639534:
                if (key.equals("remoteview_bottom_margin")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1888686143:
                if (key.equals("cancelView")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1984104736:
                if (key.equals("l2_cancel_fingerprint_button_width")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 2063582929:
                if (key.equals("l2_app_lock_finger_top_margin")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34603024;
            case 1:
                return 34472786;
            case 2:
                return 34472799;
            case 3:
                return 34472829;
            case 4:
                return 34472828;
            case 5:
                return 34472826;
            case 6:
                return 34472588;
            case 7:
                return 34472827;
            case '\b':
                return 34472802;
            case '\t':
                return 34472283;
            case '\n':
                return 34472284;
            case 11:
                return 34472905;
            case '\f':
                return 34603038;
            case '\r':
                return 34472584;
            case 14:
                return 34603235;
            case 15:
                return 34472795;
            case 16:
                return 34603559;
            case 17:
                return 34603023;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFingerAppView(String key) {
        char c;
        switch (key.hashCode()) {
            case -1629615882:
                if (key.equals("usepassword_text_size")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1577215844:
                if (key.equals("fingerprint_error_locked")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1518904604:
                if (key.equals("usepassword_height")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1458710275:
                if (key.equals("app_name_text_size")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1184940822:
                if (key.equals("acount_message_text_size")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1159354436:
                if (key.equals("app_title_and_summary_bottom_margin")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1103989343:
                if (key.equals("acount_message_top_margin")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -647324442:
                if (key.equals("fingerprint_inscreen_use_password")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 508599554:
                if (key.equals("fingerprint_hint")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 541908502:
                if (key.equals("acount_message")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 768596224:
                if (key.equals("hintview_text_size")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 834706353:
                if (key.equals("app_use_password")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1068226151:
                if (key.equals("hw_keychain_cancel")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1167648233:
                if (key.equals("app_name")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1612409317:
                if (key.equals("usepassword_bottom_margin")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 2074098489:
                if (key.equals("app_title_and_summary")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34603025;
            case 1:
                return 34472084;
            case 2:
                return 34603008;
            case 3:
                return 34471948;
            case 4:
                return 34471949;
            case 5:
                return 34603027;
            case 6:
                return 33685714;
            case 7:
                return 33685887;
            case '\b':
                return 34603026;
            case '\t':
                return 34472085;
            case '\n':
                return 34472950;
            case 11:
                return 34472949;
            case '\f':
                return 34472951;
            case '\r':
                return 34603237;
            case 14:
                return 34406411;
            case 15:
                return 34472605;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFingerHint(String key) {
        char c;
        switch (key.hashCode()) {
            case -2110642961:
                if (key.equals("hintview_bottom_margin")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -2033798821:
                if (key.equals("cancelView_image")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1766811994:
                if (key.equals("finger_image_only")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1759347822:
                if (key.equals("button_view")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1683129597:
                if (key.equals("fingerprint_inscreen_Identify_success")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1457871847:
                if (key.equals("fingerprint_image_only_view")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1366158495:
                if (key.equals("finger_print_button_width")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1343785119:
                if (key.equals("searchview_gobutton_height_emui")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1014032317:
                if (key.equals("fingerprint_inscreen_Identifying")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -624085274:
                if (key.equals("cancel_hotspot_image")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 53385170:
                if (key.equals("l2_hintview_top_margin")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 122185736:
                if (key.equals("l2_hintview_bottom_margin")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 142300052:
                if (key.equals("finger_print_view_for_alipay_height")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 463165547:
                if (key.equals("fingerprint_error_try_more")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 618462067:
                if (key.equals("bg_fingerprint_backside")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 815893687:
                if (key.equals("fingerprint_button_view")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1124450264:
                if (key.equals("z_backfingerprint")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1499170598:
                if (key.equals("btn_fingerprint_in_display")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1645585192:
                if (key.equals("l2_cancel_button_height")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34472604;
            case 1:
                return 34472798;
            case 2:
                return 34472797;
            case 3:
                return 33685704;
            case 4:
                return 33685705;
            case 5:
                return 34406412;
            case 6:
                return 34013347;
            case 7:
                return 34603234;
            case '\b':
                return 34603041;
            case '\t':
                return 33751602;
            case '\n':
                return 33751445;
            case 11:
                return 34471941;
            case '\f':
                return 34603039;
            case '\r':
                return 34472793;
            case 14:
                return 34472585;
            case 15:
                return 34472586;
            case 16:
                return 34013346;
            case 17:
                return 34603037;
            case 18:
                return 34013470;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFaceHint(String key) {
        char c;
        switch (key.hashCode()) {
            case -1435096385:
                if (key.equals("emui_biometric_face_detect_success")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1230883362:
                if (key.equals("emui_biometric_face_detect_usepassword")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1076691906:
                if (key.equals("emui_biometric_face_detect_cancel")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -817954814:
                if (key.equals("hw_facedetct_makesure_background")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -371971046:
                if (key.equals("emui_biometric_face_recognizing_mismatch")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 97769583:
                if (key.equals("app_back_fingerprint_cancel")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 144778710:
                if (key.equals("emui_biometric_face_detect_recognizing_fail")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 550819783:
                if (key.equals("emui_biometric_face_detect_makesure")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 726771571:
                if (key.equals("facebuttomview")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1071081095:
                if (key.equals("emui_biometric_face_detect_recognizing")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1929087296:
                if (key.equals("emui_biometric_face_detect_success_confirm")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 2069790837:
                if (key.equals("face_title_and_summary")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 33685670;
            case 1:
                return 33685669;
            case 2:
                return 33685673;
            case 3:
                return 33685668;
            case 4:
                return 33685671;
            case 5:
                return 33882609;
            case 6:
                return 34603225;
            case 7:
                return 34603023;
            case '\b':
                return 34603226;
            case '\t':
                return 33685672;
            case '\n':
                return 33685663;
            case 11:
                return 33685667;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForFaceView(String key) {
        char c;
        switch (key.hashCode()) {
            case -1771794149:
                if (key.equals("face_icon")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1575473198:
                if (key.equals("facebuttonmakesure")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1315775735:
                if (key.equals("facebuttoncancel")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1194217245:
                if (key.equals("use_password_hotspot")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1043010701:
                if (key.equals("emui_biometric_press_finger_detect")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -304830191:
                if (key.equals("face_animation_duration")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 550819783:
                if (key.equals("emui_biometric_face_detect_makesure")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 712223016:
                if (key.equals("detect_anmiation_complete")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 919298550:
                if (key.equals("face_title")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 944820261:
                if (key.equals("face_message")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 953012078:
                if (key.equals("ic_face_detecting_anim")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1048387445:
                if (key.equals("facebuttonPanel")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1081687314:
                if (key.equals("emui_biometric_finger_face_pay")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1115528845:
                if (key.equals("detect_anmiation_fail")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1461934602:
                if (key.equals("cancel_hotspot")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1777372724:
                if (key.equals("detect_anmiation_success")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 1853529545:
                if (key.equals("ic_face_all_info")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34603040;
            case 1:
                return 34603559;
            case 2:
                return 34603200;
            case 3:
                return 34603197;
            case 4:
                return 34603180;
            case 5:
                return 34603227;
            case 6:
                return 33685667;
            case 7:
                return 34603228;
            case '\b':
                return 34603229;
            case '\t':
                return 33685675;
            case '\n':
                return 33685674;
            case 11:
                return 33751852;
            case '\f':
                return 34275343;
            case '\r':
                return 33751855;
            case 14:
                return 33751853;
            case 15:
                return 33751854;
            case 16:
                return 34209877;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForAutoCamera(String key) {
        char c;
        switch (key.hashCode()) {
            case -2130643008:
                if (key.equals("camera_open_failed_protection")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1819744819:
                if (key.equals("camera_open_retry")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1501022725:
                if (key.equals("camera_switch_too_many")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -925092424:
                if (key.equals("camera_open_failed")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -724379198:
                if (key.equals("toast_layout_camera")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -159839860:
                if (key.equals("camera_use_cancel")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 26655467:
                if (key.equals("camera_falling_protection")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 403322686:
                if (key.equals("camera_close_failed")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 568691513:
                if (key.equals("camera_use_continue")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 715290872:
                if (key.equals("autocamera_text")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34013453;
            case 1:
                return 34603183;
            case 2:
                return 33685570;
            case 3:
                return 33685582;
            case 4:
                return 33685579;
            case 5:
                return 33685573;
            case 6:
                return 33685574;
            case 7:
                return 33685571;
            case '\b':
                return 33685564;
            case '\t':
                return 34406410;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForSecurity(String key) {
        char c;
        switch (key.hashCode()) {
            case -1923252574:
                if (key.equals("button_selftest_fail_recovery")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1721921262:
                if (key.equals("perm_icon_dropzone")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1111820710:
                if (key.equals("sendMMSPermission")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -957453172:
                if (key.equals("install_package_permission")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -723380735:
                if (key.equals("popuptitle_selftest_fail_info")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -20106182:
                if (key.equals("config_keyguardComponent")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -20104400:
                if (key.equals("perm_icon_homescreen_shortcuts")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -7313028:
                if (key.equals("popupwindow_selftest_fail")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 81595328:
                if (key.equals("permission_access_browser_records")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 302409171:
                if (key.equals("edit_shortcut_Permission")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 365533217:
                if (key.equals("perm_app_install_other_app")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 411058772:
                if (key.equals("permission_call_forward")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1650737394:
                if (key.equals("dropzoneAppTitle")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 2140802648:
                if (key.equals("toast_security")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 17039856;
            case 1:
                return 33685662;
            case 2:
                return 33685659;
            case 3:
                return 33686160;
            case 4:
                return 33686235;
            case 5:
                return 33685916;
            case 6:
                return 33686159;
            case 7:
                return 33686285;
            case '\b':
                return 33752337;
            case '\t':
                return 33752336;
            case '\n':
                return 33752335;
            case 11:
                return 33686205;
            case '\f':
                return 33686208;
            case '\r':
                return 33685563;
            default:
                return -1;
        }
    }

    private static int getStringResourceIdForPowerOffice(String key) {
        return powerStringOfficeResourceIdMap.getOrDefault(key, -1).intValue();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getLayoutResourceIdForPowerOffice(String key) {
        boolean z;
        switch (key.hashCode()) {
            case -726230849:
                if (key.equals("pc_decision_dialog")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 149065316:
                if (key.equals("switch_pc_mode")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 340668964:
                if (key.equals("bluetooth_reminder_dialog")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 647729849:
                if (key.equals("window_cast_mode_reminder_secure")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1309761656:
                if (key.equals("pc_notification_big")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1323132855:
                if (key.equals("pc_notification")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 34013439;
        }
        if (z) {
            return 34013425;
        }
        if (z) {
            return 34013424;
        }
        if (z) {
            return 34013469;
        }
        if (z) {
            return 34013231;
        }
        if (!z) {
            return -1;
        }
        return 34013423;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getIdResourceIdForPowerOffice(String key) {
        char c;
        switch (key.hashCode()) {
            case -1751981878:
                if (key.equals("pc_notification_switch_phone_action")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1583722258:
                if (key.equals("pc_notification_device_text")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1426071557:
                if (key.equals("switch_desktop_mode_remember")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1292455524:
                if (key.equals("pc_notification_switch_desktop_action")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1183376551:
                if (key.equals("pc_notification_touchpad_action")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1060848703:
                if (key.equals("pc_notification_mode_text")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -990624519:
                if (key.equals("pc_decision_dialog_content")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -954551078:
                if (key.equals("reminder_text")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -951985701:
                if (key.equals("pc_decision_dialog_image")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -886631332:
                if (key.equals("never_notify")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -685453642:
                if (key.equals("hw_caption")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -90150393:
                if (key.equals("notify_detail")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -30802314:
                if (key.equals("reminder_img")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 5770676:
                if (key.equals("pc_notification_disconnect_icon")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 14085070:
                if (key.equals("switch_desktop_mode_description")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 104820065:
                if (key.equals("hw_caption_rtl")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 272173008:
                if (key.equals("pc_notification_title")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 888391260:
                if (key.equals("pc_notification_touchpad_icon")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1021766833:
                if (key.equals("pc_notification_disconnect_action")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 2088208924:
                if (key.equals("pc_notification_switch_icon")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 2104678061:
                if (key.equals("switch_desktop_mode_image")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 34603527;
            case 1:
                return 34603526;
            case 2:
                return 34603528;
            case 3:
                return 34603456;
            case 4:
                return 34603454;
            case 5:
                return 34603458;
            case 6:
                return 34603455;
            case 7:
                return 34603451;
            case '\b':
                return 34603459;
            case '\t':
                return 34603452;
            case '\n':
                return 34603457;
            case 11:
                return 34603453;
            case '\f':
                return 34603450;
            case '\r':
                return 34603274;
            case 14:
                return 34603273;
            case 15:
                return 34603478;
            case 16:
                return 34603479;
            case 17:
                return 34603439;
            case 18:
                return 34603443;
            case 19:
                return 34603447;
            case 20:
                return 34603446;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getDrawableResourceIdForPowerOffice(String key) {
        char c;
        switch (key.hashCode()) {
            case -1875044080:
                if (key.equals("ic_notify_pc_btn_phone")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1115641789:
                if (key.equals("ic_lock_window_cast_mode")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -724266258:
                if (key.equals("ic_notify_cast_control")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -173470914:
                if (key.equals("ic_notify_pc_btn_desktop")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 229151552:
                if (key.equals("hw_decor_caption_title_dark")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 946437665:
                if (key.equals("ic_safe_window_cast_mode")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1672527292:
                if (key.equals("image_pc_pad_setting")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1781444149:
                if (key.equals("hw_decor_caption_title")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 33752307;
            case 1:
                return 33751738;
            case 2:
                return 33752269;
            case 3:
                return 33752267;
            case 4:
                return 33751782;
            case 5:
                return 33752013;
            case 6:
                return 33752250;
            case 7:
                return 33752299;
            default:
                return -1;
        }
    }

    private static int getColorResourceIdForPowerOffice(String key) {
        if ((key.hashCode() == -207643736 && key.equals("hw_decor_title_text_white")) ? false : true) {
            return -1;
        }
        return 33883075;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045 A[RETURN] */
    private static int getDimenResourceIdForPowerOffice(String key) {
        boolean z;
        int hashCode = key.hashCode();
        if (hashCode != -2055242121) {
            if (hashCode != 257843204) {
                if (hashCode == 1748784758 && key.equals("hw_hicar_dock_width")) {
                    z = true;
                    if (z) {
                        return 34472523;
                    }
                    if (z) {
                        return 34472521;
                    }
                    if (!z) {
                        return -1;
                    }
                    return 34472522;
                }
            } else if (key.equals("hw_hicar_status_bar_height")) {
                z = false;
                if (z) {
                }
            }
        } else if (key.equals("hw_hicar_dock_height")) {
            z = true;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    private static int getResourceIdForPowerOffice(String key) {
        int value = getStringResourceIdForPowerOffice(key);
        if (value != -1) {
            return value;
        }
        int value2 = getLayoutResourceIdForPowerOffice(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getIdResourceIdForPowerOffice(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getDrawableResourceIdForPowerOffice(key);
        if (value4 != -1) {
            return value4;
        }
        int value5 = getColorResourceIdForPowerOffice(key);
        if (value5 != -1) {
            return value5;
        }
        int value6 = getDimenResourceIdForPowerOffice(key);
        if (value6 != -1) {
            return value6;
        }
        return -1;
    }

    private static int getResourceIdForMmiCode(String key) {
        return mmiCodeStringResourceIdMap.getOrDefault(key, -1).intValue();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002f A[RETURN] */
    private static int getResourceIdForMedia(String key) {
        boolean z;
        int hashCode = key.hashCode();
        if (hashCode != -320641599) {
            if (hashCode == 935865500 && key.equals("frontcamera_slide_tip")) {
                z = false;
                if (!z) {
                    return 34013349;
                }
                if (!z) {
                    return -1;
                }
                return 34603249;
            }
        } else if (key.equals("frontcamera_slide_imageview")) {
            z = true;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForPadEdu(String key) {
        char c;
        switch (key.hashCode()) {
            case -2110466121:
                if (key.equals("tips_circle")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1936473494:
                if (key.equals("magic_window_toast")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1912319805:
                if (key.equals("activity_open_exit")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1892826571:
                if (key.equals("activity_close_exit")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1647969273:
                if (key.equals("cubic_bezier_interpolator_type_33_33")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1310739770:
                if (key.equals("tips_cb")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -767686477:
                if (key.equals("hw_multiwindow_freeform_corner_radius")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -253297697:
                if (key.equals("hw_split_divider_bar_width")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 129604664:
                if (key.equals("tips_dragBar")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 617414370:
                if (key.equals("magic_window_bg")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 630264469:
                if (key.equals("magic_window_tips")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 847340499:
                if (key.equals("activity_open_enter")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 873729603:
                if (key.equals("magic_window_confirm")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 927374011:
                if (key.equals("tips_go_settings_magicwin")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1206212742:
                if (key.equals("tips_pageRight")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1424200797:
                if (key.equals("tips_pageLeft")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1451630753:
                if (key.equals("activity_close_enter")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return ConstantValues.FREEFORM_RADIUS;
            case 1:
                return 34209803;
            case 2:
                return 34209805;
            case 3:
                return 34209800;
            case 4:
                return 34209808;
            case 5:
                return 34013418;
            case 6:
                return 33686026;
            case 7:
                return 34013419;
            case '\b':
                return 33686032;
            case '\t':
                return 34603538;
            case '\n':
                return 34603542;
            case 11:
                return 34603543;
            case '\f':
                return 34603539;
            case '\r':
                return 34603540;
            case 14:
                return 34603537;
            case 15:
                return 34078724;
            case 16:
                return 34472627;
            default:
                return -1;
        }
    }
}
