package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_-uorQG8Pmcm3AyhzkM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_uorQG8Pmcm3AyhzkM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_uorQG8Pmcm3AyhzkM INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_uorQG8Pmcm3AyhzkM();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_uorQG8Pmcm3AyhzkM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).luxScale = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Float((String) obj);
    }
}
