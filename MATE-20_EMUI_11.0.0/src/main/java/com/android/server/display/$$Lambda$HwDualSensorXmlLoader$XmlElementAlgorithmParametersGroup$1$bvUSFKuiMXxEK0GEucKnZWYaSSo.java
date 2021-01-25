package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backRoofThresh = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Float((String) obj);
    }
}
