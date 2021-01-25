package com.huawei.server.magicwin;

import com.huawei.server.magicwin.HwMagicWindowConfig;
import java.util.function.BiConsumer;

/* renamed from: com.huawei.server.magicwin.-$$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ INSTANCE = new $$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ();

    private /* synthetic */ $$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        String str = (String) obj;
        ((HwMagicWindowConfig.EasyGoConfig) obj2).updateRationAndBound();
    }
}
