package com.android.server.am;

import java.util.Comparator;

/* renamed from: com.android.server.am.-$$Lambda$RunningTasks$BGar3HlUsTw-0HzSmfkEWly0moY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RunningTasks$BGar3HlUsTw0HzSmfkEWly0moY implements Comparator {
    public static final /* synthetic */ $$Lambda$RunningTasks$BGar3HlUsTw0HzSmfkEWly0moY INSTANCE = new $$Lambda$RunningTasks$BGar3HlUsTw0HzSmfkEWly0moY();

    private /* synthetic */ $$Lambda$RunningTasks$BGar3HlUsTw0HzSmfkEWly0moY() {
    }

    public final int compare(Object obj, Object obj2) {
        return Long.signum(((TaskRecord) obj2).lastActiveTime - ((TaskRecord) obj).lastActiveTime);
    }
}
