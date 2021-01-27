package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D-_3uXEzb-my8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D_3uXEzbmy8 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D_3uXEzbmy8 INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D_3uXEzbmy8();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D_3uXEzbmy8() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).moduleSensorMap.put("HwNormalizedAutomaticBrightnessController", Integer.valueOf(HwDualSensorXmlLoader.XmlElementModuleSensorOptionsGroup.string2Int((String) obj)));
    }
}
