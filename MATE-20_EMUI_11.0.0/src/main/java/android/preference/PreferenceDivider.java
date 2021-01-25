package android.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class PreferenceDivider extends PreferenceCategory {
    private int mDefHeight;
    private int mDividerHeight;
    private LinearLayout.LayoutParams mLayoutParams;
    private CharSequence mMore;

    public PreferenceDivider(Context context) {
        this(context, null);
    }

    public PreferenceDivider(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "DividerPreferenceStyle"));
    }

    public PreferenceDivider(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceDivider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Resources.Theme theme = ResLoader.getInstance().getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(attrs, ResLoader.getInstance().getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "DividerPreference"), defStyleAttr, defStyleRes);
            this.mDefHeight = a.getResources().getDimensionPixelSize(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.DIMEN, "emui_dividerPreference_default_height"));
            this.mMore = a.getString(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "DividerPreference_more"));
            this.mDividerHeight = a.getDimensionPixelSize(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "DividerPreference_dividerHeight"), this.mDefHeight);
            a.recycle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(getLayoutResource(), parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            if (getWidgetLayoutResource() != 0) {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onBindView(View view) {
        super.onBindView(view);
        CharSequence title = getTitle();
        TextView titleView = (TextView) view.findViewById(16908310);
        TextView moreView = (TextView) view.findViewById(ResLoaderUtil.getViewId(getContext(), "more"));
        LinearLayout dividerContainer = (LinearLayout) view.findViewById(ResLoaderUtil.getViewId(getContext(), "divider_container"));
        if (moreView != null) {
            CharSequence more = getMore();
            if (TextUtils.isEmpty(this.mMore)) {
                moreView.setVisibility(8);
            } else {
                moreView.setText(more);
                moreView.setVisibility(0);
            }
        }
        if (titleView != null && !TextUtils.isEmpty(title) && this.mDividerHeight <= titleView.getMeasuredHeight()) {
            this.mDividerHeight = this.mDefHeight;
        }
        this.mLayoutParams = new LinearLayout.LayoutParams(-1, this.mDividerHeight);
        if (dividerContainer != null) {
            dividerContainer.setLayoutParams(this.mLayoutParams);
        }
    }

    public void setDividerHeight(int height) {
        if ((height != 0 && this.mDividerHeight != 0) || (height != 0 && height != this.mDividerHeight)) {
            this.mDividerHeight = height;
            notifyChanged();
        }
    }

    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    public void setMore(CharSequence more) {
        if ((more == null && this.mMore != null) || (more != null && !more.equals(this.mMore))) {
            this.mMore = more;
            notifyChanged();
        }
    }

    public CharSequence getMore() {
        return this.mMore;
    }
}
