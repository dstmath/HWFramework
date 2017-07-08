package android.view.accessibility;

import android.util.ArraySet;
import android.util.Log;
import android.util.LongArray;
import android.util.LongSparseArray;
import android.util.SparseArray;
import com.android.internal.util.Protocol;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

final class AccessibilityCache {
    private static final boolean CHECK_INTEGRITY = false;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityCache";
    private long mAccessibilityFocus;
    private long mInputFocus;
    private boolean mIsAllWindowsCached;
    private final Object mLock;
    private final SparseArray<LongSparseArray<AccessibilityNodeInfo>> mNodeCache;
    private final SparseArray<AccessibilityWindowInfo> mTempWindowArray;
    private final SparseArray<AccessibilityWindowInfo> mWindowCache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityCache.<clinit>():void");
    }

    AccessibilityCache() {
        this.mLock = new Object();
        this.mAccessibilityFocus = 2147483647L;
        this.mInputFocus = 2147483647L;
        this.mWindowCache = new SparseArray();
        this.mNodeCache = new SparseArray();
        this.mTempWindowArray = new SparseArray();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAccessibilityEvent(AccessibilityEvent event) {
        synchronized (this.mLock) {
            switch (event.getEventType()) {
                case HwCfgFilePolicy.EMUI /*1*/:
                case HwCfgFilePolicy.CUST /*4*/:
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
                    refreshCachedNodeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (this.mInputFocus != 2147483647L) {
                        refreshCachedNodeLocked(event.getWindowId(), this.mInputFocus);
                    }
                    this.mInputFocus = event.getSourceNodeId();
                    refreshCachedNodeLocked(event.getWindowId(), this.mInputFocus);
                    break;
                case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                case AccessibilityEvent.TYPE_WINDOWS_CHANGED /*4194304*/:
                    clear();
                    break;
                case GL10.GL_EXP /*2048*/:
                    synchronized (this.mLock) {
                        int windowId = event.getWindowId();
                        long sourceId = event.getSourceNodeId();
                        if ((event.getContentChangeTypes() & 1) == 0) {
                            refreshCachedNodeLocked(windowId, sourceId);
                            break;
                        }
                        clearSubTreeLocked(windowId, sourceId);
                        break;
                    }
                    break;
                case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
                    clearSubTreeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
                case AccessibilityNodeInfo.ACTION_PASTE /*32768*/:
                    if (this.mAccessibilityFocus != 2147483647L) {
                        refreshCachedNodeLocked(event.getWindowId(), this.mAccessibilityFocus);
                    }
                    this.mAccessibilityFocus = event.getSourceNodeId();
                    refreshCachedNodeLocked(event.getWindowId(), this.mAccessibilityFocus);
                    break;
                case Protocol.BASE_SYSTEM_RESERVED /*65536*/:
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
            if (cachedInfo != null && !cachedInfo.refresh(true)) {
                clearSubTreeLocked(windowId, sourceId);
            }
        }
    }

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
            return info;
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
                    long oldChildId = oldInfo.getChildId(i);
                    if (newChildrenIds == null || newChildrenIds.indexOf(oldChildId) < 0) {
                        clearSubTreeLocked(windowId, oldChildId);
                    }
                }
                long oldParentId = oldInfo.getParentNodeId();
                if (info.getParentNodeId() != oldParentId) {
                    clearSubTreeLocked(windowId, oldParentId);
                }
            }
            nodes.put(sourceId, AccessibilityNodeInfo.obtain(info));
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
        this.mIsAllWindowsCached = DEBUG;
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
        }
    }

    public void checkIntegrity() {
        synchronized (this.mLock) {
            int i;
            if (this.mWindowCache.size() <= 0) {
                if (this.mNodeCache.size() == 0) {
                    return;
                }
            }
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
                                boolean childOfItsParent = DEBUG;
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
                                if (child != null) {
                                    if (((AccessibilityNodeInfo) nodes.get(child.getParentNodeId())) != node) {
                                        Log.e(LOG_TAG, "Invalid child-parent relation between child: " + node + " and parent: " + nodeParent);
                                    }
                                }
                            }
                        } else {
                            Log.e(LOG_TAG, "Duplicate node: " + node + " in window:" + windowId);
                        }
                    }
                }
            }
        }
    }
}
