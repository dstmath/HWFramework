package ohos.ace.featureabilityplugin;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import ohos.ace.ability.AceInternalAbility;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.tools.Bytrace;

public class AbilityProxy implements IRemoteBroker {
    private static final String TAG = AbilityProxy.class.getSimpleName();
    private AceInternalAbility.AceInternalAbilityHandler internalAbilityHandler;
    private boolean internalFlag = false;
    private IRemoteObject remote;

    AbilityProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
        this.internalFlag = false;
    }

    public AbilityProxy(AceInternalAbility.AceInternalAbilityHandler aceInternalAbilityHandler) {
        this.internalAbilityHandler = aceInternalAbilityHandler;
        this.internalFlag = true;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    public void callAbility(int i, String str, Result result, boolean z) {
        Bytrace.startTrace(549755813888L, "callAbility");
        if (z) {
            try {
                AbilityResponse abilityResponse = new AbilityResponse();
                if (callAbilitySync(i, str, abilityResponse)) {
                    result.successWithRawString(abilityResponse.getResponse());
                } else {
                    ALog.e(TAG, "call ability failed");
                    result.error(2006, abilityResponse.getResponse());
                }
            } catch (RemoteException e) {
                String str2 = TAG;
                ALog.e(str2, "Call ability exception: " + e.getLocalizedMessage());
                result.error(2010, "Call ability exception: " + e.getLocalizedMessage());
            }
        } else {
            callAbilityAsync(i, str, result);
        }
        Bytrace.finishTrace(549755813888L, "callAbility");
    }

    public boolean subscribeAbility(int i, RemoteObject remoteObject, Result result, boolean z) {
        if (!z) {
            return subscribeAbilityAsync(i, remoteObject, result);
        }
        try {
            AbilityResponse abilityResponse = new AbilityResponse();
            if (subscribeAbilitySync(i, remoteObject, abilityResponse)) {
                result.successWithRawString(abilityResponse.getResponse());
                return true;
            }
            ALog.e(TAG, "subscribe ability failed");
            result.error(2007, abilityResponse.getResponse());
            return false;
        } catch (RemoteException e) {
            String str = TAG;
            ALog.e(str, "Subscribe ability exception: " + e.getLocalizedMessage());
            result.error(2010, "Subscribe ability exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean unsubscribeAbility(int i, RemoteObject remoteObject, Result result, boolean z) {
        if (!z) {
            return unsubscribeAbilityAsync(i, remoteObject, result);
        }
        try {
            AbilityResponse abilityResponse = new AbilityResponse();
            if (unsubscribeAbilitySync(i, remoteObject, abilityResponse)) {
                result.successWithRawString(abilityResponse.getResponse());
                return true;
            }
            ALog.e(TAG, "unsubscribe ability failed");
            result.error(2008, abilityResponse.getResponse());
            return false;
        } catch (RemoteException e) {
            String str = TAG;
            ALog.e(str, "Unsubscribe ability error: " + e.getLocalizedMessage());
            result.error(2010, "Unsubscribe ability exception: " + e.getLocalizedMessage());
            return false;
        }
    }

    private boolean callAbilitySync(int i, String str, AbilityResponse abilityResponse) throws RemoteException {
        boolean z;
        String str2 = TAG;
        ALog.i(str2, "call ability begin sync, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeString(str)) {
            String str3 = TAG;
            ALog.e(str3, "request data Length exceeds maximum limit:" + str.length());
            abilityResponse.setResponse("request data Length exceeds maximum limit:" + str.length());
            return false;
        }
        if (!this.internalFlag) {
            z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
        } else {
            Bytrace.startTrace(549755813888L, "callAppCode");
            z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
            Bytrace.finishTrace(549755813888L, "callAppCode");
        }
        abilityResponse.setResponse(obtain2.readString());
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    private boolean subscribeAbilitySync(int i, RemoteObject remoteObject, AbilityResponse abilityResponse) throws RemoteException {
        boolean z;
        String str = TAG;
        ALog.i(str, "subscribe ability sync begin, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeRemoteObject(remoteObject)) {
            ALog.e(TAG, "subscribe ability sync write remote object failed!");
            abilityResponse.setResponse("subscribe ability sync write remote object failed!");
            return false;
        }
        if (!this.internalFlag) {
            z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
        } else {
            z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
        }
        String str2 = TAG;
        ALog.d(str2, "subscribe ability sync result: " + z);
        abilityResponse.setResponse(obtain2.readString());
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    private boolean unsubscribeAbilitySync(int i, RemoteObject remoteObject, AbilityResponse abilityResponse) throws RemoteException {
        boolean z;
        String str = TAG;
        ALog.i(str, "unsubscribe ability sync begin, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeRemoteObject(remoteObject)) {
            ALog.e(TAG, "unsubscribe ability sync write remote object failed!");
            abilityResponse.setResponse("unsubscribe ability sync write remote object failed!");
            return false;
        }
        if (!this.internalFlag) {
            z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
        } else {
            z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
        }
        String str2 = TAG;
        ALog.d(str2, "unsubscribe ability sync result: " + z);
        abilityResponse.setResponse(obtain2.readString());
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    private void callAbilityAsync(int i, String str, Result result) throws RemoteException {
        boolean z;
        String str2 = TAG;
        ALog.i(str2, "call ability async begin, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(1);
        if (!obtain.writeString(str)) {
            String str3 = TAG;
            ALog.e(str3, "request data Length exceeds maximum limit:" + str.length());
            result.error(2006, "request data Length exceeds maximum limit:" + str.length());
        } else if (!obtain2.writeRemoteObject(new AbilityStub(result))) {
            ALog.e(TAG, "call ability async write remote object failed!");
            obtain.reclaim();
            result.error(2001, "call ability async write remote object failed!");
        } else {
            if (!this.internalFlag) {
                z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
            } else {
                z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
            }
            obtain.reclaim();
            obtain2.reclaim();
            String str4 = TAG;
            ALog.d(str4, "call ability async result: " + z);
            if (!z) {
                ALog.e(TAG, "call ability async failed!");
                result.error(2006, "call ability async failed!");
            }
        }
    }

    private boolean subscribeAbilityAsync(int i, RemoteObject remoteObject, Result result) throws RemoteException {
        boolean z;
        String str = TAG;
        ALog.i(str, "subscribe ability async begin, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(1);
        if (!obtain.writeRemoteObject(remoteObject)) {
            ALog.e(TAG, "subscribe ability async write remote object failed!");
            result.error(2007, "subscribe ability async write remote object failed!");
            return false;
        } else if (!obtain2.writeRemoteObject(new AbilityStub(result))) {
            ALog.e(TAG, "subscribe ability async write remote object failed!");
            obtain.reclaim();
            result.error(2001, "subscribe ability async write remote object failed!");
            return false;
        } else {
            if (!this.internalFlag) {
                z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
            } else {
                z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
            }
            String str2 = TAG;
            ALog.d(str2, "subscribe ability async result: " + z);
            obtain2.reclaim();
            obtain.reclaim();
            if (z) {
                return true;
            }
            ALog.e(TAG, "subscribe ability async failed!");
            result.error(2007, "subscribe ability async failed!");
            return false;
        }
    }

    private boolean unsubscribeAbilityAsync(int i, RemoteObject remoteObject, Result result) throws RemoteException {
        boolean z;
        String str = TAG;
        ALog.i(str, "unsubscribe ability async begin, code: " + i + ", flag:" + this.internalFlag);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(1);
        if (!obtain.writeRemoteObject(remoteObject)) {
            ALog.e(TAG, "unsubscribe ability async write remote object failed!");
            result.error(2008, "unsubscribe ability async write remote object failed!");
            return false;
        } else if (!obtain2.writeRemoteObject(new AbilityStub(result))) {
            ALog.e(TAG, "unsubscribe ability async write remote object failed!");
            obtain.reclaim();
            result.error(2001, "unsubscribe ability async write remote object failed!");
            return false;
        } else {
            if (!this.internalFlag) {
                z = this.remote.sendRequest(i, obtain, obtain2, messageOption);
            } else {
                z = this.internalAbilityHandler.onRemoteRequest(i, obtain, obtain2, messageOption);
            }
            String str2 = TAG;
            ALog.d(str2, "unsubscribe ability async result: " + z);
            obtain2.reclaim();
            obtain.reclaim();
            if (z) {
                return true;
            }
            ALog.e(TAG, "unsubscribe ability async failed!");
            result.error(2008, "unsubscribe ability async failed!");
            return false;
        }
    }
}
