package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsRegistrationListener;
import com.mediatek.gba.NafSessionKey;
import com.mediatek.ims.internal.IMtkImsCallSession;
import com.mediatek.ims.internal.IMtkImsConfig;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import com.mediatek.ims.internal.IMtkImsUt;
import java.util.Map;

public interface IMtkImsService extends IInterface {
    void UpdateImsState(int i) throws RemoteException;

    void addImsSmsListener(int i, IImsSmsListener iImsSmsListener) throws RemoteException;

    void changeEnabledCapabilities(int i, CapabilityChangeRequest capabilityChangeRequest) throws RemoteException;

    IMtkImsCallSession createMtkCallSession(int i, ImsCallProfile imsCallProfile, IImsCallSessionListener iImsCallSessionListener, IImsCallSession iImsCallSession) throws RemoteException;

    void deregisterIms(int i) throws RemoteException;

    void fallBackAospMTFlow(int i) throws RemoteException;

    IMtkImsConfig getConfigInterfaceEx(int i) throws RemoteException;

    int getCurrentCallCount(int i) throws RemoteException;

    int[] getImsNetworkState(int i) throws RemoteException;

    int getImsRegUriType(int i) throws RemoteException;

    int getImsState(int i) throws RemoteException;

    int getModemMultiImsCount() throws RemoteException;

    IMtkImsUt getMtkUtInterface(int i) throws RemoteException;

    IMtkImsCallSession getPendingMtkCallSession(int i, String str) throws RemoteException;

    void hangupAllCall(int i) throws RemoteException;

    boolean isCameraAvailable() throws RemoteException;

    void registerProprietaryImsListener(int i, IImsRegistrationListener iImsRegistrationListener, IMtkImsRegistrationListener iMtkImsRegistrationListener, boolean z) throws RemoteException;

    NafSessionKey runGbaAuthentication(String str, byte[] bArr, boolean z, int i, int i2) throws RemoteException;

    void sendSms(int i, int i2, int i3, String str, String str2, boolean z, byte[] bArr) throws RemoteException;

    void setCallIndication(int i, String str, String str2, int i2, String str3, boolean z, int i3) throws RemoteException;

    void setMTRedirect(int i, boolean z) throws RemoteException;

    void setSipHeader(int i, Map map, String str) throws RemoteException;

    void updateRadioState(int i, int i2) throws RemoteException;

    public static class Default implements IMtkImsService {
        @Override // com.mediatek.ims.internal.IMtkImsService
        public void setCallIndication(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow, int cause) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public IMtkImsCallSession createMtkCallSession(int phoneId, ImsCallProfile profile, IImsCallSessionListener listener, IImsCallSession aospCallSessionImpl) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public IMtkImsCallSession getPendingMtkCallSession(int phoneId, String callId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public int getImsState(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public int getImsRegUriType(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void hangupAllCall(int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void deregisterIms(int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void updateRadioState(int radioState, int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void UpdateImsState(int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public IMtkImsConfig getConfigInterfaceEx(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public IMtkImsUt getMtkUtInterface(int serviceId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public int getModemMultiImsCount() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public int getCurrentCallCount(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public int[] getImsNetworkState(int capability) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void addImsSmsListener(int phoneId, IImsSmsListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void sendSms(int phoneId, int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void registerProprietaryImsListener(int phoneId, IImsRegistrationListener listener, IMtkImsRegistrationListener mtklistener, boolean notifyOnly) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public boolean isCameraAvailable() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void setMTRedirect(int phoneId, boolean enable) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void fallBackAospMTFlow(int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void setSipHeader(int phoneId, Map extraHeaders, String fromUri) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsService
        public void changeEnabledCapabilities(int phoneId, CapabilityChangeRequest request) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsService {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsService";
        static final int TRANSACTION_UpdateImsState = 9;
        static final int TRANSACTION_addImsSmsListener = 16;
        static final int TRANSACTION_changeEnabledCapabilities = 23;
        static final int TRANSACTION_createMtkCallSession = 2;
        static final int TRANSACTION_deregisterIms = 7;
        static final int TRANSACTION_fallBackAospMTFlow = 21;
        static final int TRANSACTION_getConfigInterfaceEx = 10;
        static final int TRANSACTION_getCurrentCallCount = 14;
        static final int TRANSACTION_getImsNetworkState = 15;
        static final int TRANSACTION_getImsRegUriType = 5;
        static final int TRANSACTION_getImsState = 4;
        static final int TRANSACTION_getModemMultiImsCount = 13;
        static final int TRANSACTION_getMtkUtInterface = 11;
        static final int TRANSACTION_getPendingMtkCallSession = 3;
        static final int TRANSACTION_hangupAllCall = 6;
        static final int TRANSACTION_isCameraAvailable = 19;
        static final int TRANSACTION_registerProprietaryImsListener = 18;
        static final int TRANSACTION_runGbaAuthentication = 12;
        static final int TRANSACTION_sendSms = 17;
        static final int TRANSACTION_setCallIndication = 1;
        static final int TRANSACTION_setMTRedirect = 20;
        static final int TRANSACTION_setSipHeader = 22;
        static final int TRANSACTION_updateRadioState = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsService)) {
                return new Proxy(obj);
            }
            return (IMtkImsService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsCallProfile _arg1;
            CapabilityChangeRequest _arg12;
            if (code != 1598968902) {
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                IBinder iBinder4 = null;
                boolean _arg5 = false;
                boolean _arg13 = false;
                boolean _arg3 = false;
                boolean _arg52 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg14 = data.readString();
                        String _arg2 = data.readString();
                        int _arg32 = data.readInt();
                        String _arg4 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setCallIndication(_arg0, _arg14, _arg2, _arg32, _arg4, _arg5, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        IMtkImsCallSession _result = createMtkCallSession(_arg02, _arg1, IImsCallSessionListener.Stub.asInterface(data.readStrongBinder()), IImsCallSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsCallSession _result2 = getPendingMtkCallSession(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder4 = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder4);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getImsState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getImsRegUriType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        hangupAllCall(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        deregisterIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_updateRadioState /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        updateRadioState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_UpdateImsState /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        UpdateImsState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getConfigInterfaceEx /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsConfig _result5 = getConfigInterfaceEx(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder3 = _result5.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case TRANSACTION_getMtkUtInterface /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IMtkImsUt _result6 = getMtkUtInterface(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            iBinder2 = _result6.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
                        return true;
                    case TRANSACTION_runGbaAuthentication /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        NafSessionKey _result7 = runGbaAuthentication(data.readString(), data.createByteArray(), data.readInt() != 0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getModemMultiImsCount /*{ENCODED_INT: 13}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getModemMultiImsCount();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_getCurrentCallCount /*{ENCODED_INT: 14}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getCurrentCallCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_getImsNetworkState /*{ENCODED_INT: 15}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result10 = getImsNetworkState(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result10);
                        return true;
                    case TRANSACTION_addImsSmsListener /*{ENCODED_INT: 16}*/:
                        data.enforceInterface(DESCRIPTOR);
                        addImsSmsListener(data.readInt(), IImsSmsListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sendSms /*{ENCODED_INT: 17}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg15 = data.readInt();
                        int _arg22 = data.readInt();
                        String _arg33 = data.readString();
                        String _arg42 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = true;
                        }
                        sendSms(_arg03, _arg15, _arg22, _arg33, _arg42, _arg52, data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_registerProprietaryImsListener /*{ENCODED_INT: 18}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        IImsRegistrationListener _arg16 = IImsRegistrationListener.Stub.asInterface(data.readStrongBinder());
                        IMtkImsRegistrationListener _arg23 = IMtkImsRegistrationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        registerProprietaryImsListener(_arg04, _arg16, _arg23, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isCameraAvailable /*{ENCODED_INT: 19}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCameraAvailable = isCameraAvailable();
                        reply.writeNoException();
                        reply.writeInt(isCameraAvailable ? 1 : 0);
                        return true;
                    case TRANSACTION_setMTRedirect /*{ENCODED_INT: 20}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setMTRedirect(_arg05, _arg13);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_fallBackAospMTFlow /*{ENCODED_INT: 21}*/:
                        data.enforceInterface(DESCRIPTOR);
                        fallBackAospMTFlow(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setSipHeader /*{ENCODED_INT: 22}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setSipHeader(data.readInt(), data.readHashMap(getClass().getClassLoader()), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_changeEnabledCapabilities /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = (CapabilityChangeRequest) CapabilityChangeRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        changeEnabledCapabilities(_arg06, _arg12);
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
        public static class Proxy implements IMtkImsService {
            public static IMtkImsService sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void setCallIndication(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow, int cause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callNum);
                        try {
                            _data.writeInt(seqNum);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(toNumber);
                            _data.writeInt(isAllow ? 1 : 0);
                            _data.writeInt(cause);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setCallIndication(phoneId, callId, callNum, seqNum, toNumber, isAllow, cause);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public IMtkImsCallSession createMtkCallSession(int phoneId, ImsCallProfile profile, IImsCallSessionListener listener, IImsCallSession aospCallSessionImpl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (aospCallSessionImpl != null) {
                        iBinder = aospCallSessionImpl.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createMtkCallSession(phoneId, profile, listener, aospCallSessionImpl);
                    }
                    _reply.readException();
                    IMtkImsCallSession _result = IMtkImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public IMtkImsCallSession getPendingMtkCallSession(int phoneId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPendingMtkCallSession(phoneId, callId);
                    }
                    _reply.readException();
                    IMtkImsCallSession _result = IMtkImsCallSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public int getImsState(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsState(phoneId);
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public int getImsRegUriType(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsRegUriType(phoneId);
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void hangupAllCall(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hangupAllCall(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void deregisterIms(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deregisterIms(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void updateRadioState(int radioState, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(radioState);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateRadioState, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateRadioState(radioState, phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void UpdateImsState(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(Stub.TRANSACTION_UpdateImsState, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().UpdateImsState(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public IMtkImsConfig getConfigInterfaceEx(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getConfigInterfaceEx, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfigInterfaceEx(phoneId);
                    }
                    _reply.readException();
                    IMtkImsConfig _result = IMtkImsConfig.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public IMtkImsUt getMtkUtInterface(int serviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMtkUtInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMtkUtInterface(serviceId);
                    }
                    _reply.readException();
                    IMtkImsUt _result = IMtkImsUt.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) throws RemoteException {
                NafSessionKey _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecureProtocolId);
                    _data.writeInt(forceRun ? 1 : 0);
                    _data.writeInt(netId);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_runGbaAuthentication, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun, netId, phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NafSessionKey.CREATOR.createFromParcel(_reply);
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public int getModemMultiImsCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getModemMultiImsCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModemMultiImsCount();
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public int getCurrentCallCount(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCurrentCallCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentCallCount(phoneId);
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public int[] getImsNetworkState(int capability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(capability);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getImsNetworkState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsNetworkState(capability);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void addImsSmsListener(int phoneId, IImsSmsListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_addImsSmsListener, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addImsSmsListener(phoneId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void sendSms(int phoneId, int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(messageRef);
                        try {
                            _data.writeString(format);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(smsc);
                            _data.writeInt(isRetry ? 1 : 0);
                            _data.writeByteArray(pdu);
                            if (this.mRemote.transact(Stub.TRANSACTION_sendSms, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendSms(phoneId, token, messageRef, format, smsc, isRetry, pdu);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void registerProprietaryImsListener(int phoneId, IImsRegistrationListener listener, IMtkImsRegistrationListener mtklistener, boolean notifyOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (mtklistener != null) {
                        iBinder = mtklistener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(notifyOnly ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_registerProprietaryImsListener, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerProprietaryImsListener(phoneId, listener, mtklistener, notifyOnly);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public boolean isCameraAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isCameraAvailable, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCameraAvailable();
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

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void setMTRedirect(int phoneId, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setMTRedirect, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMTRedirect(phoneId, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void fallBackAospMTFlow(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(Stub.TRANSACTION_fallBackAospMTFlow, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fallBackAospMTFlow(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void setSipHeader(int phoneId, Map extraHeaders, String fromUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeMap(extraHeaders);
                    _data.writeString(fromUri);
                    if (this.mRemote.transact(Stub.TRANSACTION_setSipHeader, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSipHeader(phoneId, extraHeaders, fromUri);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsService
            public void changeEnabledCapabilities(int phoneId, CapabilityChangeRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_changeEnabledCapabilities, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().changeEnabledCapabilities(phoneId, request);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
