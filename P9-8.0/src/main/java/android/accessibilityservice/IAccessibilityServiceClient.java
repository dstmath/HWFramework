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
        static final int TRANSACTION_onAccessibilityButtonAvailabilityChanged = 13;
        static final int TRANSACTION_onAccessibilityButtonClicked = 12;
        static final int TRANSACTION_onAccessibilityEvent = 2;
        static final int TRANSACTION_onFingerprintCapturingGesturesChanged = 10;
        static final int TRANSACTION_onFingerprintGesture = 11;
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!serviceWantsEvent) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onInterrupt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clearAccessibilityCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onKeyEvent(KeyEvent event, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequence);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (region != null) {
                        _data.writeInt(1);
                        region.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(scale);
                    _data.writeFloat(centerX);
                    _data.writeFloat(centerY);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSoftKeyboardShowModeChanged(int showMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showMode);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (!completedSuccessfully) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFingerprintCapturingGesturesChanged(boolean capturing) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!capturing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFingerprintGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAccessibilityButtonClicked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAccessibilityButtonAvailabilityChanged(boolean available) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!available) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, null, 1);
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
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    init(android.accessibilityservice.IAccessibilityServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readStrongBinder());
                    return true;
                case 2:
                    AccessibilityEvent _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (AccessibilityEvent) AccessibilityEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onAccessibilityEvent(_arg0, data.readInt() != 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onInterrupt();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onGesture(data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    clearAccessibilityCache();
                    return true;
                case 6:
                    KeyEvent _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onKeyEvent(_arg02, data.readInt());
                    return true;
                case 7:
                    Region _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    onMagnificationChanged(_arg03, data.readFloat(), data.readFloat(), data.readFloat());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onSoftKeyboardShowModeChanged(data.readInt());
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onPerformGestureResult(data.readInt(), data.readInt() != 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onFingerprintCapturingGesturesChanged(data.readInt() != 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    onFingerprintGesture(data.readInt());
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    onAccessibilityButtonClicked();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    onAccessibilityButtonAvailabilityChanged(data.readInt() != 0);
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

    void onAccessibilityButtonAvailabilityChanged(boolean z) throws RemoteException;

    void onAccessibilityButtonClicked() throws RemoteException;

    void onAccessibilityEvent(AccessibilityEvent accessibilityEvent, boolean z) throws RemoteException;

    void onFingerprintCapturingGesturesChanged(boolean z) throws RemoteException;

    void onFingerprintGesture(int i) throws RemoteException;

    void onGesture(int i) throws RemoteException;

    void onInterrupt() throws RemoteException;

    void onKeyEvent(KeyEvent keyEvent, int i) throws RemoteException;

    void onMagnificationChanged(Region region, float f, float f2, float f3) throws RemoteException;

    void onPerformGestureResult(int i, boolean z) throws RemoteException;

    void onSoftKeyboardShowModeChanged(int i) throws RemoteException;
}
