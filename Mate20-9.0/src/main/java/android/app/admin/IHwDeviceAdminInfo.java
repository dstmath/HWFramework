package android.app.admin;

import android.app.admin.DeviceAdminInfo;
import java.util.ArrayList;

public interface IHwDeviceAdminInfo {
    ArrayList<DeviceAdminInfo.PolicyInfo> getHwUsedPoliciesList();
}
