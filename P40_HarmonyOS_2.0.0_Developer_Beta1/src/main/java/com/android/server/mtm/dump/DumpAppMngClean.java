package com.android.server.mtm.dump;

import android.app.mtm.MultiTaskManager;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CrashClean;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAppMngClean {
    private static final Object LOCK = new Object();
    private static final String TAG = "DumpAppMngClean";
    private static volatile Map<String, Consumer<Params>> sConsumers = new HashMap();

    /* access modifiers changed from: package-private */
    public static class Params {
        public String[] args;
        public Context context;
        public PrintWriter pw;

        public Params(Context context2, PrintWriter pw2, String[] args2) {
            this.context = context2;
            this.pw = pw2;
            this.args = args2;
        }
    }

    public static void dump(Context context, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 2 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (LOCK) {
                dumpInfos(context, pw, args);
                Consumer<Params> func = sConsumers.get(cmd);
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
        }
    }

    private static void dumpInfos(Context context, PrintWriter pw, String[] arg) {
        if (sConsumers.isEmpty()) {
            sConsumers.put("clean", $$Lambda$DumpAppMngClean$2XeGQhnFySZMuwgGMTUicbZ3jDI.INSTANCE);
            sConsumers.put("dumpPackage", $$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30MXPM.INSTANCE);
            sConsumers.put("dumpPackageList", $$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE.INSTANCE);
            sConsumers.put("dumpTask", $$Lambda$DumpAppMngClean$o_1JixNN0q39P2WLeS0DF2UtcxY.INSTANCE);
            sConsumers.put("SMClean", $$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4.INSTANCE);
            sConsumers.put("SMQuery", $$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw.INSTANCE);
            sConsumers.put("PGClean", $$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5kyDqDdmcPo4.INSTANCE);
            sConsumers.put("ThermalClean", $$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg.INSTANCE);
            sConsumers.put("CrashClean", $$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8.INSTANCE);
            sConsumers.put("CheckStatus", $$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk.INSTANCE);
            sConsumers.put("dumpDecide", $$Lambda$DumpAppMngClean$z3LUvhJ4fQIs7XnDNN0AFtQQs.INSTANCE);
            sConsumers.put("dumpHistory", $$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8.INSTANCE);
            sConsumers.put("help", $$Lambda$DumpAppMngClean$1yvzFnXpdAsUB2eq8fWiSUbQ7L0.INSTANCE);
            sConsumers.put("dumpBigData", $$Lambda$DumpAppMngClean$DbFdmxSYh6HguuZkrpQSJhVuWxg.INSTANCE);
            dumpInfosEx(context, pw, arg);
        }
    }

    private static void dumpInfosEx(Context context, PrintWriter pw, String[] arg) {
        sConsumers.put("dumpAppType", $$Lambda$DumpAppMngClean$v6LUh_c8wsrLuN80WW8bVGmnkd0.INSTANCE);
        sConsumers.put("CheckProcStatus", $$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c.INSTANCE);
    }

    /* access modifiers changed from: private */
    public static void dumpPackage(Params params) {
        if (params.args.length < 4) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackage(params.args[2], Integer.parseInt(params.args[3])), params.pw);
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpPackageList(Params params) {
        if (params.args.length < 3) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        String[] packList = params.args[2].split(",");
        ArrayMap<String, Integer> packMap = new ArrayMap<>();
        for (String str : packList) {
            String[] pack = str.split(":");
            params.pw.println("  package name: " + pack[0] + ", uid: " + pack[1]);
            try {
                packMap.put(pack[0], Integer.valueOf(Integer.parseInt(pack[1])));
            } catch (NumberFormatException e) {
                params.pw.println("  please check your param!");
                return;
            }
        }
        ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackageMap(packMap), params.pw);
    }

    /* access modifiers changed from: private */
    public static void dumpTask(Params params) {
        if (params.args.length < 4) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromTask(Integer.parseInt(params.args[2]), Integer.parseInt(params.args[3])), params.pw);
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpSMClean(Params params) {
        if (params.args.length < 5) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgNames = getStrList(params);
        List<Integer> userIds = getIntList(params.args[3]);
        List<Integer> killTypes = getIntList(params.args[4]);
        if (!(pkgNames == null || userIds == null || killTypes == null)) {
            List<AppCleanParam.AppCleanInfo> appCleanInfoList = new ArrayList<>();
            int size = pkgNames.size();
            for (int i = 0; i < size; i++) {
                appCleanInfoList.add(new AppCleanParam.AppCleanInfo(pkgNames.get(i), userIds.get(i), killTypes.get(i)));
            }
            MultiTaskManager.getInstance().executeMultiAppClean(appCleanInfoList, new IAppCleanCallback.Stub() {
                /* class com.android.server.mtm.dump.DumpAppMngClean.AnonymousClass1 */

                public void onCleanFinish(AppCleanParam result) {
                    AwareLog.i(DumpAppMngClean.TAG, "DumpResult onCleanFinish:" + result);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static void getSMCleanList(Params params) {
        MultiTaskManager.getInstance().getAppListForUserClean(new IAppCleanCallback.Stub() {
            /* class com.android.server.mtm.dump.DumpAppMngClean.AnonymousClass2 */

            public void onCleanFinish(AppCleanParam result) {
                List<String> pkgList = result.getStringList();
                List<Integer> uidList = result.getIntList();
                List<Integer> killTypeList = result.getIntListEx();
                AwareLog.i(DumpAppMngClean.TAG, "SMQuery callback called, size = " + pkgList.size() + ", in Thread: " + Thread.currentThread().getName());
                int i = 0;
                int len = pkgList.size();
                while (i < len) {
                    int uid = i < uidList.size() ? uidList.get(i).intValue() : -1;
                    AwareLog.i(DumpAppMngClean.TAG, "SMQuery pkg = " + pkgList.get(i) + ", uid: " + uid + ", mCleanType = " + ProcessCleaner.CleanType.values()[killTypeList.get(i).intValue()]);
                    i++;
                }
            }
        });
        params.pw.println("  request sended ! please grep AwareLog for result!");
    }

    /* access modifiers changed from: private */
    public static void dumpPGClean(Params params) {
        if (params.args.length < 5) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgName = getStrList(params);
        List<Integer> userId = getIntList(params.args[3]);
        try {
            int level = Integer.parseInt(params.args[4]);
            if (pkgName == null || userId == null) {
                params.pw.println("  bad param, str int must have same size and not null!");
                return;
            }
            MultiTaskManager.getInstance().requestAppCleanFromPG(pkgName, userId, level, "DumpSys");
            params.pw.println("  request sended ! please grep AwareLog for result!");
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpThermalClean(Params params) {
        if (params.args.length < 5) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgName = getStrList(params);
        List<Integer> userId = getIntList(params.args[3]);
        try {
            int level = Integer.parseInt(params.args[4]);
            if (pkgName == null || userId == null) {
                params.pw.println("  bad param, str int must have same size and not null!");
                return;
            }
            MultiTaskManager.getInstance().requestAppClean(pkgName, userId, level, "DumpSys", 7);
            params.pw.println("  request sended ! please grep AwareLog for result!");
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    private static List<String> getStrList(Params params) {
        String strList = params.args[2];
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
        ArrayList<Integer> result = new ArrayList<>();
        for (String str : list) {
            try {
                result.add(Integer.valueOf(Integer.parseInt(str)));
            } catch (NumberFormatException e) {
                return result;
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public static void dumpCrashClean(Params params) {
        if (params.args.length != 5) {
            params.pw.println("  please check your param number!");
            return;
        }
        String packageName = params.args[2];
        try {
            int userid = Integer.parseInt(params.args[3]);
            int level = Integer.parseInt(params.args[4]);
            if (AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userid).isEmpty()) {
                params.pw.println("  the application is not alive!");
                return;
            }
            CrashClean crashClean = new CrashClean(userid, level, packageName, params.context);
            crashClean.clean();
            if (crashClean.getCleanCount() == 0) {
                PrintWriter printWriter = params.pw;
                printWriter.println("  CrashClean can't clean package: " + params.args[2]);
                return;
            }
            PrintWriter printWriter2 = params.pw;
            printWriter2.println("  CrashClean forcestop package: " + params.args[2]);
            PrintWriter printWriter3 = params.pw;
            printWriter3.println("  killed " + crashClean.getCleanCount() + " processes");
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpAppStatus(Params params) {
        if (params.args.length < 4) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ArrayList<AwareProcessInfo> fakeAwareProcList = AwareProcessInfo.getAwareProcInfosFromPackage(params.args[2], Integer.parseInt(params.args[3]));
            if (fakeAwareProcList.isEmpty()) {
                params.pw.println("  the application is not alive!");
                return;
            }
            AppStatusUtils.Status[] allStatus = AppStatusUtils.Status.values();
            params.pw.println("  matched status: ");
            for (int i = 1; i < allStatus.length; i++) {
                Iterator<AwareProcessInfo> it = fakeAwareProcList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    if (AppStatusUtils.getInstance().checkAppStatus(allStatus[i], it.next())) {
                        PrintWriter printWriter = params.pw;
                        printWriter.println("    " + allStatus[i].toString());
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void clean(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 4) {
                pw.println("  clean parameter error!");
                return;
            }
            pw.println("  AppMng clean process/package/task :");
            String cleanType = args[2];
            int currentUserId = AwareAppAssociate.getInstance().getCurUserId();
            try {
                if ("removetask".equals(cleanType)) {
                    AwareProcessInfo fakeAwareProc = AwareProcessInfo.getAwareProcInfosFromTask(Integer.parseInt(args[3]), currentUserId).get(0);
                    AwareProcessBlockInfo fakeAwareProcBlock = new AwareProcessBlockInfo(fakeAwareProc.procProcInfo.mUid);
                    fakeAwareProcBlock.add(fakeAwareProc);
                    ProcessCleaner.getInstance(mContext).removeTask(fakeAwareProcBlock);
                    pw.println("  remove task:" + fakeAwareProc.procTaskId);
                } else if ("kill-allow-start".equals(cleanType)) {
                    int pid = Integer.parseInt(args[3]);
                    ProcessCleaner.getInstance(mContext).killProcess(pid, true, "dump-kill-allow-start");
                    pw.println("  kill process:" + pid + ", allow restart");
                } else if ("kill-forbid-start".equals(cleanType)) {
                    int pid2 = Integer.parseInt(args[3]);
                    ProcessCleaner.getInstance(mContext).killProcess(pid2, false, "dump-kill-forbid-start");
                    pw.println("  kill process:" + pid2 + ", forbid restart");
                } else if ("force-stop".equals(cleanType)) {
                    if (ProcessCleaner.getInstance(mContext).forceStopAppsAsUser(AwareProcessInfo.getAwareProcInfosFromPackage(args[3], currentUserId).get(0), "Dump")) {
                        pw.println("  force-stop package:" + args[3]);
                        return;
                    }
                    pw.println("  force-stop package:" + args[3] + " failed!");
                } else {
                    pw.println("  bad clean type!");
                }
            } catch (NumberFormatException e) {
                pw.println("  please check your param!");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpDecide(Params params) {
        if (params.args.length < 7) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        String packageName = params.args[2];
        try {
            int userid = Integer.parseInt(params.args[3]);
            int level = Integer.parseInt(params.args[4]);
            int feature = Integer.parseInt(params.args[5]);
            int source = Integer.parseInt(params.args[6]);
            ArrayList<AwareProcessInfo> procList = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userid);
            long start = System.nanoTime();
            AppMngConstant.EnumWithDesc config = null;
            if (feature >= 0) {
                if (feature < AppMngConstant.AppMngFeature.values().length) {
                    int i = AnonymousClass3.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.values()[feature].ordinal()];
                    if (i == 1) {
                        config = AppMngConstant.AppCleanSource.values()[source];
                    } else if (i == 2) {
                        config = AppMngConstant.AppFreezeSource.values()[source];
                    }
                    if (config != null) {
                        List<AwareProcessBlockInfo> resultInfo = DecisionMaker.getInstance().decideAll(procList, level, AppMngConstant.AppMngFeature.values()[feature], config);
                        long end = System.nanoTime();
                        PrintWriter printWriter = params.pw;
                        printWriter.println("  time consume = " + (end - start));
                        for (AwareProcessBlockInfo info : resultInfo) {
                            PrintWriter printWriter2 = params.pw;
                            printWriter2.println("  pkg = " + info.procPackageName + ", uid = " + info.procUid + ", policy = " + info.procCleanType + ", " + config.getDesc() + ", reason = " + info.procReason);
                            userid = userid;
                        }
                        return;
                    }
                    params.pw.println("  get the config name error");
                    return;
                }
            }
            params.pw.println("  Bad command: invalid feature!");
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.mtm.dump.DumpAppMngClean$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature = new int[AppMngConstant.AppMngFeature.values().length];

        static {
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_CLEAN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_FREEZE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpHistory(Params params) {
        if (params.args.length < 3) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            DecisionMaker.getInstance().dumpHistory(params.pw, AppMngConstant.AppCleanSource.values()[Integer.parseInt(params.args[2])]);
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpBigData(Params params) {
        AppCleanupDumpRadar.getInstance().dumpBigData(params.pw);
    }

    /* access modifiers changed from: private */
    public static void dumpAppType(Params params) {
        if (params.args.length < 3) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        int appType = AwareIntelligentRecg.getInstance().getAppMngSpecType(params.args[2]);
        PrintWriter printWriter = params.pw;
        printWriter.println("  App Type = " + appType);
    }

    /* access modifiers changed from: private */
    public static void help(Params params) {
        params.pw.println("  PGClean [pkgName-1,...,pkgName-n] [userId-1,...,userId-n] [level]");
        params.pw.println("  dumpDecide [pkgName] [userId] [level] [feature] [source]");
        params.pw.println("  CheckStatus [pkgName] [userId]");
        params.pw.println("  CheckProcStatus [pid]");
    }

    /* access modifiers changed from: private */
    public static void dumpProcStatus(Params params) {
        if (params.args.length < 3) {
            params.pw.println("  Bad command: need more args!");
            return;
        }
        List<AwareProcessInfo> infos = AwareProcessInfo.getAwareProcInfosList();
        try {
            int pid = Integer.parseInt(params.args[2]);
            AwareProcessInfo awareProcInfo = null;
            for (AwareProcessInfo info : infos) {
                if (info != null && info.procPid == pid) {
                    awareProcInfo = info;
                }
            }
            if (awareProcInfo == null || awareProcInfo.procProcInfo == null) {
                params.pw.println("The pid not found!");
                return;
            }
            PrintWriter printWriter = params.pw;
            printWriter.println("ProcName : " + awareProcInfo.procProcInfo.mProcessName);
            AppStatusUtils.Status[] allStatus = AppStatusUtils.Status.values();
            params.pw.println("  matched status: ");
            for (int index = 1; index < allStatus.length; index++) {
                if (AppStatusUtils.getInstance().checkAppStatus(allStatus[index], awareProcInfo)) {
                    PrintWriter printWriter2 = params.pw;
                    printWriter2.println("    " + allStatus[index].toString());
                }
            }
        } catch (NumberFormatException e) {
            params.pw.println("  please check your param!");
        }
    }
}
