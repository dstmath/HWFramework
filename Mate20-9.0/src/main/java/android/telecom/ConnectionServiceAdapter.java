package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telecom.Connection;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class ConnectionServiceAdapter implements IBinder.DeathRecipient {
    private final Set<IConnectionServiceAdapter> mAdapters = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));

    ConnectionServiceAdapter() {
    }

    /* access modifiers changed from: package-private */
    public void addAdapter(IConnectionServiceAdapter adapter) {
        for (IConnectionServiceAdapter it : this.mAdapters) {
            if (it.asBinder() == adapter.asBinder()) {
                Log.w((Object) this, "Ignoring duplicate adapter addition.", new Object[0]);
                return;
            }
        }
        if (this.mAdapters.add(adapter)) {
            try {
                adapter.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                this.mAdapters.remove(adapter);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAdapter(IConnectionServiceAdapter adapter) {
        if (adapter != null) {
            for (IConnectionServiceAdapter it : this.mAdapters) {
                if (it.asBinder() == adapter.asBinder() && this.mAdapters.remove(it)) {
                    adapter.asBinder().unlinkToDeath(this, 0);
                    return;
                }
            }
        }
    }

    public void binderDied() {
        Iterator<IConnectionServiceAdapter> it = this.mAdapters.iterator();
        while (it.hasNext()) {
            IConnectionServiceAdapter adapter = it.next();
            if (!adapter.asBinder().isBinderAlive()) {
                it.remove();
                adapter.asBinder().unlinkToDeath(this, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCreateConnectionComplete(String id, ConnectionRequest request, ParcelableConnection connection) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.handleCreateConnectionComplete(id, request, connection, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setActive(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setActive(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRinging(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setRinging(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDialing(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDialing(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPulling(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setPulling(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisconnected(String callId, DisconnectCause disconnectCause) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDisconnected(callId, disconnectCause, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDisconnectedWithSsNotification(callId, disconnectCause, disconnectMessage, type, code);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setOnHold(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setOnHold(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRingbackRequested(String callId, boolean ringback) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setRingbackRequested(callId, ringback, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConnectionCapabilities(String callId, int capabilities) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConnectionCapabilities(callId, capabilities, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConnectionProperties(String callId, int properties) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConnectionProperties(callId, properties, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsConferenced(String callId, String conferenceCallId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "sending connection %s with conference %s", callId, conferenceCallId);
                adapter.setIsConferenced(callId, conferenceCallId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onConferenceMergeFailed(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "merge failed for call %s", callId);
                adapter.setConferenceMergeFailed(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeCall(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.removeCall(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostDialWait(String callId, String remaining) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onPostDialWait(callId, remaining, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostDialChar(String callId, char nextChar) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onPostDialChar(callId, nextChar, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addConferenceCall(String callId, ParcelableConference parcelableConference) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.addConferenceCall(callId, parcelableConference, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void queryRemoteConnectionServices(RemoteServiceCallback callback) {
        if (this.mAdapters.size() == 1) {
            try {
                this.mAdapters.iterator().next().queryRemoteConnectionServices(callback, Log.getExternalSession());
            } catch (RemoteException e) {
                Log.e((Object) this, (Throwable) e, "Exception trying to query for remote CSs", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoProvider(String callId, Connection.VideoProvider videoProvider) {
        IVideoProvider iVideoProvider;
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            if (videoProvider == null) {
                iVideoProvider = null;
            } else {
                try {
                    iVideoProvider = videoProvider.getInterface();
                } catch (RemoteException e) {
                }
            }
            adapter.setVideoProvider(callId, iVideoProvider, Log.getExternalSession());
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsVoipAudioMode(String callId, boolean isVoip) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setIsVoipAudioMode(callId, isVoip, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setStatusHints(String callId, StatusHints statusHints) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setStatusHints(callId, statusHints, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAddress(String callId, Uri address, int presentation) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setAddress(callId, address, presentation, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallerDisplayName(String callId, String callerDisplayName, int presentation) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setCallerDisplayName(callId, callerDisplayName, presentation, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoState(String callId, int videoState) {
        Log.v((Object) this, "setVideoState: %d", Integer.valueOf(videoState));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setVideoState(callId, videoState, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConferenceableConnections(String callId, List<String> conferenceableCallIds) {
        Log.v((Object) this, "setConferenceableConnections: %s, %s", callId, conferenceableCallIds);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConferenceableConnections(callId, conferenceableCallIds, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addExistingConnection(String callId, ParcelableConnection connection) {
        Log.v((Object) this, "addExistingConnection: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.addExistingConnection(callId, connection, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putExtras(String callId, Bundle extras) {
        Log.v((Object) this, "putExtras: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.putExtras(callId, extras, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putExtra(String callId, String key, boolean value) {
        Log.v((Object) this, "putExtra: %s %s=%b", callId, key, Boolean.valueOf(value));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putBoolean(key, value);
                adapter.putExtras(callId, bundle, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putExtra(String callId, String key, int value) {
        Log.v((Object) this, "putExtra: %s %s=%d", callId, key, Integer.valueOf(value));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putInt(key, value);
                adapter.putExtras(callId, bundle, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putExtra(String callId, String key, String value) {
        Log.v((Object) this, "putExtra: %s %s=%s", callId, key, value);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString(key, value);
                adapter.putExtras(callId, bundle, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeExtras(String callId, List<String> keys) {
        Log.v((Object) this, "removeExtras: %s %s", callId, keys);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.removeExtras(callId, keys, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAudioRoute(String callId, int audioRoute, String bluetoothAddress) {
        Log.v((Object) this, "setAudioRoute: %s %s %s", callId, CallAudioState.audioRouteToString(audioRoute), bluetoothAddress);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setAudioRoute(callId, audioRoute, bluetoothAddress, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onConnectionEvent(String callId, String event, Bundle extras) {
        Log.v((Object) this, "onConnectionEvent: %s", event);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onConnectionEvent(callId, event, extras, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttInitiationSuccess(String callId) {
        Log.v((Object) this, "onRttInitiationSuccess: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onRttInitiationSuccess(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttInitiationFailure(String callId, int reason) {
        Log.v((Object) this, "onRttInitiationFailure: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onRttInitiationFailure(callId, reason, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRttSessionRemotelyTerminated(String callId) {
        Log.v((Object) this, "onRttSessionRemotelyTerminated: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onRttSessionRemotelyTerminated(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRemoteRttRequest(String callId) {
        Log.v((Object) this, "onRemoteRttRequest: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onRemoteRttRequest(callId, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) {
        Log.v((Object) this, "setPhoneAccountHandle: %s, %s", callId, pHandle);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setPhoneAccountHandle(callId, pHandle);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPhoneAccountChanged(String callId, PhoneAccountHandle pHandle) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "onPhoneAccountChanged %s", callId);
                adapter.onPhoneAccountChanged(callId, pHandle, Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onConnectionServiceFocusReleased() {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "onConnectionServiceFocusReleased", new Object[0]);
                adapter.onConnectionServiceFocusReleased(Log.getExternalSession());
            } catch (RemoteException e) {
            }
        }
    }
}
