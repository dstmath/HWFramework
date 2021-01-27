package android.app.admin;

import android.app.admin.IHwDeviceAdminInfo;
import java.util.ArrayList;

public class HwDeviceAdminInfoDummy implements IHwDeviceAdminInfo {
    private static final String TAG = "HwDeviceAdminInfoDummy";
    private static IHwDeviceAdminInfo sHwDeviceAdminInfo = null;

    public static IHwDeviceAdminInfo getDefault() {
        IHwDeviceAdminInfo iHwDeviceAdminInfo;
        synchronized (HwDeviceAdminInfoDummy.class) {
            if (sHwDeviceAdminInfo == null) {
                sHwDeviceAdminInfo = new HwDeviceAdminInfoDummy();
            }
            iHwDeviceAdminInfo = sHwDeviceAdminInfo;
        }
        return iHwDeviceAdminInfo;
    }

    @Override // android.app.admin.IHwDeviceAdminInfo
    public ArrayList<IHwDeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList() {
        return new ArrayList<>();
    }
}
