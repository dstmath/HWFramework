package android.support.v4.view.accessibility;

import android.support.annotation.RequiresApi;

@RequiresApi(19)
class AccessibilityNodeInfoCompatKitKat {

    static class RangeInfo {
        RangeInfo() {
        }

        static float getCurrent(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.RangeInfo) info).getCurrent();
        }

        static float getMax(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.RangeInfo) info).getMax();
        }

        static float getMin(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.RangeInfo) info).getMin();
        }

        static int getType(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.RangeInfo) info).getType();
        }
    }

    AccessibilityNodeInfoCompatKitKat() {
    }
}
