package com.android.internal.util;

import android.os.Handler;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class DumpUtils {

    /* renamed from: com.android.internal.util.DumpUtils.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ Dump val$dump;
        final /* synthetic */ String val$prefix;
        final /* synthetic */ StringWriter val$sw;

        AnonymousClass1(StringWriter val$sw, Dump val$dump, String val$prefix) {
            this.val$sw = val$sw;
            this.val$dump = val$dump;
            this.val$prefix = val$prefix;
        }

        public void run() {
            PrintWriter lpw = new FastPrintWriter(this.val$sw);
            this.val$dump.dump(lpw, this.val$prefix);
            lpw.close();
        }
    }

    public interface Dump {
        void dump(PrintWriter printWriter, String str);
    }

    private DumpUtils() {
    }

    public static void dumpAsync(Handler handler, Dump dump, PrintWriter pw, String prefix, long timeout) {
        StringWriter sw = new StringWriter();
        if (handler.runWithScissors(new AnonymousClass1(sw, dump, prefix), timeout)) {
            pw.print(sw.toString());
        } else {
            pw.println("... timed out");
        }
    }
}
