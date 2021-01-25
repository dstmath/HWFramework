package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.List;

public class HwBottomNavigationView extends LinearLayout {
    private static final int ATTR_ICON_INDEX = 1;
    private static final int ATTR_TEXT_INDEX = 0;
    private static final int CHILD_COUNT_TWO = 2;
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int DICHOTOMY_SIZE = 2;
    private static final int DOUBLE_SIZE = 2;
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final float ITEM_NUM_MAX = 5.0f;
    private static final int ITEM_NUM_THREAD = 3;
    private static final int MENU_MAX_SIZE_WARNING = 5;
    private static final int SPACE_THREAD = 104;
    private static final String TAG = "HwBottomNavigationView";
    private int mActiveColor;
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private BottomNavItemClickListener mBottomNavItemClickListener;
    private ClickEffectEntry mClickEffectEntry;
    private HwColumnSystem mColumnSystem;
    private float mColumnUserSetDensity;
    private int mColumnUserSetHeight;
    private int mColumnUserSetWidth;
    private int mColumnWidth;
    private Context mContext;
    private int mDefaultColor;
    private Drawable mDivider;
    private HwKeyEventDetector mHwKeyEventDetector;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private int mIconBounds;
    private ColorStateList mIconColor;
    private boolean mIsBlurEnable;
    private boolean mIsColumnConfigured;
    private boolean mIsColumnEnabled;
    private boolean mIsGlobalNextTabEnable;
    private boolean mIsNextTabEnable;
    private boolean mIsPortLayout;
    private boolean mIsSpaceEnough;
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
    private int mSmallWidth;
    private final Rect mTempRect;
    private ColorStateList mTextColor;
    private int mTextPadding;

    public interface BottemNavListener {
        void onBottemNavItemReselected(MenuItem menuItem, int i);

        void onBottemNavItemSelected(MenuItem menuItem, int i);

        void onBottemNavItemUnselected(MenuItem menuItem, int i);
    }

    public HwBottomNavigationView(Context context) {
        this(context, null);
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "bottomNavStyle"));
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public HwBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempRect = new Rect();
        this.mLastSelectItem = -1;
        this.mIsPortLayout = false;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.mIsBlurEnable = false;
        this.mClickEffectEntry = null;
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mMeasureSize = new MeasureSize();
        this.mIsNextTabEnable = true;
        this.mIsGlobalNextTabEnable = true;
        this.mHwKeyEventDetector = null;
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
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
        initAttribute(context, attrs, defStyleAttr, defStyleRes);
        this.mBottomNavItemClickListener = new BottomNavItemClickListener();
        this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(this.mContext);
        initColumnLayout();
        createNewItem(this.mMenu);
    }

    private void initAttribute(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = ResLoader.getInstance().getContext(context);
        this.mMenu = new MenuBuilder(this.mContext);
        this.mMenuInflater = new MenuInflater(this.mContext);
        ResLoader resLoader = ResLoader.getInstance();
        Resources resources = resLoader.getResources(this.mContext);
        Resources.Theme theme = resLoader.getTheme(context);
        TypedArray typedArray = theme.obtainStyledAttributes(attrs, resLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation"), defStyleAttr, defStyleRes);
        int menuResId = typedArray.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavMenu"), 0);
        int defatultColor = typedArray.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavItemDefaultColor"), resLoader.getIdentifier(getContext(), ResLoaderUtil.COLOR, "emui_color_gray_7"));
        int activeColor = typedArray.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavItemActiveColor"), resLoader.getIdentifier(getContext(), ResLoaderUtil.COLOR, "emui_accent"));
        int messageColor = typedArray.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomMessageBgColor"), resLoader.getIdentifier(getContext(), ResLoaderUtil.COLOR, "bottom_nav_message_bg"));
        int bottomDividerId = typedArray.getResourceId(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavDivider"), -1);
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(typedArray.getInteger(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavBlurType"), DEFAULT_BLUR_TYPE));
        if (blurType != null) {
            this.mBlurType = blurType;
        }
        this.mBlurOverlayColor = typedArray.getColor(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_bottomNavBlurOverlayColor"), INVALID_BLUR_OVERLAY_COLOR);
        this.mIsColumnEnabled = typedArray.getBoolean(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "BottomNavigation_hwColumnEnabled"), false);
        typedArray.recycle();
        parseTextAndIconColor(theme);
        this.mClickEffectEntry = HwWidgetUtils.getCleckEffectEntry(context, defStyleAttr);
        this.mDefaultColor = resources.getColor(defatultColor);
        this.mActiveColor = resources.getColor(activeColor);
        this.mMessageBgColor = resources.getColor(messageColor);
        this.mTextPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_text_margin");
        this.mIconBounds = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_icon_size");
        if (bottomDividerId != -1) {
            this.mDivider = resources.getDrawable(bottomDividerId, theme);
        }
        if (menuResId > 0) {
            this.mMenuInflater.inflate(menuResId, this.mMenu);
        }
    }

    private void initColumnLayout() {
        int i;
        this.mColumnSystem = new HwColumnSystem(this.mContext);
        this.mIsColumnConfigured = false;
        this.mColumnWidth = getDefaultColumnLayoutWidth(this.mColumnSystem, this.mMenu.size());
        setGravity(1);
        if (!this.mIsColumnEnabled || (i = this.mLargeWidth) <= 0) {
            this.mIsSpaceEnough = isSpaceEnough((this.mContext.getResources().getDisplayMetrics().widthPixels - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mIsSpaceEnough = isSpaceEnough(i);
        }
    }

    private void parseTextAndIconColor(Resources.Theme theme) {
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{33620147, 34668564});
        this.mTextColor = typedArray.getColorStateList(0);
        this.mIconColor = typedArray.getColorStateList(1);
        typedArray.recycle();
        ColorStateList colorStateList = this.mIconColor;
        if (colorStateList != null) {
            this.mActiveColor = colorStateList.getColorForState(SELECTED_STATE_SET, this.mActiveColor);
            this.mDefaultColor = this.mIconColor.getDefaultColor();
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

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        return super.onApplyWindowInsets(insets);
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

    private boolean addMenu(int group, int id, int categoryOrder, CharSequence title, Drawable iconRes) {
        MenuItem item = this.mMenu.add(group, id, categoryOrder, title);
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
        createNewItem(item, this.mMenuSize - 1);
        return isMenuSizeReasonable(this.mMenu);
    }

    public boolean addMenu(CharSequence title, Drawable iconRes) {
        return addMenu(0, 0, 0, title, iconRes);
    }

    public boolean addMenu(int titleRes, Drawable iconRes) {
        return addMenu(0, 0, 0, titleRes, iconRes);
    }

    private boolean addMenu(int group, int id, int categoryOrder, int titleRes, Drawable iconRes) {
        MenuItem item = this.mMenu.add(0, 0, 0, titleRes).setIcon(iconRes);
        this.mMenuSize = this.mMenu.size();
        createNewItem(item, this.mMenuSize - 1);
        return isMenuSizeReasonable(this.mMenu);
    }

    public void addView(View child, int index, LinearLayout.LayoutParams params) {
        if (!(child instanceof BottomNavigationItemView)) {
            Log.w(TAG, "illegal to addView by this method");
        } else {
            super.addView(child, index, (ViewGroup.LayoutParams) params);
        }
    }

    public void notifyScrollToTop(View scollView) {
        if ((scollView instanceof ScrollCallback) && scollView.getVisibility() == 0) {
            ((ScrollCallback) scollView).scrollToTop();
        }
    }

    public void notifyDotMessage(int index, boolean isHasMessage) {
        if (index < this.mMenuSize) {
            ((BottomNavigationItemView) getChildAt(index)).setHasMessage(isHasMessage);
        }
    }

    public boolean isHasMessage(int index) {
        if (index < this.mMenuSize) {
            return ((BottomNavigationItemView) getChildAt(index)).isHasMessage();
        }
        return false;
    }

    public void setMessageBgColor(int color) {
        this.mMessageBgColor = color;
        for (int i = 0; i < this.mMenuSize; i++) {
            ((BottomNavigationItemView) getChildAt(i)).setMsgBgColor(color);
        }
    }

    private void createNewItem(Menu menu) {
        this.mMenuSize = menu.size();
        for (int i = 0; i < this.mMenuSize; i++) {
            createNewItem(menu.getItem(i), i);
        }
    }

    private void createNewItem(MenuItem menuItem, int index) {
        BottomNavigationItemView itemView = new BottomNavigationItemView(this.mContext, menuItem, this.mIsSpaceEnough, index);
        itemView.setClickable(true);
        itemView.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, this.mClickEffectEntry));
        itemView.setActiveColor(this.mActiveColor);
        itemView.setDefaultColor(this.mDefaultColor);
        itemView.setTextColor(this.mTextColor);
        itemView.setMsgBgColor(this.mMessageBgColor);
        itemView.setOnClickListener(this.mBottomNavItemClickListener);
        addView(itemView);
    }

    private boolean isSpaceEnough(int totalWidth) {
        float spaceThread = 104.0f * getResources().getDisplayMetrics().density;
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

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        boolean isColumnEnabled = this.mIsColumnEnabled;
        int placeWidth = (View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        if (childCount == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
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
        if (isEnough != this.mIsSpaceEnough) {
            this.mIsSpaceEnough = isEnough;
            for (int i2 = 0; i2 < childCount; i2++) {
                ((BottomNavigationItemView) getChildAt(i2)).setDirection(this.mIsSpaceEnough);
            }
        }
        this.mMeasureSize.init();
        if (this.mIsSpaceEnough) {
            onMeasureLand(widthMeasureSpec, heightMeasureSpec);
        } else {
            onMeasurePort(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMeasureSize.getHeight(), 1073741824));
    }

    private void onMeasureLand(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int placeWidth = (totalWidth - getPaddingLeft()) - getPaddingRight();
        int maxHeight = 0;
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        int childCount = getChildCount();
        if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
            if (i >= placeWidth) {
                i = placeWidth;
            }
            placeWidth = i;
        }
        int averageWidth = placeWidth / childCount;
        for (int i2 = 0; i2 < childCount; i2++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) getChildAt(i2);
            setStableWidth(itemView, averageWidth);
            itemView.setPadding(itemView.mHorizontalPadding, itemView.getPaddingTop(), itemView.mHorizontalPadding, itemView.getPaddingBottom());
            itemView.measure(View.MeasureSpec.makeMeasureSpec(averageWidth, 1073741824), itemHeightSpec);
            setMarginHorizontal(itemView.getContainer(), 0, 0);
            int height = itemView.getMeasuredHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        this.mMeasureSize.setWidth(totalWidth);
        this.mMeasureSize.setHeight(maxHeight + heightPadding);
    }

    private void onMeasurePort(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount == 2 || childCount == 1) {
            measureOnPortraitByAverageWidth(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureOnPortraitByAutoWidth(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureOnPortraitByAverageWidth(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int maxHeight = 0;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int placeWidth = (totalWidth - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
            if (i >= placeWidth) {
                i = placeWidth;
            }
            placeWidth = i;
        }
        int averageWidth = placeWidth / childCount;
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        for (int i2 = 0; i2 < childCount; i2++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) getChildAt(i2);
            itemView.measure(View.MeasureSpec.makeMeasureSpec(averageWidth, 1073741824), itemHeightSpec);
            setStableWidth(itemView, averageWidth);
            LinearLayout container = itemView.getContainer();
            ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams containerLayoutParams = (LinearLayout.LayoutParams) layoutParams;
                containerLayoutParams.gravity = 1;
                container.setLayoutParams(containerLayoutParams);
            }
            int height = itemView.getMeasuredHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        this.mMeasureSize.setWidth(totalWidth);
        this.mMeasureSize.setHeight(maxHeight + heightPadding);
    }

    private void measureOnPortraitByAutoWidth(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int totalWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        float placeWidth = (float) ((totalWidth - getPaddingLeft()) - getPaddingRight());
        int childCount = getChildCount();
        if (this.mIsColumnEnabled && (i = this.mColumnWidth) > 0) {
            placeWidth = ((float) i) < placeWidth ? (float) i : placeWidth;
        }
        float averageWidth = placeWidth / ((float) childCount);
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = getChildMeasureSpec(heightMeasureSpec, heightPadding, -2);
        List<Float> dataList = new ArrayList<>(childCount);
        for (int i2 = 0; i2 < childCount; i2++) {
            generateOffsetList(dataList, i2, averageWidth);
        }
        measureLongItemOnPortrait(averageWidth, itemHeightSpec, dataList);
        measureShortItemOnPortrait(averageWidth, itemHeightSpec, dataList);
        this.mMeasureSize.setWidth(totalWidth);
        MeasureSize measureSize = this.mMeasureSize;
        measureSize.setHeight(measureSize.getHeight() + heightPadding);
    }

    private void measureLongItemOnPortrait(float averageWidth, int itemHeightSpec, List<Float> dataList) {
        int childCount = getChildCount();
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) getChildAt(i);
            if (dataList.get(i).floatValue() < 0.0f) {
                ImageView icon = itemView.getIcon();
                ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
                if (layoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams iconLayoutParams = (LinearLayout.LayoutParams) layoutParams;
                    iconLayoutParams.gravity = 1;
                    setMarginHorizontal(icon, 0, 0, iconLayoutParams);
                }
                setMarginHorizontal(itemView.getContainer(), 0, 0);
                adjustWidthOnPortrait(averageWidth, itemHeightSpec, dataList, i, itemView);
                int height = itemView.getMeasuredHeight();
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }
        this.mMeasureSize.setHeight(maxHeight);
    }

    private void adjustWidthOnPortrait(float averageWidth, int itemHeightSpec, List<Float> dataList, int curIndex, BottomNavigationItemView itemView) {
        int correctWidth;
        int childCount = getChildCount();
        if (curIndex == 0 || curIndex == childCount - 1) {
            itemView.measure(View.MeasureSpec.makeMeasureSpec((int) averageWidth, 1073741824), itemHeightSpec);
            correctWidth = (int) averageWidth;
        } else {
            float startData = dataList.get(curIndex - 1).floatValue();
            float endData = dataList.get(curIndex + 1).floatValue();
            if (startData < 0.0f || endData < 0.0f) {
                itemView.measure(View.MeasureSpec.makeMeasureSpec((int) averageWidth, 1073741824), itemHeightSpec);
                correctWidth = (int) averageWidth;
            } else {
                float minWidth = startData > endData ? endData : startData;
                float current = dataList.get(curIndex).floatValue();
                BottomNavigationItemView leftItem = (BottomNavigationItemView) getChildAt(curIndex - 1);
                BottomNavigationItemView rightItem = (BottomNavigationItemView) getChildAt(curIndex + 1);
                if ((current / 2.0f) + minWidth > 0.0f) {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec((int) (averageWidth - current), 1073741824), itemHeightSpec);
                    leftItem.mEndRent = (-current) / 2.0f;
                    rightItem.mStartRent = (-current) / 2.0f;
                    correctWidth = (int) (averageWidth - current);
                } else {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec((int) (averageWidth + (minWidth * 2.0f)), 1073741824), itemHeightSpec);
                    leftItem.mEndRent = minWidth;
                    rightItem.mStartRent = minWidth;
                    correctWidth = (int) ((2.0f * minWidth) + averageWidth);
                }
            }
        }
        itemView.mIsMeasured = true;
        setStableWidth(itemView, correctWidth);
    }

    private void measureShortItemOnPortrait(float averageWidth, int itemHeightSpec, List<Float> dataList) {
        int childCount = getChildCount();
        int maxHeight = this.mMeasureSize.getHeight();
        for (int i = 0; i < childCount; i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) getChildAt(i);
            if (itemView.mIsMeasured) {
                itemView.mIsMeasured = false;
            } else {
                float current = dataList.get(i).floatValue();
                setMarginHorizontal(itemView.getContainer(), (int) (current - itemView.mStartRent), (int) (current - itemView.mEndRent));
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
                int height = itemView.getMeasuredHeight();
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }
        this.mMeasureSize.setHeight(maxHeight);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mHwWidgetSafeInsets.applyDisplaySafeInsets(true);
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        this.mHwWidgetSafeInsets.updateOriginPadding(left, top, right, bottom);
    }

    private void generateOffsetList(List<Float> dataList, int index, float standardData) {
        float size = standardData - (Layout.getDesiredWidth(this.mMenu.getItem(index).getTitle(), ((BottomNavigationItemView) getChildAt(index)).getContent().getPaint()) + ((float) (this.mTextPadding * 2)));
        if (size > 0.0f) {
            dataList.add(Float.valueOf(size / 2.0f));
        } else {
            dataList.add(Float.valueOf(size));
        }
    }

    private void setMarginHorizontal(View view, int marginStart, int marginEnd) {
        setMarginHorizontal(view, marginStart, marginEnd, (ViewGroup.MarginLayoutParams) view.getLayoutParams());
    }

    private void setMarginHorizontal(View view, int marginStart, int marginEnd, ViewGroup.MarginLayoutParams layoutParams) {
        layoutParams.setMarginStart(marginStart);
        layoutParams.setMarginEnd(marginEnd);
        view.setLayoutParams(layoutParams);
    }

    private void setStableWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    public void setBottemNavListener(BottemNavListener listener) {
        this.mListener = listener;
    }

    public void setItemChecked(int index) {
        int childCount = getChildCount();
        if (index >= 0 && index < childCount) {
            BottomNavigationItemView view = (BottomNavigationItemView) getChildAt(index);
            view.setChecked(true, this.mLastSelectItem != -1);
            changeItem(view, false);
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
        int index = itemView.getItemIndex();
        int i = this.mLastSelectItem;
        if (index == i) {
            BottemNavListener bottemNavListener = this.mListener;
            if (bottemNavListener != null) {
                bottemNavListener.onBottemNavItemReselected(this.mMenu.getItem(index), index);
                return;
            }
            return;
        }
        if (i < this.mMenuSize && i >= 0) {
            ((BottomNavigationItemView) getChildAt(i)).setChecked(false, true);
            BottemNavListener bottemNavListener2 = this.mListener;
            if (bottemNavListener2 != null) {
                bottemNavListener2.onBottemNavItemUnselected(this.mMenu.getItem(this.mLastSelectItem), this.mLastSelectItem);
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

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawDivider(canvas);
    }

    private void drawDivider(Canvas canvas) {
        if (this.mDivider != null) {
            Rect bounds = this.mTempRect;
            bounds.left = this.mPaddingLeft;
            bounds.right = (this.mRight - this.mLeft) - this.mPaddingRight;
            bounds.top = 0;
            bounds.bottom = this.mDivider.getIntrinsicHeight();
            this.mDivider.setBounds(bounds);
            this.mDivider.draw(canvas);
        }
    }

    private void setBlurColor(int blurColor) {
        this.mBlurOverlayColor = blurColor;
    }

    private void setBlurType(int blurTypeId) {
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
        if (blurType != null) {
            this.mBlurType = blurType;
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
        private int mActiveColor;
        private LinearLayout mContainer;
        private HwTextView mContent;
        private ComplexDrawable mCurrentDrawable;
        private int mDefaultColor;
        float mEndRent;
        private int mHorizontalPadding;
        private int mIndex;
        private boolean mIsChecked;
        private boolean mIsDirectionLand;
        private boolean mIsHasMessage;
        boolean mIsMeasured;
        private MenuItem mItem;
        private ComplexDrawable mLandComplexDrawable;
        private int mLandMinHeight;
        private int mLandTextSize;
        private int mMinTextSize;
        private int mMsgBgColor;
        private Paint mPaint = new Paint();
        private ComplexDrawable mPortComplexDrawable;
        private int mPortMinHeight;
        private int mPortTextSize;
        private int mRedDotRadius;
        private ImageView mStartImage;
        float mStartRent;
        private int mStepGranularity;
        private ColorStateList mTextColor;
        private ImageView mTopImage;
        private int mVerticalAddedPadding;
        private int mVerticalPadding;

        BottomNavigationItemView(Context context, MenuItem menuItem, boolean isLand, int index) {
            super(context);
            this.mItem = menuItem;
            inflate(context, ResLoaderUtil.getLayoutId(context, "bottomnav_item_layout"), this);
            this.mContent = (HwTextView) findViewById(ResLoaderUtil.getViewId(context, "content"));
            this.mTopImage = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "topIcon"));
            this.mStartImage = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "startIcon"));
            this.mContainer = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "container"));
            this.mLandComplexDrawable = new ComplexDrawable(context, this.mItem.getIcon());
            this.mPortComplexDrawable = new ComplexDrawable(context, this.mItem.getIcon());
            this.mLandMinHeight = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_land_minheight");
            this.mPortMinHeight = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_port_minheight");
            this.mPortTextSize = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_item_port_textsize"));
            this.mLandTextSize = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_item_land_textsize"));
            this.mStepGranularity = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_text_stepgranularity"));
            this.mMinTextSize = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_item_min_textsize"));
            this.mVerticalPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_vertical_padding");
            this.mHorizontalPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_horizontal_padding");
            this.mVerticalAddedPadding = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_add_top_margin");
            this.mRedDotRadius = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_red_dot_radius");
            this.mContent.setAutoTextInfo(this.mMinTextSize, this.mStepGranularity, 1);
            this.mIsDirectionLand = isLand;
            this.mIndex = index;
            this.mStartImage.setImageDrawable(this.mLandComplexDrawable);
            this.mTopImage.setImageDrawable(this.mPortComplexDrawable);
            this.mPaint.setAntiAlias(true);
            setOrientation(1);
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
        public void setTextColor(ColorStateList textColor) {
            this.mTextColor = textColor;
            updateTextAndIcon(false, true);
        }

        /* access modifiers changed from: package-private */
        public int getItemIndex() {
            return this.mIndex;
        }

        private boolean isChecked() {
            return this.mIsChecked;
        }

        /* access modifiers changed from: package-private */
        public void setChecked(boolean isChecked, boolean isAnim) {
            if (isChecked != this.mIsChecked) {
                this.mIsChecked = isChecked;
                this.mCurrentDrawable = this.mIsDirectionLand ? this.mLandComplexDrawable : this.mPortComplexDrawable;
                this.mCurrentDrawable.setState(this.mIsChecked, !HwWidgetFactory.isEmuiLite() && !HwWidgetFactory.isEmuiNovaPerformance() && isAnim);
                this.mContent.setSelected(isChecked);
            }
        }

        @Override // android.view.View
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            if (info != null) {
                super.onInitializeAccessibilityNodeInfo(info);
                info.setSelected(this.mIsChecked);
            }
        }

        @Override // android.view.View
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            if (event != null) {
                super.onInitializeAccessibilityEvent(event);
                CharSequence text = this.mItem.getTitle();
                if (event.getEventType() == 32768 && !TextUtils.isEmpty(text)) {
                    event.getText().add(text);
                }
            }
        }

        @Override // android.view.View
        public boolean performClick() {
            boolean result = super.performClick();
            sendAccessibilityEvent(ActionBarEx.DISPLAY_HW_NO_SPLIT_LINE);
            return result;
        }

        /* access modifiers changed from: package-private */
        public void setDirection(boolean isDirectionLand) {
            if (isDirectionLand != this.mIsDirectionLand) {
                this.mIsDirectionLand = isDirectionLand;
            }
            updateTextAndIcon(true, false);
        }

        private void updateTextAndIcon(boolean isDirectionChanged, boolean isColorChanged) {
            if (isDirectionChanged) {
                if (this.mIsDirectionLand) {
                    setGravity(17);
                    setMinimumHeight(this.mLandMinHeight);
                    int i = this.mHorizontalPadding;
                    setPaddingRelative(i, 0, i, 0);
                    this.mTopImage.setVisibility(8);
                    this.mStartImage.setVisibility(0);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.mContent.getLayoutParams();
                    layoutParams.setMarginsRelative(0, 0, 0, 0);
                    this.mContent.setLayoutParams(layoutParams);
                    this.mContent.setAutoTextSize(1, (float) this.mLandTextSize);
                    this.mContent.setGravity(8388611);
                    this.mCurrentDrawable = this.mLandComplexDrawable;
                } else {
                    setGravity(0);
                    setMinimumHeight(this.mPortMinHeight);
                    int i2 = this.mVerticalPadding;
                    setPaddingRelative(0, this.mVerticalAddedPadding + i2, 0, i2);
                    this.mTopImage.setVisibility(0);
                    this.mStartImage.setVisibility(8);
                    ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) this.mContent.getLayoutParams();
                    layoutParams2.setMarginsRelative(HwBottomNavigationView.this.mTextPadding, 0, HwBottomNavigationView.this.mTextPadding, 0);
                    this.mContent.setLayoutParams(layoutParams2);
                    this.mContent.setAutoTextSize(1, (float) this.mPortTextSize);
                    this.mContent.setGravity(1);
                    this.mCurrentDrawable = this.mPortComplexDrawable;
                }
                this.mContent.setText(this.mItem.getTitle());
                this.mCurrentDrawable.setState(this.mIsChecked, false);
            }
            if (isColorChanged) {
                this.mLandComplexDrawable.setActiveColor(this.mActiveColor);
                this.mLandComplexDrawable.setDefaultColor(this.mDefaultColor);
                this.mPortComplexDrawable.setActiveColor(this.mActiveColor);
                this.mPortComplexDrawable.setDefaultColor(this.mDefaultColor);
                ColorStateList colorStateList = this.mTextColor;
                if (colorStateList != null) {
                    this.mContent.setTextColor(colorStateList);
                }
            }
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
            int centerX;
            super.dispatchDraw(canvas);
            if (this.mIsHasMessage) {
                ImageView icon = getIcon();
                Rect itemRect = new Rect();
                Rect iconRect = new Rect();
                getGlobalVisibleRect(itemRect);
                icon.getGlobalVisibleRect(iconRect);
                if (isLayoutRtl()) {
                    centerX = (iconRect.left - itemRect.left) + this.mRedDotRadius;
                } else {
                    centerX = (iconRect.right - itemRect.left) - this.mRedDotRadius;
                }
                int i = iconRect.top - itemRect.top;
                int i2 = this.mRedDotRadius;
                canvas.drawCircle((float) centerX, (float) (i + i2), (float) i2, this.mPaint);
            }
        }

        @Override // android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (HwBottomNavigationView.this.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override // android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (HwBottomNavigationView.this.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    /* access modifiers changed from: private */
    public class BottomNavItemClickListener implements View.OnClickListener {
        private BottomNavigationItemView itemView;

        private BottomNavItemClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view instanceof BottomNavigationItemView) {
                this.itemView = (BottomNavigationItemView) view;
                HwBottomNavigationView.this.changeItem(this.itemView, true);
            }
        }
    }
}
