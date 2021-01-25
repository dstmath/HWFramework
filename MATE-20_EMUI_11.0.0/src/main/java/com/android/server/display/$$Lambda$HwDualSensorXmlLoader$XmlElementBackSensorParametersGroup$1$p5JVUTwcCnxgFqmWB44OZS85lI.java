package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcC-nxgFqmWB44OZS85lI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcCnxgFqmWB44OZS85lI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcCnxgFqmWB44OZS85lI INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcCnxgFqmWB44OZS85lI();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcCnxgFqmWB44OZS85lI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).fusedRateMillis = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Int((String) obj);
    }
}
