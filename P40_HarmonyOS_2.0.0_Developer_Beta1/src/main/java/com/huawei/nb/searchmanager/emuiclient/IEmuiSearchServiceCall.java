package com.huawei.nb.searchmanager.emuiclient;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorDescriptorEx;
import java.util.List;

public interface IEmuiSearchServiceCall extends IInterface {
    void executeClearData(String str, int i, String str2) throws RemoteException;

    void executeDBCrawl(String str, List<String> list, int i, String str2) throws RemoteException;

    BulkCursorDescriptorEx executeSearch(String str, String str2, List<String> list, String str3) throws RemoteException;

    public static class Default implements IEmuiSearchServiceCall {
        @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
        public void executeDBCrawl(String pkgName, List<String> list, int op, String callingPkgName) throws RemoteException {
        }

        @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
        public BulkCursorDescriptorEx executeSearch(String pkgName, String queryString, List<String> list, String callingPkgName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
        public void executeClearData(String pkgName, int userId, String callingPkgName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEmuiSearchServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall";
        static final int TRANSACTION_executeClearData = 3;
        static final int TRANSACTION_executeDBCrawl = 1;
        static final int TRANSACTION_executeSearch = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEmuiSearchServiceCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEmuiSearchServiceCall)) {
                return new Proxy(obj);
            }
            return (IEmuiSearchServiceCall) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                executeDBCrawl(data.readString(), data.createStringArrayList(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                BulkCursorDescriptorEx _result = executeSearch(data.readString(), data.readString(), data.createStringArrayList(), data.readString());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                executeClearData(data.readString(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IEmuiSearchServiceCall {
            public static IEmuiSearchServiceCall sDefaultImpl;
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

            @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
            public void executeDBCrawl(String pkgName, List<String> idList, int op, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStringList(idList);
                    _data.writeInt(op);
                    _data.writeString(callingPkgName);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().executeDBCrawl(pkgName, idList, op, callingPkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
            public BulkCursorDescriptorEx executeSearch(String pkgName, String queryString, List<String> fieldList, String callingPkgName) throws RemoteException {
                BulkCursorDescriptorEx _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(queryString);
                    _data.writeStringList(fieldList);
                    _data.writeString(callingPkgName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().executeSearch(pkgName, queryString, fieldList, callingPkgName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BulkCursorDescriptorEx.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall
            public void executeClearData(String pkgName, int userId, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    _data.writeString(callingPkgName);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().executeClearData(pkgName, userId, callingPkgName);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEmuiSearchServiceCall impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEmuiSearchServiceCall getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
