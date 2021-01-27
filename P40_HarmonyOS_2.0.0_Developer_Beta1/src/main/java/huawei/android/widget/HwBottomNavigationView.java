package huawei.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HwKeyEventDetector;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.view.menu.MenuBuilder;
import com.huawei.android.app.ActionBarEx;
import huawei.android.widget.columnsystem.HwColumnSystem;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.android.widget.plume.HwPlumeManager;
import java.util.ArrayList;
import java.util.List;

public class HwBottomNavigationView extends LinearLayout {
    private static final int AVERAGE_WIDTH_CHILD_COUNT = 2;
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int DICHOTOMY_SIZE = 2;
    private static final int DOUBLE_SIZE = 2;
    private static final int ICON_ACTIVE_COLOR = 678391;
    private static final int ICON_DEFAULT_COLOR = 855638016;
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final int INVALID_INDEX = -1;
    private static final int INVALID_VALUE = -1;
    private static final float ITEM_NUM_MAX = 5.0f;
    private static final int ITEM_NUM_THREAD = 3;
    private static final int MENU_MAX_SIZE_WARNING = 5;
    private static final int MESSAGE_BG_COLOR = 16394797;
    private static final String RES_TYPE_STYLEABLE = "styleable";
    private static final String TAG = "HwBottomNavigationView";
    private static final int TITLE_ACTIVE_COLOR = 678391;
    private static final int TITLE_DEFAULT_COLOR = -1728053248;
    private String mAccessAbilityMsgTxt;
    private int mActiveColor;
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private BottomNavItemClickListener mBottomNavItemClickListener;
    private BottomNavItemTouchListener mBottomNavItemTouchListener;
    private HwColumnSystem mColumnSystem;
    private float mColumnUserSetDensity;
    private int mColumnUserSetHeight;
    private int mColumnUserSetWidth;
    private int mColumnWidth;
    private Context mContext;
    private BottomNavigationItemView mCurrentTouchedItem;
    private int mDefaultColor;
    private Drawable mDivider;
    protected OnItemDoubleTapListener mDoubleTapListener;
    private int mFocusPos;
    private GestureDetector mGestureDetector;
    private HwKeyEventDetector mHwKeyEventDetector;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private int mIconBounds;
    private int mInteractSelector;
    private boolean mIsBlurEnable;
    private boolean mIsColumnConfigured;
    private boolean mIsColumnEnabled;
    private boolean mIsDividerEnable;
    private boolean mIsGlobalNextTabEnable;
    private boolean mIsHwEmphasizeTheme;
    private boolean mIsNextTabEnable;
    private boolean mIsPortLayout;
    private boolean mIsSpaceEnough;
    private boolean mIsTintEnable;
    private int mLargeWidth;
    private int mLastSelectItem;
    private BottemNavListener mListener;
    private MeasureSize mMeasureSize;
    private Menu mMenu;
    private MenuInflater mMenuInflater;
    private int mMenuSize;
    private int mMessageBgColor;
    private HwOnGlobalNextTabEventListener mOnGlobalNextTabListener;
    private HwOnNextTabEventListener mOnNextTabListener;
    private int mPortWidthInVertical;
    private int mSmallWidth;
    private int mSpaceThread;
    private final Rect mTempRect;
    private int mTextPadding;
    protected int mTitleActiveColor;
    protected int mTitleDefaultColor;

    public interface BottemNavListener {
        void onBottemNavItemReselected(MenuItem menuItem, int i);

        void onBottemNavItemSelected(MenuItem menuItem, int i);

        void onBottemNavItemUnselected(MenuItem menuItem, int i);
    }

    public interface OnItemDoubleTapListener {
        void onDoubleTaped(MenuItem menuItem, int i);
    }

    public HwBottomNavigationView(Context context) {
        this(context, null);
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 33620065);
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMeasureSize = new MeasureSize();
        this.mTempRect = new Rect();
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mLastSelectItem = -1;
        this.mIsPortLayout = false;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.mIsBlurEnable = false;
        this.mIsDividerEnable = false;
        this.mSpaceThread = 0;
        this.mFocusPos = 0;
        this.mHwKeyEventDetector = null;
        this.mIsNextTabEnable = true;
        this.mIsGlobalNextTabEnable = true;
        this.mOnNextTabListener = new HwOnNextTabEventListener() {
            /* class huawei.android.widget.HwBottomNavigationView.AnonymousClass1 */

            public boolean onNextTab(int action, KeyEvent event) {
                if (action == 1) {
                    HwBottomNavigationView.this.changeToNextItem();
                }
                return true;
            }
        };
        this.mOnGlobalNextTabListener = new HwOnGlobalNextTabEventListener() {
            /* class huawei.android.widget.HwBottomNavigationView.AnonymousClass2 */

            public boolean onGlobalNextTab(int action, KeyEvent event) {
                if (action == 1) {
                    HwBottomNavigationView.this.changeToNextItem();
                }
                return true;
            }
        };
        initialise(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialise(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = ResLoader.getInstance().getContext(context);
        this.mMenu = new MenuBuilder(this.mContext);
        initWidgetSafeInsets(context, attrs);
        this.mMenuInflater = new MenuInflater(this.mContext);
        this.mIsHwEmphasizeTheme = HwWidgetFactory.isHwEmphasizeTheme(this.mContext);
        initAttr(context, attrs, defStyleAttr, defStyleRes);
        this.mTextPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_text_margin");
        this.mIconBounds = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_icon_size");
        this.mSpaceThread = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_space_thread"));
        this.mPortWidthInVertical = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_port_width_in_vertical");
        this.mBottomNavItemClickListener = new BottomNavItemClickListener();
        this.mBottomNavItemTouchListener = new BottomNavItemTouchListener();
        initColumnLayout();
        createNewItem();
        this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(this.mContext);
        setValueFromPlume();
        initDoubleTapListener();
    }

    private void initDoubleTapListener() {
        this.mGestureDetector = new GestureDetector(this.mContext, new GestureDetector.SimpleOnGestureListener() {
            /* class huawei.android.widget.HwBottomNavigationView.AnonymousClass3 */

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onSingleTapConfirmed(MotionEvent event) {
                return super.onSingleTapConfirmed(event);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTap(MotionEvent event) {
                if (!(HwBottomNavigationView.this.mDoubleTapListener == null || HwBottomNavigationView.this.mCurrentTouchedItem == null)) {
                    int index = HwBottomNavigationView.this.mCurrentTouchedItem.getItemIndex();
                    HwBottomNavigationView.this.mDoubleTapListener.onDoubleTaped(HwBottomNavigationView.this.mMenu.getItem(index), index);
                }
                return super.onDoubleTap(event);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTapEvent(MotionEvent event) {
                return super.onDoubleTapEvent(event);
            }
        });
    }

    private void setValueFromPlume() {
        if (HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendedNextTabEnabled(true, HwPlumeManager.getInstance(this.mContext).getDefault(this, "switchTabEnabled", true));
            setExtendedNextTabEnabled(false, HwPlumeManager.getInstance(this.mContext).getDefault(this, "switchTabWhenFocusedEnabled", true));
        }
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        ResLoader resLoader = ResLoader.getInstance();
        Resources resources = resLoader.getResources(this.mContext);
        Resources.Theme theme = resLoader.getTheme(context);
        TypedArray typedArray = theme.obtainStyledAttributes(attrs, resLoader.getIdentifierArray(context, "styleable", "BottomNavigation"), defStyleAttr, defStyleRes);
        this.mDefaultColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavItemDefaultColor"), ICON_DEFAULT_COLOR);
        this.mActiveColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavItemActiveColor"), 678391);
        this.mTitleDefaultColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_hwTitleDefaultColor"), TITLE_DEFAULT_COLOR);
        this.mTitleActiveColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_hwTitleActiveColor"), 678391);
        this.mMessageBgColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomMessageBgColor"), MESSAGE_BG_COLOR);
        initDivider(context, resLoader, resources, theme, typedArray);
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(typedArray.getInteger(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavBlurType"), DEFAULT_BLUR_TYPE));
        if (blurType != null) {
            this.mBlurType = blurType;
        }
        this.mBlurOverlayColor = typedArray.getColor(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavBlurOverlayColor"), INVALID_BLUR_OVERLAY_COLOR);
        this.mIsColumnEnabled = typedArray.getBoolean(resLoader.getIdentifier(context, "styleable", "BottomNavigation_hwColumnEnabled"), false);
        initSelectorAndTint(typedArray, resLoader, this.mContext);
        int menuResId = typedArray.getResourceId(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavMenu"), 0);
        typedArray.recycle();
        if (menuResId > 0) {
            this.mMenuInflater.inflate(menuResId, this.mMenu);
        }
        this.mAccessAbilityMsgTxt = ResLoaderUtil.getString(this.mContext, "bottomnav_access_ability_message_text");
    }

    private void initDivider(Context context, ResLoader resLoader, Resources resources, Resources.Theme theme, TypedArray typedArray) {
        int bottomDividerId = typedArray.getResourceId(resLoader.getIdentifier(context, "styleable", "BottomNavigation_bottomNavDivider"), -1);
        if (bottomDividerId == -1) {
            bottomDividerId = getDefaultDividerId();
        }
        try {
            this.mDivider = resources.getDrawable(bottomDividerId, theme);
        } catch (Resources.NotFoundException e) {
            this.mDivider = null;
            Log.e(TAG, "initAttr: Obtaining dividing line res fail, mDivider is null");
        }
    }

    private void initSelectorAndTint(TypedArray typedArray, ResLoader resLoader, Context context) {
        int i;
        this.mInteractSelector = typedArray.getResourceId(resLoader.getIdentifier(context, "styleable", "BottomNavigation_hwInteractSelector"), -1);
        if (this.mInteractSelector == -1) {
            if (this.mIsHwEmphasizeTheme) {
                i = 33751529;
            } else {
                i = 33751527;
            }
            this.mInteractSelector = i;
        }
        this.mIsTintEnable = typedArray.getBoolean(resLoader.getIdentifier(context, "styleable", "BottomNavigation_hwTintEnabled"), true);
    }

    private int getDefaultDividerId() {
        if (getOrientation() == 1) {
            if (this.mIsHwEmphasizeTheme) {
                return 33751470;
            }
            return 33751466;
        } else if (this.mIsHwEmphasizeTheme) {
            return 33751463;
        } else {
            return 33751450;
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        View view = getChildAt(this.mFocusPos);
        if (hasFocus() || this.mFocusPos < 0 || view == null) {
            super.addFocusables(views, direction, focusableMode);
        } else if (view.isFocusable()) {
            views.add(view);
        } else {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    private void initWidgetSafeInsets(Context context, AttributeSet attrs) {
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
    }

    private void initColumnLayout() {
        int i;
        this.mColumnSystem = new HwColumnSystem(this.mContext);
        this.mIsColumnConfigured = false;
        this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        if (!this.mIsColumnEnabled || (i = this.mLargeWidth) <= 0) {
            this.mIsSpaceEnough = isSpaceEnough((this.mContext.getResources().getDisplayMetrics().widthPixels - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mIsSpaceEnough = isSpaceEnough(i);
        }
    }

    private boolean isMenuSizeReasonable(Menu menu) {
        return menu.size() <= 5;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mHwWidgetSafeInsets.updateOriginPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            if (this.mIsNextTabEnable) {
                hwKeyEventDetector.setOnNextTabListener(this.mOnNextTabListener);
            }
            if (this.mIsGlobalNextTabEnable) {
                this.mHwKeyEventDetector.setOnGlobalNextTabListener(this, this.mOnGlobalNextTabListener);
                return;
            }
            return;
        }
        Log.w(TAG, "onAttachedToWindow: mHwKeyEventDetector is null");
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsColumnConfigured) {
            this.mColumnWidth = getUserSetColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        } else {
            this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        }
    }

    public boolean isDividerEnabled() {
        return this.mIsDividerEnable;
    }

    public void setDividerEnabled(boolean isEnabled) {
        if (this.mIsDividerEnable != isEnabled) {
            this.mIsDividerEnable = isEnabled;
            requestLayout();
        }
    }

    public boolean isColumnEnabled() {
        return this.mIsColumnEnabled;
    }

    public void setColumnEnabled(boolean isEnabled) {
        this.mIsColumnEnabled = isEnabled;
        requestLayout();
    }

    public void configureColumn(int width, int height, float density) {
        if (width > 0 && height > 0 && density > 0.0f) {
            this.mColumnUserSetWidth = width;
            this.mColumnUserSetHeight = height;
            this.mColumnUserSetDensity = density;
            this.mIsColumnConfigured = true;
            this.mColumnWidth = getUserSetColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        } else if (this.mIsColumnConfigured) {
            this.mIsColumnConfigured = false;
            this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        } else {
            return;
        }
        if (this.mIsColumnEnabled) {
            requestLayout();
        }
    }

    public void setPortLayout(boolean isForcePort) {
        if (this.mIsPortLayout != isForcePort) {
            this.mIsPortLayout = isForcePort;
            requestLayout();
        }
    }

    private boolean addMenu(int id, int categoryOrder, CharSequence title, Drawable iconRes, boolean isTint) {
        MenuItem item = this.mMenu.add(0, id, categoryOrder, title);
        if (item == null) {
            return false;
        }
        item.setIcon(iconRes);
        this.mMenuSize = this.mMenu.size();
        if (this.mIsColumnConfigured) {
            this.mColumnWidth = getUserSetColumnLayoutWidth(this.mColumnSystem, this.mMenuSize);
        } else {
            this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenuSize);
        }
        createNewItem(item, this.mMenuSize - 1, isTint);
        return isMenuSizeReasonable(this.mMenu);
    }

    public boolean addMenu(CharSequence title, Drawable iconRes) {
        return addMenu(0, 0, title, iconRes, true);
    }

    public boolean addMenu(int titleRes, Drawable iconRes) {
        return addMenu(0, 0, titleRes, iconRes, true);
    }

    private boolean addMenu(int id, int categoryOrder, int titleRes, Drawable iconRes, boolean isTint) {
        MenuItem item = this.mMenu.add(0, 0, 0, titleRes).setIcon(iconRes);
        this.mMenuSize = this.mMenu.size();
        if (this.mIsColumnConfigured) {
            this.mColumnWidth = getUserSetColumnLayoutWidth(this.mColumnSystem, this.mMenuSize);
        } else {
            this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenuSize);
        }
        createNewItem(item, this.mMenuSize - 1, isTint);
        return isMenuSizeReasonable(this.mMenu);
    }

    public boolean addMenu(CharSequence title, Drawable iconRes, boolean isTint) {
        return addMenu(0, 0, title, iconRes, isTint);
    }

    public boolean addMenu(int titleRes, Drawable iconRes, boolean isTint) {
        return addMenu(0, 0, titleRes, iconRes, isTint);
    }

    public void addView(View child, int index, LinearLayout.LayoutParams params) {
        if (!(child instanceof BottomNavigationItemView)) {
            Log.w(TAG, "illegal to addView by this method");
        } else {
            super.addView(child, index, (ViewGroup.LayoutParams) params);
        }
    }

    public void replaceMenuItem(int titleRes, Drawable iconRes, int index, boolean isTint) {
        if (index >= 0 && index < this.mMenuSize) {
            MenuItem replaceMenu = this.mMenu.getItem(index);
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) view;
                if (titleRes != 0) {
                    replaceMenu.setTitle(titleRes);
                }
                if (iconRes != null) {
                    replaceMenu.setIcon(iconRes);
                }
                itemView.replaceMenuItem(replaceMenu, isTint);
            }
        }
    }

    public void replaceMenuItem(CharSequence title, Drawable iconRes, int index, boolean isTint) {
        if (index >= 0 && index < this.mMenuSize) {
            MenuItem replaceMenu = this.mMenu.getItem(index);
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) view;
                if (title != null) {
                    replaceMenu.setTitle(title);
                }
                if (iconRes != null) {
                    replaceMenu.setIcon(iconRes);
                }
                itemView.replaceMenuItem(replaceMenu, isTint);
            }
        }
    }

    public void replaceMenuItems(int[] titleResSet, Drawable[] iconSet, boolean isTint) {
        int i;
        if (titleResSet != null && titleResSet.length == (i = this.mMenuSize) && iconSet != null && iconSet.length == i) {
            for (int i2 = 0; i2 < this.mMenuSize; i2++) {
                replaceMenuItem(titleResSet[i2], iconSet[i2], i2, isTint);
            }
        }
    }

    public void replaceMenuItems(CharSequence[] titleSet, Drawable[] iconSet, boolean isTint) {
        int i;
        if (titleSet != null && titleSet.length == (i = this.mMenuSize) && iconSet != null && iconSet.length == i) {
            for (int i2 = 0; i2 < this.mMenuSize; i2++) {
                replaceMenuItem(titleSet[i2], iconSet[i2], i2, isTint);
            }
        }
    }

    public void replaceSingleImage(Drawable singleIcon, int index, boolean isExtend) {
        if (index >= 0 && index < this.mMenuSize && singleIcon != null) {
            MenuItem replaceMenu = this.mMenu.getItem(index);
            replaceMenu.setIcon(singleIcon);
            if (getChildAt(index) instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) getChildAt(index)).replaceSingleImage(replaceMenu, isExtend);
            }
        }
    }

    public void setActiveColor(int color) {
        for (int i = 0; i < this.mMenuSize; i++) {
            setItemActiveColor(color, i);
        }
    }

    public void setItemActiveColor(int color, int index) {
        if (index >= 0 && index < this.mMenuSize) {
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) view).setActiveColor(color);
            }
        }
    }

    public void setTitleActiveColor(int color) {
        for (int i = 0; i < this.mMenuSize; i++) {
            setItemTitleActiveColor(color, i);
        }
    }

    public void setItemTitleActiveColor(int color, int index) {
        if (index >= 0 && index < this.mMenuSize) {
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) view).setTitleActiveColor(color);
            }
        }
    }

    public void setDefaultColor(int color) {
        for (int i = 0; i < this.mMenuSize; i++) {
            setItemDefaultColor(color, i);
        }
    }

    public void setItemDefaultColor(int color, int index) {
        if (index >= 0 && index < this.mMenuSize) {
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) view).setDefaultColor(color);
            }
        }
    }

    public void setTitleDefaultColor(int color) {
        for (int i = 0; i < this.mMenuSize; i++) {
            setItemTitleDefaultColor(color, i);
        }
    }

    public void setItemTitleDefaultColor(int color, int index) {
        if (index >= 0 && index < this.mMenuSize) {
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) view).setTitleDefaultColor(color);
            }
        }
    }

    public void notifyScrollToTop(View scrollView) {
        if (scrollView == null) {
            Log.w(TAG, "notifyScrollToTop: Param scollView is null");
        } else if ((scrollView instanceof ScrollCallback) && scrollView.getVisibility() == 0) {
            ((ScrollCallback) scrollView).scrollToTop();
        }
    }

    public void notifyDotMessage(int index, boolean isHasMessage) {
        if (index < this.mMenuSize && (getChildAt(index) instanceof BottomNavigationItemView)) {
            ((BottomNavigationItemView) getChildAt(index)).setHasMessage(isHasMessage);
        }
    }

    public boolean isHasMessage(int index) {
        if (index >= this.mMenuSize || !(getChildAt(index) instanceof BottomNavigationItemView)) {
            return false;
        }
        return ((BottomNavigationItemView) getChildAt(index)).isHasMessage();
    }

    public void setMessageBgColor(int color) {
        this.mMessageBgColor = color;
        for (int i = 0; i < this.mMenuSize; i++) {
            View childView = getChildAt(i);
            if (childView instanceof BottomNavigationItemView) {
                ((BottomNavigationItemView) childView).setMsgBgColor(color);
            }
        }
    }

    private void createNewItem() {
        this.mMenuSize = this.mMenu.size();
        for (int i = 0; i < this.mMenuSize; i++) {
            createNewItem(this.mMenu.getItem(i), i, this.mIsTintEnable);
        }
    }

    private void createNewItem(MenuItem menuItem, int index, boolean isTint) {
        if (menuItem == null) {
            Log.w(TAG, "createNewItem: Param menuItem is null");
            return;
        }
        BottomNavigationItemView itemView = new BottomNavigationItemView(this.mContext, menuItem, this.mIsSpaceEnough, index, isTint);
        itemView.setClickable(true);
        itemView.setFocusable(true);
        itemView.setBackgroundResource(this.mInteractSelector);
        itemView.setActiveColor(this.mActiveColor);
        itemView.setDefaultColor(this.mDefaultColor);
        itemView.setTitleActiveColor(this.mTitleActiveColor);
        itemView.setTitleDefaultColor(this.mTitleDefaultColor);
        itemView.setMsgBgColor(this.mMessageBgColor);
        itemView.setOnClickListener(this.mBottomNavItemClickListener);
        itemView.setOnTouchListener(this.mBottomNavItemTouchListener);
        addView(itemView);
    }

    private boolean isSpaceEnough(int totalWidth) {
        float spaceThread = ((float) this.mSpaceThread) * getResources().getDisplayMetrics().density;
        float eachItemSpace = ((float) totalWidth) / ITEM_NUM_MAX;
        if (this.mIsPortLayout || eachItemSpace <= spaceThread) {
            return false;
        }
        return true;
    }

    private int getDefaultColumnLayoutWidth(HwColumnSystem columnSystem, int itemNum) {
        columnSystem.setColumnType(8);
        columnSystem.updateConfigation(this.mContext);
        this.mSmallWidth = columnSystem.getSuggestWidth();
        columnSystem.setColumnType(9);
        columnSystem.updateConfigation(this.mContext);
        this.mLargeWidth = columnSystem.getSuggestWidth();
        return itemNum > 3 ? this.mLargeWidth : this.mSmallWidth;
    }

    private int getUserSetColumnLayoutWidth(HwColumnSystem columnSystem, int itemNum) {
        columnSystem.setColumnType(8);
        columnSystem.updateConfigation(this.mContext, this.mColumnUserSetWidth, this.mColumnUserSetHeight, this.mColumnUserSetDensity);
        this.mSmallWidth = columnSystem.getSuggestWidth();
        columnSystem.setColumnType(9);
        columnSystem.updateConfigation(this.mContext, this.mColumnUserSetWidth, this.mColumnUserSetHeight, this.mColumnUserSetDensity);
        this.mLargeWidth = columnSystem.getSuggestWidth();
        return itemNum > 3 ? this.mLargeWidth : this.mSmallWidth;
    }

    private void measureOnLand(int widthMeasureSpec, int heightMeasureSpec, MeasureSize measureSize) {
        int placeWidth;
        boolean z;
        int i;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int placeWidth2 = (totalWidth - getPaddingLeft()) - getPaddingRight();
        int maxHeight = 0;
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int childCount = getChildCount();
        boolean z2 = false;
        if (childCount <= 0) {
            measureSize.setWidth(totalWidth);
            measureSize.setHeight(0);
            return;
        }
        if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
            if (i >= placeWidth2) {
                i = placeWidth2;
            }
            placeWidth2 = i;
        }
        int averageWidth = placeWidth2 / childCount;
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        int i2 = 0;
        while (i2 < childCount) {
            View childView = getChildAt(i2);
            if (!(childView instanceof BottomNavigationItemView)) {
                placeWidth = placeWidth2;
                z = z2;
            } else {
                BottomNavigationItemView itemView = (BottomNavigationItemView) childView;
                if (itemView.isSingleImageMode()) {
                    setClipChildren(z2);
                    setClipToPadding(z2);
                    itemView.setClipChildren(z2);
                    itemView.setClipToPadding(z2);
                } else {
                    itemView.setClipChildren(true);
                    itemView.setClipToPadding(true);
                }
                itemView.measure(View.MeasureSpec.makeMeasureSpec(averageWidth, 1073741824), itemHeightSpec);
                LinearLayout container = itemView.getContainer();
                ViewGroup.LayoutParams containerLayoutParams = container.getLayoutParams();
                if (containerLayoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) containerLayoutParams;
                    placeWidth = placeWidth2;
                    containerParams.gravity = 17;
                    z = false;
                    setMarginHorizontal(container, 0, 0, containerParams);
                } else {
                    placeWidth = placeWidth2;
                    z = false;
                }
                int height = itemView.getMeasuredHeight();
                if (height > maxHeight) {
                    maxHeight = height;
                }
                setStableWidth(itemView, averageWidth);
            }
            i2++;
            z2 = z;
            placeWidth2 = placeWidth;
        }
        measureSize.setWidth(totalWidth);
        measureSize.setHeight(maxHeight + heightPadding);
    }

    /* access modifiers changed from: protected */
    public void measureOnPortraitByAverageWidth(int widthMeasureSpec, int heightMeasureSpec, MeasureSize measureSize) {
        int i;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int placeWidth = (totalWidth - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        if (measureSize == null) {
            Log.w(TAG, "measureOnPortraitByAverageWidth: Param measureSize is null");
        } else if (childCount <= 0) {
            measureSize.setWidth(totalWidth);
            measureSize.setHeight(0);
        } else {
            if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
                if (i >= placeWidth) {
                    i = placeWidth;
                }
                placeWidth = i;
            }
            int heightPadding = getPaddingTop() + getPaddingBottom();
            measureSize.setWidth(totalWidth);
            measureSize.setHeight(measurePortraitItemByAverageWidth(childCount, placeWidth / childCount, getChildMeasureSpec(heightMeasureSpec, heightPadding, -2)) + heightPadding);
        }
    }

    private int measurePortraitItemByAverageWidth(int childCount, int averageWidth, int itemHeightSpec) {
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) view;
                if (itemView.isSingleImageMode()) {
                    itemView.setClipChildren(false);
                    itemView.setClipToPadding(false);
                } else {
                    itemView.setClipChildren(true);
                    itemView.setClipToPadding(true);
                }
                itemView.measure(View.MeasureSpec.makeMeasureSpec(averageWidth, 1073741824), itemHeightSpec);
                setStableWidth(itemView, averageWidth);
                LinearLayout container = itemView.getContainer();
                ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                if (layoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams containerLayoutParams = (LinearLayout.LayoutParams) layoutParams;
                    containerLayoutParams.gravity = 1;
                    container.setLayoutParams(containerLayoutParams);
                    setMarginHorizontal(container, 0, 0, containerLayoutParams);
                }
                ImageView icon = itemView.getIcon();
                ViewGroup.LayoutParams params = icon.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) params;
                    iconLayoutParams.gravity = 1;
                    setMarginHorizontal(icon, 0, 0, iconLayoutParams);
                }
                int height = itemView.getMeasuredHeight();
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }
        return maxHeight;
    }

    private void adjustWidthOnPortrait(List<Float> dataList, int curIndex, float averageWidth, int itemHeightSpec, BottomNavigationItemView itemView) {
        int correctWidth;
        int i;
        int childCount = getChildCount();
        if (curIndex == 0) {
            i = 1073741824;
        } else if (curIndex == childCount - 1) {
            i = 1073741824;
        } else {
            float startData = dataList.get(curIndex - 1).floatValue();
            float endData = dataList.get(curIndex + 1).floatValue();
            if (startData < 0.0f || endData < 0.0f) {
                itemView.measure(View.MeasureSpec.makeMeasureSpec((int) averageWidth, 1073741824), itemHeightSpec);
                correctWidth = (int) averageWidth;
                itemView.mIsMeasured = true;
                setStableWidth(itemView, correctWidth);
            }
            float current = dataList.get(curIndex).floatValue();
            float minWidth = startData > endData ? endData : startData;
            float sizeRemaining = (current / 2.0f) + minWidth;
            if ((getChildAt(curIndex - 1) instanceof BottomNavigationItemView) && (getChildAt(curIndex + 1) instanceof BottomNavigationItemView)) {
                BottomNavigationItemView leftItem = (BottomNavigationItemView) getChildAt(curIndex - 1);
                BottomNavigationItemView rightItem = (BottomNavigationItemView) getChildAt(curIndex + 1);
                if (sizeRemaining > 0.0f) {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec((int) (averageWidth - current), 1073741824), itemHeightSpec);
                    leftItem.mEndRent = (-current) / 2.0f;
                    rightItem.mStartRent = (-current) / 2.0f;
                    correctWidth = (int) (averageWidth - current);
                } else {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec((int) ((minWidth * 2.0f) + averageWidth), 1073741824), itemHeightSpec);
                    leftItem.mEndRent = minWidth;
                    rightItem.mStartRent = minWidth;
                    correctWidth = (int) ((2.0f * minWidth) + averageWidth);
                }
                itemView.mIsMeasured = true;
                setStableWidth(itemView, correctWidth);
            }
            return;
        }
        itemView.measure(View.MeasureSpec.makeMeasureSpec((int) averageWidth, i), itemHeightSpec);
        correctWidth = (int) averageWidth;
        itemView.mIsMeasured = true;
        setStableWidth(itemView, correctWidth);
    }

    private void measureOnPortraitFirst(float averageWidth, int itemHeightSpec, List<Float> dataList, MeasureSize measureSize) {
        int childCount = getChildCount();
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            if (dataList.get(i).floatValue() < 0.0f) {
                View childView = getChildAt(i);
                if (childView instanceof BottomNavigationItemView) {
                    BottomNavigationItemView itemView = (BottomNavigationItemView) childView;
                    ImageView icon = itemView.getIcon();
                    ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
                    if (layoutParams instanceof LinearLayout.LayoutParams) {
                        LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) layoutParams;
                        iconLayoutParams.gravity = 1;
                        setMarginHorizontal(icon, 0, 0, iconLayoutParams);
                    }
                    setMarginHorizontal(itemView.getContainer(), 0, 0);
                    adjustWidthOnPortrait(dataList, i, averageWidth, itemHeightSpec, itemView);
                    int height = itemView.getMeasuredHeight();
                    if (height > maxHeight) {
                        maxHeight = height;
                    }
                }
            }
        }
        measureSize.setHeight(maxHeight);
    }

    private void measureOnPortraitLast(float averageWidth, int itemHeightSpec, List<Float> dataList, MeasureSize measureSize) {
        int maxHeight = measureSize.getHeight();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) childView;
                if (itemView.isSingleImageMode()) {
                    itemView.setClipChildren(false);
                    itemView.setClipToPadding(false);
                } else {
                    itemView.setClipChildren(true);
                    itemView.setClipToPadding(true);
                }
                if (itemView.mIsMeasured) {
                    itemView.mIsMeasured = false;
                } else {
                    itemViewMeasure(averageWidth, itemHeightSpec, itemView, dataList.get(i).floatValue());
                    int height = itemView.getMeasuredHeight();
                    if (height > maxHeight) {
                        maxHeight = height;
                    }
                }
            }
        }
        measureSize.setHeight(maxHeight);
    }

    private void itemViewMeasure(float averageWidth, int itemHeightSpec, BottomNavigationItemView itemView, float current) {
        LinearLayout container = itemView.getContainer();
        ViewGroup.LayoutParams containerLayoutParams = container.getLayoutParams();
        if (containerLayoutParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) containerLayoutParams;
            containerParams.gravity = 0;
            setMarginHorizontal(container, (int) (current - itemView.mStartRent), (int) (current - itemView.mEndRent), containerParams);
        }
        ImageView icon = itemView.getIcon();
        ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) layoutParams;
            iconLayoutParams.gravity = 0;
            setMarginHorizontal(icon, (int) (((averageWidth - ((float) this.mIconBounds)) / 2.0f) - itemView.mStartRent), (int) (((averageWidth - ((float) this.mIconBounds)) / 2.0f) - itemView.mEndRent), iconLayoutParams);
        }
        itemView.measure(View.MeasureSpec.makeMeasureSpec((int) ((averageWidth - itemView.mStartRent) - itemView.mEndRent), 1073741824), itemHeightSpec);
        setStableWidth(itemView, (int) ((averageWidth - itemView.mStartRent) - itemView.mEndRent));
        itemView.mStartRent = 0.0f;
        itemView.mEndRent = 0.0f;
    }

    private void measureOnPortraitByAutoWidth(int widthMeasureSpec, int heightMeasureSpec, MeasureSize measureSize) {
        int i;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        float placeWidth = (float) ((totalWidth - getPaddingLeft()) - getPaddingRight());
        measureSize.setWidth(totalWidth);
        int childCount = getChildCount();
        if (childCount <= 0) {
            measureSize.setWidth(totalWidth);
            measureSize.setHeight(0);
            return;
        }
        if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
            placeWidth = ((float) i) < placeWidth ? (float) i : placeWidth;
        }
        float averageWidth = placeWidth / ((float) childCount);
        List<Float> dataList = new ArrayList<>(childCount);
        for (int i2 = 0; i2 < childCount; i2++) {
            generateOffsetList(dataList, i2, averageWidth);
        }
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        measureOnPortraitFirst(averageWidth, itemHeightSpec, dataList, measureSize);
        measureOnPortraitLast(averageWidth, itemHeightSpec, dataList, measureSize);
        measureSize.setHeight(measureSize.getHeight() + heightPadding);
    }

    private void measureOnPortrait(int widthMeasureSpec, int heightMeasureSpec, MeasureSize measureSize) {
        int childCount = getChildCount();
        boolean isExtend = false;
        boolean isSingleImage = false;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) childView;
                isSingleImage |= itemView.isSingleImageMode();
                isExtend |= itemView.isExtend();
            }
        }
        if (isExtend) {
            setClipChildren(false);
            setClipToPadding(false);
        }
        if (childCount == 2 || childCount == 1 || isSingleImage) {
            measureOnPortraitByAverageWidth(widthMeasureSpec, heightMeasureSpec, measureSize);
        } else {
            measureOnPortraitByAutoWidth(widthMeasureSpec, heightMeasureSpec, measureSize);
        }
    }

    private void measureOnPortraitByAutoHeight(int heightMeasureSpec, MeasureSize measureSize) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            int childCount = getChildCount();
            boolean isWrapContent = false;
            if (childCount <= 0) {
                measureSize.setHeight(0);
                measureSize.setWidth(0);
                return;
            }
            int totalHeight = View.MeasureSpec.getSize(heightMeasureSpec);
            int heightPadding = getPaddingTop() + getPaddingBottom();
            int averageHeight = (totalHeight - heightPadding) / (childCount * 2);
            measureSize.setHeight(totalHeight);
            measureSize.setWidth(this.mPortWidthInVertical);
            if (params.height == -2) {
                isWrapContent = true;
            }
            if (isWrapContent) {
                averageHeight = 0;
            }
            int finalHeight = measureOnPortraitVertical(measureSize.getWidth(), averageHeight);
            if (isWrapContent) {
                measureSize.setHeight(finalHeight * childCount * 2);
            } else {
                measureSize.setHeight(averageHeight * childCount * 2);
            }
            measureSize.setHeight(measureSize.getHeight() + heightPadding);
        }
    }

    private int measureOnPortraitVertical(int width, int calcHeight) {
        int maxHeight = 0;
        int itemCount = getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), 0);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
        }
        int maxHeight2 = Math.max(maxHeight, calcHeight);
        for (int i2 = 0; i2 < itemCount; i2++) {
            View child2 = getChildAt(i2);
            if (child2 instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) child2;
                itemView.setDirection(false);
                itemView.setGravity(17);
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams itemLayoutParams = (LinearLayout.LayoutParams) params;
                    itemLayoutParams.height = maxHeight2;
                    itemLayoutParams.width = width;
                    child2.setLayoutParams(params);
                }
            }
        }
        return maxHeight2;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        this.mMeasureSize.init();
        if (getOrientation() == 1) {
            measureOnPortraitByAutoHeight(heightMeasureSpec, this.mMeasureSize);
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getHeight(), 1073741824));
            return;
        }
        boolean isColumnEnabled = this.mIsColumnEnabled;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int placeWidth = (totalWidth - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        if (childCount <= 3 && this.mSmallWidth <= 0) {
            isColumnEnabled = false;
        }
        if (isColumnEnabled && (i = this.mLargeWidth) > 0) {
            if (i >= placeWidth) {
                i = placeWidth;
            }
            placeWidth = i;
        }
        boolean isEnough = isSpaceEnough(placeWidth);
        if (childCount == 0) {
            setMeasuredDimension(totalWidth, 0);
            return;
        }
        if (isEnough != this.mIsSpaceEnough) {
            this.mIsSpaceEnough = isEnough;
            for (int i2 = 0; i2 < childCount; i2++) {
                View childView = getChildAt(i2);
                if (childView instanceof BottomNavigationItemView) {
                    ((BottomNavigationItemView) childView).setDirection(this.mIsSpaceEnough);
                }
            }
        }
        if (this.mIsSpaceEnough) {
            measureOnLand(widthMeasureSpec, heightMeasureSpec, this.mMeasureSize);
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getHeight(), 1073741824));
            return;
        }
        measureOnPortrait(widthMeasureSpec, heightMeasureSpec, this.mMeasureSize);
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getHeight(), 1073741824));
    }

    private void generateOffsetList(List<Float> dataList, int index, float standardData) {
        View view = getChildAt(index);
        if (view instanceof BottomNavigationItemView) {
            float size = standardData - (Layout.getDesiredWidth(this.mMenu.getItem(index).getTitle(), ((BottomNavigationItemView) view).getContent().getPaint()) + ((float) (this.mTextPadding * 2)));
            if (size > 0.0f) {
                dataList.add(Float.valueOf(size / 2.0f));
            } else {
                dataList.add(Float.valueOf(size));
            }
        }
    }

    private void setMarginHorizontal(View view, int marginStart, int marginEnd) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            setMarginHorizontal(view, marginStart, marginEnd, (ViewGroup.MarginLayoutParams) layoutParams);
        }
    }

    private void setMarginHorizontal(View view, int marginStart, int marginEnd, ViewGroup.MarginLayoutParams layoutParams) {
        if (isLayoutRtl()) {
            layoutParams.setMargins(marginEnd, layoutParams.topMargin, marginStart, layoutParams.bottomMargin);
        } else {
            layoutParams.setMargins(marginStart, layoutParams.topMargin, marginEnd, layoutParams.bottomMargin);
        }
        view.setLayoutParams(layoutParams);
    }

    private void setStableWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        this.mHwWidgetSafeInsets.applyDisplaySafeInsets(true);
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        this.mHwWidgetSafeInsets.updateOriginPadding(left, top, right, bottom);
    }

    public void setBottemNavListener(BottemNavListener listener) {
        this.mListener = listener;
    }

    public void setDoubleTapListener(OnItemDoubleTapListener listener) {
        this.mDoubleTapListener = listener;
    }

    public OnItemDoubleTapListener getDoubleTapListener() {
        return this.mDoubleTapListener;
    }

    public void setItemChecked(int index) {
        int childCount = getChildCount();
        if (index >= 0 && index < childCount) {
            View view = getChildAt(index);
            if (view instanceof BottomNavigationItemView) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) view;
                itemView.setChecked(true, this.mLastSelectItem != -1);
                changeItem(itemView, false);
            }
        }
    }

    public void removeMenuItems() {
        this.mLastSelectItem = -1;
        this.mMenu.clear();
        this.mMenuSize = 0;
        removeAllViews();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeItem(BottomNavigationItemView itemView, boolean isClick) {
        BottemNavListener bottemNavListener;
        int index = itemView.getItemIndex();
        if (index != this.mLastSelectItem || (bottemNavListener = this.mListener) == null) {
            int i = this.mLastSelectItem;
            if (index != i) {
                if (i < this.mMenuSize && i >= 0) {
                    View view = getChildAt(i);
                    if (view instanceof BottomNavigationItemView) {
                        ((BottomNavigationItemView) view).setChecked(false, true);
                        BottemNavListener bottemNavListener2 = this.mListener;
                        if (bottemNavListener2 != null) {
                            bottemNavListener2.onBottemNavItemUnselected(this.mMenu.getItem(this.mLastSelectItem), this.mLastSelectItem);
                        }
                    } else {
                        return;
                    }
                }
                this.mLastSelectItem = index;
                if (isClick) {
                    itemView.setChecked(true, true);
                }
                BottemNavListener bottemNavListener3 = this.mListener;
                if (bottemNavListener3 != null) {
                    bottemNavListener3.onBottemNavItemSelected(this.mMenu.getItem(this.mLastSelectItem), this.mLastSelectItem);
                }
            } else {
                Log.e(TAG, "invalid index");
            }
        } else {
            bottemNavListener.onBottemNavItemReselected(this.mMenu.getItem(index), index);
        }
        this.mFocusPos = this.mLastSelectItem;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mBlurEngine.isShowHwBlur(this)) {
            this.mBlurEngine.draw(canvas, this);
            super.dispatchDraw(canvas);
            drawDivider(canvas);
            return;
        }
        super.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            this.mBlurEngine.addBlurTargetView(this, this.mBlurType);
            this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
            int i = this.mBlurOverlayColor;
            if (i != INVALID_BLUR_OVERLAY_COLOR) {
                this.mBlurEngine.setTargetViewOverlayColor(this, i);
                return;
            }
            return;
        }
        this.mBlurEngine.removeBlurTargetView(this);
    }

    public boolean isBlurEnable() {
        return this.mIsBlurEnable;
    }

    public void setBlurEnable(boolean isBlurEnable) {
        this.mIsBlurEnable = isBlurEnable;
        this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
    }

    private int getBlurColor() {
        return this.mBlurOverlayColor;
    }

    private void setBlurColor(int blurColor) {
        this.mBlurOverlayColor = blurColor;
    }

    private int getBlurType() {
        return this.mBlurType.getValue();
    }

    private void setBlurType(HwBlurEngine.BlurType blurType) {
        this.mBlurType = blurType;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawDivider(canvas);
    }

    private void drawDivider(Canvas canvas) {
        if (this.mIsDividerEnable && this.mDivider != null) {
            Rect bounds = this.mTempRect;
            if (getOrientation() != 1) {
                bounds.left = getPaddingLeft();
                bounds.right = (getRight() - getLeft()) - getPaddingRight();
                bounds.top = 0;
                bounds.bottom = this.mDivider.getIntrinsicHeight();
            } else if (isLayoutRtl()) {
                bounds.left = 0;
                bounds.right = this.mDivider.getIntrinsicWidth();
                bounds.top = getPaddingTop();
                bounds.bottom = (getBottom() - getTop()) - getPaddingBottom();
            } else {
                bounds.left = ((getRight() - getLeft()) - getPaddingRight()) - 1;
                bounds.right = this.mDivider.getIntrinsicWidth();
                bounds.top = getPaddingTop();
                bounds.bottom = (getBottom() - getTop()) - getPaddingBottom();
            }
            this.mDivider.setBounds(bounds);
            this.mDivider.draw(canvas);
        }
    }

    /* access modifiers changed from: private */
    public class MeasureSize {
        private int mHeight;
        private int mWidth;

        MeasureSize() {
        }

        /* access modifiers changed from: package-private */
        public int getWidth() {
            return this.mWidth;
        }

        /* access modifiers changed from: package-private */
        public void setWidth(int width) {
            this.mWidth = width;
        }

        /* access modifiers changed from: package-private */
        public int getHeight() {
            return this.mHeight;
        }

        /* access modifiers changed from: package-private */
        public void setHeight(int height) {
            this.mHeight = height;
        }

        /* access modifiers changed from: package-private */
        public void init() {
            this.mWidth = 0;
            this.mHeight = 0;
        }
    }

    /* access modifiers changed from: private */
    public class BottomNavigationItemView extends LinearLayout {
        protected int mActiveColor;
        protected LinearLayout mContainer;
        protected HwTextView mContent;
        protected Context mContext;
        private ComplexDrawable mCurrentDrawable;
        protected int mDefaultColor;
        float mEndRent;
        private int mHorizontalPadding;
        private int mIndex;
        protected boolean mIsChecked;
        private boolean mIsDirectionLand;
        private boolean mIsExtend;
        private boolean mIsHasMessage;
        boolean mIsMeasured;
        protected boolean mIsSingleImage;
        protected boolean mIsTint;
        protected MenuItem mItem;
        private ComplexDrawable mLandComplexDrawable;
        private int mLandExtendSize;
        private int mLandMinHeight;
        private int mLandNormalSize;
        private int mLandTextSize;
        protected int mMinTextSize;
        private int mMsgBgColor;
        protected Paint mPaint;
        private ComplexDrawable mPortComplexDrawable;
        private int mPortExtendSize;
        protected int mPortMinHeight;
        private int mPortNormalSize;
        protected int mPortTextSize;
        private int mRedDotRadius;
        private ComplexDrawable mSingleComplexDrawable;
        protected ImageView mSingleImage;
        protected ImageView mStartImage;
        float mStartRent;
        protected int mStepGranularity;
        protected ImageView mTopImage;
        private int mVerticalAddedPadding;
        private int mVerticalPadding;

        BottomNavigationItemView(HwBottomNavigationView hwBottomNavigationView, Context context, MenuItem menuItem, boolean isLand, int index) {
            this(context, menuItem, isLand, index, hwBottomNavigationView.mIsTintEnable);
        }

        BottomNavigationItemView(Context context, MenuItem menuItem, boolean isLand, int index, boolean isTint) {
            super(context);
            this.mIsChecked = false;
            this.mIsTint = true;
            this.mContext = context;
            this.mItem = menuItem;
            inflate(context, ResLoaderUtil.getLayoutId(context, "bottomnav_item_layout"), this);
            this.mContent = (HwTextView) findViewById(ResLoaderUtil.getViewId(context, "content"));
            this.mTopImage = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "topIcon"));
            this.mStartImage = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "startIcon"));
            this.mSingleImage = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "singleIcon"));
            this.mContainer = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "container"));
            this.mLandComplexDrawable = new ComplexDrawable(context, this.mItem.getIcon());
            this.mPortComplexDrawable = new ComplexDrawable(context, this.mItem.getIcon());
            this.mLandMinHeight = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_land_minheight");
            this.mPortMinHeight = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_port_minheight");
            this.mPortTextSize = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_port_textsize");
            this.mLandTextSize = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_item_land_textsize"));
            this.mStepGranularity = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_text_stepgranularity"));
            this.mMinTextSize = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_item_min_textsize"));
            this.mVerticalPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_vertical_padding");
            this.mHorizontalPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_horizontal_padding");
            this.mVerticalAddedPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_add_top_margin");
            this.mPortNormalSize = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_single_icon_normal_size_port");
            this.mPortExtendSize = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_single_icon_extend_size_port");
            this.mLandNormalSize = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_single_icon_normal_size_land");
            this.mLandExtendSize = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_single_icon_extend_size_land");
            this.mRedDotRadius = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_red_dot_radius");
            this.mContent.setAutoTextInfo(this.mMinTextSize, this.mStepGranularity, 1);
            this.mIsDirectionLand = isLand;
            this.mIndex = index;
            this.mIsTint = isTint;
            this.mStartImage.setImageDrawable(this.mLandComplexDrawable);
            this.mTopImage.setImageDrawable(this.mPortComplexDrawable);
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            setOrientation(1);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            updateTextAndIcon(true, true);
        }

        /* access modifiers changed from: package-private */
        public BottomNavigationItemView setActiveColor(int color) {
            this.mActiveColor = color;
            updateTextAndIcon(false, true);
            return this;
        }

        /* access modifiers changed from: package-private */
        public BottomNavigationItemView setDefaultColor(int color) {
            this.mDefaultColor = color;
            updateTextAndIcon(false, true);
            return this;
        }

        /* access modifiers changed from: package-private */
        public BottomNavigationItemView setTitleActiveColor(int color) {
            HwBottomNavigationView.this.mTitleActiveColor = color;
            updateTextAndIcon(false, true);
            return this;
        }

        /* access modifiers changed from: package-private */
        public BottomNavigationItemView setTitleDefaultColor(int color) {
            HwBottomNavigationView.this.mTitleDefaultColor = color;
            updateTextAndIcon(false, true);
            return this;
        }

        /* access modifiers changed from: package-private */
        public int getItemIndex() {
            return this.mIndex;
        }

        public void setChecked(boolean isChecked, boolean isHasAnim) {
            boolean z = false;
            if (this.mIsSingleImage) {
                this.mIsChecked = isChecked;
                this.mSingleComplexDrawable.setState(this.mIsChecked, false);
            } else if (isChecked != this.mIsChecked) {
                this.mIsChecked = isChecked;
                this.mCurrentDrawable = this.mIsDirectionLand ? this.mLandComplexDrawable : this.mPortComplexDrawable;
                ComplexDrawable complexDrawable = this.mCurrentDrawable;
                boolean z2 = this.mIsChecked;
                if (!HwWidgetFactory.isEmuiLite() && !HwWidgetFactory.isEmuiNovaPerformance() && isHasAnim) {
                    z = true;
                }
                complexDrawable.setState(z2, z);
                this.mContent.setTextColor(this.mIsChecked ? HwBottomNavigationView.this.mTitleActiveColor : HwBottomNavigationView.this.mTitleDefaultColor);
            }
        }

        private boolean isChecked() {
            return this.mIsChecked;
        }

        @Override // android.view.View
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            if (info != null) {
                super.onInitializeAccessibilityNodeInfo(info);
                info.setSelected(this.mIsChecked);
                if (isHasMessage()) {
                    info.setHintText(HwBottomNavigationView.this.mAccessAbilityMsgTxt);
                }
            }
        }

        @Override // android.view.View
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            if (event != null) {
                super.onInitializeAccessibilityEvent(event);
            }
        }

        @Override // android.view.View
        public boolean performClick() {
            boolean isHandled = super.performClick();
            sendAccessibilityEvent(ActionBarEx.DISPLAY_HW_NO_SPLIT_LINE);
            return isHandled;
        }

        /* access modifiers changed from: package-private */
        public void setDirection(boolean isDirectionLand) {
            if (isDirectionLand != this.mIsDirectionLand) {
                this.mIsDirectionLand = isDirectionLand;
            }
            if (this.mIsSingleImage) {
                updateSingleImage();
            } else {
                updateTextAndIcon(true, false);
            }
        }

        private void updateTextAndIcon(boolean isDirectionChanged, boolean isColorChanged) {
            ViewGroup.LayoutParams params = this.mContent.getLayoutParams();
            if (isDirectionChanged && (params instanceof ViewGroup.MarginLayoutParams)) {
                if (this.mIsDirectionLand) {
                    setGravity(17);
                    setMinimumHeight(this.mLandMinHeight);
                    int i = this.mHorizontalPadding;
                    setPadding(i, 0, i, 0);
                    this.mTopImage.setVisibility(8);
                    this.mStartImage.setVisibility(0);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) params;
                    layoutParams.setMargins(0, 0, 0, 0);
                    this.mContent.setLayoutParams(layoutParams);
                    this.mContent.setAutoTextSize(1, (float) this.mLandTextSize);
                    this.mContent.setGravity(8388611);
                    this.mCurrentDrawable = this.mLandComplexDrawable;
                } else {
                    if (getOrientation() != 1) {
                        setGravity(0);
                    }
                    setMinimumHeight(this.mPortMinHeight);
                    int i2 = this.mVerticalPadding;
                    setPadding(0, this.mVerticalAddedPadding + i2, 0, i2);
                    this.mTopImage.setVisibility(0);
                    this.mStartImage.setVisibility(8);
                    ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) params;
                    layoutParams2.setMargins(HwBottomNavigationView.this.mTextPadding, 0, HwBottomNavigationView.this.mTextPadding, 0);
                    this.mContent.setLayoutParams(layoutParams2);
                    this.mContent.setAutoTextSize(0, (float) this.mPortTextSize);
                    this.mContent.setGravity(1);
                    this.mCurrentDrawable = this.mPortComplexDrawable;
                }
                this.mContent.setText(this.mItem.getTitle());
                this.mCurrentDrawable.setState(this.mIsChecked, false);
            }
            if (isColorChanged) {
                if (this.mIsTint) {
                    this.mLandComplexDrawable.setActiveColor(this.mActiveColor);
                    this.mLandComplexDrawable.setDefaultColor(this.mDefaultColor);
                    this.mPortComplexDrawable.setActiveColor(this.mActiveColor);
                    this.mPortComplexDrawable.setDefaultColor(this.mDefaultColor);
                }
                this.mContent.setTextColor(this.mIsChecked ? HwBottomNavigationView.this.mTitleActiveColor : HwBottomNavigationView.this.mTitleDefaultColor);
            }
        }

        /* access modifiers changed from: package-private */
        public void replaceMenuItem(MenuItem item, boolean isTint) {
            this.mIsSingleImage = false;
            this.mIsTint = isTint;
            this.mSingleImage.setVisibility(8);
            this.mContainer.setVisibility(0);
            if (!this.mIsDirectionLand) {
                this.mTopImage.setVisibility(0);
            }
            this.mItem = item;
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;
                params.gravity = 48;
                setLayoutParams(params);
            }
            this.mLandComplexDrawable.setSrcDrawable(this.mItem.getIcon());
            this.mPortComplexDrawable.setSrcDrawable(this.mItem.getIcon());
            updateTextAndIcon(true, true);
        }

        /* access modifiers changed from: package-private */
        public void replaceSingleImage(MenuItem item, boolean isExtend) {
            this.mIsSingleImage = true;
            this.mIsExtend = isExtend;
            this.mContainer.setVisibility(8);
            this.mTopImage.setVisibility(8);
            this.mSingleImage.setVisibility(0);
            setGravity(81);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;
                if (!isExtend) {
                    params.gravity = 16;
                } else {
                    params.gravity = 48;
                }
                setLayoutParams(params);
            }
            this.mItem = item;
            this.mSingleComplexDrawable = new ComplexDrawable(this.mContext, item.getIcon(), getSingleImageSize());
            this.mSingleImage.setImageDrawable(this.mSingleComplexDrawable);
            updateSingleImage();
        }

        private void updateSingleImage() {
            if (this.mIsDirectionLand) {
                setMinimumHeight(this.mLandMinHeight);
                if (this.mIsExtend) {
                    int i = this.mHorizontalPadding;
                    setPadding(i, 0, i, 0);
                } else {
                    int i2 = this.mHorizontalPadding;
                    int i3 = this.mVerticalPadding;
                    setPadding(i2, i3, i2, i3);
                }
            } else {
                setMinimumHeight(this.mPortMinHeight);
                if (this.mIsExtend) {
                    setPadding(HwBottomNavigationView.this.mTextPadding, 0, HwBottomNavigationView.this.mTextPadding, 0);
                } else {
                    setPadding(HwBottomNavigationView.this.mTextPadding, this.mVerticalPadding, HwBottomNavigationView.this.mTextPadding, this.mVerticalPadding);
                }
            }
            int size = getSingleImageSize();
            ViewGroup.LayoutParams params = this.mSingleImage.getLayoutParams();
            params.width = size;
            params.height = size;
            this.mSingleImage.setLayoutParams(params);
            this.mSingleComplexDrawable.setDrawableSize(size);
            this.mSingleComplexDrawable.setState(this.mIsChecked, false);
        }

        private int getSingleImageSize() {
            if (!this.mIsDirectionLand && !this.mIsExtend) {
                return this.mPortNormalSize;
            }
            if (!this.mIsDirectionLand && this.mIsExtend) {
                return this.mPortExtendSize;
            }
            if (!this.mIsDirectionLand || this.mIsExtend) {
                return this.mLandExtendSize;
            }
            return this.mLandNormalSize;
        }

        /* access modifiers changed from: package-private */
        public boolean isSingleImageMode() {
            return this.mIsSingleImage;
        }

        /* access modifiers changed from: package-private */
        public boolean isExtend() {
            return this.mIsExtend;
        }

        /* access modifiers changed from: package-private */
        public TextView getContent() {
            return this.mContent;
        }

        /* access modifiers changed from: package-private */
        public ImageView getIcon() {
            return this.mIsDirectionLand ? this.mStartImage : this.mTopImage;
        }

        /* access modifiers changed from: package-private */
        public LinearLayout getContainer() {
            return this.mContainer;
        }

        /* access modifiers changed from: package-private */
        public boolean isHasMessage() {
            return this.mIsHasMessage;
        }

        /* access modifiers changed from: package-private */
        public void setHasMessage(boolean isHasMessage) {
            this.mIsHasMessage = isHasMessage;
            invalidate();
        }

        /* access modifiers changed from: package-private */
        public void setMsgBgColor(int color) {
            this.mMsgBgColor = color;
            this.mPaint.setColor(this.mMsgBgColor);
            invalidate();
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void dispatchDraw(Canvas canvas) {
            int cx;
            if (canvas == null) {
                Log.w(HwBottomNavigationView.TAG, "dispatchDraw: Param canvas is null");
                return;
            }
            super.dispatchDraw(canvas);
            if (this.mIsHasMessage && !this.mIsSingleImage) {
                ImageView icon = getIcon();
                Rect itemRect = new Rect();
                Rect iconRect = new Rect();
                getGlobalVisibleRect(itemRect);
                icon.getGlobalVisibleRect(iconRect);
                if (isLayoutRtl()) {
                    cx = (iconRect.left - itemRect.left) + this.mRedDotRadius;
                } else {
                    cx = (iconRect.right - itemRect.left) - this.mRedDotRadius;
                }
                int i = iconRect.top - itemRect.top;
                int i2 = this.mRedDotRadius;
                canvas.drawCircle((float) cx, (float) (i + i2), (float) i2, this.mPaint);
            }
        }

        @Override // android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            HwBottomNavigationView.this.updateFocusPos(event, keyCode);
            if (HwBottomNavigationView.this.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override // android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (HwBottomNavigationView.this.onKeyUp(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFocusPos(KeyEvent event, int keyCode) {
        View focusedChild = getChildAt(this.mFocusPos);
        if (focusedChild != null && focusedChild.isFocused()) {
            if (getOrientation() == 1) {
                updateFocusPosVertical(keyCode);
            } else {
                updateFocusPosHorizontal(keyCode);
            }
            if (keyCode == 61) {
                updateFocusPosByTab(event);
            }
        } else if (this.mFocusPos == getChildCount()) {
            this.mFocusPos--;
        }
    }

    private void updateFocusPosHorizontal(int keyCode) {
        boolean isRtl = isLayoutRtl();
        if (keyCode == 21) {
            if (!isRtl) {
                int i = this.mFocusPos;
                if (i > 0) {
                    this.mFocusPos = i - 1;
                }
            } else if (this.mFocusPos < getChildCount()) {
                this.mFocusPos++;
            }
        } else if (keyCode != 22) {
        } else {
            if (isRtl) {
                int i2 = this.mFocusPos;
                if (i2 > 0) {
                    this.mFocusPos = i2 - 1;
                }
            } else if (this.mFocusPos < getChildCount()) {
                this.mFocusPos++;
            }
        }
    }

    private void updateFocusPosVertical(int keyCode) {
        int i;
        if (keyCode != 21 && keyCode != 22) {
            if (keyCode == 19 && (i = this.mFocusPos) > 0) {
                this.mFocusPos = i - 1;
            } else if (keyCode == 20 && this.mFocusPos < getChildCount()) {
                this.mFocusPos++;
            }
        }
    }

    private void updateFocusPosByTab(KeyEvent event) {
        int i;
        if (event == null) {
            return;
        }
        if (event.isShiftPressed() && (i = this.mFocusPos) > 0) {
            this.mFocusPos = i - 1;
        } else if (!event.isShiftPressed() && this.mFocusPos < getChildCount() - 1) {
            this.mFocusPos++;
        }
    }

    /* access modifiers changed from: private */
    public class BottomNavItemClickListener implements View.OnClickListener {
        private BottomNavItemClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view instanceof BottomNavigationItemView) {
                HwBottomNavigationView.this.changeItem((BottomNavigationItemView) view, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public class BottomNavItemTouchListener implements View.OnTouchListener {
        private BottomNavItemTouchListener() {
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent event) {
            if (!(view instanceof BottomNavigationItemView)) {
                return false;
            }
            HwBottomNavigationView.this.mCurrentTouchedItem = (BottomNavigationItemView) view;
            return HwBottomNavigationView.this.mGestureDetector.onTouchEvent(event);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeToNextItem() {
        int count = getChildCount();
        if (count > 0) {
            int current = (this.mLastSelectItem + 1) % count;
            setItemChecked(current);
            if (current == this.mLastSelectItem) {
                int childCount = getChildCount();
                if (current >= 0 && current < childCount) {
                    View view = getChildAt(current);
                    if (view instanceof BottomNavigationItemView) {
                        view.requestFocus();
                    }
                }
            }
        }
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null || !hwKeyEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null || !hwKeyEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            hwKeyEventDetector.onDetachedFromWindow();
        }
        super.onDetachedFromWindow();
    }

    public void setExtendedNextTabEnabled(boolean isGlobal, boolean isEnabled) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            if (isGlobal) {
                if (isEnabled) {
                    hwKeyEventDetector.setOnGlobalNextTabListener(this, this.mOnGlobalNextTabListener);
                } else {
                    hwKeyEventDetector.setOnGlobalNextTabListener(this, (HwOnGlobalNextTabEventListener) null);
                }
                this.mIsGlobalNextTabEnable = isEnabled;
                return;
            }
            if (isEnabled) {
                hwKeyEventDetector.setOnNextTabListener(this.mOnNextTabListener);
            } else {
                hwKeyEventDetector.setOnNextTabListener((HwOnNextTabEventListener) null);
            }
            this.mIsNextTabEnable = isEnabled;
        }
    }

    public boolean isExtendedNextTabEnabled(boolean isGlobal) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null) {
            return false;
        }
        if (isGlobal) {
            if (hwKeyEventDetector.getOnGlobalNextTabListener() != null) {
                return true;
            }
            return false;
        } else if (hwKeyEventDetector.getOnNextTabListener() != null) {
            return true;
        } else {
            return false;
        }
    }
}
