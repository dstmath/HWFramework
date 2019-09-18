package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pools;
import android.view.View;
import com.android.internal.R;
import java.util.ArrayList;

public class ProgressButton extends View {
    private static final String ELLIPSIS = "...";
    private static final int MAX_LEVEL = 10000;
    private static final String TAG = "ProgressButton";
    private boolean mAttached;
    private int mMax;
    private int mMaxHeight;
    private int mMaxWidth;
    private int mMinHeight;
    private int mMinWidth;
    private Paint mPaint;
    private int mProgress;
    private Drawable mProgressDrawable;
    /* access modifiers changed from: private */
    public final ArrayList<RefreshData> mRefreshData;
    /* access modifiers changed from: private */
    public boolean mRefreshIsPosted;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private CharSequence mText;
    private int mTextColor;
    private int mTextPaddingLeft;
    private int mTextPaddingRight;
    private int mTextSize;
    private long mUiThreadId;

    private static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final Pools.SynchronizedPool<RefreshData> sPool = new Pools.SynchronizedPool<>(POOL_MAX);
        public boolean fromUser;
        public int id;
        public int progress;

        private RefreshData() {
        }

        public static RefreshData obtain(int id2, int progress2, boolean fromUser2) {
            RefreshData rd = (RefreshData) sPool.acquire();
            if (rd == null) {
                rd = new RefreshData();
            }
            rd.id = id2;
            rd.progress = progress2;
            rd.fromUser = fromUser2;
            return rd;
        }

        public void recycle() {
            sPool.release(this);
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        private RefreshProgressRunnable() {
        }

        public void run() {
            synchronized (ProgressButton.this) {
                int count = ProgressButton.this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = (RefreshData) ProgressButton.this.mRefreshData.get(i);
                    ProgressButton.this.doRefreshProgress(rd.id, rd.progress, rd.fromUser, true);
                    rd.recycle();
                }
                ProgressButton.this.mRefreshData.clear();
                boolean unused = ProgressButton.this.mRefreshIsPosted = false;
            }
        }
    }

    public static final class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private static SavedState instance = null;
        int progress;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.progress = in.readInt();
        }

        public static SavedState getSavedStateInstance(Parcelable superState) {
            if (instance == null) {
                instance = new SavedState(superState);
            }
            return instance;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.progress);
        }
    }

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle, styleRes);
        this.mRefreshData = new ArrayList<>();
        this.mUiThreadId = Thread.currentThread().getId();
        initProgressButton(context);
        init();
        initAttrs(context, attrs, defStyle, styleRes);
    }

    private synchronized void initProgressButton(Context context) {
        this.mMax = 100;
        this.mProgress = 0;
        Resources res = context.getResources();
        this.mMaxWidth = res.getDimensionPixelSize(34472485);
        this.mMinWidth = res.getDimensionPixelSize(34472487);
        this.mMaxHeight = res.getDimensionPixelSize(34472484);
        this.mMinHeight = res.getDimensionPixelSize(34472486);
        this.mTextSize = res.getDimensionPixelSize(34472491);
        this.mTextPaddingLeft = res.getDimensionPixelSize(34472489);
        this.mTextPaddingRight = res.getDimensionPixelSize(34472490);
        this.mProgressDrawable = res.getDrawable(33751824);
        this.mTextColor = res.getColor(33882848);
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize((float) getTextSize());
        this.mPaint.setColor(getTextColor());
        setClickable(true);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    private synchronized void initAttrs(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        if (attrs != null) {
            TypedArray aTextView = context.obtainStyledAttributes(attrs, R.styleable.TextView, defStyle, styleRes);
            if (aTextView != null) {
                int n = aTextView.getIndexCount();
                for (int i = 0; i < n; i++) {
                    int attr = aTextView.getIndex(i);
                    if (attr == 2) {
                        setTextSize(aTextView.getDimensionPixelSize(attr, this.mTextSize));
                    } else if (attr == 5) {
                        setTextColor(aTextView.getColor(attr, this.mTextColor));
                    } else if (attr == 18) {
                        this.mText = aTextView.getText(attr);
                    }
                }
                setText(this.mText);
                aTextView.recycle();
            }
            TypedArray aProgressBar = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
            if (aProgressBar != null) {
                int n2 = aProgressBar.getIndexCount();
                for (int i2 = 0; i2 < n2; i2++) {
                    int attr2 = aProgressBar.getIndex(i2);
                    if (attr2 != 8) {
                        switch (attr2) {
                            case 0:
                                this.mMaxWidth = aProgressBar.getDimensionPixelSize(attr2, this.mMaxWidth);
                                break;
                            case 1:
                                this.mMaxHeight = aProgressBar.getDimensionPixelSize(attr2, this.mMaxHeight);
                                break;
                            case 2:
                                setMax(aProgressBar.getInt(attr2, this.mMax));
                                break;
                            case 3:
                                setProgress(aProgressBar.getInt(attr2, this.mProgress));
                                break;
                            default:
                                switch (attr2) {
                                    case 11:
                                        this.mMinWidth = aProgressBar.getDimensionPixelSize(attr2, this.mMinWidth);
                                        break;
                                    case 12:
                                        this.mMinHeight = aProgressBar.getDimensionPixelSize(attr2, this.mMinHeight);
                                        break;
                                }
                        }
                    } else {
                        Drawable progressDrawable = aProgressBar.getDrawable(attr2);
                        if (progressDrawable != null) {
                            setProgressDrawable(progressDrawable);
                        }
                    }
                }
                aProgressBar.recycle();
            }
            TypedArray a = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.ProgressButton, defStyle, styleRes);
            if (a != null) {
                this.mTextPaddingLeft = a.getDimensionPixelSize(0, this.mTextPaddingLeft);
                this.mTextPaddingRight = a.getDimensionPixelSize(1, this.mTextPaddingRight);
                a.recycle();
            }
        }
    }

    public synchronized Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public void setProgressDrawable(Drawable d) {
        setProgressDrawable(d, 0);
    }

    public synchronized void setProgressDrawable(Drawable d, int progress) {
        boolean needUpdate;
        if (this.mProgressDrawable == null || d == this.mProgressDrawable) {
            needUpdate = false;
        } else {
            this.mProgressDrawable.setCallback(null);
            unscheduleDrawable(this.mProgressDrawable);
            needUpdate = true;
        }
        this.mProgressDrawable = d;
        if (d != null) {
            d.setCallback(this);
        }
        if (needUpdate) {
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            if (progress < 0) {
                progress = 0;
            }
            if (progress > this.mMax) {
                progress = this.mMax;
            }
            this.mProgress = progress;
            doRefreshProgress(16908301, this.mProgress, false, false);
        } else {
            setProgress(progress);
        }
    }

    public synchronized void setTextPaddingLeft(int textPaddingLeft) {
        this.mTextPaddingLeft = textPaddingLeft;
    }

    public synchronized void setTextPaddingRight(int textPaddingRight) {
        this.mTextPaddingRight = textPaddingRight;
    }

    public synchronized void setText(CharSequence text) {
        if (text == null) {
            try {
                this.mText = "";
            } catch (Throwable th) {
                throw th;
            }
        } else {
            this.mText = text;
        }
        invalidate();
    }

    public synchronized CharSequence getText() {
        return this.mText;
    }

    public synchronized int getTextSize() {
        return this.mTextSize;
    }

    public synchronized void setTextSize(int textSize) {
        if (this.mTextSize != textSize) {
            this.mTextSize = textSize;
            this.mPaint.setTextSize((float) textSize);
            invalidate();
        }
    }

    public int getTextColor() {
        return this.mTextColor;
    }

    public void setTextColor(int textColor) {
        if (this.mTextColor != textColor) {
            this.mTextColor = textColor;
            getPaint().setColor(textColor);
        }
    }

    public synchronized void setTypeface(Typeface typeFace) {
        this.mPaint.setTypeface(typeFace);
        invalidate();
    }

    public synchronized Paint getPaint() {
        return this.mPaint;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean verifyDrawable(Drawable who) {
        return who == this.mProgressDrawable || super.verifyDrawable(who);
    }

    public synchronized void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: private */
    public synchronized void doRefreshProgress(int id, int progress, boolean fromUser, boolean callBackToApp) {
        float scale = this.mMax > 0 ? ((float) progress) / ((float) this.mMax) : 0.0f;
        Drawable d = this.mProgressDrawable;
        if (d != null) {
            d.setLevel((int) (10000.0f * scale));
            invalidate();
        }
        if (callBackToApp && id == 16908301) {
            onProgressRefresh(scale, fromUser);
        }
    }

    private void onProgressRefresh(float scale, boolean fromUser) {
    }

    public synchronized int getProgress() {
        return this.mProgress;
    }

    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    private synchronized void setProgress(int progress, boolean fromUser) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
            refreshProgress(16908301, this.mProgress, fromUser);
        }
    }

    private synchronized void refreshProgress(int id, int progress, boolean fromUser) {
        if (this.mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, fromUser, true);
        } else {
            if (this.mRefreshProgressRunnable == null) {
                this.mRefreshProgressRunnable = new RefreshProgressRunnable();
            }
            this.mRefreshData.add(RefreshData.obtain(id, progress, fromUser));
            if (this.mAttached && !this.mRefreshIsPosted) {
                post(this.mRefreshProgressRunnable);
                this.mRefreshIsPosted = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mRefreshData != null) {
            synchronized (this) {
                int count = this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = this.mRefreshData.get(i);
                    doRefreshProgress(rd.id, rd.progress, rd.fromUser, true);
                    rd.recycle();
                }
                this.mRefreshData.clear();
            }
        }
        this.mAttached = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        if (this.mRefreshProgressRunnable != null) {
            removeCallbacks(this.mRefreshProgressRunnable);
            this.mRefreshIsPosted = false;
        }
        super.onDetachedFromWindow();
        this.mAttached = false;
    }

    public synchronized int getMax() {
        return this.mMax;
    }

    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            postInvalidate();
            if (this.mProgress > max) {
                this.mProgress = max;
            }
            refreshProgress(16908301, this.mProgress, false);
        }
    }

    public final synchronized void incrementProgressBy(int diff) {
        setProgress(this.mProgress + diff);
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
    }

    private synchronized void updateDrawableBounds(int w, int h) {
        int w2 = w - (getPaddingRight() + getPaddingLeft());
        int right = w2;
        int bottom = h - (getPaddingTop() + getPaddingBottom());
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;
        Drawable d = this.mProgressDrawable;
        if (d != null) {
            dw = maxInt(this.mMinWidth, minInt(this.mMaxWidth, d.getIntrinsicWidth()));
            dh = maxInt(this.mMinHeight, minInt(this.mMaxHeight, d.getIntrinsicHeight()));
        }
        updateDrawableState();
        setMeasuredDimension(resolveSizeAndState(dw + getPaddingLeft() + getPaddingRight(), widthMeasureSpec, 0), resolveSizeAndState(dh + getPaddingTop() + getPaddingBottom(), heightMeasureSpec, 0));
    }

    /* access modifiers changed from: protected */
    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable d = this.mProgressDrawable;
        if (d != null) {
            d.draw(canvas);
        }
        drawText(canvas);
    }

    private synchronized void drawText(Canvas canvas) {
        if (this.mText != null && this.mText.length() > 0) {
            CharSequence tempText = this.mText.toString().intern();
            int textWidth = (int) (this.mPaint.measureText(tempText, 0, tempText.length()) + 0.5f);
            int allowMaxWidth = (((getWidth() - getPaddingLeft()) - getPaddingRight()) - this.mTextPaddingLeft) - this.mTextPaddingRight;
            if (textWidth > allowMaxWidth) {
                tempText = getInterceptText(tempText, allowMaxWidth);
            }
            Rect rect = new Rect();
            int tempWidth = 0;
            if (tempText != null && tempText.length() > 0) {
                this.mPaint.getTextBounds(tempText.toString(), 0, tempText.length(), rect);
                tempWidth = (int) this.mPaint.measureText(tempText, 0, tempText.length());
            }
            int x = (int) (((((double) getWidth()) / 2.0d) - (((double) tempWidth) / 2.0d)) + 0.5d);
            canvas.drawText(tempText, 0, tempText.length(), (float) x, (float) ((int) (((((double) getHeight()) / 2.0d) - ((double) rect.centerY())) + 0.5d)), this.mPaint);
        } else if (this.mText != null && this.mText.length() == 0) {
            canvas.drawText(this.mText, 0, 0, (float) 0, (float) 0, this.mPaint);
        }
    }

    private CharSequence getInterceptText(CharSequence text, int maxWidth) {
        int textLenth = getText().length();
        int tempMax = maxWidth - ((int) (this.mPaint.measureText(ELLIPSIS, 0, ELLIPSIS.length()) + 0.5f));
        if (tempMax > 0) {
            int i = 0;
            while (i < textLenth && ((int) (this.mPaint.measureText(text, 0, i) + 0.5f)) <= tempMax) {
                i++;
            }
            return text.toString().substring(0, i) + ELLIPSIS;
        }
        int i2 = 0;
        while (i2 < textLenth && ((int) (this.mPaint.measureText(text, 0, i2) + 0.5f)) <= maxWidth) {
            i2++;
        }
        return text.toString().substring(0, i2);
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private synchronized void updateDrawableState() {
        int[] state = getDrawableState();
        if (this.mProgressDrawable != null && this.mProgressDrawable.isStateful()) {
            this.mProgressDrawable.setState(state);
        }
    }

    public synchronized Parcelable onSaveInstanceState() {
        SavedState ss;
        ss = SavedState.getSavedStateInstance(super.onSaveInstanceState());
        ss.progress = this.mProgress;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
    }

    private int maxInt(int a, int b) {
        return a > b ? a : b;
    }

    private int minInt(int a, int b) {
        return a < b ? a : b;
    }
}
