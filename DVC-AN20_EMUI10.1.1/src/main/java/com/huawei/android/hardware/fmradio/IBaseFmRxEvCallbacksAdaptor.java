package com.huawei.android.hardware.fmradio;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBaseFmRxEvCallbacksAdaptor extends IInterface {
    void FmRxEvRadioTuneStatus(int i) throws RemoteException;

    void FmRxEvRdsAfInfo() throws RemoteException;

    void FmRxEvRdsInfo(int i) throws RemoteException;

    void FmRxEvRdsLockStatus(boolean z) throws RemoteException;

    void FmRxEvRdsPsInfo() throws RemoteException;

    void FmRxEvRdsRtInfo() throws RemoteException;

    void FmRxEvSearchCancelled() throws RemoteException;

    void FmRxEvSearchComplete(int i) throws RemoteException;

    void FmRxEvSearchInProgress() throws RemoteException;

    public static abstract class Stub extends Binder implements IBaseFmRxEvCallbacksAdaptor {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor";
        static final int TRANSACTION_FmRxEvRadioTuneStatus = 1;
        static final int TRANSACTION_FmRxEvRdsAfInfo = 7;
        static final int TRANSACTION_FmRxEvRdsInfo = 6;
        static final int TRANSACTION_FmRxEvRdsLockStatus = 2;
        static final int TRANSACTION_FmRxEvRdsPsInfo = 8;
        static final int TRANSACTION_FmRxEvRdsRtInfo = 9;
        static final int TRANSACTION_FmRxEvSearchCancelled = 4;
        static final int TRANSACTION_FmRxEvSearchComplete = 5;
        static final int TRANSACTION_FmRxEvSearchInProgress = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBaseFmRxEvCallbacksAdaptor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBaseFmRxEvCallbacksAdaptor)) {
                return new Proxy(obj);
            }
            return (IBaseFmRxEvCallbacksAdaptor) iin;
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
                        FmRxEvRadioTuneStatus(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvRdsLockStatus(data.readInt() != 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvSearchInProgress();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvSearchCancelled();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvSearchComplete(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvRdsInfo(data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvRdsAfInfo();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvRdsPsInfo();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        FmRxEvRdsRtInfo();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IBaseFmRxEvCallbacksAdaptor {
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

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRadioTuneStatus(int freq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(freq);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRdsLockStatus(boolean rdsAvail) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rdsAvail ? 1 : 0);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvSearchInProgress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvSearchCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvSearchComplete(int freq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(freq);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRdsInfo(int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRdsAfInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRdsPsInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
            public void FmRxEvRdsRtInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
