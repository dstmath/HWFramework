package android.app.admin;

import android.app.admin.IHwDeviceAdminInfo;
import java.util.ArrayList;

public class HwDeviceAdminInfoDummy implements IHwDeviceAdminInfo {
    private static final String TAG = "HwDeviceAdminInfoDummy";
    private static IHwDeviceAdminInfo mHwDeviceAdminInfo = null;

    public static IHwDeviceAdminInfo getDefault() {
        IHwDeviceAdminInfo iHwDeviceAdminInfo;
        synchronized (HwDeviceAdminInfoDummy.class) {
            if (mHwDeviceAdminInfo == null) {
                mHwDeviceAdminInfo = new HwDeviceAdminInfoDummy();
            }
            iHwDeviceAdminInfo = mHwDeviceAdminInfo;
        }
        return iHwDeviceAdminInfo;
    }

    @Override // android.app.admin.IHwDeviceAdminInfo
    public ArrayList<IHwDeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList() {
        return new ArrayList<>();
    }
}
