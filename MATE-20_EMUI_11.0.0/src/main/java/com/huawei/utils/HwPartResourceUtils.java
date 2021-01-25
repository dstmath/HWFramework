package com.huawei.utils;

import android.util.HwLogExceptionInner;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.bastet.BastetParameters;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.internal.widget.ConstantValues;
import com.huawei.sidetouch.TpCommandConstant;

public class HwPartResourceUtils {
    public static final int INVALID_ID = -1;

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
        int value2 = getResourceIdForTelephony(key);
        if (value2 != -1) {
            return value2;
        }
        int value3 = getResourceIdForSecurity(key);
        if (value3 != -1) {
            return value3;
        }
        int value4 = getResourceIdForPowerOffice(key);
        if (value4 != -1) {
            return value4;
        }
        int value5 = getResourceIdForMmiCode(key);
        if (value5 != -1) {
            return value5;
        }
        int value6 = getResourceIdForAutoCamera(key);
        if (value6 != -1) {
            return value6;
        }
        int value7 = getResourceIdForSingleHand(key);
        if (value7 != -1) {
            return value7;
        }
        int value8 = getResourceIdForMedia(key);
        if (value8 != -1) {
            return value8;
        }
        int value9 = getResourceIdForPadEdu(key);
        if (value9 != -1) {
            return value9;
        }
        return value9;
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
                return 33685714;
            case 1:
                return 33685705;
            case 2:
                return 33685703;
            case 3:
                return 33685704;
            case 4:
                return 33685697;
            case 5:
                return 33686253;
            case 6:
                return 33686252;
            case 7:
                return 33685713;
            case '\b':
                return 34603252;
            case '\t':
                return 34603250;
            case '\n':
                return 34603253;
            case 11:
                return 34603251;
            case '\f':
                return 34603164;
            case '\r':
                return 34603524;
            case 14:
                return 34603235;
            case 15:
                return 34603237;
            case 16:
                return 34603234;
            case 17:
                return 34603233;
            case 18:
                return 34603232;
            case 19:
                return 34603236;
            case 20:
                return 34603049;
            case 21:
                return 33751911;
            case 22:
                return 33752020;
            case 23:
                return 33752094;
            case 24:
                return 33752048;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForTelephony(String key) {
        char c;
        switch (key.hashCode()) {
            case -1994759852:
                if (key.equals("config_device_vt_available")) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case -1781600493:
                if (key.equals("network_limit_prompt_content")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -1774060327:
                if (key.equals("network_limit_prompt")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -1710080684:
                if (key.equals("notification_typeC_nonotify")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1642130809:
                if (key.equals("skip_restoring_network_selection")) {
                    c = '\'';
                    break;
                }
                c = 65535;
                break;
            case -1463854439:
                if (key.equals("smart_card_5G_bind_toast")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1424422315:
                if (key.equals("data_near_explain")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1372005574:
                if (key.equals("data_remind")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1367724422:
                if (key.equals("cancel")) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -1279876122:
                if (key.equals("sim_card_manager")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1204717312:
                if (key.equals("gsm_alphabet_default_charset")) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case -1035510794:
                if (key.equals("ic_phone_fail_statusbar")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -952305534:
                if (key.equals("config_device_volte_available")) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -770819464:
                if (key.equals("config_sms_enabled_single_shift_tables")) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -523179886:
                if (key.equals("Theme_Emui_Dialog_Alert")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -309722556:
                if (key.equals("defaultVoiceMailAlphaTag")) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case -295804352:
                if (key.equals("network_limit_prompt_summary")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case -243760243:
                if (key.equals("network_rejinfo_notify_title")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -202696928:
                if (key.equals("smart_card_4G_5G_switch_back_toast")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -202305772:
                if (key.equals("ic_sim_statusbar")) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case -151891815:
                if (key.equals("data_near_limit")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -66577298:
                if (key.equals("network_rejinfo_notify_content")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 159857663:
                if (key.equals("network_rejinfo_notification_action")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 193306705:
                if (key.equals("sim_removed_title")) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case 325872594:
                if (key.equals("data_enable_confirm_title")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 358898085:
                if (key.equals("back_to_main")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 501250726:
                if (key.equals("uim_tip_ok")) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 661784443:
                if (key.equals("data_enable_confirm_msg")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 679574936:
                if (key.equals("magngesture_dialog_ignore")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 914706337:
                if (key.equals("data_remind_summary")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1191408099:
                if (key.equals("global_actions_toggle_airplane_mode")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1259473723:
                if (key.equals("popupwindow_incall_networking_prompt_text1")) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case 1263440144:
                if (key.equals("alert_dialog_ok")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 1283649839:
                if (key.equals("data_saver_enable_button")) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case 1293107471:
                if (key.equals("smart_card_4G_switch_toast")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1484367234:
                if (key.equals("config_device_wfc_ims_available")) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case 1690828612:
                if (key.equals("emergency_calls_only")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1747727825:
                if (key.equals("systemui_vowifi_operator_name")) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case 1805302008:
                if (key.equals("network_rejinfo_notification_app")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1835164946:
                if (key.equals("lockscreen_carrier_default")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1937387263:
                if (key.equals("wfcSpnFormats")) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case 1993891474:
                if (key.equals("data_enable_confirm_msg_vowifi")) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case 2059941617:
                if (key.equals("sim_added_title")) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case 2099983851:
                if (key.equals("config_sms_enabled_locking_shift_tables")) {
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case 2143698140:
                if (key.equals("switch_notify_toast")) {
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
                return 33686043;
            case 1:
                return 33752014;
            case 2:
                return 33686044;
            case 3:
                return 33686051;
            case 4:
                return 33686050;
            case 5:
                return 17040219;
            case 6:
                return 17040419;
            case 7:
                return 17040030;
            case '\b':
                return 33686246;
            case '\t':
                return 33686228;
            case '\n':
                return 33686227;
            case 11:
                return 33686229;
            case '\f':
                return 33685615;
            case '\r':
                return 33685614;
            case 14:
                return 33686216;
            case 15:
                return 33685616;
            case 16:
                return 33685617;
            case 17:
                return 33685985;
            case 18:
                return 33685756;
            case 19:
                return 33685532;
            case 20:
                return 33685725;
            case 21:
                return 33686038;
            case 22:
                return 33686042;
            case 23:
                return 33686041;
            case 24:
                return 33947691;
            case 25:
                return 33685613;
            case 26:
                return 33685611;
            case 27:
                return 33685612;
            case 28:
                return 17039360;
            case 29:
                return 17040237;
            case 30:
                return 17039370;
            case 31:
                return 17041236;
            case ' ':
                return 17041239;
            case '!':
                return 17039364;
            case '\"':
                return 17039922;
            case '#':
                return 33686183;
            case '$':
                return 17891405;
            case '%':
                return 17891404;
            case '&':
                return 17891403;
            case '\'':
                return 17891621;
            case '(':
                return 17236062;
            case ')':
                return 17236061;
            case '*':
                return 17236117;
            case '+':
                return 17041358;
            case ',':
                return 33752021;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForBasicPlatform(String key) {
        char c;
        switch (key.hashCode()) {
            case -2050330750:
                if (key.equals("left_circle_logo")) {
                    c = '@';
                    break;
                }
                c = 65535;
                break;
            case -1971601134:
                if (key.equals("slide_out_circle_logo_side_margin")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1946840984:
                if (key.equals("non_standard_charge_line_title")) {
                    c = '2';
                    break;
                }
                c = 65535;
                break;
            case -1791309455:
                if (key.equals("wireless_tx_error_low_battery")) {
                    c = '/';
                    break;
                }
                c = 65535;
                break;
            case -1780162851:
                if (key.equals("config_notificationsBatteryFullARGB")) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case -1772244925:
                if (key.equals("config_notificationsBatteryMediumARGB")) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case -1743417713:
                if (key.equals("slide_out_max_distance")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1742891583:
                if (key.equals("notify_know")) {
                    c = '<';
                    break;
                }
                c = 65535;
                break;
            case -1694973564:
                if (key.equals("default_wallpaper")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -1647969273:
                if (key.equals("cubic_bezier_interpolator_type_33_33")) {
                    c = 'M';
                    break;
                }
                c = 65535;
                break;
            case -1631871138:
                if (key.equals("slide_out_circle_diameter_max_size")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1540336536:
                if (key.equals("gesture_nav_back_max_distance_1")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1540336535:
                if (key.equals("gesture_nav_back_max_distance_2")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1480134508:
                if (key.equals("battery_iscd_error_notification_icon")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -1410993327:
                if (key.equals("close_layout")) {
                    c = 'I';
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
            case -1344547834:
                if (key.equals("notify_description")) {
                    c = 'L';
                    break;
                }
                c = 65535;
                break;
            case -1298360886:
                if (key.equals("ic_hivoice_oversea_app")) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case -1033716838:
                if (key.equals("ic_dock_app")) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case -974638712:
                if (key.equals("config_notificationsBatteryLedOn")) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case -841625987:
                if (key.equals("gesture_nav_back_window_width")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -840660532:
                if (key.equals("non_standard_charging_line_profile_url")) {
                    c = '5';
                    break;
                }
                c = 65535;
                break;
            case -717454760:
                if (key.equals("screen_on_proximity_view_land")) {
                    c = '>';
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
            case -341235177:
                if (key.equals("non_standard_charge_line_message")) {
                    c = '3';
                    break;
                }
                c = 65535;
                break;
            case -311912715:
                if (key.equals("notify_dismiss_softinput")) {
                    c = '?';
                    break;
                }
                c = 65535;
                break;
            case -256449576:
                if (key.equals("wireless_tx_error_temperature_low")) {
                    c = '-';
                    break;
                }
                c = 65535;
                break;
            case -250338241:
                if (key.equals("mask_circle_oversea")) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case -208721198:
                if (key.equals("fold_disable_touch_padding_top")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -205566778:
                if (key.equals("disable_touch_notchview")) {
                    c = 'E';
                    break;
                }
                c = 65535;
                break;
            case -149029146:
                if (key.equals("config_notificationsBatteryLedOff")) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -86433766:
                if (key.equals("toast_gesture_retry")) {
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case -47741946:
                if (key.equals("battery_iscd_error_notification_msg")) {
                    c = '1';
                    break;
                }
                c = 65535;
                break;
            case -21535010:
                if (key.equals("config_notificationsBatteryLowARGB")) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case 194783274:
                if (key.equals("non_standard_charge_line_button_text")) {
                    c = '4';
                    break;
                }
                c = 65535;
                break;
            case 257729274:
                if (key.equals("mis_touch_hint_layout")) {
                    c = 'G';
                    break;
                }
                c = 65535;
                break;
            case 278039962:
                if (key.equals("navigation_bar_height_landscape")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 310368618:
                if (key.equals("slide_out_circle_travel_distance")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 514797397:
                if (key.equals("wireless_tx_error_full_device")) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case 532094270:
                if (key.equals("image_laptop")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 572382990:
                if (key.equals("mask_circle1")) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case 572382991:
                if (key.equals("mask_circle2")) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case 630374991:
                if (key.equals("slide_out_start_threshold")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 630789324:
                if (key.equals("slide_out_circle_base_margin")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 639872414:
                if (key.equals("wireless_tx_error_temperature_high")) {
                    c = '.';
                    break;
                }
                c = 65535;
                break;
            case 668352511:
                if (key.equals("toast_hiVision_not_available")) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case 694738398:
                if (key.equals("navigation_bar_height")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 886899504:
                if (key.equals("slide_out_circle_diameter_min_size")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 894114179:
                if (key.equals("notify_content")) {
                    c = ':';
                    break;
                }
                c = 65535;
                break;
            case 919226644:
                if (key.equals("gesture_nav_bottom_window_height")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 928728477:
                if (key.equals("slide_out_circle_center_start_pos")) {
                    c = 17;
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
            case 955038389:
                if (key.equals("wireless_tx_status_error")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 1074750786:
                if (key.equals("battery_charge_notification_channel_name")) {
                    c = '6';
                    break;
                }
                c = 65535;
                break;
            case 1077819710:
                if (key.equals("disable_touch_hint")) {
                    c = 'F';
                    break;
                }
                c = 65535;
                break;
            case 1091708282:
                if (key.equals("gesture_slide_out_view")) {
                    c = '=';
                    break;
                }
                c = 65535;
                break;
            case 1116659355:
                if (key.equals("anim_text")) {
                    c = 'J';
                    break;
                }
                c = 65535;
                break;
            case 1135162361:
                if (key.equals("Animation_RecentApplications")) {
                    c = '\'';
                    break;
                }
                c = 65535;
                break;
            case 1143740951:
                if (key.equals("right_circle_logo")) {
                    c = 'A';
                    break;
                }
                c = 65535;
                break;
            case 1179105159:
                if (key.equals("slide_out_scrim")) {
                    c = 'B';
                    break;
                }
                c = 65535;
                break;
            case 1182283737:
                if (key.equals("mistouch_prevention_quitnote2")) {
                    c = '7';
                    break;
                }
                c = 65535;
                break;
            case 1189503080:
                if (key.equals("ic_hivoice_oversea")) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case 1272098723:
                if (key.equals("wireless_tx_error_no_device")) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case 1284359852:
                if (key.equals("ic_hivoice")) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case 1370812829:
                if (key.equals("battery_iscd_error_notification_title")) {
                    c = '0';
                    break;
                }
                c = 65535;
                break;
            case 1372049228:
                if (key.equals("gesture_nav_bottom_quick_out_height")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1382503648:
                if (key.equals("gesture_nav_bottom_side_width")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1575080903:
                if (key.equals("split_app_three_finger_slide_message")) {
                    c = '9';
                    break;
                }
                c = 65535;
                break;
            case 1629467435:
                if (key.equals("gesture_nav_curved_offset")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1661231814:
                if (key.equals("ic_battery_non_standard_charge_line")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 1700706169:
                if (key.equals("mistouch_prevention_quitnote")) {
                    c = '8';
                    break;
                }
                c = 65535;
                break;
            case 1731386482:
                if (key.equals("system_notification_accent_color")) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case 1739990575:
                if (key.equals("slide_out_circle")) {
                    c = 'C';
                    break;
                }
                c = 65535;
                break;
            case 1770187795:
                if (key.equals("mis_touch_bottom")) {
                    c = 'H';
                    break;
                }
                c = 65535;
                break;
            case 1803045093:
                if (key.equals("notify_image")) {
                    c = 'K';
                    break;
                }
                c = 65535;
                break;
            case 1813103074:
                if (key.equals("notify_title")) {
                    c = ';';
                    break;
                }
                c = 65535;
                break;
            case 2114275289:
                if (key.equals("slide_out_circle_legacy")) {
                    c = 'D';
                    break;
                }
                c = 65535;
                break;
            case 2119368151:
                if (key.equals("gesture_nav_back_anim")) {
                    c = 26;
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
                return 17105307;
            case 4:
                return 17105309;
            case 5:
                return 17105188;
            case 6:
                return 17105187;
            case 7:
                return 34472818;
            case '\b':
                return 34472817;
            case '\t':
                return 34472555;
            case '\n':
                return 34472556;
            case 11:
                return 34472557;
            case '\f':
                return 34472553;
            case '\r':
                return 34472554;
            case 14:
                return 34472814;
            case 15:
                return 34472813;
            case 16:
                return 34472812;
            case 17:
                return 34472811;
            case 18:
                return 34472810;
            case 19:
                return 34472816;
            case 20:
                return 34472520;
            case 21:
                return 17302139;
            case 22:
                return 33752175;
            case 23:
                return 33751117;
            case 24:
                return 33751968;
            case 25:
                return 33752026;
            case 26:
                return 33751912;
            case 27:
                return 33751972;
            case 28:
                return 33752045;
            case 29:
                return 33752046;
            case 30:
                return 33752047;
            case 31:
                return 33751985;
            case ' ':
                return 33751986;
            case '!':
                return 33751987;
            case '\"':
                return 17694866;
            case '#':
                return 17694867;
            case '$':
                return 17694863;
            case '%':
                return 17694865;
            case '&':
                return 17694864;
            case '\'':
                return 16974596;
            case '(':
                return 17170460;
            case ')':
                return 33686255;
            case '*':
                return 33686256;
            case '+':
                return 33686301;
            case ',':
                return 33686303;
            case '-':
                return 33686305;
            case '.':
                return 33686304;
            case '/':
                return 33686302;
            case '0':
                return 33685541;
            case '1':
                return 33685540;
            case '2':
                return 33686071;
            case '3':
                return 33686070;
            case '4':
                return 33686069;
            case '5':
                return 33686072;
            case IDisplayEngineService.DE_ACTION_MOTION_SWAP /* 54 */:
                return 33685539;
            case IDisplayEngineService.DE_ACTION_MAX /* 55 */:
                return 33686030;
            case '8':
                return 33686028;
            case '9':
                return 33686234;
            case ':':
                return 33686091;
            case ';':
                return 33686093;
            case '<':
                return 33686092;
            case '=':
                return 34013349;
            case '>':
                return 34013425;
            case '?':
                return 34013420;
            case '@':
                return 34603401;
            case HwLogExceptionInner.LEVEL_A /* 65 */:
                return 34603467;
            case HwLogExceptionInner.LEVEL_B /* 66 */:
                return 34603491;
            case HwLogExceptionInner.LEVEL_C /* 67 */:
                return 34603488;
            case HwLogExceptionInner.LEVEL_D /* 68 */:
                return 34603489;
            case 'E':
                return 34603061;
            case 'F':
                return 34603060;
            case 'G':
                return 34603419;
            case 'H':
                return 34603418;
            case 'I':
                return 34603045;
            case 'J':
                return 34603022;
            case TpCommandConstant.VOLUME_FLICK_THRESHOLD_MIN /* 75 */:
                return 34603430;
            case 'L':
                return 34603428;
            case 'M':
                return 34078724;
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
                return 34013437;
            case 1:
                return 34603183;
            case 2:
                return 33685564;
            case 3:
                return 33685579;
            case 4:
                return 33685574;
            case 5:
                return 33685571;
            case 6:
                return 33685573;
            case 7:
                return 33685570;
            case '\b':
                return 33685563;
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
                return 17039866;
            case 1:
                return 33685661;
            case 2:
                return 33685658;
            case 3:
                return 33686141;
            case 4:
                return 33686211;
            case 5:
                return 33685814;
            case 6:
                return 33686140;
            case 7:
                return 33686261;
            case '\b':
                return 33752057;
            case '\t':
                return 33752056;
            case '\n':
                return 33752055;
            case 11:
                return 33686182;
            case '\f':
                return 33686185;
            case '\r':
                return 33685562;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForPowerOffice(String key) {
        char c;
        switch (key.hashCode()) {
            case -2116287505:
                if (key.equals("pc_vassist_prev_page")) {
                    c = 'U';
                    break;
                }
                c = 65535;
                break;
            case -2097696376:
                if (key.equals("pc_vassist_ok")) {
                    c = 'Q';
                    break;
                }
                c = 65535;
                break;
            case -2055242121:
                if (key.equals("hw_hicar_dock_height")) {
                    c = 'm';
                    break;
                }
                c = 65535;
                break;
            case -2013897267:
                if (key.equals("pc_vassist_no_problem")) {
                    c = 'R';
                    break;
                }
                c = 65535;
                break;
            case -1997802991:
                if (key.equals("desktop_mode_exit_content")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1957761204:
                if (key.equals("notification_pc_phone_mode")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1939330414:
                if (key.equals("pc_recommend_dialog_open")) {
                    c = 'e';
                    break;
                }
                c = 65535;
                break;
            case -1888571806:
                if (key.equals("desktop_mode_exit_exclusive_keyboard2")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1875044080:
                if (key.equals("ic_notify_pc_btn_phone")) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case -1857788881:
                if (key.equals("pc_vassist_close_ok")) {
                    c = 'P';
                    break;
                }
                c = 65535;
                break;
            case -1786653982:
                if (key.equals("pc_vassist_next_page1")) {
                    c = 'Z';
                    break;
                }
                c = 65535;
                break;
            case -1786653981:
                if (key.equals("pc_vassist_next_page2")) {
                    c = '[';
                    break;
                }
                c = 65535;
                break;
            case -1786653980:
                if (key.equals("pc_vassist_next_page3")) {
                    c = '\\';
                    break;
                }
                c = 65535;
                break;
            case -1751981878:
                if (key.equals("pc_notification_switch_phone_action")) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case -1633817211:
                if (key.equals("pc_vassist_start_ok")) {
                    c = 'N';
                    break;
                }
                c = 65535;
                break;
            case -1583722258:
                if (key.equals("pc_notification_device_text")) {
                    c = '\'';
                    break;
                }
                c = 65535;
                break;
            case -1471718341:
                if (key.equals("pc_vassist_disconn_external_display_tip")) {
                    c = 'T';
                    break;
                }
                c = 65535;
                break;
            case -1426071557:
                if (key.equals("switch_desktop_mode_remember")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1292455524:
                if (key.equals("pc_notification_switch_desktop_action")) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -1267255179:
                if (key.equals("bluetooth_notify_dialog_open_title")) {
                    c = 'C';
                    break;
                }
                c = 65535;
                break;
            case -1260968373:
                if (key.equals("notification_btn_touchpad")) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case -1230250023:
                if (key.equals("pc_vassist_ok_done")) {
                    c = 'c';
                    break;
                }
                c = 65535;
                break;
            case -1216215046:
                if (key.equals("desktop_mode_exit_start")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -1215601168:
                if (key.equals("desktop_mode_exit_title")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -1215440449:
                if (key.equals("desktop_mode_exit_toast")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1183376551:
                if (key.equals("pc_notification_touchpad_action")) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case -1180403166:
                if (key.equals("pc_vassist_prev_page1")) {
                    c = 'V';
                    break;
                }
                c = 65535;
                break;
            case -1180403165:
                if (key.equals("pc_vassist_prev_page2")) {
                    c = 'W';
                    break;
                }
                c = 65535;
                break;
            case -1180403164:
                if (key.equals("pc_vassist_prev_page3")) {
                    c = 'X';
                    break;
                }
                c = 65535;
                break;
            case -1115641789:
                if (key.equals("ic_lock_window_cast_mode")) {
                    c = ':';
                    break;
                }
                c = 65535;
                break;
            case -1112956483:
                if (key.equals("notification_btn_desktop_mode")) {
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case -1096258307:
                if (key.equals("pc_vassist_start_in_screen")) {
                    c = 'H';
                    break;
                }
                c = 65535;
                break;
            case -1071520363:
                if (key.equals("pc_vassist_play_ppt")) {
                    c = ']';
                    break;
                }
                c = 65535;
                break;
            case -1060848703:
                if (key.equals("pc_notification_mode_text")) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -1047562910:
                if (key.equals("desktop_mode_enter_start")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1046949032:
                if (key.equals("desktop_mode_enter_title")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1046788313:
                if (key.equals("desktop_mode_enter_toast")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -999423065:
                if (key.equals("pc_dp_link_error1")) {
                    c = '.';
                    break;
                }
                c = 65535;
                break;
            case -999423064:
                if (key.equals("pc_dp_link_error2")) {
                    c = '/';
                    break;
                }
                c = 65535;
                break;
            case -990624519:
                if (key.equals("pc_decision_dialog_content")) {
                    c = 'h';
                    break;
                }
                c = 65535;
                break;
            case -954551078:
                if (key.equals("reminder_text")) {
                    c = '9';
                    break;
                }
                c = 65535;
                break;
            case -951985701:
                if (key.equals("pc_decision_dialog_image")) {
                    c = 'g';
                    break;
                }
                c = 65535;
                break;
            case -886631332:
                if (key.equals("never_notify")) {
                    c = '>';
                    break;
                }
                c = 65535;
                break;
            case -885995718:
                if (key.equals("pc_recommend_dialog_desc_new")) {
                    c = 'j';
                    break;
                }
                c = 65535;
                break;
            case -852033173:
                if (key.equals("notification_btn_phone_mode")) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -772774040:
                if (key.equals("desktop_mode_enter_exclusive_keyboard")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -726230849:
                if (key.equals("pc_decision_dialog")) {
                    c = 'd';
                    break;
                }
                c = 65535;
                break;
            case -724266258:
                if (key.equals("ic_notify_cast_control")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -685453642:
                if (key.equals("hw_caption")) {
                    c = '1';
                    break;
                }
                c = 65535;
                break;
            case -596350173:
                if (key.equals("desktop_mode_enter_settings")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -574111793:
                if (key.equals("pc_vassist_cannot_do_it")) {
                    c = 'J';
                    break;
                }
                c = 65535;
                break;
            case -544930759:
                if (key.equals("pc_vassist_inline_search_market")) {
                    c = 'I';
                    break;
                }
                c = 65535;
                break;
            case -400928729:
                if (key.equals("pc_vassist_cannot_larger")) {
                    c = 'b';
                    break;
                }
                c = 65535;
                break;
            case -315082051:
                if (key.equals("pc_vassist_start_in_phone")) {
                    c = 'G';
                    break;
                }
                c = 65535;
                break;
            case -207643736:
                if (key.equals("hw_decor_title_text_white")) {
                    c = '5';
                    break;
                }
                c = 65535;
                break;
            case -173470914:
                if (key.equals("ic_notify_pc_btn_desktop")) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case -167879023:
                if (key.equals("notification_pc_connected")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -90150393:
                if (key.equals("notify_detail")) {
                    c = '?';
                    break;
                }
                c = 65535;
                break;
            case -87447745:
                if (key.equals("bluetooth_notify_dialog_close_title")) {
                    c = 'A';
                    break;
                }
                c = 65535;
                break;
            case -57634001:
                if (key.equals("pc_vassist_next_page")) {
                    c = 'Y';
                    break;
                }
                c = 65535;
                break;
            case -45640884:
                if (key.equals("pc_vassist_app_not_found")) {
                    c = '_';
                    break;
                }
                c = 65535;
                break;
            case -30802314:
                if (key.equals("reminder_img")) {
                    c = '8';
                    break;
                }
                c = 65535;
                break;
            case 5770676:
                if (key.equals("pc_notification_disconnect_icon")) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case 7197436:
                if (key.equals("pc_recommend_dialog_learn")) {
                    c = 'i';
                    break;
                }
                c = 65535;
                break;
            case 14085070:
                if (key.equals("switch_desktop_mode_description")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 104820065:
                if (key.equals("hw_caption_rtl")) {
                    c = '0';
                    break;
                }
                c = 65535;
                break;
            case 139464730:
                if (key.equals("pc_recommend_dialog_ignore")) {
                    c = 'f';
                    break;
                }
                c = 65535;
                break;
            case 149065316:
                if (key.equals("switch_pc_mode")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 229151552:
                if (key.equals("hw_decor_caption_title_dark")) {
                    c = '4';
                    break;
                }
                c = 65535;
                break;
            case 257843204:
                if (key.equals("hw_hicar_status_bar_height")) {
                    c = 'l';
                    break;
                }
                c = 65535;
                break;
            case 272173008:
                if (key.equals("pc_notification_title")) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case 340668964:
                if (key.equals("bluetooth_reminder_dialog")) {
                    c = '=';
                    break;
                }
                c = 65535;
                break;
            case 355556909:
                if (key.equals("pc_vassist_close_external_app_for_start")) {
                    c = 'L';
                    break;
                }
                c = 65535;
                break;
            case 456550988:
                if (key.equals("window_cast_mode_reminder_locked")) {
                    c = ';';
                    break;
                }
                c = 65535;
                break;
            case 476798434:
                if (key.equals("desktop_mode_exit_cancel")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 575822810:
                if (key.equals("proj_mode_notification_label")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 647729849:
                if (key.equals("window_cast_mode_reminder_secure")) {
                    c = '6';
                    break;
                }
                c = 65535;
                break;
            case 660249707:
                if (key.equals("desktop_mode_exit_incall")) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case 770362320:
                if (key.equals("desktop_mode_exit_exclusive_keyboard")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 781908055:
                if (key.equals("window_cast_mode_reminder_secure_string")) {
                    c = '7';
                    break;
                }
                c = 65535;
                break;
            case 830598563:
                if (key.equals("pc_vassist_doc_not_found")) {
                    c = 'a';
                    break;
                }
                c = 65535;
                break;
            case 864013407:
                if (key.equals("bluetooth_notify_cancel")) {
                    c = 'E';
                    break;
                }
                c = 65535;
                break;
            case 888391260:
                if (key.equals("pc_notification_touchpad_icon")) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case 932889448:
                if (key.equals("bluetooth_notify_enable")) {
                    c = 'F';
                    break;
                }
                c = 65535;
                break;
            case 946437665:
                if (key.equals("ic_safe_window_cast_mode")) {
                    c = '<';
                    break;
                }
                c = 65535;
                break;
            case 994175434:
                if (key.equals("pc_vassist_close_phone_app_for_start")) {
                    c = 'K';
                    break;
                }
                c = 65535;
                break;
            case 1021766833:
                if (key.equals("pc_notification_disconnect_action")) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case 1098956446:
                if (key.equals("popupwindow_networking_prompt_001")) {
                    c = 'k';
                    break;
                }
                c = 65535;
                break;
            case 1142607164:
                if (key.equals("pc_vassist_play_ppt1")) {
                    c = '^';
                    break;
                }
                c = 65535;
                break;
            case 1163109753:
                if (key.equals("desktop_mode_enter_content")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1181035251:
                if (key.equals("bluetooth_notify_enable_bluetooth_tip")) {
                    c = 'B';
                    break;
                }
                c = 65535;
                break;
            case 1294737223:
                if (key.equals("restore_current_window")) {
                    c = '2';
                    break;
                }
                c = 65535;
                break;
            case 1309761656:
                if (key.equals("pc_notification_big")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1323132855:
                if (key.equals("pc_notification")) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case 1371412437:
                if (key.equals("pc_vassist_close_succ")) {
                    c = 'O';
                    break;
                }
                c = 65535;
                break;
            case 1410047354:
                if (key.equals("desktop_mode_enter_cancel")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1434295134:
                if (key.equals("notification_pc_desktop_mode")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1502507059:
                if (key.equals("notification_btn_disconnect")) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case 1502878513:
                if (key.equals("pc_vassist_conn_external_display_tip")) {
                    c = 'S';
                    break;
                }
                c = 65535;
                break;
            case 1593498627:
                if (key.equals("desktop_mode_enter_incall")) {
                    c = '-';
                    break;
                }
                c = 65535;
                break;
            case 1672527292:
                if (key.equals("image_pc_pad_setting")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1748784758:
                if (key.equals("hw_hicar_dock_width")) {
                    c = 'n';
                    break;
                }
                c = 65535;
                break;
            case 1781444149:
                if (key.equals("hw_decor_caption_title")) {
                    c = '3';
                    break;
                }
                c = 65535;
                break;
            case 1813808586:
                if (key.equals("desktop_mode_enter_exclusive_keyboard2")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1859822507:
                if (key.equals("pc_vassist_start_succ")) {
                    c = 'M';
                    break;
                }
                c = 65535;
                break;
            case 1949948977:
                if (key.equals("pc_vassist_app_not_operation")) {
                    c = '`';
                    break;
                }
                c = 65535;
                break;
            case 1952462045:
                if (key.equals("welink_device_name")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 2016650734:
                if (key.equals("bluetooth_notify_disable_bluetooth_tip")) {
                    c = '@';
                    break;
                }
                c = 65535;
                break;
            case 2088208924:
                if (key.equals("pc_notification_switch_icon")) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case 2104678061:
                if (key.equals("switch_desktop_mode_image")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 2135703971:
                if (key.equals("bluetooth_notify_disable")) {
                    c = 'D';
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
                return 33686087;
            case 1:
                return 33686088;
            case 2:
                return 33686086;
            case 3:
                return 34013436;
            case 4:
                return 34603510;
            case 5:
                return 34603509;
            case 6:
                return 34603511;
            case 7:
                return 33685625;
            case '\b':
                return 33752027;
            case '\t':
                return 33685623;
            case '\n':
                return 33685627;
            case 11:
                return 33685633;
            case '\f':
                return 33685628;
            case '\r':
                return 33685622;
            case 14:
                return 33685634;
            case 15:
                return 33685649;
            case 16:
                return 33685624;
            case 17:
                return 33685641;
            case 18:
                return 33685642;
            case 19:
                return 33685640;
            case 20:
                return 33685645;
            case 21:
                return 33685644;
            case 22:
                return 33685636;
            case 23:
                return 33751738;
            case 24:
                return 33685941;
            case 25:
                return 33686281;
            case 26:
                return 34013423;
            case 27:
                return 34603442;
            case 28:
                return 34603440;
            case 29:
                return 34603444;
            case 30:
                return 33686083;
            case 31:
                return 34013422;
            case ' ':
                return 34603441;
            case '!':
                return 34603437;
            case '\"':
                return 33686074;
            case '#':
                return 34603445;
            case '$':
                return 34603438;
            case '%':
                return 34603443;
            case '&':
                return 34603439;
            case '\'':
                return 34603436;
            case '(':
                return 33686075;
            case ')':
                return 33686073;
            case '*':
                return 33752012;
            case '+':
                return 33752010;
            case ',':
                return 33685643;
            case '-':
                return 33685626;
            case '.':
                return 33686096;
            case '/':
                return 33686097;
            case '0':
                return 34603261;
            case '1':
                return 34603260;
            case '2':
                return 33686198;
            case '3':
                return 33751782;
            case '4':
                return 33751913;
            case '5':
                return 33882959;
            case IDisplayEngineService.DE_ACTION_MOTION_SWAP /* 54 */:
                return 34013464;
            case IDisplayEngineService.DE_ACTION_MAX /* 55 */:
                return 33686300;
            case '8':
                return 34603463;
            case '9':
                return 34603464;
            case ':':
                return 33751993;
            case ';':
                return 33686299;
            case '<':
                return 33752019;
            case '=':
                return 34013231;
            case '>':
                return 34603425;
            case '?':
                return 34603429;
            case '@':
                return 33685558;
            case HwLogExceptionInner.LEVEL_A /* 65 */:
                return 33685554;
            case HwLogExceptionInner.LEVEL_B /* 66 */:
                return 33685560;
            case HwLogExceptionInner.LEVEL_C /* 67 */:
                return 33685555;
            case HwLogExceptionInner.LEVEL_D /* 68 */:
                return 33685556;
            case 'E':
                return 33685552;
            case 'F':
                return 33685559;
            case 'G':
                return 33686129;
            case 'H':
                return 33686130;
            case 'I':
                return 33686115;
            case 'J':
                return 33686105;
            case TpCommandConstant.VOLUME_FLICK_THRESHOLD_MIN /* 75 */:
                return 33686110;
            case 'L':
                return 33686108;
            case 'M':
                return 33686132;
            case 'N':
                return 33686131;
            case 'O':
                return 33686111;
            case 'P':
                return 33686109;
            case 'Q':
                return 33686121;
            case 'R':
                return 33686120;
            case 'S':
                return 33686112;
            case 'T':
                return 33686113;
            case 'U':
                return 33686125;
            case 'V':
                return 33686126;
            case 'W':
                return 33686127;
            case 'X':
                return 33686128;
            case 'Y':
                return 33686116;
            case 'Z':
                return 33686117;
            case '[':
                return 33686118;
            case '\\':
                return 33686119;
            case ']':
                return 33686123;
            case '^':
                return 33686124;
            case '_':
                return 33686103;
            case '`':
                return 33686104;
            case 'a':
                return 33686114;
            case 'b':
                return 33686107;
            case 'c':
                return 33686122;
            case BastetParameters.HONGBAO_SPEEDUP_START /* 100 */:
                return 34013421;
            case BastetParameters.HONGBAO_SPEEDUP_STOP /* 101 */:
                return 33686101;
            case WindowConfigurationEx.HW_MULTI_WINDOWING_MODE_FREEFORM /* 102 */:
                return 33686099;
            case WindowConfigurationEx.HW_MULTI_WINDOWING_MODE_MAGIC /* 103 */:
                return 34603433;
            case 'h':
                return 34603432;
            case 'i':
                return 33686100;
            case 'j':
                return 33686098;
            case 'k':
                return 33686184;
            case 'l':
                return 34472523;
            case 'm':
                return 34472521;
            case 'n':
                return 34472522;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForMmiCode(String key) {
        char c;
        switch (key.hashCode()) {
            case -1904311020:
                if (key.equals("PinMmi")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1891679636:
                if (key.equals("PwdMmi")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1769430727:
                if (key.equals("ClipMmi")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1769371145:
                if (key.equals("ClirMmi")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1680772711:
                if (key.equals("ColpMmi")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1680713129:
                if (key.equals("ColrMmi")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1452106934:
                if (key.equals("passwordIncorrect")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1396669616:
                if (key.equals("badPin")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1300233866:
                if (key.equals("serviceNotProvisioned")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -967589602:
                if (key.equals("CLIRDefaultOnNextCallOn")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -818347635:
                if (key.equals("CfNrcMmi")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -817692233:
                if (key.equals("CfNryMmi")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -304897257:
                if (key.equals("serviceRegistered")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 63919594:
                if (key.equals("BaMmi")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 64992070:
                if (key.equals("CfMmi")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 65498517:
                if (key.equals("CwMmi")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 69493264:
                if (key.equals("CLIRDefaultOnNextCallOff")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 152008060:
                if (key.equals("CLIRPermanent")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 646862540:
                if (key.equals("serviceEnabled")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 812622654:
                if (key.equals("CLIRDefaultOffNextCallOff")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1139780839:
                if (key.equals("mismatchPin")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1411686960:
                if (key.equals("CLIRDefaultOffNextCallOn")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 1501618257:
                if (key.equals("serviceDisabled")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1825692051:
                if (key.equals("serviceErased")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 2014711999:
                if (key.equals("mmiError")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2015349258:
                if (key.equals("CfbMmi")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 2015915287:
                if (key.equals("CfuMmi")) {
                    c = 21;
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
                return 17039467;
            case 1:
                return 17039702;
            case 2:
                return 17040608;
            case 3:
                return 17040618;
            case 4:
                return 17039397;
            case 5:
                return 17039408;
            case 6:
                return 17039409;
            case 7:
                return 17039468;
            case '\b':
                return 17039413;
            case '\t':
                return 17039415;
            case '\n':
                return 17039414;
            case 11:
                return 17041206;
            case '\f':
                return 17041204;
            case '\r':
                return 17040719;
            case 14:
                return 17041205;
            case 15:
                return 17039402;
            case 16:
                return 17039401;
            case 17:
                return 17039400;
            case 18:
                return 17039398;
            case 19:
                return 17039399;
            case 20:
                return 17039403;
            case 21:
                return 17039407;
            case 22:
                return 17039406;
            case 23:
                return 17039405;
            case 24:
                return 17039404;
            case 25:
                return 17041202;
            case 26:
                return 17041201;
            default:
                return -1;
        }
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
                    return 34013347;
                }
                if (!z) {
                    return -1;
                }
                return 34603238;
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
                return 34013416;
            case 6:
                return 33685978;
            case 7:
                return 34013417;
            case '\b':
                return 33686009;
            case '\t':
                return 34603521;
            case '\n':
                return 34603525;
            case 11:
                return 34603526;
            case '\f':
                return 34603522;
            case '\r':
                return 34603523;
            case 14:
                return 34603520;
            case 15:
                return 34078724;
            case 16:
                return 34472582;
            default:
                return -1;
        }
    }
}
