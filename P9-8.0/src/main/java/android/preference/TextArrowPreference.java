package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidhwext.R;

public class TextArrowPreference extends Preference {
    private static final int PADDING_LEFT_RIGHT = 16;
    private static final int PADDING_TOP_BOTTOM_SINGLELINE = 14;
    private static final int PADDING_TOP_BOTTOM_THREELINES = 6;
    private static final int PADDING_TOP_BOTTOM_TWOLINES = 9;
    private CharSequence mDetail;
    private boolean mHideArrow;
    private CharSequence mNetherSummary;

    public TextArrowPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextArrowPreference, defStyleAttr, defStyleRes);
        this.mNetherSummary = a.getString(1);
        this.mDetail = a.getString(0);
        this.mHideArrow = a.getBoolean(2, false);
        a.recycle();
    }

    public TextArrowPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextArrowPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 33620021);
    }

    public TextArrowPreference(Context context) {
        this(context, null);
    }

    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(getLayoutResource(), parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            if (getWidgetLayoutResource() == 0 || (this.mHideArrow ^ 1) == 0) {
                widgetFrame.setVisibility(8);
            } else {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            }
        }
        adjustLayoutPadding(layout);
        return layout;
    }

    protected void onBindView(View view) {
        int i;
        int i2 = 8;
        super.onBindView(view);
        View imageFrame = view.findViewById(34603108);
        if (imageFrame != null) {
            if (getIcon() != null) {
                i = 0;
            } else {
                i = 8;
            }
            imageFrame.setVisibility(i);
        }
        TextView netherSummaryView = (TextView) view.findViewById(34603115);
        if (netherSummaryView != null) {
            CharSequence netherSummary = getNetherSummary();
            if (TextUtils.isEmpty(netherSummary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(netherSummary);
                netherSummaryView.setVisibility(0);
            }
        }
        View title_frame = view.findViewById(34603109);
        if (title_frame != null) {
            boolean title_frame_empty;
            if (TextUtils.isEmpty(getTitle()) && TextUtils.isEmpty(getSummary())) {
                title_frame_empty = TextUtils.isEmpty(getNetherSummary());
            } else {
                title_frame_empty = false;
            }
            if (title_frame_empty) {
                i = 8;
            } else {
                i = 0;
            }
            title_frame.setVisibility(i);
        }
        TextView detailView = (TextView) view.findViewById(34603110);
        if (detailView != null) {
            CharSequence detail = getDetail();
            if (TextUtils.isEmpty(this.mDetail)) {
                detailView.setVisibility(8);
            } else {
                detailView.setText(detail);
                detailView.setVisibility(0);
            }
        }
        View arrowWidgetView = view.findViewById(34603116);
        if (arrowWidgetView != null && (arrowWidgetView instanceof ImageView)) {
            if (!this.mHideArrow) {
                i2 = 0;
            }
            arrowWidgetView.setVisibility(i2);
        }
    }

    public CharSequence getDetail() {
        return this.mDetail;
    }

    public void setDetail(CharSequence detail) {
        if ((detail == null && this.mDetail != null) || (detail != null && (detail.equals(this.mDetail) ^ 1) != 0)) {
            this.mDetail = detail;
            notifyChanged();
        }
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence netherSummary) {
        if ((netherSummary == null && this.mNetherSummary != null) || (netherSummary != null && (netherSummary.equals(this.mNetherSummary) ^ 1) != 0)) {
            this.mNetherSummary = netherSummary;
            notifyChanged();
        }
    }

    public boolean isHideArrow() {
        return this.mHideArrow;
    }

    public void setHideArrow(boolean hideArrow) {
        if (this.mHideArrow != hideArrow) {
            this.mHideArrow = hideArrow;
            notifyChanged();
        }
    }

    private void adjustLayoutPadding(View view) {
        TextView titleView = (TextView) view.findViewById(16908310);
        int padding_left_right = dp2pixel(16);
        int padding_tb_singline = dp2pixel(14);
        int padding_tb_twolines = dp2pixel(9);
        int padding_tb_threelines = dp2pixel(6);
        if (titleView != null && (TextUtils.isEmpty(getTitle()) ^ 1) != 0) {
            ViewParent viewParent = titleView.getParent();
            if (viewParent != null && (viewParent instanceof RelativeLayout)) {
                int childCount = ((RelativeLayout) viewParent).getChildCount();
                if (childCount == 1) {
                    view.setPadding(padding_left_right, padding_tb_singline, padding_left_right, padding_tb_singline);
                } else if (childCount == 2) {
                    view.setPadding(padding_left_right, padding_tb_twolines, padding_left_right, padding_tb_twolines);
                } else {
                    view.setPadding(padding_left_right, padding_tb_threelines, padding_left_right, padding_tb_threelines);
                }
            }
        }
    }

    private int dp2pixel(int dpValue) {
        return (int) ((((float) dpValue) * getContext().getResources().getDisplayMetrics().density) + 0.5f);
    }
}
