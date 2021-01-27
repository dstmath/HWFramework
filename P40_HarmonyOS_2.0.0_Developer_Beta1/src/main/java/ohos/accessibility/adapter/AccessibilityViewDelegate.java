package ohos.accessibility.adapter;

import android.os.Bundle;
import android.util.IntArray;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityViewDelegate extends BarrierFreeDelegateHelper {
    private static final int ROOT_VIEW_ID = 0;
    private static final String TAG = "AccessibilityViewDelegate";

    public AccessibilityViewDelegate(View view) {
        super(view);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateAllViewIds(IntArray intArray) {
        AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(0);
        if (queryAccessibilityViewInfoById != null && queryAccessibilityViewInfoById.getChildIdList().length > 0) {
            intArray.addAll(IntArray.wrap(queryAccessibilityViewInfoById.getChildIdList()));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateEvent(AccessibilityViewInfo accessibilityViewInfo, AccessibilityEvent accessibilityEvent) {
        if (accessibilityViewInfo != null && accessibilityEvent != null) {
            accessibilityEvent.setClassName(AccessibilityConst.getViewTypeClass(accessibilityViewInfo.getClassName(), accessibilityViewInfo.getClassName()));
            LogUtil.info(TAG, "PopulateEvent, viewId:" + accessibilityViewInfo.getId() + " event:" + accessibilityEvent.getEventType());
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
        LogUtil.info(TAG, "onPerformActionForVirtualView id:" + i + " action:" + i2);
        return AccessibilityNativeTools.onAccessibilityEvent(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateNodeInfo(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityViewInfo == null || accessibilityNodeInfo == null) {
            LogUtil.error(TAG, "onPopulateNodeInfo failed, node is null.");
            return;
        }
        accessibilityNodeInfo.setClassName(AccessibilityConst.getViewTypeClass(accessibilityViewInfo.getClassName(), accessibilityViewInfo.getClassName()));
        populateActionsForNode(accessibilityViewInfo, accessibilityNodeInfo);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public AccessibilityViewInfo queryAccessibilityViewInfoById(int i) {
        return AccessibilityNativeTools.getAccessibilityNodeInfoById(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public AccessibilityViewInfo queryRootAccessibilityViewInfo() {
        return AccessibilityNativeTools.getAccessibilityNodeInfoById(0);
    }

    private void populateActionsForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        int[] actionList = accessibilityViewInfo.getActionList();
        if (actionList.length > 0) {
            for (int i = 0; i < actionList.length; i++) {
                LogUtil.info(TAG, "initNodeInfoFromView add id:" + accessibilityViewInfo.getId() + " action:" + actionList[i]);
                try {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(actionList[i], null));
                } catch (IllegalArgumentException unused) {
                    LogUtil.error(TAG, "action is is illegal, just skip, action:" + actionList[i]);
                }
            }
        }
    }
}
