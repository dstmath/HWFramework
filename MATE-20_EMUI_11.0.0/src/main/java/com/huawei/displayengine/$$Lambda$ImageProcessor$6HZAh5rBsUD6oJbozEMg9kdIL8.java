package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kd-IL8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kdIL8 implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kdIL8 INSTANCE = new $$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kdIL8();

    private /* synthetic */ $$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kdIL8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPreTransform();
    }
}
