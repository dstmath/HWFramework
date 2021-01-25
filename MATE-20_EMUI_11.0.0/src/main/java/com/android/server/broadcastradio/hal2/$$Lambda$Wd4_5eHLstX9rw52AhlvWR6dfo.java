package com.android.server.broadcastradio.hal2;

import android.hardware.radio.ProgramSelector;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$Wd4_5eHLstX9rw-52AhlvWR6dfo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Wd4_5eHLstX9rw52AhlvWR6dfo implements Function {
    public static final /* synthetic */ $$Lambda$Wd4_5eHLstX9rw52AhlvWR6dfo INSTANCE = new $$Lambda$Wd4_5eHLstX9rw52AhlvWR6dfo();

    private /* synthetic */ $$Lambda$Wd4_5eHLstX9rw52AhlvWR6dfo() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Convert.programIdentifierToHal((ProgramSelector.Identifier) obj);
    }
}
