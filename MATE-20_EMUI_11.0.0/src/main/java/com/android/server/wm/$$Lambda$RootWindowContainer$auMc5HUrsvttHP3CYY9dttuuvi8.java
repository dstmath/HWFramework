package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8 implements Consumer {
    public static final /* synthetic */ $$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8 INSTANCE = new $$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8();

    private /* synthetic */ $$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowState) obj).updateAppOpsState();
    }
}
