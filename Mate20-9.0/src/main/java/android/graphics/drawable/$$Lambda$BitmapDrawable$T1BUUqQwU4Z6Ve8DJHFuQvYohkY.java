package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY INSTANCE = new $$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY();

    private /* synthetic */ $$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
