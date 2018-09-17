package android.view.accessibility;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub;
import com.android.internal.os.HwBootFail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public final class AccessibilityInteractionClient extends Stub {
    private static final boolean CHECK_INTEGRITY = true;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityInteractionClient";
    public static final int NO_ID = -1;
    private static final long TIMEOUT_INTERACTION_MILLIS = 5000;
    private static final AccessibilityCache sAccessibilityCache = null;
    private static final LongSparseArray<AccessibilityInteractionClient> sClients = null;
    private static final SparseArray<IAccessibilityServiceConnection> sConnectionCache = null;
    private static final Object sStaticLock = null;
    private AccessibilityNodeInfo mFindAccessibilityNodeInfoResult;
    private List<AccessibilityNodeInfo> mFindAccessibilityNodeInfosResult;
    private final Object mInstanceLock;
    private volatile int mInteractionId;
    private final AtomicInteger mInteractionIdCounter;
    private boolean mPerformAccessibilityActionResult;
    private Message mSameThreadMessage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityInteractionClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityInteractionClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityInteractionClient.<clinit>():void");
    }

    public static AccessibilityInteractionClient getInstance() {
        return getInstanceForThread(Thread.currentThread().getId());
    }

    public static AccessibilityInteractionClient getInstanceForThread(long threadId) {
        AccessibilityInteractionClient client;
        synchronized (sStaticLock) {
            client = (AccessibilityInteractionClient) sClients.get(threadId);
            if (client == null) {
                client = new AccessibilityInteractionClient();
                sClients.put(threadId, client);
            }
        }
        return client;
    }

    private AccessibilityInteractionClient() {
        this.mInteractionIdCounter = new AtomicInteger();
        this.mInstanceLock = new Object();
        this.mInteractionId = NO_ID;
    }

    public void setSameThreadMessage(Message message) {
        synchronized (this.mInstanceLock) {
            this.mSameThreadMessage = message;
            this.mInstanceLock.notifyAll();
        }
    }

    public AccessibilityNodeInfo getRootInActiveWindow(int connectionId) {
        return findAccessibilityNodeInfoByAccessibilityId(connectionId, HwBootFail.STAGE_BOOT_SUCCESS, AccessibilityNodeInfo.ROOT_NODE_ID, DEBUG, 4);
    }

    public AccessibilityWindowInfo getWindow(int connectionId, int accessibilityWindowId) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                AccessibilityWindowInfo window = sAccessibilityCache.getWindow(accessibilityWindowId);
                if (window != null) {
                    return window;
                }
                long identityToken = Binder.clearCallingIdentity();
                window = connection.getWindow(accessibilityWindowId);
                Binder.restoreCallingIdentity(identityToken);
                if (window != null) {
                    sAccessibilityCache.addWindow(window);
                    return window;
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindow", re);
        }
        return null;
    }

    public List<AccessibilityWindowInfo> getWindows(int connectionId) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                List<AccessibilityWindowInfo> windows = sAccessibilityCache.getWindows();
                if (windows != null) {
                    return windows;
                }
                long identityToken = Binder.clearCallingIdentity();
                windows = connection.getWindows();
                Binder.restoreCallingIdentity(identityToken);
                if (windows != null) {
                    sAccessibilityCache.setWindows(windows);
                    return windows;
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindows", re);
        }
        return Collections.emptyList();
    }

    public AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId(int connectionId, int accessibilityWindowId, long accessibilityNodeId, boolean bypassCache, int prefetchFlags) {
        if ((prefetchFlags & 2) == 0 || (prefetchFlags & 1) != 0) {
            try {
                IAccessibilityServiceConnection connection = getConnection(connectionId);
                if (connection != null) {
                    if (!bypassCache) {
                        AccessibilityNodeInfo cachedInfo = sAccessibilityCache.getNode(accessibilityWindowId, accessibilityNodeId);
                        if (cachedInfo != null) {
                            return cachedInfo;
                        }
                    }
                    int interactionId = this.mInteractionIdCounter.getAndIncrement();
                    long identityToken = Binder.clearCallingIdentity();
                    boolean success = connection.findAccessibilityNodeInfoByAccessibilityId(accessibilityWindowId, accessibilityNodeId, interactionId, this, prefetchFlags, Thread.currentThread().getId());
                    Binder.restoreCallingIdentity(identityToken);
                    if (success) {
                        List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                        finalizeAndCacheAccessibilityNodeInfos(infos, connectionId);
                        if (!(infos == null || infos.isEmpty())) {
                            return (AccessibilityNodeInfo) infos.get(0);
                        }
                    }
                }
            } catch (Throwable re) {
                Log.e(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByAccessibilityId", re);
            }
            return null;
        }
        throw new IllegalArgumentException("FLAG_PREFETCH_SIBLINGS requires FLAG_PREFETCH_PREDECESSORS");
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(int connectionId, int accessibilityWindowId, long accessibilityNodeId, String viewId) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                boolean success = connection.findAccessibilityNodeInfosByViewId(accessibilityWindowId, accessibilityNodeId, viewId, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (success) {
                    List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                    if (infos != null) {
                        finalizeAndCacheAccessibilityNodeInfos(infos, connectionId);
                        return infos;
                    }
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByViewIdInActiveWindow", re);
        }
        return Collections.emptyList();
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(int connectionId, int accessibilityWindowId, long accessibilityNodeId, String text) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                boolean success = connection.findAccessibilityNodeInfosByText(accessibilityWindowId, accessibilityNodeId, text, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (success) {
                    List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                    if (infos != null) {
                        finalizeAndCacheAccessibilityNodeInfos(infos, connectionId);
                        return infos;
                    }
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfosByViewText", re);
        }
        return Collections.emptyList();
    }

    public AccessibilityNodeInfo findFocus(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int focusType) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                boolean success = connection.findFocus(accessibilityWindowId, accessibilityNodeId, focusType, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (success) {
                    AccessibilityNodeInfo info = getFindAccessibilityNodeInfoResultAndClear(interactionId);
                    finalizeAndCacheAccessibilityNodeInfo(info, connectionId);
                    return info;
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote findFocus", re);
        }
        return null;
    }

    public AccessibilityNodeInfo focusSearch(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int direction) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                boolean success = connection.focusSearch(accessibilityWindowId, accessibilityNodeId, direction, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (success) {
                    AccessibilityNodeInfo info = getFindAccessibilityNodeInfoResultAndClear(interactionId);
                    finalizeAndCacheAccessibilityNodeInfo(info, connectionId);
                    return info;
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote accessibilityFocusSearch", re);
        }
        return null;
    }

    public boolean performAccessibilityAction(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                boolean success = connection.performAccessibilityAction(accessibilityWindowId, accessibilityNodeId, action, arguments, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (success) {
                    return getPerformAccessibilityActionResultAndClear(interactionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote performAccessibilityAction", re);
        }
        return DEBUG;
    }

    public void clearCache() {
        sAccessibilityCache.clear();
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        sAccessibilityCache.onAccessibilityEvent(event);
    }

    private AccessibilityNodeInfo getFindAccessibilityNodeInfoResultAndClear(int interactionId) {
        AccessibilityNodeInfo accessibilityNodeInfo;
        synchronized (this.mInstanceLock) {
            accessibilityNodeInfo = waitForResultTimedLocked(interactionId) ? this.mFindAccessibilityNodeInfoResult : null;
            clearResultLocked();
        }
        return accessibilityNodeInfo;
    }

    public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info, int interactionId) {
        synchronized (this.mInstanceLock) {
            if (interactionId > this.mInteractionId) {
                this.mFindAccessibilityNodeInfoResult = info;
                this.mInteractionId = interactionId;
            }
            this.mInstanceLock.notifyAll();
        }
    }

    private List<AccessibilityNodeInfo> getFindAccessibilityNodeInfosResultAndClear(int interactionId) {
        List<AccessibilityNodeInfo> result;
        synchronized (this.mInstanceLock) {
            if (waitForResultTimedLocked(interactionId)) {
                result = this.mFindAccessibilityNodeInfosResult;
            } else {
                result = Collections.emptyList();
            }
            clearResultLocked();
            if (Build.IS_DEBUGGABLE) {
                checkFindAccessibilityNodeInfoResultIntegrity(result);
            }
        }
        return result;
    }

    public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) {
        synchronized (this.mInstanceLock) {
            if (interactionId > this.mInteractionId) {
                if (infos != null) {
                    if (Binder.getCallingPid() != Process.myPid() ? CHECK_INTEGRITY : DEBUG) {
                        this.mFindAccessibilityNodeInfosResult = infos;
                    } else {
                        this.mFindAccessibilityNodeInfosResult = new ArrayList(infos);
                    }
                } else {
                    this.mFindAccessibilityNodeInfosResult = Collections.emptyList();
                }
                this.mInteractionId = interactionId;
            }
            this.mInstanceLock.notifyAll();
        }
    }

    private boolean getPerformAccessibilityActionResultAndClear(int interactionId) {
        boolean z;
        synchronized (this.mInstanceLock) {
            z = waitForResultTimedLocked(interactionId) ? this.mPerformAccessibilityActionResult : DEBUG;
            clearResultLocked();
        }
        return z;
    }

    public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) {
        synchronized (this.mInstanceLock) {
            if (interactionId > this.mInteractionId) {
                this.mPerformAccessibilityActionResult = succeeded;
                this.mInteractionId = interactionId;
            }
            this.mInstanceLock.notifyAll();
        }
    }

    private void clearResultLocked() {
        this.mInteractionId = NO_ID;
        this.mFindAccessibilityNodeInfoResult = null;
        this.mFindAccessibilityNodeInfosResult = null;
        this.mPerformAccessibilityActionResult = DEBUG;
    }

    private boolean waitForResultTimedLocked(int interactionId) {
        long startTimeMillis = SystemClock.uptimeMillis();
        while (true) {
            try {
                Message sameProcessMessage = getSameProcessMessageAndClear();
                if (sameProcessMessage != null) {
                    sameProcessMessage.getTarget().handleMessage(sameProcessMessage);
                }
                if (this.mInteractionId == interactionId) {
                    return CHECK_INTEGRITY;
                }
                if (this.mInteractionId > interactionId) {
                    return DEBUG;
                }
                long waitTimeMillis = TIMEOUT_INTERACTION_MILLIS - (SystemClock.uptimeMillis() - startTimeMillis);
                if (waitTimeMillis <= 0) {
                    return DEBUG;
                }
                this.mInstanceLock.wait(waitTimeMillis);
            } catch (InterruptedException e) {
            }
        }
    }

    private void finalizeAndCacheAccessibilityNodeInfo(AccessibilityNodeInfo info, int connectionId) {
        if (info != null) {
            info.setConnectionId(connectionId);
            info.setSealed(CHECK_INTEGRITY);
            sAccessibilityCache.add(info);
        }
    }

    private void finalizeAndCacheAccessibilityNodeInfos(List<AccessibilityNodeInfo> infos, int connectionId) {
        if (infos != null) {
            int infosCount = infos.size();
            for (int i = 0; i < infosCount; i++) {
                finalizeAndCacheAccessibilityNodeInfo((AccessibilityNodeInfo) infos.get(i), connectionId);
            }
        }
    }

    private Message getSameProcessMessageAndClear() {
        Message result;
        synchronized (this.mInstanceLock) {
            result = this.mSameThreadMessage;
            this.mSameThreadMessage = null;
        }
        return result;
    }

    public IAccessibilityServiceConnection getConnection(int connectionId) {
        IAccessibilityServiceConnection iAccessibilityServiceConnection;
        synchronized (sConnectionCache) {
            iAccessibilityServiceConnection = (IAccessibilityServiceConnection) sConnectionCache.get(connectionId);
        }
        return iAccessibilityServiceConnection;
    }

    public void addConnection(int connectionId, IAccessibilityServiceConnection connection) {
        synchronized (sConnectionCache) {
            sConnectionCache.put(connectionId, connection);
        }
    }

    public void removeConnection(int connectionId) {
        synchronized (sConnectionCache) {
            sConnectionCache.remove(connectionId);
        }
    }

    private void checkFindAccessibilityNodeInfoResultIntegrity(List<AccessibilityNodeInfo> infos) {
        if (infos.size() != 0) {
            int i;
            int j;
            AccessibilityNodeInfo root = (AccessibilityNodeInfo) infos.get(0);
            int infoCount = infos.size();
            for (i = 1; i < infoCount; i++) {
                for (j = i; j < infoCount; j++) {
                    AccessibilityNodeInfo candidate = (AccessibilityNodeInfo) infos.get(j);
                    if (root.getParentNodeId() == candidate.getSourceNodeId()) {
                        root = candidate;
                        break;
                    }
                }
            }
            if (root == null) {
                Log.e(LOG_TAG, "No root.");
            }
            HashSet<AccessibilityNodeInfo> seen = new HashSet();
            Queue<AccessibilityNodeInfo> fringe = new LinkedList();
            fringe.add(root);
            while (!fringe.isEmpty()) {
                AccessibilityNodeInfo current = (AccessibilityNodeInfo) fringe.poll();
                if (seen.add(current)) {
                    int childCount = current.getChildCount();
                    for (i = 0; i < childCount; i++) {
                        long childId = current.getChildId(i);
                        for (j = 0; j < infoCount; j++) {
                            AccessibilityNodeInfo child = (AccessibilityNodeInfo) infos.get(j);
                            if (child.getSourceNodeId() == childId) {
                                fringe.add(child);
                            }
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Duplicate node.");
                    return;
                }
            }
            int disconnectedCount = infos.size() - seen.size();
            if (disconnectedCount > 0) {
                Log.e(LOG_TAG, disconnectedCount + " Disconnected nodes.");
            }
        }
    }
}
