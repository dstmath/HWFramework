package com.android.server.autofill;

import android.content.IntentSender;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.autofill.-$$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc INSTANCE = new $$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc();

    private /* synthetic */ $$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Session) obj).doStartIntentSender((IntentSender) obj2);
    }
}
