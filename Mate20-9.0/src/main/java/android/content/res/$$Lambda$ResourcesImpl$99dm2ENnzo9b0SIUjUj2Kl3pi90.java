package android.content.res;

import android.graphics.ImageDecoder;

/* renamed from: android.content.res.-$$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90 implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90 INSTANCE = new $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90();

    private /* synthetic */ $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
