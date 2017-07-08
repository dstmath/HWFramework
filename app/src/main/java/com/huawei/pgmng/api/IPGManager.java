package com.huawei.pgmng.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IPGManager extends IInterface {

    public static abstract class Stub extends Binder implements IPGManager {
        private static final String DESCRIPTOR = "com.huawei.pgmng.api.IPGManager";
        static final int TRANSACTION_configBrightnessRange = 12;
        static final int TRANSACTION_forceReleaseWakeLockByPidUid = 6;
        static final int TRANSACTION_forceRestoreWakeLockByPidUid = 7;
        static final int TRANSACTION_getWakeLockByUid = 9;
        static final int TRANSACTION_proxyApp = 10;
        static final int TRANSACTION_proxyBCConfig = 11;
        static final int TRANSACTION_proxyBroadcast = 1;
        static final int TRANSACTION_proxyBroadcastByPid = 2;
        static final int TRANSACTION_proxyWakeLockByPidUid = 5;
        static final int TRANSACTION_setActionExcludePkg = 4;
        static final int TRANSACTION_setLcdRatio = 8;
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
                int i = Stub.TRANSACTION_proxyBroadcast;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (!proxy) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_proxyBroadcast, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_proxyBroadcast;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_proxyBroadcastByPid, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setProxyBCActions, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setActionExcludePkg, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_proxyBroadcast;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_proxyWakeLockByPidUid, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_forceReleaseWakeLockByPidUid, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_forceRestoreWakeLockByPidUid, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_proxyBroadcast;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setLcdRatio, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getWakeLockByUid, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_proxyBroadcast;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_proxyApp, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_proxyBCConfig, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_configBrightnessRange, _data, _reply, 0);
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
                case TRANSACTION_proxyBroadcast /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = proxyBroadcast(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_proxyBroadcastByPid /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = proxyBroadcastByPid(data.createStringArrayList(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_setProxyBCActions /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setProxyBCActions(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setActionExcludePkg /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setActionExcludePkg(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_proxyWakeLockByPidUid /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    proxyWakeLockByPidUid(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceReleaseWakeLockByPidUid /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    forceReleaseWakeLockByPidUid(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceRestoreWakeLockByPidUid /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    forceRestoreWakeLockByPidUid(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setLcdRatio /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLcdRatio(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWakeLockByUid /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWakeLockByUid(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_proxyBroadcast : 0);
                    return true;
                case TRANSACTION_proxyApp /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = proxyApp(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_proxyBroadcast : 0);
                    return true;
                case TRANSACTION_proxyBCConfig /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    proxyBCConfig(data.readInt(), data.readString(), data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_configBrightnessRange /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    configBrightnessRange(data.readInt(), data.readInt(), data.readInt());
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

    void configBrightnessRange(int i, int i2, int i3) throws RemoteException;

    void forceReleaseWakeLockByPidUid(int i, int i2) throws RemoteException;

    void forceRestoreWakeLockByPidUid(int i, int i2) throws RemoteException;

    boolean getWakeLockByUid(int i, int i2) throws RemoteException;

    boolean proxyApp(String str, int i, boolean z) throws RemoteException;

    void proxyBCConfig(int i, String str, List<String> list) throws RemoteException;

    long proxyBroadcast(List<String> list, boolean z) throws RemoteException;

    long proxyBroadcastByPid(List<String> list, boolean z) throws RemoteException;

    void proxyWakeLockByPidUid(int i, int i2, boolean z) throws RemoteException;

    void setActionExcludePkg(String str, String str2) throws RemoteException;

    void setLcdRatio(int i, boolean z) throws RemoteException;

    void setProxyBCActions(List<String> list) throws RemoteException;
}
