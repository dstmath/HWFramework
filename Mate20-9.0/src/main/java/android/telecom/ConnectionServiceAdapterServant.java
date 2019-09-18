package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.Logging.Session;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.List;

final class ConnectionServiceAdapterServant {
    private static final int MSG_ADD_CONFERENCE_CALL = 10;
    private static final int MSG_ADD_EXISTING_CONNECTION = 21;
    private static final int MSG_CONNECTION_SERVICE_FOCUS_RELEASED = 35;
    private static final int MSG_HANDLE_CREATE_CONNECTION_COMPLETE = 1;
    private static final int MSG_ON_CONNECTION_EVENT = 26;
    private static final int MSG_ON_POST_DIAL_CHAR = 22;
    private static final int MSG_ON_POST_DIAL_WAIT = 12;
    private static final int MSG_ON_RTT_INITIATION_FAILURE = 31;
    private static final int MSG_ON_RTT_INITIATION_SUCCESS = 30;
    private static final int MSG_ON_RTT_REMOTELY_TERMINATED = 32;
    private static final int MSG_ON_RTT_UPGRADE_REQUEST = 33;
    private static final int MSG_PUT_EXTRAS = 24;
    private static final int MSG_QUERY_REMOTE_CALL_SERVICES = 13;
    private static final int MSG_REMOVE_CALL = 11;
    private static final int MSG_REMOVE_EXTRAS = 25;
    private static final int MSG_SET_ACTIVE = 2;
    private static final int MSG_SET_ADDRESS = 18;
    private static final int MSG_SET_AUDIO_ROUTE = 29;
    private static final int MSG_SET_CALLER_DISPLAY_NAME = 19;
    private static final int MSG_SET_CONFERENCEABLE_CONNECTIONS = 20;
    private static final int MSG_SET_CONFERENCE_MERGE_FAILED = 23;
    private static final int MSG_SET_CONNECTION_CAPABILITIES = 8;
    private static final int MSG_SET_CONNECTION_PROPERTIES = 27;
    private static final int MSG_SET_DIALING = 4;
    private static final int MSG_SET_DISCONNECTED = 5;
    private static final int MSG_SET_DISCONNECTED_WITH_SUPP_NOTIFICATION = 101;
    private static final int MSG_SET_IS_CONFERENCED = 9;
    private static final int MSG_SET_IS_VOIP_AUDIO_MODE = 16;
    private static final int MSG_SET_ON_HOLD = 6;
    private static final int MSG_SET_PHONE_ACCOUNT = 102;
    private static final int MSG_SET_PHONE_ACCOUNT_CHANGED = 34;
    private static final int MSG_SET_PULLING = 28;
    private static final int MSG_SET_RINGBACK_REQUESTED = 7;
    private static final int MSG_SET_RINGING = 3;
    private static final int MSG_SET_STATUS_HINTS = 17;
    private static final int MSG_SET_VIDEO_CALL_PROVIDER = 15;
    private static final int MSG_SET_VIDEO_STATE = 14;
    /* access modifiers changed from: private */
    public final IConnectionServiceAdapter mDelegate;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                internalHandleMessage(msg);
            } catch (RemoteException e) {
            }
        }

        private void internalHandleMessage(Message msg) throws RemoteException {
            int i = msg.what;
            boolean z = false;
            switch (i) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.handleCreateConnectionComplete((String) args.arg1, (ConnectionRequest) args.arg2, (ParcelableConnection) args.arg3, null);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 2:
                    ConnectionServiceAdapterServant.this.mDelegate.setActive((String) msg.obj, null);
                    return;
                case 3:
                    ConnectionServiceAdapterServant.this.mDelegate.setRinging((String) msg.obj, null);
                    return;
                case 4:
                    ConnectionServiceAdapterServant.this.mDelegate.setDialing((String) msg.obj, null);
                    return;
                case 5:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setDisconnected((String) args2.arg1, (DisconnectCause) args2.arg2, null);
                        return;
                    } finally {
                        args2.recycle();
                    }
                case 6:
                    ConnectionServiceAdapterServant.this.mDelegate.setOnHold((String) msg.obj, null);
                    return;
                case 7:
                    IConnectionServiceAdapter access$000 = ConnectionServiceAdapterServant.this.mDelegate;
                    String str = (String) msg.obj;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    access$000.setRingbackRequested(str, z, null);
                    return;
                case 8:
                    ConnectionServiceAdapterServant.this.mDelegate.setConnectionCapabilities((String) msg.obj, msg.arg1, null);
                    return;
                case 9:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setIsConferenced((String) args3.arg1, (String) args3.arg2, null);
                        return;
                    } finally {
                        args3.recycle();
                    }
                case 10:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.addConferenceCall((String) args4.arg1, (ParcelableConference) args4.arg2, null);
                        return;
                    } finally {
                        args4.recycle();
                    }
                case 11:
                    ConnectionServiceAdapterServant.this.mDelegate.removeCall((String) msg.obj, null);
                    return;
                case 12:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.onPostDialWait((String) args5.arg1, (String) args5.arg2, null);
                        return;
                    } finally {
                        args5.recycle();
                    }
                case 13:
                    ConnectionServiceAdapterServant.this.mDelegate.queryRemoteConnectionServices((RemoteServiceCallback) msg.obj, null);
                    return;
                case 14:
                    ConnectionServiceAdapterServant.this.mDelegate.setVideoState((String) msg.obj, msg.arg1, null);
                    return;
                case 15:
                    SomeArgs args6 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setVideoProvider((String) args6.arg1, (IVideoProvider) args6.arg2, null);
                        return;
                    } finally {
                        args6.recycle();
                    }
                case 16:
                    IConnectionServiceAdapter access$0002 = ConnectionServiceAdapterServant.this.mDelegate;
                    String str2 = (String) msg.obj;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    access$0002.setIsVoipAudioMode(str2, z, null);
                    return;
                case 17:
                    SomeArgs args7 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setStatusHints((String) args7.arg1, (StatusHints) args7.arg2, null);
                        return;
                    } finally {
                        args7.recycle();
                    }
                case 18:
                    SomeArgs args8 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setAddress((String) args8.arg1, (Uri) args8.arg2, args8.argi1, null);
                        return;
                    } finally {
                        args8.recycle();
                    }
                case 19:
                    SomeArgs args9 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setCallerDisplayName((String) args9.arg1, (String) args9.arg2, args9.argi1, null);
                        return;
                    } finally {
                        args9.recycle();
                    }
                case 20:
                    SomeArgs args10 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setConferenceableConnections((String) args10.arg1, (List) args10.arg2, null);
                        return;
                    } finally {
                        args10.recycle();
                    }
                case 21:
                    SomeArgs args11 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.addExistingConnection((String) args11.arg1, (ParcelableConnection) args11.arg2, null);
                        return;
                    } finally {
                        args11.recycle();
                    }
                case 22:
                    SomeArgs args12 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.onPostDialChar((String) args12.arg1, (char) args12.argi1, null);
                        return;
                    } finally {
                        args12.recycle();
                    }
                case 23:
                    SomeArgs args13 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setConferenceMergeFailed((String) args13.arg1, null);
                        return;
                    } finally {
                        args13.recycle();
                    }
                case 24:
                    SomeArgs args14 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.putExtras((String) args14.arg1, (Bundle) args14.arg2, null);
                        return;
                    } finally {
                        args14.recycle();
                    }
                case 25:
                    SomeArgs args15 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.removeExtras((String) args15.arg1, (List) args15.arg2, null);
                        return;
                    } finally {
                        args15.recycle();
                    }
                case 26:
                    SomeArgs args16 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.onConnectionEvent((String) args16.arg1, (String) args16.arg2, (Bundle) args16.arg3, null);
                        return;
                    } finally {
                        args16.recycle();
                    }
                case 27:
                    ConnectionServiceAdapterServant.this.mDelegate.setConnectionProperties((String) msg.obj, msg.arg1, null);
                    return;
                case 28:
                    ConnectionServiceAdapterServant.this.mDelegate.setPulling((String) msg.obj, null);
                    return;
                case 29:
                    SomeArgs args17 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setAudioRoute((String) args17.arg1, args17.argi1, (String) args17.arg2, (Session.Info) args17.arg3);
                        return;
                    } finally {
                        args17.recycle();
                    }
                case 30:
                    ConnectionServiceAdapterServant.this.mDelegate.onRttInitiationSuccess((String) msg.obj, null);
                    return;
                case 31:
                    ConnectionServiceAdapterServant.this.mDelegate.onRttInitiationFailure((String) msg.obj, msg.arg1, null);
                    return;
                case 32:
                    ConnectionServiceAdapterServant.this.mDelegate.onRttSessionRemotelyTerminated((String) msg.obj, null);
                    return;
                case 33:
                    ConnectionServiceAdapterServant.this.mDelegate.onRemoteRttRequest((String) msg.obj, null);
                    return;
                case 34:
                    SomeArgs args18 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.onPhoneAccountChanged((String) args18.arg1, (PhoneAccountHandle) args18.arg2, null);
                        return;
                    } finally {
                        args18.recycle();
                    }
                case 35:
                    ConnectionServiceAdapterServant.this.mDelegate.onConnectionServiceFocusReleased(null);
                    return;
                default:
                    switch (i) {
                        case 101:
                            SomeArgs args19 = (SomeArgs) msg.obj;
                            try {
                                ConnectionServiceAdapterServant.this.mDelegate.setDisconnectedWithSsNotification((String) args19.arg1, args19.argi1, (String) args19.arg2, args19.argi2, args19.argi3);
                                return;
                            } finally {
                                args19.recycle();
                            }
                        case 102:
                            SomeArgs args20 = (SomeArgs) msg.obj;
                            try {
                                ConnectionServiceAdapterServant.this.mDelegate.setPhoneAccountHandle((String) args20.arg1, (PhoneAccountHandle) args20.arg2);
                                return;
                            } finally {
                                args20.recycle();
                            }
                        default:
                            return;
                    }
            }
        }
    };
    private final IConnectionServiceAdapter mStub = new IConnectionServiceAdapter.Stub() {
        public void handleCreateConnectionComplete(String id, ConnectionRequest request, ParcelableConnection connection, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = id;
            args.arg2 = request;
            args.arg3 = connection;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        public void setActive(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(2, connectionId).sendToTarget();
        }

        public void setRinging(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(3, connectionId).sendToTarget();
        }

        public void setDialing(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(4, connectionId).sendToTarget();
        }

        public void setPulling(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(28, connectionId).sendToTarget();
        }

        public void setDisconnected(String connectionId, DisconnectCause disconnectCause, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = disconnectCause;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        public void setDisconnectedWithSsNotification(String connectionId, int disconnectCause, String disconnectMessage, int type, int code) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = disconnectMessage;
            args.argi1 = disconnectCause;
            args.argi2 = type;
            args.argi3 = code;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(101, args).sendToTarget();
        }

        public void setOnHold(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(6, connectionId).sendToTarget();
        }

        public void setRingbackRequested(String connectionId, boolean ringback, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(7, ringback, 0, connectionId).sendToTarget();
        }

        public void setConnectionCapabilities(String connectionId, int connectionCapabilities, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(8, connectionCapabilities, 0, connectionId).sendToTarget();
        }

        public void setConnectionProperties(String connectionId, int connectionProperties, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(27, connectionProperties, 0, connectionId).sendToTarget();
        }

        public void setConferenceMergeFailed(String callId, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(23, args).sendToTarget();
        }

        public void setIsConferenced(String callId, String conferenceCallId, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = conferenceCallId;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(9, args).sendToTarget();
        }

        public void addConferenceCall(String callId, ParcelableConference parcelableConference, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = parcelableConference;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(10, args).sendToTarget();
        }

        public void removeCall(String connectionId, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(11, connectionId).sendToTarget();
        }

        public void onPostDialWait(String connectionId, String remainingDigits, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = remainingDigits;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(12, args).sendToTarget();
        }

        public void onPostDialChar(String connectionId, char nextChar, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.argi1 = nextChar;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(22, args).sendToTarget();
        }

        public void queryRemoteConnectionServices(RemoteServiceCallback callback, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(13, callback).sendToTarget();
        }

        public void setVideoState(String connectionId, int videoState, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(14, videoState, 0, connectionId).sendToTarget();
        }

        public void setVideoProvider(String connectionId, IVideoProvider videoProvider, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = videoProvider;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(15, args).sendToTarget();
        }

        public final void setIsVoipAudioMode(String connectionId, boolean isVoip, Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(16, isVoip, 0, connectionId).sendToTarget();
        }

        public final void setStatusHints(String connectionId, StatusHints statusHints, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = statusHints;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(17, args).sendToTarget();
        }

        public final void setAddress(String connectionId, Uri address, int presentation, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = address;
            args.argi1 = presentation;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(18, args).sendToTarget();
        }

        public final void setCallerDisplayName(String connectionId, String callerDisplayName, int presentation, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = callerDisplayName;
            args.argi1 = presentation;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(19, args).sendToTarget();
        }

        public final void setConferenceableConnections(String connectionId, List<String> conferenceableConnectionIds, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = conferenceableConnectionIds;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(20, args).sendToTarget();
        }

        public final void addExistingConnection(String connectionId, ParcelableConnection connection, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = connection;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(21, args).sendToTarget();
        }

        public final void putExtras(String connectionId, Bundle extras, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = extras;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(24, args).sendToTarget();
        }

        public final void removeExtras(String connectionId, List<String> keys, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = keys;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(25, args).sendToTarget();
        }

        public final void setAudioRoute(String connectionId, int audioRoute, String bluetoothAddress, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.argi1 = audioRoute;
            args.arg2 = bluetoothAddress;
            args.arg3 = sessionInfo;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(29, args).sendToTarget();
        }

        public final void onConnectionEvent(String connectionId, String event, Bundle extras, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = event;
            args.arg3 = extras;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(26, args).sendToTarget();
        }

        public final void setPhoneAccountHandle(String connectionId, PhoneAccountHandle pHandle) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = pHandle;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(102, args).sendToTarget();
        }

        public void onRttInitiationSuccess(String connectionId, Session.Info sessionInfo) throws RemoteException {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(30, connectionId).sendToTarget();
        }

        public void onRttInitiationFailure(String connectionId, int reason, Session.Info sessionInfo) throws RemoteException {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(31, reason, 0, connectionId).sendToTarget();
        }

        public void onRttSessionRemotelyTerminated(String connectionId, Session.Info sessionInfo) throws RemoteException {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(32, connectionId).sendToTarget();
        }

        public void onRemoteRttRequest(String connectionId, Session.Info sessionInfo) throws RemoteException {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(33, connectionId).sendToTarget();
        }

        public void onPhoneAccountChanged(String callId, PhoneAccountHandle pHandle, Session.Info sessionInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = pHandle;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(34, args).sendToTarget();
        }

        public void onConnectionServiceFocusReleased(Session.Info sessionInfo) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(35).sendToTarget();
        }
    };

    public ConnectionServiceAdapterServant(IConnectionServiceAdapter delegate) {
        this.mDelegate = delegate;
    }

    public IConnectionServiceAdapter getStub() {
        return this.mStub;
    }
}
