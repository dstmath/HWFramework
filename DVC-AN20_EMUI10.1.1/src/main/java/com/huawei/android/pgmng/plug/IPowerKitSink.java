package com.huawei.android.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPowerKitSink extends IInterface {
    void onPowerOverUsing(String str, int i, long j, long j2, String str2) throws RemoteException;

    void onStateChanged(int i, int i2, int i3, String str, int i4) throws RemoteException;

    public static class Default implements IPowerKitSink {
        @Override // com.huawei.android.pgmng.plug.IPowerKitSink
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.pgmng.plug.IPowerKitSink
        public void onPowerOverUsing(String module, int resourceType, long stats_duration, long hold_time, String extend) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPowerKitSink {
        private static final String DESCRIPTOR = "com.huawei.android.pgmng.plug.IPowerKitSink";
        static final int TRANSACTION_onPowerOverUsing = 2;
        static final int TRANSACTION_onStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPowerKitSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPowerKitSink)) {
                return new Proxy(obj);
            }
            return (IPowerKitSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onStateChanged(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onPowerOverUsing(data.readString(), data.readInt(), data.readLong(), data.readLong(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPowerKitSink {
            public static IPowerKitSink sDefaultImpl;
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

            @Override // com.huawei.android.pgmng.plug.IPowerKitSink
            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    _data.writeInt(eventType);
                    _data.writeInt(pid);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStateChanged(stateType, eventType, pid, pkg, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.pgmng.plug.IPowerKitSink
            public void onPowerOverUsing(String module, int resourceType, long stats_duration, long hold_time, String extend) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(module);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(resourceType);
                        try {
                            _data.writeLong(stats_duration);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(hold_time);
                            _data.writeString(extend);
                            if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPowerOverUsing(module, resourceType, stats_duration, hold_time, extend);
                            _data.recycle();
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _data.recycle();
                    throw th;
                }
            }
        }

        public static boolean setDefaultImpl(IPowerKitSink impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPowerKitSink getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
