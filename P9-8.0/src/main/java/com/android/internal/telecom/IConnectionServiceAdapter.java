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
import android.telecom.Logging.Session.Info;
import android.telecom.ParcelableConference;
import android.telecom.ParcelableConnection;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import java.util.List;

public interface IConnectionServiceAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionServiceAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionServiceAdapter";
        static final int TRANSACTION_addConferenceCall = 14;
        static final int TRANSACTION_addExistingConnection = 26;
        static final int TRANSACTION_handleCreateConnectionComplete = 1;
        static final int TRANSACTION_onConnectionEvent = 30;
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

            public void handleCreateConnectionComplete(String callId, ConnectionRequest request, ParcelableConnection connection, Info sessionInfo) throws RemoteException {
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

            public void setActive(String callId, Info sessionInfo) throws RemoteException {
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

            public void setRinging(String callId, Info sessionInfo) throws RemoteException {
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

            public void setDialing(String callId, Info sessionInfo) throws RemoteException {
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

            public void setPulling(String callId, Info sessionInfo) throws RemoteException {
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

            public void setDisconnected(String callId, DisconnectCause disconnectCause, Info sessionInfo) throws RemoteException {
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

            public void setOnHold(String callId, Info sessionInfo) throws RemoteException {
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

            public void setRingbackRequested(String callId, boolean ringing, Info sessionInfo) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!ringing) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void setConnectionCapabilities(String callId, int connectionCapabilities, Info sessionInfo) throws RemoteException {
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

            public void setConnectionProperties(String callId, int connectionProperties, Info sessionInfo) throws RemoteException {
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

            public void setIsConferenced(String callId, String conferenceCallId, Info sessionInfo) throws RemoteException {
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

            public void setConferenceMergeFailed(String callId, Info sessionInfo) throws RemoteException {
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

            public void addConferenceCall(String callId, ParcelableConference conference, Info sessionInfo) throws RemoteException {
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

            public void removeCall(String callId, Info sessionInfo) throws RemoteException {
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

            public void onPostDialWait(String callId, String remaining, Info sessionInfo) throws RemoteException {
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

            public void onPostDialChar(String callId, char nextChar, Info sessionInfo) throws RemoteException {
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

            public void queryRemoteConnectionServices(RemoteServiceCallback callback, Info sessionInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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

            public void setVideoProvider(String callId, IVideoProvider videoProvider, Info sessionInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (videoProvider != null) {
                        iBinder = videoProvider.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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

            public void setVideoState(String callId, int videoState, Info sessionInfo) throws RemoteException {
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

            public void setIsVoipAudioMode(String callId, boolean isVoip, Info sessionInfo) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!isVoip) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public void setStatusHints(String callId, StatusHints statusHints, Info sessionInfo) throws RemoteException {
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

            public void setAddress(String callId, Uri address, int presentation, Info sessionInfo) throws RemoteException {
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

            public void setCallerDisplayName(String callId, String callerDisplayName, int presentation, Info sessionInfo) throws RemoteException {
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

            public void setConferenceableConnections(String callId, List<String> conferenceableCallIds, Info sessionInfo) throws RemoteException {
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

            public void addExistingConnection(String callId, ParcelableConnection connection, Info sessionInfo) throws RemoteException {
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

            public void putExtras(String callId, Bundle extras, Info sessionInfo) throws RemoteException {
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

            public void removeExtras(String callId, List<String> keys, Info sessionInfo) throws RemoteException {
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

            public void setAudioRoute(String callId, int audioRoute, Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(audioRoute);
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

            public void onConnectionEvent(String callId, String event, Bundle extras, Info sessionInfo) throws RemoteException {
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

            public void onRttInitiationSuccess(String callId, Info sessionInfo) throws RemoteException {
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

            public void onRttInitiationFailure(String callId, int reason, Info sessionInfo) throws RemoteException {
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

            public void onRttSessionRemotelyTerminated(String callId, Info sessionInfo) throws RemoteException {
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

            public void onRemoteRttRequest(String callId, Info sessionInfo) throws RemoteException {
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
            Info _arg3;
            Info _arg1;
            Info _arg2;
            boolean _arg12;
            int _arg13;
            String _arg14;
            int _arg22;
            List<String> _arg15;
            switch (code) {
                case 1:
                    ConnectionRequest _arg16;
                    ParcelableConnection _arg23;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg16 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg16 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg23 = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    handleCreateConnectionComplete(_arg0, _arg16, _arg23, _arg3);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setActive(_arg0, _arg1);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setRinging(_arg0, _arg1);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setDialing(_arg0, _arg1);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setPulling(_arg0, _arg1);
                    return true;
                case 6:
                    DisconnectCause _arg17;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg17 = (DisconnectCause) DisconnectCause.CREATOR.createFromParcel(data);
                    } else {
                        _arg17 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setDisconnected(_arg0, _arg17, _arg2);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    setDisconnectedWithSsNotification(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setOnHold(_arg0, _arg1);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setRingbackRequested(_arg0, _arg12, _arg2);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setConnectionCapabilities(_arg0, _arg13, _arg2);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setConnectionProperties(_arg0, _arg13, _arg2);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg14 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setIsConferenced(_arg0, _arg14, _arg2);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setConferenceMergeFailed(_arg0, _arg1);
                    return true;
                case 14:
                    ParcelableConference _arg18;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg18 = (ParcelableConference) ParcelableConference.CREATOR.createFromParcel(data);
                    } else {
                        _arg18 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    addConferenceCall(_arg0, _arg18, _arg2);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    removeCall(_arg0, _arg1);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg14 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onPostDialWait(_arg0, _arg14, _arg2);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    char _arg19 = (char) data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onPostDialChar(_arg0, _arg19, _arg2);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    RemoteServiceCallback _arg02 = com.android.internal.telecom.RemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    queryRemoteConnectionServices(_arg02, _arg1);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    IVideoProvider _arg110 = com.android.internal.telecom.IVideoProvider.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setVideoProvider(_arg0, _arg110, _arg2);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setVideoState(_arg0, _arg13, _arg2);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg12 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setIsVoipAudioMode(_arg0, _arg12, _arg2);
                    return true;
                case 22:
                    StatusHints _arg111;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg111 = (StatusHints) StatusHints.CREATOR.createFromParcel(data);
                    } else {
                        _arg111 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setStatusHints(_arg0, _arg111, _arg2);
                    return true;
                case 23:
                    Uri _arg112;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg112 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg112 = null;
                    }
                    _arg22 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    setAddress(_arg0, _arg112, _arg22, _arg3);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg14 = data.readString();
                    _arg22 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    setCallerDisplayName(_arg0, _arg14, _arg22, _arg3);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg15 = data.createStringArrayList();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setConferenceableConnections(_arg0, _arg15, _arg2);
                    return true;
                case 26:
                    ParcelableConnection _arg113;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg113 = (ParcelableConnection) ParcelableConnection.CREATOR.createFromParcel(data);
                    } else {
                        _arg113 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    addExistingConnection(_arg0, _arg113, _arg2);
                    return true;
                case 27:
                    Bundle _arg114;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg114 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg114 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    putExtras(_arg0, _arg114, _arg2);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg15 = data.createStringArrayList();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    removeExtras(_arg0, _arg15, _arg2);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setAudioRoute(_arg0, _arg13, _arg2);
                    return true;
                case 30:
                    Bundle _arg24;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg14 = data.readString();
                    if (data.readInt() != 0) {
                        _arg24 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    onConnectionEvent(_arg0, _arg14, _arg24, _arg3);
                    return true;
                case 31:
                    PhoneAccountHandle _arg115;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg115 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg115 = null;
                    }
                    setPhoneAccountHandle(_arg0, _arg115);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onRttInitiationSuccess(_arg0, _arg1);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onRttInitiationFailure(_arg0, _arg13, _arg2);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onRttSessionRemotelyTerminated(_arg0, _arg1);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Info) Info.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onRemoteRttRequest(_arg0, _arg1);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addConferenceCall(String str, ParcelableConference parcelableConference, Info info) throws RemoteException;

    void addExistingConnection(String str, ParcelableConnection parcelableConnection, Info info) throws RemoteException;

    void handleCreateConnectionComplete(String str, ConnectionRequest connectionRequest, ParcelableConnection parcelableConnection, Info info) throws RemoteException;

    void onConnectionEvent(String str, String str2, Bundle bundle, Info info) throws RemoteException;

    void onPostDialChar(String str, char c, Info info) throws RemoteException;

    void onPostDialWait(String str, String str2, Info info) throws RemoteException;

    void onRemoteRttRequest(String str, Info info) throws RemoteException;

    void onRttInitiationFailure(String str, int i, Info info) throws RemoteException;

    void onRttInitiationSuccess(String str, Info info) throws RemoteException;

    void onRttSessionRemotelyTerminated(String str, Info info) throws RemoteException;

    void putExtras(String str, Bundle bundle, Info info) throws RemoteException;

    void queryRemoteConnectionServices(RemoteServiceCallback remoteServiceCallback, Info info) throws RemoteException;

    void removeCall(String str, Info info) throws RemoteException;

    void removeExtras(String str, List<String> list, Info info) throws RemoteException;

    void setActive(String str, Info info) throws RemoteException;

    void setAddress(String str, Uri uri, int i, Info info) throws RemoteException;

    void setAudioRoute(String str, int i, Info info) throws RemoteException;

    void setCallerDisplayName(String str, String str2, int i, Info info) throws RemoteException;

    void setConferenceMergeFailed(String str, Info info) throws RemoteException;

    void setConferenceableConnections(String str, List<String> list, Info info) throws RemoteException;

    void setConnectionCapabilities(String str, int i, Info info) throws RemoteException;

    void setConnectionProperties(String str, int i, Info info) throws RemoteException;

    void setDialing(String str, Info info) throws RemoteException;

    void setDisconnected(String str, DisconnectCause disconnectCause, Info info) throws RemoteException;

    void setDisconnectedWithSsNotification(String str, int i, String str2, int i2, int i3) throws RemoteException;

    void setIsConferenced(String str, String str2, Info info) throws RemoteException;

    void setIsVoipAudioMode(String str, boolean z, Info info) throws RemoteException;

    void setOnHold(String str, Info info) throws RemoteException;

    void setPhoneAccountHandle(String str, PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void setPulling(String str, Info info) throws RemoteException;

    void setRingbackRequested(String str, boolean z, Info info) throws RemoteException;

    void setRinging(String str, Info info) throws RemoteException;

    void setStatusHints(String str, StatusHints statusHints, Info info) throws RemoteException;

    void setVideoProvider(String str, IVideoProvider iVideoProvider, Info info) throws RemoteException;

    void setVideoState(String str, int i, Info info) throws RemoteException;
}
