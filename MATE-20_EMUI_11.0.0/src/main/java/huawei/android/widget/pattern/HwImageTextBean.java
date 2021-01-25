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
    private Context mContext;
    Drawable mDrawable;
    private String mLinkText;
    private int mLinkTextColor;
    private View.OnClickListener mListener;
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
            SpannableString spannableStr = new SpannableString(this.mContent + this.mLinkText);
            spannableStr.setSpan(new ClickableSpan() {
                /* class huawei.android.widget.pattern.HwImageTextBean.AnonymousClass1 */

                @Override // android.text.style.ClickableSpan
                public void onClick(View widget) {
                    if (HwImageTextBean.this.mListener != null) {
                        HwImageTextBean.this.mListener.onClick(widget);
                    }
                }

                @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setUnderlineText(false);
                    Resources.Theme theme = ResLoader.getInstance().getTheme(HwImageTextBean.this.mContext);
                    if (theme != null) {
                        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{16842907});
                        HwImageTextBean hwImageTextBean = HwImageTextBean.this;
                        hwImageTextBean.mLinkTextColor = typedArray.getColor(0, ResLoaderUtil.getColor(hwImageTextBean.mContext, "emui_functional_blue"));
                        typedArray.recycle();
                    }
                    textPaint.setColor(HwImageTextBean.this.mLinkTextColor);
                }
            }, spannableStr.length() - this.mLinkText.length(), spannableStr.length(), 18);
            this.mSpannableText = spannableStr;
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
            SpannableString spannableStr = new SpannableString(this.mContent + this.mLinkText);
            spannableStr.setSpan(new ClickableSpan() {
                /* class huawei.android.widget.pattern.HwImageTextBean.AnonymousClass2 */

                @Override // android.text.style.ClickableSpan
                public void onClick(View widget) {
                    if (HwImageTextBean.this.mListener != null) {
                        HwImageTextBean.this.mListener.onClick(widget);
                    }
                }

                @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setUnderlineText(false);
                    textPaint.setColor(HwImageTextBean.this.mLinkTextColor);
                }
            }, spannableStr.length() - this.mLinkText.length(), spannableStr.length(), 18);
            this.mSpannableText = spannableStr;
        }
    }
}
