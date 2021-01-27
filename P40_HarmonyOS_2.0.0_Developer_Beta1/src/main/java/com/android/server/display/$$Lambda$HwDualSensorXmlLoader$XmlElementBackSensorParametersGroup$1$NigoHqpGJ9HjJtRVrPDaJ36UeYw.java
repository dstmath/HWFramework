package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).sensorVersion = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Int((String) obj);
    }
}
