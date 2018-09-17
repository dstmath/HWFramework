package com.huawei.android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IHwActivityManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwActivityManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwActivityManager";
        static final int TRANSACTION_cleanPackageRes = 3;
        static final int TRANSACTION_registerDAMonitorCallback = 1;
        static final int TRANSACTION_setCpusetSwitch = 2;

        private static class Proxy implements IHwActivityManager {
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

            public void registerDAMonitorCallback(IHwDAMonitorCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCpusetSwitch(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean cleanPackageRes(List<String> packageList, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    _data.writeMap(alarmTags);
                    _data.writeInt(targetUid);
                    if (cleanAlarm) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (isNative) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!hasPerceptAlarm) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(3, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwActivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwActivityManager)) {
                return new Proxy(obj);
            }
            return (IHwActivityManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    registerDAMonitorCallback(com.huawei.android.app.IHwDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    setCpusetSwitch(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = cleanPackageRes(data.createStringArrayList(), data.readHashMap(getClass().getClassLoader()), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean cleanPackageRes(List<String> list, Map map, int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void registerDAMonitorCallback(IHwDAMonitorCallback iHwDAMonitorCallback) throws RemoteException;

    void setCpusetSwitch(boolean z) throws RemoteException;
}
