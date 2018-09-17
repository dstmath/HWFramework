package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import android.widget.RemoteViews;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.lights.LightsService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pm.UserManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.HwStatusBarManagerService.HwNotificationDelegate;
import com.android.server.statusbar.StatusBarManagerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class HwNotificationManagerService extends NotificationManagerService {
    private static final String CLONE_APP_LIST = "clone_app_list";
    private static final String CONTENTVIEW_REVERTING_FLAG = "HW_CONTENTVIEW_REVERTING_FLAG";
    private static final boolean DEBUG = false;
    static final int INIT_CFG_DELAY = 10000;
    private static final String NOTIFICATION_ACTION_ALLOW = "com.huawei.notificationmanager.notification.allow";
    private static final String NOTIFICATION_ACTION_ALLOW_FORAPK = "com.huawei.notificationmanager.notification.allow.forAPK";
    private static final String NOTIFICATION_ACTION_REFUSE = "com.huawei.notificationmanager.notification.refuse";
    private static final String NOTIFICATION_ACTION_REFUSE_FORAPK = "com.huawei.notificationmanager.notification.refuse.forAPK";
    private static final String NOTIFICATION_CENTER_ORIGIN_PKG = "hw_origin_sender_package_name";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    static final int NOTIFICATION_CFG_REMIND = 0;
    private static final int NOTIFICATION_PRIORITY_MULTIPLIER = 10;
    private static final String PACKAGE_NAME_HSM = "com.huawei.systemmanager";
    private static final int REASON_CLONED_APP_DELETED = 14;
    private static final int SMCS_NOTIFICATION_GET_NOTI = 1;
    static final String TAG = "HwNotificationManagerService";
    final Uri URI_NOTIFICATION_CFG;
    private BatteryManagerInternal mBatteryManagerInternal;
    DBContentObserver mCfgDBObserver;
    Map<String, Integer> mCfgMap;
    private CloneAppObserver mCloneAppObserver;
    Context mContext;
    private BroadcastReceiver mHangButtonReceiver;
    Handler mHwHandler;
    private final HwNotificationDelegate mHwNotificationDelegate;
    private ArrayList<NotificationContentViewRecord> mOriginContentViews;

    private final class CloneAppObserver extends ContentObserver {
        private final Uri CLONE_APP_LIST_URI;
        private String mCloneAppList;

        CloneAppObserver(Handler handler) {
            super(handler);
            this.CLONE_APP_LIST_URI = Secure.getUriFor(HwNotificationManagerService.CLONE_APP_LIST);
            ContentResolver resolver = HwNotificationManagerService.this.getContext().getContentResolver();
            resolver.registerContentObserver(this.CLONE_APP_LIST_URI, HwNotificationManagerService.DEBUG, this, HwNotificationManagerService.NOTIFICATION_CFG_REMIND);
            this.mCloneAppList = Secure.getStringForUser(resolver, HwNotificationManagerService.CLONE_APP_LIST, HwNotificationManagerService.NOTIFICATION_CFG_REMIND);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = HwNotificationManagerService.this.getContext().getContentResolver();
            if (this.CLONE_APP_LIST_URI.equals(uri)) {
                String cloneAppList = Secure.getStringForUser(resolver, HwNotificationManagerService.CLONE_APP_LIST, HwNotificationManagerService.NOTIFICATION_CFG_REMIND);
                if (!(this.mCloneAppList == null || this.mCloneAppList.equals(cloneAppList))) {
                    String[] packages = this.mCloneAppList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    int length = packages.length;
                    for (int i = HwNotificationManagerService.NOTIFICATION_CFG_REMIND; i < length; i += HwNotificationManagerService.SMCS_NOTIFICATION_GET_NOTI) {
                        String pkg = packages[i];
                        if (!(cloneAppList + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(pkg + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                            HwNotificationManagerService.this.cancelAllNotificationsInt(Process.myUid(), Process.myPid(), pkg, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, true, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, HwNotificationManagerService.REASON_CLONED_APP_DELETED, null);
                        }
                    }
                }
                this.mCloneAppList = cloneAppList;
            }
        }
    }

    private class DBContentObserver extends ContentObserver {
        public DBContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            Slog.v(HwNotificationManagerService.TAG, "Notification db cfg changed");
            HwNotificationManagerService.this.mHwHandler.post(new HwCfgLoadingRunnable(null));
        }
    }

    private class HwCfgLoadingRunnable implements Runnable {

        /* renamed from: com.android.server.notification.HwNotificationManagerService.HwCfgLoadingRunnable.1 */
        class AnonymousClass1 extends Thread {
            AnonymousClass1(String $anonymous0) {
                super($anonymous0);
            }

            public void run() {
                HwCfgLoadingRunnable.this.load();
            }
        }

        private HwCfgLoadingRunnable() {
        }

        public void run() {
            new AnonymousClass1("HwCfgLoading").start();
        }

        private void load() {
            Context context;
            Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Starts ");
            Cursor cursor = null;
            try {
                context = HwNotificationManagerService.this.mContext.createPackageContextAsUser(HwNotificationManagerService.PACKAGE_NAME_HSM, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, new UserHandle(ActivityManager.getCurrentUser()));
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
                    if (cursor.getCount() > 0) {
                        int nPkgColIndex = cursor.getColumnIndex(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
                        int nCfgColIndex = cursor.getColumnIndex("sound_vibrate");
                        while (cursor.moveToNext()) {
                            String pkgName = cursor.getString(nPkgColIndex);
                            if (!TextUtils.isEmpty(pkgName)) {
                                tempMap.put(pkgName, Integer.valueOf(cursor.getInt(nCfgColIndex)));
                            }
                        }
                    }
                    synchronized (HwNotificationManagerService.this.mCfgMap) {
                        HwNotificationManagerService.this.mCfgMap.clear();
                        HwNotificationManagerService.this.mCfgMap.putAll(tempMap);
                        Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: get cfg size:" + HwNotificationManagerService.this.mCfgMap.size());
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    Slog.v(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable: Ends. ");
                } catch (Exception e2) {
                    Slog.e(HwNotificationManagerService.TAG, "HwCfgLoadingRunnable : Exception ", e2);
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

    private StatusBarManagerService getStatusBarManagerService() {
        return (StatusBarManagerService) ServiceManager.getService("statusbar");
    }

    public void onStart() {
        super.onStart();
        this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            this.mCloneAppObserver = new CloneAppObserver(this.mHwHandler);
        }
    }

    public HwNotificationManagerService(Context context, StatusBarManagerService statusBar, LightsService lights) {
        super(context);
        this.mHwHandler = null;
        this.mCfgDBObserver = null;
        this.mCfgMap = new HashMap();
        this.URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
        this.mHwNotificationDelegate = new HwNotificationDelegate() {
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, true, userId, 2, null);
            }
        };
        this.mHangButtonReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    String pkg;
                    int uid;
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        pkg = intent.getStringExtra("pkgName");
                        uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (action != null && action.equals(HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE)) {
                        pkg = intent.getStringExtra("pkgName");
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
        this.mContext = context;
    }

    public HwNotificationManagerService(Context context) {
        super(context);
        this.mHwHandler = null;
        this.mCfgDBObserver = null;
        this.mCfgMap = new HashMap();
        this.URI_NOTIFICATION_CFG = Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg");
        this.mHwNotificationDelegate = new HwNotificationDelegate() {
            public void onNotificationResidentClear(String pkg, String tag, int id, int userId) {
                HwNotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, HwNotificationManagerService.NOTIFICATION_CFG_REMIND, true, userId, 2, null);
            }
        };
        this.mHangButtonReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    String pkg;
                    int uid;
                    if (HwNotificationManagerService.NOTIFICATION_ACTION_ALLOW.equals(action)) {
                        pkg = intent.getStringExtra("pkgName");
                        uid = intent.getIntExtra("uid", -1);
                        if (!(pkg == null || uid == -1)) {
                            HwNotificationManagerService.this.recoverNotificationContentView(pkg, uid);
                        }
                    } else if (action != null && action.equals(HwNotificationManagerService.NOTIFICATION_ACTION_REFUSE)) {
                        pkg = intent.getStringExtra("pkgName");
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
        this.mContext = context;
        init();
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
        this.mHwHandler.postDelayed(new HwCfgLoadingRunnable(), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mCfgDBObserver = new DBContentObserver();
        this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
    }

    protected void handleGetNotifications(Parcel data, Parcel reply) {
        HashSet<String> notificationPkgs = new HashSet();
        if (this.mContext.checkCallingPermission("huawei.permission.IBINDER_NOTIFICATION_SERVICE") == 0) {
            getNotificationPkgs_hwHsm(notificationPkgs);
            reply.writeInt(notificationPkgs.size());
            Iterator<String> it = notificationPkgs.iterator();
            while (it.hasNext()) {
                reply.writeString((String) it.next());
            }
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
                for (int i = NOTIFICATION_CFG_REMIND; i < N; i += SMCS_NOTIFICATION_GET_NOTI) {
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
            for (int i = NOTIFICATION_CFG_REMIND; i < len; i += SMCS_NOTIFICATION_GET_NOTI) {
                NotificationContentViewRecord r = (NotificationContentViewRecord) list.get(i);
                if ((userId == -1 || r.userId == -1 || r.userId == userId) && r.id == id) {
                    if (tag == null) {
                        if (r.tag != null) {
                            continue;
                        }
                    } else if (!tag.equals(r.tag)) {
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
                    int indexNoti = indexOfNotificationLocked(cvr.pkg, cvr.tag, cvr.id, cvr.userId);
                    if (indexNoti >= 0) {
                        NotificationRecord r = (NotificationRecord) this.mNotificationList.get(indexNoti);
                        r.sbn.getNotification().bigContentView = cvr.rOldBigContentView;
                        int[] idOut = new int[SMCS_NOTIFICATION_GET_NOTI];
                        Slog.d(TAG, "revertNotificationView enqueueNotificationInternal pkg=" + pkg + " id=" + r.sbn.getId() + " userId=" + r.sbn.getUserId());
                        r.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, true);
                        enqueueNotificationInternal(pkg, r.sbn.getOpPkg(), r.sbn.getUid(), r.sbn.getInitialPid(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getNotification(), idOut, r.sbn.getUserId());
                        itor.remove();
                    } else {
                        Slog.w(TAG, "Notification can't find in NotificationRecords");
                    }
                }
            }
        }
    }

    protected boolean isHwSoundAllow(String pkg, int userId) {
        Integer cfg;
        synchronized (this.mCfgMap) {
            cfg = (Integer) this.mCfgMap.get(pkg);
        }
        Slog.v(TAG, "isHwSoundAllow pkg=" + pkg + ", userId=" + userId + ", cfg=" + cfg);
        if (cfg == null || (cfg.intValue() & SMCS_NOTIFICATION_GET_NOTI) != 0) {
            return true;
        }
        return DEBUG;
    }

    protected boolean isHwVibrateAllow(String pkg, int userId) {
        Integer cfg;
        synchronized (this.mCfgMap) {
            cfg = (Integer) this.mCfgMap.get(pkg);
        }
        Slog.v(TAG, "isHwVibrateAllow pkg=" + pkg + ", userId=" + userId + ", cfg=" + cfg);
        if (cfg == null || (cfg.intValue() & 2) != 0) {
            return true;
        }
        return DEBUG;
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
        if (nr.sbn.getNotification().extras.getBoolean(CONTENTVIEW_REVERTING_FLAG, DEBUG)) {
            nr.sbn.getNotification().extras.putBoolean(CONTENTVIEW_REVERTING_FLAG, DEBUG);
        }
    }

    protected boolean inNonDisturbMode(String pkg) {
        if (pkg == null) {
            return DEBUG;
        }
        return isWhiteApp(pkg);
    }

    private boolean isWhiteApp(String pkg) {
        String NODISTURB_WHITEAPP = "nodisturb_whiteapps";
        String DEFAULT_WHITEAPP = "com.android.mms;com.android.contacts;";
        if (this.mContext == null) {
            return DEBUG;
        }
        String whiteApps = System.getString(this.mContext.getContentResolver(), "nodisturb_whiteapps");
        if (whiteApps == null) {
            whiteApps = "com.android.mms;com.android.contacts;";
        }
        return whiteApps.contains(pkg);
    }

    protected boolean isImportantNotification(String pkg, Notification notification) {
        if (notification == null || notification.priority < 3) {
            return DEBUG;
        }
        if ((pkg.equals("com.android.phone") || pkg.equals("com.android.mms") || pkg.equals("com.android.contacts") || pkg.equals("com.android.server.telecom")) && notification.priority < 7) {
            return true;
        }
        return DEBUG;
    }

    protected boolean isMmsNotificationEnable(String pkg) {
        if (pkg == null || (!pkg.equalsIgnoreCase("com.android.mms") && !pkg.equalsIgnoreCase("com.android.contacts"))) {
            return DEBUG;
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
            this.mHwHandler.post(new HwCfgLoadingRunnable());
            this.mContext.getContentResolver().unregisterContentObserver(this.mCfgDBObserver);
            this.mContext.getContentResolver().registerContentObserver(this.URI_NOTIFICATION_CFG, true, this.mCfgDBObserver, ActivityManager.getCurrentUser());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isAFWUserId(int userId) {
        boolean z = DEBUG;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManagerService.getInstance().getUserInfo(userId);
            if (userInfo != null) {
                z = userInfo.isManagedProfile();
            }
            Binder.restoreCallingIdentity(token);
        } catch (Exception e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
        return z;
    }

    protected boolean isClonedAppDeleted(int reason, String tag) {
        return (REASON_CLONED_APP_DELETED != reason || (tag != null && tag.contains("_hwclone"))) ? DEBUG : true;
    }

    protected String convertNotificationTag(String tag, int pid) {
        if (!((ActivityManagerService) ServiceManager.getService("activity")).isClonedProcess(pid)) {
            return tag;
        }
        return TextUtils.isEmpty(tag) ? "_hwclone" : tag + "_hwclone";
    }

    protected void handleNotificationForClone(Notification notification, int pid) {
        if (((ActivityManagerService) ServiceManager.getService("activity")).isClonedProcess(pid)) {
            notification.extras.putBoolean("com.huawei.isClonedProcess", true);
        }
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
        return DEBUG;
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
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(targetPkg, NOTIFICATION_CFG_REMIND, UserHandle.getCallingUserId());
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
            if (AppGlobals.getPackageManager().getApplicationInfo(pkg, NOTIFICATION_CFG_REMIND, UserHandle.getCallingUserId()) == null) {
                return DEBUG;
            }
            return true;
        } catch (RemoteException e) {
            return DEBUG;
        }
    }
}
