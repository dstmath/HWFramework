package android.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.VideoProfile.CameraCapabilities;
import android.util.ArraySet;
import android.view.Surface;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.IVideoProvider.Stub;
import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Connection extends Conferenceable {
    public static final int CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO = 8388608;
    public static final int CAPABILITY_CAN_PAUSE_VIDEO = 1048576;
    public static final int CAPABILITY_CAN_PULL_CALL = 16777216;
    public static final int CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION = 4194304;
    public static final int CAPABILITY_CAN_UPGRADE_TO_VIDEO = 524288;
    public static final int CAPABILITY_CONFERENCE_HAS_NO_CHILDREN = 2097152;
    public static final int CAPABILITY_DISCONNECT_FROM_CONFERENCE = 8192;
    public static final int CAPABILITY_HOLD = 1;
    public static final int CAPABILITY_MANAGE_CONFERENCE = 128;
    public static final int CAPABILITY_MERGE_CONFERENCE = 4;
    public static final int CAPABILITY_MUTE = 64;
    public static final int CAPABILITY_RESPOND_VIA_TEXT = 32;
    public static final int CAPABILITY_SEPARATE_FROM_CONFERENCE = 4096;
    public static final int CAPABILITY_SPEED_UP_MT_AUDIO = 262144;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL = 768;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_RX = 256;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_TX = 512;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL = 3072;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_RX = 1024;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_TX = 2048;
    public static final int CAPABILITY_SUPPORT_HOLD = 2;
    public static final int CAPABILITY_SWAP_CONFERENCE = 8;
    public static final int CAPABILITY_UNUSED = 16;
    public static final int CAPABILITY_UNUSED_2 = 16384;
    public static final int CAPABILITY_UNUSED_3 = 32768;
    public static final int CAPABILITY_UNUSED_4 = 65536;
    public static final int CAPABILITY_UNUSED_5 = 131072;
    public static final int CAPABILITY_VOICE_PRIVACY = 67108864;
    public static final String EVENT_CALL_PULL_FAILED = "android.telecom.event.CALL_PULL_FAILED";
    public static final String EVENT_ON_HOLD_TONE_END = "android.telecom.event.ON_HOLD_TONE_END";
    public static final String EVENT_ON_HOLD_TONE_START = "android.telecom.event.ON_HOLD_TONE_START";
    public static final String EXTRA_CALL_SUBJECT = "android.telecom.extra.CALL_SUBJECT";
    public static final String EXTRA_CHILD_ADDRESS = "android.telecom.extra.CHILD_ADDRESS";
    public static final String EXTRA_LAST_FORWARDED_NUMBER = "android.telecom.extra.LAST_FORWARDED_NUMBER";
    private static final boolean PII_DEBUG = false;
    public static final int PROPERTY_GENERIC_CONFERENCE = 2;
    public static final int PROPERTY_HIGH_DEF_AUDIO = 4;
    public static final int PROPERTY_IS_EXTERNAL_CALL = 16;
    public static final int PROPERTY_SHOW_CALLBACK_NUMBER = 1;
    public static final int PROPERTY_WIFI = 8;
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_DIALING = 3;
    public static final int STATE_DISCONNECTED = 6;
    public static final int STATE_HOLDING = 5;
    public static final int STATE_INITIALIZING = 0;
    public static final int STATE_NEW = 1;
    public static final int STATE_PULLING_CALL = 7;
    public static final int STATE_RINGING = 2;
    private Uri mAddress;
    private int mAddressPresentation;
    private boolean mAudioModeIsVoip;
    private CallAudioState mCallAudioState;
    private String mCallerDisplayName;
    private int mCallerDisplayNamePresentation;
    private Conference mConference;
    private final android.telecom.Conference.Listener mConferenceDeathListener;
    private final List<Conferenceable> mConferenceables;
    private long mConnectTimeMillis;
    private int mConnectionCapabilities;
    private final Listener mConnectionDeathListener;
    private int mConnectionProperties;
    private ConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private Bundle mExtras;
    private final Object mExtrasLock;
    private final Set<Listener> mListeners;
    private PhoneAccountHandle mPhoneAccountHandle;
    private Set<String> mPreviousExtraKeys;
    private boolean mRingbackRequested;
    private int mState;
    private StatusHints mStatusHints;
    private String mTelecomCallId;
    private final List<Conferenceable> mUnmodifiableConferenceables;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    public static abstract class Listener {
        public void onStateChanged(Connection c, int state) {
        }

        public void onAddressChanged(Connection c, Uri newAddress, int presentation) {
        }

        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
        }

        public void onVideoStateChanged(Connection c, int videoState) {
        }

        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
        }

        public void onSsNotificationData(int type, int code) {
        }

        public void onPostDialWait(Connection c, String remaining) {
        }

        public void onPostDialChar(Connection c, char nextChar) {
        }

        public void onRingbackRequested(Connection c, boolean ringback) {
        }

        public void onDestroyed(Connection c) {
        }

        public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
        }

        public void onConnectionPropertiesChanged(Connection c, int properties) {
        }

        public void onVideoProviderChanged(Connection c, VideoProvider videoProvider) {
        }

        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
        }

        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
        }

        public void onConferenceablesChanged(Connection c, List<Conferenceable> list) {
        }

        public void onConferenceChanged(Connection c, Conference conference) {
        }

        public void onConferenceParticipantsChanged(Connection c, List<ConferenceParticipant> list) {
        }

        public void onConferenceStarted() {
        }

        public void onConferenceMergeFailed(Connection c) {
        }

        public void onExtrasChanged(Connection c, Bundle extras) {
        }

        public void onExtrasRemoved(Connection c, List<String> list) {
        }

        public void onConnectionEvent(Connection c, String event, Bundle extras) {
        }

        public void onConnectionEvent(Connection c, String event) {
        }

        public void onPhoneAccountChanged(Connection c, PhoneAccountHandle pHandle) {
        }
    }

    private static class FailureSignalingConnection extends Connection {
        private boolean mImmutable;

        public FailureSignalingConnection(DisconnectCause disconnectCause) {
            this.mImmutable = Connection.PII_DEBUG;
            setDisconnected(disconnectCause);
            this.mImmutable = true;
        }

        public void checkImmutable() {
            if (this.mImmutable) {
                throw new UnsupportedOperationException("Connection is immutable");
            }
        }
    }

    public static abstract class VideoProvider {
        private static final int MSG_ADD_VIDEO_CALLBACK = 1;
        private static final int MSG_REMOVE_VIDEO_CALLBACK = 12;
        private static final int MSG_REQUEST_CAMERA_CAPABILITIES = 9;
        private static final int MSG_REQUEST_CONNECTION_DATA_USAGE = 10;
        private static final int MSG_SEND_SESSION_MODIFY_REQUEST = 7;
        private static final int MSG_SEND_SESSION_MODIFY_RESPONSE = 8;
        private static final int MSG_SET_CAMERA = 2;
        private static final int MSG_SET_DEVICE_ORIENTATION = 5;
        private static final int MSG_SET_DISPLAY_SURFACE = 4;
        private static final int MSG_SET_PAUSE_IMAGE = 11;
        private static final int MSG_SET_PREVIEW_SURFACE = 3;
        private static final int MSG_SET_ZOOM = 6;
        public static final int SESSION_EVENT_CAMERA_FAILURE = 5;
        public static final int SESSION_EVENT_CAMERA_READY = 6;
        public static final int SESSION_EVENT_RX_PAUSE = 1;
        public static final int SESSION_EVENT_RX_RESUME = 2;
        public static final int SESSION_EVENT_TX_START = 3;
        public static final int SESSION_EVENT_TX_STOP = 4;
        public static final int SESSION_MODIFY_REQUEST_FAIL = 2;
        public static final int SESSION_MODIFY_REQUEST_INVALID = 3;
        public static final int SESSION_MODIFY_REQUEST_REJECTED_BY_REMOTE = 5;
        public static final int SESSION_MODIFY_REQUEST_SUCCESS = 1;
        public static final int SESSION_MODIFY_REQUEST_TIMED_OUT = 4;
        private final VideoProviderBinder mBinder;
        private VideoProviderHandler mMessageHandler;
        private ConcurrentHashMap<IBinder, IVideoCallback> mVideoCallbacks;

        private final class VideoProviderBinder extends Stub {
            private VideoProviderBinder() {
            }

            public void addVideoCallback(IBinder videoCallbackBinder) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_MODIFY_REQUEST_SUCCESS, videoCallbackBinder).sendToTarget();
            }

            public void removeVideoCallback(IBinder videoCallbackBinder) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_REMOVE_VIDEO_CALLBACK, videoCallbackBinder).sendToTarget();
            }

            public void setCamera(String cameraId) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_MODIFY_REQUEST_FAIL, cameraId).sendToTarget();
            }

            public void setPreviewSurface(Surface surface) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_MODIFY_REQUEST_INVALID, surface).sendToTarget();
            }

            public void setDisplaySurface(Surface surface) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_MODIFY_REQUEST_TIMED_OUT, surface).sendToTarget();
            }

            public void setDeviceOrientation(int rotation) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_MODIFY_REQUEST_REJECTED_BY_REMOTE, rotation, Connection.STATE_INITIALIZING).sendToTarget();
            }

            public void setZoom(float value) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.SESSION_EVENT_CAMERA_READY, Float.valueOf(value)).sendToTarget();
            }

            public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
                Object[] objArr = new Object[VideoProvider.SESSION_MODIFY_REQUEST_SUCCESS];
                objArr[Connection.STATE_INITIALIZING] = Integer.valueOf(Binder.getCallingUid());
                Log.d((Object) this, "sendSessionModifyRequest CallingUid: %d", objArr);
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = fromProfile;
                args.arg2 = toProfile;
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_SEND_SESSION_MODIFY_REQUEST, args).sendToTarget();
            }

            public void sendSessionModifyResponse(VideoProfile responseProfile) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_SEND_SESSION_MODIFY_RESPONSE, responseProfile).sendToTarget();
            }

            public void requestCameraCapabilities() {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_REQUEST_CAMERA_CAPABILITIES).sendToTarget();
            }

            public void requestCallDataUsage() {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_REQUEST_CONNECTION_DATA_USAGE).sendToTarget();
            }

            public void setPauseImage(Uri uri) {
                VideoProvider.this.mMessageHandler.obtainMessage(VideoProvider.MSG_SET_PAUSE_IMAGE, uri).sendToTarget();
            }
        }

        private final class VideoProviderHandler extends Handler {
            public VideoProviderHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                IBinder binder;
                IVideoCallback callback;
                switch (msg.what) {
                    case VideoProvider.SESSION_MODIFY_REQUEST_SUCCESS /*1*/:
                        binder = msg.obj;
                        callback = IVideoCallback.Stub.asInterface((IBinder) msg.obj);
                        if (callback == null) {
                            Log.w((Object) this, "addVideoProvider - skipped; callback is null.", new Object[Connection.STATE_INITIALIZING]);
                        } else if (VideoProvider.this.mVideoCallbacks.containsKey(binder)) {
                            Log.i((Object) this, "addVideoProvider - skipped; already present.", new Object[Connection.STATE_INITIALIZING]);
                        } else {
                            VideoProvider.this.mVideoCallbacks.put(binder, callback);
                        }
                    case VideoProvider.SESSION_MODIFY_REQUEST_FAIL /*2*/:
                        VideoProvider.this.onSetCamera((String) msg.obj);
                    case VideoProvider.SESSION_MODIFY_REQUEST_INVALID /*3*/:
                        VideoProvider.this.onSetPreviewSurface((Surface) msg.obj);
                    case VideoProvider.SESSION_MODIFY_REQUEST_TIMED_OUT /*4*/:
                        VideoProvider.this.onSetDisplaySurface((Surface) msg.obj);
                    case VideoProvider.SESSION_MODIFY_REQUEST_REJECTED_BY_REMOTE /*5*/:
                        VideoProvider.this.onSetDeviceOrientation(msg.arg1);
                    case VideoProvider.SESSION_EVENT_CAMERA_READY /*6*/:
                        VideoProvider.this.onSetZoom(((Float) msg.obj).floatValue());
                    case VideoProvider.MSG_SEND_SESSION_MODIFY_REQUEST /*7*/:
                        SomeArgs args = msg.obj;
                        try {
                            VideoProvider.this.onSendSessionModifyRequest((VideoProfile) args.arg1, (VideoProfile) args.arg2);
                        } finally {
                            args.recycle();
                        }
                    case VideoProvider.MSG_SEND_SESSION_MODIFY_RESPONSE /*8*/:
                        VideoProvider.this.onSendSessionModifyResponse((VideoProfile) msg.obj);
                    case VideoProvider.MSG_REQUEST_CAMERA_CAPABILITIES /*9*/:
                        VideoProvider.this.onRequestCameraCapabilities();
                    case VideoProvider.MSG_REQUEST_CONNECTION_DATA_USAGE /*10*/:
                        VideoProvider.this.onRequestConnectionDataUsage();
                    case VideoProvider.MSG_SET_PAUSE_IMAGE /*11*/:
                        VideoProvider.this.onSetPauseImage((Uri) msg.obj);
                    case VideoProvider.MSG_REMOVE_VIDEO_CALLBACK /*12*/:
                        binder = (IBinder) msg.obj;
                        callback = IVideoCallback.Stub.asInterface((IBinder) msg.obj);
                        if (VideoProvider.this.mVideoCallbacks.containsKey(binder)) {
                            VideoProvider.this.mVideoCallbacks.remove(binder);
                        } else {
                            Log.i((Object) this, "removeVideoProvider - skipped; not present.", new Object[Connection.STATE_INITIALIZING]);
                        }
                    default:
                }
            }
        }

        public abstract void onRequestCameraCapabilities();

        public abstract void onRequestConnectionDataUsage();

        public abstract void onSendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2);

        public abstract void onSendSessionModifyResponse(VideoProfile videoProfile);

        public abstract void onSetCamera(String str);

        public abstract void onSetDeviceOrientation(int i);

        public abstract void onSetDisplaySurface(Surface surface);

        public abstract void onSetPauseImage(Uri uri);

        public abstract void onSetPreviewSurface(Surface surface);

        public abstract void onSetZoom(float f);

        public VideoProvider() {
            this.mVideoCallbacks = new ConcurrentHashMap(MSG_SEND_SESSION_MODIFY_RESPONSE, 0.9f, SESSION_MODIFY_REQUEST_SUCCESS);
            this.mBinder = new VideoProviderBinder();
            this.mMessageHandler = new VideoProviderHandler(Looper.getMainLooper());
        }

        public VideoProvider(Looper looper) {
            this.mVideoCallbacks = new ConcurrentHashMap(MSG_SEND_SESSION_MODIFY_RESPONSE, 0.9f, SESSION_MODIFY_REQUEST_SUCCESS);
            this.mBinder = new VideoProviderBinder();
            this.mMessageHandler = new VideoProviderHandler(looper);
        }

        public final IVideoProvider getInterface() {
            return this.mBinder;
        }

        public void receiveSessionModifyRequest(VideoProfile videoProfile) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.receiveSessionModifyRequest(videoProfile);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "receiveSessionModifyRequest callback failed", objArr);
                    }
                }
            }
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.receiveSessionModifyResponse(status, requestedProfile, responseProfile);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "receiveSessionModifyResponse callback failed", objArr);
                    }
                }
            }
        }

        public void handleCallSessionEvent(int event) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.handleCallSessionEvent(event);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "handleCallSessionEvent callback failed", objArr);
                    }
                }
            }
        }

        public void changePeerDimensions(int width, int height) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changePeerDimensions(width, height);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "changePeerDimensions callback failed", objArr);
                    }
                }
            }
        }

        public void setCallDataUsage(long dataUsage) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changeCallDataUsage(dataUsage);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "setCallDataUsage callback failed", objArr);
                    }
                }
            }
        }

        public void changeCallDataUsage(long dataUsage) {
            setCallDataUsage(dataUsage);
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changeCameraCapabilities(cameraCapabilities);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "changeCameraCapabilities callback failed", objArr);
                    }
                }
            }
        }

        public void changeVideoQuality(int videoQuality) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changeVideoQuality(videoQuality);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[SESSION_MODIFY_REQUEST_SUCCESS];
                        objArr[Connection.STATE_INITIALIZING] = ignored;
                        Log.w((Object) this, "changeVideoQuality callback failed", objArr);
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.Connection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.Connection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.<clinit>():void");
    }

    public void removeCapability(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.Connection.removeCapability(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.removeCapability(int):void");
    }

    public static boolean can(int capabilities, int capability) {
        return (capabilities & capability) == capability ? true : PII_DEBUG;
    }

    public boolean can(int capability) {
        return can(this.mConnectionCapabilities, capability);
    }

    public void addCapability(int capability) {
        this.mConnectionCapabilities |= capability;
    }

    public static String capabilitiesToString(int capabilities) {
        StringBuilder builder = new StringBuilder();
        builder.append("[Capabilities:");
        if (can(capabilities, STATE_NEW)) {
            builder.append(" CAPABILITY_HOLD");
        }
        if (can(capabilities, STATE_RINGING)) {
            builder.append(" CAPABILITY_SUPPORT_HOLD");
        }
        if (can(capabilities, STATE_ACTIVE)) {
            builder.append(" CAPABILITY_MERGE_CONFERENCE");
        }
        if (can(capabilities, PROPERTY_WIFI)) {
            builder.append(" CAPABILITY_SWAP_CONFERENCE");
        }
        if (can(capabilities, CAPABILITY_RESPOND_VIA_TEXT)) {
            builder.append(" CAPABILITY_RESPOND_VIA_TEXT");
        }
        if (can(capabilities, CAPABILITY_MUTE)) {
            builder.append(" CAPABILITY_MUTE");
        }
        if (can(capabilities, CAPABILITY_MANAGE_CONFERENCE)) {
            builder.append(" CAPABILITY_MANAGE_CONFERENCE");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_RX)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_RX");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_TX)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_TX");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_RX)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_RX");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_TX)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_TX");
        }
        if (can(capabilities, CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL)) {
            builder.append(" CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL");
        }
        if (can(capabilities, CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO)) {
            builder.append(" CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO");
        }
        if (can(capabilities, CAPABILITY_SPEED_UP_MT_AUDIO)) {
            builder.append(" CAPABILITY_SPEED_UP_MT_AUDIO");
        }
        if (can(capabilities, CAPABILITY_CAN_UPGRADE_TO_VIDEO)) {
            builder.append(" CAPABILITY_CAN_UPGRADE_TO_VIDEO");
        }
        if (can(capabilities, CAPABILITY_CAN_PAUSE_VIDEO)) {
            builder.append(" CAPABILITY_CAN_PAUSE_VIDEO");
        }
        if (can(capabilities, CAPABILITY_CONFERENCE_HAS_NO_CHILDREN)) {
            builder.append(" CAPABILITY_SINGLE_PARTY_CONFERENCE");
        }
        if (can(capabilities, CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION)) {
            builder.append(" CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION");
        }
        if (can(capabilities, CAPABILITY_CAN_PULL_CALL)) {
            builder.append(" CAPABILITY_CAN_PULL_CALL");
        }
        builder.append("]");
        return builder.toString();
    }

    public static String propertiesToString(int properties) {
        StringBuilder builder = new StringBuilder();
        builder.append("[Properties:");
        if (can(properties, STATE_NEW)) {
            builder.append(" PROPERTY_SHOW_CALLBACK_NUMBER");
        }
        if (can(properties, STATE_ACTIVE)) {
            builder.append(" PROPERTY_HIGH_DEF_AUDIO");
        }
        if (can(properties, PROPERTY_WIFI)) {
            builder.append(" PROPERTY_WIFI");
        }
        if (can(properties, STATE_RINGING)) {
            builder.append(" PROPERTY_GENERIC_CONFERENCE");
        }
        if (can(properties, PROPERTY_IS_EXTERNAL_CALL)) {
            builder.append(" PROPERTY_IS_EXTERNAL_CALL");
        }
        builder.append("]");
        return builder.toString();
    }

    public Connection() {
        this.mConnectionDeathListener = new Listener() {
            public void onDestroyed(Connection c) {
                if (Connection.this.mConferenceables.remove(c)) {
                    Connection.this.fireOnConferenceableConnectionsChanged();
                }
            }
        };
        this.mConferenceDeathListener = new android.telecom.Conference.Listener() {
            public void onDestroyed(Conference c) {
                if (Connection.this.mConferenceables.remove(c)) {
                    Connection.this.fireOnConferenceableConnectionsChanged();
                }
            }
        };
        this.mListeners = Collections.newSetFromMap(new ConcurrentHashMap(PROPERTY_WIFI, 0.9f, STATE_NEW));
        this.mConferenceables = new ArrayList();
        this.mUnmodifiableConferenceables = Collections.unmodifiableList(this.mConferenceables);
        this.mState = STATE_NEW;
        this.mRingbackRequested = PII_DEBUG;
        this.mConnectTimeMillis = 0;
        this.mExtrasLock = new Object();
        this.mPhoneAccountHandle = null;
    }

    public final String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public final Uri getAddress() {
        return this.mAddress;
    }

    public final int getAddressPresentation() {
        return this.mAddressPresentation;
    }

    public final String getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public final int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public final int getState() {
        return this.mState;
    }

    public final int getVideoState() {
        return this.mVideoState;
    }

    @Deprecated
    public final AudioState getAudioState() {
        if (this.mCallAudioState == null) {
            return null;
        }
        return new AudioState(this.mCallAudioState);
    }

    public final CallAudioState getCallAudioState() {
        return this.mCallAudioState;
    }

    public final Conference getConference() {
        return this.mConference;
    }

    public final boolean isRingbackRequested() {
        return this.mRingbackRequested;
    }

    public final boolean getAudioModeIsVoip() {
        return this.mAudioModeIsVoip;
    }

    public final long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

    public final StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public final Bundle getExtras() {
        Bundle bundle = null;
        synchronized (this.mExtrasLock) {
            if (this.mExtras != null) {
                bundle = new Bundle(this.mExtras);
            }
        }
        return bundle;
    }

    public final Connection addConnectionListener(Listener l) {
        this.mListeners.add(l);
        return this;
    }

    public final Connection removeConnectionListener(Listener l) {
        if (l != null) {
            this.mListeners.remove(l);
        }
        return this;
    }

    public final DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public void setTelecomCallId(String callId) {
        this.mTelecomCallId = callId;
    }

    final void setCallAudioState(CallAudioState state) {
        checkImmutable();
        Object[] objArr = new Object[STATE_NEW];
        objArr[STATE_INITIALIZING] = state;
        Log.d((Object) this, "setAudioState %s", objArr);
        this.mCallAudioState = state;
        onAudioStateChanged(getAudioState());
        onCallAudioStateChanged(state);
    }

    public static String stateToString(int state) {
        switch (state) {
            case STATE_INITIALIZING /*0*/:
                return "INITIALIZING";
            case STATE_NEW /*1*/:
                return "NEW";
            case STATE_RINGING /*2*/:
                return "RINGING";
            case STATE_DIALING /*3*/:
                return "DIALING";
            case STATE_ACTIVE /*4*/:
                return "ACTIVE";
            case STATE_HOLDING /*5*/:
                return "HOLDING";
            case STATE_DISCONNECTED /*6*/:
                return "DISCONNECTED";
            default:
                Object[] objArr = new Object[STATE_NEW];
                objArr[STATE_INITIALIZING] = Integer.valueOf(state);
                Log.wtf((Object) Connection.class, "Unknown state %d", objArr);
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public final int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public final int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public final void setAddress(Uri address, int presentation) {
        checkImmutable();
        Object[] objArr = new Object[STATE_NEW];
        objArr[STATE_INITIALIZING] = "xxxxxx";
        Log.d((Object) this, "setAddress %s", objArr);
        this.mAddress = address;
        this.mAddressPresentation = presentation;
        for (Listener l : this.mListeners) {
            l.onAddressChanged(this, address, presentation);
        }
    }

    public final void setCallerDisplayName(String callerDisplayName, int presentation) {
        checkImmutable();
        Object[] objArr = new Object[STATE_NEW];
        objArr[STATE_INITIALIZING] = callerDisplayName;
        Log.d((Object) this, "setCallerDisplayName %s", objArr);
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = presentation;
        for (Listener l : this.mListeners) {
            l.onCallerDisplayNameChanged(this, callerDisplayName, presentation);
        }
    }

    public final void setVideoState(int videoState) {
        checkImmutable();
        Object[] objArr = new Object[STATE_NEW];
        objArr[STATE_INITIALIZING] = Integer.valueOf(videoState);
        Log.d((Object) this, "setVideoState %d", objArr);
        this.mVideoState = videoState;
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this, this.mVideoState);
        }
    }

    public final void setActive() {
        checkImmutable();
        setRingbackRequested(PII_DEBUG);
        setState(STATE_ACTIVE);
    }

    public final void setRinging() {
        checkImmutable();
        setState(STATE_RINGING);
    }

    public final void setInitializing() {
        checkImmutable();
        setState(STATE_INITIALIZING);
    }

    public final void setInitialized() {
        checkImmutable();
        setState(STATE_NEW);
    }

    public final void setDialing() {
        checkImmutable();
        setState(STATE_DIALING);
    }

    public final void setOnHold() {
        checkImmutable();
        setState(STATE_HOLDING);
    }

    public final void setVideoProvider(VideoProvider videoProvider) {
        checkImmutable();
        this.mVideoProvider = videoProvider;
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this, videoProvider);
        }
    }

    public final VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public final void setDisconnected(DisconnectCause disconnectCause) {
        checkImmutable();
        this.mDisconnectCause = disconnectCause;
        setState(STATE_DISCONNECTED);
        Object[] objArr = new Object[STATE_NEW];
        objArr[STATE_INITIALIZING] = disconnectCause;
        Log.d((Object) this, "Disconnected with cause %s", objArr);
        for (Listener l : this.mListeners) {
            l.onDisconnected(this, disconnectCause);
        }
    }

    public final void setSsNotificationData(int type, int code) {
        Log.d((Object) this, "setSsNotificationData = " + type + " " + code, new Object[STATE_INITIALIZING]);
        for (Listener l : this.mListeners) {
            l.onSsNotificationData(type, code);
        }
    }

    public final void setPostDialWait(String remaining) {
        checkImmutable();
        for (Listener l : this.mListeners) {
            l.onPostDialWait(this, remaining);
        }
    }

    public final void setNextPostDialChar(char nextChar) {
        checkImmutable();
        for (Listener l : this.mListeners) {
            l.onPostDialChar(this, nextChar);
        }
    }

    public final void setRingbackRequested(boolean ringback) {
        checkImmutable();
        if (this.mRingbackRequested != ringback) {
            this.mRingbackRequested = ringback;
            for (Listener l : this.mListeners) {
                l.onRingbackRequested(this, ringback);
            }
        }
    }

    public final void setConnectionCapabilities(int connectionCapabilities) {
        checkImmutable();
        if (this.mConnectionCapabilities != connectionCapabilities) {
            this.mConnectionCapabilities = connectionCapabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this, this.mConnectionCapabilities);
            }
        }
    }

    public final void setConnectionProperties(int connectionProperties) {
        checkImmutable();
        if (this.mConnectionProperties != connectionProperties) {
            this.mConnectionProperties = connectionProperties;
            for (Listener l : this.mListeners) {
                l.onConnectionPropertiesChanged(this, this.mConnectionProperties);
            }
        }
    }

    public final void destroy() {
        for (Listener l : this.mListeners) {
            l.onDestroyed(this);
        }
    }

    public final void setAudioModeIsVoip(boolean isVoip) {
        checkImmutable();
        this.mAudioModeIsVoip = isVoip;
        for (Listener l : this.mListeners) {
            l.onAudioModeIsVoipChanged(this, isVoip);
        }
    }

    public final void setConnectTimeMillis(long connectTimeMillis) {
        this.mConnectTimeMillis = connectTimeMillis;
    }

    public final void setStatusHints(StatusHints statusHints) {
        checkImmutable();
        this.mStatusHints = statusHints;
        for (Listener l : this.mListeners) {
            l.onStatusHintsChanged(this, statusHints);
        }
    }

    public final void setConferenceableConnections(List<Connection> conferenceableConnections) {
        checkImmutable();
        clearConferenceableList();
        for (Connection c : conferenceableConnections) {
            if (!this.mConferenceables.contains(c)) {
                c.addConnectionListener(this.mConnectionDeathListener);
                this.mConferenceables.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final void setConferenceables(List<Conferenceable> conferenceables) {
        clearConferenceableList();
        for (Conferenceable c : conferenceables) {
            if (!this.mConferenceables.contains(c)) {
                if (c instanceof Connection) {
                    ((Connection) c).addConnectionListener(this.mConnectionDeathListener);
                } else if (c instanceof Conference) {
                    ((Conference) c).addListener(this.mConferenceDeathListener);
                }
                this.mConferenceables.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final List<Conferenceable> getConferenceables() {
        return this.mUnmodifiableConferenceables;
    }

    public void setPhoneAccountHandle(PhoneAccountHandle pHandle) {
        this.mPhoneAccountHandle = pHandle;
        for (Listener l : this.mListeners) {
            l.onPhoneAccountChanged(this, pHandle);
        }
    }

    public PhoneAccountHandle getPhoneAccountHandle() {
        return this.mPhoneAccountHandle;
    }

    public final void setConnectionService(ConnectionService connectionService) {
        checkImmutable();
        if (this.mConnectionService != null) {
            Log.e((Object) this, new Exception(), "Trying to set ConnectionService on a connection which is already associated with another ConnectionService.", new Object[STATE_INITIALIZING]);
        } else {
            this.mConnectionService = connectionService;
        }
    }

    public final void unsetConnectionService(ConnectionService connectionService) {
        if (this.mConnectionService != connectionService) {
            Log.e((Object) this, new Exception(), "Trying to remove ConnectionService from a Connection that does not belong to the ConnectionService.", new Object[STATE_INITIALIZING]);
        } else {
            this.mConnectionService = null;
        }
    }

    public final ConnectionService getConnectionService() {
        return this.mConnectionService;
    }

    public final boolean setConference(Conference conference) {
        checkImmutable();
        if (this.mConference != null) {
            return PII_DEBUG;
        }
        this.mConference = conference;
        if (this.mConnectionService != null && this.mConnectionService.containsConference(conference)) {
            fireConferenceChanged();
        }
        return true;
    }

    public final void resetConference() {
        if (this.mConference != null) {
            Log.d((Object) this, "Conference reset", new Object[STATE_INITIALIZING]);
            this.mConference = null;
            fireConferenceChanged();
        }
    }

    public final void setExtras(Bundle extras) {
        checkImmutable();
        putExtras(extras);
        if (this.mPreviousExtraKeys != null) {
            List<String> toRemove = new ArrayList();
            for (String oldKey : this.mPreviousExtraKeys) {
                if (extras == null || !extras.containsKey(oldKey)) {
                    toRemove.add(oldKey);
                }
            }
            if (!toRemove.isEmpty()) {
                removeExtras(toRemove);
            }
        }
        if (this.mPreviousExtraKeys == null) {
            this.mPreviousExtraKeys = new ArraySet();
        }
        this.mPreviousExtraKeys.clear();
        if (extras != null) {
            this.mPreviousExtraKeys.addAll(extras.keySet());
        }
    }

    public final void putExtras(Bundle extras) {
        checkImmutable();
        if (extras != null) {
            Bundle listenerExtras;
            synchronized (this.mExtrasLock) {
                if (this.mExtras == null) {
                    this.mExtras = new Bundle();
                }
                this.mExtras.putAll(extras);
                listenerExtras = new Bundle(this.mExtras);
            }
            for (Listener l : this.mListeners) {
                l.onExtrasChanged(this, new Bundle(listenerExtras));
            }
        }
    }

    public final void putExtra(String key, boolean value) {
        Bundle newExtras = new Bundle();
        newExtras.putBoolean(key, value);
        putExtras(newExtras);
    }

    public final void putExtra(String key, int value) {
        Bundle newExtras = new Bundle();
        newExtras.putInt(key, value);
        putExtras(newExtras);
    }

    public final void putExtra(String key, String value) {
        Bundle newExtras = new Bundle();
        newExtras.putString(key, value);
        putExtras(newExtras);
    }

    public final void removeExtras(List<String> keys) {
        synchronized (this.mExtrasLock) {
            if (this.mExtras != null) {
                for (String key : keys) {
                    this.mExtras.remove(key);
                }
            }
        }
        List<String> unmodifiableKeys = Collections.unmodifiableList(keys);
        for (Listener l : this.mListeners) {
            l.onExtrasRemoved(this, unmodifiableKeys);
        }
    }

    @Deprecated
    public void onAudioStateChanged(AudioState state) {
    }

    public void onCallAudioStateChanged(CallAudioState state) {
    }

    public void onStateChanged(int state) {
    }

    public void onPlayDtmfTone(char c) {
    }

    public void onStopDtmfTone() {
    }

    public void setLocalCallHold(int lchState) {
    }

    public void setActiveSubscription() {
    }

    public void onDisconnect() {
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
    }

    public void onSeparate() {
    }

    public void onAbort() {
    }

    public void onHold() {
    }

    public void onUnhold() {
    }

    public void onAnswer(int videoState) {
    }

    public void onAnswer() {
        onAnswer(STATE_INITIALIZING);
    }

    public void onReject() {
    }

    public void onReject(String replyMessage) {
    }

    public void onSilence() {
    }

    public void onPostDialContinue(boolean proceed) {
    }

    public void onPullExternalCall() {
    }

    public void onCallEvent(String event, Bundle extras) {
    }

    public void onExtrasChanged(Bundle extras) {
    }

    static String toLogSafePhoneNumber(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = STATE_INITIALIZING; i < number.length(); i += STATE_NEW) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.') {
                builder.append(c);
            } else {
                builder.append(StateProperty.TARGET_X);
            }
        }
        return builder.toString();
    }

    private void setState(int state) {
        checkImmutable();
        if (this.mState != STATE_DISCONNECTED || this.mState == state) {
            if (this.mState != state) {
                Object[] objArr = new Object[STATE_NEW];
                objArr[STATE_INITIALIZING] = stateToString(state);
                Log.d((Object) this, "setState: %s", objArr);
                this.mState = state;
                onStateChanged(state);
                for (Listener l : this.mListeners) {
                    l.onStateChanged(this, state);
                }
            }
            return;
        }
        Log.d((Object) this, "Connection already DISCONNECTED; cannot transition out of this state.", new Object[STATE_INITIALIZING]);
    }

    public static Connection createFailedConnection(DisconnectCause disconnectCause) {
        return new FailureSignalingConnection(disconnectCause);
    }

    public void checkImmutable() {
    }

    public static Connection createCanceledConnection() {
        return new FailureSignalingConnection(new DisconnectCause(STATE_ACTIVE));
    }

    private final void fireOnConferenceableConnectionsChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceablesChanged(this, getConferenceables());
        }
    }

    private final void fireConferenceChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceChanged(this, this.mConference);
        }
    }

    private final void clearConferenceableList() {
        for (Conferenceable c : this.mConferenceables) {
            if (c instanceof Connection) {
                ((Connection) c).removeConnectionListener(this.mConnectionDeathListener);
            } else if (c instanceof Conference) {
                ((Conference) c).removeListener(this.mConferenceDeathListener);
            }
        }
        this.mConferenceables.clear();
    }

    final void handleExtrasChanged(Bundle extras) {
        Bundle bundle = null;
        synchronized (this.mExtrasLock) {
            this.mExtras = extras;
            if (this.mExtras != null) {
                bundle = new Bundle(this.mExtras);
            }
        }
        onExtrasChanged(bundle);
    }

    protected final void notifyConferenceMergeFailed() {
        for (Listener l : this.mListeners) {
            l.onConferenceMergeFailed(this);
        }
    }

    protected final void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        for (Listener l : this.mListeners) {
            l.onConferenceParticipantsChanged(this, conferenceParticipants);
        }
    }

    protected void notifyConferenceStarted() {
        for (Listener l : this.mListeners) {
            l.onConferenceStarted();
        }
    }

    public void sendConnectionEvent(String event, Bundle extras) {
        for (Listener l : this.mListeners) {
            l.onConnectionEvent(this, event, extras);
        }
    }
}
