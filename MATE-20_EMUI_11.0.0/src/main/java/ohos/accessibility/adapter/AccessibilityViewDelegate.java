package ohos.accessibility.adapter;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.IntArray;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import ohos.accessibility.utils.LogUtil;

public class AccessibilityViewDelegate extends BarrierFreeDelegateHelper {
    private static final int POSITION_SIZE = 2;
    private static final int ROOT_VIEW_ID = 0;
    private static final String TAG = "AccessibilityViewDelegate";
    private View mHostView;

    public AccessibilityViewDelegate(View view) {
        super(view);
        this.mHostView = view;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public int getViewIdByCoordinates(float f, float f2) {
        AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(0);
        if (queryAccessibilityViewInfoById == null) {
            LogUtil.info(TAG, "getViewIdByCoordinates failed, root view is null.");
            return -1;
        }
        AccessibilityViewInfo clickedAccessibilityView = getClickedAccessibilityView(queryAccessibilityViewInfoById, (int) f, (int) f2);
        if (clickedAccessibilityView != null) {
            return clickedAccessibilityView.getId();
        }
        return -1;
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
        if (accessibilityViewInfo != null && accessibilityNodeInfo != null) {
            accessibilityNodeInfo.setClassName(AccessibilityConst.getViewTypeClass(accessibilityViewInfo.getClassName(), accessibilityViewInfo.getClassName()));
            populateActionsForNode(accessibilityViewInfo, accessibilityNodeInfo);
            LogUtil.info(TAG, "onPopulateNodeInfo end, id:" + accessibilityViewInfo.getId());
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public AccessibilityViewInfo queryAccessibilityViewInfoById(int i) {
        return AccessibilityNativeTools.getAccessibilityNodeInfoById(i);
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

    private Rect getRectInScreen(ohos.agp.utils.Rect rect) {
        int[] iArr = new int[2];
        View view = this.mHostView;
        if (view != null) {
            view.getLocationOnScreen(iArr);
        }
        Rect rect2 = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        rect2.offset(iArr[0], iArr[1]);
        return rect2;
    }

    private AccessibilityViewInfo getClickedAccessibilityView(AccessibilityViewInfo accessibilityViewInfo, int i, int i2) {
        Rect rectInScreen = getRectInScreen(accessibilityViewInfo.getRect());
        LogUtil.info(TAG, "getViewIdByCoordinates clickId:" + accessibilityViewInfo.getId() + " rawX:" + i + " rawY:" + i2 + " groupRegion:" + rectInScreen.toString());
        AccessibilityViewInfo accessibilityViewInfo2 = null;
        if (!rectInScreen.contains(i, i2)) {
            return null;
        }
        int[] childIdList = accessibilityViewInfo.getChildIdList();
        if (childIdList.length > 0) {
            int length = childIdList.length;
            int i3 = 0;
            while (true) {
                if (i3 >= length) {
                    break;
                }
                int i4 = childIdList[i3];
                if (i4 <= 0) {
                    LogUtil.info(TAG, "Child id is not right, just skip.");
                } else {
                    AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(i4);
                    if (queryAccessibilityViewInfoById == null) {
                        LogUtil.info(TAG, "getViewIdByCoordinates can not find view:" + i4);
                    } else {
                        accessibilityViewInfo2 = getClickedAccessibilityView(queryAccessibilityViewInfoById, i, i2);
                        if (accessibilityViewInfo2 != null) {
                            LogUtil.info(TAG, "getViewIdByCoordinates end, tempViewInfo:" + accessibilityViewInfo2.getId());
                            break;
                        }
                    }
                }
                i3++;
            }
        }
        return accessibilityViewInfo2 == null ? accessibilityViewInfo : accessibilityViewInfo2;
    }
}
