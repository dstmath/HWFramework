package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$Drawable$wmqxcnFJRLY7tFDmv2eEGR5vtvU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Drawable$wmqxcnFJRLY7tFDmv2eEGR5vtvU implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$Drawable$wmqxcnFJRLY7tFDmv2eEGR5vtvU INSTANCE = new $$Lambda$Drawable$wmqxcnFJRLY7tFDmv2eEGR5vtvU();

    private /* synthetic */ $$Lambda$Drawable$wmqxcnFJRLY7tFDmv2eEGR5vtvU() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
