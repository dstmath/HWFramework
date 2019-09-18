package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IHwSecurityDiagnoseCallback;

public interface IHwSecurityDiagnosePlugin extends IInterface {

    public static abstract class Stub extends Binder implements IHwSecurityDiagnosePlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityDiagnosePlugin";
        static final int TRANSACTION_componentValid = 4;
        static final int TRANSACTION_getRootStatus = 5;
        static final int TRANSACTION_getRootStatusSync = 6;
        static final int TRANSACTION_getSystemStatus = 2;
        static final int TRANSACTION_getSystemStatusSync = 7;
        static final int TRANSACTION_report = 1;
        static final int TRANSACTION_sendComponentInfo = 3;
        static final int TRANSACTION_sendThreatenInfo = 8;

        private static class Proxy implements IHwSecurityDiagnosePlugin {
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

            public int report(int reporterId, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reporterId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSystemStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendComponentInfo(Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean componentValid(String componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public void getRootStatus(IHwSecurityDiagnoseCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRootStatusSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSystemStatusSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeByte(status);
                    _data.writeByte(credible);
                    _data.writeByte(version);
                    _data.writeString(name);
                    _data.writeString(addition_info);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSecurityDiagnosePlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSecurityDiagnosePlugin)) {
                return new Proxy(obj);
            }
            return (IHwSecurityDiagnosePlugin) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                Bundle _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        int _result = report(_arg02, _arg0);
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = getSystemStatus();
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        sendComponentInfo(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = componentValid(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        getRootStatus(IHwSecurityDiagnoseCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = getRootStatusSync();
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result5 = getSystemStatusSync();
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result6 = sendThreatenInfo(data.readInt(), data.readByte(), data.readByte(), data.readByte(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean componentValid(String str) throws RemoteException;

    void getRootStatus(IHwSecurityDiagnoseCallback iHwSecurityDiagnoseCallback) throws RemoteException;

    int getRootStatusSync() throws RemoteException;

    int getSystemStatus() throws RemoteException;

    int getSystemStatusSync() throws RemoteException;

    int report(int i, Bundle bundle) throws RemoteException;

    void sendComponentInfo(Bundle bundle) throws RemoteException;

    int sendThreatenInfo(int i, byte b, byte b2, byte b3, String str, String str2) throws RemoteException;
}
