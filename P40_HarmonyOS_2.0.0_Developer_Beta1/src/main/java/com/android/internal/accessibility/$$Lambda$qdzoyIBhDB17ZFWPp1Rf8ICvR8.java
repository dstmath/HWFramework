package com.android.internal.accessibility;

import android.speech.tts.TextToSpeech;
import java.util.function.Consumer;

/* renamed from: com.android.internal.accessibility.-$$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICv-R8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICvR8 implements Consumer {
    public static final /* synthetic */ $$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICvR8 INSTANCE = new $$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICvR8();

    private /* synthetic */ $$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICvR8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TextToSpeech) obj).shutdown();
    }
}
