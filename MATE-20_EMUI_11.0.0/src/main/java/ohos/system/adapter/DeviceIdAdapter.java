package ohos.system.adapter;

import android.os.IDeviceIdentifiersPolicyService;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DeviceIdAdapter {
    private static final String DEVICE_ID_SERVICE = "device_identifiers";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, DeviceIdAdapter.class.getSimpleName());
    private static final String UDID_EXCEPTION = "AndroidRuntimeException";
    private static IDeviceIdentifiersPolicyService service = null;

    public static Optional<String> getUdid() {
        if (service == null) {
            service = IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService(DEVICE_ID_SERVICE));
            if (service == null) {
                HiLog.error(TAG, "remote service get is null.", new Object[0]);
                return Optional.empty();
            }
        }
        try {
            String udid = service.getUDID();
            HiLog.info(TAG, "device uuid is %{private}s", udid);
            if (UDID_EXCEPTION.equals(udid)) {
                return Optional.empty();
            }
            return Optional.of(udid);
        } catch (RemoteException e) {
            HiLog.warn(TAG, "RemoteException happens in getUDID! %s", e.getMessage());
            return Optional.empty();
        } catch (SecurityException e2) {
            HiLog.warn(TAG, "SecurityException happens in getUDID! %s", e2.getMessage());
            return Optional.empty();
        }
    }
}
