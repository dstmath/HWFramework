package android.app.admin;

import android.app.Service;
import android.app.admin.IDeviceAdminService.Stub;
import android.content.Intent;
import android.os.IBinder;

public class DeviceAdminService extends Service {
    private final IDeviceAdminServiceImpl mImpl = new IDeviceAdminServiceImpl(this, null);

    private class IDeviceAdminServiceImpl extends Stub {
        /* synthetic */ IDeviceAdminServiceImpl(DeviceAdminService this$0, IDeviceAdminServiceImpl -this1) {
            this();
        }

        private IDeviceAdminServiceImpl() {
        }
    }

    public final IBinder onBind(Intent intent) {
        return this.mImpl.asBinder();
    }
}
