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
        static final int TRANSACTION_setActive = 2;
        static final int TRANSACTION_setAddress = 23;
        static final int TRANSACTION_setAudioRoute = 29;
        static final int TRANSACTION_setCallerDisplayName = 24;
        static final int TRANSACTION_setConferenceMergeFailed = 13;
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(6, _data, null, 1);
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
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setRingbackRequested(String callId, boolean ringing, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(ringing);
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
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void queryRemoteConnectionServices(RemoteServiceCallback callback, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
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
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setIsVoipAudioMode(String callId, boolean isVoip, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(isVoip);
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
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(30, _data, null, 1);
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
                        _data.writeInt(1);
                        pHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(35, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(36, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(37, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v45, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v143, resolved type: android.telecom.Logging.Session$Info} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ConnectionRequest _arg1;
            ParcelableConnection _arg2;
            DisconnectCause _arg12;
            ParcelableConference _arg13;
            StatusHints _arg14;
            Uri _arg15;
            ParcelableConnection _arg16;
            Bundle _arg17;
            Bundle _arg22;
            PhoneAccountHandle _arg18;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                boolean _arg19 = false;
                Session.Info _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        handleCreateConnectionComplete(_arg02, _arg1, _arg2, _arg0);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setActive(_arg03, _arg0);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setRinging(_arg04, _arg0);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setDialing(_arg05, _arg0);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setPulling(_arg06, _arg0);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (DisconnectCause) DisconnectCause.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setDisconnected(_arg07, _arg12, _arg0);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDisconnectedWithSsNotification(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setOnHold(_arg08, _arg0);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setRingbackRequested(_arg09, _arg19, _arg0);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setConnectionCapabilities(_arg010, _arg110, _arg0);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        int _arg111 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setConnectionProperties(_arg011, _arg111, _arg0);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setIsConferenced(_arg012, _arg112, _arg0);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setConferenceMergeFailed(_arg013, _arg0);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = (ParcelableConference) ParcelableConference.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        addConferenceCall(_arg014, _arg13, _arg0);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        removeCall(_arg015, _arg0);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onPostDialWait(_arg016, _arg113, _arg0);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        char _arg114 = (char) data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onPostDialChar(_arg017, _arg114, _arg0);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        RemoteServiceCallback _arg018 = RemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        queryRemoteConnectionServices(_arg018, _arg0);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        IVideoProvider _arg115 = IVideoProvider.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setVideoProvider(_arg019, _arg115, _arg0);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setVideoState(_arg020, _arg116, _arg0);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg021 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setIsVoipAudioMode(_arg021, _arg19, _arg0);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (StatusHints) StatusHints.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setStatusHints(_arg022, _arg14, _arg0);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg023 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg15 = null;
                        }
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setAddress(_arg023, _arg15, _arg23, _arg0);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg024 = data.readString();
                        String _arg117 = data.readString();
                        int _arg24 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setCallerDisplayName(_arg024, _arg117, _arg24, _arg0);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        List<String> _arg118 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setConferenceableConnections(_arg025, _arg118, _arg0);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg16 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        addExistingConnection(_arg026, _arg16, _arg0);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg027 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        putExtras(_arg027, _arg17, _arg0);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg028 = data.readString();
                        List<String> _arg119 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        removeExtras(_arg028, _arg119, _arg0);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg029 = data.readString();
                        int _arg120 = data.readInt();
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        setAudioRoute(_arg029, _arg120, _arg25, _arg0);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg030 = data.readString();
                        String _arg121 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onConnectionEvent(_arg030, _arg121, _arg22, _arg0);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg031 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        }
                        setPhoneAccountHandle(_arg031, _arg0);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg032 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onRttInitiationSuccess(_arg032, _arg0);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg033 = data.readString();
                        int _arg122 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onRttInitiationFailure(_arg033, _arg122, _arg0);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg034 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onRttSessionRemotelyTerminated(_arg034, _arg0);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg035 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onRemoteRttRequest(_arg035, _arg0);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg036 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg18 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Session.Info) Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onPhoneAccountChanged(_arg036, _arg18, _arg0);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Session.Info.CREATOR.createFromParcel(parcel);
                        }
                        onConnectionServiceFocusReleased(_arg0);
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

    void queryRemoteConnectionServices(RemoteServiceCallback remoteServiceCallback, Session.Info info) throws RemoteException;

    void removeCall(String str, Session.Info info) throws RemoteException;

    void removeExtras(String str, List<String> list, Session.Info info) throws RemoteException;

    void setActive(String str, Session.Info info) throws RemoteException;

    void setAddress(String str, Uri uri, int i, Session.Info info) throws RemoteException;

    void setAudioRoute(String str, int i, String str2, Session.Info info) throws RemoteException;

    void setCallerDisplayName(String str, String str2, int i, Session.Info info) throws RemoteException;

    void setConferenceMergeFailed(String str, Session.Info info) throws RemoteException;

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
}
