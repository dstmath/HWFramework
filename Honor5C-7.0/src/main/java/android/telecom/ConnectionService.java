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
import android.telecom.Conference.Listener;
import android.telecom.Connection.VideoProvider;
import com.android.ims.ImsCallProfile;
import com.android.internal.logging.MetricsProto.MetricsEvent;
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
    private static final int MSG_DISCONNECT = 6;
    private static final int MSG_HOLD = 7;
    private static final int MSG_MERGE_CONFERENCE = 18;
    private static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 9;
    private static final int MSG_ON_EXTRAS_CHANGED = 24;
    private static final int MSG_ON_POST_DIAL_CONTINUE = 14;
    private static final int MSG_PLAY_DTMF_TONE = 10;
    private static final int MSG_PULL_EXTERNAL_CALL = 22;
    private static final int MSG_REJECT = 5;
    private static final int MSG_REJECT_WITH_MESSAGE = 20;
    private static final int MSG_REMOVE_CONNECTION_SERVICE_ADAPTER = 16;
    private static final int MSG_SEND_CALL_EVENT = 23;
    private static final int MSG_SET_ACTIVE_SUB = 101;
    private static final int MSG_SET_LOCAL_HOLD = 100;
    private static final int MSG_SILENCE = 21;
    private static final int MSG_SPLIT_FROM_CONFERENCE = 13;
    private static final int MSG_STOP_DTMF_TONE = 11;
    private static final int MSG_SWAP_CONFERENCE = 19;
    private static final int MSG_UNHOLD = 8;
    private static final boolean PII_DEBUG = false;
    public static final String SERVICE_INTERFACE = "android.telecom.ConnectionService";
    private static Connection sNullConnection;
    private final ConnectionServiceAdapter mAdapter;
    private boolean mAreAccountsInitialized;
    private final IBinder mBinder;
    private final Map<String, Conference> mConferenceById;
    private final Listener mConferenceListener;
    private final Map<String, Connection> mConnectionById;
    private final Connection.Listener mConnectionListener;
    private final Handler mHandler;
    private int mId;
    private final Map<Conference, String> mIdByConference;
    private final Map<Connection, String> mIdByConnection;
    private Object mIdSyncRoot;
    private final List<Runnable> mPreInitializationConnectionRequests;
    private final RemoteConnectionManager mRemoteConnectionManager;
    private int mSsNotificationCode;
    private int mSsNotificationType;
    private Conference sNullConference;

    /* renamed from: android.telecom.ConnectionService.2 */
    class AnonymousClass2 extends Handler {

        /* renamed from: android.telecom.ConnectionService.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ PhoneAccountHandle val$connectionManagerPhoneAccount;
            final /* synthetic */ String val$id;
            final /* synthetic */ boolean val$isIncoming;
            final /* synthetic */ boolean val$isUnknown;
            final /* synthetic */ ConnectionRequest val$request;

            AnonymousClass1(PhoneAccountHandle val$connectionManagerPhoneAccount, String val$id, ConnectionRequest val$request, boolean val$isIncoming, boolean val$isUnknown) {
                this.val$connectionManagerPhoneAccount = val$connectionManagerPhoneAccount;
                this.val$id = val$id;
                this.val$request = val$request;
                this.val$isIncoming = val$isIncoming;
                this.val$isUnknown = val$isUnknown;
            }

            public void run() {
                ConnectionService.this.createConnection(this.val$connectionManagerPhoneAccount, this.val$id, this.val$request, this.val$isIncoming, this.val$isUnknown);
            }
        }

        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            switch (msg.what) {
                case ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER /*1*/:
                    ConnectionService.this.mAdapter.addAdapter((IConnectionServiceAdapter) msg.obj);
                    ConnectionService.this.onAdapterAttached();
                case ConnectionService.MSG_CREATE_CONNECTION /*2*/:
                    args = msg.obj;
                    try {
                        PhoneAccountHandle connectionManagerPhoneAccount = args.arg1;
                        String id = args.arg2;
                        ConnectionRequest request = args.arg3;
                        boolean isIncoming = args.argi1 == ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER ? true : ConnectionService.PII_DEBUG;
                        boolean isUnknown = args.argi2 == ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER ? true : ConnectionService.PII_DEBUG;
                        if (ConnectionService.this.mAreAccountsInitialized) {
                            ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                        } else {
                            Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                            objArr[0] = id;
                            Log.d((Object) this, "Enqueueing pre-init request %s", objArr);
                            ConnectionService.this.mPreInitializationConnectionRequests.add(new AnonymousClass1(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown));
                        }
                        args.recycle();
                    } catch (Throwable th) {
                        args.recycle();
                    }
                case ConnectionService.MSG_ABORT /*3*/:
                    ConnectionService.this.abort((String) msg.obj);
                case ConnectionService.MSG_ANSWER /*4*/:
                    ConnectionService.this.answer((String) msg.obj);
                case ConnectionService.MSG_REJECT /*5*/:
                    ConnectionService.this.reject((String) msg.obj);
                case ConnectionService.MSG_DISCONNECT /*6*/:
                    ConnectionService.this.disconnect((String) msg.obj);
                case ConnectionService.MSG_HOLD /*7*/:
                    ConnectionService.this.hold((String) msg.obj);
                case ConnectionService.MSG_UNHOLD /*8*/:
                    ConnectionService.this.unhold((String) msg.obj);
                case ConnectionService.MSG_ON_CALL_AUDIO_STATE_CHANGED /*9*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        String callId = (String) args.arg1;
                        CallAudioState audioState = args.arg2;
                        if (audioState != null) {
                            ConnectionService.this.onCallAudioStateChanged(callId, new CallAudioState(audioState));
                        }
                        args.recycle();
                    } catch (Throwable th2) {
                        args.recycle();
                    }
                case ConnectionService.MSG_PLAY_DTMF_TONE /*10*/:
                    ConnectionService.this.playDtmfTone((String) msg.obj, (char) msg.arg1);
                case ConnectionService.MSG_STOP_DTMF_TONE /*11*/:
                    ConnectionService.this.stopDtmfTone((String) msg.obj);
                case ConnectionService.MSG_CONFERENCE /*12*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.conference(args.arg1, args.arg2);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_SPLIT_FROM_CONFERENCE /*13*/:
                    ConnectionService.this.splitFromConference((String) msg.obj);
                case ConnectionService.MSG_ON_POST_DIAL_CONTINUE /*14*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.onPostDialContinue((String) args.arg1, args.argi1 == ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER ? true : ConnectionService.PII_DEBUG);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_REMOVE_CONNECTION_SERVICE_ADAPTER /*16*/:
                    ConnectionService.this.mAdapter.removeAdapter((IConnectionServiceAdapter) msg.obj);
                case ConnectionService.MSG_ANSWER_VIDEO /*17*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.answerVideo(args.arg1, args.argi1);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_MERGE_CONFERENCE /*18*/:
                    ConnectionService.this.mergeConference((String) msg.obj);
                case ConnectionService.MSG_SWAP_CONFERENCE /*19*/:
                    ConnectionService.this.swapConference((String) msg.obj);
                case ConnectionService.MSG_REJECT_WITH_MESSAGE /*20*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.reject((String) args.arg1, (String) args.arg2);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_SILENCE /*21*/:
                    ConnectionService.this.silence((String) msg.obj);
                case ConnectionService.MSG_PULL_EXTERNAL_CALL /*22*/:
                    ConnectionService.this.pullExternalCall((String) msg.obj);
                case ConnectionService.MSG_SEND_CALL_EVENT /*23*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.sendCallEvent((String) args.arg1, args.arg2, args.arg3);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_ON_EXTRAS_CHANGED /*24*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.handleExtrasChanged((String) args.arg1, (Bundle) args.arg2);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_SET_LOCAL_HOLD /*100*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.setLocalCallHold((String) args.arg1, args.argi1);
                    } finally {
                        args.recycle();
                    }
                case ConnectionService.MSG_SET_ACTIVE_SUB /*101*/:
                    ConnectionService.this.setActiveSubscription((String) msg.obj);
                default:
            }
        }
    }

    /* renamed from: android.telecom.ConnectionService.7 */
    class AnonymousClass7 extends Conference {
        AnonymousClass7(PhoneAccountHandle $anonymous0) {
            super($anonymous0);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.ConnectionService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.ConnectionService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.<clinit>():void");
    }

    public ConnectionService() {
        this.mConnectionById = new ConcurrentHashMap();
        this.mIdByConnection = new ConcurrentHashMap();
        this.mConferenceById = new ConcurrentHashMap();
        this.mIdByConference = new ConcurrentHashMap();
        this.mRemoteConnectionManager = new RemoteConnectionManager(this);
        this.mPreInitializationConnectionRequests = new ArrayList();
        this.mAdapter = new ConnectionServiceAdapter();
        this.mSsNotificationType = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        this.mSsNotificationCode = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        this.mAreAccountsInitialized = PII_DEBUG;
        this.mIdSyncRoot = new Object();
        this.mId = 0;
        this.mBinder = new Stub() {
            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER, adapter).sendToTarget();
            }

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_REMOVE_CONNECTION_SERVICE_ADAPTER, adapter).sendToTarget();
            }

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String id, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
                int i;
                int i2 = ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER;
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = connectionManagerPhoneAccount;
                args.arg2 = id;
                args.arg3 = request;
                if (isIncoming) {
                    i = ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER;
                } else {
                    i = 0;
                }
                args.argi1 = i;
                if (!isUnknown) {
                    i2 = 0;
                }
                args.argi2 = i2;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_CREATE_CONNECTION, args).sendToTarget();
            }

            public void abort(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ABORT, callId).sendToTarget();
            }

            public void answerVideo(String callId, int videoState) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.argi1 = videoState;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ANSWER_VIDEO, args).sendToTarget();
            }

            public void answer(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ANSWER, callId).sendToTarget();
            }

            public void reject(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_REJECT, callId).sendToTarget();
            }

            public void rejectWithMessage(String callId, String message) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = message;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_REJECT_WITH_MESSAGE, args).sendToTarget();
            }

            public void silence(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SILENCE, callId).sendToTarget();
            }

            public void disconnect(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_DISCONNECT, callId).sendToTarget();
            }

            public void hold(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_HOLD, callId).sendToTarget();
            }

            public void unhold(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_UNHOLD, callId).sendToTarget();
            }

            public void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = callAudioState;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ON_CALL_AUDIO_STATE_CHANGED, args).sendToTarget();
            }

            public void playDtmfTone(String callId, char digit) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_PLAY_DTMF_TONE, digit, 0, callId).sendToTarget();
            }

            public void stopDtmfTone(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_STOP_DTMF_TONE, callId).sendToTarget();
            }

            public void setLocalCallHold(String callId, int lchState) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.argi1 = lchState;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SET_LOCAL_HOLD, args).sendToTarget();
            }

            public void setActiveSubscription(String callId) {
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = callId;
                Log.i((Object) this, "setActiveSubscription %s", objArr);
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SET_ACTIVE_SUB, callId).sendToTarget();
            }

            public void conference(String callId1, String callId2) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId1;
                args.arg2 = callId2;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_CONFERENCE, args).sendToTarget();
            }

            public void splitFromConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SPLIT_FROM_CONFERENCE, callId).sendToTarget();
            }

            public void mergeConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_MERGE_CONFERENCE, callId).sendToTarget();
            }

            public void swapConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SWAP_CONFERENCE, callId).sendToTarget();
            }

            public void onPostDialContinue(String callId, boolean proceed) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.argi1 = proceed ? ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER : 0;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ON_POST_DIAL_CONTINUE, args).sendToTarget();
            }

            public void pullExternalCall(String callId) {
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_PULL_EXTERNAL_CALL, callId).sendToTarget();
            }

            public void sendCallEvent(String callId, String event, Bundle extras) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = event;
                args.arg3 = extras;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_SEND_CALL_EVENT, args).sendToTarget();
            }

            public void onExtrasChanged(String callId, Bundle extras) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = extras;
                ConnectionService.this.mHandler.obtainMessage(ConnectionService.MSG_ON_EXTRAS_CHANGED, args).sendToTarget();
            }
        };
        this.mHandler = new AnonymousClass2(Looper.getMainLooper());
        this.mConferenceListener = new Listener() {
            public void onStateChanged(Conference conference, int oldState, int newState) {
                String id = (String) ConnectionService.this.mIdByConference.get(conference);
                switch (newState) {
                    case ConnectionService.MSG_ANSWER /*4*/:
                        ConnectionService.this.mAdapter.setActive(id);
                    case ConnectionService.MSG_REJECT /*5*/:
                        ConnectionService.this.mAdapter.setOnHold(id);
                    default:
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
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Connection.capabilitiesToString(connectionCapabilities);
                Log.d((Object) this, "call capabilities: conference: %s", objArr);
                ConnectionService.this.mAdapter.setConnectionCapabilities(id, connectionCapabilities);
            }

            public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
                String id = (String) ConnectionService.this.mIdByConference.get(conference);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Connection.propertiesToString(connectionProperties);
                Log.d((Object) this, "call capabilities: conference: %s", objArr);
                ConnectionService.this.mAdapter.setConnectionProperties(id, connectionProperties);
            }

            public void onVideoStateChanged(Conference c, int videoState) {
                String id = (String) ConnectionService.this.mIdByConference.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Integer.valueOf(videoState);
                Log.d((Object) this, "onVideoStateChanged set video state %d", objArr);
                ConnectionService.this.mAdapter.setVideoState(id, videoState);
            }

            public void onVideoProviderChanged(Conference c, VideoProvider videoProvider) {
                String id = (String) ConnectionService.this.mIdByConference.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = c;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = videoProvider;
                Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", objArr);
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
        };
        this.mConnectionListener = new Connection.Listener() {
            public void onStateChanged(Connection c, int state) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = id;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = Connection.stateToString(state);
                Log.d((Object) this, "Adapter set state %s %s", objArr);
                switch (state) {
                    case ConnectionService.MSG_CREATE_CONNECTION /*2*/:
                        ConnectionService.this.mAdapter.setRinging(id);
                    case ConnectionService.MSG_ABORT /*3*/:
                        ConnectionService.this.mAdapter.setDialing(id);
                    case ConnectionService.MSG_ANSWER /*4*/:
                        ConnectionService.this.mAdapter.setActive(id);
                    case ConnectionService.MSG_REJECT /*5*/:
                        ConnectionService.this.mAdapter.setOnHold(id);
                    default:
                }
            }

            public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = disconnectCause;
                Log.d((Object) this, "Adapter set disconnected %s", objArr);
                if (ConnectionService.this.mSsNotificationType == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE && ConnectionService.this.mSsNotificationCode == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
                    ConnectionService.this.mAdapter.setDisconnected(id, disconnectCause);
                    return;
                }
                ConnectionService.this.mAdapter.setDisconnectedWithSsNotification(id, disconnectCause.getCode(), disconnectCause.getReason(), ConnectionService.this.mSsNotificationType, ConnectionService.this.mSsNotificationCode);
                ConnectionService.this.mSsNotificationType = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
                ConnectionService.this.mSsNotificationCode = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            }

            public void onVideoStateChanged(Connection c, int videoState) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Integer.valueOf(videoState);
                Log.d((Object) this, "Adapter set video state %d", objArr);
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
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = c;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = remaining;
                Log.d((Object) this, "Adapter onPostDialWait %s, %s", objArr);
                ConnectionService.this.mAdapter.onPostDialWait(id, remaining);
            }

            public void onPostDialChar(Connection c, char nextChar) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = c;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = Character.valueOf(nextChar);
                Log.d((Object) this, "Adapter onPostDialChar %s, %s", objArr);
                ConnectionService.this.mAdapter.onPostDialChar(id, nextChar);
            }

            public void onRingbackRequested(Connection c, boolean ringback) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Boolean.valueOf(ringback);
                Log.d((Object) this, "Adapter onRingback %b", objArr);
                ConnectionService.this.mAdapter.setRingbackRequested(id, ringback);
            }

            public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Connection.capabilitiesToString(capabilities);
                Log.d((Object) this, "capabilities: parcelableconnection: %s", objArr);
                ConnectionService.this.mAdapter.setConnectionCapabilities(id, capabilities);
            }

            public void onConnectionPropertiesChanged(Connection c, int properties) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = Connection.propertiesToString(properties);
                Log.d((Object) this, "properties: parcelableconnection: %s", objArr);
                ConnectionService.this.mAdapter.setConnectionProperties(id, properties);
            }

            public void onVideoProviderChanged(Connection c, VideoProvider videoProvider) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = c;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = videoProvider;
                Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", objArr);
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

            public void onSsNotificationData(int type, int code) {
                ConnectionService.this.mSsNotificationType = type;
                ConnectionService.this.mSsNotificationCode = code;
            }

            public void onPhoneAccountChanged(Connection c, PhoneAccountHandle pHandle) {
                String id = (String) ConnectionService.this.mIdByConnection.get(c);
                Object[] objArr = new Object[ConnectionService.MSG_CREATE_CONNECTION];
                objArr[0] = c;
                objArr[ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER] = pHandle;
                Log.i((Object) this, "Adapter onPhoneAccountChanged %s, %s", objArr);
                ConnectionService.this.mAdapter.setPhoneAccountHandle(id, pHandle);
            }
        };
    }

    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        endAllConnections();
        return super.onUnbind(intent);
    }

    private void createConnection(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
        Connection connection;
        Object[] objArr = new Object[MSG_REJECT];
        objArr[0] = callManagerAccount;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = callId;
        objArr[MSG_CREATE_CONNECTION] = request;
        objArr[MSG_ABORT] = Boolean.valueOf(isIncoming);
        objArr[MSG_ANSWER] = Boolean.valueOf(isUnknown);
        Log.d((Object) this, "createConnection, callManagerAccount: %s, callId: %s, request: %s, isIncoming: %b, isUnknown: %b", objArr);
        if (isUnknown) {
            connection = onCreateUnknownConnection(callManagerAccount, request);
        } else if (isIncoming) {
            connection = onCreateIncomingConnection(callManagerAccount, request);
        } else {
            connection = onCreateOutgoingConnection(callManagerAccount, request);
        }
        objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = connection;
        Log.d((Object) this, "createConnection, connection: %s", objArr);
        if (connection == null) {
            connection = Connection.createFailedConnection(new DisconnectCause(MSG_ADD_CONNECTION_SERVICE_ADAPTER));
        }
        connection.setTelecomCallId(callId);
        if (connection.getState() != MSG_DISCONNECT) {
            addConnection(callId, connection);
        }
        objArr = new Object[MSG_ANSWER];
        objArr[0] = "xxxxxx";
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = Connection.stateToString(connection.getState());
        objArr[MSG_CREATE_CONNECTION] = Connection.capabilitiesToString(connection.getConnectionCapabilities());
        objArr[MSG_ABORT] = Connection.propertiesToString(connection.getConnectionProperties());
        Log.v((Object) this, "createConnection, number: %s, state: %s, capabilities: %s, properties: %s", objArr);
        objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "createConnection, calling handleCreateConnectionSuccessful %s", objArr);
        ConnectionServiceAdapter connectionServiceAdapter = this.mAdapter;
        connectionServiceAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(getAccountHandle(request, connection), connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), createIdList(connection.getConferenceables()), connection.getExtras()));
        if (isUnknown) {
            triggerConferenceRecalculate();
        }
    }

    public PhoneAccountHandle getAccountHandle(ConnectionRequest request, Connection connection) {
        PhoneAccountHandle pHandle = connection.getPhoneAccountHandle();
        if (pHandle == null) {
            return request.getAccountHandle();
        }
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = pHandle;
        Log.i((Object) this, "getAccountHandle, return account handle from local, %s", objArr);
        return pHandle;
    }

    private void abort(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "abort %s", objArr);
        findConnectionForAction(callId, "abort").onAbort();
    }

    private void answerVideo(String callId, int videoState) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "answerVideo %s", objArr);
        findConnectionForAction(callId, "answer").onAnswer(videoState);
    }

    private void answer(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "answer %s", objArr);
        findConnectionForAction(callId, "answer").onAnswer();
    }

    private void reject(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "reject %s", objArr);
        findConnectionForAction(callId, "reject").onReject();
    }

    private void reject(String callId, String rejectWithMessage) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "reject %s with message", objArr);
        findConnectionForAction(callId, "reject").onReject(rejectWithMessage);
    }

    private void silence(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "silence %s", objArr);
        findConnectionForAction(callId, "silence").onSilence();
    }

    private void disconnect(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "disconnect %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "disconnect").onDisconnect();
        } else {
            findConferenceForAction(callId, "disconnect").onDisconnect();
        }
    }

    private void hold(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "hold %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "hold").onHold();
        } else {
            findConferenceForAction(callId, "hold").onHold();
        }
    }

    private void unhold(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "unhold %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "unhold").onUnhold();
        } else {
            findConferenceForAction(callId, "unhold").onUnhold();
        }
    }

    private void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = callId;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = callAudioState;
        Log.d((Object) this, "onAudioStateChanged %s %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        } else {
            findConferenceForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        }
    }

    private void playDtmfTone(String callId, char digit) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "playDtmfTone %s *", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        } else {
            findConferenceForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        }
    }

    private void stopDtmfTone(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "stopDtmfTone %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "stopDtmfTone").onStopDtmfTone();
        } else {
            findConferenceForAction(callId, "stopDtmfTone").onStopDtmfTone();
        }
    }

    private void setLocalCallHold(String callId, int lchStatus) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "setLocalCallHold %s", objArr);
        findConnectionForAction(callId, "setLocalCallHold").setLocalCallHold(lchStatus);
    }

    private void setActiveSubscription(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "setActiveSubscription %s", objArr);
        findConnectionForAction(callId, "setActiveSubscription").setActiveSubscription();
    }

    private void conference(String callId1, String callId2) {
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = callId1;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = callId2;
        Log.d((Object) this, "conference %s, %s", objArr);
        Connection connection2 = findConnectionForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
        Conference conference2 = getNullConference();
        if (connection2 == getNullConnection()) {
            conference2 = findConferenceForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
            if (conference2 == getNullConference()) {
                objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = callId2;
                Log.w((Object) this, "Connection2 or Conference2 missing in conference request %s.", objArr);
                return;
            }
        }
        Connection connection1 = findConnectionForAction(callId1, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection1 == getNullConnection()) {
            Conference conference1 = findConferenceForAction(callId1, "addConnection");
            if (conference1 == getNullConference()) {
                objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
                objArr[0] = callId1;
                Log.w((Object) this, "Connection1 or Conference1 missing in conference request %s.", objArr);
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
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "splitFromConference(%s)", objArr);
        Connection connection = findConnectionForAction(callId, "splitFromConference");
        if (connection == getNullConnection()) {
            objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
            objArr[0] = callId;
            Log.w((Object) this, "Connection missing in conference request %s.", objArr);
            return;
        }
        Conference conference = connection.getConference();
        if (conference != null) {
            conference.onSeparate(connection);
        }
    }

    private void mergeConference(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "mergeConference(%s)", objArr);
        Conference conference = findConferenceForAction(callId, "mergeConference");
        if (conference != null) {
            conference.onMerge();
        }
    }

    private void swapConference(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "swapConference(%s)", objArr);
        Conference conference = findConferenceForAction(callId, "swapConference");
        if (conference != null) {
            conference.onSwap();
        }
    }

    private void pullExternalCall(String callId) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "pullExternalCall(%s)", objArr);
        Connection connection = findConnectionForAction(callId, "pullExternalCall");
        if (connection != null) {
            connection.onPullExternalCall();
        }
    }

    private void sendCallEvent(String callId, String event, Bundle extras) {
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = callId;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = event;
        Log.d((Object) this, "sendCallEvent(%s, %s)", objArr);
        Connection connection = findConnectionForAction(callId, "sendCallEvent");
        if (connection != null) {
            connection.onCallEvent(event, extras);
        }
    }

    private void handleExtrasChanged(String callId, Bundle extras) {
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = callId;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = extras;
        Log.d((Object) this, "handleExtrasChanged(%s, %s)", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        } else if (this.mConferenceById.containsKey(callId)) {
            findConferenceForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        }
    }

    private void onPostDialContinue(String callId, boolean proceed) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = callId;
        Log.d((Object) this, "onPostDialContinue(%s)", objArr);
        findConnectionForAction(callId, "stopDtmfTone").onPostDialContinue(proceed);
    }

    private void onAdapterAttached() {
        if (!this.mAreAccountsInitialized) {
            this.mAdapter.queryRemoteConnectionServices(new RemoteServiceCallback.Stub() {

                /* renamed from: android.telecom.ConnectionService.5.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ List val$componentNames;
                    final /* synthetic */ List val$services;

                    AnonymousClass1(List val$componentNames, List val$services) {
                        this.val$componentNames = val$componentNames;
                        this.val$services = val$services;
                    }

                    public void run() {
                        int i = 0;
                        while (i < this.val$componentNames.size() && i < this.val$services.size()) {
                            ConnectionService.this.mRemoteConnectionManager.addConnectionService((ComponentName) this.val$componentNames.get(i), Stub.asInterface((IBinder) this.val$services.get(i)));
                            i += ConnectionService.MSG_ADD_CONNECTION_SERVICE_ADAPTER;
                        }
                        ConnectionService.this.onAccountsInitialized();
                        Log.d((Object) this, "remote connection services found: " + this.val$services, new Object[0]);
                    }
                }

                public void onResult(List<ComponentName> componentNames, List<IBinder> services) {
                    ConnectionService.this.mHandler.post(new AnonymousClass1(componentNames, services));
                }

                public void onError() {
                    ConnectionService.this.mHandler.post(new Runnable() {
                        public void run() {
                            ConnectionService.this.mAreAccountsInitialized = true;
                        }
                    });
                }
            });
        }
    }

    public final RemoteConnection createRemoteIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, true);
    }

    public final RemoteConnection createRemoteOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, PII_DEBUG);
    }

    public final void conferenceRemoteConnections(RemoteConnection remoteConnection1, RemoteConnection remoteConnection2) {
        this.mRemoteConnectionManager.conferenceRemoteConnections(remoteConnection1, remoteConnection2);
    }

    public final void addConference(Conference conference) {
        Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
        objArr[0] = conference;
        Log.d((Object) this, "addConference: conference=%s", objArr);
        String id = addConferenceInternal(conference);
        if (id != null) {
            IVideoProvider iVideoProvider;
            List<String> connectionIds = new ArrayList(MSG_CREATE_CONNECTION);
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
        String id = addExistingConnectionInternal(phoneAccountHandle, connection);
        if (id != null) {
            this.mAdapter.addExistingConnection(id, new ParcelableConnection(phoneAccountHandle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), new ArrayList(0), connection.getExtras()));
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
        if (handle == null) {
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
        if (this.mIdByConference.containsKey(conference)) {
            Object[] objArr = new Object[MSG_ADD_CONNECTION_SERVICE_ADAPTER];
            objArr[0] = conference;
            Log.w((Object) this, "Re-adding an existing conference: %s.", objArr);
        } else if (conference != null) {
            String id = UUID.randomUUID().toString();
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
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = action;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = callId;
        Log.w((Object) this, "%s - Cannot find Connection %s", objArr);
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
        Object[] objArr = new Object[MSG_CREATE_CONNECTION];
        objArr[0] = action;
        objArr[MSG_ADD_CONNECTION_SERVICE_ADAPTER] = conferenceId;
        Log.w((Object) this, "%s - Cannot find conference %s", objArr);
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
            this.sNullConference = new AnonymousClass7(null);
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
            i = this.mId + MSG_ADD_CONNECTION_SERVICE_ADAPTER;
            this.mId = i;
        }
        return i;
    }
}
