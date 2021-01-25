package android.view.accessibility;

import android.os.Build;
import android.util.ArraySet;
import android.util.Log;
import android.util.LongArray;
import android.util.LongSparseArray;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityCache {
    public static final int CACHE_CRITICAL_EVENTS_MASK = 4307005;
    private static final boolean CHECK_INTEGRITY = Build.IS_ENG;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityCache";
    private long mAccessibilityFocus = 2147483647L;
    private final AccessibilityNodeRefresher mAccessibilityNodeRefresher;
    private long mInputFocus = 2147483647L;
    private boolean mIsAllWindowsCached;
    private final Object mLock = new Object();
    private final SparseArray<LongSparseArray<AccessibilityNodeInfo>> mNodeCache = new SparseArray<>();
    private final SparseArray<AccessibilityWindowInfo> mTempWindowArray = new SparseArray<>();
    private final SparseArray<AccessibilityWindowInfo> mWindowCache = new SparseArray<>();

    public AccessibilityCache(AccessibilityNodeRefresher nodeRefresher) {
        this.mAccessibilityNodeRefresher = nodeRefresher;
    }

    public void setWindows(List<AccessibilityWindowInfo> windows) {
        synchronized (this.mLock) {
            clearWindowCache();
            if (windows != null) {
                int windowCount = windows.size();
                for (int i = 0; i < windowCount; i++) {
                    addWindow(windows.get(i));
                }
                this.mIsAllWindowsCached = true;
            }
        }
    }

    public void addWindow(AccessibilityWindowInfo window) {
        synchronized (this.mLock) {
            this.mWindowCache.put(window.getId(), new AccessibilityWindowInfo(window));
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
                    break;
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
        AccessibilityNodeInfo cachedInfo;
        LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
        if (nodes != null && (cachedInfo = nodes.get(sourceId)) != null && !this.mAccessibilityNodeRefresher.refreshNode(cachedInfo, true)) {
            clearSubTreeLocked(windowId, sourceId);
        }
    }

    public AccessibilityNodeInfo getNode(int windowId, long accessibilityNodeId) {
        synchronized (this.mLock) {
            LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
            if (nodes == null) {
                return null;
            }
            AccessibilityNodeInfo info = nodes.get(accessibilityNodeId);
            if (info != null) {
                info = new AccessibilityNodeInfo(info);
            }
            return info;
        }
    }

    public List<AccessibilityWindowInfo> getWindows() {
        synchronized (this.mLock) {
            if (!this.mIsAllWindowsCached) {
                return null;
            }
            int windowCount = this.mWindowCache.size();
            if (windowCount <= 0) {
                return null;
            }
            SparseArray<AccessibilityWindowInfo> sortedWindows = this.mTempWindowArray;
            sortedWindows.clear();
            for (int i = 0; i < windowCount; i++) {
                AccessibilityWindowInfo window = this.mWindowCache.valueAt(i);
                sortedWindows.put(window.getLayer(), window);
            }
            int sortedWindowCount = sortedWindows.size();
            List<AccessibilityWindowInfo> windows = new ArrayList<>(sortedWindowCount);
            for (int i2 = sortedWindowCount - 1; i2 >= 0; i2--) {
                windows.add(new AccessibilityWindowInfo(sortedWindows.valueAt(i2)));
                sortedWindows.removeAt(i2);
            }
            return windows;
        }
    }

    public AccessibilityWindowInfo getWindow(int windowId) {
        synchronized (this.mLock) {
            AccessibilityWindowInfo window = this.mWindowCache.get(windowId);
            if (window == null) {
                return null;
            }
            return new AccessibilityWindowInfo(window);
        }
    }

    public void add(AccessibilityNodeInfo info) {
        synchronized (this.mLock) {
            int windowId = info.getWindowId();
            LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
            if (nodes == null) {
                nodes = new LongSparseArray<>();
                this.mNodeCache.put(windowId, nodes);
            }
            long sourceId = info.getSourceNodeId();
            AccessibilityNodeInfo oldInfo = nodes.get(sourceId);
            if (oldInfo != null) {
                LongArray newChildrenIds = info.getChildNodeIds();
                int oldChildCount = oldInfo.getChildCount();
                for (int i = 0; i < oldChildCount; i++) {
                    long oldChildId = oldInfo.getChildId(i);
                    if (newChildrenIds == null || newChildrenIds.indexOf(oldChildId) < 0) {
                        clearSubTreeLocked(windowId, oldChildId);
                    }
                    if (nodes.get(sourceId) == null) {
                        clearNodesForWindowLocked(windowId);
                        return;
                    }
                }
                long oldParentId = oldInfo.getParentNodeId();
                if (info.getParentNodeId() != oldParentId) {
                    clearSubTreeLocked(windowId, oldParentId);
                }
            }
            AccessibilityNodeInfo clone = new AccessibilityNodeInfo(info);
            nodes.put(sourceId, clone);
            if (clone.isAccessibilityFocused()) {
                if (!(this.mAccessibilityFocus == 2147483647L || this.mAccessibilityFocus == sourceId)) {
                    refreshCachedNodeLocked(windowId, this.mAccessibilityFocus);
                }
                this.mAccessibilityFocus = sourceId;
            } else if (this.mAccessibilityFocus == sourceId) {
                this.mAccessibilityFocus = 2147483647L;
            }
            if (clone.isFocused()) {
                this.mInputFocus = sourceId;
            }
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            clearWindowCache();
            for (int i = this.mNodeCache.size() - 1; i >= 0; i--) {
                clearNodesForWindowLocked(this.mNodeCache.keyAt(i));
            }
            this.mAccessibilityFocus = 2147483647L;
            this.mInputFocus = 2147483647L;
        }
    }

    private void clearWindowCache() {
        this.mWindowCache.clear();
        this.mIsAllWindowsCached = false;
    }

    private void clearNodesForWindowLocked(int windowId) {
        if (this.mNodeCache.get(windowId) != null) {
            this.mNodeCache.remove(windowId);
        }
    }

    private void clearSubTreeLocked(int windowId, long rootNodeId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
        if (nodes != null) {
            clearSubTreeRecursiveLocked(nodes, rootNodeId);
        }
    }

    private boolean clearSubTreeRecursiveLocked(LongSparseArray<AccessibilityNodeInfo> nodes, long rootNodeId) {
        AccessibilityNodeInfo current = nodes.get(rootNodeId);
        if (current == null) {
            clear();
            return true;
        }
        nodes.remove(rootNodeId);
        int childCount = current.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (clearSubTreeRecursiveLocked(nodes, current.getChildId(i))) {
                return true;
            }
        }
        return false;
    }

    public void checkIntegrity() {
        AccessibilityWindowInfo activeWindow;
        int windowCount;
        AccessibilityWindowInfo focusedWindow;
        AccessibilityWindowInfo activeWindow2;
        int windowCount2;
        AccessibilityWindowInfo focusedWindow2;
        int childCount;
        AccessibilityNodeInfo inputFocus;
        AccessibilityCache accessibilityCache = this;
        synchronized (accessibilityCache.mLock) {
            if (accessibilityCache.mWindowCache.size() > 0 || accessibilityCache.mNodeCache.size() != 0) {
                AccessibilityWindowInfo focusedWindow3 = null;
                AccessibilityWindowInfo activeWindow3 = null;
                int windowCount3 = accessibilityCache.mWindowCache.size();
                for (int i = 0; i < windowCount3; i++) {
                    AccessibilityWindowInfo window = accessibilityCache.mWindowCache.valueAt(i);
                    if (window.isActive()) {
                        if (activeWindow3 != null) {
                            Log.e(LOG_TAG, "Duplicate active window:" + window);
                        } else {
                            activeWindow3 = window;
                        }
                    }
                    if (window.isFocused()) {
                        if (focusedWindow3 != null) {
                            Log.e(LOG_TAG, "Duplicate focused window:" + window);
                        } else {
                            focusedWindow3 = window;
                        }
                    }
                }
                AccessibilityNodeInfo accessFocus = null;
                AccessibilityNodeInfo inputFocus2 = null;
                int nodesForWindowCount = accessibilityCache.mNodeCache.size();
                int i2 = 0;
                while (i2 < nodesForWindowCount) {
                    LongSparseArray<AccessibilityNodeInfo> nodes = accessibilityCache.mNodeCache.valueAt(i2);
                    if (nodes.size() <= 0) {
                        focusedWindow = focusedWindow3;
                        activeWindow = activeWindow3;
                        windowCount = windowCount3;
                    } else {
                        ArraySet<AccessibilityNodeInfo> seen = new ArraySet<>();
                        int windowId = accessibilityCache.mNodeCache.keyAt(i2);
                        int nodeCount = nodes.size();
                        int j = 0;
                        while (j < nodeCount) {
                            AccessibilityNodeInfo node = nodes.valueAt(j);
                            if (!seen.add(node)) {
                                focusedWindow2 = focusedWindow3;
                                Log.e(LOG_TAG, "Duplicate node: " + node + " in window:" + windowId);
                                activeWindow2 = activeWindow3;
                                windowCount2 = windowCount3;
                            } else {
                                focusedWindow2 = focusedWindow3;
                                if (node.isAccessibilityFocused()) {
                                    if (accessFocus != null) {
                                        Log.e(LOG_TAG, "Duplicate accessibility focus:" + node + " in window:" + windowId);
                                    } else {
                                        accessFocus = node;
                                    }
                                }
                                if (node.isFocused()) {
                                    if (inputFocus2 != null) {
                                        Log.e(LOG_TAG, "Duplicate input focus: " + node + " in window:" + windowId);
                                    } else {
                                        inputFocus2 = node;
                                    }
                                }
                                AccessibilityNodeInfo nodeParent = nodes.get(node.getParentNodeId());
                                if (nodeParent != null) {
                                    int childCount2 = nodeParent.getChildCount();
                                    boolean childOfItsParent = false;
                                    int k = 0;
                                    while (true) {
                                        if (k >= childCount2) {
                                            activeWindow2 = activeWindow3;
                                            windowCount2 = windowCount3;
                                            break;
                                        }
                                        activeWindow2 = activeWindow3;
                                        windowCount2 = windowCount3;
                                        if (nodes.get(nodeParent.getChildId(k)) == node) {
                                            childOfItsParent = true;
                                            break;
                                        }
                                        k++;
                                        windowCount3 = windowCount2;
                                        activeWindow3 = activeWindow2;
                                    }
                                    if (!childOfItsParent) {
                                        Log.e(LOG_TAG, "Invalid parent-child relation between parent: " + nodeParent + " and child: " + node);
                                    }
                                } else {
                                    activeWindow2 = activeWindow3;
                                    windowCount2 = windowCount3;
                                }
                                int childCount3 = node.getChildCount();
                                int k2 = 0;
                                while (k2 < childCount3) {
                                    AccessibilityNodeInfo child = nodes.get(node.getChildId(k2));
                                    if (child != null) {
                                        inputFocus = inputFocus2;
                                        if (nodes.get(child.getParentNodeId()) != node) {
                                            childCount = childCount3;
                                            Log.e(LOG_TAG, "Invalid child-parent relation between child: " + node + " and parent: " + nodeParent);
                                        } else {
                                            childCount = childCount3;
                                        }
                                    } else {
                                        childCount = childCount3;
                                        inputFocus = inputFocus2;
                                    }
                                    k2++;
                                    accessFocus = accessFocus;
                                    inputFocus2 = inputFocus;
                                    childCount3 = childCount;
                                }
                            }
                            j++;
                            focusedWindow3 = focusedWindow2;
                            windowCount3 = windowCount2;
                            activeWindow3 = activeWindow2;
                        }
                        focusedWindow = focusedWindow3;
                        activeWindow = activeWindow3;
                        windowCount = windowCount3;
                    }
                    i2++;
                    accessibilityCache = this;
                    focusedWindow3 = focusedWindow;
                    windowCount3 = windowCount;
                    activeWindow3 = activeWindow;
                }
            }
        }
    }

    public static class AccessibilityNodeRefresher {
        public boolean refreshNode(AccessibilityNodeInfo info, boolean bypassCache) {
            return info.refresh(null, bypassCache);
        }
    }
}
