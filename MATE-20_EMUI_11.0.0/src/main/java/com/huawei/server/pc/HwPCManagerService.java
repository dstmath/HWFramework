package com.huawei.server.pc;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.HwRecentTaskInfo;
import android.app.HwRioClientInfo;
import android.app.IHwRioRuleCb;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
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
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.wm.HwHiCarMultiWindowManager;
import com.android.server.wm.HwPCMultiWindowManager;
import com.android.server.wm.TaskRecordEx;
import com.android.server.wm.WindowManagerInternalEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.NotificationEx;
import com.huawei.android.app.PendingIntentEx;
import com.huawei.android.app.TaskStackListenerEx;
import com.huawei.android.app.UserSwitchObserverExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.hardware.display.DisplayManagerEx;
import com.huawei.android.hardware.display.DisplayManagerInternalEx;
import com.huawei.android.hardware.display.WifiDisplayExt;
import com.huawei.android.hardware.display.WifiDisplayStatusExt;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.net.wifi.p2p.WifiP2pGroupExt;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.IRemoteCallbackExt;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UEventObserverExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerInternalEx;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.InputDeviceEx;
import com.huawei.android.view.InputEventEx;
import com.huawei.android.view.KeyEventEx;
import com.huawei.android.view.MotionEventEx;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
import com.huawei.server.ServiceThreadExt;
import com.huawei.server.UiThreadEx;
import com.huawei.server.hwmultidisplay.HwMultiDisplayUtils;
import com.huawei.server.hwmultidisplay.audio.HwMultiDisplayAudioManager;
import com.huawei.server.hwmultidisplay.castplus.AirSharingManager;
import com.huawei.server.hwmultidisplay.hicar.HiCarManager;
import com.huawei.server.hwmultidisplay.hicar.HwRioViewManager;
import com.huawei.server.hwmultidisplay.power.HwMultiDisplayPowerManager;
import com.huawei.server.hwmultidisplay.videocall.HwVideoCallCastManager;
import com.huawei.server.hwmultidisplay.windows.HwWindowsCastManager;
import com.huawei.server.pc.HwPCMultiDisplaysManager;
import com.huawei.server.pc.decision.DecisionUtil;
import com.huawei.server.pc.vassist.HwPCVAssistCmdExecutor;
import com.huawei.server.pc.whiltestrategy.WhiteListAppStrategyManager;
import com.huawei.server.policy.PhoneWindowManagerEx;
import com.huawei.server.statusbar.StatusBarManagerServiceEx;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.os.HwProtectAreaManager;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCManagerService extends DefaultHwPCManagerService {
    private static final String ACTION_CLEAR_LIGHTER_DRAWED = "com.android.server.pc.action.clear_lighter_drawed";
    private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_NOTIFY_CHANGE_STATUS_BAR = "com.android.server.pc.action.CHANGE_STATUS_BAR";
    private static final String ACTION_NOTIFY_DISCONNECT = "com.android.server.pc.action.DISCONNECT";
    private static final String ACTION_NOTIFY_OPEN_EASY_PROJECTION = "com.android.server.pc.action.EASY_PROJECTION";
    private static final String ACTION_NOTIFY_PROJECTION_CAPSULE_STATUS_BAR = "com.android.server.action.PROJECTION_CAPSULE_STATUS_BAR";
    private static final String ACTION_NOTIFY_SHOW_MK = "com.android.server.pc.action.SHOW_MK";
    private static final String ACTION_NOTIFY_SWITCH_MODE = "com.android.server.pc.action.SWITCH_MODE";
    private static final String ACTION_NOTIFY_UNINSTALL_APP = "com.android.server.pc.action.UNINSTALL_APP";
    private static final String ACTION_WIFI_DISPLAY_CASTING = "com.huawei.hardware.display.action.WIFI_DISPLAY_CASTING";
    private static final int AGS3_KEYBOARD_PID = 4265;
    private static final int AGS3_KEYBOARD_VID = 4817;
    private static final String ALARM_ALERT_CONFLICT = "huawei.deskclock.ALARM_ALERT_CONFLICT";
    private static final String AOD_VIRTUAL_DISPLAY = "AodVirtualDisplay";
    private static final long APP_SYSTEM_RELAUNCH_DELAY = 1000;
    private static final int BACH3_KEYBOARD_PID = 4254;
    private static final int BACH3_KEYBOARD_VID = 4817;
    private static final int BIT_KEYBOARD = 2;
    private static final int BIT_MOUSE = 1;
    private static final int BIT_NONE = 0;
    private static final int BIT_TOUCHSCREEN = 4;
    private static final String BROADCAST_PERMISSION = "com.huawei.deskclock.broadcast.permission";
    private static final long BROADCAST_SEND_INTERVAL = 500;
    private static final String CAST_PLUS_VIRTUALDISPLAY_NAME = "CastPlusDisplay";
    private static final String CHANGE_POWERMODE_PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
    private static final int CHECK_HARD_BROAD_DELAY = 2000;
    private static final int CMR_KEYBOARD_PID = 4817;
    private static final boolean DEBUG = LogEx.getLogHWInfo();
    private static final int DEFAULT_CAPACITY_LIST = 10;
    private static final String DEMO_VENDOR = "demo";
    private static final boolean DESKTOP_ENABLED = SystemPropertiesEx.getBoolean("ro.config.hw_emui_desktop_mode", false);
    private static final int DEVICE_MODE_SHOW_DEFAULT = 0;
    private static final int DEVICE_MODE_SHOW_LASERPOINTER = 1;
    private static final int DEVICE_MODE_TEST = 2;
    private static final long DISPLAY_ADD_DELAY = 1000;
    private static final String DISPLAY_ID = "display_id";
    private static final String DP_LINK_STATE_AUX_FAILED = "AUX_FAILED";
    private static final String DP_LINK_STATE_CABLE_IN = "CABLE_IN";
    private static final String DP_LINK_STATE_CABLE_OUT = "CABLE_OUT";
    private static final String DP_LINK_STATE_EDID_FAILED = "EDID_FAILED";
    private static final String DP_LINK_STATE_HDCP_FAILED = "HDCP_FAILED";
    private static final String DP_LINK_STATE_LINK_FAILED = "LINK_FAILED";
    private static final String DP_LINK_STATE_LINK_RETRAINING = "LINK_RETRAINING";
    private static final String DP_LINK_STATE_MULTI_HPD = "MULTI_HPD";
    private static final String DP_LINK_STATE_SAFE_MODE = "SAFE_MODE";
    private static final String DRAG_DROP_CACHE_DIR = "/storage/emulated/0/Android/data/com.huawei.pcassistant/cache/.dragfile_template/";
    private static final int DREAMS_DISENABLED = 0;
    private static final int DREAMS_ENABLED = 1;
    private static final int DREAMS_INVALID = -1;
    private static final String ESHARE_DISPLAY_NAME = "eshare mirror";
    private static final int EXCLUSIVE_BLUETOOTH_KEYBOARD_CONNECTED = 0;
    private static final String EXCLUSIVE_DP_LINK = "DEVPATH=/devices/virtual/dp/source";
    private static final int EXPLORER_BIND_ERROR = 2;
    private static final int EXPLORER_LAUNCH_DELAY = 4000;
    private static final String EXPLORER_SERVICE_NAME = "HwPCExplorer";
    private static final int FINGERPRINT_SLIDE_OFF = 0;
    private static final int FINGERPRINT_SLIDE_ON = 1;
    private static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    private static final int HELP_MSG_PROMPTED = 100;
    private static final String HELP_MSG_PROMPT_STATE = "help-msg-prompt_number";
    private static final int HELP_MSG_PROMPT_STATE_DEFAULT = 0;
    private static final String HW_PCM = "HW_PCM";
    private static final String INIT_BUFFER = "0";
    private static final int INVALID_ARG = -1;
    private static final boolean IS_NOTCH = (!TextUtils.isEmpty(SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR)));
    private static final boolean IS_TABLET = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", BuildConfig.FLAVOR));
    private static final int KEEP_RECORD_TIMEOUT = 180000;
    private static final int KEEP_RECORD_TIMEOUT_FOR_HICAR = 10000;
    private static final int[] KEYBOARD_PRODUCT_ID = {4817, SCM_KEYBOARD_PID, MRX_KEYBOARD_PID, AGS3_KEYBOARD_PID, BACH3_KEYBOARD_PID, KRJ_KEYBOARD_PID, SCMR_KEYBOARD_PID, WAG_KEYBOARD_PID, WGR_KEYBOARD_PID};
    private static final int[] KEYBOARD_VENDOR_ID = {KIHITECH_KEYBOARD_VID, KIHITECH_KEYBOARD_VID, 4817, 4817, 4817, 4817, 4817, 4817, 4817};
    private static final String KEY_BEFORE_BOOT_ANIM_TIME = "before_boot_anim_time";
    private static final String KEY_CURRENT_DISPLAY_UNIQUEID = "current_display_uniqueId";
    private static final String KEY_IS_DISSCONNECT = "is_dissconnect";
    private static final String KEY_IS_WIRELESS_MODE = "is_wireless_mode";
    private static final int KIDS_MODE_NO_RUNNING = 0;
    private static final String KIDS_MODE_PACKAGES_NAME = "com.huawei.kidsmode";
    private static final int KIDS_MODE_RUNNING = 1;
    private static final String KIDS_MODE_RUNNING_SETTINGS = "hwkidsmode_running";
    private static final int KIHITECH_KEYBOARD_VID = 1455;
    private static final int KRJ_KEYBOARD_PID = 4260;
    private static final int KRJ_KEYBOARD_VID = 4817;
    private static final int LAUNCH_GUIDE_DELAY = 500;
    private static final int LAUNCH_TASK_ID = 1;
    private static final int MAX_FREQ = 2472;
    private static final int MIN_FREQ = 2412;
    private static final String MMI_TEST_PROPERTY = "runtime.mmitest.isrunning";
    private static final int MRX_KEYBOARD_PID = 4253;
    private static final int MRX_KEYBOARD_VID = 4817;
    private static final int MSG_APP_RUNNING_ON_OTHER_DISPLAY = 101;
    private static final int MSG_CLEAR_DRAG_DROP_CACHE_DIR = 27;
    private static final int MSG_CLEAR_LIGHTER_DRAWED = 24;
    private static final int MSG_CLEAR_REOCRD_TASKID = 30;
    private static final int MSG_CLOSE_CLIENT_TOP_WINDOW = 4;
    private static final int MSG_DISMISS_CLIENT_TASK_VIEW = 6;
    static final int MSG_DISPLAY_ADDED = 6;
    private static final int MSG_DISPLAY_CHANGED = 7;
    private static final int MSG_DISPLAY_REMOVED = 8;
    private static final int MSG_DP_LINK_ERROR = 22;
    private static final int MSG_DP_LINK_STATE_CABLE_OUT = 25;
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
    private static final int MSG_RECORD_TASKID = 28;
    private static final int MSG_REFRESH_NTFS = 5;
    private static final int MSG_REGISTER_ALARM = 15;
    private static final int MSG_RELAUNCH_IME = 14;
    private static final int MSG_RESTART_WECHAT = 29;
    private static final int MSG_RESTORE_APP = 10;
    private static final int MSG_SCREENSHOT_PC_DISPLAY = 3;
    private static final int MSG_SEND_PROJECTION_CAPSULE = 31;
    private static final int MSG_SET_FOCUS_DISPLAY = 17;
    private static final int MSG_SET_PROJ_MODE = 4;
    private static final int MSG_SHOW_CLIENT_STARTUP_MENU = 2;
    private static final int MSG_SHOW_CLIENT_TASK_VIEW = 5;
    private static final int MSG_SHOW_CLIENT_TOPBAR = 1;
    private static final int MSG_SHOW_HELP_TO_PC_MODEL = 26;
    private static final int MSG_SHOW_TASK_RECENT = 22;
    private static final int MSG_SHOW_TASK_SIDE_PREVIEW = 23;
    private static final int MSG_START_RESTORE_APPS = 9;
    private static final int MSG_START_VOICE_ASSISTANT = 13;
    private static final int MSG_SWITCH_AUDIO_OUTPUT_TO_EXDISPLAY = 102;
    private static final int MSG_SWITCH_AUDIO_OUTPUT_TO_PAD = 103;
    private static final int MSG_SWITCH_USER = 12;
    private static final int MSG_TASK_CREATED = 16;
    private static final int MSG_TASK_MOVE_TO_BACK = 20;
    private static final int MSG_TASK_MOVE_TO_FRONT = 18;
    private static final int MSG_TASK_PROFILE_LOCKED = 19;
    private static final int MSG_TASK_REMOVED = 17;
    private static final int MSG_UNINSTALL_APP = 16;
    private static final int MSG_UPDATE_CFG = 18;
    private static final int MSG_USER_ACTIVITY_ON_DESKTOP = 7;
    private static final int NOTCH_STATUS_DISABLED = 0;
    private static final int NOTCH_STATUS_INVALID = -1;
    private static final int NOTIFY_SWITCH_PROJ_ID = 0;
    private static final int NOTIFY_VIRTUAL_M_K_ID = 1;
    private static final int OOBE_GUIDE_EVER_STARTED = 100;
    private static final String OOBE_START_GUIDE_PACKAGE = "com.huawei.hwstartupguide";
    private static final int OPEN_SAVE_POWER = 3;
    private static final String PERMISSION_BROADCAST_CHANGE_STATUS_BAR = "com.huawei.permission.pc.CHANGE_STATUS_BAR";
    private static final String PERMISSION_BROADCAST_CLEAR_LIGHTER_DRAWED = "com.huawei.permission.pc.CLEAR_LIGHTER_DRAWED";
    private static final String PERMISSION_BROADCAST_PROJECTION_CAPSULE_STATUS_BAR = "com.huawei.permission.pc.BackgroundProjectionModeLayout";
    private static final String PERMISSION_BROADCAST_SWITCH_MODE = "com.huawei.permission.SWITCH_MODE";
    private static final String PERMISSION_PC_MANAGER_API = "com.huawei.permission.PC_MANAGER_API";
    private static final String PKG_NAME_DATA = "pkgName";
    private static final String POWER_MODE = "power_mode";
    private static final String PRIVACY_PROJECTION_MODE = "multi_display_privacy_projection_mode";
    private static final String PROJECTION_STATE = "projection_state";
    private static final int READ_BUFFER_LENGTH = 1024;
    private static final int READ_BUFFER_SUCCESS = 0;
    private static final int RECENT_TASKS_FLAGS = 1;
    private static final int RECENT_TASKS_MAX_NUM = 20;
    private static final int RECOVER_SHOWTOUCH_STATUS_TIMEOUT = 1000;
    private static final int RELAUNCH_IME_DELAY = 2000;
    private static final int RESO_1080P = 1920;
    private static final int RE_BIND_SERVICE_DELAY = 800;
    private static final int ROTATION_SWITCH_CLOSE = 0;
    private static final int ROTATION_SWITCH_OPEN = 1;
    private static final int RUNNING_TASK_NUM = 1;
    private static final int SCMR_KEYBOARD_PID = 4266;
    private static final int SCMR_KEYBOARD_VID = 4817;
    private static final int SCM_KEYBOARD_PID = 4096;
    private static final String SCREEN_POWER_DEVICE = "/sys/devices/virtual/dp/power/lcd_power";
    private static final String SCREEN_POWER_OFF = "0";
    private static final String SCREEN_POWER_ON = "1";
    private static final String SHOW_CONTROL_PANEL = "SHOW_CONTROL_PANEL";
    private static final int SHOW_TOUCHES_DISABLED = 0;
    private static final int START_ACTIVITY_INTERVAL = 800;
    private static final int START_RESTORE_APPS_DELAY_TIME = 3000;
    private static final int START_RESTORE_APPS_DELAY_TIME_FOR_HICAR = 1500;
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
    private static final int TWO_SEC_DELAY = 2000;
    private static final int TYPE_AIRSHARING = 3;
    private static final int TYPE_HPPCAT = 1;
    private static final int TYPE_UNKNOWN = -1;
    private static final int TYPE_WELINK = 2;
    private static final String VENDOR_AND_COUNTRY = "VENDOR_AND_COUNTRY";
    private static final int WAG_KEYBOARD_PID = 4267;
    private static final int WAG_KEYBOARD_VID = 4817;
    private static final int WAKE_REASON_APPLICATION = 2;
    private static final int WGR_KEYBOARD_PID = 4274;
    private static final int WGR_KEYBOARD_VID = 4817;
    public static final int WINDOW_LAYOUT_MODE_MASK = 15;
    private static final String WIRELESS_PROJECTION_STATE = "wireless_projection_state";
    private static final int devicetestmode = 2;
    private final String DEVICE_PROVISIONED_URI = "content://settings/global/device_provisioned";
    private final String SCREEN_OF_TIMEOUT_URI = "content://settings/system/screen_off_timeout";
    private final String USER_SETUP_COMPLETE_URI = "content://settings/secure/user_setup_complete";
    private boolean beginShow = false;
    private boolean isNeedEixtDesktop = false;
    private boolean isNeedEnterDesktop = false;
    private HwActivityManagerService mAMS;
    private AirSharingManager mAirSharingManager = null;
    private final BroadcastReceiver mAlarmClockReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass14 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwPCUtils.log(HwPCManagerService.TAG, "receive clock alarm");
            HwPCManagerService.this.setScreenPower(true);
        }
    };
    private final AlarmManager.OnAlarmListener mAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass13 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwPCManagerService.this.mHandler.sendEmptyMessage(11);
        }
    };
    private AlarmManager mAlarmManager;
    private float mAxisX = 0.0f;
    private float mAxisY = 0.0f;
    private BluetoothReminderDialog mBluetoothReminderDialog;
    private boolean mBluetoothStateOnEnter = false;
    private Toast mCallingToast;
    private CastMode mCastModeForIntentList = CastMode.INVALID;
    private final ServiceConnection mConnExplorer = new ServiceConnection() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceConnected");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "explorer onServiceDisconnected");
            HwPCDataReporter.getInstance().reportFailToConnEvent(2, HwPCManagerService.EXPLORER_SERVICE_NAME, HwPCManagerService.this.mPCDisplayInfo);
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass2.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.get1stDisplay().mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        Intent intent = new Intent();
                        intent.putExtra(HwPCManagerService.KEY_BEFORE_BOOT_ANIM_TIME, HwPCManagerService.this.mPCBeforeBootAnimTime);
                        intent.putExtra(HwPCManagerService.KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
                        intent.putExtra(HwPCManagerService.KEY_CURRENT_DISPLAY_UNIQUEID, HwPCManagerService.this.getPcDisplayInfo() != null ? HwPCManagerService.this.getPcDisplayInfo().getUniqueId() : BuildConfig.FLAVOR);
                        intent.setComponent(HwPCManagerService.this.mExplorerComponent);
                        HwPCManagerService.this.mContext.bindService(intent, HwPCManagerService.this.mConnExplorer, 1);
                    }
                }
            }, 800);
        }
    };
    private final ServiceConnection mConnSysUI = new ServiceConnection() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceConnected");
            if (HwPCManagerService.this.get1stDisplay().mDisplayId != -1) {
                HwPCManagerService hwPCManagerService = HwPCManagerService.this;
                hwPCManagerService.updateDisplayOverrideConfiguration(hwPCManagerService.get1stDisplay().mDisplayId, 2000);
                HwPCManagerService.this.relaunchIMEDelay(2000);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(HwPCManagerService.TAG, "SysUI onServiceDisconnected");
            HwPCDataReporter.getInstance().reportFailToConnEvent(1, HwPCManagerService.SYSTEMUI_SERVICE_NAME, HwPCManagerService.this.mPCDisplayInfo);
            HwPCManagerService.this.mHandler.postDelayed(new Runnable() {
                /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HwPCUtils.isValidExtDisplayId(HwPCManagerService.this.get1stDisplay().mDisplayId) && HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                        Intent intent = new Intent();
                        intent.putExtra(HwPCManagerService.KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
                        intent.setComponent(HwPCManagerService.this.mSystemUIComponent);
                        HwPCManagerService.this.mContext.bindService(intent, HwPCManagerService.this.mConnSysUI, 1);
                    }
                }
            }, 800);
        }
    };
    private int mConnectedInputDevices = 0;
    private Context mContext;
    private final ComponentName mControlPanelComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.projectionpanel.ControlPanelService");
    private final DisplayDriverCommunicator mDDC;
    private UEventObserverExt mDPLinkStateObserver = new UEventObserverExt() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass18 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            if (event != null) {
                String state = event.get("DP_LINK_EVENT");
                if (!TextUtils.isEmpty(state)) {
                    String tip1 = HwPCManagerService.this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("pc_dp_link_error1"));
                    String tip2 = HwPCManagerService.this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("pc_dp_link_error2"));
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
                                c = '\b';
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
                            HwPCManagerService.this.beginShow = true;
                            break;
                        case 1:
                            HwPCManagerService.this.mHandler.removeMessages(22);
                            HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(HwPCManagerService.MSG_DP_LINK_STATE_CABLE_OUT));
                            HwPCManagerService.this.beginShow = false;
                            break;
                        case 2:
                        case 3:
                            tip = tip1;
                            break;
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case '\b':
                            tip = tip2;
                            break;
                        default:
                            HwPCManagerService.this.beginShow = false;
                            break;
                    }
                    if (tip != null && HwPCManagerService.this.beginShow) {
                        HwPCManagerService.this.mHandler.removeMessages(22);
                        HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(22, tip));
                        HwPCManagerService.this.beginShow = false;
                    }
                }
            }
        }
    };
    private DisplayManager mDisplayManager;
    private int mDreamsEnabledSetting = -1;
    private AlertDialog mEnterDesktopAlertDialog = null;
    private AlertDialog mExitDesktopAlertDialog = null;
    private final ComponentName mExplorerComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.services.ExplorerService");
    private int mFreq;
    final LocalHandler mHandler;
    final ServiceThreadExt mHandlerThread;
    private volatile boolean mHasSwitchNtf = false;
    private final ComponentName mHelpMsgComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.PCModeRecommendActivity");
    private HiCarManager mHiCarManager = null;
    private IBinder mIBinderAudioService;
    private int mIMEWithHardKeyboardState = 1;
    private final InputManager.InputDeviceListener mInputDeviceListener = new InputManager.InputDeviceListener() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass12 */

        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceAdded(int deviceId) {
            HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceAdded, deviceId:" + deviceId + ", mConnectedInputDevices: " + HwPCManagerService.this.mConnectedInputDevices);
            InputDevice device = InputDevice.getDevice(deviceId);
            HwPCManagerService hwPCManagerService = HwPCManagerService.this;
            hwPCManagerService.mConnectedInputDevices = hwPCManagerService.mConnectedInputDevices | HwPCManagerService.whichInputDevice(device);
            if ((HwPCManagerService.whichInputDevice(device) & 1) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10004, BuildConfig.FLAVOR);
            }
            if ((HwPCManagerService.whichInputDevice(device) & 2) != 0) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10005, BuildConfig.FLAVOR);
            }
            if ((HwPCManagerService.whichInputDevice(device) & 4) != 0 && !HwPCUtils.isHiCarCastMode()) {
                HwPCUtils.mTouchDeviceID = deviceId;
                HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceAdded, mTouchDeviceID = " + HwPCUtils.mTouchDeviceID);
            }
            if (HwPCUtils.enabledInPad()) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("DEVICE", HwPCManagerService.getDeviceDescription(device));
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

        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceRemoved(int deviceId) {
            int[] devices;
            HwPCUtils.log(HwPCManagerService.TAG, "onInputDeviceRemoved, deviceId:" + deviceId + ", mConnectedInputDevices: " + HwPCManagerService.this.mConnectedInputDevices);
            int connectedInputDevices = 0;
            for (int i : InputDevice.getDeviceIds()) {
                connectedInputDevices |= HwPCManagerService.whichInputDevice(InputDevice.getDevice(i));
            }
            if (HwPCUtils.mTouchDeviceID == deviceId && !HwPCUtils.isHiCarCastMode()) {
                HwPCUtils.mTouchDeviceID = -1;
                HwPCManagerService.this.relaunchIMEIfNecessary();
            }
            HwPCManagerService.this.mConnectedInputDevices = connectedInputDevices;
            if (HwPCUtils.enabledInPad()) {
                try {
                    if (HwPCManagerService.this.mKeyboardInfo.containsKey(Integer.valueOf(deviceId))) {
                        JSONObject jo = (JSONObject) HwPCManagerService.this.mKeyboardInfo.get(Integer.valueOf(deviceId));
                        jo.put("END_TIME", System.currentTimeMillis());
                        HwPCUtils.log(HwPCManagerService.TAG, "report msg:" + jo.toString());
                        HwPCManagerService.this.mKeyboardInfo.remove(Integer.valueOf(deviceId));
                        HwPCUtils.bdReport(HwPCManagerService.this.mContext, (int) Constant.REPORT_CONNECT_PAD_KEYBORAD, jo.toString());
                    }
                } catch (JSONException e) {
                    HwPCUtils.log(HwPCManagerService.TAG, "JSONException");
                }
            }
        }

        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceChanged(int deviceId) {
        }
    };
    private InputManager mInputManager;
    private final ComponentName mInstructionComponent = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.PCHelpInformationActivity");
    private final ComponentName mInstructionComponentWirelessEnabled = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.desktopinstruction.EasyProjection");
    private ArrayList<Intent> mIntentList = new ArrayList<>();
    private boolean mIsAppOpenTwice = false;
    private boolean mIsDisplayAddedAfterSwitch = false;
    private boolean mIsDisplayLargerThan1080p = false;
    private boolean mIsNeedUnRegisterBluetoothReciver = false;
    private boolean mIsShopDemoVersion = false;
    private boolean mIsSkipBootAnimation = false;
    private boolean mIsWifiBroadDone = false;
    private ConcurrentHashMap<Integer, JSONObject> mKeyboardInfo = new ConcurrentHashMap<>();
    private KeyguardManager mKeyguardManager;
    private long mLastTimeOpen = 0;
    private int mLockScreenTimeout = 0;
    private final ArrayList<Messenger> mMessengers = new ArrayList<>();
    private HwMultiDisplayAudioManager mMultiDisplayAudioMgr;
    private final ArrayList<Intent> mNeedRestartIntentList = new ArrayList<>();
    private NotificationManager mNm;
    private int mNotchStatus = -1;
    private final BroadcastReceiver mNotifyReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "mNotifyReceiver received a null intent");
                return;
            }
            String action = intent.getAction();
            if (HwPCManagerService.ACTION_NOTIFY_SWITCH_MODE.equals(action)) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10003, BuildConfig.FLAVOR);
                HwPCManagerService.this.collapsePanels();
                HwPCManagerService.this.switchProjMode();
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                HwPCManagerService.this.refreshNotifications();
            } else if (HwPCManagerService.ACTION_NOTIFY_SHOW_MK.equals(action)) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10047, BuildConfig.FLAVOR);
                HwPCManagerService.this.collapsePanels();
                HwPCManagerService.this.sendShowMkMessage();
            } else if (HwPCManagerService.ACTION_NOTIFY_DISCONNECT.equals(action)) {
                if (HwPCManagerService.this.mDisplayManager != null) {
                    HwPCManagerService hwPCManagerService = HwPCManagerService.this;
                    if (hwPCManagerService.isConnectFromThirdApp(hwPCManagerService.get1stDisplay().mDisplayId) == 2) {
                        HwPCManagerService.this.launchWeLink();
                        HwPCManagerService.this.collapsePanels();
                    }
                }
                if (HwPCManagerService.this.mDisplayManager != null) {
                    HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10048, BuildConfig.FLAVOR);
                    HwPCManagerService.this.disconnectWifiProjection();
                }
                HwPCManagerService.this.collapsePanels();
            } else if (HwPCManagerService.ACTION_NOTIFY_OPEN_EASY_PROJECTION.equals(action)) {
                HwPCManagerService.this.sendOpenEasyProjectionMessage();
                HwPCManagerService.this.collapsePanels();
            } else if (HwPCManagerService.ACTION_NOTIFY_UNINSTALL_APP.equals(action)) {
                String pkgName = intent.getStringExtra("PACKAGE_NAME");
                if (pkgName != null) {
                    HwPCManagerService.this.sendUninstallAppMessage(pkgName);
                }
            } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(action) && HwPCUtils.getIsWifiMode()) {
                WifiDisplayStatusExt statusExt = new WifiDisplayStatusExt();
                statusExt.init(intent);
                if (!statusExt.isEmpty()) {
                    WifiDisplayExt wifiDisplayExt = statusExt.getActiveDisplay();
                    if (wifiDisplayExt != null) {
                        HwPCManagerService.this.mWireLessDeviceName = String.format(context.getResources().getString(HwPartResourceUtils.getResourceId("notification_pc_connected")), wifiDisplayExt.getDeviceName());
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
    private DisplayInfoEx mPCDisplayInfo = null;
    private HwPCMkManager mPCMkManager;
    PCSettingsObserver mPCSettingsObserver;
    private int mPadDesktopModeLockScreenTimeout = 0;
    private int mPadLockScreenTimeout = 0;
    private boolean mPadPCDisplayIsRemoved = false;
    private HwPCMultiDisplaysManager mPcMultiDisplayMgr;
    private int mPhoneState = 0;
    PointerEventListenerExt mPointerListener = new PointerEventListenerExt();
    private PhoneWindowManagerEx mPolicy;
    private long mPrevTimeForBroadcast = SystemClock.uptimeMillis();
    volatile HwPCUtils.ProjectionMode mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
    boolean mProvisioned = false;
    private int mRelaunchTaskId = -1;
    boolean mRestartAppsWhenUnlocked = false;
    private int mRotationSwitch = 1;
    private int mRotationValue = 0;
    private AlertDialog mShowDpLinkErrorTipDialog = null;
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass8 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String act;
            if (intent != null && (act = intent.getAction()) != null && "android.intent.action.ACTION_SHUTDOWN".equals(act) && HwPCUtils.isPcCastModeInServer()) {
                HwPCManagerService.this.restoreRotationInPad();
                if (HwPCUtils.enabledInPad()) {
                    HwPCUtils.log(HwPCManagerService.TAG, "receive shut down broadcast , IME With Hard Keyboard State:" + HwPCManagerService.this.mIMEWithHardKeyboardState);
                    SettingsEx.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", HwPCManagerService.this.mIMEWithHardKeyboardState);
                }
                HwPCManagerService.this.restoreDreamSettingInPad();
            }
        }
    };
    private boolean mSupportOverlay = SystemPropertiesEx.getBoolean("hw_pc_support_overlay", false);
    private List<Integer> mSupportProjectionList = new ArrayList(10);
    private boolean mSupportTouchPad = SystemPropertiesEx.getBoolean("hw_pc_support_touchpad", true);
    private final ComponentName mSystemUIComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.SystemUIService");
    private TaskStackListenerEx mTaskStackListener = new TaskStackListenerEx() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass20 */

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
            HwPCUtils.log(HwPCManagerService.TAG, "onTaskCreated taskId=" + taskId);
            HwPCManagerService.this.sendToMessengers(16, taskId, -1, componentName);
        }

        public void onTaskRemoved(int taskId) throws RemoteException {
            HwPCManagerService.super.onTaskRemoved(taskId);
            HwPCUtils.log(HwPCManagerService.TAG, "onTaskRemoved taskId=" + taskId);
            Message msg = HwPCManagerService.this.mHandler.obtainMessage(HwPCManagerService.MSG_RESTART_WECHAT);
            msg.arg1 = taskId;
            HwPCManagerService.this.mHandler.sendMessage(msg);
            HwPCManagerService.this.sendToMessengers(17, taskId, -1, null);
            if (HwPCUtils.isHiCarCastMode()) {
                HwHiCarMultiWindowManager.getInstance().onTaskRemoved(taskId);
            }
        }

        public void onTaskMovedToFront(int taskId) throws RemoteException {
            HwPCManagerService.super.onTaskMovedToFront(taskId);
            HwPCUtils.log(HwPCManagerService.TAG, "onTaskMovedToFront taskId=" + taskId);
            Message msg = HwPCManagerService.this.mHandler.obtainMessage(HwPCManagerService.MSG_RECORD_TASKID);
            msg.arg1 = taskId;
            HwPCManagerService.this.mHandler.sendMessage(msg);
            Message msg2 = HwPCManagerService.this.mHandler.obtainMessage(HwPCManagerService.MSG_CLEAR_REOCRD_TASKID);
            HwPCManagerService.this.mHandler.removeMessages(HwPCManagerService.MSG_CLEAR_REOCRD_TASKID);
            HwPCManagerService.this.mHandler.sendMessageDelayed(msg2, 2000);
            if (!HwPCUtils.isHiCarCastMode() || taskId != -1) {
                HwPCManagerService.this.sendToMessengers(18, taskId, -1, null);
                if (HwPCUtils.isHiCarCastMode()) {
                    HwHiCarMultiWindowManager.getInstance().onMoveTaskToFront(taskId);
                }
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
            HwPCManagerService.this.sendToMessengers(HwPCManagerService.MSG_TASK_PROFILE_LOCKED, taskId, userId, null);
        }
    };
    private TelephonyManager mTelephonyPhone;
    int mTmpDisplayId2Unlocked;
    private final ComponentName mTouchPadComponent = new ComponentName("com.huawei.desktop.systemui", "com.huawei.systemui.mk.activity.ImitateActivity");
    Handler mUIHandler = new Handler(UiThreadEx.getLooper());
    private UnlockScreenReceiver mUnlockScreenReceiver;
    private int mUserId = 0;
    UserManagerInternalEx mUserManagerInternal;
    boolean mUserSetupComplete = false;
    private HwPCVAssistCmdExecutor mVAssistCmdExecutor;
    private final ComponentName mWechatComponent = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pManager mWifiP2pManager;
    private BroadcastReceiver mWifiPCReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass17 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null && action.equals("android.bluetooth.adapter.action.STATE_CHANGED") && intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0) == 12 && HwPCUtils.getIsWifiMode()) {
                HwPCManagerService.this.mBluetoothReminderDialog.showCloseBluetoothTip(HwPCManagerService.this.mContext);
            }
        }
    };
    private WindowManagerInternalEx mWindowManagerInternalEx;
    private String mWireLessDeviceName = BuildConfig.FLAVOR;
    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "powerReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && HwPCManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action) && intent.getIntExtra(HwPCManagerService.POWER_MODE, 0) == 3 && HwPCUtils.getIsWifiMode() && HwPCManagerService.this.mDisplayManager != null) {
                HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10048, BuildConfig.FLAVOR);
                DisplayManagerEx.disconnectWifiDisplay(HwPCManagerService.this.mDisplayManager);
                HwPCUtils.log(HwPCManagerService.TAG, "super power save recevier receive brodcast and disconnectWifiDisplay");
            }
        }
    };
    private boolean restartByUnlock2SetAnimTime;

    /* access modifiers changed from: private */
    public static boolean isDesktopMode(HwPCUtils.ProjectionMode mode) {
        return mode == HwPCUtils.ProjectionMode.DESKTOP_MODE;
    }

    public enum CastMode {
        INVALID(-1),
        PC(0),
        HICAR(1);
        
        private int mCastMode;

        private CastMode(int castMode) {
            this.mCastMode = castMode;
        }
    }

    private Context getCurrentContext() {
        if (!isDesktopMode(this.mProjMode)) {
            return this.mContext;
        }
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager == null) {
            return null;
        }
        Display[] displays = displayManager.getDisplays();
        Display display = null;
        int length = displays.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Display dis = displays[i];
            if (DisplayEx.getType(dis) != 1 && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                display = dis;
                break;
            }
            i++;
        }
        if (display != null) {
            return this.mContext.createDisplayContext(display);
        }
        return null;
    }

    private boolean isEnterDesktopModeRemembered() {
        int isRemembered = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 0);
        Log.d(TAG, "isEnterDesktopModeRemembered" + isRemembered);
        if (isRemembered == 1) {
            return true;
        }
        return false;
    }

    private boolean isExitDesktopModeRemembered() {
        int isRemembered = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "exit-desktop-mode-remember", 0);
        Log.d(TAG, "isExitDesktopModeRemembered" + isRemembered);
        if (isRemembered == 1) {
            return true;
        }
        return false;
    }

    private boolean isShowEnterDialog() {
        int isRemembered = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 0);
        Log.d(TAG, "isShowEnterDialog" + isRemembered);
        if (isRemembered == 0) {
            return true;
        }
        return false;
    }

    private boolean isShowExitDialog() {
        int isRemembered = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 0);
        Log.d(TAG, "isShowExitDialog" + isRemembered);
        if (isRemembered == 0) {
            return true;
        }
        return false;
    }

    private SpannableString getSpanString(String orig, String re, String url) {
        Log.d(TAG, String.format("getSpanString: orig=%s re=%s url %s", orig, re, url));
        SpannableString spannableString = new SpannableString(orig);
        int start = orig.indexOf(re);
        if (start < 0) {
            return spannableString;
        }
        spannableString.setSpan(new ClickableSpan() {
            /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass5 */

            @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }

            @Override // android.text.style.ClickableSpan
            public void onClick(View widget) {
                Log.d(HwPCManagerService.TAG, "SpannableString onClick.");
            }
        }, start, re.length() + start, 33);
        return spannableString;
    }

    private boolean isMMIRunning() {
        return SystemPropertiesEx.get(MMI_TEST_PROPERTY, "false").equals("true");
    }

    private void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        showEnterDesktopAlertDialog(context, isExclusiveKeyboard, false);
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardLocked();
        }
        return false;
    }

    private boolean isKidsModeRunning() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(KIDS_MODE_PACKAGES_NAME, 8192);
            if (Settings.Global.getInt(this.mContext.getContentResolver(), KIDS_MODE_RUNNING_SETTINGS, 0) == 1) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            HwPCUtils.log(TAG, "kidsmode not found!");
            return false;
        }
    }

    private void showEnterDesktopAlertDialog(Context context, boolean isExclusiveKeyboard, boolean notDisplayAdd) {
        String content;
        if (isMMIRunning() || isKeyguardLocked() || isKidsModeRunning()) {
            HwPCUtils.log(TAG, "showEnterDesktopAlertDialog failed! Is MMI test Running:" + isMMIRunning() + ",or is Kids Mode Running:" + isKidsModeRunning());
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
            } else if (context != null) {
                AlertDialog alertDialog = this.mEnterDesktopAlertDialog;
                if (alertDialog != null && alertDialog.isShowing()) {
                    return;
                }
                if (isEnterDesktopModeRemembered() && !isDesktopMode(this.mProjMode) && !isExclusiveKeyboard) {
                    backToHomeInDefaultDisplay(get1stDisplay().mDisplayId);
                    sendSwitchMsgDelayed(TIME_SEND_SWITCHPROJ_MSG_DELAYED);
                } else if (!isExclusiveKeyboard || isShowEnterDialog()) {
                    AlertDialog.Builder buider = new AlertDialog.Builder(context, HwPartResourceUtils.getResourceId("Theme_Emui_Dialog_Alert"));
                    View view = LayoutInflater.from(buider.getContext()).inflate(HwPartResourceUtils.getResourceId("switch_pc_mode"), (ViewGroup) null);
                    if (view == null) {
                        HwPCDataReporter.getInstance().reportFailEnterPadEvent(4, this.mPCDisplayInfo);
                        return;
                    }
                    ImageView imageView = (ImageView) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_image"));
                    TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_description"));
                    CheckBox checkBox = (CheckBox) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_remember"));
                    if (imageView != null && textView != null && checkBox != null) {
                        if (isExclusiveKeyboard) {
                            imageView.setPadding(0, 0, 0, 0);
                            content = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_exclusive_keyboard2"));
                        } else {
                            imageView.setImageResource(HwPartResourceUtils.getResourceId("image_pc_pad_setting"));
                            content = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_content"));
                        }
                        textView.setText(getSpanString(content, this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_settings")), BuildConfig.FLAVOR));
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        checkBox.setOnCheckedChangeListener($$Lambda$HwPCManagerService$g0sCWFv6fEG5wbF7RtSb4mA4ELU.INSTANCE);
                        this.mEnterDesktopAlertDialog = buider.setPositiveButton(HwPartResourceUtils.getResourceId("desktop_mode_enter_start"), new DialogInterface.OnClickListener(checkBox, isExclusiveKeyboard) {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$ta1h_omBjRXMwtQFMiIP9sKXbH8 */
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.this.lambda$showEnterDesktopAlertDialog$1$HwPCManagerService(this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setNegativeButton(HwPartResourceUtils.getResourceId("desktop_mode_enter_cancel"), new DialogInterface.OnClickListener(checkBox, isExclusiveKeyboard) {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$jI8dFaSDl6PUETSlJ6f3Farh5L4 */
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.this.lambda$showEnterDesktopAlertDialog$2$HwPCManagerService(this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setView(view).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$XF_doXfSscGJdZcRfYDOAbytgIM */

                            @Override // android.content.DialogInterface.OnDismissListener
                            public final void onDismiss(DialogInterface dialogInterface) {
                                HwPCManagerService.this.lambda$showEnterDesktopAlertDialog$3$HwPCManagerService(dialogInterface);
                            }
                        }).create();
                        this.mEnterDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mEnterDesktopAlertDialog.getWindow().setType(2008);
                        this.mEnterDesktopAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass6 */

                            @Override // android.content.DialogInterface.OnShowListener
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
                    }
                }
            }
        }
    }

    static /* synthetic */ void lambda$showEnterDesktopAlertDialog$0(CompoundButton buttonView, boolean isChecked) {
    }

    public /* synthetic */ void lambda$showEnterDesktopAlertDialog$1$HwPCManagerService(CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        backToHomeInDefaultDisplay(get1stDisplay().mDisplayId);
        this.isNeedEnterDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 1);
            } else {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    public /* synthetic */ void lambda$showEnterDesktopAlertDialog$2$HwPCManagerService(CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:cancel which=" + which);
        this.isNeedEnterDesktop = false;
        if (checkBox.isChecked()) {
            if (isExclusiveKeyboard) {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show-enter-dialog-use-keyboard", 1);
            } else {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "enter-desktop-mode-remember", 1);
            }
        }
        dialog.cancel();
    }

    public /* synthetic */ void lambda$showEnterDesktopAlertDialog$3$HwPCManagerService(DialogInterface dialog) {
        if (this.isNeedEnterDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            this.isNeedEnterDesktop = false;
            sendSwitchMsgDelayed(TIME_SEND_SWITCHPROJ_MSG_DELAYED);
        }
    }

    private void sendSwitchMsgDelayed(int delayMillis) {
        this.mHandler.removeMessages(4);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), (long) delayMillis);
    }

    private void showExitDesktopAlertDialog(Context context, boolean isExclusiveKeyboard) {
        if (isMMIRunning() || isKeyguardLocked()) {
            HwPCUtils.log(TAG, "showExitDesktopAlertDialog failed! Is MMI test Running:" + isMMIRunning());
            return;
        }
        int i = 0;
        if (isCalling()) {
            if (isDesktopMode(this.mProjMode)) {
                i = get1stDisplay().mDisplayId;
            }
            showCallingToast(i);
            HwPCUtils.log(TAG, "switchProjMode failed! in Calling");
        } else if (context != null) {
            AlertDialog alertDialog = this.mExitDesktopAlertDialog;
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            if (isExitDesktopModeRemembered() && isDesktopMode(this.mProjMode) && !isExclusiveKeyboard) {
                sendSwitchMsg();
            } else if (!isExclusiveKeyboard || isShowExitDialog()) {
                AlertDialog.Builder buider = new AlertDialog.Builder(context, HwPartResourceUtils.getResourceId("Theme_Emui_Dialog_Alert"));
                View view = LayoutInflater.from(buider.getContext()).inflate(HwPartResourceUtils.getResourceId("switch_pc_mode"), (ViewGroup) null);
                if (view != null) {
                    String enter_toast = context.getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_toast"));
                    String exit_toast = context.getString(HwPartResourceUtils.getResourceId("desktop_mode_exit_toast"));
                    String enter_exclusive_keyboard = context.getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_exclusive_keyboard"));
                    String exit_exclusive_keyboard = context.getString(HwPartResourceUtils.getResourceId("desktop_mode_exit_exclusive_keyboard"));
                    HwPCUtils.log(TAG, "these string will be used in future:" + enter_toast + exit_toast + enter_exclusive_keyboard + exit_exclusive_keyboard);
                    ImageView imageView = (ImageView) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_image"));
                    TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_description"));
                    CheckBox checkBox = (CheckBox) view.findViewById(HwPartResourceUtils.getResourceId("switch_desktop_mode_remember"));
                    if (imageView != null && textView != null && checkBox != null) {
                        imageView.setPadding(0, 0, 0, 0);
                        if (isExclusiveKeyboard) {
                            textView.setText(HwPartResourceUtils.getResourceId("desktop_mode_exit_exclusive_keyboard2"));
                        } else {
                            textView.setText(HwPartResourceUtils.getResourceId("desktop_mode_exit_content"));
                        }
                        checkBox.setOnCheckedChangeListener($$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk.INSTANCE);
                        this.mExitDesktopAlertDialog = buider.setTitle(HwPartResourceUtils.getResourceId("desktop_mode_exit_title")).setPositiveButton(HwPartResourceUtils.getResourceId("desktop_mode_exit_start"), new DialogInterface.OnClickListener(checkBox, isExclusiveKeyboard) {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$KM7VvCD_e1bvqj_TVkP2krWu4J0 */
                            private final /* synthetic */ CheckBox f$1;
                            private final /* synthetic */ boolean f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.this.lambda$showExitDesktopAlertDialog$5$HwPCManagerService(this.f$1, this.f$2, dialogInterface, i);
                            }
                        }).setNegativeButton(HwPartResourceUtils.getResourceId("desktop_mode_exit_cancel"), new DialogInterface.OnClickListener() {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$iDzqMeWjuvPkauQxU86qGQrdVFU */

                            @Override // android.content.DialogInterface.OnClickListener
                            public final void onClick(DialogInterface dialogInterface, int i) {
                                HwPCManagerService.this.lambda$showExitDesktopAlertDialog$6$HwPCManagerService(dialogInterface, i);
                            }
                        }).setView(view).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$M7OLWtiKdUuEpCnyuMN5QcdDqAI */

                            @Override // android.content.DialogInterface.OnDismissListener
                            public final void onDismiss(DialogInterface dialogInterface) {
                                HwPCManagerService.this.lambda$showExitDesktopAlertDialog$7$HwPCManagerService(dialogInterface);
                            }
                        }).create();
                        this.mExitDesktopAlertDialog.setCanceledOnTouchOutside(true);
                        this.mExitDesktopAlertDialog.getWindow().setType(2008);
                        this.mExitDesktopAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass7 */

                            @Override // android.content.DialogInterface.OnShowListener
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

    public /* synthetic */ void lambda$showExitDesktopAlertDialog$5$HwPCManagerService(CheckBox checkBox, boolean isExclusiveKeyboard, DialogInterface dialog, int which) {
        Log.d(TAG, "onClick:start which=" + which);
        this.isNeedEixtDesktop = true;
        if (checkBox.isChecked()) {
            Log.d(TAG, "onClick:start which=" + which);
            if (isExclusiveKeyboard) {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show-exit-dialog-use-keyboard", 1);
            } else {
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "exit-desktop-mode-remember", 1);
            }
        }
        dialog.dismiss();
    }

    public /* synthetic */ void lambda$showExitDesktopAlertDialog$6$HwPCManagerService(DialogInterface dialog, int which) {
        this.isNeedEixtDesktop = false;
        Log.d(TAG, "onClick:cancel which=" + which);
        dialog.cancel();
    }

    public /* synthetic */ void lambda$showExitDesktopAlertDialog$7$HwPCManagerService(DialogInterface dialog) {
        if (this.isNeedEixtDesktop) {
            Log.d(TAG, "EnterDesktopAlertDialog dismiss");
            this.isNeedEixtDesktop = false;
            sendSwitchMsgDelayed(TIME_SEND_SWITCHPROJ_MSG_DELAYED);
        }
    }

    /* access modifiers changed from: private */
    public class LocalHandler extends HandlerEx {
        public LocalHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r1v37, resolved type: android.app.AlarmManager */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX INFO: Multiple debug info for r0v136 int: [D('taskId' int), D('removeTaskId' int)] */
        /* JADX WARN: Type inference failed for: r7v1, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
        /* JADX WARNING: Code restructure failed: missing block: B:112:0x0413, code lost:
            if (r0.isProjectionMode(r0.get2ndDisplay().mDisplayId) != false) goto L_0x0415;
         */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void handleMessage(Message msg) {
            PowerManager powerManager;
            boolean forScroll = true;
            switch (msg.what) {
                case 1:
                    HwPCManagerService.this.sendNotificationForSwitch((HwPCUtils.ProjectionMode) msg.obj);
                    return;
                case 2:
                case 3:
                case HwPCManagerService.MSG_TASK_PROFILE_LOCKED /* 19 */:
                case 20:
                default:
                    return;
                case 4:
                    if (HwPCUtils.enabledInPad() && HwPCManagerService.this.isMonkeyRunning()) {
                        HwPCUtils.log(HwPCManagerService.TAG, "MSG_SET_PROJ_MODE isMonkeyRunning return !");
                        return;
                    } else if (HwPCManagerService.this.get1stDisplay().mDisplayId == -1) {
                        HwPCUtils.log(HwPCManagerService.TAG, "mDisplayId is incorrect,just return");
                        return;
                    } else {
                        HwPCManagerService.this.mIsDisplayAddedAfterSwitch = false;
                        if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode)) {
                            HwPCUtils.setPCDisplayID(HwPCManagerService.this.get1stDisplay().mDisplayId);
                            HwPCUtils.setPhoneDisplayID(-1);
                            HwPCManagerService.this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
                            HwPCManagerService.this.mDDC.enableProjectionMode();
                            if (!HwPCManagerService.this.mIsDisplayLargerThan1080p || HwPCManagerService.this.get1stDisplay().mType != 2) {
                                HwPCManagerService.this.handleSwitchToDesktopMode();
                                return;
                            } else {
                                SettingsEx.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "selected-proj-mode", 1);
                                return;
                            }
                        } else {
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
                                if (!(HwPCManagerService.this.get1stDisplay() == null || HwPCManagerService.this.get2ndDisplay() == null)) {
                                    HwPCManagerService hwPCManagerService = HwPCManagerService.this;
                                    if (!hwPCManagerService.isProjectionMode(hwPCManagerService.get1stDisplay().mDisplayId)) {
                                        HwPCManagerService hwPCManagerService2 = HwPCManagerService.this;
                                        break;
                                    }
                                    HwPCManagerService.this.startProjectionService();
                                }
                            }
                            if (HwPCUtils.enabledInPad()) {
                                HwPCManagerService.this.bindUnbindService(false);
                                DisplayManagerInternalEx.pcDisplayChange(false);
                                HwPCManagerService.this.mPadPCDisplayIsRemoved = true;
                                DisplayManagerInternalEx.pcDisplayChange(true);
                            }
                            HwActivityTaskManager.togglePCMode(HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode), HwPCManagerService.this.get1stDisplay().mDisplayId);
                            HwPCManagerService.this.setUsePCModeMouseIconContext(false);
                            HwPCUtils.setPcCastModeInServer(false);
                            HwPCManagerService.this.setPcCastingDisplayId(-1);
                            HwPCManagerService.this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                            HwPCManagerService.this.updateIMEWithHardKeyboardState(false);
                            SettingsEx.Secure.putInt(HwPCManagerService.this.mContext.getContentResolver(), "selected-proj-mode", 0);
                            HwPCManagerService.this.restoreDreamSettingInPad();
                            HwPCManagerService hwPCManagerService3 = HwPCManagerService.this;
                            hwPCManagerService3.sendNotificationForSwitch(hwPCManagerService3.mProjMode);
                            HwPCManagerService.this.bdReportDiffSrcStatus(false);
                            HwPCManagerService.this.bdReportSameSrcStatus(true);
                            HwPCManagerService.this.sendSwitchToStatusBar();
                            HwPCManagerService.this.setDesktopModeToAudioService(0);
                            HwPCManagerService.this.updateFingerprintSlideSwitch();
                            HwPCManagerService.this.relaunchIMEDelay(0);
                            HwPCManagerService.this.exitDesktopModeForMk();
                            HwPCManagerService.this.disableShowTouchStatus();
                            HwPCManagerService hwPCManagerService4 = HwPCManagerService.this;
                            hwPCManagerService4.backToHomeInDefaultDisplay(hwPCManagerService4.get1stDisplay().mDisplayId);
                            if (HwPCUtils.enabledInPad()) {
                                HwPCManagerService.this.setNotchStatus(false);
                                ContextEx.updateDisplay(HwPCManagerService.this.mContext, 0);
                                ContextEx.updateDisplay(ActivityThreadEx.currentActivityThread().getSystemUiContext(), 0);
                            }
                            synchronized (HwPCManagerService.this.mMessengers) {
                                HwPCManagerService.this.mMessengers.clear();
                            }
                            return;
                        }
                    }
                case 5:
                    if (HwPCManagerService.this.mHasSwitchNtf) {
                        HwPCManagerService hwPCManagerService5 = HwPCManagerService.this;
                        hwPCManagerService5.sendNotificationForSwitch(hwPCManagerService5.mProjMode);
                        return;
                    }
                    return;
                case 6:
                    if (HwPCManagerService.this.mKeyguardManager == null) {
                        HwPCManagerService hwPCManagerService6 = HwPCManagerService.this;
                        hwPCManagerService6.mKeyguardManager = (KeyguardManager) hwPCManagerService6.mContext.getSystemService("keyguard");
                    }
                    if (HwPCManagerService.this.mUserManagerInternal == null) {
                        HwPCManagerService hwPCManagerService7 = HwPCManagerService.this;
                        hwPCManagerService7.mUserManagerInternal = hwPCManagerService7.getUserManagerInternal();
                    }
                    if (!HwPCManagerService.this.mUserManagerInternal.isUserUnlockingOrUnlocked(UserHandleEx.myUserId())) {
                        HwPCUtils.log(HwPCManagerService.TAG, "MSG_DISPLAY_ADDED user locked wait 1s");
                        HwPCManagerService.this.mHandler.removeMessages(6);
                        HwPCManagerService.this.mHandler.sendMessageDelayed(HwPCManagerService.this.mHandler.obtainMessage(6, msg.arg1, -1), 1000);
                        return;
                    }
                    boolean isConnToHiCar = HiCarManager.isConnToHiCar(HwPCManagerService.this.mContext, msg.arg1);
                    if ((HwPCManagerService.this.mKeyguardManager == null || HwPCManagerService.this.mKeyguardManager.isKeyguardLocked() || !StorageManagerExt.isUserKeyUnlocked(HwPCManagerService.this.mUserId)) && !HwMultiDisplayUtils.isConnectedToWindows(HwPCManagerService.this.mContext, msg.arg1) && !isConnToHiCar) {
                        HwPCManagerService.this.mTmpDisplayId2Unlocked = msg.arg1;
                        HwPCManagerService.this.mRestartAppsWhenUnlocked = true;
                        return;
                    }
                    if (isConnToHiCar && (powerManager = (PowerManager) HwPCManagerService.this.mContext.getSystemService("power")) != null) {
                        HwPCUtils.log(HwPCManagerService.TAG, "wakeup when screen off in HiCar mode");
                        PowerManagerEx.wakeUp(powerManager, SystemClock.uptimeMillis(), 2, "DisplayAdd");
                    }
                    HwPCUtils.log(HwPCManagerService.TAG, "keyguard is unlocked and filesystem is unlocked");
                    HwPCManagerService.this.onDisplayAdded(msg.arg1);
                    return;
                case 7:
                    HwPCManagerService.this.onDisplayChanged(msg.arg1);
                    return;
                case 8:
                    HwPCManagerService.this.onDisplayRemoved(msg.arg1);
                    return;
                case 9:
                    HwPCManagerService.this.handleRestoreApps(msg.arg1);
                    return;
                case 10:
                    if (msg.arg1 == 0 || HwPCManagerService.this.getCastMode()) {
                        HwPCManagerService.this.restoreApp(msg.arg1, (Intent) msg.obj);
                        return;
                    } else {
                        removeMessages(10);
                        return;
                    }
                case 11:
                    HwPCManagerService.this.mIntentList.clear();
                    return;
                case 12:
                    HwPCManagerService.this.onSwitchUser(msg.arg1);
                    return;
                case 13:
                    HwPCManagerService.this.launchMK();
                    return;
                case 14:
                    HwPCManagerService.this.doRelaunchIMEIfNecessary();
                    return;
                case HwPCManagerService.WINDOW_LAYOUT_MODE_MASK /* 15 */:
                    HwPCManagerService.this.mIntentList.clear();
                    HwPCManagerService.this.mIntentList.addAll((List) msg.obj);
                    HwPCManagerService.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + ((long) msg.arg1), "keep_record", HwPCManagerService.this.mAlarmListener, HwPCManagerService.this.mHandler);
                    return;
                case 16:
                    String pkgName = (String) msg.obj;
                    if (HwPCManagerService.DEBUG) {
                        HwPCUtils.log(HwPCManagerService.TAG, "handleMessage MSG_UNINSTALL_APP:" + pkgName);
                    }
                    if (pkgName != null) {
                        PackageManagerExt.deletePackage(HwPCManagerService.this.mContext.getPackageManager(), pkgName, 2);
                        return;
                    }
                    return;
                case 17:
                    HwPCManagerService.this.setFocusedPCDisplayId("unlockScreen");
                    return;
                case 18:
                    HwPCManagerService.this.doUpdateDisplayOverrideConfiguration(msg.arg1);
                    return;
                case HwPCManagerService.MSG_DP_STATE_CHANGED /* 21 */:
                    HwPCManagerService.this.mPcMultiDisplayMgr.notifyDpState(((Boolean) msg.obj).booleanValue());
                    return;
                case 22:
                    HwPCManagerService hwPCManagerService8 = HwPCManagerService.this;
                    hwPCManagerService8.showDPLinkErrorDialog(hwPCManagerService8.mContext, (String) msg.obj);
                    return;
                case 23:
                    HwPCManagerService.this.openEasyProjection();
                    return;
                case HwPCManagerService.MSG_CLEAR_LIGHTER_DRAWED /* 24 */:
                    KeyEvent ev = (KeyEvent) msg.obj;
                    if (msg.arg1 == 0) {
                        forScroll = false;
                    }
                    if (HwPCManagerService.this.shouldSendBroadcastForClearLighterDrawed(ev, forScroll)) {
                        HwPCManagerService.this.sendBroadcastForClearLighterDrawed();
                        return;
                    }
                    return;
                case HwPCManagerService.MSG_DP_LINK_STATE_CABLE_OUT /* 25 */:
                    HwPCManagerService.this.dismissDpLinkErrorDialog();
                    return;
                case HwPCManagerService.MSG_SHOW_HELP_TO_PC_MODEL /* 26 */:
                    Intent intent = new Intent();
                    intent.setAction("com.huawei.filemanager.desktopinstruction.PCModeRecommendActivity");
                    intent.setComponent(HwPCManagerService.this.mHelpMsgComponent);
                    intent.addFlags(268435456);
                    ActivityOptions activityOptions = ActivityOptions.makeBasic();
                    activityOptions.setLaunchDisplayId(0);
                    try {
                        HwPCManagerService.this.mContext.startActivity(intent, activityOptions.toBundle());
                        return;
                    } catch (ActivityNotFoundException e) {
                        HwPCUtils.log(HwPCManagerService.TAG, "fail to start activity");
                        return;
                    }
                case HwPCManagerService.MSG_CLEAR_DRAG_DROP_CACHE_DIR /* 27 */:
                    HwPCManagerService.clearCacheFiles(HwPCManagerService.DRAG_DROP_CACHE_DIR);
                    return;
                case HwPCManagerService.MSG_RECORD_TASKID /* 28 */:
                    int removeTaskId = msg.arg1;
                    if (HwPCManagerService.this.checkAppName(removeTaskId)) {
                        HwPCUtils.log(HwPCManagerService.TAG, "MSG_RECORD_TASKID" + msg.arg1);
                        if (HwPCManagerService.this.mRelaunchTaskId == removeTaskId) {
                            HwPCUtils.log(HwPCManagerService.TAG, "MSG_RECORD_TASKID wechat twice");
                            HwPCManagerService.this.mIsAppOpenTwice = true;
                            HwPCManagerService.this.mLastTimeOpen = System.currentTimeMillis();
                        }
                        HwPCManagerService.this.mRelaunchTaskId = removeTaskId;
                        return;
                    }
                    return;
                case HwPCManagerService.MSG_RESTART_WECHAT /* 29 */:
                    int removeTaskId2 = msg.arg1;
                    long timePass = System.currentTimeMillis() - HwPCManagerService.this.mLastTimeOpen;
                    HwPCUtils.log(HwPCManagerService.TAG, "MSG_RESTART_WECHAT" + msg.arg1 + ", timePass:" + timePass);
                    if (removeTaskId2 == HwPCManagerService.this.mRelaunchTaskId && HwPCManagerService.this.mIsAppOpenTwice && timePass < 1000) {
                        HwPCUtils.log(HwPCManagerService.TAG, "MSG_RESTART_WECHAT reopen wechat");
                        HwPCManagerService.this.reOpenWechat();
                        return;
                    }
                    return;
                case HwPCManagerService.MSG_CLEAR_REOCRD_TASKID /* 30 */:
                    HwPCUtils.log(HwPCManagerService.TAG, "MSG_CLEAR_REOCRD_TASKID");
                    HwPCManagerService.this.mRelaunchTaskId = -1;
                    HwPCManagerService.this.mIsAppOpenTwice = false;
                    HwPCManagerService.this.mLastTimeOpen = 0;
                    return;
                case HwPCManagerService.MSG_SEND_PROJECTION_CAPSULE /* 31 */:
                    HwPCUtils.log(HwPCManagerService.TAG, "MSG_SEND_PROJECTION_CAPSULE");
                    HwPCManagerService.this.sendBroadcastForProjectionCapsule(msg.arg1, msg.arg2);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startProjectionService() {
        if (this.mContext != null) {
            HwPCUtils.log(TAG, "startProjectionService start.");
            Intent controlPanelIntent = new Intent();
            controlPanelIntent.putExtra(SHOW_CONTROL_PANEL, false);
            controlPanelIntent.setComponent(this.mControlPanelComponent);
            this.mContext.startService(controlPanelIntent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchToDesktopMode() {
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
        HwActivityTaskManager.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
        setUsePCModeMouseIconContext(true);
        SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
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
            setNotchStatus(true);
            setFocusedPCDisplayId("enterDesktop");
        }
        relaunchIMEDelay(2000);
        enterDesktopModeForMk();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableShowTouchStatus() {
        if (HwPCUtils.enabledInPad()) {
            SettingsEx.Systemex.putInt(this.mContext.getContentResolver(), "show_touches", 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNotchStatus(boolean isEnterDesktop) {
        if (IS_NOTCH) {
            if (isEnterDesktop) {
                this.mNotchStatus = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "display_notch_status", -1);
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "display_notch_status", 0);
                return;
            }
            SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "display_notch_status", this.mNotchStatus);
            this.mNotchStatus = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public UserManagerInternalEx getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = new UserManagerInternalEx();
        }
        return this.mUserManagerInternal;
    }

    public void scheduleDisplayAdded(int displayId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "scheduleDisplayAdded checkCallingPermission failed" + Binder.getCallingPid());
        } else if (isAodDisplay(displayId)) {
            HwPCUtils.log(TAG, "scheduleDisplayAdded do not support aod display.");
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
    /* access modifiers changed from: public */
    private void bindUnbindService(boolean bind) {
        this.mPCSettingsObserver.restoreScreenOffSettings();
        if (bind) {
            bindAllPCService();
            registerAudioDeviceSelect();
            registerScreenOnEvent();
            registerShutdownEvent();
            registerPowerOnEvent();
            if (HwPCUtils.getIsWifiMode()) {
                registerBluetoothReceiver();
            }
            HwActivityTaskManager.registerHwTaskStackListener(this.mTaskStackListener);
            return;
        }
        unbindAllPcService();
        unregisterAudioDeviceSelect();
        unRegisterScreenOnEvent();
        restoreRotationInPad();
        unRegisterShutdownEvent();
        unRegisterPowerOnEvent();
        if (this.mIsNeedUnRegisterBluetoothReciver) {
            unRegisterBluetoothReceiver();
        }
        HwActivityTaskManager.unRegisterHwTaskStackListener(this.mTaskStackListener);
    }

    private void saveRotationInPad() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mRotationSwitch = SettingsEx.Systemex.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                if (this.mRotationSwitch == 0) {
                    this.mRotationValue = SettingsEx.Systemex.getIntForUser(this.mContext.getContentResolver(), "user_rotation", 0, this.mUserId);
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveRotationInPad Exception.");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreRotationInPad() {
        if (HwPCUtils.enabledInPad()) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mRotationSwitch == 0) {
                    SettingsEx.Systemex.putIntForUser(this.mContext.getContentResolver(), "user_rotation", this.mRotationValue, this.mUserId);
                } else {
                    SettingsEx.Systemex.putIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 1, this.mUserId);
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreRotationInPad Exception.");
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
                HwPCUtils.log(TAG, "unRegisterShutdownEvent Exception");
            }
        }
    }

    private void saveDreamSettingInPad() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            try {
                int DreamsSetting = SettingsEx.Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (DreamsSetting == 1) {
                    SettingsEx.Secure.putIntForUser(resolver, "screensaver_enabled", 0, this.mUserId);
                    this.mDreamsEnabledSetting = DreamsSetting;
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "saveDreamSettingInPad Exception");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreDreamSettingInPad() {
        if (HwPCUtils.enabledInPad()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            try {
                int DreamsSetting = SettingsEx.Secure.getIntForUser(resolver, "screensaver_enabled", -1, this.mUserId);
                if (this.mDreamsEnabledSetting == 1 && this.mDreamsEnabledSetting != DreamsSetting) {
                    SettingsEx.Secure.putIntForUser(resolver, "screensaver_enabled", this.mDreamsEnabledSetting, this.mUserId);
                    this.mDreamsEnabledSetting = -1;
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "restoreDreamSettingInPad Exception.");
            }
        }
    }

    private void bindAllPCService() {
        HiCarManager hiCarManager;
        if (HwPCUtils.isHiCarCastMode() && (hiCarManager = this.mHiCarManager) != null) {
            hiCarManager.bindHiCarService(this.mContext, get1stDisplay().mDisplayId);
        } else if (isDesktopEnabled()) {
            Intent intent = new Intent();
            intent.putExtra(KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
            intent.setComponent(this.mSystemUIComponent);
            bindService(this.mContext, intent, this.mConnSysUI);
            Intent intent2 = new Intent();
            intent2.putExtra(KEY_BEFORE_BOOT_ANIM_TIME, this.mPCBeforeBootAnimTime);
            intent2.putExtra(KEY_IS_WIRELESS_MODE, HwPCUtils.getIsWifiMode());
            intent2.putExtra(KEY_CURRENT_DISPLAY_UNIQUEID, getPcDisplayInfo() != null ? getPcDisplayInfo().getUniqueId() : BuildConfig.FLAVOR);
            intent2.setComponent(this.mExplorerComponent);
            bindService(this.mContext, intent2, this.mConnExplorer);
        } else {
            HwPCUtils.log(TAG, "no services need bind");
        }
    }

    private void bindService(Context context, Intent intent, ServiceConnection connection) {
        if (context != null) {
            try {
                context.bindService(intent, connection, 1);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "failed to bind pc service");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void clearCacheFiles(String cacheDir) {
        if (cacheDir != null) {
            File file = new File(cacheDir);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) {
                    HwPCUtils.log(TAG, "get file list fail");
                    return;
                }
                for (File f : files) {
                    if (f.isFile() && !new File(f.getPath()).delete()) {
                        HwPCUtils.log(TAG, "delete file fail. ");
                    }
                }
            }
        }
    }

    private void unbindAllPcService() {
        HiCarManager hiCarManager;
        HwPCUtils.log(TAG, "unbindAllPcService");
        if (HwPCUtils.isHiCarCastMode() && (hiCarManager = this.mHiCarManager) != null) {
            hiCarManager.unBindHiCarService(this.mContext);
        }
        try {
            this.mContext.unbindService(this.mConnSysUI);
            this.mContext.unbindService(this.mConnExplorer);
        } catch (Exception e) {
            HwPCUtils.log(TAG, "failed to unbind pc services");
        }
    }

    /* JADX WARN: Type inference failed for: r9v0, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
    /* JADX WARN: Type inference failed for: r10v2, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
    /* JADX WARNING: Unknown variable types count: 2 */
    public HwPCManagerService(Context context, ActivityManagerServiceEx amsEx) {
        super(context, amsEx);
        boolean projMode = false;
        this.mContext = context;
        this.mAMS = amsEx.switchToHwActivityManagerService();
        this.mHandlerThread = new ServiceThreadExt(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new LocalHandler(this.mHandlerThread.getLooper());
        boolean isFactory = SystemPropertiesEx.get("ro.runmode", "normal").equals("factory");
        boolean isMmiTest = SystemPropertiesEx.get(MMI_TEST_PROPERTY, "false").equals("true");
        if (isFactory || isMmiTest) {
            HwPCUtils.setFactoryOrMmiState(true);
        }
        if (!this.mAMS.isCurrentUserEmpty()) {
            this.mUserId = this.mAMS.getCurrentUserId();
        }
        try {
            ActivityManagerExt.registerUserSwitchObserver(new UserSwitchObserverExt() {
                /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass9 */

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    HwPCUtils.log(HwPCManagerService.TAG, "onUserSwitchComplete userId: " + newUserId);
                }

                public void onUserSwitching(int newUserId, IRemoteCallbackExt reply) throws RemoteException {
                    HwPCUtils.log(HwPCManagerService.TAG, "onUserSwitching userId: " + newUserId);
                    HwPCManagerService.this.scheduleSwitchUser(newUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult((Bundle) null);
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
        final int displayId = findPCDisplayId();
        if (displayId != -1) {
            this.mUIHandler.postDelayed(new Runnable() {
                /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass10 */

                @Override // java.lang.Runnable
                public void run() {
                    HwPCUtils.log(HwPCManagerService.TAG, "main thread display:" + displayId);
                    HwPCManagerService.this.scheduleDisplayAdded(displayId);
                }
            }, 5000);
        }
        InputManager im = (InputManager) this.mContext.getSystemService("input");
        if (im != null) {
            int[] devices = InputDevice.getDeviceIds();
            for (int i = 0; i < devices.length; i++) {
                this.mConnectedInputDevices |= whichInputDevice(InputDevice.getDevice(devices[i]));
            }
            im.registerInputDeviceListener(this.mInputDeviceListener, null);
        }
        this.mPCSettingsObserver = new PCSettingsObserver(this.mHandler);
        this.mPCSettingsObserver.readScreenOffSettings();
        this.mProvisioned = deviceIsProvisioned();
        this.mUserSetupComplete = isUserSetupComplete();
        this.mPCSettingsObserver.observe();
        if (!HwPCUtils.enabledInPad()) {
            this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
            SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
        } else if (SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1 ? true : projMode) {
            this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
        } else {
            this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
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
        unlockFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiver(this.mUnlockScreenReceiver, unlockFilter);
        registerExternalPointerEventListener();
        this.mIBinderAudioService = ServiceManagerEx.getService("audio");
        setDesktopModeToAudioService(-1);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mTelephonyPhone = (TelephonyManager) this.mContext.getSystemService("phone");
        HwPCUtils.setPcCastModeInServerEarly(HwPCUtils.ProjectionMode.PHONE_MODE);
        this.mDPLinkStateObserver.startObserving(EXCLUSIVE_DP_LINK);
        this.mBluetoothReminderDialog = new BluetoothReminderDialog();
        this.mPcMultiDisplayMgr = new HwPCMultiDisplaysManager(this.mContext, this.mHandler, this);
        this.mVAssistCmdExecutor = new HwPCVAssistCmdExecutor(this.mContext, this, this.mAMS);
        this.mMultiDisplayAudioMgr = new HwMultiDisplayAudioManager(this.mContext, this, this.mAMS);
        SystemPropertiesEx.set("hw.pc.support.app.projection", String.valueOf(true));
        this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        WifiP2pManager wifiP2pManager = this.mWifiP2pManager;
        if (wifiP2pManager != null) {
            this.mWifiP2pChannel = wifiP2pManager.initialize(this.mContext, this.mHandler.getLooper(), null);
        }
        this.mHandler.handleMessage(this.mHandler.obtainMessage(MSG_CLEAR_DRAG_DROP_CACHE_DIR));
    }

    /* access modifiers changed from: package-private */
    public boolean deviceIsProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isUserSetupComplete() {
        return SettingsEx.Secure.getInt(this.mContext.getContentResolver(), SettingsEx.Secure.getUserSetupComplete(), 0) != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleSwitchUser(int userId) {
        this.mHandler.removeMessages(12);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, userId, -1));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSwitchUser(int userId) {
        HwPCUtils.log(TAG, "onSwitchUser userId =" + userId);
        this.mUserId = userId;
        if (get1stDisplay().mDisplayId != -1) {
            AlertDialog alertDialog = this.mEnterDesktopAlertDialog;
            if (alertDialog != null && alertDialog.isShowing()) {
                this.mEnterDesktopAlertDialog.cancel();
            }
            if (userId == 0) {
                HwPCUtils.log(TAG, "onSwitchUser: UserHandle.USER_OWNER");
                DisplayManager displayManager = this.mDisplayManager;
                if (displayManager != null && displayManager.getDisplay(get1stDisplay().mDisplayId) != null) {
                    sendNotificationForSwitch(this.mProjMode);
                    return;
                }
                return;
            }
            if (isDesktopMode(this.mProjMode)) {
                lightPhoneScreen();
                HwPCUtils.log(TAG, "onSwitchUser: The current mode is DesktopMode");
                this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                HwPCUtils.setPcCastModeInServerEarly(this.mProjMode);
                if (!HwPCUtils.enabledInPad()) {
                    bindUnbindService(false);
                }
                HwActivityTaskManager.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
                setUsePCModeMouseIconContext(false);
                HwPCUtils.setPcCastModeInServer(false);
                setPcCastingDisplayId(-1);
                this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                updateIMEWithHardKeyboardState(false);
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
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
            NotificationManager notificationManager = this.mNm;
            if (notificationManager != null) {
                notificationManager.cancelAll();
                this.mHasSwitchNtf = false;
            }
        }
    }

    private boolean isWiredDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager == null) {
            HwPCUtils.log(TAG, "isWiredDisplay null dm:");
            return false;
        }
        Display display = displayManager.getDisplay(displayId);
        if (display == null) {
            HwPCUtils.log(TAG, "isWiredDisplay null display:");
            return false;
        } else if (!HwPCUtils.enabledInPad()) {
            int type = DisplayEx.getType(display);
            if (type == 5 && HiCarManager.isHiCarDevice(display.getName())) {
                return true;
            }
            if (type == 5 && "com.hpplay.happycast".equals(DisplayEx.getOwnerPackageName(display))) {
                return true;
            }
            if (type == 5 && CAST_PLUS_VIRTUALDISPLAY_NAME.equals(display.getName())) {
                HwPCUtils.log(TAG, "is virtual CastPlusDisplay");
                return true;
            } else if (type == 5 && "com.huawei.works".equals(DisplayEx.getOwnerPackageName(display)) && ESHARE_DISPLAY_NAME.equals(display.getName())) {
                return true;
            } else {
                if (HwPCUtils.isWirelessProjectionEnabled()) {
                    if (type == 2 || type == 3 || ((type == 5 || type == 4) && this.mSupportOverlay)) {
                        return true;
                    }
                    return false;
                } else if (type == 2 || ((type == 5 || type == 4) && this.mSupportOverlay)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else if (isPadPCDisplay(displayId)) {
            HwPCUtils.log(TAG, "isWiredDisplay pad pc screen return true display:");
            return true;
        } else {
            HwPCUtils.log(TAG, "isWiredDisplay pad pc screen return false display:");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isProjectionMode(int displayId) {
        if (IS_TABLET) {
            HwPCUtils.log(TAG, "isProjectionMode: Don't support pad!");
            return false;
        } else if (HiCarManager.isConnToHiCar(this.mContext, displayId)) {
            HwPCUtils.log(TAG, "isProjectionMode: Don't support Hicar!");
            return false;
        } else {
            if (this.mDisplayManager == null) {
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
            }
            DisplayManager displayManager = this.mDisplayManager;
            if (displayManager == null) {
                HwPCUtils.log(TAG, "isProjectionMode: mDisplayManager is null!");
                return false;
            }
            Display display = displayManager.getDisplay(displayId);
            if (display == null) {
                HwPCUtils.log(TAG, "isProjectionMode: display is null!");
                return false;
            }
            int type = DisplayEx.getType(display);
            if ((type == 5 && "com.huawei.works".equals(DisplayEx.getOwnerPackageName(display)) && ESHARE_DISPLAY_NAME.equals(display.getName())) || type == 2 || ((type == 5 || type == 4) && this.mSupportOverlay)) {
                return true;
            }
            if (type == 5 && CAST_PLUS_VIRTUALDISPLAY_NAME.equals(display.getName())) {
                return true;
            }
            if (!HwPCUtils.isWirelessProjectionEnabled() || type != 3) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNotificationForSwitch(HwPCUtils.ProjectionMode projMode) {
        String mode;
        if (!HwPCUtils.enabledInPad() && this.mNm != null && !HwPCUtils.isHiCarCastMode() && isDesktopEnabled()) {
            Notification.Builder builder = new Notification.Builder(this.mContext, HW_PCM);
            builder.setSmallIcon(HwPartResourceUtils.getResourceId("ic_notify_cast_control"));
            builder.setVisibility(-1);
            builder.setPriority(1);
            Bundle extras = new Bundle();
            extras.putBoolean("hw_disable_ntf_delete_menu", true);
            builder.addExtras(extras);
            NotificationEx.Builder.setAppName(builder, this.mContext.getString(HwPartResourceUtils.getResourceId("proj_mode_notification_label")));
            if (isDesktopMode(projMode)) {
                mode = this.mContext.getString(HwPartResourceUtils.getResourceId("notification_pc_desktop_mode"));
            } else {
                mode = this.mContext.getString(HwPartResourceUtils.getResourceId("notification_pc_phone_mode"));
            }
            boolean isConnectFromWelink = isConnectFromThirdApp(get1stDisplay().mDisplayId) == 2;
            boolean isNeedShowDisconnectBtn = isConnectFromWelink || (isConnectFromThirdApp(get1stDisplay().mDisplayId) == 3);
            this.mWireLessDeviceName = BuildConfig.FLAVOR;
            if (HwPCUtils.getIsWifiMode()) {
                DisplayManager displayManager = this.mDisplayManager;
                if (!(displayManager == null || displayManager.getDisplay(get1stDisplay().mDisplayId) == null)) {
                    this.mWireLessDeviceName = String.format(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("notification_pc_connected")), this.mDisplayManager.getDisplay(get1stDisplay().mDisplayId).getName());
                }
            } else if (isConnectFromWelink) {
                this.mWireLessDeviceName = String.format(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("notification_pc_connected")), this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("welink_device_name")));
            }
            PendingIntent clickPendingIntent = PendingIntentEx.getBroadcastAsUser(this.mContext, 0, new Intent(ACTION_NOTIFY_OPEN_EASY_PROJECTION), 134217728, UserHandleEx.OWNER);
            PendingIntent switchModePendingIntent = PendingIntentEx.getBroadcastAsUser(this.mContext, 1, new Intent(ACTION_NOTIFY_SWITCH_MODE), 268435456, UserHandleEx.OWNER);
            PendingIntent showMKPendingIntent = PendingIntentEx.getBroadcastAsUser(this.mContext, 1, new Intent(ACTION_NOTIFY_SHOW_MK), 134217728, UserHandleEx.OWNER);
            PendingIntent disconnectPendingIntent = PendingIntentEx.getBroadcastAsUser(this.mContext, 1, new Intent(ACTION_NOTIFY_DISCONNECT), 134217728, UserHandleEx.OWNER);
            builder.setContentIntent(clickPendingIntent);
            RemoteViews contentView = createContentView(mode, switchModePendingIntent, showMKPendingIntent, disconnectPendingIntent, isNeedShowDisconnectBtn, false);
            RemoteViews bigContentView = createContentView(mode, switchModePendingIntent, showMKPendingIntent, disconnectPendingIntent, isNeedShowDisconnectBtn, true);
            builder.setCustomContentView(contentView);
            builder.setCustomBigContentView(bigContentView);
            Notification notification = builder.getNotification();
            notification.flags |= 2;
            notification.flags |= 32;
            this.mNm.notify(TAG, 0, notification);
            this.mHasSwitchNtf = true;
        }
    }

    private RemoteViews createContentView(String mode, PendingIntent switchModePendingIntent, PendingIntent showMKPendingIntent, PendingIntent disconnectPendingIntent, boolean isNeedShowDisconnectBtn, boolean isBigContentVIew) {
        RemoteViews contentView;
        int i = 8;
        if (isBigContentVIew) {
            contentView = new RemoteViews(this.mContext.getPackageName(), HwPartResourceUtils.getResourceId("pc_notification_big"));
            if (isDesktopMode(this.mProjMode)) {
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_switch_phone_action"), switchModePendingIntent);
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_switch_phone_action"), 0);
                contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_switch_phone_action"), getActionText(true));
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_switch_desktop_action"), 8);
            } else {
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_switch_desktop_action"), switchModePendingIntent);
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_switch_phone_action"), 8);
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_switch_desktop_action"), 0);
                contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_switch_desktop_action"), getActionText(false));
            }
            contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_touchpad_action"), this.mContext.getString(HwPartResourceUtils.getResourceId("notification_btn_touchpad")));
            if (!isNeedShowMKAction()) {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_touchpad_action"), 8);
            } else {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_touchpad_action"), 0);
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_touchpad_action"), showMKPendingIntent);
            }
            contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_disconnect_action"), this.mContext.getString(HwPartResourceUtils.getResourceId("notification_btn_disconnect")));
            if (HwPCUtils.getIsWifiMode() || isNeedShowDisconnectBtn) {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_disconnect_action"), 0);
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_disconnect_action"), disconnectPendingIntent);
            } else {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_disconnect_action"), 8);
            }
        } else {
            contentView = new RemoteViews(this.mContext.getPackageName(), HwPartResourceUtils.getResourceId("pc_notification"));
            contentView.setImageViewResource(HwPartResourceUtils.getResourceId("pc_notification_switch_icon"), getResDrawableId(isDesktopMode(this.mProjMode)));
            contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_switch_icon"), switchModePendingIntent);
            if (!isNeedShowMKAction()) {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_touchpad_icon"), 8);
            } else {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_touchpad_icon"), 0);
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_touchpad_icon"), showMKPendingIntent);
            }
            if (HwPCUtils.getIsWifiMode() || isNeedShowDisconnectBtn) {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_disconnect_icon"), 0);
                contentView.setOnClickPendingIntent(HwPartResourceUtils.getResourceId("pc_notification_disconnect_icon"), disconnectPendingIntent);
            } else {
                contentView.setViewVisibility(HwPartResourceUtils.getResourceId("pc_notification_disconnect_icon"), 8);
            }
        }
        contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_title"), this.mContext.getString(HwPartResourceUtils.getResourceId("proj_mode_notification_label")));
        contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_mode_text"), mode);
        contentView.setTextViewText(HwPartResourceUtils.getResourceId("pc_notification_device_text"), this.mWireLessDeviceName);
        int resourceId = HwPartResourceUtils.getResourceId("pc_notification_device_text");
        if (!TextUtils.isEmpty(this.mWireLessDeviceName)) {
            i = 0;
        }
        contentView.setViewVisibility(resourceId, i);
        return contentView;
    }

    private CharSequence getActionText(boolean isDesktopMode) {
        Context context = this.mContext;
        if (isDesktopMode) {
            return context.getString(HwPartResourceUtils.getResourceId("notification_btn_phone_mode"));
        }
        return context.getString(HwPartResourceUtils.getResourceId("notification_btn_desktop_mode"));
    }

    private int getResDrawableId(boolean isDesktopMode) {
        if (isDesktopMode) {
            return HwPartResourceUtils.getResourceId("ic_notify_pc_btn_phone");
        }
        return HwPartResourceUtils.getResourceId("ic_notify_pc_btn_desktop");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void collapsePanels() {
        new StatusBarManagerServiceEx().collapsePanels();
    }

    public void onDisplayChanged(int displayId) {
        HwPCUtils.log(TAG, "onDisplayChanged, displayId:" + displayId);
        if (this.mProvisioned) {
        }
    }

    public void onDisplayRemoved(int displayId) {
        if (HwPCUtils.enabledInPad()) {
            if (isExDisplayInDesktopMode(displayId)) {
                sendToMessengers(MSG_SWITCH_AUDIO_OUTPUT_TO_PAD);
            }
            HwPCUtils.log(TAG, "onDisplayRemoved enabledInPad return");
        } else if (HwPCUtils.enabled()) {
            if (!isInWindowsCastMode() || displayId != HwPCUtils.getWindowsCastDisplayId()) {
                AirSharingManager airSharingManager = this.mAirSharingManager;
                if (airSharingManager != null && airSharingManager.isDisplayBound(displayId)) {
                    this.mAirSharingManager.unbindAirSharingService(displayId);
                    this.mAirSharingManager = null;
                }
                if (HwVideoCallCastManager.getDefault().isDisplayForVideoCall(displayId)) {
                    HwVideoCallCastManager.getDefault().onDisplayRemoved(this.mContext, displayId);
                    HwPCUtils.setPhoneDisplayID(-1);
                    return;
                }
                if (this.mSupportProjectionList.contains(Integer.valueOf(displayId))) {
                    processProjectionCapsule(displayId);
                }
                if (displayId == get2ndDisplay().mDisplayId) {
                    HwPCUtils.log(TAG, "onDisplayRemoved ignore it if 2nd display removed.");
                    if (get2ndDisplay().mType == 3) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 0);
                    }
                    get2ndDisplay().mDisplayId = -1;
                    get2ndDisplay().mType = 0;
                    return;
                }
                Settings.Secure.putString(this.mContext.getContentResolver(), PRIVACY_PROJECTION_MODE, "false");
                HwPCUtils.log(TAG, "onDisplayRemoved 1nd display, set multi_display_privacy_projection_mode false");
                boolean isInPhoneMode = HwPCUtils.getPhoneDisplayID() == displayId && !isDesktopMode(this.mProjMode);
                HwPCUtils.log(TAG, "onDisplayRemoved, displayId:" + displayId + ", isInPhoneMode:" + isInPhoneMode + " mProjMode: " + this.mProjMode);
                if (HwPCUtils.getPCDisplayID() != displayId && !isInPhoneMode) {
                    HwPCUtils.log(TAG, "onDisplayRemoved, displayId is neither PC Display ID nor Phone Display ID.");
                } else if ((!this.mProvisioned || !this.mUserSetupComplete) && displayId != get1stDisplay().mDisplayId) {
                    HwPCUtils.log(TAG, "onDisplayRemoved not permitted before setup or not scheduleDisplayAdded");
                } else {
                    if (HwPCUtils.getIsWifiMode() || get1stDisplay().mType == 5) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 0);
                    }
                    try {
                        HwPCUtils.setPcCastModeInServerEarly(HwPCUtils.ProjectionMode.PHONE_MODE);
                        if (this.mProjMode == HwPCUtils.ProjectionMode.DESKTOP_MODE) {
                            bdReportDiffSrcStatus(false);
                        } else {
                            bdReportSameSrcStatus(false);
                        }
                        this.mHandler.removeMessages(1);
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
                            setPcCastingDisplayId(-1);
                            this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                            setDesktopModeToAudioService(-1);
                            updateFingerprintSlideSwitch();
                            HwMultiDisplayPowerManager.getDefault().lockScreenWhenDisconnected(this.mContext);
                            synchronized (this.mMessengers) {
                                this.mMessengers.clear();
                            }
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
                        setPcCastingDisplayId(-1);
                        this.mVAssistCmdExecutor.notifyDesktopModeChanged(false, 0);
                        setDesktopModeToAudioService(-1);
                        updateFingerprintSlideSwitch();
                        Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 1);
                        this.mPcMultiDisplayMgr.handlelstDisplayInDisplayRemoved();
                        HwPCDataReporter.getInstance().stopPCDisplay();
                        exitDesktopModeForMk();
                        if (HwPCUtils.isHiCarCastMode()) {
                            this.mHiCarManager = null;
                            HwPCUtils.mTouchDeviceID = -1;
                            HwPCUtils.setIsHiCarMode(false);
                            HiCarManager.setIsOverlay(false);
                            HiCarManager.getInstance().setInputMethodUid(1067);
                            HwHiCarMultiWindowManager.onDisplayRemoved();
                        }
                        synchronized (this.mNeedRestartIntentList) {
                            this.mHandler.removeMessages(10);
                            int N = this.mNeedRestartIntentList.size();
                            for (int i = 0; i < N; i++) {
                                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10, 0, 0, this.mNeedRestartIntentList.get(i)), (long) (i * 800));
                            }
                            this.mNeedRestartIntentList.clear();
                        }
                        HwMultiDisplayPowerManager.getDefault().lockScreenWhenDisconnected(this.mContext);
                        synchronized (this.mMessengers) {
                            this.mMessengers.clear();
                        }
                    } catch (Throwable th) {
                        HwMultiDisplayPowerManager.getDefault().lockScreenWhenDisconnected(this.mContext);
                        synchronized (this.mMessengers) {
                            this.mMessengers.clear();
                            throw th;
                        }
                    }
                }
            } else {
                HwWindowsCastManager.getDefault().onDisplayRemoved(this.mContext, displayId);
                HwPCUtils.setPhoneDisplayID(-1);
                this.mIsShopDemoVersion = false;
            }
        }
    }

    static /* synthetic */ boolean lambda$processProjectionCapsule$8(int displayId, Integer intDisplay) {
        return intDisplay.intValue() == displayId;
    }

    private void processProjectionCapsule(int displayId) {
        this.mSupportProjectionList.removeIf(new Predicate(displayId) {
            /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$QmDOQILD5nOkN5RW_FqwLCWCLo */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HwPCManagerService.lambda$processProjectionCapsule$8(this.f$0, (Integer) obj);
            }
        });
        HwPCUtils.log(TAG, "processProjectionCapsule displayId: " + displayId + ", mSupportProjectionList: " + this.mSupportProjectionList);
        if (this.mSupportProjectionList.isEmpty()) {
            Settings.Secure.putString(this.mContext.getContentResolver(), PRIVACY_PROJECTION_MODE, "false");
            sendProjectionCapsuleMsg(0, displayId);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v44, resolved type: com.huawei.server.hwmultidisplay.videocall.HwVideoCallCastManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v1, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
    /* JADX WARN: Type inference failed for: r7v9, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
    /* JADX WARN: Type inference failed for: r2v33, types: [android.os.Handler, com.huawei.server.pc.HwPCManagerService$LocalHandler] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Unknown variable types count: 3 */
    private void onDisplayAdded(int displayId) {
        Display display;
        if (HwPCUtils.enabled()) {
            HwPCUtils.log(TAG, "onDisplayAdded, displayId:" + displayId);
            if (!this.mProvisioned || !this.mUserSetupComplete) {
                HwPCUtils.log(TAG, "onDisplayAdded not permitted before setup");
            } else if (displayId == -1 || displayId == 0) {
                HwPCUtils.log(TAG, "context is null or is default display");
            } else if (HwPCUtils.enabledInPad() && this.mUserId != 0) {
                HwPCUtils.log(TAG, "switchProjMode failed! currentUser is not UserHandle.USER_OWNER");
            } else if (!HwPCUtils.enabledInPad() || !isMonkeyRunning()) {
                bdReportOnDisplayAdded(displayId);
                if (HwMultiDisplayUtils.isConnectedToWindows(this.mContext, displayId)) {
                    HwPCUtils.log(TAG, "onDisplayAdded isConnectedToWindows return");
                    updateShopDemoVersion();
                    HwWindowsCastManager.getDefault().onDisplayAdded(this.mContext, displayId);
                    HwPCUtils.setPhoneDisplayID(displayId);
                } else if (HwVideoCallCastManager.getDefault().isConnForVideoCall(this.mContext, displayId)) {
                    HwPCUtils.log(TAG, "onDisplayAdded isConnForVideoCall return");
                    HwVideoCallCastManager.getDefault().onDisplayAdded(this.mContext, displayId, this.mHandler);
                    HwPCUtils.setPhoneDisplayID(displayId);
                } else {
                    if (isExDisplayInDesktopMode(displayId)) {
                        sendToMessengers(MSG_SWITCH_AUDIO_OUTPUT_TO_EXDISPLAY);
                    }
                    if (isAodDisplay(displayId)) {
                        HwPCUtils.log(TAG, "Do not support aod display.");
                    } else if (!isWiredDisplay(displayId)) {
                        HwPCUtils.log(TAG, "is not a wired display.");
                    } else {
                        if (isConnectFromThirdApp(displayId) == 2) {
                            Settings.Global.putInt(this.mContext.getContentResolver(), WIRELESS_PROJECTION_STATE, 1);
                        }
                        Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 0);
                        this.mPcMultiDisplayMgr.checkInitialDpConnectAfterBoot(displayId);
                        if (isProjectionMode(displayId)) {
                            this.mSupportProjectionList.add(Integer.valueOf(displayId));
                            sendProjectionCapsuleMsg(1, displayId);
                        }
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
                        boolean projMode = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "selected-proj-mode", 0) == 1 && isConnectFromThirdApp(displayId) != 2;
                        if (HiCarManager.isConnToHiCar(this.mContext, displayId)) {
                            if (this.mDisplayManager == null) {
                                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
                            }
                            HwHiCarMultiWindowManager.initInstance(this.mContext, this, this.mAMS, this.mDisplayManager.getDisplay(displayId), this.mHandler.getLooper());
                            HwPCUtils.setIsHiCarMode(true);
                            HwPCUtils.setPCDisplayID(displayId);
                            projMode = true;
                            HwPCUtils.mTouchDeviceID = -1431655681;
                            this.mHiCarManager = new HiCarManager(this.mHandler, this.mContext);
                            HwPCUtils.bdReport(this.mContext, 10062, BuildConfig.FLAVOR);
                            removeTaskFromRecent();
                        } else {
                            HwPCUtils.setIsHiCarMode(false);
                        }
                        if (projMode) {
                            this.mProjMode = HwPCUtils.ProjectionMode.DESKTOP_MODE;
                        } else {
                            this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                        }
                        get1stDisplay().mDisplayId = displayId;
                        get1stDisplay().mType = this.mPcMultiDisplayMgr.getDisplayType(displayId);
                        HwPCUtils.setIsWifiMode(isWifiPCMode(get1stDisplay().mDisplayId));
                        boolean enterDesktopMode = isDesktopMode(this.mProjMode) && this.mUserId == 0;
                        HwPCUtils.log(TAG, "onDisplayAdded mProjMode " + this.mProjMode + ", mConnectedInputDevices = " + this.mConnectedInputDevices + ", mUserId = " + this.mUserId + ", enterDesktopMode =" + enterDesktopMode);
                        DisplayManager displayManager = this.mDisplayManager;
                        if (!(displayManager == null || (display = displayManager.getDisplay(get1stDisplay().mDisplayId)) == null)) {
                            this.mPCDisplayInfo = new DisplayInfoEx();
                            if (DisplayEx.getDisplayInfo(display, this.mPCDisplayInfo)) {
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
                        if (isOnOTAStartupPage()) {
                            this.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
                            HwPCUtils.log(TAG, "avoid start pc-mode when OTA update in pc-mode");
                            return;
                        }
                        if (enterDesktopMode) {
                            HwPCUtils.setPCDisplayID(displayId);
                            HwPCUtils.setPhoneDisplayID(-1);
                            if (!isDesktopMode(this.mProjMode)) {
                                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 1);
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
                            HwActivityTaskManager.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
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
                            HwActivityTaskManager.togglePCMode(isDesktopMode(this.mProjMode), get1stDisplay().mDisplayId);
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
                        if (AirSharingManager.isCastPlusDisplay(this.mContext, displayId)) {
                            this.mAirSharingManager = new AirSharingManager(this.mContext, this.mHandler);
                            AirSharingManager airSharingManager = this.mAirSharingManager;
                            if (airSharingManager == null) {
                                HwPCUtils.log(TAG, "err construct AirSharingManager failed");
                            } else {
                                airSharingManager.bindAirSharingService(displayId);
                            }
                        }
                    }
                }
            } else {
                HwPCUtils.log(TAG, "onDisplayAdded isMonkeyRunning return !");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastForProjectionCapsule(int proState, int displayId) {
        HwPCUtils.log(TAG, "sendBroadcastForProjectionCapsule proState: " + proState + ", displayId: " + displayId);
        Intent intent = new Intent();
        intent.putExtra(DISPLAY_ID, displayId);
        if (proState == 1) {
            intent.putExtra(PROJECTION_STATE, 1);
        } else {
            intent.putExtra(PROJECTION_STATE, 0);
        }
        intent.setAction(ACTION_NOTIFY_PROJECTION_CAPSULE_STATUS_BAR);
        this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_PROJECTION_CAPSULE_STATUS_BAR);
    }

    private boolean isAodDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null) {
            return AOD_VIRTUAL_DISPLAY.equals(display.getName());
        }
        return false;
    }

    public boolean isShopDemo() {
        return this.mIsShopDemoVersion;
    }

    private void updateShopDemoVersion() {
        String[] readBuffers = {DisplayDriverCommunicator.START_DESKTOP_MODE_VALUE};
        if (!(HwProtectAreaManager.getInstance().readProtectArea(VENDOR_AND_COUNTRY, (int) READ_BUFFER_LENGTH, readBuffers, new int[1]) != 0 || readBuffers[0] == null || readBuffers[0].length() == 0)) {
            readBuffers[0].replace('/', '-');
        }
        this.mIsShopDemoVersion = readBuffers[0].contains(DEMO_VENDOR);
    }

    private boolean isOnOTAStartupPage() {
        List<ResolveInfo> resolveInfos = this.mContext.getPackageManager().queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT").setPackage(OOBE_START_GUIDE_PACKAGE), 0);
        if (resolveInfos == null || resolveInfos.size() <= 0) {
            return false;
        }
        HwPCUtils.log(TAG, "isOnStartupPage OOBE_ACTIVITY enable");
        return true;
    }

    private boolean isExDisplayInDesktopMode(int displayId) {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || displayId == HwPCUtils.getPCDisplayID() || displayId == 0) {
            return false;
        }
        return true;
    }

    private void sendShowHelpMsgToPCModel() {
        if (!HwPCUtils.enabledInPad()) {
            int helpMsgState = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), HELP_MSG_PROMPT_STATE, 0);
            int thirdAppType = isConnectFromThirdApp(get1stDisplay().mDisplayId);
            if (helpMsgState != 100 && thirdAppType != 1) {
                HwPCUtils.log(TAG, "sendShowHelpMsgToPCModel help msg not prompted and it is not hppcat, so show help msg only once");
                SettingsEx.Secure.putInt(this.mContext.getContentResolver(), HELP_MSG_PROMPT_STATE, 100);
                this.mHandler.removeMessages(MSG_SHOW_HELP_TO_PC_MODEL);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SHOW_HELP_TO_PC_MODEL, 0, -1));
            }
        }
    }

    private void autoLaunchMK() {
        if (!HwPCUtils.isHiCarCastMode()) {
            boolean isGuideStarted = false;
            if (SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "guide-started", 0) == 100) {
                isGuideStarted = true;
            }
            int thirdAppType = isConnectFromThirdApp(get1stDisplay().mDisplayId);
            if (isGuideStarted || thirdAppType == 2 || thirdAppType == 1) {
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass11 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwPCManagerService.this.sendShowMkMessage();
                    }
                }, 2000);
            }
        }
    }

    public void LaunchMKForWifiMode() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "LaunchMKForWifiMode checkCallingPermission failed" + Binder.getCallingPid());
        } else if (get1stDisplay().mDisplayId == -1 || HwPCUtils.enabledInPad() || !isDesktopMode(this.mProjMode)) {
            this.mIsWifiBroadDone = true;
        } else {
            autoLaunchMK();
            this.mIsWifiBroadDone = false;
        }
    }

    private void enterDesktopModeForMk() {
        HwPCUtils.log(TAG, "enterDesktopModeForMk   ");
        if (this.mPolicy == null) {
            this.mPolicy = new PhoneWindowManagerEx();
        }
        this.mPCMkManager = HwPCMkManager.getInstance(this.mContext);
        this.mPCMkManager.initCrop(this.mContext, this);
        this.mPCMkManager.startSendEventThread();
        this.mPCMkManager.updatePointerAxis(getPointerCoordinateAxis());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void exitDesktopModeForMk() {
        HwPCUtils.log(TAG, "exitDesktopModeForMk   ");
        HwPCMkManager hwPCMkManager = this.mPCMkManager;
        if (hwPCMkManager != null) {
            hwPCMkManager.stopSendEventThreadAndRelease();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void backToHomeInDefaultDisplay(int curDisplayId) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "backToHomeInDefaultDisplay");
            Intent homeIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
            homeIntent.addFlags(268435456);
            try {
                Context displayContext = getDisplayContext(this.mContext, 0);
                if (displayContext != null) {
                    displayContext.startActivity(homeIntent);
                } else {
                    HwPCUtils.log(TAG, "backToHomeInDefaultDisplay, fail to get the default display.");
                }
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "ActivityMovedToDesktopDisplay fail to start go home");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setUsePCModeMouseIconContext(boolean pcmode) {
        InputManagerServiceEx.DefaultHwInputManagerLocalService inputManager = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        if (inputManager == null) {
            return;
        }
        if (pcmode) {
            if (this.mDisplayManager == null) {
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
            }
            DisplayManager displayManager = this.mDisplayManager;
            if (displayManager != null) {
                Display[] displays = displayManager.getDisplays();
                Display display = null;
                int length = displays.length;
                int i = 0;
                while (true) {
                    if (i < length) {
                        Display dis = displays[i];
                        if (dis != null && HwPCUtils.isValidExtDisplayId(dis.getDisplayId())) {
                            display = dis;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
                if (display != null) {
                    Context context = this.mContext.createDisplayContext(display);
                    HwPCUtils.log(TAG, "setUsePCModeMouseIconContext displayId = " + display.getDisplayId());
                    inputManager.setExternalDisplayContext(context);
                    return;
                }
                return;
            }
            return;
        }
        inputManager.setExternalDisplayContext((Context) null);
    }

    private void sendSwitchMsg() {
        this.mHandler.removeMessages(4);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
    }

    private void sendProjectionCapsuleMsg(int proState, int displayId) {
        this.mHandler.removeMessages(MSG_SEND_PROJECTION_CAPSULE);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SEND_PROJECTION_CAPSULE, proState, displayId));
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
            } else if (!isDesktopMode(this.mProjMode)) {
                showEnterDesktopAlertDialog(getCurrentContext(), false);
            } else {
                showExitDesktopAlertDialog(getCurrentContext(), false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSwitchToStatusBar() {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "sendSwitchToStatusBar!");
            Intent intent = new Intent();
            intent.setAction(ACTION_NOTIFY_CHANGE_STATUS_BAR);
            this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_CHANGE_STATUS_BAR);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshNotifications() {
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
        if (device == null || !InputDeviceEx.isExternal(device)) {
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

    /* access modifiers changed from: private */
    public static String getDeviceDescription(InputDevice device) {
        if (device == null) {
            return " ";
        }
        int sources = device.getSources();
        Map<Integer, String> sourceTypes = new HashMap<>();
        sourceTypes.put(257, "keyboard");
        sourceTypes.put(513, "dpad");
        sourceTypes.put(4098, "touchscreen");
        sourceTypes.put(8194, "mouse");
        sourceTypes.put(16386, "stylus");
        sourceTypes.put(65540, "trackball");
        sourceTypes.put(131076, "mouse_relative");
        sourceTypes.put(1048584, "touchpad");
        sourceTypes.put(16777232, "joystick");
        sourceTypes.put(1025, "gamepad");
        StringBuilder description = new StringBuilder(100);
        description.append(" ");
        for (Map.Entry<Integer, String> entry : sourceTypes.entrySet()) {
            if ((entry.getKey().intValue() & sources) == entry.getKey().intValue()) {
                description.append(entry.getValue());
                description.append(" ");
            }
        }
        return description.toString();
    }

    private class UnlockScreenReceiver extends BroadcastReceiver {
        private UnlockScreenReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "mUnlockScreenReceiver received a null intent");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.USER_PRESENT".equals(action) || "android.intent.action.USER_UNLOCKED".equals(action)) {
                HwPCUtils.log(HwPCManagerService.TAG, "receive: action is" + action + ",mRestartAppsWhenUnlocked is " + HwPCManagerService.this.mRestartAppsWhenUnlocked);
                if (HwPCManagerService.this.mRestartAppsWhenUnlocked) {
                    HwPCManagerService hwPCManagerService = HwPCManagerService.this;
                    hwPCManagerService.mRestartAppsWhenUnlocked = false;
                    hwPCManagerService.restartByUnlock2SetAnimTime = true;
                    HwPCManagerService hwPCManagerService2 = HwPCManagerService.this;
                    hwPCManagerService2.scheduleDisplayAdded(hwPCManagerService2.mTmpDisplayId2Unlocked);
                } else {
                    HwPCUtils.log(HwPCManagerService.TAG, "receive: action is" + action + ", MSG_SET_FOCUS_DISPLAY");
                    HwPCManagerService.this.resizeTaskOnUnLock();
                    HwPCManagerService.this.mHandler.removeMessages(17);
                    HwPCManagerService.this.mHandler.sendMessage(HwPCManagerService.this.mHandler.obtainMessage(17));
                }
                if (HwPCManagerService.this.isInWindowsCastMode()) {
                    HwPCUtils.bdReport(HwPCManagerService.this.mContext, 10066, BuildConfig.FLAVOR);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resizeTaskOnUnLock() {
        int taskId;
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mAMS.getActivityTaskManagerServiceEx());
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (!tasks.isEmpty() && tasks.get(0) != null && tasks.get(0).topActivity != null && (taskId = tasks.get(0).id) != 1) {
                TaskRecordEx taskRecordEx = this.mAMS.getActivityTaskManagerServiceEx().anyTaskForId(taskId);
                if ((taskRecordEx.getOriginalWindowState() & 15) == 3 || (taskRecordEx.getNextWindowState() & 15) == 3) {
                    multiWindowMgr.resizeTaskFromPC(taskRecordEx, multiWindowMgr.getLaunchBounds(taskRecordEx));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        while (true) {
            int[] iArr = KEYBOARD_PRODUCT_ID;
            if (i >= iArr.length) {
                HwPCUtils.log(TAG, "isExclusiveKeyboard=false");
                return false;
            } else if (keyboardProductId == iArr[i] && keyboardVendorId == KEYBOARD_VENDOR_ID[i]) {
                HwPCUtils.log(TAG, "isExclusiveKeyboard=true");
                return true;
            } else {
                i++;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendShowMkMessage() {
        HwPCUtils.log(TAG, "sendShowMkMessage todo launch touchpad");
        if (!isNeedShowMKAction()) {
            HwPCUtils.log(TAG, "unable to send ShowMk message, existMouse:" + existMouseInputDevices() + " get1stDisplay().mDisplayId:" + get1stDisplay().mDisplayId + " mProjMode:" + this.mProjMode);
            return;
        }
        this.mHandler.removeMessages(13);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(13));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOpenEasyProjectionMessage() {
        this.mHandler.removeMessages(23);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void launchMK() {
        if (!HwPCUtils.enabledInPad() && !HwPCUtils.isHiCarCastMode()) {
            HwPCUtils.log(TAG, "launchMK todo start touchpad activiy");
            if (!isNeedShowMKAction()) {
                HwPCUtils.log(TAG, "cannot launch MK, existMouse:" + existMouseInputDevices() + " get1stDisplay().mDisplayId:" + get1stDisplay().mDisplayId);
                return;
            }
            Display display = ContextEx.getDisplay(this.mContext);
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
    /* access modifiers changed from: public */
    private void openEasyProjection() {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        if (HwPCUtils.isWirelessProjectionEnabled()) {
            intent.setComponent(this.mInstructionComponentWirelessEnabled);
            HwPCUtils.bdReport(this.mContext, 10045, BuildConfig.FLAVOR);
        } else {
            intent.setComponent(this.mInstructionComponent);
            HwPCUtils.bdReport(this.mContext, 10046, BuildConfig.FLAVOR);
        }
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HwPCUtils.log(TAG, "openEasyProjection can not find activity.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUninstallAppMessage(String packageName) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendUninstallAppMessage: " + packageName);
        }
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

    private boolean isDesktopEnabled() {
        return DESKTOP_ENABLED && systemUIExist() && explorerExist();
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
        return this.mAMS.checkPermission(permission, Binder.getCallingPid(), UserHandleEx.getAppId(Binder.getCallingUid())) == 0;
    }

    private boolean checkCallingPermission(int taskId) {
        if (UserHandleEx.getAppId(Binder.getCallingUid()) == RECOVER_SHOWTOUCH_STATUS_TIMEOUT) {
            return true;
        }
        String[] callingPackages = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
        HwRecentTaskInfo targetTask = HwActivityTaskManager.getHwRecentTaskInfo(taskId);
        if (!(callingPackages == null || targetTask == null || targetTask.topActivity == null)) {
            String targetPackageName = targetTask.topActivity.getPackageName();
            for (String callingPackageName : callingPackages) {
                if (targetPackageName.equals(callingPackageName) || "com.huawei.desktop.systemui".equals(callingPackageName) || "com.huawei.desktop.explorer".equals(callingPackageName) || HiCarManager.HI_CAR_LAUNCHER_PKG.equals(callingPackageName)) {
                    return true;
                }
            }
        }
        HwPCUtils.log(TAG, "checkCallingPermission failed:" + taskId);
        return false;
    }

    public boolean getCastMode() {
        return isDesktopMode(this.mProjMode) && HwPCUtils.isValidExtDisplayId(get1stDisplay().mDisplayId);
    }

    public boolean isHiCarCastModeForClient() {
        return HwPCUtils.isHiCarCastMode();
    }

    public int getPackageSupportPcState(String packageName) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "getPackageSupportPcState:" + packageName);
        }
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return WhiteListAppStrategyManager.getInstance(this.mContext).getAppSupportPCState(packageName);
        }
        HwPCUtils.log(TAG, "getPackageSupportPcState checkCallingPermission failed" + Binder.getCallingPid());
        return -1;
    }

    public boolean checkPermissionForHwMultiDisplay(int uid) {
        if (HwPCUtils.isHiCarCastMode()) {
            return HiCarManager.checkPermission(this.mContext, uid);
        }
        return false;
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
    /* access modifiers changed from: public */
    private void relaunchIMEDelay(int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "relaunchIMEDelay");
            this.mHandler.removeMessages(14);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(14), (long) delayMillis);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisplayOverrideConfiguration(int display, int delayMillis) {
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "updateDisplayOverrideConfiguration");
            this.mHandler.removeMessages(18);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(18, display, 0), (long) delayMillis);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUpdateDisplayOverrideConfiguration(int displayid) {
        this.mAMS.updateDisplayOverrideConfiguration((Configuration) null, displayid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doRelaunchIMEIfNecessary() {
        this.mAMS.relaunchIMEIfNecessary();
    }

    public void hwRestoreTask(int taskId, float x, float y) {
        if (!HwActivityTaskManager.checkTaskId(taskId) || !checkCallingPermission(taskId)) {
            HwPCUtils.log(TAG, "hwRestoreTask checktaskId failed or permission denied:" + taskId);
            return;
        }
        HwActivityTaskManager.hwRestoreTask(taskId, x, y);
    }

    public void hwResizeTask(int taskId, Rect bounds) {
        if (!HwActivityTaskManager.checkTaskId(taskId) || !checkCallingPermission(taskId)) {
            HwPCUtils.log(TAG, "hwResizeTask checktaskId failed or permission denied:" + taskId);
            return;
        }
        HwActivityTaskManager.hwResizeTask(taskId, bounds);
    }

    public void enterFullScreen(int taskId) {
        HwPCUtils.log(TAG, "enterFullScreen, taskId:" + taskId);
        if (!HwActivityTaskManager.checkTaskId(taskId) || !checkCallingPermission(taskId)) {
            HwPCUtils.log(TAG, "enterFullScreen checktaskId failed or permission denied:" + taskId);
            return;
        }
        HwActivityTaskManager.hwResizeTask(taskId, new Rect(-1, -1, -1, -1));
    }

    public void exitFullScreen(int taskId) {
        HwPCUtils.log(TAG, "exitFullScreen ,taskId:" + taskId);
        if (!HwActivityTaskManager.checkTaskId(taskId) || !checkCallingPermission(taskId)) {
            HwPCUtils.log(TAG, "exitFullScreen checktaskId failed or permission denied:" + taskId);
            return;
        }
        HwActivityTaskManager.hwRestoreTask(taskId, -1.0f, -1.0f);
    }

    public int getWindowState(IBinder token) {
        return HwActivityTaskManager.getWindowState(token);
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return HwActivityTaskManager.getHwRecentTaskInfo(taskId);
        }
        HwPCUtils.log(TAG, "getHwRecentTaskInfo checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return HwWindowManager.getDisplayBitmap(displayId, width, height);
        }
        HwPCUtils.log(TAG, "getDisplayBitmap checkCallingPermission failed" + Binder.getCallingPid());
        return null;
    }

    public void registHwSystemUIController(Messenger messenger) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "registHwSystemUIController checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "registHwSystemUIController " + messenger);
        if (messenger != null) {
            synchronized (this.mMessengers) {
                if (!this.mMessengers.contains(messenger)) {
                    this.mMessengers.add(messenger);
                }
            }
        }
    }

    public void setNetworkReconnectionState(boolean isNetworkReconnecting) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setNetworkReconnectionState checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "setNetworkReconnectionState isNetworkReconnecting: " + isNetworkReconnecting);
        HwWindowsCastManager.getDefault().setNetworkReconnectionState(isNetworkReconnecting);
    }

    public void sendLockScreenShowViewMsg() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "sendLockScreenShowViewMsg checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "send lock screen show view message");
        HwWindowsCastManager.getDefault().sendShowViewMsg(1);
    }

    public boolean isInWindowsCastMode() {
        return HwMultiDisplayUtils.getInstance().isInWindowsCastMode();
    }

    public boolean isInSinkWindowsCastMode() {
        return HwMultiDisplayUtils.getInstance().isInSinkWindowsCastMode();
    }

    public void setIsInSinkWindowsCastMode(boolean isInCastMode) {
        HwPCUtils.log(TAG, "setIsInSinkWindowsCastMode " + isInCastMode);
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setIsInSinkWindowsCastMode checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwMultiDisplayUtils.setIsInSinkWindowsCastMode(isInCastMode);
    }

    public boolean isSinkHasKeyboard() {
        return HwMultiDisplayUtils.isSinkHasKeyboard();
    }

    public void setIsSinkHasKeyboard(boolean isKeyboardExist) {
        HwPCUtils.log(TAG, "setIsSinkHasKeyboard " + isKeyboardExist);
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setIsSinkHasKeyboard checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwMultiDisplayUtils.setIsSinkHasKeyboard(isKeyboardExist);
    }

    public int getFocusedDisplayId() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "getFocusedDisplayId checkCallingPermission failed" + Binder.getCallingPid());
            return -1;
        }
        if (this.mWindowManagerInternalEx == null) {
            this.mWindowManagerInternalEx = new WindowManagerInternalEx();
        }
        return this.mWindowManagerInternalEx.getFocusedDisplayId();
    }

    public boolean isFocusedOnWindowsCastDisplay() {
        if (this.mWindowManagerInternalEx == null) {
            this.mWindowManagerInternalEx = new WindowManagerInternalEx();
        }
        return this.mWindowManagerInternalEx.getFocusedDisplayId() == HwPCUtils.getWindowsCastDisplayId() && HwPCUtils.isSinkHasKeyboard();
    }

    public void updateFocusDisplayToWindowsCast() {
        if (this.mWindowManagerInternalEx == null) {
            this.mWindowManagerInternalEx = new WindowManagerInternalEx();
        }
        this.mWindowManagerInternalEx.setFocusedDisplay(HwPCUtils.getWindowsCastDisplayId(), false, "setWindowsCastDisplayId");
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
        sendToMessengers(1);
    }

    public void showStartMenu() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "showStartMenu checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(2);
    }

    public void screenshotPc() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "screenshotPc checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(3);
    }

    public void userActivityOnDesktop() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "userActivityOnDesktop checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(7);
    }

    public void closeTopWindow() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "closeTopWindow checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(4);
    }

    public void triggerSwitchTaskView(boolean show) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "triggerSwitchTaskView checkCallingPermission failed" + Binder.getCallingPid());
        } else if (show) {
            sendToMessengers(5);
        } else {
            sendToMessengers(6);
        }
    }

    public void triggerRecentTaskSplitView(int side, int triggerTaskId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "triggerRecentTaskSplitView checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(22, side, triggerTaskId, null);
    }

    public void triggerSplitWindowPreviewLayer(int side, int action) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "triggerSplitWindowPreviewLayer checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        sendToMessengers(23, side, action, null);
    }

    public Bitmap getTaskThumbnailEx(int id) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "getTaskThumbnailEx checkCallingPermission failed" + Binder.getCallingPid());
            return null;
        } else if (this.mAMS == null) {
            HwPCUtils.log(TAG, "getTaskThumbnailEx failed , ams is null");
            return null;
        } else {
            Binder.clearCallingIdentity();
            return HwActivityTaskManager.getTaskThumbnailOnPCMode(id);
        }
    }

    public void toggleHome() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "toggleHome checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwActivityTaskManager.toggleHome();
    }

    private boolean injectInputEventImmediately(InputEvent event) {
        if (this.mInputManager == null) {
            this.mInputManager = (InputManager) this.mContext.getSystemService("input");
        }
        InputManager inputManager = this.mInputManager;
        if (inputManager != null) {
            return InputManagerEx.injectInputEvent(inputManager, event, InputManagerEx.getInjectInputEventModeAsync());
        }
        return false;
    }

    public boolean injectInputEventExternal(InputEvent event, int mode) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "injectInputEventExternal checkCallingPermission failed" + Binder.getCallingPid());
            return false;
        } else if ((event instanceof MotionEvent) && mode == 0 && ((MotionEvent) event).getDeviceId() == HwPCUtils.mTouchDeviceID) {
            return false;
        } else {
            MotionEvent motionEvent = this.mPolicy;
            if (motionEvent == null || this.mPCMkManager == null) {
                HwPCUtils.log(TAG, " injectInputEventExternal policy or PCMkManager is null");
                return false;
            } else if (mode == 2) {
                int displayId = HwPCUtils.getPCDisplayID();
                if (displayId == 0 || displayId == -1) {
                    HwPCUtils.log(TAG, "the extend displayId is invalid, displayId = " + displayId);
                    return false;
                }
                if (event != null) {
                    InputEventEx.setDisplayId(event, displayId);
                }
                return injectInputEventImmediately(event);
            } else {
                WindowStateEx focusedWinEx = null;
                if (!motionEvent.isPhoneWindowManagerExEmpty()) {
                    focusedWinEx = this.mPolicy.getTopFullscreenWindow();
                }
                if (!(event instanceof MotionEvent) || focusedWinEx == null || focusedWinEx.getDisplayId() != 0 || focusedWinEx.getAttrs() == null || focusedWinEx.getAttrs().getTitle() == null || !"com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(focusedWinEx.getAttrs().getTitle().toString())) {
                    return false;
                }
                if (HwWindowManager.shouldDropMotionEventForTouchPad(((MotionEvent) event).getX(), ((MotionEvent) event).getY())) {
                    HwPCUtils.log(TAG, "injectInputEventExternal should drop MotionEvent for TouchPad");
                    return false;
                }
                Rect visibleRect = focusedWinEx.getVisibleFrameLw();
                Rect displayRect = focusedWinEx.getDisplayFrameLw();
                if (this.mPCMkManager.sendEvent((MotionEvent) event, visibleRect, displayRect, mode)) {
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

    /* access modifiers changed from: package-private */
    public class PointerEventListenerExt extends PointerEventListenerEx {
        PointerEventListenerExt() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent == null) {
                HwPCUtils.log(HwPCManagerService.TAG, "onPointerEvent motionEvent is null.");
                return;
            }
            if (HwPCUtils.isValidExtDisplayId(MotionEventEx.getDisplayId(motionEvent)) && motionEvent.getAction() == 8) {
                HwPCManagerService.this.filterScrollForPCMode();
            }
            HwPCManagerService.this.mAxisX = motionEvent.getX();
            HwPCManagerService.this.mAxisY = motionEvent.getY();
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0029: APUT  
      (r0v1 'axis' float[] A[D('axis' float[])])
      (0 ??[int, short, byte, char])
      (wrap: float : 0x0027: IGET  (r2v0 float) = (r3v0 'this' com.huawei.server.pc.HwPCManagerService A[IMMUTABLE_TYPE, THIS]) com.huawei.server.pc.HwPCManagerService.mAxisX float)
     */
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
        this.mCastModeForIntentList = getCurrentCastMode();
        this.mHandler.removeMessages(15);
        Message msg = this.mHandler.obtainMessage(15, intents);
        msg.arg1 = HwPCUtils.isHiCarCastMode() ? KEEP_RECORD_TIMEOUT_FOR_HICAR : KEEP_RECORD_TIMEOUT;
        this.mHandler.sendMessage(msg);
    }

    private void scheduleRestoreApps(int displayId) {
        if (this.mCastModeForIntentList == getCurrentCastMode() && !this.mIntentList.isEmpty()) {
            this.mAlarmManager.cancel(this.mAlarmListener);
            this.mHandler.removeMessages(10);
            this.mHandler.removeMessages(9);
            LocalHandler localHandler = this.mHandler;
            localHandler.sendMessageDelayed(localHandler.obtainMessage(9, displayId, 0), HwPCUtils.isHiCarCastMode() ? 1500 : 3000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRestoreApps(int displayId) {
        int N = this.mIntentList.size();
        for (int i = 0; i < N; i++) {
            LocalHandler localHandler = this.mHandler;
            localHandler.sendMessageDelayed(localHandler.obtainMessage(10, displayId, 0, this.mIntentList.get(i)), (long) (i * 800));
        }
        this.mIntentList.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreApp(int displayId, Intent intent) {
        Context displayContext = getDisplayContext(this.mContext, displayId);
        if (displayContext != null && intent != null) {
            try {
                if (!HwPCUtils.enabledInPad() || intent.getComponent() == null || !"com.android.incallui".equals(intent.getComponent().getPackageName())) {
                    intent.addFlags(268435456);
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchDisplayId(displayId);
                    displayContext.startActivity(intent, options.toBundle());
                    return;
                }
                HwPCUtils.log(TAG, " restoreApp skip intent, displayId:" + displayId);
            } catch (ActivityNotFoundException e) {
                HwPCUtils.log(TAG, "startActivity error.");
            } catch (FileUriExposedException e2) {
                HwPCUtils.log(TAG, "restoreApp FileUriExposedException.");
            } catch (IllegalArgumentException e3) {
                HwPCUtils.log(TAG, "restoreApp argument error, displayId:" + displayId);
            } catch (IllegalStateException e4) {
                HwPCUtils.log(TAG, "restoreApp state error, displayId:" + displayId);
            }
        }
    }

    public void lockScreen(boolean lock) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "lockScreen checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        if (isInWindowsCastMode() && lock) {
            HwPCUtils.bdReport(this.mContext, 10065, BuildConfig.FLAVOR);
        }
        sendToMessengers(8, lock ? 1 : 0, -1, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bdReportSameSrcStatus(boolean isConnected) {
        if (isConnected) {
            HwPCUtils.bdReport(this.mContext, 10001, "same src is connected");
        } else {
            HwPCUtils.bdReport(this.mContext, 10001, "same src is disconnected");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bdReportDiffSrcStatus(boolean isConnected) {
        if (isConnected) {
            HwPCUtils.bdReport(this.mContext, 10002, "diff src is connected");
            if (HwPCUtils.enabledInPad()) {
                HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_PAD_DESKTOP_MODE, "{status:enter}");
                return;
            }
            return;
        }
        HwPCUtils.bdReport(this.mContext, 10002, "diff src is disconnected");
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_PAD_DESKTOP_MODE, "{status:exit}");
        }
    }

    private void bdReportConnectDisplay(int displayId) {
        String displayTypeReport;
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null) {
            String name = display.getName();
            String displayTypeReport2 = "{\"displayType\":\"Display<" + name + "> type is ";
            int type = DisplayEx.getType(display);
            if (type == 2) {
                displayTypeReport = displayTypeReport2 + " HDMI";
            } else if (type == 3) {
                displayTypeReport = displayTypeReport2 + " WIFI";
            } else if (type == 4) {
                displayTypeReport = displayTypeReport2 + " OVERLAY";
            } else if (type != 5) {
                displayTypeReport = displayTypeReport2 + " UNKNOWN";
            } else {
                displayTypeReport = displayTypeReport2 + " VIRTUAL";
            }
            if (HwFoldScreenManagerEx.isInwardFoldDevice()) {
                String displayTypeReport3 = displayTypeReport + ". InwardFoldDevice, display mode: ";
                int displayMode = HwFoldScreenManagerEx.getDisplayMode();
                if (displayMode == 1) {
                    displayTypeReport = displayTypeReport3 + "FULL";
                } else if (displayMode == 2) {
                    displayTypeReport = displayTypeReport3 + "MAIN";
                } else if (displayMode == 3) {
                    displayTypeReport = displayTypeReport3 + "SUB";
                } else if (displayMode != 4) {
                    displayTypeReport = displayTypeReport3 + "UNKNOWN";
                } else {
                    displayTypeReport = displayTypeReport3 + "COORDINATION";
                }
            }
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_TYPE, displayTypeReport + "\"}");
            Point size = new Point();
            display.getRealSize(size);
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_SIZE, "{\"displaySize\":\"Display<" + name + "> width:" + size.x + " height:" + size.y + "\"}");
            StringBuilder sb = new StringBuilder();
            sb.append(size.x);
            sb.append(BuildConfig.FLAVOR);
            BigInteger biWidth = new BigInteger(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append(size.y);
            sb2.append(BuildConfig.FLAVOR);
            BigInteger value = biWidth.gcd(new BigInteger(sb2.toString()));
            HwPCUtils.bdReport(this.mContext, 10011, "Display<" + name + "> ratio:" + (size.x / value.intValue()) + ":" + (size.y / value.intValue()));
        }
    }

    private void bdReportOnDisplayAdded(int displayId) {
        if (isWifiPCMode(displayId) || isConnectFromThirdApp(displayId) > 0) {
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_BY_WIRELESS, BuildConfig.FLAVOR);
        } else if (HwPCUtils.enabledInPad()) {
            if (!isPadPCDisplay(displayId)) {
                HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_BY_WIRED, BuildConfig.FLAVOR);
            }
        } else if (isWiredDisplay(displayId)) {
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_BY_WIRED, BuildConfig.FLAVOR);
        } else {
            HwPCUtils.bdReport(this.mContext, (int) Constant.REPORT_CONNECT_DISPLAY_BY_WIRELESS, BuildConfig.FLAVOR);
        }
        bdReportConnectDisplay(displayId);
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
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            return HwMultiDisplayPowerManager.getDefault().getScreenPowerOn();
        }
        HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
        return true;
    }

    public void setScreenPower(boolean powerOn) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
            return;
        }
        HwMultiDisplayPowerManager.getDefault().setScreenPowerInner(powerOn, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lightPhoneScreen() {
        HwMultiDisplayPowerManager.getDefault().setScreenPowerInner(true, false);
    }

    public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "checkCallingPermission failed " + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "dispatchKeyEvent " + ke);
        int keyCode = ke.getKeyCode();
        if (ke.getAction() == 0) {
            if (keyCode == 3) {
                sendToMessengers(10);
            } else if (keyCode == 4) {
                sendToMessengers(11);
            } else if (keyCode == 62) {
                sendToMessengers(12);
            } else if (keyCode == 118) {
                sendToMessengers(13);
            }
        } else if (keyCode == 187) {
            sendToMessengers(9);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    private void registerAudioDeviceSelect() {
        if (!HwPCUtils.enabledInPad() && !HwPCUtils.isHiCarCastMode()) {
            this.mMultiDisplayAudioMgr.registerAudioDeviceSelect();
        }
    }

    private void unregisterAudioDeviceSelect() {
        if (!HwPCUtils.enabledInPad() && !HwPCUtils.isHiCarCastMode()) {
            this.mMultiDisplayAudioMgr.unregisterAudioDeviceSelect();
        }
    }

    private void registerScreenOnEvent() {
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ALARM_ALERT_CONFLICT);
        try {
            ContextEx.registerReceiverAsUser(this.mContext, this.mAlarmClockReceiver, UserHandleEx.ALL, filter1, BROADCAST_PERMISSION, (Handler) null);
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

    private void registerPowerOnEvent() {
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
        try {
            ContextEx.registerReceiverAsUser(this.mContext, this.powerReceiver, UserHandleEx.ALL, powerFilter, CHANGE_POWERMODE_PERMISSION, (Handler) null);
        } catch (IllegalArgumentException e) {
            HwPCUtils.log(TAG, "registerPowerOnEvent IllegalArgumentException");
        }
    }

    private void unRegisterPowerOnEvent() {
        try {
            this.mContext.unregisterReceiver(this.powerReceiver);
        } catch (IllegalArgumentException e) {
            HwPCUtils.log(TAG, "unRegisterScreenOnEvent IllegalArgumentException");
        }
    }

    private void enableFingerprintSlideSwitch() {
        try {
            ContentResolver resolver = this.mContext.getContentResolver();
            int userId = ActivityManagerEx.getCurrentUser();
            if (SettingsEx.Systemex.getIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 0, userId) == 0) {
                HwPCUtils.log(TAG, "enableFingerprintSlideSwitch");
                SettingsEx.Systemex.putIntForUser(resolver, FINGERPRINT_SLIDE_SWITCH, 1, userId);
            }
        } catch (Exception e) {
            HwPCUtils.log(TAG, "enableFingerprintSlideSwitch Exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerprintSlideSwitch() {
        try {
            this.mAMS.updateFingerprintSlideSwitch();
        } catch (Exception e) {
            HwPCUtils.log(TAG, "updateFingerprintSlideSwitch Exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIMEWithHardKeyboardState(boolean switchToPcMode) {
        long ident = Binder.clearCallingIdentity();
        if (switchToPcMode) {
            try {
                this.mIMEWithHardKeyboardState = SettingsEx.Secure.getInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
                HwPCUtils.log(TAG, "switch to PcMode, IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
                if (HwPCUtils.enabledInPad()) {
                    SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0);
                } else {
                    SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 1);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            HwPCUtils.log(TAG, "switch To PhoneMode, update IME With Hard Keyboard State:" + this.mIMEWithHardKeyboardState);
            SettingsEx.Secure.putInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", this.mIMEWithHardKeyboardState);
        }
    }

    private boolean isCalling() {
        this.mPhoneState = getPhoneState();
        return this.mPhoneState != 0;
    }

    private int getPhoneState() {
        int phoneState = 0;
        int simCount = this.mTelephonyPhone.getPhoneCount();
        for (int i = 0; i < simCount; i++) {
            phoneState = TelephonyManagerEx.getCallState(this.mTelephonyPhone, i);
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
                UiThreadEx.getHandler().post(new Runnable() {
                    /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass15 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (HwPCManagerService.this.mCallingToast != null) {
                            HwPCManagerService.this.mCallingToast.cancel();
                        }
                        if (HwPCUtils.isValidExtDisplayId(displayId)) {
                            HwPCManagerService hwPCManagerService = HwPCManagerService.this;
                            Context context = context;
                            hwPCManagerService.mCallingToast = Toast.makeText(context, context.getResources().getString(HwPartResourceUtils.getResourceId("desktop_mode_exit_incall")), 1);
                        } else {
                            HwPCManagerService hwPCManagerService2 = HwPCManagerService.this;
                            Context context2 = context;
                            hwPCManagerService2.mCallingToast = Toast.makeText(context2, context2.getResources().getString(HwPartResourceUtils.getResourceId("desktop_mode_enter_incall")), 1);
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
            case 1006:
                return setOverScanMode(0);
            case 1007:
                return setOverScanMode(1);
            case 1008:
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
            return 1007;
        }
        if (mode == 2) {
            return 1008;
        }
        return 1006;
    }

    private void uploadPcDisplaySizePro() {
        int[] size = getPcDisplaySize();
        if (size.length >= 2) {
            SystemPropertiesEx.set("hw.pc.display.width", String.valueOf(size[0]));
            SystemPropertiesEx.set("hw.pc.display.height", String.valueOf(size[1]));
        }
    }

    private int[] getPcDisplaySize() {
        Display.Mode mode;
        DisplayInfoEx displayInfo = getPcDisplayInfo();
        return (displayInfo == null || (mode = displayInfo.getDefaultMode()) == null) ? new int[0] : new int[]{mode.getPhysicalWidth(), mode.getPhysicalHeight()};
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DisplayInfoEx getPcDisplayInfo() {
        Display display = getPcDisplay(this.mContext);
        if (display == null) {
            return null;
        }
        DisplayInfoEx displayInfo = new DisplayInfoEx();
        DisplayEx.getDisplayInfo(display, displayInfo);
        return displayInfo;
    }

    private Display getPcDisplay(Context context) {
        Display[] displays;
        DisplayManager dm = (DisplayManager) context.getSystemService("display");
        if (!(dm == null || (displays = dm.getDisplays()) == null || displays.length <= 0)) {
            for (int i = displays.length - 1; i >= 0; i--) {
                if (displays[i] != null && HwPCUtils.isValidExtDisplayId(displays[i].getDisplayId())) {
                    return displays[i];
                }
            }
        }
        HwPCUtils.log(TAG, "getPcDisplay not find PCDisplay");
        return null;
    }

    public void setFocusedPCDisplayId(String reason) {
        if (this.mWindowManagerInternalEx == null) {
            this.mWindowManagerInternalEx = new WindowManagerInternalEx();
        }
    }

    private boolean isWelinkDisplayInPad() {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager != null) {
            Display[] displays = displayManager.getDisplays();
            for (Display dis : displays) {
                if (dis != null && ESHARE_DISPLAY_NAME.equals(dis.getName()) && "com.huawei.works".equals(DisplayEx.getOwnerPackageName(dis))) {
                    HwPCUtils.log(TAG, "isWelinkDisplayInPad return true ");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConnectExtDisplayFromPkg(String pkgName) {
        if (HwPCUtils.enabledInPad() && "com.huawei.works".equals(pkgName)) {
            return isWelinkDisplayInPad();
        }
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
        return isFirstDisplayConnectFromPkg || isSecondDisplayConnectFromPkg;
    }

    public void showImeStatusIcon(int iconResId, String pkgName) {
        if (DEBUG) {
            HwPCUtils.log(TAG, String.format("PCMS showImeStatusIcon:%s,%s", Integer.valueOf(iconResId), pkgName));
        }
        if (validateImeCall(pkgName)) {
            Bundle bundle = new Bundle();
            bundle.putString(PKG_NAME_DATA, pkgName);
            sendToMessengers(14, iconResId, -1, bundle);
        }
    }

    public void hideImeStatusIcon(String pkgName) {
        HwPCUtils.log(TAG, "PCMS hideImeStatusIcon");
        if (validateImeCall(pkgName)) {
            sendToMessengers(15);
        }
    }

    private boolean validateImeCall(String pkgName) {
        String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (DEBUG) {
            HwPCUtils.log(TAG, "PCMS callingApp: " + callingApp + ", pkg=" + pkgName);
        }
        if (callingApp == null || !callingApp.equals(pkgName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public class PCSettingsObserver extends ContentObserver {
        PCSettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            if (HwPCUtils.enabledInPad()) {
                ContentResolverExt.registerContentObserver(resolver, SettingsEx.Systemex.getUriFor("screen_off_timeout"), false, this, 0);
            }
            ContentResolverExt.registerContentObserver(resolver, Settings.Global.getUriFor("device_provisioned"), false, this, 0);
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor(SettingsEx.Secure.getUserSetupComplete()), false, this, 0);
        }

        @Override // android.database.ContentObserver
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
                if (c == 0) {
                    updateScreenOffTimeoutSettings();
                } else if (c == 1 || c == 2) {
                    deviceChanged();
                }
            }
        }

        private synchronized void updateScreenOffTimeoutSettings() {
            ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
            try {
                HwPCManagerService.this.mLockScreenTimeout = SettingsEx.Systemex.getIntForUser(resolver, "screen_off_timeout", 0, -2);
                if (HwPCUtils.enabledInPad()) {
                    if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) || !HwPCUtils.isPcCastModeInServer()) {
                        HwPCManagerService.this.mPadLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                        SettingsEx.Secure.putInt(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout);
                        HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadLockScreenTimeout);
                    } else {
                        HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = HwPCManagerService.this.mLockScreenTimeout;
                        SettingsEx.Secure.putInt(resolver, "pad_desktop_mode_screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                        HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings PAD_DESKTOP_MODE_SCREEN_OFF_TIMEOUT=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                    }
                }
            } catch (Exception e) {
                HwPCUtils.log(HwPCManagerService.TAG, "catch Exception");
            }
            HwPCUtils.log(HwPCManagerService.TAG, "updateScreenOffTimeoutSettings " + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
        }

        /* access modifiers changed from: package-private */
        public synchronized void readScreenOffSettings() {
            try {
                if (HwPCUtils.enabledInPad()) {
                    ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                    HwPCManagerService.this.mLockScreenTimeout = SettingsEx.Systemex.getIntForUser(resolver, "screen_off_timeout", 0, -2);
                    HwPCManagerService.this.mPadLockScreenTimeout = SettingsEx.Secure.getIntForUser(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mLockScreenTimeout, -2);
                    SettingsEx.Secure.putInt(resolver, "pad_screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout);
                    HwPCManagerService.this.mPadDesktopModeLockScreenTimeout = SettingsEx.Secure.getIntForUser(resolver, "pad_desktop_mode_screen_off_timeout", 600000, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "read screen off settings current=" + HwPCManagerService.this.mLockScreenTimeout + " pad=" + HwPCManagerService.this.mPadLockScreenTimeout + " pc=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                }
            } catch (Exception e) {
                HwPCUtils.log(HwPCManagerService.TAG, "catch Exception");
            }
            return;
        }

        /* access modifiers changed from: package-private */
        public synchronized void restoreScreenOffSettings() {
            if (HwPCUtils.enabledInPad()) {
                ContentResolver resolver = HwPCManagerService.this.mContext.getContentResolver();
                if (!HwPCManagerService.isDesktopMode(HwPCManagerService.this.mProjMode) || !HwPCUtils.isPcCastModeInServer()) {
                    SettingsEx.Systemex.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadLockScreenTimeout=" + HwPCManagerService.this.mPadLockScreenTimeout);
                } else {
                    SettingsEx.Systemex.putIntForUser(resolver, "screen_off_timeout", HwPCManagerService.this.mPadDesktopModeLockScreenTimeout, -2);
                    HwPCUtils.log(HwPCManagerService.TAG, "restoreScreenOffSettings mPadDesktopModeLockScreenTimeout=" + HwPCManagerService.this.mPadDesktopModeLockScreenTimeout);
                }
            }
        }

        private void deviceChanged() {
            int displayId;
            boolean wasProvisioned = HwPCManagerService.this.mProvisioned;
            boolean wasUserSetupComplete = HwPCManagerService.this.mUserSetupComplete;
            boolean isProvisioned = HwPCManagerService.this.deviceIsProvisioned();
            boolean isUserSetupComplete = HwPCManagerService.this.isUserSetupComplete();
            HwPCManagerService hwPCManagerService = HwPCManagerService.this;
            hwPCManagerService.mProvisioned = isProvisioned;
            hwPCManagerService.mUserSetupComplete = isUserSetupComplete;
            if (((isProvisioned && !wasProvisioned) || (isUserSetupComplete && !wasUserSetupComplete)) && (displayId = HwPCManagerService.this.findPCDisplayId()) != -1) {
                HwPCManagerService.this.scheduleDisplayAdded(displayId);
            }
        }
    }

    private void registerBluetoothReceiver() {
        if (this.mWifiP2pManager == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        }
        this.mFreq = 0;
        WifiP2pManager wifiP2pManager = this.mWifiP2pManager;
        if (wifiP2pManager != null) {
            if (this.mWifiP2pChannel == null) {
                this.mWifiP2pChannel = wifiP2pManager.initialize(this.mContext, this.mHandler.getLooper(), null);
            }
            WifiP2pManager.Channel channel = this.mWifiP2pChannel;
            if (channel != null) {
                this.mWifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                    /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass16 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
                    public void onGroupInfoAvailable(WifiP2pGroup info) {
                        if (info != null) {
                            HwPCManagerService.this.mFreq = WifiP2pGroupExt.getFrequency(info);
                            if (HwPCManagerService.this.mFreq >= HwPCManagerService.MIN_FREQ && HwPCManagerService.this.mFreq <= HwPCManagerService.MAX_FREQ) {
                                IntentFilter filter = new IntentFilter();
                                filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
                                HwPCManagerService.this.mContext.registerReceiver(HwPCManagerService.this.mWifiPCReceiver, filter);
                                HwPCManagerService.this.mIsNeedUnRegisterBluetoothReciver = true;
                                HwPCManagerService.this.mBluetoothReminderDialog.dismissDialog();
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (bluetoothAdapter != null) {
                                    HwPCManagerService.this.mBluetoothStateOnEnter = bluetoothAdapter.isEnabled();
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
        int i = this.mFreq;
        if (i >= MIN_FREQ && i <= MAX_FREQ) {
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
        DisplayManager displayManager;
        Display display;
        if (displayid == -1 || (displayManager = this.mDisplayManager) == null || (display = displayManager.getDisplay(displayid)) == null || DisplayEx.getType(display) != 3) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int isConnectFromThirdApp(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null && DisplayEx.getType(display) == 5) {
            if ("com.hpplay.happycast".equals(DisplayEx.getOwnerPackageName(display))) {
                HwPCUtils.bdReport(this.mContext, 10061, BuildConfig.FLAVOR);
                return 1;
            } else if ("com.huawei.works".equals(DisplayEx.getOwnerPackageName(display)) && ESHARE_DISPLAY_NAME.equals(display.getName())) {
                return 2;
            } else {
                if (CAST_PLUS_VIRTUALDISPLAY_NAME.equals(display.getName())) {
                    return 3;
                }
                HwPCUtils.log(TAG, "unknown third-party app");
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void launchWeLink() {
        try {
            String encodedUri = URLEncoder.encode("ui://welink.wirelessdisplay/home", "utf-8");
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse("welink://works.huawei.com?uri=" + encodedUri));
            intent.setFlags(335544320);
            intent.putExtra("src", 203);
            intent.putExtra("target", MSG_SWITCH_AUDIO_OUTPUT_TO_PAD);
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
        InputManagerServiceEx.DefaultHwInputManagerLocalService inputManager = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        if (inputManager != null) {
            inputManager.setPointerIconTypeAndKeep(iconId, keep);
        }
    }

    public void setCustomPointerIcon(PointerIcon icon, boolean keep) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setCustomPointerIcon checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        InputManagerServiceEx.DefaultHwInputManagerLocalService inputManager = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
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
        this.mHandler.removeMessages(MSG_DP_STATE_CHANGED);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DP_STATE_CHANGED, Boolean.valueOf(dpState)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HwPCMultiDisplaysManager.CastingDisplay get1stDisplay() {
        return this.mPcMultiDisplayMgr.get1stDisplay();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HwPCMultiDisplaysManager.CastingDisplay get2ndDisplay() {
        return this.mPcMultiDisplayMgr.get2ndDisplay();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDPLinkErrorDialog(Context context, String tip) {
        dismissDpLinkErrorDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, HwPartResourceUtils.getResourceId("Theme_Emui_Dialog_Alert"));
        String title = BuildConfig.FLAVOR;
        String tip2 = String.format(tip, title);
        if (!HwPCUtils.enabledInPad()) {
            title = this.mContext.getString(HwPartResourceUtils.getResourceId("proj_mode_notification_label"));
        }
        this.mShowDpLinkErrorTipDialog = builder.setTitle(title).setPositiveButton(33685817, new DialogInterface.OnClickListener() {
            /* class com.huawei.server.pc.HwPCManagerService.AnonymousClass19 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        }).setMessage(tip2).create();
        this.mShowDpLinkErrorTipDialog.getWindow().setType(2008);
        this.mShowDpLinkErrorTipDialog.show();
        this.mShowDpLinkErrorTipDialog.getWindow().getAttributes().setTitle("ShowDpLinkErrorTipDialog");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissDpLinkErrorDialog() {
        AlertDialog alertDialog = this.mShowDpLinkErrorTipDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
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
        if (currTime - this.mPrevTimeForBroadcast <= BROADCAST_SEND_INTERVAL) {
            return true;
        }
        this.mPrevTimeForBroadcast = currTime;
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastForClearLighterDrawed() {
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
        return keyCode == 20 || keyCode == 269 || keyCode == 271 || keyCode == MSG_DP_STATE_CHANGED || keyCode == 22 || keyCode == MSG_TASK_PROFILE_LOCKED || keyCode == 268 || keyCode == 270 || keyCode == 23;
    }

    private static boolean isVolumeKey(KeyEvent ev) {
        int keyCode = ev.getKeyCode();
        return keyCode == MSG_DP_LINK_STATE_CABLE_OUT || keyCode == MSG_CLEAR_LIGHTER_DRAWED;
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
    /* access modifiers changed from: public */
    private void filterScrollForPCMode() {
        if (HwPCUtils.isPcCastModeInServer() && HwWindowManager.hasLighterViewInPCCastMode()) {
            shouldInterceptInputEvent(null, true);
        }
    }

    private boolean isFullScreenApp(Rect bounds) {
        if (bounds == null) {
            return true;
        }
        if (this.mPCDisplayInfo != null && bounds.left == 0 && bounds.top == 0 && bounds.right == this.mPCDisplayInfo.getLogicalWidth() && bounds.bottom == this.mPCDisplayInfo.getLogicalHeight()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldSendBroadcastForClearLighterDrawed(KeyEvent ev, boolean forScroll) {
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
        } else if (ev == null || isDapKey(ev.getKeyCode()) || KeyEventEx.isSystemKey(ev.getKeyCode())) {
            this.mHandler.removeMessages(MSG_CLEAR_LIGHTER_DRAWED);
            Message msg = this.mHandler.obtainMessage(MSG_CLEAR_LIGHTER_DRAWED);
            msg.obj = ev;
            msg.arg1 = forScroll ? 1 : 0;
            this.mHandler.sendMessage(msg);
            return false;
        } else {
            HwPCUtils.log(TAG, "shouldInterceptInputEvent only dap or volume for PPT accept when lighter view appear");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPcCastingDisplayId(int displayId) {
        SystemPropertiesEx.set("hw.pc.casting.displayid", String.valueOf(displayId));
    }

    private boolean isPadPCDisplay(int displayId) {
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null || DisplayEx.getType(display) != 2 || !"HUAWEI PAD PC Display".equals(display.getName())) {
            return false;
        }
        HwPCUtils.log(TAG, "isPadPCDisplay: is pad display true");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findPCDisplayId() {
        Display[] displays;
        int displayId = -1;
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager != null && (displays = displayManager.getDisplays()) != null && displays.length > 0) {
            int i = displays.length - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                if (displays[i] == null || displays[i].getDisplayId() == 0) {
                    HwPCUtils.log(TAG, "findPCDisplayId display wrong continue!");
                } else if (isWiredDisplay(displays[i].getDisplayId())) {
                    displayId = displays[i].getDisplayId();
                    break;
                }
                i--;
            }
        }
        HwPCUtils.log(TAG, "findPCDisplayId return val:" + displayId);
        return displayId;
    }

    public void onTaskMovedToBack(int taskId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "onTaskMovedToBack, checkCallingPermission failed. " + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "onTaskMovedToBack taskId=" + taskId);
        HiCarManager hiCarManager = this.mHiCarManager;
        if (hiCarManager != null && hiCarManager.isHiCarHome(taskId)) {
            sendToMessengers(20, taskId, -1, null);
        }
    }

    public void onTaskMovedToFront(int taskId) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "onTaskMovedToFront, checkCallingPermission failed. " + Binder.getCallingPid());
            return;
        }
        HwPCUtils.log(TAG, "onTaskMovedToFront taskId=" + taskId);
        sendToMessengers(18, taskId, -1, null);
    }

    private void sendToMessengers(int what) {
        sendToMessengers(what, -1, -1, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendToMessengers(int what, int arg1, int arg2, Object obj) {
        synchronized (this.mMessengers) {
            Iterator<Messenger> messengerIterator = this.mMessengers.iterator();
            while (messengerIterator.hasNext()) {
                try {
                    Message message = Message.obtain();
                    message.what = what;
                    message.arg1 = arg1;
                    message.arg2 = arg2;
                    message.obj = obj;
                    messengerIterator.next().send(message);
                } catch (RemoteException e) {
                    messengerIterator.remove();
                    HwPCUtils.log(TAG, "sendToMessengers RemoteException. what=" + what);
                }
            }
        }
    }

    public void saveNeedRestartAppIntent(List<Intent> intents) {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "saveNeedRestartAppIntent, checkCallingPermission failed. " + Binder.getCallingPid());
            return;
        }
        synchronized (this.mNeedRestartIntentList) {
            this.mNeedRestartIntentList.clear();
            this.mNeedRestartIntentList.addAll(intents);
        }
    }

    public boolean isDisallowLockScreenForHwMultiDisplay() {
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "isDisallowLockScreenForHwMultiDisplay, checkCallingPermission failed. " + Binder.getCallingPid());
            return false;
        } else if (HwPCUtils.isHiCarCastMode() || HwVideoCallCastManager.getDefault().isInCastModeForVideoCall()) {
            return true;
        } else {
            return false;
        }
    }

    private CastMode getCurrentCastMode() {
        if (HwPCUtils.isPcCastModeInServer()) {
            return HwPCUtils.isHiCarCastMode() ? CastMode.HICAR : CastMode.PC;
        }
        return CastMode.INVALID;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectWifiProjection() {
        if ((isConnectFromThirdApp(get1stDisplay().mDisplayId) == 3) || !isDesktopMode(this.mProjMode) || this.mDisplayManager == null) {
            HwPCUtils.log(TAG, "Disconnect wifi projection by startService.");
            Intent intent = new Intent();
            intent.putExtra(KEY_IS_DISSCONNECT, true);
            intent.setComponent(this.mExplorerComponent);
            this.mContext.startService(intent);
            return;
        }
        HwPCUtils.log(TAG, "Disconnect wifi projection by displayManager.");
        DisplayManagerEx.disconnectWifiDisplay(this.mDisplayManager);
    }

    public void showDialogForSwitchDisplay(int displayId, String pkgName) {
        Bundle bundle = new Bundle();
        bundle.putString(PKG_NAME_DATA, pkgName);
        sendToMessengers(MSG_APP_RUNNING_ON_OTHER_DISPLAY, displayId, -1, bundle);
    }

    public boolean isAvoidShowDefaultKeyguard(int displayId) {
        Display display;
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "isAvoidShowDefaultKeyguard checkCallingPermission failed. " + Binder.getCallingPid());
            return false;
        } else if (displayId == -1 || displayId == 0 || (display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(displayId)) == null) {
            return false;
        } else {
            if (HwPCUtils.enabledInPad()) {
                if ((DisplayEx.getType(display) != 2 || !"HUAWEI PAD PC Display".equals(display.getName())) && !HwPCUtils.isPcCastModeInServer()) {
                    return false;
                }
                return true;
            } else if ((!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) && !HwMultiDisplayUtils.getWindowsCastDisplayName().equals(display.getName()) && !HwVideoCallCastManager.getDefault().isDisplayForVideoCall(displayId) && !HwVideoCallCastManager.getDefault().isConnForVideoCall(this.mContext, displayId)) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void setPadAssistant(boolean isAssistWithPad) {
        HwPCUtils.log(TAG, "HwPCManagerService.setPadAssistant:" + isAssistWithPad);
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.setPadAssistant(isAssistWithPad);
        }
    }

    public boolean isPadAssistantMode() {
        return HwPCUtils.isPadAssistantMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkAppName(int taskId) {
        this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
        HwRecentTaskInfo targetTask = HwActivityTaskManager.getHwRecentTaskInfo(taskId);
        if (targetTask == null || targetTask.topActivity == null || !"com.tencent.mm".equals(targetTask.topActivity.getPackageName())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reOpenWechat() {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setComponent(this.mWechatComponent);
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchDisplayId(get1stDisplay().mDisplayId);
        try {
            this.mContext.startActivity(intent, options.toBundle());
        } catch (ActivityNotFoundException e) {
            HwPCUtils.log(TAG, "reOpenWechat can not find activity.");
        }
    }

    public void setIsInBasicMode(boolean isInBasicMode) {
        HwPCUtils.log(TAG, "setIsInBasicMode " + isInBasicMode);
        if (!checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwPCUtils.log(TAG, "setIsInBasicMode checkCallingPermission failed" + Binder.getCallingPid());
            return;
        }
        HwMultiDisplayUtils.getInstance();
        HwMultiDisplayUtils.setIsInBasicMode(isInBasicMode);
    }

    public boolean isInBasicMode() {
        return HwMultiDisplayUtils.getInstance().isInBasicMode();
    }

    public boolean isModeSupportDrag() {
        return isInWindowsCastMode() || isInSinkWindowsCastMode() || isInBasicMode();
    }

    private void removeTaskFromRecent() {
        HwPCUtils.log(TAG, "remove incallUI task from recent list when hicar display added.");
        List<ActivityManager.RecentTaskInfo> recentTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRecentTasks(20, 1);
        if (recentTasks != null && recentTasks.size() > 0) {
            recentTasks.stream().filter($$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg.INSTANCE).map($$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGikN2Xxs.INSTANCE).forEach(new Consumer() {
                /* class com.huawei.server.pc.$$Lambda$HwPCManagerService$yTuNZ9IMttTCQvmVN7TpEyvJXT4 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    HwPCManagerService.this.lambda$removeTaskFromRecent$11$HwPCManagerService((Integer) obj);
                }
            });
        }
    }

    static /* synthetic */ boolean lambda$removeTaskFromRecent$9(ActivityManager.RecentTaskInfo rTask) {
        String packageName = rTask.baseActivity == null ? BuildConfig.FLAVOR : rTask.baseActivity.getPackageName();
        return "com.android.incallui".equals(packageName) || Constant.MEETIME_PACKAGE_NAME.equals(packageName);
    }

    public /* synthetic */ void lambda$removeTaskFromRecent$11$HwPCManagerService(Integer taskId) {
        this.mAMS.removeTask(taskId.intValue());
    }

    public void setCarApp(String app) {
        HiCarManager hiCarManager;
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API) && (hiCarManager = this.mHiCarManager) != null) {
            hiCarManager.setCarApp(app);
        }
    }

    public List<String> getCarAppList() {
        HiCarManager hiCarManager;
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API) && (hiCarManager = this.mHiCarManager) != null) {
            return hiCarManager.getCarAppList();
        }
        return null;
    }

    public void removeCarApp(String app) {
        HiCarManager hiCarManager;
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API) && (hiCarManager = this.mHiCarManager) != null) {
            hiCarManager.removeCarApp(app);
        }
    }

    public String getHwRioRule(HwRioClientInfo info) {
        return HwRioViewManager.getInstance().getHwRioRule(info);
    }

    public void enableRio(String mode, int displayId, IHwRioRuleCb callback, List<String> whiteList) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwRioViewManager.getInstance().enableRio(mode, displayId, callback, whiteList);
        }
    }

    public void disableRio(String mode) {
        if (checkCallingPermission(PERMISSION_PC_MANAGER_API)) {
            HwRioViewManager.getInstance().disableRio(mode);
        }
    }

    public boolean isRioEnable(int displayId, String packageName) {
        return HwRioViewManager.getInstance().isRioEnable(displayId, packageName);
    }
}
