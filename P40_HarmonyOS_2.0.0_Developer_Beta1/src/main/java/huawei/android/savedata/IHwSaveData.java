package huawei.android.savedata;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHwSaveData extends IInterface {
    IBinder getBinderObject(String str) throws RemoteException;

    Map getBinderObjects(String str) throws RemoteException;

    void putBinderObject(String str, IBinder iBinder) throws RemoteException;

    void removeBinderObject(String str) throws RemoteException;

    public static class Default implements IHwSaveData {
        @Override // huawei.android.savedata.IHwSaveData
        public void putBinderObject(String name, IBinder binder) throws RemoteException {
        }

        @Override // huawei.android.savedata.IHwSaveData
        public void removeBinderObject(String name) throws RemoteException {
        }

        @Override // huawei.android.savedata.IHwSaveData
        public IBinder getBinderObject(String name) throws RemoteException {
            return null;
        }

        @Override // huawei.android.savedata.IHwSaveData
        public Map getBinderObjects(String prefix) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSaveData {
        private static final String DESCRIPTOR = "huawei.android.savedata.IHwSaveData";
        static final int TRANSACTION_getBinderObject = 3;
        static final int TRANSACTION_getBinderObjects = 4;
        static final int TRANSACTION_putBinderObject = 1;
        static final int TRANSACTION_removeBinderObject = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSaveData asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSaveData)) {
                return new Proxy(obj);
            }
            return (IHwSaveData) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                putBinderObject(data.readString(), data.readStrongBinder());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                removeBinderObject(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _result = getBinderObject(data.readString());
                reply.writeNoException();
                reply.writeStrongBinder(_result);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                Map _result2 = getBinderObjects(data.readString());
                reply.writeNoException();
                reply.writeMap(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSaveData {
            public static IHwSaveData sDefaultImpl;
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

            @Override // huawei.android.savedata.IHwSaveData
            public void putBinderObject(String name, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeStrongBinder(binder);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().putBinderObject(name, binder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.savedata.IHwSaveData
            public void removeBinderObject(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeBinderObject(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.savedata.IHwSaveData
            public IBinder getBinderObject(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBinderObject(name);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.savedata.IHwSaveData
            public Map getBinderObjects(String prefix) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(prefix);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBinderObjects(prefix);
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
        }

        public static boolean setDefaultImpl(IHwSaveData impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSaveData getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
