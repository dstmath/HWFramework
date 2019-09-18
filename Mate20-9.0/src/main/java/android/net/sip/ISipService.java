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
                        _data.writeInt(1);
                        localProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRegistrationListener(String localProfileUri, ISipSessionListener listener, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localProfileUri);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return ISipSession.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return ISipSession.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return (SipProfile[]) _reply.createTypedArray(SipProfile.CREATOR);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.net.sip.SipProfile} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.app.PendingIntent} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v14, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v16, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v20 */
        /* JADX WARNING: type inference failed for: r1v21 */
        /* JADX WARNING: type inference failed for: r1v22 */
        /* JADX WARNING: type inference failed for: r1v23 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SipProfile _arg0;
            SipProfile _arg02;
            if (code != 1598968902) {
                ? _arg1 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = SipProfile.CREATOR.createFromParcel(data);
                        }
                        open(_arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        open3(_arg0, _arg1, ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        close(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isOpened(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isRegistered(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setRegistrationListener(data.readString(), ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = SipProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        ISipSession _result3 = createSession(_arg02, ISipSessionListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            _arg1 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(_arg1);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ISipSession _result4 = getPendingSession(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            _arg1 = _result4.asBinder();
                        }
                        reply.writeStrongBinder(_arg1);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        SipProfile[] _result5 = getListOfProfiles(data.readString());
                        reply.writeNoException();
                        reply.writeTypedArray(_result5, 1);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
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
