package ohos.media.photokit.adapter;

import android.graphics.Bitmap;
import java.util.function.Function;
import ohos.media.image.inner.ImageDoubleFwConverter;

/* renamed from: ohos.media.photokit.adapter.-$$Lambda$U-gcuZ9GI5zEleF3oROMthyyZ2U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UgcuZ9GI5zEleF3oROMthyyZ2U implements Function {
    public static final /* synthetic */ $$Lambda$UgcuZ9GI5zEleF3oROMthyyZ2U INSTANCE = new $$Lambda$UgcuZ9GI5zEleF3oROMthyyZ2U();

    private /* synthetic */ $$Lambda$UgcuZ9GI5zEleF3oROMthyyZ2U() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ImageDoubleFwConverter.createShellPixelMap((Bitmap) obj);
    }
}
