package ohos.accessibility.adapter;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityRecord;
import java.util.ArrayList;
import java.util.List;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.accessibility.utils.LogUtil;
import ohos.app.Context;

public class AccessibilityViewAbilityAdapter {
    private static final int ABILITY_ACE = 1;
    private static final int ABILITY_JAVA_UI = 0;
    private static final String TAG = "AccessibilityViewAbilityAdapter";
    private static final int TYPE_ACCESSIBILITY_ABILITY_INTERRUPT = -1;
    private static final int TYPE_ACE_DIALOG_DISMISS = 32;
    private static final int TYPE_SCROLL_START = 16777216;
    private int abilityType = 0;
    private AccessibilityManager accessibilityManager = null;
    private Activity hostActivity;
    private View hostView;

    public AccessibilityViewAbilityAdapter(Context context, int i) {
        if (context == null) {
            LogUtil.info(TAG, "create AccessibilityViewAbilityAdapter error, abilityContext is null, just return.");
            return;
        }
        Object hostContext = context.getHostContext();
        if (hostContext == null || !(hostContext instanceof Activity)) {
            LogUtil.error(TAG, "create AccessibilityViewAbilityAdapter error, context is null, just return.");
            return;
        }
        this.hostActivity = (Activity) hostContext;
        this.accessibilityManager = AccessibilityManager.getInstance(this.hostActivity.getApplicationContext());
        this.hostView = this.hostActivity.getWindow().findViewById(16908290);
        initBarrierFreeView(i);
    }

    private void initBarrierFreeView(int i) {
        BarrierFreeDelegateHelper barrierFreeDelegateHelper;
        LogUtil.info(TAG, "initBarrierFreeView start.");
        View view = this.hostView;
        if (view == null || this.hostActivity == null) {
            LogUtil.error(TAG, "initBarrierFreeView failed, hostView or hostActivity is null.");
            return;
        }
        if (i == 0) {
            barrierFreeDelegateHelper = new AccessibilityViewDelegate(view);
        } else if (i == 1) {
            barrierFreeDelegateHelper = new AccessibilityAceViewDelegate(view);
        } else {
            LogUtil.info(TAG, "initBarrierFreeView failed, ability type is illegal.");
            return;
        }
        this.abilityType = i;
        this.hostView.setAccessibilityDelegate(barrierFreeDelegateHelper);
        View decorView = this.hostActivity.getWindow().getDecorView();
        if (decorView == null) {
            LogUtil.error(TAG, "initBarrierFreeView end, decorView is null.");
            return;
        }
        AdapterTouchDelegate adapterTouchDelegate = new AdapterTouchDelegate(new Rect(0, 0, 2147483646, 2147483646), this.hostView, barrierFreeDelegateHelper);
        for (View view2 : getAllChildViews(decorView)) {
            view2.setTouchDelegate(adapterTouchDelegate);
        }
        decorView.setTouchDelegate(adapterTouchDelegate);
        LogUtil.info(TAG, "initBarrierFreeView end.");
    }

    public void releaseBarrierFreeView() {
        clearBarrierFreeFocus();
        View view = this.hostView;
        if (view != null) {
            view.setAccessibilityDelegate(null);
            this.hostView = null;
        }
        Activity activity = this.hostActivity;
        if (activity != null) {
            View decorView = activity.getWindow().getDecorView();
            if (decorView != null) {
                for (View view2 : getAllChildViews(decorView)) {
                    view2.setTouchDelegate(null);
                }
            }
            this.hostActivity = null;
        }
    }

    public void clearBarrierFreeFocus() {
        Activity activity = this.hostActivity;
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                /* class ohos.accessibility.adapter.$$Lambda$AccessibilityViewAbilityAdapter$mDB6e3eESqRcXuY0c1xcua9reZM */

                @Override // java.lang.Runnable
                public final void run() {
                    AccessibilityViewAbilityAdapter.this.lambda$clearBarrierFreeFocus$0$AccessibilityViewAbilityAdapter();
                }
            });
        }
    }

    public /* synthetic */ void lambda$clearBarrierFreeFocus$0$AccessibilityViewAbilityAdapter() {
        View view = this.hostView;
        if (view != null) {
            view.clearAccessibilityFocus();
            LogUtil.debug(TAG, "clearBarrierFreeFocus successful.");
        }
    }

    private static List<View> getAllChildViews(View view) {
        ArrayList arrayList = new ArrayList();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childAt = viewGroup.getChildAt(i);
                arrayList.add(childAt);
                arrayList.addAll(getAllChildViews(childAt));
            }
        }
        return arrayList;
    }

    public boolean sendEvent(AccessibilityEventInfo accessibilityEventInfo) {
        LogUtil.info(TAG, "sendEventInfo start.");
        if (accessibilityEventInfo == null) {
            LogUtil.error(TAG, "eventInfo is null.");
            return false;
        }
        LogUtil.info(TAG, "sendEvent id:" + accessibilityEventInfo.getViewId() + " type:" + accessibilityEventInfo.getAccessibilityEventType());
        if (this.accessibilityManager == null) {
            LogUtil.error(TAG, "manager is null.");
            return false;
        } else if (startProcessCustomEvent(accessibilityEventInfo)) {
            return true;
        } else {
            this.accessibilityManager.sendAccessibilityEvent(convertEventInfo(accessibilityEventInfo));
            return true;
        }
    }

    private boolean startProcessCustomEvent(AccessibilityEventInfo accessibilityEventInfo) {
        int accessibilityEventType = accessibilityEventInfo.getAccessibilityEventType();
        if (accessibilityEventType == -1) {
            this.accessibilityManager.interrupt();
            return true;
        } else if (accessibilityEventType == 32) {
            clearBarrierFreeFocus();
            return false;
        } else if (accessibilityEventType == 200) {
            LogUtil.info(TAG, "start refresh ace page. ");
            clearBarrierFreeFocus();
            return true;
        } else if (accessibilityEventType != 16777216) {
            return false;
        } else {
            LogUtil.info(TAG, "scroll start, clear barrier free focus.");
            clearBarrierFreeFocus();
            return true;
        }
    }

    private AccessibilityEvent convertEventInfo(AccessibilityEventInfo accessibilityEventInfo) {
        AccessibilityEvent obtain = AccessibilityEvent.obtain(accessibilityEventInfo.getAccessibilityEventType());
        fillEventWithSourceInfo(obtain, accessibilityEventInfo);
        obtain.setAction(accessibilityEventInfo.getTriggerAction());
        obtain.setContentChangeTypes(accessibilityEventInfo.getWindowChangeTypes());
        obtain.setMovementGranularity(accessibilityEventInfo.getTextMoveStep());
        View view = this.hostView;
        if (!(view == null || view.getContext() == null)) {
            obtain.setPackageName(this.hostView.getContext().getPackageName());
        }
        ArrayList<AccessibilityEventInfo> records = accessibilityEventInfo.getRecords();
        if (!records.isEmpty()) {
            for (AccessibilityEventInfo accessibilityEventInfo2 : records) {
                if (accessibilityEventInfo2 != null) {
                    AccessibilityRecord obtain2 = AccessibilityRecord.obtain();
                    fillEventWithSourceInfo(obtain2, accessibilityEventInfo2);
                    obtain.appendRecord(obtain2);
                }
            }
        }
        return obtain;
    }

    private void fillEventWithSourceInfo(AccessibilityRecord accessibilityRecord, AccessibilityEventInfo accessibilityEventInfo) {
        accessibilityRecord.setSource(this.hostView, accessibilityEventInfo.getViewId());
        accessibilityRecord.setImportantForAccessibility(true);
        if (this.abilityType == 0) {
            String valueOf = String.valueOf(accessibilityEventInfo.getClassName());
            accessibilityRecord.setClassName(AccessibilityConst.getViewTypeClass(valueOf, valueOf));
        }
        accessibilityRecord.setContentDescription(accessibilityEventInfo.getDescription());
        accessibilityRecord.setItemCount(accessibilityEventInfo.getCount());
        accessibilityRecord.setCurrentItemIndex(accessibilityEventInfo.getCurrentIndex());
    }
}
