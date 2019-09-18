package android.se.omapi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.se.omapi.ISecureElementReader;

public interface ISecureElementService extends IInterface {

    public static abstract class Stub extends Binder implements ISecureElementService {
        private static final String DESCRIPTOR = "android.se.omapi.ISecureElementService";
        static final int TRANSACTION_getReader = 2;
        static final int TRANSACTION_getReaders = 1;
        static final int TRANSACTION_isNFCEventAllowed = 3;
        static final int TRANSACTION_isOffhostAidRouteAllowed = 4;

        private static class Proxy implements ISecureElementService {
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

            public String[] getReaders() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISecureElementReader getReader(String reader) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return ISecureElementReader.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean[] isNFCEventAllowed(String reader, byte[] aid, String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeByteArray(aid);
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBooleanArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOffhostAidRouteAllowed(String reader, String aid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeString(aid);
                    _data.writeString(packageName);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureElementService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecureElementService)) {
                return new Proxy(obj);
            }
            return (ISecureElementService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getReaders();
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ISecureElementReader _result2 = getReader(data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean[] _result3 = isNFCEventAllowed(data.readString(), data.createByteArray(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeBooleanArray(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = isOffhostAidRouteAllowed(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
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

    ISecureElementReader getReader(String str) throws RemoteException;

    String[] getReaders() throws RemoteException;

    boolean[] isNFCEventAllowed(String str, byte[] bArr, String[] strArr) throws RemoteException;

    boolean isOffhostAidRouteAllowed(String str, String str2, String str3) throws RemoteException;
}
