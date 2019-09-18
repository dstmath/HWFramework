package com.android.server.am;

import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;

/* renamed from: com.android.server.am.-$$Lambda$ActivityManagerService$3$poTyYzHinA8s8lAJ-y6Bb3JsBNo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityManagerService$3$poTyYzHinA8s8lAJy6Bb3JsBNo implements ProcessCpuTracker.FilterStats {
    public static final /* synthetic */ $$Lambda$ActivityManagerService$3$poTyYzHinA8s8lAJy6Bb3JsBNo INSTANCE = new $$Lambda$ActivityManagerService$3$poTyYzHinA8s8lAJy6Bb3JsBNo();

    private /* synthetic */ $$Lambda$ActivityManagerService$3$poTyYzHinA8s8lAJy6Bb3JsBNo() {
    }

    public final boolean needed(ProcessCpuTracker.Stats stats) {
        return ActivityManagerService.AnonymousClass3.lambda$handleMessage$0(stats);
    }
}
