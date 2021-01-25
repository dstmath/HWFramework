package android.net.sip;

import android.net.sip.ISipSessionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISipSession extends IInterface {
    void answerCall(String str, int i) throws RemoteException;

    void changeCall(String str, int i) throws RemoteException;

    void endCall() throws RemoteException;

    String getCallId() throws RemoteException;

    String getLocalIp() throws RemoteException;

    SipProfile getLocalProfile() throws RemoteException;

    SipProfile getPeerProfile() throws RemoteException;

    int getState() throws RemoteException;

    boolean isInCall() throws RemoteException;

    void makeCall(SipProfile sipProfile, String str, int i) throws RemoteException;

    void register(int i) throws RemoteException;

    void setListener(ISipSessionListener iSipSessionListener) throws RemoteException;

    void unregister() throws RemoteException;

    public static class Default implements ISipSession {
        @Override // android.net.sip.ISipSession
        public String getLocalIp() throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipSession
        public SipProfile getLocalProfile() throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipSession
        public SipProfile getPeerProfile() throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipSession
        public int getState() throws RemoteException {
            return 0;
        }

        @Override // android.net.sip.ISipSession
        public boolean isInCall() throws RemoteException {
            return false;
        }

        @Override // android.net.sip.ISipSession
        public String getCallId() throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipSession
        public void setListener(ISipSessionListener listener) throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void register(int duration) throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void unregister() throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void makeCall(SipProfile callee, String sessionDescription, int timeout) throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void answerCall(String sessionDescription, int timeout) throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void endCall() throws RemoteException {
        }

        @Override // android.net.sip.ISipSession
        public void changeCall(String sessionDescription, int timeout) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISipSession {
        private static final String DESCRIPTOR = "android.net.sip.ISipSession";
        static final int TRANSACTION_answerCall = 11;
        static final int TRANSACTION_changeCall = 13;
        static final int TRANSACTION_endCall = 12;
        static final int TRANSACTION_getCallId = 6;
        static final int TRANSACTION_getLocalIp = 1;
        static final int TRANSACTION_getLocalProfile = 2;
        static final int TRANSACTION_getPeerProfile = 3;
        static final int TRANSACTION_getState = 4;
        static final int TRANSACTION_isInCall = 5;
        static final int TRANSACTION_makeCall = 10;
        static final int TRANSACTION_register = 8;
        static final int TRANSACTION_setListener = 7;
        static final int TRANSACTION_unregister = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISipSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISipSession)) {
                return new Proxy(obj);
            }
            return (ISipSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SipProfile _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getLocalIp();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        SipProfile _result2 = getLocalProfile();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        SipProfile _result3 = getPeerProfile();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getState();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInCall = isInCall();
                        reply.writeNoException();
                        reply.writeInt(isInCall ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getCallId();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setListener(ISipSessionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        register(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        unregister();
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        makeCall(_arg0, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_answerCall /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        answerCall(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_endCall /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        endCall();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_changeCall /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        changeCall(data.readString(), data.readInt());
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
        public static class Proxy implements ISipSession {
            public static ISipSession sDefaultImpl;
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

            @Override // android.net.sip.ISipSession
            public String getLocalIp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalIp();
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

            @Override // android.net.sip.ISipSession
            public SipProfile getLocalProfile() throws RemoteException {
                SipProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalProfile();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SipProfile.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.sip.ISipSession
            public SipProfile getPeerProfile() throws RemoteException {
                SipProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeerProfile();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SipProfile.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.sip.ISipSession
            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getState();
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

            @Override // android.net.sip.ISipSession
            public boolean isInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInCall();
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

            @Override // android.net.sip.ISipSession
            public String getCallId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.net.sip.ISipSession
            public void setListener(ISipSessionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // android.net.sip.ISipSession
            public void register(int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(duration);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().register(duration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipSession
            public void unregister() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregister();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipSession
            public void makeCall(SipProfile callee, String sessionDescription, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callee != null) {
                        _data.writeInt(1);
                        callee.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(sessionDescription);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().makeCall(callee, sessionDescription, timeout);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipSession
            public void answerCall(String sessionDescription, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionDescription);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(Stub.TRANSACTION_answerCall, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().answerCall(sessionDescription, timeout);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipSession
            public void endCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_endCall, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endCall();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipSession
            public void changeCall(String sessionDescription, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionDescription);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(Stub.TRANSACTION_changeCall, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().changeCall(sessionDescription, timeout);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISipSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISipSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
