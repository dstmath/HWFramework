package ohos.accessibility;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import ohos.accessibility.adapter.AccessibilityViewAbilityAdapter;
import ohos.accessibility.utils.LogUtil;
import ohos.app.Context;

public class BarrierFreeInnerClient {
    public static final int ABILITY_ACE = 1;
    public static final int ABILITY_JAVA_UI = 0;
    private static final Object LOCK_OBJ = new Object();
    private static final String TAG = "BarrierFreeInnerClient";
    private static Map<String, AccessibilityViewAbilityAdapter> sInstanceMap = new BarrierFreeAdapterMap(16);

    private BarrierFreeInnerClient() {
    }

    public static boolean registerBarrierFreeAbility(Context context, int i) {
        LogUtil.info(TAG, "registerBarrierFreeAbility start, type:" + i);
        if (context == null) {
            LogUtil.error(TAG, "registerBarrierFreeAbility failed, context is null.");
            return false;
        } else if (i == 0 || i == 1) {
            String mapKey = getMapKey(context);
            synchronized (LOCK_OBJ) {
                if (!sInstanceMap.containsKey(mapKey)) {
                    sInstanceMap.put(mapKey, new AccessibilityViewAbilityAdapter(context, i));
                }
                sInstanceMap.get(mapKey).clearBarrierFreeFocus();
            }
            LogUtil.info(TAG, "registerBarrierFreeAbility end.");
            return true;
        } else {
            LogUtil.error(TAG, "registerBarrierFreeAbility failed, type is illegal.");
            return false;
        }
    }

    public static boolean unRegisterBarrierFreeAbility(Context context) {
        LogUtil.info(TAG, "unRegisterBarrierFreeAbility start.");
        if (context == null) {
            LogUtil.error(TAG, "unRegisterBarrierFreeAbility failed, context is null.");
            return false;
        }
        String mapKey = getMapKey(context);
        synchronized (LOCK_OBJ) {
            if (!sInstanceMap.containsKey(mapKey)) {
                return false;
            }
            sInstanceMap.get(mapKey).releaseBarrierFreeView();
            sInstanceMap.remove(mapKey);
            LogUtil.info(TAG, "unRegisterBarrierFreeAbility end.");
            return true;
        }
    }

    public static boolean sendBarrierFreeEvent(Context context, AccessibilityEventInfo accessibilityEventInfo) {
        LogUtil.info(TAG, "sendBarrierFreeEvent start.");
        String mapKey = getMapKey(context);
        synchronized (LOCK_OBJ) {
            if (sInstanceMap.containsKey(mapKey)) {
                LogUtil.info(TAG, "Use default adapter, sendBarrierFreeEvent end.");
                return sInstanceMap.get(mapKey).sendEvent(accessibilityEventInfo);
            }
            Iterator<Map.Entry<String, AccessibilityViewAbilityAdapter>> it = sInstanceMap.entrySet().iterator();
            Map.Entry<String, AccessibilityViewAbilityAdapter> entry = null;
            while (it.hasNext()) {
                entry = it.next();
            }
            if (entry != null) {
                LogUtil.info(TAG, "Use last visited adapter, sendBarrierFreeEvent end.");
                return entry.getValue().sendEvent(accessibilityEventInfo);
            }
            LogUtil.info(TAG, "sendBarrierFreeEvent failed.");
            return false;
        }
    }

    public static void fillAccessibilityEventInfo(Context context, AccessibilityEventInfo accessibilityEventInfo, AccessibilityEvent accessibilityEvent) {
        LogUtil.info(TAG, "fillAccessibilityEventInfo start.");
        if (context == null) {
            LogUtil.error(TAG, "convertEvent failed, context is null.");
        } else if (accessibilityEventInfo == null || accessibilityEvent == null) {
            LogUtil.error(TAG, "convertEvent failed, event is null.");
        } else {
            View view = null;
            Object hostContext = context.getHostContext();
            if (hostContext != null && (hostContext instanceof Activity)) {
                view = ((Activity) hostContext).getWindow().findViewById(16908290);
            }
            if (view == null) {
                LogUtil.error(TAG, "convertEvent failed, rootView is null.");
                return;
            }
            fillRecordWithEventInfo(view, accessibilityEventInfo, accessibilityEvent);
            accessibilityEvent.setEventType(accessibilityEventInfo.getAccessibilityEventType());
            accessibilityEvent.setAction(accessibilityEventInfo.getTriggerAction());
            accessibilityEvent.setContentChangeTypes(accessibilityEventInfo.getWindowChangeTypes());
            accessibilityEvent.setMovementGranularity(accessibilityEventInfo.getTextMoveStep());
            if (!TextUtils.isEmpty(accessibilityEventInfo.getBundleName())) {
                accessibilityEvent.setPackageName(accessibilityEventInfo.getBundleName());
            }
            ArrayList<AccessibilityEventInfo> records = accessibilityEventInfo.getRecords();
            if (!records.isEmpty()) {
                for (AccessibilityEventInfo accessibilityEventInfo2 : records) {
                    if (accessibilityEventInfo2 != null) {
                        AccessibilityRecord obtain = AccessibilityRecord.obtain();
                        fillRecordWithEventInfo(view, accessibilityEventInfo2, obtain);
                        accessibilityEvent.appendRecord(obtain);
                    }
                }
            }
            LogUtil.info(TAG, "fillAccessibilityEventInfo end.");
        }
    }

    public static void fillBarrierFreeEventInfo(AccessibilityEvent accessibilityEvent, AccessibilityEventInfo accessibilityEventInfo) {
        LogUtil.info(TAG, "fillBarrierFreeEventInfo start.");
        if (accessibilityEvent == null || accessibilityEventInfo == null) {
            LogUtil.error(TAG, "convertEvent failed, event is null.");
            return;
        }
        fillEventInfoWithRecord(accessibilityEvent, accessibilityEventInfo);
        accessibilityEventInfo.setAccessibilityEventType(accessibilityEvent.getEventType());
        accessibilityEventInfo.setViewId(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityEvent.getSourceNodeId()));
        accessibilityEventInfo.setWindowChangeTypes(accessibilityEvent.getContentChangeTypes());
        accessibilityEventInfo.setTextMoveStep(accessibilityEvent.getMovementGranularity());
        accessibilityEventInfo.setTriggerAction(accessibilityEvent.getAction());
        if (!TextUtils.isEmpty(accessibilityEvent.getPackageName())) {
            accessibilityEventInfo.setBundleName(accessibilityEvent.getPackageName());
        }
        for (int i = 0; i < accessibilityEvent.getRecordCount(); i++) {
            AccessibilityRecord record = accessibilityEvent.getRecord(i);
            AccessibilityEventInfo accessibilityEventInfo2 = new AccessibilityEventInfo();
            fillEventInfoWithRecord(record, accessibilityEventInfo2);
            accessibilityEventInfo.addRecord(accessibilityEventInfo2);
        }
        LogUtil.info(TAG, "fillBarrierFreeEventInfo end.");
    }

    private static void fillRecordWithEventInfo(View view, AccessibilityEventInfo accessibilityEventInfo, AccessibilityRecord accessibilityRecord) {
        accessibilityRecord.setSource(view, accessibilityEventInfo.getViewId());
        accessibilityRecord.setImportantForAccessibility(true);
    }

    private static void fillEventInfoWithRecord(AccessibilityRecord accessibilityRecord, AccessibilityEventInfo accessibilityEventInfo) {
        accessibilityEventInfo.setViewId(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityRecord.getSourceNodeId()));
    }

    private static String getMapKey(Context context) {
        return context == null ? "" : context.toString();
    }

    private static class BarrierFreeAdapterMap<K, V> extends LinkedHashMap<K, V> {
        private static final int DEFAULT_CAPACITY = 16;
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;
        private static final long serialVersionUID = 1;
        private int maxEntityCount;

        BarrierFreeAdapterMap(int i) {
            super(16, DEFAULT_LOAD_FACTOR, true);
            this.maxEntityCount = i;
        }

        /* access modifiers changed from: protected */
        @Override // java.util.LinkedHashMap
        public boolean removeEldestEntry(Map.Entry<K, V> entry) {
            return size() > this.maxEntityCount;
        }
    }
}
