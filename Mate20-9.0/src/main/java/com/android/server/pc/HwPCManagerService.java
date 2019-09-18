package com.android.server.pc;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.HwRecentTaskInfo;
import android.app.ITaskStackListener;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackListener;
import android.app.UserSwitchObserver;
import android.bluetooth.BluetoothAdapter;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SurfaceControl;
import android.view.View;
import android.view.Window;
import android.view.WindowManagerPolicyConstants;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.HwInputManagerService;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.pc.HwPCMultiDisplaysManager;
import com.android.server.pc.decision.DecisionUtil;
import com.android.server.pc.vassist.HwPCVAssistCmdExecutor;
import com.android.server.pc.whiltestrategy.WhiteListAppStrategyManager;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowState;
import com.huawei.android.view.HwWindowManager;
import com.huawei.displayengine.IDisplayEngineService;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCManagerService extends IHwPCManager.Stub {
    private static final String ACTION_ALARM_WAKEUP = "com.android.deskclock.ALARM_ALERT";
    private static final String ACTION_CLEAR_LIGHTER_DRAWED = "com.android.server.pc.action.clear_lighter_drawed";
    private static final String ACTION_NOTIFY_CHANGE_STATUS_BAR = "com.android.server.pc.action.CHANGE_STATUS_BAR";
    private static final String ACTION_NOTIFY_DISCONNECT = "com.android.server.pc.action.DISCONNECT";
    private static final String ACTION_NOTIFY_OPEN_EASY_PROJECTION = "com.android.server.pc.action.EASY_PROJECTION";
    private static final String ACTION_NOTIFY_SHOW_MK = "com.android.server.pc.action.SHOW_MK";
    private static final String ACTION_NOTIFY_SWITCH_MODE = "com.android.server.pc.action.SWITCH_MODE";
    private static final String ACTION_NOTIFY_UNINSTALL_APP = "com.android.server.pc.action.UNINSTALL_APP";
    private static final String ACTION_WIFI_DISPLAY_CASTING = "com.huawei.hardware.display.action.WIFI_DISPLAY_CASTING";
    private static final String ALARM_ALERT_CONFLICT = "huawei.deskclock.ALARM_ALERT_CONFLICT";
    private static final int BIT_KEYBOARD = 2;
    private static final int BIT_MOUSE = 1;
    private static final int BIT_NONE = 0;
    private static final int BIT_TOUCHSCREEN = 4;
    private static final String BROADCAST_PERMISSION = "com.huawei.deskclock.broadcast.permission";
    private static final long BROADCAST_SEND_INTERVAL = 500;
    private static final int CHECK_HARD_BROAD_DELAY = 2000;
    private static final int DEVICE_MODE_FREE_MOUSE = 3;
    private static final int DEVICE_MODE_SHOW_DEFAULT = 0;
    private static final int DEVICE_MODE_SHOW_LASERPOINTER = 1;
    private static final int DEVICE_MODE_TEST = 2;
    private static final String DP_LINK_STATE_AUX_FAILED = "AUX_FAILED";
    private static final String DP_LINK_STATE_CABLE_IN = "CABLE_IN";
    private static final String DP_LINK_STATE_CABLE_OUT = "CABLE_OUT";
    private static final String DP_LINK_STATE_EDID_FAILED = "EDID_FAILED";
    private static final String DP_LINK_STATE_HDCP_FAILED = "HDCP_FAILED";
    private static final String DP_LINK_STATE_LINK_FAILED = "LINK_FAILED";
    private static final String DP_LINK_STATE_LINK_RETRAINING = "LINK_RETRAINING";
    private static final String DP_LINK_STATE_MULTI_HPD = "MULTI_HPD";
    private static final String DP_LINK_STATE_SAFE_MODE = "SAFE_MODE";
    private static final int DREAMS_DISENABLED = 0;
    private static final int DREAMS_ENABLED = 1;
    private static final int DREAMS_INVALID = -1;
    private static final String ESHARE_DISPLAY_NAME = "eshare mirror";
    private static final String EXCLUSIVE_DP_LINK = "DEVPATH=/devices/virtual/dp/source";
    private static final String EXCLUSIVE_KEYBOARD = "DEVPATH=/devices/virtual/hwsw_kb/hwkb";
    private static final int EXPLORER_BIND_ERROR = 2;
    private static final int EXPLORER_LAUNCH_DELAY = 4000;
    private static final String EXPLORER_SERVICE_NAME = "HwPCExplorer";
    private static final int FINGERPRINT_SLIDE_OFF = 0;
    private static final int FINGERPRINT_SLIDE_ON = 1;
    private static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    private static final String HELP_MSG_PROMPT_NUMBER = "help-msg-prompt_number";
    private static final int HELP_MSG_PROMPT_NUMBER_DEFAULT = 0;
    private static final int HELP_MSG_PROMPT_NUMBER_MAX = 3;
    private static final int INVALID_ARG = -1;
    private static final int KEEP_RECORD_TIMEOUT = 180000;
    private static final int[] KEYBOARD_PRODUCT_ID = {4817};
    private static final int[] KEYBOARD_VENDOR_ID = {1455};
    private static final String KEY_BEFORE_BOOT_ANIM_TIME = "before_boot_anim_time";
    private static final String KEY_CURRENT_DISPLAY_UNIQUEID = "current_display_uniqueId";
    private static final String KEY_IS_WIRELESS_MODE = "is_wireless_mode";
    private static final int LAUNCH_GUIDE_DELAY = 500;
    private static final int MAX_FREQ = 2472;
    private static final int MIN_FREQ = 2412;
    private static final String MMI_TEST_PROPERTY = "runtime.mmitest.isrunning";
    private static final int MSG_CLEAR_LIGHTER_DRAWED = 24;
    private static final int MSG_CLOSE_CLIENT_TOP_WINDOW = 4;
    private static final int MSG_DISMISS_CLIENT_TASK_VIEW = 6;
    private static final int MSG_DISPLAY_ADDED = 6;
    private static final int MSG_DISPLAY_CHANGED = 7;
    private static final int MSG_DISPLAY_REMOVED = 8;
    private static final int MSG_DP_LINK_ERROR = 22;
    private static final int MSG_DP_STATE_CHANGED = 21;
    private static final int MSG_IME_STATUS_ICON_HIDE = 15;
    private static final int MSG_IME_STATUS_ICON_SHOW = 14;
    private static final int MSG_INPUTMETHOD_SWITCH = 12;
    private static final int MSG_KEEP_RECORD_TIMEOUT = 11;
    private static final int MSG_KEYCODE_APP_SWITCH = 9;
    private static final int MSG_KEYCODE_BACK = 11;
    private static final int MSG_KEYCODE_HOME = 10;
    private static final int MSG_LAUNCH_MK = 13;
    private static final int MSG_LOCK_SCREEN = 8;
    static final int MSG_NOTIFY_SWITCH_PROJ = 1;
    private static final int MSG_OPEN_EASY_PROJECTION = 23;
    private static final int MSG_REFRESH_NTFS = 5;
    private static final int MSG_REGISTER_ALARM = 15;
    private static final int MSG_RELAUNCH_IME = 14;
    private static final int MSG_RESTORE_APP = 10;
    private static final int MSG_SCREENSHOT_PC_DISPLAY = 3;
    private static final int MSG_SET_FOCUS_DISPLAY = 17;
    private static final int MSG_SET_PROJ_MODE = 4;
    private static final int MSG_SHOW_CLIENT_STARTUP_MENU = 2;
    private static final int MSG_SHOW_CLIENT_TASK_VIEW = 5;
    private static final int MSG_SHOW_CLIENT_TOPBAR = 1;
    private static final int MSG_SHOW_ENTER_DESKTOP_MODE = 19;
    private static final int MSG_SHOW_EXIT_DESKTOP_MODE = 20;
    private static final int MSG_SHOW_HELP_TO_PC_MODEL = 26;
    private static final int MSG_START_RESTORE_APPS = 9;
    private static final int MSG_START_VOICE_ASSISTANT = 13;
    private static final int MSG_SWITCH_USER = 12;
    private static final int MSG_TASK_CREATED = 16;
    private static final int MSG_TASK_MOVE_TO_FRONT = 18;
    private static final int MSG_TASK_PROFILE_LOCKED = 19;
    private static final int MSG_TASK_REMOVED = 17;
    private static final int MSG_UNINSTALL_APP = 16;
    private static final int MSG_UPDATE_CFG = 18;
    private static final int MSG_USER_ACTIVITY_ON_DESKTOP = 7;
    private static final int NOTIFY_SWITCH_PROJ_ID = 0;
    private static final int NOTIFY_VIRTUAL_M_K_ID = 1;
    private static final String PERMISSION_BROADCAST_CHANGE_STATUS_BAR = "com.huawei.permission.pc.CHANGE_STATUS_BAR";
    private static final String PERMISSION_BROADCAST_CLEAR_LIGHTER_DRAWED = "com.huawei.permission.pc.CLEAR_LIGHTER_DRAWED";
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
    private static final int SWITCH_STATE_OFF = 0;
    private static final int SWITCH_STATE_ON = 1;
    private static final int SYSTEMUI_BIND_ERROR = 1;
    private static final String SYSTEMUI_SERVICE_NAME = "HwPCSystemUI";
    private static final String TAG = "HwPCManagerService";
    private static int TIME_DISPALY_ADD_BEFORE_BOOT_ANIM = 2000;
    private static final int TIME_NO_DELAY_BEFORE_BOOT_ANIM = 0;
    private static final int TIME_SEND_SWITCHPROJ_MSG_DELAYED = 200;
    private static final int TIME_SWITCH_MODE_BEFORE_BOOT_ANIM = 1500;
    private static final int TIME_UNLOCK_ACTION_BEFORE_BOOT_ANIM = 1500;
    private static final int TYPE_HPPCAT = 1;
    private static final int TYPE_UNKNOWN = -1;
    private static final int TYPE_WELINK = 2;
    private static final String WIRELESS_PROJECTION_STATE = "wireless_projection_state";
    private static final int devicetestmode = 2;
    private final String DEVICE_PROVISIONED_URI = "content://settings/global/device_provisioned";
    private final String SCREEN_OF_TIMEOUT_URI = "content://settings/system/screen_off_timeout";
    private final String USER_SETUP_COMPLETE_URI = "content://settings/secure/user_setup_complete";
    /* access modifiers changed from: private */
    public boolean beginShow = false;
    private boolean isNeedEixtDesktop = false;
    private boolean isNeedEnterDesktop = false;
    /* access modifiers changed from: private */
    public HwActivityManagerService mAMS;
    private final BroadcastReceiver mAlarmClockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwPCUtils.log(HwPCManagerService.TAG, "receive clock alarm");
            HwPCManagerService.this.setScreenPower(true);
        }
    };
    /* access modifiers changed from: private */
    public final AlarmManager.OnAlarmListener mAlarmListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            HwPCManagerService.this.mHandler.sendEmptyMessage(11);
        }
    };
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public float mAxisX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mAxisY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public BluetoothReminderDialog mBluetoothReminderDialog;
    /* access modifiers changed from: private */
    public boolean mBluetoothStateOnEnter = false;
    /* access modifiers changed from: private */
    public Toast mCallingToast;
    /* access modifiers changed from: private */
    public final ServiceConnection mConnExplorer = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceDisconnected");
            HwPCDataReporter.getInstance().reportFailToConnEvent(2, HwPCManagerService.EXPLORER_SERVICE_NAME, HwPCManagerService.this.mPCDisplayInfo);
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.get1stDisplay().mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        HwPCManagerService.this.mContext.bindService(new Intent().setComponent(HwPCManagerService.this.mExplorerComponent), HwPCManagerService.this.mConnExplorer, 1);
                    }
                }
            }, 800);
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnSysUI = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceConnected");
            if (HwPCManagerService.this.get1stDisplay().mDisplayId != -1) {
                HwPCManagerService.this.updateDisplayOverrideConfiguration(HwPCManagerService.this.get1stDisplay().mDisplayId, 2000);
                HwPCManagerService.this.relaunchIMEDelay(2000);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceDisconnected");
            HwPCDataReporter.getInstance().reportFailToConnEvent(1, HwPCManagerService.SYSTEMUI_SERVICE_NAME, HwPCManagerService.this.mPCDisplayInfo);
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.get1stDisplay().mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        HwPCManagerService.this.mContext.bindService(new Intent().setComponent(HwPCManagerService.this.mSystemUIComponent), HwPCManagerService.this.mConnSysUI, 1);
                    }
                }
            }, 800);
        }
    };
    /* access modifiers changed from: private */
    public int mConnectedInputDevices = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public final DisplayDriverCommunicator mDDC;
    private UEventObserver mDPLinkStateObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
            if (event != null) {
                String state = event.get("DP_LINK_EVENT");
                if (!TextUtils.isEmpty(state)) {
                    String tip1 = HwPCManagerService.this.mContext.getResources().getString(33686166);
                    String tip2 = HwPCManagerService.this.mContext.getResources().getString(33686167);
                    String tip = null;
                    char c = 65535;
                    switch (state.hashCode()) {
                        case -1790848990:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_LINK_FAILED)) {
                                c = 6;
                                break;
                            }
                            break;
                        case -203035278:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_LINK_RETRAINING)) {
                                c = 7;
                                break;
                            }
                            break;
                        case 18043426:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_EDID_FAILED)) {
                                c = 5;
                                break;
                            }
                            break;
                        case 324233351:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_CABLE_IN)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 325027384:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_AUX_FAILED)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 615836371:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_HDCP_FAILED)) {
                                c = 8;
                                break;
                            }
                            break;
                        case 1461305356:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_CABLE_OUT)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 1580920854:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_MULTI_HPD)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 1684923157:
                            if (state.equals(HwPCManagerService.DP_LINK_STATE_SAFE_MODE)) {
                                c = 4;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                            boolean unused = HwPCManagerService.this.beginShow = true;
                            break;
                        case 1:
                            HwPCManagerService.this.dismissDpLinkErrorDialog();
                            boolean unused2 = HwPCManagerService.this.beginShow = false;
                            break;
                        case 2:
                        case 3:
                            tip = tip1;
                            break;
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            tip = tip2;
                            break;
                        default:
                            boolean unused3 = HwPCManagerService.this.beginShow = false;
                            break;
                    }
                    if (tip != null && HwPCManagerService.this.beginShow) {
                        HwPCManagerService.this.mHandler.removeMessages(22);
                        HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(22, tip));
                        boolean unused4 = HwPCManagerService.this.beginShow = false;
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public DisplayManager mDisplayManager;
    private int mDreamsEnabledSetting = -1;
    /* access modifiers changed from: private */
    public AlertDialog mEnterDesktopAlertDialog = null;
    /* access modifiers changed from: private */
    public AlertDialog mExitDesktopAlertDialog = null;
    /* access modifiers changed from: private */
    public final ComponentName mExplorerComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.services.ExplorerService");
    /* access modifiers changed from: private */
    public int mFreq;
    final LocalHandler mHandler;
    final ServiceThread mHandlerThread;
    /* access modifiers changed from: private */
    public volatile boolean mHasSwitchNtf = false;
    /* access modifiers changed from: private */
    public final ComponentName mHelpMsgComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.PCModeRecommendActivity");
    private HwPhoneWindowManager mHwPolicy;
    private IBinder mIBinderAudioService;
    /* access modifiers changed from: private */
    public int mIMEWithHardKeyboardState = 1;
    /* access modifiers changed from: private */
    public boolean mIgnoreInjectEvent;
    private final InputManager.InputDeviceListener mInputDeviceListener = new InputManager.InputDeviceListener() {
        public void onInputDeviceAdded(int deviceId) {
            HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceAdded, deviceId:" + deviceId + ", mConnectedInputDevices: " + HwPCManagerService.this.mConnectedInputDevices);
            InputDevice device = InputDevice.getDevice(deviceId);
            HwPCManagerService.access$7076(HwPCManagerService.this, HwPCManagerService.whichInputDevice(device));
            if ((HwPCManagerService.whichInputDevice(device) & 1) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT, "");
            }
            if ((HwPCManagerService.whichInputDevice(device) & 2) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, IDisplayEngineService.DE_ACTION_PG_INPUT_START, "");
            }
            if ((HwPCManagerService.whichInputDevice(device) & 4) != 0) {
                HwPCUtils.mTouchDeviceID = deviceId;
                HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceAdded, mTouchDeviceID = " + HwPCUtils.mTouchDeviceID);
            }
            if (HwPCUtils.enabledInPad()) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("START_TIME", System.currentTimeMillis());
                    if (HwPCManagerService.this.isExclusiveKeyboard(device)) {
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
            int[] devices = InputDevice.getDeviceIds();
            for (int device : devices) {
                connectedInputDevices |= HwPCManagerService.whichInputDevice(InputDevice.getDevice(device));
            }
            if (HwPCUtils.mTouchDeviceID == deviceId) {
                HwPCUtils.mTouchDeviceID = -1;
                HwPCManagerService.this.relaunchIMEIfNecessary();
            }
            int unused = HwPCManagerService.this.mConnectedInputDevices = connectedInputDevices;
            if (HwPCUtils.enabledInPad()) {
                try {
                    if (HwPCManagerService.this.mKeyboardInfo.containsKey(Integer.valueOf(deviceId))) {
                        JSONObject jo = (JSONObject) HwPCManagerService.this.mKeyboardInfo.get(Integer.valueOf(deviceId));
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
    private final ComponentName mInstructionComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.PCHelpInformationActivity");
    private final ComponentName mInstructionComponentWirelessEnabled = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.EasyProjection");
    ArrayList<Intent> mIntentList = new ArrayList<>();
    /* access modifiers changed from: private */
    public boolean mIsDisplayAddedAfterSwitch = false;
    /* access modifiers changed from: private */
    public boolean mIsDisplayLargerThan1080p = false;
    /* access modifiers changed from: private */
    public boolean mIsNeedUnRegisterBluetoothReciver = false;
    private boolean mIsSkipBootAnimation = false;
    private boolean mIsWifiBroadDone = false;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<Integer, JSONObject> mKeyboardInfo = new ConcurrentHashMap<>();
    private final UEventObserver mKeyboardObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
            if (event == null) {
                return;
            }
            if (HwPCManagerService.this.isExclusiveKeyboardConnect(event)) {
                HwPCUtils.log(HwPCManagerService.TAG, "Exclusive Keyboard Connect");
                if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                    HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(19));
                    return;
                }
                return;
            }
            HwPCUtils.log(HwPCManagerService.TAG, "Exclusive Keyboard Disconnect");
            if (HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(20));
            }
        }
    };
    /* access modifiers changed from: private */
    public KeyguardManager mKeyguardManager;
    /* access modifiers changed from: private */
    public int mLockScreenTimeout = 0;
    /* access modifiers changed from: private */
    public Messenger mMessenger = null;
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
                HwPCManagerService.this.collapsePanels();
                HwPCManagerService.this.switchProjMode();
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                HwPCManagerService.this.refreshNotifications();
            } else if (HwPCManagerService.ACTION_NOTIFY_SHOW_MK.equals(action)) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10047, "");
                HwPCManagerService.this.collapsePanels();
                HwPCManagerService.this.sendShowMkMessage();
            } else if (HwPCManagerService.ACTION_NOTIFY_DISCONNECT.equals(action)) {
                if (HwPCManagerService.this.mDisplayManager != null && HwPCManagerService.this.isConnectFromThirdApp(HwPCManagerService.this.get1stDisplay().mDisplayId) == 2) {
                    HwPCManagerService.this.launchWeLink();
                } else if (HwPCManagerService.this.mDisplayManager != null) {
                    HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10048, "");
                    HwPCManagerService.this.mDisplayManager.disconnectWifiDisplay();
                }
                HwPCManagerService.this.collapsePanels();
            } else if (HwPCManagerService.ACTION_NOTIFY_OPEN_EASY_PROJECTION.equals(action)) {
                HwPCManagerService.this.sendOpenEasyProjectionMessage();
                HwPCManagerService.this.collapsePanels();
            } else if (HwPCManagerService.ACTION_NOTIFY_UNINSTALL_APP.equals(action)) {
                String pkgName = intent.getStringExtra("PACKAGE_NAME");
                if (pkgName != null) {
                    HwPCUtils.log(HwPCManagerService.TAG, "ACTION_NOTIFY_UNINSTALL_APP onReceive: " + pkgName);
                    HwPCManagerService.this.sendUninstallAppMessage(pkgName);
                }
            } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(action) && HwPCUtils.getIsWifiMode()) {
                WifiDisplayStatus status = intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS") != null ? (WifiDisplayStatus) intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS") : null;
                if (status != null) {
                    WifiDisplay wifiDisplay = status.getActiveDisplay();
                    if (wifiDisplay != null) {
                        String unused = HwPCManagerService.this.mWireLessDeviceName = String.format(context.getResources().getString(33686156), new Object[]{wifiDisplay.getDeviceName()});
                    } else {
                        HwPCUtils.log(HwPCManagerService.TAG, "wifiDisplay is null");
                    }
                } else {
                    HwPCUtils.log(HwPCManagerService.TAG, "status is null");
                }
            } else if (DecisionUtil.PC_TARGET_ACTION.equals(action)) {
                DecisionUtil.showPCRecommendDialog(HwPCManagerService.this.mContext);
            }
        }
    };
    private int mPCBeforeBootAnimTime = 2000;
    /* access modifiers changed from: private */
    public DisplayInfo mPCDisplayInfo = null;
    private HwPCMkManager mPCMkManager;
    PCSettingsObserver mPCSettingsObserver;
    /* access modifiers changed from: private */
    public int mPadDesktopModeLockScreenTimeout = 0;
    /* access modifiers changed from: private */
    public int mPadLockScreenTimeout = 0;
    /* access modifiers changed from: private */
    public boolean mPadPCDisplayIsRemoved = false;
    /* access modifiers changed from: private */
    public HwPCMultiDisplaysManager mPcMultiDisplayMgr;
    private int mPhoneState = 0;
    WindowManagerPolicyConstants.PointerEventListener mPointerListener = new WindowManagerPolicyConstants.PointerEventListener() {
        public void onPointerEvent(MotionEvent motionEvent, int displayId) {
            if (HwPCUtils.isValidExtDisplayId(displayId) && motionEvent.getAction() == 8) {
                HwPCManagerService.this.filterScrollForPCMode();
            }
            onPointerEvent(motionEvent);
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent != null) {
                float unused = HwPCManagerService.this.mAxisX = motionEvent.getX();
                float unused2 = HwPCManagerService.this.mAxisY = motionEvent.getY();
            }
        }
    };
    private long mPrevTimeForBroadcast = SystemClock.uptimeMillis();
    volatile HwPCUtils.ProjectionMode mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
    boolean mProvisioned = false;
    boolean mRestartAppsWhenUnlocked = false;
    private int mRotationSwitch = 1;
    private int mRotationValue = 0;
    private Object mScreenAccessLock = new Object();
    private boolean mScreenPowerOn = true;
    private AlertDialog mShowDpLinkErrorTipDialog = null;
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String act = intent.getAction();
                if (act != null && "android.intent.action.ACTION_SHUTDOWN".equals(act) && HwPCUtils.isPcCastModeInServer()) {
                    HwPCManagerService.this.restoreRotationInPad();
                    if (HwPCUtils.enabledInPad()) {
                        HwPCUtils.log(HwPCManagerService.TAG, "receive shut down broadcast , IME With Hard Keyboard State:" + HwPCManagerService.this.mIMEWithHardKeyboardState);
                        Settings.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", HwPCManagerService.this.mIMEWithHardKeyboardState);
                    }
                    HwPCManagerService.this.restoreDreamSettingInPad();
                }
            }
        }
    };
    private boolean mSupportOverlay = SystemProperties.getBoolean("hw_pc_support_overlay", false);
    private boolean mSupportTouchPad = SystemProperties.getBoolean("hw_pc_support_touchpad", true);
    /* access modifiers changed from: private */
    public final ComponentName mSystemUIComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.SystemUIService");
    private TaskStackListener mTaskStackListener = new TaskStackListener() {
        public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
            HwPCManagerService.super.onTaskSnapshotChanged(taskId, snapshot);
        }

        public void onTaskStackChanged() throws RemoteException {
            HwPCManagerService.super.onTaskStackChanged();
        }

        public void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException {
            HwPCManagerService.super.onActivityPinned(packageName, userId, taskId, stackId);
        }

        public void onActivityUnpinned() throws RemoteException {
            HwPCManagerService.super.onActivityUnpinned();
        }

        public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
            HwPCManagerService.super.onPinnedActivityRestartAttempt(clearedTask);
        }

        public void onPinnedStackAnimationStarted() throws RemoteException {
            HwPCManagerService.super.onPinnedStackAnimationStarted();
        }

        public void onPinnedStackAnimationEnded() throws RemoteException {
            HwPCManagerService.super.onPinnedStackAnimationEnded();
        }

        public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
            HwPCManagerService.super.onActivityForcedResizable(packageName, taskId, reason);
        }

        public void onActivityDismissingDockedStack() throws RemoteException {
            HwPCManagerService.super.onActivityDismissingDockedStack();
        }

        public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
            HwPCManagerService.super.onTaskCreated(taskId, componentName);
            try {
                if (HwPCManagerService.this.mMessenger != null) {
                    Message message = Message.obtain();
                    message.what = 16;
                    message.obj = componentName;
                    message.arg1 = taskId;
                    HwPCManagerService.this.mMessenger.send(message);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(HwPCManagerService.TAG, "onTaskCreated RemoteException");
            }
        }

        public void onTaskRemoved(int taskId) throws RemoteException {
            HwPCManagerService.super.onTaskRemoved(taskId);
            try {
                if (HwPCManagerService.this.mMessenger != null) {
                    Message message = Message.obtain();
                    message.what = 17;
                    message.arg1 = taskId;
                    HwPCManagerService.this.mMessenger.send(message);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(HwPCManagerService.TAG, "onTaskRemoved RemoteException");
            }
        }

        public void onTaskMovedToFront(int taskId) throws RemoteException {
            HwPCManagerService.super.onTaskMovedToFront(taskId);
            try {
                if (HwPCManagerService.this.mMessenger != null) {
                    Message message = Message.obtain();
                    message.what = 18;
                    message.arg1 = taskId;
                    HwPCManagerService.this.mMessenger.send(message);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(HwPCManagerService.TAG, "onTaskMovedToFront RemoteException");
            }
        }

        public void onTaskRemovalStarted(int taskId) throws RemoteException {
            HwPCManagerService.super.onTaskRemovalStarted(taskId);
        }

        public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
            HwPCManagerService.super.onTaskDescriptionChanged(taskId, td);
        }

        public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
            HwPCManagerService.super.onActivityRequestedOrientationChanged(taskId, requestedOrientation);
        }

        public void onTaskProfileLocked(int taskId, int userId) throws RemoteException {
            HwPCManagerService.super.onTaskProfileLocked(taskId, userId);
            try {
                if (HwPCManagerService.this.mMessenger != null) {
                    Message message = Message.obtain();
                    message.what = 19;
                    message.arg1 = taskId;
                    message.arg2 = userId;
                    HwPCManagerService.this.mMessenger.send(message);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(HwPCManagerService.TAG, "onTaskProfileLocked RemoteException");
            }
        }
    };
    private TelephonyManager mTelephonyPhone;
    int mTmpDisplayId2Unlocked;
    private final ComponentName mTouchPadComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.mk.activity.ImitateActivity");
    Handler mUIHandler = new Handler(UiThread.get().getLooper());
    private UnlockScreenReceiver mUnlockScreenReceiver;
    /* access modifiers changed from: private */
    public int mUserId = 0;
    UserManagerInternal mUserManagerInternal;
    boolean mUserSetupComplete = false;
    /* access modifiers changed from: private */
    public HwPCVAssistCmdExecutor mVAssistCmdExecutor;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pManager mWifiP2pManager;
    /* access modifiers changed from: private */
    public BroadcastReceiver mWifiPCReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && action.equals("android.bluetooth.adapter.action.STATE_CHANGED") && intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0) == 12 && HwPCUtils.getIsWifiMode()) {
                    HwPCManagerService.this.mBluetoothReminderDialog.showCloseBluetoothTip(HwPCManagerService.this.mContext);
                }
            }
        }
    };
    private WindowManagerInternal mWindowManagerInternal;
    /* access modifiers changed from: private */
    public String mWireLessDeviceName = "";
    /* access modifiers changed from: private */
    public boolean restartByUnlock2SetAnimTime;

    private class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean forScroll = true;
            if (i == 1) {
                HwPCManagerService.this.sendNotificationForSwitch((HwPCUtils.ProjectionMode) msg.obj);
            } else if (i != 26) {
                switch (i) {
                    case 4:
                        if (!HwPCUtils.enabledInPad() || !HwPCManagerService.this.isMonkeyRunning()) {
                            if (HwPCManagerService.this.get1stDisplay().mDisplayId != -1) {
                                boolean unused = HwPCManagerService.this.mIsDisplayAddedAfterSwitch = false;
                                if (HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                                    HwPCManagerService.this.lightPhoneScreen();
                                    HwPCUtils.log(HwPCManagerService.TAG, "The current mode is DesktopMode, get1stDisplay().mType = " + HwPCManagerService.this.get1stDisplay().mType);
                                    HwPCUtils.setPhoneDisplayID(HwPCManagerService.this.get1stDisplay().mDisplayId);
                                    HwPCManagerService.this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                                    HwPCUtils.setPcCastModeInServerEarly(HwPCManagerService.this.mProjMode);
                                    if (HwPCManagerService.this.get1stDisplay().mType == 2) {
                                        HwPCManagerService.this.mDDC.resetProjectionMode();
                                    }
                                    if (!HwPCUtils.enabledInPad()) {
                                        HwPCManagerService.this.bindUnbindService(false);
                                    }
                                    if (HwPCUtils.enabledInPad()) {
                                        HwPCManagerService.this.bindUnbindService(false);
                                        DisplayManagerInternal dm = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
                                        if (dm != null) {
                                            dm.pcDisplayChange(false);
                                            boolean unused2 = HwPCManagerService.this.mPadPCDisplayIsRemoved = true;
                                            dm.pcDisplayChange(true);
                                        }
                                    }
                                    HwPCManagerService.this.mAMS.togglePCMode(HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode), HwPCManagerService.this.get1stDisplay().mDisplayId);
                                    HwPCManagerService.this.setUsePCModeMouseIconContext(false);
                                    HwPCUtils.setPcCastModeInServer(false);
                                    boolean unused3 = HwPCManagerService.this.mIgnoreInjectEvent = false;
                                    HwPCManagerService.this.setPcCastingDisplayId(-1);
                                    HwPCManagerService.this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                                    HwPCManagerService.this.updateIMEWithHardKeyboardState(false);
                                    Settings.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "selected-proj-mode", 0);
                                    HwPCManagerService.this.restoreDreamSettingInPad();
                                    HwPCManagerService.this.sendNotificationForSwitch(HwPCManagerService.this.mProjMode);
                                    HwPCManagerService.this.bdReportDiffSrcStatus(false);
                                    HwPCManagerService.this.bdReportSameSrcStatus(true);
                                    HwPCManagerService.this.sendSwitchToStatusBar();
                                    HwPCManagerService.this.setDesktopModeToAudioService(0);
                                    HwPCManagerService.this.updateFingerprintSlideSwitch();
                                    HwPCManagerService.this.relaunchIMEDelay(0);
                                    HwPCManagerService.this.exitDesktopModeForMk();
                                    HwPCManagerService.this.backToHomeInDefaultDisplay(HwPCManagerService.this.get1stDisplay().mDisplayId);
                                    break;
                                } else {
                                    HwPCUtils.setPCDisplayID(HwPCManagerService.this.get1stDisplay().mDisplayId);
                                    HwPCUtils.setPhoneDisplayID(-1);
                                    HwPCManagerService.this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
                                    HwPCManagerService.this.mDDC.enableProjectionMode();
                                    if (HwPCManagerService.this.mIsDisplayLargerThan1080p && HwPCManagerService.this.get1stDisplay().mType == 2) {
                                        Settings.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "selected-proj-mode", 1);
                                        break;
                                    } else {
                                        HwPCManagerService.this.handleSwitchToDesktopMode();
                                        break;
                                    }
                                }
                            } else {
                                return;
                            }
                        } else {
                            HwPCUtils.log(HwPCManagerService.TAG, "MSG_SET_PROJ_MODE isMonkeyRunning return !");
                            return;
                        }
                        break;
                    case 5:
                        if (HwPCManagerService.this.mHasSwitchNtf) {
                            HwPCManagerService.this.sendNotificationForSwitch(HwPCManagerService.this.mProjMode);
                            break;
                        }
                        break;
                    case 6:
                        if (HwPCManagerService.this.mKeyguardManager == null) {
                            KeyguardManager unused4 = HwPCManagerService.this.mKeyguardManager = (KeyguardManager) HwPCManagerService.this.mContext.getSystemService("keyguard");
                        }
                        if (!HwPCManagerService.this.mKeyguardManager.isKeyguardLocked() && StorageManager.isUserKeyUnlocked(HwPCManagerService.this.mUserId)) {
                            HwPCManagerService.this.onDisplayAdded(msg.arg1);
                            break;
                        } else {
                            HwPCManagerService.this.mTmpDisplayId2Unlocked = msg.arg1;
                            HwPCManagerService.this.mRestartAppsWhenUnlocked = true;
                            break;
                        }
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
                        if (HwPCManagerService.this.getCastMode()) {
                            HwPCManagerService.this.restoreApp(msg.arg1, (Intent) msg.obj);
                            break;
                        } else {
                            removeMessages(10);
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
                        String pkgName = (String) msg.obj;
                        HwPCUtils.log(HwPCManagerService.TAG, "handleMessage MSG_UNINSTALL_APP:" + pkgName);
                        if (pkgName != null) {
                            HwPCManagerService.this.mContext.getPackageManager().deletePackage(pkgName, null, 2);
                            break;
                        }
                        break;
                    case 17:
                        HwPCManagerService.this.setFocusedPCDisplayId("unlockScreen");
                        break;
                    case 18:
                        HwPCManagerService.this.doUpdateDisplayOverrideConfiguration(msg.arg1);
                        break;
                    case 19:
                        HwPCManagerService.this.showEnterDesktopAlertDialog(HwPCManagerService.this.getCurrentContext(), true);
                        break;
                    case 20:
                        HwPCManagerService.this.showExitDesktopAlertDialog(HwPCManagerService.this.getCurrentContext(), true);
                        break;
                    case 21:
                        HwPCManagerService.this.mPcMultiDisplayMgr.notifyDpState(((Boolean) msg.obj).booleanValue());
                        break;
                    case 22:
                        HwPCManagerService.this.showDPLinkErrorDialog(HwPCManagerService.this.mContext, (String) msg.obj);
                        break;
                    case 23:
                        HwPCManagerService.this.openEasyProjection();
                        break;
                    case 24:
                        KeyEvent ev = (KeyEvent) msg.obj;
                        if (msg.arg1 == 0) {
                            forScroll = false;
                        }
                        if (HwPCManagerService.this.shouldSendBroadcastForClearLighterDrawed(ev, forScroll)) {
                            HwPCManagerService.this.sendBroadcastForClearLighterDrawed();
                            break;
                        }
                        break;
                }
            } else {
                Intent intent = new Intent();
                intent.setAction("com.huawei.filemanager.desktopinstruction.PCModeRecommendActivity");
                intent.setComponent(HwPCManagerService.this.mHelpMsgComponent);
                intent.addFlags(268435456);
                ActivityOptions activityOptions = ActivityOptions.makeBasic();
                activityOptions.setLaunchDisplayId(0);
                try {
                    HwPCManagerService.this.mContext.startActivity(intent, activityOptions.toBundle());
                } catch (ActivityNotFoundException e) {
                    HwPCUtils.log(HwPCManagerService.TAG, "fail to start activity");
                }
            }
        }
    }

    class PCSettingsObserver extends ContentObserver {
        PCSettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            if (HwPCUtils.enabledInPad()) {
                resolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, this, 0);
                updateScreenOffTimeoutSettings();
            }
            resolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this, 0);
            resolver.registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                HwPCUtils.log(HwPCManagerService.TAG, "PCSettingsObserver onChange:" + selfChange + " uri:" + uri.toString());
                String uri2 = uri.toString();
                char c = 65535;
                int hashCode = uri2.hashCode();
                if (hashCode != -1333899149) {
                    if (hashCode != -303652254) {
                        if (hashCode == 1024070412 && uri2.equals("content://settings/global/device_provisioned")) {
                            c = 1;
                        }
                    } else if (uri2.equals("content://settings/secure/user_setup_complete")) {
                        c = 2;
                    }
                } else if (uri2.equals("content://settings/system/screen_off_timeout")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        updateScreenOffTimeoutSettings();
                        return;
                    case 1:
                    case 2:
                        deviceChanged();
                        return;
                    default:
                        return;
                }
            }
        }

        private synchronized void updateScreenOffTimeoutSettings() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            int unused = HwPCManagerService.this.mLockScreenTimeout = Settings.System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            if (HwPCUtils.enabledInPad()) {
                if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) || !HwPCUtils.isPcCastModeInServer()) {
                    int unused2 = HwPCManagerService.this.mPadLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                    Settings.Secure.putInt(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout);
                    HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadLockScreenTimeout);
                } else {
                    int unused3 = HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                    Settings.Secure.putInt(resolver, "pad_desktop_mode_screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                    HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_DESKTOP_MODE_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                }
            }
            HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings " + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
        }

        /* access modifiers changed from: package-private */
        public synchronized void readScreenOffSettings() {
            if (HwPCUtils.enabledInPad()) {
                ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                int unused = HwPCManagerService.this.mLockScreenTimeout = Settings.System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
                int unused2 = HwPCManagerService.this.mPadLockScreenTimeout = Settings.Secure.getIntForUser(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mLockScreenTimeout, -2);
                int unused3 = HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = Settings.Secure.getIntForUser(resolver, "pad_desktop_mode_screen_off_timeout", 600000, -2);
                HwPCUtils.log(HwPCManagerService.TAG, "read screen off settings current=" + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void restoreScreenOffSettings() {
            if (HwPCUtils.enabledInPad()) {
                ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) || !HwPCUtils.isPcCastModeInServer()) {
                    Settings.System.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadLockScreenTimeout=" + HwPCManagerService.this.mPadLockScreenTimeout);
                } else {
                    Settings.System.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadDesktopModeLockScreenTimeout=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                }
            }
        }

        private void deviceChanged() {
            boolean wasProvisioned = HwPCManagerService.this.mProvisioned;
            boolean wasUserSetupComplete = HwPCManagerService.this.mUserSetupComplete;
            boolean isProvisioned = HwPCManagerService.this.deviceIsProvisioned();
            boolean isUserSetupComplete = HwPCManagerService.this.isUserSetupComplete();
            HwPCManagerService.this.mProvisioned = isProvisioned;
            HwPCManagerService.this.mUserSetupComplete = isUserSetupComplete;
            if ((isProvisioned && !wasProvisioned) || (isUserSetupComplete && !wasUserSetupComplete)) {
                int displayId = -1;
                DisplayManager unused = HwPCManagerService.this.mDisplayManager = (DisplayManager) HwPCManagerService.this.mContext.getSystemService("display");
                if (HwPCManagerService.this.mDisplayManager != null) {
                    Display[] displays = HwPCManagerService.this.mDisplayManager.getDisplays();
                    if (displays != null && displays.length > 0) {
                        int i = displays.length - 1;
                        while (true) {
                            if (i >= 0) {
                                if (displays[i] != null && displays[i].getDisplayId() != 0 && HwPCManagerService.this.isWiredDisplay(displays[i].getDisplayId())) {
                                    displayId = displays[i].getDisplayId();
                                    break;
                                }
                                i--;
                            } else {
                                break;
                            }
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
                    boolean unused = HwPCManagerService.this.restartByUnlock2SetAnimTime = true;
                    HwPCManagerService.this.scheduleDisplayAdded(HwPCManagerService.this.mTmpDisplayId2Unlocked);
                } else {
                    HwPCUtils.log(HwPCManagerService.TAG, "receive: ACTION_USER_PRESENT, MSG_SET_FOCUS_DISPLAY");
                    HwPCManagerService.this.mHandler.removeMessages(17);
                    HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(17));
                }
            }
        }
    }

    static /* synthetic */ int access$7076(HwPCManagerService x0, int x1) {
        int i = x0.mConnectedInputDevices | x1;
        x0.mConnectedInputDevices = i;
        return i;
    }

    /* access modifiers changed from: private */
    public static boolean isDesktopMode(HwPCUtils.ProjectionMode mode) {
        return mode == HwPCUtils.ProjectionMode.DESKTOP_MODE;
    }

    /* access modifiers changed from: private */
    public Context getCurrentContext() {
        if (!isDesktopMode(this.mProjMode)) {
            return this.mContext;
        }
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        if (this.mDisplayManager != null) {
            Display[] displays = this.mDisplayManager.getDisplays();
            Display display = null;
            int length = displays.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Display dis = displays[i];
                if (dis.getType() != 1 && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                    display = dis;
                    break;
                }
                i++;
            }
            if (display != null) {
                return this.mContext.createDisplayContext(display);
            }
        }
        return null;
    }

    private boolean isEnterDesktopModeRemembered() {
        int isRemembered = Settings.Secure.getInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 0);
        Log.d(TAG, "isEnterDesktopModeRemembered" + isRemembered);
        return isRemembered == 1;
    }

    private boolean isExitDesktopModeRemembered() {
        int isRemembered = Settings.Secure.getInt(this.mContext.getContentResolver(), "exit-desktop-mode-remember", 0);
        Log.d(TAG, "isExitDesktopModeRemembered" + isRemembered);
        return isRemembered == 1;
    }

    private boolean isShowEnterDialog() {
        int isRemembered = Settings.Secure.getInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 0);
        Log.d(TAG, "isShowEnterDialog" + isRemembered);
        if (isRemembered == 0) {
            return true;
        }
        return false;
    }

    private boolean isShowExitDialog() {
        int isRemembered = Settings.Secure.getInt(this.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 0);
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
        }, start, re.length() + start, 33);
        return spannableString;
    }

    private boolean isMMIRunning() {
        return SystemProperties.get(MMI_TEST_PROPERTY, "false").equals("true");
    }

    /* access modifiers changed from: private */
    public void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        showEnterDesktopAlertDialog(context, isExclusiveKeyboard, false);
    }

    private boolean isKeyguardLocked() {
        if (this.mKeyguardManager != null) {
            return this.mKeyguardManager.isKeyguardLocked();
        }
        return false;
    }

    private void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard, boolean notDisplayAdd) {
        String content;
        Context context2 = context;
        boolean z = isExclusiveKeyboard;
        if (isMMIRunning() || isKeyguardLocked()) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed!");
            HwPCDataReporter.getInstance().reportFailEnterPadEvent(1, this.mPCDisplayInfo);
        } else if (!deviceIsProvisioned() || !isUserSetupComplete()) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed!  Startup Guide is not Complete");
            HwPCDataReporter.getInstance().reportFailEnterPadEvent(2, this.mPCDisplayInfo);
        } else {
            int i = 0;
            if (isCalling()) {
                if (isDesktopMode(this.mProjMode)) {
                    i = get1stDisplay().mDisplayId;
                }
                showCallingToast(i);
                HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
                HwPCDataReporter.getInstance().reportFailEnterPadEvent(3, this.mPCDisplayInfo);
            } else if (this.mUserId != 0) {
                HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed! currentUser is not UserHandle.USER_OWNER");
            } else if (context2 != null && (this.mEnterDesktopAlertDialog == null || !this.mEnterDesktopAlertDialog.isShowing())) {
                if (isEnterDesktopModeRemembered() && !isDesktopMode(this.mProjMode) && !z) {
                    backToHomeInDefaultDisplay(get1stDisplay().mDisplayId);
                    sendSwitchMsgDelayed(200);
                } else if (!z || isShowEnterDialog()) {
                    AlertDialog.Builder buider = new AlertDialog.Builder(context2, 33947691);
                    View view = LayoutInflater.from(buider.getContext()).inflate(34013374, null);
                    if (view == null) {
                        HwPCDataReporter.getInstance().reportFailEnterPadEvent(4, this.mPCDisplayInfo);
                        return;
                    }
                    ImageView imageView = (ImageView) view.findViewById(34603415);
                    TextView textView = (TextView) view.findViewById(34603414);
                    CheckBox checkBox = (CheckBox) view.findViewById(34603416);
                    if (imageView != null && textView != null && checkBox != null) {
                        if (z) {
                            imageView.setPadding(0, 0, 0, 0);
                            content = this.mContext.getResources().getString(33686075);
                        } else {
                            imageView.setImageResource(33752046);
                            content = this.mContext.getResources().getString(33686073);
                        }
                        textView.setText(getSpanString(content, this.mContext.getResources().getString(33686077), ""));
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        checkBox.setOnCheckedChangeListener($$Lambda$HwPCManagerService$O9bKuYfIo9MANf97DdpWE4QaTDs.INSTANCE);
                        this.mEnterDesktopAlertDialog = buider.setTitle(33686079).setPositiveButton(33686078, new DialogInterface.OnClickListener(checkBox, z) {
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.lambda$showEnterDesktopAlertDialog$1(HwPCManagerService.this, this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setNegativeButton(33686072, new DialogInterface.OnClickListener(checkBox, z) {
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.lambda$showEnterDesktopAlertDialog$2(HwPCManagerService.this, this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setView(view).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public final void onDismiss(DialogInterface dialogInterface) {
                                HwPCManagerService.lambda$showEnterDesktopAlertDialog$3(HwPCManagerService.this, dialogInterface);
                            }
                        }).create();
                        this.mEnterDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mEnterDesktopAlertDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
                        this.mEnterDesktopAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
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
                            View titleView = w.findViewById(16908721);
                            boolean isTalkbackOpen = Settings.Secure.getInt(contentResolver, "accessibility_enabled", 0) > 0;
                            if (titleView != null && !titleView.isAccessibilityFocused() && z && notDisplayAdd && isTalkbackOpen) {
                                titleView.requestAccessibilityFocus();
                            }
                        }
                    }
                }
            }
        }
    }

    static /* synthetic */ void lambda$showEnterDesktopAlertDialog$0(CompoundButton buttonView, boolean isChecked) {
    }

    public static /* synthetic */ void lambda$showEnterDesktopAlertDialog$1(HwPCManagerService hwPCManagerService, CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        hwPCManagerService.backToHomeInDefaultDisplay(hwPCManagerService.get1stDisplay().mDisplayId);
        hwPCManagerService.isNeedEnterDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 1);
            } else {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "enter-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    public static /* synthetic */ void lambda$showEnterDesktopAlertDialog$2(HwPCManagerService hwPCManagerService, CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:cancel which=" + which);
        hwPCManagerService.isNeedEnterDesktop = false;
        if (checkBox.isChecked()) {
            if (isExclusiveKeyboard) {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 1);
            } else {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "enter-desktop-mode-remember", 1);
            }
        }
        dialog.cancel();
    }

    public static /* synthetic */ void lambda$showEnterDesktopAlertDialog$3(HwPCManagerService hwPCManagerService, DialogInterface dialog) {
        if (hwPCManagerService.isNeedEnterDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            hwPCManagerService.isNeedEnterDesktop = false;
            hwPCManagerService.sendSwitchMsgDelayed(200);
        }
    }

    private void sendSwitchMsgDelayed(int delayMillis) {
        this.mHandler.removeMessages(4);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), (long) delayMillis);
    }

    /* access modifiers changed from: private */
    public void showExitDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        if (isMMIRunning() || isKeyguardLocked()) {
            HwPCUtils.log(TAG, "showExitDesktopAlertDialog failed!");
            return;
        }
        int i = 0;
        if (isCalling()) {
            if (isDesktopMode(this.mProjMode)) {
                i = get1stDisplay().mDisplayId;
            }
            showCallingToast(i);
            HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
        } else if (context != null && (this.mExitDesktopAlertDialog == null || !this.mExitDesktopAlertDialog.isShowing())) {
            if (isExitDesktopModeRemembered() && isDesktopMode(this.mProjMode) && !isExclusiveKeyboard) {
                sendSwitchMsg();
            } else if (!isExclusiveKeyboard || isShowExitDialog()) {
                AlertDialog.Builder buider = new AlertDialog.Builder(context, 33947691);
                View view = LayoutInflater.from(buider.getContext()).inflate(34013374, null);
                if (view != null) {
                    String enter_toast = context.getString(33686080);
                    String exit_toast = context.getString(33686088);
                    String enter_exclusive_keyboard = context.getString(33686074);
                    String exit_exclusive_keyboard = context.getString(33686083);
                    HwPCUtils.log(TAG, "these string will be used in future:" + enter_toast + exit_toast + enter_exclusive_keyboard + exit_exclusive_keyboard);
                    ImageView imageView = (ImageView) view.findViewById(34603415);
                    TextView textView = (TextView) view.findViewById(34603414);
                    CheckBox checkBox = (CheckBox) view.findViewById(34603416);
                    if (imageView != null && textView != null && checkBox != null) {
                        imageView.setPadding(0, 0, 0, 0);
                        if (isExclusiveKeyboard) {
                            textView.setText(33686084);
                        } else {
                            textView.setText(33686082);
                        }
                        checkBox.setOnCheckedChangeListener($$Lambda$HwPCManagerService$cbPBgxGRcbQ3qc0VOMSqqDsH6w0.INSTANCE);
                        this.mExitDesktopAlertDialog = buider.setTitle(33686087).setPositiveButton(33686086, new DialogInterface.OnClickListener(checkBox, isExclusiveKeyboard) {
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.lambda$showExitDesktopAlertDialog$5(HwPCManagerService.this, this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setNegativeButton(33686081, new DialogInterface.OnClickListener() {
                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.lambda$showExitDesktopAlertDialog$6(HwPCManagerService.this, dialogInterface, i);
                            }
                        }).setView(view).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public final void onDismiss(DialogInterface dialogInterface) {
                                HwPCManagerService.lambda$showExitDesktopAlertDialog$7(HwPCManagerService.this, dialogInterface);
                            }
                        }).create();
                        this.mExitDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mExitDesktopAlertDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
                        this.mExitDesktopAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
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

    static /* synthetic */ void lambda$showExitDesktopAlertDialog$4(CompoundButton buttonView, boolean isChecked) {
    }

    public static /* synthetic */ void lambda$showExitDesktopAlertDialog$5(HwPCManagerService hwPCManagerService, CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        hwPCManagerService.isNeedEixtDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 1);
            } else {
                Settings.Secure.putInt(hwPCManagerService.mContext.getContentResolver(), "exit-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    public static /* synthetic */ void lambda$showExitDesktopAlertDialog$6(HwPCManagerService hwPCManagerService, DialogInterface dialog, int which) {
        hwPCManagerService.isNeedEixtDesktop = false;
        Log.d(TAG, "onClick:cancel which=" + which);
        dialog.cancel();
    }

    public static /* synthetic */ void lambda$showExitDesktopAlertDialog$7(HwPCManagerService hwPCManagerService, DialogInterface dialog) {
        if (hwPCManagerService.isNeedEixtDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            hwPCManagerService.isNeedEixtDesktop = false;
            hwPCManagerService.sendSwitchMsgDelayed(200);
        }
    }

    /* access modifiers changed from: private */
    public void handleSwitchToDesktopMode() {
        HwPCUtils.log(TAG, "handleSwitchToDesktopMode, mIsDisplayAddedAfterSwitch = " + this.mIsDisplayAddedAfterSwitch);
        if (this.mIsDisplayAddedAfterSwitch) {
            HwPCDataReporter.getInstance().reportFailSwitchEvent(1, this.mProjMode.ordinal(), this.mPCDisplayInfo);
            return;
        }
        HwPCUtils.setPCDisplayID(get1stDisplay().mDisplayId);
        HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
        HwPCUtils.setPcCastModeInServer(true);
        setPcCastingDisplayId(get1stDisplay().mDisplayId);
        autoLaunchMK();
        this.mVAssistCmdExecutor.notifyDesktopModeChanged(true, get1stDisplay().mDisplayId);
        updateIMEWithHardKeyboardState(true);
        saveRotationInPad();
        this.mAMS.freezeOrThawRotationInPcMode();
        saveDreamSettingInPad();
        this.mPCBeforeBootAnimTime = 0;
        bindUnbindService(true);
        this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
        setUsePCModeMouseIconContext(true);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
        sendNotificationForSwitch(this.mProjMode);
        bdReportDiffSrcStatus(true);
        bdReportSameSrcStatus(false);
        scheduleRestoreApps(get1stDisplay().mDisplayId);
        sendSwitchToStatusBar();
        setDesktopModeToAudioService(1);
        uploadPcDisplaySizePro();
        enableFingerprintSlideSwitch();
        lightPhoneScreen();
        if (HwPCUtils.enabledInPad()) {
            setFocusedPCDisplayId("enterDesktop");
        }
        relaunchIMEDelay(2000);
        enterDesktopModeForMk();
    }

    /* access modifiers changed from: package-private */
    public UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    public void scheduleDisplayAdded(int displayId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "scheduleDisplayAdded checkCallingPermission failed" + Binder.getCallingPid());
        } else if (HwFrameworkFactory.getVRSystemServiceManager().isVRDeviceConnected()) {
            HwPCUtils.log(TAG, "vr mode should not add vr display");
        } else {
            this.mHandler.removeMessages(6);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(6, displayId, -1));
        }
    }

    public void scheduleDisplayChanged(int displayId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "scheduleDisplayChanged checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mHandler.removeMessages(7);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, displayId, -1));
    }

    public void scheduleDisplayRemoved(int displayId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "scheduleDisplayRemoved checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mHandler.removeMessages(8);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(8, displayId, -1));
    }

    /* access modifiers changed from: private */
    public void bindUnbindService(boolean bind) {
        HwPCUtils.log(TAG, "bindUnbindService:" + bind + " current display is:" + get1stDisplay().mDisplayId);
        this.mPCSettingsObserver.restoreScreenOffSettings();
        if (bind) {
            Intent intent = new Intent();
            intent.putExtra(KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
            intent.setComponent(this.mSystemUIComponent);
            this.mContext.bindService(intent, this.mConnSysUI, 1);
            Intent intent2 = new Intent();
            intent2.putExtra(KEY_BEFORE_BOOT_ANIM_TIME, this.mPCBeforeBootAnimTime);
            intent2.putExtra(KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
            intent2.putExtra(KEY_CURRENT_DISPLAY_UNIQUEID, getPcDisplayInfo() != null ? getPcDisplayInfo().uniqueId : "");
            intent2.setComponent(this.mExplorerComponent);
            this.mContext.bindService(intent2, this.mConnExplorer, 1);
            registerScreenOnEvent();
            registerShutdownEvent();
            if (HwPCUtils.getIsWifiMode()) {
                registerBluetoothReceiver();
            }
            this.mAMS.registerHwTaskStackListener(this.mTaskStackListener);
            return;
        }
        unbindAllPcService();
        unRegisterScreenOnEvent();
        restoreRotationInPad();
        unRegisterShutdownEvent();
        if (this.mIsNeedUnRegisterBluetoothReciver) {
            unRegisterBluetoothReceiver();
        }
        this.mAMS.unRegisterHwTaskStackListener(this.mTaskStackListener);
    }

    private void saveRotationInPad() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mRotationSwitch = Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                if (this.mRotationSwitch == 0) {
                    this.mRotationValue = Settings.System.getIntForUser(this.mContext.getContentResolver(), "user_rotation", 0, this.mUserId);
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveRotationInPad " + e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public void restoreRotationInPad() {
        if (HwPCUtils.enabledInPad()) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mRotationSwitch == 0) {
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "user_rotation", this.mRotationValue, this.mUserId);
                } else {
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreRotationInPad " + e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
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
                int DreamsSetting = Settings.Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (DreamsSetting == 1) {
                    Settings.Secure.putIntForUser(resolver, "screensaver_enabled", 0, this.mUserId);
                    this.mDreamsEnabledSetting = DreamsSetting;
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveDreamSettingInPad " + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void restoreDreamSettingInPad() {
        if (HwPCUtils.enabledInPad()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            try {
                int DreamsSetting = Settings.Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (this.mDreamsEnabledSetting == 1 && this.mDreamsEnabledSetting != DreamsSetting) {
                    Settings.Secure.putIntForUser(resolver, "screensaver_enabled", this.mDreamsEnabledSetting, this.mUserId);
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
        Context context2 = context;
        boolean projMode = false;
        this.mContext = context2;
        this.mAMS = (HwActivityManagerService) ams;
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new LocalHandler(this.mHandlerThread.getLooper());
        boolean isFactory = SystemProperties.get("ro.runmode", "normal").equals("factory");
        boolean isMmiTest = SystemProperties.get(MMI_TEST_PROPERTY, "false").equals("true");
        if (isFactory || isMmiTest) {
            HwPCUtils.setFactoryOrMmiState(true);
        }
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
                int i = displays.length - 1;
                while (true) {
                    if (i >= 0) {
                        if (displays[i] != null && displays[i].getDisplayId() != 0 && isWiredDisplay(displays[i].getDisplayId())) {
                            displayId = displays[i].getDisplayId();
                            break;
                        }
                        i--;
                    } else {
                        break;
                    }
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
            int[] devices = InputDevice.getDeviceIds();
            for (int device : devices) {
                this.mConnectedInputDevices |= whichInputDevice(InputDevice.getDevice(device));
            }
            im.registerInputDeviceListener(this.mInputDeviceListener, null);
        }
        this.mPCSettingsObserver = new PCSettingsObserver(this.mHandler);
        this.mPCSettingsObserver.readScreenOffSettings();
        this.mPCSettingsObserver.restoreScreenOffSettings();
        this.mProvisioned = deviceIsProvisioned();
        this.mUserSetupComplete = isUserSetupComplete();
        this.mPCSettingsObserver.observe();
        if (HwPCUtils.enabledInPad()) {
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1 ? true : projMode) {
                this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
            } else {
                this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
            }
        } else {
            this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
            Settings.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 1);
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NOTIFY_SWITCH_MODE);
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction(ACTION_NOTIFY_UNINSTALL_APP);
        filter.addAction(ACTION_NOTIFY_SHOW_MK);
        filter.addAction(ACTION_NOTIFY_DISCONNECT);
        filter.addAction(ACTION_NOTIFY_OPEN_EASY_PROJECTION);
        filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
        filter.addAction(DecisionUtil.PC_TARGET_ACTION);
        filter.addAction(ACTION_WIFI_DISPLAY_CASTING);
        this.mContext.registerReceiver(this.mNotifyReceiver, filter, PERMISSION_BROADCAST_SWITCH_MODE, null);
        this.mUnlockScreenReceiver = new UnlockScreenReceiver();
        IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUnlockScreenReceiver, unlockFilter);
        registerExternalPointerEventListener();
        this.mIBinderAudioService = ServiceManager.getService("audio");
        setDesktopModeToAudioService(-1);
        this.mAlarmManager = (AlarmManager) context2.getSystemService("alarm");
        this.mTelephonyPhone = (TelephonyManager) this.mContext.getSystemService("phone");
        HwPCUtils.setPcCastModeInServerEarly(HwPCUtils.ProjectionMode.PHONE_MODE);
        this.mKeyboardObserver.startObserving(EXCLUSIVE_KEYBOARD);
        this.mDPLinkStateObserver.startObserving(EXCLUSIVE_DP_LINK);
        this.mBluetoothReminderDialog = new BluetoothReminderDialog();
        this.mPcMultiDisplayMgr = new HwPCMultiDisplaysManager(this.mContext, this.mHandler, this);
        this.mVAssistCmdExecutor = new HwPCVAssistCmdExecutor(this.mContext, this, this.mAMS);
        SystemProperties.set("hw.pc.support.app.projection", String.valueOf(true));
        this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pChannel = this.mWifiP2pManager.initialize(this.mContext, this.mHandler.getLooper(), null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean deviceIsProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isUserSetupComplete() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) != 0;
    }

    /* access modifiers changed from: private */
    public void setDesktopModeToAudioService(int mode) {
        HwPCUtils.log(TAG, "setDesktopModeToAudioService, mIBinderAudioService = " + this.mIBinderAudioService);
        if (!HwPCUtils.enabledInPad() && this.mIBinderAudioService != null) {
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

    /* access modifiers changed from: private */
    public void scheduleSwitchUser(int userId) {
        this.mHandler.removeMessages(12);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, userId, -1));
    }

    /* access modifiers changed from: private */
    public void onSwitchUser(int userId) {
        HwPCUtils.log(TAG, "onSwitchUser userId =" + userId);
        this.mUserId = userId;
        if (get1stDisplay().mDisplayId != -1) {
            if (userId == 0) {
                HwPCUtils.log(TAG, "onSwitchUser: UserHandle.USER_OWNER");
                if (!(this.mDisplayManager == null || this.mDisplayManager.getDisplay(get1stDisplay().mDisplayId) == null)) {
                    sendNotificationForSwitch(this.mProjMode);
                }
            } else {
                if (isDesktopMode(this.mProjMode)) {
                    lightPhoneScreen();
                    HwPCUtils.log(TAG, "onSwitchUser: The current mode is DesktopMode");
                    this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                    HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                    if (!HwPCUtils.enabledInPad()) {
                        bindUnbindService(false);
                    }
                    this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
                    setUsePCModeMouseIconContext(false);
                    HwPCUtils.setPcCastModeInServer(false);
                    this.mIgnoreInjectEvent = false;
                    setPcCastingDisplayId(-1);
                    this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                    updateIMEWithHardKeyboardState(false);
                    Settings.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
                    if (get1stDisplay().mType == 2) {
                        this.mDDC.resetProjectionMode();
                    }
                    updateFingerprintSlideSwitch();
                    sendSwitchToStatusBar();
                    relaunchIMEDelay(0);
                    if (HwPCUtils.enabledInPad()) {
                        bindUnbindService(false);
                    }
                }
                if (this.mNm != null) {
                    this.mNm.cancelAll();
                    this.mHasSwitchNtf = false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isWiredDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        boolean z = false;
        if (this.mDisplayManager == null) {
            return false;
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            return false;
        }
        int type = display.getType();
        if (type == 5 && "com.hpplay.happycast".equals(display.getOwnerPackageName())) {
            return true;
        }
        if (type == 5 && "com.huawei.works".equals(display.getOwnerPackageName()) && ESHARE_DISPLAY_NAME.equals(display.getName())) {
            return true;
        }
        if (HwPCUtils.isWirelessProjectionEnabled()) {
            if (type == 2 || type == 3 || ((type == 5 || type == 4) && this.mSupportOverlay)) {
                z = true;
            }
            return z;
        }
        if (type == 2 || ((type == 5 || type == 4) && this.mSupportOverlay)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void sendNotificationForSwitch(HwPCUtils.ProjectionMode projMode) {
        String mode;
        if (!HwPCUtils.enabledInPad() && this.mNm != null) {
            Notification.Builder builder = new Notification.Builder(this.mContext, "HW_PCM");
            builder.setSmallIcon(33751738);
            builder.setVisibility(-1);
            builder.setPriority(1);
            builder.setAppName(this.mContext.getString(33685941));
            if (isDesktopMode(projMode)) {
                mode = this.mContext.getString(33686157);
            } else {
                mode = this.mContext.getString(33686158);
            }
            String mode2 = mode;
            boolean isConnectFromWelink = isConnectFromThirdApp(get1stDisplay().mDisplayId) == 2;
            this.mWireLessDeviceName = "";
            if (HwPCUtils.getIsWifiMode()) {
                if (!(this.mDisplayManager == null || this.mDisplayManager.getDisplay(get1stDisplay().mDisplayId) == null)) {
                    this.mWireLessDeviceName = String.format(this.mContext.getResources().getString(33686156), new Object[]{this.mDisplayManager.getDisplay(get1stDisplay().mDisplayId).getName()});
                }
            } else if (isConnectFromWelink) {
                this.mWireLessDeviceName = String.format(this.mContext.getResources().getString(33686156), new Object[]{this.mContext.getResources().getString(33686254)});
            }
            PendingIntent clickPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(ACTION_NOTIFY_OPEN_EASY_PROJECTION), 134217728, UserHandle.OWNER);
            Intent switchModeIntent = new Intent(ACTION_NOTIFY_SWITCH_MODE);
            PendingIntent switchModePendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 1, switchModeIntent, 268435456, UserHandle.OWNER);
            Intent showMKIntent = new Intent(ACTION_NOTIFY_SHOW_MK);
            PendingIntent showMKPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 1, showMKIntent, 134217728, UserHandle.OWNER);
            Intent disconnectIntent = new Intent(ACTION_NOTIFY_DISCONNECT);
            PendingIntent disconnectPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 1, disconnectIntent, 134217728, UserHandle.OWNER);
            builder.setContentIntent(clickPendingIntent);
            String str = mode2;
            PendingIntent pendingIntent = switchModePendingIntent;
            PendingIntent pendingIntent2 = showMKPendingIntent;
            Intent intent = disconnectIntent;
            PendingIntent pendingIntent3 = disconnectPendingIntent;
            Intent intent2 = showMKIntent;
            boolean z = isConnectFromWelink;
            Intent intent3 = switchModeIntent;
            RemoteViews bigContentView = createContentView(str, pendingIntent, pendingIntent2, pendingIntent3, z, true);
            builder.setCustomContentView(createContentView(str, pendingIntent, pendingIntent2, pendingIntent3, z, false));
            builder.setCustomBigContentView(bigContentView);
            Notification notification = builder.getNotification();
            notification.flags |= 2;
            notification.flags |= 32;
            this.mNm.notify(TAG, 0, notification);
            this.mHasSwitchNtf = true;
        }
    }

    private RemoteViews createContentView(String mode, PendingIntent switchModePendingIntent, PendingIntent showMKPendingIntent, PendingIntent disconnectPendingIntent, boolean isConnectFromWelink, boolean isBigContentVIew) {
        RemoteViews contentView;
        int i = 8;
        if (isBigContentVIew) {
            contentView = new RemoteViews(this.mContext.getPackageName(), 34013361);
            if (isDesktopMode(this.mProjMode)) {
                contentView.setOnClickPendingIntent(34603364, switchModePendingIntent);
                contentView.setViewVisibility(34603364, 0);
                contentView.setTextViewText(34603364, getActionText(true));
                contentView.setViewVisibility(34603362, 8);
            } else {
                contentView.setOnClickPendingIntent(34603362, switchModePendingIntent);
                contentView.setViewVisibility(34603364, 8);
                contentView.setViewVisibility(34603362, 0);
                contentView.setTextViewText(34603362, getActionText(false));
            }
            contentView.setTextViewText(34603366, this.mContext.getString(33686155));
            if (!isNeedShowMKAction()) {
                contentView.setViewVisibility(34603366, 8);
            } else {
                contentView.setViewVisibility(34603366, 0);
                contentView.setOnClickPendingIntent(34603366, showMKPendingIntent);
            }
            contentView.setTextViewText(34603359, this.mContext.getString(33686153));
            if (HwPCUtils.getIsWifiMode() || isConnectFromWelink) {
                contentView.setViewVisibility(34603359, 0);
                contentView.setOnClickPendingIntent(34603359, disconnectPendingIntent);
            } else {
                contentView.setViewVisibility(34603359, 8);
            }
        } else {
            contentView = new RemoteViews(this.mContext.getPackageName(), 34013360);
            contentView.setImageViewResource(34603363, getResDrawableId(isDesktopMode(this.mProjMode)));
            contentView.setOnClickPendingIntent(34603363, switchModePendingIntent);
            if (!isNeedShowMKAction()) {
                contentView.setViewVisibility(34603367, 8);
            } else {
                contentView.setViewVisibility(34603367, 0);
                contentView.setOnClickPendingIntent(34603367, showMKPendingIntent);
            }
            if (HwPCUtils.getIsWifiMode() || isConnectFromWelink) {
                contentView.setViewVisibility(34603360, 0);
                contentView.setOnClickPendingIntent(34603360, disconnectPendingIntent);
            } else {
                contentView.setViewVisibility(34603360, 8);
            }
        }
        contentView.setTextViewText(34603365, this.mContext.getString(33685941));
        contentView.setTextViewText(34603361, mode);
        contentView.setTextViewText(34603358, this.mWireLessDeviceName);
        if (!TextUtils.isEmpty(this.mWireLessDeviceName)) {
            i = 0;
        }
        contentView.setViewVisibility(34603358, i);
        return contentView;
    }

    private CharSequence getActionText(boolean isDesktopMode) {
        if (isDesktopMode) {
            return this.mContext.getString(33686154);
        }
        return this.mContext.getString(33686152);
    }

    private int getResDrawableId(boolean isDesktopMode) {
        if (isDesktopMode) {
            return 33752036;
        }
        return 33752034;
    }

    /* access modifiers changed from: private */
    public void collapsePanels() {
        StatusBarManagerService statusBarService = ServiceManager.getService("statusbar");
        if (statusBarService != null) {
            statusBarService.collapsePanels();
        }
    }

    public void onDisplayChanged(int displayId) {
        HwPCUtils.log(TAG, "onDisplayChanged, displayId:" + displayId);
        if (this.mProvisioned) {
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
            if (displayId == get2ndDisplay().mDisplayId) {
                HwPCUtils.log(TAG, "onDisplayRemoved ignore it if 2nd display removed.");
                if (get2ndDisplay().mType == 3) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 0);
                }
                get2ndDisplay().mDisplayId = -1;
                get2ndDisplay().mType = 0;
                return;
            }
            if (HwPCUtils.getIsWifiMode() || get1stDisplay().mType == 5) {
                Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 0);
            }
            boolean isInPhoneMode = HwPCUtils.getPhoneDisplayID() == displayId && !isDesktopMode(this.mProjMode);
            HwPCUtils.log(TAG, "onDisplayRemoved, displayId:" + displayId + ", isInPhoneMode:" + isInPhoneMode + " mProjMode: " + this.mProjMode);
            if (HwPCUtils.getPCDisplayID() != displayId && !isInPhoneMode) {
                HwPCUtils.log(TAG, "onDisplayRemoved, displayId is neither PC Display ID nor Phone Display ID.");
            } else if ((!this.mProvisioned || !this.mUserSetupComplete) && displayId != get1stDisplay().mDisplayId) {
                HwPCUtils.log(TAG, "onDisplayRemoved not permitted before setup or not scheduleDisplayAdded");
            } else {
                lockScreenWhenDisconnected();
                HwPCUtils.setPcCastModeInServerEarly(HwPCUtils.ProjectionMode.PHONE_MODE);
                if (this.mProjMode == HwPCUtils.ProjectionMode.DESKTOP_MODE) {
                    bdReportDiffSrcStatus(false);
                } else {
                    bdReportSameSrcStatus(false);
                }
                if (this.mNm != null) {
                    HwPCUtils.log(TAG, "onDisplayRemoved cancel notification.");
                    this.mNm.cancelAll();
                    this.mHasSwitchNtf = false;
                }
                if (this.mPcMultiDisplayMgr.is4KHdmi1stDisplayRemoved(displayId)) {
                    HwPCUtils.log(TAG, "onDisplayRemoved ignore it when dp is on.");
                    get1stDisplay().mDisplayId = -1;
                    if (!isInPhoneMode) {
                        bindUnbindService(false);
                        setUsePCModeMouseIconContext(false);
                        updateIMEWithHardKeyboardState(false);
                    } else {
                        HwPCUtils.setPhoneDisplayID(-1);
                    }
                    HwPCUtils.setPcCastModeInServer(false);
                    this.mIgnoreInjectEvent = false;
                    setPcCastingDisplayId(-1);
                    this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                    setDesktopModeToAudioService(-1);
                    updateFingerprintSlideSwitch();
                    return;
                }
                get1stDisplay().mDisplayId = -1;
                get1stDisplay().mType = 0;
                if (!isInPhoneMode) {
                    bindUnbindService(false);
                    setUsePCModeMouseIconContext(false);
                    updateIMEWithHardKeyboardState(false);
                } else {
                    HwPCUtils.setPhoneDisplayID(-1);
                }
                HwPCUtils.setPcCastModeInServer(false);
                this.mIgnoreInjectEvent = false;
                setPcCastingDisplayId(-1);
                this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                setDesktopModeToAudioService(-1);
                updateFingerprintSlideSwitch();
                Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 1);
                this.mPcMultiDisplayMgr.handlelstDisplayInDisplayRemoved();
                HwPCDataReporter.getInstance().stopPCDisplay();
                exitDesktopModeForMk();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onDisplayAdded(int displayId) {
        if (HwPCUtils.enabled()) {
            HwPCUtils.log(TAG, "onDisplayAdded, displayId:" + displayId);
            if (!this.mProvisioned || !this.mUserSetupComplete) {
                HwPCUtils.log(TAG, "onDisplayAdded not permitted before setup");
            } else if (displayId == -1 || displayId == 0) {
                HwPCUtils.log(TAG, "context is null or is default display");
            } else if (HwPCUtils.enabledInPad() && this.mUserId != 0) {
                HwPCUtils.log(TAG, "switchProjMode failed! currentUser is not UserHandle.USER_OWNER");
            } else if (!HwPCUtils.enabledInPad() || !isMonkeyRunning()) {
                if (isWifiPCMode(displayId) || isConnectFromThirdApp(displayId) > 0) {
                    HwPCUtils.bdReport(this.mContext, 10057, "");
                } else if (isWiredDisplay(displayId)) {
                    HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT, "");
                } else {
                    HwPCUtils.bdReport(this.mContext, 10057, "");
                }
                bdReportConnectDisplay(displayId);
                if (!isWiredDisplay(displayId)) {
                    HwPCUtils.log(TAG, "is not a wired display.");
                    return;
                }
                if (isConnectFromThirdApp(displayId) == 2) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 1);
                }
                Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 0);
                this.mPcMultiDisplayMgr.checkInitialDpConnectAfterBoot(displayId);
                if (!this.mPcMultiDisplayMgr.handleTwoDisplaysInDisplayAdded(displayId)) {
                    HwPCUtils.log(TAG, "it's not 1st display added, need not continue.");
                    return;
                }
                if (HwPCUtils.enabledInPad()) {
                    if (this.mPadPCDisplayIsRemoved) {
                        get1stDisplay().mDisplayId = displayId;
                        this.mPadPCDisplayIsRemoved = false;
                        HwPCUtils.log(TAG, "PadPCDisplayIsRemoved return.");
                        return;
                    } else if (!deviceIsProvisioned() || !isUserSetupComplete()) {
                        HwPCUtils.log(TAG, "Startup Guide is not Complete");
                        return;
                    }
                }
                if (Settings.Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1 && isConnectFromThirdApp(displayId) != 2) {
                    this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
                } else {
                    this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                    if (isExclusiveKeyboardConnected() && HwPCUtils.enabledInPad()) {
                        showEnterDesktopAlertDialog(getCurrentContext(), true);
                    }
                }
                get1stDisplay().mDisplayId = displayId;
                get1stDisplay().mType = this.mPcMultiDisplayMgr.getDisplayType(displayId);
                HwPCUtils.setIsWifiMode(isWifiPCMode(get1stDisplay().mDisplayId));
                boolean exist = systemUIExist() && explorerExist();
                boolean enterDesktopMode = exist && isDesktopMode(this.mProjMode) && this.mUserId == 0;
                HwPCUtils.log(TAG, "onDisplayAdded mProjMode " + this.mProjMode + ", mConnectedInputDevices = " + this.mConnectedInputDevices + ", exist =" + exist + ", mUserId = " + this.mUserId + ", enterDesktopMode =" + enterDesktopMode);
                if (this.mDisplayManager != null) {
                    Display display = this.mDisplayManager.getDisplay(get1stDisplay().mDisplayId);
                    if (display != null) {
                        this.mPCDisplayInfo = new DisplayInfo();
                        if (display.getDisplayInfo(this.mPCDisplayInfo)) {
                            int width = this.mPCDisplayInfo.getNaturalWidth();
                            int height = this.mPCDisplayInfo.getNaturalHeight();
                            HwPCUtils.log(TAG, "width = " + width + ", height = " + height);
                            if (width > RESO_1080P) {
                                this.mIsDisplayLargerThan1080p = true;
                            } else {
                                this.mIsDisplayLargerThan1080p = false;
                            }
                        }
                    }
                }
                if (enterDesktopMode) {
                    HwPCUtils.setPCDisplayID(displayId);
                    HwPCUtils.setPhoneDisplayID(-1);
                    if (!isDesktopMode(this.mProjMode)) {
                        Settings.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
                    }
                    if (!HwPCUtils.getIsWifiMode()) {
                        autoLaunchMK();
                    } else if (this.mIsWifiBroadDone) {
                        autoLaunchMK();
                        this.mIsWifiBroadDone = false;
                    }
                    this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
                    HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                    HwPCUtils.setPcCastModeInServer(true);
                    setPcCastingDisplayId(get1stDisplay().mDisplayId);
                    this.mVAssistCmdExecutor.notifyDesktopModeChanged(true, get1stDisplay().mDisplayId);
                    updateIMEWithHardKeyboardState(true);
                    saveRotationInPad();
                    this.mAMS.freezeOrThawRotationInPcMode();
                    this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
                    setUsePCModeMouseIconContext(true);
                    if (this.restartByUnlock2SetAnimTime) {
                        this.mPCBeforeBootAnimTime = 1500;
                    } else if (HwPCUtils.getIsWifiMode()) {
                        this.mPCBeforeBootAnimTime = 0;
                    } else {
                        this.mPCBeforeBootAnimTime = TIME_DISPALY_ADD_BEFORE_BOOT_ANIM;
                    }
                    bindUnbindService(true);
                    this.restartByUnlock2SetAnimTime = false;
                    this.mHandler.removeMessages(1);
                    Message msg = this.mHandler.obtainMessage(1);
                    msg.obj = this.mProjMode;
                    this.mHandler.sendMessage(msg);
                    bdReportSameSrcStatus(true);
                    setDesktopModeToAudioService(1);
                    this.mIsDisplayAddedAfterSwitch = true;
                    backToHomeInDefaultDisplay(get1stDisplay().mDisplayId);
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
                    enterDesktopModeForMk();
                } else {
                    HwPCUtils.setPhoneDisplayID(displayId);
                    if (HwPCUtils.enabledInPad()) {
                        HwPCUtils.log(TAG, "onDisplayAdded there is something wrong when enter PAD PC mode !");
                        return;
                    }
                    lightPhoneScreen();
                    this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                    HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                    this.mAMS.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
                    setUsePCModeMouseIconContext(false);
                    HwPCUtils.setPcCastModeInServer(false);
                    setPcCastingDisplayId(-1);
                    this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                    updateIMEWithHardKeyboardState(false);
                    this.mHandler.removeMessages(1);
                    Message msg2 = this.mHandler.obtainMessage(1);
                    msg2.obj = this.mProjMode;
                    this.mHandler.sendMessage(msg2);
                    bdReportDiffSrcStatus(true);
                    setDesktopModeToAudioService(0);
                    updateFingerprintSlideSwitch();
                    exitDesktopModeForMk();
                    sendShowHelpMsgToPCModel();
                }
                scheduleRestoreApps(displayId);
                sendSwitchToStatusBar();
                HwPCDataReporter.getInstance().startPCDisplay();
                DecisionUtil.executeEnterProjectionEvent(this.mContext);
            } else {
                HwPCUtils.log(TAG, "onDisplayAdded isMonkeyRunning return !");
            }
        }
    }

    private void sendShowHelpMsgToPCModel() {
        if (!HwPCUtils.enabledInPad()) {
            int helpMsgNumber = Settings.Secure.getInt(this.mContext.getContentResolver(), HELP_MSG_PROMPT_NUMBER, 0);
            HwPCUtils.log(TAG, "showHelpMsgToPCModel helpMsgNumber : " + helpMsgNumber);
            if (helpMsgNumber < 3) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), HELP_MSG_PROMPT_NUMBER, helpMsgNumber + 1);
                this.mHandler.removeMessages(26);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(26, 0, -1));
            }
        }
    }

    private void autoLaunchMK() {
        boolean z = true;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "guide-started", 0) != 1) {
            z = false;
        }
        boolean guideStarted = z;
        if (HwPCUtils.getIsWifiMode() || guideStarted || isConnectFromThirdApp(get1stDisplay().mDisplayId) == 2) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    HwPCManagerService.this.sendShowMkMessage();
                }
            }, 2000);
        }
    }

    public void LaunchMKForWifiMode() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "LaunchMKForWifiMode checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        if (get1stDisplay().mDisplayId == -1 || HwPCUtils.enabledInPad() || !isDesktopMode(this.mProjMode)) {
            this.mIsWifiBroadDone = true;
        } else {
            autoLaunchMK();
            this.mIsWifiBroadDone = false;
        }
    }

    private void enterDesktopModeForMk() {
        HwPCUtils.log(TAG, "enterDesktopModeForMk   ");
        if (this.mHwPolicy == null) {
            this.mHwPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        }
        this.mPCMkManager = HwPCMkManager.getInstance(this.mContext);
        this.mPCMkManager.initCrop(this.mContext, this);
        this.mPCMkManager.startSendEventThread();
        this.mPCMkManager.updatePointerAxis(getPointerCoordinateAxis());
    }

    /* access modifiers changed from: private */
    public void exitDesktopModeForMk() {
        HwPCUtils.log(TAG, "exitDesktopModeForMk   ");
        if (this.mPCMkManager != null) {
            this.mPCMkManager.stopSendEventThreadAndRelease();
        }
    }

    /* access modifiers changed from: private */
    public void backToHomeInDefaultDisplay(int curDisplayId) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "backToHomeInDefaultDisplay");
            Intent homeIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
            homeIntent.addFlags(268435456);
            try {
                this.mContext.startActivity(homeIntent);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "ActivityMovedToDesktopDisplay fail to start go home");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setUsePCModeMouseIconContext(boolean pcmode) {
        HwInputManagerService.HwInputManagerLocalService inputManager = (HwInputManagerService.HwInputManagerLocalService) LocalServices.getService(HwInputManagerService.HwInputManagerLocalService.class);
        if (inputManager == null) {
            return;
        }
        if (pcmode) {
            if (this.mDisplayManager == null) {
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
            }
            if (this.mDisplayManager != null) {
                Display[] displays = this.mDisplayManager.getDisplays();
                Display display = null;
                int length = displays.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    Display dis = displays[i];
                    if (dis != null && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                        display = dis;
                        break;
                    }
                    i++;
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
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "switchProjMode failed " + Binder.getCallingPid());
            HwPCDataReporter.getInstance().reportFailSwitchEvent(2, this.mProjMode.ordinal(), this.mPCDisplayInfo);
            return;
        }
        HwPCUtils.log(TAG, "switchProjMode, mProjMode " + this.mProjMode + ", isEnterDesktopModeRemembered:" + isEnterDesktopModeRemembered() + " isExitDesktopModeRemembered:" + isExitDesktopModeRemembered());
        if (!HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "Not enabledInPad()");
            sendSwitchMsg();
        } else if (this.mUserId != 0) {
            HwPCUtils.log(TAG, "switchProjMode failed! currentUser is not UserHandle.USER_OWNER");
            HwPCDataReporter.getInstance().reportFailSwitchEvent(3, this.mProjMode.ordinal(), this.mPCDisplayInfo);
        } else {
            int i = 0;
            if (isCalling()) {
                if (isDesktopMode(this.mProjMode)) {
                    i = get1stDisplay().mDisplayId;
                }
                showCallingToast(i);
                HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
                HwPCDataReporter.getInstance().reportFailSwitchEvent(4, this.mProjMode.ordinal(), this.mPCDisplayInfo);
                return;
            }
            if (!isDesktopMode(this.mProjMode)) {
                showEnterDesktopAlertDialog(getCurrentContext(), false);
            } else {
                showExitDesktopAlertDialog(getCurrentContext(), false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendSwitchToStatusBar() {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "sendSwitchToStatusBar!");
            Intent intent = new Intent();
            intent.setAction(ACTION_NOTIFY_CHANGE_STATUS_BAR);
            this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_CHANGE_STATUS_BAR);
        }
    }

    /* access modifiers changed from: private */
    public void refreshNotifications() {
        HwPCUtils.log(TAG, "refreshNotifications mHasSwitchNtf = " + this.mHasSwitchNtf);
        this.mHandler.removeMessages(5);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5));
    }

    /* access modifiers changed from: private */
    public static int whichInputDevice(InputDevice device) {
        int ret = 0;
        if (device != null) {
            HwPCUtils.log(TAG, "device=" + device + ", souces = " + device.getSources());
        }
        if (device == null || !device.isExternal()) {
            HwPCUtils.log(TAG, "whichInputDevice unkown input device");
            return 0;
        }
        if ((device.getSources() & 139270) != 0) {
            ret = 0 | 1;
        }
        if ((device.getSources() & 257) != 0) {
            ret |= 2;
        }
        if ((device.getSources() & 4098) == 4098) {
            return ret | 4;
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

    /* access modifiers changed from: private */
    public boolean isExclusiveKeyboard(InputDevice inputDevice) {
        int keyboardProductId;
        int keyboardVendorId;
        if (inputDevice == null) {
            HwPCUtils.log(TAG, "isExclusiveKeyboard=false");
            return false;
        }
        HwPCUtils.log(TAG, "Keyboard ProductId:" + keyboardProductId + " VendorId:" + keyboardVendorId);
        if (KEYBOARD_PRODUCT_ID.length != KEYBOARD_VENDOR_ID.length) {
            return false;
        }
        for (int i = 0; i < KEYBOARD_PRODUCT_ID.length; i++) {
            if (keyboardProductId == KEYBOARD_PRODUCT_ID[i] && keyboardVendorId == KEYBOARD_VENDOR_ID[i]) {
                HwPCUtils.log(TAG, "isExclusiveKeyboard=true");
                return true;
            }
        }
        HwPCUtils.log(TAG, "isExclusiveKeyboard=false");
        return false;
    }

    /* access modifiers changed from: private */
    public void sendShowMkMessage() {
        HwPCUtils.log(TAG, "sendShowMkMessage todo launch touchpad");
        if (!isNeedShowMKAction()) {
            HwPCUtils.log(TAG, "unable to send ShowMk message, existMouse:" + existMouseInputDevices() + " get1stDisplay().mDisplayId:" + get1stDisplay().mDisplayId + " mProjMode:" + this.mProjMode);
            return;
        }
        this.mHandler.removeMessages(13);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(13));
    }

    /* access modifiers changed from: private */
    public void sendOpenEasyProjectionMessage() {
        this.mHandler.removeMessages(23);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23));
    }

    /* access modifiers changed from: private */
    public void launchMK() {
        if (!HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "launchMK todo start touchpad activiy");
            if (!isNeedShowMKAction()) {
                HwPCUtils.log(TAG, "cannot launch MK, existMouse:" + existMouseInputDevices() + " get1stDisplay().mDisplayId:" + get1stDisplay().mDisplayId);
                return;
            }
            Display display = this.mContext.getDisplay();
            if (!(display == null || display.getDisplayId() == 0)) {
                HwPCUtils.log(TAG, "launchMK start touchpad activiy displayid is " + display.getDisplayId());
            }
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
        }
    }

    /* access modifiers changed from: private */
    public void openEasyProjection() {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        if (HwPCUtils.isWirelessProjectionEnabled()) {
            intent.setComponent(this.mInstructionComponentWirelessEnabled);
            HwPCUtils.bdReport(this.mContext, 10045, "");
        } else {
            intent.setComponent(this.mInstructionComponent);
            HwPCUtils.bdReport(this.mContext, 10046, "");
        }
        this.mContext.startActivity(intent);
    }

    /* access modifiers changed from: private */
    public void sendUninstallAppMessage(String packageName) {
        HwPCUtils.log(TAG, "sendUninstallAppMessage: " + packageName);
        Message msg = this.mHandler.obtainMessage(16);
        msg.obj = packageName;
        this.mHandler.sendMessage(msg);
    }

    private boolean isNeedShowMKAction() {
        if (this.mSupportTouchPad && this.mProjMode == HwPCUtils.ProjectionMode.DESKTOP_MODE && get1stDisplay().mDisplayId != -1) {
            return true;
        }
        HwPCUtils.log(TAG, "cannot show mk Action notify, get1stDisplay().mDisplayId:" + get1stDisplay().mDisplayId + " existMouse:" + existMouseInputDevices() + " mSupportTouchPad = " + this.mSupportTouchPad + " mProjMode = " + this.mProjMode);
        return false;
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
        return isDesktopMode(this.mProjMode) && HwPCUtils.isValidExtDisplayId(get1stDisplay().mDisplayId);
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
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "relaunchIMEIfNecessary checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mHandler.removeMessages(14);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(14));
    }

    /* access modifiers changed from: private */
    public void relaunchIMEDelay(int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "relaunchIMEDelay");
            this.mHandler.removeMessages(14);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(14), (long) delayMillis);
        }
    }

    /* access modifiers changed from: private */
    public void updateDisplayOverrideConfiguration(int display, int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "updateDisplayOverrideConfiguration");
            this.mHandler.removeMessages(18);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(18, display, 0), (long) delayMillis);
        }
    }

    /* access modifiers changed from: private */
    public void doUpdateDisplayOverrideConfiguration(int displayid) {
        this.mAMS.updateDisplayOverrideConfiguration(null, displayid);
    }

    /* access modifiers changed from: private */
    public void doRelaunchIMEIfNecessary() {
        this.mAMS.relaunchIMEIfNecessary();
    }

    public void hwRestoreTask(int taskId, float x, float y) {
        if (this.mAMS.checkTaskId(taskId) || checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.hwRestoreTask(taskId, x, y);
            return;
        }
        HwPCUtils.log(TAG, "hwRestoreTask checktaskId failed:" + taskId);
    }

    public void hwResizeTask(int taskId, Rect bounds) {
        if (this.mAMS.checkTaskId(taskId) || checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            this.mAMS.hwResizeTask(taskId, bounds);
            return;
        }
        HwPCUtils.log(TAG, "hwResizeTask checktaskId failed:" + taskId);
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
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "registerHwTaskStackListener checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mAMS.registerHwTaskStackListener(listener);
    }

    public void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "unRegisterHwTaskStackListener checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mAMS.unRegisterHwTaskStackListener(listener);
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return this.mAMS.getDisplayBitmap(displayId, width, height);
        }
        HwPCUtils.log(TAG, "getDisplayBitmap checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void registHwSystemUIController(Messenger messenger) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "registHwSystemUIController checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mMessenger = messenger;
    }

    private Message getMessage(int what) {
        Message message = Message.obtain();
        message.what = what;
        return message;
    }

    public void showTopBar() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "showTopBar checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                this.mMessenger.send(getMessage(1));
            }
        } catch (RemoteException e) {
            HwPCUtils.log("showTopBar", "RemoteException");
        }
    }

    public void showStartMenu() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "showStartMenu checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                this.mMessenger.send(getMessage(2));
            }
        } catch (RemoteException e) {
            HwPCUtils.log("showStartMenu", "RemoteException");
        }
    }

    public void screenshotPc() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "screenshotPc checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                this.mMessenger.send(getMessage(3));
            }
        } catch (RemoteException e) {
            HwPCUtils.log("screenshotPc", "RemoteException");
        }
    }

    public void userActivityOnDesktop() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "userActivityOnDesktop checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                this.mMessenger.send(getMessage(7));
            }
        } catch (RemoteException e) {
            HwPCUtils.log("userActivityOnDesktop", "RemoteException");
        }
    }

    public void closeTopWindow() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "closeTopWindow checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                this.mMessenger.send(getMessage(4));
            }
        } catch (RemoteException e) {
            HwPCUtils.log("closeTopWindow", "RemoteException");
        }
    }

    public void triggerSwitchTaskView(boolean show) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "triggerSwitchTaskView checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                if (show) {
                    this.mMessenger.send(getMessage(5));
                } else {
                    this.mMessenger.send(getMessage(6));
                }
            }
        } catch (RemoteException e) {
            HwPCUtils.log("screenshotPc", "RemoteException");
        }
    }

    public Bitmap getTaskThumbnailEx(int id) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "getTaskThumbnailEx checkCallingPermission failed" + Binder.getCallingPid());
            return null;
        } else if (this.mAMS != null) {
            return this.mAMS.getTaskThumbnailOnPCMode(id);
        } else {
            HwPCUtils.log(TAG, "getTaskThumbnailEx failed , ams is null");
            return null;
        }
    }

    public void toggleHome() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "toggleHome checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mAMS.toggleHome();
    }

    public boolean injectInputEventExternal(InputEvent event, int mode) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "injectInputEventExternal checkCallingPermission failed" + Binder.getCallingPid());
            return false;
        } else if ((event instanceof MotionEvent) && mode == 0 && ((MotionEvent) event).getDeviceId() == HwPCUtils.mTouchDeviceID) {
            return false;
        } else {
            if (this.mHwPolicy == null || this.mPCMkManager == null) {
                HwPCUtils.log(TAG, " injectInputEventExternal policy or PCMkManager is null");
                return false;
            } else if (mode == 2 || mode == 3) {
                HwInputManagerService.HwInputManagerLocalService inputManager = (HwInputManagerService.HwInputManagerLocalService) LocalServices.getService(HwInputManagerService.HwInputManagerLocalService.class);
                if (inputManager != null) {
                    return inputManager.injectInputEvent(event, 0);
                }
                return false;
            } else if (this.mIgnoreInjectEvent) {
                HwPCUtils.log(TAG, " injectInputEventExternal mIgnoreInjectEvent = true");
                return false;
            } else {
                WindowState focusedWin = this.mHwPolicy.getTopFullscreenWindow();
                if (!(event instanceof MotionEvent) || focusedWin == null || focusedWin.getDisplayId() != 0 || focusedWin.getAttrs() == null || focusedWin.getAttrs().getTitle() == null || !"com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(focusedWin.getAttrs().getTitle().toString())) {
                    return false;
                }
                boolean shouldDrop = false;
                try {
                    shouldDrop = HwWindowManager.shouldDropMotionEventForTouchPad(((MotionEvent) event).getX(), ((MotionEvent) event).getY());
                } catch (NullPointerException e) {
                    HwPCUtils.log(TAG, "injectInputEventExternal NullPointerException");
                }
                if (shouldDrop) {
                    HwPCUtils.log(TAG, "injectInputEventExternal should drop MotionEvent for TouchPad");
                    return false;
                }
                if (this.mPCMkManager.sendEvent((MotionEvent) event, focusedWin.getVisibleFrameLw(), focusedWin.getDisplayFrameLw(), mode)) {
                    return true;
                }
                return false;
            }
        }
    }

    public void registerExternalPointerEventListener() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "registerExternalPointerEventListener checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mAMS.registerExternalPointerEventListener(this.mPointerListener);
    }

    public void unregisterExternalPointerEventListener() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "unregisterExternalPointerEventListener checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mAMS.unregisterExternalPointerEventListener(this.mPointerListener);
    }

    public float[] getPointerCoordinateAxis() {
        float[] axis = new float[2];
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "getPointerCoordinateAxis checkCallingPermission failed" + Binder.getCallingPid());
            return axis;
        }
        axis[0] = this.mAxisX;
        axis[1] = this.mAxisY;
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
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "saveAppIntent checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mHandler.removeMessages(15);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(15, intents));
    }

    private void scheduleRestoreApps(int displayId) {
        if (!this.mIntentList.isEmpty()) {
            this.mAlarmManager.cancel(this.mAlarmListener);
            this.mHandler.removeMessages(10);
            this.mHandler.removeMessages(9);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(9, displayId, 0), 3000);
        }
    }

    /* access modifiers changed from: private */
    public void handleRestoreApps(int displayId) {
        int N = this.mIntentList.size();
        for (int i = 0; i < N; i++) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10, displayId, 0, this.mIntentList.get(i)), (long) (i * 800));
        }
        this.mIntentList.clear();
    }

    /* access modifiers changed from: private */
    public void restoreApp(int displayId, Intent intent) {
        Context displayContext = getDisplayContext(this.mContext, displayId);
        if (!(displayContext == null || intent == null)) {
            try {
                if (!HwPCUtils.enabledInPad() || intent.getComponent() == null || !"com.android.incallui".equals(intent.getComponent().getPackageName())) {
                    intent.addFlags(268435456);
                    displayContext.startActivity(intent);
                } else {
                    HwPCUtils.log(TAG, " restoreApp skip intent:" + intent + ",displayId:" + displayId);
                }
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "startActivity error.");
            } catch (FileUriExposedException e2) {
                HwPCUtils.log(TAG, "restoreApp FileUriExposedException.");
            }
        }
    }

    public void lockScreen(boolean lock) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "lockScreen checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        try {
            if (this.mMessenger != null) {
                Message msg = getMessage(8);
                msg.arg1 = lock;
                this.mMessenger.send(msg);
            }
        } catch (RemoteException e) {
            HwPCUtils.log("lockScreen", "RemoteException");
        }
    }

    /* access modifiers changed from: private */
    public void bdReportSameSrcStatus(boolean isConnected) {
        if (isConnected) {
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT, "same src is connected");
        } else {
            HwPCUtils.bdReport(this.mContext, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT, "same src is disconnected");
        }
    }

    /* access modifiers changed from: private */
    public void bdReportDiffSrcStatus(boolean isConnected) {
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
            int type = display.getType();
            if (type != 0) {
                switch (type) {
                    case 2:
                        Context context = this.mContext;
                        HwPCUtils.bdReport(context, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is HDMI");
                        break;
                    case 3:
                        Context context2 = this.mContext;
                        HwPCUtils.bdReport(context2, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is WIFI");
                        break;
                    case 4:
                        Context context3 = this.mContext;
                        HwPCUtils.bdReport(context3, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is OVERLAY");
                        break;
                    case 5:
                        Context context4 = this.mContext;
                        HwPCUtils.bdReport(context4, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is VIRTUAL");
                        break;
                }
            } else {
                Context context5 = this.mContext;
                HwPCUtils.bdReport(context5, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT, "Display<" + name + "> type is UNKNOWN");
            }
            Point size = new Point();
            display.getRealSize(size);
            Context context6 = this.mContext;
            HwPCUtils.bdReport(context6, IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT, "Display<" + name + "> width:" + size.x + " height:" + size.y);
            StringBuilder sb = new StringBuilder();
            sb.append(size.x);
            sb.append("");
            BigInteger biWidth = new BigInteger(sb.toString());
            BigInteger value = biWidth.gcd(new BigInteger(size.y + ""));
            Context context7 = this.mContext;
            HwPCUtils.bdReport(context7, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT, "Display<" + name + "> ratio:" + (size.x / value.intValue()) + ":" + (size.y / value.intValue()));
        }
    }

    public boolean isPackageRunningOnPCMode(String packageName, int uid) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "isPackageRunningOnPCMode checkCallingPermission failed " + Binder.getCallingPid());
            return false;
        }
        boolean ret = this.mAMS.isPackageRunningOnPCMode(packageName, uid);
        HwPCUtils.log(TAG, "isPackageRunningOnPCMode ret = " + ret);
        return ret;
    }

    public boolean isScreenPowerOn() {
        boolean z;
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
            return true;
        }
        synchronized (this.mScreenAccessLock) {
            z = this.mScreenPowerOn;
        }
        return z;
    }

    public void setScreenPower(boolean powerOn) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
            return;
        }
        setScreenPowerInner(powerOn, true);
    }

    /* access modifiers changed from: private */
    public void lightPhoneScreen() {
        setScreenPowerInner(true, false);
    }

    private void setScreenPowerInner(boolean powerOn, boolean checking) {
        synchronized (this.mScreenAccessLock) {
            HwPCUtils.log(TAG, "setScreenPower old=" + this.mScreenPowerOn + " new=" + powerOn);
            if (powerOn != this.mScreenPowerOn || !checking) {
                int val = 0;
                IBinder displayToken = SurfaceControl.getBuiltInDisplay(0);
                if (powerOn) {
                    val = 2;
                }
                SurfaceControl.setDisplayPowerMode(displayToken, val);
                this.mScreenPowerOn = powerOn;
            }
        }
    }

    public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "dispatchKeyEvent " + ke);
        try {
            if (this.mMessenger != null) {
                int keyCode = ke.getKeyCode();
                if (ke.getAction() == 0) {
                    if (keyCode == 62) {
                        this.mMessenger.send(getMessage(12));
                    } else if (keyCode != 118) {
                        switch (keyCode) {
                            case 3:
                                this.mMessenger.send(getMessage(10));
                                break;
                            case 4:
                                this.mMessenger.send(getMessage(11));
                                break;
                        }
                    } else {
                        this.mMessenger.send(getMessage(13));
                    }
                } else if (keyCode == 187) {
                    this.mMessenger.send(getMessage(9));
                }
            }
        } catch (RemoteException e) {
            HwPCUtils.log("dispatchKeyEvent", "RemoteException");
        }
    }

    /* access modifiers changed from: private */
    public boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    private void registerScreenOnEvent() {
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ALARM_ALERT_CONFLICT);
        try {
            this.mContext.registerReceiverAsUser(this.mAlarmClockReceiver, UserHandle.ALL, filter1, BROADCAST_PERMISSION, null);
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
            if (Settings.System.getIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 0, userId) == 0) {
                HwPCUtils.log(TAG, "enableFingerprintSlideSwitch");
                Settings.System.putIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
            }
        } catch (Exception e) {
            HwPCUtils.log(TAG, "enableFingerprintSlideSwitch " + e);
        }
    }

    /* access modifiers changed from: private */
    public void updateFingerprintSlideSwitch() {
        try {
            this.mAMS.updateFingerprintSlideSwitch();
        } catch (Exception e) {
            HwPCUtils.log(TAG, "updateFingerprintSlideSwitch " + e);
        }
    }

    /* access modifiers changed from: private */
    public void updateIMEWithHardKeyboardState(boolean switchToPcMode) {
        long ident = Binder.clearCallingIdentity();
        if (switchToPcMode) {
            try {
                this.mIMEWithHardKeyboardState = Settings.Secure.getInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
                HwPCUtils.log(TAG, "switch to PcMode, IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
                if (HwPCUtils.enabledInPad()) {
                    Settings.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0);
                } else {
                    Settings.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 1);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            HwPCUtils.log(TAG, "switch To PhoneMode, update IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
        }
    }

    private boolean isCalling() {
        this.mPhoneState = getPhoneState();
        return this.mPhoneState != 0;
    }

    private int getPhoneState() {
        int simCount = this.mTelephonyPhone.getPhoneCount();
        int phoneState = 0;
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

    /* access modifiers changed from: protected */
    public void showCallingToast(final int displayId) {
        final Context context;
        if (HwPCUtils.enabledInPad()) {
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
                            Toast unused = HwPCManagerService.this.mCallingToast = Toast.makeText(context, context.getResources().getString(33686085), 1);
                        } else {
                            Toast unused2 = HwPCManagerService.this.mCallingToast = Toast.makeText(context, context.getResources().getString(33686076), 1);
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
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "forceDisplayMode checkCallingPermission failed" + Binder.getCallingPid());
            return 0;
        }
        HwPCUtils.log(TAG, "forceDisplayMode mode:" + mode);
        switch (mode) {
            case 1005:
                return getOverScanMode();
            case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT:
                return setOverScanMode(0);
            case HwArbitrationDEFS.MSG_CELL_STATE_ENABLE:
                return setOverScanMode(1);
            case HwArbitrationDEFS.MSG_CELL_STATE_DISABLE:
                return setOverScanMode(2);
            default:
                return 0;
        }
    }

    public int getPCDisplayId() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "getPCDisplayId checkCallingPermission failed" + Binder.getCallingPid());
            return -1;
        } else if (HwPCUtils.isPcCastModeInServer()) {
            return get1stDisplay().mDisplayId;
        } else {
            HwPCUtils.log(TAG, "getPCDisplayId  is not in PC CastMode.");
            return -1;
        }
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
            return HwArbitrationDEFS.MSG_CELL_STATE_ENABLE;
        }
        if (mode == 2) {
            return HwArbitrationDEFS.MSG_CELL_STATE_DISABLE;
        }
        return HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT;
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
        if (displayInfo != null) {
            Display.Mode mode = displayInfo.getDefaultMode();
            if (mode != null) {
                return new int[]{mode.getPhysicalWidth(), mode.getPhysicalHeight()};
            }
        }
        return new int[0];
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
                for (int i = displays.length - 1; i >= 0; i--) {
                    if (displays[i] != null && HwPCUtils.isValidExtDisplayId(displays[i].getDisplayId())) {
                        return displays[i];
                    }
                }
            }
        }
        HwPCUtils.log(TAG, "getPcDisplay not find PCDisplay");
        return null;
    }

    public void setFocusedPCDisplayId(String reason) {
        if (this.mWindowManagerInternal == null) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        int extdisplayid = HwPCUtils.getPCDisplayID();
        if (this.mWindowManagerInternal != null && HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "setFocusedDisplayId extdisplayid = " + extdisplayid);
            this.mWindowManagerInternal.setFocusedDisplayId(extdisplayid, reason);
        }
    }

    public boolean isConnectExtDisplayFromPkg(String pkgName) {
        boolean isFirstDisplayConnectFromPkg = false;
        int firstDisplayId = get1stDisplay().mDisplayId;
        char c = 65535;
        if (firstDisplayId != -1) {
            if (!((pkgName.hashCode() == -463518712 && pkgName.equals("com.huawei.works")) ? false : true)) {
                isFirstDisplayConnectFromPkg = isConnectFromThirdApp(firstDisplayId) == 2;
            }
        }
        boolean isSecondDisplayConnectFromPkg = false;
        int secondDisplayId = get2ndDisplay().mDisplayId;
        if (secondDisplayId != -1) {
            if (pkgName.hashCode() == -463518712 && pkgName.equals("com.huawei.works")) {
                c = 0;
            }
            if (c == 0) {
                isSecondDisplayConnectFromPkg = isConnectFromThirdApp(secondDisplayId) == 2;
            }
        }
        if (isFirstDisplayConnectFromPkg || isSecondDisplayConnectFromPkg) {
            return true;
        }
        return false;
    }

    public void showImeStatusIcon(int iconResId, String pkgName) {
        try {
            HwPCUtils.log(TAG, String.format("PCMS showImeStatusIcon:%s,%s", new Object[]{Integer.valueOf(iconResId), pkgName}));
            if (validateImeCall(pkgName) && this.mMessenger != null) {
                Message message = Message.obtain();
                message.what = 14;
                Bundle bundle = new Bundle();
                bundle.putString(AwareIntelligentRecg.CMP_PKGNAME, pkgName);
                message.obj = bundle;
                message.arg1 = iconResId;
                this.mMessenger.send(message);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "PCMS showImeStatusIcon-RemoteException");
        }
    }

    public void hideImeStatusIcon(String pkgName) {
        try {
            HwPCUtils.log(TAG, "PCMS hideImeStatusIcon");
            if (validateImeCall(pkgName) && this.mMessenger != null) {
                this.mMessenger.send(getMessage(15));
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

    /* access modifiers changed from: private */
    public boolean isExclusiveKeyboardConnect(UEventObserver.UEvent event) {
        if (event != null) {
            String kbState = event.get("KB_STATE");
            if (kbState != null && kbState.equals(AwareJobSchedulerConstants.SERVICES_STATUS_CONNECTED)) {
                return true;
            }
        }
        return false;
    }

    private void registerBluetoothReceiver() {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        this.mFreq = 0;
        if (this.mWifiP2pManager != null) {
            if (this.mWifiP2pChannel == null) {
                this.mWifiP2pChannel = this.mWifiP2pManager.initialize(this.mContext, this.mHandler.getLooper(), null);
            }
            if (this.mWifiP2pChannel != null) {
                this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                    public void onGroupInfoAvailable(WifiP2pGroup info) {
                        if (info != null) {
                            int unused = HwPCManagerService.this.mFreq = info.getFrequence();
                            if (HwPCManagerService.this.mFreq >= HwPCManagerService.MIN_FREQ && HwPCManagerService.this.mFreq <= HwPCManagerService.MAX_FREQ) {
                                IntentFilter filter = new IntentFilter();
                                filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
                                HwPCManagerService.this.mContext.registerReceiver(HwPCManagerService.this.mWifiPCReceiver, filter);
                                boolean unused2 = HwPCManagerService.this.mIsNeedUnRegisterBluetoothReciver = true;
                                HwPCManagerService.this.mBluetoothReminderDialog.dismissDialog();
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (bluetoothAdapter != null) {
                                    boolean unused3 = HwPCManagerService.this.mBluetoothStateOnEnter = bluetoothAdapter.isEnabled();
                                    if (HwPCManagerService.this.mBluetoothStateOnEnter) {
                                        HwPCManagerService.this.mBluetoothReminderDialog.showCloseBluetoothTip(HwPCManagerService.this.mContext);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void unRegisterBluetoothReceiver() {
        if (this.mFreq >= MIN_FREQ && this.mFreq <= MAX_FREQ) {
            this.mContext.unregisterReceiver(this.mWifiPCReceiver);
            this.mBluetoothReminderDialog.dismissDialog();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled() && this.mBluetoothStateOnEnter) {
                this.mBluetoothReminderDialog.showOpenBluetoothTip(this.mContext);
            }
            this.mBluetoothStateOnEnter = false;
            this.mIsNeedUnRegisterBluetoothReciver = false;
            this.mFreq = 0;
        }
    }

    private boolean isWifiPCMode(int displayid) {
        boolean z = false;
        if (!(displayid == -1 || this.mDisplayManager == null)) {
            Display display = this.mDisplayManager.getDisplay(displayid);
            if (display != null) {
                if (display.getType() == 3) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int isConnectFromThirdApp(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null && display.getType() == 5) {
            if ("com.hpplay.happycast".equals(display.getOwnerPackageName())) {
                HwPCUtils.bdReport(this.mContext, 10061, "");
                return 1;
            } else if ("com.huawei.works".equals(display.getOwnerPackageName()) && ESHARE_DISPLAY_NAME.equals(display.getName())) {
                return 2;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void launchWeLink() {
        try {
            String encodedUri = URLEncoder.encode("ui://welink.wirelessdisplay/home", "utf-8");
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse("welink://works.huawei.com?uri=" + encodedUri));
            intent.setFlags(335544320);
            intent.putExtra("src", 203);
            intent.putExtra("target", 103);
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HwPCUtils.log(TAG, "launchWeLink ActivityNotFoundException error");
        } catch (UnsupportedEncodingException e2) {
            HwPCUtils.log(TAG, "launchWeLink UnsupportedEncodingException error");
        }
    }

    public void setPointerIconType(int iconId, boolean keep) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setCustomPointerIcon checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwInputManagerService.HwInputManagerLocalService inputManager = (HwInputManagerService.HwInputManagerLocalService) LocalServices.getService(HwInputManagerService.HwInputManagerLocalService.class);
        if (inputManager != null) {
            inputManager.setPointerIconTypeAndKeep(iconId, keep);
        }
    }

    public void setCustomPointerIcon(PointerIcon icon, boolean keep) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setCustomPointerIcon checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwInputManagerService.HwInputManagerLocalService inputManager = (HwInputManagerService.HwInputManagerLocalService) LocalServices.getService(HwInputManagerService.HwInputManagerLocalService.class);
        if (inputManager != null) {
            inputManager.setCustomPointerIconAndKeep(icon, keep);
        }
    }

    public void notifyDpState(boolean dpState) {
        HwPCUtils.log(TAG, "notifyDpState dpState = " + dpState);
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "notifyDpState checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        this.mHandler.removeMessages(21);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, Boolean.valueOf(dpState)));
    }

    /* access modifiers changed from: private */
    public HwPCMultiDisplaysManager.CastingDisplay get1stDisplay() {
        return this.mPcMultiDisplayMgr.get1stDisplay();
    }

    private HwPCMultiDisplaysManager.CastingDisplay get2ndDisplay() {
        return this.mPcMultiDisplayMgr.get2ndDisplay();
    }

    /* access modifiers changed from: private */
    public void showDPLinkErrorDialog(Context context, String tip) {
        dismissDpLinkErrorDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 33947691);
        this.mShowDpLinkErrorTipDialog = builder.setTitle(33685941).setPositiveButton(33685817, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        }).setMessage(String.format(tip, new Object[]{""})).create();
        this.mShowDpLinkErrorTipDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        this.mShowDpLinkErrorTipDialog.show();
        this.mShowDpLinkErrorTipDialog.getWindow().getAttributes().setTitle("ShowDpLinkErrorTipDialog");
    }

    /* access modifiers changed from: private */
    public void dismissDpLinkErrorDialog() {
        if (this.mShowDpLinkErrorTipDialog != null && this.mShowDpLinkErrorTipDialog.isShowing()) {
            this.mShowDpLinkErrorTipDialog.dismiss();
            this.mShowDpLinkErrorTipDialog = null;
        }
    }

    public void execVoiceCmd(Message message) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "execVoiceCmd checkCallingPermission failed " + Binder.getCallingPid());
        } else if (message == null) {
            HwPCUtils.log(TAG, "execVoiceCmd message = null");
        } else {
            this.mVAssistCmdExecutor.execVoiceCmd(message);
        }
    }

    private boolean shouldDropEventsForSendBroadcast() {
        long currTime = SystemClock.uptimeMillis();
        if (currTime - this.mPrevTimeForBroadcast <= 500) {
            return true;
        }
        this.mPrevTimeForBroadcast = currTime;
        return false;
    }

    /* access modifiers changed from: private */
    public void sendBroadcastForClearLighterDrawed() {
        HwPCUtils.log(TAG, "sendBroadcastForClearLighterDrawed");
        if (shouldDropEventsForSendBroadcast()) {
            HwPCUtils.log(TAG, "ignore because of frequent events");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_CLEAR_LIGHTER_DRAWED);
        intent.setPackage("com.huawei.desktop.systemui");
        this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_CLEAR_LIGHTER_DRAWED);
    }

    private static boolean isDapKey(int keyCode) {
        return keyCode == 20 || keyCode == 269 || keyCode == 271 || keyCode == 21 || keyCode == 22 || keyCode == 19 || keyCode == 268 || keyCode == 270 || keyCode == 23;
    }

    private static boolean isVolumeKey(KeyEvent ev) {
        int keyCode = ev.getKeyCode();
        return keyCode == 25 || keyCode == 24;
    }

    private static boolean keyForPPTSwitch(KeyEvent ev) {
        if (isDapKey(ev.getKeyCode())) {
            return true;
        }
        if (!isVolumeKey(ev) || ev.getAction() == 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void filterScrollForPCMode() {
        if (HwPCUtils.isPcCastModeInServer() && HwWindowManager.hasLighterViewInPCCastMode()) {
            shouldInterceptInputEvent(null, true);
        }
    }

    private boolean isFullScreenApp(Rect bounds) {
        if (bounds == null) {
            return true;
        }
        if (this.mPCDisplayInfo != null && bounds.left == 0 && bounds.top == 0 && bounds.right == this.mPCDisplayInfo.logicalWidth && bounds.bottom == this.mPCDisplayInfo.logicalHeight) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean shouldSendBroadcastForClearLighterDrawed(KeyEvent ev, boolean forScroll) {
        boolean sendBroadcastForClearDrawed = false;
        HwPCVAssistCmdExecutor.WindowStateData wsd = new HwPCVAssistCmdExecutor.WindowStateData();
        if (!HwPCVAssistCmdExecutor.specialAppFocused(get1stDisplay().mDisplayId, wsd, true, true) || !isFullScreenApp(wsd.bounds)) {
            HwPCUtils.log(TAG, "shouldInterceptInputEvent ignore it when special app not focused or not full screen");
            return false;
        }
        if (ev != null) {
            if (keyForPPTSwitch(ev)) {
                sendBroadcastForClearDrawed = true;
            }
        } else if (forScroll) {
            sendBroadcastForClearDrawed = true;
        }
        HwPCUtils.log(TAG, "shouldInterceptInputEvent sendBroadcastForClearDrawed = " + sendBroadcastForClearDrawed);
        return sendBroadcastForClearDrawed;
    }

    public boolean shouldInterceptInputEvent(KeyEvent ev, boolean forScroll) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "shouldInterceptInputEvent checkCallingPermission failed " + Binder.getCallingPid());
            return true;
        } else if (ev == null && !forScroll) {
            HwPCUtils.log(TAG, "shouldInterceptInputEvent ev = null");
            return true;
        } else if (ev == null || isDapKey(ev.getKeyCode()) || KeyEvent.isSystemKey(ev.getKeyCode())) {
            this.mHandler.removeMessages(24);
            Message msg = this.mHandler.obtainMessage(24);
            msg.obj = ev;
            msg.arg1 = forScroll;
            this.mHandler.sendMessage(msg);
            return false;
        } else {
            HwPCUtils.log(TAG, "shouldInterceptInputEvent only dap or volume for PPT accept when lighter view appear");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void setPcCastingDisplayId(int displayId) {
        SystemProperties.set("hw.pc.casting.displayid", String.valueOf(displayId));
    }

    public void ignoreInjectEventForFreeMouse(boolean ignore) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "ignoreInjectEventForFreeMouse checkCallingPermission failed " + Binder.getCallingPid());
            return;
        }
        this.mIgnoreInjectEvent = ignore;
    }
}
