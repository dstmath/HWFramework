package com.huawei.android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Process;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class VibratorEx {
    public static final String HW_VIBRATOR_ABILITY_AV_SYNC = "haptic.audio_vibrate.sync";
    public static final String HW_VIBRATOR_ABILITY_SELF_AV_SYNC = "haptic.self_audio_vibrate.sync";
    public static final String HW_VIBRATOR_GRADE_VALUE = "haptic.grade.value";
    public static final String HW_VIBRATOR_TPYE_ALLSCREEN_UPGLIDE_MULTITASK = "haptic.allscreen.upglide_multitask";
    public static final String HW_VIBRATOR_TPYE_BATTERY_CHARGING = "haptic.battery.charging";
    public static final String HW_VIBRATOR_TPYE_CAMERA_CLICK = "haptic.camera.click";
    public static final String HW_VIBRATOR_TPYE_CAMERA_FOCUS = "haptic.camera.focus";
    public static final String HW_VIBRATOR_TPYE_CAMERA_GEAR_SLIP = "haptic.camera.gear_slip";
    public static final String HW_VIBRATOR_TPYE_CAMERA_MODE_SWITCH = "haptic.camera.mode_switch";
    public static final String HW_VIBRATOR_TPYE_CAMERA_PORTRAIT_SWITCH = "haptic.camera.portrait_switch";
    public static final String HW_VIBRATOR_TPYE_CLOCK_STOPWATCH = "haptic.clock.stopwatch";
    public static final String HW_VIBRATOR_TPYE_CLOCK_TIMER = "haptic.clock.timer";
    public static final String HW_VIBRATOR_TPYE_CONTACTS_LETTERS_INDEX = "haptic.contacts.letters_index";
    public static final String HW_VIBRATOR_TPYE_CONTACTS_LIST_SCROLL = "haptic.contacts.list_scroll";
    public static final String HW_VIBRATOR_TPYE_CONTROL_DATE_SCROLL = "haptic.control.date_scroll";
    public static final String HW_VIBRATOR_TPYE_CONTROL_EDIT_SLIP = "haptic.control.edit_slip";
    public static final String HW_VIBRATOR_TPYE_CONTROL_LETTERS_SCROLL = "haptic.control.letters_scroll";
    public static final String HW_VIBRATOR_TPYE_CONTROL_TIME_SCROLL = "haptic.control.time_scroll";
    public static final String HW_VIBRATOR_TPYE_DESKTOP_LONG_PRESS = "haptic.desktop.long_press";
    public static final String HW_VIBRATOR_TPYE_DIALLER_CLICK = "haptic.dialler.click";
    public static final String HW_VIBRATOR_TPYE_DIALLER_LONG_PRESS = "haptic.dialler.long_press";
    public static final String HW_VIBRATOR_TPYE_FINGERPRINT_ENTERING = "haptic.fingerprint.entering";
    public static final String HW_VIBRATOR_TPYE_FINGERPRINT_LIFT = "haptic.fingerprint.lift";
    public static final String HW_VIBRATOR_TPYE_FINGERPRINT_LONG_PRESS = "haptic.fingerprint.long_press";
    public static final String HW_VIBRATOR_TPYE_FINGERPRINT_UNLOCK_FAIL = "haptic.fingerprint.unlock_fail";
    public static final String HW_VIBRATOR_TPYE_GAME_SHOOTING = "haptic.game.shooting";
    public static final String HW_VIBRATOR_TPYE_HIVOICE_CLICK = "haptic.hivoice.click";
    public static final String HW_VIBRATOR_TPYE_NULL = "haptic.null";
    public static final String HW_VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_BACK = "haptic.virtual_navigation.click_back";
    public static final String HW_VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_HOME = "haptic.virtual_navigation.click_home";
    public static final String HW_VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_MULTITASK = "haptic.virtual_navigation.click_multitask";
    public static final String HW_VIBRATOR_TPYE_VIRTUALNAVIGATION_LONGPRESS_HOME = "haptic.virtual_navigation.long_press";
    public static final String HW_VIBRATOR_TPYE_WALLET_TIME_SCROLL = "haptic.wallet.time_scroll";
    public static final String HW_VIBRATOR_TYPE_ALARM_AEGEAN_SEA = "haptic.alarm.Aegean_Sea";
    public static final String HW_VIBRATOR_TYPE_ALARM_AWAKENING = "haptic.alarm.Awakening";
    public static final String HW_VIBRATOR_TYPE_ALARM_CREEK = "haptic.alarm.Creek";
    public static final String HW_VIBRATOR_TYPE_ALARM_CUCKOO = "haptic.alarm.Cuckoo";
    public static final String HW_VIBRATOR_TYPE_ALARM_DAWN = "haptic.alarm.Dawn";
    public static final String HW_VIBRATOR_TYPE_ALARM_FOREST_MELODY = "haptic.alarm.Forest_Melody";
    public static final String HW_VIBRATOR_TYPE_ALARM_FRESH_AIR = "haptic.alarm.Fresh_Air";
    public static final String HW_VIBRATOR_TYPE_ALARM_HAWAII = "haptic.alarm.Hawaii";
    public static final String HW_VIBRATOR_TYPE_ALARM_MOMENT = "haptic.alarm.Moment";
    public static final String HW_VIBRATOR_TYPE_ALARM_MORNING_LIGHT = "haptic.alarm.Morning_Light";
    public static final String HW_VIBRATOR_TYPE_ALARM_OCEAN_WHISPER = "haptic.alarm.Ocean_Whisper";
    public static final String HW_VIBRATOR_TYPE_ALARM_RAYS = "haptic.alarm.Rays";
    public static final String HW_VIBRATOR_TYPE_ALARM_RIPPLE = "haptic.alarm.Ripple";
    public static final String HW_VIBRATOR_TYPE_ALARM_SAKURA_DROP = "haptic.alarm.Sakura_Drop";
    public static final String HW_VIBRATOR_TYPE_ALARM_STAR = "haptic.alarm.Star";
    public static final String HW_VIBRATOR_TYPE_ALARM_TIMER_BEEP = "haptic.alarm.Timer_Beep";
    public static final String HW_VIBRATOR_TYPE_ALLSCREEN_SLIP_BACK = "haptic.allscreen.slip_back";
    public static final String HW_VIBRATOR_TYPE_CALCULATOR_DELETE_LONG_PRESS = "haptic.calculator.delete_long_press";
    public static final String HW_VIBRATOR_TYPE_CAMERA_CLICK_UP = "haptic.camera.click_up";
    public static final String HW_VIBRATOR_TYPE_CAMERA_LONG_PRESS = "haptic.camera.long_press";
    public static final String HW_VIBRATOR_TYPE_CHARGING = "haptic.common.charging";
    public static final String HW_VIBRATOR_TYPE_COMMON_BUTTON = "haptic.common.button";
    public static final String HW_VIBRATOR_TYPE_COMMON_CLICK = "haptic.common.click";
    public static final String HW_VIBRATOR_TYPE_COMMON_CLICKUP = "haptic.common.click_up";
    public static final String HW_VIBRATOR_TYPE_COMMON_DCLICK = "haptic.common.double_click";
    public static final String HW_VIBRATOR_TYPE_COMMON_DEL_LONG_PRESS = "haptic.common.delete_long_press";
    public static final String HW_VIBRATOR_TYPE_COMMON_FAIL_1 = "haptic.common.fail_pattern1";
    public static final String HW_VIBRATOR_TYPE_COMMON_FAIL_2 = "haptic.common.fail_pattern2";
    public static final String HW_VIBRATOR_TYPE_COMMON_LONG_PRESS = "haptic.common.long_press";
    public static final String HW_VIBRATOR_TYPE_COMMON_LONG_PRESS1 = "haptic.common.long_press1";
    public static final String HW_VIBRATOR_TYPE_COMMON_LONG_PRESS2 = "haptic.common.long_press2";
    public static final String HW_VIBRATOR_TYPE_COMMON_LONG_PRESS3 = "haptic.common.long_press3";
    public static final String HW_VIBRATOR_TYPE_COMMON_NOTICE = "haptic.common.notice";
    public static final String HW_VIBRATOR_TYPE_COMMON_PINCH = "haptic.common.pinch";
    public static final String HW_VIBRATOR_TYPE_COMMON_SUCCESS = "haptic.common.success";
    public static final String HW_VIBRATOR_TYPE_COMMON_SWITCH = "haptic.common.switch";
    public static final String HW_VIBRATOR_TYPE_COMMON_THRESHOLD = "haptic.common.threshold";
    public static final String HW_VIBRATOR_TYPE_COMMON_TICK = "haptic.common.tick";
    public static final String HW_VIBRATOR_TYPE_COMMON_UPGLIDE = "haptic.common.upglide";
    public static final String HW_VIBRATOR_TYPE_CONTROL_SEARCH_LONG_PRESS = "haptic.control.search_long_press";
    public static final String HW_VIBRATOR_TYPE_CONTROL_TEXT_CHOOSE_CURSOR_MOVE = "haptic.control.text_choose_cursor_move";
    public static final String HW_VIBRATOR_TYPE_CONTROL_TEXT_EDIT = "haptic.control.text_edit";
    public static final String HW_VIBRATOR_TYPE_CONTROL_WIDGET_OPERATION = "haptic.control.widget_operation";
    public static final String HW_VIBRATOR_TYPE_DESKTOP_PAGE_EDIT_CLICK = "haptic.desktop.page_edit_click";
    public static final String HW_VIBRATOR_TYPE_DESKTOP_PAGE_LONG_PRESS = "haptic.desktop.page_long_press";
    public static final String HW_VIBRATOR_TYPE_DESKTOP_WIDGET_LONG_PRESS = "haptic.desktop.widget_long_press";
    public static final String HW_VIBRATOR_TYPE_DIALLER_DELETE_LONG_PRESS = "haptic.dialler.delete_long_press";
    public static final String HW_VIBRATOR_TYPE_FINGERPRINT_IN_LONG_PRESS = "haptic.fingerprint.input_long_press";
    public static final String HW_VIBRATOR_TYPE_FLOAT_TASKS_LONGPRESS = "haptic.virtual_float_tasks.long_press";
    public static final String HW_VIBRATOR_TYPE_GALLERY_ALBUMS_LONG_PRESS = "haptic.gallery.albums_long_press";
    public static final String HW_VIBRATOR_TYPE_GALLERY_PHOTOS_LONG_PRESS = "haptic.gallery.photos_long_press";
    public static final String HW_VIBRATOR_TYPE_GALLERY_UPGLIDE_RELATED = "haptic.gallery.upglide_related";
    public static final String HW_VIBRATOR_TYPE_GRADE_STRENGTH1 = "haptic.grade.strength1";
    public static final String HW_VIBRATOR_TYPE_GRADE_STRENGTH2 = "haptic.grade.strength2";
    public static final String HW_VIBRATOR_TYPE_GRADE_STRENGTH3 = "haptic.grade.strength3";
    public static final String HW_VIBRATOR_TYPE_GRADE_STRENGTH4 = "haptic.grade.strength4";
    public static final String HW_VIBRATOR_TYPE_GRADE_STRENGTH5 = "haptic.grade.strength5";
    public static final String HW_VIBRATOR_TYPE_INPUT_VOICE_END = "haptic.input.voice_end";
    public static final String HW_VIBRATOR_TYPE_INPUT_VOICE_ENTER = "haptic.input.voice_enter";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_FACE_UNLOCK_FAIL = "haptic.lockscreen.face_unlock_fail";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_FACE_UNLOCK_RETRY = "haptic.lockscreen.face_unlock_retry";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_NUMBER_UNLOCK_FAIL = "haptic.lockscreen.number_unlock_fail";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_ONEHAND_KEYBOARD_SWITCH = "haptic.lockscreen.onehand_keyboard_switch";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_POWER_LONG_PRESS = "haptic.lockscreen.power_long_press";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_UNLOCK_CLICK = "haptic.lockscreen.unlock_click";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_UNLOCK_SLIP = "haptic.lockscreen.unlock_slip";
    public static final String HW_VIBRATOR_TYPE_LOCKSCREEN_UPGLIDE_SWITCHES = "haptic.lockscreen.upglide_switches";
    public static final String HW_VIBRATOR_TYPE_MODE_CHANGE = "haptic.mode.change";
    public static final String HW_VIBRATOR_TYPE_NOTEPAD_NOTE_LONG_PRESS = "haptic.notepad.note_long_press";
    public static final String HW_VIBRATOR_TYPE_NOTICE_ARROW = "haptic.notice.Arrow";
    public static final String HW_VIBRATOR_TYPE_NOTICE_BELL = "haptic.notice.Bell";
    public static final String HW_VIBRATOR_TYPE_NOTICE_BONGO = "haptic.notice.Bongo";
    public static final String HW_VIBRATOR_TYPE_NOTICE_CAR_LOCK = "haptic.notice.Car_Lock";
    public static final String HW_VIBRATOR_TYPE_NOTICE_CAVE = "haptic.notice.Cave";
    public static final String HW_VIBRATOR_TYPE_NOTICE_CHESS = "haptic.notice.Chess";
    public static final String HW_VIBRATOR_TYPE_NOTICE_CRYSTAL_DROP = "haptic.notice.Crystal_Drop";
    public static final String HW_VIBRATOR_TYPE_NOTICE_CUCKOO = "haptic.notice.Cuckoo";
    public static final String HW_VIBRATOR_TYPE_NOTICE_DOORBELL = "haptic.notice.Doorbell";
    public static final String HW_VIBRATOR_TYPE_NOTICE_DRIP = "haptic.notice.Drip";
    public static final String HW_VIBRATOR_TYPE_NOTICE_ECHO = "haptic.notice.Echo";
    public static final String HW_VIBRATOR_TYPE_NOTICE_EMERGING = "haptic.notice.Emerging";
    public static final String HW_VIBRATOR_TYPE_NOTICE_FOUNTAIN = "haptic.notice.Fountain";
    public static final String HW_VIBRATOR_TYPE_NOTICE_HAND_DRUM = "haptic.notice.Hand_Drum";
    public static final String HW_VIBRATOR_TYPE_NOTICE_HUAWEI_CASCADE = "haptic.notice.Huawei_Cascade";
    public static final String HW_VIBRATOR_TYPE_NOTICE_JOYFUL = "haptic.notice.Joyful";
    public static final String HW_VIBRATOR_TYPE_NOTICE_JUMP = "haptic.notice.Jump";
    public static final String HW_VIBRATOR_TYPE_NOTICE_LEAF = "haptic.notice.Leaf";
    public static final String HW_VIBRATOR_TYPE_NOTICE_LETTER = "haptic.notice.Letter";
    public static final String HW_VIBRATOR_TYPE_NOTICE_LIT = "haptic.notice.Lit";
    public static final String HW_VIBRATOR_TYPE_NOTICE_LITTLE_WISH = "haptic.notice.Little_Wish";
    public static final String HW_VIBRATOR_TYPE_NOTICE_MICROWAVE_OVEN = "haptic.notice.Microwave_Oven";
    public static final String HW_VIBRATOR_TYPE_NOTICE_OLD_BICYCLE = "haptic.notice.Old_Bicycle";
    public static final String HW_VIBRATOR_TYPE_NOTICE_PEKING_OPERA_DRUM = "haptic.notice.Peking_Opera_Drum";
    public static final String HW_VIBRATOR_TYPE_NOTICE_PINGPONG = "haptic.notice.PingPong";
    public static final String HW_VIBRATOR_TYPE_NOTICE_PIXIES = "haptic.notice.Pixies";
    public static final String HW_VIBRATOR_TYPE_NOTICE_PLAY = "haptic.notice.Play";
    public static final String HW_VIBRATOR_TYPE_NOTICE_STEP = "haptic.notice.Step";
    public static final String HW_VIBRATOR_TYPE_NOTICE_TWINKLE = "haptic.notice.Twinkle";
    public static final String HW_VIBRATOR_TYPE_NOTICE_WATERFLOW = "haptic.notice.Waterflow";
    public static final String HW_VIBRATOR_TYPE_NOTICE_WHISTLE = "haptic.notice.Whistle";
    public static final String HW_VIBRATOR_TYPE_NOTICE_ZEN = "haptic.notice.Zen";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE1 = "haptic.pattern.type1";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE10 = "haptic.pattern.type10";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE11 = "haptic.pattern.type11";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE12 = "haptic.pattern.type12";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE2 = "haptic.pattern.type2";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE3 = "haptic.pattern.type3";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE4 = "haptic.pattern.type4";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE5 = "haptic.pattern.type5";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE6 = "haptic.pattern.type6";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE7 = "haptic.pattern.type7";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE8 = "haptic.pattern.type8";
    public static final String HW_VIBRATOR_TYPE_PATTERN_TYPE9 = "haptic.pattern.type9";
    public static final String HW_VIBRATOR_TYPE_POWEROFF = "haptic.poweroff";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_BOUNCE = "haptic.ringtone.Bounce";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CARTOON = "haptic.ringtone.Cartoon";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CHILLED = "haptic.ringtone.Chilled";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CLASSIC_BELL = "haptic.ringtone.Classic_Bell";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CONCENTRATE = "haptic.ringtone.Concentrate";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DAY_LILY = "haptic.ringtone.Day_lily";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DIGITAL_RINGTONE = "haptic.ringtone.Digital_Ringtone";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DREAM = "haptic.ringtone.Dream";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DREAM_IT_POSSIBLE = "haptic.ringtone.Dream_It_Possible";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DYNAMO = "haptic.ringtone.Dynamo";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FLIPPED = "haptic.ringtone.Flipped";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FOREST_DAY = "haptic.ringtone.Forest_Day";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FREE = "haptic.ringtone.Free";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HALO = "haptic.ringtone.Halo";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HARP = "haptic.ringtone.Harp";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HELLO_YA = "haptic.ringtone.Hello_Ya";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_CLEAN = "haptic.ringtone.Huawei_Tune_Clean";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_LIVING = "haptic.ringtone.Huawei_Tune_Living";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_ORCHESTRAL = "haptic.ringtone.Huawei_Tune_Orchestral";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_MENUET = "haptic.ringtone.Menuet";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NEON = "haptic.ringtone.Neon";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NOTES = "haptic.ringtone.Notes";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_1 = "haptic.ringtone.optional1";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_2 = "haptic.ringtone.optional2";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_3 = "haptic.ringtone.optional3";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_4 = "haptic.ringtone.optional4";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_5 = "haptic.ringtone.optional5";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PULSE = "haptic.ringtone.Pulse";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SAILING = "haptic.ringtone.Sailing";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SAX = "haptic.ringtone.Sax";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SPIN = "haptic.ringtone.Spin";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WESTLAKE = "haptic.ringtone.Westlake";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WHISTLE = "haptic.ringtone.Whistle";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE1 = "haptic.slide.type1";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE2 = "haptic.slide.type2";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE3 = "haptic.slide.type3";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE4 = "haptic.slide.type4";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE5 = "haptic.slide.type5";
    public static final String HW_VIBRATOR_TYPE_SLIDE_TYPE6 = "haptic.slide.type6";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_EXPAND = "haptic.systemui.notifications_expand";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_LONG_PRESS = "haptic.systemui.notifications_long_press";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_MOVE = "haptic.systemui.notifications_move";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_SCREEN_RECORD_STOP = "haptic.systemui.screen_record_stop";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_SWITCHES_LONG_PRESS = "haptic.systemui.switches_long_press";
    public static final String HW_VIBRATOR_TYPE_SYSTEMUI_SWITCHES_SORT_LONG_PRESS = "haptic.systemui.switches_sort_long_press";
    public static final String HW_VIBRATOR_TYPE_VIRTUALNAVIGATION_CLICK_UP = "haptic.virtual_navigation.click_up";
    public static final String HW_VIBRATOR_TYPE_VIRTUALNAVIGATION_SINGLE_CLICK = "haptic.virtual_navigation_single.click";
    public static final String HW_VIBRATOR_TYPE_VIRTUALNAVIGATION_SINGLE_CLICK_UP = "haptic.virtual_navigation_single.click_up";
    public static final String HW_VIBRATOR_TYPE_VIRTUALNAVIGATION_SINGLE_LONGPRESS = "haptic.virtual_navigation_single.long_press";
    public static final String HW_VIBRATOR_TYPE_VOLUME_CHANGE = "haptic.volume.change";
    public static final String HW_VIBRATOR_TYPE_VOLUME_MAX = "haptic.volume.max";
    public static final String HW_VIBRATOR_TYPE_VOLUME_MAX_MIN = "haptic.volume.maxmin";
    public static final String HW_VIBRATOR_TYPE_VOLUME_MIN = "haptic.volume.min";
    public static final String HW_VIBRATOR_TYPE_VOLUME_TRIGGER = "haptic.volume.trigger";
    public static final String HW_VIBRATOR_TYPE_WEATHER_RAIN = "haptic.weather.rain";
    public static final String HW_VIBRATOR_TYPE_WEATHER_THUNDER = "haptic.weather.thunder";
    private static final String REPLY_EMPTY_VIBRATOR_TYPE = "can not set an empty vibrator type";
    private static final String TAG = "VibratorEx";
    private final String mPackageName;

    public VibratorEx() {
        this.mPackageName = ActivityThread.currentPackageName();
    }

    protected VibratorEx(Context context) {
        if (context != null) {
            this.mPackageName = context.getOpPackageName();
        } else {
            this.mPackageName = null;
        }
    }

    public boolean isSupportHwVibrator(String type) {
        if (type != null && !"".equals(type)) {
            return HwVibrator.isSupportHwVibrator(type);
        }
        Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        return false;
    }

    public void setHwVibrator(String type) {
        if (type == null || "".equals(type)) {
            Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        } else {
            HwVibrator.setHwVibrator(Process.myUid(), this.mPackageName, type);
        }
    }

    public void setHwVibrator(String type, int delay) {
        if (type == null || "".equals(type)) {
            Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        } else {
            HwVibrator.setHwVibrator(Process.myUid(), this.mPackageName, type, delay);
        }
    }

    public void setHwVibratorRepeat(String type, int repeat) {
        if (type == null || "".equals(type)) {
            Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        } else {
            HwVibrator.setHwVibratorRepeat(Process.myUid(), this.mPackageName, type, repeat);
        }
    }

    public void setHwAmplitude(String type, int amplitude) {
        if (type == null || "".equals(type)) {
            Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        } else {
            HwVibrator.setHwAmplitude(Process.myUid(), this.mPackageName, type, amplitude);
        }
    }

    public void stopHwVibrator(String type) {
        if (type == null || "".equals(type)) {
            Log.w(TAG, REPLY_EMPTY_VIBRATOR_TYPE);
        } else {
            HwVibrator.stopHwVibrator(Process.myUid(), this.mPackageName, type);
        }
    }

    public void setHwParameter(String command) {
        if (command == null || "".equals(command)) {
            Log.w(TAG, "can not set an empty vibrator command");
        } else {
            HwVibrator.setHwParameter(command);
        }
    }

    public String getHwParameter(String command) {
        if (command != null && !"".equals(command)) {
            return HwVibrator.getHwParameter(command);
        }
        Log.w(TAG, "can not set an empty vibrator command");
        return null;
    }

    @HwSystemApi
    public static void vibrate(Vibrator vibrator, long milliseconds) {
        if (vibrator instanceof SystemVibrator) {
            ((SystemVibrator) vibrator).vibrate(milliseconds);
        }
    }
}
