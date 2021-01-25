package huawei.android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwAlarmManagerEx extends IInterface {
    void removeAllAdjustAlarms() throws RemoteException;

    void removeAllPendingAlarms() throws RemoteException;

    void setAlarmsAdjust(List<String> list, List<String> list2, boolean z, int i, long j, int i2) throws RemoteException;

    void setAlarmsPending(List<String> list, List<String> list2, boolean z, int i) throws RemoteException;

    public static class Default implements IHwAlarmManagerEx {
        @Override // huawei.android.app.IHwAlarmManagerEx
        public void setAlarmsPending(List<String> list, List<String> list2, boolean pending, int allAlarms) throws RemoteException {
        }

        @Override // huawei.android.app.IHwAlarmManagerEx
        public void removeAllPendingAlarms() throws RemoteException {
        }

        @Override // huawei.android.app.IHwAlarmManagerEx
        public void setAlarmsAdjust(List<String> list, List<String> list2, boolean adjust, int type, long interval, int mode) throws RemoteException {
        }

        @Override // huawei.android.app.IHwAlarmManagerEx
        public void removeAllAdjustAlarms() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAlarmManagerEx {
        private static final String DESCRIPTOR = "huawei.android.app.IHwAlarmManagerEx";
        static final int TRANSACTION_removeAllAdjustAlarms = 4;
        static final int TRANSACTION_removeAllPendingAlarms = 2;
        static final int TRANSACTION_setAlarmsAdjust = 3;
        static final int TRANSACTION_setAlarmsPending = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAlarmManagerEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAlarmManagerEx)) {
                return new Proxy(obj);
            }
            return (IHwAlarmManagerEx) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg2 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _arg0 = data.createStringArrayList();
                List<String> _arg1 = data.createStringArrayList();
                if (data.readInt() != 0) {
                    _arg2 = true;
                }
                setAlarmsPending(_arg0, _arg1, _arg2, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                removeAllPendingAlarms();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setAlarmsAdjust(data.createStringArrayList(), data.createStringArrayList(), data.readInt() != 0, data.readInt(), data.readLong(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                removeAllAdjustAlarms();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwAlarmManagerEx {
            public static IHwAlarmManagerEx sDefaultImpl;
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

            @Override // huawei.android.app.IHwAlarmManagerEx
            public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int allAlarms) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    _data.writeStringList(actionList);
                    _data.writeInt(pending ? 1 : 0);
                    _data.writeInt(allAlarms);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAlarmsPending(pkgList, actionList, pending, allAlarms);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.app.IHwAlarmManagerEx
            public void removeAllPendingAlarms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllPendingAlarms();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.app.IHwAlarmManagerEx
            public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStringList(pkgList);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringList(actionList);
                        _data.writeInt(adjust ? 1 : 0);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(type);
                        try {
                            _data.writeLong(interval);
                            _data.writeInt(mode);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setAlarmsAdjust(pkgList, actionList, adjust, type, interval, mode);
                            _reply.recycle();
                            _data.recycle();
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.app.IHwAlarmManagerEx
            public void removeAllAdjustAlarms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllAdjustAlarms();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAlarmManagerEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAlarmManagerEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
