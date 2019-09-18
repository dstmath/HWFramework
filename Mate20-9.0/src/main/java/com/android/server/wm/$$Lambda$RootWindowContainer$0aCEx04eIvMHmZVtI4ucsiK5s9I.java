package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I implements Consumer {
    public static final /* synthetic */ $$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I INSTANCE = new $$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I();

    private /* synthetic */ $$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I() {
    }

    public final void accept(Object obj) {
        ((WindowState) obj).updateAppOpsState();
    }
}
