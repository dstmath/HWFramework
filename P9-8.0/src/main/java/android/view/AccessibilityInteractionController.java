package android.view;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.text.style.AccessibilityClickableSpan;
import android.text.style.ClickableSpan;
import android.util.LongSparseArray;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.internal.R;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.function.Predicate;

final class AccessibilityInteractionController {
    private static final boolean ENFORCE_NODE_TREE_CONSISTENT = false;
    private AddNodeInfosForViewId mAddNodeInfosForViewId;
    private final Handler mHandler;
    private final long mMyLooperThreadId;
    private final int mMyProcessId;
    private final AccessibilityNodePrefetcher mPrefetcher;
    private final ArrayList<AccessibilityNodeInfo> mTempAccessibilityNodeInfoList = new ArrayList();
    private final ArrayList<View> mTempArrayList = new ArrayList();
    private final Point mTempPoint = new Point();
    private final Rect mTempRect = new Rect();
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();
    private final ViewRootImpl mViewRootImpl;

    private class AccessibilityNodePrefetcher {
        private static final int MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE = 50;
        private final ArrayList<View> mTempViewList;

        /* synthetic */ AccessibilityNodePrefetcher(AccessibilityInteractionController this$0, AccessibilityNodePrefetcher -this1) {
            this();
        }

        private AccessibilityNodePrefetcher() {
            this.mTempViewList = new ArrayList();
        }

        public void prefetchAccessibilityNodeInfos(View view, int virtualViewId, int fetchFlags, List<AccessibilityNodeInfo> outInfos, Bundle arguments) {
            String extraDataRequested;
            AccessibilityNodeProvider provider = view.getAccessibilityNodeProvider();
            if (arguments == null) {
                extraDataRequested = null;
            } else {
                extraDataRequested = arguments.getString(AccessibilityNodeInfo.EXTRA_DATA_REQUESTED_KEY);
            }
            AccessibilityNodeInfo root;
            if (provider == null) {
                root = view.createAccessibilityNodeInfo();
                if (root != null) {
                    if (extraDataRequested != null) {
                        view.addExtraDataToAccessibilityNodeInfo(root, extraDataRequested, arguments);
                    }
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
            root = provider.createAccessibilityNodeInfo(virtualViewId);
            if (root != null) {
                if (extraDataRequested != null) {
                    provider.addExtraDataToAccessibilityNodeInfo(virtualViewId, root, extraDataRequested, arguments);
                }
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
                if (seen.add(current)) {
                    if (current.isAccessibilityFocused()) {
                        if (accessFocus != null) {
                            throw new IllegalStateException("Duplicate accessibility focus:" + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
                        }
                        accessFocus = current;
                    }
                    if (current.isFocused()) {
                        if (inputFocus != null) {
                            throw new IllegalStateException("Duplicate input focus: " + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
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
                    throw new IllegalStateException("Duplicate node: " + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
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
            for (ViewParent parent = view.getParentForAccessibility(); (parent instanceof View) && outInfos.size() < 50; parent = parent.getParentForAccessibility()) {
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
                        if (outInfos.size() < 50) {
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
                        if (outInfos.size() < 50) {
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
                    if (outInfos.size() < 50) {
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
            while (accessibilityViewId != Integer.MAX_VALUE && outInfos.size() < 50) {
                int virtualDescendantId = AccessibilityNodeInfo.getVirtualDescendantId(parentNodeId);
                if (virtualDescendantId != -1 || accessibilityViewId == providerHost.getAccessibilityViewId()) {
                    AccessibilityNodeInfo parent = provider.createAccessibilityNodeInfo(virtualDescendantId);
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
            if (parentVirtualDescendantId != -1 || parentAccessibilityViewId == providerHost.getAccessibilityViewId()) {
                AccessibilityNodeInfo parent = provider.createAccessibilityNodeInfo(parentVirtualDescendantId);
                if (parent != null) {
                    int childCount = parent.getChildCount();
                    for (int i = 0; i < childCount && outInfos.size() < 50; i++) {
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
                if (outInfos.size() < 50) {
                    AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(root.getChildId(i)));
                    if (child != null) {
                        outInfos.add(child);
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (outInfos.size() < 50) {
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

        /* synthetic */ AddNodeInfosForViewId(AccessibilityInteractionController this$0, AddNodeInfosForViewId -this1) {
            this();
        }

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

        public boolean test(View view) {
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
                case 1:
                    return "MSG_PERFORM_ACCESSIBILITY_ACTION";
                case 2:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID";
                case 3:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID";
                case 4:
                    return "MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT";
                case 5:
                    return "MSG_FIND_FOCUS";
                case 6:
                    return "MSG_FOCUS_SEARCH";
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        }

        public void handleMessage(Message message) {
            int type = message.what;
            switch (type) {
                case 1:
                    AccessibilityInteractionController.this.performAccessibilityActionUiThread(message);
                    return;
                case 2:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfoByAccessibilityIdUiThread(message);
                    return;
                case 3:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfosByViewIdUiThread(message);
                    return;
                case 4:
                    AccessibilityInteractionController.this.findAccessibilityNodeInfosByTextUiThread(message);
                    return;
                case 5:
                    AccessibilityInteractionController.this.findFocusUiThread(message);
                    return;
                case 6:
                    AccessibilityInteractionController.this.focusSearchUiThread(message);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        }
    }

    public AccessibilityInteractionController(ViewRootImpl viewRootImpl) {
        Looper looper = viewRootImpl.mHandler.getLooper();
        this.mMyLooperThreadId = looper.getThread().getId();
        this.mMyProcessId = Process.myPid();
        this.mHandler = new PrivateHandler(looper);
        this.mViewRootImpl = viewRootImpl;
        this.mPrefetcher = new AccessibilityNodePrefetcher(this, null);
    }

    private void scheduleMessage(Message message, int interrogatingPid, long interrogatingTid) {
        if (interrogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    private boolean isShown(View view) {
        if (view.mAttachInfo == null || view.mAttachInfo.mWindowVisibility != 0) {
            return false;
        }
        return view.isShown();
    }

    public void findAccessibilityNodeInfoByAccessibilityIdClientThread(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle arguments) {
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
        args.arg4 = arguments;
        message.obj = args;
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
        Bundle arguments = args.arg4;
        args.recycle();
        List<AccessibilityNodeInfo> infos = this.mTempAccessibilityNodeInfoList;
        infos.clear();
        try {
            if (this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null) {
                updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                return;
            }
            View root;
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
            if (accessibilityViewId == 2147483646) {
                root = this.mViewRootImpl.mView;
            } else {
                root = findViewByAccessibilityId(accessibilityViewId);
            }
            if (root != null && isShown(root)) {
                this.mPrefetcher.prefetchAccessibilityNodeInfos(root, virtualDescendantId, flags, infos, arguments);
            }
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th) {
            Throwable th2 = th;
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
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
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
        List<AccessibilityNodeInfo> infos = this.mTempAccessibilityNodeInfoList;
        infos.clear();
        try {
            if (this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null) {
                updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                return;
            }
            View root;
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
            if (accessibilityViewId != 2147483646) {
                root = findViewByAccessibilityId(accessibilityViewId);
            } else {
                root = this.mViewRootImpl.mView;
            }
            if (root != null) {
                int resolvedViewId = root.getContext().getResources().getIdentifier(viewId, null, null);
                if (resolvedViewId <= 0) {
                    updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                    return;
                }
                if (this.mAddNodeInfosForViewId == null) {
                    this.mAddNodeInfosForViewId = new AddNodeInfosForViewId(this, null);
                }
                this.mAddNodeInfosForViewId.init(resolvedViewId, infos);
                root.findViewByPredicate(this.mAddNodeInfosForViewId);
                this.mAddNodeInfosForViewId.reset();
            }
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th) {
            Throwable th2 = th;
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
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
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
            if (this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null) {
                updateInfosForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
                return;
            }
            View root;
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
            if (accessibilityViewId != 2147483646) {
                root = findViewByAccessibilityId(accessibilityViewId);
            } else {
                root = this.mViewRootImpl.mView;
            }
            if (root != null && isShown(root)) {
                AccessibilityNodeProvider provider = root.getAccessibilityNodeProvider();
                if (provider != null) {
                    infos = provider.findAccessibilityNodeInfosByText(text, virtualDescendantId);
                } else if (virtualDescendantId == -1) {
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
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th) {
            Throwable th2 = th;
            updateInfosForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
        }
    }

    public void findFocusClientThread(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
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
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
            if (this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null) {
                updateInfoForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
                return;
            }
            View root;
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
            if (accessibilityViewId != 2147483646) {
                root = findViewByAccessibilityId(accessibilityViewId);
            } else {
                root = this.mViewRootImpl.mView;
            }
            if (root != null && isShown(root)) {
                switch (focusType) {
                    case 1:
                        View target = root.findFocus();
                        if (target != null && (isShown(target) ^ 1) == 0) {
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
                    case 2:
                        View host = this.mViewRootImpl.mAccessibilityFocusedHost;
                        if (host != null && (ViewRootImpl.isViewDescendantOf(host, root) ^ 1) == 0) {
                            if (isShown(host)) {
                                if (host.getAccessibilityNodeProvider() == null) {
                                    if (virtualDescendantId == -1) {
                                        focused = host.createAccessibilityNodeInfo();
                                        break;
                                    }
                                } else if (this.mViewRootImpl.mAccessibilityFocusedVirtualView != null) {
                                    focused = AccessibilityNodeInfo.obtain(this.mViewRootImpl.mAccessibilityFocusedVirtualView);
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown focus type: " + focusType);
                }
            }
            updateInfoForViewportAndReturnFindNodeResult(focused, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th) {
            Throwable th2 = th;
            updateInfoForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
        }
    }

    public void focusSearchClientThread(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
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
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
            if (this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null) {
                updateInfoForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
                return;
            }
            View root;
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
            if (accessibilityViewId != 2147483646) {
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
            updateInfoForViewportAndReturnFindNodeResult(next, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th) {
            Throwable th2 = th;
            updateInfoForViewportAndReturnFindNodeResult(null, callback, interactionId, spec, interactiveRegion);
        }
    }

    public void performAccessibilityActionClientThread(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
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
        scheduleMessage(message, interrogatingPid, interrogatingTid);
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
                    if (accessibilityViewId != 2147483646) {
                        target = findViewByAccessibilityId(accessibilityViewId);
                    } else {
                        target = this.mViewRootImpl.mView;
                    }
                    if (target != null && isShown(target)) {
                        if (action == R.id.accessibilityActionClickOnClickableSpan) {
                            succeeded = handleClickableSpanActionUiThread(target, virtualDescendantId, arguments);
                        } else {
                            AccessibilityNodeProvider provider = target.getAccessibilityNodeProvider();
                            if (provider != null) {
                                succeeded = provider.performAction(virtualDescendantId, action, arguments);
                            } else if (virtualDescendantId == -1) {
                                succeeded = target.performAccessibilityAction(action, arguments);
                            }
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
        if (foundView == null || (isShown(foundView) ^ 1) == 0) {
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

    private void applyAppScaleAndMagnificationSpecIfNeeded(AccessibilityNodeInfo info, MagnificationSpec spec) {
        if (info != null) {
            float applicationScale = this.mViewRootImpl.mAttachInfo.mApplicationScale;
            if (shouldApplyAppScaleAndMagnificationSpec(applicationScale, spec)) {
                Rect boundsInParent = this.mTempRect;
                Rect boundsInScreen = this.mTempRect1;
                info.getBoundsInParent(boundsInParent);
                info.getBoundsInScreen(boundsInScreen);
                if (applicationScale != 1.0f) {
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
                if (info.hasExtras()) {
                    Parcelable[] textLocations = info.getExtras().getParcelableArray(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY);
                    if (textLocations != null) {
                        for (RectF textLocation : textLocations) {
                            textLocation.scale(applicationScale);
                            if (spec != null) {
                                textLocation.scale(spec.scale);
                                textLocation.offset(spec.offsetX, spec.offsetY);
                            }
                        }
                    }
                }
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
        if (appScale == 1.0f) {
            return spec != null ? spec.isNop() ^ 1 : false;
        } else {
            return true;
        }
    }

    private void updateInfosForViewportAndReturnFindNodeResult(List<AccessibilityNodeInfo> infos, IAccessibilityInteractionConnectionCallback callback, int interactionId, MagnificationSpec spec, Region interactiveRegion) {
        try {
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            applyAppScaleAndMagnificationSpecIfNeeded((List) infos, spec);
            adjustIsVisibleToUserIfNeeded((List) infos, interactiveRegion);
            callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
            if (infos != null) {
                infos.clear();
            }
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
        } catch (RemoteException e) {
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
        } catch (Throwable th) {
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
            throw th;
        }
    }

    private void updateInfoForViewportAndReturnFindNodeResult(AccessibilityNodeInfo info, IAccessibilityInteractionConnectionCallback callback, int interactionId, MagnificationSpec spec, Region interactiveRegion) {
        try {
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            applyAppScaleAndMagnificationSpecIfNeeded(info, spec);
            adjustIsVisibleToUserIfNeeded(info, interactiveRegion);
            callback.setFindAccessibilityNodeInfoResult(info, interactionId);
        } catch (RemoteException e) {
        } finally {
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
        }
    }

    private void recycleMagnificationSpecAndRegionIfNeeded(MagnificationSpec spec, Region region) {
        if (Process.myPid() != Binder.getCallingPid()) {
            if (spec != null) {
                spec.recycle();
            }
        } else if (region != null) {
            region.recycle();
        }
    }

    private boolean handleClickableSpanActionUiThread(View view, int virtualDescendantId, Bundle arguments) {
        Parcelable span = arguments.getParcelable(AccessibilityNodeInfo.ACTION_ARGUMENT_ACCESSIBLE_CLICKABLE_SPAN);
        if (!(span instanceof AccessibilityClickableSpan)) {
            return false;
        }
        AccessibilityNodeInfo infoWithSpan = null;
        AccessibilityNodeProvider provider = view.getAccessibilityNodeProvider();
        if (provider != null) {
            infoWithSpan = provider.createAccessibilityNodeInfo(virtualDescendantId);
        } else if (virtualDescendantId == -1) {
            infoWithSpan = view.createAccessibilityNodeInfo();
        }
        if (infoWithSpan == null) {
            return false;
        }
        ClickableSpan clickableSpan = ((AccessibilityClickableSpan) span).findClickableSpan(infoWithSpan.getOriginalText());
        if (clickableSpan == null) {
            return false;
        }
        clickableSpan.onClick(view);
        return true;
    }
}
