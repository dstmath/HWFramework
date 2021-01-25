package ohos.accessibility.adapter.ability;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityInteractionClient;
import ohos.accessibility.utils.LogUtil;

public class PerformActionAdapter {
    private static final String TAG = "PerformActionAdapter";

    private PerformActionAdapter() {
    }

    public static boolean performAction(int i, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return false;
        }
        try {
            return connection.performGlobalAction(i2);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "performGlobalAction RemoteException.");
            return false;
        }
    }
}
