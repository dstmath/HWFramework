package com.huawei.securityserver;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.securityserver.IGeographyLocationCallback;
import java.util.Map;

public interface IGeographyLocation extends IInterface {
    int changeParams(Map map) throws RemoteException;

    int sendAntiRelayCommand(byte[] bArr, byte[] bArr2, byte b, IGeographyLocationCallback iGeographyLocationCallback) throws RemoteException;

    int startService(byte[] bArr, byte[] bArr2, IGeographyLocationCallback iGeographyLocationCallback) throws RemoteException;

    int stopService() throws RemoteException;

    public static abstract class Stub extends Binder implements IGeographyLocation {
        private static final String DESCRIPTOR = "com.huawei.securityserver.IGeographyLocation";
        static final int TRANSACTION_changeParams = 4;
        static final int TRANSACTION_sendAntiRelayCommand = 2;
        static final int TRANSACTION_startService = 1;
        static final int TRANSACTION_stopService = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeographyLocation asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeographyLocation)) {
                return new Proxy(obj);
            }
            return (IGeographyLocation) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = startService(data.createByteArray(), data.createByteArray(), IGeographyLocationCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = sendAntiRelayCommand(data.createByteArray(), data.createByteArray(), data.readByte(), IGeographyLocationCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = stopService();
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = changeParams(data.readHashMap(getClass().getClassLoader()));
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IGeographyLocation {
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

            @Override // com.huawei.securityserver.IGeographyLocation
            public int startService(byte[] entityId, byte[] sessionId, IGeographyLocationCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(entityId);
                    _data.writeByteArray(sessionId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.securityserver.IGeographyLocation
            public int sendAntiRelayCommand(byte[] entityId, byte[] sessionId, byte operationId, IGeographyLocationCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(entityId);
                    _data.writeByteArray(sessionId);
                    _data.writeByte(operationId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.securityserver.IGeographyLocation
            public int stopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.securityserver.IGeographyLocation
            public int changeParams(Map input) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(input);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
