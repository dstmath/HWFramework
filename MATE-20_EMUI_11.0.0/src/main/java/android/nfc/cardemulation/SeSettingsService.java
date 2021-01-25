package android.nfc.cardemulation;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.nfc.cardemulation.ISeSettingsService;
import android.os.IBinder;

public abstract class SeSettingsService extends Service {
    public static final String SESETTINGS_SERVICE_INTERFACE = "android.nfc.cardemulation.SeSettingsService";
    private final ISeSettingsService.Stub mStubWrapper = new ISeSettingsServiceWrapper();

    public abstract void setSeacActive(ComponentName componentName, boolean z);

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mStubWrapper;
    }

    private class ISeSettingsServiceWrapper extends ISeSettingsService.Stub {
        private ISeSettingsServiceWrapper() {
        }

        @Override // android.nfc.cardemulation.ISeSettingsService
        public void setSeacActive(ComponentName service, boolean foreground) {
            SeSettingsService.this.setSeacActive(service, foreground);
        }
    }
}
