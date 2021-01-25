package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).moduleSensorMap.put("HwLightSensorController", Integer.valueOf(HwDualSensorXmlLoader.XmlElementModuleSensorOptionsGroup.string2Int((String) obj)));
    }
}
