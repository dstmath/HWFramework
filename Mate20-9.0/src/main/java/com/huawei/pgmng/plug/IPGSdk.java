package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.pgmng.plug.IStateRecognitionSink;
import java.util.List;
import java.util.Map;

public interface IPGSdk extends IInterface {

    public static abstract class Stub extends Binder implements IPGSdk {
        private static final String DESCRIPTOR = "com.huawei.pgmng.plug.IPGSdk";
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

        private static class Proxy implements IPGSdk {
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

            public boolean checkStateByPid(String callingPkg, int pid, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(pid);
                    _data.writeInt(state);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkStateByPkg(String callingPkg, String pkg, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    _data.writeInt(state);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPkgType(String callingPkg, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHibernateApps(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hibernateApps(String callingPkg, List<String> pkgNames, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeStringList(pkgNames);
                    _data.writeString(reason);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTopFrontApp(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getSupportedStates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStateSupported(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerSink(IStateRecognitionSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterSink(IStateRecognitionSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableStateEvent(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getSensorInfoByUid(String callingPkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getThermalInfo(String callingPkg, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(type);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<DetailBatterySipper> getBatteryStats(String callingPkg, List<UserHandle> userList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeTypedList(userList);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(DetailBatterySipper.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean applyForResourceUse(String callingPkg, boolean apply, String module, int resourceType, long timeoutInMS, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(apply);
                    _data.writeString(module);
                    _data.writeInt(resourceType);
                    _data.writeLong(timeoutInMS);
                    _data.writeString(reason);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean notifyCallingModules(String callingPkg, String module, List<String> callingModules) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(module);
                    _data.writeStringList(callingModules);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isKeptAliveApp(String callingPkg, String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
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

        public static IPGSdk asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPGSdk)) {
                return new Proxy(obj);
            }
            return (IPGSdk) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = checkStateByPid(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = checkStateByPkg(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result3 = getPkgType(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getHibernateApps(data.readString());
                        reply.writeNoException();
                        parcel2.writeStringList(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = hibernateApps(data.readString(), data.createStringArrayList(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result6 = getTopFrontApp(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getSupportedStates();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = isStateSupported(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = registerSink(IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result10 = unregisterSink(IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = enableStateEvent(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = disableStateEvent(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map _result13 = getSensorInfoByUid(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeMap(_result13);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result14 = getThermalInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result15 = fastHibernation(data.readString(), parcel.createTypedArrayList(AppInfo.CREATOR), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<DetailBatterySipper> _result16 = getBatteryStats(data.readString(), parcel.createTypedArrayList(UserHandle.CREATOR));
                        reply.writeNoException();
                        parcel2.writeTypedList(_result16);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result17 = applyForResourceUse(data.readString(), data.readInt() != 0, data.readString(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = notifyCallingModules(data.readString(), data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = isKeptAliveApp(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

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

    boolean registerSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;

    boolean unregisterSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;
}
