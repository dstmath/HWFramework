package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import com.mediatek.ims.internal.IMtkImsCallSession;

public interface IMtkImsCallSessionListener extends IInterface {
    void callSessionBusy(IMtkImsCallSession iMtkImsCallSession) throws RemoteException;

    void callSessionCalling(IMtkImsCallSession iMtkImsCallSession) throws RemoteException;

    void callSessionDeviceSwitchFailed(IMtkImsCallSession iMtkImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionDeviceSwitched(IMtkImsCallSession iMtkImsCallSession) throws RemoteException;

    void callSessionMergeComplete(IMtkImsCallSession iMtkImsCallSession) throws RemoteException;

    void callSessionMergeStarted(IMtkImsCallSession iMtkImsCallSession, IMtkImsCallSession iMtkImsCallSession2, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionRedialEcc(IMtkImsCallSession iMtkImsCallSession, boolean z) throws RemoteException;

    void callSessionRinging(IMtkImsCallSession iMtkImsCallSession, ImsCallProfile imsCallProfile) throws RemoteException;

    void callSessionRttEventReceived(IMtkImsCallSession iMtkImsCallSession, int i) throws RemoteException;

    void callSessionTextCapabilityChanged(IMtkImsCallSession iMtkImsCallSession, int i, int i2, int i3, int i4) throws RemoteException;

    void callSessionTransferFailed(IMtkImsCallSession iMtkImsCallSession, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void callSessionTransferred(IMtkImsCallSession iMtkImsCallSession) throws RemoteException;

    public static class Default implements IMtkImsCallSessionListener {
        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionTransferred(IMtkImsCallSession session) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionTransferFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionTextCapabilityChanged(IMtkImsCallSession session, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionRttEventReceived(IMtkImsCallSession session, int event) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionMergeStarted(IMtkImsCallSession session, IMtkImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionMergeComplete(IMtkImsCallSession session) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionDeviceSwitched(IMtkImsCallSession session) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionDeviceSwitchFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionRedialEcc(IMtkImsCallSession session, boolean isNeedUserConfirm) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionRinging(IMtkImsCallSession session, ImsCallProfile profile) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionBusy(IMtkImsCallSession session) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
        public void callSessionCalling(IMtkImsCallSession session) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsCallSessionListener {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsCallSessionListener";
        static final int TRANSACTION_callSessionBusy = 11;
        static final int TRANSACTION_callSessionCalling = 12;
        static final int TRANSACTION_callSessionDeviceSwitchFailed = 8;
        static final int TRANSACTION_callSessionDeviceSwitched = 7;
        static final int TRANSACTION_callSessionMergeComplete = 6;
        static final int TRANSACTION_callSessionMergeStarted = 5;
        static final int TRANSACTION_callSessionRedialEcc = 9;
        static final int TRANSACTION_callSessionRinging = 10;
        static final int TRANSACTION_callSessionRttEventReceived = 4;
        static final int TRANSACTION_callSessionTextCapabilityChanged = 3;
        static final int TRANSACTION_callSessionTransferFailed = 2;
        static final int TRANSACTION_callSessionTransferred = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsCallSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsCallSessionListener)) {
                return new Proxy(obj);
            }
            return (IMtkImsCallSessionListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsReasonInfo _arg1;
            ImsCallProfile _arg2;
            ImsReasonInfo _arg12;
            ImsCallProfile _arg13;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionTransferred(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsCallSession _arg0 = IMtkImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        callSessionTransferFailed(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionTextCapabilityChanged(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionRttEventReceived(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsCallSession _arg02 = IMtkImsCallSession.Stub.asInterface(data.readStrongBinder());
                        IMtkImsCallSession _arg14 = IMtkImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        callSessionMergeStarted(_arg02, _arg14, _arg2);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionMergeComplete(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionDeviceSwitched(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callSessionDeviceSwitchFailed /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsCallSession _arg03 = IMtkImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        callSessionDeviceSwitchFailed(_arg03, _arg12);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callSessionRedialEcc /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionRedialEcc(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callSessionRinging /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsCallSession _arg04 = IMtkImsCallSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        callSessionRinging(_arg04, _arg13);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callSessionBusy /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionBusy(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callSessionCalling /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        callSessionCalling(IMtkImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMtkImsCallSessionListener {
            public static IMtkImsCallSessionListener sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionTransferred(IMtkImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionTransferred(session);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionTransferFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionTransferFailed(session, reasonInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionTextCapabilityChanged(IMtkImsCallSession session, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeInt(localCapability);
                    _data.writeInt(remoteCapability);
                    _data.writeInt(localTextStatus);
                    _data.writeInt(realRemoteCapability);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionTextCapabilityChanged(session, localCapability, remoteCapability, localTextStatus, realRemoteCapability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionRttEventReceived(IMtkImsCallSession session, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeInt(event);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionRttEventReceived(session, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionMergeStarted(IMtkImsCallSession session, IMtkImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionMergeStarted(session, newSession, profile);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionMergeComplete(IMtkImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionMergeComplete(session);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionDeviceSwitched(IMtkImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionDeviceSwitched(session);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionDeviceSwitchFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_callSessionDeviceSwitchFailed, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionDeviceSwitchFailed(session, reasonInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionRedialEcc(IMtkImsCallSession session, boolean isNeedUserConfirm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeInt(isNeedUserConfirm ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_callSessionRedialEcc, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionRedialEcc(session, isNeedUserConfirm);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionRinging(IMtkImsCallSession session, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_callSessionRinging, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionRinging(session, profile);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionBusy(IMtkImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_callSessionBusy, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionBusy(session);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSessionListener
            public void callSessionCalling(IMtkImsCallSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_callSessionCalling, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callSessionCalling(session);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsCallSessionListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsCallSessionListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
