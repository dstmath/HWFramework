package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class TextArrowPreference extends Preference {
    private static final int CHILD_COUNT_TWO = 2;
    private static final float DP_PIXEL_VALUE = 0.5f;
    private static final int PADDING_LEFT_RIGHT = 0;
    private static final int PADDING_TOP_BOTTOM_SINGLELINE = 0;
    private static final int PADDING_TOP_BOTTOM_THREELINES = 16;
    private static final int PADDING_TOP_BOTTOM_TWOLINES = 8;
    private CharSequence mDetail;
    private boolean mIsHideArrow;
    private CharSequence mNetherSummary;

    public TextArrowPreference(Context context) {
        this(context, null);
    }

    public TextArrowPreference(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "textArrowPreferenceStyle"));
    }

    public TextArrowPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextArrowPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = ResLoader.getInstance().getTheme(context).obtainStyledAttributes(attrs, ResLoader.getInstance().getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference"), defStyleAttr, defStyleRes);
        this.mNetherSummary = array.getString(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_netherSummary"));
        this.mDetail = array.getString(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_detail"));
        this.mIsHideArrow = array.getBoolean(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_hideArrow"), false);
        array.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public View onCreateView(ViewGroup parent) {
        ViewGroup widgetFrame;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layoutView = layoutInflater.inflate(getLayoutResource(), parent, false);
        if (!(layoutView == null || (widgetFrame = (ViewGroup) layoutView.findViewById(16908312)) == null)) {
            if (getWidgetLayoutResource() == 0 || this.mIsHideArrow) {
                widgetFrame.setVisibility(8);
            } else {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            }
        }
        return layoutView;
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onBindView(View view) {
        super.onBindView(view);
        View imageFrame = view.findViewById(ResLoaderUtil.getViewId(getContext(), "icon_frame"));
        int i = 8;
        if (imageFrame != null) {
            imageFrame.setVisibility(getIcon() != null ? 0 : 8);
        }
        TextView netherSummaryView = (TextView) view.findViewById(ResLoaderUtil.getViewId(getContext(), "nether_summary"));
        if (netherSummaryView != null) {
            CharSequence netherSummary = getNetherSummary();
            if (TextUtils.isEmpty(netherSummary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(netherSummary);
                netherSummaryView.setVisibility(0);
            }
        }
        View titleFrame = view.findViewById(ResLoaderUtil.getViewId(getContext(), "titleFrame"));
        if (titleFrame != null) {
            titleFrame.setVisibility(TextUtils.isEmpty(getTitle()) && TextUtils.isEmpty(getSummary()) && TextUtils.isEmpty(getNetherSummary()) ? 8 : 0);
        }
        TextView detailView = (TextView) view.findViewById(ResLoaderUtil.getViewId(getContext(), "detail"));
        if (detailView != null) {
            CharSequence detail = getDetail();
            if (TextUtils.isEmpty(this.mDetail)) {
                detailView.setVisibility(8);
            } else {
                detailView.setText(detail);
                detailView.setVisibility(0);
            }
        }
        View arrowWidgetView = view.findViewById(ResLoaderUtil.getViewId(getContext(), "arrowWidget"));
        if (arrowWidgetView != null) {
            if (!this.mIsHideArrow) {
                i = 0;
            }
            arrowWidgetView.setVisibility(i);
        }
        adjustLayoutPadding(view);
    }

    public CharSequence getDetail() {
        return this.mDetail;
    }

    public void setDetail(CharSequence detail) {
        if ((detail == null && this.mDetail != null) || (detail != null && !detail.equals(this.mDetail))) {
            this.mDetail = detail;
            notifyChanged();
        }
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence netherSummary) {
        if ((netherSummary == null && this.mNetherSummary != null) || (netherSummary != null && !netherSummary.equals(this.mNetherSummary))) {
            this.mNetherSummary = netherSummary;
            notifyChanged();
        }
    }

    public boolean isHideArrow() {
        return this.mIsHideArrow;
    }

    public void setHideArrow(boolean isHide) {
        if (this.mIsHideArrow != isHide) {
            this.mIsHideArrow = isHide;
            notifyChanged();
        }
    }

    private void adjustLayoutPadding(View view) {
        TextView titleView = (TextView) view.findViewById(16908310);
        int paddingLeftRight = dp2pixel(0);
        int paddingTbSingline = dp2pixel(0);
        int paddingTbTwolines = dp2pixel(8);
        int paddingTbThreelines = dp2pixel(16);
        if (titleView != null && !TextUtils.isEmpty(getTitle()) && (titleView.getParent() instanceof RelativeLayout)) {
            int childCount = getChildCount(view, titleView);
            if (childCount == 1) {
                view.setPadding(paddingLeftRight, paddingTbSingline, paddingLeftRight, paddingTbSingline);
            } else if (childCount == 2) {
                view.setPadding(paddingLeftRight, paddingTbTwolines, paddingLeftRight, paddingTbTwolines);
            } else {
                view.setPadding(paddingLeftRight, paddingTbThreelines, paddingLeftRight, paddingTbThreelines);
            }
        }
    }

    private int getChildCount(View view, TextView titleView) {
        TextView summaryView = (TextView) view.findViewById(16908304);
        TextView netherSummaryView = (TextView) view.findViewById(ResLoaderUtil.getViewId(getContext(), "nether_summary"));
        int childCount = 0;
        if (titleView.getVisibility() == 0) {
            childCount = 0 + 1;
        }
        if (summaryView != null && summaryView.getVisibility() == 0) {
            childCount++;
        }
        if (netherSummaryView == null || netherSummaryView.getVisibility() != 0) {
            return childCount;
        }
        return childCount + 1;
    }

    private int dp2pixel(int dpValue) {
        return (int) ((((float) dpValue) * getContext().getResources().getDisplayMetrics().density) + DP_PIXEL_VALUE);
    }
}
