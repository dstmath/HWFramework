package com.mediatek.ims.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;

public interface IMtkImsRegistrationListener extends IInterface {
    void onRedirectIncomingCallIndication(int i, String[] strArr) throws RemoteException;

    void onRegistrationImsStateChanged(int i, Uri[] uriArr, int i2, ImsReasonInfo imsReasonInfo) throws RemoteException;

    public static class Default implements IMtkImsRegistrationListener {
        @Override // com.mediatek.ims.internal.IMtkImsRegistrationListener
        public void onRegistrationImsStateChanged(int state, Uri[] uris, int expireTime, ImsReasonInfo imsReasonInfo) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsRegistrationListener
        public void onRedirectIncomingCallIndication(int phoneId, String[] info) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsRegistrationListener {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsRegistrationListener";
        static final int TRANSACTION_onRedirectIncomingCallIndication = 2;
        static final int TRANSACTION_onRegistrationImsStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsRegistrationListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsRegistrationListener)) {
                return new Proxy(obj);
            }
            return (IMtkImsRegistrationListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsReasonInfo _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                Uri[] _arg1 = (Uri[]) data.createTypedArray(Uri.CREATOR);
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                onRegistrationImsStateChanged(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onRedirectIncomingCallIndication(data.readInt(), data.createStringArray());
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
        public static class Proxy implements IMtkImsRegistrationListener {
            public static IMtkImsRegistrationListener sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsRegistrationListener
            public void onRegistrationImsStateChanged(int state, Uri[] uris, int expireTime, ImsReasonInfo imsReasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeTypedArray(uris, 0);
                    _data.writeInt(expireTime);
                    if (imsReasonInfo != null) {
                        _data.writeInt(1);
                        imsReasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRegistrationImsStateChanged(state, uris, expireTime, imsReasonInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsRegistrationListener
            public void onRedirectIncomingCallIndication(int phoneId, String[] info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeStringArray(info);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRedirectIncomingCallIndication(phoneId, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsRegistrationListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsRegistrationListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
