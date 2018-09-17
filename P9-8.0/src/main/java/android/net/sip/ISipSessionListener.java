package android.net.sip;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISipSessionListener extends IInterface {

    public static abstract class Stub extends Binder implements ISipSessionListener {
        private static final String DESCRIPTOR = "android.net.sip.ISipSessionListener";
        static final int TRANSACTION_onCallBusy = 6;
        static final int TRANSACTION_onCallChangeFailed = 9;
        static final int TRANSACTION_onCallEnded = 5;
        static final int TRANSACTION_onCallEstablished = 4;
        static final int TRANSACTION_onCallTransferring = 7;
        static final int TRANSACTION_onCalling = 1;
        static final int TRANSACTION_onError = 8;
        static final int TRANSACTION_onRegistering = 10;
        static final int TRANSACTION_onRegistrationDone = 11;
        static final int TRANSACTION_onRegistrationFailed = 12;
        static final int TRANSACTION_onRegistrationTimeout = 13;
        static final int TRANSACTION_onRinging = 2;
        static final int TRANSACTION_onRingingBack = 3;

        private static class Proxy implements ISipSessionListener {
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

            public void onCalling(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRinging(ISipSession session, SipProfile caller, String sessionDescription) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (caller != null) {
                        _data.writeInt(1);
                        caller.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(sessionDescription);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRingingBack(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCallEstablished(ISipSession session, String sessionDescription) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(sessionDescription);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCallEnded(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCallBusy(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCallTransferring(ISipSession newSession, String sessionDescription) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newSession != null) {
                        iBinder = newSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(sessionDescription);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onError(ISipSession session, int errorCode, String errorMessage) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(errorCode);
                    _data.writeString(errorMessage);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCallChangeFailed(ISipSession session, int errorCode, String errorMessage) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(errorCode);
                    _data.writeString(errorMessage);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRegistering(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRegistrationDone(ISipSession session, int duration) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(duration);
                    this.mRemote.transact(Stub.TRANSACTION_onRegistrationDone, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRegistrationFailed(ISipSession session, int errorCode, String errorMessage) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(errorCode);
                    _data.writeString(errorMessage);
                    this.mRemote.transact(Stub.TRANSACTION_onRegistrationFailed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRegistrationTimeout(ISipSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_onRegistrationTimeout, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISipSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISipSessionListener)) {
                return new Proxy(obj);
            }
            return (ISipSessionListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onCalling(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    SipProfile _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    ISipSession _arg0 = android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (SipProfile) SipProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onRinging(_arg0, _arg1, data.readString());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onRingingBack(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onCallEstablished(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onCallEnded(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onCallBusy(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onCallTransferring(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onError(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onCallChangeFailed(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onRegistering(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onRegistrationDone /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRegistrationDone(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onRegistrationFailed /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRegistrationFailed(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onRegistrationTimeout /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRegistrationTimeout(android.net.sip.ISipSession.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onCallBusy(ISipSession iSipSession) throws RemoteException;

    void onCallChangeFailed(ISipSession iSipSession, int i, String str) throws RemoteException;

    void onCallEnded(ISipSession iSipSession) throws RemoteException;

    void onCallEstablished(ISipSession iSipSession, String str) throws RemoteException;

    void onCallTransferring(ISipSession iSipSession, String str) throws RemoteException;

    void onCalling(ISipSession iSipSession) throws RemoteException;

    void onError(ISipSession iSipSession, int i, String str) throws RemoteException;

    void onRegistering(ISipSession iSipSession) throws RemoteException;

    void onRegistrationDone(ISipSession iSipSession, int i) throws RemoteException;

    void onRegistrationFailed(ISipSession iSipSession, int i, String str) throws RemoteException;

    void onRegistrationTimeout(ISipSession iSipSession) throws RemoteException;

    void onRinging(ISipSession iSipSession, SipProfile sipProfile, String str) throws RemoteException;

    void onRingingBack(ISipSession iSipSession) throws RemoteException;
}
