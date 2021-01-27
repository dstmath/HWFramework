package com.android.server.accessibility;

import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import android.view.MagnificationSpec;
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
    @GuardedBy({"mLock"})
    boolean mDone;
    private final int mInteractionId;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    boolean mMultiNodeCallbackHappened;
    @GuardedBy({"mLock"})
    AccessibilityNodeInfo mNodeFromOriginalWindow;
    @GuardedBy({"mLock"})
    List<AccessibilityNodeInfo> mNodesFromOriginalWindow;
    @GuardedBy({"mLock"})
    List<AccessibilityNodeInfo> mNodesWithReplacementActions;
    private final IAccessibilityInteractionConnectionCallback mServiceCallback;
    @GuardedBy({"mLock"})
    boolean mSingleNodeCallbackHappened;

    public ActionReplacingCallback(IAccessibilityInteractionConnectionCallback serviceCallback, IAccessibilityInteractionConnection connectionWithReplacementActions, int interactionId, int interrogatingPid, long interrogatingTid) {
        this.mServiceCallback = serviceCallback;
        this.mConnectionWithReplacementActions = connectionWithReplacementActions;
        this.mInteractionId = interactionId;
        long identityToken = Binder.clearCallingIdentity();
        try {
            this.mConnectionWithReplacementActions.findAccessibilityNodeInfoByAccessibilityId(AccessibilityNodeInfo.ROOT_NODE_ID, (Region) null, interactionId + 1, this, 0, interrogatingPid, interrogatingTid, (MagnificationSpec) null, (Bundle) null);
        } catch (RemoteException e) {
            this.mMultiNodeCallbackHappened = true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
        Binder.restoreCallingIdentity(identityToken);
    }

    public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info, int interactionId) {
        boolean readyForCallback;
        synchronized (this.mLock) {
            if (interactionId == this.mInteractionId) {
                this.mNodeFromOriginalWindow = info;
                this.mSingleNodeCallbackHappened = true;
                readyForCallback = this.mMultiNodeCallbackHappened;
            } else {
                Slog.e(LOG_TAG, "Callback with unexpected interactionId");
                return;
            }
        }
        if (readyForCallback) {
            replaceInfoActionsAndCallService();
        }
    }

    public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) {
        boolean callbackForSingleNode;
        boolean callbackForMultipleNodes;
        synchronized (this.mLock) {
            if (interactionId == this.mInteractionId) {
                this.mNodesFromOriginalWindow = infos;
            } else if (interactionId == this.mInteractionId + 1) {
                this.mNodesWithReplacementActions = infos;
            } else {
                Slog.e(LOG_TAG, "Callback with unexpected interactionId");
                return;
            }
            callbackForSingleNode = this.mSingleNodeCallbackHappened;
            callbackForMultipleNodes = this.mMultiNodeCallbackHappened;
            this.mMultiNodeCallbackHappened = true;
        }
        if (callbackForSingleNode) {
            replaceInfoActionsAndCallService();
        }
        if (callbackForMultipleNodes) {
            replaceInfosActionsAndCallService();
        }
    }

    public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws RemoteException {
        this.mServiceCallback.setPerformAccessibilityActionResult(succeeded, interactionId);
    }

    private void replaceInfoActionsAndCallService() {
        synchronized (this.mLock) {
            if (!this.mDone) {
                if (this.mNodeFromOriginalWindow != null) {
                    replaceActionsOnInfoLocked(this.mNodeFromOriginalWindow);
                }
                recycleReplaceActionNodesLocked();
                AccessibilityNodeInfo nodeToReturn = this.mNodeFromOriginalWindow;
                this.mDone = true;
                try {
                    this.mServiceCallback.setFindAccessibilityNodeInfoResult(nodeToReturn, this.mInteractionId);
                } catch (RemoteException e) {
                }
            }
        }
    }

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
                try {
                    this.mServiceCallback.setFindAccessibilityNodeInfosResult(nodesToReturn, this.mInteractionId);
                } catch (RemoteException e) {
                }
            }
        }
    }

    @GuardedBy({"mLock"})
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

    @GuardedBy({"mLock"})
    private void recycleReplaceActionNodesLocked() {
        List<AccessibilityNodeInfo> list = this.mNodesWithReplacementActions;
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                this.mNodesWithReplacementActions.get(i).recycle();
            }
            this.mNodesWithReplacementActions = null;
        }
    }
}
