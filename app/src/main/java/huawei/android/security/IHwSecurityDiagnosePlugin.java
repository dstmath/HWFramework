package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwSecurityDiagnosePlugin extends IInterface {

    public static abstract class Stub extends Binder implements IHwSecurityDiagnosePlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityDiagnosePlugin";
        static final int TRANSACTION_componentValid = 4;
        static final int TRANSACTION_getSystemStatus = 2;
        static final int TRANSACTION_report = 1;
        static final int TRANSACTION_sendComponentInfo = 3;

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
                        _data.writeInt(Stub.TRANSACTION_report);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_report, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSystemStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_report);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendComponentInfo, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_componentValid, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
            int i = 0;
            int _result;
            switch (code) {
                case TRANSACTION_report /*1*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = report(_arg0, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getSystemStatus /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSystemStatus();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendComponentInfo /*3*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    sendComponentInfo(bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_componentValid /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result2 = componentValid(data.readString());
                    reply.writeNoException();
                    if (_result2) {
                        i = TRANSACTION_report;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean componentValid(String str) throws RemoteException;

    int getSystemStatus() throws RemoteException;

    int report(int i, Bundle bundle) throws RemoteException;

    void sendComponentInfo(Bundle bundle) throws RemoteException;
}
