package android.net.sip;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISipService extends IInterface {

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

        private static class Proxy implements ISipService {
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

            public void open(SipProfile localProfile, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_open, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void open3(SipProfile localProfile, PendingIntent incomingCallPendingIntent, ISipSessionListener listener, String opPackageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (incomingCallPendingIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        incomingCallPendingIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_open3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOpened(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_isOpened, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRegistered(String localProfileUri, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_isRegistered, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRegistrationListener(String localProfileUri, ISipSessionListener listener, String opPackageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_setRegistrationListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISipSession createSession(SipProfile localProfile, ISipSessionListener listener, String opPackageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (localProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, _reply, 0);
                    _reply.readException();
                    ISipSession _result = android.net.sip.ISipSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISipSession getPendingSession(String callId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_getPendingSession, _data, _reply, 0);
                    _reply.readException();
                    ISipSession _result = android.net.sip.ISipSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SipProfile[] getListOfProfiles(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_getListOfProfiles, _data, _reply, 0);
                    _reply.readException();
                    SipProfile[] _result = (SipProfile[]) _reply.createTypedArray(SipProfile.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SipProfile sipProfile;
            boolean _result;
            ISipSession _result2;
            switch (code) {
                case TRANSACTION_open /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        sipProfile = (SipProfile) SipProfile.CREATOR.createFromParcel(data);
                    } else {
                        sipProfile = null;
                    }
                    open(sipProfile, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_open3 /*2*/:
                    PendingIntent pendingIntent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        sipProfile = (SipProfile) SipProfile.CREATOR.createFromParcel(data);
                    } else {
                        sipProfile = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    open3(sipProfile, pendingIntent, android.net.sip.ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_close /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    close(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isOpened /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOpened(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_open : 0);
                    return true;
                case TRANSACTION_isRegistered /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRegistered(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_open : 0);
                    return true;
                case TRANSACTION_setRegistrationListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRegistrationListener(data.readString(), android.net.sip.ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createSession /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        sipProfile = (SipProfile) SipProfile.CREATOR.createFromParcel(data);
                    } else {
                        sipProfile = null;
                    }
                    _result2 = createSession(sipProfile, android.net.sip.ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case TRANSACTION_getPendingSession /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPendingSession(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case TRANSACTION_getListOfProfiles /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    SipProfile[] _result3 = getListOfProfiles(data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result3, TRANSACTION_open);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void close(String str, String str2) throws RemoteException;

    ISipSession createSession(SipProfile sipProfile, ISipSessionListener iSipSessionListener, String str) throws RemoteException;

    SipProfile[] getListOfProfiles(String str) throws RemoteException;

    ISipSession getPendingSession(String str, String str2) throws RemoteException;

    boolean isOpened(String str, String str2) throws RemoteException;

    boolean isRegistered(String str, String str2) throws RemoteException;

    void open(SipProfile sipProfile, String str) throws RemoteException;

    void open3(SipProfile sipProfile, PendingIntent pendingIntent, ISipSessionListener iSipSessionListener, String str) throws RemoteException;

    void setRegistrationListener(String str, ISipSessionListener iSipSessionListener, String str2) throws RemoteException;
}
