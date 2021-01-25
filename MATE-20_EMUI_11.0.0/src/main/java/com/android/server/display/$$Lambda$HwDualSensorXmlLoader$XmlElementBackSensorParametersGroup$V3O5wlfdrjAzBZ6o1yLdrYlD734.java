package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734 INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).needNirCompensation = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Boolean((String) obj);
    }
}
