package com.android.internal.telephony;

import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.dataconnection.DcTracker;

public class HwPhoneBaseReferenceImpl implements AbstractPhoneBase.HwPhoneBaseReference {
    private static final int INVALID_SCOPE = -1;
    private static final String TAG = "HwPhoneBaseReferenceImpl";
    private Phone mPhone;

    public HwPhoneBaseReferenceImpl(AbstractPhoneBase phoneBase) {
        this.mPhone = (Phone) phoneBase;
    }

    public int getDataRoamingScope() {
        if (this.mPhone.getDcTracker(1) == null) {
            return -1;
        }
        DcTracker dcTracker = this.mPhone.getDcTracker(1);
        if (dcTracker.getHwCustDcTracker() != null) {
            return dcTracker.getHwCustDcTracker().getDataRoamingScope();
        }
        return -1;
    }

    public boolean setDataRoamingScope(int scope) {
        if (this.mPhone.getDcTracker(1) == null) {
            return false;
        }
        DcTracker dcTracker = this.mPhone.getDcTracker(1);
        if (dcTracker.getHwCustDcTracker() != null) {
            return dcTracker.getHwCustDcTracker().setDataRoamingScope(scope);
        }
        return false;
    }
}
