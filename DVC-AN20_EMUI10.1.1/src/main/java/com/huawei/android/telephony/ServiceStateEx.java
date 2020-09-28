package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import com.huawei.annotation.HwSystemApi;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;

public class ServiceStateEx {
    public static final int NR_INFO_DC_NR_RESTRICTED = 1;
    public static final int NR_INFO_ENDC_AVAILABLE = 3;
    public static final int NR_INFO_NR_AVAILABLE = 2;
    public static final int RADIO_TECHNOLOGY_1xRTT = 6;
    public static final int RADIO_TECHNOLOGY_DCHSPAP = 30;
    public static final int RADIO_TECHNOLOGY_EDGE = 2;
    public static final int RADIO_TECHNOLOGY_EHRPD = 13;
    public static final int RADIO_TECHNOLOGY_EVDO_0 = 7;
    public static final int RADIO_TECHNOLOGY_EVDO_A = 8;
    public static final int RADIO_TECHNOLOGY_EVDO_B = 12;
    public static final int RADIO_TECHNOLOGY_GPRS = 1;
    public static final int RADIO_TECHNOLOGY_GSM = 16;
    public static final int RADIO_TECHNOLOGY_HSDPA = 9;
    public static final int RADIO_TECHNOLOGY_HSPA = 11;
    public static final int RADIO_TECHNOLOGY_HSPAP = 15;
    public static final int RADIO_TECHNOLOGY_HSUPA = 10;
    public static final int RADIO_TECHNOLOGY_IS95A = 4;
    public static final int RADIO_TECHNOLOGY_IS95B = 5;
    public static final int RADIO_TECHNOLOGY_IWLAN = 18;
    public static final int RADIO_TECHNOLOGY_LTE = 14;
    public static final int RADIO_TECHNOLOGY_LTE_CA = 19;
    public static final int RADIO_TECHNOLOGY_TD_SCDMA = 17;
    public static final int RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int RIL_RADIO_TECHNOLOGY_NR = 20;

    public static int getVoiceRegState(ServiceState object) {
        if (object != null) {
            return object.getVoiceRegState();
        }
        return 1;
    }

    public static int getDataState(ServiceState object) {
        return object.getDataRegState();
    }

    public static boolean isEmergencyOnly(ServiceState serviceState) {
        return serviceState != null && serviceState.isEmergencyOnly();
    }

    public static ServiceState newFromBundle(Bundle m) {
        return ServiceState.newFromBundle(m);
    }

    public static int getVoiceNetworkType(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getVoiceNetworkType();
        }
        return 0;
    }

    public static int getDataNetworkType(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getDataNetworkType();
        }
        return 0;
    }

    public static int getCdmaEriIconMode(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getCdmaEriIconMode();
        }
        return -1;
    }

    public static int getCdmaEriIconIndex(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getCdmaEriIconIndex();
        }
        return -1;
    }

    public static int getRilVoiceRadioTechnology(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getRilVoiceRadioTechnology();
        }
        return 0;
    }

    public static int getRilDataRadioTechnology(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getRilDataRadioTechnology();
        }
        return 0;
    }

    public static boolean isUsingCarrierAggregation(ServiceState serviceState) {
        return serviceState != null && serviceState.isUsingCarrierAggregation();
    }

    public static boolean isGsm(int radioTechnology) {
        return ServiceState.isGsm(radioTechnology);
    }

    public static boolean isCdma(int radioTechnology) {
        return ServiceState.isCdma(radioTechnology);
    }

    public static int getNsaState(ServiceState serviceState) {
        if (serviceState != null) {
            return serviceState.getNsaState();
        }
        return -1;
    }

    @HwSystemApi
    public static String getVoiceOperatorNumeric(ServiceState serviceState) {
        return serviceState.getVoiceOperatorNumeric();
    }

    @HwSystemApi
    public static void fillInNotifierBundle(ServiceState serviceState, Bundle data) {
        serviceState.fillInNotifierBundle(data);
    }

    public static boolean isNrAvailable(ServiceState serviceState, int nrInfo) {
        DataSpecificRegistrationInfo info = getDataSpecificRegistrationInfoByServiceState(serviceState);
        if (info == null) {
            return false;
        }
        if (nrInfo == 1) {
            return info.isDcNrRestricted;
        }
        if (nrInfo == 2) {
            return info.isNrAvailable;
        }
        if (nrInfo != 3) {
            return false;
        }
        return info.isEnDcAvailable;
    }

    private static DataSpecificRegistrationInfo getDataSpecificRegistrationInfoByServiceState(ServiceState serviceState) {
        NetworkRegistrationInfo regInfo;
        if (serviceState == null || (regInfo = serviceState.getNetworkRegistrationInfo(2, 1)) == null) {
            return null;
        }
        return regInfo.getDataSpecificInfo();
    }

    public static int getConfigRadioTechnology(ServiceState serviceState) {
        if (serviceState == null) {
            return 0;
        }
        return serviceState.getHwNetworkType();
    }

    @HwSystemApi
    public static int networkTypeToRilRadioTechnology(int networkType) {
        return ServiceState.networkTypeToRilRadioTechnology(networkType);
    }

    @HwSystemApi
    public static boolean isLte(int radioTechnology) {
        return ServiceState.isLte(radioTechnology);
    }

    @HwSystemApi
    public static void setOperatorAlphaLong(ServiceState serviceState, String longName) {
        if (serviceState != null) {
            serviceState.setOperatorAlphaLong(longName);
        }
    }

    @HwSystemApi
    public static void addNetworkRegistrationInfo(ServiceState serviceState, NetworkRegistrationInfoEx nri) {
        if (serviceState != null && nri != null) {
            serviceState.addNetworkRegistrationInfo(nri.getNetworkRegistrationInfo());
        }
    }

    @HwSystemApi
    public static NetworkRegistrationInfoEx getNetworkRegistrationInfo(ServiceState serviceState, int domain, int transportType) {
        if (serviceState != null) {
            return NetworkRegistrationInfoEx.getNetworkRegistrationInfoEx(serviceState.getNetworkRegistrationInfo(domain, transportType));
        }
        return null;
    }

    @HwSystemApi
    public static NetworkRegistrationInfoEx getNetworkRegistrationInfoHw(ServiceState serviceState, int domain, int transportType) {
        if (serviceState != null) {
            return NetworkRegistrationInfoEx.getNetworkRegistrationInfoEx(serviceState.getNetworkRegistrationInfoHw(domain, transportType));
        }
        return null;
    }

    @HwSystemApi
    public static void setCdmaEriIcon(ServiceState serviceState, boolean isCdmaRoaming) {
        if (serviceState == null) {
            return;
        }
        if (isCdmaRoaming) {
            serviceState.setCdmaEriIconIndex(0);
            serviceState.setCdmaEriIconMode(0);
            return;
        }
        serviceState.setCdmaEriIconIndex(1);
        serviceState.setCdmaEriIconMode(0);
    }
}
