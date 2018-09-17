package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import huawei.android.widget.Attributes.Mode;
import huawei.android.widget.SwipeLayout.OnLayout;
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
    private List<View> mAnimatedViews;
    private final Object[] mAnimationLock;
    private int mDismissAnimationRefCount;
    private SwipeDismissCallback mDismissCallback;
    protected int mOpenPosition;
    protected Set<Integer> mOpenPositions;
    private SortedSet<PendingDismissData> mPendingDismisses;
    protected Set<SwipeLayout> mShownLayouts;
    private Mode mode;
    protected SwipeAdapterInterface swipeAdapterInterface;

    /* renamed from: huawei.android.widget.SwipeItemMangerImpl.1 */
    class AnonymousClass1 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$childView;
        final /* synthetic */ int val$position;
        final /* synthetic */ View val$view;

        AnonymousClass1(View val$view, View val$childView, int val$position) {
            this.val$view = val$view;
            this.val$childView = val$childView;
            this.val$position = val$position;
        }

        public void onAnimationEnd(Animator animation) {
            SwipeItemMangerImpl.this.performDismiss(this.val$view, this.val$childView, this.val$position);
        }
    }

    /* renamed from: huawei.android.widget.SwipeItemMangerImpl.2 */
    class AnonymousClass2 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$dismissView;
        final /* synthetic */ int val$originalLayoutHeight;

        AnonymousClass2(View val$dismissView, int val$originalLayoutHeight) {
            this.val$dismissView = val$dismissView;
            this.val$originalLayoutHeight = val$originalLayoutHeight;
        }

        public void onAnimationEnd(Animator animation) {
            synchronized (SwipeItemMangerImpl.this.mAnimationLock) {
                SwipeItemMangerImpl swipeItemMangerImpl = SwipeItemMangerImpl.this;
                swipeItemMangerImpl.mDismissAnimationRefCount = swipeItemMangerImpl.mDismissAnimationRefCount + SwipeItemMangerImpl.INVALID_POSITION;
                SwipeItemMangerImpl.this.mAnimatedViews.remove(this.val$dismissView);
                boolean noAnimationLeft = SwipeItemMangerImpl.this.mDismissAnimationRefCount == 0;
            }
            if (noAnimationLeft) {
                for (PendingDismissData dismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                    SwipeItemMangerImpl.this.mDismissCallback.onDismiss(dismiss.position);
                }
                for (PendingDismissData pendingDismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                    pendingDismiss.view.setAlpha(1.0f);
                    pendingDismiss.view.setTranslationX(0.0f);
                    LayoutParams lp = pendingDismiss.childView.getLayoutParams();
                    lp.height = this.val$originalLayoutHeight;
                    pendingDismiss.childView.setLayoutParams(lp);
                }
                SwipeItemMangerImpl.this.mPendingDismisses.clear();
            }
        }
    }

    class OnLayoutListener implements OnLayout {
        private int position;

        OnLayoutListener(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
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

        PendingDismissData(int position, View view, View childView) {
            this.position = position;
            this.view = view;
            this.childView = childView;
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

        SwipeMemory(int position) {
            this.position = position;
        }

        public void onClose(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.remove(Integer.valueOf(this.position));
            } else if (this.position == SwipeItemMangerImpl.this.mOpenPosition) {
                SwipeItemMangerImpl.this.mOpenPosition = SwipeItemMangerImpl.INVALID_POSITION;
            }
        }

        public void onStartOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Mode.Single) {
                SwipeItemMangerImpl.this.closeAllExcept(layout);
            }
        }

        public void onOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mode == Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.add(Integer.valueOf(this.position));
                return;
            }
            SwipeItemMangerImpl.this.closeAllExcept(layout);
            SwipeItemMangerImpl.this.mOpenPosition = this.position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    private static class UpdateListener implements AnimatorUpdateListener {
        private View listItemView;
        private LayoutParams lp;

        private UpdateListener(LayoutParams lp, View listItemView) {
            this.lp = lp;
            this.listItemView = listItemView;
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            this.lp.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            this.listItemView.setLayoutParams(this.lp);
        }
    }

    public static class ValueBox {
        OnLayoutListener onLayoutListener;
        int position;
        SwipeMemory swipeMemory;

        ValueBox(int position, SwipeMemory swipeMemory, OnLayoutListener onLayoutListener) {
            this.swipeMemory = swipeMemory;
            this.onLayoutListener = onLayoutListener;
            this.position = position;
        }

        public int getPosition() {
            return this.position;
        }
    }

    public SwipeItemMangerImpl(SwipeAdapterInterface swipeAdapterInterface) {
        this.mode = Mode.Single;
        this.mOpenPosition = INVALID_POSITION;
        this.mOpenPositions = new HashSet();
        this.mShownLayouts = new HashSet();
        this.mAnimationLock = new Object[0];
        this.mAnimatedViews = new LinkedList();
        this.mPendingDismisses = new TreeSet();
        if (swipeAdapterInterface == null) {
            throw new IllegalArgumentException("SwipeAdapterInterface can not be null");
        }
        this.swipeAdapterInterface = swipeAdapterInterface;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.mOpenPositions.clear();
        this.mShownLayouts.clear();
        this.mOpenPosition = INVALID_POSITION;
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
        if (this.mode != Mode.Multiple) {
            this.mOpenPosition = position;
        } else if (!this.mOpenPositions.contains(Integer.valueOf(position))) {
            this.mOpenPositions.add(Integer.valueOf(position));
        }
        this.swipeAdapterInterface.notifyDatasetChanged();
    }

    public void closeItem(int position) {
        if (this.mode == Mode.Multiple) {
            this.mOpenPositions.remove(Integer.valueOf(position));
        } else if (this.mOpenPosition == position) {
            this.mOpenPosition = INVALID_POSITION;
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
        if (this.mode == Mode.Multiple) {
            this.mOpenPositions.clear();
        } else {
            this.mOpenPosition = INVALID_POSITION;
        }
        for (SwipeLayout s : this.mShownLayouts) {
            s.close();
        }
    }

    public void removeShownLayouts(SwipeLayout layout) {
        this.mShownLayouts.remove(layout);
    }

    public List<Integer> getOpenItems() {
        if (this.mode == Mode.Multiple) {
            return new ArrayList(this.mOpenPositions);
        }
        return Collections.singletonList(Integer.valueOf(this.mOpenPosition));
    }

    public List<SwipeLayout> getOpenLayouts() {
        return new ArrayList(this.mShownLayouts);
    }

    public boolean isOpen(int position) {
        if (this.mode == Mode.Multiple) {
            return this.mOpenPositions.contains(Integer.valueOf(position));
        }
        return this.mOpenPosition == position;
    }

    public void setDismissCallback(SwipeDismissCallback dismissCallback) {
        this.mDismissCallback = dismissCallback;
    }

    public void deleteItem(View childView, int position) {
        if (this.mDismissCallback == null) {
            throw new IllegalStateException("SwipeDismissCallback has to be set before deleting items.");
        }
        View surfaceView = null;
        if (childView instanceof SwipeLayout) {
            ((SwipeLayout) childView).close();
            surfaceView = ((SwipeLayout) childView).getSurfaceView();
        }
        if (surfaceView == null) {
            surfaceView = childView;
        }
        slideOutView(surfaceView, childView, position, true);
    }

    private void slideOutView(View view, View childView, int position, boolean toRightSide) {
        synchronized (this.mAnimationLock) {
            if (this.mAnimatedViews.contains(view)) {
                return;
            }
            this.mDismissAnimationRefCount++;
            this.mAnimatedViews.add(view);
            view.animate().translationX((float) (toRightSide ? view.getWidth() : -view.getWidth())).alpha(0.0f).setDuration(200).setListener(new AnonymousClass1(view, childView, position)).start();
        }
    }

    private void performDismiss(View dismissView, View listItemView, int dismissPosition) {
        LayoutParams lp = listItemView.getLayoutParams();
        int originalLayoutHeight = lp.height;
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{listItemView.getHeight(), 1}).setDuration(200);
        animator.addListener(new AnonymousClass2(dismissView, originalLayoutHeight));
        animator.addUpdateListener(new UpdateListener(listItemView, null));
        this.mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView, listItemView));
        animator.start();
    }
}
