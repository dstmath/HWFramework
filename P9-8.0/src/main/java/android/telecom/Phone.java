package android.telecom;

import android.os.Bundle;
import android.telecom.InCallService.VideoCall;
import android.util.ArrayMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
public final class Phone {
    private CallAudioState mCallAudioState;
    private final Map<String, Call> mCallByTelecomCallId = new ArrayMap();
    private final String mCallingPackage;
    private final List<Call> mCalls = new CopyOnWriteArrayList();
    private boolean mCanAddCall = true;
    private final InCallAdapter mInCallAdapter;
    private final List<Listener> mListeners = new CopyOnWriteArrayList();
    private final int mTargetSdkVersion;
    private final List<Call> mUnmodifiableCalls = Collections.unmodifiableList(this.mCalls);

    public static abstract class Listener {
        @Deprecated
        public void onAudioStateChanged(Phone phone, AudioState audioState) {
        }

        public void onCallAudioStateChanged(Phone phone, CallAudioState callAudioState) {
        }

        public void onBringToForeground(Phone phone, boolean showDialpad) {
        }

        public void onCallAdded(Phone phone, Call call) {
        }

        public void onCallRemoved(Phone phone, Call call) {
        }

        public void onCanAddCallChanged(Phone phone, boolean canAddCall) {
        }

        public void onSilenceRinger(Phone phone) {
        }
    }

    Phone(InCallAdapter adapter, String callingPackage, int targetSdkVersion) {
        this.mInCallAdapter = adapter;
        this.mCallingPackage = callingPackage;
        this.mTargetSdkVersion = targetSdkVersion;
    }

    final void internalAddCall(ParcelableCall parcelableCall) {
        Call call = new Call(this, parcelableCall.getId(), this.mInCallAdapter, parcelableCall.getState(), this.mCallingPackage, this.mTargetSdkVersion, parcelableCall.mIsActiveSub);
        this.mCallByTelecomCallId.put(parcelableCall.getId(), call);
        this.mCalls.add(call);
        checkCallTree(parcelableCall);
        call.internalUpdate(parcelableCall, this.mCallByTelecomCallId);
        fireCallAdded(call);
    }

    final void internalRemoveCall(Call call) {
        this.mCallByTelecomCallId.remove(call.internalGetCallId());
        this.mCalls.remove(call);
        VideoCall videoCall = call.getVideoCall();
        if (videoCall != null) {
            videoCall.destroy();
        }
        fireCallRemoved(call);
    }

    final void internalUpdateCall(ParcelableCall parcelableCall) {
        Call call = (Call) this.mCallByTelecomCallId.get(parcelableCall.getId());
        if (call != null) {
            checkCallTree(parcelableCall);
            call.internalUpdate(parcelableCall, this.mCallByTelecomCallId);
        }
    }

    final void internalSetPostDialWait(String telecomId, String remaining) {
        Call call = (Call) this.mCallByTelecomCallId.get(telecomId);
        if (call != null) {
            call.internalSetPostDialWait(remaining);
        }
    }

    final void internalCallAudioStateChanged(CallAudioState callAudioState) {
        if (!Objects.equals(this.mCallAudioState, callAudioState)) {
            this.mCallAudioState = callAudioState;
            fireCallAudioStateChanged(callAudioState);
        }
    }

    final Call internalGetCallByTelecomId(String telecomId) {
        return (Call) this.mCallByTelecomCallId.get(telecomId);
    }

    final void internalBringToForeground(boolean showDialpad) {
        fireBringToForeground(showDialpad);
    }

    final void internalSetCanAddCall(boolean canAddCall) {
        if (this.mCanAddCall != canAddCall) {
            this.mCanAddCall = canAddCall;
            fireCanAddCallChanged(canAddCall);
        }
    }

    final void internalSilenceRinger() {
        fireSilenceRinger();
    }

    final void internalOnConnectionEvent(String telecomId, String event, Bundle extras) {
        Call call = (Call) this.mCallByTelecomCallId.get(telecomId);
        if (call != null) {
            call.internalOnConnectionEvent(event, extras);
        }
    }

    final void internalOnRttUpgradeRequest(String callId, int requestId) {
        Call call = (Call) this.mCallByTelecomCallId.get(callId);
        if (call != null) {
            call.internalOnRttUpgradeRequest(requestId);
        }
    }

    final void internalOnRttInitiationFailure(String callId, int reason) {
        Call call = (Call) this.mCallByTelecomCallId.get(callId);
        if (call != null) {
            call.internalOnRttInitiationFailure(reason);
        }
    }

    final void destroy() {
        for (Call call : this.mCalls) {
            VideoCall videoCall = call.getVideoCall();
            if (videoCall != null) {
                videoCall.destroy();
            }
            if (call.getState() != 7) {
                call.internalSetDisconnected();
            }
        }
    }

    public final void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        if (listener != null) {
            this.mListeners.remove(listener);
        }
    }

    public final List<Call> getCalls() {
        return this.mUnmodifiableCalls;
    }

    public final boolean canAddCall() {
        return this.mCanAddCall;
    }

    public final void setMuted(boolean state) {
        this.mInCallAdapter.mute(state);
    }

    public final void setAudioRoute(int route) {
        this.mInCallAdapter.setAudioRoute(route);
    }

    public final void setBluetoothAudioRoute(String address) {
        this.mInCallAdapter.setBluetoothAudioRoute(address);
    }

    public final void setProximitySensorOn() {
        this.mInCallAdapter.turnProximitySensorOn();
    }

    public final void setProximitySensorOff(boolean screenOnImmediately) {
        this.mInCallAdapter.turnProximitySensorOff(screenOnImmediately);
    }

    public void switchToOtherActiveSub(String subId, boolean retainLch) {
        this.mInCallAdapter.switchToOtherActiveSub(subId, retainLch);
    }

    @Deprecated
    public final AudioState getAudioState() {
        return new AudioState(this.mCallAudioState);
    }

    public final CallAudioState getCallAudioState() {
        return this.mCallAudioState;
    }

    private void fireCallAdded(Call call) {
        for (Listener listener : this.mListeners) {
            listener.onCallAdded(this, call);
        }
    }

    private void fireCallRemoved(Call call) {
        for (Listener listener : this.mListeners) {
            listener.onCallRemoved(this, call);
        }
    }

    private void fireCallAudioStateChanged(CallAudioState audioState) {
        for (Listener listener : this.mListeners) {
            listener.onCallAudioStateChanged(this, audioState);
            listener.onAudioStateChanged(this, new AudioState(audioState));
        }
    }

    private void fireBringToForeground(boolean showDialpad) {
        for (Listener listener : this.mListeners) {
            listener.onBringToForeground(this, showDialpad);
        }
    }

    private void fireCanAddCallChanged(boolean canAddCall) {
        for (Listener listener : this.mListeners) {
            listener.onCanAddCallChanged(this, canAddCall);
        }
    }

    private void fireSilenceRinger() {
        for (Listener listener : this.mListeners) {
            listener.onSilenceRinger(this);
        }
    }

    private void checkCallTree(ParcelableCall parcelableCall) {
        if (parcelableCall.getChildCallIds() != null) {
            for (int i = 0; i < parcelableCall.getChildCallIds().size(); i++) {
                if (!this.mCallByTelecomCallId.containsKey(parcelableCall.getChildCallIds().get(i))) {
                    Log.wtf((Object) this, "ParcelableCall %s has nonexistent child %s", parcelableCall.getId(), parcelableCall.getChildCallIds().get(i));
                }
            }
        }
    }
}
