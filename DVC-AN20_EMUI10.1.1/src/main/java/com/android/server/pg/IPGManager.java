package com.android.server.pg;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IPGManager extends IInterface {
    void forceReleaseWakeLockByPidUid(int i, int i2) throws RemoteException;

    void forceRestoreWakeLockByPidUid(int i, int i2) throws RemoteException;

    void getWakeLockBatteryStats(List<String> list) throws RemoteException;

    boolean getWakeLockByUid(int i, int i2) throws RemoteException;

    void killProc(int i) throws RemoteException;

    boolean proxyAppGps(String str, int i, boolean z) throws RemoteException;

    long proxyBroadcast(List<String> list, boolean z) throws RemoteException;

    long proxyBroadcastByPid(List<String> list, boolean z) throws RemoteException;

    void proxyBroadcastConfig(int i, String str, List<String> list) throws RemoteException;

    void proxyWakeLockByPidUid(int i, int i2, boolean z) throws RemoteException;

    void refreshPackageWhitelist(int i, List<String> list) throws RemoteException;

    void setActionExcludePkg(String str, String str2) throws RemoteException;

    boolean setPgConfig(int i, int i2, List<String> list) throws RemoteException;

    void setProxyBroadcastActions(List<String> list) throws RemoteException;

    public static class Default implements IPGManager {
        @Override // com.android.server.pg.IPGManager
        public long proxyBroadcast(List<String> list, boolean proxy) throws RemoteException {
            return 0;
        }

        @Override // com.android.server.pg.IPGManager
        public long proxyBroadcastByPid(List<String> list, boolean proxy) throws RemoteException {
            return 0;
        }

        @Override // com.android.server.pg.IPGManager
        public void setProxyBroadcastActions(List<String> list) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void setActionExcludePkg(String action, String pkg) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void forceReleaseWakeLockByPidUid(int pid, int uid) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void forceRestoreWakeLockByPidUid(int pid, int uid) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public boolean getWakeLockByUid(int uid, int wakeflag) throws RemoteException {
            return false;
        }

        @Override // com.android.server.pg.IPGManager
        public boolean proxyAppGps(String pkg, int uid, boolean proxy) throws RemoteException {
            return false;
        }

        @Override // com.android.server.pg.IPGManager
        public void proxyBroadcastConfig(int type, String key, List<String> list) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void getWakeLockBatteryStats(List<String> list) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public boolean setPgConfig(int type, int subType, List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.android.server.pg.IPGManager
        public void refreshPackageWhitelist(int type, List<String> list) throws RemoteException {
        }

        @Override // com.android.server.pg.IPGManager
        public void killProc(int pid) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPGManager {
        private static final String DESCRIPTOR = "com.android.server.pg.IPGManager";
        static final int TRANSACTION_forceReleaseWakeLockByPidUid = 6;
        static final int TRANSACTION_forceRestoreWakeLockByPidUid = 7;
        static final int TRANSACTION_getWakeLockBatteryStats = 11;
        static final int TRANSACTION_getWakeLockByUid = 8;
        static final int TRANSACTION_killProc = 14;
        static final int TRANSACTION_proxyAppGps = 9;
        static final int TRANSACTION_proxyBroadcast = 1;
        static final int TRANSACTION_proxyBroadcastByPid = 2;
        static final int TRANSACTION_proxyBroadcastConfig = 10;
        static final int TRANSACTION_proxyWakeLockByPidUid = 5;
        static final int TRANSACTION_refreshPackageWhitelist = 13;
        static final int TRANSACTION_setActionExcludePkg = 4;
        static final int TRANSACTION_setPgConfig = 12;
        static final int TRANSACTION_setProxyBroadcastActions = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPGManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPGManager)) {
                return new Proxy(obj);
            }
            return (IPGManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "proxyBroadcast";
                case 2:
                    return "proxyBroadcastByPid";
                case 3:
                    return "setProxyBroadcastActions";
                case 4:
                    return "setActionExcludePkg";
                case 5:
                    return "proxyWakeLockByPidUid";
                case 6:
                    return "forceReleaseWakeLockByPidUid";
                case 7:
                    return "forceRestoreWakeLockByPidUid";
                case 8:
                    return "getWakeLockByUid";
                case 9:
                    return "proxyAppGps";
                case 10:
                    return "proxyBroadcastConfig";
                case 11:
                    return "getWakeLockBatteryStats";
                case 12:
                    return "setPgConfig";
                case 13:
                    return "refreshPackageWhitelist";
                case 14:
                    return "killProc";
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
                boolean _arg2 = false;
                boolean _arg22 = false;
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg0 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        long _result = proxyBroadcast(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg02 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        long _result2 = proxyBroadcastByPid(_arg02, _arg12);
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setProxyBroadcastActions(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setActionExcludePkg(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        proxyWakeLockByPidUid(_arg03, _arg13, _arg22);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        forceReleaseWakeLockByPidUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        forceRestoreWakeLockByPidUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wakeLockByUid = getWakeLockByUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(wakeLockByUid ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean proxyAppGps = proxyAppGps(_arg04, _arg14, _arg2);
                        reply.writeNoException();
                        reply.writeInt(proxyAppGps ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        proxyBroadcastConfig(data.readInt(), data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg05 = new ArrayList<>();
                        getWakeLockBatteryStats(_arg05);
                        reply.writeNoException();
                        reply.writeStringList(_arg05);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean pgConfig = setPgConfig(data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(pgConfig ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        refreshPackageWhitelist(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        killProc(data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements IPGManager {
            public static IPGManager sDefaultImpl;
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

            @Override // com.android.server.pg.IPGManager
            public long proxyBroadcast(List<String> pkgs, boolean proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    _data.writeInt(proxy ? 1 : 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().proxyBroadcast(pkgs, proxy);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public long proxyBroadcastByPid(List<String> pids, boolean proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pids);
                    _data.writeInt(proxy ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().proxyBroadcastByPid(pids, proxy);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void setProxyBroadcastActions(List<String> actions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(actions);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProxyBroadcastActions(actions);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void setActionExcludePkg(String action, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(pkg);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActionExcludePkg(action, pkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(proxy ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().proxyWakeLockByPidUid(pid, uid, proxy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void forceReleaseWakeLockByPidUid(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceReleaseWakeLockByPidUid(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void forceRestoreWakeLockByPidUid(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceRestoreWakeLockByPidUid(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public boolean getWakeLockByUid(int uid, int wakeflag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(wakeflag);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWakeLockByUid(uid, wakeflag);
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

            @Override // com.android.server.pg.IPGManager
            public boolean proxyAppGps(String pkg, int uid, boolean proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = true;
                    _data.writeInt(proxy ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().proxyAppGps(pkg, uid, proxy);
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

            @Override // com.android.server.pg.IPGManager
            public void proxyBroadcastConfig(int type, String key, List<String> value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(key);
                    _data.writeStringList(value);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().proxyBroadcastConfig(type, key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void getWakeLockBatteryStats(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readStringList(list);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getWakeLockBatteryStats(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public boolean setPgConfig(int type, int subType, List<String> value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(subType);
                    _data.writeStringList(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPgConfig(type, subType, value);
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

            @Override // com.android.server.pg.IPGManager
            public void refreshPackageWhitelist(int type, List<String> pkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeStringList(pkgList);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().refreshPackageWhitelist(type, pkgList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.pg.IPGManager
            public void killProc(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killProc(pid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPGManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPGManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
