package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1G-aCpCX2OzE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1GaCpCX2OzE implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1GaCpCX2OzE INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1GaCpCX2OzE();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1GaCpCX2OzE() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).backSensorBypassCountMax = HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.string2Int((String) obj);
    }
}
