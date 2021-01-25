package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).productionCalibrationY = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Float((String) obj);
    }
}
