package android.view;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.view.IWindowFocusObserver.Stub;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;
import java.util.HashMap;

public class WindowId implements Parcelable {
    public static final Creator<WindowId> CREATOR = null;
    private final IWindowId mToken;

    public static abstract class FocusObserver {
        final Handler mHandler;
        final Stub mIObserver;
        final HashMap<IBinder, WindowId> mRegistrations;

        class H extends Handler {
            H() {
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwCfgFilePolicy.EMUI /*1*/:
                        FocusObserver.this.onFocusGained((WindowId) msg.obj);
                    case HwCfgFilePolicy.PC /*2*/:
                        FocusObserver.this.onFocusLost((WindowId) msg.obj);
                    default:
                        super.handleMessage(msg);
                }
            }
        }

        public abstract void onFocusGained(WindowId windowId);

        public abstract void onFocusLost(WindowId windowId);

        public FocusObserver() {
            this.mIObserver = new Stub() {
                public void focusGained(IBinder inputToken) {
                    synchronized (FocusObserver.this.mRegistrations) {
                        WindowId token = (WindowId) FocusObserver.this.mRegistrations.get(inputToken);
                    }
                    if (FocusObserver.this.mHandler != null) {
                        FocusObserver.this.mHandler.sendMessage(FocusObserver.this.mHandler.obtainMessage(1, token));
                    } else {
                        FocusObserver.this.onFocusGained(token);
                    }
                }

                public void focusLost(IBinder inputToken) {
                    synchronized (FocusObserver.this.mRegistrations) {
                        WindowId token = (WindowId) FocusObserver.this.mRegistrations.get(inputToken);
                    }
                    if (FocusObserver.this.mHandler != null) {
                        FocusObserver.this.mHandler.sendMessage(FocusObserver.this.mHandler.obtainMessage(2, token));
                    } else {
                        FocusObserver.this.onFocusLost(token);
                    }
                }
            };
            this.mRegistrations = new HashMap();
            this.mHandler = new H();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.WindowId.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.WindowId.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.WindowId.<clinit>():void");
    }

    public boolean isFocused() {
        try {
            return this.mToken.isFocused();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void registerFocusObserver(FocusObserver observer) {
        synchronized (observer.mRegistrations) {
            if (observer.mRegistrations.containsKey(this.mToken.asBinder())) {
                throw new IllegalStateException("Focus observer already registered with input token");
            }
            observer.mRegistrations.put(this.mToken.asBinder(), this);
            try {
                this.mToken.registerFocusObserver(observer.mIObserver);
            } catch (RemoteException e) {
            }
        }
    }

    public void unregisterFocusObserver(FocusObserver observer) {
        synchronized (observer.mRegistrations) {
            if (observer.mRegistrations.remove(this.mToken.asBinder()) == null) {
                throw new IllegalStateException("Focus observer not registered with input token");
            }
            try {
                this.mToken.unregisterFocusObserver(observer.mIObserver);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean equals(Object otherObj) {
        if (otherObj instanceof WindowId) {
            return this.mToken.asBinder().equals(((WindowId) otherObj).mToken.asBinder());
        }
        return false;
    }

    public int hashCode() {
        return this.mToken.asBinder().hashCode();
    }

    public String toString() {
        Object obj = null;
        StringBuilder sb = new StringBuilder(LogPower.START_CHG_ROTATION);
        sb.append("IntentSender{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        if (this.mToken != null) {
            obj = this.mToken.asBinder();
        }
        sb.append(obj);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mToken.asBinder());
    }

    public IWindowId getTarget() {
        return this.mToken;
    }

    public WindowId(IWindowId target) {
        this.mToken = target;
    }

    public WindowId(IBinder target) {
        this.mToken = IWindowId.Stub.asInterface(target);
    }
}
