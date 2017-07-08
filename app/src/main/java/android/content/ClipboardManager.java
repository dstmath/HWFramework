package android.content;

import android.content.IOnPrimaryClipChangedListener.Stub;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;

public class ClipboardManager extends android.text.ClipboardManager {
    static final int MSG_REPORT_PRIMARY_CLIP_CHANGED = 1;
    private static IClipboard sService;
    private static final Object sStaticLock = null;
    private final Context mContext;
    private final Handler mHandler;
    private final ArrayList<OnPrimaryClipChangedListener> mPrimaryClipChangedListeners;
    private final Stub mPrimaryClipChangedServiceListener;

    public interface OnPrimaryClipChangedListener {
        void onPrimaryClipChanged();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.ClipboardManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.ClipboardManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ClipboardManager.<clinit>():void");
    }

    private static IClipboard getService() {
        synchronized (sStaticLock) {
            if (sService != null) {
                IClipboard iClipboard = sService;
                return iClipboard;
            }
            sService = IClipboard.Stub.asInterface(ServiceManager.getService(Context.CLIPBOARD_SERVICE));
            iClipboard = sService;
            return iClipboard;
        }
    }

    public ClipboardManager(Context context, Handler handler) {
        this.mPrimaryClipChangedListeners = new ArrayList();
        this.mPrimaryClipChangedServiceListener = new Stub() {
            public void dispatchPrimaryClipChanged() {
                ClipboardManager.this.mHandler.sendEmptyMessage(ClipboardManager.MSG_REPORT_PRIMARY_CLIP_CHANGED);
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ClipboardManager.MSG_REPORT_PRIMARY_CLIP_CHANGED /*1*/:
                        ClipboardManager.this.reportPrimaryClipChanged();
                    default:
                }
            }
        };
        this.mContext = context;
    }

    public void setPrimaryClip(ClipData clip) {
        if (clip != null) {
            try {
                clip.prepareToLeaveProcess(true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        getService().setPrimaryClip(clip, this.mContext.getOpPackageName());
    }

    public ClipData getPrimaryClip() {
        try {
            return getService().getPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ClipDescription getPrimaryClipDescription() {
        try {
            return getService().getPrimaryClipDescription(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPrimaryClip() {
        try {
            return getService().hasPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.size() == 0) {
                try {
                    getService().addPrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener, this.mContext.getOpPackageName());
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
            if (this.mPrimaryClipChangedListeners.size() == 0) {
                try {
                    getService().removePrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public CharSequence getText() {
        ClipData clip = getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return null;
        }
        return clip.getItemAt(0).coerceToText(this.mContext);
    }

    public void setText(CharSequence text) {
        setPrimaryClip(ClipData.newPlainText(null, text));
    }

    public boolean hasText() {
        try {
            return getService().hasClipboardText(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    void reportPrimaryClipChanged() {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.size() <= 0) {
                return;
            }
            Object[] listeners = this.mPrimaryClipChangedListeners.toArray();
            for (int i = 0; i < listeners.length; i += MSG_REPORT_PRIMARY_CLIP_CHANGED) {
                ((OnPrimaryClipChangedListener) listeners[i]).onPrimaryClipChanged();
            }
        }
    }
}
