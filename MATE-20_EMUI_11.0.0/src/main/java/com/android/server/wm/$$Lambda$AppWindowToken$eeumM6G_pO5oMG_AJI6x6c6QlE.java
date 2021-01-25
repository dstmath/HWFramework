package com.android.server.wm;

import java.util.function.Predicate;

/* renamed from: com.android.server.wm.-$$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6Q-lE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6QlE implements Predicate {
    public static final /* synthetic */ $$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6QlE INSTANCE = new $$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6QlE();

    private /* synthetic */ $$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6QlE() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AppWindowToken.lambda$hasHwSecureWindowOnScreen$6((WindowState) obj);
    }
}
