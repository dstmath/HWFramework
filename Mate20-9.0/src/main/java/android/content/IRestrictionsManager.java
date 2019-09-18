package android.content;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;

public interface IRestrictionsManager extends IInterface {

    public static abstract class Stub extends Binder implements IRestrictionsManager {
        private static final String DESCRIPTOR = "android.content.IRestrictionsManager";
        static final int TRANSACTION_createLocalApprovalIntent = 5;
        static final int TRANSACTION_getApplicationRestrictions = 1;
        static final int TRANSACTION_hasRestrictionsProvider = 2;
        static final int TRANSACTION_notifyPermissionResponse = 4;
        static final int TRANSACTION_requestPermission = 3;

        private static class Proxy implements IRestrictionsManager {
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

            public Bundle getApplicationRestrictions(String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasRestrictionsProvider() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public void requestPermission(String packageName, String requestType, String requestId, PersistableBundle requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(requestType);
                    _data.writeString(requestId);
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
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

            public void notifyPermissionResponse(String packageName, PersistableBundle response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent createLocalApprovalIntent() throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRestrictionsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRestrictionsManager)) {
                return new Proxy(obj);
            }
            return (IRestrictionsManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                PersistableBundle _arg1 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result = getApplicationRestrictions(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = hasRestrictionsProvider();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        String _arg12 = data.readString();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                        }
                        requestPermission(_arg0, _arg12, _arg2, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                        }
                        notifyPermissionResponse(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        Intent _result3 = createLocalApprovalIntent();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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

    Intent createLocalApprovalIntent() throws RemoteException;

    Bundle getApplicationRestrictions(String str) throws RemoteException;

    boolean hasRestrictionsProvider() throws RemoteException;

    void notifyPermissionResponse(String str, PersistableBundle persistableBundle) throws RemoteException;

    void requestPermission(String str, String str2, String str3, PersistableBundle persistableBundle) throws RemoteException;
}
