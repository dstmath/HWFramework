package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
    private boolean mActionModeEnabled;
    private View mBarcodeButton;
    private boolean mBarcodeEnabled;
    /* access modifiers changed from: private */
    public HwSearchAutoComplete mHwSearchSrcTextView;
    /* access modifiers changed from: private */
    public ImageView mHwVoiceButton;
    private boolean mInActionMode;
    private ResLoader mResLoader;
    /* access modifiers changed from: private */
    public int mSearchviewTextMarginEnd;
    /* access modifiers changed from: private */
    public int mSearchviewTextPadding;

    public static class HwSearchAutoComplete extends SearchView.SearchAutoComplete {
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
            if (this.mSearchView == null || !this.mSearchView.isInActionMode() || !this.mSearchView.isSubmitButtonEnabled()) {
                return SearchView.super.enoughToFilter();
            }
            return false;
        }
    }

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
        this.mResLoader = ResLoader.getInstance();
        Resources res = this.mResLoader.getResources(context);
        this.mSearchviewTextMarginEnd = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "searchview_src_text_padding_end"));
        this.mSearchviewTextPadding = res.getDimensionPixelSize(34472219);
        reflectMember();
        updateSearchTextViewMargin();
        initialBarcodeButton(context);
        View search_src_text = findViewById(16909293);
        if (search_src_text != null) {
            search_src_text.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr));
        }
        ImageView search_close_btn = (ImageView) findViewById(16909288);
        if (search_close_btn != null) {
            search_close_btn.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr));
        }
        ImageView search_voice_btn = (ImageView) findViewById(16909295);
        if (search_voice_btn != null) {
            search_voice_btn.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr));
        }
        View go_btn = findViewById(34603203);
        if (go_btn != null) {
            go_btn.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
        }
        View voice_button = findViewById(34603201);
        if (voice_button != null) {
            voice_button.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
        }
    }

    private void initialBarcodeButton(Context context) {
        this.mBarcodeButton = findViewById(34603107);
        if (this.mBarcodeButton != null) {
            this.mBarcodeButton.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
        }
    }

    public boolean isEmuiStyle() {
        return true;
    }

    public boolean isInActionMode() {
        return this.mInActionMode;
    }

    public void onInActionMode() {
        if (!this.mInActionMode) {
            this.mInActionMode = true;
            this.mActionModeEnabled = true;
            setSubmitButtonEnabled(true);
        }
    }

    public void setActionModeEnabled(boolean enabled) {
        if (this.mHwSearchSrcTextView == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        this.mActionModeEnabled = enabled;
        this.mHwSearchSrcTextView.setClickable(!enabled);
        this.mHwSearchSrcTextView.setFocusable(!enabled);
        this.mHwSearchSrcTextView.setShowSoftInputOnFocus(!enabled);
        setClickable(!enabled);
        updateViewsVisibility(isIconified());
    }

    public boolean isActionModeEnabled() {
        return this.mActionModeEnabled;
    }

    public void setQrcodeEnabled(boolean enabled) {
        this.mBarcodeEnabled = enabled;
        updateViewsVisibility(isIconified());
    }

    private boolean canShowSubmitButton() {
        return false;
    }

    private boolean isSubmitAreaEnabled() {
        return isSubmitButtonEnabled() && !isIconified();
    }

    public void updateQrcodeButton(boolean collapsed) {
        if (this.mBarcodeButton != null) {
            this.mBarcodeButton.setVisibility((!this.mBarcodeEnabled || isIconfiedByDefault()) ? 8 : 0);
        }
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(33751442);
    }

    public void adjustQueryTextView(boolean hasHint) {
        if (this.mHwSearchSrcTextView == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        int left = this.mHwSearchSrcTextView.getPaddingLeft();
        int top = this.mHwSearchSrcTextView.getPaddingTop();
        int right = this.mHwSearchSrcTextView.getPaddingRight();
        int bottom = this.mHwSearchSrcTextView.getPaddingBottom();
        int gap = (int) (getResources().getDisplayMetrics().density * 3.0f);
        if (hasHint && bottom <= 0) {
            bottom += gap;
        } else if (!hasHint && bottom > 0) {
            bottom -= gap;
        }
        this.mHwSearchSrcTextView.setPadding(left, top, right, bottom);
    }

    public void updateViewsVisibility(boolean collapsed) {
        ReflectUtil.callMethod(this, "updateViewsVisibility", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(collapsed)}, android.widget.SearchView.class);
        updateQrcodeButton(collapsed);
    }

    public void setQuery(CharSequence query, boolean submit) {
        if (this.mHwSearchSrcTextView == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null");
            return;
        }
        this.mHwSearchSrcTextView.setText(query);
        if (query != null) {
            this.mHwSearchSrcTextView.setSelection(this.mHwSearchSrcTextView.length());
            ReflectUtil.setObject("mUserQuery", this, query, android.widget.SearchView.class);
        }
        if (!this.mInActionMode && submit && !TextUtils.isEmpty(query)) {
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
        if ("iw".equals(Locale.getDefault().getLanguage()) && this.mHwSearchSrcTextView != null) {
            this.mHwSearchSrcTextView.addTextChangedListener(new TextWatcher() {
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

    /* JADX WARNING: type inference failed for: r0v0, types: [huawei.android.widget.SearchView$HwSearchAutoComplete, android.widget.EditText] */
    public EditText getSearchSrcTextView() {
        return this.mHwSearchSrcTextView;
    }
}
