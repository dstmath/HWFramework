package android.telecom;

import android.os.Bundle;
import android.telecom.Call.Details;
import android.telecom.Connection.VideoProvider;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Conference extends Conferenceable {
    public static final long CONNECT_TIME_NOT_SPECIFIED = 0;
    private CallAudioState mCallAudioState;
    private final List<Connection> mChildConnections;
    private final List<Connection> mConferenceableConnections;
    private long mConnectTimeMillis;
    private int mConnectionCapabilities;
    private final android.telecom.Connection.Listener mConnectionDeathListener;
    private int mConnectionProperties;
    private DisconnectCause mDisconnectCause;
    private String mDisconnectMessage;
    private Bundle mExtras;
    private final Object mExtrasLock;
    private final Set<Listener> mListeners;
    private PhoneAccountHandle mPhoneAccount;
    private Set<String> mPreviousExtraKeys;
    private int mState;
    private StatusHints mStatusHints;
    private String mTelecomCallId;
    private final List<Connection> mUnmodifiableChildConnections;
    private final List<Connection> mUnmodifiableConferenceableConnections;

    public static abstract class Listener {
        public void onStateChanged(Conference conference, int oldState, int newState) {
        }

        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
        }

        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> list) {
        }

        public void onDestroyed(Conference conference) {
        }

        public void onConnectionCapabilitiesChanged(Conference conference, int connectionCapabilities) {
        }

        public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
        }

        public void onVideoStateChanged(Conference c, int videoState) {
        }

        public void onVideoProviderChanged(Conference c, VideoProvider videoProvider) {
        }

        public void onStatusHintsChanged(Conference conference, StatusHints statusHints) {
        }

        public void onExtrasChanged(Conference c, Bundle extras) {
        }

        public void onExtrasRemoved(Conference c, List<String> list) {
        }
    }

    public void removeCapability(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.Conference.removeCapability(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Conference.removeCapability(int):void");
    }

    public Conference(PhoneAccountHandle phoneAccount) {
        this.mListeners = new CopyOnWriteArraySet();
        this.mChildConnections = new CopyOnWriteArrayList();
        this.mUnmodifiableChildConnections = Collections.unmodifiableList(this.mChildConnections);
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableConferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectTimeMillis = 0;
        this.mExtrasLock = new Object();
        this.mConnectionDeathListener = new android.telecom.Connection.Listener() {
            public void onDestroyed(Connection c) {
                if (Conference.this.mConferenceableConnections.remove(c)) {
                    Conference.this.fireOnConferenceableConnectionsChanged();
                }
            }
        };
        this.mPhoneAccount = phoneAccount;
    }

    public final String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public final void setTelecomCallId(String telecomCallId) {
        this.mTelecomCallId = telecomCallId;
    }

    public final PhoneAccountHandle getPhoneAccountHandle() {
        return this.mPhoneAccount;
    }

    public final List<Connection> getConnections() {
        return this.mUnmodifiableChildConnections;
    }

    public final int getState() {
        return this.mState;
    }

    public final int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public final int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public static boolean can(int capabilities, int capability) {
        return (capabilities & capability) != 0;
    }

    public boolean can(int capability) {
        return can(this.mConnectionCapabilities, capability);
    }

    public void addCapability(int capability) {
        setConnectionCapabilities(this.mConnectionCapabilities | capability);
    }

    @Deprecated
    public final AudioState getAudioState() {
        return new AudioState(this.mCallAudioState);
    }

    public final CallAudioState getCallAudioState() {
        return this.mCallAudioState;
    }

    public VideoProvider getVideoProvider() {
        return null;
    }

    public int getVideoState() {
        return 0;
    }

    public void onDisconnect() {
    }

    public void onSeparate(Connection connection) {
    }

    public void onMerge(Connection connection) {
    }

    public void onHold() {
    }

    public void onUnhold() {
    }

    public void onMerge() {
    }

    public void onSwap() {
    }

    public void onPlayDtmfTone(char c) {
    }

    public void onStopDtmfTone() {
    }

    @Deprecated
    public void onAudioStateChanged(AudioState state) {
    }

    public void onCallAudioStateChanged(CallAudioState state) {
    }

    public void onConnectionAdded(Connection connection) {
    }

    public final void setOnHold() {
        setState(5);
    }

    public final void setDialing() {
        setState(3);
    }

    public final void setActive() {
        setState(4);
    }

    public final void setDisconnected(DisconnectCause disconnectCause) {
        this.mDisconnectCause = disconnectCause;
        setState(6);
        for (Listener l : this.mListeners) {
            l.onDisconnected(this, this.mDisconnectCause);
        }
    }

    public final DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public final void setConnectionCapabilities(int connectionCapabilities) {
        if (connectionCapabilities != this.mConnectionCapabilities) {
            this.mConnectionCapabilities = connectionCapabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this, this.mConnectionCapabilities);
            }
        }
    }

    public final void setConnectionProperties(int connectionProperties) {
        if (connectionProperties != this.mConnectionProperties) {
            this.mConnectionProperties = connectionProperties;
            for (Listener l : this.mListeners) {
                l.onConnectionPropertiesChanged(this, this.mConnectionProperties);
            }
        }
    }

    public final boolean addConnection(Connection connection) {
        Log.d((Object) this, "Connection=%s, connection=", connection);
        if (connection == null || this.mChildConnections.contains(connection) || !connection.setConference(this)) {
            return false;
        }
        this.mChildConnections.add(connection);
        onConnectionAdded(connection);
        for (Listener l : this.mListeners) {
            l.onConnectionAdded(this, connection);
        }
        return true;
    }

    public final void removeConnection(Connection connection) {
        Log.d((Object) this, "removing %s from %s", connection, this.mChildConnections);
        if (connection != null && this.mChildConnections.remove(connection)) {
            connection.resetConference();
            for (Listener l : this.mListeners) {
                l.onConnectionRemoved(this, connection);
            }
        }
    }

    public final void setConferenceableConnections(List<Connection> conferenceableConnections) {
        clearConferenceableList();
        for (Connection c : conferenceableConnections) {
            if (!this.mConferenceableConnections.contains(c)) {
                c.addConnectionListener(this.mConnectionDeathListener);
                this.mConferenceableConnections.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final void setVideoState(Connection c, int videoState) {
        Log.d((Object) this, "setVideoState Conference: %s Connection: %s VideoState: %s", this, c, Integer.valueOf(videoState));
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this, videoState);
        }
    }

    public final void setVideoProvider(Connection c, VideoProvider videoProvider) {
        Log.d((Object) this, "setVideoProvider Conference: %s Connection: %s VideoState: %s", this, c, videoProvider);
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this, videoProvider);
        }
    }

    private final void fireOnConferenceableConnectionsChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceableConnectionsChanged(this, getConferenceableConnections());
        }
    }

    public final List<Connection> getConferenceableConnections() {
        return this.mUnmodifiableConferenceableConnections;
    }

    public final void destroy() {
        Log.d((Object) this, "destroying conference : %s", this);
        for (Connection connection : this.mChildConnections) {
            Log.d((Object) this, "removing connection %s", connection);
            removeConnection(connection);
        }
        if (this.mState != 6) {
            Log.d((Object) this, "setting to disconnected", new Object[0]);
            setDisconnected(new DisconnectCause(2));
        }
        for (Listener l : this.mListeners) {
            l.onDestroyed(this);
        }
    }

    public final Conference addListener(Listener listener) {
        this.mListeners.add(listener);
        return this;
    }

    public final Conference removeListener(Listener listener) {
        this.mListeners.remove(listener);
        return this;
    }

    public Connection getPrimaryConnection() {
        if (this.mUnmodifiableChildConnections == null || this.mUnmodifiableChildConnections.isEmpty()) {
            return null;
        }
        return (Connection) this.mUnmodifiableChildConnections.get(0);
    }

    @Deprecated
    public final void setConnectTimeMillis(long connectTimeMillis) {
        setConnectionTime(connectTimeMillis);
    }

    public final void setConnectionTime(long connectionTimeMillis) {
        this.mConnectTimeMillis = connectionTimeMillis;
    }

    @Deprecated
    public final long getConnectTimeMillis() {
        return getConnectionTime();
    }

    public final long getConnectionTime() {
        return this.mConnectTimeMillis;
    }

    final void setCallAudioState(CallAudioState state) {
        Log.d((Object) this, "setCallAudioState %s", state);
        this.mCallAudioState = state;
        onAudioStateChanged(getAudioState());
        onCallAudioStateChanged(state);
    }

    private void setState(int newState) {
        if (newState == 4 || newState == 3 || newState == 5 || newState == 6) {
            if (this.mState != newState) {
                int oldState = this.mState;
                this.mState = newState;
                for (Listener l : this.mListeners) {
                    l.onStateChanged(this, oldState, newState);
                }
            }
            return;
        }
        Log.w((Object) this, "Unsupported state transition for Conference call.", Connection.stateToString(newState));
    }

    private final void clearConferenceableList() {
        for (Connection c : this.mConferenceableConnections) {
            c.removeConnectionListener(this.mConnectionDeathListener);
        }
        this.mConferenceableConnections.clear();
    }

    public String toString() {
        return String.format(Locale.US, "[State: %s,Capabilites: %s, VideoState: %s, VideoProvider: %s, ThisObject %s]", new Object[]{Connection.stateToString(this.mState), Details.capabilitiesToString(this.mConnectionCapabilities), Integer.valueOf(getVideoState()), getVideoProvider(), super.toString()});
    }

    public final void setStatusHints(StatusHints statusHints) {
        this.mStatusHints = statusHints;
        for (Listener l : this.mListeners) {
            l.onStatusHintsChanged(this, statusHints);
        }
    }

    public final StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public final void setExtras(Bundle extras) {
        synchronized (this.mExtrasLock) {
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
    }

    public final void putExtras(Bundle extras) {
        if (extras != null) {
            Bundle listenersBundle;
            synchronized (this.mExtrasLock) {
                if (this.mExtras == null) {
                    this.mExtras = new Bundle();
                }
                this.mExtras.putAll(extras);
                listenersBundle = new Bundle(this.mExtras);
            }
            for (Listener l : this.mListeners) {
                l.onExtrasChanged(this, new Bundle(listenersBundle));
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
        if (keys != null && !keys.isEmpty()) {
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
    }

    public final Bundle getExtras() {
        return this.mExtras;
    }

    public void onExtrasChanged(Bundle extras) {
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
}
