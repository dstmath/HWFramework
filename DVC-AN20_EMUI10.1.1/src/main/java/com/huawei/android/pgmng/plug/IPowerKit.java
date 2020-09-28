package com.huawei.android.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.android.pgmng.plug.IPowerKitSink;
import java.util.List;
import java.util.Map;

public interface IPowerKit extends IInterface {
    boolean applyForResourceUse(String str, boolean z, String str2, int i, long j, String str3) throws RemoteException;

    boolean checkStateByPid(String str, int i, int i2) throws RemoteException;

    boolean checkStateByPkg(String str, String str2, int i) throws RemoteException;

    boolean disableStateEvent(int i) throws RemoteException;

    boolean enableStateEvent(int i) throws RemoteException;

    boolean fastHibernation(String str, List<AppInfo> list, int i, String str2) throws RemoteException;

    List<DetailBatterySipper> getBatteryStats(String str, List<UserHandle> list) throws RemoteException;

    List<String> getHibernateApps(String str) throws RemoteException;

    int getPkgType(String str, String str2) throws RemoteException;

    Map getSensorInfoByUid(String str, int i) throws RemoteException;

    int[] getSupportedStates() throws RemoteException;

    int getThermalInfo(String str, int i) throws RemoteException;

    String getTopFrontApp(String str) throws RemoteException;

    boolean hibernateApps(String str, List<String> list, String str2) throws RemoteException;

    boolean isKeptAliveApp(String str, String str2, int i) throws RemoteException;

    boolean isStateSupported(int i) throws RemoteException;

    boolean notifyCallingModules(String str, String str2, List<String> list) throws RemoteException;

    boolean registerSink(IPowerKitSink iPowerKitSink) throws RemoteException;

    boolean unregisterSink(IPowerKitSink iPowerKitSink) throws RemoteException;

    public static class Default implements IPowerKit {
        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean checkStateByPid(String callingPkg, int pid, int state) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean checkStateByPkg(String callingPkg, String pkg, int state) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public int getPkgType(String callingPkg, String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public List<String> getHibernateApps(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean hibernateApps(String callingPkg, List<String> list, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public String getTopFrontApp(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public int[] getSupportedStates() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean isStateSupported(int stateType) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean registerSink(IPowerKitSink sink) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean unregisterSink(IPowerKitSink sink) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean enableStateEvent(int stateType) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean disableStateEvent(int stateType) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public Map getSensorInfoByUid(String callingPkg, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public int getThermalInfo(String callingPkg, int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean fastHibernation(String callingPkg, List<AppInfo> list, int duration, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public List<DetailBatterySipper> getBatteryStats(String callingPkg, List<UserHandle> list) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean applyForResourceUse(String callingPkg, boolean apply, String module, int resourceType, long timeoutInMS, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean notifyCallingModules(String callingPkg, String module, List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKit
        public boolean isKeptAliveApp(String callingPkg, String pkg, int uid) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPowerKit {
        private static final String DESCRIPTOR = "com.huawei.android.pgmng.plug.IPowerKit";
        static final int TRANSACTION_applyForResourceUse = 17;
        static final int TRANSACTION_checkStateByPid = 1;
        static final int TRANSACTION_checkStateByPkg = 2;
        static final int TRANSACTION_disableStateEvent = 12;
        static final int TRANSACTION_enableStateEvent = 11;
        static final int TRANSACTION_fastHibernation = 15;
        static final int TRANSACTION_getBatteryStats = 16;
        static final int TRANSACTION_getHibernateApps = 4;
        static final int TRANSACTION_getPkgType = 3;
        static final int TRANSACTION_getSensorInfoByUid = 13;
        static final int TRANSACTION_getSupportedStates = 7;
        static final int TRANSACTION_getThermalInfo = 14;
        static final int TRANSACTION_getTopFrontApp = 6;
        static final int TRANSACTION_hibernateApps = 5;
        static final int TRANSACTION_isKeptAliveApp = 19;
        static final int TRANSACTION_isStateSupported = 8;
        static final int TRANSACTION_notifyCallingModules = 18;
        static final int TRANSACTION_registerSink = 9;
        static final int TRANSACTION_unregisterSink = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPowerKit asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPowerKit)) {
                return new Proxy(obj);
            }
            return (IPowerKit) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkStateByPid = checkStateByPid(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkStateByPid ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkStateByPkg = checkStateByPkg(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkStateByPkg ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getPkgType(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getHibernateApps(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hibernateApps = hibernateApps(data.readString(), data.createStringArrayList(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(hibernateApps ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getTopFrontApp(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result4 = getSupportedStates();
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isStateSupported = isStateSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isStateSupported ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerSink = registerSink(IPowerKitSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerSink ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterSink = unregisterSink(IPowerKitSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterSink ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableStateEvent = enableStateEvent(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableStateEvent ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableStateEvent = disableStateEvent(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableStateEvent ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result5 = getSensorInfoByUid(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result5);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getThermalInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean fastHibernation = fastHibernation(data.readString(), data.createTypedArrayList(AppInfo.CREATOR), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(fastHibernation ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        List<DetailBatterySipper> _result7 = getBatteryStats(data.readString(), data.createTypedArrayList(UserHandle.CREATOR));
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean applyForResourceUse = applyForResourceUse(data.readString(), data.readInt() != 0, data.readString(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(applyForResourceUse ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean notifyCallingModules = notifyCallingModules(data.readString(), data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(notifyCallingModules ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isKeptAliveApp = isKeptAliveApp(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isKeptAliveApp ? 1 : 0);
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
        public static class Proxy implements IPowerKit {
            public static IPowerKit sDefaultImpl;
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean checkStateByPid(String callingPkg, int pid, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(pid);
                    _data.writeInt(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkStateByPid(callingPkg, pid, state);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean checkStateByPkg(String callingPkg, String pkg, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    _data.writeInt(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkStateByPkg(callingPkg, pkg, state);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public int getPkgType(String callingPkg, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPkgType(callingPkg, pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public List<String> getHibernateApps(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHibernateApps(callingPkg);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean hibernateApps(String callingPkg, List<String> pkgNames, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeStringList(pkgNames);
                    _data.writeString(reason);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hibernateApps(callingPkg, pkgNames, reason);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public String getTopFrontApp(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopFrontApp(callingPkg);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public int[] getSupportedStates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedStates();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean isStateSupported(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isStateSupported(stateType);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean registerSink(IPowerKitSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSink(sink);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean unregisterSink(IPowerKitSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterSink(sink);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean enableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableStateEvent(stateType);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean disableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableStateEvent(stateType);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public Map getSensorInfoByUid(String callingPkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSensorInfoByUid(callingPkg, uid);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public int getThermalInfo(String callingPkg, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getThermalInfo(callingPkg, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean fastHibernation(String callingPkg, List<AppInfo> appInfo, int duration, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeTypedList(appInfo);
                    _data.writeInt(duration);
                    _data.writeString(reason);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fastHibernation(callingPkg, appInfo, duration, reason);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public List<DetailBatterySipper> getBatteryStats(String callingPkg, List<UserHandle> userList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeTypedList(userList);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBatteryStats(callingPkg, userList);
                    }
                    _reply.readException();
                    List<DetailBatterySipper> _result = _reply.createTypedArrayList(DetailBatterySipper.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean applyForResourceUse(String callingPkg, boolean apply, String module, int resourceType, long timeoutInMS, String reason) throws RemoteException {
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPkg);
                        _result = true;
                        _data.writeInt(apply ? 1 : 0);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(module);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(resourceType);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(timeoutInMS);
                        _data.writeString(reason);
                        if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() == 0) {
                                _result = false;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean applyForResourceUse = Stub.getDefaultImpl().applyForResourceUse(callingPkg, apply, module, resourceType, timeoutInMS, reason);
                        _reply.recycle();
                        _data.recycle();
                        return applyForResourceUse;
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean notifyCallingModules(String callingPkg, String module, List<String> callingModules) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(module);
                    _data.writeStringList(callingModules);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyCallingModules(callingPkg, module, callingModules);
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

            @Override // com.huawei.android.pgmng.plug.IPowerKit
            public boolean isKeptAliveApp(String callingPkg, String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isKeptAliveApp(callingPkg, pkg, uid);
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

        public static boolean setDefaultImpl(IPowerKit impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPowerKit getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
