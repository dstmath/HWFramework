package huawei.com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import java.util.ArrayList;
import java.util.Locale;

public class HwAlertController extends AlertController {
    private static final int BUTTON_COUNTS = 2;
    static final int GAP = 12;
    private static final int GAP_16 = 16;
    private static final String TAG = "HwAlertController";
    private static final int WEIGHT_COUNT = 1;
    private int mButtonCounts;
    private LinearLayout mButtonLayout;
    private ArrayList<Button> mButtonList;
    private Context mContext;
    private float mDensity;
    private final OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            HwAlertController.this.callbackFromLayoutListenner();
        }
    };
    private boolean mHasThreeButtons;
    private boolean mIsMessageScrolling = true;
    private int mOldButtonLayoutWidth;
    private int mOrientation = -1;
    private Resources mResources;

    private static class CustOnPreDrawListener implements OnPreDrawListener {
        private TextView messageView;

        /* synthetic */ CustOnPreDrawListener(TextView messageView, CustOnPreDrawListener -this1) {
            this(messageView);
        }

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
        this.mContext = context;
        this.mResources = this.mContext.getResources();
    }

    protected void setupView() {
        boolean z;
        super.setupView();
        if (!this.mIsMessageScrolling) {
            removeScrollView(getScrollView());
        }
        View buttonPanel = getWindow().findViewById(16908768);
        if (TextUtils.isEmpty(getButtonPositiveText()) || (TextUtils.isEmpty(getButtonNeutralText()) ^ 1) == 0) {
            z = false;
        } else {
            z = TextUtils.isEmpty(getButtonNegativeText()) ^ 1;
        }
        this.mHasThreeButtons = z;
        this.mButtonCounts = getButtonCounts();
        if (2 == this.mButtonCounts && (buttonPanel instanceof ViewGroup)) {
            View childView = ((ViewGroup) buttonPanel).getChildAt(0);
            if (childView instanceof LinearLayout) {
                this.mButtonLayout = (LinearLayout) childView;
                this.mButtonLayout.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                    public void onViewAttachedToWindow(View v) {
                        HwAlertController.this.mButtonLayout.getViewTreeObserver().addOnGlobalLayoutListener(HwAlertController.this.mGlobalLayoutListener);
                    }

                    public void onViewDetachedFromWindow(View v) {
                        HwAlertController.this.mButtonLayout.getViewTreeObserver().removeOnGlobalLayoutListener(HwAlertController.this.mGlobalLayoutListener);
                    }
                });
            }
        }
        needChangeButtonLayout(buttonPanel);
        TextView messageView = (TextView) getWindow().findViewById(16908299);
        View customPanel = getWindow().findViewById(16908817);
        ProgressBar progress = (ProgressBar) getWindow().findViewById(16908301);
        if (buttonPanel != null && messageView != null && progress == null) {
            View topPanel = getWindow().findViewById(16909400);
            View contentPanel = getWindow().findViewById(16908810);
            if (topPanel == null || contentPanel == null || buttonPanel.getVisibility() != 0 || topPanel.getVisibility() != 8 || messageView.getVisibility() != 0) {
                return;
            }
            if (customPanel == null || customPanel.getVisibility() == 8) {
                messageView.getViewTreeObserver().addOnPreDrawListener(new CustOnPreDrawListener(messageView, null));
            }
        }
    }

    private int getButtonCounts() {
        if (this.mResources == null) {
            Log.w(TAG, "getButtonCounts, mResources is null!");
            return 0;
        }
        this.mButtonList = new ArrayList();
        this.mOrientation = this.mResources.getConfiguration().orientation;
        if (!TextUtils.isEmpty(getButtonPositiveText())) {
            this.mButtonList.add(getButton(-1));
        }
        if (!TextUtils.isEmpty(getButtonNeutralText())) {
            this.mButtonList.add(getButton(-3));
        }
        if (!TextUtils.isEmpty(getButtonNegativeText())) {
            this.mButtonList.add(getButton(-2));
        }
        return this.mButtonList.size();
    }

    private void callbackFromLayoutListenner() {
        if (this.mResources == null) {
            Log.w(TAG, "callbackFromPreDrawListener, mResources is null!");
            return;
        }
        int orientation = this.mResources.getConfiguration().orientation;
        int buttonLayoutWidth = this.mButtonLayout.getWidth();
        if (1 == this.mButtonLayout.getOrientation() && (orientation != this.mOrientation || this.mOldButtonLayoutWidth != buttonLayoutWidth)) {
            setOrientionVertical(false);
            this.mOrientation = orientation;
            this.mOldButtonLayoutWidth = buttonLayoutWidth;
        } else if (this.mButtonLayout.getOrientation() == 0 && isTextOutOfBorder()) {
            setOrientionVertical(true);
        }
    }

    private boolean isTextOutOfBorder() {
        for (int i = 0; i < this.mButtonCounts; i++) {
            Button button = (Button) this.mButtonList.get(i);
            int buttonWidth = (button.getWidth() - button.getPaddingStart()) - button.getPaddingEnd();
            if (((float) buttonWidth) < Layout.getDesiredWidth(button.getText().toString().toUpperCase(Locale.US), 0, button.getText().length(), button.getPaint())) {
                return true;
            }
        }
        return false;
    }

    private void setOrientionVertical(boolean isVertical) {
        if (this.mResources == null) {
            Log.w(TAG, "setOrientionVertical, mResources is null!");
            return;
        }
        LayoutParams params;
        int defaultMargin = this.mResources.getDimensionPixelSize(34471952);
        if (isVertical) {
            params = new LayoutParams(-1, -2);
            this.mButtonLayout.setOrientation(1);
            params.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
        } else {
            params = new LayoutParams(0, -2, 1.0f);
            this.mButtonLayout.setOrientation(0);
            params.setMargins(defaultMargin, 0, defaultMargin, 0);
        }
        for (int i = 0; i < this.mButtonCounts; i++) {
            ((Button) this.mButtonList.get(i)).setLayoutParams(params);
        }
    }

    protected void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, View buttonPanel, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        super.setBackground(a, topPanel, contentPanel, customPanel, buttonPanel, hasTitle, hasCustomView, hasButtons);
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        if (hasTextTitle()) {
            View titleDivider = getWindow().findViewById(34603087);
            if (titleDivider != null) {
                titleDivider.setVisibility(0);
            }
        } else if (buttonPanel.getVisibility() == 0) {
            buttonPanel.setPadding(buttonPanel.getPaddingStart(), (int) (this.mDensity * 24.0f), buttonPanel.getPaddingEnd(), buttonPanel.getPaddingBottom());
        }
        if (contentPanel.getVisibility() == 0 && getListView() == null) {
            View hwGap1 = getWindow().findViewById(34603088);
            View hwGap2 = getWindow().findViewById(34603089);
            setLayoutIfHasTitle(hwGap1, hasTitle);
            setHwGapTwoLayout(hwGap2, hasTitle, hasCustomView, hasButtons);
        }
    }

    private void setHwGapTwoLayout(View hwGapTwo, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        if (!hasCustomView && !hasButtons) {
            setLayoutIfHasTitle(hwGapTwo, hasTitle);
        }
    }

    private void setLayoutIfHasTitle(View view, boolean hasTitle) {
        if (view != null) {
            if (hasTitle) {
                view.setLayoutParams(new LayoutParams(-1, (int) (this.mDensity * 16.0f)));
            }
            view.setVisibility(0);
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
            int defaultMargin = (int) this.mResources.getDimension(34471952);
            LayoutParams params = new LayoutParams(-1, -2);
            int childCount = buttonStyle.getChildCount();
            for (int i = 0; i < childCount; i++) {
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
