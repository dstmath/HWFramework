package huawei.android.widget.appbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.AbsSavedState;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import com.android.hwext.internal.R;
import huawei.android.widget.appbar.Pools;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HwCoordinatorLayout extends ViewGroup implements NestedScrollingParent2 {
    private static final int ALPHA = 255;
    private static final int BUILD_VERSION_SDK_TWENTY_ONE = 21;
    static final Class<?>[] CONSTRUCTOR_PARAMS = {Context.class, AttributeSet.class};
    private static final int DEFAULT_CAPACITY = 12;
    private static final int DOUBLE_RATE = 2;
    static final int EVENT_NESTED_SCROLL = 1;
    static final int EVENT_PRE_DRAW = 0;
    static final int EVENT_VIEW_REMOVED = 2;
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "CoordinatorLayout";
    static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    private static final int TYPE_ON_INTERCEPT = 0;
    private static final int TYPE_ON_TOUCH = 1;
    private static final String WIDGET_PACKAGE_NAME;
    static ThreadLocal<Map<String, Constructor<Behavior>>> sConstructors = new ThreadLocal<>();
    private static Pools.Pool<Rect> sRectPool = new Pools.SynchronizedPool(DEFAULT_CAPACITY);
    private View.OnApplyWindowInsetsListener mApplyWindowInsetsListener;
    private View mBehaviorTouchView;
    private final DirectedAcyclicGraph<View> mChildDags;
    private final List<View> mDependencySortedChildrenList;
    private boolean mIsAttachedToWindow;
    private boolean mIsDisallowInterceptReset;
    private boolean mIsDrawStatusBarBackground;
    private boolean mIsNeedsPreDrawListener;
    boolean mIsPertmitCollapse;
    private int[] mKeyLines;
    private WindowInsets mLastInsets;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View mNestedScrollingTarget;
    ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;
    private OnPreDrawListener mOnPreDrawListener;
    private Paint mScrimPaint;
    private Drawable mStatusBarBackground;
    private final List<View> mTemp1List;
    private final List<View> mTempDependenciesList;
    private final int[] mTempIntPairs;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends Behavior> value();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DispatchChangeEvent {
    }

    static {
        Package packageName = HwCoordinatorLayout.class.getPackage();
        WIDGET_PACKAGE_NAME = packageName != null ? packageName.getName() : null;
        if (Build.VERSION.SDK_INT >= 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = new ViewElevationComparator();
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
        }
    }

    public HwCoordinatorLayout(Context context) {
        this(context, null);
    }

    public HwCoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mIsPertmitCollapse = true;
        this.mDependencySortedChildrenList = new ArrayList((int) DEFAULT_CAPACITY);
        this.mChildDags = new DirectedAcyclicGraph<>();
        this.mTemp1List = new ArrayList((int) DEFAULT_CAPACITY);
        this.mTempDependenciesList = new ArrayList((int) DEFAULT_CAPACITY);
        this.mTempIntPairs = new int[2];
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray array = theme.obtainStyledAttributes(attrs, R.styleable.HwCoordinatorLayout, 34668583, 33947778);
            int keyLineArrayRes = array.getResourceId(0, 0);
            if (keyLineArrayRes != 0) {
                Resources res = context.getResources();
                this.mKeyLines = res.getIntArray(keyLineArrayRes);
                float density = res.getDisplayMetrics().density;
                int count = this.mKeyLines.length;
                for (int i = 0; i < count; i++) {
                    int[] iArr = this.mKeyLines;
                    iArr[i] = (int) (((float) iArr[i]) * density);
                }
            }
            this.mStatusBarBackground = array.getDrawable(1);
            array.recycle();
        }
        setupForInsets();
        super.setOnHierarchyChangeListener(new HierarchyChangeListener());
    }

    private static Rect acquireTempRect() {
        Rect rect = sRectPool.acquire();
        if (rect == null) {
            return new Rect();
        }
        return rect;
    }

    private static void releaseTempRect(Rect rect) {
        rect.setEmpty();
        sRectPool.release(rect);
    }

    @Override // android.view.ViewGroup
    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener) {
        this.mOnHierarchyChangeListener = onHierarchyChangeListener;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetTouchBehaviors();
        if (this.mIsNeedsPreDrawListener) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mLastInsets == null && getFitsSystemWindows()) {
            requestApplyInsets();
        }
        this.mIsAttachedToWindow = true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetTouchBehaviors();
        if (this.mIsNeedsPreDrawListener && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        View view = this.mNestedScrollingTarget;
        if (view != null) {
            onStopNestedScroll(view);
        }
        this.mIsAttachedToWindow = false;
    }

    public void setStatusBarBackground(Drawable drawable) {
        Drawable drawable2 = this.mStatusBarBackground;
        if (drawable2 != drawable) {
            Drawable drawable3 = null;
            if (drawable2 != null) {
                drawable2.setCallback(null);
            }
            if (drawable != null) {
                drawable3 = drawable.mutate();
            }
            this.mStatusBarBackground = drawable3;
            Drawable drawable4 = this.mStatusBarBackground;
            if (drawable4 != null) {
                if (drawable4.isStateful()) {
                    this.mStatusBarBackground.setState(getDrawableState());
                }
                this.mStatusBarBackground.setLayoutDirection(getLayoutDirection());
                this.mStatusBarBackground.setVisible(getVisibility() == 0, false);
                this.mStatusBarBackground.setCallback(this);
            }
            postInvalidateOnAnimation();
        }
    }

    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] states = getDrawableState();
        boolean isChanged = false;
        Drawable background = this.mStatusBarBackground;
        if (background != null && background.isStateful()) {
            isChanged = false | background.setState(states);
        }
        if (isChanged) {
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mStatusBarBackground;
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean isVisible = visibility == 0;
        Drawable drawable = this.mStatusBarBackground;
        if (drawable != null && drawable.isVisible() != isVisible) {
            this.mStatusBarBackground.setVisible(isVisible, false);
        }
    }

    public void setStatusBarBackgroundResource(int resId) {
        setStatusBarBackground(resId != 0 ? getContext().getResources().getDrawable(resId) : null);
    }

    public void setStatusBarBackgroundColor(int color) {
        setStatusBarBackground(new ColorDrawable(color));
    }

    /* access modifiers changed from: package-private */
    public final WindowInsets setWindowInsets(WindowInsets insets) {
        if (Objects.equals(this.mLastInsets, insets)) {
            return insets;
        }
        this.mLastInsets = insets;
        boolean z = true;
        this.mIsDrawStatusBarBackground = insets != null && insets.getSystemWindowInsetTop() > 0;
        if (this.mIsDrawStatusBarBackground || getBackground() != null) {
            z = false;
        }
        setWillNotDraw(z);
        WindowInsets realInsets = dispatchApplyWindowInsetsToBehaviors(insets);
        requestLayout();
        return realInsets;
    }

    /* access modifiers changed from: package-private */
    public final WindowInsets getLastWindowInsets() {
        return this.mLastInsets;
    }

    private void resetTouchBehaviors() {
        View view = this.mBehaviorTouchView;
        if (view != null) {
            Behavior behavior = ((LayoutParams) view.getLayoutParams()).getBehavior();
            if (behavior != null) {
                long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                behavior.onTouchEvent(this, this.mBehaviorTouchView, cancelEvent);
                cancelEvent.recycle();
            }
            this.mBehaviorTouchView = null;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getLayoutParams() instanceof LayoutParams) {
                ((LayoutParams) child.getLayoutParams()).resetTouchBehaviorTracking();
            }
        }
        this.mIsDisallowInterceptReset = false;
    }

    private void getTopSortedChildren(List<View> outViewList) {
        outViewList.clear();
        boolean isUseCustomOrder = isChildrenDrawingOrderEnabled();
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            outViewList.add(getChildAt(isUseCustomOrder ? getChildDrawingOrder(childCount, i) : i));
        }
        Comparator<View> comparator = TOP_SORTED_CHILDREN_COMPARATOR;
        if (comparator != null) {
            Collections.sort(outViewList, comparator);
        }
    }

    private boolean performIntercept(MotionEvent event, int type, MotionEvent cancelEvent, View child, Behavior behavior) {
        boolean isIntercepted = false;
        MotionEvent motionEvent = behavior != null ? event : cancelEvent;
        boolean isHandled = false;
        if (type == 0) {
            isHandled = behavior.onInterceptTouchEvent(this, child, motionEvent);
        } else if (type == 1) {
            isHandled = behavior.onTouchEvent(this, child, motionEvent);
        }
        if (behavior != null && ((isIntercepted = isHandled))) {
            this.mBehaviorTouchView = child;
        }
        return isIntercepted;
    }

    private boolean performIntercept(MotionEvent event, int type) {
        MotionEvent cancelEvent;
        boolean isBlocking;
        MotionEvent cancelEvent2 = null;
        List<View> topmostChildList = this.mTemp1List;
        getTopSortedChildren(topmostChildList);
        Iterator<View> it = topmostChildList.iterator();
        boolean isIntercepted = false;
        boolean isNewBlock = false;
        while (true) {
            if (!it.hasNext()) {
                cancelEvent = cancelEvent2;
                break;
            }
            View child = it.next();
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                Behavior behavior = params.getBehavior();
                if (isIntercepted || (isNewBlock && event.getActionMasked() != 0)) {
                    if (behavior != null) {
                        if (cancelEvent2 == null) {
                            long now = SystemClock.uptimeMillis();
                            cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                            isIntercepted = performIntercept(event, type, cancelEvent, child, behavior);
                            boolean isBlocked = params.didBlockInteraction();
                            isBlocking = params.isBlockingInteractionBelow(this, child);
                            isNewBlock = !isBlocking && !isBlocked;
                            if (!isBlocking && !isNewBlock) {
                                break;
                            }
                            cancelEvent2 = cancelEvent;
                        }
                    } else {
                        continue;
                    }
                }
                cancelEvent = cancelEvent2;
                isIntercepted = performIntercept(event, type, cancelEvent, child, behavior);
                boolean isBlocked2 = params.didBlockInteraction();
                isBlocking = params.isBlockingInteractionBelow(this, child);
                isNewBlock = !isBlocking && !isBlocked2;
                if (!isBlocking) {
                }
                cancelEvent2 = cancelEvent;
            }
        }
        if (cancelEvent != null) {
            cancelEvent.recycle();
        }
        topmostChildList.clear();
        return isIntercepted;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            resetTouchBehaviors();
        }
        boolean isIntercepted = performIntercept(event, 0);
        if (action == 1 || action == 3) {
            resetTouchBehaviors();
        }
        return isIntercepted;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        Behavior behavior;
        boolean isHandled = false;
        MotionEvent cancelEvent = null;
        if (!this.mIsPertmitCollapse) {
            return false;
        }
        int action = event.getActionMasked();
        boolean isCancelSuper = performIntercept(event, 1);
        View view = this.mBehaviorTouchView;
        if (!(view == null || !(view.getLayoutParams() instanceof LayoutParams) || (behavior = ((LayoutParams) this.mBehaviorTouchView.getLayoutParams()).getBehavior()) == null)) {
            isHandled = behavior.onTouchEvent(this, this.mBehaviorTouchView, event);
        }
        if (this.mBehaviorTouchView == null) {
            isHandled |= super.onTouchEvent(event);
        } else if (isCancelSuper) {
            long now = SystemClock.uptimeMillis();
            cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
            super.onTouchEvent(cancelEvent);
        }
        if (cancelEvent != null) {
            cancelEvent.recycle();
        }
        if (action == 1 || action == 3) {
            resetTouchBehaviors();
        }
        return isHandled;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean isDisallowIntercept) {
        super.requestDisallowInterceptTouchEvent(isDisallowIntercept);
        if (isDisallowIntercept && !this.mIsDisallowInterceptReset) {
            resetTouchBehaviors();
            this.mIsDisallowInterceptReset = true;
        }
    }

    private int getKeyLine(int index) {
        int[] iArr = this.mKeyLines;
        if (iArr == null) {
            Log.e(TAG, "No keyLines defined for " + this + " - attempted index lookup " + index);
            return 0;
        } else if (index >= 0 && index < iArr.length) {
            return iArr[index];
        } else {
            Log.e(TAG, "KeyLine index " + index + " out of range for " + this);
            return 0;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v3, resolved type: java.lang.Class<?> */
    /* JADX WARN: Multi-variable type inference failed */
    static Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
        String fullName;
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (name.startsWith(".")) {
            fullName = context.getPackageName() + name;
        } else if (name.indexOf(46) >= 0) {
            fullName = name;
        } else if (!TextUtils.isEmpty(WIDGET_PACKAGE_NAME)) {
            fullName = WIDGET_PACKAGE_NAME + '.' + name;
        } else {
            fullName = name;
        }
        try {
            Map<String, Constructor<Behavior>> constructors = sConstructors.get();
            if (constructors == null) {
                constructors = new HashMap((int) DEFAULT_CAPACITY);
                sConstructors.set(constructors);
            }
            Constructor<Behavior> constructor = constructors.get(fullName);
            if (constructor == null) {
                constructor = Class.forName(fullName, true, context.getClassLoader()).getConstructor(CONSTRUCTOR_PARAMS);
                constructor.setAccessible(true);
                constructors.put(fullName, constructor);
            }
            return constructor.newInstance(context, attrs);
        } catch (InstantiationException e) {
            Log.e(TAG, "instantiation error");
            return null;
        } catch (InvocationTargetException e2) {
            Log.e(TAG, "invocation target error");
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "no found method error");
            return null;
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "illegal access error");
            return null;
        } catch (ClassNotFoundException e5) {
            Log.e(TAG, "class not found error");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public LayoutParams getResolvedLayoutParams(View child) {
        if (!(child.getLayoutParams() instanceof LayoutParams)) {
            return new LayoutParams(child.getLayoutParams());
        }
        LayoutParams result = (LayoutParams) child.getLayoutParams();
        if (result.mIsBehaviorResolved) {
            return result;
        }
        Class<?> childClass = child.getClass();
        DefaultBehavior defaultBehavior = (DefaultBehavior) childClass.getAnnotation(DefaultBehavior.class);
        while (childClass != null && defaultBehavior == null) {
            defaultBehavior = (DefaultBehavior) childClass.getAnnotation(DefaultBehavior.class);
            childClass = childClass.getSuperclass();
        }
        if (defaultBehavior != null) {
            try {
                result.setBehavior((Behavior) defaultBehavior.value().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "createDefaultBehaviorInstance: no constructor");
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "createDefaultBehaviorInstance: IllegalAccessException");
            } catch (InstantiationException e3) {
                Log.e(TAG, "createDefaultBehaviorInstance: InstantiationException");
            } catch (InvocationTargetException e4) {
                Log.e(TAG, "createDefaultBehaviorInstance: InvocationTargetException");
            }
        }
        result.mIsBehaviorResolved = true;
        return result;
    }

    private void prepareChildren() {
        this.mDependencySortedChildrenList.clear();
        this.mChildDags.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams params = getResolvedLayoutParams(view);
            params.findAnchorView(this, view);
            this.mChildDags.addNode(view);
            for (int j = 0; j < childCount; j++) {
                if (j != i) {
                    View other = getChildAt(j);
                    if (params.dependsOn(this, view, other)) {
                        if (!this.mChildDags.contains(other)) {
                            this.mChildDags.addNode(other);
                        }
                        this.mChildDags.addEdge(other, view);
                    }
                }
            }
        }
        this.mDependencySortedChildrenList.addAll(this.mChildDags.getSortedList());
        Collections.reverse(this.mDependencySortedChildrenList);
    }

    /* access modifiers changed from: package-private */
    public void getDescendantRect(View descendant, Rect out) {
        ViewGroupUtils.getDescendantRect(this, descendant, out);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getSuggestedMinimumWidth() {
        return MathUtils.max(super.getSuggestedMinimumWidth(), getPaddingLeft() + getPaddingRight());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getSuggestedMinimumHeight() {
        return MathUtils.max(super.getSuggestedMinimumHeight(), getPaddingTop() + getPaddingBottom());
    }

    public void onMeasureChild(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private void measureChild(int widthMeasureSpec, int heightMeasureSpec, View child) {
        int keyLineWidthUsed;
        int childHeightMeasureSpec;
        int childWidthMeasureSpec;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        boolean isApplyInsets = true;
        boolean z = getLayoutDirection() == 1;
        if (this.mLastInsets == null || !getFitsSystemWindows()) {
            isApplyInsets = false;
        }
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.mKeyline < 0 || widthMode == 0) {
                keyLineWidthUsed = 0;
            } else {
                int keyLinePos = getKeyLine(params.mKeyline);
                int keyLineGravity = (resolveKeyLineGravity(params.mGravity) & 7) | 8388608;
                if (keyLineGravity == 8388611) {
                    keyLineWidthUsed = MathUtils.max(0, (widthSize - getPaddingRight()) - keyLinePos);
                } else if (keyLineGravity == 8388613) {
                    keyLineWidthUsed = MathUtils.max(0, keyLinePos - getPaddingLeft());
                } else {
                    keyLineWidthUsed = 0;
                }
            }
            if (!isApplyInsets || child.getFitsSystemWindows()) {
                childWidthMeasureSpec = widthMeasureSpec;
                childHeightMeasureSpec = heightMeasureSpec;
            } else {
                int horizontalInsets = this.mLastInsets.getSystemWindowInsetLeft() + this.mLastInsets.getSystemWindowInsetRight();
                int verticalInsets = this.mLastInsets.getSystemWindowInsetTop() + this.mLastInsets.getSystemWindowInsetBottom();
                childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize - horizontalInsets, widthMode);
                childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightSize - verticalInsets, heightMode);
            }
            Behavior behavior = params.getBehavior();
            if (behavior == null || !behavior.onMeasureChild(this, child, childWidthMeasureSpec, keyLineWidthUsed, childHeightMeasureSpec, 0)) {
                onMeasureChild(child, childWidthMeasureSpec, keyLineWidthUsed, childHeightMeasureSpec, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        prepareChildren();
        ensurePreDrawListener();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int widthPadding = paddingLeft + getPaddingRight();
        int heightPadding = paddingTop + getPaddingBottom();
        int widthUsed = getSuggestedMinimumWidth();
        int heightUsed = getSuggestedMinimumHeight();
        int childState = 0;
        for (View child : this.mDependencySortedChildrenList) {
            if (child.getVisibility() != 8 && (child.getLayoutParams() instanceof LayoutParams)) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                measureChild(widthMeasureSpec, heightMeasureSpec, child);
                widthUsed = MathUtils.max(widthUsed, child.getMeasuredWidth() + widthPadding + params.leftMargin + params.rightMargin);
                heightUsed = MathUtils.max(heightUsed, child.getMeasuredHeight() + heightPadding + params.topMargin + params.bottomMargin);
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
                paddingLeft = paddingLeft;
            }
        }
        setMeasuredDimension(View.resolveSizeAndState(widthUsed, widthMeasureSpec, -16777216 & childState), View.resolveSizeAndState(heightUsed, heightMeasureSpec, childState << 16));
    }

    private WindowInsets dispatchApplyWindowInsetsToBehaviors(WindowInsets insets) {
        Behavior behavior;
        WindowInsets myInsets = insets;
        if (myInsets.isConsumed()) {
            return myInsets;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getFitsSystemWindows() && (child.getLayoutParams() instanceof LayoutParams) && (behavior = ((LayoutParams) child.getLayoutParams()).getBehavior()) != null) {
                myInsets = behavior.onApplyWindowInsets(this, child, myInsets);
                if (myInsets.isConsumed()) {
                    break;
                }
            }
        }
        return myInsets;
    }

    public void onLayoutChild(View child, int layoutDirection) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.checkAnchorChanged()) {
                throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
            } else if (params.mAnchorView != null) {
                layoutChildWithAnchor(child, params.mAnchorView, layoutDirection);
            } else if (params.mKeyline >= 0) {
                layoutChildWithKeyLine(child, params.mKeyline, layoutDirection);
            } else {
                layoutChild(child, layoutDirection);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        int layoutDirection = getLayoutDirection();
        for (View child : this.mDependencySortedChildrenList) {
            if (child.getVisibility() != 8 && (child.getLayoutParams() instanceof LayoutParams)) {
                Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
                if (behavior == null || !behavior.onLayoutChild(this, child, layoutDirection)) {
                    onLayoutChild(child, layoutDirection);
                }
            }
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIsDrawStatusBarBackground && this.mStatusBarBackground != null) {
            WindowInsets windowInsets = this.mLastInsets;
            int inset = windowInsets != null ? windowInsets.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                this.mStatusBarBackground.draw(canvas);
            }
        }
    }

    @Override // android.view.View
    public void setFitsSystemWindows(boolean isFitSystemWindows) {
        super.setFitsSystemWindows(isFitSystemWindows);
        setupForInsets();
    }

    /* access modifiers changed from: package-private */
    public void recordLastChildRect(View child, Rect rect) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            ((LayoutParams) child.getLayoutParams()).setLastChildRect(rect);
        }
    }

    /* access modifiers changed from: package-private */
    public void getLastChildRect(View child, Rect out) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            out.set(((LayoutParams) child.getLayoutParams()).getLastChildRect());
        }
    }

    /* access modifiers changed from: package-private */
    public void getChildRect(View child, boolean isTransform, Rect out) {
        if (child.isLayoutRequested() || child.getVisibility() == 8) {
            out.setEmpty();
        } else if (isTransform) {
            getDescendantRect(child, out);
        } else {
            out.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
    }

    private void getDesiredAnchoredChildRectWithoutConstraints(int layoutDirection, Rect anchorRect, Rect out, LayoutParams params, Size childSize) {
        int absGravity = resolveAnchoredChildGravity(params.mGravity);
        int absAnchorGravity = resolveGravity(params.mAnchorGravity);
        int left = getLeft(8388608 | (absAnchorGravity & 7), anchorRect, childSize.getWidth(), (absGravity & 7) | 8388608);
        int top = getTop(absAnchorGravity & 112, anchorRect, absGravity & 112, childSize.getHeight());
        out.set(left, top, childSize.getWidth() + left, childSize.getHeight() + top);
    }

    private int getTop(int anchorGravityV, Rect anchorRect, int gravityV, int childHeight) {
        int top;
        if (anchorGravityV == 16) {
            top = anchorRect.top + (anchorRect.height() / 2);
        } else if (anchorGravityV == 48) {
            top = anchorRect.top;
        } else if (anchorGravityV != 80) {
            top = anchorRect.top;
        } else {
            top = anchorRect.bottom;
        }
        if (gravityV == 16) {
            return top - (childHeight / 2);
        }
        if (gravityV == 48) {
            return top - childHeight;
        }
        if (gravityV != 80) {
            return top - childHeight;
        }
        return top;
    }

    private int getLeft(int anchorGravityH, Rect anchorRect, int childWidth, int gravityH) {
        int left;
        boolean isRtl = true;
        int i = 0;
        if (getLayoutDirection() != 1) {
            isRtl = false;
        }
        if (anchorGravityH == 8388609) {
            left = anchorRect.left + (anchorRect.width() / 2);
        } else if (anchorGravityH != 8388611) {
            left = anchorGravityH != 8388613 ? anchorRect.left : isRtl ? anchorRect.left : anchorRect.right;
        } else {
            left = isRtl ? anchorRect.right : anchorRect.left;
        }
        if (gravityH == 8388609) {
            return left - (childWidth / 2);
        }
        if (gravityH == 8388611) {
            if (!isRtl) {
                i = childWidth;
            }
            return left - i;
        } else if (gravityH != 8388613) {
            return left - childWidth;
        } else {
            if (isRtl) {
                i = childWidth;
            }
            return left - i;
        }
    }

    private void constrainChildRect(LayoutParams params, Rect out, int childWidth, int childHeight) {
        int width = getWidth();
        int height = getHeight();
        int left = MathUtils.max(getPaddingLeft() + params.leftMargin, MathUtils.min(out.left, ((width - getPaddingRight()) - childWidth) - params.rightMargin));
        int top = MathUtils.max(getPaddingTop() + params.topMargin, MathUtils.min(out.top, ((height - getPaddingBottom()) - childHeight) - params.bottomMargin));
        out.set(left, top, left + childWidth, top + childHeight);
    }

    /* access modifiers changed from: package-private */
    public void getDesiredAnchoredChildRect(View child, int layoutDirection, Rect anchorRect, Rect out) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            getDesiredAnchoredChildRectWithoutConstraints(layoutDirection, anchorRect, out, params, new Size(childWidth, childHeight));
            constrainChildRect(params, out, childWidth, childHeight);
        }
    }

    private void layoutChildWithAnchor(View child, View anchor, int layoutDirection) {
        Rect anchorRect = acquireTempRect();
        Rect childRect = acquireTempRect();
        try {
            getDescendantRect(anchor, anchorRect);
            getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, childRect);
            child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
        } finally {
            releaseTempRect(anchorRect);
            releaseTempRect(childRect);
        }
    }

    private void layoutChildWithKeyLine(View child, int keyLine, int layoutDirection) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int absGravity = resolveKeyLineGravity(params.mGravity);
            int gravityH = (absGravity & 7) | 8388608;
            int gravityV = absGravity & 112;
            int width = getWidth();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int myKeyLine = keyLine;
            boolean isRtl = true;
            int i = 0;
            if (layoutDirection != 1) {
                isRtl = false;
            }
            if (isRtl) {
                myKeyLine = width - myKeyLine;
            }
            int left = getKeyLine(myKeyLine) - childWidth;
            int top = 0;
            if (gravityH == 8388609) {
                left += childWidth / 2;
            } else if (gravityH == 8388611) {
                if (isRtl) {
                    i = childWidth;
                }
                left += i;
            } else if (gravityH == 8388613) {
                if (!isRtl) {
                    i = childWidth;
                }
                left += i;
            }
            if (gravityV == 16) {
                top = 0 + (childHeight / 2);
            } else if (gravityV != 48 && gravityV == 80) {
                top = 0 + childHeight;
            }
            int left2 = MathUtils.max(getPaddingLeft() + params.leftMargin, MathUtils.min(left, ((width - getPaddingRight()) - childWidth) - params.rightMargin));
            int top2 = MathUtils.max(getPaddingTop() + params.topMargin, MathUtils.min(top, ((getHeight() - getPaddingBottom()) - childHeight) - params.bottomMargin));
            child.layout(left2, top2, left2 + childWidth, top2 + childHeight);
        }
    }

    private void layoutChild(View child, int layoutDirection) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            Rect parent = acquireTempRect();
            parent.set(getPaddingLeft() + params.leftMargin, getPaddingTop() + params.topMargin, (getWidth() - getPaddingRight()) - params.rightMargin, (getHeight() - getPaddingBottom()) - params.bottomMargin);
            if (this.mLastInsets != null && getFitsSystemWindows() && !child.getFitsSystemWindows()) {
                parent.left += this.mLastInsets.getSystemWindowInsetLeft();
                parent.top += this.mLastInsets.getSystemWindowInsetTop();
                parent.right -= this.mLastInsets.getSystemWindowInsetRight();
                parent.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
            }
            Rect out = acquireTempRect();
            Gravity.apply(resolveGravity(params.mGravity), child.getMeasuredWidth(), child.getMeasuredHeight(), parent, out, layoutDirection);
            child.layout(out.left, out.top, out.right, out.bottom);
            releaseTempRect(parent);
            releaseTempRect(out);
        }
    }

    private static int resolveGravity(int gravity) {
        int myGravity = gravity;
        if ((myGravity & 7) == 0) {
            myGravity |= 8388611;
        }
        if ((myGravity & 112) == 0) {
            return myGravity | 48;
        }
        return myGravity;
    }

    private static int resolveKeyLineGravity(int gravity) {
        if (gravity == 0) {
            return 8388661;
        }
        return gravity;
    }

    private static int resolveAnchoredChildGravity(int gravity) {
        if (gravity == 0) {
            return 17;
        }
        return gravity;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (!(child.getLayoutParams() instanceof LayoutParams)) {
            return super.drawChild(canvas, child, drawingTime);
        }
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        if (params.mBehavior != null) {
            float scrimAlpha = params.mBehavior.getScrimOpacity(this, child);
            if (scrimAlpha > 0.0f) {
                if (this.mScrimPaint == null) {
                    this.mScrimPaint = new Paint();
                }
                this.mScrimPaint.setColor(params.mBehavior.getScrimColor(this, child));
                this.mScrimPaint.setAlpha(MathUtils.clamp(Math.round(255.0f * scrimAlpha), 0, (int) ALPHA));
                int saved = canvas.save();
                if (child.isOpaque()) {
                    canvas.clipRect((float) child.getLeft(), (float) child.getTop(), (float) child.getRight(), (float) child.getBottom(), Region.Op.DIFFERENCE);
                }
                canvas.drawRect((float) getPaddingLeft(), (float) getPaddingTop(), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - getPaddingBottom()), this.mScrimPaint);
                canvas.restoreToCount(saved);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /* access modifiers changed from: package-private */
    public final void onChildViewsChanged(int type) {
        int layoutDirection = getLayoutDirection();
        int childCount = this.mDependencySortedChildrenList.size();
        Rect inset = acquireTempRect();
        Rect drawRect = acquireTempRect();
        Rect lastDrawRect = acquireTempRect();
        for (int i = 0; i < childCount; i++) {
            View child = this.mDependencySortedChildrenList.get(i);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                if (type != 0 || child.getVisibility() != 8) {
                    for (int j = 0; j < i; j++) {
                        if (params.mAnchorDirectChild == this.mDependencySortedChildrenList.get(j)) {
                            offsetChildToAnchor(child, layoutDirection);
                        }
                    }
                    getChildRect(child, true, drawRect);
                    if (params.mInsetEdge != 0 && !drawRect.isEmpty()) {
                        accumulateInset(params, drawRect, inset);
                    }
                    if (params.mDodgeInsetEdges != 0 && child.getVisibility() == 0) {
                        offsetChildByInset(child, inset, layoutDirection);
                    }
                    if (type != 2) {
                        getLastChildRect(child, lastDrawRect);
                        if (!lastDrawRect.equals(drawRect)) {
                            recordLastChildRect(child, drawRect);
                        }
                    }
                    handleChildBehavior(i, type, childCount, child);
                }
            }
        }
        releaseTempRect(inset);
        releaseTempRect(drawRect);
        releaseTempRect(lastDrawRect);
    }

    private void accumulateInset(LayoutParams params, Rect drawRect, Rect inset) {
        int absInsetEdge = params.mInsetEdge;
        int i = absInsetEdge & 112;
        if (i == 48) {
            inset.top = MathUtils.max(inset.top, drawRect.bottom);
        } else if (i == 80) {
            inset.bottom = MathUtils.max(inset.bottom, getHeight() - drawRect.top);
        }
        boolean isRtl = true;
        if (getLayoutDirection() != 1) {
            isRtl = false;
        }
        int i2 = (absInsetEdge & 7) | 8388608;
        if (i2 != 8388611) {
            if (i2 == 8388613) {
                if (isRtl) {
                    inset.left = MathUtils.max(inset.left, drawRect.right);
                } else {
                    inset.right = MathUtils.max(inset.right, getWidth() - drawRect.left);
                }
            }
        } else if (isRtl) {
            inset.right = MathUtils.max(inset.right, getWidth() - drawRect.left);
        } else {
            inset.left = MathUtils.max(inset.left, drawRect.right);
        }
    }

    private void handleChildBehavior(int i, int type, int childCount, View child) {
        LayoutParams params;
        Behavior behavior;
        boolean isHandled;
        for (int j = i + 1; j < childCount; j++) {
            View checkChild = this.mDependencySortedChildrenList.get(j);
            if ((checkChild.getLayoutParams() instanceof LayoutParams) && (behavior = (params = (LayoutParams) checkChild.getLayoutParams()).getBehavior()) != null && behavior.layoutDependsOn(this, checkChild, child)) {
                if (type != 0 || !params.getChangedAfterNestedScroll()) {
                    if (type == 2) {
                        behavior.onDependentViewRemoved(this, checkChild, child);
                        isHandled = true;
                    } else {
                        isHandled = behavior.onDependentViewChanged(this, checkChild, child);
                    }
                    if (type == 1) {
                        params.setChangedAfterNestedScroll(isHandled);
                    }
                } else {
                    params.resetChangedAfterNestedScroll();
                }
            }
        }
    }

    private void offsetChildByInset(View child, Rect inset, int layoutDirection) {
        boolean isOffsetY;
        int distance;
        int distance2;
        if (child.isLaidOut() && child.getWidth() > 0 && child.getHeight() > 0 && (child.getLayoutParams() instanceof LayoutParams)) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            Behavior behavior = params.getBehavior();
            Rect dodgeRect = acquireTempRect();
            Rect bounds = acquireTempRect();
            bounds.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if (behavior == null || !behavior.getInsetDodgeRect(this, child, dodgeRect)) {
                dodgeRect.set(bounds);
            } else if (!bounds.contains(dodgeRect)) {
                throw new IllegalArgumentException("Rect should be within the child's bounds. Rect:" + dodgeRect.toShortString() + " | Bounds:" + bounds.toShortString());
            }
            releaseTempRect(bounds);
            if (dodgeRect.isEmpty()) {
                releaseTempRect(dodgeRect);
                return;
            }
            int absDodgeInsetEdges = Gravity.getAbsoluteGravity(params.mDodgeInsetEdges, layoutDirection);
            boolean isOffsetY2 = false;
            if ((absDodgeInsetEdges & 48) == 48 && (distance2 = (dodgeRect.top - params.topMargin) - params.mInsetOffsetY) < inset.top) {
                setInsetOffsetY(child, inset.top - distance2);
                isOffsetY2 = true;
            }
            if ((absDodgeInsetEdges & 80) != 80 || (distance = ((getHeight() - dodgeRect.bottom) - params.bottomMargin) + params.mInsetOffsetY) >= inset.bottom) {
                isOffsetY = isOffsetY2;
            } else {
                setInsetOffsetY(child, distance - inset.bottom);
                isOffsetY = true;
            }
            if (!isOffsetY) {
                setInsetOffsetY(child, 0);
            }
            setInsetOffset(absDodgeInsetEdges, dodgeRect, params, inset, child);
            releaseTempRect(dodgeRect);
        }
    }

    private void setInsetOffset(int absDodgeInsetEdges, Rect dodgeRect, LayoutParams params, Rect inset, View child) {
        int distance;
        int distance2;
        boolean isOffsetX = false;
        boolean isRight = true;
        boolean isRtl = getLayoutDirection() == 1;
        int gravity = 8388608 | absDodgeInsetEdges;
        if (((isRtl && (gravity & 8388613) == 8388613) || (!isRtl && (gravity & 8388611) == 8388611)) && (distance2 = (dodgeRect.left - params.leftMargin) - params.mInsetOffsetX) < inset.left) {
            setInsetOffsetX(child, inset.left - distance2);
            isOffsetX = true;
        }
        if (!isRtl || (gravity & 8388611) != 8388611) {
            isRight = false;
        }
        if ((isRight || (!isRtl && (gravity & 8388613) == 8388613)) && (distance = ((getWidth() - dodgeRect.right) - params.rightMargin) + params.mInsetOffsetX) < inset.right) {
            setInsetOffsetX(child, distance - inset.right);
            isOffsetX = true;
        }
        if (!isOffsetX) {
            setInsetOffsetX(child, 0);
        }
    }

    private void setInsetOffsetX(View child, int offsetX) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (offsetX != params.mInsetOffsetX) {
                ViewOffsetHelper.offsetLeftAndRight(child, offsetX - params.mInsetOffsetX);
                params.mInsetOffsetX = offsetX;
            }
        }
    }

    private void setInsetOffsetY(View child, int offsetY) {
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.mInsetOffsetY != offsetY) {
                ViewOffsetHelper.offsetTopAndBottom(child, offsetY - params.mInsetOffsetY);
                params.mInsetOffsetY = offsetY;
            }
        }
    }

    public void dispatchDependentViewsChanged(View view) {
        Behavior behavior;
        List<View> dependents = this.mChildDags.getIncomingEdges(view);
        if (!(dependents == null || dependents.isEmpty())) {
            for (View child : dependents) {
                if ((child.getLayoutParams() instanceof LayoutParams) && (behavior = ((LayoutParams) child.getLayoutParams()).getBehavior()) != null) {
                    behavior.onDependentViewChanged(this, child, view);
                }
            }
        }
    }

    public List<View> getDependencies(View child) {
        List<View> dependencies = this.mChildDags.getOutgoingEdges(child);
        this.mTempDependenciesList.clear();
        if (dependencies != null) {
            this.mTempDependenciesList.addAll(dependencies);
        }
        return this.mTempDependenciesList;
    }

    public List<View> getDependents(View child) {
        List<View> edges = this.mChildDags.getIncomingEdges(child);
        this.mTempDependenciesList.clear();
        if (edges != null) {
            this.mTempDependenciesList.addAll(edges);
        }
        return this.mTempDependenciesList;
    }

    /* access modifiers changed from: package-private */
    public final List<View> getDependencySortedChildren() {
        prepareChildren();
        return Collections.unmodifiableList(this.mDependencySortedChildrenList);
    }

    /* access modifiers changed from: package-private */
    public void ensurePreDrawListener() {
        boolean isHasDependencies = false;
        int childCount = getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            } else if (hasDependencies(getChildAt(i))) {
                isHasDependencies = true;
                break;
            } else {
                i++;
            }
        }
        if (isHasDependencies == this.mIsNeedsPreDrawListener) {
            return;
        }
        if (isHasDependencies) {
            addPreDrawListener();
        } else {
            removePreDrawListener();
        }
    }

    private boolean hasDependencies(View child) {
        return this.mChildDags.hasOutgoingEdges(child);
    }

    /* access modifiers changed from: package-private */
    public void addPreDrawListener() {
        if (this.mIsAttachedToWindow) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mIsNeedsPreDrawListener = true;
    }

    /* access modifiers changed from: package-private */
    public void removePreDrawListener() {
        if (this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mIsNeedsPreDrawListener = false;
    }

    /* access modifiers changed from: package-private */
    public void offsetChildToAnchor(View child, int layoutDirection) {
        Behavior behavior;
        if (child.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.mAnchorView != null) {
                Rect anchorRect = acquireTempRect();
                Rect childRect = acquireTempRect();
                Rect desiredChildRect = acquireTempRect();
                getDescendantRect(params.mAnchorView, anchorRect);
                boolean isChanged = false;
                getChildRect(child, false, childRect);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                getDesiredAnchoredChildRectWithoutConstraints(layoutDirection, anchorRect, desiredChildRect, params, new Size(childWidth, childHeight));
                if (!(desiredChildRect.left == childRect.left && desiredChildRect.top == childRect.top)) {
                    isChanged = true;
                }
                constrainChildRect(params, desiredChildRect, childWidth, childHeight);
                int deltaX = desiredChildRect.left - childRect.left;
                int deltaY = desiredChildRect.top - childRect.top;
                if (deltaX != 0) {
                    ViewOffsetHelper.offsetLeftAndRight(child, deltaX);
                }
                if (deltaY != 0) {
                    ViewOffsetHelper.offsetTopAndBottom(child, deltaY);
                }
                if (isChanged && (behavior = params.getBehavior()) != null) {
                    behavior.onDependentViewChanged(this, child, params.mAnchorView);
                }
                releaseTempRect(anchorRect);
                releaseTempRect(childRect);
                releaseTempRect(desiredChildRect);
            }
        }
    }

    public boolean isPointInChildBounds(View child, int coordX, int coordY) {
        Rect rect = acquireTempRect();
        getDescendantRect(child, rect);
        try {
            return rect.contains(coordX, coordY);
        } finally {
            releaseTempRect(rect);
        }
    }

    public boolean doViewsOverlap(View first, View second) {
        boolean z = false;
        if (first.getVisibility() != 0 || second.getVisibility() != 0) {
            return false;
        }
        Rect firstRect = acquireTempRect();
        getChildRect(first, first.getParent() != this, firstRect);
        Rect secondRect = acquireTempRect();
        getChildRect(second, second.getParent() != this, secondRect);
        try {
            if (firstRect.left <= secondRect.right && firstRect.top <= secondRect.bottom && firstRect.right >= secondRect.left && firstRect.bottom >= secondRect.top) {
                z = true;
            }
            return z;
        } finally {
            releaseTempRect(firstRect);
            releaseTempRect(secondRect);
        }
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) params);
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) params);
        }
        return new LayoutParams(params);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return (params instanceof LayoutParams) && super.checkLayoutParams(params);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, 0);
    }

    @Override // huawei.android.widget.appbar.NestedScrollingParent2
    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        if (!this.mIsPertmitCollapse) {
            return false;
        }
        int childCount = getChildCount();
        boolean isHandled = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8 && (child.getLayoutParams() instanceof LayoutParams)) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                Behavior viewBehavior = params.getBehavior();
                if (viewBehavior != null) {
                    boolean isAccepted = viewBehavior.onStartNestedScroll(this, view, child, target, axes, type);
                    params.setNestedScrollAccepted(type, isAccepted);
                    isHandled |= isAccepted;
                } else {
                    params.setNestedScrollAccepted(type, false);
                }
            }
        }
        return isHandled;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, 0);
    }

    @Override // huawei.android.widget.appbar.NestedScrollingParent2
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes, int type) {
        Behavior viewBehavior;
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes, type);
        this.mNestedScrollingTarget = target;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params.isNestedScrollAccepted(type) && (viewBehavior = params.getBehavior()) != null) {
                    viewBehavior.onNestedScrollAccepted(this, view, child, target, nestedScrollAxes, type);
                }
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onStopNestedScroll(View target) {
        onStopNestedScroll(target, 0);
    }

    @Override // huawei.android.widget.appbar.NestedScrollingParent2
    public void onStopNestedScroll(View target, int type) {
        if (this.mIsPertmitCollapse) {
            this.mNestedScrollingParentHelper.onStopNestedScroll(target, type);
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view.getLayoutParams() instanceof LayoutParams) {
                    LayoutParams params = (LayoutParams) view.getLayoutParams();
                    if (params.isNestedScrollAccepted(type)) {
                        Behavior viewBehavior = params.getBehavior();
                        if (viewBehavior != null) {
                            viewBehavior.onStopNestedScroll(this, view, target, type);
                        }
                        params.resetNestedScroll(type);
                        params.resetChangedAfterNestedScroll();
                    }
                }
            }
            this.mNestedScrollingTarget = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onNestedScroll(View target, int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY) {
        onNestedScroll(target, consumedDeltaX, consumedDeltaY, unconsumedDeltaX, unconsumedDeltaY, 0);
    }

    @Override // huawei.android.widget.appbar.NestedScrollingParent2
    public void onNestedScroll(View target, int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY, int type) {
        Behavior viewBehavior;
        int childCount = getChildCount();
        if (this.mIsPertmitCollapse) {
            boolean isAccepted = false;
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view.getVisibility() != 8) {
                    if (view.getLayoutParams() instanceof LayoutParams) {
                        LayoutParams params = (LayoutParams) view.getLayoutParams();
                        if (params.isNestedScrollAccepted(type) && (viewBehavior = params.getBehavior()) != null) {
                            viewBehavior.onNestedScroll(this, view, target, consumedDeltaX, consumedDeltaY, unconsumedDeltaX, unconsumedDeltaY, type);
                            isAccepted = true;
                        }
                    }
                }
            }
            if (isAccepted) {
                onChildViewsChanged(1);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onNestedPreScroll(View target, int deltaX, int deltaY, int[] consumeArrays) {
        onNestedPreScroll(target, deltaX, deltaY, consumeArrays, 0);
    }

    @Override // huawei.android.widget.appbar.NestedScrollingParent2
    public void onNestedPreScroll(View target, int deltaX, int deltaY, int[] consumeArrays, int type) {
        int consumedX;
        int consumedY;
        if (!this.mIsPertmitCollapse) {
            consumeArrays[0] = 0;
            consumeArrays[1] = 0;
            return;
        }
        int childCount = getChildCount();
        int consumedX2 = 0;
        int consumedY2 = 0;
        boolean isAccepted = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8 && (view.getLayoutParams() instanceof LayoutParams)) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params.isNestedScrollAccepted(type)) {
                    Behavior viewBehavior = params.getBehavior();
                    if (viewBehavior != null) {
                        int[] iArr = this.mTempIntPairs;
                        iArr[0] = 0;
                        iArr[1] = 0;
                        viewBehavior.onNestedPreScroll(this, view, target, deltaX, deltaY, iArr, type);
                        if (deltaX > 0) {
                            consumedX = MathUtils.max(consumedX2, this.mTempIntPairs[0]);
                        } else {
                            consumedX = MathUtils.min(consumedX2, this.mTempIntPairs[0]);
                        }
                        if (deltaY > 0) {
                            consumedY = MathUtils.max(consumedY2, this.mTempIntPairs[1]);
                        } else {
                            consumedY = MathUtils.min(consumedY2, this.mTempIntPairs[1]);
                        }
                        consumedX2 = consumedX;
                        consumedY2 = consumedY;
                        isAccepted = true;
                    }
                }
            }
        }
        consumeArrays[0] = consumedX2;
        consumeArrays[1] = consumedY2;
        if (isAccepted) {
            onChildViewsChanged(1);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean isConsumed) {
        Behavior viewBehavior;
        int childCount = getChildCount();
        boolean isHandled = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8 && (view.getLayoutParams() instanceof LayoutParams)) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params.isNestedScrollAccepted(0) && (viewBehavior = params.getBehavior()) != null) {
                    isHandled = viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, isConsumed) | isHandled;
                }
            }
        }
        if (isHandled) {
            onChildViewsChanged(1);
        }
        return isHandled;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Behavior viewBehavior;
        boolean isHandled = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8 && (view.getLayoutParams() instanceof LayoutParams)) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params.isNestedScrollAccepted(0) && (viewBehavior = params.getBehavior()) != null) {
                    isHandled |= viewBehavior.onNestedPreFling(this, view, target, velocityX, velocityY);
                }
            }
        }
        return isHandled;
    }

    @Override // android.view.ViewGroup
    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    /* access modifiers changed from: package-private */
    public class OnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        OnPreDrawListener() {
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            HwCoordinatorLayout.this.onChildViewsChanged(0);
            return true;
        }
    }

    static class ViewElevationComparator implements Comparator<View> {
        ViewElevationComparator() {
        }

        public int compare(View left, View right) {
            return Float.compare(right.getZ(), left.getZ());
        }
    }

    public static abstract class Behavior<V extends View> {
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
        }

        public void onAttachedToLayoutParams(LayoutParams params) {
        }

        public void onDetachedFromLayoutParams() {
        }

        public boolean onInterceptTouchEvent(HwCoordinatorLayout parent, V v, MotionEvent event) {
            return false;
        }

        public boolean onTouchEvent(HwCoordinatorLayout parent, V v, MotionEvent event) {
            return false;
        }

        public int getScrimColor(HwCoordinatorLayout parent, V v) {
            return -16777216;
        }

        public float getScrimOpacity(HwCoordinatorLayout parent, V v) {
            return 0.0f;
        }

        public boolean blocksInteractionBelow(HwCoordinatorLayout parent, V child) {
            return getScrimOpacity(parent, child) > 0.0f;
        }

        public boolean layoutDependsOn(HwCoordinatorLayout parent, V v, View dependency) {
            return false;
        }

        public boolean onDependentViewChanged(HwCoordinatorLayout parent, V v, View dependency) {
            return false;
        }

        public void onDependentViewRemoved(HwCoordinatorLayout parent, V v, View dependency) {
        }

        public boolean onMeasureChild(HwCoordinatorLayout parent, V v, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            return false;
        }

        public boolean onLayoutChild(HwCoordinatorLayout parent, V v, int layoutDirection) {
            return false;
        }

        public static void setTag(View child, Object tag) {
            if (child.getLayoutParams() instanceof LayoutParams) {
                ((LayoutParams) child.getLayoutParams()).mBehaviorTag = tag;
            }
        }

        public static Object getTag(View child) {
            return ((LayoutParams) child.getLayoutParams()).mBehaviorTag;
        }

        public boolean onStartNestedScroll(HwCoordinatorLayout coordinatorLayout, V v, View directTargetChild, View target, int axes) {
            return false;
        }

        public boolean onStartNestedScroll(HwCoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int axes, int type) {
            if (type == 0) {
                return onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes);
            }
            return false;
        }

        public void onNestedScrollAccepted(HwCoordinatorLayout coordinatorLayout, V v, View directTargetChild, View target, int axes) {
        }

        public void onNestedScrollAccepted(HwCoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int axes, int type) {
            if (type == 0) {
                onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes);
            }
        }

        @Deprecated
        public void onStopNestedScroll(HwCoordinatorLayout coordinatorLayout, V v, View target) {
        }

        public void onStopNestedScroll(HwCoordinatorLayout coordinatorLayout, V child, View target, int type) {
            if (type == 0) {
                onStopNestedScroll(coordinatorLayout, child, target);
            }
        }

        public void onNestedScroll(HwCoordinatorLayout coordinatorLayout, V v, View target, int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY) {
        }

        public void onNestedScroll(HwCoordinatorLayout coordinatorLayout, V child, View target, int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY, int type) {
            if (type == 0) {
                onNestedScroll(coordinatorLayout, child, target, consumedDeltaX, consumedDeltaY, unconsumedDeltaX, unconsumedDeltaY);
            }
        }

        public void onNestedPreScroll(HwCoordinatorLayout coordinatorLayout, V v, View target, int deltaX, int deltaY, int[] consumeArrays) {
        }

        public void onNestedPreScroll(HwCoordinatorLayout coordinatorLayout, V child, View target, int deltaX, int deltaY, int[] consumeArrays, int type) {
            if (type == 0) {
                onNestedPreScroll(coordinatorLayout, child, target, deltaX, deltaY, consumeArrays);
            }
        }

        public boolean onNestedFling(HwCoordinatorLayout coordinatorLayout, V v, View target, float velocityX, float velocityY, boolean isConsumed) {
            return false;
        }

        public boolean onNestedPreFling(HwCoordinatorLayout coordinatorLayout, V v, View target, float velocityX, float velocityY) {
            return false;
        }

        public WindowInsets onApplyWindowInsets(HwCoordinatorLayout coordinatorLayout, V v, WindowInsets insets) {
            return insets;
        }

        public boolean onRequestChildRectangleOnScreen(HwCoordinatorLayout coordinatorLayout, V v, Rect rectangle, boolean isImmediate) {
            return false;
        }

        public void onRestoreInstanceState(HwCoordinatorLayout parent, V v, Parcelable state) {
        }

        public Parcelable onSaveInstanceState(HwCoordinatorLayout parent, V v) {
            return View.BaseSavedState.EMPTY_STATE;
        }

        public boolean getInsetDodgeRect(HwCoordinatorLayout parent, V v, Rect rect) {
            return false;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        View mAnchorDirectChild;
        public int mAnchorGravity = 0;
        int mAnchorId = -1;
        View mAnchorView;
        Behavior mBehavior;
        Object mBehaviorTag;
        public int mDodgeInsetEdges = 0;
        public int mGravity = 0;
        public int mInsetEdge = 0;
        int mInsetOffsetX;
        int mInsetOffsetY;
        boolean mIsBehaviorResolved = false;
        private boolean mIsDidAcceptNestedScrollNonTouch;
        private boolean mIsDidAcceptNestedScrollTouch;
        private boolean mIsDidBlockInteraction;
        private boolean mIsDidChangeAfterNestedScroll;
        public int mKeyline = -1;
        final Rect mLastChildRect = new Rect();

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HwCoordinatorLayout_Layout);
            this.mGravity = array.getInteger(0, 0);
            this.mAnchorId = array.getResourceId(1, -1);
            this.mAnchorGravity = array.getInteger(2, 0);
            this.mKeyline = array.getInteger(6, -1);
            this.mInsetEdge = array.getInt(5, 0);
            this.mDodgeInsetEdges = array.getInt(4, 0);
            this.mIsBehaviorResolved = array.hasValue(3);
            if (this.mIsBehaviorResolved) {
                this.mBehavior = HwCoordinatorLayout.parseBehavior(context, attrs, array.getString(3));
            }
            array.recycle();
            Behavior behavior = this.mBehavior;
            if (behavior != null) {
                behavior.onAttachedToLayoutParams(this);
            }
        }

        public LayoutParams(LayoutParams params) {
            super((ViewGroup.MarginLayoutParams) params);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams params) {
            super(params);
        }

        public LayoutParams(ViewGroup.LayoutParams params) {
            super(params);
        }

        public int getAnchorId() {
            return this.mAnchorId;
        }

        public void setAnchorId(int id) {
            invalidateAnchor();
            this.mAnchorId = id;
        }

        public Behavior getBehavior() {
            return this.mBehavior;
        }

        public void setBehavior(Behavior behavior) {
            Behavior behavior2 = this.mBehavior;
            if (behavior2 != behavior) {
                if (behavior2 != null) {
                    behavior2.onDetachedFromLayoutParams();
                }
                this.mBehavior = behavior;
                this.mBehaviorTag = null;
                this.mIsBehaviorResolved = true;
                if (behavior != null) {
                    behavior.onAttachedToLayoutParams(this);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setLastChildRect(Rect rect) {
            this.mLastChildRect.set(rect);
        }

        /* access modifiers changed from: package-private */
        public Rect getLastChildRect() {
            return this.mLastChildRect;
        }

        /* access modifiers changed from: package-private */
        public boolean checkAnchorChanged() {
            return this.mAnchorView == null && this.mAnchorId != -1;
        }

        /* access modifiers changed from: package-private */
        public boolean didBlockInteraction() {
            if (this.mBehavior == null) {
                this.mIsDidBlockInteraction = false;
            }
            return this.mIsDidBlockInteraction;
        }

        /* access modifiers changed from: package-private */
        public boolean isBlockingInteractionBelow(HwCoordinatorLayout parent, View child) {
            boolean z = this.mIsDidBlockInteraction;
            if (z) {
                return true;
            }
            if (!(z || this.mBehavior != null) || !this.mBehavior.blocksInteractionBelow(parent, child)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public void resetTouchBehaviorTracking() {
            this.mIsDidBlockInteraction = false;
        }

        /* access modifiers changed from: package-private */
        public void resetNestedScroll(int type) {
            setNestedScrollAccepted(type, false);
        }

        /* access modifiers changed from: package-private */
        public void setNestedScrollAccepted(int type, boolean isAccept) {
            if (type == 0) {
                this.mIsDidAcceptNestedScrollTouch = isAccept;
            } else if (type == 1) {
                this.mIsDidAcceptNestedScrollNonTouch = isAccept;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isNestedScrollAccepted(int type) {
            if (type == 0) {
                return this.mIsDidAcceptNestedScrollTouch;
            }
            if (type != 1) {
                return false;
            }
            return this.mIsDidAcceptNestedScrollNonTouch;
        }

        /* access modifiers changed from: package-private */
        public boolean getChangedAfterNestedScroll() {
            return this.mIsDidChangeAfterNestedScroll;
        }

        /* access modifiers changed from: package-private */
        public void setChangedAfterNestedScroll(boolean isChanged) {
            this.mIsDidChangeAfterNestedScroll = isChanged;
        }

        /* access modifiers changed from: package-private */
        public void resetChangedAfterNestedScroll() {
            this.mIsDidChangeAfterNestedScroll = false;
        }

        /* access modifiers changed from: package-private */
        public boolean dependsOn(HwCoordinatorLayout parent, View child, View dependency) {
            Behavior behavior;
            return dependency == this.mAnchorDirectChild || shouldDodge(dependency, parent.getLayoutDirection()) || ((behavior = this.mBehavior) != null && behavior.layoutDependsOn(parent, child, dependency));
        }

        /* access modifiers changed from: package-private */
        public void invalidateAnchor() {
            this.mAnchorView = null;
            this.mAnchorDirectChild = null;
        }

        /* access modifiers changed from: package-private */
        public View findAnchorView(HwCoordinatorLayout parent, View forChild) {
            if (this.mAnchorId == -1) {
                this.mAnchorView = null;
                this.mAnchorDirectChild = null;
                return null;
            }
            if (this.mAnchorView == null || !verifyAnchorView(forChild, parent)) {
                resolveAnchorView(forChild, parent);
            }
            return this.mAnchorView;
        }

        private void resolveAnchorView(View forChild, HwCoordinatorLayout parent) {
            this.mAnchorView = parent.findViewById(this.mAnchorId);
            View view = this.mAnchorView;
            if (view != null) {
                if (view != parent) {
                    View directChild = this.mAnchorView;
                    ViewParent viewParent = view.getParent();
                    while (viewParent != parent && viewParent != null) {
                        if (viewParent != forChild) {
                            if (viewParent instanceof View) {
                                directChild = (View) viewParent;
                            }
                            viewParent = viewParent.getParent();
                        } else if (parent.isInEditMode()) {
                            this.mAnchorView = null;
                            this.mAnchorDirectChild = null;
                            return;
                        } else {
                            throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                        }
                    }
                    this.mAnchorDirectChild = directChild;
                } else if (parent.isInEditMode()) {
                    this.mAnchorView = null;
                    this.mAnchorDirectChild = null;
                } else {
                    throw new IllegalStateException("View can not be anchored to the the parent CoordinatorLayout");
                }
            } else if (parent.isInEditMode()) {
                this.mAnchorView = null;
                this.mAnchorDirectChild = null;
            } else {
                throw new IllegalStateException("Could not find CoordinatorLayout descendant view with id " + parent.getResources().getResourceName(this.mAnchorId) + " to anchor view " + forChild);
            }
        }

        private boolean verifyAnchorView(View forChild, HwCoordinatorLayout parent) {
            if (this.mAnchorView.getId() != this.mAnchorId) {
                return false;
            }
            View directChild = this.mAnchorView;
            for (ViewParent viewParent = this.mAnchorView.getParent(); viewParent != parent; viewParent = viewParent.getParent()) {
                if (viewParent == null || viewParent == forChild) {
                    this.mAnchorView = null;
                    this.mAnchorDirectChild = null;
                    return false;
                }
                if (viewParent instanceof View) {
                    directChild = (View) viewParent;
                }
            }
            this.mAnchorDirectChild = directChild;
            return true;
        }

        private boolean shouldDodge(View other, int layoutDirection) {
            int absInset;
            if ((other.getLayoutParams() instanceof LayoutParams) && (absInset = Gravity.getAbsoluteGravity(((LayoutParams) other.getLayoutParams()).mInsetEdge, layoutDirection)) != 0 && (Gravity.getAbsoluteGravity(this.mDodgeInsetEdges, layoutDirection) & absInset) == absInset) {
                return true;
            }
            return false;
        }
    }

    private class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        HierarchyChangeListener() {
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewAdded(View parent, View child) {
            if (HwCoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                HwCoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewRemoved(View parent, View child) {
            HwCoordinatorLayout.this.onChildViewsChanged(2);
            if (HwCoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                HwCoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        Parcelable savedState;
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState status = (SavedState) state;
        super.onRestoreInstanceState(status.getSuperState());
        SparseArray<Parcelable> behaviorStates = status.behaviorStates;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childId = child.getId();
            Behavior behavior = getResolvedLayoutParams(child).getBehavior();
            if (!(childId == -1 || behavior == null || (savedState = behaviorStates.get(childId)) == null)) {
                behavior.onRestoreInstanceState(this, child, savedState);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        Parcelable state;
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        SparseArray<Parcelable> behaviorStates = new SparseArray<>();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childId = child.getId();
            if (child.getLayoutParams() instanceof LayoutParams) {
                Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
                if (!(childId == -1 || behavior == null || (state = behavior.onSaveInstanceState(this, child)) == null)) {
                    behaviorStates.append(childId, state);
                }
            }
        }
        savedState.behaviorStates = behaviorStates;
        return savedState;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean isImmediate) {
        if (!(child.getLayoutParams() instanceof LayoutParams)) {
            return false;
        }
        Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
        if (behavior == null || !behavior.onRequestChildRectangleOnScreen(this, child, rectangle, isImmediate)) {
            return super.requestChildRectangleOnScreen(child, rectangle, isImmediate);
        }
        return true;
    }

    private void setupForInsets() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (!getFitsSystemWindows()) {
                setOnApplyWindowInsetsListener(null);
                return;
            }
            if (this.mApplyWindowInsetsListener == null) {
                this.mApplyWindowInsetsListener = new View.OnApplyWindowInsetsListener() {
                    /* class huawei.android.widget.appbar.HwCoordinatorLayout.AnonymousClass1 */

                    @Override // android.view.View.OnApplyWindowInsetsListener
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                        return HwCoordinatorLayout.this.setWindowInsets(insets);
                    }
                };
            }
            setOnApplyWindowInsetsListener(this.mApplyWindowInsetsListener);
            setSystemUiVisibility(1280);
        }
    }

    /* access modifiers changed from: protected */
    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
            /* class huawei.android.widget.appbar.HwCoordinatorLayout.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.ClassLoaderCreator
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private static final int INT32_SIZE = 4;
        SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            int size = source.readInt();
            if (source.dataAvail() + 4 < size || size < 0) {
                this.behaviorStates = new SparseArray<>(0);
                return;
            }
            int[] ids = new int[size];
            source.readIntArray(ids);
            Parcelable[] states = source.readParcelableArray(loader);
            this.behaviorStates = new SparseArray<>(size);
            for (int i = 0; i < size; i++) {
                if (states != null) {
                    this.behaviorStates.append(ids[i], states[i]);
                }
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            SparseArray<Parcelable> sparseArray = this.behaviorStates;
            int size = sparseArray != null ? sparseArray.size() : 0;
            dest.writeInt(size);
            int[] ids = new int[size];
            Parcelable[] states = new Parcelable[size];
            for (int i = 0; i < size; i++) {
                ids[i] = this.behaviorStates.keyAt(i);
                states[i] = this.behaviorStates.valueAt(i);
            }
            dest.writeIntArray(ids);
            dest.writeParcelableArray(states, flags);
        }
    }
}
