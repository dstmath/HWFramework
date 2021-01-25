package com.huawei.internal.telephony;

import android.telephony.CellIdentity;
import android.telephony.NetworkRegistrationInfo;

public class NetworkRegistrationInfoEx {
    public static final int DOMAIN_CS = 1;
    public static final int DOMAIN_PS = 2;
    public static final int NSA_INVALID_STATE = 0;
    public static final int NSA_STATE1 = 1;
    public static final int NSA_STATE2 = 2;
    public static final int NSA_STATE3 = 3;
    public static final int NSA_STATE4 = 4;
    public static final int NSA_STATE5 = 5;
    public static final int REGISTRATION_STATE_DENIED = 3;
    public static final int REGISTRATION_STATE_HOME = 1;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_OR_SEARCHING = 0;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_SEARCHING = 2;
    public static final int REGISTRATION_STATE_ROAMING = 5;
    public static final int REGISTRATION_STATE_UNKNOWN = 4;
    private NetworkRegistrationInfo mNetworkRegistrationInfo;

    public static NetworkRegistrationInfoEx getNetworkRegistrationInfoEx(NetworkRegistrationInfo registrationInfo) {
        NetworkRegistrationInfoEx networkRegistrationInfoEx = new NetworkRegistrationInfoEx();
        networkRegistrationInfoEx.setNetworkRegistrationInfo(registrationInfo);
        return networkRegistrationInfoEx;
    }

    public NetworkRegistrationInfo getNetworkRegistrationInfo() {
        return this.mNetworkRegistrationInfo;
    }

    public void setNetworkRegistrationInfo(NetworkRegistrationInfo networkRegistrationInfo) {
        this.mNetworkRegistrationInfo = networkRegistrationInfo;
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

    public int getRegistrationState() {
        return this.mNetworkRegistrationInfo.getRegistrationState();
    }

    public int getTransportType() {
        return this.mNetworkRegistrationInfo.getTransportType();
    }

    public CellIdentity getCellIdentity() {
        NetworkRegistrationInfo networkRegistrationInfo = this.mNetworkRegistrationInfo;
        if (networkRegistrationInfo != null) {
            return networkRegistrationInfo.getCellIdentity();
        }
        return null;
    }
}
