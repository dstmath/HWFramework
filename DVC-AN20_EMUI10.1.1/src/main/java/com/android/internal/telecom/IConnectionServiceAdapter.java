package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.ConnectionRequest;
import android.telecom.DisconnectCause;
import android.telecom.Logging.Session;
import android.telecom.ParcelableConference;
import android.telecom.ParcelableConnection;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.List;

public interface IConnectionServiceAdapter extends IInterface {
    void addConferenceCall(String str, ParcelableConference parcelableConference, Session.Info info) throws RemoteException;

    void addExistingConnection(String str, ParcelableConnection parcelableConnection, Session.Info info) throws RemoteException;

    void handleCreateConnectionComplete(String str, ConnectionRequest connectionRequest, ParcelableConnection parcelableConnection, Session.Info info) throws RemoteException;

    void onConnectionEvent(String str, String str2, Bundle bundle, Session.Info info) throws RemoteException;

    void onConnectionServiceFocusReleased(Session.Info info) throws RemoteException;

    void onPhoneAccountChanged(String str, PhoneAccountHandle phoneAccountHandle, Session.Info info) throws RemoteException;

    void onPostDialChar(String str, char c, Session.Info info) throws RemoteException;

    void onPostDialWait(String str, String str2, Session.Info info) throws RemoteException;

    void onRemoteRttRequest(String str, Session.Info info) throws RemoteException;

    void onRttInitiationFailure(String str, int i, Session.Info info) throws RemoteException;

    void onRttInitiationSuccess(String str, Session.Info info) throws RemoteException;

    void onRttSessionRemotelyTerminated(String str, Session.Info info) throws RemoteException;

    void putExtras(String str, Bundle bundle, Session.Info info) throws RemoteException;

    void queryRemoteConnectionServices(RemoteServiceCallback remoteServiceCallback, String str, Session.Info info) throws RemoteException;

    void removeCall(String str, Session.Info info) throws RemoteException;

    void removeExtras(String str, List<String> list, Session.Info info) throws RemoteException;

    void resetConnectionTime(String str, Session.Info info) throws RemoteException;

    void setActive(String str, Session.Info info) throws RemoteException;

    void setAddress(String str, Uri uri, int i, Session.Info info) throws RemoteException;

    void setAudioRoute(String str, int i, String str2, Session.Info info) throws RemoteException;

    void setCallerDisplayName(String str, String str2, int i, Session.Info info) throws RemoteException;

    void setConferenceMergeFailed(String str, Session.Info info) throws RemoteException;

    void setConferenceState(String str, boolean z, Session.Info info) throws RemoteException;

    void setConferenceableConnections(String str, List<String> list, Session.Info info) throws RemoteException;

    void setConnectionCapabilities(String str, int i, Session.Info info) throws RemoteException;

    void setConnectionProperties(String str, int i, Session.Info info) throws RemoteException;

    void setDialing(String str, Session.Info info) throws RemoteException;

    void setDisconnected(String str, DisconnectCause disconnectCause, Session.Info info) throws RemoteException;

    void setDisconnectedWithSsNotification(String str, int i, String str2, int i2, int i3) throws RemoteException;

    void setIsConferenced(String str, String str2, Session.Info info) throws RemoteException;

    void setIsVoipAudioMode(String str, boolean z, Session.Info info) throws RemoteException;

    void setOnHold(String str, Session.Info info) throws RemoteException;

    void setPhoneAccountHandle(String str, PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void setPulling(String str, Session.Info info) throws RemoteException;

    void setRingbackRequested(String str, boolean z, Session.Info info) throws RemoteException;

    void setRinging(String str, Session.Info info) throws RemoteException;

    void setStatusHints(String str, StatusHints statusHints, Session.Info info) throws RemoteException;

    void setVideoProvider(String str, IVideoProvider iVideoProvider, Session.Info info) throws RemoteException;

    void setVideoState(String str, int i, Session.Info info) throws RemoteException;

    public static class Default implements IConnectionServiceAdapter {
        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void handleCreateConnectionComplete(String callId, ConnectionRequest request, ParcelableConnection connection, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setActive(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setRinging(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setDialing(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setPulling(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setDisconnected(String callId, DisconnectCause disconnectCause, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setOnHold(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setRingbackRequested(String callId, boolean ringing, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setConnectionCapabilities(String callId, int connectionCapabilities, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setConnectionProperties(String callId, int connectionProperties, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setIsConferenced(String callId, String conferenceCallId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setConferenceMergeFailed(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void addConferenceCall(String callId, ParcelableConference conference, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void removeCall(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onPostDialWait(String callId, String remaining, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onPostDialChar(String callId, char nextChar, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void queryRemoteConnectionServices(RemoteServiceCallback callback, String callingPackage, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setVideoProvider(String callId, IVideoProvider videoProvider, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setVideoState(String callId, int videoState, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setIsVoipAudioMode(String callId, boolean isVoip, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setStatusHints(String callId, StatusHints statusHints, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setAddress(String callId, Uri address, int presentation, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setCallerDisplayName(String callId, String callerDisplayName, int presentation, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setConferenceableConnections(String callId, List<String> list, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void addExistingConnection(String callId, ParcelableConnection connection, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void putExtras(String callId, Bundle extras, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void removeExtras(String callId, List<String> list, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setAudioRoute(String callId, int audioRoute, String bluetoothAddress, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onConnectionEvent(String callId, String event, Bundle extras, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onRttInitiationSuccess(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onRttInitiationFailure(String callId, int reason, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onRttSessionRemotelyTerminated(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onRemoteRttRequest(String callId, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onPhoneAccountChanged(String callId, PhoneAccountHandle pHandle, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onConnectionServiceFocusReleased(Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void resetConnectionTime(String callIdi, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setConferenceState(String callId, boolean isConference, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnectionServiceAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionServiceAdapter";
        static final int TRANSACTION_addConferenceCall = 14;
        static final int TRANSACTION_addExistingConnection = 26;
        static final int TRANSACTION_handleCreateConnectionComplete = 1;
        static final int TRANSACTION_onConnectionEvent = 30;
        static final int TRANSACTION_onConnectionServiceFocusReleased = 37;
        static final int TRANSACTION_onPhoneAccountChanged = 36;
        static final int TRANSACTION_onPostDialChar = 17;
        static final int TRANSACTION_onPostDialWait = 16;
        static final int TRANSACTION_onRemoteRttRequest = 35;
        static final int TRANSACTION_onRttInitiationFailure = 33;
        static final int TRANSACTION_onRttInitiationSuccess = 32;
        static final int TRANSACTION_onRttSessionRemotelyTerminated = 34;
        static final int TRANSACTION_putExtras = 27;
        static final int TRANSACTION_queryRemoteConnectionServices = 18;
        static final int TRANSACTION_removeCall = 15;
        static final int TRANSACTION_removeExtras = 28;
        static final int TRANSACTION_resetConnectionTime = 38;
        static final int TRANSACTION_setActive = 2;
        static final int TRANSACTION_setAddress = 23;
        static final int TRANSACTION_setAudioRoute = 29;
        static final int TRANSACTION_setCallerDisplayName = 24;
        static final int TRANSACTION_setConferenceMergeFailed = 13;
        static final int TRANSACTION_setConferenceState = 39;
        static final int TRANSACTION_setConferenceableConnections = 25;
        static final int TRANSACTION_setConnectionCapabilities = 10;
        static final int TRANSACTION_setConnectionProperties = 11;
        static final int TRANSACTION_setDialing = 4;
        static final int TRANSACTION_setDisconnected = 6;
        static final int TRANSACTION_setDisconnectedWithSsNotification = 7;
        static final int TRANSACTION_setIsConferenced = 12;
        static final int TRANSACTION_setIsVoipAudioMode = 21;
        static final int TRANSACTION_setOnHold = 8;
        static final int TRANSACTION_setPhoneAccountHandle = 31;
        static final int TRANSACTION_setPulling = 5;
        static final int TRANSACTION_setRingbackRequested = 9;
        static final int TRANSACTION_setRinging = 3;
        static final int TRANSACTION_setStatusHints = 22;
        static final int TRANSACTION_setVideoProvider = 19;
        static final int TRANSACTION_setVideoState = 20;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectionServiceAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectionServiceAdapter)) {
                return new Proxy(obj);
            }
            return (IConnectionServiceAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "handleCreateConnectionComplete";
                case 2:
                    return "setActive";
                case 3:
                    return "setRinging";
                case 4:
                    return "setDialing";
                case 5:
                    return "setPulling";
                case 6:
                    return "setDisconnected";
                case 7:
                    return "setDisconnectedWithSsNotification";
                case 8:
                    return "setOnHold";
                case 9:
                    return "setRingbackRequested";
                case 10:
                    return "setConnectionCapabilities";
                case 11:
                    return "setConnectionProperties";
                case 12:
                    return "setIsConferenced";
                case 13:
                    return "setConferenceMergeFailed";
                case 14:
                    return "addConferenceCall";
                case 15:
                    return "removeCall";
                case 16:
                    return "onPostDialWait";
                case 17:
                    return "onPostDialChar";
                case 18:
                    return "queryRemoteConnectionServices";
                case 19:
                    return "setVideoProvider";
                case 20:
                    return "setVideoState";
                case 21:
                    return "setIsVoipAudioMode";
                case 22:
                    return "setStatusHints";
                case 23:
                    return "setAddress";
                case 24:
                    return "setCallerDisplayName";
                case 25:
                    return "setConferenceableConnections";
                case 26:
                    return "addExistingConnection";
                case 27:
                    return "putExtras";
                case 28:
                    return "removeExtras";
                case 29:
                    return "setAudioRoute";
                case 30:
                    return "onConnectionEvent";
                case 31:
                    return "setPhoneAccountHandle";
                case 32:
                    return "onRttInitiationSuccess";
                case 33:
                    return "onRttInitiationFailure";
                case 34:
                    return "onRttSessionRemotelyTerminated";
                case 35:
                    return "onRemoteRttRequest";
                case 36:
                    return "onPhoneAccountChanged";
                case 37:
                    return "onConnectionServiceFocusReleased";
                case 38:
                    return "resetConnectionTime";
                case 39:
                    return "setConferenceState";
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
            ConnectionRequest _arg1;
            ParcelableConnection _arg2;
            Session.Info _arg3;
            Session.Info _arg12;
            Session.Info _arg13;
            Session.Info _arg14;
            Session.Info _arg15;
            DisconnectCause _arg16;
            Session.Info _arg22;
            Session.Info _arg17;
            Session.Info _arg23;
            Session.Info _arg24;
            Session.Info _arg25;
            Session.Info _arg26;
            Session.Info _arg18;
            ParcelableConference _arg19;
            Session.Info _arg27;
            Session.Info _arg110;
            Session.Info _arg28;
            Session.Info _arg29;
            Session.Info _arg210;
            Session.Info _arg211;
            Session.Info _arg212;
            Session.Info _arg213;
            StatusHints _arg111;
            Session.Info _arg214;
            Uri _arg112;
            Session.Info _arg32;
            Session.Info _arg33;
            Session.Info _arg215;
            ParcelableConnection _arg113;
            Session.Info _arg216;
            Bundle _arg114;
            Session.Info _arg217;
            Session.Info _arg218;
            Session.Info _arg34;
            Bundle _arg219;
            Session.Info _arg35;
            PhoneAccountHandle _arg115;
            Session.Info _arg116;
            Session.Info _arg220;
            Session.Info _arg117;
            Session.Info _arg118;
            PhoneAccountHandle _arg119;
            Session.Info _arg221;
            Session.Info _arg0;
            Session.Info _arg120;
            Session.Info _arg222;
            if (code != 1598968902) {
                boolean _arg121 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ConnectionRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = ParcelableConnection.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        handleCreateConnectionComplete(_arg02, _arg1, _arg2, _arg3);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setActive(_arg03, _arg12);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        setRinging(_arg04, _arg13);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        setDialing(_arg05, _arg14);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        setPulling(_arg06, _arg15);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = DisconnectCause.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        setDisconnected(_arg07, _arg16, _arg22);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setDisconnectedWithSsNotification(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        setOnHold(_arg08, _arg17);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg121 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        setRingbackRequested(_arg09, _arg121, _arg23);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg122 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        setConnectionCapabilities(_arg010, _arg122, _arg24);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        int _arg123 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg25 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        setConnectionProperties(_arg011, _arg123, _arg25);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        String _arg124 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        setIsConferenced(_arg012, _arg124, _arg26);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        setConferenceMergeFailed(_arg013, _arg18);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = ParcelableConference.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg27 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        addConferenceCall(_arg014, _arg19, _arg27);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg110 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        removeCall(_arg015, _arg110);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        String _arg125 = data.readString();
                        if (data.readInt() != 0) {
                            _arg28 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg28 = null;
                        }
                        onPostDialWait(_arg016, _arg125, _arg28);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        char _arg126 = (char) data.readInt();
                        if (data.readInt() != 0) {
                            _arg29 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg29 = null;
                        }
                        onPostDialChar(_arg017, _arg126, _arg29);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        RemoteServiceCallback _arg018 = RemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg127 = data.readString();
                        if (data.readInt() != 0) {
                            _arg210 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg210 = null;
                        }
                        queryRemoteConnectionServices(_arg018, _arg127, _arg210);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        IVideoProvider _arg128 = IVideoProvider.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg211 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg211 = null;
                        }
                        setVideoProvider(_arg019, _arg128, _arg211);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        int _arg129 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg212 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg212 = null;
                        }
                        setVideoState(_arg020, _arg129, _arg212);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg021 = data.readString();
                        if (data.readInt() != 0) {
                            _arg121 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg213 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg213 = null;
                        }
                        setIsVoipAudioMode(_arg021, _arg121, _arg213);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg111 = StatusHints.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg214 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg214 = null;
                        }
                        setStatusHints(_arg022, _arg111, _arg214);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg023 = data.readString();
                        if (data.readInt() != 0) {
                            _arg112 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        int _arg223 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        setAddress(_arg023, _arg112, _arg223, _arg32);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg024 = data.readString();
                        String _arg130 = data.readString();
                        int _arg224 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg33 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        setCallerDisplayName(_arg024, _arg130, _arg224, _arg33);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        List<String> _arg131 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg215 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg215 = null;
                        }
                        setConferenceableConnections(_arg025, _arg131, _arg215);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        if (data.readInt() != 0) {
                            _arg113 = ParcelableConnection.CREATOR.createFromParcel(data);
                        } else {
                            _arg113 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg216 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg216 = null;
                        }
                        addExistingConnection(_arg026, _arg113, _arg216);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg027 = data.readString();
                        if (data.readInt() != 0) {
                            _arg114 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg114 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg217 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg217 = null;
                        }
                        putExtras(_arg027, _arg114, _arg217);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg028 = data.readString();
                        List<String> _arg132 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg218 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg218 = null;
                        }
                        removeExtras(_arg028, _arg132, _arg218);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg029 = data.readString();
                        int _arg133 = data.readInt();
                        String _arg225 = data.readString();
                        if (data.readInt() != 0) {
                            _arg34 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        setAudioRoute(_arg029, _arg133, _arg225, _arg34);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg030 = data.readString();
                        String _arg134 = data.readString();
                        if (data.readInt() != 0) {
                            _arg219 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg219 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg35 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg35 = null;
                        }
                        onConnectionEvent(_arg030, _arg134, _arg219, _arg35);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg031 = data.readString();
                        if (data.readInt() != 0) {
                            _arg115 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg115 = null;
                        }
                        setPhoneAccountHandle(_arg031, _arg115);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg032 = data.readString();
                        if (data.readInt() != 0) {
                            _arg116 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg116 = null;
                        }
                        onRttInitiationSuccess(_arg032, _arg116);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg033 = data.readString();
                        int _arg135 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg220 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg220 = null;
                        }
                        onRttInitiationFailure(_arg033, _arg135, _arg220);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg034 = data.readString();
                        if (data.readInt() != 0) {
                            _arg117 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg117 = null;
                        }
                        onRttSessionRemotelyTerminated(_arg034, _arg117);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg035 = data.readString();
                        if (data.readInt() != 0) {
                            _arg118 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg118 = null;
                        }
                        onRemoteRttRequest(_arg035, _arg118);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg036 = data.readString();
                        if (data.readInt() != 0) {
                            _arg119 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg119 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg221 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg221 = null;
                        }
                        onPhoneAccountChanged(_arg036, _arg119, _arg221);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onConnectionServiceFocusReleased(_arg0);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg037 = data.readString();
                        if (data.readInt() != 0) {
                            _arg120 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg120 = null;
                        }
                        resetConnectionTime(_arg037, _arg120);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg038 = data.readString();
                        if (data.readInt() != 0) {
                            _arg121 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg222 = Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg222 = null;
                        }
                        setConferenceState(_arg038, _arg121, _arg222);
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
        public static class Proxy implements IConnectionServiceAdapter {
            public static IConnectionServiceAdapter sDefaultImpl;
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

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void handleCreateConnectionComplete(String callId, ConnectionRequest request, ParcelableConnection connection, Session.Info sessionInfo) throws RemoteException {
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
                    if (connection != null) {
                        _data.writeInt(1);
                        connection.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleCreateConnectionComplete(callId, request, connection, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setActive(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setActive(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setRinging(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRinging(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setDialing(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDialing(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setPulling(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPulling(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setDisconnected(String callId, DisconnectCause disconnectCause, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (disconnectCause != null) {
                        _data.writeInt(1);
                        disconnectCause.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDisconnected(callId, disconnectCause, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(disconnectCause);
                    _data.writeString(disconnectMessage);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDisconnectedWithSsNotification(callId, disconnectCause, disconnectMessage, type, code);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setOnHold(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setOnHold(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setRingbackRequested(String callId, boolean ringing, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(ringing ? 1 : 0);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRingbackRequested(callId, ringing, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setConnectionCapabilities(String callId, int connectionCapabilities, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(connectionCapabilities);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setConnectionCapabilities(callId, connectionCapabilities, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setConnectionProperties(String callId, int connectionProperties, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(connectionProperties);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setConnectionProperties(callId, connectionProperties, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setIsConferenced(String callId, String conferenceCallId, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(conferenceCallId);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setIsConferenced(callId, conferenceCallId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setConferenceMergeFailed(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setConferenceMergeFailed(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void addConferenceCall(String callId, ParcelableConference conference, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (conference != null) {
                        _data.writeInt(1);
                        conference.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addConferenceCall(callId, conference, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void removeCall(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeCall(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onPostDialWait(String callId, String remaining, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPostDialWait(callId, remaining, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onPostDialChar(String callId, char nextChar, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(nextChar);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPostDialChar(callId, nextChar, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void queryRemoteConnectionServices(RemoteServiceCallback callback, String callingPackage, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().queryRemoteConnectionServices(callback, callingPackage, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setVideoProvider(String callId, IVideoProvider videoProvider, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStrongBinder(videoProvider != null ? videoProvider.asBinder() : null);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setVideoProvider(callId, videoProvider, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setVideoState(String callId, int videoState, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setVideoState(callId, videoState, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setIsVoipAudioMode(String callId, boolean isVoip, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(isVoip ? 1 : 0);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setIsVoipAudioMode(callId, isVoip, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setStatusHints(String callId, StatusHints statusHints, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (statusHints != null) {
                        _data.writeInt(1);
                        statusHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setStatusHints(callId, statusHints, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setAddress(String callId, Uri address, int presentation, Session.Info sessionInfo) throws RemoteException {
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
                    _data.writeInt(presentation);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAddress(callId, address, presentation, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setCallerDisplayName(String callId, String callerDisplayName, int presentation, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(callerDisplayName);
                    _data.writeInt(presentation);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCallerDisplayName(callId, callerDisplayName, presentation, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setConferenceableConnections(String callId, List<String> conferenceableCallIds, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(conferenceableCallIds);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(25, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setConferenceableConnections(callId, conferenceableCallIds, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void addExistingConnection(String callId, ParcelableConnection connection, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (connection != null) {
                        _data.writeInt(1);
                        connection.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(26, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addExistingConnection(callId, connection, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void putExtras(String callId, Bundle extras, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().putExtras(callId, extras, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void removeExtras(String callId, List<String> keys, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(keys);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeExtras(callId, keys, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setAudioRoute(String callId, int audioRoute, String bluetoothAddress, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(audioRoute);
                    _data.writeString(bluetoothAddress);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(29, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAudioRoute(callId, audioRoute, bluetoothAddress, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onConnectionEvent(String callId, String event, Bundle extras, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(30, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectionEvent(callId, event, extras, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (pHandle != null) {
                        _data.writeInt(1);
                        pHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(31, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPhoneAccountHandle(callId, pHandle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onRttInitiationSuccess(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(32, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRttInitiationSuccess(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onRttInitiationFailure(String callId, int reason, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(reason);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(33, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRttInitiationFailure(callId, reason, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onRttSessionRemotelyTerminated(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(34, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRttSessionRemotelyTerminated(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onRemoteRttRequest(String callId, Session.Info sessionInfo) throws RemoteException {
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
                    if (this.mRemote.transact(35, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRemoteRttRequest(callId, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onPhoneAccountChanged(String callId, PhoneAccountHandle pHandle, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (pHandle != null) {
                        _data.writeInt(1);
                        pHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(36, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPhoneAccountChanged(callId, pHandle, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void onConnectionServiceFocusReleased(Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(37, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectionServiceFocusReleased(sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void resetConnectionTime(String callIdi, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callIdi);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(38, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().resetConnectionTime(callIdi, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IConnectionServiceAdapter
            public void setConferenceState(String callId, boolean isConference, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(isConference ? 1 : 0);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(39, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setConferenceState(callId, isConference, sessionInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IConnectionServiceAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConnectionServiceAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
