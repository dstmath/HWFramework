package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
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
    private static final int LIST_DEFAULT_VAL = 10;
    private static final int SLIDE_OUT_ANIM_DURATION = 200;
    private static final int VALUEBOX_TAG_ID = 50331648;
    private List<View> mAnimatedViews = new LinkedList();
    private final Object[] mAnimationLocks = new Object[0];
    private int mDismissAnimationRefCount;
    private SwipeDismissCallback mDismissCallback;
    private Attributes.Mode mMode = Attributes.Mode.Single;
    protected int mOpenPosition = -1;
    protected Set<Integer> mOpenPositions = new HashSet((int) LIST_DEFAULT_VAL);
    private SortedSet<PendingDismissData> mPendingDismisses = new TreeSet();
    protected SparseArray<SwipeLayout> mShownLayouts = new SparseArray<>();
    protected SwipeAdapterInterface mSwipeAdapterInterface;

    static /* synthetic */ int access$306(SwipeItemMangerImpl x0) {
        int i = x0.mDismissAnimationRefCount - 1;
        x0.mDismissAnimationRefCount = i;
        return i;
    }

    public SwipeItemMangerImpl(SwipeAdapterInterface swipeAdapterInterface) {
        if (swipeAdapterInterface != null) {
            this.mSwipeAdapterInterface = swipeAdapterInterface;
            return;
        }
        throw new IllegalArgumentException("SwipeAdapterInterface can not be null");
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public Attributes.Mode getMode() {
        return this.mMode;
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void setMode(Attributes.Mode mode) {
        this.mMode = mode;
        this.mOpenPositions.clear();
        this.mShownLayouts.clear();
        this.mOpenPosition = -1;
    }

    public void bind(View view, int position) {
        if (view != null) {
            SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(this.mSwipeAdapterInterface.getSwipeLayoutResourceId(position));
            if (swipeLayout != null) {
                ValueBox valueBox = getValueBox(swipeLayout);
                if (valueBox == null) {
                    OnLayoutListener onLayoutListener = new OnLayoutListener(position);
                    SwipeMemory swipeMemory = new SwipeMemory(position);
                    swipeLayout.addSwipeListener(swipeMemory);
                    swipeLayout.addOnLayoutListener(onLayoutListener);
                    swipeLayout.setTag(VALUEBOX_TAG_ID, new ValueBox(position, swipeMemory, onLayoutListener));
                } else {
                    valueBox.mSwipeMemory.setPosition(position);
                    valueBox.mOnLayoutListener.setPosition(position);
                    valueBox.mPosition = position;
                }
                this.mShownLayouts.put(position, swipeLayout);
                return;
            }
            throw new IllegalStateException("can not find SwipeLayout in target view");
        }
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void openItem(int position) {
        if (this.mMode != Attributes.Mode.Multiple) {
            this.mOpenPosition = position;
        } else if (!this.mOpenPositions.contains(Integer.valueOf(position))) {
            this.mOpenPositions.add(Integer.valueOf(position));
        }
        this.mSwipeAdapterInterface.notifyDatasetChanged();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeItem(int position) {
        if (this.mMode == Attributes.Mode.Multiple) {
            this.mOpenPositions.remove(Integer.valueOf(position));
        } else if (this.mOpenPosition == position) {
            this.mOpenPosition = -1;
        }
        this.mSwipeAdapterInterface.notifyDatasetChanged();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeAllExcept(SwipeLayout layout) {
        int size = this.mShownLayouts.size();
        for (int i = 0; i < size; i++) {
            SwipeLayout swipeLayout = this.mShownLayouts.valueAt(i);
            if (swipeLayout != layout) {
                swipeLayout.close();
            }
        }
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeAllItems() {
        if (this.mMode == Attributes.Mode.Multiple) {
            this.mOpenPositions.clear();
        } else {
            this.mOpenPosition = -1;
        }
        int size = this.mShownLayouts.size();
        for (int i = 0; i < size; i++) {
            this.mShownLayouts.valueAt(i).close();
        }
    }

    private ValueBox getValueBox(SwipeLayout layout) {
        Object object = layout.getTag(VALUEBOX_TAG_ID);
        if (object instanceof ValueBox) {
            return (ValueBox) object;
        }
        return null;
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void removeShownLayouts(SwipeLayout layout) {
        ValueBox valueBox;
        if (layout != null && (valueBox = getValueBox(layout)) != null) {
            this.mShownLayouts.remove(valueBox.mPosition);
        }
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public List<Integer> getOpenItems() {
        if (this.mMode == Attributes.Mode.Multiple) {
            return new ArrayList(this.mOpenPositions);
        }
        return Collections.singletonList(Integer.valueOf(this.mOpenPosition));
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public List<SwipeLayout> getOpenLayouts() {
        int size = this.mShownLayouts.size();
        List<SwipeLayout> swipeLayoutList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            swipeLayoutList.add(this.mShownLayouts.valueAt(i));
        }
        return swipeLayoutList;
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public boolean isOpen(int position) {
        if (this.mMode == Attributes.Mode.Multiple) {
            return this.mOpenPositions.contains(Integer.valueOf(position));
        }
        return this.mOpenPosition == position;
    }

    public static class ValueBox {
        OnLayoutListener mOnLayoutListener;
        int mPosition;
        SwipeMemory mSwipeMemory;

        ValueBox(int position, SwipeMemory swipeMemory, OnLayoutListener onLayoutListener) {
            this.mSwipeMemory = swipeMemory;
            this.mOnLayoutListener = onLayoutListener;
            this.mPosition = position;
        }

        public int getPosition() {
            return this.mPosition;
        }
    }

    class OnLayoutListener implements SwipeLayout.OnLayout {
        private int mPosition;

        OnLayoutListener(int position) {
            this.mPosition = position;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override // huawei.android.widget.SwipeLayout.OnLayout
        public void onLayout(SwipeLayout view) {
            if (view != null) {
                if (SwipeItemMangerImpl.this.isOpen(this.mPosition)) {
                    view.open(false, false);
                } else {
                    view.close(false, false);
                }
            }
        }
    }

    public class SwipeMemory extends SimpleSwipeListener {
        private int mPosition;

        SwipeMemory(int position) {
            this.mPosition = position;
        }

        @Override // huawei.android.widget.SimpleSwipeListener, huawei.android.widget.SwipeLayout.SwipeListener
        public void onClose(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mMode == Attributes.Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.remove(Integer.valueOf(this.mPosition));
            } else if (this.mPosition == SwipeItemMangerImpl.this.mOpenPosition) {
                SwipeItemMangerImpl.this.mOpenPosition = -1;
            }
        }

        @Override // huawei.android.widget.SimpleSwipeListener, huawei.android.widget.SwipeLayout.SwipeListener
        public void onStartOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mMode == Attributes.Mode.Single) {
                SwipeItemMangerImpl.this.closeAllExcept(layout);
            }
        }

        @Override // huawei.android.widget.SimpleSwipeListener, huawei.android.widget.SwipeLayout.SwipeListener
        public void onOpen(SwipeLayout layout) {
            if (SwipeItemMangerImpl.this.mMode == Attributes.Mode.Multiple) {
                SwipeItemMangerImpl.this.mOpenPositions.add(Integer.valueOf(this.mPosition));
                return;
            }
            SwipeItemMangerImpl.this.closeAllExcept(layout);
            SwipeItemMangerImpl.this.mOpenPosition = this.mPosition;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }
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

    private void slideOutView(final View view, final View childView, final int position, boolean isToRightSide) {
        synchronized (this.mAnimationLocks) {
            if (!this.mAnimatedViews.contains(view)) {
                this.mDismissAnimationRefCount++;
                this.mAnimatedViews.add(view);
            } else {
                return;
            }
        }
        ViewPropertyAnimator anim = view.animate();
        int width = view.getWidth();
        if (!isToRightSide) {
            width = -width;
        }
        anim.translationX((float) width).alpha(0.0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.SwipeItemMangerImpl.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                SwipeItemMangerImpl.this.performDismiss(view, childView, position);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performDismiss(final View dismissView, View listItemView, int dismissPosition) {
        ViewGroup.LayoutParams params = listItemView.getLayoutParams();
        final int originalLayoutHeight = params.height;
        ValueAnimator animator = ValueAnimator.ofInt(listItemView.getHeight(), 1).setDuration(200L);
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.SwipeItemMangerImpl.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                boolean isAnimationLeft;
                synchronized (SwipeItemMangerImpl.this.mAnimationLocks) {
                    SwipeItemMangerImpl.access$306(SwipeItemMangerImpl.this);
                    SwipeItemMangerImpl.this.mAnimatedViews.remove(dismissView);
                    isAnimationLeft = SwipeItemMangerImpl.this.mDismissAnimationRefCount == 0;
                }
                if (isAnimationLeft) {
                    for (PendingDismissData dismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                        SwipeItemMangerImpl.this.mDismissCallback.onDismiss(dismiss.mPosition);
                    }
                    for (PendingDismissData pendingDismiss : SwipeItemMangerImpl.this.mPendingDismisses) {
                        pendingDismiss.mView.setAlpha(1.0f);
                        pendingDismiss.mView.setTranslationX(0.0f);
                        ViewGroup.LayoutParams params = pendingDismiss.mChildView.getLayoutParams();
                        params.height = originalLayoutHeight;
                        pendingDismiss.mChildView.setLayoutParams(params);
                    }
                    SwipeItemMangerImpl.this.mPendingDismisses.clear();
                }
            }
        });
        animator.addUpdateListener(new UpdateListener(params, listItemView));
        this.mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView, listItemView));
        animator.start();
    }

    /* access modifiers changed from: private */
    public static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private ViewGroup.LayoutParams mLayoutParams;
        private View mListItemView;

        private UpdateListener(ViewGroup.LayoutParams layoutParams, View listItemView) {
            this.mLayoutParams = layoutParams;
            this.mListItemView = listItemView;
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (valueAnimator != null) {
                Object obj = valueAnimator.getAnimatedValue();
                if (obj instanceof Integer) {
                    this.mLayoutParams.height = ((Integer) obj).intValue();
                    this.mListItemView.setLayoutParams(this.mLayoutParams);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PendingDismissData implements Comparable<PendingDismissData> {
        private View mChildView;
        private int mPosition;
        private View mView;

        PendingDismissData(int position, View view, View childView) {
            this.mPosition = position;
            this.mView = view;
            this.mChildView = childView;
        }

        @Override // java.lang.Object
        public int hashCode() {
            return super.hashCode();
        }

        @Override // java.lang.Object
        public boolean equals(Object other) {
            if ((other instanceof PendingDismissData) && compareTo((PendingDismissData) other) == 0) {
                return true;
            }
            return false;
        }

        public int compareTo(PendingDismissData other) {
            return other.mPosition - this.mPosition;
        }
    }
}
