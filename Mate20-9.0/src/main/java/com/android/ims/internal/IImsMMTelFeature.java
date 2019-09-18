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
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

public interface IImsMMTelFeature extends IInterface {

    public static abstract class Stub extends Binder implements IImsMMTelFeature {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsMMTelFeature";
        static final int TRANSACTION_addRegistrationListener = 6;
        static final int TRANSACTION_createCallProfile = 8;
        static final int TRANSACTION_createCallSession = 9;
        static final int TRANSACTION_endSession = 2;
        static final int TRANSACTION_getConfigInterface = 12;
        static final int TRANSACTION_getEcbmInterface = 15;
        static final int TRANSACTION_getFeatureStatus = 5;
        static final int TRANSACTION_getMultiEndpointInterface = 17;
        static final int TRANSACTION_getPendingCallSession = 10;
        static final int TRANSACTION_getUtInterface = 11;
        static final int TRANSACTION_isConnected = 3;
        static final int TRANSACTION_isOpened = 4;
        static final int TRANSACTION_removeRegistrationListener = 7;
        static final int TRANSACTION_setUiTTYMode = 16;
        static final int TRANSACTION_startSession = 1;
        static final int TRANSACTION_turnOffIms = 14;
        static final int TRANSACTION_turnOnIms = 13;

        private static class Proxy implements IImsMMTelFeature {
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

            public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public void endSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConnected(int callSessionType, int callType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callSessionType);
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

            public boolean isOpened() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public int getFeatureStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) throws RemoteException {
                ImsCallProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(callSessionType);
                    _data.writeInt(callType);
                    this.mRemote.transact(8, _data, _reply, 0);
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

            public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(callId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return IImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsUt getUtInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return IImsUt.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsConfig getConfigInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void turnOnIms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void turnOffIms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsEcbm getEcbmInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return IImsEcbm.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uiTtyMode);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
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

        public static IImsMMTelFeature asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsMMTelFeature)) {
                return new Proxy(obj);
            }
            return (IImsMMTelFeature) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: android.os.Message} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v15, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v17, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v19, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v21, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v23, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v29, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v32 */
        /* JADX WARNING: type inference failed for: r1v33 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: type inference failed for: r1v35 */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: type inference failed for: r1v37 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: type inference failed for: r1v39 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsCallProfile _arg1;
            if (code != 1598968902) {
                ? _arg12 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        int _result = startSession(_arg12, IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        endSession(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isConnected(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isOpened();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getFeatureStatus();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        addRegistrationListener(IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removeRegistrationListener(IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ImsCallProfile _result5 = createCallProfile(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        IImsCallSession _result6 = createCallSession(_arg0, _arg1);
                        reply.writeNoException();
                        if (_result6 != null) {
                            _arg12 = _result6.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _result7 = getPendingCallSession(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result7 != null) {
                            _arg12 = _result7.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IImsUt _result8 = getUtInterface();
                        reply.writeNoException();
                        if (_result8 != null) {
                            _arg12 = _result8.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IImsConfig _result9 = getConfigInterface();
                        reply.writeNoException();
                        if (_result9 != null) {
                            _arg12 = _result9.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        turnOnIms();
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        turnOffIms();
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IImsEcbm _result10 = getEcbmInterface();
                        reply.writeNoException();
                        if (_result10 != null) {
                            _arg12 = _result10.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = Message.CREATOR.createFromParcel(data);
                        }
                        setUiTTYMode(_arg02, _arg12);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IImsMultiEndpoint _result11 = getMultiEndpointInterface();
                        reply.writeNoException();
                        if (_result11 != null) {
                            _arg12 = _result11.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
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

    void addRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    ImsCallProfile createCallProfile(int i, int i2, int i3) throws RemoteException;

    IImsCallSession createCallSession(int i, ImsCallProfile imsCallProfile) throws RemoteException;

    void endSession(int i) throws RemoteException;

    IImsConfig getConfigInterface() throws RemoteException;

    IImsEcbm getEcbmInterface() throws RemoteException;

    int getFeatureStatus() throws RemoteException;

    IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException;

    IImsCallSession getPendingCallSession(int i, String str) throws RemoteException;

    IImsUt getUtInterface() throws RemoteException;

    boolean isConnected(int i, int i2) throws RemoteException;

    boolean isOpened() throws RemoteException;

    void removeRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void setUiTTYMode(int i, Message message) throws RemoteException;

    int startSession(PendingIntent pendingIntent, IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void turnOffIms() throws RemoteException;

    void turnOnIms() throws RemoteException;
}
