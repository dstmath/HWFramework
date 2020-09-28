package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPEvent4D3DStatus extends IInterface {
    public static final int FACEREC_FAILED = 3001;
    public static final int FACEREC_FAILED_TO_SAVE = 3002;
    public static final int FACEREC_KEYFRAME_CODE = 20;
    public static final int FACEREC_KEYPOSE_CODE = 22;
    public static final int FACEREC_PROGRESS_BEGIN = 1000;
    public static final int FACEREC_PROGRESS_END = 2000;
    public static final int FACEREC_SHOOT_CODE = 21;
    public static final int FACEREC_SUCCEEDED = 3000;
    public static final int INVALID_VALUE = -1;
    public static final int REC_SHOOT_ALL = 11;
    public static final int REC_SHOOT_FACEBLINK = 16;
    public static final int REC_SHOOT_FACEERROR = 15;
    public static final int REC_SHOOT_FACESMALL = 18;
    public static final int REC_SHOOT_MULITYFACE = 13;
    public static final int REC_SHOOT_NOFACE = 12;
    public static final int REC_SHOOT_OK = 10;
    public static final int REC_SHOOT_OMRONERROR = 14;
    public static final int REC_SHOOT_PITCH = 19;
    public static final int REC_SHOOT_TURNFAST = 17;
    public static final int SHOOT_FACEBLINK = 4;
    public static final int SHOOT_FACECATCH = 6;
    public static final int SHOOT_FACEERROR = 3;
    public static final int SHOOT_FACESMALL = 7;
    public static final int SHOOT_MULITYFACE = 1;
    public static final int SHOOT_NOFACE = 0;
    public static final int SHOOT_OMRONERROR = 2;
    public static final int SHOOT_PITCH = 8;
    public static final int SHOOT_TURNFAST = 5;

    int getStatus() throws RemoteException;

    public static class Default implements IIPEvent4D3DStatus {
        @Override // com.huawei.servicehost.d3d.IIPEvent4D3DStatus
        public int getStatus() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4D3DStatus {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IIPEvent4D3DStatus";
        static final int TRANSACTION_getStatus = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4D3DStatus asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4D3DStatus)) {
                return new Proxy(obj);
            }
            return (IIPEvent4D3DStatus) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getStatus();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIPEvent4D3DStatus {
            public static IIPEvent4D3DStatus sDefaultImpl;
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

            @Override // com.huawei.servicehost.d3d.IIPEvent4D3DStatus
            public int getStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatus();
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

        public static boolean setDefaultImpl(IIPEvent4D3DStatus impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4D3DStatus getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
