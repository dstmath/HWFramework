package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHandoffSdkInterface extends IInterface {
    Map getHandoffBindRelationMap(String str, int i) throws RemoteException;

    boolean isEnableHandoff() throws RemoteException;

    boolean isHandoffServiceSupported(String str, int i) throws RemoteException;

    int startHandoffService(String str, String str2) throws RemoteException;

    int stopHandoffService(String str, String str2) throws RemoteException;

    int syncHandoffData(String str, String str2) throws RemoteException;

    int unbindHandoffRelation(String str, int i, String str2) throws RemoteException;

    public static class Default implements IHandoffSdkInterface {
        @Override // android.emcom.IHandoffSdkInterface
        public int startHandoffService(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public int stopHandoffService(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public int syncHandoffData(String packageName, String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public boolean isEnableHandoff() throws RemoteException {
            return false;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
            return false;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
            return null;
        }

        @Override // android.emcom.IHandoffSdkInterface
        public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHandoffSdkInterface {
        private static final String DESCRIPTOR = "android.emcom.IHandoffSdkInterface";
        static final int TRANSACTION_getHandoffBindRelationMap = 6;
        static final int TRANSACTION_isEnableHandoff = 4;
        static final int TRANSACTION_isHandoffServiceSupported = 5;
        static final int TRANSACTION_startHandoffService = 1;
        static final int TRANSACTION_stopHandoffService = 2;
        static final int TRANSACTION_syncHandoffData = 3;
        static final int TRANSACTION_unbindHandoffRelation = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHandoffSdkInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHandoffSdkInterface)) {
                return new Proxy(obj);
            }
            return (IHandoffSdkInterface) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = startHandoffService(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = stopHandoffService(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = syncHandoffData(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnableHandoff = isEnableHandoff();
                        reply.writeNoException();
                        reply.writeInt(isEnableHandoff ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHandoffServiceSupported = isHandoffServiceSupported(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isHandoffServiceSupported ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result4 = getHandoffBindRelationMap(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = unbindHandoffRelation(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
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
        public static class Proxy implements IHandoffSdkInterface {
            public static IHandoffSdkInterface sDefaultImpl;
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

            @Override // android.emcom.IHandoffSdkInterface
            public int startHandoffService(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startHandoffService(packageName, para);
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

            @Override // android.emcom.IHandoffSdkInterface
            public int stopHandoffService(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopHandoffService(packageName, para);
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

            @Override // android.emcom.IHandoffSdkInterface
            public int syncHandoffData(String packageName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(para);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.emcom.IHandoffSdkInterface
            public boolean isEnableHandoff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.emcom.IHandoffSdkInterface
            public boolean isHandoffServiceSupported(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.emcom.IHandoffSdkInterface
            public Map getHandoffBindRelationMap(String packageName, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.emcom.IHandoffSdkInterface
            public int unbindHandoffRelation(String packageName, int serviceType, String nfcSn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(serviceType);
                    _data.writeString(nfcSn);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

        public static boolean setDefaultImpl(IHandoffSdkInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHandoffSdkInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
