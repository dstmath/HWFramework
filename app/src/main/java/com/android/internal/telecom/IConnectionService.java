package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ConnectionRequest;
import android.telecom.PhoneAccountHandle;

public interface IConnectionService extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionService";
        static final int TRANSACTION_abort = 4;
        static final int TRANSACTION_addConnectionServiceAdapter = 1;
        static final int TRANSACTION_answer = 6;
        static final int TRANSACTION_answerVideo = 5;
        static final int TRANSACTION_conference = 16;
        static final int TRANSACTION_createConnection = 3;
        static final int TRANSACTION_disconnect = 9;
        static final int TRANSACTION_hold = 11;
        static final int TRANSACTION_mergeConference = 18;
        static final int TRANSACTION_onCallAudioStateChanged = 13;
        static final int TRANSACTION_onExtrasChanged = 23;
        static final int TRANSACTION_onPostDialContinue = 20;
        static final int TRANSACTION_playDtmfTone = 14;
        static final int TRANSACTION_pullExternalCall = 21;
        static final int TRANSACTION_reject = 7;
        static final int TRANSACTION_rejectWithMessage = 8;
        static final int TRANSACTION_removeConnectionServiceAdapter = 2;
        static final int TRANSACTION_sendCallEvent = 22;
        static final int TRANSACTION_setActiveSubscription = 25;
        static final int TRANSACTION_setLocalCallHold = 24;
        static final int TRANSACTION_silence = 10;
        static final int TRANSACTION_splitFromConference = 17;
        static final int TRANSACTION_stopDtmfTone = 15;
        static final int TRANSACTION_swapConference = 19;
        static final int TRANSACTION_unhold = 12;

        private static class Proxy implements IConnectionService {
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

            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addConnectionServiceAdapter, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeConnectionServiceAdapter, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) throws RemoteException {
                int i = Stub.TRANSACTION_addConnectionServiceAdapter;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(Stub.TRANSACTION_addConnectionServiceAdapter);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_addConnectionServiceAdapter);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (isIncoming) {
                        i2 = Stub.TRANSACTION_addConnectionServiceAdapter;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!isUnknown) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_createConnection, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void abort(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_abort, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void answerVideo(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(Stub.TRANSACTION_answerVideo, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void answer(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_answer, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void reject(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_reject, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectWithMessage(String callId, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(message);
                    this.mRemote.transact(Stub.TRANSACTION_rejectWithMessage, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnect(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void silence(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_silence, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void hold(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_hold, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void unhold(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_unhold, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallAudioStateChanged(String activeCallId, CallAudioState callAudioState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activeCallId);
                    if (callAudioState != null) {
                        _data.writeInt(Stub.TRANSACTION_addConnectionServiceAdapter);
                        callAudioState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCallAudioStateChanged, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void playDtmfTone(String callId, char digit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(digit);
                    this.mRemote.transact(Stub.TRANSACTION_playDtmfTone, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_stopDtmfTone, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String conferenceCallId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_conference, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_splitFromConference, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String conferenceCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    this.mRemote.transact(Stub.TRANSACTION_mergeConference, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String conferenceCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    this.mRemote.transact(Stub.TRANSACTION_swapConference, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onPostDialContinue(String callId, boolean proceed) throws RemoteException {
                int i = Stub.TRANSACTION_addConnectionServiceAdapter;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!proceed) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onPostDialContinue, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void pullExternalCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_pullExternalCall, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallEvent(String callId, String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_addConnectionServiceAdapter);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendCallEvent, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onExtrasChanged(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_addConnectionServiceAdapter);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onExtrasChanged, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void setLocalCallHold(String callId, int lchState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(lchState);
                    this.mRemote.transact(Stub.TRANSACTION_setLocalCallHold, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void setActiveSubscription(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setActiveSubscription, _data, null, Stub.TRANSACTION_addConnectionServiceAdapter);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectionService)) {
                return new Proxy(obj);
            }
            return (IConnectionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg1;
            String _arg0;
            switch (code) {
                case TRANSACTION_addConnectionServiceAdapter /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    addConnectionServiceAdapter(com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_removeConnectionServiceAdapter /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeConnectionServiceAdapter(com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_createConnection /*3*/:
                    PhoneAccountHandle phoneAccountHandle;
                    ConnectionRequest connectionRequest;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        connectionRequest = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        connectionRequest = null;
                    }
                    createConnection(phoneAccountHandle, _arg1, connectionRequest, data.readInt() != 0, data.readInt() != 0);
                    return true;
                case TRANSACTION_abort /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    abort(data.readString());
                    return true;
                case TRANSACTION_answerVideo /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    answerVideo(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_answer /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    answer(data.readString());
                    return true;
                case TRANSACTION_reject /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    reject(data.readString());
                    return true;
                case TRANSACTION_rejectWithMessage /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    rejectWithMessage(data.readString(), data.readString());
                    return true;
                case TRANSACTION_disconnect /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(data.readString());
                    return true;
                case TRANSACTION_silence /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    silence(data.readString());
                    return true;
                case TRANSACTION_hold /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    hold(data.readString());
                    return true;
                case TRANSACTION_unhold /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    unhold(data.readString());
                    return true;
                case TRANSACTION_onCallAudioStateChanged /*13*/:
                    CallAudioState callAudioState;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        callAudioState = (CallAudioState) CallAudioState.CREATOR.createFromParcel(data);
                    } else {
                        callAudioState = null;
                    }
                    onCallAudioStateChanged(_arg0, callAudioState);
                    return true;
                case TRANSACTION_playDtmfTone /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    playDtmfTone(data.readString(), (char) data.readInt());
                    return true;
                case TRANSACTION_stopDtmfTone /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopDtmfTone(data.readString());
                    return true;
                case TRANSACTION_conference /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    conference(data.readString(), data.readString());
                    return true;
                case TRANSACTION_splitFromConference /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    splitFromConference(data.readString());
                    return true;
                case TRANSACTION_mergeConference /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    mergeConference(data.readString());
                    return true;
                case TRANSACTION_swapConference /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    swapConference(data.readString());
                    return true;
                case TRANSACTION_onPostDialContinue /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPostDialContinue(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_pullExternalCall /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    pullExternalCall(data.readString());
                    return true;
                case TRANSACTION_sendCallEvent /*22*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    sendCallEvent(_arg0, _arg1, bundle);
                    return true;
                case TRANSACTION_onExtrasChanged /*23*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    onExtrasChanged(_arg0, bundle2);
                    return true;
                case TRANSACTION_setLocalCallHold /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLocalCallHold(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_setActiveSubscription /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    setActiveSubscription(data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void abort(String str) throws RemoteException;

    void addConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter) throws RemoteException;

    void answer(String str) throws RemoteException;

    void answerVideo(String str, int i) throws RemoteException;

    void conference(String str, String str2) throws RemoteException;

    void createConnection(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, boolean z2) throws RemoteException;

    void disconnect(String str) throws RemoteException;

    void hold(String str) throws RemoteException;

    void mergeConference(String str) throws RemoteException;

    void onCallAudioStateChanged(String str, CallAudioState callAudioState) throws RemoteException;

    void onExtrasChanged(String str, Bundle bundle) throws RemoteException;

    void onPostDialContinue(String str, boolean z) throws RemoteException;

    void playDtmfTone(String str, char c) throws RemoteException;

    void pullExternalCall(String str) throws RemoteException;

    void reject(String str) throws RemoteException;

    void rejectWithMessage(String str, String str2) throws RemoteException;

    void removeConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter) throws RemoteException;

    void sendCallEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void setActiveSubscription(String str) throws RemoteException;

    void setLocalCallHold(String str, int i) throws RemoteException;

    void silence(String str) throws RemoteException;

    void splitFromConference(String str) throws RemoteException;

    void stopDtmfTone(String str) throws RemoteException;

    void swapConference(String str) throws RemoteException;

    void unhold(String str) throws RemoteException;
}
