package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$3vT-yVjAiRD3BTJyMvWdvKWyEYU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$3vTyVjAiRD3BTJyMvWdvKWyEYU implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$3vTyVjAiRD3BTJyMvWdvKWyEYU INSTANCE = new $$Lambda$ImageProcessor$3vTyVjAiRD3BTJyMvWdvKWyEYU();

    private /* synthetic */ $$Lambda$ImageProcessor$3vTyVjAiRD3BTJyMvWdvKWyEYU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPostTransform();
    }
}
