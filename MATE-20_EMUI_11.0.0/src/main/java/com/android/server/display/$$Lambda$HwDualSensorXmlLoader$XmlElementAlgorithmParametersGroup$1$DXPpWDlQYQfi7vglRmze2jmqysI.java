package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).flashlightOffTimeThMs = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Int((String) obj);
    }
}
