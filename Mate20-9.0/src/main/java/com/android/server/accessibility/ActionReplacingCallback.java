package com.android.server.accessibility;

import android.os.Binder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;

public class ActionReplacingCallback extends IAccessibilityInteractionConnectionCallback.Stub {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "ActionReplacingCallback";
    private final IAccessibilityInteractionConnection mConnectionWithReplacementActions;
    @GuardedBy("mLock")
    boolean mDone;
    private final int mInteractionId;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    boolean mMultiNodeCallbackHappened;
    @GuardedBy("mLock")
    AccessibilityNodeInfo mNodeFromOriginalWindow;
    @GuardedBy("mLock")
    List<AccessibilityNodeInfo> mNodesFromOriginalWindow;
    @GuardedBy("mLock")
    List<AccessibilityNodeInfo> mNodesWithReplacementActions;
    private final IAccessibilityInteractionConnectionCallback mServiceCallback;
    @GuardedBy("mLock")
    boolean mSingleNodeCallbackHappened;

    public ActionReplacingCallback(IAccessibilityInteractionConnectionCallback serviceCallback, IAccessibilityInteractionConnection connectionWithReplacementActions, int interactionId, int interrogatingPid, long interrogatingTid) {
        long identityToken;
        int i = interactionId;
        this.mServiceCallback = serviceCallback;
        this.mConnectionWithReplacementActions = connectionWithReplacementActions;
        this.mInteractionId = i;
        long identityToken2 = Binder.clearCallingIdentity();
        try {
            long identityToken3 = identityToken2;
            try {
                this.mConnectionWithReplacementActions.findAccessibilityNodeInfoByAccessibilityId(AccessibilityNodeInfo.ROOT_NODE_ID, null, i + 1, this, 0, interrogatingPid, interrogatingTid, null, null);
                Binder.restoreCallingIdentity(identityToken3);
            } catch (RemoteException e) {
                identityToken = identityToken3;
                try {
                    this.mMultiNodeCallbackHappened = true;
                    Binder.restoreCallingIdentity(identityToken);
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken3;
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (RemoteException e2) {
            identityToken = identityToken2;
            this.mMultiNodeCallbackHappened = true;
            Binder.restoreCallingIdentity(identityToken);
        } catch (Throwable th3) {
            th = th3;
            identityToken = identityToken2;
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000f, code lost:
        if (r1 == false) goto L_0x0014;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0011, code lost:
        replaceInfoActionsAndCallService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        return;
     */
    public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info, int interactionId) {
        synchronized (this.mLock) {
            if (interactionId == this.mInteractionId) {
                this.mNodeFromOriginalWindow = info;
                this.mSingleNodeCallbackHappened = true;
                boolean readyForCallback = this.mMultiNodeCallbackHappened;
            } else {
                Slog.e(LOG_TAG, "Callback with unexpected interactionId");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        if (r1 == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        replaceInfoActionsAndCallService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        if (r3 == false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        replaceInfosActionsAndCallService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0023, code lost:
        return;
     */
    public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) {
        synchronized (this.mLock) {
            if (interactionId == this.mInteractionId) {
                this.mNodesFromOriginalWindow = infos;
            } else if (interactionId == this.mInteractionId + 1) {
                this.mNodesWithReplacementActions = infos;
            } else {
                Slog.e(LOG_TAG, "Callback with unexpected interactionId");
                return;
            }
            boolean callbackForSingleNode = this.mSingleNodeCallbackHappened;
            boolean callbackForMultipleNodes = this.mMultiNodeCallbackHappened;
            this.mMultiNodeCallbackHappened = true;
        }
    }

    public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws RemoteException {
        this.mServiceCallback.setPerformAccessibilityActionResult(succeeded, interactionId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r3.mServiceCallback.setFindAccessibilityNodeInfoResult(r1, r3.mInteractionId);
     */
    private void replaceInfoActionsAndCallService() {
        synchronized (this.mLock) {
            if (!this.mDone) {
                if (this.mNodeFromOriginalWindow != null) {
                    replaceActionsOnInfoLocked(this.mNodeFromOriginalWindow);
                }
                recycleReplaceActionNodesLocked();
                AccessibilityNodeInfo nodeToReturn = this.mNodeFromOriginalWindow;
                this.mDone = true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r3.mServiceCallback.setFindAccessibilityNodeInfosResult(r1, r3.mInteractionId);
     */
    private void replaceInfosActionsAndCallService() {
        synchronized (this.mLock) {
            if (!this.mDone) {
                if (this.mNodesFromOriginalWindow != null) {
                    for (int i = 0; i < this.mNodesFromOriginalWindow.size(); i++) {
                        replaceActionsOnInfoLocked(this.mNodesFromOriginalWindow.get(i));
                    }
                }
                recycleReplaceActionNodesLocked();
                List<AccessibilityNodeInfo> nodesToReturn = this.mNodesFromOriginalWindow == null ? null : new ArrayList<>(this.mNodesFromOriginalWindow);
                this.mDone = true;
            }
        }
    }

    @GuardedBy("mLock")
    private void replaceActionsOnInfoLocked(AccessibilityNodeInfo info) {
        info.removeAllActions();
        info.setClickable(false);
        info.setFocusable(false);
        info.setContextClickable(false);
        info.setScrollable(false);
        info.setLongClickable(false);
        info.setDismissable(false);
        if (info.getSourceNodeId() == AccessibilityNodeInfo.ROOT_NODE_ID && this.mNodesWithReplacementActions != null) {
            for (int i = 0; i < this.mNodesWithReplacementActions.size(); i++) {
                AccessibilityNodeInfo nodeWithReplacementActions = this.mNodesWithReplacementActions.get(i);
                if (nodeWithReplacementActions.getSourceNodeId() == AccessibilityNodeInfo.ROOT_NODE_ID) {
                    List<AccessibilityNodeInfo.AccessibilityAction> actions = nodeWithReplacementActions.getActionList();
                    if (actions != null) {
                        for (int j = 0; j < actions.size(); j++) {
                            info.addAction(actions.get(j));
                        }
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                    }
                    info.setClickable(nodeWithReplacementActions.isClickable());
                    info.setFocusable(nodeWithReplacementActions.isFocusable());
                    info.setContextClickable(nodeWithReplacementActions.isContextClickable());
                    info.setScrollable(nodeWithReplacementActions.isScrollable());
                    info.setLongClickable(nodeWithReplacementActions.isLongClickable());
                    info.setDismissable(nodeWithReplacementActions.isDismissable());
                }
            }
        }
    }

    @GuardedBy("mLock")
    private void recycleReplaceActionNodesLocked() {
        if (this.mNodesWithReplacementActions != null) {
            for (int i = this.mNodesWithReplacementActions.size() - 1; i >= 0; i--) {
                this.mNodesWithReplacementActions.get(i).recycle();
            }
            this.mNodesWithReplacementActions = null;
        }
    }
}
