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

    public static class Default implements IImsMMTelFeature {
        @Override // com.android.ims.internal.IImsMMTelFeature
        public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void endSession(int sessionId) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public boolean isConnected(int callSessionType, int callType) throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public boolean isOpened() throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public int getFeatureStatus() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsUt getUtInterface() throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsConfig getConfigInterface() throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void turnOnIms() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void turnOffIms() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsEcbm getEcbmInterface() throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsMMTelFeature
        public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startSession";
                case 2:
                    return "endSession";
                case 3:
                    return "isConnected";
                case 4:
                    return "isOpened";
                case 5:
                    return "getFeatureStatus";
                case 6:
                    return "addRegistrationListener";
                case 7:
                    return "removeRegistrationListener";
                case 8:
                    return "createCallProfile";
                case 9:
                    return "createCallSession";
                case 10:
                    return "getPendingCallSession";
                case 11:
                    return "getUtInterface";
                case 12:
                    return "getConfigInterface";
                case 13:
                    return "turnOnIms";
                case 14:
                    return "turnOffIms";
                case 15:
                    return "getEcbmInterface";
                case 16:
                    return "setUiTTYMode";
                case 17:
                    return "getMultiEndpointInterface";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PendingIntent _arg0;
            ImsCallProfile _arg1;
            Message _arg12;
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result = startSession(_arg0, IImsRegistrationListener.Stub.asInterface(data.readStrongBinder()));
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
                        boolean isConnected = isConnected(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isConnected ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOpened = isOpened();
                        reply.writeNoException();
                        reply.writeInt(isOpened ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getFeatureStatus();
                        reply.writeNoException();
                        reply.writeInt(_result2);
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
                        ImsCallProfile _result3 = createCallProfile(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        IImsCallSession _result4 = createCallSession(_arg02, _arg1);
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IImsCallSession _result5 = getPendingCallSession(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder = _result5.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IImsUt _result6 = getUtInterface();
                        reply.writeNoException();
                        if (_result6 != null) {
                            iBinder = _result6.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IImsConfig _result7 = getConfigInterface();
                        reply.writeNoException();
                        if (_result7 != null) {
                            iBinder = _result7.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
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
                        IImsEcbm _result8 = getEcbmInterface();
                        reply.writeNoException();
                        if (_result8 != null) {
                            iBinder = _result8.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setUiTTYMode(_arg03, _arg12);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IImsMultiEndpoint _result9 = getMultiEndpointInterface();
                        reply.writeNoException();
                        if (_result9 != null) {
                            iBinder = _result9.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
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
        public static class Proxy implements IImsMMTelFeature {
            public static IImsMMTelFeature sDefaultImpl;
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

            @Override // com.android.ims.internal.IImsMMTelFeature
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
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startSession(incomingCallIntent, listener);
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public void endSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endSession(sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public boolean isConnected(int callSessionType, int callType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callSessionType);
                    _data.writeInt(callType);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConnected(callSessionType, callType);
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public boolean isOpened() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOpened();
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public int getFeatureStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFeatureStatus();
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addRegistrationListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
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
                    Stub.getDefaultImpl().removeRegistrationListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) throws RemoteException {
                ImsCallProfile _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(callSessionType);
                    _data.writeInt(callType);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createCallProfile(sessionId, callSessionType, callType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImsCallProfile.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.IImsMMTelFeature
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createCallSession(sessionId, profile);
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(callId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPendingCallSession(sessionId, callId);
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

            @Override // com.android.ims.internal.IImsMMTelFeature
            public IImsUt getUtInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUtInterface();
                    }
                    _reply.readException();
                    IImsUt _result = IImsUt.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public IImsConfig getConfigInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfigInterface();
                    }
                    _reply.readException();
                    IImsConfig _result = IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public void turnOnIms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnOnIms();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public void turnOffIms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnOffIms();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public IImsEcbm getEcbmInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEcbmInterface();
                    }
                    _reply.readException();
                    IImsEcbm _result = IImsEcbm.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
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
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUiTTYMode(uiTtyMode, onComplete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsMMTelFeature
            public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMultiEndpointInterface();
                    }
                    _reply.readException();
                    IImsMultiEndpoint _result = IImsMultiEndpoint.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsMMTelFeature impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsMMTelFeature getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
