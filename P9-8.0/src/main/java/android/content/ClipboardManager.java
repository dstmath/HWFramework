package android.content;

import android.content.IOnPrimaryClipChangedListener.Stub;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import java.util.ArrayList;

public class ClipboardManager extends android.text.ClipboardManager {
    static final int MSG_REPORT_PRIMARY_CLIP_CHANGED = 1;
    private final Context mContext;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ClipboardManager.this.reportPrimaryClipChanged();
                    return;
                default:
                    return;
            }
        }
    };
    private final ArrayList<OnPrimaryClipChangedListener> mPrimaryClipChangedListeners = new ArrayList();
    private final Stub mPrimaryClipChangedServiceListener = new Stub() {
        public void dispatchPrimaryClipChanged() {
            ClipboardManager.this.mHandler.sendEmptyMessage(1);
        }
    };
    private final IClipboard mService;

    public interface OnPrimaryClipChangedListener {
        void onPrimaryClipChanged();
    }

    public ClipboardManager(Context context, Handler handler) throws ServiceNotFoundException {
        this.mContext = context;
        this.mService = IClipboard.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CLIPBOARD_SERVICE));
    }

    public void setPrimaryClip(ClipData clip) {
        if (clip != null) {
            try {
                clip.prepareToLeaveProcess(true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        this.mService.setPrimaryClip(clip, this.mContext.getOpPackageName());
    }

    public ClipData getPrimaryClip() {
        try {
            return this.mService.getPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ClipDescription getPrimaryClipDescription() {
        try {
            return this.mService.getPrimaryClipDescription(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPrimaryClip() {
        try {
            return this.mService.hasPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.isEmpty()) {
                try {
                    this.mService.addPrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener, this.mContext.getOpPackageName());
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
                    this.mService.removePrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @Deprecated
    public CharSequence getText() {
        ClipData clip = getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return null;
        }
        return clip.getItemAt(0).coerceToText(this.mContext);
    }

    @Deprecated
    public void setText(CharSequence text) {
        setPrimaryClip(ClipData.newPlainText(null, text));
    }

    @Deprecated
    public boolean hasText() {
        try {
            return this.mService.hasClipboardText(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            if (r1 >= r2.length) goto L_0x0025;
     */
    /* JADX WARNING: Missing block: B:13:0x0018, code:
            ((android.content.ClipboardManager.OnPrimaryClipChangedListener) r2[r1]).onPrimaryClipChanged();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:17:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void reportPrimaryClipChanged() {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.size() <= 0) {
                return;
            }
            Object[] listeners = this.mPrimaryClipChangedListeners.toArray();
        }
    }
}
