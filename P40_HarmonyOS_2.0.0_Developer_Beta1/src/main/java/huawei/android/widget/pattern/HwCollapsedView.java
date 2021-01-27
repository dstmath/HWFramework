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
    private static final int DRAWABLE_BORDER_INDEX_BOTTOM = 3;
    private static final int DRAWABLE_BORDER_INDEX_END = 2;
    private static final int DRAWABLE_BORDER_INDEX_START = 0;
    private static final int DRAWABLE_BORDER_INDEX_TOP = 1;
    private static final String ELLIPSE = "...";
    private static final float FLOAT_TO_INT_ROUND_FACTOR = 0.5f;
    public static final int LINE_END = 1;
    private static final String REPLACE_TEXT = "@";
    private static final String TAG = "CollapsedTextView";
    public static final int TEXT_END = 0;
    private Drawable mCollapsedDrawable;
    private int mCollapsedLines;
    private Drawable mExpandedDrawable;
    private boolean mIsClicked;
    private boolean mIsExpanded;
    private boolean mIsNeedCollapse;
    private View.OnClickListener mOnTextClickListener;
    private CharSequence mOriginalText;
    private int mShowType;
    private int mShowWidth;
    private CharSequence mTextContent;
    private TextView.BufferType mType;

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
            /* class huawei.android.widget.pattern.HwCollapsedView.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
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
    @Override // android.widget.TextView, android.view.View
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        int i = this.mShowWidth;
        if (i != 0 && i != getMeasuredWidth()) {
            this.mShowWidth = 0;
            this.mIsExpanded = DEBUG;
            setText(this.mTextContent);
        }
    }

    @Override // android.widget.TextView
    public void setText(CharSequence text, TextView.BufferType type) {
        this.mTextContent = text;
        this.mType = type;
        if (this.mShowType != 0) {
            super.setText(text, type);
        } else if (TextUtils.isEmpty(text) || this.mCollapsedLines == 0) {
            super.setText(text, type);
            this.mIsNeedCollapse = DEBUG;
        } else if (getExpandState() && this.mIsNeedCollapse) {
            this.mOriginalText = text;
            formatExpandedText(type);
        } else if (this.mShowWidth == 0) {
            addGlobalLayoutListener();
        } else {
            formatCollapsedText(type, text);
        }
    }

    private void addGlobalLayoutListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class huawei.android.widget.pattern.HwCollapsedView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    HwCollapsedView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    HwCollapsedView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                HwCollapsedView hwCollapsedView = HwCollapsedView.this;
                hwCollapsedView.mShowWidth = (hwCollapsedView.getWidth() - HwCollapsedView.this.getPaddingLeft()) - HwCollapsedView.this.getPaddingRight();
                HwCollapsedView hwCollapsedView2 = HwCollapsedView.this;
                hwCollapsedView2.formatCollapsedText(hwCollapsedView2.mType, HwCollapsedView.this.mTextContent);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void formatCollapsedText(TextView.BufferType type, CharSequence text) {
        CharSequence charSequence;
        this.mOriginalText = text;
        Layout layout = getLayout();
        TextPaint paint = getPaint();
        if (layout == null || !layout.getText().equals(this.mOriginalText)) {
            super.setText(this.mOriginalText, type);
            layout = getLayout();
        }
        int line = layout.getLineCount();
        int i = this.mCollapsedLines;
        if (line <= i) {
            this.mIsNeedCollapse = DEBUG;
            this.mIsExpanded = DEBUG;
            super.setText(this.mOriginalText, type);
            return;
        }
        this.mIsNeedCollapse = true;
        int lastLineStart = layout.getLineStart(i - 1);
        int lastLineEnd = layout.getLineVisibleEnd(this.mCollapsedLines - 1);
        float textWidth = paint.measureText(this.mOriginalText, lastLineStart, lastLineEnd);
        float lastLineWidth = layout.getLineWidth(this.mCollapsedLines - 1);
        int expandedTextWidth = (int) ((((paint.measureText(ELLIPSE) + ((float) (getExpandState() ? this.mExpandedDrawable : this.mCollapsedDrawable).getIntrinsicWidth())) + FLOAT_TO_INT_ROUND_FACTOR) + lastLineWidth) - textWidth);
        if (lastLineWidth + ((float) expandedTextWidth) > ((float) this.mShowWidth)) {
            float[] measureWidths = new float[1];
            charSequence = ELLIPSE;
            int cutCount = paint.breakText(this.mOriginalText, lastLineStart, lastLineEnd, DEBUG, (float) expandedTextWidth, measureWidths);
            if (measureWidths[0] < ((float) expandedTextWidth)) {
                cutCount++;
            }
            int lastLineEnd2 = lastLineEnd - cutCount;
            lastLineEnd = lastLineEnd2 < 0 ? 0 : lastLineEnd2;
        } else {
            charSequence = ELLIPSE;
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.append(this.mOriginalText.subSequence(0, lastLineEnd));
        spannable.append(charSequence);
        setSpan(spannable);
        super.setText(spannable, type);
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
    /* access modifiers changed from: public */
    private void handlerClick(View view) {
        if (this.mShowType == 0 && this.mIsNeedCollapse) {
            this.mIsExpanded = !this.mIsExpanded;
            setText(this.mOriginalText);
        }
        if (this.mShowType == 1) {
            this.mIsClicked = !this.mIsClicked;
            setDrawable();
        }
        View.OnClickListener onClickListener = this.mOnTextClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public void setType(int type) {
        if (type != this.mShowType) {
            this.mShowType = type;
            int i = this.mShowType;
            if (i == 0) {
                setExpandedDrawable(this.mExpandedDrawable);
                setCollapsedDrawable(this.mCollapsedDrawable);
                clearDrawable();
            } else if (i == 1) {
                setDrawable();
            }
            setText(this.mTextContent);
        }
    }

    private void clearDrawable() {
        Drawable[] drawables = getCompoundDrawablesRelative();
        if (drawables != null && drawables[2] != null) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], (Drawable) null, drawables[3]);
        }
    }

    @Override // android.widget.TextView
    public CharSequence getText() {
        if (this.mShowType == 0) {
            return this.mTextContent;
        }
        return super.getText();
    }

    public boolean getExpandState() {
        return this.mIsExpanded;
    }

    public void setExpandState(final boolean isExpand) {
        post(new Runnable() {
            /* class huawei.android.widget.pattern.HwCollapsedView.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwCollapsedView.this.mIsExpanded = isExpand;
                HwCollapsedView hwCollapsedView = HwCollapsedView.this;
                hwCollapsedView.setText(hwCollapsedView.mTextContent);
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
            Drawable drawable = this.mExpandedDrawable;
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), this.mExpandedDrawable.getIntrinsicHeight());
        }
    }

    private void setCollapsedDrawable(Drawable collapsedDrawable) {
        if (collapsedDrawable != null) {
            this.mCollapsedDrawable = collapsedDrawable;
            Drawable drawable = this.mCollapsedDrawable;
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), this.mCollapsedDrawable.getIntrinsicHeight());
        }
    }

    public void setOnCollapsedClickListener(View.OnClickListener listener) {
        this.mOnTextClickListener = listener;
    }
}
