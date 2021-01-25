package ohos.thermaladapter;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.android.iaware.IAwareSdkEx;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.thermallistener.ThermalConnection;

public class ThermalConnectionAdapter extends Binder implements IBinder.DeathRecipient {
    private static final String DESCRIPTOR = "com.huawei.iaware.sdk.ThermalCallback";
    private static final String IAWARE_SDK_SERVICE_NAME = "IAwareSdkService";
    private static final int LOG_DOMAIN = 218114308;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, LOG_DOMAIN, TAG);
    private static final String TAG = "ThermalConnectionAdapter";
    private static final int TRANSACTION_ASYNC_THERMAL_REPORT_CALLBACK = 1;
    private ThermalConnection mConnection;
    private IBinder sdkService;

    public ThermalConnectionAdapter(ThermalConnection thermalConnection) {
        this.mConnection = thermalConnection;
    }

    public static void registerCallback(ThermalConnectionAdapter thermalConnectionAdapter, String str) {
        HiLog.info(LOG_LABEL, "packageName = %{public}s", str);
        IAwareSdkEx.registerCallback(3034, str, thermalConnectionAdapter);
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        this.sdkService = null;
        this.mConnection.thermalServiceDied();
        HiLog.error(LOG_LABEL, "IAwareSdkService died.", new Object[0]);
    }

    @Override // android.os.Binder
    public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        if (i != 1) {
            return super.onTransact(i, parcel, parcel2, i2);
        }
        parcel.enforceInterface(DESCRIPTOR);
        reportThermalData(parcel.readInt());
        parcel2.writeNoException();
        return true;
    }

    public boolean linkToSdkService() {
        if (this.sdkService != null) {
            return false;
        }
        try {
            this.sdkService = ServiceManager.getService(IAWARE_SDK_SERVICE_NAME);
            if (this.sdkService != null) {
                this.sdkService.linkToDeath(this, 0);
                return true;
            }
            HiLog.error(LOG_LABEL, "failed to get IAwareSdkService.", new Object[0]);
            return false;
        } catch (RemoteException e) {
            HiLog.error(LOG_LABEL, "RemoteException: %{public}s", e.getMessage());
        }
    }

    private void reportThermalData(int i) {
        ThermalConnection.ThermalSeverityLevel thermalSeverityLevel;
        HiLog.info(LOG_LABEL, "Thermal notify level = %{public}d", Integer.valueOf(i));
        if (i < ThermalConnection.ThermalSeverityLevel.COOL.ordinal() || i > ThermalConnection.ThermalSeverityLevel.EMERGENCY.ordinal()) {
            thermalSeverityLevel = ThermalConnection.ThermalSeverityLevel.COOL;
        } else {
            thermalSeverityLevel = ThermalConnection.ThermalSeverityLevel.values()[i];
        }
        this.mConnection.thermalStatusChanged(thermalSeverityLevel);
    }
}
