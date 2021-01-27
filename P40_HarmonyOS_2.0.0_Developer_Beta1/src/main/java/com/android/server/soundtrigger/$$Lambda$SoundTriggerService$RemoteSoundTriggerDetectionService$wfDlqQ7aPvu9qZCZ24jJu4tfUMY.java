package com.android.server.soundtrigger;

import com.android.server.soundtrigger.SoundTriggerService;
import java.util.function.Consumer;

/* renamed from: com.android.server.soundtrigger.-$$Lambda$SoundTriggerService$RemoteSoundTriggerDetectionService$wfDlqQ7aPvu9qZCZ24jJu4tfUMY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SoundTriggerService$RemoteSoundTriggerDetectionService$wfDlqQ7aPvu9qZCZ24jJu4tfUMY implements Consumer {
    public static final /* synthetic */ $$Lambda$SoundTriggerService$RemoteSoundTriggerDetectionService$wfDlqQ7aPvu9qZCZ24jJu4tfUMY INSTANCE = new $$Lambda$SoundTriggerService$RemoteSoundTriggerDetectionService$wfDlqQ7aPvu9qZCZ24jJu4tfUMY();

    private /* synthetic */ $$Lambda$SoundTriggerService$RemoteSoundTriggerDetectionService$wfDlqQ7aPvu9qZCZ24jJu4tfUMY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((SoundTriggerService.RemoteSoundTriggerDetectionService) obj).stopAllPendingOperations();
    }
}
