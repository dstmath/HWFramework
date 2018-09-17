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
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.widget.RemoteViews;
import com.android.server.LocalServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.lights.LightsService;
import com.android.server.notification.RankingHelper.NotificationSysMgrCfg;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pm.UserManagerService;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.HwStatusBarManagerService.HwNotificationDelegate;
import com.android.server.statusbar.StatusBarManagerService;
import com.huawei.android.app.IGameObserver.Stub;
import com.huawei.recsys.aidl.HwRecSysAidlInterface;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwNotificationManagerService extends NotificationManagerService {
    private static final String BIND_ACTION = "com.huawei.recsys.action.THIRD_REQUEST_ENGINE";
    private static final long BIND_TIMEOUT = 10000;
    private static final String BUSINESS_NAME = "notification";
    private static final String CONTENTVIEW_REVERTING_FLAG = "HW_CONTENTVIEW_REVERTING_FLAG";
    private static final boolean DEBUG = true;
    private static final int EVENT_MARK_AS_GAME = 4;
    private static final int EVENT_MOVE_BACKGROUND = 2;
    private static final int EVENT_MOVE_FRONT = 1;
    private static final int EVENT_REPLACE_FRONT = 3;
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    static final int INIT_CFG_DELAY = 10000;
    private static final String KEY_SMART_NOTIFICATION_SWITCH = "smart_notification_switch";
    private static final String NOTIFICATION_ACTION_ALLOW = "com.huawei.notificationmanager.notification.allow";
    private static final String NOTIFICATION_ACTION_ALLOW_FORAPK = "com.huawei.notificationmanager.notification.allow.forAPK";
    private static final String NOTIFICATION_ACTION_REFUSE = "com.huawei.notificationmanager.notification.refuse";
    private static final String NOTIFICATION_ACTION_REFUSE_FORAPK = "com.huawei.notificationmanager.notification.refuse.forAPK";
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    static final int NOTIFICATION_CFG_REMIND = 0;
    private static final int NOTIFICATION_PRIORITY_MULTIPLIER = 10;
    private static final String[] NOTIFICATION_WHITE_APPS_PACKAGE = new String[]{"com.huawei.intelligent"};
    private static final int OPERATOR_QUERY = 1;
    private static final String PACKAGE_NAME_HSM = "com.huawei.systemmanager";
    private static final String RIDEMODE_NOTIFICATION_WHITE_LIST = "com.huawei.ridemode,com.huawei.sos,com.android.phone,com.android.server.telecom,com.android.incallui,com.android.deskclock,com.android.cellbroadcastreceiver";
    private static final String RULE_NAME = "ongoingAndNormalRules";
    private static final String SERVER_PAKAGE_NAME = "com.huawei.recsys";
    private static final int SMCS_NOTIFICATION_GET_NOTI = 1;
    static final String TAG = "HwNotificationManagerService";
    private static final String TYPE_COUNSELING_MESSAGE = "102";
    private static final String TYPE_IGNORE = "0";
    private static final String TYPE_INFORMATION = "3";
    private static final String TYPE_MUSIC = "103";
    private static final String TYPE_PROMOTION = "2";
    private static final String TYPE_TOOLS = "107";
    private static final String WHITELIST_LABLE = "200";
    private static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static int sRemoveableFlag;
    private static int sUpdateRemovableFlag;
    final Uri URI_NOTIFICATION_CFG;
    private BatteryManagerInternal mBatteryManagerInternal;
    private List<String> mBtwList;
    DBContentObserver mCfgDBObserver;
    Map<String, Integer> mCfgMap;
    private ContentObserver mContentObserver;
    Context mContext;
    private HwCustZenModeHelper mCust;
    private BroadcastReceiver mHangButtonReceiver;
    private HwGameObserver mHwGameObserver;
    Handler mHwHandler;
    private final HwNotificationDelegate mHwNotificationDelegate;
    private HwRecSysAidlInterface mHwRecSysAidlInterface;
    private Object mLock;
    private ArrayList<NotificationContentViewRecord> mOriginContentViews;
    private Handler mRecHandler;
    private HandlerThread mRecHandlerThread;
    private ArrayMap<String, String> mRecognizeMap;
    private ServiceConnection mServiceConnection;
    private ArrayMap<Integer, Boolean> mSmartNtfSwitchMap;

    private class DBContentObserver extends ContentObserver {
        public DBContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            Slog.v(HwNotificationManagerService.TAG, "Notification db cfg changed");
            HwNotificationManagerService.this.mHwHandler.post(new HwCfgLoadingRunnable(HwNotificationManagerService.this, null));
        }
    }

    private class HwCfgLoadingRunnable implements Runnable {
        /* synthetic */ HwCfgLoadingRunnable(HwNotificationManagerService this$0, HwCfgLoadingRunnable -this1) {
            this();
        }

        private HwCfgLoadingRunnable() {
        }

        public void run() {
            new Thread("HwCfgLoading") {
                public void run() {
                    HwCfgLoadingRunnable.this.load();
                }
            }.start();
        }

        private void load() {
            Context context;
            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Starts ");
            Cursor cursor = null;
            try {
                context = HwNotificationManagerService.this.mContext.createPackageContextAsUser(HwNotificationManagerService.PACKAGE_NAME_HSM, 0, new UserHandle(ActivityManager.getCurrentUser()));
            } catch (Exception e) {
                Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to convert context");
                context = null;
            }
            if (context != null) {
                try {
                    cursor = context.getContentResolver().query(HwNotificationManagerService.this.URI_NOTIFICATION_CFG, null, null, null, null);
                    if (cursor == null) {
                        Slog.w(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Fail to get cfg from DB");
                        if (cursor != null) {
                            cursor.close();
                        }
                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                        return;
                    }
                    HashMap<String, Integer> tempMap = new HashMap();
                    ArrayList<NotificationSysMgrCfg> tempSysMgrCfgList = new ArrayList();
                    if (cursor.getCount() > 0) {
                        int nPkgColIndex = cursor.getColumnIndex(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
                        int nCfgColIndex = cursor.getColumnIndex("sound_vibrate");
                        int nCidColIndex = cursor.getColumnIndex("channelid");
                        int nLockscreenColIndex = cursor.getColumnIndex("lockscreencfg");
                        int nImportaceColIndex = cursor.getColumnIndex("channelimportance");
                        int nBypassdndColIndex = cursor.getColumnIndex("channelbypassdnd");
                        int nIconDadgeColIndex = cursor.getColumnIndex("channeliconbadge");
                        while (cursor.moveToNext()) {
                            String pkgName = cursor.getString(nPkgColIndex);
                            if (!TextUtils.isEmpty(pkgName)) {
                                int cfg = cursor.getInt(nCfgColIndex);
                                String channelId = cursor.getString(nCidColIndex);
                                String key = HwNotificationManagerService.this.pkgAndCidKey(pkgName, channelId);
                                if ("miscellaneous".equals(channelId)) {
                                    NotificationSysMgrCfg mgrCfg = new NotificationSysMgrCfg();
                                    mgrCfg.smc_userId = ActivityManager.getCurrentUser();
                                    mgrCfg.smc_packageName = pkgName;
                                    mgrCfg.smc_visilibity = cursor.getInt(nLockscreenColIndex);
                                    mgrCfg.smc_importance = cursor.getInt(nImportaceColIndex);
                                    mgrCfg.smc_bypassDND = cursor.getInt(nBypassdndColIndex);
                                    mgrCfg.smc_iconBadge = cursor.getInt(nIconDadgeColIndex);
                                    tempSysMgrCfgList.add(mgrCfg);
                                }
                                tempMap.put(key, Integer.valueOf(cfg));
                            }
                        }
                    }
                    HwNotificationManagerService.this.setSysMgrCfgMap(tempSysMgrCfgList);
                    synchronized (HwNotificationManagerService.this.mCfgMap) {
                        HwNotificationManagerService.this.mCfgMap.clear();
                        HwNotificationManagerService.this.mCfgMap.putAll(tempMap);
                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: get cfg size:" + HwNotificationManagerService.this.mCfgMap.size());
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (RuntimeException e2) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : RuntimeException ", e2);
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (Exception e3) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception ", e3);
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                }
            }
        }
    }

    private class HwGameObserver extends Stub {
        /* synthetic */ HwGameObserver(HwNotificationManagerService this$0, HwGameObserver -this1) {
            this();
        }

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

        NotificationContentViewRecord(String pkg, int uid, String tag, int id, int userId, RemoteViews rOldBigContentView) {
            this.pkg = pkg;
            this.tag = tag;
            this.uid = uid;
            this.id = id;
            this.userId = userId;
            this.rOldBigContentView = rOldBigContentView;
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
        return (StatusBarManagerService) ServiceManager.getService("statusbar");
    }

    public void onStart() {
        super.onStart();
        this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
    }

    public HwNotificationManagerService(Context context, StatusBarManagerService statusBar, LightsService lights) {
        super(context);
        this.mHwHandler = null;
        this.mCfgDBObserver = null;
        this.mCfgMap = new HashMap();
        this.URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
        this.mLock = new Object();
        this.mRecognizeMap = new ArrayMap();
        this.mSmartNtfSwitchMap = new ArrayMap();
        this.mCust = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        this.mHwGameObserver = null;
        this.mBtwList = new ArrayList();
        this.mBtwList.add("2");
        this.mBtwList.add("3");
        this.mBtwList.add(TYPE_COUNSELING_MESSAGE);
        this.mBtwList.add(TYPE_TOOLS);
        this.mHwNotificationDelegate = new HwNotificationDelegate() {
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, 0, true, userId, 2, null);
            }
        };
        this.mHangButtonReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    String pkg;
                    int uid;
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        pkg = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (action != null && action.equals(HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE)) {
                        pkg = intent.getStringExtra(AwareIntelligentRecg.CMP_PKGNAME);
                        uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            synchronized (HwNotificationManagerService.this.mOriginContentViews) {
                                Iterator<NotificationContentViewRecord> itor = HwNotificationManagerService.this.mOriginContentViews.iterator();
                                while (itor.hasNext()) {
                                    NotificationContentViewRecord cvr = (NotificationContentViewRecord) itor.next();
                                    if (cvr.pkg.equals(pkg) && cvr.uid == uid) {
                                        itor.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        this.mOriginContentViews = new ArrayList();
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HwNotificationManagerService.TAG, "onServiceConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
                synchronized (HwNotificationManagerService.this.mLock) {
                    HwNotificationManagerService.this.mLock.notifyAll();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(HwNotificationManagerService.TAG, "onServiceDisConnected");
                HwNotificationManagerService.this.mHwRecSysAidlInterface = null;
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
                        boolean enable = System.getIntForUser(HwNotificationManagerService.this.mContext.getContentResolver(), HwNotificationManagerService.KEY_SMART_NOTIFICATION_SWITCH, HwNotificationManagerService.mIsChina ? 1 : 0, userId) == 1;
                        boolean allUserClose = true;
                        synchronized (HwNotificationManagerService.this.mSmartNtfSwitchMap) {
                            HwNotificationManagerService.this.mSmartNtfSwitchMap.put(Integer.valueOf(userId), Boolean.valueOf(enable));
                            if (HwNotificationManagerService.this.mSmartNtfSwitchMap.containsValue(Boolean.valueOf(true))) {
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
        this.mHwHandler = null;
        this.mCfgDBObserver = null;
        this.mCfgMap = new HashMap();
        this.URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
        this.mLock = new Object();
        this.mRecognizeMap = new ArrayMap();
        this.mSmartNtfSwitchMap = new ArrayMap();
        this.mCust = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        this.mHwGameObserver = null;
        this.mBtwList = new ArrayList();
        this.mBtwList.add("2");
        this.mBtwList.add("3");
        this.mBtwList.add(TYPE_COUNSELING_MESSAGE);
        this.mBtwList.add(TYPE_TOOLS);
        this.mHwNotificationDelegate = /* anonymous class already generated */;
        this.mHangButtonReceiver = /* anonymous class already generated */;
        this.mOriginContentViews = new ArrayList();
        this.mServiceConnection = /* anonymous class already generated */;
        this.mContentObserver = /* anonymous class already generated */;
        this.mContext = context;
        init();
        registerHwGameObserver();
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_ACTION_ALLOW);
        filter.addAction(NOTIFICATION_ACTION_REFUSE);
        this.mContext.registerReceiverAsUser(this.mHangButtonReceiver, UserHandle.ALL, filter, "com.android.permission.system_manager_interface", null);
        StatusBarManagerService sb = getStatusBarManagerService();
        if (sb instanceof HwStatusBarManagerService) {
            ((HwStatusBarManagerService) sb).setHwNotificationDelegate(this.mHwNotificationDelegate);
        }
        this.mHwHandler = new Handler();
        this.mHwHandler.postDelayed(new HwCfgLoadingRunnable(this, null), 10000);
        this.mCfgDBObserver = new DBContentObserver();
        this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        this.mRecHandlerThread = new HandlerThread("notification manager");
        this.mRecHandlerThread.start();
        this.mRecHandler = new Handler(this.mRecHandlerThread.getLooper());
        try {
            ContentResolver cr = this.mContext.getContentResolver();
            if (cr != null) {
                cr.registerContentObserver(System.getUriFor(KEY_SMART_NOTIFICATION_SWITCH), false, this.mContentObserver, -1);
            }
            this.mContentObserver.onChange(true);
        } catch (Exception e) {
            Log.w(TAG, "init failed", e);
        }
    }

    protected void handleGetNotifications(Parcel data, Parcel reply) {
        HashSet<String> notificationPkgs = new HashSet();
        if (this.mContext.checkCallingPermission("huawei.permission.IBINDER_NOTIFICATION_SERVICE") != 0) {
            Slog.e(TAG, "NotificationManagerService.handleGetNotifications: permissin deny");
            return;
        }
        getNotificationPkgs_hwHsm(notificationPkgs);
        Slog.v(TAG, "NotificationManagerService.handleGetNotifications: got " + notificationPkgs.size() + " pkgs");
        reply.writeInt(notificationPkgs.size());
        Iterator<String> it = notificationPkgs.iterator();
        while (it.hasNext()) {
            String pkg = (String) it.next();
            Slog.v(TAG, "NotificationManagerService.handleGetNotifications: reply " + pkg);
            reply.writeString(pkg);
        }
    }

    void getNotificationPkgs_hwHsm(HashSet<String> notificationPkgs) {
        if (notificationPkgs != null) {
            if (notificationPkgs.size() > 0) {
                notificationPkgs.clear();
            }
            synchronized (this.mNotificationList) {
                int N = this.mNotificationList.size();
                if (N == 0) {
                    return;
                }
                for (int i = 0; i < N; i++) {
                    NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                    if (r != null) {
                        String sPkg = r.sbn.getPackageName();
                        if (sPkg != null && sPkg.length() > 0) {
                            notificationPkgs.add(sPkg);
                        }
                    }
                }
            }
        }
    }

    private int indexOfContentViewLocked(String pkg, String tag, int id, int userId) {
        synchronized (this.mOriginContentViews) {
            ArrayList<NotificationContentViewRecord> list = this.mOriginContentViews;
            int len = list.size();
            for (int i = 0; i < len; i++) {
                NotificationContentViewRecord r = (NotificationContentViewRecord) list.get(i);
                if ((userId == -1 || r.userId == -1 || r.userId == userId) && r.id == id) {
                    if (tag == null) {
                        if (r.tag != null) {
                            continue;
                        }
                    } else if (!tag.equals(r.tag)) {
                        continue;
                    }
                    if (r.pkg.equals(pkg)) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    private void recoverNotificationContentView(String pkg, int uid) {
        synchronized (this.mOriginContentViews) {
            Iterator<NotificationContentViewRecord> itor = this.mOriginContentViews.iterator();
            while (itor.hasNext()) {
                NotificationContentViewRecord cvr = (NotificationContentViewRecord) itor.next();
                if (cvr.pkg.equals(pkg)) {
                    NotificationRecord r = findNotificationByListLocked(this.mNotificationList, cvr.pkg, cvr.tag, cvr.id, cvr.userId);
                    if (r != null) {
                        r.sbn.getNotification().bigContentView = cvr.rOldBigContentView;
                        Slog.d(TAG, "revertNotificationView enqueueNotificationInternal pkg=" + pkg + " id=" + r.sbn.getId() + " userId=" + r.sbn.getUserId());
                        r.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, true);
                        enqueueNotificationInternal(pkg, r.sbn.getOpPkg(), r.sbn.getUid(), r.sbn.getInitialPid(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getNotification(), r.sbn.getUserId());
                        itor.remove();
                    } else {
                        Slog.w(TAG, "Notification can't find in NotificationRecords");
                    }
                }
            }
        }
    }

    protected boolean isHwSoundAllow(String pkg, String channelId, int userId) {
        Integer cfg;
        String key = pkgAndCidKey(pkg, channelId);
        synchronized (this.mCfgMap) {
            cfg = (Integer) this.mCfgMap.get(key);
        }
        Slog.v(TAG, "isHwSoundAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", cfg=" + cfg);
        if (cfg == null || (cfg.intValue() & 1) != 0) {
            return true;
        }
        return false;
    }

    protected boolean isHwVibrateAllow(String pkg, String channelId, int userId) {
        Integer cfg;
        String key = pkgAndCidKey(pkg, channelId);
        synchronized (this.mCfgMap) {
            cfg = (Integer) this.mCfgMap.get(key);
        }
        Slog.v(TAG, "isHwVibrateAllow pkg=" + pkg + ", channelId=" + channelId + ", userId=" + userId + ", cfg=" + cfg);
        if (cfg == null || (cfg.intValue() & 2) != 0) {
            return true;
        }
        return false;
    }

    private String pkgAndCidKey(String pkg, String channelId) {
        StringBuilder key = new StringBuilder();
        key.append(pkg);
        key.append("_");
        key.append(channelId);
        return key.toString();
    }

    @Deprecated
    protected int modifyScoreBySM(String pkg, int callingUid, int origScore) {
        int score = origScore;
        return origScore;
    }

    protected void detectNotifyBySM(int callingUid, String pkg, Notification notification) {
        Intent intent = new Intent("com.huawei.notificationmanager.detectnotify");
        intent.putExtra("callerUid", callingUid);
        intent.putExtra(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, pkg);
        Bundle bundle = new Bundle();
        bundle.putParcelable("sendNotify", notification);
        intent.putExtra("notifyBundle", bundle);
    }

    protected void hwEnqueueNotificationWithTag(String pkg, int uid, NotificationRecord nr) {
        if (nr.sbn.getNotification().extras.getBoolean(CONTENTVIEW_REVERTING_FLAG, false)) {
            nr.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, false);
        }
    }

    protected boolean inNonDisturbMode(String pkg) {
        if (pkg == null) {
            return false;
        }
        return isWhiteApp(pkg);
    }

    private boolean isWhiteApp(String pkg) {
        if (this.mCust != null && this.mCust.getWhiteApps(this.mContext) != null) {
            return Arrays.asList(this.mCust.getWhiteApps(this.mContext)).contains(pkg);
        }
        String[] DEFAULT_WHITEAPP = new String[]{"com.android.mms", "com.android.contacts"};
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

    protected boolean isImportantNotification(String pkg, Notification notification) {
        if (notification == null || notification.priority < 3) {
            return false;
        }
        if ((pkg.equals("com.android.phone") || pkg.equals("com.android.mms") || pkg.equals("com.android.contacts") || pkg.equals("com.android.server.telecom")) && notification.priority < 7) {
            return true;
        }
        return false;
    }

    protected boolean isMmsNotificationEnable(String pkg) {
        if (pkg == null || (!pkg.equalsIgnoreCase("com.android.mms") && !pkg.equalsIgnoreCase("com.android.contacts"))) {
            return false;
        }
        return true;
    }

    protected void hwCancelNotification(String pkg, String tag, int id, int userId) {
        synchronized (this.mOriginContentViews) {
            int indexView = indexOfContentViewLocked(pkg, tag, id, userId);
            if (indexView >= 0) {
                this.mOriginContentViews.remove(indexView);
                Slog.d(TAG, "hwCancelNotification: pkg = " + pkg + ", id = " + id);
            }
        }
    }

    protected void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
        this.mBatteryManagerInternal.updateBatteryLight(enable, ledOnMS, ledOffMS);
    }

    protected void handleUserSwitchEvents(int userId) {
        if (this.mCfgDBObserver != null) {
            this.mHwHandler.post(new HwCfgLoadingRunnable(this, null));
            this.mContext.getContentResolver().unregisterContentObserver(this.mCfgDBObserver);
            this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        }
    }

    protected void stopPlaySound() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    protected boolean isAFWUserId(int userId) {
        boolean temp = false;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManagerService.getInstance().getUserInfo(userId);
            if (userInfo != null) {
                temp = !userInfo.isManagedProfile() ? userInfo.isClonedProfile() : true;
            }
            Binder.restoreCallingIdentity(token);
        } catch (Exception e) {
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        return temp;
    }

    protected void addHwExtraForNotification(Notification notification, String pkg, int pid) {
        if (isIntentProtectedApp(pkg)) {
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

    protected int getNCTargetAppUid(String opPkg, String pkg, int defaultUid, Notification notification) {
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

    protected String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
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

    protected boolean isBlockRideModeNotification(String pkg) {
        return (HWRIDEMODE_FEATURE_SUPPORTED && SystemProperties.getBoolean("sys.ride_mode", false)) ? RIDEMODE_NOTIFICATION_WHITE_LIST.contains(pkg) ^ 1 : false;
    }

    public void reportToIAware(String pkg, int uid, int nid, boolean added) {
        if (pkg != null && !pkg.isEmpty()) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkg);
                bundleArgs.putInt("tgtUid", uid);
                bundleArgs.putInt("notification_id", nid);
                bundleArgs.putInt("relationType", added ? 20 : 21);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    protected boolean isFromPinNotification(Notification notification, String pkg) {
        return isPkgInWhiteApp(pkg) ? notification.extras.getBoolean("pin_notification") : false;
    }

    private boolean isPkgInWhiteApp(String pkg) {
        for (String s : NOTIFICATION_WHITE_APPS_PACKAGE) {
            if (s.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isGameRunningForeground() {
        HwActivityManagerService mAms = HwActivityManagerService.self();
        if (mAms != null) {
            return mAms.isGameDndOn();
        }
        Slog.e(TAG, "Canot obtain HwActivityManagerService , mAms is null");
        return false;
    }

    protected boolean isGameDndSwitchOn() {
        String GAME_DND_MODE = "game_dnd_mode";
        if (Secure.getInt(this.mContext.getContentResolver(), "game_dnd_mode", 2) == 1) {
            return true;
        }
        return false;
    }

    private void registerHwGameObserver() {
        if (this.mHwGameObserver == null) {
            this.mHwGameObserver = new HwGameObserver(this, null);
        }
        HwActivityManagerService.self().registerGameObserver(this.mHwGameObserver);
    }

    private void updateNotificationInGameMode() {
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    public void bindRecSys() {
        this.mRecHandler.post(new Runnable() {
            public void run() {
                HwNotificationManagerService.this.bind();
            }
        });
    }

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

    private void unBind() {
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

    public void recognize(String tag, int id, Notification notification, UserHandle user, String pkg, int uid, int pid) {
        if (!mIsChina) {
            Log.d(TAG, "recognize: not in china");
        } else if (isFeartureDisable()) {
            Log.d(TAG, "recognize: feature is disabled");
        } else {
            Log.d(TAG, "recognize: tag=" + tag + ", id=" + id + ", user=" + user + ", pkg=" + pkg + ", uid=" + uid + ", callingPid=" + pid);
            String key = pkg + pid;
            if (this.mRecognizeMap != null && this.mRecognizeMap.containsKey(key) && this.mRecognizeMap.get(key) != null && ((String) this.mRecognizeMap.get(key)).equals("0")) {
                Log.d(TAG, "Return ! recognize the app not in list : " + pkg);
            } else if (isSystemApp(pkg, uid)) {
                Log.d(TAG, "recognize: system app");
                this.mRecognizeMap.put(key, "0");
            } else {
                if (this.mHwRecSysAidlInterface != null) {
                    try {
                        String type = this.mHwRecSysAidlInterface.doNotificationCollect(new StatusBarNotification(pkg, pkg, id, tag, uid, pid, notification, user, null, System.currentTimeMillis()));
                        if (type != null) {
                            if (type.equals("0")) {
                                this.mRecognizeMap.put(key, type);
                                Log.d(TAG, "recognize: just ignore type : " + type);
                                return;
                            }
                            if (type.equals(TYPE_MUSIC)) {
                                notification.extras.putString("hw_type", "type_music");
                            } else {
                                notification.extras.putString("hw_type", type);
                            }
                            notification.extras.putBoolean("hw_btw", this.mBtwList.contains(type));
                            Log.i(TAG, "doNotificationCollect: pkg=" + pkg + ", uid=" + uid + ", hw_type=" + type + ", hw_btw=" + this.mBtwList.contains(type));
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, "doNotificationCollect failed", e);
                    }
                }
            }
        }
    }

    private boolean isSystemApp(String pkg, int uid) {
        boolean z = false;
        if ("android".equals(pkg)) {
            return true;
        }
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getUserId(uid));
            if (ai == null) {
                return false;
            }
            if (ai.isSystemApp()) {
                z = isRemoveAblePreInstall(ai, pkg) ^ 1;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isRemoveAblePreInstall(ApplicationInfo ai, String pkg) {
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(ai)).intValue();
            return ((sRemoveableFlag & hwFlags) == 0 && (sUpdateRemovableFlag & hwFlags) == 0) ? false : true;
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
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return 0;
    }

    private boolean isFeartureDisable() {
        long callingId = Binder.clearCallingIdentity();
        boolean isDisable = false;
        try {
            int userId = ActivityManager.getCurrentUser();
            synchronized (this.mSmartNtfSwitchMap) {
                isDisable = this.mSmartNtfSwitchMap.containsKey(Integer.valueOf(userId)) ? ((Boolean) this.mSmartNtfSwitchMap.get(Integer.valueOf(userId))).booleanValue() ^ 1 : false;
            }
            return isDisable;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    protected boolean isNotificationDisable() {
        return HwDeviceManager.disallowOp(102);
    }
}
