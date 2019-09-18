package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ConnectionRequest;
import android.telecom.Logging.Session;
import android.telecom.PhoneAccountHandle;
import com.android.internal.telecom.IConnectionServiceAdapter;

public interface IConnectionService extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionService";
        static final int TRANSACTION_abort = 6;
        static final int TRANSACTION_addConnectionServiceAdapter = 1;
        static final int TRANSACTION_answer = 8;
        static final int TRANSACTION_answerVideo = 7;
        static final int TRANSACTION_conference = 19;
        static final int TRANSACTION_connectionServiceFocusGained = 33;
        static final int TRANSACTION_connectionServiceFocusLost = 32;
        static final int TRANSACTION_createConnection = 3;
        static final int TRANSACTION_createConnectionComplete = 4;
        static final int TRANSACTION_createConnectionFailed = 5;
        static final int TRANSACTION_deflect = 9;
        static final int TRANSACTION_disconnect = 12;
        static final int TRANSACTION_handoverComplete = 35;
        static final int TRANSACTION_handoverFailed = 34;
        static final int TRANSACTION_hold = 14;
        static final int TRANSACTION_mergeConference = 21;
        static final int TRANSACTION_onCallAudioStateChanged = 16;
        static final int TRANSACTION_onExtrasChanged = 26;
        static final int TRANSACTION_onPostDialContinue = 23;
        static final int TRANSACTION_playDtmfTone = 17;
        static final int TRANSACTION_pullExternalCall = 24;
        static final int TRANSACTION_reject = 10;
        static final int TRANSACTION_rejectWithMessage = 11;
        static final int TRANSACTION_removeConnectionServiceAdapter = 2;
        static final int TRANSACTION_respondToRttUpgradeRequest = 31;
        static final int TRANSACTION_sendCallEvent = 25;
        static final int TRANSACTION_setActiveSubscription = 28;
        static final int TRANSACTION_setLocalCallHold = 27;
        static final int TRANSACTION_silence = 13;
        static final int TRANSACTION_splitFromConference = 20;
        static final int TRANSACTION_startRtt = 29;
        static final int TRANSACTION_stopDtmfTone = 18;
        static final int TRANSACTION_stopRtt = 30;
        static final int TRANSACTION_swapConference = 22;
        static final int TRANSACTION_unhold = 15;

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

            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(adapter != null ? adapter.asBinder() : null);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(adapter != null ? adapter.asBinder() : null);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(1);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isIncoming);
                    _data.writeInt(isUnknown);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createConnectionComplete(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(1);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isIncoming);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void abort(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void answerVideo(String callId, int videoState, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void answer(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void deflect(String callId, Uri address, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (address != null) {
                        _data.writeInt(1);
                        address.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void reject(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectWithMessage(String callId, String message, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(message);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnect(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void silence(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hold(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unhold(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallAudioStateChanged(String activeCallId, CallAudioState callAudioState, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activeCallId);
                    if (callAudioState != null) {
                        _data.writeInt(1);
                        callAudioState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void playDtmfTone(String callId, char digit, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(digit);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String conferenceCallId, String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String conferenceCallId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String conferenceCallId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPostDialContinue(String callId, boolean proceed, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(proceed);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void pullExternalCall(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallEvent(String callId, String event, Bundle extras, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onExtrasChanged(String callId, Bundle extras, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, null, 1);
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
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setActiveSubscription(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startRtt(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (fromInCall != null) {
                        _data.writeInt(1);
                        fromInCall.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (toInCall != null) {
                        _data.writeInt(1);
                        toInCall.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopRtt(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void respondToRttUpgradeRequest(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (fromInCall != null) {
                        _data.writeInt(1);
                        fromInCall.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (toInCall != null) {
                        _data.writeInt(1);
                        toInCall.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void connectionServiceFocusLost(Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void connectionServiceFocusGained(Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handoverFailed(String callId, ConnectionRequest request, int error, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(error);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handoverComplete(String callId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v27, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v13, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v32, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v20, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v42, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v27, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v47, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v34, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v51, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v37, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v55, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v40, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v59, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v43, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v71, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v54, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v76, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v61, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v80, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v64, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v84, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v67, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v89, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v74, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v107, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v89, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v96, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v52, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v99, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v123, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v106, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PhoneAccountHandle _arg0;
            ConnectionRequest _arg2;
            Session.Info _arg5;
            PhoneAccountHandle _arg02;
            ConnectionRequest _arg22;
            Session.Info _arg4;
            Uri _arg1;
            CallAudioState _arg12;
            Bundle _arg23;
            Bundle _arg13;
            ParcelFileDescriptor _arg14;
            ParcelFileDescriptor _arg24;
            ParcelFileDescriptor _arg15;
            ParcelFileDescriptor _arg25;
            ConnectionRequest _arg16;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                boolean _arg17 = false;
                Session.Info _arg18 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        IConnectionServiceAdapter _arg03 = IConnectionServiceAdapter.Stub.asInterface(parcel.readStrongBinder());
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        addConnectionServiceAdapter(_arg03, _arg18);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        IConnectionServiceAdapter _arg04 = IConnectionServiceAdapter.Stub.asInterface(parcel.readStrongBinder());
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        removeConnectionServiceAdapter(_arg04, _arg18);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        String _arg19 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        boolean _arg3 = parcel.readInt() != 0;
                        boolean _arg42 = parcel.readInt() != 0;
                        if (parcel.readInt() != 0) {
                            _arg5 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg5 = null;
                        }
                        createConnection(_arg0, _arg19, _arg2, _arg3, _arg42, _arg5);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        createConnectionComplete(_arg05, _arg18);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg02 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        String _arg110 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg22 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        boolean _arg32 = parcel.readInt() != 0;
                        if (parcel.readInt() != 0) {
                            _arg4 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        createConnectionFailed(_arg02, _arg110, _arg22, _arg32, _arg4);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        abort(_arg06, _arg18);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = parcel.readString();
                        int _arg111 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        answerVideo(_arg07, _arg111, _arg18);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        answer(_arg08, _arg18);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg1 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        deflect(_arg09, _arg1, _arg18);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        reject(_arg010, _arg18);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = parcel.readString();
                        String _arg112 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        rejectWithMessage(_arg011, _arg112, _arg18);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        disconnect(_arg012, _arg18);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg013 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        silence(_arg013, _arg18);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg014 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        hold(_arg014, _arg18);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg015 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        unhold(_arg015, _arg18);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg016 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg12 = (CallAudioState) CallAudioState.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onCallAudioStateChanged(_arg016, _arg12, _arg18);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg017 = parcel.readString();
                        char _arg113 = (char) parcel.readInt();
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        playDtmfTone(_arg017, _arg113, _arg18);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg018 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        stopDtmfTone(_arg018, _arg18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg019 = parcel.readString();
                        String _arg114 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        conference(_arg019, _arg114, _arg18);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg020 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        splitFromConference(_arg020, _arg18);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg021 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        mergeConference(_arg021, _arg18);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg022 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        swapConference(_arg022, _arg18);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg023 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg17 = true;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onPostDialContinue(_arg023, _arg17, _arg18);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg024 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        pullExternalCall(_arg024, _arg18);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg025 = parcel.readString();
                        String _arg115 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg23 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        sendCallEvent(_arg025, _arg115, _arg23, _arg18);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg026 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onExtrasChanged(_arg026, _arg13, _arg18);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        setLocalCallHold(parcel.readString(), parcel.readInt());
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        setActiveSubscription(parcel.readString());
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg027 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg14 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg14 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg24 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg24 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        startRtt(_arg027, _arg14, _arg24, _arg18);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg028 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        stopRtt(_arg028, _arg18);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg029 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg15 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg15 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg25 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg25 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        respondToRttUpgradeRequest(_arg029, _arg15, _arg25, _arg18);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        connectionServiceFocusLost(_arg18);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        connectionServiceFocusGained(_arg18);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg030 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg16 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg16 = null;
                        }
                        int _arg26 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            _arg18 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        handoverFailed(_arg030, _arg16, _arg26, _arg18);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg031 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        handoverComplete(_arg031, _arg18);
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

    void abort(String str, Session.Info info) throws RemoteException;

    void addConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter, Session.Info info) throws RemoteException;

    void answer(String str, Session.Info info) throws RemoteException;

    void answerVideo(String str, int i, Session.Info info) throws RemoteException;

    void conference(String str, String str2, Session.Info info) throws RemoteException;

    void connectionServiceFocusGained(Session.Info info) throws RemoteException;

    void connectionServiceFocusLost(Session.Info info) throws RemoteException;

    void createConnection(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, boolean z2, Session.Info info) throws RemoteException;

    void createConnectionComplete(String str, Session.Info info) throws RemoteException;

    void createConnectionFailed(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, Session.Info info) throws RemoteException;

    void deflect(String str, Uri uri, Session.Info info) throws RemoteException;

    void disconnect(String str, Session.Info info) throws RemoteException;

    void handoverComplete(String str, Session.Info info) throws RemoteException;

    void handoverFailed(String str, ConnectionRequest connectionRequest, int i, Session.Info info) throws RemoteException;

    void hold(String str, Session.Info info) throws RemoteException;

    void mergeConference(String str, Session.Info info) throws RemoteException;

    void onCallAudioStateChanged(String str, CallAudioState callAudioState, Session.Info info) throws RemoteException;

    void onExtrasChanged(String str, Bundle bundle, Session.Info info) throws RemoteException;

    void onPostDialContinue(String str, boolean z, Session.Info info) throws RemoteException;

    void playDtmfTone(String str, char c, Session.Info info) throws RemoteException;

    void pullExternalCall(String str, Session.Info info) throws RemoteException;

    void reject(String str, Session.Info info) throws RemoteException;

    void rejectWithMessage(String str, String str2, Session.Info info) throws RemoteException;

    void removeConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter, Session.Info info) throws RemoteException;

    void respondToRttUpgradeRequest(String str, ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, Session.Info info) throws RemoteException;

    void sendCallEvent(String str, String str2, Bundle bundle, Session.Info info) throws RemoteException;

    void setActiveSubscription(String str) throws RemoteException;

    void setLocalCallHold(String str, int i) throws RemoteException;

    void silence(String str, Session.Info info) throws RemoteException;

    void splitFromConference(String str, Session.Info info) throws RemoteException;

    void startRtt(String str, ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, Session.Info info) throws RemoteException;

    void stopDtmfTone(String str, Session.Info info) throws RemoteException;

    void stopRtt(String str, Session.Info info) throws RemoteException;

    void swapConference(String str, Session.Info info) throws RemoteException;

    void unhold(String str, Session.Info info) throws RemoteException;
}
