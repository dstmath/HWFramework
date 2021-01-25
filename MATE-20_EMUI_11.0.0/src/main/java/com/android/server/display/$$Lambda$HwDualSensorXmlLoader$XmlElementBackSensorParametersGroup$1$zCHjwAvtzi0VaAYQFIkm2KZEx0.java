package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZ-Ex0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZEx0 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZEx0 INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZEx0();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZEx0() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).frontRateMillis = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Int((String) obj);
    }
}
