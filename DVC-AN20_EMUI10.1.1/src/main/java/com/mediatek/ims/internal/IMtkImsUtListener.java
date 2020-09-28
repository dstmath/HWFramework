package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallForwardInfo;
import com.mediatek.ims.MtkImsCallForwardInfo;
import com.mediatek.ims.internal.IMtkImsUt;

public interface IMtkImsUtListener extends IInterface {
    void utConfigurationCallForwardInTimeSlotQueried(IMtkImsUt iMtkImsUt, int i, MtkImsCallForwardInfo[] mtkImsCallForwardInfoArr) throws RemoteException;

    void utConfigurationCallForwardQueried(IMtkImsUt iMtkImsUt, int i, ImsCallForwardInfo[] imsCallForwardInfoArr) throws RemoteException;

    public static class Default implements IMtkImsUtListener {
        @Override // com.mediatek.ims.internal.IMtkImsUtListener
        public void utConfigurationCallForwardInTimeSlotQueried(IMtkImsUt ut, int id, MtkImsCallForwardInfo[] cfInfo) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsUtListener
        public void utConfigurationCallForwardQueried(IMtkImsUt ut, int id, ImsCallForwardInfo[] cfInfo) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsUtListener {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsUtListener";
        static final int TRANSACTION_utConfigurationCallForwardInTimeSlotQueried = 1;
        static final int TRANSACTION_utConfigurationCallForwardQueried = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsUtListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsUtListener)) {
                return new Proxy(obj);
            }
            return (IMtkImsUtListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                utConfigurationCallForwardInTimeSlotQueried(IMtkImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (MtkImsCallForwardInfo[]) data.createTypedArray(MtkImsCallForwardInfo.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                utConfigurationCallForwardQueried(IMtkImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsCallForwardInfo[]) data.createTypedArray(ImsCallForwardInfo.CREATOR));
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
        public static class Proxy implements IMtkImsUtListener {
            public static IMtkImsUtListener sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsUtListener
            public void utConfigurationCallForwardInTimeSlotQueried(IMtkImsUt ut, int id, MtkImsCallForwardInfo[] cfInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(ut != null ? ut.asBinder() : null);
                    _data.writeInt(id);
                    _data.writeTypedArray(cfInfo, 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().utConfigurationCallForwardInTimeSlotQueried(ut, id, cfInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsUtListener
            public void utConfigurationCallForwardQueried(IMtkImsUt ut, int id, ImsCallForwardInfo[] cfInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(ut != null ? ut.asBinder() : null);
                    _data.writeInt(id);
                    _data.writeTypedArray(cfInfo, 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().utConfigurationCallForwardQueried(ut, id, cfInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsUtListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsUtListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
