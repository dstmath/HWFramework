package ohos.vibrator.bean;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.vibrator.common.VibratorEffectUtil;

public class VibrationPattern {
    private static final int DURATION_INTENSITY = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113826, "VibrationEffect");
    private static final int MAX_COUNT = 100;
    private static final int MAX_DURATION = 1800000;
    private static final int MAX_INTENSITY = 255;
    private static final int MIN_COUNT = 0;
    private static final int MIN_DURATION = 0;
    private static final int MIN_INTENSITY = 0;
    private static final int MOD = 2;
    private static final int PARITY = 0;
    private static final int TIMING_COUNT = 2;
    private static final int TIMING_INTENSITIES_COUNT = 3;
    public static final String VIBRATOR_TPYE_ALLSCREEN_UPGLIDE_MULTITASK = "haptic.allscreen.upglide_multitask";
    public static final String VIBRATOR_TPYE_BATTERY_CHARGING = "haptic.battery.charging";
    public static final String VIBRATOR_TPYE_CAMERA_CLICK = "haptic.camera.click";
    public static final String VIBRATOR_TPYE_CAMERA_FOCUS = "haptic.camera.focus";
    public static final String VIBRATOR_TPYE_CAMERA_GEAR_SLIP = "haptic.camera.gear_slip";
    public static final String VIBRATOR_TPYE_CAMERA_MODE_SWITCH = "haptic.camera.mode_switch";
    public static final String VIBRATOR_TPYE_CAMERA_PORTRAIT_SWITCH = "haptic.camera.portrait_switch";
    public static final String VIBRATOR_TPYE_CLOCK_STOPWATCH = "haptic.clock.stopwatch";
    public static final String VIBRATOR_TPYE_CLOCK_TIMER = "haptic.clock.timer";
    public static final String VIBRATOR_TPYE_CONTACTS_LETTERS_INDEX = "haptic.contacts.letters_index";
    public static final String VIBRATOR_TPYE_CONTROL_DATE_SCROLL = "haptic.control.date_scroll";
    public static final String VIBRATOR_TPYE_CONTROL_LETTERS_SCROLL = "haptic.control.letters_scroll";
    public static final String VIBRATOR_TPYE_CONTROL_TIME_SCROLL = "haptic.control.time_scroll";
    public static final String VIBRATOR_TPYE_DESKTOP_LONG_PRESS = "haptic.desktop.long_press";
    public static final String VIBRATOR_TPYE_DIALLER_CLICK = "haptic.dialler.click";
    public static final String VIBRATOR_TPYE_DIALLER_LONG_PRESS = "haptic.dialler.long_press";
    public static final String VIBRATOR_TPYE_FINGERPRINT_LONG_PRESS = "haptic.fingerprint.long_press";
    public static final String VIBRATOR_TPYE_FINGERPRINT_UNLOCK_FAIL = "haptic.fingerprint.unlock_fail";
    public static final String VIBRATOR_TPYE_HIVOICE_CLICK = "haptic.hivoice.click";
    public static final String VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_BACK = "haptic.virtual_navigation.click_back";
    public static final String VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_HOME = "haptic.virtual_navigation.click_home";
    public static final String VIBRATOR_TPYE_VIRTUALNAVIGATION_CLICK_MULTITASK = "haptic.virtual_navigation.click_multitask";
    public static final String VIBRATOR_TPYE_VIRTUALNAVIGATION_LONGPRESS_HOME = "haptic.virtual_navigation.long_press";
    public static final String VIBRATOR_TPYE_WALLET_TIME_SCROLL = "haptic.wallet.time_scroll";
    public static final String VIBRATOR_TYPE_ALLSCREEN_SLIP_BACK = "haptic.allscreen.slip_back";
    public static final String VIBRATOR_TYPE_CALCULATOR_DELETE_LONG_PRESS = "haptic.calculator.delete_long_press";
    public static final String VIBRATOR_TYPE_CAMERA_CLICK_UP = "haptic.camera.click_up";
    public static final String VIBRATOR_TYPE_CAMERA_LONG_PRESS = "haptic.camera.long_press";
    public static final String VIBRATOR_TYPE_CONTROL_SEARCH_LONG_PRESS = "haptic.control.search_long_press";
    public static final String VIBRATOR_TYPE_CONTROL_TEXT_CHOOSE_CURSOR_MOVE = "haptic.control.text_choose_cursor_move";
    public static final String VIBRATOR_TYPE_CONTROL_TEXT_EDIT = "haptic.control.text_edit";
    public static final String VIBRATOR_TYPE_CONTROL_WIDGET_OPERATION = "haptic.control.widget_operation";
    public static final String VIBRATOR_TYPE_DESKTOP_PAGE_EDIT_CLICK = "haptic.desktop.page_edit_click";
    public static final String VIBRATOR_TYPE_DESKTOP_PAGE_LONG_PRESS = "haptic.desktop.page_long_press";
    public static final String VIBRATOR_TYPE_DESKTOP_WIDGET_LONG_PRESS = "haptic.desktop.widget_long_press";
    public static final String VIBRATOR_TYPE_DIALLER_DELETE_LONG_PRESS = "haptic.dialler.delete_long_press";
    public static final String VIBRATOR_TYPE_FINGERPRINT_IN_LONG_PRESS = "haptic.fingerprint.input_long_press";
    public static final String VIBRATOR_TYPE_FLOAT_TASKS_LONGPRESS = "haptic.virtual_float_tasks.long_press";
    public static final String VIBRATOR_TYPE_GALLERY_ALBUMS_LONG_PRESS = "haptic.gallery.albums_long_press";
    public static final String VIBRATOR_TYPE_GALLERY_PHOTOS_LONG_PRESS = "haptic.gallery.photos_long_press";
    public static final String VIBRATOR_TYPE_GALLERY_UPGLIDE_RELATED = "haptic.gallery.upglide_related";
    public static final String VIBRATOR_TYPE_GRADE_STRENGTH1 = "haptic.grade.strength1";
    public static final String VIBRATOR_TYPE_GRADE_STRENGTH2 = "haptic.grade.strength2";
    public static final String VIBRATOR_TYPE_GRADE_STRENGTH3 = "haptic.grade.strength3";
    public static final String VIBRATOR_TYPE_GRADE_STRENGTH4 = "haptic.grade.strength4";
    public static final String VIBRATOR_TYPE_GRADE_STRENGTH5 = "haptic.grade.strength5";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_FACE_UNLOCK_FAIL = "haptic.lockscreen.face_unlock_fail";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_FACE_UNLOCK_RETRY = "haptic.lockscreen.face_unlock_retry";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_NUMBER_UNLOCK_FAIL = "haptic.lockscreen.number_unlock_fail";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_ONEHAND_KEYBOARD_SWITCH = "haptic.lockscreen.onehand_keyboard_switch";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_UNLOCK_CLICK = "haptic.lockscreen.unlock_click";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_UNLOCK_SLIP = "haptic.lockscreen.unlock_slip";
    public static final String VIBRATOR_TYPE_LOCKSCREEN_UPGLIDE_SWITCHES = "haptic.lockscreen.upglide_switches";
    public static final String VIBRATOR_TYPE_NOTEPAD_NOTE_LONG_PRESS = "haptic.notepad.note_long_press";
    public static final String VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_EXPAND = "haptic.systemui.notifications_expand";
    public static final String VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_LONG_PRESS = "haptic.systemui.notifications_long_press";
    public static final String VIBRATOR_TYPE_SYSTEMUI_NOTIFICATIONS_MOVE = "haptic.systemui.notifications_move";
    public static final String VIBRATOR_TYPE_SYSTEMUI_SCREEN_RECORD_STOP = "haptic.systemui.screen_record_stop";
    public static final String VIBRATOR_TYPE_SYSTEMUI_SWITCHES_LONG_PRESS = "haptic.systemui.switches_long_press";
    public static final String VIBRATOR_TYPE_SYSTEMUI_SWITCHES_SORT_LONG_PRESS = "haptic.systemui.switches_sort_long_press";
    private int count;
    private int duration;
    private int effectFlag;
    private int[] intensities;
    private int intensity;
    private int[] timing;

    private boolean isDurationIntensityValid(int i, int i2) {
        return i >= 0 && i <= MAX_DURATION && i2 >= 0 && i2 <= 255;
    }

    private VibrationPattern(int i, int i2) {
        this.duration = i;
        this.intensity = i2;
        this.effectFlag = EffectFlagType.SINGLE.getValue();
    }

    private VibrationPattern(int[] iArr, int[] iArr2, int i) {
        this.timing = iArr;
        this.intensities = iArr2;
        this.count = i;
        this.effectFlag = EffectFlagType.PERIOD_CUSTOM.getValue();
    }

    private VibrationPattern(int[] iArr, int i) {
        this.timing = iArr;
        this.count = i;
        this.effectFlag = EffectFlagType.PERIOD_DEFAULT.getValue();
    }

    public static final VibrationPattern createSingle(int i, int i2) {
        HiLog.debug(LABEL, "createSingleEffect,duration: %{public}d intensity: %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
        return new VibrationPattern(i, i2);
    }

    public static final VibrationPattern createPeriod(int[] iArr, int[] iArr2, int i) {
        HiLog.debug(LABEL, "createPeriod effect with timing and intensities, count: %{public}d", Integer.valueOf(i));
        return new VibrationPattern(iArr, iArr2, i);
    }

    public static final VibrationPattern createPeriod(int[] iArr, int i) {
        HiLog.debug(LABEL, "createPeriod effect with timing, count: %{public}d", Integer.valueOf(i));
        return new VibrationPattern(iArr, i);
    }

    public int getIntensity() {
        return this.intensity;
    }

    public void setIntensity(int i) {
        this.intensity = i;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int i) {
        this.duration = i;
    }

    public int[] getIntensities() {
        int[] iArr = this.intensities;
        if (iArr == null) {
            return new int[0];
        }
        return (int[]) iArr.clone();
    }

    public void setIntensities(int[] iArr) {
        if (iArr == null) {
            this.intensities = null;
        } else {
            this.intensities = (int[]) iArr.clone();
        }
    }

    public int[] getTiming() {
        int[] iArr = this.timing;
        if (iArr == null) {
            return new int[0];
        }
        return (int[]) iArr.clone();
    }

    public void setTiming(int[] iArr) {
        if (iArr == null) {
            this.timing = null;
        } else {
            this.timing = (int[]) iArr.clone();
        }
    }

    public VibratorEffectUtil convert2VibratorEffectUtil() {
        int i = this.effectFlag;
        if (i != 1) {
            if (i != 2) {
                if (i == 3 && isTimingIntensityCountValid(this.timing, this.intensities, this.count)) {
                    return new VibratorEffectUtil(this.timing, this.intensities, this.count);
                }
                return null;
            } else if (!isTimingCountValid(this.timing, this.count)) {
                return null;
            } else {
                return new VibratorEffectUtil(this.timing, this.count);
            }
        } else if (!isDurationIntensityValid(this.duration, this.intensity)) {
            return null;
        } else {
            return new VibratorEffectUtil(this.duration, this.intensity);
        }
    }

    public enum EffectFlagType {
        SINGLE(1),
        PERIOD_DEFAULT(2),
        PERIOD_CUSTOM(3);
        
        private int value;

        private EffectFlagType(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    private boolean isTimingCountValid(int[] iArr, int i) {
        if (i < 0 || i > 100 || iArr.length % 2 != 0) {
            return false;
        }
        for (int i2 : iArr) {
            if (i2 < 0 || i2 > MAX_DURATION) {
                return false;
            }
        }
        int addTiming = addTiming(iArr);
        if (addTiming > MAX_DURATION || addTiming * i > MAX_DURATION) {
            return false;
        }
        return true;
    }

    private boolean isTimingIntensityCountValid(int[] iArr, int[] iArr2, int i) {
        if (i < 0 || i > 100 || iArr.length % 2 != 0 || iArr.length != iArr2.length) {
            return false;
        }
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (iArr[i2] < 0 || iArr[i2] > MAX_DURATION || iArr2[i2] < 0 || iArr2[i2] > 255) {
                return false;
            }
        }
        int addTiming = addTiming(iArr);
        if (addTiming > MAX_DURATION || addTiming * i > MAX_DURATION) {
            return false;
        }
        return true;
    }

    private int addTiming(int[] iArr) {
        int i = 0;
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (i2 % 2 != 0) {
                i += iArr[i2];
            }
        }
        return i;
    }
}
