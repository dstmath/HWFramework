package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).isFilterOn = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Boolean((String) obj);
    }
}
