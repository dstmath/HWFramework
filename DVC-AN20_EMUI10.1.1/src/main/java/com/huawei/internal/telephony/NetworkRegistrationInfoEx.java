package com.huawei.internal.telephony;

import android.telephony.NetworkRegistrationInfo;

public class NetworkRegistrationInfoEx {
    public static final int NSA_INVALID_STATE = 0;
    public static final int NSA_STATE1 = 1;
    public static final int NSA_STATE2 = 2;
    public static final int NSA_STATE3 = 3;
    public static final int NSA_STATE4 = 4;
    public static final int NSA_STATE5 = 5;
    private NetworkRegistrationInfo mNetworkRegistrationInfo;

    public void setNetworkRegistrationInfo(NetworkRegistrationInfo networkRegistrationInfo) {
        this.mNetworkRegistrationInfo = networkRegistrationInfo;
    }

    public static NetworkRegistrationInfoEx getNetworkRegistrationInfoEx(NetworkRegistrationInfo registrationInfo) {
        NetworkRegistrationInfoEx networkRegistrationInfoEx = new NetworkRegistrationInfoEx();
        networkRegistrationInfoEx.setNetworkRegistrationInfo(registrationInfo);
        return networkRegistrationInfoEx;
    }

    public NetworkRegistrationInfo getNetworkRegistrationInfo() {
        return this.mNetworkRegistrationInfo;
    }

    public int getNsaState() {
        NetworkRegistrationInfo networkRegistrationInfo = this.mNetworkRegistrationInfo;
        if (networkRegistrationInfo != null) {
            return networkRegistrationInfo.getNsaState();
        }
        return 1;
    }

    public void setNsaState(int nsaState) {
        NetworkRegistrationInfo networkRegistrationInfo = this.mNetworkRegistrationInfo;
        if (networkRegistrationInfo != null) {
            networkRegistrationInfo.setNsaState(nsaState);
        }
    }

    public void setConfigRadioTechnology(int radioTech) {
        NetworkRegistrationInfo networkRegistrationInfo = this.mNetworkRegistrationInfo;
        if (networkRegistrationInfo != null) {
            networkRegistrationInfo.setConfigRadioTechnology(radioTech);
        }
    }
}
