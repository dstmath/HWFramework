package ohos.ace.featureabilityplugin;

import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;

public class AbilityStub extends RemoteObject implements IRemoteBroker {
    private static final String TAG = AbilityStub.class.getSimpleName();
    private EventNotifier eventNotifier = null;
    private Result result = null;

    public IRemoteObject asObject() {
        return this;
    }

    public AbilityStub(EventNotifier eventNotifier2) {
        super("AbilityStub eventNotifier");
        this.eventNotifier = eventNotifier2;
    }

    public AbilityStub(Result result2) {
        super("AbilityStub result");
        this.result = result2;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) {
        if (this.eventNotifier != null) {
            String readString = messageParcel.readString();
            if (readString == null) {
                ALog.e(TAG, "eventData is not String");
                return false;
            }
            try {
                Object parse = JSON.parse(readString);
                if (parse == null) {
                    ALog.e(TAG, "Json parse failed");
                    return false;
                }
                this.eventNotifier.success(i, parse);
                return true;
            } catch (JSONException e) {
                String str = TAG;
                ALog.e(str, "Json parse event exception: " + e.getLocalizedMessage());
                EventNotifier eventNotifier2 = this.eventNotifier;
                eventNotifier2.error(2009, "Json parse event exception: " + e.getLocalizedMessage());
                return false;
            }
        } else {
            Result result2 = this.result;
            if (result2 != null) {
                result2.successWithRawString(messageParcel.readString());
                return true;
            }
            ALog.w(TAG, "event notifier is inactive, can't report event data!");
            try {
                return AbilityStub.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } catch (RemoteException e2) {
                String str2 = TAG;
                ALog.e(str2, "call super onRemoteRequest failed: " + e2.getLocalizedMessage());
                return false;
            }
        }
    }

    public void setEventNotifier(EventNotifier eventNotifier2) {
        this.eventNotifier = eventNotifier2;
    }

    public void reportEventMessage(int i, String str) {
        EventNotifier eventNotifier2 = this.eventNotifier;
        if (eventNotifier2 != null) {
            eventNotifier2.success(i, str);
        } else {
            ALog.w(TAG, "event notifier is inactive, can't report event data!");
        }
    }
}
