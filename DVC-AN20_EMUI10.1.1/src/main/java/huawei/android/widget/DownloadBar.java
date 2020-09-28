package huawei.android.widget;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class DownloadBar extends LinearLayout {
    public static final int DOWNLOAD_STYLE = 0;
    private static final String TAG = "DownloadBar";
    public static final int UPDATE_STYLE = 1;
    private Button mApplyButton;
    private LinearLayout mButtonLayout;
    private DownLoadWidget mDownloadWidget;
    private LinearLayout mEndItemLayout;
    private TextView mEndTextView;
    private ColorStateList mIconColors;
    private int mMaxIconSize;
    private int mPaddingTopAndBottom;
    private ResLoader mResLoader;
    private ColorStateList mSmartIconColors;
    private ColorStateList mSmartTitleColors;
    private LinearLayout mStartItemLayout;
    private TextView mStartTextView;
    private int mStyleMode;
    private ColorStateList mTextColors;
    private Button mUpdateButton;

    public DownloadBar(Context context) {
        this(context, null);
    }

    public DownloadBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(context, "downloadbar_layout"), (ViewGroup) this, true);
        this.mStartTextView = (TextView) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_start_view"));
        this.mEndTextView = (TextView) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_end_view"));
        this.mApplyButton = (Button) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_apply_button"));
        this.mUpdateButton = (Button) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_update_button"));
        this.mDownloadWidget = (DownLoadWidget) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_download_widget"));
        this.mButtonLayout = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "downlaodbar_button_layout"));
        this.mStartItemLayout = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_start_item"));
        this.mEndItemLayout = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "downloadbar_end_item"));
        this.mPaddingTopAndBottom = ResLoaderUtil.getDimensionPixelSize(context, "padding_s");
        this.mMaxIconSize = ResLoaderUtil.getDimensionPixelSize(context, "download_bar_item_icon_size");
        this.mResLoader = ResLoader.getInstance();
        int[] hwToolbarMenuStyleables = this.mResLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu");
        int menuAppearanceStyleable = this.mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu_hwToolbarMenuTextAppearance");
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(hwToolbarMenuStyleables);
            int textAppearanceId = a.getResourceId(menuAppearanceStyleable, -1);
            if (textAppearanceId != -1) {
                TypedArray menuStyleTa = theme.obtainStyledAttributes(attrs, hwToolbarMenuStyleables, this.mResLoader.getIdentifier(context, "attr", "hwToolbarMenuItemStyle"), 0);
                TypedArray textAppearanceTa = theme.obtainStyledAttributes(textAppearanceId, R.styleable.TextAppearance);
                this.mTextColors = textAppearanceTa.getColorStateList(3);
                this.mIconColors = menuStyleTa.getColorStateList(this.mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu_hwToolbarMenuItemColor"));
                menuStyleTa.recycle();
                textAppearanceTa.recycle();
            }
            a.recycle();
        }
        setStyleMode(0);
    }

    private void setItemDate(LinearLayout linearLayout, TextView textView, Drawable icon, CharSequence text) {
        if (textView == null || linearLayout == null) {
            Log.w(TAG, "DownloadBar item layout initialization failed");
            return;
        }
        textView.setText(text);
        if (icon != null || hasText(textView)) {
            if (icon != null) {
                int width = icon.getIntrinsicWidth();
                int height = icon.getIntrinsicHeight();
                int i = this.mMaxIconSize;
                if (width > i) {
                    float scale = ((float) i) / ((float) width);
                    width = this.mMaxIconSize;
                    height = (int) (((float) height) * scale);
                }
                int i2 = this.mMaxIconSize;
                if (height > i2) {
                    float scale2 = ((float) i2) / ((float) height);
                    height = this.mMaxIconSize;
                    width = (int) (((float) width) * scale2);
                }
                icon.setBounds(0, 0, width, height);
            }
            if (hasText(textView)) {
                textView.setCompoundDrawables(null, icon, null, null);
            } else {
                textView.setBackground(icon);
            }
            ColorStateList smartIconColors = getSmartIconColor();
            ColorStateList smartTitleColors = getSmartTitleColor();
            if (smartIconColors == null || smartTitleColors == null) {
                ColorStateList colorStateList = this.mIconColors;
                if (!(colorStateList == null || icon == null)) {
                    icon.setTintList(colorStateList);
                }
                ColorStateList colorStateList2 = this.mTextColors;
                if (colorStateList2 != null) {
                    textView.setTextColor(colorStateList2);
                    return;
                }
                return;
            }
            if (icon != null) {
                icon.setTintList(smartIconColors);
            }
            textView.setTextColor(smartTitleColors);
            return;
        }
        linearLayout.setVisibility(8);
    }

    public void setSmartIconColor(ColorStateList smartIconColor) {
        this.mSmartIconColors = smartIconColor;
    }

    public void setSmartTitleColor(ColorStateList smartTitleColor) {
        this.mSmartTitleColors = smartTitleColor;
    }

    private ColorStateList getSmartIconColor() {
        return this.mSmartIconColors;
    }

    private ColorStateList getSmartTitleColor() {
        return this.mSmartTitleColors;
    }

    private boolean hasText(TextView view) {
        return !TextUtils.isEmpty(view.getText());
    }

    public void setStyleMode(int mode) {
        this.mStyleMode = mode;
        int i = this.mStyleMode;
        if (i == 0) {
            this.mButtonLayout.setVisibility(8);
            this.mDownloadWidget.setVisibility(0);
        } else if (i == 1) {
            this.mButtonLayout.setVisibility(0);
            this.mDownloadWidget.setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int startTitleLine = 0;
        int endTitleLine = 0;
        TextView textView = this.mStartTextView;
        if (textView != null) {
            startTitleLine = textView.getLineCount();
        }
        TextView textView2 = this.mEndTextView;
        if (textView2 != null) {
            endTitleLine = textView2.getLineCount();
        }
        if (!(this.mEndTextView == null || this.mStartTextView == null)) {
            if (startTitleLine > 1 || endTitleLine > 1) {
                int i = this.mPaddingTopAndBottom;
                setPadding(0, i, 0, i);
                this.mStartItemLayout.setGravity(49);
                this.mEndItemLayout.setGravity(49);
            } else {
                setPadding(0, 0, 0, 0);
                this.mStartItemLayout.setGravity(17);
                this.mEndItemLayout.setGravity(17);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getStyleMode() {
        return this.mStyleMode;
    }

    public void setStartItem(Drawable icon, CharSequence title) {
        setItemDate(this.mStartItemLayout, this.mStartTextView, icon, title);
    }

    public void setStartItem(int iconResId, int titleResId) {
        Resources res = getContext().getResources();
        setItemDate(this.mStartItemLayout, this.mStartTextView, res.getDrawable(iconResId, getContext().getTheme()), res.getText(titleResId));
    }

    public void setEndItem(Drawable icon, CharSequence title) {
        setItemDate(this.mEndItemLayout, this.mEndTextView, icon, title);
    }

    public void setEndItem(int iconResId, int titleResId) {
        Resources res = getContext().getResources();
        setItemDate(this.mEndItemLayout, this.mEndTextView, res.getDrawable(iconResId, getContext().getTheme()), res.getText(titleResId));
    }

    public void setStartAndEndItem(Drawable startIcon, CharSequence startTitle, Drawable endIcon, CharSequence endTitle) {
        setItemDate(this.mStartItemLayout, this.mStartTextView, startIcon, startTitle);
        setItemDate(this.mEndItemLayout, this.mEndTextView, endIcon, endTitle);
    }

    public void setStartAndEndItem(int startIconResId, int startTitleResId, int endIconResId, int endTitleResId) {
        Resources res = getContext().getResources();
        setItemDate(this.mStartItemLayout, this.mStartTextView, res.getDrawable(startIconResId, getContext().getTheme()), res.getText(startTitleResId));
        setItemDate(this.mEndItemLayout, this.mEndTextView, res.getDrawable(endIconResId, getContext().getTheme()), res.getText(endTitleResId));
    }

    public void setStartItemClickListener(View.OnClickListener onClickListener) {
        LinearLayout linearLayout = this.mStartItemLayout;
        if (linearLayout == null) {
            Log.w(TAG, "DownloadBar startItem is null");
        } else {
            linearLayout.setOnClickListener(onClickListener);
        }
    }

    public void setEndItemClickListener(View.OnClickListener onClickListener) {
        LinearLayout linearLayout = this.mEndItemLayout;
        if (linearLayout == null) {
            Log.w(TAG, "DownloadBar endItem is null");
        } else {
            linearLayout.setOnClickListener(onClickListener);
        }
    }

    public void setApplyButtonClickListener(View.OnClickListener onClickListener) {
        Button button = this.mApplyButton;
        if (button == null) {
            Log.w(TAG, "DownloadBar primary button is null ");
        } else {
            button.setOnClickListener(onClickListener);
        }
    }

    public void setUpdateButtonClickListener(View.OnClickListener onClickListener) {
        Button button = this.mUpdateButton;
        if (button == null) {
            Log.w(TAG, "DownloadBar primary button is null ");
        } else {
            button.setOnClickListener(onClickListener);
        }
    }

    public void setButtonsText(CharSequence applyButtonText, CharSequence updateButtonText) {
        setButtonText(applyButtonText, this.mApplyButton);
        setButtonText(updateButtonText, this.mUpdateButton);
    }

    public void setButtonsText(int applyResId, int updateResId) {
        setButtonText(getContext().getResources().getText(applyResId), this.mApplyButton);
        setButtonText(getContext().getResources().getText(updateResId), this.mUpdateButton);
    }

    private void setButtonText(CharSequence buttonText, Button button) {
        if (button == null) {
            Log.w(TAG, "DownloadBar button layout initialization failed");
        } else {
            button.setText(buttonText);
        }
    }

    public int getDownLoadWidgetId() {
        return ResLoaderUtil.getViewId(getContext(), "downloadbar_download_widget");
    }

    public int getStartItemId() {
        return ResLoaderUtil.getViewId(getContext(), "downloadbar_start_item");
    }

    public int getEndItemId() {
        return ResLoaderUtil.getViewId(getContext(), "downloadbar_end_item");
    }

    public int getApplyButtonId() {
        return ResLoaderUtil.getViewId(getContext(), "downloadbar_apply_button");
    }

    public int getUpdateButtonId() {
        return ResLoaderUtil.getViewId(getContext(), "downloadbar_update_button");
    }
}
