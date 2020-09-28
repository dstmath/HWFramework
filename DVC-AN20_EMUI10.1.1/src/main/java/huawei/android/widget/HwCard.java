package huawei.android.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

@Deprecated
public class HwCard {
    private static final float DEFAULT_CONTENT_VIEW_MAX_HEIGHT_DP = 426.0f;
    private static final float DEFAULT_CONTENT_VIEW_MIN_HEIGHT_DP = 44.0f;
    private static final String TAG = "HwCard";
    private ViewGroup mCardLayout;
    private float mContentMaxHeightDp;
    private float mContentMinHeightDp;
    private ViewStub mContentStub;
    private View mContentView;
    private Context mContext;
    private ViewStub mFooterStub;
    private View mFooterView;
    private ViewStub mHeaderStub;
    private View mHeaderView;
    private LayoutInflater mInflater;
    private boolean mIsExpandState;

    public interface OnCardClickListener {
        void onClick();
    }

    private HwCard(Context context) {
        this.mContentMinHeightDp = DEFAULT_CONTENT_VIEW_MIN_HEIGHT_DP;
        this.mContentMaxHeightDp = DEFAULT_CONTENT_VIEW_MAX_HEIGHT_DP;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(ResLoader.getInstance().getContext(context));
        inflateCardLayout();
    }

    private static int getIdentifier(Context context, String type, String name) {
        int id = ResLoader.getInstance().getIdentifier(context, type, name);
        if (id == 0) {
            Log.w(TAG, "resources is not found");
        }
        return id;
    }

    private void inflateCardLayout() {
        int layoutId = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "hwcard_layout_stubs");
        if (layoutId != 0) {
            View view = this.mInflater.inflate(layoutId, (ViewGroup) null);
            if (view instanceof ViewGroup) {
                this.mCardLayout = (ViewGroup) view;
            }
            this.mHeaderStub = (ViewStub) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "header_stub"));
            this.mContentStub = (ViewStub) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "content_stub"));
            this.mFooterStub = (ViewStub) view.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "footer_stub"));
        }
    }

    /* access modifiers changed from: protected */
    public void setHeaderView(View headerView) {
        if (this.mHeaderView != null) {
            Log.w(TAG, "mHeaderView already exists");
        } else if (headerView != null) {
            replaceStubWithView(headerView, this.mHeaderStub);
            this.mHeaderView = headerView;
        }
    }

    /* access modifiers changed from: protected */
    public void setContentView(View contentView) {
        if (this.mContentView != null) {
            Log.w(TAG, "mContentView already exists");
        } else if (contentView != null) {
            this.mContentView = contentView;
            replaceStubWithView(this.mContentView, this.mContentStub);
            adjustContentViewMargin();
            adjustContentViewHeight();
        }
    }

    /* access modifiers changed from: protected */
    public void setFooterView(View footerView) {
        if (this.mFooterView != null) {
            Log.w(TAG, "mFooterView already exists");
        } else if (footerView != null) {
            replaceStubWithView(footerView, this.mFooterStub);
            this.mFooterView = footerView;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setContentMinHeightDp(float minHeightDp) {
        this.mContentMinHeightDp = minHeightDp;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setContentMaxHeightDp(float maxHeightDp) {
        this.mContentMaxHeightDp = maxHeightDp;
    }

    private void adjustContentViewMargin() {
        View view = this.mContentView;
        if (view != null && this.mHeaderView == null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.topMargin = ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "margin_l"));
            this.mContentView.setLayoutParams(layoutParams);
        }
        View view2 = this.mContentView;
        if (view2 != null && this.mFooterView == null) {
            ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) view2.getLayoutParams();
            layoutParams2.bottomMargin = ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "margin_l"));
            this.mContentView.setLayoutParams(layoutParams2);
        }
    }

    private void adjustContentViewHeight() {
        float f = this.mContentMinHeightDp;
        if (f >= 0.0f) {
            this.mContentView.setMinimumHeight((int) TypedValue.applyDimension(1, f, this.mContext.getResources().getDisplayMetrics()));
        }
        float f2 = this.mContentMaxHeightDp;
        if (f2 >= 0.0f) {
            final int contentViewMaxHeightPx = (int) TypedValue.applyDimension(1, f2, this.mContext.getResources().getDisplayMetrics());
            this.mContentView.post(new Runnable() {
                /* class huawei.android.widget.HwCard.AnonymousClass1 */

                public void run() {
                    ViewGroup.LayoutParams layoutParams = HwCard.this.mContentView.getLayoutParams();
                    int measuredHeight = HwCard.this.mContentView.getMeasuredHeight();
                    int i = contentViewMaxHeightPx;
                    if (measuredHeight > i) {
                        layoutParams.height = i;
                    }
                    if (HwCard.this.mContentView.getVisibility() == 4) {
                        HwCard.this.mContentView.setVisibility(8);
                    }
                    HwCard.this.mContentView.setLayoutParams(layoutParams);
                    HwCard.this.mContentView.requestLayout();
                }
            });
        }
    }

    private void replaceStubWithView(View view, ViewStub stub) {
        ViewGroup viewGroup = this.mCardLayout;
        if (viewGroup == null) {
            Log.w(TAG, "mCardLayout is null");
            return;
        }
        int index = viewGroup.indexOfChild(stub);
        this.mCardLayout.removeViewInLayout(stub);
        ViewGroup.LayoutParams layoutParams = stub.getLayoutParams();
        if (layoutParams != null) {
            this.mCardLayout.addView(view, index, layoutParams);
        } else {
            this.mCardLayout.addView(view, index);
        }
    }

    public void fillCardView(FrameLayout view) {
        if (view != null) {
            view.removeAllViews();
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (layoutParams != null) {
                int margin = ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "margin_s"));
                layoutParams.setMarginStart(margin);
                layoutParams.setMarginEnd(margin);
                view.setLayoutParams(layoutParams);
            }
            if (this.mCardLayout.getParent() != null) {
                ((ViewGroup) this.mCardLayout.getParent()).removeAllViews();
            }
            view.addView(this.mCardLayout);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0007, code lost:
        r0 = (android.widget.ImageView) r0.findViewById(getIdentifier(r6.mContext, huawei.android.widget.loader.ResLoaderUtil.ID, "action_expand"));
     */
    private void initCardExpandable(boolean isExpanded) {
        final ImageView expandView;
        int i;
        this.mIsExpandState = isExpanded;
        View view = this.mHeaderView;
        if (view != null && expandView != null) {
            int i2 = 0;
            expandView.setVisibility(0);
            View view2 = this.mContentView;
            if (view2 != null) {
                if (isExpanded) {
                    i = 0;
                } else {
                    i = this.mContentMaxHeightDp < 0.0f ? 8 : 4;
                }
                view2.setVisibility(i);
            }
            View view3 = this.mFooterView;
            if (view3 != null) {
                if (!isExpanded) {
                    i2 = 8;
                }
                view3.setVisibility(i2);
            }
            final int expandIcon = getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_arrow_up");
            final int closeIcon = getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_arrow_down");
            expandView.setImageResource(isExpanded ? expandIcon : closeIcon);
            Builder.setButtonClickEffect(expandView);
            expandView.setOnClickListener(new View.OnClickListener() {
                /* class huawei.android.widget.HwCard.AnonymousClass2 */

                public void onClick(View v) {
                    HwCard hwCard = HwCard.this;
                    hwCard.mIsExpandState = !hwCard.mIsExpandState;
                    int drawableId = HwCard.this.mIsExpandState ? expandIcon : closeIcon;
                    if (drawableId != 0) {
                        expandView.setImageResource(drawableId);
                    }
                    int viewVisible = HwCard.this.mIsExpandState ? 0 : 8;
                    if (HwCard.this.mContentView != null) {
                        HwCard.this.mContentView.setVisibility(viewVisible);
                    }
                    if (HwCard.this.mFooterView != null) {
                        HwCard.this.mFooterView.setVisibility(viewVisible);
                    }
                }
            });
        }
    }

    public static class Builder {
        public static final int FOOTER_TYPE_DEFAULT = 8193;
        public static final int FOOTER_TYPE_NO_FOOTER = 8192;
        public static final int FOOTER_TYPE_THREE_BUTTON = 8193;
        public static final int HEADER_TYPE_DEFAULT = 4098;
        public static final int HEADER_TYPE_NO_BUTTON = 4097;
        public static final int HEADER_TYPE_NO_HEADER = 4096;
        public static final int HEADER_TYPE_ONE_BUTTON = 4098;
        public static final int HEADER_TYPE_TOW_BUTTON = 4099;
        private OnCardClickListener mActionInfoListener;
        private OnCardClickListener mActionMoreListener;
        private int mContentLayout = 0;
        private float mContentMaxHeightDp = HwCard.DEFAULT_CONTENT_VIEW_MAX_HEIGHT_DP;
        private float mContentMinHeightDp = HwCard.DEFAULT_CONTENT_VIEW_MIN_HEIGHT_DP;
        private Context mContext;
        private int mFooterLayout = 0;
        private int mFooterType = 8193;
        private int mHeaderLayout = 0;
        private int mHeaderType = 4098;
        private int mIcon;
        private LayoutInflater mInflater;
        private boolean mIsExpandable;
        private boolean mIsExpanded = true;
        private OnCardClickListener mNegativeOnClickListener;
        private CharSequence mNegativeText;
        private OnCardClickListener mNeutralOnClickListener;
        private CharSequence mNeutralText;
        private OnCardClickListener mPositiveOnClickListener;
        private CharSequence mPositiveText;
        private Template mTemplate;
        private CharSequence mTitle;

        public Builder(Context context) {
            this.mContext = context;
            this.mInflater = LayoutInflater.from(ResLoader.getInstance().getContext(context));
            initDefaultLayout();
        }

        private int getIdentifier(String type, String name) {
            return ResLoader.getInstance().getIdentifier(this.mContext, type, name);
        }

        private void initDefaultLayout() {
            this.mHeaderLayout = getHeaderLayoutByType(this.mHeaderType);
            this.mFooterLayout = getFooterLayoutByType(this.mFooterType);
        }

        private Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private LayoutInflater getInflater() {
            return this.mInflater;
        }

        public void setTemplate(Template template) {
            this.mTemplate = template;
        }

        public Builder setHeaderType(int type) {
            this.mHeaderType = type;
            this.mHeaderLayout = getHeaderLayoutByType(type);
            return this;
        }

        public Builder setFooterType(int type) {
            this.mFooterType = type;
            this.mFooterLayout = getHeaderLayoutByType(type);
            return this;
        }

        private int getHeaderLayoutByType(int type) {
            if (type == 4096) {
                return 0;
            }
            if (type == 4097) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_no_button");
            }
            if (type == 4098) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_with_one_button");
            }
            if (type == 4099) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_with_tow_button");
            }
            return 0;
        }

        private int getFooterLayoutByType(int type) {
            if (type != 8192 && type == 8193) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_footer_three_button");
            }
            return 0;
        }

        public Builder setContentView(int layoutId) {
            this.mContentLayout = layoutId;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSmallIcon(int icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setExpandable(boolean isCanExpand) {
            this.mIsExpandable = isCanExpand;
            return this;
        }

        public Builder setExpandState(boolean isExpanded) {
            this.mIsExpanded = isExpanded;
            return this;
        }

        public Builder setActionMoreListener(OnCardClickListener listener) {
            this.mActionMoreListener = listener;
            return this;
        }

        public Builder setActionInfoListener(OnCardClickListener listener) {
            this.mActionInfoListener = listener;
            return this;
        }

        public Builder setPositiveListener(CharSequence text, OnCardClickListener listener) {
            this.mPositiveText = text;
            this.mPositiveOnClickListener = listener;
            return this;
        }

        public Builder setNegativeListener(CharSequence text, OnCardClickListener listener) {
            this.mNegativeText = text;
            this.mNegativeOnClickListener = listener;
            return this;
        }

        public Builder setNeutralListener(CharSequence text, OnCardClickListener listener) {
            this.mNeutralText = text;
            this.mNeutralOnClickListener = listener;
            return this;
        }

        public Builder setContentMinHeightDp(float minHeightDp) {
            this.mContentMinHeightDp = minHeightDp;
            return this;
        }

        public Builder setContentMaxHeightDp(float maxHeightDp) {
            this.mContentMaxHeightDp = maxHeightDp;
            return this;
        }

        public HwCard build() {
            return build(false);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private HwCard build(boolean isHastemplate) {
            HwCard card = new HwCard(this.mContext);
            inflateHeaderView(card);
            inflateFooterView(card);
            card.setContentMinHeightDp(this.mContentMinHeightDp);
            card.setContentMaxHeightDp(this.mContentMaxHeightDp);
            if (isHastemplate) {
                Template template = this.mTemplate;
                if (template != null) {
                    card.setContentView(template.makeContentView());
                }
            } else {
                inflateContentView(card);
            }
            enableExpand(card);
            return card;
        }

        private void enableExpand(HwCard card) {
            if (this.mIsExpandable) {
                card.initCardExpandable(this.mIsExpanded);
            }
        }

        private void inflateHeaderView(HwCard card) {
            View header;
            if (this.mHeaderLayout == 0 || card == null) {
                Log.w(HwCard.TAG, "mHeaderLayout == 0 or card is null");
                return;
            }
            if (getHeaderLayoutByType(HEADER_TYPE_NO_BUTTON) == this.mHeaderLayout) {
                header = inflaterHeaderNoButton();
            } else if (getHeaderLayoutByType(4098) == this.mHeaderLayout) {
                header = inflaterHeaderWithOneButton();
            } else if (getHeaderLayoutByType(HEADER_TYPE_TOW_BUTTON) == this.mHeaderLayout) {
                header = inflaterHeaderWithTowButton();
            } else {
                header = inflaterHeaderCustom();
            }
            if (header != null) {
                card.setHeaderView(header);
            }
        }

        private void inflateFooterView(HwCard card) {
            View header;
            if (this.mFooterLayout == 0 || card == null) {
                Log.w(HwCard.TAG, "mFooterLayout == 0 or card is null");
                return;
            }
            if (getFooterLayoutByType(8193) == this.mFooterLayout) {
                header = inflaterFooterThreeText();
            } else {
                header = inflaterFooterCustom();
            }
            if (header != null) {
                card.setFooterView(header);
            }
        }

        private void inflateContentView(HwCard card) {
            int i = this.mContentLayout;
            if (i == 0 || card == null) {
                Log.w(HwCard.TAG, "mContentLayout == 0 or card is null");
                return;
            }
            View content = this.mInflater.inflate(i, (ViewGroup) null);
            if (content != null) {
                card.setContentView(content);
            }
        }

        private View inflaterHeaderNoButton() {
            int layoutId = getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_no_button");
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, (ViewGroup) null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            int i = this.mIcon;
            if (i != 0) {
                icon.setImageResource(i);
            }
            if (this.mIsExpandable) {
                bindHeaderClickListener(view);
                adjustHeaderPadding(view);
            }
            return view;
        }

        private View inflaterHeaderWithOneButton() {
            int layoutId = getHeaderLayoutByType(4098);
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, (ViewGroup) null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ImageView actionMore = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_more"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            int i = this.mIcon;
            if (i != 0) {
                icon.setImageResource(i);
            }
            setButtonClickEffect(actionMore);
            if (this.mActionMoreListener != null) {
                actionMore.setOnClickListener(new View.OnClickListener() {
                    /* class huawei.android.widget.HwCard.Builder.AnonymousClass1 */

                    public void onClick(View v) {
                        Builder.this.mActionMoreListener.onClick();
                    }
                });
            }
            if (this.mIsExpandable) {
                bindHeaderClickListener(view);
            }
            return view;
        }

        private View inflaterHeaderWithTowButton() {
            int layoutId = getHeaderLayoutByType(HEADER_TYPE_TOW_BUTTON);
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, (ViewGroup) null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            int i = this.mIcon;
            if (i != 0) {
                icon.setImageResource(i);
            }
            ImageView actionMore = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_more"));
            setButtonClickEffect(actionMore);
            if (this.mActionMoreListener != null) {
                actionMore.setOnClickListener(new View.OnClickListener() {
                    /* class huawei.android.widget.HwCard.Builder.AnonymousClass2 */

                    public void onClick(View v) {
                        Builder.this.mActionMoreListener.onClick();
                    }
                });
            }
            ImageView actionInfo = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_info"));
            setButtonClickEffect(actionInfo);
            if (this.mActionInfoListener != null) {
                actionInfo.setOnClickListener(new View.OnClickListener() {
                    /* class huawei.android.widget.HwCard.Builder.AnonymousClass3 */

                    public void onClick(View v) {
                        Builder.this.mActionInfoListener.onClick();
                    }
                });
            }
            if (this.mIsExpandable) {
                bindHeaderClickListener(view);
            }
            return view;
        }

        private void bindHeaderClickListener(View header) {
            final ImageView expandIcon = (ImageView) header.findViewById(getIdentifier(ResLoaderUtil.ID, "action_expand"));
            header.setOnClickListener(new View.OnClickListener() {
                /* class huawei.android.widget.HwCard.Builder.AnonymousClass4 */

                public void onClick(View v) {
                    ImageView imageView = expandIcon;
                    if (imageView != null) {
                        imageView.performClick();
                    }
                }
            });
        }

        private void adjustHeaderPadding(View header) {
            header.setPaddingRelative(header.getPaddingStart(), 0, ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(ResLoaderUtil.DIMEN, "padding_m")), 0);
        }

        /* access modifiers changed from: private */
        public static void setButtonClickEffect(View button) {
            if (button != null) {
                button.setBackground(new HwAnimatedGradientDrawable());
            }
        }

        private View inflaterHeaderCustom() {
            return null;
        }

        private View inflaterFooterThreeText() {
            int layoutId;
            if (isEmptyFooter() || (layoutId = getFooterLayoutByType(8193)) == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, (ViewGroup) null);
            final TextView positive = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_positive"));
            final TextView negative = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_negative"));
            final TextView neutral = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_neutral"));
            View.OnClickListener listener = new View.OnClickListener() {
                /* class huawei.android.widget.HwCard.Builder.AnonymousClass5 */

                public void onClick(View v) {
                    if (v == positive) {
                        if (Builder.this.mPositiveOnClickListener != null) {
                            Builder.this.mPositiveOnClickListener.onClick();
                        }
                    } else if (v == negative) {
                        if (Builder.this.mNegativeOnClickListener != null) {
                            Builder.this.mNegativeOnClickListener.onClick();
                        }
                    } else if (v == neutral && Builder.this.mNeutralOnClickListener != null) {
                        Builder.this.mNeutralOnClickListener.onClick();
                    }
                }
            };
            initPositiveText(positive, listener);
            initNeutralText(neutral, listener);
            initNegativeText(negative, listener);
            return view;
        }

        private boolean isEmptyFooter() {
            return (TextUtils.isEmpty(this.mPositiveText) && TextUtils.isEmpty(this.mNegativeText) && TextUtils.isEmpty(this.mNeutralText)) && (this.mPositiveOnClickListener == null && this.mNegativeOnClickListener == null && this.mNeutralOnClickListener == null);
        }

        private void initPositiveText(TextView positive, View.OnClickListener listener) {
            if (positive != null) {
                if (!(this.mPositiveText == null && this.mPositiveOnClickListener == null)) {
                    positive.setText(this.mPositiveText);
                    positive.setOnClickListener(listener);
                    positive.setVisibility(0);
                }
                if (this.mNeutralText == null && this.mNegativeText == null) {
                    ViewGroup.LayoutParams positiveLayoutParams = positive.getLayoutParams();
                    if (positiveLayoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams positiveMarginLayoutParams = (ViewGroup.MarginLayoutParams) positiveLayoutParams;
                        positiveMarginLayoutParams.setMarginEnd(0);
                        positive.setLayoutParams(positiveMarginLayoutParams);
                    }
                }
            }
        }

        private void initNeutralText(TextView neutral, View.OnClickListener listener) {
            if (neutral != null) {
                if (!(this.mNeutralText == null && this.mNeutralOnClickListener == null)) {
                    neutral.setText(this.mNeutralText);
                    neutral.setOnClickListener(listener);
                    neutral.setVisibility(0);
                }
                if (this.mNegativeText == null) {
                    ViewGroup.LayoutParams neutralLayoutParams = neutral.getLayoutParams();
                    if (neutralLayoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams neutralMarginLayoutParams = (ViewGroup.MarginLayoutParams) neutralLayoutParams;
                        neutralMarginLayoutParams.setMarginEnd(0);
                        neutral.setLayoutParams(neutralMarginLayoutParams);
                    }
                }
            }
        }

        private void initNegativeText(TextView negative, View.OnClickListener listener) {
            if (negative != null) {
                if (this.mNegativeText != null || this.mPositiveOnClickListener != null) {
                    negative.setText(this.mNegativeText);
                    negative.setOnClickListener(listener);
                    negative.setVisibility(0);
                }
            }
        }

        private View inflaterFooterCustom() {
            return null;
        }
    }

    public static abstract class Template {
        private Builder mBuilder;

        public abstract View makeContentView();

        public Template(Builder builder) {
            setBuilder(builder);
        }

        public LayoutInflater getLayoutInflater() {
            Builder builder = this.mBuilder;
            if (builder != null) {
                return builder.getInflater();
            }
            Log.w(HwCard.TAG, "mBuilder is null");
            return null;
        }

        private void setBuilder(Builder builder) {
            if (builder != null && this.mBuilder != builder) {
                this.mBuilder = builder;
                builder.setTemplate(this);
            }
        }

        public HwCard build() {
            Builder builder = this.mBuilder;
            if (builder != null) {
                return builder.build(true);
            }
            return null;
        }
    }
}
