package com.android.server.wm;

import android.view.SurfaceControl;
import android.view.SurfaceSession;

/* renamed from: com.android.server.wm.-$$Lambda$XZ-U3HlCFtHp_gydNmNMeRmQMCI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XZU3HlCFtHp_gydNmNMeRmQMCI implements SurfaceBuilderFactory {
    public static final /* synthetic */ $$Lambda$XZU3HlCFtHp_gydNmNMeRmQMCI INSTANCE = new $$Lambda$XZU3HlCFtHp_gydNmNMeRmQMCI();

    private /* synthetic */ $$Lambda$XZU3HlCFtHp_gydNmNMeRmQMCI() {
    }

    @Override // com.android.server.wm.SurfaceBuilderFactory
    public final SurfaceControl.Builder make(SurfaceSession surfaceSession) {
        return new SurfaceControl.Builder(surfaceSession);
    }
}
