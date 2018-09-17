package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICas extends IInterface {

    public static abstract class Stub extends Binder implements ICas {
        private static final String DESCRIPTOR = "android.media.ICas";
        static final int TRANSACTION_closeSession = 3;
        static final int TRANSACTION_openSession = 2;
        static final int TRANSACTION_processEcm = 5;
        static final int TRANSACTION_processEmm = 6;
        static final int TRANSACTION_provision = 8;
        static final int TRANSACTION_refreshEntitlements = 9;
        static final int TRANSACTION_release = 10;
        static final int TRANSACTION_sendEvent = 7;
        static final int TRANSACTION_setPrivateData = 1;
        static final int TRANSACTION_setSessionPrivateData = 4;

        private static class Proxy implements ICas {
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

            public void setPrivateData(byte[] pvtData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(pvtData);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] openSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSession(byte[] sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(sessionId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSessionPrivateData(byte[] sessionId, byte[] pvtData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(sessionId);
                    _data.writeByteArray(pvtData);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void processEcm(byte[] sessionId, ParcelableCasData ecm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(sessionId);
                    if (ecm != null) {
                        _data.writeInt(1);
                        ecm.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void processEmm(ParcelableCasData emm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (emm != null) {
                        _data.writeInt(1);
                        emm.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendEvent(int event, int arg, byte[] eventData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(arg);
                    _data.writeByteArray(eventData);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void provision(String provisionString) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provisionString);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshEntitlements(int refreshType, byte[] refreshData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(refreshType);
                    _data.writeByteArray(refreshData);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
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

        public static ICas asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICas)) {
                return new Proxy(obj);
            }
            return (ICas) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setPrivateData(data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result = openSession();
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    closeSession(data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setSessionPrivateData(data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 5:
                    ParcelableCasData _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _arg0 = data.createByteArray();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelableCasData) ParcelableCasData.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    processEcm(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 6:
                    ParcelableCasData _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ParcelableCasData) ParcelableCasData.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    processEmm(_arg02);
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    sendEvent(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    provision(data.readString());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    refreshEntitlements(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    release();
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeSession(byte[] bArr) throws RemoteException;

    byte[] openSession() throws RemoteException;

    void processEcm(byte[] bArr, ParcelableCasData parcelableCasData) throws RemoteException;

    void processEmm(ParcelableCasData parcelableCasData) throws RemoteException;

    void provision(String str) throws RemoteException;

    void refreshEntitlements(int i, byte[] bArr) throws RemoteException;

    void release() throws RemoteException;

    void sendEvent(int i, int i2, byte[] bArr) throws RemoteException;

    void setPrivateData(byte[] bArr) throws RemoteException;

    void setSessionPrivateData(byte[] bArr, byte[] bArr2) throws RemoteException;
}
