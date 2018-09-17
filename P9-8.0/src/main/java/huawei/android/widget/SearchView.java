package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SearchView extends android.widget.SearchView {
    public static final int QUERY_TEXT_VIEW_GAP = 3;
    private static final String TAG = "HwSearchView";
    private boolean mActionModeEnabled;
    private View mBarcodeButton;
    private boolean mBarcodeEnabled;
    private boolean mInActionMode;
    private final OnClickListener mOnClickListener;

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
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v == SearchView.this.mBarcodeButton) {
                    SearchView.this.onBarcodeClicked();
                }
            }
        };
    }

    public void inflateSearchView(Context context, AttributeSet attrs, TypedArray a) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        if (HwWidgetFactory.isHwDarkTheme(context) || HwWidgetFactory.isHwEmphasizeTheme(context)) {
            inflater.inflate(34013257, this, true);
        } else {
            inflater.inflate(34013256, this, true);
        }
        initialBarcodeButton(context, null);
    }

    private void initialBarcodeButton(Context context, AttributeSet attrs) {
        this.mBarcodeButton = findViewById(34603107);
        if (this.mBarcodeButton != null) {
            this.mBarcodeButton.setOnClickListener(this.mOnClickListener);
        }
    }

    private void onBarcodeClicked() {
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
        this.mActionModeEnabled = enabled;
        TextView autotv = getSearchSrcTextView();
        autotv.setClickable(enabled ^ 1);
        autotv.setFocusable(enabled ^ 1);
        autotv.setShowSoftInputOnFocus(enabled ^ 1);
        setClickable(enabled ^ 1);
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
        return isSubmitButtonEnabled() ? isIconified() ^ 1 : false;
    }

    public void updateQrcodeButton(boolean collapsed) {
        if (this.mBarcodeButton != null) {
            View view = this.mBarcodeButton;
            int i = (!this.mBarcodeEnabled || (isIconfiedByDefault() ^ 1) == 0) ? 8 : 0;
            view.setVisibility(i);
        }
    }

    private void updateSubmitButtonHw(boolean hasText) {
        int visibility = 8;
        if (canShowSubmitButton() || isSubmitAreaEnabled()) {
            visibility = 0;
        }
        getGoButton().setVisibility(visibility);
    }

    private void updateSubmitAreaHw() {
        int visibility = 8;
        if ((canShowSubmitButton() || isSubmitAreaEnabled()) && getGoButton().getVisibility() == 0) {
            visibility = 0;
        }
        getSubmitArea().setVisibility(visibility);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(33751442);
    }

    public void adjustQueryTextView(boolean hasHint) {
        int left = getSearchSrcTextView().getPaddingLeft();
        int top = getSearchSrcTextView().getPaddingTop();
        int right = getSearchSrcTextView().getPaddingRight();
        int bottom = getSearchSrcTextView().getPaddingBottom();
        int gap = (int) (getResources().getDisplayMetrics().density * 3.0f);
        if (hasHint && bottom <= 0) {
            bottom += gap;
        } else if (!hasHint && bottom > 0) {
            bottom -= gap;
        }
        getSearchSrcTextView().setPadding(left, top, right, bottom);
    }

    public void updateViewsVisibility(boolean collapsed) {
        super.updateViewsVisibility(collapsed);
        updateQrcodeButton(collapsed);
    }

    protected void updateSubmitButton(boolean hasText) {
        super.updateSubmitButton(hasText);
        getGoButton().getDrawable().setState(hasText ? SELECTED_STATE_SET : EMPTY_STATE_SET);
        updateSubmitButtonHw(hasText);
    }

    protected void updateSubmitArea() {
        super.updateSubmitArea();
        updateSubmitAreaHw();
    }

    protected CharSequence getDecoratedHint(CharSequence hintText) {
        return hintText;
    }

    public void setIconsAndBackgrounds(TypedArray a) {
    }
}
