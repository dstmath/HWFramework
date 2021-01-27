package android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;

public interface IHwClipboardReadDelayerFactory {
    HwClipboardReadDelayer.IHwClipboardReadDelayer getHwClipboardReadDelayerInstance();
}
