package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGW-BcSLCTBCeCQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGWBcSLCTBCeCQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGWBcSLCTBCeCQ INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGWBcSLCTBCeCQ();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGWBcSLCTBCeCQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backRateMillis = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Int((String) obj);
    }
}
