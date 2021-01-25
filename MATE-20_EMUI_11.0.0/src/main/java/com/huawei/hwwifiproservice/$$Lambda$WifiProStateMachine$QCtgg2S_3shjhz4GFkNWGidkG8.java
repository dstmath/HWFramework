package com.huawei.hwwifiproservice;

import android.media.AudioRecordingConfiguration;
import java.util.function.Predicate;

/* renamed from: com.huawei.hwwifiproservice.-$$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG-8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG8 implements Predicate {
    public static final /* synthetic */ $$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG8 INSTANCE = new $$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG8();

    private /* synthetic */ $$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG8() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return WifiProStateMachine.lambda$isRecordingInVoip$0((AudioRecordingConfiguration) obj);
    }
}
