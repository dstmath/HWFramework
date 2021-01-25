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
import android.os.SystemClock;
import android.text.style.AccessibilityClickableSpan;
import android.text.style.ClickableSpan;
import android.util.LongSparseArray;
import android.util.Slog;
import android.view.View;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeIdManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.AccessibilityRequestPreparer;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class AccessibilityInteractionController {
    private static final boolean CONSIDER_REQUEST_PREPARERS = false;
    private static final boolean ENFORCE_NODE_TREE_CONSISTENT = false;
    private static final boolean IGNORE_REQUEST_PREPARERS = true;
    private static final String LOG_TAG = "AccessibilityInteractionController";
    private static final long REQUEST_PREPARER_TIMEOUT_MS = 500;
    private final AccessibilityManager mA11yManager;
    @GuardedBy({"mLock"})
    private int mActiveRequestPreparerId;
    private AddNodeInfosForViewId mAddNodeInfosForViewId;
    private final PrivateHandler mHandler;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private List<MessageHolder> mMessagesWaitingForRequestPreparer;
    private final long mMyLooperThreadId;
    private final int mMyProcessId;
    @GuardedBy({"mLock"})
    private int mNumActiveRequestPreparers;
    private final AccessibilityNodePrefetcher mPrefetcher;
    private final ArrayList<AccessibilityNodeInfo> mTempAccessibilityNodeInfoList = new ArrayList<>();
    private final ArrayList<View> mTempArrayList = new ArrayList<>();
    private final Point mTempPoint = new Point();
    private final Rect mTempRect = new Rect();
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();
    private final ViewRootImpl mViewRootImpl;

    public AccessibilityInteractionController(ViewRootImpl viewRootImpl) {
        Looper looper = viewRootImpl.mHandler.getLooper();
        this.mMyLooperThreadId = looper.getThread().getId();
        this.mMyProcessId = Process.myPid();
        this.mHandler = new PrivateHandler(looper);
        this.mViewRootImpl = viewRootImpl;
        this.mPrefetcher = new AccessibilityNodePrefetcher();
        this.mA11yManager = (AccessibilityManager) this.mViewRootImpl.mContext.getSystemService(AccessibilityManager.class);
    }

    private void scheduleMessage(Message message, int interrogatingPid, long interrogatingTid, boolean ignoreRequestPreparers) {
        if (!ignoreRequestPreparers && holdOffMessageIfNeeded(message, interrogatingPid, interrogatingTid)) {
            return;
        }
        if (interrogatingPid == this.mMyProcessId && interrogatingTid == this.mMyLooperThreadId && this.mHandler.hasAccessibilityCallback(message)) {
            AccessibilityInteractionClient.getInstanceForThread(interrogatingTid).setSameThreadMessage(message);
        } else if (this.mHandler.hasAccessibilityCallback(message) || Thread.currentThread().getId() != this.mMyLooperThreadId) {
            this.mHandler.sendMessage(message);
        } else {
            this.mHandler.handleMessage(message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isShown(View view) {
        return view != null && view.getWindowVisibility() == 0 && view.isShown();
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    private boolean holdOffMessageIfNeeded(Message originalMessage, int callingPid, long callingTid) {
        synchronized (this.mLock) {
            if (this.mNumActiveRequestPreparers != 0) {
                queueMessageToHandleOncePrepared(originalMessage, callingPid, callingTid);
                return true;
            } else if (originalMessage.what != 2) {
                return false;
            } else {
                SomeArgs originalMessageArgs = (SomeArgs) originalMessage.obj;
                Bundle requestArguments = (Bundle) originalMessageArgs.arg4;
                if (requestArguments == null) {
                    return false;
                }
                List<AccessibilityRequestPreparer> preparers = this.mA11yManager.getRequestPreparersForAccessibilityId(originalMessageArgs.argi1);
                if (preparers == null) {
                    return false;
                }
                String extraDataKey = requestArguments.getString(AccessibilityNodeInfo.EXTRA_DATA_REQUESTED_KEY);
                if (extraDataKey == null) {
                    return false;
                }
                this.mNumActiveRequestPreparers = preparers.size();
                for (int i = 0; i < preparers.size(); i++) {
                    Message requestPreparerMessage = this.mHandler.obtainMessage(7);
                    SomeArgs requestPreparerArgs = SomeArgs.obtain();
                    requestPreparerArgs.argi1 = originalMessageArgs.argi2 == Integer.MAX_VALUE ? -1 : originalMessageArgs.argi2;
                    requestPreparerArgs.arg1 = preparers.get(i);
                    requestPreparerArgs.arg2 = extraDataKey;
                    requestPreparerArgs.arg3 = requestArguments;
                    Message preparationFinishedMessage = this.mHandler.obtainMessage(8);
                    int i2 = this.mActiveRequestPreparerId + 1;
                    this.mActiveRequestPreparerId = i2;
                    preparationFinishedMessage.arg1 = i2;
                    requestPreparerArgs.arg4 = preparationFinishedMessage;
                    requestPreparerMessage.obj = requestPreparerArgs;
                    scheduleMessage(requestPreparerMessage, callingPid, callingTid, true);
                    this.mHandler.obtainMessage(9);
                    this.mHandler.sendEmptyMessageDelayed(9, REQUEST_PREPARER_TIMEOUT_MS);
                }
                queueMessageToHandleOncePrepared(originalMessage, callingPid, callingTid);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareForExtraDataRequestUiThread(Message message) {
        SomeArgs args = (SomeArgs) message.obj;
        ((AccessibilityRequestPreparer) args.arg1).onPrepareExtraData(args.argi1, (String) args.arg2, (Bundle) args.arg3, (Message) args.arg4);
    }

    private void queueMessageToHandleOncePrepared(Message message, int interrogatingPid, long interrogatingTid) {
        if (this.mMessagesWaitingForRequestPreparer == null) {
            this.mMessagesWaitingForRequestPreparer = new ArrayList(1);
        }
        this.mMessagesWaitingForRequestPreparer.add(new MessageHolder(message, interrogatingPid, interrogatingTid));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestPreparerDoneUiThread(Message message) {
        synchronized (this.mLock) {
            if (message.arg1 != this.mActiveRequestPreparerId) {
                Slog.e(LOG_TAG, "Surprising AccessibilityRequestPreparer callback (likely late)");
                return;
            }
            this.mNumActiveRequestPreparers--;
            if (this.mNumActiveRequestPreparers <= 0) {
                this.mHandler.removeMessages(9);
                scheduleAllMessagesWaitingForRequestPreparerLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestPreparerTimeoutUiThread() {
        synchronized (this.mLock) {
            Slog.e(LOG_TAG, "AccessibilityRequestPreparer timed out");
            scheduleAllMessagesWaitingForRequestPreparerLocked();
        }
    }

    @GuardedBy({"mLock"})
    private void scheduleAllMessagesWaitingForRequestPreparerLocked() {
        int numMessages = this.mMessagesWaitingForRequestPreparer.size();
        int i = 0;
        while (true) {
            boolean z = false;
            if (i < numMessages) {
                MessageHolder request = this.mMessagesWaitingForRequestPreparer.get(i);
                Message message = request.mMessage;
                int i2 = request.mInterrogatingPid;
                long j = request.mInterrogatingTid;
                if (i == 0) {
                    z = true;
                }
                scheduleMessage(message, i2, j, z);
                i++;
            } else {
                this.mMessagesWaitingForRequestPreparer.clear();
                this.mNumActiveRequestPreparers = 0;
                this.mActiveRequestPreparerId = -1;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findAccessibilityNodeInfoByAccessibilityIdUiThread(Message message) {
        List<AccessibilityNodeInfo> infos;
        Throwable th;
        List<AccessibilityNodeInfo> infos2;
        int flags = message.arg1;
        SomeArgs args = (SomeArgs) message.obj;
        int accessibilityViewId = args.argi1;
        int virtualDescendantId = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg1;
        MagnificationSpec spec = (MagnificationSpec) args.arg2;
        Region interactiveRegion = (Region) args.arg3;
        Bundle arguments = (Bundle) args.arg4;
        args.recycle();
        List<AccessibilityNodeInfo> infos3 = this.mTempAccessibilityNodeInfoList;
        infos3.clear();
        try {
            if (this.mViewRootImpl.mView == null) {
                infos2 = infos3;
            } else if (this.mViewRootImpl.mAttachInfo == null) {
                infos2 = infos3;
            } else {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                View root = findViewByAccessibilityId(accessibilityViewId);
                if (root == null || !isShown(root)) {
                    infos = infos3;
                } else {
                    infos = infos3;
                    try {
                        this.mPrefetcher.prefetchAccessibilityNodeInfos(root, virtualDescendantId, flags, infos3, arguments);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                return;
            }
            updateInfosForViewportAndReturnFindNodeResult(infos2, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th3) {
            th = th3;
            infos = infos3;
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
            throw th;
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    /* JADX INFO: Multiple debug info for r9v7 'infos'  java.util.List<android.view.accessibility.AccessibilityNodeInfo>: [D('flags' int), D('infos' java.util.List<android.view.accessibility.AccessibilityNodeInfo>)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findAccessibilityNodeInfosByViewIdUiThread(Message message) {
        List<AccessibilityNodeInfo> infos;
        Throwable th;
        List<AccessibilityNodeInfo> infos2;
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = (SomeArgs) message.obj;
        int interactionId = args.argi1;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg1;
        MagnificationSpec spec = (MagnificationSpec) args.arg2;
        String viewId = (String) args.arg3;
        Region interactiveRegion = (Region) args.arg4;
        args.recycle();
        List<AccessibilityNodeInfo> infos3 = this.mTempAccessibilityNodeInfoList;
        infos3.clear();
        try {
            if (this.mViewRootImpl.mView == null) {
                infos2 = infos3;
            } else if (this.mViewRootImpl.mAttachInfo == null) {
                infos2 = infos3;
            } else {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                View root = findViewByAccessibilityId(accessibilityViewId);
                if (root != null) {
                    int resolvedViewId = root.getContext().getResources().getIdentifier(viewId, null, null);
                    if (resolvedViewId <= 0) {
                        updateInfosForViewportAndReturnFindNodeResult(infos3, callback, interactionId, spec, interactiveRegion);
                        return;
                    }
                    infos = infos3;
                    try {
                        if (this.mAddNodeInfosForViewId == null) {
                            this.mAddNodeInfosForViewId = new AddNodeInfosForViewId();
                        }
                        this.mAddNodeInfosForViewId.init(resolvedViewId, infos);
                        root.findViewByPredicate(this.mAddNodeInfosForViewId);
                        this.mAddNodeInfosForViewId.reset();
                    } catch (Throwable th2) {
                        th = th2;
                        updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                        throw th;
                    }
                } else {
                    infos = infos3;
                }
                updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                return;
            }
            updateInfosForViewportAndReturnFindNodeResult(infos2, callback, interactionId, spec, interactiveRegion);
        } catch (Throwable th3) {
            th = th3;
            infos = infos3;
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
            throw th;
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findAccessibilityNodeInfosByTextUiThread(Message message) {
        int interactionId;
        Throwable th;
        int interactionId2;
        ArrayList<View> foundViews;
        View root;
        int flags = message.arg1;
        SomeArgs args = (SomeArgs) message.obj;
        String text = (String) args.arg1;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg2;
        MagnificationSpec spec = (MagnificationSpec) args.arg3;
        int accessibilityViewId = args.argi1;
        int virtualDescendantId = args.argi2;
        int interactionId3 = args.argi3;
        Region interactiveRegion = (Region) args.arg4;
        args.recycle();
        List<AccessibilityNodeInfo> infos = null;
        try {
            if (this.mViewRootImpl.mView == null) {
                interactionId2 = interactionId3;
            } else if (this.mViewRootImpl.mAttachInfo == null) {
                interactionId2 = interactionId3;
            } else {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                View root2 = findViewByAccessibilityId(accessibilityViewId);
                if (root2 != null) {
                    try {
                        if (isShown(root2)) {
                            AccessibilityNodeProvider provider = root2.getAccessibilityNodeProvider();
                            if (provider != null) {
                                infos = provider.findAccessibilityNodeInfosByText(text, virtualDescendantId);
                            } else if (virtualDescendantId == -1) {
                                ArrayList<View> foundViews2 = this.mTempArrayList;
                                foundViews2.clear();
                                root2.findViewsWithText(foundViews2, text, 7);
                                if (!foundViews2.isEmpty()) {
                                    List<AccessibilityNodeInfo> infos2 = this.mTempAccessibilityNodeInfoList;
                                    try {
                                        infos2.clear();
                                        int viewCount = foundViews2.size();
                                        int i = 0;
                                        while (i < viewCount) {
                                            View foundView = foundViews2.get(i);
                                            if (isShown(foundView)) {
                                                AccessibilityNodeProvider provider2 = foundView.getAccessibilityNodeProvider();
                                                root = root2;
                                                if (provider2 != null) {
                                                    foundViews = foundViews2;
                                                    List<AccessibilityNodeInfo> infosFromProvider = provider2.findAccessibilityNodeInfosByText(text, -1);
                                                    if (infosFromProvider != null) {
                                                        infos2.addAll(infosFromProvider);
                                                    }
                                                } else {
                                                    foundViews = foundViews2;
                                                    infos2.add(foundView.createAccessibilityNodeInfo());
                                                }
                                            } else {
                                                root = root2;
                                                foundViews = foundViews2;
                                            }
                                            i++;
                                            root2 = root;
                                            foundViews2 = foundViews;
                                        }
                                        infos = infos2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        infos = infos2;
                                        interactionId = interactionId3;
                                        updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                                        throw th;
                                    }
                                }
                            }
                            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId3, spec, interactiveRegion);
                            return;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        interactionId = interactionId3;
                        updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
                        throw th;
                    }
                }
                updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId3, spec, interactiveRegion);
                return;
            }
            updateInfosForViewportAndReturnFindNodeResult(null, callback, interactionId2, spec, interactiveRegion);
        } catch (Throwable th4) {
            th = th4;
            interactionId = interactionId3;
            updateInfosForViewportAndReturnFindNodeResult(infos, callback, interactionId, spec, interactiveRegion);
            throw th;
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findFocusUiThread(Message message) {
        int flags = message.arg1;
        int focusType = message.arg2;
        SomeArgs args = (SomeArgs) message.obj;
        int interactionId = args.argi1;
        int accessibilityViewId = args.argi2;
        int virtualDescendantId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg1;
        MagnificationSpec spec = (MagnificationSpec) args.arg2;
        Region interactiveRegion = (Region) args.arg3;
        args.recycle();
        AccessibilityNodeInfo focused = null;
        try {
            if (this.mViewRootImpl.mView != null) {
                if (this.mViewRootImpl.mAttachInfo != null) {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    View root = findViewByAccessibilityId(accessibilityViewId);
                    if (root != null && isShown(root)) {
                        if (focusType == 1) {
                            View target = root.findFocus();
                            if (isShown(target)) {
                                AccessibilityNodeProvider provider = target.getAccessibilityNodeProvider();
                                if (provider != null) {
                                    focused = provider.findFocus(focusType);
                                }
                                if (focused == null) {
                                    focused = target.createAccessibilityNodeInfo();
                                }
                            }
                        } else if (focusType == 2) {
                            View host = this.mViewRootImpl.mAccessibilityFocusedHost;
                            if (host != null) {
                                if (ViewRootImpl.isViewDescendantOf(host, root)) {
                                    if (isShown(host)) {
                                        if (host.getAccessibilityNodeProvider() != null) {
                                            if (this.mViewRootImpl.mAccessibilityFocusedVirtualView != null) {
                                                focused = AccessibilityNodeInfo.obtain(this.mViewRootImpl.mAccessibilityFocusedVirtualView);
                                            }
                                        } else if (virtualDescendantId == -1) {
                                            focused = host.createAccessibilityNodeInfo();
                                        }
                                    }
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Unknown focus type: " + focusType);
                        }
                    }
                    updateInfoForViewportAndReturnFindNodeResult(focused, callback, interactionId, spec, interactiveRegion);
                }
            }
        } finally {
            updateInfoForViewportAndReturnFindNodeResult(focused, callback, interactionId, spec, interactiveRegion);
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void focusSearchUiThread(Message message) {
        View nextView;
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = (SomeArgs) message.obj;
        int direction = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg1;
        MagnificationSpec spec = (MagnificationSpec) args.arg2;
        Region interactiveRegion = (Region) args.arg3;
        args.recycle();
        AccessibilityNodeInfo next = null;
        try {
            if (this.mViewRootImpl.mView != null) {
                if (this.mViewRootImpl.mAttachInfo != null) {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    View root = findViewByAccessibilityId(accessibilityViewId);
                    if (!(root == null || !isShown(root) || (nextView = root.focusSearch(direction)) == null)) {
                        next = nextView.createAccessibilityNodeInfo();
                    }
                    updateInfoForViewportAndReturnFindNodeResult(next, callback, interactionId, spec, interactiveRegion);
                }
            }
        } finally {
            updateInfoForViewportAndReturnFindNodeResult(next, callback, interactionId, spec, interactiveRegion);
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
        scheduleMessage(message, interrogatingPid, interrogatingTid, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performAccessibilityActionUiThread(Message message) {
        int flags = message.arg1;
        int accessibilityViewId = message.arg2;
        SomeArgs args = (SomeArgs) message.obj;
        int virtualDescendantId = args.argi1;
        int action = args.argi2;
        int interactionId = args.argi3;
        IAccessibilityInteractionConnectionCallback callback = (IAccessibilityInteractionConnectionCallback) args.arg1;
        Bundle arguments = (Bundle) args.arg2;
        args.recycle();
        boolean succeeded = false;
        try {
            if (!(this.mViewRootImpl.mView == null || this.mViewRootImpl.mAttachInfo == null || this.mViewRootImpl.mStopped)) {
                if (!this.mViewRootImpl.mPausedForTransition) {
                    this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = flags;
                    View target = findViewByAccessibilityId(accessibilityViewId);
                    if (target != null && isShown(target)) {
                        if (action == 16908694) {
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
                callback.setPerformAccessibilityActionResult(succeeded, interactionId);
            } catch (RemoteException e2) {
            }
        }
    }

    public void clearAccessibilityFocusClientThread() {
        Message message = this.mHandler.obtainMessage();
        message.what = 101;
        scheduleMessage(message, 0, 0, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAccessibilityFocusUiThread() {
        if (this.mViewRootImpl.mView != null && this.mViewRootImpl.mAttachInfo != null) {
            try {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 8;
                View root = this.mViewRootImpl.mView;
                if (root != null && isShown(root)) {
                    View host = this.mViewRootImpl.mAccessibilityFocusedHost;
                    if (host != null) {
                        if (ViewRootImpl.isViewDescendantOf(host, root)) {
                            AccessibilityNodeProvider provider = host.getAccessibilityNodeProvider();
                            AccessibilityNodeInfo focusNode = this.mViewRootImpl.mAccessibilityFocusedVirtualView;
                            if (provider == null || focusNode == null) {
                                host.performAccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                            } else {
                                provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(focusNode.getSourceNodeId()), AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                            }
                        }
                    }
                    return;
                }
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            } finally {
                this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            }
        }
    }

    public void notifyOutsideTouchClientThread() {
        Message message = this.mHandler.obtainMessage();
        message.what = 102;
        scheduleMessage(message, 0, 0, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyOutsideTouchUiThread() {
        View root;
        if (this.mViewRootImpl.mView != null && this.mViewRootImpl.mAttachInfo != null && !this.mViewRootImpl.mStopped && !this.mViewRootImpl.mPausedForTransition && (root = this.mViewRootImpl.mView) != null && isShown(root)) {
            long now = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(now, now, 4, 0.0f, 0.0f, 0);
            event.setSource(4098);
            this.mViewRootImpl.dispatchInputEvent(event);
        }
    }

    private View findViewByAccessibilityId(int accessibilityId) {
        if (accessibilityId == 2147483646) {
            return this.mViewRootImpl.mView;
        }
        return AccessibilityNodeIdManager.getInstance().findView(accessibilityId);
    }

    private void applyAppScaleAndMagnificationSpecIfNeeded(List<AccessibilityNodeInfo> infos, MagnificationSpec spec) {
        if (infos != null && shouldApplyAppScaleAndMagnificationSpec(this.mViewRootImpl.mAttachInfo.mApplicationScale, spec)) {
            int infoCount = infos.size();
            for (int i = 0; i < infoCount; i++) {
                applyAppScaleAndMagnificationSpecIfNeeded(infos.get(i), spec);
            }
        }
    }

    private void adjustIsVisibleToUserIfNeeded(List<AccessibilityNodeInfo> infos, Region interactiveRegion) {
        if (!(interactiveRegion == null || infos == null)) {
            int infoCount = infos.size();
            for (int i = 0; i < infoCount; i++) {
                adjustIsVisibleToUserIfNeeded(infos.get(i), interactiveRegion);
            }
        }
    }

    private void adjustIsVisibleToUserIfNeeded(AccessibilityNodeInfo info, Region interactiveRegion) {
        if (interactiveRegion != null && info != null) {
            Rect boundsInScreen = this.mTempRect;
            info.getBoundsInScreen(boundsInScreen);
            if (interactiveRegion.quickReject(boundsInScreen) && !shouldBypassAdjustIsVisible()) {
                info.setVisibleToUser(false);
            }
        }
    }

    private boolean shouldBypassAdjustIsVisible() {
        if (this.mViewRootImpl.mOrigWindowType == 2011) {
            return true;
        }
        return false;
    }

    private void applyAppScaleAndMagnificationSpecIfNeeded(AccessibilityNodeInfo info, MagnificationSpec spec) {
        Parcelable[] textLocations;
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
                if (info.hasExtras() && (textLocations = info.getExtras().getParcelableArray(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY)) != null) {
                    for (Parcelable parcelable : textLocations) {
                        RectF textLocation = (RectF) parcelable;
                        textLocation.scale(applicationScale);
                        if (spec != null) {
                            textLocation.scale(spec.scale);
                            textLocation.offset(spec.offsetX, spec.offsetY);
                        }
                    }
                }
                if (spec != null) {
                    View.AttachInfo attachInfo = this.mViewRootImpl.mAttachInfo;
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
        return appScale != 1.0f || (spec != null && !spec.isNop());
    }

    private void updateInfosForViewportAndReturnFindNodeResult(List<AccessibilityNodeInfo> infos, IAccessibilityInteractionConnectionCallback callback, int interactionId, MagnificationSpec spec, Region interactiveRegion) {
        try {
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            applyAppScaleAndMagnificationSpecIfNeeded(infos, spec);
            adjustIsVisibleToUserIfNeeded(infos, interactiveRegion);
            callback.setFindAccessibilityNodeInfosResult(infos, interactionId);
            if (infos != null) {
                infos.clear();
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
            throw th;
        }
        recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
    }

    private void updateInfoForViewportAndReturnFindNodeResult(AccessibilityNodeInfo info, IAccessibilityInteractionConnectionCallback callback, int interactionId, MagnificationSpec spec, Region interactiveRegion) {
        try {
            this.mViewRootImpl.mAttachInfo.mAccessibilityFetchFlags = 0;
            applyAppScaleAndMagnificationSpecIfNeeded(info, spec);
            adjustIsVisibleToUserIfNeeded(info, interactiveRegion);
            callback.setFindAccessibilityNodeInfoResult(info, interactionId);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
            throw th;
        }
        recycleMagnificationSpecAndRegionIfNeeded(spec, interactiveRegion);
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
        ClickableSpan clickableSpan;
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
        if (infoWithSpan == null || (clickableSpan = ((AccessibilityClickableSpan) span).findClickableSpan(infoWithSpan.getOriginalText())) == null) {
            return false;
        }
        clickableSpan.onClick(view);
        return true;
    }

    /* access modifiers changed from: private */
    public class AccessibilityNodePrefetcher {
        private static final int MAX_ACCESSIBILITY_NODE_INFO_BATCH_SIZE = 50;
        private final ArrayList<View> mTempViewList;

        private AccessibilityNodePrefetcher() {
            this.mTempViewList = new ArrayList<>();
        }

        public void prefetchAccessibilityNodeInfos(View view, int virtualViewId, int fetchFlags, List<AccessibilityNodeInfo> outInfos, Bundle arguments) {
            String extraDataRequested;
            AccessibilityNodeProvider provider = view.getAccessibilityNodeProvider();
            if (arguments == null) {
                extraDataRequested = null;
            } else {
                extraDataRequested = arguments.getString(AccessibilityNodeInfo.EXTRA_DATA_REQUESTED_KEY);
            }
            if (provider == null) {
                AccessibilityNodeInfo root = view.createAccessibilityNodeInfo();
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
            AccessibilityNodeInfo root2 = provider.createAccessibilityNodeInfo(virtualViewId);
            if (root2 != null) {
                if (extraDataRequested != null) {
                    provider.addExtraDataToAccessibilityNodeInfo(virtualViewId, root2, extraDataRequested, arguments);
                }
                outInfos.add(root2);
                if ((fetchFlags & 1) != 0) {
                    prefetchPredecessorsOfVirtualNode(root2, view, provider, outInfos);
                }
                if ((fetchFlags & 2) != 0) {
                    prefetchSiblingsOfVirtualNode(root2, view, provider, outInfos);
                }
                if ((fetchFlags & 4) != 0) {
                    prefetchDescendantsOfVirtualNode(root2, provider, outInfos);
                }
            }
        }

        private void enforceNodeTreeConsistent(List<AccessibilityNodeInfo> nodes) {
            LongSparseArray<AccessibilityNodeInfo> nodeMap = new LongSparseArray<>();
            int nodeCount = nodes.size();
            for (int i = 0; i < nodeCount; i++) {
                AccessibilityNodeInfo node = nodes.get(i);
                nodeMap.put(node.getSourceNodeId(), node);
            }
            AccessibilityNodeInfo root = nodeMap.valueAt(0);
            for (AccessibilityNodeInfo parent = root; parent != null; parent = nodeMap.get(parent.getParentNodeId())) {
                root = parent;
            }
            AccessibilityNodeInfo accessFocus = null;
            AccessibilityNodeInfo inputFocus = null;
            HashSet<AccessibilityNodeInfo> seen = new HashSet<>();
            Queue<AccessibilityNodeInfo> fringe = new LinkedList<>();
            fringe.add(root);
            while (!fringe.isEmpty()) {
                AccessibilityNodeInfo current = fringe.poll();
                if (seen.add(current)) {
                    if (current.isAccessibilityFocused()) {
                        if (accessFocus == null) {
                            accessFocus = current;
                        } else {
                            throw new IllegalStateException("Duplicate accessibility focus:" + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
                        }
                    }
                    if (current.isFocused()) {
                        if (inputFocus == null) {
                            inputFocus = current;
                        } else {
                            throw new IllegalStateException("Duplicate input focus: " + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
                        }
                    }
                    int childCount = current.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        AccessibilityNodeInfo child = nodeMap.get(current.getChildId(j));
                        if (child != null) {
                            fringe.add(child);
                        }
                    }
                } else {
                    throw new IllegalStateException("Duplicate node: " + current + " in window:" + AccessibilityInteractionController.this.mViewRootImpl.mAttachInfo.mAccessibilityWindowId);
                }
            }
            for (int j2 = nodeMap.size() - 1; j2 >= 0; j2--) {
                AccessibilityNodeInfo info = nodeMap.valueAt(j2);
                if (!seen.contains(info)) {
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
            AccessibilityNodeInfo info;
            ViewParent parent = current.getParentForAccessibility();
            if (parent instanceof ViewGroup) {
                ViewGroup parentGroup = (ViewGroup) parent;
                ArrayList<View> children = this.mTempViewList;
                children.clear();
                try {
                    parentGroup.addChildrenForAccessibility(children);
                    int childCount = children.size();
                    for (int i = 0; i < childCount; i++) {
                        if (outInfos.size() < 50) {
                            View child = children.get(i);
                            if (child.getAccessibilityViewId() != current.getAccessibilityViewId() && AccessibilityInteractionController.this.isShown(child)) {
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
                HashMap<View, AccessibilityNodeInfo> addedChildren = new HashMap<>();
                ArrayList<View> children = this.mTempViewList;
                children.clear();
                try {
                    root.addChildrenForAccessibility(children);
                    int childCount = children.size();
                    for (int i = 0; i < childCount; i++) {
                        if (outInfos.size() < 50) {
                            View child = children.get(i);
                            if (AccessibilityInteractionController.this.isShown(child)) {
                                AccessibilityNodeProvider provider = child.getAccessibilityNodeProvider();
                                if (provider == null) {
                                    AccessibilityNodeInfo info = child.createAccessibilityNodeInfo();
                                    if (info != null) {
                                        outInfos.add(info);
                                        addedChildren.put(child, null);
                                    }
                                } else {
                                    AccessibilityNodeInfo info2 = provider.createAccessibilityNodeInfo(-1);
                                    if (info2 != null) {
                                        outInfos.add(info2);
                                        addedChildren.put(child, info2);
                                    }
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    children.clear();
                    if (outInfos.size() < 50) {
                        for (Map.Entry<View, AccessibilityNodeInfo> entry : addedChildren.entrySet()) {
                            View addedChild = entry.getKey();
                            AccessibilityNodeInfo virtualRoot = entry.getValue();
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
            AccessibilityNodeInfo child;
            long parentNodeId = current.getParentNodeId();
            int parentAccessibilityViewId = AccessibilityNodeInfo.getAccessibilityViewId(parentNodeId);
            int parentVirtualDescendantId = AccessibilityNodeInfo.getVirtualDescendantId(parentNodeId);
            if (parentVirtualDescendantId != -1 || parentAccessibilityViewId == providerHost.getAccessibilityViewId()) {
                AccessibilityNodeInfo parent = provider.createAccessibilityNodeInfo(parentVirtualDescendantId);
                if (parent != null) {
                    int childCount = parent.getChildCount();
                    for (int i = 0; i < childCount && outInfos.size() < 50; i++) {
                        long childNodeId = parent.getChildId(i);
                        if (!(childNodeId == current.getSourceNodeId() || (child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(childNodeId))) == null)) {
                            outInfos.add(child);
                        }
                    }
                    return;
                }
                return;
            }
            prefetchSiblingsOfRealNode(providerHost, outInfos);
        }

        private void prefetchDescendantsOfVirtualNode(AccessibilityNodeInfo root, AccessibilityNodeProvider provider, List<AccessibilityNodeInfo> outInfos) {
            int initialOutInfosSize = outInfos.size();
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (outInfos.size() < 50) {
                    AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(root.getChildId(i)));
                    if (child != null) {
                        outInfos.add(child);
                    }
                } else {
                    return;
                }
            }
            if (outInfos.size() < 50) {
                int addedChildCount = outInfos.size() - initialOutInfosSize;
                for (int i2 = 0; i2 < addedChildCount; i2++) {
                    prefetchDescendantsOfVirtualNode(outInfos.get(initialOutInfosSize + i2), provider, outInfos);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class PrivateHandler extends Handler {
        private static final int FIRST_NO_ACCESSIBILITY_CALLBACK_MSG = 100;
        private static final int MSG_APP_PREPARATION_FINISHED = 8;
        private static final int MSG_APP_PREPARATION_TIMEOUT = 9;
        private static final int MSG_CLEAR_ACCESSIBILITY_FOCUS = 101;
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFOS_BY_VIEW_ID = 3;
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_ACCESSIBILITY_ID = 2;
        private static final int MSG_FIND_ACCESSIBILITY_NODE_INFO_BY_TEXT = 4;
        private static final int MSG_FIND_FOCUS = 5;
        private static final int MSG_FOCUS_SEARCH = 6;
        private static final int MSG_NOTIFY_OUTSIDE_TOUCH = 102;
        private static final int MSG_PERFORM_ACCESSIBILITY_ACTION = 1;
        private static final int MSG_PREPARE_FOR_EXTRA_DATA_REQUEST = 7;

        public PrivateHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public String getMessageName(Message message) {
            int type = message.what;
            if (type == 101) {
                return "MSG_CLEAR_ACCESSIBILITY_FOCUS";
            }
            if (type == 102) {
                return "MSG_NOTIFY_OUTSIDE_TOUCH";
            }
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
                case 7:
                    return "MSG_PREPARE_FOR_EXTRA_DATA_REQUEST";
                case 8:
                    return "MSG_APP_PREPARATION_FINISHED";
                case 9:
                    return "MSG_APP_PREPARATION_TIMEOUT";
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int type = message.what;
            if (type == 101) {
                AccessibilityInteractionController.this.clearAccessibilityFocusUiThread();
            } else if (type != 102) {
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
                    case 7:
                        AccessibilityInteractionController.this.prepareForExtraDataRequestUiThread(message);
                        return;
                    case 8:
                        AccessibilityInteractionController.this.requestPreparerDoneUiThread(message);
                        return;
                    case 9:
                        AccessibilityInteractionController.this.requestPreparerTimeoutUiThread();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message type: " + type);
                }
            } else {
                AccessibilityInteractionController.this.notifyOutsideTouchUiThread();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean hasAccessibilityCallback(Message message) {
            return message.what < 100;
        }
    }

    /* access modifiers changed from: private */
    public final class AddNodeInfosForViewId implements Predicate<View> {
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

        public boolean test(View view) {
            if (view.getId() != this.mViewId || !AccessibilityInteractionController.this.isShown(view)) {
                return false;
            }
            this.mInfos.add(view.createAccessibilityNodeInfo());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class MessageHolder {
        final int mInterrogatingPid;
        final long mInterrogatingTid;
        final Message mMessage;

        MessageHolder(Message message, int interrogatingPid, long interrogatingTid) {
            this.mMessage = message;
            this.mInterrogatingPid = interrogatingPid;
            this.mInterrogatingTid = interrogatingTid;
        }
    }
}
