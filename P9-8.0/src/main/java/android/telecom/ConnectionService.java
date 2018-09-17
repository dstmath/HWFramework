package android.telecom;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.telecom.Conference.Listener;
import android.telecom.Connection.RttTextStream;
import android.telecom.Connection.VideoProvider;
import android.telecom.Logging.Runnable;
import android.telecom.Logging.Session;
import android.telecom.Logging.Session.Info;
import com.android.ims.ImsCallProfile;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionService.Stub;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConnectionService extends Service {
    private static final int MSG_ABORT = 3;
    private static final int MSG_ADD_CONNECTION_SERVICE_ADAPTER = 1;
    private static final int MSG_ANSWER = 4;
    private static final int MSG_ANSWER_VIDEO = 17;
    private static final int MSG_CONFERENCE = 12;
    private static final int MSG_CREATE_CONNECTION = 2;
    private static final int MSG_CREATE_CONNECTION_COMPLETE = 29;
    private static final int MSG_CREATE_CONNECTION_FAILED = 25;
    private static final int MSG_DISCONNECT = 6;
    private static final int MSG_HOLD = 7;
    private static final int MSG_MERGE_CONFERENCE = 18;
    private static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 9;
    private static final int MSG_ON_EXTRAS_CHANGED = 24;
    private static final int MSG_ON_POST_DIAL_CONTINUE = 14;
    private static final int MSG_ON_START_RTT = 26;
    private static final int MSG_ON_STOP_RTT = 27;
    private static final int MSG_PLAY_DTMF_TONE = 10;
    private static final int MSG_PULL_EXTERNAL_CALL = 22;
    private static final int MSG_REJECT = 5;
    private static final int MSG_REJECT_WITH_MESSAGE = 20;
    private static final int MSG_REMOVE_CONNECTION_SERVICE_ADAPTER = 16;
    private static final int MSG_RTT_UPGRADE_RESPONSE = 28;
    private static final int MSG_SEND_CALL_EVENT = 23;
    private static final int MSG_SET_ACTIVE_SUB = 101;
    private static final int MSG_SET_LOCAL_HOLD = 100;
    private static final int MSG_SILENCE = 21;
    private static final int MSG_SPLIT_FROM_CONFERENCE = 13;
    private static final int MSG_STOP_DTMF_TONE = 11;
    private static final int MSG_SWAP_CONFERENCE = 19;
    private static final int MSG_UNHOLD = 8;
    private static final boolean PII_DEBUG = Log.isLoggable(3);
    public static final String SERVICE_INTERFACE = "android.telecom.ConnectionService";
    private static final String SESSION_ABORT = "CS.ab";
    private static final String SESSION_ADD_CS_ADAPTER = "CS.aCSA";
    private static final String SESSION_ANSWER = "CS.an";
    private static final String SESSION_ANSWER_VIDEO = "CS.anV";
    private static final String SESSION_CALL_AUDIO_SC = "CS.cASC";
    private static final String SESSION_CONFERENCE = "CS.c";
    private static final String SESSION_CREATE_CONN = "CS.crCo";
    private static final String SESSION_CREATE_CONN_COMPLETE = "CS.crCoC";
    private static final String SESSION_CREATE_CONN_FAILED = "CS.crCoF";
    private static final String SESSION_DISCONNECT = "CS.d";
    private static final String SESSION_EXTRAS_CHANGED = "CS.oEC";
    private static final String SESSION_HANDLER = "H.";
    private static final String SESSION_HOLD = "CS.h";
    private static final String SESSION_MERGE_CONFERENCE = "CS.mC";
    private static final String SESSION_PLAY_DTMF = "CS.pDT";
    private static final String SESSION_POST_DIAL_CONT = "CS.oPDC";
    private static final String SESSION_PULL_EXTERNAL_CALL = "CS.pEC";
    private static final String SESSION_REJECT = "CS.r";
    private static final String SESSION_REJECT_MESSAGE = "CS.rWM";
    private static final String SESSION_REMOVE_CS_ADAPTER = "CS.rCSA";
    private static final String SESSION_RTT_UPGRADE_RESPONSE = "CS.rTRUR";
    private static final String SESSION_SEND_CALL_EVENT = "CS.sCE";
    private static final String SESSION_SILENCE = "CS.s";
    private static final String SESSION_SPLIT_CONFERENCE = "CS.sFC";
    private static final String SESSION_START_RTT = "CS.+RTT";
    private static final String SESSION_STOP_DTMF = "CS.sDT";
    private static final String SESSION_STOP_RTT = "CS.-RTT";
    private static final String SESSION_SWAP_CONFERENCE = "CS.sC";
    private static final String SESSION_UNHOLD = "CS.u";
    private static Connection sNullConnection;
    private final ConnectionServiceAdapter mAdapter = new ConnectionServiceAdapter();
    private boolean mAreAccountsInitialized = false;
    private final IBinder mBinder = new Stub() {
        public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_ADD_CS_ADAPTER);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = adapter;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(1, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_REMOVE_CS_ADAPTER);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = adapter;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(16, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String id, ConnectionRequest request, boolean isIncoming, boolean isUnknown, Info sessionInfo) {
            int i = 1;
            Log.startSession(sessionInfo, ConnectionService.SESSION_CREATE_CONN);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = connectionManagerPhoneAccount;
                args.arg2 = id;
                args.arg3 = request;
                args.arg4 = Log.createSubsession();
                args.argi1 = isIncoming ? 1 : 0;
                if (!isUnknown) {
                    i = 0;
                }
                args.argi2 = i;
                ConnectionService.this.mHandler.obtainMessage(2, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void createConnectionComplete(String id, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CREATE_CONN_COMPLETE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = id;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(29, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void createConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CREATE_CONN_FAILED);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = request;
                args.arg3 = Log.createSubsession();
                args.arg4 = connectionManagerPhoneAccount;
                args.argi1 = isIncoming ? 1 : 0;
                ConnectionService.this.mHandler.obtainMessage(25, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void abort(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_ABORT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(3, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void answerVideo(String callId, int videoState, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_ANSWER_VIDEO);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                args.argi1 = videoState;
                ConnectionService.this.mHandler.obtainMessage(17, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void answer(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_ANSWER);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(4, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void reject(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_REJECT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(5, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void rejectWithMessage(String callId, String message, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_REJECT_MESSAGE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = message;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(20, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void silence(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_SILENCE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(21, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void disconnect(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_DISCONNECT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(6, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void hold(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_HOLD);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(7, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void unhold(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_UNHOLD);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(8, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void onCallAudioStateChanged(String callId, CallAudioState callAudioState, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CALL_AUDIO_SC);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = callAudioState;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(9, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void playDtmfTone(String callId, char digit, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_PLAY_DTMF);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Character.valueOf(digit);
                args.arg2 = callId;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(10, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void stopDtmfTone(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_STOP_DTMF);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(11, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void setLocalCallHold(String callId, int lchState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.argi1 = lchState;
            ConnectionService.this.mHandler.obtainMessage(100, args).sendToTarget();
        }

        public void setActiveSubscription(String callId) {
            Log.i((Object) this, "setActiveSubscription %s", callId);
            if (callId == null) {
                Log.i((Object) this, "when callId is null amount to the key of HashMap is null,when the key is null must happen NullPointerException", new Object[0]);
            } else {
                ConnectionService.this.mHandler.obtainMessage(101, callId).sendToTarget();
            }
        }

        public void conference(String callId1, String callId2, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CONFERENCE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId1;
                args.arg2 = callId2;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(12, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void splitFromConference(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_SPLIT_CONFERENCE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(13, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void mergeConference(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_MERGE_CONFERENCE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(18, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void swapConference(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_SWAP_CONFERENCE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(19, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void onPostDialContinue(String callId, boolean proceed, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_POST_DIAL_CONT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                args.argi1 = proceed ? 1 : 0;
                ConnectionService.this.mHandler.obtainMessage(14, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void pullExternalCall(String callId, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_PULL_EXTERNAL_CALL);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(22, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void sendCallEvent(String callId, String event, Bundle extras, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_SEND_CALL_EVENT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = event;
                args.arg3 = extras;
                args.arg4 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(23, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void onExtrasChanged(String callId, Bundle extras, Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_EXTRAS_CHANGED);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = extras;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(24, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void startRtt(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_START_RTT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = new RttTextStream(toInCall, fromInCall);
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(26, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void stopRtt(String callId, Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_STOP_RTT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(27, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        public void respondToRttUpgradeRequest(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_RTT_UPGRADE_RESPONSE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                if (toInCall == null || fromInCall == null) {
                    args.arg2 = null;
                } else {
                    args.arg2 = new RttTextStream(toInCall, fromInCall);
                }
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(28, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }
    };
    private final Map<String, Conference> mConferenceById = new ConcurrentHashMap();
    private final Listener mConferenceListener = new Listener() {
        public void onStateChanged(Conference conference, int oldState, int newState) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            switch (newState) {
                case 4:
                    ConnectionService.this.mAdapter.setActive(id);
                    return;
                case 5:
                    ConnectionService.this.mAdapter.setOnHold(id);
                    return;
                default:
                    return;
            }
        }

        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
            ConnectionService.this.mAdapter.setDisconnected((String) ConnectionService.this.mIdByConference.get(conference), disconnectCause);
        }

        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> conferenceableConnections) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConference.get(conference), ConnectionService.this.createConnectionIdList(conferenceableConnections));
        }

        public void onDestroyed(Conference conference) {
            ConnectionService.this.removeConference(conference);
        }

        public void onConnectionCapabilitiesChanged(Conference conference, int connectionCapabilities) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            Log.d((Object) this, "call capabilities: conference: %s", Connection.capabilitiesToString(connectionCapabilities));
            ConnectionService.this.mAdapter.setConnectionCapabilities(id, connectionCapabilities);
        }

        public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            Log.d((Object) this, "call capabilities: conference: %s", Connection.propertiesToString(connectionProperties));
            ConnectionService.this.mAdapter.setConnectionProperties(id, connectionProperties);
        }

        public void onVideoStateChanged(Conference c, int videoState) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            Log.d((Object) this, "onVideoStateChanged set video state %d", Integer.valueOf(videoState));
            ConnectionService.this.mAdapter.setVideoState(id, videoState);
        }

        public void onVideoProviderChanged(Conference c, VideoProvider videoProvider) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", c, videoProvider);
            ConnectionService.this.mAdapter.setVideoProvider(id, videoProvider);
        }

        public void onStatusHintsChanged(Conference conference, StatusHints statusHints) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            if (id != null) {
                ConnectionService.this.mAdapter.setStatusHints(id, statusHints);
            }
        }

        public void onExtrasChanged(Conference c, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.putExtras(id, extras);
            }
        }

        public void onExtrasRemoved(Conference c, List<String> keys) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.removeExtras(id, keys);
            }
        }

        public void onConnectionEvent(Conference c, String event, Bundle extras) {
            Log.d((Object) this, "onConnectionEvent  event: " + event, new Object[0]);
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onConnectionEvent(id, event, extras);
            }
        }
    };
    private final Map<String, Connection> mConnectionById = new ConcurrentHashMap();
    private final Connection.Listener mConnectionListener = new Connection.Listener() {
        public void onStateChanged(Connection c, int state) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter set state %s %s", id, Connection.stateToString(state));
            switch (state) {
                case 2:
                    ConnectionService.this.mAdapter.setRinging(id);
                    return;
                case 3:
                    ConnectionService.this.mAdapter.setDialing(id);
                    return;
                case 4:
                    ConnectionService.this.mAdapter.setActive(id);
                    return;
                case 5:
                    ConnectionService.this.mAdapter.setOnHold(id);
                    return;
                case 7:
                    ConnectionService.this.mAdapter.setPulling(id);
                    return;
                default:
                    return;
            }
        }

        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter set disconnected %s", disconnectCause);
            if (ConnectionService.this.mSsNotificationType == 255 && ConnectionService.this.mSsNotificationCode == 255) {
                ConnectionService.this.mAdapter.setDisconnected(id, disconnectCause);
                return;
            }
            ConnectionService.this.mAdapter.setDisconnectedWithSsNotification(id, disconnectCause.getCode(), disconnectCause.getReason(), ConnectionService.this.mSsNotificationType, ConnectionService.this.mSsNotificationCode);
            ConnectionService.this.mSsNotificationType = 255;
            ConnectionService.this.mSsNotificationCode = 255;
        }

        public void onVideoStateChanged(Connection c, int videoState) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter set video state %d", Integer.valueOf(videoState));
            ConnectionService.this.mAdapter.setVideoState(id, videoState);
        }

        public void onAddressChanged(Connection c, Uri address, int presentation) {
            ConnectionService.this.mAdapter.setAddress((String) ConnectionService.this.mIdByConnection.get(c), address, presentation);
        }

        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
            ConnectionService.this.mAdapter.setCallerDisplayName((String) ConnectionService.this.mIdByConnection.get(c), callerDisplayName, presentation);
        }

        public void onDestroyed(Connection c) {
            ConnectionService.this.removeConnection(c);
        }

        public void onPostDialWait(Connection c, String remaining) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter onPostDialWait %s, %s", c, remaining);
            ConnectionService.this.mAdapter.onPostDialWait(id, remaining);
        }

        public void onPostDialChar(Connection c, char nextChar) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter onPostDialChar %s, %s", c, Character.valueOf(nextChar));
            ConnectionService.this.mAdapter.onPostDialChar(id, nextChar);
        }

        public void onRingbackRequested(Connection c, boolean ringback) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "Adapter onRingback %b", Boolean.valueOf(ringback));
            ConnectionService.this.mAdapter.setRingbackRequested(id, ringback);
        }

        public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "capabilities: parcelableconnection: %s", Connection.capabilitiesToString(capabilities));
            ConnectionService.this.mAdapter.setConnectionCapabilities(id, capabilities);
        }

        public void onConnectionPropertiesChanged(Connection c, int properties) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "properties: parcelableconnection: %s", Connection.propertiesToString(properties));
            ConnectionService.this.mAdapter.setConnectionProperties(id, properties);
        }

        public void onVideoProviderChanged(Connection c, VideoProvider videoProvider) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", c, videoProvider);
            ConnectionService.this.mAdapter.setVideoProvider(id, videoProvider);
        }

        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
            ConnectionService.this.mAdapter.setIsVoipAudioMode((String) ConnectionService.this.mIdByConnection.get(c), isVoip);
        }

        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
            ConnectionService.this.mAdapter.setStatusHints((String) ConnectionService.this.mIdByConnection.get(c), statusHints);
        }

        public void onConferenceablesChanged(Connection connection, List<Conferenceable> conferenceables) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConnection.get(connection), ConnectionService.this.createIdList(conferenceables));
        }

        public void onConferenceChanged(Connection connection, Conference conference) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                String str = null;
                if (conference != null) {
                    str = (String) ConnectionService.this.mIdByConference.get(conference);
                }
                ConnectionService.this.mAdapter.setIsConferenced(id, str);
            }
        }

        public void onConferenceMergeFailed(Connection connection) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                ConnectionService.this.mAdapter.onConferenceMergeFailed(id);
            }
        }

        public void onExtrasChanged(Connection c, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.putExtras(id, extras);
            }
        }

        public void onExtrasRemoved(Connection c, List<String> keys) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.removeExtras(id, keys);
            }
        }

        public void onConnectionEvent(Connection connection, String event, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                ConnectionService.this.mAdapter.onConnectionEvent(id, event, extras);
            }
        }

        public void onAudioRouteChanged(Connection c, int audioRoute) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.setAudioRoute(id, audioRoute);
            }
        }

        public void onSsNotificationData(int type, int code) {
            ConnectionService.this.mSsNotificationType = type;
            ConnectionService.this.mSsNotificationCode = code;
        }

        public void onPhoneAccountChanged(Connection c, PhoneAccountHandle pHandle) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.i((Object) this, "Adapter onPhoneAccountChanged %s, %s", c, pHandle);
            ConnectionService.this.mAdapter.setPhoneAccountHandle(id, pHandle);
        }

        public void onRttInitiationSuccess(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttInitiationSuccess(id);
            }
        }

        public void onRttInitiationFailure(Connection c, int reason) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttInitiationFailure(id, reason);
            }
        }

        public void onRttSessionRemotelyTerminated(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttSessionRemotelyTerminated(id);
            }
        }

        public void onRemoteRttRequest(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRemoteRttRequest(id);
            }
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            SomeArgs args;
            final String id;
            final ConnectionRequest request;
            final boolean isIncoming;
            switch (msg.what) {
                case 1:
                    args = msg.obj;
                    try {
                        IConnectionServiceAdapter adapter = args.arg1;
                        Log.continueSession((Session) args.arg2, "H.CS.aCSA");
                        ConnectionService.this.mAdapter.addAdapter(adapter);
                        ConnectionService.this.onAdapterAttached();
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 2:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg4, "H.CS.crCo");
                    try {
                        final PhoneAccountHandle connectionManagerPhoneAccount = args.arg1;
                        id = args.arg2;
                        request = args.arg3;
                        isIncoming = args.argi1 == 1;
                        final boolean isUnknown = args.argi2 == 1;
                        if (ConnectionService.this.mAreAccountsInitialized) {
                            ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                        } else {
                            Log.d((Object) this, "Enqueueing pre-init request %s", id);
                            ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCo.pICR", null) {
                                public void loggedRun() {
                                    ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                                }
                            }.prepare());
                        }
                        args.recycle();
                        Log.endSession();
                        return;
                    } catch (Throwable th) {
                        args.recycle();
                        Log.endSession();
                    }
                case 3:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.ab");
                    try {
                        ConnectionService.this.abort((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 4:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.an");
                    try {
                        ConnectionService.this.answer((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 5:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.r");
                    try {
                        ConnectionService.this.reject((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 6:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.d");
                    try {
                        ConnectionService.this.disconnect((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 7:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.r");
                    try {
                        ConnectionService.this.hold((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 8:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.u");
                    try {
                        ConnectionService.this.unhold((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 9:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg3, "H.CS.cASC");
                    try {
                        String callId = (String) args.arg1;
                        CallAudioState audioState = args.arg2;
                        if (audioState != null) {
                            ConnectionService.this.onCallAudioStateChanged(callId, new CallAudioState(audioState));
                        }
                        args.recycle();
                        Log.endSession();
                        return;
                    } catch (Throwable th2) {
                        args.recycle();
                        Log.endSession();
                    }
                case 10:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg3, "H.CS.pDT");
                        ConnectionService.this.playDtmfTone((String) args.arg2, ((Character) args.arg1).charValue());
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 11:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.sDT");
                        ConnectionService.this.stopDtmfTone((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 12:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg3, "H.CS.c");
                        ConnectionService.this.conference(args.arg1, args.arg2);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 13:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.sFC");
                        ConnectionService.this.splitFromConference((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 14:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.oPDC");
                        ConnectionService.this.onPostDialContinue((String) args.arg1, args.argi1 == 1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 16:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.rCSA");
                        ConnectionService.this.mAdapter.removeAdapter((IConnectionServiceAdapter) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 17:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.anV");
                    try {
                        ConnectionService.this.answerVideo(args.arg1, args.argi1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 18:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.mC");
                        ConnectionService.this.mergeConference((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 19:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.sC");
                        ConnectionService.this.swapConference((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 20:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg3, "H.CS.rWM");
                    try {
                        ConnectionService.this.reject((String) args.arg1, (String) args.arg2);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 21:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.s");
                    try {
                        ConnectionService.this.silence((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 22:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.pEC");
                        ConnectionService.this.pullExternalCall((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 23:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg4, "H.CS.sCE");
                        ConnectionService.this.sendCallEvent((String) args.arg1, args.arg2, args.arg3);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 24:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg3, "H.CS.oEC");
                        ConnectionService.this.handleExtrasChanged((String) args.arg1, (Bundle) args.arg2);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 25:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg3, "H.CS.crCoF");
                    try {
                        id = (String) args.arg1;
                        request = (ConnectionRequest) args.arg2;
                        isIncoming = args.argi1 == 1;
                        final PhoneAccountHandle connectionMgrPhoneAccount = args.arg4;
                        if (ConnectionService.this.mAreAccountsInitialized) {
                            Log.i((Object) this, "createConnectionFailed %s", id);
                            ConnectionService.this.createConnectionFailed(connectionMgrPhoneAccount, id, request, isIncoming);
                        } else {
                            Log.d((Object) this, "Enqueueing pre-init request %s", id);
                            final String str = id;
                            final ConnectionRequest connectionRequest = request;
                            final boolean z = isIncoming;
                            ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCoF.pICR", null) {
                                public void loggedRun() {
                                    ConnectionService.this.createConnectionFailed(connectionMgrPhoneAccount, str, connectionRequest, z);
                                }
                            }.prepare());
                        }
                        args.recycle();
                        Log.endSession();
                        return;
                    } catch (Throwable th3) {
                        args.recycle();
                        Log.endSession();
                    }
                case 26:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg3, "H.CS.+RTT");
                        ConnectionService.this.startRtt((String) args.arg1, args.arg2);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 27:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg2, "H.CS.-RTT");
                        ConnectionService.this.stopRtt((String) args.arg1);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 28:
                    args = (SomeArgs) msg.obj;
                    try {
                        Log.continueSession((Session) args.arg3, "H.CS.rTRUR");
                        ConnectionService.this.handleRttUpgradeResponse((String) args.arg1, (RttTextStream) args.arg2);
                        return;
                    } finally {
                        args.recycle();
                        Log.endSession();
                    }
                case 29:
                    args = (SomeArgs) msg.obj;
                    Log.continueSession((Session) args.arg2, "H.CS.crCoC");
                    try {
                        id = (String) args.arg1;
                        if (ConnectionService.this.mAreAccountsInitialized) {
                            ConnectionService.this.notifyCreateConnectionComplete(id);
                        } else {
                            Log.d((Object) this, "Enqueueing pre-init request %s", id);
                            ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCoC.pICR", null) {
                                public void loggedRun() {
                                    ConnectionService.this.notifyCreateConnectionComplete(id);
                                }
                            }.prepare());
                        }
                        args.recycle();
                        Log.endSession();
                        return;
                    } catch (Throwable th4) {
                        args.recycle();
                        Log.endSession();
                    }
                case 100:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.setLocalCallHold((String) args.arg1, args.argi1);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 101:
                    ConnectionService.this.setActiveSubscription((String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private int mId = 0;
    private final Map<Conference, String> mIdByConference = new ConcurrentHashMap();
    private final Map<Connection, String> mIdByConnection = new ConcurrentHashMap();
    private Object mIdSyncRoot = new Object();
    private final List<Runnable> mPreInitializationConnectionRequests = new ArrayList();
    private final RemoteConnectionManager mRemoteConnectionManager = new RemoteConnectionManager(this);
    private int mSsNotificationCode = 255;
    private int mSsNotificationType = 255;
    private Conference sNullConference;

    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        endAllConnections();
        return super.onUnbind(intent);
    }

    private void createConnection(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
        Connection connection;
        Log.d((Object) this, "createConnection, callManagerAccount: %s, callId: %s, request: %s, isIncoming: %b, isUnknown: %b", callManagerAccount, callId, request, Boolean.valueOf(isIncoming), Boolean.valueOf(isUnknown));
        if (isUnknown) {
            connection = onCreateUnknownConnection(callManagerAccount, request);
        } else if (isIncoming) {
            connection = onCreateIncomingConnection(callManagerAccount, request);
        } else {
            connection = onCreateOutgoingConnection(callManagerAccount, request);
        }
        Log.d((Object) this, "createConnection, connection: %s", connection);
        if (connection == null) {
            connection = Connection.createFailedConnection(new DisconnectCause(1));
        }
        connection.setTelecomCallId(callId);
        if (connection.getState() != 6) {
            addConnection(callId, connection);
        }
        Log.v((Object) this, "createConnection, number: %s, state: %s, capabilities: %s, properties: %s", "xxxxxx", Connection.stateToString(connection.getState()), Connection.capabilitiesToString(connection.getConnectionCapabilities()), Connection.propertiesToString(connection.getConnectionProperties()));
        Log.d((Object) this, "createConnection, calling handleCreateConnectionSuccessful %s", callId);
        this.mAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(getAccountHandle(request, connection), connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getSupportedAudioRoutes(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), createIdList(connection.getConferenceables()), connection.getExtras()));
        if (isIncoming && request.shouldShowIncomingCallUi() && (connection.getConnectionProperties() & 128) == 128) {
            connection.onShowIncomingCallUi();
        }
        if (isUnknown) {
            triggerConferenceRecalculate();
        }
    }

    public PhoneAccountHandle getAccountHandle(ConnectionRequest request, Connection connection) {
        PhoneAccountHandle pHandle = connection.getPhoneAccountHandle();
        if (pHandle == null) {
            return request.getAccountHandle();
        }
        Log.i((Object) this, "getAccountHandle, return account handle from local, %s", pHandle);
        return pHandle;
    }

    private void createConnectionFailed(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming) {
        Log.i((Object) this, "createConnectionFailed %s", callId);
        if (isIncoming) {
            onCreateIncomingConnectionFailed(callManagerAccount, request);
        } else {
            onCreateOutgoingConnectionFailed(callManagerAccount, request);
        }
    }

    private void notifyCreateConnectionComplete(String callId) {
        Log.i((Object) this, "notifyCreateConnectionComplete %s", callId);
        onCreateConnectionComplete(findConnectionForAction(callId, "notifyCreateConnectionComplete"));
    }

    private void abort(String callId) {
        Log.d((Object) this, "abort %s", callId);
        findConnectionForAction(callId, "abort").onAbort();
    }

    private void answerVideo(String callId, int videoState) {
        Log.d((Object) this, "answerVideo %s", callId);
        findConnectionForAction(callId, "answer").onAnswer(videoState);
    }

    private void answer(String callId) {
        Log.d((Object) this, "answer %s", callId);
        findConnectionForAction(callId, "answer").onAnswer();
    }

    private void reject(String callId) {
        Log.d((Object) this, "reject %s", callId);
        findConnectionForAction(callId, "reject").onReject();
    }

    private void reject(String callId, String rejectWithMessage) {
        Log.d((Object) this, "reject %s with message", callId);
        findConnectionForAction(callId, "reject").onReject(rejectWithMessage);
    }

    private void silence(String callId) {
        Log.d((Object) this, "silence %s", callId);
        findConnectionForAction(callId, "silence").onSilence();
    }

    private void disconnect(String callId) {
        Log.d((Object) this, "disconnect %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "disconnect").onDisconnect();
        } else {
            findConferenceForAction(callId, "disconnect").onDisconnect();
        }
    }

    private void hold(String callId) {
        Log.d((Object) this, "hold %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, LaunchMode.HOLD).onHold();
        } else {
            findConferenceForAction(callId, LaunchMode.HOLD).onHold();
        }
    }

    private void unhold(String callId) {
        Log.d((Object) this, "unhold %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "unhold").onUnhold();
        } else {
            findConferenceForAction(callId, "unhold").onUnhold();
        }
    }

    private void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
        Log.d((Object) this, "onAudioStateChanged %s %s", callId, callAudioState);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        } else {
            findConferenceForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        }
    }

    private void playDtmfTone(String callId, char digit) {
        Log.d((Object) this, "playDtmfTone %s *", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        } else {
            findConferenceForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        }
    }

    private void stopDtmfTone(String callId) {
        Log.d((Object) this, "stopDtmfTone %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "stopDtmfTone").onStopDtmfTone();
        } else {
            findConferenceForAction(callId, "stopDtmfTone").onStopDtmfTone();
        }
    }

    private void setLocalCallHold(String callId, int lchStatus) {
        Log.d((Object) this, "setLocalCallHold %s", callId);
        findConnectionForAction(callId, "setLocalCallHold").setLocalCallHold(lchStatus);
    }

    private void setActiveSubscription(String callId) {
        Log.d((Object) this, "setActiveSubscription %s", callId);
        findConnectionForAction(callId, "setActiveSubscription").setActiveSubscription();
    }

    private void conference(String callId1, String callId2) {
        Log.d((Object) this, "conference %s, %s", callId1, callId2);
        Connection connection2 = findConnectionForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
        Conference conference2 = getNullConference();
        if (connection2 == getNullConnection()) {
            conference2 = findConferenceForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
            if (conference2 == getNullConference()) {
                Log.w((Object) this, "Connection2 or Conference2 missing in conference request %s.", callId2);
                return;
            }
        }
        Connection connection1 = findConnectionForAction(callId1, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection1 == getNullConnection()) {
            Conference conference1 = findConferenceForAction(callId1, "addConnection");
            if (conference1 == getNullConference()) {
                Log.w((Object) this, "Connection1 or Conference1 missing in conference request %s.", callId1);
            } else if (connection2 != getNullConnection()) {
                conference1.onMerge(connection2);
            } else {
                Log.wtf((Object) this, "There can only be one conference and an attempt was made to merge two conferences.", new Object[0]);
            }
        } else if (conference2 != getNullConference()) {
            conference2.onMerge(connection1);
        } else {
            onConference(connection1, connection2);
        }
    }

    private void splitFromConference(String callId) {
        Log.d((Object) this, "splitFromConference(%s)", callId);
        Connection connection = findConnectionForAction(callId, "splitFromConference");
        if (connection == getNullConnection()) {
            Log.w((Object) this, "Connection missing in conference request %s.", callId);
            return;
        }
        Conference conference = connection.getConference();
        if (conference != null) {
            conference.onSeparate(connection);
        }
    }

    private void mergeConference(String callId) {
        Log.d((Object) this, "mergeConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "mergeConference");
        if (conference != null) {
            conference.onMerge();
        }
    }

    private void swapConference(String callId) {
        Log.d((Object) this, "swapConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "swapConference");
        if (conference != null) {
            conference.onSwap();
        }
    }

    private void pullExternalCall(String callId) {
        Log.d((Object) this, "pullExternalCall(%s)", callId);
        Connection connection = findConnectionForAction(callId, "pullExternalCall");
        if (connection != null) {
            connection.onPullExternalCall();
        }
    }

    private void sendCallEvent(String callId, String event, Bundle extras) {
        Log.d((Object) this, "sendCallEvent(%s, %s)", callId, event);
        Connection connection = findConnectionForAction(callId, "sendCallEvent");
        if (connection != null) {
            connection.onCallEvent(event, extras);
        }
    }

    private void handleExtrasChanged(String callId, Bundle extras) {
        Log.d((Object) this, "handleExtrasChanged(%s, %s)", callId, extras);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        } else if (this.mConferenceById.containsKey(callId)) {
            findConferenceForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        }
    }

    private void startRtt(String callId, RttTextStream rttTextStream) {
        Log.d((Object) this, "startRtt(%s)", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "startRtt").onStartRtt(rttTextStream);
        } else if (this.mConferenceById.containsKey(callId)) {
            Log.w((Object) this, "startRtt called on a conference.", new Object[0]);
        }
    }

    private void stopRtt(String callId) {
        Log.d((Object) this, "stopRtt(%s)", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "stopRtt").onStopRtt();
        } else if (this.mConferenceById.containsKey(callId)) {
            Log.w((Object) this, "stopRtt called on a conference.", new Object[0]);
        }
    }

    private void handleRttUpgradeResponse(String callId, RttTextStream rttTextStream) {
        String str = "handleRttUpgradeResponse(%s, %s)";
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = Boolean.valueOf(rttTextStream == null);
        Log.d((Object) this, str, objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "handleRttUpgradeResponse").handleRttUpgradeResponse(rttTextStream);
        } else if (this.mConferenceById.containsKey(callId)) {
            Log.w((Object) this, "handleRttUpgradeResponse called on a conference.", new Object[0]);
        }
    }

    private void onPostDialContinue(String callId, boolean proceed) {
        Log.d((Object) this, "onPostDialContinue(%s)", callId);
        findConnectionForAction(callId, "stopDtmfTone").onPostDialContinue(proceed);
    }

    private void onAdapterAttached() {
        if (!this.mAreAccountsInitialized) {
            this.mAdapter.queryRemoteConnectionServices(new RemoteServiceCallback.Stub() {
                public void onResult(List<ComponentName> componentNames, List<IBinder> services) {
                    final List<ComponentName> list = componentNames;
                    final List<IBinder> list2 = services;
                    ConnectionService.this.mHandler.post(new Runnable("oAA.qRCS.oR", null) {
                        public void loggedRun() {
                            int i = 0;
                            while (i < list.size() && i < list2.size()) {
                                ConnectionService.this.mRemoteConnectionManager.addConnectionService((ComponentName) list.get(i), Stub.asInterface((IBinder) list2.get(i)));
                                i++;
                            }
                            ConnectionService.this.onAccountsInitialized();
                            Log.d((Object) this, "remote connection services found: " + list2, new Object[0]);
                        }
                    }.prepare());
                }

                public void onError() {
                    ConnectionService.this.mHandler.post(new Runnable("oAA.qRCS.oE", null) {
                        public void loggedRun() {
                            ConnectionService.this.mAreAccountsInitialized = true;
                        }
                    }.prepare());
                }
            });
        }
    }

    public final RemoteConnection createRemoteIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, true);
    }

    public final RemoteConnection createRemoteOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, false);
    }

    public final void conferenceRemoteConnections(RemoteConnection remoteConnection1, RemoteConnection remoteConnection2) {
        this.mRemoteConnectionManager.conferenceRemoteConnections(remoteConnection1, remoteConnection2);
    }

    public final void addConference(Conference conference) {
        Log.d(this, "addConference: conference=%s", conference);
        String id = addConferenceInternal(conference);
        if (id != null) {
            IVideoProvider iVideoProvider;
            List<String> connectionIds = new ArrayList(2);
            for (Connection connection : conference.getConnections()) {
                if (this.mIdByConnection.containsKey(connection)) {
                    connectionIds.add((String) this.mIdByConnection.get(connection));
                }
            }
            conference.setTelecomCallId(id);
            PhoneAccountHandle phoneAccountHandle = conference.getPhoneAccountHandle();
            int state = conference.getState();
            int connectionCapabilities = conference.getConnectionCapabilities();
            int connectionProperties = conference.getConnectionProperties();
            if (conference.getVideoProvider() == null) {
                iVideoProvider = null;
            } else {
                iVideoProvider = conference.getVideoProvider().getInterface();
            }
            this.mAdapter.addConferenceCall(id, new ParcelableConference(phoneAccountHandle, state, connectionCapabilities, connectionProperties, connectionIds, iVideoProvider, conference.getVideoState(), conference.getConnectTimeMillis(), conference.getStatusHints(), conference.getExtras()));
            this.mAdapter.setVideoProvider(id, conference.getVideoProvider());
            this.mAdapter.setVideoState(id, conference.getVideoState());
            for (Connection connection2 : conference.getConnections()) {
                String connectionId = (String) this.mIdByConnection.get(connection2);
                if (connectionId != null) {
                    this.mAdapter.setIsConferenced(connectionId, id);
                }
            }
        }
    }

    public final void addExistingConnection(PhoneAccountHandle phoneAccountHandle, Connection connection) {
        addExistingConnection(phoneAccountHandle, connection, null);
    }

    public final void addExistingConnection(PhoneAccountHandle phoneAccountHandle, Connection connection, Conference conference) {
        String id = addExistingConnectionInternal(phoneAccountHandle, connection);
        if (id != null) {
            List<String> arrayList = new ArrayList(0);
            String str = null;
            if (conference != null) {
                str = (String) this.mIdByConference.get(conference);
            }
            this.mAdapter.addExistingConnection(id, new ParcelableConnection(phoneAccountHandle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getSupportedAudioRoutes(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), arrayList, connection.getExtras(), str));
        }
    }

    public final Collection<Connection> getAllConnections() {
        return this.mConnectionById.values();
    }

    public final Collection<Conference> getAllConferences() {
        return this.mConferenceById.values();
    }

    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void onCreateConnectionComplete(Connection connection) {
    }

    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    }

    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    }

    public void triggerConferenceRecalculate() {
    }

    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public Connection onCreateUnknownConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void onConference(Connection connection1, Connection connection2) {
    }

    public void onRemoteConferenceAdded(RemoteConference conference) {
    }

    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
    }

    public boolean containsConference(Conference conference) {
        return this.mIdByConference.containsKey(conference);
    }

    void addRemoteConference(RemoteConference remoteConference) {
        onRemoteConferenceAdded(remoteConference);
    }

    void addRemoteExistingConnection(RemoteConnection remoteConnection) {
        onRemoteExistingConnectionAdded(remoteConnection);
    }

    private void onAccountsInitialized() {
        this.mAreAccountsInitialized = true;
        for (Runnable r : this.mPreInitializationConnectionRequests) {
            r.run();
        }
        this.mPreInitializationConnectionRequests.clear();
    }

    private String addExistingConnectionInternal(PhoneAccountHandle handle, Connection connection) {
        String id;
        if (connection.getExtras() != null && connection.getExtras().containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            id = connection.getExtras().getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID);
            Log.d((Object) this, "addExistingConnectionInternal - conn %s reusing original id %s", connection.getTelecomCallId(), id);
        } else if (handle == null) {
            id = UUID.randomUUID().toString();
        } else {
            id = handle.getComponentName().getClassName() + "@" + getNextCallId();
        }
        addConnection(id, connection);
        return id;
    }

    private void addConnection(String callId, Connection connection) {
        connection.setTelecomCallId(callId);
        this.mConnectionById.put(callId, connection);
        this.mIdByConnection.put(connection, callId);
        connection.addConnectionListener(this.mConnectionListener);
        connection.setConnectionService(this);
    }

    protected void removeConnection(Connection connection) {
        connection.unsetConnectionService(this);
        connection.removeConnectionListener(this.mConnectionListener);
        String id = (String) this.mIdByConnection.get(connection);
        if (id != null) {
            this.mConnectionById.remove(id);
            this.mIdByConnection.remove(connection);
            this.mAdapter.removeCall(id);
        }
    }

    private String addConferenceInternal(Conference conference) {
        String originalId = null;
        if (conference.getExtras() != null && conference.getExtras().containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            originalId = conference.getExtras().getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID);
            Log.d((Object) this, "addConferenceInternal: conf %s reusing original id %s", conference.getTelecomCallId(), originalId);
        }
        if (this.mIdByConference.containsKey(conference)) {
            Log.w((Object) this, "Re-adding an existing conference: %s.", conference);
        } else if (conference != null) {
            String id = originalId == null ? UUID.randomUUID().toString() : originalId;
            this.mConferenceById.put(id, conference);
            this.mIdByConference.put(conference, id);
            conference.addListener(this.mConferenceListener);
            return id;
        }
        return null;
    }

    private void removeConference(Conference conference) {
        if (this.mIdByConference.containsKey(conference)) {
            conference.removeListener(this.mConferenceListener);
            String id = (String) this.mIdByConference.get(conference);
            this.mConferenceById.remove(id);
            this.mIdByConference.remove(conference);
            this.mAdapter.removeCall(id);
        }
    }

    private Connection findConnectionForAction(String callId, String action) {
        if (this.mConnectionById.containsKey(callId)) {
            return (Connection) this.mConnectionById.get(callId);
        }
        Log.w((Object) this, "%s - Cannot find Connection %s", action, callId);
        return getNullConnection();
    }

    static synchronized Connection getNullConnection() {
        Connection connection;
        synchronized (ConnectionService.class) {
            if (sNullConnection == null) {
                sNullConnection = new Connection() {
                };
            }
            connection = sNullConnection;
        }
        return connection;
    }

    private Conference findConferenceForAction(String conferenceId, String action) {
        if (this.mConferenceById.containsKey(conferenceId)) {
            return (Conference) this.mConferenceById.get(conferenceId);
        }
        Log.w((Object) this, "%s - Cannot find conference %s", action, conferenceId);
        return getNullConference();
    }

    private List<String> createConnectionIdList(List<Connection> connections) {
        List<String> ids = new ArrayList();
        for (Connection c : connections) {
            if (this.mIdByConnection.containsKey(c)) {
                ids.add((String) this.mIdByConnection.get(c));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private List<String> createIdList(List<Conferenceable> conferenceables) {
        List<String> ids = new ArrayList();
        for (Conferenceable c : conferenceables) {
            if (c instanceof Connection) {
                Connection connection = (Connection) c;
                if (this.mIdByConnection.containsKey(connection)) {
                    ids.add((String) this.mIdByConnection.get(connection));
                }
            } else if (c instanceof Conference) {
                Conference conference = (Conference) c;
                if (this.mIdByConference.containsKey(conference)) {
                    ids.add((String) this.mIdByConference.get(conference));
                }
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private Conference getNullConference() {
        if (this.sNullConference == null) {
            this.sNullConference = new Conference(null) {
            };
        }
        return this.sNullConference;
    }

    private void endAllConnections() {
        for (Connection connection : this.mIdByConnection.keySet()) {
            if (connection.getConference() == null) {
                connection.onDisconnect();
            }
        }
        for (Conference conference : this.mIdByConference.keySet()) {
            conference.onDisconnect();
        }
    }

    private int getNextCallId() {
        int i;
        synchronized (this.mIdSyncRoot) {
            i = this.mId + 1;
            this.mId = i;
        }
        return i;
    }
}
