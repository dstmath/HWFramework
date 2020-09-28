package com.huawei.utils;

import android.util.DoubleConsts;
import android.util.HwLogExceptionInner;
import com.huawei.android.bastet.BastetParameters;
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
        return value4;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForTelephony(String key) {
        char c;
        switch (key.hashCode()) {
            case -1994759852:
                if (key.equals("config_device_vt_available")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1367724422:
                if (key.equals("cancel")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1204717312:
                if (key.equals("gsm_alphabet_default_charset")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -952305534:
                if (key.equals("config_device_volte_available")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -770819464:
                if (key.equals("config_sms_enabled_single_shift_tables")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -523179886:
                if (key.equals("Theme_Emui_Dialog_Alert")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 325872594:
                if (key.equals("data_enable_confirm_title")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 661784443:
                if (key.equals("data_enable_confirm_msg")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1484367234:
                if (key.equals("config_device_wfc_ims_available")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1993891474:
                if (key.equals("data_enable_confirm_msg_vowifi")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2099983851:
                if (key.equals("config_sms_enabled_locking_shift_tables")) {
                    c = '\n';
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
                return 33947691;
            case 1:
                return 33685612;
            case 2:
                return 33685608;
            case 3:
                return 33685611;
            case 4:
                return 17039360;
            case 5:
                return 17040236;
            case 6:
                return 17891405;
            case 7:
                return 17891404;
            case '\b':
                return 17891403;
            case '\t':
                return 17236062;
            case '\n':
                return 17236061;
            default:
                return -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getResourceIdForBasicPlatform(String key) {
        boolean z;
        switch (key.hashCode()) {
            case -1694973564:
                if (key.equals("default_wallpaper")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1364921414:
                if (key.equals("status_bar_height_portrait")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -611229376:
                if (key.equals("status_bar_height")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 278039962:
                if (key.equals("navigation_bar_height_landscape")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 694738398:
                if (key.equals("navigation_bar_height")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 935313020:
                if (key.equals("status_bar_height_landscape")) {
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
            return 17105443;
        }
        if (z) {
            return 17105445;
        }
        if (z) {
            return 17105444;
        }
        if (z) {
            return 17105305;
        }
        if (z) {
            return 17105307;
        }
        if (!z) {
            return -1;
        }
        return 17302139;
    }

    private static int getResourceIdForSecurity(String key) {
        if ((key.hashCode() == -20106182 && key.equals("config_keyguardComponent")) ? false : true) {
            return -1;
        }
        return 17039866;
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
                    c = 'l';
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
                    c = 'k';
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
                    c = 'm';
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
                return 33686069;
            case 1:
                return 33686070;
            case 2:
                return 33686068;
            case 3:
                return 34013435;
            case 4:
                return 34603490;
            case 5:
                return 34603489;
            case 6:
                return 34603491;
            case 7:
                return 33685624;
            case '\b':
                return 33752008;
            case '\t':
                return 33685622;
            case '\n':
                return 33685626;
            case 11:
                return 33685628;
            case '\f':
                return 33685627;
            case '\r':
                return 33685621;
            case 14:
                return 33685633;
            case 15:
                return 33685645;
            case 16:
                return 33685623;
            case 17:
                return 33685640;
            case 18:
                return 33685641;
            case 19:
                return 33685636;
            case 20:
                return 33685644;
            case 21:
                return 33685643;
            case 22:
                return 33685634;
            case 23:
                return 33751738;
            case 24:
                return 33685941;
            case 25:
                return 33686258;
            case 26:
                return 34013422;
            case 27:
                return 34603418;
            case 28:
                return 34603416;
            case 29:
                return 34603420;
            case 30:
                return 33686060;
            case 31:
                return 34013421;
            case ' ':
                return 34603417;
            case '!':
                return 34603413;
            case '\"':
                return 33686050;
            case '#':
                return 34603421;
            case '$':
                return 34603414;
            case '%':
                return 34603419;
            case '&':
                return 34603415;
            case '\'':
                return 34603412;
            case '(':
                return 33686051;
            case ')':
                return 33686044;
            case '*':
                return 33751994;
            case '+':
                return 33751992;
            case ',':
                return 33685642;
            case '-':
                return 33685625;
            case '.':
                return 33686083;
            case '/':
                return 33686084;
            case '0':
                return 34603246;
            case '1':
                return 34603245;
            case '2':
                return 33686178;
            case '3':
                return 33751782;
            case '4':
                return 33751904;
            case DoubleConsts.SIGNIFICAND_WIDTH:
                return 33882848;
            case '6':
                return 34013461;
            case '7':
                return 33686277;
            case '8':
                return 34603437;
            case '9':
                return 34603438;
            case ':':
                return 33751977;
            case ';':
                return 33686276;
            case '<':
                return 33752001;
            case '=':
                return 34013231;
            case '>':
                return 34603401;
            case '?':
                return 34603404;
            case '@':
                return 33685557;
            case HwLogExceptionInner.LEVEL_A:
                return 33685552;
            case HwLogExceptionInner.LEVEL_B:
                return 33685559;
            case HwLogExceptionInner.LEVEL_C:
                return 33685554;
            case HwLogExceptionInner.LEVEL_D:
                return 33685555;
            case 'E':
                return 33685550;
            case 'F':
                return 33685558;
            case 'G':
                return 33686116;
            case 'H':
                return 33686117;
            case 'I':
                return 33686102;
            case 'J':
                return 33686092;
            case TpCommandConstant.VOLUME_FLICK_THRESHOLD_MIN:
                return 33686097;
            case 'L':
                return 33686095;
            case 'M':
                return 33686119;
            case 'N':
                return 33686118;
            case 'O':
                return 33686098;
            case 'P':
                return 33686096;
            case 'Q':
                return 33686108;
            case 'R':
                return 33686107;
            case 'S':
                return 33686099;
            case 'T':
                return 33686100;
            case 'U':
                return 33686112;
            case 'V':
                return 33686113;
            case 'W':
                return 33686114;
            case 'X':
                return 33686115;
            case 'Y':
                return 33686103;
            case 'Z':
                return 33686104;
            case '[':
                return 33686105;
            case '\\':
                return 33686106;
            case ']':
                return 33686110;
            case '^':
                return 33686111;
            case '_':
                return 33686090;
            case '`':
                return 33686091;
            case 'a':
                return 33686101;
            case 'b':
                return 33686094;
            case 'c':
                return 33686109;
            case 'd':
                return 34013420;
            case BastetParameters.HONGBAO_SPEEDUP_STOP:
                return 33686088;
            case 'f':
                return 33686086;
            case 'g':
                return 34603409;
            case 'h':
                return 34603408;
            case 'i':
                return 33686087;
            case 'j':
                return 33686085;
            case 'k':
                return 34472523;
            case 'l':
                return 34472521;
            case 'm':
                return 34472522;
            default:
                return -1;
        }
    }
}
