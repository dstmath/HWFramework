package com.android.server.pc;

import android.app.ActivityManager;
import android.app.ActivityManager.TaskThumbnail;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.app.AlertDialog;
import android.app.HwRecentTaskInfo;
import android.app.ITaskStackListener;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UserSwitchObserver;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.pc.IHwPCManager.Stub;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.HwPCUtils;
import android.util.HwPCUtils.ProjectionMode;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManagerInternal;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.hwsystemui.IHwSystemUIController;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwPCMultiWindowThumbnailPersister;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.input.HwInputManagerService.HwInputManagerLocalService;
import com.android.server.pc.whiltestrategy.WhiteListAppStrategyManager;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.IDisplayEngineService;
import com.leisen.wallet.sdk.http.AsyncHttpClient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCManagerService extends Stub {
    private static final String ACTION_ALARM_WAKEUP = "com.android.deskclock.ALARM_ALERT";
    private static final String ACTION_NOTIFY_CHANGE_STATUS_BAR = "com.android.server.pc.action.CHANGE_STATUS_BAR";
    private static final String ACTION_NOTIFY_SHOW_MK = "com.android.server.pc.action.SHOW_MK";
    private static final String ACTION_NOTIFY_SWITCH_MODE = "com.android.server.pc.action.SWITCH_MODE";
    private static final String ACTION_NOTIFY_UNINSTALL_APP = "com.android.server.pc.action.UNINSTALL_APP";
    private static final int BIT_KEYBOARD = 2;
    private static final int BIT_MOUSE = 1;
    private static final int BIT_NONE = 0;
    private static final int CHECK_HARD_BROAD_DELAY = 2000;
    private static final int DELAY_SWITCHPROJ_TO_DISPALYADDED = 1000;
    private static final int DREAMS_DISENABLED = 0;
    private static final int DREAMS_ENABLED = 1;
    private static final int DREAMS_INVALID = -1;
    private static final int EXPLORER_LAUNCH_DELAY = 4000;
    private static final int FINGERPRINT_SLIDE_OFF = 0;
    private static final int FINGERPRINT_SLIDE_ON = 1;
    private static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    private static final int INVALID_ARG = -1;
    private static final int KEEP_RECORD_TIMEOUT = 180000;
    private static final int[] KEYBOARD_PRODUCT_ID = new int[]{4817};
    private static final int[] KEYBOARD_VENDOR_ID = new int[]{1455};
    private static final String KEY_BEFORE_BOOT_ANIM_TIME = "before_boot_anim_time";
    private static final String KIDS_MODE_PACKAGES_NAME = "com.huawei.kidsmode";
    private static final String KIDS_MODE_RUNNING_SETTINGS = "hwkidsmode_running";
    private static final String MMI_TEST_PROPERTY = "runtime.mmitest.isrunning";
    private static final int MSG_DISPLAY_ADDED = 6;
    private static final int MSG_DISPLAY_CHANGED = 7;
    private static final int MSG_DISPLAY_REMOVED = 8;
    private static final int MSG_KEEP_RECORD_TIMEOUT = 11;
    private static final int MSG_LAUNCH_GUIDE = 3;
    private static final int MSG_LAUNCH_MK = 13;
    private static final int MSG_NOTIFY_SWITCH_PROJ = 1;
    private static final int MSG_NOTIFY_VITUAL_M_K = 2;
    private static final int MSG_REFRESH_NTFS = 5;
    private static final int MSG_REGISTER_ALARM = 15;
    private static final int MSG_RELAUNCH_IME = 14;
    private static final int MSG_RESTORE_APP = 10;
    private static final int MSG_SET_FOCUS_DISPLAY = 17;
    private static final int MSG_SET_PROJ_MODE = 4;
    private static final int MSG_START_RESTORE_APPS = 9;
    private static final int MSG_SWITCH_USER = 12;
    private static final int MSG_UNINSTALL_APP = 16;
    private static final int MSG_UPDATE_CFG = 18;
    private static final int NOTIFY_SWITCH_PROJ_ID = 0;
    private static final int NOTIFY_VIRTUAL_M_K_ID = 1;
    private static final String PERMISSION_BROADCAST_CHANGE_STATUS_BAR = "com.huawei.permission.pc.CHANGE_STATUS_BAR";
    private static final String PERMISSION_BROADCAST_SWITCH_MODE = "com.huawei.permission.SWITCH_MODE";
    private static final String PERMISSION_PC_MANAGER_API = "com.huawei.permission.PC_MANAGER_API";
    private static final int RELAUNCH_IME_DELAY = 2000;
    private static final int RESO_1080P = 1920;
    private static final int RE_BIND_SERVICE_DELAY = 800;
    private static final int ROTATION_SWITCH_CLOSE = 0;
    private static final int ROTATION_SWITCH_OPEN = 1;
    private static final String SCREEN_POWER_DEVICE = "/sys/devices/virtual/dp/power/lcd_power";
    private static final String SCREEN_POWER_OFF = "0";
    private static final String SCREEN_POWER_ON = "1";
    private static final int START_ACTIVITY_INTERVAL = 800;
    private static final int START_RESTORE_APPS_DELAY_TIME = 3000;
    private static final String TAG = "HwPCManagerService";
    private static final int TIME_DISPALY_ADD_BEFORE_BOOT_ANIM = 4500;
    private static final int TIME_SEND_SWITCHPROJ_MSG_DELAYED = 200;
    private static final int TIME_SWITCH_MODE_BEFORE_BOOT_ANIM = 1500;
    private static final int TIME_UNLOCK_ACTION_BEFORE_BOOT_ANIM = 1500;
    private final String DEVICE_PROVISIONED_URI = "content://settings/global/device_provisioned";
    private final String SCREEN_OF_TIMEOUT_URI = "content://settings/system/screen_off_timeout";
    private boolean isNeedEixtDesktop = false;
    private boolean isNeedEnterDesktop = false;
    private HwActivityManagerService mAMS;
    private final BroadcastReceiver mAlarmClockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwPCUtils.log(HwPCManagerService.TAG, "receive clock alarm");
            HwPCManagerService.this.setScreenPower(true);
        }
    };
    private final OnAlarmListener mAlarmListener = new OnAlarmListener() {
        public void onAlarm() {
            HwPCManagerService.this.mHandler.sendEmptyMessage(11);
        }
    };
    private AlarmManager mAlarmManager;
    private float mAxisX = 0.0f;
    private float mAxisY = 0.0f;
    private Builder mBuilderForInput;
    private Builder mBuilderForSwitch;
    private Toast mCallingToast;
    private final ServiceConnection mConnExplorer = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceDisconnected");
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        HwPCManagerService.this.mContext.bindService(new Intent().setComponent(HwPCManagerService.this.mExplorerComponent), HwPCManagerService.this.mConnExplorer, 1);
                    }
                }
            }, 800);
        }
    };
    private final ServiceConnection mConnSysUI = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceConnected");
            if (HwPCManagerService.this.mDisplayId != -1) {
                HwPCManagerService.this.updateDisplayOverrideConfiguration(HwPCManagerService.this.mDisplayId, 2000);
                HwPCManagerService.this.relaunchIMEDelay(2000);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceDisconnected");
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        HwPCManagerService.this.mContext.bindService(new Intent().setComponent(HwPCManagerService.this.mSystemUIComponent), HwPCManagerService.this.mConnSysUI, 1);
                    }
                }
            }, 800);
        }
    };
    private int mConnectedInputDevices = 0;
    private Context mContext;
    private IHwSystemUIController mController;
    private final DisplayDriverCommunicator mDDC;
    private int mDisplayId = -1;
    private DisplayManager mDisplayManager;
    private int mDreamsEnabledSetting = -1;
    private AlertDialog mEnterDesktopAlertDialog = null;
    private AlertDialog mExitDesktopAlertDialog = null;
    private final ComponentName mExplorerComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.services.ExplorerService");
    final LocalHandler mHandler;
    final ServiceThread mHandlerThread;
    private volatile boolean mHasKMNotf = false;
    private volatile boolean mHasSwitchNtf = false;
    private IBinder mIBinderAudioService;
    private int mIMEWithHardKeyboardState = 1;
    private final InputDeviceListener mInputDeviceListener = new InputDeviceListener() {
        public void onInputDeviceAdded(int deviceId) {
            HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceAdded, deviceId:" + deviceId + ", mConnectedInputDevices: " + HwPCManagerService.this.mConnectedInputDevices);
            InputDevice device = InputDevice.getDevice(deviceId);
            HwPCManagerService hwPCManagerService = HwPCManagerService.this;
            hwPCManagerService.mConnectedInputDevices = hwPCManagerService.mConnectedInputDevices | HwPCManagerService.whichInputDevice(device);
            if ((HwPCManagerService.whichInputDevice(device) & 1) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT, "");
            }
            if ((HwPCManagerService.whichInputDevice(device) & 2) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, IDisplayEngineService.DE_ACTION_PG_INPUT_START, "");
            }
            if (HwPCUtils.enabledInPad()) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("START_TIME", System.currentTimeMillis());
                    if (HwPCManagerService.this.isExclusiveKeyboard(device)) {
                        if (!(HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) || (HwPCManagerService.this.mIsExclusiveKeyboardOnline ^ 1) == 0)) {
                            HwPCManagerService.this.showEnterDesktopAlertDialog(HwPCManagerService.this.getCurrentContext(), true, true);
                        }
                        jo.put("EXCLUSIVE", true);
                    } else {
                        jo.put("EXCLUSIVE", false);
                    }
                } catch (JSONException e) {
                    HwPCUtils.log(HwPCManagerService.TAG, "JSONException");
                }
                HwPCManagerService.this.mKeyboardInfo.put(Integer.valueOf(deviceId), jo);
            }
        }

        public void onInputDeviceRemoved(int deviceId) {
            HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceRemoved, deviceId:" + deviceId + ", mConnectedInputDevices: " + HwPCManagerService.this.mConnectedInputDevices);
            int connectedInputDevices = 0;
            for (int device : InputDevice.getDeviceIds()) {
                connectedInputDevices |= HwPCManagerService.whichInputDevice(InputDevice.getDevice(device));
            }
            HwPCManagerService.this.mConnectedInputDevices = connectedInputDevices;
            if (HwPCUtils.enabledInPad()) {
                HwPCManagerService.this.mIsExclusiveKeyboardOnline = DisplayDriverCommunicator.isExclusiveKeyboardOnline();
                try {
                    if (HwPCManagerService.this.mKeyboardInfo.containsKey(Integer.valueOf(deviceId))) {
                        JSONObject jo = (JSONObject) HwPCManagerService.this.mKeyboardInfo.get(Integer.valueOf(deviceId));
                        if (HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) && jo.getBoolean("EXCLUSIVE") && (HwPCManagerService.this.mIsExclusiveKeyboardOnline ^ 1) != 0) {
                            HwPCManagerService.this.showExitDesktopAlertDialog(HwPCManagerService.this.getCurrentContext(), true);
                        }
                        jo.put("END_TIME", System.currentTimeMillis());
                        HwPCUtils.log(HwPCManagerService.TAG, "report msg:" + jo.toString());
                        HwPCManagerService.this.mKeyboardInfo.remove(Integer.valueOf(deviceId));
                        HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10027, jo.toString());
                    }
                } catch (JSONException e) {
                    HwPCUtils.log(HwPCManagerService.TAG, "JSONException");
                }
            }
        }

        public void onInputDeviceChanged(int deviceId) {
        }
    };
    ArrayList<Intent> mIntentList = new ArrayList();
    private boolean mIsDisplayAddedAfterSwitch = false;
    private boolean mIsDisplayLargerThan1080p = false;
    private boolean mIsExclusiveKeyboardOnline = false;
    private ConcurrentHashMap<Integer, JSONObject> mKeyboardInfo = new ConcurrentHashMap();
    private KeyguardManager mKeyguardManager;
    private int mLockScreenTimeout = 0;
    private NotificationManager mNm;
    private final BroadcastReceiver mNotifyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "mNotifyReceiver received a null intent");
                return;
            }
            String action = intent.getAction();
            if (HwPCManagerService.ACTION_NOTIFY_SWITCH_MODE.equals(action)) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT, "");
                HwPCManagerService.this.switchProjMode();
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                HwPCManagerService.this.refreshNotifications();
            } else if (HwPCManagerService.ACTION_NOTIFY_SHOW_MK.equals(action)) {
                HwPCManagerService.this.sendShowMkMessage();
            } else if (HwPCManagerService.ACTION_NOTIFY_UNINSTALL_APP.equals(action)) {
                String pkgName = intent.getStringExtra("PACKAGE_NAME");
                if (pkgName != null) {
                    HwPCUtils.log(HwPCManagerService.TAG, "ACTION_NOTIFY_UNINSTALL_APP onReceive: " + pkgName);
                    HwPCManagerService.this.sendUninstallAppMessage(pkgName);
                }
            }
        }
    };
    private int mPCBeforeBootAnimTime = 2000;
    PCSettingsObserver mPCSettingsObserver;
    private int mPadDesktopModeLockScreenTimeout = 0;
    private int mPadLockScreenTimeout = 0;
    private boolean mPadPCDisplayIsRemoved = false;
    private int mPhoneState = 0;
    PointerEventListener mPointerListener = new PointerEventListener() {
        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent != null) {
                HwPCManagerService.this.mAxisX = motionEvent.getX();
                HwPCManagerService.this.mAxisY = motionEvent.getY();
            }
        }
    };
    private volatile ProjectionMode mProjMode = ProjectionMode.DESKTOP_MODE;
    boolean mProvisioned = false;
    boolean mRestartAppsWhenUnlocked = false;
    private int mRotationSwitch = 1;
    private int mRotationValue = 0;
    private Object mScreenAccessLock = new Object();
    private boolean mScreenPowerOn = true;
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String act = intent.getAction();
                if (act != null && "android.intent.action.ACTION_SHUTDOWN".equals(act) && HwPCUtils.isPcCastModeInServer()) {
                    HwPCManagerService.this.restoreRotationInPad();
                    HwPCManagerService.this.restoreDreamSettingInPad();
                }
            }
        }
    };
    private boolean mSupportOverlay = SystemProperties.getBoolean("hw_pc_support_overlay", false);
    private boolean mSupportTouchPad = SystemProperties.getBoolean("hw_pc_support_touchpad", true);
    private final ComponentName mSystemUIComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.SystemUIService");
    private TelephonyManager mTelephonyPhone;
    private HwPCMultiWindowThumbnailPersister mThumbnailPersister;
    int mTmpDisplayId2Unlocked;
    private final ComponentName mTouchPadComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.mk.activity.ImitateActivity");
    Handler mUIHandler = new Handler(UiThread.get().getLooper());
    private UnlockScreenReceiver mUnlockScreenReceiver;
    private int mUserId = 0;
    UserManagerInternal mUserManagerInternal;
    private WindowManagerInternal mWindowManagerInternal;
    private boolean restartByUnlock2SetAnimTime;

    private class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwPCManagerService.this.sendNotificationForSwitch(msg.obj);
                    break;
                case 2:
                    HwPCManagerService.this.showMKNotify();
                    break;
                case 3:
                    HwPCManagerService.this.launchGuide();
                    break;
                case 4:
                    if (!HwPCUtils.enabledInPad() || !HwPCManagerService.this.isMonkeyRunning()) {
                        if (HwPCManagerService.this.mDisplayId != -1) {
                            HwPCManagerService.this.mIsDisplayAddedAfterSwitch = false;
                            if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                                HwPCUtils.setPCDisplayID(HwPCManagerService.this.mDisplayId);
                                HwPCManagerService.this.mProjMode = ProjectionMode.DESKTOP_MODE;
                                HwPCManagerService.this.mDDC.enableProjectionMode();
                                if (!HwPCManagerService.this.mIsDisplayLargerThan1080p) {
                                    HwPCManagerService.this.handleSwitchToDesktopMode();
                                    break;
                                } else {
                                    postDelayed(new Runnable() {
                                        public void run() {
                                            HwPCManagerService.this.handleSwitchToDesktopMode();
                                        }
                                    }, 1000);
                                    break;
                                }
                            }
                            HwPCManagerService.this.lightPhoneScreen();
                            HwPCUtils.log(HwPCManagerService.TAG, "The current mode is DesktopMode.");
                            HwPCUtils.setPhoneDisplayID(HwPCManagerService.this.mDisplayId);
                            HwPCManagerService.this.mProjMode = ProjectionMode.PHONE_MODE;
                            HwPCUtils.setPcCastModeInServerEarly(HwPCManagerService.this.mProjMode);
                            HwPCManagerService.this.mDDC.resetProjectionMode();
                            if (!HwPCUtils.enabledInPad()) {
                                HwPCManagerService.this.bindUnbindService(false);
                            }
                            HwPCManagerService.this.mAMS.togglePCMode(HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode), HwPCManagerService.this.mDisplayId);
                            HwPCManagerService.this.setUsePCModeMouseIconContext(false);
                            HwPCUtils.setPcCastModeInServer(false);
                            HwPCManagerService.this.updateIMEWithHardKeyboardState(false);
                            Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "selected-proj-mode", 0);
                            HwPCManagerService.this.restoreDreamSettingInPad();
                            HwPCManagerService.this.sendNotificationForSwitch(HwPCManagerService.this.mProjMode);
                            HwPCManagerService.this.removeMKNotify();
                            HwPCManagerService.this.bdReportDiffSrcStatus(false);
                            HwPCManagerService.this.bdReportSameSrcStatus(true);
                            HwPCManagerService.this.sendSwitchToStatusBar();
                            HwPCManagerService.this.setDesktopModeToAudioService(0);
                            HwPCManagerService.this.mAMS.restoreRotationInPcMode();
                            HwPCManagerService.this.updateFingerprintSlideSwitch();
                            HwPCManagerService.this.relaunchIMEDelay(0);
                            if (HwPCUtils.enabledInPad()) {
                                HwPCManagerService.this.bindUnbindService(false);
                                DisplayManagerInternal dm = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
                                if (dm != null) {
                                    dm.pcDisplayChange(false);
                                    HwPCManagerService.this.mPadPCDisplayIsRemoved = true;
                                    dm.pcDisplayChange(true);
                                    break;
                                }
                            }
                        }
                        return;
                    }
                    HwPCUtils.log(HwPCManagerService.TAG, "MSG_SET_PROJ_MODE isMonkeyRunning return !");
                    return;
                    break;
                case 5:
                    HwPCManagerService.this.refreshMKNotify();
                    if (HwPCManagerService.this.mHasSwitchNtf) {
                        HwPCManagerService.this.sendNotificationForSwitch(HwPCManagerService.this.mProjMode);
                        break;
                    }
                    break;
                case 6:
                    if (HwPCManagerService.this.mKeyguardManager == null) {
                        HwPCManagerService.this.mKeyguardManager = (KeyguardManager) HwPCManagerService.this.mContext.getSystemService("keyguard");
                    }
                    if (!HwPCManagerService.this.mKeyguardManager.isKeyguardLocked()) {
                        HwPCManagerService.this.onDisplayAdded(msg.arg1);
                        break;
                    }
                    HwPCManagerService.this.mTmpDisplayId2Unlocked = msg.arg1;
                    HwPCManagerService.this.mRestartAppsWhenUnlocked = true;
                    break;
                case 7:
                    HwPCManagerService.this.onDisplayChanged(msg.arg1);
                    break;
                case 8:
                    HwPCManagerService.this.onDisplayRemoved(msg.arg1);
                    break;
                case 9:
                    HwPCManagerService.this.handleRestoreApps(msg.arg1);
                    break;
                case 10:
                    if (!HwPCManagerService.this.getCastMode()) {
                        removeMessages(10);
                        break;
                    } else {
                        HwPCManagerService.this.restoreApp(msg.arg1, (Intent) msg.obj);
                        break;
                    }
                case 11:
                    HwPCManagerService.this.mIntentList.clear();
                    break;
                case 12:
                    HwPCManagerService.this.onSwitchUser(msg.arg1);
                    break;
                case 13:
                    HwPCManagerService.this.launchMK();
                    break;
                case 14:
                    HwPCManagerService.this.doRelaunchIMEIfNecessary();
                    break;
                case 15:
                    HwPCManagerService.this.mIntentList.clear();
                    HwPCManagerService.this.mIntentList.addAll((List) msg.obj);
                    HwPCManagerService.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 180000, "keep_record", HwPCManagerService.this.mAlarmListener, HwPCManagerService.this.mHandler);
                    break;
                case 16:
                    String pkgName = msg.obj;
                    HwPCUtils.log(HwPCManagerService.TAG, "handleMessage MSG_UNINSTALL_APP:" + pkgName);
                    if (pkgName != null) {
                        HwPCManagerService.this.mContext.getPackageManager().deletePackage(pkgName, (IPackageDeleteObserver) null, 2);
                        break;
                    }
                    break;
                case 18:
                    HwPCManagerService.this.doUpdateDisplayOverrideConfiguration(msg.arg1);
                    break;
            }
        }
    }

    class PCSettingsObserver extends ContentObserver {
        PCSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            if (HwPCUtils.enabledInPad()) {
                resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this, 0);
                updateScreenOffTimeoutSettings();
            }
            resolver.registerContentObserver(Global.getUriFor("device_provisioned"), false, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                HwPCUtils.log(HwPCManagerService.TAG, "PCSettingsObserver onChange:" + selfChange + " uri:" + uri.toString());
                String uri2 = uri.toString();
                if (uri2.equals("content://settings/system/screen_off_timeout")) {
                    updateScreenOffTimeoutSettings();
                } else if (uri2.equals("content://settings/global/device_provisioned")) {
                    deviceChanged();
                }
            }
        }

        private synchronized void updateScreenOffTimeoutSettings() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            HwPCManagerService.this.mLockScreenTimeout = System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            if (HwPCUtils.enabledInPad()) {
                if (HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) && HwPCUtils.isPcCastModeInServer()) {
                    HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                    Secure.putInt(resolver, "pad_desktop_mode_screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                    HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_DESKTOP_MODE_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                } else {
                    HwPCManagerService.this.mPadLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                    Secure.putInt(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout);
                    HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadLockScreenTimeout);
                }
            }
            HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings " + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
        }

        synchronized void readScreenOffSettings() {
            if (HwPCUtils.enabledInPad()) {
                ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                HwPCManagerService.this.mLockScreenTimeout = System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
                HwPCManagerService.this.mPadLockScreenTimeout = Secure.getIntForUser(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mLockScreenTimeout, -2);
                HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = Secure.getIntForUser(resolver, "pad_desktop_mode_screen_off_timeout", MemoryConstant.REPEAT_RECLAIM_TIME_GAP, -2);
                HwPCUtils.log(HwPCManagerService.TAG, "read screen off settings current=" + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
            }
        }

        synchronized void restoreScreenOffSettings() {
            if (HwPCUtils.enabledInPad()) {
                ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                if (HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) && HwPCUtils.isPcCastModeInServer()) {
                    System.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadDesktopModeLockScreenTimeout=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                } else {
                    System.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadLockScreenTimeout=" + HwPCManagerService.this.mPadLockScreenTimeout);
                }
            }
        }

        private void deviceChanged() {
            boolean wasProvisioned = HwPCManagerService.this.mProvisioned;
            boolean isProvisioned = HwPCManagerService.this.deviceIsProvisioned();
            HwPCManagerService.this.mProvisioned = isProvisioned;
            if (isProvisioned && (wasProvisioned ^ 1) != 0) {
                int displayId = -1;
                HwPCManagerService.this.mDisplayManager = (DisplayManager) HwPCManagerService.this.mContext.getSystemService("display");
                if (HwPCManagerService.this.mDisplayManager != null) {
                    Display[] displays = HwPCManagerService.this.mDisplayManager.getDisplays();
                    if (displays != null && displays.length > 0) {
                        int i = displays.length - 1;
                        while (i >= 0) {
                            if (displays[i] != null && displays[i].getDisplayId() != 0 && HwPCManagerService.this.isWiredDisplay(displays[i].getDisplayId())) {
                                displayId = displays[i].getDisplayId();
                                break;
                            }
                            i--;
                        }
                    }
                    if (displayId != -1) {
                        HwPCManagerService.this.scheduleDisplayAdded(displayId);
                    }
                }
            }
        }
    }

    private class UnlockScreenReceiver extends BroadcastReceiver {
        /* synthetic */ UnlockScreenReceiver(HwPCManagerService this$0, UnlockScreenReceiver -this1) {
            this();
        }

        private UnlockScreenReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "mUnlockScreenReceiver received a null intent");
                return;
            }
            if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                HwPCUtils.log(HwPCManagerService.TAG, "receive: ACTION_USER_PRESENT, mRestartAppsWhenUnlocked is " + HwPCManagerService.this.mRestartAppsWhenUnlocked);
                if (HwPCManagerService.this.mRestartAppsWhenUnlocked) {
                    HwPCManagerService.this.mRestartAppsWhenUnlocked = false;
                    HwPCManagerService.this.restartByUnlock2SetAnimTime = true;
                    HwPCManagerService.this.scheduleDisplayAdded(HwPCManagerService.this.mTmpDisplayId2Unlocked);
                } else {
                    HwPCManagerService.this.setFocusedPCDisplayId("unlockScreen");
                }
            }
        }
    }

    static /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_22898(CompoundButton buttonView, boolean isChecked) {
    }

    static /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_29699(CompoundButton buttonView, boolean isChecked) {
    }

    private static boolean isDesktopMode(ProjectionMode mode) {
        return mode == ProjectionMode.DESKTOP_MODE;
    }

    private Context getCurrentContext() {
        if (!isDesktopMode(this.mProjMode)) {
            return this.mContext;
        }
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        if (this.mDisplayManager != null) {
            Display display = null;
            for (Display dis : this.mDisplayManager.getDisplays()) {
                if (dis.getType() != 1 && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                    display = dis;
                    break;
                }
            }
            if (display != null) {
                return this.mContext.createDisplayContext(display);
            }
        }
        return null;
    }

    private boolean isEnterDesktopModeRemembered() {
        int isRemembered = Secure.getInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 0);
        Log.d(TAG, "isEnterDesktopModeRemembered" + isRemembered);
        if (isRemembered == 1) {
            return true;
        }
        return false;
    }

    private boolean isExitDesktopModeRemembered() {
        int isRemembered = Secure.getInt(this.mContext.getContentResolver(), "exit-desktop-mode-remember", 0);
        Log.d(TAG, "isEnterDesktopModeRemembered" + isRemembered);
        if (isRemembered == 1) {
            return true;
        }
        return false;
    }

    private boolean isShowEnterDialog() {
        int isRemembered = Secure.getInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 0);
        Log.d(TAG, "isShowEnterDialog" + isRemembered);
        if (isRemembered == 0) {
            return true;
        }
        return false;
    }

    private boolean isShowExitDialog() {
        int isRemembered = Secure.getInt(this.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 0);
        Log.d(TAG, "isShowExitDialog" + isRemembered);
        if (isRemembered == 0) {
            return true;
        }
        return false;
    }

    private SpannableString getSpanString(String orig, String re, String url) {
        Log.d(TAG, String.format("getSpanString: orig=%s re=%s url %s", new Object[]{orig, re, url}));
        SpannableString spannableString = new SpannableString(orig);
        int start = orig.indexOf(re);
        if (start < 0) {
            return spannableString;
        }
        spannableString.setSpan(new ClickableSpan() {
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }

            public void onClick(View widget) {
                Log.d(HwPCManagerService.TAG, "SpannableString onClick.");
            }
        }, start, start + re.length(), 33);
        return spannableString;
    }

    private boolean isMMIRunning() {
        return SystemProperties.get(MMI_TEST_PROPERTY, StorageUtils.SDCARD_RWMOUNTED_STATE).equals(StorageUtils.SDCARD_ROMOUNTED_STATE);
    }

    private void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        showEnterDesktopAlertDialog(context, isExclusiveKeyboard, false);
    }

    private boolean isKeyguardLocked() {
        if (this.mKeyguardManager != null) {
            return this.mKeyguardManager.isKeyguardLocked();
        }
        return false;
    }

    private boolean isKidsModeRunning() {
        boolean z = true;
        if (this.mContext == null) {
            return false;
        }
        try {
            this.mContext.getPackageManager().getApplicationInfo(KIDS_MODE_PACKAGES_NAME, 8192);
            if (Global.getInt(this.mContext.getContentResolver(), "hwkidsmode_running", 0) != 1) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            HwPCUtils.log(TAG, "kidsmode not found!");
            return false;
        }
    }

    private void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard, boolean notDisplayAdd) {
        if (isMMIRunning() || isKeyguardLocked() || isKidsModeRunning()) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed!");
        } else if (!deviceIsProvisioned() || (isUserSetupComplete() ^ 1) != 0) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed!  Startup Guide is not Complete");
        } else if (isCalling()) {
            showCallingToast(isDesktopMode(this.mProjMode) ? this.mDisplayId : 0);
            HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
        } else if (this.mUserId != 0) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed! currentUser is not UserHandle.USER_OWNER");
        } else if (context != null && (this.mEnterDesktopAlertDialog == null || !this.mEnterDesktopAlertDialog.isShowing())) {
            if (isEnterDesktopModeRemembered() && (isDesktopMode(this.mProjMode) ^ 1) != 0 && (isExclusiveKeyboard ^ 1) != 0) {
                backToHomeInDefaultDisplay(this.mDisplayId);
                sendSwitchMsgDelayed(200);
            } else if (!isExclusiveKeyboard || (isShowEnterDialog() ^ 1) == 0) {
                AlertDialog.Builder buider = new AlertDialog.Builder(context, 33947691);
                View view = LayoutInflater.from(buider.getContext()).inflate(34013290, null);
                if (view != null) {
                    ImageView imageView = (ImageView) view.findViewById(34603068);
                    TextView textView = (TextView) view.findViewById(34603067);
                    CheckBox checkBox = (CheckBox) view.findViewById(34603069);
                    if (imageView != null && textView != null && checkBox != null) {
                        String content;
                        if (isExclusiveKeyboard) {
                            imageView.setPadding(0, 0, 0, 0);
                            content = this.mContext.getResources().getString(33685535);
                        } else {
                            imageView.setImageResource(33751791);
                            content = this.mContext.getResources().getString(33685533);
                        }
                        textView.setText(getSpanString(content, this.mContext.getResources().getString(33685690), ""));
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        checkBox.setOnCheckedChangeListener(new -$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ());
                        this.mEnterDesktopAlertDialog = buider.setTitle(33685778).setPositiveButton(33685760, new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass6(isExclusiveKeyboard, this, checkBox)).setNegativeButton(33685532, new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass2(this)).setView(view).setOnDismissListener(new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass4(this)).create();
                        this.mEnterDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mEnterDesktopAlertDialog.getWindow().setType(2008);
                        this.mEnterDesktopAlertDialog.setOnShowListener(new OnShowListener() {
                            public void onShow(DialogInterface dialog) {
                                Button btn = HwPCManagerService.this.mEnterDesktopAlertDialog.getButton(-1);
                                btn.setFocusable(true);
                                btn.setFocusedByDefault(true);
                                btn.setFocusableInTouchMode(true);
                                btn.requestFocus();
                            }
                        });
                        this.mEnterDesktopAlertDialog.show();
                        this.mEnterDesktopAlertDialog.getWindow().getAttributes().setTitle("EnterDesktopAlertDialog");
                        Window w = this.mEnterDesktopAlertDialog.getWindow();
                        ContentResolver contentResolver = context.getContentResolver();
                        if (!(w == null || contentResolver == null)) {
                            View titleView = w.findViewById(16908708);
                            boolean isTalkbackOpen = Secure.getInt(contentResolver, "accessibility_enabled", 0) > 0;
                            if (titleView != null && (titleView.isAccessibilityFocused() ^ 1) != 0 && isExclusiveKeyboard && notDisplayAdd && isTalkbackOpen) {
                                titleView.requestAccessibilityFocus();
                            }
                        }
                    }
                }
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_23174(CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        backToHomeInDefaultDisplay(this.mDisplayId);
        this.isNeedEnterDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                Secure.putInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 1);
            } else {
                Secure.putInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_24532(DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:cancel which=" + which);
        this.isNeedEnterDesktop = false;
        dialog.cancel();
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_24823(DialogInterface dialog) {
        if (this.isNeedEnterDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            this.isNeedEnterDesktop = false;
            sendSwitchMsgDelayed(200);
        }
    }

    private void sendSwitchMsgDelayed(int delayMillis) {
        this.mHandler.removeMessages(4);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), (long) delayMillis);
    }

    private void showExitDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        int i = 0;
        if (isMMIRunning() || isKeyguardLocked()) {
            HwPCUtils.log(TAG, "showExitDesktopAlertDialog failed!");
        } else if (isCalling()) {
            if (isDesktopMode(this.mProjMode)) {
                i = this.mDisplayId;
            }
            showCallingToast(i);
            HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
        } else if (context != null && (this.mExitDesktopAlertDialog == null || !this.mExitDesktopAlertDialog.isShowing())) {
            if (isExitDesktopModeRemembered() && isDesktopMode(this.mProjMode) && (isExclusiveKeyboard ^ 1) != 0) {
                sendSwitchMsg();
            } else if (!isExclusiveKeyboard || (isShowExitDialog() ^ 1) == 0) {
                AlertDialog.Builder buider = new AlertDialog.Builder(context, 33947691);
                View view = LayoutInflater.from(buider.getContext()).inflate(34013290, null);
                if (view != null) {
                    String enter_toast = context.getString(33685914);
                    String exit_toast = context.getString(33685922);
                    String enter_exclusive_keyboard = context.getString(33685534);
                    HwPCUtils.log(TAG, "these string will be used in future:" + enter_toast + exit_toast + enter_exclusive_keyboard + context.getString(33685917));
                    ImageView imageView = (ImageView) view.findViewById(34603068);
                    TextView textView = (TextView) view.findViewById(34603067);
                    CheckBox checkBox = (CheckBox) view.findViewById(34603069);
                    if (imageView != null && textView != null && checkBox != null) {
                        imageView.setPadding(0, 0, 0, 0);
                        if (isExclusiveKeyboard) {
                            textView.setText(33685918);
                        } else {
                            textView.setText(33685916);
                        }
                        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                                $m$0(compoundButton, z);
                            }
                        });
                        this.mExitDesktopAlertDialog = buider.setTitle(33685921).setPositiveButton(33685920, new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass7(isExclusiveKeyboard, this, checkBox)).setNegativeButton(33685915, new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass3(this)).setView(view).setOnDismissListener(new com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ.AnonymousClass5(this)).create();
                        this.mExitDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mExitDesktopAlertDialog.getWindow().setType(2008);
                        this.mExitDesktopAlertDialog.setOnShowListener(new OnShowListener() {
                            public void onShow(DialogInterface dialog) {
                                Button btn = HwPCManagerService.this.mExitDesktopAlertDialog.getButton(-1);
                                btn.setFocusable(true);
                                btn.setFocusedByDefault(true);
                                btn.setFocusableInTouchMode(true);
                                btn.requestFocus();
                            }
                        });
                        this.mExitDesktopAlertDialog.show();
                        this.mExitDesktopAlertDialog.getWindow().getAttributes().setTitle("ExitDesktopAlertDialog");
                    }
                }
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_29972(CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        this.isNeedEixtDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                Secure.putInt(this.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 1);
            } else {
                Secure.putInt(this.mContext.getContentResolver(), "exit-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_31257(DialogInterface dialog, int which) {
        this.isNeedEixtDesktop = false;
        Log.d(TAG, "onClick:cancel which=" + which);
        dialog.cancel();
    }

    /* synthetic */ void lambda$-com_android_server_pc_HwPCManagerService_31547(DialogInterface dialog) {
        if (this.isNeedEixtDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            this.isNeedEixtDesktop = false;
            sendSwitchMsgDelayed(200);
        }
    }

    private void handleSwitchToDesktopMode() {
        HwPCUtils.log(TAG, "handleSwitchToDesktopMode, mIsDisplayAddedAfterSwitch = " + this.mIsDisplayAddedAfterSwitch);
        if (!this.mIsDisplayAddedAfterSwitch) {
            HwPCUtils.setPCDisplayID(this.mDisplayId);
            HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
            HwPCUtils.setPcCastModeInServer(true);
            updateIMEWithHardKeyboardState(true);
            saveRotationInPad();
            this.mAMS.freezeOrThawRotationInPcMode();
            saveDreamSettingInPad();
            this.mPCBeforeBootAnimTime = AsyncHttpClient.DEFAULT_RETRY_SLEEP_TIME_MILLIS;
            bindUnbindService(true);
            this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), this.mDisplayId);
            setUsePCModeMouseIconContext(true);
            Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
            if (!HwPCUtils.enabledInPad()) {
                showMKNotify();
            }
            sendNotificationForSwitch(this.mProjMode);
            bdReportDiffSrcStatus(true);
            bdReportSameSrcStatus(false);
            scheduleRestoreApps(this.mDisplayId);
            sendSwitchToStatusBar();
            setDesktopModeToAudioService(1);
            uploadPcDisplaySizePro();
            enableFingerprintSlideSwitch();
            lightPhoneScreen();
            if (HwPCUtils.enabledInPad()) {
                setFocusedPCDisplayId("enterDesktop");
            }
            relaunchIMEDelay(2000);
        }
    }

    UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    public void scheduleDisplayAdded(int displayId) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mHandler.removeMessages(6);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(6, displayId, -1));
            return;
        }
        HwPCUtils.log(TAG, "scheduleDisplayAdded checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void scheduleDisplayChanged(int displayId) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mHandler.removeMessages(7);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(7, displayId, -1));
            return;
        }
        HwPCUtils.log(TAG, "scheduleDisplayChanged checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void scheduleDisplayRemoved(int displayId) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mHandler.removeMessages(8);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(8, displayId, -1));
            return;
        }
        HwPCUtils.log(TAG, "scheduleDisplayRemoved checkCallingPermission failed" + Binder.getCallingPid());
    }

    private void bindUnbindService(boolean bind) {
        HwPCUtils.log(TAG, "bindUnbindService:" + bind + " current display is:" + this.mDisplayId);
        this.mPCSettingsObserver.restoreScreenOffSettings();
        if (bind) {
            Intent intent = new Intent();
            intent.putExtra(KEY_BEFORE_BOOT_ANIM_TIME, this.mPCBeforeBootAnimTime);
            intent.setComponent(this.mSystemUIComponent);
            this.mContext.bindService(intent, this.mConnSysUI, 1);
            Intent intent2 = new Intent();
            intent2.setComponent(this.mExplorerComponent);
            this.mContext.bindService(intent2, this.mConnExplorer, 1);
            registerScreenOnEvent();
            registerShutdownEvent();
            return;
        }
        unbindAllPcService();
        unRegisterScreenOnEvent();
        restoreRotationInPad();
        unRegisterShutdownEvent();
    }

    private void saveRotationInPad() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mRotationSwitch = System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                if (this.mRotationSwitch == 0) {
                    this.mRotationValue = System.getIntForUser(this.mContext.getContentResolver(), "user_rotation", 0, this.mUserId);
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveRotationInPad " + e);
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    private void restoreRotationInPad() {
        if (HwPCUtils.enabledInPad()) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mRotationSwitch == 0) {
                    System.putIntForUser(this.mContext.getContentResolver(), "user_rotation", this.mRotationValue, this.mUserId);
                } else {
                    System.putIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreRotationInPad " + e);
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    private void registerShutdownEvent() {
        if (HwPCUtils.enabledInPad()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mContext.registerReceiver(this.mShutdownReceiver, filter);
        }
    }

    private void unRegisterShutdownEvent() {
        if (HwPCUtils.enabledInPad()) {
            try {
                this.mContext.unregisterReceiver(this.mShutdownReceiver);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "unRegisterShutdownEvent " + e);
            }
        }
    }

    private void saveDreamSettingInPad() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            try {
                int DreamsSetting = Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (DreamsSetting == 1) {
                    Secure.putIntForUser(resolver, "screensaver_enabled", 0, this.mUserId);
                    this.mDreamsEnabledSetting = DreamsSetting;
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveDreamSettingInPad " + e);
            }
        }
    }

    private void restoreDreamSettingInPad() {
        if (HwPCUtils.enabledInPad()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            try {
                int DreamsSetting = Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (this.mDreamsEnabledSetting == 1 && this.mDreamsEnabledSetting != DreamsSetting) {
                    Secure.putIntForUser(resolver, "screensaver_enabled", this.mDreamsEnabledSetting, this.mUserId);
                    this.mDreamsEnabledSetting = -1;
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreDreamSettingInPad " + e);
            }
        }
    }

    private void unbindAllPcService() {
        HwPCUtils.log(TAG, "unbindAllPcService");
        try {
            this.mContext.unbindService(this.mConnSysUI);
            this.mContext.unbindService(this.mConnExplorer);
        } catch (Exception e) {
            HwPCUtils.log(TAG, "failed to unbind pc services");
        }
    }

    public HwPCManagerService(Context context, ActivityManagerService ams) {
        int i;
        this.mContext = context;
        this.mAMS = (HwActivityManagerService) ams;
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new LocalHandler(this.mHandlerThread.getLooper());
        UserInfo userInfo = this.mAMS.getCurrentUser();
        if (userInfo != null) {
            this.mUserId = userInfo.id;
        }
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    HwPCUtils.log(HwPCManagerService.TAG, "onUserSwitchComplete userId: " + newUserId);
                }

                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    HwPCUtils.log(HwPCManagerService.TAG, "onUserSwitching userId: " + newUserId);
                    HwPCManagerService.this.scheduleSwitchUser(newUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult(null);
                        } catch (RemoteException e) {
                            HwPCUtils.log(HwPCManagerService.TAG, "onUserSwitching Exception");
                        }
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "registerUserSwitchObserver RemoteException");
        }
        this.mDDC = DisplayDriverCommunicator.getInstance();
        int displayId = -1;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        if (this.mDisplayManager != null) {
            Display[] displays = this.mDisplayManager.getDisplays();
            if (displays != null && displays.length > 0) {
                i = displays.length - 1;
                while (i >= 0) {
                    if (!(displays[i] == null || displays[i].getDisplayId() == 0)) {
                        if (isWiredDisplay(displays[i].getDisplayId())) {
                            displayId = displays[i].getDisplayId();
                            break;
                        }
                    }
                    i--;
                }
            }
            if (displayId != -1) {
                final int finalDisplayId = displayId;
                this.mUIHandler.postDelayed(new Runnable() {
                    public void run() {
                        HwPCManagerService.this.scheduleDisplayAdded(finalDisplayId);
                    }
                }, 5000);
            }
        }
        InputManager im = (InputManager) this.mContext.getSystemService("input");
        if (im != null) {
            for (int device : InputDevice.getDeviceIds()) {
                this.mConnectedInputDevices |= whichInputDevice(InputDevice.getDevice(device));
            }
            im.registerInputDeviceListener(this.mInputDeviceListener, null);
        }
        this.mPCSettingsObserver = new PCSettingsObserver(this.mHandler);
        this.mPCSettingsObserver.readScreenOffSettings();
        this.mPCSettingsObserver.restoreScreenOffSettings();
        this.mProvisioned = deviceIsProvisioned();
        this.mPCSettingsObserver.observe();
        this.mProjMode = ProjectionMode.DESKTOP_MODE;
        if (HwPCUtils.enabledInPad()) {
            if (Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1) {
                this.mProjMode = ProjectionMode.DESKTOP_MODE;
            } else {
                this.mProjMode = ProjectionMode.PHONE_MODE;
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        if (this.mNm != null) {
            initNotifications();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NOTIFY_SWITCH_MODE);
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction(ACTION_NOTIFY_UNINSTALL_APP);
        filter.addAction(ACTION_NOTIFY_SHOW_MK);
        this.mContext.registerReceiver(this.mNotifyReceiver, filter, PERMISSION_BROADCAST_SWITCH_MODE, null);
        this.mUnlockScreenReceiver = new UnlockScreenReceiver(this, null);
        IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUnlockScreenReceiver, unlockFilter);
        this.mThumbnailPersister = new HwPCMultiWindowThumbnailPersister(ams);
        registerExternalPointerEventListener();
        this.mIBinderAudioService = ServiceManager.getService("audio");
        setDesktopModeToAudioService(-1);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mTelephonyPhone = (TelephonyManager) this.mContext.getSystemService("phone");
        HwPCUtils.setPcCastModeInServerEarly(ProjectionMode.PHONE_MODE);
    }

    boolean deviceIsProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    boolean isUserSetupComplete() {
        if (Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) != 0) {
            return true;
        }
        return false;
    }

    private void setDesktopModeToAudioService(int mode) {
        HwPCUtils.log(TAG, "setDesktopModeToAudioService, mIBinderAudioService = " + this.mIBinderAudioService);
        if (this.mIBinderAudioService != null) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInt(mode);
                this.mIBinderAudioService.transact(1104, data, reply, 0);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "setDesktopModeToAudioService RemoteException");
            }
        }
    }

    private void scheduleSwitchUser(int userId) {
        this.mHandler.removeMessages(12);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, userId, -1));
    }

    private void onSwitchUser(int userId) {
        HwPCUtils.log(TAG, "onSwitchUser userId =" + userId);
        this.mUserId = userId;
        if (this.mDisplayId != -1) {
            if (userId == 0) {
                sendNotificationForSwitch(this.mProjMode);
            } else {
                if (isDesktopMode(this.mProjMode)) {
                    lightPhoneScreen();
                    HwPCUtils.log(TAG, "onSwitchUser: The current mode is DesktopMode");
                    this.mProjMode = ProjectionMode.PHONE_MODE;
                    HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                    if (!HwPCUtils.enabledInPad()) {
                        bindUnbindService(false);
                    }
                    this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), this.mDisplayId);
                    setUsePCModeMouseIconContext(false);
                    HwPCUtils.setPcCastModeInServer(false);
                    updateIMEWithHardKeyboardState(false);
                    Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
                    this.mDDC.resetProjectionMode();
                    updateFingerprintSlideSwitch();
                    sendSwitchToStatusBar();
                    relaunchIMEDelay(0);
                    if (HwPCUtils.enabledInPad()) {
                        bindUnbindService(false);
                    }
                }
                if (this.mNm != null) {
                    this.mNm.cancelAll();
                    this.mHasKMNotf = false;
                    this.mHasSwitchNtf = false;
                }
            }
        }
    }

    private boolean isWiredDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        if (this.mDisplayManager == null) {
            return false;
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            return false;
        }
        int type = display.getType();
        boolean z = type != 2 ? (type == 5 || type == 4) ? this.mSupportOverlay : false : true;
        return z;
    }

    private void initNotifications() {
        this.mBuilderForInput = new Builder(this.mContext, "HW_PCM");
        this.mBuilderForInput.setContentIntent(PendingIntent.getBroadcastAsUser(this.mContext, 1, new Intent(ACTION_NOTIFY_SHOW_MK), 134217728, UserHandle.OWNER));
        this.mBuilderForInput.setSmallIcon(33751738);
        this.mBuilderForInput.setVisibility(-1);
        this.mBuilderForInput.setGroup("HW_PCM");
        this.mBuilderForSwitch = new Builder(this.mContext, "HW_PCM");
        this.mBuilderForSwitch.setSmallIcon(33751738);
        this.mBuilderForSwitch.setContentIntent(PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(ACTION_NOTIFY_SWITCH_MODE), 134217728, UserHandle.OWNER));
        this.mBuilderForSwitch.setVisibility(-1);
        this.mBuilderForSwitch.setPriority(1);
    }

    private void sendNotificationForSwitch(ProjectionMode projMode) {
        if (!HwPCUtils.enabledInPad() && this.mNm != null) {
            String mode;
            String otherMode;
            this.mBuilderForSwitch.setAppName(this.mContext.getString(33685941));
            if (isDesktopMode(projMode)) {
                mode = this.mContext.getString(33685957);
                otherMode = this.mContext.getString(33685958);
            } else {
                mode = this.mContext.getString(33685958);
                otherMode = this.mContext.getString(33685957);
            }
            this.mBuilderForSwitch.setContentTitle(mode);
            this.mBuilderForSwitch.setContentText(String.format(this.mContext.getString(33685959), new Object[]{otherMode}));
            Notification notification = this.mBuilderForSwitch.getNotification();
            notification.flags |= 2;
            notification.flags |= 32;
            this.mNm.notify(TAG, 0, notification);
            this.mHasSwitchNtf = true;
        }
    }

    private void sendNotificationForInputDev() {
        if (!HwPCUtils.enabledInPad() && this.mNm != null) {
            this.mBuilderForInput.setAppName(this.mContext.getString(33685941));
            this.mBuilderForInput.setContentTitle(this.mContext.getString(33685960));
            this.mBuilderForInput.setContentText(this.mContext.getString(33685961));
            Notification notification = this.mBuilderForInput.getNotification();
            notification.flags |= 2;
            notification.flags |= 32;
            this.mNm.notify(TAG, 1, notification);
            this.mHasKMNotf = true;
        }
    }

    public void onDisplayChanged(int displayId) {
        HwPCUtils.log(TAG, "onDisplayChanged, displayId:" + displayId);
        if (!this.mProvisioned) {
        }
    }

    private void lockScreenWhenDisconnected() {
        if (!isScreenPowerOn()) {
            try {
                HwPCUtils.log(TAG, "Lock phone screen when PC displayer is disconnected.");
                ((PowerManager) this.mContext.getSystemService("power")).goToSleep(SystemClock.uptimeMillis(), 5, 0);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "Error accured when locking screen as PC displayer is disconnected.");
            }
        }
    }

    public void onDisplayRemoved(int displayId) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "onDisplayRemoved enabledInPad return");
        } else if (HwPCUtils.enabled()) {
            boolean isInPhoneMode = HwPCUtils.getPhoneDisplayID() == displayId ? isDesktopMode(this.mProjMode) ^ 1 : false;
            HwPCUtils.log(TAG, "onDisplayRemoved, displayId:" + displayId + ", isInPhoneMode:" + isInPhoneMode);
            if (HwPCUtils.getPCDisplayID() != displayId && (isInPhoneMode ^ 1) != 0) {
                HwPCUtils.log(TAG, "onDisplayRemoved, displayId is neither PC Display ID nor Phone Display ID.");
            } else if (this.mProvisioned) {
                lockScreenWhenDisconnected();
                HwPCUtils.setPcCastModeInServerEarly(ProjectionMode.PHONE_MODE);
                if (this.mProjMode == ProjectionMode.DESKTOP_MODE) {
                    bdReportDiffSrcStatus(false);
                } else {
                    bdReportSameSrcStatus(false);
                }
                this.mDisplayId = -1;
                if (this.mNm != null) {
                    this.mNm.cancelAll();
                    this.mHasKMNotf = false;
                    this.mHasSwitchNtf = false;
                }
                if (isInPhoneMode) {
                    HwPCUtils.setPhoneDisplayID(-1);
                } else {
                    bindUnbindService(false);
                    setUsePCModeMouseIconContext(false);
                    updateIMEWithHardKeyboardState(false);
                }
                HwPCUtils.setPcCastModeInServer(false);
                setDesktopModeToAudioService(-1);
                updateFingerprintSlideSwitch();
            } else {
                HwPCUtils.log(TAG, "onDisplayRemoved not permitted before setup or not scheduleDisplayAdded");
            }
        }
    }

    private void onDisplayAdded(int displayId) {
        if (HwPCUtils.enabled()) {
            HwPCUtils.log(TAG, "onDisplayAdded, displayId:" + displayId);
            if (!this.mProvisioned) {
                HwPCUtils.log(TAG, "onDisplayAdded not permitted before setup");
            } else if (displayId == -1 || displayId == 0) {
                HwPCUtils.log(TAG, "context is null or is default display");
            } else if (HwPCUtils.enabledInPad() && this.mUserId != 0) {
                HwPCUtils.log(TAG, "switchProjMode failed! currentUser is not UserHandle.USER_OWNER");
            } else if (HwPCUtils.enabledInPad() && isMonkeyRunning()) {
                HwPCUtils.log(TAG, "onDisplayAdded isMonkeyRunning return !");
            } else {
                if (isWiredDisplay(displayId)) {
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT, "");
                }
                bdReportConnectDisplay(displayId);
                if (isWiredDisplay(displayId)) {
                    if (HwPCUtils.enabledInPad()) {
                        if (this.mPadPCDisplayIsRemoved) {
                            this.mDisplayId = displayId;
                            this.mPadPCDisplayIsRemoved = false;
                            HwPCUtils.log(TAG, "PadPCDisplayIsRemoved return.");
                            return;
                        } else if (deviceIsProvisioned() && (isUserSetupComplete() ^ 1) == 0) {
                            if (Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1) {
                                this.mProjMode = ProjectionMode.DESKTOP_MODE;
                            } else {
                                this.mProjMode = ProjectionMode.PHONE_MODE;
                                if (isExclusiveKeyboardConnected()) {
                                    showEnterDesktopAlertDialog(getCurrentContext(), true);
                                }
                            }
                        } else {
                            HwPCUtils.log(TAG, "Startup Guide is not Complete");
                            return;
                        }
                    }
                    this.mDisplayId = displayId;
                    final boolean guideStarted = Secure.getInt(this.mContext.getContentResolver(), "guide-started", 0) == 1;
                    if (!guideStarted) {
                        this.mHandler.removeMessages(3);
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 500);
                    }
                    boolean exist = systemUIExist() ? explorerExist() : false;
                    boolean enterDesktopMode = exist && isDesktopMode(this.mProjMode) && this.mUserId == 0;
                    HwPCUtils.log(TAG, "onDisplayAdded mProjMode " + this.mProjMode + ", mConnectedInputDevices = " + this.mConnectedInputDevices + ", exist =" + exist + ", mUserId = " + this.mUserId + ", enterDesktopMode =" + enterDesktopMode);
                    if (this.mDisplayManager != null) {
                        Display display = this.mDisplayManager.getDisplay(this.mDisplayId);
                        if (display != null) {
                            DisplayInfo displayInfo = new DisplayInfo();
                            if (display.getDisplayInfo(displayInfo)) {
                                int width = displayInfo.getNaturalWidth();
                                HwPCUtils.log(TAG, "width = " + width + ", height = " + displayInfo.getNaturalHeight());
                                if (width > RESO_1080P) {
                                    this.mIsDisplayLargerThan1080p = true;
                                } else {
                                    this.mIsDisplayLargerThan1080p = false;
                                }
                            }
                        }
                    }
                    Message msg;
                    if (enterDesktopMode) {
                        HwPCUtils.setPCDisplayID(displayId);
                        if (!isDesktopMode(this.mProjMode)) {
                            Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
                        }
                        this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (guideStarted && !HwPCManagerService.this.existMouseInputDevices()) {
                                    HwPCManagerService.this.sendShowMkMessage();
                                }
                            }
                        }, 2000);
                        this.mProjMode = ProjectionMode.DESKTOP_MODE;
                        HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                        HwPCUtils.setPcCastModeInServer(true);
                        updateIMEWithHardKeyboardState(true);
                        saveRotationInPad();
                        this.mAMS.freezeOrThawRotationInPcMode();
                        this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), this.mDisplayId);
                        setUsePCModeMouseIconContext(true);
                        if (this.restartByUnlock2SetAnimTime) {
                            this.mPCBeforeBootAnimTime = AsyncHttpClient.DEFAULT_RETRY_SLEEP_TIME_MILLIS;
                        } else {
                            this.mPCBeforeBootAnimTime = TIME_DISPALY_ADD_BEFORE_BOOT_ANIM;
                        }
                        bindUnbindService(true);
                        this.restartByUnlock2SetAnimTime = false;
                        sendShowMKNotifyMessage();
                        this.mHandler.removeMessages(1);
                        msg = this.mHandler.obtainMessage(1);
                        msg.obj = this.mProjMode;
                        this.mHandler.sendMessage(msg);
                        bdReportSameSrcStatus(true);
                        setDesktopModeToAudioService(1);
                        this.mIsDisplayAddedAfterSwitch = true;
                        backToHomeInDefaultDisplay(this.mDisplayId);
                        uploadPcDisplaySizePro();
                        enableFingerprintSlideSwitch();
                        lightPhoneScreen();
                        if (HwPCUtils.enabledInPad()) {
                            setFocusedPCDisplayId("enterDesktop");
                        }
                        relaunchIMEDelay(2000);
                        if (HwPCUtils.enabledInPad()) {
                            this.mHandler.removeMessages(17);
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(17));
                        }
                    } else {
                        HwPCUtils.setPhoneDisplayID(displayId);
                        if (HwPCUtils.enabledInPad()) {
                            HwPCUtils.log(TAG, "onDisplayAdded there is something wrong when enter PAD PC mode !");
                            return;
                        }
                        lightPhoneScreen();
                        this.mProjMode = ProjectionMode.PHONE_MODE;
                        HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                        this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), this.mDisplayId);
                        setUsePCModeMouseIconContext(false);
                        HwPCUtils.setPcCastModeInServer(false);
                        updateIMEWithHardKeyboardState(false);
                        this.mHandler.removeMessages(1);
                        msg = this.mHandler.obtainMessage(1);
                        msg.obj = this.mProjMode;
                        this.mHandler.sendMessage(msg);
                        bdReportDiffSrcStatus(true);
                        setDesktopModeToAudioService(0);
                        this.mAMS.restoreRotationInPcMode();
                        updateFingerprintSlideSwitch();
                    }
                    scheduleRestoreApps(displayId);
                    sendSwitchToStatusBar();
                    return;
                }
                HwPCUtils.log(TAG, "is not a wired display.");
            }
        }
    }

    private void backToHomeInDefaultDisplay(int curDisplayId) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "backToHomeInDefaultDisplay");
            try {
                this.mContext.startActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"));
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "ActivityMovedToDesktopDisplay fail to start go home");
            }
        }
    }

    private void setUsePCModeMouseIconContext(boolean pcmode) {
        HwInputManagerLocalService inputManager = (HwInputManagerLocalService) LocalServices.getService(HwInputManagerLocalService.class);
        if (inputManager == null) {
            return;
        }
        if (pcmode) {
            if (this.mDisplayManager == null) {
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
            }
            if (this.mDisplayManager != null) {
                Display display = null;
                for (Display dis : this.mDisplayManager.getDisplays()) {
                    if (dis != null && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                        display = dis;
                        break;
                    }
                }
                if (display != null) {
                    Context context = this.mContext.createDisplayContext(display);
                    HwPCUtils.log(TAG, "setUsePCModeMouseIconContext displayId = " + display.toString());
                    inputManager.setExternalDisplayContext(context);
                    return;
                }
                return;
            }
            return;
        }
        inputManager.setExternalDisplayContext(null);
    }

    private void sendSwitchMsg() {
        this.mHandler.removeMessages(4);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
    }

    public void switchProjMode() {
        int i = 0;
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "switchProjMode, mProjMode " + this.mProjMode + ", isEnterDesktopModeRemembered:" + isEnterDesktopModeRemembered() + " isExitDesktopModeRemembered:" + isExitDesktopModeRemembered());
            if (!HwPCUtils.enabledInPad()) {
                HwPCUtils.log(TAG, "Not enabledInPad()");
                sendSwitchMsg();
                return;
            } else if (this.mUserId != 0) {
                HwPCUtils.log(TAG, "switchProjMode failed! currentUser is not UserHandle.USER_OWNER");
                return;
            } else if (isCalling()) {
                if (isDesktopMode(this.mProjMode)) {
                    i = this.mDisplayId;
                }
                showCallingToast(i);
                HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
                return;
            } else {
                if (isDesktopMode(this.mProjMode)) {
                    showExitDesktopAlertDialog(getCurrentContext(), false);
                } else {
                    showEnterDesktopAlertDialog(getCurrentContext(), false);
                }
                return;
            }
        }
        HwPCUtils.log(TAG, "switchProjMode failed " + Binder.getCallingPid());
    }

    private void sendSwitchToStatusBar() {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "sendSwitchToStatusBar!");
            Intent intent = new Intent();
            intent.setAction(ACTION_NOTIFY_CHANGE_STATUS_BAR);
            this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_CHANGE_STATUS_BAR);
        }
    }

    private void refreshNotifications() {
        HwPCUtils.log(TAG, "refreshNotifications mHasKMNotf = " + this.mHasKMNotf + ", mHasSwitchNtf = " + this.mHasSwitchNtf);
        this.mHandler.removeMessages(5);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5));
    }

    private static int whichInputDevice(InputDevice device) {
        int ret = 0;
        if (device != null) {
            HwPCUtils.log(TAG, "device=" + device + ", souces = " + device.getSources());
        }
        if (device == null || !device.isExternal()) {
            HwPCUtils.log(TAG, "whichInputDevice unkown input device");
            return 0;
        }
        if ((device.getSources() & 139270) != 0) {
            ret = 1;
        }
        if ((device.getSources() & CommandsInterface.EMCOM_SD_XENGINE_START_ACC) != 0) {
            return ret | 2;
        }
        return ret;
    }

    private boolean isExclusiveKeyboardConnected() {
        int[] devices = InputDevice.getDeviceIds();
        for (int device : devices) {
            InputDevice device2 = InputDevice.getDevice(device);
            if (device2 != null && isExclusiveKeyboard(device2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExclusiveKeyboard(InputDevice inputDevice) {
        if (inputDevice == null) {
            HwPCUtils.log(TAG, "isExclusiveKeyboard=false");
            return false;
        }
        int keyboardProductId = inputDevice.getProductId();
        int keyboardVendorId = inputDevice.getVendorId();
        HwPCUtils.log(TAG, "Keyboard ProductId:" + keyboardProductId + " VendorId:" + keyboardVendorId);
        if (KEYBOARD_PRODUCT_ID.length != KEYBOARD_VENDOR_ID.length) {
            return false;
        }
        int i = 0;
        while (i < KEYBOARD_PRODUCT_ID.length) {
            if (keyboardProductId == KEYBOARD_PRODUCT_ID[i] && keyboardVendorId == KEYBOARD_VENDOR_ID[i]) {
                HwPCUtils.log(TAG, "isExclusiveKeyboard=true");
                return true;
            }
            i++;
        }
        HwPCUtils.log(TAG, "isExclusiveKeyboard=false");
        return false;
    }

    private void launchGuide() {
        if (!HwPCUtils.enabledInPad()) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addFlags(268435456);
            intent.setClassName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.OOBEFragmentActivity");
            try {
                this.mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "fail to start guide");
            }
        }
    }

    private void sendShowMkMessage() {
        HwPCUtils.log(TAG, "sendShowMkMessage todo launch touchpad");
        if (this.mSupportTouchPad && this.mProjMode == ProjectionMode.DESKTOP_MODE && this.mDisplayId != -1) {
            this.mHandler.removeMessages(13);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(13));
            return;
        }
        HwPCUtils.log(TAG, "unable to send ShowMk message, existMouse:" + existMouseInputDevices() + " mDisplayId:" + this.mDisplayId + " mProjMode:" + this.mProjMode);
    }

    private void launchMK() {
        if (!HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "launchMK todo start touchpad activiy");
            if (this.mSupportTouchPad && this.mProjMode == ProjectionMode.DESKTOP_MODE && this.mDisplayId != -1) {
                Intent intent = new Intent();
                intent.addFlags(268435456);
                intent.setComponent(this.mTouchPadComponent);
                try {
                    ActivityOptions activityOptions = ActivityOptions.makeBasic();
                    activityOptions.setLaunchDisplayId(0);
                    this.mContext.startActivity(intent, activityOptions.toBundle());
                } catch (ActivityNotFoundException e) {
                    HwPCUtils.log(TAG, "fail to start mk activity");
                }
                return;
            }
            HwPCUtils.log(TAG, "cannot launch MK, existMouse:" + existMouseInputDevices() + " mDisplayId:" + this.mDisplayId);
        }
    }

    private void removeMKNotify() {
        if (this.mNm != null && this.mHasKMNotf) {
            this.mNm.cancel(TAG, 1);
            this.mHasKMNotf = false;
        }
    }

    private void sendShowMKNotifyMessage() {
        if (!this.mSupportTouchPad || this.mHasKMNotf || this.mProjMode != ProjectionMode.DESKTOP_MODE || this.mDisplayId == -1) {
            HwPCUtils.log(TAG, "unable to send ShowMKNotify message, mHasKMNotf:" + this.mHasKMNotf + " mDisplayId:" + this.mDisplayId + " existMouse:" + existMouseInputDevices());
            return;
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    private void sendUninstallAppMessage(String packageName) {
        HwPCUtils.log(TAG, "sendUninstallAppMessage: " + packageName);
        Message msg = this.mHandler.obtainMessage(16);
        msg.obj = packageName;
        this.mHandler.sendMessage(msg);
    }

    private void showMKNotify() {
        if (!this.mSupportTouchPad || this.mHasKMNotf || this.mProjMode != ProjectionMode.DESKTOP_MODE || this.mDisplayId == -1) {
            HwPCUtils.log(TAG, "cannot show mk notify, mHasKMNotf:" + this.mHasKMNotf + " mDisplayId:" + this.mDisplayId + " existMouse:" + existMouseInputDevices());
        } else {
            sendNotificationForInputDev();
        }
    }

    private void refreshMKNotify() {
        if (this.mHasKMNotf) {
            sendNotificationForInputDev();
        }
    }

    private boolean existMouseInputDevices() {
        return (this.mConnectedInputDevices & 1) != 0;
    }

    private boolean systemUIExist() {
        boolean exist = this.mContext.getPackageManager().resolveService(new Intent().setComponent(this.mSystemUIComponent), 65536) != null;
        if (!exist) {
            HwPCUtils.log(TAG, "systemUI does not Exist!!!");
        }
        return exist;
    }

    private boolean explorerExist() {
        boolean exist = this.mContext.getPackageManager().resolveService(new Intent().setComponent(this.mExplorerComponent), 65536) != null;
        if (!exist) {
            HwPCUtils.log(TAG, "Explorer does not Exist!!!");
        }
        return exist;
    }

    private boolean checkCallingPermission(String permission) {
        return this.mAMS.checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid())) == 0;
    }

    public boolean getCastMode() {
        return isDesktopMode(this.mProjMode) ? HwPCUtils.isValidExtDisplayId(this.mDisplayId) : false;
    }

    public int getPackageSupportPcState(String packageName) {
        HwPCUtils.log(TAG, "getPackageSupportPcState:" + packageName);
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return WhiteListAppStrategyManager.getInstance(this.mContext).getAppSupportPCState(packageName);
        }
        HwPCUtils.log(TAG, "getPackageSupportPcState checkCallingPermission failed" + Binder.getCallingPid());
        return -1;
    }

    public List<String> getAllSupportPcAppList() {
        HwPCUtils.log(TAG, "getAllSupportPcAppList");
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return WhiteListAppStrategyManager.getInstance(this.mContext).getAllSupportPcAppList();
        }
        HwPCUtils.log(TAG, "getAllSupportPcAppList checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void relaunchIMEIfNecessary() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mHandler.removeMessages(14);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(14));
            return;
        }
        HwPCUtils.log(TAG, "relaunchIMEIfNecessary checkCallingPermission failed" + Binder.getCallingPid());
    }

    private void relaunchIMEDelay(int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "relaunchIMEDelay");
            this.mHandler.removeMessages(14);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(14), (long) delayMillis);
        }
    }

    private void updateDisplayOverrideConfiguration(int display, int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "updateDisplayOverrideConfiguration");
            this.mHandler.removeMessages(18);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(18, display, 0), (long) delayMillis);
        }
    }

    private void doUpdateDisplayOverrideConfiguration(int displayid) {
        this.mAMS.updateDisplayOverrideConfiguration(null, displayid);
    }

    private void doRelaunchIMEIfNecessary() {
        this.mAMS.relaunchIMEIfNecessary();
    }

    public void hwRestoreTask(int taskId, float x, float y) {
        if (this.mAMS.checkTaskId(taskId) || checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.hwRestoreTask(taskId, x, y);
        } else {
            HwPCUtils.log(TAG, "hwRestoreTask checktaskId failed:" + taskId);
        }
    }

    public void hwResizeTask(int taskId, Rect bounds) {
        if (this.mAMS.checkTaskId(taskId) || checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.hwResizeTask(taskId, bounds);
        } else {
            HwPCUtils.log(TAG, "hwResizeTask checktaskId failed:" + taskId);
        }
    }

    public int getWindowState(IBinder token) {
        return this.mAMS.getWindowState(token);
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return this.mAMS.getHwRecentTaskInfo(taskId);
        }
        HwPCUtils.log(TAG, "getHwRecentTaskInfo checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void registerHwTaskStackListener(ITaskStackListener listener) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.registerHwTaskStackListener(listener);
        } else {
            HwPCUtils.log(TAG, "registerHwTaskStackListener checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.unRegisterHwTaskStackListener(listener);
        } else {
            HwPCUtils.log(TAG, "unRegisterHwTaskStackListener checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return this.mAMS.getDisplayBitmap(displayId, width, height);
        }
        HwPCUtils.log(TAG, "getDisplayBitmap checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void registHwSystemUIController(IHwSystemUIController controller) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mController = controller;
        } else {
            HwPCUtils.log(TAG, "registHwSystemUIController checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public void showTopBar() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.showClientTopBar();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("showTopBar", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "showTopBar checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void showStartMenu() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.showClientStartMenu();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("showStartMenu", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "showStartMenu checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void screenshotPc() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.screenshotPcDisplay();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("screenshotPc", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "screenshotPc checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void userActivityOnDesktop() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.userActivityOnDesktop();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("userActivityOnDesktop", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "userActivityOnDesktop checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void closeTopWindow() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.closeClienTopWindow();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("closeTopWindow", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "closeTopWindow checkCallingPermission failed" + Binder.getCallingPid());
    }

    public void triggerSwitchTaskView(boolean show) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.triggerSwitchClientTaskView(show);
                }
            } catch (RemoteException e) {
                HwPCUtils.log("screenshotPc", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "triggerSwitchTaskView checkCallingPermission failed" + Binder.getCallingPid());
    }

    public TaskThumbnail getTaskThumbnailEx(int id) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return this.mThumbnailPersister.getTaskThumbnail(id);
        }
        HwPCUtils.log(TAG, "getTaskThumbnailEx checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void toggleHome() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.toggleHome();
        } else {
            HwPCUtils.log(TAG, "toggleHome checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public boolean injectInputEventExternal(InputEvent event, int mode) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwInputManagerLocalService inputManager = (HwInputManagerLocalService) LocalServices.getService(HwInputManagerLocalService.class);
            if (inputManager != null) {
                return inputManager.injectInputEvent(event, mode);
            }
            return false;
        }
        HwPCUtils.log(TAG, "injectInputEventExternal checkCallingPermission failed" + Binder.getCallingPid());
        return false;
    }

    public void registerExternalPointerEventListener() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.registerExternalPointerEventListener(this.mPointerListener);
        } else {
            HwPCUtils.log(TAG, "registerExternalPointerEventListener checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public void unregisterExternalPointerEventListener() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.unregisterExternalPointerEventListener(this.mPointerListener);
        } else {
            HwPCUtils.log(TAG, "unregisterExternalPointerEventListener checkCallingPermission failed" + Binder.getCallingPid());
        }
    }

    public float[] getPointerCoordinateAxis() {
        float[] axis = new float[2];
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            axis[0] = this.mAxisX;
            axis[1] = this.mAxisY;
            return axis;
        }
        HwPCUtils.log(TAG, "getPointerCoordinateAxis checkCallingPermission failed" + Binder.getCallingPid());
        return axis;
    }

    private Context getDisplayContext(Context context, int displayId) {
        Display targetDisplay = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    public void saveAppIntent(List<Intent> intents) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mHandler.removeMessages(15);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(15, intents));
            return;
        }
        HwPCUtils.log(TAG, "saveAppIntent checkCallingPermission failed" + Binder.getCallingPid());
    }

    private void scheduleRestoreApps(int displayId) {
        if (!this.mIntentList.isEmpty()) {
            this.mAlarmManager.cancel(this.mAlarmListener);
            this.mHandler.removeMessages(10);
            this.mHandler.removeMessages(9);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(9, displayId, 0), 3000);
        }
    }

    private void handleRestoreApps(int displayId) {
        int N = this.mIntentList.size();
        for (int i = 0; i < N; i++) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10, displayId, 0, this.mIntentList.get(i)), (long) (i * 800));
        }
        this.mIntentList.clear();
    }

    private void restoreApp(int displayId, Intent intent) {
        Context displayContext = getDisplayContext(this.mContext, displayId);
        if (!(displayContext == null || intent == null)) {
            try {
                if (HwPCUtils.enabledInPad() && intent.getComponent() != null && "com.android.incallui".equals(intent.getComponent().getPackageName())) {
                    HwPCUtils.log(TAG, " restoreApp skip intent:" + intent + ",displayId:" + displayId);
                    return;
                }
                displayContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "startActivity error.");
            }
        }
    }

    public void lockScreen() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            try {
                if (this.mController != null) {
                    this.mController.lockScreen();
                }
            } catch (RemoteException e) {
                HwPCUtils.log("lockScreen", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "lockScreen checkCallingPermission failed" + Binder.getCallingPid());
    }

    private void bdReportSameSrcStatus(boolean isConnected) {
        if (isConnected) {
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT, "same src is connected");
        } else {
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT, "same src is disconnected");
        }
    }

    private void bdReportDiffSrcStatus(boolean isConnected) {
        if (isConnected) {
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT, "diff src is connected");
            if (HwPCUtils.enabledInPad()) {
                HwPCUtils.bdReport(this.mContext, 10026, "enter");
                return;
            }
            return;
        }
        HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT, "diff src is disconnected");
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.bdReport(this.mContext, 10026, "exit");
        }
    }

    private void bdReportConnectDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null) {
            String name = display.getName();
            switch (display.getType()) {
                case 0:
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is UNKNOWN");
                    break;
                case 2:
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is HDMI");
                    break;
                case 3:
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is WIFI");
                    break;
                case 4:
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is OVERLAY");
                    break;
                case 5:
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is VIRTUAL");
                    break;
            }
            Point size = new Point();
            display.getRealSize(size);
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT, "Display<" + name + "> width:" + size.x + " height:" + size.y);
            BigInteger value = new BigInteger(size.x + "").gcd(new BigInteger(size.y + ""));
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT, "Display<" + name + "> ratio:" + (size.x / value.intValue()) + ":" + (size.y / value.intValue()));
        }
    }

    public boolean isPackageRunningOnPCMode(String packageName, int uid) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            boolean ret = this.mAMS.isPackageRunningOnPCMode(packageName, uid);
            HwPCUtils.log(TAG, "isPackageRunningOnPCMode ret = " + ret);
            return ret;
        }
        HwPCUtils.log(TAG, "isPackageRunningOnPCMode checkCallingPermission failed " + Binder.getCallingPid());
        return false;
    }

    public boolean isScreenPowerOn() {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            boolean z;
            synchronized (this.mScreenAccessLock) {
                z = this.mScreenPowerOn;
            }
            return z;
        }
        HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
        return true;
    }

    public void setScreenPower(boolean powerOn) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            setScreenPowerInner(powerOn, true);
        } else {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
        }
    }

    private void lightPhoneScreen() {
        setScreenPowerInner(true, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0092 A:{SYNTHETIC, Splitter: B:42:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0077 A:{SYNTHETIC, Splitter: B:33:0x0077} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00a4 A:{SYNTHETIC, Splitter: B:49:0x00a4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setScreenPowerInner(boolean powerOn, boolean checking) {
        Throwable th;
        synchronized (this.mScreenAccessLock) {
            HwPCUtils.log(TAG, "setScreenPower old=" + this.mScreenPowerOn + " new=" + powerOn);
            if (powerOn == this.mScreenPowerOn && checking) {
                return;
            }
            FileOutputStream fileDevice = null;
            String val = powerOn ? "1" : "0";
            try {
                FileOutputStream fileDevice2 = new FileOutputStream(new File(SCREEN_POWER_DEVICE));
                try {
                    fileDevice2.write(val.getBytes("utf-8"));
                    this.mScreenPowerOn = powerOn;
                    if (fileDevice2 != null) {
                        try {
                            fileDevice2.close();
                        } catch (IOException e) {
                            HwPCUtils.log(TAG, "setScreenPower IOException2");
                        }
                    }
                    fileDevice = fileDevice2;
                } catch (FileNotFoundException e2) {
                    fileDevice = fileDevice2;
                    HwPCUtils.log(TAG, "setScreenPower FileNotFoundException");
                    if (fileDevice != null) {
                    }
                    return;
                } catch (IOException e3) {
                    fileDevice = fileDevice2;
                    try {
                        HwPCUtils.log(TAG, "setScreenPower IOException1");
                        if (fileDevice != null) {
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileDevice != null) {
                            try {
                                fileDevice.close();
                            } catch (IOException e4) {
                                HwPCUtils.log(TAG, "setScreenPower IOException2");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileDevice = fileDevice2;
                    if (fileDevice != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                HwPCUtils.log(TAG, "setScreenPower FileNotFoundException");
                if (fileDevice != null) {
                    try {
                        fileDevice.close();
                    } catch (IOException e6) {
                        HwPCUtils.log(TAG, "setScreenPower IOException2");
                    }
                }
                return;
            } catch (IOException e7) {
                HwPCUtils.log(TAG, "setScreenPower IOException1");
                if (fileDevice != null) {
                    try {
                        fileDevice.close();
                    } catch (IOException e8) {
                        HwPCUtils.log(TAG, "setScreenPower IOException2");
                    }
                }
                return;
            }
        }
    }

    public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "dispatchKeyEvent " + ke);
            try {
                if (this.mController != null) {
                    this.mController.dispatchKeyEventForExclusiveKeyboard(ke);
                }
            } catch (RemoteException e) {
                HwPCUtils.log("dispatchKeyEvent", "RemoteException");
            }
            return;
        }
        HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
    }

    private boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    private void registerScreenOnEvent() {
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_ALARM_WAKEUP);
        try {
            this.mContext.registerReceiver(this.mAlarmClockReceiver, filter1);
        } catch (IllegalArgumentException e) {
            HwPCUtils.log(TAG, "registerScreenOnEvent " + e);
        }
    }

    private void unRegisterScreenOnEvent() {
        try {
            this.mContext.unregisterReceiver(this.mAlarmClockReceiver);
        } catch (IllegalArgumentException e) {
            HwPCUtils.log(TAG, "unRegisterScreenOnEvent " + e);
        }
    }

    private void enableFingerprintSlideSwitch() {
        try {
            ContentResolver resolver = this.mContext.getContentResolver();
            int userId = ActivityManager.getCurrentUser();
            if (System.getIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 0, userId) == 0) {
                HwPCUtils.log(TAG, "enableFingerprintSlideSwitch");
                System.putIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
            }
        } catch (Exception e) {
            HwPCUtils.log(TAG, "enableFingerprintSlideSwitch " + e);
        }
    }

    private void updateFingerprintSlideSwitch() {
        try {
            this.mAMS.updateFingerprintSlideSwitch();
        } catch (Exception e) {
            HwPCUtils.log(TAG, "updateFingerprintSlideSwitch " + e);
        }
    }

    private void updateIMEWithHardKeyboardState(boolean switchToPcMode) {
        long ident = Binder.clearCallingIdentity();
        if (switchToPcMode) {
            try {
                this.mIMEWithHardKeyboardState = Secure.getInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
                HwPCUtils.log(TAG, "switch to PcMode, IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
                if (HwPCUtils.enabledInPad()) {
                    Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0);
                } else {
                    Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 1);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            HwPCUtils.log(TAG, "switch To PhoneMode, update IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
            Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
        }
        Binder.restoreCallingIdentity(ident);
    }

    private boolean isCalling() {
        this.mPhoneState = getPhoneState();
        if (this.mPhoneState != 0) {
            return true;
        }
        return false;
    }

    private int getPhoneState() {
        int phoneState = 0;
        int simCount = this.mTelephonyPhone.getPhoneCount();
        for (int i = 0; i < simCount; i++) {
            phoneState = this.mTelephonyPhone.getCallState(i);
            if (phoneState != 0) {
                HwPCUtils.log(TAG, "simCount:" + simCount + " phoneState:" + phoneState);
                return phoneState;
            }
        }
        HwPCUtils.log(TAG, "simCount:" + simCount + " phoneState:" + phoneState);
        return 0;
    }

    protected void showCallingToast(final int displayId) {
        if (HwPCUtils.enabledInPad()) {
            Context context;
            if (HwPCUtils.isValidExtDisplayId(displayId)) {
                context = HwPCUtils.getDisplayContext(this.mContext, displayId);
            } else {
                context = this.mContext;
            }
            if (context != null) {
                UiThread.getHandler().post(new Runnable() {
                    public void run() {
                        if (HwPCManagerService.this.mCallingToast != null) {
                            HwPCManagerService.this.mCallingToast.cancel();
                        }
                        if (HwPCUtils.isValidExtDisplayId(displayId)) {
                            HwPCManagerService.this.mCallingToast = Toast.makeText(context, context.getResources().getString(33685919), 1);
                        } else {
                            HwPCManagerService.this.mCallingToast = Toast.makeText(context, context.getResources().getString(33685607), 1);
                        }
                        if (HwPCManagerService.this.mCallingToast != null) {
                            HwPCManagerService.this.mCallingToast.show();
                        }
                    }
                });
            }
        }
    }

    public int forceDisplayMode(int mode) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "forceDisplayMode mode:" + mode);
            switch (mode) {
                case 1005:
                    return getOverScanMode();
                case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
                    return setOverScanMode(0);
                case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
                    return setOverScanMode(1);
                case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                    return setOverScanMode(2);
                default:
                    return 0;
            }
        }
        HwPCUtils.log(TAG, "forceDisplayMode checkCallingPermission failed" + Binder.getCallingPid());
        return 0;
    }

    private int setOverScanMode(int mode) {
        HwPCUtils.log(TAG, "setOverScanMode mode:" + mode);
        this.mAMS.setPCScreenDpMode(mode);
        return 0;
    }

    private int getOverScanMode() {
        HwPCUtils.log(TAG, "getOverScanMode");
        int mode = this.mAMS.getPCScreenDisplayMode();
        if (mode == 1) {
            return HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST;
        }
        if (mode == 2) {
            return HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED;
        }
        return HwPackageManagerService.transaction_sendLimitedPackageBroadcast;
    }

    private void uploadPcDisplaySizePro() {
        int[] size = getPcDisplaySize();
        if (size.length >= 2) {
            SystemProperties.set("hw.pc.display.width", String.valueOf(size[0]));
            SystemProperties.set("hw.pc.display.height", String.valueOf(size[1]));
        }
    }

    private int[] getPcDisplaySize() {
        DisplayInfo displayInfo = getPcDisplayInfo();
        if (displayInfo == null || displayInfo.getDefaultMode() == null) {
            return new int[0];
        }
        return new int[]{displayInfo.getDefaultMode().getPhysicalWidth(), displayInfo.getDefaultMode().getPhysicalHeight()};
    }

    private DisplayInfo getPcDisplayInfo() {
        Display display = getPcDisplay(this.mContext);
        if (display == null) {
            return null;
        }
        DisplayInfo displayInfo = new DisplayInfo();
        display.getDisplayInfo(displayInfo);
        return displayInfo;
    }

    private Display getPcDisplay(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService("display");
        if (dm != null) {
            Display[] displays = dm.getDisplays();
            if (displays != null && displays.length > 0) {
                int i = displays.length - 1;
                while (i >= 0) {
                    if (displays[i] != null && HwPCUtils.isValidExtDisplayId(displays[i].getDisplayId())) {
                        return displays[i];
                    }
                    i--;
                }
            }
        }
        HwPCUtils.log(TAG, "getPcDisplay not find PCDisplay");
        return null;
    }

    private void setFocusedPCDisplayId(String reason) {
        if (this.mWindowManagerInternal == null) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        int extdisplayid = HwPCUtils.getPCDisplayID();
        if (this.mWindowManagerInternal != null && HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "setFocusedDisplayId extdisplayid = " + extdisplayid);
            this.mWindowManagerInternal.setFocusedDisplayId(extdisplayid, reason);
        }
    }

    public void showImeStatusIcon(int iconResId, String pkgName) {
        try {
            HwPCUtils.log(TAG, String.format("PCMS showImeStatusIcon:%s,%s", new Object[]{Integer.valueOf(iconResId), pkgName}));
            if (validateImeCall(pkgName) && this.mController != null) {
                this.mController.showImeStatusIcon(iconResId, pkgName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "PCMS showImeStatusIcon-RemoteException");
        }
    }

    public void hideImeStatusIcon(String pkgName) {
        try {
            HwPCUtils.log(TAG, "PCMS hideImeStatusIcon");
            if (validateImeCall(pkgName) && this.mController != null) {
                this.mController.hideImeStatusIcon();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "PCMS hideImeStatusIcon-RemoteException");
        }
    }

    private boolean validateImeCall(String pkgName) {
        String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        HwPCUtils.log(TAG, "PCMS callingApp: " + callingApp + ", pkg=" + pkgName);
        if (callingApp == null || !callingApp.equals(pkgName)) {
            return false;
        }
        return true;
    }
}
