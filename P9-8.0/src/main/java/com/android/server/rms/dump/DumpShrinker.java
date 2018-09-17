package com.android.server.rms.dump;

import android.os.Bundle;
import android.os.Process;
import android.rms.utils.Utils;
import com.android.server.rms.IShrinker;
import com.android.server.rms.shrinker.ProcessShrinker;
import com.android.server.rms.shrinker.SystemShrinker;

public final class DumpShrinker {
    public static final void dumpSystemShrinker() {
        final IShrinker shrinker = new SystemShrinker();
        new Thread("dumpSystemShrinker") {
            public void run() {
                while (true) {
                    shrinker.reclaim("dumpSystemShrinker", null);
                    Utils.wait(5000);
                }
            }
        }.start();
    }

    public static final void dumpProcessShrinker() {
        final ProcessShrinker processShrinker = new ProcessShrinker(1);
        new Thread("dumpProcessShrinker") {
            public void run() {
                while (true) {
                    for (int pid : Process.getPidsForCommands(new String[]{"system_server", "com.android.systemui", "com.android.keyguard"})) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("pid", pid);
                        processShrinker.reclaim("dumpMemory", bundle);
                    }
                    Utils.wait(5000);
                }
            }
        }.start();
    }
}
