package android.inputmethodservice;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;

public class CompactExtractEditLayout extends LinearLayout {
    private View mInputExtractAccessories;
    private View mInputExtractAction;
    private View mInputExtractEditText;
    private boolean mPerformLayoutChanges;

    public CompactExtractEditLayout(Context context) {
        super(context);
    }

    public CompactExtractEditLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompactExtractEditLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInputExtractEditText = findViewById(R.id.inputExtractEditText);
        this.mInputExtractAccessories = findViewById(16909182);
        this.mInputExtractAction = findViewById(16909183);
        if (this.mInputExtractEditText != null && this.mInputExtractAccessories != null && this.mInputExtractAction != null) {
            this.mPerformLayoutChanges = true;
        }
    }

    private int applyFractionInt(int fraction, int whole) {
        return Math.round(getResources().getFraction(fraction, whole, whole));
    }

    private static void setLayoutHeight(View v, int px) {
        LayoutParams lp = v.getLayoutParams();
        lp.height = px;
        v.setLayoutParams(lp);
    }

    private static void setLayoutMarginBottom(View v, int px) {
        MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
        lp.bottomMargin = px;
        v.setLayoutParams(lp);
    }

    private void applyProportionalLayout(int screenWidthPx, int screenHeightPx) {
        if (getResources().getConfiguration().isScreenRound()) {
            setGravity(80);
        }
        setLayoutHeight(this, applyFractionInt(18022407, screenHeightPx));
        setPadding(applyFractionInt(18022408, screenWidthPx), 0, applyFractionInt(18022410, screenWidthPx), 0);
        setLayoutMarginBottom(this.mInputExtractEditText, applyFractionInt(18022411, screenHeightPx));
        setLayoutMarginBottom(this.mInputExtractAccessories, applyFractionInt(18022412, screenHeightPx));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mPerformLayoutChanges) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            applyProportionalLayout(dm.widthPixels, dm.heightPixels);
        }
    }
}
