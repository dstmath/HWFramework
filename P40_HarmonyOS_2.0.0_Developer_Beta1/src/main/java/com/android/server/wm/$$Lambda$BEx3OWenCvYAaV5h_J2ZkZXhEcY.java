package com.android.server.wm;

import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$BEx3OWenCvYAaV5h_J2ZkZXhEcY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BEx3OWenCvYAaV5h_J2ZkZXhEcY implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$BEx3OWenCvYAaV5h_J2ZkZXhEcY INSTANCE = new $$Lambda$BEx3OWenCvYAaV5h_J2ZkZXhEcY();

    private /* synthetic */ $$Lambda$BEx3OWenCvYAaV5h_J2ZkZXhEcY() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((WindowProcessListener) obj).updateProcessInfo(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue(), ((Boolean) obj4).booleanValue());
    }
}
