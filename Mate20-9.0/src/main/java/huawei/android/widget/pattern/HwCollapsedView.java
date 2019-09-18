package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwCollapsedView extends TextView {
    private static final int COLLAPSED_LINES = 3;
    private static final boolean DEBUG = false;
    private static final String ELLIPSE = "...";
    public static final int LINE_END = 1;
    private static final String REPLACE_TEXT = "@";
    private static final String TAG = "CollapsedTextView";
    public static final int TEXT_END = 0;
    private Drawable mCollapsedDrawable;
    private int mCollapsedLines;
    private Drawable mExpandedDrawable;
    private boolean mIsClicked;
    /* access modifiers changed from: private */
    public boolean mIsExpanded;
    private boolean mNeedCollapse;
    private View.OnClickListener mOnTextClickListener;
    private CharSequence mOriginalText;
    private int mShowType;
    /* access modifiers changed from: private */
    public int mShowWidth;
    /* access modifiers changed from: private */
    public CharSequence mTextContent;
    /* access modifiers changed from: private */
    public TextView.BufferType mType;

    public HwCollapsedView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public HwCollapsedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public HwCollapsedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public HwCollapsedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        int expandId = ResLoaderUtil.getDrawableId(context, "ic_public_arrow_up");
        int collapsedId = ResLoaderUtil.getDrawableId(context, "ic_public_arrow_down");
        this.mExpandedDrawable = ResLoader.getInstance().getResources(context).getDrawable(expandId, context.getTheme());
        this.mCollapsedDrawable = ResLoader.getInstance().getResources(context).getDrawable(collapsedId, context.getTheme());
        setType(0);
        setCollapsedLines(3);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                HwCollapsedView.this.handlerClick(view);
            }
        });
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(0);
    }

    private void setDrawable() {
        Drawable[] drawables = getCompoundDrawablesRelative();
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], this.mIsClicked ? this.mExpandedDrawable : this.mCollapsedDrawable, drawables[3]);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mShowWidth != 0 && this.mShowWidth != getMeasuredWidth()) {
            this.mShowWidth = 0;
            this.mIsExpanded = DEBUG;
            setText(this.mTextContent);
        }
    }

    public void setText(CharSequence text, TextView.BufferType type) {
        this.mTextContent = text;
        this.mType = type;
        if (this.mShowType != 0) {
            super.setText(text, type);
        } else if (TextUtils.isEmpty(text) || this.mCollapsedLines == 0) {
            super.setText(text, type);
            this.mNeedCollapse = DEBUG;
        } else if (getExpandState() && this.mNeedCollapse) {
            this.mOriginalText = text;
            formatExpandedText(type);
        } else if (this.mShowWidth == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= 16) {
                        HwCollapsedView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        HwCollapsedView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    int unused = HwCollapsedView.this.mShowWidth = (HwCollapsedView.this.getWidth() - HwCollapsedView.this.getPaddingLeft()) - HwCollapsedView.this.getPaddingRight();
                    HwCollapsedView.this.formatCollapsedText(HwCollapsedView.this.mType, HwCollapsedView.this.mTextContent);
                }
            });
        } else {
            formatCollapsedText(type, text);
        }
    }

    /* access modifiers changed from: private */
    public void formatCollapsedText(TextView.BufferType type, CharSequence text) {
        TextView.BufferType bufferType = type;
        this.mOriginalText = text;
        Layout layout = getLayout();
        TextPaint paint = getPaint();
        if (layout == null || !layout.getText().equals(this.mOriginalText)) {
            super.setText(this.mOriginalText, bufferType);
            layout = getLayout();
        }
        if (layout.getLineCount() <= this.mCollapsedLines) {
            this.mNeedCollapse = DEBUG;
            this.mIsExpanded = DEBUG;
            super.setText(this.mOriginalText, bufferType);
            return;
        }
        this.mNeedCollapse = true;
        int lastLineStart = layout.getLineStart(this.mCollapsedLines - 1);
        int lastLineEnd = layout.getLineVisibleEnd(this.mCollapsedLines - 1);
        float textWidth = paint.measureText(this.mOriginalText, lastLineStart, lastLineEnd);
        float lastLineWidth = layout.getLineWidth(this.mCollapsedLines - 1);
        Drawable drawable = getExpandState() ? this.mExpandedDrawable : this.mCollapsedDrawable;
        int expandedTextWidth = (int) ((((paint.measureText(ELLIPSE) + ((float) drawable.getIntrinsicWidth())) + 0.5f) + lastLineWidth) - textWidth);
        if (lastLineWidth + ((float) expandedTextWidth) > ((float) this.mShowWidth)) {
            float[] measureWidth = new float[1];
            int expandedTextWidth2 = expandedTextWidth;
            Drawable drawable2 = drawable;
            int cutCount = paint.breakText(this.mOriginalText, lastLineStart, lastLineEnd, DEBUG, (float) expandedTextWidth, measureWidth);
            if (measureWidth[0] < ((float) expandedTextWidth2)) {
                cutCount++;
            }
            lastLineEnd -= cutCount;
            if (lastLineEnd < 0) {
                lastLineEnd = 0;
            }
        } else {
            Drawable drawable3 = drawable;
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.append(this.mOriginalText.subSequence(0, lastLineEnd));
        spannable.append(ELLIPSE);
        setSpan(spannable);
        super.setText(spannable, bufferType);
    }

    private void formatExpandedText(TextView.BufferType type) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(this.mOriginalText);
        setSpan(spannable);
        super.setText(spannable, type);
    }

    private void setSpan(SpannableStringBuilder spannable) {
        spannable.append(REPLACE_TEXT);
        int tipsLen = REPLACE_TEXT.length();
        Drawable drawable = getExpandState() ? this.mExpandedDrawable : this.mCollapsedDrawable;
        spannable.setSpan(null, spannable.length() - tipsLen, spannable.length(), 17);
        if (drawable != null) {
            spannable.setSpan(new VerticalImageSpan(getContext(), drawable), spannable.length() - tipsLen, spannable.length(), 17);
        }
    }

    /* access modifiers changed from: private */
    public void handlerClick(View v) {
        if (this.mShowType == 0 && this.mNeedCollapse) {
            this.mIsExpanded = !this.mIsExpanded;
            setText(this.mOriginalText);
        }
        if (this.mShowType == 1) {
            this.mIsClicked = !this.mIsClicked;
            setDrawable();
        }
        if (this.mOnTextClickListener != null) {
            this.mOnTextClickListener.onClick(v);
        }
    }

    public void setType(int type) {
        if (type != this.mShowType) {
            this.mShowType = type;
            if (this.mShowType == 0) {
                setExpandedDrawable(this.mExpandedDrawable);
                setCollapsedDrawable(this.mCollapsedDrawable);
                clearDrawable();
            } else if (this.mShowType == 1) {
                setDrawable();
            }
            setText(this.mTextContent);
        }
    }

    private void clearDrawable() {
        Drawable[] drawables = getCompoundDrawablesRelative();
        if (drawables != null && drawables[2] != null) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], null, drawables[3]);
        }
    }

    public CharSequence getText() {
        if (this.mShowType == 0) {
            return this.mTextContent;
        }
        return super.getText();
    }

    public boolean getExpandState() {
        return this.mIsExpanded;
    }

    public void setExpandState(final boolean expand) {
        post(new Runnable() {
            public void run() {
                boolean unused = HwCollapsedView.this.mIsExpanded = expand;
                HwCollapsedView.this.setText(HwCollapsedView.this.mTextContent);
            }
        });
    }

    public void setCollapsedLines(int line) {
        this.mCollapsedLines = line;
        if (this.mShowType == 0) {
            setText(this.mTextContent);
        }
    }

    private void setExpandedDrawable(Drawable expandedDrawable) {
        if (expandedDrawable != null) {
            this.mExpandedDrawable = expandedDrawable;
            this.mExpandedDrawable.setBounds(0, 0, this.mExpandedDrawable.getIntrinsicWidth(), this.mExpandedDrawable.getIntrinsicHeight());
        }
    }

    private void setCollapsedDrawable(Drawable collapsedDrawable) {
        if (collapsedDrawable != null) {
            this.mCollapsedDrawable = collapsedDrawable;
            this.mCollapsedDrawable.setBounds(0, 0, this.mCollapsedDrawable.getIntrinsicWidth(), this.mCollapsedDrawable.getIntrinsicHeight());
        }
    }

    public void setOnCollapsedClickListener(View.OnClickListener listener) {
        this.mOnTextClickListener = listener;
    }
}
