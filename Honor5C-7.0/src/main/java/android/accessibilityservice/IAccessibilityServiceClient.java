package android.accessibilityservice;

import android.graphics.Region;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public interface IAccessibilityServiceClient extends IInterface {

    public static abstract class Stub extends Binder implements IAccessibilityServiceClient {
        private static final String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceClient";
        static final int TRANSACTION_clearAccessibilityCache = 5;
        static final int TRANSACTION_init = 1;
        static final int TRANSACTION_onAccessibilityEvent = 2;
        static final int TRANSACTION_onGesture = 4;
        static final int TRANSACTION_onInterrupt = 3;
        static final int TRANSACTION_onKeyEvent = 6;
        static final int TRANSACTION_onMagnificationChanged = 7;
        static final int TRANSACTION_onPerformGestureResult = 9;
        static final int TRANSACTION_onSoftKeyboardShowModeChanged = 8;

        private static class Proxy implements IAccessibilityServiceClient {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(connectionId);
                    _data.writeStrongBinder(windowToken);
                    this.mRemote.transact(Stub.TRANSACTION_init, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onAccessibilityEvent(AccessibilityEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_init);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onAccessibilityEvent, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onInterrupt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onInterrupt, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    this.mRemote.transact(Stub.TRANSACTION_onGesture, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void clearAccessibilityCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearAccessibilityCache, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onKeyEvent(KeyEvent event, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_init);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onKeyEvent, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (region != null) {
                        _data.writeInt(Stub.TRANSACTION_init);
                        region.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(scale);
                    _data.writeFloat(centerX);
                    _data.writeFloat(centerY);
                    this.mRemote.transact(Stub.TRANSACTION_onMagnificationChanged, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onSoftKeyboardShowModeChanged(int showMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showMode);
                    this.mRemote.transact(Stub.TRANSACTION_onSoftKeyboardShowModeChanged, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }

            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) throws RemoteException {
                int i = Stub.TRANSACTION_init;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (!completedSuccessfully) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onPerformGestureResult, _data, null, Stub.TRANSACTION_init);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccessibilityServiceClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityServiceClient)) {
                return new Proxy(obj);
            }
            return (IAccessibilityServiceClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_init /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    init(android.accessibilityservice.IAccessibilityServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readStrongBinder());
                    return true;
                case TRANSACTION_onAccessibilityEvent /*2*/:
                    AccessibilityEvent accessibilityEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        accessibilityEvent = (AccessibilityEvent) AccessibilityEvent.CREATOR.createFromParcel(data);
                    } else {
                        accessibilityEvent = null;
                    }
                    onAccessibilityEvent(accessibilityEvent);
                    return true;
                case TRANSACTION_onInterrupt /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onInterrupt();
                    return true;
                case TRANSACTION_onGesture /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onGesture(data.readInt());
                    return true;
                case TRANSACTION_clearAccessibilityCache /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearAccessibilityCache();
                    return true;
                case TRANSACTION_onKeyEvent /*6*/:
                    KeyEvent keyEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyEvent = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        keyEvent = null;
                    }
                    onKeyEvent(keyEvent, data.readInt());
                    return true;
                case TRANSACTION_onMagnificationChanged /*7*/:
                    Region region;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region = null;
                    }
                    onMagnificationChanged(region, data.readFloat(), data.readFloat(), data.readFloat());
                    return true;
                case TRANSACTION_onSoftKeyboardShowModeChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSoftKeyboardShowModeChanged(data.readInt());
                    return true;
                case TRANSACTION_onPerformGestureResult /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPerformGestureResult(data.readInt(), data.readInt() != 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearAccessibilityCache() throws RemoteException;

    void init(IAccessibilityServiceConnection iAccessibilityServiceConnection, int i, IBinder iBinder) throws RemoteException;

    void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) throws RemoteException;

    void onGesture(int i) throws RemoteException;

    void onInterrupt() throws RemoteException;

    void onKeyEvent(KeyEvent keyEvent, int i) throws RemoteException;

    void onMagnificationChanged(Region region, float f, float f2, float f3) throws RemoteException;

    void onPerformGestureResult(int i, boolean z) throws RemoteException;

    void onSoftKeyboardShowModeChanged(int i) throws RemoteException;
}
