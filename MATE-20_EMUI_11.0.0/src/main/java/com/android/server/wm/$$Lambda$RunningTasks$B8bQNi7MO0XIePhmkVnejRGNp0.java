package com.android.server.wm;

import java.util.Comparator;

/* renamed from: com.android.server.wm.-$$Lambda$RunningTasks$B8bQN-i7MO0XIePhmkVnejRGNp0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RunningTasks$B8bQNi7MO0XIePhmkVnejRGNp0 implements Comparator {
    public static final /* synthetic */ $$Lambda$RunningTasks$B8bQNi7MO0XIePhmkVnejRGNp0 INSTANCE = new $$Lambda$RunningTasks$B8bQNi7MO0XIePhmkVnejRGNp0();

    private /* synthetic */ $$Lambda$RunningTasks$B8bQNi7MO0XIePhmkVnejRGNp0() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return Long.signum(((TaskRecord) obj2).lastActiveTime - ((TaskRecord) obj).lastActiveTime);
    }
}
