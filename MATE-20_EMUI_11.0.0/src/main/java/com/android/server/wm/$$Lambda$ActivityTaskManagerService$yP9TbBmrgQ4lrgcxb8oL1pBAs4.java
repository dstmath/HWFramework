package com.android.server.wm;

import android.content.res.Configuration;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb-8oL1pBAs4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4 INSTANCE = new $$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((ActivityTaskManagerService) obj).sendPutConfigurationForUserMsg(((Integer) obj2).intValue(), (Configuration) obj3);
    }
}
