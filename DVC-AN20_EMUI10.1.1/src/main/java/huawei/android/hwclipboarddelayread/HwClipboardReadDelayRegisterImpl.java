package huawei.android.hwclipboarddelayread;

import android.content.ClipboardManager;
import android.content.Context;
import android.hwclipboarddelayread.HwClipboardReadDelayRegister;
import android.os.Handler;
import com.huawei.android.content.IOnPrimaryClipGetedListener;
import com.huawei.android.server.clipboard.HwClipboardServiceManager;
import huawei.android.hwclipboarddelayread.HwClipboardReadDelayRegisterImpl;

public class HwClipboardReadDelayRegisterImpl implements HwClipboardReadDelayRegister.IHwClipboardReadDelayRegister {
    private final Object clipLockObj = new Object();
    private ClipboardManager.OnPrimaryClipGetedListener primaryClipGetedListener = null;
    private IOnPrimaryClipGetedListener.Stub primaryClipGetedServiceListener = null;
    private Handler registedHandler;

    public void addPrimaryClipGetedListener(ClipboardManager.OnPrimaryClipGetedListener what, Context context, Handler handler) {
        if (what != null && context != null && handler != null) {
            synchronized (this.clipLockObj) {
                this.registedHandler = handler;
                this.primaryClipGetedListener = what;
                this.primaryClipGetedServiceListener = new IOnPrimaryClipGetedListener.Stub() {
                    /* class huawei.android.hwclipboarddelayread.HwClipboardReadDelayRegisterImpl.AnonymousClass1 */

                    public void dispatchPrimaryClipGet() {
                        synchronized (HwClipboardReadDelayRegisterImpl.this.clipLockObj) {
                            if (HwClipboardReadDelayRegisterImpl.this.registedHandler != null) {
                                HwClipboardReadDelayRegisterImpl.this.registedHandler.post(new Runnable() {
                                    /* class huawei.android.hwclipboarddelayread.$$Lambda$HwClipboardReadDelayRegisterImpl$1$Kr121qiZzK8juS3Psbak_fX1g6w */

                                    public final void run() {
                                        HwClipboardReadDelayRegisterImpl.AnonymousClass1.this.lambda$dispatchPrimaryClipGet$0$HwClipboardReadDelayRegisterImpl$1();
                                    }
                                });
                            }
                        }
                    }

                    public /* synthetic */ void lambda$dispatchPrimaryClipGet$0$HwClipboardReadDelayRegisterImpl$1() {
                        HwClipboardReadDelayRegisterImpl.this.reportPrimaryClipGeted();
                    }
                };
                HwClipboardServiceManager.addPrimaryClipGetedListener(this.primaryClipGetedServiceListener, context.getOpPackageName());
            }
        }
    }

    public void removePrimaryClipGetedListener() {
        synchronized (this.clipLockObj) {
            if (this.primaryClipGetedServiceListener != null) {
                HwClipboardServiceManager.removePrimaryClipGetedListener(this.primaryClipGetedServiceListener);
                this.primaryClipGetedServiceListener = null;
            }
            this.registedHandler = null;
            this.primaryClipGetedListener = null;
        }
    }

    public void setGetWaitTime(int waitTime) {
        HwClipboardServiceManager.setGetWaitTime(waitTime);
    }

    /* access modifiers changed from: package-private */
    public void reportPrimaryClipGeted() {
        synchronized (this.clipLockObj) {
            if (this.primaryClipGetedListener != null) {
                this.primaryClipGetedListener.onPrimaryClipGeted();
            }
        }
    }
}
