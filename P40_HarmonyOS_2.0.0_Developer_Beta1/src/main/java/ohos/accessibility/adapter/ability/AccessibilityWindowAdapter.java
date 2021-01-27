package ohos.accessibility.adapter.ability;

import android.os.Bundle;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.accessibility.ability.AccessibilityInfo;
import ohos.accessibility.ability.AccessibilityWindow;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityWindowAdapter {
    private static final String TAG = "AccessibilityWindowAdapter";

    private AccessibilityWindowAdapter() {
    }

    public static List<AccessibilityWindow> getAccessibilityWindows(int i) {
        List<AccessibilityWindowInfo> windows = AccessibilityInteractionClient.getInstance().getWindows(i);
        if (windows == null || windows.isEmpty()) {
            LogUtil.error(TAG, "getAccessibilityWindows failed, windows is empty.");
            return new ArrayList(0);
        }
        ArrayList arrayList = new ArrayList();
        for (AccessibilityWindowInfo accessibilityWindowInfo : windows) {
            Optional<AccessibilityWindow> convertToAccessibilityWindow = AccessibilityBeanConverter.convertToAccessibilityWindow(accessibilityWindowInfo);
            if (convertToAccessibilityWindow.isPresent()) {
                arrayList.add(convertToAccessibilityWindow.get());
            }
        }
        return arrayList;
    }

    public static Optional<AccessibilityInfo> getRootAccessibilityInfoByWindowId(int i, int i2) {
        if (i2 == -1 || i == -1) {
            return Optional.empty();
        }
        AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(i2, i, AccessibilityNodeInfo.ROOT_NODE_ID, true, 4, (Bundle) null);
        if (findAccessibilityNodeInfoByAccessibilityId == null) {
            return Optional.empty();
        }
        Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(findAccessibilityNodeInfoByAccessibilityId);
        findAccessibilityNodeInfoByAccessibilityId.recycle();
        return convertToAccessibilityInfo;
    }

    public static Optional<AccessibilityWindow> getChild(int i, int i2) {
        if (i2 == -1 || i == -1) {
            return Optional.empty();
        }
        AccessibilityWindowInfo window = AccessibilityInteractionClient.getInstance().getWindow(i2, i);
        if (window == null) {
            return Optional.empty();
        }
        Optional<AccessibilityWindow> convertToAccessibilityWindow = AccessibilityBeanConverter.convertToAccessibilityWindow(window);
        window.recycle();
        return convertToAccessibilityWindow;
    }

    public static Optional<AccessibilityWindow> getParent(int i, int i2) {
        if (i2 == -1 || i == -1) {
            return Optional.empty();
        }
        AccessibilityWindowInfo window = AccessibilityInteractionClient.getInstance().getWindow(i2, i);
        if (window == null) {
            return Optional.empty();
        }
        Optional<AccessibilityWindow> convertToAccessibilityWindow = AccessibilityBeanConverter.convertToAccessibilityWindow(window);
        window.recycle();
        return convertToAccessibilityWindow;
    }

    public static Optional<AccessibilityInfo> getAnchor(int i, int i2, int i3) {
        if (i3 != -1) {
            long j = (long) i2;
            if (!(j == AccessibilityNodeInfo.UNDEFINED_NODE_ID || i == -1)) {
                AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(i3, i, j, true, 0, (Bundle) null);
                if (findAccessibilityNodeInfoByAccessibilityId == null) {
                    return Optional.empty();
                }
                Optional<AccessibilityInfo> convertToAccessibilityInfo = AccessibilityBeanConverter.convertToAccessibilityInfo(findAccessibilityNodeInfoByAccessibilityId);
                findAccessibilityNodeInfoByAccessibilityId.recycle();
                return convertToAccessibilityInfo;
            }
        }
        return Optional.empty();
    }
}
