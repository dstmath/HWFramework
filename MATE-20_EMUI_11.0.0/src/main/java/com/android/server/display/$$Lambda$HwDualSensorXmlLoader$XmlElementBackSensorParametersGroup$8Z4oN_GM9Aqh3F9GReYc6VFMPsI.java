package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).spectrumTime = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Double((String) obj);
    }
}
