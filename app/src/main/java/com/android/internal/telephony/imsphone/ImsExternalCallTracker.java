package com.android.internal.telephony.imsphone;

import android.util.ArrayMap;
import android.util.Log;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsExternalCallState;
import com.android.ims.ImsExternalCallStateListener;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.imsphone.ImsExternalConnection.Listener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ImsExternalCallTracker {
    public static final String EXTRA_IMS_EXTERNAL_CALL_ID = "android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID";
    public static final String TAG = "ImsExternalCallTracker";
    private final ImsPullCall mCallPuller;
    private final ExternalCallStateListener mExternalCallStateListener;
    private final ExternalConnectionListener mExternalConnectionListener;
    private Map<Integer, ImsExternalConnection> mExternalConnections;
    private final ImsPhone mPhone;

    public class ExternalCallStateListener extends ImsExternalCallStateListener {
        public void onImsExternalCallStateUpdate(List<ImsExternalCallState> externalCallState) {
            ImsExternalCallTracker.this.refreshExternalCallState(externalCallState);
        }
    }

    public class ExternalConnectionListener implements Listener {
        public void onPullExternalCall(ImsExternalConnection connection) {
            Log.d(ImsExternalCallTracker.TAG, "onPullExternalCall: connection = " + connection);
            ImsExternalCallTracker.this.mCallPuller.pullExternalCall(connection.getAddress(), connection.getVideoState());
        }
    }

    public ImsExternalCallTracker(ImsPhone phone, ImsPullCall callPuller) {
        this.mExternalConnections = new ArrayMap();
        this.mExternalConnectionListener = new ExternalConnectionListener();
        this.mPhone = phone;
        this.mExternalCallStateListener = new ExternalCallStateListener();
        this.mCallPuller = callPuller;
    }

    public ExternalCallStateListener getExternalCallStateListener() {
        return this.mExternalCallStateListener;
    }

    public void refreshExternalCallState(List<ImsExternalCallState> externalCallStates) {
        Log.d(TAG, "refreshExternalCallState: depSize = " + externalCallStates.size());
        Iterator<Entry<Integer, ImsExternalConnection>> connectionIterator = this.mExternalConnections.entrySet().iterator();
        boolean wasCallRemoved = false;
        while (connectionIterator.hasNext()) {
            Entry<Integer, ImsExternalConnection> entry = (Entry) connectionIterator.next();
            if (!containsCallId(externalCallStates, ((Integer) entry.getKey()).intValue())) {
                ImsExternalConnection externalConnection = (ImsExternalConnection) entry.getValue();
                externalConnection.setTerminated();
                externalConnection.removeListener(this.mExternalConnectionListener);
                connectionIterator.remove();
                wasCallRemoved = true;
            }
        }
        if (wasCallRemoved) {
            this.mPhone.notifyPreciseCallStateChanged();
        }
        for (ImsExternalCallState callState : externalCallStates) {
            if (this.mExternalConnections.containsKey(Integer.valueOf(callState.getCallId()))) {
                updateExistingConnection((ImsExternalConnection) this.mExternalConnections.get(Integer.valueOf(callState.getCallId())), callState);
            } else {
                Log.d(TAG, "refreshExternalCallState: got = " + callState);
                if (callState.getCallState() == 1) {
                    createExternalConnection(callState);
                }
            }
        }
    }

    public Connection getConnectionById(int callId) {
        return (Connection) this.mExternalConnections.get(Integer.valueOf(callId));
    }

    private void createExternalConnection(ImsExternalCallState state) {
        Log.i(TAG, "createExternalConnection");
        ImsExternalConnection connection = new ImsExternalConnection(this.mPhone, state.getCallId(), state.getAddress().getSchemeSpecificPart(), state.isCallPullable());
        connection.setVideoState(ImsCallProfile.getVideoStateFromCallType(state.getCallType()));
        connection.addListener(this.mExternalConnectionListener);
        this.mExternalConnections.put(Integer.valueOf(connection.getCallId()), connection);
        this.mPhone.notifyUnknownConnection(connection);
    }

    private void updateExistingConnection(ImsExternalConnection connection, ImsExternalCallState state) {
        State existingState = connection.getState();
        State newState = state.getCallState() == 1 ? State.ACTIVE : State.DISCONNECTED;
        if (existingState != newState) {
            if (newState == State.ACTIVE) {
                connection.setActive();
            } else {
                connection.setTerminated();
                connection.removeListener(this.mExternalConnectionListener);
                this.mExternalConnections.remove(connection);
                this.mPhone.notifyPreciseCallStateChanged();
            }
        }
        connection.setIsPullable(state.isCallPullable());
        int newVideoState = ImsCallProfile.getVideoStateFromCallType(state.getCallType());
        if (newVideoState != connection.getVideoState()) {
            connection.setVideoState(newVideoState);
        }
    }

    private boolean containsCallId(List<ImsExternalCallState> externalCallStates, int callId) {
        for (ImsExternalCallState state : externalCallStates) {
            if (state.getCallId() == callId) {
                return true;
            }
        }
        return false;
    }
}
