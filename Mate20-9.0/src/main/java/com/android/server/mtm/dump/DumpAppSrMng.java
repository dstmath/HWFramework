package com.android.server.mtm.dump;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAppSrMng {
    private static volatile Map<String, Consumer<Params>> consumers = new HashMap();

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
            if (args == null || args.length < 3 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (DumpAppSrMng.class) {
                if (consumers.size() == 0) {
                    consumers.put("dump_rules", $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk.INSTANCE);
                    consumers.put("update_rules", $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY.INSTANCE);
                    consumers.put("appstart", $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg.INSTANCE);
                    consumers.put("dump_list", $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko.INSTANCE);
                }
                Consumer<Params> func = consumers.get(cmd);
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
    public static void dumpDumpRules(Params mParams) {
        if ("clean".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.APP_CLEAN);
        } else if ("start".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.APP_START);
        } else if ("freeze".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.APP_FREEZE);
        } else if ("iolimit".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.APP_IOLIMIT);
        } else if ("cpulimit".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.APP_CPULIMIT);
        } else if ("broadcast".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngConstant.AppMngFeature.BROADCAST);
        } else if ("rawcfg".equals(mParams.args[2])) {
            if (mParams.args.length < 5) {
                mParams.pw.println("  Bad command: need more args!");
                return;
            }
            ArrayList<String> result = DecisionMaker.getInstance().getRawConfig(mParams.args[3], mParams.args[4]);
            if (result == null) {
                mParams.pw.println("raw config of your type is null");
                return;
            }
            Iterator<String> it = result.iterator();
            while (it.hasNext()) {
                mParams.pw.println(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpUpdateRules(Params mParams) {
        if ("clean".equals(mParams.args[2])) {
            long start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CLEAN, mParams.context);
            long end = System.nanoTime();
            PrintWriter printWriter = mParams.pw;
            printWriter.println("time consume update appclean = " + (end - start));
        } else if ("start".equals(mParams.args[2])) {
            long start2 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_START, mParams.context);
            long end2 = System.nanoTime();
            PrintWriter printWriter2 = mParams.pw;
            printWriter2.println("time consume update appconfig = " + (end2 - start2));
        } else if ("freeze".equals(mParams.args[2])) {
            long start3 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_FREEZE, mParams.context);
            long end3 = System.nanoTime();
            PrintWriter printWriter3 = mParams.pw;
            printWriter3.println("time consume update appfree = " + (end3 - start3));
        } else if ("iolimit".equals(mParams.args[2])) {
            long start4 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_IOLIMIT, mParams.context);
            long end4 = System.nanoTime();
            PrintWriter printWriter4 = mParams.pw;
            printWriter4.println("time consume update appIoLimit = " + (end4 - start4));
        } else if ("cpulimit".equals(mParams.args[2])) {
            long start5 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CPULIMIT, mParams.context);
            long end5 = System.nanoTime();
            PrintWriter printWriter5 = mParams.pw;
            printWriter5.println("time consume update appCpuLimit = " + (end5 - start5));
        } else if ("broadcast".equals(mParams.args[2])) {
            long start6 = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, mParams.context);
            long end6 = System.nanoTime();
            PrintWriter printWriter6 = mParams.pw;
            printWriter6.println("time consume update broadcast = " + (end6 - start6));
        } else if ("cloudupdate".equals(mParams.args[2])) {
            long start7 = System.nanoTime();
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.updateCloudPolicy("");
            }
            long end7 = System.nanoTime();
            PrintWriter printWriter7 = mParams.pw;
            printWriter7.println("time consume update appconfig = " + (end7 - start7));
        }
    }

    /* access modifiers changed from: private */
    public static void dumpAppStart(Params mParams) {
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            policy.dump(mParams.pw, mParams.args);
        }
    }

    /* access modifiers changed from: private */
    public static void dumpList(Params mParams) {
        if ("clean".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dumpList(mParams.pw, AppMngConstant.AppMngFeature.APP_CLEAN);
        }
    }
}
