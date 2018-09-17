package com.android.server.pfw;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.server.HwConnectivityService;
import com.android.server.am.ActivityRecord;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.pfw.policy.HwPFWAppAutoStartupPolicy;
import com.android.server.pfw.policy.HwPFWAppRestartPolicy;
import com.android.server.pfw.policy.HwPFWAppWakeLockPolicy;
import com.android.server.pfw.policy.HwPFWGoogleServicePolicy;
import com.android.server.pfw.policy.HwPFWPolicy;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import huawei.android.pfw.HwPFWStartupControlScope;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.android.pfw.HwPFWStartupSetting;
import huawei.android.pfw.IHwPFWManager.Stub;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class HwPFWService extends Stub {
    public static final String ACTION_PFW_WAKEUP_TIMER = "action.pfw.wakeup.timer";
    private static final String HW_STOPPED = "hw_stopped";
    private static final int HW_STOPPED_MAX_NUM = 300;
    private static final String HW_STOPPED_PACKAGE_ACTION = "huawei.intent.action.HW_STOPPED_PACKAGE_ACTION";
    private static final String NO_PERMISSION_GRANTED = "Only System UID apps have POWER-FIREWALL AIDL calling permission";
    private static final int PFW_MSG_SYSTEM = 0;
    private static final int PFW_MSG_UNPROTECTED_LIST = 1;
    private static final int PFW_MSG_WAKEUP_TIMER = 2;
    private static final long PFW_SCR_OFF_TIMER_INTERVAL = 1800000;
    private static final long PFW_SCR_ON_TIMER_INTERVAL = 300000;
    private static final String TAG = "PFW.HwPFWService";
    private static HwPFWService mSelf;
    private ActivityManager mAm;
    private BatteryStatsHelper mBatteryStatsHelper;
    private final Object mBatteryStatsLock;
    private final BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private ArrayList<String> mHwStopPkgList;
    private final Object mHwStopPkgListLock;
    private final BroadcastReceiver mLauncherReceiver;
    private long mPfwTimerInterval;
    private SparseArray<HwPFWPolicy> mPolicies;
    private final BroadcastReceiver stopPackageReceiver;

    private class PfwHandler extends Handler {
        public PfwHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = msg.obj;
            if (intent != null) {
                switch (msg.what) {
                    case HwPFWService.PFW_MSG_SYSTEM /*0*/:
                        HwPFWLogger.d(HwPFWService.TAG, "handle PFW_MSG_SYSTEM");
                        HwPFWService.this.handleBroadcastMsg(intent);
                        break;
                    case HwPFWService.PFW_MSG_UNPROTECTED_LIST /*1*/:
                        HwPFWLogger.d(HwPFWService.TAG, "handle PFW_MSG_UNPROTECTED_LIST");
                        HwPFWService.this.setUnprotectedAppList(intent);
                        break;
                    case HwPFWService.PFW_MSG_WAKEUP_TIMER /*2*/:
                        HwPFWLogger.d(HwPFWService.TAG, "handle PFW_MSG_WAKEUP_TIMER");
                        HwPFWService.this.handleBroadcastMsg(new Intent(HwPFWService.ACTION_PFW_WAKEUP_TIMER));
                        break;
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pfw.HwPFWService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pfw.HwPFWService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pfw.HwPFWService.<clinit>():void");
    }

    public static HwPFWService self() {
        return mSelf;
    }

    public HwPFWService(Context context) {
        this.mHwStopPkgList = new ArrayList();
        this.mHwStopPkgListLock = new Object();
        this.mAm = null;
        this.mBatteryStatsHelper = null;
        this.mBatteryStatsLock = new Object();
        this.mHandlerThread = new HandlerThread("PFW Handler", 10);
        this.mHandler = null;
        this.mPolicies = new SparseArray();
        this.mPfwTimerInterval = PFW_SCR_OFF_TIMER_INTERVAL;
        this.stopPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null && action.equals(HwPFWService.HW_STOPPED_PACKAGE_ACTION)) {
                        HwPFWService.this.mHandler.sendMessage(HwPFWService.this.mHandler.obtainMessage(HwPFWService.PFW_MSG_UNPROTECTED_LIST, intent));
                    }
                }
            }
        };
        this.mLauncherReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null && action.equals(HwPFWAppAutoStartupPolicy.HW_LAUNCHER_WIDGET_UPDATE)) {
                        HwPFWService.this.mHandler.sendMessage(HwPFWService.this.mHandler.obtainMessage(HwPFWService.PFW_MSG_SYSTEM, intent));
                    }
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                    HwPFWLogger.e(HwPFWService.TAG, "onReceive invalid intent");
                    return;
                }
                String action = intent.getAction();
                if (action != null) {
                    HwPFWLogger.d(HwPFWService.TAG, "mBroadcastReceiver action = " + action);
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        HwPFWService.this.mPfwTimerInterval = HwPFWService.PFW_SCR_ON_TIMER_INTERVAL;
                    }
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        HwPFWService.this.mPfwTimerInterval = HwPFWService.PFW_SCR_OFF_TIMER_INTERVAL;
                    }
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        HwPFWService.this.createBatteryHelper(HwPFWService.this.mContext);
                        HwPFWService.this.setCollectorAlarm();
                    }
                    if (action.equals(HwPFWService.ACTION_PFW_WAKEUP_TIMER)) {
                        HwPFWLogger.d(HwPFWService.TAG, "alarm trigger");
                        HwPFWService.this.setCollectorAlarm();
                        HwPFWService.this.mHandler.sendMessage(HwPFWService.this.mHandler.obtainMessage(HwPFWService.PFW_MSG_WAKEUP_TIMER, intent));
                    } else {
                        HwPFWService.this.mHandler.sendMessage(HwPFWService.this.mHandler.obtainMessage(HwPFWService.PFW_MSG_SYSTEM, intent));
                    }
                }
            }
        };
        mSelf = this;
        this.mContext = context;
        initHandler();
        generatePolicies();
        this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
        registerBroadcast();
    }

    private void initHandler() {
        this.mHandlerThread.start();
        this.mHandler = new PfwHandler(this.mHandlerThread.getLooper());
    }

    private void registerBroadcast() {
        HwPFWLogger.d(TAG, "registerBroadcast");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction(HwConnectivityService.CONNECTIVITY_CHANGE_ACTION);
        filter.addAction(ACTION_PFW_WAKEUP_TIMER);
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
        IntentFilter stopPackageFilter = new IntentFilter();
        stopPackageFilter.addAction(HW_STOPPED_PACKAGE_ACTION);
        this.mContext.registerReceiver(this.stopPackageReceiver, stopPackageFilter, "android.permission.FORCE_STOP_PACKAGES", null);
        registerLauncherWidgetSubReceiver();
    }

    private void registerLauncherWidgetSubReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(HwPFWAppAutoStartupPolicy.HW_LAUNCHER_WIDGET_UPDATE);
        this.mContext.registerReceiver(this.mLauncherReceiver, filter, "android.permission.BIND_APPWIDGET", null);
    }

    private void setCollectorAlarm() {
        HwPFWLogger.d(TAG, "setCollectorAlarm mPfwTimerInterval = " + this.mPfwTimerInterval);
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent intent = new Intent(ACTION_PFW_WAKEUP_TIMER, null);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, PFW_MSG_SYSTEM, intent, PFW_MSG_SYSTEM);
        am.cancel(pendingIntent);
        am.set(3, SystemClock.elapsedRealtime() + this.mPfwTimerInterval, pendingIntent);
    }

    private void generatePolicies() {
        this.mPolicies.put(PFW_MSG_SYSTEM, new HwPFWAppRestartPolicy(this.mContext, this));
        this.mPolicies.put(PFW_MSG_UNPROTECTED_LIST, new HwPFWAppWakeLockPolicy(this.mContext, this));
        this.mPolicies.put(PFW_MSG_WAKEUP_TIMER, new HwPFWGoogleServicePolicy(this.mContext, this));
        this.mPolicies.put(3, new HwPFWAppAutoStartupPolicy(this.mContext));
    }

    private void handleBroadcastMsg(Intent intent) {
        for (int i = PFW_MSG_SYSTEM; i < this.mPolicies.size(); i += PFW_MSG_UNPROTECTED_LIST) {
            ((HwPFWPolicy) this.mPolicies.valueAt(i)).handleBroadcastIntent(intent);
        }
    }

    private <T> T findPolicy(int type) {
        return this.mPolicies.get(type);
    }

    private void setUnprotectedAppList(Intent intent) {
        if (intent != null) {
            ArrayList<String> pkgList = intent.getStringArrayListExtra(HW_STOPPED);
            if (pkgList != null) {
                synchronized (this.mHwStopPkgListLock) {
                    this.mHwStopPkgList.clear();
                    int N = pkgList.size();
                    int num = N > HW_STOPPED_MAX_NUM ? HW_STOPPED_MAX_NUM : N;
                    for (int i = PFW_MSG_SYSTEM; i < num; i += PFW_MSG_UNPROTECTED_LIST) {
                        String pkgName = (String) pkgList.get(i);
                        if (!(pkgName == null || this.mHwStopPkgList.contains(pkgName))) {
                            this.mHwStopPkgList.add(pkgName);
                        }
                    }
                }
            }
        }
    }

    public boolean isAppWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        if (!((HwPFWGoogleServicePolicy) findPolicy(PFW_MSG_WAKEUP_TIMER)).isGmsWakeLockFilterTag(flags, packageName, ws)) {
            return false;
        }
        HwPFWLogger.d(TAG, "prevent pkg : " + packageName + " acquire wakelock");
        return true;
    }

    public ArrayList<String> getStopPkgList() {
        return this.mHwStopPkgList;
    }

    public Object getStopPkgListLock() {
        return this.mHwStopPkgListLock;
    }

    public ActivityManager getActivityManager() {
        return this.mAm;
    }

    public BatteryStatsHelper getBatteryStatsHelper() {
        return this.mBatteryStatsHelper;
    }

    public Object getBatteryLockObject() {
        return this.mBatteryStatsLock;
    }

    public boolean isGoogleConnectOK() {
        if (((HwPFWGoogleServicePolicy) findPolicy(PFW_MSG_WAKEUP_TIMER)).getPreventGmsVal()) {
            return false;
        }
        return true;
    }

    public void startupFilterReceiverList(Intent intent, List<ResolveInfo> receivers) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.startupFilterReceiverList(intent, receivers);
        } else {
            HwPFWLogger.w(TAG, "startupFilterReceiverList policy not ready!");
        }
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventStartService(servInfo, callerPid, callerUid);
        }
        HwPFWLogger.w(TAG, "shouldPreventStartService policy not ready!");
        return false;
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventStartActivity(intent, aInfo, record);
        }
        HwPFWLogger.w(TAG, "shouldPreventStartActivity policy not ready!");
        return false;
    }

    public boolean shouldPreventRestartService(String pkgName) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventRestartService(pkgName);
        }
        HwPFWLogger.w(TAG, "shouldPreventRestartService policy not ready!");
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventStartProvider(cpi, callerPid, callerUid);
        }
        HwPFWLogger.w(TAG, "shouldPreventStartProvider policy not ready!");
        return false;
    }

    public boolean shouldPreventJobService(Intent intent, int userId) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventJobService(intent, userId);
        }
        HwPFWLogger.w(TAG, "shouldPreventJobService policy not ready!");
        return false;
    }

    public boolean shouldPreventSyncService(Intent intent, int userId) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.shouldPreventSyncService(intent, userId);
        }
        HwPFWLogger.w(TAG, "shouldPreventSyncService policy not ready!");
        return false;
    }

    public void addForbidRestartApp(String pkgName) {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.addForbidRestartApp(pkgName);
        } else {
            HwPFWLogger.w(TAG, "addForbidRestartApp policy not ready!");
        }
    }

    public void clearAllForbidRestartApp() {
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.clearAllForbidRestartApp();
        }
        HwPFWLogger.w(TAG, "clearAllForbidRestartApp policy not ready!");
    }

    private void createBatteryHelper(Context ctx) {
        if (this.mBatteryStatsHelper == null) {
            this.mBatteryStatsHelper = new BatteryStatsHelper(ctx, true);
            this.mBatteryStatsHelper.create((Bundle) null);
        }
    }

    public HwPFWStartupControlScope getStartupControlScope() throws RemoteException {
        enforceCallerIsSystemUID();
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            return policy.getStartupControlScope();
        }
        HwPFWLogger.w(TAG, "getStartupControlScope policy not ready!");
        return null;
    }

    public void appendExtStartupControlScope(HwPFWStartupControlScope scope) throws RemoteException {
        enforceCallerIsSystemUID();
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.appendExtStartupControlScope(scope);
        } else {
            HwPFWLogger.w(TAG, "appendExtStartupControlScope policy not ready!");
        }
    }

    public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) throws RemoteException {
        enforceCallerIsSystemUID();
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.updateStartupSettings(settings, clearFirst);
        } else {
            HwPFWLogger.w(TAG, "updateStartupSettings policy not ready!");
        }
    }

    public void removeStartupSetting(String pkgName) throws RemoteException {
        enforceCallerIsSystemUID();
        HwPFWAppAutoStartupPolicy policy = (HwPFWAppAutoStartupPolicy) findPolicy(3);
        if (policy != null) {
            policy.removeStartupSetting(pkgName);
        } else {
            HwPFWLogger.w(TAG, "removeStartupSetting policy not ready!");
        }
    }

    public void setPolicyEnabled(int policyType, boolean enabled) throws RemoteException {
        enforceCallerIsSystemUID();
        HwPFWPolicy policy = (HwPFWPolicy) findPolicy(3);
        if (policy != null) {
            policy.setPolicyEnabled(enabled);
        } else {
            HwPFWLogger.w(TAG, "setPolicyEnabled can't find policy for: " + policyType);
        }
    }

    public HwPFWStartupPackageList getStartupPackageList(int type) throws RemoteException {
        enforceCallerIsSystemUID();
        return null;
    }

    public void setStartupPackageList(HwPFWStartupPackageList startupPkgList) throws RemoteException {
        enforceCallerIsSystemUID();
    }

    private String getAppNameByPid(Context context, int pID) {
        String processName = AppHibernateCst.INVALID_PKG;
        if (context == null) {
            return processName;
        }
        List<RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (processes == null) {
            return processName;
        }
        int size = processes.size();
        for (int index = PFW_MSG_SYSTEM; index < size; index += PFW_MSG_UNPROTECTED_LIST) {
            if (pID == ((RunningAppProcessInfo) processes.get(index)).pid) {
                processName = ((RunningAppProcessInfo) processes.get(index)).processName;
                break;
            }
        }
        return processName;
    }

    public String getTopAppInfo(int topNum) {
        IOException ioe;
        RuntimeException rte;
        Exception ex;
        Throwable th;
        String topInfoStr = AppHibernateCst.INVALID_PKG;
        BufferedReader bufferedReader = null;
        Runtime runtime = Runtime.getRuntime();
        StringBuilder builder = new StringBuilder();
        String PTM_PKG = "com.huawei.imonitor";
        if (topNum <= 0) {
            topNum = 5;
        }
        builder.append("top -t -m ");
        builder.append(topNum);
        builder.append(" -d 0.5 -n 1");
        String command = builder.toString();
        builder.setLength(PFW_MSG_SYSTEM);
        if (this.mContext != null) {
            int pid = getCallingPid();
            if (!PTM_PKG.equals(getAppNameByPid(this.mContext, pid))) {
                return topInfoStr;
            }
        }
        try {
            InputStream inputStream = runtime.exec(command).getInputStream();
            if (inputStream == null) {
                return topInfoStr;
            }
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while (true) {
                    try {
                        String line = bufferedReader2.readLine();
                        if (line != null) {
                            builder.append(line);
                            builder.append('\n');
                        } else {
                            try {
                                break;
                            } catch (IOException e) {
                                HwPFWLogger.e(TAG, "when inputStream.close catch IOException :" + e.getMessage());
                            }
                        }
                    } catch (IOException e2) {
                        ioe = e2;
                        bufferedReader = bufferedReader2;
                    } catch (RuntimeException e3) {
                        rte = e3;
                        bufferedReader = bufferedReader2;
                    } catch (Exception e4) {
                        ex = e4;
                        bufferedReader = bufferedReader2;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = bufferedReader2;
                    }
                }
                inputStream.close();
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e5) {
                        HwPFWLogger.e(TAG, "when bufferedReader.close catch IOException :" + e5.getMessage());
                    }
                }
            } catch (IOException e6) {
                ioe = e6;
                try {
                    builder.append("IOException error");
                    HwPFWLogger.e(TAG, "when read topAppInfo catch IOException :" + ioe.getMessage());
                    try {
                        inputStream.close();
                    } catch (IOException e52) {
                        HwPFWLogger.e(TAG, "when inputStream.close catch IOException :" + e52.getMessage());
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e522) {
                            HwPFWLogger.e(TAG, "when bufferedReader.close catch IOException :" + e522.getMessage());
                        }
                    }
                    return builder.toString().trim();
                } catch (Throwable th3) {
                    th = th3;
                    try {
                        inputStream.close();
                    } catch (IOException e5222) {
                        HwPFWLogger.e(TAG, "when inputStream.close catch IOException :" + e5222.getMessage());
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e52222) {
                            HwPFWLogger.e(TAG, "when bufferedReader.close catch IOException :" + e52222.getMessage());
                        }
                    }
                    throw th;
                }
            } catch (RuntimeException e7) {
                rte = e7;
                builder.append("RuntimeException error");
                HwPFWLogger.e(TAG, "when read topAppInfo catch RuntimeException :" + rte.getMessage());
                try {
                    inputStream.close();
                } catch (IOException e522222) {
                    HwPFWLogger.e(TAG, "when inputStream.close catch IOException :" + e522222.getMessage());
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5222222) {
                        HwPFWLogger.e(TAG, "when bufferedReader.close catch IOException :" + e5222222.getMessage());
                    }
                }
                return builder.toString().trim();
            } catch (Exception e8) {
                ex = e8;
                builder.append("exception error");
                HwPFWLogger.e(TAG, "when read topAppInfo catch Exception :" + ex.getMessage());
                try {
                    inputStream.close();
                } catch (IOException e52222222) {
                    HwPFWLogger.e(TAG, "when inputStream.close catch IOException :" + e52222222.getMessage());
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e522222222) {
                        HwPFWLogger.e(TAG, "when bufferedReader.close catch IOException :" + e522222222.getMessage());
                    }
                }
                return builder.toString().trim();
            }
            return builder.toString().trim();
        } catch (IOException e5222222222) {
            Log.w(TAG, "getInputStream err: " + e5222222222.toString());
            return topInfoStr;
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        for (int i = PFW_MSG_SYSTEM; i < this.mPolicies.size(); i += PFW_MSG_UNPROTECTED_LIST) {
            ((HwPFWPolicy) this.mPolicies.valueAt(i)).printDumpInfo(fout);
        }
    }

    private void enforceCallerIsSystemUID() {
        if (IOTController.TYPE_MASTER != UserHandle.getAppId(Binder.getCallingUid())) {
            throw new SecurityException(NO_PERMISSION_GRANTED);
        }
    }
}
