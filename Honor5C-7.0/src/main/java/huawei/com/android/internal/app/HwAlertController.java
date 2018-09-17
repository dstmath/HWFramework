package huawei.com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.app.AlertController;

public class HwAlertController extends AlertController {
    static final int GAP = 12;
    private static final int GAP_16 = 16;
    private Context mContext;
    private float mDensity;
    private boolean mHasThreeButtons;
    private boolean mIsMessageScrolling;
    private Resources mResources;

    private static class CustOnPreDrawListener implements OnPreDrawListener {
        private TextView messageView;

        private CustOnPreDrawListener(TextView messageView) {
            this.messageView = messageView;
        }

        public boolean onPreDraw() {
            if (this.messageView.getLineCount() == 1) {
                this.messageView.setGravity(17);
            }
            return true;
        }
    }

    public HwAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
        this.mIsMessageScrolling = true;
        this.mContext = context;
        this.mResources = this.mContext.getResources();
    }

    protected void setupView() {
        boolean z = false;
        super.setupView();
        if (!this.mIsMessageScrolling) {
            removeScrollView(getScrollView());
        }
        View buttonPanel = getWindow().findViewById(16909086);
        if (!(TextUtils.isEmpty(getButtonPositiveText()) || TextUtils.isEmpty(getButtonNeutralText()) || TextUtils.isEmpty(getButtonNegativeText()))) {
            z = true;
        }
        this.mHasThreeButtons = z;
        needChangeButtonLayout(buttonPanel);
        TextView messageView = (TextView) getWindow().findViewById(16908299);
        View customPanel = getWindow().findViewById(16909091);
        ProgressBar progress = (ProgressBar) getWindow().findViewById(16908301);
        if (buttonPanel != null && messageView != null && progress == null) {
            View topPanel = getWindow().findViewById(16909082);
            View contentPanel = getWindow().findViewById(16909089);
            if (topPanel == null || contentPanel == null || buttonPanel.getVisibility() != 0 || topPanel.getVisibility() != 8 || messageView.getVisibility() != 0) {
                return;
            }
            if (customPanel == null || customPanel.getVisibility() == 8) {
                messageView.getViewTreeObserver().addOnPreDrawListener(new CustOnPreDrawListener(null));
            }
        }
    }

    protected void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, View buttonPanel, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        super.setBackground(a, topPanel, contentPanel, customPanel, buttonPanel, hasTitle, hasCustomView, hasButtons);
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        if (hasTextTitle()) {
            View titleDivider = getWindow().findViewById(34603085);
            if (titleDivider != null) {
                titleDivider.setVisibility(0);
            }
        } else if (buttonPanel.getVisibility() == 0) {
            buttonPanel.setPadding(buttonPanel.getPaddingStart(), (int) (this.mDensity * 24.0f), buttonPanel.getPaddingEnd(), buttonPanel.getPaddingBottom());
        }
        View hwGap1 = getWindow().findViewById(34603086);
        View hwGap2 = getWindow().findViewById(34603087);
        if (hwGap1 != null && contentPanel.getVisibility() == 0 && getListView() == null) {
            if (hasTitle) {
                hwGap1.setLayoutParams(new LayoutParams(-1, (int) (this.mDensity * 16.0f)));
                hwGap1.setVisibility(0);
            } else {
                hwGap1.setVisibility(0);
            }
        }
        if (hwGap2 != null && contentPanel.getVisibility() == 0 && !hasCustomView && getListView() == null) {
            if (hasTitle && !hasButtons) {
                hwGap2.setLayoutParams(new LayoutParams(-1, (int) (this.mDensity * 16.0f)));
                hwGap2.setVisibility(0);
            } else if (!hasTitle && !hasButtons) {
                hwGap2.setVisibility(0);
            }
        }
    }

    private void needChangeButtonLayout(View buttonPanel) {
        if (this.mHasThreeButtons) {
            LinearLayout buttonStyle = (LinearLayout) ((LinearLayout) buttonPanel).getChildAt(0);
            buttonStyle.setOrientation(1);
            View childfirst = buttonStyle.getChildAt(0);
            View childlast = buttonStyle.getChildAt(2);
            buttonStyle.removeViewAt(2);
            buttonStyle.removeViewAt(0);
            buttonStyle.addView(childlast, 0);
            buttonStyle.addView(childfirst);
            int defaultMargin = (int) this.mResources.getDimension(34471960);
            LayoutParams params = new LayoutParams(-1, -2);
            for (int i = 0; i < buttonStyle.getChildCount(); i++) {
                params.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
                buttonStyle.getChildAt(i).setLayoutParams(params);
            }
        }
    }

    public void setMessageNotScrolling() {
        this.mIsMessageScrolling = false;
    }

    private void removeScrollView(ViewGroup scrollContainer) {
        if (scrollContainer != null) {
            View messageView = scrollContainer.findViewById(16908299);
            if (messageView != null) {
                scrollContainer.removeView(messageView);
                ViewGroup scrollParent = (ViewGroup) scrollContainer.getParent();
                int childIndex = scrollParent.indexOfChild(scrollContainer);
                scrollParent.removeViewAt(childIndex);
                scrollParent.addView(messageView, childIndex, new ViewGroup.LayoutParams(-1, -1));
            }
        }
    }

    protected void setHuaweiScrollIndicators(boolean hasCustomPanel, boolean hasTopPanel, boolean hasButtonPanel) {
    }
}
