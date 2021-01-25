package ohos.accessibility.adapter.ability;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityInteractionClient;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityDisableAdapter {
    private static final String TAG = "AccessibilityDisableAdapter";

    private AccessibilityDisableAdapter() {
    }

    public static void disableAbility(int i) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection != null) {
            try {
                connection.disableSelf();
            } catch (RemoteException unused) {
                LogUtil.error(TAG, "disableAbility RemoteException.");
            }
        }
    }
}
