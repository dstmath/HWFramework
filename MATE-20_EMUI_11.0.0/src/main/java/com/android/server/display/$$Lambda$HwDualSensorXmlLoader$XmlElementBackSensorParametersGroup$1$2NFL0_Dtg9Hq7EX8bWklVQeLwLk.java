package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).isInwardFoldScreen = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Boolean((String) obj);
    }
}
