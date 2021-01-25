package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE INSTANCE = new $$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE();

    private /* synthetic */ $$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPreTransform();
    }
}
