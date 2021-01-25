package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZ-r-atNUrkmxcIwg6hqGI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZratNUrkmxcIwg6hqGI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZratNUrkmxcIwg6hqGI INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZratNUrkmxcIwg6hqGI();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZratNUrkmxcIwg6hqGI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).darkRoomDelta2 = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Float((String) obj);
    }
}
