package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).productionCalibrationIr1 = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Float((String) obj);
    }
}
