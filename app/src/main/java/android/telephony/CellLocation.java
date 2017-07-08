package android.telephony;

import android.os.Bundle;
import android.os.ServiceManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.PhoneConstants;
import huawei.cust.HwCfgFilePolicy;

public abstract class CellLocation {
    public abstract void fillInNotifierBundle(Bundle bundle);

    public abstract boolean isEmpty();

    public abstract void setStateInvalid();

    public static void requestLocationUpdate() {
        try {
            ITelephony phone = Stub.asInterface(ServiceManager.getService(PhoneConstants.PHONE_KEY));
            if (phone != null) {
                phone.updateServiceLocation();
            }
        } catch (Exception e) {
        }
    }

    public static CellLocation newFromBundle(Bundle bundle) {
        switch (TelephonyManager.getDefault().getCurrentPhoneType()) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return new GsmCellLocation(bundle);
            case HwCfgFilePolicy.PC /*2*/:
                return new CdmaCellLocation(bundle);
            default:
                return null;
        }
    }

    public static CellLocation newFromBundle(Bundle bundle, int slotId) {
        switch (TelephonyManager.getDefault().getCurrentPhoneType(slotId)) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return new GsmCellLocation(bundle);
            case HwCfgFilePolicy.PC /*2*/:
                return new CdmaCellLocation(bundle);
            default:
                return null;
        }
    }

    public static CellLocation getEmpty() {
        switch (TelephonyManager.getDefault().getCurrentPhoneType()) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return new GsmCellLocation();
            case HwCfgFilePolicy.PC /*2*/:
                return new CdmaCellLocation();
            default:
                return null;
        }
    }
}
