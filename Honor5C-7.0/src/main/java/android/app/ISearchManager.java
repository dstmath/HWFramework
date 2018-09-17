package android.app;

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

        private static class Proxy implements ISearchManager {
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

            public SearchableInfo getSearchableInfo(ComponentName launchActivity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SearchableInfo searchableInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (launchActivity != null) {
                        _data.writeInt(Stub.TRANSACTION_getSearchableInfo);
                        launchActivity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getSearchableInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        searchableInfo = (SearchableInfo) SearchableInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        searchableInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return searchableInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<SearchableInfo> getSearchablesInGlobalSearch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSearchablesInGlobalSearch, _data, _reply, 0);
                    _reply.readException();
                    List<SearchableInfo> _result = _reply.createTypedArrayList(SearchableInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ResolveInfo> getGlobalSearchActivities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getGlobalSearchActivities, _data, _reply, 0);
                    _reply.readException();
                    List<ResolveInfo> _result = _reply.createTypedArrayList(ResolveInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getGlobalSearchActivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getGlobalSearchActivity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getWebSearchActivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWebSearchActivity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void launchAssist(Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_getSearchableInfo);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_launchAssist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean launchLegacyAssist(String hint, int userHandle, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(hint);
                    _data.writeInt(userHandle);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_getSearchableInfo);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_launchLegacyAssist, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<SearchableInfo> getOnlineSearchablesInGlobalSearch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getOnlineSearchablesInGlobalSearch, _data, _reply, 0);
                    _reply.readException();
                    List<SearchableInfo> _result = _reply.createTypedArrayList(SearchableInfo.CREATOR);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<SearchableInfo> _result;
            ComponentName _result2;
            switch (code) {
                case TRANSACTION_getSearchableInfo /*1*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    SearchableInfo _result3 = getSearchableInfo(componentName);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getSearchableInfo);
                        _result3.writeToParcel(reply, TRANSACTION_getSearchableInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSearchablesInGlobalSearch /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSearchablesInGlobalSearch();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getGlobalSearchActivities /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ResolveInfo> _result4 = getGlobalSearchActivities();
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_getGlobalSearchActivity /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getGlobalSearchActivity();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getSearchableInfo);
                        _result2.writeToParcel(reply, (int) TRANSACTION_getSearchableInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getWebSearchActivity /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWebSearchActivity();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getSearchableInfo);
                        _result2.writeToParcel(reply, (int) TRANSACTION_getSearchableInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_launchAssist /*6*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    launchAssist(bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_launchLegacyAssist /*7*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    boolean _result5 = launchLegacyAssist(_arg0, _arg1, bundle2);
                    reply.writeNoException();
                    reply.writeInt(_result5 ? TRANSACTION_getSearchableInfo : 0);
                    return true;
                case TRANSACTION_getOnlineSearchablesInGlobalSearch /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getOnlineSearchablesInGlobalSearch();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    List<ResolveInfo> getGlobalSearchActivities() throws RemoteException;

    ComponentName getGlobalSearchActivity() throws RemoteException;

    List<SearchableInfo> getOnlineSearchablesInGlobalSearch() throws RemoteException;

    SearchableInfo getSearchableInfo(ComponentName componentName) throws RemoteException;

    List<SearchableInfo> getSearchablesInGlobalSearch() throws RemoteException;

    ComponentName getWebSearchActivity() throws RemoteException;

    void launchAssist(Bundle bundle) throws RemoteException;

    boolean launchLegacyAssist(String str, int i, Bundle bundle) throws RemoteException;
}
