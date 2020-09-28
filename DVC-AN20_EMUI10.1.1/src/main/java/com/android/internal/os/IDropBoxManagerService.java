package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDropBoxManagerService extends IInterface {
    void add(DropBoxManager.Entry entry) throws RemoteException;

    @UnsupportedAppUsage
    DropBoxManager.Entry getNextEntry(String str, long j, String str2) throws RemoteException;

    boolean isTagEnabled(String str) throws RemoteException;

    public static class Default implements IDropBoxManagerService {
        @Override // com.android.internal.os.IDropBoxManagerService
        public void add(DropBoxManager.Entry entry) throws RemoteException {
        }

        @Override // com.android.internal.os.IDropBoxManagerService
        public boolean isTagEnabled(String tag) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.os.IDropBoxManagerService
        public DropBoxManager.Entry getNextEntry(String tag, long millis, String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDropBoxManagerService {
        private static final String DESCRIPTOR = "com.android.internal.os.IDropBoxManagerService";
        static final int TRANSACTION_add = 1;
        static final int TRANSACTION_getNextEntry = 3;
        static final int TRANSACTION_isTagEnabled = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDropBoxManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDropBoxManagerService)) {
                return new Proxy(obj);
            }
            return (IDropBoxManagerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "add";
            }
            if (transactionCode == 2) {
                return "isTagEnabled";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getNextEntry";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DropBoxManager.Entry _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DropBoxManager.Entry.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                add(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean isTagEnabled = isTagEnabled(data.readString());
                reply.writeNoException();
                reply.writeInt(isTagEnabled ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                DropBoxManager.Entry _result = getNextEntry(data.readString(), data.readLong(), data.readString());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDropBoxManagerService {
            public static IDropBoxManagerService sDefaultImpl;
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

            @Override // com.android.internal.os.IDropBoxManagerService
            public void add(DropBoxManager.Entry entry) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (entry != null) {
                        _data.writeInt(1);
                        entry.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().add(entry);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.os.IDropBoxManagerService
            public boolean isTagEnabled(String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tag);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTagEnabled(tag);
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

            @Override // com.android.internal.os.IDropBoxManagerService
            public DropBoxManager.Entry getNextEntry(String tag, long millis, String packageName) throws RemoteException {
                DropBoxManager.Entry _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tag);
                    _data.writeLong(millis);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNextEntry(tag, millis, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DropBoxManager.Entry.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
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

        public static boolean setDefaultImpl(IDropBoxManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDropBoxManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
