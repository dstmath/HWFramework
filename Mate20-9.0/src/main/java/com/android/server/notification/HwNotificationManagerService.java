package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hdm.HwDeviceManager;
import android.media.IRingtonePlayer;
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
import android.widget.RemoteViews;
import com.android.server.LocalServices;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.am.HwActivityManagerService;
import com.android.server.lights.LightsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.RankingHelper;
import com.android.server.pm.UserManagerService;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.hsm.HwAddViewHelper;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.IGameObserver;
import com.huawei.recsys.aidl.HwRecSysAidlInterface;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HwNotificationManagerService extends NotificationManagerService {
    private static final String BIND_ACTION = "com.huawei.recsys.action.THIRD_REQUEST_ENGINE";
    private static final long BIND_TIMEOUT = 10000;
    private static final String BUSINESS_NAME = "notification";
    private static final String CONTENTVIEW_REVERTING_FLAG = "HW_CONTENTVIEW_REVERTING_FLAG";
    private static final boolean CUST_DIALER_ENABLE = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final boolean DEBUG = true;
    private static final int EVENT_MARK_AS_GAME = 4;
    private static final int EVENT_MOVE_BACKGROUND = 2;
    private static final int EVENT_MOVE_FRONT = 1;
    private static final int EVENT_REPLACE_FRONT = 3;
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    static final int INIT_CFG_DELAY = 10000;
    private static final String KEY_SMART_NOTIFICATION_SWITCH = "smart_notification_switch";
    private static final String KEY_TRUST_SPACE_BADGE_SWITCH = "trust_secure_hint_enable";
    private static final String NOTIFICATION_ACTION_ALLOW = "com.huawei.notificationmanager.notification.allow";
    private static final String NOTIFICATION_ACTION_ALLOW_FORAPK = "com.huawei.notificationmanager.notification.allow.forAPK";
    private static final String NOTIFICATION_ACTION_REFUSE = "com.huawei.notificationmanager.notification.refuse";
    private static final String NOTIFICATION_ACTION_REFUSE_FORAPK = "com.huawei.notificationmanager.notification.refuse.forAPK";
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    static final int NOTIFICATION_CFG_REMIND = 0;
    private static final String[] NOTIFICATION_NARROW_DISPLAY_APPS = {"com.android.contacts"};
    private static final int NOTIFICATION_PRIORITY_MULTIPLIER = 10;
    private static final String[] NOTIFICATION_WHITE_APPS_PACKAGE = {"com.huawei.intelligent"};
    private static final int OPERATOR_QUERY = 1;
    private static final String PACKAGE_NAME_HSM = "com.huawei.systemmanager";
    private static final String RIDEMODE_NOTIFICATION_WHITE_LIST = "com.huawei.ridemode,com.huawei.sos,com.android.phone,com.android.server.telecom,com.android.incallui,com.android.deskclock,com.android.cellbroadcastreceiver";
    private static final String RULE_NAME = "ongoingAndNormalRules";
    private static final String SERVER_PAKAGE_NAME = "com.huawei.recsys";
    private static final int SMCS_NOTIFICATION_GET_NOTI = 1;
    static final String TAG = "HwNotificationManagerService";
    private static final long TIME_NOT_BIND_LIMIT = 300000;
    private static final int TRUST_SPACE_BADGE_STATUS_ENABLE = 1;
    private static final String TYPE_COUNSELING_MESSAGE = "102";
    private static final String TYPE_IGNORE = "0";
    private static final String TYPE_INFORMATION = "3";
    private static final String TYPE_MUSIC = "103";
    private static final String TYPE_PROMOTION = "2";
    private static final String TYPE_TOOLS = "107";
    private static final String WECHAT_HONGBAO = "[微信红包]";
    private static final String WHITELIST_LABLE = "200";
    /* access modifiers changed from: private */
    public static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static int sRemoveableFlag;
    private static int sUpdateRemovableFlag;
    final Uri URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
    private BatteryManagerInternal mBatteryManagerInternal;
    private Runnable mBindRunable;
    private List<String> mBtwList = new ArrayList();
    DBContentObserver mCfgDBObserver = null;
    Map<String, Integer> mCfgMap = new HashMap();
    private ContentObserver mContentObserver;
    Context mContext;
    private HwCustZenModeHelper mCust = ((HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]));
    private HwCustDefaultApprovedApps mDefaultApprovedApp = ((HwCustDefaultApprovedApps) HwCustUtils.createObj(HwCustDefaultApprovedApps.class, new Object[0]));
    private BroadcastReceiver mHangButtonReceiver;
    private HwGameObserver mHwGameObserver = null;
    Handler mHwHandler = null;
    private final HwStatusBarManagerService.HwNotificationDelegate mHwNotificationDelegate;
    /* access modifiers changed from: private */
    public HwRecSysAidlInterface mHwRecSysAidlInterface;
    private long mLastBindTime = 0;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<NotificationContentViewRecord> mOriginContentViews;
    /* access modifiers changed from: private */
    public Handler mRecHandler;
    private HandlerThread mRecHandlerThread;
    private ArrayMap<String, String> mRecognizeMap = new ArrayMap<>();
    private ServiceConnection mServiceConnection;
    /* access modifiers changed from: private */
    public ArrayMap<Integer, Boolean> mSmartNtfSwitchMap = new ArrayMap<>();
    private final ArrayMap<String, NotificationRecord> mUpdateEnqueuedNotifications = new ArrayMap<>();

    private class DBContentObserver extends ContentObserver {
        public DBContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            Slog.v(HwNotificationManagerService.TAG, "Notification db cfg changed");
            HwNotificationManagerService.this.mHwHandler.post(new HwCfgLoadingRunnable());
        }
    }

    private class HwCfgLoadingRunnable implements Runnable {
        private HwCfgLoadingRunnable() {
        }

        public void run() {
            new Thread("HwCfgLoading") {
                public void run() {
                    HwCfgLoadingRunnable.this.load();
                }
            }.start();
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x019f, code lost:
            if (r2 != null) goto L_0x01a1;
         */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x0194 A[Catch:{ RuntimeException -> 0x0195, Exception -> 0x0188, all -> 0x0184, all -> 0x01ad }] */
        /* JADX WARNING: Removed duplicated region for block: B:77:0x01b0  */
        public void load() {
            Context context;
            int nCidColIndex;
            int nCfgColIndex;
            Context context2;
            int nPkgColIndex;
            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Starts ");
            Cursor cursor = null;
            try {
                context = HwNotificationManagerService.this.mContext.createPackageContextAsUser("com.huawei.systemmanager", 0, new UserHandle(ActivityManager.getCurrentUser()));
            } catch (Exception e) {
                Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to convert context");
                context = null;
            }
            Context context3 = context;
            if (context3 != null) {
                try {
                    cursor = context3.getContentResolver().query(HwNotificationManagerService.this.URI_NOTIFICATION_CFG, null, null, null, null);
                    if (cursor == null) {
                        try {
                            Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to get cfg from DB");
                            if (cursor != null) {
                                cursor.close();
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (RuntimeException e2) {
                            e = e2;
                            Context context4 = context3;
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException ", e);
                        } catch (Exception e3) {
                            e = e3;
                            Context context5 = context3;
                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception ", e);
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        } catch (Throwable th) {
                            th = th;
                            Context context6 = context3;
                            if (cursor != null) {
                            }
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                            throw th;
                        }
                    } else {
                        HashMap hashMap = new HashMap();
                        ArrayList arrayList = new ArrayList();
                        if (cursor.getCount() > 0) {
                            int nPkgColIndex2 = cursor.getColumnIndex("packageName");
                            int nCfgColIndex2 = cursor.getColumnIndex("sound_vibrate");
                            int nCidColIndex2 = cursor.getColumnIndex("channelid");
                            int nLockscreenColIndex = cursor.getColumnIndex("lockscreencfg");
                            int nImportaceColIndex = cursor.getColumnIndex("channelimportance");
                            int nBypassdndColIndex = cursor.getColumnIndex("channelbypassdnd");
                            int nIconDadgeColIndex = cursor.getColumnIndex("channeliconbadge");
                            while (cursor.moveToNext()) {
                                String pkgName = cursor.getString(nPkgColIndex2);
                                if (TextUtils.isEmpty(pkgName)) {
                                    nPkgColIndex = nPkgColIndex2;
                                    context2 = context3;
                                    nCfgColIndex = nCfgColIndex2;
                                    nCidColIndex = nCidColIndex2;
                                } else {
                                    int cfg = cursor.getInt(nCfgColIndex2);
                                    String channelId = cursor.getString(nCidColIndex2);
                                    String key = HwNotificationManagerService.this.pkgAndCidKey(pkgName, channelId);
                                    nPkgColIndex = nPkgColIndex2;
                                    if ("miscellaneous".equals(channelId) != 0) {
                                        RankingHelper.NotificationSysMgrCfg mgrCfg = new RankingHelper.NotificationSysMgrCfg();
                                        context2 = context3;
                                        try {
                                            mgrCfg.smc_userId = ActivityManager.getCurrentUser();
                                            mgrCfg.smc_packageName = pkgName;
                                            mgrCfg.smc_visilibity = cursor.getInt(nLockscreenColIndex);
                                            mgrCfg.smc_importance = cursor.getInt(nImportaceColIndex);
                                            mgrCfg.smc_bypassDND = cursor.getInt(nBypassdndColIndex);
                                            mgrCfg.smc_iconBadge = cursor.getInt(nIconDadgeColIndex);
                                            arrayList.add(mgrCfg);
                                            if ("com.android.mms".equals(pkgName)) {
                                                nCfgColIndex = nCfgColIndex2;
                                                StringBuilder sb = new StringBuilder();
                                                nCidColIndex = nCidColIndex2;
                                                sb.append("mgrCfg.importance : ");
                                                sb.append(mgrCfg.smc_importance);
                                                Slog.i(HwNotificationManagerService.TAG, sb.toString());
                                            } else {
                                                nCfgColIndex = nCfgColIndex2;
                                                nCidColIndex = nCidColIndex2;
                                            }
                                        } catch (RuntimeException e4) {
                                            e = e4;
                                        } catch (Exception e5) {
                                            e = e5;
                                            Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception ", e);
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                                        }
                                    } else {
                                        context2 = context3;
                                        nCfgColIndex = nCfgColIndex2;
                                        nCidColIndex = nCidColIndex2;
                                    }
                                    hashMap.put(key, Integer.valueOf(cfg));
                                }
                                nPkgColIndex2 = nPkgColIndex;
                                context3 = context2;
                                nCfgColIndex2 = nCfgColIndex;
                                nCidColIndex2 = nCidColIndex;
                            }
                        }
                        HwNotificationManagerService.this.setSysMgrCfgMap(arrayList);
                        synchronized (HwNotificationManagerService.this.mCfgMap) {
                            HwNotificationManagerService.this.mCfgMap.clear();
                            HwNotificationManagerService.this.mCfgMap.putAll(hashMap);
                            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: get cfg size:" + HwNotificationManagerService.this.mCfgMap.size());
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                    }
                } catch (RuntimeException e6) {
                    e = e6;
                    Context context7 = context3;
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException ", e);
                } catch (Exception e7) {
                    e = e7;
                    Context context8 = context3;
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception ", e);
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
            }
        }
    }

    private class HwGameObserver extends IGameObserver.Stub {
        private HwGameObserver() {
        }

        public void onGameListChanged() {
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (1 == event || 3 == event || 4 == event) {
                HwNotificationManagerService.this.mGameDndStatus = true;
            } else if (2 == event) {
                HwNotificationManagerService.this.mGameDndStatus = false;
            }
            HwNotificationManagerService.this.updateNotificationInGameMode();
            Log.d(HwNotificationManagerService.TAG, "onGameStatusChanged event=" + event + ",mGameDndStatus=" + HwNotificationManagerService.this.mGameDndStatus);
        }
    }

    private static final class NotificationContentViewRecord {
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

        public final String toString() {
            return "NotificationContentViewRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " uid=" + this.uid + " id=" + Integer.toHexString(this.id) + " tag=" + this.tag + " userId=" + Integer.toHexString(this.userId) + "}";
        }
    }

    static {
        sRemoveableFlag = 0;
        sUpdateRemovableFlag = 0;
        sRemoveableFlag = getStaticIntFiled("com.huawei.android.content.pm.PackageParserEx", "PARSE_IS_REMOVABLE_PREINSTALLED_APK");
        sUpdateRemovableFlag = getStaticIntFiled("com.huawei.android.content.pm.PackageParserEx", "FLAG_UPDATED_REMOVEABLE_APP");
    }

    private StatusBarManagerService getStatusBarManagerService() {
        return ServiceManager.getService("statusbar");
    }

    public void onStart() {
        HwNotificationManagerService.super.onStart();
        this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
    }

    public HwNotificationManagerService(Context context, StatusBarManagerService statusBar, LightsService lights) {
        super(context);
        this.mBtwList.add("2");
        this.mBtwList.add("3");
        this.mBtwList.add("102");
        this.mBtwList.add(TYPE_TOOLS);
        this.mHwNotificationDelegate = new HwStatusBarManagerService.HwNotificationDelegate() {
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, 0, true, userId, 2, null);
            }
        };
        this.mHangButtonReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        String pkg = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        int uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (action != null && action.equals(HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE)) {
                        String pkg2 = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        int uid2 = intent.getIntExtra("uid", -1);
                        if (!(pkg2 == null || uid2 == -1)) {
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
                    }
                }
            }
        };
        this.mOriginContentViews = new ArrayList<>();
        this.mBindRunable = new Runnable() {
            public void run() {
                HwNotificationManagerService.this.bind();
            }
        };
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwNotificationManagerService.TAG, "onServiceConnected");
                HwRecSysAidlInterface unused = HwNotificationManagerService.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(HwNotificationManagerService.TAG, "onServiceDisConnected");
                HwRecSysAidlInterface unused = HwNotificationManagerService.this.mHwRecSysAidlInterface = null;
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }
        };
        this.mContentObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                HwNotificationManagerService.this.mRecHandler.postAtFrontOfQueue(new Runnable() {
                    public void run() {
                        int userId = ActivityManager.getCurrentUser();
                        boolean enable = Settings.System.getIntForUser(HwNotificationManagerService.this.mContext.getContentResolver(), HwNotificationManagerService.KEY_SMART_NOTIFICATION_SWITCH, HwNotificationManagerService.mIsChina ? 1 : 0, userId) == 1;
                        boolean allUserClose = true;
                        synchronized (HwNotificationManagerService.this.mSmartNtfSwitchMap) {
                            HwNotificationManagerService.this.mSmartNtfSwitchMap.put(Integer.valueOf(userId), Boolean.valueOf(enable));
                            if (HwNotificationManagerService.this.mSmartNtfSwitchMap.containsValue(true)) {
                                allUserClose = false;
                            }
                        }
                        if (allUserClose) {
                            HwNotificationManagerService.this.unBind();
                        } else {
                            HwNotificationManagerService.this.bindRecSys();
                        }
                        Log.i(HwNotificationManagerService.TAG, "switch change to: " + enable + ",userId: " + userId + ", allUserClose :" + allUserClose);
                    }
                });
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
        this.mHwNotificationDelegate = new HwStatusBarManagerService.HwNotificationDelegate() {
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, 0, true, userId, 2, null);
            }
        };
        this.mHangButtonReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        String pkg = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        int uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (action != null && action.equals(HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE)) {
                        String pkg2 = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        int uid2 = intent.getIntExtra("uid", -1);
                        if (!(pkg2 == null || uid2 == -1)) {
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
                    }
                }
            }
        };
        this.mOriginContentViews = new ArrayList<>();
        this.mBindRunable = new Runnable() {
            public void run() {
                HwNotificationManagerService.this.bind();
            }
        };
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwNotificationManagerService.TAG, "onServiceConnected");
                HwRecSysAidlInterface unused = HwNotificationManagerService.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(HwNotificationManagerService.TAG, "onServiceDisConnected");
                HwRecSysAidlInterface unused = HwNotificationManagerService.this.mHwRecSysAidlInterface = null;
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }
        };
        this.mContentObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                HwNotificationManagerService.this.mRecHandler.postAtFrontOfQueue(new Runnable() {
                    public void run() {
                        int userId = ActivityManager.getCurrentUser();
                        boolean enable = Settings.System.getIntForUser(HwNotificationManagerService.this.mContext.getContentResolver(), HwNotificationManagerService.KEY_SMART_NOTIFICATION_SWITCH, HwNotificationManagerService.mIsChina ? 1 : 0, userId) == 1;
                        boolean allUserClose = true;
                        synchronized (HwNotificationManagerService.this.mSmartNtfSwitchMap) {
                            HwNotificationManagerService.this.mSmartNtfSwitchMap.put(Integer.valueOf(userId), Boolean.valueOf(enable));
                            if (HwNotificationManagerService.this.mSmartNtfSwitchMap.containsValue(true)) {
                                allUserClose = false;
                            }
                        }
                        if (allUserClose) {
                            HwNotificationManagerService.this.unBind();
                        } else {
                            HwNotificationManagerService.this.bindRecSys();
                        }
                        Log.i(HwNotificationManagerService.TAG, "switch change to: " + enable + ",userId: " + userId + ", allUserClose :" + allUserClose);
                    }
                });
            }
        };
        this.mContext = context;
        SystemServerInitThreadPool.get().submit(new Runnable() {
            public final void run() {
                HwNotificationManagerService.this.init();
            }
        }, "HwNotificationManagerService init");
    }

    /* access modifiers changed from: private */
    public void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_ACTION_ALLOW);
        filter.addAction(NOTIFICATION_ACTION_REFUSE);
        this.mContext.registerReceiverAsUser(this.mHangButtonReceiver, UserHandle.ALL, filter, "com.android.permission.system_manager_interface", null);
        HwStatusBarManagerService hwStatusBar = getStatusBarManagerService();
        if (hwStatusBar instanceof HwStatusBarManagerService) {
            hwStatusBar.setHwNotificationDelegate(this.mHwNotificationDelegate);
        }
        this.mHwHandler = new Handler(Looper.getMainLooper());
        this.mHwHandler.postDelayed(new HwCfgLoadingRunnable(), 10000);
        this.mCfgDBObserver = new DBContentObserver();
        this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        this.mRecHandlerThread = new HandlerThread("notification manager");
        this.mRecHandlerThread.start();
        this.mRecHandler = new Handler(this.mRecHandlerThread.getLooper());
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
    }

    /* access modifiers changed from: protected */
    public void handleGetNotifications(Parcel data, Parcel reply) {
        HashSet<String> notificationPkgs = new HashSet<>();
        if (this.mContext.checkCallingPermission("huawei.permission.IBINDER_NOTIFICATION_SERVICE") != 0) {
            Slog.e(TAG, "NotificationManagerService.handleGetNotifications: permissin deny");
            return;
        }
        getNotificationPkgs_hwHsm(notificationPkgs);
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
    public void getNotificationPkgs_hwHsm(HashSet<String> notificationPkgs) {
        if (notificationPkgs != null) {
            if (notificationPkgs.size() > 0) {
                notificationPkgs.clear();
            }
            synchronized (this.mNotificationList) {
                int N = this.mNotificationList.size();
                if (N != 0) {
                    for (int i = 0; i < N; i++) {
                        NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                        if (r != null) {
                            String sPkg = r.sbn.getPackageName();
                            if (sPkg != null && sPkg.length() > 0) {
                                notificationPkgs.add(sPkg);
                            }
                        }
                    }
                    int i2 = N;
                }
            }
        }
    }

    private int indexOfContentViewLocked(String pkg, String tag, int id, int userId) {
        synchronized (this.mOriginContentViews) {
            ArrayList<NotificationContentViewRecord> list = this.mOriginContentViews;
            int len = list.size();
            for (int i = 0; i < len; i++) {
                NotificationContentViewRecord r = list.get(i);
                if (userId == -1 || r.userId == -1 || r.userId == userId) {
                    if (r.id == id) {
                        if (tag == null) {
                            if (r.tag != null) {
                            }
                        } else if (!tag.equals(r.tag)) {
                        }
                        if (r.pkg.equals(pkg)) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void recoverNotificationContentView(String pkg, int uid) {
        String str = pkg;
        synchronized (this.mOriginContentViews) {
            Iterator<NotificationContentViewRecord> itor = this.mOriginContentViews.iterator();
            while (itor.hasNext()) {
                NotificationContentViewRecord cvr = itor.next();
                if (cvr.pkg.equals(str)) {
                    NotificationRecord r = findNotificationByListLocked(this.mNotificationList, cvr.pkg, cvr.tag, cvr.id, cvr.userId);
                    if (r != null) {
                        r.sbn.getNotification().bigContentView = cvr.rOldBigContentView;
                        Slog.d(TAG, "revertNotificationView enqueueNotificationInternal pkg=" + str + " id=" + r.sbn.getId() + " userId=" + r.sbn.getUserId());
                        r.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, true);
                        enqueueNotificationInternal(str, r.sbn.getOpPkg(), r.sbn.getUid(), r.sbn.getInitialPid(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getNotification(), r.sbn.getUserId());
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
        Integer cfg;
        String key = pkgAndCidKey(pkg, channelId);
        synchronized (this.mCfgMap) {
            cfg = this.mCfgMap.get(key);
        }
        Slog.v(TAG, "isHwSoundAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", cfg=" + cfg);
        return cfg == null || (cfg.intValue() & 1) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean isHwVibrateAllow(String pkg, String channelId, int userId) {
        Integer cfg;
        String key = pkgAndCidKey(pkg, channelId);
        synchronized (this.mCfgMap) {
            cfg = this.mCfgMap.get(key);
        }
        Slog.v(TAG, "isHwVibrateAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", cfg=" + cfg);
        return cfg == null || (cfg.intValue() & 2) != 0;
    }

    /* access modifiers changed from: private */
    public String pkgAndCidKey(String pkg, String channelId) {
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
        intent.putExtra("packageName", pkg);
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
        if (this.mCust != null && this.mCust.getWhiteApps(this.mContext) != null) {
            return Arrays.asList(this.mCust.getWhiteApps(this.mContext)).contains(pkg);
        }
        String[] DEFAULT_WHITEAPP = {"com.android.mms", "com.android.contacts"};
        if (this.mContext == null) {
            return false;
        }
        for (String pkgname : DEFAULT_WHITEAPP) {
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
        if ((pkg.equals("com.android.phone") || pkg.equals("com.android.mms") || pkg.equals("com.android.contacts") || pkg.equals("com.android.server.telecom")) && notification.priority < 7) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsNotificationEnable(String pkg) {
        if (pkg == null || (!pkg.equalsIgnoreCase("com.android.mms") && !pkg.equalsIgnoreCase("com.android.contacts"))) {
            return false;
        }
        return true;
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
    public void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
        this.mBatteryManagerInternal.updateBatteryLight(enable, ledOnMS, ledOffMS);
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitchEvents(int userId) {
        if (this.mCfgDBObserver != null) {
            this.mHwHandler.post(new HwCfgLoadingRunnable());
            this.mContext.getContentResolver().unregisterContentObserver(this.mCfgDBObserver);
            this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* access modifiers changed from: protected */
    public boolean isAFWUserId(int userId) {
        boolean temp = false;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManagerService.getInstance().getUserInfo(userId);
            if (userInfo != null) {
                temp = userInfo.isManagedProfile() || userInfo.isClonedProfile();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
        return temp;
    }

    /* access modifiers changed from: protected */
    public void addHwExtraForNotification(Notification notification, String pkg, int pid) {
        if (isIntentProtectedApp(pkg) && isTrustSpaceBadgeEnabled()) {
            notification.extras.putBoolean("com.huawei.isIntentProtectedApp", true);
        }
    }

    private boolean isIntentProtectedApp(String pkg) {
        TrustSpaceManagerInternal mTrustSpaceManagerInternal = (TrustSpaceManagerInternal) LocalServices.getService(TrustSpaceManagerInternal.class);
        if (mTrustSpaceManagerInternal != null) {
            return mTrustSpaceManagerInternal.isIntentProtectedApp(pkg);
        }
        Slog.e(TAG, "TrustSpaceManagerInternal not find !");
        return false;
    }

    private boolean isTrustSpaceBadgeEnabled() {
        return 1 == Settings.Secure.getInt(this.mContext.getContentResolver(), KEY_TRUST_SPACE_BADGE_SWITCH, 1);
    }

    /* access modifiers changed from: protected */
    public int getNCTargetAppUid(String opPkg, String pkg, int defaultUid, Notification notification) {
        int uid = defaultUid;
        if (!NOTIFICATION_CENTER_PKG.equals(opPkg)) {
            return uid;
        }
        Bundle bundle = notification.extras;
        if (bundle == null) {
            return uid;
        }
        String targetPkg = bundle.getString(NOTIFICATION_CENTER_ORIGIN_PKG);
        if (targetPkg == null || !targetPkg.equals(pkg)) {
            return uid;
        }
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(targetPkg, 0, UserHandle.getCallingUserId());
            if (ai != null) {
                return ai.uid;
            }
            return uid;
        } catch (Exception e) {
            Slog.w(TAG, "Unknown package pkg:" + targetPkg);
            return uid;
        }
    }

    /* access modifiers changed from: protected */
    public String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
        String pkg = defaultPkg;
        if (!NOTIFICATION_CENTER_PKG.equals(opPkg)) {
            return pkg;
        }
        Bundle bundle = notification.extras;
        if (bundle == null) {
            return pkg;
        }
        String targetPkg = bundle.getString(NOTIFICATION_CENTER_ORIGIN_PKG);
        if (targetPkg == null || !isVaildPkg(targetPkg)) {
            return pkg;
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
        return HWRIDEMODE_FEATURE_SUPPORTED && SystemProperties.getBoolean("sys.ride_mode", false) && !RIDEMODE_NOTIFICATION_WHITE_LIST.contains(pkg);
    }

    public void reportToIAware(String pkg, int uid, int nid, boolean added) {
        if (pkg != null && !pkg.isEmpty()) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkg);
                bundleArgs.putInt("tgtUid", uid);
                bundleArgs.putInt("notification_id", nid);
                bundleArgs.putInt("relationType", added ? 20 : 21);
                CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean doForUpdateNotification(String key, Handler handler) {
        int i = 0;
        if (!MemoryConstant.isNotificatinSwitchEnable() || handler == null || key == null || !isUpdateNotificationNeedToMng(key)) {
            return false;
        }
        long interval = MemoryConstant.getNotificationInterval();
        NotificationRecord newNotification = findNotificationByListLocked(key);
        if (newNotification == null) {
            return false;
        }
        if (((newNotification.getFlags() & 2) != 0 && (newNotification.getFlags() & 64) != 0) || !isImUpdateNotification(newNotification)) {
            return false;
        }
        if (this.mUpdateEnqueuedNotifications.get(key) != null) {
            if (isWechatHongbao(newNotification)) {
                handler.post(new NotificationManagerService.PostNotificationRunnable(this, key));
            } else {
                this.mUpdateEnqueuedNotifications.put(key, newNotification);
            }
            int N = this.mEnqueuedNotifications.size();
            while (true) {
                if (i >= N) {
                    break;
                } else if (Objects.equals(key, ((NotificationRecord) this.mEnqueuedNotifications.get(i)).getKey())) {
                    this.mEnqueuedNotifications.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        } else if (isWechatHongbao(newNotification)) {
            return false;
        } else {
            this.mUpdateEnqueuedNotifications.put(key, newNotification);
            handler.postDelayed(new NotificationManagerService.PostNotificationRunnable(this, key), interval);
        }
        return true;
    }

    private boolean isImUpdateNotification(NotificationRecord record) {
        StatusBarNotification n = record.sbn;
        if (n == null) {
            return false;
        }
        int appType = AwareIntelligentRecg.getInstance().getAppMngSpecType(n.getPackageName());
        if (appType == 0 || 6 == appType || 311 == appType || 318 == appType) {
            return true;
        }
        return false;
    }

    private boolean isWechatHongbao(NotificationRecord record) {
        StatusBarNotification n = record.sbn;
        String topImCN = AwareIntelligentRecg.getInstance().getActTopIMCN();
        if (topImCN == null || !topImCN.equals(n.getPackageName())) {
            return false;
        }
        Notification notification = n.getNotification();
        if (notification != null) {
            Bundle extras = notification.extras;
            if (extras != null) {
                CharSequence charSequence = extras.getCharSequence("android.text");
                if (charSequence != null && charSequence.toString().contains(WECHAT_HONGBAO)) {
                    return true;
                }
            }
        }
        return false;
    }

    private NotificationRecord findNotificationByListLocked(String key) {
        for (int i = this.mEnqueuedNotifications.size() - 1; i >= 0; i--) {
            if (key.equals(((NotificationRecord) this.mEnqueuedNotifications.get(i)).getKey())) {
                return (NotificationRecord) this.mEnqueuedNotifications.get(i);
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

    private boolean isPkgInWhiteApp(String pkg) {
        for (String s : NOTIFICATION_WHITE_APPS_PACKAGE) {
            if (s.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isGameRunningForeground() {
        HwActivityManagerService mAms = HwActivityManagerService.self();
        if (mAms != null) {
            return mAms.isGameDndOn();
        }
        Slog.e(TAG, "Canot obtain HwActivityManagerService , mAms is null");
        return false;
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
        HwActivityManagerService.self().registerGameObserver(this.mHwGameObserver);
    }

    /* access modifiers changed from: private */
    public void updateNotificationInGameMode() {
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    public void bindRecSys() {
        if (this.mRecHandler != null) {
            this.mRecHandler.removeCallbacks(this.mBindRunable);
            this.mRecHandler.post(this.mBindRunable);
        }
    }

    /* access modifiers changed from: private */
    public void bind() {
        if (this.mHwRecSysAidlInterface != null) {
            Log.d(TAG, "bind: already binded");
            return;
        }
        try {
            Log.i(TAG, "bind service: action=com.huawei.recsys.action.THIRD_REQUEST_ENGINE, pkg=com.huawei.recsys");
            Intent intent = new Intent();
            intent.setAction(BIND_ACTION);
            intent.setPackage(SERVER_PAKAGE_NAME);
            boolean ret = this.mContext.bindService(intent, this.mServiceConnection, 1);
            if (ret) {
                synchronized (this.mLock) {
                    this.mLock.wait(10000);
                }
            }
            Log.i(TAG, "bind service finish, ret=" + ret);
        } catch (Exception e) {
            Log.e(TAG, "bind service failed!", e);
        }
    }

    /* access modifiers changed from: private */
    public void unBind() {
        if (this.mRecHandler != null) {
            this.mRecHandler.removeCallbacks(this.mBindRunable);
        }
        if (this.mHwRecSysAidlInterface == null) {
            Log.d(TAG, "unbind: already unbinded");
            return;
        }
        try {
            Log.i(TAG, "unbind service");
            this.mContext.unbindService(this.mServiceConnection);
            Log.i(TAG, "unbind service finish");
        } catch (Exception e) {
            Log.e(TAG, "unbind service failed!", e);
        }
        this.mHwRecSysAidlInterface = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00ad, code lost:
        if (isSystemApp(r15, r12) == false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00af, code lost:
        android.util.Log.i(TAG, "recognize: system app");
        r2 = r1.mRecognizeMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b9, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r1.mRecognizeMap.put(r8, "0");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c1, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c2, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c8, code lost:
        if (r1.mHwRecSysAidlInterface != null) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00cc, code lost:
        if (r1.mRecHandler == null) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ce, code lost:
        r2 = java.lang.System.currentTimeMillis();
        android.util.Log.i(TAG, "RecSys service is disconnect, we should retry to connect service, bindInterval=" + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00f5, code lost:
        if (r4 <= 300000) goto L_0x00fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f7, code lost:
        r1.mLastBindTime = r2;
        bindRecSys();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00fc, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ff, code lost:
        if (r1.mHwRecSysAidlInterface == null) goto L_0x01d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0109, code lost:
        r2 = r2;
        r19 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r2 = new android.service.notification.StatusBarNotification(r15, r15, r10, r11, r12, r13, r14, r25, null, java.lang.System.currentTimeMillis());
        r3 = r1.mHwRecSysAidlInterface.doNotificationCollect(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0124, code lost:
        if (r3 != null) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0126, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x012d, code lost:
        if (r3.equals("0") == false) goto L_0x0159;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x012f, code lost:
        r4 = r1.mRecognizeMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0131, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        r1.mRecognizeMap.put(r19, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0139, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        android.util.Log.d(TAG, "recognize: just ignore type : " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0151, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0152, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0154, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0155, code lost:
        r5 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0159, code lost:
        r5 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0161, code lost:
        if (r3.equals("103") == false) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0163, code lost:
        r14.extras.putString("hw_type", "type_music");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x016e, code lost:
        r14.extras.putString("hw_type", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0175, code lost:
        r14.extras.putBoolean("hw_btw", r1.mBtwList.contains(r3));
        r4 = new java.lang.StringBuilder();
        r4.append("doNotificationCollect: pkg=");
        r4.append(r15);
        r4.append(", uid=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        r4.append(r27);
        r4.append(", hw_type=");
        r4.append(r3);
        r4.append(", hw_btw=");
        r4.append(r1.mBtwList.contains(r3));
        android.util.Log.i(TAG, r4.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01b9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01bb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01bc, code lost:
        r6 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01bf, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01c0, code lost:
        r5 = r19;
        r6 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01c5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01c6, code lost:
        r5 = r8;
        r6 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01c8, code lost:
        android.util.Log.e(TAG, "doNotificationCollect failed", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01d0, code lost:
        r5 = r8;
        r6 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01d2, code lost:
        return;
     */
    public void recognize(String tag, int id, Notification notification, UserHandle user, String pkg, int uid, int pid) {
        Notification notification2 = notification;
        String str = pkg;
        int i = uid;
        int i2 = pid;
        if (!mIsChina) {
            Log.i(TAG, "recognize: not in china");
        } else if (isFeartureDisable()) {
            Log.i(TAG, "recognize: feature is disabled");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("recognize: tag=");
            String str2 = tag;
            sb.append(str2);
            sb.append(", id=");
            int i3 = id;
            sb.append(i3);
            sb.append(", user=");
            sb.append(user);
            sb.append(", pkg=");
            sb.append(str);
            sb.append(", uid=");
            sb.append(i);
            sb.append(", callingPid=");
            sb.append(i2);
            Log.i(TAG, sb.toString());
            String key = str + i2;
            synchronized (this.mRecognizeMap) {
                try {
                    if ("0".equals(this.mRecognizeMap.get(key))) {
                        try {
                            Log.i(TAG, "Return ! recognize the app not in list : " + str);
                        } catch (Throwable th) {
                            th = th;
                            String str3 = key;
                            int i4 = i;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    String str4 = key;
                    int i5 = i;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private boolean isSystemApp(String pkg, int uid) {
        if ("android".equals(pkg)) {
            return true;
        }
        boolean z = false;
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getUserId(uid));
            if (ai == null) {
                return false;
            }
            if (ai.isSystemApp() && !isRemoveAblePreInstall(ai, pkg)) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isRemoveAblePreInstall(ApplicationInfo ai, String pkg) {
        boolean removeable = false;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(ai)).intValue();
            if (!((sRemoveableFlag & hwFlags) == 0 && (sUpdateRemovableFlag & hwFlags) == 0)) {
                removeable = true;
            }
            return removeable;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return false;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return false;
        } catch (Exception e4) {
            e4.printStackTrace();
            return false;
        }
    }

    private static int getStaticIntFiled(String clazzName, String fieldName) {
        try {
            return Class.forName(clazzName).getField(fieldName).getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return 0;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return 0;
        } catch (Exception e4) {
            e4.printStackTrace();
            return 0;
        }
    }

    private boolean isFeartureDisable() {
        long callingId = Binder.clearCallingIdentity();
        boolean isDisable = false;
        try {
            int userId = ActivityManager.getCurrentUser();
            synchronized (this.mSmartNtfSwitchMap) {
                try {
                    if (this.mSmartNtfSwitchMap.containsKey(Integer.valueOf(userId)) && !this.mSmartNtfSwitchMap.get(Integer.valueOf(userId)).booleanValue()) {
                        isDisable = true;
                    }
                    try {
                        return isDisable;
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        boolean z = isDisable;
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
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
            boolean hsmCheck = HwAddViewHelper.getInstance(getContext()).addViewPermissionCheck(pkg, 2, uid);
            Binder.restoreCallingIdentity(restoreCurId);
            Slog.i("ToastInterrupt", "isAllowToShowToast:" + hsmCheck + ", pkg:" + pkg + ", topActivity:" + topActivity);
            return hsmCheck;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(restoreCurId);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCustDialer(String packageName) {
        return CUST_DIALER_ENABLE && "com.android.dialer".equals(packageName);
    }

    /* access modifiers changed from: protected */
    public String readDefaultApprovedFromWhiteList(String defaultApproved) {
        String str;
        if (this.mDefaultApprovedApp == null) {
            return defaultApproved;
        }
        String approvedWhiteApps = this.mDefaultApprovedApp.getWhiteApps(this.mContext);
        if (TextUtils.isEmpty(approvedWhiteApps)) {
            return defaultApproved;
        }
        if (defaultApproved != null) {
            str = addApprovedApps + ":" + approvedWhiteApps;
        } else {
            str = approvedWhiteApps;
        }
        return str;
    }
}
