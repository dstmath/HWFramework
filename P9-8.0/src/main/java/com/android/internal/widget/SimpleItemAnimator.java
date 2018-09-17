package com.android.internal.widget;

import android.view.View;
import com.android.internal.widget.RecyclerView.ItemAnimator;
import com.android.internal.widget.RecyclerView.ItemAnimator.ItemHolderInfo;
import com.android.internal.widget.RecyclerView.ViewHolder;

public abstract class SimpleItemAnimator extends ItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleItemAnimator";
    boolean mSupportsChangeAnimations = true;

    public abstract boolean animateAdd(ViewHolder viewHolder);

    public abstract boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, int i, int i2, int i3, int i4);

    public abstract boolean animateMove(ViewHolder viewHolder, int i, int i2, int i3, int i4);

    public abstract boolean animateRemove(ViewHolder viewHolder);

    public boolean getSupportsChangeAnimations() {
        return this.mSupportsChangeAnimations;
    }

    public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
        this.mSupportsChangeAnimations = supportsChangeAnimations;
    }

    public boolean canReuseUpdatedViewHolder(ViewHolder viewHolder) {
        return this.mSupportsChangeAnimations ? viewHolder.isInvalid() : true;
    }

    public boolean animateDisappearance(ViewHolder viewHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
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

    public boolean animateAppearance(ViewHolder viewHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
        if (preLayoutInfo == null || (preLayoutInfo.left == postLayoutInfo.left && preLayoutInfo.top == postLayoutInfo.top)) {
            return animateAdd(viewHolder);
        }
        return animateMove(viewHolder, preLayoutInfo.left, preLayoutInfo.top, postLayoutInfo.left, postLayoutInfo.top);
    }

    public boolean animatePersistence(ViewHolder viewHolder, ItemHolderInfo preInfo, ItemHolderInfo postInfo) {
        if (preInfo.left == postInfo.left && preInfo.top == postInfo.top) {
            dispatchMoveFinished(viewHolder);
            return false;
        }
        return animateMove(viewHolder, preInfo.left, preInfo.top, postInfo.left, postInfo.top);
    }

    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, ItemHolderInfo preInfo, ItemHolderInfo postInfo) {
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

    public final void dispatchRemoveFinished(ViewHolder item) {
        onRemoveFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchMoveFinished(ViewHolder item) {
        onMoveFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchAddFinished(ViewHolder item) {
        onAddFinished(item);
        dispatchAnimationFinished(item);
    }

    public final void dispatchChangeFinished(ViewHolder item, boolean oldItem) {
        onChangeFinished(item, oldItem);
        dispatchAnimationFinished(item);
    }

    public final void dispatchRemoveStarting(ViewHolder item) {
        onRemoveStarting(item);
    }

    public final void dispatchMoveStarting(ViewHolder item) {
        onMoveStarting(item);
    }

    public final void dispatchAddStarting(ViewHolder item) {
        onAddStarting(item);
    }

    public final void dispatchChangeStarting(ViewHolder item, boolean oldItem) {
        onChangeStarting(item, oldItem);
    }

    public void onRemoveStarting(ViewHolder item) {
    }

    public void onRemoveFinished(ViewHolder item) {
    }

    public void onAddStarting(ViewHolder item) {
    }

    public void onAddFinished(ViewHolder item) {
    }

    public void onMoveStarting(ViewHolder item) {
    }

    public void onMoveFinished(ViewHolder item) {
    }

    public void onChangeStarting(ViewHolder item, boolean oldItem) {
    }

    public void onChangeFinished(ViewHolder item, boolean oldItem) {
    }
}
