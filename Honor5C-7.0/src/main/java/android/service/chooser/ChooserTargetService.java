package android.service.chooser;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.chooser.IChooserTargetService.Stub;
import java.util.List;

public abstract class ChooserTargetService extends Service {
    public static final String BIND_PERMISSION = "android.permission.BIND_CHOOSER_TARGET_SERVICE";
    private static final boolean DEBUG = false;
    public static final String META_DATA_NAME = "android.service.chooser.chooser_target_service";
    public static final String SERVICE_INTERFACE = "android.service.chooser.ChooserTargetService";
    private final String TAG;
    private IChooserTargetServiceWrapper mWrapper;

    private class IChooserTargetServiceWrapper extends Stub {
        private IChooserTargetServiceWrapper() {
        }

        public void getChooserTargets(ComponentName targetComponentName, IntentFilter matchedFilter, IChooserTargetResult result) throws RemoteException {
            List<ChooserTarget> targets = null;
            long id = Binder.clearCallingIdentity();
            try {
                targets = ChooserTargetService.this.onGetChooserTargets(targetComponentName, matchedFilter);
            } finally {
                Binder.restoreCallingIdentity(id);
                result.sendResult(targets);
            }
        }
    }

    public abstract List<ChooserTarget> onGetChooserTargets(ComponentName componentName, IntentFilter intentFilter);

    public ChooserTargetService() {
        this.TAG = ChooserTargetService.class.getSimpleName() + '[' + getClass().getSimpleName() + ']';
        this.mWrapper = null;
    }

    public IBinder onBind(Intent intent) {
        if (!SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        }
        if (this.mWrapper == null) {
            this.mWrapper = new IChooserTargetServiceWrapper();
        }
        return this.mWrapper;
    }
}
