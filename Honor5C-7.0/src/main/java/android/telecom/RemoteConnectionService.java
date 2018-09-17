package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.telecom.RemoteConnection.Callback;
import android.telecom.RemoteConnection.VideoProvider;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class RemoteConnectionService {
    private static final RemoteConference NULL_CONFERENCE = null;
    private static final RemoteConnection NULL_CONNECTION = null;
    private final Map<String, RemoteConference> mConferenceById;
    private final Map<String, RemoteConnection> mConnectionById;
    private final DeathRecipient mDeathRecipient;
    private final ConnectionService mOurConnectionServiceImpl;
    private final IConnectionService mOutgoingConnectionServiceRpc;
    private final Set<RemoteConnection> mPendingConnections;
    private final ConnectionServiceAdapterServant mServant;
    private final IConnectionServiceAdapter mServantDelegate;

    /* renamed from: android.telecom.RemoteConnectionService.3 */
    class AnonymousClass3 extends Callback {
        final /* synthetic */ String val$id;

        AnonymousClass3(String val$id) {
            this.val$id = val$id;
        }

        public void onDestroyed(RemoteConnection connection) {
            RemoteConnectionService.this.mConnectionById.remove(this.val$id);
            RemoteConnectionService.this.maybeDisconnectAdapter();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.RemoteConnectionService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.RemoteConnectionService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.RemoteConnectionService.<clinit>():void");
    }

    RemoteConnectionService(IConnectionService outgoingConnectionServiceRpc, ConnectionService ourConnectionServiceImpl) throws RemoteException {
        this.mServantDelegate = new IConnectionServiceAdapter() {

            /* renamed from: android.telecom.RemoteConnectionService.1.1 */
            class AnonymousClass1 extends RemoteConference.Callback {
                final /* synthetic */ String val$callId;

                AnonymousClass1(String val$callId) {
                    this.val$callId = val$callId;
                }

                public void onDestroyed(RemoteConference c) {
                    RemoteConnectionService.this.mConferenceById.remove(this.val$callId);
                    RemoteConnectionService.this.maybeDisconnectAdapter();
                }
            }

            public void handleCreateConnectionComplete(String id, ConnectionRequest request, ParcelableConnection parcel) {
                RemoteConnection connection = RemoteConnectionService.this.findConnectionForAction(id, "handleCreateConnectionSuccessful");
                if (connection != RemoteConnectionService.NULL_CONNECTION && RemoteConnectionService.this.mPendingConnections.contains(connection)) {
                    RemoteConnectionService.this.mPendingConnections.remove(connection);
                    connection.setConnectionCapabilities(parcel.getConnectionCapabilities());
                    connection.setConnectionProperties(parcel.getConnectionProperties());
                    if (!(parcel.getHandle() == null && parcel.getState() == 6)) {
                        connection.setAddress(parcel.getHandle(), parcel.getHandlePresentation());
                    }
                    if (!(parcel.getCallerDisplayName() == null && parcel.getState() == 6)) {
                        connection.setCallerDisplayName(parcel.getCallerDisplayName(), parcel.getCallerDisplayNamePresentation());
                    }
                    if (parcel.getState() == 6) {
                        connection.setDisconnected(parcel.getDisconnectCause());
                    } else {
                        connection.setState(parcel.getState());
                    }
                    List<RemoteConnection> conferenceable = new ArrayList();
                    for (String confId : parcel.getConferenceableConnectionIds()) {
                        if (RemoteConnectionService.this.mConnectionById.containsKey(confId)) {
                            conferenceable.add((RemoteConnection) RemoteConnectionService.this.mConnectionById.get(confId));
                        }
                    }
                    connection.setConferenceableConnections(conferenceable);
                    connection.setVideoState(parcel.getVideoState());
                    if (connection.getState() == 6) {
                        connection.setDestroyed();
                    }
                }
            }

            public void setActive(String callId) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setActive").setState(4);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setActive").setState(4);
                }
            }

            public void setRinging(String callId) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setRinging").setState(2);
            }

            public void setDialing(String callId) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setDialing").setState(3);
            }

            public void setDisconnected(String callId, DisconnectCause disconnectCause) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setDisconnected").setDisconnected(disconnectCause);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setDisconnected").setDisconnected(disconnectCause);
                }
            }

            public void setDisconnectedWithSsNotification(String callId, int disconnectCause, String disconnectMessage, int type, int code) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setDisconnectedWithSsNotification").setDisconnectedWithSsNotification(disconnectCause, disconnectMessage, type, code);
            }

            public void setOnHold(String callId) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setOnHold").setState(5);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setOnHold").setState(5);
                }
            }

            public void setRingbackRequested(String callId, boolean ringing) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setRingbackRequested").setRingbackRequested(ringing);
            }

            public void setConnectionCapabilities(String callId, int connectionCapabilities) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setConnectionCapabilities").setConnectionCapabilities(connectionCapabilities);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setConnectionCapabilities").setConnectionCapabilities(connectionCapabilities);
                }
            }

            public void setConnectionProperties(String callId, int connectionProperties) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setConnectionProperties").setConnectionProperties(connectionProperties);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setConnectionProperties").setConnectionProperties(connectionProperties);
                }
            }

            public void setIsConferenced(String callId, String conferenceCallId) {
                RemoteConnection connection = RemoteConnectionService.this.findConnectionForAction(callId, "setIsConferenced");
                if (connection == RemoteConnectionService.NULL_CONNECTION) {
                    return;
                }
                if (conferenceCallId != null) {
                    RemoteConference conference = RemoteConnectionService.this.findConferenceForAction(conferenceCallId, "setIsConferenced");
                    if (conference != RemoteConnectionService.NULL_CONFERENCE) {
                        conference.addConnection(connection);
                    }
                } else if (connection.getConference() != null) {
                    connection.getConference().removeConnection(connection);
                }
            }

            public void setConferenceMergeFailed(String callId) {
            }

            public void addConferenceCall(String callId, ParcelableConference parcel) {
                RemoteConference conference = new RemoteConference(callId, RemoteConnectionService.this.mOutgoingConnectionServiceRpc);
                for (String id : parcel.getConnectionIds()) {
                    RemoteConnection c = (RemoteConnection) RemoteConnectionService.this.mConnectionById.get(id);
                    if (c != null) {
                        conference.addConnection(c);
                    }
                }
                if (conference.getConnections().size() != 0) {
                    conference.setState(parcel.getState());
                    conference.setConnectionCapabilities(parcel.getConnectionCapabilities());
                    RemoteConnectionService.this.mConferenceById.put(callId, conference);
                    conference.registerCallback(new AnonymousClass1(callId));
                    RemoteConnectionService.this.mOurConnectionServiceImpl.addRemoteConference(conference);
                }
            }

            public void removeCall(String callId) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "removeCall").setDestroyed();
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "removeCall").setDestroyed();
                }
            }

            public void onPostDialWait(String callId, String remaining) {
                RemoteConnectionService.this.findConnectionForAction(callId, "onPostDialWait").setPostDialWait(remaining);
            }

            public void onPostDialChar(String callId, char nextChar) {
                RemoteConnectionService.this.findConnectionForAction(callId, "onPostDialChar").onPostDialChar(nextChar);
            }

            public void queryRemoteConnectionServices(RemoteServiceCallback callback) {
            }

            public void setVideoProvider(String callId, IVideoProvider videoProvider) {
                VideoProvider videoProvider2 = null;
                if (videoProvider != null) {
                    videoProvider2 = new VideoProvider(videoProvider);
                }
                RemoteConnectionService.this.findConnectionForAction(callId, "setVideoProvider").setVideoProvider(videoProvider2);
            }

            public void setVideoState(String callId, int videoState) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setVideoState").setVideoState(videoState);
            }

            public void setIsVoipAudioMode(String callId, boolean isVoip) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setIsVoipAudioMode").setIsVoipAudioMode(isVoip);
            }

            public void setStatusHints(String callId, StatusHints statusHints) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setStatusHints").setStatusHints(statusHints);
            }

            public void setAddress(String callId, Uri address, int presentation) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setAddress").setAddress(address, presentation);
            }

            public void setCallerDisplayName(String callId, String callerDisplayName, int presentation) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setCallerDisplayName").setCallerDisplayName(callerDisplayName, presentation);
            }

            public IBinder asBinder() {
                throw new UnsupportedOperationException();
            }

            public final void setConferenceableConnections(String callId, List<String> conferenceableConnectionIds) {
                List<RemoteConnection> conferenceable = new ArrayList();
                for (String id : conferenceableConnectionIds) {
                    if (RemoteConnectionService.this.mConnectionById.containsKey(id)) {
                        conferenceable.add((RemoteConnection) RemoteConnectionService.this.mConnectionById.get(id));
                    }
                }
                if (RemoteConnectionService.this.hasConnection(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "setConferenceableConnections").setConferenceableConnections(conferenceable);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "setConferenceableConnections").setConferenceableConnections(conferenceable);
                }
            }

            public void addExistingConnection(String callId, ParcelableConnection connection) {
                RemoteConnectionService.this.mOurConnectionServiceImpl.addRemoteExistingConnection(new RemoteConnection(callId, RemoteConnectionService.this.mOutgoingConnectionServiceRpc, connection));
            }

            public void putExtras(String callId, Bundle extras) {
                if (RemoteConnectionService.this.hasConnection(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "putExtras").putExtras(extras);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "putExtras").putExtras(extras);
                }
            }

            public void removeExtras(String callId, List<String> keys) {
                if (RemoteConnectionService.this.hasConnection(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "removeExtra").removeExtras(keys);
                } else {
                    RemoteConnectionService.this.findConferenceForAction(callId, "removeExtra").removeExtras(keys);
                }
            }

            public void onConnectionEvent(String callId, String event, Bundle extras) {
                if (RemoteConnectionService.this.mConnectionById.containsKey(callId)) {
                    RemoteConnectionService.this.findConnectionForAction(callId, "onConnectionEvent").onConnectionEvent(event, extras);
                }
            }

            public void setPhoneAccountHandle(String callId, PhoneAccountHandle pHandle) {
                RemoteConnectionService.this.findConnectionForAction(callId, "setPhoneAccountHandle").setPhoneAccountHandle(pHandle);
            }
        };
        this.mServant = new ConnectionServiceAdapterServant(this.mServantDelegate);
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                for (RemoteConnection c : RemoteConnectionService.this.mConnectionById.values()) {
                    c.setDestroyed();
                }
                for (RemoteConference c2 : RemoteConnectionService.this.mConferenceById.values()) {
                    c2.setDestroyed();
                }
                RemoteConnectionService.this.mConnectionById.clear();
                RemoteConnectionService.this.mConferenceById.clear();
                RemoteConnectionService.this.mPendingConnections.clear();
                RemoteConnectionService.this.mOutgoingConnectionServiceRpc.asBinder().unlinkToDeath(RemoteConnectionService.this.mDeathRecipient, 0);
            }
        };
        this.mConnectionById = new HashMap();
        this.mConferenceById = new HashMap();
        this.mPendingConnections = new HashSet();
        this.mOutgoingConnectionServiceRpc = outgoingConnectionServiceRpc;
        this.mOutgoingConnectionServiceRpc.asBinder().linkToDeath(this.mDeathRecipient, 0);
        this.mOurConnectionServiceImpl = ourConnectionServiceImpl;
    }

    public String toString() {
        return "[RemoteCS - " + this.mOutgoingConnectionServiceRpc.asBinder().toString() + "]";
    }

    final RemoteConnection createRemoteConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request, boolean isIncoming) {
        String id = UUID.randomUUID().toString();
        ConnectionRequest newRequest = new ConnectionRequest(request.getAccountHandle(), request.getAddress(), request.getExtras(), request.getVideoState());
        try {
            if (this.mConnectionById.isEmpty()) {
                this.mOutgoingConnectionServiceRpc.addConnectionServiceAdapter(this.mServant.getStub());
            }
            RemoteConnection connection = new RemoteConnection(id, this.mOutgoingConnectionServiceRpc, newRequest);
            this.mPendingConnections.add(connection);
            this.mConnectionById.put(id, connection);
            this.mOutgoingConnectionServiceRpc.createConnection(connectionManagerPhoneAccount, id, newRequest, isIncoming, false);
            connection.registerCallback(new AnonymousClass3(id));
            return connection;
        } catch (RemoteException e) {
            return RemoteConnection.failure(new DisconnectCause(1, e.toString()));
        }
    }

    private boolean hasConnection(String callId) {
        return this.mConnectionById.containsKey(callId);
    }

    private RemoteConnection findConnectionForAction(String callId, String action) {
        if (this.mConnectionById.containsKey(callId)) {
            return (RemoteConnection) this.mConnectionById.get(callId);
        }
        Log.w((Object) this, "%s - Cannot find Connection %s", action, callId);
        return NULL_CONNECTION;
    }

    private RemoteConference findConferenceForAction(String callId, String action) {
        if (this.mConferenceById.containsKey(callId)) {
            return (RemoteConference) this.mConferenceById.get(callId);
        }
        Log.w((Object) this, "%s - Cannot find Conference %s", action, callId);
        return NULL_CONFERENCE;
    }

    private void maybeDisconnectAdapter() {
        if (this.mConnectionById.isEmpty() && this.mConferenceById.isEmpty()) {
            try {
                this.mOutgoingConnectionServiceRpc.removeConnectionServiceAdapter(this.mServant.getStub());
            } catch (RemoteException e) {
            }
        }
    }
}
