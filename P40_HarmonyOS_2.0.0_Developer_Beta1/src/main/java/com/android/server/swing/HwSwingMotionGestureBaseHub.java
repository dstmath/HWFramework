package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.android.server.intellicom.common.SmartDualCardConsts;

public class HwSwingMotionGestureBaseHub {
    protected static final int AWARE_ACTION_UNKNOWN = 0;
    protected static final int AWARE_STATUS_UNKNOWN = 0;
    private static final String KEY_MOTION_ITEM_GRAB_SCREEN_CAPTURE = "item_grab_screen_capture_switch";
    private static final String KEY_MOTION_ITEM_HOVER_GESTURE_SWITCH = "item_hover_gesture_switch";
    private static final String KEY_MOTION_ITEM_PUSH_GESTURE_SWITCH = "item_push_gesture_switch";
    private static final String KEY_MOTION_ITEM_SPACED_SLIDING_SWITCH = "item_space_sliding_switch";
    protected static final int RECONNECT_MAX_COUNT = 3;
    protected static final int RECOONECT_WAIT_TIME_MS = 10000;
    protected static final int SWING_MOTION_DISABLED = 0;
    protected static final int SWING_MOTION_ENABLED = 1;
    private static final String TAG = "HwSwingMotionGestureBaseHub";
    private static final String TV_AIVISION_SWITCH = "tv_aivision_switch";
    private static final String TV_AI_GESTURE_PEOPLE_COUNT_SWITCH = "tv_airgesture_people_count_switch";
    private static final String TV_AI_GESTURE_SOUND_CHANGES_SWITCH = "tv_airgesture_sound_change_switch";
    private static final String TV_AI_GESTURE_SOUND_MUTE_SWITCH = "tv_airgesture_sound_mute_switch";
    private static final String TV_AI_GESTURE_VIDEO_PLAY_SWITCH = "tv_airgesture_video_play_switch";
    private static final String TV_AI_GESTURE_VIDEO_PROGRESS_SWITCH = "tv_airgesture_video_progress_switch";
    protected Handler mAwarenessHandler;
    protected int mAwarenessReconnectTimes;
    protected Context mContext;
    protected String mFocusPkgName;
    protected String mFocusWindowTitle;
    protected boolean mIsAwarenessConnected;
    protected boolean mIsSwingGrabScreenEnabled;
    protected boolean mIsSwingHoverGestureEnabled;
    protected boolean mIsSwingPushGestureEnabled;
    protected boolean mIsSwingSlideScreenEnabled;
    protected boolean mIsTvAiEnabled;
    protected boolean mIsTvPeopleCountEnabled;
    protected boolean mIsTvSoundChangeEnabled;
    protected boolean mIsTvSoundMuteEnabled;
    protected boolean mIsTvVideoPlayEnabled;
    protected boolean mIsTvVideoProcessEnabled;
    protected HwSwingMotionGestureDispatcher mMotionGestureDispatcher;
    private ContentObserver mSwingMotionObserver = new ContentObserver(this.mAwarenessHandler) {
        /* class com.android.server.swing.HwSwingMotionGestureBaseHub.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Log.i(HwSwingMotionGestureBaseHub.TAG, "observer swing motion switch");
            HwSwingMotionGestureBaseHub.this.refreshAwarenessConnection();
        }
    };

    protected HwSwingMotionGestureBaseHub(Context context) {
        Log.i(TAG, "constructor");
        this.mContext = context;
        this.mMotionGestureDispatcher = new HwSwingMotionGestureDispatcher(this.mContext);
        this.mAwarenessHandler = new Handler();
        registerGestureSwitchObserver();
        SwingStatusReceiver receiver = new SwingStatusReceiver();
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(receiver, statusFilter);
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwSwingMotionGestureBaseHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Log.i(HwSwingMotionGestureBaseHub.TAG, "on receive action:" + action);
            if (SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED.equals(action)) {
                HwSwingMotionGestureBaseHub.this.refreshAwarenessConnection();
            }
        }
    }

    public void start() {
        Log.i(TAG, "start");
        refreshAwarenessConnection();
    }

    public boolean dispatchUnhandledKey(KeyEvent event, String pkgName) {
        return false;
    }

    public void notifyRotationChange(int rotation) {
    }

    public void notifyFingersTouching(boolean isTouching) {
    }

    public void notifyFocusChange(String focusWindowTitle, String focusPkgName) {
        HwSwingReport.setFocusPkgName(focusPkgName);
    }

    /* access modifiers changed from: protected */
    public void refreshAwarenessConnection() {
        refreshGestureSwitch();
    }

    /* access modifiers changed from: protected */
    public boolean getSwingSlideScreenEnable() {
        return this.mIsSwingSlideScreenEnabled;
    }

    private void registerGestureSwitchObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AIVISION_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AI_GESTURE_PEOPLE_COUNT_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AI_GESTURE_VIDEO_PLAY_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AI_GESTURE_SOUND_MUTE_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AI_GESTURE_SOUND_CHANGES_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TV_AI_GESTURE_VIDEO_PROGRESS_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_MOTION_ITEM_SPACED_SLIDING_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_MOTION_ITEM_GRAB_SCREEN_CAPTURE), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_MOTION_ITEM_HOVER_GESTURE_SWITCH), true, this.mSwingMotionObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_MOTION_ITEM_PUSH_GESTURE_SWITCH), true, this.mSwingMotionObserver, -1);
    }

    private void refreshGestureSwitch() {
        boolean z = false;
        this.mIsTvAiEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AIVISION_SWITCH, 0, -2) == 1;
        this.mIsTvPeopleCountEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AI_GESTURE_PEOPLE_COUNT_SWITCH, 0, -2) == 1;
        this.mIsTvVideoPlayEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AI_GESTURE_VIDEO_PLAY_SWITCH, 0, -2) == 1;
        this.mIsTvSoundMuteEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AI_GESTURE_SOUND_MUTE_SWITCH, 0, -2) == 1;
        this.mIsTvSoundChangeEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AI_GESTURE_SOUND_CHANGES_SWITCH, 0, -2) == 1;
        this.mIsTvVideoProcessEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TV_AI_GESTURE_VIDEO_PROGRESS_SWITCH, 0, -2) == 1;
        this.mIsSwingGrabScreenEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_GRAB_SCREEN_CAPTURE, 0, -2) == 1;
        this.mIsSwingSlideScreenEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_SPACED_SLIDING_SWITCH, 0, -2) == 1;
        this.mIsSwingHoverGestureEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_HOVER_GESTURE_SWITCH, 0, -2) == 1;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_PUSH_GESTURE_SWITCH, 0, -2) == 1) {
            z = true;
        }
        this.mIsSwingPushGestureEnabled = z;
    }
}
