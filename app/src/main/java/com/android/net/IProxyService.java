package com.android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IProxyService extends IInterface {

    public static abstract class Stub extends Binder implements IProxyService {
        private static final String DESCRIPTOR = "com.android.net.IProxyService";
        static final int TRANSACTION_resolvePacFile = 1;
        static final int TRANSACTION_setPacFile = 2;
        static final int TRANSACTION_startPacSystem = 3;
        static final int TRANSACTION_stopPacSystem = 4;

        private static class Proxy implements IProxyService {
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

            public String resolvePacFile(String host, String url) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(host);
                    _data.writeString(url);
                    this.mRemote.transact(Stub.TRANSACTION_resolvePacFile, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPacFile(String scriptContents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(scriptContents);
                    this.mRemote.transact(Stub.TRANSACTION_setPacFile, _data, null, Stub.TRANSACTION_resolvePacFile);
                } finally {
                    _data.recycle();
                }
            }

            public void startPacSystem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startPacSystem, _data, null, Stub.TRANSACTION_resolvePacFile);
                } finally {
                    _data.recycle();
                }
            }

            public void stopPacSystem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopPacSystem, _data, null, Stub.TRANSACTION_resolvePacFile);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IProxyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IProxyService)) {
                return new Proxy(obj);
            }
            return (IProxyService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_resolvePacFile /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = resolvePacFile(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_setPacFile /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPacFile(data.readString());
                    return true;
                case TRANSACTION_startPacSystem /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    startPacSystem();
                    return true;
                case TRANSACTION_stopPacSystem /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopPacSystem();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String resolvePacFile(String str, String str2) throws RemoteException;

    void setPacFile(String str) throws RemoteException;

    void startPacSystem() throws RemoteException;

    void stopPacSystem() throws RemoteException;
}
