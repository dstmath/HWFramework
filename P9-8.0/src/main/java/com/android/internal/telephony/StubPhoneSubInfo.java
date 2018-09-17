package com.android.internal.telephony;

import android.content.Context;
import android.os.Binder;
import com.huawei.hsm.permission.StubController;

public class StubPhoneSubInfo extends PhoneSubInfoController {
    private static final String PERMISSION_DENIED_IMSI = "000000000000000";
    private static final String PERMISSION_DENIED_PHONE = "000000";
    private static final String TAG = "StubPhoneSubInfo";
    private Context mContext = null;

    public StubPhoneSubInfo(Context context, Phone[] phone) {
        super(context, phone);
        this.mContext = context;
    }

    public String getLine1Number(String callingPackage) {
        isReadPhoneNumberBlocked();
        return super.getLine1Number(callingPackage);
    }

    public String getSubscriberId(String callingPackage) {
        isReadPhoneNumberBlocked();
        return super.getSubscriberId(callingPackage);
    }

    public String getDeviceId(String callingPackage) {
        isReadPhoneNumberBlocked();
        return super.getDeviceId(callingPackage);
    }

    public boolean isReadPhoneNumberBlocked() {
        boolean z = false;
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (!StubController.checkPrecondition(uid) || !StubController.isGlobalSwitchOn(this.mContext, 16)) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(16, uid, pid, null);
        if (selectionResult == 0) {
            return false;
        }
        if (2 == selectionResult) {
            z = true;
        }
        return z;
    }
}
