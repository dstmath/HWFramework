package android.view.accessibility;

import android.os.Bundle;
import java.util.List;

public abstract class AccessibilityNodeProvider {
    public static final int HOST_VIEW_ID = -1;

    public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
        return null;
    }

    public void addExtraDataToAccessibilityNodeInfo(int virtualViewId, AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
    }

    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        return false;
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String text, int virtualViewId) {
        return null;
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return null;
    }
}
