package huawei.android.app.admin;

import android.app.admin.HwDeviceAdminInfoFactory;
import android.app.admin.IHwDeviceAdminInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;

public class HwDeviceAdminInfoFactoryImpl extends HwDeviceAdminInfoFactory {
    public IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        return new HwDeviceAdminInfo(context, activityInfo);
    }
}
