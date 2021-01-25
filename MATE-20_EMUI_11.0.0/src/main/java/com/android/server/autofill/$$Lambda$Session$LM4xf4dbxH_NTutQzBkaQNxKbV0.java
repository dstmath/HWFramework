package com.android.server.autofill;

import android.content.Intent;
import android.content.IntentSender;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.autofill.-$$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0 INSTANCE = new $$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0();

    private /* synthetic */ $$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((Session) obj).startAuthentication(((Integer) obj2).intValue(), (IntentSender) obj3, (Intent) obj4);
    }
}
