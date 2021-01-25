package com.huawei.displayengine;

import com.huawei.displayengine.ImageProcessor;
import java.util.function.Consumer;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessor$BsdKR-MQScfwPT82u5hWhNI_OgQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessor$BsdKRMQScfwPT82u5hWhNI_OgQ implements Consumer {
    public static final /* synthetic */ $$Lambda$ImageProcessor$BsdKRMQScfwPT82u5hWhNI_OgQ INSTANCE = new $$Lambda$ImageProcessor$BsdKRMQScfwPT82u5hWhNI_OgQ();

    private /* synthetic */ $$Lambda$ImageProcessor$BsdKRMQScfwPT82u5hWhNI_OgQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageProcessor.BitmapConfigTransformer) obj).doPostTransform();
    }
}
