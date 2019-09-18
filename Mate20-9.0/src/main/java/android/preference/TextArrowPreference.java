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
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

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
        TypedArray a = ResLoader.getInstance().getTheme(context).obtainStyledAttributes(attrs, ResLoader.getInstance().getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference"), defStyleAttr, defStyleRes);
        this.mNetherSummary = a.getString(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_netherSummary"));
        this.mDetail = a.getString(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_detail"));
        this.mHideArrow = a.getBoolean(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "TextArrowPreference_hideArrow"), false);
        a.recycle();
    }

    public TextArrowPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextArrowPreference(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "textArrowPreferenceStyle"));
    }

    public TextArrowPreference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(getLayoutResource(), parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            if (getWidgetLayoutResource() == 0 || this.mHideArrow) {
                widgetFrame.setVisibility(8);
            } else {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            }
        }
        adjustLayoutPadding(layout);
        return layout;
    }

    /* access modifiers changed from: protected */
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
        View title_frame = view.findViewById(ResLoaderUtil.getViewId(getContext(), "title_frame"));
        if (title_frame != null) {
            title_frame.setVisibility(TextUtils.isEmpty(getTitle()) && TextUtils.isEmpty(getSummary()) && TextUtils.isEmpty(getNetherSummary()) ? 8 : 0);
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
        if (arrowWidgetView != null && (arrowWidgetView instanceof ImageView)) {
            if (!this.mHideArrow) {
                i = 0;
            }
            arrowWidgetView.setVisibility(i);
        }
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
        int padding_left_right = dp2pixel(PADDING_LEFT_RIGHT);
        int padding_tb_singline = dp2pixel(PADDING_TOP_BOTTOM_SINGLELINE);
        int padding_tb_twolines = dp2pixel(PADDING_TOP_BOTTOM_TWOLINES);
        int padding_tb_threelines = dp2pixel(6);
        if (titleView != null && !TextUtils.isEmpty(getTitle())) {
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
