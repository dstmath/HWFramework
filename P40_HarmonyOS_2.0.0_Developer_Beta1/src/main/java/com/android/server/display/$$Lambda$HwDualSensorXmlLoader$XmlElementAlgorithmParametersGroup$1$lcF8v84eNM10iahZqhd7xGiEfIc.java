package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backLuxDeviationThresh = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Long((String) obj);
    }
}
