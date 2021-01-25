package com.android.server.mtm.dump;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import com.android.server.mtm.iaware.appmng.CloudPushManager;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAppSrMng {
    private static final Object LOCK = new Object();
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
            if (args == null || args.length < 3 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (LOCK) {
                if (sConsumers.size() == 0) {
                    sConsumers.put("dump_rules", $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk.INSTANCE);
                    sConsumers.put("update_rules", $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY.INSTANCE);
                    sConsumers.put("appstart", $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg.INSTANCE);
                    sConsumers.put("dump_list", $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko.INSTANCE);
                }
                Consumer<Params> func = sConsumers.get(cmd);
                if (func == null) {
                    pw.println("  Bad command: " + cmd);
                    return;
                }
                try {
                    func.accept(new Params(context, pw, args));
                } catch (ArrayIndexOutOfBoundsException e) {
                    pw.println("  Bad command:");
                    pw.println(e.toString());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpDumpRules(Params params) {
        String cmd = params.args[2];
        if ("clean".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.APP_CLEAN);
        } else if ("start".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.APP_START);
        } else if ("freeze".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.APP_FREEZE);
        } else if ("iolimit".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.APP_IOLIMIT);
        } else if ("cpulimit".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.APP_CPULIMIT);
        } else if ("broadcast".equals(cmd)) {
            DecisionMaker.getInstance().dump(params.pw, AppMngConstant.AppMngFeature.BROADCAST);
        } else if (!"rawcfg".equals(cmd)) {
        } else {
            if (params.args.length < 5) {
                params.pw.println("  Bad command: need more args!");
                return;
            }
            ArrayList<String> result = DecisionMaker.getInstance().getRawConfig(params.args[3], params.args[4]);
            if (result == null) {
                params.pw.println("raw config of your type is null");
                return;
            }
            Iterator<String> it = result.iterator();
            while (it.hasNext()) {
                params.pw.println(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpUpdateRules(Params params) {
        String cmd = params.args[2];
        if ("clean".equals(cmd)) {
            long start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CLEAN, params.context);
            long end = System.nanoTime();
            PrintWriter printWriter = params.pw;
            printWriter.println("time consume update appclean = " + (end - start));
        } else if ("start".equals(cmd)) {
            long start2 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_START, params.context);
            long end2 = System.nanoTime();
            PrintWriter printWriter2 = params.pw;
            printWriter2.println("time consume update appconfig = " + (end2 - start2));
        } else if ("freeze".equals(cmd)) {
            long start3 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_FREEZE, params.context);
            long end3 = System.nanoTime();
            PrintWriter printWriter3 = params.pw;
            printWriter3.println("time consume update appfree = " + (end3 - start3));
        } else if ("iolimit".equals(cmd)) {
            long start4 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_IOLIMIT, params.context);
            long end4 = System.nanoTime();
            PrintWriter printWriter4 = params.pw;
            printWriter4.println("time consume update appIoLimit = " + (end4 - start4));
        } else if ("cpulimit".equals(cmd)) {
            long start5 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CPULIMIT, params.context);
            long end5 = System.nanoTime();
            PrintWriter printWriter5 = params.pw;
            printWriter5.println("time consume update appCpuLimit = " + (end5 - start5));
        } else if ("broadcast".equals(cmd)) {
            long start6 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, params.context);
            long end6 = System.nanoTime();
            PrintWriter printWriter6 = params.pw;
            printWriter6.println("time consume update broadcast = " + (end6 - start6));
        } else if ("updateappmngconfig".equals(cmd)) {
            long start7 = System.nanoTime();
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.updateAppMngConfig();
            }
            long end7 = System.nanoTime();
            PrintWriter printWriter7 = params.pw;
            printWriter7.println("time consume update appconfig = " + (end7 - start7));
        }
    }

    /* access modifiers changed from: private */
    public static void dumpAppStart(Params params) {
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            policy.dump(params.pw, params.args);
        }
    }

    /* access modifiers changed from: private */
    public static void dumpList(Params params) {
        String cmd = params.args[2];
        if ("cloud_push_data".equals(cmd)) {
            CloudPushManager.getInstance().dump(params.pw);
        } else if ("clean".equals(cmd)) {
            DecisionMaker.getInstance().dumpList(params.pw, AppMngConstant.AppMngFeature.APP_CLEAN);
        }
    }
}
