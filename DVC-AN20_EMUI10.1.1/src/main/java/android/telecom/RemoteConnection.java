package android.telecom;

import android.annotation.SystemApi;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.telecom.Connection;
import android.telecom.RemoteConnection;
import android.telecom.VideoProfile;
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

        public void onRttInitiationSuccess(RemoteConnection connection) {
        }

        public void onRttInitiationFailure(RemoteConnection connection, int reason) {
        }

        public void onRttSessionRemotelyTerminated(RemoteConnection connection) {
        }

        public void onRemoteRttRequest(RemoteConnection connection) {
        }
    }

    public static class VideoProvider {
        private final Set<Callback> mCallbacks = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        private final String mCallingPackage;
        private final int mTargetSdkVersion;
        private final IVideoCallback mVideoCallbackDelegate = new IVideoCallback() {
            /* class android.telecom.RemoteConnection.VideoProvider.AnonymousClass1 */

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyRequest(VideoProfile videoProfile) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onSessionModifyRequestReceived(VideoProvider.this, videoProfile);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onSessionModifyResponseReceived(VideoProvider.this, status, requestedProfile, responseProfile);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void handleCallSessionEvent(int event) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onCallSessionEvent(VideoProvider.this, event);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changePeerDimensions(int width, int height) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onPeerDimensionsChanged(VideoProvider.this, width, height);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCallDataUsage(long dataUsage) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onCallDataUsageChanged(VideoProvider.this, dataUsage);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onCameraCapabilitiesChanged(VideoProvider.this, cameraCapabilities);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeVideoQuality(int videoQuality) {
                for (Callback l : VideoProvider.this.mCallbacks) {
                    l.onVideoQualityChanged(VideoProvider.this, videoQuality);
                }
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return null;
            }
        };
        private final VideoCallbackServant mVideoCallbackServant = new VideoCallbackServant(this.mVideoCallbackDelegate);
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

            public void onCameraCapabilitiesChanged(VideoProvider videoProvider, VideoProfile.CameraCapabilities cameraCapabilities) {
            }

            public void onVideoQualityChanged(VideoProvider videoProvider, int videoQuality) {
            }
        }

        VideoProvider(IVideoProvider videoProviderBinder, String callingPackage, int targetSdkVersion) {
            this.mVideoProviderBinder = videoProviderBinder;
            this.mCallingPackage = callingPackage;
            this.mTargetSdkVersion = targetSdkVersion;
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
                this.mVideoProviderBinder.setCamera(cameraId, this.mCallingPackage, this.mTargetSdkVersion);
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

    RemoteConnection(String callId, IConnectionService connectionService, ParcelableConnection connection, String callingPackage, int targetSdkVersion) {
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
        IVideoProvider videoProvider = connection.getVideoProvider();
        if (videoProvider != null) {
            this.mVideoProvider = new VideoProvider(videoProvider, callingPackage, targetSdkVersion);
        } else {
            this.mVideoProvider = null;
        }
        this.mIsVoipAudioMode = connection.getIsVoipAudioMode();
        this.mStatusHints = connection.getStatusHints();
        this.mAddress = connection.getHandle();
        this.mAddressPresentation = connection.getHandlePresentation();
        this.mCallerDisplayName = connection.getCallerDisplayName();
        this.mCallerDisplayNamePresentation = connection.getCallerDisplayNamePresentation();
        this.mConference = null;
        putExtras(connection.getExtras());
        Bundle newExtras = new Bundle();
        newExtras.putString(Connection.EXTRA_ORIGINAL_CONNECTION_ID, callId);
        putExtras(newExtras);
    }

    RemoteConnection(DisconnectCause disconnectCause) {
        this.mCallbackRecords = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableconferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectionId = WifiEnterpriseConfig.EMPTY_VALUE;
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
                this.mConnectionService.abort(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer() {
        try {
            if (this.mConnected) {
                this.mConnectionService.answer(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer(int videoState) {
        try {
            if (this.mConnected) {
                this.mConnectionService.answerVideo(this.mConnectionId, videoState, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void reject() {
        try {
            if (this.mConnected) {
                this.mConnectionService.reject(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void hold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.hold(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void unhold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.unhold(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void disconnect() {
        try {
            if (this.mConnected) {
                this.mConnectionService.disconnect(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void playDtmfTone(char digit) {
        try {
            if (this.mConnected) {
                this.mConnectionService.playDtmfTone(this.mConnectionId, digit, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone() {
        try {
            if (this.mConnected) {
                this.mConnectionService.stopDtmfTone(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void postDialContinue(boolean proceed) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onPostDialContinue(this.mConnectionId, proceed, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void pullExternalCall() {
        try {
            if (this.mConnected) {
                this.mConnectionService.pullExternalCall(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    @SystemApi
    @Deprecated
    public void setAudioState(AudioState state) {
        setCallAudioState(new CallAudioState(state));
    }

    public void setCallAudioState(CallAudioState state) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onCallAudioStateChanged(this.mConnectionId, state, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void startRtt(Connection.RttTextStream rttTextStream) {
        try {
            if (this.mConnected) {
                this.mConnectionService.startRtt(this.mConnectionId, rttTextStream.getFdFromInCall(), rttTextStream.getFdToInCall(), null);
            }
        } catch (RemoteException e) {
        }
    }

    public void stopRtt() {
        try {
            if (this.mConnected) {
                this.mConnectionService.stopRtt(this.mConnectionId, null);
            }
        } catch (RemoteException e) {
        }
    }

    public void sendRttUpgradeResponse(Connection.RttTextStream rttTextStream) {
        try {
            if (!this.mConnected) {
                return;
            }
            if (rttTextStream == null) {
                this.mConnectionService.respondToRttUpgradeRequest(this.mConnectionId, null, null, null);
            } else {
                this.mConnectionService.respondToRttUpgradeRequest(this.mConnectionId, rttTextStream.getFdFromInCall(), rttTextStream.getFdToInCall(), null);
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

    /* access modifiers changed from: package-private */
    public String getId() {
        return this.mConnectionId;
    }

    /* access modifiers changed from: package-private */
    public IConnectionService getConnectionService() {
        return this.mConnectionService;
    }

    /* access modifiers changed from: package-private */
    public void setState(final int state) {
        if (this.mState != state) {
            this.mState = state;
            for (CallbackRecord record : this.mCallbackRecords) {
                final Callback callback = record.getCallback();
                record.getHandler().post(new Runnable() {
                    /* class android.telecom.RemoteConnection.AnonymousClass1 */

                    public void run() {
                        callback.onStateChanged(this, state);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisconnected(final DisconnectCause disconnectCause) {
        if (this.mState != 6) {
            this.mState = 6;
            this.mDisconnectCause = disconnectCause;
            for (CallbackRecord record : this.mCallbackRecords) {
                final Callback callback = record.getCallback();
                record.getHandler().post(new Runnable() {
                    /* class android.telecom.RemoteConnection.AnonymousClass2 */

                    public void run() {
                        callback.onDisconnected(this, disconnectCause);
                    }
                });
            }
        }
    }

    public void setDisconnectedWithSsNotification(int disconnectCause, String disconnectMessage, int type, int code) {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.setDisconnectedWithSsNotification(this, disconnectCause, disconnectMessage, type, code);
        }
    }

    /* access modifiers changed from: package-private */
    public void setRingbackRequested(final boolean ringback) {
        if (this.mRingbackRequested != ringback) {
            this.mRingbackRequested = ringback;
            for (CallbackRecord record : this.mCallbackRecords) {
                final Callback callback = record.getCallback();
                record.getHandler().post(new Runnable() {
                    /* class android.telecom.RemoteConnection.AnonymousClass3 */

                    public void run() {
                        callback.onRingbackRequested(this, ringback);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConnectionCapabilities(final int connectionCapabilities) {
        this.mConnectionCapabilities = connectionCapabilities;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass4 */

                public void run() {
                    callback.onConnectionCapabilitiesChanged(this, connectionCapabilities);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setConnectionProperties(final int connectionProperties) {
        this.mConnectionProperties = connectionProperties;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass5 */

                public void run() {
                    callback.onConnectionPropertiesChanged(this, connectionProperties);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setDestroyed() {
        if (!this.mCallbackRecords.isEmpty()) {
            if (this.mState != 6) {
                setDisconnected(new DisconnectCause(1, "Connection destroyed."));
            }
            for (CallbackRecord record : this.mCallbackRecords) {
                final Callback callback = record.getCallback();
                record.getHandler().post(new Runnable() {
                    /* class android.telecom.RemoteConnection.AnonymousClass6 */

                    public void run() {
                        callback.onDestroyed(this);
                    }
                });
            }
            this.mCallbackRecords.clear();
            this.mConnected = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPostDialWait(final String remainingDigits) {
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass7 */

                public void run() {
                    callback.onPostDialWait(this, remainingDigits);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostDialChar(final char nextChar) {
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass8 */

                public void run() {
                    callback.onPostDialChar(this, nextChar);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoState(final int videoState) {
        this.mVideoState = videoState;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass9 */

                public void run() {
                    callback.onVideoStateChanged(this, videoState);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoProvider(final VideoProvider videoProvider) {
        this.mVideoProvider = videoProvider;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass10 */

                public void run() {
                    callback.onVideoProviderChanged(this, videoProvider);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsVoipAudioMode(final boolean isVoip) {
        this.mIsVoipAudioMode = isVoip;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass11 */

                public void run() {
                    callback.onVoipAudioChanged(this, isVoip);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setStatusHints(final StatusHints statusHints) {
        this.mStatusHints = statusHints;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass12 */

                public void run() {
                    callback.onStatusHintsChanged(this, statusHints);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setAddress(final Uri address, final int presentation) {
        this.mAddress = address;
        this.mAddressPresentation = presentation;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass13 */

                public void run() {
                    callback.onAddressChanged(this, address, presentation);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallerDisplayName(final String callerDisplayName, final int presentation) {
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = presentation;
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass14 */

                public void run() {
                    callback.onCallerDisplayNameChanged(this, callerDisplayName, presentation);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setConferenceableConnections(List<RemoteConnection> conferenceableConnections) {
        this.mConferenceableConnections.clear();
        this.mConferenceableConnections.addAll(conferenceableConnections);
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass15 */

                public void run() {
                    callback.onConferenceableConnectionsChanged(this, RemoteConnection.this.mUnmodifiableconferenceableConnections);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setConference(final RemoteConference conference) {
        if (this.mConference != conference) {
            this.mConference = conference;
            for (CallbackRecord record : this.mCallbackRecords) {
                final Callback callback = record.getCallback();
                record.getHandler().post(new Runnable() {
                    /* class android.telecom.RemoteConnection.AnonymousClass16 */

                    public void run() {
                        callback.onConferenceChanged(this, conference);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putExtras(Bundle extras) {
        if (extras != null) {
            if (this.mExtras == null) {
                this.mExtras = new Bundle();
            }
            try {
                this.mExtras.putAll(extras);
            } catch (BadParcelableException bpe) {
                Log.w(this, "putExtras: could not unmarshal extras; exception = " + bpe, new Object[0]);
            }
            notifyExtrasChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeExtras(List<String> keys) {
        if (!(this.mExtras == null || keys == null || keys.isEmpty())) {
            for (String key : keys) {
                this.mExtras.remove(key);
            }
            notifyExtrasChanged();
        }
    }

    private void notifyExtrasChanged() {
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass17 */

                public void run() {
                    callback.onExtrasChanged(this, RemoteConnection.this.mExtras);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void onConnectionEvent(final String event, final Bundle extras) {
        for (CallbackRecord record : this.mCallbackRecords) {
            final Callback callback = record.getCallback();
            record.getHandler().post(new Runnable() {
                /* class android.telecom.RemoteConnection.AnonymousClass18 */

                public void run() {
                    callback.onConnectionEvent(this, event, extras);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setPhoneAccountHandle(PhoneAccountHandle pHandle) {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.setPhoneAccountHandle(this, pHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttInitiationSuccess() {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this) {
                /* class android.telecom.$$Lambda$RemoteConnection$C4t0J6QK31Ef1UFsdPVwkew1VaQ */
                private final /* synthetic */ RemoteConnection f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    RemoteConnection.Callback.this.onRttInitiationSuccess(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttInitiationFailure(int reason) {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this, reason) {
                /* class android.telecom.$$Lambda$RemoteConnection$AwagQDJDcNDplrFif6DlYZldL5E */
                private final /* synthetic */ RemoteConnection f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    RemoteConnection.Callback.this.onRttInitiationFailure(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttSessionRemotelyTerminated() {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this) {
                /* class android.telecom.$$Lambda$RemoteConnection$mmHouQhUcou9PRJ9qkMqlkKzAs */
                private final /* synthetic */ RemoteConnection f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    RemoteConnection.Callback.this.onRttSessionRemotelyTerminated(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void onRemoteRttRequest() {
        for (CallbackRecord record : this.mCallbackRecords) {
            record.getHandler().post(new Runnable(this) {
                /* class android.telecom.$$Lambda$RemoteConnection$yp1cNJ53RzQGFz3RZRlC3urzQv4 */
                private final /* synthetic */ RemoteConnection f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    RemoteConnection.Callback.this.onRemoteRttRequest(this.f$1);
                }
            });
        }
    }

    public static RemoteConnection failure(DisconnectCause disconnectCause) {
        return new RemoteConnection(disconnectCause);
    }

    /* access modifiers changed from: private */
    public static final class CallbackRecord extends Callback {
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
}
