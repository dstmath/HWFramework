package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6-sgjCbNwcaJqP6ngnkB_kJprGc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6sgjCbNwcaJqP6ngnkB_kJprGc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6sgjCbNwcaJqP6ngnkB_kJprGc INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6sgjCbNwcaJqP6ngnkB_kJprGc();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6sgjCbNwcaJqP6ngnkB_kJprGc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backFloorThresh = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Float((String) obj);
    }
}
