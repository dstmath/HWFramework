package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DevicePolicyPlugin {
    protected Context mContext;
    protected PolicyStruct mPolicyStruct;
    private HashMap<String, IPolicyItemKeyGetter> mPrivatePolicyItemKeyGetters = new HashMap<>();

    public interface IPolicyItemKeyGetter {
        String getKey(String str);
    }

    public abstract boolean checkCallingPermission(ComponentName componentName, String str);

    public abstract String getPluginName();

    public abstract PolicyStruct getPolicyStruct();

    public abstract boolean onActiveAdminRemoved(ComponentName componentName, ArrayList<PolicyStruct.PolicyItem> arrayList);

    public abstract boolean onRemovePolicy(ComponentName componentName, String str, Bundle bundle, boolean z);

    public abstract boolean onSetPolicy(ComponentName componentName, String str, Bundle bundle, boolean z);

    public DevicePolicyPlugin(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public IPolicyItemKeyGetter getKeyGetter(String policyName) {
        return this.mPrivatePolicyItemKeyGetters.get(policyName);
    }

    /* access modifiers changed from: protected */
    public void addGetter(String policyName, IPolicyItemKeyGetter keyGetter) {
        this.mPrivatePolicyItemKeyGetters.put(policyName, keyGetter);
    }

    public boolean init(PolicyStruct struct) {
        this.mPolicyStruct = struct;
        return onInit(struct);
    }

    public boolean onInit(PolicyStruct struct) {
        return true;
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies, int userId) {
        return onActiveAdminRemoved(who, removedPolicies);
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
    }
}
