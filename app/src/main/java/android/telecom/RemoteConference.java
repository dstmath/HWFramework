package android.telecom;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.android.internal.telecom.IConnectionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public final class RemoteConference {
    private final Set<CallbackRecord<Callback>> mCallbackRecords;
    private final List<RemoteConnection> mChildConnections;
    private final List<RemoteConnection> mConferenceableConnections;
    private int mConnectionCapabilities;
    private int mConnectionProperties;
    private final IConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private Bundle mExtras;
    private final String mId;
    private int mState;
    private final List<RemoteConnection> mUnmodifiableChildConnections;
    private final List<RemoteConnection> mUnmodifiableConferenceableConnections;

    /* renamed from: android.telecom.RemoteConference.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;

        AnonymousClass1(Callback val$callback, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onDestroyed(this.val$conference);
        }
    }

    /* renamed from: android.telecom.RemoteConference.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;
        final /* synthetic */ int val$newState;
        final /* synthetic */ int val$oldState;

        AnonymousClass2(Callback val$callback, RemoteConference val$conference, int val$oldState, int val$newState) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
            this.val$oldState = val$oldState;
            this.val$newState = val$newState;
        }

        public void run() {
            this.val$callback.onStateChanged(this.val$conference, this.val$oldState, this.val$newState);
        }
    }

    /* renamed from: android.telecom.RemoteConference.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass3(Callback val$callback, RemoteConference val$conference, RemoteConnection val$connection) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
            this.val$connection = val$connection;
        }

        public void run() {
            this.val$callback.onConnectionAdded(this.val$conference, this.val$connection);
        }
    }

    /* renamed from: android.telecom.RemoteConference.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;
        final /* synthetic */ RemoteConnection val$connection;

        AnonymousClass4(Callback val$callback, RemoteConference val$conference, RemoteConnection val$connection) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
            this.val$connection = val$connection;
        }

        public void run() {
            this.val$callback.onConnectionRemoved(this.val$conference, this.val$connection);
        }
    }

    /* renamed from: android.telecom.RemoteConference.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;

        AnonymousClass5(Callback val$callback, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onConnectionCapabilitiesChanged(this.val$conference, RemoteConference.this.mConnectionCapabilities);
        }
    }

    /* renamed from: android.telecom.RemoteConference.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;

        AnonymousClass6(Callback val$callback, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onConnectionPropertiesChanged(this.val$conference, RemoteConference.this.mConnectionProperties);
        }
    }

    /* renamed from: android.telecom.RemoteConference.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;

        AnonymousClass7(Callback val$callback, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onConferenceableConnectionsChanged(this.val$conference, RemoteConference.this.mUnmodifiableConferenceableConnections);
        }
    }

    /* renamed from: android.telecom.RemoteConference.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;
        final /* synthetic */ DisconnectCause val$disconnectCause;

        AnonymousClass8(Callback val$callback, RemoteConference val$conference, DisconnectCause val$disconnectCause) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
            this.val$disconnectCause = val$disconnectCause;
        }

        public void run() {
            this.val$callback.onDisconnected(this.val$conference, this.val$disconnectCause);
        }
    }

    /* renamed from: android.telecom.RemoteConference.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ Callback val$callback;
        final /* synthetic */ RemoteConference val$conference;

        AnonymousClass9(Callback val$callback, RemoteConference val$conference) {
            this.val$callback = val$callback;
            this.val$conference = val$conference;
        }

        public void run() {
            this.val$callback.onExtrasChanged(this.val$conference, RemoteConference.this.mExtras);
        }
    }

    public static abstract class Callback {
        public void onStateChanged(RemoteConference conference, int oldState, int newState) {
        }

        public void onDisconnected(RemoteConference conference, DisconnectCause disconnectCause) {
        }

        public void onConnectionAdded(RemoteConference conference, RemoteConnection connection) {
        }

        public void onConnectionRemoved(RemoteConference conference, RemoteConnection connection) {
        }

        public void onConnectionCapabilitiesChanged(RemoteConference conference, int connectionCapabilities) {
        }

        public void onConnectionPropertiesChanged(RemoteConference conference, int connectionProperties) {
        }

        public void onConferenceableConnectionsChanged(RemoteConference conference, List<RemoteConnection> list) {
        }

        public void onDestroyed(RemoteConference conference) {
        }

        public void onExtrasChanged(RemoteConference conference, Bundle extras) {
        }
    }

    RemoteConference(String id, IConnectionService connectionService) {
        this.mCallbackRecords = new CopyOnWriteArraySet();
        this.mChildConnections = new CopyOnWriteArrayList();
        this.mUnmodifiableChildConnections = Collections.unmodifiableList(this.mChildConnections);
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableConferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mId = id;
        this.mConnectionService = connectionService;
    }

    String getId() {
        return this.mId;
    }

    void setDestroyed() {
        for (RemoteConnection connection : this.mChildConnections) {
            connection.setConference(null);
        }
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            RemoteConference conference = this;
            record.getHandler().post(new AnonymousClass1((Callback) record.getCallback(), this));
        }
    }

    void setState(int newState) {
        if (newState == 4 || newState == 5 || newState == 6) {
            if (this.mState != newState) {
                int oldState = this.mState;
                this.mState = newState;
                for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                    RemoteConference conference = this;
                    record.getHandler().post(new AnonymousClass2((Callback) record.getCallback(), this, oldState, newState));
                }
            }
            return;
        }
        Log.w((Object) this, "Unsupported state transition for Conference call.", Connection.stateToString(newState));
    }

    void addConnection(RemoteConnection connection) {
        if (!this.mChildConnections.contains(connection)) {
            this.mChildConnections.add(connection);
            connection.setConference(this);
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                RemoteConference conference = this;
                record.getHandler().post(new AnonymousClass3((Callback) record.getCallback(), this, connection));
            }
        }
    }

    void removeConnection(RemoteConnection connection) {
        if (this.mChildConnections.contains(connection)) {
            this.mChildConnections.remove(connection);
            connection.setConference(null);
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                RemoteConference conference = this;
                record.getHandler().post(new AnonymousClass4((Callback) record.getCallback(), this, connection));
            }
        }
    }

    void setConnectionCapabilities(int connectionCapabilities) {
        if (this.mConnectionCapabilities != connectionCapabilities) {
            this.mConnectionCapabilities = connectionCapabilities;
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                RemoteConference conference = this;
                record.getHandler().post(new AnonymousClass5((Callback) record.getCallback(), this));
            }
        }
    }

    void setConnectionProperties(int connectionProperties) {
        if (this.mConnectionProperties != connectionProperties) {
            this.mConnectionProperties = connectionProperties;
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                RemoteConference conference = this;
                record.getHandler().post(new AnonymousClass6((Callback) record.getCallback(), this));
            }
        }
    }

    void setConferenceableConnections(List<RemoteConnection> conferenceableConnections) {
        this.mConferenceableConnections.clear();
        this.mConferenceableConnections.addAll(conferenceableConnections);
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            RemoteConference conference = this;
            record.getHandler().post(new AnonymousClass7((Callback) record.getCallback(), this));
        }
    }

    void setDisconnected(DisconnectCause disconnectCause) {
        if (this.mState != 6) {
            this.mDisconnectCause = disconnectCause;
            setState(6);
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                RemoteConference conference = this;
                record.getHandler().post(new AnonymousClass8((Callback) record.getCallback(), this, disconnectCause));
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
        for (CallbackRecord<Callback> record : this.mCallbackRecords) {
            RemoteConference conference = this;
            record.getHandler().post(new AnonymousClass9((Callback) record.getCallback(), this));
        }
    }

    public final List<RemoteConnection> getConnections() {
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

    public final Bundle getExtras() {
        return this.mExtras;
    }

    public void disconnect() {
        try {
            this.mConnectionService.disconnect(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void separate(RemoteConnection connection) {
        if (this.mChildConnections.contains(connection)) {
            try {
                this.mConnectionService.splitFromConference(connection.getId());
            } catch (RemoteException e) {
            }
        }
    }

    public void merge() {
        try {
            this.mConnectionService.mergeConference(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void swap() {
        try {
            this.mConnectionService.swapConference(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void hold() {
        try {
            this.mConnectionService.hold(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void unhold() {
        try {
            this.mConnectionService.unhold(this.mId);
        } catch (RemoteException e) {
        }
    }

    public DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public void playDtmfTone(char digit) {
        try {
            this.mConnectionService.playDtmfTone(this.mId, digit);
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone() {
        try {
            this.mConnectionService.stopDtmfTone(this.mId);
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public void setAudioState(AudioState state) {
        setCallAudioState(new CallAudioState(state));
    }

    public void setCallAudioState(CallAudioState state) {
        try {
            this.mConnectionService.onCallAudioStateChanged(this.mId, state);
        } catch (RemoteException e) {
        }
    }

    public List<RemoteConnection> getConferenceableConnections() {
        return this.mUnmodifiableConferenceableConnections;
    }

    public final void registerCallback(Callback callback) {
        registerCallback(callback, new Handler());
    }

    public final void registerCallback(Callback callback, Handler handler) {
        unregisterCallback(callback);
        if (callback != null && handler != null) {
            this.mCallbackRecords.add(new CallbackRecord(callback, handler));
        }
    }

    public final void unregisterCallback(Callback callback) {
        if (callback != null) {
            for (CallbackRecord<Callback> record : this.mCallbackRecords) {
                if (record.getCallback() == callback) {
                    this.mCallbackRecords.remove(record);
                    return;
                }
            }
        }
    }
}
