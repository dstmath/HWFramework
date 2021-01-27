package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.RemoteException;
import com.android.server.wifi.SupplicantStaNetworkHal;

public interface IHwSupplicantStaNetworkHalInner {
    SupplicantStaNetworkHal.SupplicantStaNetworkHalCallback createHalCallback(int i, String str);

    Object getHalLock();

    ISupplicantStaNetwork getISupplicantStaNetwork();

    boolean vendorCheckAndLogFailure(String str);

    boolean vendorCheckStatusAndLogFailure(SupplicantStatus supplicantStatus, String str);

    void vendorHandleRemoteException(RemoteException remoteException, String str);
}
