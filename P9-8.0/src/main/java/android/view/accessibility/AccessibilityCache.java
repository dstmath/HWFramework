package android.view.accessibility;

import android.os.Build;
import android.util.ArraySet;
import android.util.Log;
import android.util.LongArray;
import android.util.LongSparseArray;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

public final class AccessibilityCache {
    public static final int CACHE_CRITICAL_EVENTS_MASK = 4307005;
    private static final boolean CHECK_INTEGRITY = "eng".equals(Build.TYPE);
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityCache";
    private long mAccessibilityFocus = 2147483647L;
    private final AccessibilityNodeRefresher mAccessibilityNodeRefresher;
    private long mInputFocus = 2147483647L;
    private boolean mIsAllWindowsCached;
    private final Object mLock = new Object();
    private final SparseArray<LongSparseArray<AccessibilityNodeInfo>> mNodeCache = new SparseArray();
    private final SparseArray<AccessibilityWindowInfo> mTempWindowArray = new SparseArray();
    private final SparseArray<AccessibilityWindowInfo> mWindowCache = new SparseArray();

    public static class AccessibilityNodeRefresher {
        public boolean refreshNode(AccessibilityNodeInfo info, boolean bypassCache) {
            return info.refresh(null, bypassCache);
        }
    }

    public AccessibilityCache(AccessibilityNodeRefresher nodeRefresher) {
        this.mAccessibilityNodeRefresher = nodeRefresher;
    }

    public void setWindows(List<AccessibilityWindowInfo> windows) {
        synchronized (this.mLock) {
            clearWindowCache();
            if (windows == null) {
                return;
            }
            int windowCount = windows.size();
            for (int i = 0; i < windowCount; i++) {
                addWindow((AccessibilityWindowInfo) windows.get(i));
            }
            this.mIsAllWindowsCached = true;
        }
    }

    public void addWindow(AccessibilityWindowInfo window) {
        synchronized (this.mLock) {
            int windowId = window.getId();
            AccessibilityWindowInfo oldWindow = (AccessibilityWindowInfo) this.mWindowCache.get(windowId);
            if (oldWindow != null) {
                oldWindow.recycle();
            }
            this.mWindowCache.put(windowId, AccessibilityWindowInfo.obtain(window));
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        synchronized (this.mLock) {
            switch (event.getEventType()) {
                case 1:
                case 4:
                case 16:
                case 8192:
                    refreshCachedNodeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
                case 8:
                    if (this.mInputFocus != 2147483647L) {
                        refreshCachedNodeLocked(event.getWindowId(), this.mInputFocus);
                    }
                    this.mInputFocus = event.getSourceNodeId();
                    refreshCachedNodeLocked(event.getWindowId(), this.mInputFocus);
                    break;
                case 32:
                case 4194304:
                    clear();
                    break;
                case 2048:
                    synchronized (this.mLock) {
                        int windowId = event.getWindowId();
                        long sourceId = event.getSourceNodeId();
                        if ((event.getContentChangeTypes() & 1) != 0) {
                            clearSubTreeLocked(windowId, sourceId);
                        } else {
                            refreshCachedNodeLocked(windowId, sourceId);
                        }
                    }
                case 4096:
                    clearSubTreeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
                case 32768:
                    if (this.mAccessibilityFocus != 2147483647L) {
                        refreshCachedNodeLocked(event.getWindowId(), this.mAccessibilityFocus);
                    }
                    this.mAccessibilityFocus = event.getSourceNodeId();
                    refreshCachedNodeLocked(event.getWindowId(), this.mAccessibilityFocus);
                    break;
                case 65536:
                    if (this.mAccessibilityFocus == event.getSourceNodeId()) {
                        refreshCachedNodeLocked(event.getWindowId(), this.mAccessibilityFocus);
                        this.mAccessibilityFocus = 2147483647L;
                        break;
                    }
                    break;
            }
        }
        if (CHECK_INTEGRITY) {
            checkIntegrity();
        }
    }

    private void refreshCachedNodeLocked(int windowId, long sourceId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.get(windowId);
        if (nodes != null) {
            AccessibilityNodeInfo cachedInfo = (AccessibilityNodeInfo) nodes.get(sourceId);
            if (cachedInfo != null && !this.mAccessibilityNodeRefresher.refreshNode(cachedInfo, true)) {
                clearSubTreeLocked(windowId, sourceId);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AccessibilityNodeInfo getNode(int windowId, long accessibilityNodeId) {
        synchronized (this.mLock) {
            LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.get(windowId);
            if (nodes == null) {
                return null;
            }
            AccessibilityNodeInfo info = (AccessibilityNodeInfo) nodes.get(accessibilityNodeId);
            if (info != null) {
                info = AccessibilityNodeInfo.obtain(info);
            }
        }
    }

    public List<AccessibilityWindowInfo> getWindows() {
        synchronized (this.mLock) {
            if (this.mIsAllWindowsCached) {
                int windowCount = this.mWindowCache.size();
                if (windowCount > 0) {
                    int i;
                    SparseArray<AccessibilityWindowInfo> sortedWindows = this.mTempWindowArray;
                    sortedWindows.clear();
                    for (i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindowCache.valueAt(i);
                        sortedWindows.put(window.getLayer(), window);
                    }
                    int sortedWindowCount = sortedWindows.size();
                    List<AccessibilityWindowInfo> windows = new ArrayList(sortedWindowCount);
                    for (i = sortedWindowCount - 1; i >= 0; i--) {
                        windows.add(AccessibilityWindowInfo.obtain((AccessibilityWindowInfo) sortedWindows.valueAt(i)));
                        sortedWindows.removeAt(i);
                    }
                    return windows;
                }
                return null;
            }
            return null;
        }
    }

    public AccessibilityWindowInfo getWindow(int windowId) {
        synchronized (this.mLock) {
            AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindowCache.get(windowId);
            if (window != null) {
                AccessibilityWindowInfo obtain = AccessibilityWindowInfo.obtain(window);
                return obtain;
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:32:0x0088, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(AccessibilityNodeInfo info) {
        synchronized (this.mLock) {
            int windowId = info.getWindowId();
            LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.get(windowId);
            if (nodes == null) {
                nodes = new LongSparseArray();
                this.mNodeCache.put(windowId, nodes);
            }
            long sourceId = info.getSourceNodeId();
            AccessibilityNodeInfo oldInfo = (AccessibilityNodeInfo) nodes.get(sourceId);
            if (oldInfo != null) {
                LongArray newChildrenIds = info.getChildNodeIds();
                int oldChildCount = oldInfo.getChildCount();
                for (int i = 0; i < oldChildCount; i++) {
                    if (nodes.get(sourceId) == null) {
                        clearNodesForWindowLocked(windowId);
                        return;
                    }
                    long oldChildId = oldInfo.getChildId(i);
                    if (newChildrenIds == null || newChildrenIds.indexOf(oldChildId) < 0) {
                        clearSubTreeLocked(windowId, oldChildId);
                    }
                }
                long oldParentId = oldInfo.getParentNodeId();
                if (info.getParentNodeId() != oldParentId) {
                    clearSubTreeLocked(windowId, oldParentId);
                } else {
                    oldInfo.recycle();
                }
            }
            AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(info);
            nodes.put(sourceId, clone);
            if (clone.isAccessibilityFocused()) {
                this.mAccessibilityFocus = sourceId;
            }
            if (clone.isFocused()) {
                this.mInputFocus = sourceId;
            }
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            clearWindowCache();
            int nodesForWindowCount = this.mNodeCache.size();
            for (int i = 0; i < nodesForWindowCount; i++) {
                clearNodesForWindowLocked(this.mNodeCache.keyAt(i));
            }
            this.mAccessibilityFocus = 2147483647L;
            this.mInputFocus = 2147483647L;
        }
    }

    private void clearWindowCache() {
        for (int i = this.mWindowCache.size() - 1; i >= 0; i--) {
            ((AccessibilityWindowInfo) this.mWindowCache.valueAt(i)).recycle();
            this.mWindowCache.removeAt(i);
        }
        this.mIsAllWindowsCached = false;
    }

    private void clearNodesForWindowLocked(int windowId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.get(windowId);
        if (nodes != null) {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo info = (AccessibilityNodeInfo) nodes.valueAt(i);
                nodes.removeAt(i);
                info.recycle();
            }
            this.mNodeCache.remove(windowId);
        }
    }

    private void clearSubTreeLocked(int windowId, long rootNodeId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.get(windowId);
        if (nodes != null) {
            clearSubTreeRecursiveLocked(nodes, rootNodeId);
        }
    }

    private void clearSubTreeRecursiveLocked(LongSparseArray<AccessibilityNodeInfo> nodes, long rootNodeId) {
        AccessibilityNodeInfo current = (AccessibilityNodeInfo) nodes.get(rootNodeId);
        if (current != null) {
            nodes.remove(rootNodeId);
            int childCount = current.getChildCount();
            for (int i = 0; i < childCount; i++) {
                clearSubTreeRecursiveLocked(nodes, current.getChildId(i));
            }
            current.recycle();
        }
    }

    public void checkIntegrity() {
        synchronized (this.mLock) {
            if (this.mWindowCache.size() > 0 || this.mNodeCache.size() != 0) {
                int i;
                AccessibilityWindowInfo focusedWindow = null;
                AccessibilityWindowInfo activeWindow = null;
                int windowCount = this.mWindowCache.size();
                for (i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindowCache.valueAt(i);
                    if (window.isActive()) {
                        if (activeWindow != null) {
                            Log.e(LOG_TAG, "Duplicate active window:" + window);
                        } else {
                            activeWindow = window;
                        }
                    }
                    if (window.isFocused()) {
                        if (focusedWindow != null) {
                            Log.e(LOG_TAG, "Duplicate focused window:" + window);
                        } else {
                            focusedWindow = window;
                        }
                    }
                }
                AccessibilityNodeInfo accessFocus = null;
                AccessibilityNodeInfo inputFocus = null;
                int nodesForWindowCount = this.mNodeCache.size();
                for (i = 0; i < nodesForWindowCount; i++) {
                    LongSparseArray<AccessibilityNodeInfo> nodes = (LongSparseArray) this.mNodeCache.valueAt(i);
                    if (nodes.size() > 0) {
                        ArraySet<AccessibilityNodeInfo> seen = new ArraySet();
                        int windowId = this.mNodeCache.keyAt(i);
                        int nodeCount = nodes.size();
                        for (int j = 0; j < nodeCount; j++) {
                            AccessibilityNodeInfo node = (AccessibilityNodeInfo) nodes.valueAt(j);
                            if (seen.add(node)) {
                                int childCount;
                                int k;
                                if (node.isAccessibilityFocused()) {
                                    if (accessFocus != null) {
                                        Log.e(LOG_TAG, "Duplicate accessibility focus:" + node + " in window:" + windowId);
                                    } else {
                                        accessFocus = node;
                                    }
                                }
                                if (node.isFocused()) {
                                    if (inputFocus != null) {
                                        Log.e(LOG_TAG, "Duplicate input focus: " + node + " in window:" + windowId);
                                    } else {
                                        inputFocus = node;
                                    }
                                }
                                AccessibilityNodeInfo nodeParent = (AccessibilityNodeInfo) nodes.get(node.getParentNodeId());
                                if (nodeParent != null) {
                                    boolean childOfItsParent = false;
                                    childCount = nodeParent.getChildCount();
                                    for (k = 0; k < childCount; k++) {
                                        if (((AccessibilityNodeInfo) nodes.get(nodeParent.getChildId(k))) == node) {
                                            childOfItsParent = true;
                                            break;
                                        }
                                    }
                                    if (!childOfItsParent) {
                                        Log.e(LOG_TAG, "Invalid parent-child relation between parent: " + nodeParent + " and child: " + node);
                                    }
                                }
                                childCount = node.getChildCount();
                                for (k = 0; k < childCount; k++) {
                                    AccessibilityNodeInfo child = (AccessibilityNodeInfo) nodes.get(node.getChildId(k));
                                    if (!(child == null || ((AccessibilityNodeInfo) nodes.get(child.getParentNodeId())) == node)) {
                                        Log.e(LOG_TAG, "Invalid child-parent relation between child: " + node + " and parent: " + nodeParent);
                                    }
                                }
                            } else {
                                Log.e(LOG_TAG, "Duplicate node: " + node + " in window:" + windowId);
                            }
                        }
                    }
                }
                return;
            }
        }
    }
}
