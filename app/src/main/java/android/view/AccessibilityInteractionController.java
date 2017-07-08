package android.view;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.LongSparseArray;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Predicate;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

final class AccessibilityInteractionController {
    private static final boolean ENFORCE_NODE_TREE_CONSISTENT = false;
    private AddNodeInfosForViewId mAddNodeInfosForViewId;
    private final Handler mHandler;
    private final long mMyLooperThreadId;
    private final int mMyProcessId;
    private final AccessibilityNodePrefetcher mPrefetcher;
    private final ArrayList<AccessibilityNodeInfo> mTempAccessibilityNodeInfoList;
    private final ArrayList<View> mTempArrayList;
    private final Point mTempPoint;
    private final Rect mTempRect;
    private final Rect mTempRect1;
    private final Rect mTempRect2;
    private final ViewRootImpl mViewRootImpl;

    private class AccessibilityNodePrefetcher {
        private static final int MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE = 50;
        private final ArrayList<View> mTempViewList;

        private AccessibilityNodePrefetcher() {
            this.mTempViewList = new ArrayList();
        }

        public void prefetchAccessibilityNodeInfos(View view, int virtualViewId, int fetchFlags, List<AccessibilityNodeInfo> outInfos) {
            AccessibilityNodeProvider provider = view.getAccessibilityNodeProvider();
            AccessibilityNodeInfo root;
            if (provider == null) {
                root = view.createAccessibilityNodeInfo();
                if (root != null) {
                    outInfos.add(root);
                    if ((fetchFlags & 1) != 0) {
                        prefetchPredecessorsOfRealNode(view, outInfos);
                    }
                    if ((fetchFlags & 2) != 0) {
                        prefetchSiblingsOfRealNode(view, outInfos);
                    }
                    if ((fetchFlags & 4) != 0) {
                        prefetchDescendantsOfRealNode(view, outInfos);
                        return;
                    }
                    return;
                }
                return;
            }
            if (virtualViewId != HwBootFail.STAGE_BOOT_SUCCESS) {
                root = provider.createAccessibilityNodeInfo(virtualViewId);
            } else {
                root = provider.createAccessibilityNodeInfo(-1);
            }
            if (root != null) {
                outInfos.add(root);
                if ((fetchFlags & 1) != 0) {
                    prefetchPredecessorsOfVirtualNode(root, view, provider, outInfos);
                }
                if ((fetchFlags & 2) != 0) {
                    prefetchSiblingsOfVirtualNode(root, view, provider, outInfos);
                }
                if ((fetchFlags & 4) != 0) {
                    prefetchDescendantsOfVirtualNode(root, provider, outInfos);
                }
            }
        }

        private void enforceNodeTreeConsistent(List<AccessibilityNodeInfo> nodes) {
            int j;
            LongSparseArray<AccessibilityNodeInfo> nodeMap = new LongSparseArray();
            int nodeCount = nodes.size();
            for (int i = 0; i < nodeCount; i++) {
                AccessibilityNodeInfo node = (AccessibilityNodeInfo) nodes.get(i);
                nodeMap.put(node.getSourceNodeId(), node);
            }
            AccessibilityNodeInfo root = (AccessibilityNodeInfo) nodeMap.valueAt(0);
            for (AccessibilityNodeInfo parent = root; parent != null; parent = (AccessibilityNodeInfo) nodeMap.get(parent.getParentNodeId())) {
                root = parent;
            }
            AccessibilityNodeInfo accessFocus = null;
            AccessibilityNodeInfo inputFocus = null;
            HashSet<AccessibilityNodeInfo> seen = new HashSet();
            Queue<AccessibilityNodeInfo> fringe = new LinkedList();
            fringe.add(root);
            while (!fringe.isEmpty()) {
                AccessibilityNodeInfo current = (AccessibilityNodeInfo) fringe.poll();
                StringBuilder append;
                if (seen.add(current)) {
                    if (current.isAccessibilityFocused()) {
                        if (accessFocus != null) {
                            append = new StringBuilder().append("Duplicate accessibility focus:");
                            throw new IllegalStateException(r20.append(current).append(" in window:").append(AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId).toString());
                        }
                        accessFocus = current;
                    }
                    if (current.isFocused()) {
                        if (inputFocus != null) {
                            append = new StringBuilder().append("Duplicate input focus: ");
                            throw new IllegalStateException(r20.append(current).append(" in window:").append(AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId).toString());
                        }
                        inputFocus = current;
                    }
                    int childCount = current.getChildCount();
                    for (j = 0; j < childCount; j++) {
                        AccessibilityNodeInfo child = (AccessibilityNodeInfo) nodeMap.get(current.getChildId(j));
                        if (child != null) {
                            fringe.add(child);
                        }
                    }
                } else {
                    append = new StringBuilder().append("Duplicate node: ");
                    throw new IllegalStateException(r20.append(current).append(" in window:").append(AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId).toString());
                }
            }
            j = nodeMap.size() - 1;
            while (j >= 0) {
                AccessibilityNodeInfo info = (AccessibilityNodeInfo) nodeMap.valueAt(j);
                if (seen.contains(info)) {
                    j--;
                } else {
                    throw new IllegalStateException("Disconnected node: " + info);
                }
            }
        }

        private void prefetchPredecessorsOfRealNode(View view, List<AccessibilityNodeInfo> outInfos) {
            for (ViewParent parent = view.getParentForAccessibility(); (parent instanceof View) && outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE; parent = parent.getParentForAccessibility()) {
                AccessibilityNodeInfo info = ((View) parent).createAccessibilityNodeInfo();
                if (info != null) {
                    outInfos.add(info);
                }
            }
        }

        private void prefetchSiblingsOfRealNode(View current, List<AccessibilityNodeInfo> outInfos) {
            ViewParent parent = current.getParentForAccessibility();
            if (parent instanceof ViewGroup) {
                ViewGroup parentGroup = (ViewGroup) parent;
                ArrayList<View> children = this.mTempViewList;
                children.clear();
                try {
                    parentGroup.addChildrenForAccessibility(children);
                    int childCount = children.size();
                    int i = 0;
                    while (i < childCount) {
                        if (outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                            View child = (View) children.get(i);
                            if (child.getAccessibilityViewId() != current.getAccessibilityViewId() && AccessibilityInteractionController.this.isShown(child)) {
                                AccessibilityNodeInfo info;
                                AccessibilityNodeProvider provider = child.getAccessibilityNodeProvider();
                                if (provider == null) {
                                    info = child.createAccessibilityNodeInfo();
                                } else {
                                    info = provider.createAccessibilityNodeInfo(-1);
                                }
                                if (info != null) {
                                    outInfos.add(info);
                                }
                            }
                            i++;
                        } else {
                            return;
                        }
                    }
                    children.clear();
                } finally {
                    children.clear();
                }
            }
        }

        private void prefetchDescendantsOfRealNode(View root, List<AccessibilityNodeInfo> outInfos) {
            if (root instanceof ViewGroup) {
                HashMap<View, AccessibilityNodeInfo> addedChildren = new HashMap();
                ArrayList<View> children = this.mTempViewList;
                children.clear();
                try {
                    root.addChildrenForAccessibility(children);
                    int childCount = children.size();
                    int i = 0;
                    while (i < childCount) {
                        if (outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                            View child = (View) children.get(i);
                            if (AccessibilityInteractionController.this.isShown(child)) {
                                AccessibilityNodeProvider provider = child.getAccessibilityNodeProvider();
                                AccessibilityNodeInfo info;
                                if (provider == null) {
                                    info = child.createAccessibilityNodeInfo();
                                    if (info != null) {
                                        outInfos.add(info);
                                        addedChildren.put(child, null);
                                    }
                                } else {
                                    info = provider.createAccessibilityNodeInfo(-1);
                                    if (info != null) {
                                        outInfos.add(info);
                                        addedChildren.put(child, info);
                                    }
                                }
                            }
                            i++;
                        } else {
                            return;
                        }
                    }
                    children.clear();
                    if (outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                        for (Entry<View, AccessibilityNodeInfo> entry : addedChildren.entrySet()) {
                            View addedChild = (View) entry.getKey();
                            AccessibilityNodeInfo virtualRoot = (AccessibilityNodeInfo) entry.getValue();
                            if (virtualRoot == null) {
                                prefetchDescendantsOfRealNode(addedChild, outInfos);
                            } else {
                                prefetchDescendantsOfVirtualNode(virtualRoot, addedChild.getAccessibilityNodeProvider(), outInfos);
                            }
                        }
                    }
                } finally {
                    children.clear();
                }
            }
        }

        private void prefetchPredecessorsOfVirtualNode(AccessibilityNodeInfo root, View providerHost, AccessibilityNodeProvider provider, List<AccessibilityNodeInfo> outInfos) {
            int initialResultSize = outInfos.size();
            long parentNodeId = root.getParentNodeId();
            int accessibilityViewId = AccessibilityNodeInfo.getAccessibilityViewId(parentNodeId);
            while (accessibilityViewId != HwBootFail.STAGE_BOOT_SUCCESS && outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                int virtualDescendantId = AccessibilityNodeInfo.getVirtualDescendantId(parentNodeId);
                if (virtualDescendantId != HwBootFail.STAGE_BOOT_SUCCESS || accessibilityViewId == providerHost.getAccessibilityViewId()) {
                    AccessibilityNodeInfo parent;
                    if (virtualDescendantId != HwBootFail.STAGE_BOOT_SUCCESS) {
                        parent = provider.createAccessibilityNodeInfo(virtualDescendantId);
                    } else {
                        parent = provider.createAccessibilityNodeInfo(-1);
                    }
                    if (parent == null) {
                        for (int i = outInfos.size() - 1; i >= initialResultSize; i--) {
                            outInfos.remove(i);
                        }
                        return;
                    }
                    outInfos.add(parent);
                    parentNodeId = parent.getParentNodeId();
                    accessibilityViewId = AccessibilityNodeInfo.getAccessibilityViewId(parentNodeId);
                } else {
                    prefetchPredecessorsOfRealNode(providerHost, outInfos);
                    return;
                }
            }
        }

        private void prefetchSiblingsOfVirtualNode(AccessibilityNodeInfo current, View providerHost, AccessibilityNodeProvider provider, List<AccessibilityNodeInfo> outInfos) {
            long parentNodeId = current.getParentNodeId();
            int parentAccessibilityViewId = AccessibilityNodeInfo.getAccessibilityViewId(parentNodeId);
            int parentVirtualDescendantId = AccessibilityNodeInfo.getVirtualDescendantId(parentNodeId);
            if (parentVirtualDescendantId != HwBootFail.STAGE_BOOT_SUCCESS || parentAccessibilityViewId == providerHost.getAccessibilityViewId()) {
                AccessibilityNodeInfo parent;
                if (parentVirtualDescendantId != HwBootFail.STAGE_BOOT_SUCCESS) {
                    parent = provider.createAccessibilityNodeInfo(parentVirtualDescendantId);
                } else {
                    parent = provider.createAccessibilityNodeInfo(-1);
                }
                if (parent != null) {
                    int childCount = parent.getChildCount();
                    for (int i = 0; i < childCount && outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE; i++) {
                        long childNodeId = parent.getChildId(i);
                        if (childNodeId != current.getSourceNodeId()) {
                            AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(childNodeId));
                            if (child != null) {
                                outInfos.add(child);
                            }
                        }
                    }
                    return;
                }
            }
            prefetchSiblingsOfRealNode(providerHost, outInfos);
        }

        private void prefetchDescendantsOfVirtualNode(AccessibilityNodeInfo root, AccessibilityNodeProvider provider, List<AccessibilityNodeInfo> outInfos) {
            int initialOutInfosSize = outInfos.size();
            int childCount = root.getChildCount();
            int i = 0;
            while (i < childCount) {
                if (outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                    AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(root.getChildId(i)));
                    if (child != null) {
                        outInfos.add(child);
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (outInfos.size() < MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE) {
                int addedChildCount = outInfos.size() - initialOutInfosSize;
                for (i = 0; i < addedChildCount; i++) {
                    prefetchDescendantsOfVirtualNode((AccessibilityNodeInfo) outInfos.get(initialOutInfosSize + i), provider, outInfos);
                }
            }
        }
    }

    private final class AddNodeInfosForViewId implements Predicate<View> {
        private List<AccessibilityNodeInfo> mInfos;
        private int mViewId;

        private AddNodeInfosForViewId() {
            this.mViewId = -1;
        }

        public void init(int viewId, List<AccessibilityNodeInfo> infos) {
            this.mViewId = viewId;
            this.mInfos = infos;
        }

        public void reset() {
            this.mViewId = -1;
            this.mInfos = null;
        }

        public boolean apply(View view) {
            if (view.getId() == this.mViewId && AccessibilityInteractionController.this.isShown(view)) {
                this.mInfos.add(view.createAccessibilityNodeInfo());
            }
            return false;
        }
    }

    private class PrivateHandler extends Handler {
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID = 3;
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID = 2;
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT = 4;
        private static final int MSG_FIND_FOCUS = 5;
        private static final int MSG_FOCUS_SEARCH = 6;
        private static final int MSG_PERFORM_ACCESSIBILITY_ACTION = 1;

        public PrivateHandler(Looper looper) {
            super(looper);
        }

        public String getMessageName(Message message) {
            int type = message.what;
            switch (type) {
                case MSG_PERFORM_ACCESSIBILITY_ACTION /*1*/:
                    return "MSG_PERFORM_ACCESSIBILITY_ACTION";
                case MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID /*2*/:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID";
                case MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID /*3*/:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID";
                case MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT /*4*/:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT";
                case MSG_FIND_FOCUS /*5*/:
                    return "MSG_FIND_FOCUS";
                case MSG_FOCUS_SEARCH /*6*/:
                    return "MSG_FOCUS_SEARCH";
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        }

        public void handleMessage(Message message) {
            int type = message.what;
            switch (type) {
                case MSG_PERFORM_ACCESSIBILITY_ACTION /*1*/:
                    AccessibilityInteractionController.this.performAccessibilityActionUiThread(message);
                case MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID /*2*/:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfoByAccessibilityIdUiThread(message);
                case MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID /*3*/:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfosByViewIdUiThread(message);
                case MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT /*4*/:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfosByTextUiThread(message);
                case MSG_FIND_FOCUS /*5*/:
                    AccessibilityInteractionController.this.findFocusUiThread(message);
                case MSG_FOCUS_SEARCH /*6*/:
                    AccessibilityInteractionController.this.focusSearchUiThread(message);
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        }
    }

    public AccessibilityInteractionController(ViewRootImpl viewRootImpl) {
        this.mTempAccessibilityNodeInfoList = new ArrayList();
        this.mTempArrayList = new ArrayList();
        this.mTempPoint = new Point();
        this.mTempRect = new Rect();
        this.mTempRect1 = new Rect();
        this.mTempRect2 = new Rect();
        Looper looper = viewRootImpl.mHandler.getLooper();
        this.mMyLooperThreadId = looper.getThread().getId();
        this.mMyProcessId = Process.myPid();
        this.mHandler = new PrivateHandler(looper);
        this.mViewRootImpl = viewRootImpl;
        this.mPrefetcher = new AccessibilityNodePrefetcher();
    }

    private boolean isShown(View view) {
        if (view.mAttachInfo == null || view.mAttachInfo.mWindowVisibility != 0) {
            return false;
        }
        return view.isShown();
    }

    public void findAccessibilityNodeInfoByAccessibilityIdClientThread(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
        Message message = this.mHandler.obtainMessage();
        message.what = 2;
        message.arg1 = flags;
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        args.argi2 = AccessibilityNodeInfo.getVirtualDescendantId(accessibilityNodeId);
        args.argi3 = interactionId;
        args.arg1 = callback;
        args.arg2 = spec;
        args.arg3 = interactiveRegion;
        message.obj = args;
        if (interrogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void findAccessibilityNodeInfoByAccessibilityIdUiThread(Message message) {
        int flags = message.arg1;
        SomeArgs args = message.obj;
        int accessibilityViewId = args.argi1;
        int virtualDescendantId = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = args.arg1;
        MagnificationSpec spec = args.arg2;
        Region interactiveRegion = args.arg3;
        args.recycle();
        List infos = this.mTempAccessibilityNodeInfoList;
        infos.clear();
        try {
            if (this.mViewRootImpl.mView != null && this.mViewRootImpl.mAttachInfo != null) {
                View root;
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                if (accessibilityViewId == HwBootFail.STAGE_BOOT_SUCCESS) {
                    root = this.mViewRootImpl.mView;
                } else {
                    root = findViewByAccessibilityId(accessibilityViewId);
                }
                if (root != null && isShown(root)) {
                    this.mPrefetcher.prefetchAccessibilityNodeInfos(root, virtualDescendantId, flags, infos);
                }
                try {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                    applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                    if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                        spec.recycle();
                    }
                    adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                    callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
                    infos.clear();
                } catch (RemoteException e) {
                }
                if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                    interactiveRegion.recycle();
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                    spec.recycle();
                }
                adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
                infos.clear();
            } catch (RemoteException e2) {
            }
            if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                interactiveRegion.recycle();
            }
        }
    }

    public void findAccessibilityNodeInfosByViewIdClientThread(long accessibilityNodeId, String viewId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
        Message message = this.mHandler.obtainMessage();
        message.what = 3;
        message.arg1 = flags;
        message.arg2 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = interactionId;
        args.arg1 = callback;
        args.arg2 = spec;
        args.arg3 = viewId;
        args.arg4 = interactiveRegion;
        message.obj = args;
        if (interrogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void findAccessibilityNodeInfosByViewIdUiThread(Message message) {
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = message.obj;
        int interactionId = args.argi1;
        IAccessibilityInteractionConnectionCallback callback = args.arg1;
        MagnificationSpec spec = args.arg2;
        String viewId = args.arg3;
        Region interactiveRegion = args.arg4;
        args.recycle();
        List infos = this.mTempAccessibilityNodeInfoList;
        infos.clear();
        try {
            if (this.mViewRootImpl.mView != null && this.mViewRootImpl.mAttachInfo != null) {
                View root;
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                if (accessibilityViewId != HwBootFail.STAGE_BOOT_SUCCESS) {
                    root = findViewByAccessibilityId(accessibilityViewId);
                } else {
                    root = this.mViewRootImpl.mView;
                }
                if (root != null) {
                    int resolvedViewId = root.getContext().getResources().getIdentifier(viewId, null, null);
                    if (resolvedViewId <= 0) {
                        try {
                            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                            applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                            if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                                spec.recycle();
                            }
                            adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                            callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
                        } catch (RemoteException e) {
                        }
                        if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                            interactiveRegion.recycle();
                        }
                        return;
                    }
                    if (this.mAddNodeInfosForViewId == null) {
                        AccessibilityInteractionController accessibilityInteractionController = this;
                        this.mAddNodeInfosForViewId = new AddNodeInfosForViewId();
                    }
                    this.mAddNodeInfosForViewId.init(resolvedViewId, infos);
                    root.findViewByPredicate(this.mAddNodeInfosForViewId);
                    this.mAddNodeInfosForViewId.reset();
                }
                try {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                    applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                    if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                        spec.recycle();
                    }
                    adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                    callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
                } catch (RemoteException e2) {
                }
                if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                    interactiveRegion.recycle();
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                    spec.recycle();
                }
                adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
            } catch (RemoteException e3) {
            }
            if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                interactiveRegion.recycle();
            }
        }
    }

    public void findAccessibilityNodeInfosByTextClientThread(long accessibilityNodeId, String text, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
        Message message = this.mHandler.obtainMessage();
        message.what = 4;
        message.arg1 = flags;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = text;
        args.arg2 = callback;
        args.arg3 = spec;
        args.argi1 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        args.argi2 = AccessibilityNodeInfo.getVirtualDescendantId(accessibilityNodeId);
        args.argi3 = interactionId;
        args.arg4 = interactiveRegion;
        message.obj = args;
        if (interrogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void findAccessibilityNodeInfosByTextUiThread(Message message) {
        int flags = message.arg1;
        SomeArgs args = message.obj;
        String text = args.arg1;
        IAccessibilityInteractionConnectionCallback callback = args.arg2;
        MagnificationSpec spec = args.arg3;
        int accessibilityViewId = args.argi1;
        int virtualDescendantId = args.argi2;
        int interactionId = args.argi3;
        Region interactiveRegion = args.arg4;
        args.recycle();
        List infos = null;
        try {
            if (this.mViewRootImpl.mView != null) {
                if (this.mViewRootImpl.mAttachInfo != null) {
                    View root;
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    if (accessibilityViewId != Integer.MAX_VALUE) {
                        root = findViewByAccessibilityId(accessibilityViewId);
                    } else {
                        root = this.mViewRootImpl.mView;
                    }
                    if (root != null && isShown(root)) {
                        AccessibilityNodeProvider provider = root.getAccessibilityNodeProvider();
                        if (provider != null) {
                            infos = virtualDescendantId != Integer.MAX_VALUE ? provider.findAccessibilityNodeInfosByText(text, virtualDescendantId) : provider.findAccessibilityNodeInfosByText(text, -1);
                        } else if (virtualDescendantId == Integer.MAX_VALUE) {
                            ArrayList<View> foundViews = this.mTempArrayList;
                            foundViews.clear();
                            root.findViewsWithText(foundViews, text, 7);
                            if (!foundViews.isEmpty()) {
                                infos = this.mTempAccessibilityNodeInfoList;
                                infos.clear();
                                int viewCount = foundViews.size();
                                for (int i = 0; i < viewCount; i++) {
                                    View foundView = (View) foundViews.get(i);
                                    if (isShown(foundView)) {
                                        provider = foundView.getAccessibilityNodeProvider();
                                        if (provider != null) {
                                            List<AccessibilityNodeInfo> infosFromProvider = provider.findAccessibilityNodeInfosByText(text, -1);
                                            if (infosFromProvider != null) {
                                                infos.addAll(infosFromProvider);
                                            }
                                        } else {
                                            infos.add(foundView.createAccessibilityNodeInfo());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    try {
                        this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                        applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
                        if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                            spec.recycle();
                        }
                        adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
                        callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
                    } catch (RemoteException e) {
                    }
                    if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                        interactiveRegion.recycle();
                    }
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                applyAppScaleAndMagnificationSpecIfNeeded(null, spec);
                if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                    spec.recycle();
                }
                adjustIsVisibleToUserIfNeeded(null, interactiveRegion);
                callback.setFindAccessibilityNodeInfosResult(null, interactionId);
            } catch (RemoteException e2) {
            }
            if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                interactiveRegion.recycle();
            }
        }
    }

    public void findFocusClientThread(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interogatingPid, long interrogatingTid, MagnificationSpec spec) {
        Message message = this.mHandler.obtainMessage();
        message.what = 5;
        message.arg1 = flags;
        message.arg2 = focusType;
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = interactionId;
        args.argi2 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        args.argi3 = AccessibilityNodeInfo.getVirtualDescendantId(accessibilityNodeId);
        args.arg1 = callback;
        args.arg2 = spec;
        args.arg3 = interactiveRegion;
        message.obj = args;
        if (interogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void findFocusUiThread(Message message) {
        int flags = message.arg1;
        int focusType = message.arg2;
        SomeArgs args = message.obj;
        int interactionId = args.argi1;
        int accessibilityViewId = args.argi2;
        int virtualDescendantId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = args.arg1;
        MagnificationSpec spec = args.arg2;
        Region interactiveRegion = args.arg3;
        args.recycle();
        AccessibilityNodeInfo focused = null;
        try {
            if (this.mViewRootImpl.mView != null) {
                if (this.mViewRootImpl.mAttachInfo != null) {
                    View root;
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    if (accessibilityViewId != Integer.MAX_VALUE) {
                        root = findViewByAccessibilityId(accessibilityViewId);
                    } else {
                        root = this.mViewRootImpl.mView;
                    }
                    if (root != null && isShown(root)) {
                        switch (focusType) {
                            case HwCfgFilePolicy.EMUI /*1*/:
                                View target = root.findFocus();
                                if (target != null && isShown(target)) {
                                    AccessibilityNodeProvider provider = target.getAccessibilityNodeProvider();
                                    if (provider != null) {
                                        focused = provider.findFocus(focusType);
                                    }
                                    if (focused == null) {
                                        focused = target.createAccessibilityNodeInfo();
                                        break;
                                    }
                                }
                                break;
                            case HwCfgFilePolicy.PC /*2*/:
                                View host = this.mViewRootImpl.mAccessibilityFocusedHost;
                                if (host != null && ViewRootImpl.isViewDescendantOf(host, root) && isShown(host)) {
                                    if (host.getAccessibilityNodeProvider() == null) {
                                        if (virtualDescendantId == Integer.MAX_VALUE) {
                                            focused = host.createAccessibilityNodeInfo();
                                            break;
                                        }
                                    }
                                    if (this.mViewRootImpl.mAccessibilityFocusedVirtualView != null) {
                                        focused = AccessibilityNodeInfo.obtain(this.mViewRootImpl.mAccessibilityFocusedVirtualView);
                                        break;
                                    }
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown focus type: " + focusType);
                        }
                    }
                    try {
                        this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                        applyAppScaleAndMagnificationSpecIfNeeded(focused, spec);
                        if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                            spec.recycle();
                        }
                        adjustIsVisibleToUserIfNeeded(focused, interactiveRegion);
                        callback.setFindAccessibilityNodeInfoResult(focused, interactionId);
                    } catch (RemoteException e) {
                    }
                    if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                        interactiveRegion.recycle();
                    }
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                applyAppScaleAndMagnificationSpecIfNeeded(null, spec);
                if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                    spec.recycle();
                }
                adjustIsVisibleToUserIfNeeded(null, interactiveRegion);
                callback.setFindAccessibilityNodeInfoResult(null, interactionId);
            } catch (RemoteException e2) {
            }
            if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                interactiveRegion.recycle();
            }
        }
    }

    public void focusSearchClientThread(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interogatingPid, long interrogatingTid, MagnificationSpec spec) {
        Message message = this.mHandler.obtainMessage();
        message.what = 6;
        message.arg1 = flags;
        message.arg2 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        SomeArgs args = SomeArgs.obtain();
        args.argi2 = direction;
        args.argi3 = interactionId;
        args.arg1 = callback;
        args.arg2 = spec;
        args.arg3 = interactiveRegion;
        message.obj = args;
        if (interogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void focusSearchUiThread(Message message) {
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = message.obj;
        int direction = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = args.arg1;
        MagnificationSpec spec = args.arg2;
        Region interactiveRegion = args.arg3;
        args.recycle();
        AccessibilityNodeInfo next = null;
        try {
            if (this.mViewRootImpl.mView != null && this.mViewRootImpl.mAttachInfo != null) {
                View root;
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                if (accessibilityViewId != HwBootFail.STAGE_BOOT_SUCCESS) {
                    root = findViewByAccessibilityId(accessibilityViewId);
                } else {
                    root = this.mViewRootImpl.mView;
                }
                if (root != null && isShown(root)) {
                    View nextView = root.focusSearch(direction);
                    if (nextView != null) {
                        next = nextView.createAccessibilityNodeInfo();
                    }
                }
                try {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                    applyAppScaleAndMagnificationSpecIfNeeded(next, spec);
                    if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                        spec.recycle();
                    }
                    adjustIsVisibleToUserIfNeeded(next, interactiveRegion);
                    callback.setFindAccessibilityNodeInfoResult(next, interactionId);
                } catch (RemoteException e) {
                }
                if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                    interactiveRegion.recycle();
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                applyAppScaleAndMagnificationSpecIfNeeded(null, spec);
                if (!(spec == null || Process.myPid() == Binder.getCallingPid())) {
                    spec.recycle();
                }
                adjustIsVisibleToUserIfNeeded(null, interactiveRegion);
                callback.setFindAccessibilityNodeInfoResult(null, interactionId);
            } catch (RemoteException e2) {
            }
            if (interactiveRegion != null && Process.myPid() == Binder.getCallingPid()) {
                interactiveRegion.recycle();
            }
        }
    }

    public void performAccessibilityActionClientThread(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interogatingPid, long interrogatingTid) {
        Message message = this.mHandler.obtainMessage();
        message.what = 1;
        message.arg1 = flags;
        message.arg2 = AccessibilityNodeInfo.getAccessibilityViewId(accessibilityNodeId);
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = AccessibilityNodeInfo.getVirtualDescendantId(accessibilityNodeId);
        args.argi2 = action;
        args.argi3 = interactionId;
        args.arg1 = callback;
        args.arg2 = arguments;
        message.obj = args;
        if (interogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private void performAccessibilityActionUiThread(Message message) {
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = message.obj;
        int virtualDescendantId = args.argi1;
        int action = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = args.arg1;
        Bundle arguments = args.arg2;
        args.recycle();
        boolean succeeded = false;
        try {
            if (!(this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null)) {
                if (!(this.mViewRootImpl.mStopped || this.mViewRootImpl.mPausedForTransition)) {
                    View target;
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    if (accessibilityViewId != HwBootFail.STAGE_BOOT_SUCCESS) {
                        target = findViewByAccessibilityId(accessibilityViewId);
                    } else {
                        target = this.mViewRootImpl.mView;
                    }
                    if (target != null && isShown(target)) {
                        AccessibilityNodeProvider provider = target.getAccessibilityNodeProvider();
                        if (provider != null) {
                            succeeded = virtualDescendantId != HwBootFail.STAGE_BOOT_SUCCESS ? provider.performAction(virtualDescendantId, action, arguments) : provider.performAction(-1, action, arguments);
                        } else if (virtualDescendantId == HwBootFail.STAGE_BOOT_SUCCESS) {
                            succeeded = target.performAccessibilityAction(action, arguments);
                        }
                    }
                    try {
                        this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                        callback.setPerformAccessibilityActionResult(succeeded, interactionId);
                    } catch (RemoteException e) {
                    }
                }
            }
        } finally {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
                callback.setPerformAccessibilityActionResult(false, interactionId);
            } catch (RemoteException e2) {
            }
        }
    }

    private View findViewByAccessibilityId(int accessibilityId) {
        View root = this.mViewRootImpl.mView;
        if (root == null) {
            return null;
        }
        View foundView = root.findViewByAccessibilityId(accessibilityId);
        if (foundView == null || isShown(foundView)) {
            return foundView;
        }
        return null;
    }

    private void applyAppScaleAndMagnificationSpecIfNeeded(List<AccessibilityNodeInfo> infos, MagnificationSpec spec) {
        if (infos != null && shouldApplyAppScaleAndMagnificationSpec(this.mViewRootImpl.mAttachInfo.mApplicationScale, spec)) {
            int infoCount = infos.size();
            for (int i = 0; i < infoCount; i++) {
                applyAppScaleAndMagnificationSpecIfNeeded((AccessibilityNodeInfo) infos.get(i), spec);
            }
        }
    }

    private void adjustIsVisibleToUserIfNeeded(List<AccessibilityNodeInfo> infos, Region interactiveRegion) {
        if (interactiveRegion != null && infos != null) {
            int infoCount = infos.size();
            for (int i = 0; i < infoCount; i++) {
                adjustIsVisibleToUserIfNeeded((AccessibilityNodeInfo) infos.get(i), interactiveRegion);
            }
        }
    }

    private void adjustIsVisibleToUserIfNeeded(AccessibilityNodeInfo info, Region interactiveRegion) {
        if (interactiveRegion != null && info != null) {
            Rect boundsInScreen = this.mTempRect;
            info.getBoundsInScreen(boundsInScreen);
            if (interactiveRegion.quickReject(boundsInScreen)) {
                info.setVisibleToUser(false);
            }
        }
    }

    private void applyAppScaleAndMagnificationSpecIfNeeded(Point point, MagnificationSpec spec) {
        float applicationScale = this.mViewRootImpl.mAttachInfo.mApplicationScale;
        if (shouldApplyAppScaleAndMagnificationSpec(applicationScale, spec)) {
            if (applicationScale != LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                point.x = (int) (((float) point.x) * applicationScale);
                point.y = (int) (((float) point.y) * applicationScale);
            }
            if (spec != null) {
                point.x = (int) (((float) point.x) * spec.scale);
                point.y = (int) (((float) point.y) * spec.scale);
                point.x += (int) spec.offsetX;
                point.y += (int) spec.offsetY;
            }
        }
    }

    private void applyAppScaleAndMagnificationSpecIfNeeded(AccessibilityNodeInfo info, MagnificationSpec spec) {
        if (info != null) {
            float applicationScale = this.mViewRootImpl.mAttachInfo.mApplicationScale;
            if (shouldApplyAppScaleAndMagnificationSpec(applicationScale, spec)) {
                Rect boundsInParent = this.mTempRect;
                Rect boundsInScreen = this.mTempRect1;
                info.getBoundsInParent(boundsInParent);
                info.getBoundsInScreen(boundsInScreen);
                if (applicationScale != LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                    boundsInParent.scale(applicationScale);
                    boundsInScreen.scale(applicationScale);
                }
                if (spec != null) {
                    boundsInParent.scale(spec.scale);
                    boundsInScreen.scale(spec.scale);
                    boundsInScreen.offset((int) spec.offsetX, (int) spec.offsetY);
                }
                info.setBoundsInParent(boundsInParent);
                info.setBoundsInScreen(boundsInScreen);
                if (spec != null) {
                    AttachInfo attachInfo = this.mViewRootImpl.mAttachInfo;
                    if (attachInfo.mDisplay != null) {
                        float scale = attachInfo.mApplicationScale * spec.scale;
                        Rect visibleWinFrame = this.mTempRect1;
                        visibleWinFrame.left = (int) ((((float) attachInfo.mWindowLeft) * scale) + spec.offsetX);
                        visibleWinFrame.top = (int) ((((float) attachInfo.mWindowTop) * scale) + spec.offsetY);
                        visibleWinFrame.right = (int) (((float) visibleWinFrame.left) + (((float) this.mViewRootImpl.mWidth) * scale));
                        visibleWinFrame.bottom = (int) (((float) visibleWinFrame.top) + (((float) this.mViewRootImpl.mHeight) * scale));
                        attachInfo.mDisplay.getRealSize(this.mTempPoint);
                        int displayWidth = this.mTempPoint.x;
                        int displayHeight = this.mTempPoint.y;
                        Rect visibleDisplayFrame = this.mTempRect2;
                        visibleDisplayFrame.set(0, 0, displayWidth, displayHeight);
                        if (!visibleWinFrame.intersect(visibleDisplayFrame)) {
                            visibleDisplayFrame.setEmpty();
                        }
                        if (!visibleWinFrame.intersects(boundsInScreen.left, boundsInScreen.top, boundsInScreen.right, boundsInScreen.bottom)) {
                            info.setVisibleToUser(false);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldApplyAppScaleAndMagnificationSpec(float appScale, MagnificationSpec spec) {
        if (appScale == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return (spec == null || spec.isNop()) ? false : true;
        } else {
            return true;
        }
    }
}
