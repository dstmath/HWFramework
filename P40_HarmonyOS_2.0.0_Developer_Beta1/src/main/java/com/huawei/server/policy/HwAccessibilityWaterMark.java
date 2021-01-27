package com.huawei.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.huawei.android.hardware.input.HwSideTouchManagerEx;

public class HwAccessibilityWaterMark {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String ACCESSIBILITY_WATERMARK_ENABLED = "accessibility_watermark_enabled";
    private static final String BROADCAST_NOTIFY_DESKTOP_MODE = "com.android.server.pc.action.desktop_mode";
    private static final boolean ISDEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean IS_SIDE_PROP;
    private static final int MSG_ADD_WATERMARK_VIEW = 0;
    private static final int MSG_ON_DISPLAY_CHANGE = 3;
    private static final int MSG_REMOVE_WATERMARK_VIEW = 1;
    private static final int MSG_UPDATE_TEXT_FORCED = 4;
    private static final int MSG_UPDATE_VIEW_TEXT = 2;
    private static final String PERMISSION_BROADCAST_VASSIST_DESKTOP = "com.huawei.permission.VASSIST_DESKTOP";
    private static final int POWEROFF_TIMEOUT = 3;
    private static final int PROCESS_DISPLAYCHG_MSG_DELAY = 50;
    private static final String TAG = "HwAccessibilityWaterMark";
    private static final String TALKBACK_SCREENSPEAK = "com.bjbyhd.screenreader_huawei/com.bjbyhd.screenreader_huawei.ScreenReaderService";
    private static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String WATERMARK_WINDOW_NAME = "Emui:A11WaterMarkWnd";
    private A11WaterMarkHandler mA11WatermarkHandle;
    private ContentObserver mAccessWaterMarkObserver;
    private Context mContext;
    private int mCurUser;
    private String mEnabledAccessibilityServices;
    private boolean mIsAccessibilityLockScreenOn;
    private boolean mIsAccessibilityServiceOn;
    private boolean mIsKeyGuardShowing;
    private boolean mIsShortCutSwitchOn;
    private boolean mIsShortcutEnabled;
    private boolean mIsWaterMarkEnabled;
    private boolean mIsWaterMarkViewAdded;
    private boolean mIsWatermarkBroadcastRegister = false;
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.policy.HwAccessibilityWaterMark.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(HwAccessibilityWaterMark.TAG, "on receive localeChangeReceiver");
            HwAccessibilityWaterMark.this.mA11WatermarkHandle.sendEmptyMessage(4);
        }
    };
    private WindowManager.LayoutParams mParams = null;
    private ContentResolver mResolver;
    private String mShortcutAccessibilityServices;
    private View mWaterMarkView = null;
    private BroadcastReceiver mWatermarkPcModeChangeReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.policy.HwAccessibilityWaterMark.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if (!HwAccessibilityWaterMark.BROADCAST_NOTIFY_DESKTOP_MODE.equals(action)) {
                    Log.i(HwAccessibilityWaterMark.TAG, "onReceive action: " + action);
                    return;
                }
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (HwAccessibilityWaterMark.ISDEBUG) {
                        Log.i(HwAccessibilityWaterMark.TAG, "PC mode change: " + extras.get("mode"));
                    }
                    HwAccessibilityWaterMark.this.mA11WatermarkHandle.sendEmptyMessage(3);
                }
            }
        }
    };
    private WindowManager mWindowManager;

    static {
        boolean z = false;
        if (HwSideTouchManagerEx.getInstance().getSideTouchMode() == 1) {
            z = true;
        }
        IS_SIDE_PROP = z;
    }

    public HwAccessibilityWaterMark(Context context) {
        this.mResolver = context.getContentResolver();
        this.mContext = context;
        this.mCurUser = ActivityManager.getCurrentUser();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mAccessWaterMarkObserver = new ContentObserver(new Handler()) {
            /* class com.huawei.server.policy.HwAccessibilityWaterMark.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isChange) {
                HwAccessibilityWaterMark.this.updateAccessWaterMarkStat();
                HwAccessibilityWaterMark.this.updateAccessibilityWaterMarkView();
                HwAccessibilityWaterMark.this.mA11WatermarkHandle.sendEmptyMessage(2);
            }
        };
        updateAccessWaterMarkStat();
        registerAccessWaterMarkStat(this.mCurUser);
        registerA11WatermarkBroadcast();
        this.mA11WatermarkHandle = new A11WaterMarkHandler();
    }

    /* access modifiers changed from: private */
    public class A11WaterMarkHandler extends Handler {
        private A11WaterMarkHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwAccessibilityWaterMark.this.prepareWaterMarkView();
            } else if (i == 1) {
                HwAccessibilityWaterMark.this.removeWaterMarkView();
            } else if (i == 2) {
                HwAccessibilityWaterMark.this.updateAccessibilityWaterMarkText();
            } else if (i == 3) {
                HwAccessibilityWaterMark.this.updateOnDisplayChange();
            } else if (i == 4) {
                HwAccessibilityWaterMark.this.updateA11WaterMarkTextForced();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOnDisplayChange() {
        if (ISDEBUG) {
            Log.i(TAG, "rmv view because display id change ");
        }
        this.mA11WatermarkHandle.sendEmptyMessage(1);
        updateAccessWaterMarkStat();
        if (!this.mIsWaterMarkEnabled) {
            return;
        }
        if (isTalkBackServicesOn() || isScreenSpeakServicesOn()) {
            this.mA11WatermarkHandle.sendEmptyMessageDelayed(0, 50);
            updateShortCutSwitch();
            this.mA11WatermarkHandle.sendEmptyMessageDelayed(4, 50);
        }
    }

    private boolean isTalkBackServicesOn() {
        String str = this.mEnabledAccessibilityServices;
        if (str == null || !str.contains(TALKBACK_SERVICE)) {
            return false;
        }
        if (!ISDEBUG) {
            return true;
        }
        Log.i(TAG, "talkback On");
        return true;
    }

    private boolean isScreenSpeakServicesOn() {
        String str = this.mEnabledAccessibilityServices;
        if (str == null || !str.contains(TALKBACK_SCREENSPEAK)) {
            return false;
        }
        if (!ISDEBUG) {
            return true;
        }
        Log.i(TAG, "ScreenSpeakServices On");
        return true;
    }

    private boolean isShortCutScreenSpeakServiceOn() {
        String str = this.mShortcutAccessibilityServices;
        if (str == null || !str.equals(TALKBACK_SCREENSPEAK) || !isScreenSpeakServicesOn()) {
            return false;
        }
        if (!ISDEBUG) {
            return true;
        }
        Log.i(TAG, "ShortCut ScreenSpeakServices On");
        return true;
    }

    private boolean isShortCutTalkbackServiceOn() {
        String str = this.mShortcutAccessibilityServices;
        if (str == null || !str.equals(TALKBACK_SERVICE) || !isTalkBackServicesOn()) {
            return false;
        }
        if (!ISDEBUG) {
            return true;
        }
        Log.i(TAG, "ShortCut talkbackServices On");
        return true;
    }

    private void updateShortCutSwitch() {
        if (IS_SIDE_PROP) {
            this.mIsShortCutSwitchOn = false;
        } else if (!this.mIsKeyGuardShowing || this.mIsAccessibilityLockScreenOn) {
            if (!this.mIsShortcutEnabled || (!isShortCutTalkbackServiceOn() && !isShortCutScreenSpeakServiceOn())) {
                this.mIsShortCutSwitchOn = false;
            } else {
                this.mIsShortCutSwitchOn = true;
            }
            Log.i(TAG, "updateShortCutSwitch: " + this.mIsShortCutSwitchOn);
        } else {
            this.mIsShortCutSwitchOn = false;
            Log.i(TAG, "mIsAccessibilityLockScreenOn: " + this.mIsAccessibilityLockScreenOn + " mIsKeyGuardShowing " + this.mIsKeyGuardShowing);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessWaterMarkStat() {
        this.mEnabledAccessibilityServices = Settings.Secure.getStringForUser(this.mResolver, "enabled_accessibility_services", ActivityManager.getCurrentUser());
        this.mShortcutAccessibilityServices = Settings.Secure.getStringForUser(this.mResolver, "accessibility_shortcut_target_service", ActivityManager.getCurrentUser());
        boolean z = true;
        this.mIsShortcutEnabled = Settings.Secure.getIntForUser(this.mResolver, "accessibility_shortcut_enabled", 0, this.mCurUser) == 1;
        this.mIsWaterMarkEnabled = Settings.Secure.getIntForUser(this.mResolver, ACCESSIBILITY_WATERMARK_ENABLED, 1, this.mCurUser) == 1;
        if (Settings.Secure.getIntForUser(this.mResolver, "accessibility_shortcut_on_lock_screen", 0, this.mCurUser) != 1) {
            z = false;
        }
        this.mIsAccessibilityLockScreenOn = z;
        if (ISDEBUG) {
            Log.i(TAG, "enabledService:" + this.mEnabledAccessibilityServices + " shortCutService " + this.mShortcutAccessibilityServices + " mIsShortcutEnabled " + this.mIsShortcutEnabled + " mIsWaterMarkEnabled " + this.mIsWaterMarkEnabled + " mIsAccessibilityLockScreenOn " + this.mIsAccessibilityLockScreenOn);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateA11WaterMarkTextForced() {
        setWaterMarkModeText();
        setWaterMarkTextView(this.mIsShortCutSwitchOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessibilityWaterMarkText() {
        boolean isOldShortCutSwitchOn = this.mIsShortCutSwitchOn;
        updateShortCutSwitch();
        if (ISDEBUG) {
            Log.i(TAG, "newShortCut:" + this.mIsShortCutSwitchOn + " oldShortCut: " + isOldShortCutSwitchOn);
        }
        if (isOldShortCutSwitchOn != this.mIsShortCutSwitchOn) {
            setWaterMarkModeText();
            setWaterMarkTextView(this.mIsShortCutSwitchOn);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessibilityWaterMarkView() {
        boolean isOldSwitchOn = this.mIsAccessibilityServiceOn;
        if (!this.mIsWaterMarkEnabled || (!isTalkBackServicesOn() && !isScreenSpeakServicesOn())) {
            this.mIsAccessibilityServiceOn = false;
        } else {
            this.mIsAccessibilityServiceOn = true;
        }
        Log.i(TAG, "new:" + this.mIsAccessibilityServiceOn + " old: " + isOldSwitchOn);
        boolean z = this.mIsAccessibilityServiceOn;
        if (isOldSwitchOn != z) {
            if (isOldSwitchOn && !z) {
                this.mA11WatermarkHandle.sendEmptyMessage(1);
                unregisterA11WatermarkBroadcast();
            } else if (!isOldSwitchOn && this.mIsAccessibilityServiceOn) {
                registerA11WatermarkBroadcast();
                this.mA11WatermarkHandle.sendEmptyMessage(0);
            }
        }
    }

    private void registerAccessWaterMarkStat(int userId) {
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("enabled_accessibility_services"), false, this.mAccessWaterMarkObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_shortcut_target_service"), false, this.mAccessWaterMarkObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_shortcut_enabled"), false, this.mAccessWaterMarkObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(ACCESSIBILITY_WATERMARK_ENABLED), false, this.mAccessWaterMarkObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_shortcut_on_lock_screen"), false, this.mAccessWaterMarkObserver, userId);
    }

    private void registerA11WatermarkBroadcast() {
        if (ISDEBUG) {
            Log.i(TAG, "register broadcast: " + this.mIsWatermarkBroadcastRegister);
        }
        if (!this.mIsWatermarkBroadcastRegister) {
            this.mContext.registerReceiverAsUser(this.mLocaleChangeReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.LOCALE_CHANGED"), null, null);
            this.mContext.registerReceiverAsUser(this.mWatermarkPcModeChangeReceiver, UserHandle.ALL, new IntentFilter(BROADCAST_NOTIFY_DESKTOP_MODE), PERMISSION_BROADCAST_VASSIST_DESKTOP, null);
            this.mIsWatermarkBroadcastRegister = true;
        }
    }

    private void unregisterA11WatermarkBroadcast() {
        if (ISDEBUG) {
            Log.i(TAG, "unregister broadcast: " + this.mIsWatermarkBroadcastRegister);
        }
        if (this.mIsWatermarkBroadcastRegister) {
            this.mIsWatermarkBroadcastRegister = false;
            this.mContext.unregisterReceiver(this.mLocaleChangeReceiver);
            this.mContext.unregisterReceiver(this.mWatermarkPcModeChangeReceiver);
        }
    }

    public void setCurrentUser(int userId) {
        if (ISDEBUG) {
            Log.i(TAG, "setCurrentUser: " + userId);
        }
        this.mCurUser = userId;
        registerAccessWaterMarkStat(this.mCurUser);
        updateAccessWaterMarkStat();
        updateAccessibilityWaterMarkView();
        this.mA11WatermarkHandle.sendEmptyMessage(2);
    }

    public void isKeyGuardShowing(boolean isKeyGuardShowing) {
        if (ISDEBUG) {
            Log.i(TAG, "isKeyGuardShowing: " + isKeyGuardShowing);
        }
        this.mIsKeyGuardShowing = isKeyGuardShowing;
        this.mA11WatermarkHandle.sendEmptyMessage(2);
    }

    public void systemReady() {
        this.mIsAccessibilityServiceOn = this.mIsWaterMarkEnabled && (isTalkBackServicesOn() || isScreenSpeakServicesOn());
        if (ISDEBUG) {
            Log.i(TAG, "mIsAccessibilityServiceOn: " + this.mIsAccessibilityServiceOn + " mIsWaterMarkEnabled: " + this.mIsWaterMarkEnabled);
        }
        if (this.mIsAccessibilityServiceOn) {
            registerA11WatermarkBroadcast();
            this.mA11WatermarkHandle.sendEmptyMessage(0);
        }
    }

    private void setWaterMarkModeText() {
        String modeInfo;
        View view = this.mWaterMarkView;
        if (view != null) {
            TextView modeText = (TextView) view.findViewById(34603566);
            String waterMarkNote = this.mContext.getResources().getString(33685507);
            if (isTalkBackServicesOn()) {
                modeInfo = "TalkBack";
            } else if (isScreenSpeakServicesOn()) {
                modeInfo = this.mContext.getResources().getString(33685505);
            } else if (ISDEBUG) {
                Log.i(TAG, "Do not need set mode text.");
                return;
            } else {
                return;
            }
            String textInfo = String.format(waterMarkNote, modeInfo);
            modeText.setText(textInfo);
            Log.i(TAG, "ModeText: " + textInfo);
        }
    }

    private void setWaterMarkTextView(boolean isShortCutSwitchOn) {
        String noteText;
        View view = this.mWaterMarkView;
        if (view != null) {
            TextView watermarkText = (TextView) view.findViewById(34603472);
            if (isShortCutSwitchOn) {
                noteText = this.mContext.getResources().getString(33685511, 3);
            } else {
                noteText = this.mContext.getResources().getString(33685506);
            }
            watermarkText.setText(noteText);
            Log.i(TAG, "TextView: " + noteText);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareWaterMarkView() {
        WindowManager.LayoutParams layoutParams;
        if (this.mWaterMarkView != null) {
            Log.e(TAG, "duplicate inflate");
            return;
        }
        this.mWaterMarkView = View.inflate(this.mContext, 34013186, null);
        setWaterMarkModeText();
        setWaterMarkTextView(this.mIsShortCutSwitchOn);
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2100, 1280, -2);
        WindowManager.LayoutParams layoutParams2 = this.mParams;
        layoutParams2.flags = 24;
        layoutParams2.privateFlags |= 16;
        this.mParams.setTitle(WATERMARK_WINDOW_NAME);
        View view = this.mWaterMarkView;
        if (!(view == null || (layoutParams = this.mParams) == null)) {
            this.mWindowManager.addView(view, layoutParams);
        }
        Log.i(TAG, "add water mark view");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeWaterMarkView() {
        Log.i(TAG, "remove water mark view");
        View view = this.mWaterMarkView;
        if (view != null) {
            this.mWindowManager.removeViewImmediate(view);
            this.mWaterMarkView = null;
            return;
        }
        Log.e(TAG, "duplicate remove");
    }
}
