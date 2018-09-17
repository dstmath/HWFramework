package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;
import java.util.List;

public interface IInCallAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IInCallAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IInCallAdapter";
        static final int TRANSACTION_answerCall = 1;
        static final int TRANSACTION_conference = 12;
        static final int TRANSACTION_disconnectCall = 3;
        static final int TRANSACTION_holdCall = 4;
        static final int TRANSACTION_mergeConference = 14;
        static final int TRANSACTION_mute = 6;
        static final int TRANSACTION_phoneAccountSelected = 11;
        static final int TRANSACTION_playDtmfTone = 8;
        static final int TRANSACTION_postDialContinue = 10;
        static final int TRANSACTION_pullExternalCall = 18;
        static final int TRANSACTION_putExtras = 20;
        static final int TRANSACTION_rejectCall = 2;
        static final int TRANSACTION_removeExtras = 21;
        static final int TRANSACTION_sendCallEvent = 19;
        static final int TRANSACTION_setAudioRoute = 7;
        static final int TRANSACTION_splitFromConference = 13;
        static final int TRANSACTION_stopDtmfTone = 9;
        static final int TRANSACTION_swapConference = 15;
        static final int TRANSACTION_switchToOtherActiveSub = 22;
        static final int TRANSACTION_turnOffProximitySensor = 17;
        static final int TRANSACTION_turnOnProximitySensor = 16;
        static final int TRANSACTION_unholdCall = 5;
        static final int TRANSACTION_updateRcsPreCallInfo = 23;

        private static class Proxy implements IInCallAdapter {
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

            public void answerCall(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(Stub.TRANSACTION_answerCall, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!rejectWithMessage) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(textMessage);
                    this.mRemote.transact(Stub.TRANSACTION_rejectCall, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnectCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_disconnectCall, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void holdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_holdCall, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void unholdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_unholdCall, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void mute(boolean shouldMute) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!shouldMute) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_mute, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void setAudioRoute(int route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(route);
                    this.mRemote.transact(Stub.TRANSACTION_setAudioRoute, _data, null, Stub.TRANSACTION_answerCall);
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
                    this.mRemote.transact(Stub.TRANSACTION_playDtmfTone, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_stopDtmfTone, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void postDialContinue(String callId, boolean proceed) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!proceed) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_postDialContinue, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle, boolean setDefault) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_answerCall);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!setDefault) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_phoneAccountSelected, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String callId, String otherCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(otherCallId);
                    this.mRemote.transact(Stub.TRANSACTION_conference, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_splitFromConference, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_mergeConference, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_swapConference, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void turnOnProximitySensor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_turnOnProximitySensor, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void turnOffProximitySensor(boolean screenOnImmediately) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!screenOnImmediately) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_turnOffProximitySensor, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void pullExternalCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_pullExternalCall, _data, null, Stub.TRANSACTION_answerCall);
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
                        _data.writeInt(Stub.TRANSACTION_answerCall);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendCallEvent, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void putExtras(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_answerCall);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_putExtras, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void removeExtras(String callId, List<String> keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(keys);
                    this.mRemote.transact(Stub.TRANSACTION_removeExtras, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void switchToOtherActiveSub(String sub, boolean retainLch) throws RemoteException {
                int i = Stub.TRANSACTION_answerCall;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sub);
                    if (!retainLch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_switchToOtherActiveSub, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }

            public void updateRcsPreCallInfo(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_answerCall);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateRcsPreCallInfo, _data, null, Stub.TRANSACTION_answerCall);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInCallAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInCallAdapter)) {
                return new Proxy(obj);
            }
            return (IInCallAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            Bundle bundle;
            switch (code) {
                case TRANSACTION_answerCall /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    answerCall(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_rejectCall /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    rejectCall(data.readString(), data.readInt() != 0, data.readString());
                    return true;
                case TRANSACTION_disconnectCall /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnectCall(data.readString());
                    return true;
                case TRANSACTION_holdCall /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    holdCall(data.readString());
                    return true;
                case TRANSACTION_unholdCall /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    unholdCall(data.readString());
                    return true;
                case TRANSACTION_mute /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    mute(data.readInt() != 0);
                    return true;
                case TRANSACTION_setAudioRoute /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAudioRoute(data.readInt());
                    return true;
                case TRANSACTION_playDtmfTone /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    playDtmfTone(data.readString(), (char) data.readInt());
                    return true;
                case TRANSACTION_stopDtmfTone /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopDtmfTone(data.readString());
                    return true;
                case TRANSACTION_postDialContinue /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    postDialContinue(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_phoneAccountSelected /*11*/:
                    PhoneAccountHandle phoneAccountHandle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    phoneAccountSelected(_arg0, phoneAccountHandle, data.readInt() != 0);
                    return true;
                case TRANSACTION_conference /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    conference(data.readString(), data.readString());
                    return true;
                case TRANSACTION_splitFromConference /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    splitFromConference(data.readString());
                    return true;
                case TRANSACTION_mergeConference /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    mergeConference(data.readString());
                    return true;
                case TRANSACTION_swapConference /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    swapConference(data.readString());
                    return true;
                case TRANSACTION_turnOnProximitySensor /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    turnOnProximitySensor();
                    return true;
                case TRANSACTION_turnOffProximitySensor /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    turnOffProximitySensor(data.readInt() != 0);
                    return true;
                case TRANSACTION_pullExternalCall /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    pullExternalCall(data.readString());
                    return true;
                case TRANSACTION_sendCallEvent /*19*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    sendCallEvent(_arg0, _arg1, bundle2);
                    return true;
                case TRANSACTION_putExtras /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    putExtras(_arg0, bundle);
                    return true;
                case TRANSACTION_removeExtras /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeExtras(data.readString(), data.createStringArrayList());
                    return true;
                case TRANSACTION_switchToOtherActiveSub /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    switchToOtherActiveSub(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_updateRcsPreCallInfo /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    updateRcsPreCallInfo(_arg0, bundle);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void answerCall(String str, int i) throws RemoteException;

    void conference(String str, String str2) throws RemoteException;

    void disconnectCall(String str) throws RemoteException;

    void holdCall(String str) throws RemoteException;

    void mergeConference(String str) throws RemoteException;

    void mute(boolean z) throws RemoteException;

    void phoneAccountSelected(String str, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    void playDtmfTone(String str, char c) throws RemoteException;

    void postDialContinue(String str, boolean z) throws RemoteException;

    void pullExternalCall(String str) throws RemoteException;

    void putExtras(String str, Bundle bundle) throws RemoteException;

    void rejectCall(String str, boolean z, String str2) throws RemoteException;

    void removeExtras(String str, List<String> list) throws RemoteException;

    void sendCallEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void setAudioRoute(int i) throws RemoteException;

    void splitFromConference(String str) throws RemoteException;

    void stopDtmfTone(String str) throws RemoteException;

    void swapConference(String str) throws RemoteException;

    void switchToOtherActiveSub(String str, boolean z) throws RemoteException;

    void turnOffProximitySensor(boolean z) throws RemoteException;

    void turnOnProximitySensor() throws RemoteException;

    void unholdCall(String str) throws RemoteException;

    void updateRcsPreCallInfo(String str, Bundle bundle) throws RemoteException;
}
