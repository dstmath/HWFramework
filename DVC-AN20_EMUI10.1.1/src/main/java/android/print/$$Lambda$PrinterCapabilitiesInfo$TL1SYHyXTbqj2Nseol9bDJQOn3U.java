package android.print;

import java.util.function.IntConsumer;

/* renamed from: android.print.-$$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U implements IntConsumer {
    public static final /* synthetic */ $$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U INSTANCE = new $$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U();

    private /* synthetic */ $$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U() {
    }

    public final void accept(int i) {
        PrintAttributes.enforceValidDuplexMode(i);
    }
}
