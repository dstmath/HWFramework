package com.huawei.android.os;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IVibratorService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.os.IHwVibrator;

public class HwVibrator {
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
    public static final String HW_VIBRATOR_TYPE_RINGTONE_ALMIDI = "haptic.ringtone.Institucionalmidi";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_AMUSEMENT_PARK = "haptic.ringtone.Amusement_Park";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_ANOTHER_VAN = "haptic.ringtone.Another_Van";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_ATT = "haptic.ringtone.Ringtone_ATT";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_BOUNCE = "haptic.ringtone.Bounce";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_BRAVE = "haptic.ringtone.Brave";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_BREATHE_FREELY = "haptic.ringtone.Breathe_Freely";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CARTOON = "haptic.ringtone.Cartoon";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CHILLED = "haptic.ringtone.Chilled";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CLASSIC = "haptic.ringtone.Classic";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CLASSIC_BELL = "haptic.ringtone.Classic_Bell";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_CONCENTRATE = "haptic.ringtone.Concentrate";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DANCE = "haptic.ringtone.Dance";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DAY_LILY = "haptic.ringtone.Day_lily";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DIGITAL_RINGTONE = "haptic.ringtone.Digital_Ringtone";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DREAM = "haptic.ringtone.Dream";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DREAM_IT_POSSIBLE = "haptic.ringtone.Dream_It_Possible";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DXWONEK = "haptic.ringtone.T-Mobile_dzwonek";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_DYNAMO = "haptic.ringtone.Dynamo";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_ENTEL_TT = "haptic.ringtone.entel123_tt";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_EYE = "haptic.ringtone.Eye";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FANTASY_WORLD = "haptic.ringtone.Fantasy_World";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FIREFLY = "haptic.ringtone.ATT_FIREFLY_2016_ATT";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FLIPPED = "haptic.ringtone.Flipped";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FLOW = "haptic.ringtone.Flow";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FOREST_DAY = "haptic.ringtone.Forest_Day";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FREE = "haptic.ringtone.Free";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_FUTURE = "haptic.ringtone.Future";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_GAMELAN_MUSIC = "haptic.ringtone.Gamelan_Music";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HALO = "haptic.ringtone.Halo";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HARP = "haptic.ringtone.Harp";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HEART = "haptic.ringtone.Heart";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HELLO_YA = "haptic.ringtone.Hello_Ya";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_CLEAN = "haptic.ringtone.Huawei_Tune_Clean";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_LIVING = "haptic.ringtone.Huawei_Tune_Living";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_HUAWEI_TUNE_ORCHESTRAL = "haptic.ringtone.Huawei_Tune_Orchestral";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_JAZZ = "haptic.ringtone.Jazz";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_KOLBI = "haptic.ringtone.Kolbi";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_LITTLE_EMOTION = "haptic.ringtone.Little_Emotion";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_MENUET = "haptic.ringtone.Menuet";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NEON = "haptic.ringtone.Neon";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NEVER_ATT = "haptic.ringtone.Now_or_Never_ATT";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NOTES = "haptic.ringtone.Notes";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_NOVA_SONG = "haptic.ringtone.Nova_Song";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_1 = "haptic.ringtone.optional1";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_2 = "haptic.ringtone.optional2";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_3 = "haptic.ringtone.optional3";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_4 = "haptic.ringtone.optional4";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_OPTIONAL_5 = "haptic.ringtone.optional5";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PLAY = "haptic.ringtone.PLAY";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_POLONAISE = "haptic.ringtone.Polonaise";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PORSCHE_DESIGN_TITANIUM = "haptic.ringtone.Porsche_Design_Titanium";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PROXIMUS_CLASSIC = "haptic.ringtone.Proximus_Ringtone_Classic";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PROXIMUS_DANCE = "haptic.ringtone.Proximus_Ringtone_Dance";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PROXIMUS_JAZZ = "haptic.ringtone.Proximus_Ringtone_Jazz";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PSYCHEDELIC = "haptic.ringtone.Psychedelic";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_PULSE = "haptic.ringtone.Pulse";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_RIPPLE = "haptic.ringtone.Ripple";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_RUBRICA = "haptic.ringtone.Institucional_Rubrica";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SAILING = "haptic.ringtone.Sailing";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SAX = "haptic.ringtone.Sax";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SMILE = "haptic.ringtone.Smile";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SPIN = "haptic.ringtone.Spin";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SUITE = "haptic.ringtone.Suite";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SUMMER_AFTERNOON = "haptic.ringtone.Summer_Afternoon";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SUNLIT_GARDEN = "haptic.ringtone.Sunlit_Garden";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SURGING_POWER = "haptic.ringtone.Surging_Power";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_SWING = "haptic.ringtone.Swing";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_TELEKOM = "haptic.ringtone.Telekom_Ring";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_TMOBILE = "haptic.ringtone.T-Mobile_Ring";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_UN = "haptic.ringtone.Ringtone_UN";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WAVE = "haptic.ringtone.Wave";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WESTLAKE = "haptic.ringtone.Westlake";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WE_ARE_THE_BRAVE = "haptic.ringtone.We_Are_The_Brave";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WHISTLE = "haptic.ringtone.Whistle";
    public static final String HW_VIBRATOR_TYPE_RINGTONE_WIND_DANCE = "haptic.ringtone.Wind_Dance";
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
    private static final Singleton<IHwVibrator> IVibratorSingleton = new Singleton<IHwVibrator>() {
        /* class com.huawei.android.os.HwVibrator.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwVibrator create() {
            try {
                IVibratorService vibratorService = IVibratorService.Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
                if (vibratorService != null) {
                    return IHwVibrator.Stub.asInterface(vibratorService.getHwInnerService());
                }
                Log.e(HwVibrator.TAG, "failed to connect VibratorService!");
                return null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwVibrator";
    private static final Binder TOKEN = new Binder();

    public static IHwVibrator getService() {
        return IVibratorSingleton.get();
    }

    public static boolean isSupportHwVibrator(String type) {
        try {
            return getService().isSupportHwVibrator(type);
        } catch (RemoteException e) {
            Log.e(TAG, "isSupportHwVibrator fail with RemoteException");
            return false;
        }
    }

    public static void setHwVibrator(int pid, String opPkg, String type) {
        try {
            getService().setHwVibrator(pid, opPkg, TOKEN, type);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwVibrator fail with RemoteException");
        }
    }

    public static void setHwVibrator(int pid, String opPkg, IBinder token, String type, int delay) {
        try {
            getService().setHwVibratorDelay(pid, opPkg, token, type, delay);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwVibrator fail with RemoteException");
        }
    }

    public static void setHwVibrator(int pid, String opPkg, String type, int delay) {
        try {
            getService().setHwVibratorDelay(pid, opPkg, TOKEN, type, delay);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwVibrator fail with RemoteException");
        }
    }

    public static void setHwVibratorRepeat(int pid, String opPkg, IBinder token, String type, int repeat) {
        try {
            getService().setHwVibratorRepeat(pid, opPkg, token, type, repeat);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwVibrator fail with RemoteException");
        }
    }

    public static void setHwVibratorRepeat(int pid, String opPkg, String type, int repeat) {
        try {
            getService().setHwVibratorRepeat(pid, opPkg, TOKEN, type, repeat);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwVibrator fail with RemoteException");
        }
    }

    public static void setHwAmplitude(int pid, String opPkg, String type, int amplitude) {
        try {
            getService().setHwAmplitude(pid, opPkg, TOKEN, type, amplitude);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwAmplitude fail with RemoteException");
        }
    }

    public static void stopHwVibrator(int pid, String opPkg, IBinder token, String type) {
        try {
            getService().stopHwVibrator(pid, opPkg, token, type);
        } catch (RemoteException e) {
            Log.e(TAG, "stopHwVibrator fail with RemoteException");
        }
    }

    public static void stopHwVibrator(int pid, String opPkg, String type) {
        try {
            getService().stopHwVibrator(pid, opPkg, TOKEN, type);
        } catch (RemoteException e) {
            Log.e(TAG, "stopHwVibrator fail with RemoteException");
        }
    }

    public static void setHwParameter(String command) {
        try {
            getService().setHwParameter(command);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwParameter fail with RemoteException");
        }
    }

    public static String getHwParameter(String command) {
        try {
            return getService().getHwParameter(command);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwParameter fail with RemoteException");
            return null;
        }
    }

    public static void notifyVibrateOptions(Bundle options) {
        try {
            getService().notifyVibrateOptions(TOKEN, options);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyVibrateOptions fail with RemoteException");
        }
    }
}
