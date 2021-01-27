package com.huawei.hardware.iawareperfpolicy;

import android.os.RemoteException;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.iawareperfpolicy.V1_0.IPerfPolicy;

public class IPerfPolicyAdapter {
    private IPerfPolicy mPolicy;

    public boolean getService(String policy) throws RemoteException {
        this.mPolicy = IPerfPolicy.getService(policy);
        if (this.mPolicy == null) {
            return false;
        }
        return true;
    }

    public void setOnDemandBoostPolicy(String key, int value) throws RemoteException, NoSuchElementException {
        this.mPolicy.setOnDemandBoostPolicy(key, value);
    }
}
