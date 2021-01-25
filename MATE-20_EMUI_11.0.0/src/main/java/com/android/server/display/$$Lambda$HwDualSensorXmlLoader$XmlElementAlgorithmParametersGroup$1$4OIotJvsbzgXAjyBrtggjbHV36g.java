package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).darkRoomThresh3 = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Float((String) obj);
    }
}
