package ohos.accessibility.adapter.ability;

import android.os.Bundle;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Optional;
import ohos.accessibility.ability.AccessibilityInfo;
import ohos.accessibility.utils.LogUtil;

public class AccessibilitySearchAdapter {
    private static final String TAG = "AccessibilitySearchAdapter";

    private AccessibilitySearchAdapter() {
    }

    public static Optional<AccessibilityInfo> findFocusedAccessibilityInfo(int i, int i2) {
        AccessibilityNodeInfo findFocus = AccessibilityInteractionClient.getInstance().findFocus(i, -2, AccessibilityNodeInfo.ROOT_NODE_ID, i2);
        if (findFocus == null) {
            LogUtil.error(TAG, "findFocus failed.");
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(findFocus);
        findFocus.recycle();
        return convertToAccessibilityInfo;
    }

    public static Optional<AccessibilityInfo> getChild(int i, int i2, long j) {
        if (i == -1 || i2 == -1) {
            return Optional.empty();
        }
        AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(i, i2, j, false, 4, (Bundle) null);
        if (findAccessibilityNodeInfoByAccessibilityId == null) {
            LogUtil.error(TAG, "getChild failed.");
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(findAccessibilityNodeInfoByAccessibilityId);
        findAccessibilityNodeInfoByAccessibilityId.recycle();
        return convertToAccessibilityInfo;
    }

    public static Optional<AccessibilityInfo> getRootAccessibilityInfo(int i) {
        AccessibilityNodeInfo rootInActiveWindow = AccessibilityInteractionClient.getInstance().getRootInActiveWindow(i);
        if (rootInActiveWindow == null) {
            LogUtil.error(TAG, "getRootAccessibilityInfo failed.");
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(rootInActiveWindow);
        rootInActiveWindow.recycle();
        return convertToAccessibilityInfo;
    }

    public static Optional<AccessibilityInfo> gainFocus(int i, int i2, long j, int i3) {
        AccessibilityNodeInfo findFocus = AccessibilityInteractionClient.getInstance().findFocus(i, i2, j, i3);
        if (findFocus == null) {
            LogUtil.error(TAG, "gainFocus failed.");
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(findFocus);
        findFocus.recycle();
        return convertToAccessibilityInfo;
    }

    public static Optional<AccessibilityInfo> gainNextFocus(int i, int i2, long j, int i3) {
        AccessibilityNodeInfo focusSearch = AccessibilityInteractionClient.getInstance().focusSearch(i, i2, j, i3);
        if (focusSearch == null) {
            LogUtil.error(TAG, "gainNextFocus failed.");
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(focusSearch);
        focusSearch.recycle();
        return convertToAccessibilityInfo;
    }
}
