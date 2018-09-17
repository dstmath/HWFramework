package com.huawei.motiondetection;

public final class MotionTypeApps {
    public static final String KEY_ACTIVITY = "motion_activity";
    public static final String KEY_ACTIVITY_RECO = "motion_activity_state";
    public static final String KEY_FLIP = "motion_flip";
    public static final String KEY_FLIP_MUTE_AOD = "motion_flip_mute_aod";
    public static final String KEY_FLIP_MUTE_CALL = "motion_flip_mute_call";
    public static final String KEY_FLIP_MUTE_CLOCK = "motion_flip_mute_clock";
    public static final String KEY_HW_STEP_COUNTER = "motion_hw_step_counter";
    public static final String KEY_HW_STEP_COUNTER_HEALTH = "motion_hw_step_counter_health";
    public static final String KEY_PICKUP = "motion_pickup";
    public static final String KEY_PICKUP_END_HINTS = "motion_pickup_end_hints";
    public static final String KEY_PICKUP_REDUCE_CALL = "motion_pickup_reduce_call";
    public static final String KEY_PICKUP_REDUCE_CLOCK = "motion_pickup_reduce_clock";
    public static final String KEY_PICKUP_WAKESCREEN = "motion_pickup_wake_screen";
    public static final String KEY_POCKET = "motion_pocket";
    public static final String KEY_POCKET_AOD = "motion_pocket_aod";
    public static final String KEY_POCKET_CALL_RAISE = "motion_pocket_call_raise";
    public static final String KEY_PROXI = "motion_proximity";
    public static final String KEY_PROXI_ANSWER = "motion_proximity_answer";
    public static final String KEY_PROXI_BLUETOOTHSET = "motion_proximity_bluetoothset";
    public static final String KEY_PROXI_DIAL = "motion_proximity_dial";
    public static final String KEY_PROXI_SCREEN_OFF = "motion_proximity_screen_off";
    public static final String KEY_PROXI_SPEAKER = "motion_proximity_speaker";
    public static final String KEY_SHAKE = "motion_shake";
    public static final String KEY_SHAKE_CHANGE_WALLPAPER = "motion_shake_change_wallpaper";
    public static final String KEY_SHAKE_REARRAGE_WIDGETS = "motion_shake_rearrange_widgets";
    public static final String KEY_SHAKE_REFRESH = "motion_shake_refresh";
    public static final String KEY_SHAKE_START_PRIVACY = "motion_shake_start_privacy";
    public static final String KEY_SWIPE = "motion_swipe";
    public static final String KEY_SWIPE_ANSWER = "motion_swipe_answer";
    public static final String KEY_TAKE_OFF = "motion_take_off";
    public static final String KEY_TAKE_OFF_EAR = "motion_take_off_ear";
    public static final String KEY_TAP = "motion_tap";
    public static final String KEY_TAP_TOP = "motion_tap_top";
    public static final String KEY_TILT_LR = "motion_tilt_lr";
    public static final String KEY_TILT_LR_MOVE_WIDGETS = "motion_tilt_lr_move_widgets";
    public static final String KEY_TILT_LR_SINGLE_HAND = "motion_tilt_lr_single_hand";
    @Deprecated
    public static final String MOTION_KEY_APPS_FLIP_SILENT = "motion_flip_silent";
    @Deprecated
    public static final String MOTION_KEY_APPS_PICKUP_WEAKEN = "motion_pickup_weaken";
    public static final String MOTION_KEY_APPS_PROXI_ANSWER = "motion_proximity_answer_call";
    public static final int MOTION_TYPE_APPS_FLIP_SILENT = 2;
    public static final int MOTION_TYPE_APPS_PICKUP_WEAKEN = 1;
    public static final int MOTION_TYPE_APPS_PROXIMITY_ANSWER = 3;
    public static final int MOTION_TYPE_OPERAND = 100;
    public static final int TYPE_ACTIVITY = 900;
    public static final int TYPE_ACTIVITY_RECO = 901;
    public static final int TYPE_FLIP = 200;
    public static final int TYPE_FLIP_MUTE_AOD = 203;
    public static final int TYPE_FLIP_MUTE_CALL = 201;
    public static final int TYPE_FLIP_MUTE_CLOCK = 202;
    public static final int TYPE_HW_STEP_COUNTER = 1100;
    public static final int TYPE_HW_STEP_COUNTER_HEALTH = 1101;
    public static final int TYPE_NOT_SUPPORT = -1;
    public static final int TYPE_PICKUP = 100;
    public static final int TYPE_PICKUP_END_HINTS = 103;
    public static final int TYPE_PICKUP_REDUCE_CALL = 101;
    public static final int TYPE_PICKUP_REDUCE_CLOCK = 102;
    public static final int TYPE_PICKUP_WAKESCREEN = 104;
    public static final int TYPE_POCKET = 800;
    public static final int TYPE_POCKET_AOD = 802;
    public static final int TYPE_POCKET_CALL_RAISE = 801;
    public static final int TYPE_PROXIMITY = 300;
    public static final int TYPE_PROXIMITY_ANSWER = 301;
    public static final int TYPE_PROXIMITY_BLUETOOTHSET = 305;
    public static final int TYPE_PROXIMITY_DIAL = 302;
    public static final int TYPE_PROXIMITY_SCREEN_OFF = 303;
    public static final int TYPE_PROXIMITY_SPEAKER = 304;
    public static final int TYPE_ROTATION = 700;
    public static final int TYPE_ROTATION_SCREEN = 701;
    public static final int TYPE_SHAKE = 400;
    public static final int TYPE_SHAKE_CHANGE_WALLPAPER = 402;
    public static final int TYPE_SHAKE_REARRAGE_WIDGETS = 403;
    public static final int TYPE_SHAKE_REFRESH = 401;
    public static final int TYPE_SHAKE_START_PRIVACY = 404;
    public static final int TYPE_SWIPE = 1200;
    public static final int TYPE_SWIPE_ANSWER = 1201;
    public static final int TYPE_TAKE_OFF = 1000;
    public static final int TYPE_TAKE_OFF_EAR = 1001;
    public static final int TYPE_TAP = 500;
    public static final int TYPE_TAP_TOP = 501;
    public static final int TYPE_TILT_LR = 600;
    public static final int TYPE_TILT_LR_MOVE_WIDGETS = 601;
    public static final int TYPE_TILT_LR_SINGLE_HAND = 602;

    private MotionTypeApps() {
    }

    public static int getMotionTypeByMotionApps(int motionApps) {
        return motionApps - (motionApps % 100);
    }

    public static String getMotionKeyByMotionApps(int motionApps) {
        String motionKey = "";
        switch (getMotionTypeByMotionApps(motionApps)) {
            case 100:
                return getPickupKey(motionApps);
            case 200:
                return getFlipKey(motionApps);
            case 300:
                return getProxiKey(motionApps);
            case 400:
                return getShakeKey(motionApps);
            case 500:
                return getTapKey(motionApps);
            case 600:
                return getTiltLrKey(motionApps);
            case 800:
                return getPocketKey(motionApps);
            case 900:
                return getActivityKey(motionApps);
            case 1000:
                return getTakeoffKey(motionApps);
            case 1100:
                return getHwStepCounterKey(motionApps);
            case 1200:
                return getSwipeKey(motionApps);
            default:
                return motionKey;
        }
    }

    private static String getHwStepCounterKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 1100:
                return KEY_HW_STEP_COUNTER;
            case TYPE_HW_STEP_COUNTER_HEALTH /*1101*/:
                return KEY_HW_STEP_COUNTER_HEALTH;
            default:
                return motionKey;
        }
    }

    private static String getTakeoffKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 1000:
                return KEY_TAKE_OFF;
            case 1001:
                return KEY_TAKE_OFF_EAR;
            default:
                return motionKey;
        }
    }

    private static String getActivityKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 900:
                return KEY_ACTIVITY;
            case TYPE_ACTIVITY_RECO /*901*/:
                return KEY_ACTIVITY_RECO;
            default:
                return motionKey;
        }
    }

    private static String getTapKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 500:
                return KEY_TAP;
            case 501:
                return KEY_TAP_TOP;
            default:
                return motionKey;
        }
    }

    private static String getPocketKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 800:
                return KEY_POCKET;
            case TYPE_POCKET_CALL_RAISE /*801*/:
                return KEY_POCKET_CALL_RAISE;
            case TYPE_POCKET_AOD /*802*/:
                return KEY_POCKET_AOD;
            default:
                return motionKey;
        }
    }

    private static String getSwipeKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 1200:
                return KEY_SWIPE;
            case TYPE_SWIPE_ANSWER /*1201*/:
                return KEY_SWIPE_ANSWER;
            default:
                return motionKey;
        }
    }

    private static String getTiltLrKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 600:
                return KEY_TILT_LR;
            case 601:
                return KEY_TILT_LR_MOVE_WIDGETS;
            case 602:
                return KEY_TILT_LR_SINGLE_HAND;
            default:
                return motionKey;
        }
    }

    private static String getShakeKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 400:
                return KEY_SHAKE;
            case TYPE_SHAKE_REFRESH /*401*/:
                return KEY_SHAKE_REFRESH;
            case TYPE_SHAKE_CHANGE_WALLPAPER /*402*/:
                return KEY_SHAKE_CHANGE_WALLPAPER;
            case TYPE_SHAKE_REARRAGE_WIDGETS /*403*/:
                return KEY_SHAKE_REARRAGE_WIDGETS;
            case TYPE_SHAKE_START_PRIVACY /*404*/:
                return KEY_SHAKE_START_PRIVACY;
            default:
                return motionKey;
        }
    }

    private static String getProxiKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 300:
                return KEY_PROXI;
            case TYPE_PROXIMITY_ANSWER /*301*/:
                return KEY_PROXI_ANSWER;
            case TYPE_PROXIMITY_DIAL /*302*/:
                return KEY_PROXI_DIAL;
            case TYPE_PROXIMITY_SCREEN_OFF /*303*/:
                return KEY_PROXI_SCREEN_OFF;
            case TYPE_PROXIMITY_SPEAKER /*304*/:
                return KEY_PROXI_SPEAKER;
            case TYPE_PROXIMITY_BLUETOOTHSET /*305*/:
                return KEY_PROXI_BLUETOOTHSET;
            default:
                return motionKey;
        }
    }

    private static String getFlipKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 200:
                return KEY_FLIP;
            case TYPE_FLIP_MUTE_CALL /*201*/:
                return KEY_FLIP_MUTE_CALL;
            case TYPE_FLIP_MUTE_CLOCK /*202*/:
                return KEY_FLIP_MUTE_CLOCK;
            case TYPE_FLIP_MUTE_AOD /*203*/:
                return KEY_FLIP_MUTE_AOD;
            default:
                return motionKey;
        }
    }

    private static String getPickupKey(int motionApps) {
        String motionKey = "";
        switch (motionApps) {
            case 100:
                return KEY_PICKUP;
            case 101:
                return KEY_PICKUP_REDUCE_CALL;
            case 102:
                return KEY_PICKUP_REDUCE_CLOCK;
            case 103:
                return KEY_PICKUP_END_HINTS;
            case 104:
                return KEY_PICKUP_WAKESCREEN;
            default:
                return motionKey;
        }
    }
}
