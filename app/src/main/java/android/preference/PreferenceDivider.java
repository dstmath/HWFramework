package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import androidhwext.R;

public class PreferenceDivider extends PreferenceCategory {
    private int mDefHeight;
    private int mDividerHeight;
    private CharSequence mMore;
    private LayoutParams params;

    public PreferenceDivider(Context context) {
        this(context, null);
    }

    public PreferenceDivider(Context context, AttributeSet attrs) {
        this(context, attrs, 34799630);
    }

    public PreferenceDivider(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceDivider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DividerPreference, defStyleAttr, defStyleRes);
        this.mDefHeight = a.getResources().getDimensionPixelSize(34472025);
        this.mMore = a.getString(1);
        this.mDividerHeight = a.getDimensionPixelSize(0, this.mDefHeight);
        a.recycle();
    }

    protected View onCreateView(ViewGroup parent) {
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

    protected void onBindView(View view) {
        super.onBindView(view);
        CharSequence title = getTitle();
        RelativeLayout dividerContainer = (RelativeLayout) view.findViewById(34603160);
        TextView titleView = (TextView) view.findViewById(16908310);
        TextView moreView = (TextView) view.findViewById(34603161);
        if (moreView != null) {
            CharSequence more = getMore();
            if (TextUtils.isEmpty(this.mMore)) {
                moreView.setVisibility(8);
            } else {
                moreView.setText(more);
                moreView.setVisibility(0);
            }
        }
        if (!(titleView == null || TextUtils.isEmpty(title) || this.mDividerHeight > titleView.getMeasuredHeight())) {
            this.mDividerHeight = this.mDefHeight;
        }
        this.params = new LayoutParams(-1, this.mDividerHeight);
        dividerContainer.setLayoutParams(this.params);
    }

    public void setDividerHeight(int height) {
        if (height == 0 || this.mDividerHeight == 0) {
            if (height == 0 || height == this.mDividerHeight) {
                return;
            }
        }
        this.mDividerHeight = height;
        notifyChanged();
    }

    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    public CharSequence getMore() {
        return this.mMore;
    }

    public void setMore(CharSequence more) {
        if (more != null || this.mMore == null) {
            if (more == null) {
                return;
            }
            if (more.equals(this.mMore)) {
                return;
            }
        }
        this.mMore = more;
        notifyChanged();
    }
}
