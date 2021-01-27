package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.RemoteException;
import com.android.server.wifi.SupplicantStaIfaceHal;
import java.util.HashMap;

public interface IHwSupplicantStaIfaceHalInner {
    boolean checkStatusAndLogFailure(SupplicantStatus supplicantStatus, String str);

    ISupplicantStaIface checkSupplicantStaIfaceAndLogFailure(String str, String str2);

    HashMap<String, ISupplicantStaIfaceCallback> getISupplicantStaIfaceCallbacks();

    HashMap<String, ISupplicantStaIface> getISupplicantStaIfaces();

    SupplicantStaIfaceHal.SupplicantStaIfaceHalCallbackV1_1 getSupplicantStaIfaceHalCallbackV1_1(String str, SupplicantStaIfaceHal.SupplicantStaIfaceHalCallback supplicantStaIfaceHalCallback);

    Object getSupplicantStaIfaceHalLock();

    void handleRemoteException(RemoteException remoteException, String str);

    void supplicantServiceDiedHandler(long j);
}
