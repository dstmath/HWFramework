package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;
import android.telephony.ims.ImsCallProfile;
import java.util.List;

public interface IInCallAdapter extends IInterface {
    void answerCall(String str, int i) throws RemoteException;

    void conference(String str, String str2) throws RemoteException;

    void deflectCall(String str, Uri uri) throws RemoteException;

    void disconnectCall(String str) throws RemoteException;

    void handoverTo(String str, PhoneAccountHandle phoneAccountHandle, int i, Bundle bundle) throws RemoteException;

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

    void respondToRttRequest(String str, int i, boolean z) throws RemoteException;

    void sendCallEvent(String str, String str2, int i, Bundle bundle) throws RemoteException;

    void sendRttRequest(String str) throws RemoteException;

    void setAudioRoute(int i, String str) throws RemoteException;

    void setBluetoothAudioRoute(String str) throws RemoteException;

    void setRttMode(String str, int i) throws RemoteException;

    void splitFromConference(String str) throws RemoteException;

    void stopDtmfTone(String str) throws RemoteException;

    void stopRtt(String str) throws RemoteException;

    void swapConference(String str) throws RemoteException;

    void switchToOtherActiveSub(String str, boolean z) throws RemoteException;

    void turnOffProximitySensor(boolean z) throws RemoteException;

    void turnOnProximitySensor() throws RemoteException;

    void unholdCall(String str) throws RemoteException;

    void updateRcsPreCallInfo(String str, Bundle bundle) throws RemoteException;

    public static class Default implements IInCallAdapter {
        @Override // com.android.internal.telecom.IInCallAdapter
        public void answerCall(String callId, int videoState) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void deflectCall(String callId, Uri address) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void disconnectCall(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void holdCall(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void unholdCall(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void mute(boolean shouldMute) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void setAudioRoute(int route, String bluetoothAddress) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void setBluetoothAudioRoute(String address) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void playDtmfTone(String callId, char digit) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void stopDtmfTone(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void postDialContinue(String callId, boolean proceed) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle, boolean setDefault) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void conference(String callId, String otherCallId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void splitFromConference(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void mergeConference(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void swapConference(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void turnOnProximitySensor() throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void turnOffProximitySensor(boolean screenOnImmediately) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void pullExternalCall(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void sendCallEvent(String callId, String event, int targetSdkVer, Bundle extras) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void putExtras(String callId, Bundle extras) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void removeExtras(String callId, List<String> list) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void switchToOtherActiveSub(String sub, boolean retainLch) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void updateRcsPreCallInfo(String callId, Bundle extras) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void sendRttRequest(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void respondToRttRequest(String callId, int id, boolean accept) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void stopRtt(String callId) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void setRttMode(String callId, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallAdapter
        public void handoverTo(String callId, PhoneAccountHandle destAcct, int videoState, Bundle extras) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInCallAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IInCallAdapter";
        static final int TRANSACTION_answerCall = 1;
        static final int TRANSACTION_conference = 14;
        static final int TRANSACTION_deflectCall = 2;
        static final int TRANSACTION_disconnectCall = 4;
        static final int TRANSACTION_handoverTo = 30;
        static final int TRANSACTION_holdCall = 5;
        static final int TRANSACTION_mergeConference = 16;
        static final int TRANSACTION_mute = 7;
        static final int TRANSACTION_phoneAccountSelected = 13;
        static final int TRANSACTION_playDtmfTone = 10;
        static final int TRANSACTION_postDialContinue = 12;
        static final int TRANSACTION_pullExternalCall = 20;
        static final int TRANSACTION_putExtras = 22;
        static final int TRANSACTION_rejectCall = 3;
        static final int TRANSACTION_removeExtras = 23;
        static final int TRANSACTION_respondToRttRequest = 27;
        static final int TRANSACTION_sendCallEvent = 21;
        static final int TRANSACTION_sendRttRequest = 26;
        static final int TRANSACTION_setAudioRoute = 8;
        static final int TRANSACTION_setBluetoothAudioRoute = 9;
        static final int TRANSACTION_setRttMode = 29;
        static final int TRANSACTION_splitFromConference = 15;
        static final int TRANSACTION_stopDtmfTone = 11;
        static final int TRANSACTION_stopRtt = 28;
        static final int TRANSACTION_swapConference = 17;
        static final int TRANSACTION_switchToOtherActiveSub = 24;
        static final int TRANSACTION_turnOffProximitySensor = 19;
        static final int TRANSACTION_turnOnProximitySensor = 18;
        static final int TRANSACTION_unholdCall = 6;
        static final int TRANSACTION_updateRcsPreCallInfo = 25;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "answerCall";
                case 2:
                    return "deflectCall";
                case 3:
                    return "rejectCall";
                case 4:
                    return "disconnectCall";
                case 5:
                    return "holdCall";
                case 6:
                    return "unholdCall";
                case 7:
                    return "mute";
                case 8:
                    return "setAudioRoute";
                case 9:
                    return "setBluetoothAudioRoute";
                case 10:
                    return "playDtmfTone";
                case 11:
                    return "stopDtmfTone";
                case 12:
                    return "postDialContinue";
                case 13:
                    return "phoneAccountSelected";
                case 14:
                    return ImsCallProfile.EXTRA_CONFERENCE;
                case 15:
                    return "splitFromConference";
                case 16:
                    return "mergeConference";
                case 17:
                    return "swapConference";
                case 18:
                    return "turnOnProximitySensor";
                case 19:
                    return "turnOffProximitySensor";
                case 20:
                    return "pullExternalCall";
                case 21:
                    return "sendCallEvent";
                case 22:
                    return "putExtras";
                case 23:
                    return "removeExtras";
                case 24:
                    return "switchToOtherActiveSub";
                case 25:
                    return "updateRcsPreCallInfo";
                case 26:
                    return "sendRttRequest";
                case 27:
                    return "respondToRttRequest";
                case 28:
                    return "stopRtt";
                case 29:
                    return "setRttMode";
                case 30:
                    return "handoverTo";
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
            Uri _arg1;
            PhoneAccountHandle _arg12;
            Bundle _arg3;
            Bundle _arg13;
            Bundle _arg14;
            PhoneAccountHandle _arg15;
            Bundle _arg32;
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        answerCall(data.readString(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        deflectCall(_arg0, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        rejectCall(_arg02, _arg2, data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        disconnectCall(data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        holdCall(data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        unholdCall(data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        mute(_arg2);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setAudioRoute(data.readInt(), data.readString());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setBluetoothAudioRoute(data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        playDtmfTone(data.readString(), (char) data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        stopDtmfTone(data.readString());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        postDialContinue(_arg03, _arg2);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        phoneAccountSelected(_arg04, _arg12, _arg2);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        conference(data.readString(), data.readString());
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        splitFromConference(data.readString());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        mergeConference(data.readString());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        swapConference(data.readString());
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        turnOnProximitySensor();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        turnOffProximitySensor(_arg2);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        pullExternalCall(data.readString());
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        String _arg16 = data.readString();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        sendCallEvent(_arg05, _arg16, _arg22, _arg3);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        putExtras(_arg06, _arg13);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        removeExtras(data.readString(), data.createStringArrayList());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        switchToOtherActiveSub(_arg07, _arg2);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        updateRcsPreCallInfo(_arg08, _arg14);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        sendRttRequest(data.readString());
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        respondToRttRequest(_arg09, _arg17, _arg2);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        stopRtt(data.readString());
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        setRttMode(data.readString(), data.readInt());
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        handoverTo(_arg010, _arg15, _arg23, _arg32);
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
        public static class Proxy implements IInCallAdapter {
            public static IInCallAdapter sDefaultImpl;
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

            @Override // com.android.internal.telecom.IInCallAdapter
            public void answerCall(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().answerCall(callId, videoState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void deflectCall(String callId, Uri address) throws RemoteException {
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().deflectCall(callId, address);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(rejectWithMessage ? 1 : 0);
                    _data.writeString(textMessage);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().rejectCall(callId, rejectWithMessage, textMessage);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void disconnectCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().disconnectCall(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void holdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().holdCall(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void unholdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unholdCall(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void mute(boolean shouldMute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(shouldMute ? 1 : 0);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().mute(shouldMute);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void setAudioRoute(int route, String bluetoothAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(route);
                    _data.writeString(bluetoothAddress);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAudioRoute(route, bluetoothAddress);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void setBluetoothAudioRoute(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setBluetoothAudioRoute(address);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void playDtmfTone(String callId, char digit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(digit);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().playDtmfTone(callId, digit);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void stopDtmfTone(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopDtmfTone(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void postDialContinue(String callId, boolean proceed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(proceed ? 1 : 0);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().postDialContinue(callId, proceed);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle, boolean setDefault) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    int i = 0;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (setDefault) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().phoneAccountSelected(callId, accountHandle, setDefault);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void conference(String callId, String otherCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(otherCallId);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().conference(callId, otherCallId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void splitFromConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().splitFromConference(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void mergeConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().mergeConference(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void swapConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().swapConference(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void turnOnProximitySensor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().turnOnProximitySensor();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void turnOffProximitySensor(boolean screenOnImmediately) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(screenOnImmediately ? 1 : 0);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().turnOffProximitySensor(screenOnImmediately);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void pullExternalCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pullExternalCall(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void sendCallEvent(String callId, String event, int targetSdkVer, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    _data.writeInt(targetSdkVer);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendCallEvent(callId, event, targetSdkVer, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void putExtras(String callId, Bundle extras) throws RemoteException {
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
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().putExtras(callId, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void removeExtras(String callId, List<String> keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(keys);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeExtras(callId, keys);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void switchToOtherActiveSub(String sub, boolean retainLch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sub);
                    _data.writeInt(retainLch ? 1 : 0);
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().switchToOtherActiveSub(sub, retainLch);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void updateRcsPreCallInfo(String callId, Bundle extras) throws RemoteException {
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
                    if (this.mRemote.transact(25, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateRcsPreCallInfo(callId, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void sendRttRequest(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(26, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendRttRequest(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void respondToRttRequest(String callId, int id, boolean accept) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(id);
                    _data.writeInt(accept ? 1 : 0);
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().respondToRttRequest(callId, id, accept);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void stopRtt(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopRtt(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void setRttMode(String callId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(29, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRttMode(callId, mode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallAdapter
            public void handoverTo(String callId, PhoneAccountHandle destAcct, int videoState, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (destAcct != null) {
                        _data.writeInt(1);
                        destAcct.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(videoState);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(30, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handoverTo(callId, destAcct, videoState, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInCallAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInCallAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
