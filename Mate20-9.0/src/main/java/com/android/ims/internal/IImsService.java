package com.android.ims.internal;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

public interface IImsService extends IInterface {

    public static abstract class Stub extends Binder implements IImsService {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsService";
        static final int TRANSACTION_addRegistrationListener = 6;
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_createCallProfile = 7;
        static final int TRANSACTION_createCallSession = 8;
        static final int TRANSACTION_getConfigInterface = 11;
        static final int TRANSACTION_getEcbmInterface = 14;
        static final int TRANSACTION_getMultiEndpointInterface = 16;
        static final int TRANSACTION_getPendingCallSession = 9;
        static final int TRANSACTION_getUtInterface = 10;
        static final int TRANSACTION_isConnected = 3;
        static final int TRANSACTION_isOpened = 4;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_setRegistrationListener = 5;
        static final int TRANSACTION_setUiTTYMode = 15;
        static final int TRANSACTION_turnOffIms = 13;
        static final int TRANSACTION_turnOnIms = 12;

        private static class Proxy implements IImsService {
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

            public int open(int phoneId, int serviceClass, PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(serviceClass);
                    if (incomingCallIntent != null) {
                        _data.writeInt(1);
                        incomingCallIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConnected(int serviceId, int serviceType, int callType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeInt(serviceType);
                    _data.writeInt(callType);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public boolean isOpened(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
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

            public void setRegistrationListener(int serviceId, IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRegistrationListener(int phoneId, int serviceClass, IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(serviceClass);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) throws RemoteException {
                ImsCallProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeInt(serviceType);
                    _data.writeInt(callType);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsCallSession createCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsCallSession getPendingCallSession(int serviceId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeString(callId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsUt getUtInterface(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return IImsUt.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsConfig getConfigInterface(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void turnOnIms(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void turnOffIms(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsEcbm getEcbmInterface(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return IImsEcbm.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUiTTYMode(int serviceId, int uiTtyMode, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeInt(uiTtyMode);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsMultiEndpoint getMultiEndpointInterface(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return IImsMultiEndpoint.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsService)) {
                return new Proxy(obj);
            }
            return (IImsService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v24, resolved type: android.os.Message} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v12, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v14, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v16, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v18, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v22, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v28, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v31 */
        /* JADX WARNING: type inference failed for: r1v32 */
        /* JADX WARNING: type inference failed for: r1v33 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: type inference failed for: r1v35 */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: type inference failed for: r1v37 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsCallProfile _arg1;
            if (code != 1598968902) {
                ? _arg2 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        int _result = open(_arg0, _arg12, _arg2, IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        close(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isConnected(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isOpened(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setRegistrationListener(data.readInt(), IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        addRegistrationListener(data.readInt(), data.readInt(), IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        ImsCallProfile _result4 = createCallProfile(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        IImsCallSession _result5 = createCallSession(_arg02, _arg1, IImsCallSessionListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result5 != null) {
                            _arg2 = _result5.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _result6 = getPendingCallSession(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            _arg2 = _result6.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IImsUt _result7 = getUtInterface(data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            _arg2 = _result7.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IImsConfig _result8 = getConfigInterface(data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            _arg2 = _result8.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        turnOnIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        turnOffIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IImsEcbm _result9 = getEcbmInterface(data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            _arg2 = _result9.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = Message.CREATOR.createFromParcel(data);
                        }
                        setUiTTYMode(_arg03, _arg13, _arg2);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IImsMultiEndpoint _result10 = getMultiEndpointInterface(data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            _arg2 = _result10.asBinder();
                        }
                        reply.writeStrongBinder(_arg2);
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

    void addRegistrationListener(int i, int i2, IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void close(int i) throws RemoteException;

    ImsCallProfile createCallProfile(int i, int i2, int i3) throws RemoteException;

    IImsCallSession createCallSession(int i, ImsCallProfile imsCallProfile, IImsCallSessionListener iImsCallSessionListener) throws RemoteException;

    IImsConfig getConfigInterface(int i) throws RemoteException;

    IImsEcbm getEcbmInterface(int i) throws RemoteException;

    IImsMultiEndpoint getMultiEndpointInterface(int i) throws RemoteException;

    IImsCallSession getPendingCallSession(int i, String str) throws RemoteException;

    IImsUt getUtInterface(int i) throws RemoteException;

    boolean isConnected(int i, int i2, int i3) throws RemoteException;

    boolean isOpened(int i) throws RemoteException;

    int open(int i, int i2, PendingIntent pendingIntent, IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void setRegistrationListener(int i, IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void setUiTTYMode(int i, int i2, Message message) throws RemoteException;

    void turnOffIms(int i) throws RemoteException;

    void turnOnIms(int i) throws RemoteException;
}
