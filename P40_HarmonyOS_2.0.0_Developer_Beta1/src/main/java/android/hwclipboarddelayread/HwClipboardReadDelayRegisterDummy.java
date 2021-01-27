package android.hwclipboarddelayread;

import android.content.ClipboardManager;
import android.content.Context;
import android.hwclipboarddelayread.HwClipboardReadDelayRegister;
import android.os.Handler;

public class HwClipboardReadDelayRegisterDummy implements HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister {
    @Override // android.hwclipboarddelayread.HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister
    public void addPrimaryClipGetedListener(ClipboardManager.OnPrimaryClipGetedListener what, Context context, Handler handler) {
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister
    public void removePrimaryClipGetedListener() {
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister
    public void setGetWaitTime(int waitTime) {
    }
}
