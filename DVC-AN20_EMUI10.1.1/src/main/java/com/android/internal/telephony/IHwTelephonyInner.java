package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwTelephonyInner extends IInterface {
    int getLevelForSa(int i, int i2, int i3) throws RemoteException;

    int getRrcConnectionState(int i) throws RemoteException;

    public static class Default implements IHwTelephonyInner {
        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getRrcConnectionState(int slotId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwTelephonyInner {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwTelephonyInner";
        static final int TRANSACTION_getLevelForSa = 1;
        static final int TRANSACTION_getRrcConnectionState = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwTelephonyInner asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwTelephonyInner)) {
                return new Proxy(obj);
            }
            return (IHwTelephonyInner) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getLevelForSa(data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = getRrcConnectionState(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwTelephonyInner {
            public static IHwTelephonyInner sDefaultImpl;
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(nrLevel);
                    _data.writeInt(primaryLevel);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLevelForSa(phoneId, nrLevel, primaryLevel);
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getRrcConnectionState(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRrcConnectionState(slotId);
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

        public static boolean setDefaultImpl(IHwTelephonyInner impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwTelephonyInner getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
