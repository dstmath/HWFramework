package android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public class HwClipboardReadDelayerDummy implements HwClipboardReadDelayer.IHwClipboardReadDelayer {
    @Override // android.hwclipboarddelayread.HwClipboardReadDelayer.IHwClipboardReadDelayer
    public boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        return false;
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayer.IHwClipboardReadDelayer
    public boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        return false;
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayer.IHwClipboardReadDelayer
    public void setPrimaryClipNotify() {
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayer.IHwClipboardReadDelayer
    public void getPrimaryClipNotify() {
    }

    @Override // android.hwclipboarddelayread.HwClipboardReadDelayer.IHwClipboardReadDelayer
    public int setGetWaitTime(int waitTime) {
        return -1;
    }
}
