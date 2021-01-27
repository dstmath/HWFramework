package com.huawei.internal.telephony.vsim;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwVSimPhoneSwitchCallback extends IInterface {
    void onVsimPhoneSwitch(int i) throws RemoteException;

    public static class Default implements IHwVSimPhoneSwitchCallback {
        @Override // com.huawei.internal.telephony.vsim.IHwVSimPhoneSwitchCallback
        public void onVsimPhoneSwitch(int phoneId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwVSimPhoneSwitchCallback {
        private static final String DESCRIPTOR = "com.huawei.internal.telephony.vsim.IHwVSimPhoneSwitchCallback";
        static final int TRANSACTION_onVsimPhoneSwitch = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwVSimPhoneSwitchCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwVSimPhoneSwitchCallback)) {
                return new Proxy(obj);
            }
            return (IHwVSimPhoneSwitchCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onVsimPhoneSwitch(data.readInt());
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
        public static class Proxy implements IHwVSimPhoneSwitchCallback {
            public static IHwVSimPhoneSwitchCallback sDefaultImpl;
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

            @Override // com.huawei.internal.telephony.vsim.IHwVSimPhoneSwitchCallback
            public void onVsimPhoneSwitch(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVsimPhoneSwitch(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwVSimPhoneSwitchCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwVSimPhoneSwitchCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
