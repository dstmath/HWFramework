package android.graphics.drawable;

import android.graphics.ImageDecoder;

/* renamed from: android.graphics.drawable.-$$Lambda$Drawable$KZt6g0-IxKV2yrq1V3HrWrb1kXg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Drawable$KZt6g0IxKV2yrq1V3HrWrb1kXg implements ImageDecoder.OnPartialImageListener {
    public static final /* synthetic */ $$Lambda$Drawable$KZt6g0IxKV2yrq1V3HrWrb1kXg INSTANCE = new $$Lambda$Drawable$KZt6g0IxKV2yrq1V3HrWrb1kXg();

    private /* synthetic */ $$Lambda$Drawable$KZt6g0IxKV2yrq1V3HrWrb1kXg() {
    }

    @Override // android.graphics.ImageDecoder.OnPartialImageListener
    public final boolean onPartialImage(ImageDecoder.DecodeException decodeException) {
        return Drawable.lambda$getBitmapDrawable$0(decodeException);
    }
}
