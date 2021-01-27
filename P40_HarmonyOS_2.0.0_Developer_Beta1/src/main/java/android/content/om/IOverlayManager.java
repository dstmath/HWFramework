package android.content.om;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IOverlayManager extends IInterface {
    @UnsupportedAppUsage
    Map getAllOverlays(int i) throws RemoteException;

    String[] getDefaultOverlayPackages() throws RemoteException;

    @UnsupportedAppUsage
    OverlayInfo getOverlayInfo(String str, int i) throws RemoteException;

    List getOverlayInfosForTarget(String str, int i) throws RemoteException;

    boolean setEnabled(String str, boolean z, int i) throws RemoteException;

    boolean setEnabledExclusive(String str, boolean z, int i) throws RemoteException;

    boolean setEnabledExclusiveInCategory(String str, int i) throws RemoteException;

    boolean setHighestPriority(String str, int i) throws RemoteException;

    boolean setLowestPriority(String str, int i) throws RemoteException;

    boolean setPriority(String str, String str2, int i) throws RemoteException;

    public static class Default implements IOverlayManager {
        @Override // android.content.om.IOverlayManager
        public Map getAllOverlays(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.om.IOverlayManager
        public List getOverlayInfosForTarget(String targetPackageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.om.IOverlayManager
        public OverlayInfo getOverlayInfo(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setEnabled(String packageName, boolean enable, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setEnabledExclusiveInCategory(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setPriority(String packageName, String newParentPackageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.om.IOverlayManager
        public String[] getDefaultOverlayPackages() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOverlayManager {
        private static final String DESCRIPTOR = "android.content.om.IOverlayManager";
        static final int TRANSACTION_getAllOverlays = 1;
        static final int TRANSACTION_getDefaultOverlayPackages = 10;
        static final int TRANSACTION_getOverlayInfo = 3;
        static final int TRANSACTION_getOverlayInfosForTarget = 2;
        static final int TRANSACTION_setEnabled = 4;
        static final int TRANSACTION_setEnabledExclusive = 5;
        static final int TRANSACTION_setEnabledExclusiveInCategory = 6;
        static final int TRANSACTION_setHighestPriority = 8;
        static final int TRANSACTION_setLowestPriority = 9;
        static final int TRANSACTION_setPriority = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOverlayManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOverlayManager)) {
                return new Proxy(obj);
            }
            return (IOverlayManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getAllOverlays";
                case 2:
                    return "getOverlayInfosForTarget";
                case 3:
                    return "getOverlayInfo";
                case 4:
                    return "setEnabled";
                case 5:
                    return "setEnabledExclusive";
                case 6:
                    return "setEnabledExclusiveInCategory";
                case 7:
                    return "setPriority";
                case 8:
                    return "setHighestPriority";
                case 9:
                    return "setLowestPriority";
                case 10:
                    return "getDefaultOverlayPackages";
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
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result = getAllOverlays(data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List _result2 = getOverlayInfosForTarget(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        OverlayInfo _result3 = getOverlayInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean enabled = setEnabled(_arg0, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enabled ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean enabledExclusive = setEnabledExclusive(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enabledExclusive ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enabledExclusiveInCategory = setEnabledExclusiveInCategory(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enabledExclusiveInCategory ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean priority = setPriority(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(priority ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean highestPriority = setHighestPriority(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(highestPriority ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean lowestPriority = setLowestPriority(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(lowestPriority ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result4 = getDefaultOverlayPackages();
                        reply.writeNoException();
                        reply.writeStringArray(_result4);
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
        public static class Proxy implements IOverlayManager {
            public static IOverlayManager sDefaultImpl;
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

            @Override // android.content.om.IOverlayManager
            public Map getAllOverlays(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllOverlays(userId);
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

            @Override // android.content.om.IOverlayManager
            public List getOverlayInfosForTarget(String targetPackageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOverlayInfosForTarget(targetPackageName, userId);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.om.IOverlayManager
            public OverlayInfo getOverlayInfo(String packageName, int userId) throws RemoteException {
                OverlayInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOverlayInfo(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = OverlayInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.om.IOverlayManager
            public boolean setEnabled(String packageName, boolean enable, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEnabled(packageName, enable, userId);
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

            @Override // android.content.om.IOverlayManager
            public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEnabledExclusive(packageName, enable, userId);
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

            @Override // android.content.om.IOverlayManager
            public boolean setEnabledExclusiveInCategory(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEnabledExclusiveInCategory(packageName, userId);
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

            @Override // android.content.om.IOverlayManager
            public boolean setPriority(String packageName, String newParentPackageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(newParentPackageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPriority(packageName, newParentPackageName, userId);
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

            @Override // android.content.om.IOverlayManager
            public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHighestPriority(packageName, userId);
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

            @Override // android.content.om.IOverlayManager
            public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLowestPriority(packageName, userId);
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

            @Override // android.content.om.IOverlayManager
            public String[] getDefaultOverlayPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultOverlayPackages();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOverlayManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOverlayManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
