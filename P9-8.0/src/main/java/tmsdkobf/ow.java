package tmsdkobf;

import android.os.IBinder;

final class ow {
    private static int Jc = 41;
    private static int Jd = 43;
    private IBinder mBinder;

    public ow() {
        if (mh.bY("android.app.admin.IDevicePolicyManager$Stub")) {
            Jc = mh.e("TRANSACTION_packageHasActiveAdmins", 41);
            Jd = mh.e("TRANSACTION_removeActiveAdmin", 43);
            this.mBinder = mi.getService("device_policy");
        }
    }
}
