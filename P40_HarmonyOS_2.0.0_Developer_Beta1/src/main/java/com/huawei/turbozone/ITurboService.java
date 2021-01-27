package com.huawei.turbozone;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITurboService extends IInterface {
    int StartTurboZoneAdaptation(String str) throws RemoteException;

    int StopTurboZoneAdaptation() throws RemoteException;

    int TestTurboZone() throws RemoteException;

    public static class Default implements ITurboService {
        @Override // com.huawei.turbozone.ITurboService
        public int TestTurboZone() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.turbozone.ITurboService
        public int StartTurboZoneAdaptation(String userTopApps) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.turbozone.ITurboService
        public int StopTurboZoneAdaptation() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITurboService {
        private static final String DESCRIPTOR = "com.huawei.turbozone.ITurboService";
        static final int TRANSACTION_StartTurboZoneAdaptation = 2;
        static final int TRANSACTION_StopTurboZoneAdaptation = 3;
        static final int TRANSACTION_TestTurboZone = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITurboService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITurboService)) {
                return new Proxy(obj);
            }
            return (ITurboService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = TestTurboZone();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = StartTurboZoneAdaptation(data.readString());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = StopTurboZoneAdaptation();
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITurboService {
            public static ITurboService sDefaultImpl;
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

            @Override // com.huawei.turbozone.ITurboService
            public int TestTurboZone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().TestTurboZone();
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

            @Override // com.huawei.turbozone.ITurboService
            public int StartTurboZoneAdaptation(String userTopApps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(userTopApps);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().StartTurboZoneAdaptation(userTopApps);
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

            @Override // com.huawei.turbozone.ITurboService
            public int StopTurboZoneAdaptation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().StopTurboZoneAdaptation();
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
        }

        public static boolean setDefaultImpl(ITurboService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITurboService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
