package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ConnectionRequest;
import android.telecom.Logging.Session.Info;
import android.telecom.PhoneAccountHandle;

public interface IConnectionService extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionService";
        static final int TRANSACTION_abort = 6;
        static final int TRANSACTION_addConnectionServiceAdapter = 1;
        static final int TRANSACTION_answer = 8;
        static final int TRANSACTION_answerVideo = 7;
        static final int TRANSACTION_conference = 18;
        static final int TRANSACTION_createConnection = 3;
        static final int TRANSACTION_createConnectionComplete = 4;
        static final int TRANSACTION_createConnectionFailed = 5;
        static final int TRANSACTION_disconnect = 11;
        static final int TRANSACTION_hold = 13;
        static final int TRANSACTION_mergeConference = 20;
        static final int TRANSACTION_onCallAudioStateChanged = 15;
        static final int TRANSACTION_onExtrasChanged = 25;
        static final int TRANSACTION_onPostDialContinue = 22;
        static final int TRANSACTION_playDtmfTone = 16;
        static final int TRANSACTION_pullExternalCall = 23;
        static final int TRANSACTION_reject = 9;
        static final int TRANSACTION_rejectWithMessage = 10;
        static final int TRANSACTION_removeConnectionServiceAdapter = 2;
        static final int TRANSACTION_respondToRttUpgradeRequest = 30;
        static final int TRANSACTION_sendCallEvent = 24;
        static final int TRANSACTION_setActiveSubscription = 27;
        static final int TRANSACTION_setLocalCallHold = 26;
        static final int TRANSACTION_silence = 12;
        static final int TRANSACTION_splitFromConference = 19;
        static final int TRANSACTION_startRtt = 28;
        static final int TRANSACTION_stopDtmfTone = 17;
        static final int TRANSACTION_stopRtt = 29;
        static final int TRANSACTION_swapConference = 21;
        static final int TRANSACTION_unhold = 14;

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

            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter, Info sessionInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter, Info sessionInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown, Info sessionInfo) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
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
                    if (isIncoming) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!isUnknown) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void createConnectionComplete(String callId, Info sessionInfo) throws RemoteException {
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

            public void createConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, Info sessionInfo) throws RemoteException {
                int i = 1;
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
                    if (!isIncoming) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void abort(String callId, Info sessionInfo) throws RemoteException {
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

            public void answerVideo(String callId, int videoState, Info sessionInfo) throws RemoteException {
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

            public void answer(String callId, Info sessionInfo) throws RemoteException {
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

            public void reject(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectWithMessage(String callId, String message, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnect(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void silence(String callId, Info sessionInfo) throws RemoteException {
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

            public void hold(String callId, Info sessionInfo) throws RemoteException {
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

            public void unhold(String callId, Info sessionInfo) throws RemoteException {
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

            public void onCallAudioStateChanged(String activeCallId, CallAudioState callAudioState, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void playDtmfTone(String callId, char digit, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String conferenceCallId, String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String conferenceCallId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String conferenceCallId, Info sessionInfo) throws RemoteException {
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

            public void onPostDialContinue(String callId, boolean proceed, Info sessionInfo) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!proceed) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void pullExternalCall(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallEvent(String callId, String event, Bundle extras, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onExtrasChanged(String callId, Bundle extras, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(25, _data, null, 1);
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
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setActiveSubscription(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startRtt(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopRtt(String callId, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void respondToRttUpgradeRequest(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Info sessionInfo) throws RemoteException {
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
                    this.mRemote.transact(30, _data, null, 1);
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
            IConnectionServiceAdapter _arg0;
            Info _arg1;
            PhoneAccountHandle _arg02;
            String _arg12;
            ConnectionRequest _arg2;
            boolean _arg3;
            String _arg03;
            Info _arg22;
            Info _arg32;
            ParcelFileDescriptor _arg13;
            ParcelFileDescriptor _arg23;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    addConnectionServiceAdapter(_arg0, _arg1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    removeConnectionServiceAdapter(_arg0, _arg1);
                    return true;
                case 3:
                    Info _arg5;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt() != 0;
                    boolean _arg4 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg5 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    createConnection(_arg02, _arg12, _arg2, _arg3, _arg4, _arg5);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    createConnectionComplete(_arg03, _arg1);
                    return true;
                case 5:
                    Info _arg42;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg42 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg42 = null;
                    }
                    createConnectionFailed(_arg02, _arg12, _arg2, _arg3, _arg42);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    abort(_arg03, _arg1);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    int _arg14 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    answerVideo(_arg03, _arg14, _arg22);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    answer(_arg03, _arg1);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    reject(_arg03, _arg1);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    rejectWithMessage(_arg03, _arg12, _arg22);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    disconnect(_arg03, _arg1);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    silence(_arg03, _arg1);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    hold(_arg03, _arg1);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    unhold(_arg03, _arg1);
                    return true;
                case 15:
                    CallAudioState _arg15;
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg15 = (CallAudioState) CallAudioState.CREATOR.createFromParcel(data);
                    } else {
                        _arg15 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    onCallAudioStateChanged(_arg03, _arg15, _arg22);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    char _arg16 = (char) data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    playDtmfTone(_arg03, _arg16, _arg22);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    stopDtmfTone(_arg03, _arg1);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    conference(_arg03, _arg12, _arg22);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    splitFromConference(_arg03, _arg1);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    mergeConference(_arg03, _arg1);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    swapConference(_arg03, _arg1);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    boolean _arg17 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    onPostDialContinue(_arg03, _arg17, _arg22);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    pullExternalCall(_arg03, _arg1);
                    return true;
                case 24:
                    Bundle _arg24;
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg24 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg32 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    sendCallEvent(_arg03, _arg12, _arg24, _arg32);
                    return true;
                case 25:
                    Bundle _arg18;
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg18 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg18 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    onExtrasChanged(_arg03, _arg18, _arg22);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    setLocalCallHold(data.readString(), data.readInt());
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    setActiveSubscription(data.readString());
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg23 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg32 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    startRtt(_arg03, _arg13, _arg23, _arg32);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    stopRtt(_arg03, _arg1);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg23 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg32 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    respondToRttUpgradeRequest(_arg03, _arg13, _arg23, _arg32);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void abort(String str, Info info) throws RemoteException;

    void addConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter, Info info) throws RemoteException;

    void answer(String str, Info info) throws RemoteException;

    void answerVideo(String str, int i, Info info) throws RemoteException;

    void conference(String str, String str2, Info info) throws RemoteException;

    void createConnection(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, boolean z2, Info info) throws RemoteException;

    void createConnectionComplete(String str, Info info) throws RemoteException;

    void createConnectionFailed(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, Info info) throws RemoteException;

    void disconnect(String str, Info info) throws RemoteException;

    void hold(String str, Info info) throws RemoteException;

    void mergeConference(String str, Info info) throws RemoteException;

    void onCallAudioStateChanged(String str, CallAudioState callAudioState, Info info) throws RemoteException;

    void onExtrasChanged(String str, Bundle bundle, Info info) throws RemoteException;

    void onPostDialContinue(String str, boolean z, Info info) throws RemoteException;

    void playDtmfTone(String str, char c, Info info) throws RemoteException;

    void pullExternalCall(String str, Info info) throws RemoteException;

    void reject(String str, Info info) throws RemoteException;

    void rejectWithMessage(String str, String str2, Info info) throws RemoteException;

    void removeConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter, Info info) throws RemoteException;

    void respondToRttUpgradeRequest(String str, ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, Info info) throws RemoteException;

    void sendCallEvent(String str, String str2, Bundle bundle, Info info) throws RemoteException;

    void setActiveSubscription(String str) throws RemoteException;

    void setLocalCallHold(String str, int i) throws RemoteException;

    void silence(String str, Info info) throws RemoteException;

    void splitFromConference(String str, Info info) throws RemoteException;

    void startRtt(String str, ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, Info info) throws RemoteException;

    void stopDtmfTone(String str, Info info) throws RemoteException;

    void stopRtt(String str, Info info) throws RemoteException;

    void swapConference(String str, Info info) throws RemoteException;

    void unhold(String str, Info info) throws RemoteException;
}
