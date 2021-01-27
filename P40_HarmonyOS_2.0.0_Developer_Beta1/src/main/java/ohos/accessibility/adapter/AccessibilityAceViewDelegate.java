package ohos.accessibility.adapter;

import android.os.Bundle;
import android.util.IntArray;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.HashMap;
import java.util.Map;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityAceViewDelegate extends BarrierFreeDelegateHelper {
    private static final int ACTION_ACCESSIBILITY_FOCUS = 15;
    private static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 16;
    private static final int ACTION_CLICK = 10;
    private static final int ACTION_FOCUS = 14;
    private static final int ACTION_LONG_CLICK = 11;
    private static final Map<Integer, Integer> ACTION_MAP = new HashMap();
    private static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 17;
    private static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 18;
    private static final int ACTION_SCROLL_BACKWARD = 13;
    private static final int ACTION_SCROLL_FORWARD = 12;
    private static final String TAG = "AccessibilityAceViewDelegate";

    static {
        ACTION_MAP.put(10, 16);
        ACTION_MAP.put(11, 32);
        ACTION_MAP.put(12, 4096);
        ACTION_MAP.put(13, 8192);
        ACTION_MAP.put(14, 1);
        ACTION_MAP.put(15, 64);
        ACTION_MAP.put(16, 128);
        ACTION_MAP.put(17, 256);
        ACTION_MAP.put(18, 512);
    }

    public AccessibilityAceViewDelegate(View view) {
        super(view);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateAllViewIds(IntArray intArray) {
        AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(0);
        if (queryAccessibilityViewInfoById != null && queryAccessibilityViewInfoById.getChildIdList().length > 0) {
            intArray.addAll(IntArray.wrap(queryAccessibilityViewInfoById.getChildIdList()));
        }
        LogUtil.info(TAG, "onPopulateAllViewIds end.");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateEvent(AccessibilityViewInfo accessibilityViewInfo, AccessibilityEvent accessibilityEvent) {
        if (accessibilityViewInfo != null && accessibilityEvent != null) {
            LogUtil.info(TAG, "PopulateEvent, viewId:" + accessibilityViewInfo.getId() + " event:" + accessibilityEvent.getEventType());
            String viewType = accessibilityViewInfo.getViewType();
            if ("input".equals(viewType) && accessibilityViewInfo.getComponentInputType() != null) {
                viewType = accessibilityViewInfo.getComponentInputType();
            }
            accessibilityEvent.setClassName(AccessibilityConst.getViewTypeClass(viewType, viewType));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateNodeInfo(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityViewInfo == null || accessibilityNodeInfo == null) {
            LogUtil.error(TAG, "onPopulateNodeInfo failed, node is null.");
            return;
        }
        String viewType = accessibilityViewInfo.getViewType();
        if ("input".equals(viewType) && accessibilityViewInfo.getComponentInputType() != null) {
            viewType = accessibilityViewInfo.getComponentInputType();
        }
        accessibilityNodeInfo.setClassName(AccessibilityConst.getViewTypeClass(viewType, viewType));
        int maxTextLength = accessibilityViewInfo.getMaxTextLength();
        if (maxTextLength <= 0) {
            maxTextLength = Integer.MAX_VALUE;
        }
        accessibilityNodeInfo.setMaxTextLength(maxTextLength);
        populateActionsForNode(accessibilityViewInfo, accessibilityNodeInfo);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
        LogUtil.info(TAG, "onPerformActionForVirtualView id:" + i + " action:" + i2);
        return AccessibilityNativeAceMethods.performAction(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public AccessibilityViewInfo queryAccessibilityViewInfoById(int i) {
        return AccessibilityNativeAceMethods.getAccessibilityViewInfoById(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public AccessibilityViewInfo queryRootAccessibilityViewInfo() {
        return AccessibilityNativeAceMethods.getAccessibilityViewInfoById(0);
    }

    private void populateActionsForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        int[] actionList = accessibilityViewInfo.getActionList();
        if (actionList.length > 0) {
            for (int i = 0; i < actionList.length; i++) {
                try {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(ACTION_MAP.getOrDefault(Integer.valueOf(actionList[i]), 0).intValue(), null));
                } catch (IllegalArgumentException unused) {
                    LogUtil.error(TAG, "action is is illegal, just skip, action:" + actionList[i]);
                }
            }
        }
    }
}
