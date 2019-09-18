package com.android.server.rms.dump;

import com.android.server.rms.dump.DumpAlarmManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.rms.dump.-$$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8 INSTANCE = new $$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8();

    private /* synthetic */ $$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8() {
    }

    public final void accept(Object obj) {
        DumpAlarmManager.dumpBigData(((DumpAlarmManager.Params) obj).context, ((DumpAlarmManager.Params) obj).pw, ((DumpAlarmManager.Params) obj).args);
    }
}
