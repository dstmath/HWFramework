package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteConnection {
    private Uri mAddress;
    private int mAddressPresentation;
    private final Set<CallbackRecord> mCallbackRecords;
    private String mCallerDisplayName;
    private int mCallerDisplayNamePresentation;
    private RemoteConference mConference;
    private final List<RemoteConnection> mConferenceableConnections;
    private boolean mConnected;
    private int mConnectionCapabilities;
    private final String mConnectionId;
    private int mConnectionProperties;
    private IConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private Bundle mExtras;
    private boolean mIsVoipAudioMode;
    private boolean mRingbackRequested;
    private int mState;
    private StatusHints mStatusHints;
    private final List<RemoteConnection> mUnmodifiableconferenceableConnections;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    /* renamed from: android.telecom.RemoteConnection.10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ VideoProvider val$videoProvider;

        AnonymousClass10(Callback val$callback, RemoteConnection val$connection, VideoProvider val$videoProvider) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$videoProvider = val$videoProvider;
        }

        public void run() {
            this.val$callback.onVideoProviderChanged(this.val$connection, this.val$videoProvider);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ boolean val$isVoip;

        AnonymousClass11(Callback val$callback, RemoteConnection val$connection, boolean val$isVoip) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$isVoip = val$isVoip;
        }

        public void run() {
            this.val$callback.onVoipAudioChanged(this.val$connection, this.val$isVoip);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.12 */
    class AnonymousClass12 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ StatusHints val$statusHints;

        AnonymousClass12(Callback val$callback, RemoteConnection val$connection, StatusHints val$statusHints) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$statusHints = val$statusHints;
        }

        public void run() {
            this.val$callback.onStatusHintsChanged(this.val$connection, this.val$statusHints);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.13 */
    class AnonymousClass13 implements Runnable {
        final /* synthetic */ Uri val$address;
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$presentation;

        AnonymousClass13(Callback val$callback, RemoteConnection val$connection, Uri val$address, int val$presentation) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$address = val$address;
            this.val$presentation = val$presentation;
        }

        public void run() {
            this.val$callback.onAddressChanged(this.val$connection, this.val$address, this.val$presentation);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.14 */
    class AnonymousClass14 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ String val$callerDisplayName;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$presentation;

        AnonymousClass14(Callback val$callback, RemoteConnection val$connection, String val$callerDisplayName, int val$presentation) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$callerDisplayName = val$callerDisplayName;
            this.val$presentation = val$presentation;
        }

        public void run() {
            this.val$callback.onCallerDisplayNameChanged(this.val$connection, this.val$callerDisplayName, this.val$presentation);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.15 */
    class AnonymousClass15 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass15(Callback val$callback, RemoteConnection val$connection) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
        }

        public void run() {
            this.val$callback.onConferenceableConnectionsChanged(this.val$connection, RemoteConnection.this.mUnmodifiableconferenceableConnections);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.16 */
    class AnonymousClass16 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass16(Callback val$callback, RemoteConnection val$connection, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onConferenceChanged(this.val$connection, this.val$conference);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.17 */
    class AnonymousClass17 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass17(Callback val$callback, RemoteConnection val$connection) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
        }

        public void run() {
            this.val$callback.onExtrasChanged(this.val$connection, RemoteConnection.this.mExtras);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.18 */
    class AnonymousClass18 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ String val$event;
        final /* synthetic */ Bundle val$extras;

        AnonymousClass18(Callback val$callback, RemoteConnection val$connection, String val$event, Bundle val$extras) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$event = val$event;
            this.val$extras = val$extras;
        }

        public void run() {
            this.val$callback.onConnectionEvent(this.val$connection, this.val$event, this.val$extras);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$state;

        AnonymousClass1(Callback val$callback, RemoteConnection val$connection, int val$state) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$state = val$state;
        }

        public void run() {
            this.val$callback.onStateChanged(this.val$connection, this.val$state);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ DisconnectCause val$disconnectCause;

        AnonymousClass2(Callback val$callback, RemoteConnection val$connection, DisconnectCause val$disconnectCause) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$disconnectCause = val$disconnectCause;
        }

        public void run() {
            this.val$callback.onDisconnected(this.val$connection, this.val$disconnectCause);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ boolean val$ringback;

        AnonymousClass3(Callback val$callback, RemoteConnection val$connection, boolean val$ringback) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$ringback = val$ringback;
        }

        public void run() {
            this.val$callback.onRingbackRequested(this.val$connection, this.val$ringback);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$connectionCapabilities;

        AnonymousClass4(Callback val$callback, RemoteConnection val$connection, int val$connectionCapabilities) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$connectionCapabilities = val$connectionCapabilities;
        }

        public void run() {
            this.val$callback.onConnectionCapabilitiesChanged(this.val$connection, this.val$connectionCapabilities);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$connectionProperties;

        AnonymousClass5(Callback val$callback, RemoteConnection val$connection, int val$connectionProperties) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$connectionProperties = val$connectionProperties;
        }

        public void run() {
            this.val$callback.onConnectionPropertiesChanged(this.val$connection, this.val$connectionProperties);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass6(Callback val$callback, RemoteConnection val$connection) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
        }

        public void run() {
            this.val$callback.onDestroyed(this.val$connection);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ String val$remainingDigits;

        AnonymousClass7(Callback val$callback, RemoteConnection val$connection, String val$remainingDigits) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$remainingDigits = val$remainingDigits;
        }

        public void run() {
            this.val$callback.onPostDialWait(this.val$connection, this.val$remainingDigits);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ char val$nextChar;

        AnonymousClass8(Callback val$callback, RemoteConnection val$connection, char val$nextChar) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$nextChar = val$nextChar;
        }

        public void run() {
            this.val$callback.onPostDialChar(this.val$connection, this.val$nextChar);
        }
    }

    /* renamed from: android.telecom.RemoteConnection.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConnection val$connection;
        final /* synthetic */ int val$videoState;

        AnonymousClass9(Callback val$callback, RemoteConnection val$connection, int val$videoState) {
            this.val$callback = val$callback;
            this.val$connection = val$connection;
            this.val$videoState = val$videoState;
        }

        public void run() {
            this.val$callback.onVideoStateChanged(this.val$connection, this.val$videoState);
        }
    }

    public static abstract class Callback {
        public void onStateChanged(RemoteConnection connection, int state) {
        }

        public void onDisconnected(RemoteConnection connection, DisconnectCause disconnectCause) {
        }

        public void setDisconnectedWithSsNotification(RemoteConnection connection, int disconnectCause, String disconnectMessage, int type, int code) {
        }

        public void onRingbackRequested(RemoteConnection connection, boolean ringback) {
        }

        public void onConnectionCapabilitiesChanged(RemoteConnection connection, int connectionCapabilities) {
        }

        public void onConnectionPropertiesChanged(RemoteConnection connection, int connectionProperties) {
        }

        public void onPostDialWait(RemoteConnection connection, String remainingPostDialSequence) {
        }

        public void onPostDialChar(RemoteConnection connection, char nextChar) {
        }

        public void onVoipAudioChanged(RemoteConnection connection, boolean isVoip) {
        }

        public void onStatusHintsChanged(RemoteConnection connection, StatusHints statusHints) {
        }

        public void onAddressChanged(RemoteConnection connection, Uri address, int presentation) {
        }

        public void onCallerDisplayNameChanged(RemoteConnection connection, String callerDisplayName, int presentation) {
        }

        public void onVideoStateChanged(RemoteConnection connection, int videoState) {
        }

        public void onDestroyed(RemoteConnection connection) {
        }

        public void onConferenceableConnectionsChanged(RemoteConnection connection, List<RemoteConnection> list) {
        }

        public void onVideoProviderChanged(RemoteConnection connection, VideoProvider videoProvider) {
        }

        public void onConferenceChanged(RemoteConnection connection, RemoteConference conference) {
        }

        public void onExtrasChanged(RemoteConnection connection, Bundle extras) {
        }

        public void onConnectionEvent(RemoteConnection connection, String event, Bundle extras) {
        }

        public void setPhoneAccountHandle(RemoteConnection connection, PhoneAccountHandle pHandle) {
        }
    }

    private static final class CallbackRecord extends Callback {
        private final Callback mCallback;
        private final Handler mHandler;

        public CallbackRecord(Callback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public Callback getCallback() {
            return this.mCallback;
        }

        public Handler getHandler() {
            return this.mHandler;
        }
    }

    public static class VideoProvider {
        private final Set<Callback> mCallbacks;
        private final IVideoCallback mVideoCallbackDelegate;
        private final VideoCallbackServant mVideoCallbackServant;
        private final IVideoProvider mVideoProviderBinder;

        public static abstract class Callback {
            public void onSessionModifyRequestReceived(VideoProvider videoProvider, VideoProfile videoProfile) {
            }

            public void onSessionModifyResponseReceived(VideoProvider videoProvider, int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
            }

            public void onCallSessionEvent(VideoProvider videoProvider, int event) {
            }

            public void onPeerDimensionsChanged(VideoProvider videoProvider, int width, int height) {
            }

            public void onCallDataUsageChanged(VideoProvider videoProvider, long dataUsage) {
            }

            public void onCameraCapabilitiesChanged(VideoProvider videoProvider, CameraCapabilities cameraCapabilities) {
            }

            public void onVideoQualityChanged(VideoProvider videoProvider, int videoQuality) {
            }
        }

        VideoProvider(IVideoProvider videoProviderBinder) {
            this.mVideoCallbackDelegate = new IVideoCallback() {
                public void receiveSessionModifyRequest(VideoProfile videoProfile) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onSessionModifyRequestReceived(VideoProvider.this, videoProfile);
                    }
                }

                public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onSessionModifyResponseReceived(VideoProvider.this, status, requestedProfile, responseProfile);
                    }
                }

                public void handleCallSessionEvent(int event) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onCallSessionEvent(VideoProvider.this, event);
                    }
                }

                public void changePeerDimensions(int width, int height) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onPeerDimensionsChanged(VideoProvider.this, width, height);
                    }
                }

                public void changeCallDataUsage(long dataUsage) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onCallDataUsageChanged(VideoProvider.this, dataUsage);
                    }
                }

                public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onCameraCapabilitiesChanged(VideoProvider.this, cameraCapabilities);
                    }
                }

                public void changeVideoQuality(int videoQuality) {
                    for (Callback l : VideoProvider.this.mCallbacks) {
                        l.onVideoQualityChanged(VideoProvider.this, videoQuality);
                    }
                }

                public IBinder asBinder() {
                    return null;
                }
            };
            this.mVideoCallbackServant = new VideoCallbackServant(this.mVideoCallbackDelegate);
            this.mCallbacks = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
            this.mVideoProviderBinder = videoProviderBinder;
            try {
                this.mVideoProviderBinder.addVideoCallback(this.mVideoCallbackServant.getStub().asBinder());
            } catch (RemoteException e) {
            }
        }

        public void registerCallback(Callback l) {
            this.mCallbacks.add(l);
        }

        public void unregisterCallback(Callback l) {
            this.mCallbacks.remove(l);
        }

        public void setCamera(String cameraId) {
            try {
                this.mVideoProviderBinder.setCamera(cameraId);
            } catch (RemoteException e) {
            }
        }

        public void setPreviewSurface(Surface surface) {
            try {
                this.mVideoProviderBinder.setPreviewSurface(surface);
            } catch (RemoteException e) {
            }
        }

        public void setDisplaySurface(Surface surface) {
            try {
                this.mVideoProviderBinder.setDisplaySurface(surface);
            } catch (RemoteException e) {
            }
        }

        public void setDeviceOrientation(int rotation) {
            try {
                this.mVideoProviderBinder.setDeviceOrientation(rotation);
            } catch (RemoteException e) {
            }
        }

        public void setZoom(float value) {
            try {
                this.mVideoProviderBinder.setZoom(value);
            } catch (RemoteException e) {
            }
        }

        public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
            try {
                this.mVideoProviderBinder.sendSessionModifyRequest(fromProfile, toProfile);
            } catch (RemoteException e) {
            }
        }

        public void sendSessionModifyResponse(VideoProfile responseProfile) {
            try {
                this.mVideoProviderBinder.sendSessionModifyResponse(responseProfile);
            } catch (RemoteException e) {
            }
        }

        public void requestCameraCapabilities() {
            try {
                this.mVideoProviderBinder.requestCameraCapabilities();
            } catch (RemoteException e) {
            }
        }

        public void requestCallDataUsage() {
            try {
                this.mVideoProviderBinder.requestCallDataUsage();
            } catch (RemoteException e) {
            }
        }

        public void setPauseImage(Uri uri) {
            try {
                this.mVideoProviderBinder.setPauseImage(uri);
            } catch (RemoteException e) {
            }
        }
    }

    RemoteConnection(String id, IConnectionService connectionService, ConnectionRequest request) {
        this.mCallbackRecords = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableconferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectionId = id;
        this.mConnectionService = connectionService;
        this.mConnected = true;
        this.mState = 0;
    }

    RemoteConnection(String callId, IConnectionService connectionService, ParcelableConnection connection) {
        this.mCallbackRecords = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableconferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectionId = callId;
        this.mConnectionService = connectionService;
        this.mConnected = true;
        this.mState = connection.getState();
        this.mDisconnectCause = connection.getDisconnectCause();
        this.mRingbackRequested = connection.isRingbackRequested();
        this.mConnectionCapabilities = connection.getConnectionCapabilities();
        this.mConnectionProperties = connection.getConnectionProperties();
        this.mVideoState = connection.getVideoState();
        this.mVideoProvider = new VideoProvider(connection.getVideoProvider());
        this.mIsVoipAudioMode = connection.getIsVoipAudioMode();
        this.mStatusHints = connection.getStatusHints();
        this.mAddress = connection.getHandle();
        this.mAddressPresentation = connection.getHandlePresentation();
        this.mCallerDisplayName = connection.getCallerDisplayName();
        this.mCallerDisplayNamePresentation = connection.getCallerDisplayNamePresentation();
        this.mConference = null;
    }

    RemoteConnection(DisconnectCause disconnectCause) {
        this.mCallbackRecords = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableconferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectionId = "NULL";
        this.mConnected = false;
        this.mState = 6;
        this.mDisconnectCause = disconnectCause;
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, new Handler());
    }

    public void registerCallback(Callback callback, Handler handler) {
        unregisterCallback(callback);
        if (callback != null && handler != null) {
            this.mCallbackRecords.add(new CallbackRecord(callback, handler));
        }
    }

    public void unregisterCallback(Callback callback) {
        if (callback != null) {
            for (CallbackRecord record : this.mCallbackRecords) {
                if (record.getCallback() == callback) {
                    this.mCallbackRecords.remove(record);
                    return;
                }
            }
        }
    }

    public int getState() {
        return this.mState;
    }

    public DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public boolean isVoipAudioMode() {
        return this.mIsVoipAudioMode;
    }

    public StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public int getAddressPresentation() {
        return this.mAddressPresentation;
    }

    public CharSequence getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public final VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public final Bundle getExtras() {
        return this.mExtras;
    }

    public boolean isRingbackRequested() {
        return this.mRingbackRequested;
    }

    public void abort() {
        try {
            if (this.mConnected) {
                this.mConnectionService.abort(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer() {
        try {
            if (this.mConnected) {
                this.mConnectionService.answer(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer(int videoState) {
        try {
            if (this.mConnected) {
                this.mConnectionService.answerVideo(this.mConnectionId, videoState);
            }
        } catch (RemoteException e) {
        }
    }

    public void reject() {
        try {
            if (this.mConnected) {
                this.mConnectionService.reject(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void hold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.hold(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void unhold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.unhold(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void disconnect() {
        try {
            if (this.mConnected) {
                this.mConnectionService.disconnect(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void playDtmfTone(char digit) {
        try {
            if (this.mConnected) {
                this.mConnectionService.playDtmfTone(this.mConnectionId, digit);
            }
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone() {
        try {
            if (this.mConnected) {
                this.mConnectionService.stopDtmfTone(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void postDialContinue(boolean proceed) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onPostDialContinue(this.mConnectionId, proceed);
            }
        } catch (RemoteException e) {
        }
    }

    public void pullExternalCall() {
        try {
            if (this.mConnected) {
                this.mConnectionService.pullExternalCall(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public void setAudioState(AudioState state) {
        setCallAudioState(new CallAudioState(state));
    }

    public void setCallAudioState(CallAudioState state) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onCallAudioStateChanged(this.mConnectionId, state);
            }
        } catch (RemoteException e) {
        }
    }

    public List<RemoteConnection> getConferenceableConnections() {
        return this.mUnmodifiableconferenceableConnections;
    }

    public RemoteConference getConference() {
        return this.mConference;
    }

    String getId() {
        return this.mConnectionId;
    }

    IConnectionService getConnectionService() {
        return this.mConnectionService;
    }

    void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            for (CallbackRecord record : this.mCallbackRecords) {
                RemoteConnection connection = this;
                record.getHandler().post(new AnonymousClass1(record.getCallback(), this, state));
            }
        }
    }

    void setDisconnected(DisconnectCause disconnectCause) {
        if (this.mState != 6) {
            this.mState = 6;
            this.mDisconnectCause = disconnectCause;
            for (CallbackRecord record : this.mCallbackRecords) {
                RemoteConnection connection = this;
                record.getHandler().post(new AnonymousClass2(record.getCallback(), this, disconnectCause));
            }
        }
    }

    public void setDisconnectedWithSsNotification(int disconnectCause, String disconnectMessage, int type, int code) {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.setDisconnectedWithSsNotification(this, disconnectCause, disconnectMessage, type, code);
        }
    }

    void setRingbackRequested(boolean ringback) {
        if (this.mRingbackRequested != ringback) {
            this.mRingbackRequested = ringback;
            for (CallbackRecord record : this.mCallbackRecords) {
                RemoteConnection connection = this;
                record.getHandler().post(new AnonymousClass3(record.getCallback(), this, ringback));
            }
        }
    }

    void setConnectionCapabilities(int connectionCapabilities) {
        this.mConnectionCapabilities = connectionCapabilities;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass4(record.getCallback(), this, connectionCapabilities));
        }
    }

    void setConnectionProperties(int connectionProperties) {
        this.mConnectionProperties = connectionProperties;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass5(record.getCallback(), this, connectionProperties));
        }
    }

    void setDestroyed() {
        if (!this.mCallbackRecords.isEmpty()) {
            if (this.mState != 6) {
                setDisconnected(new DisconnectCause(1, "Connection destroyed."));
            }
            for (CallbackRecord record : this.mCallbackRecords) {
                RemoteConnection connection = this;
                record.getHandler().post(new AnonymousClass6(record.getCallback(), this));
            }
            this.mCallbackRecords.clear();
            this.mConnected = false;
        }
    }

    void setPostDialWait(String remainingDigits) {
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass7(record.getCallback(), this, remainingDigits));
        }
    }

    void onPostDialChar(char nextChar) {
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass8(record.getCallback(), this, nextChar));
        }
    }

    void setVideoState(int videoState) {
        this.mVideoState = videoState;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass9(record.getCallback(), this, videoState));
        }
    }

    void setVideoProvider(VideoProvider videoProvider) {
        this.mVideoProvider = videoProvider;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass10(record.getCallback(), this, videoProvider));
        }
    }

    void setIsVoipAudioMode(boolean isVoip) {
        this.mIsVoipAudioMode = isVoip;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass11(record.getCallback(), this, isVoip));
        }
    }

    void setStatusHints(StatusHints statusHints) {
        this.mStatusHints = statusHints;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass12(record.getCallback(), this, statusHints));
        }
    }

    void setAddress(Uri address, int presentation) {
        this.mAddress = address;
        this.mAddressPresentation = presentation;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass13(record.getCallback(), this, address, presentation));
        }
    }

    void setCallerDisplayName(String callerDisplayName, int presentation) {
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = presentation;
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass14(record.getCallback(), this, callerDisplayName, presentation));
        }
    }

    void setConferenceableConnections(List<RemoteConnection> conferenceableConnections) {
        this.mConferenceableConnections.clear();
        this.mConferenceableConnections.addAll(conferenceableConnections);
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass15(record.getCallback(), this));
        }
    }

    void setConference(RemoteConference conference) {
        if (this.mConference != conference) {
            this.mConference = conference;
            for (CallbackRecord record : this.mCallbackRecords) {
                RemoteConnection connection = this;
                record.getHandler().post(new AnonymousClass16(record.getCallback(), this, conference));
            }
        }
    }

    void putExtras(Bundle extras) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putAll(extras);
        notifyExtrasChanged();
    }

    void removeExtras(List<String> keys) {
        if (this.mExtras != null && keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                this.mExtras.remove(key);
            }
            notifyExtrasChanged();
        }
    }

    private void notifyExtrasChanged() {
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass17(record.getCallback(), this));
        }
    }

    void onConnectionEvent(String event, Bundle extras) {
        for (CallbackRecord record : this.mCallbackRecords) {
            RemoteConnection connection = this;
            record.getHandler().post(new AnonymousClass18(record.getCallback(), this, event, extras));
        }
    }

    void setPhoneAccountHandle(PhoneAccountHandle pHandle) {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.setPhoneAccountHandle(this, pHandle);
        }
    }

    public static RemoteConnection failure(DisconnectCause disconnectCause) {
        return new RemoteConnection(disconnectCause);
    }
}
