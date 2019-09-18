package android.support.v4.view.accessibility;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityNodeProviderCompat {
    public static final int HOST_VIEW_ID = -1;
    private final Object mProvider;

    @RequiresApi(16)
    static class AccessibilityNodeProviderApi16 extends AccessibilityNodeProvider {
        final AccessibilityNodeProviderCompat mCompat;

        AccessibilityNodeProviderApi16(AccessibilityNodeProviderCompat compat) {
            this.mCompat = compat;
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            AccessibilityNodeInfoCompat compatInfo = this.mCompat.createAccessibilityNodeInfo(virtualViewId);
            if (compatInfo == null) {
                return null;
            }
            return compatInfo.unwrap();
        }

        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String text, int virtualViewId) {
            List<AccessibilityNodeInfoCompat> compatInfos = this.mCompat.findAccessibilityNodeInfosByText(text, virtualViewId);
            if (compatInfos == null) {
                return null;
            }
            List<AccessibilityNodeInfo> infoList = new ArrayList<>();
            int infoCount = compatInfos.size();
            for (int i = 0; i < infoCount; i++) {
                infoList.add(compatInfos.get(i).unwrap());
            }
            return infoList;
        }

        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return this.mCompat.performAction(virtualViewId, action, arguments);
        }
    }

    @RequiresApi(19)
    static class AccessibilityNodeProviderApi19 extends AccessibilityNodeProviderApi16 {
        AccessibilityNodeProviderApi19(AccessibilityNodeProviderCompat compat) {
            super(compat);
        }

        public AccessibilityNodeInfo findFocus(int focus) {
            AccessibilityNodeInfoCompat compatInfo = this.mCompat.findFocus(focus);
            if (compatInfo == null) {
                return null;
            }
            return compatInfo.unwrap();
        }
    }

    public AccessibilityNodeProviderCompat() {
        if (Build.VERSION.SDK_INT >= 19) {
            this.mProvider = new AccessibilityNodeProviderApi19(this);
        } else if (Build.VERSION.SDK_INT >= 16) {
            this.mProvider = new AccessibilityNodeProviderApi16(this);
        } else {
            this.mProvider = null;
        }
    }

    public AccessibilityNodeProviderCompat(Object provider) {
        this.mProvider = provider;
    }

    public Object getProvider() {
        return this.mProvider;
    }

    @Nullable
    public AccessibilityNodeInfoCompat createAccessibilityNodeInfo(int virtualViewId) {
        return null;
    }

    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        return false;
    }

    @Nullable
    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByText(String text, int virtualViewId) {
        return null;
    }

    @Nullable
    public AccessibilityNodeInfoCompat findFocus(int focus) {
        return null;
    }
}
