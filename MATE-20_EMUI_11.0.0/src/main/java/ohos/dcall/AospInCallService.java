package ohos.dcall;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.util.ArrayMap;
import com.huawei.ohos.interwork.AbilityUtils;
import java.util.Iterator;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AospInCallService extends InCallService {
    private static final String HARMONYOS_CALL_ABILITY_NAME = ".core.DCallServiceAbility";
    private static final String HARMONYOS_CALL_COMPONENT_NAME = "com.huawei.ohos.call";
    private static final Integer INVALID_KEY_SET_IN_MAP = -1;
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final int MAXIMUM_CALLS = 32;
    private static final String TAG = "AospInCallService";
    private Integer mCallId = 1;
    private final Map<Integer, Call> mCallMapById = new ArrayMap();
    private Call mPendingAddCall;
    private CallAppAbilityProxy mProxy;
    private ServiceConnection mServiceConnection;
    private final Call.Callback mTelecomCallCallback = new Call.Callback() {
        /* class ohos.dcall.AospInCallService.AnonymousClass1 */

        @Override // android.telecom.Call.Callback
        public void onStateChanged(Call call, int i) {
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy.onStateChanged(call, i);
            }
        }

        @Override // android.telecom.Call.Callback
        public void onDetailsChanged(Call call, Call.Details details) {
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy.onDetailsChanged(call, details);
            }
        }

        @Override // android.telecom.Call.Callback
        public void onPostDialWait(Call call, String str) {
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy.onPostDialWait(call, str);
            }
        }

        @Override // android.telecom.Call.Callback
        public void onCallDestroyed(Call call) {
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy.onCallDestroyed(call);
            }
        }

        @Override // android.telecom.Call.Callback
        public void onConnectionEvent(Call call, String str, Bundle bundle) {
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy.onConnectionEvent(call, str, bundle);
            }
        }
    };

    public Integer getIdByCall(Call call) {
        if (call == null) {
            HiLog.info(LOG_LABEL, "getIdByCall fail, call is null.", new Object[0]);
            return INVALID_KEY_SET_IN_MAP;
        }
        Integer num = INVALID_KEY_SET_IN_MAP;
        Iterator<Integer> it = this.mCallMapById.keySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Integer next = it.next();
            if (call.equals(getCallById(next))) {
                num = next;
                break;
            }
        }
        if (num == INVALID_KEY_SET_IN_MAP) {
            HiLog.error(LOG_LABEL, "getIdByCall fail, call is not in map.", new Object[0]);
        }
        return num;
    }

    @Override // android.telecom.InCallService, android.app.Service
    public IBinder onBind(Intent intent) {
        HiLog.info(LOG_LABEL, "onBind", new Object[0]);
        AospCallAdapter.getInstance().setAospInCallService(this);
        return super.onBind(intent);
    }

    @Override // android.telecom.InCallService, android.app.Service
    public boolean onUnbind(Intent intent) {
        HiLog.info(LOG_LABEL, "onUnbind", new Object[0]);
        AospCallAdapter.getInstance().setAospInCallService(null);
        this.mCallMapById.clear();
        disconnectInCallAbility();
        return super.onUnbind(intent);
    }

    @Override // android.telecom.InCallService
    public void onCallAudioStateChanged(CallAudioState callAudioState) {
        HiLog.info(LOG_LABEL, "onCallAudioStateChanged", new Object[0]);
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.onCallAudioStateChanged(callAudioState);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000f: APUT  (r2v0 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v0 java.lang.String) */
    @Override // android.telecom.InCallService
    public void onCallAdded(Call call) {
        HiLogLabel hiLogLabel = LOG_LABEL;
        Object[] objArr = new Object[1];
        objArr[0] = call == null ? "" : call.toString();
        HiLog.info(hiLogLabel, "onCallAdded: call = %{public}s", objArr);
        if (call == null) {
            return;
        }
        if (this.mCallMapById.size() >= 32) {
            HiLog.error(LOG_LABEL, "onCallAdded: reached the maximum number of calls.", new Object[0]);
            return;
        }
        this.mCallMapById.put(this.mCallId, call);
        this.mCallId = Integer.valueOf(this.mCallId.intValue() + 1);
        call.registerCallback(this.mTelecomCallCallback);
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.onCallAdded(call);
            return;
        }
        this.mPendingAddCall = call;
        connectInCallAbility();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000f: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r2v0 java.lang.String) */
    @Override // android.telecom.InCallService
    public void onCallRemoved(Call call) {
        HiLogLabel hiLogLabel = LOG_LABEL;
        Object[] objArr = new Object[1];
        objArr[0] = call == null ? "" : call.toString();
        HiLog.info(hiLogLabel, "onCallRemoved: call = %{public}s", objArr);
        if (call != null) {
            call.unregisterCallback(this.mTelecomCallCallback);
            CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
            if (callAppAbilityProxy != null) {
                callAppAbilityProxy.onCallRemoved(call);
            }
            removeCallFromCallMap(call);
        }
    }

    @Override // android.telecom.InCallService
    public void onCanAddCallChanged(boolean z) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.onCanAddCallChanged(z);
        }
    }

    @Override // android.telecom.InCallService
    public void onSilenceRinger() {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.onSilenceRinger();
        }
    }

    private void connectInCallAbility() {
        HiLog.info(LOG_LABEL, "connectInCallAbility", new Object[0]);
        if (this.mServiceConnection == null) {
            this.mServiceConnection = new AbilityConnection();
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HARMONYOS_CALL_COMPONENT_NAME, "com.huawei.ohos.call.core.DCallServiceAbility"));
        try {
            AbilityUtils.connectAbility(this, intent, this.mServiceConnection);
        } catch (IllegalArgumentException unused) {
            HiLog.error(LOG_LABEL, "connectInCallAbility got IllegalArgumentException.", new Object[0]);
        } catch (SecurityException unused2) {
            HiLog.error(LOG_LABEL, "connectInCallAbility got SecurityException.", new Object[0]);
        } catch (IllegalStateException unused3) {
            HiLog.error(LOG_LABEL, "connectInCallAbility got IllegalStateException.", new Object[0]);
        }
    }

    private void disconnectInCallAbility() {
        HiLog.info(LOG_LABEL, "disconnectInCallAbility", new Object[0]);
        ServiceConnection serviceConnection = this.mServiceConnection;
        if (serviceConnection == null) {
            HiLog.info(LOG_LABEL, "no need disconnectInCallAbility", new Object[0]);
            return;
        }
        try {
            AbilityUtils.disconnectAbility(this, serviceConnection);
        } catch (IllegalArgumentException unused) {
            HiLog.error(LOG_LABEL, "disconnectInCallAbility got IllegalArgumentException.", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPendingCallToProxy(IBinder iBinder) {
        HiLog.info(LOG_LABEL, "addPendingCallToProxy", new Object[0]);
        if (this.mProxy == null) {
            this.mProxy = new CallAppAbilityProxy(this, iBinder);
        }
        Call call = this.mPendingAddCall;
        if (call != null) {
            this.mProxy.onCallAdded(call);
            this.mPendingAddCall = null;
        }
    }

    /* access modifiers changed from: package-private */
    public class AbilityConnection implements ServiceConnection {
        AbilityConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.info(AospInCallService.LOG_LABEL, "onServiceConnected", new Object[0]);
            AospInCallService.this.addPendingCallToProxy(iBinder);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            HiLog.info(AospInCallService.LOG_LABEL, "onServiceDisconnected", new Object[0]);
            if (AospInCallService.this.mProxy != null) {
                AospInCallService.this.mProxy = null;
            }
        }
    }

    public void answer(int i, int i2) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "answer, callId is %{public}d, videoState is %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
            callById.answer(i2);
            return;
        }
        HiLog.error(LOG_LABEL, "answer, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    public void disconnect(int i) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "disconnectCall, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
            callById.disconnect();
            return;
        }
        HiLog.error(LOG_LABEL, "disconnectCall, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    public void playDtmfTone(int i, char c) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "playDtmfTone, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
            callById.playDtmfTone(c);
            return;
        }
        HiLog.error(LOG_LABEL, "playDtmfTone, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    public void stopDtmfTone(int i) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "stopDtmfTone, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
            callById.stopDtmfTone();
            return;
        }
        HiLog.error(LOG_LABEL, "stopDtmfTone, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    public void postDialContinue(int i, boolean z) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "postDialContinue, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
            callById.postDialContinue(z);
            return;
        }
        HiLog.error(LOG_LABEL, "postDialContinue, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    public void reject(int i, boolean z, String str) {
        Call callById = getCallById(Integer.valueOf(i));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "rejectCall, callId is %{public}d, rejectWithMessage is %{public}d.", new Object[]{Integer.valueOf(i), Boolean.valueOf(z)});
            callById.reject(z, str);
            return;
        }
        HiLog.error(LOG_LABEL, "rejectCall, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
    }

    private Call getCallById(Integer num) {
        return this.mCallMapById.get(num);
    }

    private void removeCallFromCallMap(Call call) {
        Integer idByCall = getIdByCall(call);
        if (idByCall != INVALID_KEY_SET_IN_MAP) {
            HiLog.info(LOG_LABEL, "removeCallFromCallMap success.", new Object[0]);
            this.mCallMapById.remove(idByCall);
            return;
        }
        HiLog.error(LOG_LABEL, "removeCallFromCallMap fail, call is not in map.", new Object[0]);
    }
}
