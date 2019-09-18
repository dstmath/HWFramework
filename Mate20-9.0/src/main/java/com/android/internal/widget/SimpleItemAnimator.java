package com.android.internal.widget;

import android.view.View;
import com.android.internal.widget.RecyclerView;

public abstract class SimpleItemAnimator extends RecyclerView.ItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleItemAnimator";
    boolean mSupportsChangeAnimations = true;

    public abstract boolean animateAdd(RecyclerView.ViewHolder viewHolder);

    public abstract boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4);

    public abstract boolean animateMove(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4);

    public abstract boolean animateRemove(RecyclerView.ViewHolder viewHolder);

    public boolean getSupportsChangeAnimations() {
        return this.mSupportsChangeAnimations;
    }

    public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
        this.mSupportsChangeAnimations = supportsChangeAnimations;
    }

    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return !this.mSupportsChangeAnimations || viewHolder.isInvalid();
    }

    public boolean animateDisappearance(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preLayoutInfo, RecyclerView.ItemAnimator.ItemHolderInfo postLayoutInfo) {
        int oldLeft = preLayoutInfo.left;
        int oldTop = preLayoutInfo.top;
        View disappearingItemView = viewHolder.itemView;
        int newLeft = postLayoutInfo == null ? disappearingItemView.getLeft() : postLayoutInfo.left;
        int newTop = postLayoutInfo == null ? disappearingItemView.getTop() : postLayoutInfo.top;
        if (viewHolder.isRemoved() || (oldLeft == newLeft && oldTop == newTop)) {
            return animateRemove(viewHolder);
        }
        disappearingItemView.layout(newLeft, newTop, disappearingItemView.getWidth() + newLeft, disappearingItemView.getHeight() + newTop);
        return animateMove(viewHolder, oldLeft, oldTop, newLeft, newTop);
    }

    public boolean animateAppearance(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preLayoutInfo, RecyclerView.ItemAnimator.ItemHolderInfo postLayoutInfo) {
        if (preLayoutInfo == null || (preLayoutInfo.left == postLayoutInfo.left && preLayoutInfo.top == postLayoutInfo.top)) {
            return animateAdd(viewHolder);
        }
        return animateMove(viewHolder, preLayoutInfo.left, preLayoutInfo.top, postLayoutInfo.left, postLayoutInfo.top);
    }

    public boolean animatePersistence(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preInfo, RecyclerView.ItemAnimator.ItemHolderInfo postInfo) {
        if (preInfo.left == postInfo.left && preInfo.top == postInfo.top) {
            dispatchMoveFinished(viewHolder);
            return false;
        }
        return animateMove(viewHolder, preInfo.left, preInfo.top, postInfo.left, postInfo.top);
    }

    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, RecyclerView.ItemAnimator.ItemHolderInfo preInfo, RecyclerView.ItemAnimator.ItemHolderInfo postInfo) {
        int toLeft;
        int toTop;
        int fromLeft = preInfo.left;
        int fromTop = preInfo.top;
        if (newHolder.shouldIgnore()) {
            toLeft = preInfo.left;
            toTop = preInfo.top;
        } else {
            toLeft = postInfo.left;
            toTop = postInfo.top;
        }
        return animateChange(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop);
    }

    public final void dispatchRemoveFinished(RecyclerView.ViewHolder item) {
        onRemoveFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchMoveFinished(RecyclerView.ViewHolder item) {
        onMoveFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchAddFinished(RecyclerView.ViewHolder item) {
        onAddFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchChangeFinished(RecyclerView.ViewHolder item, boolean oldItem) {
        onChangeFinished(item, oldItem);
        dispatchAnimationFinished(item);
    }

    public final void dispatchRemoveStarting(RecyclerView.ViewHolder item) {
        onRemoveStarting(item);
    }

    public final void dispatchMoveStarting(RecyclerView.ViewHolder item) {
        onMoveStarting(item);
    }

    public final void dispatchAddStarting(RecyclerView.ViewHolder item) {
        onAddStarting(item);
    }

    public final void dispatchChangeStarting(RecyclerView.ViewHolder item, boolean oldItem) {
        onChangeStarting(item, oldItem);
    }

    public void onRemoveStarting(RecyclerView.ViewHolder item) {
    }

    public void onRemoveFinished(RecyclerView.ViewHolder item) {
    }

    public void onAddStarting(RecyclerView.ViewHolder item) {
    }

    public void onAddFinished(RecyclerView.ViewHolder item) {
    }

    public void onMoveStarting(RecyclerView.ViewHolder item) {
    }

    public void onMoveFinished(RecyclerView.ViewHolder item) {
    }

    public void onChangeStarting(RecyclerView.ViewHolder item, boolean oldItem) {
    }

    public void onChangeFinished(RecyclerView.ViewHolder item, boolean oldItem) {
    }
}
