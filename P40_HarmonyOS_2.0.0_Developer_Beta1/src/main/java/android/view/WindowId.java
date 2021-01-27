package android.view;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import java.util.HashMap;

public class WindowId implements Parcelable {
    public static final Parcelable.Creator<WindowId> CREATOR = new Parcelable.Creator<WindowId>() {
        /* class android.view.WindowId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WindowId createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new WindowId(target);
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public WindowId[] newArray(int size) {
            return new WindowId[size];
        }
    };
    private final IWindowId mToken;

    public static abstract class FocusObserver {
        final Handler mHandler = new H();
        final IWindowFocusObserver.Stub mIObserver = new IWindowFocusObserver.Stub() {
            /* class android.view.WindowId.FocusObserver.AnonymousClass1 */

            @Override // android.view.IWindowFocusObserver
            public void focusGained(IBinder inputToken) {
                WindowId token;
                synchronized (FocusObserver.this.mRegistrations) {
                    token = FocusObserver.this.mRegistrations.get(inputToken);
                }
                if (FocusObserver.this.mHandler != null) {
                    FocusObserver.this.mHandler.sendMessage(FocusObserver.this.mHandler.obtainMessage(1, token));
                } else {
                    FocusObserver.this.onFocusGained(token);
                }
            }

            @Override // android.view.IWindowFocusObserver
            public void focusLost(IBinder inputToken) {
                WindowId token;
                synchronized (FocusObserver.this.mRegistrations) {
                    token = FocusObserver.this.mRegistrations.get(inputToken);
                }
                if (FocusObserver.this.mHandler != null) {
                    FocusObserver.this.mHandler.sendMessage(FocusObserver.this.mHandler.obtainMessage(2, token));
                } else {
                    FocusObserver.this.onFocusLost(token);
                }
            }
        };
        final HashMap<IBinder, WindowId> mRegistrations = new HashMap<>();

        public abstract void onFocusGained(WindowId windowId);

        public abstract void onFocusLost(WindowId windowId);

        class H extends Handler {
            H() {
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    FocusObserver.this.onFocusGained((WindowId) msg.obj);
                } else if (i != 2) {
                    super.handleMessage(msg);
                } else {
                    FocusObserver.this.onFocusLost((WindowId) msg.obj);
                }
            }
        }
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
            if (!observer.mRegistrations.containsKey(this.mToken.asBinder())) {
                observer.mRegistrations.put(this.mToken.asBinder(), this);
                try {
                    this.mToken.registerFocusObserver(observer.mIObserver);
                } catch (RemoteException e) {
                }
            } else {
                throw new IllegalStateException("Focus observer already registered with input token");
            }
        }
    }

    public void unregisterFocusObserver(FocusObserver observer) {
        synchronized (observer.mRegistrations) {
            if (observer.mRegistrations.remove(this.mToken.asBinder()) != null) {
                try {
                    this.mToken.unregisterFocusObserver(observer.mIObserver);
                } catch (RemoteException e) {
                }
            } else {
                throw new IllegalStateException("Focus observer not registered with input token");
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
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentSender{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        sb.append(this.mToken.asBinder());
        sb.append('}');
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
