package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import java.util.ArrayList;

public abstract class DevicePolicyPlugin {
    protected Context mContext;
    protected PolicyStruct mPolicyStruct;

    public abstract boolean checkCallingPermission(ComponentName componentName, String str);

    public abstract String getPluginName();

    public abstract PolicyStruct getPolicyStruct();

    public abstract boolean onActiveAdminRemoved(ComponentName componentName, ArrayList<PolicyItem> arrayList);

    public abstract boolean onRemovePolicy(ComponentName componentName, String str, Bundle bundle, boolean z);

    public abstract boolean onSetPolicy(ComponentName componentName, String str, Bundle bundle, boolean z);

    public boolean init(PolicyStruct struct) {
        this.mPolicyStruct = struct;
        return onInit(struct);
    }

    public DevicePolicyPlugin(Context context) {
        this.mContext = context;
    }

    public boolean onInit(PolicyStruct struct) {
        return true;
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean changed) {
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyItem> arrayList) {
    }
}
