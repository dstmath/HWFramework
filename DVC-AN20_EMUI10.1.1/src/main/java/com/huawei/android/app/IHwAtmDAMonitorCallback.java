package com.huawei.android.app;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAtmDAMonitorCallback extends IInterface {
    boolean isResourceNeeded(String str) throws RemoteException;

    void noteActivityDisplayed(String str, int i, int i2, boolean z) throws RemoteException;

    void noteActivityStart(String str, String str2, String str3, int i, int i2, boolean z) throws RemoteException;

    void notifyActivityState(String str) throws RemoteException;

    void notifyAppEventToIaware(int i, String str) throws RemoteException;

    void recognizeFakeActivity(String str, int i, int i2) throws RemoteException;

    void reportData(String str, long j, Bundle bundle) throws RemoteException;

    public static class Default implements IHwAtmDAMonitorCallback {
        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void notifyAppEventToIaware(int type, String packageName) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public boolean isResourceNeeded(String resourceid) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void recognizeFakeActivity(String compName, int pid, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void notifyActivityState(String activityInfo) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
        public void noteActivityDisplayed(String componentName, int uid, int pid, boolean isStart) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAtmDAMonitorCallback {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwAtmDAMonitorCallback";
        static final int TRANSACTION_isResourceNeeded = 3;
        static final int TRANSACTION_noteActivityDisplayed = 7;
        static final int TRANSACTION_noteActivityStart = 1;
        static final int TRANSACTION_notifyActivityState = 6;
        static final int TRANSACTION_notifyAppEventToIaware = 2;
        static final int TRANSACTION_recognizeFakeActivity = 5;
        static final int TRANSACTION_reportData = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAtmDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAtmDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwAtmDAMonitorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "noteActivityStart";
                case 2:
                    return "notifyAppEventToIaware";
                case 3:
                    return "isResourceNeeded";
                case 4:
                    return "reportData";
                case 5:
                    return "recognizeFakeActivity";
                case 6:
                    return "notifyActivityState";
                case 7:
                    return "noteActivityDisplayed";
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
            Bundle _arg2;
            if (code != 1598968902) {
                boolean _arg3 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        noteActivityStart(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        notifyAppEventToIaware(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isResourceNeeded = isResourceNeeded(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isResourceNeeded ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        long _arg1 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        reportData(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        recognizeFakeActivity(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        notifyActivityState(data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg12 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        noteActivityDisplayed(_arg02, _arg12, _arg22, _arg3);
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
        public static class Proxy implements IHwAtmDAMonitorCallback {
            public static IHwAtmDAMonitorCallback sDefaultImpl;
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

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(processName);
                        try {
                            _data.writeString(activityName);
                            try {
                                _data.writeInt(pid);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _data.writeInt(started ? 1 : 0);
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().noteActivityStart(packageName, processName, activityName, pid, uid, started);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void notifyAppEventToIaware(int type, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyAppEventToIaware(type, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public boolean isResourceNeeded(String resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isResourceNeeded(resourceid);
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

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    _data.writeLong(timestamp);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(resourceid, timestamp, args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void recognizeFakeActivity(String compName, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(compName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().recognizeFakeActivity(compName, pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void notifyActivityState(String activityInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityInfo);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyActivityState(activityInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwAtmDAMonitorCallback
            public void noteActivityDisplayed(String componentName, int uid, int pid, boolean isStart) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(isStart ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().noteActivityDisplayed(componentName, uid, pid, isStart);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAtmDAMonitorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAtmDAMonitorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
