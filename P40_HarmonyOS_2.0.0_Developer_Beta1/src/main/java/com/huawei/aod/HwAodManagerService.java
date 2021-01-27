package com.huawei.aod;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Slog;
import android.view.SurfaceControl;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import huawei.android.aod.AodConfigInfo;
import huawei.android.aod.AodVolumeInfo;
import huawei.android.aod.IAodManager;
import huawei.android.aod.Trigger;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class HwAodManagerService extends IAodManager.Stub {
    private static final int ANALOG_CLOCK_HOUR_IMAGE_HEIGHT = dp2Px(120);
    private static final int ANALOG_CLOCK_HOUR_IMAGE_WIDTH = dp2Px(24);
    private static final int ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT = dp2Px(80);
    private static final int ANALOG_DUAL_CLOCK_HOUR_IMAGE_WIDTH = dp2Px(16);
    private static final int AOD_MODE_ANIMATION = 128;
    private static final int AOD_MODE_FP_ONLY = 2;
    private static final int AOD_MODE_PORSCHE_LOGO = 32;
    private static final int AOD_MODE_PORSCHE_LOGO_AND_CLOCK = 33;
    private static final int AOD_MODE_TIME_ONLY = 1;
    private static final int AOD_MODE_VOLUME_BAR = 64;
    private static final String AOD_PACKAGE_NAME = "com.huawei.aod";
    private static final String AOD_SERVICE_ACTION = "com.huawei.aod.action.AODSERVICE_START";
    private static final String AOD_SERVICE_NAME = "com.huawei.aod.AODService";
    private static final int AOD_STATE_INIT = 0;
    private static final int AOD_STATE_PAUSE = 3;
    private static final int AOD_STATE_RESUME = 4;
    private static final int AOD_STATE_START = 1;
    private static final int AOD_STATE_STOP = 2;
    private static final int BITMAP_TYPE_ANALOG_CLOCK = 7;
    private static final int BITMAP_TYPE_ANIMATION = 12;
    private static final int BITMAP_TYPE_CHARGE_TIPS = 13;
    private static final int BITMAP_TYPE_DIGITAL_CLOCK = 6;
    private static final int BITMAP_TYPE_FACEID = 4;
    private static final int BITMAP_TYPE_FPDYNAMIC = 2;
    public static final int BITMAP_TYPE_PATTERN_CLOCK = 8;
    private static final int BITMAP_TYPE_PORSCHEIMAGE = 5;
    private static final int BITMAP_TYPE_VERTICAL_DIGITAL_CLOCK = 11;
    private static final int BITMAP_TYPE_VOLUME_BAR = 9;
    private static final int BITMAP_TYPE_VOLUME_ICON = 10;
    private static final int CHARGE_TIPS_MAX_TEXT_LINE = 2;
    private static final int CHARGE_TIPS_TEXT_SIZE_DP = 18;
    private static final int CHARGE_TIPS_TEXT_SIZE_MIN_DP = 9;
    private static final String CLASS_NAME_DISPLAY_SIZE_UTIL = "com.huawei.android.view.ExtDisplaySizeUtilEx";
    private static final int CONST_NUMBER_1 = 1;
    private static final int CONST_NUMBER_2 = 2;
    private static final int CONST_NUMBER_4 = 4;
    private static final int CONST_NUMBER_8 = 8;
    private static final int DEFAULT_INVALID_USER_ID = -111;
    private static final int DEFAULT_SCREEN_SIZE_STRING_LENGHT = 2;
    private static final int DESIRED_DPI = AodResUtil.getLcdDensity();
    private static int DIGITAL_CLOCK_IMAGE_HEIGHT = dp2Px(64);
    private static final int DIGITAL_CLOCK_IMAGE_HEIGHT_DP = 64;
    private static int DIGITAL_CLOCK_IMAGE_WIDTH = dp2Px(DIGITAL_CLOCK_IMAGE_WIDTH_DP);
    private static final int DIGITAL_CLOCK_IMAGE_WIDTH_DP = 168;
    private static final int DIGITAL_DUAL_CLOCK_IMAGE_HEIGHT_DP = 49;
    private static int DIGITAL_DUAL_CLOCK_IMAGE_HEIGHT_FOLD_SCREEN = WMStateCons.MSG_HIGH_TEMPERATURE;
    private static int DIGITAL_DUAL_CLOCK_IMAGE_WIDTH = dp2Px(112);
    private static final int DIGITAL_DUAL_CLOCK_IMAGE_WIDTH_DP = 112;
    private static int DIGITAL_DUAL_CLOCK_REAL_IMAGE_WIDTH = dp2Px(110);
    private static final int DIGITAL_DUAL_CLOCK_REAL_IMAGE_WIDTH_DP = 110;
    private static final int DISPALY_SCREEN_OFF = 1;
    private static final int DISPALY_SCREEN_ON = 0;
    private static final int DISP_POS_HOUR_SING = 2;
    private static final int DISP_POS_HOUR_TEN = 1;
    private static final int DISP_POS_STATIC = 0;
    private static final float DPI_DIVIDER = 160.0f;
    private static final int DUAL_CLOCK_CHARGE_TIPS_RECT = 35;
    private static final int DUAL_CLOCK_DIGITAL_COLOR_RECT = 14;
    private static final int DUAL_VERTICAL_CLOCK_NON_IMAGE_INDEX_HOUR_SING = 40;
    private static final int DUAL_VERTICAL_CLOCK_NON_IMAGE_INDEX_HOUR_TEN = 12;
    private static final int DUAL_VERTICAL_CLOCK_NON_IMAGE_INDEX_STATIC = 4;
    private static final int DUAL_VERTICAL_HOUR_INDEX = 33;
    private static final int EXTEND_ANALOG_CLOCK = 3;
    private static final int EXTEND_DIGITAL_CLOCK = 2;
    private static final int EXTEND_FOREST_CLOCK = 7;
    private static final int EXTEND_PATTERN_CLOCK = 4;
    private static final int EXTEND_VERTICAL_CLOCK = 6;
    private static final int FACE_ID_BITMAP_HEIGHT = 144;
    private static final int FACE_ID_BITMAP_WIDTH = 144;
    private static final int FACE_ID_BITMAP_WITH_TEXT_HEIGHT = 288;
    private static final int FACE_ID_BITMAP_WITH_TEXT_WIDTH = TARGET_DYNAMIC_WIDTH;
    private static final int FACE_ID_TEXT_HEIGHT = 144;
    private static final float FACE_ID_TEXT_SIZE = 13.0f;
    private static final String FACE_KEYGUARD_WITH_LOCK = "face_bind_with_lock";
    private static final String FACE_RECOGNIZE_SLIDE_UNLOCK = "face_recognize_slide_unlock";
    private static final String FACE_RECOGNIZE_UNLOCK = "face_recognize_unlock";
    private static final int FINGER_AUTH_FAIL_STATUS = 3;
    private static final int FINGER_AUTH_SUCCESS_STATUS = 2;
    private static final int FINGER_DOWN_STATUS = 1;
    private static final int FINGER_INIT_STATUS = 0;
    private static final String FINGER_PRINT_ANIM_TYPE = "fp_theme_dir";
    private static final String FINGER_PRINT_ENABLE = "fp_keyguard_enable";
    private static final int FINGER_UP_STATUS = 4;
    private static final int FIRST_LOCATION_PIC_INDEX_HOUR_SING = 20;
    private static final int FIRST_LOCATION_PIC_INDEX_HOUR_TEN = 6;
    private static final int FIRST_LOCATION_PIC_INDEX_STATIC = 2;
    private static final int FOLDING_SCREEN = 1;
    private static final int FOLD_ROTATED_DEGREE = -90;
    private static final String FORCE_UPDATE = "0";
    private static final String FP_ANIM_AOD = "/res/black";
    private static final String FP_ANIM_PATH = "/hw_product/";
    private static final String FP_BALCK_RES = "fp_black_res";
    private static final int FP_BITMAP_SIZE_HDPI = 720;
    private static final int FP_BITMAP_SIZE_XXHDPI = 1000;
    private static final int FP_BITMAP_SIZE_XXXHDPI = 1200;
    private static final int FP_RADIUS_DEF = 95;
    private static final int GET_INFO_TYPE_AODPOS = 1;
    private static final String HW_FOLD_DISPLAY_CHNAGE_MODE = "hw_fold_display_mode_prepare";
    private static final int INDEX_OF_FIELD_CONFIG_STR = 1;
    private static final int INDEX_OF_FIELD_CURRENT_TIME_ZONE = 0;
    private static final int INDEX_OF_FIELD_FORCE_UPDATE = 2;
    private static final int IN_SCREEN_INVALID_TYPE = -1;
    private static final int IN_SCREEN_OPTIC_TYPE = 1;
    private static final int IN_SCREEN_ULTRA_TYPE = 2;
    private static final boolean IS_FOLDING_SCREEN = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty());
    private static final boolean IS_SUPPORT_AP = "2".equals(SystemProperties.get("ro.config.support_aod", (String) null));
    private static final int LAYA_ANALOG_CLOCK_HOUR_IMAGE_HEIGHT = 360;
    private static final int LAYA_ANALOG_CLOCK_HOUR_IMAGE_WIDTH = 72;
    private static final int LAYA_ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT = 240;
    private static final int LAYA_ANALOG_DUAL_CLOCK_HOUR_IMAGE_WIDTH = 48;
    private static final int LAYA_PRODUCT_FLAG = 1;
    private static final int LENGTH_OF_CONFIG_STR = 3;
    private static int LTET_DIGITAL_DUAL_CLOCK_IMAGE_HEIGHT_FOLD_SCREEN = 136;
    private static final int MAX_ANIMATION_ION_BYTES = 104472576;
    private static final int MAX_BITMAP_NUM = 12;
    private static final int MAX_DIGITAL_CLOCK_BITMAP_COUNT = 8;
    private static final int MAX_DIGITAL_CLOCK_BITMAP_NUM = 2;
    private static final int MAX_DUAL_VERTICAL4_CLOCK_IMAGE_NUM_STATIC = 4;
    private static final int MAX_DUAL_VERTICAL_CLOCK_IMAGE_NUM = 42;
    private static final int MAX_DUAL_VERTICAL_CLOCK_IMAGE_NUM_HOUR_SING = 42;
    private static final int MAX_DUAL_VERTICAL_CLOCK_IMAGE_NUM_HOUR_TEN = 14;
    private static final int MAX_DUAL_VERTICAL_CLOCK_IMAGE_NUM_STATIC = 6;
    private static final int MAX_DUAL_VERTICAL_CLOCK_IMAGE_STATIC = 6;
    private static final int MAX_FACEID_BITMAP_NUM = 12;
    private static final int MAX_PATTERN_CLOCK_BITMAP_COUNT = 1;
    private static final int MAX_PATTERN_CLOCK_IMAGE_NUM = 1;
    private static final int MAX_PORSCHE_IMAGE_NUM = 1;
    private static final int MAX_SINGLE_ANALOG_HOUR_POINT_NUM = 6;
    private static final int MAX_SINGLE_DIGITAL_CLOCK_BITMAP_NUM = 1;
    private static final int MAX_SINGLE_VERTICAL4_CLOCK_IMAGE_STATIC = 2;
    private static final int MAX_SINGLE_VERTICAL_CLOCK_IMAGE_NUM = 22;
    private static final int MAX_SINGLE_VERTICAL_CLOCK_IMAGE_NUM_HOUR_SING = 22;
    private static final int MAX_SINGLE_VERTICAL_CLOCK_IMAGE_NUM_HOUR_TEN = 8;
    private static final int MAX_SINGLE_VERTICAL_CLOCK_IMAGE_NUM_STATIC = 4;
    private static final int MAX_SINGLE_VERTICAL_CLOCK_IMAGE_STATIC = 4;
    private static final int MAX_VOLUME_BAR_BITMAP_COUNT = 2;
    private static final int MAX_VOLUME_ICON_BITMAP_COUNT = 6;
    private static final String METHOD_NAME_DISPLAY_SAFE_INSETS = "getDisplaySafeInsets";
    private static final int NOT_FOLDING_SCREEN = 0;
    private static final int PAGE_SIZE = 4096;
    private static final int PATTERN1_HEIGHT_DP = 248;
    private static final int PATTERN2_HEIGHT_DP = 164;
    private static int PATTERN_ART_CUSTOM_PIC = getAlignWidth(dp2Px(PATTERN_ART_PIC_HEIGHT_DP));
    private static final int PATTERN_ART_PIC_HEIGHT_DP = 356;
    private static final int PATTERN_ART_WIDTH_DP = 312;
    private static int PATTERN_CLOCK_IMAGE_HEIGHT_TYPE1 = dp2Px(248);
    private static int PATTERN_CLOCK_IMAGE_HEIGHT_TYPE2_FOLD_SCREEN = 496;
    private static int PATTERN_CLOCK_IMAGE_HEIGHT_USER_CUSTOM_PIC = dp2Px(204);
    private static final int PATTERN_USER_CUSTOM_PIC_HEIGHT_DP = 204;
    private static final int PATTERN_WIDTH_DP = 360;
    private static final int POWER_STATE_INIT = -1;
    private static final int POWER_STATE_STARTED_GOING_TO_SLEEP = 101;
    private static final int POWER_STATE_STARTED_TURNING_ON = 102;
    private static final int POWER_STATE_STARTED_WAKING_UP = 100;
    private static final float REAL_DENSITY = (((float) DESIRED_DPI) / DPI_DIVIDER);
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_OK = 0;
    private static final int RESUME_FAILED = 1;
    private static final int RESUME_SUCCESS = 0;
    private static final String[] RES_IDS_DIGITAL_DOUBLE_CLOCK_XXHDPI = {"dual_clock_gradation_color_0", "dual_clock_gradation_color_1", "dual_clock_gradation_color_2", "dual_clock_gradation_color_3", "dual_clock_gradation_color_4", "dual_clock_gradation_color_5", "dual_clock_gradation_color_6", "dual_clock_gradation_color_7"};
    private static final String[] RES_IDS_DIGITAL_SINGLE_CLOCK_XXHDPI = {"single_clock_gradation_color_0", "single_clock_gradation_color_1", "single_clock_gradation_color_2", "single_clock_gradation_color_3", "single_clock_gradation_color_4", "single_clock_gradation_color_5", "single_clock_gradation_color_6", "single_clock_gradation_color_7"};
    private static final String[] RES_IDS_DUAL_ANALOG_CLOCK_XXHDPI = {"double_clock_hour_hand_1", "double_clock_minute_hand_1", "double_clock_hour_hand_2", "double_clock_minute_hand_2"};
    private static final String[] RES_IDS_FACEID = {"face_id_144x144_00", "face_id_144x144_01", "face_id_144x144_02", "face_id_144x144_03", "face_id_144x144_04", "face_id_144x144_05", "face_id_144x144_06", "face_id_144x144_07", "face_id_144x144_08", "face_id_144x144_09", "face_id_144x144_10", "face_id_144x144_11"};
    private static final String[] RES_IDS_SINGLE_ANALOG_CLOCK_XXHDPI = {"clock_hour_hand", "clock_minute_hand", "single_second_hand"};
    private static final String[] RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI = {"pre_dual1_digit0_bg", "pre_dual1_digit0_fg", "pre_dual1_digit1_bg", "pre_dual1_digit1_fg", "pre_dual1_digit2_bg", "pre_dual1_digit2_fg", "pre_dual2_digit0_bg", "pre_dual2_digit0_fg", "pre_dual2_digit1_bg", "pre_dual2_digit1_fg", "pre_dual2_digit2_bg", "pre_dual2_digit2_fg", "pre_dual_bg_black", "pre_dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI_FOR_STATIC_BUTTERFLY = {"static_butterfly_dual_left_bg", "static_butterfly_dual_left_fg", "static_butterfly_dual_right_bg", "static_butterfly_dual_right_fg", "static_dual_bg_black", "static_dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI_FOR_STATIC_FISH = {"static_fish_dual_left_bg", "static_fish_dual_left_fg", "static_fish_dual_right_bg", "static_fish_dual_right_fg", "static_dual_bg_black", "static_dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_SING_DOUBLE_CLOCK_XXHDPI = {"dual1_digit0_bg", "dual1_digit0_fg", "dual1_digit1_bg", "dual1_digit1_fg", "dual1_digit2_bg", "dual1_digit2_fg", "dual1_digit3_bg", "dual1_digit3_fg", "dual1_digit4_bg", "dual1_digit4_fg", "dual1_digit5_bg", "dual1_digit5_fg", "dual1_digit6_bg", "dual1_digit6_fg", "dual1_digit7_bg", "dual1_digit7_fg", "dual1_digit8_bg", "dual1_digit8_fg", "dual1_digit9_bg", "dual1_digit9_fg", "dual2_digit0_bg", "dual2_digit0_fg", "dual2_digit1_bg", "dual2_digit1_fg", "dual2_digit2_bg", "dual2_digit2_fg", "dual2_digit3_bg", "dual2_digit3_fg", "dual2_digit4_bg", "dual2_digit4_fg", "dual2_digit5_bg", "dual2_digit5_fg", "dual2_digit6_bg", "dual2_digit6_fg", "dual2_digit7_bg", "dual2_digit7_fg", "dual2_digit8_bg", "dual2_digit8_fg", "dual2_digit9_bg", "dual2_digit9_fg", "dual_bg_black", "dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_SING_SINGLE_CLOCK_XXHDPI = {"single_digit0_bg", "single_digit0_fg", "single_digit1_bg", "single_digit1_fg", "single_digit2_bg", "single_digit2_fg", "single_digit3_bg", "single_digit3_fg", "single_digit4_bg", "single_digit4_fg", "single_digit5_bg", "single_digit5_fg", "single_digit6_bg", "single_digit6_fg", "single_digit7_bg", "single_digit7_fg", "single_digit8_bg", "single_digit8_fg", "single_digit9_bg", "single_digit9_fg", "single_bg_black", "single_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_STATIC_DOUBLE_CLOCK_XXHDPI = {"dual1_bg", "dual1_fg", "dual2_bg", "dual2_fg", "dual_bg_black", "dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_STATIC_SINGLE_CLOCK_XXHDPI = {"single_bg", "single_fg", "single_bg_black", "single_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_TEN_DOUBLE_CLOCK_XXHDPI = {"dual1_digit0_bg", "dual1_digit0_fg", "dual1_digit1_bg", "dual1_digit1_fg", "dual1_digit2_bg", "dual1_digit2_fg", "dual2_digit0_bg", "dual2_digit0_fg", "dual2_digit1_bg", "dual2_digit1_fg", "dual2_digit2_bg", "dual2_digit2_fg", "dual_bg_black", "dual_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_ONLINE_TEN_SINGLE_CLOCK_XXHDPI = {"single_digit0_bg", "single_digit0_fg", "single_digit1_bg", "single_digit1_fg", "single_digit2_bg", "single_digit2_fg", "single_bg_black", "single_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI = {"pre_single_digit0_bg", "pre_single_digit0_fg", "pre_single_digit1_bg", "pre_single_digit1_fg", "pre_single_digit2_bg", "pre_single_digit2_fg", "pre_single_bg_black", "pre_single_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI_FOR_STATIC_BUTTERFLY = {"static_butterfly_single_bg", "static_butterfly_single_fg", "static_single_bg_black", "static_single_fg_empty"};
    private static final String[] RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI_FOR_STATIC_FISH = {"static_fish_single_bg", "static_fish_single_fg", "static_single_bg_black", "static_single_fg_empty"};
    private static final String[] RES_IDS_VOLUME_BAR = {"ic_bg_volume_bar", "ic_fg_volume_bar", "ic_call", "ic_call_bluetooth", "ic_volume", "ic_volume_mute", "ic_bluetooth", "ic_bluetooth_mute"};
    private static final String[] RES_IDS_XXHDPI = {"wave_500x500_01", "wave_500x500_02", "wave_500x500_03", "wave_500x500_04", "wave_500x500_05", "wave_500x500_06", "wave_500x500_07", "wave_500x500_08", "wave_500x500_09", "wave_500x500_10", "wave_500x500_11", "wave_500x500_12"};
    private static final String[] RES_IDS_XXXHDPI = {"wave_600x600_01", "wave_600x600_02", "wave_600x600_03", "wave_600x600_04", "wave_600x600_05", "wave_600x600_06", "wave_600x600_07", "wave_600x600_08", "wave_600x600_09", "wave_600x600_10", "wave_600x600_11", "wave_600x600_12"};
    private static final String[] RES_ID_PATTERN_CLOCK_IMAGE_XXHDPI = {"pattern_clock_background"};
    private static final String[] RES_ID_PORSCHEIMAGE_XXXHDPI = {"porsche_768x48_xxxhpi_01"};
    private static final int SAFE_MARGIN = 4;
    private static final int SAFE_PADDING = 24;
    private static final float SCALE_RATIO = 2.0f;
    private static final float SCALE_RATIO_FOUR = 4.0f;
    private static final int SCREEN_DEFAULT_WIDTH = 1440;
    private static final int SET_FINGER_DOWN_ERROR_END = 13;
    private static final int SET_FINGER_DOWN_NORMAL_END = 12;
    private static final int SET_FINGER_DOWN_START = 11;
    private static final int SET_FINGER_INIT_STATUS = 10;
    private static final int SET_FINGER_PRINT_UP = 14;
    private static final String SHOW_PIC_INDEX = "show_pic_index";
    private static final String SIDE_DISP = "ro.config.hw_curved_side_disp";
    private static final int SIGNATURE_DEFAULT_INDEX = 0;
    private static final int SINGLE_CLOCK_CHARGE_TIPS_RECT = 26;
    private static final int SINGLE_CLOCK_DIGITAL_COLOR_RECT = 10;
    private static final int SINGLE_CLOCK_MODE = 0;
    private static final int SINGLE_VERTICAL_CLOCK_NON_IMAGE_INDEX_HOUR_SING = 20;
    private static final int SINGLE_VERTICAL_CLOCK_NON_IMAGE_INDEX_HOUR_TEN = 6;
    private static final int SINGLE_VERTICAL_CLOCK_NON_IMAGE_INDEX_STATIC = 2;
    private static final int SING_VERTICAL_HOUR_INDEX = 24;
    private static final int STATIC_BG_FG = 4;
    private static final int STATIC_DIGIT_CLOCK_PIC_INDEX = 2;
    private static final int STATIC_DIGIT_CLOCK_TRANSPARENT_PIC_INDEX = 4;
    private static final int STATIC_ONLY_BG = 3;
    private static final String TAG = "HwAodManagerService";
    private static int TARGET_DYNAMIC_HEIGHT = TARGET_DYNAMIC_WIDTH;
    private static int TARGET_DYNAMIC_WIDTH = getFpBitmapSize();
    private static int TARGET_PORSCHE_IMAGE_HEIGHT = 48;
    private static int TARGET_PORSCHE_IMAGE_WIDTH = HighBitsALModeID.MODE_SRE_DISABLE;
    private static final int TETON_MAIN_SCREEN_WIDTH = 1160;
    private static final int[] TIME_TEXT_COLOR = {-4995585, -3152154, -4793870, -4793870, -4793870, -4793870, -4793870, -2502926};
    private static final String URI_SYSTEMUI_NOFIFY_PROVIDER = "content://com.android.systemui.doze.hwaodremote";
    private static int VERTICAL3_CLOCK_IMAGE_HEIGHT = dp2Px(200);
    private static final int VERTICAL3_HEIGHT_DP = 200;
    private static final int VERTICAL4_CLOCK_IMAGE_HEIGHT = dp2Px(200);
    private static final int VERTICAL4_CLOCK_IMAGE_HEIGHT_DP = 200;
    private static final int VERTICAL_BG_FG_PIC_NUM = 2;
    private static final int VERTICAL_CLOCK_IMAGE_HEIGHT = dp2Px(248);
    private static final int VERTICAL_CLOCK_IMAGE_HEIGHT_DP = 248;
    private static final int VERTICAL_CLOCK_IMAGE_OFFSET = 4;
    private static final int VERTICAL_CLOCK_IMAGE_WIDTH_DP = 360;
    private static final int VOLUME_BAR_IMAGE_HEIGHT = 480;
    private static final int VOLUME_BAR_IMAGE_WIDTH = 6;
    private static final int VOLUME_BAR_IMAGE_WIDTH_ALIGN = 8;
    private static final int VOLUME_ICON_IMAGE_HEIGHT = 72;
    private static final int VOLUME_ICON_IMAGE_WIDTH = 72;
    private static final int XXHDPI = 480;
    private static final int XXXHDPI = 640;
    private static int currrentColorFlag = 0;
    private static int homeColorFlag = 0;
    private static boolean mAnalogClockDecodeFlag = false;
    private static boolean mDigitalClockDecodeFlag = false;
    private static int mFingerPrintCircleRadius = FingerprintManagerEx.getHighLightspotRadius();
    private static int mRadiusColor = -16711681;
    private static int sFingerprintType = FingerprintManagerEx.getHardwareType();
    private static HwAodManagerService sInstance = null;
    private static boolean sIsLayaPorduct;
    private static boolean sIsLayaPorschePorduct;
    private static boolean sIsPorscheProduct = SystemProperties.getBoolean("ro.config.pd_font_enable", false);
    private static boolean sIsSupportReconstruction;
    private static boolean sIsTahitiProduct;
    private static boolean sIsTaurusProduct;
    private static boolean sIsTetonProduct;
    private static String sProductBrandString = SystemProperties.get("ro.product.board", "UNKOWN");
    private String[] RES_IDS = RES_IDS_XXXHDPI;
    private Rect[] aodInfoRect;
    private int aodinfoTextHeight = 0;
    private int aodinfoTextWidth = 0;
    private int mAODWorkMode = 0;
    private int mActionDataSize = 0;
    private List<String> mAnimFileNames = new CopyOnWriteArrayList();
    private byte[] mAnimationBitmapBuffer;
    private int mAnimationBitmapNum = 0;
    private int mAnimationBufferSize = 0;
    private long mAodDevicePtr;
    private int mAodQuitType = 0;
    private ContentObserver mAodThemeVersionObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass8 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.i(HwAodManagerService.TAG, "mAodThemeVersionObserver onChange");
            HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
            HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
        }
    };
    private byte[] mBaseTimeAreaBitmapBuffer;
    private int mBaseTimeAreaBitmapNum = 0;
    private int mBaseTimeAreaBufferSize = 0;
    private int mBgFgdisplayPos = -1;
    private Bitmap[] mBitmap = new Bitmap[12];
    private byte[] mBitmapBuffer;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.w(HwAodManagerService.TAG, "onReceive, the context or intent is null.");
                return;
            }
            String action = intent.getAction();
            Slog.w(HwAodManagerService.TAG, "mBroadcastReceiver receive action : " + action);
            if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                if (HwAodManagerService.this.mFaceIdResDecodeFlag) {
                    Slog.i(HwAodManagerService.TAG, "BroadcastReceiver onReceive Intent.ACTION_LOCALE_CHANGED.");
                    HwAodManagerService.this.decodeFaceIdResourceAsync();
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", HwAodManagerService.DEFAULT_INVALID_USER_ID);
                Slog.i(HwAodManagerService.TAG, " mBroadcastReceiver with delete userid is : " + userId);
                AodThemeManager.getInstance().deleteUserSharedPreferences(userId);
                HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
                HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
            } else {
                Slog.i(HwAodManagerService.TAG, " mBroadcastReceiver default branch");
            }
        }
    };
    private String mChargeTips = "";
    private byte[] mChargeTipsBitmapBuffer;
    private int mChargeTipsBitmapBufferSize = 0;
    private int mChargeTipsBitmapNum = 0;
    private int mClockMode = 0;
    private boolean mConfigChanged = false;
    private int mConfigScreenMode = 0;
    private Context mContext;
    private String[] mCurrentConfigString = new String[3];
    private Runnable mDecodeAnalogImageResourceRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass7 */

        @Override // java.lang.Runnable
        public void run() {
            HwAodManagerService.this.getAnologAndBackgroundResDecode();
        }
    };
    private Runnable mDecodeFaceIdResourceRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass5 */

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwAodManagerService.TAG, "decodeFaceIdResource Async begin!!!");
            Context aodContext = null;
            try {
                aodContext = HwAodManagerService.this.mContext.createPackageContext(HwAodManagerService.AOD_PACKAGE_NAME, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(HwAodManagerService.TAG, "package not exist " + e.toString());
            }
            if (aodContext == null) {
                Slog.w(HwAodManagerService.TAG, "can not decode resource as aodContext is null");
                return;
            }
            boolean unused = HwAodManagerService.sIsPorscheProduct = SystemProperties.getBoolean("ro.config.pd_font_enable", false);
            long time = System.currentTimeMillis();
            int size = HwAodManagerService.RES_IDS_FACEID.length;
            for (int j = 0; j < size; j++) {
                HwAodManagerService.this.decodeFaceIdResource(aodContext, j);
            }
            Slog.i(HwAodManagerService.TAG, "decodeFaceIdResource Async finish!!!, time total = " + (System.currentTimeMillis() - time));
        }
    };
    private Runnable mDecodePorscheImageResourceRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwAodManagerService.TAG, "mDecodePorscheImageResourceRunnable Async begin!!!");
            Context aodContext = null;
            try {
                aodContext = HwAodManagerService.this.mContext.createPackageContext(HwAodManagerService.AOD_PACKAGE_NAME, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(HwAodManagerService.TAG, "package not exist " + e.toString());
            }
            if (aodContext == null) {
                Slog.w(HwAodManagerService.TAG, "can not decode resource as aodContext is null");
                return;
            }
            long time = System.currentTimeMillis();
            int size = HwAodManagerService.RES_ID_PORSCHEIMAGE_XXXHDPI.length;
            for (int i = 0; i < size; i++) {
                HwAodManagerService.this.decodePorscheImageResource(aodContext, i);
            }
            Slog.i(HwAodManagerService.TAG, "decodePorscheImageResource Async finish!!!, time total = " + (System.currentTimeMillis() - time));
        }
    };
    private Runnable mDecodeResourceRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            HwAodManagerService.this.decodeFpResource();
        }
    };
    private int mDeviceNodeFD = -2147483647;
    private Bitmap[] mDigitalClockBitmap = new Bitmap[8];
    private byte[] mDigitalClockBitmapBuffer;
    private int mDigitalClockBitmapNum = 0;
    private Bitmap[] mDigitalDoubleClockBitmap = new Bitmap[8];
    private int mDisplayGMPBufferSize = 0;
    private int[] mDisplayGMPHighBuffer;
    private long[] mDisplayGMPLowBuffer;
    private int mDisplayMode = 0;
    private boolean mDisplayModeChanging = false;
    private ContentObserver mDisplayModeObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.i(HwAodManagerService.TAG, "display mode observer mLastDisplayMode:" + HwAodManagerService.this.mLastDisplayMode);
            if (HwAodManagerService.this.mContext != null) {
                if (!(HwAodManagerService.this.mPowerState == 100 || HwAodManagerService.this.mPowerState == 102)) {
                    HwAodManagerService.this.mDisplayModeChanging = true;
                }
                int nextDisplayMode = Settings.Global.getInt(HwAodManagerService.this.mContext.getContentResolver(), HwAodManagerService.HW_FOLD_DISPLAY_CHNAGE_MODE, 0);
                if (HwAodManagerService.this.mLastDisplayMode == 1 && nextDisplayMode == 2) {
                    HwAodManagerService.this.mAodQuitType = 1;
                } else if (HwAodManagerService.this.mLastDisplayMode == 2 && nextDisplayMode == 1) {
                    HwAodManagerService.this.mAodQuitType = 2;
                } else {
                    HwAodManagerService.this.mAodQuitType = 0;
                }
                Slog.i(HwAodManagerService.TAG, "display mode observer mLastDisplayMode:" + HwAodManagerService.this.mLastDisplayMode + " nextDisplayMode:" + nextDisplayMode + " mAodQuitType:" + HwAodManagerService.this.mAodQuitType);
                HwAodManagerService.this.mLastDisplayMode = nextDisplayMode;
                HwAodManagerService.nativeSetFoldDisplayMode(HwAodManagerService.this.mAodDevicePtr, nextDisplayMode);
            }
        }
    };
    private Bitmap[] mDualAnalogHourBitmap = new Bitmap[6];
    private byte[] mDynamicBitmapBuffer;
    private int mDynamicBitmapNum = 0;
    private Bitmap[] mFaceIdBitmap = new Bitmap[12];
    private byte[] mFaceIdBitmapBuffer;
    private int mFaceIdBitmapNum = 0;
    private ContentObserver mFaceIdObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass15 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean isFaceIdEnable = HwAodManagerService.isFaceSettingSwitchEnabled(HwAodManagerService.this.mContext);
            Slog.i(HwAodManagerService.TAG, "face recognition switch onChange, resources decode mFaceIdResDecodeFlag:" + HwAodManagerService.this.mFaceIdResDecodeFlag + " isFaceIdEnable:" + isFaceIdEnable);
            if (!HwAodManagerService.this.mFaceIdResDecodeFlag && isFaceIdEnable) {
                HwAodManagerService.this.decodeFaceIdResourceAsync();
                HwAodManagerService.this.mFaceIdResDecodeFlag = true;
            } else if (HwAodManagerService.this.mFaceIdResDecodeFlag && !isFaceIdEnable) {
                for (int i = 0; i < 12; i++) {
                    if (HwAodManagerService.this.mFaceIdBitmap[i] != null) {
                        HwAodManagerService.this.mFaceIdBitmap[i].recycle();
                        HwAodManagerService.this.mFaceIdBitmap[i] = null;
                    }
                }
                Slog.w(HwAodManagerService.TAG, " mFaceIdObserver release FaceId bitmap done. ");
                HwAodManagerService.this.mFaceIdBitmapNum = 0;
                HwAodManagerService.this.mFaceIdResDecodeFlag = false;
            }
        }
    };
    private boolean mFaceIdResDecodeFlag = false;
    private IntentFilter mFilter;
    private byte[] mFingerAreaBitmapBuffer;
    private int mFingerAreaBitmapNum = 0;
    private int mFingerAreaBufferSize = 0;
    private int mFingerStatus = 0;
    private HwFoldScreenManagerEx.FoldDisplayModeListener mFoldDisplayModeListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass3 */

        public void onScreenDisplayModeChange(int displayMode) {
            Slog.i(HwAodManagerService.TAG, "onScreenDisplayModeChange displayMode:" + displayMode);
            HwAodManagerService.this.mDisplayModeChanging = false;
        }
    };
    private ContentObserver mFpAnimObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass14 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (this) {
                Slog.i(HwAodManagerService.TAG, " fingerprint anim switch onChange");
                HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeResourceRunnable);
                HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeResourceRunnable);
            }
        }
    };
    private ContentObserver mFpObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass13 */

        @Override // android.database.ContentObserver
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (this) {
                Slog.i(HwAodManagerService.TAG, " fingerprint switch onChange, resources decode mResDecodeFlag:" + HwAodManagerService.this.mResDecodeFlag);
                if (!HwAodManagerService.this.mResDecodeFlag && HwAodManagerService.isSettingSwitchEnabled(HwAodManagerService.this.mContext, HwAodManagerService.FINGER_PRINT_ENABLE)) {
                    HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeResourceRunnable);
                    HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeResourceRunnable);
                    HwAodManagerService.this.mResDecodeFlag = true;
                }
            }
        }
    };
    private Handler mHandler;
    private boolean mIsConstructDone = false;
    private boolean mIsPreSet = true;
    private boolean mIsProductCustomized = false;
    private int mIsSupportReconstruction = 0;
    private int mIsTimeFresh = 1;
    private boolean mIsVmallCustomized = false;
    private String[] mLastConfigString = new String[3];
    private int mLastDisplayMode = 0;
    private int mNotificationOffset = 0;
    private byte[] mNumberAreaBitmapBuffer;
    private int mNumberAreaBitmapNum = 0;
    private int mNumberAreaBufferSize = 0;
    private int mOldChipSetProduct = 1;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private int mParcelFileSize;
    private Bitmap[] mPatternClockBitmap = new Bitmap[1];
    private byte[] mPatternClockBitmapBuffer;
    private int mPatternClockBitmapNum = 0;
    private int mPatternClockBufferSize = 0;
    private Bitmap[] mPorscheImageBitmap = new Bitmap[1];
    private byte[] mPorscheImageBuffer;
    private int mPorscheImageNum = 0;
    private boolean mPosReady = false;
    private int mPosX = 0;
    private int mPosY = 0;
    private int mPowerState = -1;
    private boolean mReloadThemeFlag = false;
    private boolean mResDecodeFlag = false;
    private Bitmap[] mRingVolumeBitmap = new Bitmap[8];
    private Runnable mSetDynamicBitmapRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass11 */

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwAodManagerService.TAG, "mSetDynamicBitmapRunnable enter mAODWorkMode:" + HwAodManagerService.this.mAODWorkMode);
            HwAodManagerService.this.releaseClockBitmapBufferResources();
            if (HwAodManagerService.sIsLayaPorschePorduct && (HwAodManagerService.this.mAODWorkMode & 33) == 33) {
                HwAodManagerService.this.handlePorscheImageResources();
            }
            HwAodManagerService.this.handleClockImageResources();
            if (!TextUtils.isEmpty(SystemProperties.get(HwAodManagerService.SIDE_DISP, ""))) {
                HwAodManagerService.this.handleVolumeImageResources();
            }
            if ((HwAodManagerService.this.mAODWorkMode & 2) == 2) {
                HwAodManagerService.this.handleCachedResources();
            }
            if (HwAodManagerService.sIsSupportReconstruction && (HwAodManagerService.this.mAODWorkMode & 128) == 128) {
                HwAodManagerService.this.handleAnimationResource();
            }
            if (HwAodManagerService.IS_FOLDING_SCREEN) {
                HwAodManagerService.this.handleChargeTipsResources();
            }
            HwAodManagerService.this.handleGMPDataForNative();
        }
    };
    private byte[] mSingleAnalogClockHourBitmapBuffer;
    private int mSingleAnalogClockHourBitmapNum = 0;
    private int mSingleAnalogClockHourBufferSize = 0;
    private Bitmap[] mSingleAnalogHourBitmap = new Bitmap[6];
    private int mSingleDigitalClockBufferSize = 0;
    private int mSingleDynamicBitmapBufferSize = 0;
    private int mSingleFaceIdBitmapBufferSize = 0;
    private int mSinglePorscheImageBufferSize = 0;
    private int mState = 0;
    private int mTirggerSize = 0;
    private List<Trigger> mTriggerList;
    private Runnable mUpdateDynamicBitmapRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass12 */

        @Override // java.lang.Runnable
        public void run() {
            Slog.e(HwAodManagerService.TAG, "mUpdateDynamicBitmapRunnable begin. mDisplayMode:" + HwAodManagerService.this.mDisplayMode);
            if (HwAodManagerService.sIsTaurusProduct && HwAodManagerService.this.mDisplayMode == 2) {
                HwAodManagerService.this.updateDigitalClockCachedResources();
            }
            if (HwAodManagerService.sIsTaurusProduct && HwAodManagerService.this.mDisplayMode == 6) {
                HwAodManagerService.this.updateVerticalClockCachedResources();
            }
        }
    };
    private Runnable mUpdateRunnable = new Runnable() {
        /* class com.huawei.aod.HwAodManagerService.AnonymousClass10 */

        @Override // java.lang.Runnable
        public void run() {
            if (HwAodManagerService.this.mParcelFileDescriptor == null) {
                Slog.i(HwAodManagerService.TAG, "mUpdateRunnable error. return. ");
                return;
            }
            HwAodManagerService hwAodManagerService = HwAodManagerService.this;
            if (hwAodManagerService.setBitmapByMemoryFileInner(hwAodManagerService.mParcelFileSize, HwAodManagerService.this.mParcelFileDescriptor) == 0) {
                HwAodManagerService.nativeBeginUpdate(HwAodManagerService.this.mAodDevicePtr);
                HwAodManagerService.nativeEndUpdate(HwAodManagerService.this.mAodDevicePtr);
                return;
            }
            Slog.e(HwAodManagerService.TAG, "setBitmapByMemoryFileInner err! stop update AOD!");
        }
    };
    private Bitmap[] mVerticalClockBitmap = new Bitmap[22];
    private byte[] mVerticalClockBitmapBuffer;
    private int mVerticalClockBitmapNum = 0;
    private int mVerticalClockBufferSize = 0;
    private Rect mVerticalDisplayNumber = new Rect();
    private Bitmap[] mVerticalDoubleClockBitmap = new Bitmap[42];
    private byte[] mVolumeBarBitmapBuffer;
    private int mVolumeBarBitmapNum = 0;
    private int mVolumeBarBufferSize = 0;
    private byte[] mVolumeIconBitmapBuffer;
    private int mVolumeIconBitmapNum = 0;
    private int mVolumeIconBufferSize = 0;
    private final TpController tpCmdSender = new TpController();

    /* access modifiers changed from: private */
    public static native void nativeBeginUpdate(long j);

    /* access modifiers changed from: private */
    public static native void nativeEndUpdate(long j);

    private static native int nativeGetDeviceNodeFD(long j);

    private static native int[] nativeGetPos(long j);

    private static native int nativeGetResumeStatus(long j);

    private static native long nativeInit(HwAodManagerService hwAodManagerService);

    private static native void nativeIsRtl(long j, boolean z);

    private static native void nativePause(long j, int i);

    private static native int nativeReleaseClockBitmapBuffer(long j);

    private static native void nativeResume(long j);

    private static native int nativeSetAnalogClockHourBitmap(long j, int i);

    private static native int nativeSetAnimationBitmap(long j, int i);

    private static native void nativeSetAodConfigInfo(long j, AodConfigInfo aodConfigInfo);

    private static native int nativeSetBaseTimeAreaBitmap(long j, int i);

    private static native int nativeSetChargeTipsImage(long j, int i);

    private static native int nativeSetDigitalClockBitmap(long j, int i);

    private static native void nativeSetDisplayScreenStatus(long j, int i);

    private static native int nativeSetDynamicBitmap(long j, int i);

    private static native int nativeSetFaceIdBitmap(long j, int i);

    private static native int nativeSetFingerAreaBitmap(long j, int i);

    private static native void nativeSetFingerStatus(long j, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetFoldDisplayMode(long j, int i);

    private static native void nativeSetFoldScreenState(long j, int i, int i2);

    private static native int nativeSetNumAreaBitmap(long j, int i);

    private static native int nativeSetPatternClockBitmap(long j, int i);

    private static native int nativeSetPorscheImage(long j, int i);

    private static native void nativeSetProduct(long j, int i);

    private static native int nativeSetVerticalClockBitmap(long j, int i);

    private static native int nativeSetVolumeBarBitmap(long j, int i);

    private static native int nativeSetVolumeIconBitmap(long j, int i);

    private static native void nativeStart(long j);

    private static native void nativeUpdateAodVolumeInfo(long j, AodVolumeInfo aodVolumeInfo);

    private static native int nativeUpdateClockBitmapBuffer(long j, int i);

    static {
        sIsTaurusProduct = false;
        sIsLayaPorschePorduct = false;
        sIsLayaPorduct = false;
        sIsTetonProduct = false;
        sIsTahitiProduct = false;
        sIsSupportReconstruction = false;
        String board = sProductBrandString.toUpperCase(Locale.US);
        boolean isLayaBoard = board.contains("LYA") || board.contains("LAYA");
        sIsLayaPorduct = isLayaBoard && DESIRED_DPI == XXXHDPI;
        if (sIsPorscheProduct) {
            if (isLayaBoard || board.contains("NLE") || board.contains("LIO") || board.contains("NOP")) {
                sIsLayaPorschePorduct = true;
            }
            Slog.i(TAG, "HwAodManagerService sIsLayaPorscheProduct " + sIsLayaPorschePorduct);
        }
        if (SystemProperties.getInt("ro.config.color_aod_type", 0) != 0) {
            sIsTaurusProduct = true;
        }
        sIsSupportReconstruction = AodThemeConst.isSupportAod3Platform(SystemProperties.get("ro.board.platform", "UNKOWN").toUpperCase(Locale.US));
        sIsTetonProduct = board.contains("TET");
        sIsTahitiProduct = board.contains("TAH");
        Slog.i(TAG, "HwAodManagerService sIsTaurusProduct:" + sIsTaurusProduct + ", isSupportReconstruction:" + sIsSupportReconstruction);
    }

    private void registerDisplayModeObserver() {
        Context context = this.mContext;
        if (context != null) {
            this.mLastDisplayMode = Settings.Global.getInt(context.getContentResolver(), HW_FOLD_DISPLAY_CHNAGE_MODE, 0);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(HW_FOLD_DISPLAY_CHNAGE_MODE), true, this.mDisplayModeObserver);
            nativeSetFoldDisplayMode(this.mAodDevicePtr, this.mLastDisplayMode);
            Slog.i(TAG, "register display mode listener and observer done, mLastDisplayMode:" + this.mLastDisplayMode);
        }
    }

    public static synchronized HwAodManagerService getInstance(Context context) {
        HwAodManagerService hwAodManagerService;
        synchronized (HwAodManagerService.class) {
            if (sInstance == null) {
                sInstance = new HwAodManagerService(context);
            }
            hwAodManagerService = sInstance;
        }
        return hwAodManagerService;
    }

    public HwAodManagerService(Context context) {
        Slog.i(TAG, "HwAodManagerService constructor,mIsConstructDone:" + this.mIsConstructDone);
        try {
            System.loadLibrary("aod_jni");
        } catch (Exception e) {
            Slog.e(TAG, "loadLibrary exception");
        }
        if (context == null) {
            Slog.e(TAG, "context is null");
            return;
        }
        this.mContext = context;
        DisplayTypeConfig.getInstance(context).setLmtEnable();
        HandlerThread hwAodManagerServiceThread = new HandlerThread("HwAOD_Manager_Service_Thread", 0);
        hwAodManagerServiceThread.start();
        this.mHandler = new Handler(hwAodManagerServiceThread.getLooper());
        int highLightSpotColor = getFingerprintSpotColor();
        if (highLightSpotColor != 0) {
            mRadiusColor = highLightSpotColor;
            Slog.i(TAG, "HwAodManagerService adjust mRadiusColor:" + mRadiusColor);
        }
        if (TARGET_DYNAMIC_WIDTH < FP_BITMAP_SIZE_XXXHDPI) {
            this.RES_IDS = RES_IDS_XXHDPI;
            Slog.i(TAG, "HwAodManagerService adjust TARGET_DYNAMIC_WIDTH " + TARGET_DYNAMIC_WIDTH);
        }
        if (!this.mIsConstructDone) {
            this.mIsConstructDone = true;
            this.mAodDevicePtr = nativeInit(this);
            this.mDeviceNodeFD = nativeGetDeviceNodeFD(this.mAodDevicePtr);
            if (this.mDeviceNodeFD > 0 && sIsLayaPorschePorduct) {
                decodePorscheImageResourceAsync();
            }
        }
        if (!TextUtils.isEmpty(SystemProperties.get(SIDE_DISP, ""))) {
            decodeRingVolumeBarResource();
        }
        this.mIsSupportReconstruction = sIsSupportReconstruction ? 1 : 0;
        Slog.i(TAG, "HwAodManagerService mIsSupportReconstruction:" + this.mIsSupportReconstruction);
        this.mFilter = new IntentFilter();
        if (this.mDeviceNodeFD > 0 && hasFingerPrintInScreen()) {
            Slog.i(TAG, "HwAodManagerService check decodeResourceAsync mResDecodeFlag:" + this.mResDecodeFlag);
            hasFingerPrintRadius();
            if (this.mResDecodeFlag || !isSettingSwitchEnabled(context, FINGER_PRINT_ENABLE)) {
                Slog.i(TAG, "HwAodManagerService register fingerprint observer.");
                context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(FINGER_PRINT_ENABLE), true, this.mFpObserver);
            } else {
                this.mHandler.removeCallbacks(this.mDecodeResourceRunnable);
                this.mHandler.post(this.mDecodeResourceRunnable);
                this.mResDecodeFlag = true;
            }
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(FINGER_PRINT_ANIM_TYPE), false, this.mFpAnimObserver, -1);
            if (this.mFaceIdResDecodeFlag || !isFaceSettingSwitchEnabled(context)) {
                Slog.i(TAG, "HwAodManagerService register face recognition observer.");
                context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(FACE_KEYGUARD_WITH_LOCK), true, this.mFaceIdObserver);
                context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(FACE_RECOGNIZE_SLIDE_UNLOCK), true, this.mFaceIdObserver);
                context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(FACE_RECOGNIZE_UNLOCK), true, this.mFaceIdObserver);
            } else {
                decodeFaceIdResourceAsync();
                this.mFaceIdResDecodeFlag = true;
            }
            this.mFilter.addAction("android.intent.action.LOCALE_CHANGED");
            Slog.i(TAG, "HwAodManagerService register locale change BroadcastReceiver.");
        }
        this.mFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mFilter);
        if (sIsTaurusProduct && this.mDeviceNodeFD > 0 && !mDigitalClockDecodeFlag) {
            mDigitalClockDecodeFlag = true;
            this.mOldChipSetProduct = 0;
            Slog.i(TAG, "HwAodManagerService register mDigitalClockDecodeFlag. mOldChipSetProduct:" + this.mOldChipSetProduct);
        }
        if (sIsTaurusProduct && this.mDeviceNodeFD > 0 && !mAnalogClockDecodeFlag) {
            mAnalogClockDecodeFlag = true;
            Slog.i(TAG, "HwAodManagerService set mAnalogClockDecodeFlag.");
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(AodThemeConst.AOD_THEME_VERSION), true, this.mAodThemeVersionObserver, -1);
            registerUserSwitch();
        }
        if (IS_FOLDING_SCREEN) {
            Slog.i(TAG, "HwAodManagerService register display mode listener and observer");
            registerDisplayModeObserver();
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayModeListener);
        }
        Slog.i(TAG, "HwAodManagerService constructor finished!!!mDeviceNodeFD:" + this.mDeviceNodeFD);
    }

    private int getScreenPanelIdByMode(int displayMode) {
        if (sIsTetonProduct && displayMode != 1) {
            return 1;
        }
        return 0;
    }

    private static int getDigitalDualClockImageHeight() {
        if (!isFullFoldableScreen()) {
            return dp2Px(49);
        }
        if (AodResUtil.isLowDensity()) {
            return LTET_DIGITAL_DUAL_CLOCK_IMAGE_HEIGHT_FOLD_SCREEN;
        }
        return DIGITAL_DUAL_CLOCK_IMAGE_HEIGHT_FOLD_SCREEN;
    }

    private static int getPatternClockImageWidth() {
        if (!sIsTetonProduct || (!AodResUtil.isLowDensity() && !isFullFoldableScreen())) {
            return dp2PxEx(360);
        }
        return getAlignWidth(dp2Px(360));
    }

    private static int getPatternArtClockImageWidth() {
        int width = PATTERN_ART_WIDTH_DP;
        Rect rect = getDisplaySafeInsets();
        if (rect.left != 0) {
            width = PATTERN_ART_WIDTH_DP + ((int) ((((float) rect.left) / AodResUtil.NEW_DENSITY) / 2.0f));
        }
        return getAlignWidth(dp2Px(width));
    }

    private static Rect getDisplaySafeInsets() {
        try {
            Class<?> clazz = Class.forName(CLASS_NAME_DISPLAY_SIZE_UTIL);
            Object result = clazz.getMethod(METHOD_NAME_DISPLAY_SAFE_INSETS, new Class[0]).invoke(clazz.newInstance(), new Object[0]);
            if (result instanceof Rect) {
                return (Rect) result;
            }
            return new Rect();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Slog.e(TAG, "getDisplaySafeInsets -> Exception = " + e.getMessage());
            return new Rect();
        }
    }

    private static int getPattenClockImageHeightType2() {
        if (!isFullFoldableScreen()) {
            return dp2Px(PATTERN2_HEIGHT_DP);
        }
        if (sIsTetonProduct) {
            return dp2Px(PATTERN2_HEIGHT_DP);
        }
        return PATTERN_CLOCK_IMAGE_HEIGHT_TYPE2_FOLD_SCREEN;
    }

    private static int getVerticalClockImageWidth() {
        if (!sIsTetonProduct || isFullFoldableScreen() || AodResUtil.isLowDensity()) {
            return dp2Px(360);
        }
        return TETON_MAIN_SCREEN_WIDTH;
    }

    private static boolean isRotationScreen() {
        if (sIsTahitiProduct) {
            return true;
        }
        if (!sIsTetonProduct) {
            return false;
        }
        if (HwFoldScreenManagerEx.getDisplayMode() == 1) {
            return true;
        }
        return false;
    }

    private static boolean isFullFoldableScreen() {
        return IS_FOLDING_SCREEN && isRotationScreen();
    }

    public int getAodStatus() {
        if (!checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "getAodStatus")) {
            return -2147483647;
        }
        Slog.w(TAG, "Current AOD status " + this.mState);
        return this.mState;
    }

    public void start() {
        start(true);
    }

    private void start(boolean isAsync) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "start")) {
            int i = this.mPowerState;
            if (i == 100 || i == 102) {
                releaseClockBitmapBufferResources();
                Slog.w(TAG, "Aod Service can not start as screen is on " + this.mPowerState);
                return;
            }
            int i2 = this.mState;
            if (i2 == 1) {
                Slog.w(TAG, "Aod Service has been started,can't start again");
            } else if (i2 == 0 || i2 == 2) {
                Slog.w(TAG, "Aod Service should started with : " + this.mState);
                this.mState = 1;
                Runnable startRunnable = new Runnable() {
                    /* class com.huawei.aod.$$Lambda$HwAodManagerService$4V4TrUjr_v40e5QncLUA3wyoqA */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwAodManagerService.this.lambda$start$0$HwAodManagerService();
                    }
                };
                if (isAsync) {
                    this.mHandler.post(startRunnable);
                } else {
                    startRunnable.run();
                }
            } else {
                resume(isAsync);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v5, resolved type: java.lang.StringBuilder */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public /* synthetic */ void lambda$start$0$HwAodManagerService() {
        ?? isFullFoldableScreen = isFullFoldableScreen();
        this.mLastDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
        int panelId = 0;
        if (IS_FOLDING_SCREEN) {
            panelId = getScreenPanelIdByMode(this.mLastDisplayMode);
            Slog.w(TAG, "Aod Service start with fold screen state:" + ((int) isFullFoldableScreen) + " display mode:" + this.mLastDisplayMode + " panel id:" + panelId);
        }
        nativeSetFoldScreenState(this.mAodDevicePtr, isFullFoldableScreen == true ? 1 : 0, panelId);
        nativeSetProduct(this.mAodDevicePtr, sIsLayaPorduct ? 1 : 0);
        if (setBitmapByMemoryFileInner(this.mParcelFileSize, this.mParcelFileDescriptor) == 0) {
            nativeStart(this.mAodDevicePtr);
        } else {
            Slog.e(TAG, "setBitmapByMemoryFileInner err! stop start AOD!");
        }
        this.mAodQuitType = 0;
        this.mPosReady = false;
    }

    public void configAndStart(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "configAndStart")) {
            this.mHandler.post(new Runnable(aodInfo, fileSize, pfd) {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$AfbDrTCrmPx6bjrhZuxykEZlKlQ */
                private final /* synthetic */ AodConfigInfo f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ ParcelFileDescriptor f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$configAndStart$1$HwAodManagerService(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$configAndStart$1$HwAodManagerService(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        if (!setAodConfig(aodInfo, false)) {
            Slog.w(TAG, "configAndStart failed as setAodConfig return false.");
            return;
        }
        setBitmapByMemoryFile(fileSize, pfd);
        start(false);
    }

    public void stop() {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "stop")) {
            if (this.mPowerState == 101) {
                Slog.w(TAG, "Aod Service can not stop as screen is off " + this.mPowerState);
            } else if (this.mState == 2) {
                Slog.w(TAG, "Aod Service has been stoped,can't stoped again");
            } else {
                this.mState = 2;
                resetAllBitmapNumAndSize();
            }
        }
    }

    public Bundle getAodInfo(int infoType) {
        if (!checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "getAodStatus")) {
            return null;
        }
        Bundle mCurPos = new Bundle();
        if (infoType == 1) {
            if (this.mPosReady) {
                mCurPos.putInt("PositionX", this.mPosX);
                mCurPos.putInt("PositionY", this.mPosY);
                Slog.i(TAG, "getAodPosition ready mPosX=" + this.mPosX + " mPosY=" + this.mPosY);
            } else {
                int[] pos = nativeGetPos(this.mAodDevicePtr);
                if (pos != null && pos.length == 4) {
                    mCurPos.putInt("PositionX", pos[0]);
                    mCurPos.putInt("PositionY", pos[1]);
                    Slog.i(TAG, "getAodPosition query mPosX=" + pos[0] + " mPosY=" + pos[1]);
                }
            }
        }
        return mCurPos;
    }

    public void pause() {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "pause")) {
            this.mHandler.post(new Runnable() {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$FNhXWDJgziy6inwT56C4HBclG7I */

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$pause$2$HwAodManagerService();
                }
            });
        }
    }

    public /* synthetic */ void lambda$pause$2$HwAodManagerService() {
        pause(false, 0);
    }

    public void pauseAodWithScreenState(int screenState) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "pauseAodWithScreenState")) {
            this.mHandler.post(new Runnable(screenState) {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$wvE0f2N5L9xhScZuwGmhpf9YP4 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$pauseAodWithScreenState$3$HwAodManagerService(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$pauseAodWithScreenState$3$HwAodManagerService(int screenState) {
        pause(false, screenState);
    }

    private void pause(boolean isAsync, int screenState) {
        if (!sIsSupportReconstruction) {
            if (this.mPowerState == 101) {
                Slog.w(TAG, "Aod Service can not pause as finger is down " + this.mPowerState);
                return;
            }
        } else if ((!IS_FOLDING_SCREEN || this.mAodQuitType == 0) && this.mPowerState == 101 && screenState != 1) {
            Slog.w(TAG, "Aod Service can not pause as finger is down " + this.mFingerStatus);
            return;
        }
        int i = this.mState;
        if (i == 1 || i == 4) {
            Slog.i(TAG, "Aod Service is pause now mState:" + this.mState + "mPowerState:" + this.mPowerState);
            this.mState = 3;
            Runnable pauseRunnale = new Runnable(screenState) {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$7nL7sPK6MAIBZueOpMHsRo893g */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$pause$4$HwAodManagerService(this.f$1);
                }
            };
            if (isAsync) {
                this.mHandler.post(pauseRunnale);
            } else {
                pauseRunnale.run();
            }
        } else {
            Slog.w(TAG, "Aod Service is not start, can't pause. mState:" + this.mState + " mPowerState:" + this.mPowerState);
        }
    }

    public /* synthetic */ void lambda$pause$4$HwAodManagerService(int screenState) {
        Slog.i(TAG, "Runnable for pause start, mFingerStatus" + this.mFingerStatus + " mAodQuitType:" + this.mAodQuitType);
        int i = this.mFingerStatus;
        if (i == 1 || i == 3) {
            updateFingerStatus();
            Slog.i(TAG, "pause run mFingerStatus:" + this.mFingerStatus);
            nativeSetFingerStatus(this.mAodDevicePtr, this.mFingerStatus);
        }
        Slog.i(TAG, "aodlayer pos start get pos");
        int[] pos = nativeGetPos(this.mAodDevicePtr);
        if (pos != null && pos.length == 4) {
            Slog.i(TAG, "aodlayer pos is:" + pos[0] + "," + pos[1] + "," + pos[3]);
            this.mPosX = pos[0];
            this.mPosY = pos[1];
            this.mPosReady = true;
            Settings.Secure.putInt(this.mContext.getContentResolver(), "current_base_position_y", pos[3]);
        }
        nativePause(this.mAodDevicePtr, screenState);
        resetAllBitmapNumAndSize();
    }

    public void resume() {
        resume(true);
    }

    private void resume(boolean isAsync) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "resume")) {
            int i = this.mPowerState;
            if (i == 100 || i == 102) {
                releaseClockBitmapBufferResources();
                Slog.w(TAG, "Aod Service can not resume as screen is on " + this.mPowerState);
                return;
            }
            if (this.mState != 3) {
                boolean isLastResumeFailed = true;
                if (nativeGetResumeStatus(this.mAodDevicePtr) != 1) {
                    isLastResumeFailed = false;
                }
                if (this.mState != 4 || !isLastResumeFailed) {
                    Slog.w(TAG, "Aod Service is not pause,can't resume!");
                    return;
                }
                Slog.w(TAG, "Aod Service last resume failed, continue resume");
            }
            if (sIsTetonProduct) {
                if (this.mDisplayModeChanging) {
                    Slog.w(TAG, "Aod Service can't resume as display mode is in changing state.");
                    return;
                }
                int currentDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
                if (this.mConfigScreenMode != currentDisplayMode) {
                    Slog.w(TAG, "Aod Service can't resume as display mode mismatch, mConfigScreenMode:" + this.mConfigScreenMode + " currentDisplayMode:" + currentDisplayMode);
                    return;
                }
            }
            Slog.e(TAG, "Aod Service is resume now!");
            this.mState = 4;
            Runnable resumeRunnable = new Runnable() {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$bxyc3VFdC77F7OJMsCwmH6fxJgk */

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$resume$5$HwAodManagerService();
                }
            };
            if (isAsync) {
                this.mHandler.post(resumeRunnable);
            } else {
                resumeRunnable.run();
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v7, resolved type: java.lang.StringBuilder */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public /* synthetic */ void lambda$resume$5$HwAodManagerService() {
        ?? isFullFoldableScreen = isFullFoldableScreen();
        int panelId = 0;
        if (IS_FOLDING_SCREEN) {
            this.mLastDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
            panelId = getScreenPanelIdByMode(this.mLastDisplayMode);
            Slog.w(TAG, "Aod Service resume with fold screen state:" + ((int) isFullFoldableScreen) + " display mode:" + this.mLastDisplayMode + " panel id:" + panelId);
        }
        nativeSetFoldScreenState(this.mAodDevicePtr, isFullFoldableScreen == true ? 1 : 0, panelId);
        boolean isLastResumeSucceed = nativeGetResumeStatus(this.mAodDevicePtr) == 0;
        nativeIsRtl(this.mAodDevicePtr, AodResUtil.isRtl(this.mContext));
        if (isLastResumeSucceed) {
            Slog.w(TAG, "Aod service already resumed success, no need resume again");
        } else {
            Slog.i(TAG, "Runnable for resume start!");
            if (setBitmapByMemoryFileInner(this.mParcelFileSize, this.mParcelFileDescriptor) == 0) {
                nativeResume(this.mAodDevicePtr);
            } else {
                Slog.e(TAG, "setBitmapByMemoryFileInner err! stop resume AOD!");
            }
        }
        this.mPosReady = false;
        this.mAodQuitType = 0;
        this.mDisplayModeChanging = false;
    }

    public void beginUpdate() {
        beginUpdate(true);
    }

    private void beginUpdate(boolean isAsync) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "beginUpdate")) {
            int i = this.mState;
            if (i == 1 || i == 4) {
                int i2 = this.mPowerState;
                if (i2 == 100 || i2 == 102) {
                    releaseClockBitmapBufferResources();
                    Slog.w(TAG, "Aod Service can not update as screen is on " + this.mPowerState);
                    return;
                }
                boolean forceUpdate = "0".equals(this.mCurrentConfigString[2]);
                Slog.e(TAG, "beginUpdate forceUpdate = " + forceUpdate);
                if (!checkIfStatusChanged() && !forceUpdate) {
                    Slog.e(TAG, "Aod state is not changed, don't need beginUpdate!");
                } else if (isAsync) {
                    this.mHandler.removeCallbacks(this.mUpdateRunnable);
                    this.mHandler.post(this.mUpdateRunnable);
                } else {
                    this.mUpdateRunnable.run();
                }
            } else {
                Slog.e(TAG, "Aod service is not start, can't beginUpdate!");
                if (this.mPowerState == -1) {
                    Slog.e(TAG, "mPowerState is init ,dont need to start");
                } else if (!sIsSupportReconstruction) {
                    start(isAsync);
                }
            }
        }
    }

    public void configAndUpdate(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "configAndUpdate")) {
            this.mHandler.post(new Runnable(aodInfo, fileSize, pfd) {
                /* class com.huawei.aod.$$Lambda$HwAodManagerService$rEHIf20B5URIKNT5dUaPGlshv8c */
                private final /* synthetic */ AodConfigInfo f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ ParcelFileDescriptor f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwAodManagerService.this.lambda$configAndUpdate$6$HwAodManagerService(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$configAndUpdate$6$HwAodManagerService(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        if (!setAodConfig(aodInfo, false)) {
            Slog.w(TAG, "configAndUpdate failed as setAodConfig return false.");
            return;
        }
        setBitmapByMemoryFile(fileSize, pfd);
        beginUpdate(false);
    }

    public void endUpdate() {
    }

    public void setAodConfig(AodConfigInfo aodInfo) {
        setAodConfig(aodInfo, true);
    }

    private boolean setAodConfig(AodConfigInfo aodInfo, boolean isAsync) {
        if (!checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "setAodConfig") || aodInfo == null) {
            return false;
        }
        String[] strArr = this.mCurrentConfigString;
        strArr[0] = aodInfo.mSecondTimeZone + "";
        String[] strArr2 = this.mCurrentConfigString;
        strArr2[1] = aodInfo.mStatusString + "";
        String[] strArr3 = this.mCurrentConfigString;
        strArr3[2] = aodInfo.mForceUpdate + "";
        nativeSetAodConfigInfo(this.mAodDevicePtr, aodInfo);
        this.mClockMode = aodInfo.mDualClock;
        this.mAODWorkMode = aodInfo.mAODWorkMode;
        this.mDisplayMode = aodInfo.mDisplayMode;
        currrentColorFlag = aodInfo.mCurrentColorFlag;
        homeColorFlag = aodInfo.mHomeColorFlag;
        this.mNotificationOffset = aodInfo.mNotificationOffset;
        this.mIsTimeFresh = aodInfo.mIsTimeRefresh;
        int i = mFingerPrintCircleRadius;
        if (i < 0) {
            i = 95;
        }
        mFingerPrintCircleRadius = i;
        Slog.i(TAG, toConfiginfoString(aodInfo));
        this.aodInfoRect = aodInfo.mAodItemRect;
        this.aodinfoTextHeight = aodInfo.mClockTextArrayAreaHeight;
        this.aodinfoTextWidth = aodInfo.mClockTextWidth;
        this.mChargeTips = aodInfo.mChargeTips;
        this.mConfigScreenMode = aodInfo.mScreenMode;
        if (this.mDisplayMode == 6) {
            getVerticalDisplayItems(aodInfo);
        }
        int i2 = this.mPowerState;
        if (i2 == 100 || i2 == 102) {
            Slog.i(TAG, "AodConfig can not set as screen is on " + this.mPowerState);
            return false;
        }
        Slog.i(TAG, "setAodConfig enter mDisplayMode:" + this.mDisplayMode + " mState:" + this.mState);
        int i3 = this.mState;
        if (i3 == 1 || i3 == 4) {
            if (isAsync) {
                this.mHandler.removeCallbacks(this.mUpdateDynamicBitmapRunnable);
                this.mHandler.post(this.mUpdateDynamicBitmapRunnable);
            } else {
                this.mUpdateDynamicBitmapRunnable.run();
            }
        } else if (isAsync) {
            this.mHandler.removeCallbacks(this.mSetDynamicBitmapRunnable);
            this.mHandler.post(this.mSetDynamicBitmapRunnable);
        } else {
            this.mSetDynamicBitmapRunnable.run();
        }
        if (sIsSupportReconstruction) {
            this.mTriggerList = aodInfo.getTriggers();
            List<Trigger> list = this.mTriggerList;
            if (list != null) {
                this.mTirggerSize = list.size();
                this.mActionDataSize = this.mTriggerList.stream().mapToInt($$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA.INSTANCE).sum();
                Slog.e(TAG, "setBitmapByMemoryFileInnerm TirggerSize : " + this.mTirggerSize + " mActionDataSize: " + this.mActionDataSize);
                StringBuilder sb = new StringBuilder();
                sb.append("list: ");
                sb.append(this.mTriggerList);
                Slog.e(TAG, sb.toString());
            }
        }
        return true;
    }

    public void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) throws RemoteException {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "updateAodVolumeInfo") && aodVolumeInfo != null) {
            nativeUpdateAodVolumeInfo(this.mAodDevicePtr, aodVolumeInfo);
        }
    }

    public void setBitmapByMemoryFile(int fileSize, ParcelFileDescriptor pfd) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "setBitmapByMemoryFile")) {
            if (pfd == null || fileSize <= 0) {
                Slog.e(TAG, "setBitmapByMemoryFile with null pfd with size : " + fileSize);
                return;
            }
            this.mParcelFileSize = fileSize;
            this.mParcelFileDescriptor = pfd;
        }
    }

    private void resetAllBitmapNumAndSize() {
        this.mDynamicBitmapNum = 0;
        this.mSingleDynamicBitmapBufferSize = 0;
        this.mFaceIdBitmapNum = 0;
        this.mSingleFaceIdBitmapBufferSize = 0;
        this.mPorscheImageNum = 0;
        this.mSinglePorscheImageBufferSize = 0;
        this.mDigitalClockBitmapNum = 0;
        this.mSingleDigitalClockBufferSize = 0;
        this.mPatternClockBitmapNum = 0;
        this.mPatternClockBufferSize = 0;
        this.mSingleAnalogClockHourBitmapNum = 0;
        this.mSingleAnalogClockHourBufferSize = 0;
        this.mVolumeBarBitmapNum = 0;
        this.mVolumeBarBufferSize = 0;
        this.mVolumeIconBitmapNum = 0;
        this.mVolumeIconBufferSize = 0;
        this.mVerticalClockBitmapNum = 0;
        this.mVerticalClockBufferSize = 0;
        this.mAnimationBitmapNum = 0;
        this.mAnimationBufferSize = 0;
        this.mBaseTimeAreaBufferSize = 0;
        this.mBaseTimeAreaBitmapNum = 0;
        this.mNumberAreaBufferSize = 0;
        this.mNumberAreaBitmapNum = 0;
        this.mFingerAreaBufferSize = 0;
        this.mFingerAreaBitmapNum = 0;
        this.mChargeTipsBitmapBufferSize = 0;
        this.mChargeTipsBitmapNum = 0;
    }

    private void getVerticalDisplayItems(AodConfigInfo aodInfo) {
        if (this.mClockMode == 0) {
            this.mVerticalDisplayNumber.left = aodInfo.mAodItemRect[24].left;
            this.mVerticalDisplayNumber.top = aodInfo.mAodItemRect[24].top;
            return;
        }
        this.mVerticalDisplayNumber.left = aodInfo.mAodItemRect[33].left;
        this.mVerticalDisplayNumber.top = aodInfo.mAodItemRect[33].top;
        this.mVerticalDisplayNumber.right = aodInfo.mAodItemRect[33].right;
        this.mVerticalDisplayNumber.bottom = aodInfo.mAodItemRect[33].bottom;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int setBitmapByMemoryFileInner(int fileSize, ParcelFileDescriptor pfd) {
        if (!checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "setBitmapByMemoryFile")) {
            return -1;
        }
        if (pfd == null || fileSize <= 0) {
            Slog.e(TAG, "setBitmapByMemoryFileInner with null pfd with size : " + fileSize);
            return -1;
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        if (fd == null) {
            Slog.e(TAG, "setBitmapByMemoryFileInner with null fd ");
            return -1;
        }
        Slog.i(TAG, "setBitmapByMemoryFileInner enter. fileSize = " + fileSize);
        FileInputStream fileInputStream = new FileInputStream(fd);
        byte[] contentBytes = new byte[fileSize];
        try {
            int length = fileInputStream.read(contentBytes);
            if (length != -1) {
                Slog.e(TAG, "setBitmapByMemoryFileInner read succeed. length =  " + length);
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Slog.e(TAG, "setBitmapByMemoryFileInner FileInputStream close IOException happen!");
                }
                this.mBitmapBuffer = contentBytes;
                int result = setBasePictureBitmap();
                this.mParcelFileDescriptor = null;
                this.mParcelFileSize = 0;
                Slog.e(TAG, "setBitmapByMemoryFileInner result:" + result);
                return result;
            }
            Slog.i(TAG, "setBitmapByMemoryFileInner read line end error .length =  " + length);
            try {
                fileInputStream.close();
            } catch (IOException e2) {
                Slog.e(TAG, "setBitmapByMemoryFileInner FileInputStream close IOException happen!");
            }
            return -1;
        } catch (IOException e3) {
            Slog.e(TAG, "setBitmapByMemoryFileInner FileInputStream read IOException happen!");
            fileInputStream.close();
        } catch (Throwable th) {
            try {
                fileInputStream.close();
            } catch (IOException e4) {
                Slog.e(TAG, "setBitmapByMemoryFileInner FileInputStream close IOException happen!");
            }
            throw th;
        }
    }

    private int setBasePictureBitmap() {
        int result = 0;
        if (!sIsSupportReconstruction) {
            Slog.i(TAG, "this is not BaltimoProduct, return. ");
            return 0;
        }
        int i = this.mState;
        if (i == 1 || i == 4) {
            if ((this.mAODWorkMode & 1) == 1) {
                int result2 = handleBaseTimeCachedResources();
                if (result2 != 0) {
                    return result2;
                }
                result = handleNumberCachedResources();
                if (result != 0) {
                    return result;
                }
            }
            if ((this.mAODWorkMode & 2) == 2 && (result = handleFingerCachedResources()) != 0) {
                return result;
            }
        }
        return result;
    }

    private int handleBaseTimeCachedResources() {
        if (this.mBitmapBuffer == null || this.aodInfoRect == null) {
            Slog.i(TAG, "mBitmapBuffer is null. ");
        }
        if (this.mBaseTimeAreaBitmapNum >= 1) {
            Slog.w(TAG, "mBaseTimeAreaBitmapNum out of bounds " + this.mBaseTimeAreaBitmapNum);
            return 0;
        }
        int baseAodWidth = this.aodInfoRect[0].right - this.aodInfoRect[0].left;
        this.mBaseTimeAreaBufferSize = formatHeight(baseAodWidth) * (this.aodInfoRect[0].bottom - this.aodInfoRect[0].top) * 2;
        Slog.i(TAG, "handleBaseTimeCachedResources is  " + this.mBaseTimeAreaBufferSize + ", mBaseTimeAreaBitmapNum : " + this.mBaseTimeAreaBitmapNum);
        int i = this.mBaseTimeAreaBufferSize;
        this.mBaseTimeAreaBitmapBuffer = new byte[i];
        System.arraycopy(this.mBitmapBuffer, 0, this.mBaseTimeAreaBitmapBuffer, 0, i);
        int result = nativeSetBaseTimeAreaBitmap(this.mAodDevicePtr, this.mBaseTimeAreaBitmapNum);
        if (result == 0) {
            Slog.i(TAG, "current SUCC num is " + (this.mBaseTimeAreaBitmapNum + 1));
            this.mBaseTimeAreaBitmapNum = this.mBaseTimeAreaBitmapNum + 1;
        }
        return result;
    }

    private int handleNumberCachedResources() {
        if (this.mBitmapBuffer == null || this.aodInfoRect == null) {
            Slog.i(TAG, "mBitmapBuffer is null. ");
        }
        if (this.mNumberAreaBitmapNum >= 1) {
            Slog.w(TAG, "mNumberAreaBitmapNum out of bounds " + this.mNumberAreaBitmapNum);
            return 0;
        }
        this.mNumberAreaBufferSize = this.aodinfoTextHeight * this.aodinfoTextWidth * 10 * 2;
        int i = this.mNumberAreaBufferSize;
        this.mNumberAreaBitmapBuffer = new byte[i];
        System.arraycopy(this.mBitmapBuffer, this.mBaseTimeAreaBufferSize, this.mNumberAreaBitmapBuffer, 0, i);
        Slog.w(TAG, "handleNumberCachedResources , mBaseTimeAreaBufferSize " + this.mBaseTimeAreaBufferSize + ", mNumberAreaBufferSize is " + this.mNumberAreaBufferSize + ", mNumberAreaBitmapNum is " + this.mNumberAreaBitmapNum);
        int result = nativeSetNumAreaBitmap(this.mAodDevicePtr, this.mNumberAreaBitmapNum);
        if (result == 0) {
            Slog.i(TAG, " current SUCC num is " + (this.mNumberAreaBitmapNum + 1));
            this.mNumberAreaBitmapNum = this.mNumberAreaBitmapNum + 1;
        }
        return result;
    }

    private int handleFingerCachedResources() {
        int fpLogoIndex;
        if (this.mBitmapBuffer == null || this.aodInfoRect == null) {
            Slog.i(TAG, "mBitmapBuffer is null. ");
        }
        int baseAodWidth = this.aodInfoRect[0].right - this.aodInfoRect[0].left;
        int baseTimeLength = getAlignWidth(baseAodWidth) * (this.aodInfoRect[0].bottom - this.aodInfoRect[0].top) * 2;
        int numberLength = this.aodinfoTextHeight * this.aodinfoTextWidth * 10 * 2;
        if (this.mClockMode == 0) {
            fpLogoIndex = 5;
        } else {
            fpLogoIndex = 9;
        }
        this.mFingerAreaBufferSize = (this.aodInfoRect[fpLogoIndex].right - this.aodInfoRect[fpLogoIndex].left) * (this.aodInfoRect[fpLogoIndex].bottom - this.aodInfoRect[fpLogoIndex].top) * 2;
        int i = 4;
        int offset = baseTimeLength + numberLength + 4;
        this.mFingerAreaBitmapBuffer = new byte[this.mFingerAreaBufferSize];
        int i2 = 0;
        while (true) {
            if (i2 >= i) {
                break;
            }
            byte[] bArr = this.mBitmapBuffer;
            int i3 = this.mFingerAreaBufferSize;
            System.arraycopy(bArr, (i3 * i2) + offset, this.mFingerAreaBitmapBuffer, 0, i3);
            if (this.mFingerAreaBitmapBuffer == null) {
                Slog.w(TAG, "handleVolumeBarImageResources single clock bitmap not exist " + i2);
                break;
            } else if (this.mFingerAreaBitmapNum >= 4) {
                Slog.w(TAG, "mNumberAreaBitmapNum out of bounds " + this.mNumberAreaBitmapNum);
                return 0;
            } else {
                Slog.w(TAG, "handleFingerCachedResources is  " + this.mFingerAreaBufferSize);
                int result = nativeSetFingerAreaBitmap(this.mAodDevicePtr, this.mFingerAreaBitmapNum);
                if (result != 0) {
                    return result;
                }
                Slog.i(TAG, "RESULT_OK index is " + i2 + ", current SUCC num is " + (this.mFingerAreaBitmapNum + 1));
                this.mFingerAreaBitmapNum = this.mFingerAreaBitmapNum + 1;
                i2++;
                i = 4;
            }
        }
        return 0;
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    private void startAodServiceBroadcast() {
        if (this.mContext != null) {
            Intent startIntent = new Intent(AOD_SERVICE_ACTION);
            startIntent.setComponent(new ComponentName(AOD_PACKAGE_NAME, AOD_SERVICE_NAME));
            this.mContext.startService(startIntent);
            Slog.i(TAG, "Start send service.");
        }
    }

    public int getDeviceNodeFD() {
        if (!checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "getDeviceNodeFD")) {
            return -2147483647;
        }
        return this.mDeviceNodeFD;
    }

    public void setPowerState(int powerState) {
        int i;
        int i2;
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "setPowerState")) {
            if (getDeviceNodeFD() <= 0) {
                Slog.e(TAG, "with invaild deviceid");
                return;
            }
            Slog.w(TAG, "setPowerState new mPowerState = " + this.mPowerState + ", powerState = " + powerState + " , mFingerStatus = " + this.mFingerStatus);
            if (powerState == 0) {
                nativeSetDisplayScreenStatus(this.mAodDevicePtr, 0);
            } else if (powerState == 1) {
                nativeSetDisplayScreenStatus(this.mAodDevicePtr, 1);
            } else if (powerState == 11) {
                int i3 = this.mState;
                if (i3 != 2 && i3 != 3) {
                    this.mFingerStatus = 1;
                    nativeSetFingerStatus(this.mAodDevicePtr, 1);
                }
            } else if (powerState == 10) {
                this.mFingerStatus = 0;
                nativeSetFingerStatus(this.mAodDevicePtr, 0);
            } else if (powerState == 12) {
                if ((sIsTetonProduct || sIsTahitiProduct) && sIsSupportReconstruction && this.mAodQuitType == 1) {
                    Slog.i(TAG, "teton, not pause when sceen is unfolding.!");
                    return;
                }
                this.mFingerStatus = 2;
                Slog.i(TAG, "nativeSetFingerStatus FINGER_AUTH_SUCCESS_STATUS!");
                nativeSetFingerStatus(this.mAodDevicePtr, 2);
                this.mPowerState = 100;
                pause();
            } else if (powerState == 13) {
                if (!(this.mFingerStatus == 3 || (i2 = this.mState) == 3 || i2 == 2)) {
                    this.mFingerStatus = 3;
                    nativeSetFingerStatus(this.mAodDevicePtr, 3);
                }
                if (this.mPowerState == 101) {
                    Slog.i(TAG, " Finger auth error, regist again. mPowerState = " + this.mPowerState);
                    startAodServiceBroadcast();
                }
            } else if (powerState != 14) {
                if (powerState == 101) {
                    nativeSetDisplayScreenStatus(this.mAodDevicePtr, 1);
                }
                if (powerState == 100) {
                    this.mFingerStatus = 2;
                    nativeSetFingerStatus(this.mAodDevicePtr, 2);
                }
                if (powerState == 102 && ((i = this.mFingerStatus) == 1 || i == 3)) {
                    Slog.i(TAG, "FINGER_DOWN_STATUS return !");
                } else {
                    this.mPowerState = powerState;
                }
            }
        }
    }

    private boolean checkIfStatusChanged() {
        Slog.w(TAG, "checkIfStatusChanged");
        if (checkIfConfigIsNull()) {
            Slog.w(TAG, "checkIfStatusChanged mLastConfigString is null");
            updateConfigString(true);
            return true;
        }
        String[] strArr = this.mLastConfigString;
        if (strArr[0] == null || strArr[0].equals(this.mCurrentConfigString[0])) {
            String[] lastStatus = this.mLastConfigString[1].split(AwarenessInnerConstants.COLON_KEY);
            String[] currentStatus = this.mCurrentConfigString[1].split(AwarenessInnerConstants.COLON_KEY);
            Slog.w(TAG, "checkIfStatusChanged last status is " + this.mLastConfigString[1] + ", current status is " + this.mCurrentConfigString[1] + ", length = " + currentStatus.length);
            if (lastStatus.length != currentStatus.length) {
                updateConfigString(true);
                return true;
            }
            int length = currentStatus.length;
            for (int i = 0; i < length; i++) {
                if (!lastStatus[i].equals(currentStatus[i])) {
                    updateConfigString(true);
                    return true;
                }
            }
            updateConfigString(false);
            return false;
        }
        Slog.w(TAG, "checkIfStatusChanged time zone changed from " + this.mLastConfigString[0] + " to " + this.mCurrentConfigString[0]);
        updateConfigString(true);
        return true;
    }

    private void updateConfigString(boolean configChanged) {
        String[] strArr = this.mLastConfigString;
        String[] strArr2 = this.mCurrentConfigString;
        strArr[0] = strArr2[0];
        strArr[1] = strArr2[1];
        strArr[2] = strArr2[2];
        this.mConfigChanged = configChanged;
    }

    private boolean checkIfConfigIsNull() {
        String[] strArr = this.mLastConfigString;
        if (strArr == null || this.mCurrentConfigString == null) {
            return true;
        }
        int lengh = strArr.length;
        for (int i = 0; i < lengh; i++) {
            String[] strArr2 = this.mLastConfigString;
            if (strArr2[i] == null || "".equals(strArr2[i])) {
                return true;
            }
        }
        int lengh2 = this.mCurrentConfigString.length;
        for (int i2 = 0; i2 < lengh2; i2++) {
            String[] strArr3 = this.mCurrentConfigString;
            if (strArr3[i2] == null || "".equals(strArr3[i2])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decodeFpResource() {
        Slog.i(TAG, "decodeFpResource begin!!!");
        Context aodContext = null;
        try {
            aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
        if (aodContext == null) {
            Slog.w(TAG, "can not decode resource as aodContext is null");
            return;
        }
        long time = System.currentTimeMillis();
        loadFpThemeResource();
        if (this.mAnimFileNames.isEmpty()) {
            int size = this.RES_IDS.length;
            for (int i = 0; i < size; i++) {
                decodeResource(aodContext, i);
            }
        } else {
            for (int i2 = 0; i2 < 12; i2++) {
                decodeResourceFromPath(i2);
            }
        }
        Slog.i(TAG, "decodeResource Async finish!!!, time total = " + (System.currentTimeMillis() - time));
    }

    private void loadFpThemeResource() {
        this.mAnimFileNames.clear();
        String fpAnimDir = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), FINGER_PRINT_ANIM_TYPE, ActivityManager.getCurrentUser());
        if (fpAnimDir == null) {
            Slog.e(TAG, "fpAnimDir is null");
            return;
        }
        predecodeResourceFromPath(FP_ANIM_PATH + fpAnimDir + FP_ANIM_AOD);
    }

    private void predecodeResourceFromPath(String path) {
        try {
            File canonicalFiles = new File(path).getCanonicalFile();
            String canonicalPath = canonicalFiles.getPath();
            if (!canonicalPath.startsWith(FP_ANIM_PATH)) {
                Slog.e(TAG, "current path is error,need use default");
            } else if (!canonicalFiles.exists()) {
                Slog.w(TAG, "predecodeResourceFromPath the folder does not exist");
            } else {
                String[] fileAnimNames = canonicalFiles.list();
                if (fileAnimNames == null) {
                    Slog.e(TAG, "The fileAnimNames is null,use default");
                    return;
                }
                String fpBlackRes = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), FP_BALCK_RES, ActivityManager.getCurrentUser());
                if (fpBlackRes == null) {
                    Slog.e(TAG, "The fpAnimBlackRes is null,use default");
                    return;
                }
                Slog.i(TAG, "The fpAnimBlackRes length is " + fpBlackRes.length() + "fileAnimNames length is " + fileAnimNames.length);
                sortAndLoadFpAnimRes(fileAnimNames, canonicalPath, fpBlackRes);
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Read files has SecurityException");
        } catch (IOException e2) {
            Slog.e(TAG, "getCanonicalFile files has IOException");
        }
    }

    private void sortAndLoadFpAnimRes(String[] fileAnimNames, String canonicalPath, String fpBlackRes) {
        List<String> fpBlackResNames = Arrays.asList(fpBlackRes.split(","));
        for (String fileAnimName : fileAnimNames) {
            if (fpBlackResNames.contains(fileAnimName)) {
                this.mAnimFileNames.add(canonicalPath + File.separator + fileAnimName);
            }
            if (this.mAnimFileNames.size() == 12) {
                Collections.sort(this.mAnimFileNames);
                return;
            }
        }
        if (this.mAnimFileNames.size() < 12) {
            Slog.e(TAG, "The picture is less than default count");
            this.mReloadThemeFlag = true;
            this.mAnimFileNames.clear();
        }
    }

    private boolean isNeedReload(Bitmap[] bitmap, int bitmapMaxNum, int bitmapType) {
        if (bitmapMaxNum > bitmap.length || bitmapMaxNum <= 0 || bitmap.length <= 0) {
            Slog.i(TAG, "isNeedReload, bitmapMaxNum = " + bitmapMaxNum + "  bitmap.length =  " + bitmap.length);
            return false;
        }
        int clockType = AodThemeManager.getInstance().getClockType();
        Slog.i(TAG, "isNeedReload clockType:" + clockType + " bitmapType:" + bitmapType);
        if (AodThemeConst.isNeedReloadBitmapType(bitmapType) && AodThemeConst.isNeedReloadClockType(clockType)) {
            Slog.i(TAG, "user custom background, reload always");
            return true;
        } else if (!sIsTahitiProduct && IS_FOLDING_SCREEN && this.mAodQuitType != 0) {
            Slog.i(TAG, "display screen changed, reload always");
            return true;
        } else if (!sIsTetonProduct || bitmapType != 6) {
            for (int i = 0; i < bitmapMaxNum; i++) {
                if (bitmap[i] == null) {
                    return true;
                }
            }
            return false;
        } else {
            Slog.i(TAG, "foldable screen, digital clock bitmap reload always.");
            return true;
        }
    }

    private void needReloadBitmapResource(int bitmapType) {
        Slog.i(TAG, "needReloadBitmapResource, bitmapType = " + bitmapType);
        if (bitmapType != 2) {
            switch (bitmapType) {
                case 6:
                    if (isNeedReload(this.mDigitalClockBitmap, 1, bitmapType)) {
                        doDecodeDigitalImageResource();
                        return;
                    }
                    return;
                case 7:
                    if (isNeedReload(this.mSingleAnalogHourBitmap, 2, bitmapType)) {
                        getAnologAndBackgroundResDecode();
                        return;
                    }
                    return;
                case 8:
                    if (isNeedReload(this.mPatternClockBitmap, 1, bitmapType)) {
                        getAnologAndBackgroundResDecode();
                        return;
                    }
                    return;
                case 9:
                case 10:
                    if (isNeedReload(this.mRingVolumeBitmap, 8, bitmapType)) {
                        decodeRingVolumeBarResource();
                        return;
                    }
                    return;
                case 11:
                    if (this.mBgFgdisplayPos == -1) {
                        getAnologAndBackgroundResDecode();
                        return;
                    }
                    int maxSingleVerticalClockImageNum = getMaxSingleVerticalClockImageNum();
                    int maxDualVerticalClockImageNum = getMaxDualVerticalClockImageNum();
                    if (maxSingleVerticalClockImageNum == 0 || maxDualVerticalClockImageNum == 0) {
                        Slog.w(TAG, "unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
                        return;
                    } else if (isNeedReload(this.mVerticalClockBitmap, maxSingleVerticalClockImageNum, bitmapType)) {
                        decodeVerticalClockBgFgResource(maxSingleVerticalClockImageNum, maxDualVerticalClockImageNum);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else if (isNeedReload(this.mBitmap, 12, bitmapType) || this.mReloadThemeFlag) {
            this.mReloadThemeFlag = false;
            decodeFpResource();
        }
    }

    private int getMaxSingleVerticalClockImageNum() {
        int i = this.mBgFgdisplayPos;
        if (i == 1) {
            return 8;
        }
        if (i == 2) {
            return 22;
        }
        if (i == 0) {
            return 4;
        }
        if (i == 3) {
            return 2;
        }
        if (i == 4) {
            return 4;
        }
        return 0;
    }

    private int getMaxDualVerticalClockImageNum() {
        int i = this.mBgFgdisplayPos;
        if (i == 1) {
            return 14;
        }
        if (i == 2) {
            return 42;
        }
        if (i == 0) {
            return 6;
        }
        if (i == 3) {
            return 4;
        }
        if (i == 4) {
            return 6;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @TargetApi(11)
    private void decodeFaceIdResourceAsync() {
        new Thread(this.mDecodeFaceIdResourceRunnable).start();
    }

    @TargetApi(11)
    private void decodePorscheImageResourceAsync() {
        new Thread(this.mDecodePorscheImageResourceRunnable).start();
    }

    private void decodeRingVolumeBarResource() {
        Slog.i(TAG, "decodeRingVolumeBarResource begin");
        Context aodContext = null;
        try {
            aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
        if (aodContext == null) {
            Slog.w(TAG, "can not decode resource as aodContext is null");
            return;
        }
        long beginTime = System.currentTimeMillis();
        int size = RES_IDS_VOLUME_BAR.length;
        for (int i = 0; i < size; i++) {
            decodeRingVolumeBarResource(aodContext, i);
        }
        long endTime = System.currentTimeMillis();
        Slog.i(TAG, "decodeRingVolumeBarResource finish!!!, time total = " + (endTime - beginTime));
    }

    private void decodeRingVolumeBarResource(Context context, int index) {
        Slog.i(TAG, "decodeRingVolumeBarResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            Drawable drawable = context.getResources().getDrawableForDensity(context.getResources().getIdentifier(RES_IDS_VOLUME_BAR[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mRingVolumeBitmap[index]);
                this.mRingVolumeBitmap[index] = VolumeDrawableToBitmap(drawable, index, Bitmap.Config.ARGB_8888);
                this.mRingVolumeBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeRingVolumeBarResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mRingVolumeBitmap[index].getDensity() + ", RES_ID_PORSCHEIMAGE_XXHDPI:" + RES_IDS_VOLUME_BAR[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "resource not found");
        } catch (Exception e2) {
            Slog.i(TAG, "exception");
        }
    }

    private static void savePicToSdcard(Bitmap b, String strFileName) {
        if (b == null) {
            Slog.e(TAG, "savePicToSdcard,bitmap is null");
            return;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            try {
                fos.close();
            } catch (IOException e) {
                Slog.e(TAG, "Failed to close FileOutputStream.");
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "FileNotFoundException." + e2);
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e3) {
            Slog.e(TAG, "IOException." + e3);
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "Failed to close FileOutputStream.");
                }
            }
            throw th;
        }
    }

    private void doDecodeDigitalImageResource() {
        Slog.i(TAG, "mDecodeDigitalImageResourceRunnable Async begin!!!");
        Context aodContext = null;
        try {
            aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
        if (aodContext == null) {
            Slog.w(TAG, "can not decode resource as aodContext is null");
            return;
        }
        long time = System.currentTimeMillis();
        int size = RES_IDS_DIGITAL_SINGLE_CLOCK_XXHDPI.length;
        for (int i = 0; i < size; i++) {
            decodeDigitalImageResource(aodContext, i);
        }
        int size2 = RES_IDS_DIGITAL_DOUBLE_CLOCK_XXHDPI.length;
        for (int i2 = 0; i2 < size2; i2++) {
            decodeDualDigitalImageResource(aodContext, i2);
        }
        Slog.i(TAG, "decodeDigitalImageResource Async finish!!!, time total = " + (System.currentTimeMillis() - time));
    }

    private void decodeVerticalClockBgFgResource(int maxSingleVerticalClockImageNum, int maxDualVerticalClockImageNum) {
        Slog.i(TAG, "decodeVerticalClockBgFgResource Async begin!!!");
        Context aodContext = null;
        try {
            aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
        if (aodContext == null) {
            Slog.w(TAG, "can not decode resource as aodContext is null");
            return;
        }
        this.mIsPreSet = AodThemeManager.getInstance().getIsPreSet();
        long time = System.currentTimeMillis();
        for (int i = 0; i < maxSingleVerticalClockImageNum; i++) {
            decodeVerticalDigitalImageResource(aodContext, i);
        }
        for (int i2 = 0; i2 < maxDualVerticalClockImageNum; i2++) {
            decodeVerticalDualDigitalImageResource(aodContext, i2);
        }
        Slog.i(TAG, "decodeVerticalClockBgFgResource Async finish!!!, time total = " + (System.currentTimeMillis() - time));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getAnologAndBackgroundResDecode() {
        Slog.i(TAG, "mDecodeAnalogImageResourceRunnable Async begin!!!");
        Context aodContext = null;
        try {
            Slog.i(TAG, "mDecodeAnalogImageResourceRunnable with uid : " + ActivityManager.getCurrentUser());
            aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
            AodThemeManager.getInstance().initPreferenceManager(aodContext);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "package not exist " + e.toString());
        }
        if (aodContext == null) {
            Slog.w(TAG, "can not decode resource as aodContext is null");
            return;
        }
        this.mIsPreSet = AodThemeManager.getInstance().getIsPreSet();
        this.mIsProductCustomized = AodThemeManager.getInstance().getProductCustomized();
        this.mIsVmallCustomized = AodThemeManager.getInstance().isVmallCustomized();
        int clockThemeType = AodThemeManager.getInstance().getClockThemeType();
        if (clockThemeType == 4) {
            doBackgroundResDecode(aodContext);
        } else if (clockThemeType == 3) {
            doAnalogClockResDecode(aodContext);
        } else if (clockThemeType == 5) {
            this.mBgFgdisplayPos = AodThemeManager.getInstance().getVerticalBgFgDisplayPos();
            int maxSingleVerticalClockImageNum = getMaxSingleVerticalClockImageNum();
            int maxDualVerticalClockImageNum = getMaxDualVerticalClockImageNum();
            if (maxSingleVerticalClockImageNum == 0 || maxDualVerticalClockImageNum == 0) {
                Slog.w(TAG, "unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
                return;
            }
            decodeVerticalClockBgFgResource(maxSingleVerticalClockImageNum, maxDualVerticalClockImageNum);
        } else {
            Slog.w(TAG, "current clockThemeType is " + clockThemeType);
        }
    }

    private void doBackgroundResDecode(Context aodContext) {
        String digitalBgName;
        if (aodContext == null) {
            Slog.w(TAG, "doBackgroundResDecode aodContext is null");
            return;
        }
        long time = System.currentTimeMillis();
        int digitalBgId = 0;
        int clockType = AodThemeManager.getInstance().getClockType();
        if (clockType == 102 || clockType == 1021) {
            digitalBgName = AodThemeManager.getInstance().getDigitalBackgroundResNames(this.mClockMode != 0);
        } else if (clockType == 103) {
            digitalBgId = AodThemeManager.getInstance().getDigitalBackgroundResId();
            digitalBgName = AodThemeManager.getInstance().getDigitalBackgroundResName();
        } else if (!AodThemeConst.isAodSignature(clockType)) {
            digitalBgId = AodThemeManager.getInstance().getDigitalBackgroundResId();
            digitalBgName = AodThemeManager.getInstance().getDigitalBackgroundResName();
        } else if (clockType == 110) {
            digitalBgName = "art_signature_" + getSignaturePicIndex(aodContext);
        } else {
            digitalBgName = "art_signature_0";
        }
        decodePatternClockImageResource(aodContext, digitalBgId, digitalBgName);
        Slog.i(TAG, "doBackgroundResDecode Async finish!!!, time total = " + (System.currentTimeMillis() - time));
    }

    private void initAnalogCLockRes() {
        for (int index = 0; index < 6; index++) {
            recycleBitmapBeforeReAssign(this.mSingleAnalogHourBitmap[index]);
            this.mSingleAnalogHourBitmap[index] = null;
        }
        for (int index2 = 0; index2 < 6; index2++) {
            recycleBitmapBeforeReAssign(this.mDualAnalogHourBitmap[index2]);
            this.mDualAnalogHourBitmap[index2] = null;
        }
    }

    private void doAnalogClockResDecode(Context aodContext) {
        int sizeDouble;
        int i;
        int[] idsDouble;
        if (aodContext == null) {
            Slog.w(TAG, "doAnalogClockResDecode aodContext is null");
            return;
        }
        initAnalogCLockRes();
        boolean isHaveSecond = AodThemeManager.getInstance().isHaveSecondHand();
        Slog.w(TAG, "doAnalogClockResDecode second flag: " + isHaveSecond);
        long time = System.currentTimeMillis();
        int[] ids = AodThemeManager.getInstance().getSingleAnalogClockIds();
        String[] names = AodThemeManager.getInstance().getSingleAnalogClockNames();
        int size = ids.length;
        if (!isHaveSecond) {
            size--;
        }
        for (int i2 = 0; i2 < size; i2++) {
            try {
                try {
                    decodeSingleAnalogClockImageResource(aodContext, i2, ids[i2], names[i2]);
                } catch (Exception e) {
                }
            } catch (Exception e2) {
                Slog.w(TAG, "decodeAnalogClockImageResource can not get resource from id");
            }
        }
        int[] idsDouble2 = AodThemeManager.getInstance().getDoubleAnalogClockIds();
        String[] namesDouble = AodThemeManager.getInstance().getDoubleAnalogClockNames();
        boolean isSameRes = isHaveSecond ? false : AodResUtil.getDualAnalogClockSame(idsDouble2);
        int sizeDouble2 = idsDouble2.length;
        if (isSameRes) {
            sizeDouble2 /= 2;
        }
        int sizeDouble3 = isHaveSecond ? sizeDouble2 : sizeDouble2 - 1;
        int i3 = 0;
        while (i3 < sizeDouble3) {
            try {
                i = i3;
                sizeDouble = sizeDouble3;
                idsDouble = idsDouble2;
                try {
                    decodeDualAnalogClockImageResource(aodContext, i3, idsDouble2[i3], namesDouble[i3], isSameRes);
                } catch (Exception e3) {
                }
            } catch (Exception e4) {
                i = i3;
                sizeDouble = sizeDouble3;
                idsDouble = idsDouble2;
                Slog.w(TAG, "decodeDualAnalogClockImageResource can not get resource from id");
                i3 = i + 1;
                idsDouble2 = idsDouble;
                sizeDouble3 = sizeDouble;
            }
            i3 = i + 1;
            idsDouble2 = idsDouble;
            sizeDouble3 = sizeDouble;
        }
        Slog.i(TAG, "mDecodeAnalogImageResourceRunnable Async finish!!!, time total = " + (System.currentTimeMillis() - time));
    }

    private void registerUserSwitch() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                /* class com.huawei.aod.HwAodManagerService.AnonymousClass9 */

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
                    HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeAnalogImageResourceRunnable);
                    HwAodManagerService.this.mHandler.removeCallbacks(HwAodManagerService.this.mDecodeResourceRunnable);
                    HwAodManagerService.this.mHandler.post(HwAodManagerService.this.mDecodeResourceRunnable);
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.w(TAG, "registerUserSwitch failed to listen for user switch");
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable, Bitmap.Config color) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, color);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private static int formatHeight(int height) {
        return height % 8 == 0 ? height : ((height / 8) * 8) + 8;
    }

    private static Bitmap drawableToBitmapForFoldScreen(Drawable drawable, Bitmap.Config color) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        int height = formatHeight(drawable.getIntrinsicHeight());
        int heightOffset = (height - drawable.getIntrinsicHeight()) / 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, color);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, heightOffset, width, height - heightOffset);
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap VolumeDrawableToBitmap(Drawable drawable, int index, Bitmap.Config color) {
        int height;
        int width;
        int left = 0;
        if (index == 0 || index == 1) {
            left = 1;
            width = 8;
            height = 480;
        } else {
            width = 72;
            height = 72;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, color);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(left, 0, width - left, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap drawableToAlignBitmap(Drawable drawable, int height, int width, Bitmap.Config color) {
        int widthAlign = getAlignWidth(width);
        int left = (widthAlign - width) / 2;
        Bitmap bitmap = Bitmap.createBitmap(widthAlign, height, color);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(left, 0, width - left, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap dualVerticaldrawableToAlignBitmap(Drawable drawable, int height, int width, Bitmap.Config color) {
        int widthAlign = width % 4 == 0 ? width : ((width / 4) * 4) + 4;
        int left = (widthAlign - width) / 2;
        Bitmap bitmap = Bitmap.createBitmap(widthAlign, height, color);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(left, 0, width - left, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void decodeResource(Context context, int index) {
        Slog.i(TAG, "decodeResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            Drawable drawable = context.getResources().getDrawableForDensity(context.getResources().getIdentifier(this.RES_IDS[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mBitmap[index]);
                this.mBitmap[index] = Bitmap.createScaledBitmap(drawableToBitmap(drawable, Bitmap.Config.RGB_565), (int) (((float) TARGET_DYNAMIC_WIDTH) / 2.0f), (int) (((float) TARGET_DYNAMIC_HEIGHT) / 2.0f), true);
                this.mBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.w(TAG, "decodeResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mBitmap[index].getDensity() + ", RES_IDS:" + this.RES_IDS[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "FingerPrint Resources NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "FingerPrint Resources Exception.");
        }
    }

    private void decodeResourceFromPath(int index) {
        Slog.i(TAG, "decodeResourceFromPath begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            if (index < this.mAnimFileNames.size()) {
                recycleBitmapBeforeReAssign(this.mBitmap[index]);
                this.mBitmap[index] = BitmapFactory.decodeFile(this.mAnimFileNames.get(index)).copy(Bitmap.Config.RGB_565, true);
                this.mBitmap[index].setDensity(DESIRED_DPI);
                Slog.i(TAG, " decodeResourceFromPath time = " + (System.currentTimeMillis() - time));
            }
        } catch (IllegalArgumentException e) {
            Slog.i(TAG, " not found the path or path is error");
        }
    }

    private static Bitmap drawableToBitmapWithText(Context context, BitmapDrawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(FACE_ID_BITMAP_WITH_TEXT_WIDTH, FACE_ID_BITMAP_WITH_TEXT_HEIGHT, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int i = FACE_ID_BITMAP_WITH_TEXT_WIDTH;
        drawable.setBounds((i - 144) / 2, 0, (i + 144) / 2, 144);
        drawable.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(-7829368);
        String strFaceHint = context.getString(33686290);
        String textFont = "/system/fonts/Roboto-Regular.ttf";
        try {
            if (sIsPorscheProduct) {
                textFont = "/system/fonts/PorscheDesignFont.ttf";
            }
            Typeface fontTypeface = Typeface.createFromFile(textFont);
            if (fontTypeface != null) {
                paint.setTypeface(fontTypeface);
            }
        } catch (Exception e) {
            Slog.e(TAG, e.toString(), e);
        }
        paint.setTextSize((float) ((int) ((FACE_ID_TEXT_SIZE * context.getResources().getDisplayMetrics().density) + 0.5f)));
        paint.getFontMetricsInt();
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(strFaceHint, (float) (((int) (((float) FACE_ID_BITMAP_WITH_TEXT_WIDTH) - paint.measureText(strFaceHint))) / 2), (float) 216, paint);
        return bitmap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decodeFaceIdResource(Context context, int index) {
        Slog.i(TAG, "decodeFaceIdResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawableForDensity(context.getResources().getIdentifier(RES_IDS_FACEID[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                if (this.mFaceIdBitmap[index] != null) {
                    this.mFaceIdBitmap[index].recycle();
                }
                this.mFaceIdBitmap[index] = drawableToBitmapWithText(this.mContext, drawable);
                this.mFaceIdBitmap[index].setDensity(DESIRED_DPI);
                drawable.getBitmap().recycle();
            }
            Slog.w(TAG, "decodeFaceIdResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mBitmap[index].getDensity() + ", RES_IDS_FACEID:" + RES_IDS_FACEID[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "FaceIdResource NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "FaceIdResource Exception.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decodePorscheImageResource(Context context, int index) {
        Slog.i(TAG, "decodePorscheImageResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            Drawable drawable = context.getResources().getDrawableForDensity(context.getResources().getIdentifier(RES_ID_PORSCHEIMAGE_XXXHDPI[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                this.mPorscheImageBitmap[index] = drawableToBitmap(drawable, Bitmap.Config.RGB_565);
                this.mPorscheImageBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.w(TAG, "decodePorscheImageResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mPorscheImageBitmap[index].getDensity() + ", RES_ID_PORSCHEIMAGE_XXXHDPI:" + RES_ID_PORSCHEIMAGE_XXXHDPI[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "PorscheImageResource NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "PorscheImageResource Exception.");
        }
    }

    private static Bitmap getTheRotatedBitmap(Bitmap bitmap, int orientationDegree) {
        if (bitmap == null) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) orientationDegree);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    private void decodeDigitalImageResource(Context context, int index) {
        Bitmap bitmap;
        Slog.i(TAG, "decodeDigitalImageResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            Drawable drawable = context.getResources().getDrawableForDensity(context.getResources().getIdentifier(RES_IDS_DIGITAL_SINGLE_CLOCK_XXHDPI[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mDigitalClockBitmap[index]);
                Bitmap[] bitmapArr = this.mDigitalClockBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmap(drawable, Bitmap.Config.RGB_565), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawable, DIGITAL_CLOCK_IMAGE_HEIGHT, DIGITAL_CLOCK_IMAGE_WIDTH, Bitmap.Config.RGB_565);
                }
                bitmapArr[index] = bitmap;
                this.mDigitalClockBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeDigitalImageResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mDigitalClockBitmap[index].getDensity() + ", RES_ID_PORSCHEIMAGE_XXXHDPI:" + RES_IDS_DIGITAL_SINGLE_CLOCK_XXHDPI[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "DigitalImageResource NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "DigitalImageResource Exception.");
        }
    }

    private void decodeDualDigitalImageResource(Context context, int index) {
        Bitmap bitmap;
        Slog.i(TAG, "decodeDigitalImageResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            Drawable drawable = context.getResources().getDrawableForDensity(context.getResources().getIdentifier(RES_IDS_DIGITAL_DOUBLE_CLOCK_XXHDPI[index], "drawable", AOD_PACKAGE_NAME), DESIRED_DPI, null);
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mDigitalDoubleClockBitmap[index]);
                Bitmap[] bitmapArr = this.mDigitalDoubleClockBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmapForFoldScreen(drawable, Bitmap.Config.RGB_565), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawable, getDigitalDualClockImageHeight(), DIGITAL_DUAL_CLOCK_REAL_IMAGE_WIDTH, Bitmap.Config.RGB_565);
                }
                bitmapArr[index] = bitmap;
                this.mDigitalDoubleClockBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "mDigitalDoubleClockBitmap finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mDigitalDoubleClockBitmap[index].getDensity() + ", RES_ID_PORSCHEIMAGE_XXXHDPI:" + this.mDigitalDoubleClockBitmap[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "DualDigitalImageResource NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "DualDigitalImageResource Exception.");
        }
    }

    private void decodeVerticalDigitalImageResource(Context context, int index) {
        int bitmapWidth;
        int bitmapHeight;
        String[] resIdsVerticalSingleClock;
        Drawable drawable;
        Bitmap bitmap;
        Slog.i(TAG, "decodeVerticalDigitalImageResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        if (AodThemeManager.getInstance().getClockType() == 104) {
            bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getRealScreenWidth() - 48;
        } else {
            bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getVerticalClockImageWidth();
        }
        try {
            if (this.mBgFgdisplayPos == 1) {
                resIdsVerticalSingleClock = this.mIsPreSet ? RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI : RES_IDS_VERTICAL_ONLINE_TEN_SINGLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 2) {
                resIdsVerticalSingleClock = RES_IDS_VERTICAL_ONLINE_SING_SINGLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 0) {
                resIdsVerticalSingleClock = RES_IDS_VERTICAL_ONLINE_STATIC_SINGLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 3) {
                resIdsVerticalSingleClock = AodThemeManager.getInstance().getSingleVertical4ResNames();
            } else if (this.mBgFgdisplayPos == 4) {
                String themeName = AodThemeManager.getInstance().getThemeName();
                if (TextUtils.equals(AodThemeConst.STATIC_FISH_STYLE, themeName)) {
                    resIdsVerticalSingleClock = RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI_FOR_STATIC_FISH;
                } else if (TextUtils.equals(AodThemeConst.STATIC_BUTTERFLY_STYLE, themeName)) {
                    resIdsVerticalSingleClock = RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI_FOR_STATIC_BUTTERFLY;
                } else {
                    Slog.w(TAG, " unexpected resIdsVerticalSingleClock, themeName = " + themeName);
                    return;
                }
            } else {
                Slog.w(TAG, " unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
                return;
            }
            String picName = resIdsVerticalSingleClock[index];
            int resID = context.getResources().getIdentifier(picName, "drawable", AOD_PACKAGE_NAME);
            if (!this.mIsPreSet) {
                drawable = context.getResources().getDrawableForDensity(resID, DESIRED_DPI, null);
                if (drawable == null) {
                    drawable = AodResUtil.getDrawableByPath(context, picName, resID, true);
                }
            } else if (this.mIsProductCustomized) {
                drawable = AodResUtil.getDrawableByPath(context, picName, resID, false);
            } else {
                drawable = context.getResources().getDrawableForDensity(resID, DESIRED_DPI, null);
            }
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mVerticalClockBitmap[index]);
                Bitmap[] bitmapArr = this.mVerticalClockBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmap(drawable, Bitmap.Config.ARGB_8888), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawable, bitmapHeight, bitmapWidth, Bitmap.Config.ARGB_8888);
                }
                bitmapArr[index] = bitmap;
                this.mVerticalClockBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeVerticalDigitalImageResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mVerticalClockBitmap[index].getDensity() + ", RES_IDS_VERTICAL_SINGLE_CLOCK_XXHDPI:" + picName);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "resource not found " + e.toString());
        } catch (Exception e2) {
            Slog.i(TAG, "resource not found " + e2.toString());
        }
    }

    private void recycleBitmapBeforeReAssign(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private void decodeVerticalDualDigitalImageResource(Context context, int index) {
        int bitmapHeight;
        int bitmapWidth;
        String[] resIdsVerticalDoubleClock;
        Drawable drawable;
        Bitmap bitmap;
        Slog.i(TAG, "decodeVerticalDualDigitalImageResource begin!!! index " + index);
        long time = System.currentTimeMillis();
        try {
            if (AodThemeManager.getInstance().getClockType() == 104) {
                bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
                bitmapWidth = ((getRealScreenWidth() - 48) - 4) / 2;
            } else {
                bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
                bitmapWidth = getVerticalClockImageWidth() / 2;
            }
            if (this.mBgFgdisplayPos == 1) {
                resIdsVerticalDoubleClock = this.mIsPreSet ? RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI : RES_IDS_VERTICAL_ONLINE_TEN_DOUBLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 2) {
                resIdsVerticalDoubleClock = RES_IDS_VERTICAL_ONLINE_SING_DOUBLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 0) {
                resIdsVerticalDoubleClock = RES_IDS_VERTICAL_ONLINE_STATIC_DOUBLE_CLOCK_XXHDPI;
            } else if (this.mBgFgdisplayPos == 3) {
                resIdsVerticalDoubleClock = AodThemeManager.getInstance().getDualVertical4ResNames();
            } else if (this.mBgFgdisplayPos == 4) {
                String themeName = AodThemeManager.getInstance().getThemeName();
                if (TextUtils.equals(AodThemeConst.STATIC_FISH_STYLE, themeName)) {
                    resIdsVerticalDoubleClock = RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI_FOR_STATIC_FISH;
                } else if (TextUtils.equals(AodThemeConst.STATIC_BUTTERFLY_STYLE, themeName)) {
                    resIdsVerticalDoubleClock = RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI_FOR_STATIC_BUTTERFLY;
                } else {
                    Slog.w(TAG, " unexpected resIdsVerticalDoubleClock, themeName = " + themeName);
                    return;
                }
            } else {
                Slog.w(TAG, " unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
                return;
            }
            String picName = resIdsVerticalDoubleClock[index];
            int resID = context.getResources().getIdentifier(picName, "drawable", AOD_PACKAGE_NAME);
            if (!this.mIsPreSet) {
                drawable = context.getResources().getDrawableForDensity(resID, DESIRED_DPI, null);
                if (drawable == null) {
                    drawable = AodResUtil.getDrawableByPath(context, picName, resID, true);
                }
            } else if (this.mIsProductCustomized) {
                drawable = AodResUtil.getDrawableByPath(context, picName, resID, false);
            } else {
                drawable = context.getResources().getDrawableForDensity(resID, DESIRED_DPI, null);
            }
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mVerticalDoubleClockBitmap[index]);
                Bitmap[] bitmapArr = this.mVerticalDoubleClockBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmapForFoldScreen(drawable, Bitmap.Config.ARGB_8888), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = dualVerticaldrawableToAlignBitmap(drawable, bitmapHeight, bitmapWidth, Bitmap.Config.ARGB_8888);
                }
                bitmapArr[index] = bitmap;
                this.mVerticalDoubleClockBitmap[index].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeVerticalDualDigitalImageResource finish!!!, index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mVerticalDoubleClockBitmap[index].getDensity() + ",RES_IDS_VERTICAL_DOUBLE_CLOCK_XXHDPI:" + picName);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "resource not found " + e.toString());
        } catch (Exception e2) {
            Slog.i(TAG, "resource not found " + e2.toString());
        }
    }

    private void decodeSingleAnalogClockImageResource(Context context, int index, int resId, String resName) {
        Drawable drawableHour;
        Bitmap bitmap;
        Slog.i(TAG, "decodeSingleAnalogClockImageResource begin index " + index + " and resId : " + resId);
        long time = System.currentTimeMillis();
        try {
            int i = 480;
            if (!this.mIsPreSet) {
                drawableHour = context.getResources().getDrawableForDensity(resId, sIsLayaPorduct ? 480 : DESIRED_DPI, null);
                if (drawableHour == null) {
                    drawableHour = AodResUtil.getDrawableByPath(context, resName, 0, true);
                }
            } else if (this.mIsProductCustomized) {
                drawableHour = AodResUtil.getDrawableByPath(context, resName, 0, false);
            } else {
                drawableHour = context.getResources().getDrawableForDensity(resId, sIsLayaPorduct ? 480 : DESIRED_DPI, null);
            }
            if (drawableHour != null) {
                recycleBitmapBeforeReAssign(this.mSingleAnalogHourBitmap[index]);
                int height = sIsLayaPorduct ? 360 : ANALOG_CLOCK_HOUR_IMAGE_HEIGHT;
                int width = sIsLayaPorduct ? 72 : ANALOG_CLOCK_HOUR_IMAGE_WIDTH;
                Bitmap[] bitmapArr = this.mSingleAnalogHourBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmap(drawableHour, Bitmap.Config.ARGB_8888), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawableHour, height, width, Bitmap.Config.ARGB_8888);
                }
                bitmapArr[index] = bitmap;
                Bitmap bitmap2 = this.mSingleAnalogHourBitmap[index];
                if (!sIsLayaPorduct) {
                    i = DESIRED_DPI;
                }
                bitmap2.setDensity(i);
                if (drawableHour instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawableHour).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeSingleAnalogClockImageResource for hour resource index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mSingleAnalogHourBitmap[index].getDensity() + ", RES_IDS_SINGLE_ANALOG_CLOCK_XXHDPI:" + RES_IDS_SINGLE_ANALOG_CLOCK_XXHDPI[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "decodeSingleAnalogClockImageResource resource not found.");
        } catch (Exception e2) {
            Slog.i(TAG, "decodeSingleAnalogClockImageResource exception.");
        }
    }

    private void decodeDualAnalogClockImageResource(Context context, int index, int resId, String resName, boolean isSameRes) {
        Drawable drawableHour;
        Bitmap bitmap;
        Slog.i(TAG, "decodeDualAnalogClockImageResource begin index " + index + "and resId : " + resId + " isSameRes : " + isSameRes);
        long time = System.currentTimeMillis();
        try {
            int i = 480;
            if (!this.mIsPreSet) {
                drawableHour = context.getResources().getDrawableForDensity(resId, sIsLayaPorduct ? 480 : DESIRED_DPI, null);
                if (drawableHour == null) {
                    drawableHour = AodResUtil.getDrawableByPath(context, resName, 0, true);
                }
            } else if (this.mIsProductCustomized) {
                drawableHour = AodResUtil.getDrawableByPath(context, resName, 0, false);
            } else {
                drawableHour = context.getResources().getDrawableForDensity(resId, sIsLayaPorduct ? 480 : DESIRED_DPI, null);
            }
            if (drawableHour != null) {
                recycleBitmapBeforeReAssign(this.mDualAnalogHourBitmap[index]);
                int height = sIsLayaPorduct ? LAYA_ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT : ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT;
                int width = sIsLayaPorduct ? 48 : ANALOG_DUAL_CLOCK_HOUR_IMAGE_WIDTH;
                Bitmap[] bitmapArr = this.mDualAnalogHourBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmap(drawableHour, Bitmap.Config.ARGB_8888), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawableHour, height, width, Bitmap.Config.ARGB_8888);
                }
                bitmapArr[index] = bitmap;
                this.mDualAnalogHourBitmap[index].setDensity(sIsLayaPorduct ? 480 : DESIRED_DPI);
                if (isSameRes) {
                    recycleBitmapBeforeReAssign(this.mDualAnalogHourBitmap[index + 2]);
                    this.mDualAnalogHourBitmap[index + 2] = this.mDualAnalogHourBitmap[index];
                    Bitmap bitmap2 = this.mDualAnalogHourBitmap[index + 2];
                    if (!sIsLayaPorduct) {
                        i = DESIRED_DPI;
                    }
                    bitmap2.setDensity(i);
                }
                if (drawableHour instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawableHour).getBitmap().recycle();
                }
            }
            Slog.i(TAG, "decodeDualAnalogClockImageResource for hour resource index = " + index + ", time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mDualAnalogHourBitmap[index].getDensity() + ", RES_IDS_DUAL_ANALOG_CLOCK_XXHDPI:" + RES_IDS_DUAL_ANALOG_CLOCK_XXHDPI[index]);
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "decodeDualAnalogClockImageResource resource not found.");
        }
    }

    private void decodePatternClockImageResource(Context context, int resId, String picName) {
        Bitmap bitmap;
        long time = System.currentTimeMillis();
        Drawable drawable = null;
        try {
            boolean z = true;
            if (!this.mIsPreSet) {
                if (this.mIsVmallCustomized) {
                    drawable = AodResUtil.getVmallThemeDrawable(context, picName);
                } else {
                    drawable = context.getResources().getDrawableForDensity(resId, DESIRED_DPI, null);
                }
                if (drawable == null) {
                    drawable = AodResUtil.getDrawableByPath(context, picName, 0, true);
                }
            } else if (this.mIsProductCustomized) {
                drawable = AodResUtil.getDrawableByPath(context, picName, 0, false);
            } else {
                if (resId > 0) {
                    drawable = context.getResources().getDrawableForDensity(resId, DESIRED_DPI, null);
                }
                if (drawable == null) {
                    if (this.mClockMode == 0) {
                        z = false;
                    }
                    int clockType = AodThemeManager.getInstance().getClockType();
                    if (clockType != 102) {
                        if (clockType != 1021) {
                            if (!(clockType == 105 || clockType == 106 || clockType == 107 || clockType == 108 || clockType == 109)) {
                                if (clockType != 110) {
                                    drawable = AodResUtil.getUserCustomBg(context, getPatternClockImageWidth(), PATTERN_CLOCK_IMAGE_HEIGHT_USER_CUSTOM_PIC, "");
                                }
                            }
                            drawable = AodResUtil.getArtSignatureBg(context, getPatternArtClockImageWidth(), PATTERN_ART_CUSTOM_PIC, picName, sIsTetonProduct);
                        }
                    }
                    drawable = AodResUtil.getArtCustomBg(context, getPatternClockImageWidth(), VERTICAL3_CLOCK_IMAGE_HEIGHT, picName);
                }
            }
            if (drawable != null) {
                recycleBitmapBeforeReAssign(this.mPatternClockBitmap[0]);
                int clockType2 = AodThemeManager.getInstance().getClockType();
                int height = getPattenClockImageHeightType2();
                int width = getPatternClockImageWidth();
                if (clockType2 == 11 || clockType2 == 14 || clockType2 == 16) {
                    height = PATTERN_CLOCK_IMAGE_HEIGHT_TYPE1;
                }
                if (clockType2 == 102 || clockType2 == 1021 || clockType2 == 103) {
                    height = VERTICAL3_CLOCK_IMAGE_HEIGHT;
                }
                if (clockType2 == 15) {
                    height = PATTERN_CLOCK_IMAGE_HEIGHT_USER_CUSTOM_PIC;
                }
                if (AodThemeConst.isAodSignature(clockType2)) {
                    height = PATTERN_ART_CUSTOM_PIC;
                    width = getPatternArtClockImageWidth();
                }
                Bitmap[] bitmapArr = this.mPatternClockBitmap;
                if (isFullFoldableScreen()) {
                    bitmap = getTheRotatedBitmap(drawableToBitmapForFoldScreen(drawable, Bitmap.Config.RGB_565), FOLD_ROTATED_DEGREE);
                } else {
                    bitmap = drawableToAlignBitmap(drawable, height, width, Bitmap.Config.RGB_565);
                }
                bitmapArr[0] = bitmap;
                this.mPatternClockBitmap[0].setDensity(DESIRED_DPI);
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
            Slog.w(TAG, "decodePatternClockImageResource finish time = " + (System.currentTimeMillis() - time) + ", bitmap density = " + this.mPatternClockBitmap[0].getDensity());
        } catch (Resources.NotFoundException e) {
            Slog.i(TAG, "PatternClockImage NotFoundException.");
        } catch (Exception e2) {
            Slog.i(TAG, "Resources Exception.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleClockImageResources() {
        Slog.i(TAG, "handleClockImageResources enter mDisplayMode:" + this.mDisplayMode);
        if (sIsTaurusProduct) {
            int i = this.mDisplayMode;
            if (i == 3) {
                handleAnalogClockHourCachedResources();
                Slog.i(TAG, "mSetAnalogClockBitmapRunnable ");
            } else if (i == 4 || i == 7) {
                if (!sIsSupportReconstruction || (this.mAODWorkMode & 128) != 128) {
                    Slog.i(TAG, "mSet PatternCLock BitmapRunnable ");
                    handlePatternClockImageResources();
                    Slog.i(TAG, "mSet PatterCLockBitmapRunnable: " + this.mPatternClockBitmapNum);
                    return;
                }
                Slog.i(TAG, "pattern or forest clock, and enble animation, no need pattern image.");
            } else if (i == 2) {
                Slog.i(TAG, "mSetClockBitmapRunnable enter");
                handleDigitalClockCachedResources();
                Slog.i(TAG, "mSetClockBitmapRunnable: " + this.mDigitalClockBitmapNum);
            } else if (i != 6) {
            } else {
                if (!sIsSupportReconstruction || (this.mAODWorkMode & 128) != 128) {
                    Slog.i(TAG, "mSet VerticalCLockBitmapRunnable ");
                    handleVerticalDigitalClockBgFgCachedResources();
                    Slog.i(TAG, "mSet VerticalCLockBitmapRunnable: " + this.mVerticalClockBitmapNum);
                    return;
                }
                Slog.i(TAG, "vertical, and enble animation, no need pattern image.");
            }
        }
    }

    private void handleVolumeBarResources() {
        for (int i = 0; i < 2; i++) {
            Bitmap newBM = Bitmap.createBitmap(8, 480, Bitmap.Config.ARGB_8888);
            if (newBM == null) {
                Slog.e(TAG, "handleVolumeBarImageResources return with null newBM");
            } else {
                newBM.setDensity(DESIRED_DPI);
                Canvas canvas = new Canvas(newBM);
                Bitmap[] bitmapArr = this.mRingVolumeBitmap;
                if (bitmapArr[i] == null) {
                    Slog.w(TAG, "handleVolumeBarImageResources single clock bitmap not exist " + i);
                } else {
                    canvas.drawBitmap(bitmapArr[i], 0.0f, 0.0f, (Paint) null);
                    this.mVolumeBarBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 9);
                    if (this.mVolumeBarBitmapNum >= 2) {
                        Slog.w(TAG, "mVolumeBarBitmapNum out of bounds " + this.mVolumeBarBitmapNum);
                        if (!newBM.isRecycled()) {
                            newBM.recycle();
                            return;
                        }
                        return;
                    }
                    Slog.w(TAG, "mVolumeBarBufferSize is  " + this.mVolumeBarBufferSize);
                    if (nativeSetVolumeBarBitmap(this.mAodDevicePtr, this.mVolumeBarBitmapNum) == 0) {
                        Slog.i(TAG, "handleVolumeBarImageResources RESULT_OK index is " + i + ", current SUCC num is " + (this.mVolumeBarBitmapNum + 1));
                        this.mVolumeBarBitmapNum = this.mVolumeBarBitmapNum + 1;
                    }
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                    }
                }
            }
        }
    }

    private void handleVolumeIconResources() {
        for (int i = 0; i < 6; i++) {
            Bitmap newBM = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);
            if (newBM == null) {
                Slog.e(TAG, "handleVolumeIconImageResources return with null newBM");
            } else {
                newBM.setDensity(DESIRED_DPI);
                Canvas canvas = new Canvas(newBM);
                Bitmap[] bitmapArr = this.mRingVolumeBitmap;
                if (bitmapArr[i + 2] == null) {
                    Slog.w(TAG, "handleVolumeIconImageResources single clock bitmap not exist " + i);
                } else {
                    canvas.drawBitmap(bitmapArr[i + 2], 0.0f, 0.0f, (Paint) null);
                    this.mVolumeIconBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 10);
                    if (this.mVolumeIconBitmapNum >= 6) {
                        Slog.w(TAG, "mVolumeIconBitmapNum out of bounds " + this.mVolumeIconBitmapNum);
                        if (!newBM.isRecycled()) {
                            newBM.recycle();
                            return;
                        }
                        return;
                    }
                    Slog.w(TAG, "mVolumeIconBufferSize is  " + this.mVolumeIconBufferSize);
                    if (nativeSetVolumeIconBitmap(this.mAodDevicePtr, this.mVolumeIconBitmapNum) == 0) {
                        Slog.i(TAG, "handleVolumeIconImageResources RESULT_OK index is " + i + ", current SUCC num is " + (this.mVolumeIconBitmapNum + 1));
                        this.mVolumeIconBitmapNum = this.mVolumeIconBitmapNum + 1;
                    }
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleVolumeImageResources() {
        needReloadBitmapResource(9);
        handleVolumeBarResources();
        handleVolumeIconResources();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGMPDataForNative() {
        AodGmpConfigParser.getInstance();
        if (AodGmpConfigParser.parserThemeXml()) {
            this.mDisplayGMPBufferSize = AodGmpConfigParser.getInstance().getGmpParamLength();
            this.mDisplayGMPLowBuffer = AodGmpConfigParser.getInstance().getGmpLowPara();
            this.mDisplayGMPHighBuffer = AodGmpConfigParser.getInstance().getGmpHighPara();
        }
        Slog.i(TAG, "handleGMPDataForNative RESULT_OK .");
    }

    private void handlePatternClockImageResources() {
        needReloadBitmapResource(8);
        int clockType = AodThemeManager.getInstance().getClockType();
        int height = getPattenClockImageHeightType2();
        int width = getPatternClockImageWidth();
        if (clockType == 11 || clockType == 14 || clockType == 16) {
            height = getAlignWidth(PATTERN_CLOCK_IMAGE_HEIGHT_TYPE1);
        }
        if (clockType == 102 || clockType == 1021 || clockType == 103) {
            height = VERTICAL3_CLOCK_IMAGE_HEIGHT;
        }
        if (clockType == 15) {
            height = getAlignWidth(PATTERN_CLOCK_IMAGE_HEIGHT_USER_CUSTOM_PIC);
        }
        if (AodThemeConst.isAodSignature(clockType)) {
            height = PATTERN_ART_CUSTOM_PIC;
            width = getPatternArtClockImageWidth();
        }
        int bitmapWidth = isFullFoldableScreen() ? height : width;
        int bitmapHeight = isFullFoldableScreen() ? width : height;
        int bitmapWidth2 = getAlignWidth(bitmapWidth);
        Slog.i(TAG, "handlePatternClockImageResources clockType:" + clockType + " bitmapWidth " + bitmapWidth2 + " bitmapHeight:" + bitmapHeight);
        Bitmap newBM = Bitmap.createBitmap(bitmapWidth2, bitmapHeight, Bitmap.Config.RGB_565);
        if (newBM == null) {
            Slog.e(TAG, "handlePatternClockImageResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        for (int i = 0; i < 1; i++) {
            Bitmap[] bitmapArr = this.mPatternClockBitmap;
            if (bitmapArr[i] == null || bitmapArr[i].isRecycled()) {
                Slog.w(TAG, "handlePatternClockImageResources bitmap not exist " + i);
            } else {
                canvas.drawBitmap(this.mPatternClockBitmap[i], 0.0f, 0.0f, (Paint) null);
                this.mPatternClockBitmapBuffer = bitmap2Bytes(newBM, 8);
                int i2 = this.mPatternClockBitmapNum;
                if (i2 >= 1) {
                    Slog.w(TAG, "mPatternClockBitmapNum out of bounds " + this.mPatternClockBitmapNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                } else if (nativeSetPatternClockBitmap(this.mAodDevicePtr, i2) == 0) {
                    Slog.i(TAG, "handlePatternClockImageResources RESULT_OK index is " + i + ", current SUCC num is " + (this.mPatternClockBitmapNum + 1));
                    this.mPatternClockBitmapNum = this.mPatternClockBitmapNum + 1;
                }
            }
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    private static int getAlignWidth(int width) {
        return width % 8 == 0 ? width : ((width / 8) * 8) + 8;
    }

    private static int getPageAlignSize(int size) {
        return size % 4096 == 0 ? size : ((size / 4096) * 4096) + 4096;
    }

    private static int dp2Px(int dp) {
        float px = ((float) dp) * REAL_DENSITY;
        return (int) (px % 1.0f == 0.0f ? px : 1.0f + px);
    }

    private static int dp2PxEx(int dp) {
        if (!sIsTetonProduct) {
            return dp2Px(dp);
        }
        int density = (int) REAL_DENSITY;
        Slog.i(TAG, "use int density:" + density);
        return dp * density;
    }

    private void handleAnalogClockHourCachedResources() {
        int height;
        int width;
        Bitmap newBM;
        needReloadBitmapResource(7);
        if (sIsLayaPorduct) {
            width = this.mClockMode == 0 ? 72 : 48;
            height = this.mClockMode == 0 ? 360 : LAYA_ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT;
        } else {
            width = this.mClockMode == 0 ? ANALOG_CLOCK_HOUR_IMAGE_WIDTH : ANALOG_DUAL_CLOCK_HOUR_IMAGE_WIDTH;
            height = this.mClockMode == 0 ? ANALOG_CLOCK_HOUR_IMAGE_HEIGHT : ANALOG_DUAL_CLOCK_HOUR_IMAGE_HEIGHT;
        }
        int width2 = getAlignWidth(width);
        for (int i = 0; i < 6; i++) {
            if (isFullFoldableScreen()) {
                newBM = Bitmap.createBitmap(height, width2, Bitmap.Config.ARGB_8888);
            } else {
                newBM = Bitmap.createBitmap(width2, height, Bitmap.Config.ARGB_8888);
            }
            if (newBM == null) {
                Slog.e(TAG, "handleAnalogClockHourCachedResources return with null newBM");
            } else {
                newBM.setDensity(sIsLayaPorduct ? 480 : DESIRED_DPI);
                Canvas canvas = new Canvas(newBM);
                if (this.mClockMode == 0) {
                    Bitmap[] bitmapArr = this.mSingleAnalogHourBitmap;
                    if (bitmapArr[i] == null) {
                        Slog.w(TAG, "handleAnalogClockHourCachedResources single clock bitmap not exist " + i);
                    } else {
                        canvas.drawBitmap(bitmapArr[i], 0.0f, 0.0f, (Paint) null);
                    }
                } else {
                    Bitmap[] bitmapArr2 = this.mDualAnalogHourBitmap;
                    if (bitmapArr2[i] == null) {
                        Slog.w(TAG, "handleAnalogClockHourCachedResources double clock bitmap not exist " + i);
                    } else {
                        canvas.drawBitmap(bitmapArr2[i], 0.0f, 0.0f, (Paint) null);
                    }
                }
                this.mSingleAnalogClockHourBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 7);
                if (this.mSingleAnalogClockHourBitmapNum >= 6) {
                    Slog.w(TAG, "mSingleAnalogClockHourBitmapNum out of bounds " + this.mSingleAnalogClockHourBitmapNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                }
                Slog.w(TAG, "mSingleAnalogClockHourBufferSize is  " + this.mSingleAnalogClockHourBufferSize);
                if (nativeSetAnalogClockHourBitmap(this.mAodDevicePtr, this.mSingleAnalogClockHourBitmapNum) == 0) {
                    Slog.i(TAG, "handleAnalogClockHourCachedResources RESULT_OK index is " + i + ", current SUCC num is " + (this.mSingleAnalogClockHourBitmapNum + 1));
                    this.mSingleAnalogClockHourBitmapNum = this.mSingleAnalogClockHourBitmapNum + 1;
                }
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseClockBitmapBufferResources() {
        if (sIsTaurusProduct) {
            Slog.i(TAG, "releaseImageCachedResources enter");
            if (nativeReleaseClockBitmapBuffer(this.mAodDevicePtr) == 0) {
                resetAllBitmapNumAndSize();
                Slog.i(TAG, "nativeReleaseClockBitmapBuffer ok");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDigitalClockCachedResources() {
        Bitmap newBM;
        Slog.i(TAG, "updateDigitalClockCachedResources enter");
        boolean isDualClock = this.mClockMode == 0;
        int digitalClockImageWidth = isDualClock ? DIGITAL_CLOCK_IMAGE_WIDTH : DIGITAL_DUAL_CLOCK_IMAGE_WIDTH;
        int digitalClockImageHight = isDualClock ? DIGITAL_CLOCK_IMAGE_HEIGHT : getDigitalDualClockImageHeight();
        int digitalClockImageWidth2 = getAlignWidth(digitalClockImageWidth);
        boolean isRotation = isFullFoldableScreen();
        if (isRotation && sIsTetonProduct) {
            if (this.mClockMode == 0) {
                digitalClockImageWidth2 = this.aodInfoRect[10].height();
                digitalClockImageHight = this.aodInfoRect[10].width();
            } else {
                digitalClockImageWidth2 = this.aodInfoRect[14].height();
                digitalClockImageHight = this.aodInfoRect[14].width();
            }
        }
        if (isRotation) {
            newBM = Bitmap.createBitmap(digitalClockImageHight, digitalClockImageWidth2, Bitmap.Config.RGB_565);
        } else {
            newBM = Bitmap.createBitmap(digitalClockImageWidth2, digitalClockImageHight, Bitmap.Config.RGB_565);
        }
        if (newBM == null) {
            Slog.e(TAG, "handleDigitalClockCachedResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        if (this.mClockMode == 0) {
            Bitmap[] bitmapArr = this.mDigitalClockBitmap;
            int i = currrentColorFlag;
            if (bitmapArr[i] == null) {
                Slog.w(TAG, "handleDigitalClockCachedResources bitmap not exist. " + currrentColorFlag);
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                    return;
                }
                return;
            }
            canvas.drawBitmap(bitmapArr[i], 0.0f, 0.0f, (Paint) null);
            this.mDigitalClockBitmapBuffer = bitmap2Bytes(newBM, 6);
        } else {
            Bitmap[] bitmapArr2 = this.mDigitalDoubleClockBitmap;
            int i2 = currrentColorFlag;
            if (bitmapArr2[i2] == null) {
                Slog.w(TAG, "handleDigitalClockCachedResources bitmap not exist. " + currrentColorFlag);
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                    return;
                }
                return;
            }
            int left = 0;
            if (!isRotation) {
                left = (digitalClockImageWidth2 - bitmapArr2[i2].getWidth()) / 2;
            }
            canvas.drawBitmap(this.mDigitalDoubleClockBitmap[currrentColorFlag], (float) left, 0.0f, (Paint) null);
            this.mDigitalClockBitmapBuffer = bitmap2Bytes(newBM, 6);
        }
        Slog.i(TAG, "nativeUpdateClockBitmapBuffer, result is : " + this.mSingleDigitalClockBufferSize);
        if (nativeUpdateClockBitmapBuffer(this.mAodDevicePtr, 0) == 0) {
            Slog.i(TAG, "nativeUpdateClockBitmapBuffer , ok");
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    private void handleDigitalClockCachedResources() {
        Bitmap newBM;
        int digitalClockImageWidth;
        boolean isDualClock;
        int digitalClockImageHight;
        int left;
        String str;
        needReloadBitmapResource(6);
        int i = 1;
        boolean isDualClock2 = this.mClockMode == 0;
        int digitalClockImageWidth2 = isDualClock2 ? DIGITAL_CLOCK_IMAGE_WIDTH : DIGITAL_DUAL_CLOCK_IMAGE_WIDTH;
        int digitalClockImageHight2 = isDualClock2 ? DIGITAL_CLOCK_IMAGE_HEIGHT : getDigitalDualClockImageHeight();
        int digitalClockImageWidth3 = getAlignWidth(digitalClockImageWidth2);
        boolean isRotation = isFullFoldableScreen();
        if (sIsTetonProduct && isRotation) {
            if (this.mClockMode == 0) {
                digitalClockImageWidth3 = this.aodInfoRect[10].height();
                digitalClockImageHight2 = this.aodInfoRect[10].width();
            } else {
                digitalClockImageWidth3 = this.aodInfoRect[14].height();
                digitalClockImageHight2 = this.aodInfoRect[14].width();
            }
        }
        if (isRotation) {
            newBM = Bitmap.createBitmap(digitalClockImageHight2, digitalClockImageWidth3, Bitmap.Config.RGB_565);
        } else {
            newBM = Bitmap.createBitmap(digitalClockImageWidth3, digitalClockImageHight2, Bitmap.Config.RGB_565);
        }
        if (newBM == null) {
            Slog.e(TAG, "handleDigitalClockCachedResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        Paint paint = null;
        String str2 = "handleDigitalClockCachedResources bitmap not exist. ";
        if (this.mClockMode == 0) {
            int i2 = 0;
            while (i2 < i) {
                Bitmap[] bitmapArr = this.mDigitalClockBitmap;
                int i3 = currrentColorFlag;
                if (bitmapArr[i3] == null) {
                    Slog.w(TAG, str2 + currrentColorFlag);
                    str = str2;
                } else {
                    canvas.drawBitmap(bitmapArr[i3], 0.0f, 0.0f, paint);
                    this.mDigitalClockBitmapBuffer = bitmap2Bytes(newBM, 6);
                    int i4 = this.mDigitalClockBitmapNum;
                    if (i4 >= 1) {
                        Slog.w(TAG, "mDigitalClockBitmapNum out of bounds " + this.mDigitalClockBitmapNum);
                        if (!newBM.isRecycled()) {
                            newBM.recycle();
                            return;
                        }
                        return;
                    }
                    str = str2;
                    if (nativeSetDigitalClockBitmap(this.mAodDevicePtr, i4) == 0) {
                        Slog.e(TAG, "RESULT_OK index is " + currrentColorFlag + ", current SUCC num is " + (this.mDigitalClockBitmapNum + 1));
                        this.mDigitalClockBitmapNum = this.mDigitalClockBitmapNum + 1;
                    }
                }
                i2++;
                str2 = str;
                i = 1;
                paint = null;
            }
            if (!newBM.isRecycled()) {
                newBM.recycle();
                return;
            }
            return;
        }
        Slog.w(TAG, "mClockMode dual clock!");
        int colorFlag = currrentColorFlag;
        int i5 = 0;
        while (i5 < 2) {
            Bitmap[] bitmapArr2 = this.mDigitalDoubleClockBitmap;
            if (bitmapArr2[colorFlag] == null) {
                Slog.w(TAG, str2 + i5 + " colorFlag: " + colorFlag);
                isDualClock = isDualClock2;
                digitalClockImageWidth = digitalClockImageWidth3;
                digitalClockImageHight = digitalClockImageHight2;
            } else {
                if (!isRotation) {
                    left = (digitalClockImageWidth3 - bitmapArr2[currrentColorFlag].getWidth()) / 2;
                } else {
                    left = 0;
                }
                isDualClock = isDualClock2;
                digitalClockImageWidth = digitalClockImageWidth3;
                digitalClockImageHight = digitalClockImageHight2;
                canvas.drawBitmap(this.mDigitalDoubleClockBitmap[colorFlag], (float) left, 0.0f, (Paint) null);
                this.mDigitalClockBitmapBuffer = bitmap2Bytes(newBM, 6);
                int i6 = this.mDigitalClockBitmapNum;
                if (i6 >= 2) {
                    Slog.w(TAG, "mDigitalClockBitmapNum out of bounds " + this.mDigitalClockBitmapNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                } else if (nativeSetDigitalClockBitmap(this.mAodDevicePtr, i6) == 0) {
                    Slog.i(TAG, "RESULT_OK index is " + i5 + ", current SUCC num is " + (this.mDigitalClockBitmapNum + 1) + " colorFlag: " + colorFlag);
                    this.mDigitalClockBitmapNum = this.mDigitalClockBitmapNum + 1;
                }
            }
            i5++;
            digitalClockImageHight2 = digitalClockImageHight;
            isDualClock2 = isDualClock;
            digitalClockImageWidth3 = digitalClockImageWidth;
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    private void updateSingleVerticalClock() {
        int bitmapHeight;
        int bitmapWidth;
        Bitmap newBM;
        int picIndex = getSingleVerticalPicIndex();
        if (picIndex == -1) {
            Slog.w(TAG, "updateSingleVerticalClock unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
            return;
        }
        Slog.w(TAG, "updateSingleVerticalClock picIndex=" + picIndex);
        if (AodThemeManager.getInstance().getClockType() == 104) {
            bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getRealScreenWidth() - 48);
        } else {
            bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getVerticalClockImageWidth());
        }
        for (int i = 0; i < 2; i++) {
            if (this.mVerticalClockBitmap[picIndex + i] == null) {
                Slog.w(TAG, "handleDigitalClockCachedResources bitmap not exist:" + (picIndex + i));
            } else {
                int digitalClockImageHight = isFullFoldableScreen() ? getAlignWidth(bitmapHeight) : bitmapHeight;
                if (isFullFoldableScreen()) {
                    newBM = Bitmap.createBitmap(digitalClockImageHight, bitmapWidth, Bitmap.Config.ARGB_8888);
                } else {
                    newBM = Bitmap.createBitmap(bitmapWidth, digitalClockImageHight, Bitmap.Config.ARGB_8888);
                }
                if (newBM == null) {
                    Slog.e(TAG, "updateVerticalClockCachedResources return with null newBM");
                    return;
                }
                newBM.setDensity(DESIRED_DPI);
                new Canvas(newBM).drawBitmap(this.mVerticalClockBitmap[picIndex + i], 0.0f, 0.0f, (Paint) null);
                this.mVerticalClockBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 11);
                if (nativeUpdateClockBitmapBuffer(this.mAodDevicePtr, i) == 0) {
                    Slog.i(TAG, "updateVerticalClockCachedResources ok, result is" + this.mVerticalClockBufferSize);
                }
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                }
            }
        }
    }

    private void updateDualVerticalClock() {
        int left;
        int bitmapHeight;
        int bitmapWidth;
        int imageWidth;
        int bitmapHeight2;
        int bitmapWidth2;
        int clockType;
        int i;
        Bitmap newBM;
        int homePicIndex = getDualVerticalHomePicIndex();
        int locationPicIndex = getDualVerticalCurrPicIndex();
        if (homePicIndex == -1 || locationPicIndex == -1) {
            Slog.w(TAG, "updateDualVerticalClock unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
            return;
        }
        int imageWidth2 = getVerticalClockImageWidth();
        Slog.w(TAG, "updateDualVerticalClock homePicIndex=" + homePicIndex + ", locationPicIndex=" + locationPicIndex);
        int clockType2 = AodThemeManager.getInstance().getClockType();
        int i2 = 2;
        if (clockType2 == 104) {
            bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getRealScreenWidth() - 48);
            left = (bitmapWidth / 2) + 2;
        } else {
            bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(imageWidth2);
            left = getAlignWidth(imageWidth2) / 2;
        }
        int i3 = 0;
        while (i3 < i2) {
            Bitmap[] bitmapArr = this.mVerticalDoubleClockBitmap;
            if (bitmapArr[homePicIndex + i3] == null) {
                imageWidth = imageWidth2;
                bitmapWidth2 = bitmapWidth;
                bitmapHeight2 = bitmapHeight;
                clockType = clockType2;
                i = i2;
            } else if (bitmapArr[locationPicIndex + i3] == null) {
                imageWidth = imageWidth2;
                bitmapWidth2 = bitmapWidth;
                bitmapHeight2 = bitmapHeight;
                clockType = clockType2;
                i = i2;
            } else {
                int digitalClockImageHight = isFullFoldableScreen() ? getAlignWidth(bitmapHeight) : bitmapHeight;
                if (isFullFoldableScreen()) {
                    newBM = Bitmap.createBitmap(digitalClockImageHight, bitmapWidth, Bitmap.Config.ARGB_8888);
                } else {
                    newBM = Bitmap.createBitmap(bitmapWidth, digitalClockImageHight, Bitmap.Config.ARGB_8888);
                }
                if (newBM == null) {
                    Slog.e(TAG, "updateDualVerticalClock return with null newBM");
                    return;
                }
                newBM.setDensity(DESIRED_DPI);
                Canvas canvas = new Canvas(newBM);
                if (isFullFoldableScreen()) {
                    i = 2;
                    int top = getAlignWidth(imageWidth2) / 2;
                    int offset = getVerticalClockImageOffset();
                    bitmapWidth2 = bitmapWidth;
                    StringBuilder sb = new StringBuilder();
                    bitmapHeight2 = bitmapHeight;
                    sb.append("updateDualVerticalClock imageWidth:");
                    sb.append(imageWidth2);
                    sb.append(" top:");
                    sb.append(top);
                    sb.append(" offset:");
                    sb.append(offset);
                    Slog.i(TAG, sb.toString());
                    imageWidth = imageWidth2;
                    clockType = clockType2;
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[homePicIndex + i3], 0.0f, (float) (top - offset), (Paint) null);
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[locationPicIndex + i3], 0.0f, (float) offset, (Paint) null);
                } else {
                    imageWidth = imageWidth2;
                    bitmapWidth2 = bitmapWidth;
                    bitmapHeight2 = bitmapHeight;
                    clockType = clockType2;
                    i = 2;
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[homePicIndex + i3], 0.0f, 0.0f, (Paint) null);
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[locationPicIndex + i3], (float) left, 0.0f, (Paint) null);
                }
                this.mVerticalClockBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 11);
                if (nativeUpdateClockBitmapBuffer(this.mAodDevicePtr, i3) == 0) {
                    Slog.i(TAG, "updateVerticalClockCachedResources ok, result is" + this.mVerticalClockBufferSize);
                }
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                }
                i3++;
                i2 = i;
                clockType2 = clockType;
                bitmapWidth = bitmapWidth2;
                bitmapHeight = bitmapHeight2;
                imageWidth2 = imageWidth;
            }
            Slog.w(TAG, "updateDualVerticalClock dual bitmap not exist. " + i3);
            i3++;
            i2 = i;
            clockType2 = clockType;
            bitmapWidth = bitmapWidth2;
            bitmapHeight = bitmapHeight2;
            imageWidth2 = imageWidth;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVerticalClockCachedResources() {
        Slog.i(TAG, "updateVerticalClockCachedResources enter");
        if (this.mClockMode == 0) {
        }
        if (this.mClockMode == 0) {
            updateSingleVerticalClock();
        } else {
            updateDualVerticalClock();
        }
    }

    private static boolean useLocalizedNum() {
        String language = Locale.getDefault().getLanguage();
        return "ar".equals(language) || "fa".equals(language) || "bn".equals(language) || "ne".equals(language) || "mr".equals(language) || "as".equals(language);
    }

    private int getSingleVerticalPicIndex() {
        int picIndex;
        int picIndex2;
        int i = this.mBgFgdisplayPos;
        int picIndex3 = 2;
        if (i == 1) {
            if (useLocalizedNum()) {
                picIndex2 = 6;
            } else {
                picIndex2 = this.mVerticalDisplayNumber.left * 2;
            }
            return picIndex2;
        } else if (i == 2) {
            if (useLocalizedNum()) {
                picIndex = 20;
            } else {
                picIndex = this.mVerticalDisplayNumber.top * 2;
            }
            return picIndex;
        } else if (i == 0) {
            if (!useLocalizedNum()) {
                picIndex3 = 0;
            }
            return picIndex3;
        } else if (i == 3) {
            return 0;
        } else {
            if (i == 4) {
                return getStaticVerticalClockImagePicIndex(2);
            }
            return -1;
        }
    }

    private int getStaticVerticalClockImagePicIndex(int index) {
        String themeName = AodThemeManager.getInstance().getThemeName();
        if (!TextUtils.equals(AodThemeConst.STATIC_FISH_STYLE, themeName) && !TextUtils.equals(AodThemeConst.STATIC_BUTTERFLY_STYLE, themeName)) {
            return 0;
        }
        return useLocalizedNum() ? index : 0;
    }

    private void handleSingleVerticalClock() {
        int bitmapHeight;
        int bitmapWidth;
        Bitmap newBM;
        int picIndex = getSingleVerticalPicIndex();
        if (picIndex == -1) {
            Slog.w(TAG, "handleSingleVerticalClock unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
            return;
        }
        Slog.w(TAG, "handleSingleVerticalClock picIndex=" + picIndex);
        if (AodThemeManager.getInstance().getClockType() == 104) {
            bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getRealScreenWidth() - 48);
        } else {
            bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getVerticalClockImageWidth());
        }
        for (int i = 0; i < 2; i++) {
            if (this.mVerticalClockBitmap[picIndex + i] == null) {
                Slog.w(TAG, "handleSingleVerticalClock bitmap not exist:" + (picIndex + i));
            } else {
                int digitalClockImageHight = isFullFoldableScreen() ? getAlignWidth(bitmapHeight) : bitmapHeight;
                if (isFullFoldableScreen()) {
                    newBM = Bitmap.createBitmap(digitalClockImageHight, bitmapWidth, Bitmap.Config.ARGB_8888);
                } else {
                    newBM = Bitmap.createBitmap(bitmapWidth, digitalClockImageHight, Bitmap.Config.ARGB_8888);
                }
                if (newBM == null) {
                    Slog.e(TAG, "handleSingleVerticalClock return with null newBM");
                    return;
                }
                newBM.setDensity(DESIRED_DPI);
                new Canvas(newBM).drawBitmap(this.mVerticalClockBitmap[picIndex + i], 0.0f, 0.0f, (Paint) null);
                this.mVerticalClockBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 11);
                int i2 = this.mVerticalClockBitmapNum;
                if (i2 >= 2) {
                    Slog.w(TAG, "handleSingleVerticalClock mVerticalClockBitmapNum out of bounds " + this.mVerticalClockBitmapNum);
                    return;
                }
                if (nativeSetVerticalClockBitmap(this.mAodDevicePtr, i2) == 0) {
                    Slog.e(TAG, "handleSingleVerticalClock current SUCC num is " + (this.mVerticalClockBitmapNum + 1));
                    this.mVerticalClockBitmapNum = this.mVerticalClockBitmapNum + 1;
                }
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                }
            }
        }
    }

    private int getDualVerticalHomePicIndex() {
        int homePicIndex;
        int homePicIndex2;
        int i = this.mBgFgdisplayPos;
        if (i == 1) {
            if (useLocalizedNum()) {
                homePicIndex2 = 12;
            } else {
                homePicIndex2 = this.mVerticalDisplayNumber.left * 2;
            }
            return homePicIndex2;
        } else if (i == 2) {
            if (useLocalizedNum()) {
                homePicIndex = 40;
            } else {
                homePicIndex = this.mVerticalDisplayNumber.top * 2;
            }
            return homePicIndex;
        } else {
            int homePicIndex3 = 4;
            if (i == 0) {
                if (!useLocalizedNum()) {
                    homePicIndex3 = 0;
                }
                return homePicIndex3;
            } else if (i == 3) {
                return 0;
            } else {
                if (i == 4) {
                    return getStaticVerticalClockImagePicIndex(4);
                }
                return -1;
            }
        }
    }

    private int getDualVerticalCurrPicIndex() {
        int currPicIndex;
        int currPicIndex2;
        int i = this.mBgFgdisplayPos;
        int currPicIndex3 = 2;
        if (i == 1) {
            if (useLocalizedNum()) {
                currPicIndex2 = 12;
            } else {
                currPicIndex2 = (this.mVerticalDisplayNumber.right * 2) + 6;
            }
            return currPicIndex2;
        } else if (i == 2) {
            if (useLocalizedNum()) {
                currPicIndex = 40;
            } else {
                currPicIndex = (this.mVerticalDisplayNumber.bottom * 2) + 20;
            }
            return currPicIndex;
        } else if (i == 0) {
            if (useLocalizedNum()) {
                currPicIndex3 = 4;
            }
            return currPicIndex3;
        } else if (i == 3) {
            return 2;
        } else {
            if (i != 4) {
                return -1;
            }
            if (!useLocalizedNum()) {
                return 2;
            }
            return getStaticVerticalClockImagePicIndex(4);
        }
    }

    private void handleDualVerticalClock() {
        int left;
        int bitmapHeight;
        int bitmapWidth;
        int imageWidth;
        int bitmapHeight2;
        int bitmapWidth2;
        int clockType;
        int bitmapHeight3;
        Bitmap newBM;
        int homePicIndex = getDualVerticalHomePicIndex();
        int locationPicIndex = getDualVerticalCurrPicIndex();
        if (homePicIndex == -1 || locationPicIndex == -1) {
            Slog.w(TAG, "handleDualVerticalClock unexpected bgFgdisplayPos, mBgFgdisplayPos = " + this.mBgFgdisplayPos);
            return;
        }
        int imageWidth2 = getVerticalClockImageWidth();
        int clockType2 = AodThemeManager.getInstance().getClockType();
        int i = 2;
        if (clockType2 == 104) {
            bitmapHeight = VERTICAL4_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(getRealScreenWidth() - 48);
            left = (bitmapWidth / 2) + 2;
        } else {
            bitmapHeight = VERTICAL_CLOCK_IMAGE_HEIGHT;
            bitmapWidth = getAlignWidth(imageWidth2);
            left = getAlignWidth(imageWidth2) / 2;
        }
        Slog.w(TAG, "handleDualVerticalClock homePicIndex=" + homePicIndex + ", locationPicIndex=" + locationPicIndex + " VERTICAL_BG_FG_PIC_NUM:2 bitmapHeight:" + bitmapHeight + " bitmapWidth:" + bitmapWidth + " left:" + left + " clockType:" + clockType2);
        int i2 = 0;
        while (i2 < i) {
            Bitmap[] bitmapArr = this.mVerticalDoubleClockBitmap;
            if (bitmapArr[homePicIndex + i2] == null) {
                bitmapWidth2 = bitmapWidth;
                bitmapHeight2 = bitmapHeight;
                imageWidth = imageWidth2;
                clockType = clockType2;
                bitmapHeight3 = i;
            } else if (bitmapArr[locationPicIndex + i2] == null) {
                bitmapWidth2 = bitmapWidth;
                bitmapHeight2 = bitmapHeight;
                imageWidth = imageWidth2;
                clockType = clockType2;
                bitmapHeight3 = i;
            } else {
                int digitalClockImageHight = isFullFoldableScreen() ? getAlignWidth(bitmapHeight) : bitmapHeight;
                if (isFullFoldableScreen()) {
                    newBM = Bitmap.createBitmap(digitalClockImageHight, bitmapWidth, Bitmap.Config.ARGB_8888);
                } else {
                    newBM = Bitmap.createBitmap(bitmapWidth, digitalClockImageHight, Bitmap.Config.ARGB_8888);
                }
                if (newBM == null) {
                    Slog.e(TAG, "handleDualVerticalClock return with null newBM");
                    return;
                }
                newBM.setDensity(DESIRED_DPI);
                Canvas canvas = new Canvas(newBM);
                if (isFullFoldableScreen()) {
                    int top = getAlignWidth(imageWidth2) / 2;
                    int offset = getVerticalClockImageOffset();
                    bitmapWidth2 = bitmapWidth;
                    StringBuilder sb = new StringBuilder();
                    bitmapHeight2 = bitmapHeight;
                    sb.append("handleDualVerticalClock imageWidth:");
                    sb.append(imageWidth2);
                    sb.append(" top:");
                    sb.append(top);
                    sb.append(" offset:");
                    sb.append(offset);
                    Slog.i(TAG, sb.toString());
                    imageWidth = imageWidth2;
                    clockType = clockType2;
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[homePicIndex + i2], 0.0f, (float) (top - offset), (Paint) null);
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[locationPicIndex + i2], 0.0f, (float) offset, (Paint) null);
                } else {
                    bitmapWidth2 = bitmapWidth;
                    bitmapHeight2 = bitmapHeight;
                    imageWidth = imageWidth2;
                    clockType = clockType2;
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[homePicIndex + i2], 0.0f, 0.0f, (Paint) null);
                    canvas.drawBitmap(this.mVerticalDoubleClockBitmap[locationPicIndex + i2], (float) left, 0.0f, (Paint) null);
                }
                this.mVerticalClockBitmapBuffer = bitmap2BytesForARGB8888ToARGB4444(newBM, 11);
                int i3 = this.mVerticalClockBitmapNum;
                bitmapHeight3 = 2;
                if (i3 >= 2) {
                    Slog.w(TAG, "mVerticalClockBitmapNum out of bounds " + this.mVerticalClockBitmapNum);
                    return;
                }
                if (nativeSetVerticalClockBitmap(this.mAodDevicePtr, i3) == 0) {
                    Slog.i(TAG, "RESULT_OK index is " + i2 + ", current SUCC num is " + (this.mVerticalClockBitmapNum + 1));
                    this.mVerticalClockBitmapNum = this.mVerticalClockBitmapNum + 1;
                }
                if (!newBM.isRecycled()) {
                    newBM.recycle();
                }
                i2++;
                i = bitmapHeight3;
                clockType2 = clockType;
                bitmapWidth = bitmapWidth2;
                bitmapHeight = bitmapHeight2;
                imageWidth2 = imageWidth;
            }
            Slog.w(TAG, "handleDualVerticalClock dual bitmap not exist. " + i2);
            i2++;
            i = bitmapHeight3;
            clockType2 = clockType;
            bitmapWidth = bitmapWidth2;
            bitmapHeight = bitmapHeight2;
            imageWidth2 = imageWidth;
        }
    }

    private int getVerticalClockImageOffset() {
        if (sIsTahitiProduct || AodResUtil.isLowDensity()) {
            return 0;
        }
        return 4;
    }

    private void handleVerticalDigitalClockBgFgCachedResources() {
        needReloadBitmapResource(11);
        if (this.mClockMode == 0) {
        }
        if (this.mClockMode == 0) {
            handleSingleVerticalClock();
        } else {
            handleDualVerticalClock();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCachedResources() {
        needReloadBitmapResource(2);
        Bitmap newBM = Bitmap.createBitmap(TARGET_DYNAMIC_WIDTH, TARGET_DYNAMIC_HEIGHT, Bitmap.Config.RGB_565);
        if (newBM == null) {
            Slog.e(TAG, "handleCachedResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        Paint paint = new Paint(1);
        paint.setColor(mRadiusColor);
        Slog.i(TAG, "handleCachedResources enter.  mRadiusColor: " + mRadiusColor);
        canvas.scale(2.0f, 2.0f);
        for (int i = 0; i < 12; i++) {
            Bitmap[] bitmapArr = this.mBitmap;
            if (bitmapArr[i] == null) {
                Slog.w(TAG, "handleCachedResources bitmap not exist " + i);
            } else {
                canvas.drawBitmap(bitmapArr[i], 0.0f, 0.0f, (Paint) null);
                int i2 = TARGET_DYNAMIC_HEIGHT;
                canvas.drawCircle(((float) i2) / SCALE_RATIO_FOUR, ((float) i2) / SCALE_RATIO_FOUR, ((float) (mFingerPrintCircleRadius + 1)) / 2.0f, paint);
                this.mDynamicBitmapBuffer = bitmap2Bytes(newBM, 2);
                int i3 = this.mDynamicBitmapNum;
                if (i3 >= 12) {
                    Slog.w(TAG, "mDynamicBitmapNum out of bounds " + this.mDynamicBitmapNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                } else if (nativeSetDynamicBitmap(this.mAodDevicePtr, i3) == 0) {
                    Slog.i(TAG, "RESULT_OK index is " + i + ", current SUCC num is " + (this.mDynamicBitmapNum + 1));
                    this.mDynamicBitmapNum = this.mDynamicBitmapNum + 1;
                }
            }
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x018f  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x019b  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01a7 A[SYNTHETIC, Splitter:B:76:0x01a7] */
    private void handleAnimationResource() {
        Throwable th;
        PackageManager.NameNotFoundException e;
        FileInputStream inputSteam;
        Slog.w(TAG, "handleAnimationResource begin");
        FileInputStream inputSteam2 = null;
        try {
            boolean isDualClock = this.mClockMode != 0;
            Context aodContext = this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
            AodThemeManager.getInstance().initPreferenceManager(aodContext);
            int clockType = AodThemeManager.getInstance().getClockType();
            this.mAnimationBufferSize = getByteCount(clockType, isDualClock);
            if (this.mAnimationBufferSize == 0) {
                Slog.w(TAG, "handleAnimationResource err mAnimationBufferSize=0");
                if (0 != 0) {
                    try {
                        inputSteam2.close();
                    } catch (IOException e2) {
                        Slog.e(TAG, "handleAnimationResource() --> IOException");
                    }
                }
            } else {
                int maxAnimationCount = MAX_ANIMATION_ION_BYTES / getPageAlignSize(this.mAnimationBufferSize);
                Slog.w(TAG, "handleAnimationResource mAnimationBufferSize=" + this.mAnimationBufferSize + ", maxAnimationCount=" + maxAnimationCount);
                String directory = getDirectory(clockType, isDualClock);
                int showTimes = getShowTimes();
                int i = 0;
                while (i < showTimes) {
                    int index = 0;
                    byte[] bytes = new byte[this.mAnimationBufferSize];
                    File file = new File(directory + AodThemeConst.ANIM_FILE_NAME + 0 + AodThemeConst.ANIM_FILE_TILE);
                    while (true) {
                        try {
                            if (!file.exists()) {
                                inputSteam = inputSteam2;
                                break;
                            }
                            inputSteam = inputSteam2;
                            try {
                                if (this.mAnimationBitmapNum >= maxAnimationCount) {
                                    Slog.w(TAG, "mAnimationBitmapNum reach max animation number");
                                    break;
                                }
                                FileInputStream inputSteam3 = new FileInputStream(file);
                                while (inputSteam3.read(bytes) != -1) {
                                    this.mAnimationBitmapBuffer = bytes;
                                    if (nativeSetAnimationBitmap(this.mAodDevicePtr, this.mAnimationBitmapNum) == 0) {
                                        this.mAnimationBitmapNum++;
                                    }
                                    Slog.e(TAG, "mAnimationBitmapNum:" + this.mAnimationBitmapNum);
                                    inputSteam3 = inputSteam3;
                                    file = file;
                                    bytes = bytes;
                                }
                                index++;
                                file = new File(directory + AodThemeConst.ANIM_FILE_NAME + index + AodThemeConst.ANIM_FILE_TILE);
                                inputSteam2 = inputSteam3;
                                bytes = bytes;
                            } catch (FileNotFoundException e3) {
                                inputSteam2 = inputSteam;
                                Slog.e(TAG, "handleAnimationResource() --> FileNotFoundException");
                                if (inputSteam2 != null) {
                                }
                                Slog.w(TAG, "handleAnimationResource end");
                            } catch (IOException e4) {
                                inputSteam2 = inputSteam;
                                Slog.e(TAG, "handleAnimationResource() --> IOException");
                                if (inputSteam2 != null) {
                                }
                                Slog.w(TAG, "handleAnimationResource end");
                            } catch (PackageManager.NameNotFoundException e5) {
                                e = e5;
                                inputSteam2 = inputSteam;
                                try {
                                    Slog.w(TAG, "package not exist " + e.toString());
                                    if (inputSteam2 != null) {
                                    }
                                    Slog.w(TAG, "handleAnimationResource end");
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (inputSteam2 != null) {
                                        try {
                                            inputSteam2.close();
                                        } catch (IOException e6) {
                                            Slog.e(TAG, "handleAnimationResource() --> IOException");
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                inputSteam2 = inputSteam;
                                if (inputSteam2 != null) {
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e7) {
                            Slog.e(TAG, "handleAnimationResource() --> FileNotFoundException");
                            if (inputSteam2 != null) {
                            }
                            Slog.w(TAG, "handleAnimationResource end");
                        } catch (IOException e8) {
                            Slog.e(TAG, "handleAnimationResource() --> IOException");
                            if (inputSteam2 != null) {
                            }
                            Slog.w(TAG, "handleAnimationResource end");
                        } catch (PackageManager.NameNotFoundException e9) {
                            e = e9;
                            Slog.w(TAG, "package not exist " + e.toString());
                            if (inputSteam2 != null) {
                            }
                            Slog.w(TAG, "handleAnimationResource end");
                        } catch (Throwable th4) {
                            th = th4;
                            if (inputSteam2 != null) {
                            }
                            throw th;
                        }
                    }
                    i++;
                    isDualClock = isDualClock;
                    aodContext = aodContext;
                    inputSteam2 = inputSteam;
                }
                if (inputSteam2 != null) {
                    try {
                        inputSteam2.close();
                    } catch (IOException e10) {
                        Slog.e(TAG, "handleAnimationResource() --> IOException");
                    }
                }
                Slog.w(TAG, "handleAnimationResource end");
            }
        } catch (FileNotFoundException e11) {
            Slog.e(TAG, "handleAnimationResource() --> FileNotFoundException");
            if (inputSteam2 != null) {
                inputSteam2.close();
            }
            Slog.w(TAG, "handleAnimationResource end");
        } catch (IOException e12) {
            Slog.e(TAG, "handleAnimationResource() --> IOException");
            if (inputSteam2 != null) {
                inputSteam2.close();
            }
            Slog.w(TAG, "handleAnimationResource end");
        } catch (PackageManager.NameNotFoundException e13) {
            e = e13;
            Slog.w(TAG, "package not exist " + e.toString());
            if (inputSteam2 != null) {
                inputSteam2.close();
            }
            Slog.w(TAG, "handleAnimationResource end");
        }
    }

    private String getDirectory(int clockType, boolean isDualClock) {
        if (isFullFoldableScreen()) {
            if (clockType != 1000 || !isDualClock) {
                return AodThemeConst.ANIM_ROTATION_RES_DIR;
            }
            return AodThemeConst.ANIM_ROTATION_DUAL_RES_DIR;
        } else if (clockType == 1000 && isDualClock) {
            return AodThemeConst.ANIM_DUAL_RES_DIR;
        } else {
            if (clockType != 101 || !isDualClock) {
                return AodThemeConst.ANIM_RES_DIR;
            }
            return AodThemeConst.ANIM_DUAL_RES_DIR;
        }
    }

    private int parseInt(String num, int defaultNum) {
        if (TextUtils.isEmpty(num)) {
            return defaultNum;
        }
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            Slog.e(TAG, " parseInt error with string:" + num);
            return defaultNum;
        }
    }

    private int getShowTimes() {
        int num;
        if (!AodThemeConst.PERSONALITY_STYLE.equals(AodThemeManager.getInstance().getThemeName()) || (num = parseInt(AodThemeManager.getInstance().getAnimNum().split(",")[0], 60)) == 0) {
            return 1;
        }
        return 60 / num;
    }

    private int getByteCount(int clockType, boolean isDualClock) {
        if (clockType == 1000 && isDualClock) {
            return AodThemeManager.getInstance().getDualByteCount();
        }
        if (clockType != 101 || !isDualClock) {
            return AodThemeManager.getInstance().getByteCount();
        }
        return AodThemeManager.getInstance().getDualByteCount();
    }

    private void handleFaceIdCachedResources() {
        Bitmap newBM = Bitmap.createBitmap(FACE_ID_BITMAP_WITH_TEXT_WIDTH, FACE_ID_BITMAP_WITH_TEXT_HEIGHT, Bitmap.Config.RGB_565);
        if (newBM == null) {
            Slog.e(TAG, "handleFaceIdCachedResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        for (int i = 0; i < 12; i++) {
            Bitmap[] bitmapArr = this.mFaceIdBitmap;
            if (bitmapArr[i] == null || bitmapArr[i].isRecycled()) {
                Slog.w(TAG, "handleFaceIdCachedResources bitmap not exist " + i + ", mFaceIdBitmapNum=" + this.mFaceIdBitmapNum);
            } else {
                canvas.drawBitmap(this.mFaceIdBitmap[i], 0.0f, 0.0f, (Paint) null);
                this.mFaceIdBitmapBuffer = bitmap2Bytes(newBM, 4);
                int i2 = this.mFaceIdBitmapNum;
                if (i2 >= 12) {
                    Slog.w(TAG, "mFaceIdBitmapNum out of bounds " + this.mFaceIdBitmapNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                }
                int result = nativeSetFaceIdBitmap(this.mAodDevicePtr, i2);
                Slog.e(TAG, "nativeSetFaceIdBitmap i:" + i + ", mFaceIdBitmapNum:" + this.mFaceIdBitmapNum + " result:" + result);
                if (result == 0) {
                    Slog.i(TAG, "RESULT_OK index is " + i + ", current SUCC num is " + (this.mFaceIdBitmapNum + 1));
                    this.mFaceIdBitmapNum = this.mFaceIdBitmapNum + 1;
                }
            }
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePorscheImageResources() {
        Bitmap newBM = Bitmap.createBitmap(TARGET_PORSCHE_IMAGE_WIDTH, TARGET_PORSCHE_IMAGE_HEIGHT, Bitmap.Config.RGB_565);
        if (newBM == null) {
            Slog.e(TAG, "handleFpDynamicBitmapResources return with null newBM");
            return;
        }
        newBM.setDensity(DESIRED_DPI);
        Canvas canvas = new Canvas(newBM);
        for (int i = 0; i < 1; i++) {
            Bitmap[] bitmapArr = this.mPorscheImageBitmap;
            if (bitmapArr[i] == null) {
                Slog.w(TAG, "handlePorscheImageResources bitmap not exist " + i);
            } else {
                canvas.drawBitmap(bitmapArr[i], 0.0f, 0.0f, (Paint) null);
                this.mPorscheImageBuffer = bitmap2Bytes(newBM, 5);
                int i2 = this.mPorscheImageNum;
                if (i2 >= 1) {
                    Slog.w(TAG, "mPorscheImageNum out of bounds " + this.mPorscheImageNum);
                    if (!newBM.isRecycled()) {
                        newBM.recycle();
                        return;
                    }
                    return;
                } else if (nativeSetPorscheImage(this.mAodDevicePtr, i2) == 0) {
                    Slog.i(TAG, "handlePorscheImageResources RESULT_OK index is " + i + ", current SUCC num is " + (this.mPorscheImageNum + 1));
                    this.mPorscheImageNum = this.mPorscheImageNum + 1;
                }
            }
        }
        if (!newBM.isRecycled()) {
            newBM.recycle();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChargeTipsResources() {
        Bitmap bitmap;
        if (TextUtils.isEmpty(this.mChargeTips)) {
            Slog.e(TAG, "handleChargeTipsResources mChargeTips is empty.");
            return;
        }
        int chargeTipsIndex = this.mClockMode == 0 ? 26 : 35;
        Rect[] rectArr = this.aodInfoRect;
        if (rectArr.length <= chargeTipsIndex) {
            Slog.e(TAG, "handleChargeTipsResources aodInfoRect.length:" + this.aodInfoRect.length + " chargeTipsIndex:" + chargeTipsIndex);
            return;
        }
        Rect chargeTipsRect = rectArr[chargeTipsIndex];
        if (chargeTipsRect.isEmpty()) {
            Slog.e(TAG, "handleChargeTipsResources charge tips rect is empty.");
            return;
        }
        if (sIsTahitiProduct) {
            bitmap = Bitmap.createBitmap(chargeTipsRect.height(), chargeTipsRect.width(), Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(chargeTipsRect.width(), chargeTipsRect.height(), Bitmap.Config.RGB_565);
        }
        if (bitmap == null) {
            Slog.e(TAG, "handleFpDynamicBitmapResources return with null newBM");
            return;
        }
        bitmap.setDensity(DESIRED_DPI);
        drawChargeTipsText(bitmap, chargeTipsRect);
        if (sIsTahitiProduct) {
            bitmap = getTheRotatedBitmap(bitmap, FOLD_ROTATED_DEGREE);
        }
        this.mChargeTipsBitmapBuffer = bitmap2Bytes(bitmap, 13);
        if (nativeSetChargeTipsImage(this.mAodDevicePtr, this.mChargeTipsBitmapNum) == 0) {
            Slog.i(TAG, "handleChargeTipsResources RESULT_OK ");
            this.mChargeTipsBitmapNum++;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0028 A[Catch:{ Exception -> 0x002c }] */
    private void drawChargeTipsText(Bitmap bitmap, Rect chargeTipsRect) {
        int textSize;
        Canvas canvas;
        StaticLayout staticLayout;
        Typeface fontTypeface;
        TextPaint textPaint = new TextPaint();
        if (this.mDisplayMode == 2) {
            int i = currrentColorFlag;
            int[] iArr = TIME_TEXT_COLOR;
            if (i < iArr.length) {
                textPaint.setColor(iArr[i]);
                textPaint.setStyle(Paint.Style.FILL);
                fontTypeface = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
                if (fontTypeface != null) {
                    textPaint.setTypeface(fontTypeface);
                }
                textSize = 18;
                textPaint.setTextSize((float) dp2Px(18));
                canvas = new Canvas(bitmap);
                String str = this.mChargeTips;
                StaticLayout.Builder builder = StaticLayout.Builder.obtain(str, 0, str.length(), textPaint, canvas.getWidth());
                builder.setMaxLines(2);
                builder.setAlignment(Layout.Alignment.ALIGN_CENTER);
                staticLayout = builder.build();
                while (textSize > 9 && staticLayout.getLineCount() > 2) {
                    textSize--;
                    textPaint.setTextSize((float) dp2Px(textSize));
                    String str2 = this.mChargeTips;
                    StaticLayout.Builder builder2 = StaticLayout.Builder.obtain(str2, 0, str2.length(), textPaint, canvas.getWidth());
                    builder2.setMaxLines(2);
                    builder2.setAlignment(Layout.Alignment.ALIGN_CENTER);
                    staticLayout = builder2.build();
                }
                staticLayout.draw(canvas);
            }
        }
        textPaint.setColor(-1);
        textPaint.setStyle(Paint.Style.FILL);
        try {
            fontTypeface = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
            if (fontTypeface != null) {
            }
        } catch (Exception e) {
            Slog.e(TAG, "fontTypeface create fail.");
        }
        textSize = 18;
        textPaint.setTextSize((float) dp2Px(18));
        canvas = new Canvas(bitmap);
        String str3 = this.mChargeTips;
        StaticLayout.Builder builder3 = StaticLayout.Builder.obtain(str3, 0, str3.length(), textPaint, canvas.getWidth());
        builder3.setMaxLines(2);
        builder3.setAlignment(Layout.Alignment.ALIGN_CENTER);
        staticLayout = builder3.build();
        while (textSize > 9) {
            textSize--;
            textPaint.setTextSize((float) dp2Px(textSize));
            String str22 = this.mChargeTips;
            StaticLayout.Builder builder22 = StaticLayout.Builder.obtain(str22, 0, str22.length(), textPaint, canvas.getWidth());
            builder22.setMaxLines(2);
            builder22.setAlignment(Layout.Alignment.ALIGN_CENTER);
            staticLayout = builder22.build();
        }
        staticLayout.draw(canvas);
    }

    private void updateSingleBitmapBufferSize(int bytesAll, int iBitmapType) {
        switch (iBitmapType) {
            case 2:
                if (this.mSingleDynamicBitmapBufferSize == 0) {
                    this.mSingleDynamicBitmapBufferSize = bytesAll;
                    return;
                }
                return;
            case 3:
            default:
                return;
            case 4:
                if (this.mSingleFaceIdBitmapBufferSize == 0) {
                    this.mSingleFaceIdBitmapBufferSize = bytesAll;
                    return;
                }
                return;
            case 5:
                if (this.mSinglePorscheImageBufferSize == 0) {
                    this.mSinglePorscheImageBufferSize = bytesAll;
                    return;
                }
                return;
            case 6:
                if (this.mSingleDigitalClockBufferSize == 0) {
                    this.mSingleDigitalClockBufferSize = bytesAll;
                    return;
                }
                return;
            case 7:
                if (this.mSingleAnalogClockHourBufferSize == 0) {
                    this.mSingleAnalogClockHourBufferSize = bytesAll / 2;
                    return;
                }
                return;
            case 8:
                if (this.mPatternClockBufferSize == 0) {
                    this.mPatternClockBufferSize = bytesAll;
                    return;
                }
                return;
            case 9:
                if (this.mVolumeBarBufferSize == 0) {
                    this.mVolumeBarBufferSize = bytesAll / 2;
                    return;
                }
                return;
            case 10:
                if (this.mVolumeIconBufferSize == 0) {
                    this.mVolumeIconBufferSize = bytesAll / 2;
                    return;
                }
                return;
            case 11:
                if (this.mVerticalClockBufferSize == 0) {
                    this.mVerticalClockBufferSize = bytesAll / 2;
                    return;
                }
                return;
            case 12:
                if (this.mAnimationBufferSize == 0) {
                    this.mAnimationBufferSize = bytesAll;
                    return;
                }
                return;
            case 13:
                if (this.mChargeTipsBitmapBufferSize == 0) {
                    this.mChargeTipsBitmapBufferSize = bytesAll;
                    return;
                }
                return;
        }
    }

    @TargetApi(12)
    private byte[] bitmap2Bytes(Bitmap bitmap, int iBitmapType) {
        if (bitmap == null) {
            return new byte[0];
        }
        int bytesAll = bitmap.getByteCount();
        updateSingleBitmapBufferSize(bytesAll, iBitmapType);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesAll);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        Slog.i(TAG, "bitmap2Bytes, type:" + iBitmapType + " size:" + byteArray.length);
        return byteArray;
    }

    @TargetApi(12)
    private byte[] bitmap2BytesForARGB8888ToARGB4444(Bitmap bitmap, int iBitmapType) {
        if (bitmap == null) {
            return new byte[0];
        }
        int bytesAll = bitmap.getByteCount();
        updateSingleBitmapBufferSize(bytesAll, iBitmapType);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesAll);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        byte[] byteArrayConverted = new byte[(bytesAll / 2)];
        int tempFlag = 0;
        for (int bytes = 0; bytes < bytesAll; bytes++) {
            if (bytes > 1 && bytes % 4 == 0) {
                int tempHigh = (byteArray[bytes - 1] & 240) | ((byteArray[bytes - 4] & 240) >> 4);
                int tempFlag2 = tempFlag + 1;
                byteArrayConverted[tempFlag] = (byte) (((byteArray[bytes - 3] & 240) | ((byteArray[bytes - 2] & 240) >> 4)) & 255);
                tempFlag = tempFlag2 + 1;
                byteArrayConverted[tempFlag2] = (byte) (tempHigh & 255);
            }
        }
        Slog.i(TAG, "bitmap2BytesForARGB8888ToARGB4444, type:" + iBitmapType + " after convert size:" + byteArrayConverted.length);
        return byteArrayConverted;
    }

    private static int getRealScreenWidth() {
        String defaultScreenSize = SystemProperties.get("ro.config.default_screensize");
        if (!TextUtils.isEmpty(defaultScreenSize)) {
            String[] array = defaultScreenSize.split(",");
            if (array.length == 2) {
                try {
                    int width = Integer.parseInt(array[0]);
                    Slog.i(TAG, "display screen size from prop, width:" + width);
                    return width;
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "display screen size : NumberFormatException");
                }
            }
        }
        SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(SurfaceControl.getPhysicalDisplayToken(0));
        if (configs == null || configs.length == 0 || configs[0].width == 0 || configs[0].height == 0) {
            int width2 = SystemProperties.getInt("persist.sys.aps.defaultWidth", (int) SCREEN_DEFAULT_WIDTH);
            Slog.i(TAG, "display screen size from SystemProperties, width:" + width2);
            return width2;
        }
        int width3 = configs[0].width;
        Slog.i(TAG, "display screen size from SurfaceControl, width:" + configs[0].width + ", height:" + configs[0].height);
        return width3;
    }

    private static int getFpBitmapSize() {
        int width = getRealScreenWidth();
        if (width >= FP_BITMAP_SIZE_XXXHDPI) {
            Slog.i(TAG, "widthPixels:" + width + ", fingerprint bitmap size:" + FP_BITMAP_SIZE_XXXHDPI);
            return FP_BITMAP_SIZE_XXXHDPI;
        } else if (width >= 1000) {
            Slog.i(TAG, "widthPixels:" + width + ", fingerprint bitmap size:1000");
            return 1000;
        } else {
            Slog.i(TAG, "widthPixels:" + width + ", fingerprint bitmap size:" + FP_BITMAP_SIZE_HDPI);
            return FP_BITMAP_SIZE_HDPI;
        }
    }

    private static boolean hasFingerPrintInScreen() {
        if (sFingerprintType == -1) {
            sFingerprintType = FingerprintManagerEx.getHardwareType();
        }
        Slog.i(TAG, "sFingerprintType = " + sFingerprintType);
        int i = sFingerprintType;
        return i == 1 || i == 2;
    }

    private static int hasFingerPrintRadius() {
        if (sFingerprintType == -1 || mFingerPrintCircleRadius == -1) {
            mFingerPrintCircleRadius = FingerprintManagerEx.getHighLightspotRadius();
        }
        Slog.i(TAG, "mFingerPrintCircleRadius = " + mFingerPrintCircleRadius);
        return mFingerPrintCircleRadius;
    }

    /* access modifiers changed from: private */
    public static boolean isSettingSwitchEnabled(Context context, String switchName) {
        if (context == null) {
            Slog.i(TAG, " isSettingSwitchEnabled context is null.");
            return false;
        }
        int switchVal = Settings.Secure.getIntForUser(context.getContentResolver(), switchName, 0, ActivityManager.getCurrentUser());
        Slog.i(TAG, "isSettingSwitchEnabled switchName: " + switchName + " switchVal:" + switchVal);
        if (switchVal == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean isFaceSettingSwitchEnabled(Context context) {
        if (context == null) {
            Slog.i(TAG, " isSettingSwitchEnabled context is null.");
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        int switchVal = Settings.Secure.getIntForUser(resolver, FACE_KEYGUARD_WITH_LOCK, -1, ActivityManager.getCurrentUser());
        if (switchVal == -1) {
            if (Settings.Secure.getIntForUser(resolver, FACE_RECOGNIZE_UNLOCK, 0, 0) == 1 || Settings.Secure.getIntForUser(resolver, FACE_RECOGNIZE_SLIDE_UNLOCK, 0, 0) == 1) {
                return true;
            }
            return false;
        } else if (switchVal == 1) {
            return true;
        } else {
            return false;
        }
    }

    private static int getFingerprintSpotColor() {
        return FingerprintManagerEx.getUDFingerprintSpotColor();
    }

    private int getSignaturePicIndex(Context aodContext) {
        return Settings.Secure.getIntForUser(aodContext.getContentResolver(), "show_pic_index", 0, ActivityManager.getCurrentUser());
    }

    private String toConfiginfoString(AodConfigInfo aodInfo) {
        return "AodManagerService setAodConfig, mState:" + this.mState + " mDualClock:" + aodInfo.mDualClock + " mAODWorkMode:" + aodInfo.mAODWorkMode + " mDisplayMode:" + aodInfo.mDisplayMode + " mCurrentColorFlag:" + aodInfo.mCurrentColorFlag + " Radius:" + mFingerPrintCircleRadius + " mPowerState:" + this.mPowerState + " mIsTimeRefresh:" + aodInfo.mIsTimeRefresh + " mNotificationOffset:" + aodInfo.mNotificationOffset;
    }

    public void sendCommandToTp(int featureFlag, String cmdStr) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "sendCommandToTp") && IS_SUPPORT_AP) {
            this.tpCmdSender.sendCommandToTp(featureFlag, cmdStr);
        }
    }

    public void configTpInApMode(int aodState) {
        if (checkCallingPermission("com.huawei.permission.aod.UPDATE_AOD", "configTpInApMode") && IS_SUPPORT_AP) {
            this.tpCmdSender.configTpInApMode(this.mContext, aodState);
        }
    }

    private void updateFingerStatus() {
        if ((sIsTetonProduct || sIsTahitiProduct) && sIsSupportReconstruction && this.mAodQuitType != 0) {
            this.mFingerStatus = 0;
        } else {
            this.mFingerStatus = 2;
        }
    }
}
