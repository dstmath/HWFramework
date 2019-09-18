package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU implements Consumer {
    public static final /* synthetic */ $$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU INSTANCE = new $$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU();

    private /* synthetic */ $$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU() {
    }

    public final void accept(Object obj) {
        ((WindowState) obj).mWinAnimator.setOffsetPositionForStackResize(true);
    }
}
