package android.telephony.ims.aidl;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.internal.IImsCallSession;

public interface IImsMmTelListener extends IInterface {
    void onIncomingCall(IImsCallSession iImsCallSession, Bundle bundle) throws RemoteException;

    void onRejectedCall(ImsCallProfile imsCallProfile, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void onVoiceMessageCountUpdate(int i) throws RemoteException;

    public static class Default implements IImsMmTelListener {
        @Override // android.telephony.ims.aidl.IImsMmTelListener
        public void onIncomingCall(IImsCallSession c, Bundle extras) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IImsMmTelListener
        public void onRejectedCall(ImsCallProfile callProfile, ImsReasonInfo reason) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IImsMmTelListener
        public void onVoiceMessageCountUpdate(int count) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsMmTelListener {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IImsMmTelListener";
        static final int TRANSACTION_onIncomingCall = 1;
        static final int TRANSACTION_onRejectedCall = 2;
        static final int TRANSACTION_onVoiceMessageCountUpdate = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsMmTelListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsMmTelListener)) {
                return new Proxy(obj);
            }
            return (IImsMmTelListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onIncomingCall";
            }
            if (transactionCode == 2) {
                return "onRejectedCall";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onVoiceMessageCountUpdate";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            ImsCallProfile _arg0;
            ImsReasonInfo _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IImsCallSession _arg02 = IImsCallSession.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg1 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onIncomingCall(_arg02, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ImsCallProfile.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = ImsReasonInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                onRejectedCall(_arg0, _arg12);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onVoiceMessageCountUpdate(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IImsMmTelListener {
            public static IImsMmTelListener sDefaultImpl;
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

            @Override // android.telephony.ims.aidl.IImsMmTelListener
            public void onIncomingCall(IImsCallSession c, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onIncomingCall(c, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IImsMmTelListener
            public void onRejectedCall(ImsCallProfile callProfile, ImsReasonInfo reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callProfile != null) {
                        _data.writeInt(1);
                        callProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (reason != null) {
                        _data.writeInt(1);
                        reason.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRejectedCall(callProfile, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IImsMmTelListener
            public void onVoiceMessageCountUpdate(int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(count);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVoiceMessageCountUpdate(count);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsMmTelListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsMmTelListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
