package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FastScrollerEx;
import android.widget.ListAdapter;
import android.widget.RemoteViews;
import androidhwext.R;
import huawei.android.view.HwPositionPair;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.RollbackRuleDetector;
import huawei.android.widget.plume.HwPlumeManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RemoteViews.RemoteView
public class ListView extends android.widget.ListView implements ScrollCallback {
    private static final String ANIMATOR_TAG = "listDeleteAnimation";
    public static final int CHOICE_MODE_MULTIPLE_MODAL_AUTO_SCROLL = 8;
    private static final String CLICL_STATUS_BAR_ACTION = "com.huawei.intent.action.CLICK_STATUSBAR";
    private static final int COMPARE_LESS = -1;
    private static final int CONSTRUCTOR_INTEGER_VALUE = -1;
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final int DEFAULT_SENSITIVITY_MODE = 1;
    private static final int DELETE_ANIMATION_ALPHA_DURATION = 200;
    private static final int DELETE_ANIMATION_HEIGHT_DURATION = 300;
    private static final int INVALID_INDEX = -1;
    private static final int INVALID_POSITION = -1;
    private static final boolean IS_ANIMATOR_DBG = false;
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final int MAX_ALPHA_VALUE = 255;
    private static final int SCREEN_PAGE_NUMBER = 15;
    private static final int SCROLL_TO_TOP_DURATION = 600;
    private static final String SYSTEMUI_PERMITION = "huawei.permission.CLICK_STATUSBAR_BROADCAST";
    private static final String TAG = "ListView";
    private Class mAbsListParamsClass;
    private Context mContext;
    private Animator mDeleteAnimator;
    private int mDeleteCountAfterVisible;
    private int mDeleteCountBeforeVisible;
    private DeleteAnimatorCallback mDeleteInterface;
    private int mDividerAlphaOld;
    private IntentFilter mFilter;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private boolean mIsHasRegistReciver;
    private boolean mIsHasUsedRollback;
    private boolean mIsLocalSetting;
    private boolean mIsScrollTopEnable;
    private Map<Integer, Rect> mItemViewOriginalPadding;
    private Rect mListInsetsRect;
    private Rect mListRect;
    private ListViewFlingCoordinator mListViewFlingCoordinator;
    private Context mReceiverContext;
    private RollbackRuleDetector mRollbackRuleDetector;
    private BroadcastReceiver mScrollToTopReceiver;
    private List<DeleteItemInfo> mVisibleDeleteItems;

    public interface DeleteAnimatorCallback {
        int getItemPosition(Object obj);

        void notifyDataSetChanged();

        void notifyResult(boolean z);

        void remove(Object obj);
    }

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mItemViewOriginalPadding = new HashMap(16);
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mAbsListParamsClass = null;
        this.mIsScrollTopEnable = false;
        this.mIsHasRegistReciver = false;
        this.mListRect = new Rect();
        this.mListInsetsRect = new Rect();
        this.mDividerAlphaOld = MAX_ALPHA_VALUE;
        this.mVisibleDeleteItems = new ArrayList(0);
        this.mDeleteCountBeforeVisible = 0;
        this.mDeleteCountAfterVisible = 0;
        this.mDeleteInterface = null;
        this.mDeleteAnimator = null;
        this.mIsLocalSetting = false;
        this.mScrollToTopReceiver = new BroadcastReceiver() {
            /* class huawei.android.widget.ListView.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && ListView.this.getScrollY() == 0 && ListView.CLICL_STATUS_BAR_ACTION.equals(intent.getAction())) {
                    ListView.this.handleScrollToTop();
                }
            }
        };
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;
        if (context.getApplicationContext() != null) {
            this.mReceiverContext = context.getApplicationContext();
        } else {
            this.mReceiverContext = context;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwListView, defStyleAttr, defStyleRes);
        this.mIsScrollTopEnable = typedArray.getBoolean(0, true);
        int sensitivityMode = typedArray.getInt(1, 1);
        typedArray.recycle();
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
        this.mRollbackRuleDetector = new RollbackRuleDetector(new RollbackRuleDetector.RollBackScrollListener() {
            /* class huawei.android.widget.ListView.AnonymousClass2 */

            @Override // huawei.android.widget.RollbackRuleDetector.RollBackScrollListener
            public int getScrollYDistance() {
                View view = ListView.this.getChildAt(0);
                if (view != null) {
                    return (ListView.this.getFirstVisiblePosition() * view.getHeight()) - view.getTop();
                }
                return 0;
            }
        });
        setValueFromPlume();
        setSensitivityMode(sensitivityMode);
    }

    private void setValueFromPlume() {
        if (!HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendedMultiChoiceEnabled(false, true);
            setExtendedMultiChoiceEnabled(true, true);
            setExtendScrollEnabled(true);
            return;
        }
        setExtendedMultiChoiceEnabled(false, HwPlumeManager.getInstance(this.mContext).getDefault(this, "quickSelectEnabled", true));
        setExtendedMultiChoiceEnabled(true, HwPlumeManager.getInstance(this.mContext).getDefault(this, "consecutiveSelectEnabled", true));
        setExtendScrollEnabled(HwPlumeManager.getInstance(this.mContext).getDefault(this, "listScrollEnabled", true));
    }

    @Override // huawei.android.widget.ScrollCallback
    public void scrollToTop() {
        handleScrollToTop();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScrollToTop() {
        post(new Runnable() {
            /* class huawei.android.widget.ListView.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                int num = ListView.this.getChildCount() * 15;
                if (ListView.this.getFirstVisiblePosition() > num) {
                    ListView.this.setSelection(num);
                }
                ListView.this.smoothScrollToPositionFromTop(0, 0, ListView.SCROLL_TO_TOP_DURATION);
                if (!ListView.this.mIsHasUsedRollback) {
                    ListView.this.mRollbackRuleDetector.postScrollUsedEvent();
                    ListView.this.mIsHasUsedRollback = true;
                }
            }
        });
    }

    public boolean dispatchStatusBarTop() {
        return true;
    }

    public void setItemDeleteAnimation(boolean isEnable) {
        this.mIsSupportAnim = isEnable;
        if (IS_EMUI_LITE) {
            this.mIsSupportAnim = false;
        }
        wrapObserver();
        if (this.mIsSupportAnim) {
            onFirstPositionChange();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateListPadding();
        wrapObserver();
        registerReceiver();
        if (this.mIsHasRegistReciver) {
            this.mRollbackRuleDetector.start(this);
        }
        initListViewFlingCoordinator();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mIsSupportAnim && this.mListDeleteAnimator != null) {
            this.mListDeleteAnimator.cancel();
            return true;
        } else if (this.mVisibleDeleteItems.size() != 0) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override // android.widget.ListView, android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mIsSupportAnim && this.mListDeleteAnimator != null) {
            this.mListDeleteAnimator.cancel();
            Log.w(ANIMATOR_TAG, "dispatchTouchEvent : end the deleting animator, ignore key code");
            return true;
        } else if (this.mVisibleDeleteItems.size() == 0) {
            return super.dispatchKeyEvent(event);
        } else {
            Log.w(ANIMATOR_TAG, "dispatchTouchEvent : mVisibleDeleteItems not empty, ignore key code.");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.widget.AbsListView, android.view.View
    public void onFocusChanged(boolean isGainFocus, int direction, Rect previouslyFocusedRect) {
        if (this.mIsSupportAnim && this.mListDeleteAnimator != null) {
            this.mListDeleteAnimator.cancel();
            Log.w(ANIMATOR_TAG, "onFocusChanged : animator is playing, do't support change focus.");
        } else if (this.mVisibleDeleteItems.size() != 0) {
            Log.w(ANIMATOR_TAG, "onFocusChanged : mVisibleDeleteItems not empty, do't support change focus.");
        } else {
            super.onFocusChanged(isGainFocus, direction, previouslyFocusedRect);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewTreeObserver.OnTouchModeChangeListener
    public void onTouchModeChanged(boolean isInTouchMode) {
        if (this.mIsSupportAnim && this.mListDeleteAnimator != null) {
            this.mListDeleteAnimator.cancel();
            Log.w(ANIMATOR_TAG, "onTouchModeChanged : animator is playing, do't support change touch mode.");
        } else if (this.mVisibleDeleteItems.size() != 0) {
            Log.w(ANIMATOR_TAG, "onTouchModeChanged : mVisibleDeleteItems not empty. ignore touch mode change.");
        } else {
            super.onTouchModeChanged(isInTouchMode);
        }
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        this.mRollbackRuleDetector.onTouchEvent(ev);
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            listViewFlingCoordinator.onTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        return super.onApplyWindowInsets(insets);
    }

    private void updateListPadding() {
        this.mListRect.set(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        this.mHwWidgetSafeInsets.updateOriginPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.AdapterView
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        updateChildrenInsets();
    }

    private void updateChildrenInsets() {
        if (isCutoutEnabled()) {
            ListAdapter adapter = getAdapter();
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child == null || adapter == null)) {
                    int firstVisibleItemPosition = getFirstVisiblePosition();
                    if (firstVisibleItemPosition + i < adapter.getCount()) {
                        doItemCutoutPadding(child, adapter.getItemViewType(firstVisibleItemPosition + i));
                    } else {
                        return;
                    }
                }
            }
            Rect rect = this.mHwWidgetSafeInsets.getDisplaySafeInsets(this);
            if (rect != null) {
                this.mListInsetsRect.set(rect);
            }
        }
    }

    private void initFitCutout() {
        if (this.mAbsListParamsClass == null) {
            this.mAbsListParamsClass = ReflectUtil.getPrivateClass("android.widget.AbsListView$LayoutParams");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
        if (isCutoutEnabled()) {
            doItemCutoutPadding(child, params);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean isPreventRequestLayout) {
        if (isCutoutEnabled()) {
            doItemCutoutPadding(child, params);
        }
        return super.addViewInLayout(child, index, params, isPreventRequestLayout);
    }

    private void doItemCutoutPadding(View child, ViewGroup.LayoutParams params) {
        if ((child == null || params == null || !(params instanceof AbsListView.LayoutParams)) ? false : true) {
            initFitCutout();
            Class cls = this.mAbsListParamsClass;
            if (cls != null) {
                doItemCutoutPadding(child, ((Integer) ReflectUtil.getObject(params, "viewType", cls)).intValue());
            }
        }
    }

    private boolean isCutoutEnabled() {
        return !this.mHwWidgetSafeInsets.isCutoutModeNever();
    }

    private void doItemCutoutPadding(final View child, int viewType) {
        Rect childRect;
        int layoutDirection = getLayoutDirection();
        if (child.getLayoutDirection() != layoutDirection) {
            child.setLayoutDirection(layoutDirection);
        }
        Rect originRect = this.mItemViewOriginalPadding.get(Integer.valueOf(viewType));
        if (originRect == null) {
            originRect = new Rect(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), child.getPaddingBottom());
            this.mItemViewOriginalPadding.put(Integer.valueOf(viewType), originRect);
            childRect = originRect;
        } else {
            childRect = new Rect(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), child.getPaddingBottom());
        }
        Rect childSafeRect = originRect;
        if ((child instanceof NonSafeInsetsAvailable) || (childSafeRect = this.mHwWidgetSafeInsets.getDisplaySafeInsets(this, originRect)) != null) {
            Rect newRect = new Rect(childSafeRect.left, child.getPaddingTop(), childSafeRect.right, child.getPaddingBottom());
            if (!newRect.equals(childRect)) {
                boolean isNeedReqLayout = child.isInLayout() && !child.isLayoutRequested();
                child.setPadding(newRect.left, newRect.top, newRect.right, newRect.bottom);
                if (isNeedReqLayout) {
                    post(new Runnable() {
                        /* class huawei.android.widget.ListView.AnonymousClass4 */

                        @Override // java.lang.Runnable
                        public void run() {
                            child.requestLayout();
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        Rect listSafeInsetsRect = this.mListInsetsRect;
        if (isCutoutEnabled() && !(this.mListRect.left == listSafeInsetsRect.left && this.mListRect.right == listSafeInsetsRect.right)) {
            this.mPaddingLeft = listSafeInsetsRect.left;
            this.mPaddingRight = listSafeInsetsRect.right;
            super.dispatchDraw(canvas);
            this.mPaddingLeft = this.mListRect.left;
            this.mPaddingRight = this.mListRect.right;
            return;
        }
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int left, int top, int right, int bottom) {
        super.onDrawVerticalScrollBar(canvas, scrollBar, left, top, right, bottom);
    }

    /* access modifiers changed from: protected */
    public void adjustVerticalScrollBarBounds(Rect bounds) {
        int offsetX;
        if (bounds != null && !bounds.isEmpty() && isCutoutEnabled()) {
            int verticalScrollbarPosition = getVerticalScrollbarPosition();
            if (verticalScrollbarPosition == 0) {
                verticalScrollbarPosition = isRtlLocale() ? 1 : 2;
            }
            Rect listSafeInsetsRect = this.mListInsetsRect;
            if (this.mListRect.left != listSafeInsetsRect.left && verticalScrollbarPosition == 1) {
                offsetX = listSafeInsetsRect.left - this.mListRect.left;
            } else if (this.mListRect.right == listSafeInsetsRect.right || verticalScrollbarPosition != 2) {
                offsetX = 0;
            } else {
                offsetX = this.mListRect.right - listSafeInsetsRect.right;
            }
            bounds.offset(offsetX, 0);
        }
    }

    public Object getScroller() {
        return super.getScrollerInner();
    }

    @Deprecated
    public void setScroller(FastScrollerEx scroller) {
        super.setScrollerInner(scroller);
    }

    public void setScroller(com.huawei.android.widget.FastScrollerEx scroller) {
        super.setScrollerInner((FastScrollerEx) scroller);
    }

    public void setScrollerInner(Object fastScroller) {
        if (fastScroller == null || (fastScroller instanceof FastScrollerEx)) {
            super.setScrollerInner((FastScrollerEx) fastScroller);
        }
    }

    public void setScrollTopEnable(boolean isScrollTopEnable) {
        if (isScrollTopEnable != this.mIsScrollTopEnable) {
            this.mIsScrollTopEnable = isScrollTopEnable;
            if (isScrollTopEnable) {
                registerReceiver();
                if (this.mIsHasRegistReciver) {
                    this.mRollbackRuleDetector.start(this);
                    return;
                }
                return;
            }
            unregisterReceiver();
            this.mRollbackRuleDetector.stop();
        }
    }

    private void registerReceiver() {
        if (this.mIsScrollTopEnable && !this.mIsHasRegistReciver && this.mReceiverContext != null) {
            if (this.mFilter == null) {
                this.mFilter = new IntentFilter(CLICL_STATUS_BAR_ACTION);
            }
            try {
                this.mReceiverContext.registerReceiver(this.mScrollToTopReceiver, this.mFilter, SYSTEMUI_PERMITION, null);
                this.mIsHasRegistReciver = true;
            } catch (IllegalStateException e) {
                Log.w(TAG, "registerReceiver IllegalStateException");
                this.mIsHasRegistReciver = false;
            } catch (ReceiverCallNotAllowedException e2) {
                Log.w(TAG, "There is a problem with the APP application scenario:BroadcastReceiver components are not allowed to register to receive intents");
                this.mIsHasRegistReciver = false;
            }
        }
    }

    private void unregisterReceiver() {
        Context context;
        if (this.mIsHasRegistReciver && (context = this.mReceiverContext) != null) {
            try {
                context.unregisterReceiver(this.mScrollToTopReceiver);
                this.mIsHasRegistReciver = false;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.AdapterView
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
        this.mRollbackRuleDetector.stop();
    }

    /* access modifiers changed from: private */
    public class AnimDrawable extends BitmapDrawable {
        private int mDeltaX;
        private int mDeltaY;
        private int mPositionX;
        private int mPositionY;

        private AnimDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
            this.mDeltaX = 0;
            this.mDeltaY = 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setPosition(int positionX, int positionY) {
            this.mPositionX = positionX;
            this.mPositionY = positionY;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setTranslation(int deltaX, int deltaY) {
            this.mDeltaX = deltaX;
            this.mDeltaY = deltaY;
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.clipRect(this.mPositionX, this.mPositionY, canvas.getWidth(), canvas.getHeight());
            canvas.translate((float) (this.mDeltaX + this.mPositionX), (float) (this.mDeltaY + this.mPositionY));
            super.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: private */
    public class DeleteItemInfo {
        float mAlpha = 1.0f;
        AnimDrawable mAnimDrawable;
        int mHeight;
        boolean mIsDeleted = false;
        boolean mIsInitDrawable = false;
        Object mItem;
        View mItemView;
        int mOriginalHeight;
        int mOriginalLayoutHeight;
        ViewGroupOverlay mOverlayView = null;
        int mPosition;
        int mTotalBeforeHeight;
        int mTotalDeltaY = 0;
        int mTotalHeight;

        DeleteItemInfo(int pos, Object item) {
            int firstPos = ListView.this.getFirstVisiblePosition();
            this.mPosition = pos;
            this.mItemView = ListView.this.getChildAt(pos - firstPos);
            View view = this.mItemView;
            if (view != null) {
                this.mOriginalHeight = view.getHeight();
                ViewGroup.LayoutParams params = this.mItemView.getLayoutParams();
                if (params != null) {
                    this.mOriginalLayoutHeight = params.height;
                } else {
                    this.mOriginalLayoutHeight = 0;
                }
            } else {
                Log.e(ListView.ANIMATOR_TAG, "DeleteItemInfo: call getChildAt failed");
                this.mOriginalHeight = 0;
            }
            this.mHeight = this.mOriginalHeight;
            this.mItem = item;
        }

        private void initDrawable() {
            View view = this.mItemView;
            if (view != null) {
                this.mIsInitDrawable = true;
                ViewParent viewParent = view.getParent();
                if (viewParent == null) {
                    Log.e(ListView.ANIMATOR_TAG, "initDrawable: viewParent is null");
                    return;
                }
                int width = this.mItemView.getWidth();
                int height = this.mItemView.getHeight();
                if (width <= 0 || height <= 0) {
                    Log.e(ListView.ANIMATOR_TAG, "initDrawable: mItemView's width or height is invalid");
                    return;
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                this.mItemView.draw(new Canvas(bitmap));
                ListView listView = ListView.this;
                this.mAnimDrawable = new AnimDrawable(listView.getResources(), bitmap);
                this.mAnimDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                this.mAnimDrawable.setPosition(this.mItemView.getLeft(), this.mItemView.getTop());
                this.mAnimDrawable.setAlpha((int) (this.mAlpha * 255.0f));
                if (viewParent instanceof ViewGroup) {
                    this.mOverlayView = ((ViewGroup) viewParent).getOverlay();
                    this.mOverlayView.add(this.mAnimDrawable);
                    this.mItemView.setAlpha(0.0f);
                    return;
                }
                Log.e(ListView.ANIMATOR_TAG, "initDrawable: viewParent is not instance of ViewGroup");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCurrentHeight(int height, int totalDeltaY) {
            this.mHeight = height;
            if (this.mItemView == null) {
                this.mTotalDeltaY += totalDeltaY;
                return;
            }
            if (!this.mIsInitDrawable) {
                initDrawable();
            }
            AnimDrawable animDrawable = this.mAnimDrawable;
            if (animDrawable != null) {
                animDrawable.setPosition(this.mItemView.getLeft(), this.mItemView.getTop());
                this.mAnimDrawable.setTranslation(0, this.mHeight - this.mOriginalHeight);
            }
            this.mItemView.getLayoutParams().height = height;
            this.mItemView.requestLayout();
            this.mTotalDeltaY = 0;
            ListView.this.requestLayout();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCurrentAlpha(float alpha) {
            this.mAlpha = alpha;
            if (this.mIsInitDrawable) {
                AnimDrawable animDrawable = this.mAnimDrawable;
                if (animDrawable != null) {
                    animDrawable.setAlpha((int) (this.mAlpha * 255.0f));
                    View view = this.mItemView;
                    if (view != null) {
                        view.setAlpha(0.0f);
                        return;
                    }
                    return;
                }
                View view2 = this.mItemView;
                if (view2 != null) {
                    view2.setAlpha(this.mAlpha);
                    return;
                }
                return;
            }
            View view3 = this.mItemView;
            if (view3 != null) {
                view3.setAlpha(this.mAlpha);
            }
        }

        private void setItemDeleted() {
            AnimDrawable animDrawable;
            this.mIsDeleted = true;
            this.mPosition = -1;
            ViewGroupOverlay viewGroupOverlay = this.mOverlayView;
            if (viewGroupOverlay != null && (animDrawable = this.mAnimDrawable) != null) {
                viewGroupOverlay.remove(animDrawable);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetSavedView(boolean isDeleted) {
            if (isDeleted) {
                this.mPosition = -1;
                setItemDeleted();
            } else {
                this.mPosition--;
            }
            View view = this.mItemView;
            if (view != null) {
                view.setAlpha(1.0f);
                ViewGroup.LayoutParams params = this.mItemView.getLayoutParams();
                int i = this.mOriginalLayoutHeight;
                if (i != 0) {
                    params.height = i;
                } else {
                    params.height = this.mOriginalHeight;
                }
                this.mItemView.setLayoutParams(params);
                this.mItemView = null;
                if (this.mIsInitDrawable && isDeleted) {
                    ViewGroupOverlay viewGroupOverlay = this.mOverlayView;
                    if (viewGroupOverlay == null) {
                        this.mIsInitDrawable = false;
                        return;
                    }
                    AnimDrawable animDrawable = this.mAnimDrawable;
                    if (animDrawable != null) {
                        viewGroupOverlay.remove(animDrawable);
                        this.mAnimDrawable = null;
                    }
                    this.mOverlayView = null;
                    this.mIsInitDrawable = false;
                }
            }
        }

        private void refreshItemHeight() {
            AnimDrawable animDrawable;
            View view = this.mItemView;
            if (view != null) {
                if (this.mIsInitDrawable && (animDrawable = this.mAnimDrawable) != null) {
                    animDrawable.setPosition(view.getLeft(), this.mAnimDrawable.mPositionY - this.mTotalDeltaY);
                    this.mAnimDrawable.setTranslation(0, this.mHeight - this.mOriginalHeight);
                    this.mTotalDeltaY = 0;
                }
                ViewGroup.LayoutParams params = this.mItemView.getLayoutParams();
                params.height = this.mHeight;
                this.mItemView.setLayoutParams(params);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void refreshItemView(int firstPosition) {
            if (this.mItemView == null) {
                View view = ListView.this.getChildAt(this.mPosition - firstPosition);
                if (view == null) {
                    Log.e(ListView.ANIMATOR_TAG, "refreshItemView: call getChildAt failed");
                    return;
                }
                this.mItemView = view;
                setCurrentAlpha(this.mAlpha);
                refreshItemHeight();
            }
        }
    }

    private void sortDeleteInfo() {
        if (!this.mVisibleDeleteItems.isEmpty()) {
            if (this.mVisibleDeleteItems.get(0).mItem instanceof HwPositionPair) {
                this.mVisibleDeleteItems.sort(new Comparator<DeleteItemInfo>() {
                    /* class huawei.android.widget.ListView.AnonymousClass5 */

                    public int compare(DeleteItemInfo item1, DeleteItemInfo item2) {
                        if (item1.mPosition < item2.mPosition) {
                            return 1;
                        }
                        if (item1.mPosition == item2.mPosition) {
                            return 0;
                        }
                        return -1;
                    }
                });
            } else {
                this.mVisibleDeleteItems.sort(new Comparator<DeleteItemInfo>() {
                    /* class huawei.android.widget.ListView.AnonymousClass6 */

                    public int compare(DeleteItemInfo item1, DeleteItemInfo item2) {
                        if (item1.mPosition > item2.mPosition) {
                            return 1;
                        }
                        if (item1.mPosition == item2.mPosition) {
                            return 0;
                        }
                        return -1;
                    }
                });
            }
        }
    }

    private void prepareBeforeAnimator() {
        sortDeleteInfo();
        if (this.mVisibleDeleteItems.isEmpty() || !(this.mVisibleDeleteItems.get(0).mItem instanceof HwPositionPair)) {
            for (DeleteItemInfo deleteItemInfo : this.mVisibleDeleteItems) {
                deleteItemInfo.mPosition -= this.mDeleteCountBeforeVisible;
            }
        }
    }

    private void updateSavedDeleteItemInfo(int begin, int end) {
        if (begin > end || end >= this.mVisibleDeleteItems.size()) {
            Log.e(ANIMATOR_TAG, "updateSavedDeleteItemInfo: index is invalid.");
            return;
        }
        int beforeHeight = 0;
        for (int idx = begin; idx <= end; idx++) {
            this.mVisibleDeleteItems.get(idx).mTotalBeforeHeight = beforeHeight;
            beforeHeight += this.mVisibleDeleteItems.get(idx).mOriginalHeight;
        }
        for (int idx2 = begin; idx2 <= end; idx2++) {
            this.mVisibleDeleteItems.get(idx2).mTotalHeight = beforeHeight;
        }
    }

    private void prepareForHeightAnimator() {
        int begin = -1;
        int end = -1;
        int deleteCountVisible = this.mVisibleDeleteItems.size();
        for (int idx = 0; idx < deleteCountVisible; idx++) {
            if (begin == -1) {
                begin = idx;
                end = idx;
            } else if (this.mVisibleDeleteItems.get(idx).mPosition == this.mVisibleDeleteItems.get(end).mPosition + 1) {
                end++;
            } else {
                updateSavedDeleteItemInfo(begin, end);
                begin = idx;
                end = idx;
            }
        }
        if (begin != -1) {
            updateSavedDeleteItemInfo(begin, end);
        }
    }

    private ValueAnimator.AnimatorUpdateListener getAlphaListener() {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.ListView.AnonymousClass7 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                for (DeleteItemInfo deleteItemInfo : ListView.this.mVisibleDeleteItems) {
                    if (!deleteItemInfo.mIsDeleted) {
                        deleteItemInfo.setCurrentAlpha(value);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSavedDeleteItemInfo(int position) {
        for (DeleteItemInfo deleteItemInfo : this.mVisibleDeleteItems) {
            if (!deleteItemInfo.mIsDeleted && deleteItemInfo.mPosition >= position) {
                if (deleteItemInfo.mPosition == position) {
                    DeleteAnimatorCallback deleteAnimatorCallback = this.mDeleteInterface;
                    if (deleteAnimatorCallback != null) {
                        deleteAnimatorCallback.remove(deleteItemInfo.mItem);
                    }
                    deleteItemInfo.resetSavedView(true);
                } else {
                    deleteItemInfo.resetSavedView(false);
                }
            }
        }
        addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            /* class huawei.android.widget.ListView.AnonymousClass8 */

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ListView.this.getNewViews();
                ListView.this.removeLayoutChangeListener(this);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeLayoutChangeListener(View.OnLayoutChangeListener listener) {
        removeOnLayoutChangeListener(listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getNewViews() {
        int firstPosition = getFirstVisiblePosition();
        for (DeleteItemInfo deleteItemInfo : this.mVisibleDeleteItems) {
            if (!deleteItemInfo.mIsDeleted) {
                deleteItemInfo.refreshItemView(firstPosition);
            }
        }
    }

    private ValueAnimator.AnimatorUpdateListener getHeightListener() {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.ListView.AnonymousClass9 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                int deleteHeight;
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                boolean isDeleteItem = false;
                int totalDeltaY = 0;
                for (DeleteItemInfo deleteItemInfo : ListView.this.mVisibleDeleteItems) {
                    if (!deleteItemInfo.mIsDeleted && (deleteHeight = (int) (((float) deleteItemInfo.mTotalHeight) * value)) >= deleteItemInfo.mTotalBeforeHeight) {
                        if (deleteHeight >= deleteItemInfo.mTotalBeforeHeight + deleteItemInfo.mOriginalHeight) {
                            totalDeltaY += deleteItemInfo.mHeight;
                            ListView.this.updateSavedDeleteItemInfo(deleteItemInfo.mPosition);
                            isDeleteItem = true;
                        } else {
                            int newHeight = deleteItemInfo.mOriginalHeight - (deleteHeight - deleteItemInfo.mTotalBeforeHeight);
                            totalDeltaY += deleteItemInfo.mHeight - newHeight;
                            deleteItemInfo.setCurrentHeight(newHeight, totalDeltaY);
                        }
                    }
                }
                if (isDeleteItem && ListView.this.mDeleteInterface != null) {
                    ListView.this.mDeleteInterface.notifyDataSetChanged();
                }
            }
        };
    }

    @TargetApi(11)
    private void playAnimator() {
        ValueAnimator animatorAlpha = ValueAnimator.ofFloat(1.0f, 0.0f);
        animatorAlpha.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563661));
        animatorAlpha.setDuration(200L);
        animatorAlpha.addUpdateListener(getAlphaListener());
        ValueAnimator animatorHeight = ValueAnimator.ofFloat(0.0f, 1.0f);
        animatorHeight.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563661));
        animatorHeight.setDuration(300L);
        animatorHeight.addUpdateListener(getHeightListener());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorAlpha).with(animatorHeight);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.ListView.AnonymousClass10 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                ListView.this.clearSavedInfo();
                if (ListView.this.mDeleteInterface != null) {
                    ListView.this.mDeleteInterface.notifyDataSetChanged();
                    ListView.this.mDeleteInterface.notifyResult(true);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                boolean isDeleteItem = false;
                for (DeleteItemInfo deleteItemInfo : ListView.this.mVisibleDeleteItems) {
                    if (!deleteItemInfo.mIsDeleted) {
                        if (ListView.this.mDeleteInterface != null) {
                            isDeleteItem = true;
                            ListView.this.mDeleteInterface.remove(deleteItemInfo.mItem);
                        }
                        deleteItemInfo.resetSavedView(true);
                    }
                }
                if (isDeleteItem && ListView.this.mDeleteInterface != null) {
                    ListView.this.mDeleteInterface.notifyDataSetChanged();
                }
            }
        });
        this.mDeleteAnimator = animatorSet;
        this.mDeleteAnimator.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSavedInfo() {
        this.mDeleteCountBeforeVisible = 0;
        this.mDeleteCountAfterVisible = 0;
        this.mDeleteAnimator = null;
        this.mVisibleDeleteItems.clear();
    }

    private void playAnimatorBeforeDelete() {
        prepareBeforeAnimator();
        prepareForHeightAnimator();
        playAnimator();
    }

    @TargetApi(5)
    private void saveDeleteItemInfo(int position, Object item, int firstPos, int lastPos, List<Pair<Boolean, Object>> allItemsFlags) {
        if (position < firstPos) {
            if (((Boolean) allItemsFlags.get(position).first).booleanValue()) {
                Log.w(ANIMATOR_TAG, "saveDeleteItemInfo: repeat delete item");
            } else {
                this.mDeleteCountBeforeVisible++;
            }
        } else if (position <= lastPos) {
            DeleteItemInfo deleteItemInfo = new DeleteItemInfo(position, item);
            if (this.mVisibleDeleteItems.indexOf(deleteItemInfo) == -1) {
                this.mVisibleDeleteItems.add(deleteItemInfo);
            } else {
                Log.w(ANIMATOR_TAG, "saveDeleteItemInfo: repeat delete item");
            }
        } else if (((Boolean) allItemsFlags.get(position).first).booleanValue()) {
            Log.w(ANIMATOR_TAG, "saveDeleteItemInfo: repeat delete item");
        } else {
            this.mDeleteCountAfterVisible++;
        }
        allItemsFlags.set(position, new Pair<>(true, item));
    }

    @TargetApi(5)
    private void deleteItemsProc(List<Pair<Boolean, Object>> positionMap, int firstIdx, int lastIdx, boolean isFirstTime) {
        int positionMapSize = positionMap.size();
        int correctLastIdx = lastIdx;
        if (lastIdx >= positionMapSize) {
            correctLastIdx = positionMapSize - 1;
        }
        for (int position = firstIdx; position <= correctLastIdx; position++) {
            Pair<Boolean, Object> item = positionMap.get(position);
            if (((Boolean) item.first).booleanValue()) {
                if (isFirstTime) {
                    setItemChecked(position, false);
                } else if (item.second == null) {
                    Log.e(ANIMATOR_TAG, "deleteAfterAnimator: saved item is null.");
                } else {
                    this.mDeleteInterface.remove(item.second);
                }
            }
        }
    }

    @TargetApi(5)
    private boolean transmitItemToPosition(List<Object> deleteItems) {
        ListAdapter adapter = getAdapter();
        int itemCount = adapter.getCount();
        int firstPos = getFirstVisiblePosition();
        int lastPos = getLastVisiblePosition();
        List<Pair<Boolean, Object>> allItemsFlags = new ArrayList<>(itemCount);
        for (int idx = 0; idx < adapter.getCount(); idx++) {
            allItemsFlags.add(idx, new Pair<>(false, null));
        }
        for (Object item : deleteItems) {
            int position = this.mDeleteInterface.getItemPosition(item);
            if (position < 0 || position >= itemCount) {
                Log.e(ANIMATOR_TAG, "transmitItemToPosition: position is invalid.");
                return false;
            }
            saveDeleteItemInfo(position, item, firstPos, lastPos, allItemsFlags);
        }
        deleteItemsProc(allItemsFlags, 0, itemCount - 1, true);
        deleteItemsProc(allItemsFlags, lastPos + 1, itemCount - 1, false);
        deleteItemsProc(allItemsFlags, 0, firstPos - 1, false);
        setFirstPosition(firstPos - this.mDeleteCountBeforeVisible);
        if (itemCount != adapter.getCount()) {
            this.mDeleteInterface.notifyDataSetChanged();
        }
        return true;
    }

    public void deleteItemsWithAnimator(List<Object> deleteItems, DeleteAnimatorCallback callback) {
        boolean isRemoveItem;
        if (callback == null) {
            Log.e(ANIMATOR_TAG, "deleteItemsWithAnimator: callback is null.");
        } else if (deleteItems == null || deleteItems.size() == 0) {
            Log.w(ANIMATOR_TAG, "deleteItemsWithAnimator: deleteItems is null.");
            callback.notifyResult(false);
        } else if (this.mVisibleDeleteItems.size() != 0) {
            Log.e(ANIMATOR_TAG, "deleteItemsWithAnimator:last animator has not end.");
        } else if (this.mDeleteCountBeforeVisible > 0 || this.mDeleteCountAfterVisible > 0) {
            Log.e(ANIMATOR_TAG, "deleteItemsWithAnimator:delete count before or after visible items is not zero.");
        } else {
            this.mDeleteInterface = callback;
            if (getAdapter() == null) {
                Log.e(ANIMATOR_TAG, "deleteItemsWithAnimator: adapter is null, set adapter before delete please.");
                this.mDeleteInterface.notifyResult(false);
                return;
            }
            clearSavedInfo();
            if (deleteItems.isEmpty() || !(deleteItems.get(0) instanceof HwPositionPair)) {
                isRemoveItem = transmitItemToPosition(deleteItems);
            } else {
                isRemoveItem = removeItemInPositionRange(deleteItems);
            }
            if (!isRemoveItem) {
                Log.w(ANIMATOR_TAG, "deleteItemsWithAnimator: fail to get items position.");
                clearSavedInfo();
                this.mDeleteInterface.notifyResult(false);
            } else if (this.mVisibleDeleteItems.size() == 0) {
                Log.w(ANIMATOR_TAG, "deleteItemsWithAnimator: no visible item to delete.");
                clearSavedInfo();
                this.mDeleteInterface.notifyResult(true);
            } else {
                playAnimatorBeforeDelete();
            }
        }
    }

    @TargetApi(5)
    private boolean removeItemInPositionRange(List<Object> deletePositionPairs) {
        ListAdapter adapter = getAdapter();
        int itemCount = adapter.getCount();
        int mFirstVisiblePosForDelete = getFirstVisiblePosition();
        int mLastVisiblePosForDelete = getLastVisiblePosition();
        List<HwPositionPair> positionRangeBeforeVisible = new ArrayList<>();
        List<HwPositionPair> positionRangeAfterVisible = new ArrayList<>();
        List<HwPositionPair> positionRangeVisible = new ArrayList<>();
        for (Object obj : deletePositionPairs) {
            if (!(obj instanceof HwPositionPair)) {
                Log.e(ANIMATOR_TAG, "removeItemInPositionRange, obj in deletePositionPairs is not HwPositionPair type");
                return false;
            }
            HwPositionPair positionPair = (HwPositionPair) obj;
            HwPositionPair beforePair = mergePositionPair(positionPair, new HwPositionPair(-1, Integer.valueOf(mFirstVisiblePosForDelete - 1)));
            if (beforePair != null) {
                positionRangeBeforeVisible.add(beforePair);
            }
            HwPositionPair afterPair = mergePositionPair(positionPair, new HwPositionPair(Integer.valueOf(mLastVisiblePosForDelete + 1), Integer.MAX_VALUE));
            if (afterPair != null) {
                positionRangeAfterVisible.add(afterPair);
            }
            HwPositionPair visiblePair = mergePositionPair(positionPair, new HwPositionPair(Integer.valueOf(mFirstVisiblePosForDelete), Integer.valueOf(mLastVisiblePosForDelete)));
            if (visiblePair != null) {
                positionRangeVisible.add(visiblePair);
            }
        }
        this.mDeleteCountAfterVisible += deleteItemInRange(positionRangeAfterVisible);
        this.mDeleteCountBeforeVisible += deleteItemInRange(positionRangeBeforeVisible);
        for (HwPositionPair positionPair2 : positionRangeVisible) {
            for (int position = ((Integer) positionPair2.second).intValue(); position >= ((Integer) positionPair2.first).intValue(); position--) {
                int updatedPosition = position - this.mDeleteCountBeforeVisible;
                this.mVisibleDeleteItems.add(new DeleteItemInfo(position, new HwPositionPair(Integer.valueOf(updatedPosition), Integer.valueOf(updatedPosition))));
            }
        }
        setFirstPosition(mFirstVisiblePosForDelete - this.mDeleteCountBeforeVisible);
        if (itemCount == adapter.getCount()) {
            return true;
        }
        this.mDeleteInterface.notifyDataSetChanged();
        return true;
    }

    @TargetApi(5)
    private int deleteItemInRange(List<HwPositionPair> positionPairList) {
        int deletedItemNum = 0;
        Collections.sort(positionPairList, new Comparator<HwPositionPair>() {
            /* class huawei.android.widget.ListView.AnonymousClass11 */

            public int compare(HwPositionPair o1, HwPositionPair o2) {
                return o2.compareTo(o1);
            }
        });
        for (HwPositionPair positionPair : positionPairList) {
            this.mDeleteInterface.remove(positionPair);
            this.mDeleteInterface.notifyDataSetChanged();
            deletedItemNum += (((Integer) positionPair.second).intValue() - ((Integer) positionPair.first).intValue()) + 1;
        }
        return deletedItemNum;
    }

    @TargetApi(5)
    private HwPositionPair mergePositionPair(HwPositionPair firstPositionPair, HwPositionPair secondPositionPair) {
        int mergePairFirst = ((Integer) (((Integer) firstPositionPair.first).intValue() < ((Integer) secondPositionPair.first).intValue() ? secondPositionPair.first : firstPositionPair.first)).intValue();
        int mergePairSecond = ((Integer) (((Integer) firstPositionPair.second).intValue() < ((Integer) secondPositionPair.second).intValue() ? firstPositionPair.second : secondPositionPair.second)).intValue();
        if (mergePairFirst > mergePairSecond) {
            return null;
        }
        return new HwPositionPair(Integer.valueOf(mergePairFirst), Integer.valueOf(mergePairSecond));
    }

    public void stopDeleteAnimator() {
        Animator animator = this.mDeleteAnimator;
        if (animator != null) {
            animator.cancel();
        }
    }

    /* access modifiers changed from: protected */
    public boolean needDrawThisDivider(int childIndex, Drawable divider, boolean isBeforeDraw) {
        if (this.mVisibleDeleteItems.size() == 0) {
            return true;
        }
        if (!isBeforeDraw) {
            divider.setAlpha(this.mDividerAlphaOld);
            return true;
        }
        this.mDividerAlphaOld = divider.getAlpha();
        int firstPosition = getFirstVisiblePosition();
        for (DeleteItemInfo deleteItemInfo : this.mVisibleDeleteItems) {
            if (!deleteItemInfo.mIsDeleted && childIndex == deleteItemInfo.mPosition - firstPosition) {
                divider.setAlpha((int) (deleteItemInfo.mAlpha * 255.0f));
                return true;
            }
        }
        return true;
    }

    @Override // android.view.View
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        boolean isFlingConsumed = super.dispatchNestedPreFling(velocityX, velocityY);
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null && !isFlingConsumed) {
            listViewFlingCoordinator.startScrollerOnFling(isFlingConsumed, (int) velocityY);
        }
        return isFlingConsumed;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedOverFlingMoreAtEdge(boolean isScrollerRunning, boolean isAtEnd, int overshoot, int deltaY) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isNeedOverFlingMoreAtEdge(isAtEnd, overshoot, deltaY);
        }
        return super.isNeedOverFlingMoreAtEdge(isScrollerRunning, isAtEnd, overshoot, deltaY);
    }

    @Override // android.view.View
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumeds, int[] offsetInWindows) {
        if (this.mListViewFlingCoordinator != null) {
            setEnabledOfNestedPreScroll();
        }
        return super.dispatchNestedPreScroll(dx, dy, consumeds, offsetInWindows);
    }

    @Override // android.view.View
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindows) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator == null) {
            return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindows);
        }
        setNestedScrollingEnabledLocal(listViewFlingCoordinator.checkNestedScrollEnabled(dyUnconsumed));
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindows) && this.mListViewFlingCoordinator.getHeaderScrollViewHeight() != this.mListViewFlingCoordinator.getHeaderScrollViewHeight();
    }

    /* access modifiers changed from: protected */
    public boolean isNeedFlingOnTop() {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isNeedFlingOnTop();
        }
        return super.isNeedFlingOnTop();
    }

    /* access modifiers changed from: protected */
    public boolean canOverScroll(int directionY) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isOverScrollEnabled(directionY);
        }
        return super.canOverScroll(directionY);
    }

    private void initListViewFlingCoordinator() {
        if (new HeaderScrollViewStatusChecker(this).getScrollingViewStatus() != -2 && getOverScrollMode() != 2 && isNestedScrollingEnabled()) {
            this.mListViewFlingCoordinator = new ListViewFlingCoordinator(this);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            listViewFlingCoordinator.onInterceptTouchEvent(motionEvent);
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public void setNestedScrollingEnabled(boolean isEnabled) {
        if (this.mListViewFlingCoordinator == null || !isAttachedToWindow() || this.mIsLocalSetting) {
            super.setNestedScrollingEnabled(isEnabled);
        }
    }

    private void setNestedScrollingEnabledLocal(boolean isEnabled) {
        this.mIsLocalSetting = true;
        setNestedScrollingEnabled(isEnabled);
        this.mIsLocalSetting = false;
    }

    private void setEnabledOfNestedPreScroll() {
        boolean isNestedPreScrollEnabled = this.mListViewFlingCoordinator.isNestedPreScrollEnabled();
        setNestedScrollingEnabledLocal(isNestedPreScrollEnabled);
        if (isNestedPreScrollEnabled) {
            startNestedScroll(2);
        }
    }
}
