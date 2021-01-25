package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4 INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).flashlightDetectionMode = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Int((String) obj);
    }
}
