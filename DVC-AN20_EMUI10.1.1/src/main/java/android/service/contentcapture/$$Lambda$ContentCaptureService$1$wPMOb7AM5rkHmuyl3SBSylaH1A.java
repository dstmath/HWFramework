package android.service.contentcapture;

import java.util.function.Consumer;

/* renamed from: android.service.contentcapture.-$$Lambda$ContentCaptureService$1$wPMOb7AM5r-kHmuyl3SBSylaH1A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContentCaptureService$1$wPMOb7AM5rkHmuyl3SBSylaH1A implements Consumer {
    public static final /* synthetic */ $$Lambda$ContentCaptureService$1$wPMOb7AM5rkHmuyl3SBSylaH1A INSTANCE = new $$Lambda$ContentCaptureService$1$wPMOb7AM5rkHmuyl3SBSylaH1A();

    private /* synthetic */ $$Lambda$ContentCaptureService$1$wPMOb7AM5rkHmuyl3SBSylaH1A() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ContentCaptureService) obj).handleOnDisconnected();
    }
}
