package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$lM4y7oOfdlEk7JJ3u_zy-rL_-YI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$lM4y7oOfdlEk7JJ3u_zyrL_YI implements Consumer {
    public static final /* synthetic */ $$Lambda$UserState$lM4y7oOfdlEk7JJ3u_zyrL_YI INSTANCE = new $$Lambda$UserState$lM4y7oOfdlEk7JJ3u_zyrL_YI();

    private /* synthetic */ $$Lambda$UserState$lM4y7oOfdlEk7JJ3u_zyrL_YI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((UserState) obj).onConfigurationChanged();
    }
}
