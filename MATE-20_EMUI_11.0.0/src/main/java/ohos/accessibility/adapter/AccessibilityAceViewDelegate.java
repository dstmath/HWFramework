package ohos.accessibility.adapter;

import android.app.ActionBar;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.IntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.HashMap;
import java.util.Map;
import ohos.accessibility.utils.LogUtil;
import ohos.ai.engine.bigreport.BigReportKeyValue;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

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
    private static final Map<String, String> CLASS_MAP = new HashMap();
    private static final int CUSTOM_ACTION = 2;
    private static final int GLOBAL_ACTION_BACK = 1;
    private static final int LOCATION_SIZE = 2;
    private static final String TAG = "AccessibilityAceViewDelegate";
    private View mHostView;

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
        CLASS_MAP.put("text", TextView.class.getName());
        CLASS_MAP.put("span", TextView.class.getName());
        CLASS_MAP.put("titleText", TextView.class.getName());
        CLASS_MAP.put("headerText", TextView.class.getName());
        CLASS_MAP.put("textarea", EditText.class.getName());
        CLASS_MAP.put("div", ViewGroup.class.getName());
        CLASS_MAP.put("progress", ProgressBar.class.getName());
        CLASS_MAP.put("list-item", ViewGroup.class.getName());
        CLASS_MAP.put(SchemaSymbols.ATTVAL_LIST, ListView.class.getName());
        CLASS_MAP.put("rating", RatingBar.class.getName());
        CLASS_MAP.put("switch", Switch.class.getName());
        CLASS_MAP.put(BigReportKeyValue.TYPE_IMAGE, ImageView.class.getName());
        CLASS_MAP.put("button", Button.class.getName());
        CLASS_MAP.put("dialogButton", Button.class.getName());
        CLASS_MAP.put("checkbox", CheckBox.class.getName());
        CLASS_MAP.put("radio", RadioButton.class.getName());
        CLASS_MAP.put("search", SearchView.class.getName());
        CLASS_MAP.put(Constants.ATTRNAME_SELECT, Spinner.class.getName());
        CLASS_MAP.put("menu", ActionMenuView.class.getName());
        CLASS_MAP.put("navigation-bar", ActionBar.class.getName());
        CLASS_MAP.put("slider", SeekBar.class.getName());
        CLASS_MAP.put("Toast", Toast.class.getName());
        CLASS_MAP.put(BigReportKeyValue.TYPE_VIDEO, VideoView.class.getName());
        CLASS_MAP.put("canvas", Canvas.class.getName());
    }

    public AccessibilityAceViewDelegate(View view) {
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
            accessibilityEvent.setClassName(CLASS_MAP.getOrDefault(viewType, viewType));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.accessibility.adapter.BarrierFreeDelegateHelper
    public void onPopulateNodeInfo(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityViewInfo != null && accessibilityNodeInfo != null) {
            String viewType = accessibilityViewInfo.getViewType();
            if ("input".equals(viewType) && accessibilityViewInfo.getComponentInputType() != null) {
                viewType = accessibilityViewInfo.getComponentInputType();
            }
            accessibilityNodeInfo.setClassName(CLASS_MAP.getOrDefault(viewType, viewType));
            int maxTextLength = accessibilityViewInfo.getMaxTextLength();
            if (maxTextLength <= 0) {
                maxTextLength = Integer.MAX_VALUE;
            }
            accessibilityNodeInfo.setMaxTextLength(maxTextLength);
            populateActionsForNode(accessibilityViewInfo, accessibilityNodeInfo);
            LogUtil.info(TAG, "onPopulateNodeInfo end, id:" + accessibilityViewInfo.getId());
        }
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

    private AccessibilityViewInfo getClickedAccessibilityView(AccessibilityViewInfo accessibilityViewInfo, int i, int i2) {
        Rect rectOnScreen = getRectOnScreen(accessibilityViewInfo.getRect());
        LogUtil.info(TAG, "getVirtualViewAt clickId:" + accessibilityViewInfo.getId() + " rawX:" + i + " rawY:" + i2 + " groupRegion:" + rectOnScreen.toString());
        AccessibilityViewInfo accessibilityViewInfo2 = null;
        if (accessibilityViewInfo.getId() > 0 && !rectOnScreen.contains(i, i2)) {
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
                        LogUtil.info(TAG, "getVirtualViewAt can not find view:" + i4);
                    } else {
                        accessibilityViewInfo2 = getClickedAccessibilityView(queryAccessibilityViewInfoById, i, i2);
                        if (accessibilityViewInfo2 != null) {
                            LogUtil.info(TAG, "getVirtualViewAt end, tempViewInfo:" + accessibilityViewInfo2.getId());
                            break;
                        }
                    }
                }
                i3++;
            }
        }
        return accessibilityViewInfo2 == null ? accessibilityViewInfo : accessibilityViewInfo2;
    }

    private Rect getRectOnScreen(ohos.agp.utils.Rect rect) {
        int[] iArr = new int[2];
        View view = this.mHostView;
        if (view != null) {
            view.getLocationOnScreen(iArr);
        }
        Rect rect2 = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        rect2.offset(iArr[0], iArr[1]);
        return rect2;
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
