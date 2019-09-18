package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwImageTextBean {
    String mContent;
    /* access modifiers changed from: private */
    public Context mContext;
    Drawable mDrawable;
    private String mLinkText;
    /* access modifiers changed from: private */
    public int mLinkTextColor;
    /* access modifiers changed from: private */
    public View.OnClickListener mListener;
    SpannableString mSpannableText;
    String mTitle;

    public HwImageTextBean(Drawable drawable, String title, String content) {
        this.mDrawable = drawable;
        this.mTitle = title;
        this.mContent = content;
    }

    public HwImageTextBean(Context context, Drawable drawable, String title, String content, String linkText, View.OnClickListener listener) {
        this.mDrawable = drawable;
        this.mTitle = title;
        this.mContent = content;
        this.mContext = context;
        this.mLinkText = linkText;
        this.mListener = listener;
        if (this.mLinkText != null) {
            SpannableString spStr = new SpannableString(this.mContent + this.mLinkText);
            spStr.setSpan(new ClickableSpan() {
                public void onClick(View widget) {
                    if (HwImageTextBean.this.mListener != null) {
                        HwImageTextBean.this.mListener.onClick(widget);
                    }
                }

                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                    Resources.Theme theme = ResLoader.getInstance().getTheme(HwImageTextBean.this.mContext);
                    if (theme != null) {
                        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{16842907});
                        int unused = HwImageTextBean.this.mLinkTextColor = typedArray.getColor(0, ResLoaderUtil.getColor(HwImageTextBean.this.mContext, "emui_functional_blue"));
                        typedArray.recycle();
                    }
                    ds.setColor(HwImageTextBean.this.mLinkTextColor);
                }
            }, spStr.length() - this.mLinkText.length(), spStr.length(), 18);
            this.mSpannableText = spStr;
        }
    }

    public HwImageTextBean(Drawable drawable, String title, String content, String linkText, int linkTextColor, View.OnClickListener listener) {
        this.mDrawable = drawable;
        this.mTitle = title;
        this.mContent = content;
        this.mLinkText = linkText;
        this.mLinkTextColor = linkTextColor;
        this.mListener = listener;
        if (this.mLinkText != null) {
            SpannableString spStr = new SpannableString(this.mContent + this.mLinkText);
            spStr.setSpan(new ClickableSpan() {
                public void onClick(View widget) {
                    if (HwImageTextBean.this.mListener != null) {
                        HwImageTextBean.this.mListener.onClick(widget);
                    }
                }

                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setColor(HwImageTextBean.this.mLinkTextColor);
                }
            }, spStr.length() - this.mLinkText.length(), spStr.length(), 18);
            this.mSpannableText = spStr;
        }
    }
}
