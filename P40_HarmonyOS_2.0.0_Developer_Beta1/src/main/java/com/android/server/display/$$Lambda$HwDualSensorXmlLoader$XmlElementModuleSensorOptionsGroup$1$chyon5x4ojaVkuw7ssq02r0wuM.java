package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$c-hyon5x4ojaVkuw7ssq02r0wuM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$chyon5x4ojaVkuw7ssq02r0wuM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$chyon5x4ojaVkuw7ssq02r0wuM INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$chyon5x4ojaVkuw7ssq02r0wuM();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$chyon5x4ojaVkuw7ssq02r0wuM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).moduleSensorMap.put("HwNormalizedManualBrightnessController", Integer.valueOf(HwDualSensorXmlLoader.XmlElementModuleSensorOptionsGroup.string2Int((String) obj)));
    }
}
