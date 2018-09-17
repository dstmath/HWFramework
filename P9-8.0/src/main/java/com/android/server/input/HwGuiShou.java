package com.android.server.input;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;

public class HwGuiShou {
    private static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    private static boolean GUISHOU_ENABLED = false;
    private static final int SIM_SUB1 = 0;
    private static final int SIM_SUB2 = 1;
    private static final String TAG = "HwGuiShou";
    private Context mContext;
    private FingerprintNavigation mFingerprintNavigationFilter;
    private FpSlideSwitchSettingsObserver mFpSlideSwitchSettingsObserver;
    private Handler mHandler;
    private HwInputManagerService mHwInputManagerService;
    private int mInCallCount = 0;
    private boolean mIsPhoneStateListenerNotRegister = true;
    private Looper mLooper;
    private TelephonyManager mPhoneManager;
    private ContentResolver mResolver;
    private int mTempSlideSwitch = 0;

    class FpSlideSwitchSettingsObserver extends ContentObserver {
        public boolean mmForceUpdateChange = false;

        FpSlideSwitchSettingsObserver(Handler handler) {
            super(handler);
        }

        public void registerContentObserver(int userId) {
            HwGuiShou.this.mResolver.registerContentObserver(Secure.getUriFor(HwGuiShou.FINGERPRINT_SLIDE_SWITCH), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            int i = 1;
            boolean injectSlide = Secure.getIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, 0, ActivityManager.getCurrentUser()) != 0;
            if (this.mmForceUpdateChange || HwGuiShou.this.mInCallCount <= 0 || (injectSlide ^ 1) == 0) {
                this.mmForceUpdateChange = false;
                Log.d(HwGuiShou.TAG, "open fingerprint nav=" + injectSlide);
                ContentResolver -get3 = HwGuiShou.this.mResolver;
                String str = HwGuiShou.FINGERPRINT_SLIDE_SWITCH;
                if (!injectSlide) {
                    i = 0;
                }
                System.putInt(-get3, str, i);
                return;
            }
            Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, 1, ActivityManager.getCurrentUser());
        }
    }

    static {
        GUISHOU_ENABLED = false;
        boolean isStopAlarmDisabled = SystemProperties.getBoolean("ro.config.fp_rm_alarm", false);
        boolean isGallerySlideEnabled = SystemProperties.getBoolean("ro.config.fp_navigation", true);
        boolean isNotificationSlideEnabledByAdd = SystemProperties.getBoolean("ro.config.fp_add_notification", true);
        boolean isNotificationSlideDisabledByRm = SystemProperties.getBoolean("ro.config.fp_rm_notification", false);
        if (isStopAlarmDisabled || (isGallerySlideEnabled ^ 1) == 0) {
            isNotificationSlideDisabledByRm = false;
        } else if (!isNotificationSlideEnabledByAdd) {
            isNotificationSlideDisabledByRm = true;
        }
        GUISHOU_ENABLED = isNotificationSlideDisabledByRm;
    }

    public HwGuiShou(HwInputManagerService inputService, Context context, Handler handler, FingerprintNavigation fn) {
        this.mContext = context;
        this.mHandler = handler;
        this.mHwInputManagerService = inputService;
        this.mFingerprintNavigationFilter = fn;
        if (context != null) {
            this.mResolver = context.getContentResolver();
        }
    }

    public static boolean isGuiShouEnabled() {
        return GUISHOU_ENABLED;
    }

    public static boolean isFPNavigationInThreeAppsEnabled(boolean injectCamera, boolean answerCall, boolean stopAlarm) {
        if (isGuiShouEnabled()) {
            return true;
        }
        boolean z = (injectCamera || (answerCall ^ 1) == 0) ? false : stopAlarm ^ 1;
        return z;
    }

    private boolean isDefaultSlideSwitchOn() {
        if (this.mHwInputManagerService != null) {
            return this.mHwInputManagerService.isDefaultSlideSwitchOn();
        }
        return true;
    }

    private void registerPhoneStateListenerBySub(int sub) {
        this.mTempSlideSwitch = isDefaultSlideSwitchOn() ? 1 : 0;
        PhoneStateListener listener = new PhoneStateListener(Integer.valueOf(sub), this.mLooper) {
            public void onCallStateChanged(int state, String incomingNumber) {
                int i = 0;
                HwGuiShou hwGuiShou;
                switch (state) {
                    case 0:
                        Slog.i(HwGuiShou.TAG, "call state: idle");
                        HwGuiShou hwGuiShou2 = HwGuiShou.this;
                        hwGuiShou2.mInCallCount = hwGuiShou2.mInCallCount - 1;
                        if (HwGuiShou.this.mInCallCount == 0) {
                            Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.this.mTempSlideSwitch, ActivityManager.getCurrentUser());
                            return;
                        } else if (HwGuiShou.this.mInCallCount < 0) {
                            HwGuiShou.this.mInCallCount = 0;
                            return;
                        } else {
                            return;
                        }
                    case 1:
                        Slog.i(HwGuiShou.TAG, "call state: ringing");
                        if (HwGuiShou.this.mInCallCount == 0) {
                            if (HwGuiShou.this.mFingerprintNavigationFilter.isAlarm()) {
                                hwGuiShou = HwGuiShou.this;
                                if (HwGuiShou.this.isDefaultSlideSwitchOn()) {
                                    i = 1;
                                }
                                hwGuiShou.mTempSlideSwitch = i;
                            } else {
                                HwGuiShou.this.mTempSlideSwitch = Secure.getIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, 0, ActivityManager.getCurrentUser());
                            }
                        }
                        HwGuiShou hwGuiShou3 = HwGuiShou.this;
                        hwGuiShou3.mInCallCount = hwGuiShou3.mInCallCount + 1;
                        Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, 1, ActivityManager.getCurrentUser());
                        return;
                    case 2:
                        Slog.i(HwGuiShou.TAG, "call state: offhook");
                        hwGuiShou = HwGuiShou.this;
                        if (HwGuiShou.this.isDefaultSlideSwitchOn()) {
                            i = 1;
                        }
                        hwGuiShou.mTempSlideSwitch = i;
                        HwGuiShou.this.mFpSlideSwitchSettingsObserver.mmForceUpdateChange = true;
                        Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.this.mTempSlideSwitch, ActivityManager.getCurrentUser());
                        return;
                    default:
                        return;
                }
            }
        };
        if (this.mPhoneManager != null) {
            this.mPhoneManager.listen(listener, 481);
        }
    }

    public void registerPhoneStateListener() {
        if (isGuiShouEnabled()) {
            this.mLooper = Looper.myLooper();
            this.mPhoneManager = TelephonyManager.from(this.mContext);
            if (this.mPhoneManager != null && this.mIsPhoneStateListenerNotRegister) {
                registerPhoneStateListenerBySub(0);
                registerPhoneStateListenerBySub(1);
                this.mIsPhoneStateListenerNotRegister = false;
            }
        }
    }

    public void registerFpSlideSwitchSettingsObserver() {
        if (isGuiShouEnabled()) {
            this.mFpSlideSwitchSettingsObserver = new FpSlideSwitchSettingsObserver(this.mHandler);
            this.mFpSlideSwitchSettingsObserver.registerContentObserver(UserHandle.myUserId());
        }
    }

    public void registerFpSlideSwitchSettingsObserver(int newUserId) {
        if (isGuiShouEnabled()) {
            this.mFpSlideSwitchSettingsObserver = new FpSlideSwitchSettingsObserver(this.mHandler);
            this.mFpSlideSwitchSettingsObserver.registerContentObserver(newUserId);
        }
    }

    public void setFingerprintSlideSwitchValue(boolean value) {
        if (isGuiShouEnabled()) {
            Secure.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, value ? 1 : 0, ActivityManager.getCurrentUser());
        }
    }
}
