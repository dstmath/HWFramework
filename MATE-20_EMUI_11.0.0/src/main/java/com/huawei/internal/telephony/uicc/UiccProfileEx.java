package com.huawei.internal.telephony.uicc;

import android.os.Handler;
import com.android.internal.telephony.uicc.IUiccProfileInner;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccProfile;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;

public class UiccProfileEx {
    private static final int APP_NUMBER_DEFAULT = 0;
    private UiccProfile mUiccProfile;

    public UiccProfile getUiccProfile() {
        return this.mUiccProfile;
    }

    public void setUiccProfile(IUiccProfileInner uiccProfile) {
        if (uiccProfile instanceof UiccProfile) {
            this.mUiccProfile = (UiccProfile) uiccProfile;
        }
    }

    public CommandsInterfaceEx getCiHw() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return null;
        }
        return uiccProfile.getCiHw();
    }

    public int getPhoneIdHw() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return -1;
        }
        return uiccProfile.getPhoneIdHw();
    }

    public Handler getHandler() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return null;
        }
        return uiccProfile.getHandler();
    }

    public IccCardConstantsEx.StateEx getState() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return null;
        }
        return IccCardConstantsEx.StateEx.getStateExByState(uiccProfile.getState());
    }

    public boolean isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx type) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return false;
        }
        return uiccProfile.isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.getAppTypeByEx(type));
    }

    public UiccCardApplicationEx getApplicationIndex(int index) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return null;
        }
        UiccCardApplication uiccCardApplication = uiccProfile.getApplicationIndex(index);
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(uiccCardApplication);
        return uiccCardApplicationEx;
    }

    public String getIccId() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return null;
        }
        return uiccProfile.getIccId();
    }

    public int getNumApplications() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return 0;
        }
        return uiccProfile.getNumApplications();
    }

    public UiccCardApplicationEx getApplicationByType(int type) {
        UiccCardApplication uiccCardApplication;
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null || (uiccCardApplication = uiccProfile.getApplicationByType(type)) == null) {
            return null;
        }
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(uiccCardApplication);
        return uiccCardApplicationEx;
    }

    public boolean isEmptyProfile() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return false;
        }
        return uiccProfile.isEmptyProfile();
    }

    public boolean getIccCardStateHw() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile == null) {
            return false;
        }
        return uiccProfile.getIccCardStateHw();
    }
}
