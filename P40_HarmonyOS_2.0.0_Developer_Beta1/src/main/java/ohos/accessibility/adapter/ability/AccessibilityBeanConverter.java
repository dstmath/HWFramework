package ohos.accessibility.adapter.ability;

import android.graphics.Rect;
import android.os.Parcel;
import android.util.LongArray;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.Optional;
import ohos.accessibility.ability.AccessibilityInfo;
import ohos.accessibility.ability.AccessibilityWindow;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityBeanConverter {
    private static final String TAG = "AccessibilityBeanConverter";

    private AccessibilityBeanConverter() {
    }

    public static Optional<AccessibilityWindow> convertToAccessibilityWindow(AccessibilityWindowInfo accessibilityWindowInfo) {
        LogUtil.info(TAG, "convertToAccessibilityWindow start.");
        if (accessibilityWindowInfo == null) {
            return Optional.empty();
        }
        AccessibilityWindow accessibilityWindow = AccessibilityWindow.get();
        Parcel obtain = Parcel.obtain();
        accessibilityWindowInfo.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        accessibilityWindow.setWindowType(obtain.readInt());
        accessibilityWindow.setWindowLayer(obtain.readInt());
        int readInt = obtain.readInt();
        LogUtil.info(TAG, "convertToAccessibilityWindow booleanProperties:" + readInt);
        accessibilityWindow.setWindowId(obtain.readInt());
        accessibilityWindow.setParentId(obtain.readInt());
        Rect rect = new Rect();
        rect.readFromParcel(obtain);
        accessibilityWindow.setRectInScreen(rect.left, rect.top, rect.right, rect.bottom);
        accessibilityWindow.setWindowTitle(obtain.readCharSequence());
        accessibilityWindow.setAnchorId(obtain.readLong());
        int readInt2 = obtain.readInt();
        if (readInt2 > 0) {
            for (int i = 0; i < readInt2; i++) {
                accessibilityWindow.addChild(obtain.readInt());
            }
        }
        accessibilityWindow.setConnectionId(obtain.readInt());
        obtain.recycle();
        accessibilityWindow.setActive(accessibilityWindowInfo.isActive());
        accessibilityWindow.setFocused(accessibilityWindowInfo.isFocused());
        accessibilityWindow.setAccessibilityFocused(accessibilityWindowInfo.isAccessibilityFocused());
        LogUtil.info(TAG, "convertToAccessibilityWindow end.");
        return Optional.of(accessibilityWindow);
    }

    public static Optional<AccessibilityInfo> convertToAccessibilityInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        LogUtil.info(TAG, "convertToAccessibilityInfo start.");
        if (accessibilityNodeInfo == null) {
            return Optional.empty();
        }
        AccessibilityInfo accessibilityInfo = new AccessibilityInfo();
        accessibilityInfo.setAccessibilityId(accessibilityNodeInfo.getSourceNodeId());
        accessibilityInfo.setConnectionId(accessibilityNodeInfo.getConnectionId());
        accessibilityInfo.setWindowId(accessibilityNodeInfo.getWindowId());
        accessibilityInfo.setComponentResourceId(accessibilityNodeInfo.getViewIdResourceName());
        accessibilityInfo.setContent(accessibilityNodeInfo.getText());
        accessibilityInfo.setDescription(accessibilityNodeInfo.getContentDescription());
        accessibilityInfo.setBundleName(accessibilityNodeInfo.getPackageName());
        Rect boundsInScreen = accessibilityNodeInfo.getBoundsInScreen();
        if (boundsInScreen != null) {
            accessibilityInfo.setRectInScreen(boundsInScreen.left, boundsInScreen.top, boundsInScreen.right, boundsInScreen.bottom);
        }
        LongArray childNodeIds = accessibilityNodeInfo.getChildNodeIds();
        if (childNodeIds != null) {
            int size = childNodeIds.size();
            for (int i = 0; i < size; i++) {
                accessibilityInfo.addChildId(childNodeIds.get(i));
            }
        }
        LogUtil.info(TAG, "convertToAccessibilityInfo end.");
        return Optional.of(accessibilityInfo);
    }
}
