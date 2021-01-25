package com.huawei.android.internal.util;

import android.os.Handler;
import com.android.internal.util.DumpUtils;
import java.io.PrintWriter;

public class DumpUtilsEx {
    private DumpUtilsEx() {
    }

    public static void dumpAsync(Handler handler, DumpEx dumpEx, PrintWriter pw, String prefix, long timeout) {
        DumpUtils.dumpAsync(handler, dumpEx.getDump(), pw, prefix, timeout);
    }

    /* access modifiers changed from: private */
    public static class DumpBridge implements DumpUtils.Dump {
        private DumpEx mDumpEx;

        private DumpBridge() {
        }

        public void setDumpEx(DumpEx dumpEx) {
            this.mDumpEx = dumpEx;
        }

        public void dump(PrintWriter pw, String prefix) {
            DumpEx dumpEx = this.mDumpEx;
            if (dumpEx != null) {
                dumpEx.dump(pw, prefix);
            }
        }
    }

    public static class DumpEx {
        private DumpBridge mBridge = new DumpBridge();

        public DumpEx() {
            this.mBridge.setDumpEx(this);
        }

        public DumpUtils.Dump getDump() {
            return this.mBridge;
        }

        public void dump(PrintWriter pw, String prefix) {
        }
    }
}
