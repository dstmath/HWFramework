package com.huawei.pgmng.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IPGManager extends IInterface {

    public static abstract class Stub extends Binder implements IPGManager {
        private static final String DESCRIPTOR = "com.huawei.pgmng.api.IPGManager";
        static final int TRANSACTION_closeSocketsForUid = 15;
        static final int TRANSACTION_configBrightnessRange = 12;
        static final int TRANSACTION_forceReleaseWakeLockByPidUid = 6;
        static final int TRANSACTION_forceRestoreWakeLockByPidUid = 7;
        static final int TRANSACTION_getWakeLockByUid = 9;
        static final int TRANSACTION_getWlBatteryStats = 13;
        static final int TRANSACTION_killProc = 17;
        static final int TRANSACTION_proxyApp = 10;
        static final int TRANSACTION_proxyBCConfig = 11;
        static final int TRANSACTION_proxyBroadcast = 1;
        static final int TRANSACTION_proxyBroadcastByPid = 2;
        static final int TRANSACTION_proxyWakeLockByPidUid = 5;
        static final int TRANSACTION_refreshPackageWhitelist = 16;
        static final int TRANSACTION_setActionExcludePkg = 4;
        static final int TRANSACTION_setLcdRatio = 8;
        static final int TRANSACTION_setPgConfig = 14;
        static final int TRANSACTION_setProxyBCActions = 3;

        private static class Proxy implements IPGManager {
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

            public long proxyBroadcast(List<String> pkgs, boolean proxy) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (!proxy) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long proxyBroadcastByPid(List<String> pids, boolean proxy) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pids);
                    if (proxy) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProxyBCActions(List<String> actions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(actions);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setActionExcludePkg(String action, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(pkg);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (proxy) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceReleaseWakeLockByPidUid(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceRestoreWakeLockByPidUid(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLcdRatio(int ratio, boolean autoAdjust) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ratio);
                    if (autoAdjust) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getWakeLockByUid(int uid, int wakeflag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(wakeflag);
                    this.mRemote.transact(9, _data, _reply, 0);
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

            public boolean proxyApp(String pkg, int uid, boolean proxy) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (proxy) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, _reply, 0);
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

            public void proxyBCConfig(int type, String key, List<String> value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(key);
                    _data.writeStringList(value);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ratioMin);
                    _data.writeInt(ratioMax);
                    _data.writeInt(autoLimit);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getWlBatteryStats(List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPgConfig(int type, int subType, List<String> value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(subType);
                    _data.writeStringList(value);
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean closeSocketsForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public void refreshPackageWhitelist(int type, List<String> pkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeStringList(pkgList);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void killProc(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(17, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            boolean _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = proxyBroadcast(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = proxyBroadcastByPid(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setProxyBCActions(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setActionExcludePkg(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    proxyWakeLockByPidUid(data.readInt(), data.readInt(), data.readInt() != 0);
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
                    setLcdRatio(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWakeLockByUid(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = proxyApp(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    proxyBCConfig(data.readInt(), data.readString(), data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    configBrightnessRange(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _arg0 = new ArrayList();
                    getWlBatteryStats(_arg0);
                    reply.writeNoException();
                    reply.writeStringList(_arg0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setPgConfig(data.readInt(), data.readInt(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = closeSocketsForUid(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    refreshPackageWhitelist(data.readInt(), data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    killProc(data.readInt());
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

    boolean closeSocketsForUid(int i) throws RemoteException;

    void configBrightnessRange(int i, int i2, int i3) throws RemoteException;

    void forceReleaseWakeLockByPidUid(int i, int i2) throws RemoteException;

    void forceRestoreWakeLockByPidUid(int i, int i2) throws RemoteException;

    boolean getWakeLockByUid(int i, int i2) throws RemoteException;

    void getWlBatteryStats(List<String> list) throws RemoteException;

    void killProc(int i) throws RemoteException;

    boolean proxyApp(String str, int i, boolean z) throws RemoteException;

    void proxyBCConfig(int i, String str, List<String> list) throws RemoteException;

    long proxyBroadcast(List<String> list, boolean z) throws RemoteException;

    long proxyBroadcastByPid(List<String> list, boolean z) throws RemoteException;

    void proxyWakeLockByPidUid(int i, int i2, boolean z) throws RemoteException;

    void refreshPackageWhitelist(int i, List<String> list) throws RemoteException;

    void setActionExcludePkg(String str, String str2) throws RemoteException;

    void setLcdRatio(int i, boolean z) throws RemoteException;

    boolean setPgConfig(int i, int i2, List<String> list) throws RemoteException;

    void setProxyBCActions(List<String> list) throws RemoteException;
}
