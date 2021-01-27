package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).fusedColorRateMillis = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Int((String) obj);
    }
}
