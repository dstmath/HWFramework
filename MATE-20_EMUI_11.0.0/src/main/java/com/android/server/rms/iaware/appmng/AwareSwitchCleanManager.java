package com.android.server.rms.iaware.appmng;

import android.app.ActivityManager;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.ComponentName;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.AwareCallback;
import com.huawei.android.app.IUserSwitchObserverEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.os.IRemoteCallbackEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.provider.SettingsExEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareSwitchCleanManager {
    private static final int CLEAN_DELAY_TIME = 5000;
    private static final int CONFIG_ITEM_ACTIVITY_NAME = 3;
    private static final int CONFIG_ITEM_LENGTH = 4;
    private static final int CONFIG_ITEM_NUM_STR = 1;
    private static final int CONFIG_ITEM_PROC_NAME = 0;
    private static final int CONFIG_ITEM_QUERY_STR = 2;
    private static final int CONFIG_NUM_LENGTH = 3;
    private static final int CONFIG_NUM_NEWACTIVITY = 2;
    private static final int CONFIG_NUM_SWITCHON = 1;
    private static final int CONFIG_NUM_TYPE = 0;
    private static final int HAS_ACTIVITY_FLAG = 1;
    private static final int INIT_DELAY_TIME = 0;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int INVALID_VALUE = -1;
    private static final int MAX_TASKS = 100;
    private static final int MSG_CLEAN = 2;
    private static final int MSG_INIT = 1;
    private static final int MSG_UPDATE = 3;
    private static final int SETTING_GLOBAL = 3;
    private static final String SETTING_PKG = "com.android.settings";
    private static final int SETTING_SECURE = 2;
    private static final int SETTING_SYSTEM = 1;
    private static final String SPLIT_VALUE_FOR_QUERYSTR = ":";
    private static final String SPLIT_VALUE_FOR_SWITCHAPPINFO = ";";
    private static final String SWITCH_KILL = "switch_apps";
    private static final String TAG = "AwareSwitchCleanManager";
    private static final int UPDATE_DELAY_TIME = 0;
    private static AwareSwitchCleanManager sInstance;
    private final ArrayList<String> mConfigItems = new ArrayList<>();
    private Context mContext = null;
    private int mCurSwitchUser = -2;
    private AtomicBoolean mFeatureEnable = new AtomicBoolean(false);
    private boolean mHasCleanApps = false;
    private final HwActivityManagerService mHwAms = HwActivityManagerService.self();
    private ContentObserver mSettingsObserver = new ContentObserver(null) {
        /* class com.android.server.rms.iaware.appmng.AwareSwitchCleanManager.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (AwareSwitchCleanManager.this.mFeatureEnable.get()) {
                AwareSwitchCleanManager.this.sendMessage(3, uri, 0);
            }
        }
    };
    private SwitchHandler mSwitchHandler = null;
    private ArrayMap<String, SwitchInfo> mSwitchInfos = new ArrayMap<>();
    private IUserSwitchObserverEx mUserSwitchObserver = new IUserSwitchObserverEx() {
        /* class com.android.server.rms.iaware.appmng.AwareSwitchCleanManager.AnonymousClass2 */

        public void onUserSwitching(int newUserId, IRemoteCallbackEx reply) {
            if (reply != null) {
                try {
                    reply.sendResult((Bundle) null);
                } catch (RemoteException e) {
                    AwareLog.e(AwareSwitchCleanManager.TAG, "RemoteException onUserSwitching");
                }
            }
        }

        public void onUserSwitchComplete(int newUserId) {
            AwareSwitchCleanManager.this.unregisterContentObserver();
            AwareLog.i(AwareSwitchCleanManager.TAG, "onUserSwitchComplete newUserId:" + newUserId);
            AwareSwitchCleanManager.this.sendMessage(1, Integer.valueOf(newUserId), 0);
        }
    };

    public static AwareSwitchCleanManager getInstance() {
        AwareSwitchCleanManager awareSwitchCleanManager;
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new AwareSwitchCleanManager();
            }
            awareSwitchCleanManager = sInstance;
        }
        return awareSwitchCleanManager;
    }

    public void enable(Context context) {
        if (context == null) {
            AwareLog.w(TAG, "context is null!");
            return;
        }
        this.mContext = context;
        if (initHandler()) {
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CLEAN, this.mContext);
            ArrayList<String> items = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_CLEAN.getDesc(), SWITCH_KILL);
            if (items == null) {
                AwareLog.w(TAG, "items is null!");
                return;
            }
            synchronized (this.mConfigItems) {
                this.mConfigItems.addAll(items);
            }
            sendMessage(1, Integer.valueOf(this.mCurSwitchUser), 0);
            initSwitchUser();
            this.mFeatureEnable.set(true);
        }
    }

    public void disable() {
        this.mFeatureEnable.set(false);
        unregisterContentObserver();
        deInitSwitchUser();
    }

    public void notifyFgActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        if (this.mFeatureEnable.get() && !foregroundActivities) {
            sendMessage(3, Integer.valueOf(pid), 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(int msgType, Object key, long delay) {
        SwitchHandler switchHandler = this.mSwitchHandler;
        if (switchHandler != null) {
            switchHandler.removeMessages(msgType, key);
            this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(msgType, key), delay);
        }
    }

    private boolean initHandler() {
        if (this.mSwitchHandler != null) {
            return true;
        }
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mSwitchHandler = new SwitchHandler(looper);
            return true;
        }
        Looper looper2 = BackgroundThreadEx.getLooper();
        if (looper2 == null) {
            AwareLog.w(TAG, "bgLooper is null!");
            return false;
        }
        this.mSwitchHandler = new SwitchHandler(looper2);
        return true;
    }

    /* access modifiers changed from: private */
    public class SwitchHandler extends Handler {
        public SwitchHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != 1) {
                if (i == 2) {
                    AwareSwitchCleanManager.this.cleanSwitchApps();
                } else if (i != 3) {
                    AwareLog.w(AwareSwitchCleanManager.TAG, "error msg what = " + msg.what);
                } else {
                    AwareSwitchCleanManager.this.updateSwitchStatus(msg.obj);
                }
            } else if (msg.obj instanceof Integer) {
                AwareSwitchCleanManager.this.mCurSwitchUser = ((Integer) msg.obj).intValue();
                AwareSwitchCleanManager.this.initAppSwitch();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwitchStatus(Object obj) {
        if (obj != null) {
            boolean needSendCleanMsg = false;
            if (obj instanceof Uri) {
                Uri uri = (Uri) obj;
                for (SwitchInfo switchInfo : this.mSwitchInfos.values()) {
                    if (switchInfo != null && uri.toString().equals(switchInfo.getUriStr())) {
                        boolean status = fetchSwitchStatus(switchInfo.getType(), switchInfo.getName(), switchInfo.getSwitchOn());
                        switchInfo.setStatus(status);
                        AwareLog.d(TAG, "updateSwitchStatus status:" + status + " uri:" + uri + " procname:" + switchInfo.getProcName());
                        if (!status) {
                            needSendCleanMsg = true;
                        }
                    }
                }
                if (needSendCleanMsg) {
                    this.mHasCleanApps = true;
                    sendMessage(2, null, 5000);
                    AwareLog.d(TAG, "updateSwitchStatus clean apps by observer:" + uri);
                }
            } else if (obj instanceof Integer) {
                updateSwitchStatusInternal(((Integer) obj).intValue());
            }
        }
    }

    private void updateSwitchStatusInternal(int pid) {
        ProcessInfo procInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (procInfo != null && procInfo.mProcessName != null) {
            if (SETTING_PKG.equals(procInfo.mProcessName)) {
                this.mHasCleanApps = true;
                sendMessage(2, null, 5000);
                AwareLog.d(TAG, "updateSwitchStatus clean apps by fg change:" + procInfo.mProcessName);
                return;
            }
            SwitchInfo switchInfo = this.mSwitchInfos.get(procInfo.mProcessName);
            if (switchInfo != null && switchInfo.getHasActivity() == 1) {
                this.mHasCleanApps = true;
                sendMessage(2, null, 5000);
                AwareLog.d(TAG, "updateSwitchStatus clean apps by fg change:" + procInfo.mProcessName);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanSwitchApps() {
        if (this.mHasCleanApps) {
            this.mHasCleanApps = false;
            ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
            if (!procList.isEmpty()) {
                int size = procList.size();
                for (int i = 0; i < size; i++) {
                    ProcessInfo info = procList.get(i);
                    if (info != null && this.mSwitchInfos.containsKey(info.mProcessName)) {
                        tryCleaning(info, this.mSwitchInfos.get(info.mProcessName));
                    }
                }
            }
        }
    }

    private void tryCleaning(ProcessInfo procInfo, SwitchInfo switchInfo) {
        if (!switchInfo.getStatus()) {
            if (AwareAppAssociate.getInstance().isForeGroundApp(procInfo.mUid)) {
                AwareLog.i(TAG, "Do not clean app pid:" + procInfo.mPid + " procName:" + procInfo.mProcessName + " is ForeGroundApp");
                this.mHasCleanApps = true;
                return;
            }
            SparseSet strong = new SparseSet();
            AwareAppAssociate.getInstance().getAssocClientListForPid(procInfo.mPid, strong);
            if (!strong.isEmpty()) {
                AwareLog.i(TAG, "Do not clean app pid:" + procInfo.mPid + " procName:" + procInfo.mProcessName + " has assoc processes " + strong);
                this.mHasCleanApps = true;
                return;
            }
            boolean status = fetchSwitchStatus(switchInfo.getType(), switchInfo.getName(), switchInfo.getSwitchOn());
            if (status) {
                switchInfo.setStatus(status);
            } else if (switchInfo.getHasActivity() == 1 && isRecentTaskAssociate(switchInfo)) {
                AwareLog.i(TAG, "Do not clean app pid:" + procInfo.mPid + " procName:" + procInfo.mProcessName + " isRecentTaskAssociate");
                this.mHasCleanApps = true;
            } else if (ProcessCleaner.getInstance(this.mContext).killProcess(procInfo.mPid, false, "switch_clean")) {
                AwareLog.i(TAG, "switch clean app pid:" + procInfo.mPid + " process:" + procInfo.mProcessName);
            } else {
                this.mHasCleanApps = true;
            }
        }
    }

    private boolean isRecentTaskAssociate(SwitchInfo switchInfo) {
        List<ActivityManager.RecentTaskInfo> taskInfoList;
        ComponentName topActivity;
        HwActivityManagerService hwActivityManagerService = this.mHwAms;
        if (hwActivityManagerService == null || (taskInfoList = hwActivityManagerService.getRecentTasksList(100, 1, this.mCurSwitchUser)) == null) {
            return true;
        }
        for (ActivityManager.RecentTaskInfo recentTask : taskInfoList) {
            if (!(recentTask == null || (topActivity = recentTask.topActivity) == null || !topActivity.toString().contains(switchInfo.getActivityName()))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initAppSwitch() {
        this.mSwitchInfos.clear();
        synchronized (this.mConfigItems) {
            int size = this.mConfigItems.size();
            for (int i = 0; i < size; i++) {
                SwitchInfo switchInfo = fetchSwitchInfo(this.mConfigItems.get(i));
                if (switchInfo != null) {
                    this.mSwitchInfos.put(switchInfo.getProcName(), switchInfo);
                    AwareLog.i(TAG, "initAppSwitch mSwitchKillApps: " + switchInfo);
                }
            }
        }
        registerContentObserver();
    }

    private SwitchInfo fetchSwitchInfo(String item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        String[] configList = item.split(";");
        if (configList.length != 4) {
            AwareLog.e(TAG, "invalid config: " + item);
            return null;
        }
        String numStr = configList[1].trim();
        if (numStr == null) {
            return null;
        }
        String[] numList = numStr.split(SPLIT_VALUE_FOR_QUERYSTR);
        if (numList.length != 3) {
            AwareLog.e(TAG, "invalid numStr: " + numList);
            return null;
        }
        try {
            int type = Integer.parseInt(numList[0].trim());
            int hasActivity = Integer.parseInt(numList[2].trim());
            String switchNum = numList[1].trim();
            String procName = configList[0].trim();
            String name = configList[2].trim();
            Uri uri = fetchSwitchUri(type, name);
            if (uri == null) {
                return null;
            }
            return new SwitchInfo(procName, type, name, uri.toString(), fetchSwitchStatus(type, name, switchNum), switchNum, hasActivity, configList[3].trim());
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "invalid config: " + item);
            return null;
        }
    }

    private boolean fetchSwitchStatus(int type, String name, String switchNum) {
        Context context = this.mContext;
        if (context == null) {
            return true;
        }
        String switchStatusStr = null;
        if (type == 1) {
            switchStatusStr = SettingsEx.System.getStringForUser(context.getContentResolver(), name, this.mCurSwitchUser);
        } else if (type == 2) {
            switchStatusStr = SettingsEx.Secure.getStringForUser(context.getContentResolver(), name, this.mCurSwitchUser);
        } else if (type == 3) {
            switchStatusStr = SettingsExEx.Global.getStringForUser(context.getContentResolver(), name, this.mCurSwitchUser);
        }
        if (switchStatusStr != null && !switchStatusStr.equals(switchNum)) {
            return false;
        }
        return true;
    }

    private void registerContentObserver() {
        Uri uri;
        if (!(this.mSettingsObserver == null || this.mContext == null)) {
            ArrayList<String> uriList = new ArrayList<>();
            for (SwitchInfo switchInfo : this.mSwitchInfos.values()) {
                if (!(switchInfo == null || (uri = fetchSwitchUri(switchInfo.getType(), switchInfo.getName())) == null || uriList.contains(uri.toString()))) {
                    AwareLog.d(TAG, "registerContentObserver config: " + uri.toString());
                    uriList.add(uri.toString());
                    ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), uri, false, this.mSettingsObserver, this.mCurSwitchUser);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterContentObserver() {
        Context context;
        if (this.mSettingsObserver != null && (context = this.mContext) != null) {
            context.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        }
    }

    private Uri fetchSwitchUri(int type, String methodStr) {
        if (type == 1) {
            return Settings.System.getUriFor(methodStr);
        }
        if (type == 2) {
            return Settings.Secure.getUriFor(methodStr);
        }
        if (type != 3) {
            return null;
        }
        return Settings.Global.getUriFor(methodStr);
    }

    /* access modifiers changed from: private */
    public static final class SwitchInfo {
        private String mActivityName;
        private int mHasActivity;
        private String mName;
        private String mProcName;
        private boolean mStatus;
        private String mSwitchOn;
        private int mType;
        private String mUriStr;

        public SwitchInfo(String procName, int type, String name, String uriStr, boolean status, String switchOn, int hasActivity, String activityName) {
            this.mType = type;
            this.mProcName = procName;
            this.mName = name;
            this.mUriStr = uriStr;
            this.mStatus = status;
            this.mSwitchOn = switchOn;
            this.mHasActivity = hasActivity;
            this.mActivityName = activityName;
        }

        public String getUriStr() {
            return this.mUriStr;
        }

        public int getType() {
            return this.mType;
        }

        public String getProcName() {
            return this.mProcName;
        }

        public String getName() {
            return this.mName;
        }

        public boolean getStatus() {
            return this.mStatus;
        }

        public void setStatus(boolean status) {
            this.mStatus = status;
        }

        public String getSwitchOn() {
            return this.mSwitchOn;
        }

        public int getHasActivity() {
            return this.mHasActivity;
        }

        public String getActivityName() {
            return this.mActivityName;
        }

        public String toString() {
            return "mType:" + this.mType + ",mProcName:" + this.mProcName + ",mName:" + this.mName + ",mUriStr:" + this.mUriStr + ",mStatus:" + this.mStatus + ",mSwitchOn:" + this.mSwitchOn + ",mHasActivity:" + this.mHasActivity + ",mActivityName:" + this.mActivityName;
        }
    }

    private void initSwitchUser() {
        AwareCallback.getInstance().registerUserSwitchObserver(this.mUserSwitchObserver);
    }

    private void deInitSwitchUser() {
        AwareCallback.getInstance().unregisterUserSwitchObserver(this.mUserSwitchObserver);
    }
}
