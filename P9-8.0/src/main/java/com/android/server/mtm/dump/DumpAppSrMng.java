package com.android.server.mtm.dump;

import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.content.Context;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAppSrMng {
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

    public static final void dump(Context context, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 3 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (DumpAppSrMng.class) {
                if (consumers.size() == 0) {
                    consumers.put("dump_rules", new -$Lambda$sEA1SlCqedZxuFuuadCGYt4wQWk());
                    consumers.put("update_rules", new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    consumers.put("appstart", new Consumer() {
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
                } catch (ArrayIndexOutOfBoundsException e) {
                    pw.println("  Bad command:");
                    pw.println(e.toString());
                }
            }
        } else {
            return;
        }
        return;
    }

    private static void dumpDumpRules(Params mParams) {
        if ("clean".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngFeature.APP_CLEAN);
        } else if ("start".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngFeature.APP_START);
        } else if ("freeze".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngFeature.APP_FREEZE);
        } else if ("iolimit".equals(mParams.args[2])) {
            DecisionMaker.getInstance().dump(mParams.pw, AppMngFeature.APP_IOLIMIT);
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
            for (String s : result) {
                mParams.pw.println(s);
            }
        }
    }

    private static void dumpUpdateRules(Params mParams) {
        long start;
        if ("clean".equals(mParams.args[2])) {
            start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngFeature.APP_CLEAN, mParams.context);
            mParams.pw.println("time consume update appclean = " + (System.nanoTime() - start));
        } else if ("start".equals(mParams.args[2])) {
            start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngFeature.APP_START, mParams.context);
            mParams.pw.println("time consume update appconfig = " + (System.nanoTime() - start));
        } else if ("freeze".equals(mParams.args[2])) {
            start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngFeature.APP_FREEZE, mParams.context);
            mParams.pw.println("time consume update appfree = " + (System.nanoTime() - start));
        } else if ("iolimit".equals(mParams.args[2])) {
            start = System.nanoTime();
            DecisionMaker.getInstance().updateRule(AppMngFeature.APP_IOLIMIT, mParams.context);
            mParams.pw.println("time consume update appIoLimit = " + (System.nanoTime() - start));
        } else if ("cloudupdate".equals(mParams.args[2])) {
            start = System.nanoTime();
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.updateCloudPolicy("");
            }
            mParams.pw.println("time consume update appconfig = " + (System.nanoTime() - start));
        }
    }

    private static void dumpAppStart(Params mParams) {
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            policy.dump(mParams.pw, mParams.args);
        }
    }
}
