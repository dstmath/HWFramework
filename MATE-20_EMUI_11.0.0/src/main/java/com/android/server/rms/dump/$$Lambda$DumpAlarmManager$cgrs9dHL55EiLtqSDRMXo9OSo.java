package com.android.server.rms.dump;

import com.android.server.rms.dump.DumpAlarmManager;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.rms.dump.-$$Lambda$DumpAlarmManager$cgr-s9dHL-55EiLtqSDRMXo9OSo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAlarmManager$cgrs9dHL55EiLtqSDRMXo9OSo implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAlarmManager$cgrs9dHL55EiLtqSDRMXo9OSo INSTANCE = new $$Lambda$DumpAlarmManager$cgrs9dHL55EiLtqSDRMXo9OSo();

    private /* synthetic */ $$Lambda$DumpAlarmManager$cgrs9dHL55EiLtqSDRMXo9OSo() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        AwareWakeUpManager.getInstance().dumpParam(((DumpAlarmManager.Params) obj).pw);
    }
}
