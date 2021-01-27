package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHandoffServiceCallback extends IInterface {
    int batchRegisterHandoff(String str) throws RemoteException;

    Map getHandoffBindRelationMap(String str, int i) throws RemoteException;

    boolean isEnableHandoff() throws RemoteException;

    boolean isHandoffServiceSupported(String str, int i) throws RemoteException;

    int realRegisterHandoff(String str, int i) throws RemoteException;

    int realUnRegisterHandoff(String str, int i) throws RemoteException;

    int syncHandoffData(String str, String str2) throws RemoteException;

    int unbindHandoffRelation(String str, int i, String str2) throws RemoteException;

    public static class Default implements IHandoffServiceCallback {
        @Override // android.emcom.IHandoffServiceCallback
        public int realRegisterHandoff(String packageName, int dataType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int realUnRegisterHandoff(String packageName, int dataType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int batchRegisterHandoff(String handoffRegisterInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int syncHandoffData(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public boolean isEnableHandoff() throws RemoteException {
            return false;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
            return false;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHandoffServiceCallback {
        private static final String DESCRIPTOR = "android.emcom.IHandoffServiceCallback";
        static final int TRANSACTION_batchRegisterHandoff = 3;
        static final int TRANSACTION_getHandoffBindRelationMap = 7;
        static final int TRANSACTION_isEnableHandoff = 5;
        static final int TRANSACTION_isHandoffServiceSupported = 6;
        static final int TRANSACTION_realRegisterHandoff = 1;
        static final int TRANSACTION_realUnRegisterHandoff = 2;
        static final int TRANSACTION_syncHandoffData = 4;
        static final int TRANSACTION_unbindHandoffRelation = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHandoffServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHandoffServiceCallback)) {
                return new Proxy(obj);
            }
            return (IHandoffServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = realRegisterHandoff(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = realUnRegisterHandoff(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = batchRegisterHandoff(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = syncHandoffData(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnableHandoff = isEnableHandoff();
                        reply.writeNoException();
                        reply.writeInt(isEnableHandoff ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHandoffServiceSupported = isHandoffServiceSupported(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isHandoffServiceSupported ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result5 = getHandoffBindRelationMap(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = unbindHandoffRelation(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
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
        public static class Proxy implements IHandoffServiceCallback {
            public static IHandoffServiceCallback sDefaultImpl;
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

            @Override // android.emcom.IHandoffServiceCallback
            public int realRegisterHandoff(String packageName, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().realRegisterHandoff(packageName, dataType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int realUnRegisterHandoff(String packageName, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().realUnRegisterHandoff(packageName, dataType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int batchRegisterHandoff(String handoffRegisterInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(handoffRegisterInfo);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().batchRegisterHandoff(handoffRegisterInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int syncHandoffData(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().syncHandoffData(packageName, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public boolean isEnableHandoff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnableHandoff();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHandoffServiceSupported(packageName, serviceType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHandoffBindRelationMap(packageName, serviceType);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    _data.writeString(nfcSn);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unbindHandoffRelation(packageName, serviceType, nfcSn);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHandoffServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHandoffServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
