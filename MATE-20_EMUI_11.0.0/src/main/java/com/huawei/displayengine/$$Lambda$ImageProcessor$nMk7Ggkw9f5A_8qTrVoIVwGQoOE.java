package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE INSTANCE = new $$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE();

    private /* synthetic */ $$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPostTransform();
    }
}
