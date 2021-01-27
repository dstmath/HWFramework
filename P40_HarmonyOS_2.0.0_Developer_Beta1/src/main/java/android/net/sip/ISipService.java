package android.net.sip;

import android.app.PendingIntent;
import android.net.sip.ISipSession;
import android.net.sip.ISipSessionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISipService extends IInterface {
    void close(String str, String str2) throws RemoteException;

    ISipSession createSession(SipProfile sipProfile, ISipSessionListener iSipSessionListener, String str) throws RemoteException;

    SipProfile[] getListOfProfiles(String str) throws RemoteException;

    ISipSession getPendingSession(String str, String str2) throws RemoteException;

    boolean isOpened(String str, String str2) throws RemoteException;

    boolean isRegistered(String str, String str2) throws RemoteException;

    void open(SipProfile sipProfile, String str) throws RemoteException;

    void open3(SipProfile sipProfile, PendingIntent pendingIntent, ISipSessionListener iSipSessionListener, String str) throws RemoteException;

    void setRegistrationListener(String str, ISipSessionListener iSipSessionListener, String str2) throws RemoteException;

    public static class Default implements ISipService {
        @Override // android.net.sip.ISipService
        public void open(SipProfile localProfile, String opPackageName) throws RemoteException {
        }

        @Override // android.net.sip.ISipService
        public void open3(SipProfile localProfile, PendingIntent incomingCallPendingIntent, ISipSessionListener listener, String opPackageName) throws RemoteException {
        }

        @Override // android.net.sip.ISipService
        public void close(String localProfileUri, String opPackageName) throws RemoteException {
        }

        @Override // android.net.sip.ISipService
        public boolean isOpened(String localProfileUri, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.net.sip.ISipService
        public boolean isRegistered(String localProfileUri, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.net.sip.ISipService
        public void setRegistrationListener(String localProfileUri, ISipSessionListener listener, String opPackageName) throws RemoteException {
        }

        @Override // android.net.sip.ISipService
        public ISipSession createSession(SipProfile localProfile, ISipSessionListener listener, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipService
        public ISipSession getPendingSession(String callId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.net.sip.ISipService
        public SipProfile[] getListOfProfiles(String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISipService {
        private static final String DESCRIPTOR = "android.net.sip.ISipService";
        static final int TRANSACTION_close = 3;
        static final int TRANSACTION_createSession = 7;
        static final int TRANSACTION_getListOfProfiles = 9;
        static final int TRANSACTION_getPendingSession = 8;
        static final int TRANSACTION_isOpened = 4;
        static final int TRANSACTION_isRegistered = 5;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_open3 = 2;
        static final int TRANSACTION_setRegistrationListener = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISipService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISipService)) {
                return new Proxy(obj);
            }
            return (ISipService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SipProfile _arg0;
            SipProfile _arg02;
            PendingIntent _arg1;
            SipProfile _arg03;
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        open(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        open3(_arg02, _arg1, ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        close(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOpened = isOpened(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isOpened ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRegistered = isRegistered(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRegistered ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setRegistrationListener(data.readString(), ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        ISipSession _result = createSession(_arg03, ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ISipSession _result2 = getPendingSession(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        SipProfile[] _result3 = getListOfProfiles(data.readString());
                        reply.writeNoException();
                        reply.writeTypedArray(_result3, 1);
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
        public static class Proxy implements ISipService {
            public static ISipService sDefaultImpl;
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

            @Override // android.net.sip.ISipService
            public void open(SipProfile localProfile, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(1);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().open(localProfile, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public void open3(SipProfile localProfile, PendingIntent incomingCallPendingIntent, ISipSessionListener listener, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(1);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (incomingCallPendingIntent != null) {
                        _data.writeInt(1);
                        incomingCallPendingIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().open3(localProfile, incomingCallPendingIntent, listener, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public void close(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().close(localProfileUri, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public boolean isOpened(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOpened(localProfileUri, opPackageName);
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

            @Override // android.net.sip.ISipService
            public boolean isRegistered(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRegistered(localProfileUri, opPackageName);
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

            @Override // android.net.sip.ISipService
            public void setRegistrationListener(String localProfileUri, ISipSessionListener listener, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRegistrationListener(localProfileUri, listener, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public ISipSession createSession(SipProfile localProfile, ISipSessionListener listener, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(1);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createSession(localProfile, listener, opPackageName);
                    }
                    _reply.readException();
                    ISipSession _result = ISipSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public ISipSession getPendingSession(String callId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPendingSession(callId, opPackageName);
                    }
                    _reply.readException();
                    ISipSession _result = ISipSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.sip.ISipService
            public SipProfile[] getListOfProfiles(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getListOfProfiles(opPackageName);
                    }
                    _reply.readException();
                    SipProfile[] _result = (SipProfile[]) _reply.createTypedArray(SipProfile.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISipService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISipService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
