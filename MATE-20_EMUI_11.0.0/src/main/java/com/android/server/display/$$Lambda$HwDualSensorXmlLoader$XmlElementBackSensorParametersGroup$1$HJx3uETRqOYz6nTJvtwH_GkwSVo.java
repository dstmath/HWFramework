package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).productionCalibrationZ = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Float((String) obj);
    }
}
