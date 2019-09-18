package android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public class HwClipboardReadDelayerDummy implements HwClipboardReadDelayer.IHwClipboardReadDelayer {
    public boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        return false;
    }

    public boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        return false;
    }

    public void setPrimaryClipNotify() {
    }

    public void getPrimaryClipNotify() {
    }

    public int setGetWaitTime(int waitTime) {
        return -1;
    }
}
