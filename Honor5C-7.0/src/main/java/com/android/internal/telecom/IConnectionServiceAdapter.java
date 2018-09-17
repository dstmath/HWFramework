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
import android.telecom.ParcelableConference;
import android.telecom.ParcelableConnection;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import java.util.List;

public interface IConnectionServiceAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionServiceAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionServiceAdapter";
        static final int TRANSACTION_addConferenceCall = 13;
        static final int TRANSACTION_addExistingConnection = 25;
        static final int TRANSACTION_handleCreateConnectionComplete = 1;
        static final int TRANSACTION_onConnectionEvent = 28;
        static final int TRANSACTION_onPostDialChar = 16;
        static final int TRANSACTION_onPostDialWait = 15;
        static final int TRANSACTION_putExtras = 26;
        static final int TRANSACTION_queryRemoteConnectionServices = 17;
        static final int TRANSACTION_removeCall = 14;
        static final int TRANSACTION_removeExtras = 27;
        static final int TRANSACTION_setActive = 2;
        static final int TRANSACTION_setAddress = 22;
        static final int TRANSACTION_setCallerDisplayName = 23;
        static final int TRANSACTION_setConferenceMergeFailed = 12;
        static final int TRANSACTION_setConferenceableConnections = 24;
        static final int TRANSACTION_setConnectionCapabilities = 9;
        static final int TRANSACTION_setConnectionProperties = 10;
        static final int TRANSACTION_setDialing = 4;
        static final int TRANSACTION_setDisconnected = 5;
        static final int TRANSACTION_setDisconnectedWithSsNotification = 6;
        static final int TRANSACTION_setIsConferenced = 11;
        static final int TRANSACTION_setIsVoipAudioMode = 20;
        static final int TRANSACTION_setOnHold = 7;
        static final int TRANSACTION_setPhoneAccountHandle = 29;
        static final int TRANSACTION_setRingbackRequested = 8;
        static final int TRANSACTION_setRinging = 3;
        static final int TRANSACTION_setStatusHints = 21;
        static final int TRANSACTION_setVideoProvider = 18;
        static final int TRANSACTION_setVideoState = 19;

        private static class Proxy implements IConnectionServiceAdapter {
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

            public void handleCreateConnectionComplete(String callId, ConnectionRequest request, ParcelableConnection connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (connection != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        connection.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleCreateConnectionComplete, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setActive(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setActive, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setRinging(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setRinging, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setDialing(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setDialing, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setDisconnected(String callId, DisconnectCause disconnectCause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (disconnectCause != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        disconnectCause.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDisconnected, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(disconnectCause);
                    _data.writeString(disconnectMessage);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    this.mRemote.transact(Stub.TRANSACTION_setDisconnectedWithSsNotification, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setOnHold(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setOnHold, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setRingbackRequested(String callId, boolean ringing) throws RemoteException {
                int i = Stub.TRANSACTION_handleCreateConnectionComplete;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!ringing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRingbackRequested, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setConnectionCapabilities(String callId, int connectionCapabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(connectionCapabilities);
                    this.mRemote.transact(Stub.TRANSACTION_setConnectionCapabilities, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setConnectionProperties(String callId, int connectionProperties) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(connectionProperties);
                    this.mRemote.transact(Stub.TRANSACTION_setConnectionProperties, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setIsConferenced(String callId, String conferenceCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(conferenceCallId);
                    this.mRemote.transact(Stub.TRANSACTION_setIsConferenced, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setConferenceMergeFailed(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_setConferenceMergeFailed, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void addConferenceCall(String callId, ParcelableConference conference) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (conference != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        conference.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addConferenceCall, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void removeCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_removeCall, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void onPostDialWait(String callId, String remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    this.mRemote.transact(Stub.TRANSACTION_onPostDialWait, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void onPostDialChar(String callId, char nextChar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(nextChar);
                    this.mRemote.transact(Stub.TRANSACTION_onPostDialChar, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void queryRemoteConnectionServices(RemoteServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_queryRemoteConnectionServices, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setVideoProvider(String callId, IVideoProvider videoProvider) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (videoProvider != null) {
                        iBinder = videoProvider.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setVideoProvider, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setVideoState(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(Stub.TRANSACTION_setVideoState, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setIsVoipAudioMode(String callId, boolean isVoip) throws RemoteException {
                int i = Stub.TRANSACTION_handleCreateConnectionComplete;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!isVoip) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setIsVoipAudioMode, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatusHints(String callId, StatusHints statusHints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (statusHints != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        statusHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStatusHints, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setAddress(String callId, Uri address, int presentation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (address != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        address.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(presentation);
                    this.mRemote.transact(Stub.TRANSACTION_setAddress, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setCallerDisplayName(String callId, String callerDisplayName, int presentation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(callerDisplayName);
                    _data.writeInt(presentation);
                    this.mRemote.transact(Stub.TRANSACTION_setCallerDisplayName, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setConferenceableConnections(String callId, List<String> conferenceableCallIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(conferenceableCallIds);
                    this.mRemote.transact(Stub.TRANSACTION_setConferenceableConnections, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void addExistingConnection(String callId, ParcelableConnection connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (connection != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        connection.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addExistingConnection, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
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
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_putExtras, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
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
                    this.mRemote.transact(Stub.TRANSACTION_removeExtras, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectionEvent(String callId, String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onConnectionEvent, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }

            public void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (pHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_handleCreateConnectionComplete);
                        pHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPhoneAccountHandle, _data, null, Stub.TRANSACTION_handleCreateConnectionComplete);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            switch (code) {
                case TRANSACTION_handleCreateConnectionComplete /*1*/:
                    ConnectionRequest connectionRequest;
                    ParcelableConnection parcelableConnection;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        connectionRequest = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        connectionRequest = null;
                    }
                    if (data.readInt() != 0) {
                        parcelableConnection = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(data);
                    } else {
                        parcelableConnection = null;
                    }
                    handleCreateConnectionComplete(_arg0, connectionRequest, parcelableConnection);
                    return true;
                case TRANSACTION_setActive /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    setActive(data.readString());
                    return true;
                case TRANSACTION_setRinging /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRinging(data.readString());
                    return true;
                case TRANSACTION_setDialing /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDialing(data.readString());
                    return true;
                case TRANSACTION_setDisconnected /*5*/:
                    DisconnectCause disconnectCause;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        disconnectCause = (DisconnectCause) DisconnectCause.CREATOR.createFromParcel(data);
                    } else {
                        disconnectCause = null;
                    }
                    setDisconnected(_arg0, disconnectCause);
                    return true;
                case TRANSACTION_setDisconnectedWithSsNotification /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDisconnectedWithSsNotification(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_setOnHold /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOnHold(data.readString());
                    return true;
                case TRANSACTION_setRingbackRequested /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRingbackRequested(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_setConnectionCapabilities /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setConnectionCapabilities(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_setConnectionProperties /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    setConnectionProperties(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_setIsConferenced /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    setIsConferenced(data.readString(), data.readString());
                    return true;
                case TRANSACTION_setConferenceMergeFailed /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setConferenceMergeFailed(data.readString());
                    return true;
                case TRANSACTION_addConferenceCall /*13*/:
                    ParcelableConference parcelableConference;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parcelableConference = (ParcelableConference) ParcelableConference.CREATOR.createFromParcel(data);
                    } else {
                        parcelableConference = null;
                    }
                    addConferenceCall(_arg0, parcelableConference);
                    return true;
                case TRANSACTION_removeCall /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeCall(data.readString());
                    return true;
                case TRANSACTION_onPostDialWait /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPostDialWait(data.readString(), data.readString());
                    return true;
                case TRANSACTION_onPostDialChar /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPostDialChar(data.readString(), (char) data.readInt());
                    return true;
                case TRANSACTION_queryRemoteConnectionServices /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    queryRemoteConnectionServices(com.android.internal.telecom.RemoteServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setVideoProvider /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVideoProvider(data.readString(), com.android.internal.telecom.IVideoProvider.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setVideoState /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVideoState(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_setIsVoipAudioMode /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setIsVoipAudioMode(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_setStatusHints /*21*/:
                    StatusHints statusHints;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        statusHints = (StatusHints) StatusHints.CREATOR.createFromParcel(data);
                    } else {
                        statusHints = null;
                    }
                    setStatusHints(_arg0, statusHints);
                    return true;
                case TRANSACTION_setAddress /*22*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    setAddress(_arg0, uri, data.readInt());
                    return true;
                case TRANSACTION_setCallerDisplayName /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCallerDisplayName(data.readString(), data.readString(), data.readInt());
                    return true;
                case TRANSACTION_setConferenceableConnections /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    setConferenceableConnections(data.readString(), data.createStringArrayList());
                    return true;
                case TRANSACTION_addExistingConnection /*25*/:
                    ParcelableConnection parcelableConnection2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parcelableConnection2 = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(data);
                    } else {
                        parcelableConnection2 = null;
                    }
                    addExistingConnection(_arg0, parcelableConnection2);
                    return true;
                case TRANSACTION_putExtras /*26*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    putExtras(_arg0, bundle);
                    return true;
                case TRANSACTION_removeExtras /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeExtras(data.readString(), data.createStringArrayList());
                    return true;
                case TRANSACTION_onConnectionEvent /*28*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    onConnectionEvent(_arg0, _arg1, bundle2);
                    return true;
                case TRANSACTION_setPhoneAccountHandle /*29*/:
                    PhoneAccountHandle phoneAccountHandle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    setPhoneAccountHandle(_arg0, phoneAccountHandle);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addConferenceCall(String str, ParcelableConference parcelableConference) throws RemoteException;

    void addExistingConnection(String str, ParcelableConnection parcelableConnection) throws RemoteException;

    void handleCreateConnectionComplete(String str, ConnectionRequest connectionRequest, ParcelableConnection parcelableConnection) throws RemoteException;

    void onConnectionEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void onPostDialChar(String str, char c) throws RemoteException;

    void onPostDialWait(String str, String str2) throws RemoteException;

    void putExtras(String str, Bundle bundle) throws RemoteException;

    void queryRemoteConnectionServices(RemoteServiceCallback remoteServiceCallback) throws RemoteException;

    void removeCall(String str) throws RemoteException;

    void removeExtras(String str, List<String> list) throws RemoteException;

    void setActive(String str) throws RemoteException;

    void setAddress(String str, Uri uri, int i) throws RemoteException;

    void setCallerDisplayName(String str, String str2, int i) throws RemoteException;

    void setConferenceMergeFailed(String str) throws RemoteException;

    void setConferenceableConnections(String str, List<String> list) throws RemoteException;

    void setConnectionCapabilities(String str, int i) throws RemoteException;

    void setConnectionProperties(String str, int i) throws RemoteException;

    void setDialing(String str) throws RemoteException;

    void setDisconnected(String str, DisconnectCause disconnectCause) throws RemoteException;

    void setDisconnectedWithSsNotification(String str, int i, String str2, int i2, int i3) throws RemoteException;

    void setIsConferenced(String str, String str2) throws RemoteException;

    void setIsVoipAudioMode(String str, boolean z) throws RemoteException;

    void setOnHold(String str) throws RemoteException;

    void setPhoneAccountHandle(String str, PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void setRingbackRequested(String str, boolean z) throws RemoteException;

    void setRinging(String str) throws RemoteException;

    void setStatusHints(String str, StatusHints statusHints) throws RemoteException;

    void setVideoProvider(String str, IVideoProvider iVideoProvider) throws RemoteException;

    void setVideoState(String str, int i) throws RemoteException;
}
