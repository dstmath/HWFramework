package android.hardware.radio;

import android.hardware.radio.ProgramList;
import java.util.function.Consumer;

/* renamed from: android.hardware.radio.-$$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI implements Consumer {
    public static final /* synthetic */ $$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI INSTANCE = new $$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI();

    private /* synthetic */ $$Lambda$ProgramList$GfCj9jJ5znxw2TV4c2uykq35dgI() {
    }

    public final void accept(Object obj) {
        ((ProgramList.OnCompleteListener) obj).onComplete();
    }
}
