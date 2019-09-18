package android.hwclipboarddelayread;

import android.content.ClipboardManager;
import android.content.Context;
import android.hwclipboarddelayread.HwClipboardReadDelayRegister;
import android.os.Handler;

public class HwClipboardReadDelayRegisterDummy implements HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister {
    public void addPrimaryClipGetedListener(ClipboardManager.OnPrimaryClipGetedListener what, Context context, Handler handler) {
    }

    public void removePrimaryClipGetedListener() {
    }

    public void setGetWaitTime(int waitTime) {
    }
}
