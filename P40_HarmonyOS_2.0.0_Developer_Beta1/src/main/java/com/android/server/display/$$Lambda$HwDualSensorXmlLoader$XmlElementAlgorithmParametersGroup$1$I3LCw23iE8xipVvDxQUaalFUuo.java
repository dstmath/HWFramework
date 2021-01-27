package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFU-uo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFUuo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFUuo INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFUuo();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFUuo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backSensorTimeOutTh = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Int((String) obj);
    }
}
