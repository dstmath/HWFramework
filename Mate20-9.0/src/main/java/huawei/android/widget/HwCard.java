package huawei.android.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
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

public class HwCard {
    private static final String TAG = "HwCard";
    private ViewGroup mCardLayout;
    private ViewStub mContentStub;
    /* access modifiers changed from: private */
    public View mContentView;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mExpandState;
    private ViewStub mFooterStub;
    /* access modifiers changed from: private */
    public View mFooterView;
    private ViewStub mHeaderStub;
    private View mHeaderView;
    private LayoutInflater mInflater;

    public static class Builder {
        public static final int FOOTER_TYPE_DEFAULT = 8193;
        public static final int FOOTER_TYPE_NO_FOOTER = 8192;
        public static final int FOOTER_TYPE_THREE_BUTTON = 8193;
        public static final int HEADER_TYPE_DEFAULT = 4098;
        public static final int HEADER_TYPE_NO_BUTTON = 4097;
        public static final int HEADER_TYPE_NO_HEADER = 4096;
        public static final int HEADER_TYPE_ONE_BUTTON = 4098;
        public static final int HEADER_TYPE_TOW_BUTTON = 4099;
        private boolean isExpandable;
        private boolean isExpanded = true;
        /* access modifiers changed from: private */
        public OnCardClickListener mActionInfoListener;
        /* access modifiers changed from: private */
        public OnCardClickListener mActionMoreListener;
        private int mContentLayout = 0;
        private Context mContext;
        private int mFooterLayout = 0;
        private int mFooterType = 8193;
        private int mHeaderLayout = 0;
        private int mHeaderType = 4098;
        private int mIcon;
        private LayoutInflater mInflater;
        /* access modifiers changed from: private */
        public OnCardClickListener mNegativeOnClickListener;
        private CharSequence mNegativeText;
        /* access modifiers changed from: private */
        public OnCardClickListener mNeutralOnClickListener;
        private CharSequence mNeutralText;
        /* access modifiers changed from: private */
        public OnCardClickListener mPositiveOnClickListener;
        private CharSequence mPositiveText;
        private Template mTemplate;
        private CharSequence mTitle;

        private int getIdentifier(String type, String name) {
            return ResLoader.getInstance().getIdentifier(this.mContext, type, name);
        }

        public Builder(Context context) {
            this.mContext = context;
            this.mInflater = LayoutInflater.from(ResLoader.getInstance().getContext(context));
            initDefaultLayout();
        }

        private void initDefaultLayout() {
            this.mHeaderLayout = getHeaderLayoutByType(this.mHeaderType);
            this.mFooterLayout = getFooterLayoutByType(this.mFooterType);
        }

        private Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: private */
        public LayoutInflater getInflater() {
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
            if (4096 == type) {
                return 0;
            }
            if (4097 == type) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_no_button");
            }
            if (4098 == type) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_with_one_button");
            }
            if (4099 == type) {
                return getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_with_tow_button");
            }
            return 0;
        }

        private int getFooterLayoutByType(int type) {
            if (8192 == type) {
                return 0;
            }
            if (8193 == type) {
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

        public Builder setExpandable(boolean canExpand) {
            this.isExpandable = canExpand;
            return this;
        }

        public Builder setExpandState(boolean expanded) {
            this.isExpanded = expanded;
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

        public HwCard build() {
            return build(false);
        }

        /* access modifiers changed from: private */
        public HwCard build(boolean template) {
            HwCard card = new HwCard(this.mContext);
            inflateHeaderView(card);
            inflateFooterView(card);
            if (!template) {
                inflateContentView(card);
            } else if (this.mTemplate != null) {
                card.setContentView(this.mTemplate.makeContentView());
            }
            enableExpand(card);
            return card;
        }

        private void enableExpand(HwCard card) {
            if (this.isExpandable) {
                card.initCardExpandable(this.isExpanded);
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
            if (this.mContentLayout == 0 || card == null) {
                Log.w(HwCard.TAG, "mContentLayout == 0 or card is null");
                return;
            }
            View content = this.mInflater.inflate(this.mContentLayout, null);
            if (content != null) {
                card.setContentView(content);
            }
        }

        private View inflaterHeaderNoButton() {
            int layoutId = getIdentifier(ResLoaderUtil.LAYOUT, "hwcard_header_no_button");
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            if (this.mIcon != 0) {
                icon.setImageResource(this.mIcon);
            }
            return view;
        }

        private View inflaterHeaderWithOneButton() {
            int layoutId = getHeaderLayoutByType(4098);
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ImageView actionMore = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_more"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            if (this.mIcon != 0) {
                icon.setImageResource(this.mIcon);
            }
            setButtonClickEffect(actionMore);
            if (this.mActionMoreListener != null) {
                actionMore.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Builder.this.mActionMoreListener.onClick();
                    }
                });
            }
            return view;
        }

        private View inflaterHeaderWithTowButton() {
            int layoutId = getHeaderLayoutByType(HEADER_TYPE_TOW_BUTTON);
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, null);
            ImageView icon = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "icon"));
            ImageView actionMore = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_more"));
            ImageView actionInfo = (ImageView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "action_info"));
            ((TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "title"))).setText(this.mTitle);
            if (this.mIcon != 0) {
                icon.setImageResource(this.mIcon);
            }
            setButtonClickEffect(actionMore);
            if (this.mActionMoreListener != null) {
                actionMore.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Builder.this.mActionMoreListener.onClick();
                    }
                });
            }
            setButtonClickEffect(actionInfo);
            if (this.mActionInfoListener != null) {
                actionInfo.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Builder.this.mActionInfoListener.onClick();
                    }
                });
            }
            return view;
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
            if (TextUtils.isEmpty(this.mPositiveText) && this.mPositiveOnClickListener == null && TextUtils.isEmpty(this.mNegativeText) && this.mNegativeOnClickListener == null && TextUtils.isEmpty(this.mNeutralText) && this.mNeutralOnClickListener == null) {
                return null;
            }
            int layoutId = getFooterLayoutByType(8193);
            if (layoutId == 0) {
                return null;
            }
            View view = this.mInflater.inflate(layoutId, null);
            final TextView positive = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_positive"));
            final TextView negative = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_negative"));
            TextView neutral = (TextView) view.findViewById(getIdentifier(ResLoaderUtil.ID, "btn_neutral"));
            View.OnClickListener listener = new View.OnClickListener() {
                public void onClick(View v) {
                    if (v == positive) {
                        if (Builder.this.mPositiveOnClickListener != null) {
                            Builder.this.mPositiveOnClickListener.onClick();
                        }
                    } else if (v == negative) {
                        if (Builder.this.mNegativeOnClickListener != null) {
                            Builder.this.mNegativeOnClickListener.onClick();
                        }
                    } else if (v == Builder.this.mNeutralOnClickListener && Builder.this.mNeutralOnClickListener != null) {
                        Builder.this.mNeutralOnClickListener.onClick();
                    }
                }
            };
            if (!(this.mPositiveText == null && this.mPositiveOnClickListener == null)) {
                positive.setText(this.mPositiveText);
                positive.setOnClickListener(listener);
                positive.setVisibility(0);
                setButtonClickEffect(positive);
            }
            if (!(this.mNegativeText == null && this.mPositiveOnClickListener == null)) {
                negative.setText(this.mNegativeText);
                negative.setOnClickListener(listener);
                negative.setVisibility(0);
                setButtonClickEffect(negative);
            }
            if (!(this.mNeutralText == null && this.mNeutralOnClickListener == null)) {
                neutral.setText(this.mNeutralText);
                neutral.setOnClickListener(listener);
                neutral.setVisibility(0);
                setButtonClickEffect(neutral);
            }
            return view;
        }

        private View inflaterFooterCustom() {
            return null;
        }
    }

    public interface OnCardClickListener {
        void onClick();
    }

    public static abstract class Template {
        private Builder mBuilder;

        public abstract View makeContentView();

        public Template(Builder builder) {
            setBuilder(builder);
        }

        public LayoutInflater getLayoutInflater() {
            if (this.mBuilder != null) {
                return this.mBuilder.getInflater();
            }
            Log.w(HwCard.TAG, "mBuilder is null");
            return null;
        }

        private void setBuilder(Builder builder) {
            if (!(builder == null || this.mBuilder == builder || builder == null)) {
                this.mBuilder = builder;
                builder.setTemplate(this);
            }
        }

        public HwCard build() {
            if (this.mBuilder != null) {
                return this.mBuilder.build(true);
            }
            return null;
        }
    }

    private static int getIdentifier(Context context, String type, String name) {
        int id = ResLoader.getInstance().getIdentifier(context, type, name);
        if (id == 0) {
            Log.w(TAG, "resources is not found");
        }
        return id;
    }

    private HwCard(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(ResLoader.getInstance().getContext(context));
        inflateCardLayout();
    }

    private void inflateCardLayout() {
        int layoutId = getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "hwcard_layout_stubs");
        if (layoutId != 0) {
            View view = this.mInflater.inflate(layoutId, null);
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
            return;
        }
        if (headerView != null) {
            replaceStubWithView(headerView, this.mHeaderStub);
            this.mHeaderView = headerView;
        }
    }

    /* access modifiers changed from: protected */
    public void setContentView(View contentView) {
        if (this.mContentView != null) {
            Log.w(TAG, "mContentView already exists");
            return;
        }
        if (contentView != null) {
            replaceStubWithView(contentView, this.mContentStub);
            this.mContentView = contentView;
            adjustContentViewTopMargin();
        }
    }

    /* access modifiers changed from: protected */
    public void setFooterView(View footerView) {
        if (this.mFooterView != null) {
            Log.w(TAG, "mFooterView already exists");
            return;
        }
        if (footerView != null) {
            replaceStubWithView(footerView, this.mFooterStub);
            this.mFooterView = footerView;
        }
    }

    private void adjustContentViewTopMargin() {
        if (this.mContentView != null && this.mHeaderView == null) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mContentView.getLayoutParams();
            lp.topMargin = ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "margin_m"));
            this.mContentView.setLayoutParams(lp);
        }
    }

    private void replaceStubWithView(View view, ViewStub stub) {
        if (this.mCardLayout == null) {
            Log.w(TAG, "mCardLayout is null");
            return;
        }
        int index = this.mCardLayout.indexOfChild(stub);
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
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (lp != null) {
                int margin = ResLoader.getInstance().getResources(this.mContext).getDimensionPixelOffset(getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "margin_s"));
                lp.setMarginStart(margin);
                lp.setMarginEnd(margin);
                view.setLayoutParams(lp);
            }
            if (this.mCardLayout.getParent() != null) {
                ((ViewGroup) this.mCardLayout.getParent()).removeAllViews();
            }
            view.addView(this.mCardLayout);
        }
    }

    /* access modifiers changed from: private */
    public void initCardExpandable(boolean isExpanded) {
        this.mExpandState = isExpanded;
        if (this.mHeaderView != null) {
            final ImageView expandView = (ImageView) this.mHeaderView.findViewById(getIdentifier(this.mContext, ResLoaderUtil.ID, "action_expand"));
            if (expandView != null) {
                int viewVisible = 0;
                expandView.setVisibility(0);
                if (!isExpanded) {
                    viewVisible = 8;
                }
                if (this.mContentView != null) {
                    this.mContentView.setVisibility(viewVisible);
                }
                if (this.mFooterView != null) {
                    this.mFooterView.setVisibility(viewVisible);
                }
                final int expandIcon = getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_arrow_up");
                final int closeIcon = getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_arrow_down");
                expandView.setImageResource(isExpanded ? expandIcon : closeIcon);
                Builder.setButtonClickEffect(expandView);
                expandView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean unused = HwCard.this.mExpandState = !HwCard.this.mExpandState;
                        int drawableId = HwCard.this.mExpandState ? expandIcon : closeIcon;
                        if (drawableId != 0) {
                            expandView.setImageResource(drawableId);
                        }
                        int viewVisible = HwCard.this.mExpandState ? 0 : 8;
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
    }
}
