package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ INSTANCE = new $$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ();

    private /* synthetic */ $$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPreTransform();
    }
}
