package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ISearchManager extends IInterface {
    List<ResolveInfo> getGlobalSearchActivities() throws RemoteException;

    @UnsupportedAppUsage
    ComponentName getGlobalSearchActivity() throws RemoteException;

    List<SearchableInfo> getOnlineSearchablesInGlobalSearch() throws RemoteException;

    SearchableInfo getSearchableInfo(ComponentName componentName) throws RemoteException;

    List<SearchableInfo> getSearchablesInGlobalSearch() throws RemoteException;

    ComponentName getWebSearchActivity() throws RemoteException;

    void launchAssist(Bundle bundle) throws RemoteException;

    boolean launchLegacyAssist(String str, int i, Bundle bundle) throws RemoteException;

    public static class Default implements ISearchManager {
        @Override // android.app.ISearchManager
        public SearchableInfo getSearchableInfo(ComponentName launchActivity) throws RemoteException {
            return null;
        }

        @Override // android.app.ISearchManager
        public List<SearchableInfo> getSearchablesInGlobalSearch() throws RemoteException {
            return null;
        }

        @Override // android.app.ISearchManager
        public List<ResolveInfo> getGlobalSearchActivities() throws RemoteException {
            return null;
        }

        @Override // android.app.ISearchManager
        public ComponentName getGlobalSearchActivity() throws RemoteException {
            return null;
        }

        @Override // android.app.ISearchManager
        public ComponentName getWebSearchActivity() throws RemoteException {
            return null;
        }

        @Override // android.app.ISearchManager
        public void launchAssist(Bundle args) throws RemoteException {
        }

        @Override // android.app.ISearchManager
        public boolean launchLegacyAssist(String hint, int userHandle, Bundle args) throws RemoteException {
            return false;
        }

        @Override // android.app.ISearchManager
        public List<SearchableInfo> getOnlineSearchablesInGlobalSearch() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISearchManager {
        private static final String DESCRIPTOR = "android.app.ISearchManager";
        static final int TRANSACTION_getGlobalSearchActivities = 3;
        static final int TRANSACTION_getGlobalSearchActivity = 4;
        static final int TRANSACTION_getOnlineSearchablesInGlobalSearch = 8;
        static final int TRANSACTION_getSearchableInfo = 1;
        static final int TRANSACTION_getSearchablesInGlobalSearch = 2;
        static final int TRANSACTION_getWebSearchActivity = 5;
        static final int TRANSACTION_launchAssist = 6;
        static final int TRANSACTION_launchLegacyAssist = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISearchManager)) {
                return new Proxy(obj);
            }
            return (ISearchManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSearchableInfo";
                case 2:
                    return "getSearchablesInGlobalSearch";
                case 3:
                    return "getGlobalSearchActivities";
                case 4:
                    return "getGlobalSearchActivity";
                case 5:
                    return "getWebSearchActivity";
                case 6:
                    return "launchAssist";
                case 7:
                    return "launchLegacyAssist";
                case 8:
                    return "getOnlineSearchablesInGlobalSearch";
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
            ComponentName _arg0;
            Bundle _arg02;
            Bundle _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        SearchableInfo _result = getSearchableInfo(_arg0);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<SearchableInfo> _result2 = getSearchablesInGlobalSearch();
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<ResolveInfo> _result3 = getGlobalSearchActivities();
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result4 = getGlobalSearchActivity();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result5 = getWebSearchActivity();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        launchAssist(_arg02);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean launchLegacyAssist = launchLegacyAssist(_arg03, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(launchLegacyAssist ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<SearchableInfo> _result6 = getOnlineSearchablesInGlobalSearch();
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
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
        public static class Proxy implements ISearchManager {
            public static ISearchManager sDefaultImpl;
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

            @Override // android.app.ISearchManager
            public SearchableInfo getSearchableInfo(ComponentName launchActivity) throws RemoteException {
                SearchableInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (launchActivity != null) {
                        _data.writeInt(1);
                        launchActivity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSearchableInfo(launchActivity);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SearchableInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.ISearchManager
            public List<SearchableInfo> getSearchablesInGlobalSearch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSearchablesInGlobalSearch();
                    }
                    _reply.readException();
                    List<SearchableInfo> _result = _reply.createTypedArrayList(SearchableInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.ISearchManager
            public List<ResolveInfo> getGlobalSearchActivities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGlobalSearchActivities();
                    }
                    _reply.readException();
                    List<ResolveInfo> _result = _reply.createTypedArrayList(ResolveInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.ISearchManager
            public ComponentName getGlobalSearchActivity() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGlobalSearchActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.ISearchManager
            public ComponentName getWebSearchActivity() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWebSearchActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.ISearchManager
            public void launchAssist(Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().launchAssist(args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.ISearchManager
            public boolean launchLegacyAssist(String hint, int userHandle, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(hint);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().launchLegacyAssist(hint, userHandle, args);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.ISearchManager
            public List<SearchableInfo> getOnlineSearchablesInGlobalSearch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOnlineSearchablesInGlobalSearch();
                    }
                    _reply.readException();
                    List<SearchableInfo> _result = _reply.createTypedArrayList(SearchableInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISearchManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISearchManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
