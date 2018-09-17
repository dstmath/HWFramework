package android.service.chooser;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.chooser.IChooserTargetService.Stub;
import java.util.List;

public abstract class ChooserTargetService extends Service {
    public static final String BIND_PERMISSION = "android.permission.BIND_CHOOSER_TARGET_SERVICE";
    private static final boolean DEBUG = false;
    public static final String META_DATA_NAME = "android.service.chooser.chooser_target_service";
    public static final String SERVICE_INTERFACE = "android.service.chooser.ChooserTargetService";
    private final String TAG = (ChooserTargetService.class.getSimpleName() + '[' + getClass().getSimpleName() + ']');
    private IChooserTargetServiceWrapper mWrapper = null;

    private class IChooserTargetServiceWrapper extends Stub {
        /* synthetic */ IChooserTargetServiceWrapper(ChooserTargetService this$0, IChooserTargetServiceWrapper -this1) {
            this();
        }

        private IChooserTargetServiceWrapper() {
        }

        public void getChooserTargets(ComponentName targetComponentName, IntentFilter matchedFilter, IChooserTargetResult result) throws RemoteException {
            List<ChooserTarget> targets = null;
            long id = clearCallingIdentity();
            try {
                targets = ChooserTargetService.this.onGetChooserTargets(targetComponentName, matchedFilter);
            } finally {
                restoreCallingIdentity(id);
                result.sendResult(targets);
            }
        }
    }

    public abstract List<ChooserTarget> onGetChooserTargets(ComponentName componentName, IntentFilter intentFilter);

    public IBinder onBind(Intent intent) {
        if (!SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        }
        if (this.mWrapper == null) {
            this.mWrapper = new IChooserTargetServiceWrapper(this, null);
        }
        return this.mWrapper;
    }
}
