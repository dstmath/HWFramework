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
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.Logging.Runnable;
import android.telecom.Logging.Session;
import android.telephony.ims.ImsCallProfile;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConnectionService extends Service {
    public static final String EXTRA_IS_HANDOVER = "android.telecom.extra.IS_HANDOVER";
    private static final int MSG_ABORT = 3;
    private static final int MSG_ADD_CONNECTION_SERVICE_ADAPTER = 1;
    private static final int MSG_ANSWER = 4;
    private static final int MSG_ANSWER_VIDEO = 17;
    private static final int MSG_CONFERENCE = 12;
    private static final int MSG_CONNECTION_SERVICE_FOCUS_GAINED = 31;
    private static final int MSG_CONNECTION_SERVICE_FOCUS_LOST = 30;
    private static final int MSG_CREATE_CONNECTION = 2;
    private static final int MSG_CREATE_CONNECTION_COMPLETE = 29;
    private static final int MSG_CREATE_CONNECTION_FAILED = 25;
    private static final int MSG_DEFLECT = 34;
    private static final int MSG_DISCONNECT = 6;
    private static final int MSG_HANDOVER_COMPLETE = 33;
    private static final int MSG_HANDOVER_FAILED = 32;
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
    private static final String SESSION_CONNECTION_SERVICE_FOCUS_GAINED = "CS.cSFG";
    private static final String SESSION_CONNECTION_SERVICE_FOCUS_LOST = "CS.cSFL";
    private static final String SESSION_CREATE_CONN = "CS.crCo";
    private static final String SESSION_CREATE_CONN_COMPLETE = "CS.crCoC";
    private static final String SESSION_CREATE_CONN_FAILED = "CS.crCoF";
    private static final String SESSION_DEFLECT = "CS.def";
    private static final String SESSION_DISCONNECT = "CS.d";
    private static final String SESSION_EXTRAS_CHANGED = "CS.oEC";
    private static final String SESSION_HANDLER = "H.";
    private static final String SESSION_HANDOVER_COMPLETE = "CS.hC";
    private static final String SESSION_HANDOVER_FAILED = "CS.haF";
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
    private static final String SESSION_UPDATE_RTT_PIPES = "CS.uRTT";
    private static Connection sNullConnection;
    private Set<String> mAbnormalAbortCallIds = new HashSet();
    private final ConnectionServiceAdapter mAdapter = new ConnectionServiceAdapter();
    private boolean mAreAccountsInitialized = false;
    private final IBinder mBinder = new IConnectionService.Stub() {
        /* class android.telecom.ConnectionService.AnonymousClass1 */

        @Override // com.android.internal.telecom.IConnectionService
        public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String id, ConnectionRequest request, boolean isIncoming, boolean isUnknown, Session.Info sessionInfo) {
            int i = 0;
            if (id == null) {
                Log.w(this, "createConnection, callId can not create connection", new Object[0]);
                return;
            }
            Log.startSession(sessionInfo, ConnectionService.SESSION_CREATE_CONN);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = connectionManagerPhoneAccount;
                args.arg2 = id;
                args.arg3 = request;
                args.arg4 = Log.createSubsession();
                args.argi1 = isIncoming ? 1 : 0;
                if (isUnknown) {
                    i = 1;
                }
                args.argi2 = i;
                ConnectionService.this.mHandler.obtainMessage(2, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void createConnectionComplete(String id, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void createConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void handoverFailed(String callId, ConnectionRequest request, int reason, Session.Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_HANDOVER_FAILED);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = request;
                args.arg3 = Log.createSubsession();
                args.arg4 = Integer.valueOf(reason);
                ConnectionService.this.mHandler.obtainMessage(32, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void handoverComplete(String callId, Session.Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_HANDOVER_COMPLETE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(33, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void abort(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void answerVideo(String callId, int videoState, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void answer(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void deflect(String callId, Uri address, Session.Info sessionInfo) {
            Log.startSession(sessionInfo, ConnectionService.SESSION_DEFLECT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = address;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(34, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void reject(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void rejectWithMessage(String callId, String message, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void silence(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void disconnect(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void hold(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void unhold(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void onCallAudioStateChanged(String callId, CallAudioState callAudioState, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void playDtmfTone(String callId, char digit, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void stopDtmfTone(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void setLocalCallHold(String callId, int lchState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.argi1 = lchState;
            ConnectionService.this.mHandler.obtainMessage(100, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void setActiveSubscription(String callId) {
            Log.i(this, "setActiveSubscription %s", callId);
            if (callId == null) {
                Log.i(this, "when callId is null amount to the key of HashMap is null,when the key is null must happen NullPointerException", new Object[0]);
            } else {
                ConnectionService.this.mHandler.obtainMessage(101, callId).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void conference(String callId1, String callId2, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void splitFromConference(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void mergeConference(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void swapConference(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void onPostDialContinue(String callId, boolean proceed, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void pullExternalCall(String callId, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void sendCallEvent(String callId, String event, Bundle extras, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void onExtrasChanged(String callId, Bundle extras, Session.Info sessionInfo) {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void startRtt(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Session.Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_START_RTT);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = new Connection.RttTextStream(toInCall, fromInCall);
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(26, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void stopRtt(String callId, Session.Info sessionInfo) throws RemoteException {
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

        @Override // com.android.internal.telecom.IConnectionService
        public void respondToRttUpgradeRequest(String callId, ParcelFileDescriptor fromInCall, ParcelFileDescriptor toInCall, Session.Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_RTT_UPGRADE_RESPONSE);
            try {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                if (toInCall != null) {
                    if (fromInCall != null) {
                        args.arg2 = new Connection.RttTextStream(toInCall, fromInCall);
                        args.arg3 = Log.createSubsession();
                        ConnectionService.this.mHandler.obtainMessage(28, args).sendToTarget();
                    }
                }
                args.arg2 = null;
                args.arg3 = Log.createSubsession();
                ConnectionService.this.mHandler.obtainMessage(28, args).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void connectionServiceFocusLost(Session.Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CONNECTION_SERVICE_FOCUS_LOST);
            try {
                ConnectionService.this.mHandler.obtainMessage(30).sendToTarget();
            } finally {
                Log.endSession();
            }
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void connectionServiceFocusGained(Session.Info sessionInfo) throws RemoteException {
            Log.startSession(sessionInfo, ConnectionService.SESSION_CONNECTION_SERVICE_FOCUS_GAINED);
            try {
                ConnectionService.this.mHandler.obtainMessage(31).sendToTarget();
            } finally {
                Log.endSession();
            }
        }
    };
    private final Map<String, Conference> mConferenceById = new ConcurrentHashMap();
    private final Conference.Listener mConferenceListener = new Conference.Listener() {
        /* class android.telecom.ConnectionService.AnonymousClass3 */

        @Override // android.telecom.Conference.Listener
        public void onStateChanged(Conference conference, int oldState, int newState) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            if (newState == 4) {
                ConnectionService.this.mAdapter.setActive(id);
            } else if (newState == 5) {
                ConnectionService.this.mAdapter.setOnHold(id);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
            ConnectionService.this.mAdapter.setDisconnected((String) ConnectionService.this.mIdByConference.get(conference), disconnectCause);
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        @Override // android.telecom.Conference.Listener
        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> conferenceableConnections) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConference.get(conference), ConnectionService.this.createConnectionIdList(conferenceableConnections));
        }

        @Override // android.telecom.Conference.Listener
        public void onDestroyed(Conference conference) {
            ConnectionService.this.removeConference(conference);
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionCapabilitiesChanged(Conference conference, int connectionCapabilities) {
            Log.i(this, "call capabilities: conference: %s", Connection.capabilitiesToString(connectionCapabilities));
            ConnectionService.this.mAdapter.setConnectionCapabilities((String) ConnectionService.this.mIdByConference.get(conference), connectionCapabilities);
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
            Log.i(this, "call capabilities: conference: %s", Connection.propertiesToString(connectionProperties));
            ConnectionService.this.mAdapter.setConnectionProperties((String) ConnectionService.this.mIdByConference.get(conference), connectionProperties);
        }

        @Override // android.telecom.Conference.Listener
        public void onVideoStateChanged(Conference c, int videoState) {
            Log.i(this, "onVideoStateChanged set video state %d", Integer.valueOf(videoState));
            ConnectionService.this.mAdapter.setVideoState((String) ConnectionService.this.mIdByConference.get(c), videoState);
        }

        @Override // android.telecom.Conference.Listener
        public void onVideoProviderChanged(Conference c, Connection.VideoProvider videoProvider) {
            Log.i(this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", c, videoProvider);
            ConnectionService.this.mAdapter.setVideoProvider((String) ConnectionService.this.mIdByConference.get(c), videoProvider);
        }

        @Override // android.telecom.Conference.Listener
        public void onStatusHintsChanged(Conference conference, StatusHints statusHints) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            if (id != null) {
                ConnectionService.this.mAdapter.setStatusHints(id, statusHints);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onExtrasChanged(Conference c, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.putExtras(id, extras);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onExtrasRemoved(Conference c, List<String> keys) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.removeExtras(id, keys);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionEvent(Conference c, String event, Bundle extras) {
            Log.i(this, "onConnectionEvent  event: " + event, new Object[0]);
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onConnectionEvent(id, event, extras);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onConferenceStateChanged(Conference c, boolean isConference) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.setConferenceState(id, isConference);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onAddressChanged(Conference c, Uri newAddress, int presentation) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.setAddress(id, newAddress, presentation);
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onCallerDisplayNameChanged(Conference c, String callerDisplayName, int presentation) {
            String id = (String) ConnectionService.this.mIdByConference.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.setCallerDisplayName(id, callerDisplayName, presentation);
            }
        }
    };
    private final Map<String, Connection> mConnectionById = new ConcurrentHashMap();
    private final Connection.Listener mConnectionListener = new Connection.Listener() {
        /* class android.telecom.ConnectionService.AnonymousClass4 */

        @Override // android.telecom.Connection.Listener
        public void onStateChanged(Connection c, int state) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.i(this, "Adapter set state %s %s", id, Connection.stateToString(state));
            switch (state) {
                case 1:
                case 6:
                default:
                    return;
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
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.i(this, "Adapter set disconnected %s", disconnectCause);
            if (ConnectionService.this.mSsNotificationType == 255 && ConnectionService.this.mSsNotificationCode == 255) {
                ConnectionService.this.mAdapter.setDisconnected(id, disconnectCause);
                return;
            }
            ConnectionService.this.mAdapter.setDisconnectedWithSsNotification(id, disconnectCause.getCode(), disconnectCause.getReason(), ConnectionService.this.mSsNotificationType, ConnectionService.this.mSsNotificationCode);
            ConnectionService.this.mSsNotificationType = 255;
            ConnectionService.this.mSsNotificationCode = 255;
        }

        @Override // android.telecom.Connection.Listener
        public void onVideoStateChanged(Connection c, int videoState) {
            Log.i(this, "Adapter set video state %d", Integer.valueOf(videoState));
            ConnectionService.this.mAdapter.setVideoState((String) ConnectionService.this.mIdByConnection.get(c), videoState);
        }

        @Override // android.telecom.Connection.Listener
        public void onAddressChanged(Connection c, Uri address, int presentation) {
            ConnectionService.this.mAdapter.setAddress((String) ConnectionService.this.mIdByConnection.get(c), address, presentation);
        }

        @Override // android.telecom.Connection.Listener
        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
            ConnectionService.this.mAdapter.setCallerDisplayName((String) ConnectionService.this.mIdByConnection.get(c), callerDisplayName, presentation);
        }

        @Override // android.telecom.Connection.Listener
        public void onDestroyed(Connection c) {
            ConnectionService.this.removeConnection(c);
        }

        @Override // android.telecom.Connection.Listener
        public void onPostDialWait(Connection c, String remaining) {
            Log.i(this, "Adapter onPostDialWait %s, %s", c, remaining);
            ConnectionService.this.mAdapter.onPostDialWait((String) ConnectionService.this.mIdByConnection.get(c), remaining);
        }

        @Override // android.telecom.Connection.Listener
        public void onPostDialChar(Connection c, char nextChar) {
            Log.i(this, "Adapter onPostDialChar %s, %s", c, Character.valueOf(nextChar));
            ConnectionService.this.mAdapter.onPostDialChar((String) ConnectionService.this.mIdByConnection.get(c), nextChar);
        }

        @Override // android.telecom.Connection.Listener
        public void onRingbackRequested(Connection c, boolean ringback) {
            Log.i(this, "Adapter onRingback %b", Boolean.valueOf(ringback));
            ConnectionService.this.mAdapter.setRingbackRequested((String) ConnectionService.this.mIdByConnection.get(c), ringback);
        }

        @Override // android.telecom.Connection.Listener
        public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
            Log.i(this, "capabilities: parcelableconnection: %s", Connection.capabilitiesToString(capabilities));
            ConnectionService.this.mAdapter.setConnectionCapabilities((String) ConnectionService.this.mIdByConnection.get(c), capabilities);
        }

        @Override // android.telecom.Connection.Listener
        public void onConnectionPropertiesChanged(Connection c, int properties) {
            Log.i(this, "properties: parcelableconnection: %s", Connection.propertiesToString(properties));
            ConnectionService.this.mAdapter.setConnectionProperties((String) ConnectionService.this.mIdByConnection.get(c), properties);
        }

        @Override // android.telecom.Connection.Listener
        public void onVideoProviderChanged(Connection c, Connection.VideoProvider videoProvider) {
            Log.i(this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", c, videoProvider);
            ConnectionService.this.mAdapter.setVideoProvider((String) ConnectionService.this.mIdByConnection.get(c), videoProvider);
        }

        @Override // android.telecom.Connection.Listener
        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
            ConnectionService.this.mAdapter.setIsVoipAudioMode((String) ConnectionService.this.mIdByConnection.get(c), isVoip);
        }

        @Override // android.telecom.Connection.Listener
        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
            ConnectionService.this.mAdapter.setStatusHints((String) ConnectionService.this.mIdByConnection.get(c), statusHints);
        }

        @Override // android.telecom.Connection.Listener
        public void onConferenceablesChanged(Connection connection, List<Conferenceable> conferenceables) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConnection.get(connection), ConnectionService.this.createIdList(conferenceables));
        }

        @Override // android.telecom.Connection.Listener
        public void onConferenceChanged(Connection connection, Conference conference) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                String conferenceId = null;
                if (conference != null) {
                    conferenceId = (String) ConnectionService.this.mIdByConference.get(conference);
                }
                ConnectionService.this.mAdapter.setIsConferenced(id, conferenceId);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onConferenceMergeFailed(Connection connection) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                ConnectionService.this.mAdapter.onConferenceMergeFailed(id);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onExtrasChanged(Connection c, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.putExtras(id, extras);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onExtrasRemoved(Connection c, List<String> keys) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.removeExtras(id, keys);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onConnectionEvent(Connection connection, String event, Bundle extras) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                ConnectionService.this.mAdapter.onConnectionEvent(id, event, extras);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onAudioRouteChanged(Connection c, int audioRoute, String bluetoothAddress) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.setAudioRoute(id, audioRoute, bluetoothAddress);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onSsNotificationData(int type, int code) {
            ConnectionService.this.mSsNotificationType = type;
            ConnectionService.this.mSsNotificationCode = code;
        }

        @Override // android.telecom.Connection.Listener
        public void onRttInitiationSuccess(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttInitiationSuccess(id);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onRttInitiationFailure(Connection c, int reason) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttInitiationFailure(id, reason);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onRttSessionRemotelyTerminated(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRttSessionRemotelyTerminated(id);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onRemoteRttRequest(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onRemoteRttRequest(id);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onPhoneAccountChanged(Connection c, PhoneAccountHandle pHandle) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.onPhoneAccountChanged(id, pHandle);
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onConnectionTimeReset(Connection c) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            if (id != null) {
                ConnectionService.this.mAdapter.resetConnectionTime(id);
            }
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class android.telecom.ConnectionService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 100) {
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    ConnectionService.this.setLocalCallHold((String) args.arg1, args.argi1);
                } finally {
                    args.recycle();
                }
            } else if (i != 101) {
                boolean proceed = false;
                switch (i) {
                    case 1:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args2.arg2, "H.CS.aCSA");
                            ConnectionService.this.mAdapter.addAdapter((IConnectionServiceAdapter) args2.arg1);
                            ConnectionService.this.onAdapterAttached();
                            return;
                        } finally {
                            args2.recycle();
                            Log.endSession();
                        }
                    case 2:
                        SomeArgs args3 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args3.arg4, "H.CS.crCo");
                        try {
                            final PhoneAccountHandle connectionManagerPhoneAccount = (PhoneAccountHandle) args3.arg1;
                            final String id = (String) args3.arg2;
                            final ConnectionRequest request = (ConnectionRequest) args3.arg3;
                            final boolean isIncoming = args3.argi1 == 1;
                            final boolean isUnknown = args3.argi2 == 1;
                            if (!ConnectionService.this.mAreAccountsInitialized) {
                                Log.i(this, "Enqueueing pre-init request %s", id);
                                ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCo.pICR", null) {
                                    /* class android.telecom.ConnectionService.AnonymousClass2.AnonymousClass1 */

                                    @Override // android.telecom.Logging.Runnable
                                    public void loggedRun() {
                                        ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                                    }
                                }.prepare());
                            } else {
                                ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                            }
                            return;
                        } finally {
                            args3.recycle();
                            Log.endSession();
                        }
                    case 3:
                        SomeArgs args4 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args4.arg2, "H.CS.ab");
                        try {
                            ConnectionService.this.abort((String) args4.arg1);
                            return;
                        } finally {
                            args4.recycle();
                            Log.endSession();
                        }
                    case 4:
                        SomeArgs args5 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args5.arg2, "H.CS.an");
                        try {
                            ConnectionService.this.answer((String) args5.arg1);
                            return;
                        } finally {
                            args5.recycle();
                            Log.endSession();
                        }
                    case 5:
                        SomeArgs args6 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args6.arg2, "H.CS.r");
                        try {
                            ConnectionService.this.reject((String) args6.arg1);
                            return;
                        } finally {
                            args6.recycle();
                            Log.endSession();
                        }
                    case 6:
                        SomeArgs args7 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args7.arg2, "H.CS.d");
                        try {
                            ConnectionService.this.disconnect((String) args7.arg1);
                            return;
                        } finally {
                            args7.recycle();
                            Log.endSession();
                        }
                    case 7:
                        SomeArgs args8 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args8.arg2, "H.CS.r");
                        try {
                            ConnectionService.this.hold((String) args8.arg1);
                            return;
                        } finally {
                            args8.recycle();
                            Log.endSession();
                        }
                    case 8:
                        SomeArgs args9 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args9.arg2, "H.CS.u");
                        try {
                            ConnectionService.this.unhold((String) args9.arg1);
                            return;
                        } finally {
                            args9.recycle();
                            Log.endSession();
                        }
                    case 9:
                        SomeArgs args10 = (SomeArgs) msg.obj;
                        Log.continueSession((Session) args10.arg3, "H.CS.cASC");
                        try {
                            ConnectionService.this.onCallAudioStateChanged((String) args10.arg1, new CallAudioState((CallAudioState) args10.arg2));
                            return;
                        } finally {
                            args10.recycle();
                            Log.endSession();
                        }
                    case 10:
                        SomeArgs args11 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args11.arg3, "H.CS.pDT");
                            ConnectionService.this.playDtmfTone((String) args11.arg2, ((Character) args11.arg1).charValue());
                            return;
                        } finally {
                            args11.recycle();
                            Log.endSession();
                        }
                    case 11:
                        SomeArgs args12 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args12.arg2, "H.CS.sDT");
                            ConnectionService.this.stopDtmfTone((String) args12.arg1);
                            return;
                        } finally {
                            args12.recycle();
                            Log.endSession();
                        }
                    case 12:
                        SomeArgs args13 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args13.arg3, "H.CS.c");
                            ConnectionService.this.conference((String) args13.arg1, (String) args13.arg2);
                            return;
                        } finally {
                            args13.recycle();
                            Log.endSession();
                        }
                    case 13:
                        SomeArgs args14 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args14.arg2, "H.CS.sFC");
                            ConnectionService.this.splitFromConference((String) args14.arg1);
                            return;
                        } finally {
                            args14.recycle();
                            Log.endSession();
                        }
                    case 14:
                        SomeArgs args15 = (SomeArgs) msg.obj;
                        try {
                            Log.continueSession((Session) args15.arg2, "H.CS.oPDC");
                            String callId = (String) args15.arg1;
                            if (args15.argi1 == 1) {
                                proceed = true;
                            }
                            ConnectionService.this.onPostDialContinue(callId, proceed);
                            return;
                        } finally {
                            args15.recycle();
                            Log.endSession();
                        }
                    default:
                        switch (i) {
                            case 16:
                                SomeArgs args16 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args16.arg2, "H.CS.rCSA");
                                    ConnectionService.this.mAdapter.removeAdapter((IConnectionServiceAdapter) args16.arg1);
                                    return;
                                } finally {
                                    args16.recycle();
                                    Log.endSession();
                                }
                            case 17:
                                SomeArgs args17 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args17.arg2, "H.CS.anV");
                                try {
                                    ConnectionService.this.answerVideo((String) args17.arg1, args17.argi1);
                                    return;
                                } finally {
                                    args17.recycle();
                                    Log.endSession();
                                }
                            case 18:
                                SomeArgs args18 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args18.arg2, "H.CS.mC");
                                    ConnectionService.this.mergeConference((String) args18.arg1);
                                    return;
                                } finally {
                                    args18.recycle();
                                    Log.endSession();
                                }
                            case 19:
                                SomeArgs args19 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args19.arg2, "H.CS.sC");
                                    ConnectionService.this.swapConference((String) args19.arg1);
                                    return;
                                } finally {
                                    args19.recycle();
                                    Log.endSession();
                                }
                            case 20:
                                SomeArgs args20 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args20.arg3, "H.CS.rWM");
                                try {
                                    ConnectionService.this.reject((String) args20.arg1, (String) args20.arg2);
                                    return;
                                } finally {
                                    args20.recycle();
                                    Log.endSession();
                                }
                            case 21:
                                SomeArgs args21 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args21.arg2, "H.CS.s");
                                try {
                                    ConnectionService.this.silence((String) args21.arg1);
                                    return;
                                } finally {
                                    args21.recycle();
                                    Log.endSession();
                                }
                            case 22:
                                SomeArgs args22 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args22.arg2, "H.CS.pEC");
                                    ConnectionService.this.pullExternalCall((String) args22.arg1);
                                    return;
                                } finally {
                                    args22.recycle();
                                    Log.endSession();
                                }
                            case 23:
                                SomeArgs args23 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args23.arg4, "H.CS.sCE");
                                    ConnectionService.this.sendCallEvent((String) args23.arg1, (String) args23.arg2, (Bundle) args23.arg3);
                                    return;
                                } finally {
                                    args23.recycle();
                                    Log.endSession();
                                }
                            case 24:
                                SomeArgs args24 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args24.arg3, "H.CS.oEC");
                                    ConnectionService.this.handleExtrasChanged((String) args24.arg1, (Bundle) args24.arg2);
                                    return;
                                } finally {
                                    args24.recycle();
                                    Log.endSession();
                                }
                            case 25:
                                SomeArgs args25 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args25.arg3, "H.CS.crCoF");
                                try {
                                    final String id2 = (String) args25.arg1;
                                    final ConnectionRequest request2 = (ConnectionRequest) args25.arg2;
                                    final boolean isIncoming2 = args25.argi1 == 1;
                                    final PhoneAccountHandle connectionMgrPhoneAccount = (PhoneAccountHandle) args25.arg4;
                                    if (!ConnectionService.this.mAreAccountsInitialized) {
                                        Log.i(this, "Enqueueing pre-init request %s", id2);
                                        ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCoF.pICR", null) {
                                            /* class android.telecom.ConnectionService.AnonymousClass2.AnonymousClass3 */

                                            @Override // android.telecom.Logging.Runnable
                                            public void loggedRun() {
                                                ConnectionService.this.createConnectionFailed(connectionMgrPhoneAccount, id2, request2, isIncoming2);
                                            }
                                        }.prepare());
                                    } else {
                                        Log.i(this, "createConnectionFailed %s", id2);
                                        ConnectionService.this.createConnectionFailed(connectionMgrPhoneAccount, id2, request2, isIncoming2);
                                    }
                                    return;
                                } finally {
                                    args25.recycle();
                                    Log.endSession();
                                }
                            case 26:
                                SomeArgs args26 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args26.arg3, "H.CS.+RTT");
                                    ConnectionService.this.startRtt((String) args26.arg1, (Connection.RttTextStream) args26.arg2);
                                    return;
                                } finally {
                                    args26.recycle();
                                    Log.endSession();
                                }
                            case 27:
                                SomeArgs args27 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args27.arg2, "H.CS.-RTT");
                                    ConnectionService.this.stopRtt((String) args27.arg1);
                                    return;
                                } finally {
                                    args27.recycle();
                                    Log.endSession();
                                }
                            case 28:
                                SomeArgs args28 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args28.arg3, "H.CS.rTRUR");
                                    ConnectionService.this.handleRttUpgradeResponse((String) args28.arg1, (Connection.RttTextStream) args28.arg2);
                                    return;
                                } finally {
                                    args28.recycle();
                                    Log.endSession();
                                }
                            case 29:
                                SomeArgs args29 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args29.arg2, "H.CS.crCoC");
                                try {
                                    final String id3 = (String) args29.arg1;
                                    if (!ConnectionService.this.mAreAccountsInitialized) {
                                        Log.i(this, "Enqueueing pre-init request %s", id3);
                                        ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.crCoC.pICR", null) {
                                            /* class android.telecom.ConnectionService.AnonymousClass2.AnonymousClass2 */

                                            @Override // android.telecom.Logging.Runnable
                                            public void loggedRun() {
                                                ConnectionService.this.notifyCreateConnectionComplete(id3);
                                            }
                                        }.prepare());
                                    } else {
                                        ConnectionService.this.notifyCreateConnectionComplete(id3);
                                    }
                                    return;
                                } finally {
                                    args29.recycle();
                                    Log.endSession();
                                }
                            case 30:
                                ConnectionService.this.onConnectionServiceFocusLost();
                                return;
                            case 31:
                                ConnectionService.this.onConnectionServiceFocusGained();
                                return;
                            case 32:
                                SomeArgs args30 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args30.arg3, "H.CS.haF");
                                try {
                                    final String id4 = (String) args30.arg1;
                                    final ConnectionRequest request3 = (ConnectionRequest) args30.arg2;
                                    final int reason = ((Integer) args30.arg4).intValue();
                                    if (!ConnectionService.this.mAreAccountsInitialized) {
                                        Log.i(this, "Enqueueing pre-init request %s", id4);
                                        ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable("H.CS.haF.pICR", null) {
                                            /* class android.telecom.ConnectionService.AnonymousClass2.AnonymousClass4 */

                                            @Override // android.telecom.Logging.Runnable
                                            public void loggedRun() {
                                                ConnectionService.this.handoverFailed(id4, request3, reason);
                                            }
                                        }.prepare());
                                    } else {
                                        Log.i(this, "createConnectionFailed %s", id4);
                                        ConnectionService.this.handoverFailed(id4, request3, reason);
                                    }
                                    return;
                                } finally {
                                    args30.recycle();
                                    Log.endSession();
                                }
                            case 33:
                                SomeArgs args31 = (SomeArgs) msg.obj;
                                try {
                                    Log.continueSession((Session) args31.arg2, "H.CS.hC");
                                    ConnectionService.this.notifyHandoverComplete((String) args31.arg1);
                                    return;
                                } finally {
                                    args31.recycle();
                                    Log.endSession();
                                }
                            case 34:
                                SomeArgs args32 = (SomeArgs) msg.obj;
                                Log.continueSession((Session) args32.arg3, "H.CS.def");
                                try {
                                    ConnectionService.this.deflect((String) args32.arg1, (Uri) args32.arg2);
                                    return;
                                } finally {
                                    args32.recycle();
                                    Log.endSession();
                                }
                            default:
                                return;
                        }
                }
            } else {
                ConnectionService.this.setActiveSubscription((String) msg.obj);
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

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        endAllConnections();
        return super.onUnbind(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createConnection(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
        Connection connection;
        Connection connection2;
        PhoneAccountHandle fromPhoneAccountHandle;
        synchronized (this.mAbnormalAbortCallIds) {
            if (callId != null) {
                if (this.mAbnormalAbortCallIds.contains(callId)) {
                    this.mAbnormalAbortCallIds.remove(callId);
                    return;
                }
            }
        }
        boolean isLegacyHandover = request.getExtras() != null && request.getExtras().getBoolean("android.telecom.extra.IS_HANDOVER", false);
        boolean isHandover = request.getExtras() != null && request.getExtras().getBoolean(TelecomManager.EXTRA_IS_HANDOVER_CONNECTION, false);
        Log.i(this, "createConnection, callManagerAccount: %s, callId: %s, request: %s, isIncoming: %b, isUnknown: %b, isLegacyHandover: %b, isHandover: %b", callManagerAccount, callId, request, Boolean.valueOf(isIncoming), Boolean.valueOf(isUnknown), Boolean.valueOf(isLegacyHandover), Boolean.valueOf(isHandover));
        IVideoProvider iVideoProvider = null;
        if (isHandover) {
            if (request.getExtras() != null) {
                fromPhoneAccountHandle = (PhoneAccountHandle) request.getExtras().getParcelable(TelecomManager.EXTRA_HANDOVER_FROM_PHONE_ACCOUNT);
            } else {
                fromPhoneAccountHandle = null;
            }
            if (!isIncoming) {
                connection = onCreateOutgoingHandoverConnection(fromPhoneAccountHandle, request);
            } else {
                connection = onCreateIncomingHandoverConnection(fromPhoneAccountHandle, request);
            }
        } else {
            if (isUnknown) {
                connection2 = onCreateUnknownConnection(callManagerAccount, request);
            } else if (isIncoming) {
                connection2 = onCreateIncomingConnection(callManagerAccount, request);
            } else {
                connection2 = onCreateOutgoingConnection(callManagerAccount, request);
            }
            connection = connection2;
        }
        Log.i(this, "createConnection, connection: %s", connection);
        if (connection == null) {
            Log.i(this, "createConnection, implementation returned null connection.", new Object[0]);
            connection = Connection.createFailedConnection(new DisconnectCause(1, "IMPL_RETURNED_NULL_CONNECTION"));
        }
        boolean isSelfManaged = (connection.getConnectionProperties() & 128) == 128;
        if (isSelfManaged) {
            connection.setAudioModeIsVoip(true);
        }
        connection.setTelecomCallId(callId);
        if (connection.getState() != 6) {
            addConnection(request.getAccountHandle(), callId, connection);
        }
        Log.v(this, "createConnection, number: %s, state: %s, capabilities: %s, properties: %s", "xxxxxx", Connection.stateToString(connection.getState()), Connection.capabilitiesToString(connection.getConnectionCapabilities()), Connection.propertiesToString(connection.getConnectionProperties()));
        Log.i(this, "createConnection, calling handleCreateConnectionSuccessful %s", callId);
        ConnectionServiceAdapter connectionServiceAdapter = this.mAdapter;
        PhoneAccountHandle accountHandle = getAccountHandle(request, connection);
        int state = connection.getState();
        int connectionCapabilities = connection.getConnectionCapabilities();
        int connectionProperties = connection.getConnectionProperties();
        int supportedAudioRoutes = connection.getSupportedAudioRoutes();
        Uri address = connection.getAddress();
        int addressPresentation = connection.getAddressPresentation();
        String callerDisplayName = connection.getCallerDisplayName();
        int callerDisplayNamePresentation = connection.getCallerDisplayNamePresentation();
        if (connection.getVideoProvider() != null) {
            iVideoProvider = connection.getVideoProvider().getInterface();
        }
        connectionServiceAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(accountHandle, state, connectionCapabilities, connectionProperties, supportedAudioRoutes, address, addressPresentation, callerDisplayName, callerDisplayNamePresentation, iVideoProvider, connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getConnectElapsedTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), createIdList(connection.getConferenceables()), connection.getExtras()));
        if (isIncoming && request.shouldShowIncomingCallUi() && isSelfManaged) {
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
        Log.i(this, "getAccountHandle, return account handle from local, %s", pHandle);
        return pHandle;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createConnectionFailed(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming) {
        Log.i(this, "createConnectionFailed %s", callId);
        if (isIncoming) {
            onCreateIncomingConnectionFailed(callManagerAccount, request);
        } else {
            onCreateOutgoingConnectionFailed(callManagerAccount, request);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handoverFailed(String callId, ConnectionRequest request, int reason) {
        Log.i(this, "handoverFailed %s", callId);
        onHandoverFailed(request, reason);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCreateConnectionComplete(String callId) {
        Log.i(this, "notifyCreateConnectionComplete %s", callId);
        if (callId == null) {
            Log.w(this, "notifyCreateConnectionComplete: callId is null.", new Object[0]);
        } else {
            onCreateConnectionComplete(findConnectionForAction(callId, "notifyCreateConnectionComplete"));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abort(String callId) {
        Log.i(this, "abort %s", callId);
        Connection connection = findConnectionForAction(callId, "abort");
        if (connection.equals(getNullConnection()) && callId != null) {
            synchronized (this.mAbnormalAbortCallIds) {
                this.mAbnormalAbortCallIds.add(callId);
            }
        }
        connection.onAbort();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void answerVideo(String callId, int videoState) {
        Log.i(this, "answerVideo %s", callId);
        findConnectionForAction(callId, "answer").onAnswer(videoState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void answer(String callId) {
        Log.i(this, "answer %s", callId);
        findConnectionForAction(callId, "answer").onAnswer();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deflect(String callId, Uri address) {
        Log.i(this, "deflect %s", callId);
        findConnectionForAction(callId, "deflect").onDeflect(address);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reject(String callId) {
        Log.i(this, "reject %s", callId);
        findConnectionForAction(callId, "reject").onReject();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reject(String callId, String rejectWithMessage) {
        Log.i(this, "reject %s with message", callId);
        findConnectionForAction(callId, "reject").onReject(rejectWithMessage);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void silence(String callId) {
        Log.i(this, "silence %s", callId);
        findConnectionForAction(callId, "silence").onSilence();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnect(String callId) {
        Log.i(this, "disconnect %s", callId);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "disconnect").onDisconnect();
        } else {
            findConnectionForAction(callId, "disconnect").onDisconnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hold(String callId) {
        Log.i(this, "hold %s", callId);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "hold").onHold();
        } else {
            findConnectionForAction(callId, "hold").onHold();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unhold(String callId) {
        Log.i(this, "unhold %s", callId);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "unhold").onUnhold();
        } else {
            findConnectionForAction(callId, "unhold").onUnhold();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
        Log.i(this, "onAudioStateChanged %s %s", callId, callAudioState);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        } else {
            findConnectionForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playDtmfTone(String callId, char digit) {
        Log.i(this, "playDtmfTone %s *", callId);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        } else {
            findConnectionForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDtmfTone(String callId) {
        Log.i(this, "stopDtmfTone %s", callId);
        if (callId == null || !this.mConnectionById.containsKey(callId)) {
            findConferenceForAction(callId, "stopDtmfTone").onStopDtmfTone();
        } else {
            findConnectionForAction(callId, "stopDtmfTone").onStopDtmfTone();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLocalCallHold(String callId, int lchStatus) {
        Log.i(this, "setLocalCallHold %s", callId);
        findConnectionForAction(callId, "setLocalCallHold").setLocalCallHold(lchStatus);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActiveSubscription(String callId) {
        Log.i(this, "setActiveSubscription %s", callId);
        findConnectionForAction(callId, "setActiveSubscription").setActiveSubscription();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void conference(String callId1, String callId2) {
        Log.i(this, "conference %s, %s", callId1, callId2);
        Connection connection2 = findConnectionForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
        Conference conference2 = getNullConference();
        if (connection2 == getNullConnection() && (conference2 = findConferenceForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE)) == getNullConference()) {
            Log.w(this, "Connection2 or Conference2 missing in conference request %s.", callId2);
            return;
        }
        Connection connection1 = findConnectionForAction(callId1, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection1 == getNullConnection()) {
            Conference conference1 = findConferenceForAction(callId1, "addConnection");
            if (conference1 == getNullConference()) {
                Log.w(this, "Connection1 or Conference1 missing in conference request %s.", callId1);
            } else if (connection2 != getNullConnection()) {
                conference1.onMerge(connection2);
            } else {
                Log.wtf(this, "There can only be one conference and an attempt was made to merge two conferences.", new Object[0]);
            }
        } else if (conference2 != getNullConference()) {
            conference2.onMerge(connection1);
        } else {
            onConference(connection1, connection2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void splitFromConference(String callId) {
        Log.i(this, "splitFromConference(%s)", callId);
        Connection connection = findConnectionForAction(callId, "splitFromConference");
        if (connection == getNullConnection()) {
            Log.w(this, "Connection missing in conference request %s.", callId);
            return;
        }
        Conference conference = connection.getConference();
        if (conference != null) {
            conference.onSeparate(connection);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void mergeConference(String callId) {
        Log.i(this, "mergeConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "mergeConference");
        if (conference != null) {
            conference.onMerge();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void swapConference(String callId) {
        Log.i(this, "swapConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "swapConference");
        if (conference != null) {
            conference.onSwap();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pullExternalCall(String callId) {
        Log.i(this, "pullExternalCall(%s)", callId);
        Connection connection = findConnectionForAction(callId, "pullExternalCall");
        if (connection != null) {
            connection.onPullExternalCall();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCallEvent(String callId, String event, Bundle extras) {
        Log.i(this, "sendCallEvent(%s, %s)", callId, event);
        Connection connection = findConnectionForAction(callId, "sendCallEvent");
        if (connection != null) {
            connection.onCallEvent(event, extras);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHandoverComplete(String callId) {
        Log.i(this, "notifyHandoverComplete(%s)", callId);
        Connection connection = findConnectionForAction(callId, "notifyHandoverComplete");
        if (connection != null) {
            connection.onHandoverComplete();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleExtrasChanged(String callId, Bundle extras) {
        Log.i(this, "handleExtrasChanged(%s, %s)", callId, extras);
        if (callId != null) {
            if (this.mConnectionById.containsKey(callId)) {
                findConnectionForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
            } else if (this.mConferenceById.containsKey(callId)) {
                findConferenceForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startRtt(String callId, Connection.RttTextStream rttTextStream) {
        Log.i(this, "startRtt(%s)", callId);
        if (callId != null) {
            if (this.mConnectionById.containsKey(callId)) {
                findConnectionForAction(callId, "startRtt").onStartRtt(rttTextStream);
            } else if (this.mConferenceById.containsKey(callId)) {
                Log.w(this, "startRtt called on a conference.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopRtt(String callId) {
        Log.i(this, "stopRtt(%s)", callId);
        if (callId != null) {
            if (this.mConnectionById.containsKey(callId)) {
                findConnectionForAction(callId, "stopRtt").onStopRtt();
            } else if (this.mConferenceById.containsKey(callId)) {
                Log.w(this, "stopRtt called on a conference.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0010: APUT  
      (r0v1 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.lang.Boolean : 0x000c: INVOKE  (r3v1 java.lang.Boolean) = (r3v0 boolean) type: STATIC call: java.lang.Boolean.valueOf(boolean):java.lang.Boolean)
     */
    /* access modifiers changed from: public */
    private void handleRttUpgradeResponse(String callId, Connection.RttTextStream rttTextStream) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = Boolean.valueOf(rttTextStream == null);
        Log.i(this, "handleRttUpgradeResponse(%s, %s)", objArr);
        if (callId != null) {
            if (this.mConnectionById.containsKey(callId)) {
                findConnectionForAction(callId, "handleRttUpgradeResponse").handleRttUpgradeResponse(rttTextStream);
            } else if (this.mConferenceById.containsKey(callId)) {
                Log.w(this, "handleRttUpgradeResponse called on a conference.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPostDialContinue(String callId, boolean proceed) {
        Log.i(this, "onPostDialContinue(%s)", callId);
        findConnectionForAction(callId, "stopDtmfTone").onPostDialContinue(proceed);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAdapterAttached() {
        if (!this.mAreAccountsInitialized) {
            this.mAdapter.queryRemoteConnectionServices(new RemoteServiceCallback.Stub() {
                /* class android.telecom.ConnectionService.AnonymousClass5 */

                @Override // com.android.internal.telecom.RemoteServiceCallback
                public void onResult(final List<ComponentName> componentNames, final List<IBinder> services) {
                    ConnectionService.this.mHandler.post(new Runnable("oAA.qRCS.oR", null) {
                        /* class android.telecom.ConnectionService.AnonymousClass5.AnonymousClass1 */

                        @Override // android.telecom.Logging.Runnable
                        public void loggedRun() {
                            int i = 0;
                            while (i < componentNames.size() && i < services.size()) {
                                ConnectionService.this.mRemoteConnectionManager.addConnectionService((ComponentName) componentNames.get(i), IConnectionService.Stub.asInterface((IBinder) services.get(i)));
                                i++;
                            }
                            ConnectionService.this.onAccountsInitialized();
                            Log.i(this, "remote connection services found: " + services, new Object[0]);
                        }
                    }.prepare());
                }

                @Override // com.android.internal.telecom.RemoteServiceCallback
                public void onError() {
                    ConnectionService.this.mHandler.post(new Runnable("oAA.qRCS.oE", null) {
                        /* class android.telecom.ConnectionService.AnonymousClass5.AnonymousClass2 */

                        @Override // android.telecom.Logging.Runnable
                        public void loggedRun() {
                            ConnectionService.this.mAreAccountsInitialized = true;
                        }
                    }.prepare());
                }
            }, getOpPackageName());
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
        Log.i(this, "addConference: conference=%s", conference);
        String id = addConferenceInternal(conference);
        if (id != null) {
            List<String> connectionIds = new ArrayList<>(2);
            for (Connection connection : conference.getConnections()) {
                if (this.mIdByConnection.containsKey(connection)) {
                    connectionIds.add(this.mIdByConnection.get(connection));
                }
            }
            conference.setTelecomCallId(id);
            this.mAdapter.addConferenceCall(id, new ParcelableConference(conference.getPhoneAccountHandle(), conference.getState(), conference.getConnectionCapabilities(), conference.getConnectionProperties(), connectionIds, conference.getVideoProvider() == null ? null : conference.getVideoProvider().getInterface(), conference.getVideoState(), conference.getConnectTimeMillis(), conference.getConnectionStartElapsedRealTime(), conference.getStatusHints(), conference.getExtras(), conference.getAddress(), conference.getAddressPresentation(), conference.getCallerDisplayName(), conference.getCallerDisplayNamePresentation()));
            this.mAdapter.setVideoProvider(id, conference.getVideoProvider());
            this.mAdapter.setVideoState(id, conference.getVideoState());
            for (Connection connection2 : conference.getConnections()) {
                String connectionId = this.mIdByConnection.get(connection2);
                if (connectionId != null) {
                    this.mAdapter.setIsConferenced(connectionId, id);
                }
            }
            onConferenceAdded(conference);
        }
    }

    public final void addExistingConnection(PhoneAccountHandle phoneAccountHandle, Connection connection) {
        addExistingConnection(phoneAccountHandle, connection, null);
    }

    public final void connectionServiceFocusReleased() {
        this.mAdapter.onConnectionServiceFocusReleased();
    }

    public final void addExistingConnection(PhoneAccountHandle phoneAccountHandle, Connection connection, Conference conference) {
        String conferenceId;
        String id = addExistingConnectionInternal(phoneAccountHandle, connection);
        if (id != null) {
            List<String> emptyList = new ArrayList<>(0);
            if (conference != null) {
                conferenceId = this.mIdByConference.get(conference);
            } else {
                conferenceId = null;
            }
            this.mAdapter.addExistingConnection(id, new ParcelableConnection(phoneAccountHandle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getSupportedAudioRoutes(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getConnectElapsedTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), emptyList, connection.getExtras(), conferenceId, connection.getCallDirection()));
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

    public Connection onCreateOutgoingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        return null;
    }

    public Connection onCreateIncomingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        return null;
    }

    public void onHandoverFailed(ConnectionRequest request, int error) {
    }

    public Connection onCreateUnknownConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void onConference(Connection connection1, Connection connection2) {
    }

    public void onConnectionAdded(Connection connection) {
    }

    public void onConnectionRemoved(Connection connection) {
    }

    public void onConferenceAdded(Conference conference) {
    }

    public void onConferenceRemoved(Conference conference) {
    }

    public void onRemoteConferenceAdded(RemoteConference conference) {
    }

    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
    }

    public void onConnectionServiceFocusLost() {
    }

    public void onConnectionServiceFocusGained() {
    }

    public boolean containsConference(Conference conference) {
        if (conference == null) {
            return false;
        }
        return this.mIdByConference.containsKey(conference);
    }

    /* access modifiers changed from: package-private */
    public void addRemoteConference(RemoteConference remoteConference) {
        onRemoteConferenceAdded(remoteConference);
    }

    /* access modifiers changed from: package-private */
    public void addRemoteExistingConnection(RemoteConnection remoteConnection) {
        onRemoteExistingConnectionAdded(remoteConnection);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            Log.i(this, "addExistingConnectionInternal - conn %s reusing original id %s", connection.getTelecomCallId(), id);
        } else if (handle == null) {
            id = UUID.randomUUID().toString();
        } else {
            id = handle.getComponentName().getClassName() + "@" + getNextCallId();
        }
        addConnection(handle, id, connection);
        return id;
    }

    private void addConnection(PhoneAccountHandle handle, String callId, Connection connection) {
        connection.setTelecomCallId(callId);
        this.mConnectionById.put(callId, connection);
        this.mIdByConnection.put(connection, callId);
        connection.addConnectionListener(this.mConnectionListener);
        connection.setConnectionService(this);
        connection.setPhoneAccountHandle(handle);
        onConnectionAdded(connection);
    }

    /* access modifiers changed from: protected */
    public void removeConnection(Connection connection) {
        connection.unsetConnectionService(this);
        connection.removeConnectionListener(this.mConnectionListener);
        String id = this.mIdByConnection.get(connection);
        if (id != null) {
            this.mConnectionById.remove(id);
            this.mIdByConnection.remove(connection);
            this.mAdapter.removeCall(id);
            onConnectionRemoved(connection);
        }
    }

    private String addConferenceInternal(Conference conference) {
        String originalId = null;
        if (conference.getExtras() != null && conference.getExtras().containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            originalId = conference.getExtras().getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID);
            Log.i(this, "addConferenceInternal: conf %s reusing original id %s", conference.getTelecomCallId(), originalId);
        }
        if (this.mIdByConference.containsKey(conference)) {
            Log.w(this, "Re-adding an existing conference: %s.", conference);
            return null;
        }
        String id = originalId == null ? UUID.randomUUID().toString() : originalId;
        this.mConferenceById.put(id, conference);
        this.mIdByConference.put(conference, id);
        conference.addListener(this.mConferenceListener);
        return id;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeConference(Conference conference) {
        if (this.mIdByConference.containsKey(conference)) {
            conference.removeListener(this.mConferenceListener);
            String id = this.mIdByConference.get(conference);
            this.mConferenceById.remove(id);
            this.mIdByConference.remove(conference);
            this.mAdapter.removeCall(id);
            onConferenceRemoved(conference);
        }
    }

    private Connection findConnectionForAction(String callId, String action) {
        if (callId != null && this.mConnectionById.containsKey(callId)) {
            return this.mConnectionById.get(callId);
        }
        Log.w(this, "%s - Cannot find Connection %s", action, callId);
        return getNullConnection();
    }

    static synchronized Connection getNullConnection() {
        Connection connection;
        synchronized (ConnectionService.class) {
            if (sNullConnection == null) {
                sNullConnection = new Connection() {
                    /* class android.telecom.ConnectionService.AnonymousClass6 */
                };
            }
            connection = sNullConnection;
        }
        return connection;
    }

    private Conference findConferenceForAction(String conferenceId, String action) {
        if (conferenceId != null && this.mConferenceById.containsKey(conferenceId)) {
            return this.mConferenceById.get(conferenceId);
        }
        Log.w(this, "%s - Cannot find conference %s", action, conferenceId);
        return getNullConference();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> createConnectionIdList(List<Connection> connections) {
        List<String> ids = new ArrayList<>();
        for (Connection c : connections) {
            if (this.mIdByConnection.containsKey(c)) {
                ids.add(this.mIdByConnection.get(c));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> createIdList(List<Conferenceable> conferenceables) {
        List<String> ids = new ArrayList<>();
        for (Conferenceable c : conferenceables) {
            if (c instanceof Connection) {
                Connection connection = (Connection) c;
                if (this.mIdByConnection.containsKey(connection)) {
                    ids.add(this.mIdByConnection.get(connection));
                }
            } else if (c instanceof Conference) {
                Conference conference = (Conference) c;
                if (this.mIdByConference.containsKey(conference)) {
                    ids.add(this.mIdByConference.get(conference));
                }
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private Conference getNullConference() {
        if (this.sNullConference == null) {
            this.sNullConference = new Conference(null) {
                /* class android.telecom.ConnectionService.AnonymousClass7 */
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
