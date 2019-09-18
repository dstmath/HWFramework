package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwShowMoreView extends LinearLayout {
    private static final boolean DEBUG = true;
    private static final String TAG = "ClickMoreTextView";
    /* access modifiers changed from: private */
    public Drawable mCollapsedDrawable;
    /* access modifiers changed from: private */
    public Drawable mExpandedDrawable;
    /* access modifiers changed from: private */
    public ImageView mImageView;
    /* access modifiers changed from: private */
    public boolean mIsExpand;
    /* access modifiers changed from: private */
    public View.OnClickListener mOnMoreClickListener;
    private TextView mTextView;

    public HwShowMoreView(Context context) {
        this(context, null);
    }

    public HwShowMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "hwClickMoreViewStyle"));
    }

    public HwShowMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwShowMoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(context, "hwpattern_click_more_view"), this, DEBUG);
        this.mImageView = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hwclickmoreview_iv"));
        this.mTextView = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwclickmoreview_tv"));
        int expandId = ResLoaderUtil.getDrawableId(context, "ic_public_arrow_up");
        int collapsedId = ResLoaderUtil.getDrawableId(context, "ic_public_arrow_down");
        this.mExpandedDrawable = ResLoader.getInstance().getResources(context).getDrawable(expandId);
        this.mCollapsedDrawable = ResLoader.getInstance().getResources(context).getDrawable(collapsedId);
        setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, 0));
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = HwShowMoreView.this.mIsExpand = HwShowMoreView.this.mIsExpand ^ HwShowMoreView.DEBUG;
                if (HwShowMoreView.this.mImageView != null) {
                    HwShowMoreView.this.mImageView.setImageDrawable(HwShowMoreView.this.mIsExpand ? HwShowMoreView.this.mExpandedDrawable : HwShowMoreView.this.mCollapsedDrawable);
                }
                if (HwShowMoreView.this.mOnMoreClickListener != null) {
                    HwShowMoreView.this.mOnMoreClickListener.onClick(view);
                }
            }
        });
    }

    private void setDrawable(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setText(String text) {
        if (this.mTextView != null) {
            this.mTextView.setText(text);
        }
    }

    public String getText() {
        if (this.mTextView == null) {
            return null;
        }
        return this.mTextView.getText().toString();
    }

    public void setOnHwMoreClickListener(View.OnClickListener listener) {
        this.mOnMoreClickListener = listener;
    }
}
