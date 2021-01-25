package com.huawei.android.appassistant;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAppAssistantService extends IInterface {
    boolean addAssistantList(List<String> list, String str) throws RemoteException;

    boolean delAssistantList(List<String> list, String str) throws RemoteException;

    List<String> getAssistantList(String str) throws RemoteException;

    boolean isAssistantForeground(String str) throws RemoteException;

    boolean isHasAssistantFunc(String str) throws RemoteException;

    boolean isInAssistantList(String str, String str2) throws RemoteException;

    public static class Default implements IAppAssistantService {
        @Override // com.huawei.android.appassistant.IAppAssistantService
        public boolean addAssistantList(List<String> list, String func) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.appassistant.IAppAssistantService
        public boolean delAssistantList(List<String> list, String func) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.appassistant.IAppAssistantService
        public boolean isAssistantForeground(String func) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.appassistant.IAppAssistantService
        public boolean isInAssistantList(String packageName, String func) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.appassistant.IAppAssistantService
        public List<String> getAssistantList(String func) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.appassistant.IAppAssistantService
        public boolean isHasAssistantFunc(String func) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppAssistantService {
        private static final String DESCRIPTOR = "com.huawei.android.appassistant.IAppAssistantService";
        static final int TRANSACTION_addAssistantList = 1;
        static final int TRANSACTION_delAssistantList = 2;
        static final int TRANSACTION_getAssistantList = 5;
        static final int TRANSACTION_isAssistantForeground = 3;
        static final int TRANSACTION_isHasAssistantFunc = 6;
        static final int TRANSACTION_isInAssistantList = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppAssistantService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppAssistantService)) {
                return new Proxy(obj);
            }
            return (IAppAssistantService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addAssistantList";
                case 2:
                    return "delAssistantList";
                case 3:
                    return "isAssistantForeground";
                case 4:
                    return "isInAssistantList";
                case 5:
                    return "getAssistantList";
                case 6:
                    return "isHasAssistantFunc";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addAssistantList = addAssistantList(data.createStringArrayList(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(addAssistantList ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean delAssistantList = delAssistantList(data.createStringArrayList(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(delAssistantList ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAssistantForeground = isAssistantForeground(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAssistantForeground ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInAssistantList = isInAssistantList(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isInAssistantList ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getAssistantList(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHasAssistantFunc = isHasAssistantFunc(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHasAssistantFunc ? 1 : 0);
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
        public static class Proxy implements IAppAssistantService {
            public static IAppAssistantService sDefaultImpl;
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

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public boolean addAssistantList(List<String> packageName, String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageName);
                    _data.writeString(func);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addAssistantList(packageName, func);
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

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public boolean delAssistantList(List<String> packageName, String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageName);
                    _data.writeString(func);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().delAssistantList(packageName, func);
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

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public boolean isAssistantForeground(String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(func);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAssistantForeground(func);
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

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public boolean isInAssistantList(String packageName, String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(func);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInAssistantList(packageName, func);
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

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public List<String> getAssistantList(String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(func);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAssistantList(func);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.appassistant.IAppAssistantService
            public boolean isHasAssistantFunc(String func) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(func);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHasAssistantFunc(func);
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
        }

        public static boolean setDefaultImpl(IAppAssistantService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppAssistantService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
