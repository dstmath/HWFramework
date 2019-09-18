package com.huawei.nb.searchmanager.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;
import java.util.List;

public interface ISearchServiceCall extends IInterface {

    public static abstract class Stub extends Binder implements ISearchServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.client.ISearchServiceCall";
        static final int TRANSACTION_executeAnalyzeText = 9;
        static final int TRANSACTION_executeClearData = 3;
        static final int TRANSACTION_executeDBCrawl = 1;
        static final int TRANSACTION_executeDeleteIndex = 7;
        static final int TRANSACTION_executeFileCrawl = 4;
        static final int TRANSACTION_executeInsertIndex = 5;
        static final int TRANSACTION_executeIntentSearch = 8;
        static final int TRANSACTION_executeSearch = 2;
        static final int TRANSACTION_executeUpdateIndex = 6;
        static final int TRANSACTION_setSearchSwitch = 10;

        private static class Proxy implements ISearchServiceCall {
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

            public BulkCursorDescriptor executeSearch(String pkgName, String queryString, List<String> fieldList, List<Attributes> attrsList, String callingPkgName) throws RemoteException {
                BulkCursorDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(queryString);
                    _data.writeStringList(fieldList);
                    _data.writeTypedList(attrsList);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BulkCursorDescriptor.CREATOR.createFromParcel(_reply);
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
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void executeFileCrawl(String pkgName, String filePath, boolean crawlContent, int op, String callingPkgName) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(filePath);
                    if (crawlContent) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(op);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeInsertIndex(String pkgName, List<SearchIndexData> dataList, List<Attributes> attrsList, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeTypedList(dataList);
                    _data.writeTypedList(attrsList);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeUpdateIndex(String pkgName, List<SearchIndexData> dataList, List<Attributes> attrsList, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeTypedList(dataList);
                    _data.writeTypedList(attrsList);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeDeleteIndex(String pkgName, List<String> idList, List<Attributes> attrsList, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStringList(idList);
                    _data.writeTypedList(attrsList);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<SearchIntentItem> executeIntentSearch(String pkgName, String queryString, List<String> fieldList, String type, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(queryString);
                    _data.writeStringList(fieldList);
                    _data.writeString(type);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(SearchIntentItem.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<Word> executeAnalyzeText(String pkgName, String text, String callingPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(text);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(Word.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSearchSwitch(String pkgName, boolean isSwitchOn, String callingPkgName) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (isSwitchOn) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPkgName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchServiceCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISearchServiceCall)) {
                return new Proxy(obj);
            }
            return (ISearchServiceCall) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    executeDBCrawl(data.readString(), data.createStringArrayList(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    BulkCursorDescriptor _result = executeSearch(data.readString(), data.readString(), data.createStringArrayList(), data.createTypedArrayList(Attributes.CREATOR), data.readString());
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
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    executeFileCrawl(data.readString(), data.readString(), data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = executeInsertIndex(data.readString(), data.createTypedArrayList(SearchIndexData.CREATOR), data.createTypedArrayList(Attributes.CREATOR), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = executeUpdateIndex(data.readString(), data.createTypedArrayList(SearchIndexData.CREATOR), data.createTypedArrayList(Attributes.CREATOR), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = executeDeleteIndex(data.readString(), data.createStringArrayList(), data.createTypedArrayList(Attributes.CREATOR), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    List<SearchIntentItem> _result5 = executeIntentSearch(data.readString(), data.readString(), data.createStringArrayList(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    List<Word> _result6 = executeAnalyzeText(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    setSearchSwitch(data.readString(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    List<Word> executeAnalyzeText(String str, String str2, String str3) throws RemoteException;

    void executeClearData(String str, int i, String str2) throws RemoteException;

    void executeDBCrawl(String str, List<String> list, int i, String str2) throws RemoteException;

    int executeDeleteIndex(String str, List<String> list, List<Attributes> list2, String str2) throws RemoteException;

    void executeFileCrawl(String str, String str2, boolean z, int i, String str3) throws RemoteException;

    int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException;

    List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3, String str4) throws RemoteException;

    BulkCursorDescriptor executeSearch(String str, String str2, List<String> list, List<Attributes> list2, String str3) throws RemoteException;

    int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException;

    void setSearchSwitch(String str, boolean z, String str2) throws RemoteException;
}
