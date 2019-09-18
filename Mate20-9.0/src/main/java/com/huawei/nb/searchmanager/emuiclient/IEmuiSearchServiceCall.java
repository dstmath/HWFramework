package com.huawei.nb.searchmanager.emuiclient;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorDescriptorEx;
import java.util.List;

public interface IEmuiSearchServiceCall extends IInterface {

    public static abstract class Stub extends Binder implements IEmuiSearchServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.emuiclient.IEmuiSearchServiceCall";
        static final int TRANSACTION_executeClearData = 3;
        static final int TRANSACTION_executeDBCrawl = 1;
        static final int TRANSACTION_executeSearch = 2;

        private static class Proxy implements IEmuiSearchServiceCall {
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

            public void executeDBCrawl(String pkgName, List<String> idList, int op, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStringList(idList);
                    _data.writeInt(op);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BulkCursorDescriptorEx.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void executeClearData(String pkgName, int userId, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        executeDBCrawl(data.readString(), data.createStringArrayList(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
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
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        executeClearData(data.readString(), data.readInt(), data.readString());
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

    void executeClearData(String str, int i, String str2) throws RemoteException;

    void executeDBCrawl(String str, List<String> list, int i, String str2) throws RemoteException;

    BulkCursorDescriptorEx executeSearch(String str, String str2, List<String> list, String str3) throws RemoteException;
}
