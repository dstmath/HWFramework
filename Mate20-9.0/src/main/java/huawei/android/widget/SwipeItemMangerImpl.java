package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.widget.Attributes;
import huawei.android.widget.SwipeLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SwipeItemMangerImpl implements SwipeItemMangerInterface {
    public static final int INVALID_POSITION = -1;
    /* access modifiers changed from: private */
    public List<View> mAnimatedViews = new LinkedList();
    /* access modifiers changed from: private */
    public final Object[] mAnimationLock = new Object[0];
    /* access modifiers changed from: private */
    public int mDismissAnimationRefCount;
    /* access modifiers changed from: private */
    public SwipeDismissCallback mDismissCallback;
    protected int mOpenPosition = -1;
    protected Set<Integer> mOpenPositions = new HashSet();
    /* access modifiers changed from: private */
    public SortedSet<PendingDismissData> mPendingDismisses = new TreeSet();
    protected Set<SwipeLayout> mShownLayouts = new HashSet();
    /* access modifiers changed from: private */
    public Attributes.Mode mode = Attributes.Mode.Single;
    protected SwipeAdapterInterface swipeAdapterInterface;

    class OnLayoutListener implements SwipeLayout.OnLayout {
        private int position;

        OnLayoutListener(int position2) {
            this.position = position2;
        }

        public void setPosition(int position2) {
            this.position = position2;
        }

        public void onLayout(SwipeLayout v) {
            if (SwipeItemMangerImpl.this.isOpen(this.position)) {
                v.open(false, false);
            } else {
                v.close(false, false);
            }
        }
    }

    private static class PendingDismissData implements Comparable<PendingDismissData> {
        public View childView;
        public int position;
        public View view;

        PendingDismissData(int position2, View view2, View childView2) {
            this.position = position2;
            this.view = view2;
            this.childView = childView2;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof PendingDismissData)) {
                return false;
            }
            if (compareTo((PendingDismissData) other) == 0) {
                z = true;
            }
            return z;
        }

        public int compareTo(PendingDismissData other) {
            return other.position - this.position;
        }
    }

    public class SwipeMemory extends SimpleSwipeListener {
        private int position;

        SwipeMemory(int position2) {
            this.position = position2;
        }

        public void onClose(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Attributes.Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.remove(Integer.valueOf(this.position));
            } else if (this.position == SwipeItemMangerImpl.this.mOpenPosition) {
                SwipeItemMangerImpl.this.mOpenPosition = -1;
            }
        }

        public void onStartOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Attributes.Mode.Single) {
                SwipeItemMangerImpl.this.closeAllExcept(layout);
            }
        }

        public void onOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Attributes.Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.add(Integer.valueOf(this.position));
                return;
            }
            SwipeItemMangerImpl.this.closeAllExcept(layout);
            SwipeItemMangerImpl.this.mOpenPosition = this.position;
        }

        public void setPosition(int position2) {
            this.position = position2;
        }
    }

    private static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private View listItemView;
        private ViewGroup.LayoutParams lp;

        private UpdateListener(ViewGroup.LayoutParams lp2, View listItemView2) {
            this.lp = lp2;
            this.listItemView = listItemView2;
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (valueAnimator.getAnimatedValue() instanceof Integer) {
                this.lp.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                this.listItemView.setLayoutParams(this.lp);
            }
        }
    }

    public static class ValueBox {
        OnLayoutListener onLayoutListener;
        int position;
        SwipeMemory swipeMemory;

        ValueBox(int position2, SwipeMemory swipeMemory2, OnLayoutListener onLayoutListener2) {
            this.swipeMemory = swipeMemory2;
            this.onLayoutListener = onLayoutListener2;
            this.position = position2;
        }

        public int getPosition() {
            return this.position;
        }
    }

    static /* synthetic */ int access$306(SwipeItemMangerImpl x0) {
        int i = x0.mDismissAnimationRefCount - 1;
        x0.mDismissAnimationRefCount = i;
        return i;
    }

    public SwipeItemMangerImpl(SwipeAdapterInterface swipeAdapterInterface2) {
        if (swipeAdapterInterface2 != null) {
            this.swipeAdapterInterface = swipeAdapterInterface2;
            return;
        }
        throw new IllegalArgumentException("SwipeAdapterInterface can not be null");
    }

    public Attributes.Mode getMode() {
        return this.mode;
    }

    public void setMode(Attributes.Mode mode2) {
        this.mode = mode2;
        this.mOpenPositions.clear();
        this.mShownLayouts.clear();
        this.mOpenPosition = -1;
    }

    public void bind(View view, int position) {
        int resId = this.swipeAdapterInterface.getSwipeLayoutResourceId(position);
        SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(resId);
        if (swipeLayout == null) {
            throw new IllegalStateException("can not find SwipeLayout in target view");
        } else if (swipeLayout.getTag(resId) == null) {
            OnLayoutListener onLayoutListener = new OnLayoutListener(position);
            SwipeMemory swipeMemory = new SwipeMemory(position);
            swipeLayout.addSwipeListener(swipeMemory);
            swipeLayout.addOnLayoutListener(onLayoutListener);
            swipeLayout.setTag(resId, new ValueBox(position, swipeMemory, onLayoutListener));
            this.mShownLayouts.add(swipeLayout);
        } else {
            ValueBox valueBox = (ValueBox) swipeLayout.getTag(resId);
            valueBox.swipeMemory.setPosition(position);
            valueBox.onLayoutListener.setPosition(position);
            valueBox.position = position;
        }
    }

    public void openItem(int position) {
        if (this.mode != Attributes.Mode.Multiple) {
            this.mOpenPosition = position;
        } else if (!this.mOpenPositions.contains(Integer.valueOf(position))) {
            this.mOpenPositions.add(Integer.valueOf(position));
        }
        this.swipeAdapterInterface.notifyDatasetChanged();
    }

    public void closeItem(int position) {
        if (this.mode == Attributes.Mode.Multiple) {
            this.mOpenPositions.remove(Integer.valueOf(position));
        } else if (this.mOpenPosition == position) {
            this.mOpenPosition = -1;
        }
        this.swipeAdapterInterface.notifyDatasetChanged();
    }

    public void closeAllExcept(SwipeLayout layout) {
        for (SwipeLayout s : this.mShownLayouts) {
            if (s != layout) {
                s.close();
            }
        }
    }

    public void closeAllItems() {
        if (this.mode == Attributes.Mode.Multiple) {
            this.mOpenPositions.clear();
        } else {
            this.mOpenPosition = -1;
        }
        for (SwipeLayout s : this.mShownLayouts) {
            s.close();
        }
    }

    public void removeShownLayouts(SwipeLayout layout) {
        this.mShownLayouts.remove(layout);
    }

    public List<Integer> getOpenItems() {
        if (this.mode == Attributes.Mode.Multiple) {
            return new ArrayList(this.mOpenPositions);
        }
        return Collections.singletonList(Integer.valueOf(this.mOpenPosition));
    }

    public List<SwipeLayout> getOpenLayouts() {
        return new ArrayList(this.mShownLayouts);
    }

    public boolean isOpen(int position) {
        if (this.mode == Attributes.Mode.Multiple) {
            return this.mOpenPositions.contains(Integer.valueOf(position));
        }
        return this.mOpenPosition == position;
    }

    public void setDismissCallback(SwipeDismissCallback dismissCallback) {
        this.mDismissCallback = dismissCallback;
    }

    public void deleteItem(View childView, int position) {
        if (this.mDismissCallback != null) {
            View surfaceView = null;
            if (childView instanceof SwipeLayout) {
                ((SwipeLayout) childView).close();
                surfaceView = ((SwipeLayout) childView).getSurfaceView();
            }
            if (surfaceView == null) {
                surfaceView = childView;
            }
            slideOutView(surfaceView, childView, position, true);
            return;
        }
        throw new IllegalStateException("SwipeDismissCallback has to be set before deleting items.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        if (r8 == false) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        r1 = r5.getWidth();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        r1 = -r5.getWidth();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
        r0.translationX((float) r1).alpha(0.0f).setDuration(200).setListener(new huawei.android.widget.SwipeItemMangerImpl.AnonymousClass1(r4)).start();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r0 = r5.animate();
     */
    private void slideOutView(final View view, final View childView, final int position, boolean toRightSide) {
        synchronized (this.mAnimationLock) {
            if (!this.mAnimatedViews.contains(view)) {
                this.mDismissAnimationRefCount++;
                this.mAnimatedViews.add(view);
            }
        }
    }

    /* access modifiers changed from: private */
    public void performDismiss(final View dismissView, View listItemView, int dismissPosition) {
        ViewGroup.LayoutParams lp = listItemView.getLayoutParams();
        final int originalLayoutHeight = lp.height;
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{listItemView.getHeight(), 1}).setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                boolean noAnimationLeft;
                synchronized (SwipeItemMangerImpl.this.mAnimationLock) {
                    SwipeItemMangerImpl.access$306(SwipeItemMangerImpl.this);
                    SwipeItemMangerImpl.this.mAnimatedViews.remove(dismissView);
                    noAnimationLeft = SwipeItemMangerImpl.this.mDismissAnimationRefCount == 0;
                }
                if (noAnimationLeft) {
                    for (PendingDismissData dismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                        SwipeItemMangerImpl.this.mDismissCallback.onDismiss(dismiss.position);
                    }
                    for (PendingDismissData pendingDismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                        pendingDismiss.view.setAlpha(1.0f);
                        pendingDismiss.view.setTranslationX(0.0f);
                        ViewGroup.LayoutParams lp = pendingDismiss.childView.getLayoutParams();
                        lp.height = originalLayoutHeight;
                        pendingDismiss.childView.setLayoutParams(lp);
                    }
                    SwipeItemMangerImpl.this.mPendingDismisses.clear();
                }
            }
        });
        animator.addUpdateListener(new UpdateListener(lp, listItemView));
        this.mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView, listItemView));
        animator.start();
    }
}
