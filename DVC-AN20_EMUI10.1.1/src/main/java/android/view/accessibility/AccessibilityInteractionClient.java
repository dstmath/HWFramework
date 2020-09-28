package android.view.accessibility;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.annotation.UnsupportedAppUsage;
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
import android.view.accessibility.AccessibilityCache;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public final class AccessibilityInteractionClient extends IAccessibilityInteractionConnectionCallback.Stub {
    private static final boolean CHECK_INTEGRITY = true;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityInteractionClient";
    public static final int NO_ID = -1;
    private static final long TIMEOUT_INTERACTION_MILLIS = 5000;
    private static AccessibilityCache sAccessibilityCache = new AccessibilityCache(new AccessibilityCache.AccessibilityNodeRefresher());
    private static final LongSparseArray<AccessibilityInteractionClient> sClients = new LongSparseArray<>();
    private static final SparseArray<IAccessibilityServiceConnection> sConnectionCache = new SparseArray<>();
    private static final Object sStaticLock = new Object();
    private AccessibilityNodeInfo mFindAccessibilityNodeInfoResult;
    private List<AccessibilityNodeInfo> mFindAccessibilityNodeInfosResult;
    private final Object mInstanceLock = new Object();
    private volatile int mInteractionId = -1;
    private final AtomicInteger mInteractionIdCounter = new AtomicInteger();
    private boolean mPerformAccessibilityActionResult;
    private Message mSameThreadMessage;

    @UnsupportedAppUsage
    public static AccessibilityInteractionClient getInstance() {
        return getInstanceForThread(Thread.currentThread().getId());
    }

    public static AccessibilityInteractionClient getInstanceForThread(long threadId) {
        AccessibilityInteractionClient client;
        synchronized (sStaticLock) {
            client = sClients.get(threadId);
            if (client == null) {
                client = new AccessibilityInteractionClient();
                sClients.put(threadId, client);
            }
        }
        return client;
    }

    public static IAccessibilityServiceConnection getConnection(int connectionId) {
        IAccessibilityServiceConnection iAccessibilityServiceConnection;
        synchronized (sConnectionCache) {
            iAccessibilityServiceConnection = sConnectionCache.get(connectionId);
        }
        return iAccessibilityServiceConnection;
    }

    public static void addConnection(int connectionId, IAccessibilityServiceConnection connection) {
        synchronized (sConnectionCache) {
            sConnectionCache.put(connectionId, connection);
        }
    }

    public static void removeConnection(int connectionId) {
        synchronized (sConnectionCache) {
            sConnectionCache.remove(connectionId);
        }
    }

    @VisibleForTesting
    public static void setCache(AccessibilityCache cache) {
        sAccessibilityCache = cache;
    }

    private AccessibilityInteractionClient() {
    }

    @UnsupportedAppUsage
    public void setSameThreadMessage(Message message) {
        synchronized (this.mInstanceLock) {
            this.mSameThreadMessage = message;
            this.mInstanceLock.notifyAll();
        }
    }

    public AccessibilityNodeInfo getRootInActiveWindow(int connectionId) {
        return findAccessibilityNodeInfoByAccessibilityId(connectionId, Integer.MAX_VALUE, AccessibilityNodeInfo.ROOT_NODE_ID, false, 4, null);
    }

    public AccessibilityWindowInfo getWindow(int connectionId, int accessibilityWindowId) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection == null) {
                return null;
            }
            AccessibilityWindowInfo window = sAccessibilityCache.getWindow(accessibilityWindowId);
            if (window != null) {
                return window;
            }
            long identityToken = Binder.clearCallingIdentity();
            try {
                AccessibilityWindowInfo window2 = connection.getWindow(accessibilityWindowId);
                if (window2 == null) {
                    return null;
                }
                sAccessibilityCache.addWindow(window2);
                return window2;
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindow", re);
            return null;
        }
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
                try {
                    List<AccessibilityWindowInfo> windows2 = connection.getWindows();
                    if (windows2 != null) {
                        sAccessibilityCache.setWindows(windows2);
                        return windows2;
                    }
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindows", re);
        }
        return Collections.emptyList();
    }

    public AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId(int connectionId, int accessibilityWindowId, long accessibilityNodeId, boolean bypassCache, int prefetchFlags, Bundle arguments) {
        if ((prefetchFlags & 2) == 0 || (prefetchFlags & 1) != 0) {
            try {
                IAccessibilityServiceConnection connection = getConnection(connectionId);
                if (connection == null) {
                    return null;
                }
                if (!bypassCache) {
                    try {
                    } catch (RemoteException e) {
                        re = e;
                        Log.e(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByAccessibilityId", re);
                        return null;
                    }
                    try {
                        AccessibilityNodeInfo cachedInfo = sAccessibilityCache.getNode(accessibilityWindowId, accessibilityNodeId);
                        if (cachedInfo != null) {
                            return cachedInfo;
                        }
                    } catch (RemoteException e2) {
                        re = e2;
                        Log.e(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByAccessibilityId", re);
                        return null;
                    }
                }
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                try {
                    String[] packageNames = connection.findAccessibilityNodeInfoByAccessibilityId(accessibilityWindowId, accessibilityNodeId, interactionId, this, prefetchFlags, Thread.currentThread().getId(), arguments);
                    Binder.restoreCallingIdentity(identityToken);
                    if (packageNames == null) {
                        return null;
                    }
                    List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                    finalizeAndCacheAccessibilityNodeInfos(infos, connectionId, bypassCache, packageNames);
                    if (infos == null || infos.isEmpty()) {
                        return null;
                    }
                    for (int i = 1; i < infos.size(); i++) {
                        infos.get(i).recycle();
                    }
                    return infos.get(0);
                } catch (RemoteException e3) {
                    re = e3;
                    Log.e(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByAccessibilityId", re);
                    return null;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (RemoteException e4) {
                re = e4;
                Log.e(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByAccessibilityId", re);
                return null;
            }
        } else {
            throw new IllegalArgumentException("FLAG_PREFETCH_SIBLINGS requires FLAG_PREFETCH_PREDECESSORS");
        }
    }

    private static String idToString(int accessibilityWindowId, long accessibilityNodeId) {
        return accessibilityWindowId + "/" + AccessibilityNodeInfo.idToString(accessibilityNodeId);
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(int connectionId, int accessibilityWindowId, long accessibilityNodeId, String viewId) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                try {
                    String[] packageNames = connection.findAccessibilityNodeInfosByViewId(accessibilityWindowId, accessibilityNodeId, viewId, interactionId, this, Thread.currentThread().getId());
                    Binder.restoreCallingIdentity(identityToken);
                    if (packageNames != null) {
                        List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                        if (infos != null) {
                            finalizeAndCacheAccessibilityNodeInfos(infos, connectionId, false, packageNames);
                            return infos;
                        }
                    }
                } catch (RemoteException e) {
                    re = e;
                    Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByViewIdInActiveWindow", re);
                    return Collections.emptyList();
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
        } catch (RemoteException e2) {
            re = e2;
            Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfoByViewIdInActiveWindow", re);
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(int connectionId, int accessibilityWindowId, long accessibilityNodeId, String text) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                int interactionId = this.mInteractionIdCounter.getAndIncrement();
                long identityToken = Binder.clearCallingIdentity();
                try {
                    String[] packageNames = connection.findAccessibilityNodeInfosByText(accessibilityWindowId, accessibilityNodeId, text, interactionId, this, Thread.currentThread().getId());
                    Binder.restoreCallingIdentity(identityToken);
                    if (packageNames != null) {
                        List<AccessibilityNodeInfo> infos = getFindAccessibilityNodeInfosResultAndClear(interactionId);
                        if (infos != null) {
                            finalizeAndCacheAccessibilityNodeInfos(infos, connectionId, false, packageNames);
                            return infos;
                        }
                    }
                } catch (RemoteException e) {
                    re = e;
                    Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfosByViewText", re);
                    return Collections.emptyList();
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
        } catch (RemoteException e2) {
            re = e2;
            Log.w(LOG_TAG, "Error while calling remote findAccessibilityNodeInfosByViewText", re);
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public AccessibilityNodeInfo findFocus(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int focusType) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection == null) {
                return null;
            }
            int interactionId = this.mInteractionIdCounter.getAndIncrement();
            long identityToken = Binder.clearCallingIdentity();
            try {
                String[] packageNames = connection.findFocus(accessibilityWindowId, accessibilityNodeId, focusType, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (packageNames == null) {
                    return null;
                }
                AccessibilityNodeInfo info = getFindAccessibilityNodeInfoResultAndClear(interactionId);
                finalizeAndCacheAccessibilityNodeInfo(info, connectionId, false, packageNames);
                return info;
            } catch (RemoteException e) {
                re = e;
                Log.w(LOG_TAG, "Error while calling remote findFocus", re);
                return null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (RemoteException e2) {
            re = e2;
            Log.w(LOG_TAG, "Error while calling remote findFocus", re);
            return null;
        }
    }

    public AccessibilityNodeInfo focusSearch(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int direction) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection == null) {
                return null;
            }
            int interactionId = this.mInteractionIdCounter.getAndIncrement();
            long identityToken = Binder.clearCallingIdentity();
            try {
                String[] packageNames = connection.focusSearch(accessibilityWindowId, accessibilityNodeId, direction, interactionId, this, Thread.currentThread().getId());
                Binder.restoreCallingIdentity(identityToken);
                if (packageNames == null) {
                    return null;
                }
                AccessibilityNodeInfo info = getFindAccessibilityNodeInfoResultAndClear(interactionId);
                finalizeAndCacheAccessibilityNodeInfo(info, connectionId, false, packageNames);
                return info;
            } catch (RemoteException e) {
                re = e;
                Log.w(LOG_TAG, "Error while calling remote accessibilityFocusSearch", re);
                return null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (RemoteException e2) {
            re = e2;
            Log.w(LOG_TAG, "Error while calling remote accessibilityFocusSearch", re);
            return null;
        }
    }

    public boolean performAccessibilityAction(int connectionId, int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments) {
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection == null) {
                return false;
            }
            int interactionId = this.mInteractionIdCounter.getAndIncrement();
            long identityToken = Binder.clearCallingIdentity();
            try {
                if (connection.performAccessibilityAction(accessibilityWindowId, accessibilityNodeId, action, arguments, interactionId, this, Thread.currentThread().getId())) {
                    return getPerformAccessibilityActionResultAndClear(interactionId);
                }
                return false;
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote performAccessibilityAction", re);
            return false;
        }
    }

    @UnsupportedAppUsage
    public void clearCache() {
        sAccessibilityCache.clear();
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        sAccessibilityCache.onAccessibilityEvent(event);
    }

    private AccessibilityNodeInfo getFindAccessibilityNodeInfoResultAndClear(int interactionId) {
        AccessibilityNodeInfo result;
        synchronized (this.mInstanceLock) {
            result = waitForResultTimedLocked(interactionId) ? this.mFindAccessibilityNodeInfoResult : null;
            clearResultLocked();
        }
        return result;
    }

    @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
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

    @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
    public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) {
        synchronized (this.mInstanceLock) {
            if (interactionId > this.mInteractionId) {
                if (infos != null) {
                    if (!(Binder.getCallingPid() != Process.myPid())) {
                        this.mFindAccessibilityNodeInfosResult = new ArrayList(infos);
                    } else {
                        this.mFindAccessibilityNodeInfosResult = infos;
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
        boolean result;
        synchronized (this.mInstanceLock) {
            result = waitForResultTimedLocked(interactionId) ? this.mPerformAccessibilityActionResult : false;
            clearResultLocked();
        }
        return result;
    }

    @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
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
        this.mInteractionId = -1;
        this.mFindAccessibilityNodeInfoResult = null;
        this.mFindAccessibilityNodeInfosResult = null;
        this.mPerformAccessibilityActionResult = false;
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
                    return true;
                }
                if (this.mInteractionId > interactionId) {
                    return false;
                }
                long waitTimeMillis = 5000 - (SystemClock.uptimeMillis() - startTimeMillis);
                if (waitTimeMillis <= 0) {
                    return false;
                }
                Log.w(LOG_TAG, "old interaction Id is: " + this.mInteractionId + ",current interaction Id is:" + interactionId);
                this.mInstanceLock.wait(waitTimeMillis);
            } catch (InterruptedException e) {
            }
        }
    }

    private void finalizeAndCacheAccessibilityNodeInfo(AccessibilityNodeInfo info, int connectionId, boolean bypassCache, String[] packageNames) {
        CharSequence packageName;
        if (info != null) {
            info.setConnectionId(connectionId);
            if (!ArrayUtils.isEmpty(packageNames) && ((packageName = info.getPackageName()) == null || !ArrayUtils.contains(packageNames, packageName.toString()))) {
                info.setPackageName(packageNames[0]);
            }
            info.setSealed(true);
            if (!bypassCache) {
                sAccessibilityCache.add(info);
            }
        }
    }

    private void finalizeAndCacheAccessibilityNodeInfos(List<AccessibilityNodeInfo> infos, int connectionId, boolean bypassCache, String[] packageNames) {
        if (infos != null) {
            int infosCount = infos.size();
            for (int i = 0; i < infosCount; i++) {
                finalizeAndCacheAccessibilityNodeInfo(infos.get(i), connectionId, bypassCache, packageNames);
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

    private void checkFindAccessibilityNodeInfoResultIntegrity(List<AccessibilityNodeInfo> infos) {
        if (infos.size() != 0) {
            AccessibilityNodeInfo root = infos.get(0);
            int infoCount = infos.size();
            for (int i = 1; i < infoCount; i++) {
                int j = i;
                while (true) {
                    if (j >= infoCount) {
                        break;
                    }
                    AccessibilityNodeInfo candidate = infos.get(j);
                    if (root.getParentNodeId() == candidate.getSourceNodeId()) {
                        root = candidate;
                        break;
                    }
                    j++;
                }
            }
            if (root == null) {
                Log.e(LOG_TAG, "No root.");
            }
            HashSet<AccessibilityNodeInfo> seen = new HashSet<>();
            Queue<AccessibilityNodeInfo> fringe = new LinkedList<>();
            fringe.add(root);
            while (!fringe.isEmpty()) {
                AccessibilityNodeInfo current = fringe.poll();
                if (!seen.add(current)) {
                    Log.e(LOG_TAG, "Duplicate node.");
                    return;
                }
                int childCount = current.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    long childId = current.getChildId(i2);
                    for (int j2 = 0; j2 < infoCount; j2++) {
                        AccessibilityNodeInfo child = infos.get(j2);
                        if (child.getSourceNodeId() == childId) {
                            fringe.add(child);
                        }
                    }
                }
            }
            int disconnectedCount = infos.size() - seen.size();
            if (disconnectedCount > 0) {
                Log.e(LOG_TAG, disconnectedCount + " Disconnected nodes.");
            }
        }
    }
}
