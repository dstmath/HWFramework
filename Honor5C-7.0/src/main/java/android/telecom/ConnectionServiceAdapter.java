package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.telecom.Connection.VideoProvider;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class ConnectionServiceAdapter implements DeathRecipient {
    private final Set<IConnectionServiceAdapter> mAdapters;

    ConnectionServiceAdapter() {
        this.mAdapters = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
    }

    void addAdapter(IConnectionServiceAdapter adapter) {
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

    void removeAdapter(IConnectionServiceAdapter adapter) {
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
            IConnectionServiceAdapter adapter = (IConnectionServiceAdapter) it.next();
            if (!adapter.asBinder().isBinderAlive()) {
                it.remove();
                adapter.asBinder().unlinkToDeath(this, 0);
            }
        }
    }

    void handleCreateConnectionComplete(String id, ConnectionRequest request, ParcelableConnection connection) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.handleCreateConnectionComplete(id, request, connection);
            } catch (RemoteException e) {
            }
        }
    }

    void setActive(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setActive(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void setRinging(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setRinging(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void setDialing(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDialing(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void setDisconnected(String callId, DisconnectCause disconnectCause) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDisconnected(callId, disconnectCause);
            } catch (RemoteException e) {
            }
        }
    }

    void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setDisconnectedWithSsNotification(callId, disconnectCause, disconnectMessage, type, code);
            } catch (RemoteException e) {
            }
        }
    }

    void setOnHold(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setOnHold(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void setRingbackRequested(String callId, boolean ringback) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setRingbackRequested(callId, ringback);
            } catch (RemoteException e) {
            }
        }
    }

    void setConnectionCapabilities(String callId, int capabilities) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConnectionCapabilities(callId, capabilities);
            } catch (RemoteException e) {
            }
        }
    }

    void setConnectionProperties(String callId, int properties) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConnectionProperties(callId, properties);
            } catch (RemoteException e) {
            }
        }
    }

    void setIsConferenced(String callId, String conferenceCallId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "sending connection %s with conference %s", callId, conferenceCallId);
                adapter.setIsConferenced(callId, conferenceCallId);
            } catch (RemoteException e) {
            }
        }
    }

    void onConferenceMergeFailed(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Log.d((Object) this, "merge failed for call %s", callId);
                adapter.setConferenceMergeFailed(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void removeCall(String callId) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.removeCall(callId);
            } catch (RemoteException e) {
            }
        }
    }

    void onPostDialWait(String callId, String remaining) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onPostDialWait(callId, remaining);
            } catch (RemoteException e) {
            }
        }
    }

    void onPostDialChar(String callId, char nextChar) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onPostDialChar(callId, nextChar);
            } catch (RemoteException e) {
            }
        }
    }

    void addConferenceCall(String callId, ParcelableConference parcelableConference) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.addConferenceCall(callId, parcelableConference);
            } catch (RemoteException e) {
            }
        }
    }

    void queryRemoteConnectionServices(RemoteServiceCallback callback) {
        if (this.mAdapters.size() == 1) {
            try {
                ((IConnectionServiceAdapter) this.mAdapters.iterator().next()).queryRemoteConnectionServices(callback);
            } catch (Throwable e) {
                Log.e((Object) this, e, "Exception trying to query for remote CSs", new Object[0]);
            }
        }
    }

    void setVideoProvider(String callId, VideoProvider videoProvider) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setVideoProvider(callId, videoProvider == null ? null : videoProvider.getInterface());
            } catch (RemoteException e) {
            }
        }
    }

    void setIsVoipAudioMode(String callId, boolean isVoip) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setIsVoipAudioMode(callId, isVoip);
            } catch (RemoteException e) {
            }
        }
    }

    void setStatusHints(String callId, StatusHints statusHints) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setStatusHints(callId, statusHints);
            } catch (RemoteException e) {
            }
        }
    }

    void setAddress(String callId, Uri address, int presentation) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setAddress(callId, address, presentation);
            } catch (RemoteException e) {
            }
        }
    }

    void setCallerDisplayName(String callId, String callerDisplayName, int presentation) {
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setCallerDisplayName(callId, callerDisplayName, presentation);
            } catch (RemoteException e) {
            }
        }
    }

    void setVideoState(String callId, int videoState) {
        Log.v((Object) this, "setVideoState: %d", Integer.valueOf(videoState));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setVideoState(callId, videoState);
            } catch (RemoteException e) {
            }
        }
    }

    void setConferenceableConnections(String callId, List<String> conferenceableCallIds) {
        Log.v((Object) this, "setConferenceableConnections: %s, %s", callId, conferenceableCallIds);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setConferenceableConnections(callId, conferenceableCallIds);
            } catch (RemoteException e) {
            }
        }
    }

    void addExistingConnection(String callId, ParcelableConnection connection) {
        Log.v((Object) this, "addExistingConnection: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.addExistingConnection(callId, connection);
            } catch (RemoteException e) {
            }
        }
    }

    void putExtras(String callId, Bundle extras) {
        Log.v((Object) this, "putExtras: %s", callId);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.putExtras(callId, extras);
            } catch (RemoteException e) {
            }
        }
    }

    void putExtra(String callId, String key, boolean value) {
        Log.v((Object) this, "putExtra: %s %s=%b", callId, key, Boolean.valueOf(value));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putBoolean(key, value);
                adapter.putExtras(callId, bundle);
            } catch (RemoteException e) {
            }
        }
    }

    void putExtra(String callId, String key, int value) {
        Log.v((Object) this, "putExtra: %s %s=%d", callId, key, Integer.valueOf(value));
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putInt(key, value);
                adapter.putExtras(callId, bundle);
            } catch (RemoteException e) {
            }
        }
    }

    void putExtra(String callId, String key, String value) {
        Log.v((Object) this, "putExtra: %s %s=%s", callId, key, value);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString(key, value);
                adapter.putExtras(callId, bundle);
            } catch (RemoteException e) {
            }
        }
    }

    void removeExtras(String callId, List<String> keys) {
        Log.v((Object) this, "removeExtras: %s %s", callId, keys);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.removeExtras(callId, keys);
            } catch (RemoteException e) {
            }
        }
    }

    void onConnectionEvent(String callId, String event, Bundle extras) {
        Log.v((Object) this, "onConnectionEvent: %s", event);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.onConnectionEvent(callId, event, extras);
            } catch (RemoteException e) {
            }
        }
    }

    void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) {
        Log.v((Object) this, "setPhoneAccountHandle: %s, %s", callId, pHandle);
        for (IConnectionServiceAdapter adapter : this.mAdapters) {
            try {
                adapter.setPhoneAccountHandle(callId, pHandle);
            } catch (RemoteException e) {
            }
        }
    }
}
