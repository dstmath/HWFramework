package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.text.TextUtils;
import android.util.Size;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Preconditions;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

public final class FloatingToolbar {
    public static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final OnMenuItemClickListener NO_OP_MENUITEM_CLICK_LISTENER = null;
    private static final boolean[] mHwFloatingToolbarFlag = null;
    private static int mHwFloatingToolbarHeightBias;
    private final Rect mContentRect;
    private final Context mContext;
    private Menu mMenu;
    private OnMenuItemClickListener mMenuItemClickListener;
    private final OnLayoutChangeListener mOrientationChangeHandler;
    private final FloatingToolbarPopup mPopup;
    private final Rect mPreviousContentRect;
    private List<Object> mShowingMenuItems;
    private int mSuggestedWidth;
    private boolean mWidthChanged;
    private final Window mWindow;

    private static final class FloatingToolbarPopup {
        private static final int MAX_OVERFLOW_SIZE = 4;
        private static final int MIN_OVERFLOW_SIZE = 2;
        private final Drawable mArrow;
        private final AnimationSet mCloseOverflowAnimation;
        private final ViewGroup mContentContainer;
        private final Context mContext;
        private final Point mCoordsOnWindow;
        private final AnimatorSet mDismissAnimation;
        private boolean mDismissed;
        private final Interpolator mFastOutLinearInInterpolator;
        private final Interpolator mFastOutSlowInInterpolator;
        private boolean mHidden;
        private final AnimatorSet mHideAnimation;
        private final OnComputeInternalInsetsListener mInsetsComputer;
        private boolean mIsOverflowOpen;
        private final Interpolator mLinearOutSlowInInterpolator;
        private final Interpolator mLogAccelerateInterpolator;
        private final ViewGroup mMainPanel;
        private Size mMainPanelSize;
        private final int mMarginHorizontal;
        private final int mMarginVertical;
        private final OnClickListener mMenuItemButtonOnClickListener;
        private OnMenuItemClickListener mOnMenuItemClickListener;
        private final AnimationSet mOpenOverflowAnimation;
        private boolean mOpenOverflowUpwards;
        private final Drawable mOverflow;
        private final AnimationListener mOverflowAnimationListener;
        private final ImageButton mOverflowButton;
        private final Size mOverflowButtonSize;
        private final OverflowPanel mOverflowPanel;
        private Size mOverflowPanelSize;
        private final OverflowPanelViewHelper mOverflowPanelViewHelper;
        private final View mParent;
        private final PopupWindow mPopupWindow;
        private final Runnable mPreparePopupContentRTLHelper;
        private final AnimatorSet mShowAnimation;
        private final int[] mTmpCoords;
        private final Rect mTmpRect;
        private final AnimatedVectorDrawable mToArrow;
        private final AnimatedVectorDrawable mToOverflow;
        private final Region mTouchableRegion;
        private int mTransitionDurationScale;
        private final Rect mViewPortOnScreen;

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10 */
        class AnonymousClass10 extends Animation {
            final /* synthetic */ float val$bottom;
            final /* synthetic */ int val$startHeight;
            final /* synthetic */ int val$targetHeight;

            AnonymousClass10(int val$targetHeight, int val$startHeight, float val$bottom) {
                this.val$targetHeight = val$targetHeight;
                this.val$startHeight = val$startHeight;
                this.val$bottom = val$bottom;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, this.val$startHeight + ((int) (((float) (this.val$targetHeight - this.val$startHeight)) * interpolatedTime)));
                if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                    FloatingToolbarPopup.this.mContentContainer.setY(this.val$bottom - ((float) FloatingToolbarPopup.this.mContentContainer.getHeight()));
                    FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                }
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11 */
        class AnonymousClass11 extends Animation {
            final /* synthetic */ float val$overflowButtonStartX;
            final /* synthetic */ float val$overflowButtonTargetX;
            final /* synthetic */ int val$startWidth;

            AnonymousClass11(float val$overflowButtonStartX, float val$overflowButtonTargetX, int val$startWidth) {
                this.val$overflowButtonStartX = val$overflowButtonStartX;
                this.val$overflowButtonTargetX = val$overflowButtonTargetX;
                this.val$startWidth = val$startWidth;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int i;
                float overflowButtonX = this.val$overflowButtonStartX + ((this.val$overflowButtonTargetX - this.val$overflowButtonStartX) * interpolatedTime);
                if (FloatingToolbarPopup.this.isRTL()) {
                    i = 0;
                } else {
                    i = FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$startWidth;
                }
                FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.12 */
        class AnonymousClass12 extends LinearLayout {
            AnonymousClass12(Context $anonymous0) {
                super($anonymous0);
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (FloatingToolbarPopup.this.isOverflowAnimating()) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(FloatingToolbarPopup.this.mMainPanelSize.getWidth(), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return FloatingToolbarPopup.this.isOverflowAnimating();
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.13 */
        class AnonymousClass13 implements OnClickListener {
            final /* synthetic */ ImageButton val$overflowButton;

            AnonymousClass13(ImageButton val$overflowButton) {
                this.val$overflowButton = val$overflowButton;
            }

            public void onClick(View v) {
                if (FloatingToolbarPopup.this.mIsOverflowOpen) {
                    this.val$overflowButton.setImageDrawable(FloatingToolbarPopup.this.mToOverflow);
                    FloatingToolbarPopup.this.mToOverflow.start();
                    FloatingToolbarPopup.this.closeOverflow();
                    return;
                }
                this.val$overflowButton.setImageDrawable(FloatingToolbarPopup.this.mToArrow);
                FloatingToolbarPopup.this.mToArrow.start();
                FloatingToolbarPopup.this.openOverflow();
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.14 */
        class AnonymousClass14 extends ArrayAdapter<MenuItem> {
            AnonymousClass14(Context $anonymous0, int $anonymous1) {
                super($anonymous0, $anonymous1);
            }

            public int getViewTypeCount() {
                return FloatingToolbarPopup.this.mOverflowPanelViewHelper.getViewTypeCount();
            }

            public int getItemViewType(int position) {
                return FloatingToolbarPopup.this.mOverflowPanelViewHelper.getItemViewType((MenuItem) getItem(position));
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                return FloatingToolbarPopup.this.mOverflowPanelViewHelper.getView((MenuItem) getItem(position), FloatingToolbarPopup.this.mOverflowPanelSize.getWidth(), convertView);
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.15 */
        class AnonymousClass15 implements OnItemClickListener {
            final /* synthetic */ OverflowPanel val$overflowPanel;

            AnonymousClass15(OverflowPanel val$overflowPanel) {
                this.val$overflowPanel = val$overflowPanel;
            }

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MenuItem menuItem = (MenuItem) this.val$overflowPanel.getAdapter().getItem(position);
                if (FloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                    FloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick(menuItem);
                }
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6 */
        class AnonymousClass6 extends Animation {
            final /* synthetic */ float val$left;
            final /* synthetic */ float val$right;
            final /* synthetic */ int val$startWidth;
            final /* synthetic */ int val$targetWidth;

            AnonymousClass6(int val$targetWidth, int val$startWidth, float val$left, float val$right) {
                this.val$targetWidth = val$targetWidth;
                this.val$startWidth = val$startWidth;
                this.val$left = val$left;
                this.val$right = val$right;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, this.val$startWidth + ((int) (((float) (this.val$targetWidth - this.val$startWidth)) * interpolatedTime)));
                if (FloatingToolbarPopup.this.isRTL()) {
                    FloatingToolbarPopup.this.mContentContainer.setX(this.val$left);
                    FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                    FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                    return;
                }
                FloatingToolbarPopup.this.mContentContainer.setX(this.val$right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$startWidth));
                FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$targetWidth));
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7 */
        class AnonymousClass7 extends Animation {
            final /* synthetic */ int val$startHeight;
            final /* synthetic */ float val$startY;
            final /* synthetic */ int val$targetHeight;

            AnonymousClass7(int val$targetHeight, int val$startHeight, float val$startY) {
                this.val$targetHeight = val$targetHeight;
                this.val$startHeight = val$startHeight;
                this.val$startY = val$startY;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, this.val$startHeight + ((int) (((float) (this.val$targetHeight - this.val$startHeight)) * interpolatedTime)));
                if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                    FloatingToolbarPopup.this.mContentContainer.setY(this.val$startY - ((float) (FloatingToolbarPopup.this.mContentContainer.getHeight() - this.val$startHeight)));
                    FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                }
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8 */
        class AnonymousClass8 extends Animation {
            final /* synthetic */ float val$overflowButtonStartX;
            final /* synthetic */ float val$overflowButtonTargetX;
            final /* synthetic */ int val$startWidth;

            AnonymousClass8(float val$overflowButtonStartX, float val$overflowButtonTargetX, int val$startWidth) {
                this.val$overflowButtonStartX = val$overflowButtonStartX;
                this.val$overflowButtonTargetX = val$overflowButtonTargetX;
                this.val$startWidth = val$startWidth;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int i;
                float overflowButtonX = this.val$overflowButtonStartX + ((this.val$overflowButtonTargetX - this.val$overflowButtonStartX) * interpolatedTime);
                if (FloatingToolbarPopup.this.isRTL()) {
                    i = 0;
                } else {
                    i = FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$startWidth;
                }
                FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9 */
        class AnonymousClass9 extends Animation {
            final /* synthetic */ float val$left;
            final /* synthetic */ float val$right;
            final /* synthetic */ int val$startWidth;
            final /* synthetic */ int val$targetWidth;

            AnonymousClass9(int val$targetWidth, int val$startWidth, float val$left, float val$right) {
                this.val$targetWidth = val$targetWidth;
                this.val$startWidth = val$startWidth;
                this.val$left = val$left;
                this.val$right = val$right;
            }

            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, this.val$startWidth + ((int) (((float) (this.val$targetWidth - this.val$startWidth)) * interpolatedTime)));
                if (FloatingToolbarPopup.this.isRTL()) {
                    FloatingToolbarPopup.this.mContentContainer.setX(this.val$left);
                    FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                    FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                    return;
                }
                FloatingToolbarPopup.this.mContentContainer.setX(this.val$right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$targetWidth));
                FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - this.val$startWidth));
            }
        }

        private static final class LogAccelerateInterpolator implements Interpolator {
            private static final int BASE = 100;
            private static final float LOGS_SCALE = 0.0f;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void");
            }

            private LogAccelerateInterpolator() {
            }

            private static float computeLog(float t, int base) {
                return (float) (1.0d - Math.pow((double) base, (double) (-t)));
            }

            public float getInterpolation(float t) {
                return LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (computeLog(LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t, BASE) * LOGS_SCALE);
            }
        }

        private static final class OverflowPanel extends ListView {
            private final FloatingToolbarPopup mPopup;

            OverflowPanel(FloatingToolbarPopup popup) {
                super(((FloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
                this.mPopup = popup;
                setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
                setScrollIndicators(3);
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), EditorInfo.IME_FLAG_NO_ENTER_ACTION));
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (this.mPopup.isOverflowAnimating()) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            protected boolean awakenScrollBars() {
                return super.awakenScrollBars();
            }
        }

        private static final class OverflowPanelViewHelper {
            private static final int NUM_OF_VIEW_TYPES = 2;
            private static final int VIEW_TYPE_ICON_ONLY = 1;
            private static final int VIEW_TYPE_STRING_TITLE = 0;
            private final Context mContext;
            private final View mIconOnlyViewCalculator;
            private final TextView mStringTitleViewCalculator;

            public OverflowPanelViewHelper(Context context) {
                this.mContext = (Context) Preconditions.checkNotNull(context);
                this.mStringTitleViewCalculator = getStringTitleView(null, 0, null);
                this.mIconOnlyViewCalculator = getIconOnlyView(null, 0, null);
            }

            public int getViewTypeCount() {
                return NUM_OF_VIEW_TYPES;
            }

            public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
                Preconditions.checkNotNull(menuItem);
                if (getItemViewType(menuItem) == VIEW_TYPE_ICON_ONLY) {
                    return getIconOnlyView(menuItem, minimumWidth, convertView);
                }
                return getStringTitleView(menuItem, minimumWidth, convertView);
            }

            public int getItemViewType(MenuItem menuItem) {
                Preconditions.checkNotNull(menuItem);
                if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                    return VIEW_TYPE_ICON_ONLY;
                }
                return 0;
            }

            public int calculateWidth(MenuItem menuItem) {
                View calculator;
                if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                    ((ImageView) this.mIconOnlyViewCalculator.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
                    calculator = this.mIconOnlyViewCalculator;
                } else {
                    this.mStringTitleViewCalculator.setText(menuItem.getTitle());
                    calculator = this.mStringTitleViewCalculator;
                }
                calculator.measure(0, 0);
                return calculator.getMeasuredWidth();
            }

            private TextView getStringTitleView(MenuItem menuItem, int minimumWidth, View convertView) {
                TextView menuButton;
                if (convertView != null) {
                    menuButton = (TextView) convertView;
                } else {
                    menuButton = (TextView) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_list_item, null);
                    menuButton.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
                }
                if (menuItem != null) {
                    FloatingToolbar.setMenuItemButtonInputType(menuButton);
                    menuButton.setText(menuItem.getTitle());
                    menuButton.setContentDescription(menuItem.getTitle());
                    menuButton.setMinimumWidth(minimumWidth);
                }
                return menuButton;
            }

            private View getIconOnlyView(MenuItem menuItem, int minimumWidth, View convertView) {
                View menuButton;
                if (convertView != null) {
                    menuButton = convertView;
                } else {
                    menuButton = LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_image_list_item, null);
                    menuButton.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
                }
                if (menuItem != null) {
                    ((ImageView) menuButton.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
                    menuButton.setMinimumWidth(minimumWidth);
                }
                return menuButton;
            }
        }

        public FloatingToolbarPopup(Context context, View parent) {
            this.mViewPortOnScreen = new Rect();
            this.mCoordsOnWindow = new Point();
            this.mTmpCoords = new int[MIN_OVERFLOW_SIZE];
            this.mTmpRect = new Rect();
            this.mTouchableRegion = new Region();
            this.mInsetsComputer = new OnComputeInternalInsetsListener() {
                public void onComputeInternalInsets(InternalInsetsInfo info) {
                    info.contentInsets.setEmpty();
                    info.visibleInsets.setEmpty();
                    info.touchableRegion.set(FloatingToolbarPopup.this.mTouchableRegion);
                    info.setTouchableInsets(3);
                }
            };
            this.mPreparePopupContentRTLHelper = new Runnable() {
                public void run() {
                    FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                    FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                    FloatingToolbarPopup.this.mContentContainer.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                }
            };
            this.mDismissed = true;
            this.mMenuItemButtonOnClickListener = new OnClickListener() {
                public void onClick(View v) {
                    if ((v.getTag() instanceof MenuItem) && FloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                        FloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
                    }
                }
            };
            this.mParent = (View) Preconditions.checkNotNull(parent);
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mContentContainer = FloatingToolbar.createContentContainer(context);
            this.mPopupWindow = FloatingToolbar.createPopupWindow(this.mContentContainer);
            this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin);
            this.mMarginVertical = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_vertical_margin);
            FloatingToolbar.mHwFloatingToolbarHeightBias = FloatingToolbar.getFloatingToolbarHeightBias(this.mContext);
            this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_slow_in);
            this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.linear_out_slow_in);
            this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_linear_in);
            this.mArrow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow, this.mContext.getTheme());
            this.mArrow.setAutoMirrored(true);
            this.mOverflow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow, this.mContext.getTheme());
            this.mOverflow.setAutoMirrored(true);
            this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, this.mContext.getTheme());
            this.mToArrow.setAutoMirrored(true);
            this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, this.mContext.getTheme());
            this.mToOverflow.setAutoMirrored(true);
            this.mOverflowButton = createOverflowButton();
            this.mOverflowButtonSize = measure(this.mOverflowButton);
            this.mMainPanel = createMainPanel();
            this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext);
            this.mOverflowPanel = createOverflowPanel();
            this.mOverflowAnimationListener = createOverflowAnimationListener();
            this.mOpenOverflowAnimation = new AnimationSet(true);
            this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mCloseOverflowAnimation = new AnimationSet(true);
            this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mShowAnimation = FloatingToolbar.createEnterAnimation(this.mContentContainer);
            this.mDismissAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                    FloatingToolbarPopup.this.mContentContainer.removeAllViews();
                }
            });
            this.mHideAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                }
            });
        }

        public void layoutMenuItems(List<MenuItem> menuItems, OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
            this.mOnMenuItemClickListener = menuItemClickListener;
            cancelOverflowAnimations();
            clearPanels();
            menuItems = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
            if (!menuItems.isEmpty()) {
                layoutOverflowPanelItems(menuItems);
            }
            updatePopupSize();
        }

        public void show(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (!isShowing()) {
                this.mHidden = false;
                this.mDismissed = false;
                cancelDismissAndHideAnimations();
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.showAtLocation(this.mParent, 0, this.mCoordsOnWindow.x, this.mCoordsOnWindow.y);
                setTouchableSurfaceInsetsComputer();
                runShowAnimation();
            }
        }

        public void dismiss() {
            if (!this.mDismissed) {
                this.mHidden = false;
                this.mDismissed = true;
                this.mHideAnimation.cancel();
                runDismissAnimation();
                setZeroTouchableSurface();
            }
        }

        public void hide() {
            if (isShowing()) {
                this.mHidden = true;
                runHideAnimation();
                setZeroTouchableSurface();
            }
        }

        public boolean isShowing() {
            return (this.mDismissed || this.mHidden) ? false : true;
        }

        public boolean isHidden() {
            return this.mHidden;
        }

        public void updateCoordinates(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (isShowing() && this.mPopupWindow.isShowing()) {
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.update(this.mCoordsOnWindow.x, this.mCoordsOnWindow.y, this.mPopupWindow.getWidth(), this.mPopupWindow.getHeight());
            }
        }

        private void refreshCoordinatesAndOverflowDirection(Rect contentRectOnScreen) {
            int y;
            refreshViewPort();
            int x = Math.min(contentRectOnScreen.centerX() - (this.mPopupWindow.getWidth() / MIN_OVERFLOW_SIZE), this.mViewPortOnScreen.right - this.mPopupWindow.getWidth());
            int statusBarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            int i = this.mViewPortOnScreen.top;
            if (r0 < statusBarHeight) {
                this.mViewPortOnScreen.top = statusBarHeight;
            }
            int availableHeightAboveContent = contentRectOnScreen.top - this.mViewPortOnScreen.top;
            int availableHeightBelowContent = this.mViewPortOnScreen.bottom - contentRectOnScreen.bottom;
            int margin = this.mMarginVertical * MIN_OVERFLOW_SIZE;
            int toolbarHeightWithVerticalMargin = getLineHeight(this.mContext) + margin;
            if (hasOverflow()) {
                int minimumOverflowHeightWithMargin = calculateOverflowHeight(MIN_OVERFLOW_SIZE) + margin;
                int availableHeightThroughContentDown = (this.mViewPortOnScreen.bottom - contentRectOnScreen.top) + toolbarHeightWithVerticalMargin;
                int availableHeightThroughContentUp = (contentRectOnScreen.bottom - this.mViewPortOnScreen.top) + toolbarHeightWithVerticalMargin;
                if (availableHeightAboveContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightAboveContent - margin);
                    y = contentRectOnScreen.top - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin && availableHeightThroughContentDown >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightThroughContentDown - margin);
                    y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightBelowContent - margin);
                    y = contentRectOnScreen.bottom;
                    this.mOpenOverflowUpwards = false;
                } else {
                    if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
                        if (this.mViewPortOnScreen.height() >= minimumOverflowHeightWithMargin) {
                            updateOverflowHeight(availableHeightThroughContentUp - margin);
                            y = (contentRectOnScreen.bottom + toolbarHeightWithVerticalMargin) - this.mPopupWindow.getHeight();
                            this.mOpenOverflowUpwards = true;
                        }
                    }
                    updateOverflowHeight(this.mViewPortOnScreen.height() - margin);
                    y = this.mViewPortOnScreen.top;
                    this.mOpenOverflowUpwards = false;
                }
            } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
            } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.bottom;
            } else {
                if (availableHeightBelowContent >= getLineHeight(this.mContext)) {
                    y = contentRectOnScreen.bottom - this.mMarginVertical;
                } else {
                    y = Math.max(this.mViewPortOnScreen.top, contentRectOnScreen.top - toolbarHeightWithVerticalMargin);
                }
            }
            View view = this.mParent;
            r0.getRootView().getLocationOnScreen(this.mTmpCoords);
            int rootViewLeftOnScreen = this.mTmpCoords[0];
            int rootViewTopOnScreen = this.mTmpCoords[1];
            view = this.mParent;
            r0.getRootView().getLocationInWindow(this.mTmpCoords);
            int rootViewLeftOnWindow = this.mTmpCoords[0];
            int windowLeftOnScreen = rootViewLeftOnScreen - rootViewLeftOnWindow;
            int windowTopOnScreen = rootViewTopOnScreen - this.mTmpCoords[1];
            this.mCoordsOnWindow.set(Math.max(0, x - windowLeftOnScreen), y - windowTopOnScreen);
        }

        private void runShowAnimation() {
            this.mShowAnimation.start();
        }

        private void runDismissAnimation() {
            this.mDismissAnimation.start();
        }

        private void runHideAnimation() {
            this.mHideAnimation.start();
        }

        private void cancelDismissAndHideAnimations() {
            this.mDismissAnimation.cancel();
            this.mHideAnimation.cancel();
        }

        private void cancelOverflowAnimations() {
            this.mContentContainer.clearAnimation();
            this.mMainPanel.animate().cancel();
            this.mOverflowPanel.animate().cancel();
            this.mToArrow.stop();
            this.mToOverflow.stop();
        }

        private void openOverflow() {
            float overflowButtonTargetX;
            int targetWidth = this.mOverflowPanelSize.getWidth();
            int targetHeight = this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias;
            int startWidth = this.mContentContainer.getWidth();
            int startHeight = this.mContentContainer.getHeight();
            float startY = this.mContentContainer.getY();
            float left = this.mContentContainer.getX();
            Animation widthAnimation = new AnonymousClass6(targetWidth, startWidth, left, left + ((float) this.mContentContainer.getWidth()));
            Animation heightAnimation = new AnonymousClass7(targetHeight, startHeight, startY);
            float overflowButtonStartX = this.mOverflowButton.getX();
            if (isRTL()) {
                overflowButtonTargetX = (((float) targetWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
            } else {
                overflowButtonTargetX = (overflowButtonStartX - ((float) targetWidth)) + ((float) this.mOverflowButton.getWidth());
            }
            Animation overflowButtonAnimation = new AnonymousClass8(overflowButtonStartX, overflowButtonTargetX, startWidth);
            widthAnimation.setInterpolator(this.mLogAccelerateInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            heightAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            heightAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.addAnimation(widthAnimation);
            this.mOpenOverflowAnimation.addAnimation(heightAnimation);
            this.mOpenOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mOpenOverflowAnimation);
            this.mIsOverflowOpen = true;
            this.mMainPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(250).start();
            this.mOverflowPanel.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }

        private void closeOverflow() {
            float overflowButtonTargetX;
            int targetWidth = this.mMainPanelSize.getWidth();
            int startWidth = this.mContentContainer.getWidth();
            float left = this.mContentContainer.getX();
            Animation widthAnimation = new AnonymousClass9(targetWidth, startWidth, left, left + ((float) this.mContentContainer.getWidth()));
            Animation heightAnimation = new AnonymousClass10(this.mMainPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias, this.mContentContainer.getHeight(), this.mContentContainer.getY() + ((float) this.mContentContainer.getHeight()));
            float overflowButtonStartX = this.mOverflowButton.getX();
            if (isRTL()) {
                overflowButtonTargetX = (overflowButtonStartX - ((float) startWidth)) + ((float) this.mOverflowButton.getWidth());
            } else {
                overflowButtonTargetX = (((float) startWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
            }
            Animation overflowButtonAnimation = new AnonymousClass11(overflowButtonStartX, overflowButtonTargetX, startWidth);
            widthAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            heightAnimation.setInterpolator(this.mLogAccelerateInterpolator);
            heightAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(MetricsEvent.FINGERPRINT_ENROLL_ONBOARD_SETUP));
            this.mCloseOverflowAnimation.getAnimations().clear();
            this.mCloseOverflowAnimation.addAnimation(widthAnimation);
            this.mCloseOverflowAnimation.addAnimation(heightAnimation);
            this.mCloseOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mCloseOverflowAnimation);
            this.mIsOverflowOpen = false;
            this.mMainPanel.animate().alpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).withLayer().setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).start();
            this.mOverflowPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(150).start();
        }

        private void setPanelsStatesAtRestingPosition() {
            this.mOverflowButton.setEnabled(true);
            this.mOverflowPanel.awakenScrollBars();
            Size containerSize;
            if (this.mIsOverflowOpen) {
                containerSize = new Size(this.mOverflowPanelSize.getWidth(), this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
                setSize(this.mContentContainer, containerSize);
                this.mMainPanel.setAlpha(0.0f);
                this.mMainPanel.setVisibility(MAX_OVERFLOW_SIZE);
                this.mOverflowPanel.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                this.mOverflowPanel.setVisibility(0);
                this.mOverflowButton.setImageDrawable(this.mArrow);
                this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_close_overflow_description));
                if (isRTL()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(-this.mContentContainer.getX());
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) this.mMarginVertical);
                    this.mMainPanel.setY((float) (containerSize.getHeight() - this.mContentContainer.getHeight()));
                    this.mOverflowButton.setY((float) ((containerSize.getHeight() - this.mOverflowButtonSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                    this.mOverflowPanel.setY(0.0f);
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            containerSize = new Size(this.mMainPanelSize.getWidth(), this.mMainPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
            setSize(this.mContentContainer, containerSize);
            this.mMainPanel.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            this.mMainPanel.setVisibility(0);
            this.mOverflowPanel.setAlpha(0.0f);
            this.mOverflowPanel.setVisibility(MAX_OVERFLOW_SIZE);
            this.mOverflowButton.setImageDrawable(this.mOverflow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_open_overflow_description));
            if (hasOverflow()) {
                if (isRTL()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX((float) (containerSize.getWidth() - this.mOverflowPanelSize.getWidth()));
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) (((this.mMarginVertical + this.mOverflowPanelSize.getHeight()) - containerSize.getHeight()) + FloatingToolbar.mHwFloatingToolbarHeightBias));
                    this.mMainPanel.setY(0.0f);
                    this.mOverflowButton.setY(0.0f);
                    this.mOverflowPanel.setY((float) (containerSize.getHeight() - this.mOverflowPanelSize.getHeight()));
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            this.mContentContainer.setX((float) this.mMarginHorizontal);
            this.mContentContainer.setY((float) this.mMarginVertical);
            this.mMainPanel.setX(0.0f);
            this.mMainPanel.setY(0.0f);
        }

        private void updateOverflowHeight(int suggestedHeight) {
            if (hasOverflow()) {
                int newHeight = calculateOverflowHeight((suggestedHeight - this.mOverflowButtonSize.getHeight()) / getLineHeight(this.mContext));
                if (this.mOverflowPanelSize.getHeight() != newHeight) {
                    this.mOverflowPanelSize = new Size(this.mOverflowPanelSize.getWidth(), newHeight);
                }
                setSize(this.mOverflowPanel, this.mOverflowPanelSize);
                if (this.mIsOverflowOpen) {
                    setSize(this.mContentContainer, this.mOverflowPanelSize);
                    if (this.mOpenOverflowUpwards) {
                        int deltaHeight = this.mOverflowPanelSize.getHeight() - newHeight;
                        this.mContentContainer.setY(this.mContentContainer.getY() + ((float) deltaHeight));
                        this.mOverflowButton.setY(this.mOverflowButton.getY() - ((float) deltaHeight));
                    }
                } else {
                    setSize(this.mContentContainer, this.mMainPanelSize);
                }
                updatePopupSize();
            }
        }

        private void updatePopupSize() {
            int width = 0;
            int height = 0;
            if (this.mMainPanelSize != null) {
                width = Math.max(0, this.mMainPanelSize.getWidth());
                height = Math.max(0, this.mMainPanelSize.getHeight());
            }
            if (this.mOverflowPanelSize != null) {
                width = Math.max(width, this.mOverflowPanelSize.getWidth());
                height = Math.max(height, this.mOverflowPanelSize.getHeight());
            }
            this.mPopupWindow.setWidth((this.mMarginHorizontal * MIN_OVERFLOW_SIZE) + width);
            this.mPopupWindow.setHeight((this.mMarginVertical * MIN_OVERFLOW_SIZE) + height);
            maybeComputeTransitionDurationScale();
        }

        private void refreshViewPort() {
            this.mParent.getWindowVisibleDisplayFrame(this.mViewPortOnScreen);
            FloatingToolbar.adjustWindowVisibleDisplayFrame(this.mViewPortOnScreen);
        }

        private int getAdjustedToolbarWidth(int suggestedWidth) {
            int width = suggestedWidth;
            refreshViewPort();
            int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin) * MIN_OVERFLOW_SIZE);
            if (suggestedWidth <= 0) {
                width = this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_preferred_width);
            }
            return Math.min(width, maximumWidth);
        }

        private void setZeroTouchableSurface() {
            this.mTouchableRegion.setEmpty();
        }

        private void setContentAreaAsTouchableSurface() {
            int width;
            int height;
            Preconditions.checkNotNull(this.mMainPanelSize);
            if (this.mIsOverflowOpen) {
                Preconditions.checkNotNull(this.mOverflowPanelSize);
                width = this.mOverflowPanelSize.getWidth();
                height = this.mOverflowPanelSize.getHeight();
            } else {
                width = this.mMainPanelSize.getWidth();
                height = this.mMainPanelSize.getHeight();
            }
            this.mTouchableRegion.set((int) this.mContentContainer.getX(), (int) this.mContentContainer.getY(), ((int) this.mContentContainer.getX()) + width, ((int) this.mContentContainer.getY()) + height);
        }

        private void setTouchableSurfaceInsetsComputer() {
            ViewTreeObserver viewTreeObserver = this.mPopupWindow.getContentView().getRootView().getViewTreeObserver();
            viewTreeObserver.removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            viewTreeObserver.addOnComputeInternalInsetsListener(this.mInsetsComputer);
        }

        private boolean isRTL() {
            return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
        }

        private boolean hasOverflow() {
            return this.mOverflowPanelSize != null;
        }

        public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
            Preconditions.checkNotNull(menuItems);
            int availableWidth = toolbarWidth;
            LinkedList<MenuItem> remainingMenuItems = new LinkedList(menuItems);
            this.mMainPanel.removeAllViews();
            this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
            boolean isFirstItem = true;
            while (!remainingMenuItems.isEmpty()) {
                MenuItem menuItem = (MenuItem) remainingMenuItems.peek();
                View menuItemButton = FloatingToolbar.createMenuItemButton(this.mContext, menuItem);
                if (isFirstItem) {
                    if (remainingMenuItems.size() == 1) {
                        menuItemButton.setBackgroundResource(androidhwext.R.drawable.item_background_material_light_single_emui);
                    } else if (isRTL()) {
                        menuItemButton.setBackgroundResource(androidhwext.R.drawable.item_background_material_light_right_emui);
                    } else {
                        menuItemButton.setBackgroundResource(androidhwext.R.drawable.item_background_material_light_left_emui);
                    }
                } else if (remainingMenuItems.size() == 1) {
                    if (isRTL()) {
                        menuItemButton.setBackgroundResource(androidhwext.R.drawable.item_background_material_light_left_emui);
                    } else {
                        menuItemButton.setBackgroundResource(androidhwext.R.drawable.item_background_material_light_right_emui);
                    }
                }
                if (isFirstItem) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart() * MAX_OVERFLOW_SIZE, menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                    isFirstItem = false;
                }
                if (remainingMenuItems.size() == 1) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd() * MAX_OVERFLOW_SIZE, menuItemButton.getPaddingBottom());
                }
                menuItemButton.measure(0, 0);
                int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
                boolean canFitWithOverflow = menuItemButtonWidth <= availableWidth - this.mOverflowButtonSize.getWidth();
                boolean canFitNoOverflow = remainingMenuItems.size() == 1 && menuItemButtonWidth <= availableWidth;
                if (!canFitWithOverflow && !canFitNoOverflow) {
                    this.mMainPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
                    break;
                }
                setButtonTagAndClickListener(menuItemButton, menuItem);
                this.mMainPanel.addView(menuItemButton);
                ViewGroup.LayoutParams params = menuItemButton.getLayoutParams();
                params.width = menuItemButtonWidth;
                menuItemButton.setLayoutParams(params);
                availableWidth -= menuItemButtonWidth;
                remainingMenuItems.pop();
            }
            this.mMainPanelSize = measure(this.mMainPanel);
            return remainingMenuItems;
        }

        private void layoutOverflowPanelItems(List<MenuItem> menuItems) {
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            int size = menuItems.size();
            for (int i = 0; i < size; i++) {
                overflowPanelAdapter.add((MenuItem) menuItems.get(i));
            }
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            if (this.mOpenOverflowUpwards) {
                this.mOverflowPanel.setY(0.0f);
            } else {
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
            }
            this.mOverflowPanelSize = new Size(Math.max(getOverflowWidth(), this.mOverflowButtonSize.getWidth()), calculateOverflowHeight(MAX_OVERFLOW_SIZE));
            setSize(this.mOverflowPanel, this.mOverflowPanelSize);
        }

        private void preparePopupContent() {
            this.mContentContainer.removeAllViews();
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowPanel);
            }
            this.mContentContainer.addView(this.mMainPanel);
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowButton);
            }
            setPanelsStatesAtRestingPosition();
            setContentAreaAsTouchableSurface();
            if (isRTL()) {
                this.mContentContainer.setAlpha(0.0f);
                this.mContentContainer.post(this.mPreparePopupContentRTLHelper);
            }
        }

        private void clearPanels() {
            this.mOverflowPanelSize = null;
            this.mMainPanelSize = null;
            this.mIsOverflowOpen = false;
            this.mMainPanel.removeAllViews();
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            this.mContentContainer.removeAllViews();
        }

        private void positionContentYCoordinatesIfOpeningOverflowUpwards() {
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY((float) ((this.mContentContainer.getHeight() - this.mMainPanelSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                this.mOverflowButton.setY((float) ((this.mContentContainer.getHeight() - this.mOverflowButton.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                this.mOverflowPanel.setY((float) ((this.mContentContainer.getHeight() - this.mOverflowPanelSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
            }
        }

        private int getOverflowWidth() {
            int overflowWidth = 0;
            int count = this.mOverflowPanel.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                overflowWidth = Math.max(this.mOverflowPanelViewHelper.calculateWidth((MenuItem) this.mOverflowPanel.getAdapter().getItem(i)), overflowWidth);
            }
            return overflowWidth;
        }

        private int calculateOverflowHeight(int maxItemSize) {
            int actualSize = Math.min(MAX_OVERFLOW_SIZE, Math.min(Math.max(MIN_OVERFLOW_SIZE, maxItemSize), this.mOverflowPanel.getCount()));
            int extension = 0;
            if (actualSize < this.mOverflowPanel.getCount()) {
                extension = (int) (((float) getLineHeight(this.mContext)) * 0.5f);
            }
            return ((getLineHeight(this.mContext) * actualSize) + this.mOverflowButtonSize.getHeight()) + extension;
        }

        private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
            View button = menuItemButton;
            if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                button = menuItemButton.findViewById(R.id.floating_toolbar_menu_item_image_button);
            }
            button.setTag(menuItem);
            button.setOnClickListener(this.mMenuItemButtonOnClickListener);
        }

        private int getAdjustedDuration(int originalDuration) {
            if (this.mTransitionDurationScale < LogPower.PC_WEBVIEW_END) {
                return Math.max(originalDuration - 50, 0);
            }
            if (this.mTransitionDurationScale > StatisticalConstant.TYPE_WIFIP2P_INVITATION_ALERT) {
                return originalDuration + 50;
            }
            return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
        }

        private void maybeComputeTransitionDurationScale() {
            if (this.mMainPanelSize != null && this.mOverflowPanelSize != null) {
                int w = this.mMainPanelSize.getWidth() - this.mOverflowPanelSize.getWidth();
                int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
                this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
            }
        }

        private ViewGroup createMainPanel() {
            return new AnonymousClass12(this.mContext);
        }

        private ImageButton createOverflowButton() {
            ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_button, null);
            overflowButton.setImageDrawable(this.mOverflow);
            overflowButton.setOnClickListener(new AnonymousClass13(overflowButton));
            return overflowButton;
        }

        private OverflowPanel createOverflowPanel() {
            OverflowPanel overflowPanel = new OverflowPanel(this);
            overflowPanel.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            overflowPanel.setDivider(null);
            overflowPanel.setDividerHeight(0);
            overflowPanel.setAdapter(new AnonymousClass14(this.mContext, 0));
            overflowPanel.setOnItemClickListener(new AnonymousClass15(overflowPanel));
            return overflowPanel;
        }

        private boolean isOverflowAnimating() {
            boolean overflowOpening = this.mOpenOverflowAnimation.hasStarted() ? !this.mOpenOverflowAnimation.hasEnded() : false;
            boolean overflowClosing = this.mCloseOverflowAnimation.hasStarted() ? !this.mCloseOverflowAnimation.hasEnded() : false;
            return !overflowOpening ? overflowClosing : true;
        }

        private AnimationListener createOverflowAnimationListener() {
            return new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    FloatingToolbarPopup.this.mOverflowButton.setEnabled(false);
                    FloatingToolbarPopup.this.mMainPanel.setVisibility(0);
                    FloatingToolbarPopup.this.mOverflowPanel.setVisibility(0);
                }

                public void onAnimationEnd(Animation animation) {
                    FloatingToolbarPopup.this.mContentContainer.post(new Runnable() {
                        public void run() {
                            FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                            FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                        }
                    });
                }

                public void onAnimationRepeat(Animation animation) {
                }
            };
        }

        private static Size measure(View view) {
            boolean z;
            if (view.getParent() == null) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkState(z);
            view.measure(0, 0);
            return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        private static void setSize(View view, int width, int height) {
            view.setMinimumWidth(width);
            view.setMinimumHeight(height);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(0, 0);
            }
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        }

        private static void setSize(View view, Size size) {
            setSize(view, size.getWidth(), size.getHeight());
        }

        private static void setWidth(View view, int width) {
            setSize(view, width, view.getLayoutParams().height);
        }

        private static void setHeight(View view, int height) {
            setSize(view, view.getLayoutParams().width, height);
        }

        private static int getLineHeight(Context context) {
            return context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_height);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.FloatingToolbar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.FloatingToolbar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.<clinit>():void");
    }

    public FloatingToolbar(Context context, Window window) {
        this(context, window, false);
    }

    public FloatingToolbar(Context context, Window window, boolean flag) {
        this.mContentRect = new Rect();
        this.mPreviousContentRect = new Rect();
        this.mShowingMenuItems = new ArrayList();
        this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        this.mWidthChanged = true;
        this.mOrientationChangeHandler = new OnLayoutChangeListener() {
            private final Rect mNewRect;
            private final Rect mOldRect;

            {
                this.mNewRect = new Rect();
                this.mOldRect = new Rect();
            }

            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (FloatingToolbar.this.mPopup.isShowing() && !this.mNewRect.equals(this.mOldRect)) {
                    FloatingToolbar.this.mWidthChanged = true;
                    FloatingToolbar.this.updateLayout();
                }
            }
        };
        mHwFloatingToolbarFlag[0] = flag;
        this.mContext = applyDefaultTheme((Context) Preconditions.checkNotNull(context));
        this.mWindow = (Window) Preconditions.checkNotNull(window);
        this.mPopup = new FloatingToolbarPopup(this.mContext, window.getDecorView());
    }

    public FloatingToolbar setMenu(Menu menu) {
        this.mMenu = (Menu) Preconditions.checkNotNull(menu);
        return this;
    }

    public FloatingToolbar setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        if (menuItemClickListener != null) {
            this.mMenuItemClickListener = menuItemClickListener;
        } else {
            this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        }
        return this;
    }

    public FloatingToolbar setContentRect(Rect rect) {
        this.mContentRect.set((Rect) Preconditions.checkNotNull(rect));
        return this;
    }

    public FloatingToolbar setSuggestedWidth(int suggestedWidth) {
        this.mWidthChanged = ((double) Math.abs(suggestedWidth - this.mSuggestedWidth)) > ((double) this.mSuggestedWidth) * 0.2d;
        this.mSuggestedWidth = suggestedWidth;
        return this;
    }

    public FloatingToolbar show() {
        registerOrientationHandler();
        doShow();
        return this;
    }

    public FloatingToolbar updateLayout() {
        if (this.mPopup.isShowing()) {
            doShow();
        }
        return this;
    }

    public void dismiss() {
        unregisterOrientationHandler();
        this.mPopup.dismiss();
    }

    public void hide() {
        this.mPopup.hide();
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isHidden() {
        return this.mPopup.isHidden();
    }

    private void doShow() {
        List<MenuItem> menuItems = getVisibleAndEnabledMenuItems(this.mMenu);
        Object selectAll = null;
        String webviewSelectAllName = "id/select_action_menu_select_all";
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem mi = (MenuItem) menuItems.get(i);
            if (i != 0 && this.mContext != null && (mi.getItemId() >> 24) == 3 && this.mContext.getResources().getResourceName(mi.getItemId()).indexOf(webviewSelectAllName) >= 0) {
                selectAll = mi;
            }
        }
        if (selectAll != null) {
            menuItems.remove(selectAll);
            menuItems.add(0, selectAll);
        }
        if (!isCurrentlyShowing(menuItems) || this.mWidthChanged) {
            this.mPopup.dismiss();
            this.mPopup.layoutMenuItems(menuItems, this.mMenuItemClickListener, this.mSuggestedWidth);
            this.mShowingMenuItems = getShowingMenuItemsReferences(menuItems);
        }
        if (!this.mPopup.isShowing()) {
            this.mPopup.show(this.mContentRect);
        } else if (!this.mPreviousContentRect.equals(this.mContentRect)) {
            this.mPopup.updateCoordinates(this.mContentRect);
        }
        this.mWidthChanged = false;
        this.mPreviousContentRect.set(this.mContentRect);
    }

    private boolean isCurrentlyShowing(List<MenuItem> menuItems) {
        return this.mShowingMenuItems.equals(getShowingMenuItemsReferences(menuItems));
    }

    private List<MenuItem> getVisibleAndEnabledMenuItems(Menu menu) {
        List<MenuItem> menuItems = new ArrayList();
        int i = 0;
        while (menu != null && i < menu.size()) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isVisible() && menuItem.isEnabled()) {
                Menu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    menuItems.addAll(getVisibleAndEnabledMenuItems(subMenu));
                } else {
                    menuItems.add(menuItem);
                }
            }
            i++;
        }
        return menuItems;
    }

    private List<Object> getShowingMenuItemsReferences(List<MenuItem> menuItems) {
        List<Object> references = new ArrayList();
        for (MenuItem menuItem : menuItems) {
            if (isIconOnlyMenuItem(menuItem)) {
                references.add(menuItem.getIcon());
            } else {
                references.add(menuItem.getTitle());
            }
        }
        return references;
    }

    private void registerOrientationHandler() {
        unregisterOrientationHandler();
        this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private void unregisterOrientationHandler() {
        this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private static boolean isIconOnlyMenuItem(MenuItem menuItem) {
        if (!TextUtils.isEmpty(menuItem.getTitle()) || menuItem.getIcon() == null) {
            return false;
        }
        return true;
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem) {
        if (isIconOnlyMenuItem(menuItem)) {
            View imageMenuItemButton = LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_image_button, null);
            ((ImageButton) imageMenuItemButton.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
            return imageMenuItemButton;
        }
        Button menuItemButton = (Button) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_button, null);
        setMenuItemButtonInputType(menuItemButton);
        menuItemButton.setText(menuItem.getTitle());
        menuItemButton.setContentDescription(menuItem.getTitle());
        return menuItemButton;
    }

    private static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = (ViewGroup) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_container, null);
        if (mHwFloatingToolbarFlag[0]) {
            contentContainer.setElevation(0.0f);
        }
        contentContainer.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        contentContainer.setTag(FLOATING_TOOLBAR_TAG);
        return contentContainer;
    }

    private static PopupWindow createPopupWindow(ViewGroup content) {
        View popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_ON_SIM);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        content.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    private static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL}).setDuration(150);
        animation.playTogether(animatorArr);
        return animation;
    }

    private static AnimatorSet createExitAnimation(View view, int startDelay, AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{LayoutParams.BRIGHTNESS_OVERRIDE_FULL, 0.0f}).setDuration(100);
        animation.playTogether(animatorArr);
        animation.setStartDelay((long) startDelay);
        animation.addListener(listener);
        return animation;
    }

    private static Context applyDefaultTheme(Context originalContext) {
        return new ContextThemeWrapper(originalContext, getThemeId(originalContext));
    }

    protected static void setMenuItemButtonInputType(TextView textview) {
        if (mHwFloatingToolbarFlag[0]) {
            textview.setAllCaps(false);
            textview.setInputType(GL10.GL_LIGHT0);
        }
    }

    protected static int getThemeId(Context originalContext) {
        if (mHwFloatingToolbarFlag[0]) {
            return originalContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
        TypedArray a = originalContext.obtainStyledAttributes(new int[]{R.attr.isLightTheme});
        int themeId = a.getBoolean(0, true) ? R.style.Theme_Material_Light : R.style.Theme_Material;
        a.recycle();
        return themeId;
    }

    protected static void adjustWindowVisibleDisplayFrame(Rect rect) {
        if (mHwFloatingToolbarFlag[0] && rect.equals(new Rect(-10000, -10000, PGAction.PG_ID_DEFAULT_FRONT, PGAction.PG_ID_DEFAULT_FRONT))) {
            Display d = DisplayManagerGlobal.getInstance().getRealDisplay(0);
            if (d != null) {
                d.getRectSize(rect);
            }
        }
    }

    protected static int getFloatingToolbarHeightBias(Context context) {
        return context.getResources().getDimensionPixelSize(androidhwext.R.dimen.floating_toolbar_height_bias);
    }
}
