package com.android.server.wm;

import com.android.server.wm.DisplayContent;
import java.util.function.Predicate;

/* renamed from: com.android.server.wm.-$$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxG-rcs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs implements Predicate {
    public static final /* synthetic */ $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs INSTANCE = new $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs();

    private /* synthetic */ $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return DisplayContent.NonAppWindowContainers.lambda$new$1((WindowState) obj);
    }
}
