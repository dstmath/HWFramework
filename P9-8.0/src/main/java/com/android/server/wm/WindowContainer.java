package com.android.server.wm;

import android.content.res.Configuration;
import android.util.Pools.SynchronizedPool;
import com.android.internal.util.ToBooleanFunction;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

class WindowContainer<E extends WindowContainer> implements Comparable<WindowContainer> {
    static final int POSITION_BOTTOM = Integer.MIN_VALUE;
    static final int POSITION_TOP = Integer.MAX_VALUE;
    protected final WindowList<E> mChildren = new WindowList();
    private final SynchronizedPool<ForAllWindowsConsumerWrapper> mConsumerWrapperPool = new SynchronizedPool(3);
    private WindowContainerController mController;
    private Configuration mFullConfiguration = new Configuration();
    private Configuration mMergedOverrideConfiguration = new Configuration();
    protected int mOrientation = -1;
    private Configuration mOverrideConfiguration = new Configuration();
    private WindowContainer mParent = null;
    private final LinkedList<WindowContainer> mTmpChain1 = new LinkedList();
    private final LinkedList<WindowContainer> mTmpChain2 = new LinkedList();

    private final class ForAllWindowsConsumerWrapper implements ToBooleanFunction<WindowState> {
        private Consumer<WindowState> mConsumer;

        /* synthetic */ ForAllWindowsConsumerWrapper(WindowContainer this$0, ForAllWindowsConsumerWrapper -this1) {
            this();
        }

        private ForAllWindowsConsumerWrapper() {
        }

        void setConsumer(Consumer<WindowState> consumer) {
            this.mConsumer = consumer;
        }

        public boolean apply(WindowState w) {
            this.mConsumer.accept(w);
            return false;
        }

        void release() {
            this.mConsumer = null;
            WindowContainer.this.mConsumerWrapperPool.release(this);
        }
    }

    WindowContainer() {
    }

    protected final WindowContainer getParent() {
        return this.mParent;
    }

    protected final void setParent(WindowContainer parent) {
        this.mParent = parent;
        if (this.mParent != null) {
            onConfigurationChanged(this.mParent.mFullConfiguration);
            onMergedOverrideConfigurationChanged();
        }
        onParentSet();
    }

    void onParentSet() {
    }

    protected void addChild(E child, Comparator<E> comparator) {
        if (child.getParent() != null) {
            throw new IllegalArgumentException("addChild: container=" + child.getName() + " is already a child of container=" + child.getParent().getName() + " can't add to container=" + getName());
        }
        int positionToAdd = -1;
        if (comparator != null) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                if (comparator.compare(child, (WindowContainer) this.mChildren.get(i)) < 0) {
                    positionToAdd = i;
                    break;
                }
            }
        }
        if (positionToAdd == -1) {
            this.mChildren.add(child);
        } else {
            this.mChildren.add(positionToAdd, child);
        }
        child.setParent(this);
    }

    void addChild(E child, int index) {
        if (child.getParent() != null) {
            throw new IllegalArgumentException("addChild: container=" + child.getName() + " is already a child of container=" + child.getParent().getName() + " can't add to container=" + getName());
        }
        this.mChildren.add(index, child);
        child.setParent(this);
    }

    void removeChild(E child) {
        if (this.mChildren.remove(child)) {
            child.setParent(null);
            return;
        }
        throw new IllegalArgumentException("removeChild: container=" + child.getName() + " is not a child of container=" + getName());
    }

    void removeImmediately() {
        while (!this.mChildren.isEmpty()) {
            WindowContainer child = (WindowContainer) this.mChildren.peekLast();
            child.removeImmediately();
            this.mChildren.remove(child);
        }
        if (this.mParent != null) {
            this.mParent.removeChild(this);
        }
        if (this.mController != null) {
            setController(null);
        }
    }

    void removeIfPossible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).removeIfPossible();
        }
    }

    boolean hasChild(WindowContainer child) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowContainer current = (WindowContainer) this.mChildren.get(i);
            if (current == child || current.hasChild(child)) {
                return true;
            }
        }
        return false;
    }

    void positionChildAt(int position, E child, boolean includingParents) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException("removeChild: container=" + child.getName() + " is not a child of container=" + getName() + " current parent=" + child.getParent());
        } else if ((position >= 0 || position == Integer.MIN_VALUE) && (position <= this.mChildren.size() || position == Integer.MAX_VALUE)) {
            if (position >= this.mChildren.size() - 1) {
                position = Integer.MAX_VALUE;
            } else if (position == 0) {
                position = Integer.MIN_VALUE;
            }
            switch (position) {
                case Integer.MIN_VALUE:
                    if (this.mChildren.peekFirst() != child) {
                        this.mChildren.remove(child);
                        this.mChildren.addFirst(child);
                    }
                    if (includingParents && getParent() != null) {
                        getParent().positionChildAt(Integer.MIN_VALUE, this, true);
                        return;
                    }
                    return;
                case Integer.MAX_VALUE:
                    if (this.mChildren.peekLast() != child) {
                        this.mChildren.remove(child);
                        this.mChildren.add(child);
                    }
                    if (includingParents && getParent() != null) {
                        getParent().positionChildAt(Integer.MAX_VALUE, this, true);
                        return;
                    }
                    return;
                default:
                    this.mChildren.remove(child);
                    this.mChildren.add(position, child);
                    return;
            }
        } else {
            throw new IllegalArgumentException("positionAt: invalid position=" + position + ", children number=" + this.mChildren.size());
        }
    }

    Configuration getConfiguration() {
        return this.mFullConfiguration;
    }

    void onConfigurationChanged(Configuration newParentConfig) {
        this.mFullConfiguration.setTo(newParentConfig);
        this.mFullConfiguration.updateFrom(this.mOverrideConfiguration);
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onConfigurationChanged(this.mFullConfiguration);
        }
    }

    Configuration getOverrideConfiguration() {
        return this.mOverrideConfiguration;
    }

    void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
        this.mOverrideConfiguration.setTo(overrideConfiguration);
        onConfigurationChanged(this.mParent != null ? this.mParent.getConfiguration() : Configuration.EMPTY);
        onMergedOverrideConfigurationChanged();
        if (this.mParent != null) {
            this.mParent.onDescendantOverrideConfigurationChanged();
        }
    }

    void onDescendantOverrideConfigurationChanged() {
        if (this.mParent != null) {
            this.mParent.onDescendantOverrideConfigurationChanged();
        }
    }

    Configuration getMergedOverrideConfiguration() {
        return this.mMergedOverrideConfiguration;
    }

    private void onMergedOverrideConfigurationChanged() {
        if (this.mParent != null) {
            this.mMergedOverrideConfiguration.setTo(this.mParent.getMergedOverrideConfiguration());
            this.mMergedOverrideConfiguration.updateFrom(this.mOverrideConfiguration);
        } else {
            this.mMergedOverrideConfiguration.setTo(this.mOverrideConfiguration);
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onMergedOverrideConfigurationChanged();
        }
    }

    void onDisplayChanged(DisplayContent dc) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onDisplayChanged(dc);
        }
    }

    void setWaitingForDrawnIfResizingChanged() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).setWaitingForDrawnIfResizingChanged();
        }
    }

    void onResize() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onResize();
        }
    }

    void onMovedByResize() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onMovedByResize();
        }
    }

    void resetDragResizingChangeReported() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).resetDragResizingChangeReported();
        }
    }

    void forceWindowsScaleableInTransaction(boolean force) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).forceWindowsScaleableInTransaction(force);
        }
    }

    boolean isAnimating() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowContainer) this.mChildren.get(j)).isAnimating()) {
                return true;
            }
        }
        return false;
    }

    void sendAppVisibilityToClients() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).sendAppVisibilityToClients();
        }
    }

    void setVisibleBeforeClientHidden() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).setVisibleBeforeClientHidden();
        }
    }

    boolean hasContentToDisplay() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowContainer) this.mChildren.get(i)).hasContentToDisplay()) {
                return true;
            }
        }
        return false;
    }

    boolean isVisible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowContainer) this.mChildren.get(i)).isVisible()) {
                return true;
            }
        }
        return false;
    }

    E getTopChild() {
        return (WindowContainer) this.mChildren.peekLast();
    }

    boolean checkCompleteDeferredRemoval() {
        boolean stillDeferringRemoval = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            stillDeferringRemoval |= ((WindowContainer) this.mChildren.get(i)).checkCompleteDeferredRemoval();
        }
        return stillDeferringRemoval;
    }

    void checkAppWindowsReadyToShow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).checkAppWindowsReadyToShow();
        }
    }

    void stepAppWindowsAnimation(long currentTime) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).stepAppWindowsAnimation(currentTime);
        }
    }

    void onAppTransitionDone() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).onAppTransitionDone();
        }
    }

    void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    int getOrientation() {
        return getOrientation(this.mOrientation);
    }

    int getOrientation(int candidate) {
        if (!fillsParent()) {
            return -2;
        }
        if (this.mOrientation != -2 && this.mOrientation != -1) {
            return this.mOrientation;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            int i2;
            WindowContainer wc = (WindowContainer) this.mChildren.get(i);
            if (candidate == 3) {
                i2 = 3;
            } else {
                i2 = -2;
            }
            int orientation = wc.getOrientation(i2);
            if (orientation == 3) {
                candidate = orientation;
            } else if (orientation != -2 && (wc.fillsParent() || orientation != -1)) {
                return orientation;
            }
        }
        return candidate;
    }

    boolean fillsParent() {
        return false;
    }

    void switchUser() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).switchUser();
        }
    }

    boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        int i;
        if (traverseTopToBottom) {
            for (i = this.mChildren.size() - 1; i >= 0; i--) {
                if (((WindowContainer) this.mChildren.get(i)).forAllWindows((ToBooleanFunction) callback, traverseTopToBottom)) {
                    return true;
                }
            }
        } else {
            int count = this.mChildren.size();
            for (i = 0; i < count; i++) {
                if (((WindowContainer) this.mChildren.get(i)).forAllWindows((ToBooleanFunction) callback, traverseTopToBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    void forAllWindows(Consumer<WindowState> callback, boolean traverseTopToBottom) {
        ToBooleanFunction wrapper = obtainConsumerWrapper(callback);
        forAllWindows(wrapper, traverseTopToBottom);
        wrapper.release();
    }

    void forAllTasks(Consumer<Task> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowContainer) this.mChildren.get(i)).forAllTasks(callback);
        }
    }

    WindowState getWindow(Predicate<WindowState> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState w = ((WindowContainer) this.mChildren.get(i)).getWindow(callback);
            if (w != null) {
                return w;
            }
        }
        return null;
    }

    public int compareTo(WindowContainer other) {
        int i = 1;
        int i2 = -1;
        if (this == other) {
            return 0;
        }
        WindowList<WindowContainer> list;
        if (this.mParent == null || this.mParent != other.mParent) {
            LinkedList<WindowContainer> thisParentChain = this.mTmpChain1;
            LinkedList<WindowContainer> otherParentChain = this.mTmpChain2;
            try {
                getParents(thisParentChain);
                other.getParents(otherParentChain);
                WindowContainer commonAncestor = null;
                WindowContainer thisTop = (WindowContainer) thisParentChain.peekLast();
                WindowContainer otherTop = (WindowContainer) otherParentChain.peekLast();
                while (thisTop != null && otherTop != null && thisTop == otherTop) {
                    commonAncestor = (WindowContainer) thisParentChain.removeLast();
                    otherParentChain.removeLast();
                    thisTop = (WindowContainer) thisParentChain.peekLast();
                    otherTop = (WindowContainer) otherParentChain.peekLast();
                }
                if (commonAncestor == null) {
                    throw new IllegalArgumentException("No in the same hierarchy this=" + thisParentChain + " other=" + otherParentChain);
                } else if (commonAncestor == this) {
                    return i2;
                } else {
                    if (commonAncestor == other) {
                        this.mTmpChain1.clear();
                        this.mTmpChain2.clear();
                        return 1;
                    }
                    list = commonAncestor.mChildren;
                    if (list.indexOf(thisParentChain.peekLast()) <= list.indexOf(otherParentChain.peekLast())) {
                        i = -1;
                    }
                    this.mTmpChain1.clear();
                    this.mTmpChain2.clear();
                    return i;
                }
            } finally {
                this.mTmpChain1.clear();
                i2 = this.mTmpChain2;
                i2.clear();
            }
        } else {
            list = this.mParent.mChildren;
            if (list.indexOf(this) <= list.indexOf(other)) {
                i = -1;
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

    WindowContainerController getController() {
        return this.mController;
    }

    void setController(WindowContainerController controller) {
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

    void dumpChildrenNames(StringBuilder out, String prefix) {
        String childPrefix = prefix + " ";
        out.append(getName()).append("\n");
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowContainer wc = (WindowContainer) this.mChildren.get(i);
            out.append(childPrefix).append("#").append(i).append(" ");
            wc.dumpChildrenNames(out, childPrefix);
        }
    }

    String getName() {
        return toString();
    }

    int getChildCount() {
        return this.mChildren.size();
    }

    private ForAllWindowsConsumerWrapper obtainConsumerWrapper(Consumer<WindowState> consumer) {
        ForAllWindowsConsumerWrapper wrapper = (ForAllWindowsConsumerWrapper) this.mConsumerWrapperPool.acquire();
        if (wrapper == null) {
            wrapper = new ForAllWindowsConsumerWrapper(this, null);
        }
        wrapper.setConsumer(consumer);
        return wrapper;
    }
}
