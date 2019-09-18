package com.android.server.rms.dump;

import com.android.server.rms.dump.DumpAlarmManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.rms.dump.-$$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA INSTANCE = new $$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA();

    private /* synthetic */ $$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA() {
    }

    public final void accept(Object obj) {
        DumpAlarmManager.dumpDebugLog(((DumpAlarmManager.Params) obj).context, ((DumpAlarmManager.Params) obj).pw, ((DumpAlarmManager.Params) obj).args);
    }
}
