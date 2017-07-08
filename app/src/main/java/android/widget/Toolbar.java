package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ToolbarWidgetWrapper;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.List;

public class Toolbar extends ViewGroup {
    private static final String TAG = "Toolbar";
    private Callback mActionMenuPresenterCallback;
    private int mButtonGravity;
    private ImageButton mCollapseButtonView;
    private CharSequence mCollapseDescription;
    private Drawable mCollapseIcon;
    private boolean mCollapsible;
    private int mContentInsetEndWithActions;
    private int mContentInsetStartWithNavigation;
    private final RtlSpacingHelper mContentInsets;
    private boolean mEatingTouch;
    View mExpandedActionView;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private int mGravity;
    private final ArrayList<View> mHiddenViews;
    private ImageView mLogoView;
    private int mMaxButtonHeight;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private ActionMenuView mMenuView;
    private final android.widget.ActionMenuView.OnMenuItemClickListener mMenuViewItemClickListener;
    private int mNavButtonStyle;
    private ImageButton mNavButtonView;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private ActionMenuPresenter mOuterActionMenuPresenter;
    private Context mPopupContext;
    private int mPopupTheme;
    private final Runnable mShowOverflowMenuRunnable;
    private CharSequence mSubtitleText;
    private int mSubtitleTextAppearance;
    private int mSubtitleTextColor;
    private TextView mSubtitleTextView;
    private final int[] mTempMargins;
    private final ArrayList<View> mTempViews;
    private int mTitleMarginBottom;
    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    private int mTitleMarginTop;
    private CharSequence mTitleText;
    private int mTitleTextAppearance;
    private int mTitleTextColor;
    private TextView mTitleTextView;
    private ToolbarWidgetWrapper mWrapper;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        public void initForMenu(Context context, MenuBuilder menu) {
            if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menu;
        }

        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                boolean found = false;
                if (this.mMenu != null) {
                    int count = this.mMenu.size();
                    for (int i = 0; i < count; i++) {
                        if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
                }
            }
        }

        public void setCallback(Callback cb) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean flagActionItems() {
            return false;
        }

        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            Toolbar.this.ensureCollapseButtonView();
            if (Toolbar.this.mCollapseButtonView.getParent() != Toolbar.this) {
                Toolbar.this.addView(Toolbar.this.mCollapseButtonView);
            }
            Toolbar.this.mExpandedActionView = item.getActionView();
            this.mCurrentExpandedItem = item;
            if (Toolbar.this.mExpandedActionView.getParent() != Toolbar.this) {
                LayoutParams lp = Toolbar.this.generateDefaultLayoutParams();
                lp.gravity = (Toolbar.this.mButtonGravity & LogPower.APP_PROCESS_EXIT) | Gravity.START;
                lp.mViewType = 2;
                Toolbar.this.mExpandedActionView.setLayoutParams(lp);
                Toolbar.this.addView(Toolbar.this.mExpandedActionView);
            }
            Toolbar.this.removeChildrenForExpandedActionView();
            Toolbar.this.requestLayout();
            item.setActionViewExpanded(true);
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewCollapsed();
            }
            Toolbar.this.removeView(Toolbar.this.mExpandedActionView);
            Toolbar.this.removeView(Toolbar.this.mCollapseButtonView);
            Toolbar.this.mExpandedActionView = null;
            Toolbar.this.addChildrenForExpandedActionView();
            this.mCurrentExpandedItem = null;
            Toolbar.this.requestLayout();
            item.setActionViewExpanded(false);
            return true;
        }

        public int getId() {
            return 0;
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }
    }

    public static class LayoutParams extends android.app.ActionBar.LayoutParams {
        static final int CUSTOM = 0;
        static final int EXPANDED = 2;
        static final int SYSTEM = 1;
        int mViewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.mViewType = CUSTOM;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.mViewType = CUSTOM;
            this.gravity = 8388627;
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.mViewType = CUSTOM;
            this.gravity = gravity;
        }

        public LayoutParams(int gravity) {
            this(-2, -1, gravity);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.mViewType = CUSTOM;
            this.mViewType = source.mViewType;
        }

        public LayoutParams(android.app.ActionBar.LayoutParams source) {
            super(source);
            this.mViewType = CUSTOM;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.mViewType = CUSTOM;
            copyMarginsFrom(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.mViewType = CUSTOM;
        }
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        public int expandedMenuItemId;
        public boolean isOverflowOpen;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Toolbar.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Toolbar.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.Toolbar.SavedState.<clinit>():void");
        }

        public SavedState(Parcel source) {
            boolean z = false;
            super(source);
            this.expandedMenuItemId = source.readInt();
            if (source.readInt() != 0) {
                z = true;
            }
            this.isOverflowOpen = z;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.expandedMenuItemId);
            out.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.toolbarStyle);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContentInsets = new RtlSpacingHelper();
        this.mGravity = 8388627;
        this.mTempViews = new ArrayList();
        this.mHiddenViews = new ArrayList();
        this.mTempMargins = new int[2];
        this.mMenuViewItemClickListener = new android.widget.ActionMenuView.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (Toolbar.this.mOnMenuItemClickListener != null) {
                    return Toolbar.this.mOnMenuItemClickListener.onMenuItemClick(item);
                }
                return false;
            }
        };
        this.mShowOverflowMenuRunnable = new Runnable() {
            public void run() {
                Toolbar.this.showOverflowMenu();
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar, defStyleAttr, defStyleRes);
        this.mTitleTextAppearance = a.getResourceId(4, 0);
        this.mSubtitleTextAppearance = a.getResourceId(5, 0);
        this.mNavButtonStyle = a.getResourceId(27, 0);
        this.mGravity = a.getInteger(0, this.mGravity);
        this.mButtonGravity = a.getInteger(23, 48);
        int dimensionPixelOffset = a.getDimensionPixelOffset(17, 0);
        this.mTitleMarginBottom = dimensionPixelOffset;
        this.mTitleMarginTop = dimensionPixelOffset;
        this.mTitleMarginEnd = dimensionPixelOffset;
        this.mTitleMarginStart = dimensionPixelOffset;
        int marginStart = a.getDimensionPixelOffset(18, -1);
        if (marginStart >= 0) {
            this.mTitleMarginStart = marginStart;
        }
        int marginEnd = a.getDimensionPixelOffset(19, -1);
        if (marginEnd >= 0) {
            this.mTitleMarginEnd = marginEnd;
        }
        int marginTop = a.getDimensionPixelOffset(20, -1);
        if (marginTop >= 0) {
            this.mTitleMarginTop = marginTop;
        }
        int marginBottom = a.getDimensionPixelOffset(21, -1);
        if (marginBottom >= 0) {
            this.mTitleMarginBottom = marginBottom;
        }
        this.mMaxButtonHeight = a.getDimensionPixelSize(22, -1);
        int contentInsetStart = a.getDimensionPixelOffset(6, RtlSpacingHelper.UNDEFINED);
        int contentInsetEnd = a.getDimensionPixelOffset(7, RtlSpacingHelper.UNDEFINED);
        int contentInsetLeft = a.getDimensionPixelSize(8, 0);
        int contentInsetRight = a.getDimensionPixelSize(9, 0);
        this.mContentInsets.setAbsolute(contentInsetLeft, contentInsetRight);
        if (!(contentInsetStart == Integer.MIN_VALUE && contentInsetEnd == Integer.MIN_VALUE)) {
            this.mContentInsets.setRelative(contentInsetStart, contentInsetEnd);
        }
        this.mContentInsetStartWithNavigation = a.getDimensionPixelOffset(25, RtlSpacingHelper.UNDEFINED);
        this.mContentInsetEndWithActions = a.getDimensionPixelOffset(26, RtlSpacingHelper.UNDEFINED);
        this.mCollapseIcon = a.getDrawable(24);
        this.mCollapseDescription = a.getText(13);
        CharSequence title = a.getText(1);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        CharSequence subtitle = a.getText(3);
        if (!TextUtils.isEmpty(subtitle)) {
            setSubtitle(subtitle);
        }
        this.mPopupContext = this.mContext;
        setPopupTheme(a.getResourceId(10, 0));
        Drawable navIcon = a.getDrawable(11);
        if (navIcon != null) {
            setNavigationIcon(navIcon);
        }
        CharSequence navDesc = a.getText(12);
        if (!TextUtils.isEmpty(navDesc)) {
            setNavigationContentDescription(navDesc);
        }
        Drawable logo = a.getDrawable(2);
        if (logo != null) {
            setLogo(logo);
        }
        CharSequence logoDesc = a.getText(16);
        if (!TextUtils.isEmpty(logoDesc)) {
            setLogoDescription(logoDesc);
        }
        if (a.hasValue(14)) {
            setTitleTextColor(a.getColor(14, -1));
        }
        if (a.hasValue(15)) {
            setSubtitleTextColor(a.getColor(15, -1));
        }
        a.recycle();
    }

    public void setPopupTheme(int resId) {
        if (this.mPopupTheme != resId) {
            this.mPopupTheme = resId;
            if (resId == 0) {
                this.mPopupContext = this.mContext;
            } else {
                this.mPopupContext = new ContextThemeWrapper(this.mContext, resId);
            }
        }
    }

    public int getPopupTheme() {
        return this.mPopupTheme;
    }

    public void setTitleMargin(int start, int top, int end, int bottom) {
        this.mTitleMarginStart = start;
        this.mTitleMarginTop = top;
        this.mTitleMarginEnd = end;
        this.mTitleMarginBottom = bottom;
        requestLayout();
    }

    public int getTitleMarginStart() {
        return this.mTitleMarginStart;
    }

    public void setTitleMarginStart(int margin) {
        this.mTitleMarginStart = margin;
        requestLayout();
    }

    public int getTitleMarginTop() {
        return this.mTitleMarginTop;
    }

    public void setTitleMarginTop(int margin) {
        this.mTitleMarginTop = margin;
        requestLayout();
    }

    public int getTitleMarginEnd() {
        return this.mTitleMarginEnd;
    }

    public void setTitleMarginEnd(int margin) {
        this.mTitleMarginEnd = margin;
        requestLayout();
    }

    public int getTitleMarginBottom() {
        return this.mTitleMarginBottom;
    }

    public void setTitleMarginBottom(int margin) {
        this.mTitleMarginBottom = margin;
        requestLayout();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean z = true;
        super.onRtlPropertiesChanged(layoutDirection);
        RtlSpacingHelper rtlSpacingHelper = this.mContentInsets;
        if (layoutDirection != 1) {
            z = false;
        }
        rtlSpacingHelper.setDirection(z);
    }

    public void setLogo(int resId) {
        setLogo(getContext().getDrawable(resId));
    }

    public boolean canShowOverflowMenu() {
        return (getVisibility() != 0 || this.mMenuView == null) ? false : this.mMenuView.isOverflowReserved();
    }

    public boolean isOverflowMenuShowing() {
        return this.mMenuView != null ? this.mMenuView.isOverflowMenuShowing() : false;
    }

    public boolean isOverflowMenuShowPending() {
        return this.mMenuView != null ? this.mMenuView.isOverflowMenuShowPending() : false;
    }

    public boolean showOverflowMenu() {
        return this.mMenuView != null ? this.mMenuView.showOverflowMenu() : false;
    }

    public boolean hideOverflowMenu() {
        return this.mMenuView != null ? this.mMenuView.hideOverflowMenu() : false;
    }

    public void setMenu(MenuBuilder menu, ActionMenuPresenter outerPresenter) {
        if (menu != null || this.mMenuView != null) {
            ensureMenuView();
            MenuBuilder oldMenu = this.mMenuView.peekMenu();
            if (oldMenu != menu) {
                if (oldMenu != null) {
                    oldMenu.removeMenuPresenter(this.mOuterActionMenuPresenter);
                    oldMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
                }
                if (this.mExpandedMenuPresenter == null) {
                    this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
                }
                outerPresenter.setExpandedActionViewsExclusive(true);
                if (menu != null) {
                    menu.addMenuPresenter(outerPresenter, this.mPopupContext);
                    menu.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
                } else {
                    outerPresenter.initForMenu(this.mPopupContext, null);
                    this.mExpandedMenuPresenter.initForMenu(this.mPopupContext, null);
                    outerPresenter.updateMenuView(true);
                    this.mExpandedMenuPresenter.updateMenuView(true);
                }
                this.mMenuView.setPopupTheme(this.mPopupTheme);
                this.mMenuView.setPresenter(outerPresenter);
                this.mOuterActionMenuPresenter = outerPresenter;
            }
        }
    }

    public void dismissPopupMenus() {
        if (this.mMenuView != null) {
            this.mMenuView.dismissPopupMenus();
        }
    }

    public boolean isTitleTruncated() {
        if (this.mTitleTextView == null) {
            return false;
        }
        Layout titleLayout = this.mTitleTextView.getLayout();
        if (titleLayout == null) {
            return false;
        }
        int lineCount = titleLayout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            if (titleLayout.getEllipsisCount(i) > 0) {
                return true;
            }
        }
        return false;
    }

    public void setLogo(Drawable drawable) {
        if (drawable != null) {
            ensureLogoView();
            if (!isChildOrHidden(this.mLogoView)) {
                addSystemView(this.mLogoView, true);
            }
        } else if (this.mLogoView != null && isChildOrHidden(this.mLogoView)) {
            removeView(this.mLogoView);
            this.mHiddenViews.remove(this.mLogoView);
        }
        if (this.mLogoView != null) {
            this.mLogoView.setImageDrawable(drawable);
        }
    }

    public Drawable getLogo() {
        return this.mLogoView != null ? this.mLogoView.getDrawable() : null;
    }

    public void setLogoDescription(int resId) {
        setLogoDescription(getContext().getText(resId));
    }

    public void setLogoDescription(CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureLogoView();
        }
        if (this.mLogoView != null) {
            this.mLogoView.setContentDescription(description);
        }
    }

    public CharSequence getLogoDescription() {
        return this.mLogoView != null ? this.mLogoView.getContentDescription() : null;
    }

    private void ensureLogoView() {
        if (this.mLogoView == null) {
            this.mLogoView = new ImageView(getContext());
        }
    }

    public boolean hasExpandedActionView() {
        if (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) {
            return false;
        }
        return true;
    }

    public void collapseActionView() {
        MenuItemImpl item = null;
        if (this.mExpandedMenuPresenter != null) {
            item = this.mExpandedMenuPresenter.mCurrentExpandedItem;
        }
        if (item != null) {
            item.collapseActionView();
        }
    }

    public CharSequence getTitle() {
        return this.mTitleText;
    }

    public void setTitle(int resId) {
        setTitle(getContext().getText(resId));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (this.mTitleTextView == null) {
                this.mTitleTextView = new TextView(getContext());
                this.mTitleTextView.setSingleLine();
                this.mTitleTextView.setEllipsize(TruncateAt.END);
                if (this.mTitleTextAppearance != 0) {
                    this.mTitleTextView.setTextAppearance(this.mTitleTextAppearance);
                }
                if (this.mTitleTextColor != 0) {
                    this.mTitleTextView.setTextColor(this.mTitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mTitleTextView)) {
                addSystemView(this.mTitleTextView, true);
            }
        } else if (this.mTitleTextView != null && isChildOrHidden(this.mTitleTextView)) {
            removeView(this.mTitleTextView);
            this.mHiddenViews.remove(this.mTitleTextView);
        }
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setText(title);
        }
        this.mTitleText = title;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    public void setSubtitle(int resId) {
        setSubtitle(getContext().getText(resId));
    }

    public void setSubtitle(CharSequence subtitle) {
        if (!TextUtils.isEmpty(subtitle)) {
            if (this.mSubtitleTextView == null) {
                this.mSubtitleTextView = new TextView(getContext());
                this.mSubtitleTextView.setSingleLine();
                this.mSubtitleTextView.setEllipsize(TruncateAt.END);
                if (this.mSubtitleTextAppearance != 0) {
                    this.mSubtitleTextView.setTextAppearance(this.mSubtitleTextAppearance);
                }
                if (this.mSubtitleTextColor != 0) {
                    this.mSubtitleTextView.setTextColor(this.mSubtitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mSubtitleTextView)) {
                addSystemView(this.mSubtitleTextView, true);
            }
        } else if (this.mSubtitleTextView != null && isChildOrHidden(this.mSubtitleTextView)) {
            removeView(this.mSubtitleTextView);
            this.mHiddenViews.remove(this.mSubtitleTextView);
        }
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setText(subtitle);
        }
        this.mSubtitleText = subtitle;
    }

    public void setTitleTextAppearance(Context context, int resId) {
        this.mTitleTextAppearance = resId;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextAppearance(resId);
        }
    }

    public void setSubtitleTextAppearance(Context context, int resId) {
        this.mSubtitleTextAppearance = resId;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextAppearance(resId);
        }
    }

    public void setTitleTextColor(int color) {
        this.mTitleTextColor = color;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextColor(color);
        }
    }

    public void setSubtitleTextColor(int color) {
        this.mSubtitleTextColor = color;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextColor(color);
        }
    }

    public CharSequence getNavigationContentDescription() {
        return this.mNavButtonView != null ? this.mNavButtonView.getContentDescription() : null;
    }

    public void setNavigationContentDescription(int resId) {
        setNavigationContentDescription(resId != 0 ? getContext().getText(resId) : null);
    }

    public void setNavigationContentDescription(CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureNavButtonView();
        }
        if (this.mNavButtonView != null) {
            this.mNavButtonView.setContentDescription(description);
        }
    }

    public void setNavigationIcon(int resId) {
        setNavigationIcon(getContext().getDrawable(resId));
    }

    public void setNavigationIcon(Drawable icon) {
        if (icon != null) {
            ensureNavButtonView();
            if (!isChildOrHidden(this.mNavButtonView)) {
                addSystemView(this.mNavButtonView, true);
            }
        } else if (this.mNavButtonView != null && isChildOrHidden(this.mNavButtonView)) {
            removeView(this.mNavButtonView);
            this.mHiddenViews.remove(this.mNavButtonView);
        }
        if (this.mNavButtonView != null) {
            this.mNavButtonView.setImageDrawable(icon);
        }
    }

    public Drawable getNavigationIcon() {
        return this.mNavButtonView != null ? this.mNavButtonView.getDrawable() : null;
    }

    public void setNavigationOnClickListener(OnClickListener listener) {
        ensureNavButtonView();
        this.mNavButtonView.setOnClickListener(listener);
    }

    public View getNavigationView() {
        return this.mNavButtonView;
    }

    public Menu getMenu() {
        ensureMenu();
        return this.mMenuView.getMenu();
    }

    public void setOverflowIcon(Drawable icon) {
        ensureMenu();
        this.mMenuView.setOverflowIcon(icon);
    }

    public Drawable getOverflowIcon() {
        ensureMenu();
        return this.mMenuView.getOverflowIcon();
    }

    private void ensureMenu() {
        ensureMenuView();
        if (this.mMenuView.peekMenu() == null) {
            MenuBuilder menu = (MenuBuilder) this.mMenuView.getMenu();
            if (this.mExpandedMenuPresenter == null) {
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            this.mMenuView.setExpandedActionViewsExclusive(true);
            menu.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
        }
    }

    private void ensureMenuView() {
        if (this.mMenuView == null) {
            this.mMenuView = new ActionMenuView(getContext());
            this.mMenuView.setPopupTheme(this.mPopupTheme);
            this.mMenuView.setOnMenuItemClickListener(this.mMenuViewItemClickListener);
            this.mMenuView.setMenuCallbacks(this.mActionMenuPresenterCallback, this.mMenuBuilderCallback);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = (this.mButtonGravity & LogPower.APP_PROCESS_EXIT) | Gravity.END;
            this.mMenuView.setLayoutParams(lp);
            addSystemView(this.mMenuView, false);
        }
    }

    private MenuInflater getMenuInflater() {
        return new MenuInflater(getContext());
    }

    public void inflateMenu(int resId) {
        getMenuInflater().inflate(resId, getMenu());
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.mOnMenuItemClickListener = listener;
    }

    public void setContentInsetsRelative(int contentInsetStart, int contentInsetEnd) {
        this.mContentInsets.setRelative(contentInsetStart, contentInsetEnd);
    }

    public int getContentInsetStart() {
        return this.mContentInsets.getStart();
    }

    public int getContentInsetEnd() {
        return this.mContentInsets.getEnd();
    }

    public void setContentInsetsAbsolute(int contentInsetLeft, int contentInsetRight) {
        this.mContentInsets.setAbsolute(contentInsetLeft, contentInsetRight);
    }

    public int getContentInsetLeft() {
        return this.mContentInsets.getLeft();
    }

    public int getContentInsetRight() {
        return this.mContentInsets.getRight();
    }

    public int getContentInsetStartWithNavigation() {
        if (this.mContentInsetStartWithNavigation != RtlSpacingHelper.UNDEFINED) {
            return this.mContentInsetStartWithNavigation;
        }
        return getContentInsetStart();
    }

    public void setContentInsetStartWithNavigation(int insetStartWithNavigation) {
        if (insetStartWithNavigation < 0) {
            insetStartWithNavigation = RtlSpacingHelper.UNDEFINED;
        }
        if (insetStartWithNavigation != this.mContentInsetStartWithNavigation) {
            this.mContentInsetStartWithNavigation = insetStartWithNavigation;
            if (getNavigationIcon() != null) {
                requestLayout();
            }
        }
    }

    public int getContentInsetEndWithActions() {
        if (this.mContentInsetEndWithActions != RtlSpacingHelper.UNDEFINED) {
            return this.mContentInsetEndWithActions;
        }
        return getContentInsetEnd();
    }

    public void setContentInsetEndWithActions(int insetEndWithActions) {
        if (insetEndWithActions < 0) {
            insetEndWithActions = RtlSpacingHelper.UNDEFINED;
        }
        if (insetEndWithActions != this.mContentInsetEndWithActions) {
            this.mContentInsetEndWithActions = insetEndWithActions;
            if (getNavigationIcon() != null) {
                requestLayout();
            }
        }
    }

    public int getCurrentContentInsetStart() {
        if (getNavigationIcon() != null) {
            return Math.max(getContentInsetStart(), Math.max(this.mContentInsetStartWithNavigation, 0));
        }
        return getContentInsetStart();
    }

    public int getCurrentContentInsetEnd() {
        boolean hasActions = false;
        if (this.mMenuView != null) {
            MenuBuilder mb = this.mMenuView.peekMenu();
            hasActions = mb != null ? mb.hasVisibleItems() : false;
        }
        if (hasActions) {
            return Math.max(getContentInsetEnd(), Math.max(this.mContentInsetEndWithActions, 0));
        }
        return getContentInsetEnd();
    }

    public int getCurrentContentInsetLeft() {
        if (isLayoutRtl()) {
            return getCurrentContentInsetEnd();
        }
        return getCurrentContentInsetStart();
    }

    public int getCurrentContentInsetRight() {
        if (isLayoutRtl()) {
            return getCurrentContentInsetStart();
        }
        return getCurrentContentInsetEnd();
    }

    private void ensureNavButtonView() {
        if (this.mNavButtonView == null) {
            this.mNavButtonView = new ImageButton(getContext(), null, 0, this.mNavButtonStyle);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = (this.mButtonGravity & LogPower.APP_PROCESS_EXIT) | Gravity.START;
            this.mNavButtonView.setLayoutParams(lp);
        }
    }

    private void ensureCollapseButtonView() {
        if (this.mCollapseButtonView == null) {
            this.mCollapseButtonView = new ImageButton(getContext(), null, 0, this.mNavButtonStyle);
            this.mCollapseButtonView.setImageDrawable(this.mCollapseIcon);
            this.mCollapseButtonView.setContentDescription(this.mCollapseDescription);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = (this.mButtonGravity & LogPower.APP_PROCESS_EXIT) | Gravity.START;
            lp.mViewType = 2;
            this.mCollapseButtonView.setLayoutParams(lp);
            this.mCollapseButtonView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Toolbar.this.collapseActionView();
                }
            });
        }
    }

    private void addSystemView(View v, boolean allowHide) {
        LayoutParams lp;
        android.view.ViewGroup.LayoutParams vlp = v.getLayoutParams();
        if (vlp == null) {
            lp = generateDefaultLayoutParams();
        } else if (checkLayoutParams(vlp)) {
            lp = (LayoutParams) vlp;
        } else {
            lp = generateLayoutParams(vlp);
        }
        lp.mViewType = 1;
        if (!allowHide || this.mExpandedActionView == null) {
            addView(v, (android.view.ViewGroup.LayoutParams) lp);
            return;
        }
        v.setLayoutParams(lp);
        this.mHiddenViews.add(v);
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        Menu peekMenu = this.mMenuView != null ? this.mMenuView.peekMenu() : null;
        if (!(ss.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || peekMenu == null)) {
            MenuItem item = peekMenu.findItem(ss.expandedMenuItemId);
            if (item != null) {
                item.expandActionView();
            }
        }
        if (ss.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    private void postShowOverflowMenu() {
        removeCallbacks(this.mShowOverflowMenuRunnable);
        post(this.mShowOverflowMenuRunnable);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mShowOverflowMenuRunnable);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean handled = super.onTouchEvent(ev);
            if (action == 0 && !handled) {
                this.mEatingTouch = true;
            }
        }
        if (action == 1 || action == 3) {
            this.mEatingTouch = false;
        }
        return true;
    }

    protected void onSetLayoutParams(View child, android.view.ViewGroup.LayoutParams lp) {
        if (!checkLayoutParams(lp)) {
            child.setLayoutParams(generateLayoutParams(lp));
        }
    }

    private void measureChildConstrained(View child, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int heightConstraint) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childWidthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, (((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin) + widthUsed, lp.width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, (((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height);
        int childHeightMode = MeasureSpec.getMode(childHeightSpec);
        if (childHeightMode != EditorInfo.IME_FLAG_NO_ENTER_ACTION && heightConstraint >= 0) {
            int size;
            if (childHeightMode != 0) {
                size = Math.min(MeasureSpec.getSize(childHeightSpec), heightConstraint);
            } else {
                size = heightConstraint;
            }
            childHeightSpec = MeasureSpec.makeMeasureSpec(size, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private int measureChildCollapseMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed, int[] collapsingMargins) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int leftDiff = lp.leftMargin - collapsingMargins[0];
        int rightDiff = lp.rightMargin - collapsingMargins[1];
        int hMargins = Math.max(0, leftDiff) + Math.max(0, rightDiff);
        collapsingMargins[0] = Math.max(0, -leftDiff);
        collapsingMargins[1] = Math.max(0, -rightDiff);
        child.measure(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, ((this.mPaddingLeft + this.mPaddingRight) + hMargins) + widthUsed, lp.width), ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, (((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height));
        return child.getMeasuredWidth() + hMargins;
    }

    private boolean shouldCollapse() {
        if (!this.mCollapsible) {
            return false;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (shouldLayout(child) && child.getMeasuredWidth() > 0 && child.getMeasuredHeight() > 0) {
                return false;
            }
        }
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int marginStartIndex;
        int marginEndIndex;
        int i;
        int height = 0;
        int childState = 0;
        int[] collapsingMargins = this.mTempMargins;
        if (isLayoutRtl()) {
            marginStartIndex = 1;
            marginEndIndex = 0;
        } else {
            marginStartIndex = 0;
            marginEndIndex = 1;
        }
        int navWidth = 0;
        if (shouldLayout(this.mNavButtonView)) {
            measureChildConstrained(this.mNavButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            navWidth = this.mNavButtonView.getMeasuredWidth() + getHorizontalMargins(this.mNavButtonView);
            i = 0;
            height = Math.max(i, this.mNavButtonView.getMeasuredHeight() + getVerticalMargins(this.mNavButtonView));
            childState = View.combineMeasuredStates(0, this.mNavButtonView.getMeasuredState());
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            measureChildConstrained(this.mCollapseButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            navWidth = this.mCollapseButtonView.getMeasuredWidth() + getHorizontalMargins(this.mCollapseButtonView);
            i = height;
            height = Math.max(i, this.mCollapseButtonView.getMeasuredHeight() + getVerticalMargins(this.mCollapseButtonView));
            childState = View.combineMeasuredStates(childState, this.mCollapseButtonView.getMeasuredState());
        }
        int contentInsetStart = getCurrentContentInsetStart();
        int width = Math.max(contentInsetStart, navWidth) + 0;
        collapsingMargins[marginStartIndex] = Math.max(0, contentInsetStart - navWidth);
        int menuWidth = 0;
        if (shouldLayout(this.mMenuView)) {
            measureChildConstrained(this.mMenuView, widthMeasureSpec, width, heightMeasureSpec, 0, this.mMaxButtonHeight);
            menuWidth = this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
            i = height;
            height = Math.max(i, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
            childState = View.combineMeasuredStates(childState, this.mMenuView.getMeasuredState());
        }
        int contentInsetEnd = getCurrentContentInsetEnd();
        width += Math.max(contentInsetEnd, menuWidth);
        collapsingMargins[marginEndIndex] = Math.max(0, contentInsetEnd - menuWidth);
        if (shouldLayout(this.mExpandedActionView)) {
            width += measureChildCollapseMargins(this.mExpandedActionView, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
            i = height;
            height = Math.max(i, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
            childState = View.combineMeasuredStates(childState, this.mExpandedActionView.getMeasuredState());
        }
        if (shouldLayout(this.mLogoView)) {
            width += measureChildCollapseMargins(this.mLogoView, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
            i = height;
            height = Math.max(i, this.mLogoView.getMeasuredHeight() + getVerticalMargins(this.mLogoView));
            childState = View.combineMeasuredStates(childState, this.mLogoView.getMeasuredState());
        }
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View child = getChildAt(i2);
            if (((LayoutParams) child.getLayoutParams()).mViewType == 0 && shouldLayout(child)) {
                width += measureChildCollapseMargins(child, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
                height = Math.max(height, child.getMeasuredHeight() + getVerticalMargins(child));
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
            }
        }
        int titleWidth = 0;
        int titleHeight = 0;
        int titleVertMargins = this.mTitleMarginTop + this.mTitleMarginBottom;
        int titleHorizMargins = this.mTitleMarginStart + this.mTitleMarginEnd;
        if (shouldLayout(this.mTitleTextView)) {
            titleWidth = measureChildCollapseMargins(this.mTitleTextView, widthMeasureSpec, width + titleHorizMargins, heightMeasureSpec, titleVertMargins, collapsingMargins);
            titleWidth = this.mTitleTextView.getMeasuredWidth() + getHorizontalMargins(this.mTitleTextView);
            titleHeight = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
            childState = View.combineMeasuredStates(childState, this.mTitleTextView.getMeasuredState());
        }
        if (shouldLayout(this.mSubtitleTextView)) {
            i = titleWidth;
            titleWidth = Math.max(i, measureChildCollapseMargins(this.mSubtitleTextView, widthMeasureSpec, width + titleHorizMargins, heightMeasureSpec, titleHeight + titleVertMargins, collapsingMargins));
            titleHeight += this.mSubtitleTextView.getMeasuredHeight() + getVerticalMargins(this.mSubtitleTextView);
            childState = View.combineMeasuredStates(childState, this.mSubtitleTextView.getMeasuredState());
        }
        width += titleWidth;
        height = Math.max(height, titleHeight) + (getPaddingTop() + getPaddingBottom());
        i = widthMeasureSpec;
        int measuredWidth = View.resolveSizeAndState(Math.max(width + (getPaddingLeft() + getPaddingRight()), getSuggestedMinimumWidth()), i, View.MEASURED_STATE_MASK & childState);
        i = heightMeasureSpec;
        int measuredHeight = View.resolveSizeAndState(Math.max(height, getSuggestedMinimumHeight()), i, childState << 16);
        if (shouldCollapse()) {
            measuredHeight = 0;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        boolean isRtl = getLayoutDirection() == 1;
        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int left = paddingLeft;
        int right = width - paddingRight;
        int[] collapsingMargins = this.mTempMargins;
        collapsingMargins[1] = 0;
        collapsingMargins[0] = 0;
        int alignmentHeight = getMinimumHeight();
        if (shouldLayout(this.mNavButtonView)) {
            if (isRtl) {
                right = layoutChildRight(this.mNavButtonView, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mNavButtonView, paddingLeft, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            if (isRtl) {
                right = layoutChildRight(this.mCollapseButtonView, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mCollapseButtonView, left, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mMenuView)) {
            if (isRtl) {
                left = layoutChildLeft(this.mMenuView, left, collapsingMargins, alignmentHeight);
            } else {
                right = layoutChildRight(this.mMenuView, right, collapsingMargins, alignmentHeight);
            }
        }
        int contentInsetLeft = getCurrentContentInsetLeft();
        int contentInsetRight = getCurrentContentInsetRight();
        collapsingMargins[0] = Math.max(0, contentInsetLeft - left);
        collapsingMargins[1] = Math.max(0, contentInsetRight - ((width - paddingRight) - right));
        left = Math.max(left, contentInsetLeft);
        right = Math.min(right, (width - paddingRight) - contentInsetRight);
        if (shouldLayout(this.mExpandedActionView)) {
            if (isRtl) {
                right = layoutChildRight(this.mExpandedActionView, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mExpandedActionView, left, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mLogoView)) {
            if (isRtl) {
                right = layoutChildRight(this.mLogoView, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mLogoView, left, collapsingMargins, alignmentHeight);
            }
        }
        boolean layoutTitle = shouldLayout(this.mTitleTextView);
        boolean layoutSubtitle = shouldLayout(this.mSubtitleTextView);
        int titleHeight = 0;
        if (layoutTitle) {
            LayoutParams lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
            titleHeight = ((lp.topMargin + this.mTitleTextView.getMeasuredHeight()) + lp.bottomMargin) + 0;
        }
        if (layoutSubtitle) {
            lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
            titleHeight += (lp.topMargin + this.mSubtitleTextView.getMeasuredHeight()) + lp.bottomMargin;
        }
        if (layoutTitle || layoutSubtitle) {
            boolean titleHasWidth;
            int titleTop;
            int spaceAbove;
            int spaceBelow;
            int rd;
            int titleRight;
            int subtitleRight;
            int titleLeft;
            int titleBottom;
            int subtitleBottom;
            int ld;
            int subtitleLeft;
            LayoutParams toplp = (LayoutParams) (layoutTitle ? this.mTitleTextView : this.mSubtitleTextView).getLayoutParams();
            LayoutParams bottomlp = (LayoutParams) (layoutSubtitle ? this.mSubtitleTextView : this.mTitleTextView).getLayoutParams();
            if (layoutTitle) {
                if (this.mTitleTextView.getMeasuredWidth() > 0) {
                    titleHasWidth = true;
                    switch (this.mGravity & LogPower.APP_PROCESS_EXIT) {
                        case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                            titleTop = (getPaddingTop() + toplp.topMargin) + this.mTitleMarginTop;
                            break;
                        case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                            titleTop = (((height - paddingBottom) - bottomlp.bottomMargin) - this.mTitleMarginBottom) - titleHeight;
                            break;
                        default:
                            spaceAbove = (((height - paddingTop) - paddingBottom) - titleHeight) / 2;
                            if (spaceAbove < toplp.topMargin + this.mTitleMarginTop) {
                                spaceAbove = toplp.topMargin + this.mTitleMarginTop;
                            } else {
                                spaceBelow = (((height - paddingBottom) - titleHeight) - spaceAbove) - paddingTop;
                                if (spaceBelow < toplp.bottomMargin + this.mTitleMarginBottom) {
                                    spaceAbove = Math.max(0, spaceAbove - ((bottomlp.bottomMargin + this.mTitleMarginBottom) - spaceBelow));
                                }
                            }
                            titleTop = paddingTop + spaceAbove;
                            break;
                    }
                    if (isRtl) {
                        rd = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[1];
                        right -= Math.max(0, rd);
                        collapsingMargins[1] = Math.max(0, -rd);
                        titleRight = right;
                        subtitleRight = right;
                        if (layoutTitle) {
                            lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                            titleLeft = right - this.mTitleTextView.getMeasuredWidth();
                            titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                            this.mTitleTextView.layout(titleLeft, titleTop, right, titleBottom);
                            titleRight = titleLeft - this.mTitleMarginEnd;
                            titleTop = titleBottom + lp.bottomMargin;
                        }
                        if (layoutSubtitle) {
                            lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                            titleTop += lp.topMargin;
                            subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                            this.mSubtitleTextView.layout(right - this.mSubtitleTextView.getMeasuredWidth(), titleTop, right, subtitleBottom);
                            subtitleRight = right - this.mTitleMarginEnd;
                            titleTop = subtitleBottom + lp.bottomMargin;
                        }
                        if (titleHasWidth) {
                            right = Math.min(titleRight, subtitleRight);
                        }
                    } else {
                        ld = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[0];
                        left += Math.max(0, ld);
                        collapsingMargins[0] = Math.max(0, -ld);
                        titleLeft = left;
                        subtitleLeft = left;
                        if (layoutTitle) {
                            lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                            titleRight = left + this.mTitleTextView.getMeasuredWidth();
                            titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                            this.mTitleTextView.layout(left, titleTop, titleRight, titleBottom);
                            titleLeft = titleRight + this.mTitleMarginEnd;
                            titleTop = titleBottom + lp.bottomMargin;
                        }
                        if (layoutSubtitle) {
                            lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                            titleTop += lp.topMargin;
                            subtitleRight = left + this.mSubtitleTextView.getMeasuredWidth();
                            subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                            this.mSubtitleTextView.layout(left, titleTop, subtitleRight, subtitleBottom);
                            subtitleLeft = subtitleRight + this.mTitleMarginEnd;
                            titleTop = subtitleBottom + lp.bottomMargin;
                        }
                        if (titleHasWidth) {
                            left = Math.max(titleLeft, subtitleLeft);
                        }
                    }
                }
            }
            if (layoutSubtitle) {
                if (this.mSubtitleTextView.getMeasuredWidth() > 0) {
                    titleHasWidth = true;
                    switch (this.mGravity & LogPower.APP_PROCESS_EXIT) {
                        case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                            titleTop = (getPaddingTop() + toplp.topMargin) + this.mTitleMarginTop;
                            break;
                        case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                            titleTop = (((height - paddingBottom) - bottomlp.bottomMargin) - this.mTitleMarginBottom) - titleHeight;
                            break;
                        default:
                            spaceAbove = (((height - paddingTop) - paddingBottom) - titleHeight) / 2;
                            if (spaceAbove < toplp.topMargin + this.mTitleMarginTop) {
                                spaceBelow = (((height - paddingBottom) - titleHeight) - spaceAbove) - paddingTop;
                                if (spaceBelow < toplp.bottomMargin + this.mTitleMarginBottom) {
                                    spaceAbove = Math.max(0, spaceAbove - ((bottomlp.bottomMargin + this.mTitleMarginBottom) - spaceBelow));
                                }
                            } else {
                                spaceAbove = toplp.topMargin + this.mTitleMarginTop;
                            }
                            titleTop = paddingTop + spaceAbove;
                            break;
                    }
                    if (isRtl) {
                        if (titleHasWidth) {
                        }
                        ld = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[0];
                        left += Math.max(0, ld);
                        collapsingMargins[0] = Math.max(0, -ld);
                        titleLeft = left;
                        subtitleLeft = left;
                        if (layoutTitle) {
                            lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                            titleRight = left + this.mTitleTextView.getMeasuredWidth();
                            titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                            this.mTitleTextView.layout(left, titleTop, titleRight, titleBottom);
                            titleLeft = titleRight + this.mTitleMarginEnd;
                            titleTop = titleBottom + lp.bottomMargin;
                        }
                        if (layoutSubtitle) {
                            lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                            titleTop += lp.topMargin;
                            subtitleRight = left + this.mSubtitleTextView.getMeasuredWidth();
                            subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                            this.mSubtitleTextView.layout(left, titleTop, subtitleRight, subtitleBottom);
                            subtitleLeft = subtitleRight + this.mTitleMarginEnd;
                            titleTop = subtitleBottom + lp.bottomMargin;
                        }
                        if (titleHasWidth) {
                            left = Math.max(titleLeft, subtitleLeft);
                        }
                    } else {
                        if (titleHasWidth) {
                        }
                        rd = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[1];
                        right -= Math.max(0, rd);
                        collapsingMargins[1] = Math.max(0, -rd);
                        titleRight = right;
                        subtitleRight = right;
                        if (layoutTitle) {
                            lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                            titleLeft = right - this.mTitleTextView.getMeasuredWidth();
                            titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                            this.mTitleTextView.layout(titleLeft, titleTop, right, titleBottom);
                            titleRight = titleLeft - this.mTitleMarginEnd;
                            titleTop = titleBottom + lp.bottomMargin;
                        }
                        if (layoutSubtitle) {
                            lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                            titleTop += lp.topMargin;
                            subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                            this.mSubtitleTextView.layout(right - this.mSubtitleTextView.getMeasuredWidth(), titleTop, right, subtitleBottom);
                            subtitleRight = right - this.mTitleMarginEnd;
                            titleTop = subtitleBottom + lp.bottomMargin;
                        }
                        if (titleHasWidth) {
                            right = Math.min(titleRight, subtitleRight);
                        }
                    }
                }
            }
            titleHasWidth = false;
            switch (this.mGravity & LogPower.APP_PROCESS_EXIT) {
                case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                    titleTop = (getPaddingTop() + toplp.topMargin) + this.mTitleMarginTop;
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                    titleTop = (((height - paddingBottom) - bottomlp.bottomMargin) - this.mTitleMarginBottom) - titleHeight;
                    break;
                default:
                    spaceAbove = (((height - paddingTop) - paddingBottom) - titleHeight) / 2;
                    if (spaceAbove < toplp.topMargin + this.mTitleMarginTop) {
                        spaceAbove = toplp.topMargin + this.mTitleMarginTop;
                    } else {
                        spaceBelow = (((height - paddingBottom) - titleHeight) - spaceAbove) - paddingTop;
                        if (spaceBelow < toplp.bottomMargin + this.mTitleMarginBottom) {
                            spaceAbove = Math.max(0, spaceAbove - ((bottomlp.bottomMargin + this.mTitleMarginBottom) - spaceBelow));
                        }
                    }
                    titleTop = paddingTop + spaceAbove;
                    break;
            }
            if (isRtl) {
                if (titleHasWidth) {
                }
                rd = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[1];
                right -= Math.max(0, rd);
                collapsingMargins[1] = Math.max(0, -rd);
                titleRight = right;
                subtitleRight = right;
                if (layoutTitle) {
                    lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    titleLeft = right - this.mTitleTextView.getMeasuredWidth();
                    titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                    this.mTitleTextView.layout(titleLeft, titleTop, right, titleBottom);
                    titleRight = titleLeft - this.mTitleMarginEnd;
                    titleTop = titleBottom + lp.bottomMargin;
                }
                if (layoutSubtitle) {
                    lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    titleTop += lp.topMargin;
                    subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                    this.mSubtitleTextView.layout(right - this.mSubtitleTextView.getMeasuredWidth(), titleTop, right, subtitleBottom);
                    subtitleRight = right - this.mTitleMarginEnd;
                    titleTop = subtitleBottom + lp.bottomMargin;
                }
                if (titleHasWidth) {
                    right = Math.min(titleRight, subtitleRight);
                }
            } else {
                if (titleHasWidth) {
                }
                ld = (titleHasWidth ? this.mTitleMarginStart : 0) - collapsingMargins[0];
                left += Math.max(0, ld);
                collapsingMargins[0] = Math.max(0, -ld);
                titleLeft = left;
                subtitleLeft = left;
                if (layoutTitle) {
                    lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    titleRight = left + this.mTitleTextView.getMeasuredWidth();
                    titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                    this.mTitleTextView.layout(left, titleTop, titleRight, titleBottom);
                    titleLeft = titleRight + this.mTitleMarginEnd;
                    titleTop = titleBottom + lp.bottomMargin;
                }
                if (layoutSubtitle) {
                    lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    titleTop += lp.topMargin;
                    subtitleRight = left + this.mSubtitleTextView.getMeasuredWidth();
                    subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                    this.mSubtitleTextView.layout(left, titleTop, subtitleRight, subtitleBottom);
                    subtitleLeft = subtitleRight + this.mTitleMarginEnd;
                    titleTop = subtitleBottom + lp.bottomMargin;
                }
                if (titleHasWidth) {
                    left = Math.max(titleLeft, subtitleLeft);
                }
            }
        }
        addCustomViewsWithGravity(this.mTempViews, 3);
        int leftViewsCount = this.mTempViews.size();
        for (i = 0; i < leftViewsCount; i++) {
            left = layoutChildLeft((View) this.mTempViews.get(i), left, collapsingMargins, alignmentHeight);
        }
        addCustomViewsWithGravity(this.mTempViews, 5);
        int rightViewsCount = this.mTempViews.size();
        for (i = 0; i < rightViewsCount; i++) {
            right = layoutChildRight((View) this.mTempViews.get(i), right, collapsingMargins, alignmentHeight);
        }
        addCustomViewsWithGravity(this.mTempViews, 1);
        int centerViewsWidth = getViewListMeasuredWidth(this.mTempViews, collapsingMargins);
        int centerLeft = (paddingLeft + (((width - paddingLeft) - paddingRight) / 2)) - (centerViewsWidth / 2);
        int centerRight = centerLeft + centerViewsWidth;
        if (centerLeft < left) {
            centerLeft = left;
        } else if (centerRight > right) {
            centerLeft -= centerRight - right;
        }
        int centerViewsCount = this.mTempViews.size();
        for (i = 0; i < centerViewsCount; i++) {
            centerLeft = layoutChildLeft((View) this.mTempViews.get(i), centerLeft, collapsingMargins, alignmentHeight);
        }
        this.mTempViews.clear();
    }

    private int getViewListMeasuredWidth(List<View> views, int[] collapsingMargins) {
        int collapseLeft = collapsingMargins[0];
        int collapseRight = collapsingMargins[1];
        int width = 0;
        int count = views.size();
        for (int i = 0; i < count; i++) {
            View v = (View) views.get(i);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            int l = lp.leftMargin - collapseLeft;
            int r = lp.rightMargin - collapseRight;
            int leftMargin = Math.max(0, l);
            int rightMargin = Math.max(0, r);
            collapseLeft = Math.max(0, -l);
            collapseRight = Math.max(0, -r);
            width += (v.getMeasuredWidth() + leftMargin) + rightMargin;
        }
        return width;
    }

    private int layoutChildLeft(View child, int left, int[] collapsingMargins, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int l = lp.leftMargin - collapsingMargins[0];
        left += Math.max(0, l);
        collapsingMargins[0] = Math.max(0, -l);
        int top = getChildTop(child, alignmentHeight);
        int childWidth = child.getMeasuredWidth();
        child.layout(left, top, left + childWidth, child.getMeasuredHeight() + top);
        return left + (lp.rightMargin + childWidth);
    }

    private int layoutChildRight(View child, int right, int[] collapsingMargins, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int r = lp.rightMargin - collapsingMargins[1];
        right -= Math.max(0, r);
        collapsingMargins[1] = Math.max(0, -r);
        int top = getChildTop(child, alignmentHeight);
        int childWidth = child.getMeasuredWidth();
        child.layout(right - childWidth, top, right, child.getMeasuredHeight() + top);
        return right - (lp.leftMargin + childWidth);
    }

    private int getChildTop(View child, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childHeight = child.getMeasuredHeight();
        int alignmentOffset = alignmentHeight > 0 ? (childHeight - alignmentHeight) / 2 : 0;
        switch (getChildVerticalGravity(lp.gravity)) {
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                return getPaddingTop() - alignmentOffset;
            case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                return (((getHeight() - getPaddingBottom()) - childHeight) - lp.bottomMargin) - alignmentOffset;
            default:
                int paddingTop = getPaddingTop();
                int paddingBottom = getPaddingBottom();
                int height = getHeight();
                int spaceAbove = (((height - paddingTop) - paddingBottom) - childHeight) / 2;
                if (spaceAbove < lp.topMargin) {
                    spaceAbove = lp.topMargin;
                } else {
                    int spaceBelow = (((height - paddingBottom) - childHeight) - spaceAbove) - paddingTop;
                    if (spaceBelow < lp.bottomMargin) {
                        spaceAbove = Math.max(0, spaceAbove - (lp.bottomMargin - spaceBelow));
                    }
                }
                return paddingTop + spaceAbove;
        }
    }

    private int getChildVerticalGravity(int gravity) {
        int vgrav = gravity & LogPower.APP_PROCESS_EXIT;
        switch (vgrav) {
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
            case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                return vgrav;
            default:
                return this.mGravity & LogPower.APP_PROCESS_EXIT;
        }
    }

    private void addCustomViewsWithGravity(List<View> views, int gravity) {
        boolean isRtl = getLayoutDirection() == 1;
        int childCount = getChildCount();
        int absGrav = Gravity.getAbsoluteGravity(gravity, getLayoutDirection());
        views.clear();
        int i;
        View child;
        LayoutParams lp;
        if (isRtl) {
            for (i = childCount - 1; i >= 0; i--) {
                child = getChildAt(i);
                lp = (LayoutParams) child.getLayoutParams();
                if (lp.mViewType == 0 && shouldLayout(child) && getChildHorizontalGravity(lp.gravity) == absGrav) {
                    views.add(child);
                }
            }
            return;
        }
        for (i = 0; i < childCount; i++) {
            child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            if (lp.mViewType == 0 && shouldLayout(child) && getChildHorizontalGravity(lp.gravity) == absGrav) {
                views.add(child);
            }
        }
    }

    private int getChildHorizontalGravity(int gravity) {
        int ld = getLayoutDirection();
        int hGrav = Gravity.getAbsoluteGravity(gravity, ld) & 7;
        switch (hGrav) {
            case HwCfgFilePolicy.EMUI /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                return hGrav;
            default:
                return ld == 1 ? 5 : 3;
        }
    }

    private boolean shouldLayout(View view) {
        return (view == null || view.getParent() != this || view.getVisibility() == 8) ? false : true;
    }

    private int getHorizontalMargins(View v) {
        MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
        return mlp.getMarginStart() + mlp.getMarginEnd();
    }

    private int getVerticalMargins(View v) {
        MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
        return mlp.topMargin + mlp.bottomMargin;
    }

    public /* bridge */ /* synthetic */ android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateLayoutParams(attrs);
    }

    public LayoutParams m24generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected /* bridge */ /* synthetic */ android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return generateLayoutParams(p);
    }

    protected LayoutParams m25generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        if (p instanceof android.app.ActionBar.LayoutParams) {
            return new LayoutParams((android.app.ActionBar.LayoutParams) p);
        }
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    protected /* bridge */ /* synthetic */ android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return generateDefaultLayoutParams();
    }

    protected LayoutParams m23generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) ? p instanceof LayoutParams : false;
    }

    private static boolean isCustomView(View child) {
        return ((LayoutParams) child.getLayoutParams()).mViewType == 0;
    }

    public DecorToolbar getWrapper() {
        if (this.mWrapper == null) {
            this.mWrapper = new ToolbarWidgetWrapper(this, true);
        }
        return this.mWrapper;
    }

    void removeChildrenForExpandedActionView() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (!(((LayoutParams) child.getLayoutParams()).mViewType == 2 || child == this.mMenuView)) {
                removeViewAt(i);
                this.mHiddenViews.add(child);
            }
        }
    }

    void addChildrenForExpandedActionView() {
        for (int i = this.mHiddenViews.size() - 1; i >= 0; i--) {
            addView((View) this.mHiddenViews.get(i));
        }
        this.mHiddenViews.clear();
    }

    private boolean isChildOrHidden(View child) {
        return child.getParent() != this ? this.mHiddenViews.contains(child) : true;
    }

    public void setCollapsible(boolean collapsible) {
        this.mCollapsible = collapsible;
        requestLayout();
    }

    public void setMenuCallbacks(Callback pcb, MenuBuilder.Callback mcb) {
        this.mActionMenuPresenterCallback = pcb;
        this.mMenuBuilderCallback = mcb;
        if (this.mMenuView != null) {
            this.mMenuView.setMenuCallbacks(pcb, mcb);
        }
    }

    ActionMenuPresenter getOuterActionMenuPresenter() {
        return this.mOuterActionMenuPresenter;
    }

    Context getPopupContext() {
        return this.mPopupContext;
    }
}
