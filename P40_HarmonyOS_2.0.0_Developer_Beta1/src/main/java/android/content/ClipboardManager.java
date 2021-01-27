package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.ClipboardManager;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.hwclipboarddelayread.HwClipboardReadDelayRegister;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;

public class ClipboardManager extends android.text.ClipboardManager {
    private final Context mContext;
    private final Handler mHandler;
    private final ArrayList<OnPrimaryClipChangedListener> mPrimaryClipChangedListeners = new ArrayList<>();
    private final IOnPrimaryClipChangedListener.Stub mPrimaryClipChangedServiceListener = new IOnPrimaryClipChangedListener.Stub() {
        /* class android.content.ClipboardManager.AnonymousClass1 */

        @Override // android.content.IOnPrimaryClipChangedListener
        public void dispatchPrimaryClipChanged() {
            ClipboardManager.this.mHandler.post(new Runnable() {
                /* class android.content.$$Lambda$ClipboardManager$1$hQk8olbGAgUi4WWNG4ZuDZsM39s */

                @Override // java.lang.Runnable
                public final void run() {
                    ClipboardManager.AnonymousClass1.this.lambda$dispatchPrimaryClipChanged$0$ClipboardManager$1();
                }
            });
        }

        public /* synthetic */ void lambda$dispatchPrimaryClipChanged$0$ClipboardManager$1() {
            ClipboardManager.this.reportPrimaryClipChanged();
        }
    };
    private final IClipboard mService;

    public interface OnPrimaryClipChangedListener {
        void onPrimaryClipChanged();
    }

    public interface OnPrimaryClipGetedListener {
        void onPrimaryClipGeted();
    }

    @UnsupportedAppUsage
    public ClipboardManager(Context context, Handler handler) throws ServiceManager.ServiceNotFoundException {
        this.mContext = context;
        this.mHandler = handler;
        this.mService = IClipboard.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CLIPBOARD_SERVICE));
    }

    public void setPrimaryClip(ClipData clip) {
        try {
            Preconditions.checkNotNull(clip);
            clip.prepareToLeaveProcess(true);
            this.mService.setPrimaryClip(clip, this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearPrimaryClip() {
        try {
            this.mService.clearPrimaryClip(this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ClipData getPrimaryClip() {
        try {
            return this.mService.getPrimaryClip(this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ClipDescription getPrimaryClipDescription() {
        try {
            return this.mService.getPrimaryClipDescription(this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPrimaryClip() {
        try {
            return this.mService.hasPrimaryClip(this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.isEmpty()) {
                try {
                    this.mService.addPrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener, this.mContext.getOpPackageName(), this.mContext.getUserId());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            this.mPrimaryClipChangedListeners.add(what);
        }
    }

    public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            this.mPrimaryClipChangedListeners.remove(what);
            if (this.mPrimaryClipChangedListeners.isEmpty()) {
                try {
                    this.mService.removePrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener, this.mContext.getOpPackageName(), this.mContext.getUserId());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @Override // android.text.ClipboardManager
    @Deprecated
    public CharSequence getText() {
        ClipData clip = getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return null;
        }
        return clip.getItemAt(0).coerceToText(this.mContext);
    }

    @Override // android.text.ClipboardManager
    @Deprecated
    public void setText(CharSequence text) {
        setPrimaryClip(ClipData.newPlainText(null, text));
    }

    @Override // android.text.ClipboardManager
    @Deprecated
    public boolean hasText() {
        try {
            return this.mService.hasClipboardText(this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void reportPrimaryClipChanged() {
        Object[] listeners;
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.size() > 0) {
                listeners = this.mPrimaryClipChangedListeners.toArray();
            } else {
                return;
            }
        }
        for (Object obj : listeners) {
            ((OnPrimaryClipChangedListener) obj).onPrimaryClipChanged();
        }
    }

    public boolean addPrimaryClipGetedListener(OnPrimaryClipGetedListener what) {
        return HwClipboardReadDelayRegister.addPrimaryClipGetedListener(what, this.mContext, this.mHandler);
    }

    public boolean removePrimaryClipGetedListener() {
        return HwClipboardReadDelayRegister.removePrimaryClipGetedListener();
    }

    public void setGetWaitTime(int waitTime) {
        HwClipboardReadDelayRegister.setGetWaitTime(waitTime);
    }
}
