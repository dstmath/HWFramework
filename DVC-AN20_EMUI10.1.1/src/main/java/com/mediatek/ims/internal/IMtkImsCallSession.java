package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.internal.IImsCallSession;
import com.mediatek.ims.internal.IMtkImsCallSessionListener;

public interface IMtkImsCallSession extends IInterface {
    void approveEccRedial(boolean z) throws RemoteException;

    void callTerminated() throws RemoteException;

    void cancelDeviceSwitch() throws RemoteException;

    void close() throws RemoteException;

    void deviceSwitch(String str, String str2) throws RemoteException;

    void explicitCallTransfer() throws RemoteException;

    String getCallId() throws RemoteException;

    ImsCallProfile getCallProfile() throws RemoteException;

    String getHeaderCallId() throws RemoteException;

    IImsCallSession getIImsCallSession() throws RemoteException;

    boolean isIncomingCallMultiparty() throws RemoteException;

    void notifyDeviceSwitchFailed(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void notifyDeviceSwitched() throws RemoteException;

    void removeLastParticipant() throws RemoteException;

    void resume() throws RemoteException;

    void setIImsCallSession(IImsCallSession iImsCallSession) throws RemoteException;

    void setImsCallMode(int i) throws RemoteException;

    void setListener(IMtkImsCallSessionListener iMtkImsCallSessionListener) throws RemoteException;

    void unattendedCallTransfer(String str, int i) throws RemoteException;

    public static class Default implements IMtkImsCallSession {
        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void close() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public String getCallId() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public ImsCallProfile getCallProfile() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void setListener(IMtkImsCallSessionListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public IImsCallSession getIImsCallSession() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void setIImsCallSession(IImsCallSession iSession) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public boolean isIncomingCallMultiparty() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void explicitCallTransfer() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void unattendedCallTransfer(String number, int type) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void deviceSwitch(String number, String deviceId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void cancelDeviceSwitch() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void notifyDeviceSwitchFailed(ImsReasonInfo reasonInfo) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void notifyDeviceSwitched() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void resume() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void callTerminated() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public String getHeaderCallId() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void removeLastParticipant() throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void setImsCallMode(int mode) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsCallSession
        public void approveEccRedial(boolean isAprroved) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsCallSession {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsCallSession";
        static final int TRANSACTION_approveEccRedial = 19;
        static final int TRANSACTION_callTerminated = 15;
        static final int TRANSACTION_cancelDeviceSwitch = 11;
        static final int TRANSACTION_close = 1;
        static final int TRANSACTION_deviceSwitch = 10;
        static final int TRANSACTION_explicitCallTransfer = 8;
        static final int TRANSACTION_getCallId = 2;
        static final int TRANSACTION_getCallProfile = 3;
        static final int TRANSACTION_getHeaderCallId = 16;
        static final int TRANSACTION_getIImsCallSession = 5;
        static final int TRANSACTION_isIncomingCallMultiparty = 7;
        static final int TRANSACTION_notifyDeviceSwitchFailed = 12;
        static final int TRANSACTION_notifyDeviceSwitched = 13;
        static final int TRANSACTION_removeLastParticipant = 17;
        static final int TRANSACTION_resume = 14;
        static final int TRANSACTION_setIImsCallSession = 6;
        static final int TRANSACTION_setImsCallMode = 18;
        static final int TRANSACTION_setListener = 4;
        static final int TRANSACTION_unattendedCallTransfer = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsCallSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsCallSession)) {
                return new Proxy(obj);
            }
            return (IMtkImsCallSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsReasonInfo _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        close();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getCallId();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ImsCallProfile _result2 = getCallProfile();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setListener(IMtkImsCallSessionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _result3 = getIImsCallSession();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setIImsCallSession(IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIncomingCallMultiparty = isIncomingCallMultiparty();
                        reply.writeNoException();
                        reply.writeInt(isIncomingCallMultiparty ? 1 : 0);
                        return true;
                    case TRANSACTION_explicitCallTransfer /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        explicitCallTransfer();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unattendedCallTransfer /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        unattendedCallTransfer(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_deviceSwitch /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        deviceSwitch(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_cancelDeviceSwitch /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        cancelDeviceSwitch();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_notifyDeviceSwitchFailed /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        notifyDeviceSwitchFailed(_arg0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_notifyDeviceSwitched /*{ENCODED_INT: 13}*/:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDeviceSwitched();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_resume /*{ENCODED_INT: 14}*/:
                        data.enforceInterface(DESCRIPTOR);
                        resume();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_callTerminated /*{ENCODED_INT: 15}*/:
                        data.enforceInterface(DESCRIPTOR);
                        callTerminated();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getHeaderCallId /*{ENCODED_INT: 16}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getHeaderCallId();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case TRANSACTION_removeLastParticipant /*{ENCODED_INT: 17}*/:
                        data.enforceInterface(DESCRIPTOR);
                        removeLastParticipant();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setImsCallMode /*{ENCODED_INT: 18}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setImsCallMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_approveEccRedial /*{ENCODED_INT: 19}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        approveEccRedial(_arg02);
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
        public static class Proxy implements IMtkImsCallSession {
            public static IMtkImsCallSession sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().close();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public String getCallId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCallId();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public ImsCallProfile getCallProfile() throws RemoteException {
                ImsCallProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCallProfile();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void setListener(IMtkImsCallSessionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public IImsCallSession getIImsCallSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIImsCallSession();
                    }
                    _reply.readException();
                    IImsCallSession _result = IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void setIImsCallSession(IImsCallSession iSession) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(iSession != null ? iSession.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIImsCallSession(iSession);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public boolean isIncomingCallMultiparty() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIncomingCallMultiparty();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void explicitCallTransfer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_explicitCallTransfer, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().explicitCallTransfer();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void unattendedCallTransfer(String number, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeInt(type);
                    if (this.mRemote.transact(Stub.TRANSACTION_unattendedCallTransfer, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unattendedCallTransfer(number, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void deviceSwitch(String number, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeString(deviceId);
                    if (this.mRemote.transact(Stub.TRANSACTION_deviceSwitch, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceSwitch(number, deviceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void cancelDeviceSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_cancelDeviceSwitch, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelDeviceSwitch();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void notifyDeviceSwitchFailed(ImsReasonInfo reasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reasonInfo != null) {
                        _data.writeInt(1);
                        reasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyDeviceSwitchFailed, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyDeviceSwitchFailed(reasonInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void notifyDeviceSwitched() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyDeviceSwitched, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyDeviceSwitched();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void resume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_resume, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resume();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void callTerminated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_callTerminated, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callTerminated();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public String getHeaderCallId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getHeaderCallId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeaderCallId();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void removeLastParticipant() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_removeLastParticipant, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeLastParticipant();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void setImsCallMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_setImsCallMode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsCallMode(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsCallSession
            public void approveEccRedial(boolean isAprroved) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isAprroved ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_approveEccRedial, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().approveEccRedial(isAprroved);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsCallSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsCallSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
