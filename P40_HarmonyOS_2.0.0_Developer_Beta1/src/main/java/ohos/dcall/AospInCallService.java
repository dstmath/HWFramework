package ohos.dcall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AospInCallService extends InCallService {
    private static final int CAAS_VOIP_NUMBER_WITHOUT_PLUS_SIGN_LENGTH = 14;
    private static final int CAAS_VOIP_NUMBER_WITH_PLUS_SIGN_LENGTH = 15;
    static final int CALL_TYPE_CARRIER = 0;
    static final int CALL_TYPE_HICALL = 1;
    static final int CALL_TYPE_UNKNOWN = -1;
    private static final String EXTRA_IS_HICALL = "extra_is_hicall";
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final String PREFIX_CAAS_VOIP_NUMBER_WITHOUT_PLUS_SIGN = "887";
    private static final String PREFIX_CAAS_VOIP_NUMBER_WITH_PLUS_SIGN = "+887";
    private static final String TAG = "AospInCallService";
    private CallAppAbilityProxy mProxy;
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

    @Override // android.telecom.InCallService, android.app.Service
    public IBinder onBind(Intent intent) {
        HiLog.info(LOG_LABEL, "onBind", new Object[0]);
        AospCallAdapter.getInstance().setAospInCallService(this);
        this.mProxy = CallAppAbilityProxy.getInstance();
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.setContext(this);
            this.mProxy.onInCallServiceBind();
        }
        return super.onBind(intent);
    }

    @Override // android.telecom.InCallService, android.app.Service
    public boolean onUnbind(Intent intent) {
        HiLog.info(LOG_LABEL, "onUnbind", new Object[0]);
        AospCallAdapter.getInstance().setAospInCallService(null);
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        if (callAppAbilityProxy != null) {
            callAppAbilityProxy.onInCallServiceUnbind();
            this.mProxy = null;
        }
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

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000f: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r2v0 java.lang.String) */
    @Override // android.telecom.InCallService
    public void onCallAdded(Call call) {
        HiLogLabel hiLogLabel = LOG_LABEL;
        Object[] objArr = new Object[1];
        objArr[0] = call == null ? "" : call.toString();
        HiLog.info(hiLogLabel, "onCallAdded: call = %{public}s", objArr);
        if (call != null) {
            call.registerCallback(this.mTelecomCallCallback);
            CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
            if (callAppAbilityProxy != null) {
                callAppAbilityProxy.onCallAdded(call);
            }
        }
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

    /* access modifiers changed from: package-private */
    public boolean answer(int i, int i2) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.answer(i2);
            return true;
        }
        HiLog.error(LOG_LABEL, "answer, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean disconnect(int i) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.disconnect();
            return true;
        }
        HiLog.error(LOG_LABEL, "disconnectCall, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        CallAppAbilityProxy callAppAbilityProxy2 = this.mProxy;
        if (callAppAbilityProxy2 != null) {
            return callAppAbilityProxy2.disconnectPreAddedCall(i);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean playDtmfTone(int i, char c) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.playDtmfTone(c);
            return true;
        }
        HiLog.error(LOG_LABEL, "playDtmfTone, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean stopDtmfTone(int i) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.stopDtmfTone();
            return true;
        }
        HiLog.error(LOG_LABEL, "stopDtmfTone, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean postDialContinue(int i, boolean z) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.postDialContinue(z);
            return true;
        }
        HiLog.error(LOG_LABEL, "postDialContinue, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean reject(int i, boolean z, String str) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.reject(z, str);
            return true;
        }
        HiLog.error(LOG_LABEL, "rejectCall, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hold(int i) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.hold();
            return true;
        }
        HiLog.error(LOG_LABEL, "hold, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean unHold(int i) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.unhold();
            return true;
        }
        HiLog.error(LOG_LABEL, "unhold, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    /* access modifiers changed from: package-private */
    public List<String> getPredefinedRejectMessages(int i) {
        ArrayList arrayList = new ArrayList();
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            return callById.getCannedTextResponses();
        }
        HiLog.error(LOG_LABEL, "getPredefinedRejectMessages, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public boolean sendCallEvent(int i, String str, Bundle bundle) {
        CallAppAbilityProxy callAppAbilityProxy = this.mProxy;
        Call callById = callAppAbilityProxy != null ? callAppAbilityProxy.getCallById(Integer.valueOf(i)) : null;
        if (callById != null) {
            callById.sendCallEvent(str, bundle);
            return true;
        }
        HiLog.error(LOG_LABEL, "sendCallEvent, no matched call, callId is %{public}d.", new Object[]{Integer.valueOf(i)});
        return false;
    }

    static boolean isCaasVoipCall(Call call) {
        if (call == null) {
            return false;
        }
        boolean isCaasVoipCallNumber = isCaasVoipCallNumber(getNumber(call));
        Bundle intentExtras = getIntentExtras(call);
        if (intentExtras != null) {
            isCaasVoipCallNumber |= intentExtras.getBoolean(EXTRA_IS_HICALL);
        }
        HiLog.info(LOG_LABEL, "isCaasVoipCall: %{public}b", new Object[]{Boolean.valueOf(isCaasVoipCallNumber)});
        return isCaasVoipCallNumber;
    }

    static int getCallType(Call call) {
        if (call == null) {
            return -1;
        }
        if (isCaasVoipCall(call)) {
            HiLog.info(LOG_LABEL, "getCallType: CALL_TYPE_HICALL", new Object[0]);
            return 1;
        }
        HiLog.info(LOG_LABEL, "getCallType: CALL_TYPE_CARRIER", new Object[0]);
        return 0;
    }

    static String getNumber(Call call) {
        if (call == null) {
            return null;
        }
        if (call.getDetails() != null && call.getDetails().getGatewayInfo() != null && call.getDetails().getGatewayInfo().getOriginalAddress() != null) {
            return call.getDetails().getGatewayInfo().getOriginalAddress().getSchemeSpecificPart();
        }
        Uri handle = getHandle(call);
        if (handle == null) {
            return null;
        }
        return handle.getSchemeSpecificPart();
    }

    static boolean isCaasVoipCallNumber(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int indexOf = str.indexOf(PREFIX_CAAS_VOIP_NUMBER_WITH_PLUS_SIGN);
        int length = str.length();
        int indexOf2 = str.indexOf(PREFIX_CAAS_VOIP_NUMBER_WITHOUT_PLUS_SIGN);
        if (indexOf != 0 && indexOf2 != 0) {
            return false;
        }
        if (length == 15 || length == 14) {
            return true;
        }
        return false;
    }

    static Uri getHandle(Call call) {
        if (call == null || call.getDetails() == null) {
            return null;
        }
        return call.getDetails().getHandle();
    }

    static Bundle getIntentExtras(Call call) {
        if (call == null || call.getDetails() == null) {
            return null;
        }
        Bundle intentExtras = call.getDetails().getIntentExtras();
        if (areCallExtrasCorrupted(intentExtras)) {
            return null;
        }
        return intentExtras;
    }

    static boolean areCallExtrasCorrupted(Bundle bundle) {
        if (bundle == null) {
            return true;
        }
        try {
            bundle.containsKey("android.telecom.extra.CHILD_ADDRESS");
            return false;
        } catch (IllegalArgumentException unused) {
            HiLog.error(LOG_LABEL, "CallExtras is corrupted, ignoring exception.", new Object[0]);
            return true;
        }
    }
}
