package com.android.ims.internal;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsCallProfile;

public interface IImsService extends IInterface {

    public static abstract class Stub extends Binder implements IImsService {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsService";
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_createCallProfile = 6;
        static final int TRANSACTION_createCallSession = 7;
        static final int TRANSACTION_getConfigInterface = 10;
        static final int TRANSACTION_getEcbmInterface = 13;
        static final int TRANSACTION_getMultiEndpointInterface = 15;
        static final int TRANSACTION_getPendingCallSession = 8;
        static final int TRANSACTION_getUtInterface = 9;
        static final int TRANSACTION_isConnected = 3;
        static final int TRANSACTION_isOpened = 4;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_setRegistrationListener = 5;
        static final int TRANSACTION_setUiTTYMode = 14;
        static final int TRANSACTION_turnOffIms = 12;
        static final int TRANSACTION_turnOnIms = 11;

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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(serviceClass);
                    if (incomingCallIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        incomingCallIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_open, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isConnected, _data, _reply, 0);
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

            public boolean isOpened(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
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

            public void setRegistrationListener(int serviceId, IImsRegistrationListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setRegistrationListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ImsCallProfile imsCallProfile;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    _data.writeInt(serviceType);
                    _data.writeInt(callType);
                    this.mRemote.transact(Stub.TRANSACTION_createCallProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(_reply);
                    } else {
                        imsCallProfile = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return imsCallProfile;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsCallSession createCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_open);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_createCallSession, _data, _reply, 0);
                    _reply.readException();
                    IImsCallSession _result = com.android.ims.internal.IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPendingCallSession, _data, _reply, 0);
                    _reply.readException();
                    IImsCallSession _result = com.android.ims.internal.IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getUtInterface, _data, _reply, 0);
                    _reply.readException();
                    IImsUt _result = com.android.ims.internal.IImsUt.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getConfigInterface, _data, _reply, 0);
                    _reply.readException();
                    IImsConfig _result = com.android.ims.internal.IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_turnOnIms, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_turnOffIms, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getEcbmInterface, _data, _reply, 0);
                    _reply.readException();
                    IImsEcbm _result = com.android.ims.internal.IImsEcbm.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_open);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setUiTTYMode, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getMultiEndpointInterface, _data, _reply, 0);
                    _reply.readException();
                    IImsMultiEndpoint _result = com.android.ims.internal.IImsMultiEndpoint.Stub.asInterface(_reply.readStrongBinder());
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            int _arg1;
            boolean _result;
            IImsCallSession _result2;
            switch (code) {
                case TRANSACTION_open /*1*/:
                    PendingIntent pendingIntent;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    int _result3 = open(_arg0, _arg1, pendingIntent, com.android.ims.internal.IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_close /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    close(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isConnected /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isConnected(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_open : 0);
                    return true;
                case TRANSACTION_isOpened /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOpened(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_open : 0);
                    return true;
                case TRANSACTION_setRegistrationListener /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRegistrationListener(data.readInt(), com.android.ims.internal.IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createCallProfile /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    ImsCallProfile _result4 = createCallProfile(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_open);
                        _result4.writeToParcel(reply, TRANSACTION_open);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_createCallSession /*7*/:
                    ImsCallProfile imsCallProfile;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                    } else {
                        imsCallProfile = null;
                    }
                    _result2 = createCallSession(_arg0, imsCallProfile, com.android.ims.internal.IImsCallSessionListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case TRANSACTION_getPendingCallSession /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPendingCallSession(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case TRANSACTION_getUtInterface /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    IImsUt _result5 = getUtInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case TRANSACTION_getConfigInterface /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    IImsConfig _result6 = getConfigInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result6 != null ? _result6.asBinder() : null);
                    return true;
                case TRANSACTION_turnOnIms /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    turnOnIms(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_turnOffIms /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    turnOffIms(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getEcbmInterface /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    IImsEcbm _result7 = getEcbmInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result7 != null ? _result7.asBinder() : null);
                    return true;
                case TRANSACTION_setUiTTYMode /*14*/:
                    Message message;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        message = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        message = null;
                    }
                    setUiTTYMode(_arg0, _arg1, message);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMultiEndpointInterface /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    IImsMultiEndpoint _result8 = getMultiEndpointInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result8 != null ? _result8.asBinder() : null);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
