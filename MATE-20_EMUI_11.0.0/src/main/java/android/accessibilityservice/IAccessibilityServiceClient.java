package android.accessibilityservice;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Region;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public interface IAccessibilityServiceClient extends IInterface {
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

    void onMagnificationChanged(int i, Region region, float f, float f2, float f3) throws RemoteException;

    void onPerformGestureResult(int i, boolean z) throws RemoteException;

    void onSoftKeyboardShowModeChanged(int i) throws RemoteException;

    public static class Default implements IAccessibilityServiceClient {
        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onInterrupt() throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onGesture(int gesture) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void clearAccessibilityCache() throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onKeyEvent(KeyEvent event, int sequence) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onSoftKeyboardShowModeChanged(int showMode) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onPerformGestureResult(int sequence, boolean completedSuccessfully) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onFingerprintCapturingGesturesChanged(boolean capturing) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onFingerprintGesture(int gesture) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityButtonClicked() throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityButtonAvailabilityChanged(boolean available) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "init";
                case 2:
                    return "onAccessibilityEvent";
                case 3:
                    return "onInterrupt";
                case 4:
                    return "onGesture";
                case 5:
                    return "clearAccessibilityCache";
                case 6:
                    return "onKeyEvent";
                case 7:
                    return "onMagnificationChanged";
                case 8:
                    return "onSoftKeyboardShowModeChanged";
                case 9:
                    return "onPerformGestureResult";
                case 10:
                    return "onFingerprintCapturingGesturesChanged";
                case 11:
                    return "onFingerprintGesture";
                case 12:
                    return "onAccessibilityButtonClicked";
                case 13:
                    return "onAccessibilityButtonAvailabilityChanged";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AccessibilityEvent _arg0;
            KeyEvent _arg02;
            Region _arg1;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        init(IAccessibilityServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readStrongBinder());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AccessibilityEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onAccessibilityEvent(_arg0, _arg03);
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
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = KeyEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onKeyEvent(_arg02, data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onMagnificationChanged(_arg04, _arg1, data.readFloat(), data.readFloat(), data.readFloat());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onSoftKeyboardShowModeChanged(data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onPerformGestureResult(_arg05, _arg03);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onFingerprintCapturingGesturesChanged(_arg03);
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
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onAccessibilityButtonAvailabilityChanged(_arg03);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAccessibilityServiceClient {
            public static IAccessibilityServiceClient sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    _data.writeInt(connectionId);
                    _data.writeStrongBinder(windowToken);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().init(connection, connectionId, windowToken);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (serviceWantsEvent) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAccessibilityEvent(event, serviceWantsEvent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onInterrupt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterrupt();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGesture(gesture);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void clearAccessibilityCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearAccessibilityCache();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
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
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onKeyEvent(event, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (region != null) {
                        _data.writeInt(1);
                        region.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(scale);
                    _data.writeFloat(centerX);
                    _data.writeFloat(centerY);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMagnificationChanged(displayId, region, scale, centerX, centerY);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onSoftKeyboardShowModeChanged(int showMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showMode);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSoftKeyboardShowModeChanged(showMode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    _data.writeInt(completedSuccessfully ? 1 : 0);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPerformGestureResult(sequence, completedSuccessfully);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onFingerprintCapturingGesturesChanged(boolean capturing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(capturing ? 1 : 0);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFingerprintCapturingGesturesChanged(capturing);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onFingerprintGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFingerprintGesture(gesture);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onAccessibilityButtonClicked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAccessibilityButtonClicked();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onAccessibilityButtonAvailabilityChanged(boolean available) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(available ? 1 : 0);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAccessibilityButtonAvailabilityChanged(available);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAccessibilityServiceClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAccessibilityServiceClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
