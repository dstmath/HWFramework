package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$AnimatedImageDrawable$Cgt3NliB7ZYUONyDd-eQGdYbEKc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AnimatedImageDrawable$Cgt3NliB7ZYUONyDdeQGdYbEKc implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$AnimatedImageDrawable$Cgt3NliB7ZYUONyDdeQGdYbEKc INSTANCE = new $$Lambda$AnimatedImageDrawable$Cgt3NliB7ZYUONyDdeQGdYbEKc();

    private /* synthetic */ $$Lambda$AnimatedImageDrawable$Cgt3NliB7ZYUONyDdeQGdYbEKc() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        AnimatedImageDrawable.lambda$updateStateFromTypedArray$0(imageDecoder, imageInfo, source);
    }
}
