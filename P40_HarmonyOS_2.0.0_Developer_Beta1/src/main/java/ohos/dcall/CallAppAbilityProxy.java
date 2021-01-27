package ohos.dcall;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.util.ArrayMap;
import java.util.Map;
import ohos.dcall.CallAppAbilityConnection;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class CallAppAbilityProxy {
    private static final Integer INVALID_KEY_SET_IN_MAP = -1;
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final String OHOS_CALL_ABILITY_NAME = ".framework.core.DCallServiceAbility";
    private static final String OHOS_CALL_COMPONENT_NAME = "com.huawei.ohos.call";
    private static final String OHOS_HICALL_ABILITY_NAME = ".hicallui.framework.core.DCallServiceAbility";
    private static final String OHOS_HICALL_COMPONENT_NAME = "com.huawei.ohos.meetime";
    private static final String TAG = "CallAppAbilityProxy";
    private static volatile CallAppAbilityProxy sInstance;
    private final Map<Integer, CallAppAbilityConnection> mCallAppAbilityConnectionMap = new ArrayMap();
    private Context mContext;
    private CallAppAbilityConnection.Listener mListener = new CallAppAbilityConnection.Listener() {
        /* class ohos.dcall.CallAppAbilityProxy.AnonymousClass2 */

        @Override // ohos.dcall.CallAppAbilityConnection.Listener
        public void onConnected(CallAppAbilityConnection callAppAbilityConnection) {
        }

        @Override // ohos.dcall.CallAppAbilityConnection.Listener
        public void onDisconnected(CallAppAbilityConnection callAppAbilityConnection) {
        }

        @Override // ohos.dcall.CallAppAbilityConnection.Listener
        public void onReleased(CallAppAbilityConnection callAppAbilityConnection) {
            if (callAppAbilityConnection != null && callAppAbilityConnection.getInfo() != null) {
                HiLog.info(CallAppAbilityProxy.LOG_LABEL, "Connection is released %{public}s.", new Object[]{callAppAbilityConnection.toString()});
                CallAppAbilityProxy.this.mCallAppAbilityConnectionMap.remove(Integer.valueOf(callAppAbilityConnection.getInfo().getType()));
            }
        }
    };
    private final SyncLock mLock = new SyncLock() {
        /* class ohos.dcall.CallAppAbilityProxy.AnonymousClass1 */
    };

    /* access modifiers changed from: package-private */
    public interface SyncLock {
    }

    static CallAppAbilityProxy getInstance() {
        if (sInstance == null) {
            synchronized (CallAppAbilityProxy.class) {
                if (sInstance == null) {
                    HiLog.info(LOG_LABEL, "new CallAppAbilityProxy", new Object[0]);
                    sInstance = new CallAppAbilityProxy();
                }
            }
        }
        return sInstance;
    }

    private CallAppAbilityProxy() {
    }

    /* access modifiers changed from: package-private */
    public void setContext(Context context) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
    }

    /* access modifiers changed from: package-private */
    public void onInCallServiceBind() {
        HiLog.info(LOG_LABEL, "onInCallServiceBind", new Object[0]);
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null) {
                callAppAbilityConnection.onInCallServiceBind();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onInCallServiceUnbind() {
        HiLog.info(LOG_LABEL, "onInCallServiceUnbind", new Object[0]);
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null) {
                callAppAbilityConnection.onInCallServiceUnbind();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Call getCallById(Integer num) {
        Call callById;
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (!(callAppAbilityConnection == null || (callById = callAppAbilityConnection.getCallById(num)) == null)) {
                return callById;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean onCallAudioStateChanged(CallAudioState callAudioState) {
        if (callAudioState == null) {
            return false;
        }
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null) {
                callAppAbilityConnection.onCallAudioStateChanged(callAudioState);
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean onCallAdded(Call call) {
        if (call == null) {
            HiLog.error(LOG_LABEL, "onCallAdded fail, no call.", new Object[0]);
            return false;
        }
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onCallAdded(call);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean preOnCallAdded(String str, int i, int i2, String str2, String str3) {
        CallAppAbilityConnection connectionByCallType = getConnectionByCallType(i2);
        if (connectionByCallType != null) {
            return connectionByCallType.preOnCallAdded(str, i, str2, str3);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean disconnectPreAddedCall(int i) {
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null && callAppAbilityConnection.disconnectPreAddedCall(i)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean preConnectCallAppAbility(int i, String str, String str2) {
        CallAppAbilityConnection connectionByCallType = getConnectionByCallType(i);
        if (connectionByCallType != null) {
            return connectionByCallType.preConnectCallAppAbility(str, str2);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean preDisconnectCallAppAbility(int i, String str, String str2) {
        CallAppAbilityConnection connectionByCallType = getConnectionByCallType(i);
        if (connectionByCallType != null) {
            return connectionByCallType.preDisconnectCallAppAbility(str, str2);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onCallRemoved(Call call) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onCallRemoved(call);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onCanAddCallChanged(boolean z) {
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null) {
                callAppAbilityConnection.onCanAddCallChanged(z);
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean onSilenceRinger() {
        for (CallAppAbilityConnection callAppAbilityConnection : this.mCallAppAbilityConnectionMap.values()) {
            if (callAppAbilityConnection != null) {
                callAppAbilityConnection.onSilenceRinger();
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean onStateChanged(Call call, int i) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onStateChanged(call, i);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onDetailsChanged(Call call, Call.Details details) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onDetailsChanged(call, details);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onPostDialWait(Call call, String str) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onPostDialWait(call, str);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onCallDestroyed(Call call) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onCallDestroyed(call);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onConnectionEvent(Call call, String str, Bundle bundle) {
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.onConnectionEvent(call, str, bundle);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public Integer getIdByCall(Call call) {
        if (call == null) {
            HiLog.info(LOG_LABEL, "getIdByCall fail, call is null.", new Object[0]);
            return INVALID_KEY_SET_IN_MAP;
        }
        CallAppAbilityConnection connectionByCall = getConnectionByCall(call);
        if (connectionByCall != null) {
            return connectionByCall.getIdByCall(call);
        }
        HiLog.info(LOG_LABEL, "getIdByCall fail, call is not in map.", new Object[0]);
        return INVALID_KEY_SET_IN_MAP;
    }

    /* access modifiers changed from: package-private */
    public CallAppAbilityInfo getDefaultCallAppAbilityInfoByCallType(int i) {
        HiLog.info(LOG_LABEL, "getDefaultCallAppAbilityInfoByCallType:call type is %{public}d.", new Object[]{Integer.valueOf(i)});
        if (i == 1) {
            return new CallAppAbilityInfo(getDefaultHiCallComponentName(), 1);
        }
        if (i == 0) {
            return new CallAppAbilityInfo(getDefaultCarrierCallComponentName(), 0);
        }
        HiLog.info(LOG_LABEL, "getDefaultCallAppAbilityInfoByCallType: unknown call type.", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public CallAppAbilityConnection getConnectionByCall(Call call) {
        return getConnectionByCallType(AospInCallService.getCallType(call));
    }

    /* access modifiers changed from: package-private */
    public CallAppAbilityConnection getConnectionByCallType(int i) {
        CallAppAbilityConnection callAppAbilityConnection;
        HiLog.info(LOG_LABEL, "getConnectionByCallType, call type is %{public}d.", new Object[]{Integer.valueOf(i)});
        for (Map.Entry<Integer, CallAppAbilityConnection> entry : this.mCallAppAbilityConnectionMap.entrySet()) {
            Integer key = entry.getKey();
            HiLog.info(LOG_LABEL, "getConnectionByCallType: type %{public}d.", new Object[]{key});
            if (key.intValue() == i && (callAppAbilityConnection = this.mCallAppAbilityConnectionMap.get(key)) != null) {
                return callAppAbilityConnection;
            }
        }
        CallAppAbilityInfo defaultCallAppAbilityInfoByCallType = getDefaultCallAppAbilityInfoByCallType(i);
        if (defaultCallAppAbilityInfoByCallType == null) {
            return null;
        }
        CallAppAbilityConnection callAppAbilityConnection2 = new CallAppAbilityConnection(this.mContext, this.mLock, defaultCallAppAbilityInfoByCallType);
        callAppAbilityConnection2.setListener(this.mListener);
        this.mCallAppAbilityConnectionMap.put(Integer.valueOf(i), callAppAbilityConnection2);
        return callAppAbilityConnection2;
    }

    /* access modifiers changed from: package-private */
    public ComponentName getDefaultCarrierCallComponentName() {
        return new ComponentName(OHOS_CALL_COMPONENT_NAME, "com.huawei.ohos.call.framework.core.DCallServiceAbility");
    }

    /* access modifiers changed from: package-private */
    public ComponentName getDefaultHiCallComponentName() {
        return new ComponentName(OHOS_HICALL_COMPONENT_NAME, "com.huawei.ohos.meetime.hicallui.framework.core.DCallServiceAbility");
    }
}
