package ohos.dcall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.util.ArrayMap;
import com.huawei.ohos.interwork.AbilityUtils;
import java.util.Iterator;
import java.util.Map;
import ohos.dcall.CallAppAbilityProxy;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class CallAppAbilityConnection {
    private static final int CONNECTION_FAILED = 2;
    private static final int CONNECTION_NOT_SUPPORTED = 3;
    private static final int CONNECTION_SUCCEEDED = 1;
    private static final long DELAYED_TIME_DISCONNECT_PRE_ADDED_CALL = 35000;
    private static final long DELAYED_TIME_RETRY_CONNECT_INCALL_ABILITY = 500;
    private static final int EVENT_DISCONNECT_PRE_ADDED_CALL = 1001;
    private static final int EVENT_RETRY_CONNECT_INCALL_ABILITY = 1000;
    private static final Integer INVALID_KEY_SET_IN_MAP = -1;
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final int MAXIMUM_CALLS = 32;
    private static final int MAX_COUNTS_RETRY_CONNECT_INCALL_ABILITY = 6;
    private static final String TAG = "CallAppAbilityConnection";
    private static int sCallId = 1;
    private final Map<Integer, Call> mCallMapById = new ArrayMap();
    private Context mContext;
    private CallAudioState mCurrentCallAudioState;
    private Boolean mCurrentCanAddCall;
    private CallAppAbilityConnnectionHandler mHandler;
    private final CallAppAbilityInfo mInfo;
    private boolean mIsBound;
    private boolean mIsConnected;
    private boolean mIsInCallServiceBinded;
    private boolean mIsPreConnected;
    private Listener mListener;
    private final CallAppAbilityProxy.SyncLock mLock;
    private CallAudioState mPendingCallAudioState;
    private Boolean mPendingCanAddCall;
    private PreAddedCall mPendingDisconnectPreAddedCall;
    private PreAddedCall mPreAddedCall;
    private IBinder mRemote;
    private int mRetryConnectCallAppAbilityCount;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class ohos.dcall.CallAppAbilityConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            synchronized (CallAppAbilityConnection.this.mLock) {
                HiLog.info(CallAppAbilityConnection.LOG_LABEL, "onServiceConnected: %{public}s, mIsBound is %{public}b, mIsConnected is %{public}b.", new Object[]{componentName, Boolean.valueOf(CallAppAbilityConnection.this.mIsBound), Boolean.valueOf(CallAppAbilityConnection.this.mIsConnected)});
                CallAppAbilityConnection.this.mIsBound = true;
                if (CallAppAbilityConnection.this.mIsConnected) {
                    CallAppAbilityConnection.this.onConnected(componentName, iBinder);
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            synchronized (CallAppAbilityConnection.this.mLock) {
                HiLog.info(CallAppAbilityConnection.LOG_LABEL, "onServiceConnected: %{public}s.", new Object[]{componentName});
                CallAppAbilityConnection.this.mIsBound = false;
                CallAppAbilityConnection.this.onDisconnected(componentName);
            }
        }
    };

    /* access modifiers changed from: package-private */
    public interface Listener {
        void onConnected(CallAppAbilityConnection callAppAbilityConnection);

        void onDisconnected(CallAppAbilityConnection callAppAbilityConnection);

        void onReleased(CallAppAbilityConnection callAppAbilityConnection);
    }

    public CallAppAbilityConnection(Context context, CallAppAbilityProxy.SyncLock syncLock, CallAppAbilityInfo callAppAbilityInfo) {
        this.mContext = context;
        this.mLock = syncLock;
        this.mInfo = callAppAbilityInfo;
    }

    public String toString() {
        CallAppAbilityInfo callAppAbilityInfo = this.mInfo;
        return callAppAbilityInfo != null ? callAppAbilityInfo.toString() : "";
    }

    /* access modifiers changed from: package-private */
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    public int connect() {
        if (this.mIsConnected) {
            HiLog.info(LOG_LABEL, "Already connected, ignoring request.", new Object[0]);
            return 1;
        }
        CallAppAbilityInfo callAppAbilityInfo = this.mInfo;
        if (callAppAbilityInfo == null || callAppAbilityInfo.getComponentName() == null) {
            HiLog.error(LOG_LABEL, "no component to connect.", new Object[0]);
            return 2;
        }
        HiLog.info(LOG_LABEL, "Attempting to connect %{public}s.", new Object[]{this.mInfo});
        this.mIsConnected = true;
        if (connectCallAppAbility(this.mInfo.getComponentName())) {
            return 1;
        }
        HiLog.error(LOG_LABEL, "Failed to connect.", new Object[0]);
        this.mIsConnected = false;
        return 2;
    }

    /* access modifiers changed from: package-private */
    public CallAppAbilityInfo getInfo() {
        return this.mInfo;
    }

    /* access modifiers changed from: package-private */
    public void disconnect() {
        if (this.mIsConnected) {
            disconnectCallAppAbility();
            this.mRemote = null;
            this.mIsConnected = false;
            return;
        }
        HiLog.error(LOG_LABEL, "Already disconnected, ignoring request.", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void releaseResource() {
        if (!isPreConnected() && !isInCallServiceBinded() && this.mCallMapById.isEmpty() && this.mPreAddedCall == null && this.mPendingDisconnectPreAddedCall == null) {
            release();
        }
    }

    /* access modifiers changed from: package-private */
    public void release() {
        HiLog.info(LOG_LABEL, "release.", new Object[0]);
        this.mCallMapById.clear();
        this.mRemote = null;
        disconnect();
        destroyHandler();
        this.mContext = null;
        this.mPendingCallAudioState = null;
        this.mCurrentCallAudioState = null;
        this.mPendingCanAddCall = null;
        this.mPreAddedCall = null;
        this.mPendingDisconnectPreAddedCall = null;
        this.mIsConnected = false;
        this.mIsBound = false;
        this.mIsInCallServiceBinded = false;
        this.mIsPreConnected = false;
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onReleased(this);
            this.mListener = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isConnected() {
        HiLog.info(LOG_LABEL, "isConnected: %{public}b", new Object[]{Boolean.valueOf(this.mIsConnected)});
        return this.mIsConnected;
    }

    /* access modifiers changed from: package-private */
    public boolean isInCallServiceBinded() {
        HiLog.info(LOG_LABEL, "isInCallServiceBinded: %{public}b", new Object[]{Boolean.valueOf(this.mIsInCallServiceBinded)});
        return this.mIsInCallServiceBinded;
    }

    /* access modifiers changed from: package-private */
    public boolean isPreConnected() {
        HiLog.info(LOG_LABEL, "isPreConnected: %{public}b", new Object[]{Boolean.valueOf(this.mIsPreConnected)});
        return this.mIsPreConnected;
    }

    /* access modifiers changed from: package-private */
    public void onConnected(ComponentName componentName, IBinder iBinder) {
        HiLog.info(LOG_LABEL, "onConnected.", new Object[0]);
        addPendingCallToProxy(iBinder);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onConnected(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisconnected(ComponentName componentName) {
        disconnect();
        HiLog.info(LOG_LABEL, "onDisconnected.", new Object[0]);
        this.mRemote = null;
        processLiveCallWhenServiceDisconnected(componentName);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onDisconnected(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void onInCallServiceBind() {
        HiLog.info(LOG_LABEL, "onInCallServiceBind.", new Object[0]);
        this.mIsInCallServiceBinded = true;
    }

    /* access modifiers changed from: package-private */
    public void onInCallServiceUnbind() {
        HiLog.info(LOG_LABEL, "onInCallServiceUnbind.", new Object[0]);
        this.mIsInCallServiceBinded = false;
        releaseResource();
    }

    /* access modifiers changed from: package-private */
    public boolean onCallAudioStateChanged(CallAudioState callAudioState) {
        this.mCurrentCallAudioState = callAudioState;
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallAudioStateChanged fail, no remote.", new Object[0]);
            this.mPendingCallAudioState = callAudioState;
            return false;
        } else if (callAudioState != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                this.mRemote.transact(1, CallSerializationUtils.writeCallAudioStateToParcel(obtain, callAudioState), obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onCallAudioStateChanged.", new Object[0]);
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onCallAudioStateChanged got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onCallAudioStateChanged fail, no audioState.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onCallAdded(Call call) {
        if (call == null) {
            HiLog.error(LOG_LABEL, "onCallAdded fail, no call.", new Object[0]);
            return false;
        } else if (call.getState() == 9 && this.mPendingDisconnectPreAddedCall != null) {
            HiLog.info(LOG_LABEL, "onCallAdded, has pending disconnect call.", new Object[0]);
            CallAppAbilityConnnectionHandler callAppAbilityConnnectionHandler = this.mHandler;
            if (callAppAbilityConnnectionHandler != null) {
                callAppAbilityConnnectionHandler.removeEvent(1001);
            }
            this.mPendingDisconnectPreAddedCall = null;
            call.disconnect();
            return true;
        } else if (!canAddCallToCallMap()) {
            return false;
        } else {
            if (isConnected()) {
                HiLog.info(LOG_LABEL, "onCallAdded, already connectted call ability.", new Object[0]);
                if (this.mPreAddedCall == null && this.mPendingDisconnectPreAddedCall == null) {
                    HiLog.info(LOG_LABEL, "onCallAdded, no pre added call.", new Object[0]);
                    addCallToCallMap(call);
                } else {
                    if (this.mPreAddedCall == null) {
                        PreAddedCall preAddedCall = this.mPendingDisconnectPreAddedCall;
                    }
                    processPreAddedCall(this.mPreAddedCall, call);
                    this.mPreAddedCall = null;
                    this.mPendingDisconnectPreAddedCall = null;
                }
                return addCall(call);
            }
            addCallToCallMap(call);
            this.mPreAddedCall = null;
            this.mPendingDisconnectPreAddedCall = null;
            connect();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean processPreAddedCall(PreAddedCall preAddedCall, Call call) {
        if (preAddedCall == null) {
            HiLog.error(LOG_LABEL, "processPreAddedCall fail, no pre added call.", new Object[0]);
            return false;
        } else if (call == null) {
            HiLog.error(LOG_LABEL, "processPreAddedCall fail, no call.", new Object[0]);
            return false;
        } else {
            String number = AospInCallService.getNumber(call);
            String number2 = preAddedCall.getNumber();
            if (call.getState() != 9 || number == null || !number.equals(number2)) {
                HiLog.info(LOG_LABEL, "processPreAddedCall, disconnect pre added call.", new Object[0]);
                disconnectPreAddedCall(preAddedCall);
                sCallId++;
                addCallToCallMap(call);
            } else {
                HiLog.info(LOG_LABEL, "processPreAddedCall, update pre added call.", new Object[0]);
                addCallToCallMap(call);
                onStateChanged(call, call.getState());
                onDetailsChanged(call, call.getDetails());
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean addCall(Call call) {
        if (call == null) {
            HiLog.error(LOG_LABEL, "addCall fail, no call.", new Object[0]);
            return false;
        } else if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "addCall fail, no remote.", new Object[0]);
            return false;
        } else {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                this.mRemote.transact(2, CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue()), obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "addCall: %{public}s", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "addCall got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean preOnCallAdded(String str, int i, String str2, String str3) {
        if (str == null) {
            HiLog.error(LOG_LABEL, "preOnCallAdded: no number.", new Object[0]);
            return false;
        } else if (str2 == null || str3 == null) {
            HiLog.error(LOG_LABEL, "preOnCallAdded: no call component or ability name.", new Object[0]);
            return false;
        } else {
            ComponentName componentName = new ComponentName(str2, str3);
            CallAppAbilityInfo callAppAbilityInfo = this.mInfo;
            if (callAppAbilityInfo == null || !componentName.equals(callAppAbilityInfo.getComponentName())) {
                HiLog.info(LOG_LABEL, "preOnCallAdded: call component name is different.", new Object[0]);
                return false;
            } else if (this.mPreAddedCall != null || this.mPendingDisconnectPreAddedCall != null) {
                HiLog.error(LOG_LABEL, "preOnCallAdded: already pre add call before.", new Object[0]);
                return false;
            } else if (hasLiveCalls()) {
                HiLog.error(LOG_LABEL, "preOnCallAdded: already has call before.", new Object[0]);
                return false;
            } else if (!canAddCallToCallMap()) {
                return false;
            } else {
                this.mPreAddedCall = new PreAddedCall(sCallId, str, 9, i);
                return preProcessCall(2, this.mPreAddedCall);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean preProcessCall(int i, PreAddedCall preAddedCall) {
        boolean z = true;
        HiLog.info(LOG_LABEL, "preProcessCall: code is %{public}d.", new Object[]{Integer.valueOf(i)});
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "preProcessCall fail, no remote.", new Object[0]);
            return false;
        } else if (preAddedCall == null) {
            HiLog.error(LOG_LABEL, "preProcessCall fail, no call.", new Object[0]);
            return false;
        } else {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                this.mRemote.transact(i, CallSerializationUtils.writePreCallToParcel(obtain, preAddedCall.getCallId(), preAddedCall.getNumber(), preAddedCall.getState(), preAddedCall.getVideoState()), obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "preProcessCall success.", new Object[0]);
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "preProcessCall got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean disconnectPreAddedCall(int i) {
        PreAddedCall preAddedCall = this.mPreAddedCall;
        if (preAddedCall == null || preAddedCall.getCallId() != i) {
            HiLog.error(LOG_LABEL, "disconnectPreAddedCall: no matched pre added call.", new Object[0]);
            return false;
        }
        Call callById = getCallById(Integer.valueOf(this.mPreAddedCall.getCallId()));
        if (callById != null) {
            HiLog.info(LOG_LABEL, "disconnectPreAddedCall: %{public}s", new Object[]{callById.toString()});
            callById.disconnect();
        } else {
            HiLog.info(LOG_LABEL, "disconnectPreAddedCall: pending disconnect call, id is %{public}d.", new Object[]{Integer.valueOf(i)});
            this.mPendingDisconnectPreAddedCall = this.mPreAddedCall;
            if (this.mHandler == null) {
                this.mHandler = createHandler();
            }
            CallAppAbilityConnnectionHandler callAppAbilityConnnectionHandler = this.mHandler;
            if (callAppAbilityConnnectionHandler == null) {
                HiLog.error(LOG_LABEL, "disconnectPreAddedCall: no handler.", new Object[0]);
                return false;
            } else if (callAppAbilityConnnectionHandler.hasInnerEvent(1001)) {
                HiLog.info(LOG_LABEL, "disconnectPreAddedCall: already call disconnect before, ignore this call.", new Object[0]);
                return true;
            } else {
                try {
                    this.mHandler.sendEvent(InnerEvent.get(1001, 0, null), DELAYED_TIME_DISCONNECT_PRE_ADDED_CALL, EventHandler.Priority.IMMEDIATE);
                } catch (IllegalArgumentException unused) {
                    HiLog.error(LOG_LABEL, "disconnectPreAddedCall: got IllegalArgumentException.", new Object[0]);
                }
            }
        }
        this.mPreAddedCall = null;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean disconnectPendingDisconnectPreAddedCall() {
        PreAddedCall preAddedCall = this.mPendingDisconnectPreAddedCall;
        if (preAddedCall == null) {
            return true;
        }
        disconnectPreAddedCall(preAddedCall);
        this.mPendingDisconnectPreAddedCall = null;
        releaseResource();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean disconnectPreAddedCall(PreAddedCall preAddedCall) {
        if (preAddedCall == null) {
            HiLog.info(LOG_LABEL, "disconnectPreAddedCall fail, no call.", new Object[0]);
            return false;
        }
        HiLog.info(LOG_LABEL, "disconnectPreAddedCall.", new Object[0]);
        preAddedCall.setState(7);
        preProcessCall(6, preAddedCall);
        preProcessCall(3, preAddedCall);
        preProcessCall(9, preAddedCall);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean preConnectCallAppAbility(String str, String str2) {
        HiLog.info(LOG_LABEL, "preConnectCallAppAbility", new Object[0]);
        if (str == null) {
            HiLog.error(LOG_LABEL, "preConnectCallAppAbility: no call component name.", new Object[0]);
            return false;
        } else if (str2 == null) {
            HiLog.error(LOG_LABEL, "preConnectCallAppAbility: no call ability name.", new Object[0]);
            return false;
        } else {
            ComponentName componentName = new ComponentName(str, str2);
            CallAppAbilityInfo callAppAbilityInfo = this.mInfo;
            if (callAppAbilityInfo == null || !componentName.equals(callAppAbilityInfo.getComponentName())) {
                if (!this.mCallMapById.isEmpty()) {
                    HiLog.info(LOG_LABEL, "preConnectCallAppAbility: still has call.", new Object[0]);
                    return false;
                }
                disconnect();
                CallAppAbilityInfo callAppAbilityInfo2 = this.mInfo;
                if (callAppAbilityInfo2 != null) {
                    callAppAbilityInfo2.setComponentName(componentName);
                }
                connect();
            } else if (isConnected()) {
                HiLog.info(LOG_LABEL, "preConnectCallAppAbility: already connected.", new Object[0]);
            } else {
                connect();
            }
            this.mIsPreConnected = true;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean preDisconnectCallAppAbility(String str, String str2) {
        HiLog.info(LOG_LABEL, "preDisconnectCallAppAbility", new Object[0]);
        if (str == null) {
            HiLog.error(LOG_LABEL, "preDisconnectCallAppAbility: no call component name.", new Object[0]);
            return false;
        } else if (str2 == null) {
            HiLog.error(LOG_LABEL, "preDisconnectCallAppAbility: no call ability name.", new Object[0]);
            return false;
        } else {
            ComponentName componentName = new ComponentName(str, str2);
            CallAppAbilityInfo callAppAbilityInfo = this.mInfo;
            if (callAppAbilityInfo == null || !componentName.equals(callAppAbilityInfo.getComponentName())) {
                HiLog.error(LOG_LABEL, "preDisconnectCallAppAbility, call component name is different.", new Object[0]);
                return false;
            }
            this.mIsPreConnected = false;
            if (this.mPreAddedCall == null && this.mPendingDisconnectPreAddedCall == null && this.mCallMapById.isEmpty()) {
                disconnect();
                releaseResource();
                return true;
            }
            HiLog.info(LOG_LABEL, "preDisconnectCallAppAbility, still has call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean connectCallAppAbility(ComponentName componentName) {
        boolean z = false;
        HiLog.info(LOG_LABEL, "connectCallAppAbility", new Object[0]);
        if (componentName == null) {
            HiLog.error(LOG_LABEL, "connectCallAppAbility: no component name.", new Object[0]);
            return false;
        } else if (this.mContext == null) {
            HiLog.error(LOG_LABEL, "connectCallAppAbility: no context.", new Object[0]);
            return false;
        } else {
            Intent intent = new Intent();
            intent.setComponent(componentName);
            try {
                z = AbilityUtils.connectAbility(this.mContext, intent, this.mServiceConnection);
            } catch (IllegalArgumentException unused) {
                HiLog.error(LOG_LABEL, "connectCallAppAbility got IllegalArgumentException.", new Object[0]);
            } catch (SecurityException unused2) {
                HiLog.error(LOG_LABEL, "connectCallAppAbility got SecurityException.", new Object[0]);
            } catch (IllegalStateException unused3) {
                HiLog.error(LOG_LABEL, "connectCallAppAbility got IllegalStateException.", new Object[0]);
            }
            if (!z) {
                connectCallAppAbilityFail();
            } else {
                connectCallAppAbilitySuccess();
            }
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public void connectCallAppAbilityFail() {
        if (needTryAgain()) {
            HiLog.error(LOG_LABEL, "connectCallAppAbilityFail: retry.", new Object[0]);
            if (this.mHandler == null) {
                this.mHandler = createHandler();
            }
            if (this.mHandler != null) {
                try {
                    this.mHandler.sendEvent(InnerEvent.get(1000, 0, null), DELAYED_TIME_RETRY_CONNECT_INCALL_ABILITY, EventHandler.Priority.IMMEDIATE);
                } catch (IllegalArgumentException unused) {
                    HiLog.error(LOG_LABEL, "connectCallAppAbilityFail: got IllegalArgumentException.", new Object[0]);
                }
            }
        } else {
            HiLog.error(LOG_LABEL, "connectCallAppAbilityFail: reached max retry counts.", new Object[0]);
            disconnectLiveCalls();
            this.mRetryConnectCallAppAbilityCount = 0;
            releaseResource();
        }
    }

    /* access modifiers changed from: package-private */
    public void connectCallAppAbilitySuccess() {
        HiLog.info(LOG_LABEL, "connectCallAppAbilitySuccess", new Object[0]);
        this.mRetryConnectCallAppAbilityCount = 0;
    }

    /* access modifiers changed from: package-private */
    public void disconnectLiveCalls() {
        if (!this.mCallMapById.isEmpty()) {
            for (Integer num : this.mCallMapById.keySet()) {
                Call callById = getCallById(num);
                if (!(callById == null || callById.getState() == 10 || callById.getState() == 7)) {
                    HiLog.info(LOG_LABEL, "disconnectLiveCalls: %{public}s.", new Object[]{callById.toString()});
                    callById.disconnect();
                }
            }
            this.mCallMapById.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectCallAppAbility() {
        HiLog.info(LOG_LABEL, "disconnectCallAppAbility", new Object[0]);
        Context context = this.mContext;
        if (context == null) {
            HiLog.error(LOG_LABEL, "disconnectCallAppAbility: no context.", new Object[0]);
            return;
        }
        try {
            AbilityUtils.disconnectAbility(context, this.mServiceConnection);
        } catch (IllegalArgumentException unused) {
            HiLog.error(LOG_LABEL, "disconnectCallAppAbility got IllegalArgumentException.", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void addPendingCallToProxy(IBinder iBinder) {
        HiLog.info(LOG_LABEL, "addPendingCallToProxy", new Object[0]);
        this.mRemote = iBinder;
        processLiveCallWhenServiceConnected();
        CallAudioState callAudioState = this.mPendingCallAudioState;
        if (callAudioState != null) {
            HiLog.info(LOG_LABEL, "addPendingCallToProxy:onCallAudioStateChanged %{public}s", new Object[]{callAudioState.toString()});
            onCallAudioStateChanged(this.mPendingCallAudioState);
            this.mPendingCallAudioState = null;
        }
        Boolean bool = this.mPendingCanAddCall;
        if (bool != null) {
            HiLog.info(LOG_LABEL, "addPendingCallToProxy:onCanAddCallChanged %{public}s", new Object[]{bool.toString()});
            onCanAddCallChanged(this.mPendingCanAddCall.booleanValue());
            this.mPendingCanAddCall = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void processLiveCallWhenServiceConnected() {
        if (!this.mCallMapById.isEmpty()) {
            for (Integer num : this.mCallMapById.keySet()) {
                Call callById = getCallById(num);
                if (!(callById == null || callById.getState() == 10 || callById.getState() == 7)) {
                    HiLog.info(LOG_LABEL, "processLiveCallWhenServiceConnected: still has call", new Object[0]);
                    addCall(callById);
                }
            }
            processCallAudioStateWhenServiceConnected();
            processCanAddCallWhenServiceConnected();
        }
    }

    /* access modifiers changed from: package-private */
    public void processCallAudioStateWhenServiceConnected() {
        CallAudioState callAudioState = this.mCurrentCallAudioState;
        if (callAudioState != null) {
            HiLog.info(LOG_LABEL, "processCallAudioStateWhenServiceConnected:onCallAudioStateChanged %{public}s", new Object[]{callAudioState.toString()});
            onCallAudioStateChanged(this.mCurrentCallAudioState);
        }
    }

    /* access modifiers changed from: package-private */
    public void processCanAddCallWhenServiceConnected() {
        Boolean bool = this.mCurrentCanAddCall;
        if (bool != null) {
            HiLog.info(LOG_LABEL, "processCanAddCallWhenServiceConnected:onCanAddCallChanged %{public}s", new Object[]{bool.toString()});
            onCanAddCallChanged(this.mCurrentCanAddCall.booleanValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void processLiveCallWhenServiceDisconnected(ComponentName componentName) {
        if (hasLiveCalls()) {
            HiLog.info(LOG_LABEL, "processLiveCallWhenServiceDisconnected, reconnect.", new Object[0]);
            connect();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasLiveCalls() {
        if (this.mCallMapById.isEmpty()) {
            return false;
        }
        for (Integer num : this.mCallMapById.keySet()) {
            Call callById = getCallById(num);
            if (!(callById == null || callById.getState() == 10 || callById.getState() == 7)) {
                HiLog.info(LOG_LABEL, "hasLiveCalls.", new Object[0]);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onCallRemoved(Call call) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallRemoved fail, no remote.", new Object[0]);
            return false;
        }
        boolean z = true;
        if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                this.mRemote.transact(3, CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue()), obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onCallRemoved: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onCallRemoved got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    removeCallFromCallMap(call);
                    releaseResource();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            z = false;
        }
        removeCallFromCallMap(call);
        releaseResource();
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean onCanAddCallChanged(boolean z) {
        this.mCurrentCanAddCall = new Boolean(z);
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCanAddCallChanged fail, no remote.", new Object[0]);
            this.mPendingCanAddCall = new Boolean(z);
            return false;
        }
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        boolean z2 = true;
        try {
            obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
            obtain.writeBoolean(z);
            this.mRemote.transact(4, obtain, obtain2, 0);
            try {
                HiLog.info(LOG_LABEL, "onCanAddCallChanged: %{public}b.", new Object[]{Boolean.valueOf(z)});
            } catch (RemoteException unused) {
            }
        } catch (RemoteException unused2) {
            z2 = false;
            try {
                HiLog.error(LOG_LABEL, "onCanAddCallChanged got RemoteException.", new Object[0]);
                obtain2.recycle();
                obtain.recycle();
                return z2;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
        }
        obtain2.recycle();
        obtain.recycle();
        return z2;
    }

    /* access modifiers changed from: package-private */
    public boolean onSilenceRinger() {
        boolean z;
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onSilenceRinger fail, no remote.", new Object[0]);
            return false;
        }
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
            this.mRemote.transact(5, obtain, obtain2, 0);
            z = true;
            try {
                HiLog.info(LOG_LABEL, "onSilenceRinger.", new Object[0]);
            } catch (RemoteException unused) {
            }
        } catch (RemoteException unused2) {
            z = false;
            try {
                HiLog.error(LOG_LABEL, "onSilenceRinger got RemoteException.", new Object[0]);
                obtain2.recycle();
                obtain.recycle();
                return z;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
        }
        obtain2.recycle();
        obtain.recycle();
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean onStateChanged(Call call, int i) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onStateChanged fail, no remote.", new Object[0]);
            return false;
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                obtain = CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue());
                obtain.writeInt(i);
                this.mRemote.transact(6, obtain, obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onStateChanged: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onStateChanged got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onStateChanged fail, no call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onDetailsChanged(Call call, Call.Details details) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onDetailsChanged fail, no remote.", new Object[0]);
            return false;
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                this.mRemote.transact(7, CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue()), obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onDetailsChanged: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onDetailsChanged got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onDetailsChanged fail, no call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onPostDialWait(Call call, String str) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onPostDialWait fail, no remote.", new Object[0]);
            return false;
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                obtain = CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue());
                obtain.writeString(str);
                this.mRemote.transact(8, obtain, obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onPostDialWait: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onPostDialWait got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onPostDialWait fail, no call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onCallDestroyed(Call call) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallDestroyed fail, no remote.", new Object[0]);
            return false;
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                obtain = CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue());
                this.mRemote.transact(9, obtain, obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onCallDestroyed: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onCallDestroyed got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onCallDestroyed fail, no call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onConnectionEvent(Call call, String str, Bundle bundle) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onConnectionEvent fail, no remote.", new Object[0]);
            return false;
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            boolean z = true;
            try {
                obtain.writeInterfaceToken(DistributedCallUtils.DISTRIBUTED_CALL_ABILITY_DESCRIPTOR);
                obtain = CallSerializationUtils.writeCallToParcel(obtain, call, getIdByCall(call).intValue());
                obtain.writeString(str);
                CallSerializationUtils.writeBundleToParcel(obtain, bundle);
                this.mRemote.transact(10, obtain, obtain2, 0);
                try {
                    HiLog.info(LOG_LABEL, "onConnectionEvent: %{public}s.", new Object[]{call.toString()});
                } catch (RemoteException unused) {
                }
            } catch (RemoteException unused2) {
                z = false;
                try {
                    HiLog.error(LOG_LABEL, "onConnectionEvent got RemoteException.", new Object[0]);
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw th;
                }
            }
            obtain2.recycle();
            obtain.recycle();
            return z;
        } else {
            HiLog.error(LOG_LABEL, "onConnectionEvent fail, no call.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
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

    /* access modifiers changed from: package-private */
    public Call getCallById(Integer num) {
        return this.mCallMapById.get(num);
    }

    /* access modifiers changed from: package-private */
    public void addCallToCallMap(Call call) {
        if (call != null && getIdByCall(call) == INVALID_KEY_SET_IN_MAP) {
            HiLog.info(LOG_LABEL, "add call to map.", new Object[0]);
            this.mCallMapById.put(Integer.valueOf(sCallId), call);
            sCallId++;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canAddCallToCallMap() {
        if (this.mCallMapById.size() < 32) {
            return true;
        }
        HiLog.error(LOG_LABEL, "canAddCallToCallMap: reached the maximum number of calls.", new Object[0]);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeCallFromCallMap(Call call) {
        Integer idByCall = getIdByCall(call);
        if (idByCall != INVALID_KEY_SET_IN_MAP) {
            HiLog.info(LOG_LABEL, "removeCallFromCallMap success.", new Object[0]);
            this.mCallMapById.remove(idByCall);
            return;
        }
        HiLog.error(LOG_LABEL, "removeCallFromCallMap fail, call is not in map.", new Object[0]);
    }

    /* access modifiers changed from: private */
    public class PreAddedCall {
        private int mCallId;
        private String mNumber;
        private int mState;
        private int mVideoState;

        private PreAddedCall(int i, String str, int i2, int i3) {
            this.mCallId = i;
            this.mNumber = str;
            this.mState = i2;
            this.mVideoState = i3;
        }

        /* access modifiers changed from: package-private */
        public int getCallId() {
            return this.mCallId;
        }

        /* access modifiers changed from: package-private */
        public int getState() {
            return this.mState;
        }

        /* access modifiers changed from: package-private */
        public void setState(int i) {
            this.mState = i;
        }

        /* access modifiers changed from: package-private */
        public String getNumber() {
            return this.mNumber;
        }

        /* access modifiers changed from: package-private */
        public int getVideoState() {
            return this.mVideoState;
        }
    }

    /* access modifiers changed from: private */
    public class CallAppAbilityConnnectionHandler extends EventHandler {
        private CallAppAbilityConnnectionHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        @Override // ohos.eventhandler.EventHandler
        public void processEvent(InnerEvent innerEvent) {
            super.processEvent(innerEvent);
            if (innerEvent != null) {
                HiLog.info(CallAppAbilityConnection.LOG_LABEL, "processEvent, eventId %{public}d.", new Object[]{Integer.valueOf(innerEvent.eventId)});
                int i = innerEvent.eventId;
                if (i == 1000) {
                    HiLog.info(CallAppAbilityConnection.LOG_LABEL, "retry connect to call app ability.", new Object[0]);
                    CallAppAbilityConnection.this.connect();
                } else if (i == 1001 && CallAppAbilityConnection.this.mPendingDisconnectPreAddedCall != null) {
                    HiLog.info(CallAppAbilityConnection.LOG_LABEL, "disconnect pending disconnect pre added call.", new Object[0]);
                    CallAppAbilityConnection callAppAbilityConnection = CallAppAbilityConnection.this;
                    callAppAbilityConnection.disconnectPreAddedCall(callAppAbilityConnection.mPendingDisconnectPreAddedCall);
                    CallAppAbilityConnection.this.mPendingDisconnectPreAddedCall = null;
                    CallAppAbilityConnection.this.releaseResource();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public CallAppAbilityConnnectionHandler createHandler() {
        EventRunner create = EventRunner.create();
        if (create != null) {
            return new CallAppAbilityConnnectionHandler(create);
        }
        HiLog.error(LOG_LABEL, "createHandler: no runner.", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public void destroyHandler() {
        CallAppAbilityConnnectionHandler callAppAbilityConnnectionHandler = this.mHandler;
        if (callAppAbilityConnnectionHandler != null) {
            callAppAbilityConnnectionHandler.removeAllEvent();
            this.mHandler = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needTryAgain() {
        boolean z = this.mRetryConnectCallAppAbilityCount < 6;
        this.mRetryConnectCallAppAbilityCount++;
        return z;
    }
}
