package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hdm.HwDeviceManager;
import android.media.IRingtonePlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SplitNotificationUtils;
import android.view.IWindowManager;
import android.widget.RemoteViews;
import com.android.server.HwBluetoothBigDataService;
import com.android.server.HwBluetoothManagerServiceEx;
import com.android.server.HwServiceFactory;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.appactcontrol.HwAppActController;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.lights.LightsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.PreferencesHelper;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.UserManagerService;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
import com.android.server.wm.HwSnsVideoManager;
import com.android.server.wm.HwWmConstants;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.os.HwVibrator;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.recsys.aidl.HwRecSysAidlInterface;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HwNotificationManagerService extends NotificationManagerService {
    private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final String BIND_ACTION = "com.huawei.recsys.action.THIRD_REQUEST_ENGINE";
    private static final long BIND_TIMEOUT = 10000;
    private static final int CLOSE_SAVE_POWER = 0;
    private static final String CONTACTS_PKGNAME = "com.android.contacts";
    private static final String CONTACTS_PKGNAME_HW = "com.huawei.contacts";
    private static final String CONTENTVIEW_REVERTING_FLAG = "HW_CONTENTVIEW_REVERTING_FLAG";
    private static final boolean CUST_DIALER_ENABLE = SystemProperties.get("ro.product.custom", HwBluetoothManagerServiceEx.DEFAULT_PACKAGE_NAME).contains("docomo");
    private static final boolean DEBUG = true;
    private static final String DEFAULTAPPROVIED_WHITEAPPS = "default_approvied_whiteapps";
    private static final String DEFAULT_CHANNEL_VIBRATOR_TYPE = "default";
    private static final String DIALER_PKGNAME = "com.android.dialer";
    private static final boolean DISABLE_MULTIWIN = SystemProperties.getBoolean("ro.huawei.disable_multiwindow", false);
    private static final int EVENT_MARK_AS_GAME = 4;
    private static final int EVENT_MOVE_BACKGROUND = 2;
    private static final int EVENT_MOVE_FRONT = 1;
    private static final int EVENT_REPLACE_FRONT = 3;
    private static final List<String> EXPANDEDNTF_PKGS = new ArrayList();
    private static final String FOLLOW_SYSTEM_RINGTONE = "follow_system_ringtone";
    private static final boolean HWMULTIWIN_ENABLED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    static final int INIT_CFG_DELAY = 10000;
    private static final boolean IS_CHINA_REGION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    private static final boolean IS_VIBRATE_RESERVE = SystemProperties.getBoolean("hw_sc.vibrate.reserve", false);
    private static final String KEY_NOTIFICATION_VIBRATOR_SETTING = "notification_vibrator_setting";
    private static final String KEY_SMART_NOTIFICATION_SWITCH = "smart_notification_switch";
    private static final String KEY_TRUST_SPACE_BADGE_SWITCH = "trust_secure_hint_enable";
    private static final String KEY_VIBRATE_WHEN_RINGING = "hw_vibrate_when_ringing";
    private static final String MMS_PKGNAME = "com.android.mms";
    private static final int MORE_PRIORITY = 5;
    private static final int NEED_REPEAT = 0;
    private static final String NOTIFICATION_ACTION_ALLOW = "com.huawei.notificationmanager.notification.allow";
    private static final String NOTIFICATION_ACTION_REFUSE = "com.huawei.notificationmanager.notification.refuse";
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    private static final String[] NOTIFICATION_NARROW_DISPLAY_APPS = {CONTACTS_PKGNAME_HW, CONTACTS_PKGNAME};
    private static final String[] NOTIFICATION_WHITE_APPS_PACKAGE = {HwWmConstants.INTELLIGENT_PKG_NAME};
    private static final String[] NOTIFICATION_WHITE_LIST = {"com.huawei.message", MMS_PKGNAME, CONTACTS_PKGNAME_HW, CONTACTS_PKGNAME, PHONE_PKGNAME, "com.android.deskclock", "com.android.calendar", "com.huawei.calendar", HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE, PackageManagerServiceEx.PLATFORM_PACKAGE_NAME, "com.android.incallui", "com.android.phone.recorder", "com.android.cellbroadcastreceiver", TELECOM_PKGNAME, "com.huawei.ohos.call", "com.huawei.meetime", "com.huawei.deskclock"};
    private static final String NULL_VIBRATOR = "null_vibrator";
    private static final int OPEN_SAVE_POWER = 3;
    private static final String PACKAGE_NAME_HSM = "com.huawei.systemmanager";
    private static final Map<String, String> PATTERN_TYPE = new HashMap<String, String>() {
        /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass1 */

        {
            put("standard_vibrator", "haptic.pattern.type1");
            put("standard_vibrator_steamwhistle", "haptic.pattern.type2");
            put("standard_vibrator_flicker", "haptic.pattern.type3");
            put(HwNotificationManagerService.TICKING_VIBRATOR, "haptic.pattern.type4");
            put("standard_vibrator_dance", "haptic.pattern.type5");
            put("standard_vibrator_knock", "haptic.pattern.type6");
            put("standard_vibrator_woodpecker", "haptic.pattern.type7");
            put("standard_vibrator_emergency", "haptic.pattern.type8");
        }
    };
    private static final String PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
    private static final String PHONE_PKGNAME = "com.android.phone";
    private static final String[] PLUS_NOTIFICATION_WHITE_LIST = {HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME};
    private static final String POWER_MODE = "power_mode";
    private static final String POWER_SAVER_NOTIFICATION_WHITELIST = "super_power_save_notification_whitelist";
    private static final String PROPERTY_TV = "ro.build.characteristics";
    private static final String RIDEMODE_NOTIFICATION_WHITE_LIST = "com.huawei.ridemode,com.huawei.sos,com.android.phone,com.android.server.telecom,com.android.incallui,com.android.deskclock,com.android.cellbroadcastreceiver,com.huawei.ohos.call,com.huawei.meetime,com.huawei.deskclock";
    private static final String SERVER_PAKAGE_NAME = "com.huawei.recsys";
    private static final String SHUTDOWN_LIMIT_POWERMODE = "shutdomn_limit_powermode";
    private static final String TABLE_COLUM_NOTIFICATION_VIBRATOR_TYPE = "vibrator_type";
    static final String TAG = "HwNotificationService";
    static final String TAG_VIBRATE = "HwNotificationService_Vibrate";
    private static final String TELECOM_PKGNAME = "com.android.server.telecom";
    private static final String TET_BOARD_NAME = "TET";
    private static final String TICKING_VIBRATOR = "standard_vibrator_ticking";
    private static final long TIME_NOT_BIND_LIMIT = 300000;
    private static final int TRUST_SPACE_BADGE_STATUS_ENABLE = 1;
    private static final String TV_TAG = "tv";
    private static final String TXL_BOARD_NAME = "TXL";
    private static final String TYPE_COUNSELING_MESSAGE = "102";
    private static final String TYPE_IGNORE = "0";
    private static final String TYPE_INFORMATION = "3";
    private static final int TYPE_ISCHINA = 1;
    private static final String TYPE_MUSIC = "103";
    private static final int TYPE_NOTCHINA = 0;
    private static final String TYPE_PROMOTION = "2";
    private static final String TYPE_TOOLS = "107";
    private static final int VIBRATE_ENABLE = 1;
    private static final String VIBRATOR_TYPE_HAPTIC_NOTICE = "haptic.notice.";
    private static final String WECHAT_HONGBAO = "[微信红包]";
    final Uri URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
    private BatteryManagerInternal mBatteryManagerInternal;
    private Runnable mBindRunable;
    private List<String> mBtwList = new ArrayList();
    DBContentObserver mCfgDBObserver = null;
    Map<String, PreferencesHelper.NotificationSysMgrCfg> mCfgMap = new HashMap();
    private ContentObserver mContentObserver;
    Context mContext;
    private HwCustZenModeHelper mCust;
    private BroadcastReceiver mHangButtonReceiver;
    private HwGameObserver mHwGameObserver;
    Handler mHwHandler = null;
    private final HwStatusBarManagerService.HwNotificationDelegate mHwNotificationDelegate;
    private HwRecSysAidlInterface mHwRecSysAidlInterface;
    private boolean mIsTv;
    private long mLastBindTime;
    private final Object mLock;
    private HwNotificationWhiteListTvObserver mNotificationWhiteListTvObserver;
    private final ArrayList<NotificationContentViewRecord> mOriginContentViews;
    private PowerSaverObserver mPowerSaverObserver;
    private Handler mRecHandler;
    private HandlerThread mRecHandlerThread;
    private final ArrayMap<String, String> mRecognizeMap;
    private ServiceConnection mServiceConnection;
    private final ArrayMap<Integer, Boolean> mSmartNtfSwitchMap;
    private final ArrayMap<String, NotificationRecord> mUpdateEnqueuedNotifications;
    private boolean mVibrateWhenRinging;
    private ContentObserver mVibrateWhenRingingObserver;
    private String mVibratorType;
    private ContentObserver mVibratorTypeObserver;
    private final BroadcastReceiver powerReceiver;
    private HashSet<String> power_save_whiteSet;

    static {
        EXPANDEDNTF_PKGS.add("com.android.incallui");
        EXPANDEDNTF_PKGS.add("com.huawei.ohos.call");
        EXPANDEDNTF_PKGS.add("com.android.deskclock");
        EXPANDEDNTF_PKGS.add("com.huawei.meetime");
        EXPANDEDNTF_PKGS.add("com.huawei.deskclock");
    }

    private StatusBarManagerService getStatusBarManagerService() {
        return ServiceManager.getService("statusbar");
    }

    public void onStart() {
        HwNotificationManagerService.super.onStart();
        this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
        getContext().registerReceiver(this.powerReceiver, powerFilter, PERMISSION, null);
    }

    public HwNotificationManagerService(Context context, StatusBarManagerService statusBar, LightsService lights) {
        super(context);
        this.mBtwList.add("2");
        this.mBtwList.add("3");
        this.mBtwList.add("102");
        this.mBtwList.add(TYPE_TOOLS);
        this.mHwGameObserver = null;
        this.mLock = new Object();
        this.mLastBindTime = 0;
        this.mRecognizeMap = new ArrayMap<>();
        this.mSmartNtfSwitchMap = new ArrayMap<>();
        this.power_save_whiteSet = new HashSet<>();
        this.mIsTv = false;
        this.mUpdateEnqueuedNotifications = new ArrayMap<>();
        this.mCust = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        this.mHangButtonReceiver = new BroadcastReceiver() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        String pkg = intent.getStringExtra("pkgName");
                        int uid = intent.getIntExtra("uid", -1);
                        if (pkg != null && uid != -1) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE.equals(action)) {
                        String pkg2 = intent.getStringExtra("pkgName");
                        int uid2 = intent.getIntExtra("uid", -1);
                        if (pkg2 != null && uid2 != -1) {
                            synchronized (HwNotificationManagerService.this.mOriginContentViews) {
                                Iterator<NotificationContentViewRecord> itor = HwNotificationManagerService.this.mOriginContentViews.iterator();
                                while (itor.hasNext()) {
                                    NotificationContentViewRecord cvr = itor.next();
                                    if (cvr.pkg.equals(pkg2) && cvr.uid == uid2) {
                                        itor.remove();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.w(HwNotificationManagerService.TAG, "HangButtom receiver: never be here!");
                    }
                }
            }
        };
        this.mOriginContentViews = new ArrayList<>();
        this.mBindRunable = new Runnable() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwNotificationManagerService.this.bind();
            }
        };
        this.mServiceConnection = new ServiceConnection() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass4 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwNotificationManagerService.TAG, "onServiceConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                Log.i(HwNotificationManagerService.TAG, "onServiceDisConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = null;
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }
        };
        this.mContentObserver = new ContentObserver(null) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                super.onChange(isSelfChange);
                if (HwNotificationManagerService.this.mRecHandler != null) {
                    HwNotificationManagerService.this.mRecHandler.postAtFrontOfQueue(new Runnable() {
                        /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass5.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            int userId = ActivityManager.getCurrentUser();
                            boolean isEnable = Settings.System.getIntForUser(HwNotificationManagerService.this.mContext.getContentResolver(), HwNotificationManagerService.KEY_SMART_NOTIFICATION_SWITCH, HwNotificationManagerService.IS_CHINA_REGION ? 1 : 0, userId) == 1;
                            boolean isAllUserClose = true;
                            synchronized (HwNotificationManagerService.this.mSmartNtfSwitchMap) {
                                HwNotificationManagerService.this.mSmartNtfSwitchMap.put(Integer.valueOf(userId), Boolean.valueOf(isEnable));
                                if (HwNotificationManagerService.this.mSmartNtfSwitchMap.containsValue(true)) {
                                    isAllUserClose = false;
                                }
                            }
                            if (isAllUserClose) {
                                HwNotificationManagerService.this.unBind();
                            } else {
                                HwNotificationManagerService.this.bindRecSys();
                            }
                            Log.i(HwNotificationManagerService.TAG, "switch change to: " + isEnable + ",userId: " + userId + ",isAllUserClose :" + isAllUserClose);
                        }
                    });
                }
            }
        };
        this.powerReceiver = new BroadcastReceiver() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                        if (intent.getIntExtra(HwNotificationManagerService.POWER_MODE, 0) == 3) {
                            if (HwNotificationManagerService.this.mPowerSaverObserver == null) {
                                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                                hwNotificationManagerService.mPowerSaverObserver = new PowerSaverObserver(hwNotificationManagerService.mHwHandler);
                            }
                            HwNotificationManagerService.this.mPowerSaverObserver.observe();
                            Log.i(HwNotificationManagerService.TAG, "super power save 2.0 recevier brodcast register sqlite listener");
                        }
                    } else if (HwNotificationManagerService.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(HwNotificationManagerService.SHUTDOWN_LIMIT_POWERMODE, 0) == 0 && HwNotificationManagerService.this.mPowerSaverObserver != null) {
                        HwNotificationManagerService.this.mPowerSaverObserver.unObserve();
                        HwNotificationManagerService.this.mPowerSaverObserver = null;
                        Log.i(HwNotificationManagerService.TAG, "super power save 2.0 recevier brodcast unregister sqlite listener");
                    }
                }
            }
        };
        this.mHwNotificationDelegate = new HwStatusBarManagerService.HwNotificationDelegate() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass7 */

            @Override // com.android.server.statusbar.HwStatusBarManagerService.HwNotificationDelegate
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, 0, true, userId, 101, null);
            }
        };
        this.mVibrateWhenRingingObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass8 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                boolean z = true;
                if (Settings.Secure.getIntForUser(hwNotificationManagerService.mContext.getContentResolver(), HwNotificationManagerService.KEY_VIBRATE_WHEN_RINGING, 1, ActivityManager.getCurrentUser()) != 1) {
                    z = false;
                }
                hwNotificationManagerService.mVibrateWhenRinging = z;
            }
        };
        this.mVibratorTypeObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass9 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                hwNotificationManagerService.mVibratorType = Settings.Secure.getStringForUser(hwNotificationManagerService.mContext.getContentResolver(), HwNotificationManagerService.KEY_NOTIFICATION_VIBRATOR_SETTING, ActivityManager.getCurrentUser());
                if (HwNotificationManagerService.this.mVibratorType != null && !TextUtils.isEmpty(HwNotificationManagerService.this.mVibratorType)) {
                    return;
                }
                if (HwNotificationManagerService.IS_VIBRATE_RESERVE || !HwVibrator.isSupportHwVibrator("haptic.audio_vibrate.sync") || HwNotificationManagerService.this.isTETProduct()) {
                    HwNotificationManagerService.this.mVibratorType = HwNotificationManagerService.TICKING_VIBRATOR;
                } else {
                    HwNotificationManagerService.this.mVibratorType = HwNotificationManagerService.FOLLOW_SYSTEM_RINGTONE;
                }
            }
        };
        this.mContext = context;
    }

    public HwNotificationManagerService(Context context) {
        super(context);
        this.mBtwList.add("2");
        this.mBtwList.add("3");
        this.mBtwList.add("102");
        this.mBtwList.add(TYPE_TOOLS);
        this.mHwGameObserver = null;
        this.mLock = new Object();
        this.mLastBindTime = 0;
        this.mRecognizeMap = new ArrayMap<>();
        this.mSmartNtfSwitchMap = new ArrayMap<>();
        this.power_save_whiteSet = new HashSet<>();
        this.mIsTv = false;
        this.mUpdateEnqueuedNotifications = new ArrayMap<>();
        this.mCust = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        this.mHangButtonReceiver = new BroadcastReceiver() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        String pkg = intent.getStringExtra("pkgName");
                        int uid = intent.getIntExtra("uid", -1);
                        if (pkg != null && uid != -1) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE.equals(action)) {
                        String pkg2 = intent.getStringExtra("pkgName");
                        int uid2 = intent.getIntExtra("uid", -1);
                        if (pkg2 != null && uid2 != -1) {
                            synchronized (HwNotificationManagerService.this.mOriginContentViews) {
                                Iterator<NotificationContentViewRecord> itor = HwNotificationManagerService.this.mOriginContentViews.iterator();
                                while (itor.hasNext()) {
                                    NotificationContentViewRecord cvr = itor.next();
                                    if (cvr.pkg.equals(pkg2) && cvr.uid == uid2) {
                                        itor.remove();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.w(HwNotificationManagerService.TAG, "HangButtom receiver: never be here!");
                    }
                }
            }
        };
        this.mOriginContentViews = new ArrayList<>();
        this.mBindRunable = new Runnable() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwNotificationManagerService.this.bind();
            }
        };
        this.mServiceConnection = new ServiceConnection() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass4 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwNotificationManagerService.TAG, "onServiceConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                Log.i(HwNotificationManagerService.TAG, "onServiceDisConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = null;
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }
        };
        this.mContentObserver = new ContentObserver(null) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                super.onChange(isSelfChange);
                if (HwNotificationManagerService.this.mRecHandler != null) {
                    HwNotificationManagerService.this.mRecHandler.postAtFrontOfQueue(new Runnable() {
                        /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass5.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            int userId = ActivityManager.getCurrentUser();
                            boolean isEnable = Settings.System.getIntForUser(HwNotificationManagerService.this.mContext.getContentResolver(), HwNotificationManagerService.KEY_SMART_NOTIFICATION_SWITCH, HwNotificationManagerService.IS_CHINA_REGION ? 1 : 0, userId) == 1;
                            boolean isAllUserClose = true;
                            synchronized (HwNotificationManagerService.this.mSmartNtfSwitchMap) {
                                HwNotificationManagerService.this.mSmartNtfSwitchMap.put(Integer.valueOf(userId), Boolean.valueOf(isEnable));
                                if (HwNotificationManagerService.this.mSmartNtfSwitchMap.containsValue(true)) {
                                    isAllUserClose = false;
                                }
                            }
                            if (isAllUserClose) {
                                HwNotificationManagerService.this.unBind();
                            } else {
                                HwNotificationManagerService.this.bindRecSys();
                            }
                            Log.i(HwNotificationManagerService.TAG, "switch change to: " + isEnable + ",userId: " + userId + ",isAllUserClose :" + isAllUserClose);
                        }
                    });
                }
            }
        };
        this.powerReceiver = new BroadcastReceiver() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                        if (intent.getIntExtra(HwNotificationManagerService.POWER_MODE, 0) == 3) {
                            if (HwNotificationManagerService.this.mPowerSaverObserver == null) {
                                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                                hwNotificationManagerService.mPowerSaverObserver = new PowerSaverObserver(hwNotificationManagerService.mHwHandler);
                            }
                            HwNotificationManagerService.this.mPowerSaverObserver.observe();
                            Log.i(HwNotificationManagerService.TAG, "super power save 2.0 recevier brodcast register sqlite listener");
                        }
                    } else if (HwNotificationManagerService.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(HwNotificationManagerService.SHUTDOWN_LIMIT_POWERMODE, 0) == 0 && HwNotificationManagerService.this.mPowerSaverObserver != null) {
                        HwNotificationManagerService.this.mPowerSaverObserver.unObserve();
                        HwNotificationManagerService.this.mPowerSaverObserver = null;
                        Log.i(HwNotificationManagerService.TAG, "super power save 2.0 recevier brodcast unregister sqlite listener");
                    }
                }
            }
        };
        this.mHwNotificationDelegate = new HwStatusBarManagerService.HwNotificationDelegate() {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass7 */

            @Override // com.android.server.statusbar.HwStatusBarManagerService.HwNotificationDelegate
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, 0, true, userId, 101, null);
            }
        };
        this.mVibrateWhenRingingObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass8 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                boolean z = true;
                if (Settings.Secure.getIntForUser(hwNotificationManagerService.mContext.getContentResolver(), HwNotificationManagerService.KEY_VIBRATE_WHEN_RINGING, 1, ActivityManager.getCurrentUser()) != 1) {
                    z = false;
                }
                hwNotificationManagerService.mVibrateWhenRinging = z;
            }
        };
        this.mVibratorTypeObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.notification.HwNotificationManagerService.AnonymousClass9 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwNotificationManagerService hwNotificationManagerService = HwNotificationManagerService.this;
                hwNotificationManagerService.mVibratorType = Settings.Secure.getStringForUser(hwNotificationManagerService.mContext.getContentResolver(), HwNotificationManagerService.KEY_NOTIFICATION_VIBRATOR_SETTING, ActivityManager.getCurrentUser());
                if (HwNotificationManagerService.this.mVibratorType != null && !TextUtils.isEmpty(HwNotificationManagerService.this.mVibratorType)) {
                    return;
                }
                if (HwNotificationManagerService.IS_VIBRATE_RESERVE || !HwVibrator.isSupportHwVibrator("haptic.audio_vibrate.sync") || HwNotificationManagerService.this.isTETProduct()) {
                    HwNotificationManagerService.this.mVibratorType = HwNotificationManagerService.TICKING_VIBRATOR;
                } else {
                    HwNotificationManagerService.this.mVibratorType = HwNotificationManagerService.FOLLOW_SYSTEM_RINGTONE;
                }
            }
        };
        this.mContext = context;
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.notification.$$Lambda$HwNotificationManagerService$ZCrKpb82sziMOc51dwZrcRLjWI */

            @Override // java.lang.Runnable
            public final void run() {
                HwNotificationManagerService.this.lambda$new$0$HwNotificationManagerService();
            }
        }, "HwNotificationManagerService init");
    }

    /* access modifiers changed from: private */
    /* renamed from: init */
    public void lambda$new$0$HwNotificationManagerService() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_ACTION_ALLOW);
        filter.addAction(NOTIFICATION_ACTION_REFUSE);
        this.mContext.registerReceiverAsUser(this.mHangButtonReceiver, UserHandle.ALL, filter, "com.android.permission.system_manager_interface", null);
        StatusBarManagerService sb = getStatusBarManagerService();
        if (sb instanceof HwStatusBarManagerService) {
            ((HwStatusBarManagerService) sb).setHwNotificationDelegate(this.mHwNotificationDelegate);
        }
        this.mHwHandler = new Handler(Looper.getMainLooper());
        this.mHwHandler.postDelayed(new HwCfgLoadingRunnable(), BIND_TIMEOUT);
        this.mCfgDBObserver = new DBContentObserver();
        this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_NOTIFICATION_VIBRATOR_SETTING), false, this.mVibratorTypeObserver, ActivityManager.getCurrentUser());
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_VIBRATE_WHEN_RINGING), false, this.mVibrateWhenRingingObserver, ActivityManager.getCurrentUser());
        this.mVibratorTypeObserver.onChange(true);
        this.mVibrateWhenRingingObserver.onChange(true);
        if (!SystemProperties.getBoolean("ro.config.hwsmartnotification.disable", false)) {
            this.mRecHandlerThread = new HandlerThread("notification manager");
            this.mRecHandlerThread.start();
            this.mRecHandler = new Handler(this.mRecHandlerThread.getLooper());
        }
        try {
            ContentResolver cr = this.mContext.getContentResolver();
            if (cr != null) {
                cr.registerContentObserver(Settings.System.getUriFor(KEY_SMART_NOTIFICATION_SWITCH), false, this.mContentObserver, -1);
            }
            this.mContentObserver.onChange(true);
        } catch (Exception e) {
            Log.w(TAG, "init failed", e);
        }
        registerHwGameObserver();
        this.mIsTv = TV_TAG.equals(SystemProperties.get(PROPERTY_TV));
        if (this.mIsTv) {
            this.mNotificationWhiteListTvObserver = new HwNotificationWhiteListTvObserver(this.mContext, this.mHwHandler);
            this.mNotificationWhiteListTvObserver.observe();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTETProduct() {
        String board = SystemPropertiesEx.get("ro.product.board", "");
        if (TextUtils.isEmpty(board)) {
            return false;
        }
        String board2 = board.toUpperCase(Locale.US);
        if (board2.contains(TET_BOARD_NAME) || board2.contains(TXL_BOARD_NAME)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class HwCfgLoadingRunnable implements Runnable {
        private HwCfgLoadingRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            new Thread("HwCfgLoading") {
                /* class com.android.server.notification.HwNotificationManagerService.HwCfgLoadingRunnable.AnonymousClass1 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    HwCfgLoadingRunnable.this.load();
                }
            }.start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x01dd, code lost:
            if (r2 == null) goto L_0x01e2;
         */
        /* JADX WARNING: Removed duplicated region for block: B:69:0x01b5 A[Catch:{ SQLiteException -> 0x01d2, SecurityException -> 0x01c4, RuntimeException -> 0x01b6, Exception -> 0x01a8, all -> 0x01a4, all -> 0x01eb }] */
        /* JADX WARNING: Removed duplicated region for block: B:74:0x01c3 A[Catch:{ SQLiteException -> 0x01d2, SecurityException -> 0x01c4, RuntimeException -> 0x01b6, Exception -> 0x01a8, all -> 0x01a4, all -> 0x01eb }] */
        /* JADX WARNING: Removed duplicated region for block: B:79:0x01d1 A[Catch:{ SQLiteException -> 0x01d2, SecurityException -> 0x01c4, RuntimeException -> 0x01b6, Exception -> 0x01a8, all -> 0x01a4, all -> 0x01eb }] */
        /* JADX WARNING: Removed duplicated region for block: B:89:0x01ee  */
        private void load() {
            Context context;
            Throwable th;
            SecurityException e;
            int nCidColIndex;
            int nTypeColIndex;
            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Starts ");
            Cursor cursor = null;
            try {
                context = HwNotificationManagerService.this.mContext.createPackageContextAsUser("com.huawei.systemmanager", 0, new UserHandle(ActivityManager.getCurrentUser()));
            } catch (Exception e2) {
                Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to convert context");
                context = null;
            }
            if (context != null) {
                try {
                    cursor = context.getContentResolver().query(HwNotificationManagerService.this.URI_NOTIFICATION_CFG, null, null, null, null);
                    if (cursor == null) {
                        try {
                            Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to get cfg from DB");
                            if (cursor != null) {
                                cursor.close();
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (SQLiteException e3) {
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SQLiteException.");
                        } catch (SecurityException e4) {
                            e = e4;
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SecurityException.", e);
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (RuntimeException e5) {
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException.");
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (Exception e6) {
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception.");
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                            throw th;
                        }
                    } else {
                        HashMap<String, PreferencesHelper.NotificationSysMgrCfg> tempMap = new HashMap<>();
                        ArrayList<PreferencesHelper.NotificationSysMgrCfg> tempSysMgrCfgList = new ArrayList<>();
                        if (cursor.getCount() > 0) {
                            int nPkgColIndex = cursor.getColumnIndex(AppActConstant.ATTR_PACKAGE_NAME);
                            int nCfgColIndex = cursor.getColumnIndex("sound_vibrate");
                            int nTypeColIndex2 = cursor.getColumnIndex(HwNotificationManagerService.TABLE_COLUM_NOTIFICATION_VIBRATOR_TYPE);
                            int nCidColIndex2 = cursor.getColumnIndex("channelid");
                            int nLockscreenColIndex = cursor.getColumnIndex("lockscreencfg");
                            int nImportaceColIndex = cursor.getColumnIndex("channelimportance");
                            int nBypassdndColIndex = cursor.getColumnIndex("channelbypassdnd");
                            int nIconDadgeColIndex = cursor.getColumnIndex("channeliconbadge");
                            while (cursor.moveToNext()) {
                                String pkgName = cursor.getString(nPkgColIndex);
                                if (!TextUtils.isEmpty(pkgName)) {
                                    int soundVibrate = cursor.getInt(nCfgColIndex);
                                    String vibratorType = cursor.getString(nTypeColIndex2);
                                    String channelId = cursor.getString(nCidColIndex2);
                                    try {
                                        String key = HwNotificationManagerService.this.pkgAndCidKey(pkgName, channelId);
                                        if ("miscellaneous".equals(channelId)) {
                                            PreferencesHelper.NotificationSysMgrCfg mgrCfg = new PreferencesHelper.NotificationSysMgrCfg();
                                            mgrCfg.smc_userId = ActivityManager.getCurrentUser();
                                            mgrCfg.smc_packageName = pkgName;
                                            mgrCfg.smc_visilibity = cursor.getInt(nLockscreenColIndex);
                                            mgrCfg.smc_importance = cursor.getInt(nImportaceColIndex);
                                            mgrCfg.smc_bypassDND = cursor.getInt(nBypassdndColIndex);
                                            mgrCfg.smc_iconBadge = cursor.getInt(nIconDadgeColIndex);
                                            tempSysMgrCfgList.add(mgrCfg);
                                            if (HwNotificationManagerService.MMS_PKGNAME.equals(pkgName)) {
                                                nTypeColIndex = nTypeColIndex2;
                                                StringBuilder sb = new StringBuilder();
                                                nCidColIndex = nCidColIndex2;
                                                sb.append("mgrCfg.importance : ");
                                                sb.append(mgrCfg.smc_importance);
                                                Slog.i(HwNotificationManagerService.TAG, sb.toString());
                                            } else {
                                                nTypeColIndex = nTypeColIndex2;
                                                nCidColIndex = nCidColIndex2;
                                            }
                                        } else {
                                            nTypeColIndex = nTypeColIndex2;
                                            nCidColIndex = nCidColIndex2;
                                        }
                                        tempMap.put(key, new PreferencesHelper.NotificationSysMgrCfg(soundVibrate, vibratorType));
                                        nPkgColIndex = nPkgColIndex;
                                        nCfgColIndex = nCfgColIndex;
                                        context = context;
                                        nTypeColIndex2 = nTypeColIndex;
                                        nCidColIndex2 = nCidColIndex;
                                    } catch (SQLiteException e7) {
                                        Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SQLiteException.");
                                    } catch (SecurityException e8) {
                                        e = e8;
                                        Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SecurityException.", e);
                                        if (cursor != null) {
                                        }
                                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                                    } catch (RuntimeException e9) {
                                        Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException.");
                                        if (cursor != null) {
                                        }
                                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                                    } catch (Exception e10) {
                                        Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception.");
                                        if (cursor != null) {
                                        }
                                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                                    }
                                }
                            }
                        }
                        HwNotificationManagerService.this.setSysMgrCfgMap(tempSysMgrCfgList);
                        synchronized (HwNotificationManagerService.this.mCfgMap) {
                            HwNotificationManagerService.this.mCfgMap.clear();
                            HwNotificationManagerService.this.mCfgMap.putAll(tempMap);
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: get cfg size:" + HwNotificationManagerService.this.mCfgMap.size());
                        }
                        cursor.close();
                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                    }
                } catch (SQLiteException e11) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SQLiteException.");
                } catch (SecurityException e12) {
                    e = e12;
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : SecurityException.", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (RuntimeException e13) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (Exception e14) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception.");
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class DBContentObserver extends ContentObserver {
        public DBContentObserver() {
            super(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Slog.v(HwNotificationManagerService.TAG, "Notification db cfg changed");
            HwNotificationManagerService.this.mHwHandler.post(new HwCfgLoadingRunnable());
        }
    }

    /* access modifiers changed from: protected */
    public void handleGetNotifications(Parcel data, Parcel reply) {
        if (this.mContext.checkCallingPermission("huawei.permission.IBINDER_NOTIFICATION_SERVICE") != 0) {
            Slog.e(TAG, "NotificationManagerService.handleGetNotifications: permissin deny");
            return;
        }
        HashSet<String> notificationPkgs = new HashSet<>();
        getNotificationPkgsHsm(notificationPkgs);
        Slog.v(TAG, "NotificationManagerService.handleGetNotifications: got " + notificationPkgs.size() + " pkgs");
        reply.writeInt(notificationPkgs.size());
        Iterator<String> it = notificationPkgs.iterator();
        while (it.hasNext()) {
            String pkg = it.next();
            Slog.v(TAG, "NotificationManagerService.handleGetNotifications: reply " + pkg);
            reply.writeString(pkg);
        }
    }

    /* access modifiers changed from: package-private */
    public void getNotificationPkgsHsm(HashSet<String> notificationPkgs) {
        if (notificationPkgs != null) {
            if (notificationPkgs.size() > 0) {
                notificationPkgs.clear();
            }
            synchronized (this.mNotificationList) {
                int notificationListSize = this.mNotificationList.size();
                if (notificationListSize != 0) {
                    for (int index = 0; index < notificationListSize; index++) {
                        NotificationRecord notificationRecord = (NotificationRecord) this.mNotificationList.get(index);
                        if (notificationRecord != null) {
                            String packageName = notificationRecord.sbn.getPackageName();
                            if (packageName != null && packageName.length() > 0) {
                                notificationPkgs.add(packageName);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class NotificationContentViewRecord {
        final int id;
        final String pkg;
        RemoteViews rOldBigContentView;
        final String tag;
        final int uid;
        final int userId;

        NotificationContentViewRecord(String pkg2, int uid2, String tag2, int id2, int userId2, RemoteViews rOldBigContentView2) {
            this.pkg = pkg2;
            this.tag = tag2;
            this.uid = uid2;
            this.id = id2;
            this.userId = userId2;
            this.rOldBigContentView = rOldBigContentView2;
        }

        public String toString() {
            return "NotificationContentViewRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " uid=" + this.uid + " id=" + Integer.toHexString(this.id) + " tag=" + this.tag + " userId=" + Integer.toHexString(this.userId) + "}";
        }
    }

    private int indexOfContentViewLocked(String pkg, String tag, int id, int userId) {
        synchronized (this.mOriginContentViews) {
            ArrayList<NotificationContentViewRecord> list = this.mOriginContentViews;
            int len = list.size();
            for (int i = 0; i < len; i++) {
                NotificationContentViewRecord record = list.get(i);
                if (userId == -1 || record.userId == -1 || record.userId == userId) {
                    if (record.id == id) {
                        if (tag == null) {
                            if (record.tag != null) {
                            }
                        } else if (!tag.equals(record.tag)) {
                        }
                        if (record.pkg.equals(pkg)) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recoverNotificationContentView(String pkg, int uid) {
        synchronized (this.mOriginContentViews) {
            Iterator<NotificationContentViewRecord> itor = this.mOriginContentViews.iterator();
            while (itor.hasNext()) {
                NotificationContentViewRecord cvr = itor.next();
                if (cvr.pkg.equals(pkg)) {
                    NotificationRecord record = findNotificationByListLocked(this.mNotificationList, cvr.pkg, cvr.tag, cvr.id, cvr.userId);
                    if (record != null) {
                        record.sbn.getNotification().bigContentView = cvr.rOldBigContentView;
                        Slog.d(TAG, "revertNotificationView enqueueNotificationInternal pkg=" + pkg + " id=" + record.sbn.getId() + " userId=" + record.sbn.getUserId());
                        record.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, true);
                        enqueueNotificationInternal(pkg, record.sbn.getOpPkg(), record.sbn.getUid(), record.sbn.getInitialPid(), record.sbn.getTag(), record.sbn.getId(), record.sbn.getNotification(), record.sbn.getUserId());
                        itor.remove();
                    } else {
                        Slog.w(TAG, "Notification can't find in NotificationRecords");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isHwSoundAllow(String pkg, String channelId, int userId) {
        String key = pkgAndCidKey(pkg, channelId);
        Integer soundVibrate = null;
        synchronized (this.mCfgMap) {
            PreferencesHelper.NotificationSysMgrCfg mgrCfg = this.mCfgMap.get(key);
            if (mgrCfg != null) {
                soundVibrate = Integer.valueOf(mgrCfg.soundVibrate);
            }
        }
        Slog.i(TAG, "isHwSoundAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", soundVibrate=" + soundVibrate);
        return soundVibrate == null || (soundVibrate.intValue() & 1) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean isHwVibrateAllow(String pkg, String channelId, int userId) {
        String key = pkgAndCidKey(pkg, channelId);
        Integer soundVibrate = null;
        synchronized (this.mCfgMap) {
            PreferencesHelper.NotificationSysMgrCfg mgrCfg = this.mCfgMap.get(key);
            if (mgrCfg != null) {
                soundVibrate = Integer.valueOf(mgrCfg.soundVibrate);
            }
        }
        boolean isVibrateEnable = false;
        boolean isGlobalVibrateEnable = !(this.mAudioManager.getRingerModeInternal() == 0) && ((this.mAudioManager.getRingerModeInternal() == 1) || this.mVibrateWhenRinging);
        if (isCloneProfile(userId)) {
            Slog.v(TAG, "clone app follow channel vibrateallow");
            return isGlobalVibrateEnable;
        }
        if (isGlobalVibrateEnable && (soundVibrate == null || (soundVibrate.intValue() & 2) != 0)) {
            isVibrateEnable = true;
        }
        Slog.v(TAG, "isHwVibrateAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", isVibrateEnable=" + isVibrateEnable);
        return isVibrateEnable;
    }

    /* access modifiers changed from: protected */
    public String getHwVibratorType(Ringtone ringtone, String pkg, String channelId, int userId) {
        String vibratorType;
        String vibratorType2 = null;
        if (!isCloneProfile(userId)) {
            String key = pkgAndCidKey(pkg, channelId);
            synchronized (this.mCfgMap) {
                PreferencesHelper.NotificationSysMgrCfg mgrCfg = this.mCfgMap.get(key);
                if (mgrCfg != null) {
                    vibratorType2 = mgrCfg.vibratorType;
                }
            }
        } else {
            Slog.v(TAG, "cloneapp follow system vibratorType");
        }
        if (vibratorType2 == null || "default".contains(vibratorType2)) {
            Slog.i(TAG_VIBRATE, "Channel vibrate is null, get global vibrator : " + this.mVibratorType);
            vibratorType2 = this.mVibratorType;
        }
        if (NULL_VIBRATOR.equals(vibratorType2)) {
            Slog.i(TAG_VIBRATE, "getHwVibratorType pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", vibratorType=" + vibratorType2);
            return vibratorType2;
        }
        if (FOLLOW_SYSTEM_RINGTONE.equals(vibratorType2)) {
            vibratorType = getSystemVibratorType(ringtone, vibratorType2);
        } else {
            vibratorType = PATTERN_TYPE.get(vibratorType2);
        }
        if (!HwVibrator.isSupportHwVibrator(vibratorType)) {
            Slog.i(TAG_VIBRATE, "Don't support this vibrate type:" + vibratorType + ", change to ticking");
            vibratorType = PATTERN_TYPE.get(TICKING_VIBRATOR);
        }
        Slog.i(TAG_VIBRATE, "getHwVibratorType pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", vibratorType=" + vibratorType);
        return vibratorType;
    }

    private boolean isCloneProfile(int userId) {
        long ident = Binder.clearCallingIdentity();
        boolean isTemp = false;
        try {
            if (!(this.mUm == null || this.mUm.getUserInfo(userId) == null)) {
                isTemp = this.mUm.getUserInfo(userId).isClonedProfile();
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "judge cloneProfile SecurityException");
        } catch (Exception e2) {
            Slog.e(TAG, "judge cloneProfile Exception");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        return isTemp;
    }

    private String getSystemVibratorType(Ringtone ringtone, String vibratorType) {
        if (ringtone == null || ringtone.getUri() == null) {
            return vibratorType;
        }
        if (this.mAudioManager.getRingerModeInternal() == 1) {
            Slog.i(TAG_VIBRATE, "RingerMode is vibrate, change to ticking");
            return PATTERN_TYPE.get(TICKING_VIBRATOR);
        }
        Uri uri = ringtone.getUri();
        if ("settings".equals(ContentProvider.getAuthorityWithoutUserId(uri.getAuthority()))) {
            uri = RingtoneManager.getActualDefaultRingtoneUri(this.mContext, RingtoneManager.getDefaultType(uri));
        }
        return VIBRATOR_TYPE_HAPTIC_NOTICE + Ringtone.getTitle(this.mContext, uri, false, true).replace(" ", "_");
    }

    /* access modifiers changed from: protected */
    public void playHwVibrate(NotificationRecord record, String type, boolean isDelayVibForSound) {
        String hwOpPkg;
        long ident = Binder.clearCallingIdentity();
        try {
            int uid = record.sbn.getUid();
            if (this.iHwNotificationManagerServiceEx != null) {
                hwOpPkg = this.iHwNotificationManagerServiceEx.getHwOpPkg(record.sbn);
            } else {
                hwOpPkg = record.sbn.getOpPkg();
            }
            if ((record.getNotification().flags & 4) != 0) {
                HwVibrator.setHwVibratorRepeat(uid, hwOpPkg, this.mToken, type, 0);
            } else if (isDelayVibForSound) {
                HwVibrator.setHwVibrator(uid, hwOpPkg, this.mToken, type, this.mAudioManager.getFocusRampTimeMs(3, record.getAudioAttributes()));
            } else {
                HwVibrator.setHwVibrator(uid, hwOpPkg, this.mToken, type, 0);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: protected */
    public NotificationChannel getHwNotificationChannel(NotificationChannel channel, String pkg, int uid) {
        if (channel == null || !channel.shouldVibrate()) {
            return channel;
        }
        return copyNotificationChannel(channel, pkg, uid);
    }

    private NotificationChannel copyNotificationChannel(NotificationChannel channel, String pkg, int uid) {
        NotificationChannel other = new NotificationChannel(channel.getId(), channel.getName(), channel.getImportance());
        other.setBypassDnd(channel.canBypassDnd());
        other.setLockscreenVisibility(channel.getLockscreenVisibility());
        other.enableLights(channel.shouldShowLights());
        other.setLightColor(channel.getLightColor());
        other.lockFields(channel.getUserLockedFields());
        other.setFgServiceShown(channel.isFgServiceShown());
        other.setShowBadge(channel.canShowBadge());
        other.setDeleted(channel.isDeleted());
        other.setBlockableSystem(channel.isBlockableSystem());
        other.setAllowBubbles(channel.canBubble());
        other.setDescription(channel.getDescription());
        other.setSound(channel.getSound(), channel.getAudioAttributes());
        other.setGroup(channel.getGroup());
        other.setImportanceLockedByOEM(channel.isImportanceLockedByOEM());
        other.setImportanceLockedByCriticalDeviceFunction(channel.isImportanceLockedByCriticalDeviceFunction());
        other.setVibrationPattern(channel.getVibrationPattern());
        other.enableVibration(!NULL_VIBRATOR.equals(getHwVibratorType(null, pkg, channel.getId(), uid)));
        return other;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String pkgAndCidKey(String pkg, String channelId) {
        return pkg + "_" + channelId;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int modifyScoreBySM(String pkg, int callingUid, int origScore) {
        return origScore;
    }

    /* access modifiers changed from: protected */
    public void detectNotifyBySM(int callingUid, String pkg, Notification notification) {
        Intent intent = new Intent("com.huawei.notificationmanager.detectnotify");
        intent.putExtra("callerUid", callingUid);
        intent.putExtra(AppActConstant.ATTR_PACKAGE_NAME, pkg);
        Bundle bundle = new Bundle();
        bundle.putParcelable("sendNotify", notification);
        intent.putExtra("notifyBundle", bundle);
    }

    /* access modifiers changed from: protected */
    public void hwEnqueueNotificationWithTag(String pkg, int uid, NotificationRecord nr) {
        if (nr.sbn.getNotification().extras.getBoolean(CONTENTVIEW_REVERTING_FLAG, false)) {
            nr.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, false);
        }
    }

    /* access modifiers changed from: protected */
    public boolean inNonDisturbMode(String pkg) {
        if (pkg == null) {
            return false;
        }
        return isWhiteApp(pkg);
    }

    private boolean isWhiteApp(String pkg) {
        HwCustZenModeHelper hwCustZenModeHelper = this.mCust;
        if (!(hwCustZenModeHelper == null || hwCustZenModeHelper.getWhiteApps(this.mContext) == null)) {
            return Arrays.asList(this.mCust.getWhiteApps(this.mContext)).contains(pkg);
        }
        String[] defaultWhiteapps = {MMS_PKGNAME};
        if (this.mContext == null) {
            return false;
        }
        for (String pkgname : defaultWhiteapps) {
            if (pkgname.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isImportantNotification(String pkg, Notification notification) {
        if (notification == null || notification.priority < 3) {
            return false;
        }
        if ((pkg.equals(PHONE_PKGNAME) || pkg.equals(MMS_PKGNAME) || pkg.equals(CONTACTS_PKGNAME) || pkg.equals(CONTACTS_PKGNAME_HW) || pkg.equals(TELECOM_PKGNAME)) && notification.priority < 7) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsNotificationEnable(String pkg) {
        if (pkg == null) {
            return false;
        }
        if (pkg.equalsIgnoreCase(MMS_PKGNAME) || pkg.equalsIgnoreCase(CONTACTS_PKGNAME) || pkg.equals(CONTACTS_PKGNAME_HW)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void hwCancelNotification(String pkg, String tag, int id, int userId) {
        synchronized (this.mOriginContentViews) {
            int indexView = indexOfContentViewLocked(pkg, tag, id, userId);
            if (indexView >= 0) {
                this.mOriginContentViews.remove(indexView);
                Slog.d(TAG, "hwCancelNotification: pkg = " + pkg + ", id = " + id);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean isEnable, int ledOnMs, int ledOffMs) {
        this.mBatteryManagerInternal.updateBatteryLight(isEnable, ledOnMs, ledOffMs);
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitchEvents(int userId) {
        if (this.mCfgDBObserver != null) {
            this.mHwHandler.post(new HwCfgLoadingRunnable());
            this.mContext.getContentResolver().unregisterContentObserver(this.mCfgDBObserver);
            this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        }
        if (this.mVibratorTypeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mVibratorTypeObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_NOTIFICATION_VIBRATOR_SETTING), false, this.mVibratorTypeObserver, ActivityManager.getCurrentUser());
            this.mVibratorTypeObserver.onChange(true);
        }
        if (this.mVibrateWhenRingingObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mVibrateWhenRingingObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_VIBRATE_WHEN_RINGING), false, this.mVibrateWhenRingingObserver, ActivityManager.getCurrentUser());
            this.mVibrateWhenRingingObserver.onChange(true);
        }
    }

    /* access modifiers changed from: protected */
    public void stopPlaySound() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "stopPlaySound RemoteException !!!");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* access modifiers changed from: protected */
    public boolean isAFWUserId(int userId) {
        boolean isTemp = false;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManagerService.getInstance().getUserInfo(userId);
            if (userInfo != null) {
                isTemp = userInfo.isManagedProfile() || userInfo.isClonedProfile();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "isAFWUserId catch SecurityException !!!");
        } catch (Exception e2) {
            Log.e(TAG, "isAFWUserId catch Exception !!!");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
        return isTemp;
    }

    /* access modifiers changed from: protected */
    public void addHwExtraForNotification(Notification notification, String pkg, int pid) {
        ITrustSpaceController controller = HwServiceFactory.getTrustSpaceController();
        if (controller != null && controller.isIntentProtectedApp(pkg) && isTrustSpaceBadgeEnabled()) {
            notification.extras.putBoolean("com.huawei.isIntentProtectedApp", true);
        }
    }

    private boolean isTrustSpaceBadgeEnabled() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), KEY_TRUST_SPACE_BADGE_SWITCH, 1) == 1;
    }

    /* access modifiers changed from: protected */
    public String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
        Bundle bundle;
        String targetPkg;
        if (!NOTIFICATION_CENTER_PKG.equals(opPkg) || (bundle = notification.extras) == null || (targetPkg = bundle.getString(NOTIFICATION_CENTER_ORIGIN_PKG)) == null || !isVaildPkg(targetPkg)) {
            return defaultPkg;
        }
        Slog.v(TAG, "Notification Center targetPkg:" + targetPkg);
        return targetPkg;
    }

    private boolean isVaildPkg(String pkg) {
        try {
            if (AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getCallingUserId()) == null) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isBlockRideModeNotification(String pkg) {
        if (!HWRIDEMODE_FEATURE_SUPPORTED || !SystemProperties.getBoolean("sys.ride_mode", false) || RIDEMODE_NOTIFICATION_WHITE_LIST.contains(pkg)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isNotInTvWhiteListNotification(String pkg) {
        HwNotificationWhiteListTvObserver hwNotificationWhiteListTvObserver;
        return this.mIsTv && (hwNotificationWhiteListTvObserver = this.mNotificationWhiteListTvObserver) != null && !hwNotificationWhiteListTvObserver.isTvNoitficationWhiteApp(pkg);
    }

    public void reportToIAware(String pkg, int uid, int nid, boolean isAdded) {
        HwSysResManager resManager;
        if (pkg != null && !pkg.isEmpty() && (resManager = HwSysResManager.getInstance()) != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putString("pkgname", pkg);
            bundleArgs.putInt("tgtUid", uid);
            bundleArgs.putInt("notification_id", nid);
            bundleArgs.putInt("relationType", isAdded ? 20 : 21);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    /* access modifiers changed from: protected */
    public boolean doForUpdateNotification(String key, Handler handler) {
        NotificationRecord newNotification;
        if (!HwPartIawareUtil.isNotificatinSwitchEnable()) {
            Log.d(TAG, "doForUpdateNotification: Notificatin Switch is false!");
            return false;
        } else if (handler == null || key == null || !isUpdateNotificationNeedToMng(key) || (newNotification = findNotificationByListLocked(key)) == null) {
            return false;
        } else {
            if (!(((newNotification.getFlags() & 2) == 0 || (newNotification.getFlags() & 64) == 0) && isImUpdateNotification(newNotification))) {
                return false;
            }
            if (this.mUpdateEnqueuedNotifications.get(key) != null) {
                if (isWechatHongbao(newNotification)) {
                    handler.post(new NotificationManagerService.PostNotificationRunnable(this, key));
                } else {
                    this.mUpdateEnqueuedNotifications.put(key, newNotification);
                }
                int size = this.mEnqueuedNotifications.size();
                for (int i = 0; i < size; i++) {
                    if (Objects.equals(key, ((NotificationRecord) this.mEnqueuedNotifications.get(i)).getKey())) {
                        this.mEnqueuedNotifications.remove(i);
                        return true;
                    }
                }
                return true;
            } else if (isWechatHongbao(newNotification)) {
                return false;
            } else {
                this.mUpdateEnqueuedNotifications.put(key, newNotification);
                handler.postDelayed(new NotificationManagerService.PostNotificationRunnable(this, key), HwPartIawareUtil.getNotificationInterval());
                return true;
            }
        }
    }

    private boolean isImUpdateNotification(NotificationRecord record) {
        StatusBarNotification statusBarNotification = record.sbn;
        if (statusBarNotification == null) {
            return false;
        }
        int appType = HwPartIawareUtil.getAppMngSpecType(statusBarNotification.getPackageName());
        if (appType == 0 || appType == 6 || appType == 311 || appType == 318) {
            return true;
        }
        return false;
    }

    private boolean isWechatHongbao(NotificationRecord record) {
        Notification notification;
        CharSequence charSequence;
        StatusBarNotification statusBarNotification = record.sbn;
        String topImCn = HwPartIawareUtil.getActTopImcn();
        if (topImCn == null || !topImCn.equals(statusBarNotification.getPackageName()) || (notification = statusBarNotification.getNotification()) == null || notification.extras == null || (charSequence = notification.extras.getCharSequence("android.text")) == null || !charSequence.toString().contains(WECHAT_HONGBAO)) {
            return false;
        }
        return true;
    }

    private NotificationRecord findNotificationByListLocked(String key) {
        for (int index = this.mEnqueuedNotifications.size() - 1; index >= 0; index--) {
            if (key.equals(((NotificationRecord) this.mEnqueuedNotifications.get(index)).getKey())) {
                return (NotificationRecord) this.mEnqueuedNotifications.get(index);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void removeNotificationInUpdateQueue(String key) {
        if (key != null && this.mUpdateEnqueuedNotifications.containsKey(key)) {
            this.mUpdateEnqueuedNotifications.remove(key);
        }
    }

    private boolean isUpdateNotificationNeedToMng(String key) {
        return indexOfNotificationLocked(key) >= 0;
    }

    /* access modifiers changed from: protected */
    public boolean isFromPinNotification(Notification notification, String pkg) {
        return isPkgInWhiteApp(pkg) && notification.extras.getBoolean("pin_notification");
    }

    private boolean isPkgInWhiteApp(String packageName) {
        String[] strArr = NOTIFICATION_WHITE_APPS_PACKAGE;
        for (String pkg : strArr) {
            if (!TextUtils.isEmpty(pkg) && pkg.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isGameRunningForeground() {
        return HwActivityTaskManager.isGameDndOn();
    }

    /* access modifiers changed from: protected */
    public boolean isGameDndSwitchOn() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "game_dnd_mode", 2) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isPackageRequestNarrowNotification() {
        String topPkg = getTopPkgName();
        for (String pkg : NOTIFICATION_NARROW_DISPLAY_APPS) {
            if (pkg.equalsIgnoreCase(topPkg)) {
                return true;
            }
        }
        return false;
    }

    private String getTopPkgName() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> tasks = null;
        if (am != null) {
            tasks = am.getRunningTasks(1);
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = null;
        if (tasks != null && !tasks.isEmpty()) {
            runningTaskInfo = tasks.get(0);
        }
        if (runningTaskInfo != null) {
            return runningTaskInfo.topActivity.getPackageName();
        }
        return "";
    }

    private void registerHwGameObserver() {
        if (this.mHwGameObserver == null) {
            this.mHwGameObserver = new HwGameObserver();
        }
        ActivityManagerEx.registerGameObserver(this.mHwGameObserver);
    }

    /* access modifiers changed from: private */
    public class HwGameObserver extends IGameObserver.Stub {
        private HwGameObserver() {
        }

        public void onGameListChanged() {
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (event == 1 || event == 3 || event == 4) {
                HwNotificationManagerService.this.mGameDndStatus = true;
            } else if (event == 2) {
                HwNotificationManagerService.this.mGameDndStatus = false;
            } else {
                Log.w(HwNotificationManagerService.TAG, "onGameStatusChanged: other event!");
            }
            HwNotificationManagerService.this.updateNotificationInGameMode();
            Log.d(HwNotificationManagerService.TAG, "onGameStatusChanged event=" + event + ",mGameDndStatus=" + HwNotificationManagerService.this.mGameDndStatus);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNotificationInGameMode() {
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    public void bindRecSys() {
        Handler handler = this.mRecHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mBindRunable);
            this.mRecHandler.post(this.mBindRunable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bind() {
        if (this.mHwRecSysAidlInterface != null) {
            Log.d(TAG, "bind: already binded");
            return;
        }
        try {
            Log.i(TAG, "bind service: action=com.huawei.recsys.action.THIRD_REQUEST_ENGINE, pkg=com.huawei.recsys");
            Intent intent = new Intent();
            intent.setAction(BIND_ACTION);
            intent.setPackage(SERVER_PAKAGE_NAME);
            boolean isBindSuccess = this.mContext.bindService(intent, this.mServiceConnection, 1);
            if (isBindSuccess) {
                synchronized (this.mLock) {
                    this.mLock.wait(BIND_TIMEOUT);
                }
            }
            Log.i(TAG, "bind service finish, isBindSuccess = " + isBindSuccess);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "bind service failed! catch IllegalArgumentException.");
        } catch (Exception e2) {
            Log.e(TAG, "bind service failed! catch Exception.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unBind() {
        Handler handler = this.mRecHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mBindRunable);
        }
        if (this.mHwRecSysAidlInterface == null) {
            Log.d(TAG, "unbind: already unbinded");
            return;
        }
        try {
            Log.i(TAG, "unbind service");
            this.mContext.unbindService(this.mServiceConnection);
            Log.i(TAG, "unbind service finish");
        } catch (SecurityException e) {
            Log.e(TAG, "bind service catch SecurityException.", e);
        } catch (Exception e2) {
            Log.e(TAG, "bind service catch Exception.");
        }
        this.mHwRecSysAidlInterface = null;
    }

    public void recognize(String tag, int id, Notification notification, UserHandle user, String pkg, int uid, int pid) {
        Throwable th;
        Exception e;
        Throwable th2;
        if (!IS_CHINA_REGION) {
            Log.i(TAG, "recognize: not in china");
        } else if (isFeartureDisable()) {
            Log.i(TAG, "recognize: feature is disabled");
        } else {
            Log.i(TAG, "recognize: tag=" + tag + ", id=" + id + ", user=" + user + ", pkg=" + pkg + ", uid=" + uid + ", callingPid=" + pid);
            StringBuilder sb = new StringBuilder();
            sb.append(pkg);
            sb.append(pid);
            String key = sb.toString();
            synchronized (this.mRecognizeMap) {
                try {
                    if ("0".equals(this.mRecognizeMap.get(key))) {
                        try {
                            Log.i(TAG, "Return ! recognize the app not in list : " + pkg);
                            return;
                        } catch (Throwable th3) {
                            th = th3;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th4) {
                                    th = th4;
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            if (isSystemApp(pkg, uid)) {
                Log.i(TAG, "recognize: system app");
                synchronized (this.mRecognizeMap) {
                    this.mRecognizeMap.put(key, "0");
                }
            } else if (this.mHwRecSysAidlInterface == null && this.mRecHandler != null) {
                long currentTime = System.currentTimeMillis();
                long bindInterval = Math.abs(currentTime - this.mLastBindTime);
                Log.i(TAG, "RecSys service is disconnect, we should retry to connect service, bindInterval=" + bindInterval);
                if (bindInterval > 300000) {
                    this.mLastBindTime = currentTime;
                    bindRecSys();
                }
            } else if (this.mHwRecSysAidlInterface != null) {
                try {
                    try {
                        String type = this.mHwRecSysAidlInterface.doNotificationCollect(new StatusBarNotification(pkg, pkg, id, tag, uid, pid, notification, user, (String) null, System.currentTimeMillis()));
                        if (type != null) {
                            if (type.equals("0")) {
                                try {
                                    synchronized (this.mRecognizeMap) {
                                        try {
                                            try {
                                                this.mRecognizeMap.put(key, type);
                                                try {
                                                    Log.d(TAG, "recognize: just ignore type : " + type);
                                                } catch (Exception e2) {
                                                    e = e2;
                                                    Log.e(TAG, "doNotificationCollect failed", e);
                                                }
                                            } catch (Throwable th6) {
                                                th2 = th6;
                                                throw th2;
                                            }
                                        } catch (Throwable th7) {
                                            th2 = th7;
                                            throw th2;
                                        }
                                    }
                                } catch (Exception e3) {
                                    e = e3;
                                    Log.e(TAG, "doNotificationCollect failed", e);
                                }
                            } else {
                                if (type.equals("103")) {
                                    notification.extras.putString("hw_type", "type_music");
                                } else {
                                    notification.extras.putString("hw_type", type);
                                }
                                notification.extras.putBoolean("hw_btw", this.mBtwList.contains(type));
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("doNotificationCollect: pkg=");
                                sb2.append(pkg);
                                sb2.append(", uid=");
                                try {
                                    sb2.append(uid);
                                    sb2.append(", hw_type=");
                                    sb2.append(type);
                                    sb2.append(", hw_btw=");
                                    sb2.append(this.mBtwList.contains(type));
                                    Log.i(TAG, sb2.toString());
                                } catch (Exception e4) {
                                    e = e4;
                                }
                            }
                        }
                    } catch (Exception e5) {
                        e = e5;
                        Log.e(TAG, "doNotificationCollect failed", e);
                    }
                } catch (Exception e6) {
                    e = e6;
                    Log.e(TAG, "doNotificationCollect failed", e);
                }
            }
        }
    }

    private boolean isSystemApp(String pkg, int uid) {
        if (PackageManagerServiceEx.PLATFORM_PACKAGE_NAME.equals(pkg)) {
            return true;
        }
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getUserId(uid));
            if (ai != null && ai.isSystemApp() && !isRemoveAblePreInstall(ai, pkg)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isRemoveAblePreInstall(ApplicationInfo ai, String pkg) {
        return ((ai.hwFlags & 33554432) == 0 && (ai.hwFlags & HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW) == 0) ? false : true;
    }

    private boolean isFeartureDisable() {
        boolean isDisable;
        long callingId = Binder.clearCallingIdentity();
        try {
            int userId = ActivityManager.getCurrentUser();
            synchronized (this.mSmartNtfSwitchMap) {
                isDisable = this.mSmartNtfSwitchMap.containsKey(Integer.valueOf(userId)) && !this.mSmartNtfSwitchMap.get(Integer.valueOf(userId)).booleanValue();
            }
            return isDisable;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNotificationDisable() {
        return HwDeviceManager.disallowOp(102);
    }

    /* JADX INFO: finally extract failed */
    public boolean isAllowToShow(String pkg, ActivityInfo topActivity) {
        if (!((topActivity == null || pkg == null || pkg.equals(topActivity.packageName)) ? false : true)) {
            return true;
        }
        int uid = Binder.getCallingUid();
        long restoreCurId = Binder.clearCallingIdentity();
        try {
            boolean isHsmCheck = HwServiceSecurityPartsFactoryEx.getInstance().getHwAddViewHelper(getContext()).addViewPermissionCheck(pkg, 2, uid);
            Binder.restoreCallingIdentity(restoreCurId);
            Slog.i("ToastInterrupt", "isAllowToShowToast:" + isHsmCheck + ", pkg:" + pkg + ", topActivity:" + topActivity);
            return isHsmCheck;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(restoreCurId);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCustDialer(String packageName) {
        return CUST_DIALER_ENABLE && DIALER_PKGNAME.equals(packageName);
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || (activityManager = (ActivityManager) getContext().getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    public void addNotificationFlag(StatusBarNotification sbn) {
        if (sbn.getNotification().extras != null) {
            if (!DISABLE_MULTIWIN) {
                try {
                    sbn.getNotification().extras.putString("specialType", getNotificationType(sbn, HWMULTIWIN_ENABLED));
                    boolean isPackageRequestNarrowNotification = isPackageRequestNarrowNotification();
                    sbn.getNotification().extras.putBoolean("topFullscreen", !isExpandedNtfPkg(sbn.getPackageName()) && (isTopFullscreen() || isPackageRequestNarrowNotification) && !isCustDialer(sbn.getPackageName()));
                    sbn.getNotification().extras.putBoolean("isRequestSingleLine", isPackageRequestNarrowNotification);
                    Log.i(TAG, "specialType is:" + sbn.getNotification().extras.getString("specialType") + " ,topFullscreen is:" + sbn.getNotification().extras.getBoolean("topFullscreen") + ",isRequestSingleLine :" + sbn.getNotification().extras.getBoolean("isRequestSingleLine") + ",HWMULTIWIN_ENABLED is:" + HWMULTIWIN_ENABLED);
                } catch (ConcurrentModificationException e) {
                    Log.e(TAG, "ConcurrentModificationException is happen !!!");
                }
            }
            boolean isGameDndSwitchOn = false;
            boolean isDeferLaunchActivity = false;
            try {
                if (this.mGameDndStatus) {
                    isGameDndSwitchOn = isGameDndSwitchOn();
                    sbn.getNotification().extras.putBoolean("gameDndSwitchOn", isGameDndSwitchOn);
                    PendingIntent pendingIntent = sbn.getNotification().fullScreenIntent;
                    if (!(pendingIntent == null || pendingIntent.getIntent() == null || pendingIntent.getIntent().getComponent() == null)) {
                        isDeferLaunchActivity = HwSnsVideoManager.getDeferLaunchingActivitys().contains(pendingIntent.getIntent().getComponent().flattenToShortString());
                    }
                    sbn.getNotification().extras.putBoolean("isDeferLaunchActivity", isDeferLaunchActivity);
                }
                sbn.getNotification().extras.putBoolean("gameDndOn", this.mGameDndStatus);
                Log.d(TAG, "mGameDndStatus is:" + this.mGameDndStatus + " ,isGameDndSwitchOn is:" + isGameDndSwitchOn + ",isDeferLaunchActivity is:" + isDeferLaunchActivity);
            } catch (ConcurrentModificationException e2) {
                Log.e(TAG, "notification.extras:" + e2.toString());
            }
        }
    }

    private boolean isExpandedNtfPkg(String pkgName) {
        return EXPANDEDNTF_PKGS.contains(pkgName);
    }

    private boolean isTopFullscreen() {
        int ret = 0;
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm == null) {
                return false;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            wm.asBinder().transact(IS_TOP_FULL_SCREEN_TOKEN, data, reply, 0);
            ret = reply.readInt();
            if (ret > 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "isTopIsFullscreen RemoteException !!!");
        }
    }

    public void setNotificationWhiteList() {
        String appsPlus = Settings.Secure.getString(getContext().getContentResolver(), POWER_SAVER_NOTIFICATION_WHITELIST);
        Log.i(TAG, "getNotificationWhiteList from db: " + appsPlus);
        this.power_save_whiteSet.clear();
        for (String pkg : PLUS_NOTIFICATION_WHITE_LIST) {
            this.power_save_whiteSet.add(pkg);
        }
        for (String pkg2 : NOTIFICATION_WHITE_LIST) {
            this.power_save_whiteSet.add(pkg2);
        }
        if (!TextUtils.isEmpty(appsPlus)) {
            for (String pkg3 : appsPlus.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
                this.power_save_whiteSet.add(pkg3);
            }
        }
    }

    public boolean isNoitficationWhiteApp(String pkg) {
        return this.power_save_whiteSet.contains(pkg);
    }

    /* access modifiers changed from: private */
    public final class PowerSaverObserver extends ContentObserver {
        private final Uri SUPER_POWER_SAVE_NOTIFICATION_URI = Settings.Secure.getUriFor(HwNotificationManagerService.POWER_SAVER_NOTIFICATION_WHITELIST);
        private boolean isInitObserver = false;

        PowerSaverObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            if (!this.isInitObserver) {
                this.isInitObserver = true;
                HwNotificationManagerService.this.getContext().getContentResolver().registerContentObserver(this.SUPER_POWER_SAVE_NOTIFICATION_URI, false, this, -1);
                update(null);
            }
        }

        /* access modifiers changed from: package-private */
        public void unObserve() {
            this.isInitObserver = false;
            HwNotificationManagerService.this.getContext().getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (uri == null || this.SUPER_POWER_SAVE_NOTIFICATION_URI.equals(uri)) {
                HwNotificationManagerService.this.setNotificationWhiteList();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkCallerIsSystemOrSystemApp() {
        if (!isCallerSystemOrSystemApp()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    private boolean isCallerSystemOrSystemApp() {
        boolean z = true;
        if (isCallerSystemOrPhone()) {
            return true;
        }
        boolean isSystemApp = false;
        int callingUid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mPackageManager.checkUidSignatures(callingUid, 1000) != 0) {
                z = false;
            }
            isSystemApp = z;
        } catch (RemoteException e) {
            Log.e(TAG, "checkUidSignatures failed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return isSystemApp;
    }

    private String getNotificationType(StatusBarNotification sbn, boolean isHwMultiWin) {
        if (isHwMultiWin) {
            return isResizeableForHwMultiWin(sbn.getNotification(), sbn.getUserId()) ? "floating_window_notification" : "";
        }
        return SplitNotificationUtils.getInstance(getContext()).getNotificationType(sbn.getPackageName(), 1);
    }

    private boolean isResizeableForHwMultiWin(Notification notification, int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            PendingIntent pendingIntent = notification.contentIntent != null ? notification.contentIntent : notification.fullScreenIntent;
            if (pendingIntent != null) {
                if (pendingIntent.isActivity()) {
                    Intent jumpIntent = pendingIntent.getIntent();
                    if (jumpIntent == null) {
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    }
                    ResolveInfo resolveInfo = this.mPackageManager.resolveIntent(jumpIntent, jumpIntent.resolveType(this.mContext), 0, userId);
                    if (resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    }
                    Intent mainIntent = new Intent("android.intent.action.MAIN");
                    mainIntent.setPackage(resolveInfo.activityInfo.packageName);
                    mainIntent.addCategory("android.intent.category.LAUNCHER");
                    ResolveInfo mainResolveInfo = this.mPackageManager.resolveIntent(mainIntent, mainIntent.resolveType(this.mContext), 0, userId);
                    if (mainResolveInfo == null || mainResolveInfo.activityInfo == null) {
                        Log.e(TAG, "isResizeableForHwMultiWin: mainResolveInfo is null");
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    }
                    boolean isSupportsHwFreeForm = HwActivityTaskManager.isSupportsHwFreeForm(mainResolveInfo.activityInfo);
                    Binder.restoreCallingIdentity(identity);
                    return isSupportsHwFreeForm;
                }
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "method isBanNotification has IllegalArgumentException.");
        } catch (Exception e2) {
            Log.e(TAG, "method isBanNotification has Exception.");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return false;
    }

    /* access modifiers changed from: protected */
    public String readDefaultApprovedFromWhiteList(String defaultApproved) {
        if (!CUST_DIALER_ENABLE) {
            return defaultApproved;
        }
        String approvedWhiteApps = Settings.Global.getString(this.mContext.getContentResolver(), DEFAULTAPPROVIED_WHITEAPPS);
        if (TextUtils.isEmpty(approvedWhiteApps)) {
            return defaultApproved;
        }
        if (TextUtils.isEmpty(defaultApproved)) {
            return approvedWhiteApps;
        }
        return defaultApproved + AwarenessInnerConstants.COLON_KEY + approvedWhiteApps;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedForbidAppNotification(String pkgName, String className, HashMap<String, String> extra) {
        return HwAppActController.getInstance().isNeedForbidAppAct(AppActConstant.NOTIFICATION_FORBIDDEN, pkgName, className, extra);
    }
}
