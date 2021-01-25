package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw INSTANCE = new $$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw();

    private /* synthetic */ $$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw() {
    }

    @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        Drawable.lambda$getBitmapDrawable$1(imageDecoder, imageInfo, source);
    }
}
