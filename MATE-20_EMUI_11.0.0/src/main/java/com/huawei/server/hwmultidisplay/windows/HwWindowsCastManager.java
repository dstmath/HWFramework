package com.huawei.server.hwmultidisplay.windows;

import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.server.pm.HwResolverManager;
import com.android.server.wm.WindowManagerInternalEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.WallpaperManagerExt;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.server.UiThreadEx;
import com.huawei.server.hwmultidisplay.HwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.power.HwMultiDisplayPowerManager;
import com.huawei.server.policy.PhoneWindowManagerEx;
import com.huawei.utils.HwPartResourceUtils;

public class HwWindowsCastManager extends DefaultHwWindowsCastManager {
    private static final String ACTION_REMINDER_VIEW_STATE_CHANGED = "com.huawei.action.hwmultidisplay.REMINDER_VIEW_STATE_CHANGED";
    private static final String ACTION_WINDOW_CAST_MODE = "com.huawei.hwmultidisplay.action.WINDOW_CAST_MODE";
    private static final Object LOCK = new Object();
    private static final int MSG_ADD_SECURE_VIEW = 1;
    private static final int MSG_REMOVE_SECURE_VIEW = 2;
    private static final int MSG_REMOVE_SECURE_VIEW_OF_KEYGUARD = 3;
    private static final String PERMISSION_RECEIVE_REMINDER_VIEW_STATE = "com.huawei.permission.hwmultidisplay.RECEIVE_REMINDER_VIEW_STATE";
    private static final String PERMISSION_RECEIVE_WINDOW_CAST_MODE = "com.huawei.hwmultidisplay.permission.WINDOW_CAST_MODE";
    public static final int REMINDER_TYPE_INVALID = -1;
    public static final int REMINDER_TYPE_KEYGUARD_LOCKED = 1;
    public static final int REMINDER_TYPE_SECURE_VIEW = 2;
    private static final long REMOVE_SECURE_VIEW_OF_KEYGUARD_DELAY = 500;
    private static final String TAG = "HwWindowsCastManager";
    private static final int TYPE_KEYGUARD_DIALOG = 2009;
    private static final int WAKE_REASON_APPLICATION = 2;
    private static volatile HwWindowsCastManager mSingleInstance = null;
    private final float DEFAULT_HEIGHT = 2340.0f;
    private final int DEFAULT_IMG_SIZE = 72;
    private final float DEFAULT_SCALE = 1.0f;
    private final int DEFAULT_TEXT_SIZE = 16;
    private Context mContext;
    private int mCurrentViewType = -1;
    private float mDensity;
    private boolean mIsNetworkReconnecting = false;
    private boolean mIsScreenLocked = false;
    private PhoneWindowManagerEx mPolicy;
    private float mScale = 1.0f;
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.hwmultidisplay.windows.HwWindowsCastManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                KeyguardManager keyguardManager = (KeyguardManager) HwWindowsCastManager.this.mContext.getSystemService("keyguard");
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -2128145023) {
                    if (hashCode != -1454123155) {
                        if (hashCode == 823795052 && action.equals("android.intent.action.USER_PRESENT")) {
                            c = 2;
                        }
                    } else if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 1;
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    c = 0;
                }
                if (c == 0) {
                    HwPCUtils.log(HwWindowsCastManager.TAG, "Receive broadcast: ACTION_SCREEN_OFF");
                    if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                        HwPCUtils.log(HwWindowsCastManager.TAG, "Keyguard is locked, need show lockview");
                        HwWindowsCastManager.this.sendShowViewMsg(1);
                    }
                } else if (c == 1) {
                    HwPCUtils.log(HwWindowsCastManager.TAG, "Receive broadcast: ACTION_SCREEN_ON");
                    if (!ActivityManagerEx.isMirrorCast("padCast") && keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                        HwPCUtils.log(HwWindowsCastManager.TAG, "Keyguard is locked, not hide lockview for the pad dis-mirror cast.");
                    } else if (!HwWindowsCastManager.this.mPolicy.isKeyguardShowingAndNotOccluded()) {
                        HwPCUtils.log(HwWindowsCastManager.TAG, "Keyguard is not showing or occluded, need hide lockview");
                        HwWindowsCastManager.this.sendHideViewMsg(1);
                    } else {
                        HwPCUtils.log(HwWindowsCastManager.TAG, "Keyguard is locked and not occluded, do nothing.");
                    }
                } else if (c == 2) {
                    HwPCUtils.log(HwWindowsCastManager.TAG, "Receive broadcast: ACTION_USER_PRESENT");
                    HwWindowsCastManager.this.sendHideViewMsg(1);
                }
            }
        }
    };
    Handler mUIHandler = new Handler(UiThreadEx.getLooper()) {
        /* class com.huawei.server.hwmultidisplay.windows.HwWindowsCastManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwWindowsCastManager.this.showReminderIfNeeded(msg.arg1);
            } else if (i == 2) {
                HwWindowsCastManager.this.hideReminderIfNeeded(msg.arg1);
            } else if (i == 3) {
                HwWindowsCastManager.this.hideReminderIfNeeded(1);
            }
        }
    };
    private View mViewForWindowCastMode = null;
    private int mWindowCastModeDisplayId = -1;
    private WindowManagerInternalEx mWindowManagerInternalEx = new WindowManagerInternalEx();

    public void setNetworkReconnectionState(boolean isNetworkReconnecting) {
        this.mIsNetworkReconnecting = isNetworkReconnecting;
    }

    public static HwWindowsCastManager getDefault() {
        if (mSingleInstance == null) {
            synchronized (LOCK) {
                if (mSingleInstance == null) {
                    mSingleInstance = new HwWindowsCastManager();
                }
            }
        }
        return mSingleInstance;
    }

    public void onDisplayAdded(Context context, int displayId) {
        HwPCUtils.log(TAG, "onDisplayAdded " + displayId);
        this.mContext = context;
        this.mWindowCastModeDisplayId = displayId;
        this.mPolicy = new PhoneWindowManagerEx();
        SystemPropertiesEx.set("hw.multi.window.cast.mode", "true");
        HwMultiDisplayUtils.setIsWindowsCastMode(true);
        HwPCUtils.setWindowsCastDisplayId(displayId);
        broadcastWindowsCastMode(true);
        this.mWindowManagerInternalEx.setFocusedDisplay(displayId, false, "enterWindowsCast");
        HwInputMethodManager.restartInputMethodForMultiDisplay();
        IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction("android.intent.action.USER_PRESENT");
        unlockFilter.addAction("android.intent.action.SCREEN_OFF");
        unlockFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mScreenStateReceiver, unlockFilter);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        if (powerManager != null && !powerManager.isInteractive()) {
            HwPCUtils.log(TAG, "wakeup if phone is in power-off state");
            PowerManagerEx.wakeUp(powerManager, SystemClock.uptimeMillis(), 2, "DisplayAdd for Windowscast");
        }
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
            sendShowViewMsg(1);
        } else if (HwPCUtils.isShopDemo()) {
            HwPCUtils.log(TAG, "shop demo do not need to set power false onDisplayAdded");
        } else {
            HwMultiDisplayPowerManager.getDefault().setScreenPowerInner(false, false);
        }
        updateWindowsCastDisplayinfo();
        HwResolverManager.getInstance().clearFirstOpenFileTypeTags();
    }

    public void onDisplayRemoved(Context context, int displayId) {
        HwPCUtils.log(TAG, "onDisplayRemoved " + displayId);
        this.mContext = context;
        this.mWindowCastModeDisplayId = -1;
        this.mViewForWindowCastMode = null;
        SystemPropertiesEx.set("hw.multi.window.cast.mode", "false");
        hideReminderIfNeeded(this.mCurrentViewType);
        this.mContext.unregisterReceiver(this.mScreenStateReceiver);
        HwMultiDisplayUtils.setIsWindowsCastMode(false);
        broadcastWindowsCastMode(false);
        HwPCUtils.setWindowsCastDisplayId(-1);
        WindowManagerExt.updateFocusWindowFreezed(true);
        HwInputMethodManager.restartInputMethodForMultiDisplay();
        if (this.mIsNetworkReconnecting) {
            HwMultiDisplayPowerManager.getDefault().setScreenPowerInner(true, false);
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            if (pm != null) {
                PowerManagerEx.userActivity(pm, SystemClock.uptimeMillis(), false);
            }
        } else {
            HwMultiDisplayPowerManager.getDefault().lockScreenWhenDisconnected(this.mContext);
            HwMultiDisplayPowerManager.getDefault().setScreenPowerOn(true);
        }
        setNetworkReconnectionState(false);
        HwResolverManager.getInstance().clearFirstOpenFileTypeTags();
    }

    public static boolean isNewPcMultiCastMode() {
        int size = SystemPropertiesEx.getInt("hw_mc.multiscreen.windowcast.value", -1);
        boolean isPcMultiCastMode = SystemPropertiesEx.getBoolean("hw.multidisplay.mode.pc", false);
        if (size > 0) {
            return isPcMultiCastMode;
        }
        return false;
    }

    public void sendShowViewMsg(int type) {
        if (type == 1) {
            this.mUIHandler.removeMessages(3);
        }
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = type;
        this.mUIHandler.sendMessage(msg);
    }

    public void sendHideViewMsg(int type) {
        sendHideViewMsg(type, false);
    }

    private void sendHideViewMsg(int type, boolean isImmediate) {
        if (type == 1) {
            this.mUIHandler.removeMessages(3);
            if (!isImmediate) {
                this.mUIHandler.sendEmptyMessageDelayed(3, REMOVE_SECURE_VIEW_OF_KEYGUARD_DELAY);
                return;
            }
        }
        Message msg = Message.obtain();
        msg.what = 2;
        msg.arg1 = type;
        this.mUIHandler.sendMessage(msg);
    }

    public void onKeyguardOccludedChangedLw(boolean occluded) {
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        boolean z = false;
        if (!(keyguardManager != null && keyguardManager.isKeyguardLocked())) {
            HwPCUtils.log(TAG, "onKeyguardOccludedChangedLw keyguard not locked, occluded:" + occluded);
            return;
        }
        boolean isIgnoreOccluded = !ActivityManagerEx.isMirrorCast("padCast");
        HwPCUtils.log(TAG, "onKeyguardOccludedChangedLw occluded:" + occluded + ", ignore:" + isIgnoreOccluded);
        if (isIgnoreOccluded || !occluded) {
            sendShowViewMsg(1);
            return;
        }
        if (occluded && !isIgnoreOccluded) {
            z = true;
        }
        sendHideViewMsg(1, z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showReminderIfNeeded(int type) {
        if (!isNewPcMultiCastMode()) {
            HwPCUtils.log(TAG, "showReminderIfNeeded, type:" + type);
            if (this.mContext != null) {
                if (type == 1) {
                    this.mIsScreenLocked = true;
                }
                Display targetDisplay = getWindowCastModeDisplay();
                if (targetDisplay != null) {
                    if (this.mViewForWindowCastMode == null) {
                        Context displayContext = this.mContext.createDisplayContext(targetDisplay);
                        this.mViewForWindowCastMode = LayoutInflater.from(displayContext).inflate(HwPartResourceUtils.getResourceId("window_cast_mode_reminder_secure"), (ViewGroup) null);
                        updateViewType(type);
                        updateViewSize();
                        WindowManager.LayoutParams params = new WindowManager.LayoutParams((int) TYPE_KEYGUARD_DIALOG);
                        params.flags = 24;
                        ((WindowManager) displayContext.getSystemService("window")).addView(this.mViewForWindowCastMode, params);
                        Drawable background = getBackgroundBlur();
                        if (background != null) {
                            this.mViewForWindowCastMode.setBackgroundDrawable(background);
                        }
                        broadcastReminderViewStateChanged(true);
                        if (type == 2 && !HwMultiDisplayUtils.getInstance().isScreenOnForHwMultiDisplay()) {
                            HwMultiDisplayUtils.getInstance().lightScreenOnForHwMultiDisplay();
                            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
                            if (pm != null) {
                                PowerManagerEx.userActivity(pm, SystemClock.uptimeMillis(), false);
                            }
                        }
                    } else {
                        updateViewType(type);
                    }
                    this.mCurrentViewType = type;
                }
            }
        }
    }

    private void updateViewType(int type) {
        View view = this.mViewForWindowCastMode;
        if (view != null) {
            ImageView reminderImg = (ImageView) view.findViewById(HwPartResourceUtils.getResourceId("reminder_img"));
            TextView reminderText = (TextView) this.mViewForWindowCastMode.findViewById(HwPartResourceUtils.getResourceId("reminder_text"));
            if (type == 1) {
                reminderImg.setImageResource(HwPartResourceUtils.getResourceId("ic_lock_window_cast_mode"));
                reminderText.setText(HwPartResourceUtils.getResourceId("window_cast_mode_reminder_locked"));
                return;
            }
            reminderImg.setImageResource(HwPartResourceUtils.getResourceId("ic_safe_window_cast_mode"));
            reminderText.setText(HwPartResourceUtils.getResourceId("window_cast_mode_reminder_secure_string"));
        }
    }

    private void updateViewSize() {
        if (this.mScale != 1.0f) {
            ImageView reminderImg = (ImageView) this.mViewForWindowCastMode.findViewById(HwPartResourceUtils.getResourceId("reminder_img"));
            ViewGroup.LayoutParams imgViewParams = reminderImg.getLayoutParams();
            float f = this.mScale;
            float f2 = this.mDensity;
            imgViewParams.height = (int) ((f * 72.0f * f2) + 0.5f);
            imgViewParams.width = (int) ((f * 72.0f * f2) + 0.5f);
            reminderImg.setLayoutParams(imgViewParams);
            ((TextView) this.mViewForWindowCastMode.findViewById(HwPartResourceUtils.getResourceId("reminder_text"))).setTextSize(this.mScale * 16.0f);
        }
    }

    private void updateWindowsCastDisplayinfo() {
        Display targetDisplay = getWindowCastModeDisplay();
        if (targetDisplay != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            targetDisplay.getMetrics(displayMetrics);
            HwPCUtils.log(TAG, "Display info:" + displayMetrics);
            float f = 1.0f;
            if (displayMetrics.heightPixels > displayMetrics.widthPixels) {
                if (((float) displayMetrics.heightPixels) < 2340.0f) {
                    f = ((float) displayMetrics.heightPixels) / 2340.0f;
                }
                this.mScale = f;
            } else {
                if (((float) displayMetrics.widthPixels) < 2340.0f) {
                    f = ((float) displayMetrics.widthPixels) / 2340.0f;
                }
                this.mScale = f;
            }
            this.mDensity = displayMetrics.density;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideReminderIfNeeded(int type) {
        if (!isNewPcMultiCastMode()) {
            HwPCUtils.log(TAG, "hideReminderIfNeeded, type:" + type);
            if (type == 1) {
                this.mIsScreenLocked = false;
            }
            if (this.mContext != null && this.mCurrentViewType == type) {
                if (this.mIsScreenLocked) {
                    showReminderIfNeeded(1);
                    return;
                }
                Display targetDisplay = getWindowCastModeDisplay();
                if (targetDisplay != null) {
                    if (this.mViewForWindowCastMode != null) {
                        ((WindowManager) this.mContext.createDisplayContext(targetDisplay).getSystemService("window")).removeView(this.mViewForWindowCastMode);
                        this.mViewForWindowCastMode = null;
                        broadcastReminderViewStateChanged(false);
                    }
                    this.mCurrentViewType = -1;
                }
            }
        }
    }

    private Drawable getBackgroundBlur() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mContext);
        Display display = getWindowCastModeDisplay();
        DisplayInfoEx displayInfo = new DisplayInfoEx();
        if (display == null || !DisplayEx.getDisplayInfo(display, displayInfo)) {
            return null;
        }
        return new BitmapDrawable(this.mContext.getResources(), WallpaperManagerExt.getBlurBitmap(wallpaperManager, new Rect(0, 0, displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight())));
    }

    private Display getWindowCastModeDisplay() {
        DisplayManager dm;
        int i;
        Context context = this.mContext;
        if (context == null || (dm = (DisplayManager) context.getSystemService("display")) == null || (i = this.mWindowCastModeDisplayId) == -1) {
            return null;
        }
        return dm.getDisplay(i);
    }

    private void broadcastWindowsCastMode(boolean isInWindowsCastMode) {
        HwPCUtils.log(TAG, "Broadcast WindowsCast mode:" + isInWindowsCastMode);
        Intent intent = new Intent(ACTION_WINDOW_CAST_MODE);
        intent.putExtra("mode", isInWindowsCastMode);
        this.mContext.sendBroadcast(intent, PERMISSION_RECEIVE_WINDOW_CAST_MODE);
    }

    private void broadcastReminderViewStateChanged(boolean isShown) {
        HwPCUtils.log(TAG, "reminder view state changed:" + isShown);
        Intent intent = new Intent(ACTION_REMINDER_VIEW_STATE_CHANGED);
        intent.putExtra("state", isShown ? 1 : 0);
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.CURRENT, PERMISSION_RECEIVE_REMINDER_VIEW_STATE);
    }
}
