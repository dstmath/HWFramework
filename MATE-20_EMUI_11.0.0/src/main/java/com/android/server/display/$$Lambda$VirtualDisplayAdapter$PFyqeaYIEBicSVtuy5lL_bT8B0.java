package com.android.server.display;

import android.os.IBinder;
import android.view.SurfaceControl;
import com.android.server.display.VirtualDisplayAdapter;

/* renamed from: com.android.server.display.-$$Lambda$VirtualDisplayAdapter$PFyqe-aYIEBicSVtuy5lL_bT8B0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VirtualDisplayAdapter$PFyqeaYIEBicSVtuy5lL_bT8B0 implements VirtualDisplayAdapter.SurfaceControlDisplayFactory {
    public static final /* synthetic */ $$Lambda$VirtualDisplayAdapter$PFyqeaYIEBicSVtuy5lL_bT8B0 INSTANCE = new $$Lambda$VirtualDisplayAdapter$PFyqeaYIEBicSVtuy5lL_bT8B0();

    private /* synthetic */ $$Lambda$VirtualDisplayAdapter$PFyqeaYIEBicSVtuy5lL_bT8B0() {
    }

    @Override // com.android.server.display.VirtualDisplayAdapter.SurfaceControlDisplayFactory
    public final IBinder createDisplay(String str, boolean z) {
        return SurfaceControl.createDisplay(str, z);
    }
}
