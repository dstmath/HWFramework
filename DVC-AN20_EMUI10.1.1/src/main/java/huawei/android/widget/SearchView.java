package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class SearchView extends android.widget.SearchView {
    public static final int QUERY_TEXT_VIEW_GAP = 3;
    private static final String TAG = "HwSearchView";
    private Drawable mBackGroundDrawable;
    private View mBarcodeButton;
    private HwSearchAutoComplete mHwSearchSrcTextView;
    private ImageView mHwVoiceButton;
    private boolean mIsActionModeEnabled;
    private boolean mIsBarcodeEnabled;
    private boolean mIsInActionMode;
    private final View.OnClickListener mOnClickListener;
    private ResLoader mResLoader;
    private int mSearchViewCornerRadius;
    private int mSearchviewTextMarginEnd;
    private int mSearchviewTextPadding;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843904);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOnClickListener = new View.OnClickListener() {
            /* class huawei.android.widget.SearchView.AnonymousClass1 */

            public void onClick(View v) {
                if (v == SearchView.this.mBarcodeButton) {
                    SearchView.this.onBarcodeClicked();
                }
            }
        };
        this.mResLoader = ResLoader.getInstance();
        Resources res = this.mResLoader.getResources(context);
        this.mSearchviewTextMarginEnd = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "searchview_src_text_padding_end"));
        this.mSearchviewTextPadding = res.getDimensionPixelSize(34472219);
        this.mSearchViewCornerRadius = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "emui_corner_radius_inputbox"));
        reflectMember();
        updateSearchTextViewMargin();
        initialBarcodeButton(context);
        HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr).setCornerRadius((float) this.mSearchViewCornerRadius);
        View searchPlate = findViewById(16909356);
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (!(hwSearchAutoComplete == null || searchPlate == null)) {
            hwSearchAutoComplete.setSearchPlateView(searchPlate);
        }
        View goBtn = findViewById(34603203);
        if (goBtn != null) {
            goBtn.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
        }
    }

    private void initialBarcodeButton(Context context) {
        this.mBarcodeButton = findViewById(34603107);
        View view = this.mBarcodeButton;
        if (view != null) {
            view.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
            this.mBarcodeButton.setOnClickListener(this.mOnClickListener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBarcodeClicked() {
    }

    public boolean isEmuiStyle() {
        return true;
    }

    public boolean isInActionMode() {
        return this.mIsInActionMode;
    }

    public void onInActionMode() {
        if (!this.mIsInActionMode) {
            this.mIsInActionMode = true;
            this.mIsActionModeEnabled = true;
            setSubmitButtonEnabled(true);
        }
    }

    public boolean isActionModeEnabled() {
        return this.mIsActionModeEnabled;
    }

    public void setActionModeEnabled(boolean isEnabled) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        this.mIsActionModeEnabled = isEnabled;
        hwSearchAutoComplete.setClickable(!isEnabled);
        this.mHwSearchSrcTextView.setFocusable(!isEnabled);
        this.mHwSearchSrcTextView.setShowSoftInputOnFocus(!isEnabled);
        setClickable(!isEnabled);
        updateViewsVisibility(isIconified());
    }

    public void setQrcodeEnabled(boolean isEnabled) {
        this.mIsBarcodeEnabled = isEnabled;
        updateViewsVisibility(isIconified());
    }

    private boolean canShowSubmitButton() {
        return false;
    }

    private boolean isSubmitAreaEnabled() {
        return isSubmitButtonEnabled() && !isIconified();
    }

    public void updateQrcodeButton(boolean isCollapsed) {
        View view = this.mBarcodeButton;
        if (view != null) {
            view.setVisibility((!this.mIsBarcodeEnabled || isIconfiedByDefault()) ? 8 : 0);
        }
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(33751442);
    }

    public void adjustQueryTextView(boolean isHint) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        int left = hwSearchAutoComplete.getPaddingLeft();
        int top = this.mHwSearchSrcTextView.getPaddingTop();
        int right = this.mHwSearchSrcTextView.getPaddingRight();
        int bottom = this.mHwSearchSrcTextView.getPaddingBottom();
        int gap = (int) (getResources().getDisplayMetrics().density * 3.0f);
        if (isHint && bottom <= 0) {
            bottom += gap;
        } else if (!isHint && bottom > 0) {
            bottom -= gap;
        }
        this.mHwSearchSrcTextView.setPadding(left, top, right, bottom);
    }

    public void updateViewsVisibility(boolean isCollapsed) {
        ReflectUtil.callMethod(this, "updateViewsVisibility", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isCollapsed)}, android.widget.SearchView.class);
        updateQrcodeButton(isCollapsed);
    }

    public void setQuery(CharSequence query, boolean isSubmit) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null");
            return;
        }
        hwSearchAutoComplete.setText(query);
        if (query != null) {
            HwSearchAutoComplete hwSearchAutoComplete2 = this.mHwSearchSrcTextView;
            hwSearchAutoComplete2.setSelection(hwSearchAutoComplete2.length());
            ReflectUtil.setObject("mUserQuery", this, query, android.widget.SearchView.class);
        }
        if (!this.mIsInActionMode && isSubmit && !TextUtils.isEmpty(query)) {
            ReflectUtil.callMethod(this, "onSubmitQuery", null, null, android.widget.SearchView.class);
        }
    }

    private void reflectMember() {
        SearchView.SearchAutoComplete mSearchSrcTextView = (SearchView.SearchAutoComplete) ReflectUtil.getObject(this, "mSearchSrcTextView", android.widget.SearchView.class);
        if (mSearchSrcTextView != null && (mSearchSrcTextView instanceof HwSearchAutoComplete)) {
            this.mHwSearchSrcTextView = (HwSearchAutoComplete) mSearchSrcTextView;
            this.mHwSearchSrcTextView.setSearchView(this);
        }
        this.mHwVoiceButton = (ImageView) ReflectUtil.getObject(this, "mVoiceButton", android.widget.SearchView.class);
    }

    private void updateSearchTextViewMargin() {
        HwSearchAutoComplete hwSearchAutoComplete;
        if ("iw".equals(Locale.getDefault().getLanguage()) && (hwSearchAutoComplete = this.mHwSearchSrcTextView) != null) {
            hwSearchAutoComplete.addTextChangedListener(new TextWatcher() {
                /* class huawei.android.widget.SearchView.AnonymousClass2 */

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    int mHwVoiceButtonVisility = 0;
                    if (SearchView.this.mHwVoiceButton != null) {
                        mHwVoiceButtonVisility = SearchView.this.mHwVoiceButton.getVisibility();
                    }
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) SearchView.this.mHwSearchSrcTextView.getLayoutParams();
                    if (!TextUtils.isEmpty(s) || mHwVoiceButtonVisility != 8) {
                        SearchView.this.mHwSearchSrcTextView.setPadding(0, 0, SearchView.this.mSearchviewTextPadding, 0);
                    } else {
                        SearchView.this.mHwSearchSrcTextView.setPadding(SearchView.this.mSearchviewTextMarginEnd, 0, SearchView.this.mSearchviewTextPadding, 0);
                    }
                    SearchView.this.mHwSearchSrcTextView.setLayoutParams(lp);
                }
            });
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [huawei.android.widget.SearchView$HwSearchAutoComplete, android.widget.EditText] */
    public EditText getSearchSrcTextView() {
        return this.mHwSearchSrcTextView;
    }

    public static class HwSearchAutoComplete extends SearchView.SearchAutoComplete {
        private View mSearchPlateView;
        private SearchView mSearchView;

        public HwSearchAutoComplete(Context context) {
            super(context);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs) {
            super(context, attrs, defStyleAttrs);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
            super(context, attrs, defStyleAttrs, defStyleRes);
        }

        /* access modifiers changed from: package-private */
        public void setSearchView(SearchView searchView) {
            this.mSearchView = searchView;
        }

        public boolean enoughToFilter() {
            SearchView searchView = this.mSearchView;
            if (searchView == null || !searchView.isInActionMode() || !this.mSearchView.isSubmitButtonEnabled()) {
                return SearchView.super.enoughToFilter();
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setSearchPlateView(View searchPlateView) {
            this.mSearchPlateView = searchPlateView;
        }

        /* access modifiers changed from: protected */
        public void drawableStateChanged() {
            Drawable background;
            SearchView.super.drawableStateChanged();
            View view = this.mSearchPlateView;
            if (view != null && (background = view.getBackground()) != null && background.isStateful()) {
                background.setState(getDrawableState());
            }
        }
    }

    public void draw(Canvas canvas) {
        Drawable backGroundDrawable = this.mBackGroundDrawable;
        if (backGroundDrawable != null) {
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
                Drawable backGround = getBackground();
                if (backGround != null) {
                    Rect bounds = backGround.getBounds();
                    backGroundDrawable.setBounds(bounds.left, bounds.top, bounds.right + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, bounds.bottom);
                    canvas.save();
                    canvas.translate((float) (-marginLayoutParams.leftMargin), 0.0f);
                    backGroundDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        }
        super.draw(canvas);
    }

    public void setBackGroundEx(boolean isEnable) {
        ViewParent parent = getParent();
        ViewGroup viewGroup = null;
        if (parent instanceof ViewGroup) {
            viewGroup = (ViewGroup) parent;
        }
        if (isEnable) {
            Drawable backGround = getBackground();
            if (backGround instanceof ColorDrawable) {
                this.mBackGroundDrawable = new ColorDrawable(((ColorDrawable) backGround).getColor());
                if (viewGroup != null) {
                    viewGroup.setClipChildren(false);
                    return;
                }
                return;
            }
            return;
        }
        this.mBackGroundDrawable = null;
        if (viewGroup != null) {
            viewGroup.setClipChildren(true);
        }
    }
}
