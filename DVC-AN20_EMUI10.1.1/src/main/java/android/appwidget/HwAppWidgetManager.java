package android.appwidget;

import android.appwidget.IHwAppWidgetManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.android.internal.appwidget.IAppWidgetService;

public class HwAppWidgetManager {
    private static final Singleton<IHwAppWidgetManager> IAppWidgetManagerSingleton = new Singleton<IHwAppWidgetManager>() {
        /* class android.appwidget.HwAppWidgetManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwAppWidgetManager create() {
            try {
                IAppWidgetService aws = IAppWidgetService.Stub.asInterface(ServiceManager.getService(Context.APPWIDGET_SERVICE));
                if (aws != null) {
                    return IHwAppWidgetManager.Stub.asInterface(aws.getHwInnerService());
                }
                Log.e(HwAppWidgetManager.TAG, "get IAppWidgetService failed");
                return null;
            } catch (RemoteException e) {
                Log.e(HwAppWidgetManager.TAG, "IHwAppWidgetManager create() fail: " + e);
                return null;
            }
        }
    };
    private static final String TAG = "HwAppWidgetManager";

    public static IHwAppWidgetManager getService() {
        return IAppWidgetManagerSingleton.get();
    }

    public static boolean registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback callback) {
        if (getService() == null) {
            return false;
        }
        try {
            getService().registerAWSIMonitorCallback(callback);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerWMMonitorCallback catch RemoteException!");
            return false;
        }
    }
}
