package com.android.server.mtm.dump;

import android.app.mtm.MultiTaskManager;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppCleanParam.AppCleanInfo;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppFreezeSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc;
import android.app.mtm.iaware.appmng.IAppCleanCallback.Stub;
import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CrashClean;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.AppStatusUtils.Status;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAppMngClean {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues = null;
    private static final int SINGLE_REMOVE = 1;
    public static final String TAG = "DumpAppMngClean";
    private static volatile Map<String, Consumer<Params>> consumers = new HashMap();

    static class Params {
        public String[] args;
        public Context context;
        public PrintWriter pw;

        public Params(Context context, PrintWriter pw, String[] args) {
            this.context = context;
            this.pw = pw;
            this.args = args;
        }
    }

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues;
        }
        int[] iArr = new int[AppMngFeature.values().length];
        try {
            iArr[AppMngFeature.APP_CLEAN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppMngFeature.APP_FREEZE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppMngFeature.APP_IOLIMIT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppMngFeature.APP_START.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues = iArr;
        return iArr;
    }

    public static final void dump(Context context, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 2 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (DumpAppMngClean.class) {
                if (consumers.isEmpty()) {
                    consumers.put("clean", new -$Lambda$8I2a-RHNxNJhwy2xVvLply0Ho_0());
                    consumers.put("dumpPackage", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpPackageList", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpTask", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("SMClean", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("SMQuery", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("PGClean", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("CrashClean", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("CheckStatus", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpDecide", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpHistory", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("help", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpBigData", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("dumpAppType", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                }
                Consumer<Params> func = (Consumer) consumers.get(cmd);
                if (func == null) {
                    pw.println("  Bad command: " + cmd);
                    return;
                }
                try {
                    func.accept(new Params(context, pw, args));
                } catch (Exception e) {
                    pw.println("  Bad command:");
                    pw.println(e.toString());
                }
            }
        } else {
            return;
        }
        return;
    }

    private static void dumpPackage(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackage(mParams.args[2], Integer.parseInt(mParams.args[3])), mParams.pw);
    }

    private static void dumpPackageList(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        String[] packList = mParams.args[2].split(",");
        HashMap<String, Integer> packMap = new HashMap();
        for (String s : packList) {
            String[] pack = s.split(":");
            mParams.pw.println("  package name: " + pack[0] + ", uid: " + pack[1]);
            packMap.put(pack[0], Integer.valueOf(Integer.parseInt(pack[1])));
        }
        ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackageMap(packMap), mParams.pw);
    }

    private static void dumpTask(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromTask(Integer.parseInt(mParams.args[2]), Integer.parseInt(mParams.args[3])), mParams.pw);
    }

    private static void dumpSMClean(Params mParams) {
        if (mParams.args.length < 5) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgNames = getStrList(mParams);
        List<Integer> userIds = getIntList(mParams.args[3]);
        List<Integer> killTypes = getIntList(mParams.args[4]);
        if (pkgNames != null && userIds != null && killTypes != null) {
            List<AppCleanInfo> appCleanInfoList = new ArrayList();
            int size = pkgNames.size();
            for (int i = 0; i < size; i++) {
                appCleanInfoList.add(new AppCleanInfo((String) pkgNames.get(i), (Integer) userIds.get(i), (Integer) killTypes.get(i)));
            }
            MultiTaskManager.getInstance().executeMultiAppClean(appCleanInfoList, new Stub() {
                public void onCleanFinish(AppCleanParam result) {
                    AwareLog.i(DumpAppMngClean.TAG, "DumpResult onCleanFinish:" + result);
                }
            });
        }
    }

    private static void getSMCleanList(Params mParams) {
        MultiTaskManager.getInstance().getAppListForUserClean(new Stub() {
            public void onCleanFinish(AppCleanParam result) {
                List<String> pkgList = result.getStringList();
                List<Integer> uidList = result.getIntList();
                List<Integer> killTypeList = result.getIntList2();
                AwareLog.i(DumpAppMngClean.TAG, "SMQuery callback called, size = " + pkgList.size() + ", in Thread: " + Thread.currentThread().getName());
                int i = 0;
                int len = pkgList.size();
                while (i < len) {
                    AwareLog.i(DumpAppMngClean.TAG, "SMQuery pkg = " + ((String) pkgList.get(i)) + ", uid: " + (i < uidList.size() ? ((Integer) uidList.get(i)).intValue() : -1) + ", mCleanType = " + CleanType.values()[((Integer) killTypeList.get(i)).intValue()]);
                    i++;
                }
            }
        });
        mParams.pw.println("  request sended ! please grep AwareLog for result!");
    }

    private static void dumpPGClean(Params mParams) {
        if (mParams.args.length < 5) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgName = getStrList(mParams);
        List<Integer> userId = getIntList(mParams.args[3]);
        int level = Integer.parseInt(mParams.args[4]);
        if (pkgName == null || userId == null) {
            mParams.pw.println("  bad param, str int must have same size and not null!");
            return;
        }
        MultiTaskManager.getInstance().requestAppCleanFromPG(pkgName, userId, level, "DumpSys");
        mParams.pw.println("  request sended ! please grep AwareLog for result!");
    }

    private static List<String> getStrList(Params mParams) {
        String strList = mParams.args[2];
        if (strList == null) {
            return null;
        }
        return Arrays.asList(strList.split(","));
    }

    private static List<Integer> getIntList(String intList) {
        if (intList == null) {
            return null;
        }
        String[] list = intList.split(",");
        ArrayList<Integer> result = new ArrayList();
        for (String parseInt : list) {
            result.add(Integer.valueOf(Integer.parseInt(parseInt)));
        }
        return result;
    }

    private static void dumpCrashClean(Params mParams) {
        if (5 != mParams.args.length) {
            mParams.pw.println("  please check your param number!");
            return;
        }
        String packageName = mParams.args[2];
        try {
            int userid = Integer.parseInt(mParams.args[3]);
            int level = Integer.parseInt(mParams.args[4]);
            if (AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userid).isEmpty()) {
                mParams.pw.println("  the application is not alive!");
                return;
            }
            CrashClean crashClean = new CrashClean(userid, level, packageName, mParams.context);
            crashClean.clean();
            if (crashClean.getCleanCount() == 0) {
                mParams.pw.println("  CrashClean can't clean package: " + mParams.args[2]);
            } else {
                mParams.pw.println("  CrashClean forcestop package: " + mParams.args[2]);
                mParams.pw.println("  killed " + crashClean.getCleanCount() + " processes");
            }
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    private static void dumpAppStatus(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        ArrayList<AwareProcessInfo> fakeAwareProcList = AwareProcessInfo.getAwareProcInfosFromPackage(mParams.args[2], Integer.parseInt(mParams.args[3]));
        if (fakeAwareProcList.isEmpty()) {
            mParams.pw.println("  the application is not alive!");
            return;
        }
        Status[] allStatus = Status.values();
        mParams.pw.println("  matched status: ");
        for (int i = 1; i < allStatus.length; i++) {
            for (AwareProcessInfo fakeAwareProc : fakeAwareProcList) {
                if (AppStatusUtils.getInstance().checkAppStatus(allStatus[i], fakeAwareProc)) {
                    mParams.pw.println("    " + allStatus[i].toString());
                    break;
                }
            }
        }
    }

    private static void clean(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 4) {
                pw.println("  clean parameter error!");
                return;
            }
            pw.println("  AppMng clean process/package/task :");
            String cleanType = args[2];
            int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
            int pid;
            if ("removetask".equals(cleanType)) {
                AwareProcessInfo fakeAwareProc = (AwareProcessInfo) AwareProcessInfo.getAwareProcInfosFromTask(Integer.parseInt(args[3]), currentUserId).get(0);
                AwareProcessBlockInfo fakeAwareProcBlock = new AwareProcessBlockInfo(fakeAwareProc.mProcInfo.mUid);
                fakeAwareProcBlock.add(fakeAwareProc);
                ProcessCleaner.getInstance(mContext).removetask(fakeAwareProcBlock);
                pw.println("  remove task:" + fakeAwareProc.mTaskId);
            } else if ("kill-allow-start".equals(cleanType)) {
                pid = Integer.parseInt(args[3]);
                ProcessCleaner.getInstance(mContext).killProcess(pid, true, "dump-kill-allow-start");
                pw.println("  kill process:" + pid + ", allow restart");
            } else if ("kill-forbid-start".equals(cleanType)) {
                pid = Integer.parseInt(args[3]);
                ProcessCleaner.getInstance(mContext).killProcess(pid, false, "dump-kill-forbid-start");
                pw.println("  kill process:" + pid + ", forbid restart");
            } else if ("force-stop".equals(cleanType)) {
                if (ProcessCleaner.getInstance(mContext).forcestopAppsAsUser((AwareProcessInfo) AwareProcessInfo.getAwareProcInfosFromPackage(args[3], currentUserId).get(0))) {
                    pw.println("  force-stop package:" + args[3]);
                } else {
                    pw.println("  force-stop package:" + args[3] + " failed!");
                }
            } else {
                pw.println("  bad clean type!");
            }
        }
    }

    private static void dumpDecide(Params mParams) {
        if (mParams.args.length < 7) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        String packageName = mParams.args[2];
        int userid = Integer.parseInt(mParams.args[3]);
        int level = Integer.parseInt(mParams.args[4]);
        int feature = Integer.parseInt(mParams.args[5]);
        int source = Integer.parseInt(mParams.args[6]);
        ArrayList<AwareProcessInfo> proclist = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userid);
        long start = System.nanoTime();
        EnumWithDesc config = null;
        if (feature < 0 || feature >= AppMngFeature.values().length) {
            mParams.pw.println("  Bad command: invalid feature!");
            return;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues()[AppMngFeature.values()[feature].ordinal()]) {
            case 1:
                config = AppCleanSource.values()[source];
                break;
            case 2:
                config = AppFreezeSource.values()[source];
                break;
        }
        if (config != null) {
            List<AwareProcessBlockInfo> resultInfo = DecisionMaker.getInstance().decideAll(proclist, level, AppMngFeature.values()[feature], config);
            mParams.pw.println("  time consume = " + (System.nanoTime() - start));
            for (AwareProcessBlockInfo info : resultInfo) {
                mParams.pw.println("  pkg = " + info.mPackageName + ", uid = " + info.mUid + ", policy = " + info.mCleanType + ", " + config.getDesc() + ", reason = " + info.mReason);
            }
        } else {
            mParams.pw.println("  get the config name error");
        }
    }

    private static void dumpHistory(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        DecisionMaker.getInstance().dumpHistory(mParams.pw, AppCleanSource.values()[Integer.parseInt(mParams.args[2])]);
    }

    private static void dumpBigData(Params mParams) {
        AppCleanupDumpRadar.getInstance().dumpBigData(mParams.pw);
    }

    private static void dumpAppType(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        mParams.pw.println("  App Type = " + AwareIntelligentRecg.getInstance().getAppMngSpecType(mParams.args[2]));
    }

    private static void help(Params mParams) {
        mParams.pw.println("  PGClean [pkgName-1,...,pkgName-n] [userId-1,...,userId-n] [level]");
        mParams.pw.println("  dumpDecide [pkgName] [userId] [level] [feature] [source]");
        mParams.pw.println("  CheckStatus [pkgName] [userId]");
    }
}
