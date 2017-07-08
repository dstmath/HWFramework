package com.android.server.rms.test;

import android.os.Bundle;
import android.os.Process;
import com.android.server.rms.IShrinker;
import com.android.server.rms.shrinker.ProcessShrinker;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.rms.shrinker.SystemShrinker;
import com.android.server.rms.utils.Utils;

public final class TestShrinker {

    /* renamed from: com.android.server.rms.test.TestShrinker.1 */
    static class AnonymousClass1 extends Thread {
        final /* synthetic */ IShrinker val$shrinker;

        AnonymousClass1(String $anonymous0, IShrinker val$shrinker) {
            this.val$shrinker = val$shrinker;
            super($anonymous0);
        }

        public void run() {
            while (true) {
                this.val$shrinker.reclaim("testSystemShrinker", null);
                Utils.wait(5000);
            }
        }
    }

    /* renamed from: com.android.server.rms.test.TestShrinker.2 */
    static class AnonymousClass2 extends Thread {
        final /* synthetic */ ProcessShrinker val$processShrinker;

        AnonymousClass2(String $anonymous0, ProcessShrinker val$processShrinker) {
            this.val$processShrinker = val$processShrinker;
            super($anonymous0);
        }

        public void run() {
            while (true) {
                for (int pid : Process.getPidsForCommands(new String[]{"system_server", "com.android.systemui", "com.android.keyguard"})) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ProcessStopShrinker.PID_KEY, pid);
                    this.val$processShrinker.reclaim("testMemory", bundle);
                }
                Utils.wait(5000);
            }
        }
    }

    public static final void testSystemShrinker() {
        new AnonymousClass1("testSystemShrinker", new SystemShrinker()).start();
    }

    public static final void testProcessShrinker() {
        new AnonymousClass2("testProcessShrinker", new ProcessShrinker(1)).start();
    }
}
