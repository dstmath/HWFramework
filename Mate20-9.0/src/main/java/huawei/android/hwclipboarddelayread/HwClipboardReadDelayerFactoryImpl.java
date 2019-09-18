package huawei.android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;
import android.hwclipboarddelayread.IHwClipboardReadDelayerFactory;

public class HwClipboardReadDelayerFactoryImpl implements IHwClipboardReadDelayerFactory {
    public HwClipboardReadDelayer.IHwClipboardReadDelayer getHwClipboardReadDelayerInstance() {
        return new HwClipboardReadDelayerImpl();
    }
}
