package huawei.android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayRegister;
import android.hwclipboarddelayread.IHwClipboardReadDelayRegisterFactory;

public class HwClipboardReadDelayRegisterFactoryImpl implements IHwClipboardReadDelayRegisterFactory {
    public HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister getHwClipboardReadDelayRegisterInstance() {
        return new HwClipboardReadDelayRegisterImpl();
    }
}
