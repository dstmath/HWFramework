package android.view.accessibility;

import android.view.View;

public final class AccessibilityNodeIdManager {
    private static AccessibilityNodeIdManager sIdManager;
    private WeakSparseArray<View> mIdsToViews = new WeakSparseArray<>();

    public static synchronized AccessibilityNodeIdManager getInstance() {
        AccessibilityNodeIdManager accessibilityNodeIdManager;
        synchronized (AccessibilityNodeIdManager.class) {
            if (sIdManager == null) {
                sIdManager = new AccessibilityNodeIdManager();
            }
            accessibilityNodeIdManager = sIdManager;
        }
        return accessibilityNodeIdManager;
    }

    private AccessibilityNodeIdManager() {
    }

    public void registerViewWithId(View view, int id) {
        synchronized (this.mIdsToViews) {
            this.mIdsToViews.append(id, view);
        }
    }

    public void unregisterViewWithId(int id) {
        synchronized (this.mIdsToViews) {
            this.mIdsToViews.remove(id);
        }
    }

    public View findView(int id) {
        View view;
        synchronized (this.mIdsToViews) {
            view = this.mIdsToViews.get(id);
        }
        if (view == null || !view.includeForAccessibility()) {
            return null;
        }
        return view;
    }
}
