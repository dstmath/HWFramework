package android.service.vr;

import android.app.Vr2dDisplayProperties;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IVrStateCallbacks;

public interface IVrManager extends IInterface {

    public static abstract class Stub extends Binder implements IVrManager {
        private static final String DESCRIPTOR = "android.service.vr.IVrManager";
        static final int TRANSACTION_getPersistentVrModeEnabled = 6;
        static final int TRANSACTION_getVr2dDisplayId = 9;
        static final int TRANSACTION_getVrModeState = 5;
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_registerPersistentVrStateListener = 3;
        static final int TRANSACTION_setAndBindCompositor = 10;
        static final int TRANSACTION_setPersistentVrModeEnabled = 7;
        static final int TRANSACTION_setStandbyEnabled = 11;
        static final int TRANSACTION_setVr2dDisplayProperties = 8;
        static final int TRANSACTION_setVrInputMethod = 12;
        static final int TRANSACTION_unregisterListener = 2;
        static final int TRANSACTION_unregisterPersistentVrStateListener = 4;

        private static class Proxy implements IVrManager {
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

            public void registerListener(IVrStateCallbacks cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IVrStateCallbacks cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerPersistentVrStateListener(IPersistentVrStateCallbacks cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterPersistentVrStateListener(IPersistentVrStateCallbacks cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getVrModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getPersistentVrModeEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPersistentVrModeEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProperties) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (vr2dDisplayProperties != null) {
                        _data.writeInt(1);
                        vr2dDisplayProperties.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVr2dDisplayId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAndBindCompositor(String componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStandbyEnabled(boolean standby) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(standby);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVrInputMethod(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVrManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVrManager)) {
                return new Proxy(obj);
            }
            return (IVrManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: android.app.Vr2dDisplayProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.app.Vr2dDisplayProperties} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                Vr2dDisplayProperties _arg02 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(IVrStateCallbacks.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(IVrStateCallbacks.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        registerPersistentVrStateListener(IPersistentVrStateCallbacks.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterPersistentVrStateListener(IPersistentVrStateCallbacks.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = getVrModeState();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = getPersistentVrModeEnabled();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setPersistentVrModeEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Vr2dDisplayProperties.CREATOR.createFromParcel(data);
                        }
                        setVr2dDisplayProperties(_arg02);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getVr2dDisplayId();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setAndBindCompositor(data.readString());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setStandbyEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setVrInputMethod(_arg02);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean getPersistentVrModeEnabled() throws RemoteException;

    int getVr2dDisplayId() throws RemoteException;

    boolean getVrModeState() throws RemoteException;

    void registerListener(IVrStateCallbacks iVrStateCallbacks) throws RemoteException;

    void registerPersistentVrStateListener(IPersistentVrStateCallbacks iPersistentVrStateCallbacks) throws RemoteException;

    void setAndBindCompositor(String str) throws RemoteException;

    void setPersistentVrModeEnabled(boolean z) throws RemoteException;

    void setStandbyEnabled(boolean z) throws RemoteException;

    void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProperties) throws RemoteException;

    void setVrInputMethod(ComponentName componentName) throws RemoteException;

    void unregisterListener(IVrStateCallbacks iVrStateCallbacks) throws RemoteException;

    void unregisterPersistentVrStateListener(IPersistentVrStateCallbacks iPersistentVrStateCallbacks) throws RemoteException;
}
