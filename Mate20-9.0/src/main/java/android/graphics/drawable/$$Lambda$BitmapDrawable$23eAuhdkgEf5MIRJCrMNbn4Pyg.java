package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJC-rMNbn4Pyg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJCrMNbn4Pyg implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJCrMNbn4Pyg INSTANCE = new $$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJCrMNbn4Pyg();

    private /* synthetic */ $$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJCrMNbn4Pyg() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
