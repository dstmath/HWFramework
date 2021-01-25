package com.android.server.display;

import com.android.server.display.HwDualSensorXmlLoader;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.display.-$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM INSTANCE = new $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM();

    private /* synthetic */ $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((HwDualSensorData) obj2).aGainRgbcw = HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.string2Float((String) obj);
    }
}
