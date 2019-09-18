package android.widget;

import android.graphics.ImageDecoder;

/* renamed from: android.widget.-$$Lambda$ImageView$GWf2-Z-LHjSbTbrF-I3WzfR0LeM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageView$GWf2ZLHjSbTbrFI3WzfR0LeM implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$ImageView$GWf2ZLHjSbTbrFI3WzfR0LeM INSTANCE = new $$Lambda$ImageView$GWf2ZLHjSbTbrFI3WzfR0LeM();

    private /* synthetic */ $$Lambda$ImageView$GWf2ZLHjSbTbrFI3WzfR0LeM() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
