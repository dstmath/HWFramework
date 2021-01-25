package com.android.server.rms.dump;

import com.android.server.rms.dump.DumpAlarmManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.rms.dump.-$$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk INSTANCE = new $$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk();

    private /* synthetic */ $$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAlarmManager.Params params;
        DumpAlarmManager.setDebugSwitch(params.context, params.pw, ((DumpAlarmManager.Params) obj).args);
    }
}
