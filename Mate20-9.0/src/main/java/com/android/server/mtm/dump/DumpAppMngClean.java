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
    private static final int SINGLE_REMOVE = 1;
    public static final String TAG = "DumpAppMngClean";
    private static volatile Map<String, Consumer<Params>> consumers = new HashMap();

    /* renamed from: com.android.server.mtm.dump.DumpAppMngClean$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
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

    static class Params {
        public String[] args;
        public Context context;
        public PrintWriter pw;

        public Params(Context context2, PrintWriter pw2, String[] args2) {
            this.context = context2;
            this.pw = pw2;
            this.args = args2;
        }
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
                    consumers.put("clean", $$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84.INSTANCE);
                    consumers.put("dumpPackage", $$Lambda$DumpAppMngClean$T_fIfVRl2YBzqRzphMJEA6Y4CdY.INSTANCE);
                    consumers.put("dumpPackageList", $$Lambda$DumpAppMngClean$Jl9_eoadNBc9X2HJLdAfLaJXZ8.INSTANCE);
                    consumers.put("dumpTask", $$Lambda$DumpAppMngClean$lLEQOsNYCeB4AgH1PFdm9jxx05k.INSTANCE);
                    consumers.put("SMClean", $$Lambda$DumpAppMngClean$LSGBU4ssTWtw_zn3EOyvEVqLXc.INSTANCE);
                    consumers.put("SMQuery", $$Lambda$DumpAppMngClean$CM_PgQDlc0bzC2qyQbvMOmEcWiE.INSTANCE);
                    consumers.put("PGClean", $$Lambda$DumpAppMngClean$MivzXvNudTNPkOIRQyMjFdydaQ4.INSTANCE);
                    consumers.put("ThermalClean", $$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels.INSTANCE);
                    consumers.put("CrashClean", $$Lambda$DumpAppMngClean$mGdKgwdx7iIj_qg5QWFmdhbTQ.INSTANCE);
                    consumers.put("CheckStatus", $$Lambda$DumpAppMngClean$Ss1x4RfAzZOs5FTNrQ9ZoYWraTE.INSTANCE);
                    consumers.put("dumpDecide", $$Lambda$DumpAppMngClean$dJzY8ndMU3LBZPZ3gGiDbMvX4.INSTANCE);
                    consumers.put("dumpHistory", $$Lambda$DumpAppMngClean$1tePXixpwElJOAA2KllLRsX2g.INSTANCE);
                    consumers.put("help", $$Lambda$DumpAppMngClean$xO85N5a1Vu3C3lVatQtBcimmd6Y.INSTANCE);
                    consumers.put("dumpBigData", $$Lambda$DumpAppMngClean$p_J8O3iMY9mnYS_Ra5btmJa_hhI.INSTANCE);
                    consumers.put("dumpAppType", $$Lambda$DumpAppMngClean$ZMwkPULrbRfppectXbk35vIFA.INSTANCE);
                }
                Consumer<Params> func = consumers.get(cmd);
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

    /* access modifiers changed from: private */
    public static void dumpPackage(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackage(mParams.args[2], Integer.parseInt(mParams.args[3])), mParams.pw);
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpPackageList(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        String[] packList = mParams.args[2].split(",");
        ArrayMap<String, Integer> packMap = new ArrayMap<>();
        int length = packList.length;
        int i = 0;
        while (i < length) {
            String[] pack = packList[i].split(":");
            mParams.pw.println("  package name: " + pack[0] + ", uid: " + pack[1]);
            try {
                packMap.put(pack[0], Integer.valueOf(Integer.parseInt(pack[1])));
                i++;
            } catch (NumberFormatException e) {
                mParams.pw.println("  please check your param!");
                return;
            }
        }
        ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromPackageMap(packMap), mParams.pw);
    }

    /* access modifiers changed from: private */
    public static void dumpTask(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ProcessInfoCollector.getInstance().dumpPackageTask(ProcessInfoCollector.getInstance().getProcessInfosFromTask(Integer.parseInt(mParams.args[2]), Integer.parseInt(mParams.args[3])), mParams.pw);
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpSMClean(Params mParams) {
        if (mParams.args.length < 5) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgNames = getStrList(mParams);
        List<Integer> userIds = getIntList(mParams.args[3]);
        List<Integer> killTypes = getIntList(mParams.args[4]);
        if (pkgNames != null && userIds != null && killTypes != null) {
            List<AppCleanParam.AppCleanInfo> appCleanInfoList = new ArrayList<>();
            int size = pkgNames.size();
            for (int i = 0; i < size; i++) {
                appCleanInfoList.add(new AppCleanParam.AppCleanInfo(pkgNames.get(i), userIds.get(i), killTypes.get(i)));
            }
            MultiTaskManager.getInstance().executeMultiAppClean(appCleanInfoList, new IAppCleanCallback.Stub() {
                public void onCleanFinish(AppCleanParam result) {
                    AwareLog.i(DumpAppMngClean.TAG, "DumpResult onCleanFinish:" + result);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static void getSMCleanList(Params mParams) {
        MultiTaskManager.getInstance().getAppListForUserClean(new IAppCleanCallback.Stub() {
            public void onCleanFinish(AppCleanParam result) {
                List<String> pkgList = result.getStringList();
                List<Integer> uidList = result.getIntList();
                List<Integer> killTypeList = result.getIntList2();
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
        mParams.pw.println("  request sended ! please grep AwareLog for result!");
    }

    /* access modifiers changed from: private */
    public static void dumpPGClean(Params mParams) {
        if (mParams.args.length < 5) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgName = getStrList(mParams);
        List<Integer> userId = getIntList(mParams.args[3]);
        try {
            int level = Integer.parseInt(mParams.args[4]);
            if (pkgName == null || userId == null) {
                mParams.pw.println("  bad param, str int must have same size and not null!");
                return;
            }
            MultiTaskManager.getInstance().requestAppCleanFromPG(pkgName, userId, level, "DumpSys");
            mParams.pw.println("  request sended ! please grep AwareLog for result!");
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpThermalClean(Params mParams) {
        if (mParams.args.length < 5) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        List<String> pkgName = getStrList(mParams);
        List<Integer> userId = getIntList(mParams.args[3]);
        try {
            int level = Integer.parseInt(mParams.args[4]);
            if (pkgName == null || userId == null) {
                mParams.pw.println("  bad param, str int must have same size and not null!");
                return;
            }
            MultiTaskManager.getInstance().requestAppClean(pkgName, userId, level, "DumpSys", 7);
            mParams.pw.println("  request sended ! please grep AwareLog for result!");
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
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
        ArrayList<Integer> result = new ArrayList<>();
        int i = 0;
        while (i < list.length) {
            try {
                result.add(Integer.valueOf(Integer.parseInt(list[i])));
                i++;
            } catch (NumberFormatException e) {
                return result;
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public static void dumpCrashClean(Params mParams) {
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
                PrintWriter printWriter = mParams.pw;
                printWriter.println("  CrashClean can't clean package: " + mParams.args[2]);
            } else {
                PrintWriter printWriter2 = mParams.pw;
                printWriter2.println("  CrashClean forcestop package: " + mParams.args[2]);
                PrintWriter printWriter3 = mParams.pw;
                printWriter3.println("  killed " + crashClean.getCleanCount() + " processes");
            }
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpAppStatus(Params mParams) {
        if (mParams.args.length < 4) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            ArrayList<AwareProcessInfo> fakeAwareProcList = AwareProcessInfo.getAwareProcInfosFromPackage(mParams.args[2], Integer.parseInt(mParams.args[3]));
            if (fakeAwareProcList.isEmpty()) {
                mParams.pw.println("  the application is not alive!");
                return;
            }
            AppStatusUtils.Status[] allStatus = AppStatusUtils.Status.values();
            mParams.pw.println("  matched status: ");
            for (int i = 1; i < allStatus.length; i++) {
                Iterator<AwareProcessInfo> it = fakeAwareProcList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    if (AppStatusUtils.getInstance().checkAppStatus(allStatus[i], it.next())) {
                        PrintWriter printWriter = mParams.pw;
                        printWriter.println("    " + allStatus[i].toString());
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
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
                    AwareProcessBlockInfo fakeAwareProcBlock = new AwareProcessBlockInfo(fakeAwareProc.mProcInfo.mUid);
                    fakeAwareProcBlock.add(fakeAwareProc);
                    ProcessCleaner.getInstance(mContext).removetask(fakeAwareProcBlock);
                    pw.println("  remove task:" + fakeAwareProc.mTaskId);
                } else if ("kill-allow-start".equals(cleanType)) {
                    int pid = Integer.parseInt(args[3]);
                    ProcessCleaner.getInstance(mContext).killProcess(pid, true, "dump-kill-allow-start");
                    pw.println("  kill process:" + pid + ", allow restart");
                } else if ("kill-forbid-start".equals(cleanType)) {
                    int pid2 = Integer.parseInt(args[3]);
                    ProcessCleaner.getInstance(mContext).killProcess(pid2, false, "dump-kill-forbid-start");
                    pw.println("  kill process:" + pid2 + ", forbid restart");
                } else if ("force-stop".equals(cleanType)) {
                    if (ProcessCleaner.getInstance(mContext).forcestopAppsAsUser(AwareProcessInfo.getAwareProcInfosFromPackage(args[3], currentUserId).get(0), "Dump")) {
                        pw.println("  force-stop package:" + args[3]);
                    } else {
                        pw.println("  force-stop package:" + args[3] + " failed!");
                    }
                } else {
                    pw.println("  bad clean type!");
                }
            } catch (NumberFormatException e) {
                pw.println("  please check your param!");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpDecide(Params mParams) {
        Params params = mParams;
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
            ArrayList<AwareProcessInfo> proclist = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userid);
            long start = System.nanoTime();
            AppMngConstant.EnumWithDesc config = null;
            if (feature < 0) {
                int i = level;
            } else if (feature >= AppMngConstant.AppMngFeature.values().length) {
                String str = packageName;
                int i2 = level;
            } else {
                switch (AnonymousClass3.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.values()[feature].ordinal()]) {
                    case 1:
                        config = AppMngConstant.AppCleanSource.values()[source];
                        break;
                    case 2:
                        config = AppMngConstant.AppFreezeSource.values()[source];
                        break;
                }
                if (config != null) {
                    List<AwareProcessBlockInfo> resultInfo = DecisionMaker.getInstance().decideAll(proclist, level, AppMngConstant.AppMngFeature.values()[feature], config);
                    long end = System.nanoTime();
                    PrintWriter printWriter = params.pw;
                    StringBuilder sb = new StringBuilder();
                    sb.append("  time consume = ");
                    String str2 = packageName;
                    int i3 = level;
                    sb.append(end - start);
                    printWriter.println(sb.toString());
                    for (AwareProcessBlockInfo info : resultInfo) {
                        PrintWriter printWriter2 = params.pw;
                        printWriter2.println("  pkg = " + info.mPackageName + ", uid = " + info.mUid + ", policy = " + info.mCleanType + ", " + config.getDesc() + ", reason = " + info.mReason);
                    }
                } else {
                    int i4 = level;
                    params.pw.println("  get the config name error");
                }
                return;
            }
            params.pw.println("  Bad command: invalid feature!");
        } catch (NumberFormatException e) {
            String str3 = packageName;
            params.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpHistory(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        try {
            DecisionMaker.getInstance().dumpHistory(mParams.pw, AppMngConstant.AppCleanSource.values()[Integer.parseInt(mParams.args[2])]);
        } catch (NumberFormatException e) {
            mParams.pw.println("  please check your param!");
        }
    }

    /* access modifiers changed from: private */
    public static void dumpBigData(Params mParams) {
        AppCleanupDumpRadar.getInstance().dumpBigData(mParams.pw);
    }

    /* access modifiers changed from: private */
    public static void dumpAppType(Params mParams) {
        if (mParams.args.length < 3) {
            mParams.pw.println("  Bad command: need more args!");
            return;
        }
        int appType = AwareIntelligentRecg.getInstance().getAppMngSpecType(mParams.args[2]);
        PrintWriter printWriter = mParams.pw;
        printWriter.println("  App Type = " + appType);
    }

    /* access modifiers changed from: private */
    public static void help(Params mParams) {
        mParams.pw.println("  PGClean [pkgName-1,...,pkgName-n] [userId-1,...,userId-n] [level]");
        mParams.pw.println("  dumpDecide [pkgName] [userId] [level] [feature] [source]");
        mParams.pw.println("  CheckStatus [pkgName] [userId]");
    }
}
