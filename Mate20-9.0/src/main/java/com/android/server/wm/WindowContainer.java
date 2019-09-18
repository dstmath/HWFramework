package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.util.HwPCUtils;
import android.util.Pools;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.MagnificationSpec;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.wm.SurfaceAnimator;
import com.android.server.wm.WindowContainer;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

class WindowContainer<E extends WindowContainer> extends ConfigurationContainer<E> implements Comparable<WindowContainer>, SurfaceAnimator.Animatable {
    static final int ANIMATION_LAYER_BOOSTED = 1;
    static final int ANIMATION_LAYER_HOME = 2;
    static final int ANIMATION_LAYER_STANDARD = 0;
    static final int POSITION_BOTTOM = Integer.MIN_VALUE;
    static final int POSITION_TOP = Integer.MAX_VALUE;
    private static final String TAG = "WindowManager";
    protected final WindowList<E> mChildren = new WindowList<>();
    private boolean mCommittedReparentToAnimationLeash;
    /* access modifiers changed from: private */
    public final Pools.SynchronizedPool<WindowContainer<E>.ForAllWindowsConsumerWrapper> mConsumerWrapperPool = new Pools.SynchronizedPool<>(3);
    WindowContainerController mController;
    private Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    int mHeight;
    private int mLastLayer = 0;
    private SurfaceControl mLastRelativeToLayer = null;
    protected final Point mLastSurfacePosition = new Point();
    float mLazyScale = 0.75f;
    protected int mOrientation = -1;
    private WindowContainer<WindowContainer> mParent = null;
    protected final SurfaceControl.Transaction mPendingTransaction;
    protected final WindowManagerService mService;
    protected final SurfaceAnimator mSurfaceAnimator;
    protected SurfaceControl mSurfaceControl;
    private final LinkedList<WindowContainer> mTmpChain1 = new LinkedList<>();
    private final LinkedList<WindowContainer> mTmpChain2 = new LinkedList<>();
    private final Point mTmpPos = new Point();
    private int mTreeWeight = 1;
    int mWidth;
    private WindowManager mWindowManager;

    @interface AnimationLayer {
    }

    @RCUnownedThisRef
    private final class ForAllWindowsConsumerWrapper implements ToBooleanFunction<WindowState> {
        private Consumer<WindowState> mConsumer;

        private ForAllWindowsConsumerWrapper() {
        }

        /* access modifiers changed from: package-private */
        public void setConsumer(Consumer<WindowState> consumer) {
            this.mConsumer = consumer;
        }

        public boolean apply(WindowState w) {
            this.mConsumer.accept(w);
            return false;
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mConsumer = null;
            WindowContainer.this.mConsumerWrapperPool.release(this);
        }
    }

    WindowContainer(WindowManagerService service) {
        this.mService = service;
        this.mPendingTransaction = service.mTransactionFactory.make();
        this.mSurfaceAnimator = new SurfaceAnimator(this, new Runnable() {
            @RCUnownedThisRef
            public void run() {
                WindowContainer.this.onAnimationFinished();
            }
        }, service);
        this.mWindowManager = (WindowManager) this.mService.mContext.getSystemService("window");
    }

    /* access modifiers changed from: protected */
    public final WindowContainer getParent() {
        return this.mParent;
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return this.mChildren.size();
    }

    /* access modifiers changed from: protected */
    public E getChildAt(int index) {
        return (WindowContainer) this.mChildren.get(index);
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        super.onConfigurationChanged(newParentConfig);
        updateSurfacePosition();
        scheduleAnimation();
    }

    /* access modifiers changed from: protected */
    public final void setParent(WindowContainer<WindowContainer> parent) {
        this.mParent = parent;
        if (this.mParent != null) {
            onConfigurationChanged(this.mParent.getConfiguration());
            onMergedOverrideConfigurationChanged();
        }
        onParentSet();
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
        if (this.mParent != null) {
            if (this.mSurfaceControl == null) {
                this.mSurfaceControl = makeSurface().build();
                getPendingTransaction().show(this.mSurfaceControl);
                updateSurfacePosition();
            } else {
                reparentSurfaceControl(getPendingTransaction(), this.mParent.mSurfaceControl);
            }
            this.mParent.assignChildLayers();
            scheduleAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public void addChild(E child, Comparator<E> comparator) {
        if (child.getParent() == null) {
            int positionToAdd = -1;
            if (comparator != null) {
                int count = this.mChildren.size();
                int i = 0;
                while (true) {
                    if (i >= count) {
                        break;
                    } else if (comparator.compare(child, (WindowContainer) this.mChildren.get(i)) < 0) {
                        positionToAdd = i;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (positionToAdd == -1) {
                this.mChildren.add(child);
            } else {
                this.mChildren.add(positionToAdd, child);
            }
            onChildAdded(child);
            child.setParent(this);
            return;
        }
        throw new IllegalArgumentException("addChild: container=" + child.getName() + " is already a child of container=" + child.getParent().getName() + " can't add to container=" + getName());
    }

    /* access modifiers changed from: package-private */
    public void addChild(E child, int index) {
        if (child.getParent() == null) {
            this.mChildren.add(index, child);
            onChildAdded(child);
            child.setParent(this);
            return;
        }
        throw new IllegalArgumentException("addChild: container=" + child.getName() + " is already a child of container=" + child.getParent().getName() + " can't add to container=" + getName());
    }

    private void onChildAdded(WindowContainer child) {
        this.mTreeWeight += child.mTreeWeight;
        for (WindowContainer parent = getParent(); parent != null; parent = parent.getParent()) {
            parent.mTreeWeight += child.mTreeWeight;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeChild(E child) {
        if (this.mChildren.remove(child)) {
            onChildRemoved(child);
            child.setParent(null);
            return;
        }
        throw new IllegalArgumentException("removeChild: container=" + child.getName() + " is not a child of container=" + getName());
    }

    private void onChildRemoved(WindowContainer child) {
        this.mTreeWeight -= child.mTreeWeight;
        for (WindowContainer parent = getParent(); parent != null; parent = parent.getParent()) {
            parent.mTreeWeight -= child.mTreeWeight;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
        while (!this.mChildren.isEmpty()) {
            E child = (WindowContainer) this.mChildren.peekLast();
            child.removeImmediately();
            if (this.mChildren.remove(child)) {
                onChildRemoved(child);
            }
        }
        if (this.mSurfaceControl != null) {
            this.mPendingTransaction.destroy(this.mSurfaceControl);
            if (this.mParent != null) {
                this.mParent.getPendingTransaction().merge(this.mPendingTransaction);
            }
            this.mSurfaceControl = null;
            scheduleAnimation();
        }
        if (this.mParent != null) {
            this.mParent.removeChild(this);
        }
        if (this.mController != null) {
            setController(null);
        }
    }

    /* access modifiers changed from: package-private */
    public int getPrefixOrderIndex() {
        if (this.mParent == null) {
            return 0;
        }
        return this.mParent.getPrefixOrderIndex(this);
    }

    private int getPrefixOrderIndex(WindowContainer child) {
        int order = 0;
        for (int i = 0; i < this.mChildren.size(); i++) {
            WindowContainer childI = (WindowContainer) this.mChildren.get(i);
            if (child == childI) {
                break;
            }
            order += childI.mTreeWeight;
        }
        if (this.mParent != null) {
            order += this.mParent.getPrefixOrderIndex(this);
        }
        return order + 1;
    }

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).removeIfPossible();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasChild(E child) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            E current = (WindowContainer) this.mChildren.get(i);
            if (current == child || current.hasChild(child)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, E child, boolean includingParents) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException("removeChild: container=" + child.getName() + " is not a child of container=" + getName() + " current parent=" + child.getParent());
        } else if ((position >= 0 || position == Integer.MIN_VALUE) && (position <= this.mChildren.size() || position == Integer.MAX_VALUE)) {
            if (position >= this.mChildren.size() - 1) {
                position = Integer.MAX_VALUE;
            } else if (position == 0) {
                position = Integer.MIN_VALUE;
            }
            if (position == Integer.MIN_VALUE) {
                if (this.mChildren.peekFirst() != child) {
                    this.mChildren.remove(child);
                    this.mChildren.addFirst(child);
                }
                if (includingParents && getParent() != null) {
                    getParent().positionChildAt(Integer.MIN_VALUE, this, true);
                }
            } else if (position != Integer.MAX_VALUE) {
                this.mChildren.remove(child);
                this.mChildren.add(position, child);
            } else {
                if (this.mChildren.peekLast() != child) {
                    this.mChildren.remove(child);
                    this.mChildren.add(child);
                }
                if (includingParents && getParent() != null) {
                    getParent().positionChildAt(Integer.MAX_VALUE, this, true);
                }
            }
        } else {
            throw new IllegalArgumentException("positionAt: invalid position=" + position + ", children number=" + this.mChildren.size());
        }
    }

    public void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
        int diff = diffOverrideBounds(overrideConfiguration.windowConfiguration.getBounds());
        super.onOverrideConfigurationChanged(overrideConfiguration);
        if (this.mParent != null) {
            this.mParent.onDescendantOverrideConfigurationChanged();
        }
        if (diff != 0) {
            if ((diff & 2) == 2) {
                onResize();
            } else {
                onMovedByResize();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDescendantOverrideConfigurationChanged() {
        if (this.mParent != null) {
            this.mParent.onDescendantOverrideConfigurationChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged(DisplayContent dc) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onDisplayChanged(dc);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWaitingForDrawnIfResizingChanged() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).setWaitingForDrawnIfResizingChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void onResize() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onParentResize();
        }
    }

    /* access modifiers changed from: package-private */
    public void onParentResize() {
        if (!hasOverrideBounds()) {
            onResize();
        }
    }

    /* access modifiers changed from: package-private */
    public void onMovedByResize() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onMovedByResize();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetDragResizingChangeReported() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).resetDragResizingChangeReported();
        }
    }

    /* access modifiers changed from: package-private */
    public void forceWindowsScaleableInTransaction(boolean force) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).forceWindowsScaleableInTransaction(force);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSelfOrChildAnimating() {
        if (isSelfAnimating()) {
            return true;
        }
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowContainer) this.mChildren.get(j)).isSelfOrChildAnimating()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimating() {
        return isSelfAnimating() || (this.mParent != null && this.mParent.isAnimating());
    }

    /* access modifiers changed from: package-private */
    public boolean isAppAnimating() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowContainer) this.mChildren.get(j)).isAppAnimating()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSelfAnimating() {
        return this.mSurfaceAnimator.isAnimating();
    }

    /* access modifiers changed from: package-private */
    public void sendAppVisibilityToClients() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).sendAppVisibilityToClients();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasContentToDisplay() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowContainer) this.mChildren.get(i)).hasContentToDisplay()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowContainer) this.mChildren.get(i)).isVisible()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isOnTop() {
        return getParent().getTopChild() == this && getParent().isOnTop();
    }

    /* access modifiers changed from: package-private */
    public E getTopChild() {
        return (WindowContainer) this.mChildren.peekLast();
    }

    /* access modifiers changed from: package-private */
    public boolean checkCompleteDeferredRemoval() {
        boolean stillDeferringRemoval = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            stillDeferringRemoval |= ((WindowContainer) this.mChildren.get(i)).checkCompleteDeferredRemoval();
        }
        return stillDeferringRemoval;
    }

    /* access modifiers changed from: package-private */
    public void checkAppWindowsReadyToShow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).checkAppWindowsReadyToShow();
        }
    }

    /* access modifiers changed from: package-private */
    public void onAppTransitionDone() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onAppTransitionDone();
        }
    }

    /* access modifiers changed from: package-private */
    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    /* access modifiers changed from: package-private */
    public int getOrientation() {
        return getOrientation(this.mOrientation);
    }

    /* access modifiers changed from: package-private */
    public int getOrientation(int candidate) {
        if (!fillsParent()) {
            if (!(HwFoldScreenState.isFoldScreenDevice() && (this instanceof TaskStack) && (inCoordinationPrimaryWindowingMode() || inCoordinationSecondaryWindowingMode()))) {
                return -2;
            }
        }
        if (this.mOrientation != -2 && this.mOrientation != -1) {
            return this.mOrientation;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowContainer wc = (WindowContainer) this.mChildren.get(i);
            int orientation = wc.getOrientation(candidate == 3 ? 3 : -2);
            if (orientation == 3) {
                candidate = orientation;
            } else if (orientation != -2 && (wc.fillsParent() || orientation != -1)) {
                return orientation;
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public boolean fillsParent() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void switchUser() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).switchUser();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                if (((WindowContainer) this.mChildren.get(i)).forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
        } else {
            int count = this.mChildren.size();
            for (int i2 = 0; i2 < count; i2++) {
                if (((WindowContainer) this.mChildren.get(i2)).forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void forAllWindows(Consumer<WindowState> callback, boolean traverseTopToBottom) {
        WindowContainer<E>.ForAllWindowsConsumerWrapper wrapper = obtainConsumerWrapper(callback);
        forAllWindows((ToBooleanFunction<WindowState>) wrapper, traverseTopToBottom);
        wrapper.release();
    }

    /* access modifiers changed from: package-private */
    public void forAllTasks(Consumer<Task> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).forAllTasks(callback);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getWindow(Predicate<WindowState> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState w = ((WindowContainer) this.mChildren.get(i)).getWindow(callback);
            if (w != null) {
                return w;
            }
        }
        return null;
    }

    public int compareTo(WindowContainer other) {
        if (this == other) {
            return 0;
        }
        int i = -1;
        if (this.mParent == null || this.mParent != other.mParent) {
            LinkedList<WindowContainer> thisParentChain = this.mTmpChain1;
            LinkedList<WindowContainer> otherParentChain = this.mTmpChain2;
            try {
                getParents(thisParentChain);
                other.getParents(otherParentChain);
                WindowContainer commonAncestor = null;
                WindowContainer thisTop = thisParentChain.peekLast();
                WindowContainer otherTop = otherParentChain.peekLast();
                while (thisTop != null && otherTop != null && thisTop == otherTop) {
                    commonAncestor = thisParentChain.removeLast();
                    otherParentChain.removeLast();
                    thisTop = thisParentChain.peekLast();
                    otherTop = otherParentChain.peekLast();
                }
                if (commonAncestor == null) {
                    throw new IllegalArgumentException("No in the same hierarchy this=" + thisParentChain + " other=" + otherParentChain);
                } else if (commonAncestor == this) {
                    return -1;
                } else {
                    if (commonAncestor == other) {
                        this.mTmpChain1.clear();
                        this.mTmpChain2.clear();
                        return 1;
                    }
                    WindowList<E> windowList = commonAncestor.mChildren;
                    if (windowList.indexOf(thisParentChain.peekLast()) > windowList.indexOf(otherParentChain.peekLast())) {
                        i = 1;
                    }
                    this.mTmpChain1.clear();
                    this.mTmpChain2.clear();
                    return i;
                }
            } finally {
                this.mTmpChain1.clear();
                this.mTmpChain2.clear();
            }
        } else {
            WindowList<E> windowList2 = this.mParent.mChildren;
            if (windowList2.indexOf(this) > windowList2.indexOf(other)) {
                i = 1;
            }
            return i;
        }
    }

    private void getParents(LinkedList<WindowContainer> parents) {
        parents.clear();
        WindowContainer current = this;
        do {
            parents.addLast(current);
            current = current.mParent;
        } while (current != null);
    }

    /* access modifiers changed from: package-private */
    public WindowContainerController getController() {
        return this.mController;
    }

    /* access modifiers changed from: package-private */
    public void setController(WindowContainerController controller) {
        if (this.mController == null || controller == null) {
            if (controller != null) {
                controller.setContainer(this);
            } else if (this.mController != null) {
                this.mController.setContainer(null);
            }
            this.mController = controller;
            return;
        }
        throw new IllegalArgumentException("Can't set controller=" + this.mController + " for container=" + this + " Already set to=" + this.mController);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurface() {
        return getParent().makeChildSurface(this);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
        WindowContainer p = getParent();
        if (p != null) {
            return p.makeChildSurface(child).setParent(this.mSurfaceControl);
        }
        Slog.e(TAG, "WindowContainer makeChildSurface get null parent " + this);
        if (this instanceof TaskStack) {
            return this.mService.getDefaultDisplayContentLocked().mTaskStackContainers.makeChildSurface(child).setParent(this.mSurfaceControl);
        }
        return null;
    }

    public SurfaceControl getParentSurfaceControl() {
        WindowContainer parent = getParent();
        if (parent == null) {
            return null;
        }
        return parent.getSurfaceControl();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldMagnify() {
        if (this.mSurfaceControl == null) {
            return false;
        }
        for (int i = 0; i < this.mChildren.size(); i++) {
            if (!((WindowContainer) this.mChildren.get(i)).shouldMagnify()) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public SurfaceSession getSession() {
        if (getParent() != null) {
            return getParent().getSession();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void assignLayer(SurfaceControl.Transaction t, int layer) {
        boolean changed = (layer == this.mLastLayer && this.mLastRelativeToLayer == null) ? false : true;
        if (this.mSurfaceControl != null && changed) {
            setLayer(t, layer);
            this.mLastLayer = layer;
            this.mLastRelativeToLayer = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void assignRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        boolean changed = (layer == this.mLastLayer && this.mLastRelativeToLayer == relativeTo) ? false : true;
        if (this.mSurfaceControl != null && changed) {
            setRelativeLayer(t, relativeTo, layer);
            this.mLastLayer = layer;
            this.mLastRelativeToLayer = relativeTo;
        }
    }

    /* access modifiers changed from: protected */
    public void setLayer(SurfaceControl.Transaction t, int layer) {
        this.mSurfaceAnimator.setLayer(t, layer);
    }

    /* access modifiers changed from: protected */
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        this.mSurfaceAnimator.setRelativeLayer(t, relativeTo, layer);
    }

    /* access modifiers changed from: protected */
    public void reparentSurfaceControl(SurfaceControl.Transaction t, SurfaceControl newParent) {
        this.mSurfaceAnimator.reparent(t, newParent);
    }

    /* access modifiers changed from: package-private */
    public void assignChildLayers(SurfaceControl.Transaction t) {
        int j = 0;
        int layer = 0;
        for (int j2 = 0; j2 < this.mChildren.size(); j2++) {
            WindowContainer wc = (WindowContainer) this.mChildren.get(j2);
            wc.assignChildLayers(t);
            if (!wc.needsZBoost()) {
                wc.assignLayer(t, layer);
                layer++;
            }
        }
        while (true) {
            int j3 = j;
            if (j3 < this.mChildren.size()) {
                WindowContainer wc2 = (WindowContainer) this.mChildren.get(j3);
                if (wc2.needsZBoost()) {
                    wc2.assignLayer(t, layer);
                    layer++;
                }
                j = j3 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void assignChildLayers() {
        assignChildLayers(getPendingTransaction());
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public boolean needsZBoost() {
        for (int i = 0; i < this.mChildren.size(); i++) {
            if (((WindowContainer) this.mChildren.get(i)).needsZBoost()) {
                return true;
            }
        }
        return false;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        proto.write(1120986464258L, this.mOrientation);
        proto.write(1133871366147L, isVisible());
        this.mSurfaceAnimator.writeToProto(proto, 1146756268036L);
        proto.end(token);
    }

    private WindowContainer<E>.ForAllWindowsConsumerWrapper obtainConsumerWrapper(Consumer<WindowState> consumer) {
        WindowContainer<E>.ForAllWindowsConsumerWrapper wrapper = (ForAllWindowsConsumerWrapper) this.mConsumerWrapperPool.acquire();
        if (wrapper == null) {
            wrapper = new ForAllWindowsConsumerWrapper();
        }
        wrapper.setConsumer(consumer);
        return wrapper;
    }

    /* access modifiers changed from: package-private */
    public void applyMagnificationSpec(SurfaceControl.Transaction t, MagnificationSpec spec) {
        if (shouldMagnify()) {
            t.setMatrix(this.mSurfaceControl, spec.scale, 0.0f, 0.0f, spec.scale).setPosition(this.mSurfaceControl, spec.offsetX, spec.offsetY);
            return;
        }
        for (int i = 0; i < this.mChildren.size(); i++) {
            ((WindowContainer) this.mChildren.get(i)).applyMagnificationSpec(t, spec);
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        if (HwFoldScreenState.isFoldScreenDevice() && (this instanceof AppWindowToken)) {
            updateSurfacePosition();
        }
        SurfaceControl.mergeToGlobalTransaction(getPendingTransaction());
        this.mCommittedReparentToAnimationLeash = this.mSurfaceAnimator.hasLeash();
        for (int i = 0; i < this.mChildren.size(); i++) {
            ((WindowContainer) this.mChildren.get(i)).prepareSurfaces();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasCommittedReparentToAnimationLeash() {
        return hasCommittedReparentToAnimationLeash(false);
    }

    /* access modifiers changed from: package-private */
    public boolean hasCommittedReparentToAnimationLeash(boolean isDelay) {
        if (isDelay) {
            return this.mCommittedReparentToAnimationLeash || this.mSurfaceAnimator.isLeashAnimationDoing();
        }
        return this.mCommittedReparentToAnimationLeash;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimation() {
        if (this.mParent != null) {
            this.mParent.scheduleAnimation();
        }
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        return this.mPendingTransaction;
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(SurfaceControl.Transaction t, AnimationAdapter anim, boolean hidden) {
        this.mSurfaceAnimator.startAnimation(t, anim, hidden);
    }

    /* access modifiers changed from: package-private */
    public void transferAnimation(WindowContainer from) {
        this.mSurfaceAnimator.transferAnimation(from.mSurfaceAnimator);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation() {
        this.mSurfaceAnimator.cancelAnimation();
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        return makeSurface();
    }

    public SurfaceControl getAnimationLeashParent() {
        return getParentSurfaceControl();
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl getAppAnimationLayer(@AnimationLayer int animationLayer) {
        WindowContainer parent = getParent();
        if (parent != null) {
            return parent.getAppAnimationLayer(animationLayer);
        }
        return null;
    }

    public void commitPendingTransaction() {
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public void reassignLayer(SurfaceControl.Transaction t) {
        WindowContainer parent = getParent();
        if (parent != null) {
            parent.assignChildLayers(t);
        }
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        this.mLastLayer = -1;
        reassignLayer(t);
    }

    public void onAnimationLeashDestroyed(SurfaceControl.Transaction t) {
        this.mLastLayer = -1;
        reassignLayer(t);
    }

    /* access modifiers changed from: protected */
    public void onAnimationFinished() {
    }

    /* access modifiers changed from: package-private */
    public AnimationAdapter getAnimation() {
        return this.mSurfaceAnimator.getAnimation();
    }

    /* access modifiers changed from: package-private */
    public void startDelayingAnimationStart() {
        this.mSurfaceAnimator.startDelayingAnimationStart();
    }

    /* access modifiers changed from: package-private */
    public void endDelayingAnimationStart() {
        this.mSurfaceAnimator.endDelayingAnimationStart();
    }

    public int getSurfaceWidth() {
        return this.mSurfaceControl.getWidth();
    }

    public int getSurfaceHeight() {
        return this.mSurfaceControl.getHeight();
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (this.mSurfaceAnimator.isAnimating()) {
            pw.print(prefix);
            pw.println("ContainerAnimator:");
            SurfaceAnimator surfaceAnimator = this.mSurfaceAnimator;
            surfaceAnimator.dump(pw, prefix + "  ");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSurfacePosition() {
        if (this.mSurfaceControl != null) {
            getRelativePosition(this.mTmpPos);
            if ((this instanceof TaskStack) && inMultiWindowMode() && !HwPCUtils.isPcDynamicStack(((TaskStack) this).mStackId)) {
                updatedisplayinfo();
                float ratio = 1.0f;
                float pendingX = 0.0f;
                float pendingY = 0.0f;
                if (this.mService.getLazyMode() == 1) {
                    ratio = this.mLazyScale;
                    pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
                } else if (this.mService.getLazyMode() == 2) {
                    ratio = this.mLazyScale;
                    pendingX = ((float) this.mWidth) * (1.0f - this.mLazyScale);
                    pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
                }
                this.mTmpPos.x = (int) ((((float) this.mTmpPos.x) * ratio) + pendingX);
                this.mTmpPos.y = (int) ((((float) this.mTmpPos.y) * ratio) + pendingY);
            }
            if (!(this.mService == null || this.mService.mHwWMSEx == null)) {
                Rect bound = new Rect(this.mTmpPos.x, this.mTmpPos.y, -1, -1);
                this.mService.mHwWMSEx.updateDimPositionForPCMode(this, bound);
                this.mTmpPos.x = bound.left;
                this.mTmpPos.y = bound.top;
            }
            if (!this.mTmpPos.equals(this.mLastSurfacePosition)) {
                getPendingTransaction().setPosition(this.mSurfaceControl, (float) this.mTmpPos.x, (float) this.mTmpPos.y);
                this.mLastSurfacePosition.set(this.mTmpPos.x, this.mTmpPos.y);
            }
        }
    }

    private void updatedisplayinfo() {
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo);
        boolean isPortrait = this.mDefaultDisplayInfo.logicalHeight > this.mDefaultDisplayInfo.logicalWidth;
        this.mWidth = isPortrait ? this.mDefaultDisplayInfo.logicalWidth : this.mDefaultDisplayInfo.logicalHeight;
        this.mHeight = isPortrait ? this.mDefaultDisplayInfo.logicalHeight : this.mDefaultDisplayInfo.logicalWidth;
    }

    /* access modifiers changed from: package-private */
    public void getRelativePosition(Point outPos) {
        Rect bounds = getBounds();
        outPos.set(bounds.left, bounds.top);
        WindowContainer parent = getParent();
        if (parent != null) {
            Rect parentBounds = parent.getBounds();
            if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode() && ((this instanceof AppWindowToken) || (this instanceof Task) || (this instanceof TaskStack))) {
                Point tmpPoint = getSubFoldScaleModePoint(outPos.x, outPos.y);
                outPos.set(tmpPoint.x, tmpPoint.y);
                if (!(this instanceof TaskStack)) {
                    Point tmpParentPoint = getSubFoldScaleModePoint(parentBounds.left, parentBounds.top);
                    parentBounds.left = tmpParentPoint.x;
                    parentBounds.top = tmpParentPoint.y;
                }
            }
            outPos.offset(-parentBounds.left, -parentBounds.top);
        }
    }

    /* access modifiers changed from: package-private */
    public Dimmer getDimmer() {
        if (this.mParent == null) {
            return null;
        }
        return this.mParent.getDimmer();
    }

    /* access modifiers changed from: package-private */
    public int getLayer() {
        return this.mLastLayer;
    }

    private Point getSubFoldScaleModePoint(int x, int y) {
        float pendingX = 0.0f;
        float pendingY = 0.0f;
        float ratio = this.mService.mSubFoldModeScale;
        int rotation = this.mService.getDefaultDisplayRotation();
        if (rotation == 0) {
            pendingY = ((float) 2480) * (1.0f - ratio);
        } else if (rotation == 1) {
            pendingY = ((float) 1136) * (1.0f - ratio);
            pendingX = ((float) 2480) * (1.0f - ratio);
        } else if (rotation == 2) {
            pendingX = ((float) 1136) * (1.0f - ratio);
        }
        return new Point((int) ((((float) x) * ratio) + pendingX), (int) ((((float) y) * ratio) + pendingY));
    }
}
