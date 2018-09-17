package android.app.admin;

import android.app.admin.DeviceAdminInfo.PolicyInfo;
import java.util.ArrayList;

public interface IHwDeviceAdminInfo {
    ArrayList<PolicyInfo> getHwUsedPoliciesList();
}
