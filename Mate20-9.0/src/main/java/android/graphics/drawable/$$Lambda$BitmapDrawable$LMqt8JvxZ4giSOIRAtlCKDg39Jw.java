package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw INSTANCE = new $$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw();

    private /* synthetic */ $$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
