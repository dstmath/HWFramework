package com.android.server.pm;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BadParcelableException;
import android.os.BatteryManagerInternal;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.pm.dex.DexoptOptions;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwBackgroundDexOptServiceEx implements IHwBackgroundDexOptServiceEx {
    private static final String ATTR_NAME = "name";
    private static final int BATTERY_FULL_LEVEL = 100;
    private static final int BATTERY_PROPERTY_CAPACITY = 95;
    private static final int BATTERY_PROPERTY_CAPACITY_COMPENSATE_JOB = 60;
    private static final int COMPENSATE_PERIOD = 5000;
    private static final String CUST_FILE_PATH = "/xml/hw_aot_compile_apps_config.xml";
    private static final int DEFAULT_EXTER_INT = -1;
    private static final int DELAYOPT_PERIOD = 60000;
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "off"));
    private static final int JOB_POST_BOOT_UPDATE_DELAYOPT = 8001;
    private static final int JOB_POST_COMPENSATE_OPTIMIZE = 8002;
    private static final int MAX_COMPENSATE_OPTIMIZE_COUNT = 5;
    private static final int MSG_COMPENSATE_OPTIMIZE = 2;
    private static final int MSG_PREPARE_DATA = 1;
    static final String TAG = "HwBackgroundDexOptServiceEx";
    private static final String TAG_NAME = "speed";
    private final AtomicBoolean isBroadcastReceiverDexopt = new AtomicBoolean(false);
    private final AtomicBoolean isExitCompensateDexopt = new AtomicBoolean(false);
    private BatteryManagerInternal mBatteryManagerInternal;
    private final BroadcastReceiverDexopt mBroadcastReceiverDexopt = new BroadcastReceiverDexopt();
    final Context mContext;
    private DexoptHandler mDexoptHandler = null;
    IHwBackgroundDexOptInner mIbgDexOptInner = null;
    private final SparseArray<JobParameters> mJobParamsMap = new SparseArray<>();
    private final Object mLock = new Object();
    private PowerManager mPowerManager;
    final JobService mService;
    private ArraySet<String> mSpeedNodePkgs = null;

    public HwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner bdos, JobService service, Context context) {
        this.mIbgDexOptInner = bdos;
        this.mService = service;
        this.mContext = context;
    }

    public int getReason(int reason, int reasonBackgroudDexopt, int reasonSpeedDexopt, String pkg) {
        if (this.mSpeedNodePkgs == null) {
            this.mSpeedNodePkgs = getAllNeedForSpeedApps();
        }
        ArraySet<String> arraySet = this.mSpeedNodePkgs;
        if (arraySet == null || !arraySet.contains(pkg)) {
            return reasonBackgroudDexopt;
        }
        return reasonSpeedDexopt;
    }

    private ArraySet<String> getAllNeedForSpeedApps() {
        try {
            File file = HwCfgFilePolicy.getCfgFile(CUST_FILE_PATH, 0);
            if (file != null) {
                return readSpeedAppsFromXml(file);
            }
            Log.i(TAG, "hw_aot_compile_apps_config not exist");
            return null;
        } catch (NoClassDefFoundError e) {
            Log.i(TAG, "get speed apps failed");
            return null;
        }
    }

    private ArraySet<String> readSpeedAppsFromXml(File config) {
        XmlPullParser parser;
        int type;
        FileInputStream stream = null;
        if (!config.exists() || !config.canRead()) {
            return null;
        }
        try {
            FileInputStream stream2 = new FileInputStream(config);
            parser = Xml.newPullParser();
            parser.setInput(stream2, StandardCharsets.UTF_8.name());
            if (type != 2) {
                Log.w(TAG, "Failed parsing config, can't find start tag");
                try {
                    stream2.close();
                } catch (IOException e) {
                    Log.w(TAG, "readSpeedAppsFromXml stream.close catch IOException");
                }
                return null;
            }
            ArraySet<String> speedApps = new ArraySet<>();
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.w(TAG, "readSpeedAppsFromXml stream.close catch IOException");
                    }
                    return speedApps;
                } else if (!(type2 == 3 || type2 == 4)) {
                    if (TAG_NAME.equals(parser.getName())) {
                        String name = parser.getAttributeValue(null, ATTR_NAME);
                        if (!TextUtils.isEmpty(name)) {
                            speedApps.add(name.intern());
                        }
                    } else {
                        Log.w(TAG, "Unknown element under <config>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        } catch (XmlPullParserException e3) {
            Log.w(TAG, "Failed parsing config XmlPullParserException");
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.w(TAG, "readSpeedAppsFromXml stream.close catch IOException");
                }
            }
            return null;
        } catch (IOException e5) {
            Log.w(TAG, "Failed parsing config IOException");
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    Log.w(TAG, "readSpeedAppsFromXml stream.close catch IOException");
                }
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    Log.w(TAG, "readSpeedAppsFromXml stream.close catch IOException");
                }
            }
            throw th;
        }
        while (true) {
            type = parser.next();
            if (type == 2 || type == 1) {
                break;
            }
        }
    }

    private int getBatteryLevel() {
        if (this.mContext == null) {
            return 0;
        }
        Intent intent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (!intent.getBooleanExtra("present", true)) {
            return 100;
        }
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (level * 100) / scale;
    }

    public boolean runBootUpdateDelayOpt(JobParameters params) {
        synchronized (this.mLock) {
            if (params != null) {
                if (params.getJobId() == JOB_POST_BOOT_UPDATE_DELAYOPT) {
                    if (this.mJobParamsMap.get(JOB_POST_COMPENSATE_OPTIMIZE) == null) {
                        this.mJobParamsMap.put(JOB_POST_BOOT_UPDATE_DELAYOPT, params);
                        if (this.mDexoptHandler == null) {
                            this.mDexoptHandler = new DexoptHandler();
                        }
                        registerReceiver();
                        if (this.mDexoptHandler != null) {
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = params;
                            this.mDexoptHandler.sendMessageDelayed(msg, 60000);
                            if (IS_DEBUG) {
                                Log.i(TAG, "Job DELAYOPT start!");
                            }
                        }
                        return true;
                    }
                }
            }
            Log.i(TAG, "Job UPDATE_DELAYOPT If there are other tasks in progress, wait for the next time");
            return false;
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiverDexopt(intentFilter);
    }

    public boolean runCompensateOpt(JobParameters params) {
        synchronized (this.mLock) {
            if (params != null) {
                if (params.getJobId() == JOB_POST_COMPENSATE_OPTIMIZE) {
                    if (this.mJobParamsMap.get(JOB_POST_BOOT_UPDATE_DELAYOPT) == null) {
                        this.mJobParamsMap.put(JOB_POST_COMPENSATE_OPTIMIZE, params);
                        if (this.mDexoptHandler == null) {
                            this.mDexoptHandler = new DexoptHandler();
                        }
                        if (this.mDexoptHandler != null && !this.mDexoptHandler.hasMessages(1) && !this.mDexoptHandler.hasMessages(2)) {
                            Message msg = Message.obtain();
                            msg.what = 2;
                            msg.obj = params;
                            this.mDexoptHandler.sendMessageDelayed(msg, 5000);
                            if (IS_DEBUG) {
                                Log.i(TAG, "Job COMPENSATEOPT start!");
                            }
                        }
                        return true;
                    }
                }
            }
            Log.i(TAG, "Job COMPENSATEOPT If there are other tasks in progress, wait for the next time");
            return true;
        }
    }

    public boolean stopBootUpdateDelayOpt(JobParameters params) {
        if (params == null || params.getJobId() != JOB_POST_BOOT_UPDATE_DELAYOPT) {
            return false;
        }
        unregisterReceiverDexopt();
        this.mJobParamsMap.remove(JOB_POST_BOOT_UPDATE_DELAYOPT);
        DexoptHandler dexoptHandler = this.mDexoptHandler;
        if (dexoptHandler != null && dexoptHandler.hasMessages(1)) {
            this.mDexoptHandler.removeMessages(1);
        }
        this.mDexoptHandler = null;
        return true;
    }

    public boolean stopCompenstateOpt(JobParameters params) {
        if (params == null || params.getJobId() != JOB_POST_COMPENSATE_OPTIMIZE) {
            return false;
        }
        removeCompensateJobParam();
        DexoptHandler dexoptHandler = this.mDexoptHandler;
        if (dexoptHandler != null && dexoptHandler.hasMessages(2)) {
            this.mDexoptHandler.removeMessages(2);
        }
        this.mDexoptHandler = null;
        return true;
    }

    public void interruptCompensateOpt() {
        Log.i(TAG, "interruptCompensateOpt");
        this.isExitCompensateDexopt.set(true);
    }

    public void cancelInterruptCompensateOpt() {
        Log.i(TAG, "cancelInterruptCompensateOpt");
        this.isExitCompensateDexopt.set(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerPrepareData(JobParameters params) {
        if (params.getJobId() != JOB_POST_BOOT_UPDATE_DELAYOPT || this.mIbgDexOptInner == null || this.mContext == null || this.mService == null) {
            Log.i(TAG, "handlerPrepareData parameter is incorrect.");
            return;
        }
        PackageManagerService packageManagerService = (PackageManagerService) ServiceManager.getService("package");
        if (!isMeetOptimizeConditions(packageManagerService, params)) {
            Log.i(TAG, "handlerPrepareData execution conditions not met, do not implement optimize.");
            return;
        }
        ArraySet<String> pkgs = packageManagerService.getOptimizablePackages();
        if (pkgs.isEmpty()) {
            this.mService.jobFinished(params, false);
            if (IS_DEBUG) {
                Log.i(TAG, "No packages to optimize");
                return;
            }
            return;
        }
        if (IS_DEBUG) {
            Log.i(TAG, "handlerPrepareData Start update!");
        }
        if (!this.mIbgDexOptInner.runPostBootUpdateEx(params, packageManagerService, pkgs)) {
            this.mService.jobFinished(params, false);
            if (IS_DEBUG) {
                Log.i(TAG, "Superseded by IDLE and Job DELAYOPT stop!");
            }
        }
        stopBootUpdateDelayOpt(params);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0084, code lost:
        android.util.Log.i(com.android.server.pm.HwBackgroundDexOptServiceEx.TAG, "excutePackageDexOpt interrupt");
     */
    private void handlerCompensateOptimize(JobParameters params) {
        if (params.getJobId() != JOB_POST_COMPENSATE_OPTIMIZE || this.mService == null) {
            Log.i(TAG, "handlerCompensateOptimize parameter is incorrect.");
            return;
        }
        PackageManagerService packageManagerService = (PackageManagerService) ServiceManager.getService("package");
        if (!isMeetOptimizeConditions(packageManagerService, params)) {
            removeCompensateJobParam();
            Log.i(TAG, "handlerCompensateOptimize execution conditions not met, do not implement optimize.");
            return;
        }
        ArraySet<String> optimizablePackages = packageManagerService.getOptimizablePackages();
        if (optimizablePackages.isEmpty()) {
            removeCompensateJobParam();
            this.mService.jobFinished(params, true);
            if (IS_DEBUG) {
                Log.i(TAG, "No packages to optimize");
                return;
            }
            return;
        }
        List<String> waitingDexOptPackages = HwCompensateDexOptManager.getInstance().getWaitingDexOptPackages();
        HwCompensateDexOptManager compensateDexOptManager = HwCompensateDexOptManager.getInstance();
        int excuteCounter = 0;
        Iterator<String> it = waitingDexOptPackages.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String packageName = it.next();
            if (excuteCounter >= 5 || this.isExitCompensateDexopt.get() || !isCharging() || isScreenOn()) {
                break;
            } else if (excutePackageDexOpt(packageName, compensateDexOptManager, packageManagerService)) {
                excuteCounter++;
            }
        }
        Iterator<String> it2 = optimizablePackages.iterator();
        while (true) {
            if (!it2.hasNext()) {
                break;
            }
            String packageName2 = it2.next();
            if (excuteCounter >= 5 || this.isExitCompensateDexopt.get() || !isCharging() || isScreenOn()) {
                break;
            } else if (excutePackageDexOpt(packageName2, compensateDexOptManager, packageManagerService)) {
                excuteCounter++;
            }
        }
        Log.i(TAG, "excutePackageDexOpt interrupt");
        stopCompenstateOpt(params);
    }

    private boolean excutePackageDexOpt(String packageName, HwCompensateDexOptManager compensateDexOptManager, PackageManagerService packageManagerService) {
        Log.i(TAG, "runCompensateDexOpt begin:" + packageName);
        int result = packageManagerService.performDexOptWithStatus(new DexoptOptions(packageName, 3, 4));
        Log.i(TAG, "runCompensateDexOpt finish:" + packageName + ",result:" + result);
        compensateDexOptManager.removePackageFromWaitingList(packageName);
        return result == 1;
    }

    private void removeCompensateJobParam() {
        synchronized (this.mLock) {
            this.mJobParamsMap.remove(JOB_POST_COMPENSATE_OPTIMIZE);
        }
    }

    private boolean isMeetOptimizeConditions(PackageManagerService packageManagerService, JobParameters params) {
        int batteryLevel = getBatteryLevel();
        int battertCapacity = 95;
        if (params.getJobId() == JOB_POST_COMPENSATE_OPTIMIZE) {
            battertCapacity = 60;
        }
        if (isScreenOn() || batteryLevel < battertCapacity) {
            if (IS_DEBUG) {
                Log.i(TAG, "ScreenOn or BatteryLevel low, and Job DELAYOPT stop! BatteryLevel =" + batteryLevel);
            }
            return false;
        } else if (!packageManagerService.isStorageLow()) {
            return true;
        } else {
            this.mService.jobFinished(params, false);
            if (IS_DEBUG) {
                Log.i(TAG, "Low storage, skipping this run");
            }
            return false;
        }
    }

    private boolean isScreenOn() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        PowerManager powerManager = this.mPowerManager;
        if (powerManager == null) {
            return false;
        }
        return powerManager.isScreenOn();
    }

    private boolean isCharging() {
        if (this.mBatteryManagerInternal == null) {
            this.mBatteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
        }
        BatteryManagerInternal batteryManagerInternal = this.mBatteryManagerInternal;
        if (batteryManagerInternal != null) {
            return batteryManagerInternal.isPowered(7);
        }
        Log.i(TAG, "BatteryManagerInternal is null");
        return false;
    }

    /* access modifiers changed from: private */
    public class DexoptHandler extends Handler {
        private DexoptHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.w(HwBackgroundDexOptServiceEx.TAG, "msg is null, error!");
                return;
            }
            int i = msg.what;
            if (i != 1) {
                if (i != 2) {
                    Log.w(HwBackgroundDexOptServiceEx.TAG, "DexoptHandler, default branch, msg.what is " + msg.what);
                } else if (msg.obj instanceof JobParameters) {
                    final JobParameters params = (JobParameters) msg.obj;
                    new Thread("HwBackgroundDexOptService_CompensateOptimize") {
                        /* class com.android.server.pm.HwBackgroundDexOptServiceEx.DexoptHandler.AnonymousClass1 */

                        @Override // java.lang.Thread, java.lang.Runnable
                        public void run() {
                            HwBackgroundDexOptServiceEx.this.handlerCompensateOptimize(params);
                        }
                    }.start();
                }
            } else if (msg.obj instanceof JobParameters) {
                HwBackgroundDexOptServiceEx.this.handlerPrepareData((JobParameters) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public class BroadcastReceiverDexopt extends BroadcastReceiver {
        private BroadcastReceiverDexopt() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                boolean isBatteryAllow = false;
                if (hashCode != -2128145023) {
                    if (hashCode == -1538406691 && action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 1;
                    }
                } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF)) {
                    c = 0;
                }
                if (c == 0) {
                    HwBackgroundDexOptServiceEx.this.handleScreenOffBroadcastReceiver();
                } else if (c == 1) {
                    try {
                        int chargeType = intent.getIntExtra("status", 1);
                        if (chargeType == 2 || chargeType == 5) {
                            isBatteryAllow = true;
                        }
                        if (HwBackgroundDexOptServiceEx.IS_DEBUG) {
                            Log.i(HwBackgroundDexOptServiceEx.TAG, "chargeType:" + chargeType + ",allow:" + isBatteryAllow);
                        }
                        HwBackgroundDexOptServiceEx.this.handleBatteryChangedBroadcastReceiver(isBatteryAllow);
                    } catch (BadParcelableException e) {
                        Log.e(HwBackgroundDexOptServiceEx.TAG, "onReceiveBatteryBroadcast BadParcelableException");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOffBroadcastReceiver() {
        synchronized (this.mLock) {
            if (!(this.mDexoptHandler == null || this.mDexoptHandler.hasMessages(1) || this.mJobParamsMap.get(JOB_POST_BOOT_UPDATE_DELAYOPT) == null)) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = this.mJobParamsMap.get(JOB_POST_BOOT_UPDATE_DELAYOPT);
                this.mDexoptHandler.sendMessageDelayed(msg, 60000);
                if (IS_DEBUG) {
                    Log.i(TAG, "Job DELAYOPT start when ACTION_SCREEN_OFF!");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBatteryChangedBroadcastReceiver(boolean isBatteryAllow) {
        synchronized (this.mLock) {
            if (isBatteryAllow) {
                if (!(this.mDexoptHandler == null || this.mDexoptHandler.hasMessages(1) || this.mJobParamsMap.get(JOB_POST_BOOT_UPDATE_DELAYOPT) == null)) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = this.mJobParamsMap.get(JOB_POST_BOOT_UPDATE_DELAYOPT);
                    this.mDexoptHandler.sendMessageDelayed(msg, 60000);
                    if (IS_DEBUG) {
                        Log.i(TAG, "Job DELAYOPT start when ACTION_BATTERY_CHANGED!");
                    }
                }
            }
        }
    }

    private synchronized void registerReceiverDexopt(IntentFilter intentFilter) {
        if (!this.isBroadcastReceiverDexopt.get() && this.mContext != null) {
            this.mContext.registerReceiver(this.mBroadcastReceiverDexopt, intentFilter);
            this.isBroadcastReceiverDexopt.set(true);
        }
    }

    private synchronized void unregisterReceiverDexopt() {
        if (this.isBroadcastReceiverDexopt.get() && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiverDexopt);
            this.isBroadcastReceiverDexopt.set(false);
        }
    }
}
